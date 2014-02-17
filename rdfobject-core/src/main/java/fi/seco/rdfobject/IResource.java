package fi.seco.rdfobject;

/**
 * An RDF resource - an URI or a blank node
 * 
 * @author jiemakel
 * 
 */
public interface IResource {

	public String getURI();

	public String getSerializationForm();

	public boolean isBlankNode();

	public boolean isURI();

	@Override
	public boolean equals(Object other);

	@Override
	public int hashCode();

}