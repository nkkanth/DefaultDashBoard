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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.ui.tvwidget2k15.R;
import org.droidtv.ui.tvwidget2k15.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class ListOptionsNode extends OptionsNode implements OnItemClickListener, 
	OnItemLongClickListener, OnItemSelectedListener {
	
	private static final String TAG = ListOptionsNode.class.getSimpleName();
	
	private static final int TYPE_GENERIC_LIST = 0;
	private static final int TYPE_RADIO_LIST = 1;
	private static final int TYPE_MULTI_CHOICE_LIST = 2;
	
	private static final String ATTR_DATA = "data";
	private static final String ATTR_DATA_ID = "data_id";	
	private static final String ATTR_GUIDE_DATA = "guidedata";	
	private static final String ATTR_LIST_TYPE = "list_type";
	private static final String ATTR_DYNAMIC_DATA = "dynamic";
	private static final String ATTR_ICON_LIST = "iconList";
	
	private ListOptionsNodeListener mListListener;
	private ListOptionsNodeFocusChangeListener mListFocusListener;
	private DynamicListOptionsNodeCallback mDynamicListCallback;
	
	private String[] mData;
	private int[] mDataID;
	private int mListType;
	private boolean mDynamic;
	private Drawable[] mIconList = null;
	
	//added for E-Accessibility
	private String[] mGData=null;
	private List<String>  mGuidencedata=null;
	private static final String PREFIX_GUIDANCE_TEXT="prefix_";
	
	protected ArrayList<String> mAvailableData = new ArrayList<String>();
	protected ArrayList<Integer> mAvailableDataID =  new ArrayList<Integer>();
	protected ArrayList<Boolean> mAvailableDataControllability = new ArrayList<Boolean>();
	protected ArrayList<Drawable> mAvailableDataIcons = new ArrayList<Drawable>();

	public ListOptionsNode(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mListType = TYPE_GENERIC_LIST;
		stripAttrs(attrs);
	}
	
	private void stripAttrs(AttributeSet attrs) {
		if(attrs != null) {
			int lDataResId = attrs.getAttributeResourceValue(null, ATTR_DATA, -1);
			int lDataIDResId = attrs.getAttributeResourceValue(null, ATTR_DATA_ID, -1);
						
			if(lDataResId != -1 && lDataIDResId != -1) {
				mData = getContext().getResources().getStringArray(lDataResId);
				mDataID = getContext().getResources().getIntArray(lDataIDResId);
			}
			
			String dynamic = attrs.getAttributeValue(null, ATTR_DYNAMIC_DATA);		
			if(dynamic!=null && dynamic.equalsIgnoreCase("true")) {
				mDynamic = true;
			} else {
				mDynamic = false;
			}			
			
			// added for Eaccessibility	
			int lGDataResId = attrs.getAttributeResourceValue(null, ATTR_GUIDE_DATA, -1);
			if(lGDataResId!=-1) {
			        mGData = getContext().getResources().getStringArray(lGDataResId);
			}
			if(mGData!=null) {
				mGuidencedata = new ArrayList<String>();
		        }
			
			
			String lt = attrs.getAttributeValue(null, ATTR_LIST_TYPE);
			if(lt != null) {
				if(TextUtils.equals(lt, "generic")) {
					mListType = TYPE_GENERIC_LIST;
				}
				else if(TextUtils.equals(lt, "radio")) {
					mListType = TYPE_RADIO_LIST;
				}
				else if(TextUtils.equals(lt, "multichoice")) {
					mListType = TYPE_MULTI_CHOICE_LIST;
				}
			}
			
			int iconsArrayResId = attrs.getAttributeResourceValue(null, ATTR_ICON_LIST, -1);
			if(iconsArrayResId != -1) {
				TypedArray ta = getContext().getResources().obtainTypedArray(iconsArrayResId);
				if(ta != null) {
					Drawable[] icons = new Drawable[ta.length()];
					for (int i = 0; i < ta.length(); i++) {
						int iconResID = ta.getResourceId(i, -1);
						icons[i] = iconResID != -1 ? getContext().getResources().getDrawable(iconResID,null) : null;
					}
					this.mIconList = icons;
					ta.recycle();
				}				
			}
		}
	}

	/**
	 *Methods sets the guidance text for the leaf node items.
	 * @param lGDataResId String array containing the guidance text
	 * @return
	 */
	public String[] setGuidenceText(int lGDataResId) {	
		return getContext().getResources().getStringArray(lGDataResId);
	}

	public ListOptionsNode(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ListOptionsNode(Context context) {
		this(context,null,0);
	}

	public Drawable[] getIconList() {
		return mIconList;
	}

	public void setIconList(Drawable[] iconList) {
	        if(iconList != null) {
		    this.mIconList = iconList.clone();
	        }
	}

	/**
	 * set listener for the listView associated with the node
	 * @param listListener
	 */
	public void setListListener(ListOptionsNodeListener listListener) {
		mListListener = listListener;
	}
	
	public void setListFocusListener(ListOptionsNodeFocusChangeListener focusListener) {
		mListFocusListener = focusListener;
	}
	
	public void setDynamicListCallback(DynamicListOptionsNodeCallback dynamicCallback) {
		mDynamicListCallback = dynamicCallback;
	}

	void refreshUI() {
		getDataFromApplication();
		if(mAvailableData.isEmpty()) {
			getOptionsManager().scrollToPrevPage();
		} else {
			setupListView();
		}
	}
	
	/*
	 * get info from application about which list items are available and controllable
	 */
	protected void getDataFromApplication() {
		mAvailableData.clear();
		mAvailableDataID.clear();
		mAvailableDataControllability.clear();
		mAvailableDataIcons.clear();
		
		//Added for Eaccesibility
		if(mGuidencedata!=null && mListListener!=null){
			mGuidencedata.clear();
			for(String mdata: mGData)
			{
				 mGuidencedata.add(PREFIX_GUIDANCE_TEXT+mdata);
			}
			
		}
		
		if(mDynamic) {
			// if dynamic node, pull data from app all the time
			if(mDynamicListCallback != null) {
				String[] data = mDynamicListCallback.getListDynamicData(getId());
				int[] dataID = mDynamicListCallback.getListDynamicDataID(getId());
				Drawable[] dataIcons = mDynamicListCallback.getListDynamicIcons(getId());
				
				boolean isDataIDSpecified = false;
				boolean isDataIconsSpecified = false;
				
				if(dataID != null) {
					for(int i=0;i<dataID.length;i++) {
						mAvailableDataID.add(dataID[i]);
					}
					isDataIDSpecified = true;
				}

				if(dataIcons != null) {
					for(int i=0;i<dataIcons.length;i++) {
						mAvailableDataIcons.add(dataIcons[i]);
					}
					isDataIconsSpecified = true;
				}

				if(data != null) {
					for(int i=0;i<data.length;i++) {
						if(!isDataIDSpecified) {
							mAvailableDataID.add(i);
						}
						if(!isDataIconsSpecified) {
							mAvailableDataIcons.add(null);
						}
						boolean controllable = mListListener.isListItemControllable(getId(), mAvailableDataID.get(i));
						mAvailableData.add(data[i]);
						mAvailableDataControllability.add(controllable);
					}
				}
				
			}
		} else {
			// if non-dynamic LON
			if(mListListener != null) {
				for(int i=0;i<mData.length;i++) {
					if(mListListener.isListItemAvailable(getId(), mDataID[i])) {
						boolean controllable = mListListener.isListItemControllable(getId(), mDataID[i]);
						mAvailableData.add(mData[i]);
						mAvailableDataID.add(mDataID[i]);
						if(mIconList != null) {
							mAvailableDataIcons.add(mIconList[i]);
						}
						mAvailableDataControllability.add(controllable);
					}
				}
			}
		}
	}
	
	/*
	 * setup listview
	 * 1. set adapter
	 * 2. set choice mode
	 * 3. set selector
	 */
	private void setupListView() {
		ListView listView = (ListView) mView.findViewById(R.id.defaultListView);
		BaseAdapter lAdapter = null;
		
		switch(mListType) {
			case TYPE_RADIO_LIST: lAdapter = new RadioListAdapter(getContext(), mAvailableData,
										mAvailableDataControllability,mGuidencedata, mAvailableDataIcons);
									listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
									break;
			case TYPE_MULTI_CHOICE_LIST: lAdapter = new MultiChoiceListAdapter(getContext(), mAvailableData,
										mAvailableDataControllability,mGuidencedata, mAvailableDataIcons);
									listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
									break;
		        case TYPE_GENERIC_LIST:        
		        default: lAdapter = new GenericListAdapter(getContext(), mAvailableData,
										mAvailableDataControllability,mGuidencedata, mAvailableDataIcons);
									listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
									break;

		}
		listView.setAdapter(lAdapter);
		if (mListType != TYPE_MULTI_CHOICE_LIST) {
			int selectedPosition = 0;
			if (mListListener != null) {
				int dataID;
				dataID = mListListener.getItemSelectionWhenFocused(getId());
				selectedPosition = mAvailableDataID.indexOf(dataID);
			}
			if (selectedPosition >= 0 && selectedPosition < listView.getCount()) {
				listView.setSelection(selectedPosition);
				listView.setItemChecked(selectedPosition, true);
			}
		} else {
			if (mListListener != null
					&& (mListListener instanceof MultiChoiceOptionsNodeListener)) {
				int []dataID;
				dataID = ((MultiChoiceOptionsNodeListener) mListListener)
						.getMultiItemsSelectionWhenFocused(getId());
				if (dataID != null){
					listView.setSelection(mAvailableData.indexOf(String.valueOf(dataID[0])));
					for(int i = 0; i < dataID.length; i++) {
						listView.setItemChecked(mAvailableDataID.indexOf(dataID[i]), true);
					}
				}
			}
			if(listView.getSelectedItemPosition() == ListView.INVALID_POSITION){
				LogHelper.e(TAG, "Invalid Selection passed");
				listView.setSelection(0);
			}
		}
	}
	
	/**
	 * set item checked
	 * @param data_id of the item to be checked
	 * @param value - new checked state value for the item
	 */
	public void setItemChecked(int dataID, boolean value) {
		if(null == mView){
			return;
		}
		ListView listView = (ListView) mView.findViewById(R.id.defaultListView);
		int position = mAvailableDataID.indexOf(dataID);
		if(position != ListView.INVALID_POSITION) {
			listView.setItemChecked(position, value);
			if(mListListener != null) {
				mListListener.onListItemClick(getId(), (int)mAvailableDataID.get(position), (AdapterView<?>)listView, 
						(View)listView.getItemAtPosition(position), position, (long)position);
			}
		} else {
			DdbLogUtility.logCommon(TAG, "The data_id passed doesn't have a match in the data_id array");
		}
	}
	
	/**
	 * set selection to the item specified by id
	 * @param data_id
	 */
	public void setSelection(int dataID) {
		if(null == mView){
			return;
		}
		ListView listView = (ListView) mView.findViewById(R.id.defaultListView);
		int position = mAvailableDataID.indexOf(dataID);
		if(position != ListView.INVALID_POSITION) {
			listView.setSelection(position);
			if(mListListener != null) {
				mListListener.onListItemSelected(getId(), (int)mAvailableDataID.get(position), (AdapterView<?>)listView, 
						(View)listView.getItemAtPosition(position), position, (long)position);
			}
		}
	}
	
	/**
	 * set selection to the checked item
	 */
	public void setSelectionOnCheckedItem() {
		if(null == mView){
			return;
		}
		ListView listView = (ListView) mView.findViewById(R.id.defaultListView);
		int position = 0;
		if (mListType != TYPE_MULTI_CHOICE_LIST) {
			position = listView.getCheckedItemPosition();
		} else {
			position = listView.getCheckedItemPositions().keyAt(0);
		}
		listView.setSelection(position);
		if(position != ListView.INVALID_POSITION && mAvailableDataID.get(position) != ListView.INVALID_POSITION){
			if(mListListener != null) {
				mListListener.onListItemSelected(getId(), (int)mAvailableDataID.get(position), (AdapterView<?>)listView, 
						(View)listView.getItemAtPosition(position), position, (long)position);
			}
		}
	}

	protected View loadOptionsNodeView() {
		mView = loadView();
		
		if((mData != null && mDataID != null) || mDynamic) {
			getDataFromApplication();
			setupListView();
		}
		return mView;
	}

	protected RelativeLayout loadView() {
		RelativeLayout lRetView = getOptionsManager().getFreeContainerViewFromCache();
		ListView lListView = (ListView) lRetView.findViewById(R.id.defaultListView);
						
		lListView.setOnItemClickListener(this);
		lListView.setOnItemLongClickListener(this);
		lListView.setOnItemSelectedListener(this);
		lListView.setOnFocusChangeListener(focusListener);
		lListView.setOnKeyListener(null);

		return lRetView;
	}
	
	/**
	 * set data to the list view. [data array and data_id array]
	 * @param data
	 * @param dataID
	 */
	public void setData(String[] data, int[] dataID) {
		if(data != null) {
			mData = data.clone();
		}
		if(dataID != null) {
			mDataID = dataID.clone();
		}
	}
	
	private CheckedTextView mLastTextView;
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if(mListListener != null && parent.hasFocus() /*&& mAvailableDataControllability.get(position)*/) {
			mListListener.onListItemSelected(getId(), mAvailableDataID.get(position), parent, view, position, id);
		}
		if (mLastTextView != null && !mLastTextView.isChecked() && mLastTextView.isEnabled()) {
			updateTextView(mLastTextView, false);
		}
		if (view != null) {
			View tempView = (TextView) view.findViewById(R.id.text1);
			if (tempView instanceof CheckedTextView) {
				mLastTextView = ((CheckedTextView) tempView);
				updateTextView(mLastTextView, true);
			}
		}
	}
	
	private void updateTextView(TextView tv, boolean highlight) {
		if (tv != null) {
			Typeface tf = null;
			if (highlight) {
				tf = Typeface.create("sans-serif", Typeface.NORMAL);
			} else {
				tf = Typeface.create("sans-serif-light", Typeface.NORMAL);
			}
			tv.setTypeface(tf);
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
		if(mListListener != null) {
			mListListener.onListNothingSelected(getId(), parent);
		}
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if(mListListener != null && position < mAvailableDataID.size()) {
			return mListListener.onListItemLongClick(getId(), mAvailableDataID.get(position), parent, view, position, id);
		}else
		{
			DdbLogUtility.logCommon(TAG, "onItemLongClick:Invalid position : "+position+" .Available data size: "+mAvailableDataID.size());
		}
		return false;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(mListListener != null && position < mAvailableDataID.size()) {
			mListListener.onListItemClick(getId(), mAvailableDataID.get(position), parent, view, position, id);
		}else
		{
			DdbLogUtility.logCommon(TAG, "onItemClick:Invalid position : "+position+" .Available data size: "+mAvailableDataID.size());
		}
		if(mListType == TYPE_RADIO_LIST) {
			getOptionsManager().scrollToPrevPage();
		}
	}
	
	private OnFocusChangeListener focusListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(mListFocusListener != null) {
				mListFocusListener.onListOptionsNodeFocusChange(getId(), v, hasFocus);
			}			
		}
	};
	
	public interface ListOptionsNodeFocusChangeListener {
		void onListOptionsNodeFocusChange(int nodeResId, View v, boolean hasFocus);
	}
	
	public interface ListOptionsNodeListener {
		void onListItemSelected(int nodeResId, int dataId, AdapterView<?> parent, View view,
                                int position, long id);
		void onListNothingSelected(int nodeResId, AdapterView<?> parent);
		boolean onListItemLongClick(int nodeResId, int dataId, AdapterView<?> parent, View view,
                                    int position, long id);
		void onListItemClick(int nodeResId, int dataId, AdapterView<?> parent, View view,
                             int position, long id);
		boolean isListItemAvailable(int nodeResId, int dataId);
		boolean isListItemControllable(int nodeResId, int dataId);
		int getItemSelectionWhenFocused(int nodeResId);
	}
	//AP206 Check box list should make getarray of selected items
	public interface MultiChoiceOptionsNodeListener extends ListOptionsNodeListener{
		public int[] getMultiItemsSelectionWhenFocused(int nodeResId);
	}
	
	public abstract static class DynamicListOptionsNodeCallback {
		
		/**
		 * Used by FW when a ListON is marked as dynamic, to pull dynamic content from application
		 * @param nodeID
		 * @return new data
		 */
		public abstract String[] getListDynamicData(int nodeID);
		
		/**
		 * Used by FW when a ListON is marked as dynamic, to pull dynamic content IDs from application
		 * @param nodeID
		 * @return new data ID
		 */
		public abstract int[] getListDynamicDataID(int nodeID);
		
		/**
		 * Used by FW when a ListON is marked as dynamic, to pull dynamic content icons from application
		 * @param nodeID
		 * @return new data icons
		 */
		public abstract Drawable[] getListDynamicIcons(int nodeID);
	}

}
