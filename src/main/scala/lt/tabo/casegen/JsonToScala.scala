package lt.tabo.casegen

import java.io.InputStreamReader

import org.json4s.JString
import org.json4s.JsonAST._
import org.json4s.native.JsonParser
import treehugger.forest._
import definitions._
import treehuggerDSL._

import scala.util.matching.Regex

object JsonToScala {

  def classFor(value: JValue, paramName: String, parent: ClassSymbol=RootClass): Type = value match {
    case s: JString => StringClass
    case i: JInt => IntClass
    case d: JDouble => DoubleClass
    case o: JObject => generateClassFromJObject(o, toUpperCamel(paramName), parent).tpe
    case a: JArray => ArrayClass TYPE_OF classFor(a.arr.head, paramName, parent)
    // TODO: generateClassFromJArray(a, toUpperCamel(paramName)).tpe
    case x => throw new Error("Don't know how to handle " + x)
  }

  def generateClassFromJArray(json: JArray, className: String): Tree = {
    ???
  }

  def toUpperCamel(str: String) = {
    val WordStart = "(^|[-_ \\.]+)(\\w)".r
    WordStart.replaceAllIn(str, _ match {
      case Regex.Groups(wordBreak, firstChar) =>
        firstChar.toUpperCase // discard break and upper first char
    })
  }

  def generateClassFromJObject(json: JObject, className: String, parent: ClassSymbol=RootClass): Tree = {
    val TopCaseClass = parent.newClass(className)

    val params: Iterable[ValDef] = for ((name, value) <- json.obj.toIterable) yield {
      PARAM(name, classFor(value, name, TopCaseClass)).empty
    }

    CASECLASSDEF(TopCaseClass).withParams(params)
  }

  def demo() = {
    val youtubeResponse = getClass.getResourceAsStream("youtube.json")
    val jObject = JsonParser.parse(new InputStreamReader(youtubeResponse)) match {
      case obj: JObject => obj
      case x => throw new Error("Expected JObject, got " + x)
    }

    val genClass = generateClassFromJObject(jObject, "YouTubeResponse")
    println(genClass)
    println(treeToString(genClass))
  }

  def main(args: Array[String]) = demo()
}