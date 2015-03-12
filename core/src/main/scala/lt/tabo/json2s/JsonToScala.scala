package lt.tabo.json2s

import java.io.InputStreamReader

import org.json4s.JsonAST._
import org.json4s.native._
import org.json4s.DefaultFormats
import scala.util.matching.Regex
import lt.tabo.json2s.Utils.{canBeDate, toUpperCamel, toSingular}
import lt.tabo.json2s.code._

case class JsonToScala(json: JValue, className: String) {
  def asJson = prettyJson(renderJValue(json))
  def asScala = {
    import JsonToScala._
    treesToString(classFor(json, className)._1)
  }
}

object JsonToScala {
  def classFor(value: JValue, paramName: String): (Seq[CaseClassStub], ClassType) = value match {
    case JString(s) =>
      if (canBeDate(s)) (Nil, DateClass)
      else (Nil, StringClass)
    case i: JInt => (Nil, IntClass)
    case d: JDouble => (Nil, DoubleClass)
    case b: JBool => (Nil, BooleanClass)
    case o: JObject => generateClassFromJObject(o, toUpperCamel(paramName))
    case a: JArray => classForJArray(a, paramName)
    case JNull => (Nil, AnyClass)
    case x => throw new Error("Don't know how to handle " + x)
  }

  def classForJArray(json: JArray, paramName: String): (Seq[CaseClassStub], ClassType) = {
    val arr = json.arr
    def terminal(t: ClassType) = (Nil: Seq[CaseClassStub], t)
    val (trees: Seq[CaseClassStub], arrType: ClassType) =
      if (arr.isEmpty) terminal(NothingClass)
      else if (arr.forall(_.isInstanceOf[JString])) {
        if (arr.forall(js => canBeDate(js.asInstanceOf[JString].s))) terminal(DateClass)
        else terminal(StringClass)
      }
      else if (arr.forall(_.isInstanceOf[JInt])) terminal(IntClass)
      else if (arr.forall(_.isInstanceOf[JDouble])) terminal(DoubleClass)
      else if (arr.forall(_.isInstanceOf[JBool])) terminal(BooleanClass)
      else if (arr.forall(_.isInstanceOf[JObject]))
        generateClassFromJObjects(arr.map(_.asInstanceOf[JObject]), toUpperCamel(toSingular(paramName)))
      else if (arr.forall(_.isInstanceOf[JArray]))
        // not safe - assume all arrays are of the same type, just use the first one
        classForJArray(arr.head.asInstanceOf[JArray], paramName)
      else throw new Error("Array types are not all the same in " + json)
    (trees, ListClass(arrType))
  }

  def getParamsForJObject(json: JObject): (Seq[CaseClassStub], Seq[BasicParam]) = {
    val (moreClasses: Seq[CaseClassStub], params: Seq[BasicParam]) = ((Seq[CaseClassStub](),Seq[BasicParam]()) /: json.obj.toList) {
      case ((treesSoFar, valsSoFar), (paramName: String, paramValue)) =>
        val (classDefs: Seq[CaseClassStub],paramClass: ClassType) = classFor(paramValue, paramName)
        (treesSoFar ++ classDefs, valsSoFar :+ BasicParam(paramName, paramClass))
    }
    (moreClasses, params)
  }

  def generateClassFromJObjects(jsons: List[JObject], className: String): (Seq[CaseClassStub], ClassType) = {
    val objectParams: List[(Seq[CaseClassStub], Seq[BasicParam])] =
      jsons.map(getParamsForJObject)
    val (moreClasses, params) = objectParams.reduce((x,y) => (x,y) match {
      case ((someClasses1, someParams1), (someClasses2, someParams2)) =>
        // TODO: a recursive merge - eg resolving nested differences with options
        val mergeClasses = (someClasses1 ++ someClasses2).groupBy(_.name).map(_._2.head).toSeq
        // the boolean means "optional"
        val (optParams1, reqPs1) = someParams1.partition(_.tpe.name == "Option")
        val (optParams2, reqPs2) = someParams2.partition(_.tpe.name == "Option")
        val mergeOpts = (optParams1 ++ optParams2).groupBy(_.name).mapValues(_.head).toList
        val mergeReqs = {
          (reqPs1 ++ reqPs2).groupBy(_.name).mapValues {
            case Seq(one) => one.copy(tpe=OptionClass(one.tpe))
            case Seq(one, two) => one
            case xs => xs.head
          }
        }
        val mergeParams = mergeOpts ++ mergeReqs
        (mergeClasses, mergeParams.map(_._2))
    })

    val newClass: CaseClassStub = CaseClassStub(className, params)

    ((moreClasses :+ newClass).toSeq, ClassType(className))
  }

  def generateClassFromJObject(json: JObject, className: String): (Seq[CaseClassStub], ClassType) = {
    generateClassFromJObjects(List(json), className)
  }

  def treesToString(trees: Iterable[CaseClassStub]) = {
    trees.map(_.render).mkString("\n")
  }

  def classForExamples(jsons: Seq[String], className: String): String = {
    treesToString {
      generateClassFromJObjects(jsons.toList.map(JsonParser.parse(_) match {
        case obj: JObject => obj
        case x => throw new IllegalArgumentException("Expected JObject, got " + x)
      }), className)._1
    }
  }

  def apply(json: String, className: String): JsonToScala = {
    apply(JsonParser.parse(json), className)
  }

  def demo() = {
    val youtubeResponse = getClass.getResourceAsStream("youtube.json")
    val jObject = JsonParser.parse(new InputStreamReader(youtubeResponse)) match {
      case obj: JObject => obj
      case x => throw new Error("Expected JObject, got " + x)
    }

    println(apply(jObject, "YouTubeResponse"))
  }

  def main(args: Array[String]) = demo()
}
