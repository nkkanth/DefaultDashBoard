package org.droidtv.defaultdashboard.data.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.BillSettingsListener;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;
import org.droidtv.defaultdashboard.util.Constants;

import java.lang.ref.WeakReference;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class BillMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, BillSettingsListener {
    private Action mAction;
    private View mView;
    private ImageView mIcon;
    private TextView mLabel;
    private float mDefaultScale;
    private float mFocusScale;
    private float mFocusScaleY;
    private float mDefaultElevation;
    private float mFocusElevation;
    private UiThreadHandler mUiThreadHandler;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;
    public BillMenuItem(Context context , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
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
            mLabel.setVisibility(View.VISIBLE);
            mLabel.animate().setDuration(150).alpha(1);
            mIcon.setBackground(view.getContext().getDrawable(R.drawable.top_menu_background_rounded));
        } else {
            scaleAndElevate(mIcon, mDefaultScale, mDefaultElevation);
            mIcon.setBackgroundColor(view.getContext().getColor(android.R.color.transparent));
            mLabel.animate().setDuration(150).alpha(0);
            mLabel.setVisibility(View.INVISIBLE);
        }
        if(mTopMenuItemFocusChangeListner != null) {
            mTopMenuItemFocusChangeListner.OnItemFocus(view.getId() ,hasFocus);
        }
    }

    @Override
    public void onBillSettingsChanged() {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_BILL_SETTING_CHANGED);
    }

    @Override
    public void onBillSettingsChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_BILL_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void init(Context context) {
        initValues(context);
        createView(context);
        createAction(context);
        mUiThreadHandler = new UiThreadHandler(this);
        DashboardDataManager.getInstance().registerBillSettingsListener(this);
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
        rootView.setId(R.id.top_menu_bill_view);
        mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
        mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
        mLabel.setText(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_WI_DASHBOARD_BILL));
        mLabel.setTextSize(context.getResources().getDimension(R.dimen.top_menu_selected_text_size));
        mLabel.setSelected(true);
        layoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
        rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        mIcon.setImageResource(R.drawable.icon_376_bill_n_48x48);
        if (DashboardDataManager.getInstance().isBillAvailable()) {
            rootView.setVisibility(View.VISIBLE);
        } else {
            rootView.setVisibility(View.GONE);
        }
        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        mView = rootView;
    }

    private void createAction(Context context) {
        mAction = new BillMenuItemAction(context);
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(150).scaleX(scale);
        view.animate().setDuration(150).scaleY(scale);
        ObjectAnimator.ofFloat(mIcon, "elevation", view.getElevation(), elevation).setDuration(150).start();
    }

    private void onBillVisibilitySettingChanged(int value) {
        if (DashboardDataManager.getInstance().isBillAvailable()) {
            mView.setVisibility(View.VISIBLE);
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    private static class BillMenuItemAction implements Action {

        private Context mContext;

        private BillMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            Intent launchBillIntent = new Intent(Constants.BILL_ACTION_INTENT);
            launchBillIntent.setClassName(Constants.BILL_CLASS_INTENT, Constants.BILL_ACTIVITY_INTENT);
            launchBillIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivityAsUser(launchBillIntent, UserHandle.CURRENT_OR_SELF);
        }
    }

    private static class UiThreadHandler extends Handler {
        WeakReference<BillMenuItem> mBillMenuItemRef;
        private static final int MSG_WHAT_BILL_SETTING_CHANGED = 101;

        private UiThreadHandler(BillMenuItem billMenuItem) {
            super();
            mBillMenuItemRef = new WeakReference<>(billMenuItem);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_BILL_SETTING_CHANGED) {
                BillMenuItem billMenuItem = mBillMenuItemRef.get();
                if (billMenuItem != null) {
                    int value = msg.arg1;
                    billMenuItem.onBillVisibilitySettingChanged(value);
                }
                return;
            }
        }
    }
}
