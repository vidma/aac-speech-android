package com.epfl.android.aac_speech.lib;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import com.epfl.android.aac_speech.MainActivity;

public class ImageUtils {

	public static final boolean DOWNSCALE_MOBILE = false;

	/**
	 * Google Android's forumns indicate that using
	 * BitmapFactory.decodeFileDescriptor over BitmapFactory.decodeFile may
	 * solve out-of-memory issues. That realy seems to be the case.
	 * 
	 * @param URI
	 * @return
	 */
	
	@SuppressWarnings("unused")
	private static BitmapFactory.Options getBitmapOptions(){
		/* scale level to further reduce memory consumption */
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = (!MainActivity.isTablet && DOWNSCALE_MOBILE) ? 2 : 1;
		return options;
	}
	
	public static Bitmap getBitmapFromURI(String URI, Context context) {
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(URI);
		FileDescriptor fd;
		try {
			fd = resolver.openFileDescriptor(uri, "r").getFileDescriptor();
			Bitmap bMap = BitmapFactory.decodeFileDescriptor(fd, new Rect(0, 0, 0, 0), getBitmapOptions());
			return bMap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @param URI
	 * @return
	 */
	public static Bitmap getBitmapFromURI(String URI) {
		String imageURI = URI.replace("file://", "");
		// options.inPurgeable = true;
		// options.inInputShareable = true;
		Bitmap bMap = BitmapFactory.decodeFile(imageURI, getBitmapOptions());
		return bMap;
	}

}
