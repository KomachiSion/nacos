# V3 Swagger API Changelog

## 2026-05-27

### Existing Annotation Checks

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

## 2026-05-26

### Existing Annotation Checks

- `GET /v3/admin/cs/capacity`: fixed the capacity API description i18n key to match the controller annotation.
- `POST /v3/admin/cs/capacity`: fixed the capacity API description i18n key to match the controller annotation.
- `PUT /v3/admin/ns/ops/switches`: fixed the Swagger operation description key typo.
- `GET /v3/admin/ns/client/service/publisher/list`: declared `port` as an integer parameter.
- `GET /v3/admin/ns/client/service/subscriber/list`: declared `port` as an integer parameter.
- `GET /v3/admin/ns/client/distro`: declared `port` as an integer parameter.
- `POST /v3/admin/ai/skills/upload`: added `targetVersion` and `commitMsg` to the multipart Swagger request body and updated i18n descriptions.

### Missing Annotation Additions

- `GET /v3/admin/ai/import/sources`: added Swagger operation, response example, parameters, controller tag, and i18n keys for import source listing.
- `POST /v3/admin/ai/import/search`: added Swagger operation, response example, parameters, controller tag, and i18n keys for external AI resource search.
- `POST /v3/admin/ai/import/validate`: added Swagger operation, response example, parameters, controller tag, and i18n keys for import validation.
- `POST /v3/admin/ai/import/execute`: added Swagger operation, response example, parameters, controller tag, and i18n keys for import execution.
- `GET /v3/admin/ai/pipelines/list`: added Swagger operation, response example, parameters, and i18n keys for paginated pipeline execution queries.
- `GET /v3/admin/ai/pipelines/detail`: added Swagger operation, response example, parameters, and i18n keys for pipeline execution detail queries.
- `POST /v3/admin/ai/skills/upload/batch`: added multipart Swagger request body, response example, and i18n keys for batch skill upload.
- `POST /v3/admin/ai/skills/force-publish`: added Swagger operation, response example, parameters, and i18n keys for force publishing skill versions.
- `POST /v3/admin/ai/skills/redraft`: added Swagger operation, response example, parameters, and i18n keys for redrafting skill versions.
- `GET /v3/admin/ai/agentspecs/version/meta`: added Swagger operation, response example, parameters, and i18n keys for AgentSpec version metadata.
- `POST /v3/admin/ai/agentspecs/force-publish`: added Swagger operation, response example, parameters, and i18n keys for force publishing AgentSpec versions.
- `POST /v3/admin/ai/agentspecs/redraft`: added Swagger operation, response example, parameters, and i18n keys for redrafting AgentSpec versions.
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
