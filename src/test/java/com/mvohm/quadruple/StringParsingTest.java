package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testdata.AssignData;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for parsing strings: {@code new Quadruple(String)} and {@code assign(String)}
 * (on "dirty" instances, holding various values; must return this).
 * <p>
 * The reference for numeric strings is the model ({@link ExactValue#ofDecimal}): the result must be the exact value
 * correctly rounded to the nearest Quadruple value, ties away from zero (half-up), with the tolerance
 * described in the javadoc of {@link Quadruple#assign(String)}: if the exact value is below the midpoint
 * between two adjacent Quadruple values by less than {@link #EPSILON} = 1e-17 ulp, either of them is accepted;
 * the midpoint itself and the values above it must be rounded up. Values exceeding {@code MAX_VALUE} by a half
 * of its ulp or more give Infinity, values less than a half of {@code MIN_VALUE} give zero (keeping the sign).
 * <p>
 * The designations of special values and the strings with no valid value are tested separately.
 */
public class StringParsingTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.StringParsingData#";

  /** The tolerance below the midpoint, in ulps, as declared in the javadoc of {@link Quadruple#assign(String)} */
  static final double EPSILON = 1e-17;

  @ParameterizedTest(name = "{0}")
  @MethodSource(DATA + "numbers")
  void shouldParseNumbersWithDeclaredAccuracy(String s, ExactValue exact) {
    checkParsing(s, exact);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(DATA + "namedValues")
  void shouldParseDesignationsOfSpecialValues(String s, Quadruple expected) {
    checkParsing(s, expected);
  }

  @ParameterizedTest(name = "\"{0}\"")
  @MethodSource(DATA + "invalidStrings")
  void shouldRejectInvalidStrings(String s) {
    assertThatThrownBy(() -> new Quadruple(s)).as("new Quadruple(\"%s\")", s)
      .isInstanceOf(NumberFormatException.class);
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances())
      assertThatThrownBy(() -> dirty.getPayload().assign(s)).as("(%s).assign(\"%s\")", dirty.getName(), s)
        .isInstanceOf(NumberFormatException.class);
  }

  @Test
  void shouldRejectNull() {
    assertThatThrownBy(() -> new Quadruple((String)null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new Quadruple(1).assign((String)null)).isInstanceOf(NullPointerException.class);
  }

  @Disabled("BUG-20: values from 2^(2^31 + 1) (~3.52e646456993) up to 1e646456994 give wrong finite values"
            + " instead of Infinity, since the binary exponent wraps around, see KNOWN_ISSUES.md")
  @ParameterizedTest(name = "{0}")
  @MethodSource(DATA + "wrappedOverflows")
  void shouldGiveInfinityForValuesBelow1e646456994(String s, ExactValue exact) {
    checkParsing(s, exact);
  }

  @Disabled("BUG-18: a sign before a designation of a special value (except INFINITY) is not accepted,"
            + " e.g. \"-NaN\" (accepted by Double.parseDouble()) or \"-MAX_VALUE\", see KNOWN_ISSUES.md")
  @ParameterizedTest(name = "{0}")
  @MethodSource(DATA + "signedNames")
  void shouldAcceptSignBeforeDesignations(String s, Quadruple expected) {
    checkParsing(s, expected);
  }

  @Disabled("BUG-19: the suffixes d, D, f, F and hexadecimal literals, accepted by Double.parseDouble(),"
            + " are not accepted, see KNOWN_ISSUES.md")
  @ParameterizedTest(name = "{0}")
  @MethodSource(DATA + "doubleSyntax")
  void shouldAcceptSyntaxOfDouble(String s, ExactValue exact) {
    checkParsing(s, exact);
  }

  /* ************************************************************************
   * Helpers
   */

  private static void checkParsing(String s, ExactValue exact) {
    final ExactValue.Rounding<Quadruple> r = exact.round();
    final double epsilon = r.offsetFromMidpoint() < 0? EPSILON : 0;  // the midpoint and above must be rounded up
    assertCorrectlyRounded(new Quadruple(s), exact, epsilon, "new Quadruple(\"" + s + "\")");
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      assertThat(q.assign(s)).as("assign(String) must return this").isSameAs(q);
      assertCorrectlyRounded(q, exact, epsilon, "(" + dirty.getName() + ").assign(\"" + s + "\")");
    }
  }

  private static void checkParsing(String s, Quadruple expected) {
    assertSameFields(new Quadruple(s), expected, "new Quadruple(\"" + s + "\")");
    for (final Named<Quadruple> dirty : AssignData.dirtyInstances()) {
      final Quadruple q = dirty.getPayload();
      assertThat(q.assign(s)).as("assign(String) must return this").isSameAs(q);
      assertSameFields(q, expected, "(" + dirty.getName() + ").assign(\"" + s + "\")");
    }
  }

}
