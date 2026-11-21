package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for {@code toString()} and {@code toHexString()}.
 * <p>
 * {@code toString()} of a finite non-zero value must give its 40 significant decimal digits, rounded half-up,
 * in the form {@code [-]d.ddd...ddde±XX} (39 digits after the point, the exponent of at least 2 digits with the sign).
 * The reference is the exact value ({@link ExactValue#toBigDecimal(int)}, 80 digits): since the midpoint between
 * two adjacent 40-digit decimals is a multiple of the unit of the 80th digit, the 80 truncated digits are enough
 * to tell whether the value is below the midpoint, or not.
 * If the exact value is closer to the midpoint than {@link #EPSILON} = 1e-17 of the unit of the 40th digit
 * (including exact midpoints), either of the two decimals is accepted: the library computes the digits
 * with a finite precision, and errs within ~3e-18 of the unit on both sides of the midpoint.
 * <p>
 * The string must be read back exactly: {@code new Quadruple(q.toString())} must be equal to {@code q}
 * (40 digits are enough to identify a value of 129 significant bits).
 * <p>
 * {@code toHexString()} must give the fields as they are, in the form of its javadoc:
 * {@code ±hhhh_hhhh_hhhh_hhhh hhhh_hhhh_hhhh_hhhh ehhhh_hhhh} (mantHi, mantLo, exponent, lower-case hex digits),
 * and {@code Quadruple(String)} must read it back (BUG-22: both are not so yet).
 */
public class ToStringTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.ToStringData#";

  /** The tolerance near the midpoint, in units of the 40th digit */
  static final double EPSILON = 1e-17;

  private static final int DIGITS = 40;
  private static final BigInteger HALF_UNIT = BigInteger.TEN.pow(DIGITS).shiftRight(1);   // 5 * 10^39

  @ParameterizedTest
  @MethodSource(DATA + "values")
  void shouldGive40CorrectlyRoundedDigits(Quadruple q) {
    final Quadruple q0 = new Quadruple(q);
    final String actual = q.toString();
    assertSameFields(q, q0, "the value after toString()");
    if (q.isZero()) return;                                      // tested by shouldGiveSpecialStrings

    final BigDecimal d = ExactValue.of(q).toBigDecimal(2 * DIGITS).abs();
    final BigInteger[] headAndTail = d.unscaledValue().divideAndRemainder(BigInteger.TEN.pow(DIGITS));
    final long exp10 = d.precision() - d.scale() - 1;            // the exponent of the leading digit
    final BigInteger tail = headAndTail[1];
    final String down = format(q.isNegative(), headAndTail[0], exp10);
    final String up = format(q.isNegative(), headAndTail[0].add(BigInteger.ONE), exp10);
    final double offset = new BigDecimal(tail.subtract(HALF_UNIT)).movePointLeft(DIGITS).doubleValue();
    final String expected = offset >= 0? up : down;

    if (Math.abs(offset) < EPSILON)
      assertThat(actual).as("%s: the value is at %s of the unit of the last digit from the midpoint", fieldsOf(q0), offset)
        .isIn(down, up);
    else
      assertThat(actual).as("%s: offset from the midpoint %s", fieldsOf(q0), offset).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource(DATA + "values")
  void shouldBeReadBackExactly(Quadruple q) {
    final String s = q.toString();
    assertSameFields(new Quadruple(s), q, "new Quadruple(\"" + s + "\")");
  }

  @ParameterizedTest(name = "{0} -> \"{1}\"")
  @MethodSource(DATA + "specialValues")
  void shouldGiveSpecialStrings(Quadruple q, String expected) {
    assertThat(q.toString()).as(fieldsOf(q)).isEqualTo(expected);
  }

  @Disabled("BUG-22: toHexString() puts a space after 'e', unlike its javadoc, see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "valuesForHexString")
  void shouldGiveFieldsInHexString(Quadruple q) {
    final String expected = (q.isNegative()? "-" : "+") + hexGroups(q.mantHi(), 16) + " " + hexGroups(q.mantLo(), 16)
                            + " e" + hexGroups(q.exponent() & 0xFFFF_FFFFL, 8);
    assertThat(q.toHexString()).as(fieldsOf(q)).isEqualTo(expected);
  }

  @Disabled("BUG-22: the output of toHexString() can't be parsed by Quadruple(String), see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "valuesForHexString")
  void shouldBeReadBackFromHexString(Quadruple q) {
    final String s = q.toHexString();
    final Quadruple actual = new Quadruple(s);
    if (q.isNaN())
      assertThat(actual.isNaN()).as("new Quadruple(\"" + s + "\") must be NaN").isTrue();
    else
      assertSameFields(actual, q, "new Quadruple(\"" + s + "\")");
  }

  /* ************************************************************************
   * Helpers
   */

  /**
   * @return {@code [-]d.ddd...ddde±XX} for the 40-digit integer {@code digits} (or {@code 10^40}, after a carry),
   * whose leading digit has the exponent {@code exp10}
   */
  private static String format(boolean negative, BigInteger digits, long exp10) {
    String s = digits.toString();
    if (s.length() > DIGITS) {                                   // 99..9 + 1 = 10^40: 1.00..0e(exp10 + 1)
      s = s.substring(0, DIGITS);
      exp10++;
    }
    return (negative? "-" : "") + s.charAt(0) + "." + s.substring(1) + "e" + (exp10 < 0? "-" : "+")
           + (Math.abs(exp10) < 10? "0" : "") + Math.abs(exp10);
  }

  /** @return the lower {@code digits} hex digits of {@code value}, in groups of 4 separated by '_' */
  private static String hexGroups(long value, int digits) {
    final String hex = String.format("%016x", value).substring(16 - digits);
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < digits; i += 4)
      sb.append(i == 0? "" : "_").append(hex, i, i + 4);
    return sb.toString();
  }

}
