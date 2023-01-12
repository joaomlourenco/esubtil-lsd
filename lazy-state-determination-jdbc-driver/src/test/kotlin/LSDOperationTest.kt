import lsd.core.parser.Parser
import lsd.core.parser.operations.lsd.SelectLSD
import lsd.core.parser.operations.lsd.UpdateLSD
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import util.Queries
import java.util.concurrent.TimeUnit


class LSDOperationTest {
    private val parser = Parser()

    @Test
    @DisplayName("Insert parameters into query - Simple LSD Query")
    fun prepareForExecutionTestSimpleLSDQuery() {
        val op = parser.parse(Queries.getNextOID) as SelectLSD
        op.parameters[1] = 1
        op.parameters[2] = 2
        op.parameters[3] = 3 // Tests if inserting too many arguments has any effect
        op.parameters[4] = 4 // Tests if inserting too many arguments has any effect

        op.prepareForLSDExecution()

        assert(op.activeQuery == Queries.getNextOIDExample)
        assert(op.future == Queries.getNextOIDFuture)
    }

    @Test
    @DisplayName("Insert parameters into query - Simple LSD Update")
    fun prepareForExecutionTestSimpleLSDUpdate() {
        val op = UpdateLSD(Queries.simpleLSDUpdate)
        op.parameters[1] = 1
        op.parameters[2] = 2
        op.parameters[3] = 3 // Tests if inserting too many arguments has any effect
        op.parameters[4] = 4 // Tests if inserting too many arguments has any effect

        op.prepareForLSDExecution()

        assert(op.activeQuery == Queries.simpleLSDUpdateExample)
    }

    @Test
    @DisplayName("Insert parameters into query - Simple LSD Insert")
    fun prepareForExecutionTestSimpleLSDInsert() {
        val op = UpdateLSD(Queries.simpleLSDInsert)
        op.parameters[1] = 1
        op.parameters[2] = 2
        op.parameters[3] = 3 // Tests if inserting too many arguments has any effect
        op.parameters[4] = 4 // Tests if inserting too many arguments has any effect

        op.prepareForLSDExecution()

        assert(op.activeQuery == Queries.simpleLSDInsertExample)
    }

    @Test
    @DisplayName("Insert parameters into query - Simple LSD If")
    fun prepareForExecutionTestSimpleLSDIf() {
        val op = UpdateLSD(Queries.simpleIfLSD)
        op.parameters[1] = "{${Queries.simpleLSDCountQueryFuture}}"
        op.parameters[2] = 10
        op.parameters[3] = 1
        op.parameters[4] = 2
        op.parameters[5] = 1
        op.parameters[6] = 2
        op.parameters[7] = 3
        op.parameters[8] = 4

        op.prepareForLSDExecution()

        assert(op.activeQuery == Queries.simpleIfLSDExample)
    }
}