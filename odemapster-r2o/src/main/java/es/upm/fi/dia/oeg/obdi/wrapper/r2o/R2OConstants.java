package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

public class R2OConstants {
	//R2O xml tags
	public static final String R2O_TAG="r2o";
	public static final String CONCEPTMAP_DEF_TAG="conceptmap-def";
	public static final String ATTRIBUTEMAP_DEF_TAG="attributemap-def";
	public static final String DBRELATION_DEF_TAG="dbrelationmap-def";
	public static final String URI_AS_TAG="uri-as";
	public static final String SELECTOR_TAG="selector";
	public static final String ARG_RESTRICTION_TAG="arg-restriction";
	public static final String HAS_VALUE_TAG="has-value";
	public static final String HAS_COLUMN_TAG="has-column";
	public static final String HAS_VIEW_TAG="has-view";
	public static final String HAS_CONCEPT_TAG="has-concept";
	public static final String HAS_DOMAIN_TAG="has-domain";
	public static final String HAS_RANGE_TAG="has-range";
	
	public static final String HAS_TRANSFORMATION_TAG="has-transform";
	public static final String HAS_SQL_TAG="has-sql";
	public static final String HAS_LANGUAGE_TAG="has-language";
	public static final String APPLIES_IF_TAG="applies-if";
	public static final String APPLIES_IF_TOP_TAG="applies-if-top";
	public static final String CONDITION_TAG="condition";
	public static final String AFTERTRANSFORM_TAG="aftertransform";
	public static final String DESCRIBED_BY_TAG="described-by";
	public static final String AND_TAG = "and";
	public static final String OR_TAG = "or";
	public static final String USE_DBCOL_TAG = "use-dbcol";
	public static final String USE_SQL_TAG = "use-sql";
	public static final String OPERATION_TAG = "operation";
	public static final String JOINS_VIA_TAG = "joins-via";
	public static final String ORDER_BY_TAG = "order-by";
	


	
	
	//R2O xml attributes
	public static final String ON_PARAM_ATTRIBUTE="on-param";
	public static final String NAME_ATTRIBUTE="name";
	public static final String ALIAS_ATTRIBUTE="alias";
	public static final String DOCUMENTATION_ATTRIBUTE="documentation";
	public static final String IDENTIFIED_BY_ATTRIBUTE="identified-by";
	public static final String MATERIALIZE_ATTRIBUTE="materialize";
	public static final String OPER_ID_ATTRIBUTE="oper-id";
	public static final String TO_CONCEPT_ATTRIBUTE="to-concept";
	public static final String TO_TABLE_ATTRIBUTE="to-table";
	public static final String DATATYPE_ATTRIBUTE="datatype";
	public static final String JOINS_TYPE_ATTRIBUTE = "joins-type";
	public static final String DATE_FORMAT_ATTRIBUTE="date-format";
	public static final String RETURN_TYPE_ATTRIBUTE="return-type";
	public static final String ENCODE_URI_ATTRIBUTE="encodeURI";
	public static final String BLANK_NODE_ATTRIBUTE = "blankNode";
	public static final String IS_COLLECTION_ATTRIBUTE="is-collection";
	public static final String FUNCTION_NAME_ATTRIBUTE="function-name";

	
	//constants related to database mapping elements
	public static final String DBSCHEMA_DESC_TAG = "dbschema-desc";
	public static final String HAS_TABLE_TAG = "has-table";
	public static final String HAS_INTERMEDIATE_TABLE_TAG = "has-intermediate-table";
	public static final String KEYCOL_DESC_TAG = "keycol-desc";
	public static final String FORKEYCOL_DESC_TAG = "forkeycol-desc";
	public static final String NONKEYCOL_DESC_TAG = "nonkeycol-desc";
	
	
	//constant related to sql
	public static final String SQL_AND = "AND";
	public static final String SQL_OR = "OR";

	public static final String DATATYPE_STRING="string";
	public static final String DATATYPE_DOUBLE="double";
	public static final String DATATYPE_NUMBER="number";
	public static final String DATATYPE_DATE="date";
	public static final String DATATYPE_INTEGER="integer";
	public static final String DATATYPE_BLOB="blob";
	public static final String DATATYPE_COLLECTION="collection";
	
	//conditional operations
	public static final String CONDITIONAL_OPERATOR_EQUALS_NAME = "equals";
	public static final String CONDITIONAL_OPERATOR_NOT_EQUALS_NAME = "not-equals";
	public static final String CONDITIONAL_OPERATOR_LO_THAN_NAME = "lo-than";
	public static final String CONDITIONAL_OPERATOR_LOEQ_THAN_NAME = "loeq-than";
	public static final String CONDITIONAL_OPERATOR_HI_THAN_NAME = "hi-than";
	public static final String CONDITIONAL_OPERATOR_HIEQ_THAN_NAME = "hieq-than";
	public static final String CONDITIONAL_OPERATOR_IN_KEYWORD_NAME = "in-keyword";
	public static final String CONDITIONAL_OPERATOR_BETWEEN_NAME = "between";
	public static final String CONDITIONAL_OPERATOR_MATCH_REGEXP_NAME = "match-regexp";
	public static final String CONDITIONAL_OPERATOR_NOT_MATCH_REGEXP_NAME = "not-match-regexp";
	
	/*
	public static final String[] DELEGABLE_CONDITIONAL_OPERATIONS = {
		CONDITIONAL_OPERATOR_EQUALS_NAME, CONDITIONAL_OPERATOR_NOT_EQUALS_NAME
		, CONDITIONAL_OPERATOR_LO_THAN_NAME, CONDITIONAL_OPERATOR_LOEQ_THAN_NAME
		, CONDITIONAL_OPERATOR_HI_THAN_NAME, CONDITIONAL_OPERATOR_HIEQ_THAN_NAME
		, CONDITIONAL_OPERATOR_IN_KEYWORD_NAME
		, CONDITIONAL_OPERATOR_BETWEEN_NAME
		};
	*/
	
	//transformation operations
	public static final String TRANSFORMATION_OPERATOR_CONSTANT = "constant";
	public static final String TRANSFORMATION_OPERATOR_CONCAT = "concat";
	public static final String TRANSFORMATION_OPERATOR_SUBSTRING = "substring";
	public static final String TRANSFORMATION_OPERATOR_CUSTOM_TRANSFORMATION = "custom-transformation";
	public static final String[] DELEGABLE_TRANSFORMATION_OPERATIONS = {
		TRANSFORMATION_OPERATOR_CONCAT, TRANSFORMATION_OPERATOR_CONSTANT
	};
	
	public static final String ONPARAM_STRING = "string";
	public static final String ONPARAM_REGEXP = "regexp";

	
	
	

	
	
	
	
	//r2o properties
	public static final String R2O_PROPERTY_CONDITIONAL_OPERATIONS_DELEGABLE="conditional-operations.delegable";
	public static final String R2O_PROPERTY_CONDITIONAL_OPERATIONS_NON_DELEGABLE="conditional-operations.nondelegable";
	public static final String R2O_PROPERTY_TRANSFORMATION_OPERATIONS_DELEGABLE="transformation-operations.delegable";
	public static final String R2O_PROPERTY_TRANSFORMATION_OPERATIONS_NON_DELEGABLE="transformation-operations.nondelegable";
	
	public static final String STRING_YES="yes";
	public static final String STRING_TRUE="true";
	public static final String STRING_NO="no";
	public static final String STRING_FALSE="false";
	
	public static final String XSD_STRING = "http://www.w3.org/2001/XMLSchema#string";
	
	



	
	public static final String MAPPING_DOCUMENT_CREATOR = "mappingdocument.creator";
	public static final String MAPPING_DOCUMENT_TAGS = "mappingdocument.tags";
	public static final String MAPPING_DOCUMENT_ORGANIZATION = "mappingdocument.organization";
	
	//aliasses
	public static final String RESTRICTION_ALIAS="r_";
	public static final String APPLIES_IF_ALIAS="ai_";
	public static final String AFTERTRANSFORM_ALIAS="at_";
	public static final String RELATIONMAPPING_ALIAS="rm_";

}
