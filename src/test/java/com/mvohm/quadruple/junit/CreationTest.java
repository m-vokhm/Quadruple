package com.mvohm.quadruple.junit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.Quadruple;

public class CreationTest {

  /**
   * Verifies that the default constructor creates a Quadruple
   * representing the numeric value 0.
   */
  @Test
  @DisplayName("Default constructor should create zero")
  void shouldCreateZeroByDefault() {
    final Quadruple q = new Quadruple();
    assertThat(q.toString())
      .withFailMessage("Default constructor must produce zero value")
      .isEqualTo("0.0");
  }

  /**
   * Verifies that the copy constructor creates a new instance
   * with the same value as the source Quadruple, without sharing identity.
   */
  @Test
  @DisplayName("Copy constructor should create independent exact copy")
  void shouldCopyValueExactly() {

    final Quadruple[] samples = {
        new Quadruple(), new Quadruple(1), new Quadruple(-1),
        new Quadruple(123.456), new Quadruple(-0.0), new Quadruple(Quadruple.maxValue()) };

    for (final Quadruple original : samples) {
      final Quadruple copy = new Quadruple(original);
      assertThat(copy)
        .withFailMessage("Copy constructor must preserve value")
        .isEqualTo(original);
      assertThat(copy)
        .withFailMessage("Copy must be a different instance")
        .isNotSameAs(original);
    }
  }

  /**
   * Verifies that the constructor taking a double value preserves
   * the exact IEEE-754 binary representation of the source value.
   * <p>
   * The value restored by doubleValue() must exactly match the value
   * passed to the constructor, including special values and signed zero.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.junit.data.CreationData#doubleValuesForCreation")
  @DisplayName("Constructor taking double should preserve exact value")
  void shouldCreateExactValueFromDouble(double value) {
    final Quadruple q = new Quadruple(value);
    if (Double.isNaN(value)) {
      // TODO 2026-05-30 13:08:11 In Quadruple, add static methods isNaN(Quadruple q), like in Double
      assertThat(q.isNaN())
        .withFailMessage("Quadruple created from NaN must be NaN")
        .isTrue();
    } else {
      assertThat(q.doubleValue())
        .withFailMessage("Value restored from Quadruple (%s) differs from source double %s", q.doubleValue(), value)
        .isEqualTo(value);
    }
  }

  /**
   * Verifies that the constructor taking a long value preserves
   * the exact numeric value of the source operand.
   */
  @ParameterizedTest
  @MethodSource("com.mvohm.quadruple.junit.data.CreationData#longValuesForCreation")
  @DisplayName("Constructor taking long should preserve exact value")
  void shouldCreateExactValueFromLong(long value) {
    final Quadruple q = new Quadruple(value);
    assertThat(q.longValue())
      .withFailMessage("Value restored from Quadruple (%d) differs from source long %d", q.longValue(), value)
      .isEqualTo(value);
  }

}