package br.com.caio.compiler.main;

import br.com.caio.compiler.exceptions.LexicalException;
import br.com.caio.compiler.lexico.IsiScanner;
import br.com.caio.compiler.lexico.Token;

public class MainClass {
	public static void main(String[] args) {
		try {
			IsiScanner sc = new IsiScanner("input.isi");
			Token token = null;
			
			do {
				token = sc.nextToken();
				if (token != null) {
					System.out.println(token);
				}
			} while (token != null);
			
			 System.out.println("\n--- LISTA DOS TOKENS ---");
	            sc.printTokens();

	            System.out.println("\n--- TABELA DE SÍMBOLOS ---");
	            sc.printSymbolTable();
	            
		} catch(LexicalException ex) {
			System.out.println("Lexical ERROR " +ex.getMessage());
			
		} catch (Exception ex) {
			System.out.println("Generic ERROR");
		}
		
	}

}
