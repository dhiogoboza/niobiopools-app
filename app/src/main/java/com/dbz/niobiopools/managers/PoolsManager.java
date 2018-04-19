package com.dbz.niobiopools.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.adapters.PoolsAdapter;
import com.dbz.niobiopools.models.Account;
import com.dbz.niobiopools.models.Pool;
import com.dbz.niobiopools.utils.ParserUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dhiogoboza on 05/04/18.
 */

public class PoolsManager implements Runnable {

    private static final String TAG = "PoolsManager";
    private static final long TIME_UPDATE_POOLS = 10;

    private static final String CONFIG_LAST_HASH_RATE = "lastHashRate";
    private static final String CONFIG_LAST_PAID = "lastPaid";

    private static final String JSON_KEY_STATS = "stats";
    private static final String JSON_KEY_HASHES = "hashes";
    private static final String JSON_KEY_LAST_SHARE = "lastShare";
    private static final String JSON_KEY_HASHRATE = "hashrate";
    private static final String JSON_KEY_BALANCE = "balance";
    private static final String JSON_KEY_PAID = "paid";
    private static final String JSON_KEY_ERROR = "error";

    private static PoolsManager instance;
    private MainActivity mContext;
    private ArrayList<Pool> mPoolsList;
    private ArrayList<Pool> mActivePoolsList;
    private ScheduledThreadPoolExecutor mExecutor;
    private ArrayList<RecyclerView> mRecyclersViewList;
    private long mTotalHashRate = 0;
    private long mTotalPaid = 0;

    private PoolsManager(MainActivity context) {
        mContext = context;
        mRecyclersViewList = new ArrayList<>();
        trustEveryone();
    }

    public static void createInstance(MainActivity context) {
        if (instance == null) {
            instance = new PoolsManager(context);
        } else {
            instance.mContext = context;
        }
    }

    public static PoolsManager getInstance() {
        return instance;
    }

    public List<Pool> getAll() {
        return getAll(false);
    }

    public List<Pool> getAll(boolean refresh) {
        if (refresh || mPoolsList == null) {
            try {
                SharedPreferences settings = mContext.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);

                if (mPoolsList == null) {
                    mPoolsList = new ArrayList<>();
                    mActivePoolsList = new ArrayList<>();
                } else {
                    mPoolsList.clear();
                    mActivePoolsList.clear();
                }

                Map<String, ?> allSettings = settings.getAll();
                Pool pool;

                for (Map.Entry<String, ?> entry : allSettings.entrySet()) {
                    Log.d(TAG, entry.getKey() + " = " + entry.getValue());

                    if (entry.getValue() instanceof String) {
                        if (entry.getKey().startsWith(MainActivity.PREFERENCES_POOL_PREFIX)) {
                            String name = entry.getKey().replace(MainActivity.PREFERENCES_POOL_PREFIX, "");
                            String value = ((String) entry.getValue());

                            Log.d(TAG, name + " = " + value);

                            String[] splitValue = value.split(",");
                            String url = splitValue[0];
                            boolean active = splitValue.length > 1 ? Boolean.parseBoolean(splitValue[1]) : false;

                            pool = new Pool(name, url, active);

                            mPoolsList.add(pool);

                            if (pool.isActive()) {
                                mActivePoolsList.add(pool);
                            }
                        }
                    }
                }

                addStaticPools(mPoolsList, settings.edit());

                startPoolsMonitoring();

                return mPoolsList;
            } catch (NullPointerException e) {
                Log.e(TAG, "Recovering pools", e);
            }
        }

        return mPoolsList;
    }

    private void startPoolsMonitoring() {
        if (mExecutor == null) {
            mExecutor = new ScheduledThreadPoolExecutor(1);
        } else if (!mExecutor.isShutdown()) {
            mExecutor.shutdown();
            mExecutor = new ScheduledThreadPoolExecutor(1);
        }
        mExecutor.scheduleWithFixedDelay(PoolsManager.this, 2, TIME_UPDATE_POOLS, TimeUnit.SECONDS);
    }

    private void addStaticPools(List<Pool> pools, SharedPreferences.Editor editor) {

        Pool pool = createStaticPool("4miner.me", "http://api-cryptonote.4miner.me:8118", editor);
        if (!pools.contains(pool)) {
            pools.add(pool);
            mActivePoolsList.add(pool);
        }

        pool = createStaticPool("CiaPool", "https://nbr.ciapool.com/api", editor);
        if (!pools.contains(pool)) {
            pools.add(pool);
            mActivePoolsList.add(pool);
        }

        pool = createStaticPool("Niobio Cash Mining Pool", "https://niobio-pool.com/api", editor);
        if (!pools.contains(pool)) {
            pools.add(pool);
            mActivePoolsList.add(pool);
        }

        pool = createStaticPool("niobiopool.com.br", "http://45.79.150.254:8119", editor);
        if (!pools.contains(pool)) {
            pools.add(pool);
            mActivePoolsList.add(pool);
        }

        pool = createStaticPool("CryptoKnight.cc", "https://cryptoknight.cc/rpc/niobio", editor);
        if (!pools.contains(pool)) {
            pools.add(pool);
            mActivePoolsList.add(pool);
        }

        editor.commit();
    }

    private Pool createStaticPool(String name, String url, SharedPreferences.Editor editor) {
        Pool pool = new Pool(name, url, true);
        pool.save(editor);

        return pool;
    }

    public List<Pool> getActivePools() {
        if (mActivePoolsList == null) {
            getAll();
        }

        return mActivePoolsList;
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Account account = AccountManager.getInstance().getActiveAccount();
        if (mActivePoolsList != null && account != null) {
            String address = account.getAddress();
            long totalPaid = 0;
            long totalHashRate = 0;
            for (Pool pool : mActivePoolsList) {
                if (updatePool(pool, address)) {
                    Log.d(TAG, "pool updated");

                    for (RecyclerView recyclerView: mRecyclersViewList) {
                        ((PoolsAdapter) recyclerView.getAdapter()).notifyItemChanged(pool);
                    }
                    totalPaid += pool.getPaid();
                    totalHashRate += pool.getNumericHashRate();

                    Log.d(TAG, "totalHashRate: " + totalHashRate);
                } else {
                    Log.d(TAG, "not updated");
                }
            }

            mTotalPaid = totalPaid;
            mTotalHashRate = totalHashRate;

            SharedPreferences settings = mContext.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(CONFIG_LAST_HASH_RATE, mTotalHashRate);
            editor.putLong(CONFIG_LAST_PAID, mTotalPaid);
            editor.commit();

            mContext.updateTotal(account, totalPaid, totalHashRate);
        }
    }

    public long getTotalhashRate() {
        return mTotalHashRate;
    }

    public long getTotalPaid() {
        return mTotalPaid;
    }

    private boolean updatePool(Pool pool, String address) {
        try {
            Log.d(TAG, "url: " + (pool.getURL() + "/stats_address?address=" + address));

            String poolURL = pool.getURL().endsWith("/") ?
                    pool.getURL().substring(0, pool.getURL().length() - 2) : pool.getURL();

            URL poolUrl = new URL(poolURL + "/stats_address?address=" + address);

            if (pool.getURL().startsWith("https")) {
                HttpsURLConnection urlConnection = (HttpsURLConnection) poolUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(8000 /* milliseconds */);
                urlConnection.setConnectTimeout(6000 /* milliseconds */);
                urlConnection.setDoOutput(true);
                urlConnection.connect();
            } else {
                HttpURLConnection urlConnection = (HttpURLConnection) poolUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(8000 /* milliseconds */);
                urlConnection.setConnectTimeout(6000 /* milliseconds */);
                urlConnection.setDoOutput(true);
                urlConnection.connect();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(poolUrl.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            String jsonString = sb.toString();
            JSONObject jsonObject = new JSONObject(jsonString);


            if (!jsonObject.has(JSON_KEY_ERROR)) {
                JSONObject stats = jsonObject.getJSONObject(JSON_KEY_STATS);

                pool.setHashes(ParserUtils.getNumberValue(stats, JSON_KEY_HASHES));
                pool.setLastShare(ParserUtils.getNumberValue(stats, JSON_KEY_LAST_SHARE));
                pool.setHashrate(ParserUtils.getStringValue(stats, JSON_KEY_HASHRATE, "0 H"));
                pool.setBalance(ParserUtils.getNumberValue(stats, JSON_KEY_BALANCE));
                pool.setPaid(ParserUtils.getNumberValue(stats, JSON_KEY_PAID));
            } else {
                pool.setHashes(0);
                pool.setLastShare(0);
                pool.setHashrate("0 H");
                pool.setBalance(0);
                pool.setPaid(0);

                Log.e(TAG, "recovering pool data[url: " + pool.getURL() + ", result: " + jsonObject + "]");
            }

            pool.setConnectionFail(false);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "requesting pool " + pool.getURL() + " data", e);

            pool.setConnectionFail(true);
        }

        boolean result =  pool.isPreviousConnectionFail() != pool.isConnectionFail();
        pool.setPreviousConnectionFail(pool.isConnectionFail());

        return result;
    }

    public void unregisterRecyclerView(RecyclerView mRecyclerView) {
        mRecyclerView.invalidate();
        mRecyclersViewList.remove(mRecyclerView);
    }

    public void registerRecyclerView(RecyclerView mRecyclerView) {
        mRecyclersViewList.add(mRecyclerView);
    }

    public void resetStats() {
        mTotalHashRate = 0;
        mTotalPaid = 0;
        for (Pool pool : mPoolsList) {
            pool.setBalance(0);
            pool.setHashes(0);
            pool.setHashrate("0");
            pool.setPaid(-1);
        }
    }

    public void init() {
        getAll(true);

        SharedPreferences settings = mContext.getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);

        mTotalHashRate = settings.getLong(CONFIG_LAST_HASH_RATE, 0);
        mTotalPaid = settings.getLong(CONFIG_LAST_PAID, 0);

    }
}
