import lsd.core.parser.Parser
import lsd.core.parser.operations.*
import lsd.core.parser.operations.lsd.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import util.Queries

class ParserTest {

    private val parser = Parser()

    @Test
    @DisplayName("Instruction Parsing - Simple Query")
    fun simpleQuery() {
        assert(parser.parse(Queries.simpleQuery) is StandardSQL)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple LSD Query")
    fun simpleLSDQuery() {
        assert(parser.parse(Queries.simpleLSDQuery) is SelectLSD)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple Query For Update")
    fun simpleQueryForUpdate() {
        assert(parser.parse(Queries.simpleQueryForUpdate) is StandardSQL)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple LSD Query For Update")
    fun simpleLSDQueryForUpdate() {
        assert(parser.parse(Queries.simpleLSDQueryForUpdate) is SelectLSD)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple Count Query")
    fun simpleCount() {
        assert(parser.parse(Queries.simpleCountQuery) is StandardSQL)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple Count Query")
    fun simpleLSDCount() {
        assert(parser.parse(Queries.simpleLSDCountQuery) is SelectLSD)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple Update")
    fun simpleUpdate() {
        assert(parser.parse(Queries.simpleUpdate) is StandardSQL)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple LSD Update")
    fun simpleLSDUpdate() {
        assert(parser.parse(Queries.simpleLSDUpdate) is UpdateLSD)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple Insert")
    fun simpleInsert() {
        assert(parser.parse(Queries.simpleInsert) is StandardSQL)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple LSD Insert")
    fun simpleLSDInsert() {
        assert(parser.parse(Queries.simpleLSDInsert) is InsertLSD)
    }

    @Test
    @DisplayName("Instruction Parsing - Simple LSD If")
    fun simpleLSDIf() {
        assert(parser.parse(Queries.simpleIfLSD) is IFLSD)
    }

    @Test
    @DisplayName("Instruction Conversion")
    fun convertTest() {
        assert(Queries.simpleQuery == (parser.parse(Queries.simpleLSDQuery) as LSDOperation).activeQuery)
        assert(Queries.simpleCountQuery == (parser.parse(Queries.simpleLSDCountQuery) as LSDOperation).activeQuery)
        assert(Queries.simpleInsert == (parser.parse(Queries.simpleLSDInsert) as LSDOperation).activeQuery)
    }

    @Test
    @DisplayName("Extraction of Fields from Instruction - Simple LSD Query")
    fun getFutureSimpleLSDQuery() {
        val op = parser.parse(Queries.simpleLSDQuery)
        assert(op is SelectLSD)
        assert((op as SelectLSD).future == Queries.simpleLSDQueryFuture)
    }

    @Test
    @DisplayName("Extraction of Fields from Instruction - Simple LSD Query Join")
    fun getFutureSimpleLSDQueryJoin() {
        val op = parser.parse(Queries.simpleLSDQueryJoin)
        assert(op is SelectLSD)
        assert((op as SelectLSD).future == Queries.simpleLSDQueryJoinFuture)
    }

    @Test
    @DisplayName("Extraction of Fields from Instruction - Simple LSD Query For Update")
    fun getFutureSimpleLSDQueryForUpdate() {
        val op = parser.parse(Queries.simpleLSDQueryForUpdate)
        assert(op is SelectLSD)
        assert((op as SelectLSD).future == Queries.simpleLSDQueryFuture)
    }

    @Test
    @DisplayName("Extraction of Fields from Instruction - Simple LSD Query Count")
    fun getFutureSimpleLSDQueryCount() {
        val op = parser.parse(Queries.simpleLSDCountQuery)
        assert(op is SelectLSD)
        assert((op as SelectLSD).future == Queries.simpleLSDCountQueryFuture)
    }

    @Test
    @DisplayName("Resolve futures - Simple LSD Update")
    fun resolveFuturesLSDUpdate() {
        val map = parser.resolveFutures(listOf(Queries.simpleLSDUpdate))

        for (entry in map) {
            assert(entry.key == Queries.simpleLSDUpdateResolved)
            assert(entry.value.contentEquals(Queries.simpleLSDUpdateFutures.toArray()))
        }
    }
}