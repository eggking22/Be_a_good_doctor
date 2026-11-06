// API 消息抽象：只描述请求消息如何调用一个业务用例。
package services.system.api

import cats.effect.IO

import java.sql.Connection

trait APIMessage[Response]:
  def plan(connection: Connection): IO[Response]
