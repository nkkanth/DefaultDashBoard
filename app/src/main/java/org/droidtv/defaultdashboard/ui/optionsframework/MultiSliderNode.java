package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import org.droidtv.defaultdashboard.R;

/**
 * Created by nikhil.tk on 09-01-2018.
 */

public abstract class MultiSliderNode extends OptionsNode implements MultiSliderListener {

    private String[] mLabelArray;
    private int[] mMaxVals;
    private int[] mMinVals;
    private int[] mStepSize;
    private int[] mLoadValues;

    public MultiSliderNode(Context context) {
        this(context, null);
    }

    public MultiSliderNode(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiSliderNode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        mMaxVals = getSliderMaximumValues();
        mMinVals = getSliderMinimumValues();
        mStepSize = getSliderStepSizes();
        mLabelArray = getSliderLabels();
        mLoadValues = getSliderValues();
    }

    protected abstract int[] getSliderMaximumValues();

    protected abstract int[] getSliderMinimumValues();

    protected abstract int[] getSliderStepSizes();

    protected abstract String[] getSliderLabels();

    protected abstract int[] getSliderValues();

    @Override
    protected RelativeLayout loadView() {
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.multislider, null);
        MultiSliderView mMultiSliderView = (MultiSliderView) root.findViewById(R.id.msliderview);
        mMultiSliderView.setMultiSliderListener(this);
        mLoadValues = getSliderValues();
        mMultiSliderView.setData(mLabelArray, mMaxVals, mMinVals, mStepSize, mLabelArray.length, mLoadValues);
        return root;
    }
}