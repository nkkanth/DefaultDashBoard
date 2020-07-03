package org.droidtv.defaultdashboard.data.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.philips.professionaldisplaysolutions.jedi.clone.appDataClone.IAppDataCloneControl;
import com.philips.professionaldisplaysolutions.jedi.clone.appDataClone.IAppDataCloneControl.IAppCloneCallback;
import com.philips.professionaldisplaysolutions.jedi.common.IJavaEnterpriseDisplayConstants;
import com.philips.professionaldisplaysolutions.jedi.exceptions.JEDIException;
import com.philips.professionaldisplaysolutions.jedi.jedifactory.JEDIFactory;

import org.droidtv.defaultdashboard.common.DDBXMlParser;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.CloneDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.CloneFileUriListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.AppUtil;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import androidx.core.content.FileProvider;

/**
 * Created by sandeep.kumar on 13/12/2017.
 */

final class CloneDataManager extends ContextualObject implements CloneFileUriListener, CloneDataListener, IAppCloneCallback {

    private static String TAG = "CloneDataManager";

    private static final String CLONE_DATA_DIRECTORY_NAME = "clone_data";
    private static final String CLONE_DATA_FILE_NAME = "clone_files.zip";
    private static final String CLONE_DATA_IDENTIFIER_NAME = "clone_identifier.txt";
    private static final String CLONE_DATA_IDENTIFIER_VERSION = "1.0";
    private static final String FILE_PROVIDER_AUTHORITY = "org.droidtv.defaultdashboard.clonedataprovider";
    private static final String FOLDER_DATABASE = "databases";
    private static final String FOLDER_SHAREDPREFERENCES = "shared_prefs";
    private static final String FOLDER_FILES = "files";
    private static final long MIN_DIR_SIZE = 4096L;

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;

    CloneDataManager(Context context) {
        super(context);
        DdbLogUtility.logCommon(TAG, "CloneDataManager constructor");
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.OTHER_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
        registerForCloneUpdates();
    }

    private void registerForCloneUpdates() {
        /*Android-P: Uncommnet below line once clone is available, Currenlty its commented for DDB bringup*/
        JEDIFactory.getInstance(IAppDataCloneControl.class).registerForCallback(getContext(), this);
    }

    private void startCloneIn() {
        CloneInCallable cloneInCallable = new CloneInCallable(getContext(), FILE_PROVIDER_AUTHORITY);
        CloneInTask cloneInTask = new CloneInTask(cloneInCallable, mUiThreadHandler, this);
        mThreadPoolExecutor.execute(cloneInTask);
    }

    private void startCloneOut() {
        CloneOutCallable cloneOutCallable = new CloneOutCallable(getContext(), FILE_PROVIDER_AUTHORITY);
        CloneOutTask cloneOutTask = new CloneOutTask(cloneOutCallable, mUiThreadHandler, this);
        mThreadPoolExecutor.execute(cloneOutTask);
    }

    private void applyCloneData() {
        CloneDataApplyCallable cloneDataApplyCallable = new CloneDataApplyCallable(getContext());
        CloneDataApplyTask cloneDataApplyTask = new CloneDataApplyTask(cloneDataApplyCallable, mUiThreadHandler, this);
        mThreadPoolExecutor.execute(cloneDataApplyTask);
    }

    private void cleanUpCloneData() {
        CloneDataDeleteCallable cloneDataDeleteCallable = new CloneDataDeleteCallable(getContext());
        CloneDataDeleteTask cloneDataDeleteTask = new CloneDataDeleteTask(cloneDataDeleteCallable, mUiThreadHandler, this);
        mThreadPoolExecutor.execute(cloneDataDeleteTask);
    }

    @Override
    public void cloneInFileUriCreated(Pair<Uri, Uri> cloneDataUriPair) {
        try {
            Uri cloneIdentifierUri = cloneDataUriPair.first;
            Uri cloneDataUri = cloneDataUriPair.second;
            DdbLogUtility.logCommon(TAG, "cloneInFileUriCreated cloneIdentifierUri " +cloneIdentifierUri + "  cloneDataUri " + cloneDataUri);
            getContext().grantUriPermission(IJavaEnterpriseDisplayConstants.CLONE_SERVICE_PACKAGE_NAME, cloneIdentifierUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContext().grantUriPermission(IJavaEnterpriseDisplayConstants.CLONE_SERVICE_PACKAGE_NAME, cloneDataUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            HashMap<IAppDataCloneControl.AppDataItemType, Uri> hashMap = new HashMap<>();
            hashMap.put(IAppDataCloneControl.AppDataItemType.CLONE_IDENTIFIER, cloneIdentifierUri);
            hashMap.put(IAppDataCloneControl.AppDataItemType.ZIPPED_CLONE_CONTENT, cloneDataUri);
            JEDIFactory.getInstance(IAppDataCloneControl.class).cloneInAppDataTo(hashMap); // Call to JEDI
        } catch (JEDIException e) {
            Log.d(TAG, "#### exception in cloneInFileUriCreated()");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }

    @Override
    public void cloneOutFileUriCreated(Pair<Uri, Uri> cloneDataUriPair) {
        try {
            Uri cloneIdentifierUri = cloneDataUriPair.first;
            Uri cloneDataUri = cloneDataUriPair.second;
            DdbLogUtility.logCommon(TAG, "cloneOutFileUriCreated cloneIdentifierUri " +cloneIdentifierUri + "  cloneDataUri " + cloneDataUri);
            getContext().grantUriPermission(IJavaEnterpriseDisplayConstants.CLONE_SERVICE_PACKAGE_NAME, cloneIdentifierUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().grantUriPermission(IJavaEnterpriseDisplayConstants.CLONE_SERVICE_PACKAGE_NAME, cloneDataUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            HashMap<IAppDataCloneControl.AppDataItemType, Uri> hashMap = new HashMap<>();
            hashMap.put(IAppDataCloneControl.AppDataItemType.CLONE_IDENTIFIER, cloneIdentifierUri);
            hashMap.put(IAppDataCloneControl.AppDataItemType.ZIPPED_CLONE_CONTENT, cloneDataUri);
            JEDIFactory.getInstance(IAppDataCloneControl.class).cloneOutAppDataFrom(hashMap); // Call to JEDI.
        } catch (JEDIException e) {
            Log.d(TAG, "#### exception in cloneOutFileUriCreated()");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
    }

    /**
     * Once Clone in is complete, need to give a callback to JEDI.
     *
     * @param success
     */
    @Override
    public void cloneDataApplied(boolean success) {
        DdbLogUtility.logCommon(TAG, "cloneDataApplied() called with: success = [" + success + "]");
        if (success) {
            JEDIFactory.getInstance(IAppDataCloneControl.class).appDataCloneInStatusChanged(IAppDataCloneControl.CloneStatus.APPLIED, 100);
			//DashboardDataManager.getInstance().applyCloneDataPrefs();
            try {
                LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION_CLONE_IN));
            }catch (Exception e){}
        } else {
            JEDIFactory.getInstance(IAppDataCloneControl.class).appDataCloneInStatusChanged(IAppDataCloneControl.CloneStatus.FAILED, 0);
        }
        Log.d(TAG, "Clone data applied: " + success);
    }

    @Override
    public void cloneDataDeleted(boolean success) {
        Log.d(TAG, "Clone data deleted: " + success);
    }

    private static File getCloneDataFile(File parent) {
        try {
            File cloneDataDirectory = new File(parent, CLONE_DATA_DIRECTORY_NAME);
            if (!cloneDataDirectory.exists()) {
                if (!cloneDataDirectory.mkdir()) {
                    Log.w(TAG, "Failed to create directory: " + CLONE_DATA_DIRECTORY_NAME);
                    return null;
                }
            }

            File cloneDataFile = new File(cloneDataDirectory, CLONE_DATA_FILE_NAME);
            if (!cloneDataFile.exists()) {
                if (!cloneDataFile.createNewFile()) {
                    Log.w(TAG, "Failed to create file: " + CLONE_DATA_FILE_NAME);
                    return null;
                }
            }
            return cloneDataFile;
        } catch (IOException e) {
            Log.w(TAG, "IOException in method getCloneDataFile()");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return null;
    }

    private static File getCloneIdentifierFile(File parent) {
        try {
            File cloneDataDirectory = new File(parent, CLONE_DATA_DIRECTORY_NAME);
            if (!cloneDataDirectory.exists()) {
                if (!cloneDataDirectory.mkdir()) {
                    Log.w(TAG, "Failed to create directory: " + CLONE_DATA_DIRECTORY_NAME);
                    return null;
                }
            }

            File cloneIdentifierFile = new File(cloneDataDirectory, CLONE_DATA_IDENTIFIER_NAME);
            if (!cloneIdentifierFile.exists()) {
                if (!cloneIdentifierFile.createNewFile()) {
                    Log.w(TAG, "Failed to create file: " + CLONE_DATA_IDENTIFIER_NAME);
                    return null;
                }else{
                    //we need to update the intial version, so that 1st time clone out will have the proper version number
                    writeInitialVersion(cloneIdentifierFile);
                }

            }
            return cloneIdentifierFile;
        } catch (IOException e) {
            Log.w(TAG, "IOException in method getCloneDataFile()");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return null;
    }

    private static boolean deleteCloneDataFile(File parent) {
        try {
            File cloneDataDirectory = new File(parent, CLONE_DATA_DIRECTORY_NAME);
            if (!cloneDataDirectory.exists()) {
                return true;
            }

            File[] children = cloneDataDirectory.listFiles();
            for (int i = 0; children != null && i < children.length; i++) {
                if (!children[i].getName().equals(CLONE_DATA_IDENTIFIER_NAME)) {
                    children[i].delete();
                }
            }
            return true;
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException in method deleteCloneDataFile()");
           Log.e(TAG,"Exception :" +e.getMessage());
        }
        return false;
    }

    private static File[] getCloneFileListExludeCloneDirectory(File parent) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !CLONE_DATA_DIRECTORY_NAME.equals(name);
            }
        };

        File[] files = parent.listFiles(filter);
        if (files != null) {
            return files;
        }

        return null;
    }

    private static File[] getCloneFileList(File parent) {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return FOLDER_FILES.equals(name) || Constants.MAPPED_DDB_SETTINGS_FILE_NAME.equals(name)
                                                 || Constants.FILE_NAME_APP_LIST.equals(name);
            }
        };
       return parent.listFiles(filter);
    }

    /**
     * To compress the Directory folder in to zipFile which is passed as first parameter.
     *
     * @param zipFile
     * @param directory
     * @return
     */
    private static boolean writeToZipFile(File zipFile, File directory, Context context) {
        DdbLogUtility.logCommon(TAG, "writeToZipFile name " + ((zipFile != null)? zipFile.getName() : null));
        URI baseDirectoryUri = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);
        InputStream in = null;
        OutputStream out = null;
        ZipOutputStream zout = null;
        try {
            out = new FileOutputStream(zipFile);
            zout = new ZipOutputStream(out);
            while (!queue.isEmpty()) {
                directory = queue.pop();
                File[] filesTobeCloned;

                if (directory.getName().equals(context.getDataDir().getName())) {
                    filesTobeCloned = getCloneFileList(directory); // To get only shared_prefs, files and databases folder if it is parent directory(Data directory)
                } else {
                    filesTobeCloned = getCloneFileListExludeCloneDirectory(directory); // To get all files, If it is child directory
                }
                for (File kid : filesTobeCloned) {
                    String name = baseDirectoryUri.relativize(kid.toURI()).getPath();// To get the hierarchy of file/folder.
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        in = new FileInputStream(kid);
                        copyFiles(in, zout);
                        zout.closeEntry();
                    }
                }
            }
            return true;
        } catch (Exception e) {
           Log.e(TAG,"Exception :" +e.getMessage());
        } finally {
            try {
                if (zout != null) {
                    zout.close();
                }
            } catch (IOException e) {
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return false;
    }

    /**
     * To unzip the cloned in file to data directory to replace the shared prefs, images and all app related data.
     *
     * @param zipfile
     * @param directory
     * @throws IOException
     */
    private static void unzip(File zipfile, File directory, Context context) throws IOException {
        DdbLogUtility.logCommon(TAG, "unzip " + zipfile.getName());
        deleteFilesDirectory(context.getFilesDir());
        try(ZipFile zfile = new ZipFile(zipfile)){
            Enumeration<? extends ZipEntry> entries = zfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File file = new File(directory, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = zfile.getInputStream(entry);
                        out = new FileOutputStream(file);
                        copyFiles(in, out);
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG,"Exception :" +e.getMessage());
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                        }
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            }
            // compress and replace files here
            String imagesDirectory = context.getFilesDir() + Constants.PATH_IMAGES;

            File dir = new File(imagesDirectory);
            if(dir.isDirectory()){
                File[] directoryListing = dir.listFiles();
                for(File child:directoryListing){
                    if(child.isDirectory()){
                        File[] fileListingInner = child.listFiles();
                        for(File innerChild:fileListingInner){
                            String mFilePath = innerChild.getAbsolutePath();
                            Log.d(TAG,"File compression called for :"+mFilePath);
                            compressAndReplaceImage(mFilePath);
                            break;
                        }
                    }
                }
            }
        }catch (IOException e){
        }
    }

    private static void copyFiles(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    /**
     * To delete the files in File directory to avoid multiple images/Datas.
     *
     * @param parentDirectory
     */
    private static void deleteFilesDirectory(File parentDirectory) {
        Deque<File> queue = new LinkedList<File>();
        queue.push(parentDirectory);
        while (!queue.isEmpty()) {
            parentDirectory = queue.pop();
            File[] filesTobeDeleted = getCloneFileListExludeCloneDirectory(parentDirectory);
            if (filesTobeDeleted != null) {
                for (File kid : filesTobeDeleted) {
                    if (kid.isDirectory() && kid.listFiles().length > 0) {
                        queue.push(kid);
                    } else {
                        kid.delete();
                    }
                }
            }
        }
    }

    public void prepareForCloningOutAppData() {
        DdbLogUtility.logCommon(TAG, "prepareForCloningOutAppData");
        startCloneOut();

    }

    public void prepareForCloningInAppData() {
        DdbLogUtility.logCommon(TAG, "prepareForCloningInAppData");
        startCloneIn();
    }

    public void onAppDataCloneOutStatusChanged(IAppDataCloneControl.CloneStatus cloneStatus) {
        DdbLogUtility.logCommon(TAG, "onAppDataCloneOutStatusChanged cloneStatus " + cloneStatus);
        switch (cloneStatus) {
            case FAILED:
            case COPIED:
                cleanUpCloneData();
                break;
            case COPYING:
                break;
        }
    }

    public void onAppDataCloneInStatusChanged(IAppDataCloneControl.CloneStatus cloneStatus) {
        DdbLogUtility.logCommon(TAG, "onAppDataCloneInStatusChanged cloneStatus " + cloneStatus);
        switch (cloneStatus) {
            case COPIED:
                applyCloneData();
                break;
            case FAILED:
                break;
            case COPYING:
                break;
        }
    }

    private static class CloneOutCallable extends CloneCallable {

        private CloneOutCallable(Context context, String authority) {
            super(context, authority);
        }

        @Override
        public Pair<Uri, Uri> call() throws Exception {

            File cloneDataFile = getCloneDataFile(getContext().getFilesDir());
            File cloneIdentifierFile = getCloneIdentifierFile(getContext().getFilesDir());
            if (cloneDataFile == null || cloneIdentifierFile == null) {
                Log.i(TAG, "Zip file doesn't exists");
                return null;
            }

            File[] filesToBeCloned = getCloneFileList(getContext().getDataDir());
            if (filesToBeCloned == null || filesToBeCloned.length == 0) {
                Log.i(TAG, "Nothing to be cloned, Data directory is empty.");
                deleteCloneDataFile(getContext().getFilesDir());
                return null;
            }
            SharedPreferences ddbSharedPref = DashboardDataManager.getInstance().getDefaultSharedPreferences();
            Map  ddbSettingValues = ddbSharedPref.getAll();
            DDBXMlParser.getInstance(getContext()).createCloneOutAppListXml(getContext());
            boolean createXmlFile = DDBXMlParser.getInstance(getContext()).createMappedXmlFromDDBSettingPref(getContext(), ddbSettingValues);
            boolean writeSucceeded = writeToZipFile(cloneDataFile, getContext().getDataDir(), getContext());
            if (writeSucceeded) {
                Uri cloneDataFileUri = FileProvider.getUriForFile(getContext(), getAuthority(), cloneDataFile);
                Uri cloneIdentifierFileUri = FileProvider.getUriForFile(getContext(), getAuthority(), cloneIdentifierFile);
                return new Pair<>(cloneIdentifierFileUri, cloneDataFileUri);
            }

            return null;
        }
    }

    private static class CloneOutTask extends CloneTask {

        private CloneOutTask(CloneOutCallable callable, Handler handler, CloneFileUriListener listener) {
            super(callable, handler, listener);
        }

        @Override
        protected String getName() {
            return "CloneOutTask";
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_CLONE_OUT_COMPLETE;
        }
    }

    private static final class CloneInCallable extends CloneCallable {

        private CloneInCallable(Context context, String authority) {
            super(context, authority);
        }

        @Override
        public Pair<Uri, Uri> call() throws Exception {
            File cloneDataFile = getCloneDataFile(getContext().getFilesDir());
            File cloneIdentifierFile = getCloneIdentifierFile(getContext().getFilesDir());
            if (cloneDataFile == null) {
                return null;
            }
            Uri cloneDataFileUri = FileProvider.getUriForFile(getContext(), getAuthority(), cloneDataFile);
            Uri cloneIdentifierFileUri = FileProvider.getUriForFile(getContext(), getAuthority(), cloneIdentifierFile);
            return new Pair<>(cloneIdentifierFileUri, cloneDataFileUri);
        }
    }

    private static final class CloneInTask extends CloneTask {

        private CloneInTask(CloneInCallable callable, Handler handler, CloneFileUriListener listener) {
            super(callable, handler, listener);
        }

        @Override
        protected String getName() {
            return "CloneInTask";
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_CLONE_IN_COMPLETE;
        }
    }

    private static final class CloneDataDeleteCallable extends CloneDataCallable {

        private CloneDataDeleteCallable(Context context) {
            super(context);
        }

        @Override
        public Boolean call() throws Exception {
            return deleteCloneDataFile(getContext().getFilesDir());
        }
    }


    private static final class CloneDataDeleteTask extends CloneDataTask {

        private CloneDataDeleteTask(CloneDataDeleteCallable callable, Handler handler, CloneDataListener listener) {
            super(callable, handler, listener);
        }

        @Override
        protected String getName() {
            return "CloneDataDeleteTask";
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_CLONE_DATA_DELETE_COMPLETE;
        }
    }

    private static final class CloneDataApplyCallable extends CloneDataCallable {

        private CloneDataApplyCallable(Context context) {
            super(context);
        }

        @Override
        public Boolean call() throws Exception {
            File cloneDataFile = getCloneDataFile(getContext().getFilesDir());
            if (cloneDataFile == null) {
                Log.w(TAG, "Clone data file does not exist");
                return false;
            }

            // If the clone data file did not exist but was created as a result of calling getCloneDataFile() above
            // then its file size will be 0
            if (cloneDataFile.length() == 0L) {
                Log.w(TAG, "Clone data file does not exist");
                deleteCloneDataFile(getContext().getFilesDir());
                return false;
            }

            try {
                unzip(getCloneDataFile(getContext().getFilesDir()), getContext().getDataDir(), getContext());
                DDBXMlParser.getInstance(getContext()).readAppListXmlAndUpdateDB(getContext());
                DDBXMlParser.getInstance(getContext()).parseClonnedInXmlNUpdateSharedPref();
                copySharingImageToCastAppDir();
                return true;
            } catch (IOException e) {
               Log.e(TAG,"Exception :" +e.getMessage());
            }
            return false;
        }

        private void copySharingImageToCastAppDir() {
            String shareBGPath = getContext().getFilesDir() + Constants.PATH_SHARING_BACKGROUND;
            DdbLogUtility.logCommon(TAG, "copySharingImageToCastAppDir() shareBGPath " + shareBGPath);
            File file = new File(shareBGPath);
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                String filePath = files[0].getAbsolutePath();
                DdbLogUtility.logCommon(TAG, "copySharingImageToCastAppDir: length " + file.length());
                try {
                    if(files.length > MIN_DIR_SIZE) {
                        AppUtil.deleteFile(Constants.PATH_CAST_APP_SHARING_BACKGROUND);
                    }
                    AppUtil.copyFile(filePath, Constants.PATH_CAST_APP_SHARING_BACKGROUND);
                    DashboardDataManager.getInstance().setPermissions(Constants.PATH_CAST_APP_SHARING_BACKGROUND, filePath);
                    DashboardDataManager.getInstance().isClonedSharingImageBackground(true);
                }catch (IOException e){Log.e(TAG,"Exception :"+e.getMessage());}
            }
        }
    }
	
    private static void compressAndReplaceImage(String mSourcePath) throws IOException {
        int width = 0;
        int height = 0;
        //ScaleDown
        if(mSourcePath.contains(Constants.PATH_HOTEL_LOGO)){
            //hotel logo usecase
            width = Constants.HOTEL_LOGO_WIDTH;
            height = Constants.HOTEL_LOGO_HEIGHT;
        }else if(mSourcePath.contains(Constants.PATH_MAIN_BACKGROUND)){
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
            FileOutputStream out = new FileOutputStream(mSourcePath);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            if(mBitmap != null && !mBitmap.isRecycled()){
                mBitmap.recycle();
                mBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class CloneDataApplyTask extends CloneDataTask {

        private CloneDataApplyTask(CloneDataApplyCallable callable, Handler handler, CloneDataListener listener) {
            super(callable, handler, listener);
        }

        @Override
        protected String getName() {
            return "CloneDataApplyTask";
        }

        @Override
        protected int getMessageType() {
            return UiThreadHandler.MSG_WHAT_CLONE_DATA_APPLY_COMPLETE;
        }
    }

    private static abstract class CloneCallable implements Callable<Pair<Uri, Uri>> {

        private Context mContext;
        private String mAuthority;

        private CloneCallable(Context context, String authority) {
            mContext = context;
            mAuthority = authority;
        }

        protected final String getAuthority() {
            return mAuthority;
        }

        protected final Context getContext() {
            return mContext;
        }
    }

    private static abstract class CloneTask extends FutureTask<Pair<Uri, Uri>> {

        private Handler mHandler;
        private CloneFileUriListener mCloneFileUriListener;

        private CloneTask(CloneCallable callable, Handler handler, CloneFileUriListener listener) {
            super(callable);
            mHandler = handler;
            mCloneFileUriListener = listener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Pair<Uri, Uri> cloneDataUriPair = get();
                    CloneFileUriResult result = new CloneFileUriResult(cloneDataUriPair, mCloneFileUriListener);
                    Message message = Message.obtain(mHandler, getMessageType(), result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, getName() + " failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }

        protected abstract String getName();

        protected abstract int getMessageType();
    }

    private static abstract class CloneDataCallable implements Callable<Boolean> {

        private Context mContext;

        private CloneDataCallable(Context context) {
            mContext = context;
        }

        protected final Context getContext() {
            return mContext;
        }
    }

    private static abstract class CloneDataTask extends FutureTask<Boolean> {

        private Handler mHandler;
        private CloneDataListener mCloneDataListener;

        private CloneDataTask(CloneDataCallable callable, Handler handler, CloneDataListener listener) {
            super(callable);
            mHandler = handler;
            mCloneDataListener = listener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Boolean succeeded = get();
                    CloneDataResult result = new CloneDataResult(succeeded, mCloneDataListener);
                    Message message = Message.obtain(mHandler, getMessageType(), result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, getName() + " failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }

        protected abstract String getName();

        protected abstract int getMessageType();
    }


    private static class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_CLONE_IN_COMPLETE = 100;
        private static final int MSG_WHAT_CLONE_OUT_COMPLETE = 101;
        private static final int MSG_WHAT_CLONE_DATA_DELETE_COMPLETE = 102;
        private static final int MSG_WHAT_CLONE_DATA_APPLY_COMPLETE = 103;

        private UiThreadHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCommon(TAG, "UiThreadHandler handleMessage msg.what " +msg.what);
            if (what == MSG_WHAT_CLONE_IN_COMPLETE) {
                CloneFileUriResult result = (CloneFileUriResult) msg.obj;
                if (result.mCloneFileUriListener != null) {
                    result.mCloneFileUriListener.cloneInFileUriCreated(result.mCloneDataUriPair);
                }
                return;
            }

            if (what == MSG_WHAT_CLONE_OUT_COMPLETE) {
                CloneFileUriResult result = (CloneFileUriResult) msg.obj;
                if (result.mCloneFileUriListener != null) {
                    result.mCloneFileUriListener.cloneOutFileUriCreated(result.mCloneDataUriPair);
                }
                return;
            }

            if (what == MSG_WHAT_CLONE_DATA_APPLY_COMPLETE) {
                CloneDataResult result = (CloneDataResult) msg.obj;
                if (result.mCloneDataListener != null) {
                    result.mCloneDataListener.cloneDataApplied(result.mSucceeded);
                }
                return;
            }

            if (what == MSG_WHAT_CLONE_DATA_DELETE_COMPLETE) {
                CloneDataResult result = (CloneDataResult) msg.obj;
                if (result.mCloneDataListener != null) {
                    result.mCloneDataListener.cloneDataDeleted(result.mSucceeded);
                }
                return;
            }
        }
    }

    private static final class CloneFileUriResult {
        private Pair<Uri, Uri> mCloneDataUriPair;
        private CloneFileUriListener mCloneFileUriListener;

        private CloneFileUriResult(Pair<Uri, Uri> cloneDataUriPair, CloneFileUriListener listener) {
            mCloneDataUriPair = cloneDataUriPair;
            mCloneFileUriListener = listener;
        }
    }

    private static final class CloneDataResult {
        private Boolean mSucceeded;
        private CloneDataListener mCloneDataListener;

        private CloneDataResult(Boolean succeeded, CloneDataListener listener) {
            mSucceeded = succeeded;
            mCloneDataListener = listener;
        }
    }

    private static void writeInitialVersion(File fileName){
        PrintWriter pw = null;
        try {
            pw=new PrintWriter(fileName);
            pw.write(CLONE_DATA_IDENTIFIER_VERSION);
            pw.flush();

        } catch (Exception e) {
           Log.e(TAG, "Could not update the clone_identifier.txt");
        } finally {
            if(null != pw){
                pw.close();
            }
        }
    }
}
