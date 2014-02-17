package fi.seco.rdfobject;

/**
 * An RDF quad
 * 
 * @author jiemakel
 * 
 */
public interface IQuad extends ITriple {
	public IRDFObject getGraph();

	@Override
	public boolean equals(Object other);

	@Override
	public int hashCode();
}
