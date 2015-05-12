package models

import scalikejdbc._

class User(val id: Int, val name: String, val email: String, val password: String, val groupId: Int)

object User extends SQLSyntaxSupport[User] {
  override val tableName = "Users"
  def apply(rs: WrappedResultSet): User =
    new User(rs.int("id"), rs.string("name"), rs.string("email"), rs.string("password"), rs.int("group_id"))
}
