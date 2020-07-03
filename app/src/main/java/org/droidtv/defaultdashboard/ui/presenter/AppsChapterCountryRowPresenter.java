package org.droidtv.defaultdashboard.ui.presenter;

import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.appsChapter.AppsChapterCountryShelfItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.AppChapterCountryShelfRowHoverCardView;
import org.droidtv.defaultdashboard.ui.view.AppsChapterCountryRowView;
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
 * Created by bhargava.gugamsetty on 07-02-2018.
 */

public class AppsChapterCountryRowPresenter extends ListRowPresenter {

    private AppsChapterCountryHoverCardPresenter mAppsChapterCountryHoverCardPresenter;
    private SinglePresenterSelector mSinglePresenterSelector;

    public AppsChapterCountryRowPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_SMALL);
    }

    public AppsChapterCountryRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public AppsChapterCountryRowPresenter(boolean useFocusDimmer) {
        this(FocusHighlight.ZOOM_FACTOR_SMALL, useFocusDimmer);
    }

    public AppsChapterCountryRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        super(focusZoomFactor, useFocusDimmer);
        initialize();
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        super.createRowViewHolder(parent);
        AppsChapterCountryRowView rowView = new AppsChapterCountryRowView(parent.getContext());
        int rowHeight = getRowHeight();
        if (rowHeight != 0) {
            rowView.getAppsChapterCountryGridView().setRowHeight(rowHeight);
        }
        setupFadingEdgeEffect(rowView);
        setupAlignment(rowView);
        return new AppsChapterCountryRowPresenter.AppsChapterShelfRowViewHolder(rowView, rowView.getAppsChapterShelfIconImageView(), rowView.getAppsChapterShelfTitleTextView(), rowView.getAppsChapterCountryGridView(), this);

    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        if (item instanceof ListRow) {
            AppsChapterCountryRowPresenter.AppsChapterShelfRowViewHolder appsChapterShelfRowViewHolder = (AppsChapterCountryRowPresenter.AppsChapterShelfRowViewHolder) holder;
            ShelfRow row = (ShelfRow) item;
            if (row.getShelfHeader() != null) {
                ShelfHeaderItem shelfHeaderItem = row.getShelfHeader();
                appsChapterShelfRowViewHolder.mAppsChapterShelfIconImageView.setImageDrawable(shelfHeaderItem.getShelfIconDrawable());
                appsChapterShelfRowViewHolder.mAppsChapterShelfTitleTextView.setText(shelfHeaderItem.getTitle());
            } else {
                appsChapterShelfRowViewHolder.mAppsChapterShelfIconImageView.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    @Override
    protected void onRowViewExpanded(RowPresenter.ViewHolder holder, boolean expanded) {
        if (getHoverCardPresenterSelector() == null) {
            setHoverCardPresenterSelector(mSinglePresenterSelector);
        }
        super.onRowViewExpanded(holder, expanded);
        DdbLogUtility.logAppsChapter("AppsChapterCountryRowPresenter", "onRowViewExpanded() called with: holder = [" + holder + "], expanded = [" + expanded + "]");
        AppsChapterShelfRowViewHolder shelfRowViewHolder = (AppsChapterShelfRowViewHolder) holder;
        shelfRowViewHolder.mAppsChapterShelfTitleTextView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        setVerticalPadding(shelfRowViewHolder);

        AppsChapterCountryRowView shelfRowView = (AppsChapterCountryRowView) shelfRowViewHolder.view;
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
        DdbLogUtility.logAppsChapter("AppsChapterCountryRowPresenter", "onRowViewSelected() called with: holder = [" + holder + "], selected = [" + selected + "]");
        super.onRowViewSelected(holder, selected);
        setVerticalPadding((AppsChapterShelfRowViewHolder) holder);
    }

    @Override
    protected ShadowOverlayHelper.Options createShadowOverlayOptions() {
        ShadowOverlayHelper.Options shadowOverlayOptions = new ShadowOverlayHelper.Options();
        shadowOverlayOptions.dynamicShadowZ(0, Constants.SHELF_ITEM_FOCUS_ELEVATION);
        return shadowOverlayOptions;
    }

    private void initialize() {
        mAppsChapterCountryHoverCardPresenter = new AppsChapterCountryHoverCardPresenter();
        mSinglePresenterSelector = new SinglePresenterSelector(mAppsChapterCountryHoverCardPresenter);
    }

    private void setupFadingEdgeEffect(AppsChapterCountryRowView rowView) {
        HorizontalGridView gridView = rowView.getAppsChapterCountryGridView();
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

    private void setupAlignment(AppsChapterCountryRowView rowView) {
        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        HorizontalGridView gridView = rowView.getAppsChapterCountryGridView();
        gridView.setItemAlignmentOffset(0);
        gridView.setItemAlignmentOffsetPercent(0);
        gridView.setWindowAlignmentOffset(isRtl ? gridView.getPaddingStart() + gridView.getFadingRightEdgeLength() : gridView.getFadingLeftEdgeLength() + gridView.getPaddingStart());
        gridView.setWindowAlignmentOffsetPercent(0);
        gridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE);
    }

    private void setVerticalPadding(ViewHolder vh) {
        int leftPadding = vh.view.getResources().getDimensionPixelSize(R.dimen.shelf_row_horizontal_gridview_padding);
        int topPadding = 0;
        int bottomPadding = 0;
        int rightPadding = 0;
        vh.getGridView().setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    public static class AppsChapterShelfRowViewHolder extends ListRowPresenter.ViewHolder {

        private ImageView mAppsChapterShelfIconImageView;
        private TextView mAppsChapterShelfTitleTextView;

        public AppsChapterShelfRowViewHolder(View rootView, ImageView shelfIconImageView, TextView shelfTitleTextView, HorizontalGridView gridView, ListRowPresenter rowPresenter) {
            super(rootView, gridView, rowPresenter);
            mAppsChapterShelfIconImageView = shelfIconImageView;
            mAppsChapterShelfTitleTextView = shelfTitleTextView;
        }
    }

    private static class AppsChapterCountryHoverCardPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            AppChapterCountryShelfRowHoverCardView shelfHoverCardView = new AppChapterCountryShelfRowHoverCardView(parent.getContext());
            return new RowPresenter.ViewHolder(shelfHoverCardView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {

            AppChapterCountryShelfRowHoverCardView shelfHoverCardView = (AppChapterCountryShelfRowHoverCardView) viewHolder.view;
            if (item instanceof AppsChapterCountryShelfItem) {
                shelfHoverCardView.setTitle(shelfHoverCardView.getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_SELECT_YOUR_HOME_COUNTRY));
            }
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }
    }
}
