package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;

/**
 * Created by bhargava.gugamsetty on 20-02-2018.
 */

public class VideoOnDemandChapterShelfItemView extends RelativeLayout {
    private ImageView mImageView;
    private ImageView mLockIconImageView;
    private TextView mTextView;

    public VideoOnDemandChapterShelfItemView(Context context) {
        this(context, null);
    }

    public VideoOnDemandChapterShelfItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoOnDemandChapterShelfItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VideoOnDemandChapterShelfItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public ImageView getShelfItemImageView() {
        return mImageView;
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(getLayoutResourceId(), this);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mImageView = (ImageView) findViewById(R.id.vod_shelf_item_image);
        mLockIconImageView = (ImageView) findViewById(R.id.lock_image);
        mTextView = (TextView) findViewById(R.id.vod_shelf_item_text);
    }

    protected int getLayoutResourceId() {
        return R.layout.view_vod_shelf_item;
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.vod_shelf_item_margin_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.vod_shelf_item_margin_start);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.vod_shelf_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.vod_shelf_item_margin_bottom);
    }

    public TextView getShelfItemTextView() {
        return mTextView;
    }

    public void showMyChoiceLockBadge() {
        mLockIconImageView.setVisibility(VISIBLE);
    }

    public void hideMyChoiceLockBadge() {
        mLockIconImageView.setVisibility(GONE);
    }
}
