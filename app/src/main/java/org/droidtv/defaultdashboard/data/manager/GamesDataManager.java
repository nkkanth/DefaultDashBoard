package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.util.Log;

import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.GamesRecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.util.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bhargava.gugamsetty on 24-04-2018.
 */

final class GamesDataManager extends ContextualObject implements RecommendationListener, AppDataListener, DashboardDataManager.PreviewProgramsListener {

    private static String TAG = "GamesDataManager";
    private DashboardDataManager mDashboardDataManager;
    private Map<String, List<Recommendation>> mGameRecommendationMap;
    private Map<String, PreviewProgramsChannel> mGamePreviewRecommendationMap;
    private ArrayList<WeakReference<GamesRecommendationListener>> mGamesRecommendationListenerRefs;

    protected GamesDataManager(Context context) {
        super(context);
        mDashboardDataManager = DashboardDataManager.getInstance();
        mGamesRecommendationListenerRefs = new ArrayList<>();
        registerForGameRecommendationChanges();
        registerForGamesPreviewProgramChanges();
        registerAppDataListener();
    }

    private void registerForGameRecommendationChanges() {
        mDashboardDataManager.addRecommendationListener(this);
    }

    private void registerForGamesPreviewProgramChanges(){
        mDashboardDataManager.addPreviewProgramsListener(this);
    }

    private void registerAppDataListener() {
        mDashboardDataManager.addAppDataListener(this);
    }

    boolean areGameRecommendationsAvailable() {
        return mGameRecommendationMap != null && !mGameRecommendationMap.isEmpty() && isAnyAppEnabledForGameRecommendation();
    }

    boolean addGamesRecommendationListener(GamesRecommendationListener gamesRecommendationListener) {
        if (gamesRecommendationListener == null) {
            return false;
        }
        return mGamesRecommendationListenerRefs.add(new WeakReference<GamesRecommendationListener>(gamesRecommendationListener));
    }

    boolean removeGamesRecommendationListener(GamesRecommendationListener gamesRecommendationListener) {
        if (mGamesRecommendationListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mGamesRecommendationListenerRefs.size(); i++) {
            WeakReference<GamesRecommendationListener> ref = mGamesRecommendationListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            GamesRecommendationListener listener = ref.get();
            if (listener != null && listener.equals(gamesRecommendationListener)) {
                return mGamesRecommendationListenerRefs.remove(ref);
            }
        }
        return false;
    }

    void buildGameRecommendationMap() {
        mGameRecommendationMap = new HashMap<>();

        List<Recommendation> gameRecommendations = mDashboardDataManager.getRecommendations(RecommendationHelper.Category.GAMES);
        for (int i = 0; gameRecommendations != null && i < gameRecommendations.size(); i++) {
            Recommendation recommendation = gameRecommendations.get(i);
            String gamePackageName = recommendation.getPendingIntent().getCreatorPackage();

            if (!mDashboardDataManager.isAppRecommendationEnabled(gamePackageName)) {
                continue;
            }

            List<Recommendation> recommendations = mGameRecommendationMap.get(gamePackageName);
            if (recommendations == null) {
                recommendations = new ArrayList<>();
                mGameRecommendationMap.put(gamePackageName, recommendations);
            }
            recommendations.add(recommendation);
        }
    }


    void buildGamesPreviewRecommendedMap(PreviewProgramsChannel previewProgramsChannel){
        if(mGamePreviewRecommendationMap == null) mGamePreviewRecommendationMap = new HashMap<>();

        String packageName = previewProgramsChannel.getPackageName();
        if ("org.droidtv.nettvadvert".equals(packageName)) {
            android.util.Log.d(TAG, "addToGamesPreviewProgramsChannelsList: nettvadvert");
            return;
        }

        if (mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
            List<Recommendation> recommendations = previewProgramsChannel.getPreviewProgramList();
            String previewChannelId = Integer.toString(previewProgramsChannel.getId());
            mGamePreviewRecommendationMap.put(previewChannelId, previewProgramsChannel);
        }else{
            android.util.Log.d(TAG, "buildGamesPreviewRecommendedMap: isAppRecommendationEnabled false");
        }
    }

    Map<String, List<Recommendation>> getGameRecommendations() {
        return mGameRecommendationMap;
    }

    Map<String, PreviewProgramsChannel> getGamePreviewRecommendationMap() { return mGamePreviewRecommendationMap; }

    public boolean arePreviewChannelGamesAvailable(){
        return mGamePreviewRecommendationMap != null && (!mGamePreviewRecommendationMap.isEmpty());
    }

    List<AppInfo> getGameAppList() {
        return mDashboardDataManager.getAppsByCategory(Constants.APP_CATEGORY_GAME);
    }

    boolean areGameAppsAvailable() {
        List<AppInfo> gameApps = getGameAppList();
        return gameApps != null && !gameApps.isEmpty();
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        DdbLogUtility.logCommon(TAG, "onRecommendationChanged() called with: recommendationChangeType = [" + recommendationChangeType + "]");
        if (!isGameRecommendation(recommendation)) {
            return;
        }

        List<Recommendation> recommendationList;
        if (recommendation.getPendingIntent() == null) {
            return;
        }

        String packageName = recommendation.getPendingIntent().getCreatorPackage();

        // If this is a new recommendation, simply add it to the list to the respective package name in the map
        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            if (mGameRecommendationMap.containsKey(packageName)) {
                recommendationList = mGameRecommendationMap.get(packageName);
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
                mGameRecommendationMap.put(packageName, recommendationList);
            }
            if (mGameRecommendationMap.size() == 1 && recommendationList.size() == 1 && mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                // A single new recommendation has been added. Notify listeners that atleast one recommendation is available
                // But notify only if this package name is in the recommendation-enabled list of apps
                notifyGameRecommendationsAvailable();
            }
            return;
        }
        // For recommendations to be cancelled, simply remove the recommendation from the list
        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            if (mGameRecommendationMap.containsKey(packageName)) {
                recommendationList = mGameRecommendationMap.get(packageName);

                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        break;
                    }
                }
                if (recommendationList.isEmpty()) {
                    mGameRecommendationMap.remove(packageName);

                    if (!areGameRecommendationsAvailable()) {
                        // No recommendations are there anymore. Notify listeners that no recommendations are available
                        notifyGameRecommendationsUnavailable();
                    }
                }
                return;
            }
        }

        // Update if there is an existing recommendation with the same id. Otherwise, add this recommendation to the list
        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            if (mGameRecommendationMap.containsKey(packageName)) {
                recommendationList = mGameRecommendationMap.get(packageName);
                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        recommendationList.add(i, recommendation);
                        return;
                    }
                }
            }

            if (mGameRecommendationMap.containsKey(packageName)) {
                recommendationList = mGameRecommendationMap.get(packageName);
                recommendationList.add(recommendation);
            } else {
                recommendationList = new ArrayList<>();
                recommendationList.add(recommendation);
                mGameRecommendationMap.put(packageName, recommendationList);
            }
            if (mGameRecommendationMap.size() == 1 && recommendationList.size() == 1 && mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                // A single new recommendation has been added. Notify listeners that atleast one recommendation is available
                // But notify only if this package name is in the recommendation-enabled list of apps
                notifyGameRecommendationsAvailable();
            }
            return;
        }
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        DdbLogUtility.logCommon(TAG, "onRecommendationsAvailable() called with: recommendationCategory = [" + recommendationCategory + "]");
        if (recommendationCategory == RecommendationHelper.Category.GAMES) {
            buildGameRecommendationMap();
            if (areGameRecommendationsAvailable()) {
                notifyGameRecommendationsAvailable();
            }
        }
    }

    @Override
    public void onAppListFetched() {
        buildGameRecommendationMap();
        notifyGameAppsUpdated();
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
        DdbLogUtility.logCommon(TAG, "isGameRecommendation " + gameRecommendation);
        return gameRecommendation;
    }

    private boolean isAnyAppEnabledForGameRecommendation() {
        for (String packageName : mGameRecommendationMap.keySet()) {
            if (mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                Log.d(TAG, "isAnyAppEnabledForGameRecommendation: returns true packageName " + packageName);
                return true;
            }
        }
        return false;
    }

    private void notifyGameRecommendationsAvailable() {
        for (int i = 0; mGamesRecommendationListenerRefs != null && i < mGamesRecommendationListenerRefs.size(); i++) {
            WeakReference<GamesRecommendationListener> listenerRef = mGamesRecommendationListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            GamesRecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onGamesRecommendationAvailable();
            }
        }
    }

    private void notifyGameRecommendationsUnavailable() {
        for (int i = 0; mGamesRecommendationListenerRefs != null && i < mGamesRecommendationListenerRefs.size(); i++) {
            WeakReference<GamesRecommendationListener> listenerRef = mGamesRecommendationListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            GamesRecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onGamesRecommendationUnavailable();
            }
        }
    }

    private void notifyGameAppsUpdated() {
        for (int i = 0; mGamesRecommendationListenerRefs != null && i < mGamesRecommendationListenerRefs.size(); i++) {
            WeakReference<GamesRecommendationListener> listenerRef = mGamesRecommendationListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            GamesRecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onGameRecommendationAppsUpdated();
            }
        }
    }

    @Override
    public void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannel) {
        if(previewProgramsChannel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION) {
            buildGamesPreviewRecommendedMap(previewProgramsChannel);
        }
    }

    @Override
    public void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannel) {
        if(previewProgramsChannel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION) {
            buildGamesPreviewRecommendedMap(previewProgramsChannel);
        }
    }

    @Override
    public void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels) {

    }
}
