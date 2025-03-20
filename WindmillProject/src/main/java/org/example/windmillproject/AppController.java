package org.example.windmillproject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;

import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppController implements Initializable {

    @FXML
    private LineChart<Number, Number> windSpeedHistoryChart;
    @FXML
    private NumberAxis windSpeedHistoryXAxis;

    private XYChart.Series<Number, Number> windSpeedHistorySeries;

    private final DatabaseHandler databaseHandler = new DatabaseHandler();
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCharts();
        startDataFetchTimer();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        windSpeedHistoryXAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                return dateFormat.format(new Date(object.longValue()));
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });

        // Set explicit axis range for 24 hours
        windSpeedHistoryXAxis.setAutoRanging(false);
        long now = System.currentTimeMillis();
        long oneDayAgo = now - (24 * 60 * 60 * 1000); // 24 hours ago
        windSpeedHistoryXAxis.setLowerBound(oneDayAgo);
        windSpeedHistoryXAxis.setUpperBound(now);
        windSpeedHistoryXAxis.setTickUnit(60 * 60 * 1000); // 2 hours tick unit
    }

    private void setupCharts() {
        windSpeedHistorySeries = new XYChart.Series<>();
        windSpeedHistorySeries.setName("Wind Speed History (m/s)");
        windSpeedHistoryChart.getData().add(windSpeedHistorySeries);
    }

    private void startDataFetchTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchAndDisplayData();
            }
        }, 0, 10 * 60 * 1000); // 10 minutes
    }

    private void fetchAndDisplayData() {
        try {
            Main.fetchData(this::updateUI, this::storeData, this::updateWindSpeedHistoryChart);
        } catch (URISyntaxException | InterruptedException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(LatestReading latestReading) {
        // No longer used, but kept for Main.fetchData signature
    }

    private void storeData(LatestReading latestReading) {
        databaseExecutor.submit(() -> {
            try {
                databaseHandler.insertReading(latestReading);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateWindSpeedHistoryChart(List<LatestReading> latestReadings) {
        Platform.runLater(() -> {
            windSpeedHistorySeries.getData().clear();
            if (latestReadings != null) {
                for (LatestReading reading : latestReadings) {
                    reading.parseLoggedAt(); // Parse the timestamp
                    if (reading.loggedAt != null) {
                        long timeMillis = reading.loggedAt.getTime();
                        double windSpeed = reading.windSpeed;

                        windSpeedHistorySeries.getData().add(new XYChart.Data<>(timeMillis, windSpeed));
                    }
                }
            }
        });
    }
}