package edu.utah.bmi.nlp.demo;

import edu.utah.bmi.nlp.context.common.ConTextSpan;
import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.SimpleParser;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.fastcontext.FastContext;
import edu.utah.bmi.nlp.fastcontext.uima.FastContext_General_AE;
import edu.utah.bmi.nlp.type.system.Concept;
import edu.utah.bmi.nlp.type.system.Context;
import edu.utah.bmi.nlp.uima.AdaptableUIMACPERunner;
import edu.utah.bmi.nlp.uima.ae.SimpleParser_AE;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

/**
 * @author Jianlin Shi on 6/5/18.
 */
public class FastConTextDemo {
	private AnalysisEngine fastContext_AE;
	private JCas jCas;
	private AdaptableUIMACPERunner runner;
	private AnalysisEngine simpleParser_AE;

	@Before
	public void setUp() {
		String typeDescriptor = "desc/type/All_Types";
		runner = new AdaptableUIMACPERunner(typeDescriptor, "target/generated-test-sources/");
		runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions("conf/ctakes_mimic/ctakes_mimic_context.xlsx", true).values());
		runner.reInitTypeSystem("target/generated-test-sources/customized");
		jCas = runner.initJCas();
		Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR, "conf/ctakes_mimic/ctakes_mimic_context.xlsx",
				FastContext_General_AE.PARAM_MARK_CLUE, true};
		try {
			fastContext_AE = createEngine(FastContext_General_AE.class,
					configurationData);
			simpleParser_AE = createEngine(SimpleParser_AE.class, new Object[]{SimpleParser_AE.PARAM_INCLUDE_PUNCTUATION, true});
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
//      Set up the parameters

	}

	@Test
	public void test0() {
		ArrayList<String> rules = new ArrayList<>();
		rules.add("@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer");
		rules.add("@FEATURE_VALUES|Negation|affirm|negated");
		rules.add("@FEATURE_VALUES|Certainty|certain|uncertain");
		rules.add("@FEATURE_VALUES|Temporality|present|historical|hypothetical");
		rules.add("@FEATURE_VALUES|Experiencer|patient|nonpatient");
		rules.add("denied|forward|trigger|negated|30");
		rules.add("although|forward|termination|negated|10");
		FastContext fc = new FastContext(rules, true);
//        fc.debug=true;
		String text = "The patient denied any fever , although he complained some headache .";
		ArrayList<Span> sent = SimpleParser.tokenizeOnWhitespaces(text);
		LinkedHashMap<String, ConTextSpan> matches = fc.getFullContextFeatures("Concept", sent, 4, 4, text);
		for (Map.Entry<String, ConTextSpan> entry : matches.entrySet()) {
			System.out.println(entry.getKey()+"\t"+entry.getValue());
		}


	}

	@Test
	public void test1() throws AnalysisEngineProcessException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		String text = "The patient Denies any problem with visual changes or hearing changes.";
		String targetWords = "visual changes";
		jCas.setDocumentText(text);
		simpleParser_AE.process(jCas);
		int begin = text.indexOf(targetWords);
		int end = begin + targetWords.length();


		Class<? extends Concept> cls = Class.forName(DeterminantValueSet.checkNameSpace("DiseaseDisorderMention")).asSubclass(Concept.class);
		Constructor<? extends Concept> cons = cls.getConstructor(JCas.class, int.class, int.class);
		Concept concept = cons.newInstance(jCas, begin, end);
		concept.addToIndexes();

		fastContext_AE.process(jCas);
		Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
		for (Concept target : targets) {
			System.out.println(target.toString());
		}
		for (Context context : JCasUtil.select(jCas, Context.class)) {
			System.out.println(context.toString());
		}
	}


	@Test
	public void test2() throws ResourceInitializationException, AnalysisEngineProcessException {
		ArrayList<String> rules = new ArrayList<>();
		rules.add("@CONCEPT_FEATURES|Concept|Negation|Certainty|Temporality|Experiencer");
		rules.add("@FEATURE_VALUES|Negation|affirm|negated");
		rules.add("@FEATURE_VALUES|Certainty|certain|uncertain");
		rules.add("@FEATURE_VALUES|Temporality|present|historical|hypothetical");
		rules.add("@FEATURE_VALUES|Experiencer|patient|nonpatient");
		rules.add("denied|forward|trigger|negated|30");
		rules.add("although|forward|termination|negated|10");
//        fc.debug=true;
		String text = "The patient denied any fever , although he complained some headache .";
		String rule = String.join("\n", rules);
		String targetWords = "fever";
		runner.addConceptTypes(FastContext_General_AE.getTypeDefinitions(rule, true).values());
		runner.reInitTypeSystem("target/generated-test-sources/customized");
		jCas = runner.initJCas();
		jCas.setDocumentText(text);
		Object[] configurationData = new Object[]{FastContext_General_AE.PARAM_RULE_STR,
				rule, FastContext_General_AE.PARAM_MARK_CLUE, true};
		fastContext_AE = createEngine(FastContext_General_AE.class,
				configurationData);

		simpleParser_AE.process(jCas);
		int begin = text.indexOf(targetWords);
		int end = begin + targetWords.length();
		Concept concept = new Concept(jCas, begin, end);
		concept.addToIndexes();
		fastContext_AE.process(jCas);
		Collection<Concept> targets = JCasUtil.select(jCas, Concept.class);
		for (Concept target : targets) {
			System.out.println(target.toString());
		}

		targetWords = "headache";
		begin = text.indexOf(targetWords);
		end = begin + targetWords.length();
		concept = new Concept(jCas, begin, end);
		concept.addToIndexes();
		fastContext_AE.process(jCas);
		targets = JCasUtil.select(jCas, Concept.class);
		for (Concept target : targets) {
			System.out.println(target.toString());
		}
	}


}
