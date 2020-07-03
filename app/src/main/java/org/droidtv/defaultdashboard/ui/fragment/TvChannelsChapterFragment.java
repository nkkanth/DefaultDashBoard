package org.droidtv.defaultdashboard.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.ChannelDataListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.SourceDataListener;
import org.droidtv.defaultdashboard.data.model.Channel;
import org.droidtv.defaultdashboard.data.model.ChannelCursorMapper;
import org.droidtv.defaultdashboard.data.model.ShelfHeaderItem;
import org.droidtv.defaultdashboard.data.model.ShelfRow;
import org.droidtv.defaultdashboard.data.model.Source;
import org.droidtv.defaultdashboard.data.model.channelFilter.ChannelFilter;
import org.droidtv.defaultdashboard.data.model.channelFilter.TifChannelsFilter;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.presenter.ChannelsShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.ChannelsShelfRowPresenter;
import org.droidtv.defaultdashboard.ui.presenter.SourcesShelfItemPresenter;
import org.droidtv.defaultdashboard.ui.presenter.SourcesShelfRowPresenter;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.CursorObjectAdapter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

/**
 * Created by sandeep.kumar on 10/10/2017.
 */

public class TvChannelsChapterFragment extends ChapterFragment implements SourceDataListener, ChannelDataListener {

    private static final String TAG = "TvChannelsChapterFragment";

    private ArrayObjectAdapter mSourcesShelfItemAdapter;
    private ShelfRow mSourcesShelfRow;
    private List<ShelfRow> mAvailableChannelFilterShelves;
    private DashboardDataManager mDashboardDataManager;
    private HashMap<String, CursorObjectAdapter> mAdapterMap;

    private static final int SOURCES_SHELF_POSITION = 0;
    private static final int AVAILABLE_CHANNEL_FILTERS_STARTING_SHELF_POSITION = 1;

    private static final int SOURCES_SHELF_ROW_ID = 1;

    public TvChannelsChapterFragment() {
        super();
        mDashboardDataManager = DashboardDataManager.getInstance();
        mAdapterMap = new HashMap<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "#### onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "#### onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "#### onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        registerListeners();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "#### onDestroyView()");

        unregisterListeners();

        cleanUp();
        super.onDestroyView();

        DdbLogUtility.logTVChannelChapter(TAG, "#### onDestroyView() exit");
    }

    @Override
    protected void createRows() {
        Log.d(TAG, "#### createRows()");
        if (DashboardDataManager.getInstance().areSourcesEnabled()) {
            createSourcesShelf();
        }
        createAvailableChannelFilterShelves();
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    private void registerSourcesChangeListener() {
        DashboardDataManager.getInstance().registerSourceDataListener(this);
    }

    private void unregisterSourcesChangeListener() {
        DashboardDataManager.getInstance().unregisterSourceDataListener(this);
    }

    @Override
    protected PresenterSelector getChapterPresenterSelector() {
        return new TvChannelsChapterPresenterSelector();
    }

    private void createSourcesShelf() {
        DdbLogUtility.logTVChannelChapter(TAG, "createSourcesShelf() called");
        mSourcesShelfRow = buildSourcesShelf();
        if (mSourcesShelfRow != null) {
            addRow(SOURCES_SHELF_POSITION, mSourcesShelfRow);
        }
    }

    private ShelfRow buildSourcesShelf() {
        List<Source> sources = new ArrayList<>(DashboardDataManager.getInstance().getSources());
        if(sources == null || sources.isEmpty()){
            Log.d(TAG, "buildSourcesShelf: No source created");
            return null;
        }
        SourcesShelfItemPresenter itemPresenter = new SourcesShelfItemPresenter();
        itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
        mSourcesShelfItemAdapter = new ArrayObjectAdapter(itemPresenter);
        for (Source source : sources) {
            mSourcesShelfItemAdapter.add(source);
        }
        String headerTitle = getString(org.droidtv.ui.htvstrings.R.string.HTV_ITEM_SOURCES);
        Drawable headerIcon = ContextCompat.getDrawable(getContext(), org.droidtv.ui.tvwidget2k15.R.drawable.icon_192_sources_48x48);
        return new ShelfRow(SOURCES_SHELF_ROW_ID, new ShelfHeaderItem(headerTitle, headerIcon), mSourcesShelfItemAdapter);
    }

    private boolean replaceSourcesShelfRow(ShelfRow row) {
        removeRow(mSourcesShelfRow);
        mSourcesShelfRow = row;
        addRow(SOURCES_SHELF_POSITION, mSourcesShelfRow);
        return true;
    }

    private void removeSourcesShelfRow() {
        removeRow(mSourcesShelfRow);
        mSourcesShelfRow = null;
    }

    private void createAvailableChannelFilterShelves() {
        Log.d(TAG, "#### createAvailableChannelFilterShelves()");
        mAvailableChannelFilterShelves = buildAvailableChannelFilterShelves();
        if (mAvailableChannelFilterShelves == null) {
            return;
        }

        for (ShelfRow shelf : mAvailableChannelFilterShelves) {
            if (shelf != null) {
                addRow(shelf);
            }
        }
    }

    private List<ShelfRow> buildAvailableChannelFilterShelves() {
        Log.d(TAG, "#### buildAvailableChannelFilterShelves()");
        List<ShelfRow> availableChannelFilterShelves = new ArrayList<>();
        List<ChannelFilter> availableChannelFilters = mDashboardDataManager.getAvailableChannelFilters();
        if (availableChannelFilters == null || availableChannelFilters.isEmpty()) {
            return null;
        }

        for (ChannelFilter channelFilter : availableChannelFilters) {
            if (channelFilter == null || !channelFilter.hasChannels()) {
                continue;
            }
            CursorObjectAdapter adapter = mAdapterMap.get(channelFilter.getName());
            if (adapter == null) {
                ChannelsShelfItemPresenter itemPresenter = new ChannelsShelfItemPresenter();
                itemPresenter.setFacet(HeaderVisibilityFacet.class, getHeaderVisibilityFacet());
                adapter = new CursorObjectAdapter(itemPresenter);
                adapter.setMapper(new ChannelCursorMapper());
                mAdapterMap.put(channelFilter.getName(), adapter);
            }
            adapter./*swapCursor*/changeCursor(channelFilter.getCursor());

            String headerTitle = channelFilter.getName();
            Drawable headerIcon = channelFilter.getIcon();
            ShelfRow shelf = new ShelfRow(new ShelfHeaderItem(headerTitle, headerIcon), adapter);
            availableChannelFilterShelves.add(shelf);
        }
        return availableChannelFilterShelves;
    }

    private boolean replaceAvailableChannelFilterShelves(List<ShelfRow> rows) {
        Log.d(TAG, "#### replaceAvailableChannelFilterShelves()");
        removeRows(mAvailableChannelFilterShelves);
        mAvailableChannelFilterShelves = rows;
        Log.d(TAG, "#### replaceAvailableChannelFilterShelves().before addRows");
        addRows(getAvailableChannelFiltersStartingShelfPosition(), mAvailableChannelFilterShelves);
        Log.d(TAG, "#### replaceAvailableChannelFilterShelves().after addRows");
        return true;
    }

    private void removeAvailableChannelFilterShelves() {
        Log.d(TAG, "#### removeAvailableChannelFilterShelves().before removeRows");
        removeRows(mAvailableChannelFilterShelves);
        if (mAvailableChannelFilterShelves != null) {
            mAvailableChannelFilterShelves.clear();
        }
    }

    private void registerChannelDataListener() {
        mDashboardDataManager.registerChannelDataListener(this);
    }

    private void unregisterChannelDataListener() {
        mDashboardDataManager.unregisterChannelDataListener(this);
    }

    private void registerListeners() {
        registerSourcesChangeListener();
        registerChannelDataListener();
    }

    private void unregisterListeners() {
        unregisterSourcesChangeListener();
        unregisterChannelDataListener();
    }

    private void cleanUp() {
        //mDashboardDataManager.clearChannelLogoCache();
        //mDashboardDataManager.clearProgramThumbnailCache();
    }

    private int getSourcesShelfPosition() {
        return SOURCES_SHELF_POSITION;
    }

    private int getAvailableChannelFiltersStartingShelfPosition() {
        int position = AVAILABLE_CHANNEL_FILTERS_STARTING_SHELF_POSITION;
        if (!isSourcesShelfAvailable()) {
            position--;
        }
        return position;
    }

    private boolean isSourcesShelfAvailable() {
        return mSourcesShelfRow != null;
    }

    private ChannelFilter findChannelFilter(String filterName) {
        List<ChannelFilter> availableFilters = mDashboardDataManager.getAvailableChannelFilters();
        for (int i = 0; availableFilters != null && i < availableFilters.size(); i++) {
            ChannelFilter filter = availableFilters.get(i);
            if (filter != null && filter.getName().equals(filterName)) {
                return filter;
            }
        }
        return null;
    }

    private void changeActiveChannelFilter(ChannelFilter newFilter) {
        if (newFilter != null) {
            int currentFilterId = mDashboardDataManager.getActiveChannelFilterId();
            int newFilterId = newFilter.getId();
            if (currentFilterId != newFilterId) {
                mDashboardDataManager.setActiveChannelFilterId(newFilterId);
            }
            if (ChannelFilter.isTifChannelFilter(newFilter)) {
                String currentTifInputId = mDashboardDataManager.getActiveTifInputId();
                String newTifInputId = ((TifChannelsFilter) newFilter).getTifInputId();
                if (!newTifInputId.equals(currentTifInputId)) {
                    mDashboardDataManager.setActiveTifInputId(newTifInputId);
                }
            }
        }
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Source) {
            onSourceClicked((Source) item);
            DashboardDataManager.getInstance().setActiveChannelFilterId(TvSettingsDefinitions.PbsActiveChannelfilter.HTV_CHANNELFILTER_SOURCE);
            return;
        }

        if (item instanceof Channel) {
            // Change the active filter id if current active filter is different from this filter
            ShelfRow shelfRow = (ShelfRow) row;
            // Row header title is the filter name
            String filterName = shelfRow.getShelfHeader().getTitle();
            ChannelFilter filter = findChannelFilter(filterName);
            changeActiveChannelFilter(filter);
            // Perform tuning action
            onChannelClicked((Channel) item);
            return;
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

    }

    @Override
    public void onSourcesFetched(List<Source> sources) {
        if (mSourcesShelfItemAdapter != null) {

            mSourcesShelfItemAdapter.clear();
            // If no sources found, then remove the Sources shelf
            if (sources == null || sources.isEmpty()) {
                removeSourcesShelfRow();
                return;
            }

            // If Source shelf is already added, update the Source items
            if (mSourcesShelfRow != null) {
                List<Source> updatedSources = new ArrayList<>(sources);
                for (Source source : updatedSources) {
                    mSourcesShelfItemAdapter.add(source);
                }
                mSourcesShelfItemAdapter.notifyItemRangeChanged(0, updatedSources.size());
                return;
            }
        }

        // Sources shelf has been removed or not yet created. Create the Sources shelf (only if all sources are enabled!)
        if (DashboardDataManager.getInstance().areSourcesEnabled()) {
            createSourcesShelf();
        }

    }

    @Override
    public void onSourceDisplayChanged() {
        if (DashboardDataManager.getInstance().areSourcesEnabled()) {
            createSourcesShelf();
        } else {
            removeSourcesShelfRow();
        }
    }

    @Override
    public void onSourceUpdated(int index, Source source) {
		if(mSourcesShelfItemAdapter != null){
			mSourcesShelfItemAdapter.removeItems(index, 1);
			mSourcesShelfItemAdapter.add(index, source);
			mSourcesShelfItemAdapter.notifyItemRangeChanged(index, 1);
		}
    }

    @Override
    public void onLastSelectedDeviceChanged(int updatedDevice) {
        if (mSourcesShelfRow != null) {
            notifyRowChanged(getSourcesShelfPosition());
        }
    }

    @Override
    public void onChannelFilterUpdated(ChannelFilter filter) {

    }

    @Override
    public void onAvailableChannelFiltersFetched(List<ChannelFilter> filters) {
        Log.d(TAG, "#### onAvailableChannelFiltersFetched()");
        List<ShelfRow> rows = buildAvailableChannelFilterShelves();
        if (rows != null && !rows.isEmpty()) {
            replaceAvailableChannelFilterShelves(rows);
        } else {
            removeAvailableChannelFilterShelves();
        }
    }

    @Override
    public void onActiveChannelFilterFetched(ChannelFilter activeFilter) {

    }

    @Override
    public void onChannelLogoEnabledStateChanged(boolean logosEnabled) {
        if (mAdapterMap != null) {
            for (CursorObjectAdapter adapter : mAdapterMap.values()) {
                int channelCount = adapter.size();
                adapter.notifyItemRangeChanged(0, channelCount);
            }
        }
    }

    @Override
    public void onLastSelectedChannelUriChanged(String updatedLastSelectedChannelUri) {
        if (mAvailableChannelFilterShelves != null && !mAvailableChannelFilterShelves.isEmpty()) {
            notifyRowsChanged(AVAILABLE_CHANNEL_FILTERS_STARTING_SHELF_POSITION, mAvailableChannelFilterShelves.size());
        }
    }

    private static final class TvChannelsChapterPresenterSelector extends PresenterSelector {
        private SourcesShelfRowPresenter mSourcesShelfRowPresenter;
        private ChannelsShelfRowPresenter mChannelsShelfRowPresenter;

        private Presenter[] mPresenters;

        TvChannelsChapterPresenterSelector() {
            mChannelsShelfRowPresenter = new ChannelsShelfRowPresenter(false);
            mChannelsShelfRowPresenter.setShadowEnabled(true);
            mChannelsShelfRowPresenter.setSelectEffectEnabled(false);
            mChannelsShelfRowPresenter.setKeepChildForeground(true);

            mSourcesShelfRowPresenter = new SourcesShelfRowPresenter(false);
            mSourcesShelfRowPresenter.setShadowEnabled(true);
            mSourcesShelfRowPresenter.setSelectEffectEnabled(false);
            mSourcesShelfRowPresenter.setKeepChildForeground(true);

            mPresenters = new Presenter[]{mSourcesShelfRowPresenter, mChannelsShelfRowPresenter};
        }

        @Override
        public Presenter[] getPresenters() {
            return mPresenters;
        }

        @Override
        public Presenter getPresenter(Object item) {
            ShelfRow row = (ShelfRow) item;
            if (row.getId() == SOURCES_SHELF_ROW_ID) {
                return mSourcesShelfRowPresenter;
            } else {
                return mChannelsShelfRowPresenter;
            }
        }
    }
}
