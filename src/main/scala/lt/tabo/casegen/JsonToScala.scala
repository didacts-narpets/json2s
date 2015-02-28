package lt.tabo.casegen

import java.io.InputStreamReader

import org.json4s.JString
import org.json4s.JsonAST._
import org.json4s.native.JsonParser
import treehugger.forest._
import treehugger.forest.definitions._
import treehugger.forest.treehuggerDSL._

import scala.util.matching.Regex

object JsonToScala {

  def classFor(value: JValue, paramName: String): (Seq[Tree], Type) = value match {
    case s: JString => (Nil, StringClass)
    case i: JInt => (Nil, IntClass)
    case d: JDouble => (Nil, DoubleClass)
    case o: JObject => generateClassFromJObject(o, toUpperCamel(paramName))
    case a: JArray => classForJArray(a, paramName)
    // TODO: generateClassFromJArray(a, toUpperCamel(paramName)).tpe
    case x => throw new Error("Don't know how to handle " + x)
  }

  def classForJArray(json: JArray, paramName: String): (Seq[Tree], Type) = {
    val arr = json.arr
    def primitive(t: Type) = (Nil: Seq[Tree], t)
    val (trees: Seq[Tree], arrType: Type) =
      if (arr.isEmpty) primitive(NothingClass)
      else if (arr.forall(_.isInstanceOf[JString])) primitive(StringClass)
      else if (arr.forall(_.isInstanceOf[JInt])) primitive(IntClass)
      else if (arr.forall(_.isInstanceOf[JDouble])) primitive(DoubleClass)
      else if (arr.forall(_.isInstanceOf[JObject]))
        generateClassFromJObjects(arr.map(_.asInstanceOf[JObject]), toUpperCamel(paramName))
      else if (arr.forall(_.isInstanceOf[JArray]))
        // not safe - assume all arrays are of the same type, just use the first one
        classForJArray(arr.head.asInstanceOf[JArray], paramName)
      else throw new Error("Array types are not all the same in " + json)
    (trees, TYPE_LIST(arrType))
  }

  /** adds backticks for names with characters like spaces etc
    * note that treehugger already adds backticks for scala keywords (like `type` or `val`)
    */
  def quotedName(name: String) = {
    if (name.matches("[a-zA-Z_][\\w\\d_]*")) name
    else '`' + name + '`'
  }

  def toUpperCamel(str: String) = {
    val WordStart = "(^|[-_ \\.]+)(\\w)".r
    WordStart.replaceAllIn(str, _ match {
      case Regex.Groups(wordBreak, firstChar) =>
        firstChar.toUpperCase // discard break and upper first char
    })
  }

  def getParamsForJObject(json: JObject): (Seq[Tree], Seq[(String, Type)]) = {
    val (moreClasses: Seq[Tree], params: Seq[(String,Type)]) = ((Seq[Tree](),Seq[(String,Type)]()) /: json.obj.toList) {
      case ((treesSoFar, valsSoFar), (name: String, value)) =>
        val (classDefs: Seq[Tree],thisClass: Type) = classFor(value, name)
        (treesSoFar ++ classDefs, valsSoFar :+ (name, thisClass))
    }
    (moreClasses, params)
  }

  def generateClassFromJObjects(jsons: List[JObject], className: String): (Seq[Tree], Type) = {
    val TopCaseClass = RootClass.newClass(className)

    val objectParams: List[(Seq[Tree], Seq[(String, Type, Boolean)])] =
      jsons.map(getParamsForJObject).map { case (trees, ps) =>
        (trees, ps.map { case (name, classType) => (name, classType, false) })
      }
    val (moreClasses, params) = objectParams.reduce((x,y) => (x,y) match {
      case ((someClasses1, someParams1), (someClasses2, someParams2)) =>
        def xor[A](xs: Seq[A], ys: Seq[A]) = {
          println("xor(" + xs + ", " + ys + ")")
          xs.diff(ys) ++ ys.diff(xs)
        }
        // someClasses1 union someClasses2 - hack since .equals is not the same as the treeToString
        val mergeClasses = (someClasses1 ++ someClasses2).groupBy(treeToString(_)).map(_._2.head)
        // the boolean means "optional"
        val (optParams1, reqPs1) = someParams1.partition(_._3)
        val (optParams2, reqPs2) = someParams2.partition(_._3)
        val mergeOpts = (optParams1 ++ optParams2).groupBy(_._1).mapValues(_.head).toList
        val mergeReqs = {
          (reqPs1 ++ reqPs2).groupBy(_._1).mapValues {
            case Seq(one) => one.copy(_3 = true)
            case Seq(one, two) => one
            case xs => xs.head
          }
        }
        val mergeParams = mergeOpts ++ mergeReqs
        (mergeClasses, mergeParams.map(_._2))
    })

    val newClass: Tree = CASECLASSDEF(TopCaseClass).withParams(params.toIterable.map {
      case (name, classType, optional) =>
        PARAM(quotedName(name), if (optional) TYPE_OPTION(classType) else classType).empty
    })

    ((moreClasses :+ newClass).toSeq, className)
  }

  def generateClassFromJObject(json: JObject, className: String): (Seq[Tree], Type) = {
    generateClassFromJObjects(List(json), className)
  }

  def apply(json: JValue, className: String): String = {
    treeToString(BLOCK {
      classFor(json, className)._1.toIterable
    })
  }

  def apply(jsons: Seq[String], className: String): String = {
    treeToString(BLOCK {
      generateClassFromJObjects(jsons.toList.map(JsonParser.parse(_) match {
        case obj: JObject => obj
        case x => throw new IllegalArgumentException("Expected JObject, got " + x)
      }), className)._1.toIterable
    })
  }

  def apply(json: String, className: String): String = {
    apply(JsonParser.parse(json), className)
  }

  def demo() = {
    val youtubeResponse = getClass.getResourceAsStream("youtube.json")
    val jObject = JsonParser.parse(new InputStreamReader(youtubeResponse)) match {
      case obj: JObject => obj
      case x => throw new Error("Expected JObject, got " + x)
    }

    val (genClass,_) = generateClassFromJObject(jObject, "YouTubeResponse")
    // println(genClass)
    println(treeToString(BLOCK(genClass)))
  }

  def main(args: Array[String]) = demo()
}