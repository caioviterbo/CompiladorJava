package br.com.caio.compiler.lexico;


public class Token {

	public static final int TK_IDENTIFIER = 0; 
	public static final int TK_NUMBER = 1; 
	public static final int TK_OPERATOR = 2; 
	public static final int TK_PONCTUATION = 3; 
	public static final int TK_RESERVED = 5;
	public static final int TK_STRING = 6;
	public static final int TK_DELIMITER = 7;
	public static final int TK_ARITHMETIC_OPERATOR = 8; 
	public static final int TK_COMPARISON_OPERATOR = 9; 
	public static final int TK_LOGICAL_OPERATOR = 10;   
	public static final int TK_ASSIGNMENT_OPERATOR = 11; 
	public static final int TK_INCREMENT_OPERATOR = 12;  
	public static final int TK_DECREMENT_OPERATOR = 13; 

	

	private int type; 
	private String text; 
	
	
	public Token(int type, String text) { 
		super(); 
		this.type = type; 
		this.text = text; 
	}

	public Token() {
		super(); 
	}
	
	
	public int getType() { 
		return type;
	}
	
	public void setType(int type) { 
		this.type = type;
	}
	
	
	public String getText() { 
		return text;
	}
	
	public void setText(String text) { 
		this.text = text;
	}
	
	@Override
    public String toString() {
        return "Token [type=" + type + ", text='" + text + "']";
    }
}
