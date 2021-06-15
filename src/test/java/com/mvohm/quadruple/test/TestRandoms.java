package com.mvohm.quadruple.test;

import java.util.Random;


import com.mvohm.quadruple.Quadruple;
import static com.mvohm.quadruple.test.AuxMethods.*;

/**
 * A simple utility to very roughly test the randomness of {@link Quadruple#nextRandom()}
 *
 * @author M.Vokhmentev
 */

public class TestRandoms {

  private static final long RAND_SEED       = 123123;
  private static final int SAMPLES_TO_TEST  = 10_000_000;
  private static final int SUBRANGES        = 10_000;

  public static void main(String[] args) {
    final Random rand = new Random(RAND_SEED);
    for (int i = 0; i < SAMPLES_TO_TEST; i++)
      examineSample(i, Quadruple.nextRandom(rand));

    showResults();
    say("Done!");
  }

  private static void examineSample(int count, Quadruple value) {
    if (value.exponent() <= Quadruple.EXPONENT_BIAS - 20)
      say("%5d: %s (%s)", count, value.toHexString(), value.format("%.6f"));

    // How often the bits of the mantissa occur to be 1
    countBits(value);

    // How uniformly is it distributed over the range
    accountInSubrange(value);
  }

  static long[] subrangeCounts = new long[SUBRANGES];

  private static void accountInSubrange(Quadruple value) {
    final int index = (int)(value.doubleValue() * SUBRANGES);
    subrangeCounts[index]++;
  }

  static long[] bitCounts = new long[128];

  private static void countBits(Quadruple value) {
    final long mantLo = value.mantLo();
    final long mantHi = value.mantHi();
    long mask = 1L;
    for (int pos = 0; pos < 64; pos++) {
      if ((mantLo & mask) != 0) bitCounts[pos]++;
      mask <<= 1;
    }
    mask = 1L;
    for (int pos = 0; pos < 64; pos++) {
      if ((mantHi & mask) != 0) bitCounts[pos + 64]++;
      mask <<= 1;
    }
  }

  private static void showResults() {
    say("\nBitCounts:");
    double mse = 0, mean = 0;
    for (int i = 0; i < bitCounts.length; i++) {
      say("%3d: %,11d (%5.3f).", i, bitCounts[i], 100.0 * bitCounts[i] / SAMPLES_TO_TEST);
      final double err = (bitCounts[i] - 0.5 * SAMPLES_TO_TEST);
      mse += err * err;
      mean += bitCounts[i];
    }
    mean /= bitCounts.length;
    say("σ = %.3f, σ/mean = %.6e", Math.sqrt(mse / bitCounts.length), Math.sqrt(mse / bitCounts.length)/mean);

    mse = mean = 0;
    say("\nSubranges:");
    for (int i = 0; i < SUBRANGES; i++) {
      if (i < 10 || i > SUBRANGES - 10)
        say("%3d: %,11d (%5.3f).", i, subrangeCounts[i], 100.0 * subrangeCounts[i] / SAMPLES_TO_TEST);
      if (i == 10)
        say("...........");
      final double err = (subrangeCounts[i] - (double)SAMPLES_TO_TEST/SUBRANGES);
      mse += err * err;
      mean += subrangeCounts[i];
    }
    mean /= SUBRANGES;
    say("σ = %.3f, σ/mean = %.6e\n", Math.sqrt(mse / SUBRANGES), Math.sqrt(mse / SUBRANGES)/mean);

    final boolean ok = isOK();
    say("Sadgewick says: OK = " + ok);
  }

  public static boolean isOK() {

    //PART B: Calculate chi-square - this approach is in Sedgewick
    final double n_r = (double)SAMPLES_TO_TEST / SUBRANGES;
    double chiSquare = 0;

    for (int i = 0; i < SUBRANGES; i++) {
      final double f = subrangeCounts[i] - n_r;
      chiSquare += f * f;
    }
    chiSquare /= n_r;

    say("chiSquare - r = %.3f, 2 * sqrt(r) = %.3f", (chiSquare - SUBRANGES), 2 * Math.sqrt(SUBRANGES));
    //PART C: According to Swdgewick: "The statistic should be within 2(r)^1/2 of r
    //This is valid if N is greater than about 10r"
    return Math.abs(chiSquare - SUBRANGES) <= 2 * Math.sqrt(SUBRANGES);
  }



}
