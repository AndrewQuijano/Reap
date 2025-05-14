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

package reap.sysml;

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
