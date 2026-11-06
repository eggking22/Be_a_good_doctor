// 前端 API 消息基础类型：输入由消息字段承载，响应通过泛型声明。
export abstract class APIMessage<Response> {
  declare readonly responseType: Response

  abstract get serviceName(): string
}
