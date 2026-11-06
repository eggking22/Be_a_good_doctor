// 设施表访问层：只负责 facilities 表的 SQL 和结果映射。
package services.clinic.tables

import cats.effect.IO
import services.clinic.objects.Facility
import services.system.database.JdbcSupport

import java.sql.Connection

object FacilityTable:
  private val createSql =
    "insert into facilities (id, game_id, name, level, upgrade_cost, capacity_bonus) values (?, ?, ?, ?, ?, ?)"

  def create(connection: Connection, gameId: String, facility: Facility): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, facility.id)
      statement.setString(2, gameId)
      statement.setString(3, facility.name)
      statement.setInt(4, facility.level)
      statement.setBigDecimal(5, facility.upgradeCost.bigDecimal)
      statement.setInt(6, facility.capacityBonus)
    }.map(_ => ())

  private val findByIdSql = "select id, name, level, upgrade_cost, capacity_bonus from facilities where id = ?"

  def findById(connection: Connection, facilityId: String): IO[Option[Facility]] =
    JdbcSupport.queryOne(
      connection,
      findByIdSql
    )(_.setString(1, facilityId))(readFacility)

  private val findByGameIdSql = "select id, name, level, upgrade_cost, capacity_bonus from facilities where game_id = ? order by id"

  def findByGameId(connection: Connection, gameId: String): IO[List[Facility]] =
    JdbcSupport.query(
      connection,
      findByGameIdSql
    )(_.setString(1, gameId))(readFacility)

  private val updateSql = "update facilities set name = ?, level = ?, upgrade_cost = ?, capacity_bonus = ? where id = ?"

  def update(connection: Connection, facility: Facility): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到设施：${facility.id}"
    ) { statement =>
      statement.setString(1, facility.name)
      statement.setInt(2, facility.level)
      statement.setBigDecimal(3, facility.upgradeCost.bigDecimal)
      statement.setInt(4, facility.capacityBonus)
      statement.setString(5, facility.id)
    }

  private val deleteSql = "delete from facilities where id = ?"

  def delete(connection: Connection, facilityId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到设施：$facilityId")(_.setString(1, facilityId))

  private def readFacility(resultSet: java.sql.ResultSet): Facility =
    Facility(
      id = resultSet.getString("id"),
      name = resultSet.getString("name"),
      level = resultSet.getInt("level"),
      upgradeCost = BigDecimal(resultSet.getBigDecimal("upgrade_cost")),
      capacityBonus = resultSet.getInt("capacity_bonus")
    )

