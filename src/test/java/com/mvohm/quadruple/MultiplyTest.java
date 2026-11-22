package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.BinaryOperation;
import com.mvohm.quadruple.testutil.CallForm;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for multiplication of finite non-zero values, in all forms of the call ({@link CallForm}).
 * <p>
 * The reference is the model ({@link ExactValue}): the result must be the exact product correctly rounded
 * to the nearest Quadruple value, ties away from zero (half-up), strictly, without any tolerance (ε = 0),
 * since the product is computed exactly; in the subnormal range too; values exceeding {@code MAX_VALUE}
 * by a half of its ulp or more give Infinity, values less than a half of {@code MIN_VALUE} give zero
 * (of the sign of the product).
 * <p>
 * Special operands (NaN, infinities, zeros) are tested by {@code SpecialValuesTest}.
 */
public class MultiplyTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.MultiplyData#";

  @ParameterizedTest(name = "{0}: {1}, a = {2}, b = {3}")
  @MethodSource(DATA + "operations")
  void shouldBeCorrectlyRounded(BinaryOperation op, CallForm form, Quadruple a, Quadruple b) {
    final Quadruple actual = op.apply(form, a, b);
    final ExactValue exact = op.applyToExactValues(ExactValue.of(a), ExactValue.of(b));
    assertCorrectlyRounded(actual, exact, 0, op.describe(form, a, b));
  }

}
