package com.dbz.niobiopools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

import com.dbz.niobiopools.models.Account;
import com.dbz.niobiopools.models.Model;
import com.dbz.niobiopools.models.Pool;

/**
 * Manage models
 */
public class ManageModelActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "ManageModelActivity";

    public static final String EXTRA_ACTION_POOL = "ActionPool";
    public static final String EXTRA_ACTION_SETTINGS = "ActionSettings";

    public static final String EXTRA_ACCOUNT_NAME = "AccountName";
    public static final String EXTRA_ACCOUNT_ACTIVE = "AccountActive";
    public static final String EXTRA_ACCOUNT_ADDRESS = "AccountAddress";

    public static final String EXTRA_POOL_NAME = "PoolName";
    public static final String EXTRA_POOL_URL = "PoolURL";
    public static final String EXTRA_POOL_ACTIVE = "PoolActive";

    public static final String EXTRA_FIRST_USE = "FirstUse";
    private SaveModelTask mSaveModelTask = null;

    // UI references.
    private EditText mNameView;
    private EditText mAddressView;
    private CheckBox mActiveView;

    private View mProgressView;
    private View mFormView;

    private Model mModel;

    private boolean mEditing = false;

    private String mInitialName;

    private enum Action {
        ADDRESS,
        POOL
    }

    private Action mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_model);

        int title = R.string.title_activity_add_address;

        mNameView = (EditText) findViewById(R.id.account_name);
        mAddressView = (EditText) findViewById(R.id.account_address);
        mActiveView = (CheckBox) findViewById(R.id.account_active);

        Intent intent = getIntent();
        if (intent != null) {
            String accountName = intent.getStringExtra(EXTRA_ACCOUNT_NAME);
            String poolName = intent.getStringExtra(EXTRA_POOL_NAME);

            if (accountName != null) {
                mModel = new Account(accountName,
                        intent.getStringExtra(EXTRA_ACCOUNT_ADDRESS),
                        intent.getBooleanExtra(EXTRA_ACCOUNT_ACTIVE, false));

                mNameView.setText(mModel.getId());
                mAddressView.setText(((Account) mModel).getAddress());
                mActiveView.setChecked(((Account) mModel).isActive());

                title = R.string.title_activity_edit_address;

                mInitialName = accountName;
                mEditing = true;

                mAction = Action.ADDRESS;
            } else if (poolName != null) {
                mModel = new Pool(poolName,
                        intent.getStringExtra(EXTRA_POOL_URL),
                        intent.getBooleanExtra(EXTRA_POOL_ACTIVE, false));

                mNameView.setText(mModel.getId());
                mAddressView.setText(((Pool) mModel).getURL());
                mActiveView.setChecked(((Pool) mModel).isActive());

                title = R.string.title_activity_edit_pool;

                mInitialName = poolName;
                mEditing = true;

                ((TextInputLayout) findViewById(R.id.account_address_til)).setHint(getString(R.string.pool_url));

                mAction = Action.POOL;
            } else if (intent.getBooleanExtra(EXTRA_FIRST_USE, false)) {
                mActiveView.setChecked(true);
                findViewById(R.id.account_active_container).setVisibility(View.GONE);

                mAction = Action.ADDRESS;
            } else if (intent.getBooleanExtra(EXTRA_ACTION_SETTINGS, false)) {
                title = R.string.title_activity_add_address;
                mAction = Action.ADDRESS;
            } else if (intent.getBooleanExtra(EXTRA_ACTION_POOL, false)) {
                ((TextInputLayout) findViewById(R.id.account_address_til)).setHint(getString(R.string.pool_url));

                title = R.string.title_activity_add_pool;
                mAction = Action.POOL;
            }

        }

        setupActionBar(title);

        findViewById(R.id.save_item).setOnClickListener(this);
        if (mEditing) {
            findViewById(R.id.delete_item).setOnClickListener(this);
        } else {
            findViewById(R.id.delete_item).setVisibility(View.GONE);
        }

        mFormView = findViewById(R.id.model_form);
        mProgressView = findViewById(R.id.model_progress);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar(@StringRes int title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setTitle(title);
        }
    }

    private void deleteItem() {
        showProgress(true);
        mSaveModelTask = new SaveModelTask(TaskAction.DELETE);
        mSaveModelTask.execute(mModel);
    }

    private void saveItem() {
        if (mSaveModelTask != null) {
            return;
        }

        // Reset errors.
        mNameView.setError(null);
        mAddressView.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mNameView.getText().toString())) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(mAddressView.getText().toString())) {
            mAddressView.setError(getString(R.string.error_field_required));
            focusView = mAddressView;
            cancel = true;
        }

        if (cancel) {
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the action.
            showProgress(true);

            if (mModel == null) {
                if (mAction == Action.ADDRESS) {
                    mModel = new Account(mNameView.getText().toString(),
                            mAddressView.getText().toString(),
                            mActiveView.isChecked());
                } else {
                    mModel = new Pool(mNameView.getText().toString(),
                            mAddressView.getText().toString(),
                            mActiveView.isChecked());
                }
            } else {
                if (mAction == Action.ADDRESS) {
                    ((Account) mModel).setName(mNameView.getText().toString());
                    ((Account) mModel).setAddress(mAddressView.getText().toString());
                    ((Account) mModel).setActive(mActiveView.isChecked());
                } else {
                    ((Pool) mModel).setName(mNameView.getText().toString());
                    ((Pool) mModel).setURL(mAddressView.getText().toString());
                    ((Pool) mModel).setActive(mActiveView.isChecked());
                }
            }


            mSaveModelTask = new SaveModelTask(TaskAction.SAVE);
            mSaveModelTask.execute(mModel);
        }
    }

    /**
     * Shows the progress UI and hides the form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_item) {
            saveItem();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_title)
                    .setMessage(R.string.delete_sure)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                                deleteItem();
                            }
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    public enum TaskAction {
        SAVE,
        DELETE
    }

    /**
     * Represents an asynchronous task.
     */
    public class SaveModelTask extends AsyncTask<Model, Void, Boolean> {
        TaskAction action;

        SaveModelTask(TaskAction action) {
            this.action = action;
        }

        @Override
        protected Boolean doInBackground(Model... models) {
            Model model = models[0];
            SharedPreferences settings = getSharedPreferences(MainActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            if (action == TaskAction.DELETE) {
                model.delete(editor);
            } else {
                try {
                    // if name changed remove old config
                    if (mEditing && !model.getId().equals(mInitialName)) {
                        editor.remove(model.getPrefix() + mInitialName);
                    }

                    model.save(editor);
                    model.done(editor);
                } catch (Exception e) {
                    e.printStackTrace();

                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveModelTask = null;
            showProgress(false);

            if (success) {
                setResult(RESULT_OK);
                finish();
            } else {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.add_address_container), getString(R.string.error_saving_item), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

        @Override
        protected void onCancelled() {
            mSaveModelTask = null;
            showProgress(false);
        }
    }
}

