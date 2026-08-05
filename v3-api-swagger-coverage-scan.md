# Nacos v3 HTTP API Swagger 注解覆盖扫描

扫描日期：2026-08-04

运行时复核：2026-08-05

扫描基线：当前工作树

目标：校验 v3 HTTP API 的 Swagger/OpenAPI 注解覆盖、参数语义、响应示例、i18n 引用和 `@Since` 元数据。

## 扫描范围

| 模块 | Controller | HTTP 映射 |
|---|---:|---:|
| core | 6 | 25 |
| config | 7 | 29 |
| naming | 7 | 32 |
| ai | 12 | 108 |
| console | 19 | 145 |
| **合计** | **51** | **339** |

以下内容按任务约定排除：

- `ai-registry-adaptor`
- 默认鉴权插件的 User、Role、Permission API
- 默认 Visibility 插件 API

## 扫描结果

当前纳入范围的 51 个 Controller 均有 `@Tag`，339 个 HTTP 映射均有：

- `@Operation`
- 至少一个 `@ApiResponse`
- `@Since`
- 与实际请求形态对应的参数或 RequestBody 描述

Swagger i18n 引用已在对应的 server/console 三份资源中补齐；中英文 key 集一致，资源内无重复 key。默认资源中的新增响应示例均为合法 JSON。

## 本轮主要修正

- 为 Agent Admin、Agent Client、Console Agent 共 40 个接口补齐整套 Swagger 注解。
- 细化 Agent Client 的 `X-Nacos-Client-Id` 配置、身份绑定和归属语义，并明确按 Client ID 而非 Endpoint 维度调度心跳及动态采用服务端返回间隔。
- 为 Admin/Console Skill 上传预检 2 个 multipart 接口补齐注解、二进制文件 schema、响应示例和 i18n。
- 修正 Config 发布、灰度发布、删除、批量删除、查询、导出、克隆和 Derby 导入的参数、binary schema 与 RequestBody 描述。
- 修正 Naming Service、Instance、Cluster 的实际参数、必填状态和响应示例类型。
- 修正 Skill、AgentSpec、MCP、Prompt、A2A、Console Plugin/Cluster 等接口中缺失或无效的参数，以及 Prompt 的 304 响应。
- 修复 `ResponseEntity<Result<T>>` 未被泛型 Schema 定制器识别而产生的 `Result`/`ResultAgentSpec` 悬空引用，并将 AgentSpec、Prompt、Skill 的 304 响应显式声明为无响应体。
- 修复 Example i18n 定制器遍历无响应体 304 时的空指针，避免 `/v3/api-docs/client-api` 返回 HTTP 500。
- 修正 Core ServerState、Namespace、Config History、Naming 等响应示例，使其与 `Result<T>` 和实际返回类型一致。
- 依据最早可用 3.x git tag，将 Admin/Console 的 AgentSpec list、Skill list、Skill upload 共 6 处 `@Since` 归一为 `3.2.0`。

当前代码版本为 `3.3.0-SNAPSHOT`。本地尚无 3.3.x tag，因此新增 Agent API 和 Skill 上传预检保留代码已有的 `@Since("3.3.0")`，待正式 3.3.x tag 发布后可再次追溯确认。

## 验证方式

- Controller 映射、`@Operation`、`@ApiResponse`、`@Since` 和 `@Tag` 静态计数及逐文件缺失扫描。
- Java 注解引用与 server/console i18n key 的交叉校验。
- i18n 重复 key 与新增 JSON example 校验。
- Swagger 定制器共 10 个单元测试，覆盖直接/包装 Result、非 Result、空 content、example i18n 空值防护、定制器组合链路及最终 `$ref` 可解析性。
- AgentSpec、Prompt、Skill Client Controller 目标单元测试共 12 个。
- 受影响 Maven 模块的 Spotless 和编译检查。
