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
import com.gmail.socraticphoenix.clack.parse.ParseException;
import com.gmail.socraticphoenix.clack.parse.ProgramParser;
import com.gmail.socraticphoenix.clack.parse.ProgramTokenizer;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.clack.program.memory.Variable;
import com.gmail.socraticphoenix.jencoding.charsets.JEncodingCharsets;
import com.gmail.socraticphoenix.nebula.math.Calculations;
import com.gmail.socraticphoenix.nebula.string.Strings;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ClackMain {

    public static void main(String[] args) {
        Arguments arguments = new Arguments(args);
        int security;
        if (arguments.hasFlag("s") && Calculations.isInteger(arguments.getFlag("s"))) {
            security = Integer.parseInt(arguments.getFlag("s"));
        } else {
            security = 10;
        }

        if (arguments.hasFlag("e") || arguments.hasFlag("f")) {
            String prog;
            if (arguments.hasFlag("e")) {
                prog = arguments.getFlag("e");
            } else if (arguments.hasFlag("f")) {
                try {
                    List<String> strings = Files.readAllLines(Paths.get(arguments.getFlag("f")), JEncodingCharsets.CLACK);
                    prog = Strings.glue("\n", (Object[]) strings.toArray(new String[strings.size()]));
                } catch (IOException e) {
                    ClackSystem.printErr(e);
                    return;
                }
            } else {
                prog = "";
            }

            ProgramTokenizer tokenizer = new ProgramTokenizer();
            tokenizer.tokenize(prog);
            ProgramParser parser = new ProgramParser(tokenizer.finish());
            try {
                Program program = new Program(parser.finish(), security, arguments);
                for (int i = 0; i < arguments.getArgs().size(); i++) {
                    program.getTrans().push(Variable.parse(arguments.getArgs().get(i)));
                }
                System.out.println(program.getFunction('$').write());
                program.run();
                program.terminate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (arguments.hasFlag("g")) {
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, ClackMain.class.getResourceAsStream("unifont.ttf"));
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            } catch (FontFormatException | IOException e) {
                ClackSystem.printlnErr("Failed to load font!");
                ClackSystem.printErr(e);
            }
            ClackUI ui = new ClackUI(arguments);
            ClackSystem.setUI(ui);
            ui.display();
            ClackSystem.printOut("The ClackApp uses GNU Unifont Glyphs, an awesome free-to-use font that handles the entire unicode basic multilingual plane (65,536 characters). Check them out here: http://unifoundry.com/unifont.html");
        }

    }

}
