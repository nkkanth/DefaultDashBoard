package org.droidtv.defaultdashboard.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.CastChapterView;
import org.droidtv.defaultdashboard.util.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.leanback.app.BrowseSupportFragment;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public class CastChapterFragment extends Fragment implements BrowseSupportFragment.MainFragmentAdapterProvider {

    private static final String TAG = CastChapterFragment.class.getSimpleName();
    private DashboardDataManager mDashboardDataManager;
    private CastChapterFragmentAdapter mMainFragmentAdapter;
    private CastChapterView mCastChapterView;

    private Drawable mDashboardFragmentBackgroundDrawable;
    private Drawable mDashboardFragmentForegroundDrawable;

    private View mBrowseContainerDock;

    // A flag to decide whether to show Cast screen after MyChoice is unlocked
    private boolean mShowCastInfo;

    private DashboardFragment mDashboardFragment = null;
    private UiThreadHandler mUiThreadHandler;

    public CastChapterFragment() {
        super();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mUiThreadHandler = new UiThreadHandler(this);
        mMainFragmentAdapter = new CastChapterFragmentAdapter(this);
        mMainFragmentAdapter.setScalingEnabled(false);

        mDashboardFragment = (DashboardFragment)getParentFragment();
        mShowCastInfo = false;
        DdbLogUtility.logCastChapter(TAG, "CastChapterFragment() mShowCastInfo " + mShowCastInfo);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mCastChapterView = (CastChapterView) inflater.inflate(R.layout.fragment_cast_chapter, container, false);
        Log.d("CastChapter" , "onCreateView called");
        return mCastChapterView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        DdbLogUtility.logCastChapter(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);
        mDashboardDataManager.setCastChapterFragment(true);
        getMainFragmentAdapter().getFragmentHost().notifyViewCreated(getMainFragmentAdapter());
        getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
        if (!mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_APPLY_BACKGROUND)) {
                mUiThreadHandler.sendEmptyMessage(UiThreadHandler.MSG_WHAT_APPLY_BACKGROUND );
        }
    }

    private void applyFragmentBackground(){
           if(mDashboardFragment == null){
               mDashboardFragment = (DashboardFragment)getParentFragment();
           }

           if(mDashboardFragment.getView() != null) {
               mBrowseContainerDock = mDashboardFragment.getView().findViewById(R.id.browse_container_dock);
               mDashboardFragmentForegroundDrawable = mBrowseContainerDock.getForeground();
               ColorDrawable foregroundDrawable = new ColorDrawable(DashboardDataManager.getInstance().getContext().getColor(R.color.transparent_overlay_color));
               mBrowseContainerDock.setForeground(foregroundDrawable);

               getMainFragmentAdapter().getFragmentHost().showTitleView(true);
               mDashboardFragment.hideTopMenuItems();
               applyCastBackground();
           }
        DdbLogUtility.logCastChapter(TAG, "CastChapterFragment()");
    }

    private void applyCastBackground() {
        BitmapDrawable bitmapDrawable = DashboardDataManager.getInstance().getSavedCastBitMapDrawable();
        if (bitmapDrawable != null && DashboardDataManager.getInstance().isValidBitmapDrawable(bitmapDrawable)) {
            mDashboardFragment.getView().setBackground(bitmapDrawable);
        } else {
            Drawable castChapterBackgroundDrawable;
            if(DashboardDataManager.getInstance().isBFLProduct()) {
                DdbLogUtility.logCastChapter(TAG,"applyCastBackground BFL");
                castChapterBackgroundDrawable = DashboardDataManager.getInstance().getContext().getDrawable(R.drawable.cast_chapter_background_image_bfl);
            }else{
                DdbLogUtility.logCastChapter(TAG,"applyCastBackground HFL");
                castChapterBackgroundDrawable = DashboardDataManager.getInstance().getContext().getDrawable(R.drawable.cast_chapter_background_image);
            }
            castChapterBackgroundDrawable.setTint(DashboardDataManager.getInstance().getContext().getColor(R.color.overlay_color));
            castChapterBackgroundDrawable.setTintMode(PorterDuff.Mode.SRC_OVER);
            mDashboardFragment.getView().setBackground(castChapterBackgroundDrawable);
            mDashboardDataManager.removeSavedImage(Constants.PATH_CAST_APP_SHARING_BACKGROUND, new DashboardDataManager.FileDataListener() {
                @Override
                public void onFileCopyComplete(boolean success) { }

                @Override
                public void onFileDeleteComplete(boolean success) { }
            });
        }
    }

    @Override
    public void onDestroy() {
        Log.d("CastChapter" , "onDestroy ");
        clearMessages();
        super.onDestroy();
        mDashboardFragment = null;
    }

    private void clearMessages(){
        if(mUiThreadHandler != null && mUiThreadHandler.hasMessages(UiThreadHandler.MSG_WHAT_APPLY_BACKGROUND)){
            mUiThreadHandler.removeMessages(UiThreadHandler.MSG_WHAT_APPLY_BACKGROUND);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(!mCastChapterView.isDeviceConnected()) {
            mDashboardDataManager.stopHotSpot();
        }
        mDashboardDataManager.setCastChapterFragment(false);
        if(mDashboardFragment != null) {
            mDashboardFragment.getView().setBackground(DashboardDataManager.getInstance().getSavedBitMapDrawable());

            if (mBrowseContainerDock == null) {
                mBrowseContainerDock = mDashboardFragment.getView().findViewById(R.id.browse_container_dock);
            }
            if (mBrowseContainerDock != null) {
                mBrowseContainerDock.setForeground(mDashboardFragmentForegroundDrawable);
            }
            mDashboardFragment.showTopMenuItems();
        }

        if (mCastChapterView != null) {
            mCastChapterView.cleanUp();
        }
        DdbLogUtility.logCastChapter(TAG, "onDestroyView() called");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DdbLogUtility.logCastChapter(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "] mShowCastInfo " + mShowCastInfo);
        if (requestCode == Constants.REQUEST_CODE_MYCHOICE_PIN_DIALOG) {
            if (resultCode == Activity.RESULT_CANCELED) {
                mShowCastInfo = false;
                mDashboardDataManager.showSidePanel();
                return;
            }

            if (mShowCastInfo) {
                if (mDashboardDataManager.isGoogleCastMyChoiceLocked()) {
                    mDashboardDataManager.showSidePanel();
                } else {
                    mCastChapterView.showCastInfo();
                }
                mShowCastInfo = false;
            }
            return;
        }
    }

    @Override
    public BrowseSupportFragment.MainFragmentAdapter getMainFragmentAdapter() {
        return mMainFragmentAdapter;
    }

    private void onSidePanelCollapseTransitionStarted() {
        if (mDashboardDataManager.isGoogleCastMyChoiceLocked()) {
            mShowCastInfo = true;
            showMyChoicePinDialogActivity();
        }
        mCastChapterView.showCastInfo();
    }

    private void onSidePanelExpandTransitionStarted() {
        mCastChapterView.hideCastInfo();
    }

    private void showMyChoicePinDialogActivity() {
        Intent launchMyChoiceIntent = new Intent(Constants.ACTION_MY_CHOICE_PIN);
        launchMyChoiceIntent.putIntegerArrayListExtra(Constants.EXTRA_MY_CHOICE_PIN_DIALOG_KEYS, new ArrayList<Integer>());
        startActivityForResult(launchMyChoiceIntent, Constants.REQUEST_CODE_MYCHOICE_PIN_DIALOG);
    }

    public static final class CastChapterFragmentAdapter extends BrowseSupportFragment.MainFragmentAdapter {
        public CastChapterFragmentAdapter(CastChapterFragment fragment) {
            super(fragment);
        }

        @Override
        public void onTransitionStart() {
            super.onTransitionStart();
            CastChapterFragment castChapterFragment = (CastChapterFragment) getFragment();
            DashboardFragment dashboardFragment = (DashboardFragment) castChapterFragment.getParentFragment();
            if (dashboardFragment != null) {
                boolean isShowingHeaders = dashboardFragment.isShowingHeaders();

                if (isShowingHeaders) {
                    castChapterFragment.onSidePanelExpandTransitionStarted();
                } else {
                    castChapterFragment.onSidePanelCollapseTransitionStarted();
                }
            }
        }

        @Override
        public void onTransitionEnd() {
            super.onTransitionEnd();

            CastChapterFragment castChapterFragment = (CastChapterFragment) getFragment();
            DashboardFragment dashboardFragment = (DashboardFragment) castChapterFragment.getParentFragment();
            if (dashboardFragment != null) {
                if (!dashboardFragment.isShowingHeaders()) {
                    getFragmentHost().showTitleView(false);
                } else {
                    getFragmentHost().showTitleView(true);
                }
            }
        }
    }

    private static class UiThreadHandler extends Handler {
        WeakReference<CastChapterFragment> mCastChapterFragmentRef;
        private static final int MSG_WHAT_APPLY_BACKGROUND = 100;

        private UiThreadHandler(CastChapterFragment castChapterFragment) {
            super();
            mCastChapterFragmentRef = new WeakReference<>(castChapterFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_WHAT_APPLY_BACKGROUND) {
                CastChapterFragment castChapterFragment = mCastChapterFragmentRef.get();
                if (castChapterFragment != null) {
                    castChapterFragment.applyFragmentBackground();
                }
                return;
            }
        }
    }
}



