# 配置说明

仓库默认只保留可提交的安全配置，不再提交数据库密码、JWT 密钥、OSS 凭据。

## 配置优先级

应用默认启用 `dev` 环境，并按以下顺序读取配置：

1. `application.yml`
2. `application-dev.yml`
3. 当前工作目录下的 `application-local.yml`（可选，不提交）
4. 当前工作目录下的 `config/application-local.yml`（可选，不提交）
5. 仓库根目录启动时可命中的 `blog-server/application-local.yml`（可选，不提交）
6. 仓库根目录启动时可命中的 `blog-server/config/application-local.yml`（可选，不提交）
7. 环境变量

本地开发建议从示例文件复制：

```powershell
Copy-Item blog-server/src/main/resources/application-local.example.yml application-local.yml
```

如果你在仓库根目录执行 `mvn -f blog-server/pom.xml spring-boot:run`，推荐把文件放在仓库根目录 `application-local.yml`。

如果你先 `cd blog-server` 再执行 `mvn spring-boot:run`，同样推荐把文件放在当前目录 `application-local.yml`。

如果团队习惯把本地配置跟模块放在一起，也支持 `blog-server/application-local.yml` 和 `blog-server/config/application-local.yml`。以上位置都已加入 `.gitignore`。

## 必填配置项

数据库：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

JWT：

- `blog.jwt.user-secret-key`

OSS：

- `blog.alioss.endpoint`
- `blog.alioss.access-key-id`
- `blog.alioss.access-key-secret`
- `blog.alioss.bucket-name`

## 环境变量映射

也可以直接通过环境变量注入：

- `BLOG_DB_URL`
- `BLOG_DB_USERNAME`
- `BLOG_DB_PASSWORD`
- `BLOG_DB_DRIVER`
- `BLOG_JWT_USER_SECRET_KEY`
- `BLOG_JWT_USER_TTL`
- `BLOG_JWT_USER_TOKEN_NAME`
- `BLOG_OSS_ENDPOINT`
- `BLOG_OSS_ACCESS_KEY_ID`
- `BLOG_OSS_ACCESS_KEY_SECRET`
- `BLOG_OSS_BUCKET_NAME`
