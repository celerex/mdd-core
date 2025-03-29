package be.celerex.mdd.api;

import java.util.List;

public interface ObjectProvider<O> {
	public O newInstance();
	public void set(O instance, String key, Object value);
	public Object get(O instance, String key);
	public List<String> keys(O instance);
	public boolean isObject(Object object);
}
