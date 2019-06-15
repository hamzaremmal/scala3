# Comment parsing
## Files from dotty-doc requiring work
* util.traversing
  * ImplicitlyAddedFrom
* Comment
  * Span + error printing
  * Packages map
  * Figure out ShortHtml
* CommentParser
  * ParseWikiAtSymbol (useless?)
  * Span + error printing
* HTMLParsers
  * Packages map
  * Multi level list not working when ordered and unordered are combined
* WikiParser (just error reporting (span))
  * Span + error printing

* method linking
* Markdown parser broken
* linking method in userdoc

# Other
* handle things such as $genericCanBuildFromInfo

# Representation
* Annotations (+ everywhere or only where it makes sense?)
* Make sense of the parents field
* Id for typeparams so we can link
* Inherited declaration
* User doc for inherited method
* Alias type
* Beautify type parameters (+ variance + bound two direction)
* Known subclasses
* Try type at top level
* Type like Graph.Node

# Output
* Output inner class/object if not inherited
* Class inside class print parents
* Doc for packages

# Report
* Goal Why we wanted to generated from tasty why to markdown
* General architecture, same intermediate interface as dotty doc
* intermediate representation md to xxxx
* Difficulties, problems with dottydoc + md libraries
* benchmark?
* Say everything is split into function
* Linking to type with a def with the same name

# To Ask
* Too much operations on String for path?
* Case classes are linked right now
* Abstract flag not working
* default value in def