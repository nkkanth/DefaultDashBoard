package org.droidtv.weather;

import org.droidtv.weather.WeatherInfo;

interface WeatherCallback {

	void onWeatherInfoDataReady(in WeatherInfo weatherInfo);

}