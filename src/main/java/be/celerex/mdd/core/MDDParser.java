package be.celerex.mdd.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

// need to switch to custom content because of meta annotations?
// do we absolutely need annotations though...?
public class MDDParser {
	
	@SuppressWarnings("unchecked")
	public Object parse(String content) throws MDDSyntaxException {
		if (content != null && !content.trim().isEmpty()) {
			List<BlockAnalysis> analyses = analyze(content);
			Stack<Object> stack = new Stack<Object>();
			// we always start at depth 0
			int depth = 0;
			// for example starting an element might generate an expectation of a depth
			// if that expection is not met, we must close the previous entry
			int expectedDepth = 0;

			boolean anonymousList = false;
			List<Object> scalars = new ArrayList<>();
			// it is a line-based approach
			for (int i = 0; i < analyses.size(); i++) {
				BlockAnalysis analysis = analyses.get(i);
				
				// we don't care about text
				if (analysis.getBlockType() == BlockType.TEXT) {
					continue;
				}
				
				// for each line we check if it is followed by multilines, this is added to the given value with the correct depth
				StringBuilder additionalContent = new StringBuilder();
				int x = i;
				while (x < analyses.size() - 1 && analyses.get(x + 1).getBlockType() == BlockType.MULTILINE) {
					x++;
					// we always add a linefeed, it is a multiline after all
					additionalContent.append("\n").append(analyses.get(x).getContent());
				}
				i = x;
				
				// if we have an expectation, check that it is met
				if (expectedDepth > 0) {
					if (analysis.getDepth() > expectedDepth) {
						// lists can get nested horribly
						if (analysis.getBlockType() != BlockType.LIST) {
							throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "Line exceeds expected depth");
						}
					}
					// close some items
					else if (analysis.getDepth() < expectedDepth) {
						int toPop = expectedDepth - analysis.getDepth();
						for (int j = toPop; j > 0; j--) {
							// we should never pop the last one
							// this means either the structure is incorrect or we are in an anonymous list
							if (stack.size() == 1) {
								boolean allowed = anonymousList && j == 0;
								if (!allowed) {
									throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "Invalid depth encountered: " + analysis.getDepth() + " != " + expectedDepth);			
								}
							}
							else {
								stack.pop();
							}
						}
						expectedDepth = analysis.getDepth();
					}
				}
				switch (analysis.getBlockType()) {
					// we don't care about text
					case TEXT: continue;
					case MULTILINE: throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "Invalid multiline detected");
					// an element must be added to the parent
					// if there is no direct parent, we are likely building an anonymous object
					case ELEMENT:
						if (stack.isEmpty()) {
							stack.push(new LinkedHashMap<String, Object>());
						}
						Object peekElement = stack.peek();
						if (!(peekElement instanceof Map)) {
							throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The element is not part of an object");
						}
						List<String> keyValue = analysis.getKeyValue();
						String elementKey = cleanupKey(analysis.getKeyValue().get(0));
						// we are starting a new element
						if (keyValue.size() == 1) {
							// if the next line is an element, we are building an object
							// if the next line is an array, we are building an array
							// if we can't find a next line, we just inject null
							expectedDepth = analysis.getDepth() + 1;
							if (i < analyses.size() - 1) {
								BlockAnalysis next = analyses.get(i + 1);
								if (next.getDepth() >= expectedDepth) {
									if (next.getBlockType() == BlockType.LIST) {
										List<Object> newList = new ArrayList<>();
										((Map<String, Object>) peekElement).put(elementKey, newList);
										stack.push(newList);
									}
									else if (next.getBlockType() == BlockType.ELEMENT) {
										LinkedHashMap<String, Object> newMap = new LinkedHashMap<String, Object>();
										((Map<String, Object>) peekElement).put(elementKey, newMap);
										stack.push(newMap);
									}
									else {
										throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The element is not followed by a list or object");			
									}
								}
								else {
									((Map<String, Object>) peekElement).put(elementKey, null);
								}
							}
							
						}
						// we have a value as well, set it immediately
						else {
							((Map<String, Object>) peekElement).put(elementKey, interpretScalar(keyValue.get(1) + additionalContent.toString()));
						}
					break;
					case LIST:
						// if we have a key AND a value, we have a named scalar list
						if (analysis.getKeyValue() != null && analysis.getKeyValue().size() == 2) {
							if (stack.isEmpty()) {
								stack.push(new LinkedHashMap<String, Object>());
							}
							Object peekList = stack.peek();
							if (!(peekList instanceof Map)) {
								throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The named list entry is not part of an object");
							}
							String key = cleanupKey(analysis.getKeyValue().get(0));
							Object existingList = ((Map<String, Object>) peekList).get(key);
							if (existingList == null) {
								existingList = new ArrayList<Object>();
								((Map<String, Object>) peekList).put(analysis.getKeyValue().get(0), existingList);
							}
							else if (!(existingList instanceof List)) {
								throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The named entry is not a list");
							}
							((List<Object>) existingList).add(interpretScalar(analysis.getKeyValue().get(1) + additionalContent.toString()));
						}
						// if we have a key only, we have a named complex entry
						else if (analysis.getKeyValue() != null && analysis.getKeyValue().size() == 1) {
							Object targetList;
							// if the key is anonymous, we are working in a list
							if (isAnonymous(analysis.getKeyValue().get(0))) {
								// new list
								if (stack.isEmpty()) {
									targetList = new ArrayList<Object>();
									stack.push(targetList);
									anonymousList = true;
								}
								// continuation of a list
								else if (stack.size() == 1 && stack.peek() instanceof List) {
									targetList = stack.peek();
								}
								// not supported?
								else {
									throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "Anonymous complex lists can only occur at the root");
								}
							}
							// we are working in a parent object with a named key
							else {
								// push an anonymous parent if needed
								if (stack.isEmpty()) {
									stack.push(new LinkedHashMap<String, Object>());
								}
								Object peekList = stack.peek();
								if (!(peekList instanceof Map)) {
									throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The named list entry is not part of an object");
								}
								String key = cleanupKey(analysis.getKeyValue().get(0));
								targetList = ((Map<String, Object>) peekList).get(key);
								if (targetList == null) {
									targetList = new ArrayList<Object>();
									((Map<String, Object>) peekList).put(key, targetList);
								}
								else if (!(targetList instanceof List)) {
									throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The named entry is not a list");
								}
							}
							Map<String, Object> newRecord = new LinkedHashMap<String, Object>();
							((List<Object>) targetList).add(newRecord);
							stack.push(newRecord);
							expectedDepth = analysis.getDepth() + 1;
						}
						// no key value, just a scalar list
						else if (analysis.getKeyValue() == null) {
							if (analysis.getContent().isEmpty()) {
								continue;
							}
							if (stack.isEmpty()) {
								stack.push(new ArrayList<Object>());
								expectedDepth = 1;
							}
							Object peekList = stack.peek();
							if (!(peekList instanceof List)) {
								throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "The scalar can not be added to a list");
							}
							for (int y = expectedDepth; y < analysis.getDepth(); y++) {
								ArrayList<Object> arrayList = new ArrayList<Object>();
								((List<Object>) peekList).add(arrayList);
								stack.push(arrayList);
								peekList = arrayList;
							}
							expectedDepth = analysis.getDepth();
							((List<Object>) peekList).add(interpretScalar(analysis.getContent() + additionalContent.toString()));
						}
					break;
					case SCALAR:
						scalars.add(interpretScalar(analysis.getContent() + additionalContent.toString()));
//						stack.push(interpretScalar(analysis.getContent()));
					break;
					case META:
						throw new MDDSyntaxException(analysis.getLineNumber(), analysis.getRaw(), "Meta entries are currently not supported");
				}
			}
			if (stack.isEmpty()) {
				if (scalars.size() == 1) {
					return scalars.get(0);
				}
				else if (scalars.size() > 1) {
					return scalars;
				}
				return null;
			}
			// return whatever is in the stack
			else {
				return stack.firstElement();
			}
		}
		return null;
	}
	
	private boolean isAnonymous(String key) {
		return key.startsWith("_") && key.endsWith("_");
	}
	
	// we can add _key_ to indicate that it is an anonymous key
	// we can add **key** just to make it pretty in markdown
	private static String cleanupKey(String key) {
		if (key.startsWith("_") && key.endsWith("_")) {
			return key.substring(1, key.length() - 1);
		}
		else if (key.startsWith("**") && key.endsWith("**")) {
			return key.substring(2, key.length() - 2);
		}
		return key;
	}
	
	private Object interpretScalar(String scalar) {
		// TODO: interpret stuff like numbers etc?
		return scalar;
	}
	
	/**
	 * Analyze a markdown text into relevant blocks
	 */
	public List<BlockAnalysis> analyze(String content) throws MDDSyntaxException {
		List<BlockAnalysis> analyses = new ArrayList<>();
		if (content != null && !content.trim().isEmpty()) {
			// it is inherently line based
			String[] split = content.split("\n");
			
			StringBuilder codeBuilder = null;
			StringBuilder rawBuilder = new StringBuilder();
			String codeType = null;
			int codeStartLine = 0;
			for (int i = 0; i < split.length; i++) {
				String line = split[i];
				
				if (!rawBuilder.isEmpty()) {
					rawBuilder.append("\n");
					rawBuilder.append(line);
				}
				
				// entering or exiting code block
				// code blocks are not interpreted, they are just combined into one block
				if (line.startsWith("```")) {
					// not in code yet, check if we have a type
					if (codeType == null) {
						codeType = line.substring(3).trim();
						if (codeType.isEmpty()) {
							codeType = "text";
						}
						codeBuilder = new StringBuilder();
						codeStartLine = i;
						continue;
					}
					else {
						BlockAnalysis analysis = new BlockAnalysis();
						analysis.setContent(codeBuilder.toString());
						analysis.setBlockType(BlockType.TEXT);
						analysis.setTextType(TextType.CODE);
						analysis.setRaw(rawBuilder.toString());
						analysis.setLineNumber(codeStartLine);
						analysis.setCodeType(codeType);
						analyses.add(analysis);
						rawBuilder = new StringBuilder();
						codeType = null;
						continue;
					}
				}
				// append to code block
				else if (codeType != null) {
					if (!codeBuilder.toString().isEmpty()) {
						codeBuilder.append("\n");
					}
					codeBuilder.append(line);
					continue;
				}
				
				// calculate the depth of a line
				int depth = depth(line);
				// strip the content to that depth
				String lineContent = content(line, depth, true);
				// attempt to extract a key and/or value out of it
				List<String> keyValue = getKeyValue(lineContent);
				
				// based on the leading character (if any), we take particular actions
				String trimmed = line.trim();
				BlockType blockType;
				TextType textType = null;
				if (trimmed.length() > 0) {
					char charAt = trimmed.charAt(0);
					
					// Every element must have a key, the value can be inline (scalar) or in the next line(s) (complex)
					if (charAt == '*' && keyValue == null) {
						throw new MDDSyntaxException(i, line, "The element is missing a key");
					}
					switch(charAt) {
						case '-': blockType = BlockType.LIST; break;
						case '*': blockType = BlockType.ELEMENT; break;
						// META is reserved for future use
						case '+': blockType = BlockType.META; break;
//						case '>': type = BlockType.COMMENT; break;
						default: 
							// at depth 0 we can still have element definitions if we have a key
							if (depth == 0 && keyValue != null && keyValue.size() == 1) {
								blockType = BlockType.ELEMENT;
							}
							// a value can be optionally there as well, that means we have a named scalar value
							else if (depth == 0 && keyValue != null && keyValue.size() == 2) {
								blockType = BlockType.ELEMENT;
							}
							else {
								// if we are not the first line, check if we are part of a multiline for the previous line
								if (!analyses.isEmpty()) {
									BlockAnalysis parentAnalysis = null;
									// find the first non-multiline parent
									for (int j = analyses.size() - 1; j >= 0; j--) {
										if (analyses.get(j).getBlockType() != BlockType.MULTILINE) {
											parentAnalysis = analyses.get(j);
											break;
										}
									}
									if (parentAnalysis == null) {
										blockType = BlockType.SCALAR;
										lineContent = line;
										depth = 0;
									}
									else {
										// it must be at least 1, but other than that it can be at the same depth as the parent
//										int expectedDepth = Math.max(parentAnalysis.getDepth(), 1);
										int expectedDepth = parentAnalysis.getDepth();
										// if we have at least as much depth, reset the depth to the expected so you can actually add leading spaces
										if (depth >= expectedDepth) {
											depth = expectedDepth;
											lineContent = content(line, depth, false);
											blockType = BlockType.MULTILINE;
										}
										else {
											blockType = BlockType.SCALAR;
										}
									}
								}
								else {
									blockType = BlockType.SCALAR;
									lineContent = line;
									depth = 0;
								}
							}
					}
				}
				// it is possible to have an empty line, it might be an actual empty line in a multiline string, to be syntactically correct it has to have at least as much depth as the parent it belongs to, but that is not analyzed here
				else {
					blockType = BlockType.TEXT;
					if (trimmed.startsWith("######")) {
						textType = TextType.H6;
					}
					else if (trimmed.startsWith("#####")) {
						textType = TextType.H5;
					}
					else if (trimmed.startsWith("####")) {
						textType = TextType.H4;
					}
					else if (trimmed.startsWith("###")) {
						textType = TextType.H3;
					}
					else if (trimmed.startsWith("##")) {
						textType = TextType.H2;
					}
					else if (trimmed.startsWith("#")) {
						textType = TextType.H1;
					}
					else if (trimmed.startsWith(">")) {
						textType = TextType.QUOTE;
					}
					else {
						textType = TextType.P;
					}
				}
				BlockAnalysis analysis = new BlockAnalysis();
				analysis.setContent(lineContent);
				analysis.setDepth(depth);
				analysis.setKeyValue(keyValue);
				analysis.setBlockType(blockType);
				analysis.setRaw(rawBuilder.toString());
				analysis.setLineNumber(i);
				analysis.setTextType(textType);
				analyses.add(analysis);
				rawBuilder = new StringBuilder();
			}
		}
		return analyses;
	}
	
	// we get the content of the line given the depth
	private static String content(String line, int depth, boolean allowLeadingWhitespace) {
		String content = line.substring(depth);
		// at greater depths, we allow for a SINGLE whitespace after the depth marker for readability
		// any whitespace beyond that is considered part of the value
		// at depth 0, there is no room for leading whitespace
		if (depth > 0 && content.length() > 0 && allowLeadingWhitespace) {
			char charAt = content.charAt(0);
			if (charAt == '\t' || charAt == ' ') {
				content = content.substring(1);
			}
		}
		return content;
	}
	
	private static int depth(String line) {
		// whether we encountered a syntactical character
		// we need to keep track of that because whitespace AFTER a syntactical element does not count
		boolean syntactical = false;
		for (int i = 0; i < line.length(); i++) {
			char charAt = line.charAt(i);
			boolean currentIsSyntactical = charAt == '-' || charAt == '+' || charAt == '*';
			syntactical |= currentIsSyntactical;
			boolean countsAsDepth =
				(!syntactical && (charAt == '\t' || charAt == ' '))
				|| currentIsSyntactical;
			// if the current character is not part of depth calculation, return the index as the depth counter
			if (!countsAsDepth) {
				return i;
			}
		}
		// if we encountered a syntactical element, the full line is the depth!
		// mostly useful for complex array layout
		return syntactical ? line.length() : 0;
	}
	
	private static List<String> getKeyValue(String line) {
		int index = 0;
		// we need to find an unescaped ":"
		while ((index = line.indexOf(':', index)) >= 0) {
			boolean escaped = false;
			// we need to check the characters before and count the backslashes
			for (int i = index - 1; i >= 0; i--) {
				char charAt = line.charAt(i);
				if (charAt == '\\') {
					escaped = !escaped;
				}
				else {
					break;
				}
			}
			if (!escaped) {
				String key = unescape(line.substring(0, index));
				// if it's not a valid key, it is not a valid key-value
				if (!isValidKey(key)) {
					return null;
				}
				String value = unescape(line.substring(index + 1));
				// we allow for exact one whitespace character for prettification, the rest is considered part of the value
				if (!value.isEmpty()) {
					char charAt = value.charAt(0);
					if (charAt == '\t' || charAt == ' ') {
						value = value.substring(1);
					}
				}
				if (value.isEmpty()) {
					return List.of(key);
				}
				else {
					return List.of(key, value);
				}
			}
		}
		return null;
	}
	
	private static boolean isValidKey(String key) {
		key = cleanupKey(key.trim());
		return key.trim().matches("[\\w-]+");
	}
	
	private static String unescape(String escaped) {
		// make sure escaped backslashes are not used for other reasons
		escaped = escaped.replace("\\\\", "::escaped-backslash::");
		escaped = escaped.replaceAll("\\:", ":");
		escaped = escaped.replace("::escaped-backslash::", "\\");
		return escaped;
	}
	
	public static class MDDSyntaxException extends Exception {
		private static final long serialVersionUID = 1L;
		private int lineNumber;
		private String line;

		public MDDSyntaxException(int lineNumber, String line, String message) {
			super("[" + lineNumber + "] " + message);
			this.lineNumber = lineNumber;
			this.line = line;
		}
		public int getLineNumber() {
			return lineNumber;
		}
		public String getLine() {
			return line;
		}
	}
}
