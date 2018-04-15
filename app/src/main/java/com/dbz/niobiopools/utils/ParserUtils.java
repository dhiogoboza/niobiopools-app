package com.dbz.niobiopools.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arena on 05/04/18.
 */

public class ParserUtils {

    public static long getNumberValue(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.has(key)) {
            return Long.parseLong(jsonObject.getString(key).trim());
        }

        return 0;
    }

    public static String getStringValue(JSONObject jsonObject, String key, String defaulValue) throws JSONException {
        if (jsonObject.has(key)) {
            return jsonObject.getString(key);
        }

        return defaulValue;
    }
}
