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

package edu.mit.ll.reap;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.plugins.Plugin;
import javax.swing.*;
import java.io.File;

public class Reap extends Plugin {

    @Override
    public void init()
    {
        ActionsConfiguratorsManager.getInstance().addMainToolbarConfigurator(new AMConfigurator()
        {
            public void configure(ActionsManager actionsManager) {
                ActionsCategory category = actionsManager.getCategory("TOOLBAR_CATEGORY");
                if(category == null) {
                    category = new ActionsCategory("TOOLBAR_CATEGORY", "Toolbar Category");
                    actionsManager.addCategory(category);
                }
                category.addAction(new OpenReap());
            }
            public int getPriority() {
                return AMConfigurator.MEDIUM_PRIORITY;
            }
        });
    }

    @Override
    public boolean close() {
        return true;
    }

    public static String selectWorkingDirectory(String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(title);

        while (true) {
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                if (selectedDirectory.isDirectory() && selectedDirectory.exists()) {
                    return selectedDirectory.getAbsolutePath();
                }
                else {
                    JOptionPane.showMessageDialog(null, "Invalid directory selected. Please try again.");
                }
            }
            else if (result == JFileChooser.CANCEL_OPTION) {
                JOptionPane.showMessageDialog(null, "No Working Directory selected.");
                System.exit(0);
            }
            else {
                JOptionPane.showMessageDialog(null, "No directory selected. Please try again.");
            }
        }
    }

    public static void showWorkingDirectoryDialog() {
        String currentDirectory = System.getProperty("user.dir");
        String message = "This is your current working directory:\n" + currentDirectory +
                "\n\nPress 'Change CWD' to update the working directory (where you read " +
                "input files/output files are written here) or 'OK' to continue.";

        Object[] options = {"Change CWD", "OK"};
        int choice = JOptionPane.showOptionDialog(null,
                message,
                "Current Working Directory",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[1]);

        if (choice == 0) {
            System.setProperty("user.dir", selectWorkingDirectory(
                                   """
                                   Select Working Directory
                                   Ideally, this should be the directory where you downloaded the REAP Git repository.
                                   """
            ));
        }
    }

    // plugin can check here for specific conditions
    // if false is returned plugin is not loaded.
    @Override
    public boolean isSupported() {
        return true;
    }
}