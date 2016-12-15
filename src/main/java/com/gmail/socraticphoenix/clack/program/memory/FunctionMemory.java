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

import com.gmail.socraticphoenix.clack.program.Program;

import java.util.Stack;

public class FunctionMemory implements Memory {
    private Program program;

    private Variable[] variables;
    private StackMemory[] stacks;

    private StackMemory parent;

    private StackMemory currentStack;

    public FunctionMemory(Program program, StackMemory parent) {
        this.program = program;
        this.parent = parent;

        this.variables = new Variable[26];
        for (int i = 0; i < this.variables.length; i++) {
            this.variables[i] = new Variable();
        }

        this.stacks = new StackMemory[26];
        for (int i = 0; i < this.stacks.length; i++) {
            this.stacks[i] = new StackMemory(this.program);
        }

        this.currentStack = parent;
    }

    public StackMemory getCurrentStack() {
        return this.currentStack;
    }

    @Override
    public Stack<Variable> current() {
        return this.currentStack.getStack();
    }

    @Override
    public Variable popWellValue() {
        return this.currentStack.produceNextWellValue();
    }

    @Override
    public Variable peekWellValue() {
        return this.currentStack.peekNextWellValue();
    }

    @Override
    public Variable get(int index) {
        return this.variables[index].copy();
    }

    @Override
    public void set(int index, Object var) {
        this.variables[index].set(var);
    }

    @Override
    public boolean isPresent(int index) {
        return this.variables[index].isPresent();
    }

    @Override
    public void setStack(int index) {
        this.currentStack = index == 26 ? this.parent : this.stacks[index];
    }

}
