package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.LruCache;

import org.droidtv.defaultdashboard.data.ContentRatingStore;
import org.droidtv.defaultdashboard.data.ContentRatingStore.RatingInfo;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ContentRatingFetchCompleteListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ProgramDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ProgramThumbnailFetchListener;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.data.model.Program;
import org.droidtv.defaultdashboard.data.query.BcEpgProgramDataQuery;
import org.droidtv.defaultdashboard.data.query.IpEpgProgramDataQuery;
import org.droidtv.defaultdashboard.data.query.ProTvEpgProgramDataQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.data.query.TifEpgProgramDataQuery;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.tv.smarttv.provider.IIpEpgContract;
import com.mediatek.twoworlds.tv.MtkTvBanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sandeep.kumar on 15/11/2017.
 */

final class ProgramDataManager extends ContextualObject implements ContentRatingFetchCompleteListener {

    private static String TAG = "ProgramDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private ProgramThumbnailCache mProgramThumbnailCache;
    private ContentRatingStore mContentRatingStore;
    private ConcurrentHashMap<String, RatingInfo> mRatingMap;

    private static Program recyclableProgramObject;

    private static final String DELIMITER = "-";

    ProgramDataManager(Context context) {
        super(context);

        recyclableProgramObject = new Program.Builder().build();

        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.PROGRAM_DATA_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
        mProgramThumbnailCache = new ProgramThumbnailCache();

        initContentRatingMap();
    }

    void fetchProgramDataForChannel(final Channel channel, final ProgramDataListener listener) {
        ProgramDataFetchCallable callable = null;

        long nowProgramStartTime = System.currentTimeMillis();
        final boolean isTifChannel = Channel.isTifChannel(channel);
        final boolean isBcepg = DashboardDataManager.getInstance().isEpgSourceBcepg();
        final boolean isEpgSourceApp = DashboardDataManager.getInstance().isEpgSourceApp();
        Log.d(TAG, "fetchProgramDataForChannel: isTifChannel " + isTifChannel + " isBcepg " + isBcepg + " isEpgSourceApp " + isEpgSourceApp);
        if (isTifChannel) /* Third party TIF channel */ {
            callable = new TifProgramDataFetchCallable(getContext(), channel, new TifEpgProgramDataQuery(channel.getId(), nowProgramStartTime));
        } else if (isBcepg /* epg source is DVB */) {
            callable = new BcepgProgramDataFetchCallable(getContext(), channel, new BcEpgProgramDataQuery(channel));
        }else if(isEpgSourceApp){
            /*Query from EPG data App provider*/
            callable = new IProTvEpgProgramDataFetchCallable(getContext(), channel, new ProTvEpgProgramDataQuery(channel));
        } else /* epg source is IPEPG */ {
            // IPEPG db expects time in seconds. Hence, we will have to convert the start time in miliseconds to seconds
            long ipepgNowProgramStartTime = nowProgramStartTime / 1000L;
            callable = new IpepgProgramDataFetchCallable(getContext(), channel, new IpEpgProgramDataQuery(channel.getId(), ipepgNowProgramStartTime));
        }

        ProgramDataFetchTask task = new ProgramDataFetchTask(callable, mUiThreadHandler, new ProgramDataListener() {
            @Override
            public void onProgramDataFetchComplete(int channelId, Program program) {
                DdbLogUtility.logCommon(TAG, "onProgramDataFetchComplete channelId " + channelId + " isTifChannel " + isTifChannel + " isBcepg " + isBcepg);
                if (program == null && !isTifChannel && !isBcepg && !isEpgSourceApp) {
                    // IPEPG program data is not available. Fallback on BCEPG program data
                    channel.setFallbackToBcEpg(true);
                    Log.d(TAG, "onProgramDataFetchComplete: Fallback to BC-EPG");
                    ProgramDataFetchCallable bcepgCallable = new BcepgProgramDataFetchCallable(getContext(), channel, new BcEpgProgramDataQuery(channel));
                    mThreadPoolExecutor.execute(new ProgramDataFetchTask(bcepgCallable, mUiThreadHandler, listener));
                } else if (listener != null) {
                    listener.onProgramDataFetchComplete(channelId, program);
                }
            }
        });
        mThreadPoolExecutor.execute(task);
    }

    void fetchProgramThumbnail(Program program, Channel channel, ProgramThumbnailFetchListener listener) {
        int programId = program.getId();
        int channelId = channel.getId();
        // A composite key needs to be created as against simply using the program id as the key because program id
        // may be the same across different channels. Hence, to ensure uniqueness of the key
        // we create a composite id by concatenating the program id, a delimiter character and the channel id
        String compositeProgramId = getCompositeProgramId(programId, channelId);
        Bitmap logo = getThumbnailFromCache(compositeProgramId);
        if (logo != null) {
            if (listener != null) {
                listener.onProgramThumbnailFetchComplete(programId, channelId, logo);
            }
        } else {
            fetchProgramThumbnailInternal(program, channel, listener);
        }
    }

    void clearProgramThumbnailCache() {
        mProgramThumbnailCache.clear();
    }

    int fetchContentRatingStringId(String contentRating) {
        int stringId = -1;
        if (mRatingMap != null) {
            ContentRatingStore.RatingInfo ratingInfo = mRatingMap.get(contentRating);
            if (ratingInfo != null) {
                return ratingInfo.getStringId();
            }
        }
        return stringId;
    }

    int fetchContentRatingStringId(int contentRatingAgeHint) {
        if (mRatingMap != null) {
            Iterator iterator = mRatingMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ContentRatingStore.RatingInfo> entry = (Map.Entry) iterator.next();
                if (entry.getValue().getContentAgeHint() == contentRatingAgeHint) {
                    return entry.getValue().getStringId();
                }
            }
        }
        return -1;
    }

    int fetchContentRatingAgeHint(String contentRating) {
        int stringId = -1;
        if (mRatingMap != null) {
            ContentRatingStore.RatingInfo ratingInfo = mRatingMap.get(contentRating);
            if (ratingInfo != null) {
                return ratingInfo.getContentAgeHint();
            }
        }
        return stringId;
    }

    @Override
    public void onContentRatingFetchComplete(ConcurrentHashMap<String, RatingInfo> ratingMap) {
        mRatingMap.putAll(ratingMap);
    }

    private void initContentRatingMap() {
        mContentRatingStore = new ContentRatingStore(getContext());
        mRatingMap = new ConcurrentHashMap<String, RatingInfo>();
        FetchContentRatingCallable callable = new FetchContentRatingCallable(mContentRatingStore);
        FetchContentRatingTask task = new FetchContentRatingTask(callable, mUiThreadHandler, this);
        mThreadPoolExecutor.execute(task);
    }

    private void fetchProgramThumbnailInternal(Program program, Channel channel, final ProgramThumbnailFetchListener listener) {
        int programId = program.getId();
        int channelId = channel.getId();
        ProgramThumbnailFetchCallable callable = null;
        if (Channel.isTifChannel(channel)) { /* TIF channel */
            callable = new TifProgramThumbnailFetchCallable(getContext(), program, channel);
        } else {
            if (!DashboardDataManager.getInstance().isEpgSourceBcepg()) { /* epg source is IPEPG */
                callable = new IpepgProgramThumbnailFetchCallable(getContext(), program, channel);
            } else { /* epg source is BCEPG hence there is no thumbnail for program */
                if (listener != null) {
                    listener.onProgramThumbnailFetchComplete(programId, channelId, null);
                }
                return;
            }
        }

        ProgramThumbnailFetchTask task = new ProgramThumbnailFetchTask(callable, mUiThreadHandler, new ProgramThumbnailFetchListener() {

            @Override
            public void onProgramThumbnailFetchComplete(int programId, int channelId, Bitmap thumbnail) {
                if (thumbnail != null) {
                    if (listener != null) {
                        listener.onProgramThumbnailFetchComplete(programId, channelId, thumbnail);
                    }
                    addThumbnailToCache(getCompositeProgramId(programId, channelId), thumbnail);
                }
            }
        });
        mThreadPoolExecutor.execute(task);
    }

    private void addThumbnailToCache(String compositeProgramId, Bitmap logo) {
        mProgramThumbnailCache.addThumbnail(compositeProgramId, logo);
    }

    private Bitmap getThumbnailFromCache(String compositeProgramId) {
        return mProgramThumbnailCache.getThumbnail(compositeProgramId);
    }

    private String getCompositeProgramId(long programId, long channelId) {
        return programId + DELIMITER + channelId;
    }

    private static Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().
                query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    private static class TifProgramDataFetchCallable extends ProgramDataFetchCallable {

        private TifProgramDataFetchCallable(Context context, Channel channel, Query query) {
            super(context, channel, query);
        }

        @Override
        protected Program buildProgramFromCursor(Cursor cursor) {
            Program.buildTifProgramDataFromCursor(cursor, recyclableProgramObject);
            return recyclableProgramObject;
        }
    }

    private static class BcepgProgramDataFetchCallable extends ProgramDataFetchCallable {

        private BcepgProgramDataFetchCallable(Context context, Channel channel, Query query) {
            super(context, channel, query);
        }

        @Override
        protected Program buildProgramFromCursor(Cursor cursor) {
            Program.buildBcepgProgramDataFromCursor(getContext(), cursor, recyclableProgramObject);
            return recyclableProgramObject;
        }
    }

    private static class IpepgProgramDataFetchCallable extends ProgramDataFetchCallable {

        private IpepgProgramDataFetchCallable(Context context, Channel channel, Query query) {
            super(context, channel, query);
        }

        @Override
        protected Program buildProgramFromCursor(Cursor cursor) {
            Program.buildIpepgProgramDataFromCursor(cursor, recyclableProgramObject);
            return recyclableProgramObject;
        }
    }

    private static class IProTvEpgProgramDataFetchCallable extends ProgramDataFetchCallable {
        int mMapedChannelId ;
        private IProTvEpgProgramDataFetchCallable(Context context, Channel channel, Query query) {
            super(context, channel, query);
            mMapedChannelId = channel.getMappedId();
        }

        @Override
        protected Program buildProgramFromCursor(Cursor cursor) {
            Program.buildProTvEpgProgramDataFromCursor(getContext(), cursor, recyclableProgramObject);
            //Temp Fix till HeadEnd CR is implemented
            if(DashboardDataManager.getInstance().isNAFTA()) {
                if (mMapedChannelId == DashboardDataManager.getInstance().getLastSelectedChannelId()) {
                    DdbLogUtility.logCommon(ProgramDataManager.class.getName(), "isNAFTA");
                    String[] naftaRating = new String[1];
                    if (MtkTvBanner.getInstance() != null) {
                        try {
                            naftaRating[0] = MtkTvBanner.getInstance().getRating();
                        } catch (Exception e) {
                            Log.d(ProgramDataManager.class.getName(), "Some issue in calling current channel Rating :" + e.getMessage());
                            naftaRating[0] = null;
                        }
                    } else {
                        DdbLogUtility.logCommon(ProgramDataManager.class.getName(), "buildProgramFromCursor: MtkTvBanner.getInstance() is null");
                        naftaRating[0] = null;
                    }
                    recyclableProgramObject.setRatings(naftaRating);
                    DdbLogUtility.logCommon(ProgramDataManager.class.getName(), "Nafta Rating set to :"+naftaRating[0]);
                } else {
                    DdbLogUtility.logCommon(ProgramDataManager.class.getName(), "buildProgramFromCursor: " + "LSC ID :" + DashboardDataManager.getInstance().getLastSelectedChannelId() + "recyclableProgramObject :" + recyclableProgramObject.getRatings()[0]);
                }
            }
            return recyclableProgramObject;
        }
    }

    private static abstract class ProgramDataFetchCallable implements Callable<Program> {

        private Context mContext;
        private int mChannelId;
        private Query mQuery;

        private ProgramDataFetchCallable(Context context, Channel channel, Query query) {
            mContext = context;
            mChannelId = channel.getId();
            mQuery = query;
        }

        private int getChannelId() {
            return mChannelId;
        }

        protected Context getContext() {
            return mContext;
        }

        protected abstract Program buildProgramFromCursor(Cursor cursor);

        @Override
        public Program call() throws Exception {
            Cursor cursor = null;
            try {
                cursor = executeQuery(mContext, mQuery);
                if (cursor != null && cursor.moveToNext()) {
                    Program program = buildProgramFromCursor(cursor);
                    return program;
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Program data query failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }
    }

    private static final class ProgramDataFetchTask extends FutureTask<Program> {

        private int mChannelId;
        private Handler mHandler;
        private ProgramDataListener mProgramDataListener;

        private ProgramDataFetchTask(ProgramDataFetchCallable callable, Handler handler, ProgramDataListener programDataListener) {
            super(callable);
            mHandler = handler;
            mProgramDataListener = programDataListener;
            mChannelId = callable.getChannelId();
        }

        @Override
        protected void done() {
            try {
                DdbLogUtility.logCommon(TAG, "done() called");
                if (!isCancelled()) {
                    Program program = get();
                    ProgramDataFetchResult result = new ProgramDataFetchResult(mChannelId, program, mProgramDataListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_PROGRAM_DATA_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "ProgramDataFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class ProgramDataFetchResult {
        private Program mProgram;
        private int mChannelId;
        private ProgramDataListener mProgramDataListener;

        private ProgramDataFetchResult(int channelId, Program program, ProgramDataListener listener) {
            mChannelId = channelId;
            mProgram = program;
            mProgramDataListener = listener;
        }
    }

    private static final class TifProgramThumbnailFetchCallable extends ProgramThumbnailFetchCallable {

        private static final int CONNECT_TIMEOUT_MS = 30000;
        private static final int READ_TIMEOUT_MS = 30000;

        private String mThumbnailUrl;

        private TifProgramThumbnailFetchCallable(Context context, Program program, Channel channel) {
            super(context, program.getId(), channel.getId());
            mThumbnailUrl = program.getPosterArtUri();
        }

        @Override
        public Bitmap call() throws Exception {
            Bitmap bmp = null;
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;

            try {
                URL url = new URL(mThumbnailUrl);
                DdbLogUtility.logCommon(TAG, "TifProgramThumbnailFetchCallable url " + url);

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                httpURLConnection.setReadTimeout(READ_TIMEOUT_MS);
                httpURLConnection.setInstanceFollowRedirects(true);
                inputStream = httpURLConnection.getInputStream();
                bmp = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                Log.w(TAG, "MalformedURLException while fetching thumbnail for channel id:" + getChannelId() + ",programId:" + getProgramId());
            } catch (IOException e) {
                Log.w(TAG, "IOException while fetching thumbnail for channel id:" + getChannelId() + ",programId:" + getProgramId() + ".Reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.w(TAG, "IOException in TifProgramThumbnailFetchCallable. channeId:" + getChannelId() + ",programId:" + getProgramId() + ".Reason:" + e.getMessage());
                       Log.e(TAG,"Exception :" +e.getMessage());
                    }
                    inputStream = null;
                }

                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                    httpURLConnection = null;
                }
            }
            return bmp;
        }
    }

    private static final class IpepgProgramThumbnailFetchCallable extends ProgramThumbnailFetchCallable {

        private IpepgProgramThumbnailFetchCallable(Context context, Program program, Channel channel) {
            super(context, program.getId(), channel.getId());
        }

        @Override
        public Bitmap call() throws Exception {
            Uri thumbnailUri = Uri.parse(IIpEpgContract.CONTENT_URI_PROGRAM_DATA + "/" + getProgramId());
            Bitmap bmp = null;
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = getContext().getContentResolver().openFileDescriptor(thumbnailUri, "r");
                if (parcelFileDescriptor != null) {
                    bmp = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG, "FileNotFoundException while fetching thumbnail for channel id:" + getChannelId() + ",programId:" + getProgramId() + ".Reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            } finally {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e) {
                        Log.w(TAG, "IOException in IpepgProgramThumbnailFetchCallable. channel id:" + getChannelId() + ",programId:" + getProgramId() + ".Reason:" + e.getMessage());
                       Log.e(TAG,"Exception :" +e.getMessage());
                    }
                }
            }
            return bmp;
        }
    }

    private static abstract class ProgramThumbnailFetchCallable implements Callable<Bitmap> {

        private Context mContext;
        private int mProgramId;
        private int mChannelId;

        private ProgramThumbnailFetchCallable(Context context, int programId, int channelId) {
            mContext = context;
            mProgramId = programId;
            mChannelId = channelId;
        }

        protected int getChannelId() {
            return mChannelId;
        }

        protected int getProgramId() {
            return mProgramId;
        }

        protected Context getContext() {
            return mContext;
        }
    }

    private static final class ProgramThumbnailFetchTask extends FutureTask<Bitmap> {

        private Handler mHandler;
        private int mProgramId;
        private int mChannelId;
        private ProgramThumbnailFetchListener mProgramThumbnailFetchListener;

        private ProgramThumbnailFetchTask(ProgramThumbnailFetchCallable callable, Handler handler, ProgramThumbnailFetchListener listener) {
            super(callable);
            mHandler = handler;
            mProgramThumbnailFetchListener = listener;
            mProgramId = callable.getProgramId();
            mChannelId = callable.getChannelId();
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Bitmap thumbnail = get();
                    Message message = Message.obtain();
                    message.what = UiThreadHandler.MSG_WHAT_PROGRAM_THUMBNAIL_FETCH_COMPLETE;
                    message.obj = new ProgramThumbnailFetchResult(mProgramId, mChannelId, thumbnail, mProgramThumbnailFetchListener);
                    mHandler.sendMessage(message);
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "ProgramThumbnailFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private class FetchContentRatingTask extends FutureTask<ConcurrentHashMap<String, RatingInfo>> {

        private Handler mHandler;
        private ContentRatingFetchCompleteListener mContentRatingFetchCompleteListener;

        public FetchContentRatingTask(FetchContentRatingCallable callable, Handler handler, ContentRatingFetchCompleteListener listener) {
            super(callable);
            mHandler = handler;
            mContentRatingFetchCompleteListener = listener;
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                try {
                    ConcurrentHashMap<String, RatingInfo> ratingMap = get();
                    Message msg = Message.obtain();
                    msg.what = UiThreadHandler.MSG_WHAT_CONTENT_RATING_FETCH_COMPLETE;
                    msg.obj = new ContentRatingFetchResult(ratingMap, mContentRatingFetchCompleteListener);
                    mHandler.sendMessage(msg);
                } catch (InterruptedException | ExecutionException e) {
                    Log.w(TAG, "FetchContentRatingTask failed.reason:" + e.getMessage());
                   Log.e(TAG,"Exception :" +e.getMessage());
                }
            }
        }
    }

    private class FetchContentRatingCallable implements Callable<ConcurrentHashMap<String, RatingInfo>> {
        private ContentRatingStore mContentRatingStore;

        FetchContentRatingCallable(ContentRatingStore contentRatingStore) {
            mContentRatingStore = contentRatingStore;
        }

        @Override
        public ConcurrentHashMap<String, RatingInfo> call() throws Exception {
            mContentRatingStore.invalidate();
            return mContentRatingStore.getRatingStringMap();
        }
    }

    private static final class ProgramThumbnailFetchResult {
        private int mProgramId;
        private int mChannelId;
        private Bitmap mThumbnail;
        private ProgramThumbnailFetchListener mProgramThumbnailFetchListener;

        private ProgramThumbnailFetchResult(int programId, int channelId, Bitmap thumbnail, ProgramThumbnailFetchListener programThumbnailFetchListener) {
            mProgramId = programId;
            mChannelId = channelId;
            mThumbnail = thumbnail;
            mProgramThumbnailFetchListener = programThumbnailFetchListener;
        }
    }

    private static final class ContentRatingFetchResult {
        private ConcurrentHashMap<String, RatingInfo> mRatingMap;
        private ContentRatingFetchCompleteListener mContentRatingFetchCompleteListener;

        private ContentRatingFetchResult(ConcurrentHashMap<String, RatingInfo> ratingMap, ContentRatingFetchCompleteListener contentRatingFetchCompleteListener) {
            mRatingMap = ratingMap;
            mContentRatingFetchCompleteListener = contentRatingFetchCompleteListener;
        }
    }

    private static final class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_PROGRAM_DATA_FETCH_COMPLETE = 100;
        private static final int MSG_WHAT_PROGRAM_THUMBNAIL_FETCH_COMPLETE = 101;
        private static final int MSG_WHAT_CONTENT_RATING_FETCH_COMPLETE = 102;

        @Override
        public void handleMessage(Message msg) {
            DdbLogUtility.logCommon(TAG, "handleMessage() called with: msg.what = " + msg.what);
            if (msg.what == MSG_WHAT_PROGRAM_DATA_FETCH_COMPLETE) {
                ProgramDataFetchResult result = (ProgramDataFetchResult) msg.obj;
                if (result.mProgramDataListener != null) {
                    result.mProgramDataListener.onProgramDataFetchComplete(result.mChannelId, result.mProgram);
                }
                return;
            }

            if (msg.what == MSG_WHAT_PROGRAM_THUMBNAIL_FETCH_COMPLETE) {
                ProgramThumbnailFetchResult result = (ProgramThumbnailFetchResult) msg.obj;
                if (result.mProgramThumbnailFetchListener != null) {
                    result.mProgramThumbnailFetchListener.onProgramThumbnailFetchComplete(result.mProgramId, result.mChannelId, result.mThumbnail);
                }
                return;
            }

            if (msg.what == MSG_WHAT_CONTENT_RATING_FETCH_COMPLETE) {
                ContentRatingFetchResult result = (ContentRatingFetchResult) msg.obj;
                if (result.mContentRatingFetchCompleteListener != null) {
                    result.mContentRatingFetchCompleteListener.onContentRatingFetchComplete(result.mRatingMap);
                }
                return;
            }
        }
    }

    private static class ProgramThumbnailCache {
        private static final String TAG = "ProgramThumbnailCache";

        // Reserve 1/32th of the max runtime memory available for this LruCache in Kilo bytes
        private static final int CACHE_SIZE_IN_KBYTES = (int)((Runtime.getRuntime().maxMemory()/1024)/32);

        private final LruCache<String, Bitmap> mThumbnailCache;

        private ProgramThumbnailCache() {
            mThumbnailCache = new LruCache<String, Bitmap>(CACHE_SIZE_IN_KBYTES){
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getAllocationByteCount() / 1024;
                }
            };
        }

        private void clear() {
            mThumbnailCache.evictAll();
            clearLogos();
        }

        private void clearLogos() {
            Set<String> logoCacheKeys = mThumbnailCache.snapshot().keySet();
            for(String key: logoCacheKeys){
                recycleBitmap(key);
                mThumbnailCache.remove(key);
            }
        }

        private void recycleBitmap(String key) {
            Bitmap bitmap = mThumbnailCache.get(key);
            if(bitmap != null && (!bitmap.isRecycled())) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        private Bitmap getThumbnail(String compositeProgramId) {
            return mThumbnailCache.get(compositeProgramId);
        }

        private void addThumbnail(String compositeProgramId, Bitmap logo) {
            mThumbnailCache.put(compositeProgramId, logo);
        }
    }
}
