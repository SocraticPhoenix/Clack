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

import com.gmail.socraticphoenix.clack.ast.ConstantNode;
import com.gmail.socraticphoenix.clack.ast.FunctionNode;
import com.gmail.socraticphoenix.clack.ast.InstructionNode;
import com.gmail.socraticphoenix.clack.ast.Node;
import com.gmail.socraticphoenix.clack.ast.PushNode;
import com.gmail.socraticphoenix.clack.ast.SequenceNode;
import com.gmail.socraticphoenix.clack.ast.SetVariableNode;
import com.gmail.socraticphoenix.clack.ast.StackNode;
import com.gmail.socraticphoenix.clack.ast.VariableNode;
import com.gmail.socraticphoenix.clack.program.ClackRegistry;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.clack.program.memory.VariableList;
import com.gmail.socraticphoenix.nebula.collection.coupling.Triple;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static com.gmail.socraticphoenix.clack.parse.Token.Type.CONSTANT;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.CONSTANT_LENGTH_PRECURSOR;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.FUNCTION;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.INSTRUCTION;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.INSTRUCTION_LENGTH_PRECURSOR;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.STACK;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_DECREASE_ARRAY_DEPTH;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_DIGIT;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_ESCAPE_CHAR;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_INCREASE_ARRAY_DEPTH;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_LBRACE;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_LBRACKET;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_NUM_PART;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_RBRACE;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_RBRACKET;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_RCEIL;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_RFLOOR;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_RPAREN;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_STRING_CONTENT;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_STRING_END;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_STRING_END_START;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_STRING_START;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.SYNTAX_VAR_SET;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.VARIABLE;
import static com.gmail.socraticphoenix.clack.parse.Token.Type.WHITESPACE;

public class ProgramParser {
    private List<Token> tokens;
    private List<SequenceNode> functions;
    private Stack<Triple<List<Node>, Token.Type, Token.Type>> stack;

    public ProgramParser(List<Token> tokens) {
        this.tokens = tokens;
        this.functions = new ArrayList<>();
        this.stack = new Stack<>();
        this.stack.push(Triple.of(new ArrayList<>(), SYNTAX_LBRACE, SYNTAX_RBRACE));
    }

    public static Optional<Instruction> instruction(Token.Type rbracket) {
        Instruction res = null;
        switch (rbracket) {
            case SYNTAX_RPAREN:
                res = ClackRegistry.instruction("Ị");
                break;
            case SYNTAX_RFLOOR:
                res = ClackRegistry.instruction("Ẉ");
                break;
            case SYNTAX_RCEIL:
                res = ClackRegistry.instruction("Ḟ");
                break;
        }
        return Optional.ofNullable(res);
    }

    public List<SequenceNode> finish() throws ParseException {
        while (this.hasNext()) {
            this.step();
        }

        if (!this.stack.isEmpty()) {
            while (this.stack.size() != 1) {
                Triple<List<Node>, Token.Type, Token.Type> triple = this.stack.pop();
                this.stack.peek().getA().add(new PushNode(new SequenceNode(triple.getA())));
                this.instruction(triple.getB()).ifPresent(n -> this.stack.peek().getA().add(new InstructionNode(n)));
            }
            this.functions.add(new SequenceNode(this.stack.pop().getA()));
        }
        return this.functions;
    }

    public void step() throws ParseException {
        this.consumeSimple();
        if (this.hasNext()) {
            this.consumeIgnored();
            Token next = this.nextToken().get();
            Token.Type t = next.getType();
            switch (t) {
                case SYNTAX_LBRACE:
                    this.stack.push(Triple.of(new ArrayList<>(), SYNTAX_RBRACE, t));
                    break;
                case SYNTAX_LFLOOR:
                    this.stack.push(Triple.of(new ArrayList<>(), SYNTAX_RFLOOR, t));
                    break;
                case SYNTAX_LCEIL:
                    this.stack.push(Triple.of(new ArrayList<>(), SYNTAX_RCEIL, t));
                    break;
                case SYNTAX_LPAREN:
                    this.stack.push(Triple.of(new ArrayList<>(), SYNTAX_RPAREN, t));
                    break;
                case SYNTAX_RPAREN:
                case SYNTAX_RBRACE:
                case SYNTAX_RFLOOR:
                case SYNTAX_RCEIL:
                    if (this.stack.isEmpty() || this.stack.size() == 1) {
                        throw new ParseException("Unmatched " + t);
                    } else {
                        Triple<List<Node>, Token.Type, Token.Type> triple = this.stack.pop();
                        if (triple.getB() != t) {
                            throw new ParseException("Mismatched brackets, attempted to match " + triple.getC() + " and " + triple.getB());
                        }
                        SequenceNode node = new SequenceNode(triple.getA());
                        this.stack.peek().getA().add(new PushNode(node));
                        this.instruction(t).ifPresent(n -> this.stack.peek().getA().add(new InstructionNode(n)));

                    }
                    break;
                case SYNTAX_END_FUNCTION:
                    while (this.stack.size() != 1) {
                        Triple<List<Node>, Token.Type, Token.Type> triple = this.stack.pop();
                        this.stack.peek().getA().add(new PushNode(new SequenceNode(triple.getA())));
                        this.instruction(triple.getB()).ifPresent(n -> this.stack.peek().getA().add(new InstructionNode(n)));
                    }
                    this.functions.add(new SequenceNode(this.stack.pop().getA()));
                    this.stack.push(Triple.of(new ArrayList<>(), SYNTAX_LBRACE, SYNTAX_RBRACE));
                    break;
                default:
                    throw new ParseException("Unexpected token after simple consume: " + t);
            }
        }
    }

    public void consumeSimple() throws ParseException {
        this.consumeIgnored();
        while (this.simpleIsNext()) {
            this.stack.peek().getA().add(this.nextSimple());
            this.consumeIgnored();
        }
    }

    public String nextString() throws ParseException {
        StringBuilder builder = new StringBuilder();
        this.nextToken();
        while (this.hasNext()) {
            Token peek = this.peekNext().get();
            if (peek.getType() == SYNTAX_ESCAPE_CHAR) {
                this.nextToken();
                builder.append("\\");
            } else if (peek.getType() == SYNTAX_STRING_CONTENT) {
                this.nextToken();
                builder.append(peek.getContent());
            } else if (peek.getType() == SYNTAX_STRING_END) {
                this.nextToken();
                char id = peek.getContent();
                if (id == '“') {
                    return Strings.deEscape(builder.toString(), PushNode.DATA);
                } else if (id == '”') {
                    return Program.decode1(Strings.deEscape(builder.toString(), PushNode.DATA));
                } else if (id == '«') {
                    return Program.decode2(Strings.deEscape(builder.toString(), PushNode.DATA));
                }
            } else if (peek.getType() == SYNTAX_STRING_END_START) {
                return Strings.deEscape(builder.toString(), PushNode.DATA);
            } else {
                throw new ParseException("Invalid token type in string mode: " + peek.getType());
            }
        }
        return Strings.deEscape(builder.toString(), PushNode.DATA);
    }

    public BigDecimal nextNumber() throws ParseException {
        StringBuilder builder = new StringBuilder();
        boolean dot;
        if (this.hasNext()) {
            Token next = this.nextToken().get();
            if (next.getType() == SYNTAX_NUM_PART || next.getType() == SYNTAX_DIGIT) {
                builder.append(next.getContent());
                dot = next.getContent() == '.';
            } else {
                throw new ParseException("Invalid token to begin with in number mode: " + next.getType());
            }
            while (this.hasNext()) {
                Token peek = this.peekNext().get();
                if (peek.getType() == SYNTAX_DIGIT || (peek.getType() == SYNTAX_NUM_PART && peek.getContent() == '.' && !dot)) {
                    builder.append(peek.getContent());
                    dot |= peek.getContent() == '.';
                    this.nextToken();
                } else {
                    break;
                }
            }
        }

        String process = builder.toString();
        if (process.equals("-")) {
            process = "-1";
        }

        if (process.startsWith(".")) {
            process = "0" + process;
        } else if (process.startsWith("-.")) {
            process = "-0" + Strings.cutFirst(process);
        }

        if (process.endsWith(".")) {
            process = process + "0";
        }

        try {
            return new BigDecimal(process);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number \"" + process + "\"", e);
        }
    }

    public VariableList nextArray() throws ParseException {
        this.nextToken();
        Stack<VariableList> lists = new Stack<>();
        lists.push(new VariableList(new ArrayList<>()));

        while (this.hasNext()) {
            this.consumeIgnored();
            if (this.numNext()) {
                lists.peek().add(Variable.of(this.nextNumber()));
            } else if (this.stringNext()) {
                lists.peek().add(Variable.of(this.nextString()));
            } else {
                Token next = this.nextToken().get();
                if (next.getType() == SYNTAX_INCREASE_ARRAY_DEPTH) {
                    lists.push(new VariableList(new ArrayList<>()));
                } else if (next.getType() == SYNTAX_DECREASE_ARRAY_DEPTH) {
                    if (lists.size() == 1) {
                        VariableList list = new VariableList(new ArrayList<>());
                        list.add(Variable.of(lists.pop()));
                        lists.push(list);
                    } else {
                        VariableList list = lists.pop();
                        lists.peek().add(Variable.of(list));
                    }
                } else if (next.getType() == SYNTAX_RBRACKET) {
                    break;
                } else {
                    throw new ParseException("Disallowed token in array mode: " + next.getType());
                }
            }
        }

        while (lists.size() > 1) {
            VariableList list = lists.pop();
            lists.peek().add(Variable.of(list));
        }

        return lists.pop();
    }

    public boolean numNext() {
        if (this.hasNext()) {
            Token.Type peek = this.peekNext().get().getType();
            return peek == SYNTAX_NUM_PART || peek == SYNTAX_DIGIT;
        }
        return false;
    }

    public boolean stringNext() {
        if (this.hasNext()) {
            Token.Type peek = this.peekNext().get().getType();
            return peek == SYNTAX_STRING_START || peek == SYNTAX_STRING_END_START;
        }
        return false;
    }

    public boolean arrNext() {
        return this.hasNext() && this.peekNext().get().getType() == SYNTAX_LBRACKET;
    }

    public boolean valNext() {
        return this.numNext() || this.stringNext() || this.arrNext();
    }

    public PushNode nextVal() throws ParseException {
        return new PushNode(this.numNext() ? this.nextNumber() : this.stringNext() ? this.nextString() : this.arrNext() ? this.nextArray() : null);
    }

    public boolean instructionNext() {
        if (this.hasNext()) {
            Token.Type peek = this.peekNext().get().getType();
            return peek == INSTRUCTION || peek == INSTRUCTION_LENGTH_PRECURSOR;
        }
        return false;
    }

    public InstructionNode nextInstruction() throws ParseException {
        if (this.hasNext()) {
            Token next = this.nextToken().get();
            if (next.getType() == INSTRUCTION_LENGTH_PRECURSOR) {
                int len = TokenGroups.INSTRUCTION_LENGTH_PRECURSORS.indexOf(next.getContent()) + 1;
                String name = String.valueOf(next.getContent());
                for (int i = 0; i < len && this.hasNext(); i++) {
                    name += this.nextToken().get().getContent();
                }
                return new InstructionNode(name);
            } else {
                return new InstructionNode(String.valueOf(next.getContent()));
            }
        }

        throw new ParseException("No remaining tokens");
    }

    public boolean constantNext() {
        if (this.hasNext()) {
            Token.Type peek = this.peekNext().get().getType();
            return peek == CONSTANT || peek == CONSTANT_LENGTH_PRECURSOR;
        }
        return false;
    }

    public ConstantNode nextConstant() throws ParseException {
        if (this.hasNext()) {
            Token next = this.nextToken().get();
            if (next.getType() == CONSTANT_LENGTH_PRECURSOR) {
                int len = TokenGroups.CONSTANT_LENGTH_PRECURSORS.indexOf(next.getContent()) + 1;
                String name = "";
                for (int i = 0; i < len && this.hasNext(); i++) {
                    name += this.nextToken().get().getContent();
                }
                return new ConstantNode(String.valueOf(next.getContent()), name);
            } else {
                return new ConstantNode("", String.valueOf(next.getContent()));
            }
        }

        throw new ParseException("No remaining tokens");
    }

    public boolean variableNext() {
        if (this.hasNext()) {
            Token.Type peek = this.peekNext().get().getType();
            return peek == VARIABLE || peek == SYNTAX_VAR_SET;
        }
        return false;
    }

    public VariableNode nextVar() throws ParseException {
        if (this.hasNext()) {
            Token next = this.nextToken().get();
            if (next.getType() == SYNTAX_VAR_SET) {
                if (this.hasNext() && this.peekNext().get().getType() == VARIABLE) {
                    return new SetVariableNode(this.nextToken().get().getContent());
                } else {
                    throw new ParseException("Invalid token following SYNTAX_VAR_SET, must be VARIABLE, was: " + (this.hasNext() ? this.peekNext().get().getType() : "NULL"));
                }
            } else {
                return new VariableNode(next.getContent());
            }
        }

        throw new ParseException("No remaining tokens");
    }

    public boolean stackNext() {
        return this.hasNext() && this.peekNext().get().getType() == STACK;
    }

    public StackNode nextStack() throws ParseException {
        if (this.hasNext()) {
            Token next = this.nextToken().get();
            return new StackNode(next.getContent());
        }

        throw new ParseException("No remaining tokens");
    }

    public boolean funcNext() {
        return this.hasNext() && this.peekNext().get().getType() == FUNCTION;
    }

    public FunctionNode nextFunc() throws ParseException {
        if (this.hasNext()) {
            Token next = this.nextToken().get();
            return new FunctionNode(next.getContent());
        }

        throw new ParseException("No remaining tokens");
    }

    public boolean simpleIsNext() {
        return this.valNext() || this.constantNext() || this.funcNext() || this.instructionNext() || this.variableNext() || this.stackNext();
    }

    public Node nextSimple() throws ParseException {
        if (this.constantNext()) {
            return this.nextConstant();
        } else if (this.funcNext()) {
            return this.nextFunc();
        } else if (this.instructionNext()) {
            return this.nextInstruction();
        } else if (this.valNext()) {
            return this.nextVal();
        } else if (this.variableNext()) {
            return this.nextVar();
        } else if (this.stackNext()) {
            return this.nextStack();
        } else {
            throw new ParseException("Simple node not present");
        }
    }

    private void consumeIgnored() {
        while (this.hasNext()) {
            Token.Type type = this.peekNext().get().getType();
            if (type == WHITESPACE) {
                this.nextToken();
            } else {
                break;
            }
        }
    }

    public boolean hasNext() {
        return !this.tokens.isEmpty();
    }

    public Optional<Token> peekNext() {
        try {
            return Optional.ofNullable(this.tokens.get(0));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public Optional<Token> nextToken() {
        try {
            return Optional.ofNullable(this.tokens.remove(0));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

}
