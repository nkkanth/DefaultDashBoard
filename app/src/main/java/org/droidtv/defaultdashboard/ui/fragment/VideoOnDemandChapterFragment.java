package org.droidtv.defaultdashboard.ui.fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.common.RecommendationHelper;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.RecommendationListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.VideoOnDemandRecommendationListener;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.VideoOnDemandShelfHeaderItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.PreviewProgramsChannel;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.recommended.RecommendationChangeType;
import org.droidtv.defaultdashboard.ui.presenter.VideoOnDemandShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.VideoOnDemandShelfRowPresenter;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.DDBImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.SinglePresenterSelector;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public class VideoOnDemandChapterFragment extends ChapterFragment implements RecommendationListener, VideoOnDemandRecommendationListener, DashboardDataManager.PreviewProgramsListener {

    private static final String TAG = "VideoOnDemandChapterFragment";

    private Map<String, ArrayObjectAdapter> mVodPackageAdapterMap;
    private Map<String, ShelfRow> mVodPackageShelfRowMap;
    private Map<String, ShelfRow> mPreviewChannelVodShelfRowMap;
    private PackageManager mPackageManager;
    private DashboardDataManager mDashboardDataManager;

    public VideoOnDemandChapterFragment() {
        super();
        mVodPackageAdapterMap = new HashMap<>();
        mVodPackageShelfRowMap = new HashMap<>();
        mPreviewChannelVodShelfRowMap = new HashMap<>();
        mDashboardDataManager = DashboardDataManager.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
        DDBImageLoader.setChapterVisibility(true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        DDBImageLoader.setChapterVisibility(false);
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        registerRecommendationListener();
        registerVideoOnDemandRecommendationListener();
        registerVideoOnDemandPreviewListner();
    }

    private void registerVideoOnDemandPreviewListner(){
        mDashboardDataManager.addPreviewProgramsListener(this);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");
        unregisterRecommendationListener();
        unregisterVideoOnDemandRecommendationListener();
        unRegisterVideoOnDemandPreviewListner();
        unregisterPreviewProgramsListener();
        super.onDestroyView();
        DdbLogUtility.logVodChapter(TAG, "#### onDestroyView() exit");
    }

    private void unregisterPreviewProgramsListener() {
        mDashboardDataManager.removePreviewProgramsListener(this);
    }

    private void unRegisterVideoOnDemandPreviewListner(){
        mDashboardDataManager.removePreviewProgramsListener(this);
    }

    @Override
    protected void createRows() {
        android.util.Log.d(TAG, "createRows() called");
        mPackageManager = getActivity().getApplicationContext().getPackageManager();
        createVideoOnDemandRecommendationShelves();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    private void registerVideoOnDemandRecommendationListener() {
        mDashboardDataManager.addVideoOnDemandRecommendationListener(this);
    }

    private void unregisterVideoOnDemandRecommendationListener() {
        mDashboardDataManager.removeVideoOnDemandRecommendationListener(this);
    }

    private void registerRecommendationListener() {
        mDashboardDataManager.addRecommendationListener(this);
    }

    private void unregisterRecommendationListener() {
        mDashboardDataManager.removeRecommendationListener(this);
    }

    private void createVideoOnDemandRecommendationShelves(){
	        DdbLogUtility.logVodChapter(TAG, "createVideoOnDemandRecommendationShelves() called");
        buildVodRecommendationShelves();//To build notification based recommendation shelf
        buildPreviewProgramRecommendationShelves(); //To build preview channel based recommendation shelf
    }

    private void buildVodRecommendationShelves() {
        List<Recommendation> recommendationList = new ArrayList<Recommendation>();
        Map<String, List<Recommendation>> vodRecommendationsMap = mDashboardDataManager.getVodRecommendations();
        android.util.Log.d(TAG, "createVideoOnDemandRecommendationShelves: vodRecommendationsMap size " + vodRecommendationsMap.size());
        for (String packageName : vodRecommendationsMap.keySet()) {
            List<Recommendation> vodRecommendationList = vodRecommendationsMap.get(packageName);
            createVideoOnDemandRecommendationShelf(packageName, vodRecommendationList, null);
        }
    }

    private void buildPreviewProgramRecommendationShelves(){
        Map<String, PreviewProgramsChannel> previewProgramsChannelMap = mDashboardDataManager.getVodPreviewProgramChannelList();
        Log.d(TAG, "buildPreviewProgramRecommendationShelves: size " + ((previewProgramsChannelMap != null) ? Integer.toString(previewProgramsChannelMap.size()) : null));
        if(previewProgramsChannelMap != null) {
            for (String previewChannelId : previewProgramsChannelMap.keySet()) {
                android.util.Log.d(TAG, "buildPreviewProgramRecommendationShelves: previewChannelId " + previewChannelId);
                PreviewProgramsChannel previewProgramsChannel = previewProgramsChannelMap.get(previewChannelId);
                List<Recommendation> vodPreviewRecommendationList = previewProgramsChannel.getPreviewProgramList();
                if(!mVodPackageShelfRowMap.containsKey(previewChannelId)) {
                    createVideoOnDemandRecommendationShelf(previewProgramsChannel.getPackageName(), vodPreviewRecommendationList, previewProgramsChannel);
                }else{
                    android.util.Log.d(TAG, "buildPreviewProgramRecommendationShelves: already shelf added");
                }
            }
        }else{
            android.util.Log.d(TAG, "buildPreviewProgramRecommendationShelves: buildPreviewProgramRecommendationShelves null");
        }
    }

    private void createVideoOnDemandRecommendationShelf(String packageName, List<Recommendation> recommendations, PreviewProgramsChannel previewProgramChannel ) {
        if (recommendations != null && !recommendations.isEmpty()) {
            String vodShelfappName = "";
            Drawable vodShelfIcon = null;
            try {
                mPackageManager = getActivity().getApplicationContext().getPackageManager();
                vodShelfappName = (String) mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                vodShelfIcon = mPackageManager.getApplicationIcon(mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
               Log.e(TAG,"Exception :" +e.getMessage());
                vodShelfappName = packageName;
                vodShelfIcon = getContext().getDrawable(R.mipmap.ic_launcher);
            }

            VideoOnDemandShelfItemPresenter itemPresenter = new VideoOnDemandShelfItemPresenter();
            itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
            ArrayObjectAdapter shelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
            shelfItemAdapter.addAll(0, recommendations);

            //Append open app tile for each row
            Recommendation openAppRecommendation = getOpenAppRecommendation(packageName);
            shelfItemAdapter.add(openAppRecommendation);
            android.util.Log.d(TAG, "createVideoOnDemandRecommendationShelf: shelfItemAdapter.size() " + shelfItemAdapter.size());
            ShelfRow shelfRow = null;
            if(previewProgramChannel != null){ //Create preview program channel shelf row
                android.util.Log.d(TAG, "createVideoOnDemandRecommendationShelf: previw channel name " + previewProgramChannel.getDisplayName());
                shelfRow = new ShelfRow(new VideoOnDemandShelfHeaderItem(vodShelfappName, vodShelfIcon, previewProgramChannel.getDisplayName()), shelfItemAdapter);
                mVodPackageAdapterMap.put(Integer.toString(previewProgramChannel.getId()), shelfItemAdapter);
                mVodPackageShelfRowMap.put(Integer.toString(previewProgramChannel.getId()), shelfRow);

            }else{//Create notifications based VOD app shelf
                android.util.Log.d(TAG, "createVideoOnDemandRecommendationShelf: Notifcation based VOD shelf");
                shelfRow = new ShelfRow(new VideoOnDemandShelfHeaderItem(vodShelfappName, vodShelfIcon, vodShelfappName), shelfItemAdapter);
                mVodPackageAdapterMap.put(packageName, shelfItemAdapter);
                mVodPackageShelfRowMap.put(packageName, shelfRow);
            }
            addRow(shelfRow);
            android.util.Log.d(TAG, "createVideoOnDemandRecommendationShelf: mVodPackageShelfRowMap.size() " + mVodPackageShelfRowMap.size());
        }
    }

    private Recommendation getOpenAppRecommendation(String packageName) {
        PendingIntent openAppPendingIntent = getOpenAppPendingInent(packageName);
        String[] contentTypes = {Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION};
        String vodAppTitle = "Vod App";
        String vodAppDescription = "Vod App";

        Recommendation openAppRecommendation = new Recommendation();
        openAppRecommendation.setContentType(contentTypes);
        openAppRecommendation.setPendingIntent(openAppPendingIntent);

        try {
            CharSequence title = mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            if (title != null) {
                vodAppTitle = title.toString();
            }
            CharSequence description = mPackageManager.getApplicationInfo(packageName, 0).loadDescription(mPackageManager);
            if (description != null) {
                vodAppDescription = description.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        openAppRecommendation.setTitle(vodAppTitle);
        openAppRecommendation.setDescription(vodAppDescription);

        return openAppRecommendation;
    }

    private PendingIntent getOpenAppPendingInent(String packageName) {
        PendingIntent openAppPendingIntent = null;
        try {
            Intent openAppLaunchIntent = mPackageManager.getLeanbackLaunchIntentForPackage(packageName);
            if (openAppLaunchIntent == null) {
                openAppLaunchIntent = mPackageManager.getLaunchIntentForPackage(packageName);
            }
            openAppPendingIntent = PendingIntent.getActivity(getContext(), 0, openAppLaunchIntent, 0);
        }catch (Exception e) {
            Log.d(TAG, "getOpenAppPendingInent: " + e.getMessage());
        }
        return openAppPendingIntent;
    }

    @Override
    protected PresenterSelector getChapterPresenterSelector() {
        VideoOnDemandShelfRowPresenter shelfRowPresenter = new VideoOnDemandShelfRowPresenter(FocusHighlight.ZOOM_FACTOR_XSMALL, false);
        shelfRowPresenter.setShadowEnabled(true);
        shelfRowPresenter.setSelectEffectEnabled(false);
        shelfRowPresenter.setKeepChildForeground(true);
        return new SinglePresenterSelector(shelfRowPresenter);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Recommendation) {
            onRecommendationClicked((Recommendation) item);
            return;
        }
    }

    @Override
    public void onRecommendationsAvailable(int recommendationCategory) {
        if (recommendationCategory == RecommendationHelper.Category.VOD) {
            createVideoOnDemandRecommendationShelves();
        }else{
            android.util.Log.d(TAG, "onRecommendationsAvailable: not a VOD conent " + recommendationCategory);
        }
    }

    @Override
    public void onRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        if (isVodRecommendation(recommendation)) { //get the row id for the recommendation, so that it can be updated as per changes
            onVodRecommendationChanged(recommendation, recommendationChangeType);
        }
    }

    @Override
    public void onVideoOnDemandRecommendationAppsUpdated() {
        updateVideoOnDemandRecommendationShelves();
    }

    @Override
    public void onVideoOnDemandRecommendationAvailable() {
        // Do nothing
    }

    @Override
    public void onVideoOnDemandRecommendationUnavailable() {
        // Do nothing
    }

    private void onVodRecommendationChanged(Recommendation recommendation, RecommendationChangeType recommendationChangeType) {
        String packageName = recommendation.getPendingIntent().getCreatorPackage();

        if (!mDashboardDataManager.isAppRecommendationEnabled(packageName)) {
            return;
        }

        if (recommendationChangeType == RecommendationChangeType.ADDED) {
            if (mVodPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mVodPackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.remove(i);
                        adapter.add(i, recommendation);
                        return;
                    }
                }
                adapter.add(0, recommendation);
            } else {
                List<Recommendation> recommendations = new ArrayList<>();
                recommendations.add(recommendation);
                createVideoOnDemandRecommendationShelf(packageName, recommendations, null);//TODO: Utam: 3rd parameter is preview channel display name, Need implementation to send preview channel name as
                                                                                            //3rd parameter while sending notification for  onVodRecommendationChanged
            }
            return;
        }

        if (recommendationChangeType == RecommendationChangeType.CANCELED) {
            if (mVodPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mVodPackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.remove(recommendation);
                    }
                }

                if (adapter.size() == 1) { //open app tile is considered so size will be 1
                    mVodPackageAdapterMap.remove(packageName);
                    ShelfRow shelfRow = mVodPackageShelfRowMap.remove(packageName);
                    removeRow(shelfRow);
                }

                return;
            }
        }

        if (recommendationChangeType == RecommendationChangeType.UPDATED) {
            if (mVodPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mVodPackageAdapterMap.get(packageName);
                for (int i = 0; i < adapter.size(); i++) {
                    Recommendation recommendationItem = (Recommendation) adapter.get(i);
                    if (recommendation.getKey().equals(recommendationItem.getKey())) {
                        adapter.replace(i, recommendation);
                        return;
                    }
                }
            }

            if (mVodPackageAdapterMap.containsKey(packageName)) {
                ArrayObjectAdapter adapter = mVodPackageAdapterMap.get(packageName);
                adapter.add(0, recommendation);
            } else {
                List<Recommendation> recommendations = new ArrayList<>();
                recommendations.add(recommendation);
                createVideoOnDemandRecommendationShelf(packageName, recommendations, null);
            }
            return;
        }
    }

    private void updateVideoOnDemandRecommendationShelves() {
        // Create VOD recommendation shelves if currently there are none and if recommendations are available
        if (mVodPackageShelfRowMap.isEmpty()) {
            if (mDashboardDataManager.areVodRecommendationsAvailable()) {
                createVideoOnDemandRecommendationShelves();
            }
            return;
        }

        // Remove all VOD recommendation shelves if no recommendation is available
        if (!mDashboardDataManager.areVodRecommendationsAvailable() && (!mDashboardDataManager.areVodPreviewRecommendationsAvailable())) {
            ArrayList<String> vodPackages = new ArrayList<>(mVodPackageShelfRowMap.keySet());
            int vodPackagesCount = vodPackages.size();
            for (int i = 0; i < vodPackagesCount; i++) {
                String vodPackage = vodPackages.get(i);
                ShelfRow row = mVodPackageShelfRowMap.remove(vodPackage);
                mVodPackageAdapterMap.remove(vodPackage);
                removeRow(row);
            }
            return;
        }
        // Create individual recommendation shelves if any recommendation-enabled app does not yet have a shelf
        Map<String, List<Recommendation>> vodRecommendationsMap = mDashboardDataManager.getVodRecommendations();
        if (vodRecommendationsMap != null) {
            ArrayList<String> vodPackages = new ArrayList<>(vodRecommendationsMap.keySet());
            int vodPackagesCount = vodPackages.size();
            for (int i = 0; i < vodPackagesCount; i++) {
                String vodPackage = vodPackages.get(i);
                if (!mVodPackageShelfRowMap.containsKey(vodPackage)) {
                    createVideoOnDemandRecommendationShelf(vodPackage, vodRecommendationsMap.get(vodPackage), null);
                }
            }
        }

        // Remove recommendation shelf for the package that is not in recommendation-enabled app list
        ArrayList<String> currentShownPackages = new ArrayList<>(mVodPackageShelfRowMap.keySet());
        int currentShownPackagesCount = currentShownPackages.size();
        for (int i = 0; i < currentShownPackagesCount; i++) {
            String pkg = currentShownPackages.get(i);
            if ((null!=vodRecommendationsMap) && !vodRecommendationsMap.containsKey(pkg)) {
                ShelfRow row = mVodPackageShelfRowMap.remove(pkg);
                mVodPackageAdapterMap.remove(pkg);
                removeRow(row);
            }
        }
        //Build preview program recommended channel shelves if available
        buildPreviewProgramRecommendationShelves();
    }

    private boolean isVodRecommendation(Recommendation recommendation) {
        boolean vodRecommendation = false;

        if (recommendation == null) {
            return vodRecommendation;
        }

        String[] contentTypes = recommendation.getContentType();
        if (contentTypes == null) {
            return vodRecommendation;
        }

        for (String contentType : contentTypes) {
            if (contentType.equals(Constants.CONTENT_TYPE_VOD_RECOMMENDATION)) {
                vodRecommendation = true;
                break;
            }
        }

        return vodRecommendation;
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }

    @Override
    public void onPreviewProgramChannelsAvailable(PreviewProgramsChannel previewProgramsChannel) {
        android.util.Log.d(TAG, "onPreviewProgramChannelsAvailable() contentType " + previewProgramsChannel.getCategory());
        if(isContentTypeVod(previewProgramsChannel)) {
            updateVideoOnDemandRecommendationShelves();
        }else{
            android.util.Log.d(TAG, "onPreviewProgramChannelsAvailable: not a VOD preview channel " + previewProgramsChannel.getCategory());
        }
    }

    @Override
    public void onPreviewProgramChannelsChanged(PreviewProgramsChannel previewProgramsChannel) {
        android.util.Log.d(TAG, "onPreviewProgramChannelsAvailable() contentType " + previewProgramsChannel.getCategory());
        if(isContentTypeVod(previewProgramsChannel)) {
            updateVideoOnDemandRecommendationShelves();
        }else{
           android.util.Log.d(TAG, "onPreviewProgramChannelsChanged: not a VOD preview channel " + previewProgramsChannel.getCategory());
        }
   }

    @Override
    public void onPreviewProgramChannelDeleted(PreviewProgramsChannel previewProgramsChannels) {

    }

    private boolean isContentTypeVod(PreviewProgramsChannel channel){
        return channel != null && channel.getCategory() == Constants.CHANNEL_CONTENT_TYPE_VOD_RECOMMENDATION;
    }
}
