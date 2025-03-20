package org.example.windmillproject;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LatestReading {

    @SerializedName("id")
    public int id;
    @SerializedName("location_id")
    public int locationId;
    @SerializedName("logged_at")
    public String loggedAtString; // Store as String initially
    public Date loggedAt; // Store as Date after parsing
    @SerializedName("wind_effect")
    public double windEffect;
    @SerializedName("wind_speed")
    public double windSpeed;
    @SerializedName("wind_direction")
    public Double windDirection;
    @SerializedName("solar_effect")
    public Double solarEffect;
    @SerializedName("solar_gradiation")
    public Double solarGradiation;
    @SerializedName("created_at")
    public String createdAt;
    @SerializedName("updated_at")
    public String updatedAt;

    public void parseLoggedAt() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.loggedAt = dateFormat.parse(loggedAtString);
        } catch (ParseException e) {
            e.printStackTrace();
            this.loggedAt = null; // Handle parsing error
        }
    }
}