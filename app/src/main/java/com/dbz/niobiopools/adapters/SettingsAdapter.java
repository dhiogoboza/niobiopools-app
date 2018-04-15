package com.dbz.niobiopools.adapters;

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
import com.dbz.niobiopools.models.Account;

import java.util.List;

/**
 * Created by dhiogoboza on 17/03/18.
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "SettingsAdapter";
    private List<Account> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewName;
        public TextView mTextViewAddress;
        public View mViewActive;

        public ViewHolder(View v) {
            super(v);

            mTextViewName = (TextView) v.findViewById(R.id.si_name);
            mTextViewAddress = (TextView) v.findViewById(R.id.si_address);
            mViewActive = v.findViewById(R.id.si_active);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SettingsAdapter() {

    }

    // Create new views (invoked by the layout manager)
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        vh.itemView.setOnClickListener(this);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Account account = mDataset.get(position);

        holder.itemView.setTag(account);

        holder.mTextViewName.setText(account.getName());
        holder.mTextViewAddress.setText(account.getAddress());
        holder.mViewActive.setVisibility(account.isActive() ? View.VISIBLE : View.INVISIBLE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset != null ? mDataset.size() : 0;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), ManageModelActivity.class);

        Account account = (Account) v.getTag();

        Log.d(TAG, "account: " + account);

        intent.putExtra(ManageModelActivity.EXTRA_ACCOUNT_NAME, account.getName());
        intent.putExtra(ManageModelActivity.EXTRA_ACCOUNT_ADDRESS, account.getAddress());
        intent.putExtra(ManageModelActivity.EXTRA_ACCOUNT_ACTIVE, account.isActive());

        ((MainActivity) v.getContext()).startActivityForResult(intent, MainActivity.REQUEST_CODE_ACCOUNT);
    }

    public void setAccounts(List<Account> accounts) {
        this.mDataset = accounts;
    }
}
