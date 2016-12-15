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
package com.gmail.socraticphoenix.clack.ast;

import com.gmail.socraticphoenix.clack.parse.ProgramParser;
import com.gmail.socraticphoenix.clack.parse.ProgramTokenizer;
import com.gmail.socraticphoenix.clack.parse.TokenGroups;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.instruction.InstructionSequence;
import com.gmail.socraticphoenix.clack.program.memory.Memory;
import com.gmail.socraticphoenix.nebula.collection.Items;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class SequenceNode implements Node, InstructionSequence {
    private List<Node> nodes;

    public SequenceNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void exec(Memory memory, Program program) {
        Iterator<Node> iterator = this.nodes.iterator();
        while (iterator.hasNext() && program.isRunning()) {
            iterator.next().exec(memory, program);
        }
    }

    @Override
    public String write() {
        StringBuilder builder = new StringBuilder();
        Stack<Node> iterator = new Stack<>();
        iterator.addAll(Items.reversed(this.nodes));
        while (!iterator.isEmpty()) {
            Node n = iterator.pop();
            if(n instanceof PushNode) {
                PushNode p = (PushNode) n;
                if(p.getObj() instanceof SequenceNode) {
                    char left = '{';
                    char right = '}';
                    if(!iterator.isEmpty()) {
                        Node peek = iterator.peek();
                        if(peek instanceof InstructionNode) {
                            Instruction peekI = ((InstructionNode) peek).getInstruction();
                            if(this.check(')', peekI)) {
                                left = '(';
                                right = ')';
                                iterator.pop();
                            } else if (this.check('⌋', peekI)) {
                                left = '⌊';
                                right = '⌋';
                                iterator.pop();
                            } else if (this.check('⌉', peekI)) {
                                left = '⌈';
                                right = '⌉';
                                iterator.pop();
                            }
                        }
                    }
                    builder.append(left).append(p.write()).append(right);
                } else if (p.getObj() instanceof BigDecimal && !iterator.isEmpty()) {
                    String val = ((BigDecimal) p.getObj()).compareTo(BigDecimal.ONE.negate()) == 0 ? "-" : ((BigDecimal) p.getObj()).compareTo(BigDecimal.ZERO) == 0 ? "." : String.valueOf(((BigDecimal) p.getObj()).stripTrailingZeros());
                    builder.append(val);
                    while (val.startsWith("0")) {
                        val = Strings.cutFirst(val);
                    }
                    Node peek = iterator.peek();
                    PushNode pk;
                    while (peek instanceof PushNode && (pk = ((PushNode) peek)).getObj() instanceof BigDecimal) {
                        iterator.pop();
                        String val2 = ((BigDecimal) pk.getObj()).compareTo(BigDecimal.ONE.negate()) == 0 ? "-" : ((BigDecimal) pk.getObj()).compareTo(BigDecimal.ZERO) == 0 ? "." : String.valueOf(((BigDecimal) pk.getObj()).stripTrailingZeros());
                        while (val2.startsWith("0")) {
                            val2 = Strings.cutFirst(val2);
                        }
                        if ((val.contains(".") && val2.startsWith(".")) || val2.startsWith("-")) {
                            builder.append(val2);
                        } else {
                            builder.append("|").append(val2);
                        }
                        peek = iterator.isEmpty() ? null : iterator.peek();
                        val = val2;
                    }
                } else {
                    builder.append(p.write());
                }
            } else {
                builder.append(n.write());
            }
        }

        return builder.toString();
    }

    private boolean check(char c, Instruction instruction) {
        Optional<Instruction> optional = ProgramParser.instruction(ProgramTokenizer.toType(c, TokenGroups.ALL.indexOf(c)));
        return optional.isPresent() && optional.get().equals(instruction);
    }

    public String toString() {
        return this.write();
    }

}
