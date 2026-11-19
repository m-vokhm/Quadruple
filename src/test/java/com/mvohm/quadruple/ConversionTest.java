package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testutil.BinaryFormat;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for conversions of Quadruple to other numeric types:
 * {@code intValue()}, {@code longValue()}, {@code doubleValue()}, {@code floatValue()}.
 * <p>
 * The expected results come from the model ({@link ExactValue}):
 * <ul>
 *   <li>{@code intValue()}, {@code longValue()} — the integer part (truncated toward zero),
 *       saturated to the range of the type, like the standard narrowing conversions of double;</li>
 *   <li>{@code doubleValue()}, {@code floatValue()} — the exact value correctly rounded to the nearest
 *       double or float, ties to even (as documented for {@code doubleValue()}, and as the standard
 *       conversions do), the same way for subnormal results; strictly, without any tolerance.</li>
 * </ul>
 * BigDecimal conversions are tested separately (as well as {@code assign(BigDecimal)}).
 */
public class ConversionTest {

  private static final String DATA = "com.mvohm.quadruple.testdata.ConversionData#";

  /* ************************************************************************
   * int, long
   */

  @ParameterizedTest
  @MethodSource(DATA + "valuesForIntegerConversion")
  void shouldConvertToLong(Quadruple q) {
    assertThat(q.longValue()).as("longValue() of " + fieldsOf(q))
      .isEqualTo(ExactValue.of(q).toLongTruncated());
  }

  @ParameterizedTest
  @MethodSource(DATA + "valuesForIntegerConversion")
  void shouldConvertToInt(Quadruple q) {
    assertThat(q.intValue()).as("intValue() of " + fieldsOf(q))
      .isEqualTo(ExactValue.of(q).toIntTruncated());
  }

  /* ************************************************************************
   * double, float
   */

  @ParameterizedTest
  @MethodSource(DATA + "valuesForDoubleConversion")
  void shouldConvertToDouble(Quadruple q) {
    assertCorrectlyRounded(q.doubleValue(), ExactValue.of(q), BinaryFormat.DOUBLE, 0, "doubleValue() of " + fieldsOf(q));
  }

  @ParameterizedTest
  @MethodSource(DATA + "valuesForFloatConversion")
  void shouldConvertToFloat(Quadruple q) {
    assertCorrectlyRounded(q.floatValue(), ExactValue.of(q), BinaryFormat.FLOAT, 0, "floatValue() of " + fieldsOf(q));
  }

  @Disabled("BUG-03: doubleValue() rounds ties half-up in the subnormal range of double, see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "subnormalDoubleTies")
  void shouldConvertSubnormalDoubleTiesToEven(Quadruple q) {
    shouldConvertToDouble(q);
  }

  @Disabled("BUG-04: floatValue() rounds the value twice, to double and then to float, see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "floatDoubleRoundingCases")
  void shouldConvertToFloatWithoutDoubleRounding(Quadruple q) {
    shouldConvertToFloat(q);
  }

  /* ************************************************************************
   * Special values
   */

  @Test
  void shouldConvertSpecialValues() {
    checkSpecial(Quadruple.nan(),              0,                 0,              Double.NaN,               Float.NaN);
    checkSpecial(Quadruple.positiveInfinity(), Integer.MAX_VALUE, Long.MAX_VALUE, Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    checkSpecial(Quadruple.negativeInfinity(), Integer.MIN_VALUE, Long.MIN_VALUE, Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    checkSpecial(new Quadruple(false, 0, 0, 0), 0,                0,              0.0,                      0.0f);
    checkSpecial(new Quadruple(true,  0, 0, 0), 0,                0,              -0.0,                     -0.0f);
  }

  private static void checkSpecial(Quadruple q, int i, long l, double d, float f) {
    final String context = fieldsOf(q);
    assertThat(q.intValue()).as("intValue() of " + context).isEqualTo(i);
    assertThat(q.longValue()).as("longValue() of " + context).isEqualTo(l);
    assertThat(Double.doubleToLongBits(q.doubleValue())).as("doubleValue() of " + context)
      .isEqualTo(Double.doubleToLongBits(d));                     // NaNs are equal, 0.0 and -0.0 are not
    assertThat(Float.floatToIntBits(q.floatValue())).as("floatValue() of " + context)
      .isEqualTo(Float.floatToIntBits(f));
  }

}
