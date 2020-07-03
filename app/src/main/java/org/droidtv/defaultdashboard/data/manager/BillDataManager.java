package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.BillSettingsListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

final class BillDataManager extends ContextualObject {

    private static final String TAG = "BillDataManager";

    private ITvSettingsManager mTvSettingsManager;
    private ArrayList<WeakReference<BillSettingsListener>> mBillSettingsListenerRefs;

    BillDataManager(Context context) {
        super(context);
        DdbLogUtility.logTopMenu(TAG, "constructor");
        init();
    }

    private void init() {
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        mBillSettingsListenerRefs = new ArrayList<>();
    }

    void onBillSettingsChanged(int value) {
        DdbLogUtility.logTopMenu(TAG, "onBillSettingsChanged value " + value);
        for (int i = 0; mBillSettingsListenerRefs != null && i < mBillSettingsListenerRefs.size(); i++) {
            WeakReference<BillSettingsListener> listenerRef = mBillSettingsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            BillSettingsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onBillSettingsChanged(value);
            }
        }
    }

    void registerBillSettingsListener(BillSettingsListener listener) {
        if (listener == null) {
            return;
        }
        mBillSettingsListenerRefs.add(new WeakReference<>(listener));
    }

    void unregisterBillSettingsListener(BillSettingsListener billSettingsListener) {
        if (mBillSettingsListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mBillSettingsListenerRefs.size(); i++) {
            WeakReference<BillSettingsListener> ref = mBillSettingsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            BillSettingsListener listener = ref.get();
            if (listener != null && listener.equals(billSettingsListener)) {
                mBillSettingsListenerRefs.remove(ref);
            }
        }
    }

    boolean isBillAvailable() {
        boolean available = (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE, 0, 0) == TvSettingsDefinitions.PbsProfessionalModeConstants.PBSMGR_PROFESSIONAL_MODE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksPmsService.PBSMGR_NETWORKS_PMS_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsTvDiscoveryServiceConstants.PBSMGR_TVDISCOVERY_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenPmsService.PBSMGR_NETWORKS_WEB_LISTEN_PMS_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenTvDiscoveryService.PBSMGR_NETWORKS_WEB_LISTEN_TV_DISCOVERY_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_BILL, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON);
        DdbLogUtility.logTopMenu(TAG, "isBillAvailable " + available);
        return available;
    }
}
