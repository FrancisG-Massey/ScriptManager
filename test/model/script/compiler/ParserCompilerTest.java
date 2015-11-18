package model.script.compiler;

import model.script.ScriptDataType;
import java.util.Stack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Francis
 */
public class ParserCompilerTest {
	
	private Linker linker;
	private ScriptParser parser;
	
	public ParserCompilerTest() {
		
	}	
	
	@Before
	public void setUp() {
		linker = new Linker();
		parser = new ScriptParser(linker);
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void testHeader () throws ParserException {		
		String source = "[test,test1](Int $i1, String $i2)(Boolean) {}";
		parser.setSource(source);
		parser.parseHeader();
		assertEquals("test1", parser.getBinding());
		assertEquals(2, parser.getParamSignature().size());
		assertEquals(ScriptDataType.INT, parser.getParamSignature().get(0));
		assertEquals(ScriptDataType.STRING, parser.getParamSignature().get(1));
		assertEquals(1, parser.getReturnSignature().size());
		assertEquals(ScriptDataType.BOOLEAN, parser.getReturnSignature().get(0));
	}
	
	@Test
	public void testVoidHeader () throws ParserException {		
		String source = "[test,test7]()() {}";
		parser.setSource(source);
		parser.parseHeader();
		assertEquals("test7", parser.getBinding());
		assertEquals(0, parser.getParamSignature().size());
		assertEquals(0, parser.getReturnSignature().size());
	}
	
	@Test(expected=ParserException.class)
	public void testInvalidHeader () throws ParserException {		
		String source = "[test,test7](int1,int2)() {}";
		parser.setSource(source);
		parser.parseHeader();
	}
	
	@Test
	public void testVarDeclaration () throws ParserException {
		String source = "Int $var1 = 12034;";
		parser.setSource(source);
		parser.parseBlockStatement();
		assertEquals(1, parser.localVars.size());
		assertTrue(parser.localVars.containsKey("$var1"));
		assertEquals(ScriptDataType.INT, parser.localVars.get("$var1").getDataType());
		assertEquals(2, parser.instructions.size());
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(0));
		assertEquals(ScriptOpcode.ASSIGN_LOCAL_INT, parser.instructions.get(1));
		assertTrue(parser.constants.containsKey(0));
		assertEquals(12034, parser.constants.get(0));
	}
	
	@Test(expected=ParserException.class)
	public void testInvalidVarDeclaration () throws ParserException {
		String source = "$var1 = 12034;";
		parser.setSource(source);
		parser.parseBlockStatement();
		assertEquals(1, parser.localVars.size());
		assertTrue(parser.localVars.containsKey("$var1"));
		assertEquals(ScriptDataType.INT, parser.localVars.get("$var1").getDataType());
		assertEquals(2, parser.instructions.size());
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(0));
		assertEquals(ScriptOpcode.ASSIGN_LOCAL_INT, parser.instructions.get(1));
		assertTrue(parser.constants.containsKey(0));
		assertEquals(12034, parser.constants.get(0));
	}
	
	@Test
	public void testConditionalBlock () throws ParserException {
		String source = "if (true) {} else {}";
		parser.setSource(source);
		parser.parseConditionalBlock();
		assertEquals(4, parser.instructions.size());
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(0));
		assertTrue(parser.constants.containsKey(0));
		assertEquals(1, parser.constants.get(0));
		assertEquals(ScriptOpcode.JUMP_TRUE, parser.instructions.get(1));
		assertTrue(parser.constants.containsKey(1));
		assertEquals(1, parser.constants.get(1));
		assertEquals(ScriptOpcode.JUMP, parser.instructions.get(2));//"False" condition
		assertTrue(parser.constants.containsKey(2));
		assertEquals(1, parser.constants.get(2));
		assertEquals(ScriptOpcode.JUMP, parser.instructions.get(3));//End of "If" block
		assertTrue(parser.constants.containsKey(3));
		assertEquals(0, parser.constants.get(3));
		
	}
	
	@Test
	public void testCondition () throws ParserException {
		String source = "while (1 == 2 || 3 != 8 && 7 >= 9) {}";
		parser.setSource(source);
		parser.parseWhileLoop();
		
		assertEquals(12, parser.instructions.size());
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(0));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(1));
		assertEquals(ScriptOpcode.JUMP_EQ, parser.instructions.get(2));
		assertTrue(parser.constants.containsKey(2));
		assertEquals(8, parser.constants.get(2));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(3));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(4));
		assertEquals(ScriptOpcode.JUMP_NT_EQ, parser.instructions.get(5));
		assertTrue(parser.constants.containsKey(5));
		assertEquals(1, parser.constants.get(5));
		assertEquals(ScriptOpcode.JUMP, parser.instructions.get(6));
		assertTrue(parser.constants.containsKey(6));
		assertEquals(3, parser.constants.get(6));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(7));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(8));
		assertEquals(ScriptOpcode.JUMP_GT_EQ, parser.instructions.get(9));
		assertTrue(parser.constants.containsKey(9));
		assertEquals(1, parser.constants.get(9));
		assertEquals(ScriptOpcode.JUMP, parser.instructions.get(10));
		assertTrue(parser.constants.containsKey(10));
		assertEquals(1, parser.constants.get(10));
		assertEquals(ScriptOpcode.JUMP, parser.instructions.get(11));
		assertTrue(parser.constants.containsKey(11));
		assertEquals(-12, parser.constants.get(11));
	}
	
	private boolean testCompiledCondition (int[] localInts) {
		int ptr = 0;
		Stack<Integer> stack = new Stack<>();
		while (ptr < parser.instructions.size()) {
			switch (parser.instructions.get(ptr)) {
				case LOAD_INT_CONST:
					stack.push((Integer) parser.constants.get(ptr));
					break;
				case LOAD_INT_LOCAL:
					stack.push(localInts[(int) parser.constants.get(ptr)]);
					break;
				case JUMP:
					int amount = (int) parser.constants.get(ptr);
					if (amount < 0) {
						return true;
					}
					ptr += amount;
					break;
				case JUMP_EQ:
					int val2 = stack.pop();
					int val1 = stack.pop();
					if (val1 == val2) {
						ptr += (int) parser.constants.get(ptr);
					}
					break;
				case JUMP_NT_EQ:
					val2 = stack.pop();
					val1 = stack.pop();
					if (val1 != val2) {
						ptr += (int) parser.constants.get(ptr);
					}
					break;
				case JUMP_GT:
					val2 = stack.pop();
					val1 = stack.pop();
					if (val1 > val2) {
						ptr += (int) parser.constants.get(ptr);
					}
					break;
				case JUMP_LT:
					val2 = stack.pop();
					val1 = stack.pop();
					if (val1 < val2) {
						ptr += (int) parser.constants.get(ptr);
					}
					break;
				case JUMP_GT_EQ:
					val2 = stack.pop();
					val1 = stack.pop();
					if (val1 >= val2) {
						ptr += (int) parser.constants.get(ptr);
					}
					break;
				case JUMP_LT_EQ:
					val2 = stack.pop();
					val1 = stack.pop();
					if (val1 <= val2) {
						ptr += (int) parser.constants.get(ptr);
					}
					break;
				default:
					throw new UnsupportedOperationException("Unsupported instruction: "+parser.instructions.get(ptr));
			}
			ptr++;
		}
		return false;
	}
	
	@Test
	public void testCondition2 () throws ParserException {		
		String source = "[t,t](Int $v1, Int $v2, Int $v3)()";
		source += "while (($v1 == 2 || $v2 != 8) && $v3 >= 9) {}";
		parser.setSource(source);
		parser.parseHeader();
		parser.parseWhileLoop();
		
		int[] v1vals = {1,2,3,9,100};
		int[] v2vals = {2,3,8,9,107};
		int[] v3vals = {2,8,9,11,100};
		for (int v1 : v1vals) {
			for (int v2 : v2vals) {
				for (int v3 : v3vals) {
					if ((v1 == 2 || v2 != 8) && v3 >= 9) {
						assertTrue("Condition should be true for v1="+v1+", v2="+v2+", v3="+v3, testCompiledCondition(new int[]{v1, v2, v3}));
					} else {
						assertFalse("Condition should be false for v1="+v1+", v2="+v2+", v3="+v3, testCompiledCondition(new int[]{v1, v2, v3}));
					}					
				}
			}
		}
	}
	
	@Test
	public void testInfix () throws ParserException {	
		String source = "1+2*8";
		parser.setSource(source);
		parser.parseParams(Token.EOF);
		
		assertEquals(5, parser.instructions.size());
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(0));
		assertTrue(parser.constants.containsKey(0));
		assertEquals(1, parser.constants.get(0));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(1));
		assertTrue(parser.constants.containsKey(1));
		assertEquals(2, parser.constants.get(1));
		assertEquals(ScriptOpcode.LOAD_INT_CONST, parser.instructions.get(2));
		assertTrue(parser.constants.containsKey(2));
		assertEquals(8, parser.constants.get(2));
		assertEquals(ScriptOpcode.MULTIPLY, parser.instructions.get(3));
		assertEquals(ScriptOpcode.ADD, parser.instructions.get(4));
	}
}
