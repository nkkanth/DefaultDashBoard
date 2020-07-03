package org.droidtv.defaultdashboard.ui.fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.GamesRecommendationListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.GamesShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.ui.presenter.AppsShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.AppsShelfRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.GamesChapterShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.GamesChapterShelfRowPresenter;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.DDBImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import static org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppDataListener;
import static org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public class GamesChapterFragment extends ChapterFragment implements RecommendationListener, AppDataListener, GamesRecommendationListener, DashboardDataManager.PreviewProgramsListener {

    private Map<String, ArrayObjectAdapter> mGamePackageAdapterMap;
    private Map<String, ShelfRow> mGamePackageShelfRowMap;
    Map<String, PreviewProgramsChannel> mRecommendedPreviewGamesMap;
    private PackageManager mPackageManager;
    private List<AppInfo> mGameAppList;
    private DashboardDataManager mDashboardDataManager;
    private static final int GAMES_SHELF_ROW_ID = 1;
    private ShelfRow mGamesShelfRow;
    private static final String TAG = "GamesChapterFragment";

    public GamesChapterFragment() {
        super();
        mGamePackageAdapterMap = new HashMap<>();
        mGamePackageShelfRowMap = new HashMap<>();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mDashboardDataManager.addPreviewProgramsListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
        DDBImageLoader.setChapterVisibility(true);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        DDBImageLoader.setChapterVisibility(false);
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        registerRecommendationListener();
        registerAppDataListener();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");
        unregisterRecommendationListener();
        unregisterAppDataListener();
        mDashboardDataManager.removePreviewProgramsListener(this);
        cleanUp();
        super.onDestroyView();
        DdbLogUtility.logGamesChapter(TAG, "#### onDestroyView() exit");
    }

    @Override
    protected void createRows() {
        mPackageManager = getActivity().getApplicationContext().getPackageManager();
        if (mDashboardDataManager.areGameRecommendationsAvailable()) {
            createGameRecommendationShelves();
            DdbLogUtility.logGamesChapter(TAG, "createRows() called createGameRecommendationShelves");
        }
        if (mDashboardDataManager.areGameAppsAvailable()) {
            createGameAppShelf();
			DdbLogUtility.logGamesChapter(TAG, "createRows() called createGameAppShelf");
        }

        if(mDashboardDataManager.arePreviewChannelGamesAvailable()){
            createPreviewChannelGameShelves();
			DdbLogUtility.logGamesChapter(TAG, "createRows() called arePreviewChannelGamesAvailable");
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    private void createGameAppShelf() {
        mGamesShelfRow = buildGameAppShelf();
        addRow(getGameAppRowIndex(), mGamesShelfRow);
    }

    private void createPreviewChannelGameShelves(){
        mRecommendedPreviewGamesMap = mDashboardDataManager.getGamePreviewRecommendationMap();
        if(mRecommendedPreviewGamesMap == null || mRecommendedPreviewGamesMap.isEmpty()){
            android.util.Log.d(TAG, "createPreviewChannelGameShelves: Gaming preview channel not available");
            return;
        }
        android.util.Log.d(TAG, "createPreviewChannelGameShelves: recommendedPreviewGamesMap.size() " + mRecommendedPreviewGamesMap.size());

        for(String previewChannelId: mRecommendedPreviewGamesMap.keySet()){
            PreviewProgramsChannel previewProgramsChannel = mRecommendedPreviewGamesMap.get(previewChannelId);
            android.util.Log.d(TAG, "createPreviewChannelGameShelves: previewChannelId " + previewChannelId);
            List<Recommendation> recommendationList = previewProgramsChannel.getPreviewProgramList();
            createGameRecommendationShelf(previewProgramsChannel.getPackageName(), recommendationList, previewProgramsChannel);
        }
    }

    private int getGameAppRowIndex() {
        Map<String, List<Recommendation>> recommendationMap = mDashboardDataManager.getGameRecommendations();
        if (recommendationMap != null) {
            return recommendationMap.size();
        }
        return 0;
    }

    private ShelfRow buildGameAppShelf() {
        mGameAppList = mDashboardDataManager.getGameAppList();
        if (mGameAppList == null || mGameAppList.isEmpty()) {
            return null;
        }
        AppsShelfItemPresenter itemPresenter = new AppsShelfItemPresenter();
        itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
        ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
        shelfItemAdapter.addAll(0, mGameAppList);
        return new ShelfRow(GAMES_SHELF_ROW_ID, new ShelfHeaderItem(getContext().getString(org.droidtv.ui.strings.R.string.MAIN_GAMES), getContext().getDrawable(R.drawable.icon_91_game_n_48x48)), shelfItemAdapter);
    }

    private void createGameRecommendationShelves() {
        List<Recommendation> recommendationList;
        Map<String, List<Recommendation>> gameRecommendationsMap = mDashboardDataManager.getGameRecommendations();
        for (String packageName : gameRecommendationsMap.keySet()) {
            recommendationList = gameRecommendationsMap.get(packageName);
            createGameRecommendationShelf(packageName, recommendationList, null);
        }
    }

    private void createGameRecommendationShelf(String packageName, List<Recommendation> recommendations, PreviewProgramsChannel previewProgramChannel) {
        if (recommendations != null && !recommendations.isEmpty()) {
            String gameShelfAppName;
            Drawable gameShelfIcon;
            try {
                gameShelfAppName = (String) mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                gameShelfIcon = mPackageManager.getApplicationIcon(mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
               Log.e(TAG,"Exception :" +e.getMessage());
                gameShelfAppName = packageName;
                gameShelfIcon = getContext().getDrawable(R.mipmap.ic_launcher);
            }

            GamesChapterShelfItemPresenter itemPresenter = new GamesChapterShelfItemPresenter();
            itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
            shelfItemAdapter.addAll(0, recommendations);

            //Append open app tile for each row
            Recommendation openAppRecommendation = getOpenAppRecommendation(packageName);
            shelfItemAdapter.add(openAppRecommendation);
            ShelfRow shelfRow = null;
            if(previewProgramChannel != null){//Create Preview channel app shelf
                android.util.Log.d(TAG, "createGameRecommendationShelf: previewProgramChannelID " + previewProgramChannel.getId());
                shelfRow = new ShelfRow(new GamesShelfHeaderItem(gameShelfAppName, gameShelfIcon, previewProgramChannel.getDisplayName()), shelfItemAdapter);
                mGamePackageAdapterMap.put(Integer.toString(previewProgramChannel.getId()), shelfItemAdapter);
                mGamePackageShelfRowMap.put(Integer.toString(previewProgramChannel.getId()), shelfRow);
                android.util.Log.d(TAG, "createGameRecommendationShelf: mGamePackageShelfRowMap.size() " + mGamePackageShelfRowMap.size());

            }else {//Create recommended app shelf through recommended service notification
                shelfRow = new ShelfRow(new GamesShelfHeaderItem(gameShelfAppName, gameShelfIcon, gameShelfAppName), shelfItemAdapter);
                mGamePackageAdapterMap.put(packageName, shelfItemAdapter);
                mGamePackageShelfRowMap.put(packageName, shelfRow);
            }
            addRow(getNextGameRecommendationShelfPosition(), shelfRow);

        }
    }

    private int getNextGameRecommendationShelfPosition() {
        return mGamePackageShelfRowMap.size()-1;//TODO: Utam: remove -1 its added only for testing
    }

    private Recommendation getOpenAppRecommendation(String packageName) {
        Intent openAppLaunchIntent = mPackageManager.getLeanbackLaunchIntentForPackage(packageName);
        if (openAppLaunchIntent == null) {
            openAppLaunchIntent = mPackageManager.getLaunchIntentForPackage(packageName);
        }

        PendingIntent openAppPendingIntent = PendingIntent.getActivity(getContext(), 0, openAppLaunchIntent, 0);
        String[] contentTypes = {Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION};
        String gameAppTitle = "Game App";
        String gameAppDescription = "Game App";

        Recommendation openAppRecommendation = new Recommendation();
        openAppRecommendation.setContentType(contentTypes);
        openAppRecommendation.setPendingIntent(openAppPendingIntent);

        try {
            CharSequence title = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            if (title != null) {
                gameAppTitle = title.toString();
            }
            CharSequence description = mPackageManager.getApplicationInfo(packageName, 0).loadDescription(mPackageManager);
            if (description != null) {
                gameAppDescription = description.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        openAppRecommendation.setTitle(gameAppTitle);
        openAppRecommendation.setDescription(gameAppDescription);

        return openAppRecommendation;
    }

    @Override
    protected PresenterSelector getChapterPresenterSelector() {
        return new GamesChapterPresenterSelector();
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (row.getId() == GAMES_SHELF_ROW_ID) {
            if (item instanceof AppInfo) {
                onAppClicked((AppInfo) item);
            }
        } else {
            if (item instanceof Recommendation) {
                onRecommendationClicked((Recommendation) item);
            }
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        if (isGameRecommendation(recommendation)) {
            onGameRecommendationChanged(recommendation, recommendationChangeType);
        }
    }

    private void onGameRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        String packageName = recommendation.getPendingIntent().getCreatorPackage();

        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            if (mGamePackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mGamePackageAdapterMap.get(packageName);
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
                createGameRecommendationShelf(packageName, recommendations, null);
            }
            return;
        }

        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            if (mGamePackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mGamePackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.remove(recommendation);
                    }
                }

                if (adapter.size() == 1) { //open app tile is considered so size will be 1
                    mGamePackageAdapterMap.remove(packageName);
                    ShelfRow shelfRow = mGamePackageShelfRowMap.remove(packageName);
                    removeRow(shelfRow);
                }

                return;
            }
        }

        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            if (mGamePackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mGamePackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.replace(i, recommendation);
                        return;
                    }
                }
            }

            if (mGamePackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mGamePackageAdapterMap.get(packageName);
                adapter.add(0, recommendation);
            } else {
                List<Recommendation> recommendations = new ArrayList<>();
                recommendations.add(recommendation);
                createGameRecommendationShelf(packageName, recommendations, null);
            }
            return;
        }
    }

    private void updateGameRecommendationShelves() {
        // Create Game recommendation shelves if currently there are none and if recommendations are available
        if (mGamePackageShelfRowMap.isEmpty()) {
            if (mDashboardDataManager.areGameRecommendationsAvailable()) {
                createGameRecommendationShelves();
            }
            return;
        }

        // Remove all Game recommendation shelves if no recommendation is available
        if (!mDashboardDataManager.areGameRecommendationsAvailable() && (!mDashboardDataManager.arePreviewChannelGamesAvailable())) {
            ArrayList<String> gameRecommendationAppPackages = new ArrayList<>(mGamePackageShelfRowMap.keySet());
            int gameRecommendationAppPackagesCount = gameRecommendationAppPackages.size();
            for (int i = 0; i < gameRecommendationAppPackagesCount; i++) {
                String gameRecommendationAppPackage = gameRecommendationAppPackages.get(i);
                ShelfRow row = mGamePackageShelfRowMap.remove(gameRecommendationAppPackage);
                mGamePackageAdapterMap.remove(gameRecommendationAppPackage);
                removeRow(row);
            }
            return;
        }

        // Create individual recommendation shelves if any recommendation-enabled app does not yet have a shelf
        Map<String, List<Recommendation>> gameRecommendationsMap = mDashboardDataManager.getGameRecommendations();
        if (gameRecommendationsMap != null) {
            ArrayList<String> gameRecommendationAppPackages = new ArrayList<>(gameRecommendationsMap.keySet());
            int gameRecommendationAppPackagesCount = gameRecommendationAppPackages.size();
            for (int i = 0; i < gameRecommendationAppPackagesCount; i++) {
                String gameRecommendationAppPackage = gameRecommendationAppPackages.get(i);
                if (!mGamePackageShelfRowMap.containsKey(gameRecommendationAppPackage)) {
                    createGameRecommendationShelf(gameRecommendationAppPackage, gameRecommendationsMap.get(gameRecommendationAppPackage), null);
                }
            }
        }

        // Remove recommendation shelf for the package that is not in recommendation-enabled app list
        ArrayList<String> currentShownPackages = new ArrayList<>(mGamePackageShelfRowMap.keySet());
        int currentShownPackagesCount = currentShownPackages.size();
        for (int i = 0; i < currentShownPackagesCount; i++) {
            String pkg = currentShownPackages.get(i);
            if ((null != gameRecommendationsMap) && !gameRecommendationsMap.containsKey(pkg)) {
                ShelfRow row = mGamePackageShelfRowMap.remove(pkg);
                mGamePackageAdapterMap.remove(pkg);
                removeRow(row);
            }
        }

        createPreviewChannelGameShelves();
    }

    private boolean isGameRecommendation(Recommendation recommendation) {
        boolean gameRecommendation = false;

        if (recommendation == null) {
            return gameRecommendation;
        }

        String[] contentTypes = recommendation.getContentType();
        if (contentTypes == null) {
            return gameRecommendation;
        }

        for (String contentType : contentTypes) {
            if (contentType.equals(Constants.CONTENT_TYPE_GAMING_RECOMMENDATION)) {
                gameRecommendation = true;
                break;
            }
        }

        return gameRecommendation;
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        if (recommendationCategory == RecommendationHelper.Category.GAMES) {
            createGameRecommendationShelves();
        }
    }

    @Override
    public void onGamesRecommendationUnavailable() {
        // Do nothing
    }

    @Override
    public void onGamesRecommendationAvailable() {
        // Do nothing
    }

    @Override
    public void onGameRecommendationAppsUpdated() {
        updateGameRecommendationShelves();
    }

    @Override
    public void onAppListFetched() {
        ShelfRow row = buildGameAppShelf();
        if (row != null) {
            replaceGamesShelfRow(row);
        } else {
            removeGamesShelfRow();
        }
    }

    private boolean replaceGamesShelfRow(ShelfRow row) {
        removeRow(mGamesShelfRow);
        mGamesShelfRow = row;
        addRow(mGamesShelfRow);
        return true;
    }

    private void removeGamesShelfRow() {
        removeRow(mGamesShelfRow);
        mGamesShelfRow = null;
    }

    private class GamesChapterPresenterSelector extends PresenterSelector {

        private GamesChapterShelfRowPresenter mGamesChapterShelfRowPresenter;
        private AppsShelfRowPresenter mAppsShelfRowPresenter;
        private Presenter[] mPresenters;

        GamesChapterPresenterSelector() {
            mGamesChapterShelfRowPresenter = new GamesChapterShelfRowPresenter(false);
            mGamesChapterShelfRowPresenter.setShadowEnabled(true);
            mGamesChapterShelfRowPresenter.setSelectEffectEnabled(false);
            mGamesChapterShelfRowPresenter.setKeepChildForeground(true);

            mAppsShelfRowPresenter = new AppsShelfRowPresenter(false);
            mAppsShelfRowPresenter.setShadowEnabled(true);
            mAppsShelfRowPresenter.setSelectEffectEnabled(false);
            mAppsShelfRowPresenter.setKeepChildForeground(true);

            mPresenters = new Presenter[]{
                    mGamesChapterShelfRowPresenter,
                    mAppsShelfRowPresenter
            };
        }

        @Override
        public Presenter[] getPresenters() {
            return mPresenters;
        }

        @Override
        public Presenter getPresenter(Object item) {
            ShelfRow row = (ShelfRow) item;
            if (row.getId() == GAMES_SHELF_ROW_ID) {
                return mAppsShelfRowPresenter;
            } else {
                return mGamesChapterShelfRowPresenter;
            }
        }
    }

    private void registerRecommendationListener() {
        mDashboardDataManager.addRecommendationListener(this);
    }

    private void unregisterRecommendationListener() {
        mDashboardDataManager.removeRecommendationListener(this);
    }

    private void unregisterAppDataListener() {
        mDashboardDataManager.removeAppDataListener(this);
    }

    private void registerAppDataListener() {
        mDashboardDataManager.addAppDataListener(this);
    }


    private void cleanUp() {
        mDashboardDataManager.clearAppLogoCache();
    }

    @Override
    public void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannel) {
       if(isGamesPreviewChannel(previewProgramsChannel)) {
            updateGameRecommendationShelves();
        }else {
            android.util.Log.d(TAG, "onPreviewProgramChannelsAvailable: not a VOD preview channel " + previewProgramsChannel.getCategory());
        }
    }

    @Override
    public void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannel) {
        android.util.Log.d(TAG, "onPreviewProgramChannelsAvailable() contentType " + previewProgramsChannel.getCategory());
        if(isGamesPreviewChannel(previewProgramsChannel)) {
            updateGameRecommendationShelves();
        }else{
            android.util.Log.d(TAG, "onPreviewProgramChannelsChanged: not a VOD preview channel " + previewProgramsChannel.getCategory());
        }
    }

    @Override
    public void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels) {

    }

    private boolean isGamesPreviewChannel(PreviewProgramsChannel channel){
        return channel != null && channel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION;
    }
}
