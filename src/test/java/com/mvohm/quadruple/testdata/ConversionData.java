package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.BinaryFormat;
import com.mvohm.quadruple.testutil.ExactValue;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Data for {@code ConversionTest}: Quadruple values to be converted to int, long, double and float.
 * <p>
 * Besides random values in the relevant exponent ranges, the sets contain values built
 * near the boundaries of the target types and near the midpoints between adjacent values
 * of the target formats: exactly at the midpoint, one Quadruple ulp away from it,
 * and a small fraction of the target ulp away from it. Random values hit such cases
 * with a negligible probability, while they are the cases where the rounding may go wrong.
 * <p>
 * The values are built with the model ({@link ExactValue}), from their exact values.
 */
public class ConversionData {

  private static final int RANDOM_COUNT    = 3000;
  private static final int MIDPOINT_COUNT  = 1000;

  /* ************************************************************************
   * int, long
   */

  /**
   * Values for {@code intValue()} and {@code longValue()}: both signs of
   * 0.5, 1 - ulp, 1, 1.5, and 2^31, 2^32, 2^63, 2^64 with neighbors (-1, -0.5, -Quadruple ulp, +0.5, +1),
   * extreme Quadruple values, random values with exponents from -2 to 66 and over the whole range.
   */
  public static Stream<Named<Quadruple>> valuesForIntegerConversion() {
    final ExactValue half = pow2(-1), one = pow2(0);
    final Stream<Labeled> small = Stream.of(
        labeled("0.5",     half),
        labeled("1 - ulp", one.subtract(pow2(-129))),
        labeled("1",       one),
        labeled("1.5",     one.add(half)));
    final Stream<Labeled> bounds = Stream.of(31, 32, 63, 64).flatMap(n -> Stream.of(
        labeled("2^" + n + " - 1",    pow2(n).subtract(one)),
        labeled("2^" + n + " - 0.5",  pow2(n).subtract(half)),
        labeled("2^" + n + " - Quadruple ulp", pow2(n).subtract(pow2(n - 129))),
        labeled("2^" + n,             pow2(n)),
        labeled("2^" + n + " + 0.5",  pow2(n).add(half)),
        labeled("2^" + n + " + 1",    pow2(n).add(one))));
    return Stream.of(
        withBothSigns(Stream.concat(small, bounds)),
        withBothSigns(extremeValues()),
        labeled(new RandomGenerator().quadruples(RANDOM_COUNT, -2, 66)),
        labeled(new RandomGenerator().quadruples(RANDOM_COUNT / 3))
      ).flatMap(s -> s).map(Labeled::toNamed);
  }

  /* ************************************************************************
   * double
   */

  /**
   * Values for {@code doubleValue()} (see {@link #allValuesForDoubleConversion()}),
   * except the ties in the subnormal range of double (BUG-03, see KNOWN_ISSUES.md).
   */
  public static Stream<Named<Quadruple>> valuesForDoubleConversion() {
    return allValuesForDoubleConversion().filter(v -> !isSubnormalDoubleTie(v.getPayload()));
  }

  /** The ties in the subnormal range of double from {@link #allValuesForDoubleConversion()} (BUG-03) */
  public static Stream<Named<Quadruple>> subnormalDoubleTies() {
    return allValuesForDoubleConversion().filter(v -> isSubnormalDoubleTie(v.getPayload()));
  }

  /** @return true if the value of {@code q} is exactly the midpoint between two adjacent subnormal doubles */
  private static boolean isSubnormalDoubleTie(Quadruple q) {
    if (q.isNaN() || q.isInfinite()) return false;
    final ExactValue.Rounding<Double> r = ExactValue.of(q).round(BinaryFormat.DOUBLE);
    return !r.exact() && r.offsetFromMidpoint() == 0 && Math.abs(r.truncated()) < Double.MIN_NORMAL;
  }

  /**
   * All values for {@code doubleValue()}: both signs of
   * <ul>
   *   <li>ties of different kinds: 1 + 2^-53 (rounded down to even), 1 + 3 * 2^-53 (up to even),
   *       MIN_VALUE / 2 (down to zero), 3 * MIN_VALUE / 2 (up to 2 * MIN_VALUE),
   *       the midpoint between the max subnormal and MIN_NORMAL, MAX_VALUE + ulp / 2 (up to infinity);</li>
   *   <li>values near the midpoints between adjacent random normal and subnormal doubles;</li>
   *   <li>extreme Quadruple values, random values with exponents from -1100 to 1050.</li>
   * </ul>
   */
  private static Stream<Named<Quadruple>> allValuesForDoubleConversion() {
    final Stream<Labeled> edges = Stream.of(
        nearMidpoint("1",                    1.0),
        nearMidpoint("1 + ulp",              Math.nextUp(1.0)),
        nearMidpoint("MIN_VALUE",            Double.MIN_VALUE),
        nearMidpoint("0",                    0.0),
        nearMidpoint("max subnormal",        Math.nextDown(Double.MIN_NORMAL)),
        nearMidpoint("MIN_NORMAL",           Double.MIN_NORMAL),
        nearMidpoint("MAX_VALUE",            Double.MAX_VALUE)
      ).flatMap(s -> s);
    final Stream<Labeled> randomMidpoints = Stream.concat(
        new RandomGenerator().doubles(MIDPOINT_COUNT).boxed(),
        new RandomGenerator().subnormalDoubles(MIDPOINT_COUNT / 2).boxed()
      ).flatMap(d -> nearMidpoint(String.valueOf(Math.abs(d)), Math.abs(d)));
    return Stream.of(
        withBothSigns(Stream.concat(edges, randomMidpoints)),
        withBothSigns(extremeValues()),
        labeled(new RandomGenerator().quadruples(RANDOM_COUNT, -1100, 1050))
      ).flatMap(s -> s).map(Labeled::toNamed);
  }

  /**
   * Values near the midpoint between {@code d} and the next double:
   * exactly at it, +/- 1 Quadruple ulp, +/- 2^-60 ulp of double
   */
  private static Stream<Labeled> nearMidpoint(String name, double d) {
    return nearMidpoint(name, ExactValue.of(d), ExactValue.of(Math.ulp(d)), 60);
  }

  /* ************************************************************************
   * float
   */

  /**
   * Values for {@code floatValue()} (see {@link #allValuesForFloatConversion()}),
   * except those that are rounded incorrectly by rounding to double first (BUG-04, see KNOWN_ISSUES.md).
   */
  public static Stream<Named<Quadruple>> valuesForFloatConversion() {
    return allValuesForFloatConversion().filter(v -> !isFloatDoubleRoundingCase(v.getPayload()));
  }

  /** The values from {@link #allValuesForFloatConversion()} that are rounded incorrectly via double (BUG-04) */
  public static Stream<Named<Quadruple>> floatDoubleRoundingCases() {
    return allValuesForFloatConversion().filter(v -> isFloatDoubleRoundingCase(v.getPayload()));
  }

  /**
   * @return true if rounding the value of {@code q} to double first, and then to float,
   * gives a result different from rounding it to float directly (a double rounding error)
   */
  private static boolean isFloatDoubleRoundingCase(Quadruple q) {
    if (q.isNaN() || q.isInfinite()) return false;
    final ExactValue x = ExactValue.of(q);
    final double d = x.round(BinaryFormat.DOUBLE).rounded();
    if (Double.isInfinite(d)) return false;
    return !BinaryFormat.FLOAT.same(x.round(BinaryFormat.FLOAT).rounded(),
                                    ExactValue.of(d).round(BinaryFormat.FLOAT).rounded());
  }

  /**
   * All values for {@code floatValue()}: the same kinds as for {@code doubleValue()}, with respect to floats.
   * Besides, the values that are greater or less than the midpoint between adjacent floats by
   * less than a half of the ulp of double (2^-31 and 2^-40 of the ulp of float): converting them
   * to double first would give exactly the midpoint, so that the second rounding, to float,
   * could go the wrong way (a "double rounding" error).
   */
  private static Stream<Named<Quadruple>> allValuesForFloatConversion() {
    final Stream<Labeled> edges = Stream.of(
        nearFloatMidpoint("1",                 1.0f),
        nearFloatMidpoint("1 + ulp",           Math.nextUp(1.0f)),
        nearFloatMidpoint("MIN_VALUE",         Float.MIN_VALUE),
        nearFloatMidpoint("0",                 0.0f),
        nearFloatMidpoint("max subnormal",     Math.nextDown(Float.MIN_NORMAL)),
        nearFloatMidpoint("MIN_NORMAL",        Float.MIN_NORMAL),
        nearFloatMidpoint("MAX_VALUE",         Float.MAX_VALUE)
      ).flatMap(s -> s);
    final Stream<Labeled> randomMidpoints = new RandomGenerator().floats(MIDPOINT_COUNT)
        .flatMap(f -> nearFloatMidpoint(String.valueOf(Math.abs(f)), Math.abs(f)));
    return Stream.of(
        withBothSigns(Stream.concat(edges, randomMidpoints)),
        withBothSigns(extremeValues()),
        labeled(new RandomGenerator().quadruples(RANDOM_COUNT, -160, 140))
      ).flatMap(s -> s).map(Labeled::toNamed);
  }

  /**
   * Values near the midpoint between {@code f} and the next float: exactly at it, +/- 1 Quadruple ulp,
   * +/- 2^-20, 2^-31 and 2^-40 ulp of float (the last two are less than a half ulp of double)
   */
  private static Stream<Labeled> nearFloatMidpoint(String name, float f) {
    final ExactValue x = ExactValue.of(f), ulp = ExactValue.of(Math.ulp(f));
    return nearMidpoint(name, x, ulp, 20, 31, 40);
  }

  /* ************************************************************************
   * Helpers
   */

  /**
   * Values near the midpoint between {@code x} and {@code x + ulp} (both non-negative):
   * exactly at it, +/- 1 Quadruple ulp, and +/- {@code 2^-k} of the given ulp for each of the given {@code k}
   */
  private static Stream<Labeled> nearMidpoint(String name, ExactValue x, ExactValue ulp, int... ks) {
    final ExactValue mid = x.add(ulp.divide(ExactValue.of(2)));
    final ExactValue quadUlp = pow2(unbiasedExponent(toQuadruple(mid)) - 128);
    final String midName = "mid(" + name + ")";
    final Stream<Labeled> nearest = Stream.of(
        labeled(midName,                        mid),
        labeled(midName + " + Quadruple ulp",   mid.add(quadUlp)),
        labeled(midName + " - Quadruple ulp",   mid.subtract(quadUlp)));
    final Stream<Labeled> farther = Arrays.stream(ks).boxed().flatMap(k -> Stream.of(
        labeled(midName + " + 2^-" + k + " ulp", mid.add(ulp.multiply(pow2(-k)))),
        labeled(midName + " - 2^-" + k + " ulp", mid.subtract(ulp.multiply(pow2(-k))))));
    return Stream.concat(nearest, farther);
  }

  /** MAX_VALUE, MIN_NORMAL, the largest subnormal and MIN_VALUE of Quadruple */
  private static Stream<Labeled> extremeValues() {
    return Stream.of(
        new Labeled("Quadruple MAX_VALUE",         Quadruple.maxValue()),
        new Labeled("Quadruple MIN_NORMAL",        new Quadruple(false, EXPONENT_OF_MIN_NORMAL, 0, 0)),
        new Labeled("Quadruple largest subnormal", new Quadruple(false, 0, -1L, -1L)),
        new Labeled("Quadruple MIN_VALUE",         Quadruple.minValue()));
  }

  /** A value with a label describing its origin */
  private record Labeled(String label, Quadruple value) {
    Labeled negated() {
      return new Labeled("-" + label, new Quadruple(!value.isNegative(), value.exponent(), value.mantHi(), value.mantLo()));
    }
    Named<Quadruple> toNamed() {
      return Named.of(label.isEmpty()? QuadAssert.fieldsOf(value) : label + " (" + QuadAssert.fieldsOf(value) + ")", value);
    }
  }

  /** The given values and their negations */
  private static Stream<Labeled> withBothSigns(Stream<Labeled> values) {
    return values.flatMap(v -> Stream.of(v, v.negated()));
  }

  private static Stream<Labeled> labeled(Stream<Quadruple> values) {
    return values.map(q -> new Labeled("", q));
  }

  private static Labeled labeled(String label, ExactValue value) {
    return new Labeled(label, toQuadruple(value));
  }

  /** @return the Quadruple exactly equal to the given value; throws if it's not representable */
  private static Quadruple toQuadruple(ExactValue value) {
    final ExactValue.Rounding<Quadruple> r = value.round();
    if (!r.exact())
      throw new IllegalStateException("Test data error: the value is not representable as Quadruple: " + value);
    return r.rounded();
  }

  private static long unbiasedExponent(Quadruple q) {
    return (q.exponent() & 0xFFFF_FFFFL) - EXPONENT_BIAS;
  }

  private static ExactValue pow2(long exponent) {
    return ExactValue.of(false, ONE, exponent);
  }

}
