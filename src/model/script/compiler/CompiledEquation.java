package model.script.compiler;

import java.util.List;
import java.util.Map;
import model.script.Script;

/**
 *
 * @author Francis
 */
public class CompiledEquation implements Script {
    
    public static CompiledEquation compile (String equation) throws ParserException {
        ScriptParser parser = ScriptParser.parseBlock("{"+equation+"}");
        CompiledEquation compiledEquation = new CompiledEquation();
        compiledEquation.instructions = parser.getInstructions();
        compiledEquation.constants = parser.getConstants();
        return compiledEquation;
    }
    
    private List<ScriptOpcode> instructions;
    private Map<Integer, Object> constants;
    
    @Override
    public ScriptOpcode getInstruction (int pos) {
        return instructions.get(pos);
    }
    
    @Override
    public Object getConstant (int pos) {
        return constants.get(pos);
    }

    @Override
    public int getInstructionCount() {
        return instructions.size();
    }

    @Override
    public int getIntLocalCount() {
        return 0;
    }

    @Override
    public int getObjLocalCount() {
        return 0;
    }
}
