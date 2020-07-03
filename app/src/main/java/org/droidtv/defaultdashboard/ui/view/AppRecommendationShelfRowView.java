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

import androidx.annotation.Nullable;
import androidx.leanback.widget.HorizontalGridView;

/**
 * Created by bhargava.gugamsetty on 21-02-2018.
 */

public class AppRecommendationShelfRowView extends LinearLayout {

    private HorizontalGridView mHorizontalGridView;
    private ImageView mShelfIconImageView;
    private TextView mShelfTitleTextView;
    private LinearLayout mShelfHeader;
    private TextView mPreviewTitle;

    public AppRecommendationShelfRowView(Context context) {
        this(context, null);
    }

    public AppRecommendationShelfRowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppRecommendationShelfRowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.view_app_recommendation_shelf_row, this);

        mHorizontalGridView = (HorizontalGridView) findViewById(R.id.row_content);
        // since we use WRAP_CONTENT for height in lb_list_row, we need set fixed size to false
        mHorizontalGridView.setHasFixedSize(false);

        mShelfIconImageView = (ImageView) findViewById(R.id.app_recommendation_shelf_icon_image_view);
        mShelfTitleTextView = (TextView) findViewById(R.id.app_recommendation_shelf_title);
        mShelfHeader = (LinearLayout) findViewById(R.id.app_recommendation_shelf_header);
        mPreviewTitle = (TextView) findViewById(R.id.app_recommended_preview_programs_title);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        setOrientation(VERTICAL);
    }

    public ImageView getShelfIconImageView() {
        return mShelfIconImageView;
    }

    public TextView getShelfTitleTextView() {
        return mShelfTitleTextView;
    }

    public TextView getPreviewTitleTextView() {
        return mPreviewTitle;
    }
    public HorizontalGridView getGridView() {
        return mHorizontalGridView;
    }

    public void onExpanded() {
        DdbLogUtility.logAppsChapter("AppRecommendationShelfRowView", "onExpanded");
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mShelfIconImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.shelf_header_margin_top_expanded), 0, 0);
        mShelfHeader.setGravity(Gravity.NO_GRAVITY);
        mPreviewTitle.setVisibility(VISIBLE);
    }

    public void onCollapsed() {
        DdbLogUtility.logAppsChapter("AppRecommendationShelfRowView", "onCollapsed");
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mShelfIconImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, 0, 0, 0);
        mShelfHeader.setGravity(Gravity.CENTER);
        mPreviewTitle.setVisibility(GONE);
    }
}
