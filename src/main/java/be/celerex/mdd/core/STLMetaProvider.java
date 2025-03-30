package be.celerex.mdd.core;

import java.util.Map;

import be.celerex.mdd.api.MetaProvider;

public class STLMetaProvider extends STLObjectProvider implements MetaProvider<Map<String, Object>, Map<String, Object>> {

	private String metaKey = "$meta";
	
	@Override
	public void setMetadata(Map<String, Object> object, Map<String, Object> metadata) {
		object.put(metaKey, metadata);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getMetadata(Map<String, Object> object) {
		return (Map<String, Object>) object.get("$meta");
	}

	public String getMetaKey() {
		return metaKey;
	}

	public void setMetaKey(String metaKey) {
		this.metaKey = metaKey;
	}

}
