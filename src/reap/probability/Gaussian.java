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

package reap.probability;

import java.security.SecureRandom;

/******************************************************************************
 *  Compilation:  javac Gaussian.java
 *  Execution:    java Gaussian x mu sigma
 * <p>
 *  Function to compute the Gaussian pdf (probability density function)
 *  and the Gaussian cdf (cumulative density function)
 * <p>
 *  % java Gaussian 820 1019 209
 *  0.17050966869132111
 * <p>
 *  % java Gaussian 1500 1019 209
 *  0.9893164837383883
 * <p>
 *  % java Gaussian 1500 1025 231
 *  0.9801220907365489
 * <p>
 *  The approximation is accurate to absolute error less than 8 * 10^(-16).
 *  Reference: Evaluating the Normal Distribution by George Marsaglia.
 *  <a href="https://www.jstatsoft.org/article/view/v011i04">...</a>
 *  <a href="https://introcs.cs.princeton.edu/java/22library/Gaussian.java.html">...</a>
 ******************************************************************************/

public class Gaussian {

    private static final SecureRandom random = new SecureRandom();

    // return pdf(x) = standard Gaussian pdf
    public static double pdf(double x) {
        return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
    }

    // return pdf(x, mu, sigma) = Gaussian pdf with mean mu and stddev sigma
    public static double pdf(double x, double mu, double sigma) {
        return pdf((x - mu) / sigma) / sigma;
    }

    // return cdf(z) = standard Gaussian cdf using Taylor approximation
    public static double cdf(double z) {
        if (z < -8.0) return 0.0;
        if (z >  8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum  = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * pdf(z);
    }

    // return cdf(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
    public static double cdf(double z, double mu, double sigma) {
        return cdf((z - mu) / sigma);
    }

    // Compute z such that cdf(z) = y via bisection search
    public static double inverseCDF(double y) {
        return inverseCDF(y, 0.00000001, -8, 8);
    }

    // bisection search
    private static double inverseCDF(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (cdf(mid) > y) return inverseCDF(y, delta, lo, mid);
        else              return inverseCDF(y, delta, mid, hi);
    }

    // pdf; get the probability of X
    // cdf: get probability P(x <= X) -> value between 0 and 1
    public static boolean eventOccurred(double probability, double mu, double sigma) {
        double randomValue = cdf(random.nextGaussian(mu, sigma), mu, sigma); // Generate a random Gaussian value
        return randomValue <= probability; // Check if the event occurs
    }

    // return phi(x) = standard Gaussian pdf
    @Deprecated
    public static double phi(double x) {
        return pdf(x);
    }

    // return phi(x, mu, sigma) = Gaussian pdf with mean mu and stddev sigma
    @Deprecated
    public static double phi(double x, double mu, double sigma) {
        return pdf(x, mu, sigma);
    }

    // return Phi(z) = standard Gaussian cdf using Taylor approximation
    @Deprecated
    public static double Phi(double z) {
        return cdf(z);
    }

    // return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
    @Deprecated
    public static double Phi(double z, double mu, double sigma) {
        return cdf(z, mu, sigma);
    }

    // Compute z such that Phi(z) = y via bisection search
    @Deprecated
    public static double PhiInverse(double y) {
        return inverseCDF(y);
    }

    // test client
    public static void main(String[] args) {
        double z     = Double.parseDouble(args[0]);
        double mu    = Double.parseDouble(args[1]);
        double sigma = Double.parseDouble(args[2]);
        System.out.println(cdf(z, mu, sigma));
        double y = cdf(z);
        System.out.println(inverseCDF(y));
    }
}
