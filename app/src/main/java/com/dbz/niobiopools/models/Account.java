package com.dbz.niobiopools.models;

import android.content.SharedPreferences;
import android.util.Log;

import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.managers.AccountManager;
import com.dbz.niobiopools.managers.PoolsManager;

import java.util.List;

/**
 * Created by arena on 17/03/18.
 */

public class Account extends Model {

    private static final String TAG = "Account";

    private String name;
    private String address;
    private boolean active;

    public Account(String name, String address, boolean active) {
        this.name = name;
        this.address = address;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getPrefix() {
        return MainActivity.PREFERENCES_ACCOUNT_PREFIX;
    }

    @Override
    public void save(SharedPreferences.Editor editor) {
        Log.d(TAG, "account: " + this);

        editor.putString(MainActivity.PREFERENCES_ACCOUNT_PREFIX + name, address + "," + String.valueOf(active));
    }

    @Override
    public void done(SharedPreferences.Editor editor) {
        Account activeAccount = AccountManager.getInstance().getActiveAccount();
        List<Account> updatedAccounts = AccountManager.getInstance().getAll(true);

        if (isActive()) {
            if (updatedAccounts != null) {
                for (Account a : updatedAccounts) {
                    if (a.isActive() && !a.getName().equals(getName())) {
                        a.setActive(false);
                        a.save(editor);
                    }
                }
            }

            AccountManager.getInstance().setActiveAccount(this);
        }

        editor.commit();

        AccountManager.getInstance().getAll(true);
        AccountManager.getInstance().loadActiveAccount();

        if (activeAccount != AccountManager.getInstance().getActiveAccount()) {
            // active account changed
            PoolsManager.getInstance().resetStats();
        }
    }

    @Override
    public String toString() {
        return "[name: " + name + ", address: " + address + ", active: " + active + "]";
    }

}
