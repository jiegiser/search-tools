# SearchTools - 全网盘资源搜索引擎

一个基于 Spring Boot + React 的全网盘资源搜索引擎，支持搜索百度网盘、阿里云盘、夸克网盘等12种网盘的资源。通过搜索引擎（百度/必应）爬取全网资源链接，自动提取网盘链接和提取码。

## 功能特性

- **全网搜索**：搜索时自动触发百度/必应全网爬取，获取真实可用的网盘资源
- **多网盘支持**：百度网盘、阿里云盘、夸克网盘、迅雷网盘、天翼云盘、UC网盘、115网盘、123网盘、微云、蓝奏云、MEGA等12种网盘
- **Lucene全文搜索**：支持中文搜索、特殊字符转义、WildcardQuery通配符查询
- **链接验证**：自动验证网盘链接有效性
- **资源管理**：系统管理页面可查看所有已爬取的资源
- **52pojie爬虫**：支持爬取52pojie论坛资源并邮件通知

## 环境要求

- **JDK 17+**（必须，不支持JDK 8）
- **Node.js 18+**
- **Maven 3.8+**

## 快速开始

### 1. 启动后端

```bash
cd backend

# Windows PowerShell 设置JAVA_HOME（如果系统默认不是JDK 17）
$env:JAVA_HOME = "D:\Java\jdk-17.0.18"

# 编译并运行测试
mvn clean test

# 启动后端服务
mvn spring-boot:run
```

后端将在 `http://localhost:8081` 启动。首次启动会自动创建SQLite数据库 `searchtools.db`。

### 2. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5174` 启动（如果5173被占用会自动递增端口）。

### 3. 使用搜索

1. 打开浏览器访问 `http://localhost:5174`
2. 在搜索框输入关键词（如 "Java"、"Python教程"、"C++"）
3. 点击搜索按钮，系统会：
   - 先搜索本地已爬取的资源（Lucene全文索引 + SQLite数据库）
   - 如果本地无结果，自动触发百度 + 必应全网爬取
   - 爬取完成后立即返回结果
4. 后续搜索相同关键词可直接从本地获取，速度更快

## 使用方法

### 搜索资源

1. 打开浏览器访问 `http://localhost:5174`
2. 在搜索框输入关键词（如"Java高手速成"、"现代C++编程实战"）
3. 点击搜索按钮或按回车
4. 查看搜索结果，包含资源标题、网盘类型、链接和提取码

### API接口

#### 搜索相关

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/search?keyword=xxx&page=0&pageSize=20` | GET | 搜索资源（无本地结果自动爬取全网） |
| `/api/resources/{id}` | GET | 获取资源详情（自动增加点击计数） |
| `/api/resources/popular?page=0&pageSize=10` | GET | 获取热门资源（按点击量排序） |
| `/api/resources/latest?page=0&pageSize=10` | GET | 获取最新资源（按创建时间排序） |
| `/api/resources/pan/{type}?page=0&pageSize=20` | GET | 按网盘类型查询（BAIDU/ALIYUN/QUARK等） |
| `/api/resources/all?page=0&pageSize=20` | GET | 获取所有资源（分页） |
| `/api/keywords/hot?limit=10` | GET | 获取热门搜索关键词 |

#### 系统状态

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/status` | GET | 系统状态（资源数、爬取任务、搜索统计等） |
| `/api/health` | GET | 健康检查 |

#### 爬取管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/crawl` | POST | 爬取指定网页 |
| `/api/crawl/batch` | POST | 批量爬取 |
| `/api/crawl/search-engine` | POST | 通过搜索引擎爬取（baidu/bing） |
| `/api/crawl/history` | GET | 爬取历史 |
| `/api/crawl/running` | GET | 运行中的任务 |
| `/api/crawl/stats` | GET | 任务统计 |
| `/api/crawl/validate-link` | POST | 验证网盘链接有效性 |
| `/api/crawl/validate-all` | POST | 批量验证所有资源链接 |
| `/api/crawl/preview/{id}` | GET | 资源预览 |
| `/api/crawl/distributed/submit` | POST | 提交分布式爬取任务 |

#### 52pojie爬虫

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/crawl/pojie/start` | POST | 手动触发52pojie爬虫 |
| `/api/crawl/pojie/results` | GET | 获取52pojie爬取结果 |
| `/api/crawl/pojie/search?keyword=xxx` | GET | 搜索52pojie资源 |
| `/api/crawl/pojie/login` | POST | 登录52pojie |
| `/api/crawl/pojie/notify` | POST | 手动发送资源通知邮件 |

## 项目架构

```
search-tools/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/searchtools/
│   │   ├── SearchToolsApplication.java    # 启动类
│   │   ├── controller/                    # REST控制器
│   │   │   ├── SearchController.java      # 搜索API
│   │   │   ├── CrawlController.java       # 爬虫API
│   │   │   └── StatusController.java      # 系统状态API
│   │   ├── service/                       # 业务逻辑
│   │   │   ├── SearchService.java         # 搜索服务（含同步全网爬取）
│   │   │   ├── CrawlService.java          # 爬虫服务
│   │   │   ├── StatusService.java         # 系统状态服务
│   │   │   ├── LinkValidatorService.java  # 链接验证
│   │   │   ├── ResourcePreviewService.java # 资源预览
│   │   │   └── EmailNotificationService.java # 邮件通知
│   │   ├── search/                        # 搜索引擎
│   │   │   └── SearchEngine.java          # Lucene搜索引擎
│   │   ├── crawler/                       # 爬虫模块
│   │   │   ├── SearchEngineCrawler.java   # 搜索引擎爬虫
│   │   │   └── DistributedCrawler.java    # 分布式爬虫
│   │   ├── model/                         # 数据模型
│   │   │   ├── Resource.java              # 资源实体
│   │   │   ├── SearchResult.java          # 搜索结果DTO
│   │   │   ├── SearchHistory.java         # 搜索历史
│   │   │   ├── CrawlRecord.java           # 爬取记录
│   │   │   ├── CrawlRequest.java          # 爬取请求
│   │   │   ├── SystemStatus.java          # 系统状态
│   │   │   └── PoJieResource.java         # 52pojie资源
│   │   ├── repository/                    # 数据访问
│   │   └── config/                        # 配置类
│   └── src/main/resources/
│       └── application.yml                # 应用配置
│
└── frontend/                   # React 前端
    ├── src/
    │   ├── App.tsx                        # 主应用
    │   ├── pages/
    │   │   └── CrawlPage.tsx              # 爬取页面
    │   └── components/
    │       └── Header.tsx                 # 导航栏
    └── package.json
```

## 技术栈

### 后端
- **Spring Boot 3.2.0**：应用框架
- **Lucene 9.9.1**：全文搜索引擎
- **SQLite + JPA/Hibernate**：数据持久化
- **Jsoup**：HTML解析
- **Lombok**：代码简化

### 前端
- **React 19**：UI框架
- **TypeScript**：类型安全
- **Vite**：构建工具
- **React Router**：路由管理
- **Axios**：HTTP客户端

## 搜索实现原理

1. **Lucene索引**：资源数据存储在SQLite数据库，同时建立Lucene全文索引
2. **WildcardQuery**：使用通配符查询 `*keyword*` 支持中文搜索
3. **特殊字符转义**：自动转义Lucene特殊字符（如 `+`、`-`、`#`、`(`、`)` 等）
4. **数据库后备**：当Lucene搜索无结果时，自动使用数据库LIKE查询
5. **全网爬取**：当本地无结果时，自动通过百度和必应搜索引擎爬取全网资源，提取网盘链接并入库

## 数据来源

系统不预置假数据。所有资源通过以下方式获取：

1. **搜索触发爬取**：用户搜索时，如果本地无结果，自动触发百度+必应全网爬取
2. **手动爬取**：通过 `/api/crawl/search-engine` 接口手动触发
3. **52pojie爬虫**：定期爬取52pojie论坛的网盘资源分享帖

## 运行测试

```bash
cd backend

# 设置JAVA_HOME
$env:JAVA_HOME = "D:\Java\jdk-17.0.18"

# 运行所有测试
mvn clean test

# 测试结果：所有测试用例通过
```

## 配置说明

配置文件：`backend/src/main/resources/application.yml`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8081 | 后端服务端口 |
| `spring.datasource.url` | jdbc:sqlite:searchtools.db | SQLite数据库路径 |
| `search.lucene.index-path` | ./lucene-index | Lucene索引路径 |
| `crawler.request-interval` | 1000 | 爬虫请求间隔(ms) |
| `crawler.user-agent` | Mozilla/5.0... | 爬虫User-Agent |
| `pojie.keywords` | Java,C++,前端... | 52pojie关注的关键词 |
| `notification.email.to` | - | 通知邮件收件人 |

## 常见问题

### Q: 搜索没有结果？
A: 首次搜索会触发全网爬取，需要等待几秒。如果爬取也无结果，可能是网络环境不支持访问百度/必应。后续搜索相同关键词可直接从本地获取。

### Q: 如何重置数据？
A: 删除 `backend/searchtools.db` 文件和 `backend/lucene-index` 目录，重启后端即可。

### Q: C++等特殊字符搜索不准确？
A: 系统会自动转义Lucene特殊字符，`C++` 会被转义为 `c\+\+` 进行通配符搜索。

### Q: 如何手动添加资源？
A: 通过爬虫API `/api/crawl` 爬取包含网盘链接的网页，系统会自动提取并存储。

## 许可证

MIT License
