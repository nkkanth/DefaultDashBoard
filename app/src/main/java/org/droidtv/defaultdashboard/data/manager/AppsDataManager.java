package org.droidtv.defaultdashboard.data.manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.content.OperationApplicationException;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AllAppListFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppLogoFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppRecommendationsListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MyChoiceListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.query.AllEnabledHtvAppsQuery;
import org.droidtv.defaultdashboard.data.query.AllHtvAppsQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.htv.htvappsync.IAppSyncControl;
import org.droidtv.htv.provider.HtvContract;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static org.droidtv.defaultdashboard.util.Constants.*;

final class AppsDataManager extends ContextualObject implements RecommendationListener, MyChoiceListener {

    private static final String TAG = "AppsDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private DashboardDataManager mDashboardDataManager;
    private AppLogoCache mAppLogoCache;
    private ArrayList<AppInfo> mEnabledAppsList;
    private ArrayList<AppInfo> mEnabledAppRecommendationsList;
    private ArrayList<AppInfo> mEnabledRecommendedAppsList;
    private Map<String, List<Recommendation>> mAppRecommendationsMap;
    private ArrayList<WeakReference<AppRecommendationsListener>> mAppRecommendationsListenerRefs;
    private boolean mHasRegisteredForAppRecommendationChanges;
    private boolean mRecommendedAppsAvailable;
    private ArrayList<WeakReference<AppDataListener>> mAppDataListenerRefs;
    private boolean mAppSyncInProgress = false;
    private static int mAppRecommendationEnabledCount;
    private static int mRecommendedAppsCountFromAppDb = 0;
    private static final long APP_LIST_FETCH_DELAY_MS = 1000;
    private final PackageReceiver mPackageReceiver = new PackageReceiver();
    private final AppSyncDBReceiver mAppSyncDBReceiver = new AppSyncDBReceiver();
    private static final String NETFLIX = "com.netflix.ninja";

    AppsDataManager(Context context) {
        super(context);
        mDashboardDataManager = DashboardDataManager.getInstance();
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.OTHER_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler(this);
        mAppLogoCache = new AppLogoCache();
        mAppRecommendationsListenerRefs = new ArrayList<>();
        mAppRecommendationsMap = new LinkedHashMap<>();
        mEnabledAppsList = new ArrayList<>();
        mEnabledAppRecommendationsList = new ArrayList<>();
        mEnabledRecommendedAppsList = new ArrayList<>();
        mHasRegisteredForAppRecommendationChanges = false;
        mRecommendedAppsAvailable = false;
        mAppRecommendationEnabledCount = 0;
        mAppDataListenerRefs = new ArrayList<>();
        registerHtvAppListDbChanges();
        registerMyChoiceListener();
        registerIntentReceivers();
        registerAppSyncDBListUpdateReceiver();
    }

    private void registerForAppRecommendationChanges() {
        mDashboardDataManager.addRecommendationListener(this);
    }

    private void registerMyChoiceListener() {
        mDashboardDataManager.addMyChoiceListener(this);
    }

    private void registerHtvAppListDbChanges() {
        /*Android-P: Uncommnet below line once dimen is available, Currenlty its commented for DDB bringup*/
        getContext().getContentResolver().registerContentObserver(HtvContract.HtvAppList.CONTENT_URI, true, mHtvAppListDbObserver);
    }

    void fetchEnabledAppList() {
        if(DashboardDataManager.getInstance().isSmartTvModeOff() || (!DashboardDataManager.getInstance().isAppSyncProfileValid())){
            Log.d(TAG, "fetchEnabledAppList: isSmartTvModeOff true");
            mEnabledAppsList = null;
            notifyOnAppListFetched();
            return;
        }
        EnabledAppListFetchCallable callable = new EnabledAppListFetchCallable(getContext() ,new WeakReference<AppsDataManager>(this));
        EnabledAppListFetchTask task = new EnabledAppListFetchTask(callable, mUiThreadHandler, mEnabledAppListFetchListener);
        mThreadPoolExecutor.execute(task);
    }

    void notifyOnAppListFetched(){
        Message message = Message.obtain();
        message.obj = new EnabledAppListFetchResult(null, mEnabledAppListFetchListener);
        message.what = UiThreadHandler.MSG_WHAT_ENABLED_APPS_FETCH_COMPLETE;
        mUiThreadHandler.sendMessage(message);
    }
    void buildRecommendedAppsList() {
        if(DashboardDataManager.getInstance().isSmartTvModeOff()){
            return;
        }
        RecommendedAppsListFetchCallable callable = new RecommendedAppsListFetchCallable(getContext());
        RecommendedAppsListFetchTask task = new RecommendedAppsListFetchTask(callable, mUiThreadHandler, mRecommendedAppsListFetchListener);
        mThreadPoolExecutor.execute(task);
    }

    public void refreshEnabledAppList() {
        fetchEnabledAppList();
    }

    private ContentObserver mHtvAppListDbObserver = new ContentObserver(null) {
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if(isHitCountUpdate(uri)){
                Log.d(TAG, "onChange: Ignored UI updated");
                return;
            }
            DdbLogUtility.logAppsChapter(TAG,"onchange "+uri);
            if (!mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST) && !isAppSyncStarted()) {
                DdbLogUtility.logAppsChapter(TAG,"inside onchange "+uri);
                // App list table has been updated; refresh the app list. But delay the refresh, so that
                // a large number of tasks will not be queued up.
                mUiThreadHandler.sendEmptyMessageDelayed(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST, APP_LIST_FETCH_DELAY_MS);
            }
        }
    };

    private boolean isHitCountUpdate(Uri uri) {
        boolean hitcountUpdate = false;
        if(uri == null) return hitcountUpdate;

        String columnName = uri.getLastPathSegment();
        if(!TextUtils.isEmpty(columnName) && columnName.equals(Constants.COLUMN_NAME_HTVAPPLIST_HITCOUNT)){
            hitcountUpdate = true;
        }
        return hitcountUpdate;
    }

    private UpdateRecommendedAppEnabledStateListener mUpdateRecommendedAppEnabledStateListener = new UpdateRecommendedAppEnabledStateListener() {
        @Override
        public void onRecommendedAppEnabledStateUpdated() {
            Log.d(TAG, "#### recommended app state updated");
            if (!mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST)) {
                // htvapplist table's "RecommendedApp" columns have been updated;refresh the app list. But delay the refresh, so that
                // a large number of tasks will not be queued up.
                mUiThreadHandler.sendEmptyMessageDelayed(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST, APP_LIST_FETCH_DELAY_MS);
            }
        }
    };

    private UpdateAppRecommendationEnabledStateListener mUpdateAppRecommendationEnabledStateListener = new UpdateAppRecommendationEnabledStateListener() {
        @Override
        public void onAppRecommendationEnabledStateUpdated() {
            Log.d(TAG, "#### app recommendation state updated");
            if (!mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST)) {
                // // htvapplist table's "AppRecommendation" columns have been updated;refresh the app list. But delay the refresh, so that
                // a large number of tasks will not be queued up.
                mUiThreadHandler.sendEmptyMessageDelayed(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST, APP_LIST_FETCH_DELAY_MS);
            }
        }
    };

    private EnabledAppListFetchListener mEnabledAppListFetchListener = new EnabledAppListFetchListener() {
        @Override
        public void onEnabledAppListFetched(List<AppInfo> enabledAppList) {
            buildEnabledAppList(enabledAppList);

            buildAppRecommendationsMap();

            // Register for listening to app recommendation changes only after the list of enabled apps is built.
            // Otherwise, we may miss recommendation changes; the package names will not match with anything in the enabled app list
            // as it will still be empty.
            // Check if we have already registered for recommendation changes as a result of an earlier fetching of enabled apps
            if (!mHasRegisteredForAppRecommendationChanges) {
                registerForAppRecommendationChanges();
                mHasRegisteredForAppRecommendationChanges = true;
            }

            notifyAppListFetched();
        }
    };

    private RecommendedAppsListFetchListener mRecommendedAppsListFetchListener = new RecommendedAppsListFetchListener() {
        @Override
        public void onRecommendedAppsListFetched(List<AppInfo> recommendedAppList) {
            if (recommendedAppList != null && recommendedAppList.size() > 0) {
                mRecommendedAppsAvailable = true;
            }
        }
    };

    private void buildAppRecommendationsMap() {
        mAppRecommendationsMap = new LinkedHashMap<>();
        List<Recommendation> appRecommendations = mDashboardDataManager.getRecommendations(RecommendationHelper.Category.APPS);
        for (int i = 0; appRecommendations != null && i < appRecommendations.size(); i++) {
            Recommendation recommendation = appRecommendations.get(i);

            String appPackageName = recommendation.getPendingIntent().getCreatorPackage();
            if (!isAppEnabledForRecommendation(appPackageName)) {
                continue;
            }
            //TODO: Remove this check after app list query from htvapplist db is functional
            if ("org.droidtv.nettvadvert".equals(appPackageName)) {
                continue;
            }

            List<Recommendation> recommendations = mAppRecommendationsMap.get(appPackageName);
            if (recommendations == null) {
                recommendations = new ArrayList<>();
                mAppRecommendationsMap.put(appPackageName, recommendations);
            }
            recommendations.add(recommendation);
        }
    }

    private boolean isAppRecommendation(Recommendation recommendation) {
        if (recommendation == null) {
            return false;
        }

        String[] contentTypes = recommendation.getContentType();
        if (contentTypes == null) {
            return true;
        }

        for (String contentType : contentTypes) {
            if (contentType.equals(Constants.CONTENT_TYPE_VOD_RECOMMENDATION) ||
                    contentType.equals(Constants.CONTENT_TYPE_SMART_INFO_RECOMMENDATION) ||
                    contentType.equals(Constants.CONTENT_TYPE_GAMING_RECOMMENDATION)) {
                return false;
            }
        }
        return true;
    }

    private void updateAppSyncStartedStatus(boolean value) {
        mAppSyncInProgress = value;
    }

    private boolean isAppSyncStarted(){
        return mAppSyncInProgress;
    }

    private void notifyAppRecommendationsAvailable() {
        for (int i = 0; mAppRecommendationsListenerRefs != null && i < mAppRecommendationsListenerRefs.size(); i++) {
            WeakReference<AppRecommendationsListener> listenerRef = mAppRecommendationsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            AppRecommendationsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onAppRecommendationsAvailable();
            }
        }
    }

    private void notifyAppRecommendationsUnavailable() {
        for (int i = 0; mAppRecommendationsListenerRefs != null && i < mAppRecommendationsListenerRefs.size(); i++) {
            WeakReference<AppRecommendationsListener> listenerRef = mAppRecommendationsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            AppRecommendationsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onAppRecommendationsUnavailable();
            }
        }
    }

    private void buildEnabledAppList(List<AppInfo> appInfos) {
        mEnabledAppsList = new ArrayList<>();
        mEnabledAppRecommendationsList = new ArrayList<>();
        mEnabledRecommendedAppsList = new ArrayList<>();
        mAppRecommendationEnabledCount = 0;
        if (appInfos == null) {
            DdbLogUtility.logRecommendationChapter(TAG, "buildEnabledAppList appInfos null");
            return;
        }
        mEnabledAppsList.addAll(appInfos);

        for (AppInfo appInfo : mEnabledAppsList) {
            if (appInfo.isAppRecommendationEnabled()) {
                if (appInfo.getIsEditModeEnabled()) {
                    mEnabledAppRecommendationsList.add(0, appInfo);
                } else {
                    mEnabledAppRecommendationsList.add(appInfo);
                }
            }
            if (appInfo.isRecommendedAppEnabled()) {
                if (appInfo.getIsEditModeEnabled()) {
                    mEnabledRecommendedAppsList.add(0, appInfo);
                } else {
                    mEnabledRecommendedAppsList.add(appInfo);
                }
                mAppRecommendationEnabledCount = mEnabledRecommendedAppsList.size();
            }
        }
        DdbLogUtility.logRecommendationChapter(TAG, "buildEnabledAppList mAppRecommendationEnabledCount " + mAppRecommendationEnabledCount);
    }

    List<AppInfo> getEnabledAppList() {
        return mEnabledAppsList;
    }

    private void addLogoToCache(String packageName, Drawable logo) {
        mAppLogoCache.addLogo(packageName, logo);
    }

    private Drawable getLogoFromCache(String packageName) {
        return mAppLogoCache.getLogo(packageName);
    }

    private static Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().
                query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    private void notifyAppListFetched() {
        for (int i = 0; mAppDataListenerRefs != null && i < mAppDataListenerRefs.size(); i++) {
            WeakReference<AppDataListener> ref = mAppDataListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppDataListener listener = ref.get();
            if (listener != null) {
                listener.onAppListFetched();
            }
        }
    }

    boolean addAppDataListener(AppDataListener appDataListener) {
        if (appDataListener == null) {
            return false;
        }
        return mAppDataListenerRefs.add(new WeakReference<AppDataListener>(appDataListener));
    }

    boolean removeAppDataListener(AppDataListener appDataListener) {
        if (mAppDataListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mAppDataListenerRefs.size(); i++) {
            WeakReference<AppDataListener> ref = mAppDataListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppDataListener listener = ref.get();
            if (listener != null && listener.equals(appDataListener)) {
                return mAppDataListenerRefs.remove(ref);
            }
        }
        return false;
    }

    boolean areAppRecommendationsAvailable() {
        return mAppRecommendationsMap != null && !mAppRecommendationsMap.isEmpty() && isAnyAppEnabledForRecommendation();
    }

    boolean areRecommendedAppsAvailable() {
        Log.w(TAG, "areRecommendedAppsAvailable mAppRecommendationEnabledCount: " + mAppRecommendationEnabledCount + " mEnabledRecommendedAppsList:" + mEnabledRecommendedAppsList.size() + "mRecommendedAppsCountFromAppDb :" +mRecommendedAppsCountFromAppDb);
        return  mAppRecommendationEnabledCount > 0 || mRecommendedAppsCountFromAppDb >0;
    }

    boolean isAppRecommendationEnabled(String packageName) {
        return mEnabledAppRecommendationsList != null && isAppEnabledForRecommendation(packageName);
    }

    boolean addAppRecommendationsListener(AppRecommendationsListener appRecommendationsListener) {
        if (appRecommendationsListener == null) {
            return false;
        }
        return mAppRecommendationsListenerRefs.add(new WeakReference<AppRecommendationsListener>(appRecommendationsListener));
    }

    boolean removeAppRecommendationsListener(AppRecommendationsListener appRecommendationsListener) {
        if (mAppRecommendationsListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mAppRecommendationsListenerRefs.size(); i++) {
            WeakReference<AppRecommendationsListener> ref = mAppRecommendationsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppRecommendationsListener listener = ref.get();
            if (listener != null && listener.equals(appRecommendationsListener)) {
                return mAppRecommendationsListenerRefs.remove(ref);
            }
        }
        return false;
    }

    void fetchAllApps(AllAppListFetchListener listener) {
        AllAppListFetchCallable callable = new AllAppListFetchCallable(getContext() ,new WeakReference<AppsDataManager>(this));
        AllAppListFetchTask task = new AllAppListFetchTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    Map<String, List<Recommendation>> getAppRecommendations() {
        return mAppRecommendationsMap;
    }

    List<AppInfo> getRecommendedApps() {
        return mEnabledRecommendedAppsList;
    }

    List<AppInfo> getAppsEnabledForRecommendations() {
        return mEnabledAppRecommendationsList;
    }

    int getAppPositionForPackage(String pkgName){
        for (int i = 0; mEnabledRecommendedAppsList != null && i < mEnabledRecommendedAppsList.size(); i++) {
            String appPackage = mEnabledRecommendedAppsList.get(i).getPackageName();
            if (appPackage.equalsIgnoreCase(pkgName)) {
                if(mEnabledRecommendedAppsList.get(i).getAppPosition() < 0){
                    return -(mEnabledRecommendedAppsList.get(i).getAppPosition());
                }else{
                    return mEnabledRecommendedAppsList.get(i).getAppPosition();
                }

            }
        }
        return 0;
    }

    String getCountryCode(int countryConstant) {
        String countryCode = null;
        switch (countryConstant) {
            case TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS:
                countryCode = APPS_COUNTRY_CODE_NETHERLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BELGIUM:
                countryCode = APPS_COUNTRY_CODE_BELGIUM;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG:
                countryCode = APPS_COUNTRY_CODE_LUXEMBERG;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.FRANCE:
                countryCode = APPS_COUNTRY_CODE_FRANCE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.GERMANY:
                countryCode = APPS_COUNTRY_CODE_GERMANY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND:
                countryCode = APPS_COUNTRY_CODE_SWITZERLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA:
                countryCode = APPS_COUNTRY_CODE_AUSTRIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.UK:
                countryCode = APPS_COUNTRY_CODE_UNITED_KINGDOM;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.IRELAND:
                countryCode = APPS_COUNTRY_CODE_IRELAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SPAIN:
                countryCode = APPS_COUNTRY_CODE_SPAIN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL:
                countryCode = APPS_COUNTRY_CODE_PORTUGAL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ITALY:
                countryCode = APPS_COUNTRY_CODE_ITALY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.NORWAY:
                countryCode = APPS_COUNTRY_CODE_NORWAY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SWEDEN:
                countryCode = APPS_COUNTRY_CODE_SWEDEN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.DENMARK:
                countryCode = APPS_COUNTRY_CODE_DENMARK;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.FINLAND:
                countryCode = APPS_COUNTRY_CODE_FINLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.GREECE:
                countryCode = APPS_COUNTRY_CODE_GREECE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.TURKEY:
                countryCode = APPS_COUNTRY_CODE_TURKEY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.RUSSIA:
                countryCode = APPS_COUNTRY_CODE_RUSSIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.UKRAINE:
                countryCode = APPS_COUNTRY_CODE_UKRAINE;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN:
                countryCode = APPS_COUNTRY_CODE_KAZAKISTHAN;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.POLAND:
                countryCode = APPS_COUNTRY_CODE_POLAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CZECHREP:
                countryCode = APPS_COUNTRY_CODE_CZECH_REPUBLIC;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA:
                countryCode = APPS_COUNTRY_CODE_SLOVAKIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.HUNGARY:
                countryCode = APPS_COUNTRY_CODE_HUNGARY;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BULGARIA:
                countryCode = APPS_COUNTRY_CODE_BULGARIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ROMANIA:
                countryCode = APPS_COUNTRY_CODE_ROMANIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LATVIA:
                countryCode = APPS_COUNTRY_CODE_LATVIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ESTONIA:
                countryCode = APPS_COUNTRY_CODE_ESTONIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA:
                countryCode = APPS_COUNTRY_CODE_LITHUANIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA:
                countryCode = APPS_COUNTRY_CODE_SLOVENIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.SERBIA:
                countryCode = APPS_COUNTRY_CODE_SERBIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.CROATIA:
                countryCode = APPS_COUNTRY_CODE_CROATIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA:
                countryCode = APPS_COUNTRY_CODE_ARGENTINA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.BRAZIL:
                countryCode = APPS_COUNTRY_CODE_BRAZIL;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA:
                countryCode = APPS_COUNTRY_CODE_AUSTRALIA;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND:
                countryCode = APPS_COUNTRY_CODE_NEW_ZEALAND;
                break;
            case TvSettingsDefinitions.InstallationCountryConstants.USA:
                countryCode = APPS_COUNTRY_CODE_USA;
                break;
            default:
                countryCode = APPS_COUNTRY_CODE_INTERNATIONAL;
                break;
        }
        DdbLogUtility.logRecommendationChapter(TAG, "countryConstant " + countryConstant + " countryCode " + countryCode);
        return countryCode;
    }

    List<AppInfo> getAppsByCategory(String category) {
        List<AppInfo> gameApps = new ArrayList<>();
        for (int i = 0; mEnabledAppsList != null && i < mEnabledAppsList.size(); i++) {
            AppInfo appInfo = mEnabledAppsList.get(i);
            String categories = appInfo.getCategories();
            if (categories != null && categories.contains(category)) {
                gameApps.add(appInfo);
            }
        }
        return gameApps;
    }

    List<AppInfo> getAppsByCountry(String countryCode) {
        List<AppInfo> appsByCountry = new ArrayList<>();
        for (int i = 0; mEnabledAppsList != null && i < mEnabledAppsList.size(); i++) {
            AppInfo appInfo = mEnabledAppsList.get(i);
            String countries = appInfo.getCountries();
            if (countries != null && countryCode != null && countries.contains(countryCode)) {
                appsByCountry.add(appInfo);
            }
        }
        return appsByCountry;
    }

    List<AppInfo> getAppsByCountryAndAllCategory(String countryCode) {
        List<AppInfo> appsByCountry = new ArrayList<>();
        for (int i = 0; mEnabledAppsList != null && i < mEnabledAppsList.size(); i++) {
            AppInfo appInfo = mEnabledAppsList.get(i);
            String countries = appInfo.getCountries();
            if (countries != null && countryCode != null && (countries.contains(countryCode) || countries.contains(Constants.APPS_COUNTRY_CODE_ALL))) {
                appsByCountry.add(appInfo);
            }
        }
        return appsByCountry;
    }

    void fetchAppLogo(String packageName, AppLogoFetchListener listener) {
        Drawable logo = getLogoFromCache(packageName);
        if (logo != null) {
            if (listener != null) {
                listener.onAppLogoFetchComplete(packageName, logo);
            }
        } else {
            fetchAppLogoInternal(packageName, listener);
        }
    }

    void updateRecommendedAppEnabledState(Map<String, Boolean> recommendedAppEnabledStateMap) {
        UpdateRecommendedAppEnabledStateCallable callable = new UpdateRecommendedAppEnabledStateCallable(getContext(), recommendedAppEnabledStateMap);
        UpdateRecommendedAppEnabledStateTask task = new UpdateRecommendedAppEnabledStateTask(callable, mUiThreadHandler, mUpdateRecommendedAppEnabledStateListener);
        mThreadPoolExecutor.execute(task);
    }

    void updateAppRecommendationEnabledState(Map<String, Boolean> appRecommendationEnabledStateMap) {
        UpdateAppRecommendationEnabledStateCallable callable = new UpdateAppRecommendationEnabledStateCallable(getContext(), appRecommendationEnabledStateMap);
        UpdateAppRecommendationEnabledStateTask task = new UpdateAppRecommendationEnabledStateTask(callable, mUiThreadHandler, mUpdateAppRecommendationEnabledStateListener);
        mThreadPoolExecutor.execute(task);
    }

    void clearAppLogoCache() {
        mAppLogoCache.clear();
    }

    private void fetchAppLogoInternal(String packageName, final AppLogoFetchListener listener) {
        AppLogoFetchCallable callable = new AppLogoFetchCallable(getContext(), packageName);
        AppLogoFetchTask task = new AppLogoFetchTask(callable, mUiThreadHandler, new AppLogoFetchListener() {
            @Override
            public void onAppLogoFetchComplete(String packageName, Drawable logo) {
                if (listener != null) {
                    listener.onAppLogoFetchComplete(packageName, logo);
                }
                if (logo != null) {
                    addLogoToCache(packageName, logo);
                }
            }
        });
        mThreadPoolExecutor.execute(task);
    }

    private boolean isAnyAppEnabledForRecommendation() {
        for (String packageName : mAppRecommendationsMap.keySet()) {
            if (isAppEnabledForRecommendation(packageName)) {
                DdbLogUtility.logRecommendationChapter(TAG, "isAnyAppEnabledForRecommendation returns true packageName " + packageName);
                return true;
            }
        }
        return false;
    }

    private boolean isAppEnabledForRecommendation(String packageName) {
        for (AppInfo appInfo : mEnabledAppRecommendationsList) {
            if (appInfo.getPackageName().equals(packageName)) {
                DdbLogUtility.logRecommendationChapter(TAG, "isAppEnabledForRecommendation returns true packageName " + packageName);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        DdbLogUtility.logRecommendationChapter(TAG, "" + ((recommendation != null) ?  recommendation.toString() : null));
        if (!isAppRecommendation(recommendation)) {
            return;
        }

        if(mDashboardDataManager.isSmartTvModeOff() ){
            return;
        }

	List<Recommendation> recommendationList;

	if(null == recommendation){
	    return;
	}

	if (null == recommendation.getPendingIntent()) {
	    return;
	}

        String packageName = recommendation.getPendingIntent().getCreatorPackage();

//TODO: Remove this check after app list query from htvapplist db is functional
        if ("org.droidtv.nettvadvert".equals(packageName)) {
            return;
        }

// If this is a new recommendation, simply add it to the list to the respective package name in the map
        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            if (mAppRecommendationsMap.containsKey(packageName)) {
                recommendationList = mAppRecommendationsMap.get(packageName);
                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        recommendationList.add(i, recommendation);
                        return;
                    }
                }
                recommendationList.add(recommendation);
            } else {
                recommendationList = new ArrayList<>();
                recommendationList.add(recommendation);
                mAppRecommendationsMap.put(packageName, recommendationList);
            }
            if (mAppRecommendationsMap.size() == 1 && recommendationList.size() == 1 && isAppEnabledForRecommendation(packageName)) {
                // A single new recommendation has been added. Notify listeners that atleast one recommendation is available
                // But notify only if this package name is in the recommendation-enabled list of apps
                notifyAppRecommendationsAvailable();
            }
            return;
        }

// For recommendations to be cancelled, simply remove the recommendation from the list
        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            if (mAppRecommendationsMap.containsKey(packageName)) {
                recommendationList = mAppRecommendationsMap.get(packageName);

                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        break;
                    }
                }
                if (recommendationList.isEmpty()) {
                    mAppRecommendationsMap.remove(packageName);

                    if (!areAppRecommendationsAvailable()) {
                        // No recommendations are there anymore. Notify listeners that no recommendations are available
                        notifyAppRecommendationsUnavailable();
                    }
                }
                return;
            }
        }

// Update if there is an existing recommendation with the same id. Otherwise, add this recommendation to the list
        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            if (mAppRecommendationsMap.containsKey(packageName)) {
                recommendationList = mAppRecommendationsMap.get(packageName);
                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        recommendationList.add(i, recommendation);
                        return;
                    }
                }
            }

            if (mAppRecommendationsMap.containsKey(packageName)) {
                recommendationList = mAppRecommendationsMap.get(packageName);
                recommendationList.add(recommendation);
            } else {
                recommendationList = new ArrayList<>();
                recommendationList.add(recommendation);
                mAppRecommendationsMap.put(packageName, recommendationList);
            }
            if (mAppRecommendationsMap.size() == 1 && recommendationList.size() == 1 && isAppEnabledForRecommendation(packageName)) {
                // A single new recommendation has been added. Notify listeners that atleast one recommendation is available
                // But notify only if this package name is in the recommendation-enabled list of apps
                notifyAppRecommendationsAvailable();
            }
            return;
        }
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        if (recommendationCategory == RecommendationHelper.Category.APPS) {
            buildAppRecommendationsMap();
            if (areAppRecommendationsAvailable()) {
                notifyAppRecommendationsAvailable();
            }
        }
    }

    @Override
    public void onMyChoiceDataChanged() {
        refreshEnabledAppList();
    }

    @Override
    public void onMyChoiceEnabledStateChanged() {
        refreshEnabledAppList();
    }

    @Override
    public void onMyChoiceEnabledStateChanged(int value) {
        refreshEnabledAppList();
    }

    @Override
    public void onMyChoiceLockStatusChanged() {
        refreshEnabledAppList();
    }

    private static class RecommendedAppsListFetchCallable extends RecommendedChapterVisibilityCallable {

        private RecommendedAppsListFetchCallable(Context context) {
            super(context);
        }

        @Override
        protected Query getAppListQuery() {
            return new AllEnabledHtvAppsQuery();
        }
    }


    private static class RecommendedAppsListFetchTask extends RecommendedChapterVisibilityTask {

        private RecommendedAppsListFetchListener mRecommendedAppsListFetchListener;

        private RecommendedAppsListFetchTask(RecommendedAppsListFetchCallable callable, Handler handler, RecommendedAppsListFetchListener enabledAppListFetchListener) {
            super(callable, handler);
            mRecommendedAppsListFetchListener = enabledAppListFetchListener;
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_APP_RECOMMENDATION_AVAILABLE;
        }

        @Override
        AppListFetchResult buildAppListFetchResult(List<AppInfo> appInfos) {
            return new RecommendedAppsListFetchResult(appInfos, mRecommendedAppsListFetchListener);
        }

        @Override
        protected String getTaskName() {
            return "RecommendedAppsListFetchTask";
        }
    }

    private static class EnabledAppListFetchCallable extends AppListFetchCallable {

        private EnabledAppListFetchCallable(Context context ,WeakReference<AppsDataManager> appsDataManagerWeakReference) {
            super(context ,appsDataManagerWeakReference);
        }

        @Override
        protected Query getAppListQuery() {
            return new AllEnabledHtvAppsQuery();
        }
    }

    private static class EnabledAppListFetchTask extends AppListFetchTask {

        private EnabledAppListFetchListener mEnabledAppListFetchListener;

        private EnabledAppListFetchTask(EnabledAppListFetchCallable callable, Handler handler, EnabledAppListFetchListener enabledAppListFetchListener) {
            super(callable, handler);
            mEnabledAppListFetchListener = enabledAppListFetchListener;
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_ENABLED_APPS_FETCH_COMPLETE;
        }

        @Override
        AppListFetchResult buildAppListFetchResult(List<AppInfo> appInfos) {
            return new EnabledAppListFetchResult(appInfos, mEnabledAppListFetchListener);
        }

        @Override
        protected String getTaskName() {
            return "EnabledAppListFetchTask";
        }
    }

    private static final class AllAppListFetchCallable extends AppListFetchCallable {

        private AllAppListFetchCallable(Context context ,WeakReference<AppsDataManager> appsDataManager) {
            super(context ,appsDataManager);
        }

        @Override
        protected Query getAppListQuery() {
            return new AllHtvAppsQuery();
        }
    }

    private static final class AllAppListFetchTask extends AppListFetchTask {

        private AllAppListFetchListener mAllAppListFetchListener;

        private AllAppListFetchTask(AllAppListFetchCallable callable, Handler handler, AllAppListFetchListener allAppListFetchListener) {
            super(callable, handler);
            mAllAppListFetchListener = allAppListFetchListener;
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_ALL_APPS_FETCH_COMPLETE;
        }

        @Override
        AppListFetchResult buildAppListFetchResult(List<AppInfo> appInfos) {
            return new AllAppListFetchResult(appInfos, mAllAppListFetchListener);
        }

        @Override
        protected String getTaskName() {
            return "AllAppListFetchTask";
        }
    }

    abstract private static class AppListFetchCallable implements Callable<List<AppInfo>> {

        private Context mContext;
        private WeakReference<AppsDataManager> mAppsDataManagerRef;
        private AppListFetchCallable(Context context ,WeakReference<AppsDataManager> appsDataManagerRef) {
            mContext = context;
            mAppsDataManagerRef = appsDataManagerRef;
        }

        @Override
        public List<AppInfo> call() throws Exception {
            PackageManager packageManager = mContext.getPackageManager();
            List<AppInfo> appInfos = new ArrayList<>();
            AppsDataManager appsDataManager = mAppsDataManagerRef.get();
            Cursor cursor = null;
            try {
                cursor = executeQuery(mContext, getAppListQuery());
                while (cursor != null && cursor.moveToNext()) {
                    String appPackageName = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_NAME));
                    String label = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_LABEL_NAME));
                    boolean isRecommendedAppEnabled = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP)) == 1;
                    int position = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_POSITION));
                    if (isRecommendedAppEnabled) {
                        mAppRecommendationEnabledCount++;
                        mRecommendedAppsCountFromAppDb++;
                    }
                    // We are interested only in apps that can be launched
                    // Other apps are probably smart info or custom dashboard apps that we need to filter out
                    if (hasLaunchIntent(packageManager, appPackageName)) {
                        int cdbAppID = appsDataManager.mDashboardDataManager.isDashboardModeCustom()?0: -1;
                        int auth_needed = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_AUTH_NEEDED));
                        int auth_status = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_AUTH_STATUS));
                        int auth_dev = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_AUTH_DEVELOPER));
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appPackageName, 0);
                        String description = "";
                        CharSequence descr = applicationInfo.loadDescription(packageManager);
                        if (!TextUtils.isEmpty(descr)) {
                            description = descr.toString();
                        }
                        String categories = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_CATEGORY));
                        String countries = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_COUNTRY));
                        boolean isAppRecommendationEnabled = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION)) == 1;
                        boolean isEditModeEnabled = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_EDIT_MODE)) == 0;
                        if (appPackageName.equalsIgnoreCase(NETFLIX)) {
                            //If netlix is not needed move to next cursor and add in AppInfo
                            if (!appsDataManager.isNetflixNeeded(auth_needed, auth_status, auth_dev, cdbAppID)) {
                                continue;
                            }
                        }
                        AppInfo.Builder builder = new AppInfo.Builder();
                        builder.setPackageName(appPackageName).
                                setLabel(label).
                                setDescription(description).
                                setCategories(categories).
                                setCountries(countries).
                                setAppRecommendationEnabled(isAppRecommendationEnabled).
                                setAppPosition(position).
                                setEditModeEnabled(isEditModeEnabled).
                                SetAppType(cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_TYPE))).
                                setRecommendedAppEnabled(isRecommendedAppEnabled);
                        appInfos.add(builder.build());
                    }else{
                        Log.d(TAG, "Package name : "+appPackageName +" doesn't have valid launch intent ");
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            DdbLogUtility.logRecommendationChapter(TAG, "");
            Log.d(TAG, "AppListFetchCallable call() mAppRecommendationEnabledCount " + mAppRecommendationEnabledCount);
            return appInfos;
        }

        private boolean hasLaunchIntent(PackageManager packageManager, String packageName) {
            Intent intent = packageManager.getLeanbackLaunchIntentForPackage(packageName);
            if (intent == null) {
                intent = packageManager.getLaunchIntentForPackage(packageName);
            }
            return intent != null;
        }

        abstract protected Query getAppListQuery();
    }

    abstract private static class RecommendedChapterVisibilityCallable implements Callable<List<AppInfo>> {

        private Context mContext;

        private RecommendedChapterVisibilityCallable(Context context) {
            mContext = context;
        }

        @Override
        public List<AppInfo> call() throws Exception {
            PackageManager packageManager = mContext.getPackageManager();
            List<AppInfo> appInfos = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = executeQuery(mContext, getAppListQuery());
                while (cursor != null && cursor.moveToNext()) {
                    String appPackageName = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_NAME));
                    String label = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_LABEL_NAME));
                    // We are interested only in apps that can be launched
                    // Other apps are probably smart info or custom dashboard apps that we need to filter out
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appPackageName, 0);
                    String description = "";
                    CharSequence descr = applicationInfo.loadDescription(packageManager);
                    if (!TextUtils.isEmpty(descr)) {
                        description = descr.toString();
                    }
                    String categories = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_CATEGORY));
                    String countries = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_COUNTRY));
                    boolean isAppRecommendationEnabled = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION)) == 1;
                    boolean isRecommendedAppEnabled = cursor.getInt(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP)) == 1;
                    AppInfo.Builder builder = new AppInfo.Builder();
                    builder.setPackageName(appPackageName).
                            setLabel(label).
                            setDescription(description).
                            setCategories(categories).
                            setCountries(countries).
                            setAppRecommendationEnabled(isAppRecommendationEnabled).
                            setRecommendedAppEnabled(isRecommendedAppEnabled);
                    appInfos.add(builder.build());
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            DdbLogUtility.logRecommendationChapter(TAG, "RecommendedChapterVisibilityCallable appInfos.size() " + appInfos.size());
            return appInfos;
        }

        abstract protected Query getAppListQuery();
    }

    public List<AppInfo> getAllApps(){
        AllHtvAppsQuery allAppQuery = new AllHtvAppsQuery();
        Cursor allApps =  executeQuery(getContext(), allAppQuery);
        return buildAllAppList(allApps);
    }

    private List<AppInfo> buildAllAppList(Cursor allAppsCursor){
        List<AppInfo> allAppList = new ArrayList<>();
        while (allAppsCursor.moveToNext()){
            allAppList.add(buildAppInfo(allAppsCursor));
        }
        if(allAppsCursor != null && !allAppsCursor.isClosed()) allAppsCursor.close();
        return allAppList;
    }

    private AppInfo buildAppInfo(Cursor allAppsCursor){
        AppInfo.Builder builder = new AppInfo.Builder();

        return  builder.setPackageName(allAppsCursor.getString(allAppsCursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_NAME)))
                .setAppRecommendationEnabled(convertIntToBool(allAppsCursor.getInt(allAppsCursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION))))
                .setRecommendedAppEnabled(convertIntToBool(allAppsCursor.getInt(allAppsCursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP))))
                .build();
    }

    private boolean convertIntToBool(int value){ return value == 0 ? false : true; }

    List<AppInfo> getAllUserInstalledApps() {
        List<AppInfo> userApps = new ArrayList<>();
        for (int i = 0; i <= mEnabledRecommendedAppsList.size() - 1; i++) {
            if (mEnabledRecommendedAppsList.get(i).getIsEditModeEnabled()) {
                userApps.add(mEnabledRecommendedAppsList.get(i));
            }
        }
        return userApps;
    }


    abstract private static class RecommendedChapterVisibilityTask extends FutureTask<List<AppInfo>> {

        private Handler mHandler;

        private RecommendedChapterVisibilityTask(RecommendedAppsListFetchCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    List<AppInfo> appInfos = get();
                    AppListFetchResult result = buildAppListFetchResult(appInfos);
                    Message message = Message.obtain(mHandler, getMessageType(), result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, getTaskName() + " failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
        }

        abstract protected int getMessageType();

        abstract protected String getTaskName();

        abstract AppListFetchResult buildAppListFetchResult(List<AppInfo> appInfos);
    }


    abstract private static class AppListFetchTask extends FutureTask<List<AppInfo>> {

        private Handler mHandler;

        private AppListFetchTask(AppListFetchCallable callable, Handler handler) {
            super(callable);
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    List<AppInfo> appInfos = get();
                    AppListFetchResult result = buildAppListFetchResult(appInfos);
                    Message message = Message.obtain(mHandler, getMessageType(), result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, getTaskName() + " failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
        }

        abstract protected int getMessageType();

        abstract protected String getTaskName();

        abstract AppListFetchResult buildAppListFetchResult(List<AppInfo> appInfos);
    }

    private static final class AppLogoFetchCallable implements Callable<Drawable> {

        private Context mContext;
        private String mPackageName;

        private AppLogoFetchCallable(Context context, String packageName) {
            mContext = context;
            mPackageName = packageName;
        }

        @Override
        public Drawable call() throws Exception {
            Drawable appLogo = null;
            try {
                PackageManager packageManager = mContext.getPackageManager();
                appLogo = packageManager.getApplicationBanner(mPackageName);
                if (appLogo == null) {
                    Intent leanbackLauncherIntent = packageManager.getLeanbackLaunchIntentForPackage(mPackageName);
                    if (leanbackLauncherIntent != null) {
                        appLogo = packageManager.getActivityBanner(leanbackLauncherIntent);
                    }
                }
                if (appLogo == null) {
                    appLogo = packageManager.getApplicationLogo(mPackageName);
                }
                if (appLogo == null) {
                    appLogo = packageManager.getApplicationIcon(mPackageName);
                }
            } catch (NameNotFoundException e) {
                Log.d(TAG, "Error while fetching logo for package:" + mPackageName + ".Reason:" + e.getMessage());
            }
            return appLogo;
        }

        private String getPackageName() {
            return mPackageName;
        }
    }

    private static final class AppLogoFetchTask extends FutureTask<Drawable> {

        private Handler mHandler;
        private String mPackageName;
        private AppLogoFetchListener mAppLogoFetchListener;

        private AppLogoFetchTask(AppLogoFetchCallable callable, Handler handler, AppLogoFetchListener listener) {
            super(callable);
            mHandler = handler;
            mAppLogoFetchListener = listener;
            mPackageName = callable.getPackageName();
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Drawable logo = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_APP_LOGO_FETCH_COMPLETE;
                    message.obj = new AppLogoFetchResult(mPackageName, logo, mAppLogoFetchListener);
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "AppLogoFetchTask failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class UpdateRecommendedAppEnabledStateCallable implements Callable<Void> {

        private Context mContext;
        private HashMap<String, Boolean> mRecommendedAppEnabledStateMap;

        UpdateRecommendedAppEnabledStateCallable(Context context, Map<String, Boolean> recommendedAppEnabledStateMap) {
            mContext = context;
            mRecommendedAppEnabledStateMap = new HashMap<>(recommendedAppEnabledStateMap);
        }

        @Override
        public Void call() throws Exception {
            Set<Map.Entry<String, Boolean>> entries = mRecommendedAppEnabledStateMap.entrySet();
            int i = 0;
            ArrayList<ContentProviderOperation> updateOperations = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : entries) {
                ContentProviderOperation update = ContentProviderOperation.
                        newUpdate(HtvContract.HtvAppList.CONTENT_URI).
                        withValue(HtvContract.HtvAppList.COLUMN_RECOMMENDED_APP, entry.getValue() ? 1 : 0).
                        withSelection(HtvContract.HtvAppList.COLUMN_NAME.concat(" = ?"), new String[]{entry.getKey()}).
                        build();

                updateOperations.add(update);
            }
            try {
                mContext.getContentResolver().applyBatch(HtvContract.HtvAppList.CONTENT_URI.getAuthority(), updateOperations);
            } catch (OperationApplicationException | RemoteException e) {
                Log.w(TAG, "#### recommended app enabled state update operation failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
            return null;
        }
    }

    private static class UpdateRecommendedAppEnabledStateTask extends FutureTask<Void> {

        private Handler mHandler;
        private UpdateRecommendedAppEnabledStateListener mUpdateRecommendedAppEnabledStateListener;

        private UpdateRecommendedAppEnabledStateTask(UpdateRecommendedAppEnabledStateCallable callable, Handler handler, UpdateRecommendedAppEnabledStateListener updateRecommendedAppEnabledStateListener) {
            super(callable);
            mHandler = handler;
            mUpdateRecommendedAppEnabledStateListener = updateRecommendedAppEnabledStateListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    get();
                    UpdateRecommendedAppEnabledStateTaskResult result = new UpdateRecommendedAppEnabledStateTaskResult(mUpdateRecommendedAppEnabledStateListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_RECOMMENDED_APP_STATE_UPDATE_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "UpdateRecommendedAppEnabledStateTask failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class UpdateAppRecommendationEnabledStateCallable implements Callable<Void> {

        private Context mContext;
        private HashMap<String, Boolean> mAppRecommendationEnabledStateMap;

        UpdateAppRecommendationEnabledStateCallable(Context context, Map<String, Boolean> appRecommendationEnabledStateMap) {
            mContext = context;
            mAppRecommendationEnabledStateMap = new HashMap<>(appRecommendationEnabledStateMap);
        }

        @Override
        public Void call() throws Exception {
            Set<Map.Entry<String, Boolean>> entries = mAppRecommendationEnabledStateMap.entrySet();
            int i = 0;
            ArrayList<ContentProviderOperation> updateOperations = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : entries) {
                ContentProviderOperation update = ContentProviderOperation.
                        newUpdate(HtvContract.HtvAppList.CONTENT_URI).
                        withValue(HtvContract.HtvAppList.COLUMN_APP_RECOMMENDATION, entry.getValue() ? 1 : 0).
                        withSelection(HtvContract.HtvAppList.COLUMN_NAME.concat(" = ?"), new String[]{entry.getKey()}).
                        build();

                updateOperations.add(update);
            }
            try {
                mContext.getContentResolver().applyBatch(HtvContract.HtvAppList.CONTENT_URI.getAuthority(), updateOperations);
            } catch (OperationApplicationException | RemoteException e) {
                Log.w(TAG, "#### app recommendation enabled state update operation failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
            return null;
        }
    }

    private static class UpdateAppRecommendationEnabledStateTask extends FutureTask<Void> {

        private Handler mHandler;
        private UpdateAppRecommendationEnabledStateListener mUpdateAppRecommendationEnabledStateListener;

        private UpdateAppRecommendationEnabledStateTask(UpdateAppRecommendationEnabledStateCallable callable, Handler handler, UpdateAppRecommendationEnabledStateListener updateAppRecommendationEnabledStateListener) {
            super(callable);
            mHandler = handler;
            mUpdateAppRecommendationEnabledStateListener = updateAppRecommendationEnabledStateListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    get();
                    UpdateAppRecommendationEnabledStateTaskResult result = new UpdateAppRecommendationEnabledStateTaskResult(mUpdateAppRecommendationEnabledStateListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_APP_RECOMMENDATION_STATE_UPDATE_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "UpdateAppRecommendationEnabledStateTask failed.reason:" + e.getMessage());
                Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static abstract class AppListFetchResult {
        List<AppInfo> mAppList;

        private AppListFetchResult(List<AppInfo> appList) {
            mAppList = appList;
        }
    }

    private static final class RecommendedAppsListFetchResult extends AppListFetchResult {
        RecommendedAppsListFetchListener mRecommendedAppsListFetchListener;

        private RecommendedAppsListFetchResult(List<AppInfo> appInfos, RecommendedAppsListFetchListener listener) {
            super(appInfos);
            mRecommendedAppsListFetchListener = listener;
        }
    }

    private static final class EnabledAppListFetchResult extends AppListFetchResult {
        EnabledAppListFetchListener mEnabledAppListFetchListener;

        private EnabledAppListFetchResult(List<AppInfo> appInfos, EnabledAppListFetchListener listener) {
            super(appInfos);
            mEnabledAppListFetchListener = listener;
        }
    }

    private static final class AllAppListFetchResult extends AppListFetchResult {
        AllAppListFetchListener mAllAppListFetchListener;

        private AllAppListFetchResult(List<AppInfo> appInfos, AllAppListFetchListener listener) {
            super(appInfos);
            mAllAppListFetchListener = listener;
        }
    }

    private static final class AppLogoFetchResult {
        private String mPackageName;
        private Drawable mLogo;
        private AppLogoFetchListener mAppLogoFetchListener;

        private AppLogoFetchResult(String packageName, Drawable logo, AppLogoFetchListener appLogoFetchListener) {
            mPackageName = packageName;
            mLogo = logo;
            mAppLogoFetchListener = appLogoFetchListener;
        }
    }

    private static final class UpdateRecommendedAppEnabledStateTaskResult {
        UpdateRecommendedAppEnabledStateListener mUpdateRecommendedAppEnabledStateListener;

        private UpdateRecommendedAppEnabledStateTaskResult(UpdateRecommendedAppEnabledStateListener listener) {
            mUpdateRecommendedAppEnabledStateListener = listener;
        }
    }

    private static final class UpdateAppRecommendationEnabledStateTaskResult {
        UpdateAppRecommendationEnabledStateListener mUpdateAppRecommendationEnabledStateListener;

        private UpdateAppRecommendationEnabledStateTaskResult(UpdateAppRecommendationEnabledStateListener listener) {
            mUpdateAppRecommendationEnabledStateListener = listener;
        }
    }

    private interface EnabledAppListFetchListener {
        void onEnabledAppListFetched(List<AppInfo> enabledAppList);
    }

    private interface RecommendedAppsListFetchListener {
        void onRecommendedAppsListFetched(List<AppInfo> recommendedAppList);
    }

    interface UpdateRecommendedAppEnabledStateListener {
        void onRecommendedAppEnabledStateUpdated();
    }

    interface UpdateAppRecommendationEnabledStateListener {
        void onAppRecommendationEnabledStateUpdated();
    }

    private static final class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_FETCH_ENABLED_APP_LIST = 100;
        private static final int MSG_WHAT_ENABLED_APPS_FETCH_COMPLETE = 101;
        private static final int MSG_WHAT_ALL_APPS_FETCH_COMPLETE = 102;
        private static final int MSG_WHAT_APP_LOGO_FETCH_COMPLETE = 103;
        private static final int MSG_WHAT_RECOMMENDED_APP_STATE_UPDATE_COMPLETE = 104;
        private static final int MSG_WHAT_APP_RECOMMENDATION_STATE_UPDATE_COMPLETE = 105;
        private static final int MSG_WHAT_APP_SYNC_STARTED = 106;
        private static final int MSG_WHAT_APP_SYNC_COMPLETED_OR_FAILED = 107;
        private static final int MSG_WHAT_APP_RECOMMENDATION_AVAILABLE = 108;

        private WeakReference<AppsDataManager> mAppsDataManagerRef;

        private UiThreadHandler(AppsDataManager appsDataManager) {
            mAppsDataManagerRef = new WeakReference<>(appsDataManager);
        }

        @Override
        public void handleMessage(Message msg) {
            DdbLogUtility.logRecommendationChapter(TAG, "UiThreadHandler msg.what " + msg.what);
            if (msg.what == MSG_WHAT_FETCH_ENABLED_APP_LIST) {
                AppsDataManager appsDataManager = mAppsDataManagerRef.get();
                if (appsDataManager != null) {
                    appsDataManager.refreshEnabledAppList();
                }
                return;
            }

            if (msg.what == MSG_WHAT_ENABLED_APPS_FETCH_COMPLETE) {
                EnabledAppListFetchResult result = (EnabledAppListFetchResult) msg.obj;
                if (result.mEnabledAppListFetchListener != null) {
                    result.mEnabledAppListFetchListener.onEnabledAppListFetched(result.mAppList);
                }

                DashboardDataManager.getInstance().getPreviewProgramDataManager().fetchPreviewPrograms();

                return;
            }

            if (msg.what == MSG_WHAT_ALL_APPS_FETCH_COMPLETE) {
                AllAppListFetchResult result = (AllAppListFetchResult) msg.obj;
                if (result.mAllAppListFetchListener != null) {
                    result.mAllAppListFetchListener.onAllAppListFetchComplete(result.mAppList);
                }
                return;
            }

            if (msg.what == MSG_WHAT_APP_LOGO_FETCH_COMPLETE) {
                AppLogoFetchResult result = (AppLogoFetchResult) msg.obj;
                if (result.mAppLogoFetchListener != null) {
                    result.mAppLogoFetchListener.onAppLogoFetchComplete(result.mPackageName, result.mLogo);
                }
                return;
            }

            if (msg.what == MSG_WHAT_RECOMMENDED_APP_STATE_UPDATE_COMPLETE) {
                UpdateRecommendedAppEnabledStateTaskResult result = (UpdateRecommendedAppEnabledStateTaskResult) msg.obj;
                if (result.mUpdateRecommendedAppEnabledStateListener != null) {
                    result.mUpdateRecommendedAppEnabledStateListener.onRecommendedAppEnabledStateUpdated();
                }
                return;
            }

            if (msg.what == MSG_WHAT_APP_RECOMMENDATION_STATE_UPDATE_COMPLETE) {
                UpdateAppRecommendationEnabledStateTaskResult result = (UpdateAppRecommendationEnabledStateTaskResult) msg.obj;
                if (result.mUpdateAppRecommendationEnabledStateListener != null) {
                    result.mUpdateAppRecommendationEnabledStateListener.onAppRecommendationEnabledStateUpdated();
                }
                return;
            }
            if (msg.what == MSG_WHAT_APP_SYNC_STARTED) {
                AppsDataManager appsDataManager = mAppsDataManagerRef.get();
                if(appsDataManager != null){
                    appsDataManager.updateAppSyncStartedStatus(true);
                }
                return;
            }
            if (msg.what == MSG_WHAT_APP_SYNC_COMPLETED_OR_FAILED) {
                AppsDataManager appsDataManager = mAppsDataManagerRef.get();
                if(appsDataManager != null){
                    DdbLogUtility.logAppsChapter(TAG,"refreshEnabledAppList called");
                    appsDataManager.updateAppSyncStartedStatus(false);
                    appsDataManager.refreshEnabledAppList();
                }
                return;
            }
            if (msg.what == MSG_WHAT_APP_RECOMMENDATION_AVAILABLE) {
                RecommendedAppsListFetchResult result = (RecommendedAppsListFetchResult) msg.obj;
                if (result.mRecommendedAppsListFetchListener != null) {
                    result.mRecommendedAppsListFetchListener.onRecommendedAppsListFetched(result.mAppList);
                }
                return;
            }

        }
    }

    private static class AppLogoCache {
        private static final String TAG = "AppLogoCache";

        // Reserve 1/32th of the max runtime memory available for this LruCache in Kilo bytes
        private static final int CACHE_SIZE_IN_KBYTES = 16 * 1024; //Value of 16MB in KB

        private final LruCache<String, Drawable> mLogoCache;

        private AppLogoCache() {
            mLogoCache = new LruCache<String, Drawable>(CACHE_SIZE_IN_KBYTES){
                @Override
                protected int sizeOf(String key, Drawable value) {
                    int assumedSizeOfEachDrawable = 4096;
                    return assumedSizeOfEachDrawable / 1024;
                }
            };
        }

        private void clear() {
            mLogoCache.evictAll();
        }

        private Drawable getLogo(String packageName) {
            return mLogoCache.get(packageName);
        }

        private void addLogo(String packageName, Drawable logo) {
            mLogoCache.put(packageName, logo);
        }
    }

    private void registerIntentReceivers() {
        // listen for package changes
        final String PACKAGE_SCHEME = "package";
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme(PACKAGE_SCHEME);
        getContext().registerReceiver(mPackageReceiver, filter);
    }


    public class PackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            Log.d(TAG,"PACKAGE STATE CHANGED Package name "+packageName);
            //Netflix CR to refresh APP list only when Netflix is removed
            if(packageName.contains(NETFLIX)) {
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_FETCH_ENABLED_APP_LIST);
            }
        }
    }

    private void registerAppSyncDBListUpdateReceiver() {
        IntentFilter filter = new IntentFilter(Constants.INTENT_NOTIFY_APPLIST_UPDATING_STATUS);
        getContext().registerReceiver(mAppSyncDBReceiver, filter);
    }

    public class AppSyncDBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String EXTRA_UPDATING_STATUS = "status";//true: update is in progress
            if(intent.getAction().equals(INTENT_NOTIFY_APPLIST_UPDATING_STATUS)) {
                boolean status = intent.getBooleanExtra(EXTRA_UPDATING_STATUS ,true);
                DdbLogUtility.logAppsChapter(TAG,"App Sync status :"+status);
                updateAppSyncStartedStatus(status);
                if(!status){
                    DdbLogUtility.logAppsChapter(TAG,"App Sync Completed Refresh the UI");
                    mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_APP_SYNC_COMPLETED_OR_FAILED);
                }
            }
        }
    }

    private boolean isNetflixNeeded(int auth_needed, int auth_status, int auth_dev, int cdbID){
        if(mDashboardDataManager != null && !mDashboardDataManager.isProfessionalModeEnabled()){
            return false;
        }
        if(auth_needed == 1){
            if(auth_status == 1) {//Authorized
                try {
                    if (cdbID != -1) {
                        // CDB is selected in PBS
                        if (mDashboardDataManager != null && isAppValidInCDBUseCase() && auth_dev == 1) {
                            return true;
                        }
                    } else {
                        // DDB is selected in PBS
                        if (mDashboardDataManager != null && isAppValidInDDBUseCase()) {
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "exception in getting the details");
                }
            }
        }else{
            // auth not needed, directly send the app.
            return true;
        }
        return false;
    }

    private boolean isAppValidInCDBUseCase(){
        if(mDashboardDataManager != null &&
                mDashboardDataManager.isProfessionalModeEnabled() &&
                mDashboardDataManager.getSmartTvMode() == TvSettingsDefinitions.PbsSmarttvMode.PBSMGR_PROFILE &&
                mDashboardDataManager.getAppControlId() == TvSettingsDefinitions.PbsAppcontrolIdTypeConstants.PBSMGR_APPCONTROL_ID_TYPE_MANUAL &&
                mDashboardDataManager.getCULStatus() == 1 &&
                mDashboardDataManager.getDiagnosticLocation() == TvSettingsDefinitions.PbsDiagnosticDestination.PBSMGR_DIAGNOSTIC_DESTINATION_INTERNAL &&/*Rongkun : mapping need to used for Internal for needed Portal*/
                mDashboardDataManager.isGuestCheckedIn()){
            return true;
        }
        return false;
    }

    public boolean isAppValidInDDBUseCase(){
        if(mDashboardDataManager != null &&
                mDashboardDataManager.isProfessionalModeEnabled() &&
                mDashboardDataManager.getSmartTvMode() == TvSettingsDefinitions.PbsSmarttvMode.PBSMGR_PROFILE &&
                mDashboardDataManager.getAppControlId()  == TvSettingsDefinitions.PbsAppcontrolIdTypeConstants.PBSMGR_APPCONTROL_ID_TYPE_MANUAL &&
                mDashboardDataManager.getCULStatus() == 1 &&
                mDashboardDataManager.getDiagnosticLocation() == TvSettingsDefinitions.PbsDiagnosticDestination.PBSMGR_DIAGNOSTIC_DESTINATION_INTERNAL &&/*Rongkun : mapping need to used for Internal for needed Portal*/
                mDashboardDataManager.getPMSSetting() == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON &&
                mDashboardDataManager.isGuestCheckedIn()){
            return true;
        }
        return false;
    }

}
