package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;

/**
 * Created by sandeep.kumar on 15/01/2018.
 */

public class BackgroundImageColorFilterNode extends MultiSliderNode {

    private int mCurrentColor;

    public BackgroundImageColorFilterNode(Context context) {
        this(context, null);
    }

    public BackgroundImageColorFilterNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackgroundImageColorFilterNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        mCurrentColor = DashboardDataManager.getInstance().getMainBackgroundColorFilter();
    }

    public void setCurrentColor(int currentColor) {
        mCurrentColor = currentColor;
    }

    public int getCurrentColor() {
        return mCurrentColor;
    }

    @Override
    protected int[] getSliderMaximumValues() {
        return getContext().getResources().getIntArray(R.array.main_background_color_max_array);
    }

    @Override
    protected int[] getSliderMinimumValues() {
        return getContext().getResources().getIntArray(R.array.main_background_color_min_array);
    }

    @Override
    protected int[] getSliderStepSizes() {
        return getContext().getResources().getIntArray(R.array.main_background_color_step_array);
    }

    @Override
    protected String[] getSliderLabels() {
        return getContext().getResources().getStringArray(R.array.main_background_color_label_array);
    }

    @Override
    protected int[] getSliderValues() {
        return getColorValue();
    }

    public void onDismissed() {
        saveCurrentColorFilter();
    }

    // int[] {Red,Green,Blue,Opacity}
    @Override
    public void setColorValue(Context context, int index, int value) {
        int color = mCurrentColor;

        int alpha = Color.alpha(color);
        int opacity = convertAlphaToOpacity(alpha);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        switch (index) {
            case 0:
                color = Color.argb(alpha, value, green, blue);
                break;
            case 1:
                color = Color.argb(alpha, red, value, blue);
                break;
            case 2:
                color = Color.argb(alpha, red, green, value);
                break;
            case 3:
                color = Color.argb(convertOpacityToAlpha(value), red, green, blue);
                break;
        }

        mCurrentColor = color;
        DdbLogUtility.logCommon("BackgroundImageColorFilterNode", "mCurrentColor " + mCurrentColor);
        DashboardDataManager.getInstance().setBackgroundColorFilter(color);

    }

    // int[] {Red,Green,Blue,Opacity}
    @Override
    public int[] getColorValue() {
        int alpha = Color.alpha(mCurrentColor);
        int opacity = convertAlphaToOpacity(alpha);
        int red = Color.red(mCurrentColor);
        int green = Color.green(mCurrentColor);
        int blue = Color.blue(mCurrentColor);

        return new int[]{red, green, blue, opacity};
    }

    private void saveCurrentColorFilter() {
        DashboardDataManager.getInstance().saveMainBackgroundColorFilter(mCurrentColor);
    }

    private static int convertAlphaToOpacity(int alpha) {
        return (int) ((alpha / 255f) * 100);
    }

    private static int convertOpacityToAlpha(int opacity) {
        return (opacity * 255) / 100;
    }
}
