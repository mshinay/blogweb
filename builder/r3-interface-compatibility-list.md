# 第 3 阶段接口兼容清单

本清单对应“旧兼容接口删除卡”执行结果，用于明确哪些历史路径已下线，哪些遗留接口仍未标准化。

## 本轮已删除的旧兼容路径

| 旧路径 | 标准路径 | 是否公开访问 | 是否需要 JWT | 当前状态 |
| --- | --- | --- | --- | --- |
| `POST /users/update` | `PUT /users/{id}` | 否 | 是 | 已删除 |
| `GET /users/public/{id}` | `GET /users/{id}` | 是 | 否 | 已删除 |
| `POST /articles/upload` | `POST /articles` | 否 | 是 | 已删除 |
| `GET /articles/detail/{articleId}` | `GET /articles/{articleId}` | 是 | 否 | 已删除 |
| `POST /articles/edit` | `PUT /articles/{articleId}` | 否 | 是 | 已删除 |
| `PATCH /articles/{articleId}` | `PATCH /articles/{articleId}/status` | 否 | 是 | 已删除 |
| `GET /articles/admin/list` | `GET /admin/articles` | 否 | 是，且管理员权限 | 已删除 |
| `GET /articles/admin/search` | `GET /admin/articles` | 否 | 是，且管理员权限 | 已删除 |
| `PATCH /articles/admin/status/{articleId}` | `PATCH /admin/articles/{articleId}/status` | 否 | 是，且管理员权限 | 已删除 |
| `POST /comments/upload` | `POST /comments` | 否 | 是 | 已删除 |
| `GET /comments/list` | `GET /comments` | 是 | 否 | 已删除 |
| `PUT /comments/update` | `PUT /comments/{commentId}` | 否 | 是 | 已删除 |
| `GET /comments/admin/list` | `GET /admin/comments` | 否 | 是，且管理员权限 | 已删除 |
| `GET /comments/admin/search` | `GET /admin/comments` | 否 | 是，且管理员权限 | 已删除 |
| `PATCH /comments/admin/status/{commentId}` | `PATCH /admin/comments/{commentId}/status` | 否 | 是，且管理员权限 | 已删除 |
| `POST /common/upload` | `POST /uploads` | 是 | 否 | 已删除 |

## 仍遗留未标准化接口

| 路径 | 现状结论 | 原因 | 后续动作 |
| --- | --- | --- | --- |
| `GET /comments/user` | 遗留兼容接口，暂不删除 | 当前实现仍走公开评论列表链路，不具备“当前用户评论历史”语义 | 需后续单独补用户评论查询链路后，再决定是否升级为 `/users/me/comments` |
| `GET /comments/user/search` | 遗留兼容接口，暂不删除 | 与 `GET /comments/user` 语义重复，且没有独立资源模型 | 与上条一起在后续删除或重做 |
