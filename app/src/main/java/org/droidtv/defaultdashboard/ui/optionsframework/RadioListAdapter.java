/*
*  Copyright(C) 2012 TP Vision Holding B.V.,
*  All Rights Reserved.
*  This  source code and any compilation or derivative thereof is the
*  proprietary information of TP Vision Holding B.V.
*  and is confidential in nature.
*  Under no circumstances is this software to be exposed to or placed
*  under an Open Source License of any type without the expressed
*  written permission of TP Vision Holding B.V.
*
*/
package org.droidtv.defaultdashboard.ui.optionsframework;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.droidtv.ui.tvwidget2k15.R;
import org.droidtv.ui.tvwidget2k15.SingleMultipleChoiceActivatedTextView;

import java.util.List;


/*
 * This Adapter class is used by both settingsframework and optionsframework
 */
public class RadioListAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> mData;
	private List<Boolean> mControllabilityData;
	private List<String> mGData = null;
	private List<Drawable> mDataIcons;
	
	public RadioListAdapter(Context context, List<String> data,
			List<Boolean> controllabilityData, List<String> mGuidencedata, List<Drawable> dataIcons) {
		mContext = context;
		mData = data;
		mControllabilityData = controllabilityData;
		mDataIcons = dataIcons;
		//added for Eaccessibility
		if(mGuidencedata!=null) {
		        mGData = mGuidencedata;
		}
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.options_menu_radiobutton_text_view, parent,false);
			
			SingleMultipleChoiceActivatedTextView text = (SingleMultipleChoiceActivatedTextView)convertView.findViewById(R.id.text1);
	
			(text).setText(mData.get(position));
			if(!mDataIcons.isEmpty()) {
				(text).setCompoundDrawablesWithIntrinsicBounds(mDataIcons.get(position), null, null, null);
			}
			
			//added for Eaccessibility
			if(getGuidanceData()!=null) {
				text.setContentDescription(mGData.get(position));
			}
			
			if(!mControllabilityData.get(position)) 
			{
				(text).setControllable(false);
			}else
			{
				(text).setControllable(true);
			}
			return convertView;
		}
		else {
			SingleMultipleChoiceActivatedTextView text = (SingleMultipleChoiceActivatedTextView)convertView.findViewById(R.id.text1);
			String lTextViewStr = (String)text.getText();
	
			String lActualStr = (String)mData.get(position);
			if(lTextViewStr.compareTo(lActualStr) != 0) {
			    text.setText(lActualStr);// type casted to SingleMultipleChoiceActivatedTextView
			    if(!mDataIcons.isEmpty()) {
				text.setCompoundDrawablesWithIntrinsicBounds(mDataIcons.get(position), null, null, null);
			    }
					
			    if(!mControllabilityData.get(position)) 
				{
				    text.setControllable(false);
				}else
				{
				    text.setControllable(true);
				}

			}
			//added for Eaccessibility
			if(getGuidanceData()!=null) {
				text.setContentDescription(mGData.get(position));
			}
			return convertView;
		}
	}
	public List<String> getGuidanceData() {
		return mGData ;
	}

	public void setGuidanceData(List<String> mGData) {
		this.mGData = mGData;
	}


}
