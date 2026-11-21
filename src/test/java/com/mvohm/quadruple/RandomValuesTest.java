package com.mvohm.quadruple;

import static com.mvohm.quadruple.Quadruple.*;
import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.mvohm.quadruple.testdata.generators.RandomGenerator;

/**
 * Tests for {@code nextRandom()} and {@code nextRandom(Random)}: the values must be in {@code [0, 1)},
 * evenly distributed, with all bits of the mantissa random (including the lowest ones, which become free
 * after the normalization of small values), and reproducible with the same {@link Random}.
 * <p>
 * The statistical checks use a fixed seed, so they are deterministic; their bounds are wide
 * (beyond 5 sigma, or the chi-square with p ~ 1e-6), so that they don't depend on the luck of the seed,
 * but still detect any systematic deviation.
 */
public class RandomValuesTest {

  private static final int COUNT = 100_000;

  @Test
  void valuesShouldBeInRangeFromZeroToOne() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    for (int i = 0; i < COUNT; i++)
      checkRange(Quadruple.nextRandom(random));
    for (int i = 0; i < 1000; i++)
      checkRange(Quadruple.nextRandom());
  }

  @Test
  void sameRandomShouldGiveSameValues() {
    final Random r1 = new Random(42), r2 = new Random(42);
    for (int i = 0; i < 1000; i++)
      assertSameFields(Quadruple.nextRandom(r1), Quadruple.nextRandom(r2), "value #" + i);
  }

  @Test
  void eachCallShouldGiveNewInstanceWithNewValue() {
    final List<Quadruple> values = new ArrayList<>();
    for (int i = 0; i < 1000; i++)
      values.add(Quadruple.nextRandom());
    assertThat(values).as("1000 values of nextRandom()").doesNotHaveDuplicates();     // by equals()
    final Random random = new Random(1);
    assertThat(Quadruple.nextRandom(random)).isNotSameAs(Quadruple.nextRandom(random));
  }

  /** Values in 16 equal subintervals of [0, 1): chi-square, 15 degrees of freedom */
  @Test
  void valuesShouldBeEvenlyDistributed() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final long[] counts = new long[16];
    for (int i = 0; i < COUNT; i++)
      counts[(int)(Quadruple.nextRandom(random).doubleValue() * 16)]++;
    final double[] expected = new double[16];
    Arrays.fill(expected, COUNT / 16.0);
    assertThat(chiSquare(counts, expected)).as("chi-square for 16 subintervals: %s", Arrays.toString(counts))
      .isLessThan(50);                                                 // p ~ 1e-5 for 15 degrees of freedom
  }

  /** The exponent is {@code EXPONENT_OF_ONE - k} with the probability {@code 2^-k}: chi-square for k = 1..12 */
  @Test
  void exponentsShouldHaveGeometricDistribution() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final long[] counts = new long[13];                                // the last one: k > 12
    for (int i = 0; i < COUNT; i++) {
      final int k = EXPONENT_OF_ONE - Quadruple.nextRandom(random).exponent();
      counts[Math.min(k, 13) - 1]++;
    }
    final double[] expected = new double[13];
    for (int k = 1; k <= 12; k++) expected[k - 1] = COUNT * Math.scalb(1.0, -k);
    expected[12] = COUNT * Math.scalb(1.0, -12);
    assertThat(chiSquare(counts, expected)).as("chi-square for exponents: %s", Arrays.toString(counts))
      .isLessThan(50);                                                 // p ~ 1e-6 for 12 degrees of freedom
  }

  /**
   * Each of 128 bits of the mantissa must be 1 in a half of the values (within 5 sigma), including the lowest bits,
   * which become free after normalization and must be filled with random bits as well
   */
  @Test
  void allBitsOfMantissaShouldBeRandom() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final long[] ones = new long[128];
    for (int i = 0; i < COUNT; i++) {
      final Quadruple q = Quadruple.nextRandom(random);
      for (int b = 0; b < 64; b++) {
        ones[b]      += q.mantLo() >>> b & 1;
        ones[64 + b] += q.mantHi() >>> b & 1;
      }
    }
    final double sigma = Math.sqrt(COUNT * 0.25);
    for (int b = 0; b < 128; b++)
      assertThat((double)ones[b]).as("the number of ones in bit %s of the mantissa (of %s values)", b, COUNT)
        .isBetween(COUNT / 2 - 5 * sigma, COUNT / 2 + 5 * sigma);
  }

  /* ************************************************************************
   * Helpers
   */

  private static void checkRange(Quadruple q) {
    assertThat(q.isNaN() || q.isInfinite()).as("%s must be finite", fieldsOf(q)).isFalse();
    assertThat(q.isNegative()).as("%s must not be negative", fieldsOf(q)).isFalse();
    assertThat(q.compareTo(new Quadruple(false, EXPONENT_OF_ONE, 0, 0))).as("%s must be less than 1", fieldsOf(q))
      .isNegative();
  }

  private static double chiSquare(long[] counts, double[] expected) {
    double sum = 0;
    for (int i = 0; i < counts.length; i++)
      sum += (counts[i] - expected[i]) * (counts[i] - expected[i]) / expected[i];
    return sum;
  }

}
