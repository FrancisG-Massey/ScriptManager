package model.script.compiler;

import java.nio.CharBuffer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Francis
 */
public class ScannerTest {
	
	public ScannerTest() {
		
	}

    @Test
	public void testBrace() throws ParserException {
		String str = "{}";
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.LBRACE, scanner.nextToken());
		assertEquals(Token.RBRACE, scanner.nextToken());
		assertEquals(Token.EOF, scanner.nextToken());
	}

    @Test
	public void testIdentifier() throws ParserException {
		String str = "hello";
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.IDENTIFIER, scanner.nextToken());
		assertEquals("hello", scanner.getStringValue());
		assertEquals(Token.EOF, scanner.nextToken());
	}

    @Test
	public void testStringLit() throws ParserException {
		String str = "\"hello!*^%$\"";//Throw in some special characters
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.STRINGLITERAL, scanner.nextToken());
		assertEquals("hello!*^%$", scanner.getStringValue());
		assertEquals(Token.EOF, scanner.nextToken());
	}

    @Test
	public void testLineNumber() throws ParserException {
		String str = "\n\n$\n\t     {}\n\n";
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.DOLLAR, scanner.nextToken());
		assertEquals(3, scanner.getLineNum());
		assertEquals(Token.LBRACE, scanner.nextToken());
		assertEquals(4, scanner.getLineNum());
		assertEquals(Token.RBRACE, scanner.nextToken());
		assertEquals(Token.EOF, scanner.nextToken());
		assertEquals(6, scanner.getLineNum());
	}

    @Test
	public void testIntLit() throws ParserException {
		String str = "12845345734";//Throw in some special characters
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.INTLITERAL, scanner.nextToken());
		assertEquals("12845345734", scanner.getStringValue());
		assertEquals(Token.EOF, scanner.nextToken());
	}

    @Test
	public void testBooleanLit() throws ParserException {
		String str = "true false";//Throw in some special characters
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.TRUE, scanner.nextToken());
		assertEquals(Token.FALSE, scanner.nextToken());
		assertEquals(Token.EOF, scanner.nextToken());
	}
	
	@Test(expected=ParserException.class)
	public void testUnclosedStr() throws ParserException {
		String str = "()\"hello []";//Throw in some special characters
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.LPAREN, scanner.nextToken());
		assertEquals(Token.RPAREN, scanner.nextToken());
		assertEquals(Token.STRINGLITERAL, scanner.nextToken());
	}
	
	@Test
	public void testComment() throws ParserException {
		String str = "24335;//Hail. false. ;wefu&54\n DataType";//Throw in some special characters
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.INTLITERAL, scanner.nextToken());
		assertEquals(Token.SEMI, scanner.nextToken());
		assertEquals(Token.IDENTIFIER, scanner.nextToken());
		assertEquals(Token.EOF, scanner.nextToken());
	}
	
	@Test
	public void testEqCombo() throws ParserException {
		String str = "!= >= <=";//Throw in some special characters
		SourceScanner scanner = new SourceScanner(CharBuffer.wrap(str));
		assertEquals(Token.BANGEQ, scanner.nextToken());
		assertEquals(Token.GTEQ, scanner.nextToken());
		assertEquals(Token.LTEQ, scanner.nextToken());
		assertEquals(Token.EOF, scanner.nextToken());
	}
}
