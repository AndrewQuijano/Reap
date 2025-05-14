
/*
 * MIT License
 *
 * Copyright (c) 2025 MIT Lincoln Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package reap.ui;

import javax.swing.*;


public class MainMenu extends JDialog {
    private JPanel contentPane;
    private JButton adjust_config;
    private JButton adjust_weights;
    private JButton set_blocks;
    private JButton run_simulation;
    private JButton recompute;
    private JButton exit;

    private boolean adjustConfigSelected;
    private boolean adjustWeightsSelected;
    private boolean setBlocksSelected;
    private boolean runSimulationSelected;
    private boolean recomputeSelected;
    private boolean closeSelected;

    public MainMenu() {
        setContentPane(contentPane);
        setModal(true);
        setSize(800, 650);
        setTitle("REAP Main Menu");

        adjust_config.addActionListener(e -> {
            adjustConfigSelected = true;
            dispose();
        });

        adjust_weights.addActionListener(e -> {
            adjustWeightsSelected = true;
            dispose();
        });

        set_blocks.addActionListener(e -> {
            setBlocksSelected = true;
            dispose();
        });

        run_simulation.addActionListener(e -> {
            runSimulationSelected = true;
            dispose();
        });

        recompute.addActionListener(e -> {
            recomputeSelected = true;
            dispose();
        });

        exit.addActionListener(e -> {
            closeSelected = true;
            dispose();
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public boolean isAdjustConfigSelected() {
        return adjustConfigSelected;
    }

    public boolean isAdjustWeightsSelected() {
        return adjustWeightsSelected;
    }

    public boolean isSetBlocksSelected() {
        return setBlocksSelected;
    }

    public boolean isRunSimulationSelected() {
        return runSimulationSelected;
    }

    public boolean isRecomputeSelected() {
        return recomputeSelected;
    }

    public boolean exitSelected() {
        return closeSelected;
    }
}
