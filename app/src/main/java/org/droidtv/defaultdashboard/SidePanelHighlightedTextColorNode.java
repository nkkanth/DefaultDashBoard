package org.droidtv.defaultdashboard;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.ui.optionsframework.MultiSliderNode;

/**
 * Created by nikhil.tk on 15-01-2018.
 */

public class SidePanelHighlightedTextColorNode extends MultiSliderNode {

    private int mCurrentColor;

    public SidePanelHighlightedTextColorNode(Context context) {
        this(context, null);
    }

    public SidePanelHighlightedTextColorNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SidePanelHighlightedTextColorNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        mCurrentColor = DashboardDataManager.getInstance().getSidePanelHighlightedTextColor();
    }

    public void setCurrentColor(int currentColor) {
        mCurrentColor = currentColor;
    }

    public int getCurrentColor() {
        return mCurrentColor;
    }

    @Override
    protected int[] getSliderMaximumValues() {
        return getContext().getResources().getIntArray(R.array.color_max_array);
    }

    @Override
    protected int[] getSliderMinimumValues() {
        return getContext().getResources().getIntArray(R.array.color_min_array);
    }

    @Override
    protected int[] getSliderStepSizes() {
        return getContext().getResources().getIntArray(R.array.color_step_array);
    }

    @Override
    protected String[] getSliderLabels() {
        return getContext().getResources().getStringArray(R.array.color_label_array);
    }

    @Override
    protected int[] getSliderValues() {
        return getColorValue();
    }

    @Override
    public void setColorValue(Context context, int index, int value) {
        int color = mCurrentColor;
        int A = Color.alpha(color);
        int R = Color.red(color);
        int G = Color.green(color);
        int B = Color.blue(color);
        switch (index) {
            case 0:
                color = Color.argb(A, value, G, B); //Red
                break;
            case 1:
                color = Color.argb(A, R, value, B);//Green
                break;
            case 2:
                color = Color.argb(A, R, G, value); //Blue
                break;
            case 3:
                value = (value * 255) / 100;
                color = Color.argb(value, R, G, B); // Opacity
                break;
        }
        mCurrentColor = color;
        DashboardDataManager.getInstance().changeSidePanelHighlightedTextColor(color);
    }

    @Override
    public int[] getColorValue() {
        int alpha = Color.alpha(mCurrentColor);
        int opacity = (int) Math.rint((alpha * 100) / 255d);
        int red = Color.red(mCurrentColor);
        int green = Color.green(mCurrentColor);
        int blue = Color.blue(mCurrentColor);
        return new int[]{red, green, blue, opacity};
    }

    public void onDismissed() {
        DashboardDataManager.getInstance().saveSidePanelHighlightedTextColor(mCurrentColor);
    }

}
