package org.droidtv.defaultdashboard.ui.presenter;

import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.MoreChapterShelfRowView;

import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.RowPresenter;

/**
 * Created by bhargava.gugamsetty on 19-12-2017.
 */

public class MoreChapterShelfRowPresenter extends ListRowPresenter {


    public MoreChapterShelfRowPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_MEDIUM);
    }

    public MoreChapterShelfRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public MoreChapterShelfRowPresenter(boolean useFocusDimmer) {
        this(FocusHighlight.ZOOM_FACTOR_MEDIUM, useFocusDimmer);
    }

    public MoreChapterShelfRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        super(focusZoomFactor, useFocusDimmer);
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        MoreChapterShelfRowView rowView = new MoreChapterShelfRowView(parent.getContext());
        setupFadingEdgeEffect(rowView);
        setupAlignment(rowView);
        return new MoreChapterShelfRowViewHolder(rowView, rowView.getMoreChapterShelfIconImageView(), rowView.getMoreChapterShelfTitleTextView(), rowView.getGridView(), this);

    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        if (item instanceof ListRow) {
            MoreChapterShelfRowViewHolder moreChapterShelfRowViewHolder = (MoreChapterShelfRowViewHolder) holder;
            ShelfRow row = (ShelfRow) item;
            if (row.getShelfHeader() != null) {
                ShelfHeaderItem shelfHeaderItem = row.getShelfHeader();
                moreChapterShelfRowViewHolder.mMoreChapterShelfIconImageView.setImageDrawable(shelfHeaderItem.getShelfIconDrawable());
                moreChapterShelfRowViewHolder.mMoreChapterShelfTitleTextView.setText(shelfHeaderItem.getTitle());
            } else {
                moreChapterShelfRowViewHolder.mMoreChapterShelfIconImageView.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    private void setupAlignment(MoreChapterShelfRowView rowView) {
        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        HorizontalGridView gridView = rowView.getGridView();
        gridView.setItemAlignmentOffset(0);
        gridView.setItemAlignmentOffsetPercent(0);
        gridView.setWindowAlignmentOffset(isRtl ? gridView.getPaddingStart() + gridView.getFadingRightEdgeLength() : gridView.getFadingLeftEdgeLength() + gridView.getPaddingStart());
        gridView.setWindowAlignmentOffsetPercent(0);
        gridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_HIGH_EDGE);
    }

    @Override
    protected void onRowViewExpanded(RowPresenter.ViewHolder holder, boolean expanded) {
        DdbLogUtility.logMoreChapter("MoreChapterShelfRowPresenter", "onRowViewExpanded() called with: expanded = [" + expanded + "]");
        MoreChapterShelfRowViewHolder shelfRowViewHolder = (MoreChapterShelfRowViewHolder) holder;
        ShelfRow row = (ShelfRow) shelfRowViewHolder.getRowObject();
        if (row == null) {
            return;
        }
        int count = row.getAdapter().size();
        for (int i = 0; i < count; i++) {
            MoreChapterShelfItemPresenter.MoreShelfItemViewHolder viewHolder = (MoreChapterShelfItemPresenter.MoreShelfItemViewHolder) shelfRowViewHolder.getItemViewHolder(i);
            if (viewHolder == null) {
                continue;
            }
            if (holder.isSelected() && expanded) {
                viewHolder.getTitleView().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTitleView().setVisibility(View.GONE);
            }
        }
        ((ShelfRow) shelfRowViewHolder.getRowObject()).getAdapter().notifyItemRangeChanged(0, count);
        super.onRowViewExpanded(holder, expanded);
        shelfRowViewHolder.mMoreChapterShelfTitleTextView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        setVerticalPadding(shelfRowViewHolder);
    }

    @Override
    protected void onRowViewSelected(RowPresenter.ViewHolder holder, boolean selected) {
        DdbLogUtility.logMoreChapter("MoreChapterShelfRowPresenter", "onRowViewSelected() called with: selected = [" + selected + "]");
        MoreChapterShelfRowViewHolder shelfRowViewHolder = (MoreChapterShelfRowViewHolder) holder;
        ShelfRow row = (ShelfRow) shelfRowViewHolder.getRowObject();
        if (row == null) {
            return;
        }
        int count = row.getAdapter().size();
        for (int i = 0; i < count; i++) {
            MoreChapterShelfItemPresenter.MoreShelfItemViewHolder viewHolder = (MoreChapterShelfItemPresenter.MoreShelfItemViewHolder) shelfRowViewHolder.getItemViewHolder(i);
            if (viewHolder == null) {
                continue;
            }
            if (selected && holder.isExpanded()) {
                viewHolder.getTitleView().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getTitleView().setVisibility(View.GONE);
            }
        }
        ((ShelfRow) shelfRowViewHolder.getRowObject()).getAdapter().notifyItemRangeChanged(0, count);
        super.onRowViewSelected(holder, selected);
        setVerticalPadding((MoreChapterShelfRowViewHolder) holder);
    }

    private void setupFadingEdgeEffect(MoreChapterShelfRowView rowView) {
        HorizontalGridView gridView = rowView.getGridView();
        TypedArray ta = gridView.getContext()
                .obtainStyledAttributes(androidx.leanback.R.styleable.LeanbackTheme);
        int browseRowsFadingEdgeLength = (int) ta.getDimension(
                androidx.leanback.R.styleable.LeanbackTheme_browseRowsFadingEdgeLength, 0);
        ta.recycle();

        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        DdbLogUtility.logMoreChapter("MoreChapterShelfRowPresenter", "setupFadingEdgeEffect: isRtl " + isRtl);
        if (isRtl) {
            gridView.setFadingRightEdge(true);
            gridView.setFadingRightEdgeLength(browseRowsFadingEdgeLength);
        } else {
            gridView.setFadingLeftEdge(true);
            gridView.setFadingLeftEdgeLength(browseRowsFadingEdgeLength);
        }
    }

    private void setVerticalPadding(ListRowPresenter.ViewHolder vh) {
        int leftPadding = vh.view.getResources().getDimensionPixelSize(R.dimen.shelf_row_horizontal_gridview_padding);
        int topPadding = 0;
        int bottomPadding = 0;
        int rightPadding = 0;
        vh.getGridView().setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    public static class MoreChapterShelfRowViewHolder extends ListRowPresenter.ViewHolder {

        private ImageView mMoreChapterShelfIconImageView;
        private TextView mMoreChapterShelfTitleTextView;

        public MoreChapterShelfRowViewHolder(View rootView, ImageView shelfIconImageView, TextView shelfTitleTextView, HorizontalGridView gridView, ListRowPresenter rowPresenter) {
            super(rootView, gridView, rowPresenter);
            mMoreChapterShelfIconImageView = shelfIconImageView;
            mMoreChapterShelfTitleTextView = shelfTitleTextView;
        }
    }
}
