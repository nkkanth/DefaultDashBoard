package org.droidtv.defaultdashboard.data.model.moreChapter;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.Action;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sandeep.kumar on 24/11/2017.
 */

public class TalkBackFeatureItem extends MoreChapterShelfItem {

    private Action mAction;
    public TalkBackFeatureItem(Context context) {
        super(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_TALKBACK), context.getDrawable(R.drawable.notifications_n_54x54));
        mAction = new TalkBackFeatureAction(context);
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    private static class TalkBackFeatureAction extends ContextualObject implements Action {
        private Context mContext;
        public static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
        final static TextUtils.SimpleStringSplitter sStringColonSplitter =
                new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);

        protected TalkBackFeatureAction(Context context) {
            super(context);
            mContext = context;
        }
        
        @Override
        public void perform() {
            try {
                String TALK_BACK_PACKAGE_NAME = "com.google.android.marvin.talkback";
                String TALK_BACK_CLASS_NAME = "com.google.android.marvin.talkback.TalkBackService";
                String TALKBACK_SERVICE = "com.google.android.marvin.talkback/.TalkBackService";
                AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
                List<AccessibilityServiceInfo> enableServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
                int i = 0;
                for (i = 0; i < enableServices.size(); i++) {
                    if (enableServices.get(i).getId().contains(TALKBACK_SERVICE)) {
                        DdbLogUtility.logMoreChapter(TalkBackFeatureItem.class.getName(), "Talkback was ON so switching OFF");
                        setAccessibilityServiceState(mContext, new ComponentName(TALK_BACK_PACKAGE_NAME, TALK_BACK_CLASS_NAME), false, UserHandle.myUserId());
                        break;
                    }
                }

                if (i == enableServices.size()) {
                    DdbLogUtility.logMoreChapter(TalkBackFeatureItem.class.getName(), "Talkback was OFF so switching ON");
                    setAccessibilityServiceState(mContext, new ComponentName(TALK_BACK_PACKAGE_NAME, TALK_BACK_CLASS_NAME), true, UserHandle.myUserId());
                }
            }catch (Exception e){
               Log.d("TalkBackFeatureItem" ,"There is some issue in enabling TalkBack :"+e.getMessage());
            }
        }
		
        /**
         * Changes an accessibility component's state for {@param userId}.
         */
        public void setAccessibilityServiceState(Context context, ComponentName toggledService,
                                                        boolean enabled, int userId) {
            // Parse the enabled services.
            Set<ComponentName> enabledServices = getEnabledServicesFromSettings(
                    context, userId);

            if (enabledServices.isEmpty()) {
                enabledServices = new ArraySet<>(1);
            }

            // Determine enabled services and accessibility state.
            boolean accessibilityEnabled = false;
            if (enabled) {
                enabledServices.add(toggledService);
                // Enabling at least one service enables accessibility.
                accessibilityEnabled = true;
            } else {
                enabledServices.remove(toggledService);
                // Check how many enabled and installed services are present.
                Set<ComponentName> installedServices = getInstalledServices(context);
                for (ComponentName enabledService : enabledServices) {
                    if (installedServices.contains(enabledService)) {
                        // Disabling the last service disables accessibility.
                        accessibilityEnabled = true;
                        break;
                    }
                }
            }

            // Update the enabled services setting.
            StringBuilder enabledServicesBuilder = new StringBuilder();
            // Keep the enabled services even if they are not installed since we
            // have no way to know whether the application restore process has
            // completed. In general the system should be responsible for the
            // clean up not settings.
            for (ComponentName enabledService : enabledServices) {
                enabledServicesBuilder.append(enabledService.flattenToString());
                enabledServicesBuilder.append(
                        ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            }
            final int enabledServicesBuilderLength = enabledServicesBuilder.length();
            if (enabledServicesBuilderLength > 0) {
                enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
            }
            Settings.Secure.putStringForUser(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    enabledServicesBuilder.toString(), userId);
        }

        /**
         * @return the set of enabled accessibility services for {@param userId}. If there are no
         * services, it returns the unmodifiable {@link Collections#emptySet()}.
         */
        private Set<ComponentName> getEnabledServicesFromSettings(Context context, int userId) {
            final String enabledServicesSetting = Settings.Secure.getStringForUser(
                    context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    userId);
            if (enabledServicesSetting == null) {
                return Collections.emptySet();
            }

            final Set<ComponentName> enabledServices = new HashSet<>();
            final TextUtils.SimpleStringSplitter colonSplitter = sStringColonSplitter;
            colonSplitter.setString(enabledServicesSetting);

            while (colonSplitter.hasNext()) {
                final String componentNameString = colonSplitter.next();
                final ComponentName enabledService = ComponentName.unflattenFromString(
                        componentNameString);
                if (enabledService != null) {
                    enabledServices.add(enabledService);
                }
            }

            return enabledServices;
        }
        /** This function return Set of Installed Accessibility Service **/
        private Set<ComponentName> getInstalledServices(Context context) {
            final Set<ComponentName> installedServices = new HashSet<>();
            installedServices.clear();

            final List<AccessibilityServiceInfo> installedServiceInfos =
                    AccessibilityManager.getInstance(context)
                            .getInstalledAccessibilityServiceList();
            if (installedServiceInfos == null) {
                return installedServices;
            }

            for (final AccessibilityServiceInfo info : installedServiceInfos) {
                final ResolveInfo resolveInfo = info.getResolveInfo();
                final ComponentName installedService = new ComponentName(
                        resolveInfo.serviceInfo.packageName,
                        resolveInfo.serviceInfo.name);
                installedServices.add(installedService);
            }
            return installedServices;
        }
    }
}
