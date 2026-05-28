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
import com.alibaba.nacos.ai.form.AiResourceFilterableForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillBizTagsUpdateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillDraftCreateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillLabelsUpdateForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillListForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillOnlineForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillPublishForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillScopeForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillSubmitForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillUpdateForm;
import com.alibaba.nacos.api.ai.model.skills.BatchUploadResult;
import com.alibaba.nacos.ai.param.SkillHttpParamExtractor;
import com.alibaba.nacos.ai.service.skills.SkillUploadRequest;
import com.alibaba.nacos.ai.utils.SkillRequestUtil;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillMeta;
import com.alibaba.nacos.api.ai.model.skills.SkillSummary;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.console.proxy.ai.SkillProxy;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.web.multipart.MultipartFile;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Resource.CONSOLE_RESOURCE_NAME_PREFIX;

/**
 * Console skill controller.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Skills.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = SkillHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.skill.api.controller.name",
    description = "nacos.console.ai.skill.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleSkillController {
    
    private final SkillProxy skillProxy;
    
    public ConsoleSkillController(SkillProxy skillProxy) {
        this.skillProxy = skillProxy;
    }
    
    /**
     * Get skill.
     *
     * @param form the skill form to get
     * @return result of the get operation
     * @throws NacosException if the skill get fails
     */
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.get.summary",
        description = "nacos.console.ai.skill.api.get.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", example = "1.0.0"), @Parameter(name = "form", hidden = true)})
    public Result<SkillMeta> getSkill(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.getSkill(form));
    }
    
    /**
     * Get specific version detail of a skill for viewing or editing.
     *
     * @param form the skill form containing skillName and version
     * @return full skill content for the specified version
     * @throws NacosException if the skill or version not found
     */
    @GetMapping("/version")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.get.version.summary",
        description = "nacos.console.ai.skill.api.get.version.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.get.version.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<Skill> getSkillVersion(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.getSkillVersion(form));
    }
    
    /**
     * Download a specific version of a skill as ZIP file.
     *
     * @param form the skill form containing skillName and version
     * @return ZIP file as ResponseEntity
     * @throws NacosException if the skill or version not found
     */
    @GetMapping("/version/download")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.download.version.summary",
        description = "nacos.console.ai.skill.api.download.version.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            schema = @Schema(type = "string", format = "binary",
                description = "ZIP file containing the skill package")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public ResponseEntity<byte[]> downloadSkillVersion(SkillForm form) throws NacosException {
        form.validate();
        Skill skill = skillProxy.downloadSkillVersion(form);
        return SkillRequestUtil.buildSkillZipResponse(skill);
    }
    
    /**
     * Delete skill.
     *
     * @param form the skill form to delete
     * @return result of the deletion operation
     * @throws NacosException if the skill deletion fails
     */
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.delete.summary",
        description = "nacos.console.ai.skill.api.delete.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", example = "1.0.0"), @Parameter(name = "form", hidden = true)})
    public Result<String> deleteSkill(SkillForm form) throws NacosException {
        form.validate();
        skillProxy.deleteSkill(form);
        return Result.success("ok");
    }
    
    /**
     * List skills.
     *
     * @param skillListForm the skill list form to list
     * @param pageForm      the page form to list
     * @return result of the list operation
     * @throws NacosException if the skill list fails
     */
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.list.summary",
        description = "nacos.console.ai.skill.api.list.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.list.example")))
    @Parameters(value = {
        @Parameter(name = "pageNo", required = true, schema = @Schema(type = "integer"),
            example = "1"),
        @Parameter(name = "pageSize", required = true, schema = @Schema(type = "integer"),
            example = "100"),
        @Parameter(name = "namespaceId", example = "public"), @Parameter(name = "skillName"),
        @Parameter(name = "search", example = "blur", description = "blur or accurate"),
        @Parameter(name = "skillListForm", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<SkillSummary>> listSkills(SkillListForm skillListForm,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException {
        skillListForm.validate();
        filterableForm.validate();
        pageForm.validate();
        return Result.success(skillProxy.listSkills(skillListForm, filterableForm, pageForm));
    }
    
    /**
     * Upload skill from zip file.
     *
     * @param request     HTTP servlet request
     * @param namespaceId namespace ID
     * @param commitMsg   version-level commit message
     * @param file        zip file containing skill
     * @return result of the upload operation
     * @throws NacosException if the upload fails
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    @Operation(summary = "nacos.console.ai.skill.api.upload.summary",
        description = "nacos.console.ai.skill.api.upload.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.upload.example")))
    @RequestBody(description = "nacos.console.ai.skill.api.upload.body.description",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schemaProperties = {
            @SchemaProperty(name = "file",
                schema = @Schema(type = "string", format = "binary",
                    description = "ZIP file containing skill")),
            @SchemaProperty(name = "overwrite",
                schema = @Schema(type = "boolean", example = "false")),
            @SchemaProperty(name = "namespaceId",
                schema = @Schema(type = "string", example = "public")),
            @SchemaProperty(name = "targetVersion",
                schema = @Schema(type = "string", example = "1.0.0")),
            @SchemaProperty(name = "commitMsg",
                schema = @Schema(type = "string", example = "Initial version"))}))
    public Result<String> uploadSkill(HttpServletRequest request,
        @RequestParam(value = "namespaceId", required = false) String namespaceId,
        @RequestParam(value = "overwrite", required = false,
            defaultValue = "false") boolean overwrite,
        @RequestParam(value = "targetVersion", required = false) String targetVersion,
        @RequestParam(value = "commitMsg", required = false) String commitMsg,
        @RequestParam("file") MultipartFile file) throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        byte[] zipBytes = SkillRequestUtil.validateAndExtractZipBytes(file);
        SkillUploadRequest uploadRequest = SkillUploadRequest.builder()
            .namespaceId(namespaceId)
            .zipBytes(zipBytes)
            .overwrite(overwrite)
            .targetVersion(targetVersion)
            .commitMsg(commitMsg)
            .build();
        String skillName = skillProxy.uploadSkillFromZip(uploadRequest);
        return Result.success(skillName);
    }
    
    /**
     * Batch upload multiple skills from a single zip file. The zip must contain one-level subdirectories,
     * each with its own SKILL.md. Uses best-effort strategy.
     *
     * @param request     HTTP servlet request
     * @param namespaceId namespace ID
     * @param overwrite   whether to overwrite existing drafts
     * @param file        zip file containing multiple skill subdirectories
     * @return batch upload result with succeeded and failed lists
     * @throws NacosException if zip parsing fails entirely
     */
    @PostMapping(value = "/upload/batch", consumes = "multipart/form-data")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    @Operation(summary = "nacos.console.ai.skill.api.upload.batch.summary",
        description = "nacos.console.ai.skill.api.upload.batch.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.2"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.upload.batch.example")))
    @RequestBody(description = "nacos.console.ai.skill.api.upload.batch.body.description",
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schemaProperties = {
            @SchemaProperty(name = "namespaceId",
                schema = @Schema(type = "string", example = "public")),
            @SchemaProperty(name = "overwrite",
                schema = @Schema(type = "boolean", example = "false")),
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary",
                description = "ZIP file containing skill directories"))}))
    public Result<BatchUploadResult> batchUploadSkills(HttpServletRequest request,
        @RequestParam(value = "namespaceId", required = false) String namespaceId,
        @RequestParam(value = "overwrite", required = false,
            defaultValue = "false") boolean overwrite,
        @RequestParam("file") MultipartFile file) throws NacosException {
        namespaceId = NamespaceUtil.processNamespaceParameter(namespaceId);
        byte[] zipBytes = SkillRequestUtil.validateAndExtractZipBytes(file);
        BatchUploadResult result =
            skillProxy.batchUploadSkillsFromZip(namespaceId, zipBytes, overwrite);
        return Result.success(result);
    }
    
    /**
     * Create draft. {@link SkillDraftCreateForm#prepareCreateDraftRequest()} validates here; handler only delegates.
     */
    @PostMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.draft.create.summary",
        description = "nacos.console.ai.skill.api.draft.create.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.draft.create.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", example = "my-skill"),
        @Parameter(name = "basedOnVersion", example = "1.0.0"),
        @Parameter(name = "targetVersion", example = "1.1.0"),
        @Parameter(name = "skillCard",
            description = "Skill card JSON; required if basedOnVersion is not set"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> createDraft(SkillDraftCreateForm form) throws NacosException {
        form.prepareCreateDraftRequest();
        return Result.success(skillProxy.createDraft(form));
    }
    
    /**
     * Update current draft content.
     */
    @PutMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.draft.update.summary",
        description = "nacos.console.ai.skill.api.draft.update.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.draft.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "skillCard", required = true,
            description = "Skill card JSON string containing complete Skill information"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateDraft(SkillUpdateForm form) throws NacosException {
        form.validate();
        skillProxy.updateDraft(form);
        return Result.success("ok");
    }
    
    /**
     * Delete current draft version.
     */
    @DeleteMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.draft.delete.summary",
        description = "nacos.console.ai.skill.api.draft.delete.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.draft.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> deleteDraft(SkillForm form) throws NacosException {
        form.validate();
        skillProxy.deleteDraft(form);
        return Result.success("ok");
    }
    
    /**
     * Submit a version for pipeline review.
     */
    @PostMapping("/submit")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.submit.summary",
        description = "nacos.console.ai.skill.api.submit.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.submit.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> submit(SkillSubmitForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.submit(form));
    }
    
    /**
     * Publish an approved reviewing version.
     */
    @PostMapping("/publish")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.publish.summary",
        description = "nacos.console.ai.skill.api.publish.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "updateLatestLabel", schema = @Schema(type = "boolean"),
            example = "true"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> publish(SkillPublishForm form) throws NacosException {
        form.validate();
        skillProxy.publish(form);
        return Result.success("ok");
    }
    
    /**
     * Force-publish a skill version, bypassing pipeline validation. Accepts draft (pipeline-rejected) and reviewing
     * (pipeline in-progress) versions. Restricted to admin users only (apiType = ADMIN_API enforces global admin
     * check).
     */
    @PostMapping("/force-publish")
    @Secured(resource = CONSOLE_RESOURCE_NAME_PREFIX
        + "skills", action = ActionTypes.WRITE, signType = SignType.CONSOLE,
        apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.force.publish.summary",
        description = "nacos.console.ai.skill.api.force.publish.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.1"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.force.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "updateLatestLabel", schema = @Schema(type = "boolean"),
            example = "true"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> forcePublish(SkillPublishForm form) throws NacosException {
        form.validate();
        skillProxy.forcePublish(form);
        return Result.success("ok");
    }
    
    /**
     * Re-edit a reviewed skill version, transitioning it back to draft status.
     */
    @PostMapping("/redraft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.redraft.summary",
        description = "nacos.console.ai.skill.api.redraft.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.2"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.redraft.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> redraft(SkillPublishForm form) throws NacosException {
        form.validate();
        skillProxy.redraft(form);
        return Result.success("ok");
    }
    
    /**
     * Update runtime route labels without changing version status.
     */
    @PutMapping("/labels")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.labels.update.summary",
        description = "nacos.console.ai.skill.api.labels.update.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.labels.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "labels", required = true, schema = @Schema(type = "string",
            description = "JSON object string parsed as runtime labels"),
            example = "\"{\\\"latest\\\":\\\"v1.0.0\\\"}\""),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateLabels(SkillLabelsUpdateForm form) throws NacosException {
        form.validate();
        skillProxy.updateLabels(form);
        return Result.success("ok");
    }
    
    /**
     * Update skill biz tags without changing version status.
     */
    @PutMapping("/biz-tags")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.biz.tags.update.summary",
        description = "nacos.console.ai.skill.api.biz.tags.update.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.biz.tags.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "bizTags", required = true, schema = @Schema(type = "string",
            description = "JSON array string parsed as biz tags"),
            example = "\"[\\\"tag1\\\",\\\"tag2\\\"]\""),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateBizTags(SkillBizTagsUpdateForm form) throws NacosException {
        form.validate();
        skillProxy.updateBizTags(form);
        return Result.success("ok");
    }
    
    /**
     * Online operation (version-level or skill-level by scope).
     */
    @PostMapping("/online")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.online.summary",
        description = "nacos.console.ai.skill.api.online.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.online.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "scope", example = "skill",
            description = "Use 'skill' for skill-level online; otherwise version-level"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> online(SkillOnlineForm form) throws NacosException {
        form.validate();
        skillProxy.online(form);
        return Result.success("ok");
    }
    
    /**
     * Offline operation (version-level or skill-level by scope).
     */
    @PostMapping("/offline")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.offline.summary",
        description = "nacos.console.ai.skill.api.offline.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.offline.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "scope", example = "skill",
            description = "Use 'skill' for skill-level offline; otherwise version-level"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> offline(SkillOnlineForm form) throws NacosException {
        form.validate();
        skillProxy.offline(form);
        return Result.success("ok");
    }
    
    /**
     * Update skill visibility scope (PUBLIC or PRIVATE).
     */
    @PutMapping("/scope")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.scope.update.summary",
        description = "nacos.console.ai.skill.api.scope.update.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.skill.api.scope.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "skillName", required = true, example = "my-skill"),
        @Parameter(name = "scope", required = true, example = "PUBLIC",
            description = "PUBLIC or PRIVATE"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateScope(SkillScopeForm form) throws NacosException {
        form.validate();
        skillProxy.updateScope(form);
        return Result.success("ok");
    }
}
