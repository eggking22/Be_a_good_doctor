// case_symptoms 表初始化：保存病例症状、严重程度和透露状态。
package services.consultation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object CaseSymptomTableInitializer:
  private val createSql =
    """
      |create table if not exists case_symptoms (
      |  case_id varchar(80) not null,
      |  symptom_id varchar(80) not null,
      |  symptom_name varchar(120) not null,
      |  severity integer not null check (severity between 1 and 5),
      |  revealed boolean not null default false,
      |  primary key (case_id, symptom_id),
      |  constraint fk_case_symptoms_case foreign key (case_id) references patient_cases(id) on delete cascade,
      |  constraint fk_case_symptoms_symptom foreign key (symptom_id) references symptoms(id) on delete restrict
      |);
      |alter table case_symptoms add column if not exists symptom_name varchar(120);
      |update case_symptoms cs set symptom_name = s.name
      |from symptoms s where cs.symptom_id = s.id and cs.symptom_name is null;
      |alter table case_symptoms alter column symptom_name set not null;
      |create index if not exists idx_case_symptoms_symptom_id on case_symptoms(symptom_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_case_symptoms_case') then
      |    alter table case_symptoms add constraint fk_case_symptoms_case foreign key (case_id) references patient_cases(id) on delete cascade;
      |  end if;
      |  if not exists (select 1 from pg_constraint where conname = 'fk_case_symptoms_symptom') then
      |    alter table case_symptoms add constraint fk_case_symptoms_symptom foreign key (symptom_id) references symptoms(id) on delete restrict;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
