package com.example.slideshow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

import static com.example.slideshow.app.Constants.TAG;

;

public class SelectImageActivity extends ActionBarActivity implements
		OnItemClickListener {

	public static final int ACCEPT_IMAGES = 1230;
	private ListView mList;
	private SimpleAdapter adapter;

	final String ATTRIBUTE_NAME_TEXT = "text";
	final String ATTRIBUTE_NAME_IMAGE = "image";
	final String ATTRIBUTE_NAME_IMAGE_PATH = "path";
	final String ATTRIBUTE_NAME_CHECKBOX = "checkbox";

	public static final String FOLDER_NAME = "folder_name";
	private static final int MENU_ID_ACCEPT = 2;
	private static final int MENU_ID_SELECT_ALL = 3;

	private Set<String> mImageSet = new HashSet<String>();

	public static Intent buildIntent(Context context, String folderName) {
		Intent intent = new Intent(context, SelectImageActivity.class);
		intent.putExtra(FOLDER_NAME, folderName);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_images);
		mList = (ListView) findViewById(R.id.imageList);

		Intent intent = getIntent();
		String folderName = intent.getStringExtra(FOLDER_NAME);

		Log.v(TAG, "folder name" + folderName);
		ImageTask task = new ImageTask();
		task.execute(folderName);

		mList.setOnItemClickListener(this);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onBackPressed() {
		setResult(0);
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_images, menu);
		// MenuItem acceptMenuItem = menu.add(0, MENU_ID_ACCEPT, Menu.NONE, "");
		//
		// MenuItem selectAll = menu.add(0, MENU_ID_SELECT_ALL, Menu.NONE,
		// "Select all");
		// selectAll.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//
		// acceptMenuItem.setIcon(R.drawable.ic_accept).setShowAsAction(
		// MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	private boolean selectedAll = false;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		int size = resultData.size();
		switch (itemId) {
		case R.id.menu_accept:
			// save to slide show
			if (mImageSet.isEmpty()) {
				Toast.makeText(this, "NO IMAGES HAS BEEN SELECTED",
						Toast.LENGTH_SHORT).show();
				break;
			}

			Intent result = new Intent();
			String[] array = mImageSet.toArray(new String[mImageSet.size()]);
			Bundle bundle = new Bundle();
			bundle.putStringArray(MainActivity.IMAGE_PATHS, array);
			result.putExtras(bundle);

			setResult(ACCEPT_IMAGES, result);
			finish();

			mImageSet.clear();

			break;
		case R.id.menu_select_all:

			selectedAll = true;
			for (int i = 0; i < size; i++) {
				HashMap<String, Object> map = (HashMap<String, Object>) resultData
						.get(i);
				Boolean checked = (Boolean) map.get(ATTRIBUTE_NAME_CHECKBOX);
				String imagePath = (String) map.get(ATTRIBUTE_NAME_IMAGE_PATH);
				if (!checked) {
					map.put(ATTRIBUTE_NAME_CHECKBOX, true);
					selectedAll = false;
					mImageSet.add(imagePath);
				} else {
					mImageSet.remove(imagePath);
				}
			}

			if (selectedAll) {
				for (int i = 0; i < size; i++) {
					HashMap<String, Object> map = (HashMap<String, Object>) resultData
							.get(i);
					Boolean checked = (Boolean) map
							.get(ATTRIBUTE_NAME_CHECKBOX);
					String imagePath = (String) map
							.get(ATTRIBUTE_NAME_IMAGE_PATH);
					mImageSet.remove(imagePath);
					checked = !checked;
					map.put(ATTRIBUTE_NAME_CHECKBOX, false);
				}
			}

			adapter.notifyDataSetChanged();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		HashMap<String, Object> map = (HashMap<String, Object>) resultData
				.get(position);

		Boolean checked = (Boolean) map.get(ATTRIBUTE_NAME_CHECKBOX);
		String imagePath = (String) map.get(ATTRIBUTE_NAME_IMAGE_PATH);
		Object imageObject = map.get(ATTRIBUTE_NAME_IMAGE);
		if (imageObject == null) {
			Log.e(TAG, "IMAGE NULL");
		}
		checked = !checked;
		if (checked) {
			mImageSet.add(imagePath);
		} else {
			mImageSet.remove(imagePath);
		}

		Log.v(TAG, "image path" + imagePath);
		map.put(ATTRIBUTE_NAME_CHECKBOX, checked);
		adapter.notifyDataSetChanged();
	}

	private ArrayList<Map<String, Object>> resultData;

	private class ImageTask extends
			AsyncTask<String, Void, ArrayList<Map<String, Object>>> {

		final String[] imageExtensions = { "jpg", "png", "jpeg" };

		private final ViewBinder viewBinder = new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {

				if (data == null) {
					ImageView i = (ImageView) view;
					i.setImageResource(R.drawable.ic_launcher);
					return true;
				}

				if (data instanceof Boolean && view instanceof CheckBox) {
					CheckBox checkBox = (CheckBox) view;
					Boolean check = (Boolean) data;
					if (!mImageSet.isEmpty()) {
						String format = String.format("%d images",
								mImageSet.size());
						setTitle(format);
					} else {
						setTitle("0 images");
					}
					checkBox.setChecked(check);
				}

				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView i = (ImageView) view;
					if (data != null) {
						i.setImageBitmap((Bitmap) data);
					}
					return true;
				} else {

				}
				return false;
			}
		};

		protected void onPreExecute() {
			mImageSet.clear();
		};

		@Override
		protected ArrayList<Map<String, Object>> doInBackground(
				String... params) {

			String path = params[0];
			File file = new File(path);
			Collection<File> listFiles = FileUtils.listFiles(file,
					imageExtensions, true);
			File[] arr = listFiles.toArray(new File[listFiles.size()]);

			ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
					listFiles.size());

			for (int i = 0; i < arr.length; i++) {
				Map<String, Object> mapData;
				mapData = new HashMap<String, Object>();
				String name = arr[i].getName();
				mapData.put(ATTRIBUTE_NAME_TEXT, name);
				String imagePath = arr[i].getAbsolutePath();
				mapData.put(ATTRIBUTE_NAME_IMAGE_PATH, imagePath);
				Bitmap decodeFile = BitmapFactory.decodeFile(imagePath);
				mapData.put(ATTRIBUTE_NAME_IMAGE, decodeFile);
				mapData.put(ATTRIBUTE_NAME_CHECKBOX, false);
				data.add(mapData);
			}

			return data;
		}

		@Override
		protected void onPostExecute(ArrayList<Map<String, Object>> result) {
			super.onPostExecute(result);

			resultData = result;
			// массив имен атрибутов, из которых будут читаться данные
			String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE,
					ATTRIBUTE_NAME_CHECKBOX };
			// массив ID View-компонентов, в которые будут вставлять данные
			int[] to = { R.id.item_title, R.id.item_icon, R.id.checkedImage };

			// создаем адаптер
			adapter = new SimpleAdapter(SelectImageActivity.this, result,
					R.layout.list_image_item, from, to);
			adapter.setViewBinder(viewBinder);
			mList.setAdapter(adapter);
		}
	}
}
