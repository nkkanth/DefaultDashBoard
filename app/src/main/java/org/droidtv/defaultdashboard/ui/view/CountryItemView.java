package org.droidtv.defaultdashboard.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by bhargava.gugamsetty on 31-01-2018.
 */

public class CountryItemView extends FrameLayout {

    private ImageView mCountryIconImage;
    private TextView mCountryIconText;

    public CountryItemView(@NonNull Context context) {
        this(context, null);
    }

    public CountryItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountryItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CountryItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {

        LayoutInflater.from(getContext()).inflate(R.layout.view_country_item, this);
        setFocusable(true);
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.country_list_item_margin);
        layoutParams.setMargins(0, margin, 0, margin);
        setLayoutParams(layoutParams);
        setClipChildren(false);
        setClipToPadding(false);
        mCountryIconImage = (ImageView) findViewById(R.id.country_icon_image_view);
        mCountryIconImage.setClipToOutline(true);
        mCountryIconText = (TextView) findViewById(R.id.country_icon_text_view);
    }

    public void hideCountryLabel() {
        mCountryIconText.setVisibility(INVISIBLE);
    }

    public void showCountryLabel() {
        mCountryIconText.setVisibility(VISIBLE);
    }

    public View getImageView() {
        return mCountryIconImage;
    }
}
