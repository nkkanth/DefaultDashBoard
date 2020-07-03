package org.droidtv.defaultdashboard.ui.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.ui.fragment.ChapterFragment;
import org.droidtv.defaultdashboard.ui.view.GamesChapterShelfItemView;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.DDBImageLoader;

import androidx.leanback.widget.Presenter;

/**
 * Created by bhargava.gugamsetty on 20-02-2018.
 */

public class GamesChapterShelfItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        GamesChapterShelfItemView shelfItemView = new GamesChapterShelfItemView(parent.getContext());
        return new GamesShelfItemViewHolder(shelfItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        GamesShelfItemViewHolder gamesShelfItemViewHolder = (GamesShelfItemViewHolder) viewHolder;
        if (item instanceof Recommendation) {
            Recommendation recommendation = (Recommendation) item;
            Bitmap logo = recommendation.getLogo();
            DdbLogUtility.logGamesChapter("GamesChapterShelfItemPresenter", "onBindViewHolder: logo " +logo);
            gamesShelfItemViewHolder.mImageView.setContentDescription(recommendation.getTitle());
            gamesShelfItemViewHolder.mDisplayName = recommendation.getTitle();
            String logoUrl = recommendation.getLogoUrl();
            if(!TextUtils.isEmpty(logoUrl)){
                Context context = gamesShelfItemViewHolder.mImageView.getContext();
                Drawable placeHolder = context.getDrawable(R.drawable.app_recommendation_item_place_holder);
                DDBImageLoader.fromUrl(logoUrl)
                        .placeHolder(placeHolder)
                        .inView(gamesShelfItemViewHolder.mImageView)
                        .fetch();
                ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);

                // MyChoice lock icon should be shown if apps are locked by MyChoice
                if (!facet.isHeaderShown() && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
                    gamesShelfItemViewHolder.showMyChoiceBadge();
                } else {
                    gamesShelfItemViewHolder.hideMyChoiceBadge();
                }
                return;
            }else if (logo != null) {
                gamesShelfItemViewHolder.mImageView.setImageBitmap(logo);

                ChapterFragment.HeaderVisibilityFacet facet = (ChapterFragment.HeaderVisibilityFacet) getFacet(ChapterFragment.HeaderVisibilityFacet.class);


                // MyChoice lock icon should be shown if Apps are locked by MyChoice
                if (!facet.isHeaderShown() && DashboardDataManager.getInstance().areAppsMyChoiceLocked()) {
                    gamesShelfItemViewHolder.showMyChoiceBadge();
                } else {
                    gamesShelfItemViewHolder.hideMyChoiceBadge();
                }

                return;
            }
            gamesShelfItemViewHolder.mImageView.setImageDrawable(null);

            String[] contentType = recommendation.getContentType();
            DdbLogUtility.logGamesChapter("GamesChapterShelfItemPresenter", "onBindViewHolder contentType[0] " + contentType[0]);
            if (contentType != null && Constants.CONTENT_TYPE_EMPTY_RECOMMENDATION.equals(contentType[0])) {
                setLayoutParamsForOpenAppItem(gamesShelfItemViewHolder);
                gamesShelfItemViewHolder.mTextView.setVisibility(View.VISIBLE);
                gamesShelfItemViewHolder.mTextView.setText(org.droidtv.ui.htvstrings.R.string.HTV_DDB_OPEN_APP);
                gamesShelfItemViewHolder.mTextView.setBackgroundColor(gamesShelfItemViewHolder.view.getContext().getColor(R.color.vod_chapter_open_app_background_color));
                gamesShelfItemViewHolder.mImageView.setVisibility(View.GONE);
            } else {
                gamesShelfItemViewHolder.mTextView.setVisibility(View.VISIBLE);
                gamesShelfItemViewHolder.mTextView.setBackgroundColor(gamesShelfItemViewHolder.view.getContext().getColor(R.color.vod_chapter_open_app_background_color));
                gamesShelfItemViewHolder.mTextView.setText("");
                gamesShelfItemViewHolder.mImageView.setVisibility(View.GONE);
            }
        }
    }

    private void setLayoutParamsForOpenAppItem(GamesShelfItemViewHolder gamesShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = gamesShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = gamesShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.open_app_item_width);
        layoutParams.height = gamesShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.open_app_item_height);
        gamesShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        GamesShelfItemViewHolder gamesShelfItemViewHolder = (GamesShelfItemViewHolder) viewHolder;
        resetLayoutParams(gamesShelfItemViewHolder);
        gamesShelfItemViewHolder.mImageView.setImageDrawable(null);
        gamesShelfItemViewHolder.mImageView.setImageBitmap(null);
        gamesShelfItemViewHolder.mImageView.setBackground(null);
        gamesShelfItemViewHolder.mImageView.setVisibility(View.VISIBLE);
        gamesShelfItemViewHolder.mTextView.setVisibility(View.GONE);
        gamesShelfItemViewHolder.hideMyChoiceBadge();
    }

    private void resetLayoutParams(GamesShelfItemViewHolder gamesShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = gamesShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        gamesShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    public static final class GamesShelfItemViewHolder extends ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;
        private String mDisplayName;

        public GamesShelfItemViewHolder(View view) {
            super(view);
            GamesChapterShelfItemView gamesShelfItemView = (GamesChapterShelfItemView) view;
            mImageView = gamesShelfItemView.getShelfItemImageView();
            mTextView = gamesShelfItemView.getShelfItemTextView();
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

        private void showMyChoiceBadge() {
            DdbLogUtility.logGamesChapter("GamesChapterShelfItemPresenter", "showMyChoiceBadge() called");
            GamesChapterShelfItemView gamesShelfItemView = (GamesChapterShelfItemView) view;
            gamesShelfItemView.showMyChoiceLockBadge();
        }

        private void hideMyChoiceBadge() {
            DdbLogUtility.logGamesChapter("GamesChapterShelfItemPresenter", "hideMyChoiceBadge() called");
            GamesChapterShelfItemView gamesShelfItemView = (GamesChapterShelfItemView) view;
            gamesShelfItemView.hideMyChoiceLockBadge();
        }
    }
}
