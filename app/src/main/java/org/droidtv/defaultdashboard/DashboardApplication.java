package org.droidtv.defaultdashboard;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;

import org.droidtv.defaultdashboard.common.ApplicationControlHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.util.DDBImageLoader;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;

import java.util.Map;

/**
 * Created by sandeep.kumar on 24/10/2017.
 */

public class DashboardApplication extends Application {

    private ITvSettingsManager mITvSettingsManager;
    private static  DashboardApplication _instance = null;
    boolean isBedSideTv = false;
    Context mDeviceContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        isBedSideTv = getBedSideTv();
        Log.d("DashboardApplication" , "calling moveSharedPrefs before DashboardDataManager init");
        moveSharedPrefs();
        DashboardDataManager.init(getApplicationContext());
        Log.d("DashboardApplication" , "#### DashboardApplication onCreate");
        ApplicationControlHelper.getInstance().bindToJapitApplicationControlService(getApplicationContext());
    }

    private Context getDeviceProtectedContext(Context c){
        return c.getApplicationContext().createDeviceProtectedStorageContext();

    }

    private void moveSharedPrefs(){
        try {
            mDeviceContext = getDeviceProtectedContext(getApplicationContext());
            if (mDeviceContext != null) {
               boolean succes = mDeviceContext.moveSharedPreferencesFrom(getApplicationContext(), PreferenceManager.getDefaultSharedPreferencesName(getApplicationContext()));
                Log.d(DashboardApplication.class.getSimpleName(), "moveSharedPrefs: " + succes);
            }else{
                Log.e(DashboardApplication.class.getSimpleName(), "mDeviceContext was null");
            }
        }catch (Exception e){
            Log.d("DashboardApplication", "moveSharedPrefs: " + e.getMessage());
        }
    }
    
    public static DashboardApplication getInstance(){return _instance;}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DashboardDataManager.getInstance().onConfigurationChanged();
    }

    private boolean getBedSideTv(){
        mITvSettingsManager = ITvSettingsManager.Instance.getInterface();
        return  (mITvSettingsManager != null && mITvSettingsManager.getInt(TvSettingsConstants.OPHTVTYPE, 0, 0) == 2);
    }

    public boolean isBedSideTv(){return isBedSideTv;}

    @Override
    public void onLowMemory() {
        Log.d("DashboardApplication", "onLowMemory called");
       // DDBImageLoader.cleanImageStore();
    }

    @Override
    public void onTrimMemory(final int level) {
        Log.d("DashboardApplication", "onTrimMemory() called with: level = " + level );
        int intermediateLevel = getIntermediateTrimMemoryLevel(level);
        switch (intermediateLevel){
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                //clearCache();
                break;
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                //clearCache();
                break;
           default:
               //No action
               break;
        }
        int  pss = getAppPssInfo(getApplicationContext(), android.os.Process.myPid());
        Log.d("Debug", "onTrimMemory intermediateLevel: " + intermediateLevel + " PSS(MB):  " + pss);
    }

    public int getAppPssInfo(Context context, int pid) {    //Returns MB of this app
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Debug.MemoryInfo memoryInfo = am.getProcessMemoryInfo(new int[]{pid})[0];
        return memoryInfo.getTotalPss()/(1024);
    }

    private void clearCache(){
        DDBImageLoader.cleanImageStore();
    }


    private int getIntermediateTrimMemoryLevel(int level) {
        int inetrmediateLevel = level;
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE && level < ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) { //level>=5 & level<10
            inetrmediateLevel = ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW && level < ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) { //level>=10 & level<15
            inetrmediateLevel = ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL && level < ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {//level>=15 & level<20
            inetrmediateLevel = ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN && level < ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {//level>=20 & level<40
            inetrmediateLevel = ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND && level < ComponentCallbacks2.TRIM_MEMORY_MODERATE) {//level>=40 & level<60
            inetrmediateLevel = ComponentCallbacks2.TRIM_MEMORY_BACKGROUND;
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE && level < ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {//level>=60 & level<80
            inetrmediateLevel = ComponentCallbacks2.TRIM_MEMORY_COMPLETE;
        }
        return inetrmediateLevel;
    }
}
