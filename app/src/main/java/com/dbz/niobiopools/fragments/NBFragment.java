package com.dbz.niobiopools.fragments;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by dhiogoboza on 07/04/18.
 */

public abstract class NBFragment extends Fragment {

    private static final String TAG = "NBFragment";

    protected RecyclerView mRecyclerView;

    public abstract String getFragmentTag();
    public abstract @StringRes int getTitle();

    public void invalidateList() {
        Log.d(TAG, "invalidating mRecyclerView " + mRecyclerView);

        if (mRecyclerView != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

}
