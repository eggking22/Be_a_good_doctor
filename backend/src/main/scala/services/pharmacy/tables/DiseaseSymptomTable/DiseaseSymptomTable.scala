// 疾病—症状关系表访问层：支持增加、移除及双向查询。
package services.pharmacy.tables

import cats.effect.IO
import services.pharmacy.objects.{CatalogDiseaseSymptom, Disease, DiseaseAttributes, Symptom}
import services.system.database.JdbcSupport

import java.sql.Connection

object DiseaseSymptomTable:
  private val linkSql =
    "insert into disease_symptoms (disease_id, symptom_id) values (?, ?) on conflict (disease_id, symptom_id) do nothing"

  def link(connection: Connection, diseaseId: String, symptomId: String): IO[Unit] =
    JdbcSupport.update(connection, linkSql) { statement =>
      statement.setString(1, diseaseId)
      statement.setString(2, symptomId)
    }.map(_ => ())

  private val unlinkSql = "delete from disease_symptoms where disease_id = ? and symptom_id = ?"

  def unlink(connection: Connection, diseaseId: String, symptomId: String): IO[Unit] =
    JdbcSupport.deleteExactlyOne(connection, unlinkSql, s"未找到疾病症状关联：$diseaseId / $symptomId") { statement =>
      statement.setString(1, diseaseId)
      statement.setString(2, symptomId)
    }

  private val findSymptomsByDiseaseIdSql =
    """
      |select s.id, s.name
      |from symptoms s join disease_symptoms ds on ds.symptom_id = s.id
      |where ds.disease_id = ? order by s.id
      |""".stripMargin

  def findSymptomsByDiseaseId(connection: Connection, diseaseId: String): IO[List[Symptom]] =
    JdbcSupport.query(connection, findSymptomsByDiseaseIdSql)(_.setString(1, diseaseId)) { row =>
      Symptom(row.getString("id"), row.getString("name"))
    }

  private val findAllSql = "select disease_id, symptom_id from disease_symptoms order by disease_id, symptom_id"

  def findAll(connection: Connection): IO[List[CatalogDiseaseSymptom]] =
    JdbcSupport.query(connection, findAllSql)(_ => ()) { row =>
      CatalogDiseaseSymptom(row.getString("disease_id"), row.getString("symptom_id"))
    }

  private val findDiseasesBySymptomIdSql =
    """
      |select d.id, d.name, d.difficulty, d.extraction_probability
      |from diseases d join disease_symptoms ds on ds.disease_id = d.id
      |where ds.symptom_id = ? order by d.id
      |""".stripMargin

  def findDiseasesBySymptomId(connection: Connection, symptomId: String): IO[List[Disease]] =
    JdbcSupport.query(connection, findDiseasesBySymptomIdSql)(_.setString(1, symptomId)) { row =>
      Disease(
        row.getString("id"),
        row.getString("name"),
        DiseaseAttributes(row.getInt("difficulty"), row.getDouble("extraction_probability"))
      )
    }
