// consultations 表初始化：保存接诊过程，不把 gameId 暴露给 Consultation 领域对象。
package services.consultation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object ConsultationTableInitializer:
  private val createSql =
    """
      |create table if not exists consultations (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  patient_id varchar(80) not null,
      |  case_id varchar(80),
      |  emotion varchar(30) not null,
      |  status varchar(30) not null,
      |  dialogue_count integer not null,
      |  cash_change numeric(14, 2) not null default 0,
      |  reputation_change double precision not null default 0,
      |  treatment_matched boolean,
      |  treatment_reputation_delta double precision,
      |  treatment_reason varchar(255),
      |  constraint fk_consultation_game foreign key (game_id) references game_sessions(id) on delete cascade,
      |  constraint fk_consultation_patient foreign key (patient_id) references patients(id) on delete cascade
      |);
      |alter table consultations add column if not exists cash_change numeric(14, 2) not null default 0;
      |alter table consultations add column if not exists reputation_change double precision not null default 0;
      |alter table consultations add column if not exists case_id varchar(80);
      |alter table consultations add column if not exists treatment_matched boolean;
      |alter table consultations add column if not exists treatment_reputation_delta double precision;
      |alter table consultations add column if not exists treatment_reason varchar(255);
      |create index if not exists idx_consultations_game_id on consultations(game_id);
      |create index if not exists idx_consultations_patient_id on consultations(patient_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_consultation_game') then
      |    alter table consultations add constraint fk_consultation_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |  if not exists (select 1 from pg_constraint where conname = 'fk_consultation_patient') then
      |    alter table consultations add constraint fk_consultation_patient foreign key (patient_id) references patients(id) on delete cascade;
      |  end if;
      |  if not exists (select 1 from pg_constraint where conname = 'fk_consultation_case') then
      |    alter table consultations add constraint fk_consultation_case foreign key (case_id) references patient_cases(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
