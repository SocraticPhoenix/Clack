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
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.clack.program.memory.VariableList;

import java.math.BigDecimal;

public class If {

    public static boolean truthy(Variable var) {
        if(var.get(BigDecimal.class).isPresent()) {
            BigDecimal val = var.get(BigDecimal.class).get();
            return val.compareTo(BigDecimal.ZERO) != 0;
        } else if (var.get(String.class).isPresent()) {
            String val = var.get(String.class).get();
            return val.equalsIgnoreCase("true");
        } else if (var.get(VariableList.class).isPresent()) {
            VariableList list = var.get(VariableList.class).get();
            return list.size() > 0;
        } else if (var.get(SequenceNode.class).isPresent()) {
            SequenceNode sequenceNode = var.get(SequenceNode.class).get();
            return sequenceNode.getNodes().size() > 0;
        } else {
            return false;
        }
    }

}
