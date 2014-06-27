package com.example.slideshow.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.slideshow.MainActivity;
import com.example.slideshow.app.Constants;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
	final public static String ONE_TIME = "onetime";
	final public static String START_PRESENTATION = "start_presentation";

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.v(Constants.TAG, "START STOP");

		boolean start = intent.getBooleanExtra(START_PRESENTATION, false);

		if (start) {
			context.sendBroadcast(new Intent(MainActivity.CLOSE_ACTION));
			
			Intent i = new Intent(context, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(START_PRESENTATION, true);
			context.startActivity(i);
		} else {
			// Stop
			context.sendBroadcast(new Intent(MainActivity.STOP_ACTION));
		}

	}

	public void setOnetimeTimer(Context context, long startTime, boolean start) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		intent.putExtra(ONE_TIME, Boolean.TRUE);
		intent.putExtra(START_PRESENTATION, start);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		am.set(AlarmManager.RTC_WAKEUP, startTime, pi);
	}
}