package com.space.aac;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

import static android.support.v4.app.ActivityCompat.startActivity;
import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static android.widget.Toast.*;

/**
 * Created by space on 20/7/16.
 */
public class TTSButtonActivity  extends AppCompatActivity {

    public static String getPreferedLanguage() {
       /* String lang = java.util.Locale.getDefault().getLanguage();

        if (!ArrayUtils.contains(SUPPORTED_LANGUAGES, lang))
            lang = DEFAULT_LANGUAGE;

        return lang;*/
        return "en";
    }




}
