// 疾病—症状关系表：一项疾病可有多个症状，一个症状也可关联多个疾病。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object DiseaseSymptomTableInitializer:
  private val createSql =
    """
      |create table if not exists disease_symptoms (
      |  disease_id varchar(80) not null references diseases(id) on delete cascade,
      |  symptom_id varchar(80) not null references symptoms(id) on delete cascade,
      |  primary key (disease_id, symptom_id)
      |);
      |create index if not exists idx_disease_symptoms_symptom_id on disease_symptoms(symptom_id);
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
