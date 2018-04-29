package sequential.dao

import java.io.{File, IOException, PrintWriter}
import java.sql.Types

import com.typesafe.config.ConfigFactory
import org.joda.time.LocalDate
import sequential.data._
import slick.lifted.{TableQuery, Tag}
import sequential.data.PostgresDbDriver.api._
import sequential.data.{DBConnection}
import slick.jdbc.{GetResult, PositionedResult, SetParameter}
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import com.julianpeeters.caseclass.generator._

import scala.reflect.runtime.universe._


class TableDao extends DBConnection {

  //  lazy val db = getDbConnection(ConfigFactory.load)
  lazy val db = getDockerDbConnection

  implicit object ResultMap extends GetResult[Map[String, Any]] {
    def apply(pr: PositionedResult) = {
      val rs = pr.rs
      val md = rs.getMetaData();
      (1 to rs.getRow).flatMap { j => (1 to pr.numColumns).map { i => md.getColumnName(i) -> rs.getObject(i) } }.toMap
    }
  }

  def getAll(): Seq[String] = {
    val sql = sql"""select tablename from pg_tables where schemaname='sequential'""".as[String]
    Await.result(db.run(sql), 2.seconds)
  }

  def getAllTableColumns(tableName: String): Vector[JsObject] = {
    println(tableName)
    val sql = sql"""select column_name, data_type from information_schema.columns where table_name='#${tableName}'"""
      .as[Map[String, Any]]
    println(sql.statements.head)
    val result = Await.result(db.run(sql), 2.seconds)
    result.map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
  }

  def createTable(createTable: CreateTable): OkResponse = {

    val tableColumns = createTable.tablecolumns.map(i =>
      s"${i.columnname} TEXT ${if (!i.isnullable) " NOT NULL" else ""} " +
        s"${if (createTable.primarykeycolumn.equals(i.columnname)) "PRIMARY KEY" else ""}").mkString(", ")
    val sql = sqlu"""create table sequential.#${createTable.tablename}(#${tableColumns})"""
    Await.result(db.run(sql), 2.seconds)
    OkResponse("Table Created")
  }

  def addColumn(tableName: String, addColumn: Seq[TableColumns]): OkResponse = {
    val columnsToAdd = addColumn.map(i =>
      s"${i.columnname} TEXT ${if (!i.isnullable) " NOT NULL" else ""}").mkString(", ADD COLUMN ")
    val sql = sqlu"""alter table sequential.#${tableName} add column #${columnsToAdd}"""
    Await.result(db.run(sql), 2.seconds)
    OkResponse("Columns Added")
  }

  def dropColumn(tableName: String, columnsToDrop: Seq[String]): OkResponse = {
    val sql = sqlu"""alter table sequential.#${tableName} drop column #${columnsToDrop.mkString(", ")}"""
    Await.result(db.run(sql), 2.seconds)
    OkResponse("Columns Dropped")
  }

  def insertRecords(tableName: String, records: Seq[Seq[Records]]): OkResponse = {
    val mappings = getTableColumnMapping(tableName)
    val columns = records.head.map(_.columnname)
    val columnNames = if (mappings.nonEmpty)
      columns.map(i => mappings.filter(_.mappingcolumn == i)).flatten.map(_.tablecolumn).mkString(",")
    else columns.mkString(",")
    val sql = records.map(i =>
      sqlu"""insert into sequential.#${tableName}(#${columnNames})
            values('#${i.map(_.columnvalue).mkString("', '")}')""")
//    println(sql.statements.head)
    sql.map(i => Await.result(db.run(i), 2.seconds))
    OkResponse("Records Inserted")
  }

  def updateRecords(tableName: String, records: UpdateRecords): OkResponse = {
    val condition = records.columnstoupdate.map(i => s"${i.columnname} = '${i.columnvalue}'").mkString(" AND ")
    val recordsToUpdate = records.valuestoupdate.map(i => s"${i.columnname} = '${i.columnvalue}'").mkString(", ")
    val sql = sqlu"""update sequential.#${tableName} SET #${recordsToUpdate} WHERE #${condition}"""
    Await.result(db.run(sql), 2.seconds)
    OkResponse("Records Updated")
  }

  def getAllRecords(tableName: String) = {
    val sql = sql"""select * from sequential.#${tableName}""".as[Map[String, Any]]
//    println(sql.statements.head)
    val result = Await.result(db.run(sql), 2.seconds)
    result.map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
  }

  def getRecordsByColumn(tableName: String, columnName: Seq[String]) = {
    val sql = sql"""select #${columnName.mkString(",")} from sequential.#${tableName}""".as[Map[String, Any]]
    println(sql.statements.head)
    val result = Await.result(db.run(sql), 2.seconds)
    result.map(i => JsObject(i.map(j => ((j._1, if (j._2 != null) j._2.toString.toJson else "".toJson)))))
  }


  class ColumnMappingTable(tag: Tag) extends Table[ColumnMapping](tag, Some("sequential"), "mapping") {
    def tableColumn = column[String]("table_column")

    def mappingColumn = column[String]("mapping_column")

    def mappingTableName = column[String]("mapping_table_name")

    def * = (tableColumn, mappingColumn, mappingTableName) <> ((ColumnMapping.apply _) tupled, ColumnMapping.unapply _)
  }

  val columnMappingTable = TableQuery[ColumnMappingTable]

  def createMapping(tableName: String, records: Seq[Records])(implicit ec: ExecutionContext): OkResponse = {
    val mappings: Seq[ColumnMapping] = records.map(i => ColumnMapping(i.columnname, i.columnvalue, tableName))
    Await.result(db.run(columnMappingTable ++= mappings), 2.seconds)
    OkResponse("Mapping Created")
  }

  def updateMapping(tableName: String, records: Seq[Records]): OkResponse = {
    val mappingsToUpdate = records.map(i => (s"'${i.columnname}'", s"'${i.columnvalue}'")).mkString(",")
    val sql =
      sqlu"""UPDATE sequential.mapping SET mapping_column = valuesToUpdate.mapping_column
      FROM ( VALUES #${mappingsToUpdate}) AS valuesToUpdate (table_column, mapping_column)
      WHERE mapping.table_column = valuesToUpdate.table_column AND mapping_table_name = $tableName"""
    println(sql.statements.head)
    Await.result(db.run(sql), 2.seconds)
    OkResponse("Mapping Updated")
  }

  def dropColumnMapping(tableName: String, columns: Seq[String])(implicit ec: ExecutionContext): OkResponse = {
    Await.result(db.run(columnMappingTable.filter(i => (i.mappingTableName === tableName &&
      (i.tableColumn inSet columns))).delete), 2.seconds)
    OkResponse("Mapping Deleted")
  }

  def getTableColumnMapping(tableName: String)(implicit ec: ExecutionContext): Seq[ColumnMapping] =
    Await.result(db.run(columnMappingTable.filter(_.mappingTableName === tableName).result), 2.seconds)

}

//db withSession {
//(Q.u + "insert into customer (id, name, address) values " +
//users.map(toSql).mkString(",")).execute()
//}
//
//def toSql(user: User): String = "(%d, '%s', '%s')".format(user.id, user.name, user.address)


//def updateEntryGroupNameByInstructionId(instructionId: Long, entryGroupsToUpdate: List[UpdateEntryGroup]) = {
//implicit val getEntryGroupResult = GetResult(r => UpdateEntryGroup(r.<<, r.<<))
//
//val groupsToUpdate = entryGroupsToUpdate.map(group =>  (group.analysisEntryGroupId, s"'${group.newAnalysisEntryGroupName}'")).mkString(",")
//val sql = sql"""UPDATE design_analysis.analysis_entry_group SET analysis_entry_group_name = valuesToUpdate.analysis_entry_group_name
//      FROM ( VALUES #${groupsToUpdate}) AS valuesToUpdate (analysis_entry_group_id, analysis_entry_group_name)
//      WHERE analysis_entry_group.analysis_entry_group_id = valuesToUpdate.analysis_entry_group_id
//      AND analysis_entry_group.analysis_instruction_id = $instructionId returning analysis_entry_group.analysis_entry_group_id,
//      analysis_entry_group.analysis_entry_group_name""".as[UpdateEntryGroup]
//Await.result(db.run(sql), Duration.Inf).toList
//}

object TableDao extends TableDao {}


//  implicit val uuidSetter = SetParameter[Seq[String]] {
//    case (Seq(uuid), params) => params.setString(uuid.toString)
//    case (Nil, params) => params.setNull(Types.VARCHAR)
//  }

//    implicit val getProtocolsResult = GetResult(r => Protocol(r.<<, r.<<))
//    implicit val protocolFormat = jsonFormat2(Protocol)