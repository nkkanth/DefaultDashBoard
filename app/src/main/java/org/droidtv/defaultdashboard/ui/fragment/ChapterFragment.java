package org.droidtv.defaultdashboard.ui.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.DashboardApplication;
import org.droidtv.defaultdashboard.common.CulLogger;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.util.AppUtil;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.tv.logger.ILogger.ChannelZapSource.HTVZapMethod;

import java.util.ArrayList;
import java.util.List;

import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.PresenterSelector;

import static org.droidtv.defaultdashboard.util.Constants.ACTION_MY_CHOICE_PIN;

//TODO: Uncommnet once ILogger interface will be available in Android-P

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public abstract class ChapterFragment extends RowsSupportFragment implements OnItemViewClickedListener, OnItemViewSelectedListener {

    private static final String TAG = "ChapterFragment";

    private static final int ACTIVITY_MOVE_TO_BACK_DELAY = 500;

    private DashboardDataManager mDashboardDataManager;
    private Handler mHandler;
    private Object mClickedItem;
    private HeaderVisibilityFacet mHeaderVisibilityFacet;
    private CulLogger mCulLogger;

    protected ArrayObjectAdapter mRowsAdapter;

    protected abstract void createRows();

    protected abstract PresenterSelector getChapterPresenterSelector();

    protected abstract String getLogTag();

    public ChapterFragment() {
        mDashboardDataManager = DashboardDataManager.getInstance();
        mHandler = new Handler();
        setOnItemViewClickedListener(this);
        setOnItemViewSelectedListener(this);
        mRowsAdapter = new ArrayObjectAdapter();
        mClickedItem = null;
        mHeaderVisibilityFacet = new HeaderVisibilityFacet();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMainFragmentAdapter().getFragmentHost().showTitleView(true);
        setPresenterSelector(getChapterPresenterSelector());
        setAdapter(mRowsAdapter);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //START: Fix for TF519PHINAMTK02-4598
                if (getActivity() != null || getContext() != null){
                    createRows();
                    getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
                }else{
                    Log.d(TAG, "onViewCreated: activty destroyed");
                }
                //END: Fix for TF519PHINAMTK02-4598
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (requestCode == Constants.REQUEST_CODE_MYCHOICE_PIN_DIALOG) {
            Object clickedItem = getClickedItem();
            if (resultCode == Activity.RESULT_CANCELED) {
                setClickedItem(null);
                return;
            }

            if (clickedItem != null) {
                if (clickedItem instanceof AppInfo) {  // App
                    if (!mDashboardDataManager.areAppsMyChoiceLocked()) {
                        AppInfo appInfo = (AppInfo) clickedItem;
                        performAppLaunch(appInfo);
                    }
                } else if (clickedItem instanceof Recommendation) {  // Recommendation
                    if (!mDashboardDataManager.areAppsMyChoiceLocked()) {
                        Recommendation recommendation = (Recommendation) clickedItem;
                        performRecommendationIntentAction(recommendation);

                        // The Recommendation should be cancelled when clicked if is auto-cancelable
                        if (recommendation.isAutoCancelTrue()) {
                            mDashboardDataManager.cancelRecommendation(recommendation);
                        }
                    }
                } else if (clickedItem instanceof Source) {  // Source (must be either Airserver or Cast; not any other source)
                    Source source = (Source) clickedItem;
                    if ((Source.AIRSERVER_SOURCE_INPUT_ID.equals(source.getId()) && !mDashboardDataManager.areAppsMyChoiceLocked()) ||
                            (Source.CAST_SOURCE_INPUT_ID.equals(source) && !mDashboardDataManager.isGoogleCastMyChoiceLocked())) {
                        performSourceTuning(source);
                    }
                }
                setClickedItem(null);
            }
            return;
        }
    }

    protected void addRow(ListRow row) {
        mRowsAdapter.add(row);
    }

    protected void addRow(int position, ListRow row) {
        mRowsAdapter.add(position, row);
    }

    protected void addRows(int position, List<ShelfRow> rows) {
        if (rows != null) {
            mRowsAdapter.addAll(position, rows);
        }
    }

    protected void replaceRow(int position, ListRow row) {
        mRowsAdapter.replace(position, row);
    }

    protected void notifyRowChanged(int rowIndex) {
        mRowsAdapter.notifyItemRangeChanged(rowIndex, 1);
    }

    protected void notifyRowsChanged(int rowIndex, int changedRowCount) {
        mRowsAdapter.notifyItemRangeChanged(rowIndex, changedRowCount);
    }

    protected void removeRow(ListRow row) {
        mRowsAdapter.remove(row);
    }

    protected void removeRows(List<ShelfRow> rows) {
        if (rows != null) {
            for (int i = rows.size() - 1; i >= 0; i--) {
                mRowsAdapter.remove(rows.get(i));
            }
        }
    }

    protected void removeRowItem(ListRow row, Object item) {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) row.getAdapter();
        adapter.remove(item);
    }

    protected void addRowItem(ListRow row, Object item) {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) row.getAdapter();
        adapter.add(item);
    }

    protected boolean isRowEmpty(ListRow row) {
        return row.getAdapter() == null || row.getAdapter().size() == 0;
    }

    protected int getRowCount() {
        return mRowsAdapter.size();
    }

    protected void onRecommendationClicked(Recommendation recommendation) {
        if (mDashboardDataManager.areAppsMyChoiceLocked()) {
            setClickedItem(recommendation);
            showMyChoicePinDialogActivity();
        } else {
            performRecommendationIntentAction(recommendation);
        }
    }

    protected void onAppClicked(AppInfo appInfo) {
        if (mDashboardDataManager.areAppsMyChoiceLocked()) {
            setClickedItem(appInfo);
            showMyChoicePinDialogActivity();
        } else {
            performAppLaunch(appInfo);
        }
    }

    protected void onChannelClicked(Channel channel) {
        android.util.Log.d(TAG, "onChannelClicked() called with: channel = " + channel);
        performTuneAction(channel);
    }

    protected void onSourceClicked(Source source) {
        if ((Source.AIRSERVER_SOURCE_INPUT_ID.equals(source.getId()) && mDashboardDataManager.areAppsMyChoiceLocked()) ||
                (Source.CAST_SOURCE_INPUT_ID.equals(source) && mDashboardDataManager.isGoogleCastMyChoiceLocked())) {
            setClickedItem(source);
            showMyChoicePinDialogActivity();
        } else {
            performSourceTuning(source);
        }
    }

    protected void onSmartInfoClicked(SmartInfo smartInfo) {
        // Smart info will not be locked by MyChoice. So, no MyChoice handling needs to be done here
        smartInfo.getAction().perform();
    }

    protected void performRecommendationIntentAction(Recommendation recommendation) {
        try {
            if(recommendation.getPendingIntent().isActivity()) {
                getContext().startActivityAsUser(recommendation.getPendingIntent().getIntent(), UserHandle.CURRENT_OR_SELF);
            }else{
                startRecommendationService(getContext(), recommendation.getPendingIntent());
            }
            // The Recommendation should be cancelled when clicked if is auto-cancelable
            if (recommendation.isAutoCancelTrue()) {
                mDashboardDataManager.cancelRecommendation(recommendation);
            }
        } catch (ActivityNotFoundException e) {
            Log.d(getLogTag(), "#### ActivityNotFoundException for recommendation:" + recommendation.getKey());
        }
    }

    private void startRecommendationService(Context c, PendingIntent pendingIntent) {
        if(pendingIntent.getIntent() == null) return;
        if(AppUtil.isTargetVersionO(c, pendingIntent.getIntent().getPackage())) {
            c.startForegroundServiceAsUser(pendingIntent.getIntent(), UserHandle.CURRENT_OR_SELF);
        }else{
            c.startServiceAsUser(pendingIntent.getIntent(), UserHandle.CURRENT_OR_SELF);
        }
    }

    protected void performAppLaunch(AppInfo appInfo) {
        if(mCulLogger != null && appInfo != null){
            mCulLogger.logAppLaunchTrigger(appInfo.getPackageName() , appInfo.getAppType());
        }
        PackageManager packageManager = getContext().getPackageManager();
        if(null == appInfo){
            Log.e(getTag(), "#### launch intent is null.package:" + appInfo);
            return;
        }
        Intent launchIntent = packageManager.getLeanbackLaunchIntentForPackage(appInfo.getPackageName());
        if (launchIntent == null) {
            launchIntent = packageManager.getLaunchIntentForPackage(appInfo.getPackageName());
        }
        if (launchIntent == null) {
            Log.e(getTag(), "#### launch intent is null.package:" + appInfo.getPackageName());
            return;
        }
        getContext().startActivityAsUser(launchIntent, UserHandle.CURRENT_OR_SELF);
    }

    protected void performChannelTuning(Channel channel) {
        int channelId = channel.getMappedId();
        Uri channelUri = TvContract.buildChannelUri(channelId);
        Intent intent = new Intent(Intent.ACTION_VIEW, channelUri);
        intent.setPackage("org.droidtv.playtv");
        intent.putExtra(Constants.EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(Constants.EXTRA_ZAP_METHOD, HTVZapMethod.CHANNEL_MATRIX.ordinal());
        /*CR TF518PHIEUMTK08-243 - START*/
        intent.putExtra(Constants.EXTRA_METHOD_ID, Constants.EXTRA_VALUE_CHANNEL_LIST);
        intent.putExtra(Constants.EXTRA_UI_NAME, getUiName(channel));
        intent.putExtra(Constants.EXTRA_TUNE_FROM, Constants.EXTRA_TUNE_VALUE);
        /*CR TF518PHIEUMTK08-243 - END*/
        try {
            getContext().startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        }catch (ActivityNotFoundException e){
            Log.d(TAG, "performSourceTuning: ActivityNotFoundException " + Intent.ACTION_VIEW);
        }
    }

    private String getUiName(Channel channel) {
        String uiName = "Watch TV";
        if(Channel.isTifChannel(channel)){
            uiName = "Third party TIF";
        }
        return uiName;
    }

    protected void performSourceTuning(Source source) {
        android.util.Log.d(TAG, "performSourceTuning: " + source);
        if( source == null) return;
        Intent intent = source.getLaunchIntent();
        if (intent != null) {
            /*CR TF518PHIEUMTK08-243 - START*/
            intent.putExtra(Constants.EXTRA_METHOD_ID, Constants.EXTRA_VALUE_CHANNEL_LIST);
            intent.putExtra(Constants.EXTRA_UI_NAME, source.getLabel());
            intent.putExtra(Constants.EXTRA_TUNE_FROM, Constants.EXTRA_TUNE_VALUE);
            /*CR TF518PHIEUMTK08-243 - END*/
            try {
                getContext().startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
            }catch (ActivityNotFoundException e){
                Log.d(TAG, "performSourceTuning: ActivityNotFoundException");
            }
        }
    }

    protected void performTuneAction(Channel channel) {
        if (Channel.isHdmiSource(channel) || Channel.isVgaSource(channel)) {
            performSourceTuning(mDashboardDataManager.getSource(channel.getInputId()));
            return;
        }
        performChannelTuning(channel);
    }

    protected void showMyChoicePinDialogActivity() {
        Intent launchMyChoiceIntent = new Intent(ACTION_MY_CHOICE_PIN);
        launchMyChoiceIntent.putIntegerArrayListExtra(Constants.EXTRA_MY_CHOICE_PIN_DIALOG_KEYS, new ArrayList<Integer>());
        startActivityForResult(launchMyChoiceIntent, Constants.REQUEST_CODE_MYCHOICE_PIN_DIALOG);
    }

    protected void setClickedItem(Object item) {
        mClickedItem = item;
    }

    protected Object getClickedItem() {
        return mClickedItem;
    }

    protected HeaderVisibilityFacet getHeaderVisibilityFacet() {
        return mHeaderVisibilityFacet;
    }

    public class HeaderVisibilityFacet {
        public boolean isHeaderShown() {
            return ((DashboardFragment) getParentFragment()).isShowingHeaders();
        }
    }
}
