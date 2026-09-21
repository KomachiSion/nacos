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

package com.alibaba.nacos.ai.controller;

import com.alibaba.nacos.api.remote.RemoteConstants;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.Operation;
import com.alibaba.nacos.api.ai.constant.AiConstants.Capability;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.ai.auth.AiCapabilityHttpResourceParser;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation capabilities of this node's Client HTTP binding.
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(Capability.CLIENT_PATH)
@Tag(name = "nacos.admin.ai.capability.client.api.controller.name",
    description = "nacos.admin.ai.capability.client.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class AiCapabilityClientController {
    
    private static final Map<String, Object> CAPABILITIES = createCapabilities();
    
    private static Map<String, Object> createCapabilities() {
        Map<String, Boolean> features = new LinkedHashMap<>();
        features.put(Capability.RAD_V1, true);
        features.put(Capability.MCP, true);
        features.put(Capability.SKILL, true);
        features.put(Capability.PROMPT, true);
        features.put(Capability.AGENT_SPEC, true);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("schemaVersion", Capability.SCHEMA_VERSION);
        response.put("capabilities", Collections.unmodifiableMap(features));
        return Collections.unmodifiableMap(response);
    }
    
    /**
     * Read binding capabilities without accessing resources or Client lifecycle state.
     *
     * @return HTTP capability declaration
     */
    @Since("3.3.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API,
        tags = Constants.Tag.ONLY_IDENTITY, parser = AiCapabilityHttpResourceParser.class)
    @Operation(summary = "nacos.admin.ai.capability.client.api.get.summary",
        description = "nacos.admin.ai.capability.client.api.get.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.capability.client.api.get.example")))
    public Result<Map<String, Object>> getCapabilities() {
        return Result.success(CAPABILITIES);
    }
}
