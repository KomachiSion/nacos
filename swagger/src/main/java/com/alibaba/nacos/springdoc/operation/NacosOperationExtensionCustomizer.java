/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.springdoc.cache.LocaleThreadLocalHolder;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.utils.PropertyResolverUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;

/**
 * Spring doc operation extension customizer for nacos.
 *
 * @author xiweng.yy
 */
public class NacosOperationExtensionCustomizer implements GlobalOperationCustomizer {
    
    private static final String NACOS_API_SINCE_EXTENSION = "nacos-api-since";
    private static final String VERSION = "version";
    private final PropertyResolverUtils propertyResolverUtils;
    public NacosOperationExtensionCustomizer(PropertyResolverUtils propertyResolverUtils) {
        this.propertyResolverUtils = propertyResolverUtils;
    }
    
    @Override
    public io.swagger.v3.oas.models.Operation customize(
        io.swagger.v3.oas.models.Operation operation,
        HandlerMethod handlerMethod) {
        Operation apiOperation =
            AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Operation.class);
        if (null != apiOperation && 0 < apiOperation.extensions().length) {
            addOperationExtensions(operation, apiOperation);
        }
        addSinceExtension(operation, handlerMethod);
        return operation;
    }
    
    private void addOperationExtensions(io.swagger.v3.oas.models.Operation operation,
        Operation apiOperation) {
        Map<String, Object> extensions = AnnotationsUtils
            .getExtensions(propertyResolverUtils.isOpenapi31(), apiOperation.extensions());
        if (propertyResolverUtils.isResolveExtensionsProperties()) {
            extensions =
                propertyResolverUtils.resolveExtensions(LocaleThreadLocalHolder.getLocale(),
                    extensions);
        }
        extensions.forEach((name, value) -> addExtensionIfAbsent(operation, name, value));
    }
    
    private void addSinceExtension(io.swagger.v3.oas.models.Operation operation,
        HandlerMethod handlerMethod) {
        Since since = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(),
            Since.class);
        if (null == since) {
            since = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(),
                Since.class);
        }
        if (null != since && !since.value().isEmpty()) {
            addExtensionIfAbsent(operation, NACOS_API_SINCE_EXTENSION,
                Map.of(VERSION, since.value()));
        }
    }
    
    private void addExtensionIfAbsent(io.swagger.v3.oas.models.Operation operation, String name,
        Object value) {
        String extensionName = getExtensionName(name);
        if (null == operation.getExtensions()
            || !operation.getExtensions().containsKey(extensionName)) {
            operation.addExtension(extensionName, value);
        }
    }
    
    private String getExtensionName(String name) {
        return name.startsWith("x-") ? name : "x-" + name;
    }
}
