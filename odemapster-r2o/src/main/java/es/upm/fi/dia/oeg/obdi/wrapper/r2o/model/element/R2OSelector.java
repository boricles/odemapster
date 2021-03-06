package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.util.Random;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2OSelector implements R2OElement, Cloneable {
	private Logger logger = Logger.getLogger(R2OSelector.class);
	
//	(38) selector::= selector (applies-if cond-expr)?
//            (aftertransform transformation)?
	private R2OConditionalExpression appliesIf;
	private R2OTransformationExpression afterTransform;
	
	public R2OSelector(R2OConditionalExpression appliesIf,
			R2OTransformationExpression afterTransform) {
		super();
		this.appliesIf = appliesIf;
		this.afterTransform = afterTransform;
	}

	private String afterTransformAlias;
	
	
	public R2OSelector(Element element) throws ParseException {
		this.parse(element);
	}
	
	public void parse(Element element) throws ParseException {
		//R2OSelector result = new R2OSelector();
		NodeList afterTransformElements = element.getElementsByTagName(R2OConstants.AFTERTRANSFORM_TAG);
		if(afterTransformElements != null && afterTransformElements.getLength() > 0) {
			Element afterTransformElement = (Element) afterTransformElements.item(0);
			this.afterTransform = new R2OTransformationExpression(afterTransformElement);
		}
		
		
		NodeList appliesIfElements = element.getElementsByTagName(R2OConstants.APPLIES_IF_TAG);
		if(appliesIfElements != null && appliesIfElements.getLength() > 0) {
			Element appliesIfElement = (Element) appliesIfElements.item(0);
			Element conditionalExpressionElement = XMLUtility.getChildElements(appliesIfElement).get(0);
			this.appliesIf = new R2OConditionalExpression(conditionalExpressionElement);
		}
		
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + R2OConstants.SELECTOR_TAG + ">\n");
		if(appliesIf != null) {
			result.append("<" + R2OConstants.APPLIES_IF_TAG + ">\n");
			result.append(appliesIf.toString() + "\n");
			result.append("</" + R2OConstants.APPLIES_IF_TAG + ">\n");
		}
		
		if(afterTransform != null) {
			result.append("<" + R2OConstants.AFTERTRANSFORM_TAG + ">\n");
			result.append(afterTransform.toString() + "\n");
			result.append("</" + R2OConstants.AFTERTRANSFORM_TAG + ">\n");
		}
		
		result.append("</" + R2OConstants.SELECTOR_TAG + ">");
		return result.toString();
	}

	public R2OConditionalExpression getAppliesIf() {
		return appliesIf;
	}

	public R2OTransformationExpression getAfterTransform() {
		return afterTransform;
	}

	public String generateAppliesIfAlias() {
		return R2OConstants.APPLIES_IF_ALIAS + this.hashCode();
	}

	public String generateAfterTransformAlias() {
		if(this.afterTransformAlias == null) {
			this.afterTransformAlias = R2OConstants.AFTERTRANSFORM_ALIAS + new Random().nextInt(10000);
		}
		return this.afterTransformAlias;
	}

	public void setAppliesIf(R2OConditionalExpression appliesIf) {
		this.appliesIf = appliesIf;
	}

	@Override
	public R2OSelector clone() {
		try {
			return (R2OSelector) super.clone();	
		} catch(Exception e) {
			logger.error("Error occured while cloning R2OSelector object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	
	
}
