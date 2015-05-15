package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import scalikejdbc._
import io.really.jwt._
import models._

object CompanyController extends Controller {
  implicit val session = AutoSession
  val secret = Play.current.configuration.getString("application.secret")

  def events = Action { request =>
    val body = request.body.asFormUrlEncoded
    def saveGet(key: String, default: String = ""): String = {
      if (body.exists(item => item.keys.exists(k => k == key)))
        body.get(key).head
      else
        default
    }

    val rawtoken = JWT.decode(saveGet("token"), secret)
    val from = saveGet("from")
    val offset = saveGet("offset", "0").toInt
    val limit = saveGet("limit", "2147483647").toInt
    var id: Int = 0

    if (rawtoken.isInstanceOf[JWTResult.JWT]) {
      val token = rawtoken.asInstanceOf[JWTResult.JWT].payload
      id = (token \ "id").as[Int]
    }

    if (from == "" || limit == 0) {
      BadRequest
    } else if (id != 0) {
      val user = sql"SELECT * FROM users WHERE id = ${id}"
        .map(rs => User(rs)).single().apply.orNull

      if (user == null) {
        Ok {
          Json.obj(
            "code" -> 500,
            "message" -> "No user found"
          )
        }
      } else if (user.groupId == 1) {
        Ok {
          Json.obj(
            "code" -> 401,
            "message" -> "Cannot reserve"
          )
        }
      } else {
        val events = sql"SELECT * FROM (SELECT events.*, COUNT(attends.user_id) AS number_of_attendees FROM events LEFT JOIN attends ON events.id = attends.event_id WHERE events.user_id = ${id} GROUP BY events.id) as events WHERE start_date >= ${from} ORDER BY start_date LIMIT ${limit} OFFSET ${offset}"
          .map(rs => CompanyEvent(rs)).list().apply
        Ok {
          Json.obj(
            "code" -> 200,
            "events" -> events.map(event => Json.obj(
              "id" -> event.id,
              "name" -> event.name,
              "start_date" -> event.startDate,
              "number_of_attendees" -> event.attendees
            ))
          )
        }
      }
    } else {
      Ok {
        Json.obj(
          "code" -> 401,
          "message" -> "Invalid token"
        )
      }
    }
  }
}
