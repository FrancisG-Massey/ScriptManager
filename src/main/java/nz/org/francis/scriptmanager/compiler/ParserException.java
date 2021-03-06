package nz.org.francis.scriptmanager.compiler;

import nz.org.francis.scriptmanager.CompilationException;

/**
 *
 * @author Francis
 */
public class ParserException extends CompilationException {
	
	
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
