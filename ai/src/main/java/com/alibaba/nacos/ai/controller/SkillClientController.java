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
import com.alibaba.nacos.ai.form.skills.client.SkillQueryForm;
import com.alibaba.nacos.ai.param.SkillHttpParamExtractor;
import com.alibaba.nacos.ai.service.skills.SkillOperationService;
import com.alibaba.nacos.ai.utils.SkillRequestUtil;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.auth.annotation.Secured;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.alibaba.nacos.plugin.auth.constant.Constants.Tag.ALLOW_ANONYMOUS;

/**
 * Skill client controller for runtime read query.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Skills.CLIENT_PATH)
@ExtractorManager.Extractor(httpExtractor = SkillHttpParamExtractor.class)
@Tag(name = "nacos.admin.ai.skill.client.api.controller.name",
    description = "nacos.admin.ai.skill.client.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class SkillClientController {
    
    private final SkillOperationService skillOperationService;
    
    public SkillClientController(SkillOperationService skillOperationService) {
        this.skillOperationService = skillOperationService;
    }
    
    /**
     * Download an online skill version as ZIP file by label/version/latest.
     */
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.OPEN_API,
        tags = {ALLOW_ANONYMOUS})
    @Operation(summary = "nacos.admin.ai.skill.client.api.get.summary",
        description = "nacos.admin.ai.skill.client.api.get.description",
        security = @SecurityRequirement(name = "nacos"), extensions = {
            @Extension(name = "nacos-api-since",
                properties = @ExtensionProperty(name = "version", value = "3.2.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            schema = @Schema(type = "string", format = "binary",
                description = "ZIP file containing the skill package")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "name", required = true, example = "my-skill"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "label"),
        @Parameter(name = "form", hidden = true)})
    public ResponseEntity<byte[]> get(SkillQueryForm form) throws NacosException {
        form.validate();
        Skill skill = skillOperationService.querySkill(form.getNamespaceId(), form.getName(),
            form.getVersion(),
            form.getLabel());
        return SkillRequestUtil.buildSkillZipResponse(skill);
    }
}
