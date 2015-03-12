package lt.tabo.json2s.code

/**
 * @author iantabolt
 * @since 3/11/15
 */
case class CaseClassStub(name: String, params: Seq[BasicParam]) extends CodeTree {
  def render = "case class " + name + "(" + params.map(_.render).mkString(", ") + ")"
}