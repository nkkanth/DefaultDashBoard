package org.droidtv.defaultdashboard.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.os.UserHandle;

import org.droidtv.htv.japit.IJapitApplicationControl;

/**
 * Created by krishnakishor.v on 20-12-2018.
 */

public class ApplicationControlHelper {
    private static final String TAG = ApplicationControlHelper.class.getSimpleName();
    private static IJapitApplicationControl mJapitAppControl = null;
    private static ApplicationControlHelper mInstance = null;

    public static ApplicationControlHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ApplicationControlHelper();
        }
        return mInstance;
    }

    public void bindToJapitApplicationControlService(Context context) {
        Log.d(TAG, "bindToJapitApplicationControlService");
        if (mJapitAppControl == null) {
            Intent serviceIntent = new Intent(IJapitApplicationControl.JAPIT_APPLICATION_CONTROL_SERVICE_INTENT_ACTION);
            serviceIntent.setPackage("org.droidtv.japit");
            context.bindServiceAsUser(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT_OR_SELF);
        } else {
            Log.d(TAG, "Japit Service already bound");
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "Connected to Japit ");
            mJapitAppControl = IJapitApplicationControl.Instance.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "Disconnected from Japit ");
            mJapitAppControl = null;
        }
    };

    public static IJapitApplicationControl getJapitApplicatioNControlService() {
        return mJapitAppControl;
    }
}
