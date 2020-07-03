package org.droidtv.defaultdashboard.ui.fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelSettingsListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SmartInfoListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.AppReccomendationShelfRow;
import org.droidtv.defaultdashboard.data.model.AppRecommendationShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ChannelCursorMapper;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.droidtv.defaultdashboard.data.model.SmartInfoPreviewProgramShelfRow;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.data.model.channelFilter.ChannelFilter;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.ui.presenter.AppRecommendationShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppRecommendationShelfRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppsShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppsShelfRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.ChannelsShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.RecommendedChannelsShelfRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.SmartInfoShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.SmartInfoShelfRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.SourcesShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.SourcesShelfRowPresenter;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.DDBImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

/**
 * Page fragment for the Recommended chapter
 */
public class RecommendedChapterFragment extends ChapterFragment implements
        SmartInfoListener, RecommendationListener, ChannelDataListener, ChannelSettingsListener, AppDataListener, DashboardDataManager.PreviewProgramsListener,DashboardDataManager.PbsSettingLanguageChangeListener {

    private static final String TAG = "RecommendedChapterFragment";

    private static final long SMART_INFO_SHELF_ROW_ID = 1;
    private static final long RECOMMENDED_CHANNELS_SHELF_ROW_ID = 2;
    private static final long RECOMMENDED_APPS_SHELF_ROW_ID = 3;
    private static final long RECOMMENDED_SOURCES_SHELF_ROW_ID = 4;

    private static final int SMART_INFO_ROW_TYPE_HTML = 1;
    private static final int SMART_INFO_ROW_TYPE_RECOMMENDATION = 2;

    private static final int SMART_INFO_SHELF_ROW_POSTION = 0;
    private static final int RECOMMENDED_CHANNELS_SHELF_ROW_POSITION = 1;
    private static final int RECOMMENDED_APPS_SHELF_ROW_POSITION = 2;

    private DashboardDataManager mDashboardDataManager;
    private ShelfRow mSmartInfoShelfRow;
    private ShelfRow mRecommendedChannelsShelfRow;
    private ShelfRow mRecommendedAppsShelfRow;
    private CursorObjectAdapter mRecommendedChannelsAdapter;
    private ArrayObjectAdapter mSourcesShelfListAdapter = null;
    private int mSmartInfoRowType;
    private static boolean isTransitionOngoing = false;

    private Map<String, ArrayObjectAdapter> mAppPackageAdapterMap;
    private Map<String, ShelfRow> mAppPackageShelfRowMap;
    private Map<Integer, ShelfRow> mPreviewProgramShelfRowMap;

    private UiHandler mUiHandler = null;//new UiHandler(Looper.getMainLooper());

    public RecommendedChapterFragment() {
        super();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mAppPackageAdapterMap = new HashMap<>();
        mAppPackageShelfRowMap = new HashMap<>();
        mPreviewProgramShelfRowMap = new HashMap<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
        isTransitionOngoing = false;
        mUiHandler = new UiHandler(Looper.getMainLooper());
        DDBImageLoader.setChapterVisibility(true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        DDBImageLoader.setChapterVisibility(false);
        isTransitionOngoing = false;
        cleanUp();
        super.onDestroy();
        DdbLogUtility.logRecommendationChapter(TAG, "#### onDestroy() exit");
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        registerRecommendationChangeListener();
        registerSmartInfoListener();
        registerChannelSettingsListener();
        registerChannelDataListener();
        registerAppDataListener();
		registerPreviewProgramsChangeListener();
        DashboardDataManager.getInstance().registerPbsSettingLanguageChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");
        unregisterRecommendationChangeListener();
        unregisterSmartInfoListener();
        unregisterChannelSettingsListener();
        unregisterChannelDataListener();
        unregisterAppDataListener();
        unregisterPreviewProgramsListener();
        DDBImageLoader.clearEvictedBitmaps();
        super.onDestroyView();
        DdbLogUtility.logRecommendationChapter(TAG, "#### onDestroyView() exit");
    }

    @Override
    protected void createRows() {
        Log.d(TAG, "#### createRows()");
        if(getActivity() == null || getContext() == null){//TF519PHINAMTK02-3000
            Log.d(TAG, "createRows: activty or context null");
            return;
        }
        if (mDashboardDataManager.isSmartInfoEnabled()) {
            mSmartInfoRowType = SMART_INFO_ROW_TYPE_HTML;
            if (mDashboardDataManager.isSmartInfoModeApp()) {
                mSmartInfoRowType = SMART_INFO_ROW_TYPE_RECOMMENDATION;
            }
            createSmartInfoShelf();
        }


        if (mDashboardDataManager.areChannelsEnabled()) {
            createRecommendedChannelsShelf();
        }

        if (mDashboardDataManager.areAppRecommendationsAvailable()) {
            createAppRecommendationsShelves();
        }

        if (mDashboardDataManager.areRecommendedAppsAvailable()) {
            createRecommendedAppsShelf();
        }
        setSelectionToFirstRow();
    }

    private void setSelectionToFirstRow() {
        if(isTransitionOngoing){
            Log.d("RecommendedChapterFragment", "setSelectionToFirstRow: isTransitionOngoing " +isTransitionOngoing);
            return;
        }

        if(getSelectedPosition() != 0) {
            setSelectedPosition(0, true);
        }else{
            setSelectedPosition(1, true);
            setSelectedPosition(0, true);
        }
    }

    @Override
    protected PresenterSelector getChapterPresenterSelector() {
        return new RecommendedChapterPresenterSelector();
    }

    private void registerRecommendationChangeListener() {
        mDashboardDataManager.addRecommendationListener(this);
    }

    private void unregisterRecommendationChangeListener() {
        mDashboardDataManager.removeRecommendationListener(this);
    }

    private void registerSmartInfoListener() {
        mDashboardDataManager.addSmartInfoListener(this);
    }

    private void unregisterSmartInfoListener() {
        mDashboardDataManager.removeSmartInfoListener(this);
    }

    private void registerPreviewProgramsChangeListener() {
        mDashboardDataManager.addPreviewProgramsListener(this);
    }
    private void unregisterPreviewProgramsListener() {
        mDashboardDataManager.removePreviewProgramsListener(this);
    }
    private void fetchSmartInfo() {
        mDashboardDataManager.fetchSmartInfo();
    }

    private void createSmartInfoShelf() {
        DdbLogUtility.logRecommendationChapter(TAG, "createSmartInfoShelf() called");
        if (mSmartInfoRowType == SMART_INFO_ROW_TYPE_RECOMMENDATION) {
			if(mDashboardDataManager.isSmartInfoAppPreviewProgramBased()){
				mSmartInfoShelfRow = buildPreviewSmartInfoRecommendationShelf();
			}else{
				mSmartInfoShelfRow = buildNotificationBasedSmartInfoShelf(); 
			}             
        } else {
            mSmartInfoShelfRow = buildSmartInfoHtmlShelf();           
        }
		 
		if (mSmartInfoShelfRow != null) {
            addRow(getSmartInfoShelfRowPosition(), mSmartInfoShelfRow);
        }
    }
	
    private ShelfRow buildPreviewSmartInfoRecommendationShelf(){
        PreviewProgramsChannel smartInfoPreviewChannel = mDashboardDataManager.getSmartInfoPreviewChannel();       
        ShelfRow previewProgBasedSmartInfoRow = buildPreviewProgramSmartInfoRecommendationsShelf(smartInfoPreviewChannel);
        return previewProgBasedSmartInfoRow;
    }

    private ShelfRow buildNotificationBasedSmartInfoShelf(){
        List<Recommendation> recommendations = mDashboardDataManager.getSmartInfoRecommendations();
        ShelfRow row = buildSmartInfoRecommendationsShelf(recommendations);
        return row;
    }

    private ShelfRow buildSmartInfoRecommendationsShelf(List<Recommendation> recommendations) {
        if (recommendations != null && !recommendations.isEmpty()) {
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(new SmartInfoShelfItemPresenter());
            shelfItemAdapter.addAll(0, recommendations);
            String shelfTitle = getSmartInfoShelfTitle();
            Drawable shelfIcon = getSmartInfoShelfIcon();
            return new ShelfRow(SMART_INFO_SHELF_ROW_ID, new ShelfHeaderItem(shelfTitle, shelfIcon), shelfItemAdapter);
        }
        // Smart info recommendations is empty or is not available yet. Fetch the data again so that if any data is available
        // this time it will be populated in the shelf
        fetchSmartInfo();
        return null;
    }

    private ShelfRow buildPreviewProgramSmartInfoRecommendationsShelf(PreviewProgramsChannel channel) {
		if(channel != null){
			//Sup: TODO: check how this can be avoided
			if(channel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_EMPTY_RECOMMENDATION){
				return buildEmptySmartInfoPreviewShelf(channel);
			}else {
				List<Recommendation> recommendations = channel.getPreviewProgramList();
				if (recommendations != null && !recommendations.isEmpty()) {
					AppRecommendationShelfItemPresenter itemPresenter = new AppRecommendationShelfItemPresenter();
					itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
					ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
					shelfItemAdapter.addAll(0, recommendations);

					String shelfTitle = getPreviewProgSmartInfoShelfTitle(channel.getPackageName());
					Drawable shelfIcon = getSmartInfoPreviewProgramShelfIcon(channel.getPackageName());
					return new SmartInfoPreviewProgramShelfRow(SMART_INFO_SHELF_ROW_ID, new AppRecommendationShelfHeaderItem(shelfTitle, shelfIcon, channel.getDisplayName(), channel.getPackageName() ,channel.getId()), shelfItemAdapter );
				}
			}
		}        
		//Smart info channel is empty.. trigger a fetch again
		fetchSmartInfo();
        return null;
    }

    private ShelfRow buildEmptySmartInfoPreviewShelf(PreviewProgramsChannel channel) {
        List<Recommendation> recommendations = channel.getPreviewProgramList();
        if (recommendations != null && !recommendations.isEmpty()) {
            SmartInfoShelfItemPresenter itemPresenter = new SmartInfoShelfItemPresenter();
            itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
            shelfItemAdapter.addAll(0, recommendations);
            String shelfTitle = getPreviewProgSmartInfoShelfTitle(channel.getPackageName());
            Drawable shelfIcon = getSmartInfoPreviewProgramShelfIcon(channel.getPackageName());
            DdbLogUtility.logRecommendationChapter(TAG, "buildEmptySmartInfoPreviewShelf: shelfTitle " + shelfTitle);
            return new ShelfRow(SMART_INFO_SHELF_ROW_ID, new ShelfHeaderItem(shelfTitle, shelfIcon), shelfItemAdapter);
        }
        return null;
    }

    private Intent getAppLaunchIntent(String packageName) {
        PackageManager packageManager = getContext().getPackageManager();
        Intent launchAppIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName);
        if (launchAppIntent == null) {
            launchAppIntent = packageManager.getLaunchIntentForPackage(packageName);
        }
        return launchAppIntent;
    }

    private String getSmartInfoShelfTitle(){
        String shelfTitle = mDashboardDataManager.getSmartInfoTitle();
        if (TextUtils.isEmpty(shelfTitle)) {
            shelfTitle = getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_SMARTINFO);
        }
        return shelfTitle;
    }

    private Drawable getSmartInfoPreviewProgramShelfIcon(String packageName){
        Drawable shelfIcon = getAppIcon(packageName);
        if (shelfIcon == null) {
            shelfIcon = ContextCompat.getDrawable(getContext(), R.drawable.icon_304_smart_info_n_48x48);
        }
        return shelfIcon;
    }

    private String getPreviewProgSmartInfoShelfTitle(String packageName){
        String shelfTitle = getAppTitle(packageName);
        if (TextUtils.isEmpty(shelfTitle)) {
            shelfTitle = getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_SMARTINFO);
        }
        return shelfTitle;
    }

    private String getAppTitle(String packageName) {
        String appTitle = null;
        try {
            PackageManager packageManager = getContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            appTitle = packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "#### buildAppRecommendationsShelves(). package not found:" + packageName);
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return appTitle;
    }

    private Drawable getAppIcon(String packageName) {
        Drawable appIcon = null;
        try {
            PackageManager packageManager = getContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            appIcon = packageManager.getApplicationIcon(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "#### buildAppRecommendationsShelves(). package not found:" + packageName);
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return appIcon;
    }

    private Drawable getSmartInfoShelfIcon(){
        Drawable shelfIcon = mDashboardDataManager.getSmartInfoIcon();
        if (shelfIcon == null) {
            shelfIcon = ContextCompat.getDrawable(getContext(), R.drawable.icon_304_smart_info_n_48x48);
        }
        return shelfIcon;
    }

    private ShelfRow buildSmartInfoHtmlShelf() {
        List<SmartInfo> smartInfoData = mDashboardDataManager.getSmartInfoData();
        if (smartInfoData != null && !smartInfoData.isEmpty()) {
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(new SmartInfoShelfItemPresenter());
            shelfItemAdapter.addAll(0, smartInfoData);
            String shelfTitle = getSmartInfoShelfTitle();
            Drawable shelfIcon = getSmartInfoShelfIcon();
            return new ShelfRow(SMART_INFO_SHELF_ROW_ID, new ShelfHeaderItem(shelfTitle, shelfIcon), shelfItemAdapter);
        }
        // Smart info html data is empty or is not available yet. Fetch the data again so that if any data is available
        // this time it will be populated in the shelf
        fetchSmartInfo();
        return null;
    }

    private void buildPreviewProgramsRecommendationsShelves() {
        android.util.Log.d(TAG, "buildPreviewProgramsRecommendationsShelves() called");
        List<AppInfo> appsEnabledForRecommendations = mDashboardDataManager.getAppsEnabledForRecommendations();
        for (AppInfo appInfo : appsEnabledForRecommendations) {
            String packageName = appInfo.getPackageName();
            List<PreviewProgramsChannel> previewProgramsChannelList = mDashboardDataManager.getPreviewProgramsChannelList(packageName);
            if (previewProgramsChannelList != null && !previewProgramsChannelList.isEmpty()) {
                for (PreviewProgramsChannel channel : previewProgramsChannelList) {
                    addPreviewProgramsRecommendation(channel);
                }
            }
        }
    }

    private void buildAppRecommendationsShelves() {
        Map<String, List<Recommendation>> appRecommendationsMap = mDashboardDataManager.getAppRecommendations();
        List<AppInfo> appsEnabledForRecommendations = mDashboardDataManager.getAppsEnabledForRecommendations();
        for (AppInfo appInfo : appsEnabledForRecommendations) {
            List<Recommendation> recommendationList = appRecommendationsMap.get(appInfo.getPackageName());
            String packageName = appInfo.getPackageName();
            ShelfRow appRecommendationsShelf = buildAppRecommendationsShelf(appInfo.getPackageName(), recommendationList);
            if (appRecommendationsShelf != null) {
                int position = getAppRecommendationShelfPosition(packageName);
                if(position <= getRowCount()) {
                    addRow(position, appRecommendationsShelf);
                }else{
                    addRow(appRecommendationsShelf);
                }
            }
        }
    }

    private ShelfRow buildAppRecommendationsShelf(String packageName, List<Recommendation> recommendations) {
        ShelfRow shelfRow = null;
        if (recommendations != null && !recommendations.isEmpty() && !mAppPackageShelfRowMap.containsKey(packageName)) {
            PackageManager packageManager = getContext().getPackageManager();
            String appShelfName = packageName;
            Drawable appShelfIcon = getContext().getDrawable(R.mipmap.ic_launcher);
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                appShelfName = packageManager.getApplicationLabel(applicationInfo).toString();
                appShelfIcon = packageManager.getApplicationIcon(applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "#### buildAppRecommendationsShelves(). package not found:" + packageName);
               Log.e(TAG,"Exception :" +e.getMessage());
            }

            AppRecommendationShelfItemPresenter itemPresenter = new AppRecommendationShelfItemPresenter();
            itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
            shelfItemAdapter.addAll(0, recommendations);

            // Each app recommendation shelf should have the last tile launch the app
            Intent launchAppIntent = getAppLaunchIntent(packageName);
            Recommendation openAppRecommendation = createAppLaunchRecommendation(launchAppIntent, packageName);
            shelfItemAdapter.add(openAppRecommendation);
            shelfRow = new ShelfRow(new ShelfHeaderItem(appShelfName, appShelfIcon), shelfItemAdapter);
            //For non preview program recommendations.
            mAppPackageAdapterMap.put(packageName, shelfItemAdapter);
            mAppPackageShelfRowMap.put(packageName, shelfRow);
        }
        return shelfRow;
    }

    private void createRecommendedChannelsShelf() {
        Log.d(TAG, "#### createRecommendedChannelsShelf()");
        mRecommendedChannelsShelfRow = buildRecommendedChannelsShelf();
        if (mRecommendedChannelsShelfRow != null) {
            addRow(getRecommendedChannelsShelfRowPosition(), mRecommendedChannelsShelfRow);
        }
    }

    private void createRecommendedAppsShelf() {
        DdbLogUtility.logRecommendationChapter(TAG, "createRecommendedAppsShelf() called");
        mRecommendedAppsShelfRow = buildRecommendedAppsShelf();
        if (mRecommendedAppsShelfRow != null) {
            addRow(getRecommendedAppsShelfRowPosition(), mRecommendedAppsShelfRow);
        }
    }

    private void createAppRecommendationsShelves() {
        DdbLogUtility.logRecommendationChapter(TAG, "createAppRecommendationsShelves() called");
        buildAppRecommendationsShelves();
        buildPreviewProgramsRecommendationsShelves();
    }

    private void createAppRecommendationsShelf(String packageName, List<Recommendation> recommendations) {
        ShelfRow appRecommendationsShelf = buildAppRecommendationsShelf(packageName, recommendations);
        if (appRecommendationsShelf != null) {
            int position = getAppRecommendationShelfPosition(packageName);
            addRow(position, appRecommendationsShelf);
        }
    }

    private int getAppRecommendationShelfPosition(String packageName) {
        int position = 0;
        ArrayList<SortObjects> sortAppObjects = new ArrayList<>();
        SortObjects newPkgPosition = null;

        Set<Map.Entry<String, ArrayObjectAdapter>> recommendationAppsList = mAppPackageAdapterMap.entrySet();
        for (Map.Entry<String, ArrayObjectAdapter> entry : recommendationAppsList) {
            String pkg = entry.getKey();
            int p1 = mDashboardDataManager.getAppPostionForPackage(pkg);
            int channelsCount = mDashboardDataManager.getPreviewProgramsChannelCount(pkg);
            Log.d(TAG, "packageName:" + pkg + " channelsCount:" + channelsCount);
            if (channelsCount <= 0) {
                channelsCount = 1;
            }
            SortObjects sortObject = new SortObjects(pkg, p1, channelsCount);
            if (pkg.equalsIgnoreCase(packageName)) {
                newPkgPosition = sortObject;
                // Log.d(TAG, "POS: new pkg coming: "+ packageName + ", its position: "+ newPkgPosition);
            }
            sortAppObjects.add(sortObject);
        }

        Collections.sort(sortAppObjects);
        int index = sortAppObjects.indexOf(newPkgPosition);

        for (int i = 0; i < index; i++) {
            position += sortAppObjects.get(i).channelCount;
        }

        if (mSmartInfoShelfRow != null) {
            position++;
        }
        if (mRecommendedChannelsShelfRow != null) {
            position++;
        }
        if (mRecommendedAppsShelfRow != null) {
            position++;
        }
        Log.d(TAG, "getAppRecommendationShelfPosition packageName:" + packageName + " position:" + position);
        return position;
    }

    //SUL: create fresh app recommendation row to sync the positions
    private void replaceAppRecommendationShelves() {
        ArrayList<String> appPackages = new ArrayList<>(mAppPackageShelfRowMap.keySet());
        int appPackagesCount = appPackages.size();
        for (int i = 0; i < appPackagesCount; i++) {
            String appPackage = appPackages.get(i);
            ShelfRow row = mAppPackageShelfRowMap.remove(appPackage);
            mAppPackageAdapterMap.remove(appPackage);
            removeRow(row);
        }
        if (mDashboardDataManager.areAppRecommendationsAvailable()) {
            createAppRecommendationsShelves();
        }

    }

    private void updateAppRecommendationShelves() {
        // Create app recommendation shelves if currently there are none and if recommendations are available
        if (mAppPackageShelfRowMap.isEmpty()) {
            if (mDashboardDataManager.areAppRecommendationsAvailable()) {
                createAppRecommendationsShelves();
            }
            return;
        }

        // Remove all app recommendation shelves if no recommendation is available
        if (!mDashboardDataManager.areAppRecommendationsAvailable()) {
            ArrayList<String> appPackages = new ArrayList<>(mAppPackageShelfRowMap.keySet());
            int appPackagesCount = appPackages.size();
            for (int i = 0; i < appPackagesCount; i++) {
                String appPackage = appPackages.get(i);
                ShelfRow row = mAppPackageShelfRowMap.remove(appPackage);
                mAppPackageAdapterMap.remove(appPackage);
                removeRow(row);
            }
            return;
        }

        // Create individual recommendation shelves if any recommendation-enabled app does not yet have a shelf
        Map<String, List<Recommendation>> appRecommendationsMap = mDashboardDataManager.getAppRecommendations();
        List<AppInfo> appsEnabledForRecommendations = mDashboardDataManager.getAppsEnabledForRecommendations();
        if (appRecommendationsMap != null) {
            for (int i = 0; appsEnabledForRecommendations != null && i < appsEnabledForRecommendations.size(); i++) {
                String appPackage = appsEnabledForRecommendations.get(i).getPackageName();
                if (!mAppPackageShelfRowMap.containsKey(appPackage)) {
                    createAppRecommendationsShelf(appPackage, appRecommendationsMap.get(appPackage));
                }
            }
        }

        // Remove recommendation shelf for the package that is not in recommendation-enabled app list
        ArrayList<String> currentShownPackages = new ArrayList<>(mAppPackageShelfRowMap.keySet());
        int currentShownPackagesCount = currentShownPackages.size();
        for (int i = 0; i < currentShownPackagesCount; i++) {
            String pkg = currentShownPackages.get(i);
            if (!appRecommendationsMap.containsKey(pkg)) {
                ShelfRow row = mAppPackageShelfRowMap.remove(pkg);
                mAppPackageAdapterMap.remove(pkg);
                removeRow(row);
            }
        }

        // Update recommendation shelf for the existing packages those that are in recommendation-enabled app list
        for (int i = 0; i < currentShownPackagesCount; i++) {
            String pkg = currentShownPackages.get(i);
            if (appRecommendationsMap.containsKey(pkg)) {
                ArrayObjectAdapter adapter = mAppPackageAdapterMap.get(pkg);
                adapter.notifyArrayItemRangeChanged(0, adapter.size());
            }
        }
    }

    private Recommendation createAppLaunchRecommendation(Intent launchAppIntent, String packageName) {
        PackageManager packageManager = getContext().getPackageManager();
        PendingIntent appLaunchPendingIntent = null;
        if (launchAppIntent != null) {
            appLaunchPendingIntent = PendingIntent.getActivity(getContext(), 0, launchAppIntent, 0);
        }
        String[] contentTypes = {Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION};
        String appTitle = "";
        String appDescription = "";

        Recommendation appLaunchRecommendation = new Recommendation();
        appLaunchRecommendation.setContentType(contentTypes);
        if (appLaunchPendingIntent != null) {
            appLaunchRecommendation.setPendingIntent(appLaunchPendingIntent);
        }

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            CharSequence title = packageManager.getApplicationLabel(applicationInfo);
            if (title != null) {
                appTitle = title.toString();
            }
            CharSequence description = applicationInfo.loadDescription(packageManager);
            if (description != null) {
                appDescription = description.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        appLaunchRecommendation.setTitle(appTitle);
        appLaunchRecommendation.setDescription(appDescription);

        return appLaunchRecommendation;
    }

    private void removeEmptyRecommendation(ArrayObjectAdapter adapter) {
        for (int i = 0; i < adapter.size(); i++) {
            Recommendation recommendation = (Recommendation) adapter.get(i);
            if (isEmptyRecommendation(recommendation)) {
                adapter.removeItems(i, 1);
                return;
            }
        }
    }

    private boolean isEmptyRecommendation(Recommendation recommendation) {
        String[] contentType = recommendation.getContentType();
        return contentType != null && Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION.equals(contentType[0]);
    }

    private boolean isSmartInfoRecommendation(Recommendation recommendation) {
        boolean smartInfoRecommendation = false;

        if (recommendation == null) {
            return smartInfoRecommendation;
        }

        String[] contentTypes = recommendation.getContentType();
        if (contentTypes == null) {
            return smartInfoRecommendation;
        }

        for (String contentType : contentTypes) {
            if (contentType.equals(Constants.CONTENT_TYPE_SMART_INFO_RECOMMENDATION)) {
                smartInfoRecommendation = true;
                break;
            }
        }

        return smartInfoRecommendation;
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

    private void onSmartInfoRecommmendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        if (mSmartInfoRowType == SMART_INFO_ROW_TYPE_RECOMMENDATION) {
            if (mSmartInfoShelfRow != null) {
                ArrayObjectAdapter adapter = (ArrayObjectAdapter) mSmartInfoShelfRow.getAdapter();
                // If this is a new recommendation, simply add it to the list
                if (recommendationChangeType == RecommendationChangeType.ADDED) {
                    removeEmptyRecommendation(adapter);
                    adapter.add(recommendation);
                    return;
                }

                // For recommendations to be cancelled, simply remove the notification from the list
                if (recommendationChangeType == RecommendationChangeType.CANCELED) {
                    for (int i = 0; i < adapter.size(); i++) {
                        Recommendation recommendationItem = (Recommendation) adapter.get(i);
                        if (recommendation.getKey().equals(recommendationItem.getKey()) && !isEmptyRecommendation(recommendationItem)) {
                            adapter.removeItems(i, 1);
                            // Try to fetch smart info if there are no smart info once the last smart info recommendation is cancelled
                            if (adapter.size() == 0) {
                                fetchSmartInfo();
                            }
                            return;
                        }
                    }
                }

                // Update if there is an existing recommendation with the same id. Otherwise, add this recommendation to the list
                if (recommendationChangeType == RecommendationChangeType.UPDATED) {
                    for (int i = 0; i < adapter.size(); i++) {
                        Recommendation recommendationItem = (Recommendation) adapter.get(i);
                        if (recommendation.getKey().equals(recommendationItem.getKey())) {
                            adapter.replace(i, recommendation);
                            return;
                        }
                    }
                    removeEmptyRecommendation(adapter);
                    adapter.add(recommendation);
                    return;
                }
            } else {
                if (recommendationChangeType == RecommendationChangeType.ADDED || recommendationChangeType == RecommendationChangeType.UPDATED) {
                    // This is the first SmartInfo recommendation.
                    // SmartInfo shelf is not yet created or has been removed. So, create smart info shelf
                    createSmartInfoShelf();
                }
            }
        }
    }

    private void onAppRecommmendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        if (recommendation.getPendingIntent() == null) {
            return;
        }

        String packageName = recommendation.getPendingIntent().getCreatorPackage();

        if (!mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
            return;
        }

        //TODO: Remove this check after app list query from htvapplist db is functional
        if ("org.droidtv.nettvadvert".equals(packageName)) {
            return;
        }

        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            if (mAppPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mAppPackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.remove(i);
                        adapter.add(i, recommendation);
                        return;
                    }
                }
                adapter.add(0, recommendation);
            } else {
                List<Recommendation> recommendations = new ArrayList<>();
                recommendations.add(recommendation);
                createAppRecommendationsShelf(packageName, recommendations);
            }
            return;
        }

        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            if (mAppPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mAppPackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.remove(recommendation);
                    }
                }

                if (adapter.size() == 1) { //open app tile is considered so size will be 1
                    mAppPackageAdapterMap.remove(packageName);
                    ShelfRow shelfRow = mAppPackageShelfRowMap.remove(packageName);
                    removeRow(shelfRow);
                }

                return;
            }
        }

        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            if (mAppPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mAppPackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.replace(i, recommendation);
                        return;
                    }
                }
            }

            if (mAppPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mAppPackageAdapterMap.get(packageName);
                adapter.add(0, recommendation);
            } else {
                List<Recommendation> recommendations = new ArrayList<>();
                recommendations.add(recommendation);
                createAppRecommendationsShelf(packageName, recommendations);
            }
            return;
        }
    }

    private void replaceSmartInfoShelfRow(ShelfRow row) {
        if (row != null) {
            removeRow(mSmartInfoShelfRow);
            mSmartInfoShelfRow = row;
            addRow(getSmartInfoShelfRowPosition(), mSmartInfoShelfRow);
        }
    }

    private void removeSmartInfoShelfRow() {
        removeRow(mSmartInfoShelfRow);
        mSmartInfoShelfRow = null;
    }

    private void registerChannelDataListener() {
        mDashboardDataManager.registerChannelDataListener(this);
    }

    private void unregisterChannelDataListener() {
        mDashboardDataManager.unregisterChannelDataListener(this);
    }

    private void registerChannelSettingsListener() {
        mDashboardDataManager.registerChannelSettingsListener(this);
    }

    private void unregisterChannelSettingsListener() {
        mDashboardDataManager.unregisterChannelSettingsListener(this);
    }

    private void registerAppDataListener() {
        mDashboardDataManager.addAppDataListener(this);
    }

    private void unregisterAppDataListener() {
        mDashboardDataManager.removeAppDataListener(this);
    }

    private void fetchActiveChannelFilter() {
        mDashboardDataManager.fetchActiveChannelFilter();
    }

    private int getSmartInfoShelfRowPosition() {
        return SMART_INFO_SHELF_ROW_POSTION;
    }

    private int getRecommendedChannelsShelfRowPosition() {
        int recommendedChannelsShelfRowPosition = RECOMMENDED_CHANNELS_SHELF_ROW_POSITION;
		
        if (mSmartInfoShelfRow == null) {
            recommendedChannelsShelfRowPosition--;
        }

        DdbLogUtility.logRecommendationChapter(TAG, "getRecommendedChannelsShelfRowPosition " + recommendedChannelsShelfRowPosition);
        return recommendedChannelsShelfRowPosition;
    }

    private int getRecommendedAppsShelfRowPosition() {
        int recommendedAppsShelfRowPosition = RECOMMENDED_APPS_SHELF_ROW_POSITION;

        if (mSmartInfoShelfRow == null) {
            recommendedAppsShelfRowPosition--;
        }

        if (mRecommendedChannelsShelfRow == null) {
            recommendedAppsShelfRowPosition--;
        }
        DdbLogUtility.logRecommendationChapter(TAG, "getRecommendedAppsShelfRowPosition" + recommendedAppsShelfRowPosition);
        return recommendedAppsShelfRowPosition;
    }

    private ShelfRow buildRecommendedChannelsShelf() {
        Log.d(TAG, "#### buildRecommendedChannelsShelf()");
        if (mDashboardDataManager.isCurrentChannelFilterSource()) {
            Log.d(TAG, "buildRecommendedChannelsShelf  isCurrentChannelFilterSource true");
            if (mSourcesShelfListAdapter == null) {
                SourcesShelfItemPresenter itemPresenter = new SourcesShelfItemPresenter();
                itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
                List<Source> mSources = DashboardDataManager.getInstance().getSources();
                if (mSources == null) return null;

                mSourcesShelfListAdapter = new ArrayObjectAdapter(itemPresenter);
                for (Source source : mSources) {
                    Log.d(TAG, "" + source.getId());
                    mSourcesShelfListAdapter.add(source);
                }
            }
            String headerTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_SHELF_RECOMMENDED_TVCHANNEL);
            Drawable headerIcon = getContext().getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.icon_192_sources_48x48);
            return new ShelfRow(RECOMMENDED_SOURCES_SHELF_ROW_ID, new ShelfHeaderItem(headerTitle, headerIcon), mSourcesShelfListAdapter);
        } else {
            ChannelFilter activeChannelFilter = mDashboardDataManager.getActiveChannelFilter();
            if (activeChannelFilter == null || !activeChannelFilter.hasChannels()) {
                return null;
            }
            Log.d(TAG, "buildRecommendedChannelsShelf isCurrentChannelFilterSource false");
            if (mRecommendedChannelsAdapter == null) {
                ChannelsShelfItemPresenter itemPresenter = new ChannelsShelfItemPresenter();
                itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
                mRecommendedChannelsAdapter = new CursorObjectAdapter(itemPresenter);
                mRecommendedChannelsAdapter.setMapper(new ChannelCursorMapper());
            }
            mRecommendedChannelsAdapter./*swapCursor*/changeCursor(activeChannelFilter.getCursor());
            String headerTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_SHELF_RECOMMENDED_TVCHANNEL);
            Drawable headerIcon = activeChannelFilter.getIcon();
            return new ShelfRow(RECOMMENDED_CHANNELS_SHELF_ROW_ID, new ShelfHeaderItem(headerTitle, headerIcon), mRecommendedChannelsAdapter);
        }
    }

    private boolean replaceRecommendedChannelsShelfRow(ShelfRow row) {
        Log.d(TAG, "#### replaceRecommendedChannelsShelfRow()");
        removeRow(mRecommendedChannelsShelfRow);
        mRecommendedChannelsShelfRow = row;
        Log.d(TAG, "#### replaceRecommendedChannelsShelfRow().before addRow");
        addRow(getRecommendedChannelsShelfRowPosition(), mRecommendedChannelsShelfRow);
        Log.d(TAG, "#### replaceRecommendedChannelsShelfRow().after addRow");
        return true;
    }

    private void removeRecommendedChannelsShelfRow() {
        Log.d(TAG, "#### removeRecommendedChannelsShelfRow()");
        removeRow(mRecommendedChannelsShelfRow);
        mRecommendedChannelsShelfRow = null;
    }

    private ShelfRow buildRecommendedAppsShelf() {
        List<AppInfo> recommendedApps = mDashboardDataManager.getRecommendedApps();
        if (recommendedApps == null || recommendedApps.isEmpty()) {
            return null;
        }

        AppsShelfItemPresenter itemPresenter = new AppsShelfItemPresenter();
        itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(itemPresenter);
        adapter.addAll(0, recommendedApps);
        String headerTitle = getString(org.droidtv.ui.strings.R.string.MAIN_LAUNCHER_TITLE_APPS);
        Drawable headerIcon = ContextCompat.getDrawable(getContext(), R.drawable.icon_1000_md_apps_n_48x48);
        return new ShelfRow(RECOMMENDED_APPS_SHELF_ROW_ID, new ShelfHeaderItem(headerTitle, headerIcon), adapter);
    }

    private boolean replaceRecommendedAppsShelfRow(ShelfRow row) {
        removeRow(mRecommendedAppsShelfRow);
        mRecommendedAppsShelfRow = row;
        addRow(getRecommendedAppsShelfRowPosition(), mRecommendedAppsShelfRow);
        return true;
    }

    private void removeRecommendedAppsShelfRow() {
        removeRow(mRecommendedAppsShelfRow);
        mRecommendedAppsShelfRow = null;
    }

    private void cleanUp() {
        resetHandler();
       // mDashboardDataManager.clearSmartInfoImageCache();
        //mDashboardDataManager.clearAppLogoCache();
    }

    private void resetHandler() {
        if(mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
            mUiHandler = null;
        }
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        ShelfRow shelfRow = (ShelfRow) row;
        if (shelfRow.getId() == SMART_INFO_SHELF_ROW_ID) {
            if (item instanceof SmartInfo) {
                onSmartInfoClicked((SmartInfo) item);
            } else {
                // Smart info will not be locked by MyChoice. So, no MyChoice handling needs to be done here
                Recommendation recommendation = (Recommendation) item;
                performRecommendationIntentAction(recommendation);
            }
            return;
        }

        if (shelfRow.getId() == RECOMMENDED_CHANNELS_SHELF_ROW_ID) {
            if (item instanceof Channel) {
                onChannelClicked((Channel) item);
            } else if (item instanceof Source) {
                onSourceClicked((Source) item);
            }
            return;
        }

        if(shelfRow.getId() == RECOMMENDED_SOURCES_SHELF_ROW_ID){
            if (item instanceof Channel) {
                onChannelClicked((Channel) item);
            } else if (item instanceof Source) {
                onSourceClicked((Source) item);
            }
            return;
        }

        if (shelfRow.getId() == RECOMMENDED_APPS_SHELF_ROW_ID) {
            if (item instanceof AppInfo) {
                onAppClicked((AppInfo) item);
            }
            return;
        }

        if (item instanceof Recommendation) {
            onRecommendationClicked((Recommendation) item);
            return;
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }

    @Override
    public void onSmartInfoDataAvailable() {
        ShelfRow row = buildSmartInfoHtmlShelf();
        replaceSmartInfoShelfRow(row);
    }

    @Override
    public void onSmartInfoRecommendationsAvailable() {
        ShelfRow row = buildNotificationBasedSmartInfoShelf();
        replaceSmartInfoShelfRow(row);
    }

    @Override
    public void onSmartInfoPreviewProgramsAvailable(){
		 ShelfRow row = buildPreviewSmartInfoRecommendationShelf();
		 replaceSmartInfoShelfRow(row);
	}

    @Override
    public void onSmartInfoOff() {
        removeSmartInfoShelfRow();
    }

    @Override
    public void onSmartInfoUnavailable() {
        removeSmartInfoShelfRow();
    }

    @Override
    public void onActiveChannelFilterFetched(ChannelFilter activeFilter) {
        if(mUiHandler != null && isAdded()) {
            mUiHandler.removeMessages(mUiHandler.MSG_WHAT_ACTIVE_CHANNEL_FILTER_FETCHED);
            sendMessage(mUiHandler.MSG_WHAT_ACTIVE_CHANNEL_FILTER_FETCHED, activeFilter);
        }
    }

    private void refreshChannelFilterShelf(ChannelFilter activeFilter) {
        Log.d(TAG, "#### onActiveChannelFilterFetched()");
        if(isChapterDestroyed()) {
            Log.d(TAG, "refreshChannelFilterShelf: ignored isTransitionOngoing " + isTransitionOngoing);
            return;
        }
        if(isCursorValid(DashboardDataManager.getInstance().getActiveChannelFilter())){
            Log.d(TAG, "refreshChannelFilterShelf: cursor closed");
            return;
        }
        ShelfRow row = buildRecommendedChannelsShelf();
        if (row != null) {
           replaceRecommendedChannelsShelfRow(row);
        } else {
          removeRecommendedChannelsShelfRow();
        }
    }

    private boolean isCursorValid(ChannelFilter activeFilter) {
        return activeFilter != null && (activeFilter.getCursor() != null) && (!activeFilter.getCursor().isClosed());
    }

    private boolean isChapterDestroyed() {
        if(!isAdded() || isDetached() || isTransitionOngoing){
            Log.d(TAG, "isChapterDestroyed true, this " + this);
            return true;
        }
        return false;
    }

    @Override
    public void onAvailableChannelFiltersFetched(List<ChannelFilter> filters) {

    }

    @Override
    public void onChannelFilterUpdated(ChannelFilter filter) {

    }

    @Override
    public void onChannelLogoEnabledStateChanged(boolean logosEnabled) {
        if (DashboardDataManager.getInstance().isCurrentChannelFilterSource()) {
            if (mSourcesShelfListAdapter != null) {
                int channelCount = mSourcesShelfListAdapter.size();
                mSourcesShelfListAdapter.notifyItemRangeChanged(0, channelCount);
            }
        } else {
            if (mRecommendedChannelsAdapter != null) {
                int channelCount = mRecommendedChannelsAdapter.size();
                mRecommendedChannelsAdapter.notifyItemRangeChanged(0, channelCount);
            }
        }
    }

    @Override
    public void onLastSelectedChannelUriChanged(String updatedLastSelectedChannelUri) {
        if (mRecommendedChannelsShelfRow != null) {
            int recommendedChannelShelfRowPosition = getRecommendedChannelsShelfRowPosition();
            notifyRowChanged(recommendedChannelShelfRowPosition);
        }
    }

    @Override
    public void onChannelsSettingChanged(boolean showChannels) {
        if (showChannels) {
            if (mRecommendedChannelsShelfRow == null) {
                createRecommendedChannelsShelf();
            }
        } else {
            removeRecommendedChannelsShelfRow();
        }
    }

    @Override
    public void onAppListFetched() {
        if(mUiHandler != null) {
            sendMessage(mUiHandler.MSG_WHAT_APP_LIST_FETCHED, null);
        }
    }

    private void sendMessage(int what, Object object) {
        if(mUiHandler != null) {
            Message message = mUiHandler.obtainMessage();
            message.what = what;
            message.obj = object;
            mUiHandler.sendMessage(message);
        }
    }

    public void refreshAppShelf(){
        if(isChapterDestroyed()){
            Log.d(TAG, "refreshAppShelf: ignored isTransitionOngoing " + isTransitionOngoing);
            return;
        }
        ShelfRow row = buildRecommendedAppsShelf();
        if (row != null && (mRecommendedAppsShelfRow != null) && !row.equals(mRecommendedAppsShelfRow)) {
            replaceRecommendedAppsShelfRow(row);
        } else {
            removeRecommendedAppsShelfRow();
        }
        replaceAppRecommendationShelves();
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        if (recommendationCategory == RecommendationHelper.Category.APPS) {
            createAppRecommendationsShelves();
        }
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        if (isSmartInfoRecommendation(recommendation) && mDashboardDataManager.isSmartInfoModeApp()) {
            onSmartInfoRecommmendationChanged(recommendation, recommendationChangeType);
            return;
        }

        if (isAppRecommendation(recommendation)) {
            onAppRecommmendationChanged(recommendation, recommendationChangeType);
            return;
        }
    }

    @Override
    public void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannels) {
        if(mUiHandler != null) {
            sendMessage(mUiHandler.MSG_WHAT_PREVIEW_PROGRAM_AVAILABLE, previewProgramsChannels);
        }
    }

    private void addPreviewProgramChannelShelf(PreviewProgramsChannel previewProgramsChannels){
        if(isChapterDestroyed()){
            Log.d(TAG, "addPreviewProgramChannelShelf: ignored isTransitionOngoing " + isTransitionOngoing);
            return;
        }
        int channelId = previewProgramsChannels.getId();
        DdbLogUtility.logRecommendationChapter("bharat", "onPreviewProgramChannelsAvailable Adding Row for" + previewProgramsChannels.getId() + " for package :" + previewProgramsChannels.getPackageName() + " for display name :" + previewProgramsChannels.getDisplayName());
        if(mPreviewProgramShelfRowMap.containsKey(channelId)) {
            DdbLogUtility.logRecommendationChapter("bharat","channelID already exists but some contents changed");
            ShelfRow shelfRow = mPreviewProgramShelfRowMap.remove(channelId);
            removeRow(shelfRow);
            addPreviewProgramsRecommendation(previewProgramsChannels);
        }else {
            DdbLogUtility.logRecommendationChapter("bharat","New channelID usecase");
            addPreviewProgramsRecommendation(previewProgramsChannels);
        }
    }

    private class UiHandler extends Handler{
        public  final int MSG_WHAT_PREVIEW_PROGRAM_AVAILABLE =1;
        public  final int MSG_WHAT_PREVIEW_PROGRAM_CHANGED =2;
        public  final int MSG_WHAT_PREVIEW_PROGRAM_DELETED =3;
        public  final int MSG_WHAT_APP_LIST_FETCHED = 4;
        public  final int MSG_WHAT_ACTIVE_CHANNEL_FILTER_FETCHED =5;

        public UiHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_WHAT_PREVIEW_PROGRAM_AVAILABLE:
                    PreviewProgramsChannel channel = (PreviewProgramsChannel)msg.obj;
                    addPreviewProgramChannelShelf(channel);
                    break;

                case MSG_WHAT_PREVIEW_PROGRAM_DELETED:
                    PreviewProgramsChannel deletedChannel = (PreviewProgramsChannel)msg.obj;
                    removePreviewProgramChannelShelf(deletedChannel);
                    break;

                case MSG_WHAT_PREVIEW_PROGRAM_CHANGED:
                    PreviewProgramsChannel changedPreviewChannel = (PreviewProgramsChannel)msg.obj;
                    refreshPreviewProgramChannelShelf(changedPreviewChannel);
                    break;

                case MSG_WHAT_APP_LIST_FETCHED:
                    refreshAppShelf();
                    break;

                case MSG_WHAT_ACTIVE_CHANNEL_FILTER_FETCHED:
                    ChannelFilter activeFilter = (ChannelFilter) msg.obj;
                    refreshChannelFilterShelf(activeFilter);
                    break;
            }
        }
    }

    @Override
    public void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels){
        if(mUiHandler != null) {
            sendMessage(mUiHandler.MSG_WHAT_PREVIEW_PROGRAM_DELETED, previewProgramsChannels);
        }
    }

    private void removePreviewProgramChannelShelf(PreviewProgramsChannel previewProgramsChannels) {
        if(isChapterDestroyed()){
            Log.d(TAG, "removePreviewProgramChannelShelf: ignored isTransitionOngoing " + isTransitionOngoing);
            return;
        }
        int channelId = previewProgramsChannels.getId();
        ShelfRow row = getShelfRowForDeletion(previewProgramsChannels);
        if (row != null) {
            AppRecommendationShelfHeaderItem item = (AppRecommendationShelfHeaderItem) row.getShelfHeader();
            DdbLogUtility.logRecommendationChapter("bharat", "onPreviewProgramChannelDeleted Row Already Exist for Deleted ChannelID " + channelId + " for package :" + item.getPackageName() + " for display name :" + item.getTitle()+" displayName :"+item.getPreviewProgramsTitle());
            ShelfRow shelfRow = mPreviewProgramShelfRowMap.remove(item.getChannelID());
            removeRow(shelfRow);
        }
    }

    private ShelfRow getShelfRowForDeletion(PreviewProgramsChannel previewProgramsChannel) {
        String packageName = previewProgramsChannel.getPackageName();
        Integer channelId = previewProgramsChannel.getId();
        if(mPreviewProgramShelfRowMap.containsKey(channelId)) {
            for (ShelfRow row : mPreviewProgramShelfRowMap.values()) {
                AppRecommendationShelfHeaderItem item = (AppRecommendationShelfHeaderItem) row.getShelfHeader();
                if ((item.getPackageName().equalsIgnoreCase(packageName)) && (item.getChannelID().equals(channelId))) {
                    DdbLogUtility.logCommon(TAG, "getShelfRowForDeletion and remove: for " + item.getPackageName() + " Title :" + item.getPreviewProgramsTitle() + "Previous channelID :"
                            + item.getChannelID() + "New ChannelID :" + channelId);
                    return row;
                }
            }
        }
        return null;
    }

    @Override
    public void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannels) {
        if(mUiHandler != null) {
            sendMessage(mUiHandler.MSG_WHAT_PREVIEW_PROGRAM_CHANGED, previewProgramsChannels);
        }
    }

    private void refreshPreviewProgramChannelShelf(PreviewProgramsChannel previewProgramsChannels){
        int channelId = previewProgramsChannels.getId();
        if(isChapterDestroyed()){
            Log.d(TAG, "refreshPreviewProgramChannelShelf: ignored isTransitionOngoing " + isTransitionOngoing);
            return;
        }
        DdbLogUtility.logRecommendationChapter(TAG, "onPreviewProgramChannelsChanged: channelId " + channelId + " isTransitionOngoing " + isTransitionOngoing);
        if(mPreviewProgramShelfRowMap.containsKey(channelId)) {
            ShelfRow shelfRow = mPreviewProgramShelfRowMap.remove(channelId);
            removeRow(shelfRow);
            addPreviewProgramsRecommendation(previewProgramsChannels);
        }
    }


    private void addPreviewProgramsRecommendation(PreviewProgramsChannel previewProgramsChannels) {
        String packageName = previewProgramsChannels.getPackageName();
        Log.d(TAG, "addPreviewProgramsReccommendation package : " + packageName + "  -- id:" + previewProgramsChannels.getId());
        if(isChapterDestroyed()) return;
        if (!mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
            return;
        }
        ShelfRow appRecommendationsShelf = buildPreviewProgramsRecommendationsShelf(previewProgramsChannels);
        if (appRecommendationsShelf != null) {
            int position = getAppRecommendationShelfPosition(packageName);
            if (position <= getRowCount()) {
                addRow(position, appRecommendationsShelf);
            } else {
                addRow(appRecommendationsShelf);
                Log.d(TAG, "invalid position for package : " + packageName + "  -- id:" + previewProgramsChannels.getId());
                Log.d(TAG, "mAppPackageShelfRowMap size : " + mAppPackageShelfRowMap.size());
            }
        }
    }

    private ShelfRow buildPreviewProgramsRecommendationsShelf(PreviewProgramsChannel previewProgramsChannels) {
        String packageName = previewProgramsChannels.getPackageName();
        String previewDisplayName = previewProgramsChannels.getDisplayName();
        List<Recommendation> recommendations = previewProgramsChannels.getPreviewProgramList();
        int channelId = previewProgramsChannels.getId();
        android.util.Log.d(TAG, "buildPreviewPrograms for ID = "+channelId );
        AppReccomendationShelfRow shelfRow = null;
        if (recommendations != null && !recommendations.isEmpty() && !mPreviewProgramShelfRowMap.containsKey(channelId)) {
            PackageManager packageManager = getContext().getPackageManager();
            String appShelfName = packageName;
            Drawable appShelfIcon = getContext().getDrawable(R.mipmap.ic_launcher);
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                appShelfName = packageManager.getApplicationLabel(applicationInfo).toString();
                appShelfIcon = packageManager.getApplicationIcon(applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "#### buildAppRecommendationsShelves(). package not found:" + packageName);
               Log.e(TAG,"Exception :" +e.getMessage());
            }
            AppRecommendationShelfItemPresenter itemPresenter = new AppRecommendationShelfItemPresenter();
            itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
            shelfItemAdapter.addAll(0, recommendations);
            Intent launchAppIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName);
            if (launchAppIntent == null) {
                launchAppIntent = packageManager.getLaunchIntentForPackage(packageName);
            }
            Recommendation openAppRecommendation = createAppLaunchRecommendation(launchAppIntent, packageName);
            shelfItemAdapter.add(openAppRecommendation);
            shelfRow = new AppReccomendationShelfRow(new AppRecommendationShelfHeaderItem(appShelfName, appShelfIcon, previewDisplayName, packageName ,channelId), shelfItemAdapter);
            mAppPackageAdapterMap.put(packageName, shelfItemAdapter);
            //mAppPackageShelfRowMap.put(packageName, shelfRow);//TF519PHINAMTK02-1411
            mPreviewProgramShelfRowMap.put(channelId, shelfRow);
        } else {
            Log.d(TAG, "Preview Programs channel exists : " + mPreviewProgramShelfRowMap.containsKey(channelId));
        }
        return shelfRow;
    }

    @Override
    public void onPbsSettingLanguageChange(int Language) {
        //clear mPreviewProgramShelfRowMap to fetch PreviewProgramfreshley
        //mPreviewProgramShelfRowMap.clear();
    }

    private static final class RecommendedChapterPresenterSelector extends PresenterSelector {
        private SmartInfoShelfRowPresenter mSmartInfoShelfRowPresenter;
        private RecommendedChannelsShelfRowPresenter mChannelsShelfRowPresenter;
        private AppRecommendationShelfRowPresenter mAppRecommendationShelfRowPresenter;
        private AppsShelfRowPresenter mAppsShelfRowPresenter;
        private SourcesShelfRowPresenter mSourcesShelfRowPresenter;

        private Presenter[] mPresenters;

        RecommendedChapterPresenterSelector() {
            mChannelsShelfRowPresenter = new RecommendedChannelsShelfRowPresenter(false);
            mChannelsShelfRowPresenter.setShadowEnabled(true);
            mChannelsShelfRowPresenter.setSelectEffectEnabled(false);
            mChannelsShelfRowPresenter.setKeepChildForeground(true);

            mSmartInfoShelfRowPresenter = new SmartInfoShelfRowPresenter(false);
            mSmartInfoShelfRowPresenter.setShadowEnabled(true);
            mSmartInfoShelfRowPresenter.setSelectEffectEnabled(false);
            mSmartInfoShelfRowPresenter.setKeepChildForeground(true);

            mAppRecommendationShelfRowPresenter = new AppRecommendationShelfRowPresenter(false);
            mAppRecommendationShelfRowPresenter.setShadowEnabled(true);
            mAppRecommendationShelfRowPresenter.setSelectEffectEnabled(false);
            mAppRecommendationShelfRowPresenter.setKeepChildForeground(true);

            mAppsShelfRowPresenter = new AppsShelfRowPresenter(false);
            mAppsShelfRowPresenter.setShadowEnabled(true);
            mAppsShelfRowPresenter.setSelectEffectEnabled(false);
            mAppsShelfRowPresenter.setKeepChildForeground(true);

            mSourcesShelfRowPresenter = new SourcesShelfRowPresenter(false);
            mSourcesShelfRowPresenter.setShadowEnabled(true);
            mSourcesShelfRowPresenter.setSelectEffectEnabled(false);
            mSourcesShelfRowPresenter.setKeepChildForeground(true);

            mPresenters = new Presenter[]{
                    mSmartInfoShelfRowPresenter,
                    mChannelsShelfRowPresenter,
                    mAppRecommendationShelfRowPresenter,
                    mAppsShelfRowPresenter,
                    mSourcesShelfRowPresenter
            };
        }

        @Override
        public Presenter[] getPresenters() {
            return mPresenters;
        }

        @Override
        public Presenter getPresenter(Object item) {
            ShelfRow row = (ShelfRow) item;
            if(row == null) return null;
            if (row.getId() == SMART_INFO_SHELF_ROW_ID) {
                return mSmartInfoShelfRowPresenter;
            }else if (row.getId() == RECOMMENDED_CHANNELS_SHELF_ROW_ID) {
                return mChannelsShelfRowPresenter;
            } else if (row.getId() == RECOMMENDED_APPS_SHELF_ROW_ID) {
                return mAppsShelfRowPresenter;
            } else if (row.getId() == RECOMMENDED_SOURCES_SHELF_ROW_ID){
                return mSourcesShelfRowPresenter;
            }
            else {
                return mAppRecommendationShelfRowPresenter;
            }
        }
    }
    class SortObjects implements Comparable<SortObjects> {

        String packageName;
        int sortPosition;
        int channelCount;

        public SortObjects(String pkg, int position, int count) {
            packageName = pkg;
            sortPosition = position;
            channelCount = count;
        }

        @Override
        public int compareTo(@NonNull SortObjects o) {
            return this.sortPosition - o.sortPosition;
        }
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        DdbLogUtility.logRecommendationChapter(TAG, "onLowMemory called");
    }

    @Override
    public void  onTransitionStart (){
        isTransitionOngoing = true;
        super.onTransitionStart();
        Log.d(TAG, "onTransitionStart() this " + this + " isTransitionOngoing " +isTransitionOngoing);
    }

    @Override
    public void onTransitionEnd (){
        super.onTransitionEnd();
        isTransitionOngoing = false;
        Log.d(TAG, "onTransitionEnd() this " + this + " isTransitionOngoing " + isTransitionOngoing);
    }
}
