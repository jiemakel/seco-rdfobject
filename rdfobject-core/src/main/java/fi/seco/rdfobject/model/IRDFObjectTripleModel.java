package fi.seco.rdfobject.model;

import java.util.Map;

import fi.seco.collections.iterator.IIterableIterator;
import fi.seco.rdfobject.BNodeResourceRDFObject;
import fi.seco.rdfobject.IRDFObject;
import fi.seco.rdfobject.ITriple;

/**
 * A model storing RDF triples
 * 
 * @author jiemakel
 * 
 */
public interface IRDFObjectTripleModel {
	public BNodeResourceRDFObject allocateNewBlankNode();

	public void addTriple(ITriple t);

	public void addTriple(IRDFObject s, IRDFObject p, IRDFObject o);

	public void removeTriple(ITriple t);

	public void removeTriple(IRDFObject s, IRDFObject p, IRDFObject o);

	public Map<String, String> getPrefixMap();

	public Iterable<? extends ITriple> listTriples();

	public IIterableIterator<? extends ITriple> listMatchingTriples(IRDFObject s, IRDFObject p, IRDFObject o);

	/**
	 * Check whether the model contains a specific triple
	 * 
	 * @param triple
	 *            the triple to check for
	 * @return true if the model contains the triple
	 */
	public boolean containsTriple(ITriple triple);

	public boolean containsTriple(IRDFObject s, IRDFObject p, IRDFObject o);

	/**
	 * Get a view on this model as a quad model
	 * 
	 * @param graphId
	 *            the graph id to use for all triples in this model
	 * @return a view of this triple model as a quad model
	 */
	public IRDFObjectQuadModel asQuadModel(IRDFObject graphId);

}
