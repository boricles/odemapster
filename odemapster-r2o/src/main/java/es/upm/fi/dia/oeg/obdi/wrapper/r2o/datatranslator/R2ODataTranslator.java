package es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.vocabulary.RDFS;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.engine.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidRestrictionType;
import es.upm.fi.dia.oeg.obdi.core.exception.PostProcessorException;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSQLRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class R2ODataTranslator extends AbstractDataTranslator {
	private static Logger logger = Logger.getLogger(R2ODataTranslator.class);
	

	//XML Schema states that xml:Lang is not meaningful on xsd datatypes.
	//Thus for almost all typed literals there is no xml:Lang tag.
	private void addDataTypeProperty(
			String predicateName, Object propVal, String datatype, String lang) 
	throws Exception {
		if(predicateName.equalsIgnoreCase(RDFS.label.toString())) { //special case of rdfs:label
			if(lang == null) {
				datatype = R2OConstants.XSD_STRING;
			}
		}


		
		this.materializer.materializeDataPropertyTriple(predicateName, propVal, datatype, lang, null);
	}
	
	private void createRDFTypeTriple(String subjectURI, String conceptName) 
	throws IOException {
		boolean isBlankNodeSubject;
		
		if(Utility.isIRI(subjectURI)) {
			isBlankNodeSubject = false;
		} else {
			isBlankNodeSubject = true;
		}
		this.materializer.materializeRDFTypeTriple(subjectURI, conceptName, isBlankNodeSubject, null);
		
	}

	private void processAttributeMapping(
			R2OAttributeMapping attributeMapping, ResultSet rs) throws Exception {
		String propName = attributeMapping.getName();
		String amDataType = attributeMapping.getDatatype();
		String lang = null;
		if(attributeMapping.getLangHasValue() != null) {
			lang = attributeMapping.getLangHasValue();
		} else if(attributeMapping.getLangDBCol() != null) {
			String columnName = attributeMapping.getLangDBCol();
			String alias = columnName.replaceAll("\\.", "_");
			lang = (String) this.processSQLExpression(columnName, alias, attributeMapping.getLangDBColDataType(), rs);			
		}

		String dbColUsed = attributeMapping.getUseDBCol();
		if(dbColUsed != null) {
			String columnName = attributeMapping.getUseDBCol();
			String alias = columnName.replaceAll("\\.", "_");
			Object dbColValue = this.processSQLExpression(columnName, alias, attributeMapping.getUseDBColDatatype(), rs);
			this.addDataTypeProperty(propName, dbColValue, amDataType, lang);
		} else if(attributeMapping.getUseSQL() != null) {
			String expression = attributeMapping.getUseSQL();
			String alias = attributeMapping.getUseSQLAlias();
			Object dbColValue = this.processSQLExpression(expression, alias, attributeMapping.getUseSQLDataType(), rs);
			this.addDataTypeProperty(propName, dbColValue, amDataType, lang);			
		} else if(attributeMapping.getSelectors() != null){
			Collection<R2OSelector> attributeMappingSelectors = attributeMapping.getSelectors();

			for(R2OSelector attributeMappingSelector : attributeMappingSelectors) {
				R2OConditionalExpression attributeMappingSelectorAppliesIf = attributeMappingSelector.getAppliesIf();
				boolean attributeMappingSelectorAppliesIfValue = false;
				if(attributeMappingSelectorAppliesIf == null) {
					//if there is no applies-if specified in the selector, then the condition is true
					attributeMappingSelectorAppliesIfValue = true;
				} else {
					//else, evaluate the applies-if condition

					//if(this.isDelegableConditionalExpression(attributeMappingSelectorAppliesIf)) {
					if(attributeMappingSelectorAppliesIf.isDelegableConditionalExpression()) {
						String selectorAppliesIfAlias = attributeMappingSelector.generateAppliesIfAlias();
						int appliesIfValue = rs.getInt(selectorAppliesIfAlias);
						if(appliesIfValue == 0) {
							attributeMappingSelectorAppliesIfValue = false;
						} else if(appliesIfValue == 1) {
							attributeMappingSelectorAppliesIfValue = true;
						}
					} else {
						attributeMappingSelectorAppliesIfValue = this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs); 
						//this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs, ATTRIBUTE_MAPPING_SELECTOR);							

					}

				}

				if(attributeMappingSelectorAppliesIfValue) {
					//String alias = attributeMapping.getId() + attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
					R2OTransformationExpression attMapSelAT = attributeMappingSelector.getAfterTransform();
					String selectorDataType = attMapSelAT.getDatatype();


					Object propVal = null;
					//if(isDelegableTransformationExpression(attMapSelAT)) {
					if(attMapSelAT.isDelegableTransformationExpression()) {
						String alias = attributeMappingSelector.generateAfterTransformAlias();
						propVal = this.processSQLExpression(alias, alias, selectorDataType, rs);
						//propVal = this.processDelegableTransformationExpression(rs, alias);							
					} else {
						propVal = this.processNonDelegableTransformationExpression(rs, attMapSelAT);
					}


					String isCollection = attMapSelAT.getIsCollection();
					if(isCollection != null && isCollection != "") {
						if(isCollection.equalsIgnoreCase(R2OConstants.DATATYPE_COLLECTION)) {
							Collection<Object> propCol = (Collection<Object>) propVal;
							for(Object propColItem : propCol) {
								this.addDataTypeProperty(
										propName, propColItem, amDataType, lang);
							}
						} else {
							throw new Exception("Unsupported return type : " + selectorDataType);
						}
					} else {
						if(propVal!= null && propVal != "" && !propVal.equals("")) {
							this.addDataTypeProperty(
									propName, propVal, amDataType, lang);							
						}
					}
				}
			}

		} else {
			throw new Exception("Unsupported attribue mapping.");
		}

	}

	/*
	private Object processDelegableTransformationExpression(ResultSet rs, String alias) throws SQLException {
		Object result = rs.getObject(alias);;
		return result;
	}
	 */

	private boolean processBetweenConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());
		R2ORestriction restriction2 = argumentRestrictions.get(2).getRestriction();
		Double operand2 = Double.parseDouble(this.processRestriction(restriction2, rs).toString());

		if(operand0 != null && operand1 != null && operand2 != null) {
			return operand0.doubleValue() >= operand1.doubleValue() && operand0.doubleValue() <= operand2.doubleValue();
		} else {
			return false;
		}
	}


	private String processConcatTransformationExpression(
			ResultSet rs, R2OTransformationExpression transformationExpression) throws PostProcessorException, InvalidRestrictionType, SQLException {
		String result = "";
		List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
		for(R2OArgumentRestriction ar : argumentRestrictions) {
			result += this.processRestriction(ar.getRestriction(), rs);
		}

		return result;
	}

	private void processConceptMapping(ResultSet rs, AbstractConceptMapping conceptMapping) throws Exception {
		String conceptName = conceptMapping.getConceptName();
		logger.info("Post processing for " + conceptName);
		long startGeneratingModel = System.currentTimeMillis();

		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		R2OConditionalExpression appliesIf = r2oConceptMapping.getAppliesIf();


		boolean processRecord = false;
		boolean appliesIfIsDelegableConditionalExpr;
		boolean appliesIfIsConjuctiveConditionalExpr;
		if(appliesIf == null) { //always process if applies-if is null
			processRecord = true;
			appliesIfIsConjuctiveConditionalExpr = false;			
		} else { 
			appliesIfIsDelegableConditionalExpr = appliesIf.isDelegableConditionalExpression();
			appliesIfIsConjuctiveConditionalExpr = appliesIf.isConjuctiveConditionalExpression();

			if(appliesIfIsDelegableConditionalExpr) { //always process if applies-if is delegable conditional expr
				processRecord = true;
			}
		}

		R2OTransformationExpression conceptMappingURIAsTransformationExpression = 
			r2oConceptMapping.getURIAs();

//		boolean uriAsIsDelegableTransformationExpr = 
//			isDelegableTransformationExpression(conceptMappingURIAsTransformationExpression);

		boolean uriAsIsDelegableTransformationExpr = 
			conceptMappingURIAsTransformationExpression.isDelegableTransformationExpression(); 
		
		boolean isEncodeURI = true;
		String encodeURI = r2oConceptMapping.getEncodeURI();

		if(encodeURI == null) {
			encodeURI = R2OConstants.STRING_TRUE;
		}

		if(encodeURI.equalsIgnoreCase(R2OConstants.STRING_FALSE)) {
			isEncodeURI = false;
		}



		long counter = 0;
		String previousSubjectURI = null;
		while(rs.next()) {
			try {

				if(!processRecord) {
					if(appliesIfIsConjuctiveConditionalExpr) { //only process the non-delegable ones
						boolean allConditionTrue = true;
						for(R2OCondition condition : appliesIf.flatConjuctiveConditionalExpression()) {
							if(processRecord && !condition.isDelegableCondition()) {
								if(!this.processCondition(condition, rs)) {
									allConditionTrue = false;
								}
							}
						}
						processRecord = allConditionTrue;
					} else {
						//processRecord = this.processConditionalExpression(appliesIf, rs, CONCEPT_MAPPING_APPLIES_IF);
						processRecord = this.processConditionalExpression(appliesIf, rs);							
					}					
				}



				if(processRecord) {
					String subjectURI = null;
					if(uriAsIsDelegableTransformationExpr) {
						String alias = r2oConceptMapping.generateURIAlias();
						subjectURI = rs.getString(alias);
						//uri = this.processDelegableTransformationExpression(rs, alias).toString();
					} else {
						subjectURI = (String) this.processNonDelegableTransformationExpression(
								rs, conceptMappingURIAsTransformationExpression);
					}
					
					if(subjectURI == null) {
						throw new Exception("null uri is not allowed!");
					}

					if(isEncodeURI) {
						subjectURI = Utility.encodeURI(subjectURI);
					}

					if(counter % 50000 == 0) {
						logger.info("Processing record no " + counter + " : " + subjectURI);
					}
					counter++;

					//create rdf type triple if the subject is different from the previous one
					if(!subjectURI.equals(previousSubjectURI)) {
						this.createRDFTypeTriple(subjectURI, conceptName);
					}

					//logger.info("Processing property mappings.");
					this.processPropertyMappings(r2oConceptMapping, rs);
					previousSubjectURI = subjectURI;
				}

			} catch(Exception e) {
//				e.printStackTrace();
				logger.error("Error processing record no " + counter + " because " + e.getMessage());

				throw e;
			}




		}
		logger.info(counter + " instances retrieved.");



		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Post Processing time was "+(durationGeneratingModel)+" s.");


	}
	
	private boolean processCondition(R2OCondition condition, ResultSet rs) throws Exception {
		boolean result = false;

		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		//		//only skip records while processing concept mapping
		//		if(source.equalsIgnoreCase(CONCEPT_MAPPING_APPLIES_IF) 
		//				&&  Utility.inArray(this.properties.getDelegableConditionalOperations(), operationId)) {
		//			return true;
		//		}

		if(R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME.equalsIgnoreCase(operationId)) {
			result = this.processEqualsConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_LO_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processLoThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_LOEQ_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processLoEqThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_HI_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processHiThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_HIEQ_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processHiEqThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_NOT_EQUALS_NAME.equalsIgnoreCase(operationId)) {
			boolean conditionEquals = this.processEqualsConditional(condition, rs);
			result = !conditionEquals;
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_MATCH_REGEXP_NAME.equalsIgnoreCase(operationId)) {
			result = this.processMatchRegExpConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_NOT_MATCH_REGEXP_NAME.equalsIgnoreCase(operationId)) {
			result = this.processMatchRegExpConditional(condition, rs);
			return !result;
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_IN_KEYWORD_NAME.equalsIgnoreCase(operationId)) {
			result = this.processInKeywordConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_BETWEEN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processBetweenConditional(condition, rs);
		}

		return result;
	}

	/*
	private void addDataTypeProperty(String propName, Object propVal, String datatype, Model model, Resource subject) {
		if(propName.equalsIgnoreCase(RDFS.label.toString())) { //special case of rdfs:label
			String propValString = propVal.toString();
			int lastAtIndex = propValString.lastIndexOf("@");
			Literal literal = null;
			if(lastAtIndex > 0) {
				String propertyValue = propValString.substring(0, lastAtIndex);
				String language = propValString.substring(lastAtIndex+1, propValString.length());
				literal = model.createLiteral(propertyValue, language);
			} else {
				literal = model.createTypedLiteral(propValString);
			}
			subject.addProperty(RDFS.label, literal);
		} else {
			Literal literal;
			if(datatype == null) {
				literal =  model.createTypedLiteral(propVal);
			} else {
				literal =  model.createTypedLiteral(propVal, datatype);
			}

			subject.addProperty(model.createProperty(propName), literal);
		}
	}
	 */


	private boolean processConditionalExpression(R2OConditionalExpression conditionalExpression, ResultSet rs) 
	throws Exception {
		boolean result = false;
		String conditionalExpressionOperator = conditionalExpression.getOperator();


		if(conditionalExpressionOperator == null) {
			result = this.processCondition(conditionalExpression.getCondition(), rs);
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs);
				if(condition == false) {
					return false;
				}
			}

			return true;
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.OR_TAG)) {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs);
				if(condition == true) {
					return true;
				}
			}

			return false;
		}

		return result;
	}


	protected Object processCustomFunctionTransformationExpression(
			List<Object> arguments) throws PostProcessorException {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	protected Object processCustomFunctionTransformationExpression(
			Object argument) throws PostProcessorException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	private boolean isDelegableCondition(R2OCondition condition) {
		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		if(Utility.inArray(R2ORunner.primitiveOperationsProperties.getDelegableConditionalOperations(), operationId)) {
			return true;
		} else {
			return false;
		}
	}
	*/

	/*
	private boolean isDelegableConditionalExpression(R2OConditionalExpression conditionalExpression) {
		if(conditionalExpression.getOperator() == null) {
			R2OCondition condition = conditionalExpression.getCondition();
			return condition.isDelegableCondition();
		} else {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				if(this.isDelegableConditionalExpression(condExpr) == false) {
					return false;
				}
			}
			return true;
		}
	}
	*/


	//	private boolean processConceptMappingAppliesIfElement(R2OConceptMapping r2oConceptMapping, ResultSet rs) throws Exception {
	//		boolean result = false;
	//		R2OConditionalExpression appliesIf = r2oConceptMapping.getAppliesIf();
	//
	//		if(appliesIf == null) {
	//			result = true;
	//		} else {
	//			return this.processConditionalExpression(appliesIf, rs, CONCEPT_MAPPING_APPLIES_IF);
	//		}
	//
	//		return result;
	//	}



	/*
	private boolean processOrConditionalExpression(OrConditionalExpression orConditionalExpression, ResultSet rs) throws Exception {
		boolean result = false;

		if(orConditionalExpression.isUsingOr()) { //the case of : OR orcond-expr condition
			Condition condition = orConditionalExpression.getCondition();
			boolean result1 = this.processCondition(condition, rs);
			if(result1) {
				return true;
			}

			OrConditionalExpression orCondExpression = orConditionalExpression.getOrCondExpr();
			boolean result2 = this.processOrConditionalExpression(orCondExpression, rs);
			if(result2) {
				return true;
			}

			//false at this point
			return false;
		} else { //the case of : condition
			Condition condition = orConditionalExpression.getCondition();
			result = this.processCondition(condition, rs);
		}

		return result;
	}
	 */

	private boolean processEqualsConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		String operand0 = (String) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		String operand1 = (String) this.processRestriction(restriction1, rs);

		if(operand0 != null && operand1 != null) {
			return operand0.equals(operand1);
		} else {
			return false;
		}
	}

	private boolean processHiEqThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() >= operand1.doubleValue());
		} else {
			return false;
		}
	}


	private boolean processHiThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() > operand1.doubleValue());
		} else {
			return false;
		}
	}

	private boolean processInKeywordConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		String operand0 = (String) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		String operand1 = (String) this.processRestriction(restriction1, rs);

		if(operand0 != null && operand1 != null) {
			return operand0.indexOf(operand1) != -1;
		} else {
			return false;
		}
	}

	private boolean processLoEqThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() <= operand1.doubleValue());
		} else {
			return false;
		}
	}

	private boolean processLoThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() < operand1.doubleValue());
		} else {
			return false;
		}
	}

	@Override
	public void translateData(AbstractMappingDocument abstractMappingDocument) throws Exception {
		int timeout = R2ORunner.configurationProperties.getDatabaseTimeout();
		
		R2OMappingDocument mappingDocument = (R2OMappingDocument) abstractMappingDocument;
		Collection<AbstractConceptMapping> conceptMappings = 
			mappingDocument.getConceptMappings();
		for(AbstractConceptMapping abstractConceptMapping : conceptMappings) {
			R2OConceptMapping conceptMapping = (R2OConceptMapping) abstractConceptMapping;
			boolean proceedToUnfolding = true;
			if(conceptMapping.getMaterialize() == null) {
				proceedToUnfolding = true;
			} else {
				if(conceptMapping.getMaterialize().equalsIgnoreCase(R2OConstants.STRING_FALSE) ||
						conceptMapping.getMaterialize().equalsIgnoreCase(R2OConstants.STRING_NO)) {
					proceedToUnfolding = false;
				} else if(conceptMapping.getMaterialize().equalsIgnoreCase(R2OConstants.STRING_TRUE) ||
						conceptMapping.getMaterialize().equalsIgnoreCase(R2OConstants.STRING_FALSE)) { 
					proceedToUnfolding = true;
				}
			}
			
			if(proceedToUnfolding) {
				ResultSet rs = null;
				try {
					//unfold mapped concepts
					logger.info("Unfolding concept " + conceptMapping.getConceptName());
					R2OUnfolder unfolder = 
						new R2OUnfolder(mappingDocument, R2ORunner.primitiveOperationsProperties, R2ORunner.configurationProperties);
					String sqlQuery = unfolder.unfoldConceptMapping(conceptMapping);
					logger.info(conceptMapping.getName() + " unfolded = \n" + sqlQuery + "\n");
					if(sqlQuery != null) {
						//evaluate query
						rs = QueryEvaluator.evaluateQuery(
								sqlQuery, R2ORunner.configurationProperties.getConn());
						this.processConceptMapping(rs, conceptMapping);
					}
				} catch(SQLException e) {
					String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
					logger.error(errorMessage);				
					throw e;
				} catch(RelationMappingUnfolderException e) {
					String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
					logger.error(errorMessage);				
					throw e;				
				} catch(Exception e) {
					//e.printStackTrace();
					String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
					logger.error(errorMessage);
					//throw e;
				} finally {
					//cleaning up
					DBUtility.closeStatement(rs.getStatement());
					DBUtility.closeRecordSet(rs);					
				}
			}
			


		}
	}

	private boolean processMatchRegExpConditional(R2OCondition condition, ResultSet rs) throws Exception {
		R2ORestriction inputStringRestriction = condition.getArgRestricts(R2OConstants.ONPARAM_STRING);
		String inputString = (String) this.processRestriction(inputStringRestriction, rs);
		/*
		if(inputString.contains("Cruz de")) {
			boolean test = true;
		}
		 */

		R2ORestriction regexRestriction = condition.getArgRestricts(R2OConstants.ONPARAM_REGEXP);
		String regex = (String) this.processRestriction(regexRestriction, rs);

		if(inputString != null && regex != null) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(inputString);
			if(matcher.find()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private Object processNonDelegableTransformationExpression(
			ResultSet rs, R2OTransformationExpression transformationExpression) throws PostProcessorException, InvalidRestrictionType, SQLException {
		if(transformationExpression.getOperId().equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_SUBSTRING)) {
			return this.processSubstringTransformationExpression(rs, transformationExpression);
		} else if(transformationExpression.getOperId().equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONCAT)) {
			String result = this.processConcatTransformationExpression(rs, transformationExpression);
			return result;
		} else if(transformationExpression.getOperId().equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CUSTOM_TRANSFORMATION)) {
			List<Object> arguments = new ArrayList<Object>();
			String functionName = transformationExpression.getFunctionName();
			arguments.add(functionName);
			List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
			for(R2OArgumentRestriction argument : argumentRestrictions) {
				Object restrictionValue = this.processRestriction(argument.getRestriction(), rs);
				arguments.add(restrictionValue);
			}
			return this.processCustomFunctionTransformationExpression(arguments);
		} else {
			String errorMessage = "Not supported transformation operation : " + transformationExpression.getOperId();
			logger.error(errorMessage);
			throw new PostProcessorException(errorMessage);
		}
	}

	private void processPropertyMappings(R2OConceptMapping r2oConceptMapping, ResultSet rs) throws Exception {
		Collection<AbstractPropertyMapping> propertyMappings = r2oConceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(AbstractPropertyMapping propertyMapping : propertyMappings) {
				if(propertyMapping instanceof R2OAttributeMapping) {
					try {
						this.processAttributeMapping(
								(R2OAttributeMapping) propertyMapping, rs);
					} catch(Exception e) {
						String newErrorMessage = e.getMessage() + " while processing attribute mapping " + propertyMapping.getName();
						logger.error(newErrorMessage);
						throw e;
					}

				} else if(propertyMapping instanceof R2ORelationMapping) {
					try {
						this.processRelationMapping(
								(R2ORelationMapping) propertyMapping, rs);
					} catch(Exception e) {
						String newErrorMessage = e.getMessage() + " while processing relation mapping " + propertyMapping.getName();
						logger.error(newErrorMessage);
						throw e;
					}

				}
			}			
		}





	}

	private void processRelationMapping(R2ORelationMapping rm, ResultSet rs) throws Exception {
		String predicateName = rm.getRelationName();

		if(rm.getToConcept() != null) {
			String rangeURIAlias = rm.generateRangeURIAlias();
			String rangeURI = rs.getString(rangeURIAlias);
			boolean isBlankNodeObject;
			if(rangeURI != null) {
				if(Utility.isIRI(rangeURI)) {
					rangeURI = Utility.encodeURI(rangeURI);
					isBlankNodeObject = false;
				} else {
					isBlankNodeObject = true;
				}
				
				this.materializer.materializeObjectPropertyTriple(predicateName, rangeURI, isBlankNodeObject, null);
			}			
		} 

		if(rm.getRmSelectors() != null) {
			Collection<R2OSelector> selectors = rm.getRmSelectors();
			if(selectors != null) {
				for(R2OSelector selector : selectors) {
					R2OConditionalExpression selectorAppliesIf = selector.getAppliesIf();
					boolean selectorAppliesIfValue = false;
					if(selectorAppliesIf == null) {
						//if there is no applies-if specified in the selector, then the condition is true
						selectorAppliesIfValue = true;
					} else {
						//else, evaluate the applies-if condition
						
						//if(this.isDelegableConditionalExpression(selectorAppliesIf)) {
						if(selectorAppliesIf.isDelegableConditionalExpression()) {
							String selectorAppliesIfAlias = selector.generateAppliesIfAlias(); 
							int appliesIfValue = rs.getInt(selectorAppliesIfAlias);
							if(appliesIfValue == 0) {
								selectorAppliesIfValue = false;
							} else if(appliesIfValue == 1) {
								selectorAppliesIfValue = true;
							}
						} else {
							selectorAppliesIfValue = this.processConditionalExpression(selectorAppliesIf, rs); 
							//this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs, ATTRIBUTE_MAPPING_SELECTOR);							

						}

					}

					if(selectorAppliesIfValue) {
						//String alias = attributeMapping.getId() + attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
						R2OTransformationExpression selectorAT = selector.getAfterTransform();


						Object propVal = null;
						//if(isDelegableTransformationExpression(selectorAT)) {
						if(selectorAT.isDelegableTransformationExpression()) {
							String returnType = selectorAT.getDatatype();
							String alias = selector.generateAfterTransformAlias();
							propVal = this.processSQLExpression(alias, alias, returnType, rs);
							//propVal = this.processDelegableTransformationExpression(rs, alias);							
						} else {
							propVal = this.processNonDelegableTransformationExpression(rs, selectorAT);
						}


						if(propVal != null) {
							String propValString = propVal.toString();
							boolean isBlankNodeObject;
							if(Utility.isIRI(propValString)) {
								propValString = Utility.encodeURI(propValString);
								isBlankNodeObject = false;
							} else {
								isBlankNodeObject = true;
							}
							
							this.materializer.materializeObjectPropertyTriple(predicateName, propValString, isBlankNodeObject, null);
							


						}		
					}
				}

			}			
		}

	}

	private Object processRestriction(R2ORestriction restriction, ResultSet rs) throws PostProcessorException, InvalidRestrictionType, SQLException {
		Object result = null;

		if(restriction instanceof R2OColumnRestriction) {
			R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
			R2ODatabaseColumn dbColumn = restrictionColumn.getDatabaseColumn();
			String fullColumnName = dbColumn.getFullColumnName();
			String columnNameOnly = dbColumn.getColumnNameOnly();
			String dataType = dbColumn.getDataType();

			String alias=null;
//			String alias = restrictionColumn.getDatabaseColumn().getAlias();
//			if(alias == null) {
//				alias = "'" + fullColumnName + "'";
//			}
			
			result = this.processSQLExpression(fullColumnName, alias, dataType, rs);
		} else if(restriction instanceof R2OConstantRestriction) {
			R2OConstantRestriction restrictionConstant = (R2OConstantRestriction) restriction;
			result = restrictionConstant.getConstantValue();
		} else if(restriction instanceof R2OTransformationRestriction) {
			R2OTransformationRestriction restrictionTransformation = 
				(R2OTransformationRestriction) restriction;
			R2OTransformationExpression childTransformationExpression = 
				restrictionTransformation.getTransformationExpression();
			Object subresult = this.processNonDelegableTransformationExpression(rs, childTransformationExpression);
			return subresult;
		} else if(restriction instanceof R2OSQLRestriction) {
			R2OSQLRestriction restrictionSQL = (R2OSQLRestriction) restriction;
			String expression = restrictionSQL.getHasSQL();
			String alias = restrictionSQL.getAlias();
			if(alias == null || alias == "") {
				alias = restriction.generateRestrictionAlias();
			}

			result = this.processSQLExpression(expression, alias, null, rs);
		} else {
			String errorMessage = "Unsupported restriction type!";
			logger.error(errorMessage);
			throw new InvalidRestrictionType(errorMessage);
		}

		return result;			

	}


	private Object processSQLExpression(String expression, String alias, String dataType, ResultSet rs) throws SQLException {
		try {
			Object result = null;

			if(alias == null) {
				alias = expression;
			} else {
				alias = alias.replaceAll("'", "");
			}
			
			String aliasTokens[] = alias.split("\\.");
			if(aliasTokens != null && aliasTokens.length==3) {
				//rename alias if it's fully quantifier
				//alias = aliasTokens[0] + aliasTokens[1] + "." + aliasTokens[2];
				alias = aliasTokens[2];
			}
			
			if(dataType == null || dataType == "") {
				result = rs.getObject(alias);
			} else {
				if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
					result = rs.getString(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
					result = rs.getDouble(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DATE)) {
					result = rs.getDate(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_INTEGER)) {
					result = rs.getInt(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_BLOB)) {
					result = rs.getBlob(alias);
				} else {
					result = rs.getObject(alias);
				}
			}

			return result;			
		} catch(SQLException e) {
			//e.printStackTrace();
			logger.error("Error while retrieving SQL records!");
			throw e;
		}

	}

	private String processSubstringTransformationExpression(
			ResultSet rs, R2OTransformationExpression transformationExpression) throws PostProcessorException, InvalidRestrictionType, SQLException {
		List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
		String argument0 = (String) this.processRestriction(argumentRestrictions.get(0).getRestriction(), rs); 
		Integer beginIndex = Integer.parseInt(this.processRestriction(argumentRestrictions.get(1).getRestriction(), rs).toString());
		Integer endIndex = Integer.parseInt(this.processRestriction(argumentRestrictions.get(2).getRestriction(), rs).toString());

		return argument0.substring(beginIndex, endIndex);
	}

	public void setMaterializer(AbstractMaterializer materializer) {
		this.materializer = materializer;
	}















	/*
	private boolean isDelegableTransformationExpression(R2OTransformationExpression transformationExpression) {
		String operator = transformationExpression.getOperId();

		//if the root operator is not delegable, then return false 
		if(!Utility.inArray(R2ORunner.primitiveOperationsProperties.getDelegableTransformationOperations(), operator)) {
			return false;
		}

		//if one of the arguments has non delegable transformation, then return false
		for(R2OArgumentRestriction argRestriction : transformationExpression.getArgRestrictions()) {
			R2ORestriction restriction = argRestriction.getRestriction();
			if(restriction instanceof R2OTransformationRestriction) {
				R2OTransformationRestriction restrictionTransformation = 
					(R2OTransformationRestriction) restriction;
				if(!isDelegableTransformationExpression(
						restrictionTransformation.getTransformationExpression())) {
					return false;
				}
			}
		}

		return true;
	}
	*/


}
