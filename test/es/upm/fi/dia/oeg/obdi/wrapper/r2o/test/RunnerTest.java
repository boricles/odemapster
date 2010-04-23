package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;

import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.ModelWriter;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;
import es.upm.fi.dia.oeg.obdi.wrapper.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OModelGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParser;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OUnfolder;


public class RunnerTest extends XMLTestCase {
	private static Logger logger = Logger.getLogger(RunnerTest.class);
	
	private static void testTestcase08() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase34/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase08/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	public static void testTestcase34() {
		String dir = "D:/Users/fpriyatna/My Dropbox/Public/odemapster/testcases/testcase34/";
		//String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase34/";
		String r2oConfigurationFile = dir + "r2o.properties";
		RunnerTest.testProcess(r2oConfigurationFile, dir);
	}

	private void testTestcase35() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase35/";
		String r2oConfigurationFile = dir + "r2o.properties";
		this.testProcess(r2oConfigurationFile, dir);
	}

	/*
	private void testTestcase37() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase37/";
		String r2oConfigurationFile = dir + "r2o.properties";
		this.testProcess(r2oConfigurationFile);
	}
	*/

	private  void testTestcase38() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase38/";
		String r2oFile = dir + "r2o.properties";
		this.testProcess(r2oFile, dir);
	}

	private void testTestcase39() {
		//String dir = "D:/Users/fpriyatna/My Dropbox/oeg/odemapster/odemapster_shared/testcases/testcase35/";
		String dir = "/home/fpriyatna/Dropbox/Public/odemapster/testcases/testcase39/";
		String r2oFile = dir + "r2o.properties";
		this.testProcess(r2oFile, dir);
	}

	public static void main(String args[]) {
		
		
		try {
			RunnerTest.testTestcase34();
			
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}


	}

	@Test
	private static void testProcess(String r2oConfigurationFile, String mappingDirectory) {
		PropertyConfigurator.configure("log4j.properties");
		long start = System.currentTimeMillis();
		
		try {
			AbstractRunner runner = new R2ORunner();
			runner.run(r2oConfigurationFile);
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		long end = System.currentTimeMillis();
		long duration = (end-start) / 1000;
		logger.info("Execution time was "+(duration)+" s.");
		assertTrue(true);
	}
}
