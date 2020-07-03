package org.droidtv.defaultdashboard.ui.presenter;

import android.content.res.TypedArray;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.SmartInfo;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.ui.view.RecommendedChapterShelfRowView;
import org.droidtv.defaultdashboard.ui.view.SmartInfoShelfRowHoverCardView;
import org.droidtv.defaultdashboard.util.Constants;

import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.ShadowOverlayHelper;
import androidx.leanback.widget.SinglePresenterSelector;


/**
 * Created by sandeep.kumar on 05/10/2017.
 */

public class SmartInfoShelfRowPresenter extends ListRowPresenter {

    private SmartInfoHoverCardPresenter mHoverCardPresenter;
    private SinglePresenterSelector mSinglePresenterSelector;

    public SmartInfoShelfRowPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_SMALL);
    }

    public SmartInfoShelfRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public SmartInfoShelfRowPresenter(boolean useFocusDimmer) {
        this(FocusHighlight.ZOOM_FACTOR_SMALL, useFocusDimmer);
    }

    public SmartInfoShelfRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        super(focusZoomFactor, useFocusDimmer);
        initialize();
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        RecommendedChapterShelfRowView rowView = new RecommendedChapterShelfRowView(parent.getContext());
        setupFadingEdgeEffect(rowView);
        setupAlignment(rowView);
        return new ShelfRowViewHolder(rowView, rowView.getShelfIconImageView(), rowView.getShelfTitleTextView(), rowView.getGridView(), this);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        if (item instanceof ListRow) {
            ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
            ShelfRow row = (ShelfRow) item;
            if (row.getShelfHeader() != null) {
                ShelfHeaderItem shelfHeaderItem = row.getShelfHeader();
                shelfRowViewHolder.mIconImageView.setImageDrawable(shelfHeaderItem.getShelfIconDrawable());
                shelfRowViewHolder.mTextView.setText(shelfHeaderItem.getTitle());
            } else {
                shelfRowViewHolder.mIconImageView.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    @Override
    protected void onRowViewExpanded(final RowPresenter.ViewHolder holder, boolean expanded) {
        if (getHoverCardPresenterSelector() == null) {
            setHoverCardPresenterSelector(mSinglePresenterSelector);
        }
        DdbLogUtility.logRecommendationChapter("SmartInfoShelfRowPresenter", "onRowViewExpanded() called with: expanded = [" + expanded + "]");
        super.onRowViewExpanded(holder, expanded);
        ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
        shelfRowViewHolder.mTextView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        setVerticalPadding(shelfRowViewHolder);

        RecommendedChapterShelfRowView shelfRowView = (RecommendedChapterShelfRowView) shelfRowViewHolder.view;
        if (expanded) {
            shelfRowView.onExpanded();
        } else {
            shelfRowView.onCollapsed();
        }

        if (!expanded) {
            setHoverCardPresenterSelector(null);
        }
    }

    @Override
    protected void onRowViewSelected(RowPresenter.ViewHolder holder, boolean selected) {
        super.onRowViewSelected(holder, selected);
        setVerticalPadding((ShelfRowViewHolder) holder);
    }

    @Override
    protected ShadowOverlayHelper.Options createShadowOverlayOptions() {
        ShadowOverlayHelper.Options shadowOverlayOptions = new ShadowOverlayHelper.Options();
        shadowOverlayOptions.dynamicShadowZ(0, Constants.SHELF_ITEM_FOCUS_ELEVATION);
        return shadowOverlayOptions;
    }

    private void initialize() {
        mHoverCardPresenter = new SmartInfoHoverCardPresenter();
        mSinglePresenterSelector = new SinglePresenterSelector(mHoverCardPresenter);
    }

    private void setupFadingEdgeEffect(RecommendedChapterShelfRowView rowView) {
        HorizontalGridView gridView = rowView.getGridView();
        TypedArray ta = gridView.getContext()
                .obtainStyledAttributes(androidx.leanback.R.styleable.LeanbackTheme);
        int browseRowsFadingEdgeLength = (int) ta.getDimension(
                androidx.leanback.R.styleable.LeanbackTheme_browseRowsFadingEdgeLength, 0);
        ta.recycle();

        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (isRtl) {
            gridView.setFadingRightEdge(true);
            gridView.setFadingRightEdgeLength(browseRowsFadingEdgeLength);
        } else {
            gridView.setFadingLeftEdge(true);
            gridView.setFadingLeftEdgeLength(browseRowsFadingEdgeLength);
        }
    }

    private void setupAlignment(RecommendedChapterShelfRowView rowView) {
        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        Log.d("SmartInfoShelfRowPresenter", "setupAlignment: isRtl " + isRtl);
        HorizontalGridView gridView = rowView.getGridView();
        gridView.setItemAlignmentOffset(0);
        gridView.setItemAlignmentOffsetPercent(0);
        gridView.setWindowAlignmentOffset(isRtl ? gridView.getPaddingStart() + gridView.getFadingRightEdgeLength() : gridView.getFadingLeftEdgeLength() + gridView.getPaddingStart());
        gridView.setWindowAlignmentOffsetPercent(0);
        gridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE);
    }

    private void setVerticalPadding(ListRowPresenter.ViewHolder vh) {
        int leftPadding = vh.view.getResources().getDimensionPixelSize(R.dimen.shelf_row_horizontal_gridview_padding);
        int topPadding = 0;
        int bottomPadding = 0;
        int rightPadding = 0;
        vh.getGridView().setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    public static class ShelfRowViewHolder extends ViewHolder {

        private ImageView mIconImageView;
        private TextView mTextView;

        public ShelfRowViewHolder(View rootView, ImageView shelfIconImageView, TextView shelfTitleTextView, HorizontalGridView gridView, ListRowPresenter rowPresenter) {
            super(rootView, gridView, rowPresenter);
            mIconImageView = shelfIconImageView;
            mTextView = shelfTitleTextView;
        }
    }

    private static final class SmartInfoHoverCardPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            SmartInfoShelfRowHoverCardView smartInfoShelfRowHoverCardView = new SmartInfoShelfRowHoverCardView(parent.getContext());
            return new RowPresenter.ViewHolder(smartInfoShelfRowHoverCardView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            SmartInfoShelfRowHoverCardView smartInfoShelfRowHoverCardView = (SmartInfoShelfRowHoverCardView) viewHolder.view;
            if (item instanceof Recommendation) {
                Recommendation recommendation = (Recommendation) item;
                populateHoverCardView(smartInfoShelfRowHoverCardView, recommendation.getTitle(), recommendation.getSubtitle(), recommendation.getDescription());
            } else {
                SmartInfo smartInfo = (SmartInfo) item;
                populateHoverCardView(smartInfoShelfRowHoverCardView, smartInfo.getTitle(), smartInfo.getSubtitle(), smartInfo.getDescription());
            }
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }

        private void populateHoverCardView(SmartInfoShelfRowHoverCardView hoverCardView, String title, String subtitle, String description) {
            DdbLogUtility.logRecommendationChapter("SmartInfoShelfRowPresenter", "populateHoverCardView() called with: hoverCardView = [" + hoverCardView + "], title = [" + title + "], subtitle = [" + subtitle + "], description = [" + description + "]");
            hoverCardView.setTitle(title);
            hoverCardView.setSubText(subtitle);
            hoverCardView.setDescription(description);
        }
    }
}
