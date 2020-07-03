package org.droidtv.defaultdashboard.recommended;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListenerServiceClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class RecommendationProviderService extends Service implements RecommendationListenerService.NotifyRecommendation, RecommendationListenerServiceClient {
    private static final String TAG = "RecommendationProviderService";
    IRecommendationServiceCallback recommendationCallback = null;
    private WeakReference<RecommendationListenerService.RecommendationServiceAdapter> mRecommendationServiceAdapterRef;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "RecommendationProviderService onBind...");
        return recommendationService.asBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "RecommendationProviderService onStartCommand...");
        return START_STICKY;
    }

    public void registerClientWithRecommendationService() {
        Log.d(TAG, "registerClientWithRecommendationService");
        DashboardDataManager.getInstance().addRecommendationListenerServiceClient(this);
    }

    public void unregisterClientWithRecommendationService() {
        Log.d(TAG, "unregisterClientWithRecommendationService");
        DashboardDataManager.getInstance().removeRecommendationListenerServiceClient(this);
    }

    @Override
    public void onRecommendationListenerServiceAvailable(WeakReference<RecommendationListenerService.RecommendationServiceAdapter> adapterRef) {
        mRecommendationServiceAdapterRef = adapterRef;

        if (mRecommendationServiceAdapterRef == null) {
            return;
        }

        RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
        if (recommendationServiceAdapter != null) {

            recommendationServiceAdapter.registerRecommendationServiceAdapterCallback(RecommendationProviderService.this);

            try {
                recommendationCallback.onRegistrationSuccess();
            } catch (RemoteException e) {
                Log.e(TAG, "onRegistrationSuccess exception: " + e.getMessage());
            }
        }
    }

    @Override
    public void onRecommendationListenerServiceUnavailable() {
        mRecommendationServiceAdapterRef = null;
    }

    private final IRecommendationService.Stub recommendationService = new IRecommendationService.Stub() {
        @Override
        public void registerCallback(IRecommendationServiceCallback listener) throws RemoteException {
            recommendationCallback = listener;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    registerClientWithRecommendationService();
                }
            });
        }

        @Override
        public void unRegisterCallback(IRecommendationServiceCallback listener) throws RemoteException {
            Log.d(TAG, "RecommendationProviderService unRegisterCallback..." + listener.getClass());
            recommendationCallback = null;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    unregisterClientWithRecommendationService();
                }
            });
        }

        @Override
        public List<Recommendation> getAllRecommendations() throws RemoteException {
            Log.d(TAG, "RecommendationProviderService getAllRecommendations...");
            List<Recommendation> recommendationsList = new ArrayList<>();
            if (mRecommendationServiceAdapterRef == null) {
                return null;
            }

            RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
            if (recommendationServiceAdapter != null) {
                return recommendationServiceAdapter.getAllNotificationRecommendations();
            }
            return recommendationsList;
        }

        @Override
        public void cancelRecommendation(Recommendation recommendation) throws RemoteException {
            Log.d(TAG, "RecommendationProviderService cancelRecommendation...");
            if (mRecommendationServiceAdapterRef == null) {
                return;
            }

            RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter = mRecommendationServiceAdapterRef.get();
            if (recommendationServiceAdapter != null) {
                recommendationServiceAdapter.cancelRecommendation(recommendation);
            }
        }
    };

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        try {
            if (recommendationCallback != null && recommendation != null) {
                recommendationCallback.onRecommendationChanged(recommendation, recommendationChangeType);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "exception while sending recommendation changed: " + e.getMessage());
        }
    }
}
