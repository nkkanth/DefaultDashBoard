package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by sandeep.kumar on 08/12/2017.
 */

public class AvailableImagesQuery implements Query {

    private Uri mUri;
    private String mMountPath;

    private static final String[] PROJECTION = {MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.WIDTH};

    public AvailableImagesQuery(Uri uri, String mountPath) {
        mUri = uri;
        mMountPath = mountPath;
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getSelection() {
        return "("
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.jpeg'" + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.jpg'" + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.jps'" + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.mpo' " + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.bmp'" + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.dib'" + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.png'" + " OR "
                + MediaStore.Images.Media.DATA + " LIKE '" + mMountPath + "/%.gif'"
                + ") AND "
                + MediaStore.Images.Media.TITLE + " != " + "'cover'" + " AND "
                + MediaStore.Images.Media.TITLE + " != " + "'folder'";
    }

    @Override
    public String[] getSelectionArgs() {
        return null;
    }

    @Override
    public String getSortOrder() {
        return MediaStore.Images.Media.DATE_TAKEN + " DESC";
    }

    @Override
    public String getGroupBy() {
        return null;
    }
}
