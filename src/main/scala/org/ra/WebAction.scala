package org.ra

import com.sksamuel.elastic4s.{ ElasticClient, ElasticsearchClientUri, TcpClient }
import com.sksamuel.elastic4s.searches.RichSearchResponse
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import scala.collection.mutable.ArrayOps
import scala.concurrent.Future

case class Task(id: String, body: String, status: String)
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
  def getId: String = java.util.UUID.randomUUID.toString
}

object WebAction {

  import com.sksamuel.elastic4s.ElasticDsl._
  import scala.concurrent.ExecutionContext.Implicits.global

  val client: ElasticClient = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  def showTasks(): TaskList = {

    val result = client.execute {
      search("todotest2" / "list")
    }.await

    TaskList({
      for {
        x <- result.hits
      } yield Task(
        x.sourceField("id").toString,
        x.sourceField("body").toString,
        x.sourceField("status").toString
      )
    }.toList)
  }

  def processAction(task: TaskAction): Future[Object] = task match {

    case CreateTaskMessage(task) =>
      client.execute {
        bulk(
          indexInto("todotest2" / "list").fields("id" -> TaskId.getId, "body" -> task.body, "status" -> task.status)
        ).refresh(RefreshPolicy.WAIT_UNTIL)
      }

    case DeleteTaskMessage(id) =>

      client.execute {
        search("todotest2").matchQuery("id", id)
      }.flatMap { result =>
        client.execute {
          delete(result.hits.head.id) from "todotest2" / "list" refresh RefreshPolicy.WAIT_UNTIL
        }
      }

    case ChangeStatusTaskMessage(id) =>

      client.execute {
        search("todotest2").matchQuery("id", id)
      }.flatMap { result =>
        client.execute {
          update(result.hits.head.id).in("todotest2" / "list")
            .doc("status" -> "closed") refresh RefreshPolicy.WAIT_UNTIL
        }
      }

    case ChangeStatusTaskOffMessage(id) =>

      client.execute {
        search("todotest2").matchQuery("id", id)
      }.flatMap { result =>
        client.execute {
          update(result.hits.head.id).in("todotest2" / "list")
            .doc("status" -> "opened") refresh RefreshPolicy.WAIT_UNTIL
        }
      }

    case DeleteAllCompletedTasksMessage() =>

      client.execute {
        search("todotest2").matchQuery("status", "closed")
      }.map(y => y.hits.map(z =>
        client.execute {
          delete(z.id) from "todotest2" / "list" refresh RefreshPolicy.WAIT_UNTIL
        }))
        .flatMap(x => Future.sequence(x.toList))

    case DeleteListMessage() =>

      client.execute {
        search("todotest2")
      }.map(y => y.hits.map(z =>
        client.execute {
          delete(z.id) from "todotest2" / "list" refresh RefreshPolicy.WAIT_UNTIL
        }))
        .flatMap(x => Future.sequence(x.toList))
  }
}
