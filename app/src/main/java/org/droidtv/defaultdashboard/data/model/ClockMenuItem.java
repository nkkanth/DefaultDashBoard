package org.droidtv.defaultdashboard.data.model;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ClockSettingChangeListener;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class ClockMenuItem extends TopMenuItem implements ClockSettingChangeListener {

    private View mView;
    private TextView mClockTime;
    private TextView mClockLabel;
    private SimpleDateFormat timeFormat12Hrs;
    private SimpleDateFormat timeFormat24Hrs;
    private Handler mHandler;
    private Context mContext;

    public ClockMenuItem(Context context) {
        init(context);
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public Action getAction() {
        return null;
    }

    @Override
    public void onClockFormatChanged() {
        if (mView != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateClockVisibility();
                    setTime();
                }
            });
        }
    }

    @Override
    public void onClockFormatChanged(int value) {
        if (mView != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateClockVisibility();
                    setTime();
                }
            });
        }
    }

    @Override
    public void onTimeChanged() {
        if (mView != null) {
            updateClockVisibility();
            setTime();
        }
    }

    @Override
    public void onTimeTick() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mView != null) {
                    if (mClockTime.getVisibility() == View.VISIBLE) {
                        setTime();
                    }
                }
            }
        });
    }

    private void init(Context context) {
        mContext = context;
        mHandler = new Handler();
        timeFormat12Hrs = new SimpleDateFormat(Constants.CLOCK_FORMAT_AM_PM);
        timeFormat24Hrs = new SimpleDateFormat(Constants.CLOCK_FORMAT_24);
        createView(context);
        DashboardDataManager.getInstance().registerClockSettingChangeListener(this);
    }

    private void createView(Context context) {
        LinearLayout rootView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.clock_menu_item, null);
        mClockTime = (TextView) rootView.findViewById(R.id.clock_hours_time_label);
        mClockLabel = (TextView) rootView.findViewById(R.id.clock_am_pm_label);
        setTime();
        updateClockVisibility();
        mView = rootView;
    }

    private void setTime() {
        long date = System.currentTimeMillis();
        if (DashboardDataManager.getInstance().isClockFormatAMPM()) {
            String dateString = timeFormat12Hrs.format(date);
            mClockTime.setText(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);
            if (cal.get(Calendar.AM_PM) == Calendar.AM) {
                mClockLabel.setText(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_AM);
            } else {
                mClockLabel.setText(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_PM);
            }
        } else {
            String dateString = timeFormat24Hrs.format(date);
            mClockTime.setText(dateString);
            mClockLabel.setVisibility(View.GONE);
        }
    }

    private void updateClockVisibility() {
        DdbLogUtility.logTopMenu("ClockMenuItem", "updateClockVisibility() called updateClockVisibility: "
                +DashboardDataManager.getInstance().isClockAvailable());
        if (DashboardDataManager.getInstance().isClockAvailable()) {
            if (DashboardDataManager.getInstance().isClockFormatAMPM()) {
                mClockTime.setVisibility(View.VISIBLE);
                mClockLabel.setVisibility(View.VISIBLE);
            } else {
                mClockTime.setVisibility(View.VISIBLE);
                mClockLabel.setVisibility(View.GONE);
            }
        } else {
            mClockTime.setText("00:00");
            mClockLabel.setText(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_AM);
            mClockTime.setVisibility(View.INVISIBLE);
            mClockLabel.setVisibility(View.INVISIBLE);
        }
    }
}
