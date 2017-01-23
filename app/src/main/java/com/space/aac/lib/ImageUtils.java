package com.space.aac.lib;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;

import com.space.aac.MainActivity;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by space on 29/7/16.
 */
public class ImageUtils {

    public static final boolean DOWNSCALE_MOBILE = false;
    public static final String ASSETS_LOCATION = "file:///android_asset/";
    /*"file:///android_asset/"    */

    /**
     * Android's forumns indicate that using BitmapFactory.decodeFileDescriptor over BitmapFactory.decodeFile
     *  may solve out-of-memory issues. This seem to be true.
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
            //System.out.println("URI:"+URI);
            FileDescriptor fd;
            if (URI.startsWith(ASSETS_LOCATION)) {
                Uri parsed_uri = Uri.parse(URI);
                System.out.println("URI PATH:" + parsed_uri.getEncodedAuthority() + parsed_uri.getEncodedPath());
                String path = URI.replace(ASSETS_LOCATION, "");
                return BitmapFactory.decodeStream(context.getAssets().open(path));
            } else {
                if (URI.startsWith("/")) {
                    URI = "file://" + URI;
                }
                ContentResolver resolver = context.getContentResolver();
                fd = resolver.openFileDescriptor(Uri.parse(URI), "r").getFileDescriptor();
            }
            return BitmapFactory.decodeFileDescriptor(fd, new Rect(0, 0, 0, 0), getBitmapOptions());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
