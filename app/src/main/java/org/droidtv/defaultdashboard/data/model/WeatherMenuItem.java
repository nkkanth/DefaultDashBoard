package org.droidtv.defaultdashboard.data.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.WeatherInfoDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.WeatherSettingsListener;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;
import org.droidtv.defaultdashboard.util.Constants;

import java.lang.ref.WeakReference;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class WeatherMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, WeatherInfoDataListener, WeatherSettingsListener {
    private Action mAction;
    private View mView;
    private TextView mLabel;
    private ImageView mIcon;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private Context mContext;
    private UiThreadHandler mUiThreadHandler;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;
    public WeatherMenuItem(Context context , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
        init(context);
        mTopMenuItemFocusChangeListner = listener;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    @Override
    public void onClick(View v) {
        getAction().perform();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            scaleAndElevate(mIcon, mFocusScale, mFocusElevation);
            mLabel.setTextSize(mContext.getResources().getDimension(R.dimen.top_menu_selected_text_size));
            FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
            labelLayoutParams.setMargins(0, (int) view.getContext().getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
            mIcon.setBackground(view.getContext().getDrawable(R.drawable.top_menu_background_rounded));
            mLabel.setText(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_APP_CATEGORY_WEATHER));

        } else {
            scaleAndElevate(mIcon, mDefaultScale, mDefaultElevation);
            mIcon.setBackgroundColor(view.getContext().getColor(android.R.color.transparent));
            FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
            labelLayoutParams.setMargins(0, (int) view.getContext().getResources().getDimension(R.dimen.top_menu_non_selected_label_margin_top), 0, 0);
            setTemperatureText();
            mLabel.setTextSize(mContext.getResources().getDimension(R.dimen.top_menu_alarm_weather_text_size));
        }
        if(mTopMenuItemFocusChangeListner != null) {
            mTopMenuItemFocusChangeListner.OnItemFocus(view.getId() ,hasFocus);
        }
    }

    @Override
    public void onWeatherInfoDataReceived() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_WEATHER_INFO_DATA_RECEIVED);
        message.sendToTarget();
    }

    @Override
    public void onWeatherInfoDataReceived(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_WEATHER_INFO_DATA_RECEIVED);
        message.arg1 = value;
        message.sendToTarget();
    }

    @Override
    public void onWeatherSettingsChanged() {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_WEATHER_SETTING_CHANGED);
    }

    @Override
    public void onWeatherSettingsChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_WEATHER_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void init(Context context) {
        mContext = context;
        initValues(context);
        createView(context);
        createAction(context);
        DashboardDataManager.getInstance().registerWeatherInfoDataListener(this);
        DashboardDataManager.getInstance().registerWeatherSettingsListener(this);
        mUiThreadHandler = new UiThreadHandler(this);
    }

    private void initValues(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.default_scale, typedValue, true);
        mDefaultScale = typedValue.getFloat();
        context.getResources().getValue(R.dimen.top_menu_item_focus_scale, typedValue, true);
        mFocusScale = typedValue.getFloat();

        mDefaultElevation = context.getResources().getDimension(R.dimen.default_elevation);
        mFocusElevation = context.getResources().getDimension(R.dimen.focus_elevation);
    }

    private void createView(Context context) {

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.top_menu_item_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.top_menu_item, null);
        rootView.setId(R.id.top_menu_weather_view);
        mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
        mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_non_selected_label_margin_top), 0, 0);
        mLabel.setVisibility(View.VISIBLE);
        mLabel.setTextSize(context.getResources().getDimension(R.dimen.top_menu_alarm_weather_text_size));
        mLabel.setSelected(true);
        layoutParams.setMargins(0, (int) mContext.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
        rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        if (DashboardDataManager.getInstance().isWeatherEnabled()) {
            rootView.setVisibility(View.VISIBLE);
        } else {
            rootView.setVisibility(View.GONE);
        }
        mView = rootView;
        setTemperatureText();

        mView.setAccessibilityDelegate(new View.AccessibilityDelegate() {

            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName("");
                String temperature = DashboardDataManager.getInstance().getCurrentTemperature();
                if(temperature != null) {
                    String[] tempratureValue = temperature.split("°");
                    int temperatureValueWithoutDegree =Integer.valueOf(tempratureValue[0]);
                    if (temperatureValueWithoutDegree == 1) {
                        String ttsText = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_WEATHER_TEMP_DEGREE).replace("^1",tempratureValue[0]);
                        info.setContentDescription(ttsText);
                    } else {
                        String ttsText = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_WEATHER_TEMP_DEGREES).replace("^1", tempratureValue[0]);
                        info.setContentDescription(ttsText);
                    }
                }else{
                    String ttsText = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_APP_CATEGORY_WEATHER) + ". Today " + mLabel.getText();
                    info.setContentDescription(ttsText);
                }
            }
        });
    }

    private void setTemperatureText() {
        mIcon.setImageResource(DashboardDataManager.getInstance().getWeatherIcon());
        if (!mView.isFocused()) {
            mLabel.setText(DashboardDataManager.getInstance().getCurrentTemperature());
        }
    }

    private void createAction(Context context) {
        mAction = new WeatherMenuItemAction(context);
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(150).scaleX(scale);
        view.animate().setDuration(150).scaleY(scale);
        ObjectAnimator.ofFloat(mIcon, "elevation", view.getElevation(), elevation).setDuration(150).start();
    }

    private void onWeatherVisibilitySettingChanged() {
        if (DashboardDataManager.getInstance().isWeatherEnabled()) {
            mView.setVisibility(View.VISIBLE);
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    private static class WeatherMenuItemAction implements Action {

        private Context mContext;

        private WeatherMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            Intent weatherIntent = new Intent();
            weatherIntent.setClassName(Constants.WEATHER_PACKAGE_INTENT, Constants.WEATHER_ACTIVITY_INTENT);
            weatherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivityAsUser(weatherIntent, UserHandle.CURRENT_OR_SELF);
        }
    }

    private static class UiThreadHandler extends Handler {
        WeakReference<WeatherMenuItem> mWeatherMenuItemRef;
        private static final int MSG_WHAT_WEATHER_INFO_DATA_RECEIVED = 101;
        private static final int MSG_WHAT_WEATHER_SETTING_CHANGED = 102;

        private UiThreadHandler(WeatherMenuItem weatherMenuItem) {
            super();
            mWeatherMenuItemRef = new WeakReference<>(weatherMenuItem);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_WEATHER_INFO_DATA_RECEIVED) {
                WeatherMenuItem weatherMenuItem = mWeatherMenuItemRef.get();
                if (weatherMenuItem != null) {
                    weatherMenuItem.setTemperatureText();
                }
                return;
            }

            if (msg.what == MSG_WHAT_WEATHER_SETTING_CHANGED) {
                WeatherMenuItem weatherMenuItem = mWeatherMenuItemRef.get();
                if (weatherMenuItem != null) {
                    weatherMenuItem.onWeatherVisibilitySettingChanged();
                }
                return;
            }
        }
    }

}
