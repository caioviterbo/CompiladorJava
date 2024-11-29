package br.com.caio.compiler.lexico;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.caio.compiler.exceptions.LexicalException;

// Classe responsável por realizar a análise léxica de um arquivo de texto.
public class IsiScanner {

	// Atributos para armazenar o conteúdo do arquivo, o estado do analisador e a
	// posição atual.
	private char[] content; // Conteúdo do arquivo convertido para um array de caracteres.
	private int status; // Representa o estado atual do analisador léxico (usado no autômato).
	private int position; // Posição atual do analisador no conteúdo do arquivo.

	private List<Token> tokens = new ArrayList<>();
	private Map<String, Symbol> symbolTable = new LinkedHashMap<>();
	private int symbolOrder = 1;
	
	// Construtor que recebe o caminho do arquivo e inicializa o conteúdo.
	public IsiScanner(String filename) {
		try {
			// Lê o conteúdo do arquivo em bytes e converte para uma string usando UTF-8.
			byte[] bytes = Files.readAllBytes(Paths.get(filename));
			String txtContent = new String(bytes, StandardCharsets.UTF_8);
			System.out.println("DEBUG----"); // Exibe o conteúdo do arquivo (para debug).
			System.out.println(txtContent);
			content = txtContent.toCharArray(); // Converte o conteúdo em um array de caracteres.
			position = 0; // Inicializa a posição no início do conteúdo.
		} catch (Exception ex) {
			// Trata exceções relacionadas à leitura do arquivo.
			ex.printStackTrace();
		}
	}

	// Método principal que retorna o próximo token encontrado no conteúdo.
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
	                tokens.add(token); // Armazena o token.
	                return token;
	            } else if (isPunctuation(currentChar)) {
	                token.setType(Token.TK_PONCTUATION);
	                token.setText(String.valueOf(currentChar));
	                tokens.add(token); // Armazena o token.
	                return token;
	            } else if (isOperator(currentChar)) {
	                char next = nextChar();
	                if (isComparisonOperator(currentChar, next)) {
	                    token.setType(Token.TK_COMPARISON_OPERATOR);
	                    token.setText("" + currentChar + next);
	                    tokens.add(token); // Armazena o token.
	                    return token;
	                } else if (isAssignmentOperator(currentChar, next)) {
	                    token.setType(Token.TK_ASSIGNMENT_OPERATOR);
	                    token.setText("" + currentChar + next);
	                    tokens.add(token); // Armazena o token.
	                    return token;
	                } else if (isArithmeticOperator(currentChar)) {
	                    token.setType(Token.TK_ARITHMETIC_OPERATOR);
	                    token.setText(String.valueOf(currentChar));
	                    tokens.add(token); // Armazena o token.
	                    return token;
	                } else if (isLogicalOperator(currentChar)) {
	                    token.setType(Token.TK_LOGICAL_OPERATOR);
	                    token.setText(String.valueOf(currentChar));
	                    tokens.add(token); // Armazena o token.
	                    return token;
	                } else {
	                    token.setType(Token.TK_OPERATOR);
	                    token.setText(String.valueOf(currentChar));
	                    tokens.add(token); // Armazena o token.
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
	                addSymbol(term); // Adiciona à tabela de símbolos.
	            }
	            token.setText(term);
	            tokens.add(token); // Armazena o token.
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
	            tokens.add(token); // Armazena o token.
	            return token;
	        case 5:
	            if (currentChar != '"') {
	                term += currentChar;
	            } else {
	                token.setType(Token.TK_STRING);
	                token.setText(term);
	                addSymbol(term);
	                tokens.add(token); // Armazena o token.
	                return token;
	            }
	            break;
	        }
	    }
	}


	// Métodos auxiliares para identificar tipos de caracteres.
	private boolean isNumber(char c) {
		return c >= '0' && c <= '9'; // Verifica se o caractere é um número.
	}

	private boolean isChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); // Verifica se é uma letra.
	}

	private boolean isOperator(char c) {
		return c == '>' || c == '<' || c == '=' || c == '!' || c == '+' || c == '-' || c == '*' || c == '/'
				|| c == '!' || c == '&' ; // Verifica se é um operador.
	}

	// Verifica se o operador é de comparação (==, !=, <=, >=)
	private boolean isComparisonOperator(char currentChar, char nextChar) {
	    return (currentChar == '=' || currentChar == '!' || currentChar == '<' || currentChar == '>')
	            && nextChar == '=';
	}

	// Verifica se o operador é de atribuição (+=, -=, *=, /=)
	private boolean isAssignmentOperator(char currentChar, char nextChar) {
	    return (currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/')
	            && nextChar == '=';
	}

	// Verifica se o operador é aritmético (como +, -, *, /)
	private boolean isArithmeticOperator(char currentChar) {
	    return currentChar == '+' || currentChar == '-' || currentChar == '*' || currentChar == '/';
	}

	// Verifica se o operador é lógico (como &&, ||, !)
	private boolean isLogicalOperator(char currentChar) {
	    return currentChar == '&' || currentChar == '|' || currentChar == '!';
	}

	private boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r'; // Verifica se é um espaço ou caractere de controle.
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

	// Obtém o próximo caractere do conteúdo e avança a posição.
	private char nextChar() {
		if (position < content.length) {
			return content[position++];
		} else {
			throw new ArrayIndexOutOfBoundsException("Tentativa de acessar índice fora do limite do conteúdo.");
		}
	}

	// Verifica se o analisador chegou ao final do arquivo.
	private boolean isEndOfFile() {
		return position == content.length;
	}

	// Retrocede uma posição no conteúdo.
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
