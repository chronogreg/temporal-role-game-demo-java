package com.temporal.roleGameDemo.server.shared;

import io.temporal.activity.ActivityInterface;

import java.io.IOException;

@ActivityInterface
public interface WeatherProvider
{
    String getCurrentRainierForecast() throws IOException, InterruptedException;
}
