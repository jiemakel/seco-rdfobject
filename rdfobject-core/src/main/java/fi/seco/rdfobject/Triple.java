package fi.seco.rdfobject;

public class Triple implements ITriple {

	protected final IRDFObject subjectURI;
	protected final IRDFObject propertyURI;
	protected final IRDFObject objectURI;

	public Triple(IRDFObject subjectURI, IRDFObject propertyURI, IRDFObject objectURI) {
		this.subjectURI = subjectURI;
		this.propertyURI = propertyURI;
		this.objectURI = objectURI;
	}

	public Triple(ITriple other) {
		this.subjectURI = other.getSubject();
		this.propertyURI = other.getProperty();
		this.objectURI = other.getObject();
	}

	@Override
	public IRDFObject getSubject() {
		return subjectURI;
	}

	@Override
	public IRDFObject getProperty() {
		return propertyURI;
	}

	@Override
	public IRDFObject getObject() {
		return objectURI;
	}

	@Override
	public String toString() {
		return subjectURI + "," + propertyURI + "," + objectURI;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectURI == null) ? 0 : objectURI.hashCode());
		result = prime * result + ((propertyURI == null) ? 0 : propertyURI.hashCode());
		result = prime * result + ((subjectURI == null) ? 0 : subjectURI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ITriple)) return false;
		ITriple other = (ITriple) obj;
		if (objectURI == null) {
			if (other.getObject() != null) return false;
		} else if (!objectURI.equals(other.getObject())) return false;
		if (propertyURI == null) {
			if (other.getProperty() != null) return false;
		} else if (!propertyURI.equals(other.getProperty())) return false;
		if (subjectURI == null) {
			if (other.getSubject() != null) return false;
		} else if (!subjectURI.equals(other.getSubject())) return false;
		return true;
	}

}
