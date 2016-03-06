import net.liftweb.json._

val inputRecord =  """
        {"RESPONSE":
         {"RESULT":
         [{"Situation": [
            {
              "Deviation":
                [
                  {"Geometry": {"WGS84": "POINT (16.8348179 61.236805)"}, "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_1430222"}
                ]
            },
            {"Deviation": [{"Geometry": {"WGS84": "POINT (15.5838528 56.5801468)"}, "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_4705932"}]},
            {"Deviation": [{"Geometry": {"WGS84": "POINT (11.4423208 58.8538551)"}, "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_10623074"}]}
          ]
          }]}}
                   """
val inputRecord2 =
  """{"RESPONSE":
     {"RESULT":
     [{"Situation": [
       {"Deviation":
        [
         {"Geometry": {"WGS84": "POINT (16.8501263 59.01875)"}, "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_10217079"}
        ]
       },
       {"Deviation":
         [
           {"Geometry": {"WGS84": "POINT (17.591959 59.20937)"}, "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_10217179"}
         ]
        },
        {"Deviation":
           [{"IconId": "trafficMessage", "Id": "SE_STA_TRISSID_1_9645247"}, {"Geometry": {"WGS84": "POINT (13.8228617 59.3933945)"},
            "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_10623154"}, {"Geometry": {"WGS84": "POINT (13.8228617 59.3933945)"},
            "IconId": "roadClosed", "Id": "SE_STA_TRISSID_2_10623154"}]}, {"Deviation": [{"Geometry": {"WGS84": "POINT (14.15505 58.02408)"},
             "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_4706052"}]}, {"Deviation": [{"Geometry": {"WGS84": "POINT (12.0132694 57.8347626)"},
              "IconId": "roadAccident", "Id": "SE_STA_TRISSID_1_10623174"}]}]}]}}"""
//val json = parse(inputRecord)
//val result = json.extract[RecordType]
//result.RESPONSE.RESULT.head.Situation

val wgs84 = "POINT (16.8348179 61.236805)"

implicit val formats = net.liftweb.json.DefaultFormats

def parseLocation(wgs84: String)={
  val pair = wgs84.substring(wgs84.indexOf("(")+1, wgs84.indexOf(")")).split(" ")
  (pair(1).toFloat, pair(0).toFloat)
}
class Accident(location: (Float, Float), id: String, iconId: String) {
  override def toString(): String = "Accident{Location:" + location + ", id:" + id + ", iconId:"+ iconId+"}";
}


def parseRecord(input: String): List[Accident] = {

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



/* Test*/
parseLocation(wgs84)
val result = parseRecord(inputRecord2)