package com.udea.physhdsl.interpreter.ast;

import java.util.Map;

public class Assign implements ASTNode {

	private String key;
	private ASTNode value;

	public Assign(String key, ASTNode value) {
		super();
		this.key = key;
		this.value = value;
	}

	@Override
	public Object execute(Map<String, Object> symbolTable) throws Exception {
		try {
//			System.out.printf("%s: %s\n", key, String.valueOf(value.execute(symbolTable)));
			if(symbolTable.containsKey(key)) {
				throw new Exception("Duplicate key " + key + ": " + value.execute(null));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		symbolTable.put(key, value.execute(symbolTable));
		return null;
	}

}
