// 开始接诊 API：校验患者归属，抽取隐藏疾病，加载症状并持久化病例。
package services.consultation.api

import cats.effect.IO
import cats.syntax.all.*
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import services.consultation.calculators.{CaseSymptomGenerator, DiseaseSelector}
import services.consultation.objects.{Consultation, ConsultationStatus, PatientCase}
import services.consultation.objects.apiTypes.StartConsultationResponse
import services.consultation.tables.{CaseSymptomTable, ConsultationTable, PatientCaseTable}
import services.consultation.utils.{ConsultationIdGenerator, PatientCaseIdGenerator}
import services.game.tables.GameStateTable
import services.game.calculators.GameClock
import services.patient.api.GeneratePatientAPIMessage
import services.patient.tables.PatientTable
import services.pharmacy.tables.{DiseaseSymptomTable, DiseaseTable}
import services.system.HttpError
import services.system.api.APIMessage

import java.sql.Connection
import scala.util.Random

final case class StartConsultationAPIMessage(
    gameId: String,
    patientId: String
) extends APIMessage[StartConsultationResponse]:
  override def plan(connection: Connection): IO[StartConsultationResponse] =
    startWithOpening(connection, gameId.trim, patientId.trim, attempt = 1)

  private def startWithOpening(
      connection: Connection,
      normalizedGameId: String,
      normalizedPatientId: String,
      attempt: Int
  ): IO[StartConsultationResponse] =
    if normalizedGameId.isEmpty || normalizedPatientId.isEmpty then
      IO.raiseError(HttpError.BadRequest("游戏 ID 和患者 ID 不能为空。"))
    else
      for
        game <- GameStateTable.findById(connection, normalizedGameId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到游戏：$normalizedGameId"))
        }
        _ <- if GameClock.hasBusinessEnded(game.currentMinute) then
          IO.raiseError(HttpError.Conflict("今日营业已经结束，请完成日结算后再接诊。"))
        else IO.unit
        patient <- PatientTable.findByGameIdAndId(connection, normalizedGameId, normalizedPatientId).flatMap {
          case Some(value) => IO.pure(value)
          case None => IO.raiseError(HttpError.NotFound(s"未找到属于该游戏的患者：$normalizedPatientId"))
        }
        _ <- PatientCaseTable.findActiveByPatientId(connection, normalizedGameId, normalizedPatientId).flatMap {
          case Some(_) => IO.raiseError(HttpError.Conflict("该患者已有进行中的病例。"))
          case None => IO.unit
        }
        diseases <- DiseaseTable.findAll(connection)
        disease <- IO.fromEither(
          DiseaseSelector.select(patient.attributes, diseases, new Random()).leftMap(HttpError.Conflict.apply)
        )
        symptoms <- DiseaseSymptomTable.findSymptomsByDiseaseId(connection, disease.id)
        _ <- if symptoms.nonEmpty then IO.unit
        else IO.raiseError(HttpError.Conflict(s"疾病 ${disease.name} 尚未配置可表现症状。"))
        patientCase = PatientCase(
          id = PatientCaseIdGenerator.nextId(),
          patientId = patient.id,
          disease = disease,
          symptoms = CaseSymptomGenerator.generate(symptoms, disease.attributes.difficulty),
          status = services.consultation.objects.CaseStatus.Active
        )
        _ <- PatientCaseTable.create(connection, normalizedGameId, patientCase)
        _ <- CaseSymptomTable.createAll(connection, patientCase.id, patientCase.symptoms)
        consultation = Consultation(
          id = ConsultationIdGenerator.nextId(),
          patient = patient,
          emotion = patient.initialEmotion,
          status = ConsultationStatus.InProgress,
          dialogueHistory = Nil,
          dialogueCount = 0,
          treatmentEvaluation = None,
          cashChange = BigDecimal(0),
          reputationChange = 0.0,
          caseId = Some(patientCase.id)
        )
        _ <- ConsultationTable.create(connection, normalizedGameId, consultation)
        openingResult <- GeneratePatientOpeningDialogueAPIMessage(consultation.id).plan(connection).attempt
        response <- openingResult match
          case Right(opening) => IO.pure(StartConsultationResponse(
            consultationId = consultation.id,
            patient = patient,
            emotion = consultation.emotion,
            status = consultation.status,
            opening = opening
          ))
          case Left(_) if attempt < StartConsultationAPIMessage.maximumOpeningAttempts =>
            PatientTable.delete(connection, patient.id) *>
              GeneratePatientAPIMessage(normalizedGameId).plan(connection).flatMap(replacement =>
                startWithOpening(connection, normalizedGameId, replacement.id, attempt + 1)
              )
          case Left(_) =>
            PatientTable.delete(connection, patient.id) *>
              IO.raiseError(HttpError.Conflict("患者开场话术连续生成失败，已弃置本次患者并停止重试。"))
      yield response

object StartConsultationAPIMessage:
  private[consultation] val maximumOpeningAttempts = 3

  given Codec.AsObject[StartConsultationAPIMessage] = deriveCodec
