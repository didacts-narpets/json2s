package lt.tabo.json2s.code

import lt.tabo.json2s.Utils

/**
 * @author iantabolt
 * @since 3/11/15
 */
case class BasicParam(name: String, tpe: ClassType) extends CodeTree {
  def render: String = Utils.quotedName(name) + ": " + tpe.render
}