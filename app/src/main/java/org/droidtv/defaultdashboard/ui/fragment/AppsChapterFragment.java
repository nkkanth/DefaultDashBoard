package org.droidtv.defaultdashboard.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.appsChapter.AppsChapterCountryShelfItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.presenter.AppsChapterCountryItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppsChapterCountryRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppsShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppsShelfRowPresenter;
import org.droidtv.defaultdashboard.util.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import static org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppCategoryFilterStateChangeListener;
import static org.droidtv.defaultdashboard.data.manager.DashboardDataManager.PbsSettingCountryChangeListener;
import static org.droidtv.defaultdashboard.data.model.appsChapter.CountryListConstants.TV_SETTINGS_COUNTRY_DRAWABLE_ARRAY;
import static org.droidtv.defaultdashboard.data.model.appsChapter.CountryListConstants.TV_SETTINGS_COUNTRY_ORDER_ARRAY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_ADULT;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_CHILDREN;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_ENTERTAINMENT;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_FINANCIAL;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_HEALTH;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_LIFE_STYLE;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_LOCAL_INFO;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_MAGAZINE;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_MOVIES;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_MUSIC;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_MY_APPS;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_NEWS;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_OTHER;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_SOCIAL;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_SPORTS;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_TECHNOLOGY;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_TRAVEL;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_TV_CHANNELS;
import static org.droidtv.defaultdashboard.util.Constants.APPS_CATEGORY_WEATHER;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_FILTER_CLASS_NAME_INTENT;
import static org.droidtv.defaultdashboard.util.Constants.COUNTRY_FILTER_INTENT;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public class AppsChapterFragment extends ChapterFragment implements PbsSettingCountryChangeListener, AppCategoryFilterStateChangeListener,
        DashboardDataManager.AppDataListener, DashboardDataManager.AppsCountryFilterStateListener {

    private static final String TAG = "AppsChapterFragment";
    private ShelfRow mCountryShelfRow;
    private static final int COUNTRY_ROW_ID = 0;
    private static final int SELECTED_COUNTRY_ROW_CHANGE_NOTIFY_INDEX = 0;
    private DashboardDataManager mDashboardDataManager;
    private UiThreadHandler mUiThreadHandler;

    private List<ShelfRow> mCategorizedShelfRowList;
    private int mCountryIntValue;
    private String mCountryCode;

    private static final int[] APPS_CATEGORY_DRAWABLE_ARRAY = {
            R.drawable.icon_126_info_n_48x48,
            R.drawable.icon_236_news_n_48x48,
            R.drawable.icon_243_genre_sports_n_48x48,
            R.drawable.icon_226_genre_economy_business_n_48x48,
            R.drawable.icon_329_technology_n_48x48,
            R.drawable.icon_270_weather_partly_cloudy_n_48x48,
            R.drawable.icon_74_tv_rc_n_48x48,
            R.drawable.icon_96_media_video_n_48x48,
            R.drawable.icon_97_sound_media_music_n_48x48,
            R.drawable.icon_379_magazines_n_48x48,
            R.drawable.icon_220_genre_children_n_48x48,
            R.drawable.icon_246_genre_travel_n_48x48,
            R.drawable.icon_233_lifestyle_n_48x48,
            R.drawable.icon_228_entertainment_n_48x48,
            R.drawable.icon_229_genre_family_n_48x48,
            R.drawable.icon_380_healthandfitness_n_48x48,
            R.drawable.icon_216_adult_n_48x48,
            R.drawable.icon_237_genre_other_n_48x48,
    };

    private static final String[] APPS_CATEGORY_ARRAY = {
            APPS_CATEGORY_LOCAL_INFO,
            APPS_CATEGORY_NEWS,
            APPS_CATEGORY_SPORTS,
            APPS_CATEGORY_FINANCIAL,
            APPS_CATEGORY_TECHNOLOGY,
            APPS_CATEGORY_WEATHER,
            APPS_CATEGORY_TV_CHANNELS,
            APPS_CATEGORY_MOVIES,
            APPS_CATEGORY_MUSIC,
            APPS_CATEGORY_MAGAZINE,
            APPS_CATEGORY_CHILDREN,
            APPS_CATEGORY_TRAVEL,
            APPS_CATEGORY_LIFE_STYLE,
            APPS_CATEGORY_ENTERTAINMENT,
            APPS_CATEGORY_SOCIAL,
            APPS_CATEGORY_HEALTH,
            APPS_CATEGORY_ADULT,
            APPS_CATEGORY_OTHER,
    };

    private Map<String, List<AppInfo>> mCategorizedAppListMap;
    private Map<String, List<AppInfo>> mAllEnabledAppListMap;
    private Map<String, List<AppInfo>> mCountryAppListMap;
    private Map<String, Integer> mShelfCategoryNameIconMap;

    public AppsChapterFragment() {
        super();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mUiThreadHandler = new UiThreadHandler(this);
        mCategorizedShelfRowList = new ArrayList<ShelfRow>();
        mCategorizedAppListMap = new LinkedHashMap<String, List<AppInfo>>();
        mAllEnabledAppListMap = new LinkedHashMap<String, List<AppInfo>>();
        mCountryAppListMap = new TreeMap<String, List<AppInfo>>();
        mShelfCategoryNameIconMap = new HashMap<String, Integer>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        registerListeners();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");
        unregisterListeners();
        cleanUp();
        super.onDestroyView();
        DdbLogUtility.logAppsChapter(TAG, "#### onDestroyView() exit");

    }

    @Override
    protected void createRows() {
        mCountryIntValue = mDashboardDataManager.getPbsCountrySelectedCode();
        mCountryCode = mDashboardDataManager.getCountryCode(mCountryIntValue);
        DdbLogUtility.logAppsChapter(TAG, "createRows mCountryIntValue " + mCountryIntValue + " mCountryCode " + mCountryCode);
        buildCategoryIconMap();
        if (mDashboardDataManager.isAppCountryFilterEnabled()) {
            createCountryShelf();
        }
        createAppShelfByCondition();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onAppListFetched() {
        mCountryIntValue = mDashboardDataManager.getPbsCountrySelectedCode();
        DdbLogUtility.logAppsChapter(TAG, "onAppListFetched() mCountryIntValue " + mCountryIntValue);
        createAppShelfByCondition();
    }

    @Override
    protected PresenterSelector getChapterPresenterSelector() {
        return new AppsChapterPresenterSelector();
    }

    private void registerListeners() {
        registerAppCategoryFilterStateChangeListener();
        registerPbsSettingCountryChangeListener();
        registerAppDataListener();
        registerAppsCountryFilterStateChangeListener();
    }

    private void registerAppsCountryFilterStateChangeListener() {
        mDashboardDataManager.registerAppCountryFilterStateChangeListener(this);
    }

    private void unregisterAppDataListener() {
        mDashboardDataManager.removeAppDataListener(this);
    }

    private void registerAppDataListener() {
        mDashboardDataManager.addAppDataListener(this);
    }


    private void registerPbsSettingCountryChangeListener() {
        mDashboardDataManager.registerPbsSettingCountryChangeListener(this);
    }

    private void unregisterPbsSettingCountryChangeListener() {
        mDashboardDataManager.unregisterPbsSettingCountryChangeListener(this);
    }

    private void unregisterListeners() {
        unregisterPbsSettingCountryChangeListener();
        unregisterAppCategoryFilterStateChangeListener();
        unregisterAppDataListener();
        unregisterAppsCountryStateListener();
    }

    private void unregisterAppsCountryStateListener() {
        mDashboardDataManager.unregisterAppCountryFilterStateChangeListener(this);
    }

    private void registerAppCategoryFilterStateChangeListener() {
        mDashboardDataManager.registerAppCategoryFilterStateChangeListener(this);
    }

    private void unregisterAppCategoryFilterStateChangeListener() {
        mDashboardDataManager.unregisterAppCategoryFilterStateChangeListener(this);
    }

    private void buildCategoryIconMap() {
        mShelfCategoryNameIconMap.clear();
        for (int i = 0; i < APPS_CATEGORY_ARRAY.length; i++) {
            mShelfCategoryNameIconMap.put(APPS_CATEGORY_ARRAY[i], APPS_CATEGORY_DRAWABLE_ARRAY[i]);
        }
    }

    private void buildCategorizedEnabledAppListMap() {
        List<AppInfo> recommendedAppList = new ArrayList<>();
        String appCategory;
        mAllEnabledAppListMap.clear();
        for (int i = 0; i < APPS_CATEGORY_ARRAY.length; i++) {
            appCategory = APPS_CATEGORY_ARRAY[i];
            recommendedAppList = mDashboardDataManager.getAppsByCategory(appCategory);
            if ((null != recommendedAppList) && !(recommendedAppList.isEmpty())) {
                mAllEnabledAppListMap.put(appCategory, recommendedAppList);
            }
        }
    }

    private void buildCountryAppList(String country) {
        List<AppInfo> countryAppList = mDashboardDataManager.getAppsByCountryAndAllCategory(country);
        Log.d(TAG, "buildCountryAppList: country " + country + "size " +countryAppList != null ? Integer.toString(countryAppList.size()) : " null");
        mCountryAppListMap.clear();
        if ((null != countryAppList) && !(countryAppList.isEmpty())) {
            mCountryAppListMap.put(country, countryAppList);
        }else{
            Log.d(TAG, "buildCountryAppList: no apps available, searching for international apps");
            countryAppList = mDashboardDataManager.getAppsByCountry(Constants.APPS_COUNTRY_CODE_INTERNATIONAL);
            if ((null != countryAppList) && !(countryAppList.isEmpty())) {
                mCountryAppListMap.put(country, countryAppList);
            }
        }
    }

    private void buildCategorizedAppListMap() {
        mCategorizedAppListMap.clear(); // rebuilding map so that residues will be removed on category removed from pbs menu
        mCountryCode = mDashboardDataManager.getCountryCode(mCountryIntValue);
        List<AppInfo> recommendedAppList = mCountryAppListMap.get(mCountryCode);
        if (recommendedAppList == null || recommendedAppList.isEmpty()) {
            return;
        }
        for (int i = 0; i < APPS_CATEGORY_ARRAY.length; i++) {
            List<AppInfo> categorizedAppList = new ArrayList<>();
            String category = APPS_CATEGORY_ARRAY[i];
            for (int j = 0; j < recommendedAppList.size(); j++) {
                AppInfo appInfo = recommendedAppList.get(j);
                String appCategory = appInfo.getCategories();
                if (appCategory != null && appCategory.contains(category) && !appInfo.getIsEditModeEnabled()) {
                    categorizedAppList.add(appInfo);
                    mCategorizedAppListMap.put(category, categorizedAppList);
                }
            }
        }
    }

    private List<AppInfo> buildSingleAppListMap() {
        List<AppInfo> singleRowAppList = new ArrayList<>();
        List<AppInfo> allEnabledList = mCountryAppListMap.get(mCountryCode);
        for (int l = 0; allEnabledList != null && l < allEnabledList.size(); l++) {
            if (!allEnabledList.get(l).getCategories().contains(getString(org.droidtv.ui.strings.R.string.MAIN_GAMES)) && !allEnabledList.get(l).getIsEditModeEnabled()) {
                singleRowAppList.add(allEnabledList.get(l));
            }
        }
        return singleRowAppList;
    }

    private void createCountryShelf() {

        ArrayObjectAdapter countryItemAdapter = new ArrayObjectAdapter(new AppsChapterCountryItemPresenter());
        AppsChapterCountryShelfItem appsChapterCountryShelfItem = new AppsChapterCountryShelfItem();
        countryItemAdapter.add(appsChapterCountryShelfItem);

        String countryName = "";
        int countryNameResourceId = mDashboardDataManager.getSelectedCountryNameResourceId();
        if (countryNameResourceId == -1) {
            countryName = getString(org.droidtv.ui.strings.R.string.MAIN_INTERNATIONAL);
        } else {
            countryName = getString(countryNameResourceId);
        }
        DdbLogUtility.logAppsChapter(TAG, "createCountryShelf countryName " + countryName);

        Drawable countryFlag = null;
        int countryFlagResourceId = mDashboardDataManager.getSelectedCountryFlagResourceId();
        if (countryFlagResourceId == -1) {
            countryFlag = ContextCompat.getDrawable(getContext(), R.drawable.international);
        } else {
            countryFlag = ContextCompat.getDrawable(getContext(), countryFlagResourceId);
        }

        mCountryShelfRow = new ShelfRow(COUNTRY_ROW_ID, new ShelfHeaderItem(countryName, countryFlag), countryItemAdapter);
        addRow(mCountryShelfRow);
    }


    private void createAppShelf(String shelfTitle, Drawable shelfIcon, List<AppInfo> appInfoList) {
        DdbLogUtility.logAppsChapter(TAG, "shelfTitle " + shelfTitle);
        AppsShelfItemPresenter itemPresenter = new AppsShelfItemPresenter();
        itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
        ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
        shelfItemAdapter.addAll(0, appInfoList);
        ShelfRow shelfRow = new ShelfRow(new ShelfHeaderItem(getShelfTitle(shelfTitle), shelfIcon), shelfItemAdapter);
        mCategorizedShelfRowList.add(shelfRow);
        addRow(shelfRow);
    }

    private String getShelfTitle(String category) {
        String currentLanguageTitle = null;
        switch (category) {
            case APPS_CATEGORY_ADULT:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_ADULT);
                break;
            case APPS_CATEGORY_CHILDREN:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_CHILDREN);
                break;
            case APPS_CATEGORY_ENTERTAINMENT:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_GENRE_ENTERTAINMENT);
                break;
            case APPS_CATEGORY_FINANCIAL:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_TTV_LST_FINANCIAL);
                break;
            case APPS_CATEGORY_HEALTH:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_APP_CATEGORY_HEALTH_FITNESS);
                break;
            case APPS_CATEGORY_LIFE_STYLE:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_GENRE_LIFESTYLE);
                break;
            case APPS_CATEGORY_LOCAL_INFO:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_TTV_LST_LOCAL_INFO);
                break;
            case APPS_CATEGORY_MAGAZINE:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_APP_CATEGORY_MAGAZINES);
                break;
            case APPS_CATEGORY_MOVIES:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_TTV_LST_MOVIES);
                break;
            case APPS_CATEGORY_MUSIC:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_MUSIC);
                break;
            case APPS_CATEGORY_NEWS:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_NEWS);
                break;
            case APPS_CATEGORY_OTHER:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_OTHER);
                break;
            case APPS_CATEGORY_SOCIAL:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_APP_CATEGORY_SOCIAL);
                break;
            case APPS_CATEGORY_SPORTS:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_SPORTS);
                break;
            case APPS_CATEGORY_TECHNOLOGY:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_TTV_LST_TECHNOLOGY);
                break;
            case APPS_CATEGORY_TRAVEL:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_TTV_LST_TRAVEL);
                break;
            case APPS_CATEGORY_TV_CHANNELS:
                currentLanguageTitle = getString(org.droidtv.ui.strings.R.string.MAIN_TV_CHANNELS);
                break;
            case APPS_CATEGORY_WEATHER:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_TTV_LST_WEATHER);
                break;
            case APPS_CATEGORY_MY_APPS:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_MYAPPS);
                break;
            default:
                currentLanguageTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ALL_APPS);
                break;
        }
        DdbLogUtility.logAppsChapter(TAG, "category " + category  + " currentLanguageTitle " + currentLanguageTitle);

        return currentLanguageTitle;
    }


    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        DdbLogUtility.logAppsChapter(TAG, "onItemClicked rowID " + row.getId());
        if (row.getId() == COUNTRY_ROW_ID) {
            Intent launchCountryFilterIntent = new Intent();
            launchCountryFilterIntent.setClassName(COUNTRY_FILTER_INTENT, COUNTRY_FILTER_CLASS_NAME_INTENT);
            getContext().startActivityAsUser(launchCountryFilterIntent, UserHandle.CURRENT_OR_SELF);
        } else {
            if (item instanceof AppInfo) {
                onAppClicked((AppInfo) item);
            }
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }

    @Override
    public void onPbsSettingCountryChange(int tvSettingsCountryCode) {
        DdbLogUtility.logAppsChapter(TAG, "onPbsSettingCountryChange() called with: tvSettingsCountryCode = [" + tvSettingsCountryCode + "]");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_COUNTRY_CHANGED, tvSettingsCountryCode, 0);
        message.sendToTarget();
    }

    private void onCountryChanged(int newCountryCode) {
        DdbLogUtility.logAppsChapter(TAG, "newCountryCode " + newCountryCode);
        if (getActivity() != null) {
            ShelfHeaderItem shelfHeaderItem = new ShelfHeaderItem(getString(TV_SETTINGS_COUNTRY_ORDER_ARRAY[newCountryCode]),
                    ContextCompat.getDrawable(getContext(), TV_SETTINGS_COUNTRY_DRAWABLE_ARRAY[newCountryCode]));
            if (mCountryShelfRow != null) {
                mCountryShelfRow.setShelfHeaderItem(shelfHeaderItem);
                notifyRowChanged(SELECTED_COUNTRY_ROW_CHANGE_NOTIFY_INDEX);
            }
            mCountryIntValue = newCountryCode;
            mCountryCode = mDashboardDataManager.getCountryCode(mCountryIntValue);
            DdbLogUtility.logAppsChapter(TAG, " mCountryCode " + mCountryCode);
            createAppShelfByCondition();
        }
    }

    @Override
    public void onAppCategoryFilterStateChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APP_CATEGORY_ENABLED_STATE_CHANGED);
        message.sendToTarget();
    }

    @Override
    public void onAppCategoryFilterStateChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APP_CATEGORY_ENABLED_STATE_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void appCategoryEnabledStateChanged() {
        if (getActivity() != null) {
            createAppShelfByCondition();
        }
    }

    private void createAppShelfByCondition() {
        boolean isAppCountryFilterEnabled = mDashboardDataManager.isAppCountryFilterEnabled();
        boolean isAppCategoryFilterEnabled = mDashboardDataManager.isAppCategoryFilterEnabled();
        DdbLogUtility.logAppsChapter(TAG, "createAppShelfByCondition isAppCountryFilterEnabled " + isAppCountryFilterEnabled
                                                + " isAppCategoryFilterEnabled " + isAppCategoryFilterEnabled + " mCountryCode " + mCountryCode);
        if (isAppCountryFilterEnabled) {
            buildCountryAppList(mCountryCode);
            buildCategorizedAppListMap();
            removeRows(mCategorizedShelfRowList);
            mCategorizedShelfRowList.clear();

            if(mDashboardDataManager.getAllUserInstalledApps()!= null && !mDashboardDataManager.getAllUserInstalledApps().isEmpty()){
                createMyAppsRow(getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_MYAPPS), ContextCompat.getDrawable(getContext(), R.drawable.icon_400_person_app_n_48x48), mDashboardDataManager.getAllUserInstalledApps());
            }

            if (isAppCategoryFilterEnabled) {
                createMultipleAppShelves(mCategorizedAppListMap);
            } else {
                createSingleAppShelf(buildSingleAppListMap());
            }
        } else {
            buildCategorizedEnabledAppListMap();
            removeRows(mCategorizedShelfRowList);
            mCategorizedShelfRowList.clear();
            if (isAppCategoryFilterEnabled) {
                createMultipleAppShelves(mAllEnabledAppListMap);
            } else {
                createSingleAppShelf(mDashboardDataManager.getEnabledAppList());
            }
        }
    }

    @Override
    public void onAppsCountryFilterStateChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APP_COUNTRY_ENABLED_STATE_CHANGED);
        message.sendToTarget();
    }

    private void appCountryEnabledStateChanged() {
        if (getActivity() != null) {
            if (mDashboardDataManager.isAppCountryFilterEnabled()) {
                createCountryShelf();
            } else {
                removeRow(mCountryShelfRow);
            }
            createAppShelfByCondition();
        }
    }

    private void createSingleAppShelf(List<AppInfo> enabledAppList) {
        DdbLogUtility.logAppsChapter(TAG, "createSingleAppShelf() called with: enabledAppList = [" + enabledAppList + "]");
        Drawable shelfIcon;
        shelfIcon = ContextCompat.getDrawable(getContext(), R.drawable.icon_1000_md_apps_n_48x48);
        if (enabledAppList != null && !enabledAppList.isEmpty()) {
            createAppShelf(getString(org.droidtv.ui.strings.R.string.MAIN_LAUNCHER_TITLE_APPS), shelfIcon, enabledAppList);
        }
    }

    private void createMultipleAppShelves(Map<String, List<AppInfo>> appListMap) {

        String shelfTitle = "";
        for (Map.Entry<String, List<AppInfo>> entry : appListMap.entrySet()) {
            try {
                shelfTitle = entry.getKey();
                List<AppInfo> appList = entry.getValue();
                Drawable shelfIcon = getContext().getDrawable(mShelfCategoryNameIconMap.get(shelfTitle));
                createAppShelf(shelfTitle, shelfIcon, appList);
            } catch (NullPointerException e) {
                if (shelfTitle == null) {
                    Log.d(TAG, "#### createMultipleAppShelves.could not create app shelf:null");
                } else {
                    Log.d(TAG, "#### createMultipleAppShelves.could not create app shelf:" + shelfTitle);
                }
            }
        }
        DdbLogUtility.logAppsChapter(TAG, "createMultipleAppShelves() called with: shelfTitle = [" + shelfTitle + "]");
    }

    private void createMyAppsRow(String shelfName, Drawable shelfIcon, List<AppInfo> userInstalledAppList){
        if (userInstalledAppList != null && !userInstalledAppList.isEmpty()) {
            createAppShelf(shelfName, shelfIcon, userInstalledAppList);
        }
    }

    private void cleanUp() {
        mDashboardDataManager.clearAppLogoCache();
    }

    private static final class AppsChapterPresenterSelector extends PresenterSelector {
        private AppsChapterCountryRowPresenter mAppsChapterCountryRowPresenter;
        private AppsShelfRowPresenter mAppsChapterShelfRowPresenter;
        private Presenter[] mPresenters;

        private AppsChapterPresenterSelector() {
            mAppsChapterCountryRowPresenter = new AppsChapterCountryRowPresenter(false);
            mAppsChapterCountryRowPresenter.setShadowEnabled(true);
            mAppsChapterCountryRowPresenter.setSelectEffectEnabled(false);
            mAppsChapterCountryRowPresenter.setKeepChildForeground(true);

            mAppsChapterShelfRowPresenter = new AppsShelfRowPresenter(false);
            mAppsChapterShelfRowPresenter.setShadowEnabled(true);
            mAppsChapterShelfRowPresenter.setSelectEffectEnabled(false);
            mAppsChapterShelfRowPresenter.setKeepChildForeground(true);

            mPresenters = new Presenter[]{mAppsChapterCountryRowPresenter, mAppsChapterShelfRowPresenter};
        }

        @Override
        public Presenter getPresenter(Object item) {
            if (COUNTRY_ROW_ID == ((ShelfRow) item).getId()) {
                return mAppsChapterCountryRowPresenter;
            }
            return mAppsChapterShelfRowPresenter;
        }

        @Override
        public Presenter[] getPresenters() {
            return mPresenters;
        }
    }

    private static class UiThreadHandler extends Handler {
        WeakReference<AppsChapterFragment> mAppsChapterFragmentRef;
        private static final int MSG_WHAT_COUNTRY_CHANGED = 100;
        private static final int MSG_WHAT_APP_CATEGORY_ENABLED_STATE_CHANGED = 101;
        private static final int MSG_WHAT_APP_COUNTRY_ENABLED_STATE_CHANGED = 102;

        private UiThreadHandler(AppsChapterFragment appsChapterFragment) {
            super();
            mAppsChapterFragmentRef = new WeakReference<AppsChapterFragment>(appsChapterFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Log.d(TAG, "handleMessage what = [" + msg.what + "]");
            if (what == MSG_WHAT_COUNTRY_CHANGED) {

                AppsChapterFragment appsChapterFragment = mAppsChapterFragmentRef.get();
                if (appsChapterFragment != null) {
                    int newCountryCode = msg.arg1;
                    appsChapterFragment.onCountryChanged(newCountryCode);
                }
                return;
            }

            if (what == MSG_WHAT_APP_CATEGORY_ENABLED_STATE_CHANGED) {
                AppsChapterFragment appsChapterFragment = mAppsChapterFragmentRef.get();
                if (appsChapterFragment != null) {
                    appsChapterFragment.appCategoryEnabledStateChanged();
                }
                return;
            }

            if (what == MSG_WHAT_APP_COUNTRY_ENABLED_STATE_CHANGED) {
                AppsChapterFragment appsChapterFragment = mAppsChapterFragmentRef.get();
                if (appsChapterFragment != null) {
                    appsChapterFragment.appCountryEnabledStateChanged();
                }
                return;
            }
        }
    }
}
