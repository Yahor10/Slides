package com.example.slideshow.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.slideshow.R;

public class IconifiedView extends LinearLayout {

	private ImageView iconView;
	private TextView titleView;
	private TextView summaryView;

	public IconifiedView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.list_item, this, true);

		iconView = (ImageView) findViewById(R.id.iv_item_icon);
		titleView = (TextView) findViewById(R.id.tv_item_title);
		summaryView = (TextView) findViewById(R.id.tv_item_summary);
	}

	public void setIcon(Drawable drawable) {
		iconView.setImageDrawable(drawable);
	}

	public void setText(CharSequence text) {
		titleView.setText(text);
	}

	public void setSummary(CharSequence text) {
		summaryView.setText(text);
	}

}
