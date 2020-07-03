package org.droidtv.defaultdashboard.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppLogoFetchListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;

import java.util.List;
import java.util.Map;

public class ConfigureAppAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflator;
    private List<AppInfo> mAppList;
    private Map<String, Boolean> mAppEnabledStateMap;

    public ConfigureAppAdapter(Context context) {
        mLayoutInflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mAppList == null) {
            return 0;
        }
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mAppList == null) {
            return null;
        }
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflator.inflate(R.layout.view_configure_app_list_item, parent, false);

            viewHolder.mAppNameTextView = (TextView) convertView.findViewById(R.id.configure_app_name);
            viewHolder.mAppIconImageView = (ImageView) convertView.findViewById(R.id.configure_app_icon);
            viewHolder.mSwitch = (Switch) convertView.findViewById(R.id.configure_app_enabled_state_switch);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AppInfo appInfo = mAppList.get(position);
        viewHolder.mAppNameTextView.setText(appInfo.getLabel());
        viewHolder.mPackageName = appInfo.getPackageName();
        DashboardDataManager.getInstance().fetchAppLogo(appInfo.getPackageName(), viewHolder);

        boolean isAppEnabled = mAppEnabledStateMap.get(appInfo.getPackageName());
        viewHolder.mSwitch.setChecked(isAppEnabled);

        return convertView;
    }

    public void clearList() {
        mAppList.clear();
    }

    public void setAppList(List<AppInfo> appList) {
        if (appList == null || appList.isEmpty()) {
            mAppList.clear();
            return;
        }
        mAppList = appList;
    }

    public void setAppEnabledStateMap(Map<String, Boolean> appEnabledStateMap) {
        mAppEnabledStateMap = appEnabledStateMap;
    }

    private static class ViewHolder implements AppLogoFetchListener {
        TextView mAppNameTextView;
        ImageView mAppIconImageView;
        Switch mSwitch;
        String mPackageName;

        @Override
        public void onAppLogoFetchComplete(String packageName, Drawable logo) {
            if (logo != null) {
                if (packageName.equals(mPackageName)) {
                    mAppIconImageView.setImageDrawable(logo);
                }
            }
        }
    }
}