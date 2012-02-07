package com.epfl.android.aac_speech.data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import simplenlg.features.Feature;
import simplenlg.features.Gender;
import simplenlg.features.LexicalFeature;
import simplenlg.features.NumberAgreement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.NPPhraseSpec;
//import android.R;
import android.graphics.Color;

import com.epfl.android.aac_speech.MainActivity;
import com.epfl.android.aac_speech.nlg.Pic2NLG;
import com.epfl.android.aac_speech.nlg.Pic2NLG.ActionType;

public class Pictogram {
	public String data; /* This is word or category_ID */
	public String display_text;
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
		return MainActivity.getPreferedLanguage().equals("en");
	}

	static boolean isFrench() {
		return MainActivity.getPreferedLanguage().equals("fr");
	}

	private void init(String word, Pic2NLG.ActionType type) {
		this.data = this.display_text = word;
		this.type = type;
		if (type != Pic2NLG.ActionType.NUMBER_AGREEMENT
				&& type != Pic2NLG.ActionType.NEGATED) {

			switch (type) {
			case ADJECTIVE:
				this.element = Pic2NLG.factory.createNLGElement(word,
						LexicalCategory.ADJECTIVE);
				break;

			case NOUN:
				// TODO: English is crap!: even WE gets wrong number and
				// person!!!
				this.element = Pic2NLG.factory.createNLGElement(word,
						LexicalCategory.NOUN);

				// TODO: Temporal hack to handle plural automatically. work OK
				if (word.endsWith("s")) {
					this.element.setFeature(Feature.NUMBER,
							NumberAgreement.PLURAL);
				}
				break;

			case VERB:
				this.element = Pic2NLG.factory.createNLGElement(word,
						LexicalCategory.VERB);

				// TODO: a hack to handle reflexivity
				if (word.startsWith("se ") && isFrench()) {
					this.element = Pic2NLG.factory.createNLGElement(
							word.replaceFirst("se ", ""), LexicalCategory.VERB);
					this.element.setFeature(LexicalFeature.REFLEXIVE, true);
				}

				// TODO: temporal hack to fix "to do smf" in English.
				if (word.startsWith("to ") && isEnglish()) {
					this.element = Pic2NLG.factory.createNLGElement(
							word.replaceFirst("to ", ""), LexicalCategory.VERB);
				}

				// in English words like can must have POS specified as modal
				// not verb, otherwise a verb he cans/he canned/ would be used
				if (word.equals("can") && isEnglish()) {
					this.element = Pic2NLG.factory.createNLGElement("can",
							LexicalCategory.MODAL);
				}

				break;

			case ADVERB:
				this.element = Pic2NLG.factory.createNLGElement(word,
						LexicalCategory.ADVERB);
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

		return mtype;
	}

	public Pictogram(String word, Pic2NLG.ActionType type, int imageResourceId) {
		init(word, type);
		this.imageResourceId = imageResourceId;
	}

	public Pictogram(String display_text, NLGElement elm,
			Pic2NLG.ActionType type, int imageResourceId) {
		init(display_text, elm, type);
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
	public Pictogram(String data, String display_text, int colorCode,
			Pic2NLG.ActionType type, int imageResourceId) {
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

	private void init(String display_text, NLGElement element,
			Pic2NLG.ActionType type) {
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
		return "[type:" + this.type + "d:" + this.element + "]";

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
		 * final static String color_white = "ffffff"; final static String
		 * color_blue = "aaaaff"; final static String color_green = "aaffaa";
		 * final static String color_yellow = "FFFF66"; // was 00 final static
		 * String color_pink = "FF99CC"; // F52887 final static String
		 * color_orange = "ffcc66"; // ffcc66
		 * 
		 * 
		 * final static String color_black = "000000"; // ffcc66
		 */

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
	 */
	public int getBgColor() {

		/** Category has it's background, so icons shall be transparent */
		if (this.type == ActionType.CATEGORY || this.type == ActionType.NEGATED
				|| this.type == ActionType.QUESTION
				|| this.type == ActionType.DOT)
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