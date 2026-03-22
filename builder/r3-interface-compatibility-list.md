# 第 2 阶段接口兼容清单

本清单对应“接口标准化第二阶段”落地结果，只覆盖本轮处理中优先级接口与仍需保留一轮兼容的旧路径。

| 旧路径 | 标准路径 | 是否公开访问 | 是否需要 JWT | 计划移除轮次 |
| --- | --- | --- | --- | --- |
| `POST /articles/edit` | `PUT /articles/{articleId}` | 否 | 是 | 下一轮旧接口删除卡 |
| `PATCH /articles/{articleId}` | `PATCH /articles/{articleId}/status` | 否 | 是 | 下一轮旧接口删除卡 |
| `PATCH /articles/admin/status/{articleId}` | `PATCH /admin/articles/{articleId}/status` | 否 | 是，且管理员权限 | 下一轮旧接口删除卡 |
| `PUT /comments/update` | `PUT /comments/{commentId}` | 否 | 是 | 下一轮旧接口删除卡 |
| `PATCH /comments/admin/status/{commentId}` | `PATCH /admin/comments/{commentId}/status` | 否 | 是，且管理员权限 | 下一轮旧接口删除卡 |
| `POST /common/upload` | `POST /uploads` | 是 | 否 | 下一轮旧接口删除卡 |

## 本轮明确不升级为标准接口的遗留路径

| 路径 | 现状结论 | 原因 | 后续动作 |
| --- | --- | --- | --- |
| `GET /comments/user` | 仅保留兼容，不定义标准路径 | 当前实现实际仍走公开评论列表链路，不具备“当前用户评论历史”语义 | 需后续单独补用户评论查询链路后，再决定是否升级为 `/users/me/comments` |
| `GET /comments/user/search` | 仅保留兼容，不定义标准路径 | 与 `GET /comments/user` 语义重复，且没有独立资源模型 | 与上条一起在后续删除或重做 |
