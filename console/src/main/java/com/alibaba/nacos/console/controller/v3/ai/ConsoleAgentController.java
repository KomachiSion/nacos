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

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.form.AiResourceFilterableForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentAdminForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentDraftCreateForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentDraftUpdateForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentLabelsUpdateForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentListForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentRuntimeEndpointForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentUpdateForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentVersionForm;
import com.alibaba.nacos.ai.form.agent.admin.AgentVersionListForm;
import com.alibaba.nacos.ai.param.AgentAdminHttpParamExtractor;
import com.alibaba.nacos.api.ai.model.agent.Agent;
import com.alibaba.nacos.api.ai.model.agent.AgentDraftCreateRequest;
import com.alibaba.nacos.api.ai.model.agent.AgentDraftUpdateRequest;
import com.alibaba.nacos.api.ai.model.agent.AgentLabelsUpdateRequest;
import com.alibaba.nacos.api.ai.model.agent.AgentOverview;
import com.alibaba.nacos.api.ai.model.agent.AgentSummary;
import com.alibaba.nacos.api.ai.model.agent.AgentUpdateRequest;
import com.alibaba.nacos.api.ai.model.agent.AgentVersionDetail;
import com.alibaba.nacos.api.ai.model.agent.AgentVersionSummary;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.model.ai.ConsoleRuntimeEndpointView;
import com.alibaba.nacos.console.proxy.ai.AgentProxy;
import com.alibaba.nacos.core.model.form.PageForm;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * Console facade for protocol-neutral Agent management.
 *
 * @author Nacos
 */
@NacosApi
@RestController
@RequestMapping(Constants.Agent.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = AgentAdminHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.agent.api.controller.name",
    description = "nacos.console.ai.agent.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleAgentController {
    
    private final AgentProxy agentProxy;
    
    public ConsoleAgentController(AgentProxy agentProxy) {
        this.agentProxy = agentProxy;
    }
    
    /**
     * Read one Agent and the first bounded Version-summary page.
     */
    @Since("3.3.0")
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.get.summary",
        description = "nacos.console.ai.agent.api.get.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentOverview> getAgent(AgentAdminForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.getAgent(form.getNamespaceId(), form.getAgentName()));
    }
    
    /**
     * Replace every writable field of one Agent.
     */
    @Since("3.3.0")
    @PutMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.update.summary",
        description = "nacos.console.ai.agent.api.update.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "displayName", example = "My Agent"),
        @Parameter(name = "description", example = "An agent for customer support"),
        @Parameter(name = "iconUrl", example = "https://example.com/icon.png"),
        @Parameter(name = "provider", description = "Provider JSON string"),
        @Parameter(name = "tags", description = "Tags JSON array string"),
        @Parameter(name = "extensions", description = "Extensions JSON object string"),
        @Parameter(name = "status", required = true, example = "enable"),
        @Parameter(name = "form", hidden = true)})
    public Result<Agent> updateAgent(AgentUpdateForm form) throws NacosException {
        AgentUpdateRequest request = form.toRequest();
        return Result.success(agentProxy.updateAgent(form.getNamespaceId(), request));
    }
    
    /**
     * Delete one Agent definition and all of its Version content.
     */
    @Since("3.3.0")
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.delete.summary",
        description = "nacos.console.ai.agent.api.delete.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "form", hidden = true)})
    public Result<Void> deleteAgent(AgentAdminForm form) throws NacosException {
        form.validate();
        agentProxy.deleteAgent(form.getNamespaceId(), form.getAgentName());
        return Result.success();
    }
    
    /**
     * Filter and page Agent summaries.
     */
    @Since("3.3.0")
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.list.summary",
        description = "nacos.console.ai.agent.api.list.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.list.example")))
    @Parameters(value = {
        @Parameter(name = "pageNo", required = true, schema = @Schema(type = "integer"),
            example = "1"),
        @Parameter(name = "pageSize", required = true, schema = @Schema(type = "integer"),
            example = "100"),
        @Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", example = "my-agent"),
        @Parameter(name = "orderBy", example = "download_count"),
        @Parameter(name = "owner", example = "nacos"),
        @Parameter(name = "scope", example = "PUBLIC"),
        @Parameter(name = "bizTag", example = "customer-service"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = "filterableForm", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<AgentSummary>> listAgents(AgentListForm form,
        AiResourceFilterableForm filterableForm, PageForm pageForm) throws NacosException {
        form.validate();
        filterableForm.validate();
        pageForm.validate();
        String scope = filterableForm.getScope() == null ? null
            : filterableForm.getScope().toUpperCase(Locale.ROOT);
        return Result.success(agentProxy.listAgents(form.getNamespaceId(), form.getAgentName(),
            filterableForm.getBizTag(), scope, filterableForm.getOwner(), form.getOrderBy(),
            pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * Page Version summaries for one Agent.
     */
    @Since("3.3.0")
    @GetMapping("/versions")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.versions.summary",
        description = "nacos.console.ai.agent.api.versions.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.versions.example")))
    @Parameters(value = {
        @Parameter(name = "pageNo", required = true, schema = @Schema(type = "integer"),
            example = "1"),
        @Parameter(name = "pageSize", required = true, schema = @Schema(type = "integer"),
            example = "100"),
        @Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "status", example = "online"),
        @Parameter(name = "form", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<AgentVersionSummary>> listVersions(AgentVersionListForm form,
        PageForm pageForm) throws NacosException {
        form.validate();
        pageForm.validate();
        return Result.success(agentProxy.listVersions(form.getNamespaceId(), form.getAgentName(),
            form.getStatus(), pageForm.getPageNo(), pageForm.getPageSize()));
    }
    
    /**
     * Read one exact Agent Version definition.
     */
    @Since("3.3.0")
    @GetMapping("/version")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.version.get.summary",
        description = "nacos.console.ai.agent.api.version.get.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.version.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionDetail> getVersion(AgentVersionForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.getVersion(form.getNamespaceId(), form.getAgentName(),
            form.getVersion()));
    }
    
    /**
     * Read one protocol's complete Runtime Endpoint snapshot and Naming service reference.
     */
    @Since("3.3.0")
    @GetMapping("/runtime-endpoints")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.runtime.endpoints.summary",
        description = "nacos.console.ai.agent.api.runtime.endpoints.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.runtime.endpoints.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "protocol", required = true, example = "A2A"),
        @Parameter(name = "version", example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<ConsoleRuntimeEndpointView> getRuntimeEndpoints(
        AgentRuntimeEndpointForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.getRuntimeEndpoints(form.getNamespaceId(),
            form.getAgentName(), form.getProtocol(), form.getVersion()));
    }
    
    /**
     * Create one initial or subsequent Agent draft.
     */
    @Since("3.3.0")
    @PostMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.draft.create.summary",
        description = "nacos.console.ai.agent.api.draft.create.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.draft.create.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "displayName", example = "My Agent"),
        @Parameter(name = "description", example = "An agent for customer support"),
        @Parameter(name = "iconUrl", example = "https://example.com/icon.png"),
        @Parameter(name = "provider", description = "Provider JSON string"),
        @Parameter(name = "tags", description = "Tags JSON array string"),
        @Parameter(name = "extensions", description = "Extensions JSON object string"),
        @Parameter(name = "callInterfaces", description = "Call interfaces JSON array string"),
        @Parameter(name = "author", example = "nacos"),
        @Parameter(name = "changeDescription", example = "Initial version"),
        @Parameter(name = "basedOnVersion", example = "0.9.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionDetail> createDraft(AgentDraftCreateForm form)
        throws NacosException {
        AgentDraftCreateRequest request = form.toRequest();
        return Result.success(agentProxy.createDraft(form.getNamespaceId(), request));
    }
    
    /**
     * Replace one exact Agent draft.
     */
    @Since("3.3.0")
    @PutMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.draft.update.summary",
        description = "nacos.console.ai.agent.api.draft.update.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.draft.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "callInterfaces", required = true,
            description = "Call interfaces JSON array string"),
        @Parameter(name = "changeDescription", example = "Update endpoint configuration"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionDetail> updateDraft(AgentDraftUpdateForm form)
        throws NacosException {
        AgentDraftUpdateRequest request = form.toRequest();
        return Result.success(agentProxy.updateDraft(form.getNamespaceId(), request));
    }
    
    /**
     * Delete one exact current Agent draft.
     */
    @Since("3.3.0")
    @DeleteMapping("/draft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.draft.delete.summary",
        description = "nacos.console.ai.agent.api.draft.delete.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.draft.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<Void> deleteDraft(AgentVersionForm form) throws NacosException {
        form.validate();
        agentProxy.deleteDraft(form.getNamespaceId(), form.getAgentName(), form.getVersion());
        return Result.success();
    }
    
    /**
     * Submit one exact Agent draft.
     */
    @Since("3.3.0")
    @PostMapping("/submit")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.submit.summary",
        description = "nacos.console.ai.agent.api.submit.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.submit.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionSummary> submit(AgentVersionForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.submit(form.getNamespaceId(), form.getAgentName(),
            form.getVersion()));
    }
    
    /**
     * Publish one exact reviewed Agent Version.
     */
    @Since("3.3.0")
    @PostMapping("/publish")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.publish.summary",
        description = "nacos.console.ai.agent.api.publish.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionSummary> publish(AgentVersionForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.publish(form.getNamespaceId(), form.getAgentName(),
            form.getVersion()));
    }
    
    /**
     * Force-publish one exact working Agent Version.
     */
    @Since("3.3.0")
    @PostMapping("/force-publish")
    @Secured(resource = Constants.Agent.CONSOLE_PATH + "/force-publish",
        action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.force.publish.summary",
        description = "nacos.console.ai.agent.api.force.publish.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.force.publish.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionSummary> forcePublish(AgentVersionForm form)
        throws NacosException {
        form.validate();
        return Result.success(agentProxy.forcePublish(form.getNamespaceId(),
            form.getAgentName(), form.getVersion()));
    }
    
    /**
     * Move one exact reviewed Agent Version back to draft.
     */
    @Since("3.3.0")
    @PostMapping("/redraft")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.redraft.summary",
        description = "nacos.console.ai.agent.api.redraft.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.redraft.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionSummary> redraft(AgentVersionForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.redraft(form.getNamespaceId(), form.getAgentName(),
            form.getVersion()));
    }
    
    /**
     * Bring one exact offline Agent Version online.
     */
    @Since("3.3.0")
    @PostMapping("/online")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.online.summary",
        description = "nacos.console.ai.agent.api.online.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.online.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionSummary> online(AgentVersionForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.online(form.getNamespaceId(), form.getAgentName(),
            form.getVersion()));
    }
    
    /**
     * Take one exact online Agent Version offline.
     */
    @Since("3.3.0")
    @PostMapping("/offline")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.offline.summary",
        description = "nacos.console.ai.agent.api.offline.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.offline.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "version", required = true, example = "1.0.0"),
        @Parameter(name = "form", hidden = true)})
    public Result<AgentVersionSummary> offline(AgentVersionForm form) throws NacosException {
        form.validate();
        return Result.success(agentProxy.offline(form.getNamespaceId(), form.getAgentName(),
            form.getVersion()));
    }
    
    /**
     * Replace custom labels while preserving the service-managed latest label.
     */
    @Since("3.3.0")
    @PutMapping("/labels")
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.agent.api.labels.update.summary",
        description = "nacos.console.ai.agent.api.labels.update.description",
        security = @SecurityRequirement(name = "nacos"))
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.agent.api.labels.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", required = true, example = "my-agent"),
        @Parameter(name = "labels", required = true, description = "Labels JSON object string"),
        @Parameter(name = "form", hidden = true)})
    public Result<Agent> updateLabels(AgentLabelsUpdateForm form) throws NacosException {
        AgentLabelsUpdateRequest request = form.toRequest();
        return Result.success(agentProxy.updateLabels(form.getNamespaceId(), request));
    }
}
