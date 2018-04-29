package sequential.service

import java.io.{File, FileOutputStream}
import java.util.UUID
import javax.ws.rs.Path

import akka.event.Logging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import sequential.data._
import sequential.{ActorSystemProvider, LogProvider}
import io.swagger.annotations._
import sequential.dao.TableDao
import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString
//import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}


@Path("/tables")
@Api(value = "/tables", produces = "application/json", consumes = "application/json")
trait TableService extends ActorSystemProvider with LogProvider with ServiceCommons with SprayJsonSupport
  with Directives {

  val tableDao: TableDao

  @Path("/")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Create DB table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "table", required = true, paramType = "body",
    dataType = "sequential.data.CreateTable", value = "Please provide the table to create")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully creates the table")))
  def createTablePath = pathEndOrSingleSlash {
    post {
      entity(as[CreateTable]) { body =>
        logRequest(s"POST /$root/tables", Logging.InfoLevel) {
          complete(tableDao.createTable(body))
        }
      }
    }
  }

  @Path("/")
  @ApiOperation(httpMethod = "GET", response = classOf[Vector[String]], value = "Returns a list of Tables")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gives list of Tables")))
  def getTablesPath = pathEndOrSingleSlash {
    get {
      logRequest(s"GET /$root/tables", Logging.InfoLevel) {
        complete(tableDao.getAll())
      }
    }
  } ~
    pathEnd(get(redirect(s"/$root/tables/", StatusCodes.TemporaryRedirect)))


  @Path("/{tableName}/add-columns")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Add Columns to DB table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to add columns"),
    new ApiImplicitParam(name = "columnsToAdd", required = true, paramType = "body",
      dataType = "sequential.data.TableColumns", value = "Please provide the columns to add")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully add column to the table")))
  def addColumnsPath = pathPrefix(Segment / "add-columns") { tableName =>
    post {
      entity(as[Seq[TableColumns]]) { body =>
        logRequest(s"POST /$root/tables/$tableName/add-columns", Logging.InfoLevel) {
          complete(tableDao.addColumn(tableName, body))
        }
      }
    }
  }

  @Path("/{tableName}/get-columns")
  @ApiOperation(httpMethod = "GET", response = classOf[Vector[String]], value = "Returns a list of Columns in Table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to get columns")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gives list of columns in Table")))
  def getColumnsPath = pathPrefix(Segment / "get-columns") { tableName =>
        get {
          logRequest(s"GET /$root/tables/$tableName/get-columns", Logging.InfoLevel) {
            complete(tableDao.getAllTableColumns(tableName))
          }
      } ~
        pathEnd(get(redirect(s"/$root/tables/$tableName/get-columns/", StatusCodes.TemporaryRedirect)))
  }


  @Path("/{tableName}/drop-columns")
  @ApiOperation(httpMethod = "DELETE", response = classOf[OkResponse], value = "Drop Columns to DB table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to add columns"),
    new ApiImplicitParam(name = "columnsToDrop", required = true, paramType = "body",
      dataType = "string", value = "Please provide the columns to drop")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully drops columns from the table")))
  def dropColumnsPath = pathPrefix(Segment / "drop-columns") { tableName =>
    delete {
      entity(as[Seq[String]]) { body =>
        logRequest(s"DELETE /$root/tables/$tableName/drop-columns", Logging.InfoLevel) {
          complete(tableDao.dropColumn(tableName, body))
        }
      }
    }
  }

  @Path("/{tableName}/insert-records")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Insert records to DB table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to insert records"),
    new ApiImplicitParam(name = "recordsToInsert", required = true, paramType = "body",
      dataType = "sequential.data.Records", value = "Please provide the records to insert")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully inserts records into the table")))
  def insertRecordsPath = pathPrefix(Segment / "insert-records") { tableName =>
    post {
      entity(as[Seq[Seq[Records]]]) { body =>
        logRequest(s"POST /$root/tables/$tableName/insert-records", Logging.InfoLevel) {
          complete(tableDao.insertRecords(tableName, body))
        }
      }
    }
  }

  @Path("/{tableName}/update-records")
  @ApiOperation(httpMethod = "PUT", response = classOf[CreateTable], value = "Update records in DB table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to update records"),
    new ApiImplicitParam(name = "recordsToUpdate", required = true, paramType = "body",
      dataType = "sequential.data.UpdateRecords", value = "Please provide the records to update")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully updates records in the table")))
  def updateRecordsPath = pathPrefix(Segment / "update-records") { tableName =>
    put {
      entity(as[UpdateRecords]) { body =>
        logRequest(s"PUT /$root/tables/$tableName/update-records", Logging.InfoLevel) {
          complete(tableDao.updateRecords(tableName, body))
        }
      }
    }
  }

  @Path("/{tableName}/get-data")
  @ApiOperation(httpMethod = "GET", response = classOf[Vector[String]], value = "Returns records from Table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to get data")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gives list of records from Table")))
  def getRecordsPath = pathPrefix(Segment / "get-data") { tableName =>
    get {
      logRequest(s"GET /$root/tables/$tableName/get-data", Logging.InfoLevel) {
        complete(tableDao.getAllRecords(tableName))
      }
    } ~
      pathEnd(get(redirect(s"/$root/tables/$tableName/get-data", StatusCodes.TemporaryRedirect)))
  }

  @Path("/{tableName}/get-records-by-columns")
  @ApiOperation(httpMethod = "GET", response = classOf[Vector[String]], value = "Returns records from Table by columns")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to get data"),
    new ApiImplicitParam(name = "columnName", required = true, paramType = "query",
      dataType = "string", value = "Please provide the column names to get data", allowMultiple = true)))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gives list of records from Table")))
  def getRecordsByColumnPath = pathPrefix(Segment / "get-records-by-columns") { tableName =>
    get {
      logRequest(s"GET /$root/tables/$tableName/get-records-by-columns", Logging.InfoLevel) {
        parameters('columnName.*) { (columnName) =>
          complete(tableDao.getRecordsByColumn(tableName, columnName.toSeq))
        }
      }
    } ~
      pathEnd(get(redirect(s"/$root/tables/$tableName/get-records-by-columns", StatusCodes.TemporaryRedirect)))
  }

  @Path("/{tableName}/create-mapping")
  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Map columns to DB table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to map columns"),
    new ApiImplicitParam(name = "columnsToMap", required = true, paramType = "body",
      dataType = "sequential.data.Records", value = "Please provide the columns to map")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully maps table columns")))
  def addColumnMappingPath = pathPrefix(Segment / "create-mapping") { tableName =>
    post {
      entity(as[Seq[Records]]) { body =>
        logRequest(s"POST /$root/tables/$tableName/create-mapping", Logging.InfoLevel) {
          complete(tableDao.createMapping(tableName, body))
        }
      }
    }
  }

  @Path("/{tableName}/update-mapping")
  @ApiOperation(httpMethod = "PUT", response = classOf[OkResponse], value = "Updates column mapping for table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to update records"),
    new ApiImplicitParam(name = "recordsToUpdate", required = true, paramType = "body",
      dataType = "sequential.data.Records", value = "Please provide the records to update")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully updates mapping records in the table")))
  def updateColumnMappingPath = pathPrefix(Segment / "update-mapping") { tableName =>
    put {
      entity(as[Seq[Records]]) { body =>
        logRequest(s"PUT /$root/tables/$tableName/update-mapping", Logging.InfoLevel) {
          complete(tableDao.updateMapping(tableName, body))
        }
      }
    }
  }

  @Path("/{tableName}/get-mapping")
  @ApiOperation(httpMethod = "GET", response = classOf[Vector[ColumnMapping]],value = "Returns mapped Columns in Table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to get mapping columns")))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully gives list of mapped columns in Table")))
  def getMappingColumnsPath = pathPrefix(Segment / "get-mapping") { tableName =>
    get {
      logRequest(s"GET /$root/tables/$tableName/get-mapping", Logging.InfoLevel) {
        complete(tableDao.getTableColumnMapping(tableName))
      }
    } ~
      pathEnd(get(redirect(s"/$root/tables/$tableName/get-mapping/", StatusCodes.TemporaryRedirect)))
  }

  @Path("/{tableName}/delete-mapping")
  @ApiOperation(httpMethod = "DELETE", response = classOf[OkResponse], value = "Drop mapping Columns from table")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
    dataType = "string", value = "Please provide the table name to add columns"),
    new ApiImplicitParam(name = "columnsToDrop", required = true, paramType = "body",
      dataType = "string", value = "Please provide the columns to delete mapping")
  ))
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully drops mapping columns from the table")))
  def dropMappingColumnsPath = pathPrefix(Segment / "delete-mapping") { tableName =>
    delete {
      entity(as[Seq[String]]) { body =>
        logRequest(s"DELETE /$root/tables/$tableName/delete-mapping", Logging.InfoLevel) {
          complete(tableDao.dropColumnMapping(tableName, body))
        }
      }
    }
  }

//  @Path("/{tableName}/upload-data")
//  @ApiOperation(httpMethod = "POST", response = classOf[OkResponse], value = "Loads data into table", consumes = "multipart/form-data")
//  @ApiImplicitParams(Array(new ApiImplicitParam(name = "tableName", required = true, paramType = "path",
//    dataType = "string", value = "Please provide the table name to load data"),
//    new ApiImplicitParam(name = "uploadFile", required = true, paramType = "formData", dataType = "file",
//      value = "Please upload a file to load data")
//  ))
//  @ApiResponses(Array(new ApiResponse(code = 200, message = "Successfully loads data into the table")))
//  def uploadFile = path(Segment / "upload-data") { tableName =>
//    (post & withoutSizeLimit & extractDataBytes) { fileData =>

//      Source.single(ByteString("""header1,header2,header3
//                             |1,2,3
//                             |4,5,6""".stripMargin))
//        .via(CsvParsing.lineScanner())
//        .via(CsvToMap.toMap())
//        .runWith(Sink.head)
////        .map(_.mapValues(_.utf8String))
////        .runForeach(println)
//
////      Source
////        .single(ByteString("""eins,zwei,drei
////                             |1,2,3""".stripMargin))
////        .via(CsvParsing.lineScanner())
////        .via(CsvToMap.toMap())
////        .runWith(Sink.head)
//
//
//        val finishedWriting = fileData //.runWith(FileIO.toPath(new File("/tmp/example.out").toPath))
//
//        //        val fileName = UUID.randomUUID().toString
//        //        val temp = System.getProperty("java.io.tmpdir")
//        //        val filePath = temp + "/" + fileName
//        println(fileData)
////      onComplete(finishedWriting) { ioResult =>
//        complete("Finished writing data: ")
////      }
////        complete(tableDao.getTableColumnMapping(tableName))
//      //        complete(processFile(filePath, fileData))
//    }
//  }

  private def processFile(filePath: String, fileData: Multipart.FormData) = {
    val fileOutput = new FileOutputStream(filePath)
    fileData.parts.mapAsync(1) { bodyPart ⇒
      def writeFileOnLocal(array: Array[Byte], byteString: ByteString): Array[Byte] = {
        val byteArray: Array[Byte] = byteString.toArray
        fileOutput.write(byteArray)
        array ++ byteArray
      }
      bodyPart.entity.dataBytes.runFold(Array[Byte]())(writeFileOnLocal)
    }.runFold(0)(_ + _.length)
  }


  val referenceDataRoutes = pathPrefix("tables"){
    createTablePath ~
      getTablesPath ~
      addColumnsPath ~
      getColumnsPath ~
      dropColumnsPath ~
      insertRecordsPath ~
      updateRecordsPath ~
      getRecordsPath ~
      getRecordsByColumnPath ~
      addColumnMappingPath ~
      getMappingColumnsPath ~
      updateColumnMappingPath ~
      dropMappingColumnsPath //~
//      uploadFile
  }

}
