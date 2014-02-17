package fi.seco.rdfobject.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.IntArrayList;

import fi.seco.collections.iterator.ALazyGeneratingIterator;
import fi.seco.collections.iterator.AMappingIterator;
import fi.seco.collections.iterator.EmptyIterator;
import fi.seco.collections.iterator.IIterableIterator;
import fi.seco.collections.iterator.NullFilteringIterator;
import fi.seco.collections.map.EnsuredMap;
import fi.seco.collections.map.IEnsuredMap;
import fi.seco.rdfobject.BNodeResourceRDFObject;
import fi.seco.rdfobject.IQuad;
import fi.seco.rdfobject.IRDFObject;
import fi.seco.rdfobject.ITriple;
import fi.seco.rdfobject.Quad;
import fi.seco.rdfobject.Triple;

public class MemoryRDFObjectTripleModel implements IRDFObjectTripleModel {

	private long bnodeId = 1;

	@Override
	public BNodeResourceRDFObject allocateNewBlankNode() {
		return new BNodeResourceRDFObject("_:b" + (bnodeId++));
	}

	private final IEnsuredMap<IRDFObject, IntArrayList> sMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);
	private final IEnsuredMap<IRDFObject, IntArrayList> pMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);
	private final IEnsuredMap<IRDFObject, IntArrayList> oMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);

	private final List<ITriple> triples = new ArrayList<ITriple>();

	@Override
	public void addTriple(ITriple t) {
		int index = triples.size();
		triples.add(t);
		sMap.ensure(t.getSubject()).add(index);
		pMap.ensure(t.getProperty()).add(index);
		oMap.ensure(t.getObject()).add(index);
	}

	@Override
	public void addTriple(IRDFObject s, IRDFObject p, IRDFObject o) {
		addTriple(new Triple(s, p, o));
	}

	@Override
	public IIterableIterator<ITriple> listTriples() {
		return new NullFilteringIterator<ITriple>(triples);
	}

	private final Map<String, String> prefixMap = new HashMap<String, String>();

	@Override
	public Map<String, String> getPrefixMap() {
		return prefixMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IIterableIterator<ITriple> listMatchingTriples(final IRDFObject s, final IRDFObject p, final IRDFObject o) {
		List<IntArrayList> filtersL = new ArrayList<IntArrayList>();
		IntArrayList clt = null;
		if (s != null) {
			IntArrayList tmp = sMap.get(s);
			if (tmp == null) return EmptyIterator.SHARED_INSTANCE;
			clt = tmp;
		}
		if (p != null) {
			IntArrayList tmp = pMap.get(p);
			if (tmp == null) return EmptyIterator.SHARED_INSTANCE;
			if (clt == null)
				clt = tmp;
			else if (clt.size() > tmp.size()) {
				filtersL.add(clt);
				clt = tmp;
			} else filtersL.add(tmp);
		}
		if (o != null) {
			IntArrayList tmp = oMap.get(o);
			if (tmp == null) return EmptyIterator.SHARED_INSTANCE;
			if (clt == null)
				clt = tmp;
			else if (clt.size() > tmp.size()) {
				filtersL.add(clt);
				clt = tmp;
			} else filtersL.add(tmp);
		}
		if (clt == null) return listTriples();
		final IntArrayList cl = clt;
		if (filtersL.isEmpty()) return new ALazyGeneratingIterator<ITriple>() {

			int i = 0;
			final int size = cl.size();

			@Override
			protected ITriple generateNext() {
				while (true) {
					if (i >= size) return null;
					ITriple pot = triples.get(cl.get(i++));
					if (pot != null) return pot;
				}
			}
		};
		final IntArrayList[] filters = filtersL.toArray(new IntArrayList[filtersL.size()]);
		final int[] curInd = new int[filters.length];
		return new ALazyGeneratingIterator<ITriple>() {

			int i = 0;
			final int size = cl.size();

			@Override
			protected ITriple generateNext() {
				outer: while (true) {
					if (i >= size) return null;
					int pot = cl.get(i++);
					for (int j = 0; j < filters.length; j++) {
						while (filters[j].get(curInd[j]) < pot) {
							curInd[j]++;
							if (curInd[j] >= filters[j].size()) return null;
						}
						if (filters[j].get(curInd[j]) != pot) continue outer;
					}
					ITriple potQ = triples.get(pot);
					if (potQ != null) return potQ;
				}
			}
		};
	}

	@Override
	public void removeTriple(ITriple q) {
		int index = triples.indexOf(q);
		if (index >= 0) triples.set(index, null);
	}

	@Override
	public void removeTriple(IRDFObject s, IRDFObject p, IRDFObject o) {
		removeTriple(new Triple(s, p, o));
	}

	@Override
	public String toString() {
		return "MemoryRDFObjectTripleModel[" + triples.size() + "](" + prefixMap.size() + ")";
	}

	@Override
	public boolean containsTriple(IRDFObject s, IRDFObject p, IRDFObject o) {
		IntArrayList t1 = sMap.get(s);
		if (t1 == null) return false;
		IntArrayList t2 = pMap.get(p);
		if (t2 == null) return false;
		IntArrayList t3 = oMap.get(o);
		if (t3 == null) return false;
		int t2i = t2.size() - 1;
		int t3i = t3.size() - 1;
		for (int i = t1.size(); i-- > 0;) {
			int cur = t1.get(i);
			while (t2.get(t2i) > cur)
				if (--t2i < 0) return false;
			if (t2.get(t2i) != cur) continue;
			while (t3.get(t3i) > cur)
				if (--t3i < 0) return false;
			if (t3.get(t3i) != cur) continue;
			if (triples.get(cur) == null) continue;
			return true;
		}
		return false;
	}

	@Override
	public boolean containsTriple(ITriple triple) {
		return containsTriple(triple.getSubject(), triple.getProperty(), triple.getObject());

	}

	@Override
	public IRDFObjectQuadModel asQuadModel(final IRDFObject graphId) {
		return new IRDFObjectQuadModel() {

			@Override
			public BNodeResourceRDFObject allocateNewBlankNode() {
				return MemoryRDFObjectTripleModel.this.allocateNewBlankNode();
			}

			@Override
			public void addQuad(IQuad q) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void removeQuad(IQuad q) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Map<String, String> getPrefixMap() {
				return MemoryRDFObjectTripleModel.this.getPrefixMap();
			}

			@Override
			public Iterable<IQuad> listQuads() {
				return new AMappingIterator<ITriple, IQuad>(listTriples()) {

					@Override
					protected IQuad map(ITriple src) {
						return new Quad(src, graphId);
					}

				};
			}

			@Override
			public IIterableIterator<IQuad> listMatchingQuads(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
				if (!graphId.equals(g)) return EmptyIterator.getInstance();
				return new AMappingIterator<ITriple, IQuad>(listMatchingTriples(s, p, o)) {

					@Override
					protected IQuad map(ITriple src) {
						return new Quad(src, graphId);
					}

				};
			}

			@Override
			public IRDFObjectTripleModel asTripleModel() {
				return MemoryRDFObjectTripleModel.this;
			}

			@Override
			public void addQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void removeQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsQuad(IQuad quad) {
				return containsQuad(quad.getSubject(), quad.getProperty(), quad.getObject(), quad.getGraph());
			}

			@Override
			public boolean containsQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
				if (!graphId.equals(g)) return false;
				return containsTriple(s, p, o);
			}

		};
	}

}
