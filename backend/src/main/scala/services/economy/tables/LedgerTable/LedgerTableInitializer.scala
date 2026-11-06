// ledger_transactions 表初始化：只维护现金流水历史表及其索引、外键。
package services.economy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object LedgerTableInitializer:
  private val createSql =
    """
      |create table if not exists ledger_transactions (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  transaction_type varchar(40) not null,
      |  amount numeric(14, 2) not null,
      |  occurred_on integer not null,
      |  description varchar(255) not null,
      |  constraint fk_ledger_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |create index if not exists idx_ledger_game_id on ledger_transactions(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_ledger_game') then
      |    alter table ledger_transactions add constraint fk_ledger_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
