package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;

/**
 * Created by sandeep.kumar on 25/10/2017.
 */

public class TvChannelsChapterShelfItemView extends RelativeLayout {

    private ImageView mImageView;
    private ProgressBar mProgressBar;

    public TvChannelsChapterShelfItemView(Context context) {
        this(context, null);
    }

    public TvChannelsChapterShelfItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvChannelsChapterShelfItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TvChannelsChapterShelfItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public ImageView getShelfItemImageView() {
        return mImageView;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    protected int getLayoutResourceId() {
        return R.layout.view_tv_channels_chapter_shelf_item;
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.tv_channels_chapter_shelf_item_margin_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.tv_channels_chapter_shelf_item_margin_start);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.tv_channels_chapter_shelf_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.tv_channels_chapter_shelf_item_margin_bottom);
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(getLayoutResourceId(), this);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);

        setFocusable(true);
        setFocusableInTouchMode(true);
        mImageView = (ImageView) findViewById(R.id.shelf_item_image);
        mProgressBar = (ProgressBar) findViewById(R.id.program_info_progress_bar);

        setBackgroundColor(getContext().getColor(R.color.tv_channels_chapter_shelf_item_background_color));
    }
}
