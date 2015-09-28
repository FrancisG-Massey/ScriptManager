package model.script.compiler;

/**
 *
 * @author Francis
 */
public enum Token {
    EOF,
    IDENTIFIER,
    BREAK("break"),
    CASE("case"),
    CONTINUE("continue"),
    DEFAULT("default"),
    ELSE("else"),
	IF("if"),
    RETURN("return"),
    WHILE("while"),
    INTLITERAL,
    LONGLITERAL,
    FLOATLITERAL,
    DOUBLELITERAL,
    CHARLITERAL,
    STRINGLITERAL,
    TRUE("true"),
    FALSE("false"),
    NULL("null"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    SEMI(";"),
    COMMA(","),
    EQ("="),
    PLUSEQ("+="),
    SUBEQ("-="),
    GT(">"),
    LT("<"),
    BANG("!"),
    TILDE("~"),
    QUES("?"),
    COLON(":"),
    EQEQ("=="),
    LTEQ("<="),
    GTEQ(">="),
    BANGEQ("!="),
	AMPAMP("&&"),
    BARBAR("||"),
    PLUSPLUS("++"),
    SUBSUB("--"),
    PLUS("+"),
    SUB("-"),
    STAR("*"),
    SLASH("/"),
    AMP("&"),
    BAR("|"),
    CARET("^"),
    PERCENT("%"),
    DOLLAR("$");
	
	private String name;

    Token() {
        this(null);
    }
    Token(String name) {
        this.name = name;
    }
	
	public String getName () {
		return name;
	}
	
	public static Token forString (String str) {
		for (Token token : values()) {
			if (str.equals(token.name)) {
				return token;
			}
		}
		return null;
	}
}
