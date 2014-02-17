package fi.seco.rdfobject;

/**
 * An RDF object - that is an URI, a blank node or a literal
 * 
 * @author jiemakel
 * 
 */
public interface IRDFObject extends ILiteral, IResource {

	public abstract boolean isLiteral();

	public abstract boolean isResource();

	@Override
	public boolean equals(Object other);

	@Override
	public int hashCode();

}