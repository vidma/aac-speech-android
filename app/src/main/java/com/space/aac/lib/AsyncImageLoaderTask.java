package com.space.aac.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by space on 29/7/16.
 *  Based on http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
 */

public class AsyncImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private String url;
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<Context> context;


    public static class AyncLoadedDrawable extends ColorDrawable {
        private final WeakReference<AsyncImageLoaderTask> bitmapDownloaderTaskReference;


        public AyncLoadedDrawable(AsyncImageLoaderTask bitmapDownloaderTask) {
            super(Color.TRANSPARENT);
            bitmapDownloaderTaskReference = new WeakReference<AsyncImageLoaderTask>(bitmapDownloaderTask);
        }

        public AsyncImageLoaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public AsyncImageLoaderTask(ImageView imageView, Context cont) {
        context =  new WeakReference<Context>(cont);
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    // Actual download method, run in the task thread
    // We try to handle the out of memory error and recover
    protected Bitmap doInBackground(String... params) {
        // params comes from the execute() call: params[0] is the uri.
        String uri = params[0];

        try {
            // TODO: not sure if it's good idea to keep context here
            return ImageUtils.getBitmapFromURI(uri, context.get());
        } catch (OutOfMemoryError e) {
            // It may be a good time now to clean up the memmory
            System.gc();
            Log.e("AsyncImageLoaderTask", e.toString());
        }
        return null;
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            ImageView imageView = imageViewReference.get();
            AsyncImageLoaderTask bitmapDownloaderTask = getAsycLoaderTask(imageView);
            // Change bitmap only if this process is still associated with it
            // (as the list could have been scrolled further out this current
            // view)
            if (this == bitmapDownloaderTask) {
                imageView.setImageBitmap(bitmap);
            }
        }
		/*
		 * if (imageViewReference != null) { ImageView imageView =
		 * imageViewReference.get(); if (imageView != null) {
		 * imageView.setImageBitmap(bitmap); } }
		 */
    }

    private static AsyncImageLoaderTask getAsycLoaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AyncLoadedDrawable) {
                AyncLoadedDrawable downloadedDrawable = (AyncLoadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        AsyncImageLoaderTask bitmapDownloaderTask = getAsycLoaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URI is already being loaded.
                return false;
            }
        }
        return true;
    }

    public static void AsycLoadImage(String imagePath, ImageView v, Context cont) {
        if (cancelPotentialDownload(imagePath, v)) {

			/* clean up the old drawable if any to be GC gathered */
            Drawable dr = v.getDrawable();

            if (dr != null && dr instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) dr;
                Bitmap bm = bd.getBitmap();
                if (bm != null) {
                    // Log.d(TAG, "bm.recycle()");
                    bm.recycle();
                    // otherwise, with asynchronous loading, UI would try to
                    // draw
                    // this recycled bitmap, and we'd get an Exception
                    v.setImageBitmap(null);
                }
            }
			/* create an async task and execute it */
            AsyncImageLoaderTask task = new AsyncImageLoaderTask(v, cont);
            AsyncImageLoaderTask.AyncLoadedDrawable downloadedDrawable = new AsyncImageLoaderTask.AyncLoadedDrawable(
                    task);
			/* set the temporal drawable */
            v.setImageDrawable(downloadedDrawable);
            task.execute(imagePath);
        }
    }
}