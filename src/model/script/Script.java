package model.script;

import model.script.compiler.ScriptOpcode;

/**
 *
 * @author Francis
 */
public interface Script {
    
    public ScriptOpcode getInstruction (int pos);
    
    public int getInstructionCount ();
    
    public Object getConstant (int pos);
    
    public int getIntLocalCount ();
    
    public int getObjLocalCount ();
}
