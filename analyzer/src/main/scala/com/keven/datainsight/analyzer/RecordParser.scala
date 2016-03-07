package com.keven.datainsight.analyzer

import net.liftweb.json._

/**
  * Created by theone4ever on 07/03/16.
  */

abstract class Event(id: String)
case class RoadClosedEvent(id: String, location: (Float, Float)) extends Event(id){
  override def toString(): String = "RoadClosedEvent(id: %s, location: %s)".format(id, location)
}
case class RoadAccidentEvent(id: String, location: Option[(Float, Float)]) extends Event(id){
  override def toString(): String = "RoadAccidentEvent(id: %s, location: %s)".format(id, location)
}
case class TrafficMessageEvent(id: String) extends Event(id) {
  override def toString(): String = "TrafficMessageEvent(id: %s)".format(id)
}


object RecordParser {

  def date2accident(input: (String, String)): (java.util.Date, List[Event]) = {
    (parseTime(input._1), parseRecord(input._2))
  }

  def parseLocation(wgs84: String)={
    val pair = wgs84.substring(wgs84.indexOf("(")+1, wgs84.indexOf(")")).split(" ")
    (pair(1).toFloat, pair(0).toFloat)

  }

  implicit val formats = net.liftweb.json.DefaultFormats

  def parseRecord(input: String): List[Event] = {

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
      deviation => deviation.Deviation.map(accident =>
        accident match {
          case AccidentType(None, "trafficMessage", id) => new TrafficMessageEvent(id)
          case AccidentType(Some(geometry), "roadClosed", id) =>
            new RoadClosedEvent(id, parseLocation(geometry.WGS84))
          case AccidentType(Some(geometry), "roadAccident", id) =>
            new RoadAccidentEvent(id, Some(parseLocation(geometry.WGS84)))

            //TODO: really strange, road accident without location!!!
          case AccidentType(None, "roadAccident", id) =>
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            println(accident)
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

            new RoadAccidentEvent(id, None)
          case _ => throw new Exception("Unknown event type: %s".format(accident))
        }
      ))
  }

  def parseTime(fileName: String)={
    val name = fileName.split("/").last
    val timeStr = name.substring(0,name.indexOf(".json"))
    val format = new java.text.SimpleDateFormat("yy-MM-dd-HH-mm-ss")
    format.parse(timeStr)
  }


}
