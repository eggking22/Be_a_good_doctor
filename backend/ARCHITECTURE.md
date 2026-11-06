# 后端架构清单

```text
backend/src/main/scala/
├── app/
│   ├── Main.scala
│   ├── ApiRouter.scala
│   └── HealthRouter.scala
│
└── services/
    ├── system/          # API Message、错误、数据库和事务基础设施
    ├── game/            # 游戏状态和经营日流程编排
    ├── economy/         # 现金流、工资、利润和破产规则
    ├── patient/         # 患者生成、患者生命周期和队列
    ├── consultation/    # 接诊、对话会话、情绪和治疗方案评价
    ├── pharmacy/        # 药品、采购、库存和过期处理
    ├── reputation/      # 声誉计算和患者生态分布
    ├── event/           # 随机事件、风险和季节
    ├── clinic/          # 诊所、员工、保安和设施
    ├── ai/              # LLM、Prompt 和记忆能力适配层
    └── voice/           # ASR、TTS 和音频流适配层
```

`calculators/` 是本项目保留的额外服务目录，用于存放纯计算规则；它与 `utils/` 的区别是前者表达业务公式，后者只存放服务内部辅助函数。两者的方法均限制在所属服务包内。

## 业务边界

- `game` 负责跨领域编排，不直接替代经济、库存或声誉规则。
- `economy` 负责现金账本和结算；`pharmacy` 负责库存资产，采购时由经济服务记录现金流。
- `consultation` 可以调用 `ai` 和 `voice`，但 AI 只返回对话/评价结果，不能直接修改游戏资产。
- `event` 负责事件生成和事件结果；事件对现金、声誉、员工或设施的影响通过对应领域规则执行。
- `clinic` 负责员工和设施数据，安保能力是诊所经营状态的一部分。
- 复杂游戏流程由 `game/utils/GameStateOperations` 按服务内部可见性承载，API Message 直接调用它，避免增加只负责转发的 Manager 层。
- `objects` 中每个 Scala 文件只定义一个主要领域对象；患者属性单独定义为 `PatientAttributes`。
- `tables` 中一个物理数据库表对应一个独立文件夹，文件夹内包含 Table 定义和 TableInitializer；`StaffTable`、`FacilityTable`、`MedicineTable`、`InventoryTable` 和 `DialogueMessageTable` 不再合并到其他 Table。
- `services/system/database/DatabaseSchemaInitializer` 只负责编排，各表的建表、索引和外键由所属 TableInitializer 维护。

## 当前核心对象关系

```text
GameState
├── FinanceState
│   ├── CashFlow
│   └── DailyStatement
├── Reputation
├── Clinic
│   ├── Facility
│   └── List[Staff]
├── Inventory
│   └── List[InventoryItem]
```

固定状态使用枚举，例如 `StaffRole`、`PatientEmotion` 和 `ConsultationStatus`；
安保能力和员工满意度由 `calculators` 根据 `Clinic` 中的员工推导，不在 `GameState` 中重复保存。

患者、接诊和事件是按游戏会话和经营日查询的数据库集合，不直接放入 `GameState`：

```text
patients        → PatientTable
consultations   → ConsultationTable
game_events     → EventTable
reputation历史  → ReputationTable
```

`GameSessionRecord` 只对应 `game_sessions` 表的一行；`GameStateOperations` 负责把持久化记录初始化为运行时 `GameState`。

## 持久化与运行时边界

```text
数据库持久化：
game_sessions、staff、facilities、medicines、medicine_inventory、patients、
consultations、dialogue_messages、game_events、reputation_records

运行时组合：
GameState、FinanceState、Clinic、Inventory、Consultation 的业务过程对象
```

`Consultation` 不保存 `gameId`；`consultations.game_id` 只作为数据库关联字段。

## 统一调用链

```text
HTTP POST
  ↓
app.ApiRouter
  ↓
services.system.api.APIMessageRouter
  ↓
services.<domain>.routes.*Routes
  ↓
services.<domain>.api.*APIMessage
  ↓
tables / calculators / utils
  ↓
PostgreSQL 或外部 AI/语音适配器
```

## 预留的 MVP API 方向

```text
game          get-game-state / advance-day / settle-day
economy       get-cash-flow / get-daily-statement
patient       list-patients / generate-patient
consultation  start-consultation / send-dialogue / evaluate-treatment
pharmacy      list-medicines / purchase-medicine / get-inventory
reputation    get-reputation
event         list-events / resolve-event
clinic        list-staff / hire-staff / list-facilities / upgrade-facility
```

当前已实现：

```text
POST /api/messages/game/requests/startgameapi
POST /api/messages/game/requests/getgamestateapi
POST /api/messages/game/requests/advancegamedayapi
```

其他接口目前只完成领域对象、计算器和数据访问边界，后续再逐步开放。
