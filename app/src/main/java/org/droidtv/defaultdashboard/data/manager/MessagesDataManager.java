package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MessageCountChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MessageSettingsListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static org.droidtv.defaultdashboard.util.Constants.INTENT_GUEST_CHECK_IN_STATUS_OCCUPIED_VALUE;
import static org.droidtv.defaultdashboard.util.Constants.INTENT_GUEST_CHECK_IN_SYSTEM_PROPERTY_KEY;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

final class MessagesDataManager extends ContextualObject {

    private static final String TAG = "MessagesDataManager";
    private final String UNREAD_MESSAGE_COUNT = "persist.sys.unReadMessageCount";
    private ITvSettingsManager mTvSettingsManager;
    private ArrayList<WeakReference<MessageSettingsListener>> mMessageSettingsListenerRefs;
    private ArrayList<WeakReference<MessageCountChangeListener>> mMessageCountChangeListenerRefs;
    private ArrayList<WeakReference<DashboardDataManager.GuestCheckInStatusChangeListener>> mGuestCheckInStatusChangeListenerRefs;

    MessagesDataManager(Context context) {
        super(context);
        init();
    }

    private void init() {
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        mMessageSettingsListenerRefs = new ArrayList<>();
        mMessageCountChangeListenerRefs = new ArrayList<>();
        mGuestCheckInStatusChangeListenerRefs = new ArrayList<>();
        DdbLogUtility.logCommon(TAG, "init");
    }

    void onMessageSettingsChanged() {
        for (int i = 0; mMessageSettingsListenerRefs != null && i < mMessageSettingsListenerRefs.size(); i++) {
            WeakReference<MessageSettingsListener> listenerRef = mMessageSettingsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MessageSettingsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onMessageSettingsChanged();
            }
        }
    }

    void onMessageSettingsChanged(int value) {
        DdbLogUtility.logCommon(TAG, "onMessageSettingsChanged() called with: value = [" + value + "]");
        for (int i = 0; mMessageSettingsListenerRefs != null && i < mMessageSettingsListenerRefs.size(); i++) {
            WeakReference<MessageSettingsListener> listenerRef = mMessageSettingsListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MessageSettingsListener listener = listenerRef.get();
            if (listener != null) {
                listener.onMessageSettingsChanged(value);
            }
        }
    }

    void registerMessageSettingsListener(MessageSettingsListener listener) {
        if (listener == null) {
            return;
        }
        mMessageSettingsListenerRefs.add(new WeakReference<>(listener));
    }

    void unregisterMessageSettingsListener(MessageSettingsListener messageSettingsListener) {
        if (mMessageSettingsListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mMessageSettingsListenerRefs.size(); i++) {
            WeakReference<MessageSettingsListener> ref = mMessageSettingsListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            MessageSettingsListener listener = ref.get();
            if (listener != null && listener.equals(messageSettingsListener)) {
                mMessageSettingsListenerRefs.remove(ref);
            }
        }
    }

    void addMessageCountChangeListener(MessageCountChangeListener messageCountChangeListener) {
        if (messageCountChangeListener == null) {
            return;
        }
        mMessageCountChangeListenerRefs.add(new WeakReference<MessageCountChangeListener>(messageCountChangeListener));
    }


    void removeMessageCountChangeListener(MessageCountChangeListener messageCountChangeListener) {
        if (mMessageCountChangeListenerRefs == null) {
            return;
        }
        if (mMessageCountChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mMessageCountChangeListenerRefs.size(); i++) {
                WeakReference<MessageCountChangeListener> ref = mMessageCountChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                MessageCountChangeListener listener = ref.get();
                if (listener != null && listener.equals(messageCountChangeListener)) {
                    mMessageCountChangeListenerRefs.remove(ref);
                }
            }
        }
    }

    void addGuestCheckInStatusChangeListener(DashboardDataManager.GuestCheckInStatusChangeListener guestCheckInStatusChangeListener) {
        if (guestCheckInStatusChangeListener == null) {
            return;
        }
        mGuestCheckInStatusChangeListenerRefs.add(new WeakReference<DashboardDataManager.GuestCheckInStatusChangeListener>(guestCheckInStatusChangeListener));
    }

    void removeGuestCheckInStatusChangeListener(DashboardDataManager.GuestCheckInStatusChangeListener guestCheckInStatusChangeListener) {
        if (mGuestCheckInStatusChangeListenerRefs == null) {
            return;
        }
        if (mGuestCheckInStatusChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mGuestCheckInStatusChangeListenerRefs.size(); i++) {
                WeakReference<DashboardDataManager.GuestCheckInStatusChangeListener> ref = mGuestCheckInStatusChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                DashboardDataManager.GuestCheckInStatusChangeListener listener = ref.get();
                if (listener != null && listener.equals(guestCheckInStatusChangeListener)) {
                    mGuestCheckInStatusChangeListenerRefs.remove(ref);
                }
            }
        }
    }

    boolean isMessageAvailable() {
        return (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE, 0, 0) == TvSettingsDefinitions.PbsProfessionalModeConstants.PBSMGR_PROFESSIONAL_MODE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksPmsService.PBSMGR_NETWORKS_PMS_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsTvDiscoveryServiceConstants.PBSMGR_TVDISCOVERY_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenPmsService.PBSMGR_NETWORKS_WEB_LISTEN_PMS_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenTvDiscoveryService.PBSMGR_NETWORKS_WEB_LISTEN_TV_DISCOVERY_SERVICE_ON
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_GUESTMESSAGES, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON);
    }

    boolean isMessageDisplayAvailable() {
        return (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksPmsService.PBSMGR_NETWORKS_PMS_SERVICE_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsTvDiscoveryServiceConstants.PBSMGR_TVDISCOVERY_SERVICE_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenPmsService.PBSMGR_NETWORKS_WEB_LISTEN_PMS_SERVICE_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenTvDiscoveryService.PBSMGR_NETWORKS_WEB_LISTEN_TV_DISCOVERY_SERVICE_ON) &&
                (SystemProperties.get(INTENT_GUEST_CHECK_IN_SYSTEM_PROPERTY_KEY).equalsIgnoreCase(INTENT_GUEST_CHECK_IN_STATUS_OCCUPIED_VALUE));
    }

    void notifyMessageCountChanged() {
        DdbLogUtility.logCommon(TAG, "notifyMessageCountChanged() called");
        for (int i = 0; mMessageCountChangeListenerRefs != null && i < mMessageCountChangeListenerRefs.size(); i++) {
            WeakReference<MessageCountChangeListener> listenerRef = mMessageCountChangeListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            MessageCountChangeListener listener = listenerRef.get();
            if (listener != null) {
                listener.onMessageCountChanged();
            }
        }
    }

    void notifyGuestCheckInStatusChanged() {
        DdbLogUtility.logCommon(TAG, "notifyGuestCheckInStatusChanged() called");
        if (mGuestCheckInStatusChangeListenerRefs == null) {
            return;
        }
        if (mGuestCheckInStatusChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mGuestCheckInStatusChangeListenerRefs.size(); i++) {
                WeakReference<DashboardDataManager.GuestCheckInStatusChangeListener> ref = mGuestCheckInStatusChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                DashboardDataManager.GuestCheckInStatusChangeListener listener = ref.get();
                if (listener != null) {
                    listener.OnGuestCheckInStatusChanged();
                }
            }
        }
    }

    int getMessageCount() {
        String messageCount = SystemProperties.get(UNREAD_MESSAGE_COUNT);
        if (!TextUtils.isEmpty(messageCount)) {
            try {
                DdbLogUtility.logCommon(TAG, "getMessageCount " + messageCount);
                return Integer.parseInt(messageCount);
            } catch (NumberFormatException e) {
                return getContext().getResources().getInteger(R.integer.maximum_message_count_to_display);
            }
        }
        return 0;
    }
}
