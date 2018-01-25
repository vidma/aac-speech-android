package com.space.aac.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.space.aac.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by space on 15/7/16.
 */
public class ZippedDatafilesHelper {

    public static void ensureReadiness(Context c){
        // TODO: check if files are up to date (checksum/success)
        boolean ready = readChecksum(c).equals(getPictogramChecksum(c));
        // if not update them
        if (!ready){
            Log.d("ZippedDatafilesHelper", "will unzip");
            updatePictograms(c);
        }
        else {
            Log.d("ZippedDatafilesHelper", "icons are already ready");
        }
    }

    private static String getPictogramChecksum(Context c){
        return "id:" + R.raw.aac_speech_data;
    }


    public static String getIconsRootDir(Context c) {
        return (CharSequence) getOutputDir(c).toString() + "/";
    }

    public static File getOutputDir(Context c){
        return c.getExternalFilesDir(null);
    }

    // TODO: move to ui class
    public static void updatePictograms(final Context mainContext) {
        Log.i("update_pictograms", "starting...");

        // TODO: change the message
        final ProgressDialog progr_dlg = new ProgressDialog(mainContext);
        final Context c = mainContext.getApplicationContext();

        progr_dlg.setMessage(c.getResources().getString(R.string.update_icons_wait_msg));
        progr_dlg.setCancelable(false);
        progr_dlg.setMax(ZipExtractorTask.PROGRESS_RANGE);
        progr_dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progr_dlg.setProgress(0);
        progr_dlg.show();

        class PictogramZipExtractor extends ZipExtractorTask {
            @Override
            protected void onProgressUpdate(Integer... progress) {
                Log.v("onProgressUpdate", "pr:" + progress[0]);
                if (progr_dlg.isShowing()) {
                    progr_dlg.setProgress(progress[0]);
                }
                super.onProgressUpdate(progress);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                writeChecksum(c);
                progr_dlg.dismiss();
            }

        };
        ZipExtractorTask.OUTPUT_DIR = getOutputDir(c);
        InputStream istream = c.getResources().openRawResource(R.raw.aac_speech_data);
        new PictogramZipExtractor().execute(istream);
    }


    private static void writeChecksum(final Context c) {
        // TODO: write a checksum, maybe I could use resource ID?
        File file = new File(getOutputDir(c), "checksum.txt");
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            os.write(getPictogramChecksum(c).getBytes());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static String readChecksum(final Context c) {
        // TODO: write a checksum, maybe I could use resource ID?
        File file = new File(getOutputDir(c), "checksum.txt");
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "non-existent";
    }
}
