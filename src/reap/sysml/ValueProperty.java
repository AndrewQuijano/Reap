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

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.ValueSpecificationHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static reap.sysml.Tagging.getTagType;
import static reap.sysml.logging.log;


public class ValueProperty {

    // This function works to add a ValueProperty to a regular SysML Block
    // This also includes being able to overwrite a ValueProperty Value
    public static void createValueProperty(Project project, Class element, String tagName,
                                           Object tagValue) throws ReadOnlyElementException {

        if (!Objects.equals(element.getHumanType(), "Block")) {
            log("Element " + element.getName() + " is not a Block! Please try again!");
            return;
        }

        Type t = getTagType(project, tagValue);
        if (t == null) {
            JOptionPane.showMessageDialog(null, "Tag type " + tagValue.getClass().getName() + " is not supported! Please try again!");
            return;
        }

        // Delete any existing Property that has the tag name
        deleteValueProperty(project, element, tagName);

        // Create a temporary session just to add the ValueProperty
        SessionManager sesMan = SessionManager.getInstance();
        sesMan.createSession(project, "Create a ValueProperty for Block " + element.getName());

        Property p = project.getElementsFactory().createPropertyInstance();
        p.setVisibility(VisibilityKindEnum.PUBLIC);
        p.setName(tagName);
        p.setOwner(element);
        ValueSpecification vs = ValueSpecificationHelper.createValueSpecification(project, p.getType(), tagValue, Collections.emptySet());
        p.setDefaultValue(vs);
        p.setType(t);
        MDCustomizationForSysMLProfile.getInstanceByProject(project).valueProperty().apply(p);

        sesMan.closeSession(project);
    }

    public static void deleteValueProperty(Project project, Class element, String tagName) {

        if (!Objects.equals(element.getHumanType(), "Block")) {
            log("Element " + element.getName() + " is not a Block! Please try again!");
            return;
        }

        // Create a temporary session just to add the tag
        SessionManager sesMan = SessionManager.getInstance();
        sesMan.createSession(project, "Deleting a ValueProperty for Block " + element.getName());

        for(Property p : ModelHelper.getPropertiesWithoutRedefined(element)) {
            if(p.getHumanType().contains("Value")) {
                if (p.getName().equals(tagName)) {
                    p.refDelete();
                }
            }
        }
        sesMan.closeSession(project);
    }

    public static Object getValueProperty(Class element, String tagName) {

        if (!Objects.equals(element.getHumanType(), "Block")) {
            log("Element " + element.getName() + " is not a Block! Please try again!");
            return null;
        }

        for(Property p : getValueProperties(element)) {
            if (p.getName().equals(tagName)) {
                if (p.getDefaultValue() != null) {
                    return ValueSpecificationHelper.getValueSpecificationValue(p.getDefaultValue());
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }

    public static List<Property> getValueProperties(Class block) {
        List<Property> vProps = new ArrayList<>();
        assert block != null;
        for(Property p : ModelHelper.getPropertiesWithoutRedefined(block)) {
            if(p.getHumanType().contains("Value")) {
                vProps.add(p);
            }
        }
        return vProps;
    }
}
