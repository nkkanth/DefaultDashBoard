package org.droidtv.defaultdashboard.oem;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.util.Constants;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class OemHelper extends ContextualObject {
    private static final String TAG = "OemHelper";

    public OemHelper(Context context){
        super(context);
        registerOemReceiver();
    }

    private void registerOemReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.OEM_START_INTENT_NOTIFICATION);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mOemStartNotificationReceiver,intentFilter);
    }

    private BroadcastReceiver mOemStartNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Constants.OEM_START_INTENT_NOTIFICATION)){
                notifyOemStart();
            }
        }
    };

    private void notifyOemStart(){
        String packageName = getContext().getPackageName();
        Bitmap adBitmap = drawableToBitmap(getDrawable(packageName));
        if(adBitmap != null){
            Intent intent = new Intent();
            int color = getColorFromPackage();
            PendingIntent pendIntent = createPendingIntent(1);
            Bundle extras = new Bundle();
            intent.setAction(Constants.OEM_SHELF_NOTIFICATION);
            extras.putString(Constants.NOTIFICATION_TITLE, "DefaultDashboard");
            StringBuilder strb = new StringBuilder();
            extras.putString(Constants.NOTIFICATION_SORT, (strb.append(1)).toString());
            extras.putInt(Constants.NOTIFICATION_COLOR, color);
            extras.putParcelable(Constants.NOTIFICATION_PENDING_INTENT, pendIntent);
            extras.putParcelable(Constants.NOTIFICATION_BITMAP, adBitmap);
            extras.putString(Constants.NOTIFICATION_REPLACE_PACKAGE, getContext().getPackageName());
            extras.putInt(Constants.NOTIFICATION_ID, 1);

            intent.putExtras(extras);
            getContext().sendBroadcast(intent, Constants.UPDATE_ADVERT_NOTIFICATION);
        }else{
            Log.e(TAG, "Could not fetch bitmap");
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap = null;
        if(drawable != null) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }
    private PendingIntent createPendingIntent(int pos){
        PendingIntent pendingIntent;
        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(getContext().getPackageName()), getContext(), OemOnClickHandling.class);
        pendingIntent = PendingIntent.getService(getContext(), pos, intent, 0);
        return pendingIntent;
    }

    private int getColorFromPackage() {
        String packageName = getContext().getPackageName();
        int DEFAULT_COLOR = Constants.DEFAULT_PACKAGE_COLOR;//Red=11,Green=94,Blue=215;
        ResolveInfo resolveInfo = resolvePackage(packageName);
        if( resolveInfo == null) { return DEFAULT_COLOR; }

        try {
            Context remoteCtx = getContext().createPackageContext( packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            Resources.Theme remoteTheme = remoteCtx.getTheme();           
            remoteTheme.applyStyle( resolveInfo.activityInfo.getThemeResource(), true );
            int [] theme_id_list = { android.R.attr.colorPrimary };
            TypedArray ta = remoteTheme.obtainStyledAttributes( theme_id_list );
            int color = ta.getColor(0, DEFAULT_COLOR);
            ta.recycle();

            return color;
        }
        catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            Log.e(TAG, "package name not found: " +localNameNotFoundException.getMessage() );
        }
        return DEFAULT_COLOR;
    }

    private ResolveInfo resolvePackage(String packageName ) {
        Intent intent = new Intent();
        intent.setPackage( packageName );
        intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        ResolveInfo resolveInfo = getContext().getPackageManager().resolveActivity(intent, 0);
        return resolveInfo;
    }

    private Drawable getDrawable(String packageName){
        Drawable appLogo = null;
        try {
            PackageManager packageManager = getContext().getPackageManager();
            appLogo = packageManager.getApplicationBanner(packageName);
            if (appLogo == null) {
                appLogo = packageManager.getApplicationLogo(packageName);
            }
            if (appLogo == null) {
                appLogo = packageManager.getApplicationIcon(packageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Error while fetching logo for package:" + packageName + ".Reason:" + e.getMessage());
        }
        return appLogo;
    }


}
