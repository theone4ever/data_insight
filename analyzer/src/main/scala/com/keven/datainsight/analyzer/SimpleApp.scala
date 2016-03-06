package com.keven.datainsight.analyzer

/* SimpleApp.scala */

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import java.io.File
import net.liftweb.json._






object SimpleApp {
  def main(args: Array[String]) {

    val files = readFileRecursive("/Volumes/data/ROAD_ACCIDENT").filter(_.getName.endsWith(".json"))

    val fileNames = files.map(_.getAbsoluteFile).mkString(",")
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val allData = sc.wholeTextFiles(fileNames)

    val d2a = allData.map(date2accident)
    println(d2a.count())

//    println("Total %s files".format(allData.count()))
//    val jsons = allData.map(str=>JSON.parseFull(str))
//    jsons.first()
  }


  def readFileRecursive(directory: String): Array[File] ={
    val files = new java.io.File(directory).listFiles
    files.filter(_.isFile) ++ files.filter(_.isDirectory).flatMap(file=>readFileRecursive(file.getAbsolutePath))
  }


  def date2accident(input: (String, String)){
    (parseTime(input._1), parseRecord(input._2))
  }



  def parseLocation(wgs84: String)={
    val pair = wgs84.substring(wgs84.indexOf("(")+1, wgs84.indexOf(")")).split(" ")
    (pair(1).toFloat, pair(0).toFloat)

  }



  class Accident(location: (Float, Float), id: String, iconId: String) {
    override def toString(): String = "Accident{Location:" + location + ", id:" + id + ", iconId:"+ iconId+"}";
  }


  implicit val formats = net.liftweb.json.DefaultFormats

  def parseRecord(input: String): List[Accident] = {
    try {
      case class GeometryType(WGS84: String)
      case class AccidentType(Geometry: Option[GeometryType], IconId: String, Id: String) {
        def this(IconId: String, Id: String) = this(new Some(GeometryType("POINT (0.0 0.0)")), IconId, Id)
      }
      case class DeviationType(Deviation: List[AccidentType])
      case class SituationType(Situation: List[DeviationType])
      case class ResultType(RESULT: List[SituationType])
      case class RecordType(RESPONSE: ResultType)
      val json = parse(input)
      val record = json.extract[RecordType]
      record.RESPONSE.RESULT.head.Situation.flatMap(
        deviation => deviation.Deviation.map(accident =>{
          new Accident(
            parseLocation(accident.Geometry.getOrElse(GeometryType("POINT (0.0 0.0)")).WGS84),
            accident.Id, accident.IconId)
        })
      )
    }
    catch {
      case e: Exception => println("Get Error when parsing, input is: %s".format(input))
        List[Accident]()
    }
  }

  def parseTime(fileName: String)={
    val name = fileName.split("/").last
    val timeStr = name.substring(0,name.indexOf(".json"))
    val format = new java.text.SimpleDateFormat("yy-MM-dd-HH-mm-ss")
    format.parse(timeStr)
  }

}