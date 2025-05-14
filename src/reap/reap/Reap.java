
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

package reap.reap;

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