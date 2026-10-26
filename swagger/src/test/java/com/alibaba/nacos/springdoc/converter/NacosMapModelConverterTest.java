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

import com.alibaba.nacos.api.model.response.NacosMember;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NacosMapModelConverterTest {
    
    private ModelConverters converters;
    
    @BeforeEach
    void setUp() {
        converters = new ModelConverters();
        converters.addConverter(new NacosMapModelConverter());
    }
    
    @Test
    void testMemberDynamicMap() {
        Map<String, io.swagger.v3.oas.models.media.Schema> properties =
            properties(NacosMember.class);
        
        assertEquals(Boolean.TRUE, properties.get("extendInfo").getAdditionalProperties());
        assertEquals("object", properties.get("extendInfo").getType());
        assertEquals("string", properties.get("ip").getType());
        assertEquals("integer", properties.get("port").getType());
    }
    
    @Test
    void testDynamicMapType() {
        AnnotatedType type = new AnnotatedType(new TypeReference<Map<String, Object>>() {
        }.getType()).schemaProperty(true);
        io.swagger.v3.oas.models.media.Schema schema =
            converters.resolveAsResolvedSchema(type).schema;
        
        assertEquals("object", schema.getType());
        assertEquals(Boolean.TRUE, schema.getAdditionalProperties());
    }
    
    @Test
    void testNestedDynamicMaps() {
        Map<String, io.swagger.v3.oas.models.media.Schema> properties =
            properties(NestedMaps.class);
        io.swagger.v3.oas.models.media.Schema inner =
            (io.swagger.v3.oas.models.media.Schema) properties.get("maps")
                .getAdditionalProperties();
        
        assertEquals("object", inner.getType());
        assertEquals(Boolean.TRUE, inner.getAdditionalProperties());
        assertEquals(Boolean.TRUE, properties.get("list").getItems().getAdditionalProperties());
    }
    
    @Test
    void testKeepTypedMapsUnchanged() {
        assertUnchanged(TypedMaps.class);
    }
    
    @Test
    void testKeepExplicitSchemasUnchanged() {
        assertUnchanged(ExplicitMaps.class);
        assertUnchanged(ObjectValues.class);
        assertUnchanged(GetterSchema.class);
    }
    
    @Test
    void testPreserveSchemaMetadataAndConstraints() {
        assertUnchanged(DescribedMap.class);
        io.swagger.v3.oas.models.media.Schema map = properties(DescribedMap.class).get("values");
        io.swagger.v3.oas.models.media.Schema values =
            (io.swagger.v3.oas.models.media.Schema) map.getAdditionalProperties();
        
        assertEquals("Dynamic values", values.getDescription());
        assertTrue(values.getNullable());
        assertTrue(values.getDeprecated());
        assertEquals(1, values.getMinProperties());
        assertEquals(10, values.getMaxProperties());
    }
    
    @Test
    void testKeepNonMapObjectsUnchanged() {
        assertUnchanged(OtherObjects.class);
    }
    
    @Test
    void testMissingSchemaOrConverter() {
        NacosMapModelConverter converter = new NacosMapModelConverter();
        AnnotatedType type = new AnnotatedType(Map.class);
        ModelConverter next = (annotatedType, context, chain) -> null;
        
        assertNull(converter.resolve(type, null, Collections.emptyIterator()));
        assertNull(converter.resolve(type, null, Collections.singletonList(next).iterator()));
    }
    
    private Map<String, io.swagger.v3.oas.models.media.Schema> properties(Class<?> model) {
        return converters.resolveAsResolvedSchema(new AnnotatedType(model)).schema.getProperties();
    }
    
    private void assertUnchanged(Class<?> model) {
        io.swagger.v3.core.converter.ResolvedSchema expected = new ModelConverters()
            .resolveAsResolvedSchema(new AnnotatedType(model));
        io.swagger.v3.core.converter.ResolvedSchema actual = converters
            .resolveAsResolvedSchema(new AnnotatedType(model));
        assertEquals(expected.schema, actual.schema);
        assertEquals(expected.referencedSchemas, actual.referencedSchemas);
    }
    
    private static class NestedMaps {
        
        public Map<String, Map<String, Object>> maps;
        
        public List<Map<String, Object>> list;
    }
    
    private static class TypedMaps {
        
        public Map<String, String> strings;
        
        public Map<String, Integer> integers;
        
        public Map<String, Detail> details;
        
        public Map<String, ? extends Detail> bounded;
    }
    
    private static class ExplicitMaps {
        
        @Schema(additionalProperties = Schema.AdditionalPropertiesValue.FALSE)
        public Map<String, Object> closed;
        
        @Schema(additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        public Map<String, Object> open;
        
        @Schema(additionalPropertiesSchema = String.class)
        public Map<String, Object> strings;
        
        @Schema(additionalPropertiesSchema = Object.class)
        public Map<String, Object> objects;
        
        @Schema(type = "object")
        public Map<String, Object> explicitType;
        
        @Schema(implementation = Detail.class)
        public Map<String, Object> implementation;
        
        @Schema(ref = "#/components/schemas/ObjectValues")
        public Map<String, Object> reference;
        
        @Schema(allOf = ObjectValues.class)
        public Map<String, Object> composed;
    }
    
    @Schema(additionalPropertiesSchema = Object.class)
    private static class ObjectValues extends LinkedHashMap<String, Object> {
        
        private static final long serialVersionUID = 1L;
    }
    
    private static class GetterSchema {
        
        @Schema(additionalPropertiesSchema = Object.class)
        public Map<String, Object> getValues() {
            return null;
        }
    }
    
    private static class DescribedMap {
        
        @Schema(description = "Dynamic values", nullable = true, deprecated = true,
            minProperties = 1, maxProperties = 10)
        public Map<String, Object> values;
    }
    
    private static class OtherObjects {
        
        public Object value;
        
        public Detail detail;
    }
    
    private static class Detail {
        
        public String name;
    }
}
