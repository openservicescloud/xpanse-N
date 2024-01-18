/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class of terraform-boot env.
 */
@Getter
@Configuration
@Profile("terraform-boot")
public class TerraformBootConfig {

    @Value("${terraform.webhook.endpoint}")
    private String clientBaseUri;

    @Value("${webhook.deployCallbackUri}")
    private String deployCallbackUri;

    @Value("${webhook.destroyCallbackUri}")
    private String destroyCallbackUri;

}
