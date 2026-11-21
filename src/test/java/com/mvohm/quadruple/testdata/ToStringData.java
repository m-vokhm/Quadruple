package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.ExactValue;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Data for {@code ToStringTest}: finite values (including zeros) to convert to strings, and special values.
 * <p>
 * {@code toString()} gives 40 significant digits, so the interesting cases are the values close to the midpoint
 * between two adjacent 40-digit decimals. They are constructed:
 * <ul>
 *   <li><b>exact decimal midpoints</b>: {@code m * 2^-k}, where {@code m * 5^k} has exactly 41 digits,
 *       so that the 41st digit is 5 (e.g. {@code 2^-58 = 3.4694469519536141888238489627838134765625e-18});</li>
 *   <li><b>near-midpoints</b>: integers {@code x = m * 2^k}, with {@code x == 2^(s-1) * (5^s ± c) (mod 10^s)}
 *       for an odd c, so that the {@code s} digits after the 40th are {@code 5000...} ± {@code c * 2^(s-1)},
 *       i.e. the distance from the midpoint is {@code c / 2 * 5^-s} of the unit of the 40th digit
 *       (from ~0.5 to ~10^-30); {@code m} is found modulo {@code 5^s} by the modular inverse of {@code 2^k}.</li>
 * </ul>
 * Besides:
 * <ul>
 *   <li>hand-made edge cases: extreme values, simple values with short decimal expansions;</li>
 *   <li>the values nearest to powers of 10 and their neighbors (10^-400..10^400), some of them giving 10^n
 *       after rounding to 40 digits (a carry to a new digit: 9.99..9|9 -> 1.00..0e+1);</li>
 *   <li>random values: over the whole range (including subnormals), and with moderate exponents.</li>
 * </ul>
 * All of them with both signs.
 */
public class ToStringData {

  private static final int RANDOM_COUNT        = 5000;
  private static final int EXACT_TIE_COUNT     = 1000;
  private static final int NEAR_MIDPOINT_COUNT = 1000;
  private static final int MAX_POWER_OF_TEN    = 400;

  private static final BigInteger FIVE = BigInteger.valueOf(5);
  private static final double LOG2_10 = Math.log(10) / Math.log(2);

  /** Finite values (see the class description) */
  public static Stream<Named<Quadruple>> values() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final List<Quadruple> list = new ArrayList<>(edgeCases());
    list.addAll(powersOfTen());
    list.addAll(exactTies(random));
    list.addAll(nearMidpoints(random));
    new RandomGenerator().quadruples(RANDOM_COUNT).forEach(list::add);
    new RandomGenerator(RandomGenerator.DEFAULT_RANDOM_SEED + 1).quadruples(RANDOM_COUNT / 2, -400, 400)
      .forEach(list::add);
    return list.stream().flatMap(q -> Stream.of(q, new Quadruple(!q.isNegative(), q.exponent(), q.mantHi(), q.mantLo())))
               .map(q -> Named.of(QuadAssert.fieldsOf(q), q));
  }

  private static List<Quadruple> edgeCases() {
    return List.of(
        new Quadruple(false, 0, 0, 0),                                      // 0
        new Quadruple(false, EXPONENT_OF_ONE, 0, 0),                        // 1
        new Quadruple(false, EXPONENT_OF_ONE, 0x8000_0000_0000_0000L, 0),   // 1.5
        new Quadruple(false, EXPONENT_OF_ONE, 0, 1),                        // 1 + ulp
        new Quadruple(false, EXPONENT_OF_ONE - 1, -1L, -1L),                // 1 - ulp/2
        new Quadruple(false, EXPONENT_OF_ONE + 1, 0, 0),                    // 2
        new Quadruple(false, EXPONENT_OF_ONE + 3, 0x4000_0000_0000_0000L, 0), // 10
        new Quadruple(false, EXPONENT_OF_ONE - 1, 0, 0),                    // 0.5
        new Quadruple(false, EXPONENT_OF_ONE - 4, 0x9999_9999_9999_9999L, 0x9999_9999_9999_999AL), // ~0.1
        new Quadruple(false, (int)EXPONENT_OF_MAX_VALUE, -1L, -1L),         // MAX_VALUE
        new Quadruple(false, (int)EXPONENT_OF_MAX_VALUE, 0, 0),
        new Quadruple(false, 1, 0, 0),                                      // MIN_NORMAL
        new Quadruple(false, 1, 0, 1),
        new Quadruple(false, 0, -1L, -1L),                                  // max subnormal
        new Quadruple(false, 0, 0x8000_0000_0000_0000L, 0),
        new Quadruple(false, 0, 0, 1),                                      // MIN_VALUE
        new Quadruple(false, 0, 0, 2),
        new Quadruple(false, 0, 0, 10),
        new Quadruple(false, 0, 0, -1L));
  }

  /** The values nearest to 10^n and their neighbors */
  private static List<Quadruple> powersOfTen() {
    final List<Quadruple> list = new ArrayList<>();
    for (int n = -MAX_POWER_OF_TEN; n <= MAX_POWER_OF_TEN; n++) {
      final Quadruple q = ExactValue.ofDecimal(false, ONE, n).round().rounded();
      list.add(q);
      list.add(nextDown(q));
      list.add(nextUp(q));
    }
    return list;
  }

  /** {@code m * 2^-k}, where {@code m * 5^k} has exactly 41 digits ({@code m} is odd) */
  private static List<Quadruple> exactTies(Random random) {
    final List<Quadruple> list = new ArrayList<>();
    final BigInteger min = TEN.pow(40), max = TEN.pow(41).subtract(ONE);
    final List<Integer> ks = new ArrayList<>();                   // k such that the range of m is not empty
    for (int k = 1; k <= 58; k++) {
      final BigInteger pow5 = FIVE.pow(k);
      if (max.divide(pow5).compareTo(min.add(pow5).subtract(ONE).divide(pow5)) >= 0)
        ks.add(k);
    }
    while (list.size() < EXACT_TIE_COUNT) {
      final int k = ks.get(random.nextInt(ks.size()));
      final BigInteger pow5 = FIVE.pow(k);
      final BigInteger mMin = min.add(pow5).subtract(ONE).divide(pow5), mMax = max.divide(pow5);
      final BigInteger m = randomBetween(random, mMin, mMax).setBit(0);
      if (m.compareTo(mMax) > 0 || m.bitLength() > 129) continue;
      list.add(exactly(m, -k));
    }
    return list;
  }

  /**
   * Integers {@code x = m * 2^k} of {@code 40 + s} digits, such that the last {@code s} digits are
   * {@code 2^(s-1) * (5^s ± c)}, i.e. the distance from the midpoint is {@code c / 2 * 5^-s} units of the 40th digit
   */
  private static List<Quadruple> nearMidpoints(Random random) {
    final List<Quadruple> list = new ArrayList<>();
    while (list.size() < NEAR_MIDPOINT_COUNT) {
      final int s = 1 + random.nextInt(45);
      final BigInteger pow5 = FIVE.pow(s);
      final BigInteger c = BigInteger.valueOf(2 * random.nextInt(1000) + 1);
      final BigInteger tail = ONE.shiftLeft(s - 1).multiply(random.nextBoolean()? pow5.add(c) : pow5.subtract(c));
      final int k = Math.max(s, (int)Math.ceil((s + 40) * LOG2_10 - 129) + 1);  // m < 2^129
      final BigInteger r = tail.multiply(ONE.shiftLeft(k).modInverse(pow5)).mod(pow5);   // m == r (mod 5^s)
      final BigInteger lo = TEN.pow(s + 39).shiftRight(k).add(ONE), hi = TEN.pow(s + 40).shiftRight(k);
      BigInteger m = randomBetween(random, lo, hi);
      m = m.subtract(m.subtract(r).mod(pow5));
      if (m.compareTo(lo) < 0) m = m.add(pow5);
      if (m.compareTo(hi) > 0 || m.bitLength() > 129) continue;
      list.add(exactly(m, k));                     // x == m * 2^k == tail (mod 10^s), since 2^s divides both
    }
    return list;
  }

  /* ************************************************************************
   * Special values: {Quadruple, the expected string}
   */

  public static Stream<Arguments> specialValues() {
    return Stream.of(
        Arguments.of(new Quadruple(false, 0, 0, 0), "0.0"),
        Arguments.of(new Quadruple(true,  0, 0, 0), "-0.0"),
        Arguments.of(new Quadruple(false, EXPONENT_OF_INFINITY, 0, 0), "Infinity"),
        Arguments.of(new Quadruple(true,  EXPONENT_OF_INFINITY, 0, 0), "-Infinity"),
        Arguments.of(Quadruple.nan(), "NaN"),
        Arguments.of(new Quadruple(true,  EXPONENT_OF_INFINITY, 0x8000_0000_0000_0000L, 0), "NaN"),
        Arguments.of(new Quadruple(false, EXPONENT_OF_INFINITY, 0, 1), "NaN"),
        Arguments.of(new Quadruple(true,  EXPONENT_OF_INFINITY, -1L, -1L), "NaN"));
  }

  /** Values for {@code toHexString()}: finite values and special values with various bit patterns */
  public static Stream<Named<Quadruple>> valuesForHexString() {
    final List<Quadruple> list = new ArrayList<>(edgeCases());
    list.add(new Quadruple(false, EXPONENT_OF_INFINITY, 0, 0));
    list.add(new Quadruple(false, EXPONENT_OF_INFINITY, 0x0123_4567_89AB_CDEFL, 0xFEDC_BA98_7654_3210L));
    new RandomGenerator().quadruples(200).forEach(list::add);
    return list.stream().flatMap(q -> Stream.of(q, new Quadruple(!q.isNegative(), q.exponent(), q.mantHi(), q.mantLo())))
               .map(q -> Named.of(QuadAssert.fieldsOf(q), q));
  }

  /* ************************************************************************
   * Helpers
   */

  private static Quadruple exactly(BigInteger mant, long exp2) {
    final ExactValue.Rounding<Quadruple> r = ExactValue.of(false, mant, exp2).round();
    if (!r.exact())
      throw new IllegalStateException("Test data error: the value is not representable: " + mant + " * 2^" + exp2);
    return r.rounded();
  }

  private static BigInteger randomBetween(Random random, BigInteger min, BigInteger max) {
    final BigInteger range = max.subtract(min).add(ONE);
    return min.add(new BigInteger(range.bitLength() + 16, random).mod(range));
  }

  /** The next value toward zero (for positive non-zero values) */
  private static Quadruple nextDown(Quadruple q) {
    final long mantLo = q.mantLo() - 1;
    final long mantHi = mantLo == -1L? q.mantHi() - 1 : q.mantHi();
    final int exponent = mantLo == -1L && mantHi == -1L? q.exponent() - 1 : q.exponent();
    return new Quadruple(false, exponent, mantHi, mantLo);
  }

  /** The next value away from zero (for positive finite values below MAX_VALUE) */
  private static Quadruple nextUp(Quadruple q) {
    final long mantLo = q.mantLo() + 1;
    final long mantHi = mantLo == 0? q.mantHi() + 1 : q.mantHi();
    final int exponent = mantLo == 0 && mantHi == 0? q.exponent() + 1 : q.exponent();
    return new Quadruple(false, exponent, mantHi, mantLo);
  }

}
