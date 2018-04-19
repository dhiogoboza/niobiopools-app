package com.dbz.niobiopools.fragments;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.R;
import com.dbz.niobiopools.adapters.PoolsAdapter;
import com.dbz.niobiopools.managers.AccountManager;
import com.dbz.niobiopools.managers.PoolsManager;
import com.dbz.niobiopools.models.Account;
import com.dbz.niobiopools.models.Pool;

import java.util.List;

/**
 * A simple {@link NBFragment} subclass.
 */
public class MiningFragment extends NBFragment implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MiningFragment";

    private PoolsAdapter mAdapter;

    private TextView mAccountName;
    private TextView mAccountAddress;

    private TextView mPaid;
    private TextView mHashrate;

    public MiningFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mining, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.mining_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PoolsAdapter(true);
        mRecyclerView.setAdapter(mAdapter);

        mAccountName = (TextView) view.findViewById(R.id.current_account_name);
        mAccountAddress = (TextView) view.findViewById(R.id.current_account_address);

        mHashrate = (TextView) view.findViewById(R.id.total_hashrate);
        mPaid = (TextView) view.findViewById(R.id.total_paid);

        View statsView = view.findViewById(R.id.card_view_stats);

        statsView.setOnClickListener(this);;
        statsView.setOnLongClickListener(this);

        PoolsManager poolsManager = PoolsManager.getInstance();
        updateTotal(AccountManager.getInstance().getActiveAccount(), poolsManager.getTotalPaid(), poolsManager.getTotalhashRate());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        PoolsManager.getInstance().unregisterRecyclerView(mRecyclerView);
    }

    private void loadList() {
        new AsyncTask<Void, Void, List<Pool>>() {

            @Override
            protected List<Pool> doInBackground(Void... params) {
                return PoolsManager.getInstance().getActivePools();
            }

            @Override
            protected void onPostExecute(List<Pool> pools) {
                super.onPostExecute(pools);

                mAdapter.setPools(pools);
                mAdapter.notifyDataSetChanged();

                PoolsManager.getInstance().registerRecyclerView(mRecyclerView);
            }
        }.execute();

    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public @StringRes  int getTitle() {
        return R.string.menu_mining;
    }

    public void updateTotal(Account account, long totalPaid, long totalHashRate) {
        if (account != null) {
            mAccountName.setText(account.getName());
            mAccountAddress.setText(account.getAddress());
        }

        mHashrate.setText(String.valueOf(totalHashRate) + " H/s");
        mPaid.setText(String.valueOf(totalPaid / 100000000) + " NBR");
    }

    @Override
    public void onClick(View v) {
        ((MainActivity) getActivity()).updateCurrentFragment(R.id.nav_settings);
    }

    @Override
    public boolean onLongClick(View v) {
        Account account = AccountManager.getInstance().getActiveAccount();

        if (account != null) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("wallet", account.getName());
            clipboard.setPrimaryClip(clip);

            Snackbar.make(getView().findViewById(R.id.fragment_mining_content),
                    R.string.wallet_copied,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }

        return true;
    }
}
