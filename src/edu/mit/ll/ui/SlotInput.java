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
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


public class SlotInput extends JDialog {
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

    private final HashMap<String, HashMap<String, Double>> security_weights;

    // The page aligns with a specific security property opened in the GUI,
    // e.g., encryption-in-transit, encryption-at-rest, etc.
    private int current_page = 0;
    private final String [] security_property_page_number;

    // For now, the input is the config_file.json
    public SlotInput(HashMap<String, HashMap<String, Double>> security_weights) {
        // Thing I will allow user to modify via GUI
        this.security_weights = security_weights;
        security_property_page_number = security_weights.keySet().toArray(new String[0]);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(800, 650);
        setTitle("Security Property Weight Adjustment");

        scrollList = new JPanel();
        scrollList.setLayout(new FlowLayout());

        scrollPane = new JScrollPane();
        mainPanel.add(scrollPane);

        setNameHolder("Modify " + security_property_page_number[current_page] + " Weights");
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

    private boolean updateSlots() {
        boolean valid_slots = validSlotValues();
        if(valid_slots) {
            String current_security_property = security_property_page_number[current_page];
            HashMap<String, Double> temp = security_weights.get(current_security_property);

            for (JPanel panel : panels) {
                JLabel label = (JLabel) panel.getComponent(0);
                JTextField textField = (JTextField) panel.getComponent(1);

                String key = label.getText();
                String valueStr = textField.getText();
                Double value = Double.valueOf(valueStr);
                temp.put(key, value);
            }
            security_weights.put(current_security_property, temp);
        }
        return valid_slots;
    }

    private void onOK() {
        if(updateSlots()) {
            dispose();
        }
    }

    private void onCancel() {
        dispose();
    }

    private void setPageHolder() {
        pageHolder.setText((current_page + 1) + " / " + security_weights.size());
    }

    private void onLeft() {
        updateSlots();
        current_page--;
        if(current_page < 0) {
            current_page = security_weights.size() - 1;
        }
        resetWindow();
    }

    private void onRight() {
        updateSlots();
        current_page++;
        if(current_page == security_weights.size()) {
            current_page = 0;
        }
        resetWindow();
    }

    private void resetWindow() {
        setNameHolder("Modify " + security_property_page_number[current_page] + " weights");
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
        String current_security_property = security_property_page_number[current_page];
        HashMap<String, Double> temp = security_weights.get(current_security_property);

        for (Map.Entry<String, Double> entry : temp.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();

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
        double value;
        boolean ok_slots = true;

        for (JPanel panel : panels) {
            JLabel label = (JLabel) panel.getComponent(0);
            JTextField textField = (JTextField) panel.getComponent(1);

            String key = label.getText();
            String valueStr = textField.getText();

            // I need to confirm this value is a valid probability, turn red if not
            try {
                value = Double.parseDouble(valueStr);
                if (key.equals("None")) {
                    if (value != 1.0) {
                        JOptionPane.showMessageDialog(null, "None MUST be set 1.0, since no security property should NOT decrease risk!");
                        ok_slots = false;
                        textField.setBackground(invalidColor);
                    }
                    else {
                        textField.setBackground(validColor);
                    }
                }
                else {
                    if (value < 0 || value > 1) {
                        JOptionPane.showMessageDialog(null, "The security weights must be a number between 0 and 1.");
                        ok_slots = false;
                        textField.setBackground(invalidColor);
                    }
                    else {
                        textField.setBackground(validColor);
                    }
                }
            }
            catch (NumberFormatException e) {
                ok_slots = false;
                textField.setBackground(invalidColor);
                JOptionPane.showMessageDialog(null, "The security weights must be a number between 0 and 1.");
            }
        }
        return ok_slots;
    }

    // Get the latest value from the text fields
    public HashMap<String, HashMap<String, Double>> getCurrentWeights() {
        return this.security_weights;
    }
}
