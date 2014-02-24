/**
 * 
 */
package fi.seco.rdfobject;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ByteArrayList;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

import fi.seco.util.VIntUtil;

/**
 * @author jiemakel
 * 
 */
public class RDFObjectPacker implements IRDFObjectPacker {

	private static final Logger log = LoggerFactory.getLogger(RDFObjectPacker.class);

	private static final Charset UTF8 = Charset.forName("UTF-8");

	public final ArrayList<byte[]> pd;
	public final ObjectIntOpenHashMap<String> dp;
	public final ArrayList<Locale> plangs;
	public ObjectIntOpenHashMap<Locale> langsp;
	public final ArrayList<String> pdatatypes;
	public ObjectIntOpenHashMap<String> datatypesp;

	private static final byte[] bzero = VIntUtil.getVInt(0);

	private boolean compressNumbersInURIs = true;

	private int compressLongLiteralsAt = 256;

	public void setCompressNumbersInURIs(boolean compressNumbersInURIs) {
		this.compressNumbersInURIs = compressNumbersInURIs;
	}

	public void setCompressLongLiteralsAt(int compressLongLiteralsAt) {
		this.compressLongLiteralsAt = compressLongLiteralsAt;
	}

	public RDFObjectPacker(ArrayList<byte[]> pd, ArrayList<String> pdatatypes, ArrayList<Locale> plangs) {
		if (pd == null) {
			pd = new ArrayList<byte[]>();
			pd.add(null);
			pd.add(null);
			pd.add(null);
			pd.add(null);
			pd.add(null);
			pd.add(null);
		}
		this.pd = pd;
		dp = new ObjectIntOpenHashMap<String>();
		for (int i = 6; i < pd.size(); i++)
			try {
				dp.put(new String(pd.get(i), "utf-8"), i);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

		if (plangs == null) {
			plangs = new ArrayList<Locale>();
			plangs.add(null);
		}
		this.plangs = plangs;
		langsp = new ObjectIntOpenHashMap<Locale>();
		for (int i = 1; i < plangs.size(); i++)
			langsp.put(plangs.get(i), i);

		if (pdatatypes == null) {
			pdatatypes = new ArrayList<String>();
			pdatatypes.add(null);
		}
		this.pdatatypes = pdatatypes;
		datatypesp = new ObjectIntOpenHashMap<String>();
		for (int i = 1; i < pdatatypes.size(); i++)
			datatypesp.put(pdatatypes.get(i), i);
	}

	@Override
	public final IRDFObject byteArrayToRDFObject(byte[] object, int index, int endIndex) {
		if (object == null) throw new IllegalArgumentException();
		if (endIndex - index == 0) log.error("This shouldn't happen. Zero length RDFObject", new Throwable());
		byte b = object[index++];
		int i = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = object[index++];
			i |= (b & 0x7F) << shift;
		}
		if (i == 1)
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InflaterOutputStream ios = new InflaterOutputStream(baos);
				ios.write(object, index, endIndex - index);
				ios.close();
				object = baos.toByteArray();
				index = 0;
				b = object[index++];
				i = b & 0x7F;
				for (int shift = 7; (b & 0x80) != 0; shift += 7) {
					b = object[index++];
					i |= (b & 0x7F) << shift;
				}
				Locale lang = plangs.get(i);
				b = object[index++];
				i = b & 0x7F;
				for (int shift = 7; (b & 0x80) != 0; shift += 7) {
					b = object[index++];
					i |= (b & 0x7F) << shift;
				}
				String datatype = pdatatypes.get(i);
				return new LiteralRDFObject(new String(object, index, object.length - index, UTF8), lang, datatype);
			} catch (IOException e) {
				throw new IOError(e);
			}
		else if (i == 2)
			return new LiteralRDFObject(new String(object, index, endIndex - index, UTF8), null, null);
		else if (i == 3) { // language-coded literal 
			b = object[index++];
			i = b & 0x7F;
			for (int shift = 7; (b & 0x80) != 0; shift += 7) {
				b = object[index++];
				i |= (b & 0x7F) << shift;
			}
			Locale lang = plangs.get(i);
			return new LiteralRDFObject(new String(object, index, endIndex - index, UTF8), lang, null);
		} else if (i == 4) { // datatyped literal
			b = object[index++];
			i = b & 0x7F;
			for (int shift = 7; (b & 0x80) != 0; shift += 7) {
				b = object[index++];
				i |= (b & 0x7F) << shift;
			}
			String datatype = pdatatypes.get(i);
			return new LiteralRDFObject(new String(object, index, endIndex - index, UTF8), null, datatype);
		} else return new URIResourceRDFObject(byteArrayToURI(object, i, index, endIndex));
	}

	public String byteArrayToURI(byte[] object, int i, int index, int endIndex) {
		byte[] domain = pd.get(i);
		ByteArrayList full;
		if (domain != null) {
			full = new ByteArrayList(domain.length + (endIndex - index) * 2);
			full.add(domain, 0, domain.length);
		} else full = new ByteArrayList((endIndex - index) * 2);
		int cs = index;
		if (compressNumbersInURIs) for (int j = index; j < endIndex; j++)
			if (object[j] >= '1' && object[j] <= '9') {
				full.add(object, cs, j - cs);
				byte size = (byte) (object[j] - '0');
				//try {
			long val = readPositivePackedLong(object, j + 1, size);
			full.add(Long.toString(val).getBytes(UTF8));
			/*} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println(new String(full.buffer, 0, full.elementsCount, UTF8) + " & " + Arrays.toString(object) + "@" + i + "@" + index + ":" + endIndex);
				throw e;
			}*/
			if (size > 1) size--;
			j += size + 1;
			cs = j;
		}
		full.add(object, cs, endIndex - cs);
		return new String(full.buffer, 0, full.elementsCount, UTF8);
	}

	private static int getLastSplit(String uri, int ls) {
		for (int i = ls; i >= 0; i--) {
			char ch = uri.charAt(i);
			if (ch > '?') continue; //last of the split chars in ASCII/Unicode
			if (ch == '#' || ch == '/' || ch == '?' || ch == '&' || ch == ':' || ch == '.') return i;
			if (ch >= '0' && ch <= '9') {
				do {
					if (--i == 0) return -1;
					ch = uri.charAt(i);
				} while (ch >= '0' && ch <= '9');
				return i;
			}
		}
		return -1;
	}

	private static long readPositivePackedLong(byte[] in, int i, byte byteLen) {
		if (byteLen == 1) return in[i++] & 0xFF;
		byteLen--;
		long value = in[i++] & 0xFFL;
		if (byteLen > 1) {
			value |= (in[i++] & 0xFFL) << 8;
			if (byteLen > 2) {
				value |= (in[i++] & 0xFFL) << 16;
				if (byteLen > 3) {
					value |= (in[i++] & 0xFFL) << 24;
					if (byteLen > 4) {
						value |= (in[i++] & 0xFFL) << 32;
						if (byteLen > 5) {
							value |= (in[i++] & 0xFFL) << 40;
							if (byteLen > 6) {
								value |= (in[i++] & 0xFFL) << 48;
								if (byteLen > 7) value |= (in[i++] & 0xFFL) << 56;
							}
						}
					}
				}
			}
		}
		return value + 255;
	}

	private static void writePositivePackedLong(ByteArrayList out, long value) {
		assert value >= 0 : "value " + value + "<0";
		if (value <= 255) {
			out.add((byte) '1');
			out.add((byte) value);
			return;
		}
		int offset = 1;
		byte[] buf = new byte[9];
		value -= 255;
		buf[offset++] = (byte) value;
		if ((value & 0xFFFFFFFFFFFFFF00L) == 0) {
			buf[0] = (byte) '2';
			out.add(buf, 0, 2);
			return;
		}
		buf[offset++] = (byte) (value >>> 8);
		if ((value & 0xFFFFFFFFFFFF0000L) == 0) {
			buf[0] = (byte) '3';
			out.add(buf, 0, 3);
			return;
		}
		buf[offset++] = (byte) (value >>> 16);
		if ((value & 0xFFFFFFFFFF000000L) == 0) {
			buf[0] = (byte) '4';
			out.add(buf, 0, 4);
			return;
		}
		buf[offset++] = (byte) (value >>> 24);
		if ((value & 0xFFFFFFFF00000000L) == 0) {
			buf[0] = (byte) '5';
			out.add(buf, 0, 5);
			return;
		}
		buf[offset++] = (byte) (value >>> 32);
		if ((value & 0xFFFFFF0000000000L) == 0) {
			buf[0] = (byte) '6';
			out.add(buf, 0, 6);
			return;
		}
		buf[offset++] = (byte) (value >>> 40);
		if ((value & 0xFFFF000000000000L) == 0) {
			buf[0] = (byte) '7';
			out.add(buf, 0, 7);
			return;
		}
		buf[offset++] = (byte) (value >>> 48);
		if ((value & 0xFF00000000000000L) == 0) {
			buf[0] = (byte) '8';
			out.add(buf, 0, 8);
			return;
		}
		buf[offset++] = (byte) (value >>> 56);
		buf[0] = (byte) '9';
		out.add(buf, 0, 9);
	}

	private static final int longMaxLength = ("" + Long.MAX_VALUE).length() - 1;

	@Override
	public final byte[] uriToByteArray(String uri) {
		int pl = getLastSplit(uri, uri.length() - 2);
		int prefix = 1;
		while (pl != -1) {
			String domainPart = uri.substring(0, pl + 1);
			prefix = dp.get(domainPart);
			if (prefix != 0) break;
			pl = getLastSplit(uri, pl - 1);
		}
		if (pl == -1) prefix = 5;
		ByteArrayList bac = new ByteArrayList(uri.length() * 2);
		int l = uri.length();
		int cs = pl + 1;
		if (compressNumbersInURIs) for (int i = pl + 1; i < l; i++) {
			char ch = uri.charAt(i);
			if (ch > '0' && ch <= '9') {
				byte[] tmp = uri.substring(cs, i).getBytes(UTF8);
				bac.add(tmp, 0, tmp.length);
				int nb = i;
				do {
					if (++i == l) break;
					ch = uri.charAt(i);
				} while (ch >= '0' && ch <= '9' && i - nb < longMaxLength);
				long val = Long.valueOf(uri.substring(nb, i));
				writePositivePackedLong(bac, val);
				cs = i;
			}
		}
		byte[] tmp = uri.substring(cs, l).getBytes(UTF8);
		bac.add(tmp, 0, tmp.length);
		byte[] p = VIntUtil.getVInt(prefix);
		byte[] ret = new byte[p.length + bac.elementsCount];
		System.arraycopy(p, 0, ret, 0, p.length);
		System.arraycopy(bac.buffer, 0, ret, p.length, bac.elementsCount);
		return ret;
	}

	@Override
	public final byte[] literalToByteArray(ILiteral l) {
		return literalToByteArray(l.getText(), l.getLang(), l.getDataType());
	}

	@Override
	public final byte[] literalToByteArray(String text, Locale lang, String datatype) {
		byte[] b3 = text.getBytes(UTF8);
		if (b3.length > compressLongLiteralsAt) {
			byte[] langArr;
			if (lang != null) {
				int tmp = langsp.get(lang);
				if (tmp == 0) synchronized (plangs) {
					tmp = langsp.get(lang);
					if (tmp == 0) {
						tmp = plangs.size();
						plangs.add(lang);
						ObjectIntOpenHashMap<Locale> tmpH = new ObjectIntOpenHashMap<Locale>(langsp);
						tmpH.put(lang, tmp);
						langsp = tmpH;
					}
				}
				langArr = VIntUtil.getVInt(tmp);
			} else langArr = bzero;
			byte[] datatypeArr;
			if (datatype != null && !Literal.xsdStringDatatype.equals(datatype)) {
				int tmp = datatypesp.get(datatype);
				if (tmp == 0) synchronized (pdatatypes) {
					tmp = datatypesp.get(datatype);
					if (tmp == 0) {
						tmp = pdatatypes.size();
						pdatatypes.add(datatype);
						ObjectIntOpenHashMap<String> tmpH = new ObjectIntOpenHashMap<String>(datatypesp);
						tmpH.put(datatype, tmp);
						datatypesp = tmpH;
					}
				}
				datatypeArr = VIntUtil.getVInt(tmp);
			} else datatypeArr = bzero;
			Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION);
			def.setStrategy(Deflater.DEFAULT_STRATEGY);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(b3.length + langArr.length + datatypeArr.length + 1);
			baos.write(1); //identifier;
			DeflaterOutputStream dos = new DeflaterOutputStream(baos, def);
			try {
				dos.write(langArr);
				dos.write(datatypeArr);
				dos.write(b3);
				dos.close();
				byte[] tmp = baos.toByteArray();
				if (tmp.length < b3.length + langArr.length + datatypeArr.length)
					return tmp;
				else if (log.isDebugEnabled())
					log.debug("Compressed length " + tmp.length + ">uncompressed length " + (b3.length + langArr.length + datatypeArr.length));
			} catch (IOException e) {
				throw new IOError(e);
			} finally {
				def.end();
			}
		}
		if (lang != null) {
			int tmp = langsp.get(lang);
			if (tmp == 0) synchronized (plangs) {
				tmp = langsp.get(lang);
				if (tmp == 0) {
					tmp = plangs.size();
					plangs.add(lang);
					ObjectIntOpenHashMap<Locale> tmpH = new ObjectIntOpenHashMap<Locale>(langsp);
					tmpH.put(lang, tmp);
					langsp = tmpH;
				}
			}
			byte[] langArr = VIntUtil.getVInt(tmp);
			byte[] data = new byte[1 + langArr.length + b3.length];
			data[0] = 3;
			System.arraycopy(langArr, 0, data, 1, langArr.length);
			System.arraycopy(b3, 0, data, 1 + langArr.length, b3.length);
			return data;
		} else if (datatype != null && !Literal.xsdStringDatatype.equals(datatype)) {
			int tmp = datatypesp.get(datatype);
			if (tmp == 0) synchronized (pdatatypes) {
				tmp = datatypesp.get(datatype);
				if (tmp == 0) {
					tmp = pdatatypes.size();
					pdatatypes.add(datatype);
					ObjectIntOpenHashMap<String> tmpH = new ObjectIntOpenHashMap<String>(datatypesp);
					tmpH.put(datatype, tmp);
					datatypesp = tmpH;
				}
			}
			byte[] datatypeArr = VIntUtil.getVInt(tmp);
			byte[] data = new byte[1 + datatypeArr.length + b3.length];
			data[0] = 4;
			System.arraycopy(datatypeArr, 0, data, 1, datatypeArr.length);
			System.arraycopy(b3, 0, data, 1 + datatypeArr.length, b3.length);
			return data;
		} else { //plain literal
			byte[] data = new byte[1 + b3.length];
			data[0] = 2;
			System.arraycopy(b3, 0, data, 1, b3.length);
			return data;
		}
	}

	@Override
	public boolean isLiteral(byte[] object) {
		return object != null && object[0] > 0 && object[0] < 5;
	}

	@Override
	public boolean isURI(byte[] object) {
		return object != null && (object[0] < 0 || object[0] >= 5);
	}

	@Override
	public boolean isBlankNode(byte[] object) {
		return object == null;
	}

}
