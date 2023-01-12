package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class jTPCCLSDConnection extends jTPCCConnection {

    public PreparedStatement stmtNewOrderSelectDistTax;
    public PreparedStatement stmtNewOrderSelectDistNextOID;

    public PreparedStatement stmtNewOrderSelectStockQty;
    public PreparedStatement stmtNewOrderSelectStockYtd;
    public PreparedStatement stmtNewOrderSelectStockOCount;
    public PreparedStatement stmtNewOrderSelectStockRCount;

    public String stmtNewOrderIsTrueQuantityString;
    public String stmtNewOrderUpdateStockThen;
    public String stmtNewOrderUpdateStockElse;

    public String stmtNewOrderInsertOrderLineString;

    private Connection dbConn;

    public jTPCCLSDConnection(Connection dbConn, int dbType) throws SQLException {
        super(dbConn, dbType);

        this.dbConn = dbConn;

        // PreparedStataments for NEW_ORDER
        stmtNewOrderSelectDistTax = dbConn.prepareStatement(
                "SELECT d_tax " +
                        "    FROM bmsql_district " +
                        "    WHERE d_w_id = ? AND d_id = ? " +
                        "    FOR UPDATE");
        stmtNewOrderSelectDistNextOID = dbConn.prepareStatement( // this should probably be LSD
                "SELECT_LSD d_next_o_id " +
                        "    FROM bmsql_district " +
                        "    WHERE d_w_id = ? AND d_id = ? " +
                        "    FOR UPDATE");

        stmtNewOrderSelectStockQty = dbConn.prepareStatement( // this should probably be LSD
                "SELECT_LSD s_quantity " +
                        "    FROM bmsql_stock " +
                        "    WHERE s_w_id = ? AND s_i_id = ? " +
                        "    FOR UPDATE");
        stmtNewOrderSelectStockYtd = dbConn.prepareStatement( // this should probably be LSD
                "SELECT_LSD s_ytd " +
                        "    FROM bmsql_stock " +
                        "    WHERE s_w_id = ? AND s_i_id = ?");
        stmtNewOrderSelectStockOCount = dbConn.prepareStatement( // this should probably be LSD
                "SELECT_LSD s_order_cnt " +
                        "    FROM bmsql_stock " +
                        "    WHERE s_w_id = ? AND s_i_id = ?");
        stmtNewOrderSelectStockRCount = dbConn.prepareStatement( // this should probably be LSD
                "SELECT_LSD s_remote_cnt " +
                        "    FROM bmsql_stock " +
                        "    WHERE s_w_id = ? AND s_i_id = ?");
        stmtNewOrderSelectStock = dbConn.prepareStatement(
                "SELECT s_data, " +
                        "       s_dist_01, s_dist_02, s_dist_03, s_dist_04, " +
                        "       s_dist_05, s_dist_06, s_dist_07, s_dist_08, " +
                        "       s_dist_09, s_dist_10 " +
                        "    FROM bmsql_stock " +
                        "    WHERE s_w_id = ? AND s_i_id = ? " +
                        "    FOR UPDATE");

        stmtNewOrderUpdateDist = dbConn.prepareStatement( // this should be LSD, also SET should received specific values
                "UPDATE_LSD bmsql_district " +
                        "    SET d_next_o_id = ? + 1 " + // bmsql_district:d_next_o_id + 1
                        "    WHERE d_w_id = ? AND d_id = ?");


        stmtNewOrderInsertOrder = dbConn.prepareStatement( // this should be LSD
                "INSERT_LSD INTO bmsql_oorder (" +
                        "    o_id, o_d_id, o_w_id, o_c_id, o_entry_d, " +
                        "    o_ol_cnt, o_all_local) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)");

        stmtNewOrderInsertNewOrder = dbConn.prepareStatement( // this should be LSD
                "INSERT_LSD INTO bmsql_new_order (" +
                        "    no_o_id, no_d_id, no_w_id) " +
                        "VALUES (?, ?, ?)");

        // updateStock.setInt(1, newOrder.s_quantity[seq] - newOrder.ol_quantity[seq])
        stmtNewOrderUpdateStockThen =  // this should be LSD, also SET should received specific values
                "UPDATE_LSD bmsql_stock " +
                        "    SET s_quantity = ? - ?, s_ytd = ? + ?, " +// get future bmsql_stock:s_ytd + ?
                        "        s_order_cnt = ? + 1, " +// get future bmsql_stock:s_order_cnt + ?
                        "        s_remote_cnt = ? + ? " +// get future bmsql_stock:s_remote_cnt + ?
                        "    WHERE s_w_id = ? AND s_i_id = ?";
        // updateStock.setInt(1, newOrder.s_quantity[seq] + 91)
        stmtNewOrderUpdateStockElse = // this should be LSD, also SET should received specific values
                "UPDATE_LSD bmsql_stock " +
                        "    SET s_quantity = ? + 91, s_ytd = ? + ?, " +// get future bmsql_stock:s_ytd + ?
                        "        s_order_cnt = ? + 1, " +// get future bmsql_stock:s_order_cnt + ?
                        "        s_remote_cnt = ? + ? " +// get future bmsql_stock:s_remote_cnt + ?
                        "    WHERE s_w_id = ? AND s_i_id = ?";
        // if (newOrder.s_quantity[seq] >= newOrder.ol_quantity[seq] + 10)
        stmtNewOrderIsTrueQuantityString = // this should be LSD, also SET should received specific values
                "IF_LSD " +
                        "IS-TRUE ? > ? + 10;;" +
                        stmtNewOrderUpdateStockThen + ";;" +
                        stmtNewOrderUpdateStockElse;

        stmtNewOrderInsertOrderLineString =   // this should be LSD
                "INSERT_LSD INTO bmsql_order_line (" +
                        "    ol_o_id, ol_d_id, ol_w_id, ol_number, " +
                        "    ol_i_id, ol_supply_w_id, ol_quantity, " +
                        "    ol_amount, ol_dist_info) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    }

    public PreparedStatement createStmtNewOrderIsTrueQuantity() throws SQLException {
        return dbConn.prepareStatement(stmtNewOrderIsTrueQuantityString);
    }

    public PreparedStatement createStmtNewOrderInsertOrderLine() throws SQLException {
        return dbConn.prepareStatement(stmtNewOrderInsertOrderLineString);
    }
}
