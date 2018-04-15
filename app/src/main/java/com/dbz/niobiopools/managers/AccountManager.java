package com.dbz.niobiopools.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dhiogoboza on 05/04/18.
 */

public class AccountManager {

    private static final String TAG = "AccountManager";

    private static AccountManager instance;
    private Context mContext;
    private Account mActiveAccount;
    private List<Account> mAllAccounts;

    private AccountManager(Context context) {
        mContext = context;
    }

    public static void createInstance(Context context) {
        if (instance == null) {
            instance = new AccountManager(context);
        } else {
            instance.mContext = context;
        }
    }

    public static AccountManager getInstance() {
        return instance;
    }

    public List<Account> getAll() {
        return getAll(false);
    }

    public List<Account> getAll(boolean refresh) {
        if (refresh || mAllAccounts == null){
            try {
                SharedPreferences settings = mContext.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);

                if (mAllAccounts == null) {
                    mAllAccounts = new ArrayList<>();
                } else {
                    mAllAccounts.clear();
                }

                Map<String, ?> allSettings = settings.getAll();

                for (Map.Entry<String, ?> entry : allSettings.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        if (entry.getKey().startsWith(MainActivity.PREFERENCES_ACCOUNT_PREFIX)) {
                            String value = ((String) entry.getValue());
                            String name = entry.getKey().replace(MainActivity.PREFERENCES_ACCOUNT_PREFIX, "");

                            Log.d(TAG, name + " = " + value);

                            String[] splitValue = value.split(",");
                            String address = splitValue[0];
                            boolean active = splitValue.length > 1 ? Boolean.parseBoolean(splitValue[1]) : false;

                            mAllAccounts.add(new Account(name, address, active));
                        }
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "Recovering accounts", e);
            }
        }

        return mAllAccounts != null && mAllAccounts.size() > 0 ? mAllAccounts : null;
    }

    public void loadActiveAccount() {
        if (mAllAccounts != null) {
            for (Account account : mAllAccounts) {
                if (account.isActive()) {
                    mActiveAccount = account;
                    return;
                }
            }

            if (!mAllAccounts.isEmpty()) {
                // if no active account was found, set first account to active
                mActiveAccount = mAllAccounts.get(0);
                mActiveAccount.setActive(true);

                SharedPreferences settings = mContext.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
                mActiveAccount.save(settings.edit());
            }
        }
    }

    public Account getActiveAccount() {
        return mActiveAccount;
    }

    public void setActiveAccount(Account activeAccount) {
        this.mActiveAccount = activeAccount;
    }
}
