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

package com.alibaba.nacos.console.controller.v3.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.importer.AiResourceImportExecuteForm;
import com.alibaba.nacos.ai.form.importer.AiResourceImportSearchForm;
import com.alibaba.nacos.ai.form.importer.AiResourceImportSourceListForm;
import com.alibaba.nacos.ai.form.importer.AiResourceImportValidateForm;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportExecuteResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSearchResponse;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportSourceInfo;
import com.alibaba.nacos.api.ai.model.importer.AiResourceImportValidateResponse;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.proxy.ai.AiResourceImportProxy;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Console API controller for AI resource import.
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
@NacosApi
@RestController
@RequestMapping(Constants.AI_RESOURCE_IMPORT_CONSOLE_PATH)
@Tag(name = "nacos.console.ai.import.api.controller.name",
    description = "nacos.console.ai.import.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleAiResourceImportController {
    
    private final AiResourceImportProxy importProxy;
    
    public ConsoleAiResourceImportController(AiResourceImportProxy importProxy) {
        this.importProxy = importProxy;
    }
    
    /**
     * List enabled managed importer plugins as import sources for Console.
     *
     * @param form source list form
     * @return source list
     * @throws NacosException if source configuration is invalid
     */
    @Since("3.2.2")
    @GetMapping("/sources")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.import.api.sources.summary",
        description = "nacos.console.ai.import.api.sources.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.import.api.sources.example")))
    @Parameters(value = {@Parameter(name = "resourceType", example = "mcp"),
        @Parameter(name = "form", hidden = true)})
    public Result<List<AiResourceImportSourceInfo>> listSources(
        AiResourceImportSourceListForm form) throws NacosException {
        form.validate();
        return Result.success(importProxy.listSources(form.getResourceType()));
    }
    
    /**
     * Search external import candidates for Console.
     *
     * @param form search form
     * @return candidate page
     * @throws NacosException if the source cannot be searched
     */
    @Since("3.2.2")
    @PostMapping("/search")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.import.api.search.summary",
        description = "nacos.console.ai.import.api.search.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.import.api.search.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "resourceType", required = true, example = "mcp"),
        @Parameter(name = "sourceId", required = true, example = "github"),
        @Parameter(name = "query", example = "nacos"),
        @Parameter(name = "cursor"),
        @Parameter(name = "limit", schema = @Schema(type = "integer"), example = "20"),
        @Parameter(name = "options", schema = @Schema(type = "string",
            description = "JSON object string parsed as import options"),
            example = "\"{\\\"owner\\\":\\\"alibaba\\\"}\""),
        @Parameter(name = "form", hidden = true)})
    public Result<AiResourceImportSearchResponse> search(AiResourceImportSearchForm form)
        throws NacosException {
        form.validate();
        return Result.success(importProxy.search(form.toRequest()));
    }
    
    /**
     * Validate selected import candidates for Console.
     *
     * @param form validate form
     * @return validation result
     * @throws NacosException if validation cannot start
     */
    @Since("3.2.2")
    @PostMapping("/validate")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.import.api.validate.summary",
        description = "nacos.console.ai.import.api.validate.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.import.api.validate.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "resourceType", required = true, example = "mcp"),
        @Parameter(name = "sourceId", required = true, example = "github"),
        @Parameter(name = "selectedItems", required = true, schema = @Schema(type = "string",
            description = "JSON array string parsed as selected import items"),
            example = "\"[{\\\"id\\\":\\\"demo\\\"}]\""),
        @Parameter(name = "overwriteExisting", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "options", schema = @Schema(type = "string",
            description = "JSON object string parsed as import options"),
            example = "\"{\\\"owner\\\":\\\"alibaba\\\"}\""),
        @Parameter(name = "form", hidden = true)})
    public Result<AiResourceImportValidateResponse> validate(AiResourceImportValidateForm form)
        throws NacosException {
        form.validate();
        return Result.success(importProxy.validate(form.toRequest()));
    }
    
    /**
     * Execute import for selected candidates from Console.
     *
     * @param form execute form
     * @return import result
     * @throws NacosException if import cannot start
     */
    @Since("3.2.2")
    @PostMapping("/execute")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.import.api.execute.summary",
        description = "nacos.console.ai.import.api.execute.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.import.api.execute.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "resourceType", required = true, example = "mcp"),
        @Parameter(name = "sourceId", required = true, example = "github"),
        @Parameter(name = "selectedItems", required = true, schema = @Schema(type = "string",
            description = "JSON array string parsed as selected import items"),
            example = "\"[{\\\"id\\\":\\\"demo\\\"}]\""),
        @Parameter(name = "overwriteExisting", schema = @Schema(type = "boolean"),
            example = "false"),
        @Parameter(name = "skipInvalid", schema = @Schema(type = "boolean"), example = "false"),
        @Parameter(name = "validationToken", example = "validation-token"),
        @Parameter(name = "options", schema = @Schema(type = "string",
            description = "JSON object string parsed as import options"),
            example = "\"{\\\"owner\\\":\\\"alibaba\\\"}\""),
        @Parameter(name = "form", hidden = true)})
    public Result<AiResourceImportExecuteResponse> execute(AiResourceImportExecuteForm form)
        throws NacosException {
        form.validate();
        return Result.success(importProxy.execute(form.toRequest()));
    }
}
