package org.droidtv.defaultdashboard;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendedAppsActivity extends AbstractConfigureAppsActivity {

    @Override
    protected String getHeaderTitle() {
        return getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_SELECT_APPS);
    }

    @Override
    protected String getHeaderSubtitle() {
        return getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_CH_SELECT_APPS);
    }

    @Override
    protected void updateAppEnabledState(Map<String, Boolean> appEnabledStateMap) {
        DashboardDataManager.getInstance().updateRecommendedAppEnabledState(appEnabledStateMap);
    }

    @Override
    protected Map<String, Boolean> buildAppEnabledStateMap(List<AppInfo> appInfos) {
        Map<String, Boolean> appEnabledStateMap = new HashMap<>();
        for (AppInfo appInfo : appInfos) {
            appEnabledStateMap.put(appInfo.getPackageName(), appInfo.isRecommendedAppEnabled());
        }
        return appEnabledStateMap;
    }
}
