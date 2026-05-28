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
import com.alibaba.nacos.ai.form.a2a.admin.AgentCardForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentCardUpdateForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentForm;
import com.alibaba.nacos.ai.form.a2a.admin.AgentListForm;
import com.alibaba.nacos.ai.param.AgentHttpParamExtractor;
import com.alibaba.nacos.ai.utils.AgentRequestUtil;
import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.ai.model.a2a.AgentCard;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardDetailInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentCardVersionInfo;
import com.alibaba.nacos.api.ai.model.a2a.AgentVersionDetail;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.api.remote.RemoteConstants;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.console.proxy.ai.A2aProxy;
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

import java.util.List;

/**
 * Console A2a Controller.
 *
 * @author KiteSoar
 */
@NacosApi
@RestController
@RequestMapping(Constants.A2A.CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = AgentHttpParamExtractor.class)
@Tag(name = "nacos.console.ai.a2a.api.controller.name",
    description = "nacos.console.ai.a2a.api.controller.description", extensions = {
        @Extension(name = RemoteConstants.LABEL_MODULE,
            properties = @ExtensionProperty(name = RemoteConstants.LABEL_MODULE, value = "ai"))})
public class ConsoleA2aController {
    
    private static final String AGENT_CARD_EXAMPLE =
        "{\"protocolVersion\":\"0.2.9\",\"name\":\"GeoSpatial Route Planner Agent\",\"description\":\"Provides advanced route planning, traffic analysis, and custom map generation services. This agent can calculate optimal routes, estimate travel times considering real-time traffic, and create personalized maps with points of interest.\",\"url\":\"https://georoute-agent.example.com/a2a/v1\",\"preferredTransport\":\"JSONRPC\",\"additionalInterfaces\":[{\"url\":\"https://georoute-agent.example.com/a2a/v1\",\"transport\":\"JSONRPC\"},{\"url\":\"https://georoute-agent.example.com/a2a/grpc\",\"transport\":\"GRPC\"},{\"url\":\"https://georoute-agent.example.com/a2a/json\",\"transport\":\"HTTP+JSON\"}],\"provider\":{\"organization\":\"Example Geo Services Inc.\",\"url\":\"https://www.examplegeoservices.com\"},\"iconUrl\":\"https://georoute-agent.example.com/icon.png\",\"version\":\"1.2.0\",\"documentationUrl\":\"https://docs.examplegeoservices.com/georoute-agent/api\",\"capabilities\":{\"streaming\":true,\"pushNotifications\":true,\"stateTransitionHistory\":false},\"securitySchemes\":{\"google\":{\"type\":\"openIdConnect\",\"openIdConnectUrl\":\"https://accounts.google.com/.well-known/openid-configuration\"}},\"security\":[{\"google\":[\"openid\",\"profile\",\"email\"]}],\"defaultInputModes\":[\"application/json\",\"text/plain\"],\"defaultOutputModes\":[\"application/json\",\"image/png\"],\"skills\":[{\"id\":\"route-optimizer-traffic\",\"name\":\"Traffic-Aware Route Optimizer\",\"description\":\"Calculates the optimal driving route between two or more locations, taking into account real-time traffic conditions, road closures, and user preferences (e.g., avoid tolls, prefer highways).\",\"tags\":[\"maps\",\"routing\",\"navigation\",\"directions\",\"traffic\"],\"examples\":[\"Plan a route from '1600 Amphitheatre Parkway, Mountain View, CA' to 'San Francisco International Airport' avoiding tolls.\",\"{\\\"origin\\\": {\\\"lat\\\": 37.422, \\\"lng\\\": -122.084}, \\\"destination\\\": {\\\"lat\\\": 37.7749, \\\"lng\\\": -122.4194}, \\\"preferences\\\": [\\\"avoid_ferries\\\"]}\"],\"inputModes\":[\"application/json\",\"text/plain\"],\"outputModes\":[\"application/json\",\"application/vnd.geo+json\",\"text/html\"]},{\"id\":\"custom-map-generator\",\"name\":\"Personalized Map Generator\",\"description\":\"Creates custom map images or interactive map views based on user-defined points of interest, routes, and style preferences. Can overlay data layers.\",\"tags\":[\"maps\",\"customization\",\"visualization\",\"cartography\"],\"examples\":[\"Generate a map of my upcoming road trip with all planned stops highlighted.\",\"Show me a map visualizing all coffee shops within a 1-mile radius of my current location.\"],\"inputModes\":[\"application/json\"],\"outputModes\":[\"image/png\",\"image/jpeg\",\"application/json\",\"text/html\"]}],\"supportsAuthenticatedExtendedCard\":true,\"signatures\":[{\"protected\":\"eyJhbGciOiJFUzI1NiIsInR5cCI6IkpPU0UiLCJraWQiOiJrZXktMSIsImprdSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vYWdlbnQvandrcy5qc29uIn0\",\"signature\":\"QFdkNLNszlGj3z3u0YQGt_T9LixY3qtdQpZmsTdDHDe3fXV9y9-B3m2-XgCpzuhiLt8E0tV6HXoZKHv4GtHgKQ\"}]}";
    
    private static final String AGENT_CARD_TEXT_EXAMPLE =
        "\"{\\\"protocolVersion\\\":\\\"0.2.9\\\",\\\"name\\\":\\\"GeoSpatial Route Planner Agent\\\",\\\"url\\\":\\\"https://georoute-agent.example.com/a2a/v1\\\",\\\"version\\\":\\\"1.2.0\\\"}\"";
    
    private final A2aProxy a2aProxy;
    
    public ConsoleA2aController(A2aProxy a2aProxy) {
        this.a2aProxy = a2aProxy;
    }
    
    /**
     * register agent.
     *
     * @param form the agent card form to register
     * @return result of the registration operation
     * @throws NacosException if the agent registration fails due to invalid input or internal error
     */
    @PostMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.a2a.api.create.summary",
        description = "nacos.console.ai.a2a.api.create.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.1.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.a2a.api.create.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentCard", required = true,
            schema = @Schema(type = "string",
                description = "JSON object string parsed as AgentCard"),
            example = AGENT_CARD_TEXT_EXAMPLE),
        @Parameter(name = "registrationType", example = AiConstants.A2a.A2A_ENDPOINT_TYPE_SERVICE),
        @Parameter(name = "form", hidden = true)})
    public Result<String> registerAgent(AgentCardForm form) throws NacosException {
        form.validate();
        AgentCard agentCard = AgentRequestUtil.parseAgentCard(form);
        a2aProxy.registerAgent(agentCard, form);
        return Result.success("ok");
    }
    
    /**
     * get agent card.
     *
     * @param form the agent form to get
     * @return result of the get operation
     * @throws NacosApiException if the agent get fails due to invalid input or internal error
     */
    @GetMapping
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.a2a.api.get.summary",
        description = "nacos.console.ai.a2a.api.get.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.1.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.a2a.api.get.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", example = "GeoSpatial Route Planner Agent", required = true),
        @Parameter(name = "version", example = "1.0.0"), @Parameter(name = "form", hidden = true),
        @Parameter(name = "registrationType", example = AiConstants.A2a.A2A_ENDPOINT_TYPE_SERVICE)})
    public Result<AgentCardDetailInfo> getAgentCard(AgentForm form) throws NacosException {
        form.validate();
        return Result.success(a2aProxy.getAgentCard(form));
    }
    
    /**
     * update agent.
     *
     * @param form the agent update form to update
     * @return result of the update operation
     * @throws NacosException if the agent update fails due to invalid input or internal error
     */
    @PutMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.a2a.api.update.summary",
        description = "nacos.console.ai.a2a.api.update.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.1.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.a2a.api.update.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentCard", required = true,
            schema = @Schema(type = "string",
                description = "JSON object string parsed as AgentCard"),
            example = AGENT_CARD_TEXT_EXAMPLE),
        @Parameter(name = "registrationType", example = AiConstants.A2a.A2A_ENDPOINT_TYPE_SERVICE),
        @Parameter(name = "setAsLatest", schema = @Schema(type = "boolean"), example = "true"),
        @Parameter(name = "form", hidden = true)})
    public Result<String> updateAgentCard(AgentCardUpdateForm form) throws NacosException {
        form.validate();
        AgentCard agentCard = AgentRequestUtil.parseAgentCard(form);
        a2aProxy.updateAgentCard(agentCard, form);
        return Result.success("ok");
    }
    
    /**
     * delete agent.
     *
     * @param form the agent form to delete
     * @return result of the deletion operation
     * @throws NacosException if the agent deletion fails due to invalid input or internal error
     */
    @DeleteMapping
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.a2a.api.delete.summary",
        description = "nacos.console.ai.a2a.api.delete.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.1.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.a2a.api.delete.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", example = "GeoSpatial Route Planner Agent", required = true),
        @Parameter(name = "version", example = "1.0.0"), @Parameter(name = "form", hidden = true)})
    public Result<String> deleteAgent(AgentForm form) throws NacosException {
        form.validate();
        a2aProxy.deleteAgent(form);
        return Result.success("ok");
    }
    
    /**
     * list agents.
     *
     * @param agentListForm the agent list form to list
     * @param pageForm      the page form to list
     * @return result of the list operation
     * @throws NacosException if the agent list fails due to invalid input or internal error
     */
    @GetMapping("/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @Operation(summary = "nacos.console.ai.a2a.api.list.summary",
        description = "nacos.console.ai.a2a.api.list.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.1.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.a2a.api.list.example")))
    @Parameters(value = {
        @Parameter(name = "pageNo", required = true, schema = @Schema(type = "integer"),
            example = "1"),
        @Parameter(name = "pageSize", required = true, schema = @Schema(type = "integer"),
            example = "100"),
        @Parameter(name = "namespaceId", example = "public"), @Parameter(name = "agentName"),
        @Parameter(name = "search", example = "blur", description = "blur or accurate",
            required = true),
        @Parameter(name = "agentListForm", hidden = true),
        @Parameter(name = "pageForm", hidden = true)})
    public Result<Page<AgentCardVersionInfo>> listAgents(AgentListForm agentListForm,
        PageForm pageForm)
        throws NacosException {
        agentListForm.validate();
        pageForm.validate();
        return Result.success(a2aProxy.listAgents(agentListForm, pageForm));
    }
    
    /**
     * List all versions for target Agent.
     *
     * @param agentForm agent form
     * @return all version for target agent.
     * @throws NacosException nacos exception
     */
    @GetMapping("/version/list")
    @Secured(action = ActionTypes.READ, signType = SignType.AI, apiType = ApiType.ADMIN_API)
    @Operation(summary = "nacos.console.ai.a2a.api.version.list.summary",
        description = "nacos.console.ai.a2a.api.version.list.description",
        security = @SecurityRequirement(name = "nacos"),
        extensions = {@Extension(name = "nacos-api-since",
            properties = @ExtensionProperty(name = "version", value = "3.1.0"))})
    @ApiResponse(responseCode = "200",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Result.class,
                example = "nacos.console.ai.a2a.api.version.list.example")))
    @Parameters(value = {@Parameter(name = "namespaceId", example = "public"),
        @Parameter(name = "agentName", example = "GeoSpatial Route Planner Agent", required = true),
        @Parameter(name = "agentForm", hidden = true)})
    public Result<List<AgentVersionDetail>> listAgentVersions(AgentForm agentForm)
        throws NacosException {
        agentForm.validate();
        return Result.success(
            a2aProxy.listAgentVersions(agentForm.getNamespaceId(), agentForm.getAgentName()));
    }
}
