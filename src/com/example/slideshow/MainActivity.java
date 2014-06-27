package com.example.slideshow;

import static com.example.slideshow.app.Constants.TAG;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFileSystem;
import com.example.slideshow.app.Constants;
import com.example.slideshow.customview.FixedSpeedScroller;
import com.example.slideshow.customview.SlideViewPager;
import com.example.slideshow.receiver.AlarmManagerBroadcastReceiver;
import com.example.slideshow.utils.PreferenceUtils;

public class MainActivity extends ActionBarActivity implements OnClickListener,
		OnTouchListener {

	private static final int UNBLOCK_PAGGING = 6;

	private static final int BLOCK_PAGGING = 5;

	public static final String IMAGE_PATHS = "image paths";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	SlideViewPager mViewPager;
	private static String[] slides = new String[1];

	private Timer timer = null;
	private boolean started = false;

	private GestureDetectorCompat mDetector;

	private static final String appKey = "k7nownkcqpeez9b";
	private static final String appSecret = "y2k4wcvjq5z9eql";
	private DbxAccountManager mDbxAcctMgr;
	private static final int REQUEST_LINK_TO_DBX = 200;

	private Handler paggingHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case BLOCK_PAGGING:
				mViewPager.setPagingEnabled(false);
				break;
			case UNBLOCK_PAGGING:
				mViewPager.setPagingEnabled(true);
				break;
			default:
				break;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (SlideViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// startActivityForResult(DirectoryActivity.buildIntent(this), 987);

		mViewPager.setPageTransformer(true, new DepthPageTransformer());

		registerReceiver(chargeReceiver, new IntentFilter(
				Intent.ACTION_POWER_CONNECTED));

		registerReceiver(closeReceiver, new IntentFilter(CLOSE_ACTION));

		mViewPager.setOnClickListener(this);

		mViewPager.setOnTouchListener(this);

		mDetector = new GestureDetectorCompat(this, new SlideGestureListener());
		initScroll();

		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
				appKey, appSecret);

		Intent intent = getIntent();
		if (intent != null) {
			boolean startPresentation = intent.getBooleanExtra(
					AlarmManagerBroadcastReceiver.START_PRESENTATION, false);
			if (startPresentation) {
				startSlideShowTimer();
				toggleHideyBar();
				hideActionBar();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(SlidePreferenceActivity.buildIntent(this));
			return true;
		}

		if (id == R.id.menu_dropbox_sync) {
			linkToDropbox();
			return true;
		}

		if (id == R.id.menu_startstop) {
			Log.v(TAG, "started" + started);
			if (started) {
				return true;
			}

			if (TextUtils.isEmpty(slides[0])) {
				Toast.makeText(this, "No slides for presentation",
						Toast.LENGTH_SHORT).show();
				return true;
			}

			if (slides.length == 1) {
				hideActionBar();
				toggleHideyBar();
				return true;
			}

			int count = mViewPager.getAdapter().getCount();

			if (mViewPager.getCurrentItem() == count - 1) {
				mViewPager.setCurrentItem(0);
			}

			hideActionBar();
			toggleHideyBar();

			stopSlideShowTimer();
			startSlideShowTimer();
		}

		if (id == R.id.menu_add_slideshow) {
			startActivityForResult(DirectoryActivity.buildIntent(this), 987);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int request, int result, Intent intent) {
		Log.v(TAG, "onActivityResult main activity");

		if (request == REQUEST_LINK_TO_DBX) {
			if (result == Activity.RESULT_OK) {
				Log.v(TAG, "onActivityResult main activity REQUEST_LINK_TO_DBX");
				linkToDropbox();
			}
		}

		if (intent != null && result == SelectImageActivity.ACCEPT_IMAGES) {
			Log.v(TAG, "onActivityResult main activity ACCEPT_IMAGES");

			Bundle extras = intent.getExtras();
			slides = extras.getStringArray(IMAGE_PATHS);
			Log.v(TAG, "slides length" + slides.length);

			mSectionsPagerAdapter = new SectionsPagerAdapter(
					getSupportFragmentManager());
			mViewPager.setAdapter(mSectionsPagerAdapter);
			mViewPager.setCurrentItem(0);

			hideActionBar();
			toggleHideyBar();

			stopSlideShowTimer();
			startSlideShowTimer();
		}
		super.onActivityResult(request, result, intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and
		// higher, but as
		// a general rule, you should design your app to hide the status bar
		// whenever you
		// hide the navigation bar.
		// final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		// | View.SYSTEM_UI_FLAG_FULLSCREEN;
		registerReceiver(stopReceiver, new IntentFilter(STOP_ACTION));

		Log.v(TAG, "CODE VERSION" + android.os.Build.VERSION.SDK_INT);
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			decorView
					.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

						@Override
						public void onSystemUiVisibilityChange(int visibility) {

							if (!started) {
								return;
							}
							// toggleHideyBar();
							if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
								// TODO: The system bars are visible. Make any
								// desired
								// adjustments to your UI, such as showing the
								// action bar or
								// other navigational controls.
								Log.v(TAG,
										"onSystemUiVisibilityChange visibility");
								toggleHideyBar();

								showActionBar();
							} else {
								Log.v(TAG,
										"onSystemUiVisibilityChange not visibility");
								hideActionBar();
							}
						}
					});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopSlideShowTimer();
		try {
			unregisterReceiver(stopReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(chargeReceiver);
		unregisterReceiver(closeReceiver);
	}

	private void initScroll() {
		try {
			Field mScroller;
			mScroller = ViewPager.class.getDeclaredField("mScroller");
			mScroller.setAccessible(true);
			DecelerateInterpolator sInterpolator = new DecelerateInterpolator();
			FixedSpeedScroller scroller = new FixedSpeedScroller(
					mViewPager.getContext(), sInterpolator);
			// scroller.setFixedDuration(5000);
			mScroller.set(mViewPager, scroller);
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

	private void startSlideShowTimer() {
		if (timer == null) {
			timer = new Timer();
			TimerTask task = new TimerTask() {

				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int count = mViewPager.getAdapter().getCount();
							int currentItem = mViewPager.getCurrentItem();

							if (count == currentItem + 1) {
								stopSlideShowTimer();
							}

							mViewPager.setCurrentItem(currentItem + 1, true);
						}
					});
				}
			};
			timer.schedule(task, 2000, 2000);
			started = true;
		}
	}

	private void stopSlideShowTimer() {
		if (timer != null) {
			timer.purge();
			timer.cancel();
			timer = null;
			started = false;
		}
	}

	private void hideActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
	}

	private void showActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.show();
	}

	private void toggleHideyBar() {

		if (android.os.Build.VERSION.SDK_INT < 14) {
			return;
		}
		// BEGIN_INCLUDE (get_current_ui_flags)
		// The UI options currently enabled are represented by a bitfield.
		// getSystemUiVisibility() gives us that bitfield.
		int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
		int newUiOptions = uiOptions;
		// END_INCLUDE (get_current_ui_flags)
		// BEGIN_INCLUDE (toggle_ui_flags)
		boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
		if (isImmersiveModeEnabled) {
			Log.i(TAG, "Turning immersive mode mode off. ");
		} else {
			Log.i(TAG, "Turning immersive mode mode on.");
		}

		// Navigation bar hiding: Backwards compatible to ICS.
		if (Build.VERSION.SDK_INT >= 14) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
		}

		// Status bar hiding: Backwards compatible to Jellybean
		if (Build.VERSION.SDK_INT >= 16) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
		}

		// Immersive mode: Backward compatible to KitKat.
		// Note that this flag doesn't do anything by itself, it only augments
		// the behavior
		// of HIDE_NAVIGATION and FLAG_FULLSCREEN. For the purposes of this
		// sample
		// all three flags are being toggled together.
		// Note that there are two immersive mode UI flags, one of which is
		// referred to as "sticky".
		// Sticky immersive mode differs in that it makes the navigation and
		// status bars
		// semi-transparent, and the UI flag does not get cleared when the user
		// interacts with
		// the screen.
		if (Build.VERSION.SDK_INT >= 18) {
			newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		}

		getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
		// END_INCLUDE (set_ui_flags)
	}

	private void linkToDropbox() {
		if (!mDbxAcctMgr.hasLinkedAccount()) {
			mDbxAcctMgr.startLink((Activity) this, REQUEST_LINK_TO_DBX);
		} else {
			Intent buildIntent = DropboxImagesActivity.buildIntent(this);
			startActivityForResult(buildIntent, 4321);
		}
	}

	class SlideGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent event) {
			if (touchCount == 0) {
				touchStartTime = System.currentTimeMillis();
			}
			touchCount++;
			if (touchCount == 3) {
				touchCount = 0;
				long currentTime = System.currentTimeMillis();
				long result = currentTime - touchStartTime;

				if (result < 650) {
					// unblock navigation bar
					if (getSupportActionBar().isShowing()) {
						hideActionBar();
						paggingHandler.sendEmptyMessage(BLOCK_PAGGING);
					} else {
						showActionBar();
						paggingHandler.sendEmptyMessage(UNBLOCK_PAGGING);
					}
					toggleHideyBar();
				}
			}
			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			return true;
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position, slides[position]);
		}

		@Override
		public int getCount() {
			return slides.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return "";
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return super.isViewFromObject(view, object);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		private static final String ARG_SECTION_IMAGE_PATH = "section_imagepath";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 * 
		 * @param slides
		 */
		public static PlaceholderFragment newInstance(int sectionNumber,
				String imagePath) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			args.putString(ARG_SECTION_IMAGE_PATH, imagePath);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			Bundle arguments = getArguments();

			int int1 = arguments.getInt(ARG_SECTION_NUMBER);
			String string = Integer.toString(int1 + 1);
			textView.setText(string);
			ImageView imageView = (ImageView) rootView.findViewById(R.id.slide);
			// String imagePath = arguments.getString(ARG_SECTION_IMAGE_PATH);
			Bitmap myBitmap = BitmapFactory.decodeFile(slides[int1]);
			imageView.setImageBitmap(myBitmap);
			return rootView;
		}
	}

	public static class ZoomOutPageTransformer implements
			ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 1) { // [-1,1]
				// Modify the default slide transition to shrink the page as
				// well
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1)
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
						/ (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}

	public static class DepthPageTransformer implements
			ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.75f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 0) { // [-1,0]
				// Use the default slide transition when moving to the left page
				view.setAlpha(1);
				view.setTranslationX(0);
				view.setScaleX(1);
				view.setScaleY(1);

			} else if (position <= 1) { // (0,1]
				// Fade the page out.
				view.setAlpha(1 - position);

				// Counteract the default slide transition
				view.setTranslationX(pageWidth * -position);

				// Scale the page down (between MIN_SCALE and 1)
				float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
						* (1 - Math.abs(position));
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}

	private static class RotationPageTransformer implements
			ViewPager.PageTransformer {

		@Override
		public void transformPage(View page, float arg1) {
			page.setRotationY(arg1 * -30);
		}

	}

	private ChargingOnReceiver chargeReceiver = new ChargingOnReceiver();

	@Override
	public void onClick(View v) {
		Log.v(TAG, "onClick");
	}

	private int touchCount = 0;
	private long touchStartTime = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		this.mDetector.onTouchEvent(event);
		if (started) {
			stopSlideShowTimer();
		}

		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	public class ChargingOnReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Start slideshow", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private StopPresentationReceiver stopReceiver = new StopPresentationReceiver();
	public static final String STOP_ACTION = "STOP_PRESENTATION_ACTION";

	private class StopPresentationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Stop slideshow", Toast.LENGTH_SHORT)
					.show();
			stopSlideShowTimer();
		}
	}

	public static final String CLOSE_ACTION = "CLOSE_ACTION";
	private CloseReceiver closeReceiver = new CloseReceiver();

	private class CloseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "FINISH");
			finish();
		}
	}
}
