package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.text.DateFormat;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;


public class R2ODatabaseColumn implements R2OElement  {
	private String alias;
	private boolean autoGeneratedAlias = false;
	private String fullColumnName;
	private String dataType;
	private DateFormat df;
	
	private ColumnType columnType;
	
	public enum ColumnType {
		PRIMARY_KEY_COLUMN, FOREIGN_KEY_COLUMN, NORMAL_COLUMN
	}

	public R2ODatabaseColumn(String fullColumnName) {
		this.fullColumnName = fullColumnName;
	}
	
	public R2ODatabaseColumn(Element element) throws ParseException {
		this.parse(element);
	}

	
	public void parse(Element element) throws ParseException {
		//R2ODatabaseColumn result = new R2ODatabaseColumn();
		
		this.fullColumnName = element.getTextContent();
		String alias = element.getAttribute(R2OConstants.ALIAS_ATTRIBUTE);
		if(alias != null && alias != "") {
			this.alias = alias;
		} else {
//			this.alias = this.generateAlias();
//			this.autoGeneratedAlias = true;
		}
		 
		this.dataType = element.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		
		String dateFormatString = element.getAttribute(R2OConstants.DATE_FORMAT_ATTRIBUTE);
		if(dateFormatString != null && !dateFormatString.equals("")) {
			
		}
		
	}

	/*
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + R2OConstants.HAS_COLUMN_TAG);

		if(this.alias != null && this.alias != "") {
			result.append(" " + R2OConstants.ALIAS_ATTRIBUTE + "=\"" + this.alias + "\"");
		}		
		
		if(this.dataType != null && this.dataType != "") {
			result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.dataType + "\"");
		}		

		result.append(" >");
		
		result.append(this.columnName);
		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_COLUMN_TAG));
		
		return result.toString();
	}
	*/

	public String getFullColumnName() {
		return fullColumnName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getAlias() {
		return alias;
	}


	public void setAlias(String alias) {
		this.alias = alias;
	}


	public boolean isAutoGeneratedAlias() {
		return autoGeneratedAlias;
	}


	public void setAutoGeneratedAlias(boolean autoGeneratedAlias) {
		this.autoGeneratedAlias = autoGeneratedAlias;
	}

	public String generateAlias() {
		return fullColumnName.replaceAll("\\.", "_");
	}
	
	public String getColumnNameOnly() {
		String[] fullColumnNameSplit = this.fullColumnName.split("\\.");
		return fullColumnNameSplit[fullColumnNameSplit.length-1];
	}

	public String getTableName() {
		String tableName;
		String[] fullColumnNameSplit = this.fullColumnName.split("\\.");
		if(fullColumnNameSplit.length == 2) {
			//VIEW.COLUMN
			tableName = fullColumnNameSplit[1];
		} else if(fullColumnNameSplit.length == 3) {
			//DB.TABLE.COLUMN
			tableName = fullColumnNameSplit[1];
		} else if(fullColumnNameSplit.length == 4) {
			//DB.SCHEMA.TABLE.COLUMN
			tableName = fullColumnNameSplit[2];			
		} else {
			tableName = null;
		}
		return tableName;
	}
	
	public void setFullColumnName(String fullColumnName) {
		this.fullColumnName = fullColumnName;
	}
	
}
