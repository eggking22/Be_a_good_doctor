// 未开药就结束接诊时的固定患者抱怨，不调用 AI。
package services.consultation.utils

import scala.util.Random

object NoPrescriptionDepartureLines:
  private val lines = Vector(
    "什么医院还不让看病？",
    "药都不开就赶人？我白跑一趟。",
    "看了半天什么也没开，太离谱了。",
    "连个药方都没有，就让我走？",
    "这也叫诊所？我还是去别家看看。"
  )

  def randomLine(): String = lines(Random.nextInt(lines.size))
