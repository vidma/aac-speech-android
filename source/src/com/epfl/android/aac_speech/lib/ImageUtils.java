package com.epfl.android.aac_speech.lib;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.epfl.android.aac_speech.MainActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

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
	public static Bitmap getBitmapFromURI(String URI, Context context) {
		String imagePath = URI.replace("file://", "");
		ContentResolver resolver = context.getContentResolver();
		Uri uri = Uri.parse(URI);
		FileDescriptor fd;
		try {
			fd = resolver.openFileDescriptor(uri, "r").getFileDescriptor();

			/* scale level to further reduce memory consumption */
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = (!MainActivity.isTablet && DOWNSCALE_MOBILE) ? 2 : 1;

			Bitmap bMap = BitmapFactory.decodeFileDescriptor(fd, new Rect(0, 0, 0, 0), options);

			if (false) {
				Log.d("aac/createImageButton", "uri: " + imagePath);
				Log.d("aac/createImageButton", "bitmap: " + bMap);
			}
			return bMap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
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
		BitmapFactory.Options options = new BitmapFactory.Options();

		/* TODO: we may use scale level to further reduce memory consumption */
		options.inSampleSize = (!MainActivity.isTablet && DOWNSCALE_MOBILE) ? 2 : 1;

		// options.inPurgeable = true;
		// options.inInputShareable = true;

		Bitmap bMap = BitmapFactory.decodeFile(imageURI, options);

		if (false) {
			Log.d("aac/createImageButton", "uri: " + imageURI);
			Log.d("aac/createImageButton", "bitmap: " + bMap);
		}
		return bMap;
	}

}
