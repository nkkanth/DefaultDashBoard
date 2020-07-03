package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LongSparseArray;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ThumbnailBitmapFetchListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ThumbnailDataListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.query.AvailableImagesQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.tv.os.storage.IUsbVolumeLister;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sandeep.kumar on 08/12/2017.
 */

public final class ImageDataManager extends ContextualObject {

    private static String TAG = "ImageDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private LongSparseArray<ThumbnailImageFetchTask> mThumbnailImageFetchTasks;
    private AvailableImagesFetchTask mAvailableImagesFetchTask;

    private static final int MAX_THUMBNAIL_BITMAP_RESOLUTION = 500 * 500;

    ImageDataManager(Context context) {
        super(context);

        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.IMAGE_THREAD_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
        mThumbnailImageFetchTasks = new LongSparseArray<>();
        DdbLogUtility.logCommon(TAG, "ImageDataManager() called with: context = [" + context + "]");
    }

    void fetchThumbnailsFromUsb(ThumbnailDataListener listener) {
        if (mAvailableImagesFetchTask == null || mAvailableImagesFetchTask.isDone() || mAvailableImagesFetchTask.isCancelled()) {
            AvailableImagesCallable callable = new AvailableImagesCallable(getContext());
            mAvailableImagesFetchTask = new AvailableImagesFetchTask(callable, mUiThreadHandler, listener);
            mThreadPoolExecutor.execute(mAvailableImagesFetchTask);
        }
    }

    void stopFetchingThumbnailsFromUsb() {
        if (mAvailableImagesFetchTask != null) {
            mAvailableImagesFetchTask.cancel(true);
            mAvailableImagesFetchTask = null;
        }
    }

    void fetchThumbnailImage(String filePath, long id, int thumbnailWidth, int thumbnailHeight, final ThumbnailBitmapFetchListener listener) {
        ThumbnailImageCallable callable = new FileThumbnailImageCallable(filePath, id, thumbnailWidth, thumbnailHeight);
        fetchThumbnailImageInternal(callable, mUiThreadHandler, listener);
    }

    void fetchThumbnailImageFromResource(int drawableResourceId, long id, int thumbnailWidth, int thumbnailHeight, final ThumbnailBitmapFetchListener listener) {
        ThumbnailImageCallable callable = new DefaultThumbnailImageCallable(getContext(), drawableResourceId, id, thumbnailWidth, thumbnailHeight);
        fetchThumbnailImageInternal(callable, mUiThreadHandler, listener);
    }

    void cancelFetchingThumbnailImage(long id) {
        ThumbnailImageFetchTask task = mThumbnailImageFetchTasks.get(id);
        if (task != null) {
            task.cancel(true);
        }
        mThumbnailImageFetchTasks.remove(id);
    }

    void fetchImage(String filePath, int width, int height, DashboardDataManager.ImageFetchListener listener) {
        ImageCallable callable = new ImageCallable(getContext(), filePath, width, height);
        ImageFetchTask imageFetchTask = new ImageFetchTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(imageFetchTask);
    }

    void fetchImageFromResourceId(int resourceId, int width, int height, DashboardDataManager.ImageFetchListener listener) {
        ResourceImageCallable callable = new ResourceImageCallable(getContext(), resourceId, width, height);
        ImageFetchTask imageFetchTask = new ImageFetchTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(imageFetchTask);
    }

    private void fetchThumbnailImageInternal(ThumbnailImageCallable callable, Handler handler, final ThumbnailBitmapFetchListener thumbnailBitmapFetchListener) {
        ThumbnailImageFetchTask thumbnailImageFetchTask = new ThumbnailImageFetchTask(callable, mUiThreadHandler, new ThumbnailBitmapFetchListener() {
            @Override
            public void onThumbnailBitmapFetchComplete(long id, Bitmap bitmap) {
                mThumbnailImageFetchTasks.remove(id);
                if (thumbnailBitmapFetchListener != null) {
                    thumbnailBitmapFetchListener.onThumbnailBitmapFetchComplete(id, bitmap);
                }
            }
        });
        mThumbnailImageFetchTasks.put(callable.getId(), thumbnailImageFetchTask);
        mThreadPoolExecutor.execute(thumbnailImageFetchTask);
    }

    private Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().
                query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    public static int getSampleSize(int originalImageWidth, int originalImageHeight, int targetWidth, int targetHeight) {
        int sampleSize = 1;
        if ((originalImageWidth <= targetWidth && originalImageHeight <= targetHeight) || (targetWidth == 0 && targetHeight == 0)) {
            return sampleSize;
        }
        if (originalImageWidth >= originalImageHeight && targetWidth != 0) {
            sampleSize = originalImageWidth / targetWidth;
        } else if (originalImageWidth >= originalImageHeight && targetWidth == 0) {
            sampleSize = originalImageHeight / targetHeight;
        } else { // if (originalImageHeight >= originalImageWidth && targetHeight != 0)
            sampleSize = originalImageHeight / targetHeight;
        }

        if (sampleSize < 1) {
            sampleSize = 1;
        }
        return sampleSize;
    }

    public static Bitmap getSampledBitmap(String mFilePath ,int width , int height) throws IOException {
        byte [] lImageBytes = getImageBytes(mFilePath);
        Bitmap bitmap = null;
        if(lImageBytes != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, options);

            int sampleSize = getSampleSize(options.outWidth, options.outHeight, width, height);
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, options);
        }else{
            Log.d(TAG, "getSampledBitmap: call mFilePath " + mFilePath);
        }
        return bitmap;
    }

    private static class AvailableImagesCallable implements Callable<Cursor> {

        private Context mContext;

        AvailableImagesCallable(Context context) {
            mContext = context;
        }

        @Override
        public Cursor call() throws Exception {
            Cursor cursor = null;
            IUsbVolumeLister volumeLister = IUsbVolumeLister.Instance.getInterface();
            List<IUsbVolumeLister.UsbVolume> volumes = volumeLister.getMountedVolumes();
            if (volumes != null && !volumes.isEmpty()) {
                IUsbVolumeLister.UsbVolume volume = volumes.get(0);
                AvailableImagesQuery query = new AvailableImagesQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, volume.mMountPath);
                cursor = MediaStore.Images.Media.query(mContext.getContentResolver(), query.getUri(), query.getProjection(),
                        query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
            }
            return cursor;
        }
    }

    private static class AvailableImagesFetchTask extends FutureTask<Cursor> {

        private Handler mHandler;
        private ThumbnailDataListener mThumbnailDataListener;

        private AvailableImagesFetchTask(AvailableImagesCallable callable, Handler handler, ThumbnailDataListener thumbnailDataListener) {
            super(callable);
            mHandler = handler;
            mThumbnailDataListener = thumbnailDataListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Cursor cursor = get();
                    AvailableImagesFetchResult result = new AvailableImagesFetchResult(cursor, mThumbnailDataListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_AVAILABLE_IMAGES_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "AvailableImagesFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static abstract class ThumbnailImageCallable implements Callable<Bitmap> {

        private long mId;
        private int mWidth;
        private int mHeight;

        ThumbnailImageCallable(long id, int thumbnailWidth, int thumbnailHeight) {
            mId = id;
            mWidth = thumbnailWidth;
            mHeight = thumbnailHeight;
        }

        private long getId() {
            return mId;
        }

        protected int getWidth() {
            return mWidth;
        }

        protected int getHeight() {
            return mHeight;
        }
    }

    private static class FileThumbnailImageCallable extends ThumbnailImageCallable {

        private String mFilePath;

        FileThumbnailImageCallable(String filePath, long id, int thumbnailWidth, int thumbnailHeight) {
            super(id, thumbnailWidth, thumbnailHeight);
            mFilePath = filePath;
        }

        @Override
        public Bitmap call() throws Exception {
            byte [] lImageBytes = getImageBytes(mFilePath);
            if(lImageBytes != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, options);

                int sampleSize = getSampleSize(options.outWidth, options.outHeight, getWidth(), getHeight());
                options.inSampleSize = sampleSize;
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, options);
                return bitmap;
            }else{
                Log.d(TAG, "FileThumbnailImageCallable: call mFilePath " + mFilePath);
                return null;
            }
        }
    }

    private static byte[] getImageBytes(String mFilePath) throws IOException{
        if (mFilePath == null) return null;

        InputStream in = null;
        int MAX_BYTE_BUFFER_SIZE = 3*1024*1024;
        byte[] readBuffer = new byte[MAX_BYTE_BUFFER_SIZE];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len;
        try {
            File file = new File(mFilePath);
            in  = new FileInputStream(file);
            while ((len = in.read(readBuffer, 0, MAX_BYTE_BUFFER_SIZE)) != -1) {
                outputStream.write(readBuffer, 0, len);
            }
            return outputStream.toByteArray();
        }catch (IOException e){
            Log.d(TAG, "getImageBytes: ERROR: in reading mFilePath: " + mFilePath);
        }finally {
            if(in != null){
                in.close();
            }
        }
        return null;
    }

    private static class DefaultThumbnailImageCallable extends ThumbnailImageCallable {
        private int mDrawableResourceId;
        private Context mContext;

        DefaultThumbnailImageCallable(Context context, int drawableResourceId, long id, int thumbnailWidth, int thumbnailHeight) {
            super(id, thumbnailWidth, thumbnailHeight);
            mDrawableResourceId = drawableResourceId;
            mContext = context;
        }

        @Override
        public Bitmap call() throws Exception {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(mContext.getResources(), mDrawableResourceId, options);

            int sampleSize = getSampleSize(options.outWidth, options.outHeight, getWidth(), getHeight());
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(mContext.getResources(), mDrawableResourceId, options);
        }
    }



    private static class ThumbnailImageFetchTask extends FutureTask<Bitmap> {

        private long mId;
        private Handler mHandler;
        private ThumbnailBitmapFetchListener mThumbnailBitmapFetchListener;

        private ThumbnailImageFetchTask(ThumbnailImageCallable callable, Handler handler, ThumbnailBitmapFetchListener thumbnailBitmapFetchListener) {
            super(callable);
            mHandler = handler;
            mThumbnailBitmapFetchListener = thumbnailBitmapFetchListener;
            mId = callable.getId();
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Bitmap thumbnail = get();
                    ThumbnailImageFetchResult result = new ThumbnailImageFetchResult(mId, thumbnail, mThumbnailBitmapFetchListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_THUMBNAIL_IMAGE_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "ThumbnailImageFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static class ImageCallable implements Callable<Bitmap> {

        private Context mContext;
        private String mFilePath;
        private int mWidth;
        private int mHeight;

        ImageCallable(Context context, String filePath, int width, int height) {
            mContext = context;
            mFilePath = filePath;
            mWidth = width;
            mHeight = height;
        }

        @Override
        public Bitmap call() throws Exception {
            byte [] lImageBytes = getImageBytes(mFilePath);
            if(lImageBytes != null) {
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, bitmapOptions);

                int sampleSize = getSampleSize(bitmapOptions.outWidth, bitmapOptions.outHeight, mWidth, mHeight);
                bitmapOptions.inSampleSize = sampleSize;
                bitmapOptions.inJustDecodeBounds = false;
                bitmapOptions.inMutable = true;
                Bitmap bitmap = BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, bitmapOptions);
                return bitmap;
            }else{
                Log.d(TAG, "ImageCallable call: mFilePath " + mFilePath);
                return null;
            }
        }
    }

    private static class ResourceImageCallable implements Callable<Bitmap> {

        private Context mContext;
        private int mResourceId;
        private int mWidth;
        private int mHeight;

        ResourceImageCallable(Context context, int resourceId, int width, int height) {
            mContext = context;
            mResourceId = resourceId;
            mWidth = width;
            mHeight = height;
        }

        @Override
        public Bitmap call() throws Exception {
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(mContext.getResources(), mResourceId, bitmapOptions);

            int sampleSize = getSampleSize(bitmapOptions.outWidth, bitmapOptions.outHeight, mWidth, mHeight);
            bitmapOptions.inSampleSize = sampleSize;
            bitmapOptions.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mResourceId, bitmapOptions);
            return bitmap;
        }
    }

    private static class ImageFetchTask extends FutureTask<Bitmap> {

        private Handler mHandler;
        private DashboardDataManager.ImageFetchListener mImageFetchListener;

        private ImageFetchTask(Callable callable, Handler handler, DashboardDataManager.ImageFetchListener imageFetchListener) {
            super(callable);
            mHandler = handler;
            mImageFetchListener = imageFetchListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Bitmap bitmap = get();
                    ImageFetchResult result = new ImageFetchResult(bitmap, mImageFetchListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_IMAGE_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "ImageFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_AVAILABLE_IMAGES_FETCH_COMPLETE = 100;
        private static final int MSG_WHAT_THUMBNAIL_IMAGE_FETCH_COMPLETE = 101;
        private static final int MSG_WHAT_IMAGE_FETCH_COMPLETE = 102;

        private UiThreadHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Log.d(TAG, "handleMessage() called with: msg.what = [" + msg.what + "]");
            if (what == MSG_WHAT_AVAILABLE_IMAGES_FETCH_COMPLETE) {
                AvailableImagesFetchResult result = (AvailableImagesFetchResult) msg.obj;
                if (result.mThumbnailDataListener != null) {
                    result.mThumbnailDataListener.onAvailableImagesFetched(result.mCursor);
                }
                return;
            }

            if (what == MSG_WHAT_THUMBNAIL_IMAGE_FETCH_COMPLETE) {
                ThumbnailImageFetchResult result = (ThumbnailImageFetchResult) msg.obj;
                if (result.mThumbnailBitmapFetchListener != null) {
                    result.mThumbnailBitmapFetchListener.onThumbnailBitmapFetchComplete(result.mId, result.mBitmap);
                }
                return;
            }

            if (what == MSG_WHAT_IMAGE_FETCH_COMPLETE) {
                ImageFetchResult result = (ImageFetchResult) msg.obj;
                if (result.mImageFetchListener != null) {
                    result.mImageFetchListener.onImageFetchComplete(result.mBitmap);
                }
                return;
            }
        }
    }

    private static final class AvailableImagesFetchResult {
        ThumbnailDataListener mThumbnailDataListener;
        Cursor mCursor;

        private AvailableImagesFetchResult(Cursor cursor, ThumbnailDataListener listener) {
            mCursor = cursor;
            mThumbnailDataListener = listener;
        }
    }

    private static final class ThumbnailImageFetchResult {
        long mId;
        Bitmap mBitmap;
        ThumbnailBitmapFetchListener mThumbnailBitmapFetchListener;

        private ThumbnailImageFetchResult(long id, Bitmap bitmap, ThumbnailBitmapFetchListener listener) {
            mId = id;
            mBitmap = bitmap;
            mThumbnailBitmapFetchListener = listener;
        }
    }

    private static final class ImageFetchResult {
        Bitmap mBitmap;
        DashboardDataManager.ImageFetchListener mImageFetchListener;

        private ImageFetchResult(Bitmap bitmap, DashboardDataManager.ImageFetchListener listener) {
            mBitmap = bitmap;
            mImageFetchListener = listener;
        }
    }
}
