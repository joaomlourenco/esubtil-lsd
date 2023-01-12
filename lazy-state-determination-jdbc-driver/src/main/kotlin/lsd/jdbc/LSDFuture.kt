package lsd.jdbc
class LSDFuture(id: String, var query: String) {
    val id = "{${id}}"
    internal var result: Any = "!!!UNRESOLVED LSD FUTURE!!!"

    internal fun resolve(conn: LSDConnection) {
        // create prepared statement
        // TODO this only works for queries that return a single cell (i.e. point queries)
        val stmt = conn.dbConn.prepareStatement(query)
        val rs = stmt.executeQuery()
        rs.next()
        result = rs.getObject(1) // First column - index starts at 1
        rs.close()
        stmt.close()
    }
}