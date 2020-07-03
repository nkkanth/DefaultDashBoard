package org.droidtv.defaultdashboard.data.manager;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.UserHandle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelLogoFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelSettingsListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MyChoiceListener;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.data.model.channelFilter.AllChannelsFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.ChannelFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.MyChoiceChannelsFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.RadioChannelsFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.ThemeTvChannelsFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.TifChannelsFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.TvChannelsFilter;
import org.droidtv.defaultdashboard.data.query.AllChannelsQuery;
import org.droidtv.defaultdashboard.data.query.AllTifPackagesQuery;
import org.droidtv.defaultdashboard.data.query.MyChoiceChannelQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.data.query.RadioChannelsQuery;
import org.droidtv.defaultdashboard.data.query.ThemeTvChannelQuery;
import org.droidtv.defaultdashboard.data.query.ThemeTvInfoQuery;
import org.droidtv.defaultdashboard.data.query.TifChannelsQuery;
import org.droidtv.defaultdashboard.data.query.TvChannelsQuery;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants.MyChoicePackage;
import org.droidtv.defaultdashboard.util.Constants.ThemeTvGroup;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.htv.provider.HtvContract;
import org.droidtv.htv.provider.HtvContract.HtvThemeTvSetting;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.leanback.widget.Presenter;

/**
 * Created by sandeep.kumar on 15/11/2017.
 */

final class ChannelDataManager extends ContextualObject implements MyChoiceListener {

    private static final String TAG = "ChannelDataManager";

    private static final String ACTION_CHANNEL_LIST_UPDATED = "org.droidtv.euinstallertc.COMBINED_LIST_UPDATED";
    private static final String ACTION_CHANNEL_LIST_UPDATED_IN_PBS = "org.droidtv.channels.COMBINED_LIST_UPDATED_INPBS";
    private static final String ACTION_CHANNEL_LIST_CLONE_IN_COMPLETED = "clone.channellist.intent.clone_in_end";
    private static final String ACTION_THEMETV_DATA_CHANGED = "org.droidtv.themetv.THEMETV_DATA_CHANGED";

    private static final int CHANNEL_FILTER_CLEANUP_COUNT_NA = -1;

    private static final int TV_CHANNELS_ENABLED = 1;
    private static final int RADIO_CHANNELS_DISABLED = 0;
    private static final int TIF_CHANNELS_ENABLED = 1;
    private static final int DISPLAY_ALL_CHANNELS_ENABLED = 1;

    private static final int LOGO_ON = 1;

    private ThreadPoolExecutor mThreadPoolExecutor;
    private ArrayList<WeakReference<ChannelDataListener>> mChannelDataListenerRefs;
    private ArrayList<WeakReference<ChannelSettingsListener>> mChannelSettingsListenerRefs;
    private ArrayList<ChannelFilter> mAvailableChannelFilters;
    private ChannelFilter mActiveChannelFilter;
    private UiThreadHandler mUiThreadHandler;
    private ChannelLogoCache mChannelLogoCache;
    private ITvSettingsManager mTvSettingsManager;
    private boolean mEpgEnabled;
    private int mEpgSource;
    private boolean mAreChannelLogosEnabled;
    private String mLastSelectedChannelUriString;

    ChannelDataManager(Context context) {
        super(context);

        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.CHANNEL_LIST_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler(new WeakReference<ChannelDataManager>(this));
        mChannelDataListenerRefs = new ArrayList<>();
        mChannelSettingsListenerRefs = new ArrayList<>();
        mAvailableChannelFilters = new ArrayList<>();
        mActiveChannelFilter = null;
        mChannelLogoCache = new ChannelLogoCache();
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        updateEpgEnabledState();
        updateEpgSource();
        updateChannelLogoEnabledState();
        updateLastSelectedChannelUri();

        registerForChannelListUpdates();
        registerMyChoiceListener();
    }

    void fetchFilters() {
        if (areChannelsEnabled()) {
            fetchActiveChannelFilter();
            fetchAvailableChannelFilters();
        }
    }

    boolean areChannelsEnabled() {
        return areRFIPChannelsEnabled() || areMediaChannelsEnabled() || areTifChannelsEnabled();
    }

    boolean areRFIPChannelsEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_SHOW_CHANNELS, 0, 0) ==
                TvSettingsDefinitions.PbsShowChannels.PBSMGR_CHANNELS_SHOW_CHANNELS_ON;
    }

    boolean areDisplayFilterAllChannelsEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_All_CHANNELS, 0, 0) == DISPLAY_ALL_CHANNELS_ENABLED;
    }

    boolean areRadioChannelsEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_RADIO_CHANNELS, 0, 0) != RADIO_CHANNELS_DISABLED;
    }

    boolean areTvChannelsEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_TV_CHANNELS, 0, 0) == TV_CHANNELS_ENABLED;
    }

    boolean areMediaChannelsEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_AV_MEDIA, 0, 0) ==
                TvSettingsDefinitions.PbsAvMediaConstants.PBSMGR_AV_MEDIA_ON;
    }

    boolean areTifChannelsEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_OTT_APP_CHANNELS, 0, 0) == TIF_CHANNELS_ENABLED;
    }

    boolean areTifChannelsDisplayEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_OTT_APP, 0, 0) == TIF_CHANNELS_ENABLED;
    }

    boolean areTifChannelsInChannelsListEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_TIF_IN_CHANNEL_LIST, 0, 0) == TIF_CHANNELS_ENABLED;
    }

    boolean isThemeTvEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_THEME_TV_ENABLE, 0, 0) ==
                TvSettingsDefinitions.PbsThemeTvEnableConstants.PBSMGR_THEME_TV_YES;
    }

    boolean isEpgEnabled() {
        return mEpgEnabled;
    }

    boolean isEpgSourceBcepg() {
        return mEpgSource == TvSettingsDefinitions.InstSettingsDvbEpgConstants.DVBEPG;
    }

    boolean isEpgSourceApp(){
        //TODO: Need to change to TvSettingsConstants once below constant will be available
        return mEpgSource == 3;//TvSettingsDefinitions.InstSettingsDvbEpgConstants.EPGAPP;
    }

    boolean areChannelLogosEnabled() {
        return mAreChannelLogosEnabled;
    }

    void fetchAvailableChannelFilters() {
        AvailableChannelFiltersCallable channelFiltersCallable = new AvailableChannelFiltersCallable(getContext());
        AvailableChannelFiltersFetchTask task = new AvailableChannelFiltersFetchTask(channelFiltersCallable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    void fetchActiveChannelFilter() {
        ActiveChannelFilterCallable activeChannelFilterCallable = new ActiveChannelFilterCallable(getContext());
        ActiveChannelFilterFetchTask task = new ActiveChannelFilterFetchTask(activeChannelFilterCallable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    List<ChannelFilter> getAvailableChannelFilters() {
        return mAvailableChannelFilters;
    }

    ChannelFilter getActiveChannelFilter() {
        return mActiveChannelFilter;
    }

    int getActiveChannelFilterId() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_ACTIVE_CHANNELFILTER, 0, 0);
    }

    void setActiveChannelFilterId(int filterId) {
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_ACTIVE_CHANNELFILTER, 0, filterId);
    }

    String getActiveTifInputId() {
        return mTvSettingsManager.getString(TvSettingsConstants.PBSMGR_PROPERTY_ACTIVE_TIF_INPUTID, 0, "");
    }

    void setActiveTifInputId(String tifInputId) {
        mTvSettingsManager.putString(TvSettingsConstants.PBSMGR_PROPERTY_ACTIVE_TIF_INPUTID, 0, tifInputId);
    }

    int getLastSelectedChannelId() {
        int lastSelectedChannelId = -1;
        String lastSelectedChannelUriString = getLastSelectedChannelUriString();
        try {
            Uri lastSelectedChannelUri = Uri.parse(getLastSelectedChannelUriString());
            lastSelectedChannelId = (int) ContentUris.parseId(lastSelectedChannelUri);
        } catch (NumberFormatException | UnsupportedOperationException | NullPointerException e) {
            Log.w(TAG, "getLastSelectedChannelId failed.lastSelectedChannelUri:" + lastSelectedChannelUriString);
        }
        DdbLogUtility.logCommon(TAG, "getLastSelectedChannelId " + lastSelectedChannelId);
        return lastSelectedChannelId;
    }

    boolean registerChannelDataListener(ChannelDataListener channelDataListener) {
        if (channelDataListener == null) {
            return false;
        }

        for (int i = 0; i < mChannelDataListenerRefs.size(); i++) {
            WeakReference<ChannelDataListener> ref = mChannelDataListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ChannelDataListener listener = ref.get();
            if (listener != null && listener.equals(channelDataListener)) {
                return false;
            }
        }
        return mChannelDataListenerRefs.add(new WeakReference<ChannelDataListener>(channelDataListener));
    }

    boolean unregisterChannelDataListener(ChannelDataListener channelDataListener) {
        for (int i = 0; i < mChannelDataListenerRefs.size(); i++) {
            WeakReference<ChannelDataListener> ref = mChannelDataListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ChannelDataListener listener = ref.get();
            if (listener != null && listener.equals(channelDataListener)) {
                return mChannelDataListenerRefs.remove(ref);
            }
        }
        return false;
    }

    boolean registerChannelSettingsListener(ChannelSettingsListener channelSettingsListener) {
        if (channelSettingsListener == null) {
            return false;
        }

        for (int i = 0; i < mChannelSettingsListenerRefs.size(); i++) {
            WeakReference<ChannelSettingsListener> ref = mChannelSettingsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ChannelSettingsListener listener = ref.get();
            if (listener != null && listener.equals(channelSettingsListener)) {
                return false;
            }
        }
        return mChannelSettingsListenerRefs.add(new WeakReference<ChannelSettingsListener>(channelSettingsListener));
    }

    boolean unregisterChannelSettingsListener(ChannelSettingsListener channelSettingsListener) {
        for (int i = 0; i < mChannelSettingsListenerRefs.size(); i++) {
            WeakReference<ChannelSettingsListener> ref = mChannelSettingsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ChannelSettingsListener listener = ref.get();
            if (listener != null && listener.equals(channelSettingsListener)) {
                return mChannelSettingsListenerRefs.remove(ref);
            }
        }
        return false;
    }

    void cleanUpChannelFilters(List<ChannelFilter> channelFilters) {
        CleanUpChannelFiltersCallable callable = new CleanUpChannelFiltersCallable(channelFilters);
        CleanUpChannelFiltersTask task = new CleanUpChannelFiltersTask(callable, mUiThreadHandler);
        mThreadPoolExecutor.execute(task);
    }

    void cleanUpChannelFilter(ChannelFilter channelFilter) {
        if (channelFilter == null) {
            return;
        }
        ArrayList<ChannelFilter> channelFilters = new ArrayList<>();
        channelFilters.add(channelFilter);
        cleanUpChannelFilters(channelFilters);
    }

    void cleanUpAvailableChannelFilters() {
        cleanUpChannelFilters(mAvailableChannelFilters);
    }

    void cleanUpActiveChannelFilter() {
        cleanUpChannelFilter(mActiveChannelFilter);
    }

    void fetchChannelLogo(Channel channel, ChannelLogoFetchListener listener) {
        Bitmap logo = getLogoFromCache(channel.getId());
        if (logo != null) {
            if (listener != null) {
                listener.onChannelLogoFetchComplete(channel.getId(), logo);
            }
        } else {
            fetchChannelLogoInternal(channel, listener);
        }
    }

    void clearChannelLogoCache() {
        mChannelLogoCache.clear();
    }

    void onConfigurationChanged() {
        clearChannelLogoCache();
        fetchFilters();
    }

    private void updateEpgEnabledState() {
        mEpgEnabled = (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_EPG, 0, 0) ==
                TvSettingsDefinitions.PbsEnableEpg.PBSMGR_ENABLE_EPG_ON);
    }

    private void updateEpgSource() {
        mEpgSource = mTvSettingsManager.getInt(TvSettingsConstants.INSTSETTINGSDVBEPGCHOICE, 0, 0);
    }

    private void updateChannelLogoEnabledState() {
        mAreChannelLogosEnabled = (mTvSettingsManager.getInt(TvSettingsConstants.INSTSETTINGSCHANNELLOGOS, 0, 0) == LOGO_ON);
    }


    private void updateLastSelectedChannelUri() {
        mLastSelectedChannelUriString = DashboardDataManager.getInstance().getTvSettingsManager().getString(TvSettingsConstants.LASTSELECTEDURI, 0, null);
    }

    private void updateEpgEnabledState(int value) {
        mEpgEnabled = (value ==
                TvSettingsDefinitions.PbsEnableEpg.PBSMGR_ENABLE_EPG_ON);
    }

    private void updateEpgSource(int value) {
        mEpgSource = value;
    }

    private void updateChannelLogoEnabledState(int value) {
        mAreChannelLogosEnabled = (value == LOGO_ON);
    }

    private void fetchChannelLogoInternal(Channel channel, final ChannelLogoFetchListener listener) {
        ChannelLogoFetchCallable callable = new ChannelLogoFetchCallable(getContext(), channel);
        ChannelLogoFetchTask task = new ChannelLogoFetchTask(callable, mUiThreadHandler, new ChannelLogoFetchListener() {
            @Override
            public void onChannelLogoFetchComplete(int channelId, Bitmap logo) {
                if (listener != null) {
                    listener.onChannelLogoFetchComplete(channelId, logo);
                }
                if (logo != null) {
                    addLogoToCache(channelId, logo);
                }
            }
        });
        mThreadPoolExecutor.execute(task);
    }

    private void addLogoToCache(int channelId, Bitmap logo) {
        mChannelLogoCache.addLogo(channelId, logo);
    }

    private Bitmap getLogoFromCache(int channelId) {
        return mChannelLogoCache.getLogo(channelId);
    }

    private void registerForChannelListUpdates() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CHANNEL_LIST_UPDATED);
        intentFilter.addAction(ACTION_CHANNEL_LIST_UPDATED_IN_PBS);
        intentFilter.addAction(ACTION_CHANNEL_LIST_CLONE_IN_COMPLETED);
        intentFilter.addAction(ACTION_THEMETV_DATA_CHANGED);
        getContext().registerReceiverAsUser(mChannelListUpdatedReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    private void registerMyChoiceListener() {
        DashboardDataManager.getInstance().addMyChoiceListener(this);
    }

    private static Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().
                query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    public Drawable getSourceIcon(Presenter.ViewHolder holder, Channel channel) {
        List<Source> sources = DashboardDataManager.getInstance().getSources();
        List<Source> tmpSources = new ArrayList<>(sources);
        int portid = extractPortId(channel.getInputId());
        Log.d(TAG, "getSourceIcon channel inputid " + channel.getInputId());
        for (Source source : tmpSources) {
            if (source.getHDMIPortId() == portid) {
                return source.getIcon();
            }
        }
        return holder.view.getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_203_hdmi_n_98x98);
    }

    /**
     * This function will return portid from HDMI inputid
     * For Eg. input id is hdmi.HDMIInputService/HW5 then it will return (5 - 4 = ) 1
     *
     * @param inputid
     * @return portid
     */
    private int extractPortId(String inputid) {
        int portid = -1;
        try {
            if (inputid != null) {
                portid = Integer.parseInt(inputid.split("HW")[1]) - DashboardDataManager.getInstance().getOffset();
            }
        } catch (NumberFormatException | NullPointerException e) {
        }
        Log.d(TAG, "extractPortId returns " + portid);
        return portid;
    }

    private static ChannelFilter getAllChannelsFilter(Context context, boolean tvChannelsEnabled, boolean radioChannelsEnabled, boolean mediaChannelsEnabled,
                                                      boolean tifChannelsEnabled, boolean hdmi1Enabled, boolean hdmi2Enabled, boolean hdmi3Enabled,
                                                      boolean hdmi4Enabled, boolean vgaEnabled) {
        AllChannelsQuery allChannelsQuery = new AllChannelsQuery(tvChannelsEnabled, radioChannelsEnabled, mediaChannelsEnabled, tifChannelsEnabled,
                hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
        Cursor cursor = executeQuery(context, allChannelsQuery);
        return new AllChannelsFilter(context, cursor);
    }

    private static ChannelFilter getTvChannelsFilter(Context context) {
        TvChannelsQuery tvChannelsQuery = new TvChannelsQuery();
        Cursor cursor = executeQuery(context, tvChannelsQuery);
        return new TvChannelsFilter(context, cursor);
    }

    private static List<ChannelFilter> getAllTifFilters(Context context, boolean radioChannelsEnabled) {
        List<ChannelFilter> allTifFilters = new ArrayList<>();
        AllTifPackagesQuery allTifPackagesQuery = new AllTifPackagesQuery();
        Cursor allTifPackagesCursor = executeQuery(context, allTifPackagesQuery);
        try {
            while (allTifPackagesCursor != null && allTifPackagesCursor.moveToNext()) {
                String inputId = allTifPackagesCursor.getString(AllTifPackagesQuery.INDEX_COLUMN_INPUT_ID);
                String tifInputPackageName = allTifPackagesCursor.getString(AllTifPackagesQuery.INDEX_COLUMN_PACKAGE_NAME);
                TifChannelsQuery tifChannelsQuery = new TifChannelsQuery(inputId, radioChannelsEnabled);
                Cursor cursor = executeQuery(context, tifChannelsQuery);
                if (cursor != null && cursor.getCount() > 0) {
                    String tifInputLabel = DashboardDataManager.getInstance().getTvInputLabel(inputId);
                    if (TextUtils.isEmpty(tifInputLabel)) {
                        tifInputLabel = getApplicationName(context, tifInputPackageName);
                    }
                    Drawable tifIcon = DashboardDataManager.getInstance().getTvInputIcon(inputId);
                    if (tifIcon == null) {
                        tifIcon = getApplicationIcon(context, tifInputPackageName);
                    }
                    allTifFilters.add(new TifChannelsFilter(inputId, tifInputLabel, tifIcon, cursor));
                }
            }
        } finally {
            if (allTifPackagesCursor != null) {
                allTifPackagesCursor.close();
            }
        }
        return allTifFilters;
    }

    private static ChannelFilter getTifFilter(Context context, String inputId, boolean radioChannelsEnabled) {
        TifChannelsQuery tifChannelsQuery = new TifChannelsQuery(inputId, radioChannelsEnabled);
        Cursor cursor = executeQuery(context, tifChannelsQuery);

        String tifInputLabel = DashboardDataManager.getInstance().getTvInputLabel(inputId);
        Drawable tifIcon = DashboardDataManager.getInstance().getTvInputIcon(inputId);
        return new TifChannelsFilter(inputId, tifInputLabel, tifIcon, cursor);
    }

    private static ChannelFilter getRadioChannelsFilter(Context context, boolean tifChannelsEnabled) {
        RadioChannelsQuery radioChannelsQuery = new RadioChannelsQuery(tifChannelsEnabled);
        Cursor cursor = executeQuery(context, radioChannelsQuery);
        return new RadioChannelsFilter(context, cursor);
    }

    private static ChannelFilter getMyChoiceChannelsFilter(Context context, MyChoicePackage myChoicePackage, boolean tvChannelsEnabled,
                                                           boolean radioChannelsEnabled, boolean mediaChannelsEnabled, boolean tifChannesEnabled,
                                                           boolean hdmi1Enabled, boolean hdmi2Enabled, boolean hdmi3Enabled, boolean hdmi4Enabled,
                                                           boolean vgaEnabled) {
        MyChoiceChannelQuery myChoiceChannelQuery = new MyChoiceChannelQuery(myChoicePackage, tvChannelsEnabled, radioChannelsEnabled, mediaChannelsEnabled,
                tifChannesEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
        Cursor cursor = executeQuery(context, myChoiceChannelQuery);
        return new MyChoiceChannelsFilter(context, myChoicePackage, cursor);
    }

    private static ChannelFilter getThemeTvChannelsFilter(Context context, ThemeTvGroup themeTvGroup, boolean tvChannelsEnabled, boolean radioChannelsEnabled,
                                                          boolean mediaChannelsEnabled, boolean tifChannesEnabled, boolean hdmi1Enabled, boolean hdmi2Enabled,
                                                          boolean hdmi3Enabled, boolean hdmi4Enabled, boolean vgaEnabled) {
        ThemeTvChannelQuery themeTvChannelQuery = new ThemeTvChannelQuery(themeTvGroup, tvChannelsEnabled, radioChannelsEnabled, mediaChannelsEnabled, tifChannesEnabled,
                hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
        Cursor cursor = executeQuery(context, themeTvChannelQuery);

        ThemeTvInfo themeTvInfo = getThemeTvInfo(context, themeTvGroup);

        return new ThemeTvChannelsFilter(themeTvGroup, context, themeTvInfo.mTitle, themeTvInfo.mLogo, cursor);
    }

    private static ThemeTvInfo getThemeTvInfo(Context context, ThemeTvGroup themeTvGroup) {
        Cursor cursor = null;
        ThemeTvInfo themeTvInfo = new ThemeTvInfo();
        themeTvInfo.mTitle = context.getString(org.droidtv.ui.htvstrings.R.string.HTV_THEME_THEME_TV);
        themeTvInfo.mLogo = context.getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_74_tv_rc_n_48x48);
        try {
            ThemeTvInfoQuery themeTvInfoQuery = new ThemeTvInfoQuery(themeTvGroup);
            cursor = executeQuery(context, themeTvInfoQuery);
            if (cursor != null && cursor.moveToNext()) {
                themeTvInfo.mTitle = cursor.getString(cursor.getColumnIndex(HtvThemeTvSetting.COLUMN_NAME));
                String logoPath = cursor.getString(cursor.getColumnIndex(HtvThemeTvSetting.COLUMN_LOGO_URI));
                themeTvInfo.mLogo = Drawable.createFromPath(logoPath);
            }
        } catch (SQLiteException e) {
            Log.w(TAG, "#### exception when fetching themetv info");
           Log.e(TAG,"Exception :" +e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return themeTvInfo;
    }

    private static String getApplicationName(Context context, String packageName) {
        String applicationName = packageName;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            applicationName = applicationInfo.loadSafeLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "getApplicationName NameNotFoundException.package:" + packageName);
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        DdbLogUtility.logCommon(TAG, "getApplicationName " + applicationName);
        return applicationName;
    }

    private static Drawable getApplicationIcon(Context context, String packageName) {
        Drawable applicationIcon = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            applicationIcon = applicationInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "getApplicationIcon NameNotFoundException.package:" + packageName);
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return applicationIcon;
    }

    private String getLastSelectedChannelUriString() {
        return mLastSelectedChannelUriString;
    }

    private void notifyAvailableChannelFiltersFetchComplete(List<ChannelFilter> channelFilters) {
        ArrayList<ChannelFilter> oldChannelFilters = mAvailableChannelFilters;
        if (channelFilters == null) {
            mAvailableChannelFilters = new ArrayList<>();
        } else {
            mAvailableChannelFilters = new ArrayList<>(channelFilters);
        }
        for (int i = 0; mChannelDataListenerRefs != null && i < mChannelDataListenerRefs.size(); i++) {
            WeakReference<ChannelDataListener> listenerRef = mChannelDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            ChannelDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onAvailableChannelFiltersFetched(mAvailableChannelFilters);
            }
        }

        // Clean-up old channel filters
        /*Commented out because StaleDataException was being thrown in some cases due to cursor being closed*/
        //cleanUpChannelFilters(oldChannelFilters);
    }

    private void notifyActiveChannelFilterFetchComplete(ChannelFilter activeChannelFilter) {
        ChannelFilter oldActiveChannelFilter = mActiveChannelFilter;
        mActiveChannelFilter = activeChannelFilter;
        for (int i = 0; mChannelDataListenerRefs != null && i < mChannelDataListenerRefs.size(); i++) {
            WeakReference<ChannelDataListener> listenerRef = mChannelDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            ChannelDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onActiveChannelFilterFetched(mActiveChannelFilter);
            }
        }

        // Clean-up old channel active channel filter
        /*Commented out because StaleDataException was being thrown in some cases due to cursor being closed*/
        //cleanUpChannelFilter(oldActiveChannelFilter);
    }

    private void notifyChannelFiltersCleanUpComplete(int successCount, int failureCount) {
        Log.d(TAG, "#### channels filter cleanup complete.successCount:" + successCount + ",failureCount:" + failureCount);
    }

    private void notifyEnableChannelsSettingChanged(boolean channelsEnabled) {
        for (int i = 0; mChannelSettingsListenerRefs != null && i < mChannelSettingsListenerRefs.size(); i++) {
            WeakReference<ChannelSettingsListener> listenerRef = mChannelSettingsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            ChannelSettingsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onChannelsSettingChanged(channelsEnabled);
            }
        }
    }

    private void notifyChannelLogoEnabledStateChanged(boolean logosEnabled) {
        for (int i = 0; mChannelDataListenerRefs != null && i < mChannelDataListenerRefs.size(); i++) {
            WeakReference<ChannelDataListener> listenerRef = mChannelDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            ChannelDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onChannelLogoEnabledStateChanged(logosEnabled);
            }
        }
    }

    private void notifyLastSelectedChannelUriChanged(String lastSelectedChannelUriString) {
        for (int i = 0; mChannelDataListenerRefs != null && i < mChannelDataListenerRefs.size(); i++) {
            WeakReference<ChannelDataListener> listenerRef = mChannelDataListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            ChannelDataListener listener = listenerRef.get();
            if (listener != null) {
                listener.onLastSelectedChannelUriChanged(lastSelectedChannelUriString);
            }
        }
    }

    private void onActiveChannelFilterChanged() {
        fetchActiveChannelFilter();
    }

    private void onActiveChannelFilterChanged(int value) {
        fetchActiveChannelFilter();
    }

    private void onEnableChannelsSettingChanged(int value) {
        notifyEnableChannelsSettingChanged(areChannelsEnabled());
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onDisplayFilterSettingsChanged(int value) {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onEnableTvChannelsSettingChanged(int value) {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onEnableRadioChannelsSettingChanged(int value) {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onEnableMediaChannelsSettingChanged(int value) {
        notifyEnableChannelsSettingChanged(areChannelsEnabled());
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onEnableTifChannelsSettingChanged(int value) {
        notifyEnableChannelsSettingChanged(areChannelsEnabled());
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onEnableThemeTvSettingChanged(int value) {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onSourcesSettingChanged(int value) {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onChannelListUpdated() {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onChannelListCloneInCompleted() {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onThemeTvDataChanged() {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    private void onEpgEnabledSettingChanged(int value) {
        updateEpgEnabledState(value);
    }

    private void onEpgSourceChanged(int value) {
        updateEpgSource(value);
    }

    private void onChannelLogoEnabledStateChanged(int value) {
        updateChannelLogoEnabledState(value);
        notifyChannelLogoEnabledStateChanged(areChannelLogosEnabled());
    }

    private void onLastSelectedChannelUriChanged() {
        updateLastSelectedChannelUri();
        notifyLastSelectedChannelUriChanged(getLastSelectedChannelUriString());
    }

    private void myChoiceDataChanged() {
        fetchActiveChannelFilter();
        fetchAvailableChannelFilters();
    }

    @Override
    public void onMyChoiceLockStatusChanged() {
        myChoiceDataChanged();
    }

    @Override
    public void onMyChoiceEnabledStateChanged() {
        myChoiceDataChanged();
    }

    @Override
    public void onMyChoiceEnabledStateChanged(int value) {
        myChoiceDataChanged();
    }

    @Override
    public void onMyChoiceDataChanged() {
        myChoiceDataChanged();
    }

    private static void sendMessage(Handler handler, int what) {
        Message message = Message.obtain(handler, what);
        message.sendToTarget();
    }

    private static void sendMessage(Handler handler, int what, Object object) {
        Message message = Message.obtain(handler, what, object);
        message.sendToTarget();
    }

    void activeChannelFilterChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ACTIVE_CHANNEL_FILTER_CHANGED);
    }

    void activeChannelFilterChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ACTIVE_CHANNEL_FILTER_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableChannelsSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_CHANNELS_SETTING_CHANGED);
    }

    void enableChannelsSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_CHANNELS_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableDisplayFilterAllChannels(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_DISPLAY_FILTER_ALL_CHANNELS);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableDisplayFilterOTTApp(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_DISPLAY_FILTER_OTT_APP);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableOTTAppInChannels(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_OTT_APP_IN_CHANNELS);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableTvChannelsSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_TV_CHANNELS_SETTING_CHANGED);
    }

    void enableTvChannelsSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_TV_CHANNELS_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableRadioChannelsSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_RADIO_CHANNELS_SETTING_CHANGED);
    }

    void enableRadioChannelsSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_RADIO_CHANNELS_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableMediaChannelsSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_MEDIA_CHANNELS_SETTING_CHANGED);
    }

    void enableMediaChannelsSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_MEDIA_CHANNELS_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableTifChannelsSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_TIF_CHANNELS_SETTING_CHANGED);
    }

    void enableTifChannelsSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_TIF_CHANNELS_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableThemeTvSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_THEME_TV_SETTING_CHANGED);
    }

    void enableThemeTvSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ENABLE_THEME_TV_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void sourcesSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_SOURCES_SETTING_CHANGED);
    }

    void sourcesSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_SOURCES_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableEpgSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_EPG_ENABLED_SETTING_CHANGED);
    }

    void enableEpgSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_EPG_ENABLED_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void epgSourceChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_EPG_SOURCE_CHANGED);
    }

    void epgSourceChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_EPG_SOURCE_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void enableChannelLogosSettingChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_CHANNEL_LOGO_ENABLED_STATE_CHANGED);
    }

    void enableChannelLogosSettingChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_CHANNEL_LOGO_ENABLED_STATE_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void lastSelectedChannelUriChanged() {
        sendMessage(mUiThreadHandler, UiThreadHandler.MSG_WHAT_LAST_SELECTED_CHANNEL_URI_CHANGED);
    }


    private static class AvailableChannelFiltersCallable implements Callable<List<ChannelFilter>> {

        private Context mContext;

        AvailableChannelFiltersCallable(Context context) {
            mContext = context;
        }

        @Override
        public List<ChannelFilter> call() throws Exception {
            ArrayList<ChannelFilter> filters = new ArrayList<>();

            boolean areRFIPChannelsEnabled = DashboardDataManager.getInstance().areRFIPChannelsEnabled();
            boolean tvChannelsEnabled = DashboardDataManager.getInstance().areTvChannelsEnabled();

            boolean radioChannelsDisplayEnabled = DashboardDataManager.getInstance().areRadioChannelsEnabled();
            boolean mediaChannelsEnabled = DashboardDataManager.getInstance().areMediaChannelsEnabled();

            boolean tifChannelsEnabled = DashboardDataManager.getInstance().areTifChannelsEnabled();
            boolean tifChannelsDisplayEnabled = DashboardDataManager.getInstance().areTifChannelsDisplayEnabled();
            boolean tifChannelsInChannelsListEnabled = DashboardDataManager.getInstance().areTifChannelsInChannelsListEnabled();

            boolean themeTvEnabled = DashboardDataManager.getInstance().isThemeTvEnabled();
            boolean myChoiceEnabled = DashboardDataManager.getInstance().isMyChoiceEnabled();
            boolean hdmi1Enabled = DashboardDataManager.getInstance().isHdmi1Enabled();
            boolean hdmi2Enabled = DashboardDataManager.getInstance().isHdmi2Enabled();
            boolean hdmi3Enabled = DashboardDataManager.getInstance().isHdmi3Enabled();
            boolean hdmi4Enabled = DashboardDataManager.getInstance().isHdmi4Enabled();
            boolean vgaEnabled = DashboardDataManager.getInstance().isVgaEnabled();
            boolean areDisplayFilterAllChannelsEnabled = DashboardDataManager.getInstance().areDisplayFilterAllChannelsEnabled();
            DdbLogUtility.logCommon(TAG, "AvailableChannelFiltersCallable call() areRFIPChannelsEnabled " + areRFIPChannelsEnabled
                                                + " tvChannelsEnabled " + tvChannelsEnabled
                                                + " radioChannelsDisplayEnabled " + radioChannelsDisplayEnabled
                                                + " mediaChannelsEnabled " + mediaChannelsEnabled
                                                + " tifChannelsEnabled " + tifChannelsEnabled
                                                + " tifChannelsDisplayEnabled " + tifChannelsDisplayEnabled
                                                + " themeTvEnabled " + themeTvEnabled
                                                + " myChoiceEnabled " + myChoiceEnabled
                                                + " hdmi1Enabled " + hdmi1Enabled
                                                + " hdmi2Enabled " + hdmi2Enabled
                                                + " hdmi3Enabled " + hdmi3Enabled
                                                + " hdmi4Enabled " + hdmi4Enabled
                                                + " vgaEnabled " + vgaEnabled);

            if (areDisplayFilterAllChannelsEnabled) {
                boolean tiffChannels = (tifChannelsEnabled && tifChannelsInChannelsListEnabled);
                ChannelFilter allChannelsFilter = getAllChannelsFilter(mContext, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled, tiffChannels,
                        hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                if (allChannelsFilter.hasChannels()) {
                    filters.add(allChannelsFilter);
                }
            }

            if (areRFIPChannelsEnabled && tvChannelsEnabled) {
                ChannelFilter tvChannelsFilter = getTvChannelsFilter(mContext);
                if (tvChannelsFilter.hasChannels()) {
                    filters.add(tvChannelsFilter);
                }
            }

            if (tifChannelsDisplayEnabled && tifChannelsEnabled) {
                List<ChannelFilter> allTifFilters = getAllTifFilters(mContext, radioChannelsDisplayEnabled);
                filters.addAll(allTifFilters);
            }

            if (areRFIPChannelsEnabled && radioChannelsDisplayEnabled) {
                ChannelFilter radioChannelsFilter = getRadioChannelsFilter(mContext, tifChannelsEnabled);
                if (radioChannelsFilter.hasChannels()) {
                    filters.add(radioChannelsFilter);
                }
            }

            if (myChoiceEnabled) {
                for (MyChoicePackage myChoicePackage : MyChoicePackage.values()) {
                    ChannelFilter myChoiceFilter = getMyChoiceChannelsFilter(mContext, myChoicePackage, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                            tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    if (myChoiceFilter.hasChannels()) {
                        filters.add(myChoiceFilter);
                    }
                }
            }

            if (themeTvEnabled) {
                for (ThemeTvGroup themeTvGroup : ThemeTvGroup.values()) {
                    ChannelFilter themeTvGroupFilter = getThemeTvChannelsFilter(mContext, themeTvGroup, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                            tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    if (themeTvGroupFilter.hasChannels()) {
                        filters.add(themeTvGroupFilter);
                    }
                }
            }

            return filters;
        }
    }

    private static class AvailableChannelFiltersFetchTask extends FutureTask<List<ChannelFilter>> {

        private Handler mHandler;

        private AvailableChannelFiltersFetchTask(AvailableChannelFiltersCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    List<ChannelFilter> filters = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_AVAILABLE_FILTERS_FETCH_COMPLETE;
                    message.obj = filters;
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "AvailableChannelFiltersFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class ActiveChannelFilterCallable implements Callable<ChannelFilter> {

        private Context mContext;

        ActiveChannelFilterCallable(Context context) {
            mContext = context;
        }

        @Override
        public ChannelFilter call() throws Exception {
            ChannelFilter filter = null;
            int activeFilterId = DashboardDataManager.getInstance().getActiveChannelFilterId();

            boolean areRFIPChannelsEnabled = DashboardDataManager.getInstance().areRFIPChannelsEnabled();
            boolean tvChannelsEnabled = DashboardDataManager.getInstance().areTvChannelsEnabled();

            boolean radioChannelsDisplayEnabled = DashboardDataManager.getInstance().areRadioChannelsEnabled();
            boolean mediaChannelsEnabled = DashboardDataManager.getInstance().areMediaChannelsEnabled();

            boolean tifChannelsEnabled = DashboardDataManager.getInstance().areTifChannelsEnabled();
            boolean tifChannelsDisplayEnabled = DashboardDataManager.getInstance().areTifChannelsDisplayEnabled();
            boolean tifChannelsInChannelsListEnabled = DashboardDataManager.getInstance().areTifChannelsInChannelsListEnabled();

            boolean themeTvEnabled = DashboardDataManager.getInstance().isThemeTvEnabled();
            boolean myChoiceEnabled = DashboardDataManager.getInstance().isMyChoiceEnabled();
            boolean hdmi1Enabled = DashboardDataManager.getInstance().isHdmi1Enabled();
            boolean hdmi2Enabled = DashboardDataManager.getInstance().isHdmi2Enabled();
            boolean hdmi3Enabled = DashboardDataManager.getInstance().isHdmi3Enabled();
            boolean hdmi4Enabled = DashboardDataManager.getInstance().isHdmi4Enabled();
            boolean vgaEnabled = DashboardDataManager.getInstance().isVgaEnabled();
            DdbLogUtility.logCommon(TAG, "ActiveChannelFilterCallable call() areRFIPChannelsEnabled " + areRFIPChannelsEnabled
                    + " tvChannelsEnabled " + tvChannelsEnabled
                    + " radioChannelsDisplayEnabled " + radioChannelsDisplayEnabled
                    + " mediaChannelsEnabled " + mediaChannelsEnabled
                    + " tifChannelsEnabled " + tifChannelsEnabled
                    + " tifChannelsDisplayEnabled " + tifChannelsDisplayEnabled
                    + " themeTvEnabled " + themeTvEnabled
                    + " myChoiceEnabled " + myChoiceEnabled
                    + " hdmi1Enabled " + hdmi1Enabled
                    + " hdmi2Enabled " + hdmi2Enabled
                    + " hdmi3Enabled " + hdmi3Enabled
                    + " hdmi4Enabled " + hdmi4Enabled
                    + " vgaEnabled " + vgaEnabled);

            switch (activeFilterId) {

                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_ALL:
                    boolean tiffChannels = (tifChannelsEnabled && tifChannelsInChannelsListEnabled);
                    filter = getAllChannelsFilter(mContext, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled, tiffChannels,
                            hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_TV:
                    if (areRFIPChannelsEnabled && tvChannelsEnabled) {
                        filter = getTvChannelsFilter(mContext);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_SOURCE:
                    //TODO: Need to confirm if notification to SourceDataManager is needed or not.
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_RADIO:
                    if (radioChannelsDisplayEnabled) {
                        filter = getRadioChannelsFilter(mContext, tifChannelsEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_TIF:
                    if (tifChannelsDisplayEnabled && tifChannelsEnabled) {
                        String activeTifFilterInputId = DashboardDataManager.getInstance().getActiveTifInputId();
                        if (!TextUtils.isEmpty(activeTifFilterInputId)) {
                            filter = getTifFilter(mContext, activeTifFilterInputId, radioChannelsDisplayEnabled);
                        } else {
                            boolean tiffChannels1 = (tifChannelsEnabled && tifChannelsInChannelsListEnabled);
                            filter = getAllChannelsFilter(mContext, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled, tiffChannels1,
                                    hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                        }
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_MYCHOICE_FREEPKG:
                    if (myChoiceEnabled) {
                        filter = getMyChoiceChannelsFilter(mContext, MyChoicePackage.MY_CHOICE_FREE_PKG, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_MYCHOICE_PAYPKG_1:
                    if (myChoiceEnabled) {
                        filter = getMyChoiceChannelsFilter(mContext, MyChoicePackage.MY_CHOICE_PKG_1, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_MYCHOICE_PAYPKG_2:
                    if (myChoiceEnabled) {
                        filter = getMyChoiceChannelsFilter(mContext, MyChoicePackage.MY_CHOICE_PKG_2, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_1:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_1, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_2:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_2, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_3:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_3, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_4:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_4, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_5:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_5, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_6:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_6, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_7:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_7, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_8:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_8, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_9:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_9, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
                case TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_THEME_10:
                    if (themeTvEnabled) {
                        filter = getThemeTvChannelsFilter(mContext, ThemeTvGroup.THEME_TV_GROUP_10, areRFIPChannelsEnabled, areRFIPChannelsEnabled, mediaChannelsEnabled,
                                tifChannelsEnabled, hdmi1Enabled, hdmi2Enabled, hdmi3Enabled, hdmi4Enabled, vgaEnabled);
                    }
                    break;
            }

            return filter;
        }
    }

    private static class ActiveChannelFilterFetchTask extends FutureTask<ChannelFilter> {

        private Handler mHandler;

        private ActiveChannelFilterFetchTask(ActiveChannelFilterCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    ChannelFilter filter = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_ACTIVE_FILTER_FETCH_COMPLETE;
                    message.obj = filter;
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "ActiveChannelFilterFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class ChannelLogoFetchCallable implements Callable<Bitmap> {

        private Context mContext;
        private Channel mChannel;

        private ChannelLogoFetchCallable(Context context, Channel channel) {
            mContext = context;
            mChannel = channel;
        }

        @Override
        public Bitmap call() throws Exception {
            InputStream inputStream = null;
            try {
                Uri logoUri = null;
                AssetFileDescriptor assetFileDescriptor = null;
                if (Channel.isTifChannel(mChannel)) {
                    logoUri = TvContract.buildChannelLogoUri(mChannel.getMappedId());
                    assetFileDescriptor = mContext.getContentResolver().openAssetFileDescriptor(logoUri, "r");
                } else {
                    logoUri = HtvContract.buildHighResolutionChannelLogoUri(mChannel.getId());
                    try {
                        assetFileDescriptor = mContext.getContentResolver().openAssetFileDescriptor(logoUri, "r");
                    } catch (FileNotFoundException e) {
                        // high resolution logo is not available probably. try fetching normal resolution logo
                        logoUri = TvContract.buildChannelLogoUri(mChannel.getMappedId());
                        assetFileDescriptor = mContext.getContentResolver().openAssetFileDescriptor(logoUri, "r");
                    }
                }
                if (assetFileDescriptor != null) {
                    inputStream = assetFileDescriptor.createInputStream();
                    return BitmapFactory.decodeStream(inputStream);
                }
            } catch (Exception e) {
                Log.d(TAG, "Could not fetch logo for channel id:" + mChannel.getId() + ".Reason:" + e.getMessage());
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return null;
        }

        private int getChannelId() {
            return mChannel.getId();
        }
    }

    private static final class ChannelLogoFetchTask extends FutureTask<Bitmap> {

        private Handler mHandler;
        private int mChannelId;
        private ChannelLogoFetchListener mChannelLogoFetchListener;

        private ChannelLogoFetchTask(ChannelLogoFetchCallable callable, Handler handler, ChannelLogoFetchListener listener) {
            super(callable);
            mHandler = handler;
            mChannelLogoFetchListener = listener;
            mChannelId = callable.getChannelId();
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Bitmap logo = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_CHANNEL_LOGO_FETCH_COMPLETE;
                    message.obj = new ChannelLogoFetchResult(mChannelId, logo, mChannelLogoFetchListener);
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "ChannelLogoFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class CleanUpChannelFiltersCallable implements Callable<Pair<Integer, Integer>> {

        private List<ChannelFilter> mChannelFilters;

        CleanUpChannelFiltersCallable(List<ChannelFilter> channelFilters) {
            mChannelFilters = channelFilters;
        }

        @Override
        public Pair<Integer, Integer> call() throws Exception {
            if (mChannelFilters != null && !mChannelFilters.isEmpty()) {
                int successCount = 0;
                int failureCount = 0;
                for (int i = 0; i < mChannelFilters.size(); i++) {
                    try {
                        ChannelFilter filter = mChannelFilters.get(i);
                        if (filter != null) {
                            filter.cleanUp();
                            successCount++;
                        }
                    } catch (Exception e) {
                        failureCount++;
                       Log.e(TAG,"Exception :" +e.getMessage());
                    }
                }
                return new Pair<Integer, Integer>(successCount, failureCount);
            }

            return null;
        }
    }

    private static class CleanUpChannelFiltersTask extends FutureTask<Pair<Integer, Integer>> {

        private Handler mHandler;

        private CleanUpChannelFiltersTask(CleanUpChannelFiltersCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Pair<Integer, Integer> result = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_CHANNEL_FILTER_CLEANUP_COMPLETE;
                    if (result != null) {
                        message.arg1 = result.first;
                        message.arg2 = result.second;
                    } else {
                        message.arg1 = CHANNEL_FILTER_CLEANUP_COUNT_NA;
                        message.arg2 = CHANNEL_FILTER_CLEANUP_COUNT_NA;
                    }
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "CleanUpChannelFiltersTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class ChannelLogoFetchResult {
        private int mChannelId;
        private Bitmap mLogo;
        private ChannelLogoFetchListener mChannelLogoFetchListener;

        private ChannelLogoFetchResult(int channelId, Bitmap logo, ChannelLogoFetchListener channelLogoFetchListener) {
            mChannelId = channelId;
            mLogo = logo;
            mChannelLogoFetchListener = channelLogoFetchListener;
        }
    }

    private static class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_AVAILABLE_FILTERS_FETCH_COMPLETE = 100;
        private static final int MSG_WHAT_ACTIVE_FILTER_FETCH_COMPLETE = 101;
        private static final int MSG_WHAT_CHANNEL_LOGO_FETCH_COMPLETE = 102;
        private static final int MSG_WHAT_CHANNEL_FILTER_CLEANUP_COMPLETE = 103;
        private static final int MSG_WHAT_ACTIVE_CHANNEL_FILTER_CHANGED = 104;
        private static final int MSG_WHAT_ENABLE_CHANNELS_SETTING_CHANGED = 105;
        private static final int MSG_WHAT_ENABLE_RADIO_CHANNELS_SETTING_CHANGED = 106;
        private static final int MSG_WHAT_ENABLE_MEDIA_CHANNELS_SETTING_CHANGED = 107;
        private static final int MSG_WHAT_ENABLE_TIF_CHANNELS_SETTING_CHANGED = 108;
        private static final int MSG_WHAT_ENABLE_THEME_TV_SETTING_CHANGED = 109;
        private static final int MSG_WHAT_SOURCES_SETTING_CHANGED = 110;
        private static final int MSG_WHAT_EPG_ENABLED_SETTING_CHANGED = 111;
        private static final int MSG_WHAT_EPG_SOURCE_CHANGED = 112;
        private static final int MSG_WHAT_CHANNEL_LOGO_ENABLED_STATE_CHANGED = 113;
        private static final int MSG_WHAT_ENABLE_TV_CHANNELS_SETTING_CHANGED = 114;
        private static final int MSG_WHAT_LAST_SELECTED_CHANNEL_URI_CHANGED = 115;
        private static final int MSG_WHAT_DISPLAY_FILTER_ALL_CHANNELS = 116;
        private static final int MSG_WHAT_DISPLAY_FILTER_OTT_APP = 117;
        private static final int MSG_WHAT_ENABLE_OTT_APP_IN_CHANNELS = 118;

        private WeakReference<ChannelDataManager> mChannelDataManagerRef;

        private UiThreadHandler(WeakReference<ChannelDataManager> ref) {
            super();
            mChannelDataManagerRef = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logTVChannelChapter(TAG, "UiThreadHandler handleMessage msg.what " + msg.what);
            if (what == MSG_WHAT_DISPLAY_FILTER_ALL_CHANNELS) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onDisplayFilterSettingsChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_DISPLAY_FILTER_OTT_APP) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onDisplayFilterSettingsChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_OTT_APP_IN_CHANNELS) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEnableTifChannelsSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_AVAILABLE_FILTERS_FETCH_COMPLETE) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.notifyAvailableChannelFiltersFetchComplete((List<ChannelFilter>) msg.obj);
                }
                return;
            }

            if (what == MSG_WHAT_ACTIVE_FILTER_FETCH_COMPLETE) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.notifyActiveChannelFilterFetchComplete((ChannelFilter) msg.obj);
                }
                return;
            }

            if (what == MSG_WHAT_CHANNEL_LOGO_FETCH_COMPLETE) {
                ChannelLogoFetchResult result = (ChannelLogoFetchResult) msg.obj;
                if (result.mChannelLogoFetchListener != null) {
                    result.mChannelLogoFetchListener.onChannelLogoFetchComplete(result.mChannelId, result.mLogo);
                }
                return;
            }

            if (what == MSG_WHAT_CHANNEL_FILTER_CLEANUP_COMPLETE) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.notifyChannelFiltersCleanUpComplete(msg.arg1, msg.arg2);
                }
                return;
            }

            if (what == MSG_WHAT_ACTIVE_CHANNEL_FILTER_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onActiveChannelFilterChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_CHANNELS_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEnableChannelsSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_RADIO_CHANNELS_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEnableRadioChannelsSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_MEDIA_CHANNELS_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEnableMediaChannelsSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_TIF_CHANNELS_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onDisplayFilterSettingsChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_THEME_TV_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEnableThemeTvSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_SOURCES_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onSourcesSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_EPG_ENABLED_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEpgEnabledSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_EPG_SOURCE_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEpgSourceChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_CHANNEL_LOGO_ENABLED_STATE_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onChannelLogoEnabledStateChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_ENABLE_TV_CHANNELS_SETTING_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onEnableTvChannelsSettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_LAST_SELECTED_CHANNEL_URI_CHANGED) {
                ChannelDataManager channelDataManager = mChannelDataManagerRef.get();
                if (channelDataManager != null) {
                    channelDataManager.onLastSelectedChannelUriChanged();
                }
                return;
            }
        }
    }

    private BroadcastReceiver mChannelListUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_CHANNEL_LIST_UPDATED.equals(action) || ACTION_CHANNEL_LIST_UPDATED_IN_PBS.equals(action)) {
                onChannelListUpdated();
                return;
            }

            if (ACTION_CHANNEL_LIST_CLONE_IN_COMPLETED.equals(action)) {
                onChannelListCloneInCompleted();
                return;
            }

            if (ACTION_THEMETV_DATA_CHANGED.equals(action)) {
                onThemeTvDataChanged();
                return;
            }
        }
    };

    private static class ChannelLogoCache {
        private static final String TAG = "ChannelLogoCache";

        // Reserve 1/32th of the max runtime memory available for this LruCache in Kilo bytes
        private static final int CACHE_SIZE_IN_KBYTES = 16 * 1024; //Value of 16MB in KB

        private final LruCache<Integer, Bitmap> mLogoCache;

        private ChannelLogoCache() {
            mLogoCache = new LruCache<Integer, Bitmap>(CACHE_SIZE_IN_KBYTES) {
                @Override
                protected int sizeOf(Integer key, Bitmap value) {
                    return value.getAllocationByteCount() / 1024;
                }
            };
        }

        private void clear() {
            mLogoCache.evictAll();
            clearLogos();
        }

        private void clearLogos() {
            Set<Integer> logoCacheKeys = mLogoCache.snapshot().keySet();
            Iterator<Integer> iterator = logoCacheKeys.iterator();
            while(iterator.hasNext()){
                Integer key = iterator.next();
                recycleBitmap(key);
                mLogoCache.remove(key);
            }
        }

        private void recycleBitmap(Integer key) {
            Bitmap bitmap = mLogoCache.get(key);
            if(bitmap != null && (!bitmap.isRecycled())) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        private Bitmap getLogo(int channelId) {
            return mLogoCache.get(channelId);
        }

        private void addLogo(int channelId, Bitmap logo) {
            mLogoCache.put(channelId, logo);
        }
    }

    private static final class ThemeTvInfo {
        private String mTitle;
        private Drawable mLogo;
    }
}
