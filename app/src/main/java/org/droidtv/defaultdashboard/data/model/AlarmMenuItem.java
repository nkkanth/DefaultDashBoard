package org.droidtv.defaultdashboard.data.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.AlarmClock;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
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
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AlarmChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ClockSettingChangeListener;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;
import org.droidtv.defaultdashboard.util.Constants;
import java.util.Calendar;

import android.os.SystemProperties;

import static java.lang.Integer.parseInt;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class AlarmMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, AlarmChangeListener, ClockSettingChangeListener {
    private Action mAction;
    private View mView;
    private TextView mLabel;
    private ImageView mIcon;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private Context mContext;
	private final String TAG = AlarmMenuItem.class.getSimpleName();
	String[] time = null;
	String mAlarmTime = null;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;
	
    public AlarmMenuItem(Context context ,TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
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
            mLabel.setText(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_ALARM_SETTINGS_SET_ALARM_BUTTON));
            FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
            labelLayoutParams.setMargins(0, (int) view.getContext().getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
            mIcon.setBackground(view.getContext().getDrawable(R.drawable.top_menu_background_rounded));
        } else {
            scaleAndElevate(mIcon, mDefaultScale, mDefaultElevation);
            mIcon.setBackgroundColor(mContext.getColor(android.R.color.transparent));
            mLabel.setTextSize(mContext.getResources().getDimension(R.dimen.top_menu_alarm_weather_text_size));
            FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
            labelLayoutParams.setMargins(0, (int) view.getContext().getResources().getDimension(R.dimen.top_menu_non_selected_label_margin_top), 0, 0);
            setAlarmTimeText();
        }
        if(mTopMenuItemFocusChangeListner != null) {
            mTopMenuItemFocusChangeListner.OnItemFocus(view.getId() ,hasFocus);
        }
    }

    @Override
    public void updateAlarm() {
        if (!mView.isFocused()) {
            setAlarmTimeText();
        }
    }

    @Override
    public void onClockFormatChanged() {
        if (mView != null && !mView.isFocused()) {
            mView.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    setAlarmTimeText();
                }
            });
        }
    }

    @Override
    public void onClockFormatChanged(int value) {
        if (mView != null && !mView.isFocused()) {
            mView.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    setAlarmTimeText();
                }
            });
        }
    }

    @Override
    public void onTimeChanged() {
        if (mView != null && !mView.isFocused()) {
            if (!DashboardDataManager.getInstance().isClockAvailable()) {
                mView.setVisibility(View.GONE);
            } else {
                mView.setVisibility(View.VISIBLE);
            }
            setAlarmTimeText();
        }
    }

    @Override
    public void onTimeTick() {

    }

    private void init(Context context) {
        mContext = context;
        initValues(context);
        createView(context);
        createAction(context);
        DashboardDataManager.getInstance().addAlarmChangeListener(this);
        DashboardDataManager.getInstance().registerClockSettingChangeListener(this);
    }

    private void initValues(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.default_scale, typedValue, true);
        mDefaultScale = typedValue.getFloat();
        context.getResources().getValue(R.dimen.top_menu_item_focus_scale, typedValue, true);
        mFocusScale = typedValue.getFloat();
		time = getAlarmTimefromNow();
        mDefaultElevation = context.getResources().getDimension(R.dimen.default_elevation);
        mFocusElevation = context.getResources().getDimension(R.dimen.focus_elevation);
    }

    private void createView(Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.top_menu_item_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.top_menu_item, null);
        rootView.setId(R.id.top_menu_alarm_view);
        mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
        mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_non_selected_label_margin_top), 0, 0);
        setAlarmTimeText();
        mLabel.setTextSize(mContext.getResources().getDimension(R.dimen.top_menu_alarm_weather_text_size));
        mLabel.setVisibility(View.VISIBLE);
        mLabel.setSelected(true);
        layoutParams.setMargins(0, (int) mContext.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
        rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        mIcon.setImageResource(R.drawable.icon_158_reminders_n_48x48);
        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        if (!DashboardDataManager.getInstance().isClockAvailable()) {
            rootView.setVisibility(View.GONE);
        } else if( DashboardDataManager.getInstance().isBFLProduct()) {
            rootView.setVisibility(View.GONE);
        }else{
            rootView.setVisibility(View.VISIBLE);
        }
        mView = rootView;
		mView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName("");
                StringBuilder ttsText = new StringBuilder();
                DdbLogUtility.logAppsChapter(TAG, "Alarm set for : "+mAlarmTime);
                String alarm = null;
                if (!DashboardDataManager.getInstance().isAlarmSet()){
                    ttsText.append(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_NO_ALARM_IS_SET));
                } else {
                    DdbLogUtility.logAppsChapter(TAG ,"hr :"+time[0] +" min :"+time[1]);
                    int hr =Integer.parseInt(time[0]);
                    int min =Integer.parseInt(time[1]);

                    if(hr == 1 && min == 1){
                        alarm = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_ALARM_SET_HOUR_MIN).replace("^1" , mAlarmTime).replace("^2" ,time[0]).replace("^3" ,time[1]);
                    }else if(hr == 1 && min > 1){
                        alarm = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_ALARM_SET_HOUR_MINS).replace("^1" , mAlarmTime).replace("^2" ,time[0]).replace("^3" ,time[1]);
                    }else if(hr > 1 && min ==1 ){
                        alarm = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_ALARM_SET_HOURS_MIN).replace("^1" , mAlarmTime).replace("^2" ,time[0]).replace("^3" ,time[1]);
                    }else {
                        alarm = mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_ALARM_SET_HOURS_MINS).replace("^1" , mAlarmTime).replace("^2" ,time[0]).replace("^3" ,time[1]);
                    }
                }
                ttsText.append(alarm);
                DdbLogUtility.logAppsChapter(TAG ,"alarm :"+alarm );
                info.setContentDescription(ttsText);
            }
        });
    }
	
	 private long getAlarmTime(){
        long hours = SystemProperties.getInt(Constants.SYSTEM_PROPERTY_HTV_ALARM_TIME_HOURS, 0);
        long minutes = SystemProperties.getInt(Constants.SYSTEM_PROPERTY_HTV_ALARM_TIME_MINUTES, 0);
        DdbLogUtility.logTopMenu(TAG, "hours " + hours + "  minutes " + minutes);
        long alarmWakeupTimeInMillis = (long) (hours * Math.pow(10, 8) + minutes);
        return alarmWakeupTimeInMillis;
    }

    private String[] getAlarmTimefromNow(){
        String[] mAlarmTimefromNow = new String[2];
        Calendar currentTimeCalenderObj = Calendar.getInstance();
        currentTimeCalenderObj.setTimeInMillis(System.currentTimeMillis());

        Calendar alarmTimeCalenderObj = Calendar.getInstance();
        alarmTimeCalenderObj.setTimeInMillis(getAlarmTime());
        int alarmMinsFromNow = (alarmTimeCalenderObj.get(Calendar.HOUR)*60 + alarmTimeCalenderObj.get(Calendar.MINUTE))-(currentTimeCalenderObj.get(Calendar.HOUR)*60 + currentTimeCalenderObj.get(Calendar.MINUTE));

        if(alarmTimeCalenderObj.get(Calendar.DATE) - currentTimeCalenderObj.get(Calendar.DATE) == 1){
            DdbLogUtility.logAppsChapter(TAG ,"Alarm is set for next day ");
            alarmMinsFromNow = (24*60) + alarmMinsFromNow;
        }
        DdbLogUtility.logAppsChapter(TAG ,"alarmMinsFromNow  :"+alarmMinsFromNow);
        if(alarmMinsFromNow < 60){
            mAlarmTimefromNow[0] = String.valueOf(0);
            mAlarmTimefromNow[1] = String.valueOf(alarmMinsFromNow %60);
        }else{
            mAlarmTimefromNow[0] = String.valueOf(alarmMinsFromNow /60);
            mAlarmTimefromNow[1] = String.valueOf(alarmMinsFromNow %60);
        }
        DdbLogUtility.logAppsChapter(TAG ,"Alarm HR :"+mAlarmTimefromNow[0] + " Current Mins: "+mAlarmTimefromNow[1]);
        return mAlarmTimefromNow;
    }

    private void createAction(Context context) {
        mAction = new AlarmMenuItemAction(context);
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(150).scaleX(scale);
        view.animate().setDuration(150).scaleY(scale);
        ObjectAnimator.ofFloat(mIcon, "elevation", view.getElevation(), elevation).setDuration(150).start();
    }

    private void setAlarmTimeText() {
        String alarmTime;
        SpannableString spannableAlarmTime;
        if (DashboardDataManager.getInstance().isAlarmSet()) {
            alarmTime = DashboardDataManager.getInstance().getAlarmTime();
            spannableAlarmTime = new SpannableString(alarmTime);
            if (DashboardDataManager.getInstance().isClockFormatAMPM()) {
                spannableAlarmTime.setSpan(new AbsoluteSizeSpan((int) mContext.getResources().getDimension(R.dimen.top_menu_alarm_AMPM_text_size)), alarmTime.length() - 2, alarmTime.length(), 0);
            }
        } else {
            alarmTime = "--:--";
            spannableAlarmTime = new SpannableString(alarmTime);
        }
        mLabel.setText(spannableAlarmTime);
		mAlarmTime = mLabel.getText().toString();
		time = getAlarmTimefromNow();
    }

    private static class AlarmMenuItemAction implements Action {

        private Context mContext;

        private AlarmMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            Intent launchSetAlarmintent = new Intent(Constants.ACTION_ALARM_INTENT);
            launchSetAlarmintent.putExtra(Constants.SET_ALARM_EXTRA_CLASS_INTENT, Constants.SET_ALARM_EXTRA_PACKAGE_INTENT);
            launchSetAlarmintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivityAsUser(launchSetAlarmintent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
