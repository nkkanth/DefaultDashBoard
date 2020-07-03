package org.droidtv.defaultdashboard.ui.view;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;

import androidx.annotation.Nullable;

/**
 * Created by sandeep.kumar on 05/12/2017.
 */

public class ThumbnailView extends FrameLayout {

    private ImageView mThumbnailImage;
    private TextView mImageNameTextView;
    private TextView mImageResolutionTextView;
    private LinearLayout mThumbnailMetadataContainer;

    public ThumbnailView(Context context) {
        this(context, null);
    }

    public ThumbnailView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbnailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_thumbnail, this);
        setFocusable(true);
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.thumbnail_view_margin);
        layoutParams.setMargins(margin, margin, margin, margin);
        setLayoutParams(layoutParams);

        mThumbnailImage = (ImageView) findViewById(R.id.thumbnail_imageview);
        mImageNameTextView = (TextView) findViewById(R.id.thumbnail_image_name_textview);
        mImageResolutionTextView = (TextView) findViewById(R.id.thumbnail_image_resolution_textview);
        mThumbnailMetadataContainer = (LinearLayout) findViewById(R.id.thumbnail_metadata_container);
    }

    public void setImage(Bitmap bitmap) {
        mThumbnailImage.setImageBitmap(bitmap);
    }

    public void setImage(Drawable drawable) {
        mThumbnailImage.setImageDrawable(drawable);
    }

    public void setImageName(String name) {
        mImageNameTextView.setText(name);
    }

    public void setImageResolution(String resolution) {
        mImageResolutionTextView.setText(resolution);
    }

    public void showMetadata() {
        mThumbnailMetadataContainer.animate().setDuration(150).alpha(1).setListener(mThumbnailMetadataAnimatorListener);
    }

    public void hideMetadata() {
        mThumbnailMetadataContainer.animate().setDuration(150).alpha(0).setListener(mThumbnailMetadataAnimatorListener);
    }

    public void clear() {
        mThumbnailImage.setImageDrawable(null);
        mImageNameTextView.setText("");
        mImageResolutionTextView.setText("");
    }

    private Animator.AnimatorListener mThumbnailMetadataAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mThumbnailMetadataContainer.getAlpha() == 1) {
                mThumbnailMetadataContainer.setVisibility(VISIBLE);
            } else {
                mThumbnailMetadataContainer.setVisibility(GONE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
}
