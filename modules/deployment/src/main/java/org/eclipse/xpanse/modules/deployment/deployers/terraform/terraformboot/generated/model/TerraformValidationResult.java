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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * TerraformValidationResult
 */
@JsonPropertyOrder({
        TerraformValidationResult.JSON_PROPERTY_VALID,
        TerraformValidationResult.JSON_PROPERTY_TERRAFORM_VERSION_USED,
        TerraformValidationResult.JSON_PROPERTY_DIAGNOSTICS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.9.0")
public class TerraformValidationResult {
    public static final String JSON_PROPERTY_VALID = "valid";
    public static final String JSON_PROPERTY_TERRAFORM_VERSION_USED = "terraformVersionUsed";
    public static final String JSON_PROPERTY_DIAGNOSTICS = "diagnostics";
    private Boolean valid;
    private String terraformVersionUsed;
    private List<TerraformValidateDiagnostics> diagnostics = new ArrayList<>();

    public TerraformValidationResult() {
    }

    public TerraformValidationResult valid(Boolean valid) {

        this.valid = valid;
        return this;
    }

    /**
     * Defines if the Terraform scripts is valid.
     *
     * @return valid
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_VALID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Boolean getValid() {
        return valid;
    }


    @JsonProperty(JSON_PROPERTY_VALID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public TerraformValidationResult terraformVersionUsed(String terraformVersionUsed) {

        this.terraformVersionUsed = terraformVersionUsed;
        return this;
    }

    /**
     * The version of the Terraform binary used to execute scripts.
     *
     * @return terraformVersionUsed
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_TERRAFORM_VERSION_USED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTerraformVersionUsed() {
        return terraformVersionUsed;
    }


    @JsonProperty(JSON_PROPERTY_TERRAFORM_VERSION_USED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTerraformVersionUsed(String terraformVersionUsed) {
        this.terraformVersionUsed = terraformVersionUsed;
    }

    public TerraformValidationResult diagnostics(List<TerraformValidateDiagnostics> diagnostics) {

        this.diagnostics = diagnostics;
        return this;
    }

    public TerraformValidationResult addDiagnosticsItem(
            TerraformValidateDiagnostics diagnosticsItem) {
        if (this.diagnostics == null) {
            this.diagnostics = new ArrayList<>();
        }
        this.diagnostics.add(diagnosticsItem);
        return this;
    }

    /**
     * List of validation errors.
     *
     * @return diagnostics
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_DIAGNOSTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TerraformValidateDiagnostics> getDiagnostics() {
        return diagnostics;
    }


    @JsonProperty(JSON_PROPERTY_DIAGNOSTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDiagnostics(List<TerraformValidateDiagnostics> diagnostics) {
        this.diagnostics = diagnostics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraformValidationResult terraformValidationResult = (TerraformValidationResult) o;
        return Objects.equals(this.valid, terraformValidationResult.valid) &&
                Objects.equals(this.terraformVersionUsed,
                        terraformValidationResult.terraformVersionUsed) &&
                Objects.equals(this.diagnostics, terraformValidationResult.diagnostics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, terraformVersionUsed, diagnostics);
    }

    @Override
    public String toString() {
        String sb = "class TerraformValidationResult {\n"
                + "    valid: " + toIndentedString(valid) + "\n"
                + "    terraformVersionUsed: " + toIndentedString(terraformVersionUsed)
                + "\n"
                + "    diagnostics: " + toIndentedString(diagnostics) + "\n"
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

