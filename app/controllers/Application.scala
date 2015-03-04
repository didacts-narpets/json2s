package controllers

import lt.tabo.json2s.JsonToScala
import org.json4s.JsonAST.JValue
import org.json4s.native._
import play.api.data.{Mapping, Form}
import play.api.data.Forms.{mapping, text}
import play.api.mvc._

object Application extends Controller {

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
  )

  def submit = Action { implicit request =>
    jsonForm.bindFromRequest.value.map { j2s =>
      Ok(views.html.submitted(j2s))
    }.getOrElse(BadRequest)
  }
}