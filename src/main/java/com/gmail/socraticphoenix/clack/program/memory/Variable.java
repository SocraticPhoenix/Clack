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
package com.gmail.socraticphoenix.clack.program.memory;

import com.gmail.socraticphoenix.nebula.reflection.Reflections;

import java.util.Optional;

public class Variable {
    private Object val;

    public Variable() {
        this.val = null;
    }

    public static Variable of(Object val) {
        Variable var = new Variable();
        var.set(val);
        return var;
    }

    public void set(Object val) {
        this.val = val;
    }

    public boolean isPresent() {
        return this.val != null;
    }

    public Class type() {
        return this.val == null ? Void.class : this.val.getClass();
    }

    public <T> Optional<T> get(Class<T> type) {
        try {
            return Optional.ofNullable(Reflections.deepCast(type, this.val));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

}
