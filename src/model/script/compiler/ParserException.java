package model.script.compiler;

/**
 *
 * @author Francis
 */
public class ParserException extends Exception {
	
	
    public ParserException() {
        super();
    }
	
	public ParserException (String message) {
		super(message);
	}
	
	public ParserException (String message, int lineNum) {
		super(message+" at line "+lineNum);
	}
	
	public ParserException (String message, Throwable cause) {
		super(message, cause);
	}
	
}
