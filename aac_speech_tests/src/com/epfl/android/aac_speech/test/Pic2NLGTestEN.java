package com.epfl.android.aac_speech.test;

import java.util.ArrayList;

import android.test.AndroidTestCase;

import com.epfl.android.aac_speech.data.PicWordAction.SpcColor.SPC_ColorCode;
import com.epfl.android.aac_speech.data.PicWordAction;
import com.epfl.android.aac_speech.nlg.Pic2NLG;

import simplenlg.features.Gender;
import simplenlg.features.LexicalFeature;
import simplenlg.framework.NLGElement;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;

import junit.framework.TestCase;

/**
 * Test for English version of NLG converter.
 *  As it can be seen, that would work but not perfectly.
 * @author vidma
 *
 */
public class Pic2NLGTestEN extends AndroidTestCase {
	Pic2NLG converter;

	protected void setUp() throws Exception {
		super.setUp();
		converter = new Pic2NLG("EN");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * The phrase is not correct, however avoir is not necessary followed by DE.
	 * At least sofar I didn't find such a rule
	 */
	public void test_jai_eu_assez_de_bonbons() {
		assertEquals(
				"I had enough candies.",
				converter
						.convertPhrasesToNLG(text_jai_eu_assez_de_bonbons(false)));
	}

	public void test_jai_eu_assez_de_bonbons_explicit_de() {
		assertEquals(
				"I had enough candies.",
				converter
						.convertPhrasesToNLG(text_jai_eu_assez_de_bonbons(true)));
	}

	public void test_jai_eu_assez_de_bonbons_adjective() {
		ArrayList<PicWordAction> phrases = text_jai_eu_assez_de_bonbons(false);
		phrases.add(new PicWordAction("delitious", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("I had enough of delitious candies.",
				converter.convertPhrasesToNLG(phrases));

	}

	/* TEXT */
	private ArrayList<PicWordAction> text_je_veux_manger() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		// je avoir [past] assez bonbon [plural] ==> j'ai eu assez des bonbons
		phrases.add(new PicWordAction("I", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("want", Pic2NLG.ActionType.VERB));

		// phrases.add(new PicWordAction("past", Pic2NLG.wordTYPE.TENSE));
		// phrases.add(new PicWordAction("manger", Pic2NLG.ActionType.VERB));
		// Test the creation from DB values
		phrases.add(new PicWordAction("eat", "verb", "", SPC_ColorCode.ACTION));

		return phrases;
	}

	private ArrayList<PicWordAction> text_jai_eu_assez_de_bonbons(
			Boolean de_explicit) {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		// je avoir [past] assez bonbon [plural] ==> j'ai eu assez des bonbons
		phrases.add(new PicWordAction("I", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("have", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("past", Pic2NLG.ActionType.TENSE_PRESENT));

		phrases.add(new PicWordAction("enough", Pic2NLG.ActionType.ADVERB));
		/*
		 * TODO in here we may have a small problem!! how to decide if a object
		 * shall be definite (le) or not (un), or nothing [TODO:is this lastone
		 * possible]?
		 */
		if (de_explicit) {
			phrases.add(new PicWordAction("of", Pic2NLG.ActionType.PREPOSITION));
		}

		phrases.add(new PicWordAction("candy", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("plural", Pic2NLG.ActionType.NUMBER_AGREEMENT));
		return phrases;
	}

	/* TODO:on fait qqch: Let's eat? */
	public void _test_on_mangera() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("manger", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("future", Pic2NLG.ActionType.TENSE_PRESENT));

		assertEquals("On mangera.", converter.convertPhrasesToNLG(phrases));
	}

	/* object not specified directly */
	public void test_tu_es_joli() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("you", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("be", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("beautiful", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("You are beautiful.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* adverb phrase */
	public void test_tu_es_tres_joli() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("you", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("be", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("very", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("beautiful", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("You are very beautiful.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* triple adverb phrase */
	public void test_tu_es_tres_tres_joli() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("you", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("be", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("very", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("very", Pic2NLG.ActionType.ADVERB));

		phrases.add(new PicWordAction("beautiful", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("You are very very beautiful.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* sort of adverb (time) */
	public void test_tu_es_joli_aujourdhui() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("you", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("be", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("beautiful", Pic2NLG.ActionType.ADJECTIVE));
		phrases.add(new PicWordAction("today", Pic2NLG.ActionType.ADVERB));

		assertEquals("You are beautiful today.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* object not specified directly */
	public void ntest_il_court_tres_viteadverb_phrase() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("he", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("run", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("very", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("quickly", Pic2NLG.ActionType.ADVERB));

		assertEquals("He runs very quickly.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* object not specified directly */
	public void _NOT_IMPORTANT_test_il_court_tres_tres_viteadverb_phrase() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("il", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("courir", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));

		phrases.add(new PicWordAction("vite", Pic2NLG.ActionType.ADVERB));

		assertEquals("Il court très très vite.",
				converter.convertPhrasesToNLG(phrases));
	}

	/*
	 * TODO: Let's go to sleep. reflexive verbs -- use of reflexive verb +
	 * future proche
	 */
	public void _test_lets_go_to_sleep() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));

		NLGElement se_coucher = Pic2NLG.factory.createNLGElement("coucher");
		se_coucher.setFeature(LexicalFeature.REFLEXIVE, true);

		phrases.add(new PicWordAction(se_coucher, Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche", Pic2NLG.ActionType.TENSE_PRESENT));

		assertEquals("On va se coucher.",
				converter.convertPhrasesToNLG(phrases));
	}

	/*
	 * determinants - un, une, le, la, les TODO: note! the gender matters only
	 * on the Noun not it's specifier! e.g. one may feed le chatte --> will be
	 * fixed --> la chatte
	 */
	public void test_le_chat_determinants() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction(Pic2NLG.factory.createNounPhrase("the",
				"cat"), Pic2NLG.ActionType.NOUN));

		VPPhraseSpec verb = Pic2NLG.factory.createVerbPhrase("sleep");
		//verb.setFeature(LexicalFeature.REFLEXIVE, true);

		phrases.add(new PicWordAction(verb, Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche", Pic2NLG.ActionType.TENSE_PRESENT));

		assertEquals("The cat goes to sleep.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* a simple phrase with a modal: vouloir, pouvoir, devoir, etc */
	public void test_je_veux_manger() {
		assertEquals("I want to eat.",
				converter.convertPhrasesToNLG(text_je_veux_manger()));
	}

	/* tests simple negation */
	public void test_je_veux_manger_negated() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));

		assertEquals("I do not want to eat.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* tests simple negation */
	public void test_nous_voulons_manger_negated() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();

		phrases.set(0, new PicWordAction("We", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));

		assertEquals("We do not want to eat.",
				converter.convertPhrasesToNLG(phrases));
	}

	/* tests simple negation */
	public void test_nous_voulons_manger_multi_objects() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();

		phrases.set(0, new PicWordAction("We", Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("potatoes", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("fish", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("cabage", Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("carrots", Pic2NLG.ActionType.NOUN));

		// some, '', or the
		assertEquals("We want to eat potatoes, fish, carrots.",
				converter.convertPhrasesToNLG(phrases));
	}

	public void _test_je_veux_manger_negated_de_object() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));
		// TODO: this is not grammatically correct in case of manger

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("soupe");
		// TODO: try to determine the determiner automatically
		noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je ne veux pas manger de soupe.",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_on_va_partir_de_Nice_de_NounIndObj_auto() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("we", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("leave", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche", Pic2NLG.ActionType.TENSE_PRESENT));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("Nice");
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("We are going to leave Nice.",
				converter.convertPhrasesToNLG(phrases));
	}

	/**
	 * Verbs with: de + verb infinitive, e.g. continuer de faire qqch Indirect
	 * object which is action.
	 * 
	 * so we have: [subject + modal+ verb [ de + [ verb + object ] ] ]
	 * 
	 * TODO: futur proche do not work, if a specific modal has already been
	 * selected -- double modal.
	 */
	public void NOT_IMPORTANT_test_on_va_continuer_de_manger_les_crepes_de_VerbInfin_auto() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("we", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("continuer", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("eat", Pic2NLG.ActionType.VERB));

		phrases.add(new PicWordAction("futur_proche", Pic2NLG.ActionType.TENSE_PRESENT));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("pancake");
		noun.setPlural(true);
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("We are going to continue eating pancakes.",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_remercier_de_automatically() {
		ArrayList<PicWordAction> phrases = get_text_je_veux_vous_remercier(false);

		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("I want to thank you for everything.",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_remercier_de_explicit() {
		ArrayList<PicWordAction> phrases = get_text_je_veux_vous_remercier(true);

		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("I want to thank you for everything.",
				converter.convertPhrasesToNLG(phrases));
	}

	private ArrayList<PicWordAction> get_text_je_veux_vous_remercier(
			Boolean de_explicit) {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("I", Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("want", Pic2NLG.ActionType.VERB));

		phrases.add(new PicWordAction("you", Pic2NLG.ActionType.NOUN));

		// UPPPPS!
		// TODO: vous, nous, etc...

		phrases.add(new PicWordAction("thank", Pic2NLG.ActionType.VERB));

		if (de_explicit)
			phrases.add(new PicWordAction("for", Pic2NLG.ActionType.PREPOSITION));

		// phrases.add(new PicWordAction("futur_proche",
		// Pic2NLG.ActionType.TENSE_PRESENT));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("everything");
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		return phrases;
	}

	public void test_je_veux_manger_a_automatically() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("I", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("live", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche", Pic2NLG.ActionType.TENSE_PRESENT));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("Paris");
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("a");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("I am going to live in Paris.",
				converter.convertPhrasesToNLG(phrases));
	}

	/**
	 * Plural test + [ default prep? De ] bonbons
	 * 
	 * Default prepositions may be: manger - de; aller - a; DE:
	 * http://french.about.com/library/prepositions/bl_prep_de_verb.htm TODO: A
	 * + Verb Inf:
	 * http://french.about.com/library/prepositions/bl_prep_a_verb.htm TODO: A +
	 * Indirect Object:
	 * http://french.about.com/library/prepositions/bl_prep_a_verb2.htm etc
	 * 
	 * 
	 * TODO: can we figure out default preposition and what if more probable to
	 * have a plural or singular for a certain phrase? I'm afraid that involves
	 * complex semantic decisions... see grammar check tools!
	 * 
	 */
	public void _test_je_veux_manger_negated_de_object_plural() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("bonbon");
		noun.setPlural(true);
		// TODO: try to determine the determiner automatically
		noun.setDeterminer("de");

		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));

		// TODO: after second noun the context is the object!
		phrases.add(new PicWordAction("plural", Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je ne veux pas manger de bonbons.",
				converter.convertPhrasesToNLG(phrases));
	}

	/**
	 * Test the genders of multi-gender words, like chanteuse
	 */
	public void _test_je_veux_etre_chanteuse_gender() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		// je avoir [past] assez bonbon [plural] ==> j'ai eu assez des bonbons
		phrases.add(new PicWordAction("je", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("vouloir", Pic2NLG.ActionType.VERB));

		// phrases.add(new PicWordAction("past", Pic2NLG.wordTYPE.TENSE));
		phrases.add(new PicWordAction("etre", Pic2NLG.ActionType.VERB));

		NLGElement chanteuse = Pic2NLG.factory.createNounPhrase("un",
				"chanteur");
		chanteuse.setFeature(LexicalFeature.GENDER, Gender.FEMININE);

		System.out.println(chanteuse);
		phrases.add(new PicWordAction(chanteuse, Pic2NLG.ActionType.NOUN));

		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je veux etre une chanteuse.",
				converter.convertPhrasesToNLG(phrases));
	}

}
