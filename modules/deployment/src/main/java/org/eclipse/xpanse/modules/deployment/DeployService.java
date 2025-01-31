/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment;

import static org.eclipse.xpanse.modules.async.TaskConfiguration.ASYNC_EXECUTOR_NAME;
import static org.eclipse.xpanse.modules.logging.LoggingKeyConstant.SERVICE_ID;

import jakarta.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.utils.EntityTransUtils;
import org.eclipse.xpanse.modules.deployment.polling.ServiceDeploymentStatusChangePolling;
import org.eclipse.xpanse.modules.logging.CustomRequestIdGenerator;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.config.ServiceLockConfig;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.service.deploy.DeployResource;
import org.eclipse.xpanse.modules.models.service.deploy.DeploymentStatusUpdate;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.BillingModeNotSupported;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.EulaNotAccepted;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.InvalidServiceStateException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceFlavorDowngradeNotAllowed;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceLockedException;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.ServiceModifyParamsNotFoundException;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.enums.DeployerTaskStatus;
import org.eclipse.xpanse.modules.models.service.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.models.service.modify.ModifyRequest;
import org.eclipse.xpanse.modules.models.service.order.ServiceOrder;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.utils.ServiceDeployVariablesJsonSchemaValidator;
import org.eclipse.xpanse.modules.models.servicetemplate.DeployVariable;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavor;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotApproved;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.modules.orchestrator.PluginManager;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployResult;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployer;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeploymentScenario;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Main class which orchestrates the OCL request processing. Calls the available plugins to deploy
 * managed service in the respective infrastructure as defined in the OCL.
 */
@Slf4j
@Component
public class DeployService {
    @Resource
    private UserServiceHelper userServiceHelper;
    @Resource
    private PluginManager pluginManager;
    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;
    @Resource
    private DeployServiceStorage deployServiceStorage;
    @Resource
    private ServiceDeployVariablesJsonSchemaValidator serviceDeployVariablesJsonSchemaValidator;
    @Resource
    private PolicyValidator policyValidator;
    @Resource
    private SensitiveDataHandler sensitiveDataHandler;
    @Resource
    private DeployServiceEntityHandler deployServiceEntityHandler;
    @Resource
    private DeployResultManager deployResultManager;
    @Resource
    private DeployerKindManager deployerKindManager;
    @Resource
    private DeployServiceEntityConverter deployServiceEntityConverter;
    @Resource
    private ServiceStateManager serviceStateManager;
    @Resource
    private ServiceOrderManager serviceOrderManager;
    @Resource
    private ServiceDeploymentStatusChangePolling serviceDeploymentStatusChangePolling;
    @Resource(name = ASYNC_EXECUTOR_NAME)
    private Executor taskExecutor;


    /**
     * Create order to deploy new service.
     *
     * @param deployRequest deploy request.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToDeployNewService(DeployRequest deployRequest) {
        UUID newServiceId = UUID.randomUUID();
        MDC.put(SERVICE_ID, newServiceId.toString());
        deployRequest.setServiceId(newServiceId);
        deployRequest.setUserId(this.userServiceHelper.getCurrentUserId());
        DeployTask deployTask = createNewDeployTask(deployRequest);
        deployService(deployTask);
        return new ServiceOrder(deployTask.getOrderId(), deployTask.getServiceId());
    }


    /**
     * Create order to redeploy failed service.
     *
     * @param serviceId failed service id.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToRedeployFailedService(UUID serviceId) {
        MDC.put(SERVICE_ID, serviceId.toString());
        String errMsg = "No permissions to redeploy services belonging to other users.";
        DeployServiceEntity deployServiceEntity = getServiceOwnedByCurrentUser(serviceId, errMsg);
        DeployTask redeployTask = getRedeployTask(deployServiceEntity);
        redeployService(redeployTask, deployServiceEntity);
        log.info("Order task {} to redeploy failed service {} started.",
                redeployTask.getOrderId(), redeployTask.getServiceId());
        return new ServiceOrder(redeployTask.getOrderId(), redeployTask.getServiceId());
    }


    /**
     * Create order to modify deployed service.
     *
     * @param serviceId     deployed service id.
     * @param modifyRequest modify request.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToModifyDeployedService(UUID serviceId,
                                                           ModifyRequest modifyRequest) {
        MDC.put(SERVICE_ID, serviceId.toString());
        modifyRequest.setUserId(this.userServiceHelper.getCurrentUserId());
        String errMsg = "No permissions to modify services belonging to other users.";
        DeployServiceEntity deployServiceEntity = getServiceOwnedByCurrentUser(serviceId, errMsg);
        DeployTask modifyTask = getModifyTask(modifyRequest, deployServiceEntity);
        modifyService(modifyTask, deployServiceEntity);
        log.info("Order task {} to modify deployed service {} started.",
                modifyTask.getOrderId(), modifyTask.getServiceId());
        return new ServiceOrder(modifyTask.getOrderId(), modifyTask.getServiceId());
    }

    /**
     * Create order to destroy deployed service.
     *
     * @param serviceId deployed service id.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToDestroyDeployedService(UUID serviceId) {
        MDC.put(SERVICE_ID, serviceId.toString());
        String errMsg = "No permissions to destroy services belonging to other users.";
        DeployServiceEntity deployServiceEntity = getServiceOwnedByCurrentUser(serviceId, errMsg);
        DeployTask destroyTask = getDestroyTask(deployServiceEntity);
        destroyService(destroyTask, deployServiceEntity);
        log.info("Order task {} to destroy deployed service {} started.",
                destroyTask.getOrderId(), destroyTask.getServiceId());
        return new ServiceOrder(destroyTask.getOrderId(), destroyTask.getServiceId());
    }


    /**
     * Create order to purge destroyed service.
     *
     * @param serviceId destroyed service id.
     * @return ServiceOrder.
     */
    public ServiceOrder createOrderToPurgeDestroyedService(UUID serviceId) {
        MDC.put(SERVICE_ID, serviceId.toString());
        String errMsg = "No permissions to purge services belonging to other users.";
        DeployServiceEntity deployServiceEntity = getServiceOwnedByCurrentUser(serviceId, errMsg);
        DeployTask purgeTask = getPurgeTask(deployServiceEntity);
        purgeService(purgeTask, deployServiceEntity);
        log.info("Order task {} to purge the destroyed service {} started.",
                purgeTask.getOrderId(), serviceId);
        return new ServiceOrder(purgeTask.getOrderId(), purgeTask.getServiceId());
    }


    /**
     * Get availability zones of region.
     *
     * @param csp        cloud service provider.
     * @param siteName   the site of the region belongs to.
     * @param regionName region name.
     * @param serviceId  deployed service id.
     * @return List of availability zones.
     */
    public List<String> getAvailabilityZonesOfRegion(Csp csp, String siteName, String regionName,
                                                     UUID serviceId) {
        String currentUserId = this.userServiceHelper.getCurrentUserId();
        OrchestratorPlugin orchestratorPlugin = pluginManager.getOrchestratorPlugin(csp);
        return orchestratorPlugin.getAvailabilityZonesOfRegion(
                siteName, regionName, currentUserId, serviceId);
    }


    /**
     * List resources of service.
     *
     * @param serviceId    service id.
     * @param resourceKind resource kind.
     * @return List of DeployResource.
     */
    public List<DeployResource> listResourcesOfDeployedService(UUID serviceId,
                                                               DeployResourceKind resourceKind) {
        String errorMsg = "No permissions to view resources of services belonging to other users.";
        DeployServiceEntity deployedService = getServiceOwnedByCurrentUser(serviceId, errorMsg);
        Stream<DeployResourceEntity> resourceEntities =
                deployedService.getDeployResourceList().stream();
        if (Objects.nonNull(resourceKind)) {
            resourceEntities = resourceEntities.filter(
                    resourceEntity -> resourceEntity.getResourceKind().equals(resourceKind));
        }
        return EntityTransUtils.transToDeployResourceList(resourceEntities.toList());
    }

    /**
     * Get user managed service.
     *
     * @param serviceId deployed service id.
     * @param errorMsg  the error message.
     * @return DeployServiceEntity.
     * @throws AccessDeniedException if the current user is not the owner of the service.
     */
    private DeployServiceEntity getServiceOwnedByCurrentUser(UUID serviceId, String errorMsg) {
        DeployServiceEntity deployedService =
                deployServiceEntityHandler.getDeployServiceEntity(serviceId);
        boolean currentUserIsOwner =
                userServiceHelper.currentUserIsOwner(deployedService.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException(errorMsg);
        }
        return deployedService;
    }

    /**
     * Create new deploy task by deploy request.
     *
     * @param deployRequest deploy request.
     * @return new deploy task.
     */
    private DeployTask createNewDeployTask(DeployRequest deployRequest) {
        // Find the approved service template and fill Ocl.
        ServiceTemplateEntity searchServiceTemplate = new ServiceTemplateEntity();
        searchServiceTemplate.setName(StringUtils.lowerCase(deployRequest.getServiceName()));
        searchServiceTemplate.setVersion(StringUtils.lowerCase(deployRequest.getVersion()));
        searchServiceTemplate.setCsp(deployRequest.getCsp());
        searchServiceTemplate.setCategory(deployRequest.getCategory());
        searchServiceTemplate.setServiceHostingType(deployRequest.getServiceHostingType());
        ServiceTemplateEntity existingServiceTemplate =
                serviceTemplateStorage.findServiceTemplate(searchServiceTemplate);
        if (Objects.isNull(existingServiceTemplate)
                || Objects.isNull(existingServiceTemplate.getId())
                || Objects.isNull(existingServiceTemplate.getOcl())) {
            throw new ServiceTemplateNotRegistered("No available service templates found.");
        }
        if (ServiceRegistrationState.APPROVED
                != existingServiceTemplate.getServiceRegistrationState()) {
            String errMsg = String.format("Found service template with id %s but not approved.",
                    existingServiceTemplate.getId());
            throw new ServiceTemplateNotApproved(errMsg);
        }
        if (StringUtils.isNotBlank(existingServiceTemplate.getOcl().getEula())
                && !deployRequest.isEulaAccepted()) {
            log.error("Service not accepted Eula.");
            throw new EulaNotAccepted("Service not accepted Eula.");
        }
        if (!existingServiceTemplate.getOcl().getBilling().getBillingModes()
                .contains(deployRequest.getBillingMode())) {
            String errorMsg = String.format(
                    "The service template with id %s does not support billing mode %s.",
                    existingServiceTemplate.getId(), deployRequest.getBillingMode());
            throw new BillingModeNotSupported(errorMsg);
        }
        // Check context validation
        validateDeployRequestWithServiceTemplate(existingServiceTemplate, deployRequest);
        sensitiveDataHandler.encodeDeployVariable(existingServiceTemplate,
                deployRequest.getServiceRequestProperties());

        AvailabilityZonesRequestValidator.validateAvailabilityZones(
                deployRequest.getAvailabilityZones(),
                existingServiceTemplate.getOcl().getDeployment().getServiceAvailabilityConfig());
        if (StringUtils.isEmpty(deployRequest.getCustomerServiceName())) {
            deployRequest.setCustomerServiceName(generateCustomerServiceName(deployRequest));
        }
        DeployTask deployTask = new DeployTask();
        deployTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        deployTask.setServiceId(deployRequest.getServiceId());
        deployTask.setTaskType(ServiceOrderType.DEPLOY);
        deployTask.setUserId(deployRequest.getUserId());
        deployTask.setDeployRequest(deployRequest);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        deployTask.setNamespace(existingServiceTemplate.getNamespace());
        deployTask.setOcl(existingServiceTemplate.getOcl());
        deployTask.setServiceTemplateId(existingServiceTemplate.getId());
        return deployTask;
    }

    private void validateDeployRequestWithServiceTemplate(
            ServiceTemplateEntity existingServiceTemplate, DeployRequest deployRequest) {
        // Check context validation
        if (Objects.nonNull(existingServiceTemplate.getOcl().getDeployment()) && Objects.nonNull(
                deployRequest.getServiceRequestProperties())) {
            List<DeployVariable> deployVariables =
                    existingServiceTemplate.getOcl().getDeployment().getVariables();

            serviceDeployVariablesJsonSchemaValidator.validateDeployVariables(deployVariables,
                    deployRequest.getServiceRequestProperties(),
                    existingServiceTemplate.getJsonObjectSchema());
        }
        getServiceFlavorWithName(deployRequest.getFlavor(),
                existingServiceTemplate.getOcl().getFlavors());
    }

    private ServiceFlavorWithPrice getServiceFlavorWithName(String flavorName,
                                                            FlavorsWithPrice flavors) {
        Optional<ServiceFlavorWithPrice> flavorOptional = flavors.getServiceFlavors().stream()
                .filter(flavor -> flavor.getName().equals(flavorName)).findAny();
        if (flavorOptional.isEmpty()) {
            throw new FlavorInvalidException(
                    String.format("Could not find service flavor with name %s", flavorName));
        }
        return flavorOptional.get();
    }

    private String generateCustomerServiceName(DeployRequest deployRequest) {
        if (deployRequest.getServiceName().length() > 5) {
            return deployRequest.getServiceName().substring(0, 4) + "-"
                    + RandomStringUtils.randomAlphanumeric(5);
        } else {
            return deployRequest.getServiceName() + "-" + RandomStringUtils.randomAlphanumeric(5);
        }
    }

    private DeployServiceEntity storeNewDeployServiceEntity(DeployTask deployTask) {
        DeployServiceEntity entity = new DeployServiceEntity();
        entity.setId(deployTask.getServiceId());
        entity.setCreateTime(OffsetDateTime.now());
        entity.setVersion(StringUtils.lowerCase(deployTask.getDeployRequest().getVersion()));
        entity.setName(StringUtils.lowerCase(deployTask.getDeployRequest().getServiceName()));
        entity.setCsp(deployTask.getDeployRequest().getCsp());
        entity.setCategory(deployTask.getDeployRequest().getCategory());
        entity.setCustomerServiceName(deployTask.getDeployRequest().getCustomerServiceName());
        entity.setFlavor(deployTask.getDeployRequest().getFlavor());
        entity.setUserId(deployTask.getUserId());
        entity.setDeployRequest(deployTask.getDeployRequest());
        entity.setDeployResourceList(new ArrayList<>());
        entity.setNamespace(deployTask.getNamespace());
        entity.setServiceDeploymentState(ServiceDeploymentState.DEPLOYING);
        if (Objects.nonNull(deployTask.getServiceTemplateId())) {
            entity.setServiceTemplateId(deployTask.getServiceTemplateId());
        } else {
            throw new ServiceTemplateNotRegistered("service template id can't be null.");
        }
        ServiceLockConfig defaultLockConfig = new ServiceLockConfig();
        defaultLockConfig.setDestroyLocked(false);
        defaultLockConfig.setModifyLocked(false);
        entity.setLockConfig(defaultLockConfig);
        DeployServiceEntity storedEntity = deployServiceEntityHandler.storeAndFlush(entity);
        if (Objects.isNull(storedEntity)) {
            log.error("Store new deploy service entity with id {} failed.",
                    deployTask.getServiceId());
            throw new RuntimeException("Store new deploy service entity failed.");
        }
        return storedEntity;
    }


    private void deployService(DeployTask deployTask) {
        DeployResult deployResult;
        RuntimeException exception = null;
        Deployer deployer = deployerKindManager.getDeployment(
                deployTask.getOcl().getDeployment().getDeployerTool().getKind());
        ServiceOrderEntity orderTaskEntity =
                serviceOrderManager.createServiceOrderTask(deployTask, null);
        DeployServiceEntity serviceEntity = storeNewDeployServiceEntity(deployTask);
        try {
            policyValidator.validateDeploymentWithPolicies(deployTask);
            serviceOrderManager.startOrderProgress(deployTask.getOrderId());
            deployResult = deployer.deploy(deployTask);
        } catch (RuntimeException e) {
            exception = e;
            deployResult = getFailedDeployResult(deployTask, exception);
        }
        DeployServiceEntity updatedServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(deployResult,
                        serviceEntity);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedServiceEntity.getServiceDeploymentState()) {
            rollbackOnDeploymentFailure(deployTask, updatedServiceEntity);
        }
        deployResultManager.updateServiceOrderTaskWithDeployResult(deployResult, orderTaskEntity);
        if (Objects.nonNull(exception)) {
            throw exception;
        }
    }

    /**
     * Redeploy service with failed state.
     *
     * @param redeployTask        redeployTask
     * @param deployServiceEntity deployServiceEntity
     */
    private void redeployService(DeployTask redeployTask,
                                 DeployServiceEntity deployServiceEntity) {
        DeployResult redeployResult;
        RuntimeException exception = null;
        Deployer deployer = deployerKindManager.getDeployment(
                redeployTask.getOcl().getDeployment().getDeployerTool().getKind());
        ServiceOrderEntity orderTaskEntity =
                serviceOrderManager.createServiceOrderTask(redeployTask, deployServiceEntity);
        try {
            policyValidator.validateDeploymentWithPolicies(redeployTask);
            deployServiceEntity =
                    deployServiceEntityHandler.updateServiceDeploymentStatus(deployServiceEntity,
                            ServiceDeploymentState.DEPLOYING);
            serviceOrderManager.startOrderProgress(redeployTask.getOrderId());
            redeployResult = deployer.deploy(redeployTask);
        } catch (RuntimeException e) {
            exception = e;
            redeployResult = getFailedDeployResult(redeployTask, exception);
        }
        DeployServiceEntity updatedServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(redeployResult,
                        deployServiceEntity);
        if (ServiceDeploymentState.DEPLOY_FAILED
                == updatedServiceEntity.getServiceDeploymentState()) {
            rollbackOnDeploymentFailure(redeployTask, updatedServiceEntity);
        }
        deployResultManager.updateServiceOrderTaskWithDeployResult(redeployResult, orderTaskEntity);
        if (Objects.nonNull(exception)) {
            throw exception;
        }
    }


    /**
     * Perform rollback when deployment fails and destroy the created resources.
     */
    public void rollbackOnDeploymentFailure(DeployTask deployTask,
                                            DeployServiceEntity deployServiceEntity) {
        log.info("Performing rollback of already provisioned resources.");
        if (CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
            log.info("No resources need to destroy, the rollback task success.");
            DeployResult rollbackResult = new DeployResult();
            rollbackResult.setOrderId(deployTask.getOrderId());
            rollbackResult.setServiceId(deployTask.getServiceId());
            rollbackResult.setIsTaskSuccessful(true);
            rollbackResult.setState(DeployerTaskStatus.ROLLBACK_SUCCESS);
            deployResultManager.updateServiceOrderTaskWithDeployResult(rollbackResult, null);
            return;
        }
        log.info("Rollback to destroy created resources of the service {}",
                deployTask.getServiceId());
        DeployResult rollbackResult;
        RuntimeException exception = null;
        deployTask.setDeploymentScenario(DeploymentScenario.ROLLBACK);
        Deployer deployer = deployerKindManager.getDeployment(
                deployTask.getOcl().getDeployment().getDeployerTool().getKind());
        try {
            rollbackResult = deployer.destroy(deployTask);
        } catch (RuntimeException e) {
            exception = e;
            rollbackResult = getFailedDeployResult(deployTask, exception);
        }
        deployResultManager.updateDeployServiceEntityWithDeployResult(rollbackResult,
                deployServiceEntity);
        deployResultManager.updateServiceOrderTaskWithDeployResult(rollbackResult, null);
        if (Objects.nonNull(exception)) {
            throw exception;
        }
    }

    /**
     * Method to change lock config of service.
     *
     * @param serviceId deployed service id.
     * @param config    serviceLockConfig.
     */
    public void changeServiceLockConfig(UUID serviceId, ServiceLockConfig config) {
        DeployServiceEntity deployServiceEntity =
                this.deployServiceEntityHandler.getDeployServiceEntity(serviceId);
        boolean currentUserIsOwner =
                this.userServiceHelper.currentUserIsOwner(deployServiceEntity.getUserId());
        if (!currentUserIsOwner) {
            throw new AccessDeniedException("No permissions to change lock config of services "
                    + "belonging to other users.");
        }
        deployServiceEntity.setLockConfig(config);
        deployServiceStorage.storeAndFlush(deployServiceEntity);
    }

    /**
     * Get modify task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     */
    private DeployTask getModifyTask(ModifyRequest modifyRequest,
                                     DeployServiceEntity deployServiceEntity) {
        if (Objects.nonNull(deployServiceEntity.getLockConfig())
                && deployServiceEntity.getLockConfig().isModifyLocked()) {
            String errorMsg =
                    "Service " + deployServiceEntity.getId() + " is locked from modification.";
            throw new ServiceLockedException(errorMsg);
        }

        if (StringUtils.isBlank(modifyRequest.getFlavor()) && Objects.isNull(
                modifyRequest.getServiceRequestProperties())) {
            throw new ServiceModifyParamsNotFoundException("No params found for modify services.");
        }

        if (!deployServiceEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.DEPLOY_SUCCESS)
                && !deployServiceEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_FAILED)
                && !deployServiceEntity.getServiceDeploymentState()
                .equals(ServiceDeploymentState.MODIFICATION_SUCCESSFUL)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to modify.",
                            deployServiceEntity.getId(),
                            deployServiceEntity.getServiceDeploymentState()));
        }
        ServiceTemplateEntity existingServiceTemplate =
                serviceTemplateStorage.getServiceTemplateById(
                        deployServiceEntity.getServiceTemplateId());
        DeployRequest previousDeployRequest = deployServiceEntity.getDeployRequest();
        DeployRequest newDeployRequest = new DeployRequest();
        BeanUtils.copyProperties(previousDeployRequest, newDeployRequest);
        if (StringUtils.isNotEmpty(modifyRequest.getCustomerServiceName())) {
            newDeployRequest.setCustomerServiceName(modifyRequest.getCustomerServiceName());
        }
        if (StringUtils.isNotBlank(modifyRequest.getFlavor())) {
            validateFlavorDowngradedIsAllowed(deployServiceEntity.getFlavor(),
                    modifyRequest.getFlavor(), existingServiceTemplate.getOcl().getFlavors());
            newDeployRequest.setFlavor(modifyRequest.getFlavor());
        }
        if (Objects.nonNull(modifyRequest.getServiceRequestProperties())
                && !modifyRequest.getServiceRequestProperties().isEmpty()) {
            newDeployRequest.setServiceRequestProperties(
                    modifyRequest.getServiceRequestProperties());
        }
        validateDeployRequestWithServiceTemplate(existingServiceTemplate, newDeployRequest);
        DeployTask modifyTask = new DeployTask();
        modifyTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        modifyTask.setTaskType(ServiceOrderType.MODIFY);
        modifyTask.setServiceId(deployServiceEntity.getId());
        modifyTask.setUserId(modifyRequest.getUserId());
        modifyTask.setServiceTemplateId(deployServiceEntity.getServiceTemplateId());
        modifyTask.setNamespace(deployServiceEntity.getNamespace());
        modifyTask.setDeployRequest(newDeployRequest);
        modifyTask.setOcl(existingServiceTemplate.getOcl());
        modifyTask.setDeploymentScenario(DeploymentScenario.MODIFY);
        return modifyTask;
    }

    private void validateFlavorDowngradedIsAllowed(String originalFlavor, String newFlavor,
                                                   FlavorsWithPrice flavors) {
        if (!flavors.isDowngradeAllowed()) {
            ServiceFlavor newServiceFlavor = getServiceFlavorWithName(newFlavor, flavors);
            ServiceFlavor originalServiceFlavor = getServiceFlavorWithName(originalFlavor, flavors);
            if (newServiceFlavor.getPriority() > originalServiceFlavor.getPriority()) {
                String errorMsg = String.format("Downgrading of flavors is not allowed. New flavor"
                                + " priority %d is lower than the original flavor priority %d.",
                        newServiceFlavor.getPriority(), originalServiceFlavor.getPriority());
                throw new ServiceFlavorDowngradeNotAllowed(errorMsg);
            }
        }
    }

    /**
     * Async method to modify service.
     *
     * @param modifyTask          modifyTask.
     * @param deployServiceEntity deployServiceEntity
     */
    public void modifyService(DeployTask modifyTask, DeployServiceEntity deployServiceEntity) {
        RuntimeException exception = null;
        DeployResult modifyResult;
        MDC.put(SERVICE_ID, modifyTask.getServiceId().toString());
        Deployer deployer = deployerKindManager.getDeployment(
                modifyTask.getOcl().getDeployment().getDeployerTool().getKind());
        ServiceOrderEntity orderTaskEntity =
                serviceOrderManager.createServiceOrderTask(modifyTask, deployServiceEntity);
        try {
            deployServiceEntity.setDeployRequest(modifyTask.getDeployRequest());
            deployServiceEntity =
                    deployServiceEntityHandler.updateServiceDeploymentStatus(deployServiceEntity,
                            ServiceDeploymentState.MODIFYING);
            serviceOrderManager.startOrderProgress(modifyTask.getOrderId());
            modifyResult = deployer.modify(modifyTask);
        } catch (RuntimeException e) {
            exception = e;
            modifyResult = getFailedDeployResult(modifyTask, e);
        }
        deployResultManager.updateDeployServiceEntityWithDeployResult(modifyResult,
                deployServiceEntity);
        deployResultManager.updateServiceOrderTaskWithDeployResult(modifyResult, orderTaskEntity);
        if (Objects.nonNull(exception)) {
            throw exception;
        }
    }

    /**
     * Async method to destroy service.
     *
     * @param destroyTask         destroyTask.
     * @param deployServiceEntity deployServiceEntity
     */
    public void destroyService(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        destroyTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        destroy(destroyTask, deployServiceEntity);
    }

    private void destroy(DeployTask destroyTask, DeployServiceEntity deployServiceEntity) {
        DeployResult destroyResult;
        RuntimeException exception = null;
        MDC.put(SERVICE_ID, destroyTask.getServiceId().toString());
        ServiceOrderEntity orderTaskEntity =
                serviceOrderManager.createServiceOrderTask(destroyTask, deployServiceEntity);
        Deployer deployer = deployerKindManager.getDeployment(
                destroyTask.getOcl().getDeployment().getDeployerTool().getKind());
        try {
            if (DeploymentScenario.ROLLBACK != destroyTask.getDeploymentScenario()) {
                deployServiceEntityHandler.updateServiceDeploymentStatus(deployServiceEntity,
                        ServiceDeploymentState.DESTROYING);
            }
            serviceOrderManager.startOrderProgress(destroyTask.getOrderId());
            destroyResult = deployer.destroy(destroyTask);
        } catch (RuntimeException e) {
            exception = e;
            destroyResult = getFailedDeployResult(destroyTask, e);
        }
        DeployServiceEntity updatedServiceEntity =
                deployResultManager.updateDeployServiceEntityWithDeployResult(destroyResult,
                        deployServiceEntity);
        if (ServiceDeploymentState.DESTROY_SUCCESS
                == updatedServiceEntity.getServiceDeploymentState()) {
            deployer.deleteTaskWorkspace(destroyTask.getServiceId());
        }
        deployResultManager.updateServiceOrderTaskWithDeployResult(destroyResult, orderTaskEntity);
        if (Objects.nonNull(exception)) {
            throw exception;
        }
    }

    /**
     * purge the service based on the serviceDeploymentState.
     *
     * @param purgeTask           purgeTask.
     * @param deployServiceEntity deployServiceEntity
     */
    public void purgeService(DeployTask purgeTask, DeployServiceEntity deployServiceEntity) {
        RuntimeException exception = null;
        MDC.put(SERVICE_ID, purgeTask.getServiceId().toString());
        DeployResult purgeResult;
        ServiceOrderEntity orderTaskEntity =
                serviceOrderManager.createServiceOrderTask(purgeTask, deployServiceEntity);
        try {
            if (!CollectionUtils.isEmpty(deployServiceEntity.getDeployResourceList())) {
                log.info("Resources of service {} need to clear with order task {}",
                        purgeTask.getServiceId(), purgeTask.getOrderId());
                purgeTask.setDeploymentScenario(DeploymentScenario.PURGE);
                Deployer deployer = deployerKindManager.getDeployment(
                        purgeTask.getOcl().getDeployment().getDeployerTool().getKind());
                deployServiceEntityHandler.updateServiceDeploymentStatus(deployServiceEntity,
                        ServiceDeploymentState.DESTROYING);
                serviceOrderManager.startOrderProgress(purgeTask.getOrderId());
                purgeResult = deployer.destroy(purgeTask);
            } else {
                deployServiceStorage.deleteDeployService(deployServiceEntity);
                serviceStateManager.deleteManagementTasksByServiceId(purgeTask.getServiceId());
                purgeResult = new DeployResult();
                purgeResult.setOrderId(purgeTask.getOrderId());
                purgeResult.setServiceId(purgeTask.getServiceId());
                purgeResult.setIsTaskSuccessful(true);
            }
        } catch (RuntimeException e) {
            exception = e;
            purgeResult = getFailedDeployResult(purgeTask, e);
        }
        deployResultManager.updateServiceOrderTaskWithDeployResult(purgeResult, orderTaskEntity);
        if (Objects.nonNull(exception)) {
            deployResultManager.updateDeployServiceEntityWithDeployResult(purgeResult,
                    deployServiceEntity);
            throw exception;
        }
    }

    /**
     * Deployment service.
     *
     * @param newId         new service id.
     * @param userId        user id.
     * @param deployRequest deploy request.
     */
    public void deployServiceById(UUID newId, String userId, DeployRequest deployRequest) {
        log.info("Migrate workflow start deploy new service instance with id: {}", newId);
        MDC.put(SERVICE_ID, newId.toString());
        deployRequest.setServiceId(newId);
        deployRequest.setUserId(userId);
        DeployTask deployTask = createNewDeployTask(deployRequest);
        deployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        // override task id and user id.
        deployTask.setServiceId(newId);
        deployTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        deployTask.setTaskType(ServiceOrderType.DEPLOY);
        deployService(deployTask);
    }

    /**
     * Destroy service by deployed service id.
     */
    public void destroyServiceById(String id) {
        MDC.put(SERVICE_ID, id);
        UUID serviceId = UUID.fromString(id);
        DeployServiceEntity deployServiceEntity =
                deployServiceEntityHandler.getDeployServiceEntity(serviceId);
        DeployTask destroyTask =
                deployServiceEntityConverter.getDeployTaskByStoredService(deployServiceEntity);
        destroyTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        destroyTask.setTaskType(ServiceOrderType.DESTROY);
        destroyTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        destroy(destroyTask, deployServiceEntity);
    }

    /**
     * Get destroy task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     */
    private DeployTask getDestroyTask(DeployServiceEntity deployServiceEntity) {
        if (Objects.nonNull(deployServiceEntity.getLockConfig())
                && deployServiceEntity.getLockConfig().isDestroyLocked()) {
            String errorMsg =
                    "Service " + deployServiceEntity.getId() + " is locked from deletion.";
            throw new ServiceLockedException(errorMsg);
        }
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (state.equals(ServiceDeploymentState.DEPLOYING) || state.equals(
                ServiceDeploymentState.DESTROYING) || state.equals(
                ServiceDeploymentState.MODIFYING)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to destroy.",
                            deployServiceEntity.getId(), state));
        }
        DeployTask destroyTask =
                deployServiceEntityConverter.getDeployTaskByStoredService(deployServiceEntity);
        destroyTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        destroyTask.setTaskType(ServiceOrderType.DESTROY);
        destroyTask.setDeploymentScenario(DeploymentScenario.DESTROY);
        return destroyTask;
    }

    /**
     * Get purge task by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    private DeployTask getPurgeTask(DeployServiceEntity deployServiceEntity) {
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_SUCCESS
                || state == ServiceDeploymentState.DESTROY_FAILED
                || state == ServiceDeploymentState.ROLLBACK_FAILED
                || state == ServiceDeploymentState.MANUAL_CLEANUP_REQUIRED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to purge.",
                            deployServiceEntity.getId(), state));
        }
        DeployTask purgeTask =
                deployServiceEntityConverter.getDeployTaskByStoredService(deployServiceEntity);
        purgeTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        purgeTask.setTaskType(ServiceOrderType.PURGE);
        purgeTask.setDeploymentScenario(DeploymentScenario.PURGE);
        return purgeTask;
    }


    /**
     * Get deploy task to redeploy the failed service by stored deploy service entity.
     *
     * @param deployServiceEntity deploy service entity.
     * @return deploy task.
     */
    public DeployTask getRedeployTask(DeployServiceEntity deployServiceEntity) {
        MDC.put(SERVICE_ID, deployServiceEntity.getId().toString());
        // Get state of service.
        ServiceDeploymentState state = deployServiceEntity.getServiceDeploymentState();
        if (!(state == ServiceDeploymentState.DEPLOY_FAILED
                || state == ServiceDeploymentState.DESTROY_FAILED
                || state == ServiceDeploymentState.ROLLBACK_FAILED)) {
            throw new InvalidServiceStateException(
                    String.format("Service %s with the state %s is not allowed to redeploy.",
                            deployServiceEntity.getId(), state));
        }
        DeployTask redeployTask =
                deployServiceEntityConverter.getDeployTaskByStoredService(deployServiceEntity);
        redeployTask.setOrderId(CustomRequestIdGenerator.generateOrderId());
        redeployTask.setTaskType(ServiceOrderType.REDEPLOY);
        redeployTask.setDeploymentScenario(DeploymentScenario.DEPLOY);
        return redeployTask;
    }


    /**
     * Get latest service deployment status.
     *
     * @param serviceId                service id.
     * @param lastKnownDeploymentState last known service deployment state.
     * @return DeferredResult.
     */
    public DeferredResult<DeploymentStatusUpdate> getLatestServiceDeploymentStatus(
            UUID serviceId, ServiceDeploymentState lastKnownDeploymentState) {
        DeferredResult<DeploymentStatusUpdate> stateDeferredResult = new DeferredResult<>();
        taskExecutor.execute(() -> {
            try {
                this.serviceDeploymentStatusChangePolling.fetchServiceDeploymentStatusWithPolling(
                        stateDeferredResult, serviceId, lastKnownDeploymentState);
            } catch (RuntimeException exception) {
                stateDeferredResult.setErrorResult(exception);
            }
        });
        return stateDeferredResult;
    }


    private DeployResult getFailedDeployResult(DeployTask task, Exception ex) {
        String errorMsg =
                String.format("Order task %s to %s the service %s failed. " + "Error message:\n %s",
                        task.getOrderId(), task.getTaskType().toValue(), task.getServiceId(),
                        ex.getMessage());
        DeployResult deployResult = new DeployResult();
        deployResult.setOrderId(task.getOrderId());
        deployResult.setServiceId(task.getServiceId());
        deployResult.setIsTaskSuccessful(false);
        deployResult.setMessage(errorMsg);
        deployResult.setState(getDeployerTaskFailedState(task.getDeploymentScenario()));
        return deployResult;
    }

    private DeployerTaskStatus getDeployerTaskFailedState(DeploymentScenario deploymentScenario) {
        return switch (deploymentScenario) {
            case DEPLOY -> DeployerTaskStatus.DEPLOY_FAILED;
            case DESTROY -> DeployerTaskStatus.DESTROY_FAILED;
            case ROLLBACK -> DeployerTaskStatus.ROLLBACK_FAILED;
            case PURGE -> DeployerTaskStatus.PURGE_FAILED;
            case MODIFY -> DeployerTaskStatus.MODIFICATION_FAILED;
        };
    }
}
