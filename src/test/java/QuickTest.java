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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class QuickTest {

    public static void main(String[] args) throws IOException {
        File file = new File("src/main/java/com/gmail/socraticphoenix/clack");
        StringBuilder builder = new StringBuilder();
        QuickTest.recursiveRead(file, builder);
        FileWriter writer = new FileWriter("program_code.txt");
        writer.write(builder.toString());
        writer.close();
    }

    public static void recursiveRead(File file, StringBuilder builder) throws IOException {
        for(File sub : file.listFiles()) {
            if(!sub.isDirectory() && sub.getAbsolutePath().endsWith(".java")) {
                builder.append("----------------------- START FILE -----------------------").append(System.lineSeparator()).append(QuickTest.readAllText(sub)).append("----------------------- END FILE -----------------------").append(System.lineSeparator());
            }
        }

        for(File sub : file.listFiles()) {
            if(sub.isDirectory()) {
                recursiveRead(sub, builder);
            }
        }
    }

    public static String readAllText(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int c;
        while ((c = reader.read()) != -1) {
            builder.appendCodePoint(c);
        }
        return builder.toString();
    }

}
