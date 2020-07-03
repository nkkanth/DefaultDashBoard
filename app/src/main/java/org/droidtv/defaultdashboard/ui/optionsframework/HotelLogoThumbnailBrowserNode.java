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

public class HotelLogoThumbnailBrowserNode extends ThumbnailBrowserNode {


    public HotelLogoThumbnailBrowserNode(Context context) {
        this(context, null);
    }

    public HotelLogoThumbnailBrowserNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HotelLogoThumbnailBrowserNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getSavedImagePath() {
        return getContext().getFilesDir() + Constants.PATH_HOTEL_LOGO;
    }

    @Override
    protected int getDefaultImageDrawableResource() {
        return R.drawable.default_hotel_logo;
    }

    @Override
    protected int getDefaultImageWidth() {
        return Constants.HOTEL_LOGO_WIDTH;
    }

    @Override
    protected int getDefaultImageHeight() {
        return Constants.HOTEL_LOGO_HEIGHT;
    }

    @Override
    public void onThumbnailClick(String imageFilePath) {
        DdbLogUtility.logCommon("HotelLogoThumbnailBrowserNode", "onThumbnailClick() called with: imageFilePath = [" + imageFilePath + "]");
        DashboardDataManager.getInstance().setHotelLogo(imageFilePath);
        super.onThumbnailClick(imageFilePath);
    }

    @Override
    public void onThumbnailClick(int drawableResourceId) {
        DdbLogUtility.logCommon("HotelLogoThumbnailBrowserNode", "onThumbnailClick() called with: drawableResourceId = [" + drawableResourceId + "]");
        DashboardDataManager.getInstance().setHotelLogo(drawableResourceId);
        super.onThumbnailClick(null);
    }
}
