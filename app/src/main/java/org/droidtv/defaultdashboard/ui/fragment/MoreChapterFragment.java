package org.droidtv.defaultdashboard.ui.fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ClockSettingChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.WeatherSettingsListener;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.moreChapter.AmbilightSettingItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.BillItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.ClearHistoryItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.DashboardConfigurationSettingItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.InternetHotspotFeatureItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.ManageAccountItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.MenuLanguageSettingItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.MessageDisplaySettingsItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.MessagesItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.MoreChapterShelfItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.MyChoiceFeatureItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.PictureFormatSettingItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.PictureSettingItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.SetAlarmFeatureItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.SleepTimerFeatureItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.SoundSettingItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.TalkBackFeatureItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.TvGuideFeatureItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.WeatherFeatureItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.receiver.GuestCheckInStatusChangeReceiver;
import org.droidtv.defaultdashboard.ui.presenter.MoreChapterShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.MoreChapterShelfRowPresenter;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;

import java.lang.ref.WeakReference;
import java.util.Set;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.SinglePresenterSelector;

import static org.droidtv.defaultdashboard.util.Constants.INTENT_ACTION_GUEST_CHECK_IN_STATUS_CHANGE;
import static org.droidtv.defaultdashboard.util.Constants.INTENT_ACTION_GUEST_CHECK_OUT_STATUS_CHANGE;


/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public class MoreChapterFragment extends ChapterFragment implements ClockSettingChangeListener, WeatherSettingsListener, DashboardDataManager.GuestCheckInStatusChangeListener,DashboardDataManager.AccountIconListener {

    private static final String TAG = "MoreChapterFragment";

    private DashboardDataManager mDashboardDataManager;
    private ITvSettingsManager mTvSettingsManager;
    private UiThreadHandler mUiThreadHandler;

    private static final int MORE_FEATURES_SHELF_ROW_POSITION = 1;
    private static final int MORE_PERSONAL_SHELF_ROW_POSITION = 2;

    private static final int ALARM_ITEM_POSITION = 0;
    private static final int SLEEP_TIMER_ITEM_POSITION = 1;
    private static final int INTERNET_HOTSPOT_ITEM_POSITION = 2;
    private static final int WEATHER_ITEM_POSITION = 3;
    private static final int MYCHOICE_ITEM_POSITION = 4;
    private static final int TV_GUIDE_ITEM_POSITION = 5;
    private static final int TALK_BALK_ITEM_POSITION = 6;

    private static final int BILL_ITEM_POSITION = 0;
    private static final int BFL_WEATHER_ITEM_POSITION = 0;
    private static final int MESSAGES_ITEM_POSITION = 1;
    private static final int MESSAGE_DISPLAY_ITEM_POSITION = 2;
    private static final int USER_ACCOUNT_MANAGEMENT_ITEM_POSITION = 3;
    private static final int DELETE_ACCOUNT_AND_CLEAR_ITEM_POSITION = 4;

    private ArrayObjectAdapter mSettingsShelfItemAdapter;

    private ArrayObjectAdapter mFeaturesShelfItemAdapter;
    private SetAlarmFeatureItem mSetAlarmFeatureItem;
    private SleepTimerFeatureItem mSleepTimerFeatureItem;
    private InternetHotspotFeatureItem mInternetHotspotFeatureItem;
    private WeatherFeatureItem mWeatherFeatureItem;
    private MyChoiceFeatureItem mMyChoiceFeatureItem;
    private TvGuideFeatureItem mTvGuideFeatureItem;
    private TalkBackFeatureItem mTalkBackFeatureItem;

    private ArrayObjectAdapter mPersonalShelfItemAdapter;
    private BillItem mBillItem;
    private MessagesItem mMessagesItem;
    private MessageDisplaySettingsItem mMessageDisplaySettingsItem;
    private ManageAccountItem mManageAccountItem;
    private ClearHistoryItem mClearHistoryItem;

    private ShelfRow mFeaturesShelfRow;
    private ShelfRow mPersonalShelfRow;

    private static final int PICTURE_SETTING_ITEM_ID = 10;
    private static final int SOUND_SETTING_ITEM_ID = 11;
    private static final int AMBILIGHT_SETTING_ITEM_ID = 12;
    private static final int PICTURE_FORMAT_SETTING_ITEM_ID = 13;

    private static final int[] PBS_SETTINGS_PROPERTIES = {
            TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE,
            TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS,
            TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE,
            TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE,
            TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE,
            TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE,
            TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_BILL,
            TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_GUESTMESSAGES,
            TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_ROOMSTATUS,
            TvSettingsConstants.PBSMGR_PROPERTY_MY_CHOICE_MYCHOICE,
            TvSettingsConstants.PBSMGR_PROPERTY_INTERNET_HOTSPOT,
            TvSettingsConstants.PBSMGR_PROPERTY_SECURE_SHARING,
            TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_EPG,
            TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_TALKBACK_ENABLE,
    };

    public MoreChapterFragment() {
        super();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mTvSettingsManager = mDashboardDataManager.getTvSettingsManager();
        mUiThreadHandler = new UiThreadHandler(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        removeClearHistoryDialogue();
        super.onDestroy();
        mUiThreadHandler.removeCallbacksAndMessages(null);
    }

    private void removeClearHistoryDialogue() {
        if(mClearHistoryItem != null){
            mClearHistoryItem.removeClearHistoryDialogue();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        registerListeners();
        registerMoreChapterUpdates(this);
    }

    private void registerMoreChapterUpdates(MoreChapterFragment moreChapterFragment) {
        DashboardDataManager.getInstance().registerMoreChapterUpdates(moreChapterFragment);
    }

    private void unRegisterMoreChapterUpdates() {
        DashboardDataManager.getInstance().unRegisterMoreChapterUpdates();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");
        unregisterListeners();
        unRegisterMoreChapterUpdates();
        super.onDestroyView();
        DdbLogUtility.logMoreChapter(TAG, "#### onDestroyView() exit");
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        MoreChapterShelfItem moreChapterShelfItem = (MoreChapterShelfItem) item;
        moreChapterShelfItem.getAction().perform();

        int id = moreChapterShelfItem.getId();
        if (id == PICTURE_SETTING_ITEM_ID || id == SOUND_SETTING_ITEM_ID || id == AMBILIGHT_SETTING_ITEM_ID || id == PICTURE_FORMAT_SETTING_ITEM_ID) {
            getActivity().moveTaskToBack(false);
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onClockFormatChanged() {
        // do nothing
    }

    @Override
    public void onClockFormatChanged(int value) {
        // do nothing
    }

    @Override
    public void onTimeChanged() {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_TIME_CHANGED);
    }

    @Override
    public void onTimeTick() {

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
    @Override
    public void showAccountIcon(boolean show) {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_SHOW_ACCOUNT_SETTING_CHANGED);
    }

    @Override
    public void setAlignment(int windowAlignOffsetFromTop) {
        super.setAlignment(windowAlignOffsetFromTop);
        if (getVerticalGridView() != null) {
            getVerticalGridView().setWindowAlignment(BaseGridView.WINDOW_ALIGN_HIGH_EDGE);
        }
    }

    @Override
    protected void createRows() {
        createSettingsShelf();
        createFeaturesShelf();
        createPersonalShelf();
    }

    private void createPersonalShelf(){
        mPersonalShelfRow = buildPersonalShelfRow();
        if(mPersonalShelfRow!= null){
            addRow(getPersonalShelfRowPosition(),mPersonalShelfRow);
        }
    }

    private void createFeaturesShelf(){
        DdbLogUtility.logMoreChapter(TAG,"createFeaturesShelf");
        mFeaturesShelfRow = buildFeaturesShelfRow();
        if(null !=mFeaturesShelfItemAdapter && mFeaturesShelfItemAdapter.size() > 0){
            addRow(MORE_FEATURES_SHELF_ROW_POSITION,mFeaturesShelfRow);
        }else{
            removeRow(mFeaturesShelfRow);
            mFeaturesShelfRow = null;
        }
    }

    @Override
    protected PresenterSelector getChapterPresenterSelector() {
        MoreChapterShelfRowPresenter moreChapterShelfRowPresenter = new MoreChapterShelfRowPresenter();
        moreChapterShelfRowPresenter.setShadowEnabled(false);
        moreChapterShelfRowPresenter.enableChildRoundedCorners(false);
        moreChapterShelfRowPresenter.setSelectEffectEnabled(false);
        moreChapterShelfRowPresenter.setKeepChildForeground(true);
        return new SinglePresenterSelector(moreChapterShelfRowPresenter);
    }

    private void registerListeners() {
        mDashboardDataManager.registerClockSettingChangeListener(this);
        mDashboardDataManager.registerWeatherSettingsListener(this);
        mDashboardDataManager.addGuestCheckInStatusChangeListener(this);
        mDashboardDataManager.setAccountIconListener(this);
    }

    private void unregisterListeners() {
        mDashboardDataManager.unregisterClockSettingChangeListener(this);
        mDashboardDataManager.unregisterWeatherSettingsListener(this);
        mDashboardDataManager.removeGuestCheckInStatusChangeListener(this);
        mDashboardDataManager.removeAccountIconListner(this);
    }


    public void onTvSettingsValueChanged(int iProperty, int value) {
        Log.d(TAG, "onTvSettingsValuesUpdate received for Property -" + iProperty);
        if(getActivity() == null || !isAdded()) {
            Log.d(TAG, "onTvSettingsValuesUpdate: fragment not added");
            return;
        }
        switch (iProperty) {
            case TvSettingsConstants.PBSMGR_PROPERTY_INTERNET_HOTSPOT:
            case TvSettingsConstants.PBSMGR_PROPERTY_SECURE_SHARING:
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_INTERNET_HOTSPOT_SETTING_CHANGED);
                break;

            case TvSettingsConstants.PBSMGR_PROPERTY_MY_CHOICE_MYCHOICE:
                Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_MYCHOICE_SETTING_CHANGED);
                message.arg1 = value;
                message.sendToTarget();
                break;

            case TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_BILL:
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_BILL_SETTING_CHANGED);
                break;

            case TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_GUESTMESSAGES:
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_MESSAGES_SETTING_CHANGED);
                break;

            case TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE:
            case TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS:
            case TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE:
            case TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE:
            case TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE:
            case TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE:
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_PMS_SETTING_CHANGED);
                break;

            case TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_ROOMSTATUS: //DELETE_ACCOUNT_AND_CLEAR_ITEM_POSITION
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_MESSAGE_DISPLAY_SETTING_CHANGED);
                break;

            //Tv Guide
            case TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_EPG: //tv channel guide is not there in tvsettings constant
                Message m = Message.obtain();
                m.what = UiThreadHandler.MSG_WHAT_TV_GUIDE_SETTING_CHANGED;
                m.arg1 = value;
                mUiThreadHandler.sendMessage(m);
                break;
            //TalkBack
            case TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_TALKBACK_ENABLE: //TalkBack
                Message msg = Message.obtain();
                msg.what = UiThreadHandler.MSG_WHAT_TALK_BACK_SETTING_CHANGED;
                msg.arg1 = value;
                mUiThreadHandler.removeMessages(msg.what);
                mUiThreadHandler.sendMessage(msg);
                break;
            default:
                throw new IllegalArgumentException("Unkown property update. property:" + iProperty);
        }
        Log.d(TAG, "onUpdate for property change not recevied");
    }


    private int getAlarmItemPosition() {
        return ALARM_ITEM_POSITION;
    }

    private int getSleepTimerItemPosition() {
        int position = SLEEP_TIMER_ITEM_POSITION;
        if (!isAlarmAvailable()) {
            position--;
        }
        return position;
    }

    private int getInternetHotspotItemPosition() {
        int position = INTERNET_HOTSPOT_ITEM_POSITION;
        if (!isAlarmAvailable()) {
            position--;
        }
        if (!isSleepTimerAvailable()) {
            position--;
        }
        return position;
    }

    private int getWeatherItemPosition() {
        int position = WEATHER_ITEM_POSITION;
        if (!isAlarmAvailable()) {
            position--;
        }
        if (!isSleepTimerAvailable()) {
            position--;
        }
        if (!isInternetHotspotAvailable()) {
            position--;
        }
        return position;
    }

    private int getMychoiceItemPosition() {
        int position = MYCHOICE_ITEM_POSITION;
        if (!isAlarmAvailable()) {
            position--;
        }
        if (!isSleepTimerAvailable()) {
            position--;
        }
        if (!isInternetHotspotAvailable()) {
            position--;
        }
        if (!isWeatherEnabled()) {
            position--;
        }
        return position;
    }

    private int getTvGuideItemPosition(){
        int position =  TV_GUIDE_ITEM_POSITION;
        if (!isAlarmAvailable()) {
            position--;
        }
        if (!isSleepTimerAvailable()) {
            position--;
        }
        if (!isInternetHotspotAvailable()) {
            position--;
        }
        if (!isWeatherEnabled()) {
            position--;
        }
        if(!isMyChoiceAvailable()){
            position--;
        }
        Log.d(TAG, "getTvGuideItemPosition() called: "+position);
        return position;
    }

    private int getTalkBackItemPosition(){
        int position =  TALK_BALK_ITEM_POSITION;
        if (!isAlarmAvailable()) {
            position--;
        }
        if (!isSleepTimerAvailable()) {
            position--;
        }
        if (!isInternetHotspotAvailable()) {
            position--;
        }
        if (!isWeatherEnabled()) {
            position--;
        }
        if(!isMyChoiceAvailable()){
            position--;
        }
        if(!isTvGuideAvailable()){
            position--;
        }
        Log.d(TAG, "getTalkBackItemPosition() called: "+position);
        return position;
    }
    private int getBillItemPosition() {
        int position = BILL_ITEM_POSITION;
        return position;
    }

    private int getMessagesItemPosition() {
        int position = MESSAGES_ITEM_POSITION;
        if (!isBillAvailable()) {
            position--;
        }
        return position;
    }

    private int getMessageDisplayItemPosition() {
        int position = MESSAGE_DISPLAY_ITEM_POSITION;
        if (!isBillAvailable()) {
            position--;
        }
        if (!isMessagesAvailable()) {
            position--;
        }
        return position;
    }

    private int getUserAccountManagementItemPosition() {
        int position = USER_ACCOUNT_MANAGEMENT_ITEM_POSITION;
        if (!isBillAvailable()) {
            position--;
        }
        if (!isMessagesAvailable()) {
            position--;
        }
        if (!isMessageDisplayAvailable()) {
            position--;
        }
        return position;
    }

    private int getDeleteAccountAndClearItemPosition() {
        int position = DELETE_ACCOUNT_AND_CLEAR_ITEM_POSITION;
        if (!isBillAvailable()) {
            position--;
        }
        if (!isMessagesAvailable()) {
            position--;
        }
        if (!isMessageDisplayAvailable()) {
            position--;
        }
        if(!isManageAccountAvailable()){
            position--;
        }
        return position;
    }

    private int getPersonalShelfRowPosition(){
        if(null == mFeaturesShelfRow){
            return MORE_FEATURES_SHELF_ROW_POSITION;
        }
        return MORE_PERSONAL_SHELF_ROW_POSITION;
    }

    private boolean isAlarmAvailable() {
        return mDashboardDataManager.isClockAvailable();
    }

    private boolean isSleepTimerAvailable() {
        return true;
    }

    private boolean isInternetHotspotAvailable() {
        return mDashboardDataManager.isInternetHotspotAvailable();
    }

    private boolean isWeatherEnabled() {
        return mDashboardDataManager.isWeatherEnabled();
    }

    private boolean isWeatherEnabled(int value) {
        return mDashboardDataManager.isWeatherEnabled(value);
    }

    private boolean isMyChoiceAvailable() {
        return mDashboardDataManager.isMyChoiceEnabled();
    }

    private  boolean isTvGuideAvailable(){
        return mDashboardDataManager.isTvGuideAvailable();
    }

    private boolean isTalkbackAvailable(){
        return mDashboardDataManager.isTalkbackAvailable();
    }

    private boolean isBillAvailable() {
        return mDashboardDataManager.isBillAvailable();
    }

    private boolean isMessagesAvailable() {
        return mDashboardDataManager.isMessageAvailable();
    }

    private boolean isMessageDisplayAvailable() {
        return mDashboardDataManager.isMessageDisplayAvailable();
    }
    private boolean isManageAccountAvailable(){
        return mDashboardDataManager.isAccountItemAvailable();
    }
    private boolean isDeleteAccountAvailable() {
        return mDashboardDataManager.isDeleteAccountAvailable();
    }

    private boolean isAmbilightAvailable() {
        return mDashboardDataManager.isAmbilightAvailable();
    }

    private void updateAlarmVisibility() {
        if (mFeaturesShelfItemAdapter == null) {
            return;
        }

        if (isAlarmAvailable()) {
            if (mFeaturesShelfItemAdapter.indexOf(mSetAlarmFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mSetAlarmFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mSetAlarmFeatureItem);
        }
    }

    private void updateInternetHotspotVisibility() {
        if (mFeaturesShelfItemAdapter == null) {
            return;
        }

        if (isInternetHotspotAvailable()) {
            if (mFeaturesShelfItemAdapter.indexOf(mInternetHotspotFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mInternetHotspotFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mInternetHotspotFeatureItem);
        }
    }

    private void updateWeatherVisibility() {
        if (mFeaturesShelfItemAdapter == null) {
            createFeaturesShelf();
            return;
        }

        if (isWeatherEnabled()) {
            if (mFeaturesShelfItemAdapter.indexOf(mWeatherFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mWeatherFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mWeatherFeatureItem);
            if (mFeaturesShelfItemAdapter == null || mDashboardDataManager.isBFLProduct()) {
                replaceRow(MORE_FEATURES_SHELF_ROW_POSITION, mPersonalShelfRow);
            }
        }
    }

    private void updateWeatherVisibility(int value) {
        if (mFeaturesShelfItemAdapter == null) {
                createFeaturesShelf();
            return;
        }

        if (isWeatherEnabled(value)) {
            if (mFeaturesShelfItemAdapter.indexOf(mWeatherFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mWeatherFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mWeatherFeatureItem);
            if (mFeaturesShelfItemAdapter == null || mDashboardDataManager.isBFLProduct()) {
                replaceRow(MORE_FEATURES_SHELF_ROW_POSITION, mPersonalShelfRow);
            }
        }
    }

    private void updateMyChoiceVisibility(int myChoiceEnabledValue) {
        if (mFeaturesShelfItemAdapter == null) {
            return;
        }

        if (myChoiceEnabledValue == 1) {
            if (mFeaturesShelfItemAdapter.indexOf(mMyChoiceFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mMyChoiceFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mMyChoiceFeatureItem);
        }
    }

    //TvGuide
    private void updateTvGuideVisibility(int tvGuideEnabledValue) {
        if (mFeaturesShelfItemAdapter == null) {
            return;
        }
        if (tvGuideEnabledValue == 1) {
            if (mFeaturesShelfItemAdapter.indexOf(mTvGuideFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mTvGuideFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mTvGuideFeatureItem);
        }
    }
    //TalkBack
    private void updateTalkBackVisibility(int talkBackEnabledValue) {
        if (mFeaturesShelfItemAdapter == null) {
            return;
        }

        if (talkBackEnabledValue == 1) {
            if (mFeaturesShelfItemAdapter.indexOf(mTalkBackFeatureItem) == -1) {
                mFeaturesShelfItemAdapter.add(mTalkBackFeatureItem);
            }
        } else {
            mFeaturesShelfItemAdapter.remove(mTalkBackFeatureItem);
        }
    }

    private void updateBillVisibility() {
        if (mPersonalShelfItemAdapter == null) {
            return;
        }

        if (isBillAvailable()) {
            if (mPersonalShelfItemAdapter.indexOf(mBillItem) == -1) {
                mPersonalShelfItemAdapter.add(mBillItem);
            }
        } else {
            mPersonalShelfItemAdapter.remove(mBillItem);
        }
    }

    private void updateMessageVisibility() {
        if (mPersonalShelfItemAdapter == null) {
            return;
        }

        if (isMessagesAvailable()) {
            if (mPersonalShelfItemAdapter.indexOf(mMessagesItem) == -1) {
                mPersonalShelfItemAdapter.add(mMessagesItem);
            }
        } else {
            mPersonalShelfItemAdapter.remove(mMessagesItem);
        }
    }

    private void updateMessageDisplayVisibility() {
        if (mPersonalShelfItemAdapter == null) {
            return;
        }

        if (isMessageDisplayAvailable()) {
            if (mPersonalShelfItemAdapter.indexOf(mMessageDisplaySettingsItem) == -1) {
                mPersonalShelfItemAdapter.add(mMessageDisplaySettingsItem);
            }
        } else {
            mPersonalShelfItemAdapter.remove(mMessageDisplaySettingsItem);
        }
    }
    private void updateManageAccountVisiblity(){
        if (mPersonalShelfItemAdapter == null) {
            return;
        }
        if(isManageAccountAvailable()) {
            if (mPersonalShelfItemAdapter.indexOf(mManageAccountItem) == -1) {
                mPersonalShelfItemAdapter.add(mManageAccountItem);
            }
        }else{
            mPersonalShelfItemAdapter.remove(mManageAccountItem);
        }
    }

    private void updateDeleteAccountVisibility() {
        if (mPersonalShelfItemAdapter == null) {
            return;
        }

        if (isDeleteAccountAvailable()) {
            if (mPersonalShelfItemAdapter.indexOf(mClearHistoryItem) == -1) {
                mPersonalShelfItemAdapter.add(mClearHistoryItem);
            }
        } else {
            mPersonalShelfItemAdapter.remove(mClearHistoryItem);
        }
    }

    private void createSettingsShelf() {
        mSettingsShelfItemAdapter = new ArrayObjectAdapter(new MoreChapterShelfItemPresenter());

        PictureSettingItem pictureSettingItem = new PictureSettingItem(getContext().getApplicationContext(), PICTURE_SETTING_ITEM_ID);
        mSettingsShelfItemAdapter.add(pictureSettingItem);

        SoundSettingItem soundSettingItem = new SoundSettingItem(getContext().getApplicationContext(), SOUND_SETTING_ITEM_ID);
        mSettingsShelfItemAdapter.add(soundSettingItem);

        if (isAmbilightAvailable()) {
            AmbilightSettingItem ambilightSettingItem = new AmbilightSettingItem(getContext().getApplicationContext(), AMBILIGHT_SETTING_ITEM_ID);
            mSettingsShelfItemAdapter.add(ambilightSettingItem);
        }

        PictureFormatSettingItem pictureFormatSettingItem = new PictureFormatSettingItem(getContext().getApplicationContext(), PICTURE_FORMAT_SETTING_ITEM_ID);
        mSettingsShelfItemAdapter.add(pictureFormatSettingItem);

        MenuLanguageSettingItem menuLanguageSettingItem = new MenuLanguageSettingItem(getContext().getApplicationContext());
        mSettingsShelfItemAdapter.add(menuLanguageSettingItem);

        if (isLaunchedInConfigMode()) {
            DashboardConfigurationSettingItem dashboardConfigurationSettingItem = new DashboardConfigurationSettingItem(getContext());
            mSettingsShelfItemAdapter.add(dashboardConfigurationSettingItem);
        }
        DdbLogUtility.logMoreChapter(TAG, "createSettingsShelf() called");
        addRow(new ShelfRow(new ShelfHeaderItem(getString(org.droidtv.ui.strings.R.string.MAIN_VB_SETUP), ContextCompat.getDrawable(getContext().getApplicationContext(), R.drawable.icon_7_settings_setup_n_36x36)), mSettingsShelfItemAdapter));
    }

    private ShelfRow buildFeaturesShelfRow() {
        DdbLogUtility.logMoreChapter(TAG, "createSettingsShelf() called");
        mFeaturesShelfItemAdapter = new ArrayObjectAdapter(new MoreChapterShelfItemPresenter());
        if(mDashboardDataManager.getInstance().isBFLProduct()){
            if (isWeatherEnabled()) {
                mWeatherFeatureItem = new WeatherFeatureItem(getContext().getApplicationContext());
                mFeaturesShelfItemAdapter.add(BFL_WEATHER_ITEM_POSITION, mWeatherFeatureItem);
            }else{
                return new ShelfRow(null, mFeaturesShelfItemAdapter);
            }
        }

        mSetAlarmFeatureItem = new SetAlarmFeatureItem(getContext().getApplicationContext());
        if (isAlarmAvailable()) {
            mFeaturesShelfItemAdapter.add(getAlarmItemPosition(), mSetAlarmFeatureItem);
        }

        mSleepTimerFeatureItem = new SleepTimerFeatureItem(getContext().getApplicationContext());
        mFeaturesShelfItemAdapter.add(getSleepTimerItemPosition(), mSleepTimerFeatureItem);

        mInternetHotspotFeatureItem = new InternetHotspotFeatureItem(getContext().getApplicationContext());
        if (isInternetHotspotAvailable()) {
            mFeaturesShelfItemAdapter.add(getInternetHotspotItemPosition(), mInternetHotspotFeatureItem);
        }

        mWeatherFeatureItem = new WeatherFeatureItem(getContext().getApplicationContext());
        if (isWeatherEnabled()) {
            mFeaturesShelfItemAdapter.add(getWeatherItemPosition(), mWeatherFeatureItem);
        }

        mMyChoiceFeatureItem = new MyChoiceFeatureItem(getContext().getApplicationContext());
        if (isMyChoiceAvailable()) {
            mFeaturesShelfItemAdapter.add(getMychoiceItemPosition(), mMyChoiceFeatureItem);
        }
		 //TV guide
        mTvGuideFeatureItem = new TvGuideFeatureItem(getContext().getApplicationContext());
        if (isTvGuideAvailable()) {
            mFeaturesShelfItemAdapter.add(getTvGuideItemPosition(), mTvGuideFeatureItem);
        }

        //TalkBack //CR:TF519PHIEUMTK06-151
        mTalkBackFeatureItem = new TalkBackFeatureItem(getContext().getApplicationContext());
        if(isTalkbackAvailable()) {
            mFeaturesShelfItemAdapter.add(getTalkBackItemPosition(), mTalkBackFeatureItem);
        }
        DdbLogUtility.logMoreChapter(TAG, "createFeaturesShelf() called");
        return new ShelfRow(new ShelfHeaderItem(getString(org.droidtv.ui.htvstrings.R.string.HTV_TOP_FEATURES), ContextCompat.getDrawable(getContext().getApplicationContext(), R.drawable.icon_191_device_n_36x36)), mFeaturesShelfItemAdapter);
    }

    private ShelfRow buildPersonalShelfRow() {
        mPersonalShelfItemAdapter = new ArrayObjectAdapter(new MoreChapterShelfItemPresenter());

        mBillItem = new BillItem(getContext().getApplicationContext());
        if (isBillAvailable()) {
            mPersonalShelfItemAdapter.add(getBillItemPosition(), mBillItem);
        }

        mMessagesItem = new MessagesItem(getContext().getApplicationContext());
        if (isMessagesAvailable()) {
            mPersonalShelfItemAdapter.add(getMessagesItemPosition(), mMessagesItem);
        }

        mMessageDisplaySettingsItem = new MessageDisplaySettingsItem(getContext().getApplicationContext());
        if (isMessageDisplayAvailable()) {
            mPersonalShelfItemAdapter.add(getMessageDisplayItemPosition(), mMessageDisplaySettingsItem);
        }

        mManageAccountItem = new ManageAccountItem(getContext().getApplicationContext());
        if (isManageAccountAvailable()) {
            mPersonalShelfItemAdapter.add(getUserAccountManagementItemPosition(), mManageAccountItem);
        }

        mClearHistoryItem = new ClearHistoryItem(getContext());
        mPersonalShelfItemAdapter.add(getDeleteAccountAndClearItemPosition(), mClearHistoryItem);
        DdbLogUtility.logMoreChapter(TAG, "buildPersonalShelfRow() called");
        return new ShelfRow(new ShelfHeaderItem(getString(org.droidtv.ui.strings.R.string.MAIN_PERSONAL), ContextCompat.getDrawable(getContext().getApplicationContext(), R.drawable.icon_93_user_n_36x36)), mPersonalShelfItemAdapter);
    }

    private boolean isLaunchedInConfigMode() {
        Intent intent = getActivity().getIntent();
        if (Constants.INTENT_ACTION_MAIN.equals(intent.getAction())) {
            Set<String> categories = intent.getCategories();
            if (categories != null && categories.contains(Constants.INTENT_CATEGORY_PROFFESIONAL_DISPLAY_CONFIGURATION)) {
                if (intent.getBooleanExtra(Constants.EXTRA_CONFIG_MODE, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void onAlarmSettingChanged() {
        updateAlarmVisibility();
    }

    private void onWeatherVisibilitySettingChanged() {
        updateWeatherVisibility();
    }

    private void onWeatherVisibilitySettingChanged(int value) {
        updateWeatherVisibility(value);
    }

    private void onInternetHotspotSettingChanged() {
        updateInternetHotspotVisibility();
    }

    private void onMyChoiceSettingChanged(int value) {
        updateMyChoiceVisibility(value);
    }
	
    private void onTvGuideSettingChanged(int value){
        updateTvGuideVisibility(value);
    }
	

	
    private void onBillSettingChanged() {
        updateBillVisibility();
    }

    private void onMessagesSettingChanged() {
        updateMessageVisibility();
    }

    private void onMessageDisplaySettingChanged() {
        updateMessageDisplayVisibility();
    }

    private void onPmsSettingChanged() {
        onBillSettingChanged();
        onMessagesSettingChanged();
    }

    private void onGuestCheckInChanged() {
        onMessageDisplaySettingChanged();
    }
    private void onShowAccountSettingChanged(){
        updateManageAccountVisiblity();
    }
    @Override
    public void OnGuestCheckInStatusChanged() {
        Log.d(TAG, "#### enter OnGuestCheckInStatusChanged()");
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_GUEST_CHECK_IN_CHANGED);
        Log.d(TAG, "#### exit OnGuestCheckInStatusChanged()");

    }

    private void onTalkBackSettingChanged(int value){
        updateTalkBackVisibility(value);
    }

    private static class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_TIME_CHANGED = 100;
        private static final int MSG_WHAT_WEATHER_SETTING_CHANGED = 101;
        private static final int MSG_WHAT_INTERNET_HOTSPOT_SETTING_CHANGED = 102;
        private static final int MSG_WHAT_MYCHOICE_SETTING_CHANGED = 103;
        private static final int MSG_WHAT_BILL_SETTING_CHANGED = 104;
        private static final int MSG_WHAT_MESSAGES_SETTING_CHANGED = 105;
        private static final int MSG_WHAT_MESSAGE_DISPLAY_SETTING_CHANGED = 106;
        private static final int MSG_WHAT_PMS_SETTING_CHANGED = 107;
        private static final int MSG_WHAT_GUEST_CHECK_IN_CHANGED = 108;
        private static final int MSG_WHAT_TV_GUIDE_SETTING_CHANGED = 109;
        private static final int MSG_WHAT_TALK_BACK_SETTING_CHANGED = 110;
        private static final int MSG_WHAT_SHOW_ACCOUNT_SETTING_CHANGED = 111;

        private WeakReference<MoreChapterFragment> mMoreChapterFragmentRef;

        private UiThreadHandler(MoreChapterFragment moreChapterFragment) {
            super();
            mMoreChapterFragmentRef = new WeakReference<>(moreChapterFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            if (what == MSG_WHAT_TIME_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onAlarmSettingChanged();
                }
                return;
            }

            if (what == MSG_WHAT_WEATHER_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onWeatherVisibilitySettingChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_INTERNET_HOTSPOT_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onInternetHotspotSettingChanged();
                }
                return;
            }

            if (what == MSG_WHAT_MYCHOICE_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onMyChoiceSettingChanged(msg.arg1);
                }
                return;
            }
			
			if (what == MSG_WHAT_TV_GUIDE_SETTING_CHANGED) { //TVGuide
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onTvGuideSettingChanged(msg.arg1);
                }
                return;
            }


            if (what == MSG_WHAT_BILL_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onBillSettingChanged();
                }
                return;
            }

            if (what == MSG_WHAT_MESSAGES_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onMessagesSettingChanged();
                }
                return;
            }

            if (what == MSG_WHAT_MESSAGE_DISPLAY_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onMessageDisplaySettingChanged();
                }
                return;
            }

            if (what == MSG_WHAT_PMS_SETTING_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onPmsSettingChanged();
                }
                return;
            }
            if (what == MSG_WHAT_GUEST_CHECK_IN_CHANGED) {
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onGuestCheckInChanged();
                }
                return;
            }
            if(what == MSG_WHAT_SHOW_ACCOUNT_SETTING_CHANGED){
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if(fragment != null){
                    fragment.onShowAccountSettingChanged();
                }
                return;
            }
            if (what == MSG_WHAT_TALK_BACK_SETTING_CHANGED) { //Talkback
                MoreChapterFragment fragment = mMoreChapterFragmentRef.get();
                if (fragment != null) {
                    fragment.onTalkBackSettingChanged(msg.arg1);
                }
                return;
            }
        }
    }
}
