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
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecBizTagsUpdateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecDraftCreateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecLabelsUpdateForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecListForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecOnlineForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecPublishForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecScopeForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecSubmitForm;
import com.alibaba.nacos.ai.form.agentspecs.admin.AgentSpecUpdateForm;
import com.alibaba.nacos.ai.param.AgentSpecHttpParamExtractor;
import com.alibaba.nacos.ai.utils.AgentSpecRequestUtil;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpec;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecMeta;
import com.alibaba.nacos.api.ai.model.agentspecs.AgentSpecSummary;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.console.proxy.ai.AgentSpecProxy;
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
import io.swagger.v3.oas.annotations.media.SchemaProperty;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Console AgentSpec Controller.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.AgentSpecs.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = AgentSpecHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.agentspec.api.controller.name", description = "nacos.console.ai.agentspec.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
                properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleAgentSpecController {
    
    private final AgentSpecProxy agentSpecProxy;
    
    public ConsoleAgentSpecController(AgentSpecProxy agentSpecProxy) {
        this.agentSpecProxy = agentSpecProxy;
    }
    
    /**
     * Get agentspec detail.
     *
     * @param form the agentspec form
     * @return result of the get operation
     * @throws NacosException if the operation fails
     */
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.get.summary", description = "nacos.console.ai.agentspec.api.get.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<AgentSpecMeta> getAgentSpec(AgentSpecForm form) throws NacosException {
        form.validate();
        return Result.success(agentSpecProxy.getAgentSpec(form));
    }
    
    /**
     * Get specific version detail of an agentspec for viewing or editing.
     *
     * @param form the agentspec form containing agentSpecName and version
     * @return full agentspec content for the specified version
     * @throws NacosException if the agentspec or version not found
     */
    @GetMapping("/version")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.get.version.summary", description = "nacos.console.ai.agentspec.api.get.version.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.get.version.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<AgentSpec> getAgentSpecVersion(AgentSpecForm form) throws NacosException {
        form.validate();
        return Result.success(agentSpecProxy.getAgentSpecVersion(form));
    }
    
    /**
     * Delete agentspec.
     *
     * @param form the agentspec form
     * @return result of the deletion operation
     * @throws NacosException if the operation fails
     */
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.delete.summary", description = "nacos.console.ai.agentspec.api.delete.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> deleteAgentSpec(AgentSpecForm form) throws NacosException {
        form.validate();
        agentSpecProxy.deleteAgentSpec(form);
        return Result.success("ok");
    }
    
    /**
     * List agentspecs with pagination.
     *
     * @param agentSpecListForm the list form
     * @param pageForm          the page form
     * @return result of the list operation
     * @throws NacosException if the operation fails
     */
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.list.summary", description = "nacos.console.ai.agentspec.api.list.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.list.example")))
    @Parameters(value = {@Parameter(name = "pageNo", required = true, example = "1"),
            @Parameter(name = "pageSize", required = true, example = "100"),
            @Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", example = "my-agentspec"),
            @Parameter(name = "search", example = "blur", description = "Search mode: accurate or blur"),
            @Parameter(name = "agentSpecListForm", hidden = true), @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<AgentSpecSummary>> listAgentSpecs(AgentSpecListForm agentSpecListForm, PageForm pageForm)
            throws NacosException {
        agentSpecListForm.validate();
        pageForm.validate();
        return Result.success(agentSpecProxy.listAgentSpecs(agentSpecListForm, pageForm));
    }
    
    /**
     * Upload agentspec from zip file.
     *
     * @param request     HTTP servlet request
     * @param namespaceId namespace ID
     * @param file        zip file containing agentspec
     * @return result of the upload operation
     * @throws NacosException if the upload fails
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    @Operation(summary = "nacos.console.ai.agentspec.api.upload.summary", description = "nacos.console.ai.agentspec.api.upload.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.upload.example")))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "nacos.console.ai.agentspec.api.upload.body.description", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schemaProperties = {
            @SchemaProperty(name = "namespaceId", schema = @Schema(type = "string", example = "public")),
            @SchemaProperty(name = "overwrite", schema = @Schema(type = "boolean", example = "false")),
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary", description = "ZIP file containing agentspec package"))}))
    public Result<String> uploadAgentSpec(HttpServletRequest request,
            @RequestParam(value = "namespaceId", required = false) String namespaceId,
            @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite,
            @RequestParam("file") MultipartFile file) throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        byte[] zipBytes = AgentSpecRequestUtil.validateAndExtractZipBytes(file);
        String agentSpecName = agentSpecProxy.uploadAgentSpecFromZip(namespaceId, zipBytes, overwrite);
        return Result.success(agentSpecName);
    }
    
    /**
     * Create draft version.
     *
     * @param form draft create form
     * @return created draft version
     * @throws NacosException if the operation fails
     */
    @PostMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.draft.create.summary", description = "nacos.console.ai.agentspec.api.draft.create.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.draft.create.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "basedOnVersion", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> createDraft(AgentSpecDraftCreateForm form) throws NacosException {
        form.validate();
        return Result.success(agentSpecProxy.createDraft(form));
    }
    
    /**
     * Update current draft content.
     *
     * @param form update form
     * @return result of the update operation
     * @throws NacosException if the operation fails
     */
    @PutMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.draft.update.summary", description = "nacos.console.ai.agentspec.api.draft.update.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.draft.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", example = "my-agentspec"),
            @Parameter(name = "agentSpecCard", required = true, description = "AgentSpec card JSON string containing complete AgentSpec information"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> updateDraft(AgentSpecUpdateForm form) throws NacosException {
        form.validate();
        agentSpecProxy.updateDraft(form);
        return Result.success("ok");
    }
    
    /**
     * Delete current draft version.
     *
     * @param form agentspec form
     * @return result of the deletion operation
     * @throws NacosException if the operation fails
     */
    @DeleteMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.draft.delete.summary", description = "nacos.console.ai.agentspec.api.draft.delete.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.draft.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> deleteDraft(AgentSpecForm form) throws NacosException {
        form.validate();
        agentSpecProxy.deleteDraft(form);
        return Result.success("ok");
    }
    
    /**
     * Submit a version for pipeline review.
     *
     * @param form submit form
     * @return submit result
     * @throws NacosException if the operation fails
     */
    @PostMapping("/submit")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.submit.summary", description = "nacos.console.ai.agentspec.api.submit.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.submit.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> submit(AgentSpecSubmitForm form) throws NacosException {
        form.validate();
        return Result.success(agentSpecProxy.submit(form));
    }
    
    /**
     * Publish an approved reviewing version.
     *
     * @param form publish form
     * @return result of the publish operation
     * @throws NacosException if the operation fails
     */
    @PostMapping("/publish")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.publish.summary", description = "nacos.console.ai.agentspec.api.publish.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "version", required = true, example = "1.0.0"),
            @Parameter(name = "updateLatestLabel", example = "true"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> publish(AgentSpecPublishForm form) throws NacosException {
        form.validate();
        agentSpecProxy.publish(form);
        return Result.success("ok");
    }
    
    /**
     * Update runtime route labels.
     *
     * @param form labels update form
     * @return result of the update operation
     * @throws NacosException if the operation fails
     */
    @PutMapping("/labels")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.labels.update.summary", description = "nacos.console.ai.agentspec.api.labels.update.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.labels.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "labels", required = true, example = "{\"latest\":\"v1.0.0\"}"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> updateLabels(AgentSpecLabelsUpdateForm form) throws NacosException {
        form.validate();
        agentSpecProxy.updateLabels(form);
        return Result.success("ok");
    }

    /**
     * Update agentspec biz tags without changing version status.
     */
    @PutMapping("/biz-tags")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.biz.tags.update.summary", description = "nacos.console.ai.agentspec.api.biz.tags.update.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.biz.tags.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "bizTags", required = true, example = "[\"tag1\",\"tag2\"]"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> updateBizTags(AgentSpecBizTagsUpdateForm form) throws NacosException {
        form.validate();
        agentSpecProxy.updateBizTags(form);
        return Result.success("ok");
    }
    
    /**
     * Online operation.
     *
     * @param form online form
     * @return result of the operation
     * @throws NacosException if the operation fails
     */
    @PostMapping("/online")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.online.summary", description = "nacos.console.ai.agentspec.api.online.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.online.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "scope", example = "agentspec", description = "Use 'agentspec' for agentspec-level online; otherwise version-level"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> online(AgentSpecOnlineForm form) throws NacosException {
        form.validate();
        agentSpecProxy.online(form);
        return Result.success("ok");
    }

    /**
     * Update agentspec visibility scope.
     *
     * @param form scope update form
     * @return result of the update operation
     * @throws NacosException if the operation fails
     */
    @PutMapping("/scope")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.scope.update.summary", description = "nacos.console.ai.agentspec.api.scope.update.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.scope.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "scope", required = true, example = "PUBLIC", description = "PUBLIC or PRIVATE"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> updateScope(AgentSpecScopeForm form) throws NacosException {
        form.validate();
        agentSpecProxy.updateScope(form);
        return Result.success("ok");
    }
    
    /**
     * Offline operation.
     *
     * @param form online form
     * @return result of the operation
     * @throws NacosException if the operation fails
     */
    @PostMapping("/offline")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agentspec.api.offline.summary", description = "nacos.console.ai.agentspec.api.offline.description", security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Result.class, example = "nacos.console.ai.agentspec.api.offline.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "agentSpecName", required = true, example = "my-agentspec"),
            @Parameter(name = "scope", example = "agentspec", description = "Use 'agentspec' for agentspec-level offline; otherwise version-level"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> offline(AgentSpecOnlineForm form) throws NacosException {
        form.validate();
        agentSpecProxy.offline(form);
        return Result.success("ok");
    }
}
