package org.droidtv.defaultdashboard.ui.presenter;

import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.GamesShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.recommended.Recommendation;
import org.droidtv.defaultdashboard.ui.view.GamesChapterShelfRowHoverCardView;
import org.droidtv.defaultdashboard.ui.view.GamesChapterShelfRowView;
import org.droidtv.defaultdashboard.util.Constants;

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
 * Created by bhargava.gugamsetty on 20-02-2018.
 */

public class GamesChapterShelfRowPresenter extends ListRowPresenter {

    private GamesChapterHoverCardPresenter mGamesChapterHoverCardPresenter;
    private SinglePresenterSelector mSinglePresenterSelector;

    public GamesChapterShelfRowPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_SMALL);
    }

    public GamesChapterShelfRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public GamesChapterShelfRowPresenter(boolean useFocusDimmer) {
        this(FocusHighlight.ZOOM_FACTOR_SMALL, useFocusDimmer);
    }

    public GamesChapterShelfRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        super(focusZoomFactor, useFocusDimmer);
        initialize();
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        GamesChapterShelfRowView rowView = new GamesChapterShelfRowView(parent.getContext());
        setupFadingEdgeEffect(rowView);
        setupAlignment(rowView);
        return new ShelfRowViewHolder(rowView, rowView.getShelfIconImageView(), rowView.getShelfTitleTextView(), rowView.getPreviewTitleTextView(), rowView.getGridView(), this);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        if (item instanceof ListRow) {
            ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
            /*GamesReccomendation*/ShelfRow row = (/*GamesReccomendation*/ShelfRow) item;
            if (row.getShelfHeader() != null) {
                GamesShelfHeaderItem shelfHeaderItem = (GamesShelfHeaderItem) row.getShelfHeader();
                shelfRowViewHolder.mIconImageView.setImageDrawable(shelfHeaderItem.getShelfIconDrawable());
                shelfRowViewHolder.mTextView.setText(shelfHeaderItem.getTitle());
                if(shelfHeaderItem instanceof GamesShelfHeaderItem) {
                    android.util.Log.d("GamesChapterShelfRowPresenter", "onBindRowViewHolder: instance of GamesShelfHeaderItem");
                    shelfRowViewHolder.mPreviewTitleView.setText(shelfHeaderItem.getPreviewProgramsTitle());
                }
            } else {
                shelfRowViewHolder.mIconImageView.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    @Override
    protected void onRowViewExpanded(final RowPresenter.ViewHolder holder, boolean expanded) {
        DdbLogUtility.logTVChannelChapter("GamesChapterShelfRowPresenter", "onRowViewExpanded() called with: holder = [" + holder + "], expanded = [" + expanded + "]");
        if (getHoverCardPresenterSelector() == null) {
            setHoverCardPresenterSelector(mSinglePresenterSelector);
        }
        super.onRowViewExpanded(holder, expanded);
        ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
        shelfRowViewHolder.mTextView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        shelfRowViewHolder.mPreviewTitleView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        setVerticalPadding(shelfRowViewHolder);

        GamesChapterShelfRowView shelfRowView = (GamesChapterShelfRowView) shelfRowViewHolder.view;
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
        mGamesChapterHoverCardPresenter = new GamesChapterHoverCardPresenter();
        mSinglePresenterSelector = new SinglePresenterSelector(mGamesChapterHoverCardPresenter);
    }

    private void setupFadingEdgeEffect(GamesChapterShelfRowView rowView) {
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

    private void setupAlignment(GamesChapterShelfRowView rowView) {
        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        HorizontalGridView gridView = rowView.getGridView();
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

    public static class ShelfRowViewHolder extends ViewHolder {

        private ImageView mIconImageView;
        private TextView mTextView;
        private TextView mPreviewTitleView;

        public ShelfRowViewHolder(View rootView, ImageView shelfIconImageView, TextView shelfTitleTextView, TextView previewTitleView, HorizontalGridView gridView, ListRowPresenter rowPresenter) {
            super(rootView, gridView, rowPresenter);
            mIconImageView = shelfIconImageView;
            mTextView = shelfTitleTextView;
            mPreviewTitleView = previewTitleView;
        }
    }

    private static final class GamesChapterHoverCardPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            GamesChapterShelfRowHoverCardView shelfHoverCardView = new GamesChapterShelfRowHoverCardView(parent.getContext());
            return new RowPresenter.ViewHolder(shelfHoverCardView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            GamesChapterShelfRowHoverCardView shelfHoverCardView = (GamesChapterShelfRowHoverCardView) viewHolder.view;
            if (item instanceof Recommendation) {
                Recommendation recommendation = (Recommendation) item;
                populateHoverCardView(shelfHoverCardView, recommendation.getTitle(), recommendation.getSubtitle(), recommendation.getDescription());
            }
        }

        private void populateHoverCardView(GamesChapterShelfRowHoverCardView hoverCardView, String title, String subtitle, String description) {
            DdbLogUtility.logGamesChapter("GamesChapterShelfRowPresenter", "populateHoverCardView() called with: hoverCardView = [" + hoverCardView + "], title = [" + title + "], subtitle = [" + subtitle + "], description = [" + description + "]");
            hoverCardView.setTitle(title);
            hoverCardView.setSubText(subtitle);
            hoverCardView.setDescription(description);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }
    }
}
