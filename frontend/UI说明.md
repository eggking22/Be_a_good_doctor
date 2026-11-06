# 黑心医院模拟器前端 UI 说明

## 一、前端开发地址

启动前端开发服务器后，默认访问地址为：

- [前端首页](http://127.0.0.1:5173/)
- [诊所主页面](http://127.0.0.1:5173/game)

前端首页 `/` 会自动跳转到 `/game`。

启动命令：

```powershell
cd frontend
npm install
npm run dev
```

## 二、当前可访问的 UI 页面

| 页面 | 访问链接 | 当前状态 |
| --- | --- | --- |
| 前端首页 | [http://127.0.0.1:5173/](http://127.0.0.1:5173/) | 自动跳转到诊所主页面 |
| 诊所经营主页面 | [http://127.0.0.1:5173/game](http://127.0.0.1:5173/game) | 已完成第一人称诊室场景 |

## 三、诊所主页面可操作区域

访问 `/game` 后可以使用以下 UI：

- 诊室门：点击门接待患者。
- 患者流程：患者从门口进入，移动到办公桌前；点击“送患者离开”后返回门口。
- 接诊：打开接诊侧边面板，查看当前患者状态。
- 药房：打开药房入口面板，当前为功能占位。
- 员工：打开员工管理入口面板，当前为功能占位。
- 财务：打开财务账本入口面板，当前为功能占位。
- 扩建：打开诊所扩建入口面板，当前为功能占位。
- 关闭按钮：关闭右侧功能面板，回到诊室场景。

## 四、当前 HUD 信息

诊所主页面中始终显示：

- 诊所名称。
- 当前游戏天数。
- 现金余额。
- 今日收入。
- 当前声誉和声誉进度条。
- 当前季节。
- 游戏时间。
- 营业状态。

## 五、后端接口入口

前端通过 Vite 将 `/api` 请求代理到后端 `http://127.0.0.1:8080`。

以下链接是后端 API，不是独立 UI 页面：

- [后端健康检查](http://127.0.0.1:8080/api/health)
- `POST http://127.0.0.1:8080/api/messages/game/requests/startgameapi`
- `POST http://127.0.0.1:8080/api/messages/game/requests/getgamestateapi`
- `POST http://127.0.0.1:8080/api/messages/game/requests/advancegamedayapi`
- `POST http://127.0.0.1:8080/api/messages/game/requests/deletegameapi`

目前诊所主页面使用与后端 `GameState` 对齐的本地预览数据；后续接入真实游戏状态时，页面数据将通过上述 API 加载和保存。

## 六、页面代码位置

- 页面入口：[src/pages/ClinicHomePage/index.tsx](./src/pages/ClinicHomePage/index.tsx)
- 诊室场景：[src/pages/ClinicHomePage/components/ClinicRoomScene.tsx](./src/pages/ClinicHomePage/components/ClinicRoomScene.tsx)
- 页面交互状态：[src/pages/ClinicHomePage/hooks/useClinicHomePage.ts](./src/pages/ClinicHomePage/hooks/useClinicHomePage.ts)
- 路由配置：[src/router.tsx](./src/router.tsx)
