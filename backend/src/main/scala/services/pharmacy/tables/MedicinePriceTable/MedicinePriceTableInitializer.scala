// medicine_prices 表初始化：价格独立于药品基础目录，便于频繁调整价格而不改动 medicines。
package services.pharmacy.tables

import cats.effect.IO
import services.system.database.JdbcSupport

import java.sql.Connection

object MedicinePriceTableInitializer:
  private val createSql =
    """
      |create table if not exists medicine_prices (
      |  medicine_id varchar(80) primary key references medicines(id) on delete cascade,
      |  purchase_price numeric(14, 2) not null check (purchase_price >= 0),
      |  sale_price numeric(14, 2) not null check (sale_price >= 0)
      |);
      |do $$
      |begin
      |  if exists (
      |    select 1 from information_schema.columns
      |    where table_schema = current_schema()
      |      and table_name = 'medicines'
      |      and column_name = 'purchase_price'
      |  ) then
      |    execute 'insert into medicine_prices (medicine_id, purchase_price, sale_price)
      |      select id, purchase_price, sale_price from medicines
      |      on conflict (medicine_id) do nothing';
      |    execute 'alter table medicines drop column if exists purchase_price';
      |    execute 'alter table medicines drop column if exists sale_price';
      |  end if;
      |end $$;
      |""".stripMargin

  def initialize(connection: Connection): IO[Unit] =
    JdbcSupport.execute(connection, createSql)
