package org.droidtv.defaultdashboard.ui.presenter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.MessageCountChangeListener;
import org.droidtv.defaultdashboard.data.model.moreChapter.MessagesItem;
import org.droidtv.defaultdashboard.data.model.moreChapter.MoreChapterShelfItem;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.MoreChapterShelfItemView;

import androidx.leanback.widget.Presenter;

/**
 * Created by bhargava.gugamsetty on 19-12-2017.
 */

public class MoreChapterShelfItemPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        DdbLogUtility.logMoreChapter("MoreChapterShelfItemPresenter", "onCreateViewHolder: ");
        MoreChapterShelfItemView moreChapterShelfItemView = new MoreChapterShelfItemView(parent.getContext());
        TextView textView = moreChapterShelfItemView.getMoreChapterShelfItemTitleView();
        final ImageView imageView = moreChapterShelfItemView.getMoreChapterShelfItemImageView();
        final TextView numericTextView = moreChapterShelfItemView.getMoreChapterShelfItemNumericTextView();
        return new MoreShelfItemViewHolder(moreChapterShelfItemView, textView, imageView, numericTextView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        DdbLogUtility.logMoreChapter("MoreChapterShelfItemPresenter", "onBindViewHolder() called with: viewHolder = [" + viewHolder + "], item = [" + item + "]");
        MoreChapterShelfItem moreChapterShelfItem = (MoreChapterShelfItem) item;
        final MoreShelfItemViewHolder moreShelfItemViewHolder = (MoreShelfItemViewHolder) viewHolder;
        moreShelfItemViewHolder.mMoreChapterItemTextView.setText(moreChapterShelfItem.getName());
        moreShelfItemViewHolder.mMoreChapterItemImageView.setImageDrawable(moreChapterShelfItem.getIcon());


        if (moreChapterShelfItem instanceof MessagesItem) {
            MessagesItem messagesItem = (MessagesItem) moreChapterShelfItem;
            int messageCount = DashboardDataManager.getInstance().getMessageCount();
            if (messageCount > 0) {
                if (messageCount > moreShelfItemViewHolder.mContext.getResources().getInteger(R.integer.maximum_message_count_to_display)) {
                    moreShelfItemViewHolder.mNumericTextView.setText(String.valueOf(moreShelfItemViewHolder.mContext.getResources().getInteger(R.integer.maximum_message_count_to_display)));
                } else {
                    moreShelfItemViewHolder.mNumericTextView.setText(String.valueOf(messageCount));
                }
                moreShelfItemViewHolder.mNumericTextView.setVisibility(View.VISIBLE);
            } else {
                moreShelfItemViewHolder.mNumericTextView.setVisibility(View.GONE);
            }
            DashboardDataManager.getInstance().addMessageCountChangeListener(new MessageCountChangeListener() {
                @Override
                public void onMessageCountChanged() {
                    int messageCount = DashboardDataManager.getInstance().getMessageCount();
                    if (messageCount > 0) {
                        if (messageCount > moreShelfItemViewHolder.mContext.getResources().getInteger(R.integer.maximum_message_count_to_display)) {
                            moreShelfItemViewHolder.mNumericTextView.setText(String.valueOf(moreShelfItemViewHolder.mContext.getResources().getInteger(R.integer.maximum_message_count_to_display)));
                        } else {
                            moreShelfItemViewHolder.mNumericTextView.setText(String.valueOf(messageCount));
                        }
                        moreShelfItemViewHolder.mNumericTextView.setVisibility(View.VISIBLE);
                    } else {
                        moreShelfItemViewHolder.mNumericTextView.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            moreShelfItemViewHolder.mNumericTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }

    class MoreShelfItemViewHolder extends ViewHolder {
        private TextView mMoreChapterItemTextView;
        private ImageView mMoreChapterItemImageView;
        private TextView mNumericTextView;
        private Context mContext;

        MoreShelfItemViewHolder(View view, TextView textView, ImageView imageView, TextView numericTextView) {
            super(view);
            mContext = view.getContext();
            mMoreChapterItemTextView = textView;
            mMoreChapterItemImageView = imageView;
            mNumericTextView = numericTextView;
        }

        TextView getTitleView() {
            return mMoreChapterItemTextView;
        }
    }
}
