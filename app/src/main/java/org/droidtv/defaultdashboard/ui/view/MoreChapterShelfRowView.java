package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;
import androidx.leanback.widget.HorizontalGridView;

/**
 * Created by bhargava.gugamsetty on 19-12-2017.
 */

public class MoreChapterShelfRowView extends LinearLayout {

    private HorizontalGridView mMoreChapterGridView;
    private ImageView mMoreChapterShelfIconImageView;
    private TextView mMoreChapterShelfTitleTextView;

    public MoreChapterShelfRowView(Context context) {
        this(context, null);
    }

    public MoreChapterShelfRowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoreChapterShelfRowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.view_more_chapter_shelf_row, this);

        mMoreChapterGridView = (HorizontalGridView) findViewById(R.id.more_chapter_row_content);
        // since we use WRAP_CONTENT for height in lb_list_row, we need set fixed size to false
        mMoreChapterGridView.setHasFixedSize(false);

        mMoreChapterShelfIconImageView = (ImageView) findViewById(R.id.more_chapter_shelf_icon_image_view);
        mMoreChapterShelfTitleTextView = (TextView) findViewById(R.id.more_chapter_shelf_title);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        setOrientation(VERTICAL);

    }

    public HorizontalGridView getMoreChapterGridView() {
        return mMoreChapterGridView;
    }

    public ImageView getMoreChapterShelfIconImageView() {
        return mMoreChapterShelfIconImageView;
    }

    public TextView getMoreChapterShelfTitleTextView() {
        return mMoreChapterShelfTitleTextView;
    }

    public HorizontalGridView getGridView() {
        return mMoreChapterGridView;
    }
}
