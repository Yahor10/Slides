package com.example.slideshow.utils;

import com.example.slideshow.app.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceUtils {

	public static void setInt(Context context, String key, int value) {
		Editor pEditor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		pEditor.putInt(key, value);
		pEditor.commit();
	}

	public static int getInt(Context context, String key) {
		SharedPreferences defaultSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return defaultSharedPreferences.getInt(key, Constants.DEFAULT_VALUE);
	}

}
