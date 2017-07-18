package org.ra

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
//import akka.stream.actor.ActorPublisherMessage.Request
//import org.ra.TaskManager.CreateTask
//import org.ra.TodoList.{GetTasks, RespondTasksList}

import scala.io.StdIn

/**
 * Created by ra on 17.07.17.
 */
case class ListTasks(list: List[Task])
case class Task(id: String, body: String, state: Boolean)
trait TaskAction
case class CreateTask(task: Task) extends TaskAction
case class DeleteTask(task: Task) extends TaskAction
case class ChangeStateTask(task: Task) extends TaskAction
case class DeleteAllCompletedTasks() extends TaskAction
case class ShowListTasks() extends TaskAction
case object TaskId {
  var count = 0
  def getId = {
    count += 1
    count.toString
  }
}

case class ViewTask(task: Task) {
  def show = if (task.state) s"${task.id} ${task.body} V" else s"${task.id} ${task.body} X"
}

object Action {

  var listT: ListTasks = new ListTasks(List())

  def processAction(task: TaskAction): Unit = task match {
    case CreateTask(task) => listT = ListTasks(task :: listT.list);
    case DeleteTask(task) => listT = ListTasks(listT.list.filter(x => x.id != task.id))
    case ChangeStateTask(task) => listT = ListTasks(listT.list.map(x => if (x.id == task.id) Task(x.id, x.body, true) else x))
    case DeleteAllCompletedTasks() => listT = ListTasks(listT.list.filter(x => !x.state))
    case ShowListTasks() => show(listT)

  }

  def show(listT: ListTasks) = {
    println("-" * 100)
    println("Список задач")
    println("-" * 100)
    listT.list.foreach(x => println(ViewTask(x).show))
    println("-" * 100)
    listT

  }


  def cycle(): Unit = while (true) {
    println("1 - Добавить задачу")
    println("2 - Изменить статус на выполнена")
    println("3 - Удалить задачу")
    println("4 - Удалить все выполненные задачи")
    val input = scala.io.StdIn.readLine()
    input match {
      case "1" => {
        println("Введите задачу")
        val input = scala.io.StdIn.readLine()
        val task = Task(TaskId.getId, input, false)
        Action.processAction( CreateTask(task))
        Action.processAction( ShowListTasks())
//        ShowListTasks.show(todo)
//        count += 1
//        cycle()
      }
      case "2" => {
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()
        val task = Task(input, "", false)
        Action.processAction(new ChangeStateTask(task))
        Action.processAction(new ShowListTasks)
//        ShowListTasks.show(todo)
//        cycle()
      }
      case "3" => {
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()
        val task = Task(input, "", false)
        Action.processAction(new DeleteTask(task))
        Action.processAction(new ShowListTasks)
//        ShowListTasks.show(todo)
//        cycle()
      }

      case "4" => {
        Action.processAction(new DeleteAllCompletedTasks)
        Action.processAction(new ShowListTasks)
//        ShowListTasks.show(todo)
//        cycle()
      }
    }
  }
}

//object ShowListTasks {
//  def show(listT: ListTasks) = {
//    println("-" * 100)
//    println("Список задач")
//    println("-" * 100)
//    listT.list
//      .map(x => if (x.state) println(x.id + " " + x.body + " V"); else println(x.id + " " + x.body + " X"))
//    println("-" * 100)
//  }
//}




object TodoApp extends App {
  //  val input = scala.io.StdIn.readLine()

  Action.cycle()



  //  println("Type something : ")
  //  val input = scala.io.StdIn.readLine()
  //  println("Did you type this ? " + input)

  //
  //  try {
  //    val supervisor = system.actorOf(ListSupervisor.props(), "list-supervisor")
  //
  //    StdIn.readLine()
  //  } finally {
  //    system.terminate()
  //  }
}
//
//case class Task(body: String, status: Boolean)
//
//object ListSupervisor {
//  def props(): Props = Props(new ListSupervisor)
//}
//
//class ListSupervisor extends Actor with ActorLogging {
//  override def preStart(): Unit = log.info("To-do Application started")
//  override def postStop(): Unit = log.info("To-do Application stopped")
//
//  override def receive = Actor.emptyBehavior
//}
//
//object TodoList {
//  def props() = Props(new TodoList)
//
//  case class RespondTasksList(requestId: String, value: String)
//
//}
//
//class TodoList extends Actor with ActorLogging {
//
//  override def preStart(): Unit = log.info("New List started")
//  override def postStop(): Unit = log.info("List stopped")
//
//  override def receive = {
//    case RespondTasksList(id, v) => ???
//    case ("create", body) => ???
//    case ("delete", id) => ???
//    case ("change", id) => ???
//
//  }
//
//}
//
//object TaskManager  {
//  def props() = Props(new TaskManager)
//
//  case class CreateTask(requestId: String, task: Task)
//  case class DeleteTask(requestId: String, task: Task)
//  case class ChangeStateTask(requestId: String, task: Task)
//
//}
//
//class TaskManager extends Actor with ActorLogging {
//  import TaskManager._
//  override def receive = {
//    case CreateTask(id, task) => ???;
//    case DeleteTask(id, task) => ???;
//    case ChangeStateTask(id, task) => ???
//  }
//}
