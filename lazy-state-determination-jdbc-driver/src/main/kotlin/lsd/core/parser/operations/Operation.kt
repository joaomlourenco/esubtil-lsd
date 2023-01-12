package lsd.core.parser.operations

import java.sql.*
import java.util.*

abstract class Operation(query: String?) {

    val query = query ?: ""
    var generatedKeys: Int = Statement.NO_GENERATED_KEYS
    var columnIndexes: IntArray? = null
    var columnNames: Array<out String>? = null

    fun usedGeneratedKeys(): Boolean {
        return generatedKeys != Statement.NO_GENERATED_KEYS
    }

    fun usedColumnIndexes(): Boolean {
        return columnIndexes != null
    }

    fun usedColumnNames(): Boolean {
        return columnNames != null
    }
}