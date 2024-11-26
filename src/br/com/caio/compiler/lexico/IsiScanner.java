package br.com.caio.compiler.lexico;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import br.com.caio.compiler.exceptions.LexicalException;

// Classe responsável por realizar a análise léxica de um arquivo de texto.
public class IsiScanner {

	// Atributos para armazenar o conteúdo do arquivo, o estado do analisador e a
	// posição atual.
	private char[] content; // Conteúdo do arquivo convertido para um array de caracteres.
	private int status; // Representa o estado atual do analisador léxico (usado no autômato).
	private int position; // Posição atual do analisador no conteúdo do arquivo.

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
		Token token = new Token(); // Cria um novo token.
		String term = "";
		status = 0; // Inicializa o estado do analisador.

		// Loop para processar os caracteres até identificar um token.
		while (true) {
			// Verifica novamente se não está no final do arquivo antes de avançar.
			if (isEndOfFile()) {
				return null;
			}

			currentChar = nextChar(); // Obtém o próximo caractere.

			switch (status) {
			case 0: // Estado inicial.
				if (isChar(currentChar)) { // Verifica se o caractere é uma letra.
					term += currentChar;
					status = 1; // Transita para o estado de identificadores.
				} else if (isNumber(currentChar)) { // Verifica se é um número.
					term += currentChar;
					status = 3; // Estado para números (ainda não implementado).
				} else if (isSpace(currentChar)) { // Ignora espaços.
					status = 0; // Permanece no estado inicial.
				} else if (currentChar == '"') {
					status = 5;
				} else if (isDelimiter(currentChar)) { // Verifica delimitadores.
					token.setType(Token.TK_DELIMITER);
					token.setText(String.valueOf(currentChar));
					return token;
				} else if (isPunctuation(currentChar)) { // Verifica pontuações.
					token.setType(Token.TK_PONCTUATION);
					token.setText(String.valueOf(currentChar));
					return token;
				} else if (isOperator(currentChar)) {
					
					//QUERO QUE OS OPERADORES SEJAM MAIS ESPECIFICOS, QUE O COMPILADOR IDENTFIQUE ++, !, && E ETC
					
					char next = nextChar(); // Olha o próximo caractere.
					// Verificando operadores compostos de comparação
				    if (isComparisonOperator(currentChar, next)) {
				        token.setType(Token.TK_COMPARISON_OPERATOR);
				        token.setText("" + currentChar + next); // Exemplo: ==, !=, <=, >=
				        return token;
				    }

				    // Verificando operadores compostos de atribuição
				    else if (isAssignmentOperator(currentChar, next)) {
				        token.setType(Token.TK_ASSIGNMENT_OPERATOR); // Atribuição com operador (ex.: +=, -=, *=)
				        token.setText("" + currentChar + next); // Exemplo: +=
				        return token;
				    }

				    // Verificando operadores aritméticos simples
				    else if (isArithmeticOperator(currentChar)) {
				        token.setType(Token.TK_ARITHMETIC_OPERATOR);
				        token.setText(String.valueOf(currentChar)); // Exemplo: +, -, *, /
				        return token;
				    }

				    // Verificando operadores lógicos
				    else if (isLogicalOperator(currentChar)) {
				        token.setType(Token.TK_LOGICAL_OPERATOR);
				        token.setText(String.valueOf(currentChar)); // Exemplo: &&, ||, !
				        return token;
				    }

				    // Se não for nenhum dos anteriores, trata como operador genérico
				    else {
				        token.setType(Token.TK_OPERATOR);
				        token.setText(String.valueOf(currentChar));
				        return token;
				    }
				}
				break;
			case 1: // Estado para identificadores.
				if (isChar(currentChar) || isNumber(currentChar)) {
					term += currentChar;
					status = 1; // Continua no mesmo estado enquanto caracteres válidos são encontrados.
				} else {
					back();
					back(); // Volta uma posição porque o caractere atual não faz parte do token.
					status = 2; // Transita para o estado de finalização do identificador.
				}
				break;
			case 2: // Estado final para identificadores.
				if (isReservedWord(term)) {
					token.setType(Token.TK_RESERVED);
				} else {
					token.setType(Token.TK_IDENTIFIER); // Define o tipo como identificador.
				}
				token.setText(term);
				return token; // Retorna o token identificado.
			case 3:
				if (isNumber(currentChar)) {
					term += currentChar;
					status = 3;
				} else if (!isChar(currentChar)) {
					status = 4;
				} else {
					throw new LexicalException("Numero Mal formado");
				}
				break;
			case 4:
				token.setType(Token.TK_NUMBER);
				token.setText(term);
				return token;
			case 5: // Estado para string literals
				if (currentChar != '"') {
					term += currentChar;
				} else {
					token.setType(Token.TK_STRING);
					token.setText(term);
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
}
