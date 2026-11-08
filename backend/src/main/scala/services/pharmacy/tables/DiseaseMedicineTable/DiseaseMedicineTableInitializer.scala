// 疾病—药物关系表：一项疾病可关联多种药物，一种药物也可关联多个疾病。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object DiseaseMedicineTableInitializer:
  private val createSql =
    """
      |create table if not exists disease_medicines (
      |  disease_id varchar(80) not null references diseases(id) on delete cascade,
      |  medicine_id varchar(80) not null references medicines(id) on delete cascade,
      |  primary key (disease_id, medicine_id)
      |);
      |create index if not exists idx_disease_medicines_medicine_id on disease_medicines(medicine_id);
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
