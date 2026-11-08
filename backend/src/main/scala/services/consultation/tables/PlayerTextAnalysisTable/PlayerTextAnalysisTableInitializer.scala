// player_text_analyses 表初始化：保存每轮玩家文字的受限理解结果，供后续声誉系统读取。
package services.consultation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object PlayerTextAnalysisTableInitializer:
  private val createSql =
    """
      |create table if not exists player_text_analyses (
      |  id bigserial primary key,
      |  consultation_id varchar(80) not null,
      |  attitude varchar(30) not null,
      |  intent varchar(40) not null,
      |  mentioned_medicine_ids text not null,
      |  confidence double precision not null check (confidence between 0 and 1),
      |  created_at timestamptz not null,
      |  constraint fk_player_text_analysis_consultation foreign key (consultation_id) references consultations(id) on delete cascade
      |);
      |create index if not exists idx_player_text_analyses_consultation_id on player_text_analyses(consultation_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_player_text_analysis_consultation') then
      |    alter table player_text_analyses add constraint fk_player_text_analysis_consultation foreign key (consultation_id) references consultations(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
