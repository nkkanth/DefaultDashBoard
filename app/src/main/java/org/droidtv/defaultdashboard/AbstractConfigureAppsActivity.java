package org.droidtv.defaultdashboard;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.adapter.ConfigureAppAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractConfigureAppsActivity extends Activity implements DashboardDataManager.AppDataListener {

    private ListView mInstalledAppsListView;
    private ConfigureAppAdapter mConfigureAppAdapter;
    private List<AppInfo> mInstalledAppsList;
    private Map<String, Boolean> mAppEnabledStateMap;
    private View mCurrentSelectedView;
    private TextView mDualTextTitle;
    private TextView mDualTextSubTitle;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_apps);

        mAppEnabledStateMap = new HashMap<>();

        mCurrentSelectedView = null;
        mDualTextTitle = (TextView) findViewById(R.id.title_text);
        mDualTextSubTitle = (TextView) findViewById(R.id.sub_title_text);

        mDualTextTitle.setText(getHeaderTitle());
        mDualTextSubTitle.setText(getHeaderSubtitle());

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mConfigureAppAdapter = new ConfigureAppAdapter(this);
        mInstalledAppsListView = (ListView) findViewById(R.id.configure_apps_list);
        mInstalledAppsListView.setAdapter(mConfigureAppAdapter);
        mInstalledAppsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.configure_app_list_item_container);
                AppInfo appInfo = mInstalledAppsList.get(position);
                Switch switchView = (Switch) relativeLayout.getChildAt(2);
                if (switchView.isChecked()) {
                    switchView.setChecked(false);
                    mAppEnabledStateMap.put(appInfo.getPackageName(), false);
                } else {
                    switchView.setChecked(true);
                    mAppEnabledStateMap.put(appInfo.getPackageName(), true);
                }
            }
        });
        mInstalledAppsListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Change the font to normal for the item that becomes unselected
                DdbLogUtility.logAppsChapter("AbstractConfigureAppsActivity", "onItemSelected() called with: " +
                        "view = [" + view + "], position = [" + position + "], id = [" + id + "]");
                RelativeLayout previousItem = (RelativeLayout) mCurrentSelectedView;
                if (previousItem != null) {
                    TextView previousTextView = (TextView) previousItem.getChildAt(1);
                    previousTextView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                }

                // Update mCurrentSelectedView with current selected view item and change its font
                mCurrentSelectedView = view;
                TextView textView = (TextView) ((RelativeLayout) view).getChildAt(1);
                textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        DashboardDataManager.getInstance().addAppDataListener(this);
        fetchAllAppList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateAppEnabledState(mAppEnabledStateMap);
        DashboardDataManager.getInstance().removeAppDataListener(this);
    }

    protected abstract String getHeaderTitle();

    protected abstract String getHeaderSubtitle();

    protected abstract void updateAppEnabledState(Map<String, Boolean> appEnabledStateMap);

    protected abstract Map<String, Boolean> buildAppEnabledStateMap(List<AppInfo> appInfos);

    private void fetchAllAppList() {
        showProgress();
        DashboardDataManager.getInstance().fetchEnabledAppList();
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onAppListFetched() {
        List<AppInfo> allApps = DashboardDataManager.getInstance().getEnabledAppList();
        enabledAppListFetchComplete(allApps);
    }

    protected void enabledAppListFetchComplete(List<AppInfo> allApps) {
        hideProgress();
        if (allApps != null && !allApps.isEmpty()) {
            mInstalledAppsList = allApps;
            mConfigureAppAdapter.setAppList(mInstalledAppsList);
            mAppEnabledStateMap = buildAppEnabledStateMap(allApps);
            mConfigureAppAdapter.setAppEnabledStateMap(mAppEnabledStateMap);
            mConfigureAppAdapter.notifyDataSetChanged();
        }
    }
}
