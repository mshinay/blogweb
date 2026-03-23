# blog-back-end

## 1. 项目简介

这是一个基于 Spring Boot 3 的博客后端项目，目标不是堆砌博客功能，而是把一个偏课设风格的后端改造成一个更适合展示工程能力、业务建模能力和架构演进思路的内容系统。

技术栈：

- Java 17
- Spring Boot 3
- MyBatis
- MySQL
- Redis
- Maven
- JWT
- Lombok

核心功能：

- 用户注册、登录、资料查看与基础权限控制
- 文章创建、编辑、发布、删除与状态流管理
- 分类与标签管理，支持文章筛选
- 评论树结构与回复链路
- 文章详情聚合与统计能力

---

## 2. 项目亮点

- 架构分层清晰：按 `common / pojo / server` 拆分模块，公共能力、数据模型、业务实现职责明确。
- 数据模型不是简单 CRUD：文章、评论、统计拆表，分类单选、标签多对多、评论树字段语义独立设计。
- 业务边界明确：前台公共查询、作者侧操作、管理员管理接口不是混用一套模糊逻辑，而是按权限和状态分别收口。
- 工程化能力补齐：参数校验、统一异常处理、密码加密、JWT 鉴权、配置外置，不再是“能跑就行”的课设式实现。
- 更贴近真实后端：文章详情由 service 聚合文章、作者、分类、标签、统计、评论，而不是把复杂聚合硬塞进 mapper。

与普通博客项目的区别：

- 不只做文章 CRUD，而是强调状态流、权限边界和聚合查询。
- 评论不是平铺列表，而是支持评论树和回复链路。
- 统计单独拆表，为缓存、榜单、异步回写预留演进空间。
- 项目文档、SQL 设计和实现顺序是按工程演进思路规划的。

---

## 3. 技术栈

后端：

- Java 17
- Spring Boot 3.4.5
- Spring Web
- Spring Validation
- Spring Data Redis
- MyBatis
- PageHelper
- JWT
- Lombok

数据库：

- MySQL 8

中间件：

- Redis

前端（如果有）：

- 当前仓库为后端项目
- 前端对接约定可参考 `前后端对齐接口文档.md`

---

## 4. 项目结构

```text
blog-back-end
├── blog-common
│   └── 公共常量、上下文、异常、统一返回结构、配置类、工具类
├── blog-pojo
│   └── 实体类、DTO、VO
├── blog-server
│   ├── controller
│   ├── service
│   ├── mapper
│   ├── resources
│   └── test
├── builder
│   └── Builder 过程文档与任务卡
├── planner
│   └── Planner 快照与架构规划文档
├── blog-complete.sql
├── 说明文档.md
└── 前后端对齐接口文档.md
```

模块职责：

- `blog-common`：放公共基础设施，如常量、异常、上下文、统一响应模型、工具类。
- `blog-pojo`：放数据模型，包括实体类、请求 DTO、返回 VO。
- `blog-server`：核心业务模块，包含控制层、服务层、持久层和运行配置。
- `builder`：记录 Builder 执行过程和任务拆分结果。
- `planner`：记录架构规划、阶段快照和任务决策。

---

## 5. 数据库设计（简化版）

表列表：

- `user`
- `category`
- `tag`
- `article`
- `article_tag`
- `comment`
- `article_stats`

核心关系：

- 一个用户可以发布多篇文章
- 一篇文章属于一个分类
- 一篇文章可以关联多个标签
- 一篇文章可以有多条评论
- 评论通过 `parent_id / root_id / reply_to_comment_id` 组织为评论树
- 一篇文章对应一条统计记录

设计思路：

- 文章主表只保留主体内容和状态字段，统计信息拆到 `article_stats`，便于后续接入 Redis。
- 分类使用单分类设计，标签使用多对多设计，兼顾简单性和扩展性。
- 评论表从数据层支持多级回复，接口层当前优先服务“两层展示”场景。
- 采用逻辑外键，由应用层做存在性校验，减少数据库层耦合。

---

## 6. 核心功能

用户：

- 用户注册、登录
- JWT 鉴权
- 查看用户公开资料
- 查看当前登录用户的评论历史

文章：

- 创建文章
- 编辑文章
- 作者切换草稿 / 发布
- 管理员切换发布 / 删除
- 前台文章列表、文章详情、后台文章管理查询

评论：

- 发布评论
- 回复评论
- 评论树查询
- 删除评论与回复链处理
- 管理员评论状态管理

分类 / 标签：

- 分类列表查询
- 标签列表查询
- 文章绑定分类和标签
- 支持按分类、标签筛选文章

---

## 7. 快速开始

环境要求：

- JDK 17
- Maven 3.9+
- MySQL 8+
- Redis 6+

数据库初始化：

1. 创建数据库并导入 SQL：

```bash
mysql -u root -p < blog-complete.sql
```

2. 根据本地环境准备配置：

- 参考 `blog-server/src/main/resources/application-local.example.yml`
- 不要提交真实数据库密码、JWT 密钥、OSS 配置

启动步骤：

1. 先安装父工程和共享模块：

```bash
mvn clean install
```

2. 启动服务：

```bash
mvn -f blog-server/pom.xml spring-boot:run
```

3. 运行测试：

```bash
mvn -f blog-server/pom.xml test
```

默认说明：

- 服务模块配置位于 `blog-server/src/main/resources/`
- 开发环境主要使用 `application.yml` 和 `application-dev.yml`
- 如需本地私有配置，建议使用未跟踪的本地配置文件或环境变量

---

## 8. API 示例

1. 用户登录

```http
POST /users/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

2. 获取文章列表

```http
GET /articles?page=1&pageSize=10&categoryId=1&tagId=2
```

3. 获取文章详情

```http
GET /articles/1
```

典型接口特点：

- 前台详情接口返回文章主体、作者、分类、标签、统计、评论树聚合数据
- 作者侧和管理员侧接口有不同的状态与权限边界
- 统一返回结构和异常处理已收口

---


