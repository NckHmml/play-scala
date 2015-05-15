package models

import scalikejdbc._
import java.sql._

class UserEvent (val id: Int, val userId: Int, val name: String, val startDate: Date, val company: String)
class CompanyEvent(val id: Int, val name: String, val startDate: Date, val attendees: Int)

object UserEvent extends SQLSyntaxSupport[UserEvent] {
  def apply(rs: WrappedResultSet): UserEvent =
    new UserEvent(rs.int("id"), rs.int("user_id"), rs.string("name"), rs.date("start_date"), rs.string("company"))
}

object CompanyEvent extends SQLSyntaxSupport[CompanyEvent] {
  def apply(rs: WrappedResultSet): CompanyEvent =
    new CompanyEvent(rs.int("id"), rs.string("name"), rs.date("start_date"), rs.int("number_of_attendees"))
}
