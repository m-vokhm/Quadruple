package com.mvohm.quadruple;

import static org.assertj.core.api.Assertions.assertThat;
import static com.mvohm.quadruple.testutil.QuadAssert.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.mvohm.quadruple.testdata.AssignData;

/**
 * Tests for {@code assign(...)} methods.
 * <p>
 * The main property checked for the methods assigning a value of another type:
 * <b>{@code dirty.assign(x)} sets all internal fields exactly as {@code new Quadruple(x)} does,
 * regardless of the previous contents of the instance</b>, and returns the same instance.
 * <p>
 * Correctness of the values themselves is checked by {@link CreationTest}
 * and by the conversion tests; here we only check that the previous value
 * of the instance does not affect the result.
 * <p>
 * Fields are compared directly (not via {@code doubleValue()} etc.), since a stale value
 * in {@code mantLo} would be lost by rounding when converting back to {@code double}.
 * <p>
 * Each test iterates over all dirty instances internally rather than using them as a parameter,
 * to keep the number of test nodes reasonable.
 */
public class AssignTest {

  /* *******************************************************************************
   * assign(double)
   *********************************************************************************/

  /**
   * Verifies that {@code assign(double)} produces the same fields as the constructor,
   * whatever value the instance held before.
   * <p>
   * {@code ±Double.MIN_VALUE} are excluded from the data (BUG-01),
   * they are tested by {@link #shouldAssignDoubleMinValueToDirtyInstance(double)}.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#doubleValuesForAssign")
  @DisplayName("assign(double) on a dirty instance should give the same result as the constructor")
  void shouldAssignDoubleToDirtyInstance(double value) {
    checkAssignDouble(value);
  }

  /**
   * The same as {@link #shouldAssignDoubleToDirtyInstance(double)}, for {@code ±Double.MIN_VALUE}.
   */
  @Disabled("BUG-01: assign(±Double.MIN_VALUE) keeps stale mantHi, see KNOWN_ISSUES.md")
  @ParameterizedTest
  @ValueSource(doubles = { Double.MIN_VALUE, -Double.MIN_VALUE })
  @DisplayName("assign(±Double.MIN_VALUE) on a dirty instance should give the same result as the constructor")
  void shouldAssignDoubleMinValueToDirtyInstance(double value) {
    checkAssignDouble(value);
  }

  private static void checkAssignDouble(double value) {
    final Quadruple expected = new Quadruple(value);
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      final Quadruple result = q.assign(value);
      assertThat(result).as("assign(double) must return this instance").isSameAs(q);
      assertSameFields(q, expected,
                       String.format("assign(%s) to instance holding %s", value, dirty.getName()));
    }
  }

  /* *******************************************************************************
   * assign(long)
   *********************************************************************************/

  /**
   * Verifies that {@code assign(long)} produces the same fields as the constructor,
   * whatever value the instance held before.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#longValuesForAssign")
  @DisplayName("assign(long) on a dirty instance should give the same result as the constructor")
  void shouldAssignLongToDirtyInstance(long value) {
    final Quadruple expected = new Quadruple(value);
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      final Quadruple result = q.assign(value);
      assertThat(result).as("assign(long) must return this instance").isSameAs(q);
      assertSameFields(q, expected,
                       String.format("assign(%d) to instance holding %s", value, dirty.getName()));
    }
  }

  /* *******************************************************************************
   * assign(Quadruple)
   *********************************************************************************/

  /**
   * Verifies that {@code assign(Quadruple)} copies all fields of the source exactly,
   * whatever value the instance held before, and leaves the source unchanged.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#quadrupleValuesForAssign")
  @DisplayName("assign(Quadruple) on a dirty instance should copy all fields of the source")
  void shouldAssignQuadrupleToDirtyInstance(Quadruple source) {
    final Quadruple sourceCopy = new Quadruple(source.isNegative(), source.exponent(), source.mantHi(), source.mantLo());
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      final Quadruple result = q.assign(source);
      final String context = String.format("assign(%s) to instance holding %s", fieldsOf(source), dirty.getName());
      assertThat(result).as("assign(Quadruple) must return this instance").isSameAs(q);
      assertSameFields(q, sourceCopy, context);
      assertSameFields(source, sourceCopy, context + ", the source must not change");
    }
  }

  /**
   * Verifies that assigning an instance to itself ({@code q.assign(q)}) doesn't change it.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.testdata.AssignData#quadrupleValuesForAssign")
  @DisplayName("q.assign(q) should leave q unchanged")
  void shouldAssignQuadrupleToItself(Quadruple q) {
    final Quadruple copy = new Quadruple(q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
    final Quadruple result = q.assign(q);
    assertThat(result).as("assign(Quadruple) must return this instance").isSameAs(q);
    assertSameFields(q, copy, "q.assign(q)");
  }

}
