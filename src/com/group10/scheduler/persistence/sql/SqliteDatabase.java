package com.group10.scheduler.persistence.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the SQLite database file: hands out JDBC connections and creates the
 * schema on first use. Every Sqlite*Repository shares one instance, so the
 * whole application lives in a single .db file.
 */
public class SqliteDatabase {

    private static final String[] SCHEMA = {
        "CREATE TABLE IF NOT EXISTS rooms ("
            + " roomId     TEXT PRIMARY KEY,"
            + " capacity   INTEGER NOT NULL,"
            + " building   TEXT,"
            + " roomNumber TEXT,"
            + " status     TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS users ("
            + " email          TEXT PRIMARY KEY,"
            + " accountType    TEXT NOT NULL,"
            + " password       TEXT,"
            + " userName       TEXT,"
            + " organizationId INTEGER NOT NULL DEFAULT 0)",
        "CREATE TABLE IF NOT EXISTS bookings ("
            + " bookingId     TEXT PRIMARY KEY,"
            + " userEmail     TEXT NOT NULL,"
            + " roomId        TEXT NOT NULL,"
            + " startTime     TEXT NOT NULL,"
            + " endTime       TEXT NOT NULL,"
            + " depositAmount REAL NOT NULL,"
            + " checkInTime   TEXT,"
            + " status        TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS payments ("
            + " paymentId   TEXT PRIMARY KEY,"
            + " bookingId   TEXT NOT NULL,"
            + " amount      REAL NOT NULL,"
            + " method      TEXT NOT NULL,"
            + " status      TEXT NOT NULL,"
            + " paymentDate TEXT)",
        "CREATE INDEX IF NOT EXISTS idx_bookings_room ON bookings(roomId)",
        "CREATE INDEX IF NOT EXISTS idx_bookings_user ON bookings(userEmail)",
        "CREATE INDEX IF NOT EXISTS idx_payments_booking ON payments(bookingId)"
    };

    private final String url;

    public SqliteDatabase(String dbFilePath) {
        this.url = "jdbc:sqlite:" + dbFilePath;
        createSchema();
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void createSchema() {
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            for (String ddl : SCHEMA) {
                st.execute(ddl);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database " + url, e);
        }
    }

    /** Runs a write in one transaction so a failed save never leaves a half-written table. */
    void inTransaction(String table, SqlWork work) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                work.run(conn);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save " + table, e);
        }
    }

    @FunctionalInterface
    interface SqlWork {
        void run(Connection conn) throws SQLException;
    }
}
