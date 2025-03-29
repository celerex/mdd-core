package be.celerex.mdd.core;

import java.util.ArrayList;
import java.util.List;

import be.celerex.mdd.api.CollectionProvider;

public class STLCollectionProvider implements CollectionProvider<List<Object>> {

	@Override
	public List<Object> newInstance() {
		return new ArrayList<Object>();
	}

	@Override
	public void add(List<Object> instance, Object value) {
		instance.add(value);
	}

	@Override
	public List<Object> list(List<Object> instance) {
		return instance;
	}

	@Override
	public boolean isCollection(Object object) {
		return object instanceof List;
	}

	@Override
	public int size(List<Object> instance) {
		return instance.size();
	}

	@Override
	public Object get(List<Object> instance, int index) {
		return instance.get(0);
	}

}
