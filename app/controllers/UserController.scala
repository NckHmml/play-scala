package controllers

import java.text.SimpleDateFormat

import play.api._
import play.api.mvc._
import play.api.libs.json._
import scalikejdbc._
import io.really.jwt._
import models._

object UserController extends Controller {
  implicit val session = AutoSession
  val secret = Play.current.configuration.getString("application.secret")

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

  def reserve = Action { request =>
    val body = request.body.asFormUrlEncoded
    def saveGet(key: String, default: String = ""): String = {
      if (body.exists(item => item.keys.exists(k => k == key)))
        body.get(key).head
      else
        default
    }

    val rawtoken = JWT.decode(saveGet("token"), secret)
    val reserve = saveGet("reserve", "false").toBoolean
    val eventId = saveGet("event_id", "0").toInt
    var id: Int = 0

    if (rawtoken.isInstanceOf[JWTResult.JWT]) {
      val token = rawtoken.asInstanceOf[JWTResult.JWT].payload
      id = (token \ "id").as[Int]
    }

    if (id != 0) {
      val user = sql"SELECT * FROM users WHERE id = ${id}"
        .map(rs => User(rs)).single().apply.orNull

      if (user == null) {
        Ok {
          Json.obj(
            "code" -> 500,
            "message" -> "No user found"
          )
        }
      } else if (user.groupId == 2) {
        Ok {
          Json.obj(
            "code" -> 401,
            "message" -> "Cannot reserve"
          )
        }
      } else {
        val num: Int = sql"SELECT COUNT(*) AS num FROM attends WHERE user_id = ${id} AND event_id = ${eventId}"
          .map(rs => rs.int("num")).first().apply().get
        if (num > 0 && reserve) {
          Ok {
            Json.obj(
              "code" -> 501,
              "message" -> "already reserved"
            )
          }
        } else if (num == 0 && !reserve) {
          Ok {
            Json.obj(
              "code" -> 502,
              "message" -> "not reserved"
            )
          }
        } else {
          if (reserve) {
            sql"INSERT INTO attends (user_id, event_id) VALUES (${id}, ${eventId})".update.apply()
          } else {
            sql"DELETE FROM attends WHERE user_id = ${id} AND event_id = ${eventId}".update.apply()
          }
          Ok {
            Json.obj(
              "code" -> 200
            )
          }
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
