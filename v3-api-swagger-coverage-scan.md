# Nacos v3 HTTP API Swagger 注解覆盖扫描

扫描日期：2026-10-26

扫描基线：`v3.0-develop-swagger-generator`，`c6805201f` 工作树。

## 范围与结果

按 `nacos-v3-swagger-annotations` 技能核对各模块 v3 HTTP API。按本次要求排除
`plugin-default-impl` 下的默认插件 Controller 与 `ai-registry-adaptor` Controller。
其他模块也进行了 Controller 清点；旧版接口、非 v3 协议端点、页面重定向与
ExceptionHandler 不计入本次 v3 API 数量。

| 模块 | Controller | HTTP 映射 | 本轮修订注解的接口 |
|---|---:|---:|---:|
| core | 6 | 25 | 8 |
| config | 7 | 29 | 5 |
| naming | 7 | 32 | 10 |
| ai | 15 | 134 | 45 |
| console | 19 | 160 | 55 |
| **合计** | **54** | **380** | **123** |

全部 54 个 Controller 已有 `@Tag`，380 个映射均有 `@Operation`、响应注解与
方法级 `@Since`。本轮没有新增路由，也没有缺失的 `@Since` 需要推断版本；现有版本标记保持不变。

## 本轮修复

- 灰度配置发布：补上实现支持的 `grayRuleExp`，移除 `grayMatchRuleExp` 单独必填的错误标记，
  说明两者至少提供一个，以及非空白 `grayMatchRuleExp` 的优先级；补充 `grayType=tagv2`
  和 `grayPriority=0` 默认值。
- Agent Discover：为 `X-Nacos-Agent-Discovery-Trigger` 添加 Header 位置、`watch-notify`
  示例与中英文说明。与 Agent API 规范一致，缺失、空值和未知值均兼容接受，仅用于诊断归因。
- Agent Search：补充 `pageNo=1`、`pageSize=20` 默认值与 `pageSize` 的 1～100 边界。
- 上传接口：11 个 multipart 接口显式声明必填请求体和 `file` 属性，保留文件的 binary schema；
  补充 overwrite=false、导入策略 ABORT 等实际默认值。其余必填 JSON 请求体同步声明 required。
  Copilot SSE 的可选 Spring RequestBody 保留现状，以维持其缺失请求体时通过 SSE 报错的文档契约。
- Console 状态查询：将误生成为 string 的 Map 响应显式声明为 object，值类型为 string。
- Console 配置导出：补充 `string/binary` 响应 schema；配置克隆请求体说明按实际 DTO 区分
  Console 的 `cfgId/dataId/group` 和 Admin 的 `configId/targetDataId/targetGroupName`。
- 补充配置检索、公告语言、插件 localOnly、Naming heartbeat/metrics、连接负载系数等实际默认值。
- Console Skill/AgentSpec 草稿更新：标明 setAsLatest 是服务端忽略的遗留兼容参数，latest 由服务端维护。
- 将参数、ZIP 响应和 304 响应中的硬编码英文说明迁移到各模块既有 i18n key 层级，
  同步默认、英文与中文资源。Console 使用 console_messages，其余模块使用 server_messages。
  运行时验证发现内联 Schema.description 不会被当前 SpringDoc 翻译；参数说明放在
  Parameter.description，响应说明放在 ApiResponse.description，上传文件说明由既有
  RequestBody.description 承载，移除重复的内联文件说明。该项未增加生成器逻辑或全局解析开关。
- 补齐 Server Swagger 配置方法的 Javadoc，并格式化相关配置类，使本次验证所需的 Checkstyle 通过。

## 复核方法与误报处理

- 全量扫描 HTTP 映射、Tag、Operation、ApiResponse、Since 与 i18n 引用。
- 对照方法签名、Form 继承、getter、validate/toRequest、辅助方法及 Console 转发链核对参数。
- 非 String 基础类型显式 schema 检查通过；multipart 的 boolean 属性保持 boolean。
- `MetricsControllerV3` 的 dataId/groupName 虽在 Spring 绑定中标为可选，但后续 ParamUtils
  校验要求非空，因此保留 Swagger 必填标记。
- Console MCP 兼容创建/更新路径未使用 resourceSpecification，不从 Admin 接口机械复制该字段。
- watchPayloadBytes、resolvedInitialSkill 等派生 getter 不作为输入参数；未使用的 Form 字段不补入文档。
- `@Deprecated` 的兼容 API 仍由 SpringDoc 识别；保留已有接口及其版本声明。
- 以忽略格式空白的源代码比较确认，380 个 HTTP 方法体均未改变。

## API IT 与规范影响

本轮修正文档注解、i18n、Swagger 配置注释、字符串 ID 和动态 Map 的 schema 推导；
路由、参数绑定、校验、响应、鉴权及 SDK 契约没有改变。
已核对 HTTP API、响应与错误、Config 灰度发布和 Agent API 相关规范，以及现有 ConfigGray 与
AgentDiscovery 独立 API IT 场景。无需新增业务场景或调整覆盖状态，未修改业务 API IT 矩阵或规范。

## 验证

- 7 个受影响模块的 Spotless apply/check 通过（含追加修复的 swagger 模块）；server 配置类的 Checkstyle 通过。
- 53 个模块 reactor 编译和 Checkstyle 通过。首次发现 server Swagger 配置的既有 Javadoc 问题，
  已补齐，追加字符串 ID 修复后再次通过完整 reactor 编译和 Checkstyle。
- 76 项现有测试通过：ConfigControllerV3 34 项、AgentClientController 14 项、
  ConsoleConfigController 16 项、Swagger i18n/泛型定制器 12 项。
- 字符串 ID 修复新增 7 项回归，与既有 12 项 Swagger 测试共同通过；覆盖实际 JSON 序列化、
  继承字段、字段/getter 注解、元数据保留、
  泛型 Result 响应的生成链，以及普通数值、日期、布尔值和 Map 的 schema 不变。
- 动态 Map 修复追加 13 项测试，合计 32 项 Swagger 测试通过。覆盖 Member.extendInfo、
  嵌套 Map、泛型 Result Map、明确值类型、显式开关/值 schema/实现/引用/组合约束、
  类级和 getter 注解、已有元数据约束及非 Map 对象。字符串 ID 与既有 i18n 回归仍通过。
- 六份语言资源无重复 key；所有 Controller Swagger i18n 引用可按语言与默认资源回退解析。
- 459 条 JSON 响应示例经 Java Properties 转义还原后可解析。
- 在独立临时目录、独立端口启动 merged 实例，读取 client/admin/console 的 en-US 与 zh-CN
  六份 `/v3/api-docs` 文档，每种语言分别含 25/195/160 个操作，与 380 个映射一致。
  文档中无未解析的 i18n key、无悬空 schema 引用；已断言灰度规则别名、诊断 Header、分页约束、
  二进制下载、Console 状态 object/string 值映射及 11 个 multipart 文件必填声明。
  380 个操作的 x-nacos-api-since 扩展均为规范版本格式；临时实例已停止。
- 字符串 ID 修复后再次获取六份文档并逐字段比较：Client 两份文档完全一致；Admin/Console
  仅显式字符串 ID 的 type 由 integer 改为 string、移除 int64 format，包含列表和分页内联模型。
  其余文档字段完全一致，动态 Map 与同名模型的处理保持原状；结构和既有注解契约验证仍通过。
- 动态 Map 修复后，以字符串 ID 修复完成的六份文档为基线再次逐字段比较：仅 additionalProperties
  改变，120 处从 `{type: object}` 放宽为 true，6 处从空 schema `{}` 等价规范化为 true。
  Member/NacosMember.extendInfo 与 Result Map 均已核对；明确值类型、显式 schema、字符串 ID、
  同名模型及其余文档字段完全一致。结构检查与既有注解契约验证通过，临时实例已停止。
- 独立业务 HTTP IT 未执行；本轮未改变业务行为。

动态 Map 文档差异数量（含模型在列表、分页等位置的内联副本）：

| 文档组 | en-US | zh-CN |
|---|---:|---:|
| client-api | 16 | 16 |
| admin-api | 20 | 20 |
| console-api | 27 | 27 |

## 生成器问题与处理范围

以下问题在运行时的响应示例与 schema 交叉核对中发现，源于既有 schema 生成逻辑，
不是 Controller 的注解缺失。按后续确认，修复字符串 ID 和动态 Map 的文档类型；不能把本次注解覆盖通过
理解为所有响应模型 schema 都已完全正确。

1. **同名模型冲突**：`api.ai.model.agent.AgentVersionDetail` 与
   `api.ai.model.a2a.AgentVersionDetail` 都以简单类名写入 SchemaCache。
   Admin/Console 文档中的 RAD Agent 版本详情 schema 被旧 A2A 模型覆盖，丢失
   namespaceId、agentName、status、callInterfaces 等字段；Client 组没有该覆盖。
   按用户要求暂不处理，后续需在 Swagger 泛型定制器和 schema 缓存层处理命名及引用冲突。
2. **序列化类型未反映到 schema（已修复）**：ConfigBasicInfo 等模型的 id 使用
   `@JsonFormat(shape = STRING)`，响应示例正确使用字符串，但生成的属性仍为 integer。
   在 Nacos 泛型响应使用的默认转换链中注册 `NacosJsonFormatModelConverter`，仅对显式使用该注解的数值属性
   将文档类型改为 string 并清除数值 format。保留其他属性元数据，不改变业务模型、Jackson 配置
   或序列化结果，与 Config 资源规范的存储 ID 字符串要求一致。该链与 SpringDoc 按 OpenAPI 版本选择的
   转换链独立，因此仅通过 SpringDoc Bean 注册不足以修正泛型响应。修复集中在 swagger 模块，
   不改变 Web 配置、OpenAPI 版本选择或通用 Bean 复用规则。
3. **动态 Map 值被过度约束（已修复）**：Member.extendInfo 的实际值可为字符串、数字或布尔值，
   生成 schema 却将 Map 值限制为 object。例如 `{"version":"3.2.1","raftPort":7848,
   "readyToUpgrade":true}` 是合法业务数据，但当前 `additionalProperties: {type: object}`
   要求每个值都是 JSON 对象，无法表达 Java Object 允许的标量值。自由值 Map 应使用
   `additionalProperties: true` 或等价的无类型限制 schema。
   新增 `NacosMapModelConverter`，按 Java Map 的 Object 值类型识别动态 Map，仅放开自动推导的
   裸 object 值 schema；同时修正泛型 Result Map 的手工构造分支。通过比较序列化后的 schema
   约束识别裸 object，兼容 Swagger 在 ObjectSchema 与 Schema 之间的内部复制。
   Map 的明确值类型、显式开关、值 schema、实现、引用、组合约束和非裸 object 值 schema 均保持原状。
   业务模型、序列化和 Web 配置不变；同名模型冲突仍按用户要求暂缓。

Capacity 和 HealthChecker 基类返回子类字段属于多态核对项，未将其误报为响应示例的错误字段。

历史扫描和已修复问题见 Git 历史及 `doc/v3-swagger-api-changelog.md`。
此前待确认的默认鉴权插件 Controller 已按本轮明确范围排除。
