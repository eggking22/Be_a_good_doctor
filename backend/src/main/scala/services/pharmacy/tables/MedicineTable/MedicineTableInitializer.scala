// medicines 表初始化：药品基础目录是跨游戏共享的基础数据；价格由 MedicinePriceTableInitializer 初始化。
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
      |  level varchar(32) not null default 'Ordinary'
      |)
      |;
      |alter table medicines add column if not exists level varchar(32) not null default 'Ordinary'
      |;
      |update medicines set level = 'EasyDrug' where level = 'OverTheCounter'
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
