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

import com.gmail.socraticphoenix.clack.parse.ParseException;
import com.gmail.socraticphoenix.clack.parse.ProgramParser;
import com.gmail.socraticphoenix.clack.parse.ProgramTokenizer;
import com.gmail.socraticphoenix.nebula.math.Calculations;
import com.gmail.socraticphoenix.nebula.reflection.Reflections;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.math.BigDecimal;
import java.util.Objects;
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

    public static Variable parse(String s) {
        ProgramTokenizer tokenizer = new ProgramTokenizer();
        tokenizer.tokenize(s);
        ProgramParser parser = new ProgramParser(tokenizer.finish());

        Variable var;
        if(!parser.valNext()) {
            var = Variable.of(Strings.deEscape(s));
        } else {
            try {
                var = Variable.of(parser.nextVal().getObj());
            } catch (ParseException e) {
                var = Variable.of(Strings.deEscape(s));
            }
        }
        return var;
    }

    public Object val() {
        return this.val;
    }

    public void set(Object val) {
        this.val = val;
    }

    public boolean isPresent() {
        return this.val != null;
    }

    public Variable copy() {
        return Variable.of(this.val);
    }

    public Class type() {
        return this.val == null ? Void.class : this.val.getClass();
    }

    public <T> Optional<T> get(Class<T> type) {
        try {
            if(this.val instanceof String && type == BigDecimal.class && Calculations.isBigDecimal((String) this.val)) {
                return Optional.of((T) new BigDecimal((String) this.val));
            } else if (this.val instanceof BigDecimal && type == String.class) {
                return Optional.of((T) String.valueOf(this.val));
            }
            return Optional.ofNullable(Reflections.deepCast(type, this.val));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    public String toString() {
        return String.valueOf(this.val);
    }

    public boolean equals(Object object) {
        return object instanceof Variable && Objects.equals(this.val, ((Variable) object).val);
    }

}
