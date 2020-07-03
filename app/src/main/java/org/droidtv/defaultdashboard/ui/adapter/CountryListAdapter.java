package org.droidtv.defaultdashboard.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ThumbnailBitmapFetchListener;
import org.droidtv.defaultdashboard.data.model.appsChapter.CountryAppListItem;
import org.droidtv.defaultdashboard.ui.view.CountryItemView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by bhargava.gugamsetty on 30-01-2018.
 */

public class CountryListAdapter extends RecyclerView.Adapter<CountryListAdapter.CountryListViewHolder> {

    private ArrayList<CountryAppListItem> mCountryListData;
    private CountryItemClickListener mCountryItemClickListener;
    private LanguageItemClickListener mLanguageItemClickListener;
    private Context mContext;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;

    private static final int ANIMATION_DURATION_MS = 100;

    public CountryListAdapter(Context context, ArrayList<CountryAppListItem> countryList) {
        mCountryListData = countryList;
        mContext = context;

        TypedValue typedValue = new TypedValue();
        mContext.getResources().getValue(R.dimen.country_list_item_default_scale, typedValue, true);
        mDefaultScale = typedValue.getFloat();

        mContext.getResources().getValue(R.dimen.country_list_item_focus_scale, typedValue, true);
        mFocusScale = typedValue.getFloat();

        mDefaultElevation = mContext.getResources().getDimension(R.dimen.country_list_item_default_elevation);
        mFocusElevation = mContext.getResources().getDimension(R.dimen.country_list_item_focus_elevation);
    }

    @Override
    public CountryListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        CountryItemView view = new CountryItemView(viewGroup.getContext());
        return new CountryListViewHolder(view, mCountryItemClickListener, mLanguageItemClickListener);
    }

    @Override
    public void onBindViewHolder(CountryListViewHolder holder, final int position) {
        holder.getTextView().setText(mCountryListData.get(position).getCountryLabelResId());
        int drawableResourceId = mCountryListData.get(position).getCountryIconResId();
        holder.setId(drawableResourceId);
        holder.getImageView().setImageResource(drawableResourceId);
    }

    @Override
    public void onViewRecycled(CountryListViewHolder holder) {
        holder.getImageView().setImageDrawable(null);
    }

    @Override
    public boolean onFailedToRecycleView(CountryListViewHolder holder) {
        return true;
    }

    @Override
    public int getItemCount() {
        return mCountryListData.size();
    }

    public class CountryListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnFocusChangeListener, ThumbnailBitmapFetchListener {
        private TextView countryLabelText;
        private ImageView countryIconImage;
        private long mId;

        public CountryListViewHolder(View rootView, CountryItemClickListener listener, LanguageItemClickListener languageListener) {
            super(rootView);
            rootView.setOnClickListener(this);
            rootView.setOnFocusChangeListener(this);
            mCountryItemClickListener = listener;
            mLanguageItemClickListener = languageListener;
            countryLabelText = (TextView) rootView.findViewById(R.id.country_icon_text_view);
            countryLabelText.setSelected(true);
            countryIconImage = (ImageView) rootView.findViewById(R.id.country_icon_image_view);
        }

        public TextView getTextView() {
            return countryLabelText;
        }

        public ImageView getImageView() {
            return countryIconImage;
        }

        public void setId(long id) {
            mId = id;
        }

        @Override
        public void onClick(View view) {
            if (mCountryItemClickListener != null) {
                mCountryItemClickListener.onCountryItemClick(view, this.getAdapterPosition());
            }
            if (mLanguageItemClickListener != null) {
                mLanguageItemClickListener.onLanguageItemClick(view, this.getAdapterPosition());
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                CountryItemView countryItemView = (CountryItemView) itemView;
                scale(countryItemView, mFocusScale);
                elevate(countryItemView, mFocusElevation);
                elevate(countryItemView.getImageView(), mFocusElevation);
                countryItemView.showCountryLabel();
            } else {
                CountryItemView currentSelectedCountryView = (CountryItemView) itemView;
                scale(currentSelectedCountryView, mDefaultScale);
                elevate(currentSelectedCountryView, mDefaultElevation);
                elevate(currentSelectedCountryView.getImageView(), mDefaultElevation);
                currentSelectedCountryView.hideCountryLabel();
            }
        }

        @Override
        public void onThumbnailBitmapFetchComplete(long id, Bitmap bitmap) {
            if (mId == id) {
                if (bitmap != null) {
                    getImageView().setImageBitmap(bitmap);
                } else {
                    getImageView().setImageDrawable(null);
                }
            }
        }

        private void scale(View view, float scale) {
            view.animate().setDuration(ANIMATION_DURATION_MS).scaleX(scale).start();
            view.animate().setDuration(ANIMATION_DURATION_MS).scaleY(scale).start();
        }

        private void elevate(View view, float elevation) {
            view.setElevation(elevation);
        }
    }

    public void setCountryItemClickListener(CountryItemClickListener itemClickListener) {
        mCountryItemClickListener = itemClickListener;
    }

    public void setLanguageItemClickListener(LanguageItemClickListener itemClickListener) {
        mLanguageItemClickListener = itemClickListener;
    }

    public interface CountryItemClickListener {
        void onCountryItemClick(View view, int position);
    }

    public interface LanguageItemClickListener {
        void onLanguageItemClick(View view, int position);
    }
}
