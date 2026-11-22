package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
import static com.mvohm.quadruple.testdata.DataHelpers.copy;
import static com.mvohm.quadruple.testdata.DataHelpers.named;
import static com.mvohm.quadruple.testdata.DataHelpers.negated;
import static com.mvohm.quadruple.testdata.DataHelpers.pow2;
import static com.mvohm.quadruple.testdata.DataHelpers.randomNormal;
import static com.mvohm.quadruple.testdata.DataHelpers.toQuadruple;
import static com.mvohm.quadruple.testdata.DataHelpers.unbiasedExponent;
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

/**
 * Data for {@code AddSubTest}: pairs of finite non-zero operands, each of them used for both addition and subtraction
 * in all forms of the call accepting the second operand ({@link CallForm#accepts(Quadruple)}).
 * <p>
 * Random values over the whole range almost always have exponents differing so much that the result is the greater
 * operand, so most of the data are built to hit the cases where an error is likely:
 * <ul>
 *   <li>random pairs with close exponents (differing by up to 140), both signs;</li>
 *   <li>exact ties: the second operand has the bits below the LSB of the first one equal to exactly a half of its ulp,
 *       and near-ties (the half plus or minus a small fraction of the ulp); the first operand is sometimes a power of 2,
 *       so that the subtraction drops the result to the lower binade, where the tie disappears or moves;</li>
 *   <li>subtractions whose result, before the rounding, is exactly a power of 2 (the lower bound of the binade
 *       of the greater operand) minus a fraction of its ulp: the exact result is in the lower binade (BUG-27);</li>
 *   <li>hand-made edge cases: exponent gaps around 128 bits, carries to the exponent, borrows from it, overflow,
 *       the boundary between subnormal and normal values, operands far below the ulp of the other one;</li>
 *   <li>massive cancellation: close values of the same sign (for subtraction) and of opposite signs (for addition),
 *       including subnormal results;</li>
 *   <li>second operands representable as double or long, with first operands of a comparable magnitude,
 *       to check the forms with double and long arguments.</li>
 * </ul>
 * Special values (NaN, infinities, zeros as operands) are tested by {@code SpecialValuesTest}.
 */
public class AddSubData {

  private static final int CLOSE_PAIR_COUNT    = scaled(2000);
  private static final int WIDE_PAIR_COUNT     = scaled(500);
  private static final int TIE_COUNT           = scaled(1000);
  private static final int CANCELLATION_COUNT  = scaled(500);
  private static final int BOUNDARY_COUNT      = scaled(1000);
  private static final int NUMBER_COUNT        = scaled(1000);   // doubles and longs

  private static final long HIGH_BIT = 0x8000_0000_0000_0000L;
  /** The binary exponent of the LSB of subnormal values */
  private static final long MIN_LSB_EXPONENT = 1L - EXPONENT_BIAS - 128;

  /**
   * {@code (operation, form, a, b)}: all the pairs (see the class description), for addition and subtraction
   */
  public static Stream<Arguments> operations() {
    return allCases().map(Case::toArguments);
  }

  private record Pair(Named<Quadruple> a, Named<Quadruple> b) {}

  private record Case(BinaryOperation op, CallForm form, Named<Quadruple> a, Named<Quadruple> b) {
    Arguments toArguments() { return Arguments.of(op, form, copy(a), copy(b)); }
  }

  private static Stream<Case> allCases() {
    final List<Case> result = new ArrayList<>();
    for (final Pair p : allPairs())
      for (final BinaryOperation op : List.of(BinaryOperation.ADD, BinaryOperation.SUBTRACT))
        for (final CallForm form : CallForm.values())
          if (form.accepts(p.b().getPayload()))
            result.add(new Case(op, form, p.a(), p.b()));
    return result.stream();
  }

  private static List<Pair> allPairs() {
    final List<Pair> pairs = new ArrayList<>(edgeCases());
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    for (int i = 0; i < CLOSE_PAIR_COUNT; i++)   pairs.add(closePair(random));
    for (int i = 0; i < TIE_COUNT; i++)          pairs.add(tie(random));
    for (int i = 0; i < CANCELLATION_COUNT; i++) pairs.add(cancellation(random));
    for (int i = 0; i < BOUNDARY_COUNT; i++)     pairs.add(binadeBoundary(random));
    new RandomGenerator().quadruplePairs(WIDE_PAIR_COUNT)
      .filter(p -> !p[0].isZero() && !p[1].isZero())
      .forEach(p -> pairs.add(pair(p[0], p[1])));
    new RandomGenerator().doubles(NUMBER_COUNT)
      .filter(d -> d != 0)
      .forEach(d -> pairs.add(pair(withExponentNear(random, Math.getExponent(d)), new Quadruple(d))));
    new RandomGenerator().longs(NUMBER_COUNT)
      .filter(l -> l != 0)
      .forEach(l -> pairs.add(pair(withExponentNear(random, 63 - Long.numberOfLeadingZeros(Math.abs(l))),
                                   new Quadruple(l))));
    return pairs;
  }

  /* ************************************************************************
   * Hand-made edge cases
   */

  private static List<Pair> edgeCases() {
    final ExactValue one = pow2(0), ulpOfOne = pow2(-128), halfUlp = pow2(-129);
    final ExactValue max = ExactValue.of(maxValue()), ulpOfMax = pow2(EXPONENT_OF_MAX_VALUE - EXPONENT_BIAS - 128);
    final ExactValue minValue = ExactValue.of(minValue()), maxSubnormal = ExactValue.of(new Quadruple(false, 0, -1, -1));
    final ExactValue minNormal = ExactValue.of(minNormal());
    final ExactValue tiny = pow2(-250);                   // the differences with tiny must fit into 129 bits

    final List<Pair> pairs = new ArrayList<>(List.of(
        pair("1", one, "half ulp of 1", halfUlp),                                      // tie
        pair("1", one, "half ulp of 1 - 2^-250", halfUlp.subtract(tiny)),              // just below the tie
        pair("1", one, "half ulp of 1 + 2^-250", halfUlp.add(tiny)),                   // just above the tie
        pair("1", one, "quarter ulp of 1", pow2(-130)),                                // 1 - 2^-130 is a tie in the lower binade
        pair("1", one, "quarter ulp of 1 + 2^-250", pow2(-130).add(tiny)),
        pair("1", one, "quarter ulp of 1 - 2^-250", pow2(-130).subtract(tiny)),
        pair("1", one, "1/8 ulp of 1", pow2(-131)),
        pair("1", one, "ulp of 1", ulpOfOne),
        pair("2 - ulp", pow2(1).subtract(pow2(-127)), "half ulp of 1", halfUlp),         // carry to the exponent
        pair("2 - ulp", pow2(1).subtract(pow2(-127)), "ulp of 1", ulpOfOne),
        pair("1 + ulp", one.add(ulpOfOne), "half ulp of 1", halfUlp),                  // tie, odd LSB
        pair("1.5", one.add(pow2(-1)), "1.5", one.add(pow2(-1))),
        pair("MAX_VALUE", max, "MAX_VALUE", max),                                      // overflow
        pair("MAX_VALUE", max, "half ulp of MAX_VALUE", ulpOfMax.multiply(pow2(-1))),  // tie -> Infinity
        pair("MAX_VALUE", max, "half ulp of MAX_VALUE - tiny",
             ulpOfMax.multiply(pow2(-1)).subtract(ulpOfMax.multiply(pow2(-100)))),     // -> MAX_VALUE
        pair("MAX_VALUE", max, "ulp of MAX_VALUE", ulpOfMax),
        pair("max subnormal", maxSubnormal, "MIN_VALUE", minValue),                    // -> MIN_NORMAL
        pair("MIN_NORMAL", minNormal, "MIN_VALUE", minValue),
        pair("MIN_NORMAL", minNormal, "max subnormal", maxSubnormal),
        pair("MIN_VALUE", minValue, "MIN_VALUE", minValue),
        pair("1", one, "MIN_VALUE", minValue),                                         // far below the ulp
        pair("1", one, "2^-1000", pow2(-1000)),
        pair("1 + ulp", one.add(ulpOfOne), "2^-1000", pow2(-1000)),
        pair("MAX_VALUE", max, "1", one),
        pair("MAX_VALUE", max, "MIN_VALUE", minValue)));

    // Exponent gaps around the length of the mantissa, b = a power of 2, all bits set, the highest bit only
    final ExactValue[] firsts = { one, one.add(pow2(-1)), pow2(1).subtract(pow2(-127)) };
    final String[] firstNames = { "1", "1.5", "2 - ulp" };
    for (int i = 0; i < firsts.length; i++)
      for (int gap = 125; gap <= 133; gap++) {
        final ExactValue p = pow2(-gap);
        pairs.add(pair(firstNames[i], firsts[i], "2^-" + gap, p));
        pairs.add(pair(firstNames[i], firsts[i], "(2 - ulp) * 2^-" + gap, p.multiply(pow2(1).subtract(pow2(-127)))));
        pairs.add(pair(firstNames[i], firsts[i], "1.5 * 2^-" + gap, p.multiply(one.add(pow2(-1)))));
      }

    // Subnormal operands with normal ones of small exponents (the shift of the subnormal mantissa about 64 and 128 bits)
    // and of huge exponents (the shift near 2^31 and beyond, BUG-12)
    final Quadruple[] subnormals = { new Quadruple(false, 0, -1, -1), new Quadruple(false, 0, HIGH_BIT, 0),
                                     new Quadruple(false, 0, HIGH_BIT, 1), new Quadruple(false, 0, 0, 1),
                                     new Quadruple(false, 0, 1, HIGH_BIT) };
    for (final int exponent : new int[] { 2, 64, 65, 66, 127, 128, 129, 130, 131, 132,
                                          0x7FFF_FF7F, 0x7FFF_FF80, 0x7FFF_FFC0, 0x7FFF_FFFF, 0x8000_0000, 0x8000_0001,
                                          (int)EXPONENT_OF_MAX_VALUE })
      for (final Quadruple subnormal : subnormals) {
        pairs.add(pair(new Quadruple(false, exponent, 0, 0), subnormal));
        pairs.add(pair(new Quadruple(false, exponent, 0x5555_5555_5555_5555L, 0xAAAA_AAAA_AAAA_AAAAL), subnormal));
      }

    // Both signs of all of them
    final List<Pair> withSigns = new ArrayList<>();
    for (final Pair p : pairs) {
      withSigns.add(p);
      withSigns.add(new Pair(p.a(), negated(p.b())));
      withSigns.add(new Pair(negated(p.a()), p.b()));
      withSigns.add(new Pair(negated(p.a()), negated(p.b())));
    }
    return withSigns;
  }

  /* ************************************************************************
   * Random cases
   */

  /** Random operands with exponents differing by up to 140, random signs */
  private static Pair closePair(Random random) {
    final Quadruple a = randomNormal(random, random.nextInt(2001) - 1000);
    final long exponent = unbiasedExponent(a) + random.nextInt(281) - 140;
    return pair(a, randomNormal(random, (int)exponent));
  }

  /**
   * A tie or a near-tie: {@code b = odd * 2^(lsb(a) - 1)}, so that the bits of {@code b} below the LSB of {@code a}
   * are exactly a half of the ulp of {@code a}; for near-ties a small fraction of the ulp of {@code a} is added or subtracted.
   * In 1/4 of the cases {@code a} is a power of 2 (subtraction gives a result in the lower binade).
   */
  private static Pair tie(Random random) {
    final boolean powerOf2 = random.nextInt(4) == 0;
    final boolean small = random.nextInt(4) == 0;               // b is subnormal (in most cases)
    final int exponent = small? (int)(2 - EXPONENT_BIAS) + random.nextInt(135) : random.nextInt(2001) - 1000;
    final Quadruple a = powerOf2? new Quadruple(random.nextBoolean(), EXPONENT_BIAS + exponent, 0, 0)
                                : randomNormal(random, exponent);
    final long lsbOfA = (long)exponent - 128;                   // long: the exponent may be near -2^31
    final int oddLength = 1 + random.nextInt(128);
    final BigInteger odd = new BigInteger(oddLength, random).setBit(0);
    ExactValue b = ExactValue.of(false, odd, lsbOfA - 1);
    final int nearness = random.nextInt(3);                     // 0: tie, 1: above, 2: below
    if (nearness != 0 && lsbOfA - 1 > MIN_LSB_EXPONENT) {
      final int j = (int)Math.min(1 + random.nextInt(Math.max(1, 128 - odd.bitLength())),  // keep b representable
                                  lsbOfA - 1 - MIN_LSB_EXPONENT);
      final ExactValue delta = pow2(lsbOfA - 1 - j);
      b = nearness == 1? b.add(delta) : b.subtract(delta);
    }
    if (random.nextBoolean())
      b = b.negate();
    return pair(a, toQuadruple(b));
  }

  /** Close values, a result much less than the operands: {@code a} and {@code a} with low-order bits changed */
  private static Pair cancellation(Random random) {
    final boolean subnormal = random.nextInt(4) == 0;
    final Quadruple a = subnormal? new Quadruple(random.nextBoolean(), 0, random.nextLong(), random.nextLong())
                                 : randomNormal(random, random.nextInt(2001) - 1000);
    final long mantLo = a.mantLo() ^ (random.nextLong() >>> random.nextInt(64));
    final long mantHi = random.nextBoolean()? a.mantHi() : a.mantHi() ^ (random.nextLong() >>> random.nextInt(64));
    final int exponent = !subnormal && random.nextInt(4) == 0? a.exponent() - 1 : a.exponent();   // a binade lower
    final boolean negative = random.nextBoolean()? a.isNegative() : !a.isNegative();               // add or subtract
    final Quadruple b = new Quadruple(negative, exponent, mantHi, mantLo);
    return b.isZero() || (a.mantLo() == mantLo && a.mantHi() == mantHi && a.exponent() == exponent)?
        cancellation(random) : pair(a, b);
  }

  /**
   * A difference at the boundary of a binade: {@code |b| = (k + r) * ulp(a)} with an integer {@code k} and {@code 0 <= r < 1},
   * {@code |a| = 2^e + k * ulp(a)}, so that {@code |a| - |b| = 2^e - r * ulp(a)}, which is in the lower binade if {@code r > 0}.
   * {@code b} is a normal value with a random 129-bit significand, shifted by 1 to 128 bits relative to {@code a};
   * {@code r} is random, or equal or close to 1/4, 1/2, 3/4 (the thresholds of the rounding in the lower binade,
   * where the ulp is a half as much). The signs are random: both operations are applied to each pair,
   * so the magnitudes are subtracted in one of them.
   */
  private static Pair binadeBoundary(Random random) {
    final int shift = 1 + random.nextInt(128);
    final boolean small = random.nextInt(8) == 0;                     // b is near MIN_NORMAL
    final long exponent = small? 1 - EXPONENT_BIAS + shift + random.nextInt(4) : random.nextInt(2001) - 1000;
    final long lsbOfA = exponent - 128;
    BigInteger significand = new BigInteger(128, random).setBit(128);   // of b, in units of ulp(b) = 2^(lsbOfA - shift)
    final BigInteger k = significand.shiftRight(shift);
    if (shift >= 2 && random.nextInt(4) != 0) {                       // r near 1/4, 1/2, 3/4
      BigInteger f = BigInteger.valueOf(1 + random.nextInt(3)).shiftLeft(shift - 2);
      final int nearness = random.nextInt(3);                         // 0: exactly, 1: above, 2: below
      if (nearness != 0) {
        final BigInteger delta = ONE.shiftLeft(random.nextInt(shift - 1));
        f = nearness == 1? f.add(delta) : f.subtract(delta);
      }
      significand = k.shiftLeft(shift).or(f);
    }
    final ExactValue a = pow2(exponent).add(ExactValue.of(false, k, lsbOfA));
    final ExactValue b = ExactValue.of(false, significand, lsbOfA - shift);
    return pair("2^e + k ulp", random.nextBoolean()? a : a.negate(), "(k + r) ulp, shift " + shift,
                random.nextBoolean()? b : b.negate());
  }

  /** A random normal value with an exponent within 70 of the given one */
  private static Quadruple withExponentNear(Random random, int exponent) {
    return randomNormal(random, exponent + random.nextInt(141) - 70);
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

}
