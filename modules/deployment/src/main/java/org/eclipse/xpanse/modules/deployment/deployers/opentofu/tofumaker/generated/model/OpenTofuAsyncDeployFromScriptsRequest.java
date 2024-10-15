/*
 * Tofu-Maker API
 * RESTful Services to interact with Tofu-Maker runtime
 *
 * The version of the OpenAPI document: 1.0.7-SNAPSHOT
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * OpenTofuAsyncDeployFromScriptsRequest
 */
@JsonPropertyOrder({
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_REQUEST_ID,
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_OPEN_TOFU_VERSION,
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_IS_PLAN_ONLY,
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_VARIABLES,
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_ENV_VARIABLES,
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_SCRIPTS,
        OpenTofuAsyncDeployFromScriptsRequest.JSON_PROPERTY_WEBHOOK_CONFIG
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.9.0")
public class OpenTofuAsyncDeployFromScriptsRequest {
    public static final String JSON_PROPERTY_REQUEST_ID = "requestId";
    public static final String JSON_PROPERTY_OPEN_TOFU_VERSION = "openTofuVersion";
    public static final String JSON_PROPERTY_IS_PLAN_ONLY = "isPlanOnly";
    public static final String JSON_PROPERTY_VARIABLES = "variables";
    public static final String JSON_PROPERTY_ENV_VARIABLES = "envVariables";
    public static final String JSON_PROPERTY_SCRIPTS = "scripts";
    public static final String JSON_PROPERTY_WEBHOOK_CONFIG = "webhookConfig";
    private UUID requestId;
    private String openTofuVersion;
    private Boolean isPlanOnly;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, String> envVariables = new HashMap<>();
    private List<String> scripts = new ArrayList<>();
    private WebhookConfig webhookConfig;

    public OpenTofuAsyncDeployFromScriptsRequest() {
    }

    public OpenTofuAsyncDeployFromScriptsRequest requestId(UUID requestId) {

        this.requestId = requestId;
        return this;
    }

    /**
     * Id of the request.
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

    public OpenTofuAsyncDeployFromScriptsRequest openTofuVersion(String openTofuVersion) {

        this.openTofuVersion = openTofuVersion;
        return this;
    }

    /**
     * The required version of the OpenTofu which will execute the scripts.
     *
     * @return openTofuVersion
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_OPEN_TOFU_VERSION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getOpenTofuVersion() {
        return openTofuVersion;
    }


    @JsonProperty(JSON_PROPERTY_OPEN_TOFU_VERSION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setOpenTofuVersion(String openTofuVersion) {
        this.openTofuVersion = openTofuVersion;
    }

    public OpenTofuAsyncDeployFromScriptsRequest isPlanOnly(Boolean isPlanOnly) {

        this.isPlanOnly = isPlanOnly;
        return this;
    }

    /**
     * Flag to control if the deployment must only generate the OpenTofu or it must also apply the changes.
     *
     * @return isPlanOnly
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_IS_PLAN_ONLY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getIsPlanOnly() {
        return isPlanOnly;
    }


    @JsonProperty(JSON_PROPERTY_IS_PLAN_ONLY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIsPlanOnly(Boolean isPlanOnly) {
        this.isPlanOnly = isPlanOnly;
    }

    public OpenTofuAsyncDeployFromScriptsRequest variables(Map<String, Object> variables) {

        this.variables = variables;
        return this;
    }

    public OpenTofuAsyncDeployFromScriptsRequest putVariablesItem(String key,
                                                                  Object variablesItem) {
        this.variables.put(key, variablesItem);
        return this;
    }

    /**
     * Key-value pairs of variables that must be used to execute the OpenTofu request.
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

    public OpenTofuAsyncDeployFromScriptsRequest envVariables(Map<String, String> envVariables) {

        this.envVariables = envVariables;
        return this;
    }

    public OpenTofuAsyncDeployFromScriptsRequest putEnvVariablesItem(String key,
                                                                     String envVariablesItem) {
        if (this.envVariables == null) {
            this.envVariables = new HashMap<>();
        }
        this.envVariables.put(key, envVariablesItem);
        return this;
    }

    /**
     * Key-value pairs of variables that must be injected as environment variables to OpenTofu process.
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

    public OpenTofuAsyncDeployFromScriptsRequest scripts(List<String> scripts) {

        this.scripts = scripts;
        return this;
    }

    public OpenTofuAsyncDeployFromScriptsRequest addScriptsItem(String scriptsItem) {
        if (this.scripts == null) {
            this.scripts = new ArrayList<>();
        }
        this.scripts.add(scriptsItem);
        return this;
    }

    /**
     * List of OpenTofu script files to be considered for deploying changes.
     *
     * @return scripts
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_SCRIPTS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getScripts() {
        return scripts;
    }


    @JsonProperty(JSON_PROPERTY_SCRIPTS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    public OpenTofuAsyncDeployFromScriptsRequest webhookConfig(WebhookConfig webhookConfig) {

        this.webhookConfig = webhookConfig;
        return this;
    }

    /**
     * Get webhookConfig
     *
     * @return webhookConfig
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_WEBHOOK_CONFIG)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public WebhookConfig getWebhookConfig() {
        return webhookConfig;
    }


    @JsonProperty(JSON_PROPERTY_WEBHOOK_CONFIG)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setWebhookConfig(WebhookConfig webhookConfig) {
        this.webhookConfig = webhookConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenTofuAsyncDeployFromScriptsRequest openTofuAsyncDeployFromScriptsRequest =
                (OpenTofuAsyncDeployFromScriptsRequest) o;
        return Objects.equals(this.requestId, openTofuAsyncDeployFromScriptsRequest.requestId) &&
                Objects.equals(this.openTofuVersion,
                        openTofuAsyncDeployFromScriptsRequest.openTofuVersion) &&
                Objects.equals(this.isPlanOnly, openTofuAsyncDeployFromScriptsRequest.isPlanOnly) &&
                Objects.equals(this.variables, openTofuAsyncDeployFromScriptsRequest.variables) &&
                Objects.equals(this.envVariables,
                        openTofuAsyncDeployFromScriptsRequest.envVariables) &&
                Objects.equals(this.scripts, openTofuAsyncDeployFromScriptsRequest.scripts) &&
                Objects.equals(this.webhookConfig,
                        openTofuAsyncDeployFromScriptsRequest.webhookConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, openTofuVersion, isPlanOnly, variables, envVariables,
                scripts, webhookConfig);
    }

    @Override
    public String toString() {
        String sb = "class OpenTofuAsyncDeployFromScriptsRequest {\n"
                + "    requestId: " + toIndentedString(requestId) + "\n"
                + "    openTofuVersion: " + toIndentedString(openTofuVersion) + "\n"
                + "    isPlanOnly: " + toIndentedString(isPlanOnly) + "\n"
                + "    variables: " + toIndentedString(variables) + "\n"
                + "    envVariables: " + toIndentedString(envVariables) + "\n"
                + "    scripts: " + toIndentedString(scripts) + "\n"
                + "    webhookConfig: " + toIndentedString(webhookConfig) + "\n"
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

