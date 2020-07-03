package org.droidtv.defaultdashboard.data.manager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.droidtv.defaultdashboard.data.manager.DashboardDataManager.AccountDataListener;
import org.droidtv.defaultdashboard.data.model.ContextualObject;
import org.droidtv.defaultdashboard.log.DdbLogUtility;
import org.droidtv.defaultdashboard.util.ThreadPoolExecutorWrapper;
import org.droidtv.tv.persistentstorage.ITvSettingsManager;
import org.droidtv.tv.persistentstorage.TvSettingsConstants;
import org.droidtv.tv.persistentstorage.TvSettingsDefinitions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import static org.droidtv.defaultdashboard.util.Constants.ACCOUNT_HTTP_ERROR_TAG;
import static org.droidtv.defaultdashboard.util.Constants.PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION;


/**
 * Created by sandeep.kumar on 30/10/2017.
 */

final class AccountDataManager extends ContextualObject {

    private static String TAG = "AccountDataManager";

    private ThreadPoolExecutor mThreadPoolExecutor;
    private UiThreadHandler mUiThreadHandler;
    private ITvSettingsManager mTvSettingsManager;
    private ArrayList<WeakReference<DashboardDataManager.AccountChangeListener>> mAccountChangeListenerRefs;
    private ArrayList<WeakReference<DashboardDataManager.GuestNameChangeListener>> mGuestNameChangeListenerRefs;

    AccountDataManager(Context context) {
        super(context);
        mThreadPoolExecutor = ThreadPoolExecutorWrapper.getInstance().getThreadPoolExecutor(ThreadPoolExecutorWrapper.ACCOUNT_THREAD_THREAD_POOL_EXECUTOR);
        mUiThreadHandler = new UiThreadHandler();
        mTvSettingsManager = DashboardDataManager.getInstance().getTvSettingsManager();
        mAccountChangeListenerRefs = new ArrayList<>();
        mGuestNameChangeListenerRefs = new ArrayList<>();
    }

    void fetchAccounts(AccountDataListener listener) {
        AccountsFetchCallable callable = new AccountsFetchCallable(getContext());
        AccountsFetchTask task = new AccountsFetchTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    int getGoogleAccountCount() {
        AccountManager mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        return accounts.length;
    }

    String getGoogleEmailId() {
        String googleEmail = "";
        AccountManager mAccountManager = AccountManager.get(getContext());
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts.length > 0) {
            googleEmail = accounts[0].name;
        } else {
            googleEmail = "";
        }
        DdbLogUtility.logCommon(TAG, "getGoogleEmailId " + googleEmail);
        return googleEmail;
    }

    String getGuestName() {
        String guestName = getGuestNameFromPms();

        boolean googleProfileAccessAllowed = DashboardDataManager.getInstance().
                getDefaultSharedPreferences().getBoolean(PREF_KEY_ACCOUNT_ACCESS_GRANT_PERMISSION, false);
        DdbLogUtility.logCommon(TAG, "getGuestName() googleProfileAccessAllowed " + googleProfileAccessAllowed);
        if (!googleProfileAccessAllowed) {
            return guestName;
        }

        GoogleSignInAccount account = getLastSignedInGoogleAccount();
        if (getGoogleAccountCount() > 0 && null != account) {
            String googleProfileName = account.getDisplayName();
            if (!TextUtils.isEmpty(googleProfileName)) {
                guestName = googleProfileName;
            } else {
                guestName = DashboardDataManager.getInstance().getGoogleEmailId();
            }
        } else if (getGoogleAccountCount() > 0) {
            guestName = DashboardDataManager.getInstance().getGoogleEmailId();
        }
        DdbLogUtility.logCommon(TAG, "getGuestName() " + guestName);
        return guestName;
    }

    String getGuestNameFromPms() {
        String guestName = "";
        if ((mTvSettingsManager.getInt(TvSettingsConstants.PBSMGR_PROPERTY_FEATURE_PMS, 0, 0) == TvSettingsDefinitions.PbsPmsConstants.PBSMGR_ON)) {
            guestName = mTvSettingsManager.getString(TvSettingsConstants.PBSMGR_PROPERTY_PMS_DISPLAYNAME, 0, null);
            if (guestName == null) {
                guestName = "";
            }
        }
        DdbLogUtility.logCommon(TAG, "getGuestNameFromPms() guestName " + guestName);
        return guestName;
    }


    GoogleSignInAccount getLastSignedInGoogleAccount() {
        try {
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext().getApplicationContext());
            return googleSignInAccount;
        } catch (Exception e) {
           Log.e(TAG,"Exception :" +e.getMessage());
            return null;
        }
    }

    public void fetchGoogleAccountImageBitmap(String personPhotoUrl, DashboardDataManager.GoogleAccountImageListener listener) {
        GoogleAccountImageFetchCallable callable = new GoogleAccountImageFetchCallable(personPhotoUrl);
        GoogleAccountImageFetchTask task = new GoogleAccountImageFetchTask(callable, mUiThreadHandler, listener);
        mThreadPoolExecutor.execute(task);
    }

    public Bitmap fetchGoogleAccountImageBitmap(String personPhotoUrl) {
        Bitmap bitmap;
        InputStream inputStream = null;
        try {
            inputStream = getHttpInputStreamConnection(personPhotoUrl);
            bitmap = BitmapFactory.decodeStream(inputStream, null, null);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Log.w(TAG, ACCOUNT_HTTP_ERROR_TAG + e.getMessage());
            }
        }
        return bitmap;

    }

    private InputStream getHttpInputStreamConnection(String personPhotoUrl) {

        InputStream inputStream = null;
        URL url = null;
        URLConnection connection = null;

        try {
            url = new URL(personPhotoUrl);
        } catch (MalformedURLException e) {
            Log.w(TAG, ACCOUNT_HTTP_ERROR_TAG + "MalformedURLException");
        }

        try {
            if(null != url){
                connection = url.openConnection();
            }
        } catch (IOException e) {
            Log.w(TAG, ACCOUNT_HTTP_ERROR_TAG + e.getMessage());
        }

        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            if (httpURLConnection != null) {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();

                }
            }
        } catch (Exception e) {
            Log.w(TAG, ACCOUNT_HTTP_ERROR_TAG + e.getMessage());
        }
        return inputStream;
    }

    private static final class AccountsFetchCallable implements Callable<List<Account>> {

        private Context mContext;

        private AccountsFetchCallable(Context context) {
            mContext = context;
        }

        @Override
        public List<Account> call() throws Exception {
            AccountManager accountManager = (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);
            return Arrays.asList(accountManager.getAccounts());
        }
    }

    private static final class AccountsFetchTask extends FutureTask<List<Account>> {

        private Handler mHandler;
        private AccountDataListener mAccountDataListener;

        private AccountsFetchTask(AccountsFetchCallable callable, Handler handler, AccountDataListener accountDataListener) {
            super(callable);
            mHandler = handler;
            mAccountDataListener = accountDataListener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    List<Account> accounts = get();
                    AccountsFetchResult result = new AccountsFetchResult(accounts, mAccountDataListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_ACCOUNTS_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "AccountsFetchTask failed.reason:" + e.getMessage());
               Log.e(TAG,"Exception :" +e.getMessage());
            }
        }
    }

    private static final class AccountsFetchResult {
        AccountDataListener mAccountDataListener;
        List<Account> mAccounts;

        private AccountsFetchResult(List<Account> accounts, AccountDataListener listener) {
            mAccounts = accounts;
            mAccountDataListener = listener;
        }
    }

    private static final class GoogleAccountImageResult {
        Bitmap mGoogleImage;
        DashboardDataManager.GoogleAccountImageListener mGoogleAccountImageListener;

        public GoogleAccountImageResult(Bitmap googleImage, DashboardDataManager.GoogleAccountImageListener googleAccountImageListener) {
            this.mGoogleImage = googleImage;
            this.mGoogleAccountImageListener = googleAccountImageListener;
        }
    }

    private static final class UiThreadHandler extends Handler {

        private static final int MSG_WHAT_ACCOUNTS_FETCH_COMPLETE = 100;
        private static final int MSG_WHAT_ACCOUNT_IMAGE_FETCH_COMPLETE = 101;


        private UiThreadHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            DdbLogUtility.logCommon(TAG, "handleMessage what " + what);
            if (what == MSG_WHAT_ACCOUNTS_FETCH_COMPLETE) {
                AccountsFetchResult result = (AccountsFetchResult) msg.obj;
                if (result.mAccountDataListener != null) {
                    result.mAccountDataListener.onAccountsFetched(result.mAccounts);
                }
                return;
            }
            if (what == MSG_WHAT_ACCOUNT_IMAGE_FETCH_COMPLETE) {
                GoogleAccountImageResult result = (GoogleAccountImageResult) msg.obj;
                if (result.mGoogleAccountImageListener != null) {
                    result.mGoogleAccountImageListener.onGoogleAccountImageFetched(result.mGoogleImage);
                }
                return;
            }
        }
    }

    private class GoogleAccountImageFetchCallable implements Callable<Bitmap> {
        private String mImageUrl;

        public GoogleAccountImageFetchCallable(String imageUrl) {
            mImageUrl = imageUrl;
        }

        @Override
        public Bitmap call() throws Exception {
            return fetchGoogleAccountImageBitmap(mImageUrl);
        }
    }

    private class GoogleAccountImageFetchTask extends FutureTask<Bitmap> {
        private Handler mHandler;
        private DashboardDataManager.GoogleAccountImageListener mGoogleAccountImageListener;

        public GoogleAccountImageFetchTask(GoogleAccountImageFetchCallable callable, Handler handler, DashboardDataManager.GoogleAccountImageListener listener) {
            super(callable);
            mHandler = handler;
            mGoogleAccountImageListener = listener;
        }

        @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    Bitmap image = get();
                    GoogleAccountImageResult result = new GoogleAccountImageResult(image, mGoogleAccountImageListener);
                    Message message = Message.obtain(mHandler, UiThreadHandler.MSG_WHAT_ACCOUNT_IMAGE_FETCH_COMPLETE, result);
                    message.sendToTarget();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, "AccountProfileImageFetchTask failed.reason:" + e.getMessage());
            }
        }
    }

    void addAccountChangeListener(DashboardDataManager.AccountChangeListener accountChangeListener) {
        if (accountChangeListener == null) {
            return;
        }
        for (int i = 0; i < mAccountChangeListenerRefs.size(); i++) {
            WeakReference<DashboardDataManager.AccountChangeListener> ref = mAccountChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardDataManager.AccountChangeListener listener = ref.get();
            if (listener != null && listener.equals(accountChangeListener)) {
                return;
            }
        }
        mAccountChangeListenerRefs.add(new WeakReference<DashboardDataManager.AccountChangeListener>(accountChangeListener));
    }

    void removeAccountChangeListener(DashboardDataManager.AccountChangeListener accountChangeListener) {
        if (mAccountChangeListenerRefs == null) {
            return;
        }
        if (mAccountChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mAccountChangeListenerRefs.size(); i++) {
                WeakReference<DashboardDataManager.AccountChangeListener> ref = mAccountChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                DashboardDataManager.AccountChangeListener listener = ref.get();
                if (listener != null && listener.equals(accountChangeListener)) {
                    mAccountChangeListenerRefs.remove(ref);
                }
            }
        }
    }

    void notifyAccountChanged() {
        DdbLogUtility.logCommon(TAG, "notifyAccountChanged() called");
        if (mAccountChangeListenerRefs == null) {
            return;
        }
        if (mAccountChangeListenerRefs.size() != 0) {
            for (int i = 0; i < mAccountChangeListenerRefs.size(); i++) {
                WeakReference<DashboardDataManager.AccountChangeListener> ref = mAccountChangeListenerRefs.get(i);
                if (ref == null) {
                    continue;
                }
                DashboardDataManager.AccountChangeListener listener = ref.get();
                if (listener != null) {
                    listener.onAccountChanged();
                }
            }
        }
    }

    void notifyGuestNameChanged() {
        DdbLogUtility.logCommon(TAG, "notifyGuestNameChanged() called");
        if (mGuestNameChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mGuestNameChangeListenerRefs.size(); i++) {
            WeakReference<DashboardDataManager.GuestNameChangeListener> ref = mGuestNameChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardDataManager.GuestNameChangeListener listener = ref.get();
            if (listener != null) {
                listener.onGuestNameChanged();
            }
        }


    }

    void registerGuestNameChangeListener(DashboardDataManager.GuestNameChangeListener guestNameChangeListener) {
        if (null != guestNameChangeListener) {
            mGuestNameChangeListenerRefs.add(new WeakReference<DashboardDataManager.GuestNameChangeListener>(guestNameChangeListener));
        }
    }

    void unregisterGuestNameChangeListener(DashboardDataManager.GuestNameChangeListener guestNameChangeListener) {
        if (mGuestNameChangeListenerRefs == null) {
            return;
        }
        for (int i = 0; i < mGuestNameChangeListenerRefs.size(); i++) {
            WeakReference<DashboardDataManager.GuestNameChangeListener> ref = mGuestNameChangeListenerRefs.get(i);
            if (ref == null) {
                continue;
            }
            DashboardDataManager.GuestNameChangeListener listener = ref.get();
            if (listener != null && listener.equals(guestNameChangeListener)) {
                mGuestNameChangeListenerRefs.remove(ref);
                return;
            }
        }
    }
}
