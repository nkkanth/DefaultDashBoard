package org.droidtv.defaultdashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.os.UserHandle;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.OptionsMenu.IOptionsMenuCallBack;
import org.droidtv.defaultdashboard.ui.optionsframework.BackgroundImageColorFilterNode;
import org.droidtv.defaultdashboard.ui.optionsframework.HotelLogoThumbnailBrowserNode;
import org.droidtv.defaultdashboard.ui.optionsframework.ListOptionsNode;
import org.droidtv.defaultdashboard.ui.optionsframework.ListOptionsNode.ListOptionsNodeListener;
import org.droidtv.defaultdashboard.ui.optionsframework.MainBackgroundThumbnailBrowserNode;
import org.droidtv.defaultdashboard.ui.optionsframework.OptionsManager;
import org.droidtv.defaultdashboard.ui.optionsframework.OptionsManager.IOptionsNodeStateCallBack;
import org.droidtv.defaultdashboard.ui.optionsframework.OptionsManager.OptionsListener2;
import org.droidtv.defaultdashboard.ui.optionsframework.OptionsNode;
import org.droidtv.defaultdashboard.ui.optionsframework.SharingBackgroundThumbnailBrowserNode;
import org.droidtv.defaultdashboard.ui.optionsframework.ThumbnailBrowserNode;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialog;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogFooterButtonProp;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogInterface;


public class EditDashboardMenu implements DashboardDataManager.DashboardConfigurationResetListener {
    private OptionsManager mOptionsManager;
    private OptionsListener mOptionsListener;
    private ListOptionsNodeCallback mListOptionsNodeCallback;
    private DashboardDataManager mDashboardDataManager;
    private Context mContext;
    private MainBackgroundThumbnailBrowserNode mMainBackgroundThumbnailBrowserNode;
    private SharingBackgroundThumbnailBrowserNode mSharingBackgroundThumbnailBrowserNode;
    private HotelLogoThumbnailBrowserNode mHotelLogoThumbnailBrowserNode;
    private SidePanelBackgroundColorNode mSidePanelBackgroundColorNode;
    private SidePanelHighlightedTextColorNode mSidePanelHighlightedTextColorNode;
    private SidePanelNonHighlightedTextColorNode mSidePanelNonHighlightedTextColorNode;
    private BackgroundImageColorFilterNode mBackgroundImageColorFilterNode;
    private SharedPreferences mDefaultSharedPreferences;
    private SharedPreferences.Editor mDefaultSharedPreferencesEditor;
    private boolean mMainBackgroundEnabledDefaultValue;
    private boolean mShowAccountIconDefaultValue;
    private boolean mShowGoogleAssistantDefaultValue;
    private final boolean mIsMainBackgroundEnabledAtSessionStart;

    public EditDashboardMenu(Context context) {
        mContext = context;
        mOptionsManager = new OptionsManager(mContext);
        mDashboardDataManager = DashboardDataManager.getInstance();
        mDefaultSharedPreferences = mDashboardDataManager.getDefaultSharedPreferences();
        mDefaultSharedPreferencesEditor = mDefaultSharedPreferences.edit();
        mMainBackgroundEnabledDefaultValue = mContext.getResources().getBoolean(R.bool.enable_main_background);
        mIsMainBackgroundEnabledAtSessionStart = mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED,
                mMainBackgroundEnabledDefaultValue);
        mShowAccountIconDefaultValue = mContext.getResources().getBoolean(R.bool.enable_show_icon);
        mShowGoogleAssistantDefaultValue = mContext.getResources().getBoolean(R.bool.enable_google_assistant_icon);
        setupOptionsManager();
    }

    public OptionsManager getOptionsManager() {
        return mOptionsManager;
    }

    public void show() {
        mOptionsManager.show();
    }

    private void setupOptionsManager() {
        mOptionsManager.enableDiming(false);

        mOptionsListener = new OptionsListener();
        mOptionsManager.setOptionsListener(mOptionsListener);
        mOptionsManager.setOptionsMenuListner(mOptionsListener);
        mOptionsManager.setOptionsNodeListener(mOptionsListener);
        mOptionsManager.setOptionsLayout(R.layout.edit_dashboard_layout);

        mDashboardDataManager.addConfigurationResetListener(this);
        mListOptionsNodeCallback = new ListOptionsNodeCallback();

        ListOptionsNode showAccountIconSettingListNode = (ListOptionsNode) mOptionsManager.findOptionsNodeById(R.id.show_account_icon_setting);
        showAccountIconSettingListNode.setListListener(mListOptionsNodeCallback);

        ListOptionsNode showAssistantIconSettingListNode = (ListOptionsNode) mOptionsManager.findOptionsNodeById(R.id.show_assistant_icon_setting);
        showAssistantIconSettingListNode.setListListener(mListOptionsNodeCallback);

        ListOptionsNode mainBackgroundEnabledSettingListNode = (ListOptionsNode) mOptionsManager.findOptionsNodeById(R.id.user_interface_main_background_enabled_setting);
        mainBackgroundEnabledSettingListNode.setListListener(mListOptionsNodeCallback);

        mSidePanelBackgroundColorNode = (SidePanelBackgroundColorNode) mOptionsManager.findOptionsNodeById(R.id.side_panel_background_color);
        mSidePanelHighlightedTextColorNode = (SidePanelHighlightedTextColorNode) mOptionsManager.findOptionsNodeById(R.id.side_panel_highlighted_text_color);
        mSidePanelNonHighlightedTextColorNode = (SidePanelNonHighlightedTextColorNode) mOptionsManager.findOptionsNodeById(R.id.side_panel_non_highlighted_text_color);
        mBackgroundImageColorFilterNode = (BackgroundImageColorFilterNode) mOptionsManager.findOptionsNodeById(R.id.user_interface_main_background_color_filter);
    }

    class ListOptionsNodeCallback implements ListOptionsNodeListener {

        @Override
        public void onListItemSelected(int nodeResId, int dataId,
                                       AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onListNothingSelected(int nodeResId, AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onListItemLongClick(int nodeResId, int dataId,
                                           AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onListItemClick(int nodeResId, int dataId,
                                    AdapterView<?> parent, View view, int position, long id) {
            if (nodeResId == R.id.user_interface_main_background_enabled_setting) {
                boolean mainBackgroundEnabledOldValue = mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED,
                        mMainBackgroundEnabledDefaultValue);

                boolean enableMainBackground = dataId == 102;

                if (enableMainBackground != mainBackgroundEnabledOldValue) {
                    mDefaultSharedPreferencesEditor.putBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED, enableMainBackground);
                    mDefaultSharedPreferencesEditor.apply();

                    if (enableMainBackground) {
                        mDashboardDataManager.applyMainBackground();
                    } else {
                        mDashboardDataManager.clearBackground();
                    }
                }

                return;
            }

            if (nodeResId == R.id.show_account_icon_setting) {
                boolean showAccountIconOldValue = mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON,
                        mShowAccountIconDefaultValue);

                boolean showAccountIcon = dataId == 102;

                if (showAccountIcon != showAccountIconOldValue) {
                    mDefaultSharedPreferencesEditor.putBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON, showAccountIcon);
                    mDefaultSharedPreferencesEditor.apply();

                    mDashboardDataManager.showAccountIcon(showAccountIcon);
                }
                return;
            }

            if (nodeResId == R.id.show_assistant_icon_setting) {
                boolean showAssistantIconOldValue = mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_SHOW_ASSISTANT_ICON,
                        mShowGoogleAssistantDefaultValue);

                boolean showAssistantIcon = dataId == 102;

                if (showAssistantIcon != showAssistantIconOldValue) {
                    mDefaultSharedPreferencesEditor.putBoolean(Constants.PREF_KEY_SHOW_ASSISTANT_ICON, showAssistantIcon);
                    mDefaultSharedPreferencesEditor.apply();

                    mDashboardDataManager.showAssistantIcon(showAssistantIcon);
                }
                return;
            }

        }

        @Override
        public boolean isListItemAvailable(int nodeResId, int dataId) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean isListItemControllable(int nodeResId, int dataId) {
            return true;
        }

        @Override
        public int getItemSelectionWhenFocused(int nodeResId) {
            if (nodeResId == R.id.show_assistant_icon_setting) {
                return mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_SHOW_ASSISTANT_ICON, mShowGoogleAssistantDefaultValue)
                        ? 102 : 101;
            }
            if (nodeResId == R.id.show_account_icon_setting) {
                return mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON, mShowAccountIconDefaultValue)
                        ? 102 : 101;
            }

            if (nodeResId == R.id.user_interface_main_background_enabled_setting) {
                return mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED, mMainBackgroundEnabledDefaultValue)
                        ? 102 : 101;
            }

            return 0;
        }
    }

    class OptionsListener implements OptionsListener2, IOptionsMenuCallBack, IOptionsNodeStateCallBack {

        @Override
        public boolean canSelectNodeItem(int nodeResId) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public void onNodeItemSelected(int nodeResId) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean canMoveToNextNode(int nodeResId) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean getAvailabilityOfNode(int nodeResId) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean getControllablityOfNode(int nodeResId) {
            if (nodeResId == R.id.user_interface_main_background_image) {
                return mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_MAIN_BACKGROUND_ENABLED, mMainBackgroundEnabledDefaultValue);
            }
            if (nodeResId == R.id.revert_session_changes) {
                return hasConfigurationSessionChanges();
            }
            if (nodeResId == R.id.reset_settings) {
                return hasSavedConfiguration();
            }
            return true;
        }

        @Override
        public void onNodeItemClicked(int nodeResId) {
            switch (nodeResId) {
                case R.id.recommended_apps:
                    startRecommendedAppsActivity();
                    break;
                case R.id.app_recommendations:
                    startAppRecommendationsActivity();
                    break;
                case R.id.reset_settings:
                    showResetSettingsConfirmationDialog();
                    break;
                case R.id.revert_session_changes:
                    showRevertSessionChangesConfirmationDialog();
                    break;
            }
        }

        @Override
        public void onMovingBackwardFromNode(int nodeResId) {
            // TODO Auto-generated method stub
        }

        @Override
        public int getSelectionIndex() {
            return 0;
        }

        @Override
        public boolean dispatchUnhandledKeys(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
                mOptionsManager.hide();
                return true;
            }
            return false;
        }

        @Override
        public boolean onOptionsMenuDismissed() {
            if (mMainBackgroundThumbnailBrowserNode != null) {
                mMainBackgroundThumbnailBrowserNode.onDismissed();
            }
            if(mSharingBackgroundThumbnailBrowserNode != null) {
                mSharingBackgroundThumbnailBrowserNode.onDismissed();
            }
            if (mHotelLogoThumbnailBrowserNode != null) {
                mHotelLogoThumbnailBrowserNode.onDismissed();
            }
            if (mSidePanelBackgroundColorNode != null) {
                mSidePanelBackgroundColorNode.onDismissed();
            }
            if (mSidePanelHighlightedTextColorNode != null) {
                mSidePanelHighlightedTextColorNode.onDismissed();
            }
            if (mSidePanelNonHighlightedTextColorNode != null) {
                mSidePanelNonHighlightedTextColorNode.onDismissed();
            }
            if (mBackgroundImageColorFilterNode != null) {
                mBackgroundImageColorFilterNode.onDismissed();
            }

            mDashboardDataManager.removeConfigurationResetListener(EditDashboardMenu.this);

            return false;
        }

        @Override
        public void onOptionNodeEntered(OptionsNode node) {
            if ((node.getId() == R.id.user_interface_main_background_image)) {
                mMainBackgroundThumbnailBrowserNode = (MainBackgroundThumbnailBrowserNode) node;
                mMainBackgroundThumbnailBrowserNode.onNodeEntered();
            } else if (node.getId() == R.id.side_panel_hotel_logo) {
                mHotelLogoThumbnailBrowserNode = (HotelLogoThumbnailBrowserNode) node;
                mHotelLogoThumbnailBrowserNode.onNodeEntered();
            } else if (node.getId() == R.id.user_interface_side_panel) {
                mDashboardDataManager.showSidePanel();
            } else if(node.getId() == R.id.user_interface_cast_background_image){
                mSharingBackgroundThumbnailBrowserNode = (SharingBackgroundThumbnailBrowserNode) node;
                mSharingBackgroundThumbnailBrowserNode.onNodeEntered();
            }
        }

        @Override
        public void onOptionNodeExited(OptionsNode node) {
            if ((node.getId() == R.id.user_interface_main_background_image) ||
                    (node.getId() == R.id.side_panel_hotel_logo) ||
                    (node.getId() == R.id.user_interface_cast_background_image)) {
                ((ThumbnailBrowserNode) node).onNodeExited();
            } else if (node.getId() == R.id.user_interface_side_panel) {
                mDashboardDataManager.hideSidePanel();
            }
        }

        private void startRecommendedAppsActivity() {
            DdbLogUtility.logMoreChapter("EditDashboardMenu", "startRecommendedAppsActivity() called");
            Intent intent = new Intent(mContext, RecommendedAppsActivity.class);
            mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        }

        private void startAppRecommendationsActivity() {
            DdbLogUtility.logMoreChapter("EditDashboardMenu", "startAppRecommendationsActivity() called");
            Intent intent = new Intent(mContext, AppRecommendationsActivity.class);
            mContext.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
        }

        private void showResetSettingsConfirmationDialog() {
            final ModalDialog.Builder builder = new ModalDialog.Builder(mContext, ModalDialog.HEADING_TYPE_DEFAULT);
            builder.setHeading(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_CONFIGURE_DASHBOARD), "");
            builder.setMessage(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_RESET_SETTING));
            final ModalDialog modalDialog = builder.build();
            ModalDialogInterface.OnClickListener onCancelClickListener = new ModalDialogInterface.OnClickListener() {
                @Override
                public void onClick(ModalDialogInterface modalDialogInterface, int i) {
                    modalDialog.dismiss();
                }
            };
            ModalDialogInterface.OnClickListener onOkClickListener = new ModalDialogInterface.OnClickListener() {
                @Override
                public void onClick(ModalDialogInterface modalDialogInterface, int i) {
                    mDashboardDataManager.resetSettings();
                }
            };
            builder.setButton(ModalDialog.BUTTON_RIGHT, new ModalDialogFooterButtonProp(true, mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_THEME_OK), onOkClickListener));
            builder.setButton(ModalDialog.BUTTON_MID_RIGHT, new ModalDialogFooterButtonProp(true, mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_THEME_CANCEL), onCancelClickListener));
            modalDialog.show();
        }

        private void showRevertSessionChangesConfirmationDialog() {
            final ModalDialog.Builder builder = new ModalDialog.Builder(mContext, ModalDialog.HEADING_TYPE_DEFAULT);
            builder.setHeading(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_CONFIGURE_DASHBOARD), "");
            builder.setMessage(mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_MAIN_CANCEL_SETTING));
            final ModalDialog modalDialog = builder.build();
            ModalDialogInterface.OnClickListener onCancelClickListener = new ModalDialogInterface.OnClickListener() {
                @Override
                public void onClick(ModalDialogInterface modalDialogInterface, int i) {
                    modalDialog.dismiss();
                }
            };
            ModalDialogInterface.OnClickListener onOkClickListener = new ModalDialogInterface.OnClickListener() {
                @Override
                public void onClick(ModalDialogInterface modalDialogInterface, int i) {
                    mDashboardDataManager.revertSessionChanges();
                }
            };
            builder.setButton(ModalDialog.BUTTON_RIGHT, new ModalDialogFooterButtonProp(true, mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_THEME_OK), onOkClickListener));
            builder.setButton(ModalDialog.BUTTON_MID_RIGHT, new ModalDialogFooterButtonProp(true, mContext.getString(org.droidtv.ui.htvstrings.R.string.HTV_THEME_CANCEL), onCancelClickListener));
            modalDialog.show();
        }
    }

    @Override
    public void revertSessionChanges() {
        if (mSidePanelBackgroundColorNode != null) {
            mSidePanelBackgroundColorNode.setCurrentColor(mDashboardDataManager.getSidePanelBackgroundColor());
        }

        if (mSidePanelNonHighlightedTextColorNode != null) {
            mSidePanelNonHighlightedTextColorNode.setCurrentColor(mDashboardDataManager.getSidePanelNonHighlightedTextColor());
        }

        if (mSidePanelHighlightedTextColorNode != null) {
            mSidePanelHighlightedTextColorNode.setCurrentColor(mDashboardDataManager.getSidePanelHighlightedTextColor());
        }

        if (mBackgroundImageColorFilterNode != null) {
            mBackgroundImageColorFilterNode.setCurrentColor(mDashboardDataManager.getMainBackgroundColorFilter());
        }

        if (mMainBackgroundThumbnailBrowserNode != null) {
            mMainBackgroundThumbnailBrowserNode.resetConfigurationSessionChanges();
        }

        if (mSharingBackgroundThumbnailBrowserNode != null) {
            mSharingBackgroundThumbnailBrowserNode.resetConfigurationSessionChanges();
        }

        if (mHotelLogoThumbnailBrowserNode != null) {
            mHotelLogoThumbnailBrowserNode.resetConfigurationSessionChanges();
        }
    }

    @Override
    public void resetSettings() {
        // do nothing as all settings are already reset by now and updates have been performed by revertSessionChanges() from DashboardDataManager.
    }

    private boolean hasConfigurationSessionChanges() {
        if (mDashboardDataManager.getSidePanelBackgroundColor() != mSidePanelBackgroundColorNode.getCurrentColor()) {
            return true;
        }
        if (mDashboardDataManager.getSidePanelHighlightedTextColor() != mSidePanelHighlightedTextColorNode.getCurrentColor()) {
            return true;
        }
        if (mDashboardDataManager.getSidePanelNonHighlightedTextColor() != mSidePanelNonHighlightedTextColorNode.getCurrentColor()) {
            return true;
        }
        if (mDashboardDataManager.getMainBackgroundColorFilter() != mBackgroundImageColorFilterNode.getCurrentColor()) {
            return true;
        }
        if (mDashboardDataManager.isMainBackgroundEnabled() != mIsMainBackgroundEnabledAtSessionStart) {
            return true;
        }
        if (mMainBackgroundThumbnailBrowserNode != null && mMainBackgroundThumbnailBrowserNode.hasConfigurationSessionChanges()) {
            return true;
        }
        if (mSharingBackgroundThumbnailBrowserNode != null && mSharingBackgroundThumbnailBrowserNode.hasConfigurationSessionChanges()) {
            return true;
        }
        if (mHotelLogoThumbnailBrowserNode != null && mHotelLogoThumbnailBrowserNode.hasConfigurationSessionChanges()) {
            return true;
        }

        return false;
    }

    private boolean hasSavedConfiguration() {
        return hasConfigurationSessionChanges() || mDashboardDataManager.hasSavedConfiguration();
    }
}