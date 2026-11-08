// 医疗目录草稿加载器：仅在药房服务内部读取本地待审查 JSON，不直接执行数据库写入。
package services.pharmacy.utils

import cats.effect.IO
import io.circe.parser.decode
import services.pharmacy.objects.{MedicalCatalogDraft, MedicinePricesDraft}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

private[pharmacy] object MedicalCatalogDraftLoader:
  private val catalogFileName = "medical_catalog.draft.json"
  private val pricesFileName = "medicine_prices.draft.json"

  private[pharmacy] def load(): IO[(MedicalCatalogDraft, MedicinePricesDraft)] =
    for
      directory <- IO.fromOption(catalogDirectory)(new IllegalStateException("未找到 backend/data/medical-catalog 目录。"))
      catalogText <- IO.blocking(Files.readString(directory.resolve(catalogFileName), StandardCharsets.UTF_8))
      pricesText <- IO.blocking(Files.readString(directory.resolve(pricesFileName), StandardCharsets.UTF_8))
      catalog <- IO.fromEither(decode[MedicalCatalogDraft](catalogText).left.map(error => new IllegalArgumentException(s"医疗目录草稿格式无效：${error.getMessage}")))
      prices <- IO.fromEither(decode[MedicinePricesDraft](pricesText).left.map(error => new IllegalArgumentException(s"药品价格草稿格式无效：${error.getMessage}")))
    yield (catalog, prices)

  private def catalogDirectory: Option[Path] =
    List(
      Path.of("data", "medical-catalog"),
      Path.of("backend", "data", "medical-catalog")
    ).find(path => Files.isRegularFile(path.resolve(catalogFileName)) && Files.isRegularFile(path.resolve(pricesFileName)))
