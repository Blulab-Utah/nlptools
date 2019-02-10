package edu.utah.bmi.nlp.ctakes.wrapper;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import java.util.Collection;

/**
 * @author Jianlin Shi on 6/13/18.
 */
public class ClinicPipeTest {
	String input = "EKG appears sinus rhythm with a right bundle branch block. There is some \n" +
			"ST abnormality, nonspecific.";

	@Test
	public void getInstance() {
		ClinicPipe clinicPipe = ClinicPipe.getInstance("conf/ctakes_integrate/ctakes_integrate_config.xml","");

		JCas jCas = clinicPipe.process(input, "DOC_NAME,1.TXT");
		Collection<Annotation> annos = JCasUtil.select(jCas, Annotation.class);
		for (Annotation anno:annos){
			System.out.println(anno.getCoveredText());
		}




	}

	@Test
	public void test2() {
//		TestAE testAE=new TestAE();
//		testAE.loadAEDescriptor(new File("/uufs/chpc.utah.edu/common/home/u0876964/local/software/apache-ctakes-4.0.0/desc/ctakes-clinical-pipeline/desc/analysis_engine/AggregatePlaintextFastUMLSProcessor.xml"));
//		testAE.initCas(input);
		org.apache.uima.tools.cvd.CVD.main(new String[]{});

	}

	public static void main(String[] args) {
//		org.apache.uima.tools.cvd.CVD.main(new String[]{});
		org.apache.uima.tools.docanalyzer.DocumentAnalyzer.main(args);
	}
}