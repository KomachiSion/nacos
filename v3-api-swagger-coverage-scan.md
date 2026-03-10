# Nacos v3 HTTP API Swagger 注解覆盖扫描

扫描时间：按当前代码库状态。  
用途：找出**完全未加 Swagger** 的 v3 Controller，以及**已有 @Tag 但可能部分接口缺 @Operation** 的类，便于先小范围试用 skill 再优化。

---

## 一、完全无 Swagger 注解的 v3 Controller（建议优先试用 skill）

以下类**没有 @Tag**，整类均未按现有规范添加 Swagger 注解，适合作为「先补全一个类」的试跑范围。

| 模块 | 类路径 | 约 API 数 | 说明 |
|------|--------|-----------|------|
| **plugin-auth** | `plugin-default-impl/.../controller/v3/UserControllerV3` | 7 | 用户创建/登录/admin/删除/更新/list/search |
| **plugin-auth** | `plugin-default-impl/.../controller/v3/RoleControllerV3` | 4 | 角色 CRUD、list、search |
| **plugin-auth** | `plugin-default-impl/.../controller/v3/PermissionControllerV3` | 4 | 权限 CRUD、list、get |

**已从本表移出（已补全 Swagger）**  
- **console/ai**：ConsoleSkillController、ConsolePromptController、ConsoleCopilotController、ConsoleCopilotConfigController（见第二节；ConsoleCopilotConfigController 另有特殊说明见第五节）。  
- **ai (admin)**：PromptAdminController、PromptClientController。

**i18n 归属**  
- **console** 下所有：`console/src/main/resources/i18n/console_messages*.properties`  
- **core / config / ai / plugin**：`server/src/main/resources/i18n/server_messages*.properties`  

**建议试跑顺序（小范围）**  
1. **plugin-auth**：如 `RoleControllerV3`（4 个 API），参考 server_messages 中已有 auth 相关 key。

---

## 二、已有 @Tag、且各接口均有 @Operation 的 v3 Controller（仅作对照）

以下类已有完整 Swagger（类有 @Tag，接口有 @Operation），**无需补全**（带特殊说明的见第五节），可作为同模块参考：

- **console**：ConsoleNamespaceController、ConsoleServiceController、ConsoleInstanceController、ConsoleClusterController、ConsoleHistoryController、ConsoleConfigController、ConsoleMcpController、ConsoleA2aController、ConsoleServerStateController、ConsoleHealthController、ConsolePluginController、**ConsoleSkillController**、**ConsolePromptController**、**ConsoleCopilotController**、**ConsoleCopilotConfigController**（后者见第五节特殊说明）  
- **core**：ServerLoaderControllerV3、ServerStateController、NamespaceControllerV3、NacosClusterControllerV3、CoreOpsControllerV3、**PluginControllerV3**（见第五节特殊说明）  
- **config**：ConfigControllerV3、MetricsControllerV3、ListenerControllerV3、HistoryControllerV3、ConfigOpsControllerV3、ConfigOpenApiController、CapacityControllerV3  
- **naming**：ServiceControllerV3、OperatorControllerV3、InstanceOpenApiController、InstanceControllerV3、HealthControllerV3、ClusterControllerV3、ClientControllerV3  
- **ai (admin)**：McpAdminController、A2aAdminController、SkillAdminController、**PromptAdminController**、**PromptClientController**  

若后续在某个类上发现**新增了接口但未加 @Operation**，可把该类归入「部分缺失」再按 skill 补全。

---

## 三、未纳入本次 v3 范围的 Controller

- **OidcLoginController**：路径为 `/v1/auth/oidc`，非 v3，未纳入。  
- **PrometheusController / McpRegistryController**：未按 v3 路径扫描，若实际暴露 v3 API 可后续单独纳入。  
- **UserController**（非 v3 包）：兼容注解里提到 v3 auth login，但类不在 `controller/v3` 下，未计入上表；若需统一 v3 文档可单独处理。

---

## 四、小结与建议

- **缺 Swagger 的 v3 Controller**：共 **3 个类**（均为 plugin-auth：UserControllerV3、RoleControllerV3、PermissionControllerV3），约 **15 个 API**。  
- **已完成并归入第二节**：ConsolePluginController、PluginControllerV3、SkillAdminController；以及本次补全的 **ConsoleSkillController**、**ConsolePromptController**、**ConsoleCopilotController**、**ConsoleCopilotConfigController**、**PromptAdminController**、**PromptClientController**。其中 PluginControllerV3、ConsoleCopilotConfigController 见第五节特殊说明，待接口改为 form 后可再完善 Swagger。  
- **建议**：对剩余 **plugin-auth** 中的 1 个类（如 `RoleControllerV3`）用当前 skill 做一次完整补全（含 i18n），看效果后再扩大范围。

---

## 五、特殊说明（待改 API 形态后再完善 Swagger）

以下 Controller 已加 Swagger，但部分接口为 **RequestBody（JSON）**，无特殊业务理由，**建议改为 form 表单 API**，与 v3 常规风格一致；改完后再视情况调整/补全 Swagger。

| Controller | 说明 |
|------------|------|
| **PluginControllerV3** | 部分接口为 RequestBody 的 JSON API，应改为 form 表单 API，待修改完成后继续完善 Swagger。 |
| **ConsoleCopilotConfigController** | 同上：POST 配置当前为 `@RequestBody CopilotProperties`，应改为 form 表单 API，待修改完成后继续完善 Swagger。 |
