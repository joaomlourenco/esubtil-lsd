import lsd.Driver
import lsd.jdbc.LSDConnection
import lsd.jdbc.LSDResultSet
import lsd.util.LSDException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import util.Queries
import java.sql.*
import java.util.*

// TODO these tests could be improved, they could attempt to run TPC-C transactions

class DriverTest {
    private val urlLSD = "jdbc:lsd:postgresql://localhost/benchmarksql_test"
    private val url = "jdbc:postgresql://localhost/benchmarksql_test"

    @Test
    @DisplayName("URL Acceptance")
    fun acceptsURLTest() {
        assert(Driver().acceptsURL(urlLSD))
    }

    @Test
    @DisplayName("URL Conversion")
    fun convertURLTest() {
        assert(Driver().convertURL(urlLSD) == url)
    }

    @Test
    @DisplayName("Establish Connection")
    fun establishConnection() {
        val props = loadProps()
        val connLSD = createConnLSD(props)
        assert(connLSD.javaClass == LSDConnection::class.java)
        connLSD.close()
    }

    @Test
    @DisplayName("Normal statement execution in auto commit mode")
    fun executeStatement() {
        val props = loadProps()
        val connLSD = createConnLSD(props)

        val stmt = connLSD.createStatement()
        for (query in Queries.simpleQueries)
            stmt.execute(query)

        stmt.close()
        connLSD.close()
    }

    @Test
    @DisplayName("Prepared statement execution in auto commit mode")
    fun executePreparedStatement() {
        val props = loadProps()
        val connLSD = createConnLSD(props)

        for (query in Queries.simpleQueries) {
            val stmt = connLSD.prepareStatement(query)
            stmt.close()
        }

        connLSD.close()
    }

    @Test
    @DisplayName("Simple IS-TRUE test for success")
    fun executeIsTrueSuccess() {
        val props = loadProps()
        val connLSD = createConnLSD(props)
        connLSD.autoCommit = false

        var stmt = connLSD.prepareStatement(Queries.simpleLSDCountQuery)
        val rs = stmt.executeQuery() as LSDResultSet
        val future = rs.future
        rs.close()
        stmt.close()

        stmt = connLSD.prepareStatement(Queries.simpleIsTrue)
        stmt.setString(1, future.id)
        stmt.setInt(2, 10)
        stmt.executeUpdate()
        stmt.close()

        connLSD.commit()
        connLSD.close()
    }

    @Test
    @DisplayName("Simple IS-TRUE test for failure")
    fun executeIsTrueFailure() {
        val props = loadProps()
        val connLSD = createConnLSD(props)
        connLSD.autoCommit = false

        var stmt = connLSD.prepareStatement(Queries.simpleLSDCountQuery)
        val rs = stmt.executeQuery() as LSDResultSet
        val future = rs.future
        rs.close()
        stmt.close()

        stmt = connLSD.prepareStatement(Queries.simpleIsTrue)
        stmt.setString(1, future.id)
        stmt.setInt(2, 1000000)
        stmt.executeUpdate()
        stmt.close()

        Assertions.assertThrows(LSDException::class.java) {
            connLSD.commit()
        }

        connLSD.close()
    }

    @Test
    @DisplayName("Simple IF_LSD test")
    fun executeIfLSD() {
        val props = loadProps()
        val connLSD = createConnLSD(props)
        connLSD.autoCommit = false

        var stmt = connLSD.prepareStatement(Queries.simpleLSDCountQuery)
        val rs = stmt.executeQuery() as LSDResultSet
        val future = rs.future
        rs.close()
        stmt.close()

        stmt = connLSD.prepareStatement(Queries.getNextOID)
        stmt.setInt(1, 1)
        stmt.setInt(2, 2)
        stmt.executeQuery()
        rs.close()
        stmt.close()

        stmt = connLSD.prepareStatement(Queries.simpleIfLSD)
        // IS-TRUE
        stmt.setString(1, future.id)
        stmt.setInt(2, 10)

        // thenOp -> Update
        stmt.setInt(3, 1)
        stmt.setInt(4, 2)

        // elseOp -> Insert
        stmt.setInt(5, 1)
        stmt.setInt(6, 2)
        stmt.setInt(7, 3)
        stmt.setInt(8, 4)
        stmt.executeUpdate()
        stmt.close()

        connLSD.commit()
        connLSD.close()
    }

    @Test
    @DisplayName("Execute simple LSD transaction without auto commit")
    fun executeSimpleTxnNoAutoCommit() {
        val props = loadProps()
        val connLSD = createConnLSD(props)
        connLSD.autoCommit = false

        val dWID = 2
        val dID = 1

        var prepStmt = connLSD.prepareStatement(Queries.getNextOID) // ask for future next order ID
        prepStmt.setInt(1, dWID)
        prepStmt.setInt(2, dID)
        val rs = prepStmt.executeQuery() as LSDResultSet // get future
        val future = rs.future
        rs.close()
        prepStmt.close()

        prepStmt = connLSD.prepareStatement(Queries.updateNextOID)
        prepStmt.setString(1, future.id)
        prepStmt.setInt(2, dWID)
        prepStmt.setInt(3, dID)
        prepStmt.executeUpdate()
        prepStmt.close()

        /* TODO
            ERROR: insert or update on table "bmsql_new_order" violates foreign key constraint "no_order_fkey"
            Detail: Key (no_w_id, no_d_id, no_o_id)=(1, 2, 3001) is not present in table "bmsql_oorder"

            This happens because other insertions aren't made, but this proves insert works.
         */
        /*prepStmt = connLSD.prepareStatement(Queries.insertNewOrder)
        prepStmt.setString(1, rs.future.id)
        prepStmt.setInt(2, dWID)
        prepStmt.setInt(3, dID)
        prepStmt.executeUpdate()
        prepStmt.close()*/

        connLSD.commit()
        connLSD.close()
    }

    private fun loadProps(): Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("lsd.properties"))

        return props
    }

    private fun createConnLSD(props: Properties): Connection {
        DriverManager.registerDriver(Driver())
        return DriverManager.getConnection(props.getProperty("connLSD"), props)
    }

    /*private fun compareResultSets(rs1: ResultSet, rs2: ResultSet): Boolean {
        val maxCols = rs1.metaData.columnCount

        while (rs1.next() && rs2.next()) {
            for (col in 1..maxCols) {
                val res1: Any = rs1.getObject(col)
                val res2: Any = rs2.getObject(col)

                // Check values
                if (res1 != res2) {
                    return false
                }
            }

            // rs1 and rs2 must reach last row in the same iteration
            if (rs1.isLast != rs2.isLast) {
                return false
            }
        }

        return true
    }*/

    /*@Test
    fun getResultClasses() {
        var connLSD: Connection? = null
        try {
            val props = loadProps()

            connLSD = createConnLSD(props)

            for (query in Queries.newOrderQueries) {
                val rs = executeQuery(connLSD, query)
                rs.next() // move to first line
                val maxCols = rs.metaData.columnCount
                print("Query '$query' has columns of type ")
                for (col in 1..maxCols) {
                    print("| ${rs.getObject(col).javaClass.simpleName} ")
                }
                println("|")
                rs.close()
            }

        } finally {
            connLSD!!.close()
        }
    }*/

}