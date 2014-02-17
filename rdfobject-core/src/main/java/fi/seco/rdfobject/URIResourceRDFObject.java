package fi.seco.rdfobject;

import java.util.Locale;

public class URIResourceRDFObject extends URIResource implements IRDFObject {

	public URIResourceRDFObject(String uri) {
		super(uri);
	}

	@Override
	public Locale getLang() {
		throw new UnsupportedOperationException("An URI has no language");
	}

	@Override
	public String getText() {
		throw new UnsupportedOperationException("An URI has no text");
	}

	@Override
	public String getDataType() {
		throw new UnsupportedOperationException("An URI has no datatype");
	}

	@Override
	public boolean isLiteral() {
		return false;
	}

	@Override
	public boolean isResource() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IRDFObject)) return false;
		if (!((IRDFObject) obj).isResource()) return false;
		return super.equals(obj);
	}

}
