package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
import static com.mvohm.quadruple.testdata.DataHelpers.abs;
import static com.mvohm.quadruple.testdata.DataHelpers.copy;
import static com.mvohm.quadruple.testdata.DataHelpers.named;
import static com.mvohm.quadruple.testdata.DataHelpers.pow2;
import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.ExactValue;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Data for {@code SqrtTest}: positive finite non-zero values.
 * <p>
 * The square root of a Quadruple value can't be an exact tie, but it can be arbitrarily close to the midpoint
 * between two adjacent Quadruple values. Such values are constructed: for an odd 130-bit midpoint numerator {@code m}
 * and a small integer {@code e == 7 (mod 8)}, {@code m^2 == -e (mod 2^131)} is solved for {@code m}
 * (a square root modulo 2^131, by Hensel lifting), so that {@code a = (m^2 + e) / 2^131} is an integer of 128 or 129 bits;
 * then {@code sqrt(a * 2^131) = m + e / (2m) + ...}, i.e. the root is the midpoint plus about {@code e / (4m)} ulp.
 * The magnitude of {@code e} varies, so that the distance from the midpoint varies from ~2^-128 to ~2^-3 ulp;
 * many of them are in the zone ~2^-36..2^-29 ulp above the midpoint, where the library's error is the greatest.
 * <p>
 * The data:
 * <ul>
 *   <li>hand-made edge cases: exact roots, roots of values close to 1, 2 and 4, extreme values;</li>
 *   <li>near-midpoint roots (see above), with arguments over the whole range of exponents of both parities;</li>
 *   <li>exact squares {@code c^2} with short significands of {@code c}, normal and subnormal;</li>
 *   <li>random values: with moderate exponents, over the whole range, subnormal.</li>
 * </ul>
 * <p>
 * The library's square root is not always correctly rounded (BUG-15, see KNOWN_ISSUES.md): if the exact root
 * is slightly above the midpoint (up to ~2^-31.5 ulp), the result may be rounded down. So the values whose roots are
 * closer to the midpoint than {@link #NEAR_MIDPOINT_ZONE} = 2^-31 ulp are given separately, by
 * {@link #nearMidpointValues()}; about a half of them fail. {@link #values()} gives all the others.
 * <p>
 * Special values and negative values are tested by {@code SpecialValuesTest}.
 */
public class SqrtData {

  private static final int NEAR_MIDPOINT_COUNT = scaled(2000);
  /** Near-midpoint roots slightly above the midpoint, at ~2^-36..2^-29 ulp, where the library's error is the greatest */
  private static final int CRITICAL_ZONE_COUNT = scaled(3000);
  private static final int EXACT_COUNT         = scaled(500);
  private static final int RANDOM_COUNT        = scaled(2000);

  /** The distance from the midpoint, in ulps, within which the library's root may be rounded wrongly (BUG-15) */
  private static final double NEAR_MIDPOINT_ZONE = Math.scalb(1.0, -31);

  private static final BigInteger EIGHT = BigInteger.valueOf(8);
  /** The binary exponent of the LSB of subnormal values */
  private static final long MIN_LSB_EXPONENT = 1L - EXPONENT_BIAS - 128;

  /** Positive values whose square roots are not closer to the midpoint than {@link #NEAR_MIDPOINT_ZONE} */
  public static Stream<Named<Quadruple>> values() {
    return allValues().filter(v -> !isNearMidpoint(v.getPayload()));
  }

  /** Positive values whose square roots are closer to the midpoint than {@link #NEAR_MIDPOINT_ZONE} (BUG-15) */
  public static Stream<Named<Quadruple>> nearMidpointValues() {
    return allValues().filter(v -> isNearMidpoint(v.getPayload()));
  }

  /** @return true if the exact square root of {@code q} is closer to the midpoint than {@link #NEAR_MIDPOINT_ZONE} */
  public static boolean isNearMidpoint(Quadruple q) {
    final ExactValue.Rounding<Quadruple> r = ExactValue.of(q).sqrt().round();
    return !r.exact() && Math.abs(r.offsetFromMidpoint()) < NEAR_MIDPOINT_ZONE;
  }

  /** All the data (see the class description) */
  private static Stream<Named<Quadruple>> allValues() {
    final List<Named<Quadruple>> values = new ArrayList<>(edgeCases());
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    for (int i = 0; i < NEAR_MIDPOINT_COUNT; i++) values.add(nearMidpoint(random, 1 + random.nextInt(126), true));
    for (int i = 0; i < CRITICAL_ZONE_COUNT; i++) values.add(nearMidpoint(random, 92 + random.nextInt(8), false));
    for (int i = 0; i < EXACT_COUNT; i++)         values.add(exactSquare(random));
    new RandomGenerator().quadruples(RANDOM_COUNT).map(DataHelpers::abs).forEach(q -> values.add(named(q)));
    for (int i = 0; i < RANDOM_COUNT / 2; i++)
      values.add(named(new Quadruple(false, EXPONENT_BIAS + random.nextInt(2001) - 1000, random.nextLong(),
                                     random.nextInt(4) == 0? 0 : random.nextLong())));
    for (int i = 0; i < RANDOM_COUNT / 4; i++) {
      final long mantHi = random.nextLong() >>> random.nextInt(64);
      final long mantLo = mantHi == 0? random.nextLong() >>> random.nextInt(64) | 1 : random.nextLong();
      values.add(named(new Quadruple(false, 0, mantHi, mantLo)));               // subnormal
    }
    return values.stream().map(DataHelpers::copy);
  }

  /* ************************************************************************
   * Hand-made edge cases
   */

  private static List<Named<Quadruple>> edgeCases() {
    final ExactValue one = pow2(0), ulp = pow2(-128);
    return List.of(
        v("1",                   one),
        v("1 + ulp",             one.add(ulp)),
        v("1 + 2 ulp",           one.add(pow2(-127))),
        v("1 - ulp/2",           one.subtract(pow2(-129))),
        v("1 - ulp",             one.subtract(pow2(-128))),
        v("2",                   pow2(1)),
        v("2 - ulp",             pow2(1).subtract(pow2(-127))),
        v("2 + 2 ulp",           pow2(1).add(pow2(-126))),
        v("3",                   ExactValue.of(3)),
        v("4",                   pow2(2)),
        v("4 - ulp",             pow2(2).subtract(pow2(-126))),
        v("4 + 4 ulp",           pow2(2).add(pow2(-125))),
        v("0.5",                 pow2(-1)),
        v("0.25",                pow2(-2)),
        v("(2^64 + 1)^2 / 2^128", ExactValue.of(false, ONE.shiftLeft(64).add(ONE).pow(2), -128)),  // exact, 1 + 2^-63 + ulp
        v("MAX_VALUE",           ExactValue.of(maxValue())),
        v("MAX_VALUE / 2",       ExactValue.of(maxValue()).multiply(pow2(-1))),
        v("MIN_NORMAL",          ExactValue.of(minNormal())),
        v("2 * MIN_NORMAL",      ExactValue.of(minNormal()).multiply(pow2(1))),
        v("MIN_VALUE",           ExactValue.of(minValue())),
        v("2 * MIN_VALUE",       ExactValue.of(minValue()).multiply(pow2(1))),
        v("4 * MIN_VALUE",       ExactValue.of(minValue()).multiply(pow2(2))),
        v("max subnormal",       ExactValue.of(new Quadruple(false, 0, -1, -1))),
        v("subnormal 0x8000...", ExactValue.of(new Quadruple(false, 0, 0x8000_0000_0000_0000L, 0))));
  }

  /* ************************************************************************
   * Constructed cases
   */

  /**
   * A value whose square root is close to a midpoint: {@code a = (m^2 + e) / 2^131}, where {@code m} is an odd 130-bit
   * square root of {@code -e (mod 2^131)}, and {@code e = 8k - 1} for a random k of a random length (both signs).
   * The value is scaled by {@code 2^(2u)}, so that the root is {@code ~(m + e / 2m) * 2^u}.
   * The distance from the midpoint is {@code ~2^(kLength - 128.5)} ulp.
   * @param bothSigns if false, {@code e} is positive (the root is above the midpoint)
   */
  private static Named<Quadruple> nearMidpoint(Random random, int kLength, boolean bothSigns) {
    while (true) {
      BigInteger k = new BigInteger(kLength, random);
      if (bothSigns && random.nextBoolean())
        k = k.negate();
      final BigInteger e = k.multiply(EIGHT).subtract(ONE);                 // == 7 (mod 8)
      final BigInteger modulus = ONE.shiftLeft(131);
      final BigInteger c = e.negate().mod(modulus);                         // == 1 (mod 8), a quadratic residue
      BigInteger m = sqrtModPowerOf2(c, 131).mod(ONE.shiftLeft(130));
      if (!m.testBit(129))                                                  // the other root, -m (mod 2^130)
        m = ONE.shiftLeft(130).subtract(m);
      final BigInteger square = m.multiply(m).add(e);
      if (square.signum() <= 0 || square.mod(modulus).signum() != 0)
        throw new IllegalStateException("Test data error: wrong square root modulo 2^131");
      final BigInteger a = square.shiftRight(131);
      if (a.bitLength() > 129)
        continue;
      final long u = random.nextInt(4001) - 2000 - 129;                      // the exponent of the unit of m
      return v("near-midpoint root, e = " + e, ExactValue.of(false, a, 2 * u + 131));
    }
  }

  /** @return x such that {@code x^2 == c (mod 2^t)}; {@code c} must be {@code 1 (mod 8)} */
  private static BigInteger sqrtModPowerOf2(BigInteger c, int t) {
    BigInteger x = ONE;                                       // x^2 == c (mod 8)
    for (int k = 3; k < t; k++) {                             // x^2 == c (mod 2^k) -> (mod 2^(k+1))
      final BigInteger mod = ONE.shiftLeft(k + 1);
      if (!x.multiply(x).subtract(c).mod(mod).equals(BigInteger.ZERO))
        x = x.add(ONE.shiftLeft(k - 1));
    }
    return x;
  }

  /** {@code c^2} with a short significand of {@code c}, normal or subnormal, so that the root is exact */
  private static Named<Quadruple> exactSquare(Random random) {
    final int length = 1 + random.nextInt(64);
    final BigInteger c = new BigInteger(length, random).setBit(length - 1);
    final boolean subnormal = random.nextInt(4) == 0;
    final long lsbOfSquare = subnormal? MIN_LSB_EXPONENT + 2L * random.nextInt(64)
                                      : 2L * (random.nextInt(2001) - 1000);
    return v("exact square", ExactValue.of(false, c.multiply(c), lsbOfSquare));
  }

  /* ************************************************************************
   * Helpers
   */

  private static Named<Quadruple> v(String name, ExactValue value) {
    final ExactValue.Rounding<Quadruple> r = value.round();
    if (!r.exact())
      throw new IllegalStateException("Test data error: the value is not representable as Quadruple: " + value);
    return Named.of(name + ": " + QuadAssert.fieldsOf(r.rounded()), r.rounded());
  }

}
