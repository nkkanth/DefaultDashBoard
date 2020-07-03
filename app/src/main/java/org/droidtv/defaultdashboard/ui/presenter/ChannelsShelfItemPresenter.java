/*
 * Copyright (c) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed tochannel_name in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.droidtv.defaultdashboard.ui.presenter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelLogoFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ProgramDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ProgramThumbnailFetchListener;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.Program;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.fragment.ChapterFragment;
import org.droidtv.defaultdashboard.ui.view.ChannelsShelfItemView;

import java.lang.ref.WeakReference;

public class ChannelsShelfItemPresenter extends Presenter {

    private static final long PROGRAM_DATA_FETCH_DELAY_MS = 500;
    private static final int LOGO_ON = 1;

    private UiThreadHandler mUiThreadHandler;

    public ChannelsShelfItemPresenter() {
        mUiThreadHandler = new UiThreadHandler(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        DdbLogUtility.logTVChannelChapter("ChannelsShelfItemPresenter", "onCreateViewHolder: ");
        ChannelsShelfItemView shelfItemView = new ChannelsShelfItemView(parent.getContext());
        return new ChannelsShelfItemViewHolder(shelfItemView, mUiThreadHandler);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        ChannelsShelfItemViewHolder channelsShelfItemViewHolder = (ChannelsShelfItemViewHolder) viewHolder;
        if (item instanceof Channel) {
			
            Channel channel = (Channel) item;
            DdbLogUtility.logTVChannelChapter("ChannelsShelfItemPresenter", "onBindViewHolder " + channel.toString());
            channelsShelfItemViewHolder.mChannel = channel;
            ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);
            channelsShelfItemViewHolder.mIsHeaderShown = facet.isHeaderShown();
            channelsShelfItemViewHolder.mImageView.setContentDescription(channel.getDisplayName());
            channelsShelfItemViewHolder.mChannelNameView.setText(channel.getDisplayName());
            channelsShelfItemViewHolder.mDisplayName = channel.getDisplayName();
            displayLogo(channel, channelsShelfItemViewHolder);
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ChannelsShelfItemViewHolder channelsShelfItemViewHolder = (ChannelsShelfItemViewHolder) viewHolder;
        channelsShelfItemViewHolder.clearImage();
        channelsShelfItemViewHolder.hideLockBadge();
    }

    private static void displayLogo(Channel channel, ChannelsShelfItemViewHolder viewHolder) {
        // MyChoice lock icon should be shown in the following conditions:
        // 1. a non-TIF channel locked by MyChoice
        // 2. a TIF channel where Apps are locked by MyChoice
        viewHolder.mChannelNameView.setVisibility(View.VISIBLE);
        if (!viewHolder.mIsHeaderShown && (DashboardDataManager.getInstance().isChannelMyChoiceLocked(channel))) {
            viewHolder.showLockBadge(true);
        } else if (!viewHolder.mIsHeaderShown && channel.isScrambled()) {
            viewHolder.showLockBadge(false);
        } else {
            viewHolder.hideLockBadge();
        }

        if (Channel.isHdmiSource(channel)) {
		    viewHolder.setImageDrawable(DashboardDataManager.getInstance().getSourceIcon(viewHolder, channel));
            return;
        }

        if (Channel.isVgaSource(channel)) {
            viewHolder.setImageResource(org.droidtv.ui.tvwidget2k15.R.drawable.icon_206_vga_n_98x98);
            return;
        }

        DashboardDataManager.getInstance().fetchChannelLogo(channel, viewHolder);
    }

    private void fetchProgramData(Channel channel, ChannelsShelfItemViewHolder viewHolder) {
        DashboardDataManager.getInstance().fetchProgramDataForChannel(channel, viewHolder);
    }

    public static final class ChannelsShelfItemViewHolder extends ViewHolder implements ChannelLogoFetchListener, ProgramThumbnailFetchListener,
            ProgramDataListener, View.OnFocusChangeListener {
        private ImageView mImageView;
        private TextView mChannelNameView;
        private ProgressBar mProgressBar;
        private Channel mChannel;
        private Handler mHandler;
        private Animation mFadeIn;
        private boolean mIsHeaderShown;
        private String mDisplayName;

        private ChannelsShelfItemViewHolder(View view, Handler handler) {
            super(view);
            ChannelsShelfItemView channelsShelfItemView = (ChannelsShelfItemView) view;
            mImageView = channelsShelfItemView.getShelfItemImageView();
            mChannelNameView = channelsShelfItemView.getChannelNameTextView();
            mProgressBar = channelsShelfItemView.getProgressBar();
            mHandler = handler;
            view.setOnFocusChangeListener(this);
            mFadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.image_fade_in);
            view.setAccessibilityDelegate(new View.AccessibilityDelegate() {

                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setClassName("");
                    if(mDisplayName == null){
                        mDisplayName = "";
                    }
                    String ttsText = view.getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_PRESS_OK_TO_WATCH).replace("^1",mDisplayName);
                    info.setContentDescription(ttsText);
                }
            });
        }

        private void sendMessage(int what) {
            mHandler.removeMessages(what);
            Pair<Channel, ChannelsShelfItemViewHolder> pair = new Pair<>(mChannel, this);
            Message message = Message.obtain(mHandler, what, pair);
            mHandler.sendMessageDelayed(message, PROGRAM_DATA_FETCH_DELAY_MS);
        }

        private void hideProgramProgress() {
            mProgressBar.setVisibility(View.GONE);
        }

        private void showProgramProgress(int progress) {
            if (progress < 0) {
                mProgressBar.setProgress(0);
            } else if (progress > 100) {
                mProgressBar.setProgress(100);
            } else {
                mProgressBar.setProgress(progress);
            }
            animateProgressbarFadeInFadeIn();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        private int getProgramProgressPercent(Program program) {
            long startTime = program.getStartTime();
            long endTime = program.getEndTime();
            if (startTime < endTime) {
                long duration = endTime - startTime;
                return (int) (((System.currentTimeMillis() - startTime) * 100) / duration);
            }
            return 0;
        }

        private void showLockBadge(boolean isMyChoiceLocked) {
            ChannelsShelfItemView channelsShelfItemView = (ChannelsShelfItemView) view;
            channelsShelfItemView.showLockBadge(isMyChoiceLocked);
        }

        private void hideLockBadge() {
            ChannelsShelfItemView channelsShelfItemView = (ChannelsShelfItemView) view;
            channelsShelfItemView.hideLockBadge();
        }

        private void animateImageFadeIn() {
            //TODO: Find a better way to animate fade in
            //mImageView.startAnimation(mFadeIn);
        }

        private void animateProgressbarFadeInFadeIn() {
            mProgressBar.startAnimation(mFadeIn);
        }

        private void setImageResource(int resourceId) {
            animateImageFadeIn();
            mImageView.setImageResource(resourceId);
        }

        private void setImageBitmap(Bitmap bitmap) {
            animateImageFadeIn();
            mImageView.setImageBitmap(bitmap);
        }

        private void setImageDrawable(Drawable drawable) {
            animateImageFadeIn();
            mImageView.setImageDrawable(drawable);
        }

        private void clearImage() {
            mImageView.setImageDrawable(null);
            mImageView.setImageBitmap(null);
            mImageView.setBackground(null);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mChannelNameView.setVisibility(View.INVISIBLE);
                if (DashboardDataManager.getInstance().isEpgEnabled()) {
                    sendMessage(UiThreadHandler.MSG_WHAT_FETCH_PROGRAM_DATA);
                }
            } else {
                mChannelNameView.setVisibility(View.VISIBLE);
                hideProgramProgress();
                displayLogo(mChannel, this);
            }
        }

        @Override
        public void onChannelLogoFetchComplete(int channelId, Bitmap logo) {
            if (channelId == mChannel.getId()) {
                if (logo != null && DashboardDataManager.getInstance().areChannelLogosEnabled()) {
                    setImageBitmap(logo);
                    mChannelNameView.setVisibility(View.INVISIBLE);
                } else {
                    mChannelNameView.setText(mChannel.getDisplayName());
					if(view.hasFocus()){
                    mChannelNameView.setVisibility(View.INVISIBLE);
					}else{
						 mChannelNameView.setVisibility(View.VISIBLE);
					}
                    if (Channel.isRadioChannel(mChannel)) {
                        setImageResource(org.droidtv.ui.tvwidget2k15.R.drawable.icon_199_radio_n_98x98);
                    } else {
                        setImageResource(org.droidtv.ui.tvwidget2k15.R.drawable.icon_73_watch_tv_n_98x98);

                    }
                }
            }
        }


        @Override
        public void onProgramThumbnailFetchComplete(int programId, int channelId, Bitmap thumbnail) {
            if (channelId == mChannel.getId() && thumbnail != null && view.hasFocus()) {
                setImageBitmap(thumbnail);
            }
        }

        @Override
        public void onProgramDataFetchComplete(int channelId, Program program) {
            android.util.Log.d("ChannelsShelfItemPresenter", "onProgramDataFetchComplete: getFallbackStatusToEpg() " + mChannel.isFallbackToBcEpg());
            if (channelId == mChannel.getId() && program != null && view.hasFocus() && !mChannel.isFallbackToBcEpg()) {
                DashboardDataManager.getInstance().fetchProgramThumbnail(program, mChannel, this);

                showProgramProgress(getProgramProgressPercent(program));
            }
        }
    }

    private static final class UiThreadHandler extends Handler {

        private WeakReference<ChannelsShelfItemPresenter> mChannelsShelfItemPresenterRef;

        private static final int MSG_WHAT_FETCH_PROGRAM_DATA = 100;

        private UiThreadHandler(ChannelsShelfItemPresenter presenter) {
            mChannelsShelfItemPresenterRef = new WeakReference<ChannelsShelfItemPresenter>(presenter);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_FETCH_PROGRAM_DATA) {
                ChannelsShelfItemPresenter presenter = mChannelsShelfItemPresenterRef.get();
                if (presenter == null) {
                    return;
                }
                Pair<Channel, ChannelsShelfItemViewHolder> pair = (Pair<Channel, ChannelsShelfItemViewHolder>) msg.obj;
                Channel channel = pair.first;
                ChannelsShelfItemViewHolder itemViewHolder = pair.second;
                presenter.fetchProgramData(channel, itemViewHolder);
                return;
            }
        }
    }
}
