package fi.seco.rdfobject.model;

import java.util.Map;

import fi.seco.collections.iterator.IIterableIterator;
import fi.seco.rdfobject.BNodeResourceRDFObject;
import fi.seco.rdfobject.IQuad;
import fi.seco.rdfobject.IRDFObject;

/**
 * A model storing RDF quads
 * 
 * @author jiemakel
 * 
 */
public interface IRDFObjectQuadModel {
	public BNodeResourceRDFObject allocateNewBlankNode();

	public void addQuad(IQuad q);

	public void addQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g);

	public void removeQuad(IQuad q);

	public void removeQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g);

	public Map<String, String> getPrefixMap();

	public Iterable<IQuad> listQuads();

	/**
	 * Check whether the model contains a specific quad
	 * 
	 * @param quad
	 *            the quad to check for
	 * @return true if the model contains the quad
	 */
	public boolean containsQuad(IQuad quad);

	public boolean containsQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g);

	public IIterableIterator<IQuad> listMatchingQuads(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g);

	/**
	 * @return the model as a triple model
	 */
	public IRDFObjectTripleModel asTripleModel();

}
