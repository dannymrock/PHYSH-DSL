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

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DSLParallelMetaheuristicParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DSLParallelMetaheuristicVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DSLParallelMetaheuristicParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(DSLParallelMetaheuristicParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link DSLParallelMetaheuristicParser#team}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTeam(DSLParallelMetaheuristicParser.TeamContext ctx);
	/**
	 * Visit a parse tree produced by {@link DSLParallelMetaheuristicParser#worker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorker(DSLParallelMetaheuristicParser.WorkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DSLParallelMetaheuristicParser#pool}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPool(DSLParallelMetaheuristicParser.PoolContext ctx);
	/**
	 * Visit a parse tree produced by {@link DSLParallelMetaheuristicParser#assign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssign(DSLParallelMetaheuristicParser.AssignContext ctx);
}