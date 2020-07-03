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

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AppLogoFetchListener;
import org.droidtv.defaultdashboard.data.model.AppInfo;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.fragment.ChapterFragment;
import org.droidtv.defaultdashboard.ui.view.AppsShelfItemView;

import androidx.leanback.widget.Presenter;

public class AppsShelfItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        AppsShelfItemView shelfItemView = new AppsShelfItemView(parent.getContext());
        return new AppsShelfItemViewHolder(shelfItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {

        AppsShelfItemViewHolder appsShelfItemViewHolder = (AppsShelfItemViewHolder) viewHolder;
        AppInfo appInfo = (AppInfo) item;
        appsShelfItemViewHolder.mPackageName = appInfo.getPackageName();
        DdbLogUtility.logAppsChapter("AppsShelfItemPresenter", "onBindViewHolder() called with: viewHolder = [" + viewHolder + "], packageName = [" + appInfo.getPackageName() + "]");
        ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);
        //appsShelfItemViewHolder.mImageView.setContentDescription(appInfo.getLabel());
		appsShelfItemViewHolder.mDisplayName = appInfo.getLabel();
        displayLogo(appInfo.getPackageName(), appsShelfItemViewHolder, !facet.isHeaderShown());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        AppsShelfItemViewHolder appsShelfItemViewHolder = (AppsShelfItemViewHolder) viewHolder;
        appsShelfItemViewHolder.clearImage();
        appsShelfItemViewHolder.hideMyChoiceBadge();
    }

    private static void displayLogo(String packageName, AppsShelfItemViewHolder viewHolder, boolean isRowExpanded) {
        DdbLogUtility.logAppsChapter("AppsShelfItemPresenter", "displayLogo() called with: packageName = [" + packageName + "], viewHolder = [" + viewHolder + "], isRowExpanded = [" + isRowExpanded + "]");
        // MyChoice lock icon should be shown if apps are locked by MyChoice
        if (isRowExpanded && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
            viewHolder.showMyChoiceBadge();
        } else {
            viewHolder.hideMyChoiceBadge();
        }

        DashboardDataManager.getInstance().fetchAppLogo(packageName, viewHolder);
    }

    public static final class AppsShelfItemViewHolder extends ViewHolder implements AppLogoFetchListener {
        private ImageView mImageView;
        private String mPackageName;
        private String mDisplayName;
        private Animation mFadeIn;

        private AppsShelfItemViewHolder(View view) {
            super(view);
            AppsShelfItemView appsShelfItemView = (AppsShelfItemView) view;
            mImageView = appsShelfItemView.getShelfItemImageView();
            mFadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.image_fade_in);
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

        private void animateImageFadeIn() {
            //TODO: Find a better way to animate fade in
            //mImageView.startAnimation(mFadeIn);
        }

        private void setImageResource(int resourceId) {
            animateImageFadeIn();
            mImageView.setImageResource(resourceId);
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

        private void showMyChoiceBadge() {
            AppsShelfItemView appsShelfItemView = (AppsShelfItemView) view;
            appsShelfItemView.showMyChoiceLockBadge();
        }

        private void hideMyChoiceBadge() {
            AppsShelfItemView appsShelfItemView = (AppsShelfItemView) view;
            appsShelfItemView.hideMyChoiceLockBadge();
        }

        @Override
        public void onAppLogoFetchComplete(String packageName, Drawable logo) {
            if (packageName == mPackageName) {
                if (logo != null) {
                    setImageDrawable(logo);
                }
            }
        }
    }
}
