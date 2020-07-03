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

public class SharingBackgroundThumbnailBrowserNode extends ThumbnailBrowserNode {

    public SharingBackgroundThumbnailBrowserNode(Context context) {
        this(context, null);
    }

    public SharingBackgroundThumbnailBrowserNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SharingBackgroundThumbnailBrowserNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getSavedImagePath() {
        return getContext().getFilesDir() + Constants.PATH_SHARING_BACKGROUND;
    }

    @Override
    protected int getDefaultImageDrawableResource() {
        if(DashboardDataManager.getInstance().isBFLProduct()) {
            return R.drawable.cast_chapter_background_image_bfl;
        }else{
            return R.drawable.cast_chapter_background_image;
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
        mHasConfigurationSessionChanges = true;
//        DashboardDataManager.getInstance().setBackground(imageFilePath);
        super.onThumbnailClick(imageFilePath);
    }

    @Override
    public void onThumbnailClick(int drawableResourceId) {
        mHasConfigurationSessionChanges = true;
        DashboardDataManager.getInstance().loadCastBackground(drawableResourceId);
        super.onThumbnailClick(null);
    }

    @Override
    public void onDismissed() {
        super.onDismissed();
        DdbLogUtility.logCommon("SharingBackgroundThumbnailBrowserNode", "onDismissed: " +hasConfigurationSessionChanges() +  " getCurrentAppliedImagePath " + getCurrentAppliedImagePath()) ;
        if (hasConfigurationSessionChanges()) {
            //Copy saved image to public folder for cast app to access.
            String appliedPath = getCurrentAppliedImagePath();
            if(appliedPath == null) { //delete the saved image if current path is null, meanign default background is applied.
                deleteSavedImageFile();
            }else {//delete and copy the applied cast image file.
                DashboardDataManager.getInstance().deleteAndCopyImageToCastAppDirectory(getCurrentAppliedImagePath());
                resetConfigurationSessionChanges();
            }
        }
    }

    private void deleteSavedImageFile(){
        DashboardDataManager.getInstance().removeSavedImage(Constants.PATH_CAST_APP_SHARING_BACKGROUND, new DashboardDataManager.FileDataListener() {
            @Override
            public void onFileCopyComplete(boolean success) { }
            @Override
            public void onFileDeleteComplete(boolean success) {
            }
        });
    }
}
