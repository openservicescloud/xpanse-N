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
 * OpenTofuAsyncDestroyFromScriptsRequest
 */
@JsonPropertyOrder({
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_REQUEST_ID,
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_OPEN_TOFU_VERSION,
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_VARIABLES,
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_ENV_VARIABLES,
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_SCRIPTS,
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_TF_STATE,
        OpenTofuAsyncDestroyFromScriptsRequest.JSON_PROPERTY_WEBHOOK_CONFIG
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.9.0")
public class OpenTofuAsyncDestroyFromScriptsRequest {
    public static final String JSON_PROPERTY_REQUEST_ID = "requestId";
    public static final String JSON_PROPERTY_OPEN_TOFU_VERSION = "openTofuVersion";
    public static final String JSON_PROPERTY_VARIABLES = "variables";
    public static final String JSON_PROPERTY_ENV_VARIABLES = "envVariables";
    public static final String JSON_PROPERTY_SCRIPTS = "scripts";
    public static final String JSON_PROPERTY_TF_STATE = "tfState";
    public static final String JSON_PROPERTY_WEBHOOK_CONFIG = "webhookConfig";
    private UUID requestId;
    private String openTofuVersion;
    private Map<String, Object> variables = new HashMap<>();
    private Map<String, String> envVariables = new HashMap<>();
    private List<String> scripts = new ArrayList<>();
    private String tfState;
    private WebhookConfig webhookConfig;

    public OpenTofuAsyncDestroyFromScriptsRequest() {
    }

    public OpenTofuAsyncDestroyFromScriptsRequest requestId(UUID requestId) {

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

    public OpenTofuAsyncDestroyFromScriptsRequest openTofuVersion(String openTofuVersion) {

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

    public OpenTofuAsyncDestroyFromScriptsRequest variables(Map<String, Object> variables) {

        this.variables = variables;
        return this;
    }

    public OpenTofuAsyncDestroyFromScriptsRequest putVariablesItem(String key,
                                                                   Object variablesItem) {
        this.variables.put(key, variablesItem);
        return this;
    }

    /**
     * Key-value pairs of regular variables that must be used to execute the OpenTofu request.
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

    public OpenTofuAsyncDestroyFromScriptsRequest envVariables(Map<String, String> envVariables) {

        this.envVariables = envVariables;
        return this;
    }

    public OpenTofuAsyncDestroyFromScriptsRequest putEnvVariablesItem(String key,
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

    public OpenTofuAsyncDestroyFromScriptsRequest scripts(List<String> scripts) {

        this.scripts = scripts;
        return this;
    }

    public OpenTofuAsyncDestroyFromScriptsRequest addScriptsItem(String scriptsItem) {
        if (this.scripts == null) {
            this.scripts = new ArrayList<>();
        }
        this.scripts.add(scriptsItem);
        return this;
    }

    /**
     * List of script files for destroy requests deployed via scripts
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

    public OpenTofuAsyncDestroyFromScriptsRequest tfState(String tfState) {

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

    public OpenTofuAsyncDestroyFromScriptsRequest webhookConfig(WebhookConfig webhookConfig) {

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
        OpenTofuAsyncDestroyFromScriptsRequest openTofuAsyncDestroyFromScriptsRequest =
                (OpenTofuAsyncDestroyFromScriptsRequest) o;
        return Objects.equals(this.requestId, openTofuAsyncDestroyFromScriptsRequest.requestId) &&
                Objects.equals(this.openTofuVersion,
                        openTofuAsyncDestroyFromScriptsRequest.openTofuVersion) &&
                Objects.equals(this.variables, openTofuAsyncDestroyFromScriptsRequest.variables) &&
                Objects.equals(this.envVariables,
                        openTofuAsyncDestroyFromScriptsRequest.envVariables) &&
                Objects.equals(this.scripts, openTofuAsyncDestroyFromScriptsRequest.scripts) &&
                Objects.equals(this.tfState, openTofuAsyncDestroyFromScriptsRequest.tfState) &&
                Objects.equals(this.webhookConfig,
                        openTofuAsyncDestroyFromScriptsRequest.webhookConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, openTofuVersion, variables, envVariables, scripts, tfState,
                webhookConfig);
    }

    @Override
    public String toString() {
        String sb = "class OpenTofuAsyncDestroyFromScriptsRequest {\n"
                + "    requestId: " + toIndentedString(requestId) + "\n"
                + "    openTofuVersion: " + toIndentedString(openTofuVersion) + "\n"
                + "    variables: " + toIndentedString(variables) + "\n"
                + "    envVariables: " + toIndentedString(envVariables) + "\n"
                + "    scripts: " + toIndentedString(scripts) + "\n"
                + "    tfState: " + toIndentedString(tfState) + "\n"
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

