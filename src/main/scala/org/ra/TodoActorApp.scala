//package org.ra
//
//import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
//import com.sksamuel.elastic4s.{ ElasticsearchClientUri, TcpClient }
//import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
//import com.sksamuel.elastic4s.ElasticDsl._
//
//import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.HttpRequest
//import com.sksamuel.elastic4s.{ ElasticsearchClientUri, TcpClient }
//import com.sksamuel.elastic4s.http.HttpExecutable
//import com.sksamuel.elastic4s.indexes.IndexDefinition
//import com.sksamuel.elastic4s.http.HttpClient
//import com.sksamuel.elastic4s.ElasticDsl._
//import com.sksamuel.elastic4s.analyzers.StopAnalyzer
//import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
////import akka.stream.actor.ActorPublisherMessage.Request
////import org.ra.TaskManager.CreateTask
////import org.ra.TodoList.{GetTasks, RespondTasksLisimport com.sksamuel.elastic4s.ElasticDsl._t}
//import com.sksamuel.elastic4s.searches.RichSearchResponse
//import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
//import org.elasticsearch.common.settings.Settings
//import com.sksamuel.elastic4s.circe._
//import io.circe.generic.auto._
//
//import scala.io.StdIn
//
//import com.sksamuel.elastic4s.{ ElasticsearchClientUri, TcpClient }
//import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
//
//
//
//object TodoActorApp extends App {
//    val system: ActorSystem = ActorSystem("todoList")
//
//    try {
//      val supervisor = system.actorOf(ListSupervisor.props(), "list-supervisor")
//
//    } finally {
//      system.terminate()
//    }
//}
//
//case class ListTasks(list: List[Task])
//case class Task(id: String, body: String, state: Boolean)
//trait TaskAction
//case class CreateTask(task: Task) extends TaskAction
//case class DeleteTask(task: Task) extends TaskAction
//case class ChangeStateTask(task: Task) extends TaskAction
//case class DeleteAllCompletedTasks() extends TaskAction
//case class ShowListTasks() extends TaskAction
//case class ViewTask(task: Task) {
//  def show = if (task.state) s"${task.id} - ${task.body} (V)" else s"${task.id} - ${task.body} ( )"
//}
//object TaskId {
//  def getId = {
//    (Math.random() * 100000000).round
//
//  }
//}
//
//
//
//object ListSupervisor {
//  def props(): Props = Props(new ListSupervisor)
//}
//
//class ListSupervisor extends Actor {
//
//  val system: ActorSystem = ActorSystem("todoList")
//
////  override def preStart(): Unit = log.info("To-do Application started")
////  override def postStop(): Unit = log.info("To-do Application stopped")
//
//  val action = system.actorOf(ActionTodo.props(), "actionTodo")
//  action ! "start"
//
////  log.info("ActionTodo started")
//
//  override def receive = Actor.emptyBehavior
//}
//
//object TaskManager {
//  def props() = Props(new TaskManager)
//
//}
//
//class TaskManager extends Actor  {
//
//  import com.sksamuel.elastic4s.ElasticDsl._
//
//  val client = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))
//
//  override def receive = {
//    case CreateTask(task) => {
//      client.execute {
//        bulk(
//          indexInto("todolist1" / "list").fields("id" -> task.id, "body" -> task.body, "status" -> task.state.toString)
//        ).refresh(RefreshPolicy.WAIT_UNTIL)
//      }.await
//
//      val result = client.execute {
//        search("todolist1").matchQuery("id", task.id)
//      }.await
//
//      // prints out the original json
//      println(result)
//      println(result.hits.head.sourceAsString)
//      println(result.hits.head.sourceField("body"))
//    }
//    case DeleteTask(task) => ???
//    case ChangeStateTask(task) => ???
//    case DeleteAllCompletedTasks() => ???
//    case ShowListTasks() => ???
//
//  }
//}
////
//object ActionTodo {
//  def props(): Props = Props(new ActionTodo)
//}
//
//class ActionTodo extends Actor {
//
//  val system: ActorSystem = ActorSystem("todoList")
//
//  val manager = system.actorOf(TaskManager.props(), "TaskManager")
//
//  def cycle(): Unit = while (true) {
//    println("1 - Добавить задачу")
//    println("2 - Изменить статус на выполнена")
//    println("3 - Удалить задачу")
//    println("4 - Удалить все выполненные задачи")
//    println("=" * 100)
//    val input = scala.io.StdIn.readLine()
//    input match {
//      case "1" => {
//        println("Введите задачу")
//        val input = scala.io.StdIn.readLine()
//        val task = Task(TaskId.getId.toString, input, false)
//        manager ! task
//
//      }
//      case "2" => {
//        println("Введите номер задачи")
//        val input = scala.io.StdIn.readLine()
//        val task = Task(input, "", false)
////        Action.processAction(ChangeStateTask(task))
////        Action.processAction(ShowListTasks())
//      }
//      case "3" => {
//        println("Введите номер задачи")
//        val input = scala.io.StdIn.readLine()
//        val task = Task(input, "", false)
////        Action.processAction(DeleteTask(task))
////        Action.processAction(ShowListTasks())
//      }
//
//      case "4" => {
////        Action.processAction(DeleteAllCompletedTasks())
////        Action.processAction(ShowListTasks())
//      }
//      case _ => println("Ошибка!!! Неверный ввод")
//    }
//  }
//
//  override def receive = {
//    case "start" => cycle()
//    case _ => println("Fail")
//  }
//}