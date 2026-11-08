// 疾病目录表访问层：可按编号、难度或完整目录查询。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.{Disease, DiseaseAttributes}
import services.system.database.JdbcSupport

import java.sql.{Connection, ResultSet}

object DiseaseTable:
  private val createSql = "insert into diseases (id, name, difficulty, extraction_probability) values (?, ?, ?, ?)"

  def create(connection: Connection, disease: Disease): IO[Unit] =
    JdbcSupport.update(connection, createSql) { statement =>
      statement.setString(1, disease.id)
      statement.setString(2, disease.name)
      statement.setInt(3, disease.attributes.difficulty)
      statement.setDouble(4, disease.attributes.extractionProbability)
    }.map(_ => ())

  private val createIfMissingSql =
    "insert into diseases (id, name, difficulty, extraction_probability) values (?, ?, ?, ?) on conflict (id) do nothing"

  def createIfMissing(connection: Connection, disease: Disease): IO[Unit] =
    JdbcSupport.update(connection, createIfMissingSql) { statement =>
      statement.setString(1, disease.id)
      statement.setString(2, disease.name)
      statement.setInt(3, disease.attributes.difficulty)
      statement.setDouble(4, disease.attributes.extractionProbability)
    }.map(_ => ())

  private val findByIdSql = "select id, name, difficulty, extraction_probability from diseases where id = ?"

  def findById(connection: Connection, diseaseId: String): IO[Option[Disease]] =
    JdbcSupport.queryOne(connection, findByIdSql)(_.setString(1, diseaseId))(readDisease)

  private val findAllSql = "select id, name, difficulty, extraction_probability from diseases order by id"

  def findAll(connection: Connection): IO[List[Disease]] =
    JdbcSupport.query(connection, findAllSql)(_ => ())(readDisease)

  private val updateSql = "update diseases set name = ?, difficulty = ?, extraction_probability = ? where id = ?"

  def update(connection: Connection, disease: Disease): IO[Unit] =
    JdbcSupport.updateExactlyOne(connection, updateSql, s"未找到疾病：${disease.id}") { statement =>
      statement.setString(1, disease.name)
      statement.setInt(2, disease.attributes.difficulty)
      statement.setDouble(3, disease.attributes.extractionProbability)
      statement.setString(4, disease.id)
    }

  private val findByDifficultySql = "select id, name, difficulty, extraction_probability from diseases where difficulty = ? order by id"

  def findByDifficulty(connection: Connection, difficulty: Int): IO[List[Disease]] =
    JdbcSupport.query(connection, findByDifficultySql)(_.setInt(1, difficulty))(readDisease)

  private def readDisease(resultSet: ResultSet): Disease =
    Disease(
      id = resultSet.getString("id"),
      name = resultSet.getString("name"),
      attributes = DiseaseAttributes(
        difficulty = resultSet.getInt("difficulty"),
        extractionProbability = resultSet.getDouble("extraction_probability")
      )
    )
