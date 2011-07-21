package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator.POS;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2ORelationMappingUnfolder;

public class BetaGenerator1 extends AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(BetaGenerator1.class);

	public BetaGenerator1(Map<Node, R2OConceptMapping> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}


	@Override
	ZSelectItem calculateBeta(Triple tp, POS pos) throws Exception {
		R2OConceptMapping cm = this.mapNodeConceptMapping.get(tp.getSubject());
		return this.calculateBetaCM(tp, pos, cm);
	}


	@Override
	R2OSelectItem calculateBetaCM(Triple tp, POS pos, R2OConceptMapping cm)
	throws Exception {
		Node subject = tp.getSubject();
		String predicateURI = tp.getPredicate().getURI();;
		R2OSelectItem selectItem = null;


		if(pos == POS.sub) {
			String cmURIAlias = cm.generateURIAlias();
			selectItem = new R2OSelectItem(cmURIAlias);
		} else if(pos == POS.pre) {
			ZConstant predicateURIConstant = new ZConstant(predicateURI, ZConstant.STRING);
			selectItem = new R2OSelectItem();
			selectItem.setExpression(predicateURIConstant);
		} else if(pos == POS.obj) {
			if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
				ZConstant conceptNameConstant = new ZConstant(cm.getConceptName(), ZConstant.STRING);
				selectItem = new R2OSelectItem();
				selectItem.setExpression(conceptNameConstant);
			} else {
				List<R2OPropertyMapping> pms = cm.getPropertyMappings(predicateURI);
				if(pms != null) {
					for(R2OPropertyMapping pm : pms) {
						if(pm instanceof R2OAttributeMapping) {
							R2OAttributeMapping am = (R2OAttributeMapping) pm;
							Collection<R2OSelector> selectors = am.getSelectors();
							try {
								if(selectors != null){
									if(selectors.size() != 1) {
										String newErrorMessage = "multiple selectors in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									}

									R2OSelector selector = selectors.iterator().next();
									R2OConditionalExpression attributeMappingSelectorAppliesIf = selector.getAppliesIf();
									if(attributeMappingSelectorAppliesIf != null) {
										String newErrorMessage = "conditional expression in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									}

									R2OTransformationExpression attMapSelAT = selector.getAfterTransform();
									if(!attMapSelAT.isDelegableTransformationExpression()) {
										String newErrorMessage = "non delegable conditional expression in query translator is not supported yet.";
										logger.warn(newErrorMessage);
										throw new Exception(newErrorMessage);
									} 

									selectItem = new R2OSelectItem(selector.generateAfterTransformAlias());
								}
							}
							catch(Exception e) {
								e.printStackTrace();
								String newErrorMessage = e.getMessage() + " while processing attribute mapping " + pm.getName();
								logger.error(newErrorMessage);
								throw new Exception(newErrorMessage);
							}

						} else if(pm instanceof R2ORelationMapping) {
							try {
								R2ORelationMapping rm = (R2ORelationMapping) pm;
								R2OConceptMapping parentCM = rm.getParent();
								R2ODatabaseTable r2oBaseTable = parentCM.getHasTables().get(0);
								String cmBaseTable = r2oBaseTable.getName();
								if(r2oBaseTable.getAlias() != null && r2oBaseTable.getAlias() != "") {
									cmBaseTable = r2oBaseTable.getAlias();
								}
								String rangeTableAlias = rm.getRangeTableAlias();
								
								R2OConceptMapping rangeCM = 
									(R2OConceptMapping) this.mappingDocument.getConceptMappingByConceptMappingId(rm.getToConcept());
								R2ORelationMappingUnfolder rmUnfolder = new R2ORelationMappingUnfolder(parentCM, rm, rangeCM);
								Collection<ZSelectItem> selectItems = rmUnfolder.unfoldRangeURI(cmBaseTable,rangeTableAlias);
								if(selectItems.size() > 1) {
									String newErrorMessage = "multiple columns in range uri-as in query translator is not supported yet!";
									logger.error(newErrorMessage);
									throw new Exception(newErrorMessage);									
								}

								//String rangeURIAlias = selectItems.iterator().next().getAlias();
								String rangeURIAlias = rm.generateRangeURIAlias();
								selectItem = new R2OSelectItem(rangeURIAlias);
							} catch(Exception e) {
								String newErrorMessage = e.getMessage() + " while processing relation mapping " + pm.getName();
								logger.error(newErrorMessage);
								throw e;
							}

						}
					}			
				}
			}

		}
		return selectItem;
	}


}