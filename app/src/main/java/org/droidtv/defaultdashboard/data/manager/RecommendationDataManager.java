package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListenerServiceClient;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.recommended.RecommendationListenerService;
import org.droidtv.defaultdashboard.recommended.RecommendationListenerService.NotifyRecommendation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by sandeep.kumar on 06/02/2018.
 */

final class RecommendationDataManager extends ContextualObject implements NotifyRecommendation, RecommendationListenerServiceClient {

    private static String TAG = "RecommendationDataManager";

    private ArrayList<WeakReference<RecommendationListener>> mRecommendationChangeListenerRefs;
    private WeakReference<RecommendationListenerService.RecommendationServiceAdapter> mRecommendationServiceAdapterRef;
    private boolean IsRegistered = false;
	
    RecommendationDataManager(Context context) {
        super(context);
        mRecommendationChangeListenerRefs = new ArrayList<>();
        DashboardDataManager.getInstance().addRecommendationListenerServiceClient(this);
        startRecommendationListenerService();
    }

    private void startRecommendationListenerService() {
        DdbLogUtility.logRecommendationChapter(TAG, "startRecommendationListenerService() called");
        Intent recommendationListenerServiceIntent = new Intent(getContext(), RecommendationListenerService.class);
        getContext().startServiceAsUser(recommendationListenerServiceIntent, UserHandle.CURRENT_OR_SELF);
        DdbLogUtility.logRecommendationChapter(TAG, "startRecommendationListenerService() called end");
    }

    @Override
    public void onRecommendationListenerServiceAvailable(WeakReference<RecommendationListenerService.RecommendationServiceAdapter> adapterRef) {
        mRecommendationServiceAdapterRef = adapterRef;
        if (mRecommendationServiceAdapterRef == null) {
            return;
        }

        RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
        if (recommendationServiceAdapter != null && !IsRegistered) {

            recommendationServiceAdapter.registerRecommendationServiceAdapterCallback(RecommendationDataManager.this);
            IsRegistered = true;
            // After registering callback with RecommendationListenerService, we need a way to notify
            // our listeners that there may be existing recommendations. We shall do it by iterating through
            // each recommendation and calling onRecommendationChanged()
            notifyIfRecommendationsAvailable(RecommendationHelper.Category.SMARTINFO);
            notifyIfRecommendationsAvailable(RecommendationHelper.Category.APPS);
            notifyIfRecommendationsAvailable(RecommendationHelper.Category.GAMES);
            notifyIfRecommendationsAvailable(RecommendationHelper.Category.VOD);
        }
    }

    @Override
    public void onRecommendationListenerServiceUnavailable() {
		IsRegistered = false;
        mRecommendationServiceAdapterRef = null;
    }

    boolean addRecommendationListener(RecommendationListener listener) {
        if (listener == null) {
            return false;
        }

        boolean added = mRecommendationChangeListenerRefs.add(new WeakReference<RecommendationListener>(listener));

        // After registering for recommendation changes, we need a way to notify the new listener
        // that there may be existing recommendations. We shall do it by iterating through
        // each recommendation and calling onRecommendationChanged()
        notifyIfRecommendationsAvailable(RecommendationHelper.Category.SMARTINFO, listener);
        notifyIfRecommendationsAvailable(RecommendationHelper.Category.APPS, listener);
        notifyIfRecommendationsAvailable(RecommendationHelper.Category.GAMES, listener);
        notifyIfRecommendationsAvailable(RecommendationHelper.Category.VOD, listener);

        return added;
    }

    boolean removeRecommendationListener(RecommendationListener recommendationListener) {
        if (mRecommendationChangeListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mRecommendationChangeListenerRefs.size(); i++) {
            WeakReference<RecommendationListener> ref = mRecommendationChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            RecommendationListener listener = ref.get();
            if (listener != null && listener.equals(recommendationListener)) {
                return mRecommendationChangeListenerRefs.remove(ref);
            }
        }
        return false;
    }

    List<Recommendation> getRecommendations(int category) {
        DdbLogUtility.logRecommendationChapter(TAG, "getRecommendations() called with: category = [" + category + "]");
        if (mRecommendationServiceAdapterRef == null) {
            return null;
        }

        RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
        if (recommendationServiceAdapter != null) {
            return recommendationServiceAdapter.getRecommendations(category);
        }
        return null;
    }

    List<Recommendation> getRecommendations(int category, String packageName) {
        DdbLogUtility.logRecommendationChapter(TAG, "getRecommendations() called with: category = [" + category + "], packageName = [" + packageName + "]");
        if (mRecommendationServiceAdapterRef == null) {
            return null;
        }

        RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
        if (recommendationServiceAdapter != null) {
            List<Recommendation> categoryRecommendations = recommendationServiceAdapter.getRecommendations(category);
            List<Recommendation> packageRecommendations = new ArrayList<>();
            for (int i = 0; categoryRecommendations != null && i < categoryRecommendations.size(); i++) {
                Recommendation recommendation = categoryRecommendations.get(i);
                if (recommendation.getPendingIntent().getCreatorPackage().equals(packageName)) {
                    packageRecommendations.add(recommendation);
                }
            }
            return packageRecommendations;
        }

        return null;
    }

    List<Recommendation> getRecommendations(String packageName) {
        DdbLogUtility.logRecommendationChapter(TAG, "getRecommendations() called with: packageName = [" + packageName + "]");
        if (mRecommendationServiceAdapterRef == null) {
            return null;
        }

        RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
        if (recommendationServiceAdapter != null) {
            List<Recommendation> allRecommendations = recommendationServiceAdapter.getAllNotificationRecommendations();
            List<Recommendation> packageRecommendations = new ArrayList<>();
            for (int i = 0; allRecommendations != null && i < allRecommendations.size(); i++) {
                Recommendation recommendation = allRecommendations.get(i);
                if (recommendation.getPendingIntent().getCreatorPackage().equals(packageName)) {
                    packageRecommendations.add(recommendation);
                }
            }
            return packageRecommendations;
        }

        return null;
    }

    List<Recommendation> getAllRecommendations() {
        DdbLogUtility.logRecommendationChapter("DashboardDataManager", "getAllRecommendations() called");
        List<Recommendation> allRecommendations = new ArrayList<Recommendation>();
        if (mRecommendationServiceAdapterRef != null) {
            RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
            List<Recommendation> allNotificationRecommendations = recommendationServiceAdapter.getAllNotificationRecommendations();
            if (allNotificationRecommendations != null && !allNotificationRecommendations.isEmpty()) {
                allRecommendations.addAll(allNotificationRecommendations);
            }
        }
        return allRecommendations;
    }

    void cancelRecommendation(Recommendation recommendation) {
        DdbLogUtility.logRecommendationChapter(TAG, "cancelRecommendation() called with: recommendation = [" + recommendation + "]");
        if (mRecommendationServiceAdapterRef == null) {
            return;
        }

        RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
        if (recommendationServiceAdapter != null) {
            recommendationServiceAdapter.cancelRecommendation(recommendation);
        }
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        for (int i = 0; mRecommendationChangeListenerRefs != null && i < mRecommendationChangeListenerRefs.size(); i++) {
            WeakReference<RecommendationListener> listenerRef = mRecommendationChangeListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            RecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onRecommendationChanged(recommendation, recommendationChangeType);
            }
        }
    }

    private boolean areRecommendationsAvailable(int category) {
        DdbLogUtility.logRecommendationChapter(TAG, "areRecommendationsAvailable() called with: category = [" + category + "]");
        List<Recommendation> recommendations = getRecommendations(category);
        return recommendations == null || recommendations.isEmpty();
    }

    private void notifyIfRecommendationsAvailable(int category) {
        DdbLogUtility.logRecommendationChapter(TAG, "notifyIfRecommendationsAvailable() called with: category = [" + category + "]");
        if (!areRecommendationsAvailable(category)) {
            return;
        }

        for (int i = 0; mRecommendationChangeListenerRefs != null && i < mRecommendationChangeListenerRefs.size(); i++) {
            WeakReference<RecommendationListener> listenerRef = mRecommendationChangeListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            RecommendationListener listener = listenerRef.get();
            if (listener != null) {
                listener.onRecommendationsAvailable(category);
            }
        }
    }

    private void notifyIfRecommendationsAvailable(int category, RecommendationListener listener) {
        DdbLogUtility.logRecommendationChapter(TAG, "notifyIfRecommendationsAvailable() called with: category = [" + category + "], listener = [" + listener + "]");
        if (!areRecommendationsAvailable(category)) {
            return;
        }

        if (listener != null) {
            listener.onRecommendationsAvailable(category);
        }
    }
}
