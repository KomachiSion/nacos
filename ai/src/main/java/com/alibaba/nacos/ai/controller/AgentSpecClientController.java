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

package com.alibaba.nacos.ai.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.agentspecs.client.AgentSpecQueryForm;
import com.alibaba.nacos.ai.form.agentspecs.client.AgentSpecSearchForm;
import com.alibaba.nacos.ai.service.agentspecs.AgentSpecOperationService;
import com.alibaba.nacos.ai.service.agentspecs.AgentSpecQueryResult;
import com.alibaba.nacos.ai.utils.AgentSpecRequestUtil;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecBasicInfo;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.parser.http.AgentSpecNameHttpResourceParser;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Tag.ALLOW_ANONYMOUS;

/**
 * AgentSpec client controller for runtime read query.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.AgentSpecs.CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
@Tag(name = "nacos.admin.ai.agentspec.client.api.controller.name",
    description = "nacos.admin.ai.agentspec.client.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class AgentSpecClientController {
    
    private final AgentSpecOperationService agentSpecOperationService;
    
    public AgentSpecClientController(AgentSpecOperationService agentSpecOperationService) {
        this.agentSpecOperationService = agentSpecOperationService;
    }
    
    /**
     * Search enabled agentspecs for runtime usage.
     */
    @Since("3.2.0")
    @GetMapping("/search")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API)
    @Operation(summary = "nacos.admin.ai.agentspec.client.api.search.summary",
        description = "nacos.admin.ai.agentspec.client.api.search.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.admin.ai.agentspec.client.api.search.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "keyword", example = "my-agentspec"),
        @Parameter(name = "pageNo", required = true, schema = @Schema(type = "integer"),
            example = "1"),
        @Parameter(name = "pageSize", required = true, schema = @Schema(type = "integer"),
            example = "100"),
        @Parameter(name = "form", hidden = true), @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<AgentSpecBasicInfo>> search(AgentSpecSearchForm form, PageForm pageForm)
        throws NacosException {
        form.validate();
        pageForm.validate();
        return Result.success(
            agentSpecOperationService.searchAgentSpecs(form.getNamespaceId(), form.getKeyword(),
                pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * Get an online agentspec version by label/version/latest.
     * Supports MD5-based conditional query: returns 304 when the client cache is fresh.
     */
    @Since("3.2.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API,
        parser = AgentSpecNameHttpResourceParser.class, tags = {ALLOW_ANONYMOUS})
    @Operation(summary = "nacos.admin.ai.agentspec.client.api.get.summary",
        description = "nacos.admin.ai.agentspec.client.api.get.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            headers = {
                @Header(name = "ETag", schema = @Schema(type = "string")),
                @Header(name = "X-Nacos-AgentSpec-Md5", schema = @Schema(type = "string")),
                @Header(name = "X-Nacos-AgentSpec-Resolved-Version",
                    schema = @Schema(type = "string"))},
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = Result.class,
                    example = "nacos.admin.ai.agentspec.client.api.get.example"))),
        @ApiResponse(responseCode = "304", description = "Not Modified",
            headers = {@Header(name = "ETag", schema = @Schema(type = "string")),
                @Header(name = "X-Nacos-AgentSpec-Md5",
                    schema = @Schema(type = "string"))},
            content = @Content)})
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "name", required = true, example = "my-agentspec"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "label"),
        @Parameter(name = "md5", example = "d41d8cd98f00b204e9800998ecf8427e"),
        @Parameter(name = "form", hidden = true)})
    public ResponseEntity<Result<AgentSpec>> get(AgentSpecQueryForm form)
        throws NacosException {
        form.validate();
        AgentSpecQueryResult result =
            agentSpecOperationService.queryAgentSpecForClient(
                form.getNamespaceId(), form.getName(), form.getVersion(),
                form.getLabel(), form.getMd5());
        if (result.isNotModified()) {
            return AgentSpecRequestUtil.buildAgentSpecNotModifiedResponse(result.getMd5());
        }
        return AgentSpecRequestUtil.buildAgentSpecResponse(result.getAgentSpec(),
            result.getMd5(), result.getResolvedVersion());
    }
}
