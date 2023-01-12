package lsd.core.parser.operations.lsd

class UpdateLSD(sql: String?) : LSDOperation(sql) {
    override fun reset(): LSDOperation {
        return UpdateLSD(this.query)
    }
}