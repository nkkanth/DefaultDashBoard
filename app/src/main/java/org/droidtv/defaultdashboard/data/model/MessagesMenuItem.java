package org.droidtv.defaultdashboard.data.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MessageCountChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MessageSettingsListener;
import org.droidtv.defaultdashboard.util.Constants;

import java.lang.ref.WeakReference;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class MessagesMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, MessageCountChangeListener, MessageSettingsListener {
    private Action mAction;
    private View mView;
    private TextView mLabel;
    private ImageView mIcon;
    private TextView mMessageCount;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private Context mContext;
    private UiThreadHandler mUiThreadHandler;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;

    public MessagesMenuItem(Context context , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
        mContext = context;
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
    public void onMessageCountChanged() {
        int messageCount = DashboardDataManager.getInstance().getMessageCount();
        setMessageCountText(messageCount);
    }

    @Override
    public void onMessageSettingsChanged() {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_MESSAGES_SETTING_CHANGED);
    }

    @Override
    public void onMessageSettingsChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_MESSAGES_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void init(Context context) {
        initValues(context);
        createView(context);
        createAction(context);
        mUiThreadHandler = new UiThreadHandler(this);
        DashboardDataManager.getInstance().registerMessageSettingsListener(this);
    }

    private void initValues(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.default_scale, typedValue, true);
        mDefaultScale = typedValue.getFloat();
        context.getResources().getValue(R.dimen.top_menu_item_focus_scale, typedValue, true);
        mFocusScale = typedValue.getFloat();

        mDefaultElevation = context.getResources().getDimension(R.dimen.default_elevation);
        mFocusElevation = context.getResources().getDimension(R.dimen.focus_elevation);
        DashboardDataManager.getInstance().addMessageCountChangeListener(this);
    }

    private void createView(Context context) {
        int messageCount = DashboardDataManager.getInstance().getMessageCount();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.top_menu_item_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.top_menu_item, null);
        rootView.setId(R.id.top_menu_messages_view);
        mMessageCount = new TextView(context);
        FrameLayout.LayoutParams messageCountLayoutParams = new FrameLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.top_menu_message_count_width_height), (int) context.getResources().getDimension(R.dimen.top_menu_message_count_width_height));
        mMessageCount.setGravity(Gravity.CENTER);
        messageCountLayoutParams.setMargins((int) context.getResources().getDimension(R.dimen.top_menu_message_count_margin_left), (int) context.getResources().getDimension(R.dimen.top_menu_message_count_margin_top), 0, 0);
        mMessageCount.setLayoutParams(messageCountLayoutParams);
        mMessageCount.setBackground(context.getDrawable(R.drawable.top_menu_message_count_background));
        mMessageCount.setTextAppearance(R.style.topmenuMessageCountTextAppearance);
        rootView.addView(mMessageCount);
        mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
        mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);

        mLabel.setText(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_WI_DASHBOARD_MESSAGES));
        mLabel.setTextSize(context.getResources().getDimension(R.dimen.top_menu_selected_text_size));
        mLabel.setSelected(true);
        layoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
        rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        setMessageCountText(messageCount);
        if (DashboardDataManager.getInstance().isMessageAvailable()) {
            rootView.setVisibility(View.VISIBLE);
        } else {
            rootView.setVisibility(View.GONE);
        }


        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        mView = rootView;
    }

    private void createAction(Context context) {
        mAction = new MessagesMenuItemAction(context);
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(150).scaleX(scale);
        view.animate().setDuration(150).scaleY(scale);
        mMessageCount.setPivotX(0);
        mMessageCount.setPivotY(mMessageCount.getHeight()/2);
        mMessageCount.animate().setDuration(150).scaleX(scale);
        mMessageCount.animate().setDuration(150).scaleY(scale);
        ObjectAnimator.ofFloat(mIcon, "elevation", mIcon.getElevation(), elevation).setDuration(150).start();
        ObjectAnimator.ofFloat(mMessageCount, "elevation", mMessageCount.getElevation(), elevation).setDuration(150).start();
    }

    private void onMessagesVisibilitySettingChanged(int value) {
        if (DashboardDataManager.getInstance().isMessageAvailable()) {
            int messageCount = DashboardDataManager.getInstance().getMessageCount();
            setMessageCountText(messageCount);
            mView.setVisibility(View.VISIBLE);
        } else {
            setMessageCountText(0);
            mView.setVisibility(View.GONE);
        }
    }

    private static class MessagesMenuItemAction implements Action {

        private Context mContext;

        private MessagesMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            Intent launchMessageIntent = new Intent(Constants.MESSAGES_ACTION_INTENT);
            launchMessageIntent.setClassName(Constants.MESSAGES_PACKAGE_INTENT, Constants.MESSAGES_ACTIVITY_INTENT);
            mContext.startActivityAsUser(launchMessageIntent, UserHandle.CURRENT_OR_SELF);
        }
    }

    private void setMessageCountText(int messageCount) {
        if (messageCount > 0) {
            mIcon.setImageResource(R.drawable.icon_397_message_n_48x48);
            mMessageCount.setVisibility(View.VISIBLE);
            if (messageCount > mContext.getResources().getInteger(R.integer.maximum_message_count_to_display)) {
                mMessageCount.setText(String.valueOf(mContext.getResources().getInteger(R.integer.maximum_message_count_to_display)));
            } else {
                mMessageCount.setText(String.valueOf(messageCount));
            }

        } else {
            mIcon.setImageResource(R.drawable.icon_397_message_n_48x48);
            mMessageCount.setVisibility(View.GONE);

        }
    }

    private static class UiThreadHandler extends Handler {
        WeakReference<MessagesMenuItem> mMessagesMenuItemRef;
        private static final int MSG_WHAT_MESSAGES_SETTING_CHANGED = 101;

        private UiThreadHandler(MessagesMenuItem messagesMenuItem) {
            super();
            mMessagesMenuItemRef = new WeakReference<>(messagesMenuItem);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_MESSAGES_SETTING_CHANGED) {
                MessagesMenuItem messagesMenuItem = mMessagesMenuItemRef.get();
                if (messagesMenuItem != null) {
                    messagesMenuItem.onMessagesVisibilitySettingChanged(msg.arg1);
                }
                return;
            }
        }
    }
}
