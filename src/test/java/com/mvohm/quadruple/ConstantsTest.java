package com.mvohm.quadruple;

import static com.mvohm.quadruple.Quadruple.*;
import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static java.math.BigInteger.ONE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for the constants: the static factory methods ({@code one()}, {@code pi()}, {@code maxValue()}, etc.)
 * and the public constants of the exponent ({@code EXPONENT_OF_ONE}, etc., and their deprecated synonyms).
 * <ul>
 *   <li>the values must be exactly as defined: the finite ones are compared with the model ({@link ExactValue}),
 *       {@code pi()} must be the value of π (computed by Machin's formula) correctly rounded;</li>
 *   <li>each call must return a new instance, so that modifying it does not affect the results of the following calls,
 *       nor the parsing of the designations of the constants (which uses internal constants);</li>
 *   <li>the constants of the exponent must match the exponents of the corresponding values.</li>
 * </ul>
 */
public class ConstantsTest {

  /** The binary exponent of MIN_VALUE (the LSB of subnormal values) */
  private static final long MIN_VALUE_EXPONENT = 1L - EXPONENT_BIAS - 128;

  static Stream<Arguments> finiteConstants() {
    return Stream.of(
        constant("one()",       Quadruple::one,       ExactValue.of(1)),
        constant("two()",       Quadruple::two,       ExactValue.of(2)),
        constant("ten()",       Quadruple::ten,       ExactValue.of(10)),
        constant("minValue()",  Quadruple::minValue,  ExactValue.of(false, ONE, MIN_VALUE_EXPONENT)),
        constant("minNormal()", Quadruple::minNormal, ExactValue.of(false, ONE, MIN_VALUE_EXPONENT + 128)),
        constant("maxValue()",  Quadruple::maxValue,                              // (2 - 2^-128) * 2^(2^31 - 1)
                 ExactValue.of(false, ONE.shiftLeft(129).subtract(ONE), (1L << 31) - 1 - 128)),
        constant("pi()",        Quadruple::pi,        piRoundedToQuadruple()));
  }

  static Stream<Arguments> allConstants() {
    return Stream.concat(
        finiteConstants().map(a -> Arguments.of(a.get()[0], a.get()[1])),
        Stream.of(Arguments.of("positiveInfinity()", (Supplier<Quadruple>)Quadruple::positiveInfinity),
                  Arguments.of("negativeInfinity()", (Supplier<Quadruple>)Quadruple::negativeInfinity),
                  Arguments.of("nan()",              (Supplier<Quadruple>)Quadruple::nan)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("finiteConstants")
  void finiteConstantsShouldHaveExactValues(String name, Supplier<Quadruple> factory, ExactValue expected) {
    assertCorrectlyRounded(factory.get(), expected, 0, name);   // exact for all but pi()
  }

  @Test
  void specialConstantsShouldHaveTheirValues() {
    assertSameFields(positiveInfinity(), new Quadruple(false, EXPONENT_OF_INFINITY, 0, 0), "positiveInfinity()");
    assertSameFields(negativeInfinity(), new Quadruple(true,  EXPONENT_OF_INFINITY, 0, 0), "negativeInfinity()");
    final Quadruple nan = nan();
    assertThat(nan.isNaN()).as("nan().isNaN()").isTrue();
    assertThat(nan.exponent()).as("nan().exponent()").isEqualTo(EXPONENT_OF_INFINITY);
    assertThat(nan.mantHi() | nan.mantLo()).as("the mantissa of nan()").isNotZero();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("allConstants")
  void eachCallShouldReturnNewInstance(String name, Supplier<Quadruple> factory) {
    final Quadruple first = factory.get();
    final Quadruple expected = new Quadruple(first);
    final Quadruple second = factory.get();
    assertThat(second).as(name + " must return a new instance").isNotSameAs(first);

    first.assign(-12.5);                                         // modifying the results must not affect anything
    second.negate();
    assertSameFields(factory.get(), expected, name + " after modifying the results of previous calls");
  }

  /** The designations of constants are parsed using internal instances, which must not be exposed */
  @Test
  void parsedConstantsShouldNotBeAffectedByModifyingResults() {
    for (final String name : new String[] {"MIN_VALUE", "MAX_VALUE", "MIN_NORMAL", "NaN", "Infinity", "-Infinity"}) {
      final Quadruple expected = new Quadruple(name);
      final Quadruple parsed = new Quadruple(name);
      parsed.assign(-12.5);
      new Quadruple(1).assign(name).negate();
      assertSameFields(new Quadruple(name), expected, "new Quadruple(\"" + name + "\") after modifying parsed values");
    }
    assertSameFields(new Quadruple("MAX_VALUE"), maxValue(), "new Quadruple(\"MAX_VALUE\")");
  }

  @Test
  @SuppressWarnings("deprecation")
  void exponentConstantsShouldMatchValues() {
    assertThat(EXPONENT_OF_SUBNORMAL).isEqualTo(minValue().exponent()).isEqualTo(0);
    assertThat(EXPONENT_OF_MIN_NORMAL).isEqualTo(minNormal().exponent()).isEqualTo(1);
    assertThat(EXPONENT_OF_ONE).isEqualTo(one().exponent()).isEqualTo(0x7FFF_FFFF);
    assertThat(EXPONENT_BIAS).isEqualTo(EXPONENT_OF_ONE);
    assertThat(EXPONENT_OF_MAX_VALUE).isEqualTo(maxValue().exponent() & 0xFFFF_FFFFL).isEqualTo(0xFFFF_FFFEL);
    assertThat(EXPONENT_OF_INFINITY).isEqualTo(positiveInfinity().exponent()).isEqualTo(nan().exponent())
                                    .isEqualTo(0xFFFF_FFFF);

    assertThat(EXP_SUB).as("deprecated EXP_SUB").isEqualTo(EXPONENT_OF_SUBNORMAL);
    assertThat(EXP_MIN).as("deprecated EXP_MIN").isEqualTo(EXPONENT_OF_MIN_NORMAL);
    assertThat(EXP_OF_ONE).as("deprecated EXP_OF_ONE").isEqualTo(EXPONENT_OF_ONE);
    assertThat(EXP_MAX).as("deprecated EXP_MAX").isEqualTo(EXPONENT_OF_MAX_VALUE);
    assertThat(EXP_INF).as("deprecated EXP_INF").isEqualTo(EXPONENT_OF_INFINITY);
  }

  /* ************************************************************************
   * Helpers
   */

  private static Arguments constant(String name, Supplier<Quadruple> factory, ExactValue expected) {
    return Arguments.of(name, factory, expected);
  }

  /**
   * @return π by Machin's formula {@code π = 16 arctan(1/5) - 4 arctan(1/239)}, with ~120 digits,
   * as an exact value of 400 bits (its error is far less than the distance from the midpoint, ~0.18 ulp)
   */
  private static ExactValue piRoundedToQuadruple() {
    final MathContext mc = new MathContext(130);
    final BigDecimal pi = arctanOfInverse(5, mc).multiply(BigDecimal.valueOf(16))
                          .subtract(arctanOfInverse(239, mc).multiply(BigDecimal.valueOf(4)));
    final BigInteger scaled = pi.multiply(new BigDecimal(ONE.shiftLeft(400))).toBigInteger();   // pi * 2^400
    return ExactValue.of(false, scaled, -400);
  }

  /** @return {@code arctan(1/x) = 1/x - 1/(3x^3) + 1/(5x^5) - ...} */
  private static BigDecimal arctanOfInverse(int x, MathContext mc) {
    final BigDecimal x2 = BigDecimal.valueOf((long)x * x);
    final BigDecimal limit = BigDecimal.ONE.movePointLeft(mc.getPrecision());
    BigDecimal power = BigDecimal.ONE.divide(BigDecimal.valueOf(x), mc), sum = BigDecimal.ZERO;
    for (int k = 0; power.compareTo(limit) > 0; k++) {
      final BigDecimal term = power.divide(BigDecimal.valueOf(2 * k + 1), mc);
      sum = k % 2 == 0? sum.add(term) : sum.subtract(term);
      power = power.divide(x2, mc);
    }
    return sum;
  }

}
