// daily_statements 表初始化：日结算是历史记录，只追加并按游戏查询。
package services.economy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object DailyStatementTableInitializer:
  private val createSql =
    """
      |create table if not exists daily_statements (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  day integer not null,
      |  opening_cash numeric(14, 2) not null,
      |  income numeric(14, 2) not null,
      |  expense numeric(14, 2) not null,
      |  closing_cash numeric(14, 2) not null,
      |  bankrupt boolean not null,
      |  created_at timestamptz not null,
      |  constraint uq_daily_statement_game_day unique (game_id, day),
      |  constraint fk_daily_statement_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |create unique index if not exists uq_daily_statement_game_day on daily_statements(game_id, day);
      |create index if not exists idx_daily_statements_game_id on daily_statements(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_daily_statement_game') then
      |    alter table daily_statements add constraint fk_daily_statement_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
