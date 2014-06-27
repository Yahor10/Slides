package com.example.slideshow.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;


public class MimeTypeParser
{
	public static final String TAG_MIMETYPES = "MimeTypes";
	public static final String TAG_TYPE = "type";
	
	public static final String ATTR_EXTENSION = "extension";
	public static final String ATTR_MIMETYPE = "mimetype";
	public static final String ATTR_ICON = "icon";
	
	private XmlPullParser mXmlParser;
	private MimeTypes mMimeTypes;
	private Resources appResources;
	private String pkgName;
    
	public MimeTypeParser(Context context, String pkgName)
		throws NameNotFoundException
	{
		this.pkgName = pkgName;
		this.appResources = context.getPackageManager().getResourcesForApplication(pkgName);
	}
	
	public MimeTypes fromXml(InputStream is)
		throws XmlPullParserException, IOException
	{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

		mXmlParser = factory.newPullParser();
		mXmlParser.setInput(new InputStreamReader(is));

		return parse();
	}
	
	public MimeTypes fromXmlResource(XmlResourceParser xmlParser)
		throws XmlPullParserException, IOException
	{
		mXmlParser = xmlParser;
		return parse();
	}

	public MimeTypes parse()
		throws XmlPullParserException, IOException
	{
		mMimeTypes = new MimeTypes();
		
		int eventType = mXmlParser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String xmlElement = mXmlParser.getName();

			if (eventType == XmlPullParser.START_TAG) {
				if (xmlElement.equals(TAG_MIMETYPES)) {
					// ..?
				}
				else
				if (xmlElement.equals(TAG_TYPE)) {
					addMimeTypeStart();
				}
			}
			else
			if (eventType == XmlPullParser.END_TAG) {
				if (xmlElement.equals(TAG_MIMETYPES)) {
					// ..?
				}
			}
			eventType = mXmlParser.next();
		}

		return mMimeTypes;
	}
	
	private void addMimeTypeStart() {
		String extension = mXmlParser.getAttributeValue(null, ATTR_EXTENSION);
		String mimeType = mXmlParser.getAttributeValue(null, ATTR_MIMETYPE);
		String icon = mXmlParser.getAttributeValue(null, ATTR_ICON);
		
		if (icon != null) {
			int iconId = appResources.getIdentifier(icon.substring(1), null, pkgName);
			if (iconId > 0) {
				mMimeTypes.put(extension, mimeType, iconId);
				return;
			}
		}
		
		mMimeTypes.put(extension, mimeType);
	}
}
