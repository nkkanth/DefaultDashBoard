package org.droidtv.weather;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import org.droidtv.defaultdashboard.R;
public class WeatherInfo implements Parcelable {
	public static boolean isOnline = false;
	public boolean getInfoSuccess;
	public boolean isUnkonwnWeatherInfo = false;
	public boolean isWeatherByInternet = false;
	public boolean isWeatherByServer = false;
	public boolean isWeatherByClone = false;

	public final int STATUS_UNKNOWN = 0;
	public final int STATUS_INTERNET = 1;
	public final int STATUS_SERVER = 2;
	public final int STATUS_CLONE = 3;
	public int STATUS = 0;
	public int OLD_STATUS = 0;

    public boolean daytime;
    public int s_today_icon_widget;
    public int s_today_icon_select;

    public String s_today_location = "";
    public String s_today_time;
    public String s_today_Date;
    public String s_today_temperature = "";
    public String s_today_script;

    public int s_today_pictoId;
    public String s_today;
    public int s_today_icon;
    public int s_today_icon_small;
    public String s_today_temperature_Low = "";
    public String s_today_temperature_High = "";

    public int s_second_pictoId;
    public String s_second_descript;
    public String s_second_day;
    public int s_second_icon;
    public String s_second_temperature_Low = "";
    public String s_second_temperature_High = "";

    public int s_third_pictoId;
    public String s_third_descript;
    public String s_third_day;
    public int s_third_icon;
    public String s_third_temperature_High = "";
    public String s_third_temperature_Low = "";

    public int s_fourth_pictoId;
    public String s_fourth_descript;
    public String s_fourth_day;
    public int s_fourth_icon;
    public String s_fourth_temperature_High = "";
    public String s_fourth_temperature_Low = "";

    public int s_fifth_pictoId;
    public String s_fifth_descript;
    public String s_fifth_day;
    public int s_fifth_icon;
    public String s_fifth_temperature_High = "";
    public String s_fifth_temperature_Low = "";

    public int[] pictoID_string_day = {
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_UNKNOWN,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MOSTLY_CLOUDY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MOSTLY_SUNNY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MOSTLY_SUNNY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_THUNDER_STORMS,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_FOG,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_RAIN,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_SUNNY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_SNOW,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MIXED_RAIN_AND_SNOW,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_CLOUDY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_SHOWERS
    };
    public int[] pictoID_string_night = {
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_UNKNOWN,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MOSTLY_CLOUDY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MOSTLY_CLEAR,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MOSTLY_CLEAR,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_THUNDER_STORMS,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_FOG,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_RAIN,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_CLEAR,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_SNOW,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_MIXED_RAIN_AND_SNOW,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_CLOUDY,
            org.droidtv.ui.htvstrings.R.string.HTV_WI_WEATHER_SHOWERS
    };

    public int[] weather_icon_small_id_array = {
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_unknown_day,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mostly_cloudy_day,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mostly_clear_day,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mostly_clear_day,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_thunder_storms,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_fog,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_rain,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_clear_day,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_snow,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mixed_rain_snow,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_cloudy,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_showers_day,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_unknown_night,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mostly_cloudy_night,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mostly_clear_night,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mostly_clear_night,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_thunder_storms,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_fog,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_rain,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_clear_night,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_snow,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_mixed_rain_snow,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_cloudy,
            org.droidtv.ui.tvwidget2k15.R.drawable.ic_showers_night
    };
	/************************add for DDB end**************************/
	public int[] xhdip_weather_icon_id = {
			R.drawable.ic_unknown_day,
			R.drawable.ic_mostly_cloudy_day,
			R.drawable.ic_mostly_sunny,
			R.drawable.ic_mostly_sunny,
			R.drawable.ic_thunder_storms,
			R.drawable.ic_fog,
			R.drawable.ic_rain,
			R.drawable.ic_sunny,
			R.drawable.ic_snow,
			R.drawable.ic_mixed_rain_snow,
			R.drawable.ic_cloudy,
			R.drawable.ic_showers_day,
			R.drawable.ic_unknown_night,
			R.drawable.ic_mostly_cloudy_night,
			R.drawable.ic_mostly_clear_night,
			R.drawable.ic_mostly_clear_night,
			R.drawable.ic_thunder_storms_night,
			R.drawable.ic_fog_night,
			R.drawable.ic_rain_night,
			R.drawable.ic_clear_night,
			R.drawable.ic_snow_night,
			R.drawable.ic_mixed_rain_snow_night,
			R.drawable.ic_cloudy_night,
			R.drawable.ic_showers_night
			};
	public int[] xxhdip_weather_icon_id = {
			R.drawable.ic_unknown_day,
			R.drawable.ic_mostly_cloudy_day,
			R.drawable.ic_mostly_sunny,
			R.drawable.ic_mostly_sunny,
			R.drawable.ic_thunder_storms,
			R.drawable.ic_fog,
			R.drawable.ic_rain,
			R.drawable.ic_sunny,
			R.drawable.ic_snow,
			R.drawable.ic_mixed_rain_snow,
			R.drawable.ic_cloudy,
			R.drawable.ic_showers_day,
			R.drawable.ic_unknown_night,
			R.drawable.ic_mostly_cloudy_night,
			R.drawable.ic_mostly_clear_night,
			R.drawable.ic_mostly_clear_night,
			R.drawable.ic_thunder_storms_night,
			R.drawable.ic_fog_night,
			R.drawable.ic_rain_night,
			R.drawable.ic_clear_night,
			R.drawable.ic_snow_night,
			R.drawable.ic_mixed_rain_snow_night,
			R.drawable.ic_cloudy_night,
			R.drawable.ic_showers_night
			};
	public int[] windowBackgroundIcon = {
			R.drawable.unknown_bg,
			R.drawable.mostly_cloudy_bg,
			R.drawable.mostly_sunny_bg,
			R.drawable.mostly_sunny_bg,
			R.drawable.thunderstorms_bg,
			R.drawable.fog_bg,
			R.drawable.rain_bg,
			R.drawable.sunny_bg,
			R.drawable.snow_bg,
			R.drawable.mixed_rain_snow_bg,
			R.drawable.cloudy_bg,
			R.drawable.showers_bg,
			R.drawable.unknown_night_bg,
			R.drawable.mostly_cloudy_night_bg,
			R.drawable.mostly_sunny_night_bg,
			R.drawable.mostly_sunny_night_bg,
			R.drawable.thunderstorms_night_bg,
			R.drawable.fog_night_bg,
			R.drawable.rain_night_bg,
			R.drawable.sunny_night_bg,
			R.drawable.snow_night_bg,
			R.drawable.mixed_rain_snow_night_bg,
			R.drawable.cloudy_night_bg,
			R.drawable.showers_night_bg
			};
	public int[] highlightedBackgroundIcon = {
			R.drawable.highlighted_day_unknown_day_and_night,
			R.drawable.highlighted_day_mostly_cloudy_day,
			R.drawable.highlighted_day_mostly_clear_day,
			R.drawable.highlighted_day_mostly_clear_day,
			R.drawable.highlighted_day_thunderstorms_day,
			R.drawable.highlighted_day_fog_day,
			R.drawable.highlighted_day_rain_day,
			R.drawable.highlighted_day_clear_day,
			R.drawable.highlighted_day_snow_day,
			R.drawable.highlighted_day_rain_and_snow_day,
			R.drawable.highlighted_day_cloudy_day,
			R.drawable.highlighted_day_showers_day,
			R.drawable.highlighted_day_unknown_day_and_night,
			R.drawable.highlighted_day_mostly_cloudy_night,
			R.drawable.highlighted_day_mostly_clear_night,
			R.drawable.highlighted_day_mostly_clear_night,
			R.drawable.highlighted_day_thunderstorms_night,
			R.drawable.highlighted_day_fog_night,
			R.drawable.highlighted_day_rain_night,
			R.drawable.highlighted_day_clear_night,
			R.drawable.highlighted_day_snow_night,
			R.drawable.highlighted_day_rain_and_snow_day,
			R.drawable.highlighted_day_cloudy_night,
			R.drawable.highlighted_day_showers_night
			};
	public int[] weekBarIcon = {
			R.drawable.week_bar_unknown_day_and_night,
			R.drawable.week_bar_mostly_cloudy_day,
			R.drawable.week_bar_mostly_clear_day,
			R.drawable.week_bar_mostly_clear_day,
			R.drawable.week_bar_thunderstorms_day,
			R.drawable.week_bar_fog_day,
			R.drawable.week_bar_rain_day,
			R.drawable.week_bar_clear_day,
			R.drawable.week_bar_snow_day,
			R.drawable.week_bar_rain_and_snow_day,
			R.drawable.week_bar_cloudy_day,
			R.drawable.week_bar_showers_day,
			R.drawable.week_bar_unknown_day_and_night,
			R.drawable.week_bar_mostly_cloudy_night,
			R.drawable.week_bar_mostly_clear_night,
			R.drawable.week_bar_mostly_clear_night,
			R.drawable.week_bar_thunderstorms_night,
			R.drawable.week_bar_fog_night,
			R.drawable.week_bar_rain_night,
			R.drawable.week_bar_clear_night,
			R.drawable.week_bar_snow_night,
			R.drawable.week_bar_rain_and_snow_day,
			R.drawable.week_bar_cloudy_night,
			R.drawable.week_bar_showers_night
			};
	public int[] weekTipIcon = {
			R.drawable.icon_unknown_tip_day,
			R.drawable.icon_mostly_cloudy_tip_day,
			R.drawable.icon_mostly_clear_tip_day,
			R.drawable.icon_mostly_clear_tip_day,
			R.drawable.icon_thunderstorms_tip_day,
			R.drawable.icon_fog_tip_day,
			R.drawable.icon_rain_tip_day,
			R.drawable.icon_sunny_tip_day,
			R.drawable.icon_snow_tip_day,
			R.drawable.icon_rain_and_snow_tip_day,
			R.drawable.icon_cloudy_tip_day,
			R.drawable.icon_showers_tip_day,
			R.drawable.icon_unknown_tip_night,
			R.drawable.icon_mostly_cloudy_tip_night,
			R.drawable.icon_mostly_clear_tip_night,
			R.drawable.icon_mostly_clear_tip_night,
			R.drawable.icon_thunderstorms_tip_night,
			R.drawable.icon_fog_tip_night,
			R.drawable.icon_rain_tip_night,
			R.drawable.icon_sunny_tip_night,
			R.drawable.icon_snow_tip_night,
			R.drawable.icon_rain_and_snow_tip_night,
			R.drawable.icon_cloudy_tip_night,
			R.drawable.icon_showers_tip_night
			};

	//private ArrayList<WeekTabViewEntity> mWeatherInfors;
	private char symbol = 176;

    private WeatherInfo(Parcel in) {
        readFromParcel(in);
    }

    public WeatherInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getInfoSuccess ? 1 : 0);
        dest.writeInt(isWeatherByInternet ? 1 : 0);
        dest.writeInt(isWeatherByServer ? 1 : 0);
        dest.writeInt(isWeatherByClone ? 1 : 0);
        dest.writeInt(daytime ? 1 : 0);
        dest.writeInt(s_today_icon_widget);
        dest.writeInt(s_today_icon_select);
        dest.writeString(s_today_location);
        dest.writeString(s_today_time);
        dest.writeString(s_today_Date);
        dest.writeString(s_today_temperature);
        dest.writeString(s_today_script);
        dest.writeInt(s_today_pictoId);
        dest.writeString(s_today);
        dest.writeInt(s_today_icon);
        dest.writeInt(s_today_icon_small);
        dest.writeString(s_today_temperature_Low);
        dest.writeString(s_today_temperature_High);
    }

    public void readFromParcel(Parcel in) {
        getInfoSuccess = in.readInt() != 0;
        isWeatherByInternet = in.readInt() != 0;
        isWeatherByServer = in.readInt() != 0;
        isWeatherByClone = in.readInt() != 0;

        daytime = in.readInt() != 0;
        s_today_icon_widget = in.readInt();
        s_today_icon_select = in.readInt();

        s_today_location = in.readString();
        s_today_time = in.readString();
        s_today_Date = in.readString();
        s_today_temperature = in.readString();
        s_today_script = in.readString();

        s_today_pictoId = in.readInt();
        s_today = in.readString();
        s_today_icon = in.readInt();
        s_today_icon_small = in.readInt();
        s_today_temperature_Low = in.readString();
        s_today_temperature_High = in.readString();
    }

    public static final Creator<WeatherInfo> CREATOR = new Creator<WeatherInfo>() {

        @Override
        public WeatherInfo[] newArray(int size) {
            return new WeatherInfo[size];
        }

        @Override
        public WeatherInfo createFromParcel(Parcel in) {
            return new WeatherInfo(in);
        }
    };

	public String toString (){
		String st_WeatherInfo = "";
		st_WeatherInfo = "getInfoSuccess = " + getInfoSuccess +"  s_today_location = " + s_today_location + " s_today_pictoId = "+ s_today_pictoId + " s_today = " + s_today + " s_today_temperature = " + s_today_temperature + " s_today_script = " + s_today_script + " s_today_temperature_Low = " + s_today_temperature_Low + " s_today_temperature_High = " + s_today_temperature_High
						 + "  ;s_second_pictoId = "+ s_second_pictoId + " s_second_descript = " + s_second_descript + " s_second_day = " + s_second_day +" s_second_temperature_Low =" + s_second_temperature_Low + " s_second_temperature_High = " + s_second_temperature_High + "  ;s_third_pictoId = " + s_third_pictoId + " s_third_descript = " + s_third_descript + " s_third_day = " + s_third_day + " s_third_temperature_Low =" 
						 + s_third_temperature_Low + " s_third_temperature_High = " + s_third_temperature_High + "  ;s_fourth_pictoId = " + s_fourth_pictoId + " s_fourth_descript = " + s_fourth_descript + " s_fourth_day = " + s_fourth_day +  " s_fourth_temperature_Low =" + s_fourth_temperature_Low + " s_fourth_temperature_High = " + s_fourth_temperature_High
						 + "  ;s_fifth_pictoId = " + s_fifth_pictoId + " s_fifth_descript = " + s_fifth_descript + " s_fifth_day = " + s_fifth_day +  " s_fifth_temperature_Low =" + s_fifth_temperature_Low + " s_fifth_temperature_High = " + s_fifth_temperature_High;
		return st_WeatherInfo;
	}

	/*public ArrayList<WeekTabViewEntity> getWeatherInforData(Context context) {
		mWeatherInfors = new ArrayList<>();
		WeekTabViewEntity entity1 = new WeekTabViewEntity.Builder().currentDescript("Sunnny").currentTemperature("16" + String.valueOf(symbol))
				.weekName("Today").windowBackground(R.drawable.clear_day_bg)
				.weekTipIcon(context.getResources().getDrawable(R.drawable.icon_sunny_tip_day))
				.backgroundIcon(R.drawable.highlighted_day_clear_day).weatherIcon(R.drawable.ic_sunny)
				.temparature("22" + String.valueOf(symbol), "10" + String.valueOf(symbol)).weekBarIcon(R.drawable.week_bar_clear_day).builder();
		WeekTabViewEntity entity2 = new WeekTabViewEntity.Builder().currentDescript("Cloudy").currentTemperature("18" + String.valueOf(symbol))
				.weekName("Tuesday").windowBackground(R.drawable.cloud_day_bg)
				.weekTipIcon(context.getResources().getDrawable(R.drawable.icon_cloudy_tip_day))
				.backgroundIcon(R.drawable.highlighted_day_cloudy_day).weatherIcon(R.drawable.ic_cloudy)
				.temparature("22" + String.valueOf(symbol), "8" + String.valueOf(symbol)).weekBarIcon(R.drawable.week_bar_cloudy_day).builder();
		WeekTabViewEntity entity3 = new WeekTabViewEntity.Builder().currentDescript("Fog").currentTemperature("22" + String.valueOf(symbol))
				.weekName("Wednesday").windowBackground(R.drawable.fog_day_bg)
				.weekTipIcon(context.getResources().getDrawable(R.drawable.icon_fog_tip_day))
				.backgroundIcon(R.drawable.highlighted_day_fog_day).weatherIcon(R.drawable.ic_fog)
				.temparature("22" + String.valueOf(symbol), "7" + String.valueOf(symbol)).weekBarIcon(R.drawable.week_bar_fog_day).builder();
		WeekTabViewEntity entity4 = new WeekTabViewEntity.Builder().currentDescript("Mostly sunny")
				.currentTemperature("21" + String.valueOf(symbol)).weekName("Thursday").windowBackground(R.drawable.mostly_sunny_bg)
				.weekTipIcon(context.getResources().getDrawable(R.drawable.icon_mostly_clear_tip_day))
				.backgroundIcon(R.drawable.highlighted_day_mostly_clear_day).weatherIcon(R.drawable.ic_mostly_sunny)
				.temparature("22" + String.valueOf(symbol), "15" + String.valueOf(symbol)).weekBarIcon(R.drawable.week_bar_mostly_clear_day).builder();
		WeekTabViewEntity entity5 = new WeekTabViewEntity.Builder().currentDescript("Mostly cloudy")
				.currentTemperature("19" + String.valueOf(symbol)).weekName("Friday").windowBackground(R.drawable.mostly_cloudy_bg)
				.weekTipIcon(context.getResources().getDrawable(R.drawable.icon_mostly_cloudy_tip_day))
				.backgroundIcon(R.drawable.highlighted_day_mostly_cloudy_day)
				.weatherIcon(R.drawable.ic_mostly_cloudy_day).temparature("22" + String.valueOf(symbol), "14" + String.valueOf(symbol))
				.weekBarIcon(R.drawable.week_bar_mostly_cloudy_day).builder();
		mWeatherInfors.add(entity1);
		mWeatherInfors.add(entity2);
		mWeatherInfors.add(entity3);
		mWeatherInfors.add(entity4);
		mWeatherInfors.add(entity5);
		return mWeatherInfors;
	}

	public WeekTabViewEntity getNightEntity(Context context) {
		WeekTabViewEntity nightEntity = new WeekTabViewEntity.Builder().currentDescript("Clear")
				.currentTemperature("15" + String.valueOf(symbol)).temparature("20" + String.valueOf(symbol), "10" + String.valueOf(symbol)).weatherIcon(R.drawable.ic_night).weekName("Today")
				.weekBarIcon(R.drawable.week_bar_clear_night)
				.weekTipIcon(context.getResources().getDrawable(R.drawable.icon_sunny_tip_night))
				.backgroundIcon(R.drawable.highlighted_day_clear_night).windowBackground(R.drawable.clear_night_bg).builder();
		return nightEntity;
	}*/

}
