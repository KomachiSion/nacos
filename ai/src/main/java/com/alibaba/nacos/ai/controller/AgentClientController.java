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

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.agent.client.AgentDiscoveryForm;
import com.alibaba.nacos.ai.form.agent.client.AgentEndpointDeregistrationForm;
import com.alibaba.nacos.ai.form.agent.client.AgentEndpointRegistrationForm;
import com.alibaba.nacos.ai.form.agent.client.AgentPublishForm;
import com.alibaba.nacos.ai.form.agent.client.AgentSearchForm;
import com.alibaba.nacos.ai.param.AgentClientHttpParamExtractor;
import com.alibaba.nacos.ai.service.agent.AgentDiscoveryApplicationService;
import com.alibaba.nacos.ai.service.agent.AgentPublishApplicationService;
import com.alibaba.nacos.ai.service.agent.runtime.AgentHttpClientLifecycleService;
import com.alibaba.nacos.api.ai.model.agent.ClientLivenessInfo;
import com.alibaba.nacos.api.ai.model.agent.AgentPublishRequest;
import com.alibaba.nacos.api.ai.model.agent.AgentVersionDetail;
import com.alibaba.nacos.api.ai.model.rad.AgentCatalogEntry;
import com.alibaba.nacos.api.ai.model.rad.AgentDiscoveryRequest;
import com.alibaba.nacos.api.ai.model.rad.AgentDiscoveryResult;
import com.alibaba.nacos.api.ai.model.rad.AgentEndpointRegistrationBatch;
import com.alibaba.nacos.api.ai.model.rad.AgentSearchRequest;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.constants.ClientConstants;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAD Agent Client HTTP API.
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Agent.CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = AgentClientHttpParamExtractor.class)
@Tag(name = "nacos.admin.ai.agent.client.api.controller.name",
    description = "nacos.admin.ai.agent.client.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class AgentClientController {
    
    private final AgentDiscoveryApplicationService discoveryService;
    
    private final AgentHttpClientLifecycleService clientLifecycleService;
    
    private final AgentPublishApplicationService publishService;
    
    public AgentClientController(AgentDiscoveryApplicationService discoveryService,
        AgentHttpClientLifecycleService clientLifecycleService,
        AgentPublishApplicationService publishService) {
        this.discoveryService = discoveryService;
        this.clientLifecycleService = clientLifecycleService;
        this.publishService = publishService;
    }
    
    /**
     * Publish one exact Agent Version from application code.
     */
    @Since("3.3.0")
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agent.client.api.publish.summary",
        description = "nacos.admin.ai.agent.client.api.publish.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agent.client.api.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "displayName", example = "My Agent"),
        @Parameter(name = "description", example = "Agent description"),
        @Parameter(name = "iconUrl", example = "https://example.com/agent.png"),
        @Parameter(name = "provider", description = "Agent provider JSON object"),
        @Parameter(name = "tags", description = "Agent tag JSON array"),
        @Parameter(name = "extensions", description = "Agent extension JSON object"),
        @Parameter(name = "callInterfaces", description = "Call interface JSON array"),
        @Parameter(name = "author", example = "nacos"),
        @Parameter(name = "changeDescription", example = "Initial version"),
        @Parameter(name = "basedOnVersion", example = "0.9.0"),
        @Parameter(name = "autoSubmit", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionDetail> publish(AgentPublishForm form) throws NacosException {
        AgentPublishRequest request = form.toRequest();
        return Result.success(publishService.publish(form.getNamespaceId(), request));
    }
    
    /**
     * Search visible Agent catalog entries.
     */
    @Since("3.3.0")
    @GetMapping("/search")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agent.client.api.search.summary",
        description = "nacos.admin.ai.agent.client.api.search.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agent.client.api.search.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentNameContains", example = "agent"),
        @Parameter(name = "tagsAll", array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "protocolsAny",
            array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "pageNo", schema = @Schema(type = "integer"), example = "1"),
        @Parameter(name = "pageSize", schema = @Schema(type = "integer"), example = "20"),
        @Parameter(name = "form", hidden = true)})
    public Result<Page<AgentCatalogEntry>> search(AgentSearchForm form,
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            description = "nacos.admin.ai.agent.client.api.http.client.id.query.header.description",
            in = ParameterIn.HEADER) @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
                required = false) String clientId)
        throws NacosException {
        AgentSearchRequest request = form.toRequest();
        clientLifecycleService.renewForQuery(clientId, request.getNamespaceId());
        return Result.success(discoveryService.search(request));
    }
    
    /**
     * Discover one exact Agent Version and its current Endpoint sets.
     */
    @Since("3.3.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agent.client.api.discover.summary",
        description = "nacos.admin.ai.agent.client.api.discover.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agent.client.api.discover.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "label", example = "latest"),
        @Parameter(name = "protocol", array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "protocolVersion", example = "1.0"),
        @Parameter(name = "transport", array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "endpointSource",
            array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "metadataSelector", description = "Metadata selector JSON object"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentDiscoveryResult> discover(AgentDiscoveryForm form,
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            description = "nacos.admin.ai.agent.client.api.http.client.id.query.header.description",
            in = ParameterIn.HEADER) @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
                required = false) String clientId)
        throws NacosException {
        AgentDiscoveryRequest request = form.toRequest();
        clientLifecycleService.renewForQuery(clientId, request.getNamespaceId());
        return Result.success(discoveryService.discover(request));
    }
    
    /**
     * Replace one HTTP Publisher's complete Agent Endpoint batch.
     */
    @Since("3.3.0")
    @PostMapping("/endpoints")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agent.client.api.endpoints.register.summary",
        description = "nacos.admin.ai.agent.client.api.endpoints.register.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agent.client.api.endpoints.register.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "runtimeVersion", required = true, example = "1.0.0"),
        @Parameter(name = "versionRange", example = "[1.0.0,2.0.0)"),
        @Parameter(name = "protocol", required = true, example = "a2a"),
        @Parameter(name = "endpoints", required = true, description = "Endpoint JSON array"),
        @Parameter(name = "form", hidden = true)})
    public Result<ClientLivenessInfo> registerEndpoints(AgentEndpointRegistrationForm form,
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            description = "nacos.admin.ai.agent.client.api.http.client.id.publisher.header.description",
            required = true,
            in = ParameterIn.HEADER) @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
                required = false) String clientId,
        @Parameter(name = HttpHeaderConsts.REQUEST_MODULE,
            description = "nacos.admin.ai.agent.client.api.request.module.header.description",
            required = true, in = ParameterIn.HEADER) @RequestHeader(
                name = HttpHeaderConsts.REQUEST_MODULE,
                required = false) String requestModule)
        throws NacosException {
        AgentEndpointRegistrationBatch batch = form.toRequest();
        return Result.success(clientLifecycleService.register(clientId, requestModule, batch));
    }
    
    /**
     * Remove one HTTP Publisher's complete Agent Endpoint publication.
     */
    @Since("3.3.0")
    @DeleteMapping("/endpoints")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agent.client.api.endpoints.deregister.summary",
        description = "nacos.admin.ai.agent.client.api.endpoints.deregister.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agent.client.api.endpoints.deregister.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "protocol", required = true, example = "a2a"),
        @Parameter(name = "form", hidden = true)})
    public Result<Void> deregisterEndpoints(AgentEndpointDeregistrationForm form,
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            description = "nacos.admin.ai.agent.client.api.http.client.id.publisher.header.description",
            required = true,
            in = ParameterIn.HEADER) @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
                required = false) String clientId,
        @Parameter(name = HttpHeaderConsts.REQUEST_MODULE,
            description = "nacos.admin.ai.agent.client.api.request.module.header.description",
            required = true, in = ParameterIn.HEADER) @RequestHeader(
                name = HttpHeaderConsts.REQUEST_MODULE,
                required = false) String requestModule)
        throws NacosException {
        form.validate();
        clientLifecycleService.deregister(clientId, requestModule, form.getNamespaceId(),
            form.getAgentName(), form.getProtocol());
        return Result.success();
    }
    
    /**
     * Refresh one HTTP Client and all Agent Endpoint publications it owns.
     */
    @Since("3.3.0")
    @PutMapping("/endpoints/heartbeat")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agent.client.api.endpoints.heartbeat.summary",
        description = "nacos.admin.ai.agent.client.api.endpoints.heartbeat.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agent.client.api.endpoints.heartbeat.example")))
    public Result<ClientLivenessInfo> heartbeat(
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            description = "nacos.admin.ai.agent.client.api.http.client.id.publisher.header.description",
            required = true,
            in = ParameterIn.HEADER) @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
                required = false) String clientId,
        @Parameter(name = HttpHeaderConsts.REQUEST_MODULE,
            description = "nacos.admin.ai.agent.client.api.request.module.header.description",
            required = true, in = ParameterIn.HEADER) @RequestHeader(
                name = HttpHeaderConsts.REQUEST_MODULE,
                required = false) String requestModule)
        throws NacosException {
        return Result.success(clientLifecycleService.heartbeat(clientId, requestModule));
    }
}
