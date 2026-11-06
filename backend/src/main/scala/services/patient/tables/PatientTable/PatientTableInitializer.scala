// patients 表初始化：患者归属于一局游戏。
package services.patient.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object PatientTableInitializer:
  private val createSql =
    """
      |create table if not exists patients (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  name varchar(120) not null,
      |  illness_difficulty integer not null check (illness_difficulty between 1 and 5),
      |  payment_willingness integer not null check (payment_willingness between 1 and 5),
      |  communication_difficulty integer not null check (communication_difficulty between 1 and 5),
      |  dispute_risk double precision not null check (dispute_risk between 0 and 1),
      |  constraint fk_patient_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |create index if not exists idx_patients_game_id on patients(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_patient_game') then
      |    alter table patients add constraint fk_patient_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
