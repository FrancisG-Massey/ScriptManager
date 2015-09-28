package model.script.compiler;

import model.script.ScriptDataType;
import model.script.ApiFunction;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author Francis
 */
public class ScriptParser {
	
	public static ScriptParser parseSource (String source) throws ParserException {
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(source));
		ScriptParser parser = new ScriptParser(scanner, new Linker());
		parser.parseHeader();
		return parser;
	}
	
	private List<ScriptDataType> paramSignature;
	
	private List<ScriptDataType> returnSignature;
	
	protected final List<ScriptOpcode> instructions = new ArrayList<>();
	protected final Map<Integer, Object> constants = new HashMap<>();
	
	protected final Map<String, LocalVariable> localVars = new HashMap<>();
	
	private int localIntVarCounter = 0;
	private int localVarCounter = 0;
	
	protected class LocalVariable {
		
		private final ScriptDataType type;
		private final String name;
		private final int id;
		
		private LocalVariable (String name, ScriptDataType type) {
			this.name = name;
			this.type = type;
			this.id = type.intBase() ? localIntVarCounter++ : localVarCounter++;
		}
		
		protected int getID () {
			return id;
		}
		
		protected String getName () {
			return name;
		}
		
		protected ScriptDataType getDataType () {
			return type;
		}
	}
	
	private SourceScanner scanner;
	private final Linker linker;
	
	private final Stack<ScriptDataType> stack = new Stack<>();
	
	private String eventType;
	private String eventBinding;
	
	protected ScriptParser (Linker linker) {
		this.linker = linker;		
	}
	
	protected ScriptParser (SourceScanner scanner, Linker linker) throws ParserException {
		this.scanner = scanner;
		this.scanner.nextToken();
		this.linker = linker;
	}
	
	protected void clearScript () {
		paramSignature = null;
		returnSignature = null;
		instructions.clear();
		constants.clear();
		localVars.clear();
		localIntVarCounter = 0;
		localVarCounter = 0;
		stack.clear();
		eventType = null;
		eventBinding = null;
	}
	
	public List<ScriptDataType> getParamSignature () {
		return paramSignature;
	}
	
	public List<ScriptDataType> getReturnSignature () {
		return returnSignature;
	}
	
	public List<ScriptOpcode> getInstructions () {
		return instructions;
	}
	
	public String getBinding () {
		return eventBinding;
	}
	
	protected void parseHeader () throws ParserException {
		paramSignature = new ArrayList<>();
		returnSignature = new ArrayList<>();
		
		accept(Token.LBRACKET);//Start of event binding
		
		Token token = scanner.getToken();//Event type
		if (token != Token.IDENTIFIER) {
			throw new ParserException("Unexpected token. Expected identifier");
		}
		eventType = scanner.getStringValue();
		scanner.nextToken();
		
		accept(Token.COMMA);//Separator
		
		token = scanner.getToken();//Event binding
		if (token != Token.IDENTIFIER) {
			throw new ParserException("Unexpected token. Expected identifier");
		}
		eventBinding = scanner.getStringValue();
		scanner.nextToken();
		
		accept(Token.RBRACKET);//End of event binding
		
		accept(Token.LPAREN);//Start of paramaters
		
		parseParameters();
		
		accept(Token.RPAREN);//End of paramaters
		
		accept(Token.LPAREN);//Start of return type
		
		parseReturnTypes();
		
		accept(Token.RPAREN);//End of return types
	}
	
	protected void skipBody () throws ParserException {
		accept(Token.LBRACE);
		Token token = scanner.getToken();
		while (token != Token.RBRACE) {
			if (token == Token.LBRACE) {
				skipBody();
			}
			token = scanner.nextToken();
		}
		accept(Token.RBRACE);
	}
	
	private void parseReturnTypes () throws ParserException {
		Token token = scanner.getToken();
		while (token != Token.RPAREN) {
			token = scanner.getToken();
			if (token != Token.IDENTIFIER) {
				throw new ParserException("Unexpected token. Expected identifier");
			}
			String identifier = scanner.getStringValue();
			if (!isDataType(identifier)) {
				throw new ParserException("Unexpected token. Expected DataType");
			}
			ScriptDataType dataType = getDataType(identifier);
			if (dataType == null) {
				throw new ParserException("Unsupported DataType: "+identifier);
			}
			returnSignature.add(dataType);
			
			token = scanner.nextToken();
			if (token != Token.RPAREN) {				
				accept(Token.COMMA);
			}
		}
	}
	
	private void parseParameters () throws ParserException {
		Token token = scanner.getToken();
		while (token != Token.RPAREN) {
			token = scanner.getToken();
			if (token != Token.IDENTIFIER) {
				throw new ParserException("Unexpected token. Expected identifier");
			}
			String identifier = scanner.getStringValue();
			if (!isDataType(identifier)) {
				throw new ParserException("Unexpected token. Expected DataType");
			}
			ScriptDataType dataType = getDataType(identifier);
			if (dataType == null) {
				throw new ParserException("Unsupported DataType: "+identifier);
			}
			
			token = scanner.nextToken();
			if (token != Token.IDENTIFIER) {
				throw new ParserException("Unexpected token. Expected identifier");
			}
			identifier = scanner.getStringValue();
			if (!isLocalVariable(identifier)) {
				throw new ParserException("Invalid local variable name: "+identifier);
			}
			
			localVars.put(identifier, new LocalVariable(identifier, dataType));
			paramSignature.add(dataType);
			
			token = scanner.nextToken();
			if (token != Token.RPAREN) {				
				accept(Token.COMMA);
			}
		}
	}
	
	private boolean isDataType (String identifier) {
		if (identifier.isEmpty()) {
			return false;
		}
		return Character.isUpperCase(identifier.charAt(0));
	}
	
	private boolean isLocalVariable (String identifier) {
		if (identifier.isEmpty()) {
			return false;
		}
		return identifier.startsWith("$");
	}
	
	private boolean isGlobalVariable (String identifier) {
		if (identifier.isEmpty()) {
			return false;
		}
		return identifier.startsWith("%");
	}
	
	private boolean isScriptCall (String identifier) {
		if (identifier.isEmpty()) {
			return false;
		}
		return identifier.startsWith("~");
	}
	
	private boolean isApiCall (String identifier) {
		if (identifier.isEmpty()) {
			return false;
		}
		return Character.isLowerCase(identifier.charAt(0));
	}
	
	private ScriptDataType getDataType (String identifier) {
		return ScriptDataType.forName(identifier);
	}
	
	private void setConst (int instrId, Object value) {
		constants.put(instrId-1, value);
	}
	
	private void checkStack () throws ParserException {
		if (!stack.isEmpty()) {
			throw new ParserException("Non-empty stack.", scanner.getLineNum());
		}
	}
	
	private void accept (Token token) throws ParserException {
		if (scanner.getToken() == token) {
			scanner.nextToken();
		} else {
			throw new ParserException("Unexpected token: "+scanner.getToken()+". Expected '"+token.getName()+"'", scanner.getLineNum());
		}
	}
	
	protected void setSource (String source) throws ParserException {
		this.scanner = new SourceScanner(CharBuffer.wrap(source));
		this.scanner.nextToken();
	}
	
	protected void parseBody () throws ParserException {
		accept(Token.LBRACE);
		parseBlock();
		accept(Token.RBRACE);
		/*Instr instr;
		while (scanner.getToken() != Token.EOF) {
			switch (scanner.getToken()) {
				case IDENTIFIER:
					String identifier = scanner.getStringValue();
					char prefix = identifier.charAt(0);
					if (Character.isUpperCase(prefix)) {//Type identifier
						if (!instrStack.isEmpty()) {
							throw new ParserException("Unexpected variable definition! Var defs must be on a new line");
						}
						ScriptDataType dataType = ScriptDataType.valueOf(identifier);
						if (dataType == null) {
							throw new ParserException("Unsupported data type: "+identifier);
						}
						instr = new Instr(Token.IDENTIFIER, InstrType.DEF_VAR);
						instr.dataType = dataType;
						instrStack.push(instr);
					} else if (prefix == '$') {//Local variable
						String name = scanner.getStringValue().substring(1);
						LocalVar var;
						if (!instrStack.isEmpty() && instrStack.peek().type == InstrType.DEF_VAR) {
							//Declare new variable
							if (localVars.containsKey(name)) {
								throw new ParserException("Variable "+name+" is already defined!");
							}
							instr = instrStack.pop();
							var = new LocalVar(name, instr.dataType);
							localVars.put(name, var);
						} else {
							var = localVars.get(name);
							if (var == null) {
								throw new ParserException("Variable "+name+" must be defined before it is used!");
							}
						}
							
						instr = new Instr(Token.IDENTIFIER, InstrType.USE_VAR);
						instr.value = var.id;
						instr.dataType = var.type;
					}
					break;
			}
			scanner.nextToken();
		}*/
	}
	
	/**
	 * Parses an "if-then-else" block
	 * @throws ParserException If the block contains invalid code
	 */
	protected void parseConditionalBlock () throws ParserException {
		accept(Token.IF);//Start of conditional statement
		accept(Token.LPAREN);//Start of condition
		List<Integer> fPoses = new ArrayList<>();
		List<Integer> tPoses = new ArrayList<>();
		parseConditionalStatement(fPoses, tPoses);
		accept(Token.RPAREN);//End of condition
		for (Integer fPos : fPoses) {
			setConst(fPos, instructions.size()-fPos);
		}
		instructions.add(ScriptOpcode.JUMP);
		int jumpPosition = instructions.size();
		for (Integer tPos : tPoses) {
			setConst(tPos, instructions.size()-tPos);
		}
		accept(Token.LBRACE);//Start of body
		parseBlock();
		accept(Token.RBRACE);//End of body
		
		Token token = scanner.getToken();
		if (token == Token.ELSE) {
			token = scanner.nextToken();
			instructions.add(ScriptOpcode.JUMP);//End of the "if" block
			int endIfPos = instructions.size();
			
			setConst(jumpPosition, instructions.size()-jumpPosition);//The false condition should jump to the "else"
			
			if (token == Token.IF) {
				parseConditionalBlock();//Recursively call the method for every "else if" block
			} else {//Otherwise, this is an unconditional "else" block, so we'll just parse it
				accept(Token.LBRACE);//Start of body
				parseBlock();
				accept(Token.RBRACE);//End of body
			}
			setConst(endIfPos, instructions.size()-endIfPos);//The end of the "if" condition should jump to the end of all "else" blocks
		} else {
			setConst(jumpPosition, instructions.size()-jumpPosition);//Finish the jump for a false condition
		}
	}
	
	protected void parseWhileLoop () throws ParserException {
		accept(Token.WHILE);//Start of conditional statement
		accept(Token.LPAREN);//Start of condition
		int loopStart = instructions.size();
		List<Integer> fPoses = new ArrayList<>();
		List<Integer> tPoses = new ArrayList<>();
		parseConditionalStatement(fPoses, tPoses);
		accept(Token.RPAREN);//End of condition
		for (Integer fPos : fPoses) {
			setConst(fPos, instructions.size()-fPos);
		}
		instructions.add(ScriptOpcode.JUMP);
		int jumpPosition = instructions.size();
		for (Integer tPos : tPoses) {
			setConst(tPos, instructions.size()-tPos);
		}
		accept(Token.LBRACE);//Start of body
		List<Integer> breaks = new ArrayList<>();
		List<Integer> continues = new ArrayList<>();
		parseLoopBlock(breaks, continues);
		accept(Token.RBRACE);//End of body
		instructions.add(ScriptOpcode.JUMP);//End of the loop body
		setConst(instructions.size(), -(instructions.size()-loopStart));//Jump back to the start
		setConst(jumpPosition, instructions.size()-jumpPosition);
	}
	
	private void parseLoopBlock (List<Integer> breaks, List<Integer> continues) throws ParserException {
		Token token = scanner.getToken();
		while (token != Token.RBRACE) {
			if (token == Token.BREAK) {
				
			} else if (token == Token.CONTINUE) {
				
			} else {
				parseBlockStatement();
			}
			checkStack();//The stack should always be empty at the end of the line
			token = scanner.getToken();
		}
	}
	
	/**
	 * Parses a block of instructions, terminated with a right brace ('}')
	 * @throws ParserException If the block contains invalid code
	 */
	protected void parseBlock () throws ParserException {
		Token token = scanner.getToken();
		while (token != Token.RBRACE) {
			parseBlockStatement();
			checkStack();//The stack should always be empty at the end of the line
			token = scanner.getToken();
		}
	}
	
	/**
	 * Parses a statement within a block
	 * @throws ParserException If the statement contains invalid code
	 */
	protected void parseBlockStatement () throws ParserException {
		Token token = scanner.getToken();
		switch (token) {
			case IF:
				parseConditionalBlock();
				break;
			case WHILE:
				parseWhileLoop();
				break;
			case RETURN:
				parseParams(Token.SEMI);
				accept(Token.SEMI);
				if (stack.size() != returnSignature.size()) {
					throw new ParserException("Invalid return statement: expected "+returnSignature.size()+" values but got "+stack.size(), scanner.getLineNum());
				}
				for (ScriptDataType type : returnSignature) {
					ScriptDataType actualType = stack.pop();
					if (actualType != type) {
						throw new ParserException("Invalid return statement: expected "+type+" but got "+actualType, scanner.getLineNum());
					}
				}
				instructions.add(ScriptOpcode.RETURN);
				break;
			case IDENTIFIER:
				String identifier = scanner.getStringValue();
				if (isDataType(identifier)) {//Type identifier (for variable declaration)
					ScriptDataType dataType = getDataType(identifier);
					if (dataType == null) {
						throw new ParserException("Unsupported data type: "+identifier, scanner.getLineNum());
					}
					token = scanner.nextToken();
					if (token != Token.IDENTIFIER) {
						throw new ParserException("Unexpected token: '"+token+"'", scanner.getLineNum());
					}
					identifier = scanner.getStringValue();
					if (localVars.containsKey(identifier)) {
						throw new ParserException("Variable "+identifier+" is already defined!");
					}
					LocalVariable var = new LocalVariable(identifier, dataType);
					localVars.put(identifier, var);
					token = scanner.nextToken();
					if (token == Token.EQ) {
						parseAssignment(var);
					} else if (token != Token.SEMI) {
						throw new ParserException("Unexpected token: '"+token+"'", scanner.getLineNum());
					}
					scanner.nextToken();
				} else if (isLocalVariable(identifier)) {//Local variable setter
					LocalVariable var = localVars.get(identifier);
					if (var == null) {
						throw new ParserException("Undefined local variable: "+identifier, scanner.getLineNum());
					}
					parseAssignment(var);
				} else if (isGlobalVariable(identifier)) {//Global variable setter
					ScriptDataType type = linker.getGlobalVarType(identifier.substring(1));
					if (type == null) {
						throw new ParserException("Undefined global variable: "+identifier);
					}
					accept(Token.EQ);//For now, only regular assignments are allowed (in future, +=, -=, etc will be supported)
					parseInfix(Token.SEMI);//Get the value
					ScriptDataType assignedType = stack.pop();
					if (type != assignedType) {
						throw new ParserException("Incompatible data types. Expected: "+type+", got: "+assignedType, scanner.getLineNum());
					}
					instructions.add(ScriptOpcode.ASSIGN_GLOBAL);
					setConst(instructions.size(), linker.getGlobalVarId(identifier.substring(1)));
					accept(Token.SEMI);
				} else if (isScriptCall(identifier)) {
					ScriptHeader script = linker.lookup(identifier.substring(1));
					if (script == null) {
						throw new ParserException("Call to undefined script: "+identifier);
					}
					parseScriptCall(script);
					while (!stack.isEmpty()) {
						ScriptDataType type = stack.pop();
						//The return values are not assigned, so they must be dropped from the stack
						if (type.intBase()) {
							instructions.add(ScriptOpcode.DROP_INT);
						} else {
							instructions.add(ScriptOpcode.DROP_OBJ);
						}
					}
					accept(Token.SEMI);
				}
				break;
			default:
				throw new ParserException("Unexpected token: '"+token+"'", scanner.getLineNum());
		}
	}
	
	private void parseAssignment (LocalVariable var) throws ParserException {
		accept(Token.EQ);//For now, only regular assignments are allowed (in future, +=, -=, etc will be supported)
		parseInfix(Token.SEMI);//Get the value
		ScriptDataType type = stack.pop();
		if (type != var.getDataType()) {
			throw new ParserException("Incompatible data types. Expected: "+var.getDataType()+", got: "+type, scanner.getLineNum());
		}
		instructions.add(type.intBase() ? ScriptOpcode.ASSIGN_LOCAL_INT : ScriptOpcode.ASSIGN_LOCAL);
		setConst(instructions.size(), var.id);
		accept(Token.SEMI);
	}
	
	private void parseScriptCall (ScriptHeader script) throws ParserException {
		if (scanner.nextToken() == Token.LPAREN) {//This means the script has arguments
			int num;
			try {
				num = parseParams(Token.RPAREN);
			} catch (ParserException ex) {
				throw new ParserException("Error on call to script: "+script, ex);
			}
			if (num != script.getParams().size()) {
				throw new ParserException("Wrong signature for script: "+script);
			}
			for (ScriptDataType type : script.getParams()) {
				if (type != stack.pop()) {
					throw new ParserException("Wrong signature for script: "+script);
				}
			}
			accept(Token.RPAREN);
		}
		instructions.add(ScriptOpcode.INVOKE_SCRIPT);
		setConst(instructions.size(), script.getId());
		for (ScriptDataType returnType : script.getReturns()) {
			stack.push(returnType);
		}
	}
	
	private void parseApiCall (ApiFunction function) throws ParserException {
		if (scanner.nextToken() == Token.LPAREN) {//This means the call has arguments
			int num;
			try {
				num = parseParams(Token.RPAREN);
			} catch (ParserException ex) {
				throw new ParserException("Error on call to builtin function: "+function.getName(), ex);
			}
			if (num != function.getParamSignature().size()) {
				throw new ParserException("Wrong signature for builtin function: "+function.getName());
			}
			for (ScriptDataType type : function.getParamSignature()) {
				if (type != stack.pop()) {
					throw new ParserException("Wrong signature for builtin function: "+function.getName());
				}
			}
			scanner.nextToken();
		}
		instructions.add(ScriptOpcode.INVOKE_API);
		setConst(instructions.size(), function.getOpcode());
		for (ScriptDataType returnType : function.getReturnSignature()) {
			stack.push(returnType);
		}
	}
	
	private void parseConditionalStatement (List<Integer> falsePositions, List<Integer> truePositions) throws ParserException {
		Token token = scanner.getToken();
		List<Integer> jumpToEnd = new ArrayList<>();
		List<Integer> jumpToOr = new ArrayList<>();
		List<Integer> deepFalse = new ArrayList<>();
		List<Integer> deepTrue = new ArrayList<>();
		//&& takes precident over ||. Thus if a true || exists, it will jump to the end
		while (token != Token.RPAREN) {
			if (token == Token.LPAREN) {
				scanner.nextToken();
				parseConditionalStatement(deepFalse, deepTrue);
				accept(Token.RPAREN);
			} else {
				parseCondition();
				checkStack();//All items in the stack should be accounted for by now.
				deepTrue.add(instructions.size());
			}
			token = scanner.getToken();
			if (token == Token.AMPAMP) {//&& (and)
				instructions.add(ScriptOpcode.JUMP);//Add the 'false' condition
				deepFalse.add(instructions.size());
				
				jumpToOr.addAll(deepFalse);//All 'false' conditions should wait until the next 'or' block (or the termination of the condition)
				
				for (Integer instr : deepTrue) {//All true positions should try the next criteria
					setConst(instr, instructions.size()-instr);
				}
				deepTrue.clear();
				token = scanner.nextToken();
			} else if (token == Token.BARBAR) {//|| (or)
				jumpToEnd.addAll(deepTrue);//The previous 'true' conditions can safely jump to the end of the block
				
				for (Integer instr : jumpToOr) {//All previous 'false' conditions can jump to here
					setConst(instr, instructions.size()-instr);
				}
				jumpToOr.clear();
				
				for (Integer instr : deepFalse) {//All false positions should try the next criteria
					setConst(instr, instructions.size()-instr);
				}
				deepFalse.clear();
				token = scanner.nextToken();
			} else if (token != Token.RPAREN) {
				throw new ParserException("Unexpected token: "+token);
			}
		}
		falsePositions.addAll(deepFalse);
		falsePositions.addAll(jumpToOr);
		truePositions.addAll(deepTrue);
		truePositions.addAll(jumpToEnd);
	}
	
	private void parseCondition () throws ParserException {
		Token token = scanner.getToken();
		ScriptOpcode condition;
		boolean boolCondition = false;
		if (token == Token.BANG) {//This is a "not" conditional
			scanner.nextToken();
			parseInfix(Token.AMPAMP, Token.BARBAR, Token.RPAREN);
			condition = ScriptOpcode.JUMP_FALSE;
			boolCondition = true;
		} else {
			parseInfix(Token.AMPAMP, Token.BARBAR, Token.RPAREN, Token.EQEQ, Token.BANGEQ, Token.GT, Token.GTEQ, Token.LT, Token.LTEQ);
			token = scanner.getToken();
			switch (token) {//Check the center op for the conditional
				case EQEQ:
					condition = ScriptOpcode.JUMP_EQ;
					scanner.nextToken();
					break;
				case BANGEQ:
					condition = ScriptOpcode.JUMP_NT_EQ;
					scanner.nextToken();
					break;
				case GT:
					condition = ScriptOpcode.JUMP_GT;
					scanner.nextToken();
					break;
				case LT:
					condition = ScriptOpcode.JUMP_LT;
					scanner.nextToken();
					break;
				case GTEQ:
					condition = ScriptOpcode.JUMP_GT_EQ;
					scanner.nextToken();
					break;
				case LTEQ:
					condition = ScriptOpcode.JUMP_LT_EQ;
					scanner.nextToken();
					break;
				default://Otherwise, it must be a "true" conditional
					condition = ScriptOpcode.JUMP_TRUE;
					boolCondition = true;
					break;
			}
		}
		if (boolCondition) {
			if (stack.pop() != ScriptDataType.BOOLEAN) {
				throw new ParserException("The not operator must be applied to a boolean.");
			}
			instructions.add(condition);
		} else {//Otherwise it's a comparison (of some sort)	
			parseInfix(Token.AMPAMP, Token.BARBAR, Token.RPAREN);
			if (stack.pop() != ScriptDataType.INT || stack.pop() != ScriptDataType.INT) {
				throw new ParserException("The equal operator must be applied to two numbers.");
			}//For now we'll just use ints, but in the future this should support int base types as well
			instructions.add(condition);
		}
	}
	
	private ScriptOpcode opcodeForInfix (Token token) {
		switch (token) {
			case PLUS://Addition
				return ScriptOpcode.ADD;
			case SUB://Subtraction
				return ScriptOpcode.SUBTRACT;
			case STAR://Multiplication
				return ScriptOpcode.MULTIPLY;
			case SLASH://Division
				return ScriptOpcode.DIVIDE;
			case PERCENT://Modulo
				return ScriptOpcode.MODULO;
			default:
				return null;
		}
	}
	
	private void parseInfix (Token... breakTokens) throws ParserException {
		parseParam(breakTokens);
		List<Token> breakList = Arrays.asList(breakTokens);
		Token token = scanner.getToken();
		Stack<ScriptOpcode> opStack = new Stack<>();
		while (!breakList.contains(token)) {
			ScriptDataType op1 = stack.pop();
			ScriptOpcode operator = opcodeForInfix(token);
			if (operator == null) {
				throw new ParserException("Unexpected token: "+scanner.getToken());
			}
			scanner.nextToken();
			parseParam(breakTokens);
			ScriptDataType op2 = stack.pop();
			if (op1 != ScriptDataType.INT || op1 != op2) {
				throw new ParserException("Invalid use of plus operator: "+scanner.getToken());
			}
			token = scanner.getToken();
			ScriptOpcode nextOp = opcodeForInfix(token);
			if (operator == ScriptOpcode.ADD || operator == ScriptOpcode.SUBTRACT) {
				if (nextOp == ScriptOpcode.MULTIPLY || nextOp == ScriptOpcode.DIVIDE || nextOp == ScriptOpcode.MODULO) {
					opStack.push(operator);//Next operator has higher precidence
					stack.push(op2);
				} else {
					Iterator<ScriptOpcode> it = opStack.iterator();
					while (it.hasNext()) {
						ScriptOpcode op = it.next();
						it.remove();
						op2 = stack.pop();
						instructions.add(op);
						stack.push(op2);						
					}
					instructions.add(operator);
					stack.push(op2);
				}
			} else {
				instructions.add(operator);
				stack.push(op2);				
			}			
		}
		if (!opStack.isEmpty()) {
			for (ScriptOpcode op : opStack) {
				ScriptDataType operand2 = stack.pop();
				instructions.add(op);
				stack.push(operand2);						
			}
		}
	}
	
	protected int parseParams (Token breakTokens) throws ParserException {
		int num = 0;
		while (scanner.getToken() != breakTokens) {
			parseInfix(Token.COMMA, breakTokens);
			//parseParam(Token.COMMA, breakTokens);
			if (scanner.getToken() != Token.COMMA && scanner.getToken() != breakTokens) {
				throw new ParserException("Unexpected token: "+scanner.getToken());
			}
			scanner.nextToken();
			num++;
		}
		return num;
	}
	
	private void parseParam (Token... breakTokens) throws ParserException {
		switch (scanner.getToken()) {
			case LPAREN:
				scanner.nextToken();
				parseInfix(Token.RPAREN);
				accept(Token.RPAREN);
				break;
			case INTLITERAL://Number
				instructions.add(ScriptOpcode.LOAD_INT_CONST);
				setConst(instructions.size(), Integer.parseInt(scanner.getStringValue()));
				stack.push(ScriptDataType.INT);
				scanner.nextToken();
				break;
			case STRINGLITERAL://String
				instructions.add(ScriptOpcode.LOAD_STR_CONST);
				setConst(instructions.size(), scanner.getStringValue());
				stack.push(ScriptDataType.STRING);
				scanner.nextToken();
				break;
			case TRUE://Boolean
				instructions.add(ScriptOpcode.LOAD_INT_CONST);
				setConst(instructions.size(), 1);
				stack.push(ScriptDataType.BOOLEAN);
				scanner.nextToken();
				break;
			case FALSE://Boolean
				instructions.add(ScriptOpcode.LOAD_INT_CONST);
				setConst(instructions.size(), 0);
				stack.push(ScriptDataType.BOOLEAN);
				scanner.nextToken();
				break;
			case IDENTIFIER:
				String identifier = scanner.getStringValue();
				if (isLocalVariable(identifier)) {//Local Variable
					LocalVariable var = localVars.get(identifier);
					if (var == null) {
						throw new ParserException("Undefined local variable: "+identifier);
					}
					instructions.add(var.type.intBase() ? ScriptOpcode.LOAD_INT_LOCAL : ScriptOpcode.LOAD_LOCAL);
					setConst(instructions.size(), var.id);
					stack.push(var.type);
					scanner.nextToken();
				} else if (isGlobalVariable(identifier)) {//Global variable
					ScriptDataType type = linker.getGlobalVarType(identifier.substring(1));
					if (type == null) {
						throw new ParserException("Undefined global variable: "+identifier);
					}
					instructions.add(ScriptOpcode.LOAD_GLOBAL);
					setConst(instructions.size(), linker.getGlobalVarId(identifier.substring(1)));
					stack.push(type);
					scanner.nextToken();
				} else if (isScriptCall(identifier)) {//Call to another script
					ScriptHeader script = linker.lookup(identifier.substring(1));
					if (script == null) {
						throw new ParserException("Call to undefined script: "+identifier);
					}
					if (script.getReturns().size() != 1) {
						throw new ParserException("Inline calls must return one value. "+identifier+" returns "+script.getReturns().size());
					}
					parseScriptCall(script);
				} else if (isApiCall(identifier)) {//Call to an api function
					ApiFunction function = linker.getApiFunction(identifier);
					if (function == null) {
						throw new ParserException("Call to undefined api function: "+identifier);
					}
					if (function.getReturnSignature().size() != 1) {
						throw new ParserException("Inline calls must return one value. "+function.getName()+" returns "+function.getReturnSignature().size());
					}
					parseApiCall(function);
				} else {
					throw new ParserException("Unexpected identifier: "+identifier);
				}
				break;
			default:
				throw new ParserException("Unexpected token: "+scanner.getToken());
		}
	}
}
