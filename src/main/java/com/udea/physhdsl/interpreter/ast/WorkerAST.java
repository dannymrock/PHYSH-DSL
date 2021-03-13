package com.udea.physhdsl.interpreter.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.udea.physhdsl.Worker;

public class WorkerAST implements ASTNode {

	private ASTNode quantity;
	private List<ASTNode> body;

	public WorkerAST(ASTNode quantity, List<ASTNode> body) {
		super();
		this.quantity = quantity;
		this.body = body;
	}

	@Override
	public Object execute(Map<String, Object> symbolTable) throws Exception {
		List<Worker> workers = new ArrayList<Worker>();
		for (int i = 0; i < (int) this.quantity.execute(null); i++) {
			Map<String, Object> localSymbolTable = new HashMap<String, Object>();
			for (ASTNode assign : body) {
				assign.execute(localSymbolTable);
			}
			workers.add(formatWorker(localSymbolTable));
		}
		return workers;
	}

	private Worker formatWorker(Map<String, Object> mapObject) {
		validateData(mapObject);

		String metaheuristicType = String.valueOf(mapObject.get("MH"));
		int poolRequestId = (int) mapObject.get("requestPool");
		int poolUpdateId = (int) mapObject.get("updatePool");
//		double seed = Double.valueOf(String.valueOf(mapObject.get("seed")).toString());

		return new Worker(metaheuristicType, poolRequestId, poolUpdateId);
	}

	private void validateData(Map<String, Object> mapObject) {
		try {
			if (mapObject.get("MH") == null) {
				throw new Exception("Worker: MH is Expected");
			} else if (mapObject.get("requestPool") == null) {
				//throw new Exception("Worker: requestPool is Expected");
			} else if (mapObject.get("updatePool") == null) {
				//throw new Exception("Worker: updatePool is Expected");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
