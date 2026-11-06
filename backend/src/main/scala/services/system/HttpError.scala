// 系统级错误类型：统一映射请求错误、资源不存在和状态冲突。
package services.system

sealed abstract class HttpError(message: String) extends RuntimeException(message)

object HttpError:
  final case class BadRequest(message: String) extends HttpError(message)
  final case class NotFound(message: String) extends HttpError(message)
  final case class Conflict(message: String) extends HttpError(message)
