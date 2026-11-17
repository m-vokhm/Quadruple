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
    return String.format("%s, exp=%s, mantHi=%s, mantLo=%s",
                         q.isNegative()? "-" : "+",
                         hex(q.exponent() & 0xFFFF_FFFFL, 8), hex(q.mantHi(), 16), hex(q.mantLo(), 16));
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

  public static boolean sameFields(Quadruple q1, Quadruple q2) {
    return q1.isNegative() == q2.isNegative()
        && q1.exponent()   == q2.exponent()
        && q1.mantHi()     == q2.mantHi()
        && q1.mantLo()     == q2.mantLo();
  }

  /** Hex with '_' separating groups of 4 digits, e.g. 0x7FFF_FFFF */
  private static String hex(long value, int digits) {
    final String s = String.format("%0" + digits + "X", value);
    final StringBuilder sb = new StringBuilder("0x");
    for (int i = 0; i < s.length(); i++) {
      if (i > 0 && i % 4 == 0) sb.append('_');
      sb.append(s.charAt(i));
    }
    return sb.toString();
  }

}
