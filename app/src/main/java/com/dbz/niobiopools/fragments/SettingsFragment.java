package com.dbz.niobiopools.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dbz.niobiopools.ManageModelActivity;
import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.R;
import com.dbz.niobiopools.adapters.SettingsAdapter;
import com.dbz.niobiopools.managers.AccountManager;
import com.dbz.niobiopools.models.Account;

import java.util.List;


/**
 *
 */
public class SettingsFragment extends NBFragment {

    public final String TAG = "SettingsFragment";

    private SettingsAdapter mAdapter;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ManageModelActivity.class);
                intent.putExtra(ManageModelActivity.EXTRA_ACTION_SETTINGS, true);
                getActivity().startActivityForResult(intent, MainActivity.REQUEST_CODE_ACCOUNT);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.settings_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SettingsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadList();
    }

    private void loadList() {
        List<Account> accounts = AccountManager.getInstance().getAll();

        mAdapter.setAccounts(accounts);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public @StringRes int getTitle() {
        return R.string.menu_settings;
    }
}
