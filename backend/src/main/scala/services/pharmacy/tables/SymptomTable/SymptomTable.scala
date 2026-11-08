// 症状目录表访问层：提供基础症状的写入与查询。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.Symptom
import services.system.database.JdbcSupport

import java.sql.{Connection, ResultSet}

object SymptomTable:
  private val createSql = "insert into symptoms (id, name) values (?, ?)"

  def create(connection: Connection, symptom: Symptom): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, symptom.id)
      statement.setString(2, symptom.name)
    }.map(_ => ())

  private val createIfMissingSql =
    "insert into symptoms (id, name) values (?, ?) on conflict (id) do nothing"

  def createIfMissing(connection: Connection, symptom: Symptom): IO[Unit] =
    JdbcSupport.update(connection, createIfMissingSql) { statement =>
      statement.setString(1, symptom.id)
      statement.setString(2, symptom.name)
    }.map(_ => ())

  private val findByIdSql = "select id, name from symptoms where id = ?"

  def findById(connection: Connection, symptomId: String): IO[Option[Symptom]] =
    JdbcSupport.queryOne(connection, findByIdSql)(_.setString(1, symptomId))(readSymptom)

  private val findAllSql = "select id, name from symptoms order by id"

  def findAll(connection: Connection): IO[List[Symptom]] =
    JdbcSupport.query(connection, findAllSql)(_ => ())(readSymptom)

  private val updateSql = "update symptoms set name = ? where id = ?"

  def update(connection: Connection, symptom: Symptom): IO[Unit] =
    JdbcSupport.updateExactlyOne(connection, updateSql, s"未找到症状：${symptom.id}") { statement =>
      statement.setString(1, symptom.name)
      statement.setString(2, symptom.id)
    }

  private def readSymptom(resultSet: ResultSet): Symptom =
    Symptom(resultSet.getString("id"), resultSet.getString("name"))
