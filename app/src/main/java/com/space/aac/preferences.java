package com.space.aac;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
/**
 * Created by root on 21/10/16.
 */
public class preferences extends PreferenceActivity  {
    private final static boolean PREF_UPERCASE_DEFAULT = false;
    private final static boolean PREF_HIDE_SPC_DEFAULT = false;
    private final boolean PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT = false;
    private final String PREF_GENDER_DEFAULT = "MALE";
    protected static boolean pref_uppercase = PREF_UPERCASE_DEFAULT;
    private static boolean pref_hide_spc_color = PREF_HIDE_SPC_DEFAULT;
    private boolean pref_hide_offensive = true;
    private boolean pref_clear_phrase_after_speak = PREF_CLEAR_PHRASE_AFTER_SPEAK_DEFAULT;
    private boolean pref_switch_back_to_main_screen = true;
    private boolean pref_read_each_word;
    String text;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getText(text);
    }
    public static String getText(String text) {
        if (pref_uppercase)
            return text.toUpperCase();
        return text;
    }
}