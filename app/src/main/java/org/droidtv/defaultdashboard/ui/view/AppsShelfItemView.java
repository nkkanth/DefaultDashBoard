package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;

/**
 * Created by sandeep.kumar on 25/10/2017.
 */

public class AppsShelfItemView extends RelativeLayout {

    private ImageView mImageView;
    private ImageView mLockIconImageView;

    public AppsShelfItemView(Context context) {
        this(context, null);
    }

    public AppsShelfItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppsShelfItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppsShelfItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public ImageView getShelfItemImageView() {
        return mImageView;
    }

    public void showMyChoiceLockBadge() {
        mLockIconImageView.setVisibility(VISIBLE);
    }

    public void hideMyChoiceLockBadge() {
        mLockIconImageView.setVisibility(GONE);
    }

    protected int getLayoutResourceId() {
        return R.layout.view_apps_shelf_item;
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.apps_shelf_item_margin_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.apps_shelf_item_margin_start);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.apps_shelf_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.apps_shelf_item_margin_bottom);
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(getLayoutResourceId(), this);
        LayoutParams layoutParams = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.apps_shelf_item_width),
                getResources().getDimensionPixelSize(R.dimen.apps_shelf_item_height));
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);

        setFocusable(true);
        setFocusableInTouchMode(true);
        mImageView = (ImageView) findViewById(R.id.shelf_item_image);
        mLockIconImageView = (ImageView) findViewById(R.id.lock_image);

        setBackgroundColor(getContext().getColor(R.color.apps_shelf_item_background_color));
    }
}
