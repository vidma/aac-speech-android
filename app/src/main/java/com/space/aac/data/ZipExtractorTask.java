package com.space.aac.data;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by space on 15/7/16.
 */
public class ZipExtractorTask extends AsyncTask<InputStream, Integer, String> {
    public static int PROGRESS_RANGE = 100;
    public static int PROGRESS_DONE = PROGRESS_RANGE;

    /*
     * TODO: set the output dir as the preffered data directory, SDCARD on
     * phones, Disk on tablets
     */
    public static File OUTPUT_DIR = null;
    public static final String DONE = "DONE";
    public static final String FAIL = "FAIL";

    final int BUFFER_SIZE = 4096;

    @Override
    protected String doInBackground(InputStream... params) {
        Log.i("ZipDownloaderTask", "starting");
        try {
            InputStream input = params[0];
            long lenghtOfFile = input.available();
            Log.i("ZipExtractorTask", "file size: " + lenghtOfFile);

            extractStream(lenghtOfFile, input);
            input.close();
        } catch (Exception e) {
            Log.e("ZipExtractorTask: ERR", e.toString());
            return FAIL;
        }
        Log.i("ZipExtractorTask", "done");
        publishProgress(PROGRESS_DONE);
        return DONE;
    }

    private void extractStream(long lenghtOfFile, InputStream input) throws IOException, FileNotFoundException {
        long total = 0;
        // try to uncompress at the same time

        ZipInputStream zip_stream = new ZipInputStream(input);
        ZipEntry zipEntry;

        int progress = 0, progress_old = 0;

        while ((zipEntry = zip_stream.getNextEntry()) != null) {
            processZipEntry(zip_stream, zipEntry);

            // Update the progress
            long compresed_size = (zipEntry.getCompressedSize() != -1) ? zipEntry.getCompressedSize() : zipEntry.getSize();
            total += (compresed_size > 0) ? compresed_size : 0;
            progress = (int) ((float) total * PROGRESS_RANGE / lenghtOfFile);
            // Do not instantiate UI updates if the progress change may not be seen (we've got 6K files!)
            if (progress > progress_old) {
                progress_old = progress;
                Log.d("publishProgress", " " + progress);
                publishProgress(progress);
            }
        }
        zip_stream.close();
    }

    private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws FileNotFoundException,
            IOException {
        String zipEntryName = zipEntry.getName();
        // Log.i("ZipDownloaderTask", "entry: " + zipEntryName);

        File file = new File(OUTPUT_DIR, zipEntryName);
        //Log.d("ZipExtractor", "creating:"+ file);

        if (file.exists()) {
            // TODO: shall we override?
        } else {
            if (zipEntry.isDirectory()) {
                file.mkdirs();
            } else {
                byte buffer[] = new byte[BUFFER_SIZE];
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
                int count1;

                while ((count1 = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                    bufferedOutputStream.write(buffer, 0, count1);

                }

                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
    }
}
