/**
 * 
 */
package com.example.slideshow;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFileFilter;

import com.example.slideshow.app.Constants;
import com.example.slideshow.customview.IconifiedView;
import com.example.slideshow.data.DirectoryEntry;
import com.example.slideshow.data.IconifiedText;
import com.example.slideshow.tasks.DirectoryScanner;
import com.kanak.emptylayout.EmptyLayout;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import static com.example.slideshow.app.Constants.TAG;

/**
 * @author User
 * 
 */
public class DirectoryActivity extends ActionBarActivity implements
		OnItemClickListener {

	public static final int MSG_ID_SHOW_DIR_CONTENTS = 120;
	private ListView mList;
	private FilesAdapter adapter;
	private EmptyLayout emptyLayout;

	private Handler mDirectoryHander = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			final List<IconifiedText> data = new ArrayList<IconifiedText>();

			DirectoryEntry entry = (DirectoryEntry) msg.obj;
			if (entry.directories.isEmpty() && entry.files.isEmpty()) {
				emptyLayout.showEmpty();
				return false;
			}
			data.addAll(entry.directories);
			data.addAll(entry.files);

			adapter = new FilesAdapter(data, DirectoryActivity.this);
			mList.setAdapter(adapter);

			return false;
		}
	});

	private FilenameFilter filter = new FilenameFilter() {

		@Override
		public boolean accept(File dir, String filename) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	public static Intent buildIntent(Context context) {
		return new Intent(context, DirectoryActivity.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub

		setContentView(R.layout.activity_directory_list);
		mList = (ListView) findViewById(R.id.directoryList);

		mList.setOnItemClickListener(this);
		emptyLayout = new EmptyLayout(this, mList);

		File externalStorageDirectory = Environment
				.getExternalStorageDirectory();
		String root = Environment.getExternalStorageDirectory().toString();
		Log.v(Constants.TAG, "ROOT" + root);

		emptyLayout.showLoading();

		DirectoryScanner scanner = new DirectoryScanner(
				externalStorageDirectory, this, mDirectoryHander);
		scanner.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private static class FilesAdapter extends BaseAdapter {

		private final List<IconifiedText> mData;
		private final Context mContext;

		public FilesAdapter(List<IconifiedText> mData, Context mContext) {
			super();
			this.mData = mData;
			this.mContext = mContext;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			IconifiedText iconifiedText = mData.get(position);

			IconifiedView view = new IconifiedView(mContext);
			view.setText(iconifiedText.getTitle());
			view.setIcon(iconifiedText.getIcon());
			view.setSummary(iconifiedText.getSummary());

			return view;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		IconifiedText item = (IconifiedText) adapter.getItem(position);
		boolean hasImages = item.hasImages();
		if (hasImages) {
			String absolutePath = item.getAbsolutePath();
			Intent buildIntent = SelectImageActivity.buildIntent(this,
					absolutePath);
			startActivityForResult(buildIntent, 1);
		}
	}

	@Override
	protected void onActivityResult(int req, int res, Intent arg2) {
		super.onActivityResult(req, res, arg2);
		if (res == SelectImageActivity.ACCEPT_IMAGES) {
			setResult(res, arg2);
			finish();
		}
	}

}
