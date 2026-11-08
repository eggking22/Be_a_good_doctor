// patient_cases 表初始化：保存每次患者病例的隐藏疾病和状态。
package services.consultation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object PatientCaseTableInitializer:
  private val createSql =
    """
      |create table if not exists patient_cases (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  patient_id varchar(80) not null,
      |  disease_id varchar(80) not null,
      |  disease_name varchar(120) not null,
      |  disease_difficulty integer not null check (disease_difficulty between 1 and 5),
      |  disease_extraction_probability double precision not null check (disease_extraction_probability > 0 and disease_extraction_probability <= 1),
      |  status varchar(40) not null,
      |  created_at timestamptz not null,
      |  constraint fk_case_game foreign key (game_id) references game_sessions(id) on delete cascade,
      |  constraint fk_case_patient foreign key (patient_id) references patients(id) on delete cascade,
      |  constraint fk_case_disease foreign key (disease_id) references diseases(id) on delete restrict
      |);
      |alter table patient_cases add column if not exists disease_name varchar(120);
      |alter table patient_cases add column if not exists disease_difficulty integer;
      |alter table patient_cases add column if not exists disease_extraction_probability double precision;
      |update patient_cases c set disease_name = d.name, disease_difficulty = d.difficulty,
      |  disease_extraction_probability = d.extraction_probability
      |from diseases d where c.disease_id = d.id and
      |  (c.disease_name is null or c.disease_difficulty is null or c.disease_extraction_probability is null);
      |alter table patient_cases alter column disease_name set not null;
      |alter table patient_cases alter column disease_difficulty set not null;
      |alter table patient_cases alter column disease_extraction_probability set not null;
      |create index if not exists idx_patient_cases_game_id on patient_cases(game_id);
      |create index if not exists idx_patient_cases_patient_id on patient_cases(patient_id);
      |create index if not exists idx_patient_cases_status on patient_cases(status);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_case_game') then
      |    alter table patient_cases add constraint fk_case_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |  if not exists (select 1 from pg_constraint where conname = 'fk_case_patient') then
      |    alter table patient_cases add constraint fk_case_patient foreign key (patient_id) references patients(id) on delete cascade;
      |  end if;
      |  if not exists (select 1 from pg_constraint where conname = 'fk_case_disease') then
      |    alter table patient_cases add constraint fk_case_disease foreign key (disease_id) references diseases(id) on delete restrict;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
