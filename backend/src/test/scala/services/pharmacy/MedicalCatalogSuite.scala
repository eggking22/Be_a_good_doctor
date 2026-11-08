// 药房知识库集成测试：使用 PostgreSQL 事务并回滚测试数据。
package services.pharmacy

import cats.effect.IO
import cats.syntax.all.*
import munit.CatsEffectSuite
import services.pharmacy.objects.{Disease, DiseaseAttributes, Medicine, MedicinePrice, Symptom}
import services.pharmacy.tables.{DiseaseMedicineTable, DiseaseSymptomTable, DiseaseTable, MedicineTable, SymptomTable}
import services.system.database.{DatabaseSchemaInitializer, DatabaseSession}

import java.sql.Connection
import java.util.UUID

final class MedicalCatalogSuite extends CatsEffectSuite:
  // 测试创建的数据及对示例价格的调整均在测试结束后回滚。
  private def withRollback(operation: Connection => IO[Unit]): IO[Unit] =
    val rollback = new RuntimeException("药房知识库测试回滚")
    DatabaseSession.initialize.use { databaseSession =>
      databaseSession.withTransactionConnection { connection =>
        DatabaseSchemaInitializer.initialize(connection) *>
          operation(connection) *>
          IO.raiseError[Unit](rollback)
      }.attempt.flatMap {
        case Left(error) if error eq rollback => IO.unit
        case Left(error) => IO.raiseError(error)
        case Right(_) => IO.raiseError(new IllegalStateException("测试事务未回滚"))
      }
    }

  test("示例关系可查询，重复初始化不覆盖已修改的价格") {
    withRollback { connection =>
      for
        disease <- DiseaseTable.findById(connection, "disease-common-cold")
        symptoms <- DiseaseSymptomTable.findSymptomsByDiseaseId(connection, "disease-common-cold")
        medicines <- DiseaseMedicineTable.findMedicinesByDiseaseId(connection, "disease-common-cold")
        medicine <- IO.fromOption(medicines.find(_.id == "medicine-cold"))(new NoSuchElementException("缺少示例感冒药"))
        _ = assert(disease.isDefined)
        _ = assert(symptoms.exists(_.id == "symptom-runny-nose"))
        _ = assert(symptoms.exists(_.id == "symptom-sore-throat"))
        changed = medicine.copy(price = medicine.price.copy(salePrice = medicine.price.salePrice + BigDecimal(3)))
        _ <- MedicineTable.update(connection, changed)
        _ <- DatabaseSchemaInitializer.initialize(connection)
        _ <- DatabaseSchemaInitializer.initialize(connection)
        after <- MedicineTable.findById(connection, medicine.id)
        symptomsAfter <- DiseaseSymptomTable.findSymptomsByDiseaseId(connection, "disease-common-cold")
        medicinesAfter <- DiseaseMedicineTable.findMedicinesByDiseaseId(connection, "disease-common-cold")
        _ = assertEquals(after, Some(changed))
        _ = assertEquals(symptomsAfter.count(_.id == "symptom-runny-nose"), 1)
        _ = assertEquals(symptomsAfter.count(_.id == "symptom-sore-throat"), 1)
        _ = assertEquals(medicinesAfter.count(_.id == medicine.id), 1)
      yield ()
    }
  }

  test("症状、疾病和药物支持多对多双向查询") {
    withRollback { connection =>
      val id = UUID.randomUUID().toString
      val symptomA = Symptom(s"test-symptom-a-$id", "测试症状甲")
      val symptomB = Symptom(s"test-symptom-b-$id", "测试症状乙")
      val diseaseA = Disease(s"test-disease-a-$id", "测试疾病甲", DiseaseAttributes(1, 0.8))
      val diseaseB = Disease(s"test-disease-b-$id", "测试疾病乙", DiseaseAttributes(2, 0.4))
      val medicineA = Medicine(s"test-medicine-a-$id", "测试药品甲", MedicinePrice(BigDecimal(1), BigDecimal(2)))
      val medicineB = Medicine(s"test-medicine-b-$id", "测试药品乙", MedicinePrice(BigDecimal(3), BigDecimal(4)))
      for
        _ <- List(symptomA, symptomB).traverse_(SymptomTable.create(connection, _))
        _ <- List(diseaseA, diseaseB).traverse_(DiseaseTable.create(connection, _))
        _ <- List(medicineA, medicineB).traverse_(MedicineTable.create(connection, _))
        _ <- List(diseaseA, diseaseB).traverse_ { disease =>
          List(symptomA, symptomB).traverse_(symptom => DiseaseSymptomTable.link(connection, disease.id, symptom.id))
        }
        _ <- List(diseaseA, diseaseB).traverse_ { disease =>
          List(medicineA, medicineB).traverse_(medicine => DiseaseMedicineTable.link(connection, disease.id, medicine.id))
        }
        _ <- DiseaseSymptomTable.link(connection, diseaseA.id, symptomA.id)
        _ <- DiseaseMedicineTable.link(connection, diseaseA.id, medicineA.id)
        symptoms <- DiseaseSymptomTable.findSymptomsByDiseaseId(connection, diseaseA.id)
        symptomDiseases <- DiseaseSymptomTable.findDiseasesBySymptomId(connection, symptomA.id)
        medicines <- DiseaseMedicineTable.findMedicinesByDiseaseId(connection, diseaseA.id)
        medicineDiseases <- DiseaseMedicineTable.findDiseasesByMedicineId(connection, medicineA.id)
        _ = assertEquals(symptoms, List(symptomA, symptomB))
        _ = assertEquals(symptomDiseases, List(diseaseA, diseaseB))
        _ = assertEquals(medicines, List(medicineA, medicineB))
        _ = assertEquals(medicineDiseases, List(diseaseA, diseaseB))
        _ <- DiseaseSymptomTable.unlink(connection, diseaseA.id, symptomA.id)
        _ <- DiseaseMedicineTable.unlink(connection, diseaseA.id, medicineA.id)
        symptomsAfter <- DiseaseSymptomTable.findSymptomsByDiseaseId(connection, diseaseA.id)
        medicinesAfter <- DiseaseMedicineTable.findMedicinesByDiseaseId(connection, diseaseA.id)
        _ = assertEquals(symptomsAfter, List(symptomB))
        _ = assertEquals(medicinesAfter, List(medicineB))
      yield ()
    }
  }

  test("关系表拒绝不存在的疾病或症状") {
    withRollback { connection =>
      DiseaseSymptomTable.link(connection, s"missing-disease-${UUID.randomUUID()}", "symptom-runny-nose")
        .attempt.map(result => assert(result.isLeft))
    }
  }
