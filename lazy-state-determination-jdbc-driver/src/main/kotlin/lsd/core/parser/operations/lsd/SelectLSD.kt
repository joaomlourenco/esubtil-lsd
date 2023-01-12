package lsd.core.parser.operations.lsd

import java.lang.StringBuilder


open class SelectLSD(sql: String?, internal var future: String) : LSDOperation(sql) {

    companion object {
        const val WHERE = "WHERE"
        const val AND = "AND"
        const val FOR_UPDATE = "FOR UPDATE"
        const val END = ";"
    }

    var futureUpdated = false
    var originalFuture = future

    override fun prepareForLSDExecution() {
        super.prepareForLSDExecution()

        if (!futureUpdated) {
            // change future value to reflect the where values
            val newFuture = StringBuilder()
            newFuture.append(future)
            var pos1 = activeQuery.indexOf(WHERE)
            if (pos1 != -1) {
                newFuture.append(":(")
                pos1 += WHERE.length
                var pos2 = activeQuery.indexOf(AND, pos1)
                if (pos2 == -1) pos2 = activeQuery.indexOf(END, pos1)
                if (pos2 == -1) pos2 = activeQuery.indexOf(FOR_UPDATE, pos1)
                if (pos2 == -1) pos2 = activeQuery.length
                var finished = false
                var lastParameter = false
                while (!finished) {
                    newFuture.append(activeQuery.substring(pos1, pos2).trim())
                    if (lastParameter)
                        finished = true
                    else {
                        pos1 = pos2 + AND.length

                        pos2 = activeQuery.indexOf(AND, pos1)
                        if (pos2 == -1) {
                            lastParameter = true
                            pos2 = activeQuery.indexOf(END, pos1)
                        }

                        if (pos2 == -1)
                            pos2 = activeQuery.indexOf(FOR_UPDATE, pos1)

                        if (pos2 == -1)
                            pos2 = activeQuery.length

                        newFuture.append(",")
                    }
                }
                newFuture.append(")")
            }
            future = newFuture.toString()
            futureUpdated = true
        }
    }

    override fun reset(): LSDOperation {
        return SelectLSD(this.query, originalFuture)
    }
}