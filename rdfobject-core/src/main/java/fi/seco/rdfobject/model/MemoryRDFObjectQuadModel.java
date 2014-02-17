package fi.seco.rdfobject.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.IntArrayList;

import fi.seco.collections.iterator.AFilteringIterator;
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

public class MemoryRDFObjectQuadModel implements IRDFObjectQuadModel {

	private long bnodeId = 1;

	@Override
	public BNodeResourceRDFObject allocateNewBlankNode() {
		return new BNodeResourceRDFObject("_:b" + (bnodeId++));
	}

	private final IEnsuredMap<IRDFObject, IntArrayList> sMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);
	private final IEnsuredMap<IRDFObject, IntArrayList> pMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);
	private final IEnsuredMap<IRDFObject, IntArrayList> oMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);
	private final IEnsuredMap<IRDFObject, IntArrayList> gMap = new EnsuredMap<IRDFObject, IntArrayList>(IntArrayList.class);

	private final List<IQuad> quads = new ArrayList<IQuad>();

	@Override
	public void addQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
		addQuad(new Quad(s, p, o, g));
	}

	@Override
	public void addQuad(IQuad q) {
		int index = quads.size();
		quads.add(q);
		sMap.ensure(q.getSubject()).add(index);
		pMap.ensure(q.getProperty()).add(index);
		oMap.ensure(q.getObject()).add(index);
		gMap.ensure(q.getGraph()).add(index);
	}

	@Override
	public IIterableIterator<IQuad> listQuads() {
		return new NullFilteringIterator<IQuad>(quads);
	}

	private final Map<String, String> prefixMap = new HashMap<String, String>();

	@Override
	public Map<String, String> getPrefixMap() {
		return prefixMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IIterableIterator<IQuad> listMatchingQuads(final IRDFObject s, final IRDFObject p, final IRDFObject o,
			final IRDFObject g) {
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
		if (g != null) {
			IntArrayList tmp = gMap.get(g);
			if (tmp == null) return EmptyIterator.SHARED_INSTANCE;
			if (clt == null)
				clt = tmp;
			else if (clt.size() > tmp.size()) {
				filtersL.add(clt);
				clt = tmp;
			} else filtersL.add(tmp);
		}
		if (clt == null) return listQuads();
		final IntArrayList cl = clt;
		if (filtersL.isEmpty()) return new ALazyGeneratingIterator<IQuad>() {

			int i = 0;
			final int size = cl.size();

			@Override
			protected IQuad generateNext() {
				while (true) {
					if (i >= size) return null;
					IQuad pot = quads.get(cl.get(i++));
					if (pot != null) return pot;
				}
			}
		};
		final IntArrayList[] filters = filtersL.toArray(new IntArrayList[filtersL.size()]);
		final int[] curInd = new int[filters.length];
		return new ALazyGeneratingIterator<IQuad>() {

			int i = 0;
			final int size = cl.size();

			@Override
			protected IQuad generateNext() {
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
					IQuad potQ = quads.get(pot);
					if (potQ != null) return potQ;
				}
			}
		};
	}

	@Override
	public void removeQuad(IQuad q) {
		int index = quads.indexOf(q);
		if (index >= 0) quads.set(index, null);
	}

	@Override
	public void removeQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
		removeQuad(new Quad(s, p, o, g));
	}

	@Override
	public IRDFObjectTripleModel asTripleModel() {
		return new IRDFObjectTripleModel() {

			@Override
			public BNodeResourceRDFObject allocateNewBlankNode() {
				return MemoryRDFObjectQuadModel.this.allocateNewBlankNode();
			}

			@Override
			public void addTriple(ITriple t) {
				throw new UnsupportedOperationException();

			}

			@Override
			public void removeTriple(ITriple t) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Map<String, String> getPrefixMap() {
				return MemoryRDFObjectQuadModel.this.getPrefixMap();
			}

			@Override
			public Iterable<? extends ITriple> listTriples() {
				return new AFilteringIterator<ITriple>(listQuads()) {

					private ITriple last = new Triple(null, null, null);

					@Override
					protected boolean accept(ITriple object) {
						if (object.getSubject().equals(last.getSubject()) && object.getProperty().equals(last.getProperty()) && object.getObject().equals(last.getObject()))
							return false;
						last = object;
						return true;
					}
				};
			}

			@Override
			public IIterableIterator<? extends ITriple> listMatchingTriples(IRDFObject s, IRDFObject p, IRDFObject o) {
				return new AFilteringIterator<ITriple>(listMatchingQuads(s, p, o, null)) {

					private ITriple last = new Triple(null, null, null);

					@Override
					protected boolean accept(ITriple object) {
						if (object.getSubject().equals(last.getSubject()) && object.getProperty().equals(last.getProperty()) && object.getObject().equals(last.getObject()))
							return false;
						last = object;
						return true;
					}
				};
			}

			@Override
			public boolean containsTriple(ITriple triple) {
				return listMatchingQuads(triple.getSubject(), triple.getProperty(), triple.getObject(), null).hasNext();
			}

			private final IRDFObjectTripleModel mthis = this;

			@Override
			public IRDFObjectQuadModel asQuadModel(final IRDFObject graphId) {
				return new IRDFObjectQuadModel() {

					@Override
					public BNodeResourceRDFObject allocateNewBlankNode() {
						return MemoryRDFObjectQuadModel.this.allocateNewBlankNode();
					}

					@Override
					public void addQuad(IQuad q) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void addQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void removeQuad(IQuad q) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void removeQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
						throw new UnsupportedOperationException();
					}

					@Override
					public Map<String, String> getPrefixMap() {
						return MemoryRDFObjectQuadModel.this.getPrefixMap();
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
					public IIterableIterator<IQuad> listMatchingQuads(IRDFObject s, IRDFObject p, IRDFObject o,
							IRDFObject g) {
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
						return mthis;
					}

					@Override
					public boolean containsQuad(IQuad quad) {
						if (!graphId.equals(quad.getGraph())) return false;
						return containsTriple(quad);
					}

					@Override
					public boolean containsQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
						if (!graphId.equals(g)) return false;
						return containsTriple(s, p, o);
					}

				};
			}

			@Override
			public void addTriple(IRDFObject s, IRDFObject p, IRDFObject o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void removeTriple(IRDFObject s, IRDFObject p, IRDFObject o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsTriple(IRDFObject s, IRDFObject p, IRDFObject o) {
				return listMatchingQuads(s, p, o, null).hasNext();
			}

		};
	}

	@Override
	public boolean containsQuad(IQuad quad) {
		return containsQuad(quad.getSubject(), quad.getProperty(), quad.getObject(), quad.getGraph());
	}

	@Override
	public boolean containsQuad(IRDFObject s, IRDFObject p, IRDFObject o, IRDFObject g) {
		IntArrayList[] filters = new IntArrayList[3];
		IntArrayList clt = null;
		IntArrayList tmp = sMap.get(s);
		if (tmp == null) return false;
		clt = tmp;
		tmp = pMap.get(p);
		if (tmp == null) return false;
		if (clt.size() > tmp.size()) {
			filters[0] = clt;
			clt = tmp;
		} else filters[0] = tmp;
		tmp = oMap.get(o);
		if (tmp == null) return false;
		if (clt.size() > tmp.size()) {
			filters[1] = clt;
			clt = tmp;
		} else filters[1] = tmp;
		tmp = gMap.get(g);
		if (tmp == null) return false;
		if (clt.size() > tmp.size()) {
			filters[2] = clt;
			clt = tmp;
		} else filters[2] = tmp;
		int[] curInd = new int[filters.length];
		int i = 0;
		int size = clt.size();
		outer: while (true) {
			if (i >= size) return false;
			int pot = clt.get(i++);
			for (int j = 0; j < filters.length; j++) {
				while (filters[j].get(curInd[j]) < pot) {
					curInd[j]++;
					if (curInd[j] >= filters[j].size()) return false;
				}
				if (filters[j].get(curInd[j]) != pot) continue outer;
				IQuad potQ = quads.get(pot);
				if (potQ != null) return true;
			}
		}
	}

}
