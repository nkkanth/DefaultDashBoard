package org.droidtv.defaultdashboard.ui.presenter;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.tv.TvContentRating;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ProgramDataListener;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.Program;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.ChannelsShelfRowView;
import org.droidtv.defaultdashboard.ui.view.ShelfRowHoverCardView;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.tvclock.ITvClockManager;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.leanback.widget.BaseGridView;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.leanback.widget.FocusHighlight;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.ShadowOverlayHelper;
import androidx.leanback.widget.SinglePresenterSelector;


/**
 * Created by sandeep.kumar on 05/10/2017.
 */

public class ChannelsShelfRowPresenter extends ListRowPresenter {

    private ChannelsShelfHoverCardPresenter mHoverCardPresenter;
    private SinglePresenterSelector mSinglePresenterSelector;
    private static boolean mIsMHLSupported = false;

    public ChannelsShelfRowPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_SMALL);
    }

    public ChannelsShelfRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public ChannelsShelfRowPresenter(boolean useFocusDimmer) {
        this(FocusHighlight.ZOOM_FACTOR_SMALL, useFocusDimmer);
    }

    public ChannelsShelfRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        super(focusZoomFactor, useFocusDimmer);
        initialize();
    }

    @Override
    protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        ChannelsShelfRowView rowView = new ChannelsShelfRowView(parent.getContext());
        setupFadingEdgeEffect(rowView);
        setupAlignment(rowView);
        return new ShelfRowViewHolder(rowView, rowView.getShelfIconImageView(), rowView.getShelfTitleTextView(), rowView.getGridView(), this);
    }

    @Override
    protected void onBindRowViewHolder(RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "onBindRowViewHolder: ");
        if (item instanceof ListRow) {
            ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
            ShelfRow row = (ShelfRow) item;
            if (row.getShelfHeader() != null) {
                ShelfHeaderItem shelfHeaderItem = row.getShelfHeader();
                shelfRowViewHolder.mIconImageView.setImageDrawable(shelfHeaderItem.getShelfIconDrawable());
                shelfRowViewHolder.mTextView.setText(shelfHeaderItem.getTitle());
            } else {
                shelfRowViewHolder.mIconImageView.setImageResource(R.mipmap.ic_launcher);
            }
            int initialSelectedItemPosition = getLastSelectedChannelPosition(row);
            if (initialSelectedItemPosition != 0) {
                shelfRowViewHolder.getGridView().setSelectedPosition(initialSelectedItemPosition);
            }
        }
    }

    @Override
    protected void onRowViewExpanded(final RowPresenter.ViewHolder holder, boolean expanded) {
        DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "onRowViewExpanded() called with: holder = [" + holder + "], expanded = [" + expanded + "]");
        if (getHoverCardPresenterSelector() == null) {
            setHoverCardPresenterSelector(mSinglePresenterSelector);
        }
        super.onRowViewExpanded(holder, expanded);
        ShelfRowViewHolder shelfRowViewHolder = (ShelfRowViewHolder) holder;
        shelfRowViewHolder.mTextView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        setVerticalPadding(shelfRowViewHolder);

        ChannelsShelfRowView shelfRowView = (ChannelsShelfRowView) shelfRowViewHolder.view;
        if (expanded) {
            shelfRowView.onExpanded();
        } else {
            shelfRowView.onCollapsed();
        }

        // Refresh the row so that lock icons are shown/hidden based on the row's expanded state
        ItemBridgeAdapter adapter = shelfRowViewHolder.getBridgeAdapter();
        int itemCount = adapter.getItemCount();
        adapter.notifyItemRangeChanged(0, itemCount);

        if (!expanded) {
            setHoverCardPresenterSelector(null);
        }
    }

    @Override
    protected void onRowViewSelected(RowPresenter.ViewHolder holder, boolean selected) {
        super.onRowViewSelected(holder, selected);
        setVerticalPadding((ShelfRowViewHolder) holder);
    }

    @Override
    protected ShadowOverlayHelper.Options createShadowOverlayOptions() {
        ShadowOverlayHelper.Options shadowOverlayOptions = new ShadowOverlayHelper.Options();
        shadowOverlayOptions.dynamicShadowZ(0, Constants.SHELF_ITEM_FOCUS_ELEVATION);
        return shadowOverlayOptions;
    }

    private void initialize() {
        mHoverCardPresenter = new ChannelsShelfHoverCardPresenter();
        mSinglePresenterSelector = new SinglePresenterSelector(mHoverCardPresenter);
        mIsMHLSupported = DashboardDataManager.getInstance().getTvSettingsManager().getInt(TvSettingsConstants.OPMHL, 0, 0) == 1;
    }

    private void setupFadingEdgeEffect(ChannelsShelfRowView rowView) {
        HorizontalGridView gridView = rowView.getGridView();
        TypedArray ta = gridView.getContext()
                .obtainStyledAttributes(androidx.leanback.R.styleable.LeanbackTheme);
        int browseRowsFadingEdgeLength = (int) ta.getDimension(
                androidx.leanback.R.styleable.LeanbackTheme_browseRowsFadingEdgeLength, 32);
        ta.recycle();

        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (isRtl) {
            gridView.setFadingRightEdge(true);
            gridView.setFadingRightEdgeLength(browseRowsFadingEdgeLength);
        } else {
            gridView.setFadingLeftEdge(true);
            gridView.setFadingLeftEdgeLength(browseRowsFadingEdgeLength);
        }
    }

    private void setupAlignment(ChannelsShelfRowView rowView) {
        boolean isRtl = rowView.getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        HorizontalGridView gridView = rowView.getGridView();
        gridView.setItemAlignmentOffset(0);
        gridView.setItemAlignmentOffsetPercent(0);
        gridView.setWindowAlignmentOffset(isRtl ? gridView.getPaddingStart() + gridView.getFadingRightEdgeLength() : gridView.getFadingLeftEdgeLength() + gridView.getPaddingStart());
        gridView.setWindowAlignmentOffsetPercent(0);
        gridView.setWindowAlignment(BaseGridView.WINDOW_ALIGN_LOW_EDGE);
    }

    private void setVerticalPadding(ListRowPresenter.ViewHolder vh) {
        int leftPadding = vh.view.getResources().getDimensionPixelSize(R.dimen.shelf_row_horizontal_gridview_padding);
        int topPadding = 0;
        int bottomPadding = 0;
        int rightPadding = 0;
        vh.getGridView().setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
    }

    private int getLastSelectedChannelPosition(ShelfRow row) {
        int lastSelectedChannelId = DashboardDataManager.getInstance().getLastSelectedChannelId();
        if (lastSelectedChannelId == -1) {
            return 0;
        }
        CursorObjectAdapter adapter = (CursorObjectAdapter) row.getAdapter();
        for (int i = 0; adapter != null && i < adapter.size(); i++) {
            Channel channel = (Channel) adapter.get(i);
            if (channel.getMappedId() == lastSelectedChannelId) {
                return i;
            }
        }
        return 0;
    }

    public static class ShelfRowViewHolder extends ViewHolder {

        private ImageView mIconImageView;
        private TextView mTextView;

        public ShelfRowViewHolder(View rootView, ImageView shelfIconImageView, TextView shelfTitleTextView, HorizontalGridView gridView, ListRowPresenter rowPresenter) {
            super(rootView, gridView, rowPresenter);
            mIconImageView = shelfIconImageView;
            mTextView = shelfTitleTextView;
        }
    }

    private static final class ChannelsShelfHoverCardPresenter extends Presenter implements ProgramDataListener {

        private UiThreadHandler mUiThreadHandler;
        private Channel mChannel;
        private ShelfRowHoverCardView mHoverCardView;
        private SimpleDateFormat mSimpleDateFormat;
        private Date mRecyclableDateObject;

        private static final long PROGRAM_DATA_FETCH_DELAY_MS = 500;

        private ChannelsShelfHoverCardPresenter() {
            mUiThreadHandler = new UiThreadHandler(this);
            mSimpleDateFormat = new SimpleDateFormat("HH:mm");
            mSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            mRecyclableDateObject = new Date();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            ShelfRowHoverCardView shelfHoverCardView = new ShelfRowHoverCardView(parent.getContext());
            return new RowPresenter.ViewHolder(shelfHoverCardView);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ShelfRowHoverCardView shelfHoverCardView = (ShelfRowHoverCardView) viewHolder.view;
            if (item instanceof Channel) {
                Channel channel = (Channel) item;
                mChannel = channel;
                mHoverCardView = shelfHoverCardView;
                if (Channel.isHdmiSource(channel) && (isHdmiMHLEnabled(channel))) {
                    populateHoverCardView(shelfHoverCardView, channel.getDisplayName(), "", mHoverCardView.getContext().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_HDMI_1_SOURCE_DESCRIPTION));
                } else {
                    populateHoverCardView(shelfHoverCardView, channel.getDisplayName(), "", "");
                }
                if (!Channel.isHdmiSource(channel) && !Channel.isVgaSource(channel) && DashboardDataManager.getInstance().isEpgEnabled()) {
                    sendMessage(UiThreadHandler.MSG_WHAT_FETCH_PROGRAM_DATA, mChannel);
                }
            }
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {

        }

        private boolean isHdmiMHLEnabled(Channel channel) {
            String inputID = channel.getInputId();
            if (inputID != null) {
                Source source = DashboardDataManager.getInstance().getSource(inputID);
                if (source != null) {
                    if ((source.getHDMIPortId() == Constants.HDMI1_MHL_PORT_ID) && mIsMHLSupported) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onProgramDataFetchComplete(int channelId, Program program) {
            DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "onProgramDataFetchComplete");
            if (channelId == mChannel.getId() && program != null) {
                DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "If inside onProgramDataFetchComplete");
                String subtext = buildSubtext(program ,mChannel);
                populateHoverCardView(mHoverCardView, mChannel.getDisplayName(), subtext, program.getDescription());
            }
        }

        private String buildSubtext(Program program , Channel channel) {
            DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "buildSubtext");
            long programStartTime = program.getStartTime();
            long programEndTime = program.getEndTime();

            mRecyclableDateObject.setTime(ITvClockManager.Instance.getInterface().GetAbsoluteLocalTime(programStartTime));
            String startTimeString = mSimpleDateFormat.format(mRecyclableDateObject);

            mRecyclableDateObject.setTime(ITvClockManager.Instance.getInterface().GetAbsoluteLocalTime(programEndTime));
            String endTimeString = mSimpleDateFormat.format(mRecyclableDateObject);

            String title = program.getTitle();
            if (TextUtils.isEmpty(title)) {
                title = "";
            } else {
                title = title.concat(" ").concat(Constants.DOT_SEPARATOR_UNICODE_CODE);
            }
            String genre = program.getGenre();
            if (TextUtils.isEmpty(genre)) {
                genre = "";
            } else {
                genre = mHoverCardView.getContext().getString(org.droidtv.ui.strings.R.string.MAIN_METADATA_GENRE).concat(" ").concat(genre).
                        concat(" ").concat(Constants.DOT_SEPARATOR_UNICODE_CODE);
            }
            String rating = "";
            if(DashboardDataManager.getInstance().isNAFTA()){
                DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "If inside NAFTA ");
                rating = buildNaftaRatingString(mHoverCardView.getContext(),program ,channel);
            }else{
                DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "Else NAFTA ");
                rating = buildRatingString(mHoverCardView.getContext(), program);
            }

            return title.concat(" ").concat(startTimeString).concat(" - ").concat(endTimeString).concat(" ").concat(Constants.DOT_SEPARATOR_UNICODE_CODE).
                    concat(" ").concat(genre).concat(" ").concat(rating);
        }
        private String buildNaftaRatingString(Context context ,Program program ,Channel channel ){
            int lastSelectedChannelId = DashboardDataManager.getInstance().getLastSelectedChannelId();
            DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "buildNaftaRatingString: MappedChannelID :"+channel.getMappedId() + " lastSelectedChannelId :"+lastSelectedChannelId);
            if (program != null  && channel.getMappedId() == lastSelectedChannelId) {
                String sCurrRating = null;
                try{
                    sCurrRating = program.getRatings()[0];
                }catch (Exception e){
                    Log.d(ChannelsShelfRowPresenter.class.getName(), "buildNaftaRatingString: "+e.getMessage());
                }
                DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "buildNaftaRatingString: "+sCurrRating +" for program :"+program.getTitle());
                if(sCurrRating == null){
                    sCurrRating = "";
                }
                return context.getString(org.droidtv.ui.strings.R.string.MAIN_OSD_PARENTAL_RATING).concat(" ").concat(sCurrRating);
            }else{
                return "";
            }
        }

        private String buildRatingString(Context context, Program program) {
            String[] ratings = program.getRatings();
            if (ratings == null || ratings.length == 0) {
                return context.getString(org.droidtv.ui.strings.R.string.MAIN_OSD_PARENTAL_RATING).
                        concat(" ").concat(context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_NONE));
            }
            StringBuilder ratingTextBuilder = new StringBuilder();
            for (int i = 0; i < ratings.length; i++) {
                int ratingStringId = -1;
                try {
                    ratingStringId = DashboardDataManager.getInstance().fetchContentRatingStringId(Integer.parseInt(ratings[i]));
                } catch (NumberFormatException e) {
                    ratingStringId = DashboardDataManager.getInstance().fetchContentRatingStringId(ratings[i]);
                }
                if (ratingStringId != -1) {
                    ratingTextBuilder.append(context.getString(ratingStringId)).append(",");
                } else {
                    ratingTextBuilder.append(context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_NONE)).append(",");
                }
            }
            if (ratingTextBuilder.length() == 0) {
                for (String rating : ratings) {
                    if (!TextUtils.isEmpty(rating)) {
                        TvContentRating tvContentRating = TvContentRating.unflattenFromString(rating);
                        if (tvContentRating != null) {
                            ratingTextBuilder.append(tvContentRating.getMainRating()).append(',');
                        }
                    }
                }
                if (ratingTextBuilder.length() > 0) {
                    ratingTextBuilder.deleteCharAt(ratingTextBuilder.length() - 1);
                } else {
                    ratingTextBuilder.append(context.getString(org.droidtv.ui.strings.R.string.MAIN_INFO_NONE));
                }
            } else {
                ratingTextBuilder.deleteCharAt(ratingTextBuilder.length() - 1);
            }
            return context.getString(org.droidtv.ui.strings.R.string.MAIN_OSD_PARENTAL_RATING).concat(" ").concat(ratingTextBuilder.toString());
        }

        private void populateHoverCardView(ShelfRowHoverCardView hoverCardView, String title, String subtitle, String description) {
            DdbLogUtility.logTVChannelChapter("ChannelsShelfRowPresenter", "populateHoverCardView() called with: hoverCardView = [" + hoverCardView + "], title = [" + title + "], subtitle = [" + subtitle + "], description = [" + description + "]");
            hoverCardView.setTitle(title);
            hoverCardView.setSubText(subtitle);
            hoverCardView.setDescription(description);
        }

        private void fetchProgramData(Channel channel) {
            DashboardDataManager.getInstance().fetchProgramDataForChannel(channel, this);
        }

        private void sendMessage(int what, Object channel) {
            mUiThreadHandler.removeMessages(what);
            Message message = Message.obtain();
            message.what = what;
            message.obj = channel;
            mUiThreadHandler.sendMessageDelayed(message, PROGRAM_DATA_FETCH_DELAY_MS);
        }
    }

    private static final class UiThreadHandler extends Handler {

        private WeakReference<ChannelsShelfHoverCardPresenter> mHoverCardPresenterRef;

        private static final int MSG_WHAT_FETCH_PROGRAM_DATA = 100;

        private UiThreadHandler(ChannelsShelfHoverCardPresenter hoverCardPresenter) {
            mHoverCardPresenterRef = new WeakReference<ChannelsShelfHoverCardPresenter>(hoverCardPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_WHAT_FETCH_PROGRAM_DATA) {
                ChannelsShelfHoverCardPresenter hoverCardPresenter = mHoverCardPresenterRef.get();
                if (hoverCardPresenter == null) {
                    return;
                }
                Channel channel = (Channel) msg.obj;
                hoverCardPresenter.fetchProgramData(channel);
                return;
            }
        }
    }
}
