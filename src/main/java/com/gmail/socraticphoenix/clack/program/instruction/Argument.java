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
package com.gmail.socraticphoenix.clack.program.instruction;

import com.gmail.socraticphoenix.clack.collection.VariableList;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.nebula.collection.Items;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class Argument {
    private String name;
    private boolean prefersWell;
    private String desc;
    private Predicate<Variable> test;


    public Argument(String name, String desc, boolean prefersWell, Predicate<Variable> test) {
        this.name = name;
        this.prefersWell = prefersWell;
        this.test = test;
        this.desc = desc;
    }

    public Argument(String name, String desc, Predicate<Variable> test) {
        this(name, desc, false, test);
    }

    public static Predicate<Variable> type(Class... types) {
        return o -> Items.contains(o.type(), types);
    }

    public static Predicate<Variable> list(Predicate<Variable> test) {
        return Argument.type(VariableList.class).and(v -> v.get(VariableList.class).get().stream().allMatch(test));
    }

    public static Predicate<Variable> greater(BigDecimal num) {
        return Argument.type(BigDecimal.class).and(v -> v.get(BigDecimal.class).get().compareTo(num) > 0);
    }

    public static Predicate<Variable> less(BigDecimal num) {
        return Argument.type(BigDecimal.class).and(v -> v.get(BigDecimal.class).get().compareTo(num) < 0);
    }

    public static Predicate<Variable> greaterEqual(BigDecimal num) {
        return Argument.type(BigDecimal.class).and(v -> v.get(BigDecimal.class).get().compareTo(num) >= 0);
    }

    public static Predicate<Variable> lessEqual(BigDecimal num) {
        return Argument.type(BigDecimal.class).and(v -> v.get(BigDecimal.class).get().compareTo(num) <= 0);
    }

    public static Predicate<Variable> inRange(BigDecimal min, BigDecimal max) {
        return Argument.greaterEqual(min).and(Argument.lessEqual(max));
    }

    public String name() {
        return this.name;
    }

    public boolean prefersWell() {
        return this.prefersWell;
    }


    public String getDesc() {
        return this.desc;
    }

    public Predicate<Variable> getTest() {
        return this.test;
    }

    public String getName() {
        return this.name;
    }
}
