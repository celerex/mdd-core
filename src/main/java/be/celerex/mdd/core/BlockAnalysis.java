package be.celerex.mdd.core;

import java.util.List;

public class BlockAnalysis {
	private int depth, lineNumber;
	private String raw, content, codeType;
	private BlockType blockType;
	// for block type text
	private TextType textType;
	private List<String> keyValue;
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public BlockType getBlockType() {
		return blockType;
	}
	public void setBlockType(BlockType type) {
		this.blockType = type;
	}
	public List<String> getKeyValue() {
		return keyValue;
	}
	public void setKeyValue(List<String> keyValue) {
		this.keyValue = keyValue;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getRaw() {
		return raw;
	}
	public void setRaw(String raw) {
		this.raw = raw;
	}
	public String getCodeType() {
		return codeType;
	}
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
	public TextType getTextType() {
		return textType;
	}
	public void setTextType(TextType textType) {
		this.textType = textType;
	}
	@Override
	public String toString() {
		return "[" + blockType + (textType == null ? "" : "-" + textType) + "#" + depth + "] " + content;
	}
}