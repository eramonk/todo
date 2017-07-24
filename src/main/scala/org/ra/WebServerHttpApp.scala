package org.ra

//import akka.http.scaladsl.server.{ HttpApp, Route }
//import org.ra.routes.VersionRoutes
///**
// * Server will be started calling `WebServerHttpApp.startServer("localhost", 8080)`
// * and it will be shutdown after pressing return.
// */
//object WebServerHttpApp extends HttpApp with App with VersionRoutes {
//  // Routes that this WebServer must handle are defined here
//  // Please note this method was named `route` in versions prior to 10.0.7
//  def routes: Route =
//    pathEndOrSingleSlash { // Listens to the top `/`
//      complete("Server up and running") // Completes with some text
//    } ~ versionRoutes ~ test ~ route1
//
//  // This will start the server until the return key is pressed
//  startServer("0.0.0.0", 9000)
//
//  //  val route =
//  //    path("index" / Segment) { name =>
//  //      {
//  //        println(name)
//  //        getFromFile(s"$name.html")
//  //      } // uses implicit ContentTypeResolver
//  //    }
//
//  //  import akka.actor.ActorSystem
//  //  import akka.http.scaladsl.server.Directives._
//  //  import akka.stream.ActorMaterializer
//  //  import akka.http.scaladsl.model.HttpRequest
//  //
//  //  implicit val system = ActorSystem()
//  //  implicit val materializer = ActorMaterializer()
//  //  // needed for the future flatMap/onComplete in the end
//  //  implicit val executionContext = system.dispatcher
//  //
//  //  val route =
//  //    (get & path("lines")) {
//  //      withoutSizeLimit {
//  //        extractRequest { r: HttpRequest =>
//  //          val finishedWriting = r.discardEntityBytes().future
//  //
//  //          // we only want to respond once the incoming data has been handled:
//  //          onComplete(finishedWriting) { done =>
//  //            complete("Drained all data from connection..")
//  //          }
//  //        }
//  //      }
//  //    }
//}

