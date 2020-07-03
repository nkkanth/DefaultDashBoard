package org.droidtv.defaultdashboard.data.model;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ImageFile;

/**
 * Created by sandeep.kumar on 05/12/2017.
 */

public class Thumbnail implements DashboardDataManager.ThumbnailBitmapFetchListener {

    private long mImageId;
    private Bitmap mBitmap;
    private int mThumbnailWidth;
    private int mThumbnailHeight;
    private String mImageFilePath;
    private String mImageDisplayName;
    private String mImageResolution;
    private int mDrawableImageResourceId;
    private ThumbnailStatusListener mThumbnailStatusListener;

    public Thumbnail(int thumbnailWidth, int thumbnailHeight) {
        mImageId = -1;
        mThumbnailWidth = thumbnailWidth;
        mThumbnailHeight = thumbnailHeight;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public int getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public int getThumbnailHeight() {
        return mThumbnailHeight;
    }

    public String getImageFilePath() {
        return mImageFilePath;
    }

    public int getDrawableImageResourceId() {
        return mDrawableImageResourceId;
    }

    public String getImageDisplayName() {
        return mImageDisplayName;
    }

    public String getImageResolution() {
        return mImageResolution;
    }

    public void setThumbnailStatusListener(ThumbnailStatusListener listener) {
        mThumbnailStatusListener = listener;
    }

    private long getImageId() {
        return mImageId;
    }

    private void setImageId(long imageId) {
        mImageId = imageId;
    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    private void setImageFilePath(String filePath) {
        mImageFilePath = filePath;
    }

    private void setImageDisplayName(String displayName) {
        mImageDisplayName = displayName;
    }

    private void setImageResolution(String imageResolution) {
        mImageResolution = imageResolution;
    }

    private void setDrawableImageResourceId(int resourceId) {
        mDrawableImageResourceId = resourceId;
    }

    /**
     * Sets the values from cursor into the specified thumbnail object
     *
     * @param cursor    The cursor to read records from
     * @param thumbnail The thumbnail object into which the values will be set
     */
    public static void fromCursor(Cursor cursor, Thumbnail thumbnail) {
        thumbnail.setImageId(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
        thumbnail.setImageFilePath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
        thumbnail.setImageDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE)));
        String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)).concat("x")
                .concat(cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT)));
        thumbnail.setImageResolution(resolution);
        thumbnail.setDrawableImageResourceId(-1);

        thumbnail.fetchThumbnailImage(thumbnail.getImageFilePath(), thumbnail.getImageId(), thumbnail.getThumbnailWidth(), thumbnail.getThumbnailHeight());
    }

    public static void fromDefaultResource(int drawableResourceId, String imageDisplayName, int imageWidth, int imageHeight, Thumbnail thumbnail) {
        long id = System.currentTimeMillis();
        thumbnail.setImageId(id);
        thumbnail.setImageFilePath(null);
        thumbnail.setDrawableImageResourceId(drawableResourceId);
        thumbnail.setImageDisplayName(imageDisplayName);
        thumbnail.setImageResolution(imageWidth + "x" + imageHeight);
        thumbnail.fetchThumbnailImageFromResource(drawableResourceId, thumbnail.getImageId(), thumbnail.getThumbnailWidth(), thumbnail.getThumbnailHeight());
    }

    public static void fromImageFile(ImageFile imageFile, Thumbnail thumbnail) {
        long id = System.currentTimeMillis();
        thumbnail.setImageId(id);
        String path = imageFile.getFile().getPath();
        thumbnail.setImageFilePath(path);
        thumbnail.setImageDisplayName(extractFileNameFromFilePath(path));
        thumbnail.setImageResolution(imageFile.getWidth() + "x" + imageFile.getHeight());
        thumbnail.fetchThumbnailImage(path, thumbnail.getImageId(), thumbnail.getThumbnailWidth(), thumbnail.getThumbnailHeight());
    }

    @Override
    public void onThumbnailBitmapFetchComplete(long id, Bitmap bitmap) {
        if (id == mImageId) {
            setBitmap(bitmap);
            if (mThumbnailStatusListener != null) {
                mThumbnailStatusListener.onThumbnailReady();
            }
        }
    }

    private void fetchThumbnailImage(String imageFilePath, long id, int thumbnailWidth, int thumbnailHeight) {
        DashboardDataManager.getInstance().fetchThumbnailImage(imageFilePath, id, thumbnailWidth, thumbnailHeight, this);
    }

    private void fetchThumbnailImageFromResource(int drawableResourceId, long id, int thumbnailWidth, int thumbnailHeight) {
        DashboardDataManager.getInstance().fetchThumbnailImageFromResource(drawableResourceId, id, thumbnailWidth, thumbnailHeight, this);
    }

    public void clear() {
        DashboardDataManager.getInstance().cancelFetchingThumbnailImage(getImageId());
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mImageId = -1;
    }

    private static String extractFileNameFromFilePath(String filePath) {
        int startIndex = filePath.lastIndexOf('/') + 1;
        int endIndex = filePath.lastIndexOf('.');
        if (endIndex == -1) {
            return filePath.substring(startIndex);
        } else {
            return filePath.substring(startIndex, endIndex);
        }
    }

    public interface ThumbnailStatusListener {
        void onThumbnailReady();
    }
}
