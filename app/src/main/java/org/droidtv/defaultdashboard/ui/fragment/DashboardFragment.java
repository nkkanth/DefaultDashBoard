package org.droidtv.defaultdashboard.ui.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidtv.defaultdashboard.EditDashboardMenu;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AccountChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppRecommendationsListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.BackgroundImageChangeObserver;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelSettingsListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.DashboardPmsStateChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.GamesRecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.HotelLogoChangeObserver;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.PbsSettingCountryChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SidePanelListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SmartInfoListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SourceDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.VideoOnDemandRecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.WeatherInfoDataListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.AppsChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.CastChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.ChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.GamesChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.MoreChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.RecommendedChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.data.model.TVChannelsChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.VideoOnDemandChapterHeaderItem;
import org.droidtv.defaultdashboard.data.model.channelFilter.ChannelFilter;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.receiver.MessageCountChangeReceiver;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.ui.view.TopMenuView.TopMenuViewAdapter;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.ui.tvwidget2k15.tvtoast.TvToast;
import org.droidtv.ui.tvwidget2k15.tvtoast.TvToastMessenger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BrowseFrameLayout;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.TitleViewAdapter;

/**
 * Created by sandeep on 01-10-2017.
 */

public class DashboardFragment extends AbstractDashboardFragment implements SidePanelListener, BackgroundImageChangeObserver,
        HotelLogoChangeObserver, VideoOnDemandRecommendationListener, ChannelSettingsListener, WeatherInfoDataListener, AccountChangeListener,
        AppRecommendationsListener, SmartInfoListener, GamesRecommendationListener, AppDataListener, SourceDataListener, PbsSettingCountryChangeListener,
        DashboardPmsStateChangeListener, DashboardDataManager.GuestNameChangeListener, DashboardDataManager.AppsCountryFilterStateListener,
        DashboardDataManager.BillSettingsListener, DashboardDataManager.MessageSettingsListener, DashboardDataManager.PreviewProgramsListener {

    private static final String TAG = "DashboardFragment";

    private static final long HEADER_ID_RECOMMENDED = 1;
    private static final long HEADER_ID_TV_CHANNELS = 2;
    private static final long HEADER_ID_VIDEO_ON_DEMAND = 3;
    private static final long HEADER_ID_CAST = 4;
    private static final long HEADER_ID_APPS = 5;
    private static final long HEADER_ID_GAMES = 6;
    private static final long HEADER_ID_MORE = 7;

    private DashboardDataManager mDashboardDataManager;
    private ArrayObjectAdapter mRowsAdapter;
    private Handler mBackgroundImageUpdateHandler;
    private Handler mHotelLogoImageUpdateHandler;
    private int mMainBackgroundColorFilter;
    private Drawable mForegroundOverlay;
    private SharedPreferences mDefaultSharedPreferences;
    private TopMenuViewAdapter mTopMenuViewAdapter;

    private ValueAnimator mApplyForegroundOverlayAnimator;
    private ValueAnimator mRemoveForegroundOverlayAnimator;
    private ValueAnimator.AnimatorUpdateListener mForegroundOverlayAnimatorListener;

    public final static int FULL_TOP_ROW_VISIBLE = TitleViewAdapter.FULL_VIEW_VISIBLE;
    public final static int WELCOME_VISIBLE = 3;
    public final static int WELCOME_INVISIBLE = 5;
    public final static int TOP_MENU_ITEMS_INVISIBLE = 9;
    public final static int TOP_MENU_ITEMS_VISIBLE = 10;
    private Handler mUiThreadHandler;
    private MessageCountChangeReceiver mMessageCountChangeReceiver;

    private PageRow mRecommendedChapterPageRow;
    private PageRow mTvChannelsChapterPageRow;
    private PageRow mVideoOnDemandChapterPageRow;
    private PageRow mCastChapterPageRow;
    private PageRow mAppsChapterPageRow;
    private PageRow mGamesChapterPageRow;
    private PageRow mMoreChapterPageRow;

    private static final int RECOMMENDED_CHAPTER_POSITION = 0;
    private static final int TV_CHANNELS_CHAPTER_POSITION = 1;
    private static final int VOD_CHAPTER_POSITION = 2;
    private static final int CAST_CHAPTER_POSITION = 3;
    private static final int APPS_CHAPTER_POSITION = 4;
    private static final int GAMES_CHAPTER_POSITION = 5;
    private static final int MORE_CHAPTER_POSITION = 6;

    private static final long BACKGROUND_IMAGE_UPDATE_DELAY_MS = 100;
    private static final long HOTEL_LOGO_IMAGE_UPDATE_DELAY_MS = 100;
    private static final long OVERLAY_ANIMATION_DURATION = 600;

    private boolean mIsFragmentStarted;
    private BrowseFrameLayout mBrowseFrameLayout;
    private Runnable mSetSelectionRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
        mUiThreadHandler = new UiThreadHandler(this);
        mDashboardDataManager = DashboardDataManager.getInstance();
        setupUi();
        loadData();
        mMessageCountChangeReceiver = new MessageCountChangeReceiver();
        registerMessageCountBroadcastReceiver();
        registerPreviewChannelListner();
        getMainFragmentRegistry().registerFragment(PageRow.class, new PageRowFragmentFactory());
        mBackgroundImageUpdateHandler = new Handler();
        mHotelLogoImageUpdateHandler = new Handler();
        mDefaultSharedPreferences = mDashboardDataManager.getDefaultSharedPreferences();
        mIsFragmentStarted = false;
        //TODO: A very bad workaround! Remove this code when we move to android jetpack library from support library
        mSetSelectionRunnable = getSetSelectionRunnableByReflection();
    }

    private void registerPreviewChannelListner(){
        mDashboardDataManager.addPreviewProgramsListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreateView()");
        View root = super.onCreateView(inflater, container, savedInstanceState);
        applyForegroundOverlay(root.findViewById(R.id.browse_container_dock));
        initForegroundOverlayAnimators();

        getHeadersSupportFragment().setOnHeaderClickedListener(new HeadersSupportFragment.OnHeaderClickedListener() {
            @Override
            public void onHeaderClicked(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
                if (mIsFragmentStarted) {
                    boolean canShowHeaders = getHeadersState() != HEADERS_DISABLED;
                    boolean isShowingHeaders = isShowingHeaders();
                    if (!canShowHeaders || !isShowingHeaders || isInHeadersTransition()) {
                        return;
                    }
                    startHeadersTransition(false);
                    Fragment mainFragment = getMainFragment();
                    View mainFragmentView = null;
                    if (mainFragment != null) {
                        mainFragmentView = getMainFragment().getView();
                    }
                    if (mainFragmentView != null) {
                        mainFragmentView.requestFocus();
                    }
                }
            }
        });
		getHeadersSupportFragment().setOnHeaderViewSelectedListener(new HeadersSupportFragment.OnHeaderViewSelectedListener(){
			@Override
			public void onHeaderSelected(RowHeaderPresenter.ViewHolder viewHolder, Row row) {
				int position = getHeadersSupportFragment().getSelectedPosition();
                Log.d(TAG, "header selected position " + position);
				//TODO: Sup: cleanup once it is proven working.
				mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_HANDLE_HEADER_ITEM_SELECTED);
				Message message = Message.obtain(mUiThreadHandler, UiThreadHandler. MSG_WHAT_HANDLE_HEADER_ITEM_SELECTED);
				message.arg1 = position;
				mUiThreadHandler.sendMessageDelayed(message, 250);
            }
		});

        mBrowseFrameLayout = (BrowseFrameLayout) root.findViewById(androidx.leanback.R.id.browse_frame);
        final BrowseFrameLayout.OnFocusSearchListener onFocusSearchListener = mBrowseFrameLayout.getOnFocusSearchListener();
        mBrowseFrameLayout.setOnFocusSearchListener(new BrowseFrameLayout.OnFocusSearchListener() {
            @Override
            public View onFocusSearch(View focused, int direction) {
                if (getMainFragment() != null && getTitleView() != null && getTitleView().hasFocus()
                        && direction == View.FOCUS_DOWN) {
                    return getMainFragment().getView();
                }

                if (getMainFragment() != null && getMainFragment().getView() != null && getMainFragment().getView().hasFocus()
                        && direction == View.FOCUS_DOWN) {
                    return focused;
                }

                boolean isRtl = ViewCompat.getLayoutDirection(focused)
                        == ViewCompat.LAYOUT_DIRECTION_RTL;
                int towardStart = isRtl ? View.FOCUS_RIGHT : View.FOCUS_LEFT;
                if (getHeadersState() == HEADERS_ENABLED && direction == towardStart) {
                    if (isVerticalScrolling() || !isHeadersDataReady()) {
                        return focused;
                    }
                    return getHeadersSupportFragment().getVerticalGridView();
                }

                return onFocusSearchListener.onFocusSearch(focused, direction);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        registerListeners();
        mMainBackgroundColorFilter = mDashboardDataManager.getMainBackgroundColorFilter();
        mTopMenuViewAdapter = (TopMenuViewAdapter) getTitleViewAdapter();
        mTopMenuViewAdapter.setHeadersFragments(getHeadersSupportFragment());
        setPremisesName();
        updateGuestName();
        applyMainBackground();
        applyHotelLogo();
        loadCastBackground();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "#### enter onResume()");
        super.onResume();
        mDashboardDataManager.updateAlarmTime();
        Log.d(TAG, "#### exit onResume()");
    }

    @Override
    public void onStart() {
        Log.d(TAG, "#### enter onStart()");
        super.onStart();
        mIsFragmentStarted = true;
        mDashboardDataManager.addSmartInfoListener(this);
        mDashboardDataManager.addVideoOnDemandRecommendationListener(this);
        mDashboardDataManager.addGamesRecommendationListener(this);
        mDashboardDataManager.addAppRecommendationsListener(this);
        mDashboardDataManager.registerChannelSettingsListener(this);
        mDashboardDataManager.registerGuestNameChangeListener(this);
        mDashboardDataManager.addAppDataListener(this);
        registerSourcesChangeListener();
        mDashboardDataManager.registerPbsSettingCountryChangeListener(this);

        checkAndResetProfileAccessPref();
        startGoogleSignInFlow();
        // Fetch channels if not already fetched. This updates visibility of Recommended Channels shelf and Tv Channels chapter
        checkAndFetchChannels();
        // Fetch apps if apps are not already fetched. This updates visibility of Recommended Apps shelf and Apps chapter
        checkAndFetchApps();
        if(isRecommendedChapterAvailable()) {
            mDashboardDataManager.fetchPreviewProgramsChannels();
        }
        fetchVodRecommendations();
        fetchGamesRecommendations();
        checkLaunchMode();
        updateChaptersVisibility();

        Log.d(TAG, "#### exit onStart()");
    }

    private void startGoogleSignInFlow() {
        mUiThreadHandler.removeMessages(Constants.GOOGLE_SIGN_IN_DELAY);
        if(mDashboardDataManager.getGoogleSignInAfter2Min()) {
            Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_START_GOOGLE_SIGN_IN);
            mUiThreadHandler.sendMessageDelayed(message, Constants.GOOGLE_SIGN_IN_DELAY);
        }else{
            mDashboardDataManager.startGoogleAccountFlow();
        }
    }

    private void fetchGamesRecommendations() {
        if(isGamesChapterAvailable()){
            mDashboardDataManager.fetchPreviewProgramsGamesChannels();
        }
    }

    private void fetchVodRecommendations() {
        if(isVideoOnDemandChapterAvailable()){
            mDashboardDataManager.fetchPreviewProgramsVODChannels();
        }
    }



    @Override
    public void onStop() {
        Log.d(TAG, "#### enter onStop()");
        super.onStop();
        mIsFragmentStarted = false;
        if (mBrowseFrameLayout != null && mSetSelectionRunnable != null) {
            boolean result = mBrowseFrameLayout.removeCallbacks(mSetSelectionRunnable);
            Log.d(TAG, "#### mSetSelectionRunnable removeCallbacks result:" + result);
        }
        mDashboardDataManager.removeSmartInfoListener(this);
        mDashboardDataManager.removeVideoOnDemandRecommendationListener(this);
        mDashboardDataManager.removeGamesRecommendationListener(this);
        mDashboardDataManager.removeAppRecommendationsListener(this);
        mDashboardDataManager.unregisterGuestNameChangeListener(this);
        mDashboardDataManager.unregisterChannelSettingsListener(this);
        mDashboardDataManager.removeAppDataListener(this);
        unregisterSourcesChangeListener();
        mDashboardDataManager.unregisterPbsSettingCountryChangeListener(this);
        Log.d(TAG, "#### exit onStop()");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mIsFragmentStarted = false;
        if (mBrowseFrameLayout != null && mSetSelectionRunnable != null) {
            boolean result = mBrowseFrameLayout.removeCallbacks(mSetSelectionRunnable);
            Log.d(TAG, "#### onSaveInstanceState mSetSelectionRunnable removeCallbacks result:" + result);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");
        super.onDestroyView();
        clearTopMenuView();
        mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_START_GOOGLE_SIGN_IN);
        mBackgroundImageUpdateHandler.removeCallbacksAndMessages(null);
        mHotelLogoImageUpdateHandler.removeCallbacksAndMessages(null);
        unregisterListeners();
    }

    private void clearTopMenuView() {
        if(mTopMenuViewAdapter != null){
            hideTopMenuItems();
            mTopMenuViewAdapter.setHeadersFragments(null);
            mTopMenuViewAdapter = null;
            Log.d(TAG, "clearTopMenuView: top menu items cleared");
        }
    }

    @Override
    public void onSourcesFetched(List<Source> sources) {
        Log.d(TAG, "#### enter onSourcesFetched()");
        // Sources have been refreshed. Update Cast chapter availability based on whether Google Cast is enabled or disabled
        updateCastChapterVisibility();
        Log.d(TAG, "#### exit onSourcesFetched()");
    }

    @Override
    public void onSourceUpdated(int index, Source source) {
        // Do nothing
    }

    @Override
    public void onLastSelectedDeviceChanged(int updatedDevice) {
        // Do nothing
    }

    @Override
    public void onSourceDisplayChanged() {

    }

    @Override
    public void onVideoOnDemandRecommendationAvailable() {
        Log.d(TAG, "#### enter onVideoOnDemandRecommendationAvailable()");
        updateVideoOnDemandChapterVisibility();
        Log.d(TAG, "#### exit onVideoOnDemandRecommendationAvailable()");
    }

    @Override
    public void onVideoOnDemandRecommendationUnavailable() {
        Log.d(TAG, "#### enter onVideoOnDemandRecommendationUnavailable()");
        updateVideoOnDemandChapterVisibility();
        setSelectionToFirstChapter();
        Log.d(TAG, "#### exit onVideoOnDemandRecommendationUnavailable()");
    }

    @Override
    public void onVideoOnDemandRecommendationAppsUpdated() {
        Log.d(TAG, "#### enter onVideoOnDemandRecommendationAppsUpdated()");
        updateVideoOnDemandChapterVisibility();
        Log.d(TAG, "#### exit onVideoOnDemandRecommendationAppsUpdated()");
    }

    @Override
    public void onGamesRecommendationAvailable() {
        Log.d(TAG, "#### enter onGamesRecommendationAvailable()");
        updateGamesChapterVisibility();
        Log.d(TAG, "#### exit onGamesRecommendationAvailable()");
    }

    @Override
    public void onGamesRecommendationUnavailable() {
        Log.d(TAG, "#### enter onGamesRecommendationUnavailable()");
        updateGamesChapterVisibility();
        Log.d(TAG, "#### exit onGamesRecommendationUnavailable()");
    }

    @Override
    public void onGameRecommendationAppsUpdated() {
        Log.d(TAG, "#### enter onGameRecommendationAppsUpdated()");
        updateRecommendedChapterVisibility();
        updateGamesChapterVisibility();
        removeGamesChapter();
        updateMoreChapterVisibility();
        Log.d(TAG, "#### exit onGameRecommendationAppsUpdated()");
    }

    @Override
    public void onAppRecommendationsAvailable() {
        Log.d(TAG, "#### enter onAppRecommendationsAvailable()");
        updateRecommendedChapterVisibility();
        Log.d(TAG, "#### exit onAppRecommendationsAvailable()");
    }

    @Override
    public void onAppRecommendationsUnavailable() {
        Log.d(TAG, "#### enter onAppRecommendationsUnavailable()");
        updateRecommendedChapterVisibility();
        setSelectionToFirstChapter();
        Log.d(TAG, "#### exit onAppRecommendationsUnavailable()");
    }

    @Override
    public void onAppRecommendationsDisabled(String packageName) {

    }

    @Override
    public void onAppRecommendationsEnabled(String packageName) {

    }

    @Override
    public void onSmartInfoDataAvailable() {
        Log.d(TAG, "#### enter onSmartInfoDataAvailable()");
        updateRecommendedChapterVisibility();
        Log.d(TAG, "#### exit onSmartInfoDataAvailable()");
    }

    @Override
    public void onSmartInfoRecommendationsAvailable() {
        Log.d(TAG, "#### enter onSmartInfoRecommendationsAvailable()");
        updateRecommendedChapterVisibility();
        Log.d(TAG, "#### exit onSmartInfoRecommendationsAvailable()");
    }
	
	@Override
	public void onSmartInfoPreviewProgramsAvailable(){
		Log.d(TAG, "#### enter onSmartInfoPreviewProgramsAvailable()");
        updateRecommendedChapterVisibility();
        Log.d(TAG, "#### exit onSmartInfoPreviewProgramsAvailable()");
	}


    @Override
    public void onSmartInfoOff() {
        Log.d(TAG, "#### enter onSmartInfoOff()");
        updateRecommendedChapterVisibility();
        Log.d(TAG, "#### exit onSmartInfoOff()");
    }

    @Override
    public void onSmartInfoUnavailable() {
        Log.d(TAG, "#### enter onSmartInfoUnavailable()");
        updateRecommendedChapterVisibility();
        Log.d(TAG, "#### exit onSmartInfoUnavailable()");
    }

    @Override
    public void onChannelsSettingChanged(boolean showChannels) {
        Log.d(TAG, "#### enter onChannelsSettingChanged().showChannels:" + showChannels);
        updateChaptersVisibility();
        if (!showChannels) {
            setSelectionToFirstChapter();
        }
        Log.d(TAG, "#### exit onChannelsSettingChanged()");
    }

    @Override
    public void onAppListFetched() {
        Log.d(TAG, "#### enter onAppListFetched()");
        updateRecommendedChapterVisibility();
        updateAppsChapterVisibility();
        updateGamesChapterVisibility();
        Log.d(TAG, "#### exit onAppListFetched()");
    }

    @Override
    public void onWeatherInfoDataReceived() {
        Log.d(TAG, "#### enter onWeatherInfoDataReceived()");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_WEATHER_INFO_DATA_RECEIVED);
        message.sendToTarget();
        Log.d(TAG, "#### exit onWeatherInfoDataReceived()");
    }

    @Override
    public void onWeatherInfoDataReceived(int value) {
        Log.d(TAG, "#### enter onWeatherInfoDataReceived()");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_WEATHER_INFO_DATA_RECEIVED);
        message.arg1 = value;
        message.sendToTarget();
        Log.d(TAG, "#### exit onWeatherInfoDataReceived()");
    }

    @Override
    public void onAccountChanged() {
        Log.d(TAG, "#### enter onAccountChanged()");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_ACCOUNT_CHANGED);
        message.sendToTarget();
        Log.d(TAG, "#### exit onAccountChanged()");
    }

    @Override
    public void showTitle(int flags) {
        if (flags == FULL_TOP_ROW_VISIBLE && isShowingHeaders()) {
            getTitleViewAdapter().updateComponentsVisibility(WELCOME_INVISIBLE);
        } else {
            super.showTitle(flags);
        }
    }

    private void setupUi() {
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        int backgroundColor = mDashboardDataManager.getSidePanelBackgroundColor();
        setBrandColor(backgroundColor);
        showTitle(true);
        setBrowseTransitionListener(new BrowseTransitionListener() {
            @Override
            public void onHeadersTransitionStart(boolean withHeaders) {
                if (withHeaders) {
                    getTitleViewAdapter().updateComponentsVisibility(WELCOME_INVISIBLE);
                    animateApplyOverlay();
                } else {
                    animateRemoveOverlay();
                }
            }

            @Override
            public void onHeadersTransitionStop(boolean withHeaders) {
                if (withHeaders) {
                    showTitle(true);
                } else {
                    if (isShowingTitle()) {
                        getTitleViewAdapter().updateComponentsVisibility(WELCOME_VISIBLE);
                    }
                }
            }
        });
        prepareEntranceTransition();
    }

    private void setPremisesName() {
        DdbLogUtility.logCommon(TAG, "setPremisesName() called: "+ mDashboardDataManager.getPremisesName());
        if(mTopMenuViewAdapter != null) {
            mTopMenuViewAdapter.setPremiseName(mDashboardDataManager.getPremisesName());
        }else{
            Log.d(TAG, "setPremisesName: titleViewAdapter null");
        }
    }

    private void updateGuestName() {
        String guestName = mDashboardDataManager.getGuestName();
        updateGuestName(guestName);
        DdbLogUtility.logCommon(TAG, "updateGuestName() called guestName: " + guestName);
    }

    private void updateGuestName(String name) {
        if(mTopMenuViewAdapter == null){
            Log.d(TAG, "updateGuestName: TitleViewAdpeter null");
            return;
        }
        String greetingMessage = "";
        if(DashboardDataManager.getInstance().isHFLProduct()){
            greetingMessage = DashboardDataManager.getInstance().getContext().
                    getString(org.droidtv.ui.htvstrings.R.string.HTV_WI_DASHBOARD_GREETING_WELCOME);
        }

        if (name != null) {
            greetingMessage = greetingMessage.concat(" ").concat(name);
        }
        mTopMenuViewAdapter.setWelcomeMessage(greetingMessage);
        DdbLogUtility.logCommon(TAG, "updateGuestName() called greetingMessage: " + greetingMessage);
    }

    private void loadData() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        createRows();
        startEntranceTransition();
    }

    private void createRows() {
        createRecommendedChapterRow();

        createTvChannelsChapterRow();

        createVideoOnDemandChapterRow();

        createCastChapterRow();

        createAppsChapterRow();

        createGamesChapterRow();

        createMoreChapterRow();
    }

    private void createRecommendedChapterRow() {
        ChapterHeaderItem recommendedHeaderItem = new RecommendedChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_RECOMMENDED);
        mRecommendedChapterPageRow = new PageRow(recommendedHeaderItem);
    }

    private void createTvChannelsChapterRow() {
        ChapterHeaderItem tvChannelsChapterHeaderItem = new TVChannelsChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_TV_CHANNELS);
        mTvChannelsChapterPageRow = new PageRow(tvChannelsChapterHeaderItem);
    }

    private void createVideoOnDemandChapterRow() {
        ChapterHeaderItem videoOnDemandChapterHeaderItem = new VideoOnDemandChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_VIDEO_ON_DEMAND);
        mVideoOnDemandChapterPageRow = new PageRow(videoOnDemandChapterHeaderItem);
    }

    private void createCastChapterRow() {
        ChapterHeaderItem castChapterHeaderItem = new CastChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_CAST);
        mCastChapterPageRow = new PageRow(castChapterHeaderItem);
    }

    private void createAppsChapterRow() {
        ChapterHeaderItem appsChapterHeaderItem = new AppsChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_APPS);
        mAppsChapterPageRow = new PageRow(appsChapterHeaderItem);
    }

    private void createGamesChapterRow() {
        ChapterHeaderItem gamesChapterHeaderItem = new GamesChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_GAMES);
        mGamesChapterPageRow = new PageRow(gamesChapterHeaderItem);
    }

    private void createMoreChapterRow() {
        ChapterHeaderItem moreChapterHeaderItem = new MoreChapterHeaderItem(DashboardDataManager.getInstance().getContext(), HEADER_ID_MORE);
        mMoreChapterPageRow = new PageRow(moreChapterHeaderItem);
    }

    private void updateChaptersVisibility() {
        updateRecommendedChapterVisibility();
        updateTvChannelsChapterVisibility();
        updateVideoOnDemandChapterVisibility();
        updateCastChapterVisibility();
        updateAppsChapterVisibility();
        updateGamesChapterVisibility();
        updateMoreChapterVisibility();
    }

    private void updateRecommendedChapterVisibility() {
        if (isRecommendedChapterAvailable()) {
            updateChapterPosition(mRecommendedChapterPageRow, getRecommendedChapterPosition());
        } else {
            hideChapter(mRecommendedChapterPageRow);
        }
    }

    private void updateTvChannelsChapterVisibility() {
        if (isTvChannelChapterAvailable()) {
            updateChapterPosition(mTvChannelsChapterPageRow, getTvChannelsChapterPosition());
        } else {
            hideChapter(mTvChannelsChapterPageRow);
        }
    }

    private void updateVideoOnDemandChapterVisibility() {
        if (isVideoOnDemandChapterAvailable()) {
            updateChapterPosition(mVideoOnDemandChapterPageRow, getVideoOnDemandChapterPosition());
        } else {
            hideChapter(mVideoOnDemandChapterPageRow);
        }
    }

    private void updateCastChapterVisibility() {
        if (isCastChapterAvailable()) {
            updateChapterPosition(mCastChapterPageRow, getCastChapterPosition());
        } else {
            hideChapter(mCastChapterPageRow);
        }
    }

    private void applyCastBackgroundImage(BitmapDrawable  bitmapDrawable) {
        if (getSelectedPosition() == getCastChapterPosition()) {
            if (bitmapDrawable != null) {
                if (getView() != null) {
                    getView().setBackground(bitmapDrawable);
                }
            }
        }
    }

    private void updateAppsChapterVisibility() {
        android.util.Log.d(TAG, "updateAppsChapterVisibility: ");
        if (isAppsChapterAvailable()) {
            updateChapterPosition(mAppsChapterPageRow, getAppsChapterPosition());
        } else {
            hideChapter(mAppsChapterPageRow);
        }
    }

    private void updateGamesChapterVisibility() {
        if (isGamesChapterAvailable()) {
            updateChapterPosition(mGamesChapterPageRow, getGamesChapterPosition());
        } else {
            hideChapter(mGamesChapterPageRow);
        }
    }

    private void updateMoreChapterVisibility() {
        if (isMoreChapterAvailable()) {
            updateChapterPosition(mMoreChapterPageRow, getMoreChapterPosition());
        } else {
            hideChapter(mMoreChapterPageRow);
        }
    }

    private void updateChapterPosition(PageRow row, int chapterPosition) {
        /*if (mRowsAdapter.indexOf(row) != -1) {
            mRowsAdapter.remove(row);
        }
        if (chapterPosition > mRowsAdapter.size()) {
            chapterPosition = mRowsAdapter.size();
        }
        mRowsAdapter.add(chapterPosition, row);*/
        int size = mRowsAdapter.size();
        int position = (chapterPosition <= size ) ?  chapterPosition : size;
        DdbLogUtility.logCommon(TAG, "updateChapterPosition: position " + position);
        if (position <= size && mRowsAdapter.indexOf(row) == -1) {
            mRowsAdapter.add(position, row);
        }
    }

    private void hideChapter(PageRow row) {
        boolean isCurrentlySelected = getSelectedPosition() == mRowsAdapter.indexOf(row);
        boolean isRemoved = mRowsAdapter.remove(row);
        if (isRemoved && isCurrentlySelected) {
            setSelectionToFirstChapter();
        }
    }

    private void removeGamesChapter() {
        removeRow(mGamesChapterPageRow);
    }

    private void removeRow(PageRow row) {
        if (row != null) {
            mRowsAdapter.remove(row);
        }
    }

    private void setSelectionToFirstChapter() {
        setSelectedPosition(0, true);
    }

    private void setSelectionToChapter(int chapterPosition) {
        setSelectedPosition(chapterPosition, true);
    }

    @Override
    public void setSelectedPosition(int position, boolean smooth) {
        if (mIsFragmentStarted) {
            Log.d(TAG, "#### enter setSelectedPosition(int position, boolean smooth).position:" + position);
            super.setSelectedPosition(position, smooth);
            Log.d(TAG, "#### exit setSelectedPosition(int position, boolean smooth)");
        }
    }

    @Override
    public void setSelectedPosition(int position) {
        if (mIsFragmentStarted) {
            Log.d(TAG, "#### enter setSelectedPosition(int position).position:" + position);
            super.setSelectedPosition(position);
            Log.d(TAG, "#### exit setSelectedPosition(int position)");
        }
    }

    @Override
    public void setSelectedPosition(int rowPosition, boolean smooth, Presenter.ViewHolderTask rowHolderTask) {
        if (mIsFragmentStarted) {
            Log.d(TAG, "#### enter setSelectedPosition(int rowPosition, boolean smooth, Presenter.ViewHolderTask rowHolderTask).rowPosition:" + rowPosition);
            super.setSelectedPosition(rowPosition, smooth, rowHolderTask);
            Log.d(TAG, "#### exit setSelectedPosition(int rowPosition, boolean smooth, Presenter.ViewHolderTask rowHolderTask)");
        }
    }

    private boolean isVerticalScrolling() {
        return getHeadersSupportFragment().isScrolling() || (getMainFragment() != null && ((MainFragmentAdapterProvider) getMainFragment()).getMainFragmentAdapter().isScrolling());
    }

    private boolean isHeadersDataReady() {
        return getAdapter() != null && getAdapter().size() != 0;
    }

    private void registerListeners() {
        mDashboardDataManager.addSidePanelListener(this);
        mDashboardDataManager.addBackgroundImageChangeObserver(this);
        mDashboardDataManager.addHotelLogoChangeObserver(this);
        mDashboardDataManager.registerWeatherInfoDataListener(this);
        mDashboardDataManager.registerDashboardPmsStateChangeListener(this);
        mDashboardDataManager.addAccountChangeListener(this);
        mDashboardDataManager.registerAppCountryFilterStateChangeListener(this);
        mDashboardDataManager.registerBillSettingsListener(this);
        mDashboardDataManager.registerMessageSettingsListener(this);
    }

    private void unregisterListeners() {
        mDashboardDataManager.removeSidePanelListener(this);
        mDashboardDataManager.removeBackgroundImageChangeObserver(this);
        mDashboardDataManager.removeHotelLogoChangeObserver(this);
        mDashboardDataManager.unregisterWeatherInfoDataListener(this);
        mDashboardDataManager.removeAccountChangeListener(this);
        mDashboardDataManager.unregisterDashboardPmsStateChangeListener(this);
        mDashboardDataManager.unregisterAppCountryFilterStateChangeListener(this);
        mDashboardDataManager.unregisterBillSettingsListener(this);
        mDashboardDataManager.unregisterMessageSettingsListener(this);
    }

    private boolean isLaunchedInConfigMode() {
        Intent intent = getActivity().getIntent();
        if (Constants.INTENT_ACTION_MAIN.equals(intent.getAction())) {
            DdbLogUtility.logCommon(TAG, "isLaunchedInConfigMode() called");
            Set<String> categories = intent.getCategories();
            if (categories != null && categories.contains(Constants.INTENT_CATEGORY_PROFFESIONAL_DISPLAY_CONFIGURATION)) {
                return intent.getBooleanExtra(Constants.EXTRA_CONFIG_MODE, false);
            }
        }
        return false;
    }

    private boolean isLaunchedInGuestMode() {
        DdbLogUtility.logCommon(TAG, "isLaunchedInGuestMode() called");
        Intent intent = getActivity().getIntent();
        return Constants.INTENT_ACTION_GUEST_MENU.equals(intent.getAction());
    }

    private boolean isLaunchedInAppsMode() {
        DdbLogUtility.logCommon(TAG, "isLaunchedInAppsMode() called");
        Intent intent = getActivity().getIntent();
        return Constants.INTENT_ACTION_APPS.equals(intent.getAction());
    }

    private void launchFragment(int position) {
        setSelectionToChapter(position);
    }

    private void launchAppsFragment() {
        if (isAppsChapterAvailable()) {
            int currentChapterPosition = getSelectedPosition();
            int targetPosition = getAppsChapterPosition();
            if (currentChapterPosition != targetPosition) { // If Apps chapter is already launched, re-launch is not necessary
                launchFragment(targetPosition);
            }
        } else {
            TvToastMessenger tvToastMessenger = TvToastMessenger.getInstance(getActivity().getApplicationContext());
            TvToast noAppsAvailableToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
                    getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_APP_APPS_NOT_AVAILABLE), -1);

            tvToastMessenger.showTvToastMessage(noAppsAvailableToast);
        }
    }


    private void launchMoreChapterFragmentInConfigMode() {
        DdbLogUtility.logCommon(TAG, "launchMoreChapterFragmentInConfigMode() called");
        int currentChapterPosition = getSelectedPosition();
        int targetPosition = getMoreChapterPosition();
        if(currentChapterPosition !=targetPosition){ // If More chapter is already launched, re-launch is not necessary
            launchFragment(targetPosition);
        }
        EditDashboardMenu editDashboardMenu = new EditDashboardMenu(getContext());
        editDashboardMenu.show();
        mDashboardDataManager.hideSidePanel();
    }

    private void launchMoreChapterFragment() {
        DdbLogUtility.logCommon(TAG, "launchMoreChapterFragment() called");
        int currentChapterPosition = getSelectedPosition();
        int targetPosition = getMoreChapterPosition();
        if (currentChapterPosition != targetPosition) { // If More chapter is already launched, re-launch is not necessary
            launchFragment(targetPosition);
        }
    }

    private void onAppsCountryChanged(int newCountryCode) {
        Log.d(TAG, "#### enter onAppsCountryChanged()");
        updateAppsChapterVisibility();
        Log.d(TAG, "#### exit onAppsCountryChanged()");
    }

    private void billSettingChanged() {
        if (!mDashboardDataManager.isBillAvailable()) {
            requestFocusToMainFragment();
        }
    }

    private void messageSettingChanged() {
        if (!mDashboardDataManager.isMessageAvailable()) {
            requestFocusToMainFragment();
        }
    }

    private void requestFocusToMainFragment() {
        View menuLanguageView = mBrowseFrameLayout.findViewById(R.id.top_menu_language_view);
        View view = getView();
        if (mIsFragmentStarted && null != view && null == view.findFocus() && null != menuLanguageView) {
            menuLanguageView.requestFocus();
        }
    }

    @Override
    public void onAppsCountryFilterStateChanged() {
        Log.d(TAG, "#### enter onAppsCountryFilterStateChanged()");
        updateAppsChapterVisibility();
        Log.d(TAG, "#### exit onAppsCountryFilterStateChanged()");
    }

    //TODO: A very bad workaround! Remove this when we move to android jetpack library from support library
    // This is done to get a reference to 'mSetSelectionRunnable' which is private final field in the parent class
    // Inside onStop(), removeCallbacks() is called on 'mSetSelectionRunnable' to prevent a crash due to pending fragment transaction commits happening
    // after onStop().
    private Runnable getSetSelectionRunnableByReflection() {
        Runnable runnable = null;
        try {
            Field setSelectionRunnableField = BrowseSupportFragment.class.getDeclaredField("mSetSelectionRunnable");
            setSelectionRunnableField.setAccessible(true);
            runnable = (Runnable) setSelectionRunnableField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.d(TAG, "#### could not find field mSetSelectionRunnable");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return runnable;
    }

    public void checkLaunchMode() {
        if (isLaunchedInConfigMode()) {
            launchMoreChapterFragmentInConfigMode();
        } else if (isLaunchedInAppsMode()) {
            launchAppsFragment();
        } else if (isLaunchedInGuestMode()) {
            launchMoreChapterFragment();
        }
    }

    @Override
    public void onPbsSettingCountryChange(int country) {
        Log.d(TAG, "#### enter onPbsSettingCountryChange().country:" + country);
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_APPS_COUNTRY_CHANGED, country, 0);
        message.sendToTarget();
        Log.d(TAG, "#### exit onPbsSettingCountryChange()");
    }

    @Override
    public void onDashboardPmsStateChange() {
        Log.d(TAG, "#### enter onDashboardPmsStateChange()");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_ACCOUNT_CHANGED);
        message.sendToTarget();
        Log.d(TAG, "#### exit onDashboardPmsStateChange()");
    }

    @Override
    public void onDashboardPmsStateChange(int value) {
        Log.d(TAG, "#### enter onDashboardPmsStateChangeWithValue()");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_ACCOUNT_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
        Log.d(TAG, "#### exit onDashboardPmsStateChangeWithValue()");
    }

    @Override
    public void onGuestNameChanged() {
        Log.d(TAG, "#### enter onGuestNameChanged()");
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_ACCOUNT_CHANGED);
        message.sendToTarget();
        Log.d(TAG, "#### exit onGuestNameChanged()");
    }

    @Override
    public void onBillSettingsChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_BILL_CHANGED);
        message.sendToTarget();
    }

    @Override
    public void onBillSettingsChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_BILL_CHANGED);
        message.sendToTarget();
    }

    @Override
    public void onMessageSettingsChanged() {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_MESSAGE_CHANGED);
        message.sendToTarget();
    }

    @Override
    public void onMessageSettingsChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_MESSAGE_CHANGED);
        message.sendToTarget();
    }

    private static class PageRowFragmentFactory extends BrowseSupportFragment.FragmentFactory {

        PageRowFragmentFactory() {

        }

        @Override
        public Fragment createFragment(Object rowObj) {
            Row row = (Row) rowObj;

            if (row.getHeaderItem().getId() == HEADER_ID_RECOMMENDED) {
                return new RecommendedChapterFragment();
            }

            if (row.getHeaderItem().getId() == HEADER_ID_TV_CHANNELS) {
                return new TvChannelsChapterFragment();
            }

            if (row.getHeaderItem().getId() == HEADER_ID_VIDEO_ON_DEMAND) {
                return new VideoOnDemandChapterFragment();
            }

            if (row.getHeaderItem().getId() == HEADER_ID_CAST) {
                return new CastChapterFragment();
            }

            if (row.getHeaderItem().getId() == HEADER_ID_APPS) {
                return new AppsChapterFragment();
            }

            if (row.getHeaderItem().getId() == HEADER_ID_GAMES) {
                return new GamesChapterFragment();
            }

            if (row.getHeaderItem().getId() == HEADER_ID_MORE) {
                return new MoreChapterFragment();
            }

            throw new IllegalArgumentException(String.format("Invalid row %s", rowObj));
        }
    }

    @Override
    public void changeSidePanelBackgroundColor(int color) {
        setBrandColor(color);
    }

    @Override
    public void showSidePanel() {
        startHeadersTransition(true);
    }

    @Override
    public void hideSidePanel() {
        startHeadersTransition(false);
    }

    @Override
    public void changeBackgroundImage(Bitmap bitmap) {
        BitmapDrawable maiBackgroundBitmapDrawble = new BitmapDrawable(getResources(), bitmap);
        DashboardDataManager.getInstance().saveBackgroundImage(maiBackgroundBitmapDrawble);
        updateBackground(maiBackgroundBitmapDrawble);
    }

    @Override
    public void changeBackgroundImage(int drawableResourceId) {
        updateBackground(drawableResourceId);
    }

    @Override
    public void changeCastBackgroundImage(final Bitmap bitmap) {
        if (bitmap != null) {
            BitmapDrawable castBackgroundBitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            DashboardDataManager.getInstance().saveCastBackgroundImage(castBackgroundBitmapDrawable);
            applyCastBackgroundImage(castBackgroundBitmapDrawable);
        }
    }

    @Override
    public void changeBackgroundColorFilter(int color) {
        updateBackgroundColor(color);
    }

    @Override
    public void clearBackground() {
        clearBackgroundDrawable();
    }

    @Override
    public void changeHotelLogo(Bitmap bitmap) {
        DashboardDataManager.getInstance().saveHotelLogoBitmap(bitmap);
        updateHotelLogoDelayed(bitmap);
    }

    @Override
    public void changeHotelLogo(int drawableResourceId) {
        updateHotelLogoDelayed(drawableResourceId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        super.onDestroy();
        unRegisterMessageCountBroadcastReceiver();
    }

    public void hideTopMenuItems() {
        getTitleViewAdapter().updateComponentsVisibility(TOP_MENU_ITEMS_INVISIBLE);
    }

    public void showTopMenuItems() {
        getTitleViewAdapter().updateComponentsVisibility(TOP_MENU_ITEMS_VISIBLE);
    }

    private int getRecommendedChapterPosition() {
        return RECOMMENDED_CHAPTER_POSITION;
    }

    private int getTvChannelsChapterPosition() {
        int position = TV_CHANNELS_CHAPTER_POSITION;

        if (!isRecommendedChapterAvailable()) {
            position--;
        }

        return position;
    }

    private int getVideoOnDemandChapterPosition() {
        int position = VOD_CHAPTER_POSITION;

        if (!isRecommendedChapterAvailable()) {
            position--;
        }

        if (!isTvChannelChapterAvailable()) {
            position--;
        }

        return position;
    }

    private int getCastChapterPosition() {
        int position = CAST_CHAPTER_POSITION;

        if (!isRecommendedChapterAvailable()) {
            position--;
        }

        if (!isTvChannelChapterAvailable()) {
            position--;
        }

        if (!isVideoOnDemandChapterAvailable()) {
            position--;
        }

        return position;
    }

    private int getAppsChapterPosition() {
        int position = APPS_CHAPTER_POSITION;

        if (!isRecommendedChapterAvailable()) {
            position--;
        }

        if (!isTvChannelChapterAvailable()) {
            position--;
        }

        if (!isVideoOnDemandChapterAvailable()) {
            position--;
        }

        if (!isCastChapterAvailable()) {
            position--;
        }

        return position;
    }

    private int getGamesChapterPosition() {
        int position = GAMES_CHAPTER_POSITION;

        if (!isRecommendedChapterAvailable()) {
            position--;
        }

        if (!isTvChannelChapterAvailable()) {
            position--;
        }

        if (!isVideoOnDemandChapterAvailable()) {
            position--;
        }

        if (!isCastChapterAvailable()) {
            position--;
        }

        if (!isAppsChapterAvailable()) {
            position--;
        }

        return position;
    }

    private int getMoreChapterPosition() {
        int position = MORE_CHAPTER_POSITION;

        if (!isRecommendedChapterAvailable()) {
            position--;
        }

        if (!isTvChannelChapterAvailable()) {
            position--;
        }

        if (!isVideoOnDemandChapterAvailable()) {
            position--;
        }

        if (!isCastChapterAvailable()) {
            position--;
        }

        if (!isAppsChapterAvailable()) {
            position--;
        }

        if (!isGamesChapterAvailable()) {
            position--;
        }

        return position;
    }

    private boolean isRecommendedChapterAvailable() {
        boolean available = mDashboardDataManager.areRecommendedAppsAvailable()|| mDashboardDataManager.isSmartInfoAvailable() || mDashboardDataManager.areAppRecommendationsAvailable();
        Log.d(TAG, "isRecommendedChapterAvailable() returned: " + available);
        return available;
    }

    private boolean isTvChannelChapterAvailable() {
        return mDashboardDataManager.areChannelsEnabled();
    }

    private boolean isVideoOnDemandChapterAvailable() {
        if(mDashboardDataManager.isSmartTvModeOff()) {
            return false;
        }
        return mDashboardDataManager.areVodRecommendationsAvailable() || mDashboardDataManager.areVodPreviewRecommendationsAvailable();
    }

    private boolean isCastChapterAvailable() {
        return mDashboardDataManager.isGoogleCastEnabled();
    }

    private boolean isAppsChapterAvailable() {
       if(mDashboardDataManager.isSmartTvModeOff()) {
           return false;
       }else{
           List<AppInfo> apps = mDashboardDataManager.getEnabledAppList();
           return  (apps != null && !apps.isEmpty());
       }
    }

    private boolean isGamesChapterAvailable() {
        if(mDashboardDataManager.isSmartTvModeOff()) {
            return false;
        }
        return mDashboardDataManager.areGameRecommendationsAvailable() || mDashboardDataManager.areGameAppsAvailable() || mDashboardDataManager.arePreviewChannelGamesAvailable();
    }

    private boolean isMoreChapterAvailable() {
        return true;
    }

    private void registerSourcesChangeListener() {
        mDashboardDataManager.registerSourceDataListener(this);
    }

    private void unregisterSourcesChangeListener() {
        mDashboardDataManager.unregisterSourceDataListener(this);
    }

    private void registerMessageCountBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_NAU_MESSAGE_COUNTER_CHANGE);
        DashboardDataManager.getInstance().getContext().getApplicationContext().registerReceiverAsUser(mMessageCountChangeReceiver, UserHandle.CURRENT_OR_SELF,intentFilter, null, null);
    }

    private void unRegisterMessageCountBroadcastReceiver() {
        DashboardDataManager.getInstance().getContext().getApplicationContext().unregisterReceiver(mMessageCountChangeReceiver);
    }

    protected void updateBackground(Bitmap bitmap) {
        updateBackgroundDelayed(bitmap);
    }

    protected void updateBackground(int drawableResourceId) {
        updateBackgroundDelayed(drawableResourceId);
    }

    protected void updateBackground(Drawable drawable) {
        updateBackgroundDelayed(drawable);
    }

    protected void updateBackgroundColor(int color) {
        mMainBackgroundColorFilter = color;
        Drawable backgroundDrawable = getView().getBackground();
        if (backgroundDrawable instanceof BitmapDrawable) {
            backgroundDrawable.setColorFilter(color, PorterDuff.Mode.SRC_OVER);
        } else if (backgroundDrawable instanceof ColorDrawable) {
            getView().setBackgroundColor(color);
        }
    }

    private void clearBackgroundDrawable() {
        getView().setBackgroundColor(mMainBackgroundColorFilter);
    }

    private void applyMainBackground() {
        boolean mainBackgroundEnabled = mDashboardDataManager.isMainBackgroundEnabled();
        if (!mainBackgroundEnabled) {
            clearBackground();
            return;
        }
        // Apply an inital background color which will immediately be shown when the Activity shows up
        // This is to get around when the background image takes longer to get decoded and be shown
        applyInitialBackgroundColor();
        BitmapDrawable mainBackgroundBitmapDrawable = DashboardDataManager.getInstance().getSavedBitMapDrawable();
        if(isValidBitmap(mainBackgroundBitmapDrawable)){
            Log.d(TAG, "applyMainBackground: valid main backgroind saved bitmap");
            updateBackground(mainBackgroundBitmapDrawable);
        }else {
            Log.d(TAG, "applyMainBackground: invalid saved bitmapDrawable, fetch again");
            mDashboardDataManager.applyMainBackground();
        }
    }

    private boolean isValidBitmap(BitmapDrawable mainBackgroundBitmapDrawable) {
        return mainBackgroundBitmapDrawable != null && mainBackgroundBitmapDrawable.getBitmap() != null && (!mainBackgroundBitmapDrawable.getBitmap().isRecycled());
    }

    private void applyHotelLogo() {
        Bitmap hotelLogoBitmap = DashboardDataManager.getInstance().getHotelLogoBitmap();
        if(isValidBitmap(hotelLogoBitmap)){
            Log.d(TAG, "applyHotelLogo: valid saved hotel logo");   
            updateHotelLogoDelayed(hotelLogoBitmap);
        }else {
            Log.d(TAG, "applyHotelLogo: invalid saved bitmap, fetch again");
            mDashboardDataManager.applyHotelLogo();
        }
    }
    
    private boolean isValidBitmap(Bitmap b){
        return b != null && (!b.isRecycled());
    }

    private void loadCastBackground() {
        BitmapDrawable castBackgroundBitmapDrawable = DashboardDataManager.getInstance().getSavedCastBitMapDrawable();
        if(isValidBitmap(castBackgroundBitmapDrawable)){
            Log.d(TAG, "loadCastBackground: valid bitmap");
            applyCastBackgroundImage(castBackgroundBitmapDrawable);
        }else {
            Log.d(TAG, "loadCastBackground: invalid cast bitmapDrawable, fetch again");
            mDashboardDataManager.loadSavedCastBacktound();
        }
    }
    
    private void applyInitialBackgroundColor() {
        int color = getContext().getColor(R.color.dashboard_initial_background_color);
        getView().setBackgroundColor(color);
    }

    private void updateBackgroundDelayed(final Bitmap bitmap) {
        mBackgroundImageUpdateHandler.removeCallbacksAndMessages(null);
        mBackgroundImageUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                    bitmapDrawable.setColorFilter(mMainBackgroundColorFilter, PorterDuff.Mode.SRC_OVER);
                    View v = getView();
                    if (v != null) {
                        if(!mDashboardDataManager.getCastChapterFragment()) {
                           v.setBackground(bitmapDrawable);
                        }
                    }
                  }
                }
        }, BACKGROUND_IMAGE_UPDATE_DELAY_MS);
    }

    private void updateBackgroundDelayed(final int drawableResourceId) {
        mBackgroundImageUpdateHandler.removeCallbacksAndMessages(null);
        mBackgroundImageUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = DashboardDataManager.getInstance().getContext().getDrawable(drawableResourceId);
                drawable.setColorFilter(mMainBackgroundColorFilter, PorterDuff.Mode.SRC_OVER);
                View v = getView();
                if (v != null) {
                    v.setBackground(drawable);
                }
            }
        }, BACKGROUND_IMAGE_UPDATE_DELAY_MS);
    }

    private void updateBackgroundDelayed(final Drawable drawable) {
        mBackgroundImageUpdateHandler.removeCallbacksAndMessages(null);
        mBackgroundImageUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawable.setColorFilter(mMainBackgroundColorFilter, PorterDuff.Mode.SRC_OVER);
                View v = getView();
                if (v != null) {
                    v.setBackground(drawable);
                }
            }
        }, BACKGROUND_IMAGE_UPDATE_DELAY_MS);
    }

    private void updateHotelLogoDelayed(final Bitmap bitmap) {
        mHotelLogoImageUpdateHandler.removeCallbacksAndMessages(null);
        mHotelLogoImageUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DashboardHeadersFragment headersFragment = (DashboardHeadersFragment) getHeadersSupportFragment();
                if (headersFragment != null) {
                    headersFragment.setHotelLogo(bitmap);
                }
            }
        }, HOTEL_LOGO_IMAGE_UPDATE_DELAY_MS);
    }

    private void updateHotelLogoDelayed(final int drawableResourceId) {
        mHotelLogoImageUpdateHandler.removeCallbacksAndMessages(null);
        mHotelLogoImageUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DashboardHeadersFragment headersFragment = (DashboardHeadersFragment) getHeadersSupportFragment();
                if (headersFragment != null) {
                    headersFragment.setHotelLogo(drawableResourceId);
                }
            }
        }, HOTEL_LOGO_IMAGE_UPDATE_DELAY_MS);
    }

    private void initForegroundOverlayAnimators() {
        initAnimatorListener();
        initApplyForegroundOverlayAnimator();
        initRemoveForegroundOverlayAnimator();
    }

    private void initAnimatorListener() {
        mForegroundOverlayAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mForegroundOverlay.setTint((int) animation.getAnimatedValue());
            }
        };
    }

    private void initApplyForegroundOverlayAnimator() {
        int colorFrom = DashboardDataManager.getInstance().getContext().getColor(R.color.transparent_overlay_color);
        int colorTo = DashboardDataManager.getInstance().getContext().getColor(R.color.overlay_color);
        mApplyForegroundOverlayAnimator = createOverlayAnimator(colorFrom, colorTo, OVERLAY_ANIMATION_DURATION);
    }

    private void initRemoveForegroundOverlayAnimator() {
        int colorFrom = DashboardDataManager.getInstance().getContext().getColor(R.color.overlay_color);
        int colorTo = DashboardDataManager.getInstance().getContext().getColor(R.color.transparent_overlay_color);
        mRemoveForegroundOverlayAnimator = createOverlayAnimator(colorFrom, colorTo, OVERLAY_ANIMATION_DURATION);
    }

    private ValueAnimator createOverlayAnimator(int colorFrom, int colorTo, long duration) {
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimator.setDuration(duration);
        colorAnimator.addUpdateListener(mForegroundOverlayAnimatorListener);
        return colorAnimator;
    }

    private void applyForegroundOverlay(View view) {
        mForegroundOverlay = new ColorDrawable(DashboardDataManager.getInstance().getContext().getColor(R.color.transparent_overlay_color));
        mForegroundOverlay.setTint(DashboardDataManager.getInstance().getContext().getColor(R.color.overlay_color));
        mForegroundOverlay.setTintMode(PorterDuff.Mode.SRC_OVER);
        view.setForeground(mForegroundOverlay);
    }

    private void animateApplyOverlay() {
        mApplyForegroundOverlayAnimator.start();
    }

    private void animateRemoveOverlay() {
        mRemoveForegroundOverlayAnimator.start();
    }

    private void checkAndFetchChannels() {
        if (mDashboardDataManager.areChannelsEnabled()) {
            ChannelFilter activeChannelFilter = mDashboardDataManager.getActiveChannelFilter();
            if (activeChannelFilter == null || !activeChannelFilter.hasChannels()) {
                mDashboardDataManager.fetchActiveChannelFilter();
            }

            List<ChannelFilter> availableChannelFilters = mDashboardDataManager.getAvailableChannelFilters();
            if (availableChannelFilters == null || availableChannelFilters.isEmpty()) {
                mDashboardDataManager.fetchAvailableChannelFilters();
            }
        }
    }

    private void checkAndResetProfileAccessPref() {
        if (mDefaultSharedPreferences.contains(Constants.PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION)) {
            if (mDashboardDataManager.getGoogleAccountCount() == 0) {
                mDefaultSharedPreferences.edit().remove(Constants.PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION).apply();
            }
        }
    }

    private void checkAndFetchApps() {
        List<AppInfo> apps = mDashboardDataManager.getEnabledAppList();
        if (apps == null || apps.isEmpty()) {
            mDashboardDataManager.fetchEnabledAppList();
        }
    }

    private static class UiThreadHandler extends Handler {
        WeakReference<DashboardFragment> mDashboardFragmentRef;

        private static final int MSG_WHAT_ON_ACCOUNT_CHANGED = 1;
        private static final int MSG_WHAT_WEATHER_INFO_DATA_RECEIVED = 2;
        private static final int MSG_WHAT_APPS_COUNTRY_CHANGED = 3;
        private static final int MSG_WHAT_ON_BILL_CHANGED = 4;
        private static final int MSG_WHAT_ON_MESSAGE_CHANGED = 5;
        private static final int MSG_WHAT_ON_VOD_PREVIEW_CHANNEL_AVAILABLE = 6;
        private static final int MSG_WHAT_ON_GAMES_PREVIEW_CHANNEL_AVAILABLE = 7;
        private static final int MSG_WHAT_START_GOOGLE_SIGN_IN = 8;
        private static final int MSG_WHAT_HANDLE_HEADER_ITEM_SELECTED = 9;

        private UiThreadHandler(DashboardFragment dashboardFragment) {
            super();
            mDashboardFragmentRef = new WeakReference<DashboardFragment>(dashboardFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_WHAT_WEATHER_INFO_DATA_RECEIVED) {
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    dashboardFragment.setPremisesName();
                }
                return;
            }

            if (what == MSG_WHAT_ON_ACCOUNT_CHANGED) {
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    if (DashboardDataManager.getInstance().getGoogleAccountCount() > 0) {
                        dashboardFragment.updateGuestName();
                    } else {
                        String guestName = DashboardDataManager.getInstance().getGuestNameFromPms();
                        dashboardFragment.updateGuestName(guestName);
                        // Reset the profile access permission because no accounts are present
                        dashboardFragment.mDashboardDataManager.resetGoogleAccountPreferences();
                    }
                }
                return;
            }

            if (what == MSG_WHAT_APPS_COUNTRY_CHANGED) {
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    int newCountryCode = msg.arg1;
                    dashboardFragment.onAppsCountryChanged(newCountryCode);
                }
                return;
            }

            if (what == MSG_WHAT_ON_BILL_CHANGED) {
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    dashboardFragment.billSettingChanged();
                }
                return;
            }
            if (what == MSG_WHAT_ON_MESSAGE_CHANGED) {
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    dashboardFragment.messageSettingChanged();
                }
                return;
            }

            if(what == MSG_WHAT_ON_VOD_PREVIEW_CHANNEL_AVAILABLE){
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    dashboardFragment.updateVideoOnDemandChapterVisibility();
                }
                return;
            }

            if(what == MSG_WHAT_ON_GAMES_PREVIEW_CHANNEL_AVAILABLE){
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null) {
                    dashboardFragment.updateGamesChapterVisibility();
                }
                return;
            }

            if(what == MSG_WHAT_START_GOOGLE_SIGN_IN){
                Log.d(TAG, "handleMessage: MSG_WHAT_START_GOOGLE_SIGN_IN after2min");
                DashboardFragment dashboardFragment = mDashboardFragmentRef.get();
                if (dashboardFragment != null && dashboardFragment.isAdded()) {
                    DashboardDataManager.getInstance().startGoogleAccountFlow();
                }
                return;
            }
			if(what == MSG_WHAT_HANDLE_HEADER_ITEM_SELECTED){
				DashboardFragment dashboardFragment = mDashboardFragmentRef.get();				
				if (dashboardFragment != null && dashboardFragment.isAdded()) {
					Log.d(TAG, "Set Selected position to " + msg.arg1);
                    dashboardFragment.setSelectedPosition(msg.arg1, true);
                }
                return;
			}
        }
    }

    @Override
    public void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannels) {
        updateChapterVisibility(previewProgramsChannels);
    }

    @Override
    public void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannels) {
        updateChapterVisibility(previewProgramsChannels);
    }

    @Override
    public void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels){
        updateChapterVisibility(previewProgramsChannels);
    }

    private void updateChapterVisibility(PreviewProgramsChannel previewProgramsChannels){
        if(previewProgramsChannels.getCategory() == Constants.CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION){
            android.util.Log.d(TAG, "updateChapterVisibility: VOD");
            Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_VOD_PREVIEW_CHANNEL_AVAILABLE);
            message.sendToTarget();
        }else if(previewProgramsChannels.getCategory() == Constants.CHANNEL_CONTENT_TYPE_GAMES_RECOMMENDATION){
            android.util.Log.d(TAG, "updateChapterVisibility: GAMES");
            Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_ON_GAMES_PREVIEW_CHANNEL_AVAILABLE);
            message.sendToTarget();
        }
    }

}

