package org.example.windmillproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DatabaseHandler {

    private final String url = "jdbc:sqlite:winddata.db";

    public DatabaseHandler() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS readings (" +
                             "logged_at TIMESTAMP PRIMARY KEY," +
                             "wind_speed REAL," +
                             "solar_effect REAL," +
                             "wind_effect INTEGER" +
                             ")")) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertReading(LatestReading reading) throws SQLException {
        if(reading.loggedAt == null){
            return;
        }
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO readings (logged_at, wind_speed, solar_effect, wind_effect) VALUES (?, ?, ?, ?)")) {
            stmt.setTimestamp(1, new Timestamp(reading.loggedAt.getTime()));
            stmt.setDouble(2, reading.windSpeed);
            if(reading.solarEffect == null){
                stmt.setDouble(3, 0.0);
            } else {
                stmt.setDouble(3, reading.solarEffect);
            }
            stmt.setInt(4, (int)reading.windEffect);
            stmt.executeUpdate();
        }
    }
}
