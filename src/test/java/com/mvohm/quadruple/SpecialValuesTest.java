package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testdata.SpecialValuesData.Kind;
import com.mvohm.quadruple.testutil.BinaryOperation;
import com.mvohm.quadruple.testutil.CallForm;

/**
 * Tests for arithmetic with special values (NaN, infinities, zeros), and for the classification methods
 * ({@code isNaN()}, {@code isInfinite()}, {@code isZero()}, {@code isNegative()}).
 * <p>
 * The reference is the arithmetic of {@code double}. Each operand is replaced by its "proxy" double:
 * NaN, infinities and zeros by the same double values, finite non-zero values by ±1.5 (of the same sign),
 * and the operation is performed on the proxies:
 * <ul>
 *   <li>if the result is NaN, the result of Quadruple must be NaN (any NaN);</li>
 *   <li>if the result is an infinity or a zero, the result of Quadruple must be the same, including the sign;</li>
 *   <li>otherwise, it is a case like {@code x + 0}, {@code 0 - x}, {@code x - 0}, and the result must be
 *       exactly {@code a}, {@code b}, or {@code -b} (with all the fields).</li>
 * </ul>
 * Each case is run in all forms of the call ({@link CallForm}), which also checks the contract of the form.
 * <p>
 * Also, {@code x + (-x)} and {@code x - x} must give +0, like in {@code double} (IEEE-754, rounding to nearest);
 * {@code sqrt()} of special and negative values must give the same as {@code Math.sqrt()}.
 * The other cases of arithmetic (rounding, overflow, underflow) are tested by the tests of the operations.
 */
public class SpecialValuesTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.SpecialValuesData#";

  /* ************************************************************************
   * Binary operations
   */

  @ParameterizedTest(name = "{0}: {1}, a = {2}, b = {3}")
  @MethodSource(DATA + "operationsWithSpecialOperands")
  void shouldHandleSpecialOperandsLikeDouble(BinaryOperation op, CallForm form, Quadruple a, Quadruple b) {
    final Quadruple actual = op.apply(form, a, b);
    final double reference = op.applyToDoubles(proxy(a), proxy(b));
    final String context = op.describe(form, a, b) + ", reference: " + proxy(a) + " " + op + " " + proxy(b)
                           + " = " + reference;
    if (Double.isNaN(reference) || Double.isInfinite(reference) || reference == 0)
      checkSpecial(actual, reference, context);
    else
      assertSameFields(actual, nonZeroOperand(op, a, b), context);
  }

  /** The expected result of {@code a op b} if one of the operands is zero and the other is finite: a, b or -b */
  private static Quadruple nonZeroOperand(BinaryOperation op, Quadruple a, Quadruple b) {
    if (b.isZero())
      return a;                                                 // x + 0, x - 0
    assertThat(a.isZero()).as("one of the operands must be zero").isTrue();
    return op == BinaryOperation.ADD? b : negated(b);           // 0 + x, 0 - x
  }

  @ParameterizedTest(name = "{0}: {1}, a = {2}, b = {3}")
  @MethodSource(DATA + "cancellations")
  void shouldGivePositiveZeroOnCancellation(BinaryOperation op, CallForm form, Quadruple a, Quadruple b) {
    checkSpecial(op.apply(form, a, b), 0.0, op.describe(form, a, b));
  }

  /* ************************************************************************
   * sqrt
   */

  @ParameterizedTest
  @MethodSource(DATA + "valuesForSqrt")
  void shouldComputeSqrtOfSpecialValuesLikeMathSqrt(Quadruple q) {
    final Quadruple q0 = new Quadruple(q);
    final double reference = Math.sqrt(proxy(q));
    final String context = "sqrt of " + fieldsOf(q) + ", reference: Math.sqrt(" + proxy(q) + ") = " + reference;

    final Quadruple fromStatic = Quadruple.sqrt(q);
    assertThat(fromStatic).as("Quadruple.sqrt(q) must return a new instance").isNotSameAs(q);
    assertSameFields(q, q0, "the operand after Quadruple.sqrt(q)");
    checkSpecial(fromStatic, reference, "Quadruple.sqrt(q): " + context);

    final Quadruple fromInstance = q.sqrt();
    assertThat(fromInstance).as("q.sqrt() must return this").isSameAs(q);
    checkSpecial(fromInstance, reference, "q.sqrt(): " + context);
  }

  /* ************************************************************************
   * Classification
   */

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource(DATA + "valuesForClassification")
  void shouldClassifyValues(Quadruple q, Kind kind, boolean negative) {
    final String context = fieldsOf(q);
    assertThat(q.isNaN()).as("isNaN() of " + context).isEqualTo(kind == Kind.NAN);
    assertThat(q.isInfinite()).as("isInfinite() of " + context).isEqualTo(kind == Kind.INFINITY);
    assertThat(q.isZero()).as("isZero() of " + context).isEqualTo(kind == Kind.ZERO);
    assertThat(q.isNegative()).as("isNegative() of " + context).isEqualTo(negative);
  }

  /* ************************************************************************
   * Helpers
   */

  /** The proxy double of a value: the same for special values, ±1.5 for finite non-zero values */
  private static double proxy(Quadruple q) {
    if (q.exponent() == Quadruple.EXPONENT_OF_INFINITY)
      return (q.mantHi() | q.mantLo()) != 0? Double.NaN :
             q.isNegative()? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    if ((q.exponent() | q.mantHi() | q.mantLo()) == 0)
      return q.isNegative()? -0.0 : 0.0;
    return q.isNegative()? -1.5 : 1.5;
  }

  /** Checks that {@code actual} is NaN, an infinity or a zero, the same as the {@code expected} double */
  private static void checkSpecial(Quadruple actual, double expected, String context) {
    if (Double.isNaN(expected)) {
      assertThat(actual.isNaN()).as(context + ": expected NaN, actual: " + fieldsOf(actual)).isTrue();
      return;
    }
    final boolean negative = Double.doubleToRawLongBits(expected) < 0;
    final Quadruple expectedQ = Double.isInfinite(expected)?
        new Quadruple(negative, Quadruple.EXPONENT_OF_INFINITY, 0, 0) :
        new Quadruple(negative, 0, 0, 0);
    assertSameFields(actual, expectedQ, context);
  }

  private static Quadruple negated(Quadruple q) {
    return new Quadruple(!q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
  }

}
