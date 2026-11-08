# AI 诊所经营模拟器后端

当前目录是按参考项目规范生成的模块化单体后端。阶段 2 已补全 PostgreSQL 持久化、外键、级联删除、事务边界和恢复测试。

## 分层约定

- `app`：应用启动、健康检查和总路由入口。
- `services/system`：统一 API Message、错误处理、数据库连接和事务基础设施。
- `services/<domain>/api`：请求消息和业务编排入口。
- `services/<domain>/objects`：领域对象、请求对象、响应对象和参数对象。
- `services/<domain>/routes`：注册本领域对外暴露的 API Message。
- `services/<domain>/tables`：每张物理表一个独立文件夹，包含 `XXTable.scala` 和 `XXTableInitializer.scala`；Table 只负责封装 SQL，初始化器只负责本表结构、索引和外键。
- `services/<domain>/calculators`：纯业务公式。它是当前项目保留的额外目录，原因是计算规则与内部工具需要明确区分，计算器方法仅对所属服务可见。
- `services/<domain>/utils`：解析器、适配器和领域辅助工具。

`objects` 目录遵循“一文件一个主要对象”的约定；同名伴生 `object` 只用于 Codec、常量或简单构造。患者的四个经营属性集中在独立的 `PatientAttributes.scala` 中。

`GameState` 是游戏会话聚合根，组合 `FinanceState`、`Reputation`、`Clinic` 和 `Inventory`。患者、接诊和事件按数据库表独立保存，由 `GameStateOperations` 按需加载；不要把数据库集合全部复制到 `GameState`。

## API 约定

```text
POST /api/messages/{service}/requests/{api-name}
```

API 名称由 Message 类型统一推导，Routes 只登记服务级 API 列表。查询 API 使用只读连接，创建、更新、删除和跨表流程使用事务。

## 数据库与持久化

默认连接参数为：`127.0.0.1:5432/ai_clinic_simulation`，用户名 `db`，密码 `root`。也可以通过 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD` 环境变量覆盖。

应用启动时由 `DatabaseSchemaInitializer` 按外键依赖顺序调用各表所属的 `XXTableInitializer`。初始化器使用 `IF NOT EXISTS` 和兼容性 `ALTER TABLE`，不删除或重建用户数据。

症状、疾病和药品目录由所有游戏共享：`disease_symptoms` 和 `disease_medicines` 分别保存疾病与症状、疾病与药品的多对多关联。启动时会补齐三组游戏示例：流鼻涕、嗓子疼 → 感冒 → 感冒药；发热、咳嗽、头痛 → 流感 → 流感对症药；发热、咳嗽、胸闷 → 重度呼吸道感染 → 呼吸道特效药。示例数据定义在 `services/pharmacy/utils/InitialMedicalCatalog.scala`，疾病难度覆盖患者属性使用的 1、3、5。

感冒药的示例初始采购价为 10、售价为 20，定义在 `services/pharmacy/utils/InitialMedicinePrices.scala`，仅用于游戏数值。初始化按编号补缺，不会覆盖数据库中已有的名称或价格；调整已入库的价格应更新数据库中的药品记录。示例药品只是共享目录，不会自动加入任意游戏的库存。

第一阶段接诊由 `StartConsultationAPIMessage` 提供：它校验患者归属，按患者疾病难度抽取隐藏疾病，生成并持久化病例症状快照，再创建接诊记录。病例快照保存当时的疾病名称、难度和症状名称，后续修改目录数据不会影响历史病例；响应只返回公开的患者和接诊信息。

游戏删除只删除 `game_sessions`，其员工、设施、患者、接诊、对话、库存、事件、声誉历史、流水和日结算由外键级联删除；药品目录不属于某一局游戏，库存清除后才允许删除药品。

## 当前状态

- 已生成应用层、统一消息路由、数据库会话和各业务模块目录。
- `game` 服务已提供最小可用的开始游戏、查询状态和推进经营日 API。
- 经济、药房、患者、接诊、诊所、声誉和事件服务已生成领域对象、计算器和完整表访问入口；历史记录采用追加和查询接口。
- `GameStateOperations.loadComplete` 会恢复诊所、库存、患者、接诊及对话、事件、声誉历史、日结算和现金流水；数据库异常会直接传播，不会静默替换为空列表。
- `src/test/scala/Stage2PersistenceSuite.scala` 覆盖 CRUD、外键、级联删除、完整加载和事务回滚场景。
- `frontend/README.md` 是前端占位文件，暂不创建前端页面。
