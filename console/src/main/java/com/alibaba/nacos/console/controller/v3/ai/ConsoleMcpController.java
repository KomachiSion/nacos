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

package com.alibaba.nacos.console.controller.v3.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.mcp.admin.McpDetailForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpImportForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpListForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpServerDraftForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpServerLabelsForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpServerScopeForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpServerStatusForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpServerVersionForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpServerVersionListForm;
import com.alibaba.nacos.ai.form.mcp.admin.McpUpdateForm;
import com.alibaba.nacos.ai.param.McpHttpParamExtractor;
import com.alibaba.nacos.ai.utils.McpRequestUtil;
import com.alibaba.nacos.api.ai.model.mcp.McpEndpointSpec;
import com.alibaba.nacos.api.ai.model.mcp.McpServerVersionDetail;
import com.alibaba.nacos.api.ai.model.mcp.McpServerVersionSummary;
import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerDetailInfo;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportRequest;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportResponse;
import com.alibaba.nacos.api.ai.model.mcp.McpServerImportValidationResult;
import com.alibaba.nacos.api.ai.model.mcp.McpToolSpecification;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.api.utils.StringUtils;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.config.McpEndpointAccessValidator;
import com.alibaba.nacos.console.proxy.ai.McpProxy;
import com.alibaba.nacos.core.controller.compatibility.CompatibilityHelper;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.alibaba.nacos.api.ai.constant.AiConstants.Mcp.MCP_PROTOCOL_SSE;
import static com.alibaba.nacos.api.ai.constant.AiConstants.Mcp.MCP_PROTOCOL_STREAMABLE;

/**
 * Nacos Console AI MCP Server Constants.
 *
 * @author xiweng.yy
 */
@NacosApi
@RestController
@RequestMapping(Constants.MCP_CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = McpHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.mcp.api.controller.name",
    description = "nacos.console.ai.mcp.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleMcpController {
    
    private static final String MCP_SERVER_SPEC_EXAMPLE =
        "{\"protocol\":\"stdio\",\"frontProtocol\":\"stdio\",\"name\":\"test\",\"id\":\"\","
            + "\"description\":\"ceshi\",\"versionDetail\":{\"version\":\"1.0.0\"},"
            + "\"enabled\":true,\"localServerConfig\":{\"test\":{}}}";
    
    private static final String MCP_SERVER_SPEC_WITH_ID_EXAMPLE =
        "{\"protocol\":\"stdio\",\"frontProtocol\":\"stdio\",\"name\":\"test\","
            + "\"id\":\"d7a64724-a556-4fe4-82fa-e806d43e00dc\",\"description\":\"ceshi\","
            + "\"versionDetail\":{\"version\":\"1.0.0\"},\"enabled\":true,"
            + "\"localServerConfig\":{\"test\":{}}}";
    
    private static final String MCP_SERVER_SPEC_TEXT_EXAMPLE =
        "\"{\\\"protocol\\\":\\\"stdio\\\",\\\"frontProtocol\\\":\\\"stdio\\\","
            + "\\\"name\\\":\\\"test\\\",\\\"versionDetail\\\":{"
            + "\\\"version\\\":\\\"1.0.0\\\"}}\"";
    
    private static final String MCP_SERVER_SPEC_WITH_ID_TEXT_EXAMPLE =
        "\"{\\\"protocol\\\":\\\"stdio\\\",\\\"frontProtocol\\\":\\\"stdio\\\","
            + "\\\"name\\\":\\\"test\\\",\\\"id\\\":\\\"d7a64724-a556-4fe4-82fa-e806d43e00dc\\\","
            + "\\\"versionDetail\\\":{\\\"version\\\":\\\"1.0.0\\\"}}\"";
    
    private final McpProxy mcpProxy;
    
    private final McpEndpointAccessValidator mcpEndpointAccessValidator;
    
    public ConsoleMcpController(McpProxy mcpProxy,
        McpEndpointAccessValidator mcpEndpointAccessValidator) {
        this.mcpProxy = mcpProxy;
        this.mcpEndpointAccessValidator = mcpEndpointAccessValidator;
    }
    
    /**
     * List mcp server.
     *
     * @param mcpListForm list mcp servers request form
     * @param pageForm    page info
     * @return mcp server list wrapper with {@link Result}
     * @throws NacosApiException if request parameter is invalid or handle error
     */
    @Since("3.0.0")
    @GetMapping(value = "/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.list.summary",
        description = "nacos.console.ai.mcp.api.list.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.list.example")))
    @Parameters(value = {
        @Parameter(name = "pageNo",
            schema = @Schema(type = "integer", defaultValue = "1", minimum = "1"),
            example = "1"),
        @Parameter(name = "pageSize",
            schema = @Schema(type = "integer", defaultValue = "100", minimum = "1"),
            example = "100"),
        @Parameter(name = "namespaceId", example = "public"), @Parameter(name = "mcpName"),
        @Parameter(name = "search", example = "blur", description = "blur or accurate"),
        @Parameter(name = "mcpListForm", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<McpServerBasicInfo>> listMcpServers(McpListForm mcpListForm,
        PageForm pageForm)
        throws NacosException {
        mcpListForm.validate();
        pageForm.validate();
        return Result.success(
            mcpProxy.listMcpServers(mcpListForm.getNamespaceId(), mcpListForm.getMcpName(),
                mcpListForm.getSearch(),
                pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * Import tools from mcp result.
     *
     * @param transportType the transport type
     * @param baseUrl       the base url
     * @param endpoint      the endpoint
     * @return the result
     * @throws NacosException the nacos exception
     */
    @Since("3.0.3")
    @GetMapping("/importToolsFromMcp")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.import.tools.summary",
        description = "nacos.console.ai.mcp.api.import.tools.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.import.tools.example")))
    @Parameters(value = {@Parameter(name = "transportType", example = "mcp-sse", required = true),
        @Parameter(name = "baseUrl", required = true),
        @Parameter(name = "endpoint", required = true),
        @Parameter(name = "authToken")})
    public Result<List<McpSchema.Tool>> importToolsFromMcp(@RequestParam String transportType,
        @RequestParam String baseUrl, @RequestParam String endpoint,
        @RequestParam(required = false) String authToken) throws NacosException {
        if (!StringUtils.equals(transportType, MCP_PROTOCOL_SSE)
            && !StringUtils.equals(transportType, MCP_PROTOCOL_STREAMABLE)) {
            return Result.failure(ErrorCode.SERVER_ERROR.getCode(),
                "Unsupported transport type: " + transportType,
                null);
        }
        try {
            mcpEndpointAccessValidator.validate(baseUrl, endpoint);
        } catch (SecurityException e) {
            return Result.failure(ErrorCode.ACCESS_DENIED.getCode(), e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            return Result.failure(ErrorCode.PARAMETER_VALIDATE_ERROR.getCode(), e.getMessage(),
                null);
        }
        McpClientTransport transport;
        if (StringUtils.equals(transportType, MCP_PROTOCOL_SSE)) {
            HttpClientSseClientTransport.Builder transportBuilder =
                HttpClientSseClientTransport.builder(baseUrl)
                    .sseEndpoint(endpoint)
                    .customizeClient(builder -> builder
                        .followRedirects(java.net.http.HttpClient.Redirect.NEVER));
            if (!StringUtils.isBlank(authToken)) {
                transportBuilder
                    .customizeRequest(req -> req.header("Authorization", "Bearer " + authToken));
            }
            transport = transportBuilder.build();
        } else {
            HttpClientStreamableHttpTransport.Builder transportBuilder =
                HttpClientStreamableHttpTransport.builder(
                    baseUrl).endpoint(endpoint)
                    .customizeClient(builder -> builder
                        .followRedirects(java.net.http.HttpClient.Redirect.NEVER));
            if (!StringUtils.isBlank(authToken)) {
                transportBuilder
                    .customizeRequest(req -> req.header("Authorization", "Bearer " + authToken));
            }
            transport = transportBuilder.build();
        }
        try (McpSyncClient client =
            McpClient.sync(transport).requestTimeout(Duration.ofSeconds(10)).build()) {
            client.initialize();
            McpSchema.ListToolsResult tools = client.listTools();
            return Result.success(tools.tools());
        } catch (Exception e) {
            // 可以记录日志或抛出 NacosException
            throw new NacosException(NacosException.SERVER_ERROR,
                "Failed to import tools from MCP server", e);
        }
    }
    
    /**
     * Get specified mcp server detail info.
     *
     * @param mcpForm get mcp server request form
     * @return detail info with {@link McpServerDetailInfo}
     * @throws NacosException any exception during handling
     */
    @Since("3.0.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.get.summary",
        description = "nacos.console.ai.mcp.api.get.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpId", example = "d7a64724-a556-4fe4-82fa-e806d43e00dc"),
        @Parameter(name = "mcpName", example = "test"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "mcpForm", hidden = true)})
    public Result<McpServerDetailInfo> getMcpServer(McpForm mcpForm) throws NacosException {
        mcpForm.validate();
        return Result.success(mcpProxy.getMcpServer(mcpForm.getNamespaceId(), mcpForm.getMcpName(),
            mcpForm.getMcpId(),
            mcpForm.getVersion()));
    }
    
    /**
     * Create new mcp server.
     *
     * @param mcpForm create mcp server request form
     * @throws NacosException any exception during handling
     */
    @Since("3.0.0")
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.create.summary",
        description = "nacos.console.ai.mcp.api.create.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.create.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName",
            description = "nacos.console.ai.mcp.api.mcpName.fallback.description"),
        @Parameter(name = "mcpId", deprecated = true,
            description = "nacos.console.ai.mcp.api.mcpId.fallback.description"),
        @Parameter(name = "serverSpecification", required = true, schema = @Schema(type = "string",
            description = "JSON object string parsed as McpServerBasicInfo"),
            example = MCP_SERVER_SPEC_TEXT_EXAMPLE),
        @Parameter(name = "toolSpecification", schema = @Schema(type = "string",
            description = "JSON object string parsed as McpToolSpecification"), example = "\"{}\""),
        @Parameter(name = "endpointSpecification", schema = @Schema(type = "string",
            description = "JSON object string parsed as McpEndpointSpec"), example = "\"{}\""),
        @Parameter(name = "mcpForm", hidden = true)})
    public Result<String> createMcpServer(McpDetailForm mcpForm) throws NacosException {
        mcpForm.validate();
        McpServerBasicInfo basicInfo = McpRequestUtil.parseMcpServerBasicInfo(mcpForm);
        McpToolSpecification mcpTools = McpRequestUtil.parseMcpTools(mcpForm);
        McpEndpointSpec endpointSpec = McpRequestUtil.parseMcpEndpointSpec(basicInfo, mcpForm);
        String mcpId =
            mcpProxy.createMcpServer(mcpForm.getNamespaceId(), basicInfo, mcpTools, endpointSpec);
        return Result.success(mcpId);
    }
    
    /**
     * Update existed mcp server.
     *
     * <p>
     * `namespaceId` and `mcpName` can't be changed.
     * </p>
     *
     * @param mcpForm update mcp servers request form
     * @throws NacosException any exception during handling
     */
    @Since("3.0.0")
    @PutMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.update.summary",
        description = "nacos.console.ai.mcp.api.update.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName",
            description = "nacos.console.ai.mcp.api.mcpName.fallback.description"),
        @Parameter(name = "mcpId", deprecated = true,
            description = "nacos.console.ai.mcp.api.mcpId.fallback.description"),
        @Parameter(name = "latest", schema = @Schema(type = "boolean"), example = "true"),
        @Parameter(name = "overrideExisting", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "serverSpecification", required = true, schema = @Schema(type = "string",
            description = "JSON object string parsed as McpServerBasicInfo"),
            example = MCP_SERVER_SPEC_WITH_ID_TEXT_EXAMPLE),
        @Parameter(name = "toolSpecification", schema = @Schema(type = "string",
            description = "JSON object string parsed as McpToolSpecification"), example = "\"{}\""),
        @Parameter(name = "endpointSpecification", schema = @Schema(type = "string",
            description = "JSON object string parsed as McpEndpointSpec"), example = "\"{}\""),
        @Parameter(name = "mcpForm", hidden = true)})
    public Result<String> updateMcpServer(McpUpdateForm mcpForm) throws NacosException {
        mcpForm.validate();
        McpServerBasicInfo basicInfo = McpRequestUtil.parseMcpServerBasicInfo(mcpForm);
        McpToolSpecification mcpTools = McpRequestUtil.parseMcpTools(mcpForm);
        McpEndpointSpec endpointSpec = McpRequestUtil.parseMcpEndpointSpec(basicInfo, mcpForm);
        mcpProxy.updateMcpServer(mcpForm.getNamespaceId(), mcpForm.getLatest(), basicInfo, mcpTools,
            endpointSpec,
            mcpForm.isOverrideExisting());
        return Result.success("ok");
    }
    
    /**
     * Delete existed mcp server.
     *
     * @param mcpForm delete mcp server request form
     * @throws NacosException any exception during handling
     */
    @Since("3.0.0")
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.delete.summary",
        description = "nacos.console.ai.mcp.api.delete.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", example = "test"), @Parameter(name = "mcpForm", hidden = true),
        @Parameter(name = "mcpId", example = "d7a64724-a556-4fe4-82fa-e806d43e00dc"),
        @Parameter(name = "version", example = "1.0.0")})
    public Result<String> deleteMcpServer(McpForm mcpForm) throws NacosException {
        mcpForm.validate();
        mcpProxy.deleteMcpServer(mcpForm.getNamespaceId(), mcpForm.getMcpName(), mcpForm.getMcpId(),
            mcpForm.getVersion());
        return Result.success("ok");
    }
    
    /**
     * Page management metadata for the Versions of one MCP resource.
     */
    @Since("3.3.0")
    @GetMapping("/versions")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.versions.summary",
        description = "nacos.console.ai.mcp.api.versions.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.versions.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "status",
            schema = @Schema(type = "string",
                allowableValues = {"draft", "reviewing", "reviewed", "online", "offline"})),
        @Parameter(name = "pageNo",
            schema = @Schema(type = "integer", defaultValue = "1", minimum = "1"), example = "1"),
        @Parameter(name = "pageSize",
            schema = @Schema(type = "integer", defaultValue = "100", minimum = "1"),
            example = "100"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<McpServerVersionSummary>> listMcpServerVersions(
        McpServerVersionListForm form, PageForm pageForm) throws NacosException {
        form.validate();
        pageForm.validate();
        return Result.success(mcpProxy.listMcpServerVersions(form.getNamespaceId(),
            form.getMcpName(), form.getStatus(), pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * Read one exact MCP Version.
     */
    @Since("3.3.0")
    @GetMapping("/version")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.version.summary",
        description = "nacos.console.ai.mcp.api.version.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.version.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionDetail> getMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.getMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Create one new MCP draft Version.
     */
    @Since("3.3.0")
    @PostMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.draft.create.summary",
        description = "nacos.console.ai.mcp.api.draft.create.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.draft.create.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "serverSpecification", required = true,
            description = "nacos.console.ai.mcp.api.draft.serverSpecification.description",
            schema = @Schema(type = "string"),
            example = "\"{\\\"protocol\\\":\\\"stdio\\\",\\\"frontProtocol\\\":\\\"stdio\\\"}\""),
        @Parameter(name = "toolSpecification",
            description = "nacos.console.ai.mcp.api.draft.toolSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "resourceSpecification",
            description = "nacos.console.ai.mcp.api.draft.resourceSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "endpointSpecification",
            description = "nacos.console.ai.mcp.api.draft.endpointSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionDetail> createMcpServerDraft(
        McpServerDraftForm form) throws NacosException {
        form.validate();
        McpServerBasicInfo server = McpRequestUtil.parseMcpServerBasicInfo(form);
        return Result.success(mcpProxy.createMcpServerDraft(form.getNamespaceId(), server,
            McpRequestUtil.parseMcpTools(form), McpRequestUtil.parseMcpResources(form),
            McpRequestUtil.parseMcpEndpointSpec(server, form)));
    }
    
    /**
     * Replace one exact current MCP draft.
     */
    @Since("3.3.0")
    @PutMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.draft.update.summary",
        description = "nacos.console.ai.mcp.api.draft.update.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.draft.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "serverSpecification", required = true,
            description = "nacos.console.ai.mcp.api.draft.serverSpecification.description",
            schema = @Schema(type = "string"),
            example = "\"{\\\"protocol\\\":\\\"stdio\\\",\\\"frontProtocol\\\":\\\"stdio\\\"}\""),
        @Parameter(name = "toolSpecification",
            description = "nacos.console.ai.mcp.api.draft.toolSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "resourceSpecification",
            description = "nacos.console.ai.mcp.api.draft.resourceSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "endpointSpecification",
            description = "nacos.console.ai.mcp.api.draft.endpointSpecification.description",
            schema = @Schema(type = "string")),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionDetail> updateMcpServerDraft(
        McpServerDraftForm form) throws NacosException {
        form.validate();
        McpServerBasicInfo server = McpRequestUtil.parseMcpServerBasicInfo(form);
        return Result.success(mcpProxy.updateMcpServerDraft(form.getNamespaceId(), server,
            McpRequestUtil.parseMcpTools(form), McpRequestUtil.parseMcpResources(form),
            McpRequestUtil.parseMcpEndpointSpec(server, form)));
    }
    
    /**
     * Delete one exact current MCP draft.
     */
    @Since("3.3.0")
    @DeleteMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.draft.delete.summary",
        description = "nacos.console.ai.mcp.api.draft.delete.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.draft.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<Void> deleteMcpServerDraft(McpServerVersionForm form)
        throws NacosException {
        form.validate();
        mcpProxy.deleteMcpServerDraft(form.getNamespaceId(), form.getMcpName(),
            form.getVersion());
        return Result.success();
    }
    
    /**
     * Submit one exact MCP working Version.
     */
    @Since("3.3.0")
    @PostMapping("/submit")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.submit.summary",
        description = "nacos.console.ai.mcp.api.submit.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.submit.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionSummary> submitMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.submitMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Publish one exact reviewed MCP Version.
     */
    @Since("3.3.0")
    @PostMapping("/publish")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.publish.summary",
        description = "nacos.console.ai.mcp.api.publish.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionSummary> publishMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.publishMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Force-publish one exact MCP working Version.
     */
    @Since("3.3.0")
    @PostMapping("/force-publish")
    @Secured(resource = Constants.MCP_CONSOLE_PATH + "/force-publish",
        action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.force.publish.summary",
        description = "nacos.console.ai.mcp.api.force.publish.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.force.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionSummary> forcePublishMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.forcePublishMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Return one exact reviewed MCP Version to draft.
     */
    @Since("3.3.0")
    @PostMapping("/redraft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.redraft.summary",
        description = "nacos.console.ai.mcp.api.redraft.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.redraft.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionSummary> redraftMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.redraftMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Bring one exact offline MCP Version online and make it latest.
     */
    @Since("3.3.0")
    @PostMapping("/online")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.online.summary",
        description = "nacos.console.ai.mcp.api.online.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.online.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionSummary> onlineMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.onlineMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Take one exact online MCP Version offline.
     */
    @Since("3.3.0")
    @PostMapping("/offline")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.offline.summary",
        description = "nacos.console.ai.mcp.api.offline.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.offline.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<McpServerVersionSummary> offlineMcpServerVersion(
        McpServerVersionForm form) throws NacosException {
        form.validate();
        return Result.success(mcpProxy.offlineMcpServerVersion(form.getNamespaceId(),
            form.getMcpName(), form.getVersion()));
    }
    
    /**
     * Replace custom MCP labels while preserving the server-managed latest label.
     */
    @Since("3.3.0")
    @PutMapping("/labels")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.labels.summary",
        description = "nacos.console.ai.mcp.api.labels.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.labels.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "labels", schema = @Schema(type = "string"),
            example = "\"{\\\"stable\\\":\\\"1.0.0\\\"}\""),
        @Parameter(name = "form", hidden = true)})
    public Result<Map<String, String>> updateMcpServerLabels(McpServerLabelsForm form)
        throws NacosException {
        form.validate();
        Map<String, String> labels = McpRequestUtil.parseMcpServerLabels(form.getLabels());
        return Result.success(mcpProxy.updateMcpServerLabels(form.getNamespaceId(),
            form.getMcpName(), labels));
    }
    
    /**
     * Enable or disable one MCP Server Resource without changing Version states.
     */
    @Since("3.3.0")
    @PutMapping("/status")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.status.summary",
        description = "nacos.console.ai.mcp.api.status.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.status.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "enabled", required = true, schema = @Schema(type = "boolean"),
            example = "true"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateMcpServerStatus(McpServerStatusForm form)
        throws NacosException {
        form.validate();
        mcpProxy.updateMcpServerStatus(form.getNamespaceId(), form.getMcpName(),
            form.getEnabled());
        return Result.success("ok");
    }
    
    /**
     * Update one MCP Server Resource visibility scope.
     */
    @Since("3.3.0")
    @PutMapping("/scope")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.scope.summary",
        description = "nacos.console.ai.mcp.api.scope.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.scope.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "mcpName", required = true, example = "my-mcp"),
        @Parameter(name = "scope", required = true,
            schema = @Schema(type = "string", allowableValues = {"PUBLIC", "PRIVATE"}),
            example = "PUBLIC"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateMcpServerScope(McpServerScopeForm form)
        throws NacosException {
        form.validate();
        mcpProxy.updateMcpServerScope(form.getNamespaceId(), form.getMcpName(), form.getScope());
        return Result.success("ok");
    }
    
    /**
     * Validate MCP server import request.
     *
     * @param mcpImportForm import request form
     * @return validation result with details about potential issues
     * @throws NacosException any exception during validation
     * @deprecated use {@code POST /v3/console/ai/import/validate} instead. Planned for removal in
     *     Nacos 3.4.0.
     */
    @Deprecated
    @Since("3.1.0")
    @PostMapping("/import/validate")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.import.validate.summary",
        description = "nacos.console.ai.mcp.api.import.validate.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.import.validate.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "importType", example = "url", required = true,
            description = "enum of `file`, `json`, `url`"),
        @Parameter(name = "data", required = true),
        @Parameter(name = "overrideExisting", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "validateOnly", schema = @Schema(type = "boolean"),
            example = "true"),
        @Parameter(name = "skipInvalid", schema = @Schema(type = "boolean"), example = "false"),
        @Parameter(name = "selectedServers",
            array = @ArraySchema(schema = @Schema(type = "string")),
            example = "[\"mcp-server\"]"),
        @Parameter(name = "limit", schema = @Schema(type = "integer"), example = "10"),
        @Parameter(name = "cursor",
            description = "Optional start cursor for URL-based import pagination."),
        @Parameter(name = "search",
            description = "Optional fuzzy search keyword for registry import listing. Only used when importType is 'url'."),
        @Parameter(name = "mcpImportForm", hidden = true)})
    public Result<McpServerImportValidationResult> validateImport(McpImportForm mcpImportForm)
        throws NacosException {
        CompatibilityHelper.check("POST /v3/console/ai/import/validate");
        mcpImportForm.validate();
        McpServerImportRequest request = convertToImportRequest(mcpImportForm);
        McpServerImportValidationResult result =
            mcpProxy.validateImport(mcpImportForm.getNamespaceId(), request);
        return Result.success(result);
    }
    
    /**
     * Execute MCP server import operation.
     *
     * @param mcpImportForm import request form
     * @return import response with results and statistics
     * @throws NacosException any exception during import execution
     * @deprecated use {@code POST /v3/console/ai/import/execute} instead. Planned for removal in
     *     Nacos 3.4.0.
     */
    @Deprecated
    @Since("3.1.0")
    @PostMapping("/import/execute")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.mcp.api.import.execute.summary",
        description = "nacos.console.ai.mcp.api.import.execute.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.mcp.api.import.execute.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "importType", example = "url", required = true,
            description = "enum of `file`, `json`, `url`"),
        @Parameter(name = "data", required = true),
        @Parameter(name = "overrideExisting", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "validateOnly", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "skipInvalid", schema = @Schema(type = "boolean"), example = "false"),
        @Parameter(name = "selectedServers",
            array = @ArraySchema(schema = @Schema(type = "string")),
            example = "[\"mcp-server\"]"),
        @Parameter(name = "limit", schema = @Schema(type = "integer"), example = "10"),
        @Parameter(name = "cursor",
            description = "Optional start cursor for URL-based import pagination."),
        @Parameter(name = "search",
            description = "Optional fuzzy search keyword for registry import listing. Only used when importType is 'url'."),
        @Parameter(name = "mcpImportForm", hidden = true)})
    public Result<McpServerImportResponse> executeImport(McpImportForm mcpImportForm)
        throws NacosException {
        CompatibilityHelper.check("POST /v3/console/ai/import/execute");
        mcpImportForm.validate();
        McpServerImportRequest request = convertToImportRequest(mcpImportForm);
        McpServerImportResponse response =
            mcpProxy.executeImport(mcpImportForm.getNamespaceId(), request);
        return Result.success(response);
    }
    
    /**
     * Convert McpImportForm to McpServerImportRequest.
     *
     * @param form the form from HTTP request
     * @return the import request for service layer
     * @deprecated part of the legacy MCP import endpoint bridge. Planned for removal in Nacos
     *     3.4.0.
     */
    @Deprecated
    private McpServerImportRequest convertToImportRequest(McpImportForm form) {
        McpServerImportRequest request = new McpServerImportRequest();
        request.setImportType(form.getImportType());
        request.setData(form.getData());
        request.setOverrideExisting(form.isOverrideExisting());
        request.setValidateOnly(form.isValidateOnly());
        request.setSkipInvalid(form.isSkipInvalid());
        request.setSelectedServers(form.getSelectedServers());
        // Optional URL pagination parameters
        request.setCursor(form.getCursor());
        request.setLimit(form.getLimit());
        // Optional registry search parameter
        request.setSearch(form.getSearch());
        return request;
    }
}
