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
package com.gmail.socraticphoenix.clack.parse;

public interface TokenGroups {
    String ALL = "αβγδεζηθικλμνξοπρστυφχψωΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩabcdefghijklmnopqrstuvwxyz#ABCDEFGHIJKLMNOPQRSTUVWXYZ$-0123456789.()[]{}⌊⌋⌈⌉|\"“”«»:;=\n\\\t ẠḄḌẸḤỊḲḶṂṆỌṚṢṬỤṾẈỴẒȦḂĊḊĖḞĠḢİĿṀṄȮṖṘṠṪẆẊẎŻạḅḍẹḥịḳḷṃṇọṛṣṭụṿẉỵẓȧḃċḋėḟġḣŀṁṅȯṗṙṡṫẇẋẏż!@&*_+/%^?~`️<>'₢€£￦￥¥₳฿￠₡¢₢₵₫￡Ł₥₦₱₯₤₣₲₭";
    String ALL_BY_ROW = "αβγδεζηθικλμνξοπρστυφχψω,ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ,abcdefghijklmnopqrstuvwxyz#,ABCDEFGHIJKLMNOPQRSTUVWXYZ$,-0123456789.,()[]{}⌊⌋⌈⌉|\"“”«»:;=\n\\\t ,ẠḄḌẸḤỊḲḶṂṆỌṚṢṬỤṾẈỴẒȦḂĊḊĖḞĠḢİĿṀṄȮṖṘṠṪẆẊẎŻ,ạḅḍẹḥịḳḷṃṇọṛṣṭụṿẉỵẓ,ȧḃċḋėḟġḣŀṁṅȯṗṙṡṫẇẋẏż!@&*_+/%^?~`️<>',₢€£￦￥¥₳฿￠₡¢₢₵₫￡Ł,₥₦₱₯₤₣₲₭";

    String VARIABLES = "αβγδεζηθικλμνξοπρστυφχψω";
    String CONSTANTS = "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ";
    String STACKS = "abcdefghijklmnopqrstuvwxyz#";
    String FUNCTIONS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ$";

    String SYNTAX = "-0123456789.()[]{}⌊⌋⌈⌉|\"“”«»:;=\n\\";
    String DIGITS = "0123456789";

    String WHITESPACE = "\t ";

    String INSTRUCTIONS = "ẠḄḌẸḤỊḲḶṂṆỌṚṢṬỤṾẈỴẒȦḂĊḊĖḞĠḢİĿṀṄȮṖṘṠṪẆẊẎŻạḅḍẹḥịḳḷṃṇọṛṣṭụṿẉỵẓȧḃċḋėḟġḣŀṁṅȯṗṙṡṫẇẋẏż!@&*_+/%^?~`️<>'₢€£￦￥¥₳฿￠₡¢₢₵₫￡Ł";

    String CONSTANT_LENGTH_PRECURSORS = "₥₦₱₯";

    String INSTRUCTION_LENGTH_PRECURSORS = "₤₣₲₭";

    static int index(char c) {
        return TokenGroups.ALL.indexOf(c);
    }

    static int[] range(String r) {
        int index = TokenGroups.ALL.indexOf(r);
        return new int[] {index, index + r.length() - 1};
    }
}
