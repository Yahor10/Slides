/**
 * 
 */
package com.example.slideshow;

import static com.example.slideshow.app.Constants.TAG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.kanak.emptylayout.EmptyLayout;

/**
 * @author User
 * 
 */
public class DropboxImagesActivity extends ActionBarActivity {
	private static final String appKey = "k7nownkcqpeez9b";
	private static final String appSecret = "y2k4wcvjq5z9eql";

	private DbxAccountManager mDbxAcctMgr;

	private ListView mList;
	private DropBoxImagesTask task;
	private DbxFileSystem forAccount;
	private String dropboxFolder;

	private EmptyLayout emptyLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dropbox);
		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
				appKey, appSecret);
		mList = (ListView) findViewById(R.id.dropboxList);
		// getDropboxFilesAsync();
		File externalStorageDirectory = Environment
				.getExternalStorageDirectory();

		dropboxFolder = externalStorageDirectory.getAbsolutePath() + "/"
				+ "dropboxfolder";

		File f = new File(dropboxFolder);
		if (!f.exists()) {
			f.setReadable(true);
			f.mkdir();
		}

		emptyLayout = new EmptyLayout(this, mList);
		task = new DropBoxImagesTask();
		task.execute();
	}

	private class DropBoxImagesTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			emptyLayout.showLoading();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub

			// save to device;
			try {
				forAccount = DbxFileSystem.forAccount(mDbxAcctMgr
						.getLinkedAccount());
				forAccount.awaitFirstSync();
				Log.v(TAG, "START GET FILES");
				List<DbxFileInfo> listFolder = forAccount
						.listFolder(DbxPath.ROOT);

				for (DbxFileInfo dbxFileInfo : listFolder) {
					DbxPath path = dbxFileInfo.path;
					if (!dbxFileInfo.isFolder) {
						DbxFile dbxFile = forAccount.open(path);

						File f = new File(dropboxFolder,
								dbxFileInfo.path.toString());
						FileInputStream readStream = dbxFile.getReadStream();
						FileUtils.copyInputStreamToFile(readStream, f);
						dbxFile.close();
					}
				}
			} catch (Unauthorized e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			return true;
			// put into result
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				startActivityForResult(SelectImageActivity.buildIntent(
						DropboxImagesActivity.this, dropboxFolder), 1561);
			} else {
				Toast.makeText(DropboxImagesActivity.this,
						"Drop box sync failed", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	protected void onActivityResult(int arg0, int result, Intent intent) {
		if (result == SelectImageActivity.ACCEPT_IMAGES && intent != null) {
			setResult(result, intent);
			Log.v(TAG, "FINISH DROP BOX IMAGES");
		}
		finish();
		super.onActivityResult(arg0, result, intent);
	}

	public static Intent buildIntent(MainActivity mainActivity) {
		return new Intent(mainActivity, DropboxImagesActivity.class);
	}

}
