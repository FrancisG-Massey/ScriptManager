package nz.org.francis.scriptmanager;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Francis
 */
public interface Compiler {
    
    
    public Script compileEquation (String equation) throws CompilationException;
    
    public Script compileScriptFromFile (File source) throws CompilationException, IOException;
    
    public Script compileScriptFromString (String source) throws CompilationException;
}
