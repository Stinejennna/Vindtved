package org.example.windmillproject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Main {
    public static void fetchData(Consumer<LatestReading> uiUpdate, Consumer<LatestReading> databaseStore, Consumer<List<LatestReading>> historyUpdate, Consumer<List<LastMonthData>> dailyTotalConsumer) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(new URI("https://vind-og-klima-app.videnomvind.dk/api/stats?location=vindtved"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        System.out.println("API Response: " + responseBody); // Log the response

        try {
            JsonObject jObject = JsonParser.parseString(responseBody).getAsJsonObject();

            Gson gson = new Gson();
            LatestReading latestReading = gson.fromJson(jObject.get("latest_reading"), LatestReading.class);

            Type listType = new TypeToken<List<LatestReading>>() {}.getType();
            List<LatestReading> latestReadings = gson.fromJson(jObject.get("latest_readings"), listType);

            // Extract last_month data
            if (jObject.has("last_month") && jObject.get("last_month").isJsonArray()) {
                JsonArray lastMonthArray = jObject.getAsJsonArray("last_month");
                List<LastMonthData> lastMonthDataList = new ArrayList<>();
                for (int i = 0; i < lastMonthArray.size(); i++) {
                    LastMonthData data = gson.fromJson(lastMonthArray.get(i), LastMonthData.class);
                    lastMonthDataList.add(data);
                }
                if (dailyTotalConsumer != null) {
                    dailyTotalConsumer.accept(lastMonthDataList);
                }
            }

            uiUpdate.accept(latestReading);
            databaseStore.accept(latestReading);
            for (LatestReading reading : latestReadings) {
                databaseStore.accept(reading);
            }
            historyUpdate.accept(latestReadings);
        } catch (JsonParseException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}