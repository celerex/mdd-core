package be.celerex.mdd.api;

import java.util.List;

public interface CollectionProvider<L> {
	public L newInstance();
	public void add(L instance, Object value);
	public int size(L instance);
	public Object get(L instance, int index);
	public List<Object> list(L instance);
	public boolean isCollection(Object object);
}
