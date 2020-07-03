package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.util.Log;

import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.VideoOnDemandRecommendationListener;
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
 * Created by bhargava.gugamsetty on 20-02-2018.
 */

final class VideoOnDemandDataManager extends ContextualObject implements RecommendationListener, AppDataListener, DashboardDataManager.PreviewProgramsListener {

    private static String TAG = "VideoOnDemandDataManager";
    private DashboardDataManager mDashboardDataManager;
    private Map<String, List<Recommendation>> mVodRecommendationMap;
    private Map<String, PreviewProgramsChannel> mVodPreviewRecommendationMap;

    private ArrayList<WeakReference<VideoOnDemandRecommendationListener>> mVideoOnDemandRecommendationListenerRefs;

    protected VideoOnDemandDataManager(Context context) {
        super(context);
        DdbLogUtility.logVodChapter(TAG, "VideoOnDemandDataManager Constructor");
        mDashboardDataManager = DashboardDataManager.getInstance();
        mVideoOnDemandRecommendationListenerRefs = new ArrayList<>();
        registerForVodRecommendationChanges();
        registerAppDataListener();
        registerForVodPreviewProgramChanges();
        fetchVodPreviewRecommendations();
    }

    private void fetchVodPreviewRecommendations(){
        mDashboardDataManager.fetchPreviewProgramsVODChannels();
    }

    private void registerForVodRecommendationChanges() {
        mDashboardDataManager.addRecommendationListener(this);
    }

    private void registerForVodPreviewProgramChanges(){
        mDashboardDataManager.addPreviewProgramsListener(this);
    }

    private void registerAppDataListener() {
        mDashboardDataManager.addAppDataListener(this);
    }

    boolean areVodRecommendationsAvailable() {
       return mVodRecommendationMap != null && !mVodRecommendationMap.isEmpty() && isAnyAppEnabledForVodRecommendation();
    }

    boolean areVodPreviewRecommendationsAvailable() {
        return mVodPreviewRecommendationMap != null && !mVodPreviewRecommendationMap.isEmpty() /*&& isAnyAppEnabledForVodPreviewRecommendation()*/;
    }


    boolean addVideoOnDemandRecommendationListener(VideoOnDemandRecommendationListener videoOnDemandRecommendationListener) {
        if (videoOnDemandRecommendationListener == null) {
            return false;
        }
        return mVideoOnDemandRecommendationListenerRefs.add(new WeakReference<VideoOnDemandRecommendationListener>(videoOnDemandRecommendationListener));
    }

    boolean removeVideoOnDemandRecommendationListener(VideoOnDemandRecommendationListener videoOnDemandRecommendationListener) {
        if (mVideoOnDemandRecommendationListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mVideoOnDemandRecommendationListenerRefs.size(); i++) {
            WeakReference<VideoOnDemandRecommendationListener> ref = mVideoOnDemandRecommendationListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            VideoOnDemandRecommendationListener listener = ref.get();
            if (listener != null && listener.equals(videoOnDemandRecommendationListener)) {
                return mVideoOnDemandRecommendationListenerRefs.remove(ref);
            }
        }
        return false;
    }

    void buildVodRecommendationMap() {
        mVodRecommendationMap = new HashMap<>();

        List<Recommendation> vodRecommendations = mDashboardDataManager.getRecommendations(RecommendationHelper.Category.VOD);
        for (int i = 0; vodRecommendations != null && i < vodRecommendations.size(); i++) {
            Recommendation recommendation = vodRecommendations.get(i);
            String vodPackageName = recommendation.getPendingIntent().getCreatorPackage();

            if (!mDashboardDataManager.isAppRecommendationEnabled(vodPackageName)) {
                continue;
            }

            List<Recommendation> recommendations = mVodRecommendationMap.get(vodPackageName);
            if (recommendations == null) {
                recommendations = new ArrayList<>();
                mVodRecommendationMap.put(vodPackageName, recommendations);
            }
            recommendations.add(recommendation);
        }
        android.util.Log.d(TAG, "buildVodRecommendationMap: mVodRecommendationMap.size() " + mVodRecommendationMap.size());
    }

    private void buildPreviewVodRecommendationMap(PreviewProgramsChannel previewProgramsChannel){
        if(previewProgramsChannel == null) return;
        if(null == mVodPreviewRecommendationMap) mVodPreviewRecommendationMap = new HashMap<>();
        String vodPackageName = previewProgramsChannel.getPackageName();
        boolean isAppRecommendationEnabled = mDashboardDataManager.isAppRecommendationEnabled(vodPackageName);
        if (isAppRecommendationEnabled) {
            mVodPreviewRecommendationMap.put(Integer.toString(previewProgramsChannel.getId()), previewProgramsChannel);
        }
        android.util.Log.d(TAG, "buildPreviewVodRecommendationMap: isAppRecommendationEnabled " + isAppRecommendationEnabled + "size " + mVodPreviewRecommendationMap.size());
    }

    Map<String, List<Recommendation>> getVodRecommendations() {
        return mVodRecommendationMap;
    }


    Map<String, PreviewProgramsChannel> getVodPreviewProgramRecommendations() {
        return mVodPreviewRecommendationMap;
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {

        if (!isVodRecommendation(recommendation)) {
            return;
        }

        List<Recommendation> recommendationList;
        if (recommendation.getPendingIntent() == null) {
            return;
        }

        String packageName = recommendation.getPendingIntent().getCreatorPackage();
        DdbLogUtility.logVodChapter(TAG, "onRecommendationChanged " + recommendation.toString());
        // If this is a new recommendation, simply add it to the list to the respective package name in the map
        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            if (mVodRecommendationMap.containsKey(packageName)) {
                recommendationList = mVodRecommendationMap.get(packageName);
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
                mVodRecommendationMap.put(packageName, recommendationList);
            }
            if (mVodRecommendationMap.size() == 1 && recommendationList.size() == 1 && mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                // A single new recommendation has been added. Notify listeners that atleast one recommendation is available
                // But notify only if this package name is in the recommendation-enabled list of apps
                notifyVideoOnDemandRecommendationsAvailable();
            }
            return;
        }
        // For recommendations to be cancelled, simply remove the recommendation from the list
        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            if (mVodRecommendationMap.containsKey(packageName)) {
                recommendationList = mVodRecommendationMap.get(packageName);

                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        break;
                    }
                }
                if (recommendationList.isEmpty()) {
                    mVodRecommendationMap.remove(packageName);

                    if (!areVodRecommendationsAvailable()) {
                        // No recommendations are there anymore. Notify listeners that no recommendations are available
                        notifyVideoOnDemandRecommendationsUnavailable();
                    }
                }
                return;
            }
        }

        // Update if there is an existing recommendation with the same id. Otherwise, add this recommendation to the list
        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            if (mVodRecommendationMap.containsKey(packageName)) {
                recommendationList = mVodRecommendationMap.get(packageName);
                for (int i = 0; i < recommendationList.size(); i++) {
                    Recommendation recommendationItem = recommendationList.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        recommendationList.remove(i);
                        recommendationList.add(i, recommendation);
                        return;
                    }
                }
            }

            if (mVodRecommendationMap.containsKey(packageName)) {
                recommendationList = mVodRecommendationMap.get(packageName);
                recommendationList.add(recommendation);
            } else {
                recommendationList = new ArrayList<>();
                recommendationList.add(recommendation);
                mVodRecommendationMap.put(packageName, recommendationList);
            }
            if (mVodRecommendationMap.size() == 1 && recommendationList.size() == 1 && mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                // A single new recommendation has been added. Notify listeners that atleast one recommendation is available
                // But notify only if this package name is in the recommendation-enabled list of apps
                notifyVideoOnDemandRecommendationsAvailable();
            }
            return;
        }
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        if (recommendationCategory == RecommendationHelper.Category.VOD) {
            buildVodRecommendationMap();
            if (areVodRecommendationsAvailable()) {
                notifyVideoOnDemandRecommendationsAvailable();
            }
        }
    }

    @Override
    public void onAppListFetched() {
        buildVodRecommendationMap();
        notifyVideoOnDemandAppsUpdated();
    }

    private boolean isVodRecommendation(Recommendation recommendation) {
        boolean vodRecommendation = false;

        if (recommendation == null) {
            return vodRecommendation;
        }

        String[] contentTypes = recommendation.getContentType();
        if (contentTypes == null) {
            return vodRecommendation;
        }

        for (String contentType : contentTypes) {
            if (contentType.equals(Constants.CONTENT_TYPE_VOD_RECOMMENDATION)) {
                vodRecommendation = true;
                break;
            }
        }
        DdbLogUtility.logVodChapter(TAG, "isVodRecommendation " + vodRecommendation);
        return vodRecommendation;
    }

    private boolean isAnyAppEnabledForVodRecommendation() {
        for (String packageName : mVodRecommendationMap.keySet()) {
            if (mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                Log.d(TAG, "isAnyAppEnabledForVodRecommendation: true packageName " + packageName);
                return true;
            }
        }
        return false;
    }

    private boolean isAnyAppEnabledForVodPreviewRecommendation() {
        for (String packageName : mVodPreviewRecommendationMap.keySet()) {
            if (mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void notifyVideoOnDemandRecommendationsAvailable() {
        for (int i = 0; mVideoOnDemandRecommendationListenerRefs != null && i < mVideoOnDemandRecommendationListenerRefs.size(); i++) {
            WeakReference<VideoOnDemandRecommendationListener> listenerRef = mVideoOnDemandRecommendationListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            VideoOnDemandRecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onVideoOnDemandRecommendationAvailable();
            }
        }
    }

    private void notifyVideoOnDemandRecommendationsUnavailable() {
        for (int i = 0; mVideoOnDemandRecommendationListenerRefs != null && i < mVideoOnDemandRecommendationListenerRefs.size(); i++) {
            WeakReference<VideoOnDemandRecommendationListener> listenerRef = mVideoOnDemandRecommendationListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            VideoOnDemandRecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onVideoOnDemandRecommendationUnavailable();
            }
        }
    }

    private void notifyVideoOnDemandAppsUpdated() {
        for (int i = 0; mVideoOnDemandRecommendationListenerRefs != null && i < mVideoOnDemandRecommendationListenerRefs.size(); i++) {
            WeakReference<VideoOnDemandRecommendationListener> listenerRef = mVideoOnDemandRecommendationListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            VideoOnDemandRecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onVideoOnDemandRecommendationAppsUpdated();
            }
        }
    }

    @Override
    public void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannel) {
      
      if(previewProgramsChannel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION) {
        buildPreviewVodRecommendationMap(previewProgramsChannel);
      }else{
        android.util.Log.d(TAG, "onPreviewProgramChannelsAvailable: not a VOD recommendation " + previewProgramsChannel.getCategory());
      }
    }

    @Override
    public void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannel) {
      if(previewProgramsChannel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION) {
          buildPreviewVodRecommendationMap(previewProgramsChannel);
      }else{
        android.util.Log.d(TAG, "onPreviewProgramChannelsChanged: not a VOD recommendation " + previewProgramsChannel.getCategory());
      }
    }

    @Override
    public void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels) {

    }
}
