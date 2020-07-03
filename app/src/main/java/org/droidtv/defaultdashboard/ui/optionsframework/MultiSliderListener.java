package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;

/**
 * Created by nikhil.tk on 10-01-2018.
 */

public interface MultiSliderListener {

    void setColorValue(Context context, int index, int value);

    int[] getColorValue();
}