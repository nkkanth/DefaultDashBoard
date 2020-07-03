package org.droidtv.defaultdashboard;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.droidtv.defaultdashboard.util.Constants.PACKAGE_NAME_NET_TV;

public class AppRecommendationsActivity extends AbstractConfigureAppsActivity {

    @Override
    protected String getHeaderTitle() {
        return getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_SELECT_APP_CHANNELS);
    }

    @Override
    protected String getHeaderSubtitle() {
        return getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_CH_SELECT_APP_CHANNELS);
    }

    @Override
    protected void updateAppEnabledState(Map<String, Boolean> appEnabledStateMap) {
        DashboardDataManager.getInstance().updateAppRecommendationEnabledState(appEnabledStateMap);
    }

    @Override
    protected Map<String, Boolean> buildAppEnabledStateMap(List<AppInfo> appInfos) {
        Map<String, Boolean> appEnabledStateMap = new HashMap<>();
        for (AppInfo appInfo : appInfos) {
            if (!appInfo.getPackageName().contains(PACKAGE_NAME_NET_TV)) {
                appEnabledStateMap.put(appInfo.getPackageName(), appInfo.isAppRecommendationEnabled());
            }
        }
        return appEnabledStateMap;
    }

    @Override
    public void onAppListFetched() {
        List<AppInfo> allApps = DashboardDataManager.getInstance().getEnabledAppList();
        List<AppInfo> RecommendedAppList = new ArrayList<>();
        for (AppInfo appInfo : allApps) {
            if (!appInfo.getPackageName().contains(PACKAGE_NAME_NET_TV)) {
                RecommendedAppList.add(appInfo);
            }
        }
        enabledAppListFetchComplete(RecommendedAppList);
    }
}
