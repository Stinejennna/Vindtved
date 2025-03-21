package org.example.windmillproject;

import com.google.gson.annotations.SerializedName;

public class LastMonthData {

    @SerializedName("date")
    public String date;

    @SerializedName("daily_wind_total")
    public double dailyWindTotal;

    @SerializedName("daily_solar_total")
    public double dailySolarTotal;

    @SerializedName("daily_total")
    public double dailyTotal;
}
