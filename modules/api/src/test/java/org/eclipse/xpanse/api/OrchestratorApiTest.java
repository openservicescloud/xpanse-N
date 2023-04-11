/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.api.response.Response;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.deployment.Deployment;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.DeployTask;
import org.eclipse.xpanse.modules.models.SystemStatus;
import org.eclipse.xpanse.modules.models.enums.Category;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.enums.ServiceState;
import org.eclipse.xpanse.modules.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.service.CreateRequest;
import org.eclipse.xpanse.modules.models.service.DeployResource;
import org.eclipse.xpanse.modules.models.utils.OclLoader;
import org.eclipse.xpanse.modules.models.view.CategoryOclVo;
import org.eclipse.xpanse.modules.models.view.OclDetailVo;
import org.eclipse.xpanse.modules.models.view.RegisteredServiceVo;
import org.eclipse.xpanse.modules.models.view.ServiceDetailVo;
import org.eclipse.xpanse.modules.models.view.ServiceVo;
import org.eclipse.xpanse.modules.models.view.VersionOclVo;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.eclipse.xpanse.orchestrator.register.RegisterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for OrchestratorApi.
 */
@ExtendWith(MockitoExtension.class)
public class OrchestratorApiTest {

    @Mock
    private OrchestratorService orchestratorService;

    @Mock
    private RegisterService registerService;

    @InjectMocks
    private OrchestratorApi orchestratorApi;

    @Test
    public void testListCategories() {
        Category[] categories = Category.values();
        List<Category> expectedCategories = Arrays.asList(categories);
        List<Category> categoryList = orchestratorApi.listCategories();
        Assertions.assertEquals(expectedCategories, categoryList);
    }

    @Test
    public void testRegister() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        UUID uuid = UUID.randomUUID();
        Mockito.when(registerService.registerService(Mockito.any(Ocl.class))).thenReturn(uuid);
        UUID uuidRegister = orchestratorApi.register(ocl);
        Assertions.assertEquals(uuid, uuidRegister);
    }

    @Test
    public void testUpdate() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        UUID uuid = UUID.randomUUID();
        Mockito.doNothing().when(registerService)
                .updateRegisteredService(Mockito.eq(uuid.toString()),
                        Mockito.eq(ocl));
        Response response = orchestratorApi.update(uuid.toString(), ocl);
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    public void testFetch() throws Exception {
        String oclLocation = "file:./target/test-classes/ocl_test.yaml";
        UUID uuid = UUID.randomUUID();
        Mockito.when(registerService.registerServiceByUrl(Mockito.anyString())).thenReturn(uuid);
        UUID uuidFetch = registerService.registerServiceByUrl(oclLocation);
        Assertions.assertEquals(uuid, uuidFetch);
    }

    @Test
    public void testFetchUpdate() throws Exception {
        String oclLocation = "file:./target/test-classes/ocl_test.yaml";
        UUID uuid = UUID.randomUUID();
        Mockito.doNothing().when(registerService)
                .updateRegisteredServiceByUrl(Mockito.eq(uuid.toString()),
                        Mockito.eq(oclLocation));
        Response response = orchestratorApi.fetchUpdate(uuid.toString(), oclLocation);
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    public void testUnregister() {
        UUID uuid = UUID.randomUUID();
        Mockito.doNothing().when(registerService).unregisterService(Mockito.eq(uuid.toString()));
        Response response = orchestratorApi.unregister(uuid.toString());
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    public void testListRegisteredServices() throws Exception {
        String categoryName = "middleware";
        String cspName = "huawei";
        String serviceName = "kafka";
        String serviceVersion = "v1.0";
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(UUID.randomUUID());
        registerServiceEntity.setName("kafka");
        registerServiceEntity.setVersion("1.0");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.COMPUTE);
        registerServiceEntity.setOcl(ocl);
        registerServiceEntity.setServiceState(ServiceState.REGISTERED);
        List serviceEntities = new ArrayList<>();
        serviceEntities.add(registerServiceEntity);
        Mockito.when(
                        registerService.queryRegisteredServices(Mockito.any(RegisteredServiceQuery.class)))
                .thenReturn(serviceEntities);
        List<RegisteredServiceVo> registeredServiceVos = orchestratorApi.listRegisteredServices(
                categoryName, cspName, serviceName, serviceVersion);
        Assertions.assertEquals(serviceEntities, registeredServiceVos);
    }

    @Test
    public void testListRegisteredServicesTree() {
        String categoryName = "middleware";
        VersionOclVo versionOclVo = new VersionOclVo();
        List versionOclVoList = new ArrayList<>();
        versionOclVoList.add(versionOclVo);
        CategoryOclVo categoryOclVo = new CategoryOclVo();
        categoryOclVo.setName("kafka");
        categoryOclVo.setVersions(versionOclVoList);
        List categoryOclVoList = new ArrayList<>();
        categoryOclVoList.add(categoryOclVo);
        Mockito.when(registerService.queryRegisteredServicesTree(
                Mockito.any(RegisteredServiceQuery.class))).thenReturn(categoryOclVoList);
        List<CategoryOclVo> categoryOclVos = orchestratorApi.listRegisteredServicesTree(
                categoryName);
        Assertions.assertEquals(categoryOclVoList, categoryOclVos);
    }

    @Test
    public void testDetail() {
        UUID uuid = UUID.randomUUID();
        OclDetailVo oclDetailVo = new OclDetailVo();
        oclDetailVo.setId(UUID.randomUUID());
        oclDetailVo.setCreateTime(new Date());
        oclDetailVo.setLastModifiedTime(new Date());
        oclDetailVo.setServiceState(ServiceState.REGISTERED);
        Mockito.when(registerService.getRegisteredService(uuid.toString())).thenReturn(oclDetailVo);
        OclDetailVo detail = orchestratorApi.detail(uuid.toString());
        Assertions.assertEquals(oclDetailVo, detail);
    }

    @Test
    public void testHealth() {
        SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(HealthStatus.OK);
        SystemStatus health = orchestratorApi.health();
        Assertions.assertEquals(systemStatus, health);
    }

    @Test
    public void testServiceDetail() {
        UUID uuid = UUID.randomUUID();
        ServiceDetailVo serviceDetailVo = new ServiceDetailVo();
        serviceDetailVo.setCreateRequest(new CreateRequest());
        List deployResouceList = new ArrayList();
        DeployResource deployResource = new DeployResource();
        deployResouceList.add(deployResource);
        serviceDetailVo.setDeployResources(deployResouceList);
        serviceDetailVo.setDeployedServiceProperties(new HashMap<>());
        Mockito.when(orchestratorService.getDeployServiceDetail(uuid)).thenReturn(serviceDetailVo);
        ServiceDetailVo serviceDetailVo1 = orchestratorApi.serviceDetail(uuid.toString());
        Assertions.assertEquals(serviceDetailVo, serviceDetailVo1);
    }

    @Test
    public void testServices() {
        ServiceVo serviceVo = new ServiceVo();
        serviceVo.setId(UUID.randomUUID());
        serviceVo.setCategory(Category.COMPUTE);
        serviceVo.setName("kafka");
        serviceVo.setVersion("1.0");
        serviceVo.setCsp(Csp.HUAWEI);
        serviceVo.setFlavor("3-2-node-without-zookeeper");
        serviceVo.setServiceState(ServiceState.REGISTERED);
        serviceVo.setCreateTime(new Date());
        serviceVo.setLastModifiedTime(new Date());
        List serviceVoList = new ArrayList<>();
        serviceVoList.add(serviceVo);
        Mockito.when(orchestratorService.listDeployServices()).thenReturn(serviceVoList);
        List<ServiceVo> services = orchestratorApi.services();
        Assertions.assertEquals(serviceVoList, services);
    }

    @Test
    public void testDeploy() throws Exception {
        OclLoader oclLoader = new OclLoader();
        Ocl ocl = oclLoader.getOcl(new URL("file:./target/test-classes/ocl_test.yaml"));
        CreateRequest createRequest = new CreateRequest();
        createRequest.setId(UUID.randomUUID());
        createRequest.setCategory(Category.COMPUTE);
        createRequest.setServiceName("kafka");
        createRequest.setCustomerServiceName("111");
        createRequest.setVersion("v1.0");
        createRequest.setRegion("cn-southwest-2");
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setFlavor("3-2-node-without-zookeeper");
        createRequest.setOcl(ocl);
        createRequest.setProperty(new HashMap<>());
        Deployment deploymentMock = Mockito.mock(Deployment.class);
        Mockito.when(orchestratorService.getDeployHandler(Mockito.any(DeployTask.class)))
                .thenReturn(deploymentMock);
        Mockito.doNothing().when(orchestratorService)
                .asyncDeployService(Mockito.any(Deployment.class),
                        Mockito.any(DeployTask.class));
        UUID uuid = orchestratorApi.deploy(createRequest);
        Assertions.assertEquals(createRequest.getId(), uuid);
    }

    @Test
    public void testDestroy() {
        UUID uuid = UUID.randomUUID();
        Deployment myDeploymentMock = Mockito.mock(Deployment.class);
        Mockito.when(orchestratorService.getDestroyHandler(Mockito.any(DeployTask.class)))
                .thenReturn(myDeploymentMock);
        Mockito.doNothing().when(orchestratorService)
                .asyncDestroyService(Mockito.any(Deployment.class),
                        Mockito.any(DeployTask.class));
        Response response = orchestratorApi.destroy(uuid.toString());
        Assertions.assertTrue(response.getSuccess());
    }

    @Test
    public void testOpenApi() throws IOException {
        String uuid = UUID.randomUUID().toString();
        String openApiUrl = "http://localhost:8080/openapi";
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(registerService.getOpenApiUrl(uuid)).thenReturn(openApiUrl);
        orchestratorApi.openApi(uuid, response);
        Mockito.verify(registerService).getOpenApiUrl(uuid);
        Mockito.verify(response).sendRedirect(openApiUrl);
    }
}
