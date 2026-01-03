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

package edu.mit.anon.ui;

import javax.swing.*;

import static edu.mit.anon.input_validation.InputValidation.check_config_key;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConfigInput extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JScrollPane scrollPane;
    private JPanel mainPanel;
    private JButton buttonLeft;
    private JButton buttonRight;
    private JLabel nameHolder;
    private JLabel pageHolder;
    private final JPanel scrollList;

    private List<JTextField> textFields;
    private List<JPanel> panels;

    final Color invalidColor = new Color(255, 141, 141);
    final Color validColor = new JTextField().getBackground();

    private final HashMap<String, Object> config;

    // For now, the input is the config_file.json
    public ConfigInput(HashMap<String, Object> config) {
        this.config = config;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(800, 650);
        setTitle("Security Property Weight Adjustment");

        scrollList = new JPanel();
        scrollList.setLayout(new FlowLayout());

        scrollPane = new JScrollPane();
        mainPanel.add(scrollPane);

        setNameHolder("Modify Config Options");
        setPageHolder();
        fillSlotList();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                addPropertyChangeListener("focusOwner", evt -> {
                    if (!(evt.getNewValue() instanceof JTextField focused)) {
                        return;
                    }

                    int index = 0;
                    for (JTextField tf : textFields) {
                        if (focused == tf) {
                            break;
                        }
                        index++;
                    }

                    if (40 * index + 5 >= scrollPane.getVerticalScrollBar().getValue() && 40 * index + 5 <= scrollPane.getVerticalScrollBar().getValue() + scrollPane.getHeight() - 20)
                        return;

                    if (40 * index + 5 > scrollPane.getVerticalScrollBar().getValue()) {
                        scrollList.scrollRectToVisible(new Rectangle(scrollPane.getX(), 40 * index + 5 - scrollPane.getHeight() + 45, scrollPane.getWidth(), scrollPane.getHeight()));
                    } else {
                        scrollList.scrollRectToVisible(new Rectangle(scrollPane.getX(), 40 * index + 5, scrollPane.getWidth(), scrollPane.getHeight()));
                    }
                });

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        buttonLeft.addActionListener(e -> onLeft());
        buttonRight.addActionListener(e -> onRight());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if(validSlotValues()) {
            for (JPanel panel : panels) {
                JLabel label = (JLabel) panel.getComponent(0);
                JTextField textField = (JTextField) panel.getComponent(1);

                String key = label.getText();
                String value = textField.getText().toLowerCase();

                switch (key) {
                    case "uniform_size", "days", "simulations", "backup_frequency", "rto", "rpo" -> {
                        Integer intValue = Integer.parseInt(value);
                        config.put(key, intValue);
                    }
                    case "threat_likelihood", "patch_likelihood" -> {
                        Double doubleValue = Double.valueOf(value);
                        config.put(key, doubleValue);
                    }
                    case "populate_blocks" -> config.put(key, Boolean.parseBoolean(value));
                    default -> config.put(key, value);
                }
            }
            dispose();
        }
    }

    private void onCancel() {
        dispose();
    }

    private void setPageHolder() {
        // Should only be one page of config, hard coding this
        int current_page = 0;
        pageHolder.setText((current_page + 1) + " / " + (current_page + 1));
    }

    private void onLeft() {
        resetWindow();
    }

    private void onRight() {
        resetWindow();
    }

    private void resetWindow() {
        setNameHolder("Modify Config options");
        setPageHolder();
        fillSlotList();
    }

    // Set the title of the GUI
    private void setNameHolder(String title) {
        nameHolder.setText(title);
    }

    // Initialize the text fields for user to input updated weights or configuration
    private void fillSlotList() {
        mainPanel.remove(scrollPane);
        List<JLabel> labels = new ArrayList<>();
        textFields = new ArrayList<>();
        panels = new ArrayList<>();
        scrollList.removeAll();

        // This is the data structure I want people to dynamically update from JSON if needed
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // labels creates both the title of the text to the left
            labels.add(new JLabel(key));
            labels.get(labels.size() - 1).setSize(new Dimension(250, 25));
            labels.get(labels.size() - 1).setPreferredSize(new Dimension(250, 25));

            // This is to create the text field to read input from a user,
            // For now I have this aligning with what the JSON initially provided.
            JTextField tf = new JTextField();
            textFields.add(tf);
            tf.setSize(new Dimension(250, 25));
            tf.setPreferredSize(new Dimension(250, 25));
            tf.setLocation(260, 40 * textFields.size());
            tf.setText(value.toString());

            tf.addActionListener(e -> {
                if(tf.getText().isEmpty()) {
                    tf.setBackground(validColor);
                }
            });

            tf.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {

                }

                @Override
                public void focusLost(FocusEvent e) {
                    if(tf.getText().isEmpty()) {
                        tf.setBackground(validColor);
                    }
                }
            });

            // The panel contains both input box and description text to the right
            panels.add(new JPanel());
            panels.get(panels.size() - 1).add(labels.get(labels.size() - 1));
            panels.get(panels.size() - 1).add(textFields.get(textFields.size() - 1));
            scrollList.add(panels.get(panels.size() - 1));
        }

        // Allows you to scroll up and down the text and input boxes
        scrollList.setPreferredSize(new Dimension(780, 40 * labels.size() + 6));
        scrollPane = new JScrollPane(scrollList);
        scrollPane.setPreferredSize(new Dimension(780, 500));

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane);
    }

    // Confirm the input is OK
    private boolean validSlotValues() {
        boolean ok_slots = true;

        for (JPanel panel : panels) {
            JLabel key_label = (JLabel) panel.getComponent(0);
            JTextField textField = (JTextField) panel.getComponent(1);
            String key = key_label.getText();
            // Careful you need to make the parseInt/parseDouble here too
            String value = textField.getText().toLowerCase();

            switch (key) {
                case "uniform_size", "days", "simulations", "backup_frequency", "rto", "rpo" -> {
                    try {
                        Integer intValue = Integer.parseInt(value);
                        // I need to confirm this value is OK for the config, recycle method in Input Validation
                        if (check_config_key(key, intValue)) {
                            textField.setBackground(validColor);
                        } else {
                            ok_slots = false;
                            textField.setBackground(invalidColor);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Config: REAP expects an Integer for the " + key + " key");
                        ok_slots = false;
                        textField.setBackground(invalidColor);
                    }
                }
                case "threat_likelihood", "patch_likelihood" -> {
                    // I need to confirm this value is OK for the config, recycle method in Input Validation
                    try {
                        Double doubleValue = Double.valueOf(value);
                        if (check_config_key(key, doubleValue)) {
                            textField.setBackground(validColor);
                        } else {
                            ok_slots = false;
                            textField.setBackground(invalidColor);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Config: REAP expects an Decimal for the " + key + " key");
                        ok_slots = false;
                        textField.setBackground(invalidColor);
                    }
                }
                case "populate_blocks" -> {
                    if (value.equalsIgnoreCase("true")) {
                        Boolean boolValue = Boolean.parseBoolean(value);
                        if (check_config_key(key, boolValue)) {
                            textField.setBackground(validColor);
                        } else {
                            ok_slots = false;
                            textField.setBackground(invalidColor);
                        }
                    } else if (value.equalsIgnoreCase("false")) {
                        Boolean boolValue = Boolean.parseBoolean(value);
                        if (check_config_key(key, boolValue)) {
                            textField.setBackground(validColor);
                        } else {
                            ok_slots = false;
                            textField.setBackground(invalidColor);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Config: REAP expects an Boolean for the " + key + " key, expecting string 'true' or 'false'");
                        ok_slots = false;
                        textField.setBackground(invalidColor);
                    }
                }
                default -> {
                    // I need to confirm this value is OK for the config, recycle method in Input Validation
                    if (check_config_key(key, value)) {
                        textField.setBackground(validColor);
                    } else {
                        ok_slots = false;
                        textField.setBackground(invalidColor);
                    }
                }
            }
        }
        return ok_slots;
    }

    // Get the latest value from the text fields
    public HashMap<String, Object> getConfig() {
        return this.config;
    }
}
