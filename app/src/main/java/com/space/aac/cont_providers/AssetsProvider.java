package com.space.aac.cont_providers;

// based on: http://stackoverflow.com/questions/5888718/android-share-images-from-assets-folder

import android.content.ContentProvider;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import java.io.FileNotFoundException;
import android.content.ContentValues;
import android.database.Cursor;
import java.io.IOException;


public class AssetsProvider extends ContentProvider {
	private static String TAG = "AssetsProvider";
	// TODO: can we take URI from manifest?
	public static String URI = "content://com.space.aac.icons-data/";
	//content://com.space.aac.icons-data/

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
		final long token = Binder.clearCallingIdentity();
		Log.v(TAG, "AssetsGetter: Open asset file");
		AssetManager am = getContext().getAssets();
		String file_path = uri.getPath();
		if (file_path == null)
			throw new FileNotFoundException();
		if (file_path.startsWith("/"))
			file_path = file_path.substring(1);
		
		// serve the file
		AssetFileDescriptor afd = null;
		try {
			afd = am.openFd(file_path);
		} catch (IOException e) {
			// TODO: how to handle file not found gracefully? serve a default file?
			e.printStackTrace();
			return super.openAssetFile(uri, mode);
		}
		return afd;
	}

	@Override
	public String getType(Uri p1) {
		// TODO: Implement this method
		return null;
	}


	@Override
	public Cursor query(Uri p1, String[] p2, String p3, String[] p4, String p5) {
		// TODO: Implement this method
		return null;
	}


	@Override
	public boolean onCreate() {

		Log.v(TAG, "content provider create method");
		// TODO: Implement this method
		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}