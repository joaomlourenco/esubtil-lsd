package client;

import lsd.jdbc.LSDResultSet;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class jTPCCTLSDData extends jTPCCTData {

    public void execute(Logger log, jTPCCConnection db) throws Exception {
        transStart = System.currentTimeMillis();
        if (transDue == 0)
            transDue = transStart;

        switch (transType) {
            case TT_NEW_ORDER:
                if (useStoredProcedures) {
                    switch (dbType) {
                        case jTPCCConfig.DB_POSTGRES:
                            executeNewOrderStoredProcPostgres(log, db);
                            break;

                        case jTPCCConfig.DB_ORACLE:
                            executeNewOrderStoredProcOracle(log, db);
                            break;

                        default:
                            throw new Exception("Stored Procedure for NEW_ORDER not implemented");
                    }
                } else {
                    executeNewOrder(log, (jTPCCLSDConnection) db);
                }
                break;

            case TT_PAYMENT:
                if (useStoredProcedures) {
                    switch (dbType) {
                        case jTPCCConfig.DB_POSTGRES:
                            executePaymentStoredProcPostgres(log, db);
                            break;

                        case jTPCCConfig.DB_ORACLE:
                            executePaymentStoredProcOracle(log, db);
                            break;

                        default:
                            throw new Exception("Stored Procedure for PAYMENT not implemented");
                    }
                } else {
                    executePayment(log, db);
                }
                break;

            case TT_ORDER_STATUS:
                if (useStoredProcedures) {
                    switch (dbType) {
                        case jTPCCConfig.DB_POSTGRES:
                            executeOrderStatusStoredProcPostgres(log, db);
                            break;

                        case jTPCCConfig.DB_ORACLE:
                            executeOrderStatusStoredProcOracle(log, db);
                            break;

                        default:
                            throw new Exception("Stored Procedure for ORDER_STATUS not implemented");
                    }
                } else {
                    executeOrderStatus(log, db);
                }
                break;

            case TT_STOCK_LEVEL:
                if (useStoredProcedures) {
                    switch (dbType) {
                        case jTPCCConfig.DB_POSTGRES:
                            executeStockLevelStoredProcPostgres(log, db);
                            break;

                        case jTPCCConfig.DB_ORACLE:
                            executeStockLevelStoredProcOracle(log, db);
                            break;

                        default:
                            throw new Exception("Stored Procedure for STOCK_LEVEL not implemented");
                    }
                } else {
                    executeStockLevel(log, db);
                }

                break;

            case TT_DELIVERY:
                executeDelivery(log, db);
                break;

            case TT_DELIVERY_BG:
                if (useStoredProcedures) {
                    switch (dbType) {
                        case jTPCCConfig.DB_POSTGRES:
                            executeDeliveryBGStoredProcPostgres(log, db);
                            break;

                        case jTPCCConfig.DB_ORACLE:
                            executeDeliveryBGStoredProcOracle(log, db);
                            break;

                        default:
                            throw new Exception("Stored Procedure for DELIVERY_BG not implemented");
                    }
                } else {
                    executeDeliveryBG(log, db);
                }

                break;

            default:
                throw new Exception("Unknown transType " + transType);
        }

        transEnd = System.currentTimeMillis();
    }

    /* TODO first run selects with no dependencies
        then run select_lsds with (maybe) update_lsds
        and finally insert_lsds
     */
    protected void executeNewOrder(Logger log, jTPCCLSDConnection db) throws Exception {
        PreparedStatement stmt;
        ResultSet rs;

        int o_id;
        int o_all_local = 1;
        long o_entry_d;
        int ol_cnt;
        double total_amount = 0.0;

        int[] ol_seq = new int[15];
        String[] s_quantity_futures = new String[15]; // probably doesnt need to be an array
        String[] s_ytd_futures = new String[15]; // probably doesnt need to be an array
        String[] s_order_cnt_futures = new String[15]; // probably doesnt need to be an array
        String[] s_remote_cnt_futures = new String[15]; // probably doesnt need to be an array

        // The o_entry_d is now.
        o_entry_d = System.currentTimeMillis();
        newOrder.o_entry_d = new Timestamp(o_entry_d).toString();

        /*
         * When processing the order lines we must select the STOCK rows
         * FOR UPDATE. This is because we must perform business logic
         * (the juggling with the S_QUANTITY) here in the application
         * and cannot do that in an atomic UPDATE statement while getting
         * the original value back at the same time (UPDATE ... RETURNING
         * may not be vendor neutral). This can lead to possible deadlocks
         * if two transactions try to lock the same two stock rows in
         * opposite order. To avoid that we process the order lines in
         * the order of the order of ol_supply_w_id, ol_i_id.
         */
        for (ol_cnt = 0; ol_cnt < 15 && newOrder.ol_i_id[ol_cnt] != 0; ol_cnt++) {
            ol_seq[ol_cnt] = ol_cnt;

            // While looping we also determine o_all_local.
            if (newOrder.ol_supply_w_id[ol_cnt] != newOrder.w_id)
                o_all_local = 0;
        }

        for (int x = 0; x < ol_cnt - 1; x++) {
            for (int y = x + 1; y < ol_cnt; y++) {
                if (newOrder.ol_supply_w_id[ol_seq[y]] < newOrder.ol_supply_w_id[ol_seq[x]]) {
                    int tmp = ol_seq[x];
                    ol_seq[x] = ol_seq[y];
                    ol_seq[y] = tmp;
                } else if (newOrder.ol_supply_w_id[ol_seq[y]] == newOrder.ol_supply_w_id[ol_seq[x]] &&
                        newOrder.ol_i_id[ol_seq[y]] < newOrder.ol_i_id[ol_seq[x]]) {
                    int tmp = ol_seq[x];
                    ol_seq[x] = ol_seq[y];
                    ol_seq[y] = tmp;
                }
            }
        }

        // The above also provided the output value for o_ol_cnt;
        newOrder.o_ol_cnt = ol_cnt;

        try {
            // Get District Tax
            stmt = db.stmtNewOrderSelectDistTax;
            stmt.setInt(1, newOrder.w_id);
            stmt.setInt(2, newOrder.d_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("District for" +
                        " W_ID=" + newOrder.w_id +
                        " D_ID=" + newOrder.d_id + " not found");
            }
            newOrder.d_tax = rs.getDouble("d_tax");
            rs.close();

            // Get Future Next Order ID
            stmt = db.stmtNewOrderSelectDistNextOID;
            stmt.setInt(1, newOrder.w_id);
            stmt.setInt(2, newOrder.d_id);
            rs = stmt.executeQuery();
            String o_id_future = ((LSDResultSet) (rs)).getFuture().getId();
            rs.close();

            // Retrieve the required data from CUSTOMER and WAREHOUSE
            stmt = db.stmtNewOrderSelectWhseCust;
            stmt.setInt(1, newOrder.w_id);
            stmt.setInt(2, newOrder.d_id);
            stmt.setInt(3, newOrder.c_id);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                rs.close();
                throw new SQLException("Warehouse or Customer for" +
                        " W_ID=" + newOrder.w_id +
                        " D_ID=" + newOrder.d_id +
                        " C_ID=" + newOrder.c_id + " not found");
            }
            newOrder.w_tax = rs.getDouble("w_tax");
            newOrder.c_last = rs.getString("c_last");
            newOrder.c_credit = rs.getString("c_credit");
            newOrder.c_discount = rs.getDouble("c_discount");
            rs.close();

            // Issue Update for Next Order ID
            stmt = db.stmtNewOrderUpdateDist;
            stmt.setString(1, o_id_future);
            stmt.setInt(2, newOrder.w_id);
            stmt.setInt(3, newOrder.d_id);
            stmt.executeUpdate();

            // Insert the ORDER row
            stmt = db.stmtNewOrderInsertOrder;
            stmt.setString(1, o_id_future);
            stmt.setInt(2, newOrder.d_id);
            stmt.setInt(3, newOrder.w_id);
            stmt.setInt(4, newOrder.c_id);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(6, ol_cnt);
            stmt.setInt(7, o_all_local);
            stmt.executeUpdate();

            // Insert the NEW_ORDER row
            stmt = db.stmtNewOrderInsertNewOrder;
            stmt.setString(1, o_id_future);
            stmt.setInt(2, newOrder.d_id);
            stmt.setInt(3, newOrder.w_id);
            stmt.executeUpdate();

            // Per ORDER_LINE
            for (int i = 0; i < ol_cnt; i++) {
                int ol_number = i + 1;
                int seq = ol_seq[i];
                String i_data;

                stmt = db.stmtNewOrderSelectItem;
                stmt.setInt(1, newOrder.ol_i_id[seq]);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    rs.close();

                    /*
                     * 1% of NEW_ORDER transactions use an unused item
                     * in the last line to simulate user entry errors.
                     * Make sure this is precisely that case.
                     */

                    if (transRbk && (newOrder.ol_i_id[seq] < 1 ||
                            newOrder.ol_i_id[seq] > 100000)) {

                        db.rollback();

                        newOrder.total_amount = total_amount;
                        newOrder.execution_status = new String(
                                "Item number is not valid");
                        return;
                    }

                    // This ITEM should have been there.
                    throw new Exception("ITEM " + newOrder.ol_i_id[seq] +
                            " not fount");
                }
                // Found ITEM
                newOrder.i_name[seq] = rs.getString("i_name");
                newOrder.i_price[seq] = rs.getDouble("i_price");
                i_data = rs.getString("i_data");
                rs.close();

                // Select STOCK for update.
                // First, get the quantity of the stock
                stmt = db.stmtNewOrderSelectStockQty;
                stmt.setInt(1, newOrder.ol_supply_w_id[seq]);
                stmt.setInt(2, newOrder.ol_i_id[seq]);
                rs = stmt.executeQuery();
                s_quantity_futures[seq] = ((LSDResultSet) (rs)).getFuture().getId();
                rs.close();

                // Then, get the ytd of the stock
                stmt = db.stmtNewOrderSelectStockYtd;
                stmt.setInt(1, newOrder.ol_supply_w_id[seq]);
                stmt.setInt(2, newOrder.ol_i_id[seq]);
                rs = stmt.executeQuery();
                s_ytd_futures[seq] = ((LSDResultSet) (rs)).getFuture().getId();
                rs.close();

                // Then, get the order count of the stock
                stmt = db.stmtNewOrderSelectStockOCount;
                stmt.setInt(1, newOrder.ol_supply_w_id[seq]);
                stmt.setInt(2, newOrder.ol_i_id[seq]);
                rs = stmt.executeQuery();
                s_order_cnt_futures[seq] = ((LSDResultSet) (rs)).getFuture().getId();
                rs.close();

                // Then, get the remote count of the stock
                stmt = db.stmtNewOrderSelectStockRCount;
                stmt.setInt(1, newOrder.ol_supply_w_id[seq]);
                stmt.setInt(2, newOrder.ol_i_id[seq]);
                rs = stmt.executeQuery();
                s_remote_cnt_futures[seq] = ((LSDResultSet) (rs)).getFuture().getId();
                rs.close();

                // Finally, get the rest of the information
                stmt = db.stmtNewOrderSelectStock;
                stmt.setInt(1, newOrder.ol_supply_w_id[seq]);
                stmt.setInt(2, newOrder.ol_i_id[seq]);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw new Exception("STOCK with" +
                            " S_W_ID=" + newOrder.ol_supply_w_id[seq] +
                            " S_I_ID=" + newOrder.ol_i_id[seq] +
                            " not fount");
                }

                // Leave the ResultSet open ... we need it for the s_dist_NN.

                newOrder.ol_amount[seq] = newOrder.i_price[seq] * newOrder.ol_quantity[seq];
                if (i_data.contains("ORIGINAL") &&
                        rs.getString("s_data").contains("ORIGINAL"))
                    newOrder.brand_generic[seq] = "B";
                else
                    newOrder.brand_generic[seq] = "G";

                total_amount += newOrder.ol_amount[seq] *
                        (1.0 - newOrder.c_discount) *
                        (1.0 + newOrder.w_tax + newOrder.d_tax);

                // Update the STOCK row.
                PreparedStatement updateStock = db.createStmtNewOrderIsTrueQuantity();

                // Checking if order needs remote stock
                int s_remote_add = 0;
                if (newOrder.ol_supply_w_id[seq] != newOrder.w_id) {
                    s_remote_add = 1;
                }

                // IS-TRUE parameters -> IS-TRUE ?1 > ?2 + 10
                updateStock.setString(1, s_quantity_futures[seq]);
                updateStock.setInt(2, newOrder.ol_quantity[seq]);

                /*
                 * First UPDATE_LSD parameters
                 *
                 * UPDATE_LSD bmsql_stock
                 *  SET s_quantity = ?3 - ?4, s_ytd = ?5 + ?6,
                 *      s_order_cnt = ?7 + 1,
                 *      s_remote_cnt = ?8 + ?9
                 *  WHERE s_w_id = ?10 AND s_i_id = ?11
                 */
                updateStock.setString(3, s_quantity_futures[seq]);
                updateStock.setInt(4, newOrder.ol_quantity[seq]);
                updateStock.setString(5, s_ytd_futures[seq]);
                updateStock.setInt(6, newOrder.ol_quantity[seq]);
                updateStock.setString(7, s_order_cnt_futures[seq]);
                updateStock.setString(8, s_remote_cnt_futures[seq]);
                updateStock.setInt(9, s_remote_add);
                updateStock.setInt(10, newOrder.ol_supply_w_id[seq]);
                updateStock.setInt(11, newOrder.ol_i_id[seq]);

                /*
                 * Second UPDATE_LSD parameters
                 *
                 * UPDATE_LSD bmsql_stock
                 *  SET s_quantity = ?12 + 91, s_ytd = ?13 + ?14,
                 *      s_order_cnt = ?15 + 1,
                 *      s_remote_cnt = ?16 + ?17
                 *  WHERE s_w_id = ?18 AND s_i_id = ?19"
                 */
                updateStock.setString(12, s_quantity_futures[seq]);
                updateStock.setString(13, s_ytd_futures[seq]);
                updateStock.setInt(14, newOrder.ol_quantity[seq]);
                updateStock.setString(15, s_order_cnt_futures[seq]);
                updateStock.setString(16, s_remote_cnt_futures[seq]);
                updateStock.setInt(17, s_remote_add);
                updateStock.setInt(18, newOrder.ol_supply_w_id[seq]);
                updateStock.setInt(19, newOrder.ol_i_id[seq]);

                /*
                    Insert the ORDER_LINE row.
                    INSERT_LSD INTO bmsql_order_line (
                        ol_o_id,
                        ol_d_id,
                        ol_w_id,
                        ol_number,
                        ol_i_id,
                        ol_supply_w_id,
                        ol_quantity,
                        ol_amount,
                        ol_dist_info
                      )
                      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                 */
                /*log.info("VALUES (" +
                        o_id_future + ", " +
                        newOrder.d_id + ", " +
                        newOrder.w_id + ", " +
                        seq + 1 + ", " +
                        newOrder.ol_i_id[seq] + ", " +
                        newOrder.ol_supply_w_id[seq] + ", " +
                        newOrder.ol_quantity[seq] + ", " +
                        newOrder.ol_amount[seq] + ", " +
                        newOrder.d_id + ")");*/
                PreparedStatement insertNewOrder = db.createStmtNewOrderInsertOrderLine();
                insertNewOrder.setString(1, o_id_future);
                insertNewOrder.setInt(2, newOrder.d_id);
                insertNewOrder.setInt(3, newOrder.w_id);
                insertNewOrder.setInt(4, seq + 1);
                insertNewOrder.setInt(5, newOrder.ol_i_id[seq]);
                insertNewOrder.setInt(6, newOrder.ol_supply_w_id[seq]);
                insertNewOrder.setInt(7, newOrder.ol_quantity[seq]);
                insertNewOrder.setDouble(8, newOrder.ol_amount[seq]);
                switch (newOrder.d_id) {
                    case 1:
                        insertNewOrder.setString(9, rs.getString("s_dist_01"));
                        break;
                    case 2:
                        insertNewOrder.setString(9, rs.getString("s_dist_02"));
                        break;
                    case 3:
                        insertNewOrder.setString(9, rs.getString("s_dist_03"));
                        break;
                    case 4:
                        insertNewOrder.setString(9, rs.getString("s_dist_04"));
                        break;
                    case 5:
                        insertNewOrder.setString(9, rs.getString("s_dist_05"));
                        break;
                    case 6:
                        insertNewOrder.setString(9, rs.getString("s_dist_06"));
                        break;
                    case 7:
                        insertNewOrder.setString(9, rs.getString("s_dist_07"));
                        break;
                    case 8:
                        insertNewOrder.setString(9, rs.getString("s_dist_08"));
                        break;
                    case 9:
                        insertNewOrder.setString(9, rs.getString("s_dist_09"));
                        break;
                    case 10:
                        insertNewOrder.setString(9, rs.getString("s_dist_10"));
                        break;
                }

                updateStock.executeUpdate(); // update stocks
                insertNewOrder.executeUpdate(); // insert new order
                updateStock.close();
                insertNewOrder.close();
            }

            db.commit();
        } catch (SQLException se) {
            log.error("Unexpected SQLException in NEW_ORDER");
            for (SQLException x = se; x != null; x = x.getNextException())
                log.error(x.getMessage());
            se.printStackTrace();

            System.exit(1); // TODO temporary

            try {
                db.stmtNewOrderUpdateStock.clearBatch();
                db.stmtNewOrderInsertOrderLine.clearBatch();
                db.rollback();
            } catch (SQLException se2) {
                throw new Exception("Unexpected SQLException on rollback: " +
                        se2.getMessage());
            }
        } catch (Exception e) {
            try {
                db.stmtNewOrderUpdateStock.clearBatch();
                db.stmtNewOrderInsertOrderLine.clearBatch();
                db.rollback();
            } catch (SQLException se2) {
                throw new Exception("Unexpected SQLException on rollback: " +
                        se2.getMessage());
            }
            throw e;
        }

    }
    // STANDARD WAY OF DOING NEW ORDER

    /*// Retrieve the required data from DISTRICT
    stmt = db.stmtNewOrderSelectDist;
            stmt.setInt(1, newOrder.w_id);
            stmt.setInt(2, newOrder.d_id);
    rs = stmt.executeQuery();
            if (!rs.next()) {
        rs.close();
        throw new SQLException("District for" +
                " W_ID=" + newOrder.w_id +
                " D_ID=" + newOrder.d_id + " not found");
    }
    newOrder.d_tax = rs.getDouble("d_tax");
    newOrder.o_id = rs.getInt("d_next_o_id");
    o_id = newOrder.o_id + 1;
            rs.close();

    // Retrieve the required data from CUSTOMER and WAREHOUSE
    stmt = db.stmtNewOrderSelectWhseCust;
            stmt.setInt(1, newOrder.w_id);
            stmt.setInt(2, newOrder.d_id);
            stmt.setInt(3, newOrder.c_id);
    rs = stmt.executeQuery();
            if (!rs.next()) {
        rs.close();
        throw new SQLException("Warehouse or Customer for" +
                " W_ID=" + newOrder.w_id +
                " D_ID=" + newOrder.d_id +
                " C_ID=" + newOrder.c_id + " not found");
    }
    newOrder.w_tax = rs.getDouble("w_tax");
    newOrder.c_last = rs.getString("c_last");
    newOrder.c_credit = rs.getString("c_credit");
    newOrder.c_discount = rs.getDouble("c_discount");
            rs.close();

    // Update the DISTRICT bumping the D_NEXT_O_ID
    stmt = db.stmtNewOrderUpdateDist;
            stmt.setInt(1, newOrder.w_id);
            stmt.setInt(2, newOrder.d_id);
            stmt.executeUpdate();

    // Insert the ORDER row
    stmt = db.stmtNewOrderInsertOrder;
            stmt.setInt(1, o_id);
            stmt.setInt(2, newOrder.d_id);
            stmt.setInt(3, newOrder.w_id);
            stmt.setInt(4, newOrder.c_id);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(6, ol_cnt);
            stmt.setInt(7, o_all_local);
            stmt.executeUpdate();

    // Insert the NEW_ORDER row
    stmt = db.stmtNewOrderInsertNewOrder;
            stmt.setInt(1, o_id);
            stmt.setInt(2, newOrder.d_id);
            stmt.setInt(3, newOrder.w_id);
            stmt.executeUpdate();

    // Per ORDER_LINE
    insertOrderLineBatch = db.stmtNewOrderInsertOrderLine;
    updateStockBatch = db.stmtNewOrderUpdateStock;
            for (int i = 0; i < ol_cnt; i++) {
        int seq = ol_seq[i];
        String i_data;

        stmt = db.stmtNewOrderSelectItem;
        stmt.setInt(1, newOrder.ol_i_id[seq]);
        rs = stmt.executeQuery();
        if (!rs.next()) {
            rs.close();

            *//*
     * 1% of NEW_ORDER transactions use an unused item
     * in the last line to simulate user entry errors.
     * Make sure this is precisely that case.
     *//*
            if (transRbk && (newOrder.ol_i_id[seq] < 1 ||
                    newOrder.ol_i_id[seq] > 100000)) {
                *//*
     * Clause 2.4.2.3 mandates that the entire
     * transaction profile up to here must be executed
     * before we can roll back, except for retrieving
     * the missing STOCK row and inserting this
     * ORDER_LINE row. Note that we haven't updated
     * STOCK rows or inserted any ORDER_LINE rows so
     * far, we only batched them up. So we must do
     * that now in order to satisfy 2.4.2.3.
     *//*
                insertOrderLineBatch.executeBatch();
                insertOrderLineBatch.clearBatch();
                updateStockBatch.executeBatch();
                updateStockBatch.clearBatch();

                db.rollback();

                newOrder.total_amount = total_amount;
                newOrder.execution_status = "Item number is not valid";
                return;
            }

            // This ITEM should have been there.
            throw new Exception("ITEM " + newOrder.ol_i_id[seq] +
                    " not fount");
        }
        // Found ITEM
        newOrder.i_name[seq] = rs.getString("i_name");
        newOrder.i_price[seq] = rs.getDouble("i_price");
        i_data = rs.getString("i_data");
        rs.close();

        // Select STOCK for update.
        stmt = db.stmtNewOrderSelectStock;
        stmt.setInt(1, newOrder.ol_supply_w_id[seq]);
        stmt.setInt(2, newOrder.ol_i_id[seq]);
        rs = stmt.executeQuery();
        if (!rs.next()) {
            throw new Exception("STOCK with" +
                    " S_W_ID=" + newOrder.ol_supply_w_id[seq] +
                    " S_I_ID=" + newOrder.ol_i_id[seq] +
                    " not fount");
        }
        newOrder.s_quantity[seq] = rs.getInt("s_quantity");
        // Leave the ResultSet open ... we need it for the s_dist_NN.

        newOrder.ol_amount[seq] = newOrder.i_price[seq] * newOrder.ol_quantity[seq];
        if (i_data.contains("ORIGINAL") &&
                rs.getString("s_data").contains("ORIGINAL"))
            newOrder.brand_generic[seq] = "B";
        else
            newOrder.brand_generic[seq] = "G";

        total_amount += newOrder.ol_amount[seq] *
                (1.0 - newOrder.c_discount) *
                (1.0 + newOrder.w_tax + newOrder.d_tax);

        // Update the STOCK row.
        if (newOrder.s_quantity[seq] >= newOrder.ol_quantity[seq] + 10)
            updateStockBatch.setInt(1, newOrder.s_quantity[seq] -
                    newOrder.ol_quantity[seq]);
        else
            updateStockBatch.setInt(1, newOrder.s_quantity[seq] + 91);
        updateStockBatch.setInt(2, newOrder.ol_quantity[seq]);
        if (newOrder.ol_supply_w_id[seq] == newOrder.w_id)
            updateStockBatch.setInt(3, 0);
        else
            updateStockBatch.setInt(3, 1);
        updateStockBatch.setInt(4, newOrder.ol_supply_w_id[seq]);
        updateStockBatch.setInt(5, newOrder.ol_i_id[seq]);
        updateStockBatch.addBatch();

        // Insert the ORDER_LINE row.
        insertOrderLineBatch.setInt(1, o_id);
        insertOrderLineBatch.setInt(2, newOrder.d_id);
        insertOrderLineBatch.setInt(3, newOrder.w_id);
        insertOrderLineBatch.setInt(4, seq + 1);
        insertOrderLineBatch.setInt(5, newOrder.ol_i_id[seq]);
        insertOrderLineBatch.setInt(6, newOrder.ol_supply_w_id[seq]);
        insertOrderLineBatch.setInt(7, newOrder.ol_quantity[seq]);
        insertOrderLineBatch.setDouble(8, newOrder.ol_amount[seq]);
        switch (newOrder.d_id) {
            case 1:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_01"));
                break;
            case 2:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_02"));
                break;
            case 3:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_03"));
                break;
            case 4:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_04"));
                break;
            case 5:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_05"));
                break;
            case 6:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_06"));
                break;
            case 7:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_07"));
                break;
            case 8:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_08"));
                break;
            case 9:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_09"));
                break;
            case 10:
                insertOrderLineBatch.setString(9, rs.getString("s_dist_10"));
                break;
        }
        insertOrderLineBatch.addBatch();
    }
            rs.close();

    // All done ... execute the batches.
            updateStockBatch.executeBatch();
            updateStockBatch.clearBatch();
            insertOrderLineBatch.executeBatch();
            insertOrderLineBatch.clearBatch();

    newOrder.execution_status = "Order placed";
    newOrder.total_amount = total_amount;*/
}
