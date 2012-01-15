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

public class Pic2NLGTest extends AndroidTestCase {
	static Pic2NLG converter = null;

	protected void setUp() throws Exception {
		super.setUp();
		if (converter == null)
			converter = new Pic2NLG();
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
				"J'ai eu assez bonbons",
				converter
						.convertPhrasesToNLG(text_jai_eu_assez_de_bonbons(false)));
	}

	public void test_jai_eu_assez_de_bonbons_explicit_de() {
		assertEquals(
				"J'ai eu assez de bonbons",
				converter
						.convertPhrasesToNLG(text_jai_eu_assez_de_bonbons(true)));
	}

	public void test_jai_eu_assez_de_bonbons_adjective() {
		ArrayList<PicWordAction> phrases = text_jai_eu_assez_de_bonbons(false);
		phrases.add(new PicWordAction("délicieux", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("J'ai eu assez de bonbons délicieux",
				converter.convertPhrasesToNLG(phrases));

	}

	/* TEXT */
	private ArrayList<PicWordAction> text_je_veux_manger() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		// je avoir [past] assez bonbon [plural] ==> j'ai eu assez des bonbons
		phrases.add(new PicWordAction("je", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("vouloir", Pic2NLG.ActionType.VERB));

		// Test the creation from DB values
		phrases.add(new PicWordAction("manger", "verb", "",
				SPC_ColorCode.ACTION));

		return phrases;
	}

	private ArrayList<PicWordAction> text_jai_eu_assez_de_bonbons(
			Boolean de_explicit) {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		// je avoir [past] assez bonbon [plural] ==> j'ai eu assez des bonbons
		phrases.add(new PicWordAction("je", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("avoir", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("past", Pic2NLG.ActionType.TENSE_PAST));

		phrases.add(new PicWordAction("assez", Pic2NLG.ActionType.ADVERB));
		/*
		 * TODO in here we may have a small problem!! how to decide if a object
		 * shall be definite (le) or not (un), or nothing [TODO:is this lastone
		 * possible]?
		 */
		if (de_explicit) {
			phrases.add(new PicWordAction("de", Pic2NLG.ActionType.PREPOSITION));
		}

		phrases.add(new PicWordAction("bonbon", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("plural",
				Pic2NLG.ActionType.NUMBER_AGREEMENT));
		return phrases;
	}

	/* on fait qqch */
	public void test_on_mangera() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("manger", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("future", Pic2NLG.ActionType.TENSE_FUTURE));

		assertEquals("On mangera", converter.convertPhrasesToNLG(phrases));
	}

	/* object not specified directly */
	public void test_tu_es_joli() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("tu", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("être", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("joli", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("Tu es joli", converter.convertPhrasesToNLG(phrases));
	}

	/* gender specified for je/I */
	public void test_je_suis_heureuse() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		NPPhraseSpec je = Pic2NLG.factory.createNounPhrase("je");
		je.setFeature(LexicalFeature.GENDER, Gender.FEMININE);

		phrases.add(new PicWordAction(je, Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("être", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("heureux", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("Je suis heureuse", converter.convertPhrasesToNLG(phrases));
	}

	/* adverb phrase */
	public void test_tu_es_tres_joli() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("tu", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("être", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("joli", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("Tu es très joli", converter.convertPhrasesToNLG(phrases));
	}

	/* triple adverb phrase */
	public void test_tu_es_tres_tres_joli() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("tu", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("être", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));

		phrases.add(new PicWordAction("joli", Pic2NLG.ActionType.ADJECTIVE));

		assertEquals("Tu es très très joli",
				converter.convertPhrasesToNLG(phrases));
	}

	/* sort of adverb (time) */
	public void test_tu_es_joli_aujourdhui() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("tu", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("être", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("joli", Pic2NLG.ActionType.ADJECTIVE));
		phrases.add(new PicWordAction("aujourd'hui", Pic2NLG.ActionType.ADVERB));

		assertEquals("Tu es joli aujourd'hui",
				converter.convertPhrasesToNLG(phrases));
	}

	/* object not specified directly */
	public void ntest_il_court_tres_viteadverb_phrase() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("il", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("courir", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("vite", Pic2NLG.ActionType.ADVERB));

		assertEquals("Il court très vite",
				converter.convertPhrasesToNLG(phrases));
	}

	/* object not specified directly */
	public void NOT_IMPORTANT_test_il_court_tres_tres_viteadverb_phrase() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("il", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("courir", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));
		phrases.add(new PicWordAction("très", Pic2NLG.ActionType.ADVERB));

		phrases.add(new PicWordAction("vite", Pic2NLG.ActionType.ADVERB));

		assertEquals("Il court très très vite",
				converter.convertPhrasesToNLG(phrases));
	}

	/* reflexive verbs -- use of reflexive verb + future proche */
	public void test_on_va_se_coucher() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));

		NLGElement se_coucher = Pic2NLG.factory.createNLGElement("coucher");
		se_coucher.setFeature(LexicalFeature.REFLEXIVE, true);

		phrases.add(new PicWordAction(se_coucher, Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche",
				Pic2NLG.ActionType.TENSE_PRESENT));

		assertEquals("On va se coucher", converter.convertPhrasesToNLG(phrases));
	}

	/* TODO: future proche do not work if selected before the verb */
	public void TODO_NOT_IMPORTANT_test_on_va_se_coucher_futur_proche_before_verb() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));

		NLGElement se_coucher = Pic2NLG.factory.createNLGElement("coucher");
		se_coucher.setFeature(LexicalFeature.REFLEXIVE, true);
		phrases.add(new PicWordAction("futur_proche",
				Pic2NLG.ActionType.TENSE_FUTUR_PROCHE));

		phrases.add(new PicWordAction(se_coucher, Pic2NLG.ActionType.VERB));

		assertEquals("On va se coucher", converter.convertPhrasesToNLG(phrases));
	}

	/* reflexive verbs - past */
	public void test_on_s_est_couche() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		NPPhraseSpec subject = Pic2NLG.factory.createNounPhrase("on");

		phrases.add(new PicWordAction(subject, Pic2NLG.ActionType.NOUN));

		NLGElement se_coucher = Pic2NLG.factory.createNLGElement("coucher");
		se_coucher.setFeature(LexicalFeature.REFLEXIVE, true);

		phrases.add(new PicWordAction(se_coucher, Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("past", Pic2NLG.ActionType.TENSE_PAST));

		assertEquals("On s'est couché", converter.convertPhrasesToNLG(phrases));
	}

	/*
	 * determinants - un, une, le, la, les TODO: note! the gender matters only
	 * on the Noun not it's specifier! e.g. one may feed le chatte --> will be
	 * fixed --> la chatte
	 */
	public void test_le_chat_determinants() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction(Pic2NLG.factory.createNounPhrase("le",
				"chat"), Pic2NLG.ActionType.NOUN));

		VPPhraseSpec verb = Pic2NLG.factory.createVerbPhrase("coucher");
		verb.setFeature(LexicalFeature.REFLEXIVE, true);

		phrases.add(new PicWordAction(verb, Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche",
				Pic2NLG.ActionType.TENSE_FUTUR_PROCHE));

		assertEquals("Le chat va se coucher",
				converter.convertPhrasesToNLG(phrases));
	}

	/* a simple phrase with a modal: vouloir, pouvoir, devoir, etc */
	public void test_je_veux_manger() {
		assertEquals("Je veux manger",
				converter.convertPhrasesToNLG(text_je_veux_manger()));
	}

	/* tests simple negation */
	public void test_je_veux_manger_negated() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));

		assertEquals("Je ne veux pas manger",
				converter.convertPhrasesToNLG(phrases));
	}

	/* tests simple negation */
	public void test_nous_voulons_manger_negated() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();

		phrases.set(0, new PicWordAction("nous", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));

		assertEquals("Nous ne voulons pas manger",
				converter.convertPhrasesToNLG(phrases));
	}

	/* tests simple negation */
	public void test_nous_voulons_manger_multi_objects() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();

		phrases.set(0, new PicWordAction("nous", Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("patates", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("poisson", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("chou", Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("carrotes", Pic2NLG.ActionType.NOUN));

		assertEquals(
				"Nous voulons manger des patates, du poisson, du chou et des carrotes",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_je_veux_manger_negated_de_object() {
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
		assertEquals("Je ne veux pas manger de soupe",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_on_va_partir_de_Nice_de_NounIndObj_auto() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("partir", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche",
				Pic2NLG.ActionType.TENSE_FUTUR_PROCHE));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("Nice");
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("On va partir de Nice",
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

		phrases.add(new PicWordAction("on", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("aller", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("continuer", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("manger", Pic2NLG.ActionType.VERB));

		phrases.add(new PicWordAction("futur_proche",
				Pic2NLG.ActionType.TENSE_FUTUR_PROCHE));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("le", "crêpe");
		noun.setPlural(true);
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("On va continuer de manger les crêpes",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_remercier_de_automatically() {
		ArrayList<PicWordAction> phrases = get_text_je_veux_vous_remercier(false);

		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je veux vous remercier de tout",
				converter.convertPhrasesToNLG(phrases));
	}

	public void test_remercier_de_explicit() {
		ArrayList<PicWordAction> phrases = get_text_je_veux_vous_remercier(true);

		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je veux vous remercier de tout",
				converter.convertPhrasesToNLG(phrases));
	}

	private ArrayList<PicWordAction> get_text_je_veux_vous_remercier(
			Boolean de_explicit) {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("je", Pic2NLG.ActionType.NOUN));

		phrases.add(new PicWordAction("vouloir", Pic2NLG.ActionType.VERB));

		phrases.add(new PicWordAction("vous", Pic2NLG.ActionType.NOUN));

		// UPPPPS!
		// TODO: vous, nous, etc...

		phrases.add(new PicWordAction("remercier", Pic2NLG.ActionType.VERB));

		if (de_explicit)
			phrases.add(new PicWordAction("de", Pic2NLG.ActionType.PREPOSITION));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("tout");
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("de");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		return phrases;
	}

	public void test_je_veux_manger_a_automatically() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();

		phrases.add(new PicWordAction("je", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("habiter", Pic2NLG.ActionType.VERB));
		phrases.add(new PicWordAction("futur_proche",
				Pic2NLG.ActionType.TENSE_FUTUR_PROCHE));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("Paris");
		// TODO: try to determine the determiner automatically
		// noun.setDeterminer("a");
		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));
		// TODO: after second noun the context is object!
		// TODO: phrases.add(new PicWordAction("plural",
		// Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je vais habiter à Paris",
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
	public void test_je_veux_manger_negated_de_object_plural() {
		ArrayList<PicWordAction> phrases = text_je_veux_manger();
		phrases.add(new PicWordAction("true", Pic2NLG.ActionType.NEGATED));

		NPPhraseSpec noun = Pic2NLG.factory.createNounPhrase("bonbon");
		noun.setPlural(true);
		// TODO: try to determine the determiner automatically
		noun.setDeterminer("de");

		phrases.add(new PicWordAction(noun, Pic2NLG.ActionType.NOUN));

		// TODO: after second noun the context is the object!
		phrases.add(new PicWordAction("plural",
				Pic2NLG.ActionType.NUMBER_AGREEMENT));
		assertEquals("Je ne veux pas manger de bonbons",
				converter.convertPhrasesToNLG(phrases));
	}

	/**
	 * Test the genders of multi-gender words, like chanteuse
	 */
	public void test_je_veux_etre_chanteuse_gender() {
		ArrayList<PicWordAction> phrases = new ArrayList<PicWordAction>();
		// je avoir [past] assez bonbon [plural] ==> j'ai eu assez des bonbons
		phrases.add(new PicWordAction("je", Pic2NLG.ActionType.NOUN));
		phrases.add(new PicWordAction("vouloir", Pic2NLG.ActionType.VERB));

		phrases.add(new PicWordAction("etre", Pic2NLG.ActionType.VERB));

		NLGElement chanteuse = Pic2NLG.factory.createNounPhrase("un",
				"chanteur");
		chanteuse.setFeature(LexicalFeature.GENDER, Gender.FEMININE);

		System.out.println(chanteuse);
		phrases.add(new PicWordAction(chanteuse, Pic2NLG.ActionType.NOUN));

		assertEquals("Je veux etre une chanteuse",
				converter.convertPhrasesToNLG(phrases));
	}

}
