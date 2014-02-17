package fi.seco.rdfobject.model;

import static org.junit.Assert.fail;

import org.junit.Test;

import fi.seco.rdfobject.IQuad;
import fi.seco.rdfobject.IRDFObject;
import fi.seco.rdfobject.LiteralRDFObject;
import fi.seco.rdfobject.Quad;
import fi.seco.rdfobject.URIResourceRDFObject;

public class TestMemoryRDFObjectQuadModel {

	private static final void testSize(int expectedSize, IRDFObjectQuadModel m, IRDFObject s, IRDFObject p,
			IRDFObject o, IRDFObject g) {
		int size = 0;
		for (IQuad q : m.listMatchingQuads(s, p, o, g))
			size++;
		if (size != expectedSize) fail("returned " + size + " quads instead of expected " + expectedSize);
	}

	private static final IRDFObject graph = new URIResourceRDFObject("http://test.fi/graph/");
	private static final IRDFObject graph2 = new URIResourceRDFObject("http://test.fi/graph2/");
	private static final IRDFObject r1 = new URIResourceRDFObject("http://test.fi/1");
	private static final IRDFObject r2 = new URIResourceRDFObject("http://test.fi/2");
	private static final IRDFObject r3 = new URIResourceRDFObject("http://test.fi/3");
	private static final IRDFObject lit = new LiteralRDFObject("test");

	@Test
	public void testIterator() {
		MemoryRDFObjectQuadModel m = new MemoryRDFObjectQuadModel();
		m.addQuad(new Quad(r1, r2, r3, graph));
		m.addQuad(new Quad(r1, r2, lit, graph));
		m.addQuad(new Quad(r2, r2, lit, graph));
		m.addQuad(new Quad(r2, r2, lit, graph2));
		m.addQuad(new Quad(r3, r2, r1, graph));
		testSize(5, m, null, null, null, null);
		testSize(2, m, r1, null, null, null);
		testSize(5, m, null, r2, null, null);
		testSize(4, m, null, r2, null, graph);
		testSize(3, m, null, null, lit, null);
		testSize(2, m, null, null, lit, graph);
		testSize(4, m, null, null, null, graph);
		testSize(2, m, null, null, lit, graph);
		testSize(1, m, r1, null, lit, graph);
		testSize(2, m, r2, r2, null, null);
		testSize(1, m, r2, r2, null, graph);
		testSize(1, m, r2, r2, lit, graph);
		testSize(0, m, lit, r3, r1, graph);
		testSize(0, m, lit, null, null, graph);
		testSize(0, m, lit, null, null, null);
		testSize(0, m, r1, null, null, graph2);
	}

	@Test
	public void testAddAndRemove() {
		MemoryRDFObjectQuadModel m = new MemoryRDFObjectQuadModel();
		m.addQuad(new Quad(r1, r2, r3, graph));
		m.addQuad(new Quad(r2, r2, r3, graph));
		m.addQuad(new Quad(r2, lit, r3, graph));
		testSize(3, m, null, null, null, null);
		testSize(2, m, r2, null, null, null);
		testSize(2, m, r2, null, r3, null);
		testSize(3, m, null, null, r3, graph);
		m.removeQuad(new Quad(r2, r2, r3, graph));
		testSize(2, m, null, null, null, null);
		testSize(1, m, r2, null, null, null);
		testSize(1, m, null, r2, r3, graph);
		testSize(0, m, r2, r2, r3, null);
		testSize(0, m, r2, r2, r3, graph);
	}

}
