package sequential.data


import com.typesafe.config.Config
import org.apache.commons.codec.binary.Base64
import sequential.data.PostgresDbDriver.api.Database


trait DBConnection {

  def getDbConnection(config: Config): Database = {
    Database.forURL(config.getString("sequential-db.url"), config.getString("sequential-db.user"),
      new String(Base64.decodeBase64(config.getString("sequential-db.password"))), driver = "org.postgresql.Driver")
  }

  //For local testing only (Added until AWS Postgres has data for testing)
  def getDockerDbConnection(): Database = {
    Database.forURL("jdbc:postgresql://192.168.99.100:32769/sequential", "postgres", "test123",
      driver = "org.postgresql.Driver")
  }
}

//object ConfigDecoder extends (ConfigProperty => String) {
//  def apply(encodedProperty: ConfigProperty): String = {
//    new String(Base64.decodeBase64(encodedProperty.value.getBytes))
//  }
//}