package lsd.core.parser.operations.lsd

import lsd.core.parser.operations.Operation
import lsd.util.LSDException
import java.lang.StringBuilder
import java.sql.Timestamp
import java.util.*
import kotlin.NoSuchElementException

abstract class LSDOperation(query: String?) : Operation(query) {

    companion object {
        const val WILDCARD = '?'
    }

    open var activeQuery = this.query
    val parameters = TreeMap<Int, Any>()

    /**
     * This method inserts all received parameters into the query.
     * This must be invoked prior to sending query to database
     */
    internal open fun prepareForLSDExecution() {
        var pos2 = activeQuery.indexOf(WILDCARD, 0)
        if (pos2 == -1) return

        var pos = 0
        val it = parameters.iterator()
        val stringBuilder = StringBuilder()

        try {
            while (pos2 != -1) {
                stringBuilder.append(activeQuery, pos, pos2)
                when (val par = it.next().value) {
                    is String ->
                        if (par.startsWith("{") && par.endsWith("}"))
                            stringBuilder.append(par)
                        else
                            stringBuilder.append("'$par'")
                    is Timestamp -> stringBuilder.append("'${par}'")
                    else -> stringBuilder.append(par)
                }

                pos = pos2 + 1
                pos2 = activeQuery.indexOf(WILDCARD, pos)
            }
            stringBuilder.append(activeQuery, pos, activeQuery.length)
            activeQuery = stringBuilder.toString()
            parameters.clear()
        } catch (e: NoSuchElementException) {
            throw LSDException("Not enough parameters were passed!\nQuery: $activeQuery")
        }
    }

    abstract fun reset(): LSDOperation
}