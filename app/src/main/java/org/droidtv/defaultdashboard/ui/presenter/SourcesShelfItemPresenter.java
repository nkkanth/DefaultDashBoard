/*
 * Copyright (c) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.droidtv.defaultdashboard.ui.presenter;

import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.fragment.ChapterFragment;
import org.droidtv.defaultdashboard.ui.view.SourcesShelfItemView;

public class SourcesShelfItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        SourcesShelfItemView sourcesShelfItemView = new SourcesShelfItemView(parent.getContext());
        TextView contentView = sourcesShelfItemView.getContentTextView();
        ImageView imageView = sourcesShelfItemView.getShelfItemImageView();
        return new SourcesShelfItemViewHolder(sourcesShelfItemView, contentView, imageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        SourcesShelfItemViewHolder sourcesShelfItemViewHolder = (SourcesShelfItemViewHolder) viewHolder;
        Source source = (Source) item;
        if(source == null) return;
        sourcesShelfItemViewHolder.mContent.setText(source.getHdmiDeviceLabel());
        sourcesShelfItemViewHolder.mImageView.setImageDrawable(source.getIcon());
        sourcesShelfItemViewHolder.mDisplayName = source.getHdmiDeviceLabel();
        ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);

        // MyChoice lock icon should be shown in the following conditions:
        // 1. If Source is MediaBrowser(USB) and MediaBrowser is Locked by MyChoice
        // 2. If Source is Google Cast and Google Cast is locked by MyChoice
        // 3. If Source is Airserver app and Apps are locked by MyChoice
        if (!facet.isHeaderShown() && ((Source.isMediaBrowser(source) && DashboardDataManager.getInstance().isMediaBrowserMyChoiceLocked()) ||
                (Source.isGoogleCast(source) && DashboardDataManager.getInstance().isGoogleCastMyChoiceLocked()) ||
                (Source.isAirserver(source) && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) ||
                DashboardDataManager.getInstance().isSourceMyChoiceLocked(source.getId()))) {
            DdbLogUtility.logRecommendationChapter("SourcesShelfItemPresenter", "showMyChoiceBadge");
            sourcesShelfItemViewHolder.showMyChoiceBadge();
        } else {
            DdbLogUtility.logRecommendationChapter("SourcesShelfItemPresenter", "hideMyChoiceBadge");
            sourcesShelfItemViewHolder.hideMyChoiceBadge();
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        SourcesShelfItemViewHolder sourcesShelfItemViewHolder = (SourcesShelfItemViewHolder) viewHolder;
        sourcesShelfItemViewHolder.mImageView.setImageDrawable(null);
        sourcesShelfItemViewHolder.hideMyChoiceBadge();
    }

    public static final class SourcesShelfItemViewHolder extends ViewHolder implements  View.OnFocusChangeListener {
        private TextView mContent;
        private ImageView mImageView;
        private String mDisplayName;

        private SourcesShelfItemViewHolder(View view, TextView content, ImageView imageView) {
            super(view);
            mContent = content;
            mImageView = imageView;
            view.setOnFocusChangeListener(this);
            view.setAccessibilityDelegate(new View.AccessibilityDelegate() {

                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setClassName("");
                    String ttsText = view.getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_PRESS_OK_TO_LAUNCH).replace("^1",mDisplayName);
                    info.setContentDescription(ttsText);
                }
            });
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                mContent.setVisibility(View.INVISIBLE);
            } else {
                mContent.setVisibility(View.VISIBLE);
            }
        }

        private void showMyChoiceBadge() {
            SourcesShelfItemView sourcesShelfItemView = (SourcesShelfItemView) view;
            sourcesShelfItemView.showMyChoiceLockBadge();
        }

        private void hideMyChoiceBadge() {
            SourcesShelfItemView sourcesShelfItemView = (SourcesShelfItemView) view;
            sourcesShelfItemView.hideMyChoiceLockBadge();
        }
    }
}