package org.droidtv.defaultdashboard.ui.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.ui.fragment.ChapterFragment;
import org.droidtv.defaultdashboard.ui.view.VideoOnDemandChapterShelfItemView;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.DDBImageLoader;

import androidx.leanback.widget.Presenter;

/**
 * Created by bhargava.gugamsetty on 20-02-2018.
 */

public class VideoOnDemandShelfItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        VideoOnDemandChapterShelfItemView shelfItemView = new VideoOnDemandChapterShelfItemView(parent.getContext());
        return new VodShelfItemViewHolder(shelfItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {

        VodShelfItemViewHolder vodShelfItemViewHolder = (VodShelfItemViewHolder) viewHolder;
        if (item instanceof Recommendation) {
            Recommendation recommendation = (Recommendation) item;
            Bitmap logo = recommendation.getLogo();
            vodShelfItemViewHolder.mImageView.setContentDescription(recommendation.getTitle());
            /*Fetch the logo and set it to ImageView*/
            String logoUrl = recommendation.getLogoUrl();
            if (!TextUtils.isEmpty(logoUrl)) {
                Context context = vodShelfItemViewHolder.mImageView.getContext();
                Drawable placeHolder = context.getDrawable(R.drawable.vod_recommendation_item_place_holder);
                DDBImageLoader.fromUrl(logoUrl)
                        .placeHolder(placeHolder)
                        .inView(vodShelfItemViewHolder.mImageView)
                        .fetch();
                ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);
                // MyChoice lock icon should be shown if Apps are locked by MyChoice
                if (!facet.isHeaderShown() && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
                    vodShelfItemViewHolder.showMyChoiceBadge();
                } else {
                    vodShelfItemViewHolder.hideMyChoiceBadge();
                }
                return;
            }else if (logo != null) {
                vodShelfItemViewHolder.mImageView.setImageBitmap(logo);
                ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);
                // MyChoice lock icon should be shown if Apps are locked by MyChoice
                if (!facet.isHeaderShown() && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
                    DdbLogUtility.logVodChapter("VideoOnDemandShelfItemPresenter", "onBindViewHolder: showMyChoiceBadge");
                    vodShelfItemViewHolder.showMyChoiceBadge();
                } else {
                    DdbLogUtility.logVodChapter("VideoOnDemandShelfItemPresenter", "onBindViewHolder: hideMyChoiceBadge");
                    vodShelfItemViewHolder.hideMyChoiceBadge();
                }
                return;
            }
            vodShelfItemViewHolder.mImageView.setImageDrawable(null);

            String[] contentType = recommendation.getContentType();
            DdbLogUtility.logVodChapter("VideoOnDemandShelfItemPresenter", "onBindViewHolder() contentType[0] " + contentType[0]);
            if (contentType != null && Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION.equals(contentType[0])) {
                setLayoutParamsForOpenAppItem(vodShelfItemViewHolder);
                vodShelfItemViewHolder.mTextView.setVisibility(View.VISIBLE);
                vodShelfItemViewHolder.mTextView.setText(org.droidtv.ui.htvstrings.R.string.HTV_DDB_OPEN_APP);
                vodShelfItemViewHolder.mTextView.setBackgroundColor(vodShelfItemViewHolder.view.getContext().getColor(R.color.vod_chapter_open_app_background_color));
                vodShelfItemViewHolder.mImageView.setVisibility(View.GONE);
            } else {
                vodShelfItemViewHolder.mTextView.setVisibility(View.VISIBLE);
                vodShelfItemViewHolder.mTextView.setText("");//DUMMY text
                vodShelfItemViewHolder.mTextView.setBackgroundColor(vodShelfItemViewHolder.view.getContext().getColor(R.color.vod_chapter_open_app_background_color));
                vodShelfItemViewHolder.mImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        VodShelfItemViewHolder vodShelfItemViewHolder = (VodShelfItemViewHolder) viewHolder;
        resetLayoutParams(vodShelfItemViewHolder);
        vodShelfItemViewHolder.mImageView.setImageDrawable(null);
        vodShelfItemViewHolder.mImageView.setImageBitmap(null);
        vodShelfItemViewHolder.mImageView.setBackground(null);
        vodShelfItemViewHolder.mImageView.setVisibility(View.VISIBLE);
        vodShelfItemViewHolder.mTextView.setVisibility(View.GONE);
        vodShelfItemViewHolder.hideMyChoiceBadge();
    }

    private void setLayoutParamsForOpenAppItem(VodShelfItemViewHolder vodShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = vodShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = vodShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.open_app_item_width);
        layoutParams.height = vodShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.open_app_item_height);
        vodShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    private void resetLayoutParams(VodShelfItemViewHolder vodShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = vodShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        vodShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }


    public static final class VodShelfItemViewHolder extends ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public VodShelfItemViewHolder(View view) {
            super(view);
            VideoOnDemandChapterShelfItemView vodShelfItemView = (VideoOnDemandChapterShelfItemView) view;
            mImageView = vodShelfItemView.getShelfItemImageView();
            mTextView = vodShelfItemView.getShelfItemTextView();
        }

        private void showMyChoiceBadge() {
            VideoOnDemandChapterShelfItemView vodShelfItemView = (VideoOnDemandChapterShelfItemView) view;
            vodShelfItemView.showMyChoiceLockBadge();
        }

        private void hideMyChoiceBadge() {
            VideoOnDemandChapterShelfItemView vodShelfItemView = (VideoOnDemandChapterShelfItemView) view;
            vodShelfItemView.hideMyChoiceLockBadge();
        }
    }
}
