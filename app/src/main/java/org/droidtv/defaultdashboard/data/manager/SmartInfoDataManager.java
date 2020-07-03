package org.droidtv.defaultdashboard.data.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.philips.professionaldisplaysolutions.jedi.jedifactory.JEDIFactory;
import com.philips.professionaldisplaysolutions.jedi.professionalsettings.IGuestPreference;

import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SmartInfoAppDataFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SmartInfoFileParseListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SmartInfoListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ThumbnailBitmapFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SmartInfoPreviewProgramListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.droidtv.defaultdashboard.data.query.HtvAppQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.util.AppUtil;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.SmartInfoXmlParser;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.htv.provider.HtvContract;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by sandeep.kumar on 06/02/2018.
 */

final class SmartInfoDataManager extends ContextualObject implements SmartInfoAppDataFetchListener, SmartInfoFileParseListener, RecommendationListener, SmartInfoPreviewProgramListener {
    private static String TAG = "SmartInfoDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private SmartInfoImageCache mSmartInfoImageCache;
    private String mSmartInfoFileLocation;
    private ArrayList<WeakReference<SmartInfoListener>> mSmartInfoListenerRefs;
    private List<Recommendation> mSmartInfoRecommendations;
    private List<SmartInfo> mSmartInfoData;
    private ITvSettingsManager mTvSettingsManager;
    private SmartInfoDownloadCompleteReceiver mSmartInfoDownloadCompleteReceiver;
    private String mSmartInfoTitle;
    private Drawable mSmartInfoIcon;
    private String mSmartInfoDescription;
    private String mSmartInfoMainUrl;
	private String mSelectedSmartinfoAppPackageName;
    private int mSmartInfoMode;
	private int mSmartInfoBrowserSource;
	private boolean mIsSmartInfoAppPreviewProgramBased;
	private PreviewProgramsChannel mSmartInfoPreviewChannel;
    private static String smartInfoUSBPath = null;

    SmartInfoDataManager(Context context) {
        super(context);

        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.SMART_INFO_THREAD_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler(this);
        mSmartInfoImageCache = new SmartInfoImageCache();
        mSmartInfoListenerRefs = new ArrayList<>();
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();

        updateSmartInfoMode();
        registerForSmartInfoDownloadCompleteBroadcast();
        registerForSmartInfoRecommendationChanges();
		registerUsbEventsReceiver();
    }

    boolean addSmartInfoListener(SmartInfoListener smartInfoListener) {
        if (smartInfoListener == null) {
            return false;
        }
        return mSmartInfoListenerRefs.add(new WeakReference<SmartInfoListener>(smartInfoListener));
    }

    boolean removeSmartInfoListener(SmartInfoListener smartInfoListener) {
        if (mSmartInfoListenerRefs == null) {
            return false;
        }
        for (int i = 0; i < mSmartInfoListenerRefs.size(); i++) {
            WeakReference<SmartInfoListener> ref = mSmartInfoListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            SmartInfoListener listener = ref.get();
            if (listener != null && listener.equals(smartInfoListener)) {
                return mSmartInfoListenerRefs.remove(ref);
            }
        }
        return false;
    }

    @Override
    public void onSmartInfoAppDataFetched(String packageName, String title, Drawable icon) {
        if (!TextUtils.isEmpty(packageName)) {
            if (mSmartInfoData != null) {
                mSmartInfoData.clear();
            }
			mSelectedSmartinfoAppPackageName = packageName;
			mSmartInfoTitle = title;
            mSmartInfoIcon = icon;
			if(AppUtil.isPreviewChannelApp(getContext(), packageName)){
				mIsSmartInfoAppPreviewProgramBased = true;
				registerSmartInfoPreviewProgramListener();
				DashboardDataManager.getInstance().fetchSmartInfoPreviewProgram(packageName);
			}else{
				// TODO: Smart info recommendations should also be filtered by pacakge name
				// so that only those recommendations are displayed which belong to the package name and are of smart info type
				mIsSmartInfoAppPreviewProgramBased = false;
				mSmartInfoRecommendations = DashboardDataManager.getInstance().getRecommendations(RecommendationHelper.Category.SMARTINFO, packageName);
				if(mSmartInfoRecommendations == null){
					notifySmartInfoUnavailable();
					return;
				}else if (mSmartInfoRecommendations.isEmpty()) {
					Recommendation emptyAppRecommendation = createEmptyAppRecommendation(packageName, null, title);
					if (emptyAppRecommendation == null) {
						// Not able to create even an empty recommendation! Return.
						notifySmartInfoUnavailable();
						return;
					}
					if(mSmartInfoRecommendations == null) mSmartInfoRecommendations = new ArrayList<>(1);
					mSmartInfoRecommendations.add(emptyAppRecommendation);
					
				}
				notifySmartInfoRecommendationsAvailable();
			}
            
            
        } else {
            notifySmartInfoUnavailable();
        }
    }

    @Override
    public void onSmartInfoFileParseComplete(List<SmartInfo> smartInfoData, String title, Drawable icon, String smartInfoDescription, String smartInfoMainUrl) {
        if (smartInfoData != null && !smartInfoData.isEmpty()) {
            if (mSmartInfoRecommendations != null) {
                mSmartInfoRecommendations.clear();
            }
            mSmartInfoData = smartInfoData;
            mSmartInfoTitle = title;
            mSmartInfoIcon = icon;
            mSmartInfoDescription = smartInfoDescription;
            mSmartInfoMainUrl = smartInfoMainUrl;
            notifySmartInfoDataAvailable();
        } else {
            notifySmartInfoUnavailable();
        }
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        // We are only bothered about recommendations of type SmartInfo
		
        if (!isSmartInfoRecommendation(recommendation)) {
            return;
        }

        // If this is a new recommendation, simply add it to the list
        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            removeEmptyRecommendation();
            if (mSmartInfoRecommendations == null) {
                mSmartInfoRecommendations = new ArrayList<>();
            }
            mSmartInfoRecommendations.add(recommendation);
            return;
        }

        // For recommendations to be cancelled, simply remove the notification from the list
        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            for (int i = 0; mSmartInfoRecommendations != null && i < mSmartInfoRecommendations.size(); i++) {
                Recommendation recommendationItem = mSmartInfoRecommendations.get(i);
                if (recommendation.getKey().equals(recommendationItem.getKey())) {
                    mSmartInfoRecommendations.remove(i);
                    return;
                }
            }
        }

        // Update if there is an existing recommendation with the same id. Otherwise, add this recommendation to the list
        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            for (int i = 0; mSmartInfoRecommendations != null && i < mSmartInfoRecommendations.size(); i++) {
                Recommendation recommendationItem = (Recommendation) mSmartInfoRecommendations.get(i);
                if (recommendation.getKey().equals(recommendationItem.getKey())) {
                    mSmartInfoRecommendations.remove(i);
                    mSmartInfoRecommendations.add(i, recommendation);
                    return;
                }
            }

            removeEmptyRecommendation();
            if (mSmartInfoRecommendations == null) {
                mSmartInfoRecommendations = new ArrayList<>();
            }
            mSmartInfoRecommendations.add(recommendation);
            return;
        }
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        if (isSmartInfoModeApp() && !mIsSmartInfoAppPreviewProgramBased && recommendationCategory == RecommendationHelper.Category.SMARTINFO) {
            fetchSmartInfo();
        }
    }
	
	@Override
	public void onSmartinfoPreviewProgramFetchComplete(List<PreviewProgramsChannel> SmartInfoPreviewChannelList){
		Log.d("Sup", "on onSmartinfoPreviewProgramFetchComplete");
		if(SmartInfoPreviewChannelList == null || SmartInfoPreviewChannelList.isEmpty()){
			mSmartInfoPreviewChannel = createDummyPreviewChannel();
		}else{
			if(mSmartInfoPreviewChannel == null || isDummyPreviewChannel(mSmartInfoPreviewChannel) || !SmartInfoPreviewChannelList.contains(mSmartInfoPreviewChannel)){
				//Only one smart info channel is supported. If there are many pick the first one. 
				mSmartInfoPreviewChannel = SmartInfoPreviewChannelList.get(0);
			}else{
				Log.d(TAG, "mSmartInfoPreviewChannel is already updated with correct channel");
			}
		}
		
		if(mSmartInfoPreviewChannel != null){
			notifySmartInfoPreviewProgramsAvailable();
		}		
	}
	
	private PreviewProgramsChannel createDummyPreviewChannel(){
		PreviewProgramsChannel dummyPreviewChannel = new PreviewProgramsChannel();
        dummyPreviewChannel.setCategory(Constants.CHANNEL_CONTENT_TYPE_EMPTY_RECOMMENDATION);
        
        DdbLogUtility.logRecommendationChapter(TAG, "createEmptyRecommendations packageName " + mSelectedSmartinfoAppPackageName);
        List<Recommendation> emptyRecommendations = new ArrayList<>(1);
        dummyPreviewChannel.setPackageName(mSelectedSmartinfoAppPackageName);

        Recommendation emptyRecommendation = createEmptyRecommendation(mSelectedSmartinfoAppPackageName);
        emptyRecommendations.add(emptyRecommendation);

        dummyPreviewChannel.setPreviewProgramList(emptyRecommendations);
        return dummyPreviewChannel;
	}
	
	private Recommendation createEmptyRecommendation(String packageName) {
        Recommendation emptyRecommendation = new Recommendation();
        emptyRecommendation.setId(0);
        emptyRecommendation.setLogo(null);
        emptyRecommendation.setTitle(mSmartInfoTitle);
        emptyRecommendation.setContentType(new String[]{Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION});
		
		Intent launchIntent = new Intent();
        launchIntent.setPackage(packageName);
        launchIntent.addCategory("com.philips.professionaldisplaysolutions.jedi.intent.category.SMART_INFO");
        launchIntent.putExtra("com.philips.professionaldisplaysolutions.jedi.LAUNCH_SMART_INFO", true);
        PendingIntent pendingIntent = PendingIntent.getService(getContext(), 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        emptyRecommendation.setPendingIntent(pendingIntent);
        return emptyRecommendation;
    }
	
	private boolean isDummyPreviewChannel(PreviewProgramsChannel channel){
		return channel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_EMPTY_RECOMMENDATION;
	}

    List<Recommendation> getSmartInfoRecommendations() {
        return mSmartInfoRecommendations;
    }

    PreviewProgramsChannel getSmartInfoPreviewChannel(){
        return mSmartInfoPreviewChannel;
    }

    List<SmartInfo> getSmartInfoData() {
        return mSmartInfoData;
    }

    void clearSmartInfoData() {
        if(mSmartInfoData != null) {
            mSmartInfoData.clear();
        }
    }

    String getSmartInfoTitle() {
        return mSmartInfoTitle;
    }

    Drawable getSmartInfoIcon() {
        return mSmartInfoIcon;
    }

    boolean isSmartInfoEnabled() {
        return getSmartInfoMode() != TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_OFF &&
                !DashboardDataManager.getInstance().isDashboardModeCustom();
    }

    boolean isSmartInfoAvailable() {
        Log.d(TAG, "isSmartInfoAvailable mSmartInfoData " + mSmartInfoData );
        Log.d(TAG, "isSmartInfoAvailable mSmartInfoRecommendations " + mSmartInfoRecommendations );

        return isSmartInfoEnabled() &&
                ((mSmartInfoData != null && !mSmartInfoData.isEmpty()) ||
                        (mSmartInfoRecommendations != null && !mSmartInfoRecommendations.isEmpty()) || (mSmartInfoPreviewChannel != null));
    }

    boolean isSmartInfoModeBrowser() {
        return getSmartInfoMode() == TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_BROWER;
    }

    boolean isSmartInfoBrowserSourceServer() {
        return getSmartInfoBrowserSource() == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_SERVER;
    }

    boolean isSmartInfoBrowserSourceLocal() {
        return getSmartInfoBrowserSource() == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_LOCAL;
    }

    boolean isSmartInfoBrowserSourceUSB() {
        return getSmartInfoBrowserSource() == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_USB;
    }

    boolean isSmartInfoModeApp() {
        return getSmartInfoMode() == TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_APPS;
    }
	
	public boolean isSmartInfoAppPreviewProgramBased(){
		return mIsSmartInfoAppPreviewProgramBased;
	}
	
	public String getSelectedSmartInfoPackage(){
		return mSelectedSmartinfoAppPackageName;
	}

    void fetchSmartInfoImageFromRelativePath(String relativePathToImage, long id, int thumbnailWidth, int thumbnailHeight, final ThumbnailBitmapFetchListener listener) {
        Log.d(TAG, "fetchSmartInfoImageFromRelativePath relativePathToImage " + relativePathToImage);
        Bitmap cachedImage = getImageFromCache(id);
        Log.d(TAG, "fetchSmartInfoImageFromRelativePath cachedImage " + cachedImage);
        if (cachedImage != null) {
            if (listener != null) {
                listener.onThumbnailBitmapFetchComplete(id, cachedImage);
            }
        } else {
            String absolutePath = "";
            if (isSmartInfoBrowserSourceServer()) {
                absolutePath = Constants.SMART_INFO_FILE_PATH_SERVER.concat(relativePathToImage);
            } else if(isSmartInfoBrowserSourceLocal()){
                absolutePath = Constants.SMART_INFO_FILE_PATH_LOCAL.concat(relativePathToImage);
            } else {
                if(smartInfoUSBPath == null) {
                    smartInfoUSBPath = AppUtil.getUsbSmartInfoPath();
                }
                if (!TextUtils.isEmpty(smartInfoUSBPath)) {
                    absolutePath = smartInfoUSBPath.concat(relativePathToImage);
                }
            }
            DashboardDataManager.getInstance().fetchThumbnailImage(absolutePath, id, thumbnailWidth, thumbnailHeight, new ThumbnailBitmapFetchListener() {
                @Override
                public void onThumbnailBitmapFetchComplete(long id, Bitmap bitmap) {
                    if (listener != null) {
                        listener.onThumbnailBitmapFetchComplete(id, bitmap);
                    }

                    if (bitmap != null) {
                        addImageToCache(id, bitmap);
                    }
                }
            });
        }
    }

    void fetchSmartInfo() {
        if (!isSmartInfoEnabled()) {
            DdbLogUtility.logRecommendationChapter("fetchSmartInfo", "smartInfo not enabled");
            return;
        }
        DdbLogUtility.logCommon(TAG, "fetchSmartInfo: isSmartInfoEnabled true mSmartInfoMode " + mSmartInfoMode);
        // clear the smart info tile image cache before fetching new data
       // clearSmartInfoImageCache();
        /*Android-P: DDB Bringup: Remove below line once TVSettingManager is available*/
        //fetchSmartInfoFromLocalPath();
        Log.d(TAG, "fetchSmartInfo mSmartInfoMode " +mSmartInfoMode);
        if (mSmartInfoMode == TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_BROWER) {
            int smartInfoBrowserSource = getSmartInfoBrowserSource();
            if (smartInfoBrowserSource == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_SERVER) {
                fetchSmartInfoFromServerPath();
            } else if (smartInfoBrowserSource == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_LOCAL) {
                fetchSmartInfoFromLocalPath();
            } else if (smartInfoBrowserSource == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_USB) {
                fetchSmartInfoFromUsbDrive();
            }
        } else if (isSmartInfoModeApp()) {
            fetchSmartInfoFromApp();
        }
    }

    void clearSmartInfoImageCache() {
        android.util.Log.d(TAG, "clearSmartInfoImageCache");
		notifySmartInfoUnavailable();
        mSmartInfoImageCache.clear();
    }

    void onConfigurationChanged() {
        fetchSmartInfo();
    }

    private void updateSmartInfoMode(int value) {
        DdbLogUtility.logCommon(TAG, "updateSmartInfoMode() called with: value = [" + value + "]");
        mSmartInfoMode = value;
    }

    private void updateSmartInfoMode() {
        mSmartInfoMode = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO, 0, 0);
		mSmartInfoBrowserSource =  mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_BROWSER_SETTINGS_SOURCE, 0, 0);    
    }

    private void registerForSmartInfoDownloadCompleteBroadcast() {
        mSmartInfoDownloadCompleteReceiver = new SmartInfoDownloadCompleteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_SMARTINFO_DOWNLOAD_COMPLETED);
        intentFilter.addAction(Constants.ACTION_CLONE_RESULT_CDB_SMARTINFO_BANNER);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mSmartInfoDownloadCompleteReceiver,intentFilter);
    }

    private void registerForSmartInfoRecommendationChanges() {
        DashboardDataManager.getInstance().addRecommendationListener(this);
    }
	
	private void registerSmartInfoPreviewProgramListener(){
		DashboardDataManager.getInstance().registerSmartInfoPreviewProgramListener(this);
	}

	private void unRegisterSmartInfoPreviewProgramListener(){
		DashboardDataManager.getInstance().unRegisterSmartInfoPreviewProgramListener(this);
	}	
		
	
	private void registerUsbEventsReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.LOCAL_ACTION_USB_BREAKIN);
        //intentFilter.addAction(Constants.LOCAL_ACTION_USB_BREAKOUT);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUsbBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isSmartInfoBrowserSourceUSB()) {
                String action = intent.getAction();
                if (Constants.LOCAL_ACTION_USB_BREAKIN.equals(action)) {
                    smartInfoUSBPath = null;
                    fetchSmartInfoFromUsbDrive();
                    return;
                }
                if (Constants.LOCAL_ACTION_USB_BREAKOUT.equals(action)) {
					/* as per TV should still continue to display smart info when USB is removed.
                    notifySmartInfoUnavailable();
                    clearSmartInfoData();
                    clearSmartInfoImageCache();*/
                    return;
                }
            }
        }
    };

    private void fetchSmartInfoFromLocalPath() {
        fetchSmartInfoFromPath(Constants.SMART_INFO_FILE_PATH_LOCAL.concat("/").concat(Constants.SMART_INFO_BROWSER_METADATA_FILE_NAME), this);
    }

    private void fetchSmartInfoFromServerPath() {
        fetchSmartInfoFromPath(Constants.SMART_INFO_FILE_PATH_SERVER.concat("/").concat(Constants.SMART_INFO_BROWSER_METADATA_FILE_NAME), this);
    }

    private void fetchSmartInfoFromUsbDrive() {
        if(smartInfoUSBPath == null) {
            smartInfoUSBPath = AppUtil.getUsbSmartInfoPath();
        }
        if (!TextUtils.isEmpty(smartInfoUSBPath)) {
            fetchSmartInfoFromPath(smartInfoUSBPath.concat("/").concat(Constants.SMART_INFO_BROWSER_METADATA_FILE_NAME), this);
        }else{
			notifySmartInfoUnavailable();
		}
    }

    private void fetchSmartInfoFromApp() {
	   fetchSmartInfoFromApp(mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_APPS, 0, 0));      
    }

    private void fetchSmartInfoFromApp(int appId){
        fetchSmartInfoAppPackageName(appId, this);		
    }
	
    private void fetchSamrtInfoPreviewPrograms(int appId) {
        DashboardDataManager.getInstance().fetchSmartInfoPreviewProgram(getPackageName(appId));
    }	

    private String getPackageName(int selectedAppId){
        String packageName = null;
        Cursor c=null;
        try {
            HtvAppQuery query = new HtvAppQuery(selectedAppId);
            c = executeQuery(getContext(), query);
            if (c != null && c.moveToFirst()) {
                packageName = c.getString(c.getColumnIndex(HtvContract.HtvAppList.COLUMN_NAME));
            }
        }finally {
            if(c != null) c.close();
        }
        DdbLogUtility.logRecommendationChapter("DashboardDataManager", "getPackageName selectedAppId "+ selectedAppId + "  " + packageName);
        return packageName;
    }


    private void fetchSmartInfoAppPackageName(int appId, SmartInfoAppDataFetchListener listener) {
        SmartInfoAppDataFetchCallable callable = new SmartInfoAppDataFetchCallable(getContext(), appId);
        SmartInfoAppDataFetchTask task = new SmartInfoAppDataFetchTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    private void fetchSmartInfoFromPath(String path, SmartInfoFileParseListener listener) {
        SmartInfoFileParseCallable callable = new SmartInfoFileParseCallable(getContext(), path);
        SmartInfoFileParseTask task = new SmartInfoFileParseTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    private void smartInfoModeChanged(int value) {
        updateSmartInfoMode(value);
		mSmartInfoPreviewChannel = null;
		unRegisterSmartInfoPreviewProgramListener();
        // clear the smart info tile image cache before fetching new data
        //clearSmartInfoImageCache();
        if (mSmartInfoMode == TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_BROWER) {
            smartInfoBrowserSettingsChanged(mSmartInfoBrowserSource);
        } else if (mSmartInfoMode == TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_APPS) {
            fetchSmartInfoFromApp();
        } else if (mSmartInfoMode == TvSettingsDefinitions.PbsSmartinfo.PBSMGR_SMARTINFO_OFF) {
            notifySmartInfoOff();
        }
    }

    private int getSmartInfoMode() {
        return mSmartInfoMode;
    }

    private int getSmartInfoBrowserSource() {
        return mSmartInfoBrowserSource;
    }

    private String getPreferredLanguage() {
        return JEDIFactory.getInstance(IGuestPreference.class).getGuestLanguage();
    }

    private void smartInfoBrowserSettingsChanged(int value) {
        android.util.Log.d(TAG, "smartInfoBrowserSettingsChanged() called with: value = [" + value + "]");
        // clear the smart info tile image cache before fetching new data
        clearSmartInfoImageCache();
        mSmartInfoBrowserSource = value;
        if (getSmartInfoBrowserSource() == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_SERVER) {
            fetchSmartInfoFromServerPath();
        } else if (getSmartInfoBrowserSource() == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_LOCAL) {
            fetchSmartInfoFromLocalPath();
        } else if(getSmartInfoBrowserSource() == TvSettingsDefinitions.PbsSmartinfoBrowserSettingsConstants.PBSMGR_BROWSER_SETTINGS_SOURCE_USB){
            fetchSmartInfoFromUsbDrive();
        }
    }

    private void smartInfoAppChanged(int value) {
		unRegisterSmartInfoPreviewProgramListener();
        fetchSmartInfoFromApp(value);
    }

    private void dashboardModeChanged() {
        if (isSmartInfoEnabled()) {
            // Dashboard mode has been changed. Fetch fresh smart info data and notify listeners
            fetchSmartInfo();
        } else {
            notifySmartInfoOff();
        }
    }

    private void notifySmartInfoDataAvailable() {
        for (int i = 0; mSmartInfoListenerRefs != null && i < mSmartInfoListenerRefs.size(); i++) {
            WeakReference<SmartInfoListener> listenerRef = mSmartInfoListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SmartInfoListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSmartInfoDataAvailable();
            }
        }
    }

    private void notifySmartInfoRecommendationsAvailable() {
        for (int i = 0; mSmartInfoListenerRefs != null && i < mSmartInfoListenerRefs.size(); i++) {
            WeakReference<SmartInfoListener> listenerRef = mSmartInfoListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SmartInfoListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSmartInfoRecommendationsAvailable();
            }
        }
    }
	
	 private void notifySmartInfoPreviewProgramsAvailable() {
        for (int i = 0; mSmartInfoListenerRefs != null && i < mSmartInfoListenerRefs.size(); i++) {
            WeakReference<SmartInfoListener> listenerRef = mSmartInfoListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SmartInfoListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSmartInfoPreviewProgramsAvailable();
            }
        }
    }

    private void notifySmartInfoOff() {
        for (int i = 0; mSmartInfoListenerRefs != null && i < mSmartInfoListenerRefs.size(); i++) {
            WeakReference<SmartInfoListener> listenerRef = mSmartInfoListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SmartInfoListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSmartInfoOff();
            }
        }
    }

    private void notifySmartInfoUnavailable() {
        for (int i = 0; mSmartInfoListenerRefs != null && i < mSmartInfoListenerRefs.size(); i++) {
            WeakReference<SmartInfoListener> listenerRef = mSmartInfoListenerRefs.get(i);
            if (listenerRef == null) {
                continue;
            }

            SmartInfoListener listener = listenerRef.get();
            if (listener != null) {
                listener.onSmartInfoUnavailable();
            }
        }
    }

    private Recommendation createEmptyAppRecommendation(String packageName, Bitmap logo, String appName) {
        Recommendation recommendation = new Recommendation();
        recommendation.setId(0);
        recommendation.setLogo(logo);
        recommendation.setTitle(appName);
        recommendation.setContentType(new String[]{Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION});
		
		Intent launchIntent = new Intent();
        launchIntent.setPackage(packageName);
        launchIntent.addCategory("com.philips.professionaldisplaysolutions.jedi.intent.category.SMART_INFO");
        launchIntent.putExtra("com.philips.professionaldisplaysolutions.jedi.SHOW_UI", true);
        PendingIntent pendingIntent = PendingIntent.getService(getContext(), 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        recommendation.setPendingIntent(pendingIntent);
        return recommendation;
    }

    private void removeEmptyRecommendation() {
        for (int i = 0; mSmartInfoRecommendations != null && i < mSmartInfoRecommendations.size(); i++) {
            Recommendation recommendation = mSmartInfoRecommendations.get(i);
            if (isEmptyRecommendation(recommendation)) {
                mSmartInfoRecommendations.remove(i);
                return;
            }
        }
    }

    private boolean isEmptyRecommendation(Recommendation recommendation) {
        String[] contentType = recommendation.getContentType();
        return contentType != null && Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION.equals(contentType[0]);
    }

    private boolean isSmartInfoRecommendation(Recommendation recommendation) {
        boolean smartInfoRecommendation = false;

        if (recommendation == null) {
            return smartInfoRecommendation;
        }

        String[] contentTypes = recommendation.getContentType();
        if (contentTypes == null) {
            return smartInfoRecommendation;
        }

        for (String contentType : contentTypes) {
            if (contentType.equals(Constants.CONTENT_TYPE_SMART_INFO_RECOMMENDATION)) {
                smartInfoRecommendation = true;
                break;
            }
        }

        return smartInfoRecommendation;
    }

    private static Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().
                query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    private void addImageToCache(long imageId, Bitmap image) {
        mSmartInfoImageCache.addImage(imageId, image);
    }

    private Bitmap getImageFromCache(long imageId) {
        return mSmartInfoImageCache.getImage(imageId);
    }

    void onSmartInfoModeChanged() {
        sendMessage(UiThreadHandler.MSG_WHAT_SMARTINFO_MODE_CHANGED);
    }

    void onSmartInfoModeChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_SMARTINFO_MODE_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void onSmartInfoBrowserSettingsSourceChanged() {
        sendMessage(UiThreadHandler.MSG_WHAT_SMARTINFO_BROWSER_SETTINGS_SOURCE_CHANGED);
    }

    void onSmartInfoBrowserSettingsSourceChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_SMARTINFO_BROWSER_SETTINGS_SOURCE_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void onClearDataTriggered(){
        sendMessage(UiThreadHandler.MSG_WHAT_CLEAR_DATA_TRIGGERED);
    }

    void onSmartInfoAppChanged() {
        sendMessage(UiThreadHandler.MSG_WHAT_SMARTINFO_APP_CHANGED);
    }

    void onSmartInfoAppChanged(int value) {
        Message message = Message.obtain(mUiThreadHandler, UiThreadHandler.MSG_WHAT_SMARTINFO_APP_CHANGED);
        message.arg1 = value;
        message.sendToTarget();
    }

    void onDashboardModeChanged() {
        sendMessage(UiThreadHandler.MSG_WHAT_DASHBOARD_MODE_CHANGED);
    }

    private void sendMessage(int what) {
        Message message = Message.obtain(mUiThreadHandler, what);
        message.sendToTarget();
    }

    private final class SmartInfoDownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DdbLogUtility.logCommon(TAG, "onReceive() called with: intent = [" + intent + "]");
            if (Constants.ACTION_SMARTINFO_DOWNLOAD_COMPLETED.equals(action) ||
                    Constants.ACTION_CLONE_RESULT_CDB_SMARTINFO_BANNER.equals(action)) {
                fetchSmartInfo();
                return;
            }
        }
    }

    private static class SmartInfoAppDataFetchCallable implements Callable<AppData> {

        private Context mContext;
        private int mAppId;

        SmartInfoAppDataFetchCallable(Context context, int appId) {
            mContext = context;
            mAppId = appId;
        }

        @Override
        public AppData call() throws Exception {
            String appPackageName = null;
            String title = null;
            Drawable icon = null;
            Cursor cursor = null;
            HtvAppQuery query = new HtvAppQuery(mAppId);
            DdbLogUtility.logCommon(TAG, "SmartInfoAppDataFetchCallable call() mAppId " + mAppId);
            try {
                cursor = executeQuery(mContext, query);
                if (cursor != null && cursor.moveToNext()) {
                    appPackageName = cursor.getString(cursor.getColumnIndex(HtvContract.HtvAppList.COLUMN_NAME));

                    PackageManager packageManager = mContext.getPackageManager();
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appPackageName, PackageManager.GET_META_DATA);
                    title = packageManager.getApplicationLabel(applicationInfo).toString();
                    icon = packageManager.getApplicationBanner(applicationInfo);
                    if (icon == null) {
                        icon = packageManager.getApplicationLogo(applicationInfo);
                    }
                    if (icon == null) {
                        icon = packageManager.getApplicationIcon(applicationInfo);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return new AppData(appPackageName, title, icon);
        }
    }

    private static class SmartInfoAppDataFetchTask extends FutureTask<AppData> {

        private Handler mHandler;
        private SmartInfoAppDataFetchListener mSmartInfoAppDataFetchListener;

        private SmartInfoAppDataFetchTask(SmartInfoAppDataFetchCallable callable, Handler handler, SmartInfoAppDataFetchListener smartInfoAppDataFetchListener) {
            super(callable);
            mHandler = handler;
            mSmartInfoAppDataFetchListener = smartInfoAppDataFetchListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    AppData appData = get();
                    SmartInfoAppDataFetchResult result = new SmartInfoAppDataFetchResult(appData, mSmartInfoAppDataFetchListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_APP_DATA_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "SmartInfoAppDataFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class SmartInfoFileParseCallable implements Callable<SmartInfoFileData> {

        private Context mContext;
        private String mPath;

        SmartInfoFileParseCallable(Context context, String path) {
            mContext = context;
            mPath = path;
        }

        @Override
        public SmartInfoFileData call() throws Exception {
            SmartInfoFileData smartInfoFileData = null;
            String title = null;
            Drawable icon = null;

            File smartInfoXmlFile = new File(mPath);
            if (!smartInfoXmlFile.getParentFile().exists()) {
                // if smartinfo mode is browser and browser source is server and server url is not empty/null
                // then create an empty tile
                if (DashboardDataManager.getInstance().isSmartInfoBrowserSourceServer()) {
                    String serverUrl = DashboardDataManager.getInstance().getTvSettingsManager()
                            .getString(TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_BROWSER_SEVER_URL, 0, null);
                    if (!TextUtils.isEmpty(serverUrl)) {
                        SmartInfo smartInfo = new SmartInfo.Builder(mContext).build();
                        List<SmartInfo> smartInfoData = new ArrayList<>();
                        smartInfoData.add(smartInfo);
                        smartInfoFileData = new SmartInfoFileData(smartInfoData,
                                null,
                                null,
                                null,
                                null);
                        return smartInfoFileData;
                    }
                }

                // smart info xml file parent directory does not exist. return everything as empty.
                smartInfoFileData = new SmartInfoFileData(null, null, null, null, null);
                return smartInfoFileData;
            }
            // If smart info metadata xml file is not found, check for presence of any index.html file
            if (!smartInfoXmlFile.exists()) {
                File[] indexHtmlFiles = smartInfoXmlFile.getParentFile().listFiles(getSmartInfoIndexHtmlFileFilter());
                if (indexHtmlFiles != null && indexHtmlFiles.length > 0 ) {
                    String mainUrl = "/".concat(indexHtmlFiles[0].getName());
                    SmartInfo.Builder builder = new SmartInfo.Builder(mContext);
                    SmartInfo smartInfo = builder.setTitle(null).
                            setDescription(null).
                            setUrl(mainUrl).
                            setUrlType(Constants.SMARTINFO_URL_TYPE_MAIN).build();
                    List<SmartInfo> smartInfoData = new ArrayList<>();
                    smartInfoData.add(smartInfo);
                    smartInfoFileData = new SmartInfoFileData(smartInfoData,
                            null,
                            null,
                            mainUrl,
                            null);
                    return smartInfoFileData;
                } else {
                    // if smartinfo mode is browser and browser source is server and server url is not empty/null
                    // then create an empty tile
                    if (DashboardDataManager.getInstance().isSmartInfoBrowserSourceServer()) {
                        String serverUrl = DashboardDataManager.getInstance().getTvSettingsManager()
                                .getString(TvSettingsConstants.PBSMGR_PROPERTY_SMARTINFO_BROWSER_SEVER_URL, 0, null);
                        if (!TextUtils.isEmpty(serverUrl)) {
                            SmartInfo smartInfo = new SmartInfo.Builder(mContext).build();
                            List<SmartInfo> smartInfoData = new ArrayList<>();
                            smartInfoData.add(smartInfo);
                            smartInfoFileData = new SmartInfoFileData(smartInfoData,
                                    null,
                                    null,
                                    null,
                                    null);
                            return smartInfoFileData;
                        }
                    }

                    // smart info index file is not present either. return everything as empty.
                    smartInfoFileData = new SmartInfoFileData(null, null, null, null, null);
                    return smartInfoFileData;
                }
            }


            SmartInfoXmlParser smartInfoXmlParser = new SmartInfoXmlParser(mContext);
            smartInfoXmlParser.parseXml(mPath);

            // Fetch the bitmap from the icon file's path
            String iconFileRelativePath = smartInfoXmlParser.getSmartInfoIconPath();
            if (!TextUtils.isEmpty(iconFileRelativePath)) {
                String iconFileAbsolutePath = smartInfoXmlFile.getParentFile().getPath().concat(iconFileRelativePath);
                icon = new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeFile(iconFileAbsolutePath));
                // A bitmap may not be null even if the decoding is done from an invalid or non-existent path
                // Hence, we also need to check if a valid bitmap has really been decoded
                // A valid bitmap will have its height (or width) not-equal to -1.
                if (icon != null && icon.getIntrinsicHeight() == -1) {
                    icon = null;
                }
            }

            List<SmartInfo> smartInfoData = smartInfoXmlParser.getSmartInfoData();
            // If no <Tile> tag is present in the xml, smartInfoData will be null/empty.
            // In this case, create a single smartInfo object with the values that have been extracted from
            // the <SmartInfoDetails> tag in the xml
            if (smartInfoData == null || smartInfoData.isEmpty()) {
                // If <StartPageURL> tag is not not present either it means smart info data is effectively empty
                // return everything as empty.
                if (TextUtils.isEmpty(smartInfoXmlParser.getSmartInfoMainUrl())) {
                    smartInfoFileData = new SmartInfoFileData(null, null, null, null, null);
                    return smartInfoFileData;
                }

                SmartInfo.Builder builder = new SmartInfo.Builder(mContext);
                SmartInfo smartInfo = builder.setTitle(smartInfoXmlParser.getSmartInfoTitle()).
                        setDescription(smartInfoXmlParser.getSmartInfoDescription()).
                        setUrl(smartInfoXmlParser.getSmartInfoMainUrl()).
                        setUrlType(Constants.SMARTINFO_URL_TYPE_MAIN).build();
                smartInfoData = new ArrayList<>();
                smartInfoData.add(smartInfo);
            }

            smartInfoFileData = new SmartInfoFileData(smartInfoData,
                    smartInfoXmlParser.getSmartInfoTitle(),
                    smartInfoXmlParser.getSmartInfoDescription(),
                    smartInfoXmlParser.getSmartInfoMainUrl(),
                    icon);
            return smartInfoFileData;
        }

        private FileFilter getSmartInfoIndexHtmlFileFilter() {
            return new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.compile(Constants.SMART_INFO_BROWSER_INDEX_FILE_NAME_REGEX).matcher(pathname.getPath()).find();
                }
            };
        }
    }

    private static class SmartInfoFileParseTask extends FutureTask<SmartInfoFileData> {

        private Handler mHandler;
        private SmartInfoFileParseListener mSmartInfoFileParseListener;

        private SmartInfoFileParseTask(SmartInfoFileParseCallable callable, Handler handler, SmartInfoFileParseListener smartInfoFileParseListener) {
            super(callable);
            mHandler = handler;
            mSmartInfoFileParseListener = smartInfoFileParseListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    SmartInfoFileData smartInfoFileData = get();
                    SmartInfoFileParseResult result = new SmartInfoFileParseResult(smartInfoFileData, mSmartInfoFileParseListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_SMARTINFO_FILE_PARSE_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "SmartInfoFileParseTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class SmartInfoAppDataFetchResult {
        SmartInfoAppDataFetchListener mSmartInfoAppDataFetchListener;
        AppData mAppData;

        private SmartInfoAppDataFetchResult(AppData appData, SmartInfoAppDataFetchListener listener) {
            mAppData = appData;
            mSmartInfoAppDataFetchListener = listener;
        }
    }

    private static final class AppData {
        String mPackageName;
        String mTitle;
        Drawable mIcon;

        private AppData(String packageName, String title, Drawable icon) {
            mPackageName = packageName;
            mTitle = title;
            mIcon = icon;
        }
    }

    private static final class SmartInfoFileParseResult {
        SmartInfoFileParseListener mSmartInfoFileParseListener;
        SmartInfoFileData mSmartInfoFileData;

        private SmartInfoFileParseResult(SmartInfoFileData smartInfoFileData, SmartInfoFileParseListener listener) {
            mSmartInfoFileData = smartInfoFileData;
            mSmartInfoFileParseListener = listener;
        }
    }

    private static final class SmartInfoFileData {
        List<SmartInfo> mSmartInfoData;
        Drawable mIcon;
        String mTitle;
        String mDescription;
        String mSmartInfoMainUrl;

        private SmartInfoFileData(List<SmartInfo> smartInfoData, String title, String description, String smartInfoMainUrl, Drawable icon) {
            Log.d(TAG, "SmartInfoFileData() called with: title = [" + title + "] smartInfoMainUrl = [" + smartInfoMainUrl);
            mSmartInfoData = smartInfoData;
            mTitle = title;
            mDescription = description;
            mSmartInfoMainUrl = smartInfoMainUrl;
            mIcon = icon;
        }
    }

    private static final class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_APP_DATA_FETCH_COMPLETE = 100;
        private static final int MSG_WHAT_SMARTINFO_MODE_CHANGED = 101;
        private static final int MSG_WHAT_SMARTINFO_BROWSER_SETTINGS_SOURCE_CHANGED = 102;
        private static final int MSG_WHAT_SMARTINFO_APP_CHANGED = 103;
        private static final int MSG_WHAT_SMARTINFO_FILE_PARSE_COMPLETE = 104;
        private static final int MSG_WHAT_DASHBOARD_MODE_CHANGED = 105;
        private static final int MSG_WHAT_CLEAR_DATA_TRIGGERED = 106;

        private WeakReference<SmartInfoDataManager> mSmartInfoDataManagerRef;

        private UiThreadHandler(SmartInfoDataManager smartInfoDataManager) {
            mSmartInfoDataManagerRef = new WeakReference<SmartInfoDataManager>(smartInfoDataManager);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCommon(TAG, "UiThreadHandler handleMessage msg.what " + msg.what);
            if (what == MSG_WHAT_APP_DATA_FETCH_COMPLETE) {
                SmartInfoAppDataFetchResult result = (SmartInfoAppDataFetchResult) msg.obj;
                if (result.mSmartInfoAppDataFetchListener != null) {
                    result.mSmartInfoAppDataFetchListener.onSmartInfoAppDataFetched(result.mAppData.mPackageName, result.mAppData.mTitle, result.mAppData.mIcon);
                }
                return;
            }

            if (what == MSG_WHAT_SMARTINFO_MODE_CHANGED) {
                SmartInfoDataManager smartInfoDataManager = mSmartInfoDataManagerRef.get();
                if (smartInfoDataManager != null) {
                    smartInfoDataManager.smartInfoModeChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_SMARTINFO_BROWSER_SETTINGS_SOURCE_CHANGED) {
                SmartInfoDataManager smartInfoDataManager = mSmartInfoDataManagerRef.get();
                if (smartInfoDataManager != null) {
                    smartInfoDataManager.smartInfoBrowserSettingsChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_SMARTINFO_APP_CHANGED) {
                SmartInfoDataManager smartInfoDataManager = mSmartInfoDataManagerRef.get();
                if (smartInfoDataManager != null) {
                    smartInfoDataManager.smartInfoAppChanged(msg.arg1);
                }
                return;
            }

            if (what == MSG_WHAT_SMARTINFO_FILE_PARSE_COMPLETE) {
                SmartInfoFileParseResult result = (SmartInfoFileParseResult) msg.obj;
                if (result.mSmartInfoFileParseListener != null) {
                    result.mSmartInfoFileParseListener.onSmartInfoFileParseComplete(result.mSmartInfoFileData.mSmartInfoData,
                            result.mSmartInfoFileData.mTitle,
                            result.mSmartInfoFileData.mIcon,
                            result.mSmartInfoFileData.mDescription,
                            result.mSmartInfoFileData.mSmartInfoMainUrl);
                }
                return;
            }

            if (what == MSG_WHAT_DASHBOARD_MODE_CHANGED) {
                SmartInfoDataManager smartInfoDataManager = mSmartInfoDataManagerRef.get();
                if (smartInfoDataManager != null) {
                    smartInfoDataManager.dashboardModeChanged();
                }
                return;
            }

            if(what == MSG_WHAT_CLEAR_DATA_TRIGGERED){
                SmartInfoDataManager smartInfoDataManager = mSmartInfoDataManagerRef.get();
                if (smartInfoDataManager != null) {
                    smartInfoDataManager.notifySmartInfoUnavailable();
                    smartInfoDataManager.clearSmartInfoData();
                    smartInfoDataManager.clearSmartInfoImageCache();
                }
                return;
            }
        }
    }

    private static class SmartInfoImageCache {
        private static final String TAG = "SmartInfoLogoCache";

        // Reserve 1/32th of the max runtime memory available for this LruCache in Kilo bytes
        private static final int CACHE_SIZE_IN_KBYTES = 16 * 1024; //Value of 16MB in KB

        private final LruCache<Long, Bitmap> mImageCache;

        private SmartInfoImageCache() {
            mImageCache = new LruCache<Long, Bitmap>(CACHE_SIZE_IN_KBYTES) {
                @Override
                protected int sizeOf(Long key, Bitmap value) {
                    return value.getAllocationByteCount() / 1024;
                }
				
				@Override
				protected void entryRemoved(final boolean evicted, final Long key, Bitmap oldValue, final Bitmap newValue) {
                    DdbLogUtility.logCommon(TAG, "entryRemoved: called with: evicted = [" + evicted + "], key = [" + key + "], oldValue = [" + oldValue + "], newValue = [" + newValue + "]");
					if(evicted && oldValue != null && !oldValue.isRecycled()){
						oldValue.recycle();
						oldValue = null;
				}
		}

            };
        }

        private void clear() {
            mImageCache.evictAll();
        }

        private Bitmap getImage(long imageId) {
            return mImageCache.get(imageId);
        }

        private void addImage(long imageId, Bitmap image) {
            mImageCache.put(imageId, image);
        }
    }
}
