package fi.seco.rdfobject;

import java.util.Locale;

/**
 * An RDF literal with an optional lang tag or a datatype
 * 
 * @author jiemakel
 * 
 */
public interface ILiteral {

	public Locale getLang();

	public String getText();

	public String getDataType();

	public String getSerializationForm();

	@Override
	public boolean equals(Object other);

	@Override
	public int hashCode();

}