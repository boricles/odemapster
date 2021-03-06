package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import java.util.Collection;

import Zql.ZQuery;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.engine.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class R2OPropertyMappingUnfolder {
	private R2OConceptMapping parentMapping;
	private AbstractPropertyMapping r2oPropertyMapping;
	private R2OMappingDocument r2oMappingDocument;
	
	
	public R2OPropertyMappingUnfolder(
			R2OConceptMapping parentMapping,
			AbstractPropertyMapping r2oPropertyMapping,
			R2OMappingDocument r2oMappingDocument) {
		super();
		this.parentMapping = parentMapping;
		this.r2oPropertyMapping = r2oPropertyMapping;
		this.r2oMappingDocument = r2oMappingDocument;
	}


	public void unfold(SQLQuery cmQuery) 
	throws AttributeMappingUnfolderException, RelationMappingUnfolderException 
	{
		if(r2oPropertyMapping instanceof R2OAttributeMapping) {
			R2OAttributeMapping r2oAttributeMapping = (R2OAttributeMapping) r2oPropertyMapping;
			R2OAttributeMappingUnfolder r2oAttributeMappingUnfolder =
				new R2OAttributeMappingUnfolder(r2oAttributeMapping);
			Collection<ZSelectItem> attMappingUnfoldingItems = r2oAttributeMappingUnfolder.unfold();
			cmQuery.getSelect().addAll(attMappingUnfoldingItems);
		} else if(r2oPropertyMapping instanceof R2ORelationMapping) {
			R2ORelationMapping r2oRelationMapping = (R2ORelationMapping) r2oPropertyMapping;
			
			String toConcept = r2oRelationMapping.getToConcept();
			R2OConceptMapping rangeConceptMapping = 
				(R2OConceptMapping) this.r2oMappingDocument.getConceptMappingByMappingId(toConcept);
			
			R2ORelationMappingUnfolder r2oRelationMappingUnfolder = new R2ORelationMappingUnfolder(
					this.parentMapping, (R2ORelationMapping) r2oPropertyMapping, rangeConceptMapping);
			r2oRelationMappingUnfolder.unfold(cmQuery);
		}
	}
}
