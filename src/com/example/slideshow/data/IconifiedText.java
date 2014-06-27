package com.example.slideshow.data;

import android.graphics.drawable.Drawable;

public class IconifiedText {

	private Drawable mIcon;
	private String mTitle;
	private String mSummary;
	private String absolutePath;
	private boolean mHasImages;

	public IconifiedText(Drawable mIcon, String mTitle, String mSummary,String path,boolean hasImages) {
		this.mIcon = mIcon;
		this.mTitle = mTitle;
		this.mSummary = mSummary;
		this.absolutePath = path;
		this.mHasImages = hasImages;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public void setIcon(Drawable mIcon) {
		this.mIcon = mIcon;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getSummary() {
		return mSummary;
	}

	public void setSummary(String mSummary) {
		this.mSummary = mSummary;
	}

	public boolean hasImages() {
		return mHasImages;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	
}
