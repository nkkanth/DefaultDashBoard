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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ThumbnailBitmapFetchListener;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.ui.view.SmartInfoShelfItemView;

import androidx.leanback.widget.Presenter;

public class SmartInfoShelfItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        DdbLogUtility.logRecommendationChapter("SmartInfoShelfItemPresenter", "onCreateViewHolder() called with: parent = [" + parent + "]");
        SmartInfoShelfItemView shelfItemView = new SmartInfoShelfItemView(parent.getContext());
        return new SmartInfoShelfItemViewHolder(shelfItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
		String mTitle = null;
        DdbLogUtility.logRecommendationChapter("SmartInfoShelfItemPresenter", "onBindViewHolder() called with: viewHolder = [" + viewHolder + "], item = [" + item + "]");
        if (item == null) {
            return;
        }

        SmartInfoShelfItemViewHolder smartInfoShelfItemViewHolder = (SmartInfoShelfItemViewHolder) viewHolder;
        if (item instanceof Recommendation) {
            Recommendation recommendation = (Recommendation) item;
            Bitmap logo = recommendation.getLogo();
            smartInfoShelfItemViewHolder.mImageView.setContentDescription(recommendation.getTitle());
            mTitle = recommendation.getTitle();
            if (logo != null &&(!logo.isRecycled())) {
                smartInfoShelfItemViewHolder.mImageView.setImageBitmap(logo);
                smartInfoShelfItemViewHolder.mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return;
            }

            Drawable appIcon = DashboardDataManager.getInstance().getSmartInfoIcon();
            if (appIcon != null) {
                smartInfoShelfItemViewHolder.mImageView.setImageDrawable(appIcon);
                smartInfoShelfItemViewHolder.mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return;
            }

            smartInfoShelfItemViewHolder.mImageView.setImageResource(R.drawable.icon_304_smart_info_n_54x54);
            smartInfoShelfItemViewHolder.mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            SmartInfo smartInfo = (SmartInfo) item;
            smartInfoShelfItemViewHolder.mId = smartInfo.getId();
            String smartInfoIconPath = smartInfo.getIconPath();
            if (TextUtils.isEmpty(smartInfoIconPath)) {
                smartInfoShelfItemViewHolder.mImageView.setImageResource(R.drawable.icon_304_smart_info_n_54x54);
                smartInfoShelfItemViewHolder.mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                DashboardDataManager.getInstance().fetchSmartInfoImageFromRelativePath(smartInfoIconPath, smartInfoShelfItemViewHolder.mId
                        , 0, viewHolder.view.getContext().getResources().getDimensionPixelSize(R.dimen.smart_info_item_height),
                        smartInfoShelfItemViewHolder);
            }
            mTitle = smartInfo.getTitle();
        }
        smartInfoShelfItemViewHolder.setTitle(mTitle);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        SmartInfoShelfItemViewHolder smartInfoShelfItemViewHolder = (SmartInfoShelfItemViewHolder) viewHolder;
        smartInfoShelfItemViewHolder.mImageView.setImageResource(R.drawable.icon_304_smart_info_n_54x54);
        smartInfoShelfItemViewHolder.mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    public static final class SmartInfoShelfItemViewHolder extends ViewHolder implements ThumbnailBitmapFetchListener {
        private ImageView mImageView;
        private long mId;
        private String mTitle;

        private SmartInfoShelfItemViewHolder(final View view) {
            super(view);
            SmartInfoShelfItemView smartInfoShelfItemView = (SmartInfoShelfItemView) view;
            mImageView = smartInfoShelfItemView.getShelfItemImageView();
            view.setAccessibilityDelegate(new View.AccessibilityDelegate() {

                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setClassName("");                  
                    info.setContentDescription(mTitle == null ? "" : mTitle);
                }
            });
        }

        @Override
        public void onThumbnailBitmapFetchComplete(long id, Bitmap bitmap) {
            if (id == mId) {
                if (bitmap != null && (!bitmap.isRecycled())) {
                    mImageView.setImageBitmap(bitmap);
                    mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    mImageView.setImageResource(R.drawable.icon_304_smart_info_n_54x54);
                    mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
            }
        }

        private void setTitle(String title) {
            mTitle = title;
        }
    }
}
