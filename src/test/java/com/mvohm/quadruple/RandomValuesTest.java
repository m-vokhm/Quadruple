package com.mvohm.quadruple;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
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

  private static final int COUNT = scaled(100_000);

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

  /**
   * The cases that random data practically never hit (probability ~2^-64 and 2^-128): the 64 high bits of the
   * random fraction are zero, so that more than 64 low bits become free after normalization, and both words are zero.
   * A {@link Random} with a given sequence of {@code nextLong()} values is used: two words of the fraction,
   * then the words to fill the free bits (BUG-24).
   */
  @Test
  void shouldFillFreeBitsWhenHighWordOfFractionIsZero() {
    // 2^-96: 96 free bits, all of mantLo and 32 low bits of mantHi
    assertSameFields(Quadruple.nextRandom(scripted(0, 0x0000_0001_0000_0000L, -1L, -1L)),
                     new Quadruple(false, EXPONENT_OF_ONE - 96, 0x0000_0000_FFFF_FFFFL, -1L), "2^-96 + random bits");
    assertSameFields(Quadruple.nextRandom(scripted(0, 0x0000_0001_0000_0000L, 0x1234_5678_9ABC_DEF0L, 0x0FED_CBA9_8765_4321L)),
                     new Quadruple(false, EXPONENT_OF_ONE - 96, 0x0FED_CBA9L, 0x1234_5678_9ABC_DEF0L), "2^-96 + random bits");
    // 2^-128: all 128 bits are free
    assertSameFields(Quadruple.nextRandom(scripted(0, 1, 0x1111_2222_3333_4444L, 0x5555_6666_7777_8888L)),
                     new Quadruple(false, EXPONENT_OF_ONE - 128, 0x5555_6666_7777_8888L, 0x1111_2222_3333_4444L),
                     "2^-128 + random bits");
    // 2^-65: 65 free bits, all of mantLo and the lowest bit of mantHi
    assertSameFields(Quadruple.nextRandom(scripted(0, HIGH_BIT_OF_LONG, -1L, -1L)),
                     new Quadruple(false, EXPONENT_OF_ONE - 65, 1, -1L), "2^-65 + random bits");
    // 2^-64: 64 free bits, all of mantLo (as before)
    assertSameFields(Quadruple.nextRandom(scripted(1, 0, -1L)),
                     new Quadruple(false, EXPONENT_OF_ONE - 64, 0, -1L), "2^-64 + random bits");
    // both words are zero: 0
    assertSameFields(Quadruple.nextRandom(scripted(0, 0, -1L, -1L)), new Quadruple(false, 0, 0, 0), "zero");
  }

  /* ************************************************************************
   * Helpers
   */

  private static final long HIGH_BIT_OF_LONG = 0x8000_0000_0000_0000L;

  /** A {@link Random} whose {@code nextLong()} returns the given values in turn */
  private static Random scripted(long... values) {
    return new Random() {
      private static final long serialVersionUID = 1L;
      private int index;
      @Override
      public long nextLong() {
        return values[index++];
      }
    };
  }

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
