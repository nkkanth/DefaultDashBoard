package org.droidtv.defaultdashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import com.philips.professionaldisplaysolutions.jedi.applicationControl.IAppActivateAndDeactivateControl;

import org.droidtv.defaultdashboard.common.ApplicationControlServiceBinder;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.ui.fragment.DashboardFragment;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.htv.japit.IJapitApplicationControl;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static org.droidtv.defaultdashboard.util.Constants.ACTION_OSD_SUPPRESS;
import static org.droidtv.defaultdashboard.util.Constants.RC_SIGN_IN;

import org.droidtv.defaultdashboard.log.DdbLogUtility;

/**
 * Created by sandeep on 30-09-2017.
 */

public class DashboardActivity extends FragmentActivity implements IAppActivateAndDeactivateControl.IAppActivateAndDeactivateCallback {

    private static final String TAG = "DashboardActivity";
    private final String TAG_DASHBOARD_FRAGMENT = "DashboardFragment";
    DashboardFragment mDashboardFragment = null;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "DispatchKeyEvent for keycode " + event.getKeyCode());
        }
        return super.dispatchKeyEvent(event);
    }

    ApplicationControlServiceBinder applicationControlServiceBinder = null;
    private String DASHBOARD_APPS_ACTION = "org.droidtv.intent.action.APPS";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "#### enter onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        addDashboardFragment();
        applicationControlServiceBinder = ApplicationControlServiceBinder.getInstance(getApplicationContext());
        sendOnApplicationCreated(getIntent().getAction());
        registerOsdSuppressBroadcastReceiver();
        registerStandbyReceiver();
        registerApplicationControlReceiver();
        initSWOFLaunchMode();
        Log.d(TAG, "#### exit onCreate");
    }

    private void initSWOFLaunchMode() {
        boolean isSWOFMode = getIntent().getBooleanExtra(Constants.EXTRA_LAUNCH_MODE_SWOF, false);
        boolean isColdBoot = getIntent().getBooleanExtra(Constants.EXTRA_LAUNCH_MODE_SWOF_REASON_COLD_BOOT, false);
        Log.d(TAG, "initSWOFLaunchMode:isSWOFMode " + isSWOFMode + " isColdBoot " + isColdBoot);
        DashboardDataManager.getInstance().setGoogleSignInAfter2Min(isSWOFMode && isColdBoot);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "#### enter onNewIntent");
        super.onNewIntent(intent);
        if (applicationControlServiceBinder == null) {
            applicationControlServiceBinder = ApplicationControlServiceBinder.getInstance(getApplicationContext());
        }
        sendOnApplicationCreated(intent.getAction());
       // setApplicationAvailability(intent.getAction());
        setIntent(intent);
        Log.d(TAG, "#### exit onNewIntent");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "#### enter onStart");
        super.onStart();
        setApplicationAvailability(getIntent().getAction());
        Log.d(TAG, "#### exit onStart");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "#### enter onResume");
        super.onResume();
        Log.d(TAG, "#### exit onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "#### enter onPause is finsishing : " +isFinishing());
        super.onPause();
        Log.d(TAG, "#### exit onPause");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "#### enter onStop isFinishing "+ isFinishing());
        super.onStop();
        Log.d(TAG, "#### exit onStop");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "#### enter onDestroy action " + getIntent().getAction());
        removeDashboardFragment();
        sendOnApplicationDestroyed(getIntent().getAction());
        unregisterOsdSuppressBroadcastReceiver();
        unregisterStandbyReceiver();
        unregisterApplicationControlReceiver();
        applicationControlServiceBinder = null;
        //cleanCachedMemory();
        super.onDestroy();
        Log.d(TAG, "#### exit onDestroy");
    }

    private void cleanCachedMemory() {
        DashboardDataManager.getInstance().clearChannelLogoCache();
        DashboardDataManager.getInstance().clearProgramThumbnailCache();
    }

    private void replaceDashboardFragment(){
        removeDashboardFragment();
        addDashboardFragment();
    }

    private void addDashboardFragment() {
        mDashboardFragment = new DashboardFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.container_dashboard_fragment, mDashboardFragment, TAG_DASHBOARD_FRAGMENT).commitNow();
    }

    private void removeDashboardFragment(){
        Log.d(TAG, "removeDashboardFragment: success, mDashboardFragment " + mDashboardFragment);
        if(mDashboardFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mDashboardFragment).commitAllowingStateLoss();
            mDashboardFragment = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            DashboardDataManager.getInstance().startGoogleAccountFlow();
            return;
        } else if (requestCode == Constants.REQUEST_CODE_ACTIVITY_GATEWAY_PAGE) {
            if (resultCode == RESULT_CANCELED) {
                DashboardDataManager.getInstance().showSidePanel();
            } else if (resultCode == RESULT_OK) {
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION_SHOW_CAST_READY_SCREEN));
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged() called with: newConfig = [" + newConfig + "]");
        Intent newIntent = new Intent();
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.setAction(Constants.INTENT_ACTION_DEFAULT_DASHBOARD);
        newIntent.addCategory(Intent.CATEGORY_DEFAULT);
        setIntent(newIntent);
        replaceDashboardFragment();
    }

    @Override
    public void deactivateApplication() {
        Log.d("DDB", "deactivateApplication called for DDB");
        if(!isFinishing()) {
            finish();
        }
    }

    private void sendOnApplicationCreated(String action) {
        if (action.equals(DASHBOARD_APPS_ACTION)) {
            applicationControlServiceBinder.onApplicationCreated(IJapitApplicationControl.ApplicationType.APP_TYPE_SMARTTV);
        } else {
            applicationControlServiceBinder.onApplicationCreated(IJapitApplicationControl.ApplicationType.APP_TYPE_DEFAULT_DASHBOARD);
        }
    }

    private void setApplicationAvailability(String action) {
        Log.d(TAG, "setApplicationAvailability with action: " + action);
        if (applicationControlServiceBinder == null) {
            applicationControlServiceBinder = ApplicationControlServiceBinder.getInstance(getApplicationContext());
        }
        if (action.equals(DASHBOARD_APPS_ACTION)) {
            applicationControlServiceBinder.setApplicationAvailability(IJapitApplicationControl.ApplicationType.APP_TYPE_SMARTTV, true);
            applicationControlServiceBinder.setApplicationAvailability(IJapitApplicationControl.ApplicationType.APP_TYPE_DEFAULT_DASHBOARD, false);
        } else {
            applicationControlServiceBinder.setApplicationAvailability(IJapitApplicationControl.ApplicationType.APP_TYPE_SMARTTV, false);
            applicationControlServiceBinder.setApplicationAvailability(IJapitApplicationControl.ApplicationType.APP_TYPE_DEFAULT_DASHBOARD, true);
        }
    }

    private void sendOnApplicationDestroyed(String action) {
        if (action.equals(DASHBOARD_APPS_ACTION)) {
            applicationControlServiceBinder.onApplicationDestroyed(IJapitApplicationControl.ApplicationType.APP_TYPE_SMARTTV);
        } else {
            applicationControlServiceBinder.onApplicationDestroyed(IJapitApplicationControl.ApplicationType.APP_TYPE_DEFAULT_DASHBOARD);
        }
    }

    private void registerOsdSuppressBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_OSD_SUPPRESS);
        registerReceiverAsUser(mOsdSuppressBroadcastReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    private void unregisterOsdSuppressBroadcastReceiver() {
        try {
            unregisterReceiver(mOsdSuppressBroadcastReceiver);
            mOsdSuppressBroadcastReceiver = null;
        } catch (Exception e) {
            // Receiver may not be registered. Do nothing
        }
    }

    private BroadcastReceiver mOsdSuppressBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            moveTaskToBack(false);
        }
    };

    private void registerStandbyReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_STANDBY);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mStandbyBroadcastReceiver, intentFilter);
    }

    private void unregisterStandbyReceiver() {
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mStandbyBroadcastReceiver);
            mStandbyBroadcastReceiver = null;
        } catch (Exception e) {
            // Receiver may not be registered. Do nothing
        }
    }

    private BroadcastReceiver mStandbyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            if(!isFinishing()) {
                finish();
            }
        }
    };

    private void registerApplicationControlReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_STOP_DDB);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mStopDDBReceiver, intentFilter);
    }

    private void unregisterApplicationControlReceiver() {
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mStopDDBReceiver);
            mStopDDBReceiver = null;
        } catch (Exception e) {
            // Receiver may not be registered. Do nothing
        }
    }



    private BroadcastReceiver mStopDDBReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            if(!isFinishing()) {
                finish();
            }
        }
    };

}
