package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import androidx.leanback.widget.HorizontalGridView;

/**
 * Created by sandeep.kumar on 06/10/2017.
 */

public class SourcesShelfRowView extends LinearLayout {

    private HorizontalGridView mGridView;
    private ImageView mShelfIconImageView;
    private TextView mShelfTitleTextView;
    private LinearLayout mShelfHeader;

    public SourcesShelfRowView(Context context) {
        this(context, null);
    }

    public SourcesShelfRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SourcesShelfRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater.from(context).inflate(R.layout.view_sources_shelf_row, this);

        mGridView = (HorizontalGridView) findViewById(R.id.row_content);
        // since we use WRAP_CONTENT for height in lb_list_row, we need set fixed size to false
        mGridView.setHasFixedSize(false);

        mShelfIconImageView = (ImageView) findViewById(R.id.shelf_icon_image_view);
        mShelfTitleTextView = (TextView) findViewById(R.id.shelf_title);
        mShelfHeader = (LinearLayout) findViewById(R.id.shelf_header);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        setOrientation(VERTICAL);
    }

    /**
     * Returns the HorizontalGridView.
     */
    public HorizontalGridView getGridView() {
        return mGridView;
    }

    public ImageView getShelfIconImageView() {
        return mShelfIconImageView;
    }


    public TextView getShelfTitleTextView() {
        return mShelfTitleTextView;
    }

    public void onExpanded() {
        DdbLogUtility.logRecommendationChapter("SourcesShelfRowView", "onExpanded() called");
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mShelfIconImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.shelf_header_margin_top_expanded), 0, 0);
        mShelfHeader.setGravity(Gravity.NO_GRAVITY);
    }

    public void onCollapsed() {
        DdbLogUtility.logRecommendationChapter("SourcesShelfRowView", "onCollapsed() called");
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mShelfIconImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, 0, 0, 0);
        mShelfHeader.setGravity(Gravity.CENTER);
    }
}
