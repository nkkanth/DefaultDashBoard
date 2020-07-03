package org.droidtv.defaultdashboard.receiver;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

/**
 * Created by bhargava.gugamsetty on 09-01-2018.
 */

public class AccountChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DdbLogUtility.logCommon("AccountChangeReceiver", "intent " + intent.getAction());
        if (AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION.equals(intent.getAction())) {
            DashboardDataManager.getInstance().notifyAccountChanged();
        }
    }
}
