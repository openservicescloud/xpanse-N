/*
 * Terraform-Boot API
 * RESTful Services to interact with Terraform-Boot runtime
 *
 * The version of the OpenAPI document: 1.0.12-SNAPSHOT
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * TerraformDestroyFromGitRepoRequest
 */
@JsonPropertyOrder({
        TerraformDestroyFromGitRepoRequest.JSON_PROPERTY_REQUEST_ID,
        TerraformDestroyFromGitRepoRequest.JSON_PROPERTY_TERRAFORM_VERSION,
        TerraformDestroyFromGitRepoRequest.JSON_PROPERTY_VARIABLES,
        TerraformDestroyFromGitRepoRequest.JSON_PROPERTY_ENV_VARIABLES,
        TerraformDestroyFromGitRepoRequest.JSON_PROPERTY_GIT_REPO_DETAILS,
        TerraformDestroyFromGitRepoRequest.JSON_PROPERTY_TF_STATE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.9.0")
public class TerraformDestroyFromGitRepoRequest {
    public static final String JSON_PROPERTY_REQUEST_ID = "requestId";
    public static final String JSON_PROPERTY_TERRAFORM_VERSION = "terraformVersion";
    public static final String JSON_PROPERTY_VARIABLES = "variables";
    public static final String JSON_PROPERTY_ENV_VARIABLES = "envVariables";
    public static final String JSON_PROPERTY_GIT_REPO_DETAILS = "gitRepoDetails";
    public static final String JSON_PROPERTY_TF_STATE = "tfState";
    private UUID requestId;
    private String terraformVersion;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, String> envVariables = new HashMap<>();
    private TerraformScriptGitRepoDetails gitRepoDetails;
    private String tfState;

    public TerraformDestroyFromGitRepoRequest() {
    }

    public TerraformDestroyFromGitRepoRequest requestId(UUID requestId) {

        this.requestId = requestId;
        return this;
    }

    /**
     * Id of the request
     *
     * @return requestId
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_REQUEST_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getRequestId() {
        return requestId;
    }


    @JsonProperty(JSON_PROPERTY_REQUEST_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public TerraformDestroyFromGitRepoRequest terraformVersion(String terraformVersion) {

        this.terraformVersion = terraformVersion;
        return this;
    }

    /**
     * The required version of the terraform which will execute the scripts.
     *
     * @return terraformVersion
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_TERRAFORM_VERSION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getTerraformVersion() {
        return terraformVersion;
    }


    @JsonProperty(JSON_PROPERTY_TERRAFORM_VERSION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTerraformVersion(String terraformVersion) {
        this.terraformVersion = terraformVersion;
    }

    public TerraformDestroyFromGitRepoRequest variables(Map<String, Object> variables) {

        this.variables = variables;
        return this;
    }

    public TerraformDestroyFromGitRepoRequest putVariablesItem(String key, Object variablesItem) {
        this.variables.put(key, variablesItem);
        return this;
    }

    /**
     * Key-value pairs of regular variables that must be used to execute the Terraform request.
     *
     * @return variables
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Map<String, Object> getVariables() {
        return variables;
    }


    @JsonProperty(JSON_PROPERTY_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public TerraformDestroyFromGitRepoRequest envVariables(Map<String, String> envVariables) {

        this.envVariables = envVariables;
        return this;
    }

    public TerraformDestroyFromGitRepoRequest putEnvVariablesItem(String key,
                                                                  String envVariablesItem) {
        if (this.envVariables == null) {
            this.envVariables = new HashMap<>();
        }
        this.envVariables.put(key, envVariablesItem);
        return this;
    }

    /**
     * Key-value pairs of variables that must be injected as environment variables to terraform process.
     *
     * @return envVariables
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENV_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getEnvVariables() {
        return envVariables;
    }


    @JsonProperty(JSON_PROPERTY_ENV_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnvVariables(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    public TerraformDestroyFromGitRepoRequest gitRepoDetails(
            TerraformScriptGitRepoDetails gitRepoDetails) {

        this.gitRepoDetails = gitRepoDetails;
        return this;
    }

    /**
     * Get gitRepoDetails
     *
     * @return gitRepoDetails
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_GIT_REPO_DETAILS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TerraformScriptGitRepoDetails getGitRepoDetails() {
        return gitRepoDetails;
    }


    @JsonProperty(JSON_PROPERTY_GIT_REPO_DETAILS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGitRepoDetails(TerraformScriptGitRepoDetails gitRepoDetails) {
        this.gitRepoDetails = gitRepoDetails;
    }

    public TerraformDestroyFromGitRepoRequest tfState(String tfState) {

        this.tfState = tfState;
        return this;
    }

    /**
     * The .tfState file content after deployment
     *
     * @return tfState
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_TF_STATE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getTfState() {
        return tfState;
    }


    @JsonProperty(JSON_PROPERTY_TF_STATE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTfState(String tfState) {
        this.tfState = tfState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraformDestroyFromGitRepoRequest terraformDestroyFromGitRepoRequest =
                (TerraformDestroyFromGitRepoRequest) o;
        return Objects.equals(this.requestId, terraformDestroyFromGitRepoRequest.requestId) &&
                Objects.equals(this.terraformVersion,
                        terraformDestroyFromGitRepoRequest.terraformVersion) &&
                Objects.equals(this.variables, terraformDestroyFromGitRepoRequest.variables) &&
                Objects.equals(this.envVariables, terraformDestroyFromGitRepoRequest.envVariables)
                &&
                Objects.equals(this.gitRepoDetails,
                        terraformDestroyFromGitRepoRequest.gitRepoDetails) &&
                Objects.equals(this.tfState, terraformDestroyFromGitRepoRequest.tfState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, terraformVersion, variables, envVariables, gitRepoDetails,
                tfState);
    }

    @Override
    public String toString() {
        String sb = "class TerraformDestroyFromGitRepoRequest {\n"
                + "    requestId: " + toIndentedString(requestId) + "\n"
                + "    terraformVersion: " + toIndentedString(terraformVersion) + "\n"
                + "    variables: " + toIndentedString(variables) + "\n"
                + "    envVariables: " + toIndentedString(envVariables) + "\n"
                + "    gitRepoDetails: " + toIndentedString(gitRepoDetails) + "\n"
                + "    tfState: " + toIndentedString(tfState) + "\n"
                + "}";
        return sb;
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

