// ------------------------------------------------------------------------------
// --  ______  __________
// --  \    / /_____    /
// --   |  | /      |  |
// --   |  |   --   |  |
// --   |  |  |\/|  |  |
// --   |  |  |/\|  |  |
// --   |  |  |/\|  |  |
// --   |  |   --   |  |
// --   |  |_____ / |  |
// --  /_________/ /____\
// ------------------------------------------------------------------------------
/*
 * DISTRIBUTION STATEMENT A. Approved for public release. Distribution is unlimited.
 * This material is based upon work supported by the Dept of the Navy under Air
 * Force Contract No. FA8702-15-D-0001 or FA8702-25-D-B002.
 * Any opinions, findings, conclusions or recommendations expressed in this material
 * are those of the author(s) and do not necessarily reflect the views of the Dept
 * of the Navy.
 * (c) 2024 Massachusetts Institute of Technology.
 * The software/firmware is provided to you on an As-Is basis.
 * Delivered to the U.S. Government with Unlimited Rights, as defined in DFARS Part
 * 252.227-7013 or 7014 (Feb 2014).
 * Notwithstanding any copyright notice, U.S. Government rights in this work are
 * defined by DFARS 252.227-7013 or DFARS 252.227-7014 as detailed above.
 * Use of this work other than as specifically authorized by the U.S. Government may
 * violate any copyrights that exist in this work.
 */

package edu.mit.ll.ui;

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
