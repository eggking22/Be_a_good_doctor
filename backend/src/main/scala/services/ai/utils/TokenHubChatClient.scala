// TokenHub 文本客户端：只供 AI 服务调用，API Key 始终停留在后端进程。
package services.ai.utils

import cats.effect.IO
import io.circe.Json
import io.circe.parser.parse
import io.circe.syntax.*
import services.system.HttpError

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.time.Duration
import scala.jdk.CollectionConverters.*

private[ai] object TokenHubChatClient:
  private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build()

  private[ai] def complete(systemPrompt: String, userPrompt: String): IO[(String, String)] =
    IO.blocking {
      val configuration = loadConfiguration()
      val payload = Json.obj(
        "model" -> Json.fromString(configuration.modelName),
        "messages" -> Json.arr(
          Json.obj("role" -> Json.fromString("system"), "content" -> Json.fromString(systemPrompt)),
          Json.obj("role" -> Json.fromString("user"), "content" -> Json.fromString(userPrompt))
        ),
        "max_tokens" -> Json.fromInt(configuration.maxTokens)
      ).noSpaces
      val request = HttpRequest.newBuilder()
        .uri(URI.create(s"${configuration.baseUrl}/chat/completions"))
        .timeout(Duration.ofSeconds(45))
        .header("Authorization", s"Bearer ${configuration.apiKey}")
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
        .build()
      val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
      if response.statusCode() / 100 != 2 then
        throw HttpError.Conflict(s"TokenHub 文本生成失败，HTTP ${response.statusCode()}。")
      val cursor = parse(response.body()).fold(error => throw HttpError.Conflict(s"TokenHub 返回的 JSON 无法解析：${error.getMessage}"), _.hcursor)
      val text = cursor.downField("choices").downArray.downField("message").get[String]("content")
        .fold(error => throw HttpError.Conflict(s"TokenHub 未返回文本内容：${error.getMessage}"), _.trim)
      if text.isEmpty then throw HttpError.Conflict("TokenHub 未生成可用文本，请稍后重试。")
      val modelName = cursor.get[String]("model").getOrElse(configuration.modelName)
      (text, modelName)
    }

  private def loadConfiguration(): TokenHubConfiguration =
    val localValues = readLocalEnvironment()
    val apiKey = valueOf("TOKENHUB_API_KEY", localValues).getOrElse(
      throw HttpError.Conflict("未配置 TOKENHUB_API_KEY，无法生成患者话术。")
    )
    TokenHubConfiguration(
      apiKey = apiKey,
      baseUrl = valueOf("TOKENHUB_BASE_URL", localValues).getOrElse("https://tokenhub.tencentmaas.com/v1").stripSuffix("/"),
      modelName = valueOf("TOKENHUB_CHAT_MODEL", localValues).getOrElse("deepseek-v4.1-flash"),
      maxTokens = valueOf("TOKENHUB_CHAT_MAX_TOKENS", localValues).flatMap(_.toIntOption).filter(_ >= 128).getOrElse(1024)
    )

  private def valueOf(name: String, localValues: Map[String, String]): Option[String] =
    sys.env.get(name).orElse(localValues.get(name)).map(_.trim).filter(_.nonEmpty)

  private def readLocalEnvironment(): Map[String, String] =
    List(Path.of(".env"), Path.of("backend", ".env")).find(Files.isRegularFile(_)).map { path =>
      Files.readAllLines(path, StandardCharsets.UTF_8).asScala.foldLeft(Map.empty[String, String]) { (values, line) =>
        line.split("=", 2).toList match
          case key :: value :: Nil if key.trim.matches("[A-Z0-9_]+") => values.updated(key.trim, value.trim)
          case _ => values
      }
    }.getOrElse(Map.empty)

  private final case class TokenHubConfiguration(apiKey: String, baseUrl: String, modelName: String, maxTokens: Int)
