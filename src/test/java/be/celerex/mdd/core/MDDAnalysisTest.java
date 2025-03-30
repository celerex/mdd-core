package be.celerex.mdd.core;

import java.util.List;
import static be.celerex.mdd.core.TextType.*;

import junit.framework.TestCase;

public class MDDAnalysisTest extends TestCase {
	public void testAnalyze() throws MDDSyntaxException {
		String content = MDDParserTest.readFile("examples/story.md");
		List<BlockAnalysis> analyses = new MDDParser().analyze(content);
		List<TextType> textTypes = analyses.stream()
			.filter(a -> a.getBlockType() == BlockType.SCALAR)
			.map(a -> a.getTextType())
			.toList();
		assertEquals(List.of(H1, P, P, P, P, TABLE), textTypes);
	}
}
