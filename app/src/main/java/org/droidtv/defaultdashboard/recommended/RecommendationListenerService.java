package org.droidtv.defaultdashboard.recommended;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sandeep.kumar on 12/10/2017.
 */

public class RecommendationListenerService extends NotificationListenerService {

    private static final String TAG = "RecommendationListenerService";

    private ArrayList<NotifyRecommendation> mListeners = null;

    private final RecommendationServiceAdapter mRecommendationServiceAdapter;

    private HashMap<String, Recommendation> mVodRecommendationMap;
    private ArrayList<Recommendation> mVodRecommendations;

    private HashMap<String, Recommendation> mSmartInfoRecommendationMap;
    private ArrayList<Recommendation> mSmartInfoRecommendations;

    private HashMap<String, Recommendation> mGamingRecommendationMap;
    private ArrayList<Recommendation> mGamingRecommendations;

    private HashMap<String, Recommendation> mAppRecommendationMap;
    private ArrayList<Recommendation> mAppRecommendations;

    public interface NotifyRecommendation {
        void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType);
    }

    public class RecommendationServiceAdapter {
        public List<Recommendation> getAllNotificationRecommendations() {
            return RecommendationListenerService.this.getAllRecommendations();
        }

        public ArrayList<Recommendation> getRecommendations(int categoryId) {
            return RecommendationListenerService.this.getRecommendations(categoryId);
        }

        public void registerRecommendationServiceAdapterCallback(NotifyRecommendation notifyRecommendation) {
            RecommendationListenerService.this.registerCallback(notifyRecommendation);
        }

        public void unregisterRecommendationServiceAdapterCallback(NotifyRecommendation notifyRecommendation) {
            RecommendationListenerService.this.unRegisterCallback(notifyRecommendation);
        }

        public void cancelRecommendation(Recommendation recommendation) {
            RecommendationListenerService.this.cancelRecommendation(recommendation);
        }
    }

    public RecommendationListenerService() {
        mListeners = new ArrayList<>();
        mVodRecommendationMap = new HashMap<>();
        mSmartInfoRecommendationMap = new HashMap<>();
        mGamingRecommendationMap = new HashMap<>();
        mAppRecommendationMap = new HashMap<>();
        mVodRecommendations = new ArrayList<>();
        mSmartInfoRecommendations = new ArrayList<>();
        mGamingRecommendations = new ArrayList<>();
        mAppRecommendations = new ArrayList<>();
        mRecommendationServiceAdapter = new RecommendationServiceAdapter();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "#### onCreate");
        registerAsSystemService(this, this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "#### onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "#### onListenerConnected");

        DashboardDataManager.getInstance().notifyRecommendationListenerServiceAvailable(mRecommendationServiceAdapter);

        // Check if any any active notifications are there which the user can see
        StatusBarNotification[] activeNotifications = getActiveNotifications();
        for (int i = 0; activeNotifications != null && i < activeNotifications.length; i++) {
            extractsRecommedationDetails(activeNotifications[i]);
        }
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "#### onListenerDisconnected");

        DashboardDataManager.getInstance().notifyRecommendationListenerServiceUnavailable();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn != null) {
            extractsRecommedationDetails(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn != null) {
            updateRecommedationMap(sbn);
        }
    }

    public void registerCallback(NotifyRecommendation notifyRecommendation) {
        Log.d(TAG, "registerCallback --> " + notifyRecommendation);
        if (mListeners != null) {
            mListeners.add(notifyRecommendation);
        }
    }

    public void unRegisterCallback(NotifyRecommendation notifyRecommendation) {
        if (mListeners != null) {
            mListeners.remove(notifyRecommendation);
        }
    }

    private void extractsRecommedationDetails(StatusBarNotification statusBarNotification) {

        boolean existingNotification = false;
        String[] notiContentType = null;

        Notification notification = statusBarNotification.getNotification();
        String category = notification.category;

        if (category == null || !category.equals(Notification.CATEGORY_RECOMMENDATION)) {
            return;
        }

        Bundle extras = notification.extras;
        int flags = notification.flags;
        boolean isCancel;
        if (flags == 0) {
            isCancel = false;
        } else {
            isCancel = true;
        }
        int priority = notification.priority;
        RecommendationImportance importance = getPriority(priority);
        PendingIntent pendingIntent = notification.contentIntent;

        Bundle extenderExtras = extras.getBundle("android.CONTENT_INFO_EXTENSIONS");
        if (extenderExtras != null) {
            notiContentType = extenderExtras.getStringArray("android.contentType");
        }

        int notificationId = statusBarNotification.getId();
        String key = statusBarNotification.getKey();
        // Extra "android.title" may return SpannableString. Hence we will retrieve "android.title" as an object and call toString() on that object
        String title = null;
        if(extras.get("android.title") != null) {
            title = extras.get("android.title").toString();
        }
        String description = extras.getString("android.text");
        String subTitle = extras.getString("android.subText");
        Bitmap largeIcon = (Bitmap) extras.get("android.largeIcon");
        String backgroundImageUri = extras.getString("android.backgroundImageUri");

        Recommendation recommendation = new Recommendation(notificationId, key, title, subTitle, description, importance, backgroundImageUri, notiContentType, isCancel, largeIcon, pendingIntent);
        DdbLogUtility.logRecommendationChapter(TAG, "extractsRecommedationDetails " + recommendation.toString());

        if (notiContentType != null) {
            for (String contentType : notiContentType) {
                Log.d(TAG, "contentType : " + contentType);
                if (contentType.equalsIgnoreCase("VOD")) {
                    existingNotification = manipulateRecommendationArray(mVodRecommendationMap, mVodRecommendations, recommendation);
                } else if (contentType.equalsIgnoreCase("SmartInfo")) {
                    existingNotification = manipulateRecommendationArray(mSmartInfoRecommendationMap, mSmartInfoRecommendations, recommendation);
                } else if (contentType.equalsIgnoreCase("Gaming")) {
                    existingNotification = manipulateRecommendationArray(mGamingRecommendationMap, mGamingRecommendations, recommendation);
                } else {
                    existingNotification = manipulateRecommendationArray(mAppRecommendationMap, mAppRecommendations, recommendation);
                }
            }
        } else {
            existingNotification = manipulateRecommendationArray(mAppRecommendationMap, mAppRecommendations, recommendation);
        }

        if (mListeners != null) {
            for (NotifyRecommendation notifyRecommendation : mListeners) {
                if (existingNotification) {
                    notifyRecommendation.onRecommendationChanged(recommendation, RecommendationChangeType.UPDATED);
                } else {
                    notifyRecommendation.onRecommendationChanged(recommendation, RecommendationChangeType.ADDED);
                }
            }
        }
    }

    private RecommendationImportance getPriority(int priority) {
        DdbLogUtility.logRecommendationChapter(TAG, "getPriority() called with: priority = [" + priority + "]");
        RecommendationImportance imp = RecommendationImportance.DEFAULT;
        switch (priority) {
            case 1:
                imp = RecommendationImportance.MIN;
                break;
            case 2:
                imp = RecommendationImportance.LOW;
                break;
            case 3:
                imp = RecommendationImportance.DEFAULT;
                break;
            case 4:
                imp = RecommendationImportance.HIGH;
                break;
            case 5:
                imp = RecommendationImportance.MAX;
                break;
            default:
                imp = RecommendationImportance.NONE;
                break;
        }
        return imp;
    }

    private boolean manipulateRecommendationArray(HashMap<String, Recommendation> recommendationMap, ArrayList<Recommendation> recommendations,
                                                  Recommendation recommendation) {
        boolean existingNotification = false;
        String notificationKey = recommendation.getKey();
        if (recommendationMap.get(notificationKey) != null) {
            existingNotification = true;
            for (int i = 0; i < recommendations.size(); i++) {
                Recommendation recommendationItem = recommendations.get(i);
                if (recommendation.getKey().equals(recommendationItem.getKey())) {
                    recommendations.remove(i);
                    recommendationMap.remove(notificationKey);
                    break;
                }
            }
        }
        recommendationMap.put(notificationKey, recommendation);
        recommendations.add(recommendation);
        return existingNotification;
    }

    /**
     * Update respective recommedation map on cancellation of notification
     *
     * @param statusBarNotification
     */
    private void updateRecommedationMap(StatusBarNotification statusBarNotification) {

        Recommendation recommendation = null;
        String[] notiContentType = null;

        Notification notification = statusBarNotification.getNotification();
        Bundle extras = notification.extras;

        Bundle extenderExtras = extras.getBundle("android.CONTENT_INFO_EXTENSION");
        if (extenderExtras != null) {
            notiContentType = extenderExtras.getStringArray("android.contentType");
        }

        String notificationKey = statusBarNotification.getKey();

        recommendation = getRecommendationToBeCanceled(notificationKey);

        if (mListeners != null) {
            for (NotifyRecommendation notifyRecommendation : mListeners) {
                notifyRecommendation.onRecommendationChanged(recommendation, RecommendationChangeType.CANCELED);
            }
        }
    }

    private Recommendation getRecommendationToBeCanceled(String notificationKey) {
        Recommendation recommendation = null;

        recommendation = mVodRecommendationMap.get(notificationKey);
        if (recommendation != null) {
            mVodRecommendationMap.remove(notificationKey);
            mVodRecommendations.remove(recommendation);
            return recommendation;
        }

        recommendation = mSmartInfoRecommendationMap.get(notificationKey);
        if (recommendation != null) {
            mSmartInfoRecommendationMap.remove(notificationKey);
            mSmartInfoRecommendations.remove(recommendation);
            return recommendation;
        }

        recommendation = mGamingRecommendationMap.get(notificationKey);
        if (recommendation != null) {
            mGamingRecommendationMap.remove(notificationKey);
            mGamingRecommendations.remove(recommendation);
            return recommendation;
        }

        recommendation = mAppRecommendationMap.get(notificationKey);
        if (recommendation != null) {
            mAppRecommendationMap.remove(notificationKey);
            mAppRecommendations.remove(recommendation);
            return recommendation;
        }

        return recommendation;
    }

    /**
     * Get all active notifications from the notification manager
     *
     * @return
     */
    public List<Recommendation> getAllRecommendations() {
        Log.d(TAG, "Inside RecommendationListenerService getAllRecommedations()...");
        List<Recommendation> allRecommedations = new ArrayList<Recommendation>();

        allRecommedations.addAll(getRecommendations(RecommendationHelper.Category.VOD));
        allRecommedations.addAll(getRecommendations(RecommendationHelper.Category.SMARTINFO));
        allRecommedations.addAll(getRecommendations(RecommendationHelper.Category.GAMES));
        allRecommedations.addAll(getRecommendations(RecommendationHelper.Category.APPS));

        return allRecommedations;
    }

    /**
     * Get all recommedations based on recommedation type
     *
     * @param categoryId
     * @return
     */
    public ArrayList<Recommendation> getRecommendations(int categoryId) {
        DdbLogUtility.logRecommendationChapter(TAG, "getRecommendations() called with: categoryId = [" + categoryId + "]");
        switch (categoryId) {
            case RecommendationHelper.Category.VOD:
                return mVodRecommendations;
            case RecommendationHelper.Category.SMARTINFO:
                return mSmartInfoRecommendations;
            case RecommendationHelper.Category.GAMES:
                return mGamingRecommendations;
            case RecommendationHelper.Category.APPS:
                return mAppRecommendations;
            default:
                return null;
        }
    }

    private ArrayList<Recommendation> getRecommendations(HashMap<String, Recommendation> recommendationMap) {
        Log.d(TAG, "Inside RecommendationListenerService getRecommendations() ...");
        return new ArrayList<>(recommendationMap.values());
    }

    /**
     * Cancel the specified Recommendation
     *
     * @param recommendation The Recommendation to be cancelled
     */
    public void cancelRecommendation(Recommendation recommendation) {
        cancelNotification(recommendation.getKey());
    }

    public static void registerAsSystemService(RecommendationListenerService service, Context context) {
        String className = "android.service.notification.NotificationListenerService";
        try {

            @SuppressWarnings("rawtypes")
            Class notificationListenerService = Class.forName(className);

            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[3];
            paramTypes[0] = Context.class;
            paramTypes[1] = ComponentName.class;
            paramTypes[2] = int.class;

            Method registerAsSystemService = notificationListenerService.getMethod("registerAsSystemService", paramTypes);

            //Parameters of the registerAsSystemService methodd
            Object[] params = new Object[3];
            params[0] = context;
            params[1] = new ComponentName(context.getPackageName(), service.getClass().getCanonicalName());
            params[2] = -1; // All user of the device, -2 if only current user
            // finally, invoke the function on our instance
            registerAsSystemService.invoke(service, params);

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found", e);
           Log.e(TAG,"Exception :" +e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "No such method", e);
           Log.e(TAG,"Exception :" +e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTarget", e);
           Log.e(TAG,"Exception :" +e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal access", e);
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }
}
