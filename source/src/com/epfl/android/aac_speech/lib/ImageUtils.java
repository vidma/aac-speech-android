package com.epfl.android.aac_speech.lib;

import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import com.epfl.android.aac_speech.MainActivity;

public class ImageUtils {

	public static final boolean DOWNSCALE_MOBILE = false;
	public static final String ASSETS_LOCATION = "file:///android_asset/";

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

		try {
			System.out.println("URI:"+URI);
			if (URI.startsWith(ASSETS_LOCATION)){
				String path = URI.replace(ASSETS_LOCATION, "");
				return BitmapFactory.decodeStream(context.getAssets().open(path));
			} else {
				ContentResolver resolver = context.getContentResolver();
				FileDescriptor fd = resolver.openFileDescriptor(Uri.parse(URI), "r").getFileDescriptor();
				return BitmapFactory.decodeFileDescriptor(fd, new Rect(0, 0, 0, 0), getBitmapOptions());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
