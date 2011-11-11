package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.sql.SQLException;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public interface R2RMLElementVisitor {
	public Object visit(R2RMLMappingDocument mappingDocument) throws Exception;
	public Object visit(R2RMLTriplesMap triplesMap) throws Exception;
	public Object visit(R2RMLLogicalTable logicalTable);
	public Object visit(R2RMLObjectMap objectMap);
	public Object visit(R2RMLRefObjectMap refObjectMap);
}
