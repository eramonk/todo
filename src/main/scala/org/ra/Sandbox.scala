package org.ra

import com.sksamuel.elastic4s.{ ElasticsearchClientUri, TcpClient }
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy

/**
 * Created by ra on 17.07.17.
 */

case class Task(id: String, body: String, status: Boolean)
trait TaskAction
case class CreateTask(task: Task) extends TaskAction
case class DeleteTask(id: String) extends TaskAction
case class ChangeStateTask(id: String) extends TaskAction
case class DeleteAllCompletedTasks() extends TaskAction
case class ShowListTasks() extends TaskAction
object TaskId {
  def getId = {
    (Math.random() * 10000).round.toString
  }
}
object ShowList {
  def show(id: String, body: String, status: String) =
    if (status == "true") println(s"${id} - ${body} (X)")
    else println(s"${id} - ${body} ( )")

}

object Action {

  import com.sksamuel.elastic4s.ElasticDsl._

  val client = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  def processAction(task: TaskAction): Unit = task match {

    case CreateTask(task) =>
      client.execute {
        bulk(
          indexInto("todotest1" / "list").fields("id" -> task.id, "body" -> task.body, "status" -> task.status.toString)
        ).refresh(RefreshPolicy.WAIT_UNTIL)
      }.await

    case DeleteTask(id) =>
      client.execute {
        delete(id) from "todotest1" / "list"
      }.await

    case ChangeStateTask(id) =>
      val resp = client.execute {
        update(id).in("todotest1" / "list").doc(
          "status" -> "true"
        )
      }.await

    case DeleteAllCompletedTasks() =>

      val result = client.execute {
        search("todotest1").matchQuery("status", "true")

      }.await

      result.hits.map(x =>
        client.execute {
          delete(x.id) from "todotest1" / "list"
        }.await)
    case ShowListTasks() =>
      val result = client.execute {
        search("todotest1" / "list")
      }.await

      result
        .hits
        .map(x =>
          ShowList.show(
            x.sourceField("id").toString,
            x.sourceField("body").toString,
            x.sourceField("status").toString
          ))
  }

  def frontEnd() = {
    println("=" * 100)
    println("Список задач")
    println("-" * 100)
    Action.processAction(ShowListTasks())
    println("=" * 100)
    println("1 - Добавить задачу")
    println("2 - Изменить статус на выполнена")
    println("3 - Удалить задачу")
    println("4 - Удалить все выполненные задачи")
    println("5 - Показать задачи")
    println("=" * 100)
  }

  def cycle(): Unit = while (true) {
    Thread.sleep(1000)
    Action.frontEnd()

    val input = scala.io.StdIn.readLine()
    input match {
      case "1" => {
        println("Введите задачу")
        val input = scala.io.StdIn.readLine()
        val task = Task(TaskId.getId, input, false)
        Action.processAction(CreateTask(task))

      }
      case "2" => {
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()
        val result = client.execute {
          search("todotest1").matchQuery("id", input)
        }.await
        Action.processAction(ChangeStateTask(result.hits.head.id))

      }
      case "3" => {
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()
        val result = client.execute {
          search("todotest1").matchQuery("id", input)
        }.await

        Action.processAction(DeleteTask(result.hits.head.id))
      }

      case "4" => {
        Action.processAction(DeleteAllCompletedTasks())
      }

      case "5" => {
        Action.processAction(ShowListTasks())
      }

      case _ => println("Ошибка!!! Неверный ввод")
    }
  }
}
object TodoApp extends App {
  Action.cycle()
}

