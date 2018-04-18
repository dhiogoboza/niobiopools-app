package com.dbz.niobiopools.models;

import android.content.SharedPreferences;
import android.util.Log;

import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.managers.PoolsManager;

/**
 * Created by dhiogoboza on 27/03/18.
 */

public class Pool extends Model {

    private static final String TAG = "Pool";

    private static final int MAX_NAME_SIZE = 25;
    private static final int MAX_URL_SIZE = 35;

    private String name;
    private String url;
    private boolean active;

    private boolean connectionFail = false;
    private boolean previousConnectionFail = false;

    private long hashes = 0;
    private long lastShare = 0;
    private String hashrate = "0 H";
    private long balance = 0;
    private long paid = -1;
    private String showName;
    private String showURL;

    public Pool(String name, String url, boolean active) {
        this.name = name;
        this.url = url;
        this.active = active;

        setShowURL();
        setShowName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setShowName();
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
        setShowURL();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getHashRate() {
        return hashrate;
    }

    public long getPaid() {
        return paid;
    }

    public String getURLIdentifier() {
        if (url.endsWith("/")) {
            return url.length() > 1? url.substring(0, url.length() - 2) : "";
        }

        return url;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getPrefix() {
        return MainActivity.PREFERENCES_POOL_PREFIX;
    }

    @Override
    public void save(SharedPreferences.Editor editor) {
        editor.putString(MainActivity.PREFERENCES_POOL_PREFIX + name, url + "," + String.valueOf(active));
    }

    @Override
    public void done(SharedPreferences.Editor editor) {
        editor.commit();

        // update pools list
        PoolsManager.getInstance().getAll(true);
    }

    @Override
    public String toString() {
        return "[name: " + name + ", url: " + url + ", active: " + active + "]";
    }

    public void setHashes(long hashes) {
        this.hashes = hashes;
    }

    public void setLastShare(long lastShare) {
        this.lastShare = lastShare;
    }

    public void setHashrate(String hashrate) {
        this.hashrate = hashrate;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setPaid(long paid) {
        this.paid = paid;
    }

    public boolean isConnectionFail() {
        return connectionFail;
    }

    public void setConnectionFail(boolean connectionFail) {
        this.connectionFail = connectionFail;
    }

    public boolean isPreviousConnectionFail() {
        return previousConnectionFail;
    }

    public void setPreviousConnectionFail(boolean previousConnectionFail) {
        this.previousConnectionFail = previousConnectionFail;
    }

    public long getNumericHashRate() {
        try {
            if (hashrate.isEmpty()) {
                return 0;
            }

            int multiplier = 1;
            if (hashrate.contains("K")) {
                multiplier = 1000;
            } else if (hashrate.contains("M")) {
                multiplier = 1000000;
            }

            return (long) (Double.parseDouble(hashrate.replaceAll("[^0-9.]", "")) * multiplier);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parsing number", e);
            return 0;
        }
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName() {
        if (name != null && name.length() > MAX_NAME_SIZE) {
            showName = name.substring(0, MAX_NAME_SIZE) + "...";
        } else {
            showName = name;
        }
    }

    public String getShowURL() {
        return showURL;
    }

    public void setShowURL() {
        if (url != null && url.length() > MAX_URL_SIZE) {
            showURL = url.substring(0, MAX_URL_SIZE) + "...";
        } else {
            showURL = url;
        }
    }
}
