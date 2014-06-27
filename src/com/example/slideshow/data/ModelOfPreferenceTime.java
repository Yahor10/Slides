package com.example.slideshow.data;

public class ModelOfPreferenceTime {
	public int speed;
	public int start_time_hour;
	public int start_time_minute;
	public int end_time_hour;
	public int end_time_minute;

	public ModelOfPreferenceTime() {
		this.speed = 0;
		this.start_time_hour = 0;
		this.start_time_minute = 0;
		this.end_time_hour = 0;
		this.end_time_minute = 0;
	}

	public ModelOfPreferenceTime(int speed, int start_hour, int start_minute,
			int end_hour, int end_minute) {
		this.speed = speed;
		this.start_time_hour = start_hour;
		this.start_time_minute = start_minute;
		this.end_time_hour = end_hour;
		this.end_time_minute = end_minute;
	}
}
