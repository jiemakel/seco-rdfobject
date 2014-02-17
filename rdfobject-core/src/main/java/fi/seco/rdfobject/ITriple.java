package fi.seco.rdfobject;

/**
 * An RDF triple
 * 
 * @author jiemakel
 * 
 */
public interface ITriple {
	public IRDFObject getSubject();

	public IRDFObject getProperty();

	public IRDFObject getObject();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object other);

}
