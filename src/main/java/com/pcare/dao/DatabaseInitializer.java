
package com.pcare.dao;

import com.pcare.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() {
        DatabaseConnection.initializeDatabase();
        migrateActivityLogsTable();
    }

    private void migrateActivityLogsTable() {

        String checkSql = "PRAGMA table_info(Activity_Logs)";

        try (Connection connection = DatabaseConnection.connect()) {

            boolean alreadyUpdated = false;

            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery(checkSql)) {

                boolean hasActionType = false;
                boolean hasDescription = false;
                boolean hasTimestamp = false;

                while (result.next()) {
                    String column = result.getString("name");

                    if ("action_type".equalsIgnoreCase(column)) {
                        hasActionType = true;
                    }

                    if ("description".equalsIgnoreCase(column)) {
                        hasDescription = true;
                    }

                    if ("timestamp".equalsIgnoreCase(column)) {
                        hasTimestamp = true;
                    }
                }

                alreadyUpdated = hasActionType
                        && hasDescription
                        && hasTimestamp;
            }

            if (alreadyUpdated) {
                System.out.println("Activity logs table is already updated.");
                return;
            }

            connection.setAutoCommit(false);

            try (Statement statement = connection.createStatement()) {

                statement.execute("""
                        DROP TABLE IF EXISTS Activity_Logs_New
                        """);

                statement.execute("""
                        CREATE TABLE Activity_Logs_New (
                            log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            user_id INTEGER,
                            action_type TEXT NOT NULL,
                            description TEXT NOT NULL,
                            timestamp TEXT NOT NULL,
                            FOREIGN KEY (user_id)
                                REFERENCES Users(user_id)
                                ON DELETE SET NULL
                        )
                        """);

                statement.execute("""
                        INSERT INTO Activity_Logs_New
                            (log_id, user_id, action_type,
                             description, timestamp)
                        SELECT
                            log_id,
                            user_id,
                            'LEGACY',
                            action,
                            date_time
                        FROM Activity_Logs
                        """);

                statement.execute("""
                        DROP TABLE Activity_Logs
                        """);

                statement.execute("""
                        ALTER TABLE Activity_Logs_New
                        RENAME TO Activity_Logs
                        """);

                connection.commit();

                System.out.println(
                        "Activity logs table migrated successfully!"
                );

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println(
                    "Activity logs migration error: " + e.getMessage()
            );

            throw new IllegalStateException(
                    "Could not migrate the activity logs table.", e
            );
        }
    }
}
