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

import com.gmail.socraticphoenix.clack.program.ClackRegistry;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.instruction.Argument;
import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.memory.Memory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class InstructionNode implements Node {
    private Instruction instruction;
    private String name;

    public InstructionNode(String name) {
        this.instruction = ClackRegistry.instruction(name);
        this.name = name;
    }

    public InstructionNode(Instruction instruction) {
        this.instruction = instruction;
        this.name = instruction.name();
    }

    public Instruction getInstruction() {
        return this.instruction;
    }

    @Override
    public void exec(Memory memory, Program program) {
        program.waitForGo();
        program.visit(this);
        if (!((10 - program.getSecurity()) >= this.instruction.danger())) {
            program.error("Security level is " + program.getSecurity() + ", which prevents \"" + this.instruction.canonical() + "\" from executing due to its danger level of " + this.instruction.danger());
            return;
        }

        Map<String, Variable> variables = new HashMap<>();
        List<Argument> arguments = this.instruction.arguments(memory, program);
        Stack<Variable> current = memory.current();

        Stack<Variable> popped = new Stack<>();

        while (!current.isEmpty() && !arguments.isEmpty() && program.isRunning()) {
            Variable var = current.pop();
            boolean found = false;
            for (int i = 0; i < arguments.size(); i++) {
                Argument argument = arguments.get(i);
                if(argument.getTest().test(var)) {
                    found = true;
                    variables.put(argument.getName(), var);
                    arguments.remove(i);
                    if(!argument.pops()) {
                        popped.push(var);
                    }
                    break;
                }
            }
            if(!found) {
                popped.push(var);
            }
        }

        while (!popped.isEmpty()) {
            current.push(popped.pop());
        }

        Iterator<Argument> iterator = arguments.iterator();
        while (iterator.hasNext() && program.isRunning()) {
            Argument argument = iterator.next();
            if(argument.prefersWell()) {
                Variable var = memory.peekWellValue();
                if(argument.getTest().test(var)) {
                    variables.put(argument.getName(), memory.popWellValue());
                } else {
                    variables.put(argument.getName(), program.input(argument.getTest(), argument.type()));
                }
            } else {
                variables.put(argument.getName(), program.input(argument.getTest(), argument.type()));
            }
        }

        this.instruction.exec(memory, program, variables);
    }

    @Override
    public String write() {
        return this.name;
    }

}
