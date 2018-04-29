package sequential.data

import org.joda.time.LocalDate
import spray.json._, DefaultJsonProtocol._


case class TableColumns(columnname: String, columntype: String, isnullable: Boolean)
  extends RestRequest

case class CreateTable(tablename: String, tablecolumns: Seq[TableColumns], primarykeycolumn: String) extends RestRequest

case class Records(columnname: String, columnvalue: String) extends RestRequest

case class UpdateRecords(columnstoupdate: Seq[Records], valuestoupdate: Seq[Records]) extends RestRequest

case class ColumnMapping(tablecolumn: String, mappingcolumn: String, mappingtablename: String)

object TableColumns extends RestRequest {
  implicit val tableColumnsJsonFormat = jsonFormat(TableColumns.apply,
    "columnname",
    "columntype",
    "isnullable"
  )
}

object CreateTable extends RestRequest {
  implicit val createTableJsonFormat = jsonFormat(CreateTable.apply,
    "tablename",
    "tablecolumns",
    "primarykeycolumn"
  )
}

object Records extends RestRequest {
  implicit val recordsJsonFormat = jsonFormat(Records.apply,
    "columnname",
    "columnvalue"
  )
}

object UpdateRecords extends RestRequest {
  implicit val updateRecordsJsonFormat = jsonFormat(UpdateRecords.apply,
    "columnstoupdate",
    "valuestoupdate")
}

object ColumnMapping {
  implicit val columnMappingJsonFormat = jsonFormat3(ColumnMapping.apply)
}

