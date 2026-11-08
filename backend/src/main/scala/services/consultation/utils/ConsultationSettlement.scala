// 患者离场结算：收入只包括药品销售和基于沟通态度的小费。
package services.consultation.utils

import cats.effect.IO
import cats.syntax.all.*
import services.consultation.objects.{Consultation, PlayerAttitude, TreatmentEvaluation}
import services.consultation.tables.{ConsultationTable, PatientCaseTable, PlayerTextAnalysisTable, PrescriptionItemTable}
import services.economy.objects.{LedgerTransaction, LedgerTransactionType}
import services.economy.tables.LedgerTable
import services.economy.utils.Money
import services.game.tables.GameStateTable
import services.pharmacy.tables.{DiseaseMedicineTable, InventoryTable}
import services.reputation.utils.ReputationOperations
import services.system.HttpError

import java.sql.Connection
import java.time.OffsetDateTime
import java.util.UUID

object ConsultationSettlement:
  def settleIfNeeded(connection: Connection, consultationId: String): IO[Unit] =
    ConsultationTable.findById(connection, consultationId).flatMap {
      case Some(consultation) if consultation.settled => IO.unit
      case Some(consultation) => settle(connection, consultation)
      case None => IO.raiseError(HttpError.NotFound(s"未找到接诊记录：$consultationId"))
    }

  private def settle(connection: Connection, consultation: Consultation): IO[Unit] =
    for
      gameId <- ConsultationTable.findGameIdById(connection, consultation.id).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(HttpError.NotFound(s"未找到接诊所属游戏：${consultation.id}"))
      }
      game <- GameStateTable.findById(connection, gameId).flatMap {
        case Some(value) => IO.pure(value)
        case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：$gameId"))
      }
      prescriptions <- PrescriptionItemTable.findByConsultationId(connection, consultation.id)
      analyses <- PlayerTextAnalysisTable.findByConsultationId(connection, consultation.id)
      patientCase <- consultation.caseId.traverse(PatientCaseTable.findById(connection, _)).map(_.flatten)
      matchedMedicineIds <- patientCase.traverse(patientCase =>
        DiseaseMedicineTable.findMedicinesByDiseaseId(connection, patientCase.disease.id).map(_.map(_.id).toSet)
      ).map(_.getOrElse(Set.empty))
      treatmentMatched = prescriptions.nonEmpty && matchedMedicineIds.nonEmpty && prescriptions.forall(item => matchedMedicineIds.contains(item.medicine.id))
      treatmentEvaluation = TreatmentEvaluation(
        matched = treatmentMatched,
        reputationDelta = 0.0,
        reason = if treatmentMatched then "开具药品与病例匹配，可参与康复回访。" else "开具药品与病例不匹配，无法参与康复回访。"
      )
      (reputationDelta, reputationReason) = ReputationOperations.departureDelta(prescriptions.nonEmpty, analyses.lastOption.map(_.attitude))
      medicineIncome = Money.round(prescriptions.map(item => item.medicine.price.salePrice * item.quantity).sum)
      tip = Money.round(medicineIncome * tipRate(analyses.lastOption.map(_.attitude)))
      cashChange = Money.round(medicineIncome + tip)
      _ <- prescriptions.traverse_(item => InventoryTable.consumeMedicine(connection, gameId, item.medicine.id, item.quantity))
      updatedGame = game.copy(
        cash = Money.round(game.cash + cashChange),
        updatedAt = OffsetDateTime.now()
      )
      _ <- GameStateTable.update(connection, updatedGame)
      _ <- ReputationOperations.applyDelta(connection, updatedGame, reputationDelta, reputationReason)
      _ <- appendIfPositive(connection, gameId, game.currentDay, LedgerTransactionType.MedicineSale, medicineIncome, s"患者 ${consultation.patient.name} 购药")
      _ <- appendIfPositive(connection, gameId, game.currentDay, LedgerTransactionType.PatientTip, tip, s"患者 ${consultation.patient.name} 小费")
      _ <- ConsultationTable.update(connection, consultation.copy(
        cashChange = cashChange,
        reputationChange = reputationDelta,
        treatmentEvaluation = Some(treatmentEvaluation),
        settled = true,
        closedDay = Some(game.currentDay)
      ))
    yield ()

  private def tipRate(attitude: Option[PlayerAttitude]): BigDecimal = attitude match
    case Some(PlayerAttitude.Polite) => BigDecimal("0.15")
    case Some(PlayerAttitude.Neutral) => BigDecimal("0.07")
    case _ => BigDecimal(0)

  private def appendIfPositive(
      connection: Connection,
      gameId: String,
      day: Int,
      transactionType: String,
      amount: BigDecimal,
      description: String
  ): IO[Unit] =
    if amount <= 0 then IO.unit
    else LedgerTable.append(connection, LedgerTransaction(s"ledger-${UUID.randomUUID()}", gameId, transactionType, amount, day, description))
