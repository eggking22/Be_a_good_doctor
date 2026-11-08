// 药品名匹配：AI 负责理解同义表达；完整目录名出现在原话中时由后端兜底，保证“感冒药”等明确药名能入清单。
package services.consultation.utils

import services.pharmacy.objects.Medicine

object MedicineNameMatcher:
  private[consultation] def matchedMedicineIds(text: String, medicines: List[Medicine]): List[String] =
    val normalizedText = text.trim.toLowerCase(java.util.Locale.ROOT)
    medicines.collect {
      case medicine if medicine.name.trim.nonEmpty && normalizedText.contains(medicine.name.trim.toLowerCase(java.util.Locale.ROOT)) => medicine.id
    }
