package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.util.AttributeSet;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.Constants;

/**
 * Created by sandeep.kumar on 18/01/2018.
 */

public class MainBackgroundThumbnailBrowserNode extends ThumbnailBrowserNode {


    public MainBackgroundThumbnailBrowserNode(Context context) {
        this(context, null);
    }

    public MainBackgroundThumbnailBrowserNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainBackgroundThumbnailBrowserNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getSavedImagePath() {
        return getContext().getFilesDir() + Constants.PATH_MAIN_BACKGROUND;
    }

    @Override
    protected int getDefaultImageDrawableResource() {
        if(DashboardDataManager.getInstance().isBFLProduct()) {
            return R.drawable.default_main_background_bfl;
        }else{
            return R.drawable.default_main_background;
        }
    }

    @Override
    protected int getDefaultImageWidth() {
        return Constants.MAIN_BACKGROUND_WIDTH;
    }

    @Override
    protected int getDefaultImageHeight() {
        return Constants.MAIN_BACKGROUND_HEIGHT;
    }

    @Override
    public void onThumbnailClick(String imageFilePath) {
        DdbLogUtility.logCommon("MainBackgroundThumbnailBrowserNode", "onThumbnailClick() called with: imageFilePath = [" + imageFilePath + "]");
        DashboardDataManager.getInstance().setBackground(imageFilePath);
        super.onThumbnailClick(imageFilePath);
    }

    @Override
    public void onThumbnailClick(int drawableResourceId) {
        DdbLogUtility.logCommon("MainBackgroundThumbnailBrowserNode", "onThumbnailClick() called with: drawableResourceId = [" + drawableResourceId + "]");
        DashboardDataManager.getInstance().setBackground(drawableResourceId);
        super.onThumbnailClick(null);
    }
}
