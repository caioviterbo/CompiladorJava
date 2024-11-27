package br.com.caio.compiler.lexico;

public class Symbol {
	private String name;
	private int quantity;
	private int order;
	
	public Symbol(String name, int quantity, int order) {
		this.name = name;
		this.quantity = quantity;
		this.order = order;
	}
	
	public void incrementQuantity() {
		this.quantity++;
	}
	
	@Override
	public String toString() {
		return String.format("Símbolo: %s, Quantidade: %d, Ordem: %d", name, quantity, order);
	}
}
