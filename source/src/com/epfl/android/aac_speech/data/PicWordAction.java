package com.epfl.android.aac_speech.data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import simplenlg.features.Feature;
import simplenlg.features.Gender;
import simplenlg.features.LexicalFeature;
import simplenlg.features.NumberAgreement;
import simplenlg.framework.NLGElement;
import simplenlg.phrasespec.NPPhraseSpec;
//import android.R;
import android.graphics.Color;

import com.epfl.android.aac_speech.MainActivity;
import com.epfl.android.aac_speech.nlg.Pic2NLG;
import com.epfl.android.aac_speech.nlg.Pic2NLG.ActionType;

public class PicWordAction {
	public String data; /* This is word or category_ID */
	public String display_text;
	public Pic2NLG.ActionType type;
	public int colorCode = 0;
	public int wordID = 0;
	// TODO: remove public int use_count = 0;

	public NLGElement element = null;
	public int imageResourceId = 0;
	public String imageFileURI = "";

	public PicWordAction(String word, Pic2NLG.ActionType type) {
		init(word, type);
	}

	private void init(String word, Pic2NLG.ActionType type) {
		this.data = this.display_text = word;
		this.type = type;
		if (type != Pic2NLG.ActionType.NUMBER_AGREEMENT
				&& type != Pic2NLG.ActionType.NEGATED) {
			// TODO: this requires the simpleNLG to be fully initialized!
			this.element = Pic2NLG.factory.createNLGElement(word);
			// TODO: Temporal hack to handle plural automatically. seems to work
			// not so badly! :)
			if (type == Pic2NLG.ActionType.NOUN && word.endsWith("s")) {
				this.element = Pic2NLG.factory.createNounPhrase(word);
				this.element.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
			}

			// TODO: a hack handle reflexivity (check if allways work)
			if (type == Pic2NLG.ActionType.VERB && word.startsWith("se ")
					&& MainActivity.getPreferedLanguage().equals("fr")) {
				this.element = Pic2NLG.factory.createNLGElement(word
						.replaceFirst("se ", ""));
				this.element.setFeature(LexicalFeature.REFLEXIVE, true);
			}

			// TODO: temporal hack to fix "to do smf" in English. that seem not
			// be recognized nicely
			if (type == Pic2NLG.ActionType.VERB && word.startsWith("to ")
					&& MainActivity.getPreferedLanguage().equals("en")) {
				this.element = Pic2NLG.factory.createNLGElement(word
						.replaceFirst("to ", ""));
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

	public PicWordAction(String word, Pic2NLG.ActionType type,
			int imageResourceId) {
		init(word, type);
		this.imageResourceId = imageResourceId;
	}

	public PicWordAction(String display_text, NLGElement elm,
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
	public PicWordAction(String data, String display_text, int colorCode,
			Pic2NLG.ActionType type, int imageResourceId) {
		init(data, type);
		this.colorCode = colorCode;
		this.display_text = display_text;

		this.imageResourceId = imageResourceId;
	}

	public PicWordAction(String word, String stype, String image_URI,
			int spc_color) {
		Pic2NLG.ActionType type = PicWordAction.StringToActionTypeEnum(stype);
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

	public PicWordAction(NLGElement element, Pic2NLG.ActionType type) {
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
		return "data: " + this.data + " type: " + this.type + "elm: "
				+ this.element;
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
		final static String color_white = "ffffff";
		final static String color_blue = "aaaaff";
		final static String color_green = "aaffaa";
		final static String color_yellow = "FFFF66"; // was 00
		final static String color_pink = "FF99CC"; // F52887
		final static String color_orange = "ffcc66"; // ffcc66

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
		/* get color */
		int hexColor = SpcColor.getColor(this.colorCode, true);
		if (hexColor != 0)
			return hexColor;

		String color = SpcColor.color_white;
		switch (this.type) {
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