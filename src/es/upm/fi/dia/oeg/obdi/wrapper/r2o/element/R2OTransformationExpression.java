package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Zql.ZConstant;
import Zql.ZSelectItem;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction.RestrictionType;

public class R2OTransformationExpression extends R2OExpression {
	//	(30) transformation::= primitive-transf (arg-restriction arg-restriction)*
	private String primitiveTransf;
	private String operId;
	private String datatype;
	private String isCollection;
	private List<R2OArgumentRestriction> argRestrictions;
	private boolean autoGeneratedDatatype = false;
	
	@Override
	public R2OTransformationExpression parse(Element element) throws ParseException {
		R2OTransformationExpression result = new R2OTransformationExpression();
		Element operationElement = XMLUtility.getFirstElement(element);
		String operationElementNodeName = operationElement.getNodeName();
		result.primitiveTransf = operationElementNodeName;

		if(R2OConstants.OPERATION_TAG.equalsIgnoreCase(operationElementNodeName)) {
			result.operId = operationElement.getAttribute(R2OConstants.OPER_ID_ATTRIBUTE);
		}

		result.datatype = operationElement.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		if(result.datatype == null || result.datatype == "") {
			if(result.operId.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONCAT)) {
				result.datatype = R2OConstants.DATATYPE_STRING;
				autoGeneratedDatatype = true;
			} else if(result.operId.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_SUBSTRING)) {
				result.datatype = R2OConstants.DATATYPE_STRING;
				autoGeneratedDatatype = true;
			}
		}
		
		result.isCollection = operationElement.getAttribute(R2OConstants.IS_COLLECTION_ATTRIBUTE);
		if(result.isCollection == "") {
			result.isCollection = null;
		}
		
		result.argRestrictions = new ArrayList<R2OArgumentRestriction>();
		List<Element> argumentRestrictionsNodeList = XMLUtility.getChildElementsByTagName(operationElement, R2OConstants.ARG_RESTRICTION_TAG);
		for(int i=0; i<argumentRestrictionsNodeList.size(); i++) {
			Element argumentRestrictionElement = argumentRestrictionsNodeList.get(i);
			R2OArgumentRestriction argumentRestrictionObject = new R2OArgumentRestriction().parse(argumentRestrictionElement);
			result.argRestrictions.add(argumentRestrictionObject);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		if(R2OConstants.OPERATION_TAG.equalsIgnoreCase(this.primitiveTransf)) {
			result.append("<" + this.primitiveTransf + " ");
			result.append(R2OConstants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\"");
		} else {
			result.append("<" + this.primitiveTransf);
		}

		if(this.datatype != null && this.datatype != "" && this.autoGeneratedDatatype) {
			result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.datatype + "\"");
		}

		if(this.isCollection != null && this.isCollection != "") {
			result.append(" " + R2OConstants.IS_COLLECTION_ATTRIBUTE + "=\"" + this.isCollection + "\"");
		}

		result.append(">\n");
		
		for(R2OArgumentRestriction argRestriction : argRestrictions) {
			result.append(argRestriction.toString() + "\n");
		}
		result.append("</" + this.primitiveTransf + ">");
		return result.toString();
	}
	
	public Collection<ZSelectItem> getInvolvedExpression() {
		Vector<ZSelectItem> result = new Vector<ZSelectItem>();
		
		for(R2OArgumentRestriction argRestriction : argRestrictions) {
			R2ORestriction restriction = argRestriction.getRestriction();
			RestrictionType restrictionType = restriction.getRestrictionType();
			if(restrictionType == RestrictionType.HAS_COLUMN) {
				ZConstant columnName = new ZConstant(restriction.getHasColumn(), ZConstant.COLUMNNAME);
				ZSelectItem selectItem = new ZSelectItem();
				selectItem.setExpression(columnName);
				String alias = restriction.getAlias();
				if(alias == null || alias == "") {
					alias = restriction.getHasColumn().replaceAll("\\.", "_");
				}
				selectItem.setAlias(alias);
				result.add(selectItem);
			} else if(restrictionType == RestrictionType.HAS_SQL) {
				ZSelectItem selectItem = new ZSelectItem(restriction.getHasSQL());
				ZConstant selectItemExpression = new ZConstant(restriction.getHasSQL(), ZConstant.UNKNOWN);
				selectItem.setExpression(selectItemExpression);
				String alias = restriction.getAlias();
				if(alias == null || alias=="") {
					alias = R2OConstants.RESTRICTION_ALIAS + restriction.hashCode();
				}
				selectItem.setAlias(alias);
				result.add(selectItem);				
			} else if(restrictionType == RestrictionType.HAS_TRANSFORMATION) {
				result.addAll(restriction.getHasTransformation().getInvolvedExpression());
			}
		}
		return result;
	}

	public Vector<String> getInvolvedTables() {
		Vector<String> result = new Vector<String>();
		
		for(R2OArgumentRestriction argRestriction : argRestrictions) {
			R2ORestriction restriction = argRestriction.getRestriction();
			RestrictionType restrictionType = restriction.getRestrictionType();
			if(restrictionType == RestrictionType.HAS_COLUMN) {
				String columnName = restriction.getHasColumn(); 
				String tableName = columnName.substring(0, columnName.lastIndexOf("."));
				result.add(tableName);
			}
		}
		return result;
	}

	public String getPrimitiveTransf() {
		return primitiveTransf;
	}

	public List<R2OArgumentRestriction> getArgRestrictions() {
		return argRestrictions;
	}

	public String getOperId() {
		return operId;
	}

	public String getDatatype() {
		return datatype;
	}

	public String getIsCollection() {
		return isCollection;
	}

}
