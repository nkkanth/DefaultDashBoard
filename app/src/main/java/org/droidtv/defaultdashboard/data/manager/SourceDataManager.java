package org.droidtv.defaultdashboard.data.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.hardware.hdmi.HdmiControlManager;
import android.hardware.hdmi.HdmiDeviceInfo;
import android.hardware.hdmi.IHdmiControlService;
import android.hardware.hdmi.IHdmiDeviceEventListener;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;

import org.droidtv.defaultdashboard.DashboardApplication;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MyChoiceListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SourceDataListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;
import org.droidtv.tv.video.IVideoSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.droidtv.defaultdashboard.util.Constants.EXTRA_PRCS_MY_CHOICE;

/**
 * Created by sandeep.kumar on 15/11/2017.
 */

final class SourceDataManager extends ContextualObject implements MyChoiceListener {

    private static String TAG = "SourceDataManager";

    private ArrayList<Source> mSources;
    private ArrayList<WeakReference<SourceDataListener>> mSourceDataListenerRefs;
    private int mPBSMGRSelectableAVProperty;
    private boolean mIsMHLSupported = false;
    private DashboardDataManager mDashboardDataManager;
    private ITvSettingsManager mITvSettingsManager;
    private Source.Builder mBuilder;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private TvInputManager mTvInputManager;
    private UiThreadHandler mUiThreadHandler;
    private int mLastSelectedDevice;
    private static final String AIRSERVER_PACKAGE_NAME = "com.appdynamic.airserver.android.tv";
    private static final String CAST_WIZARD_PACKAGE_NAME = "org.droidtv.hotspot.castwizard";
    private static final String ACTION_CAST_WIZARD = "org.droidtv.hotspot.castwizardactivity";
    private static final String EXTRA_MODE = "mode";

    private static final int CAST_MODE_GOOGLE_CAST_WIZARD_ACTIVITY = 2010;

    private static final String EXTRA_PHYSICAL_ADDRESS = "PHYSICAL_ADDRESS";
    private static final String EXTRA_LOGICAL_ADDRESS = "LOGICAL_ADDRESS";

    private static final int DISPLAY_SOURCES_ENABLE = 1;

    SourceDataManager(Context context) {
        super(context);
        init();
    }

    private void init() {
        mDashboardDataManager = DashboardDataManager.getInstance();
        mUiThreadHandler = new UiThreadHandler(new WeakReference<SourceDataManager>(this));
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.SOURCE_THREAD_POOL_EXECUTOR);
        mSources = new ArrayList<>();
        mSourceDataListenerRefs = new ArrayList<>();
        mTvInputManager = (TvInputManager) getContext().getSystemService(Context.TV_INPUT_SERVICE);
        mTvInputManager.registerCallback(new TvInputObserver(), new Handler());
        mITvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        mPBSMGRSelectableAVProperty = mITvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SELECTABLE_AV, 0, 0);
        mIsMHLSupported = mITvSettingsManager.getInt(TvSettingsConstants.OPMHL, 0, 0) == 1;
        registerDeviceListeners();
        registerMyChoiceListener();
        registerPackageStatusBroadcastReceivers();
        updateLastSelectedDevice();
        DdbLogUtility.logVodChapter(TAG, "init() mPBSMGRSelectableAVProperty " + mPBSMGRSelectableAVProperty + " mLastSelectedDevice " + mLastSelectedDevice);
    }

    private Runnable mFetchSourcesRunnable = new Runnable() {
        @Override
        public void run() {
            mSources.clear();
            addUsbSource();
            addHdmiSources();
            addVgaSource();
            addCastSource();
            addAirServerSource();
            sortSources();
        }
    };

    void fetchAllSources() {
        mPBSMGRSelectableAVProperty = mITvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SELECTABLE_AV, 0, 0);
        mThreadPoolExecutor.execute(mFetchSourcesRunnable);
    }

    List<Source> getSources() {
        return mSources;
    }

    boolean registerSourceDataListener(SourceDataListener listener) {
        if (listener == null) {
            return false;
        }
        return mSourceDataListenerRefs.add(new WeakReference<SourceDataListener>(listener));
    }

    boolean unregisterSourceDataListener(SourceDataListener sourceDataListener) {
        if (mSourceDataListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mSourceDataListenerRefs.size(); i++) {
            WeakReference<SourceDataListener> ref = mSourceDataListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            SourceDataListener listener = ref.get();
            if (listener != null && listener.equals(sourceDataListener)) {
                return mSourceDataListenerRefs.remove(ref);
            }
        }
        return false;
    }

    String getTvInputLabel(Context context, String tvInputId) {
        TvInputInfo tvInputInfo = mTvInputManager.getTvInputInfo(tvInputId);
        if (tvInputInfo == null) {
            return null;
        }
        return tvInputInfo.loadLabel(context).toString();
    }

    Drawable getTvInputIcon(Context context, String tvInputId) {
        TvInputInfo tvInputInfo = mTvInputManager.getTvInputInfo(tvInputId);
        if (tvInputInfo == null) {
            return null;
        }
        return tvInputInfo.loadIcon(context);
    }

    Source getSource(String inputId) {
        int portid = getPortFromInputId(inputId);
        Source lSource = null;
        for (int i = 0; mSources != null && i < mSources.size(); i++) {
            Source source = mSources.get(i);
            if (source.getId().equals(inputId) || source.getHDMIPortId() == portid) {
                lSource = source;
            }
        }
        if(lSource != null){
            android.util.Log.d(TAG, "getSource: returned " +lSource.toString());
        }else{
            android.util.Log.d(TAG, "getSource: returned null");
        }
        return lSource;
    }

    public int getPortFromInputId(String inputid) {
        int hdmiPortId = 0;
        try {
            hdmiPortId = Integer.parseInt(inputid.split("HW")[1]) - DashboardDataManager.getInstance().getOffset();
        } catch (Exception e) { }
        android.util.Log.d(TAG, "getPortFromInputId: " + inputid + " hdmiPortId " + hdmiPortId);
        return hdmiPortId;
    }



    boolean isAirServerAvailable() {
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(AIRSERVER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "#### airserver not available");
        }
        return false;
    }

    boolean areSourcesEnabled() {
        return areDisplayFilterSourcesEnabled() && (isHdmi1Enabled() || isHdmi2Enabled() || isHdmi3Enabled() || isHdmi4Enabled() || isVgaEnabled() ||
                isAirServerAvailable() || mDashboardDataManager.isGoogleCastEnabled() || isUsbBrowserEnabled() || isDirectShareEnabled());
    }

    boolean areDisplayFilterSourcesEnabled() {
        return mITvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_SOURCES, 0, 0) == DISPLAY_SOURCES_ENABLE;
    }

    boolean isHdmi1Enabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_1_FLAG) != 0;
    }

    boolean isHdmi2Enabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_2_FLAG) != 0;
    }

    boolean isHdmi3Enabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_3_FLAG) != 0;
    }

    boolean isHdmi4Enabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_4_FLAG) != 0;
    }

    boolean isVgaEnabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_VGA_FLAG) != 0;
    }

    boolean isUsbBrowserEnabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_USB_BROWSER_FLAG) != 0;
    }

    boolean isDirectShareEnabled() {
        return (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_DIRECTSHARE_MEDIA_BROWSER_FLAG) != 0;
    }

    void onConfigurationChanged() {
        fetchAllSources();
    }

    void lastSelectedDeviceChanged(int device) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_LAST_SELECTED_DEVICE_CHANGED, device, -1);
        message.sendToTarget();
    }

    int getLastSelectedDevice() {
        return mLastSelectedDevice;
    }

    @Override
    public void onMyChoiceDataChanged() {
        fetchAllSources();
    }

    @Override
    public void onMyChoiceEnabledStateChanged() {
        fetchAllSources();
    }

    @Override
    public void onMyChoiceEnabledStateChanged(int value) {
        fetchAllSources();
    }

    @Override
    public void onMyChoiceLockStatusChanged() {
        fetchAllSources();
    }

    private void registerDeviceListeners() {
        try {
            IHdmiControlService.Stub.asInterface(ServiceManager.getService(Context.HDMI_CONTROL_SERVICE)).addDeviceEventListener(new HDMIDeviceEventListener());
        } catch (RemoteException e) {
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        IVideoSource.Instance.getInterface().registerIVideoSourceChangeListener(new VideoSourceChangedListener());
    }

    private void registerMyChoiceListener() {
        DashboardDataManager.getInstance().addMyChoiceListener(this);
    }

    private void registerPackageStatusBroadcastReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiverAsUser(mPackageStatusBroadcastReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    private BroadcastReceiver mPackageStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action) || Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (AIRSERVER_PACKAGE_NAME.equals(packageName)) {
                    sourcesChanged();
                }
                return;
            }
        }
    };

    private void updateLastSelectedDevice() {
        mLastSelectedDevice = mITvSettingsManager.getInt(TvSettingsConstants.LASTSELECTEDDEVICE, 0, 0);
    }

    private boolean isHdmiEnabled(int hdmiPortId) {
        switch (hdmiPortId) {
            case 1:
                if(isBedSideTv()){
                    return false;
                }
                return ((mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_1_FLAG) != 0);
            case 2:
                if(isBedSideTv()){
                    return (((mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_1_FLAG) != 0) ||
                            (mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_2_FLAG) != 0);
                }
                return ((mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_2_FLAG) != 0);
            case 3:
                return ((mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_3_FLAG) != 0);
            case 4:
                return ((mPBSMGRSelectableAVProperty & TvSettingsDefinitions.PbsSelectableAvFlag.PBSMGR_SELECTABLE_AV_HDMI_4_FLAG) != 0);
        }
        return false;
    }

    private boolean isBedSideTv(){
        return DashboardApplication.getInstance().isBedSideTv();
    }

    private boolean isAnyHdmiInputEnabled() {
        return isHdmi1Enabled() || isHdmi2Enabled() || isHdmi3Enabled() || isHdmi4Enabled();
    }

    private void addCastSource() {
        if (mDashboardDataManager.isGoogleCastEnabled()) {
            mBuilder = new Source.Builder(Source.CAST_SOURCE_INPUT_ID);
            mBuilder.setLabel(getContext().getString(org.droidtv.ui.strings.R.string.MAIN_CAST)).
                    setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_CAST_TEASER_TEXT_1)).
                    setType(Source.SourceType.CAST).
                    setLaunchIntent(getCastWizardLaunchIntent()).
                    setIcon(getContext().getDrawable(R.drawable.icon_1025_cast_n_48x48));
            mSources.add(mBuilder.build());
            DdbLogUtility.logCommon(TAG, "addCastSource done");
        }
    }

    private void addAirServerSource() {
        Intent launchIntent = null;
        Drawable icon = null;
        String description = getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_AIRSERVER_APP);
        if (isAirServerAvailable()) {
            mBuilder = new Source.Builder(Source.AIRSERVER_SOURCE_INPUT_ID);
            mBuilder.setLabel(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_AIRSERVER_APP)).
                    setType(Source.SourceType.AIRSERVER);
            try {
                PackageManager packageManager = getContext().getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(AIRSERVER_PACKAGE_NAME, PackageManager.GET_META_DATA);
                launchIntent = packageManager.getLeanbackLaunchIntentForPackage(AIRSERVER_PACKAGE_NAME);
                if (launchIntent == null) {
                    launchIntent = packageManager.getLaunchIntentForPackage(AIRSERVER_PACKAGE_NAME);
                }
                icon = packageManager.getApplicationBanner(applicationInfo);
                if (icon == null) {
                    icon = packageManager.getApplicationIcon(applicationInfo);
                }
                CharSequence desc = applicationInfo.loadDescription(packageManager);
                if (!TextUtils.isEmpty(desc)) {
                    description = desc.toString();
                }else{
                    description = "";
                }
                mBuilder.setLaunchIntent(launchIntent).
                        setIcon(icon).
                        setDescription(description);
                mSources.add(mBuilder.build());
                DdbLogUtility.logCommon(TAG, "addAirServerSource done");
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "#### airserver package not found.");
            }
        }
    }

    private void addUsbSource() {		
        if (isUsbBrowserEnabled() || isDirectShareEnabled()) {
            mBuilder = new Source.Builder(Source.VIRTUAL_SOURCE_USB_ID);
            mBuilder.setLabel(getContext().getString(org.droidtv.ui.strings.R.string.MAIN_USB)).
                    setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_USB_SOURCE_DESCRIPTION)).
                    setType(Source.SourceType.VIRTUAL).
                    setLaunchIntent(new Intent(Source.SOURCE_INTENT_BROWSE_USB)).
                    setIcon(getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.browse_usb_n_ico_40x30_39));
            mSources.add(mBuilder.build());
            DdbLogUtility.logCommon(TAG, "addUsbSource() done");
        }
    }

    private void addHdmiSources() {
        List<TvInputInfo> tvInputInfos = mTvInputManager.getTvInputList();
        for (TvInputInfo tvInputInfo : tvInputInfos) {
            if (tvInputInfo.getType() == TvInputInfo.TYPE_HDMI) {
                removeAndAddHdmiSource(tvInputInfo);
            }
        }
    }

    private void removeAndAddHdmiSource(TvInputInfo tvInputInfo) {
        HdmiDeviceInfo hdmiDeviceInfo = tvInputInfo.getHdmiDeviceInfo();
        int index = 0;
        if (hdmiDeviceInfo != null) {
            for (int i = 0; i < mSources.size(); i++) {
                Source source = mSources.get(i);
                if (source.getHDMIPortId() == hdmiDeviceInfo.getPortId() && source.getId().contains("HW")) {
                    index = i;
                    mSources.remove(index);
                    break;
                }
            }
        }
        Source source = createHdmiSource(tvInputInfo);
    
        
        if (source != null && isHdmiEnabled(source.getHDMIPortId())) {
            mSources.add(index, source);
			DdbLogUtility.logCommon(TAG, "removeAndAddHdmiSource() isHDMIEnabled  portid " + source.getHDMIPortId());
        }
    }

    private Source createHdmiSource(TvInputInfo tvInputInfo) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("org.droidtv.playtv");
        mBuilder = new Source.Builder(tvInputInfo.getId());
        HdmiDeviceInfo hdmiDeviceInfo = tvInputInfo.getHdmiDeviceInfo();
        String label = tvInputInfo.loadLabel(getContext()).toString();
        mBuilder.setDescription("");
        if (hdmiDeviceInfo != null) {
            if (isHdmiSourceAdded(hdmiDeviceInfo.getPortId())) {
                return null;
            }
            intent.putExtra(EXTRA_PHYSICAL_ADDRESS, tvInputInfo.getHdmiDeviceInfo().getPhysicalAddress());
            intent.putExtra(EXTRA_LOGICAL_ADDRESS, tvInputInfo.getHdmiDeviceInfo().getLogicalAddress());
            if (hdmiDeviceInfo.getPortId() == 1) {
                if((mITvSettingsManager.getInt(TvSettingsConstants.OPMHL, 0, 0) == 1)) {
                    mBuilder.setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                }else{
                    mBuilder.setDescription("");
                }
            }
            mBuilder.setIcon(getContext().getDrawable(getHDMIDeviceIconId(hdmiDeviceInfo.getDeviceType())))
                    .setHdmiDeviceLabel(hdmiDeviceInfo.getDisplayName())
                    .setHdmiPortId(hdmiDeviceInfo.getPortId());
        } else {
            int hdmiPortId = Integer.parseInt(tvInputInfo.getId().split("HW")[1]) - DashboardDataManager.getInstance().getOffset();
            if (isHdmiSourceAdded(hdmiPortId)) {
                return null;
            }
            //bharat
            if((hdmiPortId == Constants.HDMI1_MHL_PORT_ID)){
                if(mIsMHLSupported) {
                    label = getContext().getResources().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1_MHL);
                    mBuilder.setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                }else{
                    label = getContext().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1);
                    mBuilder.setDescription("");
                }
            }
            mBuilder.setIcon(getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_203_hdmi_n_48x48))
                    .setHdmiPortId(hdmiPortId);
        }
        mBuilder.setLabel(label);
        intent.setData(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.getId()));
        mBuilder.setType(tvInputInfo.getType()).setLaunchIntent(intent);
        Source source =  mBuilder.build();
        DdbLogUtility.logCommon(TAG, "createHdmiSource " + source.toString());
        return source;
    }

    private boolean isHdmiSourceAdded(int hdmiPortId) {
        for (Source source : mSources) {
            if (source.getHDMIPortId() == hdmiPortId) {
                DdbLogUtility.logCommon(TAG, "isHdmiSourceAdded true");
                return true;
            }
        }
        return false;
    }

    private void addVgaSource() {
        if (isVgaEnabled()) {
            List<TvInputInfo> tvInputInfos = mTvInputManager.getTvInputList();
            for (TvInputInfo tvInputInfo : tvInputInfos) {
                if (tvInputInfo.getType() == TvInputInfo.TYPE_VGA) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("org.droidtv.playtv");
                    intent.setData(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.getId()));
                    mBuilder = new Source.Builder(tvInputInfo.getId());
                    mBuilder.setLabel((String) tvInputInfo.loadLabel(getContext()))
                            .setDescription("")
                            .setType(tvInputInfo.getType())
                            .setLaunchIntent(intent).
                            setIcon(getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_206_vga_n_48x48));
                    mSources.add(mBuilder.build());
                    DdbLogUtility.logCommon(TAG, "addVgaSource done");
                    break;
                }
            }
        }
    }

    private void sortSources() {
        Collections.sort(mSources, new SourcesComparator());
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_SOURCES_FETCH_COMPLETE);
    }

    private Intent getCastWizardLaunchIntent() {
        Intent launchIntent = new Intent(ACTION_CAST_WIZARD);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.putExtra(EXTRA_MODE, CAST_MODE_GOOGLE_CAST_WIZARD_ACTIVITY);
        launchIntent.putExtra(EXTRA_PRCS_MY_CHOICE, "");
        return launchIntent;
    }

    private int getHDMIDeviceIconId(int hdmiDeviceType) {
        switch (hdmiDeviceType) {
            case HdmiDeviceInfo.DEVICE_RECORDER:
                return org.droidtv.ui.tvwidget2k15.R.drawable.satellite_source_n_ico_40x30_41;
            case HdmiDeviceInfo.DEVICE_TUNER:
                return org.droidtv.ui.tvwidget2k15.R.drawable.digital_setup_box_n_ico_40x30_63;
            case HdmiDeviceInfo.DEVICE_PLAYBACK:
                return org.droidtv.ui.tvwidget2k15.R.drawable.av_disc_player_n_ico_40x30_52;
            case HdmiDeviceInfo.DEVICE_AUDIO_SYSTEM:
                return org.droidtv.ui.tvwidget2k15.R.drawable.icon_210_audio_l_r_n_48x48;
        }
        return org.droidtv.ui.tvwidget2k15.R.drawable.unspecified_device_n_ico_40x30_68;
    }

    private class TvInputObserver extends TvInputManager.TvInputCallback {
        @Override
        public void onInputAdded(String inputId) {
            DdbLogUtility.logCommon(TAG, "onInputAdded() called with: inputId = [" + inputId + "]");
            super.onInputAdded(inputId);
            boolean isSourceAdded = false;
            TvInputInfo tvInputInfo = mTvInputManager.getTvInputInfo(inputId);
            if (tvInputInfo == null) {
                return;
            }
            if (tvInputInfo.getType() == TvInputInfo.TYPE_HDMI && isAnyHdmiInputEnabled()) {
                for (Source source : mSources) {
                    if (source.getType() == TvInputInfo.TYPE_HDMI) {
                        isSourceAdded = true;
                        break;
                    }
                }
                Source source = null;
                if (!isSourceAdded) {
                    mBuilder = new Source.Builder(tvInputInfo.getId());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("org.droidtv.playtv");
                    intent.setData(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.getId()));
                    if(tvInputInfo.getHdmiDeviceInfo() !=  null) {
                        intent.putExtra(EXTRA_PHYSICAL_ADDRESS, tvInputInfo.getHdmiDeviceInfo().getPhysicalAddress());
                        intent.putExtra(EXTRA_LOGICAL_ADDRESS, tvInputInfo.getHdmiDeviceInfo().getLogicalAddress());
                    }
                    String label = tvInputInfo.loadLabel(getContext()).toString();
                    //bharat
                    if (tvInputInfo.getHdmiDeviceInfo() != null && tvInputInfo.getHdmiDeviceInfo().getPortId() == Constants.HDMI1_MHL_PORT_ID) {
                        if(mIsMHLSupported){
                            label = getContext().getResources().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1_MHL);
                            mBuilder.setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                        }else{
                            label = getContext().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1);
                            mBuilder.setDescription("");
                        }
                    }else{
                        mBuilder.setDescription("");
                    }
                    mBuilder.setLabel(label).
                            setType(tvInputInfo.getType()).
                            setLaunchIntent(intent).
                            setIcon(getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_203_hdmi_n_48x48));
                    source = mBuilder.build();
                    mSources.add(source);
                    sortSources();
                }
            } else if (tvInputInfo.getType() == TvInputInfo.TYPE_VGA && isVgaEnabled()) {
                for (Source source : mSources) {
                    if (source.getType() == TvInputInfo.TYPE_VGA) {
                        isSourceAdded = true;
                        break;
                    }
                }
                if (!isSourceAdded) {
                    mBuilder = new Source.Builder(tvInputInfo.getId());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("org.droidtv.playtv");
                    intent.setData(TvContract.buildChannelUriForPassthroughInput(tvInputInfo.getId()));
                    String label = tvInputInfo.loadLabel(getContext()).toString();
                    //bharat
                    /*Set Description only if the port ID = 1 HDMI1/MHL*/
                    if (tvInputInfo.getHdmiDeviceInfo() != null && tvInputInfo.getHdmiDeviceInfo().getPortId() == Constants.HDMI1_MHL_PORT_ID) {
                        if(mIsMHLSupported){
                            label = getContext().getResources().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1_MHL);
                            mBuilder.setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                        }else{
                            label = getContext().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1);
                            mBuilder.setDescription("");
                        }
                    }else{
                        mBuilder.setDescription("");
                    }

                    mBuilder.setLabel(label).
                            setType(tvInputInfo.getType()).
                            setLaunchIntent(intent).
                            setIcon(getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_206_vga_n_48x48));
                    Source source = mBuilder.build();
                    mSources.add(source);
                    sortSources();
                }
            }
        }

        @Override
        public void onInputRemoved(String inputId) {
            DdbLogUtility.logCommon(TAG, "onInputRemoved() called with: inputId = [" + inputId + "]");
            super.onInputRemoved(inputId);
        }
    }

    private class HDMIDeviceEventListener extends IHdmiDeviceEventListener.Stub {
        @Override
        public void onStatusChanged(HdmiDeviceInfo hdmiDeviceInfo, int status) throws RemoteException {
            Log.d(TAG, "onStatusChanged() called with: status = " + status);
            if (status == HdmiControlManager.DEVICE_EVENT_ADD_DEVICE || status == HdmiControlManager.DEVICE_EVENT_UPDATE_DEVICE) {
                Message message = Message.obtain();
                message.what = UiThreadHandler.MSG_WHAT_UPDATE_HDMI;
                message.obj = hdmiDeviceInfo;
                mUiThreadHandler.sendMessage(message);
            } else if (status == HdmiControlManager.DEVICE_EVENT_REMOVE_DEVICE) {
                Message message = Message.obtain();
                message.what = UiThreadHandler.MSG_WHAT_REMOVE_HDMI;
                message.obj = hdmiDeviceInfo;
                mUiThreadHandler.sendMessage(message);
            }
        }
    }

    private void updateHdmiSource(HdmiDeviceInfo hdmiDeviceInfo) {
        try {
            for (int i = 0; i < mSources.size(); i++) {
                Source source = mSources.get(i);
                if (source.getHDMIPortId() == hdmiDeviceInfo.getPortId()) {
                    int index = i;
                    mBuilder = new Source.Builder(source.getId());
                    //bharat
                    if (hdmiDeviceInfo.getPortId() == Constants.HDMI1_MHL_PORT_ID) {
                        if(mIsMHLSupported){
                            mBuilder.setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                        }else{
                            mBuilder.setDescription("");
                        }
                    }else{
                        mBuilder.setDescription("");
                    }
                   mBuilder.setLabel(hdmiDeviceInfo.getDisplayName())
                           .setHdmiDeviceLabel(hdmiDeviceInfo.getDisplayName())
                            .setType(source.getType()).setLaunchIntent(source.getLaunchIntent()).setHdmiPortId(hdmiDeviceInfo.getPortId())
                            .setIcon(getContext().getDrawable(getHDMIDeviceIconId(hdmiDeviceInfo.getDeviceType())));
                    Source updatedSource = mBuilder.build();
                    mSources.set(index, updatedSource);
                    notifySourceUpdatedComplete(index, updatedSource);
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "#### updateHdmiSource.IndexOutOfBounds");
        }
    }

    private void removeHdmiSource(HdmiDeviceInfo hdmiDeviceInfo) {
        try {
            for (int i = 0; i < mSources.size(); i++) {
                Source source = mSources.get(i);
                if (source.getHDMIPortId() == hdmiDeviceInfo.getPortId()) {
                    int index = i;
                    TvInputInfo tvInputInfo = findTvInputInfoByHdmiPortId(source.getHDMIPortId());
                    mBuilder = new Source.Builder(source.getId());
                    //bharat
                    if (hdmiDeviceInfo.getPortId() == Constants.HDMI1_MHL_PORT_ID) {
                        if(mIsMHLSupported){
                            mBuilder.setDescription(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                        }else{
                            mBuilder.setDescription("");
                        }
                    }else {
                        mBuilder.setDescription("");
                    }
                   mBuilder.setLabel(findDefaultLabelByPortId(source.getHDMIPortId())).
                            setType(source.getType()).
                            setLaunchIntent(source.getLaunchIntent()).
                            setIcon(getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_203_hdmi_n_48x48)).
                            setHdmiPortId(source.getHDMIPortId());
                    Source updatedSource = mBuilder.build();
                    mSources.set(index, updatedSource);
                    notifySourceUpdatedComplete(index, updatedSource);
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "#### removeHdmiSource.IndexOutOfBounds");
        }
    }

    private String findDefaultLabelByPortId(int portID){
        switch(portID){
            case 1 :
                //bharat
                if(mIsMHLSupported){
                    return getContext().getResources().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1_MHL);
                }else{
                    return getContext().getString(org.droidtv.ui.strings.R.string.MISC_HDMI1);
                }
            case 2:
                return getContext().getResources().getString(org.droidtv.ui.strings.R.string.MISC_HDMI2);
            case 3:
                return getContext().getResources().getString(org.droidtv.ui.strings.R.string.MISC_HDMI3);
        }
        return  "";
    }

    private TvInputInfo findTvInputInfoByHdmiPortId(int portId) {
        DdbLogUtility.logCommon(TAG, "findTvInputInfoByHdmiPortId  " + portId);
        List<TvInputInfo> tvInputInfos = mTvInputManager.getTvInputList();
        for (int i = 0; i < tvInputInfos.size(); i++) {
            TvInputInfo tvInputInfo = tvInputInfos.get(i);
            if (tvInputInfo.getType() == TvInputInfo.TYPE_HDMI) {
                HdmiDeviceInfo hdmiDeviceInfo = tvInputInfo.getHdmiDeviceInfo();
                if (hdmiDeviceInfo != null) {
                    if (hdmiDeviceInfo.getPortId() == portId) {
                        return tvInputInfo;
                    }
                    continue;
                }

                int hdmiPortId = Integer.parseInt(tvInputInfo.getId().split("HW")[1]) - DashboardDataManager.getInstance().getOffset();
                if (hdmiPortId == portId) {
                    return tvInputInfo;
                }
            }
        }
        return null;
    }

    private class VideoSourceChangedListener extends IVideoSource.IVideoSourceChangedListener {
        @Override
        public void onSourceStatusChanged(int source, boolean status) {
            super.onSourceStatusChanged(source, status);
        }
    }

    private class SourcesComparator implements Comparator<Source> {
        @Override
        public int compare(Source o1, Source o2) {
            if (Source.VIRTUAL_SOURCE_USB_ID.equals(o1.getId())) {
                return -1;
            }

            if (Source.VIRTUAL_SOURCE_USB_ID.equals(o2.getId())) {
                return 1;
            }

            if (o1.getType() == Source.SourceType.HDMI) {
                if (o2.getType() == Source.SourceType.HDMI) {
                    return o1.getHDMIPortId() - o2.getHDMIPortId();
                } else if (o2.getType() == Source.SourceType.VGA) {
                    return -1;
                } else if (Source.CAST_SOURCE_INPUT_ID.equals(o2.getId()) || Source.AIRSERVER_SOURCE_INPUT_ID.equals(o2.getId())) {
                    return -1;
                }
            } else if (o1.getType() == Source.SourceType.VGA) {
                if (Source.VIRTUAL_SOURCE_USB_ID.equals(o2.getId()) || o2.getType() == Source.SourceType.HDMI) {
                    return 1;
                }
                if (Source.CAST_SOURCE_INPUT_ID.equals(o2.getId()) || Source.AIRSERVER_SOURCE_INPUT_ID.equals(o2.getId())) {
                    return -1;
                }
            } else if (Source.CAST_SOURCE_INPUT_ID.equals(o1.getId())) {
                if (Source.VIRTUAL_SOURCE_USB_ID.equals(o2.getId()) || o2.getType() == Source.SourceType.HDMI || o2.getType() == Source.SourceType.VGA) {
                    return 1;
                }
                if (Source.AIRSERVER_SOURCE_INPUT_ID.equals(o2.getId())) {
                    return -1;
                }
            }

            if (Source.AIRSERVER_SOURCE_INPUT_ID.equals(o1.getId())) {
                return 1;
            }

            if (Source.VIRTUAL_SOURCE_USB_ID.equals(o2.getId())) {
                return -1;
            }

            return 0;
        }
    }



    private void notifySourceDisplayChanged(int value) {
        for (int i = 0; mSourceDataListenerRefs != null && i < mSourceDataListenerRefs.size(); i++) {
            WeakReference<SourceDataListener> listenerRef = mSourceDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SourceDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSourceDisplayChanged();
            }
        }
    }

    private void notifySourcesFetchComplete() {
        for (int i = 0; mSourceDataListenerRefs != null && i < mSourceDataListenerRefs.size(); i++) {
            WeakReference<SourceDataListener> listenerRef = mSourceDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SourceDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSourcesFetched(mSources);
            }
        }
    }

    private void notifySourceUpdatedComplete(int index, Source source) {
        for (int i = 0; mSourceDataListenerRefs != null && i < mSourceDataListenerRefs.size(); i++) {
            WeakReference<SourceDataListener> listenerRef = mSourceDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SourceDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSourceUpdated(index, source);
            }
        }
    }

    private void notifyLastSelectedDeviceChanged(int device) {
        for (int i = 0; mSourceDataListenerRefs != null && i < mSourceDataListenerRefs.size(); i++) {
            WeakReference<SourceDataListener> listenerRef = mSourceDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SourceDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onLastSelectedDeviceChanged(device);
            }
        }
    }

    void sourcesChanged() {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_REFRESH_SOURCES);
    }

    void sourcesChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_REFRESH_SOURCES_WITH_UPDATED_SELECTABLE_AV_PROPERTY);
        message.arg1 = value;
        message.sendToTarget();
    }


    void enableDisplayFilterSources(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_DISPLAY_FILTER_SOURCES);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void onSourcesChanged(int value) {
        mPBSMGRSelectableAVProperty = value;
        mThreadPoolExecutor.execute(mFetchSourcesRunnable);
    }

    private void onLastSelectedDeviceChanged(int device) {
        updateLastSelectedDevice();
        notifyLastSelectedDeviceChanged(device);
    }

    private static class UiThreadHandler extends Handler {
        private static final int MSG_WHAT_UPDATE_HDMI = 100;
        private static final int MSG_WHAT_REMOVE_HDMI = 101;
        private static final int MSG_WHAT_REFRESH_SOURCES = 102;
        private static final int MSG_WHAT_SOURCES_FETCH_COMPLETE = 103;
        private static final int MSG_WHAT_REFRESH_SOURCES_WITH_UPDATED_SELECTABLE_AV_PROPERTY = 104;
        private static final int MSG_WHAT_LAST_SELECTED_DEVICE_CHANGED = 105;
        private static final int MSG_WHAT_DISPLAY_FILTER_SOURCES = 106;
        private WeakReference<SourceDataManager> mSourceDataManagerRef;

        private UiThreadHandler(WeakReference<SourceDataManager> ref) {
            super();
            mSourceDataManagerRef = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            DdbLogUtility.logCommon(TAG, "UiThreadHandler handleMessage msg.what " + msg.what);
            SourceDataManager sourceDataManager = mSourceDataManagerRef.get();
            if (sourceDataManager == null) {
                return;
            }
            switch (msg.what) {
                case MSG_WHAT_UPDATE_HDMI:
                    sourceDataManager.updateHdmiSource((HdmiDeviceInfo) msg.obj);
                    break;
                case MSG_WHAT_REMOVE_HDMI:
                    sourceDataManager.removeHdmiSource((HdmiDeviceInfo) msg.obj);
                    break;
                case MSG_WHAT_REFRESH_SOURCES:
                    sourceDataManager.fetchAllSources();
                    break;
                case MSG_WHAT_REFRESH_SOURCES_WITH_UPDATED_SELECTABLE_AV_PROPERTY:
                    int value = msg.arg1;
                    sourceDataManager.onSourcesChanged(value);
                    break;
                case MSG_WHAT_SOURCES_FETCH_COMPLETE:
                    sourceDataManager.notifySourcesFetchComplete();
                    break;
                case MSG_WHAT_LAST_SELECTED_DEVICE_CHANGED:
                    sourceDataManager.onLastSelectedDeviceChanged(msg.arg1);
                    break;
                case MSG_WHAT_DISPLAY_FILTER_SOURCES:
                    sourceDataManager.notifySourceDisplayChanged(msg.arg1);
                    break;
            }
        }
    }
}