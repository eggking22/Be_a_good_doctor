// 员工表访问层：只负责 staff 表的 SQL 和结果映射，不计算工资或满意度规则。
package services.clinic.tables

import cats.effect.IO
import services.clinic.objects.{Staff, StaffRole}
import services.system.database.JdbcSupport

import java.sql.Connection

object StaffTable:
  private val createSql =
    """
      |insert into staff
      |  (id, game_id, name, role, daily_salary, ability, communication_ability, satisfaction, active)
      |values (?, ?, ?, ?, ?, ?, ?, ?, ?)
      |""".stripMargin

  def create(connection: Connection, gameId: String, staff: Staff): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, staff.id)
      statement.setString(2, gameId)
      statement.setString(3, staff.name)
      statement.setString(4, staff.role.toString)
      statement.setBigDecimal(5, staff.dailySalary.bigDecimal)
      statement.setInt(6, staff.ability)
      statement.setInt(7, staff.communicationAbility)
      statement.setDouble(8, staff.satisfaction)
      statement.setBoolean(9, staff.active)
    }.map(_ => ())

  private val findByIdSql =
    "select id, name, role, daily_salary, ability, communication_ability, satisfaction, active from staff where id = ?"

  def findById(connection: Connection, staffId: String): IO[Option[Staff]] =
    JdbcSupport.queryOne(
      connection,
      findByIdSql
    )(_.setString(1, staffId))(readStaff)

  private val findByGameIdSql =
    "select id, name, role, daily_salary, ability, communication_ability, satisfaction, active from staff where game_id = ? order by id"

  def findByGameId(connection: Connection, gameId: String): IO[List[Staff]] =
    JdbcSupport.query(
      connection,
      findByGameIdSql
    )(_.setString(1, gameId))(readStaff)

  private val updateSql =
    """
      |update staff set name = ?, role = ?, daily_salary = ?, ability = ?,
      |communication_ability = ?, satisfaction = ?, active = ? where id = ?
      |""".stripMargin

  def update(connection: Connection, staff: Staff): IO[Unit] =
    JdbcSupport.updateExactlyOne(
      connection,
      updateSql,
      s"未找到员工：${staff.id}"
    ) { statement =>
      statement.setString(1, staff.name)
      statement.setString(2, staff.role.toString)
      statement.setBigDecimal(3, staff.dailySalary.bigDecimal)
      statement.setInt(4, staff.ability)
      statement.setInt(5, staff.communicationAbility)
      statement.setDouble(6, staff.satisfaction)
      statement.setBoolean(7, staff.active)
      statement.setString(8, staff.id)
    }

  private val deleteSql = "delete from staff where id = ?"

  def delete(connection: Connection, staffId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, deleteSql, s"未找到员工：$staffId")(_.setString(1, staffId))

  private def readStaff(resultSet: java.sql.ResultSet): Staff =
    Staff(
      id = resultSet.getString("id"),
      name = resultSet.getString("name"),
      role = StaffRole.fromString(resultSet.getString("role")).fold(message => throw new IllegalStateException(message), identity),
      dailySalary = BigDecimal(resultSet.getBigDecimal("daily_salary")),
      ability = resultSet.getInt("ability"),
      communicationAbility = resultSet.getInt("communication_ability"),
      satisfaction = resultSet.getDouble("satisfaction"),
      active = resultSet.getBoolean("active")
    )

