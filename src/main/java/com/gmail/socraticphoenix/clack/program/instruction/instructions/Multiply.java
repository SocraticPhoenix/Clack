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

import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.instruction.Argument;
import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.memory.Memory;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.clack.program.memory.VariableList;
import com.gmail.socraticphoenix.nebula.collection.Items;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Multiply implements Instruction {

    public static Variable mul(Variable a, Variable b, Program program) {
        if (a.get(BigDecimal.class).isPresent() && b.get(BigDecimal.class).isPresent()) {
            return Variable.of(a.get(BigDecimal.class).get().multiply(b.get(BigDecimal.class).get(), program.mathContext()));
        } else if ((a.get(BigDecimal.class).isPresent() && b.get(String.class).isPresent()) || (b.get(BigDecimal.class).isPresent() && a.get(String.class).isPresent())) {
            StringBuilder res = new StringBuilder();
            BigDecimal decimal;
            String string;
            if (a.get(BigDecimal.class).isPresent()) {
                decimal = a.get(BigDecimal.class).get();
                string = b.get(String.class).get();
            } else {
                decimal = b.get(BigDecimal.class).get();
                string = a.get(String.class).get();
            }

            BigInteger intPart = decimal.toBigInteger();
            BigDecimal decimalPart = decimal.subtract(decimal.setScale(0, RoundingMode.FLOOR), program.mathContext());
            while (intPart.compareTo(BigInteger.ZERO) > 0) {
                res.append(string);
                intPart = intPart.subtract(BigInteger.ONE);
            }

            int fractional = (int) (decimalPart.doubleValue() * string.length());
            res.append(string.substring(0, fractional));
            return Variable.of(res.toString());
        } else if (a.get(VariableList.class).isPresent() || b.get(VariableList.class).isPresent()) {
            VariableList list = a.get(VariableList.class).orElseGet(() -> b.get(VariableList.class).get());
            Variable other = a.get(VariableList.class).isPresent() ? b : a;
            VariableList res = new VariableList(new ArrayList<>());
            for (Variable var : list) {
                res.add(Multiply.mul(var, other, program));
            }
            return Variable.of(res);
        } else {
            String as = a.val().toString();
            String bs = b.val().toString();
            StringBuilder res = new StringBuilder();
            for (char c : as.toCharArray()) {
                if (bs.indexOf(c) != -1) {
                    res.append(c);
                }
            }

            return Variable.of(res.toString());
        }
    }

    @Override
    public int danger() {
        return 0;
    }

    @Override
    public String name() {
        return "*";
    }

    @Override
    public String canonical() {
        return "multiply";
    }

    @Override
    public String doc() {
        return "";
    }

    @Override
    public List<Argument> arguments(Memory memory, Program program) {
        return Items.buildList(Argument.any("a", "top value of the stack", "any type of value", false, true), Argument.any("b", "second value on the stack", "any type of value", false, true));
    }

    @Override
    public void exec(Memory memory, Program program, Map<String, Variable> arguments) {
        Variable a = arguments.get("a");
        Variable b = arguments.get("b");
        memory.push(Multiply.mul(a, b, program));
    }

    @Override
    public String operation() {
        return "${a} * ${b} = ${res}";
    }

}
