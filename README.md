# AI智能问诊医疗辅助平台

基于SpringBoot 3 + Vue 3 + 讯飞星火大模型开发的智能医疗辅助系统。

## 技术栈

### 后端
- SpringBoot 3.2.0
- MyBatis-Plus 3.5.5
- Sa-Token 1.37.0（权限认证）
- MySQL 8.0
- Redis（缓存）
- WebSocket（实时通信）

### 前端
- Vue 3.4
- Element Plus 2.7（UI组件库）
- Pinia（状态管理）
- Axios（HTTP请求）
- Vue Router 4（路由）
- ECharts 5.5（数据可视化）

### AI能力
- 讯飞星火大模型（SparkDesk）

## 功能模块

### 1. 工作台
- 今日数据统计（患者总数、今日问诊、AI诊断数、准确率）
- 问诊趋势图表
- 疾病类型分布
- 最近问诊记录

### 2. 患者管理
- 患者信息CRUD
- 患者档案管理
- 过敏史、病史记录

### 3. AI智能问诊
- 实时对话问诊
- AI智能诊断分析
- 诊断建议生成
- 诊断结果展示

### 4. 诊断记录
- 诊断记录查询
- 医生确认诊断
- 诊断历史追溯

### 5. 知识库管理
- 医疗知识录入
- 知识审核
- 知识检索

### 6. 数据统计
- 问诊趋势分析
- 疾病分布统计
- 医生工作量分析
- 诊断准确率统计

### 7. 系统设置
- 个人信息管理
- AI参数配置
- 安全设置
- 通知设置

## 项目结构

```
test_project/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/medical/
│   │   │   │       ├── config/        # 配置类
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── dto/           # 数据传输对象
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       ├── mapper/        # Mapper接口
│   │   │   │       ├── service/       # 服务层
│   │   │   │       └── common/        # 通用类
│   │   │   └── resources/
│   │   │       ├── application.yml    # 配置文件
│   │   │       └── db/
│   │   │           └── init.sql       # 数据库初始化脚本
│   │   └── test/
│   └── pom.xml                        # Maven配置
│
└── frontend/                   # 前端项目
    ├── src/
    │   ├── api/                # API接口
    │   ├── layout/             # 布局组件
    │   ├── router/             # 路由配置
    │   ├── stores/             # Pinia状态管理
    │   ├── utils/              # 工具函数
    │   ├── views/              # 页面组件
    │   ├── App.vue             # 根组件
    │   └── main.js             # 入口文件
    ├── index.html
    ├── package.json
    └── vite.config.js          # Vite配置
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 1. 数据库初始化

创建数据库并导入初始化脚本：

```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

### 2. 后端配置

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_medical
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

spark:
  api:
    app-id: your_app_id          # 讯飞星火AppId
    api-key: your_api_key        # 讯飞星火API Key
    api-secret: your_api_secret  # 讯飞星火API Secret
```

### 3. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将运行在 http://localhost:8080

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将运行在 http://localhost:3000

## 默认账号

- **管理员**：admin / admin123
- **医生**：doctor / doctor123

## 讯飞星火API申请

1. 访问 [讯飞开放平台](https://www.xfyun.cn/)
2. 注册并登录
3. 创建应用，选择"星火认知大模型"
4. 获取 AppId、API Key、API Secret
5. 将这些信息配置到 application.yml 中

## 主要功能截图

### AI智能问诊
- 支持多轮对话问诊
- AI智能分析症状
- 生成诊断建议
- 推荐检查项目

### 数据统计
- ECharts图表展示
- 多维度数据分析
- 趋势预测

## 开发说明

### 后端开发
- 遵循RESTful API设计规范
- 使用SaToken进行权限认证
- MyBatis Plus简化数据库操作
- 统一异常处理

### 前端开发
- 组件化开发
- 响应式设计
- 状态管理使用Pinia
- Axios拦截器统一处理请求

## 注意事项

1. 本系统仅供学习参考，不得用于实际医疗诊断
2. AI诊断结果仅供参考，不能替代专业医生诊断
3. 请妥善保管讯飞星火API密钥
4. 生产环境请修改默认密码

## License

MIT License
