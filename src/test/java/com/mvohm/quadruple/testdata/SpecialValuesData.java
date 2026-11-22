package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
import static com.mvohm.quadruple.testdata.DataHelpers.abs;
import static com.mvohm.quadruple.testdata.DataHelpers.copy;
import static com.mvohm.quadruple.testdata.DataHelpers.named;
import static com.mvohm.quadruple.testdata.DataHelpers.negated;
import static com.mvohm.quadruple.Quadruple.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.BinaryOperation;
import com.mvohm.quadruple.testutil.CallForm;

/**
 * Data for {@code SpecialValuesTest}: special values (NaN, infinities, zeros) and finite values of different kinds,
 * as operands of arithmetic operations in all forms of the call, and as values to be classified.
 */
public class SpecialValuesData {

  /** The kind of a value, for the classification methods ({@code isNaN()} etc.) */
  public enum Kind { NAN, INFINITY, ZERO, FINITE }

  private static final int RANDOM_COUNT = scaled(100);
  private static final long HIGH_BIT = 0x8000_0000_0000_0000L;

  /** NaNs with different bit patterns, infinities and zeros */
  private static List<Named<Quadruple>> specialValues() {
    return List.of(
        Named.of("NaN",             nan()),
        q("NaN, mantLo = 1",        false, EXPONENT_OF_INFINITY, 0, 1),
        q("-NaN",                   true,  EXPONENT_OF_INFINITY, HIGH_BIT, 0),
        Named.of("+Infinity",       positiveInfinity()),
        Named.of("-Infinity",       negativeInfinity()),
        q("+0",                     false, 0, 0, 0),
        q("-0",                     true,  0, 0, 0));
  }

  /** Finite non-zero values of both signs: typical, extreme, representable as long and double or not, random */
  private static List<Named<Quadruple>> finiteValues() {
    final List<Named<Quadruple>> positive = new ArrayList<>(List.of(
        q("1",                      false, EXPONENT_OF_ONE, 0, 0),
        q("1.5",                    false, EXPONENT_OF_ONE, HIGH_BIT, 0),
        q("2^62",                   false, EXPONENT_OF_ONE + 62, 0, 0),
        q("1 + ulp",                false, EXPONENT_OF_ONE, 0, 1),
        q("MIN_VALUE",              false, 0, 0, 1),
        q("max subnormal",          false, 0, -1, -1),
        q("MIN_NORMAL",             false, 1, 0, 0),
        q("MAX_VALUE",              false, (int)EXPONENT_OF_MAX_VALUE, -1, -1)));
    new RandomGenerator().quadruples(3).forEach(v -> positive.add(named(abs(v))));

    final List<Named<Quadruple>> values = new ArrayList<>();
    for (final Named<Quadruple> v : positive) {
      values.add(v);
      values.add(Named.of("-(" + v.getName() + ")", negated(v.getPayload())));
    }
    return values;
  }

  private static List<Named<Quadruple>> allValues() {
    final List<Named<Quadruple>> values = new ArrayList<>(specialValues());
    values.addAll(finiteValues());
    return values;
  }

  /* ************************************************************************
   * Arithmetic
   */

  /**
   * {@code (operation, form, a, b)} for all binary operations and all forms of the call,
   * for all pairs of the values where at least one of the operands is a special value
   * (and the second operand is accepted by the form).
   */
  public static Stream<Arguments> operationsWithSpecialOperands() {
    final List<Named<Quadruple>> values = allValues();
    final List<Arguments> result = new ArrayList<>();
    for (final BinaryOperation op : BinaryOperation.values())
      for (final CallForm form : CallForm.values())
        for (final Named<Quadruple> a : values)
          for (final Named<Quadruple> b : values)
            if ((isSpecial(a.getPayload()) || isSpecial(b.getPayload())) && form.accepts(b.getPayload()))
              result.add(Arguments.of(op, form, copy(a), copy(b)));
    return result.stream();
  }

  /**
   * {@code (operation, form, a, b)} whose exact result is zero: {@code x + (-x)} and {@code x - x}
   * for the finite values and random values of both signs, in all forms of the call accepting {@code b}.
   */
  public static Stream<Arguments> cancellations() {
    final List<Named<Quadruple>> values = finiteValues();
    new RandomGenerator().quadruples(RANDOM_COUNT).forEach(v -> values.add(named(v)));
    final List<Arguments> result = new ArrayList<>();
    for (final CallForm form : CallForm.values())
      for (final Named<Quadruple> x : values) {
        final Named<Quadruple> minusX = Named.of("-(" + x.getName() + ")", negated(x.getPayload()));
        if (form.accepts(minusX.getPayload()))
          result.add(Arguments.of(BinaryOperation.ADD, form, copy(x), minusX));
        if (form.accepts(x.getPayload()))
          result.add(Arguments.of(BinaryOperation.SUBTRACT, form, copy(x), copy(x)));
      }
    return result.stream();
  }

  /** Values whose square root is a special value: special values (including -0) and negative values */
  public static Stream<Named<Quadruple>> valuesForSqrt() {
    return allValues().stream()
        .filter(v -> isSpecial(v.getPayload()) || v.getPayload().isNegative())
        .map(DataHelpers::copy);
  }

  /* ************************************************************************
   * Classification
   */

  /** {@code (value, kind, negative)} for all the values and random finite values */
  public static Stream<Arguments> valuesForClassification() {
    final List<Named<Quadruple>> values = allValues();
    new RandomGenerator().quadruples(RANDOM_COUNT).forEach(v -> values.add(named(v)));
    final List<Arguments> result = new ArrayList<>();
    for (final Named<Quadruple> v : values) {
      final Quadruple q = v.getPayload();
      result.add(Arguments.of(v, kindOf(q), q.isNegative()));
    }
    return result.stream();
  }

  /** The kind by the fields, independently of the tested methods */
  private static Kind kindOf(Quadruple q) {
    if (q.exponent() == EXPONENT_OF_INFINITY)
      return (q.mantHi() | q.mantLo()) == 0? Kind.INFINITY : Kind.NAN;
    return (q.exponent() | q.mantHi() | q.mantLo()) == 0? Kind.ZERO : Kind.FINITE;
  }

  /* ************************************************************************
   * Helpers
   */

  private static boolean isSpecial(Quadruple q) {
    return kindOf(q) != Kind.FINITE;
  }

  private static Named<Quadruple> q(String name, boolean negative, int exponent, long mantHi, long mantLo) {
    return Named.of(name, new Quadruple(negative, exponent, mantHi, mantLo));
  }

}
