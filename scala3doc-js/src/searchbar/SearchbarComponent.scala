package dotty.dokka

import org.scalajs.dom._
import org.scalajs.dom.html.Input

class SearchbarComponent(val callback: (String) => List[PageEntry]):
  extension (p: PageEntry)
    def toHTML =
      val wrapper = document.createElement("div")
      wrapper.classList.add("scala3doc-searchbar-result")
      wrapper.classList.add("monospace")

      val resultA = document.createElement("a").asInstanceOf[html.Anchor]
      resultA.href = Globals.pathToRoot + p.location
      resultA.text = s"${p.name}"

      val location = document.createElement("span")
      location.classList.add("pull-right")
      location.classList.add("scala3doc-searchbar-location")
      location.textContent = p.description

      wrapper.appendChild(resultA)
      wrapper.appendChild(location)
      wrapper

  def handleNewQuery(query: String) =
    val result = callback(query).map(_.toHTML)
    while (resultsDiv.hasChildNodes()) resultsDiv.removeChild(resultsDiv.lastChild)
    result.foreach(resultsDiv.appendChild)

  private val logoClick: html.Div =
    val span = document.createElement("span").asInstanceOf[html.Span]
    span.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20"><path d="M19.64 18.36l-6.24-6.24a7.52 7.52 0 10-1.28 1.28l6.24 6.24zM7.5 13.4a5.9 5.9 0 115.9-5.9 5.91 5.91 0 01-5.9 5.9z"></path></svg>"""
    span.id = "scala3doc-search"
    span.onclick = (event: Event) =>
      if (rootDiv.className.contains("hidden"))
        rootDiv.className = rootShowClasses
      else rootDiv.className = rootHiddenClasses

    val element = createNestingDiv("search-content")(
      createNestingDiv("search-conatiner")(
        createNestingDiv("search")(
          span
        )
      )
    )
    document.getElementById("scala3doc-searchBar").appendChild(element)
    element


  private val input: html.Input =
    val element = document.createElement("input").asInstanceOf[html.Input]
    element.id = "scala3doc-searchbar-input"
    element.addEventListener("input", (e) => handleNewQuery(e.target.asInstanceOf[html.Input].value))
    element

  private val resultsDiv: html.Div =
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.id = "scala3doc-searchbar-results"
    element

  private val rootHiddenClasses = "hidden"
  private val rootShowClasses   = ""

  private def createNestingDiv(className: String)(innerElement: html.Element): html.Div =
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.className = className
    element.appendChild(innerElement)
    element

  private val rootDiv: html.Div =
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.addEventListener("click", (e: Event) => e.stopPropagation())
    logoClick.addEventListener("click", (e: Event) => e.stopPropagation())
    document.body.addEventListener("click", (e: Event) => element.className = rootHiddenClasses)
    element.className = rootHiddenClasses
    element.id = "scala3doc-searchbar"
    element.appendChild(input)
    element.appendChild(resultsDiv)
    document.body.appendChild(element)
    element

  handleNewQuery("")
