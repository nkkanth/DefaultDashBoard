package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import org.droidtv.ui.tvwidget2k15.R;
import org.droidtv.ui.tvwidget2k15.Slider;
import org.droidtv.ui.tvwidget2k15.Slider.SliderValueChangeListener;

public class SliderOptionsNode extends OptionsNode implements SliderValueChangeListener {

	private static final String ATTRS_MIN_VALUE = "min";
	private static final String ATTRS_MAX_VALUE = "max";
	private static final String ATTRS_STEP_SIZE = "stepSize";
	private static final String ATTRS_LABEL = "label";
	
	private int mSliderMaxValue;
	private int mSliderMinValue;
	private int mSLiderStepSize;
	private int mSliderLabelResID = -1;
	
	private SliderOptionsNodeListener mListener;

	public SliderOptionsNode(Context context) {
		this(context, null, 0);
	}

	public SliderOptionsNode(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SliderOptionsNode(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		stripSliderAttrs(attrs);
	}
	
	public void setSliderOptionsNodeListener(SliderOptionsNodeListener listener) {
		mListener = listener;
	}

	private void stripSliderAttrs(AttributeSet attrs) {
		if(attrs != null) {
			mSliderMaxValue = attrs.getAttributeIntValue(null, ATTRS_MAX_VALUE, 1);
			mSliderMinValue = attrs.getAttributeIntValue(null, ATTRS_MIN_VALUE, 0);
			mSLiderStepSize = attrs.getAttributeIntValue(null, ATTRS_STEP_SIZE, 1);
			
			mSliderLabelResID = attrs.getAttributeResourceValue(null, ATTRS_LABEL, -1);
		}
	}

	@Override
	protected View loadOptionsNodeView() {
		mView = loadView();
		setupSliderView();
		
		return mView;
	}

    private RelativeLayout getDefaultSliderView(View view) {
		RelativeLayout sliderView = null;
		if(view != null) {
			sliderView = (RelativeLayout) view.findViewById(R.id.defaultSliderView);
		}
		return sliderView;
    }
	
	private void setupSliderView() {
		RelativeLayout sliderView = getDefaultSliderView(mView);
		if(sliderView != null) {
			sliderView.setGravity(Gravity.CENTER);
		}
		
		Slider slider = (Slider) mView.findViewById(R.id.slider1);

		if(slider != null) {
			slider.setLabel(getLabel());
			if(isControllable()) {
				slider.setEnabled(true);
			}else
			{
				slider.setEnabled(false);
			}
		        int lCurrentVal = mListener == null?0: mListener.getSliderCurrentValue(getId());
			slider.setSliderValue(mSliderMinValue,mSliderMaxValue,lCurrentVal,mSLiderStepSize);
			slider.setOnValueChangeListener(this);
		}
	}

	private String getLabel() {
		return mSliderLabelResID == -1? 
				(mListener == null?"":mListener.getSliderLabel(getId()))
				:getContext().getResources().getString(mSliderLabelResID);
	}

	@Override
	protected RelativeLayout loadView() {
		return getOptionsManager().getFreeSliderViewFromCache();
	}
	
    public abstract static class SliderOptionsNodeListener {
    	
    	/**
    	 * Callback given to application when Slider's value is changed by user
    	 * @param nodeID
    	 * @param minValue
    	 * @param currentValue
    	 * @param maxValue
    	 */
    	public abstract void onSliderValueChanged(int nodeID, int minValue, int currentValue, int maxValue);
    	
    	/**
    	 * Called by FW to set Slider's value when it is displayed at first
    	 * @param nodeID
    	 * @return sliderStoredValue
    	 */
    	public abstract int getSliderCurrentValue(int nodeID);
    	
    	/**
    	 * Called by FW if slider's label is not mentioned in the XML
    	 * @param nodeID
    	 * @return sliderLabel
    	 */
    	public abstract String getSliderLabel(int nodeID);
    }

	@Override
	public void onSliderValueChanged(View s, int minValue, int currentValue,
			int maxValue) {
		if(mListener != null) {
			mListener.onSliderValueChanged(getId(), minValue, currentValue, maxValue);
		}
	}

}
