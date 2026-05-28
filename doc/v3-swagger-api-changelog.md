# V3 Swagger API Changelog

## 2026-05-28

### Existing Annotation Checks

- `admin-api`: added `x-nacos-api-since.version` to all 159 admin operations; existing APIs use the earliest matching 3.x git tag, normalized to `x.y.z`.
- `POST /v3/admin/ai/agentspecs/redraft`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `GET /v3/admin/ai/import/sources`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `POST /v3/admin/ai/import/search`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `POST /v3/admin/ai/import/validate`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `POST /v3/admin/ai/import/execute`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `GET /v3/admin/ai/prompt/version/download`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `POST /v3/admin/ai/prompt/redraft`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `POST /v3/admin/ai/skills/upload/batch`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.
- `POST /v3/admin/ai/skills/redraft`: added `x-nacos-api-since.version=3.2.2` after confirming the current pom candidate version for this new API.

## 2026-05-27

### Existing Annotation Checks

- `GET /v3/client/cs/config`: added `x-nacos-api-since.version=3.0.0` based on the first 3.x tag containing the v3 client config API path.
- `POST /v3/client/ns/instance`: added `x-nacos-api-since.version=3.0.0` based on the first 3.x tag containing the v3 client naming instance API path.
- `DELETE /v3/client/ns/instance`: added `x-nacos-api-since.version=3.0.0` based on the first 3.x tag containing the v3 client naming instance API path.
- `GET /v3/client/ns/instance/list`: added `x-nacos-api-since.version=3.0.0` based on the first 3.x tag containing the v3 client naming instance list API path.
- `GET /v3/client/ai/prompt`: added `x-nacos-api-since.version=3.2.0` based on the first 3.x tag containing the prompt client controller and normalized to the `x.y.z` version prefix.
- `GET /v3/client/ai/skills`: added `x-nacos-api-since.version=3.2.0` based on the first 3.x tag containing the skill client controller.
- `GET /v3/client/ai/agentspecs`: added `x-nacos-api-since.version=3.2.0` based on the first 3.x tag containing the AgentSpec client controller.
- `GET /v3/client/ai/agentspecs/search`: added `x-nacos-api-since.version=3.2.0` based on the first 3.x tag containing the AgentSpec client controller.
- `GET /v3/client/ai/prompt`: corrected the Swagger i18n descriptions to match the current query priority `version > label > latest`.
- `POST /v3/client/ns/instance`: clarified the `metadata` query parameter as a JSON object string parsed into the metadata map.
- `POST /v3/admin/ns/service`: clarified `selector` and `metadata` as JSON object string query parameters.
- `PUT /v3/admin/ns/service`: clarified `selector` and `metadata` as JSON object string query parameters.
- `POST /v3/admin/ns/instance`: clarified `metadata` as a JSON object string query parameter.
- `PUT /v3/admin/ns/instance`: clarified `metadata` as a JSON object string query parameter.
- `PUT /v3/admin/ns/instance/partial`: clarified `metadata` as a JSON object string query parameter.
- `PUT /v3/admin/ns/instance/metadata/batch`: clarified `metadata` and `instances` as JSON string query parameters.
- `DELETE /v3/admin/ns/instance/metadata/batch`: clarified `metadata` and `instances` as JSON string query parameters.
- `PUT /v3/admin/ns/cluster`: clarified `healthChecker` and `metadata` as JSON object string query parameters.
- `PUT /v3/admin/ns/ops/switches`: declared `value` as a string parameter to match the update form.
- `PUT /v3/admin/core/plugin/config`: clarified `config` as a JSON object string query parameter.
- `DELETE /v3/admin/cs/config/batch`: declared `ids` as an integer array query parameter.
- `GET /v3/admin/cs/config/export`: declared `ids` as an integer array query parameter.
- `PUT /v3/admin/ai/skills/labels`: clarified `labels` as a JSON object string query parameter.
- `PUT /v3/admin/ai/skills/biz-tags`: clarified `bizTags` as a JSON array string query parameter.
- `PUT /v3/admin/ai/agentspecs/labels`: clarified `labels` as a JSON object string query parameter.
- `PUT /v3/admin/ai/agentspecs/biz-tags`: clarified `bizTags` as a JSON array string query parameter.
- `POST /v3/admin/ai/prompt/draft`: clarified `variables` as a JSON array string query parameter.
- `PUT /v3/admin/ai/prompt/draft`: clarified `variables` as a JSON array string query parameter.
- `PUT /v3/admin/ai/prompt/labels`: clarified `labels` as a JSON object string query parameter.
- `PUT /v3/admin/ai/prompt/biz-tags`: clarified `bizTags` as a JSON array string query parameter.
- `POST /v3/admin/ai/prompt`: clarified `variables` as a JSON array string query parameter.
- `POST /v3/admin/ai/import/search`: clarified `options` as a JSON object string query parameter.
- `POST /v3/admin/ai/import/validate`: clarified `selectedItems` and `options` as JSON string query parameters.
- `POST /v3/admin/ai/import/execute`: clarified `selectedItems` and `options` as JSON string query parameters.
- `POST /v3/admin/ai/mcp`: clarified MCP specification parameters as JSON object string query parameters.
- `PUT /v3/admin/ai/mcp`: clarified MCP specification parameters as JSON object string query parameters.
- `POST /v3/admin/ai/a2a`: clarified `agentCard` as a JSON object string query parameter.
- `PUT /v3/admin/ai/a2a`: clarified `agentCard` as a JSON object string query parameter.
- `POST /v3/admin/ai/mcp`: escaped the `serverSpecification` JSON string example so Swagger emits the full example.
- `PUT /v3/admin/ai/mcp`: escaped the `serverSpecification` JSON string example so Swagger emits the full example.
- `POST /v3/admin/ai/a2a`: escaped the `agentCard` JSON string example so Swagger emits the full example.
- `PUT /v3/admin/ai/a2a`: escaped the `agentCard` JSON string example so Swagger emits the full example.
- `PUT /v3/console/plugin/config`: clarified `config` as a JSON object string query parameter.
- `POST /v3/console/ns/service`: clarified `selector` and `metadata` as JSON object string query parameters.
- `PUT /v3/console/ns/service`: clarified `selector` and `metadata` as JSON object string query parameters.
- `PUT /v3/console/ns/service/cluster`: declared `checkPort` as an integer, `useInstancePort4Check` as a boolean, and `healthChecker`/`metadata` as JSON object string query parameters.
- `PUT /v3/console/ns/instance`: clarified `metadata` as a JSON object string query parameter.
- `DELETE /v3/console/ns/instance`: clarified `metadata` as a JSON object string query parameter.
- `DELETE /v3/console/cs/config/batchDelete`: declared `ids` as an integer array query parameter.
- `GET /v3/console/cs/config/export2`: declared `ids` as an integer array query parameter.
- `PUT /v3/console/ai/skills/labels`: clarified `labels` as a JSON object string query parameter.
- `PUT /v3/console/ai/skills/biz-tags`: clarified `bizTags` as a JSON array string query parameter.
- `PUT /v3/console/ai/agentspecs/labels`: clarified `labels` as a JSON object string query parameter.
- `PUT /v3/console/ai/agentspecs/biz-tags`: clarified `bizTags` as a JSON array string query parameter.
- `POST /v3/console/ai/prompt/draft`: clarified `variables` as a JSON array string query parameter.
- `PUT /v3/console/ai/prompt/draft`: clarified `variables` as a JSON array string query parameter.
- `PUT /v3/console/ai/prompt/labels`: clarified `labels` as a JSON object string query parameter.
- `PUT /v3/console/ai/prompt/biz-tags`: clarified `bizTags` as a JSON array string query parameter.
- `POST /v3/console/ai/import/search`: clarified `options` as a JSON object string query parameter.
- `POST /v3/console/ai/import/validate`: clarified `selectedItems` and `options` as JSON string query parameters.
- `POST /v3/console/ai/import/execute`: clarified `selectedItems` and `options` as JSON string query parameters.
- `POST /v3/console/ai/mcp`: clarified MCP specification parameters as JSON object string query parameters.
- `PUT /v3/console/ai/mcp`: clarified MCP specification parameters as JSON object string query parameters.
- `POST /v3/console/ai/mcp/import/execute`: declared `selectedServers` as a string array query parameter.
- `POST /v3/console/ai/a2a`: clarified `agentCard` as a JSON object string query parameter.
- `PUT /v3/console/ai/a2a`: clarified `agentCard` as a JSON object string query parameter.

## 2026-05-26

### Existing Annotation Checks

- `GET /v3/admin/cs/capacity`: fixed the capacity API description i18n key to match the controller annotation.
- `POST /v3/admin/cs/capacity`: fixed the capacity API description i18n key to match the controller annotation.
- `PUT /v3/admin/ns/ops/switches`: fixed the Swagger operation description key typo.
- `GET /v3/console/ns/service/subscribers`: declared `pageNo` and `pageSize` as integer parameters and `aggregation` as a boolean parameter.
- `GET /v3/console/ns/service/list`: declared `pageNo` and `pageSize` as integer parameters and `ignoreEmptyService` and `withInstances` as boolean parameters.
- `GET /v3/admin/ns/client/service/publisher/list`: declared `port` as an integer parameter.
- `GET /v3/admin/ns/client/service/subscriber/list`: declared `port` as an integer parameter.
- `GET /v3/admin/ns/client/distro`: declared `port` as an integer parameter.
- `POST /v3/admin/ai/skills/upload`: added `targetVersion` and `commitMsg` to the multipart Swagger request body and updated i18n descriptions.
- `POST /v3/console/ai/skills/upload`: added `targetVersion` and `commitMsg` to the multipart Swagger request body and updated i18n descriptions.

### Missing Annotation Additions

- `GET /v3/admin/ai/import/sources`: added Swagger operation, response example, parameters, controller tag, and i18n keys for import source listing.
- `POST /v3/admin/ai/import/search`: added Swagger operation, response example, parameters, controller tag, and i18n keys for external AI resource search.
- `POST /v3/admin/ai/import/validate`: added Swagger operation, response example, parameters, controller tag, and i18n keys for import validation.
- `POST /v3/admin/ai/import/execute`: added Swagger operation, response example, parameters, controller tag, and i18n keys for import execution.
- `GET /v3/console/ai/import/sources`: added Swagger operation, response example, parameters, controller tag, and i18n keys for console import source listing.
- `POST /v3/console/ai/import/search`: added Swagger operation, response example, parameters, controller tag, and i18n keys for console external AI resource search.
- `POST /v3/console/ai/import/validate`: added Swagger operation, response example, parameters, controller tag, and i18n keys for console import validation.
- `POST /v3/console/ai/import/execute`: added Swagger operation, response example, parameters, controller tag, and i18n keys for console import execution.
- `GET /v3/admin/ai/pipelines/list`: added Swagger operation, response example, parameters, and i18n keys for paginated pipeline execution queries.
- `GET /v3/admin/ai/pipelines/detail`: added Swagger operation, response example, parameters, and i18n keys for pipeline execution detail queries.
- `GET /v3/console/ai/pipelines/list`: added Swagger operation, response example, parameters, and i18n keys for console paginated pipeline execution queries.
- `GET /v3/console/ai/pipelines/detail`: added Swagger operation, response example, parameters, and i18n keys for console pipeline execution detail queries.
- `POST /v3/admin/ai/skills/upload/batch`: added multipart Swagger request body, response example, and i18n keys for batch skill upload.
- `POST /v3/admin/ai/skills/force-publish`: added Swagger operation, response example, parameters, and i18n keys for force publishing skill versions.
- `POST /v3/admin/ai/skills/redraft`: added Swagger operation, response example, parameters, and i18n keys for redrafting skill versions.
- `POST /v3/console/ai/skills/upload/batch`: added multipart Swagger request body, response example, and i18n keys for console batch skill upload.
- `POST /v3/console/ai/skills/force-publish`: added Swagger operation, response example, parameters, and i18n keys for console force publishing skill versions.
- `POST /v3/console/ai/skills/redraft`: added Swagger operation, response example, parameters, and i18n keys for console redrafting skill versions.
- `GET /v3/admin/ai/agentspecs/version/meta`: added Swagger operation, response example, parameters, and i18n keys for AgentSpec version metadata.
- `POST /v3/admin/ai/agentspecs/force-publish`: added Swagger operation, response example, parameters, and i18n keys for force publishing AgentSpec versions.
- `POST /v3/admin/ai/agentspecs/redraft`: added Swagger operation, response example, parameters, and i18n keys for redrafting AgentSpec versions.
- `POST /v3/console/ai/agentspecs/force-publish`: added Swagger operation, response example, parameters, and i18n keys for console force publishing AgentSpec versions.
- `POST /v3/console/ai/agentspecs/redraft`: added Swagger operation, response example, parameters, and i18n keys for console redrafting AgentSpec versions.
- `GET /v3/admin/ai/prompt/governance`: added Swagger operation, response example, parameters, and i18n keys for prompt governance data.
- `GET /v3/admin/ai/prompt/version`: added Swagger operation, response example, parameters, and i18n keys for prompt version detail queries.
- `GET /v3/admin/ai/prompt/version/download`: added Swagger operation, binary response schema, parameters, and i18n keys for prompt version downloads.
- `POST /v3/admin/ai/prompt/draft`: added Swagger operation, response example, parameters, and i18n keys for creating prompt drafts.
- `PUT /v3/admin/ai/prompt/draft`: added Swagger operation, response example, parameters, and i18n keys for updating prompt drafts.
- `DELETE /v3/admin/ai/prompt/draft`: added Swagger operation, response example, parameters, and i18n keys for deleting prompt drafts.
- `POST /v3/admin/ai/prompt/submit`: added Swagger operation, response example, parameters, and i18n keys for submitting prompt versions.
- `POST /v3/admin/ai/prompt/publish`: added Swagger operation, response example, parameters, and i18n keys for publishing prompt versions.
- `POST /v3/admin/ai/prompt/force-publish`: added Swagger operation, response example, parameters, and i18n keys for force publishing prompt versions.
- `POST /v3/admin/ai/prompt/redraft`: added Swagger operation, response example, parameters, and i18n keys for redrafting prompt versions.
- `POST /v3/admin/ai/prompt/online`: added Swagger operation, response example, parameters, and i18n keys for bringing prompt versions online.
- `POST /v3/admin/ai/prompt/offline`: added Swagger operation, response example, parameters, and i18n keys for taking prompt versions offline.
- `PUT /v3/admin/ai/prompt/labels`: added Swagger operation, response example, parameters, and i18n keys for updating prompt labels.
- `PUT /v3/admin/ai/prompt/description`: added Swagger operation, response example, parameters, and i18n keys for updating prompt descriptions.
- `PUT /v3/admin/ai/prompt/biz-tags`: added Swagger operation, response example, parameters, and i18n keys for updating prompt business tags.
- `POST /v3/admin/ai/prompt`: added Swagger operation, response example, parameters, and i18n keys for the legacy prompt publish endpoint.
- `GET /v3/console/ai/prompt/governance`: added Swagger operation, response example, parameters, and i18n keys for console prompt governance data.
- `GET /v3/console/ai/prompt/version`: added Swagger operation, response example, parameters, and i18n keys for console prompt version detail queries.
- `GET /v3/console/ai/prompt/version/download`: added Swagger operation, binary response schema, parameters, and i18n keys for console prompt version downloads.
- `POST /v3/console/ai/prompt/draft`: added Swagger operation, response example, parameters, and i18n keys for console prompt draft creation.
- `PUT /v3/console/ai/prompt/draft`: added Swagger operation, response example, parameters, and i18n keys for console prompt draft updates.
- `DELETE /v3/console/ai/prompt/draft`: added Swagger operation, response example, parameters, and i18n keys for console prompt draft deletion.
- `POST /v3/console/ai/prompt/submit`: added Swagger operation, response example, parameters, and i18n keys for console prompt version submission.
- `POST /v3/console/ai/prompt/publish`: added Swagger operation, response example, parameters, and i18n keys for console prompt version publishing.
- `POST /v3/console/ai/prompt/force-publish`: added Swagger operation, response example, parameters, and i18n keys for console force publishing prompt versions.
- `POST /v3/console/ai/prompt/redraft`: added Swagger operation, response example, parameters, and i18n keys for console redrafting prompt versions.
- `POST /v3/console/ai/prompt/online`: added Swagger operation, response example, parameters, and i18n keys for console prompt version online operations.
- `POST /v3/console/ai/prompt/offline`: corrected the existing Swagger operation keys to the offline prompt operation and added matching i18n keys.
- `PUT /v3/console/ai/prompt/labels`: added Swagger operation, response example, parameters, and i18n keys for console prompt label updates.
- `PUT /v3/console/ai/prompt/description`: added Swagger operation, response example, parameters, and i18n keys for console prompt description updates.
- `PUT /v3/console/ai/prompt/biz-tags`: added Swagger operation, response example, parameters, and i18n keys for console prompt business tag updates.
- `DELETE /v3/console/ns/instance`: added Swagger operation, response example, parameters with primitive schemas, and i18n keys for deleting persistent instances.
