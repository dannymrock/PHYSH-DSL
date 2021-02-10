package com.udea.physhdsl.interpreter.ast;

import java.util.Map;

public interface ASTNode {
	public Object execute(Map<String, Object> symbolTable) throws Exception;
}
