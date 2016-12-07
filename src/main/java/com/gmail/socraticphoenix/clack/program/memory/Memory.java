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

import com.gmail.socraticphoenix.nebula.math.big.precision.BigFraction;

import java.math.BigInteger;
import java.util.Stack;

public class Memory {
    private static final String varIds = "αβγδεζηθικλμνξοπρςστυφχψω";
    private static final String stackIds = "abcdefghijklmnopqrstuvwxyz";

    private Variable[] variables;
    private Stack<Variable>[] stacks;
    private Stack<Variable> current;

    public Memory() {
        this.variables = new Variable[26];
        this.stacks = new Stack[26];
        for (int i = 0; i < this.stacks.length; i++) {
            this.stacks[i] = new Stack<>();
        }

        for (int i = 0; i < this.variables.length; i++) {
            this.variables[i] = new Variable();
        }
    }

    public static boolean isVariableId(char c) {
        return Memory.varIds.indexOf(c) != -1;
    }

    public static boolean isStackId(char c) {
        return Memory.stackIds.indexOf(c) != -1;
    }

    public static int variableIndex(char c) {
        return Memory.varIds.indexOf(c);
    }

    public static int stackIndex(char c) {
        return Memory.stackIds.indexOf(c);
    }

    public Variable get(int index) {
        return this.variables[index];
    }

    public void set(int index, Object val) {
        this.variables[index].set(val);
    }

    public void setStack(int index) {
        this.current = this.stacks[index];
    }

    public Variable pop() {
        return this.current.isEmpty() ? Variable.of(new BigFraction(BigInteger.ZERO, BigInteger.ONE)) : this.current.pop();
    }

    public Variable peek() {
        return this.current.isEmpty() ? Variable.of(new BigFraction(BigInteger.ZERO, BigInteger.ONE)) : this.current.peek();
    }

    public boolean isEmpty() {
        return this.current.isEmpty();
    }

    public void push(Variable variable) {
        this.current.push(variable);
    }

}
