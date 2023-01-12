package lsd.core.parser.operations.lsd

import lsd.jdbc.LSDConnection
import lsd.util.LSDException


class IFLSD(query: String) : LSDOperation(query) {

    companion object {
        const val IF_LSD_SEPARATOR = ";;"
        const val IF_LSD = "IF_LSD"
    }

    fun resolve(conn: LSDConnection): String {
        val tokens = activeQuery.split(IF_LSD_SEPARATOR)
        val isTrue = conn.parser.parse(tokens[0].replaceFirst(IF_LSD, ""))
        if (isTrue !is IsTrue) throw LSDException("IF_LSD condition couldn't be parsed correctly, got ${isTrue.javaClass}")

        return if (isTrue.evaluate())
            tokens[1].trim() // thenOp
        else
            tokens[2].trim() // elseOp
    }

    override fun reset(): LSDOperation {
        return IFLSD(this.query)
    }
}