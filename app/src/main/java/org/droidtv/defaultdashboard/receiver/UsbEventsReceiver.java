package org.droidtv.defaultdashboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class UsbEventsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DdbLogUtility.logCommon("UsbEventsReceiver", "intent " + intent);
        Intent localIntent = new Intent();
        if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
            DashboardDataManager.getInstance().setMediaScannerInProgress(true);
            localIntent.setAction(Constants.LOCAL_MEDIA_SCANNER_STARTED);
        } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
            DashboardDataManager.getInstance().setMediaScannerInProgress(false);
            localIntent.setAction(Constants.LOCAL_MEDIA_SCANNER_FINISHED);
        } else if (Constants.ACTION_USB_BREAKOUT.equals(intent.getAction())) {
            localIntent.setAction(Constants.LOCAL_ACTION_USB_BREAKOUT);
            DashboardDataManager.getInstance().setUsbConnected(false);
        } else if (Constants.ACTION_USB_BREAKIN.equals(intent.getAction())) {
            DashboardDataManager.getInstance().setUsbConnected(true);
            localIntent.setAction(Constants.LOCAL_ACTION_USB_BREAKIN);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }
}
