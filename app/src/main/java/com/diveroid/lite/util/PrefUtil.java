package com.diveroid.lite.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Map;

public class PrefUtil {
	private static PrefUtil mInstance = null;

	public static PrefUtil getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PrefUtil(context);
		}
		return mInstance;
	}

	private Context mContext = null;
	private SharedPreferences mPref = null;
	private Editor mEdit = null;

	private PrefUtil(Context context) {
		mContext = context.getApplicationContext();
		mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		mEdit = mPref.edit();
	}

	public Map<String, ?> getAll() {
		return mPref.getAll();
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defValue) {
		try {
			if (key == null || key.equals("")) {
				return defValue;
			}
			return mPref.getBoolean(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}

	public float getFloat(String key) {
		return getFloat(key, 0f);
	}

	public float getFloat(String key, float defValue) {
		try {
			if (key == null || key.equals("")) {
				return defValue;
			}
			return mPref.getFloat(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int defValue) {
		try {
			if (key == null || key.equals("")) {
				return defValue;
			}
			return mPref.getInt(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}

	public long getLong(String key) {
		return getLong(key, 0L);
	}

	public long getLong(String key, long defValue) {
		try {
			if (key == null || key.equals("")) {
				return defValue;
			}
			return mPref.getLong(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key, String defValue) {
		try {
			if (key == null || key.equals("")) {
				return defValue;
			}
			return mPref.getString(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}

	public boolean put(String key, Object value) {
		boolean isRet = true;
		if (key == null || key.equals("") || value == null) {
			return false;
		}
		if (value instanceof Boolean) {
			mEdit.putBoolean(key, (Boolean) value);
		} else if (value instanceof Float) {
			mEdit.putFloat(key, (Float) value);
		} else if (value instanceof Integer) {
			mEdit.putInt(key, (Integer) value);
		} else if (value instanceof Long) {
			mEdit.putLong(key, (Long) value);
		} else if (value instanceof String) {
			mEdit.putString(key, (String) value);
		} else {
			return false;
		}
		isRet = mEdit.commit();
		return isRet;
	}

	public boolean remove(String key) {
		boolean isRet = false;
		if (key == null || key.equals("")) {
			return false;
		}
		mEdit.remove(key);
		isRet = mEdit.commit();
		return isRet;
	}
}
