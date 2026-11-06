// dialogue_messages 表初始化：对话是接诊历史，只追加，不覆盖既有消息。
package services.consultation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object DialogueMessageTableInitializer:
  private val createSql =
    """
      |create table if not exists dialogue_messages (
      |  id bigserial primary key,
      |  consultation_id varchar(80) not null,
      |  speaker varchar(30) not null,
      |  content text not null,
      |  created_at timestamptz not null,
      |  constraint fk_dialogue_consultation foreign key (consultation_id) references consultations(id) on delete cascade
      |);
      |create index if not exists idx_dialogue_consultation_id on dialogue_messages(consultation_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_dialogue_consultation') then
      |    alter table dialogue_messages add constraint fk_dialogue_consultation foreign key (consultation_id) references consultations(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
