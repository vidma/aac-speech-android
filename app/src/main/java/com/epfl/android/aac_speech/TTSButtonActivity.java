package com.epfl.android.aac_speech;

import java.util.HashMap;
import java.util.Locale;

import com.actionbarsherlock.app.SherlockActivity;
import com.epfl.android.aac_speech.lib.ArrayUtils;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * Provides an TTS activity with a speak button.
 * 
 * one needs to implement the onSpeakButtonClicked function
 * 
 * @author vidma
 * 
 */
public abstract class TTSButtonActivity extends SherlockActivity implements OnInitListener {

	// protected static String pref_lang = "FR";

	private static final String SPEAK_PHRASE = "SPEAK_PHRASE";
	private static final String SPEAK_ONE_WORD = "speakOneWord";
	protected TextToSpeech mTts;

	protected abstract void onSpeakButtonClicked();

	private static final int REQ_TTS_STATUS_CHECK = 0;
	private ImageButton speakBtn = null;

	private static final String TAG = "TTSButtonActivity";

	public static final String LANG_FR = "fr";
	public static final String LANG_EN = "en";
	// TODO: from API11, this exists as TextToSpeech.Engine.KEY_PARAM_VOLUME, but we want backwards compat 
	private static final String KEY_PARAM_VOLUME = "volume";
	

	private static String[] SUPPORTED_LANGUAGES = { LANG_FR, LANG_EN };
	private static String DEFAULT_LANGUAGE = LANG_FR;

	public TTSButtonActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check to be sure that TTS exists and is okay to use
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
	}

	@Override
	protected void onRestart() {
		// TODO: do I have any cursors?
		super.onRestart();
	}

	protected void speak(String text) {
		// TODO: indicate in the button what it's being currently spoken
		mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	/**  speak the single word, at a lower volume */
	protected void speakOneWord(String text) {
		int apiVer = android.os.Build.VERSION.SDK_INT;
		if (apiVer >= 11){
			speakOneWordApi13(text);
		} else {
			// compatibility mode
			HashMap<String, String> params = new HashMap<String, String>();
			//params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, SPEAK_ONE_WORD);		
			mTts.speak(text, TextToSpeech.QUEUE_ADD, params);
		}
	}	
	
	/**  speak the single word, at a lower volume */
	@TargetApi(13)
	protected void speakOneWordApi13(String text) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.1");
		//params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, SPEAK_ONE_WORD);		
		mTts.speak(text, TextToSpeech.QUEUE_ADD, params);
	}		

	public void onUtteranceCompleted(String uttId) {
	    if (uttId == SPEAK_ONE_WORD) {
	    }
	    
	    if (uttId == SPEAK_PHRASE) {
	    	// could change the icon...
	    }
	    
	}	
	
	protected void ui_enable_tts() {
		speakBtn = (ImageButton) findViewById(R.id.speak);
		speakBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onSpeakButtonClicked();
				// TODO: possibility to cancel
			}

		});
	}

	@Override
	public void onInit(int status) {
		// Now that the TTS engine is ready, we enable the button
		if (status == TextToSpeech.SUCCESS) {
			Locale lang = getCurrentLocale();
			int result = mTts.setLanguage(lang);
			if (result == TextToSpeech.LANG_MISSING_DATA) {
				Log.e(TAG, "Language data is not available.");
				installVoiceData();
			}
			else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {
				speakBtn.setEnabled(true);
			}
		}
	}
	
	
	/**
	 * Ask the current default engine to launch the matching INSTALL_TTS_DATA activity
	 * so the required TTS files are properly installed.
	 * 
	 * based on: http://stackoverflow.com/a/16836553/1276782
	 */
	private void installVoiceData() {
	    Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    // TODO: can use package of active TTS like this?
	    intent.setPackage(mTts.getDefaultEngine()); //"com.google.android.tts"
	    try {
	        Log.v(TAG, "Installing voice data: " + intent.toUri(0));
	        startActivity(intent);
	    } catch (ActivityNotFoundException ex) {
	        Log.e(TAG, "Failed to install TTS data, no acitivty found for " + intent + ")");
	    }
	}	

	/**
	 * @return
	 */
	private static Locale getCurrentLocale() {
		Locale lang = Locale.FRENCH;

		if (LANG_EN.equals(getPreferedLanguage()))
			lang = Locale.ENGLISH;
		return lang;
	}

	/**
	 * 
	 * @return language code (en, fr) for the currently selected UI language
	 */
	public static String getPreferedLanguage() {
		String lang = java.util.Locale.getDefault().getLanguage();

		if (!ArrayUtils.contains(SUPPORTED_LANGUAGES, lang))
			lang = DEFAULT_LANGUAGE;
		
		return lang;
	}

	@Override
	public void onPause() {
		// TODO: save any changes made to data (the app may be killed)
		super.onPause();
		// if we're losing focus, stop talking
		if (mTts != null)
			mTts.stop();
	}

	@Override
	public void onDestroy() {
		if (mTts != null)
			mTts.shutdown();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQ_TTS_STATUS_CHECK) {
			switch (resultCode) {
			case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
				// TTS is up and running
				mTts = new TextToSpeech(this, this);
				Log.v(TAG, "TTS is installed okay");
				break;
			case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
			case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
			case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
				// missing data, install it
				// TODO: handle multilanguage
				Log.v(TAG, "Need language stuff: " + resultCode);
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
				break;
			case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
			default:
				Log.e(TAG, "Got a failure. TTS apparently not available");
			}

		}
	}

}