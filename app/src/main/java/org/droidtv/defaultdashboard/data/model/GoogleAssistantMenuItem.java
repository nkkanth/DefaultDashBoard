package org.droidtv.defaultdashboard.data.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;
import org.droidtv.defaultdashboard.util.Constants;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class GoogleAssistantMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, DashboardDataManager.AssistantIconListener {

    private Action mAction;
    private View mView;
    private ImageView mIcon;
    private TextView mLabel;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private Context mContext;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;

    public GoogleAssistantMenuItem(Context context , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
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
			mLabel.setVisibility(View.VISIBLE);
            mLabel.animate().setDuration(150).alpha(1);
            FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
            labelLayoutParams.setMargins(0, (int) view.getContext().getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
            mIcon.setBackground(view.getContext().getDrawable(R.drawable.top_menu_background_rounded));

        } else {
            scaleAndElevate(mIcon, mDefaultScale, mDefaultElevation);
            mIcon.setBackgroundColor(view.getContext().getColor(android.R.color.transparent));
			FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
            labelLayoutParams.setMargins(0, (int) view.getContext().getResources().getDimension(R.dimen.top_menu_non_selected_label_margin_top), 0, 0);
            mLabel.setVisibility(View.INVISIBLE);
			mLabel.setTextSize(mContext.getResources().getDimension(R.dimen.top_menu_alarm_weather_text_size));
        }
        if(mTopMenuItemFocusChangeListner != null) {
            mTopMenuItemFocusChangeListner.OnItemFocus(view.getId() ,hasFocus);
        }
    }

    private void init(Context context) {
        mContext = context;
        initValues(context);
        createView(context);
        createAction(context);
		DashboardDataManager.getInstance().setAssistantIconListener(this);
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
        layoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
		FrameLayout rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.top_menu_item, null);
	
		mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
		int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        Drawable languageFlagDrawable = ContextCompat.getDrawable(mContext, R.drawable.assistant_logo_in_circle_96px);
		mIcon.setImageDrawable(languageFlagDrawable);
        
		
		mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);
		mLabel.setTextSize(context.getResources().getDimension(R.dimen.top_menu_selected_text_size));
		mLabel.setSelected(true);
		
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
        mLabel.setText(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_GOOGLE_ASSISTANT));

        rootView.setId(R.id.top_menu_assistant_view);
		rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        mView = rootView;
	
        if (DashboardDataManager.getInstance().getDefaultSharedPreferences().getBoolean(Constants.PREF_KEY_SHOW_ASSISTANT_ICON, false)) {
            rootView.setVisibility(View.VISIBLE);
        }else{
            rootView.setVisibility(View.GONE);
        }
    }

    private void createAction(Context context) {
        mAction = new LanguageMenuItemAction(context);
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(150).scaleX(scale);
        view.animate().setDuration(150).scaleY(scale);
        ObjectAnimator.ofFloat(mIcon, "elevation", view.getElevation(), elevation).setDuration(150).start();
    }

    @Override
    public void showAssistantIcon(boolean show) {
        if (show) {
            getView().setVisibility(View.VISIBLE);
        } else {
            getView().setVisibility(View.GONE);
        }
    }


    private static class LanguageMenuItemAction implements Action {

        private Context mContext;

        private LanguageMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            Intent launchIntent = new Intent();
            launchIntent.setAction("android.intent.action.ASSIST");
            launchIntent.addCategory("android.intent.category.DEFAULT");
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivityAsUser(launchIntent, UserHandle.CURRENT_OR_SELF);
        }
    }

}
