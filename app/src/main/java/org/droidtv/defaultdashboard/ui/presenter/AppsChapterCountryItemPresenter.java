package org.droidtv.defaultdashboard.ui.presenter;

import android.view.View;
import android.view.ViewGroup;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.AppsChapterCountryItemView;

import androidx.leanback.widget.Presenter;

/**
 * Created by bhargava.gugamsetty on 02-02-2018.
 */

public class AppsChapterCountryItemPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        DdbLogUtility.logAppsChapter("AppsChapterCountryItemPresenter", "onCreateViewHolder() called with: parent = [" + parent + "]");
        AppsChapterCountryItemView appsChapterCountryItemView = new AppsChapterCountryItemView(parent.getContext());
        return new AppsShelfItemViewHolder(appsChapterCountryItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        DdbLogUtility.logAppsChapter("AppsChapterCountryItemPresenter", "onBindViewHolder() called with: viewHolder = [" + viewHolder + "], item = [" + item + "]");
        AppsChapterCountryItemPresenter.AppsShelfItemViewHolder appsShelfItemViewHolder = (AppsChapterCountryItemPresenter.AppsShelfItemViewHolder) viewHolder;
        setLayoutParamsForOpenAppItem(appsShelfItemViewHolder);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        AppsShelfItemViewHolder appsItemViewHolder = (AppsShelfItemViewHolder) viewHolder;
        resetLayoutParams(appsItemViewHolder);
    }

    private void setLayoutParamsForOpenAppItem(AppsShelfItemViewHolder appsShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = appsShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = appsShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.apps_chapter_country_item_image_width_default);
        layoutParams.height = appsShelfItemViewHolder.view.getResources().getDimensionPixelSize(R.dimen.apps_chapter_country_item_image_height_default);
        appsShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    private void resetLayoutParams(AppsShelfItemViewHolder appsShelfItemViewHolder) {
        ViewGroup.LayoutParams layoutParams = appsShelfItemViewHolder.view.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        appsShelfItemViewHolder.view.setLayoutParams(layoutParams);
    }

    class AppsShelfItemViewHolder extends ViewHolder {

        public AppsShelfItemViewHolder(View view) {
            super(view);
        }
    }
}
