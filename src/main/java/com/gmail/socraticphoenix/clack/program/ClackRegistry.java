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

import com.gmail.socraticphoenix.clack.program.instruction.Instruction;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.Add;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.Divide;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.Multiply;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.Print;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.PrintLine;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.Subtract;
import com.gmail.socraticphoenix.clack.program.instruction.instructions.While;
import com.gmail.socraticphoenix.clack.program.memory.Constant;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClackRegistry {
    private static Map<String, Instruction> instructions;
    private static Map<String, Constant> constants;

    static {
        ClackRegistry.instructions = new LinkedHashMap<>();
        ClackRegistry.constants = new LinkedHashMap<>();
        init();
    }

    private static void init() {
        r(new Add());
        r(new Subtract());
        r(new Multiply());
        r(new Divide());
        r(new Print());
        r(new PrintLine());
        r(new While());

        r(new Constant("Π", "pi", new BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679")));
    }

    private static void r(Instruction instruction) {
        ClackRegistry.register(instruction);
    }

    private static void r(Constant constant) {
        ClackRegistry.register(constant);
    }

    public static boolean hasInstruction(String name) {
        return ClackRegistry.instructions.containsKey(name);
    }

    public static boolean hasConstant(String name) {
        return ClackRegistry.constants.containsKey(name);
    }

    public static void register(Instruction instruction) {
        ClackRegistry.instructions.put(instruction.name(), instruction);
    }

    public static void register(Constant constant) {
        ClackRegistry.constants.put(constant.getName(), constant);
    }

    public static Instruction instruction(String name) {
        return ClackRegistry.instructions.get(name);
    }

    public static Constant constant(String name) {
        return ClackRegistry.constants.get(name);
    }

}
