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
 * Created by bhargava.gugamsetty on 08-02-2018.
 */

public class AppsChapterCountryRowView extends LinearLayout {

    private HorizontalGridView mGridView;
    private ImageView mShelfIconImageView;
    private TextView mShelfTitleTextView;
    private LinearLayout mShelfHeader;

    public AppsChapterCountryRowView(Context context) {
        this(context, null);
    }

    public AppsChapterCountryRowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppsChapterCountryRowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_apps_chapter_country_shelf_row, this);

        mGridView = (HorizontalGridView) findViewById(R.id.apps_chapter_country_row_content);
        // since we use WRAP_CONTENT for height in lb_list_row, we need set fixed size to false
        mGridView.setHasFixedSize(false);

        mShelfIconImageView = (ImageView) findViewById(R.id.apps_chapter_country_icon_image_view);
        mShelfTitleTextView = (TextView) findViewById(R.id.apps_chapter_country_title);
        mShelfHeader = (LinearLayout) findViewById(R.id.apps_chapter_country_header);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        setOrientation(VERTICAL);
    }

    public HorizontalGridView getAppsChapterCountryGridView() {
        return mGridView;
    }

    public ImageView getAppsChapterShelfIconImageView() {
        return mShelfIconImageView;
    }

    public TextView getAppsChapterShelfTitleTextView() {
        return mShelfTitleTextView;
    }

    public void onExpanded() {
        DdbLogUtility.logAppsChapter("AppsChapterCountryRowView", "onExpanded");
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mShelfIconImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.shelf_header_margin_top_expanded), 0, 0);
        mShelfHeader.setGravity(Gravity.NO_GRAVITY);
    }

    public void onCollapsed() {
        DdbLogUtility.logAppsChapter("AppsChapterCountryRowView", "onCollapsed");
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mShelfIconImageView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(0, 0, 0, 0);
        mShelfHeader.setGravity(Gravity.CENTER);
    }
}
