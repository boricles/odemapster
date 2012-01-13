package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class VariableBinding {
	private Node var;
	private Triple tp;
	private String dbCol;

	VariableBinding(Node var, Triple tp, String dbCol) {
		super();
		this.var = var;
		this.tp = tp;
		this.dbCol = dbCol;
	}

	@Override
	public String toString() {
		return this.var + "---" + this.tp + "---" + this.dbCol;
	}
	

	
}