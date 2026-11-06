// medicine_inventory 表初始化：库存批次同时关联游戏和药品目录。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object InventoryTableInitializer:
  private val createSql =
    """
      |create table if not exists medicine_inventory (
      |  id varchar(80) primary key,
      |  game_id varchar(80) not null,
      |  medicine_id varchar(80) not null,
      |  quantity integer not null check (quantity >= 0),
      |  expiry_date date not null,
      |  constraint uq_inventory_lot unique (game_id, medicine_id, expiry_date),
      |  constraint fk_inventory_game foreign key (game_id) references game_sessions(id) on delete cascade,
      |  constraint fk_inventory_medicine foreign key (medicine_id) references medicines(id) on delete restrict
      |);
      |create unique index if not exists uq_inventory_lot on medicine_inventory(game_id, medicine_id, expiry_date);
      |create index if not exists idx_inventory_game_id on medicine_inventory(game_id);
      |do $$ begin
      |  if not exists (select 1 from pg_constraint where conname = 'fk_inventory_game') then
      |    alter table medicine_inventory add constraint fk_inventory_game foreign key (game_id) references game_sessions(id) on delete cascade;
      |  end if;
      |  if not exists (select 1 from pg_constraint where conname = 'fk_inventory_medicine') then
      |    alter table medicine_inventory add constraint fk_inventory_medicine foreign key (medicine_id) references medicines(id) on delete restrict;
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
