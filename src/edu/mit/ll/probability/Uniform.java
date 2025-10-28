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

package edu.mit.ll.probability;

import java.security.SecureRandom;

public class Uniform {

    private final SecureRandom random = new SecureRandom();
    private final int total_probability_space;

    public Uniform(int total_probability_space) {
        this.total_probability_space = total_probability_space;
    }

    public double get_probability(int event_space) {
        double probability = (double) event_space / this.total_probability_space;
        return Math.min(probability, 1.0);
    }

    public boolean eventOccurred(double probability) {
        int randomValue = random.nextInt(1, this.total_probability_space + 1);
        return get_probability(randomValue) <= probability; // Check if the event occurs
    }
}
