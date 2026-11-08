// 症状目录表：基础症状由所有游戏共享。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object SymptomTableInitializer:
  private val createSql =
    """
      |create table if not exists symptoms (
      |  id varchar(80) primary key,
      |  name varchar(120) not null
      |)
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
