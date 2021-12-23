/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is "Simplenlg".
 *
 * The Initial Developer of the Original Code is Ehud Reiter, Albert Gatt and Dave Westwater.
 * Portions created by Ehud Reiter, Albert Gatt and Dave Westwater are Copyright (C) 2010-11 The University of Aberdeen. All Rights Reserved.
 *
 * Contributor(s): Ehud Reiter, Albert Gatt, Dave Wewstwater, Roman Kutlak, Margaret Mitchell, Pierre-Luc Vaudry.
 */
package simplenlg.morphology.english;

import simplenlg.features.DiscourseFunction;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.Gender;
import simplenlg.features.InternalFeature;
import simplenlg.features.LexicalFeature;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Pattern;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGElement;
import simplenlg.framework.PhraseCategory;
import simplenlg.framework.StringElement;
import simplenlg.framework.WordElement;
import simplenlg.morphology.MorphologyRulesInterface;

/**
 * This is a dynamic version of the abstract class MorphologyRules, with
 * non static public (or protected) methods. It implements MorphologyRulesInterface.
 * The doDeterminerMorphology() method has been modified.
 * 
 * @author vaudrypl
 */
public class NonStaticMorphologyRules implements MorphologyRulesInterface {

	/**
	 * A triple array of Pronouns organised by singular/plural,
	 * possessive/reflexive/subjective/objective and by gender/person.
	 */
	@SuppressWarnings("nls")
	private static final String[][][] PRONOUNS = {
			{ { "I", "you", "he", "she", "it" },
					{ "me", "you", "him", "her", "it" },
					{ "myself", "yourself", "himself", "herself", "itself" },
					{ "mine", "yours", "his", "hers", "its" },
					{ "my", "your", "his", "her", "its" } },
			{
					{ "we", "you", "they", "they", "they" },
					{ "us", "you", "them", "them", "them" },
					{ "ourselves", "yourselves", "themselves", "themselves",
							"themselves" },
					{ "ours", "yours", "theirs", "theirs", "theirs" },
					{ "our", "your", "their", "their", "their" } } };

	/**
	 * This method performs the morphology for nouns.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public StringElement doNounMorphology(
			InflectedWordElement element, WordElement baseWord) {
		StringBuffer realised = new StringBuffer();

		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);

		if (element.isPlural()
				&& !element.getFeatureAsBoolean(LexicalFeature.PROPER)
						.booleanValue()) {

			String pluralForm = null;

			// AG changed: now check if default infl is uncount
			// if (element.getFeatureAsBoolean(LexicalFeature.NON_COUNT)
			// .booleanValue()) {
			// pluralForm = baseForm;
			String elementDefaultInfl = element
					.getFeatureAsString(LexicalFeature.DEFAULT_INFL);
			if (elementDefaultInfl != null && elementDefaultInfl.equals("uncount")) {
				pluralForm = baseForm;
			} else {
				pluralForm = element.getFeatureAsString(LexicalFeature.PLURAL);
			}

			if (pluralForm == null && baseWord != null) {
				// AG changed: now check if default infl is uncount
				// if (baseWord.getFeatureAsBoolean(LexicalFeature.NON_COUNT)
				// .booleanValue()) {
				// pluralForm = baseForm;
				String baseDefaultInfl = baseWord.getFeatureAsString(LexicalFeature.DEFAULT_INFL);
				if (baseDefaultInfl != null && baseDefaultInfl.equals("uncount")) {
					pluralForm = baseForm;
				} else {
					pluralForm = baseWord
							.getFeatureAsString(LexicalFeature.PLURAL);
				}
			}
			
			if (pluralForm == null) {
				Object pattern = element.getFeature(Feature.PATTERN);
				if (Pattern.GRECO_LATIN_REGULAR.equals(pattern)) {
					pluralForm = buildGrecoLatinPluralNoun(baseForm);
				} else {
					pluralForm = buildRegularPluralNoun(baseForm);
				}
			}
			realised.append(pluralForm);
		
		} else {
			realised.append(baseForm);
		}
		
		checkPossessive(element, realised);
		// vaudrypl added element as 2nd argument
		StringElement realisedElement = new StringElement(realised.toString(), element);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element
				.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}

	/**
	 * Builds a plural for regular nouns. The rules are performed in this order:
	 * <ul>
	 * <li>For nouns ending <em>-Cy</em>, where C is any consonant, the ending
	 * becomes <em>-ies</em>. For example, <em>fly</em> becomes <em>flies</em>.</li>
	 * <li>For nouns ending <em>-ch</em>, <em>-s</em>, <em>-sh</em>, <em>-x</em>
	 * or <em>-z</em> the ending becomes <em>-es</em>. For example, <em>box</em>
	 * becomes <em>boxes</em>.</li>
	 * <li>All other nouns have <em>-s</em> appended the other end. For example,
	 * <em>dog</em> becomes <em>dogs</em>.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildRegularPluralNoun(String baseForm) {
		String plural = null;
		if (baseForm != null) {
			if (baseForm.matches(".*[b-z&&[^eiou]]y\\b")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("y\\b", "ies"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.matches(".*[szx(ch)(sh)]\\b")) { //$NON-NLS-1$
				plural = baseForm + "es"; //$NON-NLS-1$
			} else {
				plural = baseForm + "s"; //$NON-NLS-1$
			}
		}
		return plural;
	}

	/**
	 * Builds a plural for Greco-Latin regular nouns. The rules are performed in
	 * this order:
	 * <ul>
	 * <li>For nouns ending <em>-us</em> the ending becomes <em>-i</em>. For
	 * example, <em>focus</em> becomes <em>foci</em>.</li>
	 * <li>For nouns ending <em>-ma</em> the ending becomes <em>-mata</em>. For
	 * example, <em>trauma</em> becomes <em>traumata</em>.</li>
	 * <li>For nouns ending <em>-a</em> the ending becomes <em>-ae</em>. For
	 * example, <em>larva</em> becomes <em>larvae</em>.</li>
	 * <li>For nouns ending <em>-um</em> or <em>-on</em> the ending becomes
	 * <em>-a</em>. For example, <em>taxon</em> becomes <em>taxa</em>.</li>
	 * <li>For nouns ending <em>-sis</em> the ending becomes <em>-ses</em>. For
	 * example, <em>analysis</em> becomes <em>analyses</em>.</li>
	 * <li>For nouns ending <em>-is</em> the ending becomes <em>-ides</em>. For
	 * example, <em>cystis</em> becomes <em>cystides</em>.</li>
	 * <li>For nouns ending <em>-men</em> the ending becomes <em>-mina</em>. For
	 * example, <em>foramen</em> becomes <em>foramina</em>.</li>
	 * <li>For nouns ending <em>-ex</em> the ending becomes <em>-ices</em>. For
	 * example, <em>index</em> becomes <em>indices</em>.</li>
	 * <li>For nouns ending <em>-x</em> the ending becomes <em>-ces</em>. For
	 * example, <em>matrix</em> becomes <em>matrices</em>.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildGrecoLatinPluralNoun(String baseForm) {
		String plural = null;
		if (baseForm != null) {
			if (baseForm.endsWith("us")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("us\\b", "i"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("ma")) { //$NON-NLS-1$
				plural = baseForm + "ta"; //$NON-NLS-1$
			} else if (baseForm.endsWith("a")) { //$NON-NLS-1$
				plural = baseForm + "e"; //$NON-NLS-1$
			} else if (baseForm.matches(".*[(um)(on)]\\b")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("[(um)(on)]\\b", "a"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("sis")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("sis\\b", "ses"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("is")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("is\\b", "ides"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("men")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("men\\b", "mina"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("ex")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("ex\\b", "ices"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("x")) { //$NON-NLS-1$
				plural = baseForm.replaceAll("x\\b", "ces"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				plural = baseForm;
			}
		}
		return plural;
	}

	/**
	 * This method performs the morphology for verbs.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public NLGElement doVerbMorphology(InflectedWordElement element,
			WordElement baseWord) {

		String realised = null;
		Object numberValue = element.getFeature(Feature.NUMBER);
		Object personValue = element.getFeature(Feature.PERSON);
		Tense tenseValue = element.getTense();
		Object formValue = element.getFeature(Feature.FORM);
		Object patternValue = element.getFeature(Feature.PATTERN);

		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);

		if (element.isNegated() || Form.BARE_INFINITIVE.equals(formValue)) {
			realised = baseForm;
		} else if (Form.PRESENT_PARTICIPLE.equals(formValue)) {
			realised = element
					.getFeatureAsString(LexicalFeature.PRESENT_PARTICIPLE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.PRESENT_PARTICIPLE);
			}
			if (realised == null) {
				if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
					realised = buildDoublePresPartVerb(baseForm);
				} else {
					realised = buildRegularPresPartVerb(baseForm);
				}
			}
		} else if (Tense.PAST.equals(tenseValue)
				|| Form.PAST_PARTICIPLE.equals(formValue)) {
			if (Form.PAST_PARTICIPLE.equals(formValue)) {
				realised = element
						.getFeatureAsString(LexicalFeature.PAST_PARTICIPLE);

				if (realised == null && baseWord != null) {
					realised = baseWord
							.getFeatureAsString(LexicalFeature.PAST_PARTICIPLE);
				}
				if (realised == null) {
					if ("be".equalsIgnoreCase(baseForm)) { //$NON-NLS-1$
						realised = "been"; //$NON-NLS-1$
					} else if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
						realised = buildDoublePastVerb(baseForm);
					} else {
						realised = buildRegularPastVerb(baseForm, numberValue);
					}
				}
			} else {
				realised = element.getFeatureAsString(LexicalFeature.PAST);

				if (realised == null && baseWord != null) {
					realised = baseWord.getFeatureAsString(LexicalFeature.PAST);
				}
				if (realised == null) {
					if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
						realised = buildDoublePastVerb(baseForm);
					} else {
						realised = buildRegularPastVerb(baseForm, numberValue);
					}
				}
			}
		} else if ((numberValue == null || NumberAgreement.SINGULAR
				.equals(numberValue))
				&& (personValue == null || Person.THIRD.equals(personValue))
				&& (tenseValue == null || Tense.PRESENT.equals(tenseValue))) {

			realised = element.getFeatureAsString(LexicalFeature.PRESENT3S);

			if (realised == null && baseWord != null
					&& !"be".equalsIgnoreCase(baseForm)) { //$NON-NLS-1$
				realised = baseWord
						.getFeatureAsString(LexicalFeature.PRESENT3S);
			}
			if (realised == null) {
				realised = buildPresent3SVerb(baseForm);
			}
		} else {
			if ("be".equalsIgnoreCase(baseForm)) { //$NON-NLS-1$
				if (Person.FIRST.equals(personValue)
						&& (NumberAgreement.SINGULAR.equals(numberValue) || numberValue == null)) {
					realised = "am"; //$NON-NLS-1$
				} else {
					realised = "are"; //$NON-NLS-1$
				}
			} else {
				realised = baseForm;
			}
		}
		// vaudrypl added element as 2nd argument
		StringElement realisedElement = new StringElement(realised, element);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element
				.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}

	/**
	 * return the base form of a word
	 * 
	 * @param element
	 * @param baseWord
	 * @return
	 */
	protected String getBaseForm(InflectedWordElement element,
			WordElement baseWord) {
		// unclear what the right behaviour should be
		// for now, prefer baseWord.getBaseForm() to element.getBaseForm() for
		// verbs (ie, "is" mapped to "be")
		// but prefer element.getBaseForm() to baseWord.getBaseForm() for other
		// words (ie, "children" not mapped to "child")

		if (LexicalCategory.VERB == element.getCategory()) {
			if (baseWord != null && baseWord.getBaseForm() != null)
				return baseWord.getBaseForm();
			else
				return element.getBaseForm();
		} else {
			if (element.getBaseForm() != null)
				return element.getBaseForm();
			else if (baseWord == null)
				return null;
			else
				return baseWord.getBaseForm();
		}
	}

	/**
	 * Checks to see if the noun is possessive. If it is then nouns in ending in
	 * <em>-s</em> become <em>-s'</em> while every other noun has <em>-'s</em> appended to
	 * the end.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>
	 * @param realised
	 *            the realisation of the word.
	 */
	protected void checkPossessive(InflectedWordElement element,
			StringBuffer realised) {

		if (element.getFeatureAsBoolean(Feature.POSSESSIVE).booleanValue()) {
			if (realised.charAt(realised.length() - 1) == 's') {
				realised.append('\'');

			} else {
				realised.append("'s"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Builds the third-person singular form for regular verbs. The rules are
	 * performed in this order:
	 * <ul>
	 * <li>If the verb is <em>be</em> the realised form is <em>is</em>.</li>
	 * <li>For verbs ending <em>-ch</em>, <em>-s</em>, <em>-sh</em>, <em>-x</em>
	 * or <em>-z</em> the ending becomes <em>-es</em>. For example,
	 * <em>preach</em> becomes <em>preaches</em>.</li>
	 * <li>For verbs ending <em>-y</em> the ending becomes <em>-ies</em>. For
	 * example, <em>fly</em> becomes <em>flies</em>.</li>
	 * <li>For every other verb, <em>-s</em> is added to the end of the word.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildPresent3SVerb(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			if (baseForm.equalsIgnoreCase("be")) { //$NON-NLS-1$
				morphology = "is"; //$NON-NLS-1$
			} else if (baseForm.matches(".*[szx(ch)(sh)]\\b")) { //$NON-NLS-1$
				morphology = baseForm + "es"; //$NON-NLS-1$
			} else if (baseForm.matches(".*[b-z&&[^eiou]]y\\b")) { //$NON-NLS-1$
				morphology = baseForm.replaceAll("y\\b", "ies"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				morphology = baseForm + "s"; //$NON-NLS-1$
			}
		}
		return morphology;
	}

	/**
	 * Builds the past-tense form for regular verbs. The rules are performed in
	 * this order:
	 * <ul>
	 * <li>If the verb is <em>be</em> and the number agreement is plural then
	 * the realised form is <em>were</em>.</li>
	 * <li>If the verb is <em>be</em> and the number agreement is singular then
	 * the realised form is <em>was</em>.</li>
	 * <li>For verbs ending <em>-e</em> the ending becomes <em>-ed</em>. For
	 * example, <em>chased</em> becomes <em>chased</em>.</li>
	 * <li>For verbs ending <em>-Cy</em>, where C is any consonant, the ending
	 * becomes <em>-ied</em>. For example, <em>dry</em> becomes <em>dried</em>.</li>
	 * <li>For every other verb, <em>-ed</em> is added to the end of the word.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @param number
	 *            the number agreement for the word.
	 * @return the inflected word.
	 */
	protected String buildRegularPastVerb(String baseForm, Object number) {
		String morphology = null;
		if (baseForm != null) {
			if (baseForm.equalsIgnoreCase("be")) { //$NON-NLS-1$
				if (NumberAgreement.PLURAL.equals(number)) {
					morphology = "were"; //$NON-NLS-1$
				} else {
					morphology = "was"; //$NON-NLS-1$
				}
			} else if (baseForm.endsWith("e")) { //$NON-NLS-1$
				morphology = baseForm + "d"; //$NON-NLS-1$
			} else if (baseForm.matches(".*[b-z&&[^eiou]]y\\b")) { //$NON-NLS-1$
				morphology = baseForm.replaceAll("y\\b", "ied"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				morphology = baseForm + "ed"; //$NON-NLS-1$
			}
		}
		return morphology;
	}

	/**
	 * Builds the past-tense form for verbs that follow the doubling form of the
	 * last consonant. <em>-ed</em> is added to the end after the last consonant
	 * is doubled. For example, <em>tug</em> becomes <em>tugged</em>.
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildDoublePastVerb(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			morphology = baseForm + baseForm.charAt(baseForm.length() - 1)
					+ "ed"; //$NON-NLS-1$
		}
		return morphology;
	}

	/**
	 * Builds the present participle form for regular verbs. The rules are
	 * performed in this order:
	 * <ul>
	 * <li>If the verb is <em>be</em> then the realised form is <em>being</em>.</li>
	 * <li>For verbs ending <em>-ie</em> the ending becomes <em>-ying</em>. For
	 * example, <em>tie</em> becomes <em>tying</em>.</li>
	 * <li>For verbs ending <em>-ee</em>, <em>-oe</em> or <em>-ye</em> then
	 * <em>-ing</em> is added to the end. For example, <em>canoe</em> becomes
	 * <em>canoeing</em>.</li>
	 * <li>For other verbs ending in <em>-e</em> the ending becomes
	 * <em>-ing</em>. For example, <em>chase</em> becomes <em>chasing</em>.</li>
	 * <li>For all other verbs, <em>-ing</em> is added to the end. For example,
	 * <em>dry</em> becomes <em>drying</em>.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @param number
	 *            the number agreement for the word.
	 * @return the inflected word.
	 */
	protected String buildRegularPresPartVerb(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			if (baseForm.equalsIgnoreCase("be")) { //$NON-NLS-1$
				morphology = "being"; //$NON-NLS-1$
			} else if (baseForm.endsWith("ie")) { //$NON-NLS-1$
				morphology = baseForm.replaceAll("ie\\b", "ying"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.matches(".*[^iyeo]e\\b")) { //$NON-NLS-1$
				morphology = baseForm.replaceAll("e\\b", "ing"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				morphology = baseForm + "ing"; //$NON-NLS-1$
			}
		}
		return morphology;
	}

	/**
	 * Builds the present participle form for verbs that follow the doubling
	 * form of the last consonant. <em>-ing</em> is added to the end after the
	 * last consonant is doubled. For example, <em>tug</em> becomes
	 * <em>tugging</em>.
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildDoublePresPartVerb(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			morphology = baseForm + baseForm.charAt(baseForm.length() - 1)
					+ "ing"; //$NON-NLS-1$
		}
		return morphology;
	}

	/**
	 * This method performs the morphology for adjectives.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public NLGElement doAdjectiveMorphology(
			InflectedWordElement element, WordElement baseWord) {

		String realised = null;
		Object patternValue = element.getFeature(Feature.PATTERN);

		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);

		if (element.getFeatureAsBoolean(Feature.IS_COMPARATIVE).booleanValue()) {
			realised = element.getFeatureAsString(LexicalFeature.COMPARATIVE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.COMPARATIVE);
			}
			if (realised == null) {
				if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
					realised = buildDoubleCompAdjective(baseForm);
				} else {
					realised = buildRegularComparative(baseForm);
				}
			}
		} else if (element.getFeatureAsBoolean(Feature.IS_SUPERLATIVE)
				.booleanValue()) {

			realised = element.getFeatureAsString(LexicalFeature.SUPERLATIVE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.SUPERLATIVE);
			}
			if (realised == null) {
				if (Pattern.REGULAR_DOUBLE.equals(patternValue)) {
					realised = buildDoubleSuperAdjective(baseForm);
				} else {
					realised = buildRegularSuperlative(baseForm);
				}
			}
		} else {
			realised = baseForm;
		}
		// vaudrypl added element as 2nd argument
		StringElement realisedElement = new StringElement(realised, element);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element
				.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}

	/**
	 * Builds the comparative form for adjectives that follow the doubling form
	 * of the last consonant. <em>-er</em> is added to the end after the last
	 * consonant is doubled. For example, <em>fat</em> becomes <em>fatter</em>.
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildDoubleCompAdjective(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			morphology = baseForm + baseForm.charAt(baseForm.length() - 1)
					+ "er"; //$NON-NLS-1$
		}
		return morphology;
	}

	/**
	 * Builds the comparative form for regular adjectives. The rules are
	 * performed in this order:
	 * <ul>
	 * <li>For verbs ending <em>-Cy</em>, where C is any consonant, the ending
	 * becomes <em>-ier</em>. For example, <em>brainy</em> becomes
	 * <em>brainier</em>.</li>
	 * <li>For verbs ending <em>-e</em> the ending becomes <em>-er</em>. For
	 * example, <em>fine</em> becomes <em>finer</em>.</li>
	 * <li>For all other verbs, <em>-er</em> is added to the end. For example,
	 * <em>clear</em> becomes <em>clearer</em>.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @param number
	 *            the number agreement for the word.
	 * @return the inflected word.
	 */
	protected String buildRegularComparative(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			if (baseForm.matches(".*[b-z&&[^eiou]]y\\b")) { //$NON-NLS-1$
				morphology = baseForm.replaceAll("y\\b", "ier"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("e")) { //$NON-NLS-1$
				morphology = baseForm + "r"; //$NON-NLS-1$
			} else {
				morphology = baseForm + "er"; //$NON-NLS-1$
			}
		}
		return morphology;
	}

	/**
	 * Builds the superlative form for adjectives that follow the doubling form
	 * of the last consonant. <em>-est</em> is added to the end after the last
	 * consonant is doubled. For example, <em>fat</em> becomes <em>fattest</em>.
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @return the inflected word.
	 */
	protected String buildDoubleSuperAdjective(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			morphology = baseForm + baseForm.charAt(baseForm.length() - 1)
					+ "est"; //$NON-NLS-1$
		}
		return morphology;
	}

	/**
	 * Builds the superlative form for regular adjectives. The rules are
	 * performed in this order:
	 * <ul>
	 * <li>For verbs ending <em>-Cy</em>, where C is any consonant, the ending
	 * becomes <em>-iest</em>. For example, <em>brainy</em> becomes
	 * <em>brainiest</em>.</li>
	 * <li>For verbs ending <em>-e</em> the ending becomes <em>-est</em>. For
	 * example, <em>fine</em> becomes <em>finest</em>.</li>
	 * <li>For all other verbs, <em>-est</em> is added to the end. For example,
	 * <em>clear</em> becomes <em>clearest</em>.</li>
	 * </ul>
	 * 
	 * @param baseForm
	 *            the base form of the word.
	 * @param number
	 *            the number agreement for the word.
	 * @return the inflected word.
	 */
	protected String buildRegularSuperlative(String baseForm) {
		String morphology = null;
		if (baseForm != null) {
			if (baseForm.matches(".*[b-z&&[^eiou]]y\\b")) { //$NON-NLS-1$
				morphology = baseForm.replaceAll("y\\b", "iest"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (baseForm.endsWith("e")) { //$NON-NLS-1$
				morphology = baseForm + "st"; //$NON-NLS-1$
			} else {
				morphology = baseForm + "est"; //$NON-NLS-1$
			}
		}
		return morphology;
	}

	/**
	 * This method performs the morphology for adverbs.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @param baseWord
	 *            the <code>WordElement</code> as created from the lexicon
	 *            entry.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public NLGElement doAdverbMorphology(InflectedWordElement element,
			WordElement baseWord) {

		String realised = null;

		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);

		if (element.getFeatureAsBoolean(Feature.IS_COMPARATIVE).booleanValue()) {
			realised = element.getFeatureAsString(LexicalFeature.COMPARATIVE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.COMPARATIVE);
			}
			if (realised == null) {
				realised = buildRegularComparative(baseForm);
			}
		} else if (element.getFeatureAsBoolean(Feature.IS_SUPERLATIVE)
				.booleanValue()) {

			realised = element.getFeatureAsString(LexicalFeature.SUPERLATIVE);

			if (realised == null && baseWord != null) {
				realised = baseWord
						.getFeatureAsString(LexicalFeature.SUPERLATIVE);
			}
			if (realised == null) {
				realised = buildRegularSuperlative(baseForm);
			}
		} else {
			realised = baseForm;
		}
		// vaudrypl added element as 2nd argument
		StringElement realisedElement = new StringElement(realised, element);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element
				.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}

	/**
	 * This method performs the morphology for pronouns.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @return a <code>StringElement</code> representing the word after
	 *         inflection.
	 */
	public NLGElement doPronounMorphology(InflectedWordElement element) {
		String realised = null;

		if (!element.getFeatureAsBoolean(InternalFeature.NON_MORPH)
				.booleanValue()) {
			Object genderValue = element.getFeature(LexicalFeature.GENDER);
			Object personValue = element.getFeature(Feature.PERSON);
			
			// way of getting discourseValue changed by vaudrypl
			NLGElement parent = element.getParent();
			Object discourseValue = element.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			if (discourseValue == DiscourseFunction.SUBJECT && parent != null
					&& parent.isA(PhraseCategory.NOUN_PHRASE)) {
				discourseValue = parent.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			}
			if (!(discourseValue instanceof DiscourseFunction)) discourseValue = DiscourseFunction.SUBJECT;

			int numberIndex = element.isPlural() ? 1 : 0;
			int genderIndex = (genderValue instanceof Gender) ? ((Gender) genderValue)
					.ordinal()
					: 2;

			int personIndex = (personValue instanceof Person) ? ((Person) personValue)
					.ordinal()
					: 2;

			if (personIndex == 2) {
				personIndex += genderIndex;
			}

			int positionIndex = 0;

			if (element.getFeatureAsBoolean(LexicalFeature.REFLEXIVE)
					.booleanValue()) {
				positionIndex = 2;
			} else if (element.getFeatureAsBoolean(Feature.POSSESSIVE)
					.booleanValue()) {
				positionIndex = 3;
				if (DiscourseFunction.SPECIFIER.equals(discourseValue)) {
					positionIndex++;
				}
			} else {
				positionIndex = (DiscourseFunction.SUBJECT
						.equals(discourseValue) && !element
						.getFeatureAsBoolean(Feature.PASSIVE).booleanValue())
						|| (DiscourseFunction.OBJECT
								.equals(discourseValue) && element
								.getFeatureAsBoolean(Feature.PASSIVE).booleanValue())
						|| DiscourseFunction.SPECIFIER.equals(discourseValue)
						|| (DiscourseFunction.COMPLEMENT.equals(discourseValue) && element
								.getFeatureAsBoolean(Feature.PASSIVE)
								.booleanValue()) ? 0 : 1;
			}
			realised = PRONOUNS[numberIndex][positionIndex][personIndex];
		} else {
			realised = element.getBaseForm();
		}
		// vaudrypl added element as 2nd argument
		StringElement realisedElement = new StringElement(realised, element);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element
				.getFeature(InternalFeature.DISCOURSE_FUNCTION));

		return realisedElement;
	}

	/**
	 * This method performs the morphology for determiners.
	 * Modified from the same method in the
	 * simplenlg.morphology.english.MorphologyRules class.
	 * 
	 * @param element
	 *            the <code>InflectedWordElement</code>.
	 * @author vaudrypl
	 */
	public NLGElement doDeterminerMorphology(InflectedWordElement element) {
		String inflectedForm = element.getBaseForm();
		if (inflectedForm.equals("a")) {
			if (element.isPlural()) {
				inflectedForm= "some";
//			} else {
//				WordElement nextWord = (WordElement) element.getParent().getFeature(InternalFeature.HEAD);
//				if (nextWord.getRealisation().matches("\\A(a|e|i|o|u).*")) { //$NON-NLS-1$
//					element.setRealisation("an"); //$NON-NLS-1$
//				}
			}
		}
		// vaudrypl added element as 2nd argument
		StringElement realisedElement = new StringElement(inflectedForm, element);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element
				.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}
}
