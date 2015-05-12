package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import scalikejdbc._
import io.really.jwt._
import models._

object AuthController extends Controller {
  implicit val session = AutoSession
  val secret = Play.current.configuration.getString("application.secret").get

  def login = Action { request =>
    val body: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
    val email = body("email").head
    val password = body("password").head
    val user = sql"SELECT * FROM users WHERE email = ${email} AND password = sha1(${password})"
      .map(rs => User(rs)).single().apply.orNull

    if (user != null) {
      val jwt = JWT.encode(secret, Json.obj("id" -> user.id))
      Ok(Json.obj(
        "code" -> 200,
        "token" -> jwt,
        "user" -> Json.obj(
          "id" -> user.id,
          "name" -> user.name,
          "group_id" -> user.groupId
        )
      ))
    } else {
      Ok(Json.obj(
        "code" -> 500
      ))
    }
  }
}
