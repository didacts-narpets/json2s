## json2s

This project will contain code to generate case classes from an example
json object. Extensions may possibly include xml or similar other input types.

### Motivation

Many scala Json libraries allow for json => case class conversion like so:


```scala
scala> import org.json4s._
scala> import org.json4s.native.JsonParser.parse
scala> implicit val formats = DefaultFormats // Brings in default date formats etc.
scala> case class Child(name: String, age: Int, birthdate: Option[java.util.Date])
scala> case class Address(street: String, city: String)
scala> case class Person(name: String, address: Address, children: List[Child])
scala> val json = parse("""
         { "name": "joe",
           "address": {
             "street": "Bulevard",
             "city": "Helsinki"
           },
           "children": [
             {
               "name": "Mary",
               "age": 5,
               "birthdate": "2004-09-04T18:06:22Z"
             },
             {
               "name": "Mazy",
               "age": 3
             }
           ]
         }
       """)

scala> json.extract[Person]
res0: Person = Person(joe,Address(Bulevard,Helsinki),List(Child(Mary,5,Some(Sat Sep 04 18:06:22 EEST 2004)), Child(Mazy,3,None)))
```
(taken from http://json4s.org/#extracting-values)

This makes for seamless access of json data through scala code.
This is a huge step up from manually mapping json objects to classes, but there
is still some tedious work to be done. Namely, creating the case classes to
correspond to the json objects (in the above example, `Child`, `Address`, and
`Person`). This can get quite difficult if you need to handle a large json
object (ie from an API response), since if even one field or type is wrong,
the `extract` will fail.

This project will attempt to automatically create the case classes for you so
that, in combination with `extract`, any json API that provides example output
can be quickly wrapped in awesome scala code.

### Example basic functionality
```scala
scala> import lt.tabo.json2s.JsonToScala
scala> JsonToScala("""
          { "name": "joe",
            "address": {
              "street": "Bulevard",
              "city": "Helsinki"
            },
            "children": [
              {
                "name": "Mary",
                "age": 5,
                "birthdate": "2004-09-04T18:06:22Z"
              },
              {
                "name": "Mazy",
                "age": 3
              }
            ]
          }
        """, "Person")
res0: String =
case class Address(street: String, city: String)
case class Child(age: Int, name: String, birthdate: Option[java.util.Date])
case class Person(name: String, address: Address, children: List[Child])
```

### Features
 - handles `java.util.Date`, more to come possibly?
 - convert array names to singular class names (eg `children: List[Child]`)
 - handle names with invalid scala characters by putting them in backticks

### TODO
 - add documentation, tests
 - make web interface for people to use
