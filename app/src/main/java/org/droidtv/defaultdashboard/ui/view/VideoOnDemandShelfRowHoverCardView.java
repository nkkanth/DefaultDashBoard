package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import androidx.annotation.Nullable;

/**
 * Created by bhargava.gugamsetty on 15-11-2017.
 */

public class VideoOnDemandShelfRowHoverCardView extends LinearLayout {

    private TextView mTitle;
    private TextView mSubText;
    private TextView mDescription;

    public VideoOnDemandShelfRowHoverCardView(Context context) {
        this(context, null);
    }

    public VideoOnDemandShelfRowHoverCardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoOnDemandShelfRowHoverCardView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.view_vod_shelf_row_hovercard, this);
        mTitle = (TextView) findViewById(R.id.vod_item_title);
        mSubText = (TextView) findViewById(R.id.vod_shelf_item_subtext);
        mDescription = (TextView) findViewById(R.id.vod_shelf_item_description);
        mTitle.setSelected(true);
        if (isRtl()) {
            applyRtlMarginStart();
        }
    }

    public CharSequence getTitle() {
        return mTitle.getText();
    }

    public void setTitle(CharSequence mTitle) {
        DdbLogUtility.logCastChapter("VideoOnDemandShelfRowHoverCardView", "setTitle " + mTitle);
        if (!TextUtils.isEmpty(mTitle)) {
            this.mTitle.setText(mTitle);
            this.mTitle.setVisibility(View.VISIBLE);
        } else {
            this.mTitle.setVisibility(View.GONE);
        }
    }

    public CharSequence getSubText() {
        return mSubText.getText();
    }

    public void setSubText(CharSequence mSubTitle) {
        DdbLogUtility.logCastChapter("VideoOnDemandShelfRowHoverCardView", "setSubText " + mSubTitle);
        if (!TextUtils.isEmpty(mSubTitle)) {
            this.mSubText.setText(mSubTitle);
            this.mSubText.setVisibility(View.VISIBLE);
        } else {
            this.mSubText.setVisibility(View.GONE);
        }
    }

    public CharSequence getDescription() {
        return mDescription.getText();
    }

    public void setDescription(CharSequence mDescription) {
        if (!TextUtils.isEmpty(mDescription)) {
            this.mDescription.setText(mDescription);
            this.mDescription.setVisibility(View.VISIBLE);
        } else {
            this.mDescription.setVisibility(View.GONE);
        }
    }

    private boolean isRtl() {
        Configuration config = getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void applyRtlMarginStart() {
        int rtlMarginStart = getResources().getDimensionPixelSize(R.dimen.margin_start_hover_card_rtl_vod_chapter);
        LinearLayout.LayoutParams titleParams = (LayoutParams) mTitle.getLayoutParams();
        titleParams.setMarginStart(rtlMarginStart);
        mTitle.setLayoutParams(titleParams);

        LinearLayout.LayoutParams subTextParams = (LayoutParams) mSubText.getLayoutParams();
        subTextParams.setMarginStart(rtlMarginStart);
        mSubText.setLayoutParams(subTextParams);

        LinearLayout.LayoutParams descriptionParams = (LayoutParams) mDescription.getLayoutParams();
        descriptionParams.setMarginStart(rtlMarginStart);
        mDescription.setLayoutParams(descriptionParams);
    }
}
