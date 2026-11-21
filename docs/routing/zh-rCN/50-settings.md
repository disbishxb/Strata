# 50 设置

> 元规则：改代码后更新本册字节数与条目；用 `stat -c '%s %n'` 核对。字节单位 byte。

## A. fork 与 1.0.0 相同的逻辑

### A.1 文件路由表
| 路径 | 1.0.0 | fork | 职责 |
|---|---|---|---|
| `feature/settings/SettingsScreen.kt` | 22672 | 23904 | 设置首页/搜索/导航；SettingsPage/SettingsEntry/SettingsIndex；SettingsActions |
| `feature/settings/SettingsPages.kt` | 37458 | 55798 | 各设置页：FileList/Behavior/Operations/Reader/Editor/Apps/Access/Opening/Backup/About；fork 多 Install 后端/优先级/第三方 |
| `feature/settings/AppearancePage.kt` | 18684 | 18684 | 外观：主题模式/纯黑/壁纸色/自定义色/调色板/对比度/字号/角/图标形状/彩色图标/动画/触感/声音 |
| `feature/settings/SecurityPages.kt` | 13566 | 13657 | 安全：应用锁/延迟/锁屏/保险库/安全屏幕；语言页 |
| `feature/settings/ToolbarPage.kt` | 19353 | 19353 | 工具栏分区编辑：浏览/选择/长按 |
| `feature/settings/ZoneEditor.kt` | 23032 | 23032 | 拖拽分区编辑器：ZoneStyle/ZoneButton/ZoneDrag |
| `feature/settings/SettingsComponents.kt` | 15609 | 15609 | 设置组件：SettingsPageScaffold/SettingsGroup/switch/link/choice/pick/slider/SettingNote/ConnectedChoice/Tint |
| `feature/settings/SupportPage.kt` | 4756 | 8849 | 支持页；fork 增二维码/原作者/fork 维护者 |

### A.2 工作逻辑
- **入口**：SettingsScreen 持 page 状态；SettingsHome 搜索 + 分组；SettingsIndex.search 中英匹配。
- **分发**：when(page) 到各 Page；BackHandler 回首页。
- **SettingsActions**：appearance/list/behavior/operations/security/editor/reader/actions；run { repo.block() }。
- **设置组件**：SettingsPageScaffold（大顶栏 + 滚动 + snackbar）；SettingsGroup（SegmentedListItem）；switch/link/choice/pick/slider。
- **ZoneEditor**：Zoned 分区；拖拽/上下移/移入移出/溢出回调。
- **各页**：FileList/Behavior/Operations/Reader/Editor/Apps/Access/Opening/Backup/About。
- **外观页**：ThemePreview、SeedSwatches、StyleChips、IconShapes、TextSize 滑块。
- **安全页**：AppLock 开关/延迟/锁屏；保险库设置/解锁/改密/删除；安全屏幕。
- **支持页**：wallet 列表 + 复制。
- **备份页**：导出/导入/重置。

## B. fork 独有的逻辑

### B.1 新增文件路由表
| 路径 | fork | 职责 |
|---|---|---|
| `feature/settings/DrawerBoxPage.kt` | 12877 | 抽屉盒设置页：SidebarPreview + ZoneEditor |
| `feature/settings/StartLocationPage.kt` | 4505 | 启动位置页：左/右窗选择与自定义路径 |

### B.2 新增/改动逻辑
- **SettingsScreen**：`SettingsPage` 新增 `DRAWER_BOX`、`START_LOCATION`、`GRAMMAR`；`SettingsIndex.home` 新增 DRAWER_BOX；`SettingsIndex.entries` 新增 drawer_box/grammar/start_location 相关；搜索支持英文兜底。
- **SettingsPages**：`AppsPage` 新增安装后端开关/优先级拖拽/第三方安装器；`AccessPage` 新增 Dhizuku；`BehaviorPage` 链到 `StartLocationPage`；`FileListPage`/`OperationsPage`/`EditorSettingsPage` 等有增补。
- **SupportPage**：fork 增二维码、原作者/fork 维护者分组、保存到相册。
- **AppearancePage**：无结构性新增，但依赖 fork 的 `KindColors`/`Stgx`。
- **SecurityPages**：无结构性新增，但依赖 fork 的 `VaultAccess`。
- **ToolbarPage**：无结构性新增。
- **ZoneEditor**：无结构性新增。
- **SettingsComponents**：无结构性新增。

## C. 1.0.0 独有、fork 已删或改掉
- 1.0.0 无 `DrawerBoxPage.kt`、`StartLocationPage.kt`。
- 1.0.0 `SettingsPage` 无 DRAWER_BOX/START_LOCATION/GRAMMAR。
- 1.0.0 `AppsPage` 无安装后端/优先级/第三方安装器。
- 1.0.0 `AccessPage` 无 Dhizuku。
- 1.0.0 `SupportPage` 无二维码/原作者/fork 维护者。