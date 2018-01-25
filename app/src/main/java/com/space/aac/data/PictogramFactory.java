package com.space.aac.data;

import android.content.res.Resources;
import android.util.Log;

import com.space.aac.R;
import com.space.aac.nlg.Pic2NLG;
import com.space.aac.nlg.Pic2NLG.ActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by space on 28/7/16.
 */
public class PictogramFactory {

    private class TwoWayHashmap<K extends Object, V extends Object> {

        private Map<K, V> forward = new Hashtable<K, V>();
        private Map<V, K> backward = new Hashtable<V, K>();

        public synchronized void put(K key, V value) {
            forward.put(key, value);
            backward.put(value, key);
        }

        @SuppressWarnings("unused")
        public synchronized V getForward(K key) {
            return forward.get(key);
        }

        public synchronized K getBackward(V key) {
            return backward.get(key);
        }
    }

    private static final String REGEXP_NUMBER = "[0-9]+";

    // Serialized constants; these may be used to create the buttons for the homescreen
    public static final String ACT_NEGATE = "NEGATE";

    public static final String ACT_DOT = ".";
    public static final String ACT_QUESTION = "?";

    public static final String ACT_TENSE_PAST = "PAST";
    public static final String ACT_TENSE_FUTURE = "FUTURE";
    public static final String ACT_TENSE_CLOSE_FUTURE = "FUTUR-PROCHE";
    public static final String ACT_TENSE_PRESENT = "PRESENT";

    public static final String ACT_PRONOUN_I = "I";
    public static final String ACT_PRONOUN_YOU = "YOU";
    public static final String ACT_PRONOUN_HE = "HE";
    public static final String ACT_PRONOUN_SHE = "SHE";
    public static final String ACT_PRONOUN_WE = "WE";
    public static final String ACT_PRONOUN_YOU_PL = "YOU;PL";
    public static final String ACT_PRONOUN_THEY_M = "THEY_M";
    public static final String ACT_PRONOUN_THEY_F = "THEY_F";
    public static final String ACT_PRONOUN_THAT = "THAT";

    // TODO
    public static final String ACT_PRONOUN_OBJ_MYSELF = "MYSELF"; // me - me
    public static final String ACT_PRONOUN_OBJ_YOU = "YOU;OBJ"; // te -you
    public static final String ACT_PRONOUN_OBJ_HIM = "HIM"; // lui -him
    public static final String ACT_PRONOUN_OBJ_HER = "HER"; // lui -her

    public static final String ACT_PRONOUN_OBJ_US = "US"; // nous -us
    public static final String ACT_PRONOUN_OBJ_YOU_PL = "YOU;PL;OBJ"; // vous -
    // you
    public static final String ACT_PRONOUN_OBJ_THEM = "THEM"; // eux -them

    // TODO: this is not really needed right?
    public static final String ACT_PRONOUN_OBJ_THEM_F = "THEM;FEM"; // eux -them

    public static final String ACT_LETS_DO_SMF = "LETS";

    public static final String ACT_VERB_WANT = "WANT";
    public static final String ACT_VERB_HAVE = "HAVE";
    public static final String ACT_VERB_CAN = "CAN";
    public static final String ACT_VERB_TO_BE = "TOBE";

    private static final String TAG = "PictogramFactory";

    /* separator used to encode the serialized items */
    private static final String SEPARATOR = "|";
    /* and the one to decode */
    private static final String SEPARATOR_REGEXP = "\\|";

    class ActionDefinition {
        /* title in current language */
        // public String title;
        public int titleResourceId = 0;
        public int imageResourceId = 0;
        public ActionType actType;

        public ActionDefinition(ActionType actType, int titleResourceId, int IconResourceId) {
            this.titleResourceId = titleResourceId;
            this.imageResourceId = IconResourceId;
            this.actType = actType;
        }

    }

    /* serialized --> actionType */
    protected static TwoWayHashmap<String, ActionType> special_actions = null;

    /* key=Serialized constant */
    protected static HashMap<String, ActionDefinition> action_defs = null;

    protected static PictogramFactory INSTANCE = null;

    /* Context dependent variables */
    protected DBHelper db;
    // deprecated: protected String pref_gender;
    protected Resources res;

    /**
     * title in current language [If I had this, I could use the same routine to
     * create the homescreen] serialized -- may be actionType.toString()
     *
     * actiontype data params
     *
     *
     * 1 to 1: tenses; etc;
     *
     * 1 to N: nouns: je, tu, eux, etc verbs: to have
     */
    public PictogramFactory(DBHelper db, Resources res) {
        this.res = res;
        this.db = db;

		/* Initialize action definitions */

        special_actions = new TwoWayHashmap<String, Pic2NLG.ActionType>();
        action_defs = new HashMap<String, ActionDefinition>();

        System.out.println("DOT IS:" + ActionType.DOT.toString());

        special_actions.put(ACT_DOT, ActionType.DOT);
        action_defs.put(ACT_DOT, new ActionDefinition(ActionType.DOT, R.string.btn_dot, R.drawable.point));

        special_actions.put(ACT_QUESTION, ActionType.QUESTION);
        action_defs.put(ACT_QUESTION, new ActionDefinition(ActionType.QUESTION, R.string.btn_quesion,
                R.drawable.question));

        special_actions.put(ACT_NEGATE, ActionType.NEGATED);
        action_defs.put(ACT_NEGATE, new ActionDefinition(ActionType.NEGATED, R.string.btn_negated, R.drawable.no));

		/* TENSES */

        special_actions.put(ACT_TENSE_PAST, ActionType.TENSE_PAST);
        action_defs
                .put(ACT_TENSE_PAST, new ActionDefinition(ActionType.TENSE_PAST, R.string.btn_past, R.drawable.past));

        special_actions.put(ACT_TENSE_FUTURE, ActionType.TENSE_FUTURE);
        action_defs.put(ACT_TENSE_FUTURE, new ActionDefinition(ActionType.TENSE_FUTURE, R.string.btn_future,
                R.drawable.future));

        special_actions.put(ACT_TENSE_PRESENT, ActionType.TENSE_PRESENT);
        action_defs.put(ACT_TENSE_PRESENT, new ActionDefinition(ActionType.TENSE_PRESENT, R.string.btn_present,
                R.drawable.now));

		/* Pronouns */
        action_defs.put(ACT_PRONOUN_I, new ActionDefinition(ActionType.NOUN, R.string.btn_I, R.drawable.je));
        action_defs.put(ACT_PRONOUN_YOU, new ActionDefinition(ActionType.NOUN, R.string.btn_you, R.drawable.tu));
        action_defs.put(ACT_PRONOUN_HE, new ActionDefinition(ActionType.NOUN, R.string.btn_he, R.drawable.il));
        action_defs.put(ACT_PRONOUN_SHE, new ActionDefinition(ActionType.NOUN, R.string.btn_she, R.drawable.elle));

        action_defs.put(ACT_PRONOUN_WE, new ActionDefinition(ActionType.NOUN, R.string.btn_we, R.drawable.nous));
        action_defs
                .put(ACT_PRONOUN_YOU_PL, new ActionDefinition(ActionType.NOUN, R.string.btn_you_pl, R.drawable.vous));
        action_defs.put(ACT_PRONOUN_THEY_M, new ActionDefinition(ActionType.NOUN, R.string.btn_they_m, R.drawable.ils));
        action_defs
                .put(ACT_PRONOUN_THAT, new ActionDefinition(ActionType.NOUN, R.string.btn_that, R.drawable.that_one));

        action_defs.put(ACT_PRONOUN_THEY_F,
                new ActionDefinition(ActionType.NOUN, R.string.btn_they_f, R.drawable.elles));

		/* TODO: this is a specific case in French */
        action_defs.put(ACT_LETS_DO_SMF, new ActionDefinition(ActionType.NOUN, R.string.btn_lets, R.drawable.on));

		/* VERBS */
        action_defs.put(ACT_VERB_CAN, new ActionDefinition(ActionType.VERB, R.string.btn_can, R.drawable.pouvoir));
        action_defs.put(ACT_VERB_HAVE, new ActionDefinition(ActionType.VERB, R.string.btn_to_have, R.drawable.avoir));
        action_defs.put(ACT_VERB_TO_BE, new ActionDefinition(ActionType.VERB, R.string.btn_to_be, R.drawable.etre));
        action_defs.put(ACT_VERB_WANT, new ActionDefinition(ActionType.VERB, R.string.btn_to_want, R.drawable.vouloir));

		/*
		 * Clitic pronouns: me, te,
		 */

        action_defs.put(ACT_PRONOUN_OBJ_MYSELF, new ActionDefinition(ActionType.CLITIC_PRONOUN,
                R.string.btn_clitic_myself, R.drawable.je));

        action_defs.put(ACT_PRONOUN_OBJ_YOU, new ActionDefinition(ActionType.CLITIC_PRONOUN, R.string.btn_clitic_you,
                R.drawable.tu));

        action_defs.put(ACT_PRONOUN_OBJ_HIM, new ActionDefinition(ActionType.CLITIC_PRONOUN, R.string.btn_clitic_him,
                R.drawable.il));

        action_defs.put(ACT_PRONOUN_OBJ_HER, new ActionDefinition(ActionType.CLITIC_PRONOUN, R.string.btn_clitic_her,
                R.drawable.elle));

        action_defs.put(ACT_PRONOUN_OBJ_US, new ActionDefinition(ActionType.CLITIC_PRONOUN, R.string.btn_clitic_us,
                R.drawable.nous));

        action_defs.put(ACT_PRONOUN_OBJ_YOU_PL, new ActionDefinition(ActionType.CLITIC_PRONOUN,
                R.string.btn_clitic_you_pl, R.drawable.vous));
        action_defs.put(ACT_PRONOUN_OBJ_THEM, new ActionDefinition(ActionType.CLITIC_PRONOUN,
                R.string.btn_clitic_them_m, R.drawable.ils));

        action_defs.put(ACT_PRONOUN_OBJ_THEM_F, new ActionDefinition(ActionType.CLITIC_PRONOUN,
                R.string.btn_clitic_them_f, R.drawable.elles));

        // TODO: NUMBER_AGREEMENT doesn't seem to be important nor useful
    }

    Pictogram getSpecialActionJe() {
        String pronoun_I = res.getString(R.string.btn_I);
        return new Pictogram(pronoun_I, ActionType.NOUN, R.drawable.je, SpecialPicto.I);
    }

    /*
     * TODO: for this to work we must: a) store the HomeScreen on DB b) what
     * about je and it's gender? will be used for history
     */
    public String getSerializableString(Pictogram icon) {
        String key;
        // if that's a special action
        if ((key = special_actions.getBackward(icon.type)) != null) {
            return key;
        } else {
            // we just map homescreen icons by their resourceID, what's the
            // simplest solution now
            if (icon.imageResourceId != 0 && icon.type == ActionType.NOUN || icon.type == ActionType.VERB
                    || icon.type == ActionType.CLITIC_PRONOUN) {
                for (Entry<String, ActionDefinition> entry : action_defs.entrySet()) {
                    if (entry.getValue().imageResourceId == icon.imageResourceId
                            && entry.getValue().actType == icon.type) {
                        return entry.getKey();
                    }
                }
            }
            // otherwise that shall be just a simple icon from DB
            // now.. it's ID
            // TODO: we may have options too, like gender etc
            if (icon.wordID > 0) {
                return ((Integer) (icon.wordID)).toString();
            }

            Log.d(TAG, "not able to serialize:" + icon);
            return null;

        }
    }

    /**
     * @param phrase_list
     * @return
     */
    public String getSerialized(ArrayList<Pictogram> phrase_list) {
        StringBuilder s = new StringBuilder();
        int i = 0;
        // update statistics for each individual icon
        for (Pictogram word : phrase_list) {
            String serialized = getSerializableString(word);
            if (serialized == null) {
                Log.d(TAG, "PROBLEM: serialized word is null" + word.toString());
                continue;
            }

            if (i++ != 0)
                s.append(SEPARATOR);

            s.append(serialized);
        }
        return s.toString();
    }

    public ArrayList<Pictogram> createFromSerialized(String serialized) {
        // Log.d(TAG, "serialized" + serialized);
        ArrayList<Pictogram> items = new ArrayList<Pictogram>();
        for (String sItem : serialized.split(SEPARATOR_REGEXP)) {
            // Log.d(TAG, "sItem" + sItem);

            Pictogram item = get(sItem);
            if (item != null)
                items.add(item);
        }
        Log.d(TAG, "recreated from serialized:" + items);

        return items;
    }

    /**
     * Recreate ActionButton from serialized
     *
     * @param serialized
     * @param
     * @return
     */
    public Pictogram get(String serialized) {

        try {

            if (serialized.matches(REGEXP_NUMBER)) {
                try {
                    long itemId = Integer.parseInt(serialized);
                    return db.getIconById(itemId);
                } catch (NumberFormatException e) {
                    return null;
                } catch (NullPointerException ew) {
                    return null;
                }
            }

            // otherwise try to parse something more special
            // for now these would but just special words for home screen
            if (serialized.equals(ACT_PRONOUN_I)) {
                return getSpecialActionJe();
            }



            if (action_defs.containsKey(serialized)) {
                ActionDefinition act = action_defs.get(serialized);
                return new Pictogram(res.getString(act.titleResourceId), act.actType, act.imageResourceId);
            }





        }
        catch (NullPointerException r)
        { return null;}


        return null;
    }
}