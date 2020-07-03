package org.droidtv.defaultdashboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

/**
 * Created by nikhil.tk on 31-01-2018.
 */

public class MessageCountChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DdbLogUtility.logCommon("MessageCountChangeReceiver", "intent " + intent);
        DashboardDataManager.getInstance().notifyMessageCountChanged();
    }
}
