package org.droidtv.defaultdashboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.UserHandle;

import com.philips.professionaldisplaysolutions.jedi.IPowerStateControl;

import org.droidtv.defaultdashboard.common.ApplicationControlHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.RecommendationListenerService;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.tv.context.TvIntent;
import org.droidtv.tv.tvpower.ITvPowerManager;
import org.droidtv.tv.tvpower.ITvPowerManager.PowerStates;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/*Android-P: Need to uncomment once Power related change will be adapted in DDB*/

/**
 * Created by root on 27/10/17.
 */

public class DashboardBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "DashboardBroadcastReceiver";
    private DashboardDataManager mDashboardDataManager;

    /*Android-P: Need to remove below enum once power related changes is done after DDB -Bringup*/
    /*public enum PowerStates{POWER_STATE_UNKNOWN, POWER_STATE_BOOTING, POWER_STATE_BOOT_COMPLETED, POWER_STATE_FULL_SYSTEM_START,
                            POWER_STATE_SEMI_STANDBY, POWER_STATE_DDR_STANDBY, POWER_STATE_STANDBY, POWER_STATE_VIRTUAL_POWER_OFF};*/

    @Override
    public void onReceive(Context context, Intent intent) {
        mDashboardDataManager = DashboardDataManager.getInstance();
        Log.d(TAG, "broadcast received: " + intent);
        if (intent != null) {
            String action = intent.getAction();
            if (TvIntent.ACTION_POWER_BOOT_COMPLETED.equals(action)) {
                startRecommendationListenerService(context);
                ApplicationControlHelper.getInstance().bindToJapitApplicationControlService(context.getApplicationContext());
            } else if (TvIntent.ACTION_TV_POWER_STATE_CHANGE_BEGIN.equals(action)) {
                /*Android-P: Need to uncommnet below 2 lines once power related changes will be done*/
                PowerStates srcState = (ITvPowerManager.PowerStates) intent.getSerializableExtra(TvIntent.SOURCE_TV_POWER_MODE);
                PowerStates dstState = (ITvPowerManager.PowerStates) intent.getSerializableExtra(TvIntent.TARGET_TV_POWER_MODE);
                /*PowerStates srcState = (PowerStates) intent.getSerializableExtra(TvIntent.SOURCE_TV_POWER_MODE);
                PowerStates dstState = (PowerStates) intent.getSerializableExtra(TvIntent.TARGET_TV_POWER_MODE);*/
                Log.d(TAG, "SourcePower state: " + srcState + ",  Destination powerState: " + dstState);
                if (srcState != null && dstState != null) {
                    boolean isInTransition = false;
                    if (srcState == PowerStates.POWER_STATE_BOOTING ||
                            srcState == PowerStates.POWER_STATE_UNKNOWN ||
                            dstState == PowerStates.POWER_STATE_UNKNOWN) {
                        isInTransition = true;
                    } else {
                        isInTransition = false;
                    }

                    if (srcState == PowerStates.POWER_STATE_FULL_SYSTEM_START && dstState == PowerStates.POWER_STATE_SEMI_STANDBY) {
                        sendLocalBroadcast(context, Constants.ACTION_STANDBY);
                    }
                }

            } else if (Constants.ACTION_SMARTINFO_DOWNLOAD_COMPLETED.equals(action)) {
                sendLocalBroadcast(context, Constants.ACTION_SMARTINFO_DOWNLOAD_COMPLETED);
            } else if (Constants.ACTION_CLONE_RESULT_CDB_SMARTINFO_BANNER.equals(action) &&
                    intent.getBooleanExtra(Constants.EXTRA_SMARTINFO_CLONED_RESULT, false)) {
                sendLocalBroadcast(context, Constants.ACTION_CLONE_RESULT_CDB_SMARTINFO_BANNER);
            } else if (Constants.OEM_START_INTENT.equals(action)) {
                Intent localIntent = new Intent();
                localIntent.setAction(Constants.OEM_START_INTENT_NOTIFICATION);
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            } else if (Constants.ACTION_ANDROID_BOOT_COMPLETED.equals(action)) {
                sendLocalBroadcast(context, Constants.ACTION_LOCAL_BROADCAST_BOOT_COMPLETED);
            }else if(Constants.ACTION_MTK_TV_READY.equals(action)){
                sendLocalBroadcast(context, Constants.ACTION_LOCAL_BROADCAST_BOOT_COMPLETED);
            }else if(Constants.ACTION_ANDROID_LOCALE_CHANGED.equals(action)){
                sendLocalBroadcast(context, Constants.ACTION_ANDROID_LOCALE_CHANGED);
            }


        }
    }

    private IPowerStateControl.PowerState mapPowerState(PowerStates state) {
        IPowerStateControl.PowerState jediPowerState = null;
        if (state == PowerStates.POWER_STATE_FULL_SYSTEM_START ||
                state == PowerStates.POWER_STATE_BOOTING) {
            jediPowerState = IPowerStateControl.PowerState.ON;
        } else if (state == PowerStates.POWER_STATE_SEMI_STANDBY) {
            jediPowerState = IPowerStateControl.PowerState.STANDBY;
        } else if (state == PowerStates.POWER_STATE_UNKNOWN) {
            jediPowerState = null;
        }
        DdbLogUtility.logCommon("DashboardBroadcastReceiver", "jediPowerState " + jediPowerState);
        return jediPowerState;
    }

    private void startRecommendationListenerService(Context context) {
        Intent serviceIntent = new Intent(context, RecommendationListenerService.class);
        context.startServiceAsUser(serviceIntent, UserHandle.CURRENT_OR_SELF);
    }

    private void sendLocalBroadcast(Context context, String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
