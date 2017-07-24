package org.ra

import com.sksamuel.elastic4s.searches.RichSearchResponse
import com.sksamuel.elastic4s.{ ElasticClient, ElasticsearchClientUri, TcpClient }
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy

case class Task(id: String, body: String, status: Boolean)
case class TaskList(tasks: List[Task])
trait TaskAction
case class CreateTask(task: Task) extends TaskAction
case class DeleteTask(id: String) extends TaskAction
case class ChangeStateTask(id: String) extends TaskAction
case class ChangeStateTaskOff(id: String) extends TaskAction
case class DeleteAllCompletedTasks() extends TaskAction
case class ShowListTasks() extends TaskAction
case class ShowListFront() extends TaskAction
case class DeleteList() extends TaskAction
object TaskId {
  def getId: String = {
    (Math.random() * 10000).round.toString
  }
}
object ShowList {
  def show(id: String, body: String, status: String): Unit =
    if (status == "true") println(s"$id - $body (X)")
    else println(s"$id - $body ( )")

  def showFront(args: RichSearchResponse): TaskList = {
    TaskList({
      for {
        x <- args.hits
      } yield Task(
        x.sourceField("id").toString,
        x.sourceField("body").toString,
        x.sourceField("status").toString.toBoolean
      )
    }.toList)

  }

}

//noinspection ScalaDeprecation
object Action {

  import com.sksamuel.elastic4s.ElasticDsl._

  val client: ElasticClient = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  def showTasks(): TaskList = {

    val result = client.execute {
      search("todotest2" / "list")
    }.await

    ShowList.showFront(result)

  }

  def processAction(task: TaskAction): Any = task match {

    case CreateTask(task) =>
      client.execute {
        bulk(
          indexInto("todotest2" / "list").fields("id" -> TaskId.getId, "body" -> task.body, "status" -> task.status.toString)
        ).refresh(RefreshPolicy.WAIT_UNTIL)
      }.await

    case DeleteTask(id) =>

      val result = client.execute {
        search("todotest2").matchQuery("id", id)

      }.await

      client.execute {
        delete(result.hits.head.id) from "todotest2" / "list"
      }.await

    case ChangeStateTask(id) =>

      val result = client.execute {
        search("todotest2").matchQuery("id", id)

      }.await

      client.execute {
        update(result.hits.head.id).in("todotest2" / "list").doc(
          "status" -> "true"
        )
      }.await

    case ChangeStateTaskOff(id) =>

      val result = client.execute {
        search("todotest2").matchQuery("id", id)

      }.await

      client.execute {
        update(result.hits.head.id).in("todotest2" / "list").doc(
          "status" -> "false"
        )
      }.await

    case DeleteAllCompletedTasks() =>

      val result = client.execute {
        search("todotest2").matchQuery("status", "true")

      }.await

      result.hits.map(x =>
        client.execute {
          delete(x.id) from "todotest2" / "list"
        }.await)

    case DeleteList() =>

      val result = client.execute {
        search("todotest2")

      }.await

      result.hits.map(x =>
        client.execute {
          delete(x.id) from "todotest2" / "list"
        }.await)

    case ShowListTasks() =>
      val result: RichSearchResponse = client.execute {
        search("todotest2" / "list")
      }.await

      result
        .hits
        .foreach(x =>
          ShowList.show(
            x.sourceField("id").toString,
            x.sourceField("body").toString,
            x.sourceField("status").toString
          ))

    case ShowListFront() =>
      val result = client.execute {
        search("todotest2" / "list")
      }.await

      val b: TaskList = ShowList.showFront(result)
      b

    //      result
    //        .hits
    //        .map(x =>
    //          ShowList.show(
    //            x.sourceField("id").toString,
    //            x.sourceField("body").toString,
    //            x.sourceField("status").toString
    //          ))

  }

  def frontEnd(): Unit = {
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
    Thread.sleep(600)
    Action.frontEnd()

    val input = scala.io.StdIn.readLine()
    input match {
      case "1" =>
        println("Введите задачу")
        val input = scala.io.StdIn.readLine()
        val task = Task(TaskId.getId, input, false)
        Action.processAction(CreateTask(task))
      case "2" =>
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()

        Action.processAction(ChangeStateTask(input))
      case "3" =>
        println("Введите номер задачи")
        val input = scala.io.StdIn.readLine()
        val result = client.execute {
          search("todotest2").matchQuery("id", input)
        }.await

        Action.processAction(DeleteTask(result.hits.head.id))

      case "4" =>
        Action.processAction(DeleteAllCompletedTasks())

      case "5" =>
        Action.processAction(ShowListTasks())

      case _ => println("Ошибка!!! Неверный ввод")
    }
  }
}

object ConsolApp extends App {
  Action.cycle()
}

