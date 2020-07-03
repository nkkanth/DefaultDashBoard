package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.FileDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ImageFile;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ImageFileFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ThumbnailDataListener;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.adapter.ThumbnailBrowserAdapter;
import org.droidtv.defaultdashboard.ui.view.ThumbnailView;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.ui.tvwidget2k15.tvtoast.TvToast;
import org.droidtv.ui.tvwidget2k15.tvtoast.TvToastMessenger;

import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.leanback.widget.VerticalGridView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
/**
 * Created by bhargava.gugamsetty on 28-12-2017.
 */

public abstract class ThumbnailBrowserNode extends OptionsNode implements ThumbnailBrowserAdapter.ThumbnailClickListener {

    private static final String TAG = "ThumbnailBrowserNode";

    private static final int NUM_THUMBNAIL_COLUMNS = 3;

    private VerticalGridView mThumbnailListVerticalGridView;
    private ThumbnailBrowserAdapter mThumbnailAdapter;
    private ProgressBar mProgressBar;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private View mCurrentSelectedView;
    private String mCurrentAppliedImagePath;

    private TvToastMessenger mTvToastMessenger;
    private TvToast mUsbDiskUnavailableTvToast;

    protected boolean mHasConfigurationSessionChanges;

    public ThumbnailBrowserNode(Context context) {
        this(context, null);
    }

    public ThumbnailBrowserNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbnailBrowserNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mTvToastMessenger = TvToastMessenger.getInstance(context.getApplicationContext());
        mUsbDiskUnavailableTvToast = TvToastMessenger.makeTvToastMessage(TvToastMessenger.TYPE_TIME_OUT,
                context.getString(R.string.MAIN_MSG_NO_USB_DEVICE_CONNECTED), -1);
        mCurrentAppliedImagePath = null;
        mHasConfigurationSessionChanges = false;
    }

    @Override
    public void onThumbnailClick(String imageFilePath) {
        setCurrentAppliedImagePath(imageFilePath);
        // Some thumbnail has been applied. So we'll have to indicate that there is a dashboard configuration change in this session
        mHasConfigurationSessionChanges = true;
    }

    @Override
    public void onThumbnailClick(int drawableResourceId) {
        setCurrentAppliedImagePath("");
        // Some thumbnail has been applied. So we'll have to indicate that there is a dashboard configuration change in this session
        mHasConfigurationSessionChanges = true;
    }

    public void onNodeEntered() {
        registerUsbEventsReceiver();
    }

    public void onNodeExited() {
        unregisterUsbEventsReceiver();
        stopFetchingThumbnails();
        clearThumbnails();
    }

    public void onDismissed() {
        saveCurrentAppliedImage();
        onNodeExited();
    }

    public boolean hasConfigurationSessionChanges() {
        return mHasConfigurationSessionChanges;
    }

    public void resetConfigurationSessionChanges() {
        mHasConfigurationSessionChanges = false;
    }

    protected abstract String getSavedImagePath();

    protected abstract int getDefaultImageDrawableResource();

    protected abstract int getDefaultImageWidth();

    protected abstract int getDefaultImageHeight();

    @Override
    protected RelativeLayout loadView() {
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_thumbnail_browser, null);

        mThumbnailListVerticalGridView = (VerticalGridView) root.findViewById(R.id.thumbnail_list_vertical_grid_view);
        mThumbnailListVerticalGridView.setNumColumns(NUM_THUMBNAIL_COLUMNS);
        mThumbnailAdapter = new ThumbnailBrowserAdapter(getContext().getApplicationContext(), getDefaultImageDrawableResource(), getDefaultImageWidth(), getDefaultImageHeight());
        mThumbnailAdapter.setThumbnailClickListener(this);
        mThumbnailListVerticalGridView.setAdapter(mThumbnailAdapter);
        mThumbnailListVerticalGridView.setOnChildViewHolderSelectedListener(mOnChildViewHolderSelectedListener);
        mThumbnailListVerticalGridView.setDrawingCacheEnabled(false);

        mProgressBar = (ProgressBar) root.findViewById(R.id.media_scan_progress_bar);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.default_scale, typedValue, true);
        mDefaultScale = typedValue.getFloat();
        getResources().getValue(R.dimen.focus_scale, typedValue, true);
        mFocusScale = typedValue.getFloat();

        mDefaultElevation = getResources().getDimension(R.dimen.default_elevation);
        mFocusElevation = getResources().getDimension(R.dimen.focus_elevation);

        mCurrentSelectedView = null;

        populateThumbnailOfSavedImage();
        if (!DashboardDataManager.getInstance().isMediaScannerInProgress()) {
            populateThumbnailsFromUsb();
        } else {
            showWaitingAnimation();
        }
        return root;
    }

    private OnChildViewHolderSelectedListener mOnChildViewHolderSelectedListener = new OnChildViewHolderSelectedListener() {
        @Override
        public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subposition) {
            if (child == null) {
                return;
            }

            if (mCurrentSelectedView != null) {
                ThumbnailView currentSelectedThumbnailView = (ThumbnailView) mCurrentSelectedView;
                scaleAndElevate(currentSelectedThumbnailView, mDefaultScale, mDefaultElevation);
                currentSelectedThumbnailView.hideMetadata();
            }

            ThumbnailView thumbnailView = (ThumbnailView) child.itemView;
            scaleAndElevate(child.itemView, mFocusScale, mFocusElevation);
            thumbnailView.showMetadata();
            mCurrentSelectedView = thumbnailView;
        }
    };

    private void registerUsbEventsReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.LOCAL_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Constants.LOCAL_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Constants.LOCAL_ACTION_USB_BREAKOUT);
        LocalBroadcastManager.getInstance(getContext().getApplicationContext()).registerReceiver(mUsbBroadcastReceiver, intentFilter);
    }

    private void unregisterUsbEventsReceiver() {
        try {
            LocalBroadcastManager.getInstance(getContext().getApplicationContext()).unregisterReceiver(mUsbBroadcastReceiver);
        } catch (Exception e) {
            Log.w(TAG, "exception when unregistering USB events receiver");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(200).scaleX(scale);
        view.animate().setDuration(200).scaleY(scale);
        view.setElevation(elevation);
    }

    private void setCurrentAppliedImagePath(String path) {
        mCurrentAppliedImagePath = path;
    }

    protected String getCurrentAppliedImagePath() {
        return mCurrentAppliedImagePath;
    }

    private void saveCurrentAppliedImage() {
        // If there is no image change in the curresnt session (or a 'revert session'has been done) then do not save any selected image
        if(!hasConfigurationSessionChanges()){
            return;
        }

        if (TextUtils.isEmpty(getCurrentAppliedImagePath())) {
            DashboardDataManager.getInstance().removeSavedImage(getSavedImagePath(), null);
            return;
        }

        if (getCurrentAppliedImagePath().startsWith(getSavedImagePath())) {
            return;
        }

        // Remove any saved images before saving a new one
        DashboardDataManager.getInstance().removeSavedImage(getSavedImagePath(), new FileDataListener() {
            @Override
            public void onFileCopyComplete(boolean success) {

            }

            @Override
            public void onFileDeleteComplete(boolean success) {
                if (!TextUtils.isEmpty(getCurrentAppliedImagePath())) {
                    DashboardDataManager.getInstance().saveImage(getCurrentAppliedImagePath(), getSavedImagePath(), null);
                }
            }
        });
    }

    public void populateThumbnailsFromUsb() {
        DashboardDataManager.getInstance().fetchThumbnailsFromUsb(mThumbnailDataListener);
    }

    private void populateThumbnailOfSavedImage() {
        DashboardDataManager.getInstance().fetchSavedImageFile(getSavedImagePath(), mImageFileFetchListener);
    }

    private Handler mHandler = new Handler();

    private ThumbnailDataListener mThumbnailDataListener = new ThumbnailDataListener() {
        @Override
        public void onAvailableImagesFetched(Cursor cursor) {
            if (!DashboardDataManager.getInstance().isUsbConnected() && (cursor == null || cursor.getCount() == 0)) {
                DdbLogUtility.logCommon(TAG, "isUsbConnected() = " + DashboardDataManager.getInstance().isUsbConnected());
                showNoUsbDiskToast();
                return;
            }
			DdbLogUtility.logMoreChapter(TAG, "onAvailableImagesFetched count : " + cursor.getCount());
            removeWaitingAnimation();
            mThumbnailAdapter.clear();
            mThumbnailAdapter.setCursor(cursor);
            mThumbnailAdapter.notifyDataSetChanged();
        }
    };

    private ImageFileFetchListener mImageFileFetchListener = new ImageFileFetchListener() {
        @Override
        public void onImageFileFetched(ImageFile imageFile) {
            // Update current applied image path only when it is not null. This will ensure that the value is not overwritten when navigating
            // from this node to previous node and then back to this node.

            // The current applied image path can only be null when this node is created/constructed afresh.
            // In all other cases it can either contain the path to the selected image on USB or
            // be empty (in which case it is understood that the default image has been selected)
            if (getCurrentAppliedImagePath() == null) {
                setCurrentAppliedImagePath((imageFile != null && imageFile.getFile() != null) ? imageFile.getFile().getPath() : null);
            }
            mThumbnailAdapter.setSavedImageFile(imageFile);
            mThumbnailAdapter.notifyItemInserted(ThumbnailBrowserAdapter.SAVED_IMAGE_THUMBNAIL_POSITION);
            if (imageFile != null && imageFile.getFile() != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mThumbnailListVerticalGridView.setSelectedPosition(ThumbnailBrowserAdapter.SAVED_IMAGE_THUMBNAIL_POSITION);
                    }
                });
            }
        }
    };

    private void clearThumbnails() {
        if (mThumbnailAdapter != null) {
            mThumbnailAdapter.clear();
            mThumbnailAdapter.notifyDataSetChanged();
        }
    }

    private void stopFetchingThumbnails() {
        DashboardDataManager.getInstance().stopFetchingThumbnailsFromUsb();
    }

    private void showNoUsbDiskToast() {
        mTvToastMessenger.showTvToastMessage(mUsbDiskUnavailableTvToast);
    }

    private void showWaitingAnimation() {
        if (!mProgressBar.isShown()) {
            mProgressBar.setVisibility(VISIBLE);
        }
    }

    private void removeWaitingAnimation() {
        if (mProgressBar.isShown()) {
            mProgressBar.setVisibility(GONE);
        }
    }

    private BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constants.LOCAL_MEDIA_SCANNER_STARTED.equals(action)) {
                showWaitingAnimation();
                return;
            }

            if (Constants.LOCAL_MEDIA_SCANNER_FINISHED.equals(action)) {
                if (!DashboardDataManager.getInstance().isMediaScannerInProgress()) {
                    removeWaitingAnimation();
                }
                populateThumbnailsFromUsb();
                return;
            }

            if (Constants.LOCAL_ACTION_USB_BREAKOUT.equals(action)) {
                stopFetchingThumbnails();
                clearThumbnails();
                showNoUsbDiskToast();
                return;
            }
        }
    };
}
