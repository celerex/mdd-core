package be.celerex.mdd.core;

public class MDDSyntaxException extends Exception {
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