package org.ra.routes

//import akka.http.scaladsl.Http
//import org.ra.build.BuildInfo
//import akka.http.scaladsl.server.directives.MethodDirectives.get
//import akka.http.scaladsl.server.directives.PathDirectives.path
//import akka.http.scaladsl.server.directives.RouteDirectives.complete
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
//import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse }
//import akka.stream.scaladsl.FileIO
//import org.ra.WebServerHttpApp.{ getFromFile }
//import spray.json._
//
//import scala.reflect.io.File
//
//trait VersionRoutes {
//
//  lazy val versionRoutes =
//    path("version") {
//      get {
//        complete(BuildInfo.toJson.parseJson)
//      }
//    }
//  lazy val test =
//    path("test") {
//      get {
//        complete(HttpResponse(entity = HttpEntity(
//          ContentTypes.`text/html(UTF-8)`,
//          "<html><body>Hello world!<input></input></body></html>"
//        )))
//      }
//    }
//
//  lazy val route1 =
//    path("index") {
//      get {
//        getFromFile("index.html")
//      } // uses implicit ContentTypeResolver
//    }
//}
