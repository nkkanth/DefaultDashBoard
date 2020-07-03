package org.droidtv.defaultdashboard.data.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.os.UserHandle;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.WeatherInfoDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.WeatherSettingsListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;
import org.droidtv.weather.WeatherBinder;
import org.droidtv.weather.WeatherCallback;
import org.droidtv.weather.WeatherInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

final class WeatherDataManager extends ContextualObject {

    private static String TAG = "WeatherDataManager";

    private static final String ACTION_WEATHER_SERVICE = "org.droidtv.weather.WEATHER_SERVICE_NEW";
    private final int PBSMGR_PROPERTY_FORCE_CUSTOM_NAME_ON = 1;

    private ThreadPoolExecutor mThreadPoolExecutor;
    private ITvSettingsManager mTvSettingsManager;
    private WeatherServiceConnection mWeatherServiceConnection;
    private WeatherBinder mWeatherService;
    private WeatherInfoListener mWeatherInfoListener;
    private ArrayList<WeakReference<WeatherInfoDataListener>> mWeatherInfoDataListenerRefs;
    private ArrayList<WeakReference<WeatherSettingsListener>> mWeatherSettingsListenerRefs;

    WeatherDataManager(Context context) {
        super(context);
        init();
    }

    private void init() {
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.OTHER_THREAD_POOL_EXECUTOR);
        mWeatherServiceConnection = new WeatherServiceConnection();
        mWeatherInfoListener = new WeatherInfoListener();
        mWeatherInfoDataListenerRefs = new ArrayList<>();
        mWeatherSettingsListenerRefs = new ArrayList<>();
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        bindToWeatherService();
    }

    private void bindToWeatherService() {
        Intent weatherServiceIntent = new Intent();
        weatherServiceIntent.setClassName("org.droidtv.weather", "org.droidtv.weather.WeatherService");
        getContext().bindServiceAsUser(weatherServiceIntent, mWeatherServiceConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT_OR_SELF);
    }

    WeatherInfo getWeatherInfo() {
        if (mWeatherService != null) {
            try {
                return mWeatherService.getWeatherInfo();
            } catch (RemoteException | SecurityException e) {
                Log.e(TAG, "#### exception while fetching weather info.");
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
        return null;
    }

    String getPremisesName() {
        String premisesName = mTvSettingsManager.getString(TvSettingsConstants.PBSMGR_PROPERTY_PREMISES_NAME, 0, "");
        if (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FORCE_CUSTOM_NAME, 0, 0) == PBSMGR_PROPERTY_FORCE_CUSTOM_NAME_ON) {
            DdbLogUtility.logTopMenu(TAG, "getPremisesName PBSMGR_PROPERTY_FORCE_CUSTOM_NAME_ON " + premisesName);
            return premisesName;
        }

        WeatherInfo weatherInfo = getWeatherInfo();
        if (isWeatherEnabled() && weatherInfo != null) {
            if (weatherInfo.getInfoSuccess) {
                premisesName = weatherInfo.s_today_location;
            }
        }
        DdbLogUtility.logTopMenu(TAG, "getPremisesName " + premisesName);
        return premisesName;
    }

    String getCurrentTemperature() {
        String temperature = null;
        WeatherInfo weatherInfo = getWeatherInfo();
        if (isWeatherEnabled() && weatherInfo != null) {
            if (weatherInfo.getInfoSuccess) {
                temperature = weatherInfo.s_today_temperature;
                return temperature;
            }
        }
        DdbLogUtility.logTopMenu(TAG, "getCurrentTemperature " + temperature);
        return temperature;
    }

    int getWeatherIcon() {
        int icon = org.droidtv.ui.tvwidget2k15.R.drawable.ic_unknown_day;
        WeatherInfo weatherInfo = getWeatherInfo();
        if (isWeatherEnabled() && weatherInfo != null) {
            if (weatherInfo.getInfoSuccess) {
                icon = weatherInfo.s_today_icon_small;
                return icon;
            }
        }
        return icon;
    }

    boolean isWeatherEnabled() {
        return DashboardDataManager.getInstance().isProfessionalModeEnabled()
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_WEATHER_APP, 0, 0) == TvSettingsDefinitions.PbsWeatherAppConstants.PBSMGR_ON;
    }

    boolean isWeatherEnabled(int value) {
        return DashboardDataManager.getInstance().isProfessionalModeEnabled() && value == TvSettingsDefinitions.PbsWeatherAppConstants.PBSMGR_ON;
    }

    void registerWeatherInfoDataListener(WeatherInfoDataListener listener) {
        if (listener == null) {
            return;
        }
        mWeatherInfoDataListenerRefs.add(new WeakReference<>(listener));
    }

    void unregisterWeatherInfoDataListener(WeatherInfoDataListener weatherInfoDataListener) {
        if (mWeatherInfoDataListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mWeatherInfoDataListenerRefs.size(); i++) {
            WeakReference<WeatherInfoDataListener> ref = mWeatherInfoDataListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            WeatherInfoDataListener listener = ref.get();
            if (listener != null && listener.equals(weatherInfoDataListener)) {
                mWeatherInfoDataListenerRefs.remove(ref);
            }
        }
    }

    void registerWeatherSettingsListener(WeatherSettingsListener listener) {
        if (listener == null) {
            return;
        }
        mWeatherSettingsListenerRefs.add(new WeakReference<>(listener));
    }

    void unregisterWeatherSettingsListener(WeatherSettingsListener weatherSettingsListener) {
        if (mWeatherSettingsListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mWeatherSettingsListenerRefs.size(); i++) {
            WeakReference<WeatherSettingsListener> ref = mWeatherSettingsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            WeatherSettingsListener listener = ref.get();
            if (listener != null && listener.equals(weatherSettingsListener)) {
                mWeatherSettingsListenerRefs.remove(ref);
            }
        }
    }

    void onWeatherInfoReceivedNotify() {
        for (int i = 0; mWeatherInfoDataListenerRefs != null && i < mWeatherInfoDataListenerRefs.size(); i++) {
            WeakReference<WeatherInfoDataListener> listenerRef = mWeatherInfoDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            WeatherInfoDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onWeatherInfoDataReceived();
            }
        }
    }

    void onWeatherInfoReceivedNotify(int value) {
        for (int i = 0; mWeatherInfoDataListenerRefs != null && i < mWeatherInfoDataListenerRefs.size(); i++) {
            WeakReference<WeatherInfoDataListener> listenerRef = mWeatherInfoDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            WeatherInfoDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onWeatherInfoDataReceived(value);
            }
        }
    }

    void onWeatherSettingsChanged() {
        for (int i = 0; mWeatherSettingsListenerRefs != null && i < mWeatherSettingsListenerRefs.size(); i++) {
            WeakReference<WeatherSettingsListener> listenerRef = mWeatherSettingsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            WeatherSettingsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onWeatherSettingsChanged();
            }
        }
    }

    void onWeatherSettingsChanged(int value) {
        for (int i = 0; mWeatherSettingsListenerRefs != null && i < mWeatherSettingsListenerRefs.size(); i++) {
            WeakReference<WeatherSettingsListener> listenerRef = mWeatherSettingsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            WeatherSettingsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onWeatherSettingsChanged(value);
            }
        }
    }

    void professionalModeChanged(){
        onWeatherSettingsChanged();
    }

    private final class WeatherServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "#### WeatherServiceConnection onServiceConnected:" + name + ", service:" + service);
            mWeatherService = WeatherBinder.Stub.asInterface(service);
            try {
                mWeatherService.registerCallback(mWeatherInfoListener);

                // Notify listeners after weather service has been connected so that they can refresh/update weather information related UI
                onWeatherInfoReceivedNotify();
            } catch (RemoteException e) {
                Log.e(TAG, "#### exception when registering callback on WeatherService");
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mWeatherService = null;
        }
    }

    private final class WeatherInfoListener extends WeatherCallback.Stub {
        @Override
        public void onWeatherInfoDataReady(org.droidtv.weather.WeatherInfo weatherInfo) throws RemoteException {
            DdbLogUtility.logTopMenu(TAG, "onWeatherInfoDataReady");
            onWeatherInfoReceivedNotify();
        }
    }
}
