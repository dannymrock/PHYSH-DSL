
package com.udea.physhdsl;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.udea.degreework.DSLParallelMetaheuristicLexer;
import com.udea.degreework.DSLParallelMetaheuristicParser;
import com.udea.degreework.DSLParallelMetaheuristicParser.StartContext;

public class Main {
	private static final Logger LOGGER = Logger.getLogger( Main.class.getName() );
	

	private static final String EXTENSION = "pmet";

	public static void main(String[] args) throws Exception {
		String program = args.length > 1 ? args[1] : "test/test." + EXTENSION;
		
		// set logger level 
		Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(Level.ALL));
		
		LOGGER.log(Level.INFO,"Interpreting file " + program);
		
		DSLParallelMetaheuristicLexer lexer = new DSLParallelMetaheuristicLexer(new ANTLRFileStream(program));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DSLParallelMetaheuristicParser parser = new DSLParallelMetaheuristicParser(tokens);

		DSLParallelMetaheuristicParser.StartContext tree = parser.start();

		DSLParallelMetaheuristicCustomVisitor visitor = new DSLParallelMetaheuristicCustomVisitor();
		visitor.visit(tree);

		LOGGER.log(Level.INFO,"Interpretation finished");

	}

}
