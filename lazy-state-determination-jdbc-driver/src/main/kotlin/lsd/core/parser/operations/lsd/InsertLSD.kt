package lsd.core.parser.operations.lsd

class InsertLSD(sql: String?) : LSDOperation(sql) {
    override fun reset(): LSDOperation {
        return InsertLSD(this.query)
    }
}