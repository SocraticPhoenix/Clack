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

import com.gmail.socraticphoenix.nebula.string.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Arguments {
    private Map<String, String> flags;
    private List<String> args;

    public Arguments(String[] args) {
        this.flags = new HashMap<>();
        this.args = new ArrayList<>();
        for(String s : args) {
            if(s.startsWith("-")) {
                String[] pieces = Strings.cutFirst(s).split("=", 2);
                this.flags.put(pieces[0], Strings.deEscape(pieces.length == 1 ? "" : pieces[1]));
            } else if (s.startsWith("|")) {
                this.args.add(Strings.deEscape(Strings.cutFirst(s)));
            } else {
                this.args.add(Strings.deEscape(s));
            }
        }
    }

    public Arguments combine(Arguments other) {
        Arguments combined = new Arguments(new String[0]);
        combined.args.addAll(this.args);
        combined.args.addAll(other.args);
        combined.flags.putAll(this.flags);
        combined.flags.putAll(other.flags);
        return combined;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public boolean hasFlag(String s) {
        return this.flags.containsKey(s);
    }

    public String getFlag(String s) {
        return this.flags.get(s);
    }

}
