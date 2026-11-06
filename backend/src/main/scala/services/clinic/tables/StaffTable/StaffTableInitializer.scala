// staff 表初始化：员工归属于一局游戏。
package services.clinic.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object StaffTableInitializer:
  private val createSql =
    """
      |create table if not exists staff (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  name varchar(120) not null,
      |  role varchar(40) not null,
      |  daily_salary numeric(14, 2) not null,
      |  ability integer not null,
      |  communication_ability integer not null,
      |  satisfaction double precision not null,
      |  active boolean not null default true,
      |  constraint fk_staff_game foreign key (game_id) references game_sessions(id) on delete cascade
      |);
      |create index if not exists idx_staff_game_id on staff(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_staff_game') then
      |    alter table staff add constraint fk_staff_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
