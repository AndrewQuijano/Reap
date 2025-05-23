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

package edu.mit.ll.sysml;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import javax.annotation.Nullable;
import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * So after some consideration, what I noticed is the following
 * Currently, this will create a Tag for the Block StereoType.
 * Meaning ALL blocks will get the property for tags to be attached,
 * However, the tag value is unique to each block.
 * After some consideration, I decided to stick with using ValueProperty
 * instead of Tags.
 * But you should be able to simulate using values from Tags as well.
 */
public class Tagging {

    // This function works to add a tag to a regular SysML Block
    // This also includes being able to overwrite a Tag Value
    public static void createTaggedValue(Project project, Class element, String tagName,
                                         Object tagValue) throws ReadOnlyElementException {

        if (!Objects.equals(element.getHumanType(), "Block")) {
            //JOptionPane.showMessageDialog(null, "Element " + element.getName() + " is not a Block! Please try again!");
            System.out.println("Element " + element.getName() + " is not a Block! Please try again!");
            return;
        }

        // Lately, you need to also provide the profile for StereotypeHelper to work correctly.
        // You need to know which Profile the Stereotype belongs to for which you want to edit.
        Profile profile = StereotypesHelper.getProfile(project, "SysML");
        if (profile == null) {
            JOptionPane.showMessageDialog(null, "Profile not found! Please try again!");
            return;
        }

        Stereotype block_stereotype = StereotypesHelper.getStereotype(project, "Block", profile);
        if (block_stereotype == null) {
            JOptionPane.showMessageDialog(null, "Stereotype not found! Please try again!");
            return;
        }

        Type t = getTagType(project, tagValue);
        if (t == null) {
            JOptionPane.showMessageDialog(null, "Tag type " + tagValue.getClass().getName() + " is not supported! Please try again!");
            return;
        }

        // Create a temporary session just to add the tag
        SessionManager sesMan = SessionManager.getInstance();
        sesMan.createSession(project, "Create a Tag for Block " + element.getName());

        Property p = StereotypesHelper.getPropertyByName(block_stereotype, tagName);
        if(p == null) {
            System.out.println("Have to create a property for " + tagName);
            p = project.getElementsFactory().createPropertyInstance();
            p.setVisibility(VisibilityKindEnum.PUBLIC);
            p.setName(tagName);
            p.setOwner(block_stereotype);
        }
        else {
            System.out.println("Property for " + tagName + " already existed :)");
        }
        p.setType(t);

        // Rather than build a new Stereotype, find the default Stereotype for blocks and append tags there.
        // In the original code, he wanted to add tags to Diagram, so he used "DiagramInfo", but in my case, I wanted "Block" for blocks
        TaggedValue tv = StereotypesHelper.getTaggedValue(element, block_stereotype, tagName);
        // Use this to delete existing tag and overwrite it
        if(tv != null) {
            tv.refDelete();
        }

        if (tagValue instanceof String) {
            tv = project.getElementsFactory().createStringTaggedValueInstance();
            tv.setTagDefinition(p);
            ((List<String>) tv.getValue()).add((String) tagValue);
        }
        if (tagValue instanceof Integer) {
            tv = project.getElementsFactory().createIntegerTaggedValueInstance();
            tv.setTagDefinition(p);
            ((List<Integer>) tv.getValue()).add((Integer) tagValue);
        }
        if (tagValue instanceof BigDecimal) {
            tv = project.getElementsFactory().createRealTaggedValueInstance();
            tv.setTagDefinition(p);
            ((List<Double>) tv.getValue()).add(((BigDecimal) tagValue).doubleValue());
        }
        if (tagValue instanceof Double) {
            tv = project.getElementsFactory().createRealTaggedValueInstance();
            tv.setTagDefinition(p);
            ((List<Double>) tv.getValue()).add((Double) tagValue);
        }
        if (tagValue instanceof Boolean) {
            tv = project.getElementsFactory().createBooleanTaggedValueInstance();
            tv.setTagDefinition(p);
            ((List<Boolean>) tv.getValue()).add((Boolean) tagValue);
        }
        else {
            JOptionPane.showMessageDialog(null, "Tagging this primitive data type is NOT supported: " + tagName + ": " + tagValue.toString() + " " + tagValue.getClass().getName());
            return;
        }

        element.getTaggedValue().add(tv);

        // Close your session
        sesMan.closeSession(project);
    }

    public static void deleteTaggedValue(Project project, Class element, String tagName) {

        if (!Objects.equals(element.getHumanType(), "Block")) {
            //JOptionPane.showMessageDialog(null, "Element " + element.getName() + " is not a Block! Please try again!");
            System.out.println("Element " + element.getName() + " is not a Block! Please try again!");
            return;
        }

        // Lately, you need to also provide the profile for StereotypeHelper to work correctly.
        // You need to know which Profile the Stereotype belongs to for which you want to edit.
        Profile profile = StereotypesHelper.getProfile(project, "SysML");
        if (profile == null) {
            JOptionPane.showMessageDialog(null, "Profile not found! Please try again!");
            return;
        }

        Stereotype block_stereotype = StereotypesHelper.getStereotype(project, "Block", profile);
        if (block_stereotype == null) {
            JOptionPane.showMessageDialog(null, "Stereotype not found! Please try again!");
            return;
        }

        Property p = StereotypesHelper.getPropertyByName(block_stereotype, tagName);
        TaggedValue tv = StereotypesHelper.getTaggedValue(element, block_stereotype, tagName);

        // Create a temporary session just to add the tag
        SessionManager sesMan = SessionManager.getInstance();
        sesMan.createSession(project, "Delete a Tag for Block " + element.getName());

        if(tv != null) {
            tv.refDelete();
        }
        if(p != null) {
            p.refDelete();
        }

        sesMan.closeSession(project);
    }

    @Nullable
    public static Object getTaggedValue(Project project, Class element, String tagName) {

        // Lately, you need to also provide the profile for StereotypeHelper to work correctly.
        // You need to know which Profile the Stereotype belongs to for which you want to edit.
        Profile profile = StereotypesHelper.getProfile(project, "SysML");
        if (profile == null) {
            JOptionPane.showMessageDialog(null, "Profile not found! Please try again!");
            return null;
        }

        Stereotype block_stereotype = StereotypesHelper.getStereotype(project, "Block", profile);
        if (block_stereotype == null) {
            JOptionPane.showMessageDialog(null, "Stereotype not found! Please try again!");
            return null;
        }

        // https://docs.nomagic.com/display/MD2021x/Retrieving+tag+values
        TaggedValue tv = StereotypesHelper.getTaggedValue(element, block_stereotype, tagName);
        if (tv == null) {
            //JOptionPane.showMessageDialog(null, "Tagged Value not found on " + element.getName() + "! Please try again!");
            System.out.println("Tagged Value not found on " + element.getName() + "! Please try again!");
            return null;
        }
        else {
            return tv.getValue().get(0);
        }
    }

    @Nullable
    public static DataType getTagType(Project project, Object tagValue) {
        DataType t = null;
        if (tagValue instanceof String) {
            t = SysMLProfile.getInstanceByProject(project).getString();
        }
        else if (tagValue instanceof Integer) {
            t = SysMLProfile.getInstanceByProject(project).getInteger();
        }
        else if (tagValue instanceof BigDecimal) {
            t = SysMLProfile.getInstanceByProject(project).getReal();
        }
        else if (tagValue instanceof Double) {
            t = SysMLProfile.getInstanceByProject(project).getReal();
        }
        else if (tagValue instanceof Boolean) {
            t = SysMLProfile.getInstanceByProject(project).getBoolean();
        }
        return t;
    }
}
