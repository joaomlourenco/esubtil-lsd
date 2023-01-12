package lsd.core.parser.operations.lsd

import org.apache.commons.jexl3.JexlEngine
import org.apache.commons.jexl3.MapContext

class IsTrue(
    query: String,
    private val engine: JexlEngine
) : LSDOperation(query) {

    fun evaluate(): Boolean {
        val context = MapContext()
        return engine.createExpression(activeQuery).evaluate(context) as Boolean
    }

    override fun reset(): LSDOperation {
        return IsTrue(this.query, engine)
    }

}