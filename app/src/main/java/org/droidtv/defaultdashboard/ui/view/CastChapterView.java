package org.droidtv.defaultdashboard.ui.view;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.droidtv.defaultdashboard.DashboardActivity;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.TNCDetails;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialog;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogFooterButtonProp;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogInterface;
import org.droidtv.ui.tvwidget2k15.tvtoast.TvToast;
import org.droidtv.ui.tvwidget2k15.tvtoast.TvToastMessenger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by bhargava.gugamsetty on 28-03-2018.
 */

public class CastChapterView extends RelativeLayout implements DashboardDataManager.GoogleCastListener {

    private static final String TAG = "CastChapterView";

    private UiThreadHandler mUiThreadHandler;
    private DashboardDataManager mDashboardDataManager;
    private ViewGroup mSlidingTextContainer;
    private ViewGroup mSliderIndicatorContainer;


    private ViewGroup mBflSlidingTextContainer;
    private ViewGroup mBflSliderIndicatorContainer;

    private TextView mReadySlidingTextOne;
    private TextView mReadySlidingTextTwo;
    private TextView mReadySlidingTextThree;
    private TextView mReadySlidingTextFour;

    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private ValueAnimator mSliderIndicatorFadeAnimator;

    private TextView mNextIndicatorTextView;
    private ImageView mNextIndicatorImageView;
    private TextView mSlidingTextOne;
    private TextView mSlidingTextTwo;
    private TextView mSlidingTextThree;
    private TextView mSlidingTextFour;
    private Animation mNextIndicatorTranslationAnimation;

    private LinearLayout mCastConnectView;
    private Animation mCastConnectViewSlideInFromRightAnimation;
    private Animation mCastConnectViewSlideOutToLeftAnimation;
    private Animation mCastConnectViewSlideInFromLeftAnimation;
    private LinearLayout mCastReadyView;
    private Animation mCastReadyViewSlideInFromRightAnimation;
    private Animation mCastReadyViewSlideOutToRightAnimation;

    private LinearLayout mCastIndicatorContainer;
    private Animation mCastIndicatorContainerFadeInAnimation;
    private View mCastConnectIndicator;
    private LayerDrawable mCastConnectIndicatorBackground;
    private View mCastReadyIndicator;
    private LayerDrawable mCastReadyIndicatorBackground;
    private ValueAnimator mCastConnectIndicatorFadeInAnimator;
    private ValueAnimator mCastConnectIndicatorFadeOutAnimator;
    private ValueAnimator mCastReadyIndicatorFadeInAnimator;
    private ValueAnimator mCastReadyIndicatorFadeOutAnimator;

    private LinearLayout mCastCompatibilityPanel;
    private Animation mCastCompatibilityPanelFadeInAnimation;

    private Switch mCastCompatibilitySwitch;
    private TextView mCastCompatibilitySwitchTextViewOn;
    private TextView mCastCompatibilitySwitchTextViewOff;
    private TextView mCastCompatibilitySwitchToggleTitle;

    private int mCurrentCastView;
    private int mCurrentSliderTextViewIndex;
    private int mCurrentBflSliderTextViewIndex;

    private Animation mSliderTextSlideIn;
    private Animation mSliderTextSlideOut;

    private TextView mNetworkNameTextView;
    private TextView mPasswordTextView;
    private ImageView mQrCodeImageView;

    private ProgressBar mPrepareSecureSharingProgressBar;

    private TvToastMessenger mTvToastMessenger;
    private TvToast mHotspotConnectedTvToast;
    private TvToast mHotspotDisconnectedTvToast;
    private TvToast mTNCNeedToBeAcceptedToast;
    private ModalDialog modalDialog;
    private ArrayList<String> mConnectedDeviceList , mParsedDeviceList= null;

    /**
     * Indicates whether the expanded cast view is currently being shown
     */
    private boolean mIsShowingCastInfo;
    private boolean mShowCastReadyView;
    private static final int CAST_CONNECT_VIEW = 1;
    private static final int CAST_READY_VIEW = 2;

    private static final int TOAST_TIMEOUT = 5000;
    private static final int SHOW_NEXT_SLIDER_VIEW_DELAY = 5000;
    private static final int SHOW_SLIDER_ANIMATION_DURATION = 750;
    private static final int SHOW_SCREEN_DELAY = 6000;

    public CastChapterView(Context context) {
        this(context, null);
    }

    public CastChapterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CastChapterView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CastChapterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        mDashboardDataManager = DashboardDataManager.getInstance();

        boolean isSecureSharing = isSecureSharingEnabled();

        LayoutInflater.from(getContext()).inflate(R.layout.view_cast_chapter, this);
        setFocusable(true);

        mUiThreadHandler = new UiThreadHandler(new WeakReference<CastChapterView>(this));
        mIsShowingCastInfo = false;

        int sidePanelBackgroundColor = DashboardDataManager.getInstance().getSidePanelBackgroundColor();
        int sidePanelHighlightedTextColor = DashboardDataManager.getInstance().getSidePanelHighlightedTextColor();
        int sidePanelNonHighlightedTextColor = DashboardDataManager.getInstance().getSidePanelNonHighlightedTextColor();

        Drawable castViewBackground = getContext().getDrawable(R.drawable.cast_connect_view_circle_background);
        castViewBackground.setTint(sidePanelBackgroundColor);

        initSliderViews();
        initSliderIndicators();

        initTvToastMessenger();

        mNextIndicatorTextView = (TextView) findViewById(R.id.next_indicator_text_view);
        mNextIndicatorImageView = (ImageView) findViewById(R.id.next_indicator);

        if (isRtl()) {
            float rotation = 180;
            mNextIndicatorImageView.setRotation(rotation);
        }

        mFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cast_chapter_fade_in);
        mFadeInAnimation.setAnimationListener(mSlidingTextContainerFadeInAnimationListener);

        mSliderTextSlideIn = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.slider_text_slide_in_from_left) :
                AnimationUtils.loadAnimation(getContext(), R.anim.slider_text_slide_in_from_right);
        mSliderTextSlideIn.setAnimationListener(mSliderTextSlideInAnimationListener);
        mSliderTextSlideOut = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.slider_text_slide_out_to_right) :
                AnimationUtils.loadAnimation(getContext(), R.anim.slider_text_slide_out_to_left);
        mSliderTextSlideOut.setAnimationListener(mSliderTextSlideOutAnimationListener);

        mFadeOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cast_chapter_fade_out);
        mFadeOutAnimation.setAnimationListener(mSlidingTextContainerFadeOutAnimationListener);

        mNextIndicatorTranslationAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cast_indicator_anim);
        mNextIndicatorImageView.startAnimation(mNextIndicatorTranslationAnimation);
        mCastConnectView = isSecureSharing ? (LinearLayout) findViewById(R.id.cast_connect_container_secure_sharing_enabled) :
                (LinearLayout) findViewById(R.id.cast_connect_container_secure_sharing_disabled);
        mCastConnectView.setBackground(castViewBackground);
        mCastReadyView = (LinearLayout) findViewById(R.id.cast_ready_container);
        mCastReadyView.setBackground(castViewBackground);

        mCastIndicatorContainer = (LinearLayout) findViewById(R.id.cast_view_indicator_container);
        mCastConnectIndicator = findViewById(R.id.cast_connect_indicator);
        mCastConnectIndicatorBackground = (LayerDrawable) getContext().getDrawable(R.drawable.cast_indicator_drawable);
        mCastConnectIndicator.setBackground(mCastConnectIndicatorBackground);
        mCastReadyIndicator = findViewById(R.id.cast_ready_indicator);
        mCastReadyIndicatorBackground = (LayerDrawable) getContext().getDrawable(R.drawable.cast_indicator_drawable);
        mCastReadyIndicatorBackground.getDrawable(1).setAlpha(0);
        mCastReadyIndicator.setBackground(mCastReadyIndicatorBackground);

        if(mDashboardDataManager.isBFLProduct() && !isSecureSharingEnabled()){
            mCastConnectView.setVisibility(GONE);
            mCastConnectIndicator.setVisibility(GONE);
            mCastReadyIndicator.setVisibility(GONE);
        }

        mCastCompatibilityPanel = (LinearLayout) findViewById(R.id.compatibility_panel);
        mCastCompatibilityPanel.setBackgroundColor(sidePanelBackgroundColor);

        mCastCompatibilitySwitch = (Switch) findViewById(R.id.compatibility_panel_switch);

        mCastCompatibilitySwitchTextViewOn = (TextView) findViewById(R.id.compatibility_panel_switch_on);
        mCastCompatibilitySwitchTextViewOn.setTextColor(sidePanelHighlightedTextColor);
        mCastCompatibilitySwitchTextViewOff = (TextView) findViewById(R.id.compatibility_panel_switch_off);
        mCastCompatibilitySwitchTextViewOff.setTextColor(sidePanelNonHighlightedTextColor);

        updateSwitchToggleStateText();

        mCastCompatibilitySwitchToggleTitle = (TextView) findViewById(R.id.compatibility_panel_toggle_title);
        mCastCompatibilitySwitchToggleTitle.setTextColor(sidePanelNonHighlightedTextColor);

        mCastConnectViewSlideInFromRightAnimation = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_in_from_left) :
                AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_in_from_right);
        mCastConnectViewSlideInFromRightAnimation.setAnimationListener(mCastConnectViewSlideInFromRightAnimationListener);

        mCastConnectViewSlideOutToLeftAnimation = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_out_to_right) :
                AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_out_to_left);
        mCastConnectViewSlideOutToLeftAnimation.setAnimationListener(mCastConnectViewSlideOutToLeftAnimationListener);

        mCastConnectViewSlideInFromLeftAnimation = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_in_from_right) :
                AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_in_from_left);
        mCastConnectViewSlideInFromLeftAnimation.setAnimationListener(mCastConnectViewSlideInFromLeftAnimationListener);

        mCastReadyViewSlideInFromRightAnimation = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_in_from_left) :
                AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_in_from_right);
        mCastReadyViewSlideInFromRightAnimation.setAnimationListener(mCastReadyViewSlideInFromRightAnimationListener);

        mCastReadyViewSlideOutToRightAnimation = isRtl() ? AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_out_to_left) :
                AnimationUtils.loadAnimation(getContext(), R.anim.cast_view_slide_out_to_right);
        mCastReadyViewSlideOutToRightAnimation.setAnimationListener(mCastReadyViewSlideOutToRightAnimationListener);

        mCastCompatibilityPanelFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cast_chapter_fade_in);
        mCastCompatibilityPanelFadeInAnimation.setAnimationListener(mCastCompatibilityPanelFadeInAnimationListener);

        mCastIndicatorContainerFadeInAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.cast_chapter_fade_in);
        mCastIndicatorContainerFadeInAnimation.setAnimationListener(mCastIndicatorContainerFadeInAnimationListener);
        mCastConnectIndicatorFadeInAnimator = ValueAnimator.ofInt(0, 255);
        mCastConnectIndicatorFadeInAnimator.addUpdateListener(mCastConnectIndicatorFadeAnimatorUpdateListener);
        mCastConnectIndicatorFadeOutAnimator = ValueAnimator.ofInt(255, 0);
        mCastConnectIndicatorFadeOutAnimator.addUpdateListener(mCastConnectIndicatorFadeAnimatorUpdateListener);
        mCastReadyIndicatorFadeInAnimator = ValueAnimator.ofInt(0, 255);
        mCastReadyIndicatorFadeInAnimator.addUpdateListener(mCastReadyIndicatorFadeAnimatorUpdateListener);
        mCastReadyIndicatorFadeOutAnimator = ValueAnimator.ofInt(255, 0);
        mCastReadyIndicatorFadeOutAnimator.addUpdateListener(mCastReadyIndicatorFadeAnimatorUpdateListener);

        mSliderIndicatorFadeAnimator = ValueAnimator.ofInt(0, 255);
        mSliderIndicatorFadeAnimator.setDuration(SHOW_SLIDER_ANIMATION_DURATION);
        mSliderIndicatorFadeAnimator.addUpdateListener(mSliderIndicatorFadeAnimatorUpdateListener);

        mCurrentCastView = CAST_CONNECT_VIEW;

        mNetworkNameTextView = isSecureSharing ? (TextView) findViewById(R.id.cast_connect_network_name) :
                (TextView) findViewById(R.id.no_secure_sharing_cast_connect_network_name);
        mNetworkNameTextView.setTextColor(sidePanelHighlightedTextColor);
        if (!isSecureSharing && mDashboardDataManager.isGoogleCastWifiLoginEnabled()) {
            mNetworkNameTextView.setText(mDashboardDataManager.getWifiNetworkNameForGoogleCast());
        }

        mPasswordTextView = (TextView) findViewById(R.id.cast_connect_network_password);
        mPasswordTextView.setTextColor(sidePanelHighlightedTextColor);

        mQrCodeImageView = (ImageView) findViewById(R.id.cast_connect_qr_code_image);

        mPrepareSecureSharingProgressBar = (ProgressBar) findViewById(R.id.cast_secure_sharing_prepare_progress_bar);

        ImageView wifiIconImageView = (ImageView) findViewById(R.id.wifi_icon_image);
        Drawable wifiIcon = getContext().getDrawable(R.drawable.icon_14_network_n_48x48);
        wifiIcon.setTint(sidePanelHighlightedTextColor);
        wifiIconImageView.setImageDrawable(wifiIcon);

        ImageView googleCastIconImageView = (ImageView) findViewById(R.id.google_cast_icon_image);
        Drawable googleCastIcon = getContext().getDrawable(R.drawable.icon_1025_cast_n_48x48);
        googleCastIcon.setTint(sidePanelHighlightedTextColor);
        googleCastIconImageView.setImageDrawable(googleCastIcon);

        TextView compatibilityPanelIconTextView = (TextView) findViewById(R.id.compatibility_panel_icon);
        compatibilityPanelIconTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(), Constants.ICONO_FONT_PATH));
        compatibilityPanelIconTextView.setText(Constants.GOOGLE_CAST_ICON_UNICODE_CODE);
        compatibilityPanelIconTextView.setTextColor(sidePanelHighlightedTextColor);

        ImageView wifiIconImageViewNoSecureSharing = (ImageView) findViewById(R.id.no_secure_sharing_wifi_icon_image);
        wifiIconImageViewNoSecureSharing.setImageDrawable(wifiIcon);

        TextView castConnectTitle = (TextView) findViewById(R.id.cast_connect_title);
        if(mDashboardDataManager.isBFLProduct()){
            castConnectTitle.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_CONNECT_DEVICE_BUS));
        }
        castConnectTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView castConnectNetworkPasswordTitle = (TextView) findViewById(R.id.cast_connect_network_password_title);
        castConnectNetworkPasswordTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView castConnectQrCodeTitle = (TextView) findViewById(R.id.cast_connect_qr_code_title);
        castConnectQrCodeTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView noSecureSharingCastConnectTitle = (TextView) findViewById(R.id.no_secure_sharing_cast_connect_title);
        noSecureSharingCastConnectTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView noSecureSharingCastConnectDescription = (TextView) findViewById(R.id.no_secure_sharing_cast_connect_description);
        if(mDashboardDataManager.getInstance().isBFLProduct()){
            noSecureSharingCastConnectDescription.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_USE_WITH_SS_OFF_BUS));
        }
        noSecureSharingCastConnectDescription.setTextColor(sidePanelHighlightedTextColor);
        TextView noSecureSharingCastConnectNetworkName = (TextView) findViewById(R.id.no_secure_sharing_cast_connect_network_name);
        noSecureSharingCastConnectNetworkName.setTextColor(sidePanelHighlightedTextColor);

        TextView castReadyTitle = (TextView) findViewById(R.id.cast_ready_title);
        castReadyTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView castReadySubtitle = (TextView) findViewById(R.id.cast_ready_subtitle);
        castReadySubtitle.setText(isSecureSharing ?getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_CH_READY_TO_CAST):getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_CH_READY_TO_CAST_1));
        castReadySubtitle.setTextColor(sidePanelHighlightedTextColor);

        TextView compatibilityPanelGoogleCastTitle = (TextView) findViewById(R.id.compatibility_panel_google_cast_title);
        if(mDashboardDataManager.isBFLProduct()){
            compatibilityPanelGoogleCastTitle.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_CHROMECAST));
        }
        compatibilityPanelGoogleCastTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView compatibilityPanelTitle = (TextView) findViewById(R.id.compatibility_panel_title);
        compatibilityPanelTitle.setTextColor(sidePanelHighlightedTextColor);

        TextView compatibilityPanelDescription = (TextView) findViewById(R.id.compatibility_panel_description);
        compatibilityPanelDescription.setTextColor(sidePanelHighlightedTextColor);

        animateSlidingTextDelayed();
    }

    private Animation.AnimationListener mSlidingTextContainerFadeInAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            android.util.Log.d(TAG, "mSlidingTextContainerFadeInAnimationListener onAnimationEnd() called with: animation = [" + animation + "]");
            mSlidingTextContainer.setVisibility(VISIBLE);
            mSliderIndicatorContainer.setVisibility(VISIBLE);
            mNextIndicatorTextView.setVisibility(VISIBLE);
            mNextIndicatorImageView.setVisibility(VISIBLE);
            mNextIndicatorImageView.startAnimation(mNextIndicatorTranslationAnimation);
            animateSlidingTextDelayed();

            mBflSlidingTextContainer.setVisibility(GONE);
            mBflSliderIndicatorContainer.setVisibility(GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mSlidingTextContainerFadeOutAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            android.util.Log.d(TAG, "onAnimationEnd() called with: animation = [" + animation + "]");
            hideIntroElements();

            if (!mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_PREPARE_GOOGLE_HOTSPOT)) {
                sendMessage(UiThreadHandler.MSG_WHAT_PREPARE_GOOGLE_HOTSPOT);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mBflSlidingTextContainerFadeOutAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            hideIntroElements();
            if (!mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_PREPARE_GOOGLE_HOTSPOT)) {
                sendMessage(UiThreadHandler.MSG_WHAT_PREPARE_GOOGLE_HOTSPOT);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mCastConnectViewSlideInFromRightAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

            mCastConnectView.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrentCastView = CAST_CONNECT_VIEW;
            if(!isSecureSharingEnabled() && isGoogleCastWifiLoginEnabled()){
                showCastReadyViewDelayed();
            }
			
			if(isSecureSharingEnabled() && isDeviceConnected()){
			    mShowCastReadyView = false;
                mConnectedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
                mParsedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
                Message message = Message.obtain();
                DdbLogUtility.logMoreChapter(TAG, "********started mCastConnectViewSlideInFromRightAnimationListener onAnimationEnd:*****************");
                message.what = UiThreadHandler.MSG_WHAT_SHOW_TNC_AND_TOAST;
                message.obj = mConnectedDeviceList;
                mUiThreadHandler.sendMessage(message);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public void ShowTNCAndToastForConnectedDevices(){
        DdbLogUtility.logCastChapter(TAG, "ShowTNCAndToastForConnectedDevices: List size " + mConnectedDeviceList.size());
        String mCurrentDeviceDetail = mConnectedDeviceList.get(0);
        String mDeviceName = mCurrentDeviceDetail.split("::")[0];
        String mAddress = mCurrentDeviceDetail.split("::")[1];

        if(mDashboardDataManager.isTermAndConditionEnable()) {
            if (mDashboardDataManager.getTcStatus(mAddress)) {//Get TNC status for Address
                mShowCastReadyView = true;
                Message message = Message.obtain();
                message.what = UiThreadHandler.MSG_WHAT_SHOW_TOAST;
                message.obj = mDeviceName;
                mUiThreadHandler.sendMessage(message);
            } else {
                Message message = Message.obtain();
                message.what = UiThreadHandler.MSG_WHAT_SHOW_TNC;
                message.obj = mDeviceName;
                Bundle data = new Bundle();
                data.putString("address", mAddress);
                message.setData(data);
                mUiThreadHandler.sendMessage(message);
            }
        }else{
            mShowCastReadyView = true;
            Message message = Message.obtain();
            message.what = UiThreadHandler.MSG_WHAT_SHOW_TOAST;
            message.obj = mDeviceName;
            mUiThreadHandler.sendMessage(message);
        }
        mConnectedDeviceList.remove(mCurrentDeviceDetail);
    }

    private Animation.AnimationListener mCastConnectViewSlideOutToLeftAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mCastConnectView.setVisibility(INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {          
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mCastConnectViewSlideInFromLeftAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mCastConnectView.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrentCastView = CAST_CONNECT_VIEW;
            if(!isSecureSharingEnabled() && isGoogleCastWifiLoginEnabled()){
                showCastReadyViewDelayed();
            }
            if(isSecureSharingEnabled() && isDeviceConnected()){
                mShowCastReadyView = false;
                mConnectedDeviceList =(ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
                mParsedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
                Message message = Message.obtain();
                DdbLogUtility.logMoreChapter(TAG, "********started mCastConnectViewSlideInFromLeftAnimationListener onAnimationEnd:*****************");
                message.what = UiThreadHandler.MSG_WHAT_SHOW_TNC_AND_TOAST;
                message.obj = mConnectedDeviceList;
                mUiThreadHandler.sendMessage(message);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };


    private Animation.AnimationListener mCastReadyViewSlideInFromRightAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mCastReadyView.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrentCastView = CAST_READY_VIEW;
            if(!isSecureSharingEnabled() && isGoogleCastWifiLoginEnabled()){
                showCastConnectViewDelayed();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mCastReadyViewSlideOutToRightAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mCastReadyView.setVisibility(GONE);

        }

        @Override
        public void onAnimationEnd(Animation animation) {         
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mCastCompatibilityPanelFadeInAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            mCastCompatibilityPanel.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private ValueAnimator.AnimatorUpdateListener mCastConnectIndicatorFadeAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            mCastConnectIndicatorBackground.getDrawable(1).setAlpha((int) animator.getAnimatedValue());
        }
    };

    private ValueAnimator.AnimatorUpdateListener mCastReadyIndicatorFadeAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            mCastReadyIndicatorBackground.getDrawable(1).setAlpha((int) animator.getAnimatedValue());
        }
    };

    private Animation.AnimationListener mCastIndicatorContainerFadeInAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mCastIndicatorContainer.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mSliderTextSlideInAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            android.util.Log.d(TAG, "mSliderTextSlideInAnimationListener onAnimationStart() called with: animation = [" + animation + "]");
            int nextChildIndex = mCurrentSliderTextViewIndex + 1;
            if (nextChildIndex >= mSlidingTextContainer.getChildCount()) {
                nextChildIndex = 0;
            }
            mSlidingTextContainer.getChildAt(nextChildIndex).setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animaion) {
            android.util.Log.d(TAG, "mSliderTextSlideInAnimationListener onAnimationEnd() called with: animaion = [" + animaion + "]");
            int nextChildIndex = mCurrentSliderTextViewIndex + 1;
            if (nextChildIndex >= mSlidingTextContainer.getChildCount()) {
                nextChildIndex = 0;
            }
            mCurrentSliderTextViewIndex = nextChildIndex;

            // Schedule the next set of slide-out/slide-in animations after the slide-in animation for current slider view has completed
            animateSlidingTextDelayed();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mBflSliderTextSlideInAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            android.util.Log.d(TAG, "mBflSliderTextSlideInAnimationListener onAnimationStart() called with: animation = [" + animation + "]");
            int nextChildIndex = mCurrentBflSliderTextViewIndex + 1;
            if (nextChildIndex >= mBflSlidingTextContainer.getChildCount()) {
                nextChildIndex = 0;
            }
            mBflSlidingTextContainer.getChildAt(nextChildIndex).setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animaion) {
            android.util.Log.d(TAG, "mBflSliderTextSlideInAnimationListener onAnimationEnd() called with: animaion = [" + animaion + "]");
            int nextChildIndex = mCurrentBflSliderTextViewIndex + 1;
            if (nextChildIndex >= mBflSlidingTextContainer.getChildCount()) {
                nextChildIndex = 0;
            }
            mCurrentBflSliderTextViewIndex = nextChildIndex;

            // Schedule the next set of slide-out/slide-in animations after the slide-in animation for current slider view has completed
            animateBflSlidingTextDelayed();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mSliderTextSlideOutAnimationListener = new Animation.AnimationListener() {

        private int mCurrentViewIndex = 0;

        @Override
        public void onAnimationStart(Animation animation) {
            mCurrentViewIndex = mCurrentSliderTextViewIndex;
            android.util.Log.d(TAG, "mSliderTextSlideOutAnimationListener onAnimationStart() called with: animation = [" + animation + "]");
            // Animate the slider indicators as soon as slide-out animation starts
            animateSliderTextIndicator();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            android.util.Log.d(TAG, "mSliderTextSlideOutAnimationListener onAnimationEnd() called with: animation = [" + animation + "]");
            mSlidingTextContainer.getChildAt(mCurrentViewIndex).setVisibility(GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mBflSliderTextSlideOutAnimationListener = new Animation.AnimationListener() {

        private int mBflCurrentViewIndex = 0;

        @Override
        public void onAnimationStart(Animation animation) {
            mBflCurrentViewIndex = mCurrentBflSliderTextViewIndex;
            android.util.Log.d(TAG, "mBflSliderTextSlideOutAnimationListener onAnimationStart() called with: animation = [" + animation + "]");
            // Animate the slider indicators as soon as slide-out animation starts
            animateSliderTextIndicator();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            android.util.Log.d(TAG, "mBflSliderTextSlideOutAnimationListener onAnimationEnd() called with: animation = [" + animation + "]");
            mBflSlidingTextContainer.getChildAt(mBflCurrentViewIndex).setVisibility(GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private ValueAnimator.AnimatorUpdateListener mSliderIndicatorFadeAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            android.util.Log.d(TAG, "mSliderIndicatorFadeAnimatorUpdateListener onAnimationUpdate() called with: animator = [" + animator + "]");
            int nextChildIndex = mCurrentSliderTextViewIndex + 1;
            if (nextChildIndex >= mSlidingTextContainer.getChildCount()) {
                nextChildIndex = 0;
            }
            LayerDrawable nextSliderIndicatorBackground = (LayerDrawable) mSliderIndicatorContainer.getChildAt(nextChildIndex).getBackground();
            nextSliderIndicatorBackground.getDrawable(1).setAlpha((int) animator.getAnimatedValue());

            LayerDrawable currentSliderIndicatorBackground = (LayerDrawable) mSliderIndicatorContainer.getChildAt(mCurrentSliderTextViewIndex).getBackground();
            currentSliderIndicatorBackground.getDrawable(1).setAlpha(255 - (int) animator.getAnimatedValue());
        }
    };

    private ValueAnimator.AnimatorUpdateListener mBflSliderIndicatorFadeAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            android.util.Log.d(TAG, "mBflSliderIndicatorFadeAnimatorUpdateListener onAnimationUpdate() called with: animator = [" + animator + "]");
            int nextChildIndex = mCurrentBflSliderTextViewIndex + 1;
            if (nextChildIndex >= mBflSlidingTextContainer.getChildCount()) {
                nextChildIndex = 0;
            }
            LayerDrawable nextSliderIndicatorBackground = (LayerDrawable) mBflSliderIndicatorContainer.getChildAt(nextChildIndex).getBackground();
            nextSliderIndicatorBackground.getDrawable(1).setAlpha((int) animator.getAnimatedValue());

            LayerDrawable currentSliderIndicatorBackground = (LayerDrawable) mBflSliderIndicatorContainer.getChildAt(mCurrentBflSliderTextViewIndex).getBackground();
            currentSliderIndicatorBackground.getDrawable(1).setAlpha(255 - (int) animator.getAnimatedValue());
        }
    };

    /**
     * Show the expanded cast view
     */
    public void showCastInfo() {
        if (!mDashboardDataManager.isGoogleCastMyChoiceLocked()) {
            DdbLogUtility.logCastChapter(TAG, "showCastInfo() isGoogleCastMyChoiceLocked true");
            mDashboardDataManager.registerGoogleCastListener(this);
            registerShowCastReadyScreenReceiver();
            mIsShowingCastInfo = true;
            animateSlidingTextContainerHide();
            animateNextIndicatorHide();
        } else {
            DdbLogUtility.logCastChapter(TAG, "showCastInfo() isGoogleCastMyChoiceLocked false");
            hideIntroElements();
        }
    }

    /**
     * Hide the expanded cast view
     */
    public void hideCastInfo() {
        cleanUp();

        // Clear off all view animations and clear all messages in the handler specific to expanded cast view if it is no longer shown
        mIsShowingCastInfo = false;
        mUiThreadHandler.removeCallbacksAndMessages(null);
        mCastIndicatorContainer.clearAnimation();
        hideCastIndicators();
        mCastConnectView.clearAnimation();
        mCastConnectView.setVisibility(INVISIBLE);
        mCastReadyView.clearAnimation();
        mCastReadyView.setVisibility(GONE);
        mCastCompatibilityPanel.clearAnimation();
        hideCastCompatibilityPanel();
        hideProgressBar();

        resetSliderViews();
        resetSliderIndicators();

        animateSlidingTextContainerShow();
        animateNextIndicatorShow();
        DdbLogUtility.logCastChapter(TAG, "hideCastInfo");
    }

    public void cleanUp() {
        mDashboardDataManager.unregisterGoogleCastListener(this);
        unregisterShowCastReadyScreenReceiver();
        mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_SHOW_TNC_AND_TOAST);
        mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_SHOW_TNC);
        mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_SHOW_TOAST);
        if(modalDialog != null && modalDialog.isShowing()){
            modalDialog.dismiss();
            modalDialog = null;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (mCurrentCastView == CAST_CONNECT_VIEW && !isSecureSharingEnabled()) {
                    mCastConnectView.clearAnimation();
                    mCastReadyView.clearAnimation();
                    mUiThreadHandler.removeCallbacksAndMessages(null);
                    mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_SHOW_CAST_READY_VIEW);
                    sendMessage(UiThreadHandler.MSG_WHAT_SHOW_CAST_READY_VIEW);
                    return true;
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (mCurrentCastView == CAST_READY_VIEW) {
                    if (!isSecureSharingEnabled() && !isGoogleCastWifiLoginEnabled()) {
                        showGatewayWebPage();
                    } else {
                        mCastConnectView.clearAnimation();
                        mCastReadyView.clearAnimation();
                        mUiThreadHandler.removeCallbacksAndMessages(null);
                        mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_SHOW_CAST_CONNECT_VIEW);
                        sendMessage(UiThreadHandler.MSG_WHAT_SHOW_CAST_CONNECT_VIEW);
                    }
                    return true;
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                // Toggle the cast compatibility switch on OK/CENTER key press only if compatibility panel is showing
                if (mCastCompatibilityPanel.getVisibility() == VISIBLE) {
                    boolean checked = mCastCompatibilitySwitch.isChecked();
                    mCastCompatibilitySwitch.setChecked(!checked);
                    updateSwitchToggleStateText();
                    setHotspotCompatibilityMode(!checked);
                    return true;
                }
            }
        }

        return handled;
    }

    private void showNeedToConfirmationToast(){
        mTNCNeedToBeAcceptedToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
                getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_TNC_NEED_ACCEPT), -1);
        mTNCNeedToBeAcceptedToast.setTimeOutPeriod(TOAST_TIMEOUT);
        mTvToastMessenger.showTvToastMessage(mTNCNeedToBeAcceptedToast);
    }

    private void initSliderViews() {
        mSlidingTextContainer = (ViewGroup) findViewById(R.id.sliding_text_container);
        mSlidingTextOne = (TextView) findViewById(R.id.slider_text_1);
        mSlidingTextTwo = (TextView) findViewById(R.id.slider_text_2);
        mSlidingTextThree = (TextView) findViewById(R.id.slider_text_3);
        mSlidingTextFour = (TextView) findViewById(R.id.slider_text_4);
        initSliderTextView();
        resetSliderViews();
        mBflSlidingTextContainer = (ViewGroup) findViewById(R.id.ready_sliding_text_container);
        initBflSliderTextView();
        resetBflSliderViews();
    }

    private void initSliderTextView() {
        if (mDashboardDataManager.getInstance().isBFLProduct()) {
            Log.d("TPV_123", "initSliderTextView: BFL nerw strings");
            mSlidingTextOne.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_CAST_TEASER_TEXT_BUS_1));
            mSlidingTextTwo.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_CAST_TEASER_TEXT_BUS_2));
            mSlidingTextThree.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_CAST_TEASER_TEXT_BUS_3));
            mSlidingTextFour.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_CAST_TEASER_TEXT_4));
        }
    }

    private void resetSliderViews() {
        for (int i = 0; i < mSlidingTextContainer.getChildCount(); i++) {
            View sliderTextView = mSlidingTextContainer.getChildAt(i);
            sliderTextView.clearAnimation();
            sliderTextView.setVisibility(i == 0 ? VISIBLE : GONE);
        }
        mCurrentSliderTextViewIndex = 0;
    }

    private void resetBflSliderViews() {
        for (int i = 0; i < mBflSlidingTextContainer.getChildCount(); i++) {
            View sliderTextView = mBflSlidingTextContainer.getChildAt(i);
            sliderTextView.clearAnimation();
            sliderTextView.setVisibility(i == 0 ? VISIBLE : GONE);
        }
        mCurrentBflSliderTextViewIndex = 0;
    }

    private void initSliderIndicators() {
        android.util.Log.d(TAG, "initSliderIndicators() called");
        mSliderIndicatorContainer = (ViewGroup) findViewById(R.id.slider_indicator_container);
        resetSliderIndicators();
        mBflSliderIndicatorContainer = (ViewGroup) findViewById(R.id.ready_slider_indicator_container);
        resetReadySliderIndicators();
    }

    private void resetSliderIndicators() {
        android.util.Log.d(TAG, "resetSliderIndicators() called");
        int childCount = mSliderIndicatorContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View sliderIndicator = mSliderIndicatorContainer.getChildAt(i);
            LayerDrawable sliderIndicatorBackground = (LayerDrawable) getContext().getDrawable(R.drawable.cast_indicator_drawable);
            sliderIndicatorBackground.getDrawable(1).setAlpha(i == 0 ? 255 : 0);
            sliderIndicator.setBackground(sliderIndicatorBackground);
        }
    }
    private void resetReadySliderIndicators() {
        android.util.Log.d(TAG, "resetReadySliderIndicators() called");
        int childCount = mBflSliderIndicatorContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View sliderIndicator = mBflSliderIndicatorContainer.getChildAt(i);
            LayerDrawable sliderIndicatorBackground = (LayerDrawable) getContext().getDrawable(R.drawable.cast_indicator_drawable);
            sliderIndicatorBackground.getDrawable(1).setAlpha(i == 0 ? 255 : 0);
            sliderIndicator.setBackground(sliderIndicatorBackground);
        }
    }
    private void hideIntroElements() {
        mSlidingTextContainer.clearAnimation();
        mSlidingTextContainer.setVisibility(GONE);

        mSliderIndicatorFadeAnimator.end();
        mSliderIndicatorContainer.setVisibility(GONE);

        mNextIndicatorTextView.clearAnimation();
        mNextIndicatorTextView.setVisibility(GONE);

        mNextIndicatorImageView.clearAnimation();
        mNextIndicatorImageView.setVisibility(GONE);

        mCurrentSliderTextViewIndex = 0;
        if(mDashboardDataManager.isBFLProduct() && !isSecureSharingEnabled()){
           mBflSlidingTextContainer.setVisibility(VISIBLE);
           mBflSliderIndicatorContainer.setVisibility(VISIBLE);
           mSliderTextSlideIn.setAnimationListener(mBflSliderTextSlideInAnimationListener);
           mSliderTextSlideOut.setAnimationListener(mBflSliderTextSlideOutAnimationListener);
           mFadeOutAnimation.setAnimationListener(mBflSlidingTextContainerFadeOutAnimationListener);
           mSliderIndicatorFadeAnimator.addUpdateListener(mBflSliderIndicatorFadeAnimatorUpdateListener);
           animateBflSlidingTextDelayed();
        }
    }


    private void initTvToastMessenger() {
        mTvToastMessenger = TvToastMessenger.getInstance(getContext().getApplicationContext());
    }

    private void animateNextIndicatorShow() {
        mNextIndicatorTextView.startAnimation(mFadeInAnimation);
        mNextIndicatorImageView.startAnimation(mFadeInAnimation);
    }

    private void animateNextIndicatorHide() {
        mNextIndicatorTextView.startAnimation(mFadeOutAnimation);
        mNextIndicatorImageView.startAnimation(mFadeOutAnimation);
    }

    private void animateSlidingTextContainerShow() {
        mSlidingTextContainer.startAnimation(mFadeInAnimation);
    }

    private void animateSlidingTextContainerHide() {
        mSlidingTextContainer.startAnimation(mFadeOutAnimation);
    }

    private void animateSlidingTextDelayed() {
        // The sliding texts should not animate if expanded cast view is showing
        android.util.Log.d(TAG, "animateSlidingTextDelayed() called");
        if (mIsShowingCastInfo) {
            return;
        }

        mUiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSlidingTextContainer.getChildAt(mCurrentSliderTextViewIndex).startAnimation(mSliderTextSlideOut);
                // Post the next view slide in animation in a handler so that it is scheduled after slide out animation
                mUiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        android.util.Log.d(TAG, "animateSlidingTextDelayed run() called");
                        int nextChildIndex = mCurrentSliderTextViewIndex + 1;
                        if (nextChildIndex >= mSlidingTextContainer.getChildCount()) {
                            nextChildIndex = 0;
                        }
                        mSlidingTextContainer.getChildAt(nextChildIndex).startAnimation(mSliderTextSlideIn);
                    }
                });
            }
        }, SHOW_NEXT_SLIDER_VIEW_DELAY);
    }

    private void animateBflSlidingTextDelayed(){
        // The sliding texts should not animate if product type is HFL
        if (mDashboardDataManager.isHFLProduct()) {
            return;
        }

        mUiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("TPV_123","mCurrentBflSliderTextViewIndex: "+mCurrentBflSliderTextViewIndex);
                mBflSlidingTextContainer.getChildAt(mCurrentBflSliderTextViewIndex).startAnimation(mSliderTextSlideOut);
                // Post the next view slide in animation in a handler so that it is scheduled after slide out animation
                mUiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int nextChildIndex = mCurrentBflSliderTextViewIndex + 1;
                        Log.d("TPV_123","nextChildIndex: "+nextChildIndex);
                        Log.d("TPV_123","getChildCount(): "+mBflSlidingTextContainer.getChildCount());
                        if (nextChildIndex >= mBflSlidingTextContainer.getChildCount()) {
                            nextChildIndex = 0;
                        }
                        mBflSlidingTextContainer.getChildAt(nextChildIndex).startAnimation(mSliderTextSlideIn);
                    }
                });
            }
        }, SHOW_NEXT_SLIDER_VIEW_DELAY);
    }

    private void showCastConnectViewDelayed() {
        DdbLogUtility.logAppsChapter(TAG, "showCastConnectViewDelayed: mIsShowingCastInfo " + mIsShowingCastInfo);
        // The cast connected view should not animate if expanded cast view is not showing
        if (!mIsShowingCastInfo) {
            return;
        }

        mUiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showCastConnectView();
            }
        }, SHOW_SCREEN_DELAY);
    }

    private void showCastConnectView() {
        DdbLogUtility.logAppsChapter(TAG, "showCastConnectView: mIsShowingCastInfo " + mIsShowingCastInfo);
        animateCastReadyViewSlideOutToRight();
        animateCastConnectViewSlideInFromLeft();
        if (isSecureSharingEnabled() && mDashboardDataManager.isHotspotCompatibilityMode()) {
            showCastCompatibilityPanel();
        }
        animateCastConnectIndicatorFadeIn();
        animateCastReadyIndicatorFadeOut();
    }

    private void showCastReadyViewDelayed() {
        DdbLogUtility.logCastChapter(TAG, "showCastReadyViewDelayed: mIsShowingCastInfo " + mIsShowingCastInfo);
        // The cast ready view should not animate if expanded cast view is not showing
        if (!mIsShowingCastInfo) {
            return;
        }

        mUiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeHotspotConnectedToast();
                showCastReadyView();
            }
        }, SHOW_SCREEN_DELAY);
    }

    private void showCastReadyView() {
        if(mDashboardDataManager.isHFLProduct() || isSecureSharingEnabled()){
            hideCastCompatibilityPanel();
            animateCastConnectViewSlideOutToLeft();
            animateCastReadyViewSlideInFromRight();
            animateCastConnectIndicatorFadeOut();
            animateCastReadyIndicatorFadeIn();
        }
        if(mDashboardDataManager.getInstance().isBFLProduct()){
            DdbLogUtility.logCastChapter(TAG,"initBflSliderTextView: BFL new strings");
            mBflSlidingTextContainer.setVisibility(VISIBLE);
            mBflSliderIndicatorContainer.setVisibility(VISIBLE);
            mSliderTextSlideIn.setAnimationListener(mBflSliderTextSlideInAnimationListener);
            mSliderTextSlideOut.setAnimationListener(mBflSliderTextSlideOutAnimationListener);
            mFadeOutAnimation.setAnimationListener(mBflSlidingTextContainerFadeOutAnimationListener);
            mSliderIndicatorFadeAnimator.addUpdateListener(mBflSliderIndicatorFadeAnimatorUpdateListener);
            animateBflSlidingTextDelayed();
        }
    }

    private void initBflSliderTextView(){
        if(mDashboardDataManager.getInstance().isBFLProduct()){
            DdbLogUtility.logCastChapter(TAG,"initBflSliderTextView: BFL new strings");

            mReadySlidingTextOne =  (TextView) findViewById(R.id.ready_slider_text_1);
            mReadySlidingTextTwo =  (TextView) findViewById(R.id.ready_slider_text_2);
            mReadySlidingTextThree =  (TextView) findViewById(R.id.ready_slider_text_3);
            mReadySlidingTextFour =  (TextView) findViewById(R.id.ready_slider_text_4);

            mReadySlidingTextOne.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_READY_TO_CAST_TEASER_TEXT_BUS_1));
            mReadySlidingTextTwo.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_READY_TO_CAST_TEASER_TEXT_BUS_2));
            if(null != mDashboardDataManager.getDeviceName()){
                mReadySlidingTextThree.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_READY_TO_CAST_TEASER_TEXT_BUS_3)
                        .replace("^1" , mDashboardDataManager.getDeviceName()));
            }else{
                mReadySlidingTextThree.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_READY_TO_CAST_TEASER_TEXT_BUS_3)
                        .replace("^1" , ""));
            }
            mReadySlidingTextFour.setText(getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_READY_TO_CAST_TEASER_TEXT_BUS_4));
        }
    }

    private void animateCastConnectViewSlideInFromRight() {
        mCastConnectView.startAnimation(mCastConnectViewSlideInFromRightAnimation);
    }

    private void animateCastConnectViewSlideOutToLeft() {
        mCastConnectView.startAnimation(mCastConnectViewSlideOutToLeftAnimation);
    }

    private void animateCastConnectViewSlideInFromLeft() {
        mCastConnectView.startAnimation(mCastConnectViewSlideInFromLeftAnimation);
    }

    private void animateCastReadyViewSlideInFromRight() {
        mCastReadyView.startAnimation(mCastReadyViewSlideInFromRightAnimation);
    }

    private void animateCastReadyViewSlideOutToRight() {
        mCastReadyView.startAnimation(mCastReadyViewSlideOutToRightAnimation);
    }

    private void showCastCompatibilityPanel() {
        mCastCompatibilityPanel.startAnimation(mCastCompatibilityPanelFadeInAnimation);
    }

    private void hideCastCompatibilityPanel() {
        mCastCompatibilityPanel.setVisibility(GONE);
    }

    private void animateCastIndicatorsFadeIn() {
        mCastIndicatorContainer.startAnimation(mCastIndicatorContainerFadeInAnimation);
    }

    private void hideCastIndicators() {
        mCastIndicatorContainer.setVisibility(GONE);
        mCastConnectIndicatorBackground.getDrawable(1).setAlpha(255);
        mCastReadyIndicatorBackground.getDrawable(1).setAlpha(0);
    }

    private void animateCastConnectIndicatorFadeIn() {
        mCastConnectIndicatorFadeInAnimator.start();
    }

    private void animateCastConnectIndicatorFadeOut() {
        mCastConnectIndicatorFadeOutAnimator.start();
    }

    private void animateCastReadyIndicatorFadeIn() {
        mCastReadyIndicatorFadeInAnimator.start();
    }

    private void animateCastReadyIndicatorFadeOut() {
        mCastReadyIndicatorFadeOutAnimator.start();
    }

    private void updateSwitchToggleStateText() {
        if (mCastCompatibilitySwitch.isChecked()) {
            mCastCompatibilitySwitchTextViewOn.setVisibility(VISIBLE);
            mCastCompatibilitySwitchTextViewOff.setVisibility(INVISIBLE);
        } else {
            mCastCompatibilitySwitchTextViewOn.setVisibility(INVISIBLE);
            mCastCompatibilitySwitchTextViewOff.setVisibility(VISIBLE);
        }
    }

    private void animateSliderTextIndicator() {
        mSliderIndicatorFadeAnimator.start();
    }

    private void prepareGoogleCast() {
        if (isSecureSharingEnabled()) {
            showProgressBar();
            mDashboardDataManager.initHotSpot();
        } else {
            if (isGoogleCastWifiLoginEnabled()) {
                // show wifi login
                animateCastConnectViewSlideInFromRight();
                animateCastIndicatorsFadeIn();
            } else {
                // show gateway url webpage
                showGatewayWebPage();
            }
        }
    }

    private boolean isRtl() {
        Configuration config = getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private boolean isSecureSharingEnabled() {
        return mDashboardDataManager.isSecureSharingEnabled();
    }

    private boolean isGoogleCastWifiLoginEnabled() {
        return mDashboardDataManager.isGoogleCastWifiLoginEnabled();
    }

    private void setHotspotCompatibilityMode(boolean mode) {
        mDashboardDataManager.setHotspotCompatibityMode(mode);
    }

    private void setTcStatus(String macAddr, int termsStatus){
        mDashboardDataManager.setTcStatus(macAddr,termsStatus);
    }

    private boolean getTcStatus(String macAddress){
        return mDashboardDataManager.getTcStatus(macAddress);
    }

    private void updateHotspotCredentialsUi(String networkName, String passphrase, Bitmap qrCodeBitmap) {
        mNetworkNameTextView.setText(networkName);
        mNetworkNameTextView.requestFocus();
        mPasswordTextView.setText(passphrase);
        mQrCodeImageView.setImageBitmap(qrCodeBitmap);
        mPasswordTextView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName("");
                StringBuilder passwordStr = new StringBuilder();
                passwordStr.append("<speak>");
                char c;
                for(int i=0; i<passphrase.length(); i++){
                    String passwordChar;
                    c = passphrase.charAt(i);
                    if(Character.isUpperCase(c)){
                        passwordChar = "Capital "+c;
                    }else{
                        passwordChar=""+c;
                    }
                    passwordChar+="<break time=\"500ms\"></break>";
                    passwordStr.append(passwordChar);
                }
                passwordStr.append("</speak>");
                info.setContentDescription(passwordStr);
            }
        });
    }

    private void showHotspotConnectedToast() {
		if(mHotspotConnectedTvToast == null){
			 ArrayList<String> connectedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
			if(connectedDeviceList != null && connectedDeviceList.size() >0){
				String connectedDeviceName = connectedDeviceList.get(0);
				if ( connectedDeviceName == null || connectedDeviceName.equalsIgnoreCase("null") || connectedDeviceName.equals("*") || connectedDeviceName.length() == 0) {
				mHotspotConnectedTvToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
						getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MIRACAST_CONNECTED_OSD_INFO_TEXT), -1);
				} else {
				mHotspotConnectedTvToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
						getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_CONNECT_OK, connectedDeviceName), -1);
				}
                mHotspotConnectedTvToast.setTimeOutPeriod(TOAST_TIMEOUT);
		}
		}
        mTvToastMessenger.showTvToastMessage(mHotspotConnectedTvToast);
    }

    private void removeHotspotConnectedToast() {
        mTvToastMessenger.cancelTvToastMessage(mHotspotConnectedTvToast);
    }

    private void showHotspotDisconnectedToast() {
        mTvToastMessenger.showTvToastMessage(mHotspotDisconnectedTvToast);
    }

    private void removeHotspotDisconnectedToast() {
        mTvToastMessenger.cancelTvToastMessage(mHotspotDisconnectedTvToast);
    }

    private void showProgressBar() {
        mPrepareSecureSharingProgressBar.setVisibility(VISIBLE);
    }

    private void hideProgressBar() {
        mPrepareSecureSharingProgressBar.setVisibility(GONE);
    }

    public boolean isDeviceConnected(){
       ArrayList<String> mConnectedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();       
       if(mConnectedDeviceList ==  null || mConnectedDeviceList.size() == 0){
           DdbLogUtility.logCastChapter(TAG, "isDeviceConnected() false");
            return false;
       }
        DdbLogUtility.logCastChapter(TAG, "isDeviceConnected() true");
       return  true;
    }

    private void showGatewayWebPage() {
        String url = mDashboardDataManager.getGatewayUrlForGoogleCast();
        DdbLogUtility.logCastChapter(TAG, "showGatewayWebPage() url " + url);
        try {
            Intent intent = new Intent(Constants.ACTION_CAST_WIZARD);
            intent.putExtra(Constants.EXTRA_MODE, Constants.CAST_MODE_GOOGLE_CAST_WIZARD_ACTIVITY);
            intent.putExtra(Constants.EXTRA_LAUNCH_BY_DDB, true);
            intent.putExtra(Constants.EXTRA_PRCS_MY_CHOICE, "");
            ((DashboardActivity) getContext()).startActivityForResult(intent, Constants.REQUEST_CODE_ACTIVITY_GATEWAY_PAGE);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "#### no activity found to open cast gateway url:" + url);
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }

    private void registerShowCastReadyScreenReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_SHOW_CAST_READY_SCREEN);
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).registerReceiver(mShowCastReadyScreenReceiver, intentFilter);
    }

    private void unregisterShowCastReadyScreenReceiver() {
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).unregisterReceiver(mShowCastReadyScreenReceiver);
    }

    private BroadcastReceiver mShowCastReadyScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showCastReadyView();
        }
    };

    @Override
    public void onHotspotInit() {
        Log.d(TAG, "#### onHotspotInit()");
        mDashboardDataManager.startHotSpot();
    }

    @Override
    public void onHotspotStarted(String networkName, String passphrase, Bitmap qrCodeBitmap) {
        Log.d(TAG, "#### onHotspotStarted().networkName:" + networkName + ",passphrase:" + passphrase);
        hideProgressBar();
        updateHotspotCredentialsUi(networkName, passphrase, qrCodeBitmap);
        if (isSecureSharingEnabled() && mDashboardDataManager.isHotspotCompatibilityMode()) {
            if (mCastCompatibilityPanel.getVisibility() != VISIBLE) {
                showCastCompatibilityPanel();
            }
        }
        animateCastConnectViewSlideInFromRight();
        animateCastIndicatorsFadeIn();
    }

    @Override
    public void onHotspotStopped() {
        Log.d(TAG, "#### onHotspotStopped()");
    }

    @Override
    public void onHotspotConnecting() {
        Log.d(TAG, "#### onHotspotConnecting()");
    }

    @Override
    public void onHotspotConnected (String deviceName, String address, boolean isShowTerms) {
        Log.d(TAG, "#### onHotspotConnected().deviceName:" + deviceName);
        if(mConnectedDeviceList == null && mParsedDeviceList == null){//First time connection of device usecase
            DdbLogUtility.logCastChapter(TAG ,"This is usecase that device connected late by user or first time ");
            mConnectedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
            mParsedDeviceList = (ArrayList<String>) mDashboardDataManager.getClientDeviceNameList();
        }
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_DEVICE_CONNECTED);
        message.obj = deviceName;
        Bundle data = new Bundle();
        data.putString( "address", address);
        data.putBoolean( "isShowTerms", isShowTerms );
        message.setData(data);
        mUiThreadHandler.sendMessage(message);
    }

    @Override
    public void onHotspotDisconnected(String deviceName) {
        Log.d(TAG, "#### onHotspotDisconnected().deviceName:" + deviceName);
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_DEVICE_DISCONNECTED);
        message.obj = deviceName;
        mUiThreadHandler.sendMessage(message);
    }

    @Override
    public void onHotspotError(int errorCode) {
        Log.d(TAG, "#### onHotspotError().errorCode:" + errorCode);
        hideProgressBar();
    }
    
    private void onDeviceDisconnected(String deviceName){
        mHotspotDisconnectedTvToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
                getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_PTA_MIRACAST_CONNECTION_ERROR_TEXT), -1);
        mHotspotDisconnectedTvToast.setTimeOutPeriod(TOAST_TIMEOUT);
        showHotspotDisconnectedToast();
        // If we are in Cast Ready screen, then we need to go back to Cast Connect screen
        if (mCurrentCastView == CAST_READY_VIEW) {
            mCastConnectView.clearAnimation();
            mCastReadyView.clearAnimation();
            mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_SHOW_CAST_CONNECT_VIEW);
            sendMessage(UiThreadHandler.MSG_WHAT_SHOW_CAST_CONNECT_VIEW);
        }else if(mCurrentCastView == CAST_CONNECT_VIEW){
            mUiThreadHandler.removeCallbacksAndMessages(null);
        }

        // The toast showing disconnected message should be dismissed after a delay
        mUiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeHotspotDisconnectedToast();
            }
        }, TOAST_TIMEOUT);
    }

    //CR:TF519PHIEUMTK06-150
    public void showTNCConfirmationDialog(final String deviceName , final String address) {
        try {
            DdbLogUtility.logCastChapter(TAG, "showTNCConfirmationDialog: called for "+deviceName);
            ModalDialogFooterButtonProp mDeclineBtn = null, mAcceptBtn = null;
            modalDialog = null;
            TNCDetails tncDetails = mDashboardDataManager.getTNCDetails();
            if(tncDetails == null){
                return;
            }
            ModalDialog.Builder builder = new ModalDialog.Builder(getContext(), ModalDialog.HEADING_TYPE_DEFAULT);
            builder.setHeading((tncDetails.mMessageTitle), "");
            builder.setMessage("");

            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.view_terms_and_condition_message_layout, null);
            final TextView tcDeatils = (TextView) view.findViewById(R.id.tncTextView);

            tcDeatils.setText(tncDetails.mMessageBody);
            tcDeatils.setMovementMethod(new ScrollingMovementMethod());
            builder.setView(view);
            ModalDialogInterface.OnClickListener onDeclineClickListener = new ModalDialogInterface.OnClickListener() {
                @Override
                public void onClick(ModalDialogInterface modalDialogInterface, int i) {
                    DdbLogUtility.logCastChapter(TAG,"Decline clicked");
                    showNeedToConfirmationToast();
                    setTcStatus(address , 0);
                    //If mShowCastReadyView ==true and new device added of earlier scanned then no need to process other devices as it is from OnHotSpotConnected
                    //when all devices connected
                    if(!mShowCastReadyView || mConnectedDeviceList.size() >= 0){
                        Message message = Message.obtain();
                        message.what = UiThreadHandler.MSG_WHAT_SHOW_TNC_AND_TOAST;
                        message.obj = mConnectedDeviceList;
                        mUiThreadHandler.sendMessage(message);
                    }
                    modalDialog.dismiss();
                }
            };
            ModalDialogInterface.OnClickListener onAcceptClickListener = new ModalDialogInterface.OnClickListener() {
                @Override
                public void onClick(ModalDialogInterface modalDialogInterface, int i) {
                    DdbLogUtility.logCastChapter(TAG,"Accept clicked");
                    mShowCastReadyView = true;
                    showConnectedToast(deviceName);
                    setTcStatus(address , 1);
                    modalDialog.dismiss();
                }
            };
            mDeclineBtn = builder.setButton(ModalDialog.BUTTON_MID_RIGHT, getContext().getString(org.droidtv.ui.strings.R.string.MAIN_BUTTON_DECLINE), true, onDeclineClickListener);
            mAcceptBtn = builder.setButton(ModalDialog.BUTTON_RIGHT, getContext().getString(org.droidtv.ui.strings.R.string.MAIN_BUTTON_ACCEPT), true, onAcceptClickListener);
            modalDialog = builder.build();

            tcDeatils.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    Log.d(TAG ,"Old Y :"+oldScrollY + "Scroll Y :"+scrollY +"Height :"+v.getHeight());
                    if(v.getHeight() == scrollY){
                        DdbLogUtility.logCastChapter(TAG, "onScrollChange: scroll completed");
                        
                    }
                }
            });

            LinearLayout group = (LinearLayout) view.getParent();
            View replace = group.getChildAt(2);
            if (view != null && replace instanceof RelativeLayout ) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(60, 0, 60, 0);
                view.setLayoutParams(layoutParams);
                view.setPadding(0,4,0,24);
                view.setId(replace.getId());
                int index = group.indexOfChild(replace);
                group.removeView(replace);
                group.addView(view, index);
            }
            modalDialog.show();
            mAcceptBtn.requestFocus();
            modalDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if(modalDialog != null && modalDialog.isShowing()) {
                            modalDialog.dismiss();
                            setTcStatus(address , 0);
                            showNeedToConfirmationToast();
                            return true;
                        }
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            Log.d(TAG ,"something went wrong :"+e.getLocalizedMessage());
        }
    }

    private void showConnectedToast(String deviceName){
        DdbLogUtility.logCastChapter(TAG, "showConnectedToast: called for "+deviceName);
        if (deviceName == null || deviceName.equalsIgnoreCase("null") || deviceName.equals("*") || deviceName.length() == 0) {
            mHotspotConnectedTvToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
                    getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MIRACAST_CONNECTED_OSD_INFO_TEXT), -1);
        } else {
            mHotspotConnectedTvToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
                    getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_GC_CONNECT_OK, deviceName), -1);
        }
        mHotspotConnectedTvToast.setTimeOutPeriod(TOAST_TIMEOUT);
        mTvToastMessenger.showTvToastMessage(mHotspotConnectedTvToast);
        //If mShowCastReadyView ==true and new device added of earlier scanned then no need to process other devices as it is from OnHotSpotConnected
        //when all devices connected
        if(mConnectedDeviceList != null && !mShowCastReadyView || mConnectedDeviceList.size() >= 0){
            Message message = Message.obtain();
            message.what = UiThreadHandler.MSG_WHAT_SHOW_TNC_AND_TOAST;
            message.obj = mConnectedDeviceList;
            mUiThreadHandler.sendMessageDelayed(message ,TOAST_TIMEOUT);
        }
    }

    private void sendMessage(int what) {
        Message message = Message.obtain(mUiThreadHandler, what);
        message.sendToTarget();
    }

    private static class UiThreadHandler extends Handler {

        private WeakReference<CastChapterView> mCastChapterViewRef;

        private static final int MSG_WHAT_PREPARE_GOOGLE_HOTSPOT = 100;
        private static final int MSG_WHAT_SHOW_CAST_CONNECT_VIEW = 101;
        private static final int MSG_WHAT_SHOW_CAST_READY_VIEW = 102;
        private static final int MSG_WHAT_DEVICE_CONNECTED= 103;
        private static final int MSG_WHAT_DEVICE_DISCONNECTED= 104;
        private static final int MSG_WHAT_SHOW_TNC_AND_TOAST= 105;
        private static final int MSG_WHAT_SHOW_TOAST= 106;
        private static final int MSG_WHAT_SHOW_TNC= 107;

        private UiThreadHandler(WeakReference<CastChapterView> castChapterViewRef) {
            super();
            mCastChapterViewRef = castChapterViewRef;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCastChapter(TAG, "what " + what);
            if (what == MSG_WHAT_PREPARE_GOOGLE_HOTSPOT) {
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    castChapterView.prepareGoogleCast();
                }
                return;
            }
            if (what == MSG_WHAT_SHOW_CAST_CONNECT_VIEW) {
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    castChapterView.showCastConnectView();
                }
                return;
            }
            if (what == MSG_WHAT_SHOW_CAST_READY_VIEW) {
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    castChapterView.showCastReadyView();
                }
                return;
            }
            if(what == MSG_WHAT_DEVICE_CONNECTED){
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    String deviceName = (String) msg.obj;
                    String address = msg.getData().getString("address");
                    boolean isShowTerms = msg.getData().getBoolean("isShowTerms");
                    //If ShowTerm then only Show TNC which interanlly show toast if connected else no Toast
                    if(isShowTerms) {
                        castChapterView.showTNCConfirmationDialog(deviceName, address);
                    }else{
                        castChapterView.showConnectedToast(deviceName);
                        castChapterView.mShowCastReadyView = true;
                    }
                    castChapterView.mConnectedDeviceList.remove(deviceName+"::"+address);
                }
                return;
            }
            if(what == MSG_WHAT_DEVICE_DISCONNECTED){
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    String deviceName = (String) msg.obj;
                    castChapterView.onDeviceDisconnected(deviceName);
                }
                return;
            }
            if(what == MSG_WHAT_SHOW_TNC_AND_TOAST){
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    ArrayList<String> deviceList = (ArrayList<String>) msg.obj;
                    if(deviceList != null && deviceList.size() != 0) {
                        castChapterView.ShowTNCAndToastForConnectedDevices();
                    }else {
                        DdbLogUtility.logCastChapter(TAG ,"Corner usecase handling for new device if any got connected during this course ");
                        //Possibility that new device got connected so fetch list again and remove the already shown devices
                        ArrayList<String> mCurrentDeviceList = (ArrayList<String>) castChapterView.mDashboardDataManager.getClientDeviceNameList();
                        mCurrentDeviceList.removeAll(castChapterView.mParsedDeviceList);//Remove already parsed list to check any new device connected meanwhile
                        castChapterView.mParsedDeviceList.addAll(mCurrentDeviceList);//Add new devices in this variable
                        if(mCurrentDeviceList.size() == 0 && castChapterView.mShowCastReadyView){
                            Message message = Message.obtain();
                            message.what = UiThreadHandler.MSG_WHAT_SHOW_CAST_READY_VIEW;
                            castChapterView.mUiThreadHandler.sendMessage(message);
                            castChapterView.mShowCastReadyView = false;
                            if(castChapterView.mParsedDeviceList != null) {
                                castChapterView.mParsedDeviceList.clear();
                                castChapterView.mConnectedDeviceList.clear();
                                DdbLogUtility.logCastChapter(TAG ,"Clear all connected and displayed list of devices to user ");
                            }
                        }else{
                            //As the value of parsed and currrent list is not zero so new device we need to show the TNC
                            if(castChapterView.mConnectedDeviceList == null){
                                castChapterView.mConnectedDeviceList= new ArrayList<>();
                            }
                            castChapterView.mConnectedDeviceList.addAll(mCurrentDeviceList);
                            Message message = Message.obtain();
                            message.what = UiThreadHandler.MSG_WHAT_SHOW_TNC_AND_TOAST;
                            message.obj = castChapterView.mConnectedDeviceList;
                            castChapterView.mUiThreadHandler.sendMessage(message);
                        }
                    }
                }
                return;
            }

            if(what == MSG_WHAT_SHOW_TOAST){
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    String deviceName = (String) msg.obj;
                    castChapterView.showConnectedToast(deviceName);
                }
                return;
            }
            if(what == MSG_WHAT_SHOW_TNC){
                CastChapterView castChapterView = mCastChapterViewRef.get();
                if (castChapterView != null) {
                    String deviceName = (String) msg.obj;
                    String address = msg.getData().getString("address");
                    castChapterView.showTNCConfirmationDialog(deviceName ,address);
                }
                return;
            }
        }
    }
}
