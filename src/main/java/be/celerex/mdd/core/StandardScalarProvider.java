package be.celerex.mdd.core;

import be.celerex.mdd.api.ScalarProvider;

public class StandardScalarProvider implements ScalarProvider {

	@Override
	public Object unmarshal(String value, Object into, String key) {
		return value;
	}

	@Override
	public String marshal(Object value, Object parent, String key) {
		return value == null ? null : value.toString();
	}

}
