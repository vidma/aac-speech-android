package com.space.aac.nlg;

import android.util.Log;

import com.space.aac.MainActivity;
import com.space.aac.data.Pictogram;
import com.space.aac.lib.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import simplenlg.features.Feature;
import simplenlg.features.InternalFeature;
import simplenlg.features.LexicalFeature;
import simplenlg.features.NumberAgreement;
import simplenlg.features.Tense;
import simplenlg.framework.CoordinatedPhraseElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.AdvPhraseSpec;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.PPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.phrasespec.VPPhraseSpec;
import simplenlg.realiser.english.Realiser;

/**
 * Created by space on 22/7/16.
 */
public class Pic2NLG {



    public enum ActionType {
        NOUN, CLITIC_PRONOUN, VERB, ADVERB, TENSE_PRESENT, TENSE_PAST, TENSE_FUTURE, TENSE_FUTUR_PROCHE, NUMBER_AGREEMENT, NEGATED, ADJECTIVE, PREPOSITION, QUESTION, DOT, CATEGORY, EMPTY
    };

    public static Lexicon lexicon;
    public static NLGFactory factory;
    public static Realiser realiser;

    private static String LangFeature_FutureProche_MODAL = "aller";

    public static void initEnglish() {
        LangFeature_FutureProche_MODAL = "go";
        lexicon = new simplenlg.lexicon.XMLLexicon();
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        factory = new NLGFactory(lexicon);
        realiser = new Realiser();
    }

    public static void initFrench() {
        Log.d("simpleNLG XmlLexicon", " start");

      /*  // new XmlLexicon
        lexicon = new simplenlg.lexicon.french.XMLLexiconFast();

        // original XmlLexicon
        // lexicon = new simplenlg.lexicon.french.XMLLexicon();
        Log.d("simpleNLG XmlLexicon", " end");

        factory = new NLGFactory(lexicon);
        realiser = new Realiser();
        Log.d("simpleNLG Factory-and-Releasizer", " end");
*/
    }

    public Pic2NLG() {
        initFrench();
    }

    public Pic2NLG(String lang) {
        if (lang.equals("en")) {
            initEnglish();
        } else {
            initFrench();
        }
    }

    private static void log(String msg) {
        if (MainActivity.DEBUG)
            System.out.println(msg);
    }

    public static NumberAgreement getNumberAgreement(String data) {
        return (data.equals("plural")) ? NumberAgreement.PLURAL : NumberAgreement.SINGULAR;
    }

    /**
     * Returns string representation of the verb element in it "base" form
     * (without additional features removed).
     *
     * TODO: may I include reflexivity?
     *
     * @param verb_elm
     * @return
     */
    private static String getVerbMainFormAsString(NLGElement verb_elm) {
        String verb = "";
        if ((verb_elm instanceof VPPhraseSpec)) {
            verb = verb_elm.getFeatureAsString(InternalFeature.HEAD);
            NLGElement verb_elm2 = verb_elm.getFeatureAsElement(InternalFeature.HEAD);
            if (verb_elm2 instanceof WordElement) {
                verb_elm = verb_elm2;
            }
        }
        if (verb_elm instanceof WordElement) {
            verb = ((WordElement) verb_elm).getBaseForm();
        }
        log("Verb head is " + verb);

        return verb;

    }


    /**
     *
     * @param phrases
     * @return
     *
     *         TODO: parse into a tree first apply some smart rules if possible
     *         e.g. preposition detection
     *
     *         The components of a declarative clause are typically arranged in
     *         the following order (though not all components are always
     *         present):
     *
     *         see: http://en.wikipedia.org/wiki/French_grammar (word order --
     *         adjectives are considered as part of object and subject)
     *
     *         also see paper: Morris Salkoff, A context-free grammar of French @
     *         COLING '80 Proceedings of the 8th conference on Computational
     *         linguistics http://dl.acm.org/citation.cfm?id=990182
     *
     *         Adverb(s) Subject ne (usually a marker for negation, though it
     *         has some other uses) First- and second-person object pronoun, or
     *         the third-person reflexive pronoun (any of me, te, nous, vous,
     *         se) Third-person human direct-object pronoun (any of le, la, les)
     *         Third-person human indirect-object pronoun (either lui or leur)
     *         The pronoun y The pronoun en Finite verb (may be an auxiliary)
     *         Adverb(s) The pronoun rien (if not subject) Main verb (if the
     *         finite verb is an auxiliary) Adverb(s) and object(s)
     */
    public String convertPhrasesToNLG(ArrayList<Pictogram> phrases) {

		/*
		 * TODO: support multiple clauses
		 */

		/* reverse the list into a stack */
        Stack<Pictogram> stack0 = new Stack<Pictogram>();
        Stack<Pictogram> stack = new Stack<Pictogram>();
        stack0.addAll(phrases);
        log(stack0.toString());

        String dbg = "";
        while (!stack0.isEmpty()) {
            dbg += stack0.peek().toDebugString() + ", ";

            stack.push(stack0.pop());
        }
        if (MainActivity.DEBUG)
            Log.d("Pic2NLG input:", dbg);

		/* for simplicity now, this will store anything before the last dot */
        String prefixSentance = "";
        SPhraseSpec modalClauseEN = null;
        SPhraseSpec clause = factory.createClause();

		/*
		 * These POS will pop out from stack immediately as they require
		 * additional processing, like matching dependent NounPhrase
		 */
        ActionType pop_immediately_POS[] = { ActionType.PREPOSITION, ActionType.ADVERB };

		/*
		 * We use a simple simple greedy algorithm: for now we assume to have
		 * only one clause -- therefore up to two verbs: (modal) verb. So we may
		 * safely mach these first.
		 *
		 * try to greedily match NounPhrase if not then:
		 *
		 * - adjective, adverb as clause modifiers
		 */
        while (!stack.isEmpty()) {
            // Greedily the noun phrase first (if any) -- this is needed
            // because, for instance the adjevctive as complement to the clause
            // must not be mixed with adjective belonging to an noun, and in
            // french adjective may go both before and after a noun

            // TODO: To fix the problem with me, te, etc we do not allow subject
            // to consist of more than one coordinate for now
            // TODO: long term solution may be to create a separate action_type
            // for CLITIC_PRONOUN (je [te] donne, that should (?) allow to have
            // coordinated
            // subject and pronouns at the same type

            // TODO: for words like tomorrow, today -- these shall not be just
            // Nouns.
            // as we do not need to guess specifier for a time of clause
            // e.g. je mange de bonbons apres-midi

            NLGElement currentNounPrase;

            currentNounPrase = matchCoordinatedNounPhraseList(stack);

            if (currentNounPrase != null) {
                log("NP matched:" + currentNounPrase);

                /**
                 * The first NP coordinate becomes subject, the second object.
                 * If we had a modal in separate clause (for English), that
                 * shall become the Object of it's subclause (because it's more
                 * than first NP coordinate in total)
                 */
                if (clause.getSubject() == null && modalClauseEN == null) {
                    clause.setSubject(currentNounPrase);
                    // TODO: plural
                    log("set subject:" + currentNounPrase);
                } else {

                    clauseAddObject(clause, currentNounPrase);
                    setIndirectObjectSpecifier(clause, currentNounPrase);

                }
            }

            if (stack.isEmpty())
                break;

            Pictogram action = stack.peek();

            switch (action.type) {

                case CLITIC_PRONOUN:
                    clauseAddObject(clause, action.element);
                    break;

                case VERB:
				/*
				 * with simpleNLG, Reflexivity must be handled at the level of
				 * clause we are adding the Reflexive feature to the verb, which
				 * is not normally used, so we have to remove it afterwards (for
				 * forward compatability)
				 */

                    if (action.element.hasFeature(LexicalFeature.REFLEXIVE)) {
                        if (action.element.getFeatureAsBoolean(LexicalFeature.REFLEXIVE)) {
                            clause.setIndirectObject("se");
                        }

                        // TODO:
                        // action.element.removeFeature(LexicalFeature.REFLEXIVE);
                        // just not to create copy of object I don't remove this
                        // feature now
                    }

                    if (clause.getVerb() == null) {
                        clause.setVerb(action.element);
                        log("verb:" + action.element);

                    } else {
                        /**
                         * TODO this may still be more complex regarding the context
                         * (TODO) but for now we assume that subsequent VERB become
                         * a MODAL.
                         *
                         * TODO: are there cases then we may have a verb following
                         * which is not a modal? [if it's not sub-clause, e.g. je
                         * veux que tu manges] e.g. veux, peux, dois:
                         * http://www.laits.utexas.edu/tex/gr/vm1.html
                         *
                         * however, so far we do not support sub-clauses, so this do
                         * not cause a problem
                         */

					/*
					 * exchange the verb with modal: the first becomes to modal,
					 * the second is verb
					 */

                        modalClauseEN = engModal_Guess_infinitive_or_gerund(clause, action);

                        if (modalClauseEN == null) {
                           clause.setFeature(Feature.PERSON, clause.getVerb());
                            clause.setVerb(action.element);
                        }

                    }
                    break;

                /** TODO */
                case PREPOSITION:
                    PPPhraseSpec prepPhrase = factory.createPrepositionPhrase();
                    prepPhrase.setPreposition(action.element);

                    // remove preposition phrase from stack immediately
                    stack.pop();

                    // TODO: buildNounPhrase from anything afterwards
                    NLGElement nounPhrase = matchNounPhrase(stack);

                    if (nounPhrase != null)
                        prepPhrase.addComplement(nounPhrase);

                    clause.addComplement(prepPhrase);

                    // attach as a complement to current clause
                    break;

                case QUESTION:
                    // TODO: this is very very primitive way of forming questions
                    // 1) long click on question may give more options
                    // 2) we may have different complex questions and may have to
                    // set
                    // the object etc

                    String text = releaseSentence(clause, modalClauseEN) + "?";
                    prefixSentance = prefixSentance + text + " ";

                    // reset defaults
                    clause = factory.createClause();
                    modalClauseEN = null;

                    break;

                case DOT:
                    String sentence = releaseSentence(clause, modalClauseEN);

                    if (!sentence.equals("")) {
                        prefixSentance = prefixSentance + sentence + ". ";

                        // switch to a new clause
                        clause = factory.createClause();
                        modalClauseEN = null;
                    }
                    break;

                case NEGATED:
                    clause.setFeature(Feature.NEGATED, true);
                    log("negated:" + action.data);

                    break;

                case TENSE_PRESENT: /* Present is not needed as is by default */
                case TENSE_FUTURE:
                case TENSE_PAST:
                case TENSE_FUTUR_PROCHE:

                    ActionType actType = action.type;

                    Tense tense = (actType == ActionType.TENSE_PAST) ? Tense.PAST
                            : (actType == ActionType.TENSE_FUTURE) ? Tense.FUTURE : Tense.PRESENT;

                    // For "futur proche" we assign just allez a modal
                    if (actType == ActionType.TENSE_FUTUR_PROCHE) {
                        // TODO: A additional hack seems to be needed as the
                        // initial
                        // verb gets "ruined" by transforming it to some other forms
                        // prior to knowing about intension of "futur proche"!
                        // TODO: futur proche do not work if selected before the
                        // word

                        clause.setVerb(getVerbMainFormAsString(clause.getVerb()));
                        clause.setFeature(Feature.MODAL, LangFeature_FutureProche_MODAL);

                        log("tense: futur proche");

                    } else {
                        clause.setFeature(Feature.TENSE, tense);
                        log("tense:" + tense);

                        if (modalClauseEN != null)
                            modalClauseEN.setFeature(Feature.TENSE, tense);

                    }
                    log("clause: " + clause);

                    break;

                case ADVERB:
                    /**
                     * The current solution is to check if subsequent elements are
                     * adverbs too, if so we join them all into one.
                     *
                     * TODO: check if this may lead to an issue anywhere
                     */
                    NLGElement adverb = lexicon.getWord(action.data, LexicalCategory.ADVERB);
                    stack.pop();

				/*
				 * TODO: there may be multiple pre-modifiers, e.g. vraiment tres
				 * vite, but this is sufficiently bizare to care for right now
				 */
                    if (!stack.isEmpty()) {
                        Pictogram next_pos = stack.peek();
                        if (next_pos.type == ActionType.ADVERB) {
                            AdvPhraseSpec advPhrase = factory.createAdverbPhrase();
                            advPhrase.addPreModifier(adverb);
                            advPhrase.setAdverb(next_pos.element);
                            adverb = advPhrase;
                            stack.pop();
                        }

                    }

                    clause.addComplement(adverb);
                    // clause.addModifier(adverb);
                    break;

                case ADJECTIVE:
                    // this is the case of adjective that applies only directly to
                    // the clause (there's no noun), therefore we must first
                    // greedily match a noun phrase above
                    // tu es joli
                    clause.addComplement(action.element);
                    break;

                default:
                    break;
            }

			/* pop a matched action */
            if (!ArrayUtils.contains(pop_immediately_POS, action.type))
                stack.pop();
        }

        String result = (prefixSentance + releaseSentence(clause, modalClauseEN)).trim();
        if(result.startsWith("I WordElement")) {
            result = (prefixSentance);

        }
        Log.d("Pic2LNG", result);
        return result;

    }

    /**
     * @param clause
     * @param action
     *
     * @return
     */
    private SPhraseSpec engModal_Guess_infinitive_or_gerund(SPhraseSpec clause, Pictogram action) {
        String lan ="en";
        //if (lexicon.getLanguage() == Language.ENGLISH) {
        if (true){

            NLGElement modal = clause.getVerb();
            SPhraseSpec modalClause = factory.createClause();

            boolean changed = false;

            String modal_base = getVerbMainFormAsString(modal);

           // log("modal: [str: " + modal_base + " ]" + action.element);

            log("modal: [str: " + modal_base  + action.element);

			/*
			 * It seems the only way to implement full infinitive by creating a
			 * clause of the subject and modal and applying an complement to it
			 * http:// code.google.com/p/simplenlg/source/browse/trunk /testsrc
			 * /simplenlg/test/syntax/ClauseTest.java?spec =svn213&r=213
			 */

            // I want to eat; I have to eat TODO: add more words
            if ("want".equals(modal_base) || "have".equals(modal_base)) {
                clause.setFeature(Feature.FORM, simplenlg.features.Form.INFINITIVE);
                changed = true;
            }

            // I like eating; I have to eat TODO: add more words
            if ("like".equals(modal_base) || "be".equals(modal_base)) {
                clause.setFeature(Feature.FORM, simplenlg.features.Form.GERUND);
                changed = true;

            }

            if (changed) {
                // TODO
                // modalClause = clause;

                modalClause.setVerb(modal);
                modalClause.setSubject(clause.getSubject());

                // TODO: move all Modifiers and Complements to modalClause
                modalClause.setFeature(Feature.NEGATED, clause.getFeature(Feature.NEGATED));
                clause.setFeature(Feature.NEGATED, false);

                // TODO: HOw to remove clause subject? do I need this?
                clause.setFeature(InternalFeature.SUBJECTS, null);

                clause.setVerb(action.element);

                modalClause.addComplement(clause);

                // TODO: but how to remove modifiers and complements easily !!!
                // TODO: clause.removeComplements(function)

                return modalClause;
            } else
                return null;
        }
        return null;
    }


    private boolean isClauseEmpty(SPhraseSpec clause){
        if (clause.getChildren().size() != 0){
            if (!(clause.getVerb() == null && clause.getObject() == null && clause.getSubject() == null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Relizes a sentence using simpleNLG
     *
     * This additional level of abstraction is needed to handle specific cases
     * then semtence is empty, e.g. negation or dot only
     *
     *
     * @param clause
     * @return
     */
    private String releaseSentence(SPhraseSpec clause, SPhraseSpec opt_modal_clause) {
		/* Sometimes */
        String text = "";
        try {
            //wtf?
            if (opt_modal_clause != null) {
                text = realiser.realiseSentence(opt_modal_clause);
            } else if (!isClauseEmpty(clause)) { //simpleNLG can't render empty VP
                text = realiser.realiseSentence(clause);
            }

        } catch (Exception e) {
            Log.e("Pic2NLG", "exception while releasing sentence", e);
        }
        // dot at the end of sentence looks misleading as we have a button for, so remove it
        return text.replace('.', ' ').trim();
    }

    /**
     * TODO: it is not clear how to handle direct object pronoun together with
     * (indirect object?? or shall that be complement and I just have to get
     * proper tagging)
     *
     * e.g. je te coifferai aujourdhui apres-midi
     *
     * [now we get je coifferai toi et aujourdhui apres-midi ]
     *
     *
     *
     * @param clause
     * @param object_to_add
     */
    private void clauseAddObject(SPhraseSpec clause, NLGElement object_to_add) {
		/*
		 * if object is already set, i.e. in je [te] aime beaucaup, the [te] is
		 * a direct object and have will be set as object.
		 *
		 * however afterwards if we had indirect object, we have to make sure it
		 * would not override the earlier
		 *
		 * -- in short, this append one more object coordinate
		 */

        log("setting object to:" + object_to_add);

        NLGElement currentObject;
        if ((currentObject = clause.getObject()) != null) {
            CoordinatedPhraseElement objectCoordinate;
            if (currentObject instanceof CoordinatedPhraseElement) {
                objectCoordinate = (CoordinatedPhraseElement) currentObject;
            } else {
                objectCoordinate = factory.createCoordinatedPhrase();
                objectCoordinate.addCoordinate(currentObject);
            }

			/*
			 * if (object_to_add instanceof CoordinatedPhraseElement) {
			 * CoordinatedPhraseElement to_add = (CoordinatedPhraseElement)
			 * object_to_add;
			 *
			 * for (NLGElement child : to_add.getChildren()) {
			 * objectCoordinate.addCoordinate(child); }
			 *
			 * } else { objectCoordinate.addCoordinate(object_to_add); }
			 */

            objectCoordinate.addCoordinate(object_to_add);

            clause.setObject(objectCoordinate);
        } else {
            clause.setObject(object_to_add);
        }

        if (clause.getObject() != null)
            log("clause.object afterwards:" + clause.getObject());

    }

    /**
     * Try to guess indirect object specifier (de, a, nothing)
     *
     * @param clause
     * @param object
     */
    private void setIndirectObjectSpecifier(SPhraseSpec clause, NLGElement object) {
		/* === Try to guess indirect object specifier (de, a, nothing) == */
        String intelligent_guess = intelligentGuessSpecifier(clause);

        if (object instanceof CoordinatedPhraseElement) {
			/* set determiner to each of coordinated phrases */
            for (NLGElement NP : object.getChildren()) {

                if (((NPPhraseSpec) NP).getFeature(InternalFeature.SPECIFIER) == null) {
                    ((NPPhraseSpec) NP).setDeterminer(intelligent_guess);
                }
            }
        }
        if (object instanceof NPPhraseSpec && ((NPPhraseSpec) object).getFeature(InternalFeature.SPECIFIER) == null) {
            ((NPPhraseSpec) object).setDeterminer(intelligent_guess);

        }
    }

    private String intelligentGuessSpecifier(SPhraseSpec clause) {
        log("starting intellingent guess for specifier!");

        String verb = getVerbMainFormAsString(clause.getVerb());

        // TODO: refcator this thing as getVerbAsString.getFeatureAsString(InternalFeature.HEAD);

        String intelligent_guess = "";

		/*
		 * Some come from here, TODO: finish http://french.about.com/library/prepositions/bl_prep_a_verb.htm
		 */
        String[] verb_a_IndObj = { "habiter", "aider", "apprendre", "arriver", "commencer", "aller" };

        /**
         * de + noun Indirect Object http://french.about.com/library
         * /prepositions/bl_prep_de_verb2.htm
         *
         * TODO: handle reflexivity: "s'occuper","se méfier", "s'étonner",
         * "se souvenir", se tromper de,
         *
         * TODO: handle modals/double words: avoir besoin
         */

        String[] de_verb_IndObj = { "acheter", "avoir besoin", "avoir envie", "dépendre", "douter", "féliciter",
                "jouer", "jouir", "manquer", "partir", "penser", "profiter", "punir", "recompenser", "remercier",
                "rire", "vivre",

				/*
				 * these were not specified in grammar rules, but seem to be
				 * good guesses
				 */
                "boire", "vouloir", "manger" };

		/* TODO: ones with infinitive */
        String[] de_verb_infinitive = {};

        List<String> a_verbs_list = Arrays.asList(verb_a_IndObj);
        List<String> de_verb_IndObj_list = Arrays.asList(de_verb_IndObj);

        if (a_verbs_list.contains(verb))
            intelligent_guess = "à";
        if (de_verb_IndObj_list.contains(verb))
            intelligent_guess = "de";

        log("verb: " + verb + "  guessed:" + intelligent_guess);
        return intelligent_guess;
    }

    /**
     * Builds a list of coordinated noun phrases in a greedy way
     *
     * @param "part of speech" tagged words (POS)
     * @return list of coordinated Noun phrases or null if there's no noun(s)
     *
     */
    private NLGElement matchCoordinatedNounPhraseList(Stack<Pictogram> stack) {
        CoordinatedPhraseElement NP_list = factory.createCoordinatedPhrase();
        NPPhraseSpec current_NP;
        Boolean found = false;
        while ((current_NP = matchNounPhrase(stack)) != null) {
            NP_list.addCoordinate(current_NP);
            found = true;
        }
        // NP_list.getChildren().size() == 0
        if (!found)
            return null;
		/*
		 * if only one -- it must be NP but not coordinated NP e.g. Moi et mes
		 * amies ... ==> Moi
		 *
		 * TODO: fix Je!
		 */
        List<NLGElement> children = NP_list.getChildren();
        if (children.size() == 1) {
            return children.get(0);
        }

        return NP_list;
    }

    /**
     * Builds a noun phrase in a greedy way only one noun allowed in noun phrase
     * -- other will get coordinated. TODO: check the cases of names of
     * consisting of multiple items
     *
     * @param "part of speech" tagged words (POS)
     * @return Noun phrase or null if there's no noun
     *
     */
    private NPPhraseSpec matchNounPhrase(Stack<Pictogram> stack) {
        NPPhraseSpec currentNounPrase = factory.createNounPhrase();

		/* What part of speech are not allowed inside NounPhrase */

        ActionType allowed_POS[] = { ActionType.NOUN, ActionType.ADJECTIVE, ActionType.NUMBER_AGREEMENT,
                ActionType.CLITIC_PRONOUN };

        /**
         * Assertion 1: Noun phrase must have a Noun, otherwise the current list
         * may be e.g. Adjective phrase: while adjective is also accepted in
         * Noun phrase, it cannot go alone on simpleNLG
         */
        Boolean noun_exist = false;

        for (int i = stack.size() - 1; i >= 0; i--) {
            Pictogram item = stack.get(i);

            // log("NP N containement check: " + item);

            if (!ArrayUtils.contains(allowed_POS, item.type)) {
                break;
            }

            if (item.type == ActionType.NOUN || item.type == ActionType.CLITIC_PRONOUN)
                noun_exist = true;
        }
        if (!noun_exist)
            return null;

        Pictogram action;

        int nNounsFound = 0;
        while (!stack.isEmpty()) {
            action = stack.peek();

            switch (action.type) {
                case CLITIC_PRONOUN:
                case NOUN:
                    // we match only the first noun, the subsequent noun will be part of second coordinated NP
                    nNounsFound++;
                    if (nNounsFound == 1) {
                        currentNounPrase.setNoun(action.element);
                    }
                    break;

                case NUMBER_AGREEMENT:
                    currentNounPrase.setFeature(Feature.NUMBER, getNumberAgreement(action.data));
                    break;

                case ADJECTIVE:
				/*
				 * TODO: I may want to create an adjective phrase with
				 * premodifiers, like 'very happy child' (TODO: check what's
				 * better)
				 */
                    currentNounPrase.addModifier(action.element);

                    break;

                default:
                    // not allowed action, the NP is over
                    return currentNounPrase;
            }

            // If we found a second noun, break the processing
            if (nNounsFound > 1) {
                break;
            }

            // the current POS was accepted/matched, so Pop if from stack
            stack.pop();
        }

        return currentNounPrase;
    }

    public Boolean hasSubjectBeenSelected(ArrayList<Pictogram> phrases) {
        Boolean have_subject = false;
        for (Pictogram phrase : phrases) {
            if (phrase.type == ActionType.NOUN)
                have_subject = true;

            if (phrase.type == ActionType.DOT || phrase.type == ActionType.QUESTION)
                have_subject = false;
        }
        return have_subject;
    }

}
