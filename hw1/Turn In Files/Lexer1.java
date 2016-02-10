//----------------------------------------------------------------------
// A starter version of miniJava lexer (manual version). (For CS321 HW1)
//----------------------------------------------------------------------
//
// Matthew OBrien
//
//
import java.io.*;


public class Lexer1 {
  private static FileReader input = null;
  private static int nextC = -1;   // buffer for holding next char	
  private static int line = 1;     // currect line position
  private static int column = 0;   // currect column position
  
  // Internal token code
  //
  enum TokenCode {

    // Tokens with multiple lexemes
    ID, INTLIT, DBLLIT, STRLIT,

    // Keywords
    //   "class", "extends", "static", "public", "main", "void", "boolean", 
    //   "int", "double", "String", "true", "false", "new", "this", "if", 
    //   "else", "while", "return", "System", "out", "println"
    CLASS, EXTENDS, STATIC, PUBLIC, MAIN, VOID, BOOLEAN, INT, DOUBLE, STRING, 
    TRUE, FALSE, NEW, THIS, IF, ELSE, WHILE, RETURN, SYSTEM, OUT, PRINTLN,

    // Operators and delimiters
    //   +, -, *, /, &&, ||, !, ==, !=, <, <=, >, >=, =, 
    //   ;, ,, ., (, ), [, ], {, }
    ADD, SUB, MUL, DIV, AND, OR, NOT, EQ, NE, LT, LE, GT, GE,  ASSGN,
    SEMI, COMMA, DOT, LPAREN, RPAREN, LBRAC, RBRAC, LCURLY, RCURLY;
  }

  // Token representation
  //
  static class Token {
    TokenCode code;
    String lexeme;
    int line;	   	// line # of token's first char
    int column;    	// column # of token's first char
    
    public Token(TokenCode code, String lexeme, int line, int column) {
      this.code=code; this.lexeme=lexeme;
      this.line=line; this.column=column; 
    }

    public String toString() {
      return String.format("(%d,%2d) %-10s %s", line, column, code, 
			   (code==TokenCode.STRLIT)? "\""+lexeme+"\"" : lexeme);
    }
  }

  static void init(FileReader in) throws Exception { 
    input = in; 
    nextC = input.read();
  }

  //--------------------------------------------------------------------
  // Do not modify the code listed above. Add your code below. 
  //
  private static boolean isSpace(int c) {
	return (c == ' ' || c == '\t' || c == '\r' || c == '\n');
  }

  private static boolean isLetter(int c) {
	return ('a' <= c && c <= 'z') || ('A' <= c && c<= 'Z');
  }
  private static boolean isDigit(int c) {
	return ('0' <= c && c <= '9');
  }
  private static boolean isOctal(int c) {
	return ('0' <= c && c <= '7');
  }

  private static boolean isHexi(int c) {
	return ('0' <= c) && (c <= '9') || ('A' <= c) && (c <= 'F') || ('a' <= c) && (c <= 'f');
  }

  private static boolean validInt (long c) {
	//long test = Long.parseLong(c);
	return (c <= 2147483647);
  }
	
  private static boolean validDBL(String c){
	long test = Long.parseLong(c);
	return(test <= 2147483647);
  }
  // Return next char
  //
  // - need to track both line and column numbers
  // 
  private static int nextChar() throws Exception {
	int c = nextC;
	nextC = input.read();
	if(c != -1)
		column++;
	if(c == '\n') {
		column = 0;
		line++;
	}
	return c;

  }

  // Return next token (the main lexer routine)
  //
  // - need to capture the line and column numbers of the first char 
  //   of each token
  //
  static Token nextToken() throws Exception {


    // ... add code ...
	int c = nextChar();

	//Ignoring white space	
	while (isSpace(c))
		c = nextChar();

	//Recognize the EOF
	if (c == -1)
		return null;

	//Comments
	if(c == '/'){
		if(nextC == '/'){
			do{
				c = nextChar();
			}while (c != '\n' && c != -1);
			return nextToken();
		}
		if(nextC == '*'){
			int temp = column;
			int temp1 = line;
			do{
				c = nextChar();
				if(c == '*' && nextC == '/'){
					c = nextChar();
					return nextToken();
				}
			}while(c != -1);
			throw new LexError("at ("+temp1+","+temp+"). Unclosed block comments");
		}
	}


	if(c == -1)
		return null;



	//Recognizing keywords
	if (isLetter(c)) {
		StringBuilder buffer = new StringBuilder();
		buffer.append((char) c);
		int temp = column; 

		while (isLetter(nextC) || isDigit(nextC)) {
			c = nextChar();
			buffer.append((char) c);
		}

		String lex = buffer.toString();
		if(lex.equals("class"))
			return new Token(TokenCode.CLASS, lex, line, temp);
		else if (lex.equals("extends"))
			return new Token(TokenCode.EXTENDS, lex, line, temp);
		else if (lex.equals("static"))
			return new Token(TokenCode.STATIC, lex, line, temp);
		else if (lex.equals("public"))
			return new Token(TokenCode.PUBLIC, lex, line, temp);
		else if (lex.equals("main"))
			return new Token(TokenCode.MAIN, lex, line, temp);
		else if (lex.equals("void"))
			return new Token(TokenCode.VOID, lex, line, temp);
		else if (lex.equals("boolean"))
			return new Token(TokenCode.BOOLEAN, lex, line, temp);
		else if (lex.equals("int"))
			return new Token(TokenCode.INT, lex, line, temp);
		else if (lex.equals("double"))
			return new Token(TokenCode.DOUBLE, lex, line, temp);
		else if (lex.equals("String"))
			return new Token(TokenCode.STRING, lex, line, temp);
		else if (lex.equals("true"))
			return new Token(TokenCode.TRUE, lex, line, temp);
		else if (lex.equals("false"))
			return new Token(TokenCode.FALSE, lex, line, temp);
		else if (lex.equals("new"))
			return new Token(TokenCode.NEW, lex, line, temp);
		else if (lex.equals("this"))
			return new Token(TokenCode.THIS, lex, line, temp);
		else if (lex.equals("if"))
			return new Token(TokenCode.IF, lex, line, temp);
		else if (lex.equals("else"))
			return new Token(TokenCode.ELSE, lex, line, temp);
		else if (lex.equals("while"))
			return new Token(TokenCode.WHILE, lex, line, temp);
		else if (lex.equals("return"))
			return new Token(TokenCode.RETURN, lex, line, temp);
		else if (lex.equals("System"))
			return new Token(TokenCode.SYSTEM, lex, line, temp);
		else if (lex.equals("out"))
			return new Token(TokenCode.OUT, lex, line, temp);
		else if (lex.equals("println"))
			return new Token(TokenCode.PRINTLN, lex, line, temp);
		else
			return new Token(TokenCode.ID, lex, line, temp);
					 
	}


	//Recognizing digits
	if(isDigit(c)){
		StringBuilder buffer = new StringBuilder();
		buffer.append((char) c);
		int temp = column;
		long toCheck;
		boolean check = false;

		while(isDigit(nextC) || nextC == '.' || nextC == 'X' || nextC == 'x') {
			
			if(c == '0'){
				if(nextC == 'X' || nextC == 'x'){
					c = nextChar();
					buffer.append((char) c);
					if(isHexi(nextC)){
						while(isHexi(nextC)){
							c = nextChar();
							buffer.append((char) c);
						}
						String lex = buffer.toString();
						toCheck = Long.decode(lex);
						if(!validInt(toCheck))
							throw new LexError("at ("+line+","+temp+"). Invalid hexadecimal literal: "+lex);
						else
							return new Token(TokenCode.INTLIT, lex, line, temp);
					}
					else{
						while(isDigit(nextC)){
							c = nextChar();
							buffer.append((char) c);
						}
						String lex = buffer.toString();
						throw new LexError("at ("+line+","+temp+"). Invalid hexadecimal literal: "+lex);
					}
				}
				if(isOctal(nextC)){
					c = nextChar();
					buffer.append((char) c);
					if(isOctal(nextC)){
						while(isOctal(nextC)){
							c = nextChar();
							buffer.append((char) c);
						}
					}
					String lex = buffer.toString();
					toCheck = Long.decode(lex);
					if(!validInt(toCheck))
						throw new LexError("at ("+line+","+temp+"). Invalid octal literal: "+lex);
					else
						return new Token(TokenCode.INTLIT, lex, line, temp);
				}
				if(nextC == '.'){
					c = nextChar();
					buffer.append((char) c);
					if(isDigit(nextC)){
						while(isDigit(nextC)){
							c = nextChar();
							buffer.append((char) c);
						}
						String lex = buffer.toString();
						return new Token(TokenCode.DBLLIT, lex, line, temp);
					}
					else{
						String lex = buffer.toString();
						return new Token(TokenCode.DBLLIT, lex, line, temp);
					}
				}
				
						
			}	
			if( nextC == '.'){
				c = nextChar();
				buffer.append((char) c );
				if(isDigit(nextC)){
					while(isDigit(nextC)) {
						c = nextChar();
						buffer.append((char) c);
					}
					String lex = buffer.toString();
					return new Token(TokenCode.DBLLIT, lex, line, temp);
				}
				else{
					String lex = buffer.toString();
					return new Token(TokenCode.DBLLIT,lex, line, temp);
				}
			}	
			if(isDigit(nextC)){
				c = nextChar();
				buffer.append((char) c);
				if(isDigit(nextC)){
					while(isDigit(nextC) || nextC == '.'){
						c = nextChar();
						buffer.append((char) c);
						if(c == '.'){
							check = true;
						}
					}
					String lex = buffer.toString();
					//toCheck = Long.decode(lex);
					if(check)
						return new Token(TokenCode.DBLLIT, lex, line, temp);
					else if(!validDBL(lex))
						throw new LexError("at ("+line+","+temp+"). Invalid decimal literal: "+lex);
					else
						return new Token(TokenCode.INTLIT, lex, line, temp);
				}
			}
		}
		String lex = buffer.toString();
		toCheck = Long.decode(lex);	
		if(!validInt(toCheck))
			throw new Exception("Lex error column "+temp+" and at line "+line+" please fix it.");
		else 
			return new Token(TokenCode.INTLIT, lex, line, temp);
		
	}
	//Recognizing operators
	switch (c) {
	
	case '+':
		return new Token(TokenCode.ADD, "+", line, column);
	case '-':
		return new Token(TokenCode.SUB, "-", line, column);
	case '*':
		return new Token(TokenCode.MUL, "*", line, column);
	case '/':
		return new Token(TokenCode.DIV, "/", line, column);
	case '&':
		if(nextC == '&'){
			c = nextChar();
			return new Token(TokenCode.AND, "&&", line, column-1);
		}
		break;
	case '|':
		if(nextC == '|'){
			c = nextChar();
			return new Token(TokenCode.OR, "||", line, column-1);
		}
		break;
	case '!':
		if(nextC == '='){
			c = nextChar();
			return new Token(TokenCode.NE, "!=", line, column-1);
		}
		return new Token(TokenCode.NOT, "!", line, column);
	case '=':
		if(nextC == '='){
			c = nextChar();
			return new Token(TokenCode.EQ, "==", line, column-1);
		}
		return new Token(TokenCode.ASSGN, "=", line, column);
	case '<':
		if(nextC == '='){
			c = nextChar();
			return new Token(TokenCode.LE, "<=", line, column-1);
		}
		return new Token(TokenCode.LT, "<", line, column);
	case '>':
		if(nextC == '='){
			c = nextChar();
			return new Token(TokenCode.GE, ">=", line, column-1);
		}
		return new Token(TokenCode.GT, ">", line, column);
	case ';':
		return new Token(TokenCode.SEMI, ";", line, column);
	case ',':
		return new Token(TokenCode.COMMA, ",", line, column);
	case '(':
		return new Token(TokenCode.LPAREN,"(", line, column);
	case ')':
		return new Token(TokenCode.RPAREN, ")", line, column);
	case '[':
		return new Token(TokenCode.LBRAC, "[", line, column);
	case ']':
		return new Token(TokenCode.RBRAC, "]", line, column);
	case '{':
		return new Token(TokenCode.LCURLY, "{", line, column);
	case '}':
		return new Token(TokenCode.RCURLY, "}", line, column);
	case '.':
		if(isDigit(nextC)){
			int temp = column;
			StringBuilder buffer = new StringBuilder();
			buffer.append((char) c);
			c = nextChar();
			while(isDigit(nextC)){
				buffer.append((char) c);
				c = nextChar();
			}
			buffer.append((char) c);
			String lex = buffer.toString();
			return new Token(TokenCode.DBLLIT, lex, line, temp);
		}
		else
			return new Token(TokenCode.DOT, ".", line, column);
	case '"':
		int temp = column;
		StringBuilder buffer = new StringBuilder();
		c = nextChar();

		if(c == '"')
			return new Token(TokenCode.STRLIT, "", line, temp);
		buffer.append((char) c);	
		while(c != '"'){
			c = nextChar();	
			if(c == '"'){
				String lex = buffer.toString();
				return new Token(TokenCode.STRLIT, lex, line, temp);
			}
			if(c == '\r'){
				String lex = buffer.toString();
				throw new LexError("at ("+line+","+temp+"). Ill-formed or unclosed string: \""+lex);
			}
			if(nextC == '\n'){
				buffer.append((char) c);
				String lex = buffer.toString();
				throw new LexError("at ("+line+","+temp+"). Ill-formed or unclosed string: \""+lex);
			}
			buffer.append((char) c);
		}
		
}
	throw new LexError("at ("+line+","+column+"). Illegal character: "+(char)c);
}
	public static class LexError extends Exception{
		public LexError(String message){
			super(message);
		}
	}


}
