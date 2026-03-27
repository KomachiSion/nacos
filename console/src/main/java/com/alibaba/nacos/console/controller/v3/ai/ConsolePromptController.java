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

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.prompt.PromptForm;
import com.alibaba.nacos.ai.form.prompt.PromptHistoryForm;
import com.alibaba.nacos.ai.form.prompt.PromptLabelBindForm;
import com.alibaba.nacos.ai.form.prompt.PromptLabelForm;
import com.alibaba.nacos.ai.form.prompt.PromptListForm;
import com.alibaba.nacos.ai.form.prompt.PromptMetadataForm;
import com.alibaba.nacos.ai.form.prompt.PromptPublishForm;
import com.alibaba.nacos.ai.form.prompt.PromptQueryForm;
import com.alibaba.nacos.ai.param.PromptHttpParamExtractor;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptMetaSummary;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionInfo;
import com.alibaba.nacos.api.ai.model.prompt.PromptVersionSummary;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.proxy.ai.PromptProxy;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Console prompt controller.
 *
 * <p>Provides REST APIs for prompt management operations in console.</p>
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Prompt.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = PromptHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.prompt.api.controller.name", description = "nacos.console.ai.prompt.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE, properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsolePromptController {
    
    private final PromptProxy promptProxy;
    
    public ConsolePromptController(PromptProxy promptProxy) {
        this.promptProxy = promptProxy;
    }
    
    /**
     * Publish a new version of prompt.
     *
     * @param form    the prompt publish form
     * @param request HTTP request for getting client info
     * @return result of the publish operation
     * @throws NacosException if the prompt publish fails
     */
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.publish.summary", description = "nacos.console.ai.prompt.api.publish.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "version", required = true, example = "1.0.0"),
            @Parameter(name = "template", required = true), @Parameter(name = "commitMsg"),
            @Parameter(name = "description"), @Parameter(name = "bizTags"),
            @Parameter(name = "form", hidden = true)})
    public Result<Boolean> publishPrompt(PromptPublishForm form, HttpServletRequest request) throws NacosException {
        form.validate();
        String srcUser = request.getRemoteUser();
        String srcIp = request.getRemoteAddr();
        boolean success = promptProxy.publishPrompt(form, srcUser, srcIp);
        return Result.success(success);
    }
    
    @GetMapping("/metadata")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.metadata.summary", description = "nacos.console.ai.prompt.api.metadata.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.metadata.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "form", hidden = true)})
    public Result<PromptMetaInfo> getPromptMeta(PromptForm form) throws NacosException {
        form.validate();
        PromptMetaInfo detail = promptProxy.getPromptMeta(form);
        return Result.success(detail);
    }
    
    /**
     * Query prompt detail by version/label/latest with priority version > label > latest.
     */
    @GetMapping("/detail")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.detail.summary", description = "nacos.console.ai.prompt.api.detail.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.detail.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "version", example = "1.0.0"), @Parameter(name = "label"), @Parameter(name = "md5"),
            @Parameter(name = "form", hidden = true)})
    public Result<PromptVersionInfo> queryPromptDetail(PromptQueryForm form) throws NacosException {
        form.validate();
        PromptVersionInfo detail = promptProxy.queryPromptDetail(form);
        return Result.success(detail);
    }
    
    /**
     * Bind label to a specified prompt version.
     */
    @PutMapping("/label")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.label.bind.summary", description = "nacos.console.ai.prompt.api.label.bind.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.label.bind.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "label", required = true, example = "stable"),
            @Parameter(name = "version", required = true, example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<Boolean> bindLabel(PromptLabelBindForm form, HttpServletRequest request) throws NacosException {
        form.validate();
        String srcUser = request.getRemoteUser();
        String srcIp = request.getRemoteAddr();
        boolean success = promptProxy.bindLabel(form, srcUser, srcIp);
        return Result.success(success);
    }
    
    /**
     * Unbind label from prompt.
     */
    @DeleteMapping("/label")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.label.unbind.summary", description = "nacos.console.ai.prompt.api.label.unbind.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.label.unbind.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "label", required = true, example = "stable"),
            @Parameter(name = "form", hidden = true)})
    public Result<Boolean> unbindLabel(PromptLabelForm form, HttpServletRequest request) throws NacosException {
        form.validate();
        String srcUser = request.getRemoteUser();
        String srcIp = request.getRemoteAddr();
        boolean success = promptProxy.unbindLabel(form, srcUser, srcIp);
        return Result.success(success);
    }
    
    /**
     * Delete prompt.
     *
     * @param form    the prompt form
     * @param request HTTP request for getting client info
     * @return result of the deletion operation
     * @throws NacosException if the prompt deletion fails
     */
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.delete.summary", description = "nacos.console.ai.prompt.api.delete.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "form", hidden = true)})
    public Result<Boolean> deletePrompt(PromptForm form, HttpServletRequest request) throws NacosException {
        form.validate();
        String srcUser = request.getRemoteUser();
        String srcIp = request.getRemoteAddr();
        boolean success = promptProxy.deletePrompt(form, srcUser, srcIp);
        return Result.success(success);
    }
    
    /**
     * List prompts with pagination.
     *
     * @param form the prompt list form
     * @return result of the list operation
     * @throws NacosException if the prompt list fails
     */
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.list.summary", description = "nacos.console.ai.prompt.api.list.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.list.example")))
    @Parameters(value = {@Parameter(name = "pageNo", required = true, example = "1"),
            @Parameter(name = "pageSize", required = true, example = "10"),
            @Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey"), @Parameter(name = "search", example = "blur", description = "blur or accurate"),
            @Parameter(name = "bizTags"), @Parameter(name = "form", hidden = true)})
    public Result<Page<PromptMetaSummary>> listPrompts(PromptListForm form) throws NacosException {
        form.validate();
        Page<PromptMetaSummary> result = promptProxy.listPrompts(form);
        return Result.success(result);
    }
    
    /**
     * List prompt versions with pagination.
     *
     * @param form the prompt history form
     * @return result of the version list operation
     * @throws NacosException if the version list fails
     */
    @GetMapping("/versions")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.versions.summary", description = "nacos.console.ai.prompt.api.versions.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.versions.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "pageNo", required = true, example = "1"),
            @Parameter(name = "pageSize", required = true, example = "10"),
            @Parameter(name = "form", hidden = true)})
    public Result<Page<PromptVersionSummary>> listPromptVersions(PromptHistoryForm form) throws NacosException {
        form.validate();
        Page<PromptVersionSummary> result = promptProxy.listPromptVersions(form);
        return Result.success(result);
    }
    
    /**
     * Update prompt metadata (description only).
     *
     * @param form    the prompt metadata form
     * @param request HTTP request for getting client info
     * @return result of the update operation
     * @throws NacosException if the update fails
     */
    @PutMapping("/metadata")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.prompt.api.metadata.update.summary", description = "nacos.console.ai.prompt.api.metadata.update.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.prompt.api.metadata.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "promptKey", required = true, example = "my-prompt"),
            @Parameter(name = "description"), @Parameter(name = "bizTags"),
            @Parameter(name = "form", hidden = true)})
    public Result<Boolean> updatePromptMetadata(PromptMetadataForm form, HttpServletRequest request)
            throws NacosException {
        form.validate();
        String srcUser = request.getRemoteUser();
        String srcIp = request.getRemoteAddr();
        boolean success = promptProxy.updatePromptMetadata(form, srcUser, srcIp);
        return Result.success(success);
    }
}
