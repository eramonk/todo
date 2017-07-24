package org.ra

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn
import akka.http.scaladsl.server.directives._
import ContentTypeResolver.Default
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

final case class Item(name: String, id: Long)

final case class Order(items: List[Item])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val taskFormat: RootJsonFormat[Task] = jsonFormat3(Task)
  implicit val taskListFormat: RootJsonFormat[TaskList] = jsonFormat1(TaskList)
}

object BrowserApp extends Directives with JsonSupport {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      get {
        pathSingleSlash {
          getFromFile("index.html")
        } ~ path("create") {
          parameter('task) { task =>
            println("!!!!!", task)
            Action.processAction(CreateTask(Task("", s"$task", false)))
            complete(Action.showTasks())
          }
        } ~ path("delete") {
          parameter('id) { id =>
            println("id", id)
            Action.processAction(DeleteTask(s"$id"))
            Thread.sleep(1000)
            complete(Action.showTasks())
          }
        } ~ path("statuson") {
          parameter('id) { id =>
            println("id", id)
            Action.processAction(ChangeStateTask(s"$id"))
            Thread.sleep(1000)
            complete(Action.showTasks())
          }
        } ~ path("statusoff") {
          parameter('id) { id =>
            println("id", id)
            Action.processAction(ChangeStateTaskOff(s"$id"))
            Thread.sleep(1000)
            complete(Action.showTasks())
          }
        } ~ path("deleteall") {
          parameter('id) { id =>
            println("id", id)
            Action.processAction(DeleteAllCompletedTasks())
            Thread.sleep(1000)
            complete(Action.showTasks())
          }
        } ~ path("show") {
          parameter('id) { id =>
            println("id", id)

            complete(Action.showTasks())
          }
        } ~ path("newlist") {
          parameter('id) { id =>
            println("id", id)
            Action.processAction(DeleteList())
            Thread.sleep(1000)
            complete(Action.showTasks())
          }
        }

      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)

    println(s"Server online at http://localhost:9000/index\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
