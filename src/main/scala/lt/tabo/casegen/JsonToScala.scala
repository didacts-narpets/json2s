package lt.tabo.casegen

import java.io.InputStreamReader

import org.json4s.JsonAST.JObject
import org.json4s.native.JsonParser
import treehugger.forest._
import definitions._
import treehuggerDSL._

object JsonToScala {
  def generateClassFromJson(json: JObject, className: String): Tree = {
    val TopCaseClass = RootClass.newClass(className)

    val values: Iterable[Tree] = Seq() // TODO: Define inner values of case class

    CASECLASSDEF(TopCaseClass) := BLOCK(values)
  }

  def demo() = {
    val youtubeResponse = getClass.getResourceAsStream("youtube.json")
    val jObject = JsonParser.parse(new InputStreamReader(youtubeResponse)) match {
      case obj: JObject => obj
      case x => throw new Error("Expected JObject, got " + x)
    }

    val genClass = generateClassFromJson(jObject, "YouTubeResponse")
    println(treeToString(genClass))
  }

  def main(args: Array[String]) = demo()
}
