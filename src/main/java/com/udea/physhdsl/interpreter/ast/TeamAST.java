package com.udea.physhdsl.interpreter.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.udea.physhdsl.Pool;
import com.udea.physhdsl.Team;
import com.udea.physhdsl.Worker;

public class TeamAST implements ASTNode {

	private ASTNode quantity;
	private List<ASTNode> workersAST;
	private List<ASTNode> poolsAST;

	public TeamAST(ASTNode quantity, List<ASTNode> workersAST, List<ASTNode> poolsAST) {
		super();
		this.quantity = quantity;
		this.workersAST = workersAST;
		this.poolsAST = poolsAST;
	}

	@Override
	public Object execute(Map<String, Object> symbolTable) throws Exception {
		List<Team> teams = new ArrayList<Team>();
		
		for (int i = 0; i < (int) this.quantity.execute(null); i++) {
			List<Worker> workers = new ArrayList<Worker>();
			List<Pool> pools = new ArrayList<Pool>();
			for (ASTNode worker : workersAST) {
				workers.addAll((ArrayList<Worker>) worker.execute(symbolTable));
			}
			for (ASTNode pool : poolsAST) {
				pools.add((Pool)pool.execute(symbolTable));
			}
			teams.add(new Team(workers, pools));
		}

		return teams;
	}

}
