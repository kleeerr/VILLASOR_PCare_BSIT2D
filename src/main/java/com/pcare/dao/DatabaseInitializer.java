package com.pcare.dao;

import com.pcare.database.DatabaseConnection;

public class DatabaseInitializer {

    public void initialize() {
        DatabaseConnection.initializeDatabase();
    }
}
