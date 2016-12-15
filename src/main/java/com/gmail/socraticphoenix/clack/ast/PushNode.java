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

import com.gmail.socraticphoenix.clack.program.memory.VariableList;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.memory.Memory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.nebula.collection.Items;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.math.BigDecimal;
import java.util.Stack;

public class PushNode implements Node {
    private Object obj;

    public PushNode(Object obj) {
        this.obj = obj;
    }

    public static String write(Object obj) {
        return PushNode.write(obj, false, true);
    }

    public static String write(Object obj, boolean deep, boolean hasNext) {
        if (obj instanceof String) {
            return "\"" + obj + "“";
        } else if (obj instanceof BigDecimal) {
            return String.valueOf(obj);
        } else if (obj instanceof VariableList) {
            VariableList list = (VariableList) obj;
            StringBuilder builder = new StringBuilder();
            builder.append(deep ? ":" : "[");
            Stack<Variable> iterator = new Stack<>();
            iterator.addAll(Items.reversed(list));
            while (!iterator.isEmpty()) {
                Variable var = iterator.pop();

                if (var.val() instanceof BigDecimal && !iterator.isEmpty()) {
                    String val = ((BigDecimal) var.val()).compareTo(BigDecimal.ONE.negate()) == 0 ? "-" : ((BigDecimal) var.val()).compareTo(BigDecimal.ZERO) == 0 ? "." : String.valueOf(((BigDecimal) var.val()).stripTrailingZeros());
                    builder.append(val);
                    while (val.startsWith("0")) {
                        val = Strings.cutFirst(val);
                    }
                    Variable peek = iterator.peek();
                    while (peek != null && peek.val() instanceof BigDecimal) {
                        iterator.pop();
                        String val2 = ((BigDecimal) peek.val()).compareTo(BigDecimal.ONE.negate()) == 0 ? "-" : ((BigDecimal) peek.val()).compareTo(BigDecimal.ZERO) == 0 ? "." : String.valueOf(((BigDecimal) peek.val()).stripTrailingZeros());
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
                    builder.append(PushNode.write(var.val(), true, !iterator.isEmpty()));
                }
            }
            return builder.append(deep ? (hasNext ? ";" : "") : "]").toString();
        } else if (obj instanceof SequenceNode) {
            return ((SequenceNode) obj).write();
        } else {
            return "null";
        }
    }

    public Object getObj() {
        return this.obj;
    }

    @Override
    public void exec(Memory memory, Program program) {
        memory.push(Variable.of(this.obj));
    }

    @Override
    public String write() {
        return PushNode.write(this.obj);
    }

}
