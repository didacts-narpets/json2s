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

  def classFor(value: JValue, paramName: String): (Seq[Tree], Type) = value match {
    case s: JString => (Nil, StringClass)
    case i: JInt => (Nil, IntClass)
    case d: JDouble => (Nil, DoubleClass)
    case o: JObject =>
      val (tree, tpe) = generateClassFromJObject(o, toUpperCamel(paramName))
      (List(tree), tpe)
    case a: JArray =>
      val (trees, arrType) = classFor(a.arr.head, paramName)
      (trees, ArrayClass TYPE_OF arrType)
    // TODO: generateClassFromJArray(a, toUpperCamel(paramName)).tpe
    case x => throw new Error("Don't know how to handle " + x)
  }

  def generateClassFromJArray(json: JArray, className: String): (Tree, Type) = {
    // TODO: if it is array of JObects,
    // assume they are all of the same type -
    // then, resolve missing values with Options
    ???
  }

  def toUpperCamel(str: String) = {
    val WordStart = "(^|[-_ \\.]+)(\\w)".r
    WordStart.replaceAllIn(str, _ match {
      case Regex.Groups(wordBreak, firstChar) =>
        firstChar.toUpperCase // discard break and upper first char
    })
  }

  def generateClassFromJObject(json: JObject, className: String): (Tree, Type) = {
    val TopCaseClass = RootClass.newClass(className)
    println("TopCaseClass="+TopCaseClass+"="+treeToString(TopCaseClass))
    // TODO: refactor for readability
    val (moreClasses: Seq[Tree], params: Seq[ValDef]) = ((Seq[Tree](),Seq[ValDef]()) /: json.obj.toList) {
      case ((treesSoFar, valsSoFar), (name: String, value)) =>
        val (classDefs: Seq[Tree],thisClass: Type) = classFor(value, name)
        println("thisClass="+thisClass+"="+treeToString(thisClass))
      (treesSoFar ++ classDefs, valsSoFar :+ PARAM(name, thisClass).empty)
    }

    val newClass: Tree = CASECLASSDEF(TopCaseClass).withParams(params.toIterable)
    val codeBlock = BLOCK {
      (moreClasses :+ newClass).toIterable
    }.withoutPackage

    (codeBlock, className)
  }

  def demo() = {
    val youtubeResponse = getClass.getResourceAsStream("youtube.json")
    val jObject = JsonParser.parse(new InputStreamReader(youtubeResponse)) match {
      case obj: JObject => obj
      case x => throw new Error("Expected JObject, got " + x)
    }

    val (genClass,_) = generateClassFromJObject(jObject, "YouTubeResponse")
    println(genClass)
    println(treeToString(genClass))
  }

  def main(args: Array[String]) = demo()
}