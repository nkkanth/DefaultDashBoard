package org.droidtv.defaultdashboard.common;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.htv.japit.IJapitApplicationControl;

public class ApplicationControlServiceBinder {

    private static final String TAG = "ApplicationControlServiceBinder";
    private static ApplicationControlServiceBinder INSTANCE = null;
    private JapitCallBack mJapitCallback = null;
    private SparseArray<Integer> applicationAvailability = new SparseArray<>();
    private SparseArray<Integer> applicationStatus = new SparseArray<>();
    private static Context mContext;

    private ApplicationControlServiceBinder(Context context) {
        ApplicationControlHelper.getInstance().bindToJapitApplicationControlService(context);
        mJapitCallback = new JapitCallBack();
        Log.d(TAG, "#### ApplicationControlServiceBinder ");
    }

    public static ApplicationControlServiceBinder getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ApplicationControlServiceBinder(context);
        }
        mContext = context;
        return INSTANCE;
    }

    public void onApplicationCreated(IJapitApplicationControl.ApplicationType appType) {
        IJapitApplicationControl japitAppControl = ApplicationControlHelper.getJapitApplicatioNControlService();
        if (japitAppControl != null) {
            Log.d(TAG, "#### onApplicationCreated ###");
            int[] mAppType = new int[1];
            mAppType[0] = appType.ordinal();
            japitAppControl.registerJapitApplicationControlListener(mAppType, mJapitCallback);
            japitAppControl.setApplicationAvailability(mAppType[0], IJapitApplicationControl.AppAvailableStatus.APP_AVAILABLE.ordinal());
            japitAppControl.setApplicationState(mAppType[0], IJapitApplicationControl.ApplicationStatus.APP_STATE_SHOWN.ordinal());
            applicationAvailability.put(appType.ordinal(), IJapitApplicationControl.AppAvailableStatus.APP_AVAILABLE.ordinal());
            applicationStatus.put(appType.ordinal(), IJapitApplicationControl.ApplicationStatus.APP_STATE_SHOWN.ordinal());
        } else {
            Log.e(TAG, "#### onApplicationCreated japitAppControl not instanciated ");
        }
    }

    public void onApplicationDestroyed(IJapitApplicationControl.ApplicationType appType) {
        IJapitApplicationControl japitAppControl = ApplicationControlHelper.getJapitApplicatioNControlService();
        if (japitAppControl != null) {
            Log.d(TAG, "#### onApplicationDestroyed ###");
            int[] mAppType = new int[1];
            mAppType[0] = appType.ordinal();
            japitAppControl.unRegisterJapitApplicationControlListener(mAppType);
            japitAppControl.setApplicationState(mAppType[0], IJapitApplicationControl.ApplicationStatus.APP_STATE_HIDDEN.ordinal());
            applicationAvailability.put(appType.ordinal(), IJapitApplicationControl.AppAvailableStatus.APP_UNAVAILABLE.ordinal());
            applicationStatus.put(appType.ordinal(), IJapitApplicationControl.ApplicationStatus.APP_STATE_HIDDEN.ordinal());
        } else {
            Log.e(TAG, "#### onApplicationDestroyed japitAppControl not instanciated ");
        }
    }

    public void setApplicationAvailability(IJapitApplicationControl.ApplicationType appType, boolean isAvailable) {
        IJapitApplicationControl japitAppControl = ApplicationControlHelper.getJapitApplicatioNControlService();
        if(japitAppControl == null) return;

        if (isAvailable) {
            japitAppControl.setApplicationAvailability(appType.ordinal(), IJapitApplicationControl.AppAvailableStatus.APP_AVAILABLE.ordinal());
        } else {
            japitAppControl.setApplicationAvailability(appType.ordinal(), IJapitApplicationControl.AppAvailableStatus.APP_UNAVAILABLE.ordinal());
        }
    }

    public void setApplicationState(IJapitApplicationControl.ApplicationType appType, boolean isShown) {
        IJapitApplicationControl japitAppControl = ApplicationControlHelper.getJapitApplicatioNControlService();
	if(japitAppControl != null){
        	if (isShown) {
            		japitAppControl.setApplicationState(appType.ordinal(), IJapitApplicationControl.ApplicationStatus.APP_STATE_SHOWN.ordinal());
            		japitAppControl.setApplicationAvailability(appType.ordinal(), IJapitApplicationControl.AppAvailableStatus.APP_AVAILABLE.ordinal());
        	} else {
            		japitAppControl.setApplicationState(appType.ordinal(), IJapitApplicationControl.ApplicationStatus.APP_STATE_HIDDEN.ordinal());
            		japitAppControl.setApplicationAvailability(appType.ordinal(), IJapitApplicationControl.AppAvailableStatus.APP_UNAVAILABLE.ordinal());
        	}
	}
    }

    public class JapitCallBack extends IJapitApplicationControl.IJapitApplicationControlCallBack {

        @Override
        public int getApplicationAvailability(int appType) {
            return applicationAvailability.get(appType);
        }

        @Override
        public int getApplicationStatus(int appType) {
            return applicationStatus.get(appType);
        }

        @Override
        public void requestApplicationStateChange(int appType, int appRequestState) {
            if(appType == IJapitApplicationControl.ApplicationType.APP_TYPE_DEFAULT_DASHBOARD.ordinal() &&
                    appRequestState == IJapitApplicationControl.AppRequestState.APP_REQUEST_STOP.ordinal()) {
                if(mContext != null){
                    Intent intent = new Intent(Constants.ACTION_STOP_DDB);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    mContext = null;
                }
            }
        }
    }
}
