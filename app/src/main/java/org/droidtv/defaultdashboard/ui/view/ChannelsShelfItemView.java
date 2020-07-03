package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import androidx.annotation.Nullable;

/**
 * Created by sandeep.kumar on 25/10/2017.
 */

public class ChannelsShelfItemView extends RelativeLayout {

    private ImageView mImageView;
    private ImageView mLockIconImageView;
    private ProgressBar mProgressBar;
    private TextView mChannelName;

    public ChannelsShelfItemView(Context context) {
        this(context, null);
    }

    public ChannelsShelfItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChannelsShelfItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ChannelsShelfItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public ImageView getShelfItemImageView() {
        return mImageView;
    }

    public TextView getChannelNameTextView(){
        return mChannelName;
    }

    public void showLockBadge(boolean isMyChoiceLocked) {
        DdbLogUtility.logTVChannelChapter("ChannelsShelfItemView", "showLockBadge isMyChoiceLocked " + isMyChoiceLocked);
        if (isMyChoiceLocked) {
            // mychoice icon
            mLockIconImageView.setImageResource(R.drawable.ic_mychoice_lock);
        } else {
            //scrambled icon
            mLockIconImageView.setImageResource(R.drawable.scrambled_badge_circle_background);
        }
        mLockIconImageView.setVisibility(VISIBLE);
    }

    public void hideLockBadge() {
        DdbLogUtility.logAppsChapter("ChannelsShelfItemView", "hideLockBadge");
        mLockIconImageView.setVisibility(GONE);
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    protected int getLayoutResourceId() {
        return R.layout.view_channels_shelf_item;
    }

    protected int getMarginEnd() {
        return getResources().getDimensionPixelSize(R.dimen.channels_shelf_item_margin_end);
    }

    protected int getMarginStart() {
        return getResources().getDimensionPixelSize(R.dimen.channels_shelf_item_margin_start);
    }

    protected int getMarginTop() {
        return getResources().getDimensionPixelSize(R.dimen.channels_shelf_item_margin_top);
    }

    protected int getMarginBottom() {
        return getResources().getDimensionPixelSize(R.dimen.channels_shelf_item_margin_bottom);
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(getLayoutResourceId(), this);
        LayoutParams layoutParams = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.channels_shelf_item_width),
                getResources().getDimensionPixelSize(R.dimen.channels_shelf_item_height));
        layoutParams.setMargins(getMarginStart(), getMarginTop(), getMarginEnd(), getMarginBottom());
        setLayoutParams(layoutParams);

        setFocusable(true);
        setFocusableInTouchMode(true);
        mImageView = (ImageView) findViewById(R.id.shelf_item_image);
        mChannelName = (TextView) findViewById(R.id.channel_name);
        mChannelName.setVisibility(INVISIBLE);
        mLockIconImageView = (ImageView) findViewById(R.id.lock_image);
        mProgressBar = (ProgressBar) findViewById(R.id.program_info_progress_bar);


        setBackgroundColor(getContext().getColor(R.color.channels_shelf_item_background_color));
    }
}
