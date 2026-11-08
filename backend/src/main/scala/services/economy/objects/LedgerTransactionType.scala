// 现金流水分类：收入只来自售药与小费；其余类型为经营支出或预留支出。
package services.economy.objects

object LedgerTransactionType:
  val MedicineSale = "medicine-sale"
  val PatientTip = "patient-tip"
  val MedicinePurchase = "medicine-purchase"
  val RentAndUtilities = "rent-and-utilities"
  val StaffSalary = "staff-salary"
  val FacilityExpansion = "facility-expansion"

  val incomeTypes: Set[String] = Set(MedicineSale, PatientTip)
  val expenseTypes: Set[String] = Set(MedicinePurchase, RentAndUtilities, StaffSalary, FacilityExpansion)
