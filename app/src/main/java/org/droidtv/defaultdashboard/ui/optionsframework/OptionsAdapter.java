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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.droidtv.defaultdashboard.ui.optionsframework.OptionsNode.OptionsNodeListener;
import org.droidtv.ui.tvwidget2k15.ActivatedTextView;
import org.droidtv.ui.tvwidget2k15.R;

public class OptionsAdapter extends BaseAdapter implements OptionsNodeListener {

	private Context mContext;
	private OptionsNode mNode;

	public OptionsAdapter(Context context, OptionsNode node) {
		mContext = context;
		mNode = node;
		setupListenersForChildren();
	}
	
	/*
	 * in order to notify data set changed when any of the children's state changes.
	 */
	private void setupListenersForChildren() {
		int lChildCount = mNode.getChildCount();
		if(lChildCount != 0) {
			for(int position=0;position<lChildCount;position++) {
				OptionsNode lChild = (OptionsNode)mNode.getChildAt(position);
				lChild.setListener(this);
			}
		}
	}

	public int getCount() {
		return mNode.getAvailableChildCount();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ActivatedTextView textView = null;
		ImageView folderMark = null;
		
		if(convertView == null) {
			convertView = View.inflate(mContext, R.layout.options_menu_item, null);
		}

		textView = (ActivatedTextView) convertView.findViewById(R.id.text1);
		folderMark = (ImageView)convertView.findViewById(R.id.folder);

		if(textView != null && folderMark != null) {
		    OptionsNode lAvailableNode = mNode.getAvaialableChildAt(position);
		    textView.setText((String)lAvailableNode.getTag());
		    
		    textView.setCompoundDrawablesWithIntrinsicBounds(lAvailableNode.getIcon(), null, null, null);
		    
		    //added for Eaccessibility
		    textView.setContentDescription((String)lAvailableNode.getContentDescription());
		    if(lAvailableNode.getAvailableChildCount() > 0 || 
		    		(lAvailableNode.getAvailableChildCount() == 0 && !lAvailableNode.isActionable())) {
			    Drawable more ;
				more = !lAvailableNode.isControllable()?
						mContext.getResources().getDrawable(R.drawable.list_arrow_disabled, null):
							mContext.getResources().getDrawable(R.drawable.options_menu_arrow_normal, null);
				folderMark.setImageDrawable(more);
		    	folderMark.setVisibility(View.VISIBLE);
		    } else {
		    	folderMark.setVisibility(View.GONE);
		    }
		    
		    //added for issue AN-6921 and  AN 6576
		    folderMark.setEnabled(lAvailableNode.isControllable());
			textView.setControllable(lAvailableNode.isControllable());
			textView.setEnabled(lAvailableNode.isControllable());
			
		}

		return convertView;
	}

	public void onAvailabilityChanged(OptionsNode node, boolean available) {
		// if any of the children's state is changed, update parent's adapter
		notifyDataSetChanged();
	}

	public void onControllabilityChanged(OptionsNode node, boolean controllable) {
		// if any of the children's state is changed, update parent's adapter
		notifyDataSetChanged();			
	}

}
