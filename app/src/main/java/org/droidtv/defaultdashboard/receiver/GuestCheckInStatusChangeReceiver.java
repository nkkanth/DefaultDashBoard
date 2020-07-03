package org.droidtv.defaultdashboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

/**
 * Created by bhargava.gugamsetty on 16-10-2018.
 */

public class GuestCheckInStatusChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DdbLogUtility.logCommon("GuestCheckInStatusChangeReceiver", "intent " + intent);
        DashboardDataManager.getInstance().notifyGuestCheckInStatusChanged(intent.getAction());

    }
}
