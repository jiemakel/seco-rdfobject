package fi.seco.rdfobject;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Literal implements ILiteral {

	public static final String xsdNS = "http://www.w3.org/2001/XMLSchema#";

	public static final String xsdStringDatatype = xsdNS + "string";
	public static final String xsdDecimalDatatype = xsdNS + "decimal";
	public static final String xsdLongDatatype = xsdNS + "long";
	public static final String xsdUnsignedLongDatatype = xsdNS + "unsignedLong";
	public static final String xsdIntegerDatatype = xsdNS + "integer";
	public static final String xsdNonNegativeIntegerDatatype = xsdNS + "nonNegativeInteger";
	public static final String xsdPositiveIntegerDatatype = xsdNS + "positiveInteger";
	public static final String xsdIntDatatype = xsdNS + "int";
	public static final String xsdUnsignedIntDatatype = xsdNS + "unsignedInt";
	public static final String xsdShortDatatype = xsdNS + "short";
	public static final String xsdUnsignedShortDatatype = xsdNS + "unsignedShort";
	public static final String xsdFloatDatatype = xsdNS + "float";
	public static final String xsdDoubleDatatype = xsdNS + "double";
	public static final String xsdBooleanDatatype = xsdNS + "boolean";
	public static final String xsdDateDatatype = xsdNS + "date";
	public static final String xsdTimeDatatype = xsdNS + "time";
	public static final String xsdDateTimeDatatype = xsdNS + "dateTime";
	public static final String xsdDurationDatatype = xsdNS + "duration";

	private Locale lang;
	private String text;
	private String datatype;

	@Override
	public String getDataType() {
		return datatype;
	}

	public void setDataType(String datatype) {
		this.datatype = datatype;
	}

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Literal.class);

	@Override
	public Locale getLang() {
		return lang;
	}

	@Override
	public String getText() {
		return text;
	}

	public Literal(String text) {
		this.text = text;
	}

	public Literal(String text, Locale lang) {
		this.text = text;
		this.lang = lang;
	}

	public Literal() {}

	public Literal(String text, Locale lang, String datatype) {
		this.text = text;
		this.lang = lang;
		this.datatype = datatype;
	}

	public Literal(ILiteral literal) {
		this.text = literal.getText();
		this.lang = literal.getLang();
		this.datatype = literal.getDataType();
	}

	/**
	 * Create a datatyped literal
	 * 
	 * @param text
	 *            the text of the literal
	 * @param datatype
	 *            the datatype URI
	 */
	public Literal(String text, String datatype) {
		this.text = text;
		this.datatype = datatype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ILiteral)) return false;
		ILiteral other = (ILiteral) obj;
		if (datatype == null) {
			if (other.getDataType() != null) return false;
		} else if (!datatype.equals(other.getDataType())) return false;
		if (lang == null) {
			if (other.getLang() != null) return false;
		} else if (!lang.equals(other.getLang())) return false;
		if (text == null) {
			if (other.getText() != null) return false;
		} else if (!text.equals(other.getText())) return false;
		return true;
	}

	public void setLang(Locale lang) {
		this.lang = lang;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return getSerializationForm();
	}

	@Override
	public String getSerializationForm() {
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		sb.append(text);
		sb.append('"');
		if (lang != null) {
			sb.append('@');
			sb.append(lang);
		}
		if (datatype != null) {
			sb.append("^^<");
			sb.append(datatype);
			sb.append('>');
		}
		return sb.toString();
	}

}