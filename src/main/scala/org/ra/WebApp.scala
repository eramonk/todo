package org.ra

//import scala.scalajs.js.{ Dictionary, JSApp }
//import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
//import org.scalajs.dom
//import org.scalajs.dom.{ Event, html }
//import org.scalajs.dom.raw.HTMLElement
//
//import scala.collection.mutable
//import scala.scalajs.js
//
//@JSExport
//object WebApp1 extends JSApp {
//
//  @JSExport
//  override def main(): Unit = {
//    dom.document.addEventListener("DOMContentLoaded", (_: Event) => {
//      dom.document.body.outerHTML = "<body></body>"
//      //      bootstrap(dom.document.body)
//    })
//  }
//  //
//  //  def bootstrap(root: HTMLElement): Unit = {
//  //    println("loaded")
//  //  }
//
//}
//
//@JSExport
//object WebApp {
//  @JSExport
//  def main(div: html.Div) = {
//
//    val button = dom.document.getElementById("button")
//    button.addEventListener("click", { (e: dom.Event) =>
//
//      var listTodo: js.Array[js.Dictionary[Any]] = js.Array()
//      val e1 = e.asInstanceOf[dom.MouseEvent]
//      val text = dom.document.getElementById("input").asInstanceOf[html.Input].value
//      val temp: Dictionary[Any] = js.Dictionary("todo" -> text, "check" -> false)
//      //      println(temp("to-do"), temp("check"))
//
//      var i = listTodo.length
//      var text1 = ""
//
//      listTodo(i) = temp
//      //      println(listTodo(i)("to-do"), listTodo(i)("check"))
//      //      println(listTodo)
//      var quest = dom.document.createElement("div")
//      var quest1 = dom.document.createElement("div")
//      var body1 = dom.document.getElementById("div2")
//      text1 = listTodo(i)("todo").toString
//
//      val checkbox = dom.document.createElement("input")
//      checkbox.setAttribute("type", "checkbox")
//      quest1.textContent = text1
//      div.appendChild(quest)
//      quest.appendChild(quest1)
//      quest.appendChild(checkbox)
//
//      //      val temp = Map("to-do" -> text, "check" -> false)
//
//      //      println(text)
//
//    })
//
//    //    def out(list: js.Array[js.Dictionary[Any]]): Unit = {
//    //      for (
//    //        i <- list;
//    //        j <- i("to--do");
//    //      ) yield dom.document.getElementById("div").innerHTML = i.toString + "<br>"
//    //
//    //    }
//
//    //    val child = dom.document
//    //      .createElement("div")
//    //
//    //    child.id = "child"
//    //
//    //    child.textContent =
//    //      "To-do list"
//    //
//    //    div.appendChild(child)
//    //
//    //    val newTodoList = dom.document
//    //      .createElement("button")
//    //    newTodoList.textContent = "New List"
//    //    div.appendChild(newTodoList)
//    //
//    //    val newDiv = dom.document
//    //      .createElement("div")
//    //    div.appendChild(newDiv)
//
//    //        val inputNewElement = dom.document
//    //          .createElement("input")
//    //        inputNewElement.id = "input"
//    //
//    //    inputNewElement.setAttribute("placeholder", "new quest")
//    //
//    //    val enterButton = dom.document
//    //      .createElement("button")
//    //    enterButton.textContent = "Enter"
//    //
//    //        div.appendChild(inputNewElement)
//    //    newDiv.appendChild(enterButton)
//    //
//    //    val text = dom.document.getElementById("input").asInstanceOf[html.Input].value
//
//  } //        ShowListTasks.show(todo)
//  //        cycle()
//}