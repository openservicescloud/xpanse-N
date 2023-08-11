/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import jakarta.transaction.Transactional;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.ServiceDeployerApi;
import org.eclipse.xpanse.api.ServiceTemplateApi;
import org.eclipse.xpanse.common.openapi.OpenApiUtil;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.response.Response;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.models.service.utils.DeployVariableValidator;
import org.eclipse.xpanse.modules.models.service.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.service.view.ServiceVo;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.eclipse.xpanse.modules.models.servicetemplate.utils.OclLoader;
import org.eclipse.xpanse.modules.models.servicetemplate.view.ServiceTemplateVo;
import org.eclipse.xpanse.modules.servicetemplate.utils.ServiceTemplateOpenApiGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for ServiceDeployerApiTest.
 */
@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@ActiveProfiles("default")
@SpringBootTest(classes = {XpanseApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceDeployerApiTest {

    private static final String CLIENT_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/"
            + "openapitools/openapi-generator-cli/6.5.0/openapi-generator-cli-6.5.0.jar";
    private static final String OPENAPI_PATH = "openapi/";
    private static final Integer SERVICER_PORT = 8080;
    private static final String uuid = "e2d4de73-1518-40f7-8de1-60f184ea6e1d";
    private static final String userId = "defaultUserId";
    private static Ocl oclRegister;
    @Autowired
    private ServiceDeployerApi serviceDeployerApi;
    @Autowired
    private ServiceTemplateApi serviceTemplateApi;

    @BeforeAll
    static void init() throws Exception {
        OclLoader oclLoader = new OclLoader();
        oclRegister = oclLoader.getOcl(new URL("file:src/test/resources/ocl_test.yaml"));
        oclRegister.setVersion("2.1");

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("TASK_ID", uuid);
        MDC.setContextMap(contextMap);
    }

    @AfterAll
    static void tearDown() {
        MDC.clear();
    }

    @Test
    @Order(1)
    void testDownLoadOpenApiJar() {
        ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(UUID.randomUUID());
        serviceTemplateEntity.setName("kafka");
        serviceTemplateEntity.setVersion("2.0");
        serviceTemplateEntity.setCsp(Csp.HUAWEI);
        serviceTemplateEntity.setCategory(Category.MIDDLEWARE);
        serviceTemplateEntity.setOcl(oclRegister);
        serviceTemplateEntity.setServiceRegistrationState(ServiceRegistrationState.REGISTERED);
        DeployVariableValidator deployVariableValidator = new DeployVariableValidator();
        OpenApiUtil openApiUtil = new OpenApiUtil(CLIENT_DOWNLOAD_URL,
                OPENAPI_PATH, SERVICER_PORT);
        ServiceTemplateOpenApiGenerator
                registeredServicesOpenApiUtil = new ServiceTemplateOpenApiGenerator(
                deployVariableValidator, openApiUtil
        );
        registeredServicesOpenApiUtil.createServiceApi(serviceTemplateEntity);
        String openApiWorkdir = openApiUtil.getOpenApiWorkdir();
        File htmlFile = new File(openApiWorkdir,
                serviceTemplateEntity.getId().toString() + ".html");
        Assertions.assertTrue(htmlFile.exists());
    }

    @Disabled
    @Test
    void getDeployedServiceDetailsById() throws Exception {
        ServiceTemplateVo serviceTemplateVo = serviceTemplateApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(serviceTemplateVo.getName());
        createRequest.setVersion(serviceTemplateVo.getVersion());
        createRequest.setCsp(serviceTemplateVo.getCsp());
        createRequest.setCategory(serviceTemplateVo.getCategory());
        createRequest.setFlavor(serviceTemplateVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                serviceTemplateVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(createRequest);
        ServiceDetailVo deployedServiceDetailsById =
                serviceDeployerApi.getDeployedServiceDetailsById(deployUUid.toString());
        log.error(deployedServiceDetailsById.toString());
        Assertions.assertNotNull(deployedServiceDetailsById);
    }

    @Disabled
    @Test
    void listMyDeployedServices() throws Exception {
        ServiceTemplateVo serviceTemplateVo = serviceTemplateApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(serviceTemplateVo.getName());
        createRequest.setVersion(serviceTemplateVo.getVersion());
        createRequest.setCsp(serviceTemplateVo.getCsp());
        createRequest.setCategory(serviceTemplateVo.getCategory());
        createRequest.setFlavor(serviceTemplateVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                serviceTemplateVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        serviceDeployerApi.deploy(createRequest);
        List<ServiceVo> deployedServicesByUser =
                serviceDeployerApi.listMyDeployedServices();
        log.error(deployedServicesByUser.toString());
        Assertions.assertFalse(deployedServicesByUser.isEmpty());
    }

    @Disabled
    @Test
    void deploy() throws Exception {
        ServiceTemplateVo serviceTemplateVo = serviceTemplateApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(serviceTemplateVo.getName());
        createRequest.setVersion(serviceTemplateVo.getVersion());
        createRequest.setCsp(serviceTemplateVo.getCsp());
        createRequest.setCategory(serviceTemplateVo.getCategory());
        createRequest.setFlavor(serviceTemplateVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                serviceTemplateVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        Assertions.assertDoesNotThrow(() -> {
            UUID deployUUid = serviceDeployerApi.deploy(createRequest);
            Assertions.assertNotNull(deployUUid);
        });
    }

    @Disabled
    @Test
    void destroy() throws Exception {
        ServiceTemplateVo serviceTemplateVo = serviceTemplateApi.register(oclRegister);
        Thread.sleep(3000);
        CreateRequest createRequest = new CreateRequest();
        createRequest.setUserId(userId);
        createRequest.setServiceName(serviceTemplateVo.getName());
        createRequest.setVersion(serviceTemplateVo.getVersion());
        createRequest.setCsp(serviceTemplateVo.getCsp());
        createRequest.setCategory(serviceTemplateVo.getCategory());
        createRequest.setFlavor(serviceTemplateVo.getOcl().getFlavors().get(0).toString());
        createRequest.setRegion(
                serviceTemplateVo.getOcl().getCloudServiceProvider().getRegions().get(0)
                        .toString());
        Map<String, String> serviceRequestProperties = new HashMap<>();
        serviceRequestProperties.put("secgroup_id", "e2d4de73-1518-40f7-8de1-60f184ea6e1d");
        createRequest.setServiceRequestProperties(serviceRequestProperties);

        UUID deployUUid = serviceDeployerApi.deploy(createRequest);

        Response response = serviceDeployerApi.destroy(deployUUid.toString());
        Assertions.assertTrue(response.getSuccess());
    }
}
