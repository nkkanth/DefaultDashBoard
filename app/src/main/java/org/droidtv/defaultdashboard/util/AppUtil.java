package org.droidtv.defaultdashboard.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.query.HtvAppQuery;
import org.droidtv.defaultdashboard.data.query.Query;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.htv.provider.HtvContract;
import org.droidtv.tv.os.storage.IUsbVolumeLister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;

public class AppUtil {

    private static final String TAG = AppUtil.class.getSimpleName();

    public static String getUsbSmartInfoPath() {
        String retSmartInfoPath = "";
        IUsbVolumeLister volumeLister = IUsbVolumeLister.Instance.getInterface();
        if (volumeLister == null) {
            Log.d(TAG, "getUsbSmartInfoPath() - IUsbVolumeLister is null");
            return retSmartInfoPath;
        }
        List<IUsbVolumeLister.UsbVolume> volumes = volumeLister.getMountedVolumes();
        Log.d(TAG, "getUsbSmartInfoPath() - volumes size " + volumes.size());
        String smartInfoFolderPath = DashboardDataManager.getInstance().isNAFTA() ? Constants.SMART_INFO_NAFTA_FILE_PATH_USB : Constants.SMART_INFO_EU_FILE_PATH_USB;
        for (IUsbVolumeLister.UsbVolume volume : volumes) {
            String mountPath = volume.mMountPath.trim();
            String deviceName = volume.mLabel;
            Log.d(TAG, "getUsbSmartInfoPath() - mountPath: " + mountPath + ", deviceName: " + deviceName);
            if ((null != mountPath) && !mountPath.isEmpty()) {
                String smartInfoPath = mountPath + smartInfoFolderPath;
                if (isDirectoryExist(smartInfoPath)) {
                    Log.d(TAG, "getUsbSmartInfoPath() - Found SmartInfo clone folder in USB");
                    retSmartInfoPath = smartInfoPath;
                    break;
                }
            }
        }
        DdbLogUtility.logRecommendationChapter(TAG, "getUsbSmartInfoPath " + retSmartInfoPath);
        return retSmartInfoPath;
    }

    public static boolean copyFile(String sourcePath, String destinationPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File destinationFile = new File(destinationPath);
        if (!destinationFile.exists()) {
            destinationFile.mkdirs();
        }
        destinationFile = new File(destinationPath + sourcePath.substring(sourcePath.lastIndexOf("/")));
        // skip copying if source file path is the same as the destination
        if (sourceFile.equals(destinationFile)) {
            return false;
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            fileOutputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.flush();
        } catch (IOException e) {
            Log.d(TAG, "IOException while copying file.Reason:" + e.getMessage());
           Log.e(TAG,"Exception :" +e.getMessage());
        } finally {
            try { if (fileInputStream != null) fileInputStream.close(); } catch(IOException e) {}
            try { if (fileOutputStream != null) fileOutputStream.close(); } catch(IOException e) {}

        }
        File[] files = destinationFile.listFiles();
        return files != null && files.length > 0;
    }

    public static boolean isDirectoryExist(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return true;
        }
        return false;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return deleteFileRecursively(file);
    }

    private static boolean deleteFileRecursively(File file) {
        if (!file.isDirectory()) {
            return file.delete();
        }
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                deleteFileRecursively(files[i]);
            }
        }
        return file.delete();
    }

    public static boolean isPreviewChannelApp(Context c, int appId){
        if(c == null) return false;
        String packageName = getPackageName(c, appId);
        return isPreviewChannelApp(c, packageName);
    }

    public static String getPackageName(Context context, int appId){
        String packageName = null;
        Cursor c=null;
        try {
            HtvAppQuery query = new HtvAppQuery(appId);
            c = executeQuery(context, query);
            if (c != null && c.moveToFirst()) {
                packageName = c.getString(c.getColumnIndex(HtvContract.HtvAppList.COLUMN_NAME));
            }
        }finally {
            if(c != null) c.close();
        }
        DdbLogUtility.logRecommendationChapter("DashboardDataManager", "getPackageName appId "+ appId + "  " + packageName);
        return packageName;
    }

    /**
     * Below function will return true assuming if TargetSDKVersion of selected app is >= 26
     *                                and false if TargetSDKVersion of selected app is < 26
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isPreviewChannelApp(Context context, String packageName){
        return  isTargetVersionO(context, packageName);
    }

    public static boolean isTargetVersionO(Context context, String packageName){
        boolean isTargetVersionO = false;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if(applicationInfo.targetSdkVersion >= Constants.SDK_TARGET_VERSION_O) {
                isTargetVersionO = true;
            }
            DdbLogUtility.logRecommendationChapter("DashboardDataManager", "isTargetVersionO " + isTargetVersionO + " targetSdk " + applicationInfo.targetSdkVersion);
        }catch (Exception e) {Log.e(TAG,"Exception :" +e.getMessage());}
        return isTargetVersionO;
    }

    private static Cursor executeQuery(Context context, Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null!");
        }
        Cursor cursor = context.getContentResolver().query(query.getUri(), query.getProjection(), query.getSelection(), query.getSelectionArgs(), query.getSortOrder());
        return cursor;
    }

    public static String getAppName(Context context, String packageName) {
        if(packageName == null ) return  null;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return applicationInfo.name;
        }catch (PackageManager.NameNotFoundException e) {Log.e(TAG,"Exception :"+e.getMessage());}
        return null;
    }
}

