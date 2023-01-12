package lsd.jdbc

import lsd.LSDPreparedStatementAPI
import lsd.core.parser.operations.StandardSQL
import lsd.core.parser.operations.lsd.*
import lsd.util.LSDException
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*

internal class LSDPreparedStatement(
    conn: LSDConnection,
    sql: String?,
    resultSetType: Int,
    resultSetConcurrency: Int,
    resultSetHoldability: Int
) :
    LSDStatement(conn, resultSetType, resultSetConcurrency, resultSetHoldability), LSDPreparedStatementAPI {

    private var activeOp = conn.parser.parse(sql)
    private val underlyingStatement = conn.dbConn
        .prepareStatement(activeOp.query, resultSetType, resultSetConcurrency, resultSetHoldability)
    private var activeResultSet = underlyingStatement.resultSet

    /**
     * To be used by users who wish to execute queries that return result sets (i.e. SELECT)
     *
     * If another result set exists for this statement, it will be closed
     * (even if this statement fails).
     *
     * @return the result set
     */
    override fun executeQuery(): ResultSet {
        check(!closed) { throw LSDException("Statement is closed.") }

        return when (val op = activeOp) {
            is SelectLSD -> {
                op.prepareForLSDExecution()
                val fut = LSDFuture(op.future, op.activeQuery)
                this.conn.readMap[op.future] = fut
                this.conn.operations.add(op)
                activeOp = op.reset()
                LSDResultSet(fut)
            }
            is StandardSQL -> {
                activeResultSet = underlyingStatement.executeQuery()
                activeResultSet
            }
            else ->
                throw LSDException("'${activeOp.javaClass}' does not return a result set, this is an incorrect usage of the API.")
        }
    }

    /**
     * To be used by users who wish to execute queries that do not return result sets (i.e. UPDATE, CREATE, etc)
     *
     * If another result set exists for this statement, it will be closed
     * (even if this statement fails).
     *
     * If auto commit is on, this statement will be committed.
     * If the statement is a DDL statement (create, drop, alter) and does not
     * throw an exception, the current transaction (if any) is committed after
     * executing the statement.
     *
     * @return the update count (number of affected rows by a DML statement or
     *         other statement able to return number of rows, or 0 if no rows
     *         were affected or the statement returned nothing, or
     *         {@link #SUCCESS_NO_INFO} if number of rows is too large for the
     *         {@code int} data type)
     * @throws LSDException if a database error occurred or a
     *         select statement was executed
     * @see #executeLargeUpdate(String)
     */
    override fun executeUpdate(): Int {
        check(!closed) { throw LSDException("Statement is closed.") }
        return when (val op = activeOp) {
            is IsTrue -> {
                op.prepareForLSDExecution()
                conn.operations.add(op)
                activeOp = op.reset()
                Statement.SUCCESS_NO_INFO
            }
            is UpdateLSD -> {
                (activeOp as LSDOperation).prepareForLSDExecution()
                conn.operations.add(op)
                activeOp = op.reset()
                Statement.SUCCESS_NO_INFO
            }
            is InsertLSD, is IFLSD -> {
                (op as LSDOperation).prepareForLSDExecution()
                conn.operations.add(op)
                activeOp = op.reset()
                Statement.SUCCESS_NO_INFO
            }
            else -> underlyingStatement.executeUpdate()
        }
    }

    /**
     * To be used by the driver internally. Use other execute functions,
     * as this only returns if there is a result set or not
     *
     * If another result set exists for this statement, it will be closed
     * (even if this statement fails).
     *
     * If auto commit is on, this statement will be committed.
     * If the statement is a DDL statement (create, drop, alter) and does not
     * throw an exception, the current transaction (if any) is committed after
     * executing the statement.
     *
     * @return boolean value that represents the existence of a result set produced by the query
     * @throws LSDException if a database error occurred or a
     *         select statement was executed
     * @see #executeLargeUpdate(String)
     */
    override fun execute(): Boolean {
        check(!closed) { throw LSDException("Statement is closed.") }
        closeForNextExecution()

        if (activeOp is LSDOperation) throw LSDException("Can't use execute method for LSD operations.")

        // Then execute underlying statement
        return when (activeOp) {
            is LSDOperation -> false
            else -> underlyingStatement.execute()
        }
    }

    override fun addBatch() {
        underlyingStatement.addBatch()
    }

    // TODO some of these set methods are incomplete, thus not all are fully supported

    override fun setNull(p0: Int, p1: Int) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setNull(p0, p1)
    }

    override fun setNull(p0: Int, p1: Int, p2: String?) {
        underlyingStatement.setNull(p0, p1, p2)
    }

    override fun setBoolean(p0: Int, p1: Boolean) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setBoolean(p0, p1)
    }

    override fun setByte(p0: Int, p1: Byte) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setByte(p0, p1)
    }

    override fun setShort(p0: Int, p1: Short) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setShort(p0, p1)
    }

    override fun setInt(p0: Int, p1: Int) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setInt(p0, p1)
    }

    override fun setLong(p0: Int, p1: Long) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setLong(p0, p1)
    }

    override fun setFloat(p0: Int, p1: Float) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setFloat(p0, p1)
    }

    override fun setDouble(p0: Int, p1: Double) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1
        else
            underlyingStatement.setDouble(p0, p1)
    }

    override fun setBigDecimal(p0: Int, p1: BigDecimal?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setBigDecimal(p0, p1)
    }

    override fun setString(p0: Int, p1: String?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setString(p0, p1)
    }

    override fun setBytes(p0: Int, p1: ByteArray?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setBytes(p0, p1)
    }

    override fun setDate(p0: Int, p1: Date?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setDate(p0, p1)
    }

    override fun setDate(p0: Int, p1: Date?, p2: Calendar?) {
        underlyingStatement.setDate(p0, p1, p2)
    }

    override fun setTime(p0: Int, p1: Time?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setTime(p0, p1)
    }

    override fun setTime(p0: Int, p1: Time?, p2: Calendar?) {
        underlyingStatement.setTime(p0, p1, p2)
    }

    override fun setTimestamp(p0: Int, p1: Timestamp?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setTimestamp(p0, p1)
    }

    override fun setTimestamp(p0: Int, p1: Timestamp?, p2: Calendar?) {
        underlyingStatement.setTimestamp(p0, p1, p2)
    }

    override fun setAsciiStream(p0: Int, p1: InputStream?, p2: Int) {
        underlyingStatement.setAsciiStream(p0, p1, p2)
    }

    override fun setAsciiStream(p0: Int, p1: InputStream?, p2: Long) {
        underlyingStatement.setAsciiStream(p0, p1, p2)
    }

    override fun setAsciiStream(p0: Int, p1: InputStream?) {
        underlyingStatement.setAsciiStream(p0, p1)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("\")"))
    override fun setUnicodeStream(p0: Int, p1: InputStream?, p2: Int) {
        throw Exception("Deprecated in Java")
    }

    override fun setBinaryStream(p0: Int, p1: InputStream?, p2: Int) {
        underlyingStatement.setBinaryStream(p0, p1, p2)
    }

    override fun setBinaryStream(p0: Int, p1: InputStream?, p2: Long) {
        underlyingStatement.setBinaryStream(p0, p1, p2)
    }

    override fun setBinaryStream(p0: Int, p1: InputStream?) {
        underlyingStatement.setBinaryStream(p0, p1)
    }

    override fun clearParameters() {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters.clear()
        else
            underlyingStatement.clearParameters()
    }

    override fun setObject(p0: Int, p1: Any?, p2: Int) {
        underlyingStatement.setObject(p0, p1, p2)
    }

    override fun setObject(p0: Int, p1: Any?) {
        if (activeOp is LSDOperation)
            (activeOp as LSDOperation).parameters[p0] = p1!!
        else
            underlyingStatement.setObject(p0, p1)
    }

    override fun setObject(p0: Int, p1: Any?, p2: Int, p3: Int) {
        underlyingStatement.setObject(p0, p1, p2, p3)
    }

    override fun setCharacterStream(p0: Int, p1: Reader?, p2: Int) {
        underlyingStatement.setCharacterStream(p0, p1, p2)
    }

    override fun setCharacterStream(p0: Int, p1: Reader?, p2: Long) {
        underlyingStatement.setCharacterStream(p0, p1, p2)
    }

    override fun setCharacterStream(p0: Int, p1: Reader?) {
        underlyingStatement.setCharacterStream(p0, p1)
    }

    override fun setRef(p0: Int, p1: Ref?) {
        underlyingStatement.setRef(p0, p1)
    }

    override fun setBlob(p0: Int, p1: Blob?) {
        underlyingStatement.setBlob(p0, p1)
    }

    override fun setBlob(p0: Int, p1: InputStream?, p2: Long) {
        underlyingStatement.setBlob(p0, p1, p2)
    }

    override fun setBlob(p0: Int, p1: InputStream?) {
        underlyingStatement.setBlob(p0, p1)
    }

    override fun setClob(p0: Int, p1: Clob?) {
        underlyingStatement.setClob(p0, p1)
    }

    override fun setClob(p0: Int, p1: Reader?, p2: Long) {
        underlyingStatement.setClob(p0, p1, p2)
    }

    override fun setClob(p0: Int, p1: Reader?) {
        underlyingStatement.setClob(p0, p1)
    }

    override fun setArray(p0: Int, p1: Array?) {
        underlyingStatement.setArray(p0, p1)
    }

    override fun getMetaData(): ResultSetMetaData {
        return underlyingStatement.metaData
    }

    override fun setURL(p0: Int, p1: URL?) {
        underlyingStatement.setURL(p0, p1)
    }

    override fun getParameterMetaData(): ParameterMetaData {
        return underlyingStatement.parameterMetaData
    }

    override fun setRowId(p0: Int, p1: RowId?) {
        underlyingStatement.setRowId(p0, p1)
    }

    override fun setNString(p0: Int, p1: String?) {
        underlyingStatement.setNString(p0, p1)
    }

    override fun setNCharacterStream(p0: Int, p1: Reader?, p2: Long) {
        underlyingStatement.setNCharacterStream(p0, p1, p2)
    }

    override fun setNCharacterStream(p0: Int, p1: Reader?) {
        underlyingStatement.setNCharacterStream(p0, p1)
    }

    override fun setNClob(p0: Int, p1: NClob?) {
        underlyingStatement.setNClob(p0, p1)
    }

    override fun setNClob(p0: Int, p1: Reader?, p2: Long) {
        underlyingStatement.setNClob(p0, p1, p2)
    }

    override fun setNClob(p0: Int, p1: Reader?) {
        underlyingStatement.setNClob(p0, p1)
    }

    override fun setSQLXML(p0: Int, p1: SQLXML?) {
        underlyingStatement.setSQLXML(p0, p1)
    }

    override fun getResultSet(): ResultSet {
        return activeResultSet
    }

    override fun close() {
        if (!closed) {
            closed = true
        }
    }
}