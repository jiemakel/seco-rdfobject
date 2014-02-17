/**
 * 
 */
package fi.seco.rdfobject.jena;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

import fi.seco.rdfobject.BNodeResourceRDFObject;
import fi.seco.rdfobject.IQuad;
import fi.seco.rdfobject.IRDFObject;
import fi.seco.rdfobject.ITriple;
import fi.seco.rdfobject.LiteralRDFObject;
import fi.seco.rdfobject.URIResourceRDFObject;
import fi.seco.util.LocaleUtil;

/**
 * @author jiemakel
 * 
 */
public class JenaRDFObjectUtil {

	private static final Model m = ModelFactory.createDefaultModel();

	public static RDFNode getRDFNodeForRDFObject(IRDFObject o) {
		if (o.isLiteral()) {
			if (o.getLang() == null && o.getDataType() == null)
				return ResourceFactory.createPlainLiteral(o.getText());
			else if (o.getLang() != null)
				return m.createLiteral(o.getText(), o.getLang().toString());
			else return ResourceFactory.createTypedLiteral(o.getText(), TypeMapper.getInstance().getSafeTypeByName(o.getDataType()));
		} else if (o.isBlankNode())
			return m.createResource(new AnonId(o.getURI()));
		else return ResourceFactory.createResource(o.getURI());
	}

	public static Node getNodeForRDFObject(IRDFObject id) {
		if (id.isResource()) {
			String uri;
			uri = id.getURI();
			if (id.isBlankNode())
				return NodeFactory.createAnon(new AnonId(uri));
			else return NodeFactory.createURI(uri);
		} else if (id.getLang() == null && id.getDataType() == null)
			return NodeFactory.createLiteral(id.getText(), null, null);
		else if (id.getLang() != null)
			return NodeFactory.createLiteral(id.getText(), id.getLang().toString(), null);
		else return NodeFactory.createLiteral(id.getText(), null, TypeMapper.getInstance().getSafeTypeByName(id.getDataType()));

	}

	public static Triple getJenaTripleForTriple(ITriple t) {
		return new Triple(getNodeForRDFObject(t.getSubject()), getNodeForRDFObject(t.getProperty()), getNodeForRDFObject(t.getObject()));
	}

	public static Statement getJenaStatementForTriple(ITriple t) {
		RDFNode subject = getRDFNodeForRDFObject(t.getSubject());
		RDFNode object = getRDFNodeForRDFObject(t.getObject());
		if (!subject.isResource())
			throw new IllegalArgumentException("Cannot make Jena statement where subject is not a resource: " + t);
		if (!t.getProperty().isURI())
			throw new IllegalArgumentException("Cannot make Jena statement where property is not a URI resource: " + t);
		return ResourceFactory.createStatement(subject.asResource(), ResourceFactory.createProperty(t.getProperty().getURI()), object);
	}

	public static Quad getJenaQuadForQuad(IQuad t) {
		return new Quad(getNodeForRDFObject(t.getSubject()), getNodeForRDFObject(t.getProperty()), getNodeForRDFObject(t.getObject()), getNodeForRDFObject(t.getGraph()));
	}

	public static IRDFObject getRDFObjectForNode(Node object) {
		if (object.isLiteral())
			return new LiteralRDFObject(object.getLiteralLexicalForm(), !"".equals(object.getLiteralLanguage()) ? LocaleUtil.parseLocaleString(object.getLiteralLanguage()) : null, object.getLiteralDatatypeURI());
		else if (object.isBlank())
			return new BNodeResourceRDFObject(object.getBlankNodeLabel());
		else return new URIResourceRDFObject(object.getURI());
	}
}
