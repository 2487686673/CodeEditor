/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */

package com.myopicmobile.textwarrior.common;

import java.util.HashMap;

public abstract class ColorScheme {
	public enum Colorable {
		FOREGROUND, BACKGROUND, SELECTION_FOREGROUND, SELECTION_BACKGROUND,
		CARET_FOREGROUND, CARET_BACKGROUND, CARET_DISABLED, LINE_HIGHLIGHT,
		NON_PRINTING_GLYPH, COMMENT, KEYWORD, NAME, NUMBER,STRING,
		SECONDARY,DATATYPE,YSF
		}

	protected HashMap<Colorable, Integer> _colors = generateDefaultColors();

	public void setColor(Colorable colorable, int color) {
		_colors.put(colorable, color);
	}

	public int getColor(Colorable colorable) {
		Integer color = _colors.get(colorable);
		if (color == null) {
			TextWarriorException.fail("Color not specified for " + colorable);
			return 0;
		}
		return color.intValue();
	}

	// Currently, color scheme is tightly coupled with semantics of the token types
	public int getTokenColor(int tokenType) {
		Colorable element;
		switch (tokenType) {
			case Lexer.NORMAL:
				element = Colorable.FOREGROUND;
				break;
			case Lexer.KEYWORD:
				element = Colorable.KEYWORD;
				break;
			case Lexer.NAME:
				element = Colorable.NAME;
				break;
			case Lexer.DOUBLE_SYMBOL_LINE: //fall-through
			case Lexer.DOUBLE_SYMBOL_DELIMITED_MULTILINE:
				//case Lexer.SINGLE_SYMBOL_LINE_B:
				element = Colorable.COMMENT;
				break;
			case Lexer.SINGLE_SYMBOL_DELIMITED_A: //fall-through
			case Lexer.SINGLE_SYMBOL_DELIMITED_B:
				element = Colorable.STRING;
				break;
			case Lexer.NUMBER:
				element = Colorable.NUMBER;
				break;
			case Lexer.SINGLE_SYMBOL_LINE_A: //fall-through
			case Lexer.SINGLE_SYMBOL_WORD:
			case Lexer.OPERATOR:
				element = Colorable.SECONDARY;
				break;
			case Lexer.SINGLE_SYMBOL_LINE_B: //类型
				element = Colorable.NAME;
				break;
            case Lexer.DATATYPE:
                element = Colorable.DATATYPE;
                break;
            case Lexer.YSF:
                element = Colorable.YSF;
                break;
			default:
				TextWarriorException.fail("Invalid token type");
				element = Colorable.FOREGROUND;
				break;
		}
		return getColor(element);
	}

	/**
	 * Whether this color scheme uses a dark background, like black or dark grey.
	 */
	public abstract boolean isDark();

	private HashMap<Colorable, Integer> generateDefaultColors() {
		// High-contrast, black-on-white color scheme
		HashMap<Colorable, Integer> colors = new HashMap<Colorable, Integer>(Colorable.values().length);
		colors.put(Colorable.FOREGROUND, 0xFF000000);//文本颜色
		colors.put(Colorable.BACKGROUND, 0xFFFFFFFF);//编辑器背景色
		colors.put(Colorable.SELECTION_FOREGROUND, 0xFFFFFFFF);//选择文本的前景色
		colors.put(Colorable.SELECTION_BACKGROUND, 0xFF999999);//选择文本的背景色
		colors.put(Colorable.CARET_FOREGROUND, 0xff0099cc);
		colors.put(Colorable.CARET_BACKGROUND, 0xFF40B0FF);//选择文本的水滴背景色
		colors.put(Colorable.CARET_DISABLED, 0xFF000000);
		colors.put(Colorable.LINE_HIGHLIGHT, 0x20888888);//当前行颜色

		colors.put(Colorable.NON_PRINTING_GLYPH, 0xffd0d0d0);//行号
		colors.put(Colorable.COMMENT, 0xff009b00); //注释
		colors.put(Colorable.KEYWORD, 0xff2c82c8); //关键字
		colors.put(Colorable.NAME, 0xff000000); // Eclipse default color
		colors.put(Colorable.NUMBER, 0xffbc0000); // 数字
		colors.put(Colorable.STRING, 0xffbc0000); //字符串
		colors.put(Colorable.SECONDARY, 0xff6f008a);//宏定义
        colors.put(Colorable.DATATYPE, 0xff0096ff);//数据类型
        colors.put(Colorable.YSF,0xff007c1f);//运算符
		return colors;
	}
}
