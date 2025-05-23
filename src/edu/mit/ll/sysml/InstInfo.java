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

import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import java.util.*;

public class InstInfo {
    List<Class> blocks;                                                             // all blocks used to create instances
    List<String> blockNames;                                                        // names of the blocks
    public HashMap<Class, Integer> blockMultiplicity;                               // maps blocks to their multiplicities
    public HashMap<Class, Integer> instCounts;                                      // maps instances to their total counts
    List<InstanceSpecification> instances;                                          // all instances created for the new diagram
    HashMap<InstanceSpecification, List<InstanceSpecification>> instToChildren;     // maps instances to their children
    HashMap<Class, List<InstanceSpecification>> blockToInstances;                   // maps blocks to their instances
    List<String> instNames;                                                         // names of the new instances
    List<Classifier> instClassifiers;                                               // classifiers of the new instances
    LinkedHashSet<Association> associations;                                        // all associations used for links in the new diagram
    List<Integer> multList;                                                         // list of the multiplicities for the blocks
    HashMap<Class, List<Class>> blockToChildren;                                    // maps blocks to their children
    HashMap<Class, Class> blockToParentBlock;                                       // maps blocks to their parents
    HashMap<Class, List<Classifier>> blockToGens;                                   // maps blocks to their derived classifiers
    HashMap<Class, List<String>> blockToGenStrings;                                 // maps blocks to the names of their derived classifiers
    HashMap<InstanceSpecification, ShapeElement> instToShape;                       // maps new instances to their diagram shapes
    List<Integer> instNumbers;                                                      // maps instances to the index of their parent
    List<Integer> instParNumbers;                                                   // maps instances to the index of their parent
    public Set<Class> ignore;                                                       // blocks that can be ignored (used in searchBlocks())
    boolean cancelled;                                                              // whether the last window was canceled

    public InstInfo() {
        blocks = new ArrayList<>();
        blockNames = new ArrayList<>();
        blockMultiplicity = new HashMap<>();
        instCounts = new HashMap<>();
        instances = new ArrayList<>();
        instToChildren = new HashMap<>();
        blockToInstances = new HashMap<>();
        instNames = new ArrayList<>();
        instClassifiers = new ArrayList<>();
        associations = new LinkedHashSet<>();
        multList = new ArrayList<>();
        blockToChildren = new HashMap<>();
        blockToParentBlock = new HashMap<>();
        blockToGens = new HashMap<>();
        blockToGenStrings = new HashMap<>();
        instToShape = new HashMap<>();
        instNumbers = new ArrayList<>();
        instParNumbers = new ArrayList<>();
        ignore = new HashSet<>();
        cancelled = true;
    }

    // Used to recursively get all children (and child of child) of a block.
    public static void searchBlocks(InstInfo instInfo, Class parent) {
        instInfo.blocks.add(parent);
        instInfo.blockNames.add(parent.getName());
        instInfo.ignore.add(parent);
        System.out.println("searching in " + parent.getName() + "...");
        List<Classifier> derived = new ArrayList<>(ModelHelper.getDerivedClassifiers(parent));
        instInfo.blockToGens.putIfAbsent(parent, derived);
        List<String> derivedStrings = new ArrayList<>();
        for(Classifier c : derived) {
            derivedStrings.add(c.getName());
        }
        instInfo.blockToGenStrings.putIfAbsent(parent, derivedStrings);

        List<Class> children = new ArrayList<>();

        for(Iterator<Association> it = ModelHelper.associations(parent); it.hasNext(); ) {
            System.out.println("\n\tAssoc:");
            Association a = it.next();

            // ends - blocks
            Element[] ends = getAssocEnds(a);
            if(ends[0] == null || ends[1] == null) {
                continue;
            }

            // ends - properties
            NamedElement[] mends = getAssocMembers(a);
            if(mends[0] == null || mends[1] == null) {
                continue;
            }

            setOrder(ends, mends);

            if(validAssoc(ends, mends)) {
                instInfo.associations.add(a);

                Class child = (Class) ends[0];
                if(ends[0].getID().equals(parent.getID())) {
                    System.out.println("Skipping because " + parent.getName() + " is the child here");
                    continue;
                }

                System.out.println("\tThe child is " + child.getName());

                if(instInfo.ignore.contains(child)) {
                    continue;
                }
                children.add(child);
                System.out.println("Adding the association: " + ends[0].getHumanName() + " --> " + ends[1].getHumanName());
                instInfo.blockMultiplicity.putIfAbsent(child, getMultiplicity(ModelHelper.getMultiplicity((Property)mends[1])));
            }
        }

        instInfo.blockToChildren.put(parent, children);

        for(Class c : children) {
            instInfo.blockToParentBlock.putIfAbsent(c, parent);
            searchBlocks(instInfo, c);
            instInfo.instCounts.put(c, instInfo.blockMultiplicity.get(c));
        }
    }

    private static int getMultiplicity(String multiplicity_expression) {
        if(multiplicity_expression.isEmpty()) {
            return 1;
        }

        try {
            return Integer.parseInt(multiplicity_expression);
        }
        catch (NumberFormatException e) {

            int index = multiplicity_expression.indexOf("..");
            try {
                if(index == -1) {
                    return Integer.parseInt(multiplicity_expression);
                }
                int first = Integer.parseInt(multiplicity_expression.substring(0, index));
                if (first != 0) {
                    return first;
                }

                if(multiplicity_expression.substring(index + 2).equals("*")) {
                    return 1;
                }
                try {
                    return Integer.parseInt(multiplicity_expression.substring(index + 2));
                }
                catch (NumberFormatException | StringIndexOutOfBoundsException exc) {
                    System.out.println("Couldn't parse multiplicity \"" + multiplicity_expression + "\"");
                }
            }
            catch (NumberFormatException | StringIndexOutOfBoundsException exc) {
                System.out.println("Couldn't parse multiplicity \"" + multiplicity_expression + "\"");
            }
        }

        return 1;
    }

    private static boolean validAssoc(Element[] ends, NamedElement[] endTypes) {
        if(ends[0] == null || ends[1] == null) {
            return false;
        }

        if(ends[0] == ends[1] || ends[0].equals(ends[1])) {
            return false;
        }

        System.out.println("\tendTypes[0].getHumanType(): " + endTypes[0].getHumanType());
        System.out.println("\tendTypes[1].getHumanType(): " + endTypes[1].getHumanType());

        return endTypes[0].getHumanType().equals("Property") &&
                endTypes[1].getHumanType().equals("Part Property");
    }

    private static Object[] setOrder(Object[] ends, NamedElement[] endTypes) {
        if(endTypes[0].getHumanType().contains("Part Property")) {
            Object temp = ends[0];
            ends[0] = ends[1];
            ends[1] = temp;
            System.out.println("\tResetting order to:");
            System.out.println("\t\t" + ((Element)ends[0]).getHumanName());
            System.out.println("\t\t" + ((Element)ends[1]).getHumanName());
        }
        return ends;
    }

    private static NamedElement[] getAssocMembers(Association assoc) {
        Iterator<NamedElement> iteration = assoc.getMember().iterator();
        NamedElement[] ends = { null, null };
        if(iteration.hasNext()) {
            ends[0] = iteration.next();
        }
        if(iteration.hasNext()) {
            ends[1] = iteration.next();
        }
        return ends;
    }

    private static Element[] getAssocEnds(Association assoc) {
        Iterator<Element> iterator = assoc.getRelatedElement().iterator();
        Element[] ends = { null, null };
        if(iterator.hasNext()) {
            ends[0] = iterator.next();
            System.out.println("\t(0): " + ends[0].getHumanName());
        }
        if(iterator.hasNext()) {
            ends[1] = iterator.next();
            System.out.println("\t(1): " + ends[1].getHumanName());
        }
        return ends;
    }
}
