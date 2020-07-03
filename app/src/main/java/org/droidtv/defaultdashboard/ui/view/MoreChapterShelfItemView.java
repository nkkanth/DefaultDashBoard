package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import androidx.annotation.Nullable;

/**
 * Created by bhargava.gugamsetty on 20-12-2017.
 */

public class MoreChapterShelfItemView extends RelativeLayout {

    private TextView mMoreChapterShelfItemTitleView;
    private ImageView mMoreChapterShelfItemImageView;
    private TextView mMoreChapterNumericTextView;
    private FrameLayout mImageContainer;

    public MoreChapterShelfItemView(Context context) {
        this(context, null);
    }

    public MoreChapterShelfItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoreChapterShelfItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MoreChapterShelfItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    public TextView getMoreChapterShelfItemTitleView() {
        return mMoreChapterShelfItemTitleView;
    }

    public ImageView getMoreChapterShelfItemImageView() {
        return mMoreChapterShelfItemImageView;
    }

    public TextView getMoreChapterShelfItemNumericTextView() {
        return mMoreChapterNumericTextView;
    }

    protected int getLayoutResourceId() {
        return R.layout.view_more_chapter_shelf_item;
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_margin_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_margin_start);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_margin_bottom);
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(getLayoutResourceId(), this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_width),
                getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_height));
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClipChildren(false);
        mImageContainer = (FrameLayout) findViewById(R.id.more_chapter_shelf_item_image_container);
        mMoreChapterShelfItemTitleView = (TextView) findViewById(R.id.more_chapter_shelf_item_title);
        mMoreChapterShelfItemImageView = (ImageView) findViewById(R.id.more_chapter_shelf_item_image);
        mMoreChapterNumericTextView = (TextView) findViewById(R.id.message_notification_text_view);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMoreChapterShelfItemTitleView.getLayoutParams();
        DdbLogUtility.logMoreChapter("MoreChapterShelfItemView", "setSelected " + selected);
        if (selected) {
            mMoreChapterShelfItemTitleView.setTextAppearance(R.style.FontStyleMoreChapterItemTitleHighlight);
            layoutParams.width = getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_text_width_focused);
            mImageContainer.setElevation(7f);
            mMoreChapterNumericTextView.setElevation(7f);
        } else {
            mMoreChapterShelfItemTitleView.setTextAppearance(R.style.FontStyleMoreChapterItemTitleDefault);
            layoutParams.width = getResources().getDimensionPixelSize(R.dimen.more_chapter_shelf_item_text_width_default);
            mImageContainer.setElevation(0f);
            mMoreChapterNumericTextView.setElevation(0f);
        }
        mMoreChapterShelfItemTitleView.setLayoutParams(layoutParams);
    }
}
