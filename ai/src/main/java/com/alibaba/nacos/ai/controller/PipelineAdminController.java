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

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.pipeline.PipelineListForm;
import com.alibaba.nacos.ai.pipeline.model.PipelineExecution;
import com.alibaba.nacos.ai.service.pipeline.PipelineQueryService;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pipeline Admin Controller for querying pipeline execution records.
 *
 * @author kiro
 * @since 3.2.0
 */
@NacosApi
@RestController
@RequestMapping(Constants.Pipeline.ADMIN_PATH)
@Tag(name = "nacos.admin.ai.pipeline.api.controller.name", description = "nacos.admin.ai.pipeline.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
                properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class PipelineAdminController {
    
    private final PipelineQueryService pipelineQueryService;
    
    public PipelineAdminController(PipelineQueryService pipelineQueryService) {
        this.pipelineQueryService = pipelineQueryService;
    }
    
    /**
     * Get pipeline execution detail by ID.
     */
    @GetMapping("/{pipelineId}")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    @Operation(summary = "nacos.admin.ai.pipeline.api.get.summary", description = "nacos.admin.ai.pipeline.api.get.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.admin.ai.pipeline.api.get.example")))
    @Parameter(name = "pipelineId", required = true, example = "pipeline-id-example")
    public Result<PipelineExecution> getPipeline(@PathVariable String pipelineId) throws NacosException {
        return Result.success(pipelineQueryService.getPipeline(pipelineId));
    }
    
    /**
     * List pipeline executions with pagination.
     */
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    @Operation(summary = "nacos.admin.ai.pipeline.api.list.summary", description = "nacos.admin.ai.pipeline.api.list.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.admin.ai.pipeline.api.list.example")))
    @Parameters(value = {@Parameter(name = "resourceType", required = true, example = "skill"),
            @Parameter(name = "resourceName", example = "my-skill"),
            @Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "pageNo", required = true, example = "1"),
            @Parameter(name = "pageSize", required = true, example = "100"),
            @Parameter(name = "form", hidden = true), @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<PipelineExecution>> listPipelines(PipelineListForm form, PageForm pageForm)
            throws NacosException {
        form.validate();
        pageForm.validate();
        return Result.success(pipelineQueryService.listPipelines(form.getResourceType(), form.getResourceName(),
                form.getNamespaceId(), form.getVersion(), pageForm.getPageNo(), pageForm.getPageSize()));
    }
}
