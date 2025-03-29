package be.celerex.mdd.core;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import be.celerex.mdd.api.CollectionProvider;
import be.celerex.mdd.api.ObjectProvider;
import be.celerex.mdd.api.ScalarProvider;

public class MDDFormatter {
	
	@SuppressWarnings("rawtypes")
	private CollectionProvider collectionProvider = new STLCollectionProvider();
	@SuppressWarnings("rawtypes")
	private ObjectProvider objectProvider = new STLObjectProvider();
	
	private ScalarProvider scalarProvider = new StandardScalarProvider();
	
	public String format(Object object) throws MDDFormatException {
		StringWriter writer = new StringWriter();
		format(object, writer, collectionProvider.isCollection(object) ? 1 : 0, null, null);
		return writer.toString();
	}
	
	private void inject(Writer builder, String string, int depth) throws IOException {
		for (int i = 0; i < depth - 1; i++) {
			builder.write("\t");
		}
		builder.write(string);
	}
	
	@SuppressWarnings("unchecked")
	private void format(Object object, Writer builder, int depth, Object parent, String parentName) throws MDDFormatException {
		boolean explodeDashes = true;
		try {
			// if we have a collection, format as such
			if (collectionProvider.isCollection(object)) {
				Object previousElement = null;
				for (Object single : collectionProvider.list(object)) {
					// for a nested collection, we recurse
					if (collectionProvider.isCollection(single)) {
						// if we explode the dashes, we always want dashes at every level
						if (explodeDashes && (previousElement == null || collectionProvider.isCollection(previousElement))) {
							inject(builder, "-", depth);
							builder.write("\n");
						}
						// if we just did another nested collection, make sure we add a hard breaking dash
						else if (!explodeDashes && previousElement != null && collectionProvider.isCollection(previousElement)) {
							inject(builder, "-", depth);
							builder.write("\n");
						}
						format(single, builder, depth + 1, single, null);
						previousElement = single;
					}
					else {
						previousElement = single;
						// add a dash for list syntax
						inject(builder, "-", depth);
						if (objectProvider.isObject(single)) {
							if (parentName == null) {
								throw new MDDFormatException("No parent name found for object list");
							}
							builder.append(" " + parentName + ":\n");
							format(single, builder, depth + 1, parent, parentName);
						}
						else if (single != null) {
							// readability
							builder.append(" ");
							// we increase depth with 1 because the list itself adds 1
							formatSingle(single, builder, depth + 1, parent, parentName);
							builder.write("\n");
						}
					}
				}
			}
			else if (objectProvider.isObject(object)) {
				List<String> keys = objectProvider.keys(object);
				for (String key : keys) {
					Object value = objectProvider.get(object, key);
					// if we have a list, we don't want to write the key here, but instead in the list
					if (collectionProvider.isCollection(value)) {
						format(value, builder, depth + 1, object, key);
					}
					else {
						inject(builder, "*", depth);
						builder.write(" " + key + ":");
						// for collections and objects, we start a new line
						if (objectProvider.isObject(value)) {
							builder.write("\n");
							// and recurse
							format(value, builder, depth + 1, object, key);
						}
						else {
							// for a simple value, add a space for readability
							builder.write(" ");
							formatSingle(value, builder, depth, object, key);
						}
						builder.write("\n");
					}
				}
			}
			else {
				formatSingle(object, builder, depth, parent, parentName);
			}
		}
		catch (IOException e) {
			throw new MDDFormatException(e);
		}
	}

	private void formatSingle(Object single, Writer builder, int depth, Object parent, String key) throws IOException {
		String marshalled = scalarProvider.marshal(single, parent, key);
		boolean first = true;
		for (String line : marshalled.split("\n")) {
			if (first) {
				first = false;
			}
			else {
				builder.write("\n");
				// need to add whitespace up until the depth we are at
				inject(builder, "", depth);
			}
			builder.write(line);
		}
	}

	public CollectionProvider<?> getCollectionProvider() {
		return collectionProvider;
	}

	public void setCollectionProvider(CollectionProvider<?> collectionProvider) {
		this.collectionProvider = collectionProvider;
	}

	public ObjectProvider<?> getObjectProvider() {
		return objectProvider;
	}

	public void setObjectProvider(ObjectProvider<?> objectProvider) {
		this.objectProvider = objectProvider;
	}

	public ScalarProvider getScalarProvider() {
		return scalarProvider;
	}

	public void setScalarProvider(ScalarProvider scalarProvider) {
		this.scalarProvider = scalarProvider;
	}
	
}
