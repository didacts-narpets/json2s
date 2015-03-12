package lt.tabo.json2s

/**
 * @author iantabolt
 * @since 3/11/15
 */
package object code {
  val StringClass = ClassType.StringClass
  val IntClass = ClassType.IntClass
  val DoubleClass = ClassType.DoubleClass
  val BooleanClass = ClassType.BooleanClass
  val LongClass = ClassType.LongClass
  val NothingClass = ClassType.NothingClass
  val DateClass = ClassType.DateClass
  def ArrayClass(typeParam1: ClassType) = ClassType.ArrayClass(typeParam1)
  def SeqClass(typeParam1: ClassType) = ClassType.SeqClass(typeParam1)
  def ListClass(typeParam1: ClassType) = ClassType.ListClass(typeParam1)
  def OptionClass(typeParam1: ClassType) = ClassType.OptionClass(typeParam1)
}
