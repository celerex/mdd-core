package be.celerex.mdd.core;

import java.util.List;

import be.celerex.mdd.api.MetaProvider;

public class NOPMetaProvider implements MetaProvider<Object, Object> {

	@Override
	public Object newInstance() {
		return null;
	}

	@Override
	public void set(Object instance, String key, Object value) {
		// we do nothing
	}

	@Override
	public Object get(Object instance, String key) {
		return null;
	}

	@Override
	public List<String> keys(Object instance) {
		return null;
	}

	@Override
	public boolean isObject(Object object) {
		return false;
	}

	@Override
	public void setMetadata(Object object, Object metadata) {
		// we do nothing		
	}

	@Override
	public Object getMetadata(Object object) {
		return null;
	}

}
