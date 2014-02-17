package fi.seco.rdfobject;


public class BNodeResource implements IResource {

	private final String uri;

	public BNodeResource(String uri) {
		this.uri = uri;
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public String getSerializationForm() {
		return getURI();
	}

	@Override
	public String toString() {
		return uri;
	}

	@Override
	public boolean isBlankNode() {
		return true;
	}

	@Override
	public boolean isURI() {
		return false;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof IResource)) return false;
		if (!((IResource) other).isBlankNode()) return false;
		return uri.equals(((IResource) other).getURI());
	}

}
