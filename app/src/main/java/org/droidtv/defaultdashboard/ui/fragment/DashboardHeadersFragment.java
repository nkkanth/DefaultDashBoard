package org.droidtv.defaultdashboard.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.model.ChapterHeaderItem;

import androidx.leanback.app.HeadersSupportFragment;
import androidx.leanback.widget.PageRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.RowHeaderPresenter;
import androidx.leanback.widget.VerticalGridView;

/**
 * Created by sandeep.kumar on 11/10/2017.
 */

public class DashboardHeadersFragment extends HeadersSupportFragment {

    private ImageView mHotelLogoImageView;
    DashboardHeaderPresenter mDashboardHeaderPresenter;

    public DashboardHeadersFragment() {
        super();
        mDashboardHeaderPresenter= new DashboardHeaderPresenter();
        setPresenterSelector(new DashboardHeaderPresenterSelector());
    }

    @Override
    public void onStart() {
        super.onStart();
        setAlignment(getResources().getDimensionPixelSize(R.dimen.chapter_list_offset_top));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mHotelLogoImageView = (ImageView) view.findViewById(R.id.hotel_logo);
        return view;
    }

    @Override
    public void setAlignment(int windowAlignOffsetTop) {
        VerticalGridView gridView = getVerticalGridView();
        if (gridView != null) {
            // align the top edge of item
            gridView.setItemAlignmentOffset(0);
            gridView.setItemAlignmentOffsetPercent(
                    VerticalGridView.ITEM_ALIGN_OFFSET_PERCENT_DISABLED);

            // align to a fixed position from top
            gridView.setWindowAlignmentOffset(windowAlignOffsetTop);
            gridView.setWindowAlignmentOffsetPercent(
                    VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED);
            gridView.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_HIGH_EDGE);
        }
    }

    public void setHotelLogo(Bitmap bitmap) {
        if (mHotelLogoImageView != null) {
            mHotelLogoImageView.setImageBitmap(bitmap);
        }
    }

    public void setHotelLogo(int drawableResourceId) {
        if (mHotelLogoImageView != null) {
            mHotelLogoImageView.setImageResource(drawableResourceId);
        }
    }

    private class DashboardHeaderPresenterSelector extends PresenterSelector {

        @Override
        public Presenter getPresenter(Object item) {
            return mDashboardHeaderPresenter;
        }
    }

    private final class DashboardHeaderPresenter extends RowHeaderPresenter {

        int mHighlightedFontColor;
        int mNonHighlightedFontColor;

        DashboardHeaderPresenter() {
            super();
            mHighlightedFontColor = DashboardDataManager.getInstance().getSidePanelHighlightedTextColor();
            mNonHighlightedFontColor = DashboardDataManager.getInstance().getSidePanelNonHighlightedTextColor();
        }


        @Override
        public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
            LinearLayout rootView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chapter_header, parent, false);
            ChapterHeaderViewHolder headerViewHolder = new ChapterHeaderViewHolder(rootView, mHighlightedFontColor, mNonHighlightedFontColor);
            DashboardDataManager.getInstance().addSidePanelTextColorChangeListener(headerViewHolder);
            return headerViewHolder;
        }

        @Override
        public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
            PageRow row = (PageRow) item;
            ChapterHeaderViewHolder chapterHeaderViewHolder = (ChapterHeaderViewHolder) viewHolder;
            ChapterHeaderItem chapterHeaderItem = (ChapterHeaderItem) row.getHeaderItem();
            chapterHeaderViewHolder.mTitleTextView.setText(chapterHeaderItem.getName());
            chapterHeaderViewHolder.mIconImageView.setImageResource(chapterHeaderItem.getIconResourceId());
            chapterHeaderViewHolder.updateColor();
        }

        @Override
        public void onViewDetachedFromWindow(Presenter.ViewHolder holder){
            DashboardDataManager.getInstance().removeSidePanelTextColorChangeListener((ChapterHeaderViewHolder) holder);
        }

        @Override
        protected void onSelectLevelChanged(ViewHolder holder) {

        }

        private final class ChapterHeaderViewHolder extends RowHeaderPresenter.ViewHolder implements View.OnFocusChangeListener, DashboardDataManager.SidePanelTextColorChangeListener {

            private TextView mTitleTextView;
            private ImageView mIconImageView;
            private int mHighlightedFontColor;
            private int mNonHighlightedFontColor;

            @Override
            public void changeSidePanelHighlightedTextColor(int color) {
                mHighlightedFontColor = color;
                if (view.isFocused()) {
                    changeHighlightedTextColor();
                } else {
                    changeNonHighlightedTextColor();
                }
            }

            @Override
            public void changeSidePanelNonHighlightedTextColor(int color) {
                mNonHighlightedFontColor = color;
                if (view.isFocused()) {
                    changeHighlightedTextColor();
                } else {
                    changeNonHighlightedTextColor();
                }
            }

            private void updateColor() {
                if (view.isFocused()) {
                    changeHighlightedTextColor();
                } else {
                    changeNonHighlightedTextColor();
                }
            }

            private void applyTintToIcon(int color) {
                Drawable drawable = mIconImageView.getDrawable().mutate();
                drawable.setTint(color);
            }

            private ChapterHeaderViewHolder(View view, int highlightedFontColor, int nonHighlightedFontColor) {
                super(view);
                view.setOnFocusChangeListener(this);
                mTitleTextView = (TextView) view.findViewById(R.id.chapter_title_textview);
                mIconImageView = (ImageView) view.findViewById(R.id.chapter_icon_imageview);
                mHighlightedFontColor = highlightedFontColor;
                mNonHighlightedFontColor = nonHighlightedFontColor;
            }


            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    changeHighlightedTextColor();
                    mTitleTextView.setTypeface(Typeface.SANS_SERIF);
                    mTitleTextView.setSelected(true);
                    mTitleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    mTitleTextView.setMarqueeRepeatLimit(-1);
                } else {
                    changeNonHighlightedTextColor();
                    mTitleTextView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                    mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                }
            }

            private void changeHighlightedTextColor() {
                mTitleTextView.setTextColor(mHighlightedFontColor);
                applyTintToIcon(mHighlightedFontColor);
            }

            private void changeNonHighlightedTextColor() {
                mTitleTextView.setTextColor(mNonHighlightedFontColor);
                applyTintToIcon(mNonHighlightedFontColor);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("DashboardHeadersFragment", "#### onDestroy()");
        mDashboardHeaderPresenter = null;
        super.onDestroy();
    }
}
