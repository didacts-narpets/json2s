package controllers

import lt.tabo.json2s.JsonToScala
import org.json4s.JsonAST.JValue
import org.json4s.native._
import play.api.data.{Mapping, Form}
import play.api.data.Forms.{mapping, text}
import play.api.mvc._

object Application extends Controller {
  val ExampleClassName = "Person"
  val ExampleJson = """{ "name": "joe",
                      |  "address": {
                      |    "street": "Bulevard",
                      |    "city": "Helsinki"
                      |  },
                      |  "children": [
                      |    {
                      |      "name": "Mary",
                      |      "age": 5,
                      |      "birthdate": "2004-09-04T18:06:22Z"
                      |    },
                      |    {
                      |      "name": "Mazy",
                      |      "age": 3
                      |    }
                      |  ]
                      |}""".stripMargin


  def index = Action {
    Ok(views.html.index(jsonForm))
  }

  // mapping for json
  val json: Mapping[JValue] =
    text.transform[JValue](
      JsonParser.parse,
      jval => prettyJson(renderJValue(jval)))

  val jsonForm: Form[JsonToScala] = Form(
    mapping(
      "json-input" -> json,
      "class-name" -> text
    )(JsonToScala(_,_))(JsonToScala.unapply)
  ).fill(JsonToScala(ExampleJson, ExampleClassName))

  def submit = Action { implicit request =>
    jsonForm.bindFromRequest.value.map { j2s =>
      Ok(views.html.submitted(j2s, jsonForm))
    }.getOrElse(BadRequest)
  }
}