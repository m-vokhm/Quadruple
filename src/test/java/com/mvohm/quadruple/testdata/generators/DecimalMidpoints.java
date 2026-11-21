package com.mvohm.quadruple.testdata.generators;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TEN;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Generators of positive Quadruple values that are equal or close to the midpoints between two adjacent decimals
 * of a given number of significant digits {@code n}: the hard cases of conversions to decimal forms
 * ({@code toString()}: 40 digits, {@code bigDecimalValue()}: 100 digits).
 * <ul>
 *   <li>{@link #exactMidpoints}: {@code m * 2^-k}, where {@code m * 5^k} has exactly {@code n + 1} digits
 *       ({@code m} is odd), so that the decimal expansion has {@code n + 1} digits, the last one being 5
 *       (e.g. {@code 2^-58 = 3.4694469519536141888238489627838134765625e-18} for n = 40);</li>
 *   <li>{@link #nearMidpoints}: integers {@code x = m * 2^k} of {@code n + s} digits, such that
 *       {@code x == 2^(s-1) * (5^s ± c) (mod 10^s)} for an odd c, i.e. the {@code s} digits after the n-th are
 *       {@code 5000...} ± {@code c * 2^(s-1)}, and the distance from the midpoint is {@code c / 2 * 5^-s}
 *       of the unit of the n-th digit (from ~0.5 to ~10^-30, for s up to 45); {@code m} is found modulo
 *       {@code 5^s} by the modular inverse of {@code 2^k} (modulo {@code 2^s}, both sides are 0, since
 *       {@code k >= s}).</li>
 * </ul>
 */
public class DecimalMidpoints {

  private static final BigInteger FIVE = BigInteger.valueOf(5);
  private static final double LOG2_10 = Math.log(10) / Math.log(2);

  private DecimalMidpoints() {}

  /** @return {@code count} values {@code m * 2^-k}, with {@code m * 5^k} of exactly {@code digits + 1} digits */
  public static List<Quadruple> exactMidpoints(Random random, int digits, int count) {
    final BigInteger min = TEN.pow(digits), max = TEN.pow(digits + 1).subtract(ONE);
    final List<Integer> ks = new ArrayList<>();               // k such that m may be odd and less than 2^129
    for (int k = 1; FIVE.pow(k).compareTo(max) <= 0; k++) {
      final BigInteger pow5 = FIVE.pow(k);
      final BigInteger mMin = min.add(pow5).subtract(ONE).divide(pow5), mMax = max.divide(pow5);
      if (mMax.compareTo(mMin) > 0 && mMin.bitLength() <= 128)
        ks.add(k);
    }
    final List<Quadruple> list = new ArrayList<>();
    while (list.size() < count) {
      final int k = ks.get(random.nextInt(ks.size()));
      final BigInteger pow5 = FIVE.pow(k);
      final BigInteger mMin = min.add(pow5).subtract(ONE).divide(pow5);
      final BigInteger mMax = max.divide(pow5).min(ONE.shiftLeft(129).subtract(ONE));
      final BigInteger m = randomBetween(random, mMin, mMax).setBit(0);
      if (m.compareTo(mMax) > 0) continue;
      list.add(exactly(m, -k));
    }
    return list;
  }

  /**
   * @return {@code count} integers of {@code digits + s} digits (s = 1..45) at the distance {@code c / 2 * 5^-s}
   * of the unit of the {@code digits}-th digit from the midpoint, on both sides of it
   */
  public static List<Quadruple> nearMidpoints(Random random, int digits, int count) {
    final List<Quadruple> list = new ArrayList<>();
    while (list.size() < count) {
      final int s = 1 + random.nextInt(45);
      final BigInteger pow5 = FIVE.pow(s);
      final BigInteger c = BigInteger.valueOf(2 * random.nextInt(1000) + 1);
      final BigInteger tail = ONE.shiftLeft(s - 1).multiply(random.nextBoolean()? pow5.add(c) : pow5.subtract(c));
      final int k = Math.max(s, (int)Math.ceil((s + digits) * LOG2_10 - 129) + 1);   // m < 2^129
      final BigInteger r = tail.multiply(ONE.shiftLeft(k).modInverse(pow5)).mod(pow5);   // m == r (mod 5^s)
      final BigInteger lo = TEN.pow(s + digits - 1).shiftRight(k).add(ONE), hi = TEN.pow(s + digits).shiftRight(k);
      BigInteger m = randomBetween(random, lo, hi);
      m = m.subtract(m.subtract(r).mod(pow5));
      if (m.compareTo(lo) < 0) m = m.add(pow5);
      if (m.compareTo(hi) > 0 || m.bitLength() > 129) continue;
      list.add(exactly(m, k));
    }
    return list;
  }

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

}
