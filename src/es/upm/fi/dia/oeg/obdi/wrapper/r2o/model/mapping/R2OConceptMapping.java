package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;

public class R2OConceptMapping extends AbstractConceptMapping implements R2OElement, Cloneable {


	//	(18) conceptmapping-definition::= conceptmap-def name
	//    identified-by+
	//    (uri-as selector)?
	//    (applies-if cond-expr)?
	//    (joins-via concept-join-expr)?
	//    documentation?
	//    (described-by propertymap-def)*

	//	(18) conceptmapping-definition::= conceptmap-def name
	//    (has-table literal)*		
	//    identified-by+
	//    (uri-as selector)?
	//    (applies-if cond-expr)?
	//    (joins-via concept-join-expr)?
	//    documentation?
	//    (described-by propertymap-def)*
	private Vector<R2ODatabaseTable> hasTables;
	private Collection<String> orderBy;
	private R2OSelector selectorURIAs;//original version
	private R2OTransformationExpression uriAs;//odemapster 1 version
	private String encodeURI;
	private String materialize;
	private List<R2OPropertyMapping> describedBy; //describedBy
	private R2OConditionalExpression appliesIf;//original version
	private R2OConditionalExpression appliesIfTop;//odemapster 1 version
	private static Logger logger = Logger.getLogger(R2OConceptMapping.class);

	private R2OConceptMapping(String cmName, String id, Vector<R2ODatabaseTable> hasTables
			, R2OConditionalExpression appliesIf, R2OTransformationExpression uriAs) {
		this.name = cmName;
		this.id = id;
		this.hasTables = hasTables;
		this.appliesIf = appliesIf;
		this.uriAs = uriAs;
	}
	
	private R2OConceptMapping() {
		
	}
	
	public R2OConceptMapping(Element conceptMappingElement) throws ParseException{
		this.parse(conceptMappingElement);
	}
	

	private R2OSelector getSelectorURIAs() {
		return selectorURIAs;
	}

	public R2OTransformationExpression getURIAs() {
		return uriAs;
	}

	public void parse(Element conceptMappingElement) throws ParseException{
		//R2OConceptMapping result = new R2OConceptMapping();

		//parse name attribute
		this.name = conceptMappingElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		logger.info("Parsing concept " + this.name);

		//parse documentation
		this.documentation = conceptMappingElement.getAttribute(R2OConstants.DOCUMENTATION_ATTRIBUTE);

		//parse identifiedBy attribute
		this.id = conceptMappingElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);

		//parse identifiedBy attribute
		this.materialize = conceptMappingElement.getAttribute(R2OConstants.MATERIALIZE_ATTRIBUTE);
		if(this.materialize == "") {
			this.materialize = null;
		}
		if(this.materialize != null) {
			if(!this.materialize.equalsIgnoreCase(R2OConstants.STRING_FALSE) &&
					!this.materialize.equalsIgnoreCase(R2OConstants.STRING_TRUE) &&
					!this.materialize.equalsIgnoreCase(R2OConstants.STRING_YES) &&
					!this.materialize.equalsIgnoreCase(R2OConstants.STRING_NO)) 
			{
				String errorMessage = "Invalid value for materialize option of concept mapping!";
				logger.error(errorMessage);
				throw new ParseException(errorMessage);				
			}
				
		}
		
		//parse has-table
		List<Element> hasTableElements = XMLUtility.getChildElementsByTagName(conceptMappingElement, R2OConstants.HAS_TABLE_TAG);
		if(hasTableElements != null && hasTableElements.size() > 0) {
			if(hasTableElements.size() > 1) {
				throw new ParseException("Multiple hasTables is not implemented yet!");
			}
			this.hasTables = new Vector<R2ODatabaseTable>();
			for(Element hasTableElement : hasTableElements) {
				R2ODatabaseTable dbTable = new R2ODatabaseTable(hasTableElement);
				/*
				if(dbTable.getAlias() == null) {
					int cmHashCode = this.hashCode();
					int dbTableNameHashCode = dbTable.hashCode();
					String alias = R2OConstants.TABLE_ALIAS_PREFIX + cmHashCode + "_" + dbTableNameHashCode;
					dbTable.setAlias(alias);
					dbTable.setAutoGeneratedAlias(true);
				}
				*/
				this.hasTables.add(dbTable);
			}
		} else {
			String errorMessage = "Error parsing : " + this.name + " , hasTables element should be defined on a concept mapping!";
			logger.error(errorMessage);
			throw new ParseException(errorMessage);
		}
		
		//parse order-by
		List<Element> orderByElements = XMLUtility.getChildElementsByTagName(conceptMappingElement, R2OConstants.ORDER_BY_TAG);
		if(orderByElements != null && orderByElements.size() != 0) {
			this.orderBy = new ArrayList<String>();
			for(Element orderByElement : orderByElements) {
				this.orderBy.add(orderByElement.getTextContent());
			}
		}
		
		//parse uri-as element
		Element uriAsElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.URI_AS_TAG);
		this.encodeURI = uriAsElement.getAttribute(R2OConstants.ENCODE_URI_ATTRIBUTE);
		
		Element selectorURIAsElement = XMLUtility.getFirstChildElementByTagName(uriAsElement, R2OConstants.SELECTOR_TAG);
		//Element transformationExpressionURIAs = XMLUtility.getFirstElement(uriAsElement);
		if(selectorURIAsElement != null) {
			this.selectorURIAs = new R2OSelector(selectorURIAsElement);
		} else {
			this.uriAs = new R2OTransformationExpression(uriAsElement);
		}


		//parse applies-if/applies-if-top element
		Element appliesIfElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.APPLIES_IF_TAG);
		Element appliesIfTopElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.APPLIES_IF_TOP_TAG);
		if(appliesIfElement != null) {
			Element conditionElement = XMLUtility.getFirstElement(appliesIfElement);
			if(conditionElement != null) {
				this.appliesIf = new R2OConditionalExpression(conditionElement);
			}
		} else {
			if(appliesIfTopElement != null) {
				logger.warn("applies-if-top is deprecated, use applies-if instead!");
				Element conditionElement = XMLUtility.getFirstElement(appliesIfTopElement);
				this.appliesIfTop = new R2OConditionalExpression(conditionElement);
			}
		}

		//parse documentation attribute
		this.documentation = conceptMappingElement.getAttribute(R2OConstants.DOCUMENTATION_ATTRIBUTE);

		//parse described-by element
		Element describedByElement = 
			XMLUtility.getFirstChildElementByTagName(
					conceptMappingElement, R2OConstants.DESCRIBED_BY_TAG);
		if(describedByElement != null) {
			//logger.warn("Deprecated use mode. Property mappings should be defined under r2o element, not under concept mapping element!");
			this.describedBy = this.parseDescribedByElement(describedByElement);
		}


	}

	private List<R2OPropertyMapping> parseDescribedByElement(Element describedByElement) throws ParseException {
		List<R2OPropertyMapping> result = new ArrayList<R2OPropertyMapping>();

		Collection<Element> propertyMappingElements = XMLUtility.getChildElements(describedByElement);
		if(propertyMappingElements != null) {
			for(Element propertyMappingElement : propertyMappingElements) {
				if(propertyMappingElement.getNodeName().equalsIgnoreCase(R2OConstants.ATTRIBUTEMAP_DEF_TAG)) {
					R2OAttributeMapping am = new R2OAttributeMapping(this, propertyMappingElement);
					result.add(am);
				} else if(propertyMappingElement.getNodeName().equalsIgnoreCase(R2OConstants.DBRELATION_DEF_TAG)) {
					R2ORelationMapping rm = new R2ORelationMapping(propertyMappingElement, this);
					result.add(rm);
				} else {
					throw new ParseException("undefined mapping type.");
				}

			}
			
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + R2OConstants.CONCEPTMAP_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}

		if(this.materialize != null && this.materialize != "") {
			result.append(R2OConstants.MATERIALIZE_ATTRIBUTE +"=\"" + this.materialize + "\" ");
		}

		result.append(">\n");

		if(this.hasTables != null && this.hasTables.size() > 0) {
			for(R2ODatabaseTable hasTable : this.hasTables) {
				result.append(hasTable.toString() + "\n");
			}
		}
		result.append("\n");

		if(this.orderBy != null && this.orderBy.size() != 0) {
			for(String orderByString : this.orderBy) {
				result.append(XMLUtility.toOpenTag(R2OConstants.ORDER_BY_TAG));
				result.append(orderByString);
				result.append(XMLUtility.toCloseTag(R2OConstants.ORDER_BY_TAG));				
			}
		}
		
		result.append("<" + R2OConstants.URI_AS_TAG + " ");
		if(this.encodeURI != null) {
			if(this.encodeURI.equalsIgnoreCase(R2OConstants.STRING_TRUE)) {
				result.append(R2OConstants.ENCODE_URI_ATTRIBUTE +"=\"" + R2OConstants.STRING_TRUE + "\" ");
			} else if(this.encodeURI.equalsIgnoreCase(R2OConstants.STRING_FALSE)) {
				result.append(R2OConstants.ENCODE_URI_ATTRIBUTE +"=\"" + R2OConstants.STRING_FALSE + "\" ");
			}			
		}
		result.append(">\n");
		if(this.selectorURIAs != null) {
			result.append(this.selectorURIAs.toString() + "\n");
		} else {
			result.append(this.uriAs.toString() + "\n");
		}
		result.append("</" + R2OConstants.URI_AS_TAG + ">\n");
		result.append("\n");
		

		if(this.appliesIf != null) {
			result.append("<" + R2OConstants.APPLIES_IF_TAG + ">\n");
			result.append(this.appliesIf.toString());
			result.append("</" + R2OConstants.APPLIES_IF_TAG + ">\n");
			result.append("\n");
		} else {
			if(this.appliesIfTop != null) {
				result.append("<" + R2OConstants.APPLIES_IF_TOP_TAG + ">\n");
				result.append(this.appliesIfTop.toString());
				result.append("</" + R2OConstants.APPLIES_IF_TOP_TAG + ">\n");
				result.append("\n");
			}
		}
		


		if(describedBy != null) {
			result.append("<" + R2OConstants.DESCRIBED_BY_TAG + ">");
			for(R2OPropertyMapping propertyMapping : describedBy) {
				result.append("\n" + propertyMapping.toString());
			}
			result.append("</" + R2OConstants.DESCRIBED_BY_TAG + ">\n");
		}


		result.append("</" + R2OConstants.CONCEPTMAP_DEF_TAG + ">");
		return result.toString();
	}

	public R2OConditionalExpression getAppliesIf() {
		if(appliesIf != null) {
			return this.appliesIf;
		} else {
			return this.appliesIfTop;
		}
	}

	public List<R2OPropertyMapping> getDescribedBy() {
		return describedBy;
	}


	public List<R2OPropertyMapping> getPropertyMappings() {
		return describedBy;
	}

	public List<R2OPropertyMapping> getPropertyMappings(String propertyURI) {
		List<R2OPropertyMapping> result = new ArrayList<R2OPropertyMapping>();
		if(this.describedBy != null) {
			for(R2OPropertyMapping propertyMapping : describedBy) {
				String propertyName = propertyMapping.getName(); 
				if(propertyName.equals(propertyURI)) {
					result.add(propertyMapping);
				}
			}			
		}
		

		return result;
	}



	public List<R2ORelationMapping> getRelationMappings(String propertyURI) {
		List<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();
		
		for(R2OPropertyMapping propertyMapping : describedBy) {
			String propertyName = propertyMapping.getName(); 
			if(propertyName.equals(propertyURI) && propertyMapping instanceof R2ORelationMapping) {
				result.add((R2ORelationMapping) propertyMapping);
			}
		}
		return result;
	}
	
	public void addPropertyMapping(R2OPropertyMapping pm) {
		if(this.describedBy == null) {
			this.describedBy = new ArrayList<R2OPropertyMapping>();
		}
		
		this.describedBy.add(pm);
	}
	
	public void addAttributeMapping(R2OAttributeMapping am) {
		if(this.describedBy == null) {
			this.describedBy = new ArrayList<R2OPropertyMapping>();
		}
		
		this.describedBy.add(am);
	}

	public void addRelationMapping(R2ORelationMapping rm) {
		if(this.describedBy == null) {
			this.describedBy = new ArrayList<R2OPropertyMapping>();
		}

		this.describedBy.add(rm);
	}

	public List<R2OAttributeMapping> getAttributeMappings() {
		List<R2OAttributeMapping> result = new ArrayList<R2OAttributeMapping>();
		
		List<R2OPropertyMapping> propertyMappings = this.getDescribedBy();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMapping : propertyMappings) {
				if(propertyMapping instanceof R2OAttributeMapping) {
					result.add((R2OAttributeMapping) propertyMapping);
				}
				
			}			
		}

		return result;
	}

	public List<R2ORelationMapping> getRelationMappings() {
		List<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();
		
		List<R2OPropertyMapping> propertyMappings = this.getDescribedBy();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMapping : propertyMappings) {
				if(propertyMapping instanceof R2ORelationMapping) {
					result.add((R2ORelationMapping) propertyMapping);
				}
			}			
		}

		return result;
	}

	@Override
	public String getConceptName() {
		return this.name;
	}

	public Vector<R2ODatabaseTable> getHasTables() {
		return hasTables;
	}

	public R2ODatabaseTable getHasTable() {
		return hasTables.get(0);
	}
	
	public Collection<String> getOrderBy() {
		return orderBy;
	}

	public String getEncodeURI() {
		return encodeURI;
	}
	
	
	public List<R2OAttributeMapping> getAttributeMappings(String propertyURI) {
		List<R2OAttributeMapping> result = new ArrayList<R2OAttributeMapping>();
		
		for(R2OPropertyMapping propertyMapping : describedBy) {
			String propertyName = propertyMapping.getName(); 
			if(propertyName.equals(propertyURI) && propertyMapping instanceof R2OAttributeMapping) {
				result.add((R2OAttributeMapping) propertyMapping);
			}
		}
		return result;
	}

	@Override
	public R2OConceptMapping clone() {
		try {
			return (R2OConceptMapping) super.clone();
		} catch(Exception e) {
			logger.error("Error occured while cloning R2OConceptMapping object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void setAppliesIf(R2OConditionalExpression appliesIf) {
		this.appliesIf = appliesIf;
	}

	public R2OConceptMapping getStripped() {
		R2OConceptMapping cmStripped = new R2OConceptMapping(this.getConceptName(), this.getId(),
				this.getHasTables(), this.getAppliesIf(), this.getURIAs());

		return cmStripped;
	}

	
	public static R2OConceptMapping merge(Collection<R2OConceptMapping> cms) 
	throws MergeException {
		R2OConceptMapping result = null;;
		
		if(cms != null && cms.size() > 0) {
			if(cms.size() == 0) {
				result = cms.iterator().next();
			} else {
				for(R2OConceptMapping cm : cms) {
					result = R2OConceptMapping.merge(result, cm);
				}				
			}
		}
		
		return result;
	}
	
	private static R2OConceptMapping merge(R2OConceptMapping cm1, R2OConceptMapping cm2) 
	throws MergeException {
		R2OConceptMapping result = new R2OConceptMapping();
		
		
		if(cm1 == null && cm2 == null) {
			String errorMessage = "Both concept mappings are null!";
			logger.error(errorMessage);
			throw new MergeException(errorMessage);			
		} else if(cm1 == null && cm2 != null) {
			result = cm2.clone();
		} else if(cm1 != null && cm2 == null) {
			result = cm1.clone();
		} else {
			result.id = cm1.id;
			if(cm1.id != cm2.id) {
				String errorMessage = "Unconsistent identified-by values!";
				throw new MergeException(errorMessage);
			}
			
			result.name = cm1.name;
			if(cm1.name != cm2.name) {
				String errorMessage = "Unconsistent name values!";
				logger.error(errorMessage);
				throw new MergeException(errorMessage);			
			}
			
			result.hasTables = cm1.hasTables;
			if(cm1.hasTables != null && !cm1.hasTables.equals(cm2.hasTables)) {
				String errorMessage = "Unconsistent has-table values!";
				throw new MergeException(errorMessage);			
			}

			if(cm1.appliesIf != cm2.appliesIf) {
				result.appliesIf = R2OConditionalExpression.addConditionalExpression(
						cm1.appliesIf, R2OConstants.AND_TAG, cm2.appliesIf);				
			}

			
			result.uriAs = cm1.uriAs;
			if(cm1.uriAs != cm2.uriAs) {
				String errorMessage = "Unconsistent uri-as values!";
				throw new MergeException(errorMessage);			
			}
			
			result.describedBy = cm1.describedBy;
			if(result.describedBy == null) {
				result.describedBy = new ArrayList<R2OPropertyMapping>();
			}
			
			if(cm2.describedBy != null) {
				for(R2OPropertyMapping pm : cm2.describedBy) {
					String pmName = pm.getName();
					List<R2OPropertyMapping> pms = result.getPropertyMappings(pmName);
					if(pms == null || pms.size() == 0) {
						result.describedBy.add(pm);
					}
				}				
			}
			
		}

		

		
		return result;
		
	}

	public String getMaterialize() {
		return materialize;
	}

	public void setMaterialize(String materialize) {
		this.materialize = materialize;
	}
	
	public String generateURIAlias() {
		return R2OConstants.URI_AS_ALIAS + this.getId();
	}
}
