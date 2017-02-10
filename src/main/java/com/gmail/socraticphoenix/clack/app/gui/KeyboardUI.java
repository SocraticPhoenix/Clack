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

import com.gmail.socraticphoenix.clack.parse.TokenGroups;
import com.gmail.socraticphoenix.clack.program.ClackRegistry;
import com.gmail.socraticphoenix.nebula.string.Strings;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.stream.Stream;

public class KeyboardUI extends JPanel {
    private JPanel panel;

    private JFrame frame;

    private ClackUI clackUI;

    public KeyboardUI(ClackUI clackUI) {
        this.clackUI = clackUI;
        this.setupUI();
    }

    public JFrame display() {
        if(this.frame == null) {
            this.frame = new JFrame("Clack Keyboard");
            this.frame.setSize(1922, 640);
            this.frame.add(this.panel);
            this.frame.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    clackUI.setKeyboardVisible(false);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
        }
        this.frame.setVisible(true);
        return this.frame;
    }

    public void setupUI() {
        Font font = this.clackUI.getFont().deriveFont(13f);
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(TokenGroups.ROWS.length, Stream.of(TokenGroups.ROWS).sorted((a, b) -> Integer.compare(b.length(), a.length())).findFirst().get().length(), new Insets(0, 0, 0, 0), -1, -1));
        for (int i = 0; i < TokenGroups.ROWS.length; i++) {
            char[] row = TokenGroups.ROWS[i].toCharArray();
            for (int j = 0; j < row.length; j++) {
                char c = row[j];
                String name = c == ' ' ? "_" : Strings.escape(String.valueOf(c));
                String raw = String.valueOf(c);
                JButton button = new JButton(name);
                button.addActionListener(a -> this.clackUI.getProgram().append(String.valueOf(c)));
                button.setFont(font);

                String tip;
                if(TokenGroups.VARIABLES.indexOf(c) != -1) {
                    tip = "Load variable " + name + ".";
                } else if (TokenGroups.CONSTANTS.indexOf(c) != -1) {
                    if(ClackRegistry.hasConstant(raw)) {
                        tip = "Load constant " + ClackRegistry.constant(raw).getCanonical() + ", or append " + name + " to a multi-character constant.";
                    } else {
                        tip = "Append " + name + " to a multi-character constant.";
                    }
                } else if (TokenGroups.STACKS.indexOf(c) != -1) {
                    tip = "Load stack " + name + ".";
                } else if (TokenGroups.FUNCTIONS.indexOf(c) != -1) {
                    tip = "Call function " + name + ".";
                } else if (TokenGroups.SYNTAX.indexOf(c) != -1) {
                    tip = "Append syntax character " + name + ".";
                } else if (TokenGroups.WHITESPACE.indexOf(c) != -1) {
                    tip = "Append whitespace character " + name + ".";
                } else if (TokenGroups.CONSTANT_LENGTH_PRECURSORS.indexOf(c) != -1) {
                    tip = "Begin a " + (TokenGroups.CONSTANT_LENGTH_PRECURSORS.indexOf(c) + 1) + "-character constant.";
                } else if (TokenGroups.INSTRUCTION_LENGTH_PRECURSORS.indexOf(c) != -1) {
                    tip = "Begin a " + (TokenGroups.INSTRUCTION_LENGTH_PRECURSORS.indexOf(c) + 1) + "-character instruction.";
                } else if (TokenGroups.INSTRUCTIONS.indexOf(c) != -1) {
                    if(ClackRegistry.hasInstruction(raw)) {
                        tip = "Execute instruction " + ClackRegistry.instruction(raw).canonical() + ", or append " + name + " to a multi-character instruction.";
                    } else {
                        tip = "Append " + name + " to a multi-character instruction.";
                    }
                } else {
                    tip = "Append " + name + " to the program.";
                }
                button.setToolTipText(tip);

                Dimension size = new Dimension(50, 50);
                panel.add(button, new GridConstraints(i, j, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, size, size, size, 0, false));
            }
        }
    }

}
