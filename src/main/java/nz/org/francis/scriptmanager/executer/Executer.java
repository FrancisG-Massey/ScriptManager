package nz.org.francis.scriptmanager.executer;

import nz.org.francis.scriptmanager.compiler.CompiledScript;
import nz.org.francis.scriptmanager.ScriptManager;
import nz.org.francis.scriptmanager.ApiFunction;

/**
 *
 * @author Francis
 */
public class Executer {	
	
	private static ScriptManager scriptManager;
	
	public static void setScriptManager (ScriptManager manager) {
		Executer.scriptManager = manager;
	}
	
	public static void execute (CompiledScript script) {
		boolean running = true;
		Context context = new Context(script);
		while (running) {
			executeInstr(context);
			running = context.nextInstr();//Returns false when there are no more instructions
		}
	}
	
	private static void executeInstr (Context context) {
		switch (context.getInstruction()) {
			case LOAD_INT_CONST:
				context.putInt(context.getIntConstant());
				break;
			case LOAD_STR_CONST:
				context.putObj((String) context.getConstant());
				break;
			case LOAD_LOCAL:
				context.putObj(context.localObjs[context.getIntConstant()]);
				break;
			case LOAD_INT_LOCAL:
				context.putInt(context.localInts[context.getIntConstant()]);
				break;
			case ASSIGN_LOCAL:
				context.localObjs[context.getIntConstant()] = context.getObj();
				break;
			case ASSIGN_LOCAL_INT:
				context.localInts[context.getIntConstant()] = context.getInt();
				break;
			case LOAD_GLOBAL:
			case ASSIGN_GLOBAL:
				throw new UnsupportedOperationException("Global variables are not yet supported.");
			case DROP_OBJ:
				context.getObj();
				break;
			case DROP_INT:
				context.getInt();
				break;
			case INVOKE_API:
				ApiFunction function = scriptManager.getApiFuncType(context.getIntConstant());
				if (function == null) {
					throw new UnsupportedOperationException("Unsupported function: "+context.getIntConstant());
				}
				scriptManager.invokeApiFunction(function, context);
				break;
			case INVOKE_SCRIPT:
				CompiledScript script = scriptManager.getById(context.getIntConstant());
				if (script == null) {
					throw new UnsupportedOperationException("Script not found: "+context.getIntConstant());
				}
				context.setInvokeScript(script);
				break;
			case RETURN:
				context.returnOneLevel();
				break;
			case JUMP:
				context.incrementPos(context.getInt());
				break;
			case JUMP_TRUE:
				if (context.getInt() == 1) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_FALSE:
				if (context.getInt() == 0) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_EQ:
				if (context.getInt() == context.getInt()) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_NT_EQ:
				if (context.getInt() != context.getInt()) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_GT:
				int val2 = context.getInt();
				int val1 = context.getInt();
				if (val1 > val2) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_LT:
				val2 = context.getInt();
				val1 = context.getInt();
				if (val1 < val2) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_GT_EQ:
				val2 = context.getInt();
				val1 = context.getInt();
				if (val1 >= val2) {
					context.incrementPos(context.getInt());
				}
				break;
			case JUMP_LT_EQ:
				val2 = context.getInt();
				val1 = context.getInt();
				if (val1 <= val2) {
					context.incrementPos(context.getInt());
				}
				break;
			case ADD:
				val2 = context.getInt();
				val1 = context.getInt();
				context.putInt(val1 + val2);
				break;
			case SUBTRACT:
				val2 = context.getInt();
				val1 = context.getInt();
				context.putInt(val1 - val2);
				break;
			case MULTIPLY:
				val2 = context.getInt();
				val1 = context.getInt();
				context.putInt(val1 * val2);
				break;
			case DIVIDE:
				val2 = context.getInt();
				val1 = context.getInt();
				context.putInt(val1 / val2);
				break;
			case POWER:
				val2 = context.getInt();
				val1 = context.getInt();
				context.putInt((int) Math.pow(val1, val2));
				break;
			case MODULO:
				val2 = context.getInt();
				val1 = context.getInt();
				context.putInt(val1 % val2);
				break;
		}
	}
}
