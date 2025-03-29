package be.celerex.mdd.api;

public interface ScalarProvider {
	public Object unmarshal(String value, Object parent, String key);
	public String marshal(Object value, Object parent, String key);
}
