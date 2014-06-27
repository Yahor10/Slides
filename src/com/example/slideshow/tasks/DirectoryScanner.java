package com.example.slideshow.tasks;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.slideshow.DirectoryActivity;
import com.example.slideshow.R;
import com.example.slideshow.app.Constants;
import com.example.slideshow.data.DirectoryEntry;
import com.example.slideshow.data.IconifiedText;

import static com.example.slideshow.app.Constants.TAG;

public class DirectoryScanner extends Thread {

	private File mCurrFolder;
	private Context mContext;
	private Handler mMessageHandler;

	public DirectoryScanner(File mCurrFolder, Context mContext,
			Handler mMessageHandler) {
		super();
		this.mCurrFolder = mCurrFolder;
		this.mContext = mContext;
		this.mMessageHandler = mMessageHandler;
	}

	private void clearData() {
		mContext = null;
		mMessageHandler = null;
	}

	private static class ImageFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String filename) {
			String extension = com.example.slideshow.utils.FileUtils
					.getExtension(filename);
			if (extension == null) {
				return false;
			}
			if (extension.equals("jpg") || extension.equals("png")
					|| extension.equals("jpeg")) {
				return true;
			} else {
				return false;
			}
		}

	}

	@Override
	public void run() {

		Collection<File> listFilesAndDirs = FileUtils.listFilesAndDirs(
				mCurrFolder, TrueFileFilter.INSTANCE,
				DirectoryFileFilter.DIRECTORY);
		Log.v(com.example.slideshow.app.Constants.TAG, "START SCAN"
				+ mCurrFolder.getName());

		int totalCount = 0;
		totalCount = (listFilesAndDirs != null) ? listFilesAndDirs.size() : 0;

		Log.v(TAG, "count:" + totalCount);

		List<File> folders1 = new ArrayList<File>(totalCount);
		List<IconifiedText> folders2 = new ArrayList<IconifiedText>(totalCount);

		List<File> files1 = new ArrayList<File>(totalCount);
		List<IconifiedText> files2 = new ArrayList<IconifiedText>(totalCount);

		Drawable fullFolderIcon = mContext.getResources().getDrawable(
				R.drawable.ic_folder);
		Drawable fileIcon = mContext.getResources().getDrawable(
				R.drawable.ic_file);

		for (File currentFile : listFilesAndDirs) {
			if (currentFile.canRead()) {
				folders1.add(currentFile);
			} else {
				files1.add(currentFile);
			}
		}

		for (File folder : folders1) {
			String[] fileNames = folder.list(new ImageFilter());
			if (fileNames == null) {
				continue;
			}
			int imageCount = fileNames.length;
			String summary = String.format("%d %s", imageCount,
					(imageCount == 1) ? "image" : "images");

			if (imageCount > 0) {
				folders2.add(new IconifiedText(fullFolderIcon,
						folder.getName(), summary, folder.getAbsolutePath(),
						imageCount > 0));
			}
		}

		for (File file : files1) {
			String name = file.getName();
			String summary = Long.toString(file.length() / 1024).concat("KB");
			files2.add(new IconifiedText(fileIcon, name, summary, file
					.getAbsolutePath(), false));
		}

		DirectoryEntry contents = new DirectoryEntry();
		contents.directories = folders2;
		contents.files = files2;
		Message message = mMessageHandler
				.obtainMessage(DirectoryActivity.MSG_ID_SHOW_DIR_CONTENTS);
		message.obj = contents;
		message.sendToTarget();

		super.run();
		clearData();
	}
}
