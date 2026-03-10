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
import com.alibaba.nacos.ai.form.skills.admin.SkillDetailForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillListForm;
import com.alibaba.nacos.ai.form.skills.admin.SkillUpdateForm;
import com.alibaba.nacos.ai.param.SkillHttpParamExtractor;
import com.alibaba.nacos.ai.utils.SkillRequestUtil;
import com.alibaba.nacos.console.proxy.ai.SkillProxy;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillBasicInfo;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.core.model.form.PageForm;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.ApiType;
import com.alibaba.nacos.plugin.auth.constant.SignType;
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
import jakarta.servlet.http.HttpServletRequest;

/**
 * Console skill controller.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Skills.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = SkillHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.skill.api.controller.name", description = "nacos.console.ai.skill.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE, properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleSkillController {
    
    private final SkillProxy skillProxy;
    
    public ConsoleSkillController(SkillProxy skillProxy) {
        this.skillProxy = skillProxy;
    }
    
    /**
     * Register skill.
     *
     * @param form the skill detail form to register
     * @return result of the registration operation
     * @throws NacosException if the skill registration fails
     */
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.register.summary", description = "nacos.console.ai.skill.api.register.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.skill.api.register.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "skillCard", required = true, description = "Skill card JSON"),
            @Parameter(name = "skillName"), @Parameter(name = "version"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> registerSkill(SkillDetailForm form) throws NacosException {
        form.validate();
        Skill skill = SkillRequestUtil.parseSkill(form);
        skillProxy.registerSkill(skill, form);
        return Result.success("ok");
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
    @Operation(summary = "nacos.console.ai.skill.api.get.summary", description = "nacos.console.ai.skill.api.get.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.skill.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "skillName", required = true, example = "my-skill"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<Skill> getSkill(SkillForm form) throws NacosException {
        form.validate();
        return Result.success(skillProxy.getSkill(form));
    }
    
    /**
     * Update skill.
     *
     * @param form the skill update form to update
     * @return result of the update operation
     * @throws NacosException if the skill update fails
     */
    @PutMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.update.summary", description = "nacos.console.ai.skill.api.update.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.skill.api.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "skillCard", required = true, description = "Skill card JSON"),
            @Parameter(name = "skillName"), @Parameter(name = "version"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> updateSkill(SkillUpdateForm form) throws NacosException {
        form.validate();
        Skill skill = SkillRequestUtil.parseSkill(form);
        skillProxy.updateSkill(skill, form);
        return Result.success("ok");
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
    @Operation(summary = "nacos.console.ai.skill.api.delete.summary", description = "nacos.console.ai.skill.api.delete.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.skill.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "skillName", required = true, example = "my-skill"),
            @Parameter(name = "version", example = "1.0.0"),
            @Parameter(name = "form", hidden = true)})
    public Result<String> deleteSkill(SkillForm form) throws NacosException {
        form.validate();
        skillProxy.deleteSkill(form);
        return Result.success("ok");
    }
    
    /**
     * List skills.
     *
     * @param skillListForm the skill list form to list
     * @param pageForm the page form to list
     * @return result of the list operation
     * @throws NacosException if the skill list fails
     */
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.skill.api.list.summary", description = "nacos.console.ai.skill.api.list.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.skill.api.list.example")))
    @Parameters(value = {@Parameter(name = "pageNo", required = true, example = "1"),
            @Parameter(name = "pageSize", required = true, example = "100"),
            @Parameter(name = "namespaceId", example = "public"),
            @Parameter(name = "skillName"),
            @Parameter(name = "search", example = "blur", description = "blur or accurate"),
            @Parameter(name = "skillListForm", hidden = true), @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<SkillBasicInfo>> listSkills(SkillListForm skillListForm, PageForm pageForm)
            throws NacosException {
        skillListForm.validate();
        pageForm.validate();
        return Result.success(skillProxy.listSkills(skillListForm, pageForm));
    }
    
    /**
     * Upload skill from zip file.
     *
     * @param request HTTP servlet request
     * @param namespaceId namespace ID
     * @param file zip file containing skill
     * @return result of the upload operation
     * @throws NacosException if the upload fails
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @ExtractorManager.Extractor(httpExtractor = ExtractorManager.DefaultHttpExtractor.class)
    @Operation(summary = "nacos.console.ai.skill.api.upload.summary", description = "nacos.console.ai.skill.api.upload.description",
            security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class, example = "nacos.console.ai.skill.api.upload.example")))
    @RequestBody(description = "nacos.console.ai.skill.api.upload.body.description", content = @Content(
            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schemaProperties = {
                    @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary", description = "ZIP file containing skill")),
                    @SchemaProperty(name = "namespaceId", schema = @Schema(type = "string", example = "public"))
            }))
    public Result<String> uploadSkill(HttpServletRequest request,
            @RequestParam(value = "namespaceId", required = false) String namespaceId,
            @RequestParam("file") MultipartFile file) throws NacosException {
        byte[] zipBytes = SkillRequestUtil.validateAndExtractZipBytes(file);
        String skillName = skillProxy.uploadSkillFromZip(namespaceId, zipBytes);
        return Result.success(skillName);
    }
}
