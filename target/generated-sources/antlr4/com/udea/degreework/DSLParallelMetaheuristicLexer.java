// Generated from com/udea/degreework/DSLParallelMetaheuristic.g4 by ANTLR 4.5.1
package com.udea.degreework;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DSLParallelMetaheuristicLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EXECUTION=1, TEAM=2, WORKER=3, POOL=4, OPEN_CURLY_BRACKET=5, CLOSE_CURLY_BRACKET=6, 
		GREATER_THAN=7, LESS_THAN=8, NUMBER_SIGN=9, COLON=10, COMMA=11, NUMBER=12, 
		STRING=13, WS=14;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"EXECUTION", "TEAM", "WORKER", "POOL", "OPEN_CURLY_BRACKET", "CLOSE_CURLY_BRACKET", 
		"GREATER_THAN", "LESS_THAN", "NUMBER_SIGN", "COLON", "COMMA", "NUMBER", 
		"STRING", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'Execution'", "'Team'", "'Worker'", "'Pool'", "'{'", "'}'", "'>'", 
		"'<'", "'#'", "':'", "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "EXECUTION", "TEAM", "WORKER", "POOL", "OPEN_CURLY_BRACKET", "CLOSE_CURLY_BRACKET", 
		"GREATER_THAN", "LESS_THAN", "NUMBER_SIGN", "COLON", "COMMA", "NUMBER", 
		"STRING", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public DSLParallelMetaheuristicLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DSLParallelMetaheuristic.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\20Y\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5"+
		"\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r"+
		"\6\rJ\n\r\r\r\16\rK\3\16\6\16O\n\16\r\16\16\16P\3\17\6\17T\n\17\r\17\16"+
		"\17U\3\17\3\17\2\2\20\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27"+
		"\r\31\16\33\17\35\20\3\2\5\3\2\62;\7\2//\62;C\\aac|\5\2\13\f\17\17\"\""+
		"[\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\3\37\3\2\2\2\5)\3\2\2\2\7.\3\2"+
		"\2\2\t\65\3\2\2\2\13:\3\2\2\2\r<\3\2\2\2\17>\3\2\2\2\21@\3\2\2\2\23B\3"+
		"\2\2\2\25D\3\2\2\2\27F\3\2\2\2\31I\3\2\2\2\33N\3\2\2\2\35S\3\2\2\2\37"+
		" \7G\2\2 !\7z\2\2!\"\7g\2\2\"#\7e\2\2#$\7w\2\2$%\7v\2\2%&\7k\2\2&\'\7"+
		"q\2\2\'(\7p\2\2(\4\3\2\2\2)*\7V\2\2*+\7g\2\2+,\7c\2\2,-\7o\2\2-\6\3\2"+
		"\2\2./\7Y\2\2/\60\7q\2\2\60\61\7t\2\2\61\62\7m\2\2\62\63\7g\2\2\63\64"+
		"\7t\2\2\64\b\3\2\2\2\65\66\7R\2\2\66\67\7q\2\2\678\7q\2\289\7n\2\29\n"+
		"\3\2\2\2:;\7}\2\2;\f\3\2\2\2<=\7\177\2\2=\16\3\2\2\2>?\7@\2\2?\20\3\2"+
		"\2\2@A\7>\2\2A\22\3\2\2\2BC\7%\2\2C\24\3\2\2\2DE\7<\2\2E\26\3\2\2\2FG"+
		"\7.\2\2G\30\3\2\2\2HJ\t\2\2\2IH\3\2\2\2JK\3\2\2\2KI\3\2\2\2KL\3\2\2\2"+
		"L\32\3\2\2\2MO\t\3\2\2NM\3\2\2\2OP\3\2\2\2PN\3\2\2\2PQ\3\2\2\2Q\34\3\2"+
		"\2\2RT\t\4\2\2SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2\2VW\3\2\2\2WX\b\17"+
		"\2\2X\36\3\2\2\2\6\2KPU\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}