package org.droidtv.defaultdashboard.data.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.zxing.EncodeHintType;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.scheme.Wifi;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.GoogleCastListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.TNCDetails;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.htv.hotspot.SecureSharingManager;
import org.droidtv.htv.hotspot.SecureSharingManager.IHotspotListener;
import org.droidtv.htv.hotspot.SharingSettings;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

final class GoogleCastDataManager extends ContextualObject implements IHotspotListener {

    private static final String TAG = "GoogleCastDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private SecureSharingManager mSecureSharingManager;
    private SharingSettings mSharingSettings;
    private ITvSettingsManager mITvSettingsManager;
    private ArrayList<WeakReference<GoogleCastListener>> mGoogleCastListenerRefs;
    private boolean mIsGoogleCastEnabled;
    private boolean mIsTNCEnabled;

    private boolean mHotspotInitialized;
    private boolean mHotspotStarted;
    private String mNetworkName;
    private String mPassword;
    private CastLocalBroadCastReceiver mReceiver;
    private DashboardDataManager mDashboardDataManager;
    private TNCDetails tncDetails = null;
    GoogleCastDataManager(Context context) {
        super(context);
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.GOOGLE_CAST_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler(new WeakReference<GoogleCastDataManager>(this));
        mSharingSettings = SharingSettings.getInstance();
        mGoogleCastListenerRefs = new ArrayList<>();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mITvSettingsManager = mDashboardDataManager.getTvSettingsManager();
        mSecureSharingManager = new SecureSharingManager(getContext(), this);
        mReceiver = new CastLocalBroadCastReceiver();
        mHotspotInitialized = false;
        mHotspotStarted = false;
        updateGoogleCastEnabledState();
        registerLocalBootCompleteReceiver();
        registerCloneInReceiver();
        registerLocaleChangeListner();
        updateTNCEnabledState();
    }

    private void updateTNCEnabledState() {
        mIsTNCEnabled = mITvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SHARING_SHOW_TNC, 0, 0) == 1;
    }

    public boolean isTNCEnabled() {
        boolean enabled =  mIsTNCEnabled;
        DdbLogUtility.logCastChapter(TAG, "mIsTNCEnabled() " + enabled);
        return enabled;
    }

    boolean isGoogleCastEnabled() {
        boolean enabled =  mIsGoogleCastEnabled && mDashboardDataManager.isProfessionalModeEnabled();
        DdbLogUtility.logCastChapter(TAG, "isGoogleCastEnabled() " + enabled);
        return enabled;
    }

    boolean registerGoogleCastListener(GoogleCastListener googleCastListener) {
        if (googleCastListener == null) {
            return false;
        }
        return mGoogleCastListenerRefs.add(new WeakReference<GoogleCastListener>(googleCastListener));
    }

    boolean unregisterGoogleCastListener(GoogleCastListener googleCastListener) {
        if (mGoogleCastListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null && listener.equals(googleCastListener)) {
                return mGoogleCastListenerRefs.remove(ref);
            }
        }
        return false;
    }

    void initHotspot() {
        DdbLogUtility.logCastChapter(TAG, "initHotspot: mHotspotInitialized " + mHotspotInitialized);
        if (mHotspotInitialized) {
            // Hotspot is already initialized. Just invoke the callback that
            // is invoked when hotspot is initialized
            onHotspotInit();
        } else {
            mThreadPoolExecutor.execute(mInitHotspotRunnable);
        }
    }

    void deinitHotspot() {
        mThreadPoolExecutor.execute(mDeinitHotspotRunnable);
    }

    void startHotspot() {
        if (mHotspotStarted) {
            // Hotspot is already started. Just invoke the callback that
            // is invoked when hotspot is started
            onHotspotStarted(mNetworkName, mPassword);
        } else {
            mThreadPoolExecutor.execute(mStartHotspotRunnable);
        }
    }

    void stopHotspot() {
        mThreadPoolExecutor.execute(mStopHotspotRunnable);
    }

    boolean isSecureSharingEnabled() {
        return mSharingSettings.isSecureSharingEnabled();
    }

    boolean isGoogleCastWifiLoginEnabled() {
        return mSharingSettings.getGoogleCastSharingSetting() == 0;
    }

    String getWifiNetworkNameForGoogleCast() {
        return mSharingSettings.getWifiNetworkName();
    }

    String getGatewayUrlForGoogleCast() {
        return mSharingSettings.getGatewayURL();
    }

    boolean isHotspotCompatibilityMode() {
        return mSharingSettings.isDisplayCompatibilityMode(getContext());
    }

    void setHotspotCompatibityMode(boolean compatibityMode) {
        mSecureSharingManager.setCompatibilityMode(compatibityMode);
    }

    void setTcStatus(String macAddr, int termsStatus){
        DdbLogUtility.logCastChapter(TAG ,"setTcStatus :"+macAddr +" status :"+termsStatus);
        mSecureSharingManager.setTcStatus(macAddr , termsStatus);
    }

    boolean getTcStatus(String macAddr) {
        boolean mTNCStatus = mSecureSharingManager.getTcStatus(macAddr);
        DdbLogUtility.logCastChapter(TAG ,"GetTcStatus for:"+macAddr +" status :"+mTNCStatus);
        return mTNCStatus;
    }

    public void onSettingChanged(int property ,int value){
        DdbLogUtility.log(TAG ,"onSettingChanged :"+property + " Value:"+value);
        if(property == TvSettingsConstants.PBSMGR_PROPERTY_SHARING_SHOW_TNC){
            Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_TNC_ENABLED_STATE_SETTING_CHANGED);
            message.arg1 = value;
            message.sendToTarget();
        }else if(property == TvSettingsConstants.PBSMGR_PROPERTY_HTV_SWITCH_ON_GUEST_MANAGEMENT_ANDROID_SYSTEM_MENU_LANGUAGE){
            Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_FETCH_TNC );
            message.arg1 = value;
            message.sendToTarget();
        }
    }

    boolean isHotspotDefaultFrequency5Ghz() {
        return mSharingSettings.getHotspotDefaultFrequency() == 1;
    }

    boolean canInitHotspot() {
        return !mHotspotInitialized;
    }

    boolean canStartHotspot() {
        return !mHotspotStarted;
    }

    List<String> getClientDeviceNameList(){
        return mSecureSharingManager.getClientDeviceNameList();
    }

    void onGoogleCastEnabledStateSettingChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_GOOGLE_CAST_ENABLED_STATE_SETTING_CHANGED);
        message.sendToTarget();
    }

    void onGoogleCastEnabledStateSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_GOOGLE_CAST_ENABLED_STATE_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void onTNCStateSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_GOOGLE_CAST_ENABLED_STATE_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    @Override
    public void onHotspotInit() {
        // This callback can be on any thread. Post it to a handler of UI thread to ensure that any UI updates are done
        // on the UI thread
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_HOTSPOT_INITIALIZED);
        message.sendToTarget();
    }

    @Override
    public void onHotspotStarted(String networkName, String passphrase) {
        DdbLogUtility.logCastChapter(TAG, "onHotspotStarted networkName " + networkName);
        // Generate a QR code from networkname and passphrase
        HotspotInfoCallable callable = new HotspotInfoCallable(getContext(), networkName, passphrase);
        HotspotInfoTask task = new HotspotInfoTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    @Override
    public void onHotspotStopped() {
        DdbLogUtility.logCastChapter(TAG, "onHotspotStopped");
        // This callback can be on any thread. Post it to a handler of UI thread to ensure that any UI updates are done
        // on the UI thread
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_HOTSPOT_STOPPED);
        message.sendToTarget();
    }

    @Override
    public void onHotspotConnecting() {
        DdbLogUtility.logCastChapter(TAG, "onHotspotConnecting");
        // This callback can be on any thread. Post it to a handler of UI thread to ensure that any UI updates are done
        // on the UI thread
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_HOTSPOT_CONNECTING);
        message.sendToTarget();
    }

    @Override
    public void onHotspotConnected (String deviceName, String address, boolean isShowTerms) {
        DdbLogUtility.logCastChapter(TAG, "onHotspotConnected() called with: deviceName = [" + deviceName + "]");
        // This callback can be on any thread. Post it to a handler of UI thread to ensure that any UI updates are done
        // on the UI thread

        Bundle bundle = new Bundle();
        bundle.putString("deviceName", deviceName);
        bundle.putString("address", address);
        bundle.putBoolean("isShowTerms", isShowTerms);

        Message message = Message.obtain();
        message.what = UiThreadHandler.MSG_WHAT_HOTSPOT_CONNECTED;
        message.setData(bundle);
        mUiThreadHandler.sendMessage(message);
    }

    @Override
    public void onHotspotDisconnected(String deviceName) {
        DdbLogUtility.logCastChapter(TAG, "onHotspotDisconnected() called with: deviceName = [" + deviceName + "]");
        // This callback can be on any thread. Post it to a handler of UI thread to ensure that any UI updates are done
        // on the UI thread
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_HOTSPOT_DISCONNECTED, deviceName);
        message.sendToTarget();
    }

    @Override
    public void onHotspotError(int errorCode) {
        DdbLogUtility.logCastChapter(TAG, "onHotspotError() called with: errorCode = [" + errorCode + "]");
        // This callback can be on any thread. Post it to a handler of UI thread to ensure that any UI updates are done
        // on the UI thread
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_HOTSPOT_ERROR, errorCode, 0);
        message.sendToTarget();
    }

    private void onHotspotDeinit() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_HOTSPOT_DEINITIALIZED);
        message.sendToTarget();
    }

    private Runnable mInitHotspotRunnable = new Runnable() {
        @Override
        public void run() {
            mSecureSharingManager.init();
        }
    };

    private Runnable mDeinitHotspotRunnable = new Runnable() {
        @Override
        public void run() {
            mSecureSharingManager.deinit();
            // There is no callback on deinitializing hotspot. So, we defined a pseudo-callback keep the uniformity
            onHotspotDeinit();
        }
    };

    private Runnable mStartHotspotRunnable = new Runnable() {
        @Override
        public void run() {
            mSecureSharingManager.start();
        }
    };

    private Runnable mStopHotspotRunnable = new Runnable() {
        @Override
        public void run() {
            mSecureSharingManager.stop();
        }
    };

    private void hotspotInitialized() {
        mHotspotInitialized = true;
        notifyHotspotInitialized();
    }

    private void hotspotStarted(String networkName, String passphrase, Bitmap qrCodeBitmap) {
        mHotspotStarted = true;
        mNetworkName = networkName;
        mPassword = passphrase;
        notifyHotspotStarted(networkName, passphrase, qrCodeBitmap);
    }

    String getNetworkName() {
        return mNetworkName;
    }

    private void hotspotStopped() {
        mHotspotStarted = false;
        mNetworkName = "";
        mPassword = "";
        notifyHotspotStopped();
    }

    private void hotspotConnecting() {
        notifyHotspotConnecting();
    }

    private void hotspotConnected(String deviceName,String address ,boolean isShowTerms) {
        notifyHotspotConnected(deviceName ,address ,isShowTerms );
    }

    private void hotspotDisconnected(String deviceName) {
        notifyHotspotDisconnected(deviceName);
    }

    private void hotspotError(int errorCode) {
        notifyHotspotError(errorCode);
    }

    private void hotspotDeinitialized() {
        mHotspotInitialized = false;
        mNetworkName = "";
        mPassword = "";
    }

    private void notifyHotspotInitialized() {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotInit();
            }
        }
    }

    private void notifyHotspotStarted(String networkName, String passphrase, Bitmap qrCodeBitmap) {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotStarted(networkName, passphrase, qrCodeBitmap);
            }
        }
    }

    private void notifyHotspotStopped() {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotStopped();
            }
        }
    }

    private void notifyHotspotConnecting() {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotConnecting();
            }
        }
    }

    private void notifyHotspotConnected(String deviceName ,String address ,boolean isShowTerms) {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotConnected(deviceName, address, isShowTerms);
            }
        }
    }

    private void notifyHotspotDisconnected(String deviceName) {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotDisconnected(deviceName);
            }
        }
    }

    private void notifyHotspotError(int errorCode) {
        for (int i = 0; mGoogleCastListenerRefs != null && i < mGoogleCastListenerRefs.size(); i++) {
            WeakReference<GoogleCastListener> ref = mGoogleCastListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GoogleCastListener listener = ref.get();
            if (listener != null) {
                listener.onHotspotError(errorCode);
            }
        }
    }

    private void updateGoogleCastEnabledState() {
        mIsGoogleCastEnabled = mITvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_GOOGLE_CAST, 0, 0) == 1;
    }

    private void updateGoogleCastEnabledState(int value) {
        mIsGoogleCastEnabled = (value == 1);
    }

    private void updateTNCEnabledState(int value) {
        DdbLogUtility.log(TAG ,"value :"+value);
        mIsTNCEnabled = (value == 1);
    }

    private void googleCastEnabledStateSettingChanged() {
        updateGoogleCastEnabledState();
    }

    private void googleCastEnabledStateSettingChanged(int value) {
        updateGoogleCastEnabledState(value);
    }

    private void tncEnabledStateSettingChanged(int value) {
        updateTNCEnabledState(value);
    }

    private static class HotspotInfoCallable implements Callable<Bitmap> {

        private Context mContext;
        private String mNetworkName;
        private String mPassphrase;

        private static final String AUTHENTICATION_TYPE_WPA = "WPA";

        private HotspotInfoCallable(Context context, String networkName, String passphrase) {
            mContext = context;
            mNetworkName = networkName;
            mPassphrase = passphrase;
        }

        @Override
        public Bitmap call() throws Exception {
            Wifi wifiInfo = new Wifi();
            wifiInfo.setSsid(mNetworkName);
            wifiInfo.setPsk(mPassphrase);
            wifiInfo.setAuthentication(AUTHENTICATION_TYPE_WPA);
            int bitmapWidth = mContext.getResources().getDimensionPixelSize(R.dimen.cast_screen_qr_code_image_width);
            int bitmapHeight = mContext.getResources().getDimensionPixelSize(R.dimen.cast_screen_qr_code_image_height);
            return QRCode.from(wifiInfo).withHint(EncodeHintType.MARGIN, 0).withSize(bitmapWidth, bitmapHeight).bitmap();
        }

        private String getNetworkName() {
            return mNetworkName;
        }

        private String getPassphrase() {
            return mPassphrase;
        }
    }

    private static class HotspotInfoTask extends FutureTask<Bitmap> {

        private Handler mHandler;
        private String mNetworkName;
        private String mPassphrase;

        private HotspotInfoTask(HotspotInfoCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
            mNetworkName = callable.getNetworkName();
            mPassphrase = callable.getPassphrase();
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Bitmap result = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_HOTSPOT_STARTED;
                    message.obj = new HotspotInfo(mNetworkName, mPassphrase, result);
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "HotspotInfoTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class HotspotInfo {
        private String mNetworkName;
        private String mPassphrase;
        private Bitmap mQrCodeBitmap;

        private HotspotInfo(String networkName, String passphrase, Bitmap qrCodeBitmap) {
            mNetworkName = networkName;
            mPassphrase = passphrase;
            mQrCodeBitmap = qrCodeBitmap;
        }
    }

    private class TNCfetchCallable implements Callable{
        @Override
        public TNCDetails call() throws Exception {
            return fetchTermAndConditions();
        }
    }

    private class TNCFetchTask extends FutureTask<TNCDetails> {

        private TNCFetchTask(TNCfetchCallable callable) {
            super(callable);
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    TNCDetails result = get();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class UiThreadHandler extends Handler {

        private WeakReference<GoogleCastDataManager> mGoogleCastDataManagerRef;

        private static final int MSG_WHAT_HOTSPOT_INITIALIZED = 100;
        private static final int MSG_WHAT_HOTSPOT_STARTED = 101;
        private static final int MSG_WHAT_HOTSPOT_STOPPED = 102;
        private static final int MSG_WHAT_HOTSPOT_CONNECTING = 103;
        private static final int MSG_WHAT_HOTSPOT_CONNECTED = 104;
        private static final int MSG_WHAT_HOTSPOT_DISCONNECTED = 105;
        private static final int MSG_WHAT_HOTSPOT_ERROR = 106;
        private static final int MSG_WHAT_HOTSPOT_DEINITIALIZED = 107;
        private static final int MSG_WHAT_GOOGLE_CAST_ENABLED_STATE_SETTING_CHANGED = 108;
        private static final int MSG_WHAT_TNC_ENABLED_STATE_SETTING_CHANGED = 109;
        private static final int MSG_WHAT_FETCH_TNC = 110;

        private UiThreadHandler(WeakReference<GoogleCastDataManager> googleCastDataManagerRef) {
            super();
            mGoogleCastDataManagerRef = googleCastDataManagerRef;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCastChapter(TAG, "UiThreadHandler handleMessage() msg.what = [" + msg.what + "]");
            if (what == MSG_WHAT_HOTSPOT_INITIALIZED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    googleCastDataManager.hotspotInitialized();
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_STARTED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    HotspotInfo hotspotInfo = (HotspotInfo) msg.obj;
                    googleCastDataManager.hotspotStarted(hotspotInfo.mNetworkName, hotspotInfo.mPassphrase, hotspotInfo.mQrCodeBitmap);
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_STOPPED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    googleCastDataManager.hotspotStopped();
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_CONNECTING) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    googleCastDataManager.hotspotConnecting();
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_CONNECTED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    String deviceName = msg.getData().getString("deviceName");
                    String address = msg.getData().getString("address");
                    boolean isShowTerms = msg.getData().getBoolean("isShowTerms");
                    googleCastDataManager.hotspotConnected(deviceName, address , isShowTerms );
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_DISCONNECTED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    String deviceName = (String) msg.obj;
                    googleCastDataManager.hotspotDisconnected(deviceName);
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_ERROR) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    int errorCode = msg.arg1;
                    googleCastDataManager.hotspotError(errorCode);
                }
                return;
            }
            if (what == MSG_WHAT_HOTSPOT_DEINITIALIZED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    int errorCode = msg.arg1;
                    googleCastDataManager.hotspotDeinitialized();
                }
                return;
            }
            if (what == MSG_WHAT_GOOGLE_CAST_ENABLED_STATE_SETTING_CHANGED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    googleCastDataManager.googleCastEnabledStateSettingChanged(msg.arg1);
                }
                return;
            }
            if (what == MSG_WHAT_TNC_ENABLED_STATE_SETTING_CHANGED) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    googleCastDataManager.tncEnabledStateSettingChanged(msg.arg1);
                }
                return;
            }
            if (what == MSG_WHAT_FETCH_TNC) {
                GoogleCastDataManager googleCastDataManager = mGoogleCastDataManagerRef.get();
                if (googleCastDataManager != null) {
                    googleCastDataManager.fetchTermAndConditions();
                }
                return;
            }
        }
    }

    private void registerCloneInReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_CLONE_IN);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    private void registerLocalBootCompleteReceiver(){
        DdbLogUtility.logAppsChapter(TAG,"registerLockedBootCompleteReceiver");
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_LOCAL_BROADCAST_BOOT_COMPLETED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    private void registerLocaleChangeListner() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_ANDROID_LOCALE_CHANGED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, intentFilter);
    }

    private final class CastLocalBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DdbLogUtility.logAppsChapter(TAG, "onReceive() action = " + action);
            if(Constants.ACTION_LOCAL_BROADCAST_BOOT_COMPLETED.equals(action) || Constants.ACTION_CLONE_IN.equals(action)
                ||Constants.ACTION_ANDROID_LOCALE_CHANGED.equals(action)) {
                TNCfetchCallable callable = new TNCfetchCallable();
                TNCFetchTask task = new TNCFetchTask(callable);
                mThreadPoolExecutor.execute(task);
            }
        }
    }

    private TNCDetails fetchTermAndConditions() {
        try {
            File f = new File(Constants.GOOGLECAST_TERM_AND_CONDITION_FILE_PATH);
            if (f.exists()) {
                String line = null;
                BufferedReader br = new BufferedReader(new FileReader(f));
                StringBuilder fileContents = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    fileContents.append(line);
                }
                JSONObject jsonObject = new JSONObject(fileContents.toString()).getJSONObject("CommandDetails").getJSONObject("ApplicationDetails").getJSONObject("ApplicationAttributes");
                JSONArray jMessage = jsonObject.getJSONArray("Messages");
                int currentLanguageInDDB = mDashboardDataManager.getTvSettingsManager().getInt(TvSettingsConstants.MENULANGUAGE, 0, 0);
                String currentLanguage = mDashboardDataManager.getISOCodeForLanguage(currentLanguageInDDB);
                int mGuestMenuLanguage = mDashboardDataManager.getTvSettingsManager().getInt(TvSettingsConstants.PBSMGR_PROPERTY_HTV_SWITCH_ON_GUEST_MANAGEMENT_ANDROID_SYSTEM_MENU_LANGUAGE, 0, 0);
                String guestLanguageCode = mDashboardDataManager.getISOCodeForLanguage(mGuestMenuLanguage);
                int mDDBLanguageIndex = -1;
                int mGuestLanguageIndex = -1;

                for(int i=0 ; i < jMessage.length() ; i++) {
                    //ISO logic
                    if (jMessage.getJSONObject(i).get("Language").toString().equalsIgnoreCase(currentLanguage)) {
                        DdbLogUtility.logCastChapter(TAG, " DDBLanguageIndex readTermAndConditions: locale found at :" + i);
                        mDDBLanguageIndex = i;
                        break;
                    }else if (jMessage.getJSONObject(i).get("Language").toString().equalsIgnoreCase(guestLanguageCode)){
                        DdbLogUtility.logCastChapter(TAG, " GuestLanguageIndex readTermAndConditions: locale found at :" + i);
                        mGuestLanguageIndex = i;
                    }
                }

                int requiredIndexInJSON = 0; //Default index that should be 0
                if (mDDBLanguageIndex != -1){
                    requiredIndexInJSON = mDDBLanguageIndex;
                }else if(mGuestLanguageIndex != -1){
                    requiredIndexInJSON = mGuestLanguageIndex;
                }
                tncDetails = new TNCDetails( jMessage.getJSONObject(requiredIndexInJSON).get("Language").toString() , jMessage.getJSONObject(requiredIndexInJSON).get("MessageTitle").toString() , jMessage.getJSONObject(requiredIndexInJSON).get("MessageBody").toString());
                DdbLogUtility.logCastChapter(TAG, "readTermAndConditions: ISO :"+tncDetails.getLanguageCode()+" Title:"+tncDetails.getMessageTitle()+" Body:"+tncDetails.getMessageBody());
            }else{
                //No file exist use default T&C
                DdbLogUtility.logCastChapter(TAG,"File not exist default TNC");
                String messageTile = getContext().getResources().getString(R.string.MAIN_TNC_Default_MSG_Title);
                String messageBody =getContext().getResources().getString(R.string.MAIN_TNC_Default_MSG_BODY);
                tncDetails = new TNCDetails( "eng",messageTile,messageBody);
            }
            return tncDetails;
        } catch (Exception e) {
            Log.e(TAG,"Exception return default:"+e.getMessage());
            DdbLogUtility.logCastChapter(TAG,"File not exist default TNC");
            String messageTile = getContext().getResources().getString(R.string.MAIN_TNC_Default_MSG_Title);
            String messageBody =getContext().getResources().getString(R.string.MAIN_TNC_Default_MSG_BODY);
            tncDetails = new TNCDetails( "eng",messageTile,messageBody);
        }
        return tncDetails;
    }

    public TNCDetails checkAndFetchTncDetails(){
        if(tncDetails != null){
            return tncDetails;
        }else {
            return fetchTermAndConditions();
        }
    }
}
