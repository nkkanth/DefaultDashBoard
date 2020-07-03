package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.droidtv.defaultdashboard.R;
import org.droidtv.ui.tvwidget2k15.Slider;
import org.droidtv.ui.tvwidget2k15.Slider.SliderValueChangeListener;

public class MultiSliderView extends LinearLayout implements SliderValueChangeListener {

    public static final int MAX_NUM_OF_SLIDERS = 4;
    private MultiSliderListener mMultiSliderListener;

    public MultiSliderView(Context context) {
        this(context, null, 0);
    }

    public MultiSliderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiSliderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.msliderlayout, this);
    }

    public void setData(String[] sliderLabel, int[] maxval, int[] minval, int[] sliderstep, int lNumOfSliders, int[] values) {
        Slider[] mSliders = new Slider[lNumOfSliders];
        int i;
        for (i = 0; i < lNumOfSliders; i++) {
            mSliders[i] = (Slider) this.getChildAt(i);
            Slider lSlider1 = mSliders[i];
            lSlider1.setLabel(sliderLabel[i]);
            lSlider1.setSliderValue(minval[i], maxval[i], values[i], sliderstep[i]);
            lSlider1.setId(i);
            lSlider1.setOnValueChangeListener(this);
        }
        for (int j = i; j < MAX_NUM_OF_SLIDERS; j++) {
            Slider lSlider = (Slider) this.getChildAt(j);
            lSlider.setVisibility(View.GONE);
        }
    }

    public void setMultiSliderListener(MultiSliderListener multisliderListner) {
        mMultiSliderListener = multisliderListner;
    }

    @Override
    public void onSliderValueChanged(View view, int minValue, int currentValue, int maxValue) {
        if (mMultiSliderListener != null) {
            mMultiSliderListener.setColorValue(view.getContext(), view.getId(), currentValue);
        }
    }
}
