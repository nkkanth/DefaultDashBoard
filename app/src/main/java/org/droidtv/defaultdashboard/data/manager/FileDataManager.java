package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.FileDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ImageFile;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ImageFileFetchListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.AppUtil;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sandeep.kumar on 10/01/2018.
 */

final class FileDataManager extends ContextualObject {

    private static final String TAG = "FileDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;

    FileDataManager(Context context) {
        super(context);

        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.OTHER_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
    }

    void copyFile(String sourcePath, String destinationPath, FileDataListener listener) {
        DdbLogUtility.logCommon(TAG,"copyFile() called with:  sourcePath = [" + sourcePath + "], destinationPath = [" + destinationPath+"]");
        FileCopyCallable callable = new FileCopyCallable(getContext(), sourcePath, destinationPath);
        FileCopyTask task = new FileCopyTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    void deleteFile(String path, FileDataListener listener) {
        DdbLogUtility.logCommon(TAG, "deleteFile() called with: path = [" + path + "]");
        FileDeleteCallable callable = new FileDeleteCallable(getContext(), path);
        FileDeleteTask task = new FileDeleteTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    void fetchImageFileFromPath(String path, ImageFileFetchListener listener) {
        DdbLogUtility.logCommon(TAG, "fetchImageFileFromPath() called with: path = [" + path + "]");
        FetchImageFileCallable callable = new FetchImageFileCallable(getContext(), path);
        FetchImageFileTask task = new FetchImageFileTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    private static ImageFile getImageFileFromPath(String path) {
        DdbLogUtility.logCommon(TAG, "getImageFileFromPath() called with: path = [" + path + "]");
        File defaultBackgroundImageDirectory = new File(path);
        if (!defaultBackgroundImageDirectory.exists()) {
            return null;
        }

        File[] files = defaultBackgroundImageDirectory.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(files[0].getPath(), options);
        return new ImageFile(files[0], options.outWidth, options.outHeight);
    }

    private static final class FileCopyCallable extends FileCallable {

        private Context mContext;
        private String mSourcePath;
        private String mDestinationPath;

        FileCopyCallable(Context context, String sourcePath, String destinationPath) {
            mContext = context;
            mSourcePath = sourcePath;
            mDestinationPath = destinationPath;
        }

        @Override
        public Boolean call() throws Exception {
            int width = 0;
            int height = 0;
            //ScaleDown
            if(mDestinationPath.contains(Constants.PATH_HOTEL_LOGO)){
                //hotel logo usecase
                width = Constants.HOTEL_LOGO_WIDTH;
                height = Constants.HOTEL_LOGO_HEIGHT;
            }else if(mDestinationPath.contains(Constants.PATH_MAIN_BACKGROUND)){
                //Main Background
                width = Constants.MAIN_BACKGROUND_WIDTH;
                height = Constants.MAIN_BACKGROUND_HEIGHT;
            }else{
                 //Cast usecase
                width = Constants.MAIN_BACKGROUND_WIDTH;
                height = Constants.MAIN_BACKGROUND_HEIGHT;
            }
            try {
                Bitmap mBitmap = ImageDataManager.getSampledBitmap(mSourcePath ,width ,height);
                DdbLogUtility.log(TAG,"Bitmap size for :"+ mSourcePath+" is :"+mBitmap.getByteCount());
                File destinationFile = new File(mDestinationPath);
                if (!destinationFile.exists()) {
                    destinationFile.mkdirs();
                }
                String destinationPath = mDestinationPath + mSourcePath.substring(mSourcePath.lastIndexOf("/"));
                destinationFile = new File(destinationPath);
                FileOutputStream out = new FileOutputStream(destinationFile);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                if(mBitmap != null && !mBitmap.isRecycled()){
                    mBitmap.recycle();
                    mBitmap = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    private static final class FileDeleteCallable extends FileCallable {

        private Context mContext;
        private String mPath;

        FileDeleteCallable(Context context, String path) {
            mContext = context;
            mPath = path;
        }

        @Override
        public Boolean call() throws Exception {
            return AppUtil.deleteFile(mPath);
        }
    }

    private static abstract class FileCallable implements Callable<Boolean> {

    }

    private static final class FileCopyTask extends FileTask {

        FileCopyTask(FileCopyCallable callable, Handler handler, FileDataListener fileDataListener) {
            super(callable, handler, fileDataListener);
        }

        @Override
        protected String getName() {
            return "FileCopyTask";
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_FILE_COPY_COMPLETE;
        }
    }

    private static final class FileDeleteTask extends FileTask {

        FileDeleteTask(FileDeleteCallable callable, Handler handler, FileDataListener fileDataListener) {
            super(callable, handler, fileDataListener);
        }

        @Override
        protected String getName() {
            return "FileDeleteTask";
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_FILE_DELETE_COMPLETE;
        }
    }

    private static abstract class FileTask extends FutureTask<Boolean> {

        private Handler mHandler;
        private FileDataListener mFileDataListener;

        private FileTask(FileCallable callable, Handler handler, FileDataListener fileDataListener) {
            super(callable);
            mHandler = handler;
            mFileDataListener = fileDataListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Boolean success = get();
                    FileOperationResult result = new FileOperationResult(success, mFileDataListener);
                    Message message = Message.obtain(mHandler, getMessageType(), result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, getMessageType() + " failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }

        protected abstract int getMessageType();

        protected abstract String getName();
    }

    private static class FetchImageFileCallable implements Callable<ImageFile> {

        private Context mContext;
        private String mImageFilePath;

        FetchImageFileCallable(Context context, String imageFilePath) {
            mContext = context;
            mImageFilePath = imageFilePath;
        }

        @Override
        public ImageFile call() throws Exception {
            return getImageFileFromPath(mImageFilePath);
        }
    }

    private static class FetchImageFileTask extends FutureTask<ImageFile> {

        private Handler mHandler;
        private ImageFileFetchListener mImageFileFetchListener;

        private FetchImageFileTask(FetchImageFileCallable callable, Handler handler, ImageFileFetchListener imageFileFetchListener) {
            super(callable);
            mHandler = handler;
            mImageFileFetchListener = imageFileFetchListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    ImageFile file = get();
                    ImageFileResult result = new ImageFileResult(file, mImageFileFetchListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_FILE_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "FetchImageFileTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class FileOperationResult {
        boolean mSuccess;
        FileDataListener mFileDataListener;

        private FileOperationResult(boolean success, FileDataListener listener) {
            mSuccess = success;
            mFileDataListener = listener;
        }
    }

    private static final class ImageFileResult {
        ImageFile mImageFile;
        ImageFileFetchListener mImageFileFetchListener;

        private ImageFileResult(ImageFile file, ImageFileFetchListener listener) {
            mImageFile = file;
            mImageFileFetchListener = listener;
        }
    }

    private static final class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_FILE_COPY_COMPLETE = 100;
        private static final int MSG_WHAT_FILE_DELETE_COMPLETE = 101;
        private static final int MSG_WHAT_FILE_FETCH_COMPLETE = 102;

        private UiThreadHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            if (what == MSG_WHAT_FILE_COPY_COMPLETE) {
                FileOperationResult result = (FileOperationResult) msg.obj;
                if (result.mFileDataListener != null) {
                    result.mFileDataListener.onFileCopyComplete(result.mSuccess);
                }
                return;
            }

            if (what == MSG_WHAT_FILE_DELETE_COMPLETE) {
                FileOperationResult result = (FileOperationResult) msg.obj;
                if (result.mFileDataListener != null) {
                    result.mFileDataListener.onFileDeleteComplete(result.mSuccess);
                }
                return;
            }

            if (what == MSG_WHAT_FILE_FETCH_COMPLETE) {
                ImageFileResult result = (ImageFileResult) msg.obj;
                if (result.mImageFileFetchListener != null) {
                    result.mImageFileFetchListener.onImageFileFetched(result.mImageFile);
                }
                return;
            }
        }
    }
}
