/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.engine.terraform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.engine.terraform.exceptions.ExecutorException;
import org.eclipse.xpanse.modules.engine.utils.SystemCmd;

/**
 * Class to encapsulate all Terraform executions.
 */
@Slf4j
public class TfExecutor {

    private Map<String, String> env;
    private String workspace;

    public TfExecutor(Map<String, String> env, String workspace) {
        this.env = env;
        this.workspace = workspace;
    }

    /**
     * Executes terraform init command.
     *
     * @return true if initialization of terraform is successful. else false.
     */
    public boolean tfInit() {
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform init", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Executes terraform plan command.
     *
     * @return true if terraform plan creation is successful. else false.
     */
    public boolean tfPlan() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform plan", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Executes terraform apply command.
     *
     * @return true if changes are successfully applied. else false.
     */
    public boolean tfApply() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform apply -auto-approve", out);
        log.info(out.toString());
        return exeRet;
    }

    /**
     * Executes terraform destroy command.
     *
     * @return true if all resources are successfully destroyed on the target infrastructure. else
     * false.
     */
    public boolean tfDestroy() {
        // TODO: Dynamic variables need to be supported.
        StringBuilder out = new StringBuilder();
        boolean exeRet = execute("terraform destroy -auto-approve", out);
        log.info(out.toString());
        return exeRet;
    }

    private boolean execute(String cmd, StringBuilder stdOut) {
        log.info("Will executing cmd: " + String.join(" ", cmd));
        SystemCmd systemCmd = new SystemCmd();
        systemCmd.setEnv(env);
        systemCmd.setWorkDir(workspace);
        return systemCmd.execute(cmd, stdOut);
    }

    /**
     * deploy source by terraform.
     */
    public void deploy() {
        if (!tfInit()) {
            log.error("TfExecutor.tfInit failed.");
            throw new ExecutorException("TfExecutor.tfInit failed.");
        }
        if (!tfPlan()) {
            log.error("TfExecutor.tfPlan failed.");
            throw new ExecutorException("TfExecutor.tfPlan failed.");
        }
        if (!tfApply()) {
            log.error("TfExecutor.tfApply failed.");
            throw new ExecutorException("TfExecutor.tfApply failed.");
        }
    }

    /**
     * destroy resource.
     */
    public void destroy() {
        if (!tfInit()) {
            log.error("TfExecutor.tfInit failed.");
            throw new ExecutorException("TfExecutor.tfInit failed.");
        }
        if (!tfPlan()) {
            log.error("TfExecutor.tfPlan failed.");
            throw new ExecutorException("TfExecutor.tfPlan failed.");
        }
        if (!tfDestroy()) {
            log.error("TfExecutor.tfDestroy failed.");
            throw new ExecutorException("TfExecutor.tfDestroy failed.");
        }
    }

    /**
     * Reads the contents of the "terraform.tfstate" file from the terraform workspace
     *
     * @return file contents as string.
     */
    public String getTerraformState() {
        File tfState = new File(workspace + File.separator + "terraform.tfstate");
        if (!tfState.exists()) {
            log.info("Terraform state file not exists.");
            return null;
        }
        try {
            return Files.readString(tfState.toPath());
        } catch (IOException ex) {
            throw new ExecutorException("Read state file failed.", ex);
        }
    }
}
