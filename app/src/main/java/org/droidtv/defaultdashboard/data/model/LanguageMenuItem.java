package org.droidtv.defaultdashboard.data.model;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.UserHandle;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.MenuLanguageActivity;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;

import androidx.core.content.ContextCompat;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class LanguageMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, DashboardDataManager.PbsSettingLanguageChangeListener {

    private Action mAction;
    private View mView;
    private ImageView mIcon;
    private TextView mLabel;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private int mLanguageFlagResourceId;
    private Context mContext;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;
    public LanguageMenuItem(Context context , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
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
            mIcon.setBackground(view.getContext().getDrawable(R.drawable.top_menu_background_rounded));
            mLabel.setVisibility(View.VISIBLE);
            mLabel.animate().setDuration(150).alpha(1);
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

    private void init(Context context) {
        mContext = context;
        DashboardDataManager.getInstance().registerPbsSettingLanguageChangeListener(this);
        initValues(context);
        createView(context);
        createAction(context);
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.top_menu_item_width),
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.top_menu_item, null);
        rootView.setId(R.id.top_menu_language_view);
        mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
        mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);

        Drawable languageFlagDrawable = null;

        mLanguageFlagResourceId = DashboardDataManager.getInstance().getSelectedLanguageResourceId();
        if (mLanguageFlagResourceId == -1) {
            languageFlagDrawable = ContextCompat.getDrawable(mContext, R.drawable.us);
        } else {
            languageFlagDrawable = ContextCompat.getDrawable(mContext, mLanguageFlagResourceId);
        }
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
        mLabel.setText(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_GUEST_MENU_LANG));
        mLabel.setTextSize(context.getResources().getDimension(R.dimen.top_menu_selected_text_size));
        mLabel.setSelected(true);
        layoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        mIcon.setImageDrawable(languageFlagDrawable);
        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        mView = rootView;
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
    public void onPbsSettingLanguageChange(int language) {
        if (mView != null && mView.getHandler() != null) {
            mView.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mLanguageFlagResourceId = DashboardDataManager.getInstance().getSelectedLanguageResourceId();
                    mIcon.setImageResource(mLanguageFlagResourceId);
                }
            });
        }
    }

    private static class LanguageMenuItemAction implements Action {

        private Context mContext;

        private LanguageMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
            Intent launchLanguageIntent = new Intent(mContext, MenuLanguageActivity.class);
            launchLanguageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivityAsUser(launchLanguageIntent, UserHandle.CURRENT_OR_SELF);
        }
    }
}
