package nz.org.francis.scriptmanager;

/**
 *
 * @author Francis
 */
public class CompilationException extends Exception {
    
    public CompilationException() {
        super();
    }
	
	public CompilationException (String message) {
		super(message);
	}
	
	public CompilationException (String message, Throwable cause) {
		super(message, cause);
	}
    
}
