/*
 * Tofu-Maker API
 * RESTful Services to interact with Tofu-Maker runtime
 *
 * The version of the OpenAPI document: 1.0.1-SNAPSHOT
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
import java.util.Objects;

/**
 * OpenTofuPlan
 */
@JsonPropertyOrder({
  OpenTofuPlan.JSON_PROPERTY_PLAN
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
public class OpenTofuPlan {
  public static final String JSON_PROPERTY_PLAN = "plan";
  private String plan;

  public OpenTofuPlan() {
  }

  public OpenTofuPlan plan(String plan) {
    
    this.plan = plan;
    return this;
  }

   /**
   * OpenTofu plan as a JSON string
   * @return plan
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PLAN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getPlan() {
    return plan;
  }


  @JsonProperty(JSON_PROPERTY_PLAN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPlan(String plan) {
    this.plan = plan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenTofuPlan openTofuPlan = (OpenTofuPlan) o;
    return Objects.equals(this.plan, openTofuPlan.plan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(plan);
  }

  @Override
  public String toString() {
    String sb = "class OpenTofuPlan {\n"
            + "    plan: " + toIndentedString(plan) + "\n"
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

