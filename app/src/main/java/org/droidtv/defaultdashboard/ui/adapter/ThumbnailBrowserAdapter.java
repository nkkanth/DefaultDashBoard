package org.droidtv.defaultdashboard.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ImageFile;
import org.droidtv.defaultdashboard.data.model.Thumbnail;
import org.droidtv.defaultdashboard.ui.view.ThumbnailView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by sandeep.kumar on 08/12/2017.
 */

public class ThumbnailBrowserAdapter extends RecyclerView.Adapter<ThumbnailBrowserAdapter.ThumbnailViewHolder> {

    private Cursor mImagesCursor;
    private int mDefaultImageResourceId;
    private ImageFile mSavedImageFile;
    private ThumbnailClickListener mThumbnailClickListener;
    private Drawable mNoImageDrawable;
    private int mDefaultImageWidth;
    private int mDefaultImageHeight;

    private static final int ITEM_COUNT_DEFAULT_IMAGE = 1;
    private static final int ITEM_COUNT_DEFAULT_AND_CURRENT_IMAGE = 2;

    public static final int SAVED_IMAGE_THUMBNAIL_POSITION = 1;

    public ThumbnailBrowserAdapter(Context context, int defaultImageResourceId, int defaultImageWidth, int defaultImageHeight) {
        mDefaultImageResourceId = defaultImageResourceId;
        mNoImageDrawable = context.getDrawable(org.droidtv.ui.tvwidget2k15.R.drawable.media_photos_d_ico_40x30_140);
        mDefaultImageWidth = defaultImageWidth;
        mDefaultImageHeight = defaultImageHeight;
    }

    public void setThumbnailClickListener(ThumbnailClickListener listener) {
        mThumbnailClickListener = listener;
    }

    public void setCursor(Cursor cursor) {
        mImagesCursor = cursor;
    }

    public void setSavedImageFile(ImageFile imageFile) {
        mSavedImageFile = imageFile;
    }

    public void clear() {
        if (mImagesCursor != null && !mImagesCursor.isClosed()) {
            mImagesCursor.close();
        }
    }

    @Override
    public int getItemCount() {
        if (mImagesCursor == null || mImagesCursor.isClosed()) {
            if (!isValidSavedImageFile()) {
                return ITEM_COUNT_DEFAULT_IMAGE;
            } else {
                return ITEM_COUNT_DEFAULT_AND_CURRENT_IMAGE;
            }
        }

        if (!isValidSavedImageFile()) {
            return ITEM_COUNT_DEFAULT_IMAGE + mImagesCursor.getCount();
        } else {
            return ITEM_COUNT_DEFAULT_AND_CURRENT_IMAGE + mImagesCursor.getCount();
        }
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbnailView thumbnailView = new ThumbnailView(parent.getContext());
        return new ThumbnailViewHolder(thumbnailView, mThumbnailClickListener, mNoImageDrawable);
    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        if (position == 0) {
            Thumbnail.fromDefaultResource(mDefaultImageResourceId, holder.itemView.getContext().getString(R.string.thumbnail_browser_default_image_name),
                    mDefaultImageWidth, mDefaultImageHeight, holder.mThumbnail);
            holder.populateViews();
            return;
        }

        if (position == 1 && isValidSavedImageFile()) {
            Thumbnail.fromImageFile(mSavedImageFile, holder.mThumbnail);
            holder.populateViews();
            return;
        }

        if (mImagesCursor != null) {
            int cursorPosition = position;
            if (!isValidSavedImageFile()) {
                cursorPosition = cursorPosition - ITEM_COUNT_DEFAULT_IMAGE;
            } else {
                cursorPosition = cursorPosition - ITEM_COUNT_DEFAULT_AND_CURRENT_IMAGE;
            }

            if (mImagesCursor.moveToPosition(cursorPosition)) {
                Thumbnail.fromCursor(mImagesCursor, holder.mThumbnail);
                holder.populateViews();
                return;
            }
        }
    }

    @Override
    public void onViewRecycled(ThumbnailViewHolder holder) {
        holder.recycle();
    }

    @Override
    public boolean onFailedToRecycleView(ThumbnailViewHolder holder) {
        return true;
    }

    private boolean isValidSavedImageFile() {
        return mSavedImageFile != null && mSavedImageFile.getFile() != null && mSavedImageFile.getFile().exists();
    }


    static class ThumbnailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Thumbnail.ThumbnailStatusListener {
        private Thumbnail mThumbnail;
        private ThumbnailClickListener mThumbnailClickListener;
        private Drawable mNoImageDrawable;

        private ThumbnailViewHolder(ThumbnailView view, ThumbnailClickListener listener, Drawable noImageDrawable) {
            super(view);
            view.setOnClickListener(this);
            mThumbnailClickListener = listener;
            mThumbnail = new Thumbnail(view.getContext().getResources().getDimensionPixelSize(R.dimen.thumbnail_imageview_width),
                    view.getContext().getResources().getDimensionPixelSize(R.dimen.thumbnail_imageview_height));
            mThumbnail.setThumbnailStatusListener(this);
            mNoImageDrawable = noImageDrawable;
        }

        @Override
        public void onClick(View v) {
            if (mThumbnailClickListener != null) {
                String imagePath = mThumbnail.getImageFilePath();
                if (!TextUtils.isEmpty(imagePath)) {
                    mThumbnailClickListener.onThumbnailClick(imagePath);
                } else {
                    mThumbnailClickListener.onThumbnailClick(mThumbnail.getDrawableImageResourceId());
                }
            }
        }

        @Override
        public void onThumbnailReady() {
            ThumbnailView thumbnailView = (ThumbnailView) itemView;
            Bitmap thumbnailBitmap = mThumbnail.getBitmap();
            if (thumbnailBitmap != null) {
                thumbnailView.setImage(thumbnailBitmap);
            }
        }

        private void populateViews() {
            ThumbnailView thumbnailView = (ThumbnailView) itemView;
            thumbnailView.setImage(mNoImageDrawable);
            thumbnailView.setImageName(mThumbnail.getImageDisplayName());
            thumbnailView.setImageResolution(mThumbnail.getImageResolution());
        }

        private void recycle() {
            ThumbnailView thumbnailView = (ThumbnailView) itemView;
            thumbnailView.clear();
            mThumbnail.clear();
        }
    }

    public interface ThumbnailClickListener {
        void onThumbnailClick(String imageFilePath);

        void onThumbnailClick(int drawableResourceId);
    }
}
