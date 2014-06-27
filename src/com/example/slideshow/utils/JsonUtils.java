package com.example.slideshow.utils;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.slideshow.app.Constants;
import com.example.slideshow.data.ModelOfPreferenceTime;

public class JsonUtils {

	public static String createPreferenceJson(ModelOfPreferenceTime time) {
		String result = "";
		result += "{";
		result += "\"" + Constants.PREFERENCE_KEY_SPEED + "\"" + ":"
				+ time.speed + ",";
		result += "\"" + Constants.PREFERENCE_KEY_START_TIME_HOUR + "\"" + ":"
				+ time.start_time_hour + ",";
		result += "\"" + Constants.PREFERENCE_KEY_START_TIME_MINUTE + "\""
				+ ":" + time.start_time_minute + ",";
		result += "\"" + Constants.PREFERENCE_KEY_END_TIME_HOUR + "\"" + ":"
				+ time.end_time_hour + ",";
		result += "\"" + Constants.PREFERENCE_KEY_END_TIME_MINUTE + "\"" + ":"
				+ time.end_time_minute;
		result += "}";
		return result;
	}

	public static ModelOfPreferenceTime getModelFromJson(String jsonString) {
		ModelOfPreferenceTime times = new ModelOfPreferenceTime();

		try {
			JSONObject jsonObj = new JSONObject(jsonString);
			times.speed = jsonObj.getInt(Constants.PREFERENCE_KEY_SPEED);
			times.start_time_hour = jsonObj
					.getInt(Constants.PREFERENCE_KEY_START_TIME_HOUR);
			times.start_time_minute = jsonObj
					.getInt(Constants.PREFERENCE_KEY_START_TIME_MINUTE);
			times.end_time_hour = jsonObj
					.getInt(Constants.PREFERENCE_KEY_END_TIME_HOUR);
			times.end_time_minute = jsonObj
					.getInt(Constants.PREFERENCE_KEY_END_TIME_MINUTE);
			return times;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;

		}

	}
}
