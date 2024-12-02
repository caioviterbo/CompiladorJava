package br.com.caio.compiler.lexico;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.caio.compiler.exceptions.LexicalException;


public class IsiScanner {


	private char[] content; 
	private int status; 
	private int position; 

	private List<Token> tokens = new ArrayList<>();
	private Map<String, Symbol> symbolTable = new LinkedHashMap<>();
	private int symbolOrder = 1;
	
	public IsiScanner(String filename) {
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(filename));
			String txtContent = new String(bytes, StandardCharsets.UTF_8);
			System.out.println("DEBUG----"); 
			System.out.println(txtContent);
			content = txtContent.toCharArray(); 
			position = 0; 
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Token nextToken() {
	    char currentChar;
	    Token token = new Token();
	    String term = "";
	    status = 0;

	    while (true) {
	        if (isEndOfFile()) {
	            return null;
	        }

	        currentChar = nextChar();

	        switch (status) {
	        case 0:
	            if (isChar(currentChar)) {
	                term += currentChar;
	                status = 1;
	            } else if (isNumber(currentChar)) {
	                term += currentChar;
	                status = 3;
	            } else if (isSpace(currentChar)) {
	                status = 0;
	            } else if (currentChar == '"') {
	                status = 5;
	            } else if (isDelimiter(currentChar)) {
	                token.setType(Token.TK_DELIMITER);
	                token.setText(String.valueOf(currentChar));
	                tokens.add(token); 
	                return token;
	            } else if (isPunctuation(currentChar)) {
	                token.setType(Token.TK_PONCTUATION);
	                token.setText(String.valueOf(currentChar));
	                tokens.add(token); 
	                return token;
	            } else if (isOperator(currentChar)) {
	                char next = nextChar();
	                if (isComparisonOperator(currentChar, next)) {
	                    token.setType(Token.TK_COMPARISON_OPERATOR);
	                    token.setText("" + currentChar + next);
	                    tokens.add(token); 
	                    return token;
	                } else if (isAssignmentOperator(currentChar, next)) {
	                    token.setType(Token.TK_ASSIGNMENT_OPERATOR);
	                    token.setText("" + currentChar + next);
	                    tokens.add(token); 
	                    return token;
	                } else if (isArithmeticOperator(currentChar)) {
	                    token.setType(Token.TK_ARITHMETIC_OPERATOR);
	                    token.setText(String.valueOf(currentChar));
	                    tokens.add(token); 
	                    return token;
	                } else if (isLogicalOperator(currentChar)) {
	                    token.setType(Token.TK_LOGICAL_OPERATOR);
	                    token.setText(String.valueOf(currentChar));
	                    tokens.add(token); 
	                    return token;
	                } else {
	                    token.setType(Token.TK_OPERATOR);
	                    token.setText(String.valueOf(currentChar));
	                    tokens.add(token); 
	                    return token;
	                }
	            }
	            break;
	        case 1:
	            if (isChar(currentChar) || isNumber(currentChar)) {
	                term += currentChar;
	                status = 1;
	            } else {
	            	back();
	                back();
	                status = 2;
	            }
	            break;
	        case 2:
	            if (isReservedWord(term)) {
	                token.setType(Token.TK_RESERVED);
	                addSymbol(term);
	            } else {
	                token.setType(Token.TK_IDENTIFIER);
	                addSymbol(term); 
	            }
	            token.setText(term);
	            tokens.add(token); 
	            return token;
	        case 3:
	            if (isNumber(currentChar)) {
	                term += currentChar;
	                status = 3;
	            } else if (!isChar(currentChar)) {
	                status = 4;
	            } else {
	                throw new LexicalException("Numero mal formado");
	            }
	            break;
	        case 4:
	            token.setType(Token.TK_NUMBER);
	            token.setText(term);
	            addSymbol(term);
	            tokens.add(token); 
	            return token;
	        case 5:
	            if (currentChar != '"') {
	                term += currentChar;
	            } else {
	                token.setType(Token.TK_STRING);
	                token.setText(term);
	                addSymbol(term);
	                tokens.add(token); 
	                return token;
	            }
	            break;
	        }
	    }
	}


	private boolean isNumber(char c) {
		return c >= '0' && c <= '9'; 
	}

	private boolean isChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private boolean isOperator(char c) {
		return c == '>' || c == '<' || c == '=' || c == '!' || c == '+' || c == '-' || c == '*' || c == '/'
				|| c == '!' || c == '&' ; 
	}

	
	private boolean isComparisonOperator(char currentChar, char nextChar) {
	    return (currentChar == '=' || currentChar == '!' || currentChar == '<' || currentChar == '>')
	            && nextChar == '=';
	}

	private boolean isAssignmentOperator(char currentChar, char nextChar) {
	    return (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/')
	            && nextChar == '=';
	}

	private boolean isArithmeticOperator(char currentChar) {
	    return currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/';
	}

	private boolean isLogicalOperator(char currentChar) {
	    return currentChar == '&' || currentChar == '|' || currentChar == '!';
	}

	private boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r'; 
	}

	private boolean isDelimiter(char c) {
		return c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']';
	}

	private boolean isPunctuation(char c) {
		return c == ',' || c == ';' || c == '.';
	}

	private boolean isReservedWord(String text) {
		return text.equals("public") || text.equals("static") || text.equals("void") || text.equals("float")
				|| text.equals("System") || text.equals("out") || text.equals("println");
	}

	private char nextChar() {
		if (position < content.length) {
			return content[position++];
		} else {
			throw new ArrayIndexOutOfBoundsException("Tentativa de acessar índice fora do limite do conteúdo.");
		}
	}

	private boolean isEndOfFile() {
		return position == content.length;
	}

	
	private void back() {
		position--;
	}
	
	private void addSymbol(String term) {
		if (!symbolTable.containsKey(term)) {
			symbolTable.put(term, new Symbol(term, 1, symbolOrder++));
		} else {
			Symbol symbol = symbolTable.get(term);
			symbol.incrementQuantity();
		}
	}
		public void printTokens() {
			for (Token token: tokens) {
				 String typeName = getTokenTypeName(token.getType());
				System.out.printf("Token [type=%s, text='%s']%n", typeName, token.getText());
			}
	}
		public void printSymbolTable() {
			for (Symbol symbol : symbolTable.values()) {
				System.out.println(symbol);
			}
		}
		
		private static String getTokenTypeName(int type) {
		    return switch (type) {
		        case 0 -> "IDENTIFICADOR";
		        case 1 -> "NUMERO";
		        case 2 -> "OPERADOR_ATRIBUIÇÃO";
		        case 3 -> "PONTUAÇÃO";
		        case 5 -> "PALAVRA_RESERVADA";
		        case 7 -> "DELIMITADOR";
		        case 8 -> "OPERADOR_ARITIMETICO";
		        default -> "UNKNOWN";
		    };
		}

}
