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

import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.instruction.Argument;
import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.instruction.InstructionRegistry;
import com.gmail.socraticphoenix.clack.program.memory.Memory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class InstructionNode implements Node {
    private Instruction instruction;

    public InstructionNode(String name) {
        this.instruction = InstructionRegistry.instruction(name);
    }

    @Override
    public void exec(Memory memory, Program program) {
        Map<String, Variable> variables = new HashMap<>();
        List<Argument> arguments = this.instruction.arguments(memory, program);
        Stack<Variable> current = memory.current();

        while (!current.isEmpty() && !arguments.isEmpty() && program.isRunning()) {
            Variable var = current.peek();
            for (int i = 0; i < arguments.size(); i++) {
                Argument argument = arguments.get(i);
                if(argument.getTest().test(var)) {
                    variables.put(argument.getName(), var);
                    variables.remove(i);
                    break;
                }
            }
        }

        Iterator<Argument> iterator = arguments.iterator();
        while (iterator.hasNext() && program.isRunning()) {
            Argument argument = iterator.next();
            if(argument.prefersWell()) {
                Variable var = memory.peekWellValue();
                if(argument.getTest().test(var)) {
                    variables.put(argument.getName(), memory.popWellValue());
                } else {
                    variables.put(argument.getName(), program.input(argument.getTest()));
                }
            } else {
                variables.put(argument.getName(), program.input(argument.getTest()));
            }
        }

        this.instruction.exec(memory, program, variables);
    }

}
