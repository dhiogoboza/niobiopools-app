package com.dbz.niobiopools.models;

import android.content.SharedPreferences;

/**
 * Created by arena on 01/04/18.
 */

public abstract class Model {

    private static final String TAG = "Model";

    public abstract void save(SharedPreferences.Editor editor);
    public abstract String getId();
    public abstract String getPrefix();
    public abstract void done(SharedPreferences.Editor editor);

    public void delete(SharedPreferences.Editor editor) {
        editor.remove(getPrefix() + getId());
        done(editor);

        done(editor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null  && obj instanceof Model) {
            Model another = (Model) obj;

            return another.getId().equals(getId());
        }

        return false;
    }
}
