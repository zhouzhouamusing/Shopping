# 潮购商城 - 电商购物平台

基于 **SpringBoot + Vue3 + MySQL** 的前后端分离电商购物平台。

## 技术栈

### 后端
- **Spring Boot 3.2** - Web框架
- **Spring Data JPA** - 数据持久化
- **Spring Security** - 安全认证
- **JWT (jjwt 0.12)** - 无状态Token认证
- **MySQL 8.0** - 关系型数据库
- **Lombok** - 代码简化
- **SpringDoc** - API文档

### 前端
- **Vue 3** - 渐进式JavaScript框架
- **Vite 5** - 构建工具
- **Vue Router 4** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI组件库
- **Axios** - HTTP客户端
- **SCSS** - CSS预处理器
- **Animate.css** - 动画库

## 项目结构

```
Shopping/
├── backend/                    # 后端项目
│   ├── src/main/java/com/shopping/
│   │   ├── config/            # 配置类（Security、Web、全局异常处理）
│   │   ├── controller/        # REST控制器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # JPA实体类
│   │   ├── repository/        # 数据访问层
│   │   ├── security/          # JWT安全组件
│   │   ├── service/           # 业务逻辑层
│   │   └── ShoppingApplication.java
│   ├── src/main/resources/
│   │   └── application.yml    # 应用配置
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── layouts/           # 布局组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia状态管理
│   │   ├── styles/            # 全局样式
│   │   ├── utils/             # 工具类（API封装）
│   │   ├── views/             # 页面组件
│   │   │   ├── admin/         # 后台管理页面
│   │   │   ├── Login.vue      # 登录
│   │   │   ├── Register.vue   # 注册
│   │   │   ├── Home.vue       # 首页
│   │   │   ├── Products.vue   # 商品列表
│   │   │   ├── ProductDetail.vue  # 商品详情
│   │   │   ├── Cart.vue       # 购物车
│   │   │   ├── Orders.vue     # 订单列表
│   │   │   └── OrderDetail.vue    # 订单详情
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
└── database/
    └── init.sql               # 数据库初始化脚本
```

## 功能模块

| 模块 | 功能说明 |
|------|---------|
| 用户认证 | 注册、登录、JWT Token鉴权 |
| 商品展示 | 首页轮播、分类浏览、搜索、排序、分页 |
| 商品详情 | 图片展示、价格信息、库存、加入购物车 |
| 购物车 | 增删改查、数量修改、全选、实时计价 |
| 订单管理 | 创建订单、订单列表、订单详情、模拟支付、取消 |
| 后台管理 | 商品CRUD、订单管理、发货操作 |

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### 2. 数据库初始化

```sql
-- 执行 database/init.sql
mysql -u root -p < database/init.sql
```

### 3. 启动后端

```bash
cd backend

# 修改 src/main/resources/application.yml 中的数据库连接配置
# 确保MySQL已启动且shopping_db数据库已创建

mvn spring-boot:run
```

后端启动后访问：http://localhost:8080
API文档：http://localhost:8080/swagger-ui.html

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：http://localhost:5173

### 5. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | 自行注册 | - |

## API接口

### 公开接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/products` - 商品列表
- `GET /api/products/{id}` - 商品详情
- `GET /api/products/hot` - 热门商品
- `GET /api/products/new` - 最新商品
- `GET /api/categories` - 分类列表

### 需要登录
- `GET /api/auth/info` - 用户信息
- `GET/POST/PUT/DELETE /api/cart` - 购物车操作
- `GET/POST /api/orders` - 订单操作
- `PUT /api/orders/{orderNo}/pay` - 支付
- `PUT /api/orders/{orderNo}/cancel` - 取消

### 管理员接口
- `GET/POST/PUT/DELETE /api/admin/products` - 商品管理
- `GET /api/admin/orders` - 订单管理
- `PUT /api/admin/orders/{orderNo}/deliver` - 发货

## 页面预览

- **登录/注册页** - 暗色科技感背景，粒子动画，渐变光效
- **首页** - Hero大图，分类导航，热门/最新商品瀑布流
- **商品列表** - 网格布局，多维排序，悬浮动画
- **商品详情** - 左图右信息布局，价格高亮，服务保障
- **购物车** - 勾选/数量/小计实时联动
- **后台** - 侧边栏布局，表格管理，弹窗编辑

## 设计特点

- 现代化UI设计，科技感视觉风格
- 丰富的CSS动画效果（fadeInUp、scaleIn、float、gradient-shift）
- 完整的响应式布局，适配移动端
- 深色渐变登录页面，视觉重心明确
- Element Plus组件深度定制
- 统一的设计语言和色彩系统
