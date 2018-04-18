package com.dbz.niobiopools.adapters;

import android.app.MediaRouteButton;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dbz.niobiopools.ManageModelActivity;
import com.dbz.niobiopools.MainActivity;
import com.dbz.niobiopools.R;
import com.dbz.niobiopools.models.Pool;

import java.util.List;

/**
 * Created by dhiogoboza on 17/03/18.
 */
public class PoolsAdapter extends RecyclerView.Adapter<PoolsAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "SettingsAdapter";
    private final boolean mMining;
    private List<Pool> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public TextView mTextViewName;
        public TextView mTextViewURL;
        public TextView mTextViewHashrate;
        public TextView mTextViewPaid;

        public View mProgress;
        public View mWarningIcon;
        public View mViewActive;
        public View mStatsContainer;

        public ViewHolder(View v, boolean mining) {
            super(v);

            mTextViewName = (TextView) v.findViewById(R.id.pi_name);
            mTextViewURL = (TextView) v.findViewById(R.id.pi_url);

            if (mining) {
                mTextViewHashrate = (TextView) v.findViewById(R.id.pi_hashrate);
                mTextViewPaid = (TextView) v.findViewById(R.id.pi_paid);
                mWarningIcon = v.findViewById(R.id.pi_warning);
                mProgress = v.findViewById(R.id.pi_progress);
                mStatsContainer = v.findViewById(R.id.pool_mining_status_container);

                mProgress.setVisibility(View.VISIBLE);
                mStatsContainer.setVisibility(View.GONE);

                v.findViewById(R.id.pool_active_container).setVisibility(View.GONE);
            } else {
                mViewActive = v.findViewById(R.id.pi_active);
                v.findViewById(R.id.pool_mining_status_container).setVisibility(View.GONE);
                v.findViewById(R.id.pi_progress).setVisibility(View.GONE);
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PoolsAdapter(boolean mining) {
        mMining = mining;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PoolsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pools_item, parent, false);

        ViewHolder vh = new ViewHolder(v, mMining);

        vh.itemView.setOnClickListener(this);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Pool pool = mDataset.get(position);

        holder.itemView.setTag(pool);

        holder.mTextViewName.setText(pool.getShowName());
        holder.mTextViewURL.setText(pool.getShowURL());

        if (mMining) {
            if (pool.getPaid() >= 0 || pool.isConnectionFail()) {
                holder.mProgress.setVisibility(View.GONE);
                holder.mStatsContainer.setVisibility(View.VISIBLE);

                long p = pool.getPaid();

                holder.mTextViewHashrate.setText(pool.getHashRate() + "/s");
                holder.mTextViewPaid.setText(String.valueOf((p < 0 ? 0 : p) / 100000000) + " NBR");
                holder.mWarningIcon.setVisibility(pool.isConnectionFail() ? View.VISIBLE : View.INVISIBLE);
            } else {
                holder.mStatsContainer.setVisibility(View.GONE);
                holder.mProgress.setVisibility(View.VISIBLE);
            }
        } else {
            holder.mViewActive.setVisibility(pool.isActive() ? View.VISIBLE : View.INVISIBLE);
        }

    }

    public void notifyItemChanged(Pool pool) {
        if (mDataset != null && mDataset.contains(pool)) {
            notifyItemChanged(mDataset.indexOf(pool));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset != null ? mDataset.size() : 0;
    }

    @Override
    public void onClick(View v) {
        if (mMining) {
            // TODO: charts
        } else {
            Intent intent = new Intent(v.getContext(), ManageModelActivity.class);

            Pool pool = (Pool) v.getTag();

            Log.d(TAG, "account: " + pool);

            intent.putExtra(ManageModelActivity.EXTRA_POOL_NAME, pool.getName());
            intent.putExtra(ManageModelActivity.EXTRA_POOL_URL, pool.getURL());
            intent.putExtra(ManageModelActivity.EXTRA_POOL_ACTIVE, pool.isActive());

            ((MainActivity) v.getContext()).startActivityForResult(intent, MainActivity.REQUEST_CODE_POOL);
        }
    }

    public void setPools(List<Pool> pools) {
        this.mDataset = pools;
    }
}
