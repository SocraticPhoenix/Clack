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
package com.gmail.socraticphoenix.clack.app;

import com.gmail.socraticphoenix.clack.app.gui.ClackUI;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.memory.Variable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.function.Predicate;

public class ClackSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static ClackUI clackUI;

    public static void setUI(ClackUI clackUI) {
        ClackSystem.clackUI = clackUI;
    }

    public static void printlnErr(String s) {
        ClackSystem.printErr(s + System.lineSeparator());
    }

    public static void printlnOut(String s) {
        ClackSystem.printOut(s + System.lineSeparator());
    }

    public static void printErr(String s) {
        System.err.print(s);
        if(ClackSystem.clackUI != null) {
            ClackSystem.clackUI.printErr(s);
        }
    }

    public static void printOut(String s) {
        System.out.print(s);
        if(ClackSystem.clackUI != null) {
            ClackSystem.clackUI.printOut(s);
        }
    }

    public static Variable receiveInput(Program program, Predicate<Variable> form, String desc) {
        ClackSystem.printOut("Enter a value: ");
        if(ClackSystem.clackUI != null) {
            while (ClackSystem.clackUI.getInput().isEmpty() && program.isRunning());
            if(!program.isRunning()) {
                return Variable.of(BigDecimal.ONE);
            } else {
                Variable var = Variable.parse(ClackSystem.clackUI.getInput().pop());
                if (form.test(var)) {
                    return var;
                } else {
                    ClackSystem.printlnOut("Invalid input, expected \"" + desc + "\"");
                    ClackSystem.printOut("Enter a value: ");
                    return ClackSystem.receiveInput(program, form, desc);
                }
            }
        } else {
            while (!scanner.hasNextLine() && program.isRunning());
            if (!program.isRunning()) {
                return Variable.of(BigDecimal.ZERO);
            } else {
                Variable var = Variable.parse(scanner.nextLine());
                if (form.test(var)) {
                    return var;
                } else {
                    ClackSystem.printlnOut("Invalid input, expected \"" + desc + "\"");
                    ClackSystem.printOut("Enter a value: ");
                    return ClackSystem.receiveInput(program, form, desc);
                }
            }
        }
    }

    public static void printErr(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter pwrite = new PrintWriter(writer);
        e.printStackTrace(pwrite);
        pwrite.close();
        ClackSystem.printlnErr(writer.toString());
    }
}
