package org.droidtv.defaultdashboard.ui.presenter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.ui.fragment.ChapterFragment;
import org.droidtv.defaultdashboard.ui.view.AppRecommendationShelfItemView;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.DDBImageLoader;

import androidx.leanback.widget.Presenter;

import java.net.URI;
import java.util.Objects;

/**
 * Created by bhargava.gugamsetty on 20-02-2018.
 */

public class AppRecommendationShelfItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        DdbLogUtility.logAppsChapter("AppRecommendationShelfItemPresenter", "onCreateViewHolder() called with: parent = [" + parent + "]");
        AppRecommendationShelfItemView shelfItemView = new AppRecommendationShelfItemView(parent.getContext());
        return new AppRecommendationShelfItemViewHolder(shelfItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        AppRecommendationShelfItemViewHolder appRecommendationShelfItemViewHolder = (AppRecommendationShelfItemViewHolder) viewHolder;
        if (item instanceof Recommendation) {
            Recommendation recommendation = (Recommendation) item;
			appRecommendationShelfItemViewHolder.mImageView.setContentDescription(recommendation.getTitle());
            String logo = recommendation.getLogoUrl();
            if (!TextUtils.isEmpty(logo)) {
                Context context = appRecommendationShelfItemViewHolder.mImageView.getContext();
				appRecommendationShelfItemViewHolder.mImageView.setTag(Integer.toString(recommendation.getId()));
                Drawable placeHolder = context.getDrawable(R.drawable.app_recommendation_item_place_holder);
                Uri uri = Uri.parse(logo);
                if(Objects.equals(uri.getScheme(), ContentResolver.SCHEME_ANDROID_RESOURCE)){
                    appRecommendationShelfItemViewHolder.mImageView.setImageURI(uri);
                    return;
                }
                DDBImageLoader.fromUrl(logo)
                        .setEventId(recommendation.getId())
                        .placeHolder(placeHolder)
                        .inView(appRecommendationShelfItemViewHolder.mImageView)
                        .fetch();
                ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);

                // MyChoice lock icon should be shown if apps are locked by MyChoice
                if (!facet.isHeaderShown() && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
                    DdbLogUtility.logAppsChapter("AppRecommendationShelfItemPresenter", "onBindViewHolder: showMyChoiceBad3ge");
                    appRecommendationShelfItemViewHolder.showMyChoiceBadge();
                } else {
                    DdbLogUtility.logAppsChapter("AppRecommendationShelfItemPresenter", "onBindViewHolder: hideMyChoiceBadge");
                    appRecommendationShelfItemViewHolder.hideMyChoiceBadge();
                }
                return;
            } else if(recommendation.getLogo() != null) {
                appRecommendationShelfItemViewHolder.mImageView.setImageBitmap(recommendation.getLogo());
                ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);
                if (!facet.isHeaderShown() && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
                    appRecommendationShelfItemViewHolder.showMyChoiceBadge();
                } else {
                    appRecommendationShelfItemViewHolder.hideMyChoiceBadge();
                }
                return;
            }
            appRecommendationShelfItemViewHolder.mImageView.setImageDrawable(null);

            String[] contentType = recommendation.getContentType();
            if ((contentType != null) && (contentType.length > 0) && Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION.equals(contentType[0])) {
                setLayoutParamsForOpenAppItem(appRecommendationShelfItemViewHolder);
                appRecommendationShelfItemViewHolder.mTextView.setVisibility(View.VISIBLE);
                appRecommendationShelfItemViewHolder.mTextView.setText(org.droidtv.ui.htvstrings.R.string.HTV_DDB_OPEN_APP);
                appRecommendationShelfItemViewHolder.mImageView.setVisibility(View.GONE);
            } else if ((contentType != null) && (contentType.length > 0) && Constants.CONTENT_TYPE_APP_RECOMMENDATION.equals(contentType[0])) {
                appRecommendationShelfItemViewHolder.mTextView.setVisibility(View.INVISIBLE);
            }else{
                appRecommendationShelfItemViewHolder.mTextView.setVisibility(View.GONE);
                appRecommendationShelfItemViewHolder.mImageView.setImageDrawable(null);
            }
        }
    }

    private void setLayoutParamsForOpenAppItem(ViewHolder appRecommendationShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = appRecommendationShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = appRecommendationShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.open_app_item_width);
        layoutParams.height = appRecommendationShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.open_app_item_height);
        appRecommendationShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        AppRecommendationShelfItemViewHolder appRecommendationShelfItemViewHolder = (AppRecommendationShelfItemViewHolder) viewHolder;
        String tag = (String) appRecommendationShelfItemViewHolder.mImageView.getTag();
        DDBImageLoader.cleanupReference(tag);
        resetLayoutParams(appRecommendationShelfItemViewHolder);
        appRecommendationShelfItemViewHolder.mImageView.setImageDrawable(null);
        appRecommendationShelfItemViewHolder.mImageView.setImageBitmap(null);
        appRecommendationShelfItemViewHolder.mImageView.setVisibility(View.VISIBLE);
        appRecommendationShelfItemViewHolder.mTextView.setVisibility(View.GONE);
        appRecommendationShelfItemViewHolder.hideMyChoiceBadge();
    }

    private void resetLayoutParams(ViewHolder appRecommendationShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = appRecommendationShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        appRecommendationShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    public static final class AppRecommendationShelfItemViewHolder extends ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public AppRecommendationShelfItemViewHolder(View view) {
            super(view);
            AppRecommendationShelfItemView appRecommendationShelfItemView = (AppRecommendationShelfItemView) view;
            mImageView = appRecommendationShelfItemView.getShelfItemImageView();
            mTextView = appRecommendationShelfItemView.getShelfItemTextView();
        }

        private void showMyChoiceBadge() {
            AppRecommendationShelfItemView appRecommendationShelfItemView = (AppRecommendationShelfItemView) view;
            appRecommendationShelfItemView.showMyChoiceLockBadge();
        }

        private void hideMyChoiceBadge() {
            AppRecommendationShelfItemView appRecommendationShelfItemView = (AppRecommendationShelfItemView) view;
            appRecommendationShelfItemView.hideMyChoiceLockBadge();
        }
    }

    public void onViewDetachedFromWindow(Presenter.ViewHolder holder){
        AppRecommendationShelfItemViewHolder appRecommendationShelfItemViewHolder = (AppRecommendationShelfItemViewHolder) holder;
        Object key = appRecommendationShelfItemViewHolder.mImageView.getTag();
        if(key != null) {
            DDBImageLoader.cleanupReference(key.toString());
        }
    }
}
