
package org.droidtv.defaultdashboard.data.model;

import android.accounts.AccountManager;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.UserHandle;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.droidtv.defaultdashboard.DashboardActivity;
import org.droidtv.defaultdashboard.R;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AccountChangeListener;
import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.GoogleAccountFlowListener;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.ui.view.TopMenuItemManager;
import org.droidtv.defaultdashboard.util.Constants;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialog;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogFooterButtonProp;
import org.droidtv.ui.tvwidget2k15.dialog.ModalDialogInterface;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.TitleViewAdapter;

import static org.droidtv.defaultdashboard.util.Constants.ACCOUNT_TYPE_GMAIL;
import static org.droidtv.defaultdashboard.util.Constants.ACCOUNT_TYPE_GOOGLE;
import static org.droidtv.defaultdashboard.util.Constants.EXTRA_MENU_MODE;
import static org.droidtv.defaultdashboard.util.Constants.EXTRA_SHOW_ACCOUNTS;
import static org.droidtv.defaultdashboard.util.Constants.GUEST_ACCOUNT_PROFILE_ON;
import static org.droidtv.defaultdashboard.util.Constants.PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION;
import static org.droidtv.defaultdashboard.util.Constants.RC_SIGN_IN;
import static org.droidtv.defaultdashboard.util.Constants.USER_ACCOUNT_ANDROID_INTENT_CLASS_NAME;
import static org.droidtv.defaultdashboard.util.Constants.USER_ACCOUNT_ANDROID_INTENT_PACKAGE_NAME;

/**
 * Created by sandeep.kumar on 30/10/2017.
 */

public class UserAccountMenuItem extends TopMenuItem implements View.OnClickListener, View.OnFocusChangeListener, GoogleAccountFlowListener, AccountChangeListener, DashboardDataManager.AccountIconListener {
    private Action mAction;
    private View mView;
    private TextView mLabel;
    private ImageView mIcon;
    private float mDefaultScale;
    private float mFocusScale;
    private float mDefaultElevation;
    private float mFocusElevation;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mGoogleAccountProfileImageText;
    private GoogleAccount mGoogleAccount = new GoogleAccount();
    private ModalDialog mDialog;
    private ModalDialogFooterButtonProp mAcceptButton;
    private SharedPreferences mDefaultSharedPreferences;
    private DashboardDataManager mDashboardDataManager;
    private ITvSettingsManager mTvSettingsManager;
    private boolean mShowAccountIconDefaultValue;
    private boolean mPhotoAvailable;
    private StringBuilder mDisplayName;
    private String mPersonGivenName;
    private String mPersonFamilyName;
    private TopMenuItemManager.TopMenuItemFocusChangeListener mTopMenuItemFocusChangeListner;
    private Handler mUiHandler = new Handler();
    private Context mContext = null;
    

    public UserAccountMenuItem(Context context , TopMenuItemManager.TopMenuItemFocusChangeListener listener) {
        mContext = context;
        init(context);
        mTopMenuItemFocusChangeListner = listener;
        mDashboardDataManager = DashboardDataManager.getInstance();
        mDashboardDataManager.setGoogleAccountFlowListener(this);
        mDashboardDataManager.addAccountChangeListener(this);
        mDashboardDataManager.setAccountIconListener(this);
        mDefaultSharedPreferences = mDashboardDataManager.getDefaultSharedPreferences();
        mTvSettingsManager = mDashboardDataManager.getTvSettingsManager();
        checkAndDoGoogleSignIn();
    }

    private void checkAndDoGoogleSignIn(){
        if(mDashboardDataManager.getGoogleSignInAfter2Min()) {
            mUiHandler.removeCallbacks(mGoogleSignInRunnable);
            mUiHandler.postDelayed(mGoogleSignInRunnable, Constants.GOOGLE_SIGN_IN_DELAY);
        }else{
            mUiHandler.removeCallbacks(mGoogleSignInInitRunnable);
            mUiHandler.post(mGoogleSignInInitRunnable);
        }
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext.getApplicationContext(), gso);
        mShowAccountIconDefaultValue = mContext.getResources().getBoolean(R.bool.enable_show_icon);
        mPhotoAvailable = false;
    }

    private void doGoogleSignInAfter2Min(){
        mUiHandler.removeCallbacks(mGoogleSignInRunnable);
        mUiHandler.postDelayed(mGoogleSignInRunnable, Constants.GOOGLE_SIGN_IN_DELAY);
    };
    
    Runnable mGoogleSignInRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("UserAccountMenuItem", "mGoogleSignInRunnable: after2min");
            doGoogleSignIn();
        }
    };
	
    Runnable mGoogleSignInInitRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("UserAccountMenuItem", "mGoogleSignInInitRunnable ");
            initGoogleSignIn();
        }
    };

    private void doGoogleSignIn(){
        initGoogleSignIn();
        if(!googleSignInCanBeProcessed()) {
            return;
        }
        if (DashboardDataManager.getInstance().getGoogleAccountCount() > 0 &&
                (mDefaultSharedPreferences.getBoolean(PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION, false))) {
            googleAccountSignOn();
        }
        DashboardDataManager.getInstance().setGoogleSignInAfter2Min(false);
    }

    private boolean googleSignInCanBeProcessed() {
        if(mContext == null ){
            Log.d(UserAccountMenuItem.class.getSimpleName(), "googleSignInCanBeProcessed: context null");
            return false;
        }
        if(mView == null || (!mView.isAttachedToWindow())){
            Log.d(UserAccountMenuItem.class.getSimpleName(), "googleSignInCanBeProcessed: mView not added");
            return false;
        }
        return true;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public Action getAction() {
        return mAction;
    }

    @Override
    public void onClick(View v) {
        if (DashboardDataManager.getInstance().getGoogleAccountCount() > 0) {
            Intent androidSettingsIntent = new Intent();
            androidSettingsIntent.setClassName(USER_ACCOUNT_ANDROID_INTENT_PACKAGE_NAME, USER_ACCOUNT_ANDROID_INTENT_CLASS_NAME);
            androidSettingsIntent.putExtra(EXTRA_MENU_MODE, 1);
            androidSettingsIntent.putExtra(EXTRA_SHOW_ACCOUNTS, true);
            getView().getContext().startActivityAsUser(androidSettingsIntent, UserHandle.CURRENT_OR_SELF);
        } else {
            Intent addAccountIntent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            addAccountIntent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE_GOOGLE);
            getView().getContext().startActivityAsUser(addAccountIntent, UserHandle.CURRENT_OR_SELF);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            scaleAndElevate(mIcon, mFocusScale, mFocusElevation);
            scaleAndElevate(mGoogleAccountProfileImageText, mFocusScale, mFocusElevation);
            mLabel.setVisibility(View.VISIBLE);
            mLabel.animate().setDuration(150).alpha(1);
            mIcon.setBackground(view.getContext().getDrawable(R.drawable.top_menu_background_rounded));
            if (mDisplayName!=null && !mDisplayName.toString().equals("")&&!mPhotoAvailable){
                mIcon.setImageDrawable(getView().getContext().getDrawable(R.drawable.google_account_icon_text_background_highlight));
            }
            mIcon.setVisibility(View.VISIBLE);
        } else {
            scaleAndElevate(mIcon, mDefaultScale, mDefaultElevation);
            scaleAndElevate(mGoogleAccountProfileImageText, mDefaultScale, mDefaultElevation);
            mLabel.animate().setDuration(150).alpha(0);
            mLabel.setVisibility(View.INVISIBLE);
            mIcon.setBackgroundColor(view.getContext().getColor(android.R.color.transparent));
            if (mDisplayName!=null && !mDisplayName.toString().equals("")&&!mPhotoAvailable){
                mIcon.setImageDrawable(getView().getContext().getDrawable(R.drawable.google_account_icon_text_background_normal));
            }

        }
        if(mTopMenuItemFocusChangeListner != null) {
            mTopMenuItemFocusChangeListner.OnItemFocus(view.getId() ,hasFocus);
        }
    }

    @Override
    public void startGoogleAccountFlow() {
        googleAccountSignOn();
    }

    private void googleAccountSignOn(){
        if (!mDefaultSharedPreferences.getBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON, mShowAccountIconDefaultValue)) {
            showAccountIcon(false);
            return;
        }
        // Is guest profile download enabled from PBS settings ?
        boolean guestProfileDownloadEnabled = mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_GUEST_PROFLE_DOWNLOAD, 0, 0)
                == GUEST_ACCOUNT_PROFILE_ON;
        if (!guestProfileDownloadEnabled) {
            mIcon.setImageResource(R.drawable.ic_google_account);
            return;
        } else {
            boolean profileAccessPermissionPrefExists = mDefaultSharedPreferences.contains(PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION);
            boolean isProfileAccessPermissionGranted = mDefaultSharedPreferences.getBoolean(PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION, false);
            int googleAccountCount = mDashboardDataManager.getGoogleAccountCount();

            if (googleAccountCount > 0) {
                if (!profileAccessPermissionPrefExists) {
                    displayDefaultLogo();
                    displayProfileInfoDialog();
                } else if (isProfileAccessPermissionGranted) {
                    GoogleSignInAccount account = DashboardDataManager.getInstance().getLastSignedInGoogleAccount();
                    if (null != account) {
                        updateUI(account);
                    } else if (mDashboardDataManager.getGoogleEmailId().contains(ACCOUNT_TYPE_GMAIL)) {
                        String displayLetter = String.valueOf(mDashboardDataManager.getGoogleEmailId().charAt(0));
                        mGoogleAccountProfileImageText.setText(displayLetter.toString().toUpperCase());
                        mGoogleAccountProfileImageText.setVisibility(View.VISIBLE);
                        mIcon.setImageDrawable(getView().getContext().getDrawable(R.drawable.google_account_icon_text_background_normal));
                    }
                }
            }
            DashboardDataManager.getInstance().notifyAccountChanged();
        }
    }

    @Override
    public void onAccountChanged() {
        if (DashboardDataManager.getInstance().getGoogleAccountCount() <= 0) {
            if(mDisplayName!= null){
                mDisplayName.setLength(0);
            }
            accountSignOut();
            revokeAccess();
        }
    }

    private void init(Context context) {
        initValues(context);
        createView(context);

        createAction(context);
    }

    private void initValues(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(R.dimen.default_scale, typedValue, true);
        mDefaultScale = typedValue.getFloat();
        context.getResources().getValue(R.dimen.top_menu_item_focus_scale, typedValue, true);
        mFocusScale = typedValue.getFloat();
        mDefaultElevation = context.getResources().getDimension(R.dimen.default_elevation);
        mFocusElevation = context.getResources().getDimension(R.dimen.focus_elevation);
    }

    private void createView(Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.top_menu_item_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout rootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.top_menu_item, null);
        mIcon = (ImageView) rootView.findViewById(R.id.top_menu_user_icon);
        rootView.setId(R.id.top_menu_user_view);
        mGoogleAccountProfileImageText = (TextView) rootView.findViewById(R.id.top_menu_google_icon_text);
        mLabel = (TextView) rootView.findViewById(R.id.top_menu_user_label);
        FrameLayout.LayoutParams labelLayoutParams = (FrameLayout.LayoutParams) mLabel.getLayoutParams();
        labelLayoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_selected_label_margin_top), 0, 0);
        mLabel.setText(context.getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_ITEM_MANAGE_ACCOUNT));
        mLabel.setTextSize(context.getResources().getDimension(R.dimen.top_menu_selected_text_size));
        mLabel.setSelected(true);
        mGoogleAccountProfileImageText.setTextSize(context.getResources().getDimension(R.dimen.top_menu_google_account_selected_text_size));
        mGoogleAccountProfileImageText.setSelected(true);
        layoutParams.setMargins(0, (int) context.getResources().getDimension(R.dimen.top_menu_item_margin_top), 0, 0);
        layoutParams.setMarginEnd((int) context.getResources().getDimension(R.dimen.top_menu_item_margin_end));
        rootView.setLayoutParams(layoutParams);
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        int padding = (int) context.getResources().getDimension(R.dimen.top_menu_item_padding);
        mIcon.setPadding(padding, padding, padding, padding);
        mGoogleAccountProfileImageText.setPadding(padding, padding, padding, padding);
        mIcon.setImageResource(R.drawable.ic_google_account);
        if(DashboardDataManager.getInstance().getDefaultSharedPreferences().getBoolean(Constants.PREF_KEY_SHOW_ACCOUNT_ICON, true)){
            rootView.setVisibility(View.VISIBLE);
        }else{
            rootView.setVisibility(View.GONE);
        }

        rootView.setOnClickListener(this);
        rootView.setOnFocusChangeListener(this);
        mView = rootView;
        mView.addOnAttachStateChangeListener(onAttachStateChangeListener);
    }

    public View.OnAttachStateChangeListener onAttachStateChangeListener = new View.OnAttachStateChangeListener(){

        @Override
        public void onViewAttachedToWindow(View view) {
        }

        @Override
        public void onViewDetachedFromWindow(View view) {
            mUiHandler.removeCallbacks(mGoogleSignInRunnable);
            mUiHandler.removeCallbacks(mGoogleSignInInitRunnable);
        }
    };

    private void createAction(Context context) {
        mAction = new UserAccountMenuItemAction(context);
    }

    private void scaleAndElevate(View view, float scale, float elevation) {
        view.animate().setDuration(150).scaleX(scale);
        view.animate().setDuration(150).scaleY(scale);
        ObjectAnimator.ofFloat(view, "elevation", view.getElevation(), elevation).setDuration(150).start();
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            String personGivenName = account.getGivenName();
            mGoogleAccount.setGivenName(personGivenName);
            String personFamilyName = account.getFamilyName();
            mGoogleAccount.setFamilyName(personFamilyName);
            mDisplayName = new StringBuilder();
            if (account.getPhotoUrl() != null) {
                String personPhotoUrl = account.getPhotoUrl().toString();
                DashboardDataManager.getInstance().fetchGoogleAccountImageBitmap(personPhotoUrl, mGoogleAccountImageListener);
            } else {
                if (null != personGivenName && null != personFamilyName) {
                    if (personFamilyName.equalsIgnoreCase("null") && personGivenName.equalsIgnoreCase("null")) {
                        mDisplayName.append(account.getEmail().charAt(0));
                    } else {
                        mDisplayName.append(personGivenName.charAt(0)).append(personFamilyName.charAt(0));
                    }
                    displayProfileImageText();
                } else {
                    mDisplayName.append(account.getEmail().charAt(0));
                    displayProfileImageText();
                }
            }
        } else {
            displayDefaultLogo();
        }
    }

    private void displayProfileImageText() {
        mGoogleAccountProfileImageText.setText(mDisplayName.toString().toUpperCase());
        mGoogleAccountProfileImageText.setVisibility(View.VISIBLE);
        mIcon.setImageDrawable(getView().getContext().getDrawable(R.drawable.google_account_icon_text_background_normal));
    }

    private DashboardDataManager.GoogleAccountImageListener mGoogleAccountImageListener = new DashboardDataManager.GoogleAccountImageListener() {

        @Override
        public void onGoogleAccountImageFetched(Bitmap image) {
            if (null != image) {
                mGoogleAccount.setPicture(image);
                RoundedBitmapDrawable roundedGoogleAccountDrawable =
                        RoundedBitmapDrawableFactory.create(getView().getContext().getResources(), mGoogleAccount.getPicture());
                roundedGoogleAccountDrawable.setCornerRadius(getView().getContext().getResources().getDimension(R.dimen.user_account_image_corner_radius));
                mGoogleAccountProfileImageText.setVisibility(View.GONE);
                mIcon.setImageDrawable(roundedGoogleAccountDrawable);
                mPhotoAvailable = true;
            } else {
                mPhotoAvailable = false;
                mPersonGivenName = mGoogleAccount.getGivenName();
                mPersonFamilyName = mGoogleAccount.getFamilyName();
                mDisplayName = new StringBuilder();
                if (mPersonGivenName.equalsIgnoreCase("null") && (mPersonFamilyName.equalsIgnoreCase("null"))) {
                    mDisplayName.append((mGoogleAccount.getId().charAt(0)));
                } else {
                    mDisplayName.append(mPersonGivenName.charAt(0)).append(mPersonFamilyName.charAt(0));
                }

                mGoogleAccountProfileImageText.setText(mDisplayName.toString().toUpperCase());
                mGoogleAccountProfileImageText.setVisibility(View.VISIBLE);
                mIcon.setImageDrawable(getView().getContext().getDrawable(R.drawable.google_account_icon_text_background_normal));
            }
            DdbLogUtility.logTopMenu("onGoogleAccountImageFetched called : mPhotoAvailable", "" + mPhotoAvailable);
        }
    };

    private void accountSignOut() {
        if (null != mGoogleSignInClient) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                DashboardDataManager.getInstance().resetGoogleAccountPreferences();
                                updateUI(null);
                            }
                        }
                    });
        } else {
            updateUI(null);
        }
    }

    private void revokeAccess() {
        if (null != mGoogleSignInClient) {
            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateUI(null);
                            }
                        }
                    });
        }
    }

    private void displayProfileInfoDialog() {
        if (mDialog == null) {
            initProfileInfoDialog();
            mDialog.show();
            mAcceptButton.requestFocus();
        } else if (!mDialog.isShowing()) {
            mDialog.show();
            mAcceptButton.requestFocus();
        }
    }


    private void initProfileInfoDialog() {
        final ModalDialog.Builder builder = new ModalDialog.Builder(getView().getContext(), ModalDialog.HEADING_TYPE_NO_SUB_TITLE);

        builder.setButton(ModalDialog.BUTTON_LEFT,
                getView().getContext().getResources().getString(org.droidtv.ui.strings.R.string.MAIN_BUTTON_DECLINE), true,
                new ModalDialogInterface.OnClickListener() {

                    @Override
                    public void onClick(ModalDialogInterface modalDialogInterface, int which) {
                        mDefaultSharedPreferences.edit().putBoolean(PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION, false).apply();
                        mDialog.dismiss();
                        return;
                    }
                });

        mAcceptButton = builder.setButton(ModalDialog.BUTTON_RIGHT,
                getView().getContext().getResources().getString(org.droidtv.ui.strings.R.string.MAIN_BUTTON_ACCEPT), true,
                new ModalDialogInterface.OnClickListener() {

                    @Override
                    public void onClick(ModalDialogInterface modalDialogInterface, int which) {
                        mDefaultSharedPreferences.edit().putBoolean(PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION, true).apply();
                        mDialog.dismiss();
                        startGoogleSignInIntent();
                    }
                });

        builder.setHeading(getView().getContext().getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_PROFILE_INFO), null);
        builder.setMessage(getView().getContext().getResources().getString(org.droidtv.ui.htvstrings.R.string.HTV_DDB_PROFILE_ACCESS_PERMISSION));
        mDialog = builder.build(ModalDialog.MODAL_DIALOG_TYPE_LARGE);
    }

    private void startGoogleSignInIntent() {
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        ((DashboardActivity) getView().getContext()).startActivityForResult(googleSignInIntent, RC_SIGN_IN);
    }

    @Override
    public void showAccountIcon(boolean show) {
        if (show) {
            getView().setVisibility(View.VISIBLE);
            startGoogleAccountFlow();
        } else {
            getView().setVisibility(View.GONE);
        }
    }

    private void displayDefaultLogo() {
        mPhotoAvailable = false;
        mIcon.setImageResource(R.drawable.ic_google_account);
        mGoogleAccountProfileImageText.setVisibility(View.GONE);
        mIcon.setVisibility(View.VISIBLE);
        DdbLogUtility.logTopMenu("displayDefaultLogo called : mPhotoAvailable", "" + mPhotoAvailable);
    }

    private class UserAccountMenuItemAction implements Action {

        private Context mContext;

        private UserAccountMenuItemAction(Context context) {
            mContext = context;
        }

        @Override
        public void perform() {
        }
    }
}
