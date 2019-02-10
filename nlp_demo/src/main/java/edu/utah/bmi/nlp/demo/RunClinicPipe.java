package edu.utah.bmi.nlp.demo;

import edu.utah.bmi.nlp.ctakes.wrapper.ClinicPipe;
import org.apache.ctakes.core.cc.pretty.plaintext.PrettyTextWriter;
import org.apache.ctakes.typesystem.type.textspan.Sentence;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jianlin Shi on 6/22/18.
 */
public class RunClinicPipe {
	public static void main(String[] args) {
		String input = "EKG appears sinus rhythm with a right bundle branch block. There is some \n" +
				"ST abnormality, nonspecific.";
		ClinicPipe clinicPipe = ClinicPipe.getInstance("conf/ctakes_integrate/ctakes_integrate_config.xml", "xmi");
		JCas jCas = clinicPipe.process(input, "DOC_NAME,1.TXT");
		System.out.println(prettyPrint(jCas));
		CAS cas = jCas.getCas();
		Type type = CasUtil.getType(cas, "edu.utah.bmi.nlp.type.system.DiseaseDisorder");
		Collection<AnnotationFS> annos = CasUtil.select(jCas.getCas(), type);
		for (AnnotationFS anno : annos) {
			if (anno.getType().getShortName().equals("DiseaseDisorder")) {
				for (Feature feature : anno.getType().getFeatures()) {
					String domain = feature.getDomain().getShortName();
					if (domain.equals("AnnotationBase") || domain.equals("Annotation"))
						continue;
					String featureName = feature.getShortName();
					Type range = feature.getRange();
					if (!range.isPrimitive()) {
						Object value = anno.getFeatureValue(feature);
						if(value instanceof FSArray) {
							value = serilizeFSArray((FSArray) value);
						}
						System.out.println(featureName+":"+value);
					} else {
						System.out.println(featureName+":"+ anno.getFeatureValueAsString(feature) );
					}


				}
			}
		}
	}

	private static String serilizeFSArray(FSArray ary) {
		StringBuilder sb = new StringBuilder();
		int size = ary.size();
		String[] values = new String[size];
		ary.copyToArray(0, values, 0, size);
		for (FeatureStructure fs : ary) {
			List<Feature> features = fs.getType().getFeatures();
			for (Feature feature : features) {
				String domain = feature.getDomain().getShortName();
				if (domain.equals("AnnotationBase") || domain.equals("Annotation"))
					continue;
				Type range = feature.getRange();
				if (!range.isPrimitive()) {
					FeatureStructure child = fs.getFeatureValue(feature);
					sb.append(child + "");
				} else {
					sb.append("\t"+feature.getShortName() + ":" + fs.getFeatureValueAsString(feature)+"\n");
				}
			}

		}
		return sb.toString();
	}

	public static String prettyPrint(JCas jcas) {
		StringBuffer sb = new StringBuffer();
		for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
			List<Object> myRowData = new ArrayList<>();

			//mec...yoyo... added
			StringWriter sw = new StringWriter(); //mec...yoyo... added
			BufferedWriter writer = new BufferedWriter(sw); //mec...yoyo... added

			// Create the "pretty" sentence
			try {
				PrettyTextWriter.writeSentence(jcas, sentence, writer); // mec... This is the MAGIC
				writer.flush();
				sb.append(sw.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
