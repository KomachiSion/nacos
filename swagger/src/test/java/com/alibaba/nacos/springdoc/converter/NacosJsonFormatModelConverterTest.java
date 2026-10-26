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

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigDetailInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NacosJsonFormatModelConverterTest {
    
    private NacosJsonFormatModelConverter customizer;
    
    private ModelConverters converters;
    
    @BeforeEach
    void setUp() {
        customizer = new NacosJsonFormatModelConverter();
        converters = new ModelConverters();
        converters.addConverter(customizer);
    }
    
    @Test
    void testConfigIdSchemaMatchesSerializedString() {
        ConfigBasicInfo config = new ConfigBasicInfo();
        config.setId(862926428394491904L);
        config.setCreateTime(123L);
        JsonNode serialized = new ObjectMapper().valueToTree(config);
        Map<String, Schema> properties = resolveProperties(ConfigBasicInfo.class);
        
        assertTrue(serialized.get("id").isTextual());
        assertEquals("862926428394491904", serialized.get("id").asText());
        assertEquals("string", properties.get("id").getType());
        assertNull(properties.get("id").getFormat());
        assertTrue(serialized.get("createTime").isIntegralNumber());
        assertEquals("integer", properties.get("createTime").getType());
        assertEquals("int64", properties.get("createTime").getFormat());
    }
    
    @Test
    void testInheritedConfigIdSchema() {
        Map<String, Schema> properties = resolveProperties(ConfigDetailInfo.class);
        
        assertEquals("string", properties.get("id").getType());
        assertNull(properties.get("id").getFormat());
        assertEquals("integer", properties.get("modifyTime").getType());
        assertEquals("int64", properties.get("modifyTime").getFormat());
    }
    
    @Test
    void testPrimitiveAndGetterAnnotations() {
        Map<String, Schema> properties = resolveProperties(StringNumbers.class);
        JsonNode serialized = new ObjectMapper().valueToTree(new StringNumbers());
        
        for (String name : new String[] {"id", "amount"}) {
            assertTrue(serialized.get(name).isTextual());
            assertEquals("string", properties.get(name).getType());
            assertNull(properties.get(name).getFormat());
        }
    }
    
    @Test
    void testPreservePropertyMetadata() throws NoSuchFieldException {
        Schema property = new IntegerSchema().format("int64").description("Storage ID")
            .nullable(true).readOnly(true).deprecated(true);
        AnnotatedType type = new AnnotatedType(Long.class).ctxAnnotations(
            ConfigBasicInfo.class.getDeclaredField("id").getAnnotations());
        
        assertSame(property, resolve(property, type));
        assertEquals("string", property.getType());
        assertNull(property.getFormat());
        assertEquals("Storage ID", property.getDescription());
        assertTrue(property.getNullable());
        assertTrue(property.getReadOnly());
        assertTrue(property.getDeprecated());
    }
    
    @Test
    void testKeepUnrelatedSchemasUnchanged() {
        Schema expected = new ModelConverters()
            .resolveAsResolvedSchema(new AnnotatedType(UnchangedProperties.class)).schema;
        Schema actual = converters
            .resolveAsResolvedSchema(new AnnotatedType(UnchangedProperties.class)).schema;
        
        assertEquals(expected, actual);
    }
    
    @Test
    void testMissingAnnotationsOrSchema() {
        Schema property = new IntegerSchema().format("int64");
        
        assertSame(property, resolve(property, new AnnotatedType(Long.class)));
        assertEquals("integer", property.getType());
        assertEquals("int64", property.getFormat());
        assertNull(resolve(null, new AnnotatedType(Long.class)));
    }
    
    private Schema resolve(Schema property, AnnotatedType type) {
        ModelConverter next = (annotatedType, context, chain) -> property;
        return customizer.resolve(type.schemaProperty(true), null,
            Collections.singletonList(next).iterator());
    }
    
    private Map<String, Schema> resolveProperties(Class<?> model) {
        return converters.resolveAsResolvedSchema(new AnnotatedType(model)).schema.getProperties();
    }
    
    private static class StringNumbers {
        
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public long id = 123L;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double getAmount() {
            return 1.5;
        }
    }
    
    private static class UnchangedProperties {
        
        public long count;
        
        public double weight;
        
        public boolean enabled;
        
        public Map<String, Object> extendInfo;
        
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Long numericId;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public Date date;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public String name;
    }
}
