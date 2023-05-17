/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.credential;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CreateCredential;
import org.eclipse.xpanse.modules.credential.CredentialCacheKey;
import org.eclipse.xpanse.modules.credential.CredentialCacheManager;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The credential center.
 */
@Component
public class CredentialCenter {

    private static final Integer DEFAULT_TIMEOUT_SECONDS = 3600;
    private static Long lastedClearTime;
    private final OrchestratorService orchestratorService;
    private final CredentialCacheManager credentialCacheManager;
    private final DeployServiceStorage deployServiceStorage;

    /**
     * Constructor of CredentialCenter.
     */
    @Autowired
    public CredentialCenter(
            OrchestratorService orchestratorService,
            CredentialCacheManager credentialCacheManager,
            DeployServiceStorage deployServiceStorage) {
        this.orchestratorService = orchestratorService;
        this.credentialCacheManager = credentialCacheManager;
        this.deployServiceStorage = deployServiceStorage;
    }

    /**
     * List the credential capabilities by @Csp and @type.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns list of credential capabilities.
     */
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByCsp(Csp csp,
                                                                       CredentialType type) {
        OrchestratorPlugin plugin = orchestratorService.getOrchestratorPlugin(csp);
        if (Objects.isNull(type)) {
            return plugin.getCredentialDefinitions();
        }
        return plugin.getCredentialDefinitions().stream().filter(credential ->
                        Objects.equals(credential.getType(), type))
                .collect(Collectors.toList());
    }

    /**
     * List the credential capabilities by the @id and @type.
     *
     * @param id   The UUID of the deployed service.
     * @param type The type of credential.
     * @return Returns list of credential capabilities.
     */
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByServiceId(String id,
                                                                             CredentialType type) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return getCredentialCapabilitiesByCsp(deployServiceEntity.getCsp(), type);
    }

    /**
     * List the credential info of the @csp and @userName and @type.
     *
     * @param csp      The cloud service provider.
     * @param userName The name of user who provided the credential info.
     * @param type     The type of the credential.
     * @return the credentials.
     */
    public List<CredentialDefinition> getCredentialDefinitionsByCsp(Csp csp, String userName,
                                                                    CredentialType type) {
        CredentialCacheKey cacheKey = new CredentialCacheKey(csp, userName);
        if (Objects.isNull(type)) {
            return credentialCacheManager.getAllTypeCaches(cacheKey);
        }
        CredentialDefinition credentialDefinition =
                credentialCacheManager.getCachesByType(cacheKey, type);
        List<CredentialDefinition> result = new ArrayList<>();
        if (Objects.nonNull(credentialDefinition)) {
            result.add(credentialDefinition);
        }
        return result;
    }

    /**
     * List credentials of the service.
     *
     * @param id   The id of deployed service.
     * @param type The type of credential.
     * @return Returns credential capabilities list of the service.
     */
    public List<CredentialDefinition> getCredentialDefinitionsByServiceId(String id,
                                                                          CredentialType type) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return getCredentialDefinitionsByCsp(deployServiceEntity.getCsp(),
                deployServiceEntity.getUserName(), type);
    }


    /**
     * Add credential info for the csp.
     *
     * @param csp              The cloud service provider.
     * @param createCredential The credential for the service.
     * @return Deployment bean for the provided deployerKind.
     */
    public boolean addCredential(Csp csp, CreateCredential createCredential) {
        checkInputCredentialIsValid(csp, createCredential);
        createCredential.setCsp(csp);
        CredentialDefinition credential = createCredentialDefinitionObject(createCredential);
        return createCredential(credential.getCsp(), credential.getUserName(),
                credential);
    }

    /**
     * Add credential info for the service.
     *
     * @param id               The UUID of the deployed service.
     * @param createCredential The credential for the service.
     * @return true of false.
     */
    public boolean addCredentialByServiceId(String id, CreateCredential createCredential) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        CredentialDefinition credential =
                fillCredentialWithDbEntity(createCredential, deployServiceEntity);
        checkInputCredentialIsValid(deployServiceEntity.getCsp(), createCredential);
        return createCredential(credential.getCsp(), credential.getUserName(),
                credential);
    }


    /**
     * Update credential info for the service.
     *
     * @param csp              The cloud service provider.
     * @param updateCredential The credential for the service.
     * @return true of false.
     */
    public boolean updateCredential(Csp csp, CreateCredential updateCredential) {
        updateCredential.setCsp(csp);
        CredentialDefinition credential = createCredentialDefinitionObject(updateCredential);
        deleteCredentialByType(csp, credential.getUserName(),
                credential.getType());
        return createCredential(csp, credential.getUserName(),
                credential);
    }

    /**
     * Update credential info for the service.
     *
     * @param id               The UUID of the deployed service.
     * @param updateCredential The credential for the service.
     * @return true of false.
     */
    public boolean updateCredentialByServiceId(String id, CreateCredential updateCredential) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        CredentialDefinition credential =
                fillCredentialWithDbEntity(updateCredential, deployServiceEntity);
        deleteCredentialByType(credential.getCsp(), credential.getUserName(),
                credential.getType());
        return createCredential(credential.getCsp(), credential.getUserName(),
                credential);
    }


    /**
     * Delete credential for the @Csp with @userName.
     *
     * @param csp      The cloud service provider.
     * @param userName The userName to delete.
     * @return true of false.
     */
    public boolean deleteCredential(Csp csp, String userName) {
        CredentialCacheKey cacheKey = new CredentialCacheKey(csp, userName);
        credentialCacheManager.removeAllTypeCaches(cacheKey);
        return true;
    }

    /**
     * Delete credential info of the service.
     *
     * @param id   The UUID of the deployed service.
     * @param type The type of credential.
     * @return true of false.
     */
    public boolean deleteCredentialByServiceId(String id, CredentialType type) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        if (Objects.isNull(type)) {
            return deleteCredential(deployServiceEntity.getCsp(),
                    deployServiceEntity.getUserName());
        }
        return deleteCredentialByType(deployServiceEntity.getCsp(),
                deployServiceEntity.getUserName(), type);
    }

    /**
     * Delete credential for the @Csp with @userName.
     *
     * @param csp      The cloud service provider.
     * @param userName The userName to delete.
     */
    public boolean deleteCredentialByType(Csp csp, String userName, CredentialType
            credentialType) {
        CredentialCacheKey cacheKey = new CredentialCacheKey(csp, userName);
        credentialCacheManager.removeCacheByType(cacheKey, credentialType);
        return false;
    }

    /**
     * Get credential for the @Csp with @userName.
     *
     * @param csp      The cloud service provider.
     * @param userName The userName to get.
     */
    public CredentialDefinition getCredential(Csp csp, String userName) {
        CredentialCacheKey cacheKey = new CredentialCacheKey(csp, userName);
        List<CredentialDefinition> credentialDefinitionList =
                credentialCacheManager.getAllTypeCaches(cacheKey);
        if (!CollectionUtils.isEmpty(credentialDefinitionList)) {
            return credentialDefinitionList.get(0);
        }
        AbstractCredentialInfo credentialInfo = findCredentialInfoFromEnv(csp);
        if (Objects.isNull(credentialInfo)) {
            throw new IllegalStateException(
                    String.format("No credential information found for the given Csp:%s.", csp));
        }
        return (CredentialDefinition) credentialInfo;
    }

    /**
     * Check the input credential whether is valid.
     *
     * @param csp             The cloud service provider.
     * @param inputCredential The credential to create or update.
     */
    public void checkInputCredentialIsValid(Csp csp, CreateCredential inputCredential) {

        // filter defined credentials by csp and type.
        List<AbstractCredentialInfo> credentialDefinitions =
                getCredentialCapabilitiesByCsp(csp, inputCredential.getType());
        if (CollectionUtils.isEmpty(credentialDefinitions)) {
            throw new IllegalArgumentException(
                    String.format("Defined credentials with type %s provided by csp %s not found.",
                            inputCredential.getType(), csp));
        }
        Set<String> filledVariableNameSet =
                inputCredential.getVariables().stream().map(CredentialVariable::getName)
                        .collect(Collectors.toSet());
        // check all fields in the input credential are valid based on the defined credentials.
        for (AbstractCredentialInfo credentialDefinition : credentialDefinitions) {
            CredentialDefinition credential = (CredentialDefinition) credentialDefinition;
            Set<String> needVariableNameSet =
                    credential.getVariables().stream().map(CredentialVariable::getName)
                            .collect(Collectors.toSet());
            if (StringUtils.equals(credential.getName(), inputCredential.getName())
                    && filledVariableNameSet.containsAll(needVariableNameSet)) {
                Set<String> blankValueFiledNames = new HashSet<>();
                for (CredentialVariable credentialVariable : inputCredential.getVariables()) {
                    if (StringUtils.isBlank(credentialVariable.getValue())) {
                        blankValueFiledNames.add(credentialVariable.getName());
                    }
                }
                if (blankValueFiledNames.size() > 0) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Defined credentials required fields %s value is blank.",
                                    blankValueFiledNames));

                }
                return;
            }
        }
        throw new IllegalArgumentException(
                String.format("Defined credentials provided by Csp:%s with name:%s not found.",
                        csp, inputCredential.getName()));

    }

    /**
     * Create credential for the @Csp with @userName.
     *
     * @param csp        The cloud service provider.
     * @param userName   The userName to create for.
     * @param credential The credential to create.
     * @return true of false.
     */
    public boolean createCredential(Csp csp, String userName, CredentialDefinition credential) {
        CredentialCacheKey cacheKey = new CredentialCacheKey(csp, userName);
        credentialCacheManager.putCache(cacheKey, credential);
        clearExpiredCache();
        return true;
    }

    private CredentialDefinition fillCredentialWithDbEntity(CreateCredential createCredential,
                                                            DeployServiceEntity deployService) {
        createCredential.setCsp(deployService.getCsp());
        createCredential.setUserName(deployService.getUserName());
        return createCredentialDefinitionObject(createCredential);

    }

    private CredentialDefinition createCredentialDefinitionObject(
            CreateCredential createCredential) {
        CredentialDefinition credential = new CredentialDefinition(createCredential.getCsp(),
                createCredential.getName(), createCredential.getDescription(),
                createCredential.getType(), createCredential.getVariables());
        if (Objects.isNull(createCredential.getTimeToLive())) {
            createCredential.setTimeToLive(DEFAULT_TIMEOUT_SECONDS);
        }
        credential.setExpiredTime(
                System.currentTimeMillis() + createCredential.getTimeToLive() * 1000);
        return credential;
    }

    private DeployServiceEntity getDeployServiceEntityById(String id) {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(UUID.fromString(id));
        if (Objects.isNull(deployServiceEntity)) {
            throw new RuntimeException(String.format("Deployed service with id %s not found", id));
        }
        return deployServiceEntity;
    }

    private void clearExpiredCache() {
        if (Objects.isNull(lastedClearTime)
                || System.currentTimeMillis() - lastedClearTime > DEFAULT_TIMEOUT_SECONDS * 1000) {
            credentialCacheManager.removeJob();
            lastedClearTime = System.currentTimeMillis();
        }
    }

    /**
     * Get credentialInfo from the environment using @Csp.
     *
     * @param csp The cloud service provider.
     * @return Returns credentialInfo.
     */
    private AbstractCredentialInfo findCredentialInfoFromEnv(Csp csp) {
        List<AbstractCredentialInfo> credentialAbilities =
                getCredentialCapabilitiesByCsp(csp, null);
        if (CollectionUtils.isEmpty(credentialAbilities)) {
            return null;
        }
        for (AbstractCredentialInfo credentialAbility : credentialAbilities) {
            CredentialDefinition credentialDefinition = (CredentialDefinition) credentialAbility;
            List<CredentialVariable> variables = credentialDefinition.getVariables();
            int credentialVariableSetValueCount = 0;
            for (CredentialVariable variable : variables) {
                String envValue = System.getenv(variable.getName());
                if (StringUtils.isNotBlank(envValue)) {
                    variable.setValue(envValue);
                    credentialVariableSetValueCount++;
                }
            }
            // Check if all variables have been successfully set.
            if (credentialVariableSetValueCount == variables.size()) {
                return credentialAbility;
            }
        }
        return null;
    }

}
