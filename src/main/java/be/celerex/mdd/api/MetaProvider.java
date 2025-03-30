package be.celerex.mdd.api;

// meta data works only on objects so it should be compatible with the type of the object provider you use
public interface MetaProvider<O, M> extends ObjectProvider<M> {
	public void setMetadata(O object, M metadata);
	public M getMetadata(O object);
}
