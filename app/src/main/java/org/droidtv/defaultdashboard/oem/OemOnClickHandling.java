package org.droidtv.defaultdashboard.oem;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.util.Constants;

import androidx.annotation.Nullable;

public class OemOnClickHandling extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("OemOnClickHandling", "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if(intent != null) {
            Uri url = intent.getData();
            Intent ddbIntent = new Intent();
            ddbIntent.setAction(Constants.INTENT_ACTION_DEFAULT_DASHBOARD);
            ddbIntent.setComponent(new ComponentName(getPackageName(), "org.droidtv.defaultdashboard.DashboardActivity"));
            startActivityAsUser(ddbIntent, UserHandle.CURRENT_OR_SELF);

        }
        return START_NOT_STICKY;
    }
}
