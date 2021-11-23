package com.temporal.roleGameDemo.server.weather;

import com.temporal.roleGameDemo.server.shared.WeatherProvider;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherProviderImpl implements WeatherProvider
{
    private static final Pattern temperatureRegex = Pattern.compile("Summit\s*\\(14411 FT\\)\\s+(\\d+)\\s+");
    private static final DecimalFormat temperatureFormat = new DecimalFormat("#.#");

    public WeatherProviderImpl()
    {
        temperatureFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Override
    public String getCurrentRainierForecast() throws IOException, InterruptedException
    {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://a.atmos.washington.edu/data/rainier_report.html"))
                .build();

        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String payload = (String) response.body();

        if (payload == null)
        {
            return "The weather oracle is saying null";
        }

        Matcher match = temperatureRegex.matcher(payload);
        if (! match.find())
        {
            return "The weather oracle is saying things, but you cannot understand him at all";
        }

        if (match.groupCount() < 1)
        {
            return "The weather oracle is saying things, but you cannot understand enough";
        }

        String fahrenheitStr = match.group(1);

        int fahrenheitInt;
        try
        {
             fahrenheitInt = Integer.parseInt(fahrenheitStr);
        }
        catch (Exception ex)
        {
            return "The weather oracle is saying things; you heared \""
                   + (fahrenheitStr == null ? "NULL" : fahrenheitStr)
                   + "\", but what does it mean?";
        }

        double celciusDouble = (fahrenheitInt - 32) * (5.0 / 9.0);
        Math.round(celciusDouble );
        return "The weather oracle forecees " + temperatureFormat.format(celciusDouble) + "° C";

        //return "The weather is the way it is";
    }
}
