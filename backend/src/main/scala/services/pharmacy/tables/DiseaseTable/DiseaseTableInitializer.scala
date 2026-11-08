// 疾病目录表：疾病难度与患者属性使用相同的 1～5 范围。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object DiseaseTableInitializer:
  private val createSql =
    """
      |create table if not exists diseases (
      |  id varchar(80) primary key,
      |  name varchar(120) not null,
      |  difficulty integer not null check (difficulty between 1 and 5),
      |  extraction_probability double precision not null check (extraction_probability > 0 and extraction_probability <= 1)
      |);
      |alter table diseases add column if not exists extraction_probability double precision not null default 1.0
      |  check (extraction_probability > 0 and extraction_probability <= 1);
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
