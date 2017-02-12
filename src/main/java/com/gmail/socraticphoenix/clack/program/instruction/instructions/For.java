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
package com.gmail.socraticphoenix.clack.program.instruction.instructions;

import com.gmail.socraticphoenix.clack.ast.SequenceNode;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.instruction.Argument;
import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.memory.Memory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.clack.program.memory.VariableList;
import com.gmail.socraticphoenix.nebula.collection.Items;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class For implements Instruction {

    @Override
    public int danger() {
        return 0;
    }

    @Override
    public String name() {
        return "Ḟ";
    }

    @Override
    public String canonical() {
        return "for";
    }

    @Override
    public String doc() {
        return "";
    }

    @Override
    public List<Argument> arguments(Memory memory, Program program) {
        return Items.buildList(Argument.type("a", "top value of the stack", "instruction sequence", false, true, SequenceNode.class), Argument.type("b", "second value on the stack", "number, string, or list", false, true, BigDecimal.class, String.class, VariableList.class));
    }

    @Override
    public void exec(Memory memory, Program program, Map<String, Variable> arguments) {
        SequenceNode node = arguments.get("a").get(SequenceNode.class).get();
        Variable a = arguments.get("b");
        if (a.get(BigDecimal.class).isPresent()) {
            BigDecimal decimal = a.get(BigDecimal.class).get();
            while (program.isRunning()) {
                if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                    break;
                }
                node.exec(memory, program);
                decimal = decimal.subtract(BigDecimal.ONE);
            }
        } else {
            VariableList list;
            if (a.get(VariableList.class).isPresent()) {
                list = a.get(VariableList.class).get();
            } else {
                String string = a.val().toString();
                List<Variable> chars = new ArrayList<>();
                string.codePoints().forEach(i -> chars.add(Variable.of(new String(new int[]{i}, 0, 1))));
                list = new VariableList(chars);
            }
            int index = 0;
            while (program.isRunning()) {
                if (index >= list.size()) {
                    break;
                }
                Variable var = list.get(index);
                memory.push(var);
                node.exec(memory, program);
                index++;
            }
        }
    }

    @Override
    public String operation() {
        return null;
    }

}
