// game_sessions 表初始化：主表先于所有带 game_id 外键的业务表创建。
package services.game.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object GameStateTableInitializer:
  private val createSql =
    """
      |create table if not exists game_sessions (
      |  id varchar(80) primary key,
      |  player_name varchar(120) not null,
      |  current_day integer not null,
      |  cash numeric(14, 2) not null,
      |  reputation double precision not null,
      |  bankrupt boolean not null default false,
      |  created_at timestamptz not null,
      |  updated_at timestamptz not null
      |)
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
