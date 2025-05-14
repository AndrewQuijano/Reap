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

import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ClassView;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;

import java.util.*;
import java.util.stream.Collectors;


public class BDDInfo {

    List<Class> blocks;
    public Set<Class> removedDerived;
    LinkedHashSet<Association> associations;
    List<Generalization> gens;
    HashMap<Class, Integer> blockMultiplicity;
    DiagramPresentationElement bdd;
    List<String> blockNames;

    public BDDInfo(DiagramPresentationElement bdd) {
        this.bdd = bdd;
        blocks = new ArrayList<>();
        removedDerived = new HashSet<>();
        associations = new LinkedHashSet<>();
        gens = new ArrayList<>();
        blockMultiplicity = new HashMap<>();
        blockNames = new ArrayList<>();
    }

    public List<Class> getBlocks() {
        return this.blocks;
    }

    // Removes derived blocks from the list of blocks
    public void removeDerivedBlocks() {
        for(Class block : this.blocks) {
            System.out.println("Block: " + block.getName());
            for(Classifier d : ModelHelper.getDerivedClassifiers(block)) {
                this.removedDerived.add((Class)d);
            }
        }

        this.blocks = this.blocks.stream().filter(block -> !this.removedDerived.contains(block)).collect(Collectors.toList());

        for(Class b : this.blocks) {
            this.blockNames.add(b.getName());
        }
    }

    // Collect all blocks in the current diagram, do NOT go for nested blocks just yet
    public static BDDInfo getAllBlockInfo(DiagramPresentationElement bdd) {

        List<PresentationElement> elements = Objects.requireNonNull(bdd).getPresentationElements();
        BDDInfo bddInfo = new BDDInfo(bdd);

        for(PresentationElement el : elements) {
            Element e = el.getElement();
            // There are types, 'Block', 'Association' and 'Diagram', and of course, NULL
            System.out.println("Human Type: " + (e != null ? e.getHumanType() : null));

            if(el.getHumanType().equals("Block")) {
                bddInfo.blocks.add(((ClassView) el).getElement());
            }
            else if(el.getHumanType().equals("Association")) {
                bddInfo.associations.add((Association) el.getElement());
            }
        }

        // Classes found
        System.out.println("The Classes I found are");
        for(Class b : bddInfo.blocks) {
            System.out.println(b.getName());
        }
        return bddInfo;
    }

    // TODO: Only works with 'Directed Composition' arrow, failed with Association and Item flow
    public static List<Class> searchConnectedBlocks(Class parent) {

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

                Class child = (Class) ends[0];
                if(ends[0].getID().equals(parent.getID())) {
                    System.out.println("Skipping because " + parent.getName() + " is the child here");
                    continue;
                }

                System.out.println("\tThe child is " + child.getName());
                children.add(child);
                System.out.println("Adding the association: " + ends[0].getHumanName() + " --> " + ends[1].getHumanName());
            }
        }
        return children;
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