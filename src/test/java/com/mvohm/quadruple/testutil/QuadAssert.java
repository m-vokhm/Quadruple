package com.mvohm.quadruple.testutil;

import static org.assertj.core.api.Assertions.assertThat;

import com.mvohm.quadruple.Quadruple;

/**
 * Assertions and helpers for comparing Quadruple instances at the level of their internal fields.
 */
public class QuadAssert {

  /**
   * Returns a string with all internal fields of the given Quadruple in hex,
   * like {@code "-, exp=0x7FFF_FFFF, mantHi=0x8000_0000_0000_0000, mantLo=0x0000_0000_0000_0000"}.
   * Unlike {@code toString()}, it shows the exact contents of the instance and is cheap
   * for any exponent.
   */
  public static String fieldsOf(Quadruple q) {
    return formatFields(q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
  }

  /**
   * Formats the given low-level parts of a Quadruple value the same way as {@link #fieldsOf(Quadruple)}.
   */
  public static String formatFields(boolean negative, int exponent, long mantHi, long mantLo) {
    return String.format("%s, exp=%s, mantHi=%s, mantLo=%s",
                         negative? "-" : "+",
                         hex(exponent & 0xFFFF_FFFFL, 8), hex(mantHi, 16), hex(mantLo, 16));
  }

  /**
   * Asserts that all internal fields ({@code negative}, {@code exponent}, {@code mantHi}, {@code mantLo})
   * of {@code actual} are equal to those of {@code expected}.
   *
   * @param context description of the checked case, included in the failure message
   */
  public static void assertSameFields(Quadruple actual, Quadruple expected, String context) {
    assertThat(sameFields(actual, expected))
      .withFailMessage("%s:%n  expected: %s%n  actual:   %s", context, fieldsOf(expected), fieldsOf(actual))
      .isTrue();
  }

  /**
   * Asserts that {@code actual} is the correctly rounded value of {@code exact}
   * (to the nearest, ties away from zero), or, if {@code exact} is closer to the midpoint
   * between two adjacent Quadruple values than {@code epsilon} ulp, either of them.
   *
   * @param epsilon the allowed uncertainty near the midpoint, in ulps; 0 means strict correct rounding
   * @param context description of the checked case, included in the failure message
   */
  public static void assertCorrectlyRounded(Quadruple actual, ExactValue exact, double epsilon, String context) {
    assertCorrectlyRounded(actual, exact, BinaryFormat.QUADRUPLE, epsilon, context);
  }

  /**
   * Asserts that {@code actual} is the value of {@code exact} correctly rounded to the given format,
   * or, if {@code exact} is closer to the midpoint between two adjacent values of the format
   * than {@code epsilon} ulp, either of them.
   *
   * @param epsilon the allowed uncertainty near the midpoint, in ulps; 0 means strict correct rounding
   * @param context description of the checked case, included in the failure message
   */
  public static <T> void assertCorrectlyRounded(T actual, ExactValue exact, BinaryFormat<T> format,
                                                double epsilon, String context) {
    final ExactValue.Rounding<T> r = exact.round(format);
    assertThat(r.accepts(actual, epsilon))
      .withFailMessage("%s (%s):%n  exact:    %s%n  expected: %s%n  actual:   %s%n"
                       + "  offset from midpoint: %s ulp, epsilon: %s ulp",
                       context, format, exact, describe(r.rounded()), describe(actual), r.offsetFromMidpoint(), epsilon)
      .isTrue();
  }

  /**
   * @return a description of a value showing its exact contents:
   * the fields of a Quadruple, a double or float with its bits in hex, or {@code toString()} for other types
   */
  public static String describe(Object value) {
    if (value instanceof Quadruple q) return fieldsOf(q);
    if (value instanceof Double d)    return d + " (" + hex(Double.doubleToRawLongBits(d), 16) + ")";
    if (value instanceof Float f)     return f + " (" + hex(Float.floatToRawIntBits(f) & 0xFFFF_FFFFL, 8) + ")";
    return String.valueOf(value);
  }

  public static boolean sameFields(Quadruple q1, Quadruple q2) {
    return q1.isNegative() == q2.isNegative()
        && q1.exponent()   == q2.exponent()
        && q1.mantHi()     == q2.mantHi()
        && q1.mantLo()     == q2.mantLo();
  }

  /**
   * Returns the given value as a hex string of the given number of digits,
   * with '_' separating groups of 4 digits, e.g. {@code hex(0x7FFF_FFFF, 8)} returns "0x7FFF_FFFF".
   */
  public static String hex(long value, int digits) {
    final String s = String.format("%0" + digits + "X", value);
    final StringBuilder sb = new StringBuilder("0x");
    for (int i = 0; i < s.length(); i++) {
      if (i > 0 && i % 4 == 0) sb.append('_');
      sb.append(s.charAt(i));
    }
    return sb.toString();
  }

}
