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
package com.gmail.socraticphoenix.clack.program;

import com.gmail.socraticphoenix.clack.app.Arguments;
import com.gmail.socraticphoenix.clack.app.ClackSystem;
import com.gmail.socraticphoenix.clack.ast.SequenceNode;
import com.gmail.socraticphoenix.clack.parse.TokenGroups;
import com.gmail.socraticphoenix.clack.program.memory.FunctionMemory;
import com.gmail.socraticphoenix.clack.program.memory.StackMemory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;

import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Program {
    private Map<Character, SequenceNode> functions;
    private StackMemory trans;
    private boolean running;
    private boolean errored;
    private FunctionMemory main;
    private int security;

    public Program(List<SequenceNode> functions, int security) {
        if(functions.size() == 0 || functions.size() > 27) {
            throw new IllegalArgumentException("Functions must have nonzero length less than 28");
        }

        this.trans = new StackMemory(this);
        this.functions = new LinkedHashMap<>();
        this.functions.put('$', functions.remove(functions.size() - 1));
        for (int i = 0; i < functions.size(); i++) {
            this.functions.putIfAbsent(TokenGroups.FUNCTIONS.charAt(i), functions.get(i));
        }
        this.running = true;
        this.main = new FunctionMemory(this, this.trans);
        this.security = security;
    }

    public int getSecurity() {
        return this.security;
    }

    public StackMemory getTrans() {
        return this.trans;
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean setRunning(boolean running) {
        this.running = false;
        return false;
    }

    public void error(String s) {
        this.setRunning(false);
        this.errored = true;
        ClackSystem.printlnErr(s);
    }

    public boolean hasFunction(char id) {
        return this.functions.containsKey(id);
    }

    public void callFunction(char id) {
        this.functions.get(id).exec(new FunctionMemory(this, this.trans), this);
    }

    public void run() {
        this.functions.get('$').exec(this.main, this);
    }

    public Variable input(Predicate<Variable> form, String desc) {
        return ClackSystem.receiveInput(this, form, desc);
    }

    public MathContext mathContext() {
        return MathContext.DECIMAL128;
    }

    public void terminate(Arguments arguments) {
        if(!arguments.hasFlag("-n") && !this.errored) {
            while (!this.main.current().isEmpty()) {
                ClackSystem.printlnOut(this.main.pop().toString());
            }
        }
    }

    public SequenceNode getFunction(char c) {
        return this.functions.get(c);
    }
}
