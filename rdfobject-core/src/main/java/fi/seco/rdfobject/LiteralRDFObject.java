package fi.seco.rdfobject;

import java.util.Locale;

public class LiteralRDFObject extends Literal implements IRDFObject {

	public LiteralRDFObject(ILiteral literal) {
		super(literal);
	}

	public LiteralRDFObject(String text, Locale lang, String datatype) {
		super(text, lang, datatype);
	}

	public LiteralRDFObject(String text, Locale lang) {
		super(text, lang);
	}

	public LiteralRDFObject(String text) {
		super(text);
	}

	/**
	 * Create a new datatyped literal
	 * 
	 * @param text
	 *            the text of the literal
	 * @param datatype
	 *            the datatype URI
	 */
	public LiteralRDFObject(String text, String datatype) {
		super(text, datatype);
	}

	@Override
	public String getURI() {
		throw new UnsupportedOperationException("A literal has no URI");
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public boolean isResource() {
		return false;
	}

	@Override
	public boolean isBlankNode() {
		return false;
	}

	@Override
	public boolean isURI() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IRDFObject)) return false;
		if (!((IRDFObject) obj).isLiteral()) return false;
		return super.equals(obj);
	}

}
