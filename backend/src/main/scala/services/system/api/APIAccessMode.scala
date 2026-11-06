// API 数据访问模式：查询不打开写事务，命令和跨表流程使用事务。
package services.system.api

enum APIAccessMode:
  case ReadOnly
  case Transactional
