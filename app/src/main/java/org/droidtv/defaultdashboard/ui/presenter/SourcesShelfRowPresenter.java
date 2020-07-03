package org.droidtv.defaultdashboard.ui.presenter;

import android.content.res.TypedArray;
import android.media.tv.TvInputInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.SourcesShelfRowHoverCardView;
import org.droidtv.defaultdashboard.ui.view.SourcesShelfRowView;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.ShadowOverlayHelper;
import androidx.leanback.widget.SinglePresenterSelector;


/**
 * Created by sandeep.kumar on 05/10/2017.
 */

public class SourcesShelfRowPresenter extends ListRowPresenter {

    private SourcesShelfHoverCardPresenter mHoverCardPresenter;
    private SinglePresenterSelector mSinglePresenterSelector;

    public SourcesShelfRowPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_SMALL);
    }

    public SourcesShelfRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public SourcesShelfRowPresenter(boolean useFocusDimmer) {
        this(FocusHighlight.ZOOM_FACTOR_SMALL, useFocusDimmer);
    }

    public SourcesShelfRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        super(focusZoomFactor, useFocusDimmer);
        initialize();
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        super.createRowViewHolder(parent);
        SourcesShelfRowView rowView = new SourcesShelfRowView(parent.getContext());
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
            int initialSelectedItemPosition = getLastSelectedDevicePosition(row);
            if (initialSelectedItemPosition != 0) {
                shelfRowViewHolder.getGridView().setSelectedPosition(initialSelectedItemPosition);
            }
        }
    }

    @Override
    protected void onRowViewExpanded(final RowPresenter.ViewHolder holder, boolean expanded) {
        DdbLogUtility.logTVChannelChapter("SourcesShelfRowPresenter", "onRowViewExpanded() called with: holder = [" + holder + "], expanded = [" + expanded + "]");
        if (getHoverCardPresenterSelector() == null) {
            setHoverCardPresenterSelector(mSinglePresenterSelector);
        }
        super.onRowViewExpanded(holder, expanded);
        ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
        shelfRowViewHolder.mTextView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        setVerticalPadding(shelfRowViewHolder);

        SourcesShelfRowView shelfRowView = (SourcesShelfRowView) shelfRowViewHolder.view;
        if (expanded) {
            shelfRowView.onExpanded();
        } else {
            shelfRowView.onCollapsed();
        }

        // If MyChoice is enabled then refresh the row so that lock icons are shown/hidden based on the row's expanded state
        if (DashboardDataManager.getInstance().isMyChoiceEnabled()) {
            ItemBridgeAdapter adapter = shelfRowViewHolder.getBridgeAdapter();
            int itemCount = adapter.getItemCount();
            adapter.notifyItemRangeChanged(0, itemCount);
        }

        if (!expanded) {
            setHoverCardPresenterSelector(null);
        }
    }

    @Override
    protected void onRowViewSelected(RowPresenter.ViewHolder holder, boolean selected) {
        DdbLogUtility.logTVChannelChapter("SourcesShelfRowPresenter", "onRowViewSelected() called with: holder = [" + holder + "], selected = [" + selected + "]");
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
        mHoverCardPresenter = new SourcesShelfHoverCardPresenter();
        mSinglePresenterSelector = new SinglePresenterSelector(mHoverCardPresenter);
    }

    private void setupFadingEdgeEffect(SourcesShelfRowView rowView) {
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

    private void setupAlignment(SourcesShelfRowView rowView) {
        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
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

    private int getLastSelectedDevicePosition(ShelfRow row) {
        int lastSelectedDevice = DashboardDataManager.getInstance().getLastSelectedDevice();
        if (lastSelectedDevice <= 0) {
            return 0;
        }
        boolean isLastSelectedDeviceVga = false;
        int hdmiPortId = -1;
        switch (lastSelectedDevice) {
            case TvSettingsDefinitions.LastSelectedDeviceConstants.HDMI1:
                hdmiPortId = 1;
                break;
            case TvSettingsDefinitions.LastSelectedDeviceConstants.HDMI2:
                hdmiPortId = 2;
                break;
            case TvSettingsDefinitions.LastSelectedDeviceConstants.HDMI3:
                hdmiPortId = 3;
                break;
            case TvSettingsDefinitions.LastSelectedDeviceConstants.HDMI4:
                hdmiPortId = 4;
                break;
            case TvSettingsDefinitions.LastSelectedDeviceConstants.VGA:
                isLastSelectedDeviceVga = true;
                break;
        }
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) row.getAdapter();
        for (int i = 0; adapter != null && i < adapter.size(); i++) {
            Source source = (Source) adapter.get(i);
            if ((isLastSelectedDeviceVga && source.getType() == TvInputInfo.TYPE_VGA) ||
                    (hdmiPortId != -1 && source.getType() == TvInputInfo.TYPE_HDMI && hdmiPortId == source.getHDMIPortId())) {
                return i;
            }
        }
        return 0;
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

    private static final class SourcesShelfHoverCardPresenter extends Presenter {

        private SourcesShelfHoverCardPresenter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            SourcesShelfRowHoverCardView shelfHoverCardView = new SourcesShelfRowHoverCardView(parent.getContext());
            return new RowPresenter.ViewHolder(shelfHoverCardView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            SourcesShelfRowHoverCardView shelfHoverCardView = (SourcesShelfRowHoverCardView) viewHolder.view;
            if (item instanceof Source) {
                Source source = (Source) item;
                populateHoverCardView(shelfHoverCardView, source.getLabel(), "", source.getDescription());
                return;
            }
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }

        private void populateHoverCardView(SourcesShelfRowHoverCardView hoverCardView, String title, String subtitle, String description) {
            hoverCardView.setTitle(title);
            hoverCardView.setSubText(subtitle);
            hoverCardView.setDescription(description);
        }
    }
}
