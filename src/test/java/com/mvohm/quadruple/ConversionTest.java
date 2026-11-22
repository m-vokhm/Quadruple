package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.mvohm.quadruple.testdata.Ieee754Bits;
import com.mvohm.quadruple.testutil.BinaryFormat;
import com.mvohm.quadruple.testutil.ExactValue;

/**
 * Tests for conversions of Quadruple to other numeric types:
 * {@code intValue()}, {@code longValue()}, {@code doubleValue()}, {@code floatValue()},
 * {@code toIeee754Longs()}, {@code toIeee754Bytes()}.
 * <p>
 * The expected results come from the model ({@link ExactValue}):
 * <ul>
 *   <li>{@code intValue()}, {@code longValue()} — the integer part (truncated toward zero),
 *       saturated to the range of the type, like the standard narrowing conversions of double;</li>
 *   <li>{@code doubleValue()}, {@code floatValue()} — the exact value correctly rounded to the nearest
 *       double or float, ties to even (as documented for {@code doubleValue()}, and as the standard
 *       conversions do), the same way for subnormal results; strictly, without any tolerance;</li>
 *   <li>{@code toIeee754Longs()}, {@code toIeee754Bytes()} — the exact value correctly rounded to the nearest
 *       IEEE-754 quadruple-precision value, ties away from zero (half-up, as documented); strictly.</li>
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

  /* ************************************************************************
   * IEEE-754 quadruple precision
   */

  @ParameterizedTest
  @MethodSource(DATA + "valuesForIeee754Conversion")
  void shouldConvertToIeee754Longs(Quadruple q) {
    final long[] longs = q.toIeee754Longs();
    assertThat(longs).as("toIeee754Longs() of " + fieldsOf(q)).hasSize(2);
    assertCorrectlyRounded(new Ieee754Bits(longs[0], longs[1]), ExactValue.of(q), BinaryFormat.IEEE754_QUAD, 0,
                           "toIeee754Longs() of " + fieldsOf(q));
  }

  @ParameterizedTest
  @MethodSource(DATA + "valuesForIeee754Conversion")
  void shouldConvertToIeee754Bytes(Quadruple q) {
    final byte[] bytes = q.toIeee754Bytes();
    assertThat(bytes).as("toIeee754Bytes() of " + fieldsOf(q)).hasSize(16);
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);                     // big-endian
    assertCorrectlyRounded(new Ieee754Bits(buffer.getLong(), buffer.getLong()), ExactValue.of(q),
                           BinaryFormat.IEEE754_QUAD, 0, "toIeee754Bytes() of " + fieldsOf(q));
  }

  @Disabled("BUG-05: toIeee754Longs()/toIeee754Bytes() give 0 for values in [MIN_VALUE/2, MIN_VALUE) of IEEE-754,"
            + " see KNOWN_ISSUES.md")
  @ParameterizedTest
  @MethodSource(DATA + "valuesBelowIeee754MinValue")
  void shouldRoundValuesBelowIeee754MinValueToMinValue(Quadruple q) {
    shouldConvertToIeee754Longs(q);
    shouldConvertToIeee754Bytes(q);
  }

  @Test
  void shouldConvertSpecialValuesToIeee754() {
    checkSpecialIeee754(Quadruple.nan(),               new Ieee754Bits(0x7FFF_8000_0000_0000L, 0));
    checkSpecialIeee754(Quadruple.positiveInfinity(),  new Ieee754Bits(0x7FFF_0000_0000_0000L, 0));
    checkSpecialIeee754(Quadruple.negativeInfinity(),  new Ieee754Bits(0xFFFF_0000_0000_0000L, 0));
    checkSpecialIeee754(new Quadruple(false, 0, 0, 0), new Ieee754Bits(0, 0));
    checkSpecialIeee754(new Quadruple(true,  0, 0, 0), new Ieee754Bits(0x8000_0000_0000_0000L, 0));
  }

  private static void checkSpecialIeee754(Quadruple q, Ieee754Bits expected) {
    assertThat(q.toIeee754Longs()).as("toIeee754Longs() of " + fieldsOf(q)).containsExactly(expected.toLongs());
    assertThat(q.toIeee754Bytes()).as("toIeee754Bytes() of " + fieldsOf(q)).containsExactly(expected.toBytes());
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
