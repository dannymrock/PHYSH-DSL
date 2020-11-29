package com.udea.degreework.interpreter.ast;

import java.util.List;
import java.util.Map;

public class Team implements ASTNode {

	private ASTNode quantity;
	private List<ASTNode> body;

	public Team(ASTNode quantity, List<ASTNode> body) {
		super();
		this.quantity = quantity;
		this.body = body;
	}

	@Override
	public Object execute(Map<String, Object> symbolTable) throws Exception {

		for (int i = 0; i < (int) this.quantity.execute(null); i++) {
			for (ASTNode n : body) {
				n.execute(symbolTable);
			}
		}

		return null;
	}

}
