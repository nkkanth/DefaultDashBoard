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

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.ui.tvwidget2k15.R;
import org.droidtv.ui.tvwidget2k15.SingleMultipleChoiceActivatedTextView;

import java.util.List;

/*
 * This Adapter class is used by both settingsframework and optionsframework
 */
public class MultiChoiceListAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> mData;
	private List<Boolean> mControllabilityData;
	private List<String> mGData=null;
	private List<Drawable> mDataIcons;

	public MultiChoiceListAdapter(Context context, List<String> data,
			List<Boolean> controllabilityData, List<String> mGuidencedata, List<Drawable> dataIcons) {
		mContext = context;
		mData = data;
		mControllabilityData = controllabilityData;
		mDataIcons = dataIcons;
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
		DdbLogUtility.logCommon("MultiChoiceListAdapter", "getView() called with: position = " + position );
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.options_menu_checkbox_text_view, null);
			
			SingleMultipleChoiceActivatedTextView text = (SingleMultipleChoiceActivatedTextView)convertView.findViewById(R.id.text1);
			
			text.setText(mData.get(position));//SingleMultipleChoiceActivatedTextView
			if(!mDataIcons.isEmpty()) {
				text.setCompoundDrawablesWithIntrinsicBounds(mDataIcons.get(position), null, null, null);
			}
			if(!mControllabilityData.get(position)) 
			{
				text.setControllable(false);
			}
			//added by Eaccessibility
			if(getGuidanceData()!=null) {
				text.setContentDescription(mGData.get(position));
			}

			return convertView;
		}
		else {
			SingleMultipleChoiceActivatedTextView text = (SingleMultipleChoiceActivatedTextView)convertView.findViewById(R.id.text1);
			String lTextViewStr = (String)text.getText();
				
			String lActualStr = (String)mData.get(position);
			if(lTextViewStr.compareTo(lActualStr) != 0) {
			    text.setText(lActualStr); //SingleMultipleChoiceActivatedTextView
			    if(!mDataIcons.isEmpty()) {
				text.setCompoundDrawablesWithIntrinsicBounds(mDataIcons.get(position), null, null, null);
			    }
					
			    if(!mControllabilityData.get(position)) 
				{
				    text.setControllable(false);
				}
					
			}
			if(getGuidanceData()!=null) {
				text.setContentDescription(mGData.get(position));
			}
			return convertView;
		}
	}
	
	public List<String> getGuidanceData() {
		return mGData;
	}

	public void setGuidanceData(List<String> mGData) {
		this.mGData = mGData;
	}

}
