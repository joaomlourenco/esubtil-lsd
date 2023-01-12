package util

object Queries {
    const val simpleQuery = "SELECT d_city FROM bmsql_district;"
    const val simpleLSDQuery = "SELECT_LSD d_city FROM bmsql_district;"
    const val simpleLSDQueryFuture = "bmsql_district:d_city"

    const val simpleLSDQueryJoin =
        "SELECT_LSD c_discount " +
                "    FROM bmsql_customer " +
                "    JOIN bmsql_warehouse ON (w_id = c_w_id)"
    const val simpleLSDQueryJoinFuture = "bmsql_customer|bmsql_warehouse:c_discount"

    const val simpleQueryForUpdate = "SELECT d_city FROM bmsql_district FOR UPDATE;"
    const val simpleLSDQueryForUpdate = "SELECT_LSD d_city FROM bmsql_district FOR UPDATE;"

    const val simpleCountQuery = "SELECT COUNT (d_city) FROM bmsql_district;"
    const val simpleLSDCountQuery = "SELECT_LSD COUNT (d_city) FROM bmsql_district;"
    const val simpleLSDCountQueryFuture = "bmsql_district:d_city:count"

    const val simpleUpdate =
        "UPDATE bmsql_district " +
                "SET d_next_o_id = d_next_o_id + 1 " +
                "WHERE d_w_id = ? AND d_id = ?"
    const val simpleUpdateExample =
        "UPDATE bmsql_district " +
                "SET d_next_o_id = d_next_o_id + 1 " +
                "WHERE d_w_id = 1 AND d_id = 2"

    const val simpleLSDUpdateFuture = "bmsql_district:d_next_o_id:(d_w_id = 1,d_id = 2)"
    const val simpleLSDUpdate =
        "UPDATE_LSD bmsql_district " +
                "SET d_next_o_id = {$simpleLSDUpdateFuture} " +
                "WHERE d_w_id = ? AND d_id = ?"
    const val simpleLSDUpdateResolved =
        "UPDATE_LSD bmsql_district " +
                "SET d_next_o_id = ? " +
                "WHERE d_w_id = ? AND d_id = ?"
    const val simpleLSDUpdateExample =
        "UPDATE_LSD bmsql_district " +
                "SET d_next_o_id = {$simpleLSDUpdateFuture} " +
                "WHERE d_w_id = 1 AND d_id = 2"
    val simpleLSDUpdateFutures = hashSetOf(simpleLSDUpdateFuture)

    const val simpleInsert =
        "INSERT INTO bmsql_item (" +
                "i_name, i_price, i_data, i_im_id) " +
                "VALUES (?, ?, ?, ?)"
    const val simpleInsertExample =
        "INSERT INTO bmsql_item (" +
                "i_name, i_price, i_data, i_im_id) " +
                "VALUES (1, 2, 3, 4)"
    const val simpleLSDInsert =
        "INSERT_LSD INTO bmsql_item (" +
                "i_name, i_price, i_data, i_im_id) " +
                "VALUES (?, ?, ?, ?)"
    const val simpleLSDInsertExample =
        "INSERT_LSD INTO bmsql_item (" +
                "i_name, i_price, i_data, i_im_id) " +
                "VALUES (1, 2, 3, 4)"
    val simpleQueries = arrayListOf(
        simpleQuery,
        simpleQueryForUpdate,
        simpleCountQuery
    ) // TODO make some queries have timestamp parameters

    const val simpleIsTrue = "IS-TRUE ? > ?"
    const val simpleIsTrueExample = "IS-TRUE {$simpleLSDCountQueryFuture} > 10"
    const val simpleIfLSD = "IF_LSD $simpleIsTrue;; $simpleLSDUpdate;; $simpleLSDInsert"
    const val simpleIfLSDExample = "IF_LSD $simpleIsTrueExample;; $simpleLSDUpdateExample;; $simpleLSDInsertExample"

    const val getNextOID =
        "SELECT_LSD d_next_o_id " +
                "FROM bmsql_district " +
                "WHERE d_w_id = ? AND d_id = ? " +
                "FOR UPDATE"
    const val getNextOIDExample =
        "SELECT d_next_o_id " +
                "FROM bmsql_district " +
                "WHERE d_w_id = 1 AND d_id = 2 " +
                "FOR UPDATE"
    const val getNextOIDFuture = "bmsql_district:d_next_o_id:(d_w_id = 1,d_id = 2)"
    const val updateNextOID =
        "UPDATE_LSD bmsql_district " +
                "    SET d_next_o_id = ? + 1 " +
                "    WHERE d_w_id = ? AND d_id = ?"
    const val insertNewOrder =
        "INSERT_LSD INTO bmsql_new_order (" +
                "    no_o_id, no_d_id, no_w_id) " +
                "VALUES (?, ?, ?)"
}