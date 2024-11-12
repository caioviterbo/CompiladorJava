package br.com.caio.compiler.lexico;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// Classe responsável por realizar a análise léxica de um arquivo de texto.
public class IsiScanner {
	
	// Atributos para armazenar o conteúdo do arquivo, o estado do analisador e a posição atual.
	private char[] content; // Conteúdo do arquivo convertido para um array de caracteres.
	private int status; // Representa o estado atual do analisador léxico (usado no autômato).
	private int position; // Posição atual do analisador no conteúdo do arquivo.
	
	// Construtor que recebe o caminho do arquivo e inicializa o conteúdo.
	public IsiScanner(String filename) {
		try {
			// Lê o conteúdo do arquivo em bytes e converte para uma string usando UTF-8.
			byte[] bytes = Files.readAllBytes(Paths.get(filename));
			String txtContent = new String(bytes, StandardCharsets.UTF_8);
			System.out.println("txtConteudo" + txtContent); // Exibe o conteúdo do arquivo (para debug).
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
		
		// Verifica se já chegou ao final do arquivo.
		if (isEndOfFile()) {
			return null; // Retorna nulo caso não haja mais tokens.
		}
		
		status = 0; // Inicializa o estado do analisador.
		
		// Loop para processar os caracteres até identificar um token.
		while (true) {
			currentChar = nextChar(); // Obtém o próximo caractere.
			
			switch (status) {
				case 0: // Estado inicial.
					if (isChar(currentChar)) { // Verifica se o caractere é uma letra.
						status = 1; // Transita para o estado de identificadores.
					} else if (isNumber(currentChar)) { // Verifica se é um número.
						status = 3; // Estado para números (ainda não implementado).
					} else if (isSpace(currentChar)) { // Ignora espaços.
						status = 0; // Permanece no estado inicial.
					} 
					break;
				case 1: // Estado para identificadores.
					if (isChar(currentChar) || isNumber(currentChar)) {
						status = 1; // Continua no mesmo estado enquanto caracteres válidos são encontrados.
					} else {
						status = 2; // Transita para o estado de finalização do identificador.
					}
					break;
				case 2: // Estado final para identificadores.
					Token token = new Token(); // Cria um novo token.
					token.setType(Token.TK_IDENTIFIER); // Define o tipo como identificador.
					return token; // Retorna o token identificado.
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
		return c == '>' || c == '<' || c == '=' || c == '!'; // Verifica se é um operador.
	}
	
	private boolean isSpace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r'; // Verifica se é um espaço ou caractere de controle.
	}
	
	// Obtém o próximo caractere do conteúdo e avança a posição.
	private char nextChar() {
		return content[position++];
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
