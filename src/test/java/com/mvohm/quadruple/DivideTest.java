package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.BinaryOperation;
import com.mvohm.quadruple.testutil.CallForm;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for division of finite non-zero values, in all forms of the call ({@link CallForm}).
 * <p>
 * The reference is the model ({@link ExactValue}): the result must be the exact quotient correctly rounded
 * to the nearest Quadruple value, ties away from zero (half-up), in the subnormal range too;
 * values exceeding {@code MAX_VALUE} by a half of its ulp or more give Infinity,
 * values less than a half of {@code MIN_VALUE} give zero (of the sign of the quotient).
 * <p>
 * The rounding is checked strictly, without any tolerance (ε = 0): the library decides the rounding
 * by comparing the doubled remainder with the divisor, i.e. exactly, so the quotient must be correctly rounded
 * however close it is to the midpoint between two adjacent Quadruple values
 * (the data include quotients closer to the midpoint than 2^-140 ulp).
 * <p>
 * Special operands (NaN, infinities, zeros) are tested by {@code SpecialValuesTest}.
 */
public class DivideTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.DivideData#";

  @ParameterizedTest(name = "{0}: {1}, a = {2}, b = {3}")
  @MethodSource(DATA + "operations")
  void shouldBeCorrectlyRounded(BinaryOperation op, CallForm form, Quadruple a, Quadruple b) {
    final Quadruple actual = op.apply(form, a, b);
    final ExactValue exact = op.applyToExactValues(ExactValue.of(a), ExactValue.of(b));
    assertCorrectlyRounded(actual, exact, 0, op.describe(form, a, b));
  }

}
