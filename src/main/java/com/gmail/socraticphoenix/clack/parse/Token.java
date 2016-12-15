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

public class Token {
    private char content;
    private Type type;

    public Token(char content, Type type) {
        this.content = content;
        this.type = type;
    }

    public char getContent() {
        return this.content;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        VARIABLE,
        CONSTANT,
        CONSTANT_LENGTH_PRECURSOR,
        STACK,
        FUNCTION,
        WHITESPACE,
        INSTRUCTION,
        INSTRUCTION_LENGTH_PRECURSOR,
        SYNTAX_ESCAPE_CHAR,
        SYNTAX_DIGIT,
        SYNTAX_NUM_PART,
        SYNTAX_LPAREN,
        SYNTAX_RPAREN,
        SYNTAX_LBRACKET,
        SYNTAX_RBRACKET,
        SYNTAX_LBRACE,
        SYNTAX_RBRACE,
        SYNTAX_LFLOOR,
        SYNTAX_RFLOOR,
        SYNTAX_LCEIL,
        SYNTAX_RCEIL,
        SYNTAX_SEP,
        SYNTAX_STRING_START,
        SYNTAX_STRING_END,
        SYNTAX_STRING_END_START,
        SYNTAX_INCREASE_ARRAY_DEPTH,
        SYNTAX_DECREASE_ARRAY_DEPTH,
        SYNTAX_VAR_SET,
        SYNTAX_END_FUNCTION,
        SYNTAX_STRING_CONTENT

    }

}
