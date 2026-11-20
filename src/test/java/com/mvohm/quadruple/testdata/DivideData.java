package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.BinaryOperation;
import com.mvohm.quadruple.testutil.CallForm;
import com.mvohm.quadruple.testutil.ExactValue;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Data for {@code DivideTest}: pairs of finite non-zero dividends and divisors, used in all forms of the call
 * accepting the divisor ({@link CallForm#accepts(Quadruple)}).
 * <p>
 * The quotient of two 129-bit significands can't be an exact tie for a normal result, but it can be arbitrarily close
 * to the midpoint between two adjacent Quadruple values. Random operands give such quotients with a negligible
 * probability, so they are constructed: for a divisor {@code b = B * 2^β} (B is a 129-bit odd integer),
 * the midpoint numerator {@code m} (odd) and a small integer {@code e}, the dividend is {@code a = (B * m + e) / 2^t},
 * where {@code m = -e * B^-1 (mod 2^t)}, so that {@code a} is an integer of at most 129 bits;
 * then {@code a / b} is the midpoint {@code m * 2^-t} plus {@code e / B} of the unit, i.e. {@code e / (2B)} ulp.
 * The magnitude of {@code e} varies, so that the distance from the midpoint varies from 2^-130 ulp
 * (well within the tolerance) to 2^-1 ulp.
 * <p>
 * The data:
 * <ul>
 *   <li>hand-made edge cases: exact quotients, 1/3, quotients slightly less than 1, overflow, subnormal quotients,
 *       exact ties in the subnormal range ({@code MIN_VALUE / 2}), underflow to 0;</li>
 *   <li>near-midpoint quotients (see above), with normal and subnormal results;</li>
 *   <li>exact quotients: {@code a = b * c} with short significands;</li>
 *   <li>random pairs: with moderate exponents, with quotients near the overflow and the underflow thresholds,
 *       over the whole range;</li>
 *   <li>divisors representable as double or long.</li>
 * </ul>
 * Special values (NaN, infinities, zeros as operands) are tested by {@code SpecialValuesTest}.
 */
public class DivideData {

  private static final int NEAR_MIDPOINT_COUNT           = 1500;
  private static final int SUBNORMAL_NEAR_MIDPOINT_COUNT = 1000;
  private static final int EXACT_COUNT                   = 500;
  private static final int MODERATE_PAIR_COUNT           = 1500;
  private static final int THRESHOLD_PAIR_COUNT          = 500;    // near overflow and near underflow, each
  private static final int WIDE_PAIR_COUNT               = 500;
  private static final int NUMBER_COUNT                  = 1000;   // doubles and longs

  /** The binary exponent of the LSB of subnormal values */
  private static final long MIN_LSB_EXPONENT = 1L - EXPONENT_BIAS - 128;
  /** The unbiased exponents of MIN_NORMAL and MAX_VALUE */
  private static final long MIN_EXPONENT = 1L - EXPONENT_BIAS, MAX_EXPONENT = EXPONENT_OF_MAX_VALUE - EXPONENT_BIAS;

  /** {@code (operation, form, a, b)}: all the pairs (see the class description), in all forms of the call */
  public static Stream<Arguments> operations() {
    final List<Arguments> result = new ArrayList<>();
    for (final Pair p : allPairs())
      for (final CallForm form : CallForm.values())
        if (form.accepts(p.b().getPayload()))
          result.add(Arguments.of(BinaryOperation.DIVIDE, form, copy(p.a()), copy(p.b())));
    return result.stream();
  }

  private record Pair(Named<Quadruple> a, Named<Quadruple> b) {}

  private static List<Pair> allPairs() {
    final List<Pair> pairs = new ArrayList<>(edgeCases());
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    for (int i = 0; i < NEAR_MIDPOINT_COUNT; i++)
      pairs.add(nearMidpoint(random, 130, random.nextInt(2001) - 1000 - 129, 0));
    for (int i = 0; i < SUBNORMAL_NEAR_MIDPOINT_COUNT; i++)   // the unit of m is a half of the subnormal LSB
      pairs.add(nearMidpoint(random, 1 + random.nextInt(129), MIN_LSB_EXPONENT - 1, 200));
    for (int i = 0; i < EXACT_COUNT; i++)          pairs.add(exactQuotient(random));
    for (int i = 0; i < MODERATE_PAIR_COUNT; i++)
      pairs.add(pair(randomNormal(random, random.nextInt(2001) - 1000), randomNormal(random, random.nextInt(2001) - 1000)));
    for (int i = 0; i < THRESHOLD_PAIR_COUNT; i++) {
      pairs.add(randomPairWithDifference(random, MAX_EXPONENT - 1 + random.nextInt(3)));   // near overflow
      pairs.add(randomPairWithDifference(random, MIN_EXPONENT - random.nextInt(140)));     // subnormal or less
    }
    new RandomGenerator().quadruplePairs(WIDE_PAIR_COUNT)
      .filter(p -> !p[0].isZero() && !p[1].isZero())
      .forEach(p -> pairs.add(pair(p[0], p[1])));
    new RandomGenerator().doubles(NUMBER_COUNT)
      .filter(d -> d != 0)
      .forEach(d -> pairs.add(pair(randomNormal(random, random.nextInt(2001) - 1000), new Quadruple(d))));
    new RandomGenerator().longs(NUMBER_COUNT)
      .filter(l -> l != 0)
      .forEach(l -> pairs.add(pair(randomNormal(random, random.nextInt(2001) - 1000), new Quadruple(l))));
    return pairs;
  }

  /* ************************************************************************
   * Hand-made edge cases
   */

  private static List<Pair> edgeCases() {
    final ExactValue one = pow2(0), ulp = pow2(-128);
    final ExactValue max = ExactValue.of(maxValue()), minValue = ExactValue.of(minValue());
    final ExactValue minNormal = ExactValue.of(minNormal());
    final ExactValue maxSubnormal = ExactValue.of(new Quadruple(false, 0, -1, -1));
    final ExactValue onePlusUlp = one.add(ulp), oneMinusUlp = one.subtract(pow2(-129));
    final ExactValue twoMinusUlp = pow2(1).subtract(pow2(-127));
    final ExactValue three = ExactValue.of(3), half = pow2(-1);

    final List<Pair> pairs = new ArrayList<>(List.of(
        pair("1", one, "1", one),
        pair("1", one, "3", three),
        pair("2", pow2(1), "3", three),
        pair("1", one, "1 + ulp", onePlusUlp),                                 // 1 - ulp + ulp^2 -...: the lower binade
        pair("1", one, "1 - ulp/2", oneMinusUlp),
        pair("1", one, "2 - ulp", twoMinusUlp),
        pair("1 + ulp", onePlusUlp, "1 + ulp", onePlusUlp),
        pair("1 + ulp", onePlusUlp, "1 - ulp/2", oneMinusUlp),
        pair("1 - ulp/2", oneMinusUlp, "1 + ulp", onePlusUlp),
        pair("2 - ulp", twoMinusUlp, "1 - ulp/2", oneMinusUlp),
        pair("3", three, "2^129 - 1", pow2(129).subtract(one)),
        pair("1.5", one.add(half), "0.75", pow2(-1).add(pow2(-2))),             // exact 2
        pair("MAX_VALUE", max, "1", one),
        pair("MAX_VALUE", max, "MAX_VALUE", max),
        pair("MAX_VALUE", max, "1 - ulp/2", oneMinusUlp),                      // MAX + ulp/2 + ...: overflow
        pair("MAX_VALUE", max, "0.5", half),                                   // overflow
        pair("MAX_VALUE", max, "MIN_VALUE", minValue),
        pair("MAX_VALUE", max, "1 + ulp", onePlusUlp),
        pair("MIN_NORMAL", minNormal, "2", pow2(1)),                           // exact subnormal
        pair("MIN_NORMAL", minNormal, "1 + ulp", onePlusUlp),                  // subnormal, rounded
        pair("MIN_NORMAL", minNormal, "1 - ulp/2", oneMinusUlp),
        pair("MIN_NORMAL", minNormal, "3", three),
        pair("max subnormal", maxSubnormal, "1 - ulp/2", oneMinusUlp),         // -> MIN_NORMAL?
        pair("max subnormal", maxSubnormal, "0.5", half),                      // a subnormal becomes normal
        pair("max subnormal", maxSubnormal, "2", pow2(1)),                     // a tie in the subnormal range
        pair("MIN_VALUE", minValue, "2", pow2(1)),                             // a tie -> MIN_VALUE
        pair("MIN_VALUE", minValue, "2 - ulp", twoMinusUlp),                   // > a half -> MIN_VALUE
        pair("MIN_VALUE", minValue, "2 + ulp", pow2(1).add(pow2(-127))),       // < a half -> 0
        pair("MIN_VALUE", minValue, "3", three),                               // -> 0
        pair("MIN_VALUE", minValue, "0.75", pow2(-1).add(pow2(-2))),           // 4/3 -> MIN_VALUE
        pair("3 * MIN_VALUE", minValue.multiply(three), "2", pow2(1)),         // a tie -> 2 * MIN_VALUE
        pair("MIN_VALUE", minValue, "MAX_VALUE", max),                         // -> 0
        pair("MIN_VALUE", minValue, "MIN_VALUE", minValue),
        pair("MIN_VALUE", minValue, "2^-200", pow2(-200))));                   // a subnormal becomes normal

    final List<Pair> withSigns = new ArrayList<>();
    for (final Pair p : pairs) {
      withSigns.add(p);
      withSigns.add(new Pair(p.a(), negated(p.b())));
      withSigns.add(new Pair(negated(p.a()), p.b()));
      withSigns.add(new Pair(negated(p.a()), negated(p.b())));
      withSigns.add(new Pair(p.b(), p.a()));                                   // and the reciprocal-like
    }
    return withSigns;
  }

  /* ************************************************************************
   * Constructed cases
   */

  /**
   * A quotient near a midpoint: {@code b = B * 2^β} with a random odd 129-bit B, {@code a = (B * m + e) / 2^t}
   * with {@code t = mLength} and {@code m = -e * B^-1 (mod 2^t)}, so that {@code a / b = (m + e / B) * 2^unit}.
   * {@code m} is odd (as {@code e} is odd), so if it has {@code mLength} bits and {@code unit} is a half of the ulp
   * of the result, {@code m * 2^unit} is a midpoint.
   * @param mLength    the length of {@code m}: 130 for normal results, up to 129 for subnormal ones
   * @param unit       the binary exponent of the unit of {@code m}
   * @param minBeta    the lower bound of the LSB exponent of the divisor (to keep the dividend representable)
   */
  private static Pair nearMidpoint(Random random, int mLength, long unit, int minBeta) {
    while (true) {
      final BigInteger divisor = new BigInteger(129, random).setBit(128).setBit(0);
      final int eLength = 1 + random.nextInt(128);                          // the distance is ~2^(eLength - 130) ulp
      BigInteger e = new BigInteger(eLength, random).setBit(0);
      if (random.nextBoolean())
        e = e.negate();
      final BigInteger modulus = ONE.shiftLeft(mLength);
      final BigInteger m = e.negate().multiply(divisor.modInverse(modulus)).mod(modulus);
      final BigInteger dividend = divisor.multiply(m).add(e).shiftRight(mLength);   // exact: divisible by 2^t
      if (dividend.signum() <= 0 || dividend.bitLength() > 129)
        continue;
      final long beta = minBeta + random.nextInt(1000) - 128;
      final long alpha = unit + beta + mLength;                             // a / b = (m + e/B) * 2^(alpha - beta - t)
      final boolean negative = random.nextBoolean();
      return pair(toQuadruple(ExactValue.of(negative ^ random.nextBoolean(), dividend, alpha)),
                  toQuadruple(ExactValue.of(negative, divisor, beta)));
    }
  }

  /** {@code a = b * c} with short significands of {@code b} and {@code c}, so that the quotient is exact */
  private static Pair exactQuotient(Random random) {
    final int lengthB = 1 + random.nextInt(128);
    final BigInteger b = new BigInteger(lengthB, random).setBit(lengthB - 1);
    final BigInteger c = new BigInteger(129 - lengthB, random).setBit(128 - lengthB);
    final long expB = random.nextInt(2001) - 1000, expC = random.nextInt(2001) - 1000;
    return pair(toQuadruple(ExactValue.of(random.nextBoolean(), b.multiply(c), expB + expC)),
                toQuadruple(ExactValue.of(random.nextBoolean(), b, expB)));
  }

  /* ************************************************************************
   * Random pairs
   */

  /** Random operands whose unbiased exponents differ by the given value */
  private static Pair randomPairWithDifference(Random random, long difference) {
    final long exponentB = -difference / 2 + random.nextInt(2001) - 1000;
    return pair(randomNormal(random, exponentB + difference), randomNormal(random, exponentB));
  }

  /** A random normal value with the given unbiased exponent and a random sign */
  private static Quadruple randomNormal(Random random, long exponent) {
    final long mantLo = random.nextInt(4) == 0? 0 : random.nextLong();     // some shorter mantissas
    return new Quadruple(random.nextBoolean(), (int)(EXPONENT_BIAS + exponent), random.nextLong(), mantLo);
  }

  /* ************************************************************************
   * Helpers
   */

  private static Pair pair(Quadruple a, Quadruple b) {
    return new Pair(named(a), named(b));
  }

  private static Pair pair(String nameA, ExactValue a, String nameB, ExactValue b) {
    return new Pair(Named.of(nameA, toQuadruple(a)), Named.of(nameB, toQuadruple(b)));
  }

  /** @return the Quadruple exactly equal to the given value; throws if it's not representable */
  private static Quadruple toQuadruple(ExactValue value) {
    final ExactValue.Rounding<Quadruple> r = value.round();
    if (!r.exact())
      throw new IllegalStateException("Test data error: the value is not representable as Quadruple: " + value);
    return r.rounded();
  }

  private static ExactValue pow2(long exponent) {
    return ExactValue.of(false, ONE, exponent);
  }

  private static Named<Quadruple> named(Quadruple q) {
    return Named.of(QuadAssert.fieldsOf(q), q);
  }

  private static Named<Quadruple> negated(Named<Quadruple> v) {
    final Quadruple q = v.getPayload();
    return Named.of("-(" + v.getName() + ")", new Quadruple(!q.isNegative(), q.exponent(), q.mantHi(), q.mantLo()));
  }

  private static Named<Quadruple> copy(Named<Quadruple> v) {
    return Named.of(v.getName(), new Quadruple(v.getPayload()));
  }

}
