package fi.seco.rdfobject;

import java.util.Locale;

public class BNodeResourceRDFObject extends BNodeResource implements IRDFObject {

	public BNodeResourceRDFObject(String uri) {
		super(uri);
	}

	@Override
	public Locale getLang() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getText() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDataType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLiteral() {
		return false;
	}

	@Override
	public boolean isResource() {
		return true;
	}

}
