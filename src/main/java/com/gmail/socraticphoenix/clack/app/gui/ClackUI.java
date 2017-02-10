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
package com.gmail.socraticphoenix.clack.app.gui;

import com.gmail.socraticphoenix.clack.app.ClackSystem;
import com.gmail.socraticphoenix.clack.parse.ParseException;
import com.gmail.socraticphoenix.clack.parse.ProgramParser;
import com.gmail.socraticphoenix.clack.parse.ProgramTokenizer;
import com.gmail.socraticphoenix.clack.program.Program;
import com.gmail.socraticphoenix.jencoding.charsets.JEncodingCharsets;
import com.gmail.socraticphoenix.nebula.string.Strings;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ClackUI {
    private JTextArea program;
    private JTextArea debug;
    private JTextArea out;
    private JTextArea error;
    private JTextField in;
    private JButton minifyButton;
    private JButton clearOutputButton;
    private JButton runButton;
    private JButton forceStopButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton byteCountButton;
    private JButton toggleKeyboardButton;
    private JSlider securityLevel;
    private JComboBox encoding;
    private JTextField file;
    private JButton selectFileButton;
    private JPanel panel;
    private JButton debugButton;
    private JButton stepDebuggerButton;

    private AtomicBoolean running;
    private AtomicReference<Program> programRef;
    private KeyboardUI keyboard;
    private boolean keyboardVisible;

    private JFileChooser chooser = new JFileChooser();

    private JFrame frame;

    private Stack<String> input;

    private Font font;

    public ClackUI() {
        this.font = new Font("Unifont", Font.PLAIN, 20);
        this.input = new Stack<>();
        this.running = new AtomicBoolean(false);
        this.programRef = new AtomicReference<>();
        this.keyboard = new KeyboardUI(this);
        this.keyboardVisible = false;

        UIManager.put("ToolTip.font", this.font);
    }

    public void setKeyboardVisible(boolean visible) {
        this.keyboardVisible = visible;
    }

    public Font getFont() {
        return this.font;
    }

    public JTextArea getProgram() {
        return this.program;
    }

    public Stack<String> getInput() {
        return this.input;
    }

    public void printOut(String s) {
        this.out.append(s);
    }

    public void printErr(String s) {
        this.error.append(s);
    }

    private void initComponents() {
        this.program.setFont(this.font);
        this.out.setFont(this.font);
        this.error.setFont(this.font);
        this.in.setFont(this.font);
        this.file.setFont(this.font);

        this.encoding.addItem("CLACK");
        this.encoding.addItem("UTF8");

        this.selectFileButton.addActionListener(a -> {
            int chosen = this.chooser.showOpenDialog(this.panel);
            if (chosen == JFileChooser.APPROVE_OPTION) {
                File file = this.chooser.getSelectedFile();
                this.file.setText(file.getAbsolutePath());
                try {
                    String newText = Strings.glue("\n", Files.readAllLines(file.toPath()));
                    this.program.setText(newText);
                } catch (IOException e) {
                    ClackSystem.printErr(e);
                }
            }
        });

        this.openButton.addActionListener(a -> {
            new Thread(() -> {
                try {
                    List<String> strings = Files.readAllLines(Paths.get(this.file.getText()), this.currentCharset());
                    String prog = Strings.glue("\n", (Object[]) strings.toArray(new String[strings.size()]));
                    this.program.setText(prog);
                } catch (IOException e) {
                    ClackSystem.printErr(e);
                }
            }).start();
        });

        this.saveButton.addActionListener(a -> {
            new Thread(() -> {
                String prog = this.program.getText();
                try {
                    Files.write(Paths.get(this.file.getText()), prog.getBytes(this.currentCharset()));
                } catch (IOException e) {
                    ClackSystem.printErr(e);
                }
            }).start();
        });

        this.byteCountButton.addActionListener(a -> {
            String prog = this.program.getText();
            StringBuilder byteInfo = new StringBuilder();
            byteInfo.append("Clack Bytes: ").append(prog.getBytes(JEncodingCharsets.CLACK).length).append(", UTF8 Bytes: ").append(prog.getBytes(StandardCharsets.UTF_8).length);
            ClackSystem.printlnOut(byteInfo.toString());
        });

        this.clearOutputButton.addActionListener(a -> {
            this.out.setText("");
            this.error.setText("");
        });

        this.minifyButton.addActionListener(a -> {
            this.doProgramAction(p -> this.program.setText(p.write()), false);
        });

        this.runButton.addActionListener(a -> {
            this.doProgramAction(Program::run, false);
        });

        this.toggleKeyboardButton.addActionListener(a -> {
            this.keyboardVisible = !this.keyboardVisible;
            this.keyboard.display().setVisible(this.keyboardVisible);
        });

        this.forceStopButton.addActionListener(a -> {
            if (this.programRef.get() != null) {
                Program program = this.programRef.get();
                program.setRunning(false);
                ClackSystem.printlnOut("Terminated program");
            }
        });

        this.in.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String input = in.getText();
                    in.setText("");
                    ClackSystem.printlnOut("> " + input);
                    getInput().push(input);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private Charset currentCharset() {
        return this.encoding.getSelectedItem().equals("UTF8") ? StandardCharsets.UTF_8 : JEncodingCharsets.CLACK;
    }

    private void doProgramAction(Consumer<Program> action, boolean debug) {
        if (this.running.get()) {
            ClackSystem.printlnErr("A program is currently loaded! Press \"Force Stop\" to unload it.");
        } else {
            this.running.set(true);
            String text = this.program.getText();
            new Thread(() -> {
                ProgramTokenizer tokenizer = new ProgramTokenizer();
                tokenizer.tokenize(text);
                ProgramParser parser = new ProgramParser(tokenizer.finish());
                try {
                    Program program = new Program(parser.finish(), this.securityLevel.getValue(), debug);
                    this.programRef.set(program);
                    action.accept(program);
                    this.running.set(false);
                    this.programRef.set(null);
                } catch (ParseException e) {
                    ClackSystem.printErr(e);
                }
            }).start();
        }
    }

    public JFrame display() {
        if (this.frame == null) {
            this.frame = new JFrame("Clack IDE");
            this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.frame.setSize(1361, 976);
            this.frame.add(this.panel);
            this.initComponents();
            this.frame.setVisible(true);
            return this.frame;
        } else {
            this.frame.setVisible(true);
            return this.frame;
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        program = new JTextArea();
        program.setFont(new Font(program.getFont().getName(), program.getFont().getStyle(), program.getFont().getSize()));
        scrollPane1.setViewportView(program);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Program");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        minifyButton = new JButton();
        minifyButton.setText("Minify");
        panel5.add(minifyButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        runButton = new JButton();
        runButton.setText("Run");
        panel5.add(runButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        forceStopButton = new JButton();
        forceStopButton.setText("Force Stop");
        panel5.add(forceStopButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        byteCountButton = new JButton();
        byteCountButton.setText("Byte Count");
        panel5.add(byteCountButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        clearOutputButton = new JButton();
        clearOutputButton.setText("Clear Output");
        panel6.add(clearOutputButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Open");
        panel6.add(openButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel6.add(saveButton, new GridConstraints(3, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toggleKeyboardButton = new JButton();
        toggleKeyboardButton.setText("Toggle Keyboard");
        panel6.add(toggleKeyboardButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        securityLevel = new JSlider();
        securityLevel.setMajorTickSpacing(1);
        securityLevel.setMaximum(10);
        securityLevel.setPaintLabels(true);
        securityLevel.setPaintTicks(true);
        securityLevel.setSnapToTicks(true);
        securityLevel.setValue(0);
        panel7.add(securityLevel, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        encoding = new JComboBox();
        panel7.add(encoding, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        file = new JTextField();
        panel7.add(file, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Security Level");
        panel7.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Encoding");
        panel7.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("File");
        panel7.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectFileButton = new JButton();
        selectFileButton.setText("Select File");
        panel7.add(selectFileButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel8, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel8.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        out = new JTextArea();
        out.setEditable(false);
        out.setLineWrap(true);
        out.setText("");
        out.setWrapStyleWord(true);
        scrollPane2.setViewportView(out);
        final JScrollPane scrollPane3 = new JScrollPane();
        panel8.add(scrollPane3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        error = new JTextArea();
        error.setEditable(false);
        error.setLineWrap(true);
        error.setWrapStyleWord(true);
        scrollPane3.setViewportView(error);
        in = new JTextField();
        panel8.add(in, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Standard Out");
        panel8.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Standard Error");
        panel8.add(label6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Standard In");
        panel8.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }
}
