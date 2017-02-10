/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 socraticphoenix@gmail.com
 * Copyright (c) 2016 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmail.socraticphoenix.clack.parse;

import com.gmail.socraticphoenix.nebula.math.Calculations;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.gmail.socraticphoenix.clack.parse.Token.Type.*;

public class ProgramTokenizer {
    private static int[] whitespaceRange = TokenGroups.range(TokenGroups.WHITESPACE);
    private static int[] instructionRange = TokenGroups.range(TokenGroups.INSTRUCTIONS);
    private static int[] instructionLengthPrecursorRange = TokenGroups.range(TokenGroups.INSTRUCTION_LENGTH_PRECURSORS);
    private static int[] constantLengthPrecursorRange = TokenGroups.range(TokenGroups.INSTRUCTION_LENGTH_PRECURSORS);
    private static int[] varRange = TokenGroups.range(TokenGroups.VARIABLES);
    private static int[] constRange = TokenGroups.range(TokenGroups.CONSTANTS);
    private static int[] stackRange = TokenGroups.range(TokenGroups.STACKS);
    private static int[] funcRange = TokenGroups.range(TokenGroups.FUNCTIONS);
    private static int[] syntaxRange = TokenGroups.range(TokenGroups.SYNTAX);
    private static int[] digitRange = TokenGroups.range(TokenGroups.DIGITS);
    private static int[] stringEndRange = TokenGroups.range("“”«");
    private static int escape = TokenGroups.index('\\');
    private static int minus = TokenGroups.index('-');
    private static int dot = TokenGroups.index('.');
    private static int lparen = TokenGroups.index('(');
    private static int rparen = TokenGroups.index(')');
    private static int lbracket = TokenGroups.index('[');
    private static int rbracket = TokenGroups.index(']');
    private static int lbrace = TokenGroups.index('{');
    private static int rbrace = TokenGroups.index('}');
    private static int lfloor = TokenGroups.index('⌊');
    private static int rfloor = TokenGroups.index('⌋');
    private static int lceil = TokenGroups.index('⌈');
    private static int rceil = TokenGroups.index('⌉');
    private static int stringStart = TokenGroups.index('"');
    private static int stringEndStart = TokenGroups.index('»');
    private static int increaseArrayDepth = TokenGroups.index(':');
    private static int decreaseArrayDepth = TokenGroups.index(';');
    private static int varSet = TokenGroups.index('=');
    private static int funcSep = TokenGroups.index('\n');

    private Stack<Mode> mode;
    private List<Token> tokens;


    public ProgramTokenizer() {
        this.mode = new Stack<>();
        this.tokens = new ArrayList<>();
    }

    private static boolean r(int index, int[] range) {
        return Calculations.isInRangeInclusive(range[0], index, range[1]);
    }

    public Token last() {
        return this.tokens.get(this.tokens.size() - 1);
    }

    public void tokenize(String s) {
        s.chars().forEach(c -> this.next((char) c));
    }

    public void next(char c) {
        int index = TokenGroups.index(c);
        Mode mode = this.mode.isEmpty() ? Mode.NORMAL : this.mode.peek();
        Token.Type type;
        switch (mode) {
            case STRING:
                if (this.last().getType() != Token.Type.SYNTAX_ESCAPE_CHAR) {
                    if (r(index, stringEndRange)) {
                        type = SYNTAX_STRING_END;
                        this.mode.pop();
                    } else if (index == stringEndStart) {
                        type = SYNTAX_STRING_END_START;
                    } else if (index == escape) {
                        type = SYNTAX_ESCAPE_CHAR;
                    } else {
                        type = SYNTAX_STRING_CONTENT;
                    }
                } else {
                    type = SYNTAX_STRING_CONTENT;
                }
                break;
            case ARRAY:
                if (index == stringStart) {
                    type = SYNTAX_STRING_START;
                    this.mode.push(Mode.STRING);
                } else if (index == rbracket) {
                    type = SYNTAX_RBRACKET;
                    this.mode.pop();
                } else {
                    type = this.toType(c, index);
                }
                break;
            case NORMAL:
                if (index == stringStart) {
                    type = SYNTAX_STRING_START;
                    this.mode.push(Mode.STRING);
                } else if (index == lbracket) {
                    type = SYNTAX_LBRACKET;
                    this.mode.push(Mode.ARRAY);
                } else {
                    type = this.toType(c, index);
                }
                break;
            default:
                throw new IllegalStateException();
        }
        this.tokens.add(new Token(c, type));
    }

    public List<Token> finish() {
        List<Token> temp = this.tokens;
        this.tokens = new ArrayList<>();
        return temp;
    }

    public static Token.Type toType(char c, int index) {
        if (r(index, varRange)) {
            return VARIABLE;
        } else if (r(index, constRange)) {
            return CONSTANT;
        } else if (r(index, stackRange)) {
            return STACK;
        } else if (r(index, funcRange)) {
            return FUNCTION;
        } else if (r(index, whitespaceRange)) {
            return WHITESPACE;
        } else if (r(index, instructionRange)) {
            return INSTRUCTION;
        } else if (r(index, instructionLengthPrecursorRange)) {
            return INSTRUCTION_LENGTH_PRECURSOR;
        } else if (r(index, constantLengthPrecursorRange)) {
            return CONSTANT_LENGTH_PRECURSOR;
        } else if (r(index, digitRange)) {
            return SYNTAX_DIGIT;
        } else if (r(index, stringEndRange)) {
            return SYNTAX_STRING_END;
        } else if (index == escape) {
            return SYNTAX_ESCAPE_CHAR;
        } else if (index == minus || index == dot) {
            return SYNTAX_NUM_PART;
        } else if (index == lparen) {
            return SYNTAX_LPAREN;
        } else if (index == rparen) {
            return SYNTAX_RPAREN;
        } else if (index == lbracket) {
            return SYNTAX_LBRACKET;
        } else if (index == rbracket) {
            return SYNTAX_RBRACKET;
        } else if (index == lbrace) {
            return SYNTAX_LBRACE;
        } else if (index == rbrace) {
            return SYNTAX_RBRACE;
        } else if (index == lfloor) {
            return SYNTAX_LFLOOR;
        } else if (index == rfloor) {
            return SYNTAX_RFLOOR;
        } else if (index == lceil) {
            return SYNTAX_LCEIL;
        } else if (index == rceil) {
            return SYNTAX_RCEIL;
        } else if (index == stringStart) {
            return SYNTAX_STRING_START;
        } else if (index == stringEndStart) {
            return SYNTAX_STRING_END_START;
        } else if (index == increaseArrayDepth) {
            return SYNTAX_INCREASE_ARRAY_DEPTH;
        } else if (index == decreaseArrayDepth) {
            return SYNTAX_DECREASE_ARRAY_DEPTH;
        } else if (index == varSet) {
            return SYNTAX_VAR_SET;
        } else if (index == funcSep) {
            return SYNTAX_END_FUNCTION;
        } else {
            throw new IllegalStateException("Unable to find token for char: \"" + Strings.escape(String.valueOf(c)) + "\"");
        }
    }

    public enum Mode {
        STRING,
        ARRAY,
        NORMAL
    }

}
