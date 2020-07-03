package org.droidtv.defaultdashboard.data.manager;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.SparseArray;

import androidx.leanback.widget.Presenter;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.common.CulLogger;
import org.droidtv.defaultdashboard.data.ContentRatingStore;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.model.Program;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.data.model.appsChapter.CountryAppListItem;
import org.droidtv.defaultdashboard.data.model.channelFilter.ChannelFilter;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.oem.OemHelper;
import org.droidtv.defaultdashboard.receiver.GuestCheckInStatusChangeReceiver;
import org.droidtv.defaultdashboard.receiver.UsbEventsReceiver;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.recommended.RecommendationListenerService;
import org.droidtv.defaultdashboard.ui.fragment.MoreChapterFragment;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.TNCDetails;
import org.droidtv.htv.provider.HtvContract;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.ITvSettingsManager.ITvSettingsManagerNotify;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;
import org.droidtv.weather.WeatherInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sandeep.kumar on 17/10/2017.
 */

public final class DashboardDataManager extends ContextualObject {


    private final String TAG = DashboardDataManager.class.getSimpleName();
    private static DashboardDataManager sDashboardDataManager;

    private BillDataManager mBillDataManager;
    private MessagesDataManager mMessagesDataManager;
    private AccountDataManager mAccountDataManager;
    private WeatherDataManager mWeatherDataManager;
    private SourceDataManager mSourceDataManager;
    private ChannelDataManager mChannelDataManager;
    private ProgramDataManager mProgramDataManager;
    private ImageDataManager mImageDataManager;
    private CloneDataManager mCloneDataManager;
    private FileDataManager mFileDataManager;
    private SmartInfoDataManager mSmartInfoDataManager;
    private RecommendationDataManager mRecommendationDataManager;
    private VideoOnDemandDataManager mVideoOnDemandDataManager;
    private GamesDataManager mGamesDataManager;
    private AppsDataManager mAppsDataManager;
    private GoogleCastDataManager mGoogleCastDataManager;
    private MyChoiceDataManager mMyChoiceDataManager;
    private PreviewProgramsDataManager mPreviewProgramsManager;
    private int mSelectedCountryConstant;
    private int mSelectedLanguageConstant;
	private int mSmartTVMode;
	private int mCULStatus;
	private int mAppControlId;
	private int mPMSSettingState;
	private String mGuestCheckInState;
	private int mDiagonsticLocation;
	private int mClockFormat;
    private OemHelper mOemHelper;

    private ArrayList<WeakReference<BackgroundImageChangeObserver>> mBackgroundImageChangeObserverRefs;
    private ArrayList<WeakReference<HotelLogoChangeObserver>> mHotelLogoChangeObserverRefs;
    private ArrayList<WeakReference<SidePanelListener>> mSidePanelListenerRefs;
    private ArrayList<WeakReference<SidePanelTextColorChangeListener>> mSidePanelTextColorChangeListenerRefs;
    private ArrayList<WeakReference<DashboardConfigurationResetListener>> mDashboardConfigurationResetListenerRefs;
    private ArrayList<WeakReference<AlarmChangeListener>> mAlarmChangeListenerRefs;
    private SharedPreferences mDefaultSharedPreferences;
    private boolean mMediaScannerInProgress;
    private boolean misSharingImageBackgroundCloned  = false;
    private boolean misMainImageBackgroundCloned  = false;
    private boolean isUsbConnected;
    private boolean isGoogleSignInAfter2min = false;

    public void setGoogleSignInAfter2Min(boolean isGoogleSignInAfter2min){ this.isGoogleSignInAfter2min = isGoogleSignInAfter2min; }
    public boolean getGoogleSignInAfter2Min(){return isGoogleSignInAfter2min;}

    private WeakReference<GoogleAccountFlowListener> mGoogleAccountFlowListenerRef;
    private ArrayList<WeakReference<AccountIconListener>> mAccountIconListenerRefs;
    private WeakReference<AssistantIconListener> mAssistantIconListenerRef;
    private EventsBroadcastReceiver mEventsBroadcastReceiver;
    private final ITvSettingsManager mTvSettingsManager;
    private ArrayList<WeakReference<ClockSettingChangeListener>> mClockSettingChangeListenerRefs;
    private ArrayList<WeakReference<DashboardPmsStateChangeListener>> mDashboardPmsStateListenerRefs;
    private ArrayList<WeakReference<PbsSettingCountryChangeListener>> mPbsSettingCountryChangeListenerRefs;
    private ArrayList<WeakReference<PbsSettingLanguageChangeListener>> mPbsSettingLanguageChangeListenerRefs;

    private WeakReference<RecommendationListenerService.RecommendationServiceAdapter> mRecommendationServiceAdapterRef;
    private ArrayList<WeakReference<RecommendationListenerServiceClient>> mRecommendationListenerServiceClientRefs;

    private ArrayList<WeakReference<AppCategoryFilterStateChangeListener>> mAppCategoryFilterStateChangeListenerRefs;
    private ArrayList<WeakReference<AppsCountryFilterStateListener>> mAppsCountryFilterStateListenerRefs;
    private WeakReference<MoreChapterFragment> mMoreChapterFragmentWeakRef;
    private UiThreadHandler mUiThreadHandler;
    private GuestCheckInStatusChangeReceiver mGuestCheckInStatusChangeReceiver = new GuestCheckInStatusChangeReceiver();

    private int mAppsCountry;
    private boolean mIsAppCountryFilterEnabled;
    private boolean mIsProfessionalModeEnabled;
    private boolean isClockAvailable = false;
    private int mDashboardMode;
    private int mRegion;
    private Context mDeviceContext;
    private int mProductType;
    private String PMS_CHECK_IN = "CheckIn";
    private String PMS_CHECK_OUT = "CheckOut";
    private static final int DASHBOARD_MODE_PHILIPS_HOME = 20000;

    boolean mIsCastChapterFragment = false;
    boolean isRecommendedChapterDisplayed = false;

    private static final int OPTION_REGION_EU = 0;
    private static final int OFFSET_EU = 4;
    private static final int OFFSET_NAFTA = 3;

    private static BitmapDrawable mMainBackgroundSavedBitMapDrawable = null;
    private static BitmapDrawable mCastBackgroundBitMapDrawable = null;
    private static Bitmap mHotelLogoBitmap = null;
    private static boolean castBcakgroundBitmapFetchOngoing = false;
    private static boolean mainBackgroundBitmapFetchOngoing = false;
    private static boolean hotelLogoBitmapFetchOngoing = false;

    public void saveBackgroundImage(BitmapDrawable  bitmapDrawable){
        if(!isDuplicateBitmapDrawable(mMainBackgroundSavedBitMapDrawable, bitmapDrawable )){
            recycleOldBitmapDrawable(mMainBackgroundSavedBitMapDrawable);
            mMainBackgroundSavedBitMapDrawable = bitmapDrawable;
        }
    }

    public void saveCastBackgroundImage(BitmapDrawable  bitmapDrawable) {
        if(!isDuplicateBitmapDrawable(mCastBackgroundBitMapDrawable, bitmapDrawable)) {
            recycleOldBitmapDrawable(mCastBackgroundBitMapDrawable);
            mCastBackgroundBitMapDrawable = bitmapDrawable;
        }
    }

    public void saveHotelLogoBitmap(Bitmap hotelLogoBitmap){
        if(!isDuplicateBitmap(mHotelLogoBitmap, hotelLogoBitmap)){
            recycleOldBitmap(mHotelLogoBitmap);
            mHotelLogoBitmap = hotelLogoBitmap;
        }
    }

    private boolean isDuplicateBitmapDrawable(BitmapDrawable oldBitmapDrawable, BitmapDrawable newBitmapDrwable) {
        if(oldBitmapDrawable != null && newBitmapDrwable != null){
           return isDuplicateBitmap(oldBitmapDrawable.getBitmap(), newBitmapDrwable.getBitmap());
        }
        Log.d("TEST", "Testing");
        return false;
    }

    private boolean isDuplicateBitmap(Bitmap oldBitmap, Bitmap newBitmap) {
         return oldBitmap != null && newBitmap != null && oldBitmap.equals(newBitmap);
    }

    public BitmapDrawable getSavedBitMapDrawable(){ return mMainBackgroundSavedBitMapDrawable; }

    public BitmapDrawable getSavedCastBitMapDrawable(){ return mCastBackgroundBitMapDrawable; }

    public Bitmap getHotelLogoBitmap() { return mHotelLogoBitmap; }

    public static void setCastBackgroundFetchOngoing(boolean status) { castBcakgroundBitmapFetchOngoing = status;}

    public static void setMainBackgroundBitmapFetchOngoing(boolean status){ mainBackgroundBitmapFetchOngoing = status;}

    public static void setHotelLogoBitmapFetchOngoing(boolean status){ hotelLogoBitmapFetchOngoing = status;}

    private static void recycleOldBitmapDrawable(BitmapDrawable mSavedBitMapDrawable) {
        if(isValidBitmapDrawable(mSavedBitMapDrawable)){
            recycleOldBitmap(mSavedBitMapDrawable.getBitmap());
        }
    }

    private static void recycleOldBitmap(Bitmap bitmap){
        if(isValidBitmap(bitmap)){
            Log.d("DashboardDataManager", "recycleOldBitmap: recycled sucess");
            bitmap.recycle();
            bitmap = null;
        }
    }

    private static boolean isValidBitmap(Bitmap bitmap) { return  bitmap != null && (!bitmap.isRecycled()); }

    public static boolean isValidBitmapDrawable(BitmapDrawable bitmapDrawable){
        return bitmapDrawable != null && bitmapDrawable.getBitmap() != null && (!bitmapDrawable.getBitmap().isRecycled());
    }


    public  Context getDeviceProtectedContext(Context c){
        if (mDeviceContext == null){
            synchronized (this){
                    mDeviceContext = c.getApplicationContext().createDeviceProtectedStorageContext();
            }
        }
        return mDeviceContext;
    }

    private DashboardDataManager(Context context) {
        super(context);
        mDeviceContext = getDeviceProtectedContext(context);
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mDeviceContext);
        mTvSettingsManager = ITvSettingsManager.Instance.getInterface();
        mMediaScannerInProgress = false;
        mRegion = readRegion();
        //mProductType = readProductType();
        mSidePanelTextColorChangeListenerRefs = new ArrayList<>();
        mBackgroundImageChangeObserverRefs = new ArrayList<>();
        mHotelLogoChangeObserverRefs = new ArrayList<>();
        mSidePanelListenerRefs = new ArrayList<>();
        mDashboardConfigurationResetListenerRefs = new ArrayList<>();
        mAlarmChangeListenerRefs = new ArrayList<>();
        mEventsBroadcastReceiver = new EventsBroadcastReceiver();
        mClockSettingChangeListenerRefs = new ArrayList<>();
        mDashboardPmsStateListenerRefs = new ArrayList<>();
        mPbsSettingCountryChangeListenerRefs = new ArrayList<>();
        mAppCategoryFilterStateChangeListenerRefs = new ArrayList<>();
        mPbsSettingLanguageChangeListenerRefs = new ArrayList<>();
        mRecommendationListenerServiceClientRefs = new ArrayList<>();
        mAppsCountryFilterStateListenerRefs = new ArrayList<>();
        mAccountIconListenerRefs = new ArrayList<>();
        mClockFormat = getClockFormat();
    }

    public static DashboardDataManager getInstance() {
        if (sDashboardDataManager == null) {
            throw new IllegalStateException("DashboardDataManager has not been set up yet!.Make sure to call DashboardDataManager.init(context) before accessing an instance");
        }
        return sDashboardDataManager;
    }

    public static void init(Context appContext) {
        Log.d("DashboardDataManager", "init");
        if (sDashboardDataManager != null) {
            return;
        }

        sDashboardDataManager = new DashboardDataManager(appContext);
        sDashboardDataManager.initInternal();

        sDashboardDataManager.initMyChoiceDataManager();
        sDashboardDataManager.initGoogleCastDataManager();
        sDashboardDataManager.initRecommendationDataManager();
        sDashboardDataManager.initAppsDataManager();
        sDashboardDataManager.initPreviewProgramManager();
        sDashboardDataManager.initMessagesDataManager();
        sDashboardDataManager.initBillDataManager();
        sDashboardDataManager.initAccountDataManager();
        sDashboardDataManager.initWeatherDataManager();
        sDashboardDataManager.initSourceDataManager();
        sDashboardDataManager.initChannelDataManager();
        sDashboardDataManager.initProgramDataManager();
        sDashboardDataManager.initThumbnailBrowserDataManager();
        sDashboardDataManager.initCloneDataManager();
        sDashboardDataManager.initFileDataManager();
        sDashboardDataManager.initSmartInfoDataManager();
        sDashboardDataManager.initVideoOnDemandDataManager();
        sDashboardDataManager.initGamesDataManager();
        sDashboardDataManager.initOEMHelper();
        registerCloneInReceiver(appContext);
        bindCULLoggerService(appContext);
    }
	 
	private int readRegion(){
		return mTvSettingsManager.getInt(TvSettingsConstants.OPREGION, 0, 0);
	}
	
	public int getRegion(){
		return mRegion;
	}

    //private int readProductType(){
    //    return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PRODUCT_TYPE, 0, 0);
    //}

    public String getDeviceName(){
        return mGoogleCastDataManager.getNetworkName();
    }

    private void initInternal() {
        mUiThreadHandler = new UiThreadHandler(this);
        registerFactoryResetReceiver();
        registerClearDataReceiver();
        registerSettingsCallbacks();
        registerTimeChangeReceiver();
		setSmartTVMode();
        setAppsCountry();
        setAppCountryFilterEnabledState();
        setProfessionalModeEnabledState();
        setDashboardMode();
        setCULState();
        setAppControlId();
        setPMSSetting();
        setGuestCheckingState();
        setDiagnosticLocation();
        syncCustomizedSettingsToTVSettings();
        registerGuestCheckInStatusChangeReceiver();
        initCloneImageStateValues();
		registerMediaScannerReceiver();
		initClockAvailabilityStatus();
    }

    private void initClockAvailabilityStatus() {
        String clockSource = SystemProperties.get(Constants.SYSTEM_PROPERTY_CLOCK_SOURCE);
        isClockAvailable = clockSource != null && !clockSource.isEmpty() && !clockSource.equals("-1");
    }

    private void registerMediaScannerReceiver() {
        UsbEventsReceiver mUsbEventsReceiver = new UsbEventsReceiver();
        IntentFilter scanFinishFilter = new IntentFilter();
        scanFinishFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        scanFinishFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        scanFinishFilter.addDataScheme("file");
        getContext().registerReceiverAsUser(mUsbEventsReceiver, UserHandle.CURRENT_OR_SELF, scanFinishFilter, null, null);
    }

    private void setDiagnosticLocation() {
        mDiagonsticLocation = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DIAGNOSTIC_DESTINATION, 0, 0);
    }

    private void setGuestCheckingState() {
        mGuestCheckInState = SystemProperties.get("persist.sys.pmsaction");
    }

    private void setPMSSetting() {
        mPMSSettingState = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS, 0, 0);
    }

    private void setAppControlId() {
        mAppControlId = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_APPCONTROL_ID_TYPE, 0, 0);
    }

    private void setCULState() {
        mCULStatus = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DIAGNOSTIC_ENABLE, 0, 0);
    }

    public int getAppControlId() {
        return mAppControlId;
    }

    public int getCULStatus() {
        return mCULStatus;
    }

    public int getPMSSetting() {
        return mPMSSettingState;
    }

    public boolean isGuestCheckedIn() {
        boolean lRet = false;
        String action =	mGuestCheckInState;
        if(PMS_CHECK_IN.equals(action)){
            lRet = true;
        }else{
            lRet = false;
        }
        return lRet;
    }

    public int getDiagnosticLocation() {
       return mDiagonsticLocation;
    }
    public int getSmartTvMode() {
        return mSmartTVMode;
    }

    private void syncCustomizedSettingsToTVSettings() {
        int sidePanelBackgroundColor;
        if(isBFLProduct()){
            sidePanelBackgroundColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, getContext().getColor(R.color.side_panel_background_default_bfl));
            DdbLogUtility.logCommon("DashboardDataManager", "BFL syncCustomizedSettingsToTVSettigetSidePanelBackgroundColorng" +sidePanelBackgroundColor);
        }else{
            sidePanelBackgroundColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, getContext().getColor(R.color.side_panel_background_default));
            DdbLogUtility.logCommon("DashboardDataManager", "HFL syncCustomizedSettingsToTVSettings sidePanelBackgroundColor " +sidePanelBackgroundColor);
        }
        int sidePanelHighlightTextColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR, getContext().getColor(R.color.side_panel_highlighted_text_color_default));
        int sidePanelNonHighlightTextColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR, getContext().getColor(R.color.side_panel_non_highlighted_text_color_default));
        int mainBackgroundColorFilter = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER, getContext().getColor(R.color.main_background_default_color_filter));

        DdbLogUtility.logCommon("DashboardDataManager", "syncCustomizedSettingsToTVSettings sidePanelBackgroundColor " +mainBackgroundColorFilter
                                                                + " sidePanelHighlightTextColor " + sidePanelHighlightTextColor
                                                                + " sidePanelNonHighlightTextColor " + sidePanelNonHighlightTextColor
                                                                + " mainBackgroundColorFilter " + mainBackgroundColorFilter);
        saveSidePanelBackgroundColor(sidePanelBackgroundColor);
        saveSidePanelHighlightedTextColor(sidePanelHighlightTextColor);
        saveSidePanelNonHighlightedTextColor(sidePanelNonHighlightTextColor);
        saveMainBackgroundColorFilter(mainBackgroundColorFilter);
        if(hasSavedConfigurationPreferences()){
            Log.d("DashboardDataManager", "color have changed, so apply the new changes");
            changeSidePanelBackgroundColor(getSidePanelBackgroundColor());
            changeSidePanelNonHighlightedTextColor(getSidePanelNonHighlightedTextColor());
            changeSidePanelHighlightedTextColor(getSidePanelHighlightedTextColor());
        }

    }

    private void initCloneImageStateValues() {
        mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_DEFAULT_IMAGE_STATE_CHANGED);
    }

    private void setDefaultCloneImageState(){
        File castimageDirectory = new File(getContext().getFilesDir() + Constants.PATH_CAST_APP_SHARING_BACKGROUND);
        if(castimageDirectory.exists()){
            misSharingImageBackgroundCloned = true;
        }
        if(hasSavedImages()){
            misMainImageBackgroundCloned = true;
        }
    }

    private void initSourceDataManager() {
        mSourceDataManager = new SourceDataManager(getContext());
        mSourceDataManager.fetchAllSources();
    }

    private void initChannelDataManager() {
        mChannelDataManager = new ChannelDataManager(getContext());
        mChannelDataManager.fetchFilters();
    }

    private void initProgramDataManager() {
        mProgramDataManager = new ProgramDataManager(getContext());
    }

    private void initMessagesDataManager() {
        mMessagesDataManager = new MessagesDataManager(getContext());
    }

    private void initBillDataManager() {
        mBillDataManager = new BillDataManager(getContext());
    }

    private void initAccountDataManager() {
        mAccountDataManager = new AccountDataManager(getContext());
    }

    private void initWeatherDataManager() {
        mWeatherDataManager = new WeatherDataManager(getContext());
    }

    private void initThumbnailBrowserDataManager() {
        mImageDataManager = new ImageDataManager(getContext());
    }

    private void initPreviewProgramManager() {
        mPreviewProgramsManager = new PreviewProgramsDataManager(getContext());
    }


    private void initCloneDataManager() {
        mCloneDataManager = new CloneDataManager(getContext());
    }

    private void initFileDataManager() {
        mFileDataManager = new FileDataManager(getContext());
    }

    private void initOEMHelper() {
        mOemHelper = new OemHelper(getContext());
    }


    private ITvSettingsManagerNotify mSettingsCallbacks = new ITvSettingsManagerNotify() {
        @Override
        public void OnUpdate(int iProperty) {
            Log.d(DashboardDataManager.class.getSimpleName(), "### OnUpdate iProperty " + iProperty);
            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PREMISES_NAME) {
                if (mWeatherDataManager != null) {
                    mWeatherDataManager.onWeatherInfoReceivedNotify();
                }
                return;
            }
            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PMS_DISPLAYNAME) {
                if (mAccountDataManager != null) {
                    mAccountDataManager.notifyGuestNameChanged();
                }
                return;
            }

            if (iProperty == TvSettingsConstants.LASTSELECTEDURI) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.lastSelectedChannelUriChanged();
                }


            }
        }
    };

    private ITvSettingsManager.ITvSettingsManagerNotifyWithValue mSettingsWithValueCallbacks = new ITvSettingsManager.ITvSettingsManagerNotifyWithValue() {
        @Override
        public void OnUpdateWithIntValue(int iProperty, int value) {
            Log.d(DashboardDataManager.class.getSimpleName(), "### OnUpdateWithIntValue iProperty "+iProperty+ " value " + value);
            onMoreChapterUpdates(iProperty, value);

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DNT_CLOCK_FORMAT) {
				mClockFormat = value;
                notifyClockFormatChanged(value);
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_APPS_COUNTRY) {
                onAppsCountryChanged(value);
                return;
            }

            if (iProperty == TvSettingsConstants.MENULANGUAGE) {
                notifyPbsSettingLanguageChanged(value);
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS) {
                mPMSSettingState = value;
                if(mAppsDataManager != null){
                    mAppsDataManager.refreshEnabledAppList();
                }
               // updateProfessionalModeEnabledState(value);

                if (mBillDataManager != null) {
                    mBillDataManager.onBillSettingsChanged(value);
                }

                if (mMessagesDataManager != null) {
                    mMessagesDataManager.onMessageSettingsChanged(value);
                }
                if (mSourceDataManager != null) {
                    mSourceDataManager.sourcesChanged();
                }

                if (mWeatherDataManager != null) {
                    mWeatherDataManager.professionalModeChanged();
                }

                notifyDashboardPmsStateChanged(value);
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_CATEGORY_FILTER_ENABLE) {
                notifyAppCategoryFilterStateChanged(value);
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_COUNTRY_FILTER_ENABLE) {
                onAppCountryFilterEnabledStateChanged(value);
                return;
            }

            //if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PRODUCT_TYPE){
             //   onProductTypeStateChanged(value);
             //   return;
            //}

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE ||
                    iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE ||
                    iProperty == TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE ||
                    iProperty == TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE ||
                    iProperty == TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE) {

                if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE){
                    updateProfessionalModeEnabledState(value);
                    if(mAppsDataManager != null){
                        mAppsDataManager.refreshEnabledAppList();
                    }
                }

                if (mBillDataManager != null) {
                    mBillDataManager.onBillSettingsChanged(value);
                }

                if (mMessagesDataManager != null) {
                    mMessagesDataManager.onMessageSettingsChanged(value);
                }
                if (mSourceDataManager != null) {
                    mSourceDataManager.sourcesChanged();
                }

                if (mWeatherDataManager != null) {
                    mWeatherDataManager.professionalModeChanged();
                }

                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_BILL) {
                if (mBillDataManager != null) {
                    mBillDataManager.onBillSettingsChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_GUESTMESSAGES) {
                if (mMessagesDataManager != null) {
                    mMessagesDataManager.onMessageSettingsChanged(value);
                }
                return;
            }


            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO) {
                if (mSmartInfoDataManager != null) {
                    mSmartInfoDataManager.onSmartInfoModeChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_BROWSER_SETTINGS_SOURCE) {
                if (mSmartInfoDataManager != null) {
                    mSmartInfoDataManager.onSmartInfoBrowserSettingsSourceChanged(value);
                }

                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURES_SMARTINFO_APPS) {
                if(mSmartInfoDataManager != null){
                    mSmartInfoDataManager.fetchSmartInfo();
                }
                return;
            }

            if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_APPS){
                if(mSmartInfoDataManager != null){
                    mSmartInfoDataManager.fetchSmartInfo();
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_ACTIVE_CHANNELFILTER) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.activeChannelFilterChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_SHOW_CHANNELS) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableChannelsSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_All_CHANNELS) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableDisplayFilterAllChannels(value);
                }
                return;
            }


            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_OTT_APP) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableDisplayFilterOTTApp(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_SOURCES) {
                if (mChannelDataManager != null) {
                    mSourceDataManager.enableDisplayFilterSources(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_OTT_APP_CHANNELS) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableOTTAppInChannels(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_TV_CHANNELS) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableTvChannelsSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_RADIO_CHANNELS) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableRadioChannelsSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_AV_MEDIA) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableMediaChannelsSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_TIF_IN_CHANNEL_LIST) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableTifChannelsSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_THEME_TV_ENABLE) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableThemeTvSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SELECTABLE_AV) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.sourcesSettingChanged();
                }

                if (mSourceDataManager != null) {
                    mSourceDataManager.sourcesChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_EPG) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableEpgSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.INSTSETTINGSDVBEPGCHOICE) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.epgSourceChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.INSTSETTINGSCHANNELLOGOS) {
                if (mChannelDataManager != null) {
                    mChannelDataManager.enableChannelLogosSettingChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FORCE_CUSTOM_NAME) {
                if (mWeatherDataManager != null) {
                    mWeatherDataManager.onWeatherInfoReceivedNotify(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_WEATHER_APP) {
                if (mWeatherDataManager != null) {
                    mWeatherDataManager.onWeatherSettingsChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_MY_CHOICE_MYCHOICE) {
                if (mMyChoiceDataManager != null) {
                    mMyChoiceDataManager.updateMyChoiceEnabledState(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_GOOGLE_CAST) {
                if (mSourceDataManager != null) {
                    mGoogleCastDataManager.onGoogleCastEnabledStateSettingChanged(value);
                    mSourceDataManager.sourcesChanged();
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DASHBOARD) {
                onDashboardModeChanged(value);
                if(mAppsDataManager != null){
                    mAppsDataManager.refreshEnabledAppList();
                }
                return;
            }

            if (iProperty == TvSettingsConstants.LASTSELECTEDDEVICE) {
                if (mSourceDataManager != null) {
                    mSourceDataManager.lastSelectedDeviceChanged(value);
                }
                return;
            }

           if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SWITCH_ON_CHANNEL_FILTER){
                if (mChannelDataManager != null) {
                    mChannelDataManager.activeChannelFilterChanged(value);
                }
                return;
            }

            if (iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SWITCH_ON_CHANNEL){
                if (mChannelDataManager != null) {
                    mChannelDataManager.activeChannelFilterChanged(value);
                }
                return;
            }

            if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_SMARTTV_MODE){
				mSmartTVMode = value;
                if(mAppsDataManager != null){
                    mAppsDataManager.refreshEnabledAppList();
                }
                return;
            }

            if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_APPSYNC_CONFIG_STATUS){
                if(mAppsDataManager != null){
                    mAppsDataManager.refreshEnabledAppList();
                }
                return;
            }

            if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DIAGNOSTIC_ENABLE){
                if(mAppsDataManager != null){
                    mCULStatus = value;
                    mAppsDataManager.refreshEnabledAppList();
                }
                return;
            }

            if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_APPCONTROL_ID_TYPE){
                if(mAppsDataManager != null){
                    mAppControlId = value;
                    mAppsDataManager.refreshEnabledAppList();
                }
                return;
            }

            if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_DIAGNOSTIC_DESTINATION){
                if(mAppsDataManager != null){
                    mDiagonsticLocation = value;
                    mAppsDataManager.refreshEnabledAppList();
                }
                return;
            }
			if(iProperty == TvSettingsConstants.PBSMGR_PROPERTY_SHARING_SHOW_TNC ||
                    iProperty == TvSettingsConstants.PBSMGR_PROPERTY_HTV_SWITCH_ON_GUEST_MANAGEMENT_ANDROID_SYSTEM_MENU_LANGUAGE){
                if(mGoogleCastDataManager != null){
                    mGoogleCastDataManager.onSettingChanged(iProperty , value);
                }
                return;
            }
        }
    };

    private void onMoreChapterUpdates(int iProperty, int value) {
        if(mMoreChapterFragmentWeakRef == null) return;

        MoreChapterFragment moreChapterFragment = mMoreChapterFragmentWeakRef.get();
        if(moreChapterFragment != null) {
            moreChapterFragment.onTvSettingsValueChanged(iProperty, value);
        }
    }

    public void registerMoreChapterUpdates(MoreChapterFragment moreChapterFragment){
        mMoreChapterFragmentWeakRef = new WeakReference<>(moreChapterFragment);
    }

    public void unRegisterMoreChapterUpdates(){
        mMoreChapterFragmentWeakRef = null;
    }

    private BroadcastReceiver mTimeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DdbLogUtility.logCommon("DashboardDataManager", "onReceive() action " + action);
            if (Intent.ACTION_TIME_CHANGED.equals(action) ) {
                notifyTimeChanged();
                return;
            }

            if(Constants.ACTION_VALID_CLOCK_SOURCE_ACQUIRED.equals(action)){
                isClockAvailable = true;
                notifyTimeChanged();
                return;
            }

            if (Intent.ACTION_TIME_TICK.equals(action)) {
                notifyTimeTick();
                return;
            }
        }
    };

    private void initSmartInfoDataManager() {
        mSmartInfoDataManager = new SmartInfoDataManager(getContext());
        mSmartInfoDataManager.fetchSmartInfo();
    }

    private void initRecommendationDataManager() {
        mRecommendationDataManager = new RecommendationDataManager(getContext());
    }

    public Context getContext() {
        return super.getContext();
    }

    public ITvSettingsManager getTvSettingsManager() {
        return mTvSettingsManager;
    }

    public void fetchAvailableChannelFilters() {
        mChannelDataManager.fetchAvailableChannelFilters();
    }

    public void fetchActiveChannelFilter() {
        mChannelDataManager.fetchActiveChannelFilter();
    }

    public List<ChannelFilter> getAvailableChannelFilters() {
        return mChannelDataManager.getAvailableChannelFilters();
    }

    private void initVideoOnDemandDataManager() {
        mVideoOnDemandDataManager = new VideoOnDemandDataManager(getContext());
        mVideoOnDemandDataManager.buildVodRecommendationMap();
    }

    private void initGamesDataManager() {
        mGamesDataManager = new GamesDataManager(getContext());
        mGamesDataManager.buildGameRecommendationMap();
    }

    private void initAppsDataManager() {
        mAppsDataManager = new AppsDataManager(getContext());
        mAppsDataManager.buildRecommendedAppsList();
        mAppsDataManager.fetchEnabledAppList();
    }

    private void initGoogleCastDataManager() {
        mGoogleCastDataManager = new GoogleCastDataManager(getContext());
    }

    private void initMyChoiceDataManager() {
        mMyChoiceDataManager = new MyChoiceDataManager(getContext());
        mMyChoiceDataManager.updateMyChoiceItems();
    }

    public void onConfigurationChanged() {
        mSourceDataManager.onConfigurationChanged();
        mChannelDataManager.onConfigurationChanged();
        mSmartInfoDataManager.onConfigurationChanged();
    }

    public Map<String, List<Recommendation>> getVodRecommendations() {
        return mVideoOnDemandDataManager.getVodRecommendations();
    }

    public Map<String, List<Recommendation>> getGameRecommendations() {
        return mGamesDataManager.getGameRecommendations();
    }

    public List<AppInfo> getGameAppList() {
        return mGamesDataManager.getGameAppList();
    }

    public List<AppInfo> getRecommendedApps() {
        return mAppsDataManager.getRecommendedApps();
    }

    public List<AppInfo> getAppsEnabledForRecommendations() {
        return mAppsDataManager.getAppsEnabledForRecommendations();
    }

    public int getAppPostionForPackage(String packageName){
        return mAppsDataManager.getAppPositionForPackage(packageName);
    }

    public int getPbsCountrySelectedCode() {
        return mAppsCountry;
    }

    public String getCountryCode(int CountryConstant) {
        return mAppsDataManager.getCountryCode(CountryConstant);
    }

    public List<AppInfo> getAppsByCategory(String category) {
        return mAppsDataManager.getAppsByCategory(category);
    }

    public List<AppInfo> getAppsByCountry(String countryCode) {
        return mAppsDataManager.getAppsByCountry(countryCode);
    }

    public List<AppInfo> getAppsByCountryAndAllCategory(String countryCode) {
        return mAppsDataManager.getAppsByCountryAndAllCategory(countryCode);
    }

    public List<AppInfo> getEnabledAppList() {
        return mAppsDataManager.getEnabledAppList();
    }

    public Map<String, List<Recommendation>> getAppRecommendations() {
        return mAppsDataManager.getAppRecommendations();
    }

    public List<PreviewProgramsChannel> getPreviewProgramsChannelList(String packageName) {
        return mPreviewProgramsManager.getPreviewProgramsChannelList(packageName);
    }

    public List<PreviewProgramsChannel> getVodPreviewProgramsChannelList(String packageName) {
        return mPreviewProgramsManager.getVodPreviewProgramsChannelList(packageName);
    }

    public int getPreviewProgramsChannelCount(String packageName) {
        return mPreviewProgramsManager.getPreviewProgramsChannelCount(packageName);
    }
    public int getVodPreviewProgramsChannelCount(String packageName) {
        return mPreviewProgramsManager.getVodPreviewProgramsChannelCount(packageName);
    }

    public Map<String, PreviewProgramsChannel> getVodPreviewProgramChannelList(){
        return mVideoOnDemandDataManager.getVodPreviewProgramRecommendations();
    }

    public void fetchPreviewProgramsChannels() {
        if((mPreviewProgramsManager.getPreviewChannleRecommendations() == null) ||
                (mPreviewProgramsManager.getPreviewChannleRecommendations() != null && mPreviewProgramsManager.getPreviewChannleRecommendations().isEmpty())) {
            mPreviewProgramsManager.fetchPreviewPrograms();
        }
    }

    public void fetchPreviewProgramsVODChannels() {
        if(mPreviewProgramsManager.getPreviewChannleRecommendations() == null || mPreviewProgramsManager.getPreviewChannleRecommendations().isEmpty()) {
            mPreviewProgramsManager.fetchVodPreviewPrograms();
        }
    }

    public void fetchPreviewProgramsGamesChannels() {
        if(mPreviewProgramsManager.getPreviewChannleRecommendations() == null || mPreviewProgramsManager.getPreviewChannleRecommendations().isEmpty()) {
            mPreviewProgramsManager.fetchGamesPreviewPrograms();
        }else{
            Log.d("DashboardDataManager", "fetchPreviewProgramsGamesChannels: already fetched");
        }
    }

    public Map<String, PreviewProgramsChannel> getGamePreviewRecommendationMap(){
       return mGamesDataManager.getGamePreviewRecommendationMap();
    }

    public void fetchEnabledAppList() {
        mAppsDataManager.fetchEnabledAppList();
    }

    public void fetchAppLogo(String packageName, AppLogoFetchListener appLogoFetchListener) {
        mAppsDataManager.fetchAppLogo(packageName, appLogoFetchListener);
    }

    public void updateRecommendedAppEnabledState(Map<String, Boolean> recommendedAppEnabledStateMap) {
        mAppsDataManager.updateRecommendedAppEnabledState(recommendedAppEnabledStateMap);
    }

    public void updateAppRecommendationEnabledState(Map<String, Boolean> appRecommendationEnabledStateMap) {
        mAppsDataManager.updateAppRecommendationEnabledState(appRecommendationEnabledStateMap);
    }

    public void clearAppLogoCache() {
        mAppsDataManager.clearAppLogoCache();
    }

    public void fetchAllApps(AllAppListFetchListener allAppListFetchListener) {
        mAppsDataManager.fetchAllApps(allAppListFetchListener);
    }

    public List<AppInfo> getAllApps(){
       return mAppsDataManager.getAllApps();
    }
    public List<AppInfo> getAllUserInstalledApps(){
        return mAppsDataManager.getAllUserInstalledApps();
    }

    public ChannelFilter getActiveChannelFilter() {
        return mChannelDataManager.getActiveChannelFilter();
    }

    public Drawable getSourceIcon(Presenter.ViewHolder holder, Channel channel){
        return mChannelDataManager.getSourceIcon(holder, channel);
    }


    public int getActiveChannelFilterId() {
        return mChannelDataManager.getActiveChannelFilterId();
    }

    public void setActiveChannelFilterId(int filterId) {
        mChannelDataManager.setActiveChannelFilterId(filterId);
    }

    public String getActiveTifInputId() {
        return mChannelDataManager.getActiveTifInputId();
    }

    public void setActiveTifInputId(String tifInputId) {
        mChannelDataManager.setActiveTifInputId(tifInputId);
    }

    public void cleanUpChannelFilters(List<ChannelFilter> filters) {
        mChannelDataManager.cleanUpChannelFilters(filters);
    }

    public void cleanUpChannelFilter(ChannelFilter filter) {
        mChannelDataManager.cleanUpChannelFilter(filter);
    }

    public void cleanUpAvailableChannelFilters() {
        mChannelDataManager.cleanUpAvailableChannelFilters();
    }

    public void cleanUpActiveChannelFilter() {
        mChannelDataManager.cleanUpActiveChannelFilter();
    }

    public void fetchChannelLogo(Channel channel, ChannelLogoFetchListener listener) {
        mChannelDataManager.fetchChannelLogo(channel, listener);
    }

    public void clearChannelLogoCache() {
        mChannelDataManager.clearChannelLogoCache();
    }

    public int getLastSelectedChannelId() {
        return mChannelDataManager.getLastSelectedChannelId();
    }

    public boolean registerChannelDataListener(ChannelDataListener listener) {
        return mChannelDataManager.registerChannelDataListener(listener);
    }

    public boolean unregisterChannelDataListener(ChannelDataListener listener) {
        return mChannelDataManager.unregisterChannelDataListener(listener);
    }

    public boolean registerChannelSettingsListener(ChannelSettingsListener listener) {
        return mChannelDataManager.registerChannelSettingsListener(listener);
    }

    public boolean unregisterChannelSettingsListener(ChannelSettingsListener listener) {
        return mChannelDataManager.unregisterChannelSettingsListener(listener);
    }

    public boolean registerSourceDataListener(SourceDataListener listener) {
        return mSourceDataManager.registerSourceDataListener(listener);
    }

    public boolean unregisterSourceDataListener(SourceDataListener listener) {
        return mSourceDataManager.unregisterSourceDataListener(listener);
    }

    public List<Source> getSources() {
        return mSourceDataManager.getSources();
    }

    public Source getSource(String inputId) {
        return mSourceDataManager.getSource(inputId);
    }

    public String getTvInputLabel(String tvInputId) {
        return mSourceDataManager.getTvInputLabel(getContext(), tvInputId);
    }

    public Drawable getTvInputIcon(String tvInputId) {
        return mSourceDataManager.getTvInputIcon(getContext(), tvInputId);
    }

    public boolean isNAFTA(){
        return (mRegion == TvSettingsDefinitions.OpRegionDisplayConstants.US);
    }

    public boolean isBFLProduct(){
        return (mProductType == TvSettingsDefinitions.PbsAdvanceProductType.PBSMGR_ADVANCE_PRODUCT_TYPE_BUSINESS);
    }

    public boolean isHFLProduct(){
        return (mProductType == TvSettingsDefinitions.PbsAdvanceProductType.PBSMGR_ADVANCE_PRODUCT_TYPE_HOSPITALITY);
    }

    public int getOffset(){
        int offset = 0;
        int optionRegion = getTvSettingsManager().getInt(TvSettingsConstants.OPREGION, 0, 0);
        if(optionRegion == OPTION_REGION_EU){
            offset = OFFSET_EU;
        }else{
            offset = OFFSET_NAFTA;
        }
        DdbLogUtility.logCommon(DashboardDataManager.class.getSimpleName(),"Offset :"+offset);
        return offset;
    }

    public boolean areSourcesEnabled() {
        return mSourceDataManager.areSourcesEnabled();
    }

    public boolean isHdmi1Enabled() {
        return mSourceDataManager.isHdmi1Enabled();
    }

    public boolean isHdmi2Enabled() {
        return mSourceDataManager.isHdmi2Enabled();
    }

    public boolean isHdmi3Enabled() {
        return mSourceDataManager.isHdmi3Enabled();
    }

    public boolean isHdmi4Enabled() {
        return mSourceDataManager.isHdmi4Enabled();
    }

    public boolean isVgaEnabled() {
        return mSourceDataManager.isVgaEnabled();
    }

    public boolean isUsbBrowserEnabled() {
        return mSourceDataManager.isUsbBrowserEnabled();
    }

    public int getLastSelectedDevice() {
        return mSourceDataManager.getLastSelectedDevice();
    }

    public boolean isGoogleCastEnabled() {
        return mGoogleCastDataManager.isGoogleCastEnabled();
    }

    public boolean isProfessionalModeEnabled() {
        return mIsProfessionalModeEnabled;
    }

    public boolean isDashboardModeCustom() {
        return mDashboardMode != DASHBOARD_MODE_PHILIPS_HOME;
    }

    public boolean isAirServerAvailable() {
        return mSourceDataManager.isAirServerAvailable();
    }

    public void fetchProgramDataForChannel(Channel channel, ProgramDataListener listener) {
        mProgramDataManager.fetchProgramDataForChannel(channel, listener);
    }

    public void fetchProgramThumbnail(Program program, Channel channel, ProgramThumbnailFetchListener listener) {
        mProgramDataManager.fetchProgramThumbnail(program, channel, listener);
    }

    public void clearProgramThumbnailCache() {
        mProgramDataManager.clearProgramThumbnailCache();
    }

    public int fetchContentRatingStringId(String contentRating) {
        return mProgramDataManager.fetchContentRatingStringId(contentRating);
    }

    public int fetchContentRatingStringId(int contentRatingAgeHint) {
        return mProgramDataManager.fetchContentRatingStringId(contentRatingAgeHint);
    }

    public int fetchContentRatingAgeHint(String contentRating) {
        return mProgramDataManager.fetchContentRatingAgeHint(contentRating);
    }

    public void fetchAccounts(AccountDataListener listener) {
        mAccountDataManager.fetchAccounts(listener);
    }

    public void saveImage(final String imageFilePath, final String destinationPath, FileDataListener listener) {
        mFileDataManager.copyFile(imageFilePath, destinationPath, null);
    }

    public void removeSavedImage(String imageFilePath, FileDataListener listener) {
        mFileDataManager.deleteFile(imageFilePath, listener);
    }

    public void fetchImage(String filePath, int width, int height, DashboardDataManager.ImageFetchListener listener) {
        mImageDataManager.fetchImage(filePath, width, height, listener);
    }

    public void deleteAndCopyImageToCastAppDirectory(final String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            mFileDataManager.deleteFile(Constants.PATH_CAST_APP_SHARING_BACKGROUND, new FileDataListener() {
                @Override
                public void onFileCopyComplete(boolean success) {
                }

                @Override
                public void onFileDeleteComplete(boolean success) {
                    DdbLogUtility.logCommon("DashboardDataManager",  "onFileDeleteComplete  " + success);
                    mFileDataManager.copyFile(filePath, Constants.PATH_CAST_APP_SHARING_BACKGROUND, new FileDataListener() {
                        @Override
                        public void onFileCopyComplete(boolean success) {
                            setPermissions(Constants.PATH_CAST_APP_SHARING_BACKGROUND, filePath);
                            loadCastBackground(filePath);
                        }

                        @Override
                        public void onFileDeleteComplete(boolean success) {
                        }
                    });
                }
            });
        }
    }

    public void setPermissions(String castImageDirPath, String castImageFilePath){
        try {
            File castImageDir = new File(castImageDirPath);
            File castImageFile = new File(castImageFilePath);

            setCastImageDirPermissions(castImageDir);
            setCastImageFilePermissions(castImageFile);
        }catch(NullPointerException e){Log.e("DashBoardDataManager","Exception :"+e.getMessage());}
    }

    /**
     * Below function will change the permission to 777 of below path
     * PATH=/data/misc/HTV/cast_background/
     * @param castImageFile
     */
    private void setCastImageDirPermissions(File castImageDir) {
        Log.d("DashboardDataManager", "setCastImageDirPermissions: path  " + castImageDir.getAbsolutePath());
        if (castImageDir != null && castImageDir.exists()) {
           FileUtils.setPermissions(castImageDir, FileUtils.S_IRWXG | FileUtils.S_IRWXO | FileUtils.S_IRWXU, -1, -1);
        }
    }

    /**
     * Below function will change the permission to 777 of below path
     * PATH=/data/misc/HTV/cast_background/<cast_image_file_name>
     * @param castImageFile
     */
    private void setCastImageFilePermissions(File castImageFile) {
        String castImageFileName = castImageFile.getName();
                    //FilePath = /storage/C4DA-2200/OLD_TPM181HE_CloneData/MasterCloneData/ChannelList/ThemeIcons/default/Spanish.png
        String castSharingPath = Constants.PATH_CAST_APP_SHARING_BACKGROUND + "/" + castImageFileName;
                    //PATH=/data/misc/HTV/cast_background/<cast_image_file_name>
        Log.d("DashboardDataManager", "setCastImageFilePermissions castSharingPath " + castSharingPath);
        File castSharingFile = new File(castSharingPath);
        if(castSharingFile != null && castSharingFile.exists()){
            FileUtils.setPermissions(castSharingFile, FileUtils.S_IRWXG | FileUtils.S_IRWXO | FileUtils.S_IRWXU, -1, -1);
        }
    }


    public void loadSavedCastBacktound() {
        if(castBcakgroundBitmapFetchOngoing){
            Log.d(TAG, "loadSavedCastBacktound: ignored castBcakgroundBitmapFetchOngoing " + castBcakgroundBitmapFetchOngoing);
            return;
        }
        setCastBackgroundFetchOngoing(true);
        fetchSavedImageFile(getContext().getFilesDir() + Constants.PATH_SHARING_BACKGROUND, new DashboardDataManager.ImageFileFetchListener() {
            @Override
            public void onImageFileFetched(DashboardDataManager.ImageFile imageFile) {
                if (imageFile != null && imageFile.getFile() != null && imageFile.getFile().exists()) {
                    loadCastBackground(imageFile.getFile().getAbsolutePath());
                }
            }
        });
    }

    private void loadCastBackground(final String filePath) {
        fetchImage(filePath, Constants.MAIN_BACKGROUND_WIDTH, Constants.MAIN_BACKGROUND_HEIGHT, new DashboardDataManager.ImageFetchListener() {

            @Override
            public void onImageFetchComplete(Bitmap bitmap) {
                setCastBackgroundFetchOngoing(false);
                for (int i = 0; mBackgroundImageChangeObserverRefs != null && i < mBackgroundImageChangeObserverRefs.size(); i++) {
                    WeakReference<BackgroundImageChangeObserver> listenerRef = mBackgroundImageChangeObserverRefs.get(i);
                    if (listenerRef == null) {
                        continue;
                    }

                    BackgroundImageChangeObserver listener = listenerRef.get();
                    if (listener != null) {
                        listener.changeCastBackgroundImage(bitmap);
                    }
                }
            }
        });
    }

    public void loadCastBackground(int drawableResourceId) {
        mImageDataManager.fetchImageFromResourceId(drawableResourceId, Constants.MAIN_BACKGROUND_WIDTH, Constants.MAIN_BACKGROUND_HEIGHT, new ImageFetchListener() {
            @Override
            public void onImageFetchComplete(Bitmap bitmap) {
                setCastBackgroundFetchOngoing(false);
                for (int i = 0; mBackgroundImageChangeObserverRefs != null && i < mBackgroundImageChangeObserverRefs.size(); i++) {
                    WeakReference<BackgroundImageChangeObserver> listenerRef = mBackgroundImageChangeObserverRefs.get(i);
                    if (listenerRef == null) {
                        continue;
                    }

                    BackgroundImageChangeObserver listener = listenerRef.get();
                    if (listener != null) {
                        listener.changeCastBackgroundImage(bitmap);
                    }
                }
            }
        });
    }

    public void setBackground(String filePath) {
        DdbLogUtility.logCommon("DashboardDataManager", "setBackground() called with: filePath = " + filePath);
        mImageDataManager.fetchImage(filePath, Constants.MAIN_BACKGROUND_WIDTH, Constants.MAIN_BACKGROUND_HEIGHT, new ImageFetchListener() {
            @Override
            public void onImageFetchComplete(Bitmap bitmap) {
                setMainBackgroundBitmapFetchOngoing(false);
                for (int i = 0; mBackgroundImageChangeObserverRefs != null && i < mBackgroundImageChangeObserverRefs.size(); i++) {
                    WeakReference<BackgroundImageChangeObserver> listenerRef = mBackgroundImageChangeObserverRefs.get(i);
                    if (listenerRef == null) {
                        continue;
                    }
                    BackgroundImageChangeObserver listener = listenerRef.get();
                    if (listener != null) {
                        listener.changeBackgroundImage(bitmap);
                    }
                }
            }
        });
    }

    public void setBackground(int drawableResourceId) {
        mImageDataManager.fetchImageFromResourceId(drawableResourceId, Constants.MAIN_BACKGROUND_WIDTH, Constants.MAIN_BACKGROUND_HEIGHT, new ImageFetchListener() {
            @Override
            public void onImageFetchComplete(Bitmap bitmap) {
                setMainBackgroundBitmapFetchOngoing(false);
                for (int i = 0; mBackgroundImageChangeObserverRefs != null && i < mBackgroundImageChangeObserverRefs.size(); i++) {
                    WeakReference<BackgroundImageChangeObserver> listenerRef = mBackgroundImageChangeObserverRefs.get(i);
                    if (listenerRef == null) {
                        continue;
                    }
                    BackgroundImageChangeObserver listener = listenerRef.get();
                    if (listener != null) {
                        listener.changeBackgroundImage(bitmap);
                    }
                }
            }
        });
    }

    public void setBackgroundColorFilter(int color) {
        DdbLogUtility.logCommon("DashboardDataManager", "setBackgroundColorFilter() called with: color = " + color );
        for (int i = 0; mBackgroundImageChangeObserverRefs != null && i < mBackgroundImageChangeObserverRefs.size(); i++) {
            WeakReference<BackgroundImageChangeObserver> listenerRef = mBackgroundImageChangeObserverRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            BackgroundImageChangeObserver listener = listenerRef.get();
            if (listener != null) {
                listener.changeBackgroundColorFilter(color);
            }
        }
    }

    public void clearBackground() {
        for (int i = 0; mBackgroundImageChangeObserverRefs != null && i < mBackgroundImageChangeObserverRefs.size(); i++) {
            WeakReference<BackgroundImageChangeObserver> listenerRef = mBackgroundImageChangeObserverRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            BackgroundImageChangeObserver listener = listenerRef.get();
            if (listener != null) {
                listener.clearBackground();
            }
        }
    }

    public boolean isMainBackgroundEnabled() {
        return mDefaultSharedPreferences.
                getBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED, getContext().getResources().getBoolean(R.bool.enable_main_background));
    }

    public int getMainBackgroundColorFilter() {
        return mDefaultSharedPreferences.
                getInt(Constants.PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER, getContext().getColor(R.color.main_background_default_color_filter));
    }

    public void saveMainBackgroundColorFilter(int color) {
        mDefaultSharedPreferences.edit().putInt(Constants.PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER, color).apply();
    }

    public void isClonedSharingImageBackground(boolean iscloned){
        misSharingImageBackgroundCloned = iscloned;
    }

    public void isClonedMainImageBackground(boolean iscloned){
        misMainImageBackgroundCloned = iscloned;
    }

    public void applyMainBackground() {
        DdbLogUtility.logCommon("DashboardDataManager", "applyMainBackground() mainBackgroundBitmapFetchOngoing " + mainBackgroundBitmapFetchOngoing);

        if(mainBackgroundBitmapFetchOngoing){
            Log.d(DashboardDataManager.class.getSimpleName(), "applyMainBackground: ignored ");
            return;
        }
        setMainBackgroundBitmapFetchOngoing(true);
        fetchSavedImageFile(getContext().getFilesDir() + Constants.PATH_MAIN_BACKGROUND, new ImageFileFetchListener() {
            @Override
            public void onImageFileFetched(ImageFile imageFile) {
                if (imageFile == null || imageFile.getFile() == null || !imageFile.getFile().exists()) {
                    if(isBFLProduct()){
                        setBackground(R.drawable.default_main_background_bfl);
                    }else{
                        setBackground(R.drawable.default_main_background);
                    }
                } else if(isBFLProduct() && !misMainImageBackgroundCloned){
                    setBackground(R.drawable.default_main_background_bfl);
                } else {
                    setBackground(imageFile.getFile().getPath());
                }
            }
        });
    }

    public void applySharingBackground() {
        DdbLogUtility.logCommon("DashboardDataManager", "applyMainBackground() castBcakgroundBitmapFetchOngoing " + castBcakgroundBitmapFetchOngoing);
        if(castBcakgroundBitmapFetchOngoing){
            Log.d(DashboardDataManager.class.getSimpleName(), "applyMainBackground: ignored ");
            return;
        }
        setCastBackgroundFetchOngoing(true);
        fetchSavedImageFile(getContext().getFilesDir() + Constants.PATH_CAST_APP_SHARING_BACKGROUND, new ImageFileFetchListener() {
            @Override
            public void onImageFileFetched(ImageFile imageFile) {
                if (imageFile == null || imageFile.getFile() == null || !imageFile.getFile().exists()) {
                    if (isBFLProduct()) {
                        loadCastBackground(R.drawable.cast_chapter_background_image_bfl);
                    } else {
                        loadCastBackground(R.drawable.cast_chapter_background_image);
                    }
                }else if(isBFLProduct() && !misSharingImageBackgroundCloned){
                    loadCastBackground(R.drawable.cast_chapter_background_image_bfl);
                } else {
                    loadCastBackground(imageFile.getFile().getPath());
                }
            }
        });
    }

    public void applyHotelLogo() {
        Log.d(DashboardDataManager.class.getSimpleName(), "applyHotelLogo: hotelLogoBitmapFetchOngoing " + hotelLogoBitmapFetchOngoing);
        if(hotelLogoBitmapFetchOngoing){
            Log.d(DashboardDataManager.class.getSimpleName(), "applyHotelLogo: ignored");
            return;
        }
        setHotelLogoBitmapFetchOngoing(true);
        fetchSavedImageFile(getContext().getFilesDir() + Constants.PATH_HOTEL_LOGO, new ImageFileFetchListener() {
            @Override
            public void onImageFileFetched(ImageFile imageFile) {
                if (imageFile == null || imageFile.getFile() == null || !imageFile.getFile().exists()) {
                    setHotelLogo(R.drawable.default_hotel_logo);
                } else {
                    setHotelLogo(imageFile.getFile().getPath());
                }
            }
        });
    }

    public void setHotelLogo(String filePath) {
        DdbLogUtility.logCommon("DashboardDataManager", "setHotelLogo filepath " + filePath);
        mImageDataManager.fetchImage(filePath, Constants.HOTEL_LOGO_WIDTH, Constants.HOTEL_LOGO_HEIGHT, new ImageFetchListener() {
            @Override
            public void onImageFetchComplete(Bitmap bitmap) {
                setHotelLogoBitmapFetchOngoing(false);
                for (int i = 0; mHotelLogoChangeObserverRefs != null && i < mHotelLogoChangeObserverRefs.size(); i++) {
                    WeakReference<HotelLogoChangeObserver> listenerRef = mHotelLogoChangeObserverRefs.get(i);
                    if (listenerRef == null) {
                        continue;
                    }

                    HotelLogoChangeObserver listener = listenerRef.get();
                    if (listener != null) {
                        listener.changeHotelLogo(bitmap);
                    }
                }
            }
        });
    }

    public void setHotelLogo(int drawableResourceId) {
        setHotelLogoBitmapFetchOngoing(false);
        for (int i = 0; mHotelLogoChangeObserverRefs != null && i < mHotelLogoChangeObserverRefs.size(); i++) {
            WeakReference<HotelLogoChangeObserver> listenerRef = mHotelLogoChangeObserverRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            HotelLogoChangeObserver listener = listenerRef.get();
            if (listener != null) {
                listener.changeHotelLogo(drawableResourceId);
            }
        }
    }

    public void fetchThumbnailsFromUsb(ThumbnailDataListener listener) {
        mImageDataManager.fetchThumbnailsFromUsb(listener);
    }

    public void fetchSavedImageFile(String path, ImageFileFetchListener listener) {
        mFileDataManager.fetchImageFileFromPath(path, listener);
    }

    public void stopFetchingThumbnailsFromUsb() {
        mImageDataManager.stopFetchingThumbnailsFromUsb();
    }

    public void fetchThumbnailImage(String filePath, long id, int thumbnailWidth, int thumbnailHeight, ThumbnailBitmapFetchListener listener) {
        mImageDataManager.fetchThumbnailImage(filePath, id, thumbnailWidth, thumbnailHeight, listener);
    }

    public void fetchThumbnailImageFromResource(int drawableResourceId, long id, int thumbnailWidth, int thumbnailHeight, ThumbnailBitmapFetchListener listener) {
        mImageDataManager.fetchThumbnailImageFromResource(drawableResourceId, id, thumbnailWidth, thumbnailHeight, listener);
    }

    public void cancelFetchingThumbnailImage(long id) {
        mImageDataManager.cancelFetchingThumbnailImage(id);
    }

    public boolean addBackgroundImageChangeObserver(BackgroundImageChangeObserver backgroundImageChangeObserver) {
        if (backgroundImageChangeObserver == null) {
            return false;
        }
        return mBackgroundImageChangeObserverRefs.add(new WeakReference<BackgroundImageChangeObserver>(backgroundImageChangeObserver));
    }

    public boolean removeBackgroundImageChangeObserver(BackgroundImageChangeObserver backgroundImageChangeObserver) {
        if (mBackgroundImageChangeObserverRefs == null) {
            return false;
        }
        for (int i = 0; i < mBackgroundImageChangeObserverRefs.size(); i++) {
            WeakReference<BackgroundImageChangeObserver> ref = mBackgroundImageChangeObserverRefs.get(i);
            if (ref == null) {
                continue;
            }
            BackgroundImageChangeObserver listener = ref.get();
            if (listener != null && listener.equals(backgroundImageChangeObserver)) {
                return mBackgroundImageChangeObserverRefs.remove(ref);
            }
        }
        return false;
    }

    public boolean addHotelLogoChangeObserver(HotelLogoChangeObserver hotelLogoChangeObserver) {
        if (hotelLogoChangeObserver == null) {
            return false;
        }
        return mHotelLogoChangeObserverRefs.add(new WeakReference<HotelLogoChangeObserver>(hotelLogoChangeObserver));
    }

    public boolean removeHotelLogoChangeObserver(HotelLogoChangeObserver hotelLogoChangeObserver) {
        if (mHotelLogoChangeObserverRefs == null) {
            return false;
        }
        for (int i = 0; i < mHotelLogoChangeObserverRefs.size(); i++) {
            WeakReference<HotelLogoChangeObserver> ref = mHotelLogoChangeObserverRefs.get(i);
            if (ref == null) {
                continue;
            }
            HotelLogoChangeObserver listener = ref.get();
            if (listener != null && listener.equals(hotelLogoChangeObserver)) {
                return mHotelLogoChangeObserverRefs.remove(ref);
            }
        }
        return false;
    }

    public SharedPreferences getDefaultSharedPreferences() {
        return mDefaultSharedPreferences;
    }

    public void setMediaScannerInProgress(boolean mediaScannerInProgress) {
        mMediaScannerInProgress = mediaScannerInProgress;
    }

    public boolean isMediaScannerInProgress() {
        return mMediaScannerInProgress;
    }

    public void setGoogleAccountFlowListener(GoogleAccountFlowListener googleAccountFlowListener) {
        if (mGoogleAccountFlowListenerRef != null) {
            mGoogleAccountFlowListenerRef.clear();
        }
        mGoogleAccountFlowListenerRef = new WeakReference<GoogleAccountFlowListener>(googleAccountFlowListener);

    }

    public void startGoogleAccountFlow() {
        if (mGoogleAccountFlowListenerRef != null) {
            GoogleAccountFlowListener googleAccountFlowListener = mGoogleAccountFlowListenerRef.get();
            if (googleAccountFlowListener != null) {
                googleAccountFlowListener.startGoogleAccountFlow();
            }
        }
    }

    public int getGoogleAccountCount() {
        return mAccountDataManager.getGoogleAccountCount();
    }

    public String getGoogleEmailId() {
        return mAccountDataManager.getGoogleEmailId();
    }

    public GoogleSignInAccount getLastSignedInGoogleAccount() {
        return mAccountDataManager.getLastSignedInGoogleAccount();
    }

    public void fetchGoogleAccountImageBitmap(String personPhotoUrl, GoogleAccountImageListener googleAccountImageListener) {
        mAccountDataManager.fetchGoogleAccountImageBitmap(personPhotoUrl, googleAccountImageListener);
    }

    public boolean isAmbilightAvailable() {
        return mTvSettingsManager.getInt(TvSettingsConstants.OPALSIDESEGMENTS, 0, 0) != 0;
    }

    public boolean isSmartTvModeOff() {
        Log.d(DashboardDataManager.class.getSimpleName(), "Smart TV mode is " + mSmartTVMode);
        return mSmartTVMode == TvSettingsDefinitions.PbsSmarttvMode.PBSMGR_OFF;
    }

    public void setRecommendedChapterDisplayed(boolean displayed) { isRecommendedChapterDisplayed = displayed;}
    
    public boolean isRecommendedChapterDisplayed(){return isRecommendedChapterDisplayed;}

    public boolean isInternetHotspotAvailable() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_INTERNET_HOTSPOT, 0, 0) == 1
                && mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SECURE_SHARING, 0, 0) == 1;
    }

    public boolean isAppSyncProfileValid(){
        boolean isAppSyncProfileValid = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_APPSYNC_CONFIG_STATUS, 0, 0) == 0;
        DdbLogUtility.logCommon(DashboardDataManager.class.getSimpleName(), "isAppSyncProfileValid() " + isAppSyncProfileValid);
        return isAppSyncProfileValid;
    }

    public String getPremisesName() {
        return mWeatherDataManager.getPremisesName();
    }

    public String getGuestName() {
        return mAccountDataManager.getGuestName();
    }

    public String getGuestNameFromPms() {
        return mAccountDataManager.getGuestNameFromPms();
    }

    public String getCurrentTemperature() {
        return mWeatherDataManager.getCurrentTemperature();
    }

    public int getWeatherIcon() {
        return mWeatherDataManager.getWeatherIcon();
    }
    public boolean isCurrentChannelFilterSource(){
        return mChannelDataManager.getActiveChannelFilterId() == TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_SOURCE;
    }

    public boolean areDisplayFilterAllChannelsEnabled() {
        return mChannelDataManager.areDisplayFilterAllChannelsEnabled();
    }

    public void setUsbConnected(boolean status) { isUsbConnected = status;}

    public boolean isUsbConnected(){ return isUsbConnected; }

    public void fetchSmartInfoPreviewProgram(String packageName) {
        if(mPreviewProgramsManager != null) {
            mPreviewProgramsManager.fetchSmartInfoPreviewChannels(packageName);
        }
    }

    public PreviewProgramsDataManager getPreviewProgramDataManager() {
        return mPreviewProgramsManager;
    }
	 
    public boolean isTalkbackAvailable() {
        return (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_TALKBACK_ENABLE, 0, 0) == 1);
    }

    public interface WeatherInfoDataListener {
        void onWeatherInfoDataReceived();

        void onWeatherInfoDataReceived(int value);
    }

    public interface WeatherSettingsListener {
        void onWeatherSettingsChanged();

        void onWeatherSettingsChanged(int value);
    }

    public interface BillSettingsListener {
        void onBillSettingsChanged();

        void onBillSettingsChanged(int value);
    }

    public interface MessageSettingsListener {
        void onMessageSettingsChanged();

        void onMessageSettingsChanged(int value);
    }

    public void registerWeatherInfoDataListener(WeatherInfoDataListener listener) {
        mWeatherDataManager.registerWeatherInfoDataListener(listener);
    }

    public void unregisterWeatherInfoDataListener(WeatherInfoDataListener listener) {
        mWeatherDataManager.unregisterWeatherInfoDataListener(listener);
    }

    public void registerWeatherSettingsListener(WeatherSettingsListener listener) {
        mWeatherDataManager.registerWeatherSettingsListener(listener);
    }

    public void unregisterWeatherSettingsListener(WeatherSettingsListener listener) {
        mWeatherDataManager.unregisterWeatherSettingsListener(listener);
    }

    public boolean isWeatherEnabled() {
        return mWeatherDataManager.isWeatherEnabled();
    }

    public boolean isWeatherEnabled(int value) {
        return mWeatherDataManager.isWeatherEnabled(value);
    }


    boolean addMyChoiceListener(MyChoiceListener myChoiceListener) {
        return mMyChoiceDataManager.addMyChoiceListener(myChoiceListener);
    }

    boolean removeAppDataListener(MyChoiceListener myChoiceListener) {
        return mMyChoiceDataManager.removeMyChoiceListener(myChoiceListener);
    }

    public boolean isMyChoiceEnabled() {
        return mMyChoiceDataManager.isMyChoiceEnabled();
    }

    public boolean isTvGuideAvailable(){
        return isEpgEnabled();
    }


	
    public boolean isMyChoicePkg1Unlocked() {
        return mMyChoiceDataManager.isMyChoicePkg1Unlocked();
    }

    public boolean isMyChoicePkg2Unlocked() {
        return mMyChoiceDataManager.isMyChoicePkg1Unlocked();
    }

    public boolean isChannelMyChoiceLocked(Channel channel) {
        return mMyChoiceDataManager.isChannelMyChoiceLocked(channel);
    }

    public boolean isSourceMyChoiceLocked(String sourceInputId) {
        return mMyChoiceDataManager.isSourceMyChoiceLocked(sourceInputId);
    }

    public boolean areAppsMyChoiceLocked() {
        return mMyChoiceDataManager.areAppsMyChoiceLocked();
    }

    public boolean isGoogleCastMyChoiceLocked() {
        return mMyChoiceDataManager.isGoogleCastMyChoiceLocked();
    }

    public boolean isMediaBrowserMyChoiceLocked() {
        return mMyChoiceDataManager.isMediaBrowserMyChoiceLocked();
    }

    public void registerBillSettingsListener(BillSettingsListener listener) {
        mBillDataManager.registerBillSettingsListener(listener);
    }

    public void unregisterBillSettingsListener(BillSettingsListener listener) {
        mBillDataManager.unregisterBillSettingsListener(listener);
    }

    public boolean isBillAvailable() {
        return mBillDataManager.isBillAvailable();
    }

    public boolean isAccountItemAvailable(){
        return mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON, true);
    }
	
    public void registerMessageSettingsListener(MessageSettingsListener listener) {
        mMessagesDataManager.registerMessageSettingsListener(listener);
    }

    public void unregisterMessageSettingsListener(MessageSettingsListener listener) {
        mMessagesDataManager.unregisterMessageSettingsListener(listener);
    }

    public boolean isMessageAvailable() {
        return mMessagesDataManager.isMessageAvailable();
    }

    public boolean isMessageDisplayAvailable() {
        return mMessagesDataManager.isMessageDisplayAvailable();
    }

    public boolean isDeleteAccountAvailable() {
        return (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksPmsService.PBSMGR_NETWORKS_PMS_SERVICE_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsTvDiscoveryServiceConstants.PBSMGR_TVDISCOVERY_SERVICE_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenPmsService.PBSMGR_NETWORKS_WEB_LISTEN_PMS_SERVICE_ON) &&
                (mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE, 0, 0) == TvSettingsDefinitions.PbsNetworksWebListenTvDiscoveryService.PBSMGR_NETWORKS_WEB_LISTEN_TV_DISCOVERY_SERVICE_ON);
    }

    public boolean isClockAvailable() {
        return isClockAvailable;
    }

    public boolean isClockFormatAMPM() {
        return mClockFormat  == TvSettingsDefinitions.PbsClockFormatConstants.PBSMGR_CLOCK_FORMAT_AMPM;
    }

    public void registerClockSettingChangeListener(ClockSettingChangeListener listener) {
        mClockSettingChangeListenerRefs.add(new WeakReference<ClockSettingChangeListener>(listener));
    }

    public void unregisterClockSettingChangeListener(ClockSettingChangeListener clockSettingChangeListener) {
        if (mClockSettingChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mClockSettingChangeListenerRefs.size(); i++) {
            WeakReference<ClockSettingChangeListener> ref = mClockSettingChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ClockSettingChangeListener listener = ref.get();
            if (listener != null && listener.equals(clockSettingChangeListener)) {
                mClockSettingChangeListenerRefs.remove(ref);
            }
        }
    }

    public boolean isAlarmSet() {
        return SystemProperties.getInt(Constants.SYSTEM_PROPERTY_HTV_ALARM_STATE, 0) != 0;
    }

    public String getAlarmTime() {
        int hours = SystemProperties.getInt(Constants.SYSTEM_PROPERTY_HTV_ALARM_TIME_HOURS, 0);
        int minutes = SystemProperties.getInt(Constants.SYSTEM_PROPERTY_HTV_ALARM_TIME_MINUTES, 0);
        long alarmWakeupTimeInMillis = (long) hours * (long) Math.pow(10, 8) + minutes;
        Date alarmHasSetTime = new Date(alarmWakeupTimeInMillis);
        boolean isClockFormatAMorPM = isClockFormatAMPM();
        SimpleDateFormat formatAlarmTime = isClockFormatAMorPM ? new SimpleDateFormat(Constants.CLOCK_FORMAT_AM_PM) : new SimpleDateFormat(Constants.CLOCK_FORMAT_24);
        String alarmTime = formatAlarmTime.format(alarmHasSetTime);
        if(isClockFormatAMorPM){
            String AMorPM = (alarmHasSetTime.getHours() >=0 && alarmHasSetTime.getHours() < 12 ) ? mDeviceContext.getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_AM) : mDeviceContext.getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_PM);
            alarmTime = alarmTime + AMorPM;
        }
        return alarmTime;
    }

    public interface AlarmChangeListener {
        void updateAlarm();
    }

    public interface ClockSettingChangeListener {
        void onClockFormatChanged();

        void onClockFormatChanged(int value);

        void onTimeChanged();

        void onTimeTick();
    }

    public void addAlarmChangeListener(AlarmChangeListener alarmChangeListener) {
        mAlarmChangeListenerRefs.add(new WeakReference<AlarmChangeListener>(alarmChangeListener));
    }

    public void removeAlarmChangeListener(AlarmChangeListener alarmChangeListener) {
        if (mAlarmChangeListenerRefs == null) {
            return;
        }
        if (mAlarmChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mAlarmChangeListenerRefs.size(); i++) {
                WeakReference<AlarmChangeListener> ref = mAlarmChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                AlarmChangeListener listener = ref.get();
                if (listener != null && listener.equals(alarmChangeListener)) {
                    mAlarmChangeListenerRefs.remove(ref);
                }
            }
        }
    }

    public void updateAlarmTime() {
        if (mAlarmChangeListenerRefs == null) {
            return;
        }
        if (mAlarmChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mAlarmChangeListenerRefs.size(); i++) {
                WeakReference<AlarmChangeListener> ref = mAlarmChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                AlarmChangeListener listener = ref.get();
                if (listener != null) {
                    listener.updateAlarm();
                }
            }
        }
    }

    public void registerGuestNameChangeListener(GuestNameChangeListener guestNameChangeListener) {
        mAccountDataManager.registerGuestNameChangeListener(guestNameChangeListener);
    }

    public void unregisterGuestNameChangeListener(GuestNameChangeListener guestNameChangeListener) {
        mAccountDataManager.unregisterGuestNameChangeListener(guestNameChangeListener);
    }

    public void registerAppCategoryFilterStateChangeListener(AppCategoryFilterStateChangeListener appCategoryFilterStateChangeListener) {
        if (null != appCategoryFilterStateChangeListener) {
            mAppCategoryFilterStateChangeListenerRefs.add(new WeakReference<AppCategoryFilterStateChangeListener>(appCategoryFilterStateChangeListener));
        }
    }

    public void unregisterAppCategoryFilterStateChangeListener(AppCategoryFilterStateChangeListener appCategoryFilterStateChangeListener) {
        if (mAppCategoryFilterStateChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mAppCategoryFilterStateChangeListenerRefs.size(); i++) {
            WeakReference<AppCategoryFilterStateChangeListener> ref = mAppCategoryFilterStateChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppCategoryFilterStateChangeListener listener = ref.get();
            if (listener != null && listener.equals(appCategoryFilterStateChangeListener)) {
                mAppCategoryFilterStateChangeListenerRefs.remove(ref);
                return;
            }
        }
    }

    public void registerAppCountryFilterStateChangeListener(AppsCountryFilterStateListener appsCountryFilterStateListener) {
        if (null != appsCountryFilterStateListener) {
            mAppsCountryFilterStateListenerRefs.add(new WeakReference<AppsCountryFilterStateListener>(appsCountryFilterStateListener));
        }
    }

    public void unregisterAppCountryFilterStateChangeListener(AppsCountryFilterStateListener appsCountryFilterStateListener) {
        if (mAppsCountryFilterStateListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mAppsCountryFilterStateListenerRefs.size(); i++) {
            WeakReference<AppsCountryFilterStateListener> ref = mAppsCountryFilterStateListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppsCountryFilterStateListener listener = ref.get();
            if (listener != null && listener.equals(appsCountryFilterStateListener)) {
                mAppsCountryFilterStateListenerRefs.remove(ref);
                return;
            }
        }

    }

    public void registerPbsSettingCountryChangeListener(PbsSettingCountryChangeListener listener) {
        mPbsSettingCountryChangeListenerRefs.add(new WeakReference<PbsSettingCountryChangeListener>(listener));
    }

    public void unregisterPbsSettingCountryChangeListener(PbsSettingCountryChangeListener pbsSettingCountryChangeListener) {
        if (mPbsSettingCountryChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mPbsSettingCountryChangeListenerRefs.size(); i++) {
            WeakReference<PbsSettingCountryChangeListener> ref = mPbsSettingCountryChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            PbsSettingCountryChangeListener listener = ref.get();
            if (listener != null && listener.equals(pbsSettingCountryChangeListener)) {
                mPbsSettingCountryChangeListenerRefs.remove(ref);
                return;
            }
        }
    }

    public void registerDashboardPmsStateChangeListener(DashboardPmsStateChangeListener dashboardPmsStateChangeListener) {
        if (null != dashboardPmsStateChangeListener) {
            mDashboardPmsStateListenerRefs.add(new WeakReference(dashboardPmsStateChangeListener));
        }
    }

    public void unregisterDashboardPmsStateChangeListener(DashboardPmsStateChangeListener dashboardPmsStateChangeListener) {
        if (mDashboardPmsStateListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mDashboardPmsStateListenerRefs.size(); i++) {
            WeakReference<DashboardPmsStateChangeListener> ref = mDashboardPmsStateListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardPmsStateChangeListener listener = ref.get();
            if (listener != null && listener.equals(dashboardPmsStateChangeListener)) {
                mDashboardPmsStateListenerRefs.remove(ref);
                return;
            }
        }
    }

    public void registerPbsSettingLanguageChangeListener(PbsSettingLanguageChangeListener listener) {
        mPbsSettingLanguageChangeListenerRefs.add(new WeakReference<>(listener));
    }

    public interface PbsSettingLanguageChangeListener {
        void onPbsSettingLanguageChange(int Language);
    }

    private void notifyPbsSettingLanguageChanged(int language) {
        if (mPbsSettingLanguageChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mPbsSettingLanguageChangeListenerRefs.size(); i++) {
            WeakReference<PbsSettingLanguageChangeListener> ref = mPbsSettingLanguageChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            PbsSettingLanguageChangeListener listener = ref.get();
            if (listener != null) {
                listener.onPbsSettingLanguageChange(language);
            }
        }
    }

    public void notifyOnCountryItemClick(int resourceId) {
            if(resourceId == org.droidtv.ui.strings.R.string.MAIN_NETHERLANDS) {
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS;
            }else if(resourceId == org.droidtv.ui.strings.R.string.MAIN_BELGIUM) {
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.BELGIUM;
            }else if(resourceId ==  org.droidtv.ui.strings.R.string.MAIN_LUXEMBOURG){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_FRANCE) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.FRANCE;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_GERMANY){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.GERMANY;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_SWITZERLAND){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_AUSTRIA){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_UK){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.UK;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_IRELAND){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.IRELAND;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_SPAIN){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.SPAIN;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_PORTUGAL) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_ITALY) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.ITALY;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_NORWAY) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.NORWAY;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_SWEDEN) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.SWEDEN;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_DENMARK){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.DENMARK;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_FINLAND){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.FINLAND;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_GREECE) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.GREECE;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_TURKEY){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.TURKEY;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_RUSSIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.RUSSIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_UKRAINE) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.UKRAINE;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_KAZAKHSTAN){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_POLAND) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.POLAND;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_CZECH_REP) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.CZECHREP;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_SLOVAKIA){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_HUNGARY) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.HUNGARY;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_BULGARIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.BULGARIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_ROMANIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.ROMANIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_LATVIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.LATVIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_ESTONIA){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.ESTONIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_LITHUANIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_SLOVENIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_SERBIA){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.SERBIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_CROATIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.CROATIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_ARGENTINA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_BRAZIL) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.BRAZIL;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_AUSTRALIA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_NEWZEALAND){
                mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND;
            }else if(resourceId ==   org.droidtv.ui.strings.R.string.MAIN_USA) {
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.USA;
            }else{
                    mSelectedCountryConstant = TvSettingsDefinitions.InstallationCountryConstants.OTHER;
            }
      
        DdbLogUtility.logCommon("DashboardDataManager", "notifyOnCountryItemClick() called with: resourceId = " + resourceId);
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_APPS_COUNTRY, 0, mSelectedCountryConstant);
    }


    public int getSelectedCountryNameResourceId() {
        int countryNameResourceId = -1;
        switch (mAppsCountry) {
            case TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_NETHERLANDS;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.BELGIUM:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_BELGIUM;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_LUXEMBOURG;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.FRANCE:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_FRANCE;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.GERMANY:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_GERMANY;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_SWITZERLAND;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_AUSTRIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.UK:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_UK;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.IRELAND:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_IRELAND;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SPAIN:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_SPAIN;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_PORTUGAL;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ITALY:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_ITALY;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.NORWAY:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_NORWAY;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SWEDEN:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_SWEDEN;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.DENMARK:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_DENMARK;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.FINLAND:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_FINLAND;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.GREECE:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_GREECE;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.TURKEY:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_TURKEY;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.RUSSIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_RUSSIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.UKRAINE:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_UKRAINE;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_KAZAKHSTAN;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.POLAND:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_POLAND;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.CZECHREP:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_CZECH_REP;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_SLOVAKIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.HUNGARY:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_HUNGARY;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.BULGARIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_BULGARIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ROMANIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_ROMANIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.LATVIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_LATVIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ESTONIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_ESTONIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_LITHUANIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_SLOVENIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SERBIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_SERBIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.CROATIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_CROATIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_ARGENTINA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.BRAZIL:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_BRAZIL;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_AUSTRALIA;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_NEWZEALAND;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.USA:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_USA;
                break;

            default:
                countryNameResourceId = org.droidtv.ui.strings.R.string.MAIN_INTERNATIONAL;
                break;
        }
        return countryNameResourceId;
    }

    public int getSelectedCountryFlagResourceId() {
        int countryFlagResourceId = -1;
        switch (mAppsCountry) {

            case TvSettingsDefinitions.InstallationCountryConstants.NETHERLANDS:
                countryFlagResourceId = R.drawable.netherlands;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.BELGIUM:
                countryFlagResourceId = R.drawable.belgium;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.LUXEMBOURG:
                countryFlagResourceId = R.drawable.luxembourg;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.FRANCE:
                countryFlagResourceId = R.drawable.france;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.GERMANY:
                countryFlagResourceId = R.drawable.germany;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SWITZERLAND:
                countryFlagResourceId = R.drawable.switzerland;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRIA:
                countryFlagResourceId = R.drawable.austria;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.UK:
                countryFlagResourceId = R.drawable.unitedkingdom;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.IRELAND:
                countryFlagResourceId = R.drawable.ireland;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SPAIN:
                countryFlagResourceId = R.drawable.spain;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.PORTUGAL:
                countryFlagResourceId = R.drawable.portugal;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ITALY:
                countryFlagResourceId = R.drawable.italy;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.NORWAY:
                countryFlagResourceId = R.drawable.norway;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SWEDEN:
                countryFlagResourceId = R.drawable.sweden;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.DENMARK:
                countryFlagResourceId = R.drawable.denmark;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.FINLAND:
                countryFlagResourceId = R.drawable.finland;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.GREECE:
                countryFlagResourceId = R.drawable.greece;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.TURKEY:
                countryFlagResourceId = R.drawable.turkey;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.RUSSIA:
                countryFlagResourceId = R.drawable.russia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.UKRAINE:
                countryFlagResourceId = R.drawable.ukraine;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.KAZAKHSTAN:
                countryFlagResourceId = R.drawable.kazakhstan;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.POLAND:
                countryFlagResourceId = R.drawable.poland;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.CZECHREP:
                countryFlagResourceId = R.drawable.czechrepublic;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SLOVAKIA:
                countryFlagResourceId = R.drawable.slovakia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.HUNGARY:
                countryFlagResourceId = R.drawable.hungary;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.BULGARIA:
                countryFlagResourceId = R.drawable.bulgaria;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ROMANIA:
                countryFlagResourceId = R.drawable.romania;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.LATVIA:
                countryFlagResourceId = R.drawable.latvia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ESTONIA:
                countryFlagResourceId = R.drawable.estonia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.LITHUANIA:
                countryFlagResourceId = R.drawable.lithuania;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SLOVENIA:
                countryFlagResourceId = R.drawable.slovenia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.SERBIA:
                countryFlagResourceId = R.drawable.serbia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.CROATIA:
                countryFlagResourceId = R.drawable.croatia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.ARGENTINA:
                countryFlagResourceId = R.drawable.argentina;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.BRAZIL:
                countryFlagResourceId = R.drawable.brazil;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.AUSTRALIA:
                countryFlagResourceId = R.drawable.australia;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.NEWZEALAND:
                countryFlagResourceId = R.drawable.newzealand;
                break;

            case TvSettingsDefinitions.InstallationCountryConstants.USA:
                countryFlagResourceId = R.drawable.us;
                break;

            default:
                countryFlagResourceId = R.drawable.international;
                break;
        }
        return countryFlagResourceId;
    }

    public interface DashboardPmsStateChangeListener {
        void onDashboardPmsStateChange();

        void onDashboardPmsStateChange(int value);
    }

    public interface PbsSettingCountryChangeListener {
        void onPbsSettingCountryChange(int country);
    }

    public boolean isAppCategoryFilterEnabled() {
        return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_CATEGORY_FILTER_ENABLE, 0, -1) == 1;
    }

    public boolean isAppCountryFilterEnabled() {
        return mIsAppCountryFilterEnabled;
    }

    private void notifyAppCategoryFilterStateChanged() {
        if (mAppCategoryFilterStateChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mAppCategoryFilterStateChangeListenerRefs.size(); i++) {
            WeakReference<AppCategoryFilterStateChangeListener> ref = mAppCategoryFilterStateChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppCategoryFilterStateChangeListener listener = ref.get();
            if (listener != null) {
                listener.onAppCategoryFilterStateChanged();
            }
        }
    }

    private void notifyAppCategoryFilterStateChanged(int value) {
        if (mAppCategoryFilterStateChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mAppCategoryFilterStateChangeListenerRefs.size(); i++) {
            WeakReference<AppCategoryFilterStateChangeListener> ref = mAppCategoryFilterStateChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppCategoryFilterStateChangeListener listener = ref.get();
            if (listener != null) {
                listener.onAppCategoryFilterStateChanged(value);
            }
        }
    }

    private void notifyDashboardPmsStateChanged() {
        if (mDashboardPmsStateListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mDashboardPmsStateListenerRefs.size(); i++) {
            WeakReference<DashboardPmsStateChangeListener> ref = mDashboardPmsStateListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardPmsStateChangeListener listener = ref.get();
            if (listener != null) {
                listener.onDashboardPmsStateChange();
            }
        }
    }

    private void notifyDashboardPmsStateChanged(int value) {
        if (mDashboardPmsStateListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mDashboardPmsStateListenerRefs.size(); i++) {
            WeakReference<DashboardPmsStateChangeListener> ref = mDashboardPmsStateListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardPmsStateChangeListener listener = ref.get();
            if (listener != null) {
                listener.onDashboardPmsStateChange(value);
            }
        }
    }

    private void notifyAppsCountryStateChanged() {
        if (mAppsCountryFilterStateListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mAppsCountryFilterStateListenerRefs.size(); i++) {
            WeakReference<AppsCountryFilterStateListener> ref = mAppsCountryFilterStateListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AppsCountryFilterStateListener listener = ref.get();
            if (listener != null) {
                listener.onAppsCountryFilterStateChanged();
            }
        }
    }

    private void notifyPbsSettingCountryChanged(int country) {
        if (mPbsSettingCountryChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mPbsSettingCountryChangeListenerRefs.size(); i++) {
            WeakReference<PbsSettingCountryChangeListener> ref = mPbsSettingCountryChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            PbsSettingCountryChangeListener listener = ref.get();
            if (listener != null) {
                listener.onPbsSettingCountryChange(country);
            }
        }

    }

    public int getSelectedLanguageResourceId() {
        int languageTvConstant = mTvSettingsManager.getInt(TvSettingsConstants.MENULANGUAGE, 0, -1);

        int languageResId = -1;
        switch (languageTvConstant) {
            case TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH:
                languageResId = R.drawable.topmenu_flag_unitedkingdom;
                break;
	    /*Android-P: Uncommnet after  ENGLISH_US  is available, Currenlty its changed to ENGLISH for DDB bringup*/
	    case TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH_US:
                languageResId = R.drawable.topmenu_flag_us;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.ALBANIAN:
                languageResId = R.drawable.topmenu_flag_albania;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.ARABIC:
                languageResId = R.drawable.topmenu_flag_arabic;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.BAHASA_MELAYU:
                languageResId = R.drawable.topmenu_flag_malaysia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.BULGARIAN:
                languageResId = R.drawable.topmenu_flag_bulgaria;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.SIMPLIFIEDCHINESE:
                languageResId = R.drawable.topmenu_flag_simplified_chinese;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.TRADITIONALCHINESE:
                languageResId = R.drawable.topmenu_flag_traditional_chinese;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.CROATIAN:
                languageResId = R.drawable.topmenu_flag_croatia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.CZECH:
                languageResId = R.drawable.topmenu_flag_czech_republic;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.DANISH:
                languageResId = R.drawable.topmenu_flag_denmark;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.DUTCH:
                languageResId = R.drawable.topmenu_flag_netherlands;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.ESTONIAN:
                languageResId = R.drawable.topmenu_flag_estonia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.FINNISH:
                languageResId = R.drawable.topmenu_flag_finland;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.FRENCH:
                languageResId = R.drawable.topmenu_flag_france;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.GERMAN:
                languageResId = R.drawable.topmenu_flag_germany;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.GREEK:
                languageResId = R.drawable.topmenu_flag_greece;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.HEBREW:
                languageResId = R.drawable.topmenu_flag_hebrew;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.HUNGARIAN:
                languageResId = R.drawable.topmenu_flag_hungary;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.INDONESIAN:
                languageResId = R.drawable.topmenu_flag_indonesia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.IRISH:
                languageResId = R.drawable.topmenu_flag_ireland;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.ITALIAN:
                languageResId = R.drawable.topmenu_flag_italy;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.KAZAKH:
                languageResId = R.drawable.topmenu_flag_kazakhstan;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.LATVIAN:
                languageResId = R.drawable.topmenu_flag_latvia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.LITHUANIAN:
                languageResId = R.drawable.topmenu_flag_lithuania;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.MACEDONIAN:
                languageResId = R.drawable.topmenu_flag_macedonia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.NORWEGIAN:
                languageResId = R.drawable.topmenu_flag_norway;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.POLISH:
                languageResId = R.drawable.topmenu_flag_poland;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.PORTUGUESE:
                languageResId = R.drawable.topmenu_flag_portugal;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.BRAZILIANPORTUGUESE:
                languageResId = R.drawable.topmenu_flag_brazil;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.ROMANIAN:
                languageResId = R.drawable.topmenu_flag_romania;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.RUSSIAN:
                languageResId = R.drawable.topmenu_flag_russia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.SERBIAN:
                languageResId = R.drawable.topmenu_flag_serbia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.SLOVAK:
                languageResId = R.drawable.topmenu_flag_slovakia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.SLOVENIAN:
                languageResId = R.drawable.topmenu_flag_slovenia;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.SPANISH:
                languageResId = R.drawable.topmenu_flag_spain;
                if(isNAFTA()){
                languageResId = R.drawable.topmenu_flag_mexico;
                }
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.LATINSPANISH:
                languageResId = R.drawable.topmenu_flag_us;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.SWEDISH:
                languageResId = R.drawable.topmenu_flag_sweden;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.THAI:
                languageResId = R.drawable.topmenu_flag_thailand;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.TURKISH:
                languageResId = R.drawable.topmenu_flag_turkey;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.UKRAINIAN:
                languageResId = R.drawable.topmenu_flag_ukraine;
                break;
            case TvSettingsDefinitions.InstallationLanguageConstants.VIETNAMESE:
                languageResId = R.drawable.topmenu_flag_vietnam;
                break;
            default:
                languageResId = R.drawable.topmenu_flag_us;
                break;

        }
        return languageResId;
    }

    public CountryAppListItem getCountryAppListItemForLanguage(int languageTvConstant) {
        CountryAppListItem languageItem;
        if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ALBANIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_ALBANIAN, R.drawable.albania);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ARABIC) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_ARABIC, R.drawable.arabic);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.BAHASA_MELAYU) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_BAHASA_MELAYU, R.drawable.malaysia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.BULGARIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_BULGARIAN, R.drawable.bulgaria);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.SIMPLIFIEDCHINESE) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_CHINESE, R.drawable.simplified_chinese);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.TRADITIONALCHINESE) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_TRADITIONAL_CHINESE, R.drawable.traditional_chinese);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.CROATIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_CROATIAN, R.drawable.croatia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.CZECH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_CZECH, R.drawable.czechrepublic);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.DANISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_DANISH, R.drawable.denmark);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.DUTCH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_DUTCH, R.drawable.netherlands);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_ENGLISH, R.drawable.unitedkingdom);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ESTONIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_ESTONIAN, R.drawable.estonia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.FINNISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_FINNISH, R.drawable.finland);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.FRENCH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_FRENCH, R.drawable.france);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.GERMAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_GERMAN, R.drawable.germany);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.GREEK) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_GREEK, R.drawable.greece);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.HEBREW) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_HEBREW, R.drawable.hebrew);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.HUNGARIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_HUNGARIAN, R.drawable.hungary);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.INDONESIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_INDONESIAN, R.drawable.indonesia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.IRISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_IRISH, R.drawable.ireland);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ITALIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_ITALIAN, R.drawable.italy);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.KAZAKH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_KAZAKH, R.drawable.kazakhstan);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.LATVIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_LATVIAN, R.drawable.latvia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.LITHUANIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_LITHUANIAN, R.drawable.lithuania);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.MACEDONIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_MACEDONIAN, R.drawable.macedonia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.NORWEGIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_NORWEGIAN, R.drawable.norway);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.POLISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_POLISH, R.drawable.poland);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.PORTUGUESE) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_PORTUGUESE, R.drawable.portugal);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.BRAZILIANPORTUGUESE) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_PORTUGUESE_BRAZILIAN, R.drawable.brazil);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ROMANIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_ROMANIAN, R.drawable.romania);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.RUSSIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_RUSSIAN, R.drawable.russia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.SERBIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_SERBIAN, R.drawable.serbia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.SLOVAK) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_SLOVAK, R.drawable.slovakia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.SLOVENIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_SLOVENIAN, R.drawable.slovenia);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.SPANISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_SPANISH, R.drawable.spain);
            if(isNAFTA()){
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_US_SPANISH, R.drawable.mexico);
            }
        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.SWEDISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_SWEDISH, R.drawable.sweden);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.THAI) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_THAI, R.drawable.thailand);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.TURKISH) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_TURKISH, R.drawable.turkey);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.UKRAINIAN) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_UKRAINIAN, R.drawable.ukraine);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.VIETNAMESE) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_VIETNAMESE, R.drawable.vietnam);

        } else if (languageTvConstant == TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH_US) {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_US_ENGLISH, R.drawable.us);

        } else {
            languageItem = new CountryAppListItem(org.droidtv.ui.strings.R.string.MISC_US_ENGLISH, R.drawable.us);

        }
        return languageItem;
    }

    public String getISOCodeForLanguage(int installationLanguageConstants) {
        String mISOCode = "eng";
        final SparseArray<String>  ISO_639_2_language2String = new SparseArray<String>();
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH, "eng");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.GERMAN, "deu");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.SWEDISH, "swe");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ITALIAN, "ita");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.FRENCH, "fra");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.SPANISH, "spa");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.CZECH, "cze");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.POLISH, "pol");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.TURKISH, "tur");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.RUSSIAN, "rus");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.GREEK, "gre");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.BASQUE, "baq");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.CATALAN, "cat");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.CROATIAN, "hrv");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.DANISH, "dan");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.DUTCH, "dut");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.FINNISH, "fin");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.GAELIC, "gla");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.GALLIGAN, "glg");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.NORWEGIAN, "nor");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.PORTUGUESE, "por");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.SERBIAN, "srp");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.SLOVAK, "slk");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.SLOVENIAN, "slv");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.WELSH, "wel");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ROMANIAN, "ron");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ESTONIAN, "est");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.UKRAINIAN, "ukr");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ARABIC, "ara");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.HEBREW, "heb");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.HUNGARIAN, "hun");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.HUNGAL, "hau");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.MALAY, "msa");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.PERSIAN, "per");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.SIMPLIFIEDCHINESE, "chi");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.TRADITIONALCHINESE, "chi");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.BRAZILIANPORTUGUESE, "por");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.BULGARIAN, "bul");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.LATINSPANISH, "lat");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.LITHUANIAN, "lit");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.LATVIAN, "lav");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.KAZAKH, "kaz");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.THAI, "tha");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.IRISH, "gle");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.BOSNIAN, "bos");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.MACEDONIAN, "mac");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ALBANIAN, "alb");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.BAHASA_MELAYU, "msa");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.INDONESIAN, "ind");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.VIETNAMESE, "vie");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.TAMIL, "tam");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.UNDEFINED, "und");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ICELANDIC, "isl");
        ISO_639_2_language2String.put(TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH_US, "en-US");
        mISOCode = ISO_639_2_language2String.get(installationLanguageConstants ,"eng");
        return mISOCode;
    }



    public void notifyOnLanguageItemClick(int resourceId) {

         if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_ALBANIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ALBANIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_ARABIC){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ARABIC;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_BAHASA_MELAYU){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.BAHASA_MELAYU;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_BULGARIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.BULGARIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_CHINESE){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.SIMPLIFIEDCHINESE;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_TRADITIONAL_CHINESE){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.TRADITIONALCHINESE;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_CROATIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.CROATIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_CZECH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.CZECH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_DANISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.DANISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_DUTCH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.DUTCH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_ENGLISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_US_ENGLISH){
/*Android-P: Change to ENGLISH_US once constant is available, Currenlty its changed to ENGLISH for DDB bringup*/
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ENGLISH_US;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_ESTONIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ESTONIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_FINNISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.FINNISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_FRENCH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.FRENCH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_GERMAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.GERMAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_GREEK){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.GREEK;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_FRENCH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.FRENCH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_HEBREW){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.HEBREW;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_HUNGARIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.HUNGARIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_INDONESIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.INDONESIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_IRISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.IRISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_ITALIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ITALIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_KAZAKH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.KAZAKH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_LATVIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.LATVIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_LITHUANIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.LITHUANIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_MACEDONIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.MACEDONIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_NORWEGIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.NORWEGIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_POLISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.POLISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_PORTUGUESE){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.PORTUGUESE;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_PORTUGUESE_BRAZILIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.BRAZILIANPORTUGUESE;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_ROMANIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.ROMANIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_RUSSIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.RUSSIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_SERBIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.SERBIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_SLOVAK){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.SLOVAK;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_SLOVENIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.SLOVENIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_SPANISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.SPANISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_US_SPANISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.LATINSPANISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_SWEDISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.SWEDISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_THAI){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.THAI;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_TURKISH){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.TURKISH;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_UKRAINIAN){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.UKRAINIAN;
	 }else if(resourceId ==  org.droidtv.ui.strings.R.string.MISC_VIETNAMESE){
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.VIETNAMESE;
	 }else{
                mSelectedLanguageConstant = TvSettingsDefinitions.InstallationLanguageConstants.UNDEFINED;
	 }
        DdbLogUtility.logCommon("DashboardDataManager", "notifyOnLanguageItemClick mSelectedLanguageConstant " + mSelectedLanguageConstant);
        mTvSettingsManager.putInt(TvSettingsConstants.MENULANGUAGE, 0, mSelectedLanguageConstant);
    }

    public boolean areChannelsEnabled() {
        return mChannelDataManager.areChannelsEnabled();
    }

    public boolean areRFIPChannelsEnabled() {
        return mChannelDataManager.areRFIPChannelsEnabled();
    }

    public boolean areChannelLogosEnabled() {
        return mChannelDataManager.areChannelLogosEnabled();
    }

    public boolean areRadioChannelsEnabled() {
        return mChannelDataManager.areRadioChannelsEnabled();
    }

    public boolean areTvChannelsEnabled() {
        return mChannelDataManager.areTvChannelsEnabled();
    }

    public boolean areMediaChannelsEnabled() {
        return mChannelDataManager.areMediaChannelsEnabled();
    }

    public boolean areTifChannelsEnabled() {
        return mChannelDataManager.areTifChannelsEnabled();
    }

    public boolean areTifChannelsDisplayEnabled() {
        return mChannelDataManager.areTifChannelsDisplayEnabled();
    }

    public boolean areTifChannelsInChannelsListEnabled() {
        return mChannelDataManager.areTifChannelsInChannelsListEnabled();
    }

    public boolean isThemeTvEnabled() {
        return mChannelDataManager.isThemeTvEnabled();
    }

    public boolean isEpgEnabled() {
        return mChannelDataManager.isEpgEnabled();
    }

    public boolean isEpgSourceBcepg() {
        return mChannelDataManager.isEpgSourceBcepg();
    }

    public boolean isEpgSourceApp() {
        return mChannelDataManager.isEpgSourceApp();
    }

    public interface ChannelDataListener {
        void onAvailableChannelFiltersFetched(List<ChannelFilter> filters);

        void onActiveChannelFilterFetched(ChannelFilter activeFilter);

        void onChannelFilterUpdated(ChannelFilter filter);

        void onChannelLogoEnabledStateChanged(boolean logosEnabled);

        void onLastSelectedChannelUriChanged(String updatedLastSelectedChannelUri);
    }

    public interface ChannelSettingsListener {
        void onChannelsSettingChanged(boolean showChannels);
    }

    public interface GoogleAccountImageListener {
        void onGoogleAccountImageFetched(Bitmap image);
    }

    public interface AccountIconListener {
        void showAccountIcon(boolean show);
    }

    public interface AssistantIconListener{
        void showAssistantIcon(boolean show);
    }
    public interface AppCategoryFilterStateChangeListener {
        void onAppCategoryFilterStateChanged();

        void onAppCategoryFilterStateChanged(int value);
    }

    public interface GuestNameChangeListener {
        void onGuestNameChanged();
    }

    public interface AppsCountryFilterStateListener {
        void onAppsCountryFilterStateChanged();
    }

    public interface SourceDataListener {
        void onSourcesFetched(List<Source> sources);

        void onSourceUpdated(int index, Source source);

        void onLastSelectedDeviceChanged(int device);

        void onSourceDisplayChanged();
    }

    public interface ProgramDataListener {
        void onProgramDataFetchComplete(int channelId, Program program);
    }

    public interface AccountDataListener {
        void onAccountsFetched(List<Account> accounts);
    }

    public interface ThumbnailDataListener {
        void onAvailableImagesFetched(Cursor cursor);
    }

    public interface ThumbnailBitmapFetchListener {
        void onThumbnailBitmapFetchComplete(long id, Bitmap bitmap);
    }

    public interface ImageFetchListener {
        void onImageFetchComplete(Bitmap bitmap);
    }

    public interface ImageFileFetchListener {
        void onImageFileFetched(ImageFile imageFile);
    }

    public interface FileDataListener {
        void onFileCopyComplete(boolean success);

        void onFileDeleteComplete(boolean success);
    }

    public interface BackgroundImageChangeObserver {
        void changeBackgroundImage(Bitmap bitmap);

        void changeCastBackgroundImage(Bitmap bitmap);

        void changeBackgroundImage(int drawableResourceId);
        void changeBackgroundColorFilter(int color);

        void clearBackground();
    }

    public interface HotelLogoChangeObserver {
        void changeHotelLogo(Bitmap bitmap);

        void changeHotelLogo(int drawableResourceId);
    }

    public interface SidePanelTextColorChangeListener {
        void changeSidePanelHighlightedTextColor(int color);

        void changeSidePanelNonHighlightedTextColor(int color);
    }

    public interface TopMenuItemFocusChangeListner {
        void currentTopMenuFocusedItem(int itemID);
    }

    public void addSidePanelTextColorChangeListener(SidePanelTextColorChangeListener sidePanelTextColorChangeListener) {
        mSidePanelTextColorChangeListenerRefs.add(new WeakReference<SidePanelTextColorChangeListener>(sidePanelTextColorChangeListener));
    }

    public void removeSidePanelTextColorChangeListener(SidePanelTextColorChangeListener sidePanelTextColorChangeListener) {
        if (mSidePanelTextColorChangeListenerRefs == null) {
            return;
        }

        for (int i = 0; i < mSidePanelTextColorChangeListenerRefs.size(); i++) {
            WeakReference<SidePanelTextColorChangeListener> listenerRef = mSidePanelTextColorChangeListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }
            SidePanelTextColorChangeListener listener = listenerRef.get();
            if (listener != null && listener.equals(sidePanelTextColorChangeListener)) {
                mSidePanelTextColorChangeListenerRefs.remove(listenerRef);
                return;
            }
        }
    }

    public interface SidePanelListener {
        void changeSidePanelBackgroundColor(int color);

        void showSidePanel();

        void hideSidePanel();
    }

    public void changeSidePanelHighlightedTextColor(int color) {
        for (int i = 0; mSidePanelTextColorChangeListenerRefs != null && i < mSidePanelTextColorChangeListenerRefs.size(); i++) {
            WeakReference<SidePanelTextColorChangeListener> listenerRef = mSidePanelTextColorChangeListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SidePanelTextColorChangeListener listener = listenerRef.get();
            if (listener != null) {
                listener.changeSidePanelHighlightedTextColor(color);
            }
        }
    }

    public void changeSidePanelNonHighlightedTextColor(int color) {
        for (int i = 0; mSidePanelTextColorChangeListenerRefs != null && i < mSidePanelTextColorChangeListenerRefs.size(); i++) {
            WeakReference<SidePanelTextColorChangeListener> listenerRef = mSidePanelTextColorChangeListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SidePanelTextColorChangeListener listener = listenerRef.get();
            if (listener != null) {
                listener.changeSidePanelNonHighlightedTextColor(color);
            }
        }
    }

    public void showSidePanel() {
        for (int i = 0; mSidePanelListenerRefs != null && i < mSidePanelListenerRefs.size(); i++) {
            WeakReference<SidePanelListener> listenerRef = mSidePanelListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SidePanelListener listener = listenerRef.get();
            if (listener != null) {
                listener.showSidePanel();
            }
        }
    }

    public void hideSidePanel() {
        for (int i = 0; mSidePanelListenerRefs != null && i < mSidePanelListenerRefs.size(); i++) {
            WeakReference<SidePanelListener> listenerRef = mSidePanelListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SidePanelListener listener = listenerRef.get();
            if (listener != null) {
                listener.hideSidePanel();
            }
        }
    }

    public void changeSidePanelBackgroundColor(int color) {
        for (int i = 0; mSidePanelListenerRefs != null && i < mSidePanelListenerRefs.size(); i++) {
            WeakReference<SidePanelListener> listenerRef = mSidePanelListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SidePanelListener listener = listenerRef.get();
            if (listener != null) {
                listener.changeSidePanelBackgroundColor(color);
            }
        }
    }

    public boolean addSidePanelListener(SidePanelListener sidePanelListener) {
        if (sidePanelListener == null) {
            return false;
        }
        return mSidePanelListenerRefs.add(new WeakReference<SidePanelListener>(sidePanelListener));
    }

    public boolean removeSidePanelListener(SidePanelListener sidePanelListener) {
        if (mSidePanelListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mSidePanelListenerRefs.size(); i++) {
            WeakReference<SidePanelListener> ref = mSidePanelListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            SidePanelListener listener = ref.get();
            if (listener != null && listener.equals(sidePanelListener)) {
                return mSidePanelListenerRefs.remove(ref);
            }
        }
        return false;
    }

    public int getSidePanelBackgroundColor() {
        DdbLogUtility.logCommon("DashboardDataManager","getSidePanelBackgroundColor: bfl: "+isBFLProduct());
        if(isBFLProduct()){
            return mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, getContext().getColor(R.color.side_panel_background_default_bfl));
        }
        return mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, getContext().getColor(R.color.side_panel_background_default));
    }

    public void saveSidePanelBackgroundColor(int color) {
        mDefaultSharedPreferences.edit().putInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, color).apply();
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_DDB_SIDE_PANEL_BG_COLOR, 0, color);
    }

    public int getSidePanelHighlightedTextColor() {
        return mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR, getContext().getColor(R.color.side_panel_highlighted_text_color_default));
    }

    public void saveSidePanelHighlightedTextColor(int color) {
        mDefaultSharedPreferences.edit().putInt(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR, color).apply();
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_DDB_HIGHLIGHTED_TEXT_COLOR, 0, color);
    }

    public int getSidePanelNonHighlightedTextColor() {
        return mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR, getContext().getColor(R.color.side_panel_non_highlighted_text_color_default));
    }

    public void saveSidePanelNonHighlightedTextColor(int color) {
        DdbLogUtility.logCommon("DashboardDataManager", "saveSidePanelNonHighlightedTextColor() called with: color = [" + color + "]");
        mDefaultSharedPreferences.edit().putInt(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR, color).apply();
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_DDB_NON_HIGHLIGHTED_TEXT_COLOR, 0, color);
    }

    public interface DashboardConfigurationResetListener {
        void revertSessionChanges();

        void resetSettings();
    }

    public boolean hasSavedConfiguration() {
        return hasSavedConfigurationPreferences() || hasSavedImages();
    }

    public void revertSessionChanges() {
        DdbLogUtility.logCommon("DashboardDataManager", "revertSessionChanges() called");
        revertSidePanelChanges();
        revertMainBackgroundChanges();
        revertShaingBackgroundChanges();
        notifyAllRevertSessionListeners();
        int sidePanelBackgroundColor;
        if(isBFLProduct()){
            sidePanelBackgroundColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, getContext().getColor(R.color.side_panel_background_default_bfl));
        }else{
            sidePanelBackgroundColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR, getContext().getColor(R.color.side_panel_background_default));
        }
        int sidePanelHighlightedTextColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR, getContext().getColor(R.color.side_panel_highlighted_text_color_default));
        int sidePanelNonHighlightedTextColor = mDefaultSharedPreferences.getInt(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR, getContext().getColor(R.color.side_panel_non_highlighted_text_color_default));
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_DDB_SIDE_PANEL_BG_COLOR, 0, sidePanelBackgroundColor);
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_DDB_HIGHLIGHTED_TEXT_COLOR, 0, sidePanelHighlightedTextColor);
        mTvSettingsManager.putInt(TvSettingsConstants.PBSMGR_PROPERTY_DDB_NON_HIGHLIGHTED_TEXT_COLOR, 0, sidePanelNonHighlightedTextColor);
    }

    public void resetGoogleAccountPreferences() {
        SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
        editor.remove(Constants.PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION);
        editor.commit();
    }

    public void resetSettings() {
        SharedPreferences.Editor editor = getDefaultSharedPreferences().edit();
        editor.remove(Constants.PREF_KEY_SIDEPANEL_BACKGROUND_COLOR).
                remove(Constants.PREF_KEY_SIDEPANEL_HIGHLIGHTED_TEXT_COLOR).
                remove(Constants.PREF_KEY_SIDEPANEL_NON_HIGHLIGHTED_TEXT_COLOR).
                remove(Constants.PREF_KEY_MAIN_BACKGROUND_COLOR_FILTER).
                remove(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED);
        editor.apply();

        removeAllSavedImages(new FileDataListener() {
            @Override
            public void onFileCopyComplete(boolean success) {

            }

            @Override
            public void onFileDeleteComplete(boolean success) {
                revertSessionChanges();

                notifyAllResetSettingsListeners();
            }
        });
    }

    public void addConfigurationResetListener(DashboardConfigurationResetListener dashboardConfigurationResetListener) {
        mDashboardConfigurationResetListenerRefs.add(new WeakReference<DashboardConfigurationResetListener>(dashboardConfigurationResetListener));
    }

    public void removeConfigurationResetListener(DashboardConfigurationResetListener dashboardConfigurationResetListener) {
        if (mDashboardConfigurationResetListenerRefs == null) {
            return;
        }

        for (int i = 0; i < mDashboardConfigurationResetListenerRefs.size(); i++) {
            WeakReference<DashboardConfigurationResetListener> listenerRef = mDashboardConfigurationResetListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }
            DashboardConfigurationResetListener listener = listenerRef.get();
            if (listener != null && listener.equals(dashboardConfigurationResetListener)) {
                mDashboardConfigurationResetListenerRefs.remove(listenerRef);
                return;
            }
        }
    }

    public void addAccountChangeListener(AccountChangeListener accountChangeListener) {
        mAccountDataManager.addAccountChangeListener(accountChangeListener);
    }

    public void setAccountIconListener(AccountIconListener accountIconListener) {
        mAccountIconListenerRefs.add(new WeakReference<AccountIconListener>(accountIconListener));
    }

    public void removeAccountIconListner(AccountIconListener accountIconListener){
        mAccountIconListenerRefs.remove(accountIconListener);
    }

    public void  setAssistantIconListener(AssistantIconListener assistantIconListener) {
        mAssistantIconListenerRef = new WeakReference<AssistantIconListener>(assistantIconListener);
    }

    public void removeAssistantIconListener(AssistantIconListener assistantIconListener){
        mAssistantIconListenerRef = null;
    }

    public void removeAccountChangeListener(AccountChangeListener accountChangeListener) {
        mAccountDataManager.removeAccountChangeListener(accountChangeListener);
    }

    public void notifyAccountChanged() {
        mAccountDataManager.notifyAccountChanged();
    }

    public void notifyGuestCheckInStatusChanged(String action) {
        if(action.equalsIgnoreCase(Constants.INTENT_ACTION_GUEST_CHECK_IN_STATUS_CHANGE)){
            mGuestCheckInState = PMS_CHECK_IN;
        }else{
            mGuestCheckInState = PMS_CHECK_OUT;
        }
        mMessagesDataManager.notifyGuestCheckInStatusChanged();
        if(mAppsDataManager != null){
            mAppsDataManager.refreshEnabledAppList();
        }
    }

    public interface MessageCountChangeListener {
        void onMessageCountChanged();
    }

    public void addMessageCountChangeListener(MessageCountChangeListener messageCountChangeListener) {
        mMessagesDataManager.addMessageCountChangeListener(messageCountChangeListener);
    }

    public void removeMessageCountChangeListener(MessageCountChangeListener messageCountChangeListener) {
        mMessagesDataManager.removeMessageCountChangeListener(messageCountChangeListener);
    }

    public void addGuestCheckInStatusChangeListener(GuestCheckInStatusChangeListener guestCheckInStatusChangeListener) {
        mMessagesDataManager.addGuestCheckInStatusChangeListener(guestCheckInStatusChangeListener);
    }

    public void removeGuestCheckInStatusChangeListener(GuestCheckInStatusChangeListener guestCheckInStatusChangeListener) {
        mMessagesDataManager.removeGuestCheckInStatusChangeListener(guestCheckInStatusChangeListener);
    }

    public void notifyMessageCountChanged() {
        mMessagesDataManager.notifyMessageCountChanged();
    }

    public int getMessageCount() {
        return mMessagesDataManager.getMessageCount();
    }

    public void showAccountIcon(boolean show) {
        DdbLogUtility.logCommon("DashboardDataManager", "showAccountIcon() show = " + show);
        AccountIconListener listener = null;
        if (mAccountIconListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mAccountIconListenerRefs.size(); i++) {
            WeakReference<AccountIconListener> ref = mAccountIconListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            AccountIconListener accountIconListener = ref.get();
            if (accountIconListener != null) {
                accountIconListener.showAccountIcon(show);
            }
        }
    }

    public void showAssistantIcon(boolean show) {
        AssistantIconListener listener = null;
        if (mAssistantIconListenerRef == null) {
            return;
        }
        listener = mAssistantIconListenerRef.get();
        if (listener != null) {
            listener.showAssistantIcon(show);
        }
    }

    public boolean addRecommendationListener(RecommendationListener recommendationListener) {
        return mRecommendationDataManager.addRecommendationListener(recommendationListener);
    }

    public boolean removeRecommendationListener(RecommendationListener recommendationListener) {
        return mRecommendationDataManager.removeRecommendationListener(recommendationListener);
    }

    public boolean addPreviewProgramsListener(PreviewProgramsListener previewProgramsListener) {
        return mPreviewProgramsManager.addPreviewProgramsChannelsListener(previewProgramsListener);
    }
    public boolean removePreviewProgramsListener(PreviewProgramsListener previewProgramsListener) {
        return mPreviewProgramsManager.removePreviewListener(previewProgramsListener);
    }
    public boolean addVideoOnDemandRecommendationListener(VideoOnDemandRecommendationListener listener) {
        return mVideoOnDemandDataManager.addVideoOnDemandRecommendationListener(listener);
    }

    public boolean removeVideoOnDemandRecommendationListener(VideoOnDemandRecommendationListener listener) {
        return mVideoOnDemandDataManager.removeVideoOnDemandRecommendationListener(listener);
    }

    public boolean addGamesRecommendationListener(GamesRecommendationListener listener) {
        return mGamesDataManager.addGamesRecommendationListener(listener);
    }

    public boolean removeGamesRecommendationListener(GamesRecommendationListener listener) {
        return mGamesDataManager.removeGamesRecommendationListener(listener);
    }

    public boolean addAppRecommendationsListener(AppRecommendationsListener listener) {
        return mAppsDataManager.addAppRecommendationsListener(listener);
    }

    public boolean removeAppRecommendationsListener(AppRecommendationsListener listener) {
        return mAppsDataManager.removeAppRecommendationsListener(listener);
    }

    public List<Recommendation> getRecommendations(int category) {
        return mRecommendationDataManager.getRecommendations(category);
    }

    public List<Recommendation> getRecommendations(int category, String packageName) {
        return mRecommendationDataManager.getRecommendations(category, packageName);
    }

    public List<Recommendation> getRecommendations(String packageName) {
        return mRecommendationDataManager.getRecommendations(packageName);
    }

    public PreviewProgramsChannel getSmartInfoPreviewChannel(){
        return mSmartInfoDataManager.getSmartInfoPreviewChannel();
    }

    public List<Recommendation> getAllRecommendations() {
        DdbLogUtility.logRecommendationChapter("DashboardDataManager", "getAllRecommendations() called");
        List<Recommendation> allRecommendations = mRecommendationDataManager.getAllRecommendations();
        List<Recommendation> previewProgramRecommendations = mPreviewProgramsManager.getPreviewChannleRecommendations();

        allRecommendations.addAll(previewProgramRecommendations);
        return allRecommendations;
    }

    public void cancelRecommendation(Recommendation recommendation) {
        mRecommendationDataManager.cancelRecommendation(recommendation);
    }
	
	public void registerSmartInfoPreviewProgramListener(SmartInfoPreviewProgramListener listener){
		mPreviewProgramsManager.addSmartInfoListner(listener);
	}
	
	public void unRegisterSmartInfoPreviewProgramListener(SmartInfoPreviewProgramListener listener){
		mPreviewProgramsManager.unRegisterSmartInfoPreviewProgramListener(listener);
	}
	
    public boolean addSmartInfoListener(SmartInfoListener smartInfoListener) {        
        return mSmartInfoDataManager.addSmartInfoListener(smartInfoListener);
    }

	public boolean removeSmartInfoListener(SmartInfoListener smartInfoListener) {        
        return mSmartInfoDataManager.removeSmartInfoListener(smartInfoListener);
    }

    public boolean areVodRecommendationsAvailable() {
        return mVideoOnDemandDataManager.areVodRecommendationsAvailable();
    }

    public boolean areVodPreviewRecommendationsAvailable() {
        return mVideoOnDemandDataManager.areVodPreviewRecommendationsAvailable();
    }

    public boolean areGameRecommendationsAvailable() {
        return mGamesDataManager.areGameRecommendationsAvailable();
    }

    public boolean arePreviewChannelGamesAvailable(){
        return mGamesDataManager.arePreviewChannelGamesAvailable();
    }

    public boolean areAppRecommendationsAvailable() {
        return mAppsDataManager.areAppRecommendationsAvailable();
    }

    public boolean areRecommendedAppsAvailable() {
        return mAppsDataManager.areRecommendedAppsAvailable();
    }

    public boolean isAppRecommendationEnabled(String packageName) {
        return mAppsDataManager.isAppRecommendationEnabled(packageName);
    }

    public boolean areGameAppsAvailable() {
        return mGamesDataManager.areGameAppsAvailable();
    }

    public void fetchSmartInfo() {
        if(mSmartInfoDataManager != null) {
            mSmartInfoDataManager.fetchSmartInfo();
        }
    }

    public void onClearDataTriggered(){
        if(mSmartInfoDataManager != null){
            mSmartInfoDataManager.onClearDataTriggered();
        }
    }

    public void clearSmartInfoData() {
        mSmartInfoDataManager.clearSmartInfoData();
    }
    public void clearSmartInfoImageCache() {
        mSmartInfoDataManager.clearSmartInfoImageCache();
    }

    public List<SmartInfo> getSmartInfoData() {
        return mSmartInfoDataManager.getSmartInfoData();
    }

    public List<Recommendation> getSmartInfoRecommendations() {
        return mSmartInfoDataManager.getSmartInfoRecommendations();
    }

    public String getSelectedSmartInfoPackage(){
        return mSmartInfoDataManager.getSelectedSmartInfoPackage();
    }

    public String getSmartInfoTitle() {
        return mSmartInfoDataManager.getSmartInfoTitle();
    }

    public Drawable getSmartInfoIcon() {
        return mSmartInfoDataManager.getSmartInfoIcon();
    }

    public boolean isSmartInfoEnabled() {
        return mSmartInfoDataManager.isSmartInfoEnabled();
    }

    public boolean isSmartInfoAvailable() {
        return mSmartInfoDataManager.isSmartInfoAvailable();
    }

    public boolean isSmartInfoModeBrowser() {
        return mSmartInfoDataManager.isSmartInfoModeBrowser();
    }

    public boolean isSmartInfoBrowserSourceLocal() {
        return mSmartInfoDataManager.isSmartInfoBrowserSourceLocal();
    }

    public boolean isSmartInfoBrowserSourceUSB() {
        return mSmartInfoDataManager.isSmartInfoBrowserSourceUSB();
    }

    public boolean isSmartInfoBrowserSourceServer() {
        return mSmartInfoDataManager.isSmartInfoBrowserSourceServer();
    }

    public boolean isSmartInfoModeApp() {
        return mSmartInfoDataManager.isSmartInfoModeApp();
    }
	
	public boolean isSmartInfoAppPreviewProgramBased(){
		return mSmartInfoDataManager.isSmartInfoAppPreviewProgramBased();
	}
	
    public void fetchSmartInfoImageFromRelativePath(String relativePathToImage, long id, int thumbnailWidth, int thumbnailHeight, ThumbnailBitmapFetchListener listener) {
        mSmartInfoDataManager.fetchSmartInfoImageFromRelativePath(relativePathToImage, id, thumbnailWidth, thumbnailHeight, listener);
    }

    public int getInstallationCountry() {
        return mTvSettingsManager.getInt(TvSettingsConstants.INSTALLATIONCOUNTRY, 0, 0);
    }

    public void applyCloneDataPrefs() {
        System.exit(0);
    }

    public void notifyRecommendationListenerServiceAvailable(RecommendationListenerService.RecommendationServiceAdapter recommendationServiceAdapter) {
        if (mRecommendationServiceAdapterRef != null) {
            mRecommendationServiceAdapterRef.clear();
        }
        mRecommendationServiceAdapterRef = new WeakReference<>(recommendationServiceAdapter);

        for (int i = 0; mRecommendationListenerServiceClientRefs != null && i < mRecommendationListenerServiceClientRefs.size(); i++) {
            WeakReference<RecommendationListenerServiceClient> ref = mRecommendationListenerServiceClientRefs.get(i);
            if (ref == null) {
                continue;
            }

            RecommendationListenerServiceClient client = ref.get();
            if (client != null) {
                client.onRecommendationListenerServiceAvailable(mRecommendationServiceAdapterRef);
            }
        }
    }

    public void notifyRecommendationListenerServiceUnavailable() {
        if (mRecommendationServiceAdapterRef != null) {
            mRecommendationServiceAdapterRef.clear();
            mRecommendationServiceAdapterRef = null;
        }

        for (int i = 0; mRecommendationListenerServiceClientRefs != null && i < mRecommendationListenerServiceClientRefs.size(); i++) {
            WeakReference<RecommendationListenerServiceClient> ref = mRecommendationListenerServiceClientRefs.get(i);
            if (ref == null) {
                continue;
            }

            RecommendationListenerServiceClient client = ref.get();
            if (client != null) {
                client.onRecommendationListenerServiceUnavailable();
            }
        }
    }

    public void addRecommendationListenerServiceClient(RecommendationListenerServiceClient recommendationListenerServiceClient) {
        if (recommendationListenerServiceClient == null) {
            return;
        }
        mRecommendationListenerServiceClientRefs.add(new WeakReference<RecommendationListenerServiceClient>(recommendationListenerServiceClient));

        if (mRecommendationServiceAdapterRef != null) {
            recommendationListenerServiceClient.onRecommendationListenerServiceAvailable(mRecommendationServiceAdapterRef);
        }
    }

    public void removeRecommendationListenerServiceClient(RecommendationListenerServiceClient recommendationListenerServiceClient) {
        for (int i = 0; mRecommendationListenerServiceClientRefs != null && i < mRecommendationListenerServiceClientRefs.size(); i++) {
            WeakReference<RecommendationListenerServiceClient> ref = mRecommendationListenerServiceClientRefs.get(i);
            if (ref == null) {
                continue;
            }

            RecommendationListenerServiceClient client = ref.get();
            if (client != null && client.equals(recommendationListenerServiceClient)) {
                mRecommendationListenerServiceClientRefs.remove(i);
                return;
            }
        }
    }

    public boolean addAppDataListener(AppDataListener listener) {
        return mAppsDataManager.addAppDataListener(listener);
    }

    public boolean removeAppDataListener(AppDataListener listener) {
        return mAppsDataManager.removeAppDataListener(listener);
    }

    public boolean registerGoogleCastListener(GoogleCastListener listener) {
        return mGoogleCastDataManager.registerGoogleCastListener(listener);
    }

    public boolean unregisterGoogleCastListener(GoogleCastListener listener) {
        return mGoogleCastDataManager.unregisterGoogleCastListener(listener);
    }

    public void initHotSpot() {
        mGoogleCastDataManager.initHotspot();
    }

    public void deinitHotSpot() {
        mGoogleCastDataManager.deinitHotspot();
    }

    public void startHotSpot() {
        mGoogleCastDataManager.startHotspot();
    }

    public void stopHotSpot() {
        mGoogleCastDataManager.stopHotspot();
    }

    public boolean isSecureSharingEnabled() {
        return mGoogleCastDataManager.isSecureSharingEnabled();
    }

    public boolean isGoogleCastWifiLoginEnabled() {
        return mGoogleCastDataManager.isGoogleCastWifiLoginEnabled();
    }

    public String getWifiNetworkNameForGoogleCast() {
        return mGoogleCastDataManager.getWifiNetworkNameForGoogleCast();
    }

    public String getGatewayUrlForGoogleCast() {
        return mGoogleCastDataManager.getGatewayUrlForGoogleCast();
    }

    public boolean isHotspotCompatibilityMode() {
        return mGoogleCastDataManager.isHotspotCompatibilityMode();
    }

    public void setTcStatus(String macAddr, int termsStatus) {
        DdbLogUtility.logCastChapter("BHARAT","setting TNC for MAC:"+macAddr +" Status: "+termsStatus);
        mGoogleCastDataManager.setTcStatus(macAddr ,termsStatus);
    }

    public boolean getTcStatus(String macAddress) {
        return mGoogleCastDataManager.getTcStatus(macAddress);
    }

    public TNCDetails getTNCDetails(){
        return mGoogleCastDataManager.checkAndFetchTncDetails();
    }

    public boolean isTermAndConditionEnable() {
        return mGoogleCastDataManager.isTNCEnabled();
    }

    public void setHotspotCompatibityMode(boolean compatibityMode) {
        mGoogleCastDataManager.setHotspotCompatibityMode(compatibityMode);
    }

    public boolean isHotspotDefaultFrequency5Ghz() {
        return mGoogleCastDataManager.isHotspotDefaultFrequency5Ghz();
    }

    public boolean canInitHotspot() {
        return mGoogleCastDataManager.canInitHotspot();
    }

    public boolean canStartHotspot() {
        return mGoogleCastDataManager.canStartHotspot();
    }

    public List<String> getClientDeviceNameList() {
        return mGoogleCastDataManager.getClientDeviceNameList();
    }

    private void revertSidePanelChanges() {
        changeSidePanelBackgroundColor(getSidePanelBackgroundColor());
        changeSidePanelNonHighlightedTextColor(getSidePanelNonHighlightedTextColor());
        changeSidePanelHighlightedTextColor(getSidePanelHighlightedTextColor());
    }

    private void revertMainBackgroundChanges() {
        setBackgroundColorFilter(getMainBackgroundColorFilter());
        applyMainBackground();
        applyHotelLogo();
    }
    private void revertShaingBackgroundChanges() {
        applySharingBackground();
    }
    private void removeAllSavedImages(FileDataListener listener) {
        mFileDataManager.deleteFile(Constants.PATH_CAST_APP_SHARING_BACKGROUND, new FileDataListener() {
            @Override
            public void onFileCopyComplete(boolean success) { }
            @Override
            public void onFileDeleteComplete(boolean success) { }
        });
        mFileDataManager.deleteFile(getContext().getFilesDir() + Constants.PATH_IMAGES, listener);

    }

    private void notifyAllRevertSessionListeners() {
        for (int i = 0; mDashboardConfigurationResetListenerRefs != null && i < mDashboardConfigurationResetListenerRefs.size(); i++) {
            WeakReference<DashboardConfigurationResetListener> listenerRef = mDashboardConfigurationResetListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            DashboardConfigurationResetListener listener = listenerRef.get();
            if (listener != null) {
                listener.revertSessionChanges();
            }
        }
    }

    private void notifyAllResetSettingsListeners() {
        for (int i = 0; mDashboardConfigurationResetListenerRefs != null && i < mDashboardConfigurationResetListenerRefs.size(); i++) {
            WeakReference<DashboardConfigurationResetListener> listenerRef = mDashboardConfigurationResetListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            DashboardConfigurationResetListener listener = listenerRef.get();
            if (listener != null) {
                listener.resetSettings();
            }
        }
    }

    private void registerFactoryResetReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_FACTORY_RESET);
        getContext().registerReceiverAsUser(mEventsBroadcastReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    private void registerClearDataReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.CLEAR_APP_DATA_INTENT);
        getContext().registerReceiverAsUser(mEventsBroadcastReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    private void registerSettingsCallbacks() {
        mTvSettingsManager.SetCallBacks(mSettingsCallbacks, new int[]{
                TvSettingsConstants.PBSMGR_PROPERTY_PREMISES_NAME,
                TvSettingsConstants.PBSMGR_PROPERTY_PMS_DISPLAYNAME,
                TvSettingsConstants.LASTSELECTEDURI
        });

        mTvSettingsManager.SetCallBackWithValues(mSettingsWithValueCallbacks, new int[]{
                TvSettingsConstants.PBSMGR_PROPERTY_DNT_CLOCK_FORMAT,
                TvSettingsConstants.PBSMGR_PROPERTY_APPS_COUNTRY,
                TvSettingsConstants.MENULANGUAGE,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_CATEGORY_FILTER_ENABLE,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_COUNTRY_FILTER_ENABLE,
                //TvSettingsConstants.PBSMGR_PROPERTY_PRODUCT_TYPE,

                TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE,
                TvSettingsConstants.PBSMGR_PROPERTY_PMS_SERVICE,
                TvSettingsConstants.PBSMGR_PROPERTY_TVDISCOVERY_SERVICE,
                TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_PMS_SERVICE,
                TvSettingsConstants.PBSMGR_PROPERTY_WEB_LISTEN_TVDISCOVERY_SERVICE,
                TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_BILL,
                TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_GUESTMESSAGES,


                TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO,
                TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_BROWSER_SETTINGS_SOURCE,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURES_SMARTINFO_APPS,
                TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_APPS,

                TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_SHOW_CHANNELS,
                TvSettingsConstants.PBSMGR_PROPERTY_ACTIVE_CHANNELFILTER,
                TvSettingsConstants.PBSMGR_PROPERTY_TV_CHANNELS,
                TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_RADIO_CHANNELS,
                TvSettingsConstants.PBSMGR_PROPERTY_AV_MEDIA,
                TvSettingsConstants.PBSMGR_PROPERTY_CHANNELS_TIF_IN_CHANNEL_LIST,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_THEME_TV_ENABLE,
                TvSettingsConstants.PBSMGR_PROPERTY_SELECTABLE_AV,
                TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_EPG,
                TvSettingsConstants.INSTSETTINGSDVBEPGCHOICE,
                TvSettingsConstants.INSTSETTINGSCHANNELLOGOS,

                TvSettingsConstants.PBSMGR_PROPERTY_FORCE_CUSTOM_NAME,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_WEATHER_APP,

                TvSettingsConstants.PBSMGR_PROPERTY_MY_CHOICE_MYCHOICE,

                TvSettingsConstants.PBSMGR_PROPERTY_SELECTABLE_AV,
                TvSettingsConstants.PBSMGR_PROPERTY_GOOGLE_CAST,

                TvSettingsConstants.PBSMGR_PROPERTY_DASHBOARD,
                TvSettingsConstants.LASTSELECTEDDEVICE,
                TvSettingsConstants.PBSMGR_PROPERTY_SWITCH_ON_CHANNEL_FILTER,
                TvSettingsConstants.PBSMGR_PROPERTY_SWITCH_ON_CHANNEL,
                TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_All_CHANNELS,
                TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_SOURCES,
                TvSettingsConstants.PBSMGR_PROPERTY_DISPLAY_FILTTER_OTT_APP,
                TvSettingsConstants.PBSMGR_PROPERTY_ENABLE_OTT_APP_CHANNELS,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_SMARTTV_MODE,
                TvSettingsConstants.PBSMGR_PROPERTY_DIAGNOSTIC_ENABLE,
                TvSettingsConstants.PBSMGR_PROPERTY_APPCONTROL_ID_TYPE,
                TvSettingsConstants.PBSMGR_PROPERTY_DIAGNOSTIC_DESTINATION,
                TvSettingsConstants.PBSMGR_PROPERTY_PMS_FEATURES_ROOMSTATUS,
                TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_TALKBACK_ENABLE,
                TvSettingsConstants.PBSMGR_PROPERTY_SHARING_SHOW_TNC,
                TvSettingsConstants.PBSMGR_PROPERTY_HTV_SWITCH_ON_GUEST_MANAGEMENT_ANDROID_SYSTEM_MENU_LANGUAGE,
        });
    }

    private void registerTimeChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Constants.ACTION_VALID_CLOCK_SOURCE_ACQUIRED);
        getContext().registerReceiverAsUser(mTimeChangeReceiver, UserHandle.CURRENT_OR_SELF, intentFilter, null, null);
    }

    private void notifyClockFormatChanged() {
        if (mClockSettingChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mClockSettingChangeListenerRefs.size(); i++) {
            WeakReference<ClockSettingChangeListener> ref = mClockSettingChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ClockSettingChangeListener listener = ref.get();
            if (listener != null) {
                listener.onClockFormatChanged();
            }
        }
    }

    private void notifyClockFormatChanged(int value) {
        if (mClockSettingChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mClockSettingChangeListenerRefs.size(); i++) {
            WeakReference<ClockSettingChangeListener> ref = mClockSettingChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ClockSettingChangeListener listener = ref.get();
            if (listener != null) {
                listener.onClockFormatChanged(value);
            }
        }
    }

    private void notifyTimeChanged() {
        if (mClockSettingChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mClockSettingChangeListenerRefs.size(); i++) {
            WeakReference<ClockSettingChangeListener> ref = mClockSettingChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ClockSettingChangeListener listener = ref.get();
            if (listener != null) {
                listener.onTimeChanged();
            }
        }
    }

    private void notifyTimeTick() {
        if (mClockSettingChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mClockSettingChangeListenerRefs.size(); i++) {
            WeakReference<ClockSettingChangeListener> ref = mClockSettingChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            ClockSettingChangeListener listener = ref.get();
            if (listener != null) {
                listener.onTimeTick();
            }
        }
    }

    private boolean hasSavedImages() {
        File imagesDirectory = new File(getContext().getFilesDir() + Constants.PATH_IMAGES);
        if (!imagesDirectory.exists()) {
            return false;
        }
        File[] childDirectories = imagesDirectory.listFiles();
        if (childDirectories == null || childDirectories.length == 0) {
            return false;
        }

        for (int i = 0; i < childDirectories.length; i++) {
            File[] images = childDirectories[i].listFiles();
            if (images != null && images.length > 0) {
                return true;
            }
        }
        return false;
    }

    public int getDefaultHflSidePanelBackgroundColor(){
        return  getContext().getColor(R.color.side_panel_background_default);
    }

    public int getDefaultBflSidePanelBackgroundColor(){
        return  getContext().getColor(R.color.side_panel_background_default_bfl);
    }

    private boolean hasSavedConfigurationPreferences() {
        int sidePanelBackgroundDefaultColor;
        if(isBFLProduct()){
            sidePanelBackgroundDefaultColor = getContext().getColor(R.color.side_panel_background_default_bfl);
        }else{
            sidePanelBackgroundDefaultColor = getContext().getColor(R.color.side_panel_background_default);
        }
        int sidePanelHighlightedDefaultColor = getContext().getColor(R.color.side_panel_highlighted_text_color_default);
        int sidePanelNonHighlightedDefaultColor = getContext().getColor(R.color.side_panel_non_highlighted_text_color_default);
        int mainBackgroundDefaultColorFilter = getContext().getColor(R.color.main_background_default_color_filter);
        boolean mainBackgroundEnabledDefaultValue = getContext().getResources().getBoolean(R.bool.enable_main_background);

        if (getSidePanelBackgroundColor() != sidePanelBackgroundDefaultColor) {
            return true;
        }
        if (getSidePanelHighlightedTextColor() != sidePanelHighlightedDefaultColor) {
            return true;
        }
        if (getSidePanelNonHighlightedTextColor() != sidePanelNonHighlightedDefaultColor) {
            return true;
        }
        if (getMainBackgroundColorFilter() != mainBackgroundDefaultColorFilter) {
            return true;
        }
        if (isMainBackgroundEnabled() != mainBackgroundEnabledDefaultValue) {
            return true;
        }

        return false;
    }

    public WeatherInfo getWeatherInfo() {
        return mWeatherDataManager.getWeatherInfo();
    }
	private void setSmartTVMode(){
		mSmartTVMode = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_SMARTTV_MODE   , 0, 0);
	}
	private int getClockFormat(){
		return mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DNT_CLOCK_FORMAT, 0, 0);
	}

    private void setAppsCountry() {
        mAppsCountry = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_APPS_COUNTRY, 0, -1);
    }

    private void updateAppsCountry(int value) {
        mAppsCountry = value;
    }

    private void setAppCountryFilterEnabledState() {
        mIsAppCountryFilterEnabled = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_COUNTRY_FILTER_ENABLE, 0, -1) == 1;
    }

    private void onAppsCountryChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APPS_COUNTRY_CHANGED);
        message.sendToTarget();
    }

    private void onAppsCountryChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APPS_COUNTRY_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void onAppCountryFilterEnabledStateChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APP_COUNTRY_FILTER_ENABLED_SETTING_CHANGED);
        message.sendToTarget();
    }

    private void onAppCountryFilterEnabledStateChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APP_COUNTRY_FILTER_ENABLED_SETTING_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void onProductTypeStateChanged(int value){
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_PRODUCT_TYPE_STATE_CHANGED);
        message.arg1 = value;
        mUiThreadHandler.removeCallbacksAndMessages(null);
        message.sendToTarget();
    }

    private static BroadcastReceiver mCloneInReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DashboardDataManager", "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_STOP_DDB));
            recycleAllCachedBitmap();
        }
    };

    public static void recycleAllCachedBitmap(){
        recycleOldBitmapDrawable(mMainBackgroundSavedBitMapDrawable);
        recycleOldBitmapDrawable(mCastBackgroundBitMapDrawable);
        recycleOldBitmap(mHotelLogoBitmap);
        setCastBackgroundFetchOngoing(false);
        setHotelLogoBitmapFetchOngoing(false);
        setMainBackgroundBitmapFetchOngoing(false);

    }

    private static void registerCloneInReceiver( Context context) {
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_CLONE_IN);
        LocalBroadcastManager.getInstance(context).registerReceiver(mCloneInReceiver, intentFilter);
    }

    private void onDashboardModeChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_DASHBOARD_MODE_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    private void appsCountryChanged(int value) {
        updateAppsCountry(value);
        notifyPbsSettingCountryChanged(mAppsCountry);
    }

    private void appsCountryChanged() {
        setAppsCountry();
        notifyPbsSettingCountryChanged(mAppsCountry);
    }

    private void appCountryFilterEnabledStateChanged() {
        setAppCountryFilterEnabledState();
        notifyAppsCountryStateChanged();
    }

    private void dashboardModeChanged(int dashboardMode) {
        updateDashboardMode(dashboardMode);
        if (mSmartInfoDataManager != null) {
            mSmartInfoDataManager.onDashboardModeChanged();
        }
    }

    private void productTypeChanged(int productType) {
        updateProductType(productType);
        syncCustomizedSettingsToTVSettings();
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION_CLONE_IN));
    }

    private void setProfessionalModeEnabledState() {
        mIsProfessionalModeEnabled = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_PROFESSIONAL_MODE, 0, 0) == 1;
    }
    private void updateProfessionalModeEnabledState(int value) {
        mIsProfessionalModeEnabled = (value == 1);
    }

    private void setDashboardMode() {
        mDashboardMode = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_DASHBOARD, 0, 0);
    }

    private void updateDashboardMode(int dashboardMode) {
        mDashboardMode = dashboardMode;
    }

    private void updateProductType(int productType){
        mProductType = productType;
    }

    public interface GoogleAccountFlowListener {
        void startGoogleAccountFlow();
    }

    public interface ChannelLogoFetchListener {
        void onChannelLogoFetchComplete(int channelId, Bitmap logo);
    }

    public interface ProgramThumbnailFetchListener {
        void onProgramThumbnailFetchComplete(int programId, int channelId, Bitmap thumbnail);
    }

    interface CloneFileUriListener {
        void cloneInFileUriCreated(Pair<Uri, Uri> uri);

        void cloneOutFileUriCreated(Pair<Uri, Uri> uri);
    }

    interface CloneDataListener {
        void cloneDataApplied(boolean success);

        void cloneDataDeleted(boolean success);
    }

    public interface AccountChangeListener {
        void onAccountChanged();
    }

    public interface GuestCheckInStatusChangeListener {
        void OnGuestCheckInStatusChanged();
    }

    public interface RecommendationListener {
        void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType);

        void onRecommendationsAvailable(int recommendationCategory);
    }

    public interface PreviewProgramsListener {
        void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannels);

        void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannels);

        void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels);
    }
    public interface VideoOnDemandRecommendationListener {
        void onVideoOnDemandRecommendationAvailable();

        void onVideoOnDemandRecommendationUnavailable();

        void onVideoOnDemandRecommendationAppsUpdated();
    }

    public interface GamesRecommendationListener {
        void onGamesRecommendationAvailable();

        void onGamesRecommendationUnavailable();

        void onGameRecommendationAppsUpdated();
    }

    public interface AppRecommendationsListener {
        void onAppRecommendationsAvailable();

        void onAppRecommendationsUnavailable();

        void onAppRecommendationsEnabled(String packageName);

        void onAppRecommendationsDisabled(String packageName);
    }

    interface SmartInfoAppDataFetchListener {
        void onSmartInfoAppDataFetched(String packageName, String title, Drawable icon);
    }

    interface SmartInfoFileParseListener {
        void onSmartInfoFileParseComplete(List<SmartInfo> smartInfoData, String title, Drawable icon, String smartInfoDescription, String smartInfoMainUrl);
    }
	
	public interface SmartInfoPreviewProgramListener{
		void onSmartinfoPreviewProgramFetchComplete(List<PreviewProgramsChannel> SmartInfoPreviewChannel);
	}

    public interface SmartInfoListener {
        void onSmartInfoDataAvailable();

        void onSmartInfoRecommendationsAvailable();

		void onSmartInfoPreviewProgramsAvailable();

        void onSmartInfoOff();

        void onSmartInfoUnavailable();
    }

    interface ContentRatingFetchCompleteListener {
        void onContentRatingFetchComplete(ConcurrentHashMap<String, ContentRatingStore.RatingInfo> ratingMap);
    }

    public interface RecommendationListenerServiceClient {
        void onRecommendationListenerServiceAvailable(WeakReference<RecommendationListenerService.RecommendationServiceAdapter> adapterRef);

        void onRecommendationListenerServiceUnavailable();
    }

    public interface AppLogoFetchListener {
        void onAppLogoFetchComplete(String packageName, Drawable logo);
    }

    public interface AppDataListener {
        void onAppListFetched();
    }

    public interface AllAppListFetchListener {
        void onAllAppListFetchComplete(List<AppInfo> allApps);
    }

    public interface GoogleCastListener {
        void onHotspotInit();

        void onHotspotStarted(String networkName, String passphrase, Bitmap qrCodeBitmap);

        void onHotspotStopped();

        void onHotspotConnecting();

        void onHotspotConnected (String deviceName, String address, boolean isShowTerms);

        void onHotspotDisconnected(String deviceName);

        public void onHotspotError(int errorCode);

    }

    public interface MyChoiceListener {
        void onMyChoiceEnabledStateChanged();

        void onMyChoiceEnabledStateChanged(int value);

        void onMyChoiceDataChanged();

        void onMyChoiceLockStatusChanged();
    }

    public static final class ImageFile {
        private File mFile;
        private int mWidth;
        private int mHeight;

        ImageFile(File file, int width, int height) {
            mFile = file;
            mWidth = width;
            mHeight = height;
        }

        public File getFile() {
            return mFile;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }
    }

    private static final class EventsBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DdbLogUtility.logCommon("DashboardDataManager", "onReceive() action = " + action);
            if (Constants.ACTION_FACTORY_RESET.equals(action)) {
                DashboardDataManager.getInstance().resetSettings();
                return;
            }
            if (Constants.CLEAR_APP_DATA_INTENT.equals(action)) {
                DashboardDataManager.getInstance().onClearDataTriggered();
                return;
            }
        }
    }

    private static class UiThreadHandler extends Handler {

        private WeakReference<DashboardDataManager> mDashboardDataManagerRef;

        private static final int MSG_WHAT_APPS_COUNTRY_CHANGED = 100;
        private static final int MSG_WHAT_APP_COUNTRY_FILTER_ENABLED_SETTING_CHANGED = 101;
        private static final int MSG_WHAT_DASHBOARD_MODE_CHANGED = 102;
        private static final int MSG_WHAT_PRODUCT_TYPE_STATE_CHANGED = 103;
        private static final int MSG_WHAT_DEFAULT_IMAGE_STATE_CHANGED = 104;

        private UiThreadHandler(DashboardDataManager dashboardDataManager) {
            super();
            mDashboardDataManagerRef = new WeakReference<>(dashboardDataManager);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCommon("DashboardDataManager", "handleMessage() called with: msg.what = " + msg.what );
            if (what == MSG_WHAT_APPS_COUNTRY_CHANGED) {
                DashboardDataManager dashboardDataManager = mDashboardDataManagerRef.get();
                if (dashboardDataManager != null) {
                    dashboardDataManager.appsCountryChanged(msg.arg1);
                }
                return;
            }
            if (what == MSG_WHAT_APP_COUNTRY_FILTER_ENABLED_SETTING_CHANGED) {
                DashboardDataManager dashboardDataManager = mDashboardDataManagerRef.get();
                if (dashboardDataManager != null) {
                    dashboardDataManager.appCountryFilterEnabledStateChanged();
                }
                return;
            }

            if (what == MSG_WHAT_DASHBOARD_MODE_CHANGED) {
                DashboardDataManager dashboardDataManager = mDashboardDataManagerRef.get();
                if (dashboardDataManager != null) {
                    dashboardDataManager.dashboardModeChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_PRODUCT_TYPE_STATE_CHANGED) {
                DashboardDataManager dashboardDataManager = mDashboardDataManagerRef.get();
                if (dashboardDataManager != null) {
                    dashboardDataManager.productTypeChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_DEFAULT_IMAGE_STATE_CHANGED) {
                DashboardDataManager dashboardDataManager = mDashboardDataManagerRef.get();
                if (dashboardDataManager != null) {
                    dashboardDataManager.setDefaultCloneImageState();
                }
                return;
            }
        }
    }

    public void setCastChapterFragment(boolean value){
        mIsCastChapterFragment = value;
    }

    public boolean getCastChapterFragment(){
        return mIsCastChapterFragment;
    }
    
    public static CulLogger bindCULLoggerService(Context c){
        return CulLogger.getInstance(c.getApplicationContext());
    }

    private void registerGuestCheckInStatusChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.INTENT_ACTION_GUEST_CHECK_IN_STATUS_CHANGE);
        intentFilter.addAction(Constants.INTENT_ACTION_GUEST_CHECK_OUT_STATUS_CHANGE);
        DashboardDataManager.getInstance().getContext().getApplicationContext().registerReceiverAsUser(mGuestCheckInStatusChangeReceiver, UserHandle.CURRENT_OR_SELF,intentFilter, null, null);
    }
}
