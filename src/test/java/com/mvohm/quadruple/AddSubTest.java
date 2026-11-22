package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.BinaryOperation;
import com.mvohm.quadruple.testutil.CallForm;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for addition and subtraction of finite non-zero values, in all forms of the call ({@link CallForm}).
 * <p>
 * The reference is the model ({@link ExactValue}): the result must be the exact sum (difference) correctly rounded
 * to the nearest Quadruple value, ties away from zero (half-up), strictly, without any tolerance (ε = 0),
 * since the sum is computed exactly; values exceeding {@code MAX_VALUE} by a half of its ulp or more give Infinity.
 * If the exact result is zero, it must be +0, like in {@code double} (IEEE-754, rounding to nearest).
 * <p>
 * Special operands (NaN, infinities, zeros) are tested by {@code SpecialValuesTest}.
 */
public class AddSubTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.AddSubData#";

  @ParameterizedTest(name = "{0}: {1}, a = {2}, b = {3}")
  @MethodSource(DATA + "operations")
  void shouldBeCorrectlyRounded(BinaryOperation op, CallForm form, Quadruple a, Quadruple b) {
    final Quadruple actual = op.apply(form, a, b);
    final ExactValue exact = op.applyToExactValues(ExactValue.of(a), ExactValue.of(b));
    final String context = op.describe(form, a, b);
    if (exact.isZero())
      assertSameFields(actual, new Quadruple(false, 0, 0, 0), context + " (the exact result is 0)");
    else
      assertCorrectlyRounded(actual, exact, 0, context);
  }

}
