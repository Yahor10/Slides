package com.example.slideshow;

import interfaces.CallBackFromFIleDialog;

import java.io.File;
import java.util.Calendar;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.slideshow.app.Constants;
import com.example.slideshow.data.ModelOfPreferenceTime;
import com.example.slideshow.dialogs.DialogSavePreference;
import com.example.slideshow.dialogs.FileDialog;
import com.example.slideshow.receiver.AlarmManagerBroadcastReceiver;
import com.example.slideshow.utils.JsonUtils;
import com.example.slideshow.utils.PreferenceUtils;

public class SlidePreferenceActivity extends ActionBarActivity implements
		OnClickListener, OnSeekBarChangeListener, CallBackFromFIleDialog {

	SeekBar seekImageSpeed;
	TextView txtCurrentValueSpeed;
	TextView txtValueStart;
	TextView txtValueEnd;
	AlarmManagerBroadcastReceiver alarmReceiver = new AlarmManagerBroadcastReceiver();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		seekImageSpeed = (SeekBar) findViewById(R.id.seek_bar_speed);
		seekImageSpeed.setOnSeekBarChangeListener(this);
		txtCurrentValueSpeed = (TextView) findViewById(R.id.txt_current_speed_value);
		txtValueStart = (TextView) findViewById(R.id.txt_time_start_value);
		txtValueEnd = (TextView) findViewById(R.id.txt_time_end_value);
		findViewById(R.id.lay_open_pref).setOnClickListener(this);
		findViewById(R.id.lay_save_pref).setOnClickListener(this);
		findViewById(R.id.lay_time_start).setOnClickListener(this);
		findViewById(R.id.lay_time_end).setOnClickListener(this);

		updateUIFromPreference();

		findViewById(R.id.lay_open_pref).setOnClickListener(this);
		findViewById(R.id.lay_save_pref).setOnClickListener(this);

		
	}

	private void updateUIFromPreference() {
		setParamentrSeekBar(PreferenceUtils.getInt(getApplicationContext(),
				Constants.PREFERENCE_KEY_SPEED));
		setParamentrStartTime(PreferenceUtils.getInt(this,
				Constants.PREFERENCE_KEY_START_TIME_HOUR),
				PreferenceUtils.getInt(this,
						Constants.PREFERENCE_KEY_START_TIME_MINUTE));

		setParamentrEndTime(PreferenceUtils.getInt(this,
				Constants.PREFERENCE_KEY_END_TIME_HOUR),
				PreferenceUtils.getInt(this,
						Constants.PREFERENCE_KEY_END_TIME_MINUTE));
	}

	private void setParamentrSeekBar(int value) {
		seekImageSpeed.setProgress((value - 1));
		txtCurrentValueSpeed.setText("" + value);
	}

	private void setParamentrStartTime(int hour, int minute) {
		txtValueStart.setText(hour + "-" + minute);

	}

	private void setParamentrEndTime(int hour, int minute) {
		txtValueEnd.setText(hour + "-" + minute);

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.lay_open_pref:
			openFileButton();
			break;
		case R.id.lay_save_pref:
			DialogSavePreference dialogSavePreference = new DialogSavePreference(
					JsonUtils.createPreferenceJson(new ModelOfPreferenceTime(
							seekImageSpeed.getProgress(),
							PreferenceUtils.getInt(getApplicationContext(),
									Constants.PREFERENCE_KEY_START_TIME_HOUR),
							PreferenceUtils.getInt(getApplicationContext(),
									Constants.PREFERENCE_KEY_START_TIME_MINUTE),
							PreferenceUtils.getInt(getApplicationContext(),
									Constants.PREFERENCE_KEY_END_TIME_HOUR),
							PreferenceUtils.getInt(getApplicationContext(),
									Constants.PREFERENCE_KEY_END_TIME_MINUTE))));
			dialogSavePreference.show(getSupportFragmentManager(), "");
			break;
		case R.id.lay_time_start:

			int hour = PreferenceUtils.getInt(this,
					Constants.PREFERENCE_KEY_START_TIME_HOUR);
			int minute = PreferenceUtils.getInt(this,
					Constants.PREFERENCE_KEY_START_TIME_MINUTE);

			TimePickerDialog pickerDialog = new TimePickerDialog(this,
					new OnTimeSetListener() {

						@Override
						public void onTimeSet(android.widget.TimePicker view,
								int hourOfDay, int minute) {
							PreferenceUtils.setInt(getBaseContext(),
									Constants.PREFERENCE_KEY_START_TIME_HOUR,
									hourOfDay);
							PreferenceUtils.setInt(getBaseContext(),
									Constants.PREFERENCE_KEY_START_TIME_MINUTE,
									minute);

							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
							calendar.set(Calendar.MINUTE, minute);
							calendar.set(Calendar.SECOND, 00);
							
							txtValueStart.setText(hourOfDay + ":" + minute);
							
							alarmReceiver.setOnetimeTimer(getBaseContext(), calendar.getTimeInMillis(), true);
						}
					}, hour, minute, true);
			pickerDialog.show();

			break;
		case R.id.lay_time_end:

			int hour_end = PreferenceUtils.getInt(this,
					Constants.PREFERENCE_KEY_END_TIME_HOUR);
			int minute_end = PreferenceUtils.getInt(this,
					Constants.PREFERENCE_KEY_END_TIME_MINUTE);

			TimePickerDialog pickerDialogEnd = new TimePickerDialog(this,
					new OnTimeSetListener() {

						@Override
						public void onTimeSet(android.widget.TimePicker view,
								int hourOfDay, int minute) {
							PreferenceUtils.setInt(getBaseContext(),
									Constants.PREFERENCE_KEY_END_TIME_HOUR,
									hourOfDay);
							PreferenceUtils.setInt(getBaseContext(),
									Constants.PREFERENCE_KEY_END_TIME_MINUTE,
									minute);

							txtValueEnd.setText(hourOfDay + "-" + minute);
							
							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
							calendar.set(Calendar.MINUTE, minute);
							calendar.set(Calendar.SECOND, 00);
							
							alarmReceiver.setOnetimeTimer(getBaseContext(), calendar.getTimeInMillis(), false);
						}
					}, hour_end, minute_end, true);
			pickerDialogEnd.show();

			break;

		default:
			break;
		}

	}

	private void openFileButton() {
		File mPath = new File(Constants.PATH_FOR_FILES);
		FileDialog fileDialog = new FileDialog(this, mPath, this);
		fileDialog.setFileEndsWith(".ert");
		fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
			public void fileSelected(File file) {
				Log.d(getClass().getName(), "selected file " + file.toString());
			}
		});
		// fileDialog.addDirectoryListener(new
		// FileDialog.DirectorySelectedListener() {
		// public void directorySelected(File directory) {
		// Log.d(getClass().getName(), "selected dir " + directory.toString());
		// }
		// });
		// fileDialog.setSelectDirectoryOption(false);
		fileDialog.showDialog();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		switch (seekBar.getId()) {
		case R.id.seek_bar_speed:

			if (fromUser) {
				txtCurrentValueSpeed.setText("" + (seekBar.getProgress() + 1));
			}
			break;

		default:
			break;
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		switch (seekBar.getId()) {
		case R.id.seek_bar_speed:
			PreferenceUtils.setInt(this, Constants.PREFERENCE_KEY_SPEED,
					seekBar.getProgress() + 1);
			break;

		default:
			break;
		}

	}

	public static Intent buildIntent(Context context) {
		return new Intent(context, SlidePreferenceActivity.class);
	}

	@Override
	public void timesFromFile(String text_file) {
		Log.d("JSON", text_file);
		ModelOfPreferenceTime times = JsonUtils.getModelFromJson(text_file);
		if (times != null) {
			updatePreference(times);
			updateUIFromPreference();
		} else {
			Toast.makeText(this, "File is not correct", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void updatePreference(ModelOfPreferenceTime times) {
		PreferenceUtils.setInt(this, Constants.PREFERENCE_KEY_SPEED,
				times.speed);
		PreferenceUtils.setInt(this, Constants.PREFERENCE_KEY_START_TIME_HOUR,
				times.start_time_hour);
		PreferenceUtils.setInt(this,
				Constants.PREFERENCE_KEY_START_TIME_MINUTE,
				times.start_time_minute);
		PreferenceUtils.setInt(this, Constants.PREFERENCE_KEY_END_TIME_HOUR,
				times.end_time_hour);
		PreferenceUtils.setInt(this, Constants.PREFERENCE_KEY_END_TIME_MINUTE,
				times.end_time_minute);
		
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.set(Calendar.HOUR_OF_DAY, 	times.start_time_hour);
		calendarStart.set(Calendar.MINUTE, times.start_time_minute);
		calendarStart.set(Calendar.SECOND, 00);
		
		Calendar calendarStop = Calendar.getInstance();
		calendarStart.set(Calendar.HOUR_OF_DAY, 	times.end_time_hour);
		calendarStart.set(Calendar.MINUTE, times.end_time_minute);
		calendarStart.set(Calendar.SECOND, 00);
		
		alarmReceiver.setOnetimeTimer(this, calendarStart.getTimeInMillis(), true);
		alarmReceiver.setOnetimeTimer(this, calendarStop.getTimeInMillis(), false);
		
		
	}

}
