package lsd.jdbc

import lsd.util.LSDException
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*

class LSDResultSet(val future: LSDFuture) : ResultSet {
    var closed = false

    override fun <T : Any?> unwrap(iface: Class<T>?): T {
        if (iface!!.isAssignableFrom(javaClass))
            return iface.cast(this)

        throw LSDException("Cannot unwrap to " + iface.name)
    }

    override fun isWrapperFor(iface: Class<*>?): Boolean {
        return iface!!.isAssignableFrom(javaClass)
    }

    override fun close() {
        if (!closed) closed = true
    }

    override fun next(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun wasNull(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun getString(p0: Int): String {
        throw LSDException("Not implemented")
    }

    override fun getString(p0: String?): String {
        throw LSDException("Not implemented")
    }

    override fun getBoolean(p0: Int): Boolean {
        throw LSDException("Not implemented")
    }

    override fun getBoolean(p0: String?): Boolean {
        throw LSDException("Not implemented")
    }

    override fun getByte(p0: Int): Byte {
        throw LSDException("Not implemented")
    }

    override fun getByte(p0: String?): Byte {
        throw LSDException("Not implemented")
    }

    override fun getShort(p0: Int): Short {
        throw LSDException("Not implemented")
    }

    override fun getShort(p0: String?): Short {
        throw LSDException("Not implemented")
    }

    override fun getInt(p0: Int): Int {
        throw LSDException("Not implemented")
    }

    override fun getInt(p0: String?): Int {
        throw LSDException("Not implemented")
    }

    override fun getLong(p0: Int): Long {
        throw LSDException("Not implemented")
    }

    override fun getLong(p0: String?): Long {
        throw LSDException("Not implemented")
    }

    override fun getFloat(p0: Int): Float {
        throw LSDException("Not implemented")
    }

    override fun getFloat(p0: String?): Float {
        throw LSDException("Not implemented")
    }

    override fun getDouble(p0: Int): Double {
        throw LSDException("Not implemented")
    }

    override fun getDouble(p0: String?): Double {
        throw LSDException("Not implemented")
    }

    override fun getBigDecimal(p0: Int, p1: Int): BigDecimal {
        throw LSDException("Not implemented")
    }

    override fun getBigDecimal(p0: String?, p1: Int): BigDecimal {
        throw LSDException("Not implemented")
    }

    override fun getBigDecimal(p0: Int): BigDecimal {
        throw LSDException("Not implemented")
    }

    override fun getBigDecimal(p0: String?): BigDecimal {
        throw LSDException("Not implemented")
    }

    override fun getBytes(p0: Int): ByteArray {
        throw LSDException("Not implemented")
    }

    override fun getBytes(p0: String?): ByteArray {
        throw LSDException("Not implemented")
    }

    override fun getDate(p0: Int): Date {
        throw LSDException("Not implemented")
    }

    override fun getDate(p0: String?): Date {
        throw LSDException("Not implemented")
    }

    override fun getDate(p0: Int, p1: Calendar?): Date {
        throw LSDException("Not implemented")
    }

    override fun getDate(p0: String?, p1: Calendar?): Date {
        throw LSDException("Not implemented")
    }

    override fun getTime(p0: Int): Time {
        throw LSDException("Not implemented")
    }

    override fun getTime(p0: String?): Time {
        throw LSDException("Not implemented")
    }

    override fun getTime(p0: Int, p1: Calendar?): Time {
        throw LSDException("Not implemented")
    }

    override fun getTime(p0: String?, p1: Calendar?): Time {
        throw LSDException("Not implemented")
    }

    override fun getTimestamp(p0: Int): Timestamp {
        throw LSDException("Not implemented")
    }

    override fun getTimestamp(p0: String?): Timestamp {
        throw LSDException("Not implemented")
    }

    override fun getTimestamp(p0: Int, p1: Calendar?): Timestamp {
        throw LSDException("Not implemented")
    }

    override fun getTimestamp(p0: String?, p1: Calendar?): Timestamp {
        throw LSDException("Not implemented")
    }

    override fun getAsciiStream(p0: Int): InputStream {
        throw LSDException("Not implemented")
    }

    override fun getAsciiStream(p0: String?): InputStream {
        throw LSDException("Not implemented")
    }

    override fun getUnicodeStream(p0: Int): InputStream {
        throw LSDException("Not implemented")
    }

    override fun getUnicodeStream(p0: String?): InputStream {
        throw LSDException("Not implemented")
    }

    override fun getBinaryStream(p0: Int): InputStream {
        throw LSDException("Not implemented")
    }

    override fun getBinaryStream(p0: String?): InputStream {
        throw LSDException("Not implemented")
    }

    override fun getWarnings(): SQLWarning {
        throw LSDException("Not implemented")
    }

    override fun clearWarnings() {
        throw LSDException("Not implemented")
    }

    override fun getCursorName(): String {
        throw LSDException("Not implemented")
    }

    override fun getMetaData(): ResultSetMetaData {
        throw LSDException("Not implemented")
    }

    override fun getObject(p0: Int): Any {
        throw LSDException("Not implemented")
    }

    override fun getObject(p0: String?): Any {
        throw LSDException("Not implemented")
    }

    override fun getObject(p0: Int, p1: MutableMap<String, Class<*>>?): Any {
        throw LSDException("Not implemented")
    }

    override fun getObject(p0: String?, p1: MutableMap<String, Class<*>>?): Any {
        throw LSDException("Not implemented")
    }

    override fun <T : Any?> getObject(p0: Int, p1: Class<T>?): T {
        throw LSDException("Not implemented")
    }

    override fun <T : Any?> getObject(p0: String?, p1: Class<T>?): T {
        throw LSDException("Not implemented")
    }

    override fun findColumn(p0: String?): Int {
        throw LSDException("Not implemented")
    }

    override fun getCharacterStream(p0: Int): Reader {
        throw LSDException("Not implemented")
    }

    override fun getCharacterStream(p0: String?): Reader {
        throw LSDException("Not implemented")
    }

    override fun isBeforeFirst(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun isAfterLast(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun isFirst(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun isLast(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun beforeFirst() {
        throw LSDException("Not implemented")
    }

    override fun afterLast() {
        throw LSDException("Not implemented")
    }

    override fun first(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun last(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun getRow(): Int {
        throw LSDException("Not implemented")
    }

    override fun absolute(p0: Int): Boolean {
        throw LSDException("Not implemented")
    }

    override fun relative(p0: Int): Boolean {
        throw LSDException("Not implemented")
    }

    override fun previous(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun setFetchDirection(p0: Int) {
        throw LSDException("Not implemented")
    }

    override fun getFetchDirection(): Int {
        throw LSDException("Not implemented")
    }

    override fun setFetchSize(p0: Int) {
        throw LSDException("Not implemented")
    }

    override fun getFetchSize(): Int {
        throw LSDException("Not implemented")
    }

    override fun getType(): Int {
        throw LSDException("Not implemented")
    }

    override fun getConcurrency(): Int {
        throw LSDException("Not implemented")
    }

    override fun rowUpdated(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun rowInserted(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun rowDeleted(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun updateNull(p0: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateNull(p0: String?) {
        throw LSDException("Not implemented")
    }

    override fun updateBoolean(p0: Int, p1: Boolean) {
        throw LSDException("Not implemented")
    }

    override fun updateBoolean(p0: String?, p1: Boolean) {
        throw LSDException("Not implemented")
    }

    override fun updateByte(p0: Int, p1: Byte) {
        throw LSDException("Not implemented")
    }

    override fun updateByte(p0: String?, p1: Byte) {
        throw LSDException("Not implemented")
    }

    override fun updateShort(p0: Int, p1: Short) {
        throw LSDException("Not implemented")
    }

    override fun updateShort(p0: String?, p1: Short) {
        throw LSDException("Not implemented")
    }

    override fun updateInt(p0: Int, p1: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateInt(p0: String?, p1: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateLong(p0: Int, p1: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateLong(p0: String?, p1: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateFloat(p0: Int, p1: Float) {
        throw LSDException("Not implemented")
    }

    override fun updateFloat(p0: String?, p1: Float) {
        throw LSDException("Not implemented")
    }

    override fun updateDouble(p0: Int, p1: Double) {
        throw LSDException("Not implemented")
    }

    override fun updateDouble(p0: String?, p1: Double) {
        throw LSDException("Not implemented")
    }

    override fun updateBigDecimal(p0: Int, p1: BigDecimal?) {
        throw LSDException("Not implemented")
    }

    override fun updateBigDecimal(p0: String?, p1: BigDecimal?) {
        throw LSDException("Not implemented")
    }

    override fun updateString(p0: Int, p1: String?) {
        throw LSDException("Not implemented")
    }

    override fun updateString(p0: String?, p1: String?) {
        throw LSDException("Not implemented")
    }

    override fun updateBytes(p0: Int, p1: ByteArray?) {
        throw LSDException("Not implemented")
    }

    override fun updateBytes(p0: String?, p1: ByteArray?) {
        throw LSDException("Not implemented")
    }

    override fun updateDate(p0: Int, p1: Date?) {
        throw LSDException("Not implemented")
    }

    override fun updateDate(p0: String?, p1: Date?) {
        throw LSDException("Not implemented")
    }

    override fun updateTime(p0: Int, p1: Time?) {
        throw LSDException("Not implemented")
    }

    override fun updateTime(p0: String?, p1: Time?) {
        throw LSDException("Not implemented")
    }

    override fun updateTimestamp(p0: Int, p1: Timestamp?) {
        throw LSDException("Not implemented")
    }

    override fun updateTimestamp(p0: String?, p1: Timestamp?) {
        throw LSDException("Not implemented")
    }

    override fun updateAsciiStream(p0: Int, p1: InputStream?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateAsciiStream(p0: String?, p1: InputStream?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateAsciiStream(p0: Int, p1: InputStream?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateAsciiStream(p0: String?, p1: InputStream?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateAsciiStream(p0: Int, p1: InputStream?) {
        throw LSDException("Not implemented")
    }

    override fun updateAsciiStream(p0: String?, p1: InputStream?) {
        throw LSDException("Not implemented")
    }

    override fun updateBinaryStream(p0: Int, p1: InputStream?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateBinaryStream(p0: String?, p1: InputStream?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateBinaryStream(p0: Int, p1: InputStream?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateBinaryStream(p0: String?, p1: InputStream?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateBinaryStream(p0: Int, p1: InputStream?) {
        throw LSDException("Not implemented")
    }

    override fun updateBinaryStream(p0: String?, p1: InputStream?) {
        throw LSDException("Not implemented")
    }

    override fun updateCharacterStream(p0: Int, p1: Reader?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateCharacterStream(p0: String?, p1: Reader?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateCharacterStream(p0: Int, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateCharacterStream(p0: String?, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateCharacterStream(p0: Int, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun updateCharacterStream(p0: String?, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun updateObject(p0: Int, p1: Any?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateObject(p0: Int, p1: Any?) {
        throw LSDException("Not implemented")
    }

    override fun updateObject(p0: String?, p1: Any?, p2: Int) {
        throw LSDException("Not implemented")
    }

    override fun updateObject(p0: String?, p1: Any?) {
        throw LSDException("Not implemented")
    }

    override fun insertRow() {
        throw LSDException("Not implemented")
    }

    override fun updateRow() {
        throw LSDException("Not implemented")
    }

    override fun deleteRow() {
        throw LSDException("Not implemented")
    }

    override fun refreshRow() {
        throw LSDException("Not implemented")
    }

    override fun cancelRowUpdates() {
        throw LSDException("Not implemented")
    }

    override fun moveToInsertRow() {
        throw LSDException("Not implemented")
    }

    override fun moveToCurrentRow() {
        throw LSDException("Not implemented")
    }

    override fun getStatement(): Statement {
        throw LSDException("Not implemented")
    }

    override fun getRef(p0: Int): Ref {
        throw LSDException("Not implemented")
    }

    override fun getRef(p0: String?): Ref {
        throw LSDException("Not implemented")
    }

    override fun getBlob(p0: Int): Blob {
        throw LSDException("Not implemented")
    }

    override fun getBlob(p0: String?): Blob {
        throw LSDException("Not implemented")
    }

    override fun getClob(p0: Int): Clob {
        throw LSDException("Not implemented")
    }

    override fun getClob(p0: String?): Clob {
        throw LSDException("Not implemented")
    }

    override fun getArray(p0: Int): Array {
        throw LSDException("Not implemented")
    }

    override fun getArray(p0: String?): Array {
        throw LSDException("Not implemented")
    }

    override fun getURL(p0: Int): URL {
        throw LSDException("Not implemented")
    }

    override fun getURL(p0: String?): URL {
        throw LSDException("Not implemented")
    }

    override fun updateRef(p0: Int, p1: Ref?) {
        throw LSDException("Not implemented")
    }

    override fun updateRef(p0: String?, p1: Ref?) {
        throw LSDException("Not implemented")
    }

    override fun updateBlob(p0: Int, p1: Blob?) {
        throw LSDException("Not implemented")
    }

    override fun updateBlob(p0: String?, p1: Blob?) {
        throw LSDException("Not implemented")
    }

    override fun updateBlob(p0: Int, p1: InputStream?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateBlob(p0: String?, p1: InputStream?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateBlob(p0: Int, p1: InputStream?) {
        throw LSDException("Not implemented")
    }

    override fun updateBlob(p0: String?, p1: InputStream?) {
        throw LSDException("Not implemented")
    }

    override fun updateClob(p0: Int, p1: Clob?) {
        throw LSDException("Not implemented")
    }

    override fun updateClob(p0: String?, p1: Clob?) {
        throw LSDException("Not implemented")
    }

    override fun updateClob(p0: Int, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateClob(p0: String?, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateClob(p0: Int, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun updateClob(p0: String?, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun updateArray(p0: Int, p1: Array?) {
        throw LSDException("Not implemented")
    }

    override fun updateArray(p0: String?, p1: Array?) {
        throw LSDException("Not implemented")
    }

    override fun getRowId(p0: Int): RowId {
        throw LSDException("Not implemented")
    }

    override fun getRowId(p0: String?): RowId {
        throw LSDException("Not implemented")
    }

    override fun updateRowId(p0: Int, p1: RowId?) {
        throw LSDException("Not implemented")
    }

    override fun updateRowId(p0: String?, p1: RowId?) {
        throw LSDException("Not implemented")
    }

    override fun getHoldability(): Int {
        throw LSDException("Not implemented")
    }

    override fun isClosed(): Boolean {
        throw LSDException("Not implemented")
    }

    override fun updateNString(p0: Int, p1: String?) {
        throw LSDException("Not implemented")
    }

    override fun updateNString(p0: String?, p1: String?) {
        throw LSDException("Not implemented")
    }

    override fun updateNClob(p0: Int, p1: NClob?) {
        throw LSDException("Not implemented")
    }

    override fun updateNClob(p0: String?, p1: NClob?) {
        throw LSDException("Not implemented")
    }

    override fun updateNClob(p0: Int, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateNClob(p0: String?, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateNClob(p0: Int, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun updateNClob(p0: String?, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun getNClob(p0: Int): NClob {
        throw LSDException("Not implemented")
    }

    override fun getNClob(p0: String?): NClob {
        throw LSDException("Not implemented")
    }

    override fun getSQLXML(p0: Int): SQLXML {
        throw LSDException("Not implemented")
    }

    override fun getSQLXML(p0: String?): SQLXML {
        throw LSDException("Not implemented")
    }

    override fun updateSQLXML(p0: Int, p1: SQLXML?) {
        throw LSDException("Not implemented")
    }

    override fun updateSQLXML(p0: String?, p1: SQLXML?) {
        throw LSDException("Not implemented")
    }

    override fun getNString(p0: Int): String {
        throw LSDException("Not implemented")
    }

    override fun getNString(p0: String?): String {
        throw LSDException("Not implemented")
    }

    override fun getNCharacterStream(p0: Int): Reader {
        throw LSDException("Not implemented")
    }

    override fun getNCharacterStream(p0: String?): Reader {
        throw LSDException("Not implemented")
    }

    override fun updateNCharacterStream(p0: Int, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateNCharacterStream(p0: String?, p1: Reader?, p2: Long) {
        throw LSDException("Not implemented")
    }

    override fun updateNCharacterStream(p0: Int, p1: Reader?) {
        throw LSDException("Not implemented")
    }

    override fun updateNCharacterStream(p0: String?, p1: Reader?) {
        throw LSDException("Not implemented")
    }
}