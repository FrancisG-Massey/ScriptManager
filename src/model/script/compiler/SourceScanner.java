package model.script.compiler;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Francis
 */
public class SourceScanner {
	
	private final int limit;
	
	CharBuffer data;
	
    private char ch;
	
	private final StringBuilder strBuffer = new StringBuilder();
	
	private Token token;
	
	private int lineNum;
	
	private final List<Integer> lineNumbers = new ArrayList<>();
	
	public SourceScanner (CharBuffer input) {
		data = input;//.toString().toCharArray();
		limit = input.length();
		lineNum = 1;
		lineNumbers.add(0);
		scanChar();
	}
	
	private void scanChar() {
		if (!data.hasRemaining()) {
			ch = '\0';
		} else {
			ch = data.get();
		}
       // ch = data[pointer++];
	}
	
	public int getPos () {
		return data.position();
	}
	
	public void setPos (int pos) {
		data.position(pos);
		ch = 0;
		strBuffer.setLength(0);
		lineNum = 0;
		for (int p : lineNumbers) {
			if (p > pos) {
				lineNum++;
			} else {
				break;
			}
		}
	}
	
    private int getDigit(int base) {
        char c = ch;
        int result = Character.digit(c, base);
        if (result >= 0 && c > 0x7f) {
            ch = "0123456789abcdef".charAt(result);
        }
        return result;
    }
	
    private void scanLitChar() throws ParserException {
        if (ch == '\\') {
            if (data.get(getPos()+1) == '\\') {
                data.get();
                strBuffer.append('\\');
                scanChar();
            } else {
                scanChar();
                switch (ch) {
                case '0': case '1': case '2': case '3':
                case '4': case '5': case '6': case '7':
                    char leadch = ch;
                    int oct = getDigit(8);
                    scanChar();
                    if ('0' <= ch && ch <= '7') {
                        oct = oct * 8 + getDigit(8);
                        scanChar();
                        if (leadch <= '3' && '0' <= ch && ch <= '7') {
                            oct = oct * 8 + getDigit(8);
                            scanChar();
                        }
                    }
                    strBuffer.append((char)oct);
                    break;
                case 'b':
                     strBuffer.append('\b');
					 scanChar();
					 break;
                case 't':
                    strBuffer.append('\t'); 
					scanChar(); 
					break;
                case 'n':
                    strBuffer.append('\n');
					scanChar(); 
					break;
                case 'f':
                    strBuffer.append('\f');
					scanChar(); 
					break;
                case 'r':
                    strBuffer.append('\r'); 
					scanChar();
					break;
                case '\'':
                    strBuffer.append('\''); 
					scanChar(); 
					break;
                case '\"':
                    strBuffer.append('\"'); 
					scanChar(); 
					break;
                case '\\':
                    strBuffer.append('\\'); 
					scanChar(); 
					break;
                default:
                    throw new ParserException("Illegal escape character.");
                }
            }
        } else if (data.position() != limit) {
            strBuffer.append(ch); 
			scanChar();
        }
    }

    private void scanDigits(int digitRadix) throws ParserException {
        char saveCh;
        do {
            if (ch != '_') {
                strBuffer.append(ch);
            }
            saveCh = ch;
            scanChar();
        } while (getDigit(digitRadix) >= 0 || ch == '_');
        if (saveCh == '_') {
            throw new ParserException("Illegal underscore");
		}
    }
	
    private void scanIdentifier() {
        do {
			strBuffer.append(ch);

            scanChar();
			if (!isIdentifierChar(ch)) {//Reached the end of the identifier
				token = Token.forString(getStringValue());
				if (token == null) {
					token = Token.IDENTIFIER;
				}
				return;
			}
        } while (true);
    }
	
    private void scanOperator() {
        while (true) {
            strBuffer.append(ch);
			token = Token.forString(getStringValue());
			scanChar();
			
            if (!isOperatorChar(ch)) {
				break;
			}
        }
    }
	
	public Token getToken () {
		return token;
	}
	
	public int getLineNum () {
		return lineNum;
	}
	
	public String getStringValue () {
		return strBuffer.toString();
	}
	
    private boolean isOperatorChar(char ch) {
        switch (ch) {
        case '!': case '%': case '&': case '*': case '?':
        case '+': case '-': case ':': case '<': case '=':
        case '>': case '^': case '|': case '~':
        case '@':
            return true;
        default:
            return false;
        }
    }
	
	private boolean isIdentifierChar (char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
					|| (ch >= '0' && ch <= '9') || ch == '_';
	}
	
	public Token nextToken() throws ParserException {
		strBuffer.setLength(0);
		while (true) {
			switch (ch) {
				case '\0':
					token = Token.EOF;
					return token;
				case '\n':
					if (lineNumbers.size() == lineNum) {
						lineNumbers.add(data.position());
					}
					lineNum++;
				case ' ':
					do {
                        scanChar();
                    } while (ch == ' ' || ch == '\t');
					break;
				case ',':
					token = Token.COMMA;
					scanChar();
					return token;
				case ';':
					token = Token.SEMI;
					scanChar();
					return token;
				case '(':
					token = Token.LPAREN;
					scanChar();
					return token;
				case ')':
					token = Token.RPAREN;
					scanChar();
					return token;
				case '[':
					token = Token.LBRACKET;
					scanChar();
					return token;
				case ']':
					token = Token.RBRACKET;
					scanChar();
					return token;
				case '{':
					token = Token.LBRACE;
					scanChar();
					return token;
				case '}':
					token = Token.RBRACE;
					scanChar();
					return token;
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z'://Type
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z'://External call
					scanIdentifier();
					return token;
				case '~'://Call to another function
					scanChar();
					if (isIdentifierChar(ch)) {
						strBuffer.append('~');
						scanIdentifier();
					} else {
						token = Token.TILDE;
					}
					return token;
				case '%'://Call to external variable
					scanChar();
					if (isIdentifierChar(ch)) {
						strBuffer.append('%');
						scanIdentifier();
					} else {
						token = Token.PERCENT;
					}
					return token;
				case '$'://Call to local variable
					scanChar();
					if (isIdentifierChar(ch)) {
						strBuffer.append('$');
						scanIdentifier();
					} else {
						token = Token.DOLLAR;
					}
					return token;
				case '0': 
				case '1': 
				case '2': 
				case '3': 
				case '4':
                case '5': 
				case '6': 
				case '7': 
				case '8': 
				case '9':
					scanDigits(10);
					token = Token.INTLITERAL;
					return token;
				case '-':
					scanChar();
                    if (Character.isDigit(ch)) {
						scanDigits(10);
						token = Token.INTLITERAL;
						return token;
					} else {
						token = Token.SUB;
						return token;
					}					
				case '/':
                    scanChar();
                    if (ch == '/') {
                        do {
                            scanChar();
                        } while (ch != '\r' && ch != '\n' && data.hasRemaining());
                        if (data.position() < limit) {
                            //endPos = bp;
                            //processComment(CommentStyle.LINE);
                        }
                        break;
                    } else if (ch == '*') {
                        scanChar();
						while (data.position() < limit) {
							if (ch == '*') {
								scanChar();
								if (ch == '/') break;
							}
						}
                        if (ch == '/') {
                            scanChar();
                            break;
                        } else {
                            return token;
                        }
                    } else  {
                        token = Token.SLASH;
                    }
                    return token;
                case '\"':
                    scanChar();
                    while (ch != '\"' && ch != '\r' && ch != '\n' && data.hasRemaining()) {
                        scanLitChar();
					}
                    if (ch == '\"') {
                        token = Token.STRINGLITERAL;
                        scanChar();
                    } else {
                        throw new ParserException("Unclosed string literal");
                    }
                    return token;
				default:
					if (isOperatorChar(ch)) {
						scanOperator();
						return token;
					} else if (!data.hasRemaining()) {
						token = Token.EOF;
						return token;
					} else {
						scanChar();
					}
			}
		}
	}
}
