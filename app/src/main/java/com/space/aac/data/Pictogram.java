package com.space.aac.data;

import android.graphics.Color;

import com.space.aac.TTSButtonActivity;
import com.space.aac.nlg.Pic2NLG;
import com.space.aac.nlg.Pic2NLG.ActionType;

import java.util.ArrayList;
import java.util.List;

import simplenlg.features.Feature;
import simplenlg.features.Gender;
import simplenlg.features.LexicalFeature;
import simplenlg.features.NumberAgreement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGElement;


/**
 * Created by space on 21/7/16.
 */
enum SpecialPicto {
    I // must have the gender
}

public class Pictogram {

    private static final String TAG = "AAC";
    boolean a = false;
    public static String pref_my_gender = "MALE"; // TODO: this might be accessed differently?

    public String data; /* This is word or category_ID */
    public String display_text;
    private String word; // used internally when creating the NLG Element
    private List<SpecialPicto> features = new ArrayList<SpecialPicto>();
    public Pic2NLG.ActionType type;
    public int colorCode = 0;
    public int wordID = 0;
    // TODO: remove public int use_count = 0;

    public NLGElement element = null;
    public int imageResourceId = 0;
    public String imageFileURI = "";

    public Pictogram(String word, Pic2NLG.ActionType type) {
        init(word, type);
    }

    static boolean isEnglish() {
        return TTSButtonActivity.getPreferedLanguage().equals("en");
    }

    static boolean isFrench() {
        return TTSButtonActivity.getPreferedLanguage().equals("fr");
    }



    private void init(String word, Pic2NLG.ActionType type) {
        /**
         * TODO: creation of this.element is one of items preventing separation from NLG lib.
         * TODO: when parsing the icons, it could be created on the fly, right?
         */
        this.data = this.display_text = word;
        this.type = type;
        this.word = word;

        //TODO: this will be called live when interpreting not when creating?
        this.to_nlg();
    }

    private void to_nlg() {
        if (type != Pic2NLG.ActionType.NUMBER_AGREEMENT && type != Pic2NLG.ActionType.NEGATED) {


            switch (type) {
                case ADJECTIVE:
                    this.element = Pic2NLG.factory.createNLGElement(word, LexicalCategory.ADJECTIVE);
                    break;

                case NOUN:
                    // We have to use createNP instead of createNLGElement because
                    // only the earlier automatically differentiates between
                    // PRONOUNS and NOUNS (je, te, you, I, are pronouns, but we use
                    // NOUN type here)
                    this.element = Pic2NLG.factory.createNounPhrase(word);

                    // TODO: Temporal hack to handle plural automatically. work OK
                    if (word.endsWith("s")) {
                        this.element.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    }
                    for (SpecialPicto feature: features){
                        switch (feature) {
                            case I:
                                if (pref_my_gender.equals("FEMALE"))
                                    this.element.setFeature(LexicalFeature.GENDER, Gender.FEMININE);
                        }
                    }

                    break;

                case VERB:
                    this.element = Pic2NLG.factory.createNLGElement(word, LexicalCategory.VERB);



                   /* // TODO: a hack to handle reflexivity
                    if (word.startsWith("se ") && isFrench()) {
                        this.element = Pic2NLG.factory.createNLGElement(word.replaceFirst("se ", ""), LexicalCategory.VERB);
                        this.element.setFeature(LexicalFeature.REFLEXIVE, true);
                    }*/

                    // TODO: temporal hack to fix "to do smf" in English.

/*
                    if ((word.equals("can")) && isEnglish()){
                        a = true;
                    }



                    if(word.equals("want")){
                        this.element = Pic2NLG.factory.createNLGElement(word, LexicalCategory.VERB);
                    }
                    else if (word.startsWith("to ")&& a==true) {
                    //if(word.startsWith("to ")&& a==true) {

                        this.element = Pic2NLG.factory.createNLGElement("can", LexicalCategory.VERB);
                        //this.element = Pic2NLG.factory.createNLGElement(word.replaceFirst("to ", " can "), LexicalCategory.VERB);
                    }
                    else {

                            //this.element = Pic2NLG.factory.createNLGElement(word, LexicalCategory.VERB);
                            Log.d("AAC", word);

                            this.element = Pic2NLG.factory.createNLGElement(word);

                        }*/




                    // in English words like can must have POS specified as modal
                    // not verb, otherwise a verb he cans/he canned/ would be used

                     /*if (word.equals("can") && isEnglish()) {
                    this.element = Pic2NLG.factory.createNLGElement("can", LexicalCategory.ANY);
                    this.element = Pic2NLG.factory.createNLGElement(this.display_text);
                    }*/

                    break;

                case ADVERB:
                    this.element = Pic2NLG.factory.createNLGElement(word, LexicalCategory.ADVERB);
                    break;

                case PREPOSITION:
                    this.element = Pic2NLG.factory.createNLGElement(word, LexicalCategory.PREPOSITION);
                    break;

                default:
                                   this.element = Pic2NLG.factory.createNLGElement(word);
                    break;
            }
        }
    }

    public static Pic2NLG.ActionType StringToActionTypeEnum(String str) {
		/* TODO: finish this!!! */
        Pic2NLG.ActionType mtype = ActionType.NOUN;

        if (str.equals("noun"))
            mtype = ActionType.NOUN;
        if (str.equals("adjective"))
            mtype = ActionType.ADJECTIVE;
        // TODO: I have to clean up the database
        if (str.equals("adverb"))
            mtype = ActionType.ADVERB;
        if (str.equals("verb"))
            mtype = ActionType.VERB;
        if (str.equals("preposition"))
            mtype = ActionType.PREPOSITION;

        return mtype;
    }

    public Pictogram(String word, Pic2NLG.ActionType type, int imageResourceId) {
        init(word, type);
        this.imageResourceId = imageResourceId;
    }

    public Pictogram(String display_text, NLGElement elm, Pic2NLG.ActionType type, int imageResourceId) {
        init(display_text, elm, type);
        this.imageResourceId = imageResourceId;
    }

    public Pictogram(String display_text, Pic2NLG.ActionType type, int imageResourceId, SpecialPicto feature) {
        this.features.add(feature);
        init(display_text, type);
        this.imageResourceId = imageResourceId;
    }

    /**
     * Separe text for display than the param stored (e.g. category_id).
     *
     * @param data
     * @param display_text
     * @param type
     * @param imageResourceId
     */
    public Pictogram(String data, String display_text, int colorCode, Pic2NLG.ActionType type, int imageResourceId) {
        init(data, type);
        this.colorCode = colorCode;
        this.display_text = display_text;

        this.imageResourceId = imageResourceId;
    }

    public Pictogram(String word, String stype, String image_URI, int spc_color) {
        Pic2NLG.ActionType type = Pictogram.StringToActionTypeEnum(stype);
        init(word, type);
        this.imageFileURI = image_URI;
        this.colorCode = spc_color;
    }

    private void init(String display_text, NLGElement element, Pic2NLG.ActionType type) {
        this.type = type;
        this.element = element;
        this.display_text = display_text;
    }

    public Pictogram(NLGElement element, Pic2NLG.ActionType type) {
        init("", element, type);
    }

    public String toString() {
        if (type == Pic2NLG.ActionType.NUMBER_AGREEMENT)
            return this.data;

        if (type == Pic2NLG.ActionType.NEGATED)
            return "négation";

        return display_text;
    }

    public String toDebugString() {
       // return "[type:" + this.type + "d:" + this.element + "]";
        return  "d" ;

        // return "data: " + this.data + " type: " + this.type + "elm: "
        // + this.element;
    }

    /**
     * TODO: color values could come from database, but SPC is very old (80s?),
     * so it's not supposed to change
     */
    public static class SpcColor {

        public class SPC_ColorCode {
            public static final int PROPER_NAME = 1;
            public static final int COMMON_NAME = 2;
            public static final int ACTION = 3;
            public static final int DESCRIPTIVE = 4;
            public static final int SOCIAL = 5;
            public static final int MISC = 6;
        };

		/* color definitions */
		/*
		 * It has been adviced by AAC experts to use more pure colors for easier
		 * identification even if that would look worse
		 */

        final static String color_white = "ffffff";
        final static String color_blue = "0000FF";
        final static String color_green = "00FF00";
        final static String color_yellow = "FFFF00"; // was 00
        final static String color_pink = "FFC0CB"; // same as for html colorname
        // = pink
        final static String color_orange = "FFA500"; // same as for html
        // colorname = orange

        final static String color_black = "000000"; // ffcc66

        public static int getColor(int colorCode, boolean noDefault) {
            String color = color_white;
            switch (colorCode) {
                case SPC_ColorCode.PROPER_NAME:
                    color = color_yellow;
                    break;
                case SPC_ColorCode.COMMON_NAME:
                    color = color_orange;
                    break;
                case SPC_ColorCode.ACTION:
                    color = color_green;
                    break;
                case SPC_ColorCode.DESCRIPTIVE:
                    color = color_blue;
                    break;
                case SPC_ColorCode.SOCIAL:
                    color = color_pink;
                    break;
                case SPC_ColorCode.MISC:
                    color = color_white;
                    break;
                default:
                    if (noDefault)
                        return 0;
            }
            return Color.parseColor("#" + color);

        };
    };

    /*
     * Returns a SPC color:
     *
     * 1. Proper names: yellow
     *
     * 2. Common names: orange
     *
     * 3. Actions: green
     *
     * 4. Descriptives (adjectives, adverbs,…): blue
     *
     * 5. Social: pink
     *
     * 6. Miscellanea: white
     * or transparent..
     */
    public int getBgColor() {
        // Category has its background, so icons shall be transparent
        if (this.type == ActionType.CATEGORY || this.type == ActionType.NEGATED || this.type == ActionType.QUESTION
                || this.type == ActionType.DOT || this.type == ActionType.EMPTY)
            return Color.TRANSPARENT;

        int hexColor = SpcColor.getColor(this.colorCode, true);
        if (hexColor != 0)
            return hexColor;

        String color = SpcColor.color_white;
        switch (this.type) {
            case CLITIC_PRONOUN:
            case NOUN:
                // TODO: proper names vs. common names!
                color = SpcColor.color_orange;
                break;
            case VERB:
                color = SpcColor.color_green;
                break;
            case ADJECTIVE:
            case ADVERB:
                color = SpcColor.color_blue;
                break;

            default:
                break;
        }

        return Color.parseColor("#" + color);

    }
}
