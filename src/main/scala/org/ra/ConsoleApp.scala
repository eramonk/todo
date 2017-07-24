package org.ra

import com.sksamuel.elastic4s.searches.RichSearchResponse
import com.sksamuel.elastic4s.{ ElasticClient, ElasticsearchClientUri, TcpClient }
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy

case class Task(id: String, body: String, status: Boolean)
case class TaskList(tasks: List[Task])
trait TaskAction
case class CreateTaskMessage(task: Task) extends TaskAction
case class DeleteTaskMessage(id: String) extends TaskAction
case class ChangeStatusTaskMessage(id: String) extends TaskAction
case class ChangeStatusTaskOffMessage(id: String) extends TaskAction
case class DeleteAllCompletedTasksMessage() extends TaskAction
case class ShowListConsoleMessage() extends TaskAction
case class DeleteListMessage() extends TaskAction
object TaskId {
  def getId: String = {
    (Math.random() * 10000).round.toString
  }
}

//noinspection ScalaDeprecation
object Action {

  import com.sksamuel.elastic4s.ElasticDsl._

  val client: ElasticClient = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  def showConsole(id: String, body: String, status: String): Unit =
    if (status == "true") println(s"$id - $body (X)")
    else println(s"$id - $body ( )")

  def processAction(task: TaskAction): Any = task match {

    case CreateTaskMessage(task) =>
      client.execute {
        bulk(
          indexInto("todotest2" / "list").fields("id" -> TaskId.getId, "body" -> task.body, "status" -> task.status.toString)
        ).refresh(RefreshPolicy.WAIT_UNTIL)
      }.await

    case DeleteTaskMessage(id) =>
      val result = client.execute {
        search("todotest2").matchQuery("id", id)
      }.await

      client.execute {
        delete(result.hits.head.id) from "todotest2" / "list"
      }.await

    case ChangeStatusTaskMessage(id) =>
      val result = client.execute {
        search("todotest2").matchQuery("id", id)
      }.await

      client.execute {
        update(result.hits.head.id).in("todotest2" / "list").doc(
          "status" -> "true"
        )
      }.await

    case ChangeStatusTaskOffMessage(id) =>
      val result = client.execute {
        search("todotest2").matchQuery("id", id)
      }.await

      client.execute {
        update(result.hits.head.id).in("todotest2" / "list").doc(
          "status" -> "false"
        )
      }.await

    case DeleteAllCompletedTasksMessage() =>
      val result = client.execute {
        search("todotest2").matchQuery("status", "true")
      }.await

      result.hits.map(x =>
        client.execute {
          delete(x.id) from "todotest2" / "list"
        }.await)

    case DeleteListMessage() =>
      val result = client.execute {
        search("todotest2")
      }.await

      result.hits.map(x =>
        client.execute {
          delete(x.id) from "todotest2" / "list"
        }.await)

    case ShowListConsoleMessage() =>
      val result: RichSearchResponse = client.execute {
        search("todotest2" / "list")
      }.await

      result
        .hits
        .foreach(x =>
          showConsole(
            x.sourceField("id").toString,
            x.sourceField("body").toString,
            x.sourceField("status").toString
          ))
  }

  def consoleMenu(): Unit = {
    println("=" * 100)
    println("Список задач")
    println("-" * 100)
    Action.processAction(ShowListConsoleMessage())
    println("=" * 100)
    println("1 - Добавить задачу")
    println("2 - Изменить статус на выполнена")
    println("3 - Удалить задачу")
    println("4 - Удалить все выполненные задачи")
    println("5 - Показать задачи")
    println("=" * 100)
  }

  def cycle(): Unit = while (true) {
    Thread.sleep(600)
    Action.consoleMenu()

    val input = scala.io.StdIn.readLine()
    input match {
      case "1" =>
        println("Введите задачу")
        val input = scala.io.StdIn.readLine()
        val task = Task(TaskId.getId, input, status = false)
        Action.processAction(CreateTaskMessage(task))
      case "2" =>
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()

        Action.processAction(ChangeStatusTaskMessage(input))
      case "3" =>
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()

        Action.processAction(DeleteTaskMessage(input))

      case "4" =>
        Action.processAction(DeleteAllCompletedTasksMessage())

      case "5" =>
        Action.processAction(ShowListConsoleMessage())

      case _ => println("Ошибка!!! Неверный ввод")
    }
  }
}

object ConsoleApp extends App {
  Action.cycle()
}

