/**
 * 
 */
package fi.seco.rdfobject.openrdf;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import fi.seco.openrdf.IllegalURICorrectingValueFactory;
import fi.seco.rdfobject.BNodeResourceRDFObject;
import fi.seco.rdfobject.IQuad;
import fi.seco.rdfobject.IRDFObject;
import fi.seco.rdfobject.LiteralRDFObject;
import fi.seco.rdfobject.Quad;
import fi.seco.rdfobject.URIResourceRDFObject;
import fi.seco.util.LocaleUtil;

/**
 * @author jiemakel
 * 
 */
public class OpenRDFRDFObjectUtil {

	public static IQuad getQuadForStatement(Statement st) {
		return new Quad(getRDFObjectForValue(st.getSubject()), getRDFObjectForValue(st.getPredicate()), getRDFObjectForValue(st.getObject()), getRDFObjectForValue(st.getContext()));
	}

	public static IRDFObject getRDFObjectForValue(Value object) {
		if (object == null)
			return null;
		else if (object instanceof Literal) {
			Literal l = (Literal) object;
			return new LiteralRDFObject(l.getLabel(), LocaleUtil.parseLocaleString(l.getLanguage()), l.getDatatype() != null ? l.getDatatype().toString() : null);
		} else if (object instanceof BNode)
			return new BNodeResourceRDFObject("_:" + ((BNode) object).getID());
		else return new URIResourceRDFObject(((URI) object).stringValue());
	}

	private static final ValueFactory vf = new IllegalURICorrectingValueFactory();

	public static Value getValueForRDFObject(IRDFObject o) {
		if (o == null) return null;
		if (o.isBlankNode()) return vf.createBNode(o.getURI().substring(2));
		if (o.isLiteral()) {
			if (o.getLang() != null) return vf.createLiteral(o.getText(), o.getLang().toString());
			if (o.getDataType() != null) return vf.createLiteral(o.getText(), vf.createURI(o.getDataType()));
			return vf.createLiteral(o.getText());
		}
		return vf.createURI(o.getURI());
	}

	public static Literal getLiteralForRDFObject(IRDFObject o) {
		if (o == null) return null;
		if (!o.isLiteral()) throw new IllegalArgumentException(o + " is not a literal");
		if (o.getLang() != null) return vf.createLiteral(o.getText(), o.getLang().toString());
		if (o.getDataType() != null) return vf.createLiteral(o.getText(), vf.createURI(o.getDataType()));
		return vf.createLiteral(o.getText());
	}

	public static URI getURIForRDFObject(IRDFObject o) {
		if (o == null) return null;
		if (!o.isURI()) throw new IllegalArgumentException(o + " is not a URI");
		return vf.createURI(o.getURI());
	}

	public static Resource getResourceForRDFObject(IRDFObject o) {
		if (o == null) return null;
		if (!o.isResource()) throw new IllegalArgumentException(o + " is not a resource");
		if (o.isBlankNode()) if (o.getURI().startsWith("_:"))
			return vf.createBNode(o.getURI().substring(2));
		else return vf.createBNode(o.getURI());
		return vf.createURI(o.getURI());
	}

	public static Statement getStatementForQuad(IQuad q) {
		return getStatementForQuad(q.getSubject(), q.getProperty(), q.getObject(), q.getGraph());
	}

	public static Statement getStatementForQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
		return vf.createStatement(getResourceForRDFObject(s), getURIForRDFObject(p), getValueForRDFObject(o), getResourceForRDFObject(g));
	}

}
