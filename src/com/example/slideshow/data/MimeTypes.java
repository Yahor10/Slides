package com.example.slideshow.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

public class MimeTypes {
	private Map<String, String> mMimeTypes;
	private Map<String, Integer> mIcons;

	public MimeTypes() {
		mMimeTypes = new HashMap<String, String>();
		mIcons = new HashMap<String, Integer>();
	}

	public void put(String type, String extension, int icon) {
		put(type, extension);
		mIcons.put(extension, icon);
	}

	public void put(String type, String extension) {
		extension = extension.toLowerCase();
		mMimeTypes.put(type, extension);
	}
	
	private String getFileExtension(String fileName, boolean withDot) {
		String ext = null;
		if (fileName != null) {
			ext = FilenameUtils.getExtension(fileName);
			if (!TextUtils.isEmpty(ext)) {
				boolean hasDot = ext.contains(".");
				if ((hasDot && withDot) || (!hasDot && !withDot)) {
					return ext;
				}
				if (hasDot && !withDot) {
					return ext.substring(1, ext.length());
				}
				if (!hasDot && withDot) {
					return "." + ext;
				}
			}
		}
		return ext;
	}

	public String getMimeType(String fileName) {
		String mimeType = null;
		String ext = getFileExtension(fileName, false);
		// Let's first check the bilt-in Webkit extension-to-MIME map
		if (!TextUtils.isEmpty(ext)) {
//			ext = ext.substring(1, ext.length());
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
			if (mimeType != null) {
				return mimeType;
			}
			//Extension with "." for check in mimeTypes
			ext = getFileExtension(fileName, true);
			ext = ext.toLowerCase();
		}
		
		mimeType = mMimeTypes.get(ext);
		if (mimeType == null) {
			mimeType = "*/*";
		}
		return mimeType;
	}

	public int getIcon(String mimeType) {
		Integer iconId = mIcons.get(mimeType);
		return (iconId != null) ? iconId : 0;
	}
}
