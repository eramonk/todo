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

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val taskFormat: RootJsonFormat[Task] = jsonFormat3(Task)
  implicit val taskListFormat: RootJsonFormat[TaskList] = jsonFormat1(TaskList)
}

object Server extends Directives with JsonSupport {
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
            onSuccess(WebAction.processAction(CreateTaskMessage(Task("", s"$task", "opened")))) { x =>
              complete(WebAction.showTasks())
            }
          }
        } ~ path("delete") {
          parameter('id) { id =>
            onSuccess(WebAction.processAction(DeleteTaskMessage(s"$id"))) { x =>
              complete(WebAction.showTasks())
            }
          }
        } ~ path("statuson") {
          parameter('id) { id =>
            onSuccess(WebAction.processAction(ChangeStatusTaskMessage(s"$id"))) { x =>
              complete(WebAction.showTasks())
            }
          }
        } ~ path("statusoff") {
          parameter('id) { id =>
            onSuccess(WebAction.processAction(ChangeStatusTaskOffMessage(s"$id"))) { x =>
              complete(WebAction.showTasks())
            }
          }
        } ~ path("deleteall") {
          onSuccess(WebAction.processAction(DeleteAllCompletedTasksMessage())) { x =>
            complete(WebAction.showTasks())
          }
        } ~ path("show") {
          complete(WebAction.showTasks())

        } ~ path("newlist") {
          onSuccess(WebAction.processAction(DeleteListMessage())) { x =>
            complete(WebAction.showTasks())
          }
        }

      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9001)

    println(s"Server online at http://localhost:9001/index\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
