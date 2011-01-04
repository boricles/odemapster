package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class R2ODatabaseTable implements R2OElement {
	private String nameAsAttribute;
	private String nameInsideTag;
	private String alias;
	private List<R2ODatabaseColumn> hasColumns;
	
	public R2ODatabaseTable(Element element) throws ParseException {
		this.parse(element);
	}
	
	@Override
	public void parse(Element element) throws ParseException {
		//R2ODatabaseTable result = new R2ODatabaseTable();
		
		this.nameAsAttribute = element.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		if(this.nameAsAttribute == "") {this.nameAsAttribute = null;}
		
		this.nameInsideTag = element.getTextContent();
		if(this.nameInsideTag == "") {this.nameInsideTag = null;}
		
		if(this.nameAsAttribute != null && this.nameInsideTag != null 
				&& !this.nameAsAttribute.equals(this.nameInsideTag)) {
			throw new ParseException("Specify has-table value either in attribute or inside the tag.");
		}
		
		String alias = element.getAttribute(R2OConstants.ALIAS_ATTRIBUTE);
		if(alias!= null ){
			this.alias = alias;
		}
		
		this.hasColumns = new ArrayList<R2ODatabaseColumn>();
		
		NodeList nlKeyColumns = element.getElementsByTagName(R2OConstants.KEYCOL_DESC_TAG);
		for(int i=0; i<nlKeyColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlKeyColumns.item(i);
			R2ODatabaseColumn databaseColumn = new R2ODatabaseColumn(hasColumnElement);
			this.hasColumns.add(databaseColumn);
		}
		NodeList nlForeignColumns = element.getElementsByTagName(R2OConstants.FORKEYCOL_DESC_TAG);
		for(int i=0; i<nlForeignColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlForeignColumns.item(i);
			R2ODatabaseColumn databaseColumn = new R2ODatabaseColumn(hasColumnElement);
			this.hasColumns.add(databaseColumn);
		}
		NodeList nlNormalColumns = element.getElementsByTagName(R2OConstants.NONKEYCOL_DESC_TAG);
		for(int i=0; i<nlNormalColumns.getLength(); i++) {
			Element hasColumnElement = (Element) nlNormalColumns.item(i);
			R2ODatabaseColumn databaseColumn = new R2ODatabaseColumn(hasColumnElement);
			this.hasColumns.add(databaseColumn);
		}
		
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.HAS_TABLE_TAG+ " ");
		
		if(this.nameAsAttribute != null) {
			result.append(R2OConstants.NAME_ATTRIBUTE + "=\"" + this.nameAsAttribute + "\" ");
		}
		
		if(this.alias != null && this.alias != "") {
			result.append(R2OConstants.ALIAS_ATTRIBUTE + "=\"" + this.alias + "\" ");
		}
		
		

		result.append(">");

		if(this.nameInsideTag != null) {
			result.append(this.nameInsideTag);
		}
		
		if(this.hasColumns != null && this.hasColumns.size() > 0) {
			for(R2ODatabaseColumn hasColumn : hasColumns) {
				result.append(hasColumn.toString() + "\n");
			}
			
		}
		
		result.append("</" + R2OConstants.HAS_TABLE_TAG + ">");
		return result.toString();
	}

	public String getName() {
		if(this.nameAsAttribute != null && this.nameAsAttribute != "") {
			return nameAsAttribute;
		} else {
			return this.nameInsideTag;
		}
		
	}

	public String getAlias() {
		return alias;
	}

	
}
