package models

import scalikejdbc._
import java.sql._

class UserEvent (val id: Int, val userId: Int, val name: String, val startDate: Date, val company: String)

object UserEvent extends SQLSyntaxSupport[UserEvent] {
  override val tableName = "Users"
  def apply(rs: WrappedResultSet): UserEvent =
    new UserEvent(rs.int("id"), rs.int("user_id"), rs.string("name"), rs.date("start_date"), rs.string("company"))
}
