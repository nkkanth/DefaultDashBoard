package org.droidtv.defaultdashboard.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.ImageDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

public class DDBImageLoader {

    private final static String TAG = DDBImageLoader.class.getSimpleName();
    private final static ImageLruCache mImageCache = new ImageLruCache(0);
    private final static UiHandler mUIHandler = new UiHandler(Looper.getMainLooper());
    private final static ThreadPoolExecutor mImageThreadExcecutor =
            ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.IMAGE_THREAD_THREAD_POOL_EXECUTOR);
    private static boolean isChapterVisible = true;

    private DDBImageLoader() {
        
    }

	private static Vector<String> mUnBoundItems = new Vector<String>(10);
	
    public static ImageRequest fromUrl(String url) {
        ImageRequest imageRequest = new ImageRequest();
        imageRequest.setUrl(url);
        return imageRequest;
    }

    public static void setChapterVisibility(boolean displayed) {
        isChapterVisible = displayed;
    }

    public static class ImageRequest {
        String mUrl;
        ImageView mImageView;
        Drawable mPlaceHolder;
        Bitmap mBitmap;
        int eventId;

        protected String getUrl() {
            return mUrl;
        }

        protected ImageRequest setUrl(String url) {
            this.mUrl = url;
            return this;
        }

        public ImageRequest setEventId(int _id){
            this.eventId = _id;
            return this;
        }

        protected ImageView getImageView() {
            return mImageView;
        }

        public ImageRequest inView(ImageView imageView) {
            this.mImageView = imageView;
            return this;
        }

        synchronized public void fetch() {
            ImageLruCache.DDBBitmap bitmap = mImageCache.getBitmap(getKey());
            if(!isChapterVisible){
                if(mUIHandler.hasMessages(UiHandler.MSG_IMAGE_FETCH_START)) {
                    mUIHandler.removeMessages(UiHandler.MSG_IMAGE_FETCH_START);
                }
                return;
            }
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap.getImage());
				bitmap.addReference();
                return;
            }          
            if (mPlaceHolder != null) {
                mImageView.setImageDrawable(mPlaceHolder);
            }
			if(mUnBoundItems.contains(getKey())){
				mUnBoundItems.remove(getKey());
			}
            Message message = mUIHandler.obtainMessage();
            message.what = UiHandler.MSG_IMAGE_FETCH_START;
            message.obj = this;
            message.sendToTarget();
        }

        protected void setImageBitmap() {
            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
            }
        }

        public ImageRequest placeHolder(Drawable placeHolder) {
            mPlaceHolder = placeHolder;
            return this;
        }

        public String getKey(){
            return /*this.mUrl + "/" +*/ Integer.toString(eventId);
        }

    }

    private static class ImageFetchCallable implements Callable<Bitmap> {
        private final ImageRequest mRequest;

        ImageFetchCallable(ImageRequest request) {
            mRequest = request;
        }

        @Override
        public Bitmap call() throws Exception {
            return downloadImage(mRequest.getUrl());
        }

        private Bitmap downloadImage(String _url)
        {
            DdbLogUtility.logCommon(TAG, "downloadImage "+_url);
            InputStream in = null;
            HttpURLConnection conn = null;
            Bitmap bMap = null;
			byte[] lImageBytes;
            try {
                lImageBytes = getBytesFromUrl(_url ,conn);
                if (lImageBytes != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, options);

                    int targetHeight = mRequest.mImageView.getResources().getDimensionPixelSize(R.dimen.app_recommendation_shelf_item_height);
                    options.inSampleSize = ImageDataManager.getSampleSize(options.outWidth, options.outHeight, 0, targetHeight);
                    options.inJustDecodeBounds = false;
                    options.inMutable = true;
                    bMap = BitmapFactory.decodeByteArray(lImageBytes, 0, lImageBytes.length, options);
                } else {
                    DdbLogUtility.logCommon(TAG, "Inputstream is null for url :" + _url);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }finally {
                try {
                    if (conn != null) conn.disconnect();
                }catch (Exception e) {
                    Log.d(TAG, "downloadImage: ERROR: " + e.getLocalizedMessage());
                }
            }
            return bMap;
        }

		private byte[] getBytesFromUrl(String _url, HttpURLConnection conn){
			InputStream in = null;
			URL url = null;
			int MAX_BYTE_BUFFER_SIZE = 3*1024*1024;
			int CONNECTION_TIMEOUT = 5000;
			try {
				url = new URL(_url);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setConnectTimeout(CONNECTION_TIMEOUT);
				conn.setReadTimeout(CONNECTION_TIMEOUT);
				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode >= 200 && responseCode < 400) {
					byte[] readBuffer = new byte[MAX_BYTE_BUFFER_SIZE];
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					int len;
					in = conn.getInputStream();

					while ((len = in.read(readBuffer, 0, MAX_BYTE_BUFFER_SIZE)) != -1) {
					  outputStream.write(readBuffer, 0, len);
					}
					return outputStream.toByteArray();
				}else{
					DdbLogUtility.logCommon(TAG ,"Aborted due to response code :"+responseCode +" for url :"+_url);
				}
			} catch (Exception e) {
				DdbLogUtility.logCommon(TAG, "Exception getHTTPConnectionInputStream : "+e.getMessage()+" for url :"+_url);
			}
			return null;
		}
    }

    private static class ImageFetchTask extends FutureTask<Bitmap> {
        private final ImageRequest mRequest;
        private final UiHandler mHandler;

        public ImageFetchTask(ImageRequest request, UiHandler handler, @NonNull Callable<Bitmap> callable) {
            super(callable);
            mRequest = request;
            mHandler = handler;
        }

        @Override
        protected void done() {
            try {
                Bitmap bitmap = get();
                mRequest.mBitmap = bitmap;
                Message message = mHandler.obtainMessage();
                message.what = UiHandler.MSG_IMAGE_FETCH_COMPLETED;
                message.obj = mRequest;
                message.sendToTarget();
            } catch (Exception e) {
                Log.d(TAG, "ImageFetchTask fail : " + e.getMessage());
            }
        }
    }

    private static class UiHandler extends Handler {
        public final static int MSG_IMAGE_FETCH_START = 1000;
        public final static int MSG_IMAGE_FETCH_COMPLETED = 2000;

        public UiHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_IMAGE_FETCH_START:
                    ImageRequest fetch_request = (ImageRequest) msg.obj;
                    String keyFetchStart = fetch_request.getKey();//.getUrl() + "/" + fetch_request.getChannelId();
                    fetch_request.mImageView.setTag(keyFetchStart);
                    DdbLogUtility.logCommon(TAG, "handleMessage: MSG_IMAGE_FETCH_START url " +keyFetchStart);
                    ImageFetchCallable imageFetchCallable = new ImageFetchCallable(fetch_request);
                    ImageFetchTask imageFetchTask = new ImageFetchTask(fetch_request, mUIHandler, imageFetchCallable);
                    mImageThreadExcecutor.execute(imageFetchTask);

                    break;
                case MSG_IMAGE_FETCH_COMPLETED:
                    ImageRequest request = (ImageRequest) msg.obj;
                    ImageLruCache.DDBBitmap bitmap = mImageCache.getBitmap(request.getKey());
                    /*if available in LRUCache the get old bitmap from LRUCache and set it to Imageview and recycle the newly downloaded bitmap*/
                    if(bitmap != null){
                        request.getImageView().setImageBitmap(bitmap.getImage());
                        bitmap.addReference();
                        recycleBitmap(request);
                        return;
                    }
                    if(isBitmapCanBeProcessed(request)) {
                        ImageLruCache.DDBBitmap bitMap = mImageCache.new DDBBitmap(request.mBitmap);
                        mImageCache.putBitmap(request.getKey(), bitMap);
                        DdbLogUtility.logCommon(TAG, "handleMessage: MSG_IMAGE_FETCH_COMPLETED url " + request.getKey());
                        if (mUnBoundItems.contains(request.getKey())) {
                            bitMap.removeReference();
                            mUnBoundItems.remove(request.getKey());
                        } else {
                            request.setImageBitmap();
                        }
                    }else{
                        recycleBitmap(request);
                    }
                    request = null;
                    break;
            }
        }
    }

    private static void recycleBitmap(ImageRequest request) {
        if(isValidBitmap(request)){
            Bitmap b = request.mBitmap;
            if(b != null && !b.isRecycled()){
                Log.d(TAG, "recycleBitmap: recycled bitmap : " + request.getKey());
                b.recycle();
                b = null;
                request = null;
            }
        }
    }

    private static boolean isBitmapCanBeProcessed(ImageRequest request) {
        boolean cabBeProcessed = false;
        if (!isValidKey(request.getKey())) return cabBeProcessed;

        if (!isValidBitmap(request) || isAvailableInLruCache(request) || !isChapterVisible) {
            cabBeProcessed = false;
        }else{
            cabBeProcessed = true;
        }
        DdbLogUtility.logCommon(TAG, "isBitmapCanBeProcessed() returned: " + cabBeProcessed);
        return cabBeProcessed;
    }

    private static boolean isValidKey(String key) {
        return !TextUtils.isEmpty(key);
    }

    private static boolean isAvailableInLruCache(ImageRequest request) {
        return mImageCache.getBitmap(request.getKey()) != null;
    }


    private static boolean isValidBitmap(ImageRequest request) {
        return request.mBitmap != null && (!request.mBitmap.isRecycled());
    }

	public synchronized static void cleanupReference(String key){
		if(!mImageCache.cleanupReference(key)){
		    if(!mUnBoundItems.contains(key)){
                mUnBoundItems.add(key);
            } }
	}

    public static void cleanImageStore() {
        if(mImageCache != null) {
            mImageCache.clearCache();
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    public static void clearEvictedBitmaps(){
	android.util.Log.d(TAG, "clearEvictedBitmaps");
        if(mImageCache != null){
            mUnBoundItems.clear();
            mImageCache.cleanupAllReferences();
            mImageCache.RecycleEvictedBitmapPool();
        }
    }
}
