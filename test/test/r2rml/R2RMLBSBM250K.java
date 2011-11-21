package test.r2rml;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator;

import test.r2o.ODEMapsterTest;

public class R2RMLBSBM250K {
	private static Logger logger = Logger.getLogger(R2RMLBSBM250K.class);
	
	private String mappingDirectory = ODEMapsterTest.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml/r2rml-bsbm250k/";
	private String mappingDocumentFile = configurationDirectory + "bsbm.ttl";
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void run(String testName) {
		try {
			logger.info("------ Running " + testName + " ------");
			String configurationFile = testName + ".r2rml.properties";
			
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
//			R2RMLElementDataTranslateVisitor dataTranslator = 
//					new R2RMLElementDataTranslateVisitor(configurationDirectory, configurationFile); 
//			md.accept(dataTranslator);
			
			String queryFilePath = configurationDirectory + testName + ".sparql"; 
			R2RMLQueryTranslator queryTranslator = new R2RMLQueryTranslator(md);
			queryTranslator.translate(queryFilePath);
			
			
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testBSBM() throws Exception {
		String testName = "bsbm";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		try {
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			md.accept(new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile));
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void testBSBM01() throws Exception {
		String testName = "bsbm01";
		this.run(testName);
	}
	
	@Test
	public void testBSBM02() throws Exception {
		String testName = "bsbm02";
		this.run(testName);
	}	

	@Test
	public void testBSBM03() throws Exception {
		String testName = "bsbm03";
		this.run(testName);
	}	

	@Test
	public void testBSBM04() throws Exception {
		String testName = "bsbm04";
		this.run(testName);
	}	
	
	@Test
	public void testBSBM05() throws Exception {
		String testName = "bsbm05";
		this.run(testName);
	}

	@Test
	public void testBSBM06() throws Exception {
		String testName = "bsbm06";
		this.run(testName);
	}
	
	@Test
	public void testBSBM07() throws Exception {
		String testName = "bsbm07";
		this.run(testName);
	}
	
	@Test
	public void testBSBM08() throws Exception {
		String testName = "bsbm08";
		this.run(testName);
	}
	
	@Test
	public void testBSBM10() throws Exception {
		String testName = "bsbm10";
		this.run(testName);
	}	
}
