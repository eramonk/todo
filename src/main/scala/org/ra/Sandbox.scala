package org.ra

import com.sksamuel.elastic4s.searches.RichSearchResponse
import com.sksamuel.elastic4s.{ ElasticClient, ElasticsearchClientUri, TcpClient }
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy

//noinspection ScalaDeprecation
object Sandbox {

  import com.sksamuel.elastic4s.ElasticDsl._

  val client: ElasticClient = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  def showConsole(id: String, body: String, status: String): Unit =
    if (status == "closed") println(s"$id - $body (X)")
    else println(s"$id - $body ( )")

  def processAction(task: TaskAction): Any = task match {

    case CreateTaskMessage(task) =>
      client.execute {
        bulk(
          indexInto("todotest2" / "list").fields("id" -> TaskId.getId, "body" -> task.body, "status" -> task.status)
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
          "status" -> "closed"
        )
      }.await

    case ChangeStatusTaskOffMessage(id) =>
      val result = client.execute {
        search("todotest2").matchQuery("id", id)
      }.await

      client.execute {
        update(result.hits.head.id).in("todotest2" / "list").doc(
          "status" -> "opened"
        )
      }.await

    case DeleteAllCompletedTasksMessage() =>
      val result = client.execute {
        search("todotest2").matchQuery("status", "closed")
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
    Sandbox.processAction(ShowListConsoleMessage())
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
    Sandbox.consoleMenu()

    val input = scala.io.StdIn.readLine()
    input match {
      case "1" =>
        println("Введите задачу")
        val input = scala.io.StdIn.readLine()
        val task = Task(TaskId.getId, input, status = "opened")
        Sandbox.processAction(CreateTaskMessage(task))
      case "2" =>
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()

        Sandbox.processAction(ChangeStatusTaskMessage(input))
      case "3" =>
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()

        Sandbox.processAction(DeleteTaskMessage(input))

      case "4" =>
        Sandbox.processAction(DeleteAllCompletedTasksMessage())

      case "5" =>
        Sandbox.processAction(ShowListConsoleMessage())

      case _ => println("Ошибка!!! Неверный ввод")
    }
  }
}

//object ConsoleApp extends App {
//  Sandbox.cycle()
//}

