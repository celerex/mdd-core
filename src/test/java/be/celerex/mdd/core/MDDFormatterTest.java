package be.celerex.mdd.core;

import junit.framework.TestCase;

public class MDDFormatterTest extends TestCase {
	public void testObjectListFormatter() throws MDDFormatException, MDDSyntaxException {
		String content = "- book:\n"
				+ "	* author: john\n"
				+ "	* isbn: 1234\n"
				+ "- book:\n"
				+ "	* author: bob\n"
				+ "	* isbn: 2345";
		Object parsed = new MDDParser().parse(content);
		String formatted = new MDDFormatter().format(parsed);
		// note that the formatted contains a trailing linefeed, hence the trim
		assertEquals(content, formatted.trim());
	}
	
	public void testComplexListFormatter() throws MDDFormatException, MDDSyntaxException {
		String content = "-\n"
				+ "	-\n"
				+ "		-\n"
				+ "			- test1\n"
				+ "			- test2\n"
				+ "			- test3\n"
				+ "	-\n"
				+ "		-\n"
				+ "			- test4\n"
				+ "			- test5\n"
				+ "			- test6\n"
				+ "	-\n"
				+ "		-\n"
				+ "			- test7\n"
				+ "			- test8\n"
				+ "				and more!\n"
				+ "			- test9\n"
				+ "	-\n"
				+ "		-\n"
				+ "			-\n"
				+ "				- test10\n"
				+ "					- test11\n"
				+ "						with additional content\n"
				+ "				- test12\n"
				+ "					- test13";
		Object parsed = new MDDParser().parse(content);
		String formatted = new MDDFormatter().format(parsed);
		
		// note that the formatted contains a trailing linefeed, hence the trim
		assertEquals(content, formatted.trim());
	}
	
	public void testSimpleScalarFormatter() throws MDDSyntaxException, MDDFormatException {
		String content = "This is a sentence\n"
				+ "with a linefeed in it\n"
				+ "another linefeed\n"
				+ "     and some leading spaces!\n"
				+ "and some trailing content as well";
		Object parsed = new MDDParser().parse(content);
		String formatted = new MDDFormatter().format(parsed);
		assertEquals(content, formatted.trim());
	}
}
