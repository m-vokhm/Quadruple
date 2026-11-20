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
 * Data for {@code MultiplyTest}: pairs of finite non-zero factors, used in all forms of the call
 * accepting the second factor ({@link CallForm#accepts(Quadruple)}).
 * <p>
 * The product of two 129-bit significands has 257 or 258 bits, of which ~128 lower ones are to be rounded off.
 * Random factors give a tail of the product close to a half of the ulp with a negligible probability,
 * so the ties and near-ties are constructed: for an odd {@code x} and the required tail {@code R} of {@code t} bits,
 * {@code y = R * x^-1 (mod 2^t)} (plus random higher bits) gives the product {@code x * y} ending with exactly {@code R}.
 * The tails are {@code 100..00} (a tie), {@code 100..0100..00} and {@code 011..1100..00} (a half of the ulp
 * plus or minus a small fraction of it).
 * <p>
 * The data:
 * <ul>
 *   <li>hand-made edge cases: multiplication by 1 and powers of 2, carries ({@code (2 - ulp)^2}), overflow
 *       (including the exact tie {@code MAX_VALUE + ulp/2}), subnormal results, underflow to zero and to {@code MIN_VALUE};</li>
 *   <li>ties and near-ties with normal results, and with subnormal results (the position of rounding is fixed then,
 *       and the length of the tail varies);</li>
 *   <li>random pairs: with moderate exponents, with products near the overflow and near the underflow thresholds,
 *       over the whole range (mostly overflow or underflow);</li>
 *   <li>second factors representable as double or long.</li>
 * </ul>
 * Special values (NaN, infinities, zeros as factors) are tested by {@code SpecialValuesTest}.
 */
public class MultiplyData {

  private static final int TIE_COUNT             = 1500;
  private static final int SUBNORMAL_TIE_COUNT   = 1000;
  private static final int MODERATE_PAIR_COUNT   = 1500;
  private static final int THRESHOLD_PAIR_COUNT  = 500;    // near overflow and near underflow, each
  private static final int WIDE_PAIR_COUNT       = 500;
  private static final int NUMBER_COUNT          = 1000;   // doubles and longs

  /** The binary exponent of the LSB of subnormal values */
  private static final long MIN_LSB_EXPONENT = 1L - EXPONENT_BIAS - 128;
  /** The unbiased exponents of MIN_NORMAL and MAX_VALUE */
  private static final long MIN_EXPONENT = 1L - EXPONENT_BIAS, MAX_EXPONENT = EXPONENT_OF_MAX_VALUE - EXPONENT_BIAS;

  /**
   * {@code (operation, form, a, b)}: all the pairs (see the class description), in all forms of the call,
   * except the cases of BUG-14 (see KNOWN_ISSUES.md)
   */
  public static Stream<Arguments> operations() {
    return allCases().filter(c -> !isSubnormalDoubleRoundingCase(c.a(), c.b())).map(Case::toArguments);
  }

  /**
   * The cases of BUG-14: subnormal products, for which the double rounding performed by the library
   * (see {@link #isSubnormalDoubleRoundingCase(Quadruple, Quadruple)}) gives a result different from the correct one.
   */
  public static Stream<Arguments> subnormalDoubleRoundingCases() {
    return allCases().filter(c -> isSubnormalDoubleRoundingCase(c.a(), c.b())).map(Case::toArguments);
  }

  private record Pair(Named<Quadruple> a, Named<Quadruple> b) {}

  private record Case(CallForm form, Quadruple a, Quadruple b, Pair pair) {
    Arguments toArguments() { return Arguments.of(BinaryOperation.MULTIPLY, form, copy(pair.a()), copy(pair.b())); }
  }

  private static Stream<Case> allCases() {
    final List<Case> result = new ArrayList<>();
    for (final Pair p : allPairs())
      for (final CallForm form : CallForm.values())
        if (form.accepts(p.b().getPayload()))
          result.add(new Case(form, p.a().getPayload(), p.b().getPayload(), p));
    return result.stream();
  }

  /* ************************************************************************
   * The criterion of BUG-14
   */

  /**
   * Emulates the rounding of a subnormal product as the library does it, and compares the result
   * with the correctly rounded one:
   * <ol>
   *   <li>the product of the normalized 129-bit significands is rounded half-up to 128 fractional bits
   *       (as for a normal result), and to 129 bits if it is &gt;= 2 (without the second rounding,
   *       if the first one rounded up);</li>
   *   <li>if the product is subnormal and step 1 rounded it up, the LSB of the 129-bit value is cleared;</li>
   *   <li>the value is rounded half-up to the LSB of subnormal values.</li>
   * </ol>
   * @return true if the product is subnormal (by the result of step 1), and the result of step 3
   * differs from the correctly rounded exact product
   */
  private static boolean isSubnormalDoubleRoundingCase(Quadruple a, Quadruple b) {
    final BigInteger sa = significand(a), sb = significand(b);
    final BigInteger product = sa.multiply(sb);
    final long lsbOfProduct = lsbExponent(a) + lsbExponent(b);

    // Step 1: normalize the factors to 129 bits, round the product to 128 fractional bits
    final BigInteger normalized = sa.shiftLeft(129 - sa.bitLength()).multiply(sb.shiftLeft(129 - sb.bitLength()));
    long lsb = lsbOfProduct - (129 - sa.bitLength()) - (129 - sb.bitLength()) + 128;
    final boolean roundedUp = normalized.testBit(127);
    BigInteger r = normalized.shiftRight(128);
    if (roundedUp)
      r = r.add(ONE);
    if (r.bitLength() > 129) {                       // >= 2: shift by 1 bit, rounding unless already rounded up
      final boolean lowestBit = r.testBit(0);
      r = r.shiftRight(1);
      lsb++;
      if (lowestBit && !roundedUp)
        r = r.add(ONE);
    }
    if (lsb + 128 >= MIN_EXPONENT)                   // not subnormal
      return false;

    // Step 2, 3
    if (roundedUp)
      r = r.clearBit(0);
    final BigInteger doublyRounded = roundHalfUp(r, MIN_LSB_EXPONENT - lsb);
    return !doublyRounded.equals(roundHalfUp(product, MIN_LSB_EXPONENT - lsbOfProduct));
  }

  /** @return {@code value} shifted right by {@code shift} bits and rounded half-up ({@code shift > 0}) */
  private static BigInteger roundHalfUp(BigInteger value, long shift) {
    if (shift > value.bitLength() + 1)
      return BigInteger.ZERO;
    final BigInteger shifted = value.shiftRight((int)shift);
    return value.testBit((int)shift - 1)? shifted.add(ONE) : shifted;
  }

  /** @return the significand of a finite value (with the implicit unity for normal values) */
  private static BigInteger significand(Quadruple q) {
    final BigInteger mant = unsigned(q.mantHi()).shiftLeft(64).or(unsigned(q.mantLo()));
    return q.exponent() == 0? mant : mant.setBit(128);
  }

  /** @return the binary exponent of the LSB of the significand */
  private static long lsbExponent(Quadruple q) {
    return q.exponent() == 0? MIN_LSB_EXPONENT : (q.exponent() & 0xFFFF_FFFFL) - EXPONENT_BIAS - 128;
  }

  private static BigInteger unsigned(long value) {
    return BigInteger.valueOf(value).and(ONE.shiftLeft(64).subtract(ONE));
  }

  private static List<Pair> allPairs() {
    final List<Pair> pairs = new ArrayList<>(edgeCases());
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    for (int i = 0; i < TIE_COUNT; i++)            pairs.add(tie(random));
    for (int i = 0; i < SUBNORMAL_TIE_COUNT; i++)  pairs.add(subnormalTie(random));
    for (int i = 0; i < MODERATE_PAIR_COUNT; i++)
      pairs.add(randomPair(random, random.nextInt(2001) - 1000, random.nextInt(2001) - 1000));
    for (int i = 0; i < THRESHOLD_PAIR_COUNT; i++) {
      final long overflowSum = MAX_EXPONENT - 1 + random.nextInt(3);          // the product's exponent ~ max
      final long underflowSum = MIN_EXPONENT - random.nextInt(140);           // the product is subnormal or less
      pairs.add(randomPairWithSum(random, overflowSum));
      pairs.add(randomPairWithSum(random, underflowSum));
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
    final ExactValue twoMinusUlp = pow2(1).subtract(pow2(-127)), onePlusUlp = one.add(ulp), oneMinusUlp = one.subtract(pow2(-129));
    final ExactValue half = pow2(-1);
    final ExactValue twoTo65MinusOne = pow2(65).subtract(one), twoTo65PlusOne = pow2(65).add(one);

    final List<Pair> pairs = new ArrayList<>(List.of(
        pair("1", one, "1", one),
        pair("1", one, "1 + ulp", onePlusUlp),
        pair("1 + ulp", onePlusUlp, "1 + ulp", onePlusUlp),                    // 1 + 2 ulp + ulp^2
        pair("1 - ulp/2", oneMinusUlp, "1 - ulp/2", oneMinusUlp),
        pair("1 + ulp", onePlusUlp, "1 - ulp/2", oneMinusUlp),                 // 1 + ulp/2 - ulp^2/2: just below the tie
        pair("2 - ulp", twoMinusUlp, "2 - ulp", twoMinusUlp),                  // 4 - 4 ulp + ulp^2
        pair("1.5", one.add(half), "1.5", one.add(half)),
        pair("3", ExactValue.of(3), "1/3 (rounded)", toQuadrupleRounded(one.divide(ExactValue.of(3)))),
        pair("MAX_VALUE", max, "1", one),
        pair("MAX_VALUE", max, "1 + ulp", onePlusUlp),                         // overflow
        pair("MAX_VALUE", max, "1 - ulp/2", oneMinusUlp),
        pair("MAX_VALUE", max, "2", pow2(1)),
        pair("MAX_VALUE", max, "0.5", half),
        pair("MAX_VALUE", max, "MAX_VALUE", max),
        pair("(2^65 - 1) * 2^(MAX_EXP - 64)", twoTo65MinusOne.multiply(pow2(MAX_EXPONENT - 64)),
             "(2^65 + 1) * 2^-65", twoTo65PlusOne.multiply(pow2(-65))),       // = MAX_VALUE + ulp/2 exactly, a tie
        pair("(2^65 - 1) * 2^(MAX_EXP - 64)", twoTo65MinusOne.multiply(pow2(MAX_EXPONENT - 64)),
             "(2^65 - 1) * 2^-65", twoTo65MinusOne.multiply(pow2(-65))),      // below MAX_VALUE
        pair("MIN_NORMAL", minNormal, "0.5", half),                            // exact subnormal
        pair("MIN_NORMAL", minNormal, "1 - ulp/2", oneMinusUlp),               // a tie in the subnormal range
        pair("MIN_NORMAL", minNormal, "1 + ulp", onePlusUlp),
        pair("max subnormal", maxSubnormal, "1 + ulp", onePlusUlp),
        pair("max subnormal", maxSubnormal, "2", pow2(1)),                     // a subnormal becomes normal
        pair("max subnormal", maxSubnormal, "0.5", half),                      // a tie
        pair("MIN_VALUE", minValue, "0.5", half),                              // a tie -> MIN_VALUE
        pair("MIN_VALUE", minValue, "0.5 + ulp", half.add(pow2(-129))),        // -> MIN_VALUE
        pair("MIN_VALUE", minValue, "0.5 - ulp/2", half.subtract(pow2(-130))), // -> 0
        pair("MIN_VALUE", minValue, "0.25", pow2(-2)),                         // -> 0
        pair("MIN_VALUE", minValue, "1.5", one.add(half)),                     // a tie -> 2 * MIN_VALUE
        pair("MIN_VALUE", minValue, "MIN_VALUE", minValue),                    // -> 0
        pair("MIN_VALUE", minValue, "2^200", pow2(200)),                       // a subnormal becomes normal
        pair("MIN_VALUE", minValue, "MAX_VALUE", max),
        pair("MIN_NORMAL", minNormal, "MAX_VALUE", max)));

    final List<Pair> withSigns = new ArrayList<>();
    for (final Pair p : pairs) {
      withSigns.add(p);
      withSigns.add(new Pair(p.a(), negated(p.b())));
      withSigns.add(new Pair(negated(p.a()), p.b()));
      withSigns.add(new Pair(negated(p.a()), negated(p.b())));
      withSigns.add(new Pair(p.b(), p.a()));                                   // and the other order
    }
    return withSigns;
  }

  /* ************************************************************************
   * Ties and near-ties
   */

  /**
   * A tie or a near-tie with a normal result: {@code x} is an odd significand of a random length {@code Lx},
   * {@code y} is a 129-bit significand such that the lower {@code t = Lx - 1} bits of {@code x * y} are the required tail;
   * the product has {@code Lx + 128} bits (otherwise another {@code x} is tried), so that exactly {@code t} bits
   * are to be rounded off.
   */
  private static Pair tie(Random random) {
    while (true) {
      final int lengthX = 2 + random.nextInt(128);
      final BigInteger x = new BigInteger(lengthX, random).setBit(lengthX - 1).setBit(0);
      final int t = lengthX - 1;
      final BigInteger y = factorWithTail(random, x, t, tail(random, t), 129);
      if (x.multiply(y).bitLength() != lengthX + 128)
        continue;
      final int exponentX = random.nextInt(2001) - 1000, exponentY = random.nextInt(2001) - 1000;
      return pair(withSign(random, x, exponentX - (lengthX - 1)), withSign(random, y, exponentY - 128));
    }
  }

  /**
   * A tie or a near-tie with a subnormal result (or MIN_NORMAL, after rounding up):
   * the product {@code x * y} is scaled so that its lower {@code t} bits, which are the required tail,
   * are below the LSB of subnormal values; the result has {@code bitLength(x * y) - t <= 129} bits.
   */
  private static Pair subnormalTie(Random random) {
    final int lengthX = 2 + random.nextInt(100);
    final BigInteger x = new BigInteger(lengthX, random).setBit(lengthX - 1).setBit(0);
    final int t = lengthX + random.nextInt(129 - lengthX);                   // lengthX .. 128
    final BigInteger y = factorWithTail(random, x, t, tail(random, t), 129);
    final long lsbOfProduct = MIN_LSB_EXPONENT - t;
    final long lsbOfX = lsbOfProduct / 2 + random.nextInt(1000) - 500;      // split it between the factors
    return pair(withSign(random, x, lsbOfX), withSign(random, y, lsbOfProduct - lsbOfX));
  }

  /** A tail of {@code t} bits: {@code 100..00} (a tie), or a half plus or minus {@code 2^j}, {@code j < t - 1} */
  private static BigInteger tail(Random random, int t) {
    final BigInteger half = ONE.shiftLeft(t - 1);
    if (t < 2)
      return half;
    final BigInteger delta = ONE.shiftLeft(random.nextInt(t - 1));
    return switch (random.nextInt(3)) {
      case 0  -> half;
      case 1  -> half.add(delta);
      default -> half.subtract(delta);
    };
  }

  /**
   * @return a number {@code y} of exactly {@code length} bits, such that {@code x * y == tail (mod 2^t)};
   * {@code x} must be odd, {@code t < length}
   */
  private static BigInteger factorWithTail(Random random, BigInteger x, int t, BigInteger tail, int length) {
    final BigInteger modulus = ONE.shiftLeft(t);
    final BigInteger low = tail.multiply(x.modInverse(modulus)).mod(modulus);
    return new BigInteger(length - t, random).shiftLeft(t).or(low).setBit(length - 1);
  }

  /* ************************************************************************
   * Random pairs
   */

  private static Pair randomPair(Random random, long exponentA, long exponentB) {
    return pair(randomNormal(random, exponentA), randomNormal(random, exponentB));
  }

  /** Random factors whose unbiased exponents sum up to the given value */
  private static Pair randomPairWithSum(Random random, long sum) {
    final long exponentA = sum / 2 + random.nextInt(2001) - 1000;
    return randomPair(random, exponentA, sum - exponentA);
  }

  /** A random normal value with the given unbiased exponent and a random sign */
  private static Quadruple randomNormal(Random random, long exponent) {
    final long mantLo = random.nextInt(4) == 0? 0 : random.nextLong();     // some shorter mantissas
    return new Quadruple(random.nextBoolean(), (int)(EXPONENT_BIAS + exponent), random.nextLong(), mantLo);
  }

  /* ************************************************************************
   * Helpers
   */

  /** @return {@code ±significand * 2^lsbExponent} with a random sign, as a Quadruple (must be representable) */
  private static Quadruple withSign(Random random, BigInteger significand, long lsbExponent) {
    return toQuadruple(ExactValue.of(random.nextBoolean(), significand, lsbExponent));
  }

  private static Pair pair(Quadruple a, Quadruple b) {
    return new Pair(named(a), named(b));
  }

  private static Pair pair(String nameA, ExactValue a, String nameB, ExactValue b) {
    return new Pair(Named.of(nameA, toQuadruple(a)), Named.of(nameB, toQuadruple(b)));
  }

  private static Pair pair(String nameA, ExactValue a, String nameB, Quadruple b) {
    return new Pair(Named.of(nameA, toQuadruple(a)), Named.of(nameB, b));
  }

  /** @return the Quadruple exactly equal to the given value; throws if it's not representable */
  private static Quadruple toQuadruple(ExactValue value) {
    final ExactValue.Rounding<Quadruple> r = value.round();
    if (!r.exact())
      throw new IllegalStateException("Test data error: the value is not representable as Quadruple: " + value);
    return r.rounded();
  }

  private static Quadruple toQuadrupleRounded(ExactValue value) {
    return value.round().rounded();
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
