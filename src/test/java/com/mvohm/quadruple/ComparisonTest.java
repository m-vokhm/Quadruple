package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for comparisons of Quadruple values: {@code compareTo()} (with Quadruple, long, double),
 * {@code compare()}, {@code compareMagnitudeTo()}, {@code compareMagnitudes()}, {@code equals()}, {@code hashCode()},
 * {@code max()}, {@code min()}, {@code assignMax()}, {@code assignMin()}, {@code signum()}.
 * <p>
 * The reference is the behavior of {@code Double} in all respects:
 * <ul>
 *   <li>{@code compareTo()} — like {@code Double.compare()}: -Infinity &lt; finite values &lt; +Infinity &lt; NaN,
 *       finite values are compared numerically (by the model, {@link ExactValue}), but -0 &lt; +0,
 *       all NaNs are equal to each other;</li>
 *   <li>{@code equals()} — like {@code Double.equals()}: true iff {@code compareTo()} returns 0
 *       (so that all NaNs are equal, and -0 is not equal to +0); equal values have equal hash codes;</li>
 *   <li>{@code max()}, {@code min()} — like {@code Math.max()}, {@code Math.min()}: NaN if any of the operands
 *       is NaN, otherwise the greater (lesser) of the operands, -0 &lt; +0;</li>
 *   <li>{@code signum()} — like {@code Math.signum()}: NaN for NaN, -0.0 for -0
 *       (to be changed to return double in the next release, BUG-08);</li>
 *   <li>{@code compareMagnitudeTo()} has no analog in Double; it is like {@code compareTo()} applied to the
 *       absolute values: NaN is the greatest, then Infinity, then finite values, -0 is equal to +0;
 *       the result is -1, 0 or 1, as documented.</li>
 * </ul>
 * {@code compareTo(long)}, {@code compareTo(double)} are as if the argument were converted to Quadruple.
 */
public class ComparisonTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.ComparisonData#";

  /* ************************************************************************
   * compareTo, compare
   */

  @ParameterizedTest
  @MethodSource(DATA + "ladderPairs")
  void shouldOrderLadderValues(Quadruple a, Quadruple b, int expected) {
    assertThat(Integer.signum(a.compareTo(b))).as("compareTo " + pair(a, b)).isEqualTo(expected);
    assertThat(Integer.signum(Quadruple.compare(a, b))).as("compare " + pair(a, b)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource(DATA + "pairs")
  void shouldCompare(Quadruple a, Quadruple b) {
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    final int expected = expectedCompare(a, b);
    assertThat(Integer.signum(a.compareTo(b))).as("compareTo " + pair(a, b)).isEqualTo(expected);
    assertThat(Integer.signum(Quadruple.compare(a, b))).as("compare " + pair(a, b)).isEqualTo(expected);
    assertUnchanged(a, a0, b, b0);
  }

  @ParameterizedTest
  @MethodSource(DATA + "quadrupleAndLongPairs")
  void shouldCompareToLong(Quadruple a, long b) {
    final Quadruple a0 = new Quadruple(a);
    assertThat(Integer.signum(a.compareTo(b))).as("compareTo(%s) of %s", b, fieldsOf(a))
      .isEqualTo(Key.of(a).compareTo(Key.of(b)));
    assertSameFields(a, a0, "the instance after compareTo(long)");
  }

  @ParameterizedTest
  @MethodSource(DATA + "quadrupleAndDoublePairs")
  void shouldCompareToDouble(Quadruple a, double b) {
    final Quadruple a0 = new Quadruple(a);
    assertThat(Integer.signum(a.compareTo(b))).as("compareTo(%s) of %s", describe(b), fieldsOf(a))
      .isEqualTo(Key.of(a).compareTo(Key.of(b)));
    assertSameFields(a, a0, "the instance after compareTo(double)");
  }

  /* ************************************************************************
   * compareMagnitudeTo, compareMagnitudes
   */

  @ParameterizedTest
  @MethodSource(DATA + "pairs")
  void shouldCompareMagnitudes(Quadruple a, Quadruple b) {
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    final int expected = Key.of(a).abs().compareTo(Key.of(b).abs());
    assertThat(a.compareMagnitudeTo(b)).as("compareMagnitudeTo " + pair(a, b)).isEqualTo(expected);
    assertThat(Quadruple.compareMagnitudes(a, b)).as("compareMagnitudes " + pair(a, b)).isEqualTo(expected);
    assertUnchanged(a, a0, b, b0);
  }

  /* ************************************************************************
   * equals, hashCode
   */

  @ParameterizedTest
  @MethodSource(DATA + "pairsForEquals")
  void shouldTestEquality(Quadruple a, Quadruple b) {
    final boolean expected = expectedCompare(a, b) == 0;
    assertThat(a.equals(b)).as("equals " + pair(a, b)).isEqualTo(expected);
    assertThat(b.equals(a)).as("equals " + pair(b, a)).isEqualTo(expected);
    if (expected)
      assertThat(a.hashCode()).as("hashCode of equal values " + pair(a, b)).isEqualTo(b.hashCode());
  }

  @Disabled("BUG-06: equals() returns false for different instances of NaN, unlike Double.equals(), see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "pairsOfNaNs")
  void shouldConsiderAllNaNsEqual(Quadruple a, Quadruple b) {
    shouldTestEquality(a, b);
  }

  @Test
  void shouldBeEqualToItselfAndNotToOtherObjects() {
    for (final Quadruple q : new Quadruple[] { Quadruple.nan(), Quadruple.one(), new Quadruple(true, 0, 0, 0) }) {
      assertThat(q.equals(q)).as("equals itself: " + fieldsOf(q)).isTrue();
      assertThat(q.equals(null)).as("equals null: " + fieldsOf(q)).isFalse();
      assertThat(q.equals(q.doubleValue())).as("equals Double: " + fieldsOf(q)).isFalse();
    }
  }

  /* ************************************************************************
   * max, min, assignMax, assignMin
   */

  @ParameterizedTest
  @MethodSource(DATA + "pairs")
  void shouldFindMax(Quadruple a, Quadruple b) {
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    final Quadruple result = Quadruple.max(a, b);
    assertThat(result).as("max " + pair(a, b) + " must be a new instance").isNotSameAs(a).isNotSameAs(b);
    checkResult(result, expectedMax(a0, b0), "max " + pair(a0, b0));
    assertUnchanged(a, a0, b, b0);
  }

  @ParameterizedTest
  @MethodSource(DATA + "pairsForMin")
  void shouldFindMin(Quadruple a, Quadruple b) {
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    final Quadruple result = Quadruple.min(a, b);
    assertThat(result).as("min " + pair(a, b) + " must be a new instance").isNotSameAs(a).isNotSameAs(b);
    checkResult(result, expectedMin(a0, b0), "min " + pair(a0, b0));
    assertUnchanged(a, a0, b, b0);
  }

  @ParameterizedTest
  @MethodSource(DATA + "pairs")
  void shouldAssignMax(Quadruple a, Quadruple b) {
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    assertThat(a.assignMax(b)).as("assignMax must return this").isSameAs(a);
    checkResult(a, expectedMax(a0, b0), "assignMax " + pair(a0, b0));
    assertSameFields(b, b0, "the argument after assignMax " + pair(a0, b0));
  }

  @ParameterizedTest
  @MethodSource(DATA + "pairsForMin")
  void shouldAssignMin(Quadruple a, Quadruple b) {
    final Quadruple a0 = new Quadruple(a), b0 = new Quadruple(b);
    assertThat(a.assignMin(b)).as("assignMin must return this").isSameAs(a);
    checkResult(a, expectedMin(a0, b0), "assignMin " + pair(a0, b0));
    assertSameFields(b, b0, "the argument after assignMin " + pair(a0, b0));
  }

  @Disabled("BUG-07: min() and assignMin() ignore a NaN operand, unlike Math.min(), see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "pairsWithOneNaN")
  void shouldGiveNaNAsMinIfAnyOperandIsNaN(Quadruple a, Quadruple b) {
    shouldFindMin(new Quadruple(a), new Quadruple(b));
    shouldAssignMin(a, b);
  }

  /** Math.max(): NaN if any is NaN, otherwise the greater one (-0 < +0) */
  private static Quadruple expectedMax(Quadruple a, Quadruple b) {
    return a.isNaN()? a : b.isNaN()? b : expectedCompare(a, b) >= 0? a : b;
  }

  /** Math.min(): NaN if any is NaN, otherwise the lesser one (-0 < +0) */
  private static Quadruple expectedMin(Quadruple a, Quadruple b) {
    return a.isNaN()? a : b.isNaN()? b : expectedCompare(a, b) <= 0? a : b;
  }

  /** Any NaN is OK for NaN, otherwise the same fields (equal non-NaN values have the same fields) */
  private static void checkResult(Quadruple actual, Quadruple expected, String context) {
    if (expected.isNaN())
      assertThat(actual.isNaN()).as(context + " must be NaN, actual: " + fieldsOf(actual)).isTrue();
    else
      assertSameFields(actual, expected, context);
  }

  /* ************************************************************************
   * signum
   */

  @ParameterizedTest
  @MethodSource(DATA + "valuesForSignum")
  void shouldReturnSignum(Quadruple q) {
    final double actual = q.signum();      // compiles both with int signum() (now) and double signum() (BUG-08)
    final double expected = q.isNaN()? Double.NaN :
                            q.isZero()? (q.isNegative()? -0.0 : 0.0) :
                            q.isNegative()? -1.0 : 1.0;
    assertThat(Double.doubleToLongBits(actual)).as("signum() of %s: %s, expected %s", fieldsOf(q), actual, expected)
      .isEqualTo(Double.doubleToLongBits(expected));        // NaNs are equal, 0.0 and -0.0 are not
  }

  @Disabled("BUG-08: signum() returns int, can't return NaN for NaN and -0.0 for -0 like Math.signum(),"
            + " see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "negativeZeroAndNaNs")
  void shouldReturnSignumOfNaNAndNegativeZeroLikeMathSignum(Quadruple q) {
    shouldReturnSignum(q);
  }

  /* ************************************************************************
   * The reference
   */

  /** The expected result of {@code compareTo()}: -1, 0, or 1 */
  private static int expectedCompare(Quadruple a, Quadruple b) {
    return Key.of(a).compareTo(Key.of(b));
  }

  /**
   * A value in the order of {@code Double.compare()}: the rank of the value (-Infinity, finite, +Infinity, NaN)
   * and, for finite values, the exact value and the sign (to distinguish -0 from +0).
   */
  private record Key(int rank, ExactValue value, boolean negative) implements Comparable<Key> {

    private static final int NEGATIVE_INFINITY = -1, FINITE = 0, POSITIVE_INFINITY = 1, NAN = 2;

    static Key of(Quadruple q) {
      return q.isNaN()?      new Key(NAN, null, false) :
             q.isInfinite()? new Key(q.isNegative()? NEGATIVE_INFINITY : POSITIVE_INFINITY, null, q.isNegative()) :
                             new Key(FINITE, ExactValue.of(q), q.isNegative());
    }

    static Key of(double d) {
      return Double.isNaN(d)?      new Key(NAN, null, false) :
             Double.isInfinite(d)? new Key(d < 0? NEGATIVE_INFINITY : POSITIVE_INFINITY, null, d < 0) :
                                   new Key(FINITE, ExactValue.of(d), Double.doubleToRawLongBits(d) < 0);
    }

    static Key of(long l) {
      return new Key(FINITE, ExactValue.of(l), l < 0);
    }

    /** The key of the absolute value, also for NaN and infinities: NaN &gt; +Infinity &gt; finite, -0 == +0 */
    Key abs() {
      return rank == NAN?    this :
             rank != FINITE? new Key(POSITIVE_INFINITY, null, false) :
                             new Key(FINITE, value.isNegative()? value.negate() : value, false);
    }

    @Override
    public int compareTo(Key other) {
      if (rank != other.rank || rank != FINITE)
        return Integer.compare(rank, other.rank);
      final int result = value.compareTo(other.value);
      if (result == 0 && value.isZero())                     // -0 < +0
        return Boolean.compare(other.negative, negative);
      return result;
    }
  }

  /* ************************************************************************
   * Helpers
   */

  private static String pair(Quadruple a, Quadruple b) {
    return "(" + fieldsOf(a) + "), (" + fieldsOf(b) + ")";
  }

  private static void assertUnchanged(Quadruple a, Quadruple a0, Quadruple b, Quadruple b0) {
    assertSameFields(a, a0, "the first operand after the operation");
    assertSameFields(b, b0, "the second operand after the operation");
  }

}
