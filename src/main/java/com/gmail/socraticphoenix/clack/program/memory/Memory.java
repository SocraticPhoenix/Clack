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
package com.gmail.socraticphoenix.clack.program.memory;

import com.gmail.socraticphoenix.clack.parse.TokenGroups;

import java.util.Stack;

public interface Memory {

    Stack<Variable> current();

    Variable popWellValue();

    Variable peekWellValue();

    Variable get(int index);

    void set(int index, Object var);

    boolean isPresent(int index);

    void setStack(int index);

    default Variable pop() {
        return this.current().isEmpty() ? this.popWellValue() : this.current().pop();
    }

    default void push(Variable variable) {
        this.current().push(variable);
    }

    static boolean isStackId(char c) {
        return TokenGroups.STACKS.indexOf(c) != -1;
    }

    static boolean isVariableId(char c) {
        return TokenGroups.VARIABLES.indexOf(c) != -1;
    }

    static int getStackIndex(char c) {
        return TokenGroups.STACKS.indexOf(c);
    }

    static int getVariableIndex(char c) {
        return TokenGroups.VARIABLES.indexOf(c);
    }

}
