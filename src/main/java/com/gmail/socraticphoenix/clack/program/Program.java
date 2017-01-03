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
import com.gmail.socraticphoenix.clack.ast.SequenceNode;
import com.gmail.socraticphoenix.clack.parse.TokenGroups;
import com.gmail.socraticphoenix.clack.program.memory.FunctionMemory;
import com.gmail.socraticphoenix.clack.program.memory.StackMemory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.jencoding.charsets.JEncodingCharsets;
import com.gmail.socraticphoenix.nebula.collection.coupling.Pair;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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

    public void waitForGo() {

    }

    public void visit(Node node) {

    }

    public static String encode1(String s) {
        byte[] bytes = s.getBytes(JEncodingCharsets.CLACK);
        Deflater deflater = new Deflater();
        deflater.setInput(bytes);
        byte[] buffer = new byte[1024];
        StringBuilder builder = new StringBuilder();
        while (!deflater.finished()) {
            int len = deflater.deflate(buffer);
            byte[] actual = new byte[len];
            System.arraycopy(buffer, 0, actual, 0, len);
            builder.append(new String(actual, JEncodingCharsets.CLACK));
        }
        return builder.toString();
    }

    public static String encode2(String s) {
        return s;
    }

    public static String decode1(String s) throws DataFormatException {
        byte[] bytes = s.getBytes(JEncodingCharsets.CLACK);
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        byte[] buffer = new byte[1024];
        StringBuilder builder = new StringBuilder();
        while (!inflater.finished()) {
            int len = inflater.inflate(buffer);
            byte[] actual = new byte[len];
            System.arraycopy(buffer, 0, actual, 0, len);
            builder.append(new String(actual, JEncodingCharsets.CLACK));
        }
        return buffer.toString();
    }

    public static String decode2(String s) {
        return s;
    }


    public static List<Pair<Character, String>> tryEncodingStrategies(String s){
        return new ArrayList<>();
    }

    public static Optional<String> shortestEncoding(List<String> strings) {
        List<String> written = new ArrayList<>();
        List<List<Pair<Character, String>>> encodings = new ArrayList<>();
        for(String s : strings) {
            encodings.add(Program.tryEncodingStrategies(s));
        }


        for(List<Pair<Character, String>> encoding : encodings) {
            Iterator<Pair<Character, String>> iterator = encoding.iterator();
            StringBuilder builder = new StringBuilder().append("\"");
            while (iterator.hasNext()) {
                Pair<Character, String> piece = iterator.next();
                builder.append(piece.getB());
                while (iterator.hasNext()) {
                    Pair<Character, String> nextPiece = iterator.next();
                    if(piece.getA() == nextPiece.getA()) {
                        builder.append("»").append(nextPiece.getA());
                    } else {
                        builder.append(piece.getA()).append("\"").append(nextPiece.getB());
                        piece = nextPiece;
                        break;
                    }
                }
                builder.append(piece.getA());
            }
            written.add(builder.toString());
        }

        return written.stream().sorted((a, b) -> Integer.compare(b.length(), a.length())).findFirst();
    }

}
