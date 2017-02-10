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
import com.gmail.socraticphoenix.clack.ast.Node;
import com.gmail.socraticphoenix.clack.ast.PushNode;
import com.gmail.socraticphoenix.clack.ast.SequenceNode;
import com.gmail.socraticphoenix.clack.parse.TokenGroups;
import com.gmail.socraticphoenix.clack.program.memory.FunctionMemory;
import com.gmail.socraticphoenix.clack.program.memory.StackMemory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.jencoding.charsets.JEncodingCharsets;
import com.gmail.socraticphoenix.nebula.collection.Layers;
import com.gmail.socraticphoenix.nebula.collection.coupling.Pair;
import com.gmail.socraticphoenix.nebula.collection.coupling.Triple;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

public class Program {
    private Map<Character, SequenceNode> functions;
    private StackMemory trans;
    private boolean running;
    private boolean errored;
    private FunctionMemory main;
    private int security;
    private Stack<Triple<Character, SequenceNode, FunctionMemory>> methodStack;
    private Arguments arguments;

    public Program(List<SequenceNode> functions, int security, Arguments arguments) {
        if (functions.size() == 0 || functions.size() > 27) {
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
        this.arguments = arguments;
        this.methodStack = new Stack<>();
    }

    public static String encode1(String s) {
        //For future use
        return s;
    }

    public static String encode2(String s) {
        return new String(s.getBytes(StandardCharsets.UTF_8), JEncodingCharsets.CLACK);
    }

    public static String decode1(String s) {
        //For future use
        return s;
    }

    public static String decode2(String s) {
        return new String(s.getBytes(JEncodingCharsets.CLACK), StandardCharsets.UTF_8);
    }

    public static Optional<String> shortestEncoding(List<String> strings) {
        List<String> written = new ArrayList<>();
        Layers<Pair<Character, String>> encodings = new Layers<>();
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            encodings.add(Pair.of('“', s), i);
            encodings.add(Pair.of('”', Program.encode1(s)), i);
            encodings.add(Pair.of('«', Program.encode2(s)), i);
        }

        for (List<Pair<Character, String>> encoding : encodings.stacks()) {
            Iterator<Pair<Character, String>> iterator = encoding.iterator();
            StringBuilder builder = new StringBuilder().append("\"");
            while (iterator.hasNext()) {
                Pair<Character, String> piece = iterator.next();
                builder.append(Strings.escape(piece.getB(), PushNode.DATA));
                while (iterator.hasNext()) {
                    Pair<Character, String> nextPiece = iterator.next();
                    if (piece.getA() == nextPiece.getA()) {
                        builder.append("»").append(nextPiece.getA());
                    } else {
                        builder.append(piece.getA()).append("\"").append(Strings.escape(piece.getB(), PushNode.DATA));
                        piece = nextPiece;
                        break;
                    }
                }
                builder.append(piece.getA());
            }
            written.add(builder.toString());
        }

        return written.stream().sorted(Comparator.comparingInt(String::length)).findFirst();
    }

    public Stack<Triple<Character, SequenceNode, FunctionMemory>> getMethodStack() {
        return this.methodStack;
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
        SequenceNode function = this.functions.get(id);
        FunctionMemory functionMemory = new FunctionMemory(this, this.trans);
        this.methodStack.push(Triple.of(id, function, functionMemory));
        function.exec(functionMemory, this);
        this.methodStack.pop();
    }

    public void run() {
        SequenceNode function = this.functions.get('$');
        FunctionMemory functionMemory = this.main;
        this.methodStack.push(Triple.of('$', function, functionMemory));
        function.exec(functionMemory, this);
        this.methodStack.pop();
        this.terminate();
    }

    public Variable input(Predicate<Variable> form, String desc) {
        return ClackSystem.receiveInput(this, form, desc);
    }

    public MathContext mathContext() {
        return MathContext.DECIMAL128;
    }

    public void terminate() {
        if (!arguments.hasFlag("n") && !this.errored) {
            while (!this.main.current().isEmpty()) {
                ClackSystem.printlnOut(this.main.pop().toString());
            }
        }
    }

    public SequenceNode getFunction(char c) {
        return this.functions.get(c);
    }

    public void waitForGo() {
        while (!this.running) ;
    }

    public void visit(Node node) {
        //For future use
    }

    public String write() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Character, SequenceNode> function : this.functions.entrySet()) {
            if (function.getKey() != '$') {
                builder.append(function.getValue().write()).append("\n");
            } else {
                builder.append(function.getValue().write());
            }
        }
        return builder.toString();
    }

}
