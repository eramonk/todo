package org.ra

import com.sksamuel.elastic4s.{ ElasticClient, ElasticsearchClientUri, TcpClient }
import com.sksamuel.elastic4s.searches.RichSearchResponse
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy
import scala.collection.mutable.ArrayOps
import scala.concurrent.Future

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
        x.sourceField("status").toString.toBoolean
      )
    }.toList)
  }


  def processAction(task: TaskAction) = task match {

    case CreateTaskMessage(task) =>
      client.execute {
        bulk(
          indexInto("todotest2" / "list").fields("id" -> TaskId.getId, "body" -> task.body, "status" -> task.status.toString)
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
            .doc("status" -> "true") refresh RefreshPolicy.WAIT_UNTIL
        }
      }

    case ChangeStatusTaskOffMessage(id) =>

      client.execute {
        search("todotest2").matchQuery("id", id)
      }.flatMap { result =>
        client.execute {
          update(result.hits.head.id).in("todotest2" / "list")
            .doc("status" -> "false") refresh RefreshPolicy.WAIT_UNTIL
        }
      }

    case DeleteAllCompletedTasksMessage() =>

      client.execute {
        search("todotest2").matchQuery("status", "true")
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

    case ShowListBrowserMessage() =>
      val result = client.execute {
        search("todotest2" / "list")
      }.await

      Future(showBrowser(result))

  }
}