// facilities 表初始化：设施归属于一局游戏。
package services.clinic.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object FacilityTableInitializer:
  private val createSql =
    """
      |create table if not exists facilities (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  name varchar(120) not null,
      |  level integer not null,
      |  upgrade_cost numeric(14, 2) not null,
      |  capacity_bonus integer not null,
      |  constraint fk_facility_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |create index if not exists idx_facilities_game_id on facilities(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_facility_game') then
      |    alter table facilities add constraint fk_facility_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
