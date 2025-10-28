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

package edu.mit.ll.sysml;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.notification.NotificationManager;
import com.nomagic.magicdraw.ui.notification.NotificationSeverity;

import javax.swing.*;
import java.util.Locale;

public class logging {
    public static void log(String message, String title, String severity) {
        if(severity == null) {
            log(message, title, NotificationSeverity.INFO);
        }
        else if(severity.toLowerCase(Locale.ROOT).contains("w")) {
            log(message, title, NotificationSeverity.WARNING);
        }
        else if(severity.toLowerCase(Locale.ROOT).contains("e")) {
            log(message, title, NotificationSeverity.ERROR);
        }
        else {
            log(message, "REAP Plugin Message", NotificationSeverity.INFO);
        }
    }

    public static void log(String message, String title, NotificationSeverity ns) {
        System.out.println("log - " + message);
        if(NotificationManager.getInstance() != null) {
            NotificationManager nm = NotificationManager.getInstance();
        }
        else {
            if(Application.getInstance().getGUILog() != null) {
                Application.getInstance().getGUILog().showError(message);
            }
            else {
                JOptionPane.showMessageDialog(null, message);
            }
        }
    }

    public static void log(String message) {
        log(message, "CSV Exporter Plugin Message", NotificationSeverity.INFO);
    }
}
