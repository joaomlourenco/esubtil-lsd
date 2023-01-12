package client;/*
 * jTPCCTerminal - Terminal emulator code for jTPCC (transactions)
 *
 * Copyright (C) 2003, Raul Barbosa
 * Copyright (C) 2004-2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2020-2021, Eduardo Subtil
 *
 */

import lsd.jdbc.LSDConnection;
import org.apache.log4j.*;

import java.io.*;
import java.sql.*;


public class jTPCCTerminal implements jTPCCConfig, Runnable {
    private static final Logger log = Logger.getLogger(jTPCCTerminal.class);


    private final String terminalName;
    private final Connection conn;
    private final Statement stmt;
    private int terminalWarehouseID;
    private final int terminalDistrictID;
    private final boolean terminalWarehouseFixed;
    private final boolean useStoredProcedures;
    private final double paymentWeight;
    private final double orderStatusWeight;
    private final double deliveryWeight;
    private final double stockLevelWeight;
    private final int limPerMin_Terminal;
    private final jTPCC parent;
    private final jTPCCRandom rnd;

    private final int numTransactions;
    private final int numWarehouses;
    private int newOrderCounter;
    private boolean stopRunningSignal = false;
    private boolean lsdEnable = false;

    long terminalStartTime;

    jTPCCConnection db;
    int dbType;

    public jTPCCTerminal(
            String terminalName, int terminalWarehouseID, int terminalDistrictID,
            Connection conn, int dbType,
            int numTransactions, boolean terminalWarehouseFixed,
            boolean useStoredProcedures,
            double paymentWeight, double orderStatusWeight,
            double deliveryWeight, double stockLevelWeight,
            int numWarehouses, int limPerMin_Terminal, jTPCC parent
    ) throws SQLException {

        // TODO lsdEnable variable
        this.lsdEnable = true;
        this.terminalName = terminalName;
        this.conn = conn;
        this.dbType = dbType;
        this.stmt = conn.createStatement();
        this.stmt.setMaxRows(200);
        this.stmt.setFetchSize(100);

        Statement stmt1 = conn.createStatement();
        stmt1.setMaxRows(1);

        this.terminalWarehouseID = terminalWarehouseID;
        this.terminalDistrictID = terminalDistrictID;
        this.terminalWarehouseFixed = terminalWarehouseFixed;
        this.useStoredProcedures = useStoredProcedures;
        this.parent = parent;
        this.rnd = parent.getRnd().newRandom();
        this.numTransactions = numTransactions;
        this.paymentWeight = paymentWeight;
        this.orderStatusWeight = orderStatusWeight;
        this.deliveryWeight = deliveryWeight;
        this.stockLevelWeight = stockLevelWeight;
        this.numWarehouses = numWarehouses;
        this.newOrderCounter = 0;
        this.limPerMin_Terminal = limPerMin_Terminal;

        if (conn instanceof LSDConnection && lsdEnable)
            this.db = new jTPCCLSDConnection(conn, dbType);
        else this.db = new jTPCCConnection(conn, dbType);

        log.info("jTPCC Connection: " + this.db.getClass().getName());

        terminalMessage("");
        terminalMessage("Terminal '" + terminalName + "' has WarehouseID=" + terminalWarehouseID
                + " and DistrictID=" + terminalDistrictID + ".");
        terminalStartTime = System.currentTimeMillis();
    }

    public void run() {
        executeTransactions(numTransactions);
        try {
            printMessage("");
            printMessage("Closing statement and connection...");

            stmt.close();
            conn.close();
        } catch (Exception e) {
            printMessage("");
            printMessage("An error occurred!");
            logException(e);
        }

        parent.signalTerminalEnded(this, newOrderCounter);
    }

    public void stopRunningWhenPossible() {
        stopRunningSignal = true;
        printMessage("");
        printMessage("Terminal received stop signal!");
        printMessage("Finishing current transaction before exit...");
    }

    private void executeTransactions(int numTransactions) {
        boolean stopRunning = false;

        if (numTransactions != -1)
            printMessage("Executing " + numTransactions + " transactions...");
        else
            printMessage("Executing for a limited time...");

        for (int i = 0; (i < numTransactions || numTransactions == -1) && !stopRunning; i++) {

            double transactionType = rnd.nextDouble(0.0, 100.0);
            int skippedDeliveries = 0, newOrder = 0;
            String transactionTypeName;

            long transactionStart = System.currentTimeMillis();

            /*
             * TPC/C specifies that each terminal has a fixed
             * "home" warehouse. However, since this implementation
             * does not simulate "terminals", but rather simulates
             * "application threads", that association is no longer
             * valid. In the case of having less clients than
             * warehouses (which should be the normal case), it
             * leaves the warehouses without a client without any
             * significant traffic, changing the overall database
             * access pattern significantly.
             */
            if (!terminalWarehouseFixed)
                terminalWarehouseID = rnd.nextInt(1, numWarehouses);

            jTPCCTData term;
            if (db instanceof jTPCCLSDConnection && lsdEnable)
                term = new jTPCCTLSDData();
            else
                term = new jTPCCTData();

            if (transactionType <= paymentWeight) {
                term.setNumWarehouses(numWarehouses);
                term.setWarehouse(terminalWarehouseID);
                term.setDistrict(terminalDistrictID);
                term.setUseStoredProcedures(useStoredProcedures);
                term.setDBType(dbType);
                try {
                    term.generatePayment(log, rnd, 0);
                    term.traceScreen(log);
                    term.execute(log, db);
                    parent.resultAppend(term);
                    term.traceScreen(log);
                } catch (Exception e) {
                    log.fatal(e.getMessage());
                    e.printStackTrace();
                    System.exit(4);
                }
                transactionTypeName = "Payment";
            } else if (transactionType <= paymentWeight + stockLevelWeight) {
                term.setNumWarehouses(numWarehouses);
                term.setWarehouse(terminalWarehouseID);
                term.setDistrict(terminalDistrictID);
                term.setUseStoredProcedures(useStoredProcedures);
                term.setDBType(dbType);
                try {
                    term.generateStockLevel(log, rnd, 0);
                    term.traceScreen(log);
                    term.execute(log, db);
                    parent.resultAppend(term);
                    term.traceScreen(log);
                } catch (Exception e) {
                    log.fatal(e.getMessage());
                    e.printStackTrace();
                    System.exit(4);
                }
                transactionTypeName = "Stock-Level";
            } else if (transactionType <= paymentWeight + stockLevelWeight + orderStatusWeight) {
                term.setNumWarehouses(numWarehouses);
                term.setWarehouse(terminalWarehouseID);
                term.setDistrict(terminalDistrictID);
                term.setUseStoredProcedures(useStoredProcedures);
                term.setDBType(dbType);
                try {
                    term.generateOrderStatus(log, rnd, 0);
                    term.traceScreen(log);
                    term.execute(log, db);
                    parent.resultAppend(term);
                    term.traceScreen(log);
                } catch (Exception e) {
                    log.fatal(e.getMessage());
                    e.printStackTrace();
                    System.exit(4);
                }
                transactionTypeName = "Order-Status";
            } else if (transactionType <= paymentWeight + stockLevelWeight + orderStatusWeight + deliveryWeight) {
                term.setNumWarehouses(numWarehouses);
                term.setWarehouse(terminalWarehouseID);
                term.setDistrict(terminalDistrictID);
                term.setUseStoredProcedures(useStoredProcedures);
                term.setDBType(dbType);
                try {
                    term.generateDelivery(log, rnd, 0);
                    term.traceScreen(log);
                    term.execute(log, db);
                    parent.resultAppend(term);
                    term.traceScreen(log);

                    /*
                     * The old style driver does not have a delivery
                     * background queue, so we have to execute that
                     * part here as well.
                     */
                    jTPCCTData bg = term.getDeliveryBG();
                    bg.traceScreen(log);
                    bg.execute(log, db);
                    parent.resultAppend(bg);
                    bg.traceScreen(log);

                    skippedDeliveries = bg.getSkippedDeliveries();
                } catch (Exception e) {
                    log.fatal(e.getMessage());
                    e.printStackTrace();
                    System.exit(4);
                }
                transactionTypeName = "Delivery";
            } else {
                term.setNumWarehouses(numWarehouses);
                term.setWarehouse(terminalWarehouseID);
                term.setDistrict(terminalDistrictID);
                term.setUseStoredProcedures(useStoredProcedures);
                term.setDBType(dbType);
                try {
                    term.generateNewOrder(log, rnd, 0);
                    term.traceScreen(log);
                    term.execute(log, db);
                    parent.resultAppend(term);
                    term.traceScreen(log);
                } catch (Exception e) {
                    log.fatal(e.getMessage());
                    e.printStackTrace();
                    System.exit(4);
                }
                transactionTypeName = "New-Order";
                newOrderCounter++;
                newOrder = 1;
            }

            long transactionEnd = System.currentTimeMillis();

            if (!transactionTypeName.equals("Delivery")) {
                parent.signalTerminalEndedTransaction(this.terminalName, transactionTypeName,
                        transactionEnd - transactionStart, null, newOrder);
            } else {
                parent.signalTerminalEndedTransaction(this.terminalName, transactionTypeName,
                        transactionEnd - transactionStart, (skippedDeliveries == 0 ? "None" : "" +
                                skippedDeliveries + " delivery(ies) skipped."), newOrder);
            }

            if (limPerMin_Terminal > 0) {
                long elapse = transactionEnd - transactionStart;
                long timePerTx = 60000 / limPerMin_Terminal;

                if (elapse < timePerTx) {
                    try {
                        long sleepTime = timePerTx - elapse;
                        Thread.sleep((sleepTime));
                    } catch (Exception ignored) {
                    }
                }
            }
            if (stopRunningSignal) stopRunning = true;
        }
    }

    private void logException(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        log.error(stringWriter.toString());
    }


    private void terminalMessage(String message) {
        log.trace(terminalName + ", " + message);
    }


    private void printMessage(String message) {
        log.trace(terminalName + ", " + message);
    }
}
