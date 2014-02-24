/**
 * 
 */
package fi.seco.rdfobject;

import java.util.Locale;

/**
 * @author jiemakel
 * 
 */
public interface IRDFObjectPacker {

	public IRDFObject byteArrayToRDFObject(byte[] object, int offset, int endOffset);

	public byte[] literalToByteArray(String text, Locale locale, String datatype);

	public byte[] literalToByteArray(ILiteral l);

	public byte[] uriToByteArray(String uri);

	public boolean isLiteral(byte[] object);

	public boolean isURI(byte[] object);

	public boolean isBlankNode(byte[] object);

}