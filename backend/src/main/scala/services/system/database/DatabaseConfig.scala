// 数据库配置：从环境变量读取 PostgreSQL 连接参数。
package services.system.database

final case class DatabaseConfig(
    host: String,
    port: Int,
    databaseName: String,
    user: String,
    password: String,
    maxPoolSize: Int,
    connectionTimeoutMs: Long
):
  def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$databaseName"

object DatabaseConfig:
  def load: DatabaseConfig =
    DatabaseConfig(
      host = sys.env.getOrElse("DB_HOST", "127.0.0.1"),
      port = sys.env.get("DB_PORT").flatMap(_.toIntOption).getOrElse(5432),
      databaseName = sys.env.getOrElse("DB_NAME", "ai_clinic_simulation"),
      user = sys.env.getOrElse("DB_USER", "db"),
      password = sys.env.getOrElse("DB_PASSWORD", "root"),
      maxPoolSize = sys.env.get("DB_MAX_POOL_SIZE").flatMap(_.toIntOption).getOrElse(8),
      connectionTimeoutMs = sys.env.get("DB_CONNECTION_TIMEOUT_MS").flatMap(_.toLongOption).getOrElse(10000L)
    )
