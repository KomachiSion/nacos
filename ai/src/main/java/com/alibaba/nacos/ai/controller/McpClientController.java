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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.mcp.client.McpEndpointForm;
import com.alibaba.nacos.ai.form.mcp.client.McpQueryForm;
import com.alibaba.nacos.ai.form.mcp.client.McpReleaseForm;
import com.alibaba.nacos.ai.form.mcp.client.McpSearchForm;
import com.alibaba.nacos.ai.param.McpHttpParamExtractor;
import com.alibaba.nacos.ai.service.mcp.McpClientApplicationService;
import com.alibaba.nacos.ai.service.runtime.AiHttpClientLifecycleService;
import com.alibaba.nacos.ai.service.search.AiResourceSearchApplicationService;
import com.alibaba.nacos.api.ai.model.ClientLivenessInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.remote.AiRemoteConstants;
import com.alibaba.nacos.api.ai.remote.request.McpServerEndpointRequest;
import com.alibaba.nacos.api.ai.remote.request.QueryMcpServerRequest;
import com.alibaba.nacos.api.ai.remote.request.ReleaseMcpServerRequest;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.naming.constants.ClientConstants;
import com.alibaba.nacos.naming.utils.NamingRequestUtil;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MCP Client HTTP API.
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.MCP_CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = McpHttpParamExtractor.class)
@Tag(name = "nacos.admin.ai.mcp.client.api.controller.name",
    description = "nacos.admin.ai.mcp.client.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class McpClientController {
    
    private static final String MCP_RELEASE_TEXT_EXAMPLE =
        "\"{\\\"protocol\\\":\\\"stdio\\\",\\\"frontProtocol\\\":\\\"std"
            + "io\\\",\\\"name\\\":\\\"my-mcp\\\",\\\"versionDetail\\\":{\\\"ve"
            + "rsion\\\":\\\"1.0.0\\\"}}\"";
    
    private final AiResourceSearchApplicationService searchService;
    
    private final McpClientApplicationService applicationService;
    
    private final AiHttpClientLifecycleService clientLifecycleService;
    
    public McpClientController(AiResourceSearchApplicationService searchService,
        McpClientApplicationService applicationService,
        AiHttpClientLifecycleService clientLifecycleService) {
        this.searchService = searchService;
        this.applicationService = applicationService;
        this.clientLifecycleService = clientLifecycleService;
    }
    
    /**
     * Query one exact or latest serving MCP Version.
     */
    @Since("3.3.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.mcp.client.api.get.summary",
        description = "nacos.admin.ai.mcp.client.api.get.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.mcp.client.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER, in = ParameterIn.HEADER,
            description = "nacos.admin.ai.mcp.client.api.http.client.id.query.header.description")})
    public Result<McpServerDetailInfo> query(McpQueryForm form,
        @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            required = false) String clientId)
        throws NacosException {
        QueryMcpServerRequest request = form.toRequest();
        clientLifecycleService.renewForQuery(clientId, request.getNamespaceId());
        return Result.success(applicationService.query(request.getNamespaceId(),
            request.getMcpName(), request.getVersion()));
    }
    
    /**
     * Release one MCP Version directly online or create a lifecycle draft.
     */
    @Since("3.3.0")
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.mcp.client.api.release.summary",
        description = "nacos.admin.ai.mcp.client.api.release.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.mcp.client.api.release.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", example = "my-mcp"),
        @Parameter(name = "serverSpecification", required = true,
            description = "nacos.admin.ai.mcp.client.api.release.serverSpecification.description",
            schema = @Schema(type = "string"), example = MCP_RELEASE_TEXT_EXAMPLE),
        @Parameter(name = "toolSpecification",
            description = "nacos.admin.ai.mcp.client.api.release.toolSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "resourceSpecification",
            description = "nacos.admin.ai.mcp.client.api.release.resourceSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "endpointSpecification",
            description = "nacos.admin.ai.mcp.client.api.release.endpointSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "createDraft",
            schema = @Schema(type = "string", allowableValues = {"true", "false"},
                defaultValue = "false"),
            example = "false"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> release(McpReleaseForm form) throws NacosException {
        ReleaseMcpServerRequest request = form.toRequest();
        return Result.success(applicationService.release(request, "HTTP Client"));
    }
    
    /**
     * Register one MCP Runtime Endpoint under the shared HTTP Client.
     */
    @Since("3.3.0")
    @PostMapping("/endpoints")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.mcp.client.api.endpoints.register.summary",
        description = "nacos.admin.ai.mcp.client.api.endpoints.register.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.mcp.client.api.endpoints.register.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "address", required = true, example = "127.0.0.1"),
        @Parameter(name = "port", required = true, schema = @Schema(type = "integer"),
            example = "8080"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = "httpRequest", hidden = true),
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER, in = ParameterIn.HEADER,
            required = true,
            description = "nacos.admin.ai.mcp.client.api.http.client.id.publisher.header.description"),
        @Parameter(name = HttpHeaderConsts.REQUEST_MODULE, in = ParameterIn.HEADER, required = true,
            description = "nacos.admin.ai.mcp.client.api.request.module.header.description")})
    public Result<ClientLivenessInfo> registerEndpoint(McpEndpointForm form,
        @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            required = false) String clientId,
        @RequestHeader(name = HttpHeaderConsts.REQUEST_MODULE,
            required = false) String requestModule,
        HttpServletRequest httpRequest)
        throws NacosException {
        McpServerEndpointRequest request = form.toRequest(AiRemoteConstants.REGISTER_ENDPOINT);
        String sourceIp = NamingRequestUtil.getSourceIpForHttpRequest(httpRequest);
        return Result.success(clientLifecycleService.register(clientId, requestModule,
            request.getNamespaceId(), internalClientId -> applicationService.operateEndpoint(
                request, internalClientId, sourceIp)));
    }
    
    /**
     * Deregister one MCP Runtime Endpoint owned by the shared HTTP Client.
     */
    @Since("3.3.0")
    @DeleteMapping("/endpoints")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.mcp.client.api.endpoints.deregister.summary",
        description = "nacos.admin.ai.mcp.client.api.endpoints.deregister.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.mcp.client.api.endpoints.deregister.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "address", required = true, example = "127.0.0.1"),
        @Parameter(name = "port", required = true, schema = @Schema(type = "integer"),
            example = "8080"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = "httpRequest", hidden = true),
        @Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER, in = ParameterIn.HEADER,
            required = true,
            description = "nacos.admin.ai.mcp.client.api.http.client.id.publisher.header.description"),
        @Parameter(name = HttpHeaderConsts.REQUEST_MODULE, in = ParameterIn.HEADER, required = true,
            description = "nacos.admin.ai.mcp.client.api.request.module.header.description")})
    public Result<Void> deregisterEndpoint(McpEndpointForm form,
        @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            required = false) String clientId,
        @RequestHeader(name = HttpHeaderConsts.REQUEST_MODULE,
            required = false) String requestModule,
        HttpServletRequest httpRequest)
        throws NacosException {
        McpServerEndpointRequest request = form.toRequest(AiRemoteConstants.DE_REGISTER_ENDPOINT);
        String sourceIp = NamingRequestUtil.getSourceIpForHttpRequest(httpRequest);
        clientLifecycleService.deregister(clientId, requestModule, request.getNamespaceId(),
            internalClientId -> applicationService.operateEndpoint(request, internalClientId,
                sourceIp));
        return Result.success();
    }
    
    /**
     * Refresh the shared HTTP Client and all Agent and MCP Endpoint publications it owns.
     */
    @Since("3.3.0")
    @PutMapping("/endpoints/heartbeat")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.mcp.client.api.endpoints.heartbeat.summary",
        description = "nacos.admin.ai.mcp.client.api.endpoints.heartbeat.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.mcp.client.api.endpoints.heartbeat.example")))
    @Parameters(value = {@Parameter(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
        in = ParameterIn.HEADER, required = true,
        description = "nacos.admin.ai.mcp.client.api.http.client.id.publisher.header.description"),
        @Parameter(name = HttpHeaderConsts.REQUEST_MODULE, in = ParameterIn.HEADER, required = true,
            description = "nacos.admin.ai.mcp.client.api.request.module.header.description")})
    public Result<ClientLivenessInfo> heartbeat(
        @RequestHeader(name = ClientConstants.HTTP_CLIENT_ID_HEADER,
            required = false) String clientId,
        @RequestHeader(name = HttpHeaderConsts.REQUEST_MODULE,
            required = false) String requestModule)
        throws NacosException {
        return Result.success(clientLifecycleService.heartbeat(clientId, requestModule));
    }
    
    /**
     * Search visible current MCP servers.
     */
    @Since("3.3.0")
    @GetMapping("/search")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.mcp.client.api.search.summary",
        description = "nacos.admin.ai.mcp.client.api.search.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.mcp.client.api.search.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "query", schema = @Schema(type = "string", maxLength = 1024)),
        @Parameter(name = "tagsAll", array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "protocolsAny", array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "capabilitiesAny",
            array = @ArraySchema(schema = @Schema(type = "string"))),
        @Parameter(name = "pageNo",
            schema = @Schema(type = "integer", defaultValue = "1", minimum = "1"), example = "1"),
        @Parameter(name = "pageSize",
            schema = @Schema(type = "integer", defaultValue = "100", minimum = "1"),
            example = "100"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<McpServerBasicInfo>> search(McpSearchForm form, PageForm pageForm)
        throws NacosException {
        form.validate();
        pageForm.validate();
        return Result.success(searchService.searchMcpServers(form, pageForm.getPageNo(),
            pageForm.getPageSize()));
    }
}
