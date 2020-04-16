/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package com.myopicmobile.textwarrior.common;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Does lexical analysis of a text for C-like languages.
 * The programming language syntax used is set as a static class variable.
 */
public class Lexer implements JavaTokenTypes {
	private final static int MAX_KEYWORD_LENGTH = 127;

	public final static int UNKNOWN = -1;
	public final static int NORMAL = 0;
	public final static int KEYWORD = 1;
	public final static int OPERATOR = 2;
	public final static int NAME = 3;
	public final static int NUMBER = 4;
    public final static int DATATYPE=5;
    public final static int YSF=6;
	/** A word that starts with a special symbol, inclusive.
	 * Examples:
	 * :ruby_symbol
	 * */
	public final static int SINGLE_SYMBOL_WORD = 10;

	/** Tokens that extend from a single start symbol, inclusive, until the end of line.
	 * Up to 2 types of symbols are supported per language, denoted by A and B
	 * Examples:
	 * #include "myCppFile"
	 * #this is a comment in Python
	 * %this is a comment in Prolog
	 * */
	public final static int SINGLE_SYMBOL_LINE_A = 20;
	public final static int SINGLE_SYMBOL_LINE_B = 21;

	/** Tokens that extend from a two start symbols, inclusive, until the end of line.
	 * Examples:
	 * //this is a comment in C
	 * */
	public final static int DOUBLE_SYMBOL_LINE = 30;

	/** Tokens that are enclosed between a start and end sequence, inclusive,
	 * that can span multiple lines. The start and end sequences contain exactly
	 * 2 symbols.
	 * Examples:
	 * {- this is a...
	 *  ...multi-line comment in Haskell -}
	 * */
	public final static int DOUBLE_SYMBOL_DELIMITED_MULTILINE = 40;

	/** Tokens that are enclosed by the same single symbol, inclusive, and
	 * do not span over more than one line.
	 * Examples: 'c', "hello world"
	 * */
	public final static int SINGLE_SYMBOL_DELIMITED_A = 50;
	public final static int SINGLE_SYMBOL_DELIMITED_B = 51;

	private static Language _globalLanguage = LanguageNonProg.getInstance();
	synchronized public static void setLanguage(Language lang) {
		_globalLanguage = lang;
	}

	synchronized public static Language getLanguage() {
		return _globalLanguage;
	}


	private DocumentProvider _hDoc;
	private LexThread _workerThread = null;
	LexCallback _callback = null;

	public Lexer(LexCallback callback) {
		_callback = callback;
	}

	public void tokenize(DocumentProvider hDoc) {
		if (!Lexer.getLanguage().isProgLang()) {
			return;
		}

		//tokenize will modify the state of hDoc; make a copy
		setDocument(new DocumentProvider(hDoc));
		if (_workerThread == null) {
			_workerThread = new LexThread(this);
			_workerThread.start();
		} else {
			_workerThread.restart();
		}
	}

	void tokenizeDone(List<Pair> result) {
		if (_callback != null) {
			_callback.lexDone(result);
		}
		_workerThread = null;
	}

	public void cancelTokenize() {
		if (_workerThread != null) {
			_workerThread.abort();
			_workerThread = null;
		}
	}

	public synchronized void setDocument(DocumentProvider hDoc) {
		_hDoc = hDoc;
	}

	public synchronized DocumentProvider getDocument() {
		return _hDoc;
	}





	private class LexThread extends Thread {
		private boolean rescan = false;
		private final Lexer _lexManager;
		/** can be set by another thread to stop the scan immediately */
		private final Flag _abort;
		/** A collection of Pairs, where Pair.first is the start
		 *  position of the token, and Pair.second is the type of the token.*/
		/**
		 * pair的集合，first表示token的开始，second表示token的类型
		 */
		private List<Pair> _tokens;

		public LexThread(Lexer p) {
			_lexManager = p;
			_abort = new Flag();
		}

		@Override
		public void run() {
			do{
				rescan = false;
				_abort.clear();
				tokenize();
			}
			while(rescan);

			if (!_abort.isSet()) {
				// lex complete
				_lexManager.tokenizeDone(_tokens);
			}
		}

		public void restart() {
			rescan = true;
			_abort.set();
		}

		public void abort() {
			_abort.set();
		}
		/**
		 * Scans the document referenced by _lexManager for tokens.
		 * The result is stored internally.
		 *扫描结果存在list
		 * *******************************
		 * #include <stdio.h>
		 * int main(void)
		 * {
		 *
		 *     return 0;
		 * }
		 * *******************************
		 * 以上的C代码将产生以下词法分析器
		 * (0,20)---->#include <stdio.h>
		 * (20,1)---->int
		 * (24,0)空格
		 * (29,1)----->void
		 * (33,0)
		 * (42,1)----->return
		 * (49,0)
		 * ------------------------------
		 * 它的特点：first记录从哪开始有标记，second记录标记的类型
		 */
		public void tokenize() {
			DocumentProvider hDoc = getDocument();
			Language language = Lexer.getLanguage();
			//这里用ArrayList速度会发生质的飞跃
			List<Pair> tokens = new ArrayList<>();

			if (!language.isProgLang()) {
				tokens.add(new Pair(0, NORMAL));
				_tokens = tokens;
				return;
			}
			StringReader stringReader=new StringReader(hDoc.toString());
			JavaLexer jLexer=new JavaLexer(stringReader);
			int jType=0;
			int idx=0;
			String identifier=null;//存储标识符
			language.clearUserWord();
            while (jType != JavaTokenTypes.TokenNameEOF) {
                try {
                    jType = jLexer.getNextToken();
                    switch (jType) {
							//关键字
                        case TokenNameabstract:
                        case TokenNamebreak:
                        case TokenNamecase:
                        case TokenNamecatch:
                        case TokenNameclass:
                        case TokenNamecontinue:
                        case TokenNameconst:
                        case TokenNamedefault:
                        case TokenNamedo:
                        case TokenNameelse:
                        case TokenNameextends:
                        case TokenNamefinal:
                        case TokenNamefinally:
                        case TokenNamefor:
                        case TokenNameif:
                        case TokenNameimplements:
                        case TokenNameimport:
                        case TokenNameinstanceof:
                        case TokenNameinterface:
                        case TokenNamenative:
                        case TokenNamenew:
                        case TokenNamepackage:
                        case TokenNameprivate:
                        case TokenNameprotected:
                        case TokenNamepublic:
                        case TokenNamereturn:
                        case TokenNamestatic:
                        case TokenNamestrictfp:
                        case TokenNamesuper:
                        case TokenNameswitch:
                        case TokenNamesynchronized:
                        case TokenNamethis:
                        case TokenNamethrow:
                        case TokenNamethrows:
                        case TokenNametransient:
                        case TokenNametry:
                        case TokenNamevolatile:
                        case TokenNamewhile:
                            tokens.add(new Pair(idx, KEYWORD));
                            break;
                            //数据类型
                        case TokenNamevoid:
                        case TokenNameshort:
                        case TokenNamelong:
                        case TokenNameint:
                        case TokenNamefloat:
                        case TokenNamedouble:
                        case TokenNamechar:
                        case TokenNamebyte:
                        case TokenNameboolean:
                        case TokenNameCOLON_COLON:
                        case TokenNameDOT:
                        case TokenNameCOMMA:
                        case TokenNameCOLON:
                        case TokenNameSEMICOLON:
                        case TokenNameRBRACKET:
                        case TokenNameLBRACKET:
                        case TokenNameLPAREN:
                        case TokenNameRPAREN:
                        case TokenNameLBRACE:
                        case TokenNameRBRACE:
                            tokens.add(new Pair(idx, DATATYPE));
                            break;
                            //数字
                        case TokenNameCharacterLiteral:
                        case TokenNameDoubleLiteral:
                        case TokenNameFloatingPointLiteral:
                        case TokenNameLongLiteral:
                        case TokenNameIntegerLiteral:
                            //字符串
                        case TokenNameStringLiteral:
                        case TokenNamenull:
                        case TokenNametrue:
                        case TokenNamefalse:
                            tokens.add(new Pair(idx, NUMBER));
                            break;
                            //注释
                        case TokenNameCOMMENT_BLOCK:
                        case TokenNameCOMMENT_LINE:
                            tokens.add(new Pair(idx, DOUBLE_SYMBOL_LINE));
                            break;
                            //运算符
                        case TokenNamePLUS_PLUS:
                        case TokenNameMINUS_MINUS:
                        case TokenNameEQUAL_EQUAL:
                        case TokenNameLESS_EQUAL:
                        case TokenNameGREATER_EQUAL:
                        case TokenNameNOT_EQUAL:
                        case TokenNameLEFT_SHIFT:
                        case TokenNamePLUS_EQUAL:
                        case TokenNameMINUS_EQUAL:
                        case TokenNameMULTIPLY_EQUAL:
                        case TokenNameDIVIDE_EQUAL:
                        case TokenNameAND_EQUAL:
                        case TokenNameOR_EQUAL:
                        case TokenNameXOR_EQUAL:
                        case TokenNameREMAINDER_EQUAL:
                        case TokenNameLEFT_SHIFT_EQUAL:
                        case TokenNameRIGHT_SHIFT_EQUAL:
                        case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL:
                        case TokenNameOR_OR:
                        case TokenNameAND_AND:
                        case TokenNamePLUS:
                        case TokenNameMINUS:
                        case TokenNameNOT:
                        case TokenNameREMAINDER:
                        case TokenNameXOR:
                        case TokenNameAND:
                        case TokenNameMULTIPLY:
                        case TokenNameDIVIDE:
                        case TokenNameOR:
                        case TokenNameTWIDDLE:
                        case TokenNameGREATER:
                        case TokenNameLESS:
                        case TokenNameEQUAL:
                            tokens.add(new Pair(idx, YSF));
                            break;
                            //标识符
                        case TokenNameIdentifier:
                            identifier = jLexer.yytext();
                            tokens.add(new Pair(idx, NORMAL));
                            break;
                        default:
                            tokens.add(new Pair(idx, NORMAL));
                            break;
                    }
                    idx = jLexer.getCharIndex();
                } catch (Exception e) {
                    e.printStackTrace();
                    idx++;//错误了，索引也要往后挪
                }
            }

			if (tokens.isEmpty()) {
				// return value cannot be empty
				tokens.add(new Pair(0, NORMAL));
			}
			//printList(tokens);
            _tokens = tokens;
		}

	}//end inner class

	private  void log(String log) {
		System.out.println("------------------>Lexer:" + log);
	}

	private  void printList(List<Pair> list) {
		System.out.println("------------------>:Lexer start,Lexer len:" + list.size());
		for (int i=0;i < list.size();i++) {
			Pair pair=list.get(i);
			System.out.println("---------------->" + pair.toString());//不打印？
		}
		System.out.println("------------------>:Lexer end");

	}

	public interface LexCallback {
        void lexDone(List<Pair> results);
	}
}
