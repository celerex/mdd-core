package be.celerex.mdd.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MDDParserTest extends TestCase {
	
	public void testFile(String file) throws MDDSyntaxException {
		String content = readFile(file);
		MDDParser parser = new MDDParser();
		parser.setMetaProvider(new STLMetaProvider());
		List<BlockAnalysis> analyses = parser.analyze(content);
		String mdd = null;
		Object expected = null;
		int counter = 0;
		for (BlockAnalysis analysis : analyses) {
			if (analysis.getBlockType() == BlockType.TEXT && TextType.CODE.equals(analysis.getTextType())) {
				if (analysis.getCodeType().equals("mdd")) {
					mdd = analysis.getContent();
				}
				else if (analysis.getCodeType().equals("expected")) {
					expected = analysis.getContent();
				}
				else if (analysis.getCodeType().equals("json")) {
					expected = parseJson(analysis.getContent());
				}
			}
			if (mdd != null && expected != null) {
				counter++;
				if (expected instanceof String) {
					assertEquals(expected, parser.parse(mdd).toString());
				}
				else {
					assertEquals(expected, parser.parse(mdd));
				}
				mdd = null;
				expected = null;
			}
		}
		// we found an mdd without an expectation, print it
		if (mdd != null) {
			System.out.println(parser.analyze(mdd));
			System.out.println(parser.parse(mdd));
		}
		else if (counter == 0) {
			throw new IllegalArgumentException("No mdd found in: " + file);
		}
	}

	public static String readFile(String file) {
		String content;
		try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
			byte [] bytes = new byte[1024];
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int read = 0;
			while ((read = input.read(bytes)) > 0) {
				output.write(bytes, 0, read);
			}
			content = new String(output.toByteArray());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return content;
	}
	
	@SuppressWarnings("unchecked")
	private Object parseJson(String content) {
		try { 
			ObjectMapper objectMapper = new ObjectMapper();
			if (content.startsWith("{")) {
				Map<String,Object> result = objectMapper.readValue(content.getBytes(), HashMap.class);
				return result;
			}
			else if (content.startsWith("[")) {
				List<Object> result = objectMapper.readValue(content.getBytes(), ArrayList.class);
				return result;
			}
			else {
				throw new IllegalArgumentException("Currently not supported as JSON: " + content);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void testFiles() throws MDDSyntaxException {
		testFile("examples/nested-collections.md");
		testFile("examples/scalars.md");
		testFile("examples/objects.md");
	}
	
	public void testConfiguration() throws MDDSyntaxException {
		String content = readFile("examples/configuration.md");
		Object parsed = new MDDParser().parse(content);
		assertEquals(
			"{http={port=8080, ssl=false, listener=[{class=com.example.Listener1, phase=early}, {class=com.example.Listener2, phase=late}, {class=com.example.Listener3, phase=early}]}, "
				+ "mail={port=8025, starttls=true}, logLevel=INFO, development=false, name=example-server, clustered=true, count=[1, 2], final=[3]}",
			parsed.toString()
		);
	}
	
	public void testBasicScalars() throws MDDSyntaxException {
		MDDParser parser = new MDDParser();
		assertEquals("5", parser.parse("5"));
		assertEquals("true", parser.parse("true"));
		
		String content = "multiline\n"
			+ "value";
		
		// it should remain a singular value
		assertEquals(content, parser.parse(content));
		
		content = "	- test1\n"
				+ "	- test2";
		assertEquals("[[test1, test2]]", parser.parse(content).toString());
	}
	
	public void testComplexRoots() throws MDDSyntaxException {
		MDDParser parser = new MDDParser();
		
		String content = "- name: john\n"
				+ "- age: 30";
		assertEquals("{name=[john], age=[30]}", parser.parse(content).toString());
		
		content = "- name: john\n"
				+ "- name: jim\n"
				+ "- age: 30\n"
				+ "- age: 31";
		assertEquals("{name=[john, jim], age=[30, 31]}", parser.parse(content).toString());
		
		content = "* name: john\n"
				+ "* age: 30";
		assertEquals("{name=john, age=30}", parser.parse(content).toString());

		content = "- _user_:\n"
				+ "	* name: john\n"
				+ "	* age: 30\n"
				+ "- _user_:\n"
				+ "	* name: jim\n"
				+ "	* age: 40";
		assertEquals("[{name=john, age=30}, {name=jim, age=40}]", parser.parse(content).toString());
		
		content = "- user:\n"
				+ "	* name: john\n"
				+ "	* age: 30\n"
				+ "- user:\n"
				+ "	* name: jim\n"
				+ "	* age: 40";
		
		assertEquals("{user=[{name=john, age=30}, {name=jim, age=40}]}", parser.parse(content).toString());
		
		content = "book:\n"
				+ "* author: john\n"
				+ "* isbn: 51234\n"
				+ "- rating:\n"
				+ "	* user: jim\n"
				+ "	* score: 5\n"
				+ "- rating:\n"
				+ "	* user: bob\n"
				+ "	* score: 3";
		
		assertEquals("{book={author=john, isbn=51234, rating=[{user=jim, score=5}, {user=bob, score=3}]}}", parser.parse(content).toString());
		
		content = "book: 5";
		assertEquals("{book=5}", parser.parse(content).toString());
	}
	
	public void testScalarParsing() throws MDDSyntaxException {
		String content = "5";
		MDDParser parser = new MDDParser();
		parser.setScalarProvider(new ParsingScalarProvider());
		Object object = parser.parse(content);
		assertEquals(BigInteger.class, object.getClass());
		
		content = "true";
		object = parser.parse(content);
		assertEquals(Boolean.class, object.getClass());
		content = "- 1\n"
				+ "- 2\n"
				+ "- 3";
		object = parser.parse(content);
		assertEquals(List.of(
			BigInteger.valueOf(1), 
			BigInteger.valueOf(2), 
			BigInteger.valueOf(3)
		), object);
	}
}
