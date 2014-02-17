package fi.seco.rdfobject;

public class URIResource implements IResource {

	private final String uri;

	public URIResource(String uri) {
		this.uri = uri;
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public String getSerializationForm() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		sb.append(uri);
		sb.append('>');
		return sb.toString();
	}

	@Override
	public String toString() {
		return uri;
	}

	@Override
	public boolean isBlankNode() {
		return false;
	}

	@Override
	public boolean isURI() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof IResource)) return false;
		IResource other = (IResource) obj;
		if (other.isBlankNode()) return false;
		if (!other.isURI()) return false;
		if (uri == null) {
			if (other.getURI() != null) return false;
		} else if (!uri.equals(other.getURI())) return false;
		return true;
	}

}
