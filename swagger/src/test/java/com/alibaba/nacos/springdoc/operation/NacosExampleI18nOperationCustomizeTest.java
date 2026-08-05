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

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.utils.PropertyResolverUtils;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NacosExampleI18nOperationCustomizeTest {
    
    private PropertyResolverUtils propertyResolverUtils;
    
    private NacosExampleI18nOperationCustomize customizer;
    
    @BeforeEach
    void setUp() {
        propertyResolverUtils = mock(PropertyResolverUtils.class);
        customizer = new NacosExampleI18nOperationCustomize(propertyResolverUtils);
    }
    
    @Test
    void shouldIgnoreResponseWithoutContent() {
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("304", new ApiResponse());
        Operation operation = new Operation().responses(responses);
        
        Operation customized = assertDoesNotThrow(() -> customizer.customize(operation, null));
        
        assertSame(operation, customized);
        assertNull(operation.getResponses().get("304").getContent());
        verifyNoInteractions(propertyResolverUtils);
    }
    
    @Test
    void shouldResolveExampleAndSkipEmptyResponseContent() {
        Schema<?> schema = new Schema<>().example("example.key");
        Content content = new Content().addMediaType("text/plain",
            new MediaType().schema(schema));
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", new ApiResponse().content(content));
        responses.addApiResponse("304", new ApiResponse());
        Operation operation = new Operation().responses(responses);
        when(propertyResolverUtils.resolve(eq("example.key"), any(Locale.class)))
            .thenReturn("translated example");
        
        customizer.customize(operation, null);
        
        assertEquals("translated example", schema.getExample());
        assertNull(operation.getResponses().get("304").getContent());
    }
    
    @Test
    void shouldHandleOperationWithoutResponses() {
        Operation operation = new Operation();
        
        assertSame(operation,
            assertDoesNotThrow(() -> customizer.customize(operation, null)));
        verifyNoInteractions(propertyResolverUtils);
    }
    
    @Test
    void shouldHandleNullResponseAndMediaType() {
        Content content = new Content();
        content.addMediaType("*/*", null);
        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", null);
        responses.addApiResponse("304", new ApiResponse().content(content));
        Operation operation = new Operation().responses(responses);
        
        assertDoesNotThrow(() -> customizer.customize(operation, null));
        verifyNoInteractions(propertyResolverUtils);
    }
}
