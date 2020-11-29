// Generated from com/udea/degreework/DSLParallelMetaheuristic.g4 by ANTLR 4.5.1
package com.udea.degreework;

	import java.util.Map;
	import java.util.HashMap;

	import com.udea.degreework.interpreter.ast.ASTNode;
	import com.udea.degreework.interpreter.ast.Assign;
	import com.udea.degreework.interpreter.ast.Constant;
	import com.udea.degreework.interpreter.ast.Pool;
	import com.udea.degreework.interpreter.ast.Worker;
	import com.udea.degreework.interpreter.ast.Team;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

/**
 * This class provides an empty implementation of {@link DSLParallelMetaheuristicVisitor},
 * which can be extended to create a visitor which only needs to handle a subset
 * of the available methods.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public class DSLParallelMetaheuristicBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements DSLParallelMetaheuristicVisitor<T> {
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitStart(DSLParallelMetaheuristicParser.StartContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitTeam(DSLParallelMetaheuristicParser.TeamContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitWorker(DSLParallelMetaheuristicParser.WorkerContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitPool(DSLParallelMetaheuristicParser.PoolContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitAssign(DSLParallelMetaheuristicParser.AssignContext ctx) { return visitChildren(ctx); }
}