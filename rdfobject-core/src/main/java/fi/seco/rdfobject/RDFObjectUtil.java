/**
 * 
 */
package fi.seco.rdfobject;

import java.util.Locale;

import fi.seco.util.LocaleUtil;

/**
 * @author jiemakel
 * 
 */
public class RDFObjectUtil {

	public static final IRDFObject rdfType = new URIResourceRDFObject("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	public static final IRDFObject defaultGraph = new URIResourceRDFObject("http://generated.com/defaultGraph");

	/**
	 * Parse a lexical form into a corresponding RDF object
	 * 
	 * @param lexicalForm
	 *            the lexical form to parse
	 * @return an RDF object corresponding to the lexical form
	 */
	public static IRDFObject parseLexicalForm(String lexicalForm) {
		if (lexicalForm.startsWith("_:")) return new BNodeResourceRDFObject(lexicalForm);
		if (lexicalForm.startsWith("\"")) {
			int textEnd = lexicalForm.lastIndexOf('"');
			String text = lexicalForm.substring(1, textEnd);
			int langStart = lexicalForm.indexOf('@', textEnd);
			int datatypeStart = lexicalForm.indexOf("^^<", textEnd);
			Locale lang;
			if (langStart == -1)
				lang = null;
			else lang = datatypeStart != -1 ? LocaleUtil.parseLocaleString(lexicalForm.substring(langStart + 1, datatypeStart)) : LocaleUtil.parseLocaleString(lexicalForm.substring(langStart + 1));
			String datatype;
			if (datatypeStart == -1)
				datatype = null;
			else datatype = lexicalForm.substring(datatypeStart + 3, lexicalForm.length() - 1);
			return new LiteralRDFObject(text, lang, datatype);
		}
		if (lexicalForm.startsWith("<"))
			return new URIResourceRDFObject(lexicalForm.substring(1, lexicalForm.length() - 1));
		return new URIResourceRDFObject(lexicalForm);
	}

}
