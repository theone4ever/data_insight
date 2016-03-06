import scala.util.parsing.json._
import scala.collection.immutable._


val fileName = "file:/Volumes/data/ROAD_ACCIDENT/2016/2/20/16-02-20-15-00-35.json"
val json = "{\"RESPONSE\": {\"RESULT\": [{\"Situation\": [{\"Deviation\": [{\"Geometry\": {\"WGS84\": \"POINT (16.8348179 61.236805)\"}, \"IconId\": \"roadAccident\", \"Id\": \"SE_STA_TRISSID_1_1430222\"}]}, {\"Deviation\": [{\"Geometry\": {\"WGS84\": \"POINT (15.5838528 56.5801468)\"}, \"IconId\": \"roadAccident\", \"Id\": \"SE_STA_TRISSID_1_4705932\"}]}, {\"Deviation\": [{\"Geometry\": {\"WGS84\": \"POINT (11.4423208 58.8538551)\"}, \"IconId\": \"roadAccident\", \"Id\": \"SE_STA_TRISSID_1_10623074\"}]}]}]}}"

val name = fileName.split("/").last
val timeStr = name.substring(0,name.indexOf(".json"))
val format = new java.text.SimpleDateFormat("yy-MM-dd-HH-mm-ss")
val date = format.parse(timeStr)
val data = JSON.parseFull(json)
val response = data.getOrElse() match {
  case map: Map[String, Any]=> map.get("RESPONSE")
  case non_map => throw new Exception("NO RESPONSE found")
}

val result = response.getOrElse() match {
  case map: Map[String, Any]=>map.get("RESULT")
  case non_map => throw new Exception("No RESULT found")
}


val m3 = result.getOrElse() match {
  case list: List[Any]=> list.head
  case non_list => throw new Exception("Type mismatch 3")
}

val situation = m3 match {
  case map: Map[String, List[Any]]=> map.get("Situation")
  case non_map=> throw new Exception("No Situation found")
}

val accidentList = situation.getOrElse() match{
  case list: List[Map[String, List[Map[String, List[Map[String, Map[String, Any]]]]]]]=>list
  case non_list=> throw new Exception("No Accident found")
}

accidentList.map(accident=>accident.get(""))

def parseTime(fileName: String)={
  val name = fileName.split("/").last
  val timeStr = name.substring(0,name.indexOf(".json"))
  val format = new java.text.SimpleDateFormat("yy-MM-dd-HH-mm-ss")
  format.parse(timeStr)
}


parseTime(fileName)


















