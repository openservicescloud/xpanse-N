/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.workflow.handle.migrate;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;
import org.eclipse.xpanse.modules.workflow.consts.MigrateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Migration process destroy service processing class.
 */
@Slf4j
@Component
public class MigrateDestroyProcess implements Serializable, JavaDelegate {

    private static RuntimeService runtimeService;
    private static DeployService deployService;

    @Autowired
    public void setRuntimeService(RuntimeService runtimeService) {
        MigrateDestroyProcess.runtimeService = runtimeService;
    }

    @Autowired
    public void setDeployService(DeployService deployService) {
        MigrateDestroyProcess.deployService = deployService;
    }

    /**
     * Methods when performing destroy tasks.
     */
    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        String id = variables.get(MigrateConstants.ID).toString();
        int destroyRetryNum = (int) variables.get(MigrateConstants.DESTROY_RETRY_NUM);
        if (destroyRetryNum > 0) {
            log.info("Process instance: {} retry destroy service : {},RetryNum:{}",
                    processInstanceId, id, destroyRetryNum);
        }
        runtimeService.setVariable(processInstanceId, MigrateConstants.DESTROY_RETRY_NUM,
                destroyRetryNum + 1);
        log.info("Migration workflow start destroying old service with service id:{}", id);
        CompletableFuture<DeployServiceEntity> future = deployService.destroyService(id);
        DeployServiceEntity result = null;
        try {
            result = future.get();
        } catch (RuntimeException | InterruptedException | ExecutionException e) {
            log.error("Migration workflow destroy old service with id:{} failed. error:{}",
                    id, e.getMessage());
        }
        boolean destroySuccess = Objects.nonNull(result)
                && ServiceDeploymentState.DEPLOY_SUCCESS
                == result.getServiceDeploymentState();
        runtimeService.setVariable(processInstanceId, MigrateConstants.IS_DESTROY_SUCCESS,
                destroySuccess);
        log.info("Migration workflow completed destroying old service with id:{}, success:{}", id,
                destroySuccess);
    }
}
