package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testdata.AssignData;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for conversions between Quadruple and BigDecimal: {@code new Quadruple(BigDecimal)},
 * {@code assign(BigDecimal)} (on "dirty" instances; must return this), {@code bigDecimalValue()},
 * and {@code format(String)}, which formats the value of {@code bigDecimalValue()}.
 * <p>
 * <b>BigDecimal -> Quadruple.</b> The reference is the model ({@link ExactValue#ofDecimal}): the result must be
 * the exact value correctly rounded half-up, with the tolerance declared in the javadoc of
 * {@link Quadruple#assign(BigDecimal)}: if the exact value is below the midpoint by less than {@link #EPSILON} = 1e-40
 * ulp, either of the two adjacent values is accepted; the midpoint and above must be rounded up.
 * Zero (BigDecimal has no -0) must give +0.
 * <p>
 * <b>Quadruple -> BigDecimal.</b> {@code bigDecimalValue()} must give the exact value rounded to 100 significant
 * digits, {@code HALF_EVEN} (as in its javadoc), without trailing zeros. If the exact value is closer to the midpoint
 * between two adjacent 100-digit decimals than {@link #DIGITS_EPSILON} = 1e-20 of the unit of the last digit,
 * either of them is accepted (the library computes with ~120 digits), but exact midpoints must be rounded to even.
 * The reference is the exact value as BigDecimal for moderate exponents (where exact decimal midpoints are possible),
 * and 200 truncated digits for the others (enough to tell the side of the midpoint, since the midpoint is a multiple
 * of the unit of the 200th digit, and exact midpoints are impossible there). The result must be read back exactly. NaN and infinities
 * must cause {@code NumberFormatException}; zeros of both signs give {@code BigDecimal.ZERO}.
 */
public class BigDecimalConversionTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.BigDecimalData#";

  /** The tolerance below the midpoint, in ulps, as declared in the javadoc of {@link Quadruple#assign(BigDecimal)} */
  static final double EPSILON = 1e-40;

  /**
   * The tolerance of {@code bigDecimalValue()} near the midpoint between two 100-digit decimals, in units
   * of the 100th digit (the measured max error is ~3.7e-21); exact midpoints must be rounded HALF_EVEN strictly
   */
  static final double DIGITS_EPSILON = 1e-20;

  private static final MathContext MC_100 = new MathContext(100, RoundingMode.HALF_EVEN);
  private static final BigInteger HALF_UNIT = BigInteger.TEN.pow(100).shiftRight(1);
  /** The max magnitude of the unbiased exponent for which the reference is computed exactly */
  private static final long EXACT_EXPONENT_LIMIT = 2000;

  /* ************************************************************************
   * BigDecimal -> Quadruple
   */

  @ParameterizedTest(name = "{0}")
  @MethodSource(DATA + "decimals")
  void shouldConvertBigDecimalWithDeclaredAccuracy(BigDecimal bd, ExactValue exact) {
    checkConversion(bd, exact);
  }

  @Test
  void shouldRejectNullBigDecimal() {
    assertThatThrownBy(() -> new Quadruple((BigDecimal)null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new Quadruple(1).assign((BigDecimal)null)).isInstanceOf(NullPointerException.class);
  }

  /* ************************************************************************
   * Quadruple -> BigDecimal
   */

  @ParameterizedTest
  @MethodSource(DATA + "values")
  void bigDecimalValueShouldGive100CorrectlyRoundedDigits(Quadruple q) {
    final Quadruple q0 = new Quadruple(q);
    final BigDecimal actual = q.bigDecimalValue();
    assertSameFields(q, q0, "the value after bigDecimalValue()");
    assertThat(actual.precision()).as("%s.bigDecimalValue().precision()", fieldsOf(q0)).isLessThanOrEqualTo(100);
    if (q.isZero()) {
      assertThat(actual).isEqualByComparingTo(BigDecimal.ZERO);
      return;
    }
    final Reference r = reference(q0);
    if (!r.exactMidpoint() && Math.abs(r.offset()) < DIGITS_EPSILON)
      assertThat(actual).as("%s.bigDecimalValue(): the value is at %s of the unit of the last digit from the midpoint",
                            fieldsOf(q0), r.offset())
        .matches(a -> a.compareTo(r.down()) == 0 || a.compareTo(r.up()) == 0, "is " + r.down() + " or " + r.up());
    else
      assertThat(actual).as("%s.bigDecimalValue(), offset from the midpoint %s", fieldsOf(q0), r.offset())
        .isEqualByComparingTo(r.rounded());
  }

  @ParameterizedTest
  @MethodSource(DATA + "values")
  void bigDecimalValueShouldBeReadBackExactly(Quadruple q) {
    final Quadruple expected = q.isZero()? new Quadruple(false, 0, 0, 0) : q;  // BigDecimal has no -0
    assertSameFields(new Quadruple(q.bigDecimalValue()), expected, "new Quadruple(" + fieldsOf(q) + ".bigDecimalValue())");
  }

  @Test
  void bigDecimalValueOfSpecialValues() {
    assertThat(new Quadruple(false, 0, 0, 0).bigDecimalValue()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(new Quadruple(true, 0, 0, 0).bigDecimalValue()).isEqualByComparingTo(BigDecimal.ZERO);
    for (final Quadruple q : new Quadruple[] {Quadruple.nan(), Quadruple.positiveInfinity(), Quadruple.negativeInfinity()})
      assertThatThrownBy(q::bigDecimalValue).as(q.toString()).isInstanceOf(NumberFormatException.class);
  }

  /* ************************************************************************
   * format(String)
   */

  @ParameterizedTest(name = "{0}: \"{1}\"")
  @MethodSource(DATA + "formatCases")
  void formatShouldFormatTheValueAsBigDecimal(Quadruple q, String format) {
    final String expected = q.isNaN() || q.isInfinite()? q.toString()
                          : String.format(format, q.isZero()? BigDecimal.ZERO : reference(q).rounded().stripTrailingZeros());
    assertThat(q.format(format)).as("%s.format(\"%s\")", fieldsOf(q), format).isEqualTo(expected);
  }

  /* ************************************************************************
   * Helpers
   */

  private static void checkConversion(BigDecimal bd, ExactValue exact) {
    final ExactValue.Rounding<Quadruple> r = exact.round();
    final double epsilon = r.offsetFromMidpoint() < 0? EPSILON : 0;  // the midpoint and above must be rounded up
    assertCorrectlyRounded(new Quadruple(bd), exact, epsilon, "new Quadruple(" + bd + ")");
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      assertThat(q.assign(bd)).as("assign(BigDecimal) must return this").isSameAs(q);
      assertCorrectlyRounded(q, exact, epsilon, "(" + dirty.getName() + ").assign(" + bd + ")");
    }
  }

  /**
   * The reference for {@code bigDecimalValue()}: the exact value rounded to 100 digits ({@code rounded}),
   * the two adjacent 100-digit decimals ({@code down}, {@code up}, by magnitude), the distance from the midpoint
   * between them in units of the 100th digit ({@code offset}, from 200 truncated digits), and whether the value
   * is exactly the midpoint
   */
  private record Reference(BigDecimal rounded, BigDecimal down, BigDecimal up, double offset, boolean exactMidpoint) {}

  private static Reference reference(Quadruple q) {
    final ExactValue exact = ExactValue.of(q);
    final BigDecimal d = exact.toBigDecimal(200);                     // truncated
    final BigInteger[] headAndTail = d.unscaledValue().abs().divideAndRemainder(BigInteger.TEN.pow(100));
    final BigInteger sign = BigInteger.valueOf(d.signum());
    final BigDecimal down = new BigDecimal(headAndTail[0].multiply(sign), d.scale() - 100);
    final BigDecimal up = new BigDecimal(headAndTail[0].add(BigInteger.ONE).multiply(sign), d.scale() - 100);
    final double offset = new BigDecimal(headAndTail[1].subtract(HALF_UNIT)).movePointLeft(100).doubleValue();
    if (Math.abs(exact.binaryExponent()) <= EXACT_EXPONENT_LIMIT) {    // exact midpoints are possible
      final BigDecimal x = exact.toBigDecimal();
      final boolean midpoint = x.abs().subtract(down.abs()).multiply(BigDecimal.valueOf(2))
                                .compareTo(up.abs().subtract(down.abs())) == 0;
      return new Reference(x.round(MC_100), down, up, offset, midpoint);
    }
    // No exact midpoints here; the 200 truncated digits tell the side of the midpoint
    return new Reference(offset >= 0? up : down, down, up, offset, false);
  }

}
