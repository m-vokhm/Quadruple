package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for the square root of positive finite non-zero values, in both forms of the call:
 * {@code q.sqrt()} (must assign the result to {@code q} and return it) and {@code Quadruple.sqrt(q)}
 * (must return a new instance and leave the argument unchanged).
 * <p>
 * The reference is the model ({@link ExactValue}): the result must be the exact square root correctly rounded
 * to the nearest Quadruple value, strictly, without any tolerance (ε = 0), like {@code Math.sqrt()} (IEEE-754).
 * The root of a Quadruple value can't be an exact tie, but it can be arbitrarily close to the midpoint.
 * Exact roots must be exact.
 * <p>
 * Special values and negative arguments are tested by {@code SpecialValuesTest}.
 */
public class SqrtTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.SqrtData#";

  @ParameterizedTest
  @MethodSource(DATA + "values")
  void shouldBeCorrectlyRounded(Quadruple q) {
    final Quadruple q0 = new Quadruple(q);
    final ExactValue exact = ExactValue.of(q).sqrt();

    final Quadruple fromStatic = Quadruple.sqrt(q);
    assertThat(fromStatic).as("Quadruple.sqrt(q) must return a new instance").isNotSameAs(q);
    assertSameFields(q, q0, "the argument after Quadruple.sqrt(q)");
    assertCorrectlyRounded(fromStatic, exact, 0, "Quadruple.sqrt(" + fieldsOf(q0) + ")");

    final Quadruple fromInstance = q.sqrt();
    assertThat(fromInstance).as("q.sqrt() must return this").isSameAs(q);
    assertCorrectlyRounded(fromInstance, exact, 0, "q.sqrt(), q = " + fieldsOf(q0));
  }

}
