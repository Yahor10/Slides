package com.example.slideshow.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.SyncStateContract.Constants;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.View.MeasureSpec;
import android.widget.TextView;


public class FileUtils {
	public static String getExtension(String uri) {
		String ext = null;
		if (uri != null) {
			int index = uri.lastIndexOf(".");
			if (index >= 0) {
				ext = uri.substring(index+1);
			}
		}
		return ext;
	}
	
	public static String cutExtension(String currentFile) {
		String cut = currentFile;
		if (!TextUtils.isEmpty(currentFile)) {
			int index = currentFile.lastIndexOf(".");
			if (index >= 0) {
				cut = cut.substring(0, index);
			}
		}
		return cut;
	}
	
	public static String addExtension(String newFileName, String savedExtension) {
		if (!TextUtils.isEmpty(newFileName)
				&& !TextUtils.isEmpty(savedExtension)) {
			return newFileName + "." + savedExtension;
		}
		return newFileName;
	}
	
	public static Uri getUri(File file) {
		return (file != null) ? Uri.fromFile(file) : null;
	}

	public static File getFile(Uri uri) {
		String filePath = null;
		if (uri != null)
			filePath = uri.getPath();

		return (filePath != null) ? new File(filePath) : null;
	}

	public static File getFile(String path, String fileName) {
		String filePath = String.format("%s%s%s", path, path.endsWith("/") ? ""
				: "/", fileName);
		return new File(filePath);
	}

	public static File getFile(File directory, String fileName) {
		return getFile(directory.getAbsolutePath(), fileName);
	}

	public static String formatSize(Context context, long sizeInBytes) {
		return Formatter.formatFileSize(context, sizeInBytes);
	}
	
	public static String formatFilePath(String path) {
		String[] split = path.split("/");
		StringBuffer buffer = new StringBuffer();
		buffer.append("/");
		buffer.append(split[2]);		
		if(split.length > 5){
   	    buffer.append("/");
		buffer.append("...");
		}		
		if(split.length-2 != 2){
			buffer.append("/");
	    	buffer.append(split[split.length - 2]);
		}
		
		return buffer.toString();
	}
	
	public static String formatFilePath(String path, TextView textView) {
		textView.setSingleLine();    		
		textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		path = initPath(path);
		if (isStringFitsTextview(path, textView)
				|| isPathScpecialCase(path, textView)) {
			return path;
		} else {
			return getSiftedPath(path, textView);
		}
	}
	
	public static String formatFileName(String name) {
		if(name.length() > 20){
			return name.substring(0, 20);
		}else{
			return name;
		}
	}

	public static String formatDate(Context context, long dateTime) {
		Date date = new Date(dateTime);
		return DateFormat.getDateFormat(context).format(date);
	}
	
	public static String formatFileDate(Context context, long dateTime) {
		Date date = new Date(dateTime);
		return String.format("%1s, %2s", DateFormat.getDateFormat(context).format(date),
										   DateFormat.getTimeFormat(context).format(date));
	}

	public static float measureStringWidthInPixels(String string, float textSize, Typeface typeFace) {
		Paint mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(5);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setTextSize(textSize);
		mPaint.setTypeface(typeFace);
		return mPaint.measureText(string, 0, string.length());
	}
	
	public static int measureStringWidthInDIP(String string, float textSize, Typeface typeFace, Context context) {
		float stringWidthInPixels = measureStringWidthInPixels(string, textSize, typeFace);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int) (stringWidthInPixels / metrics.density);
	}
	
	public static boolean isStringFitsTextview(String string, TextView textView) {
		textView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
				, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int textViewWidth = textView.getMeasuredWidth();
		Typeface textTypeface = textView.getTypeface();
		float textSize = textView.getTextSize();
		int stringSizeDIP = measureStringWidthInDIP(string, textSize, textTypeface, textView.getContext());
		return (stringSizeDIP + 4) < textViewWidth;
	}
	
	public static String initPath(String path) {
		return path.substring(4, path.length());
	}
	
	public static String compactifyPath(String initialPath) {
		StringBuffer compactPath = new StringBuffer();
		String[] split = truncatePath(initialPath).split("/");
		List<String> pathElems = Arrays.asList(split);
		pathElems.set(1, "...");
		return rebuildPathFromSplit(pathElems);
	}
	
	public static boolean isPathScpecialCase(String path, TextView textView) {
		boolean result = false;
		String[] split = truncatePath(path).split("/");
		if (!isStringFitsTextview(path, textView)
				&& split.length == 2) {
			result = true;
		}
		return result;
	}
	
	public static String getSiftedPath(String initialPath, TextView textView) {
		String compactPath = compactifyPath(initialPath);
		while (!isCompactPathMinimal(compactPath)
				&& !isStringFitsTextview(compactPath, textView)) {
			compactPath = removePathElement(compactPath);
		}
		return compactPath;
	}
	
	public static String rebuildPathFromSplit(List<String> pathElems) {
		StringBuffer resultPath = new StringBuffer();
		for (String pathElem : pathElems) {
			resultPath.append("/");
			resultPath.append(pathElem);
		}
		return resultPath.toString();
	}
	
	public static boolean isCompactPathMinimal(String compactPath) {
		return truncatePath(compactPath).split("/").length < 4;
	}
	
	public static String removePathElement(String compactPath) {
		String[] split = truncatePath(compactPath).split("/");
		ArrayList<String> pathElems = new ArrayList<String>(Arrays.asList(split));
		pathElems.remove(2);
		return rebuildPathFromSplit(pathElems);
	}
	
	public static String truncatePath(String initialPath) {
		return initialPath.substring(1, initialPath.length());
	}
	
}
