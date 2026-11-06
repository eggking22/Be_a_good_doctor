// reputation_records 表初始化：当前声誉与历史变化分离。
package services.reputation.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object ReputationTableInitializer:
  private val createSql =
    """
      |create table if not exists reputation_records (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  score double precision,
      |  delta double precision not null,
      |  reason varchar(255) not null,
      |  created_at timestamptz not null,
      |  constraint fk_reputation_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |alter table reputation_records alter column score drop not null;
      |create index if not exists idx_reputation_game_id on reputation_records(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_reputation_game') then
      |    alter table reputation_records add constraint fk_reputation_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
