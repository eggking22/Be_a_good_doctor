// HTTP 路由总入口：组合健康检查和各业务服务的统一 API Message 路由。
package app

import cats.effect.IO
import cats.syntax.semigroupk.*
import org.http4s.HttpApp
import services.ai.routes.AIRoutes
import services.clinic.routes.ClinicRoutes
import services.consultation.routes.ConsultationRoutes
import services.economy.routes.EconomyRoutes
import services.event.routes.EventRoutes
import services.game.routes.GameRoutes
import services.patient.routes.PatientRoutes
import services.pharmacy.routes.PharmacyRoutes
import services.reputation.routes.ReputationRoutes
import services.system.api.APIMessageRouter
import services.system.database.DatabaseSession
import services.voice.routes.VoiceRoutes

object ApiRouter:
  private val serviceMessages = List(
    GameRoutes.apiMessages,
    EconomyRoutes.apiMessages,
    PatientRoutes.apiMessages,
    ConsultationRoutes.apiMessages,
    PharmacyRoutes.apiMessages,
    ReputationRoutes.apiMessages,
    EventRoutes.apiMessages,
    ClinicRoutes.apiMessages,
    AIRoutes.apiMessages,
    VoiceRoutes.apiMessages
  )

  def httpApp(databaseSession: DatabaseSession): HttpApp[IO] =
    (HealthRouter.routes <+> APIMessageRouter.routes(databaseSession, serviceMessages)).orNotFound
