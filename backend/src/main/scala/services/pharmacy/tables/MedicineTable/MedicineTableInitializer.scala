// medicines 表初始化：药品目录是跨游戏共享的基础数据。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object MedicineTableInitializer:
  private val createSql =
    """
      |create table if not exists medicines (
      |  id varchar(80) primary key,
      |  name varchar(120) not null,
      |  purchase_price numeric(14, 2) not null,
      |  sale_price numeric(14, 2) not null
      |)
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
