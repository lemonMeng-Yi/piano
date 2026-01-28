# 项目架构说明

## 推荐的分层架构

```
app/src/main/java/com/example/piano/
├── core/                          # 核心模块（共享的基础设施）
│   ├── network/                   # 网络层
│   │   ├── config/                # 网络配置
│   │   ├── interceptor/           # 拦截器
│   │   ├── model/                 # 网络模型（BaseResult 等）
│   │   └── util/                  # 网络工具类
│   ├── di/                        # 依赖注入模块
│   └── util/                      # 通用工具类（TokenManager 等）
│
├── data/                          # 数据层（Data Layer）
│   └── auth/                      # 认证功能的数据层
│       ├── api/                   # API 接口定义
│       │   ├── AuthApi.kt
│       │   ├── request/           # 请求模型
│       │   └── response/          # 响应模型
│       └── repository/            # Repository 实现
│           └── AuthRepositoryImpl.kt
│
├── domain/                        # 业务层（Domain Layer）
│   └── auth/                      # 认证功能的业务层
│       ├── model/                 # 业务实体（可选）
│       └── repository/            # Repository 接口
│           └── AuthRepository.kt
│
├── ui/                            # UI 层（Presentation Layer）
│   ├── auth/                      # 认证功能 UI
│   │   ├── screen/                # 认证相关页面
│   │   │   ├── LoginScreen.kt
│   │   │   ├── RegisterScreen.kt
│   │   │   └── ForgotPasswordScreen.kt
│   │   └── viewmodel/             # ViewModel
│   │       └── AuthViewModel.kt
│   │
│   ├── home/                      # 首页功能 UI
│   │   ├── screen/
│   │   │   └── HomeScreen.kt
│   │   └── viewmodel/
│   │       └── HomeViewModel.kt
│   │
│   ├── practice/                  # 练习功能 UI
│   │   ├── screen/
│   │   │   └── PracticeScreen.kt
│   │   └── viewmodel/
│   │       └── PracticeViewModel.kt
│   │
│   ├── progress/                  # 进度功能 UI
│   │   ├── screen/
│   │   │   └── ProgressScreen.kt
│   │   └── viewmodel/
│   │       └── ProgressViewModel.kt
│   │
│   ├── profile/                   # 个人资料功能 UI
│   │   ├── screen/
│   │   │   └── ProfileScreen.kt
│   │   └── viewmodel/
│   │       └── ProfileViewModel.kt
│   │
│   ├── common/                    # 通用 UI 组件
│   │   ├── components/            # 可复用组件
│   │   │   ├── AppText.kt
│   │   │   ├── AppSnackBarHost.kt
│   │   │   └── SnackBarManager.kt
│   │   └── theme/                # 主题相关
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   │
│   └── navigation/                # 导航相关
│       ├── NavRoutes.kt
│       ├── NavigationActions.kt
│       ├── AuthNavHost.kt
│       └── MainNavHost.kt
│
├── MainActivity.kt                 # 主 Activity
└── PianoApplication.kt            # Application 类
```

## 架构原则

### 1. 清晰的分层
- **Core**: 基础设施（网络、DI、工具类）
- **Data**: 数据源（API、Repository 实现）
- **Domain**: 业务逻辑（Repository 接口、业务实体）
- **UI**: 界面展示（Screen、ViewModel）

### 2. 按功能模块组织
每个功能（auth、home、practice 等）都有独立的：
- Data 层实现
- Domain 层接口
- UI 层展示

### 3. 依赖方向
```
UI → Domain ← Data
     ↑
   Core
```

- UI 层依赖 Domain 层（接口）
- Data 层实现 Domain 层接口
- Core 层被所有层使用

## 扩展新功能的步骤

### 示例：添加"设置"功能

1. **Data 层** (`data/settings/`)
   ```kotlin
   data/settings/api/SettingsApi.kt
   data/settings/repository/SettingsRepositoryImpl.kt
   ```

2. **Domain 层** (`domain/settings/`)
   ```kotlin
   domain/settings/repository/SettingsRepository.kt
   ```

3. **UI 层** (`ui/settings/`)
   ```kotlin
   ui/settings/screen/SettingsScreen.kt
   ui/settings/viewmodel/SettingsViewModel.kt
   ```

4. **更新 DI** (`core/di/`)
   ```kotlin
   // 在 AppModule 中添加 SettingsApi 和 SettingsRepository 的绑定
   ```

5. **更新导航** (`ui/navigation/`)
   ```kotlin
   // 在 NavRoutes 中添加路由
   // 在 MainNavHost 中添加页面
   ```

## 优势

1. **清晰的分层**：每层职责明确
2. **易于扩展**：新功能按模块添加
3. **易于测试**：可以 Mock Domain 层接口
4. **易于维护**：相关代码集中在一起
5. **团队协作**：不同开发者可以并行开发不同模块
