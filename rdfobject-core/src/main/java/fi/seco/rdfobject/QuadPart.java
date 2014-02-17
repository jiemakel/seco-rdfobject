/**
 * 
 */
package fi.seco.rdfobject;

public enum QuadPart {
	SUBJECT(0), PROPERTY(1), OBJECT(2), GRAPH(3);

	private QuadPart(int order) {
		this.order = order;
	}

	public final int order;
}
