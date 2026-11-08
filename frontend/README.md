# 黑心医院模拟器前端

当前阶段完成开场主菜单、存档读取和第一人称诊所主页。前端通过后端游戏 API 创建、列出和完整读取本机存档，并在浏览器 `localStorage` 保存当前存档 ID。

## 前端启动步骤

在项目根目录执行：

```powershell
cd frontend
npm install
npm run dev
```

启动成功后访问：

- [前端首页](http://127.0.0.1:5173/)
- [诊所主页面](http://127.0.0.1:5173/game)

前端默认将 `/api` 请求代理到 `http://127.0.0.1:8080`。如果后端使用其他地址，可以设置环境变量：

```powershell
$env:VITE_API_PROXY_TARGET = "http://127.0.0.1:8080"
npm run dev
```

常用命令：

```powershell
npm run typecheck  # TypeScript 类型检查
npm run build      # 构建生产版本
npm run preview    # 预览生产构建结果
```

## 目录约定

- `src/system/api`：全局 API 消息、路由名称推导和请求发送基础设施。
- `src/apis/<service>`：按后端服务组织、面向前端的 API 消息类。
- `src/objects/<service>`：与后端领域对象对应的前端 TypeScript 类型。
- `src/objects/<service>/apiTypes`：API 专用响应类型。
- `src/pages/<Page>`：页面级代码；包含 `MainMenuPage` 与 `ClinicHomePage`，具体经营功能面板仍按阶段逐步开发。
- `src/components/ui`：暂不创建，只有真正复用的 UI 基础组件才放入这里。
- [UI 访问说明](./UI说明.md)：列出当前可访问页面、操作区域和后端接口入口。

前端 `src/objects` 与后端 `backend/src/main/scala/services/*/objects` 按服务和文件名一一对应。类型映射约定为：Scala `BigDecimal` 使用 TypeScript `number`，Scala `Option[T]` 使用 `T | null`，Scala `List[T]` 使用 `T[]`，Scala `OffsetDateTime` 使用 ISO 时间字符串；后端枚举在前端使用同名字符串联合类型和常量对象。

## 当前后端 API 对齐

API 名称由消息类名移除末尾 `Message` 后转小写，与后端 `RegisteredAPIMessage` 保持一致：

| 前端消息 | 请求路径 |
| --- | --- |
| `StartGameAPIMessage` | `/api/messages/game/requests/startgameapi` |
| `GetGameStateAPIMessage` | `/api/messages/game/requests/getgamestateapi` |
| `ListGameSavesAPIMessage` | `/api/messages/game/requests/listgamesavesapi` |
| `LoadGameAPIMessage` | `/api/messages/game/requests/loadgameapi` |
| `OpenBusinessDayAPIMessage` | `/api/messages/game/requests/openbusinessdayapi` |
| `AdvanceGameDayAPIMessage` | `/api/messages/game/requests/advancegamedayapi` |
| `DeleteGameAPIMessage` | `/api/messages/game/requests/deletegameapi` |

开发环境通过 Vite 将 `/api` 代理到 `http://127.0.0.1:8080`，可通过 `VITE_API_PROXY_TARGET` 覆盖。
