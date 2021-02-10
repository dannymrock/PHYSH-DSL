// Generated from com/udea/degreework/DSLParallelMetaheuristic.g4 by ANTLR 4.5.1
package com.udea.degreework;

	import java.util.Map;
	import java.util.HashMap;

import com.udea.physhdsl.Execution;
import com.udea.physhdsl.Team;
import com.udea.physhdsl.interpreter.ast.ASTNode;
import com.udea.physhdsl.interpreter.ast.Assign;
import com.udea.physhdsl.interpreter.ast.Constant;
import com.udea.physhdsl.interpreter.ast.PoolAST;
import com.udea.physhdsl.interpreter.ast.TeamAST;
import com.udea.physhdsl.interpreter.ast.WorkerAST;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DSLParallelMetaheuristicParser}.
 */
public interface DSLParallelMetaheuristicListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DSLParallelMetaheuristicParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(DSLParallelMetaheuristicParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link DSLParallelMetaheuristicParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(DSLParallelMetaheuristicParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link DSLParallelMetaheuristicParser#team}.
	 * @param ctx the parse tree
	 */
	void enterTeam(DSLParallelMetaheuristicParser.TeamContext ctx);
	/**
	 * Exit a parse tree produced by {@link DSLParallelMetaheuristicParser#team}.
	 * @param ctx the parse tree
	 */
	void exitTeam(DSLParallelMetaheuristicParser.TeamContext ctx);
	/**
	 * Enter a parse tree produced by {@link DSLParallelMetaheuristicParser#worker}.
	 * @param ctx the parse tree
	 */
	void enterWorker(DSLParallelMetaheuristicParser.WorkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DSLParallelMetaheuristicParser#worker}.
	 * @param ctx the parse tree
	 */
	void exitWorker(DSLParallelMetaheuristicParser.WorkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DSLParallelMetaheuristicParser#pool}.
	 * @param ctx the parse tree
	 */
	void enterPool(DSLParallelMetaheuristicParser.PoolContext ctx);
	/**
	 * Exit a parse tree produced by {@link DSLParallelMetaheuristicParser#pool}.
	 * @param ctx the parse tree
	 */
	void exitPool(DSLParallelMetaheuristicParser.PoolContext ctx);
	/**
	 * Enter a parse tree produced by {@link DSLParallelMetaheuristicParser#assign}.
	 * @param ctx the parse tree
	 */
	void enterAssign(DSLParallelMetaheuristicParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link DSLParallelMetaheuristicParser#assign}.
	 * @param ctx the parse tree
	 */
	void exitAssign(DSLParallelMetaheuristicParser.AssignContext ctx);
}