package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;

/**
 * Created by sandeep.kumar on 25/10/2017.
 */

public class SmartInfoShelfItemView extends RelativeLayout {

    private ImageView mImageView;

    public SmartInfoShelfItemView(Context context) {
        this(context, null);
    }

    public SmartInfoShelfItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartInfoShelfItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SmartInfoShelfItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    protected int getLayoutResourceId() {
        return R.layout.view_smart_info_shelf_item;
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.smart_info_item_margin_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.smart_info_item_margin_start);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.smart_info_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.smart_info_item_margin_bottom);
    }

    public ImageView getShelfItemImageView() {
        return mImageView;
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(getLayoutResourceId(), this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);

        setFocusable(true);
        setFocusableInTouchMode(true);
        mImageView = (ImageView) findViewById(R.id.smart_info_shelf_item_image);

        setBackgroundColor(getContext().getColor(R.color.app_recommendations_shelf_item_background_color));
    }
}
