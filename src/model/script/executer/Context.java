package model.script.executer;

import model.script.ScriptDataType;
import model.script.compiler.ScriptOpcode;
import java.util.Stack;

/**
 *
 * @author Francis
 */
public class Context {
	
	private static class InvokeFrame {
		CompiledScript script;
		int instrPtr;
		int[] localInts;
		Object[] localObjs;
	}
	
	private CompiledScript script;
	public int instrPtr;
	
	protected int[] localInts;
	protected Object[] localObjs;
	
	private final Stack<Integer> intStack = new Stack<>();
	private final Stack<Object> objStack = new Stack<>();
	
	private final Stack<InvokeFrame> invokeStack = new Stack<>();
	
	public Context (CompiledScript script) {
		this.script = script;
		this.localInts = new int[script.intLocalCount];
		this.localObjs = new Object[script.objLocalCount];
	}
	
	public ScriptOpcode getInstruction () {
		return script.instructions[instrPtr];
	}
	
	public int getIntConstant () {
		return (int) script.constants.get(instrPtr);
	}
	
	public Object getConstant () {
		return script.constants.get(instrPtr);
	}
	
	public boolean nextInstr () {
		instrPtr++;
		return script.instructions.length > instrPtr;
	}
	
	public void incrementPos (int pos) {
		instrPtr += pos;
	}
	
	public void setPos (int pos) {
		instrPtr = pos;
	}
	
	public CompiledScript getScript () {
		return script;
	}
	
	public void setInvokeScript (CompiledScript script) {
		InvokeFrame frame = new InvokeFrame();
		frame.script = script;
		frame.instrPtr = instrPtr;
		frame.localInts = localInts;
		frame.localObjs = localObjs;
		invokeStack.push(frame);
		this.script = script;
		this.instrPtr = 0;
		this.localInts = new int[script.intLocalCount];
		this.localObjs = new Object[script.objLocalCount];
		int intPos = 0;
		int objPos = 0;
		for (ScriptDataType param : script.params) {
			if (param.intBase()) {
				this.localInts[intPos++] = getInt();
			} else {
				this.localObjs[objPos++] = getObj();
			}
		}
	}
	
	public void returnOneLevel () {
		if (invokeStack.isEmpty()) {
			instrPtr = script.instructions.length;
		} else {
			InvokeFrame frame = invokeStack.pop();
			this.script = frame.script;
			this.instrPtr = frame.instrPtr;
			this.localInts = frame.localInts;
			this.localObjs = frame.localObjs;
		}
	}
	
	public int getIntLocal (int id) {
		return localInts[id];
	}
	
	public void setIntLocal (int id, int value) {
		localInts[id] = value;
	}
	
	public int getInt () {
		return intStack.pop();
	}
	
	public void putInt (int value) {
		intStack.push(value);
	}
	
	public Object getObj () {
		return objStack.pop();
	}
	
	public void putObj (Object value) {
		objStack.push(value);
	}
}
