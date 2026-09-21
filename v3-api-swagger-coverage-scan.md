# Nacos v3 HTTP API Swagger 注解覆盖扫描

扫描日期：2026-09-21

扫描基线：当前分支 `v3.0-develop-swagger-generator` 工作树（基于 `eff78af90`）。

目标：核对 v3 HTTP API 的注解覆盖、实际参数与默认值、请求/响应类型、多语言文案、响应示例和 `@Since`。

## 扫描范围与结果

| 模块 | Controller | HTTP 映射 | 状态 |
|---|---:|---:|---|
| core | 6 | 25 | 已完成 |
| config | 7 | 29 | 已完成 |
| naming | 7 | 32 | 已完成 |
| ai | 15 | 134 | 已完成 |
| console | 19 | 172 | 已完成 |
| 默认鉴权插件 v3 | 4 | 17 | 待确认命名约定 |
| **合计** | **58** | **409** | **392 个接口已完成，17 个待确认** |

按用户要求排除 `ai-registry-adaptor` 与 `dns`。v1 API 和没有 HTTP 映射的 ExceptionHandler 不计入 v3 API 数量。

已完成的 54 个 Controller 均有 `@Tag`；392 个方法均有 `@Operation`、响应注解和方法级 `@Since`。
本轮没有新增 HTTP 路由或推断新版本号；保留已有 `@Since`（新增注解所对应的方法已有 `3.3.0`）。

## 本轮修正

- 补全 41 个接口：MCP Admin/Console 生命周期各 14 个，MCP Client 6 个，Agent Admin/Console scope 各 1 个，Agent watch、AI capability、跨资源 search、Prompt search、Skill search 各 1 个。
- 为 3 个尚未进入 Swagger 的 Client Controller 添加同模块风格的 Tag 和模块扩展。
- 根据 Form 继承、validate/toRequest 和辅助解析方法核对字段，补齐 MCP 兼容接口的 mcpName/mcpId 回退参数、AgentSpec 的 tagsAll 和仅校验长度的继承 query 字段，以及 Skill Admin 上传的 autoPublishIfNew。
- 修正 27 个接口共 54 个 PageForm 分页参数的必填标记，声明默认 pageNo=1、pageSize=100 和正数边界；直接由 RequestParam 要求必填的分页参数保持原契约。
- 补齐 Watch 的 generation、timeoutMillis、JSON 字符串 watches 和两个必填 Header，声明 MCP Client 的共享活性 Header、端口类型、JSON 字符串字段及 search 数组参数。
- 修正 Agent Watch 的 watches 示例：agentName/version 置于 discoveryRequest.reference 内，补充必填 materializedFingerprint；默认、中英文描述同步明确指纹格式、最近物化 Discover 结果的来源及格式示例需替换。通过实际 Form 解析校验，避免只检查 JSON 语法而漏掉嵌套请求契约。
- 同步 server/console 默认、英文、中文资源；修正 Agent、AgentSpec、Skill、MCP、Pipeline、AI Import、Console Plugin/Cluster、Naming Instance/Ops 的过时响应示例，包括字段名、枚举状态和列表/分页形态。
- 支持 `DeferredResult<Result<T>>` 的 Swagger 泛型响应解析，避免 Watch 响应引用在全局 Schema 清理后悬空。
- 修复 Copilot config 参数误用 Swagger `@RequestBody` 导致 JSON 未绑定的问题：方法级保留文档注解，参数级恢复 Spring 注解。补强保存字段和非法 JSON 的 MockMvc 验证，并补充独立 OpenAPI IT 的缺失 JSON body 场景、矩阵和覆盖登记；既有 Partial 分类保持不变。

## 验证

- 54 个 Controller / 392 个映射的 Tag、Operation、ApiResponse、Since 静态覆盖核对。
- 非 String 基础参数 schema、Form getter 与绑定参数、JSON/multipart media type 的交叉扫描及人工复核。扫描中的 group/getGroupName 别名、派生 getter、导入的 Swagger RequestBody 已人工排除误报。
- 中英文资源引用可解析，无重复 key；按 Java Properties 转义规则还原后的 JSON 响应示例均可解析。
- 使用编译后的 Controller 返回泛型及 Jackson 序列化属性，核对 367 个 Result 响应示例；按实际 Capacity/HealthChecker 子类处理多态后，无未知字段、集合形态或枚举错误。该检查不等同于部署后的真实响应验证。
- Spotless apply/check 通过；43 个模块的 reactor 编译和目标测试通过。
- Swagger 定制器测试 12 个通过，含新增 DeferredResult 引用解析及非 Result 回归场景。
- Copilot Controller 测试 8 个通过；OpenAPI IT 编译通过。
- Agent Client Controller/Form 测试 20 个通过；编译后的 Watch 注解示例通过真实 AgentWatchBatchForm.toRequest() 校验，AI 模块 Spotless apply/check 通过。
- 独立业务 HTTP IT 尚未执行；本次 i18n 排查已在临时目录、独立端口启动完整 merged 实例，对 client/admin/console 三组 `/v3/api-docs` 分别请求 en-US、zh-CN，共 6 份文档均无未解析的 Nacos i18n key。该实例实际生成 25/195/160 个操作，静态覆盖计数仍按全部 Controller 映射统计。
- Bean 复用相关测试 10 个通过，包含新增父子容器消息配置隔离和 MessageSourceProperties 禁止复用场景；sys 模块 Spotless apply/check 通过。

## 运行时 i18n 排查与修复

- 现象：原运行实例的三组 Swagger 文档均直接输出 title、Tag、summary、example 的 key，中英文请求表现一致。
- 根因：父容器因依赖 `dnsjava` 自带的 `messages.properties` 创建默认消息配置。`NacosDuplicateSpringBeanPostProcessor` 在子容器中复用父容器的 `MessageSourceProperties`，使子容器实际 basename 仍为 `messages`，未采用 server/console 各自的配置。
- 修复：将 `MessageSourceProperties` 排除在父容器 Bean 复用范围外，由各容器独立绑定本地消息配置。继续保持 `ai-registry-adaptor` 和 `dns` 源码不变。
- 验证：完整 merged 实例中，父容器仍使用 `messages`，server 使用 `i18n/server_messages`，console 使用 `i18n/console_messages`；中英文 HTTP 文档标题、摘要、Tag 和响应示例恢复。原运行进程需重启加载新代码。

## 待确认项

默认鉴权插件的 User、Role、Permission、Visibility 四个 v3 Controller 共 17 个接口目前均无 Swagger 注解，同模块没有可归纳的 Tag/i18n 参考。已询问是否采用 `nacos.auth.<资源>.api.*` 或本轮排除该插件，等待用户选择后补齐。本轮尚未修改这四个 Controller。

此前 2026-08-04/05 扫描记录保留在 `doc/v3-swagger-api-changelog.md`。
