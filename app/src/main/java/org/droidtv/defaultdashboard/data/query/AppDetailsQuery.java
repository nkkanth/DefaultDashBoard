package org.droidtv.defaultdashboard.data.query;

import android.net.Uri;

import org.droidtv.htv.provider.HtvContract;

public class AppDetailsQuery implements Query {

    private String [] PROJECTION =  { HtvContract.HtvAppList.COLUMN_NAME,  HtvContract.HtvAppList.COLUMN_CLASS_NAME};
    private String columnName = HtvContract.HtvAppList.COLUMN_NAME;
    private String className = HtvContract.HtvAppList.COLUMN_CLASS_NAME;

    private String mPackageName = null;
    private String selection = null;
    private String [] selectionArgs = null;

    public AppDetailsQuery(String packageName){
        mPackageName = packageName;
        selection = HtvContract.HtvAppList.COLUMN_NAME + " LIKE ?";

        selectionArgs = new String[1];
        selectionArgs[0] = "%".concat(mPackageName).concat("%");
    }

    @Override
    public Uri getUri() {
        return HtvContract.HtvAppList.CONTENT_URI;
    }

    @Override
    public String[] getProjection() {
        return PROJECTION;
    }

    @Override
    public String getSelection() {
        return selection;
    }

    @Override
    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    @Override
    public String getSortOrder() {
        return null;
    }

    @Override
    public String getGroupBy() {
        return null;
    }

    public String getColumnName(){
        return columnName;
    }

    public String getClassName(){
        return className;
    }
}
