// 接诊购物清单表初始化：药品识别结果先进入清单，后续结算阶段再读取并处理库存与金额。
package services.consultation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object PrescriptionItemTableInitializer:
  private val createSql =
    """
      |create table if not exists consultation_prescription_items (
      |  consultation_id varchar(80) not null,
      |  medicine_id varchar(80) not null,
      |  quantity integer not null check (quantity > 0),
      |  primary key (consultation_id, medicine_id),
      |  constraint fk_prescription_consultation foreign key (consultation_id) references consultations(id) on delete cascade,
      |  constraint fk_prescription_medicine foreign key (medicine_id) references medicines(id)
      |);
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
