// game_events 表初始化：事件归属于一局游戏，并支持级联删除。
package services.event.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object EventTableInitializer:
  private val createSql =
    """
      |create table if not exists game_events (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  event_type varchar(40) not null,
      |  title varchar(160) not null,
      |  description text not null,
      |  risk_level varchar(20) not null,
      |  resolved boolean not null default false,
      |  constraint fk_event_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |create index if not exists idx_game_events_game_id on game_events(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_event_game') then
      |    alter table game_events add constraint fk_event_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
