package com.example.slideshow.dialogs;

import interfaces.CallBackFromFIleDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.Log;

import com.example.slideshow.data.ModelOfPreferenceTime;
import com.example.slideshow.dialogs.ListenerList.FireHandler;

public class FileDialog {
	private static final String PARENT_DIR = "/..";
	// private final String TAG = getClass().getName();
	private String[] fileList;
	private File currentPath;

	public interface FileSelectedListener {
		void fileSelected(File file);
	}

	public interface DirectorySelectedListener {
		void directorySelected(File directory);
	}

	private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<FileSelectedListener>();
	private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<DirectorySelectedListener>();
	private final Context context;
	private boolean selectDirectoryOption;
	private String fileEndsWith;
	private CallBackFromFIleDialog callBackToActivity;

	/**
	 * @param activity
	 * @param initialPath
	 */
	public FileDialog(Context context, File path,
			CallBackFromFIleDialog callbackToactivity) {
		this.context = context;
		this.callBackToActivity = callbackToactivity;
		if (!path.exists())
			path = Environment.getExternalStorageDirectory();
		loadFileList(path);
	}

	/**
	 * @return file dialog
	 */
	public Dialog createFileDialog() {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(currentPath.getPath());
		if (selectDirectoryOption) {
			builder.setPositiveButton("Select directory",
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Log.d("", currentPath.getPath());
							fireDirectorySelectedEvent(currentPath);
						}
					});
		}

		builder.setItems(fileList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String fileChosen = fileList[which];
				File chosenFile = getChosenFile(fileChosen);
				if (chosenFile.isDirectory()) {
					loadFileList(chosenFile);
					dialog.cancel();
					dialog.dismiss();
					showDialog();
				} else {
					fireFileSelectedEvent(chosenFile);

					callBackToActivity
							.timesFromFile(readFileAsString(chosenFile));
				}

			}
		});

		dialog = builder.show();
		return dialog;
	}

	public String readFileAsString(File file) {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(file));
			while ((line = in.readLine()) != null)
				stringBuilder.append(line);

		} catch (FileNotFoundException e) {
			Log.d("file_error", e.toString());
		} catch (IOException e) {
			Log.d("file_error", e.toString());
		}

		return stringBuilder.toString();
	}

	public void addFileListener(FileSelectedListener listener) {
		fileListenerList.add(listener);
	}

	public void removeFileListener(FileSelectedListener listener) {
		fileListenerList.remove(listener);
	}

	public void setSelectDirectoryOption(boolean selectDirectoryOption) {
		this.selectDirectoryOption = selectDirectoryOption;
	}

	public void addDirectoryListener(DirectorySelectedListener listener) {
		dirListenerList.add(listener);
	}

	public void removeDirectoryListener(DirectorySelectedListener listener) {
		dirListenerList.remove(listener);
	}

	/**
	 * Show file dialog
	 */
	public void showDialog() {
		createFileDialog().show();
	}

	private void fireFileSelectedEvent(final File file) {
		fileListenerList.fireEvent(new FireHandler<FileSelectedListener>() {
			public void fireEvent(FileSelectedListener listener) {
				listener.fileSelected(file);
			}
		});
	}

	private void fireDirectorySelectedEvent(final File directory) {
		dirListenerList.fireEvent(new FireHandler<DirectorySelectedListener>() {
			public void fireEvent(DirectorySelectedListener listener) {
				listener.directorySelected(directory);
			}
		});
	}

	private void loadFileList(File path) {
		this.currentPath = path;
		List<String> r = new ArrayList<String>();
		if (path.exists()) {
			if (path.getParentFile() != null)
				r.add(PARENT_DIR);
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					if (!sel.canRead())
						return false;
					if (selectDirectoryOption)
						return sel.isDirectory();
					else {
						boolean endsWith = fileEndsWith != null ? filename
								.toLowerCase().endsWith(fileEndsWith) : true;
						return endsWith || sel.isDirectory();
					}
				}
			};
			String[] fileList1 = path.list(filter);
			for (String file : fileList1) {
				r.add(file);
			}
		}
		fileList = (String[]) r.toArray(new String[] {});
	}

	private File getChosenFile(String fileChosen) {
		if (fileChosen.equals(PARENT_DIR))
			return currentPath.getParentFile();
		else
			return new File(currentPath, fileChosen);
	}

	public void setFileEndsWith(String fileEndsWith) {
		this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase()
				: fileEndsWith;
	}
}

class ListenerList<L> {
	private List<L> listenerList = new ArrayList<L>();

	public interface FireHandler<L> {
		void fireEvent(L listener);
	}

	public void add(L listener) {
		listenerList.add(listener);
	}

	public void fireEvent(FireHandler<L> fireHandler) {
		List<L> copy = new ArrayList<L>(listenerList);
		for (L l : copy) {
			fireHandler.fireEvent(l);
		}
	}

	public void remove(L listener) {
		listenerList.remove(listener);
	}

	public List<L> getListenerList() {
		return listenerList;
	}
}
