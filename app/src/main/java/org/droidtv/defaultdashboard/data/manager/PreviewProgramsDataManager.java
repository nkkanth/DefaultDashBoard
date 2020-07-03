package org.droidtv.defaultdashboard.data.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.query.PreviewChannelDetails;
import org.droidtv.defaultdashboard.data.query.PreviewProgramDataQuery;
import org.droidtv.defaultdashboard.data.query.PreviewProgramsChannelGamesQuery;
import org.droidtv.defaultdashboard.data.query.PreviewProgramsChannelQuery;
import org.droidtv.defaultdashboard.data.query.PreviewProgramsChannelSmartInfoQuery;
import org.droidtv.defaultdashboard.data.query.PreviewProgramsChannelVODQuery;
import org.droidtv.defaultdashboard.data.query.PreviewProgramsQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationImportance;
import org.droidtv.defaultdashboard.util.AppUtil;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.htv.provider.HtvContract;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PreviewProgramsDataManager extends ContextualObject implements DashboardDataManager.AppDataListener{

    private static final String TAG = PreviewProgramsDataManager.class.getSimpleName();

    private UiThreadHandler mUiThreadHandler;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private Map<Integer, PreviewProgramsChannel> mPreviewProgramChannelsList;
    private Map<Integer , PreviewProgramsChannel> mVodPreviewProgramChannelsList;
    private Map<Integer , PreviewProgramsChannel> mGamesPreviewProgramChannelsList;
    private List<PreviewProgramsChannel> mSmartInfoPreviewProgramChannelList;
    private final ArrayList<WeakReference<DashboardDataManager.PreviewProgramsListener>> mPreviewProgramsListenerRefs = new ArrayList<>();
    private WeakReference<DashboardDataManager.SmartInfoPreviewProgramListener> mSmartInfoListenerRefs;

    private static final long DELAY_FETCH_PREVIEW_CHANNELS_FOR_LOCALE_CHANGE = 10 * 1000; //10 sec
    private static final long DELAY_FETCH_PREVIEW_PROGRAMS = 2 * 1000; //1 sec
    private final static int RECOMMENDATION_INTENT_CODE = 1001;
    private final String SYMBOL_MIN = "m";
    private final String SYMBOL_HOUR = "h ";
    private final String SYMBOL_HYPHEN = " - ";

    PreviewProgramsDataManager(Context c){
        super(c);
        init();
    }

    private void init(){
        mPreviewProgramChannelsList = new HashMap<>();
        mVodPreviewProgramChannelsList = new HashMap<>();
        mGamesPreviewProgramChannelsList = new HashMap<>();
        mSmartInfoPreviewProgramChannelList = new ArrayList<>();
        mUiThreadHandler = new UiThreadHandler(new WeakReference<PreviewProgramsDataManager>(this));
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.RECOMMENDATION_THREAD_POOL_EXECUTOR);
		//TODO: register once fetch is complete
        registerPreviewProgramsDbChanges();
        DashboardDataManager.getInstance().addAppDataListener(this);
        registerLocaleChangeListner();
    }

    private void registerLocaleChangeListner() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_ANDROID_LOCALE_CHANGED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(localeChangeReceiver, intentFilter);
    }

    private BroadcastReceiver localeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive called with: context = [" + context + "], intent = [" + intent + "]");
            if(intent.getAction().equals(Constants.ACTION_ANDROID_LOCALE_CHANGED)) {
                fetchPreviewChannelDelayed();
            }
        }
    };

   private void fetchPreviewChannelDelayed() {
       if (mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_LOCALE_CHANGE)) {
           Log.d(TAG, "fetchPreviewChannelDelayed: removed pending locale_changed messages");
           mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_LOCALE_CHANGE);
       }
       mUiThreadHandler.sendEmptyMessageDelayed(UiThreadHandler.MSG_WHAT_LOCALE_CHANGE, DELAY_FETCH_PREVIEW_CHANNELS_FOR_LOCALE_CHANGE);
   }

    public List<Recommendation> getPreviewChannleRecommendations(){
        List<Recommendation> previewChannelRecommendations = new ArrayList<Recommendation>();
        if (mPreviewProgramChannelsList != null && !mPreviewProgramChannelsList.isEmpty()) {
            for (PreviewProgramsChannel channel : mPreviewProgramChannelsList.values()) {
                List<Recommendation> list = channel.getPreviewProgramList();
                if (list != null && !list.isEmpty()) {
                    previewChannelRecommendations.addAll(list);
                }
            }
        }
        return previewChannelRecommendations;
    }

    public List<PreviewProgramsChannel> getSmartInfoPreviewChannelRecommendations(){
        return mSmartInfoPreviewProgramChannelList;
    }

    boolean addPreviewProgramsChannelsListener(DashboardDataManager.PreviewProgramsListener listener) {
        if (listener == null) {
            return false;
        }
        boolean added = mPreviewProgramsListenerRefs.add(new WeakReference<>(listener));
        return added;
    }

    boolean removePreviewListener(DashboardDataManager.PreviewProgramsListener previewProgramsListener) {
        if (mPreviewProgramsListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mPreviewProgramsListenerRefs.size(); i++) {
            WeakReference<DashboardDataManager.PreviewProgramsListener> ref = mPreviewProgramsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardDataManager.PreviewProgramsListener listener = ref.get();
            if (listener != null && listener.equals(previewProgramsListener)) {
                return mPreviewProgramsListenerRefs.remove(ref);
            }
        }
        return false;
    }

    private void notifyPreviewProgramAvailable(PreviewProgramsChannel previewProgramsChannels, boolean isDBUpdate) {
        addToRespectivePreviewProgramList(previewProgramsChannels);
        notifyPreviewDataAvaiable(previewProgramsChannels, isDBUpdate);
    }

    private void notifyPreviewChannelDeleted(PreviewProgramsChannel channel, boolean isDBUpdate){
        for (int i = 0; mPreviewProgramsListenerRefs != null && i < mPreviewProgramsListenerRefs.size(); i++) {
            WeakReference<DashboardDataManager.PreviewProgramsListener> listenerRef = mPreviewProgramsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }
            DdbLogUtility.logCommon(TAG, "notifyPreviewChannelDeleted isDBUpdate: " + isDBUpdate);
            DashboardDataManager.PreviewProgramsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onPreviewProgramChannelDeleted(channel);
            }
        }
    }
    private void notifyPreviewDataAvaiable(PreviewProgramsChannel previewProgramsChannels, boolean isDBUpdate){
        for (int i = 0; mPreviewProgramsListenerRefs != null && i < mPreviewProgramsListenerRefs.size(); i++) {
            WeakReference<DashboardDataManager.PreviewProgramsListener> listenerRef = mPreviewProgramsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }
            DdbLogUtility.logCommon(TAG, "notifyPreviewProgramAvailable isDBUpdate: " + isDBUpdate);
            DashboardDataManager.PreviewProgramsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onPreviewProgramChannelsAvailable(previewProgramsChannels);
            }
        }
    }

    private void addToRespectivePreviewProgramList(PreviewProgramsChannel previewProgramsChannels){
        int category = previewProgramsChannels.getCategory();
        switch (category){
            case Constants.CHANNEL_CONTENT_TYPE_APP_RECOMMENDATION:
                addToPreviewProgramsChannelsList(previewProgramsChannels);
                break;
            case Constants.CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION:
                addToVodPreviewProgramsChannelsList(previewProgramsChannels);
                break;
            case Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION:
                addToGamesPreviewProgramsChannelsList(previewProgramsChannels);
                break;
            default:
                android.util.Log.d(TAG, "addToRespectivePreviewProgramList: not a valid preview channel type, category " + category);

        }
    }

    private void addToPreviewProgramsChannelsList(PreviewProgramsChannel previewProgramsChannel) {
        if(mPreviewProgramChannelsList == null) {
            mPreviewProgramChannelsList = new HashMap<>();
        }
        DashboardDataManager dashboardDataManager = DashboardDataManager.getInstance();
        String appPackageName = previewProgramsChannel.getPackageName();
        DdbLogUtility.logRecommendationChapter(TAG, "addToPreviewProgramsChannelsList: displayName  " + previewProgramsChannel.getDisplayName() + " PackageName " + appPackageName);
        if (!dashboardDataManager.isAppRecommendationEnabled(appPackageName)) {
            return;
        }
        //TODO: Remove this check after app list query from htvapplist db is functional
        if ("org.droidtv.nettvadvert".equals(appPackageName)) {
            DdbLogUtility.logRecommendationChapter(TAG, "addToPreviewProgramsChannelsList: nettvadvert");
            return;
        }

         mPreviewProgramChannelsList.put(previewProgramsChannel.getId() ,previewProgramsChannel);

    }

    private void addToVodPreviewProgramsChannelsList(PreviewProgramsChannel previewProgramsChannel) {
        if(mVodPreviewProgramChannelsList == null) {
            mVodPreviewProgramChannelsList = new HashMap<>();
        }
        DashboardDataManager dashboardDataManager = DashboardDataManager.getInstance();
        String appPackageName = previewProgramsChannel.getPackageName();
        if (!dashboardDataManager.isAppRecommendationEnabled(appPackageName)) {
            android.util.Log.d(TAG, "addToVodPreviewProgramsChannelsList: isAppRecommendationEnabled false");
            return;
        }
        //TODO: Remove this check after app list query from htvapplist db is functional
        if ("org.droidtv.nettvadvert".equals(appPackageName)) {
            android.util.Log.d(TAG, "addToVodPreviewProgramsChannelsList: nettvadvert");
            return;
        }
        //bharat
        if(!mPreviewProgramChannelsList.containsValue(previewProgramsChannel)) {
            mVodPreviewProgramChannelsList.put(previewProgramsChannel.getId() , previewProgramsChannel);
        }
    }

    private void addToGamesPreviewProgramsChannelsList(PreviewProgramsChannel previewProgramsChannel) {
        if(mGamesPreviewProgramChannelsList == null) {
            mGamesPreviewProgramChannelsList = new HashMap<>();
        }
        DashboardDataManager dashboardDataManager = DashboardDataManager.getInstance();
        String appPackageName = previewProgramsChannel.getPackageName();
        if (!dashboardDataManager.isAppRecommendationEnabled(appPackageName)) {
            android.util.Log.d(TAG, "addToGamesPreviewProgramsChannelsList: isAppRecommendationEnabled false");
            return;
        }
        //TODO: Remove this check after app list query from htvapplist db is functional
        if ("org.droidtv.nettvadvert".equals(appPackageName)) {
            return;
        }
        if(!mGamesPreviewProgramChannelsList.containsValue(previewProgramsChannel)) {
            mGamesPreviewProgramChannelsList.put(previewProgramsChannel.getId() ,previewProgramsChannel);
        }
    }

    private void addToSmartInfoPreviewProgramsChannelsList(PreviewProgramsChannel previewProgramsChannel) {
        if(mSmartInfoPreviewProgramChannelList == null) {
            mSmartInfoPreviewProgramChannelList = new ArrayList<>();
        }
        if(!mSmartInfoPreviewProgramChannelList.contains(previewProgramsChannel)){
            mSmartInfoPreviewProgramChannelList.add(previewProgramsChannel);
        }
    }


    public synchronized List<PreviewProgramsChannel> getPreviewProgramsChannelList(String packageName) {
        List<PreviewProgramsChannel> previewProgramsChannelsList = new ArrayList<>();
        int count = 0;
        if (mPreviewProgramChannelsList != null && !mPreviewProgramChannelsList.isEmpty()) {
            for (PreviewProgramsChannel channel : mPreviewProgramChannelsList.values()) {
                if (channel.getPackageName().equalsIgnoreCase(packageName)) {
                    previewProgramsChannelsList.add(channel);
                    count++;
                }
            }
        }

        DdbLogUtility.logRecommendationChapter(TAG, "getPreviewProgramsChannelList count " + count);
        return previewProgramsChannelsList;
    }

    public int getPreviewProgramsChannelCount(String packageName) {
        int count = 0;
        if (mPreviewProgramChannelsList != null && !mPreviewProgramChannelsList.isEmpty()) {
            for (PreviewProgramsChannel channel : mPreviewProgramChannelsList.values()) {
                if (channel.getPackageName().equalsIgnoreCase(packageName)) {
                    if (channel.getPreviewProgramList() != null && !channel.getPreviewProgramList().isEmpty()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public List<PreviewProgramsChannel> getVodPreviewProgramsChannelList(String packageName){
        List<PreviewProgramsChannel> vodPreviewProgramsChannelsList = new ArrayList<>();
        if (mPreviewProgramChannelsList != null && !mPreviewProgramChannelsList.isEmpty()) {
            for (PreviewProgramsChannel channel : mVodPreviewProgramChannelsList.values()) {
                if (channel.getPackageName().equalsIgnoreCase(packageName)) {
                    vodPreviewProgramsChannelsList.add(channel);
                }
            }
        }
        return vodPreviewProgramsChannelsList;
    }

    public int getVodPreviewProgramsChannelCount(String packageName) {
        int count = 0;
        if (mPreviewProgramChannelsList != null && !mPreviewProgramChannelsList.isEmpty()) {
            for (PreviewProgramsChannel channel : mVodPreviewProgramChannelsList.values()) {
                if (channel.getPackageName().equalsIgnoreCase(packageName)) {
                    if (channel.getPreviewProgramList() != null && !channel.getPreviewProgramList().isEmpty()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public List<PreviewProgramsChannel> fetchPreviewProgramChannels(Context context) {
        DdbLogUtility.logRecommendationChapter("PreviewProgramsDataManager", "fetchPreviewProgramChannels() called ");
        List<PreviewProgramsChannel> previewProgramsChannels = new ArrayList<>();
        Cursor cursor = executeQuery(context, new PreviewProgramsChannelQuery());
        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "fetchPreviewProgramChannels: count " + cursor.getCount());
            do {
                PreviewProgramsChannel channel = new PreviewProgramsChannel();
                channel.setId(cursor.getInt(cursor.getColumnIndex( PreviewProgramsQuery.COLUMN_CHANNEL_ID)));
                channel.setPackageName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_PACKAGE_NAME)));
                channel.setDisplayName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)));
                channel.setDescription(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DESCRIPTION)));
                previewProgramsChannels.add(channel);
            } while (cursor.moveToNext());
        }
        if(cursor != null) cursor.close();

        return previewProgramsChannels;
    }
    public void fetchPreviewPrograms() {
        Log.d(TAG, "fetchPreviewPrograms: scheduled aftre 2 second");
        if (mUiThreadHandler.hasMessages(mUiThreadHandler.MSG_WHAT_START_FETCH_PREVIEW_PROGRAMS)) {
            mUiThreadHandler.removeMessages(mUiThreadHandler.MSG_WHAT_START_FETCH_PREVIEW_PROGRAMS);
        }
        mUiThreadHandler.sendEmptyMessageDelayed(mUiThreadHandler.MSG_WHAT_START_FETCH_PREVIEW_PROGRAMS, DELAY_FETCH_PREVIEW_PROGRAMS);
    }

    public void fetchPreviewPrograms(int dummy) {
        android.util.Log.d(TAG, "fetchPreviewPrograms: No delay");
        PreviewProgramChannelListCallable callable = new PreviewProgramChannelListCallable();
        PreviewProgramChannelListTask task = new PreviewProgramChannelListTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    public void fetchVodPreviewPrograms() {
        android.util.Log.d(TAG, "fetchVodPreviewPrograms");
        VodPreviewProgramChannelListCallable callable = new VodPreviewProgramChannelListCallable();
        VodPreviewProgramChannelListTask task = new VodPreviewProgramChannelListTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    public void fetchGamesPreviewPrograms() {
        android.util.Log.d(TAG, "fetchGamesPreviewPrograms");
        GamesPreviewProgramChannelListCallable callable = new GamesPreviewProgramChannelListCallable();
        GamesPreviewProgramChannelListTask task = new GamesPreviewProgramChannelListTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    public void fetchSmartInfoPreviewChannels(String packageName) {
        android.util.Log.d("Sup", "fetchSmartInfoPreviewChannels " );
        if(mSmartInfoPreviewProgramChannelList != null){
            mSmartInfoPreviewProgramChannelList.clear();
        }
        SmartInfoPreviewProgramChannelListCallable callable = new SmartInfoPreviewProgramChannelListCallable();
        callable.setPackageName(packageName);
        SmartInfoPreviewProgramChannelListTask task = new SmartInfoPreviewProgramChannelListTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    public void addSmartInfoListner(DashboardDataManager.SmartInfoPreviewProgramListener smartInfoListener) {
        mSmartInfoListenerRefs = new WeakReference<>(smartInfoListener);
    }

	//TODO: remove only unregistered listner
    public void unRegisterSmartInfoPreviewProgramListener(DashboardDataManager.SmartInfoPreviewProgramListener smartInfoListener){
        if(mSmartInfoListenerRefs != null){
            mSmartInfoListenerRefs.clear();
        }
    }

    private class PreviewProgramChannelListCallable implements Callable<List<PreviewProgramsChannel>> {

        @Override
        public List<PreviewProgramsChannel> call() throws Exception {
            return fetchPreviewProgramChannels(getContext());
        }
    }

    private class VodPreviewProgramChannelListCallable implements Callable<List<PreviewProgramsChannel>> {

        @Override
        public List<PreviewProgramsChannel> call() throws Exception {
            android.util.Log.d(TAG, "VodPreviewProgramChannelListCallable: call");
            return fetchPreviewProgramVODChannels(getContext());
        }
    }

    private class GamesPreviewProgramChannelListCallable implements Callable<List<PreviewProgramsChannel>> {

        @Override
        public List<PreviewProgramsChannel> call() throws Exception {
            android.util.Log.d(TAG, "GamesPreviewProgramChannelListCallable: call");
            return fetchPreviewProgramGamingChannels(getContext());
        }
    }

    private class SmartInfoPreviewProgramChannelListCallable implements Callable<List<PreviewProgramsChannel>> {

        String mPackageName;
        public void setPackageName(String packageName) {
            mPackageName = packageName;
        }

        @Override
        public List<PreviewProgramsChannel> call() throws Exception {
            android.util.Log.d(TAG, "SmartInfoPreviewProgramChannelListCallable: call");
            return fetchPreviewProgramSmartInfoChannels(getContext(), mPackageName);
        }
    }

    private class PreviewProgramChannelListTask extends FutureTask<List<PreviewProgramsChannel>> {
        private Handler mHandler;

        public PreviewProgramChannelListTask(PreviewProgramChannelListCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                List<PreviewProgramsChannel> channelList = get();
                android.util.Log.d(TAG, "Recommended PreviewProgramChannelListTask done() called  size " + channelList.size());
                Message message = mHandler.obtainMessage();
                message.what = UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAM_CHANNELS_LIST_FETCH_COMPLETE;
                message.obj = channelList;
                mHandler.sendMessage(message);
            } catch (Exception e) {
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private class VodPreviewProgramChannelListTask extends FutureTask<List<PreviewProgramsChannel>> {
        private Handler mHandler;

        public VodPreviewProgramChannelListTask(VodPreviewProgramChannelListCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                List<PreviewProgramsChannel> channelList = get();
                android.util.Log.d(TAG, "VodPreviewProgramChannelListTask done() called  size " + channelList.size());
                Message message = mHandler.obtainMessage();
                message.what = UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAM_VOD_CHANNELS_LIST_FETCH_COMPLETE;
                message.obj = channelList;
                mHandler.sendMessage(message);
            } catch (Exception e) {
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private class GamesPreviewProgramChannelListTask extends FutureTask<List<PreviewProgramsChannel>> {
        private Handler mHandler;

        public GamesPreviewProgramChannelListTask(GamesPreviewProgramChannelListCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                List<PreviewProgramsChannel> channelList = get();
                android.util.Log.d(TAG, "GamesPreviewProgramChannelListTask done() called  size " + channelList.size());
                Message message = mHandler.obtainMessage();
                message.what = UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAM_GAMES_CHANNELS_LIST_FETCH_COMPLETE;
                message.obj = channelList;
                mHandler.sendMessage(message);
            } catch (Exception e) {
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private class SmartInfoPreviewProgramChannelListTask extends FutureTask<List<PreviewProgramsChannel>> {
        private Handler mHandler;

        public SmartInfoPreviewProgramChannelListTask(SmartInfoPreviewProgramChannelListCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                List<PreviewProgramsChannel> channelList = get();
                android.util.Log.d(TAG, "SmartInfoPreviewProgramChannelListTask done() called  size " + channelList.size());
                Message message = mHandler.obtainMessage();
                message.what = UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAM_SMART_INFO_CHANNELS_LIST_FETCH_COMPLETE;
                message.obj = channelList;
                mHandler.sendMessage(message);
            } catch (Exception e) {
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private class UiThreadHandler extends Handler {
        protected static final int MSG_WHAT_PREVIEW_PROGRAM_CHANNELS_LIST_FETCH_COMPLETE = 100;
        protected static final int MSG_WHAT_PREVIEW_PROGRAMS_FETCH_COMPLETE = 200;
        protected static final int MSG_WHAT_PREVIEW_PROGRAMS_DB_CHANGE = 300;
        protected static final int MSG_WHAT_PREVIEW_PROGRAMS_UPDATE_FETCH_COMPLETE = 400;
        protected static final int MSG_WHAT_PREVIEW_PROGRAM_VOD_CHANNELS_LIST_FETCH_COMPLETE = 500;
        protected static final int MSG_WHAT_VOD_PREVIEW_PROGRAMS_FETCH_COMPLETE = 600;
        protected static final int MSG_WHAT_PREVIEW_PROGRAM_GAMES_CHANNELS_LIST_FETCH_COMPLETE = 700;
        protected static final int MSG_WHAT_GAMES_PREVIEW_PROGRAMS_FETCH_COMPLETE = 800;
        private static final int MSG_WHAT_PREVIEW_PROGRAM_SMART_INFO_CHANNELS_LIST_FETCH_COMPLETE = 900;
        private static final int MSG_WHAT_SMART_INFO_PREVIEW_PROGRAMS_FETCH_COMPLETE = 1000;
        private static final int MSG_WHAT_LOCALE_CHANGE = 1100;
        private static final int MSG_WHAT_START_FETCH_PREVIEW_PROGRAMS = 1200;

        private WeakReference<PreviewProgramsDataManager> mPreviewProgramsDataManagerRef;

        public UiThreadHandler(WeakReference<PreviewProgramsDataManager> ref) {
            mPreviewProgramsDataManagerRef = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_LOCALE_CHANGE:
                    Log.d(TAG, "handleMessage: MSG_WHAT_LOCALE_CHANGE");
                    removeMessages(MSG_WHAT_PREVIEW_PROGRAMS_DB_CHANGE);
                    fetchPreviewPrograms(0);
                    break;

                case MSG_WHAT_START_FETCH_PREVIEW_PROGRAMS:
                    Log.d(TAG, "handleMessage: MSG_WHAT_START_FETCH_PREVIEW_PROGRAMS");
                     fetchPreviewPrograms(0);
                     break;

                case MSG_WHAT_PREVIEW_PROGRAM_CHANNELS_LIST_FETCH_COMPLETE:
                    List<PreviewProgramsChannel> channelList = (List<PreviewProgramsChannel>) msg.obj;
                    UpdateDeletedChannels(channelList, false);
                    fetchPreviewProgramsForChannels(channelList, false);
                    break;
                case MSG_WHAT_PREVIEW_PROGRAMS_FETCH_COMPLETE:
                    PreviewProgramsDataManager previewProgramsDataManager = mPreviewProgramsDataManagerRef.get();
                    if (previewProgramsDataManager != null) {
                        notifyPreviewProgramAvailable((PreviewProgramsChannel) msg.obj, false);
                    }
                    break;

                case MSG_WHAT_PREVIEW_PROGRAM_VOD_CHANNELS_LIST_FETCH_COMPLETE:
                    List<PreviewProgramsChannel> vodChannelList = (List<PreviewProgramsChannel>) msg.obj;
                    fetchPreviewProgramsForChannels(vodChannelList, false);
                    break;

                case MSG_WHAT_VOD_PREVIEW_PROGRAMS_FETCH_COMPLETE:
                    android.util.Log.d(TAG, "handleMessage: MSG_WHAT_VOD_PREVIEW_PROGRAMS_FETCH_COMPLETE packageName " + ((PreviewProgramsChannel) msg.obj) != null ? ((PreviewProgramsChannel) msg.obj).getPackageName() : null);
                    PreviewProgramsDataManager previewProgramsDataManagerVod = mPreviewProgramsDataManagerRef.get();
                    if (previewProgramsDataManagerVod != null) {
                        notifyPreviewProgramAvailable((PreviewProgramsChannel) msg.obj, false);
                    }
                    break;

                case MSG_WHAT_PREVIEW_PROGRAM_GAMES_CHANNELS_LIST_FETCH_COMPLETE:
                    List<PreviewProgramsChannel> vodChannelListProg = (List<PreviewProgramsChannel>) msg.obj;
                    fetchPreviewProgramsForChannels(vodChannelListProg, false);
                    break;

                case MSG_WHAT_GAMES_PREVIEW_PROGRAMS_FETCH_COMPLETE:
                    PreviewProgramsDataManager previewProgramsDataManagerGames = mPreviewProgramsDataManagerRef.get();
                    if (previewProgramsDataManagerGames != null) {
                        notifyPreviewProgramAvailable((PreviewProgramsChannel) msg.obj, false);
                    }
                    break;

                case MSG_WHAT_PREVIEW_PROGRAM_SMART_INFO_CHANNELS_LIST_FETCH_COMPLETE:
                    List<PreviewProgramsChannel> smartInfoChannelListProg = (List<PreviewProgramsChannel>) msg.obj;
                    fetchSmartInfoPreviewProgramsForChannels(smartInfoChannelListProg);
                    break;

                case MSG_WHAT_SMART_INFO_PREVIEW_PROGRAMS_FETCH_COMPLETE:
                    List<PreviewProgramsChannel> smartInfochannelList = (List<PreviewProgramsChannel>) msg.obj;
                    notifySmartInfoPreviewProgramFetchComplete(smartInfochannelList);
                    break;

                case MSG_WHAT_PREVIEW_PROGRAMS_DB_CHANGE:
                    String _id = (String) msg.obj;
                    PreviewChannelDetails channelDetails = getPreviewChannelDetails(_id);
                    updatePreviewChannels(channelDetails);
                    break;

                case MSG_WHAT_PREVIEW_PROGRAMS_UPDATE_FETCH_COMPLETE:
                    PreviewProgramsDataManager previewProgramsDataManagerDb = mPreviewProgramsDataManagerRef.get();
                    if (previewProgramsDataManagerDb != null) {
                        notifyPreviewProgramAvailable((PreviewProgramsChannel) msg.obj, true);
                    }
                    break;
            }
        }
    }
    
    private void UpdateDeletedChannels(List<PreviewProgramsChannel> channelList, boolean isDBUpdate) {
        if (mPreviewProgramChannelsList != null && !mPreviewProgramChannelsList.isEmpty()) {
            for (PreviewProgramsChannel oldChannel : mPreviewProgramChannelsList.values()) {
                boolean found = false;
                for (PreviewProgramsChannel newChannel : channelList) {
                    if (oldChannel.equals(newChannel)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    DdbLogUtility.logRecommendationChapter("bharat", "UpdateDeletedChannels: called for package");
                    notifyPreviewChannelDeleted(oldChannel, false);
                }
            }
        }else{
            DdbLogUtility.logRecommendationChapter("bharat", "UpdateDeletedChannels: is not called : "+mPreviewProgramChannelsList);
        }
    }

    private void updatePreviewChannels(PreviewChannelDetails channelDetails) {
        if(channelDetails != null){
            DdbLogUtility.logCommon(TAG, "updatePreviewChannels _id " + channelDetails.get_id() + " mappedChannelId " + channelDetails.getMappedChannelId());
            int category = channelDetails.getCategory();
            if(category == Constants.CHANNEL_CONTENT_TYPE_SMART_INFO_RECOMMENDATION){
                fetchSmartInfoPreviewChannels(DashboardDataManager.getInstance().getSelectedSmartInfoPackage());
            }else {
                fetchPreviewPrograms();
            }
        }
    }

    private PreviewChannelDetails getPreviewChannelDetails(String _id) {
        Cursor c = executeQuery(getContext(), new PreviewProgramDataQuery(_id));
        PreviewChannelDetails details = null;
        if(c != null && c.moveToFirst()){
            details = new PreviewChannelDetails();
            details.set_id(_id);
            details.setMappedChannelId(c.getString(c.getColumnIndex(PreviewProgramDataQuery.COLUMN_MAPPED_CHANNEL_ID)));
            details.setCategory(c.getInt(c.getColumnIndex(PreviewProgramDataQuery.COLUMN_CATEGORY)));
        }
        if( c!= null) c.close();
        return details;
    }

    private String getMappedChannelId(String _id) {
        Query query = new PreviewProgramDataQuery(_id);
        Cursor c = executeQuery(getContext(), query);
        if(c != null && c.moveToFirst()){
            return c.getString(c.getColumnIndex(PreviewProgramDataQuery.COLUMN_MAPPED_CHANNEL_ID));
        }
        if (c != null) c.close();
        return "-1";
    }
    private void notifySmartInfoPreviewProgramFetchComplete(List<PreviewProgramsChannel> smartInfochannelList) {
        if (mSmartInfoListenerRefs != null) {
            DashboardDataManager.SmartInfoPreviewProgramListener smartInfoListenerRefs = mSmartInfoListenerRefs.get();
            if (smartInfoListenerRefs != null) {
				android.util.Log.d("Sup", "notifySmartInfoPreviewProgramFetchComplete ");
               smartInfoListenerRefs.onSmartinfoPreviewProgramFetchComplete(smartInfochannelList);
            }
        }else{
            DdbLogUtility.logRecommendationChapter(TAG, "notifySmartInfoPreviewProgramFetchComplete() - No client has registered");
        }
    }

    private PreviewProgramsChannel createEmptyRecommendations() {
        PreviewProgramsChannel emptyPreviewChannel = new PreviewProgramsChannel();
        emptyPreviewChannel.setCategory(Constants.CHANNEL_CONTENT_TYPE_EMPTY_RECOMMENDATION);
        String packageName = DashboardDataManager.getInstance().getSelectedSmartInfoPackage();
        DdbLogUtility.logRecommendationChapter(TAG, "createEmptyRecommendations packageName " + packageName);
        List<Recommendation> emptyRecommendations = new ArrayList<>(1);
        emptyPreviewChannel.setPackageName(packageName);

        Recommendation emptyRecommendation = createEmptyRecommendation(packageName);
        emptyRecommendation.setPendingIntent(createPendingIntent(packageName));
        emptyRecommendations.add(emptyRecommendation);

        emptyPreviewChannel.setPreviewProgramList(emptyRecommendations);
        return emptyPreviewChannel;
    }

    private Recommendation createEmptyRecommendation(String packageName) {
        Recommendation emptyRecommendation = new Recommendation();
        emptyRecommendation.setId(0);
        emptyRecommendation.setLogo(null);
        emptyRecommendation.setTitle(AppUtil.getAppName(getContext(), packageName));
        emptyRecommendation.setContentType(new String[]{Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION});
        return emptyRecommendation;
    }

    private PendingIntent createPendingIntent(String packageName) {
        Intent launchIntent = new Intent();
        launchIntent.setPackage(packageName);
        launchIntent.addCategory(Constants.CATEGORY_SMART_INFO);
        launchIntent.putExtra(Constants.EXTRA_JEDI_SHOW_UI, true);
        return PendingIntent.getService(getContext(), 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PreviewProgramsChannel getPreviewProgramChannel(String channelId) {
        if (!TextUtils.isEmpty(channelId)) {
            try {
                int id = Integer.parseInt(channelId);
                for (PreviewProgramsChannel channel : mPreviewProgramChannelsList.values()) {
                    if (channel.getId() == id) {
                        return channel;
                    }
                }
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }
    private void fetchPreviewProgramsForChannels(List<PreviewProgramsChannel> channelList, boolean isDBUpdate) {
        if (channelList != null && !channelList.isEmpty()) {
            for (PreviewProgramsChannel channel : channelList) {
                fetchPreviewProgramsForChannel(channel, isDBUpdate);
            }
        }
    }
    private void fetchPreviewProgramsForChannel(PreviewProgramsChannel channel, boolean isDBUpdate) {
        if(mPreviewProgramChannelsList != null) {
            mPreviewProgramChannelsList.clear();
            mPreviewProgramChannelsList = null;
        }
        if(channel != null) {
            PreviewProgramFetchCallable callable = new PreviewProgramFetchCallable(channel);
            PreviewProgramFetchTask previewProgramFetchTask = new PreviewProgramFetchTask(callable, mUiThreadHandler);
            previewProgramFetchTask.setDBUpdate(isDBUpdate);
            mThreadPoolExecutor.execute(previewProgramFetchTask);
        }
    }

    private void fetchSmartInfoPreviewProgramsForChannels(List<PreviewProgramsChannel> channelList) {
        if(channelList != null) {
            SmartInfoPreviewProgramFetchCallable callable = new SmartInfoPreviewProgramFetchCallable(channelList);
            SmartInfoPreviewProgramFetchTask previewProgramFetchTask = new SmartInfoPreviewProgramFetchTask(callable, mUiThreadHandler);
            mThreadPoolExecutor.execute(previewProgramFetchTask);
        }
    }

    private class PreviewProgramFetchCallable implements Callable<PreviewProgramsChannel> {

        private final PreviewProgramsChannel mPreviewChannel;

        public PreviewProgramFetchCallable(PreviewProgramsChannel channel) {
            mPreviewChannel = channel;
        }

        @Override
        public PreviewProgramsChannel call() throws Exception {
            return fetchPreviewProgramsForChannel(getContext(), mPreviewChannel);
        }
    }

    private class SmartInfoPreviewProgramFetchCallable implements Callable<List<PreviewProgramsChannel>> {

        private List<PreviewProgramsChannel> smartInfoChannelList = null;

        public SmartInfoPreviewProgramFetchCallable(List<PreviewProgramsChannel> channelList) {
            smartInfoChannelList = channelList;
        }

        @Override
        public List<PreviewProgramsChannel> call() throws Exception {
            if(mSmartInfoPreviewProgramChannelList != null){
                mSmartInfoPreviewProgramChannelList.clear();
            }
            for (PreviewProgramsChannel channel : smartInfoChannelList) {
                channel = fetchPreviewProgramsForChannel(getContext(), channel);
                if(isSmartInfoPreviewProgram(channel)) {
                    addToSmartInfoPreviewProgramsChannelsList(channel);
                }
            }
            return mSmartInfoPreviewProgramChannelList;
        }
    }

    private boolean isSmartInfoPreviewProgram(PreviewProgramsChannel channel) {
        if(channel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_SMART_INFO_RECOMMENDATION ||
                            channel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_EMPTY_RECOMMENDATION){
            return true;
        }
        return false;
    }

    private class SmartInfoPreviewProgramFetchTask extends FutureTask<List<PreviewProgramsChannel>> {
        private Handler mHandler;
        public SmartInfoPreviewProgramFetchTask(SmartInfoPreviewProgramFetchCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                List<PreviewProgramsChannel> channelList = get();
                Message message = mHandler.obtainMessage();
                message.what = UiThreadHandler.MSG_WHAT_SMART_INFO_PREVIEW_PROGRAMS_FETCH_COMPLETE;
                message.obj = channelList;
                mHandler.sendMessage(message);
            } catch (Exception e) {Log.e(TAG,"Exception :" +e.getMessage()); }
        }
    }

    private class PreviewProgramFetchTask extends FutureTask<PreviewProgramsChannel> {
        private Handler mHandler;
        private boolean isDBUpdate;

        public PreviewProgramFetchTask(PreviewProgramFetchCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                PreviewProgramsChannel channel = get();
                android.util.Log.d(TAG, "done: PreviewProgramFetchTask " + channel.getPackageName());
                Message message = mHandler.obtainMessage();
                if(!isDBUpdate) {
                    message.what = UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAMS_FETCH_COMPLETE;
                } else {
                    message.what = UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAMS_UPDATE_FETCH_COMPLETE;
                }
                message.obj = channel;
                mHandler.sendMessage(message);
            } catch (Exception e) {
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
        public void setDBUpdate(boolean DBUpdate) {
            this.isDBUpdate = DBUpdate;
        }
    }
    private void registerPreviewProgramsDbChanges() {
        getContext().getContentResolver().registerContentObserver(TvContract.PreviewPrograms.CONTENT_URI, true, mPreviewProgramDbObserver);
    }
    private ContentObserver mPreviewProgramDbObserver = new ContentObserver(mUiThreadHandler) {
        private final int REFRESH_WAIT_DELAY = 60*1000;
        @Override
            public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            String _id = uri.getLastPathSegment();//For Eg. getLastPathSegment will return 20556 from uri //content://android.media.tv/preview_program/20556
            if (TextUtils.isEmpty(_id)) return;

            if(mUiThreadHandler.hasMessages(getDBChangeMessage())){
                mUiThreadHandler.removeMessages(getDBChangeMessage());
            }
            Message message = mUiThreadHandler.obtainMessage(getDBChangeMessage());
            message.obj = _id;
            mUiThreadHandler.sendMessageDelayed(message, REFRESH_WAIT_DELAY);
        }

        private int getDBChangeMessage() {
            return UiThreadHandler.MSG_WHAT_PREVIEW_PROGRAMS_DB_CHANGE;
        }
    };

    public List<PreviewProgramsChannel> fetchPreviewProgramVODChannels(Context context) {
        DdbLogUtility.logRecommendationChapter("PreviewProgramsDataManager", "fetchPreviewProgramVODChannels() called ");
        List<PreviewProgramsChannel> previewProgramsVodChannels = new ArrayList<>();
        Cursor cursor = executeQuery(context, new PreviewProgramsChannelVODQuery());
        if (cursor != null && cursor.moveToFirst()) {
            do {
                PreviewProgramsChannel channel = new PreviewProgramsChannel();
                channel.setId(cursor.getInt(cursor.getColumnIndex( PreviewProgramsQuery.COLUMN_CHANNEL_ID)));
                channel.setPackageName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_PACKAGE_NAME)));
                channel.setDisplayName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)));
                channel.setDescription(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DESCRIPTION)));
                previewProgramsVodChannels.add(channel);
            } while (cursor.moveToNext());
        }
        if(cursor != null) cursor.close();

        return previewProgramsVodChannels;
    }

    public List<PreviewProgramsChannel> fetchPreviewProgramGamingChannels(Context context) {
        DdbLogUtility.logRecommendationChapter("PreviewProgramsDataManager", "fetchPreviewProgramGamingChannels() called ");
        List<PreviewProgramsChannel> previewProgramsGamesChannels = new ArrayList<>();
        Cursor cursor = executeQuery(context, new PreviewProgramsChannelGamesQuery());
        if (cursor != null && cursor.moveToFirst()) {
            do {
                PreviewProgramsChannel channel = new PreviewProgramsChannel();
                channel.setId(cursor.getInt(cursor.getColumnIndex( PreviewProgramsQuery.COLUMN_CHANNEL_ID)));
                channel.setPackageName(cursor.getString(cursor.getColumnIndex(HtvContract.HtvChannelList.COLUMN_PACKAGE_NAME)));
                channel.setDisplayName(cursor.getString(cursor.getColumnIndex(HtvContract.HtvChannelList.COLUMN_DISPLAY_NAME)));
                channel.setDescription(cursor.getString(cursor.getColumnIndex(HtvContract.HtvChannelList.COLUMN_DESCRIPTION)));
                previewProgramsGamesChannels.add(channel);
            } while (cursor.moveToNext());
        }
        if(cursor != null) cursor.close();

        return previewProgramsGamesChannels;
    }

    public List<PreviewProgramsChannel> fetchPreviewProgramSmartInfoChannels(Context context, String packageName) {
        List<PreviewProgramsChannel> previewProgramsSmartInfoChannels = new ArrayList<>();
        DdbLogUtility.logRecommendationChapter(TAG, "fetchPreviewProgramSmartInfoChannels packageName " + packageName);

        if(packageName == null)return null;

        Cursor cursor = executeQuery(context, new PreviewProgramsChannelSmartInfoQuery(packageName));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                PreviewProgramsChannel channel = new PreviewProgramsChannel();
                channel.setId(cursor.getInt(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_CHANNEL_ID)));
                channel.setPackageName(cursor.getString(cursor.getColumnIndex(HtvContract.HtvChannelList.COLUMN_PACKAGE_NAME)));
                channel.setDisplayName(cursor.getString(cursor.getColumnIndex(HtvContract.HtvChannelList.COLUMN_DISPLAY_NAME)));
                channel.setDescription(cursor.getString(cursor.getColumnIndex(HtvContract.HtvChannelList.COLUMN_DESCRIPTION)));
                previewProgramsSmartInfoChannels.add(channel);
            } while (cursor.moveToNext());
        }
        if(cursor != null) cursor.close();

        return previewProgramsSmartInfoChannels;
    }


    public PreviewProgramsChannel fetchPreviewProgramsForChannel(Context context, PreviewProgramsChannel previewChannel) {
        if(previewChannel != null) {
            setPreviewProgramsList(context, previewChannel);
        }
        return previewChannel;
    }
    private void setPreviewProgramsList(Context context, PreviewProgramsChannel channel) {
        List<Recommendation> recommendationList = new ArrayList<>();
        Cursor cursor = executeQuery(context, new PreviewProgramsQuery(channel.getId()));
        DdbLogUtility.logRecommendationChapter(TAG, "setPreviewProgramsList channelId " + channel.getId());
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_ID));
                int channel_id = cursor.getInt(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_MAPPED_CHANNEL_ID));
                String title = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_SHORT_DESC));
                String poster_art_uri = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_POSTER_ART_URI));
                String intent_uri = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_INTENT_URI));
                String packageName = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_PACKAGE_NAME));
                channel.setId(channel_id);
                int contentType = cursor.getInt(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_CHANNEL_CATEGORY));
                channel.setCategory(contentType);
                String [] mContentType = getContentType(contentType);

                Intent geoIntent = null;
                PendingIntent pendingIntent = null;
                if(intent_uri != null) {
                    geoIntent = new Intent(android.content.Intent.ACTION_VIEW);
                    try {
                        geoIntent = Intent.parseUri(intent_uri ,Intent.URI_INTENT_SCHEME);
                        PackageManager pm = getContext().getPackageManager();
                        List<ResolveInfo> ri = pm.queryIntentActivities(geoIntent, 0);
                        DdbLogUtility.logCommon(TAG," launchPreviewProgramVideo ResolveInfo.size() " + ri.size());
                        if(ri != null && ri.size() > 1) {
                            for (ResolveInfo resolveInfo: ri) {
                                if((resolveInfo.activityInfo.packageName).equalsIgnoreCase(packageName)){
                                    geoIntent.setPackage(packageName);
                                }
                            }
                        }
                    } catch (URISyntaxException e) {
                        Log.d(TAG,"Exception :"+e.getMessage());
                    }
                    pendingIntent = PendingIntent.getActivity(context, RECOMMENDATION_INTENT_CODE, geoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }

                String relaseDate = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_RELEASE_DATE));
                int durationMillis = cursor.getInt(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_DURATION_MILLIS));
                String offerPrice = cursor.getString(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_OFFER_PRICE));
                float reviewRating = cursor.getFloat(cursor.getColumnIndex(PreviewProgramsQuery.COLUMN_REVIEW_RATING));
                String reviewRatingStr = getReviewRating(reviewRating);
                String RelaseDate_Duration_OfferPrice_ReviewRating = concat(relaseDate, durationMillis, offerPrice, reviewRatingStr);

                Recommendation recommendation = new Recommendation(id, null, title, RelaseDate_Duration_OfferPrice_ReviewRating, description, RecommendationImportance.NONE, poster_art_uri,
                        mContentType, false, null, pendingIntent);
                recommendation.setLogoUrl(poster_art_uri);
                recommendationList.add(recommendation);
            } while (cursor.moveToNext());
            channel.setPreviewProgramList(recommendationList);
        }
        if(cursor != null) cursor.close();

    }

    private String concat(String relaseDate, int durationMillis, String offerPrice, String reviewRating){
        String result = relaseDate;

        String durationHourMin = convertToHourNMinuteFormat(durationMillis);
        if(durationHourMin != null){
            result = ((result != null) ? (result + SYMBOL_HYPHEN + durationHourMin) : durationHourMin);
        }

        if(offerPrice != null){
            result = ((result != null) ? (result + SYMBOL_HYPHEN + offerPrice) : offerPrice);
        }

        if(reviewRating != null){
            result = ((result != null) ? (result + SYMBOL_HYPHEN + reviewRating) : reviewRating);
        }
        return result;
    }

    private String getReviewRating(float reviewRating) {
        if(reviewRating < 0.0f) return null;

        String rating = new DecimalFormat(Constants.FORMAT_REVIEW_RATING).format(reviewRating);
        return isRaviewRatingValid(rating) ? rating  + Constants.ICON_STAR : null;
    }

    private String convertToHourNMinuteFormat(int durationMillis){
        if(durationMillis <= 0 ) return null;

        String h = String.format("%2d", TimeUnit.MILLISECONDS.toHours(durationMillis));
        String m = String.format("%02d",TimeUnit.MILLISECONDS.toMinutes(durationMillis) % TimeUnit.HOURS.toMinutes(1));
        boolean isHoursAvalable = isHoursAvailable(h);
        boolean isMinsAvalable = isMinsAvailable(m);
        String result = null;

        if(isMinsAvalable){
            result = isHoursAvalable ? (h.concat(SYMBOL_HOUR).concat(m).concat(SYMBOL_MIN)) : (m.concat(SYMBOL_MIN));
        }else if(isHoursAvalable){
            result = h.concat(SYMBOL_HOUR).concat(m).concat(SYMBOL_MIN);
        }
        return result;
    }

    private boolean isHoursAvailable(String h){
        return ((!TextUtils.isEmpty(h)) && (!h.trim().equals("0")));
    }

    private boolean isMinsAvailable(String m){
        return ((!TextUtils.isEmpty(m)) && (!m.trim().equals("00")));
    }

    private boolean isRaviewRatingValid(String rating){
        return ((!TextUtils.isEmpty(rating)) && (!rating.trim().equals("0")));
    }

    private String[] getContentType(final int contentType){
        switch(contentType){
            case Constants.CHANNEL_CONTENT_TYPE_APP_RECOMMENDATION:
                return new String[] {Constants.CONTENT_TYPE_APP_RECOMMENDATION};

            case Constants.CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION:
                return new String[] {Constants.CONTENT_TYPE_VOD_RECOMMENDATION};

            case Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION:
                return new String[] {Constants.CONTENT_TYPE_GAMING_RECOMMENDATION};

            case Constants.CHANNEL_CONTENT_TYPE_SMART_INFO_RECOMMENDATION:
                return new String[] {Constants.CONTENT_TYPE_SMART_INFO_RECOMMENDATION};

            default:
                return new String[] {Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION};
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

    @Override
    public void onAppListFetched() {
        if(mPreviewProgramChannelsList == null) {
            fetchPreviewPrograms();
        }else{
            DdbLogUtility.logRecommendationChapter(TAG, "PreviewPrograms already fetched");
        }
    }
}
