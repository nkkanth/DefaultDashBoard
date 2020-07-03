package org.droidtv.weather;

import org.droidtv.weather.WeatherInfo;
import org.droidtv.weather.WeatherCallback;

interface WeatherBinder {
	WeatherInfo getWeatherInfo();
	void registerCallback(WeatherCallback weatherCallback);
	void unregisterCallback(WeatherCallback weatherCallback);

}