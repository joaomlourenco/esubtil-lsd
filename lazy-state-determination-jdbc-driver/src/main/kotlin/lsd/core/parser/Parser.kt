package lsd.core.parser

import lsd.core.parser.operations.*
import lsd.core.parser.operations.lsd.*
import lsd.util.LSDException
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlEngine

/**
 * This parser is still very simple and only works for simple instructions (i.e. SELECT * FROM table),
 * a more elegant and correct approach would be using an Abstract Syntax Tree.
 * Thus, this whole package should be reimplemented in future work.
 */
class Parser {

    companion object {
        const val WILDCARD = "?"

        const val SELECT = "SELECT"
        const val SELECT_LSD = "SELECT_LSD"
        const val FOR_UPDATE = " FOR UPDATE"

        const val UPDATE = "UPDATE"
        const val UPDATE_LSD = "UPDATE_LSD"

        const val INSERT = "INSERT"
        const val INSERT_LSD = "INSERT_LSD"

        //const val COUNT = "COUNT"

        const val IS_TRUE = "IS-TRUE"
        const val IF_LSD = "IF_LSD"
    }

    private val engine: JexlEngine = JexlBuilder().cache(512).strict(true).silent(false).create()

    /**
     * Parses a query and looks for any LSD API operation
     *
     * @param query the SQL statement
     * @return the operation
     */
    fun parse(query: String?): Operation {
        if (query.isNullOrBlank()) return StandardSQL("")
        val sql = query.replace("\\s+".toRegex(), " ").trim()

        val tokens = sql.split(" ", limit = 2)
        return when (tokens[0].toUpperCase()) {
            SELECT_LSD -> {
                val fields = getFields(sql)
                if (fields.size > 1)
                    throw LSDException("Queries with more than one field are not supported") // TODO

                SelectLSD(sql.replace(SELECT_LSD, SELECT), fields[0])
            }
            UPDATE_LSD -> UpdateLSD(sql.replace(UPDATE_LSD, UPDATE))
            INSERT_LSD -> InsertLSD(sql.replace(INSERT_LSD, INSERT))
            IS_TRUE -> IsTrue(tokens[1], engine) // split extracts condition
            IF_LSD -> IFLSD(sql)
            else -> StandardSQL(sql)
        }
    }

    /**
     * Converts an LSD query into its SQL counterpart
     *
     * @param query the SQL statement
     * @return the converted query
     */
    internal fun convert(query: String): String {
        return when (query.split(" ", limit = 2)[0].toUpperCase()) {
            SELECT_LSD -> query.replace(SELECT_LSD, SELECT)
            UPDATE_LSD -> query.replace(UPDATE_LSD, UPDATE)
            INSERT_LSD -> query.replace(INSERT_LSD, INSERT)
            else -> query
        }
    }

    /**
     * Reads an LSD query and returns the set of futureIDs in order to be later inserted into the statement.
     * Updates have their futures substituted by the SQL Wildcard to enable parameter processing on the
     * lower level driver.
     * @param instructions, the set of update strings that have to be parsed
     * @return a Map with keys being the update and values being the set of futures in the order that they appear
     */
    internal fun resolveFutures(instructions: List<String>): HashMap<String, Array<String>> {
        val newInstructions = hashMapOf<String, Array<String>>()

        for (instruction in instructions) {
            val pair = resolveFutures(instruction)
            newInstructions[pair.first] = pair.second
        }

        return newInstructions
    }

    /**
     * Reads an LSD query and returns the set of futureIDs in order to be later inserted into the statement.
     * Updates have their futures substituted by the SQL Wildcard to enable parameter processing on the
     * lower level driver.
     * @param instruction, the instruction
     * @return a Pair containing the converted instruction and the array of futures in the order that they appear
     */
    internal fun resolveFutures(instruction: String): Pair<String, Array<String>> {
        val futureIds = mutableListOf<String>()
        var newInstruction = instruction

        var pos1 = instruction.indexOf("{") // find first bracket, representing the existence of a future
        while (pos1 != -1) {
            val pos2 = instruction.indexOf("}", pos1) // find second bracket
            val futureId = instruction.substring(pos1 + 1, pos2) // extract future
            futureIds.add(futureId)
            newInstruction = newInstruction.replaceFirst("{$futureId}", WILDCARD) // replace future with wildcard

            pos1 = instruction.indexOf("{", pos2) // try to find next future bracket
        }

        return Pair(newInstruction, futureIds.toTypedArray())
    }

    /**
     * Gets fields from supported queries
     *
     * @param query the SQL statement
     * @return the fields
     */
    private fun getFields(query: String): Array<String> {
        if (query.isBlank()) return arrayOf()
        val sql = query.replace("\\s+".toRegex(), " ").trim()


        val posFirstField = sql.indexOf(" ") + 1 // ignore first space
        val countExists = sql.indexOf("COUNT", posFirstField)
        val posFrom = sql.indexOf(" FROM", posFirstField)
        val posFirstTable = posFrom + 6 // ignore until after space
        var posFirstTableEnd = sql.indexOf(" ", posFirstTable)
        val joinExists = sql.indexOf("JOIN", posFirstTableEnd)

        if (posFirstTableEnd == -1) posFirstTableEnd = sql.indexOf(";", posFirstTable)
        if (posFirstTableEnd == -1) posFirstTableEnd = sql.length

        var tables = sql.substring(posFirstTable, posFirstTableEnd)
        if (joinExists != -1) {
            val posSecondTable = joinExists + 5
            val posSecondTableEnd = sql.indexOf(" ", posSecondTable)
            tables += "|${sql.substring(posSecondTable, posSecondTableEnd)}"
        }

        return if (countExists == -1) {
            val fields = sql.substring(posFirstField, posFrom)
            val fieldsArr = fields.split(", ").toMutableList()

            for (i in fieldsArr.indices)
                fieldsArr[i] = tables + ":" + fieldsArr[i]

            fieldsArr.toTypedArray()
        } else {
            val pos5 = sql.indexOf("(") + 1 // ignore first space
            val pos6 = sql.indexOf(")") // ignore first space

            val field = sql.substring(pos5, pos6)
            arrayOf("$tables:$field:count")
        }
    }
}