package sequential.data

import com.github.tminglei.slickpg._
import slick.basic.Capability
import slick.jdbc.{GetResult, JdbcCapabilities, PositionedResult}


trait PostgresDbDriver extends ExPostgresProfile with PgDateSupportJoda with PgSprayJsonSupport with PgArraySupport {

  // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"
  override def pgjson = "jsonb"
  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  object PostgresDbAPI extends API with DateTimeImplicits with SprayJsonImplicits with ArrayImplicits
  override val api = PostgresDbAPI

}

object PostgresDbDriver extends PostgresDbDriver

//Use this if using Joda LocalDateTime
//object PostgresDbAPI extends API with SprayJsonImplicits with ArrayImplicits {
//  implicit def dateTime =
//    MappedColumnType.base[LocalDateTime, Timestamp](
//      dt => new Timestamp(dt.toDate.getTime),
//      ts => new LocalDateTime(ts.getTime)
//    )
//}