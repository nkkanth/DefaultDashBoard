package org.droidtv.defaultdashboard.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;
import android.os.UserHandle;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.tv.logger.ILogger;

/**
 * Created by bharat.tanwar on 16/12/2019.
 */
public class CulLogger {
    private static final String TAG ="CulLogger";
    private static CulLogger mInstance = null;
    private static Context mContext;
    private ILogger mBinder;

    public static CulLogger getInstance(Context context){
        if(mInstance == null){
            mContext = context;
            mInstance = new CulLogger();
        }
        return mInstance;
    }

    private CulLogger() {
        bindToLogger();
    }

    private void bindToLogger() {
        if (mBinder == null) {
            Intent intent = new Intent();
            intent.setAction("org.droidtv.tv.intent.action.START_LOGGER");
            PackageManager pm = mContext.getPackageManager();
            ResolveInfo ri = pm.resolveService(intent, 0);
            if (ri != null) {
                intent.setClassName(ri.serviceInfo.packageName, ri.serviceInfo.name);
                mContext.bindServiceAsUser(intent, conn, Context.BIND_AUTO_CREATE, UserHandle.CURRENT_OR_SELF);
            }
        }
    }

    private ServiceConnection conn = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DdbLogUtility.logCommon(TAG," onServiceConnected callback received");
            mBinder = ILogger.Instance.asInterface(service);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            DdbLogUtility.logCommon(TAG,"received onServicedisconnected for LoggingHelper, shut this service");
            mBinder = null;
        }
    };

    public void logAppLaunchTrigger(String packagename, String appType){
        ILogger.AppTrigger appTrigger = new ILogger.AppTrigger();
        appTrigger.trigger_method = ILogger.AppTrigger.TriggerMethod.Dashboard;
        if(appType != null && packagename != null ){
            appTrigger.package_name = packagename;
            switch (appType) {
                case "Portal":
                    appTrigger.application_type = ILogger.AppTrigger.ApplicationType.PORTAL_APP;
                    break;
                case "Local":
                    appTrigger.application_type = ILogger.AppTrigger.ApplicationType.LOCAL_APP;
                    break;
                case "PlayStore":
                    appTrigger.application_type = ILogger.AppTrigger.ApplicationType.PLAYSTORE_APP;
                    break;
                default:
                    appTrigger.application_type = ILogger.AppTrigger.ApplicationType.SYSTEM_APP;
                    break;
            }
        }

        DdbLogUtility.logCommon(TAG ,"logAppLaunchTrigger for :"+" packagename :" +packagename + " appType :"+appType +" Triggered by :"+ILogger.AppTrigger.TriggerMethod.Dashboard);
        if(mBinder != null){
            mBinder.Log(appTrigger);
        }
        else{
            DdbLogUtility.logCommon(TAG,"Binder NOT available, can't log App trigger");
        }
    }
}
