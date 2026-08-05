/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.springdoc.operation;

import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.springdoc.cache.SchemaCache;
import com.alibaba.nacos.springdoc.openapi.NacosGenericSchemaOpenApiCustomizer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class NacosGenericSchemaOperationCustomizeTest {
    
    private SchemaCache schemaCache;
    
    private NacosGenericSchemaOperationCustomize customizer;
    
    @BeforeEach
    void setUp() {
        schemaCache = new SchemaCache();
        customizer = new NacosGenericSchemaOperationCustomize(schemaCache);
    }
    
    @Test
    void shouldCustomizeDirectResult() throws NoSuchMethodException {
        Operation operation = operationWithResponse("#/components/schemas/ResultTestModel");
        
        customizer.customize(operation, handlerMethod("directResult"));
        
        assertEquals("#/components/schemas/Result<TestModel>", getResponseSchemaRef(operation));
        assertTrue(schemaCache.getAllSchemas().containsKey("Result<TestModel>"));
        assertTrue(schemaCache.getAllSchemas().containsKey("TestModel"));
    }
    
    @Test
    void shouldCustomizeResultWrappedByResponseEntity() throws NoSuchMethodException {
        Operation operation = operationWithResponse("#/components/schemas/Result");
        operation.getResponses().addApiResponse("304", new ApiResponse());
        
        customizer.customize(operation, handlerMethod("responseEntityResult"));
        assertDoesNotThrow(() -> new NacosExampleI18nOperationCustomize(
            mock(PropertyResolverUtils.class)).customize(operation,
                handlerMethod("responseEntityResult")));
        
        assertEquals("#/components/schemas/Result<TestModel>", getResponseSchemaRef(operation));
        assertNull(operation.getResponses().get("304").getContent());
        assertTrue(schemaCache.getAllSchemas().containsKey("Result<TestModel>"));
    }
    
    @Test
    void shouldKeepCustomizedResponseReferenceResolvable() throws NoSuchMethodException {
        Operation operation = operationWithResponse("#/components/schemas/Result");
        customizer.customize(operation, handlerMethod("responseEntityResult"));
        Components components = new Components().addSchemas("Result", new ObjectSchema())
            .addSchemas("ResultTestModel", new ObjectSchema());
        OpenAPI openApi = new OpenAPI().components(components);
        
        new NacosGenericSchemaOpenApiCustomizer(schemaCache).customise(openApi);
        
        String schemaRef = getResponseSchemaRef(operation);
        String schemaName = schemaRef.substring("#/components/schemas/".length());
        assertTrue(openApi.getComponents().getSchemas().containsKey(schemaName));
        assertFalse(openApi.getComponents().getSchemas().containsKey("Result"));
        assertFalse(openApi.getComponents().getSchemas().containsKey("ResultTestModel"));
    }
    
    @Test
    void shouldIgnoreResponseEntityWithoutResult() throws NoSuchMethodException {
        Operation operation = operationWithResponse("#/components/schemas/ByteArray");
        
        Operation customized = customizer.customize(operation,
            handlerMethod("responseEntityBytes"));
        
        assertSame(operation, customized);
        assertEquals("#/components/schemas/ByteArray", getResponseSchemaRef(operation));
        assertTrue(schemaCache.getAllSchemas().isEmpty());
    }
    
    @Test
    void shouldHandleResponsesWithoutSchema() throws NoSuchMethodException {
        Operation operation = operationWithResponse("#/components/schemas/ResultTestModel");
        Content emptyContent = new Content();
        emptyContent.addMediaType("*/*", null);
        operation.getResponses().addApiResponse("304", new ApiResponse().content(emptyContent));
        
        assertDoesNotThrow(
            () -> customizer.customize(operation, handlerMethod("responseEntityResult")));
        assertFalse(operation.getResponses().get("304").getContent().isEmpty());
        assertNull(operation.getResponses().get("304").getContent().get("*/*"));
    }
    
    @Test
    void shouldHandleOperationWithoutResponses() throws NoSuchMethodException {
        Operation operation = new Operation();
        
        assertDoesNotThrow(
            () -> customizer.customize(operation, handlerMethod("responseEntityResult")));
        assertNull(operation.getResponses());
    }
    
    private Operation operationWithResponse(String schemaRef) {
        Schema<?> schema = new Schema<>().$ref(schemaRef);
        MediaType mediaType = new MediaType().schema(schema);
        Content content = new Content().addMediaType("application/json", mediaType);
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", new ApiResponse().content(content));
        return new Operation().responses(responses);
    }
    
    private String getResponseSchemaRef(Operation operation) {
        return operation.getResponses().get("200").getContent().get("application/json")
            .getSchema().get$ref();
    }
    
    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new TestController(), method);
    }
    
    private static class TestController {
        
        public Result<TestModel> directResult() {
            return null;
        }
        
        public ResponseEntity<Result<TestModel>> responseEntityResult() {
            return null;
        }
        
        public ResponseEntity<byte[]> responseEntityBytes() {
            return null;
        }
    }
    
    private static class TestModel {
        
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
