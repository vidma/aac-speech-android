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
package simplenlg.syntax.french;

import java.util.List;

import simplenlg.features.ClauseStatus;
import simplenlg.features.DiscourseFunction;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.InternalFeature;
import simplenlg.features.LexicalFeature;
import simplenlg.features.Gender;
import simplenlg.features.Person;
import simplenlg.features.french.FrenchLexicalFeature;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.ListElement;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.framework.PhraseElement;
import simplenlg.framework.WordElement;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;

/**
 * This is a helper class containing the main methods for realising the syntax
 * of clauses for French.
 * 
 * Reference :
 * Grevisse, Maurice (1993). Le bon usage, grammaire française,
 * 12e édition refondue par André Goosse, 8e tirage, Éditions Duculot,
 * Louvain-la-Neuve, Belgique.
 * 
 * @author vaudrypl
 */
public class ClauseHelper extends simplenlg.syntax.english.nonstatic.ClauseHelper {
	/**
	 * This method does nothing in the French clause syntax helper.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 * @param phraseFactory
	 *            the phrase factory to be used.
	 */
	@Override
	protected void addEndingTo(PhraseElement phrase,
			ListElement realisedElement, NLGFactory phraseFactory) {}

	/**
	 * Checks the subjects of the phrase to determine if there is more than one
	 * subject. This ensures that the verb phrase is correctly set. Also set
	 * person and gender correctly.
	 * Also sets FrenchLexicalFeature.NE_ONLY_NEGATION
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 */
	@Override
	protected void checkSubjectNumberPerson(PhraseElement phrase,
			NLGElement verbElement) {
		super.checkSubjectNumberPerson(phrase, verbElement);

		boolean noOnlyNegation = false;
		List<NLGElement> subjects =
			phrase.getFeatureAsElementList(InternalFeature.SUBJECTS);
		boolean feminine = false;
		Person person = Person.THIRD;
		
		if (subjects != null && subjects.size() >= 1) {
			feminine = true;
			for (NLGElement currentElement : subjects) {
				Object gender = currentElement.getFeature(LexicalFeature.GENDER);
				if (gender != Gender.FEMININE) {
					feminine = false;
				}
				// If there's at least one first person subject, the subjects as a whole
				// are first person. Otherwise, if there's at least on second person subject,
				// the subjects as a whole are second person. Otherwise they are third person
				// by default.
				Object currentPerson = currentElement.getFeature(Feature.PERSON);
				if (currentPerson == Person.FIRST) {
					person = Person.FIRST;
				} else if (person == Person.THIRD && currentPerson == Person.SECOND) {
					person = Person.SECOND;
				}

				if (!noOnlyNegation) {
					noOnlyNegation = currentElement.checkIfNeOnlyNegation();
				}
			}
		}
		// If there is at least one feminine subject and nothing else, the gender
		// of the subject group is feminine. Otherwise, it is masculine.
		if (feminine) {
			verbElement.setFeature(LexicalFeature.GENDER, Gender.FEMININE);
		}
		else {
			verbElement.setFeature(LexicalFeature.GENDER, Gender.MASCULINE);
		}
		
		verbElement.setFeature(Feature.PERSON, person);
		setNeOnlyNegation(verbElement, noOnlyNegation);
	}
	
	/**
	 * Check complements and sets FrenchLexicalFeature.NE_ONLY_NEGATION
	 * accordingly for the verb phrase.
	 * 
	 * @param phrase	the verb phrase
	 */
	protected void setNeOnlyNegation(NLGElement verbElement, boolean noOnlyNegation) {
		// check complements if subject doesn't already have the feature
		if (!noOnlyNegation) {
			
			List<NLGElement> complements =
				verbElement.getFeatureAsElementList(InternalFeature.COMPLEMENTS);
			
			for (NLGElement current : complements) {
				if ( current.checkIfNeOnlyNegation() ) {
					noOnlyNegation = true;
					break;
				}
			}
		}
		
		verbElement.setFeature(FrenchLexicalFeature.NE_ONLY_NEGATION, noOnlyNegation);
	}

	/**
	 * Add a modifier to a clause. Use heuristics to decide where it goes.
	 * Based on method of the same name in English clause helper
	 * Reference : section 935 of Grevisse (1993)
	 * 
	 * @param clause
	 * @param modifier
	 * 
	 * @author vaudrypl
	 */
	@Override
	public void addModifier(SPhraseSpec clause, Object modifier) {
		// Everything is postModifier

		if (modifier != null) {
		
			// get modifier as NLGElement if possible
			NLGElement modifierElement = null;
			if (modifier instanceof NLGElement)
				modifierElement = (NLGElement) modifier;
			else if (modifier instanceof String) {
				String modifierString = (String) modifier;
				if (modifierString.length() > 0 && !modifierString.contains(" "))
					modifierElement = clause.getFactory().createWord(modifier,
							LexicalCategory.ADVERB);
			}
		
			// if no modifier element, must be a complex string
			if (modifierElement == null) {
				clause.addPostModifier((String) modifier);
			} else {
				// default case
				clause.addPostModifier(modifierElement);
			}
		}
	}
		
	/**
	 * Checks the discourse function of the clause and alters the form of the
	 * clause as necessary.
	 * 
	 * Based on method of the same name in English syntax processor
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 */
	@Override
	protected void checkDiscourseFunction(PhraseElement phrase) {
		Object clauseForm = phrase.getFeature(Feature.FORM);
		Object discourseValue = phrase
				.getFeature(InternalFeature.DISCOURSE_FUNCTION);

		if (DiscourseFunction.OBJECT.equals(discourseValue)
				|| DiscourseFunction.INDIRECT_OBJECT.equals(discourseValue)) {

			if (Form.IMPERATIVE.equals(clauseForm)) {
				phrase.setFeature(Feature.FORM, Form.INFINITIVE);
			}
		}
	}

	/**
	 * Checks if there are any clausal subjects and if so, put each of them
	 * in a "le fait" + (conjunction) construction.
	 * 
	 * @param phrase
	 * 
	 * @author vaudrypl
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void checkClausalSubjects(PhraseElement phrase) {
		Object subjects = phrase.getFeature(InternalFeature.SUBJECTS);
		List<NLGElement> subjectList = null;
		if (subjects instanceof CoordinatedPhraseElement) {
			subjects = ((CoordinatedPhraseElement)subjects).getFeature(InternalFeature.COORDINATES);
		}
		if (subjects instanceof List) subjectList = (List<NLGElement>) subjects;
		
		if (subjectList != null) {
			for (int index = 0; index < subjectList.size(); ++index) {
				NLGElement currentSubject = subjectList.get(index);
				
				if (currentSubject instanceof SPhraseSpec) {
					Object form = currentSubject.getFeature(Feature.FORM);
					NLGElement verbPhrase = ((SPhraseSpec)currentSubject).getVerbPhrase();
					if (form == null && verbPhrase != null) form = verbPhrase.getFeature(Feature.FORM);
					if (form == Form.NORMAL || form == null) {
						NLGFactory factory = phrase.getFactory();
						NPPhraseSpec newSubject = factory.createNounPhrase("le", "fait");
						newSubject.addPostModifier(currentSubject);
						
						currentSubject.setFeature(InternalFeature.CLAUSE_STATUS, ClauseStatus.SUBORDINATE);
						currentSubject.setFeature(Feature.SUPRESSED_COMPLEMENTISER, false);
						
						currentSubject = newSubject;
					}
				}
				
				subjectList.set(index, currentSubject);
			}
		}
	}

	/**
	 * Copies the front modifiers of the clause to the list of post-modifiers of
	 * the verb only if the phrase has infinitive form.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param verbElement
	 *            the <code>NLGElement</code> representing the verb phrase for
	 *            this clause.
	 */
	@Override
	protected void copyFrontModifiers(PhraseElement phrase,
			NLGElement verbElement) {
		super.copyFrontModifiers(phrase, verbElement);

		// If the complementiser of an infinitive clause is "que", it is suppressed,
		// otherwise it is not suppressed.
		Object clauseForm = phrase.getFeature(Feature.FORM);
		Object clauseStatus = phrase.getFeature(InternalFeature.CLAUSE_STATUS);
		Object complementiser = phrase.getFeature(Feature.COMPLEMENTISER);
		WordElement que = phrase.getLexicon().lookupWord("que", LexicalCategory.COMPLEMENTISER);
		if (clauseForm == Form.INFINITIVE && clauseStatus == ClauseStatus.SUBORDINATE) {
			if (que.equals(complementiser)) phrase.setFeature(Feature.SUPRESSED_COMPLEMENTISER, true);
			else phrase.setFeature(Feature.SUPRESSED_COMPLEMENTISER, false);
		}
	}

	/**
	 * Realises the cue phrase for the clause if it exists. In French,
	 * checks if the phrase is infinitive and doesn't realise the cue phrase
	 * if so.
	 * 
	 * @param phrase
	 *            the <code>PhraseElement</code> representing this clause.
	 * @param realisedElement
	 *            the current realisation of the clause.
	 */
	@Override
	protected void addCuePhrase(PhraseElement phrase,
			ListElement realisedElement) {
		Object form = phrase.getFeature(Feature.FORM);
		if (form != Form.INFINITIVE) super.addCuePhrase(phrase, realisedElement); 
	}
}
