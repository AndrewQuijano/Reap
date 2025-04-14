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

import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ClassView;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SelectBlock extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private final HashMap<String, Class> name_to_block_map = new HashMap<>();
    private final JList<String> stringList;
    private final List<Class> selected_blocks = new ArrayList<>();

    public static List<Class> getAllBlocksinBDD(DiagramPresentationElement bdd) {
        List<PresentationElement> elements = Objects.requireNonNull(bdd).getPresentationElements();
        List<Class> blocks = new ArrayList<>();

        for (PresentationElement el : elements) {
            if (el.getHumanType().equals("Block")) {
                blocks.add(((ClassView) el).getElement());
            }
        }
        return blocks;
    }

    public SelectBlock(DiagramPresentationElement bdd) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(500, 600);
        setTitle("Select Blocks from the BDD you want to include in your REAP simulation!");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        List<Class> all_blocks = getAllBlocksinBDD(bdd);
        List<String> all_block_names = new ArrayList<>();

        for (Class block : all_blocks) {
            all_block_names.add(block.getName());
            name_to_block_map.put(block.getName(), block);
        }

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String s : all_block_names) {
            listModel.addElement(s);
        }
        stringList = new JList<>(listModel);
        stringList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        contentPane.add(new JScrollPane(stringList), BorderLayout.CENTER);

        buttonPanel.add(buttonOK);
        buttonPanel.add(buttonCancel);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setContentPane(contentPane);
    }

    private void onOK() {
        selected_blocks.clear();
        for (String selected_block : stringList.getSelectedValuesList()) {
            selected_blocks.add(name_to_block_map.get(selected_block));
        }
        if (selected_blocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one block to continue.", "No Blocks Selected", JOptionPane.ERROR_MESSAGE);
        }
        else {
            dispose();
        }
    }

    private void onCancel() {
        dispose();
    }

    public List<Class> getSelectedBlocks() {
        return selected_blocks;
    }
}