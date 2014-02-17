package fi.seco.rdfobject;

public class Quad extends Triple implements IQuad {

	private final IRDFObject graphURI;

	public Quad(IRDFObject subjectURI, IRDFObject propertyURI, IRDFObject objectURI, IRDFObject graphURI) {
		super(subjectURI, propertyURI, objectURI);
		this.graphURI = graphURI;
	}

	public Quad(ITriple t, IRDFObject graphURI) {
		super(t);
		this.graphURI = graphURI;
	}

	@Override
	public IRDFObject getGraph() {
		return graphURI;
	}

	@Override
	public String toString() {
		return super.toString() + "," + graphURI;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + (graphURI != null ? graphURI.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof IQuad)) return false;
		IQuad other = (IQuad) obj;
		if (graphURI == null) {
			if (other.getGraph() != null) return false;
		} else if (!graphURI.equals(other.getGraph())) return false;
		return super.equals(obj);
	}
}
