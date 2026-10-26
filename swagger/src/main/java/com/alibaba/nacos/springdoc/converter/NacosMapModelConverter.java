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

package com.alibaba.nacos.springdoc.converter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.AdditionalPropertiesValue.USE_ADDITIONAL_PROPERTIES_ANNOTATION;

/**
 * Allow arbitrary JSON values in dynamic maps without overriding explicit schemas.
 *
 * @author xiweng.yy
 */
public class NacosMapModelConverter implements ModelConverter {
    
    private static final JsonNode OBJECT_SCHEMA = Json.mapper().valueToTree(new ObjectSchema());
    
    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context,
        Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }
        Schema schema = chain.next().resolve(type, context, chain);
        if (null == schema || null == type.getType()
            || !(schema.getAdditionalProperties() instanceof Schema)) {
            return schema;
        }
        JavaType javaType = Json.mapper().constructType(type.getType());
        if (!Map.class.isAssignableFrom(javaType.getRawClass())
            || null == javaType.getContentType() || !javaType.getContentType().isJavaLangObject()
            || hasExplicitSchema(type.getCtxAnnotations())
            || hasExplicitSchema(javaType.getRawClass().getAnnotations())
            || null != schema.get$ref() || null != schema.getProperties()
            || null != schema.getAllOf() || null != schema.getAnyOf()
            || null != schema.getOneOf() || null != schema.getNot()) {
            return schema;
        }
        // Swagger may clone ObjectSchema into Schema; compare their documented constraints.
        if (OBJECT_SCHEMA.equals(Json.mapper().valueToTree(schema.getAdditionalProperties()))) {
            schema.setAdditionalProperties(true);
        }
        return schema;
    }
    
    private boolean hasExplicitSchema(Annotation[] annotations) {
        if (null == annotations) {
            return false;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof io.swagger.v3.oas.annotations.media.Schema) {
                io.swagger.v3.oas.annotations.media.Schema schema =
                    (io.swagger.v3.oas.annotations.media.Schema) annotation;
                return schema.additionalProperties() != USE_ADDITIONAL_PROPERTIES_ANNOTATION
                    || Void.class != schema.additionalPropertiesSchema()
                    || Void.class != schema.implementation() || !schema.ref().isEmpty()
                    || !schema.type().isEmpty() || schema.types().length > 0;
            }
        }
        return false;
    }
}
