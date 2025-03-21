package org.example.windmillproject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
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
    @FXML
    private BarChart<String, Number> dailyTotalChart;
    @FXML
    private CategoryAxis dailyTotalXAxis;

    private XYChart.Series<Number, Number> windSpeedHistorySeries;
    private XYChart.Series<String, Number> dailyTotalSeries;

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

        setupAxis(windSpeedHistoryXAxis);
    }

    private void setupAxis(NumberAxis axis){
        axis.setAutoRanging(false);
        long now = System.currentTimeMillis();
        long oneDayAgo = now - (24 * 60 * 60 * 1000);
        axis.setLowerBound(oneDayAgo);
        axis.setUpperBound(now);
        axis.setTickUnit(60 * 60 * 1000);
    }

    private void setupCharts() {
        windSpeedHistorySeries = new XYChart.Series<>();
        windSpeedHistorySeries.setName("Wind Speed History (m/s)");
        windSpeedHistoryChart.getData().add(windSpeedHistorySeries);

        dailyTotalSeries = new XYChart.Series<>();
        dailyTotalSeries.setName("Daily Total");
        dailyTotalChart.getData().add(dailyTotalSeries);
    }

    private void startDataFetchTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchAndDisplayData();
            }
        }, 0, 10 * 60 * 1000);
    }

    private void fetchAndDisplayData() {
        try {
            Main.fetchData(this::updateUI, this::storeData, this::updateWindSpeedHistoryChart, this::updateDailyTotalChart);
        } catch (URISyntaxException | InterruptedException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(LatestReading latestReading) {
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
                    reading.parseLoggedAt();
                    if (reading.loggedAt != null) {
                        long timeMillis = reading.loggedAt.getTime();
                        double windSpeed = reading.windSpeed;
                        windSpeedHistorySeries.getData().add(new XYChart.Data<>(timeMillis, windSpeed));
                    }
                }
            }
        });
    }

    private void updateDailyTotalChart(List<LastMonthData> lastMonthData) {
        Platform.runLater(() -> {
            dailyTotalSeries.getData().clear();
            if (lastMonthData != null) {
                for (LastMonthData data : lastMonthData) {
                    dailyTotalSeries.getData().add(new XYChart.Data<>(data.date, data.dailyTotal));
                }
            }
        });
    }
}