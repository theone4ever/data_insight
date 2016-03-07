package com.keven.datainsight.analyzer

/* SimpleApp.scala */

import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import java.io.File
import com.keven.datainsight.analyzer.RecordParser._


object SimpleApp {
  def main(args: Array[String]) {

    val files = readFileRecursive("/Volumes/data/ROAD_ACCIDENT").filter(_.getName.endsWith(".json"))

    val fileNames = files.map(_.getAbsoluteFile).mkString(",")
    val conf = new SparkConf().setAppName("Simple Application").setMaster("local[4]")
    val sc = new SparkContext(conf)
    val allData = sc.wholeTextFiles(fileNames)

    val date2event = allData.map(date2accident).flatMap(generatePair)
    date2event.cache()

    val roadAccidentEvents = date2event.filter(isRoadAccidentEvent)
    val trafficMessageEvents = date2event.filter(isTrafficMessageEvent)
    val roadClosedEvents = date2event.filter(isRoadClosedEvent)

    println("%s road accident events".format(roadAccidentEvents.count()))
    println("%s road closed events".format(roadClosedEvents.count()))
    println("%s traffic message events".format(trafficMessageEvents.count()))
  }


  def readFileRecursive(directory: String): Array[File] = {
    val files = new java.io.File(directory).listFiles
    files.filter(_.isFile) ++ files.filter(_.isDirectory).flatMap(file => readFileRecursive(file.getAbsolutePath))
  }

  def generatePair(pair:(java.util.Date, List[Event])): List[(java.util.Date, Event)] = {
    List(pair._1).zipAll(pair._2, pair._1, new TrafficMessageEvent("abc"))
  }

  def isRoadAccidentEvent(pair: (java.util.Date, Event)) = {
    pair._2 match {
      case RoadAccidentEvent(_, _)=>true
      case _ => false
    }
  }

  def isRoadClosedEvent(pair: (java.util.Date, Event)) = {
    pair._2 match {
      case RoadClosedEvent(_, _)=>true
      case _ => false
    }
  }

  def isTrafficMessageEvent(pair: (java.util.Date, Event)) = {
    pair._2 match {
      case TrafficMessageEvent(_)=>true
      case _ => false
    }
  }



}