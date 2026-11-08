// 医疗目录导入：把已审核的 JSON 草稿作为一次明确操作写入共享目录表。
package services.pharmacy.api

import cats.effect.IO
import cats.syntax.all.*
import services.pharmacy.objects.{CatalogMedicinePrice, Disease, MedicalCatalogDraft, Medicine, MedicineLevel, MedicinePrice, MedicinePricesDraft, Symptom}
import services.pharmacy.objects.apiTypes.MedicalCatalogImportResponse
import services.pharmacy.tables.{DiseaseMedicineTable, DiseaseSymptomTable, DiseaseTable, MedicineTable, SymptomTable}
import services.pharmacy.utils.MedicalCatalogDraftLoader
import services.system.api.APIMessage

import java.sql.Connection
import scala.util.Try

final case class ImportMedicalCatalogDraftAPIMessage() extends APIMessage[MedicalCatalogImportResponse]:
  override def plan(connection: Connection): IO[MedicalCatalogImportResponse] =
    for
      drafts <- MedicalCatalogDraftLoader.load()
      (catalog, prices) = drafts
      priceByMedicineId <- IO.fromEither(validate(catalog, prices))
      _ <- catalog.symptoms.traverse_(upsertSymptom(connection, _))
      _ <- catalog.diseases.traverse_(upsertDisease(connection, _))
      _ <- catalog.medicines.traverse_ { medicine =>
        for
          price <- IO.fromEither(toMedicinePrice(medicine.id, priceByMedicineId(medicine.id)))
          level <- IO.fromEither(MedicineLevel.fromString(medicine.level).left.map(message => new IllegalArgumentException(message)))
          _ <- upsertMedicine(connection, Medicine(medicine.id, medicine.name, price, level))
        yield ()
      }
      _ <- catalog.diseaseSymptoms.traverse_(link => DiseaseSymptomTable.link(connection, link.diseaseId, link.symptomId))
      _ <- catalog.diseaseMedicines.traverse_(link => DiseaseMedicineTable.link(connection, link.diseaseId, link.medicineId))
    yield MedicalCatalogImportResponse(
      diseaseCount = catalog.diseases.size,
      symptomCount = catalog.symptoms.size,
      medicineCount = catalog.medicines.size,
      diseaseSymptomCount = catalog.diseaseSymptoms.size,
      diseaseMedicineCount = catalog.diseaseMedicines.size
    )

  private def validate(
      catalog: MedicalCatalogDraft,
      prices: MedicinePricesDraft
  ): Either[Throwable, Map[String, CatalogMedicinePrice]] =
    val statuses = Set("approved", "imported")
    val medicineIds = catalog.medicines.map(_.id)
    val priceIds = prices.prices.map(_.medicineId)
    if !statuses.contains(catalog.status) || !statuses.contains(prices.status) then
      Left(new IllegalStateException("只有 approved 或 imported 状态的医疗目录草稿可以导入。"))
    else if medicineIds.distinct.size != medicineIds.size then Left(new IllegalArgumentException("医疗目录草稿包含重复药品 ID。"))
    else if priceIds.distinct.size != priceIds.size then Left(new IllegalArgumentException("药品价格草稿包含重复药品 ID。"))
    else if medicineIds.toSet != priceIds.toSet then Left(new IllegalArgumentException("药品目录与价格草稿的 medicineId 必须一一对应。"))
    else Right(prices.prices.map(price => price.medicineId -> price).toMap)

  private def toMedicinePrice(medicineId: String, price: CatalogMedicinePrice): Either[Throwable, MedicinePrice] =
    for
      purchase <- Try(BigDecimal(price.purchasePrice)).toEither.left.map(_ => new IllegalArgumentException(s"药品 $medicineId 的采购价无效。"))
      sale <- Try(BigDecimal(price.salePrice)).toEither.left.map(_ => new IllegalArgumentException(s"药品 $medicineId 的售价无效。"))
      _ <- Either.cond(purchase >= 0 && sale >= purchase, (), new IllegalArgumentException(s"药品 $medicineId 的价格必须满足 0 <= 采购价 <= 售价。"))
    yield MedicinePrice(purchase, sale)

  private def upsertDisease(connection: Connection, disease: Disease): IO[Unit] =
    DiseaseTable.findById(connection, disease.id).flatMap(_.fold(DiseaseTable.create(connection, disease))(_ => DiseaseTable.update(connection, disease)))

  private def upsertSymptom(connection: Connection, symptom: Symptom): IO[Unit] =
    SymptomTable.findById(connection, symptom.id).flatMap(_.fold(SymptomTable.create(connection, symptom))(_ => SymptomTable.update(connection, symptom)))

  private def upsertMedicine(connection: Connection, medicine: Medicine): IO[Unit] =
    MedicineTable.findById(connection, medicine.id).flatMap(_.fold(MedicineTable.create(connection, medicine))(_ => MedicineTable.update(connection, medicine)))
