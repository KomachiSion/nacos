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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.annotation.Annotation;
import java.util.Iterator;

/**
 * Match numeric property schemas to their explicit Jackson string representation.
 *
 * @author xiweng.yy
 */
public class NacosJsonFormatModelConverter implements ModelConverter {
    
    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context,
        Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }
        Schema property = chain.next().resolve(type, context, chain);
        if (!type.isSchemaProperty() || null == property || null == type.getCtxAnnotations()
            || !("integer".equals(property.getType()) || "number".equals(property.getType()))) {
            return property;
        }
        for (Annotation annotation : type.getCtxAnnotations()) {
            if (annotation instanceof JsonFormat
                && JsonFormat.Shape.STRING == ((JsonFormat) annotation).shape()) {
                property.setType("string");
                property.setFormat(null);
                break;
            }
        }
        return property;
    }
}
