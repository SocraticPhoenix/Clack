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
import com.gmail.socraticphoenix.clack.program.instruction.InstructionSequence;

import java.math.BigDecimal;
import java.util.Stack;

public class StackMemory implements Memory {
    private Program program;

    private Stack<Variable> stack;
    private Variable[] variables;
    private Stack<Variable>[] wellStacks;
    private InstructionSequence well;

    private Stack<Variable> currentWellStack;

    public StackMemory(Program program) {
        this.program = program;

        this.stack = new Stack<>();
        this.variables = new Variable[26];
        for (int i = 0; i < this.variables.length; i++) {
            this.variables[i] = new Variable();
        }

        this.wellStacks = new Stack[26];
        for (int i = 0; i < this.wellStacks.length; i++) {
            this.wellStacks[i] = new Stack<>();
        }

        this.currentWellStack = this.wellStacks[0];

        this.well = new ZeroSupplyingInstructionSequence();
    }

    public Stack<Variable> getStack() {
        return this.stack;
    }

    public InstructionSequence getWell() {
        return this.well;
    }

    public void setWell(InstructionSequence well) {
        this.well = well;
        for(Variable var : this.variables) {
            var.set(null);
        }

        for(Stack<Variable> stack : this.wellStacks) {
            stack.clear();
        }

        this.currentWellStack = this.wellStacks[0];
        this.produceNextWellValue();
    }

    public Variable produceNextWellValue() {
        Variable var = this.current().isEmpty() ? this.popWellValue() : this.current().peek();
        this.well.exec(this, this.program);
        return var;
    }

    public Variable peekNextWellValue() {
        return this.current().isEmpty() ? this.peekWellValue() : this.current().peek();
    }

    @Override
    public Stack<Variable> current() {
        return this.currentWellStack;
    }

    @Override
    public Variable[] variables() {
        return this.variables;
    }

    @Override
    public Variable popWellValue() {
        return Variable.of(BigDecimal.ZERO);
    }

    @Override
    public Variable peekWellValue() {
        return Variable.of(BigDecimal.ZERO);
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
        this.currentWellStack = this.wellStacks[index];
    }
}
