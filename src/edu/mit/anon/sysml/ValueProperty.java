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

package edu.mit.anon.sysml;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.sysml.util.MDCustomizationForSysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.ValueSpecificationHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import javax.swing.*;

import static edu.mit.anon.sysml.Tagging.getTagType;
import static edu.mit.anon.sysml.logging.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


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
