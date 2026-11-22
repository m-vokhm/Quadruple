package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
import static com.mvohm.quadruple.Quadruple.*;
import static java.math.BigInteger.ONE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.DecimalMidpoints;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.ExactValue;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Data for {@code BigDecimalConversionTest}.
 * <p>
 * BigDecimal -> Quadruple: {BigDecimal, ExactValue}:
 * <ul>
 *   <li>the numeric data of {@link StringParsingData} (random values, near-midpoints, exact midpoints, extreme
 *       exponents) that are in the syntax of BigDecimal, with their exact values (computed from their components,
 *       not by BigDecimal);</li>
 *   <li>the same values with other representations: with trailing zeros of the unscaled value
 *       ({@code setScale(scale + n)}), and without them ({@code stripTrailingZeros()}, often with negative scales);</li>
 *   <li>scales near the limits of int, including those for which the decimal exponent of the value does not fit
 *       in int (values not less than {@code 10^(2^31)}, BUG-25).</li>
 * </ul>
 * Quadruple -> BigDecimal: finite values, including values equal or close to the midpoints between 100-digit decimals
 * ({@link DecimalMidpoints}), random values over the whole range and with moderate exponents, extreme values;
 * all of them with both signs.
 */
public class BigDecimalData {

  private static final int MIDPOINT_COUNT = scaled(1000);
  private static final int RANDOM_COUNT   = scaled(3000);
  private static final int DIGITS         = 100;

  /* ************************************************************************
   * BigDecimal -> Quadruple
   */

  /** BigDecimal values with their exact values (see the class description) */
  public static Stream<Arguments> decimals() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final List<Arguments> list = new ArrayList<>();
    StringParsingData.numbers().forEach(a -> {
      final String s = ((Named<?>)a.get()[0]).getPayload().toString();
      final ExactValue exact = (ExactValue)a.get()[1];
      final BigDecimal bd;
      try {
        bd = new BigDecimal(s);
      } catch (final NumberFormatException e) {                    // spaces, '_', exponents beyond int
        return;
      }
      list.add(decimal(bd, exact));
      if (random.nextInt(8) == 0)
        list.add(decimal(bd.setScale(bd.scale() + 1 + random.nextInt(20)), exact));  // the same with trailing zeros
      if (random.nextInt(8) == 0)
        list.add(decimal(bd.stripTrailingZeros(), exact));
    });
    edgeScales().forEach(list::add);
    return list.stream();
  }

  /** Scales near the limits of int, with unscaled values of various lengths, with and without trailing zeros */
  private static Stream<Arguments> edgeScales() {
    final List<Arguments> list = new ArrayList<>();
    final BigInteger[] unscaled = {ONE, BigInteger.valueOf(123), BigInteger.valueOf(1230), BigInteger.valueOf(-7),
                                   new BigInteger("12345678901234567890"), new BigInteger("-98765432109876543210000")};
    for (final BigInteger u : unscaled)
      for (int d = 0; d <= 30; d++) {
        final BigDecimal huge = new BigDecimal(u, Integer.MIN_VALUE + d), tiny = new BigDecimal(u, Integer.MAX_VALUE - d);
        list.add(decimal(huge, ExactValue.ofDecimal(u.signum() < 0, u.abs(), -(long)huge.scale())));
        list.add(decimal(tiny, ExactValue.ofDecimal(u.signum() < 0, u.abs(), -(long)tiny.scale())));
      }
    return list.stream();
  }

  /* ************************************************************************
   * Quadruple -> BigDecimal
   */

  /** Finite values for {@code bigDecimalValue()} (see the class description) */
  public static Stream<Named<Quadruple>> values() {
    final Random random = new Random(RandomGenerator.DEFAULT_RANDOM_SEED);
    final List<Quadruple> list = new ArrayList<>(List.of(
        new Quadruple(false, 0, 0, 0),
        new Quadruple(false, EXPONENT_OF_ONE, 0, 0),
        new Quadruple(false, EXPONENT_OF_ONE, 0x8000_0000_0000_0000L, 0),
        new Quadruple(false, EXPONENT_OF_ONE, 0, 1),
        new Quadruple(false, EXPONENT_OF_ONE - 1, -1L, -1L),
        new Quadruple(false, (int)EXPONENT_OF_MAX_VALUE, -1L, -1L),
        new Quadruple(false, 1, 0, 0),
        new Quadruple(false, 0, -1L, -1L),
        new Quadruple(false, 0, 0, 1),
        new Quadruple(false, EXPONENT_OF_ONE + 200, -1L, -1L),
        new Quadruple(false, EXPONENT_OF_ONE - 400, 0, 1)));
    list.addAll(DecimalMidpoints.exactMidpoints(random, DIGITS, MIDPOINT_COUNT));
    list.addAll(DecimalMidpoints.nearMidpoints(random, DIGITS, MIDPOINT_COUNT));
    new RandomGenerator().quadruples(RANDOM_COUNT).forEach(list::add);
    new RandomGenerator(RandomGenerator.DEFAULT_RANDOM_SEED + 1).quadruples(RANDOM_COUNT / 2, -1000, 1000)
      .forEach(list::add);
    return withBothSigns(list).map(q -> Named.of(QuadAssert.fieldsOf(q), q));
  }

  /** {Quadruple, format}: values with moderate exponents (so that "%f" gives reasonable strings), and special values */
  public static Stream<Arguments> formatCases() {
    final String[] formats = {"%s", "%.10f", "%25.15e", "%,.3f", "%g", "%.40g", "%-60.50e|", "%+.0f", "%.150f"};
    final List<Quadruple> values = new ArrayList<>(List.of(
        new Quadruple(false, 0, 0, 0), new Quadruple(true, 0, 0, 0),
        new Quadruple(false, EXPONENT_OF_ONE, 0x8000_0000_0000_0000L, 0),
        new Quadruple(false, EXPONENT_OF_ONE - 1, -1L, -1L)));
    new RandomGenerator(RandomGenerator.DEFAULT_RANDOM_SEED + 2).quadruples(200, -300, 300).forEach(values::add);
    final List<Arguments> list = new ArrayList<>();
    for (final Quadruple q : values)
      for (final String f : formats)
        list.add(Arguments.of(Named.of(QuadAssert.fieldsOf(q), q), f));
    for (final Quadruple q : new Quadruple[] {Quadruple.nan(), Quadruple.positiveInfinity(), Quadruple.negativeInfinity()})
      for (final String f : formats)
        list.add(Arguments.of(Named.of(q.toString(), q), f));
    return list.stream();
  }

  /* ************************************************************************
   * Helpers
   */

  private static Arguments decimal(BigDecimal bd, ExactValue exact) {
    final ExactValue value = bd.signum() == 0? ExactValue.of(0) : exact;  // BigDecimal has no -0
    final String s = bd.toString();
    final String name = s.length() <= 100? s : s.substring(0, 60) + "... (" + s.length() + " chars)";
    return Arguments.of(Named.of(name + " (scale " + bd.scale() + ")", bd), value);
  }

  private static Stream<Quadruple> withBothSigns(List<Quadruple> list) {
    return list.stream().flatMap(q -> Stream.of(q, new Quadruple(!q.isNegative(), q.exponent(), q.mantHi(), q.mantLo())));
  }

}
