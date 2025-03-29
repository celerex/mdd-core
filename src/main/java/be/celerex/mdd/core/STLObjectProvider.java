package be.celerex.mdd.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.celerex.mdd.api.ObjectProvider;

public class STLObjectProvider implements ObjectProvider<Map<String, Object>> {

	@Override
	public Map<String, Object> newInstance() {
		return new LinkedHashMap<String, Object>();
	}

	@Override
	public void set(Map<String, Object> instance, String key, Object value) {
		instance.put(key, value);
	}

	@Override
	public Object get(Map<String, Object> instance, String key) {
		return instance.get(key);
	}

	@Override
	public List<String> keys(Map<String, Object> instance) {
		return new ArrayList<String>(instance.keySet());
	}

	@Override
	public boolean isObject(Object object) {
		return object instanceof Map;
	}

}
