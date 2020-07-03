package org.droidtv.defaultdashboard.data.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.UserHandle;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MyChoiceListener;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.query.MyChoiceQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.htv.provider.HtvContract.HtvChannelList;
import org.droidtv.tv.mychoice.MyChoiceManager;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

final class MyChoiceDataManager extends ContextualObject {

    private static final String TAG = "MyChoiceDataManager";

    private static final String ACTION_MYCHOICE_DATA_CHANGED = "org.droidtv.mychoice.MYCHOICE_DATA_CHANGED";
    private static final String ACTION_MYCHOICE_LOCK_STATUS_CHANGED = "org.droidtv.intent.action.japit.MYCHOICE_STATUS_CHANGED";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private ITvSettingsManager mTvSettingsManager;
    private MyChoiceManager mMyChoiceManager;
    private boolean mMyChoiceEnabled;
    private ArrayList<Channel> mMyChoiceItems;
    private ArrayList<WeakReference<MyChoiceListener>> mMyChoiceListenerRefs;

    private static final int BDS_UNLOCK_STATUS = 2;

    public static final int PKG_1_UNLOCKED = 1;
    public static final int PKG_2_UNLOCKED = 2;

    MyChoiceDataManager(Context context) {
        super(context);
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.MYCHOICE_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler(new WeakReference<MyChoiceDataManager>(this));
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        mMyChoiceManager = new MyChoiceManager();
        mMyChoiceManager.setContext(context.getApplicationContext());
        mMyChoiceItems = new ArrayList<>();
        mMyChoiceListenerRefs = new ArrayList<>();
        registerMyChoiceReceiver();
        updateMyChoiceEnabledState();
        DdbLogUtility.logCommon(TAG, "MyChoiceDataManager constructor");
    }

    boolean isMyChoiceEnabled() {
        return mMyChoiceEnabled;
    }

    boolean isMyChoicePkg1Unlocked() {
        DdbLogUtility.logMoreChapter(TAG, "isMyChoicePkg1Unlocked BdsStatus: " + mMyChoiceManager.getBdsStatus(BDS_UNLOCK_STATUS));
        return mMyChoiceManager.getBdsStatus(BDS_UNLOCK_STATUS) == PKG_1_UNLOCKED;
    }

    boolean isMyChoicePkg2Unlocked() {
        return mMyChoiceManager.getBdsStatus(BDS_UNLOCK_STATUS) == PKG_2_UNLOCKED;
    }

    boolean isChannelMyChoiceLocked(Channel channel) {
        DdbLogUtility.logMoreChapter(TAG, "isChannelMyChoiceLocked: isMyChoiceFreePackage " + channel.isMyChoiceFreePackage() + " mMyChoiceEnabled " + mMyChoiceEnabled
                                        + " displayNumber " + channel.getDisplayNumber() + " displayName " + channel.getDisplayName());
        if (channel.isMyChoiceFreePackage()) {
            return false;
        }

        if (!isMyChoiceEnabled()) {
            return false;
        }

        if(mMyChoiceManager.isSourceLocked(channel.getDisplayNumber()) != 1){
            return false;
        }
        return true;
    }

    boolean isSourceMyChoiceLocked(String sourceInputId) {
        for (Channel channel : mMyChoiceItems) {
            if (sourceInputId.equals(channel.getInputId())) {
                return isChannelMyChoiceLocked(channel);
            }
        }
        return false;
    }

    boolean areAppsMyChoiceLocked() {
        for (Channel channel : mMyChoiceItems) {
            if (HtvChannelList.TYPE_APPS.equals(channel.getMediaType())) {
                return isChannelMyChoiceLocked(channel);
            }
        }
        return false;
    }

    boolean isGoogleCastMyChoiceLocked() {
        for (Channel channel : mMyChoiceItems) {
            if (HtvChannelList.TYPE_GOOGLE_CAST.equals(channel.getMediaType())) {
                return isChannelMyChoiceLocked(channel);
            }
        }
        return false;
    }

    boolean isMediaBrowserMyChoiceLocked() {
        for (Channel channel : mMyChoiceItems) {
            if (HtvChannelList.TYPE_MEDIABROSWER.equals(channel.getMediaType())) {
                return isChannelMyChoiceLocked(channel);
            }
        }
        return false;
    }

    boolean addMyChoiceListener(MyChoiceListener myChoiceListener) {
        if (myChoiceListener == null) {
            return false;
        }
        return mMyChoiceListenerRefs.add(new WeakReference<MyChoiceListener>(myChoiceListener));
    }

    boolean removeMyChoiceListener(MyChoiceListener myChoiceListener) {
        if (mMyChoiceListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mMyChoiceListenerRefs.size(); i++) {
            WeakReference<MyChoiceListener> ref = mMyChoiceListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            MyChoiceListener listener = ref.get();
            if (listener != null && listener.equals(myChoiceListener)) {
                return mMyChoiceListenerRefs.remove(ref);
            }
        }
        return false;
    }

    private BroadcastReceiver mMyChoiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DdbLogUtility.logCommon(TAG, "onReceive: action " + intent.getAction());
            String action = intent.getAction();
            if (ACTION_MYCHOICE_DATA_CHANGED.equals(action)) {
                updateMyChoiceItems();
                return;
            }
            if (ACTION_MYCHOICE_LOCK_STATUS_CHANGED.equals(action)) {
                myChoiceLockStatusChanged();
                return;
            }
        }
    };

    void updateMyChoiceEnabledState() {
        mMyChoiceEnabled = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_MY_CHOICE_MYCHOICE, 0, 0) ==
                TvSettingsDefinitions.PbsMyChoiceConstants.PBSMGR_MY_CHOICE_ON;
    }

    void updateMyChoiceEnabledState(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_MYCHOICE_ENABLED_STATE_CHANGED, value, -1);
        message.sendToTarget();
    }

    private void onMyChoiceEnabledStateUpdated(int value) {
        mMyChoiceEnabled = (value ==
                TvSettingsDefinitions.PbsMyChoiceConstants.PBSMGR_MY_CHOICE_ON);
        updateMyChoiceItems();
    }

    private void registerMyChoiceReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MYCHOICE_DATA_CHANGED);
        intentFilter.addAction(ACTION_MYCHOICE_LOCK_STATUS_CHANGED);
        getContext().getApplicationContext().registerReceiverAsUser(mMyChoiceReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    void updateMyChoiceItems() {
        DdbLogUtility.logCommon(TAG, "updateMyChoiceItems() called");
        MyChoiceQuery query = new MyChoiceQuery();
        MyChoiceCallable callable = new MyChoiceCallable(getContext(), query);
        MyChoiceTask task = new MyChoiceTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    private void myChoiceItemsUpdated(List<Channel> myChoiceItems) {
        mMyChoiceItems = new ArrayList<>(myChoiceItems);
        notifyMyChoiceDataChanged();
    }

    private void myChoiceLockStatusChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_MYCHOICE_LOCK_STATUS_CHANGED);
        message.sendToTarget();
    }

    private void notifyMyChoiceDataChanged() {
        for (int i = 0; mMyChoiceListenerRefs != null && i < mMyChoiceListenerRefs.size(); i++) {
            WeakReference<MyChoiceListener> listenerRef = mMyChoiceListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MyChoiceListener listener = listenerRef.get();
            if (listener != null) {
                listener.onMyChoiceDataChanged();
            }
        }
    }

    void notifyMyChoiceEnabledStateChanged() {
        for (int i = 0; mMyChoiceListenerRefs != null && i < mMyChoiceListenerRefs.size(); i++) {
            WeakReference<MyChoiceListener> listenerRef = mMyChoiceListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MyChoiceListener listener = listenerRef.get();
            if (listener != null
                    ) {
                listener.onMyChoiceEnabledStateChanged();
            }
        }
    }

    void notifyMyChoiceEnabledStateChanged(int value) {
        for (int i = 0; mMyChoiceListenerRefs != null && i < mMyChoiceListenerRefs.size(); i++) {
            WeakReference<MyChoiceListener> listenerRef = mMyChoiceListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MyChoiceListener listener = listenerRef.get();
            if (listener != null) {
                listener.onMyChoiceEnabledStateChanged(value);
            }
        }
    }

    private void notifyMyChoiceLockStatusChanged() {
        for (int i = 0; mMyChoiceListenerRefs != null && i < mMyChoiceListenerRefs.size(); i++) {
            WeakReference<MyChoiceListener> listenerRef = mMyChoiceListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MyChoiceListener listener = listenerRef.get();
            if (listener != null) {
                listener.onMyChoiceLockStatusChanged();
            }
        }
    }

    private static Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().
                query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    public void unRegisterReceivers() {
        getContext().getApplicationContext().unregisterReceiver(mMyChoiceReceiver);

    }

    private static final class MyChoiceCallable implements Callable<List<Channel>> {

        private Context mContext;
        private Query mQuery;

        private MyChoiceCallable(Context context, Query query) {
            mContext = context;
            mQuery = query;
        }

        @Override
        public List<Channel> call() throws Exception {
            Cursor cursor = null;
            List<Channel> myChoiceItems = new ArrayList<>();
            try {
                cursor = executeQuery(mContext, mQuery);
                while (cursor != null && cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(HtvChannelList._ID));
					String displayNumber = cursor.getString(cursor.getColumnIndex(HtvChannelList.COLUMN_DISPLAY_NUMBER));
					String displayName = cursor.getString(cursor.getColumnIndex(HtvChannelList.COLUMN_DISPLAY_NAME));
                    String inputId = cursor.getString(cursor.getColumnIndex(HtvChannelList.COLUMN_INPUT_ID));
                    String mediaType = cursor.getString(cursor.getColumnIndex(HtvChannelList.COLUMN_MEDIA_TYPE));
                    int myChoiceFreePkg = cursor.getInt(cursor.getColumnIndex(HtvChannelList.COLUMN_FREEPKG));
                    int myChoicePayPkg1 = cursor.getInt(cursor.getColumnIndex(HtvChannelList.COLUMN_PAYPKG1));
                    int myChoicePayPkg2 = cursor.getInt(cursor.getColumnIndex(HtvChannelList.COLUMN_PAYPKG2));

                    myChoiceItems.add(new Channel.Builder(id).
							setDisplayNumber(displayNumber).
							setDisplayName(displayName).
                            setInputId(inputId).
                            setMediaType(mediaType).
                            setMyChoiceFreePackage(myChoiceFreePkg == 1).
                            setMyChoicePackage1(myChoicePayPkg1 == 1).
                            setMyChoicePackage2(myChoicePayPkg2 == 1).
                            build());
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "MyChoice query failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return myChoiceItems;
        }
    }

    private static final class MyChoiceTask extends FutureTask<List<Channel>> {

        private Handler mHandler;

        private MyChoiceTask(MyChoiceCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    List<Channel> result = get();
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_UPDATE_MYCHOICE_ITEMS, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "MyChoiceTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_UPDATE_MYCHOICE_ITEMS = 100;
        private static final int MSG_WHAT_MYCHOICE_LOCK_STATUS_CHANGED = 101;
        private static final int MSG_WHAT_MYCHOICE_ENABLED_STATE_CHANGED = 102;

        private WeakReference<MyChoiceDataManager> mMyChoiceDataManagerRef;

        private UiThreadHandler(WeakReference<MyChoiceDataManager> ref) {
            super();
            mMyChoiceDataManagerRef = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCommon(TAG, "handleMessage() called with: what = [" + what + "]");
            if (what == MSG_WHAT_UPDATE_MYCHOICE_ITEMS) {
                MyChoiceDataManager myChoiceDataManager = mMyChoiceDataManagerRef.get();
                if (myChoiceDataManager != null) {
                    myChoiceDataManager.myChoiceItemsUpdated((List<Channel>) msg.obj);
                }
                return;
            }
            if (what == MSG_WHAT_MYCHOICE_LOCK_STATUS_CHANGED) {
                MyChoiceDataManager myChoiceDataManager = mMyChoiceDataManagerRef.get();
                if (myChoiceDataManager != null) {
                    myChoiceDataManager.notifyMyChoiceLockStatusChanged();
                }
                return;
            }
            if (what == MSG_WHAT_MYCHOICE_ENABLED_STATE_CHANGED) {
                MyChoiceDataManager myChoiceDataManager = mMyChoiceDataManagerRef.get();
                if (myChoiceDataManager != null) {
                    myChoiceDataManager.onMyChoiceEnabledStateUpdated(msg.arg1);
                }
                return;
            }
        }
    }

    public void cleanup(){
        if(mMyChoiceManager != null){
            mMyChoiceManager.setContext(null);
        }
        unRegisterReceivers();
    }
}
