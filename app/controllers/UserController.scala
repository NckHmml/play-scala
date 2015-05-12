package controllers

import java.text.SimpleDateFormat

import play.api._
import play.api.mvc._
import play.api.libs.json._
import scalikejdbc._
import models._

object UserController extends Controller {
  implicit val session = AutoSession
  val secret = Play.current.configuration.getString("application.secret").get

  def events(from: Option[String], limit: Int, offset: Int) = Action { request =>
    if (limit <= 0) {
      BadRequest
    } else if (!from.nonEmpty) {
      BadRequest
    } else {
      val sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
      val events = sql"SELECT e.*, u.name AS company FROM events AS e JOIN users AS u ON u.id = e.user_id WHERE start_date >= ${from} ORDER BY start_date LIMIT ${limit} OFFSET ${offset}"
        .map(rs => UserEvent(rs)).list().apply

      Ok {
        Json.obj(
          "code" -> 200,
          "events" -> events.map(event => {
            Json.obj(
              "id" -> event.id,
              "name" -> event.name,
              "start_date" -> sdf.format(event.startDate),
              "company" -> Json.obj(
                "id" -> event.userId,
                "name" -> event.company
              )
            )
          })
        )
      }
    }
  }
}
