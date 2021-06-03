package com.mvohm.quadruple.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.TesterClasses.Conversion_Q2T_Tester;
import com.mvohm.quadruple.test.TesterClasses.Conversion_T2Q_Tester;

import static com.mvohm.quadruple.test.AuxMethods.*;

/**
 * Contains concrete descendants of the abstract tester classes defined in {@link TesterClasses}
 * intended to test conversions from {@code Quadruple} to standard IEEE-754 quadruple-precision numbers
 * and vice versa.
 * The general logic of these classes is described in more detail in the descriptions
 * of their respective ancestors such as {@link Conversion_Q2T_Tester},
 * {@link Conversion_T2Q_Tester} and others, nested in {@link TesterClasses}.
 * @author M.Vokhmentev
 */
public class IeeeConversionTesterClasses {

  private static final int  IEEE754_EXP_BIAS              = 0x3FFF;
  private static final int  IEEE754_EXP_OF_INFINITY       = 0x7FFF;
  private static final int  MAX_IEEE754_EXPONENT          = Quadruple.EXPONENT_BIAS + 16383;
  private static final int  MIN_IEEE754_EXPONENT          = Quadruple.EXPONENT_BIAS - 16384 - 112;
  private static final long LOWER_48_BITS                 = 0x0000_FFFF_FFFF_FFFFL;
  private static final long HIGHER_48_BITS                = 0xFFFF_FFFF_FFFF_0000L;
  private static final long ALL_64_BITS                   = 0xFFFF_FFFF_FFFF_FFFFL;
  private static final long IEEE754_EXPONENT_MASK         = 0x7FFF_0000_0000_0000L;

  private static final long IEEE754_SIGN_MASK             = 0x8000_0000_0000_0000L;
  private static final long IEEE754_MINUS_ZERO_LEAD       = 0x8000_0000_0000_0000L;
  private static final long IEEE754_NAN_LEAD              = 0x7FFF_8000_0000_0000L;
  private static final long IEEE754_MINUS_INFINITY_LEAD   = 0xFFFF_0000_0000_0000L;
  private static final long IEEE754_INFINITY_LEAD         = 0x7FFF_0000_0000_0000L;



  private static final Quadruple MAX_IEEE754_QUADRUPLE = new Quadruple(MAX_IEEE754_EXPONENT, -1L, -32769L);
  private static final Quadruple MIN_IEEE754_QUADRUPLE = new Quadruple(MIN_IEEE754_EXPONENT, -1L, -32769L);

  /**
   * A test data set for testing conversions from IEEE-754 quadruple value to Quadruple and vice versa.
   * Provides full coverage of {@link Quadruple#assignIeee754(long[])}, {@link Quadruple#assignIeee754(byte[])},
   * {@link Quadruple#toIeee754Longs()}, and {@link Quadruple#toIeee754Bytes()}
   */
  static String[][] q2ieeeTestData = new String[][] {
  //  {"Фигня", null},

    {"0", null},
    {"-0", null},
    {"1", null},
    {"-1", null},
    {"2", null},
    {"3", null},

    {"NaN", null},
    {"Infinity", null},
    {"-Infinity", null},

    {"1", null},
    {"2", null},


    {"// Larger than the largest normal number, should return Infinity", null},
    {"2e4933", null},
    {"-2e4933", null}, // Just to cover the code

    {"// A little larger than the largest normal number", null},
    {"1.1897314953572317650857593266280070162e4932", null},

    {"// To be rounded up to infinity", null},
    {bdStr(ALL_64_BITS, HIGHER_48_BITS >> 1, Quadruple.EXPONENT_BIAS + IEEE754_EXP_BIAS), null},
    {"-"+bdStr(ALL_64_BITS, HIGHER_48_BITS >> 1, Quadruple.EXPONENT_BIAS + IEEE754_EXP_BIAS), null},

    {"// Not to be rounded up to infinity", null},
    {bdStr(ALL_64_BITS >>> 1, HIGHER_48_BITS >> 1, Quadruple.EXPONENT_BIAS + IEEE754_EXP_BIAS), null},
    {"-"+bdStr(ALL_64_BITS >>> 1, HIGHER_48_BITS >> 1, Quadruple.EXPONENT_BIAS + IEEE754_EXP_BIAS), null},

    {"// To be rounded down to MAX_VALUE", null},
    {bdStr(ALL_64_BITS, HIGHER_48_BITS | 0x7FFF, Quadruple.EXPONENT_BIAS + IEEE754_EXP_BIAS), null},

    {"// largest normal number", null},
    {bdStr(ALL_64_BITS, HIGHER_48_BITS, Quadruple.EXPONENT_BIAS + IEEE754_EXP_BIAS), null},

    {"// A little less than the largest normal number", null},
    {mult(sub(2, powerOfTwo(-113)), powerOfTwo(16382)).toString(), null},

    {"// largest number less than one", null},
    {"0.9999999999999999999999999999999999037", null},

    {"1", null},

    {"// smallest number larger than one", null},
    {"1.0000000000000000000000000000000001926", null},

    {"// smallest positive normal number", null},
    {"3.3621031431120935062626778173217526026e-4932", null},

    {"// To be rounded up to MIN_NORMAL", null},
    {bdStr(-1, -1, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS), null},

    //  Subnormals
    {"// largest subnormal number", null},
    {bdStr(-1L, 0xFFFF_FFFF_FFFE_0000L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS), null},

    {"// Little less than half of the largest subnormal", null},
    {bdStr(-1L, 0xFFFF_FFFF_FFFC_0000L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 1), null},

    {"// Round up to half of the largest subnormal", null},
    {bdStr(-1L, 0xFFFF_FFFF_FFFF_0000L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 1), null},

    {"// Min normal / 2^16", null},
    {bdStr(0, 0, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 16), null},

    {"// A little more than min normal / 2^16", null},
    {bdStr(0x2345_6789_abcd_ef01L, 0x1234_5678_9abc_def0L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 16), null},

    {"// 64 bits of significand", null},
    {bdStr(-1L, 0L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 47), null},

    {"// 64 bits of significand", null},
    {"-"+bdStr(0x1234_5678_9abc_def1L, 0L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 47), null},

    {"// 64 bits of significand, to get rounded-up", null},
    {bdStr(-1L, -1L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 47), null},

    {"// 48 bits of significand, to get rounded-up", null},
    {bdStr(HIGHER_48_BITS >> 1, 0L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 63), null},

    {"// 48 bits of significand", null},
    {bdStr(HIGHER_48_BITS, 0L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 63), null},

    {"// Smallest positive subnormal number", ""},
    {bdStr(0, 0L, Quadruple.EXPONENT_BIAS - IEEE754_EXP_BIAS - 111), null},

    {"// Smallest positive subnormal number", ""},
    {"6.4751751194380251109244389582276465525e-4966", null},

    {"// A little larger number, to be rounded up", ""},
    {mult(bd("6.4751751194380251109244389582276465525e-4966"), 1.5).toString(), null},

    {"// A little larger number, to be rounded down", ""},
    {mult(bd("6.4751751194380251109244389582276465525e-4966"), 1.4).toString(), null},

  };

  /** A tester class to test {@link Quadruple#toIeee754Longs()}.<br>
   * Obtains the test data from {@link #q2ieeeConversionDataList()}
   * and performs {@link Quadruple#toIeee754Longs()} as the tested operation.
   */
  static class QuadToIEEELongsTester extends Conversion_Q2T_Tester<long[]> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.toIeee754Longs()}". */
    @Override protected String getName()                                  { return "Quadruple.toIeee754Longs()"; }

    /** Obtains and returns a data set intended to test {@link Quadruple#toIeee754Longs()}.<br>
     * Uses {@link #q2ieeeConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return q2ieeeConversionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#toIeee754Longs()},
     * with the given operand, and returns its result. */
    @Override
    protected long[] performOp(Quadruple operand)                         { return operand.toIeee754Longs(); }

    /**
     * Creates a {@link DataItem} instance with the resulting array of {@code long} and its string representation
     */
    @Override
    protected DataItem makeResultItem(DataItem srcData) {
      final Quadruple quadValue = srcData.getQuadValue();
      try {
        final long[] result = (quadValue == null) ?
            null :
            performOp(quadValue);                 // returns 2 longs
        return result == null?
            new DataItem("res").withRawValue(null):
//            new DataItem("res").withRawValue(result).withString(" " + stringOfLongs(result)); // A string representation of 2 longs
            new DataItem("res").withRawValue(result).withString(" " + hexStr_(result)); // A string representation of 2 longs
      } catch (final Exception x) {               // Hardly can happen, yet let it remain
        return new DataItem("res").
                    withError(String.format("%s\n%19s performing %s on %s",
                                            x.toString(), "", getName(), quadToString43(quadValue)) );
      }
    }

    /**
     * Creates a {@link DataItem} instance with the numeric value of the IEEE quadruple
     * whose value is represented by the array of {@code long[]} returned by the tested operation
     */
    @Override
    protected DataItem makeResultValueItem(DataItem result) {
      final DataItem dataItem = new DataItem("val");
      final long[] ieeeLongs = (long[])result.getRawData();
      if (ieeeLongs == null)
        return dataItem.withValueOf(null);

      return dataItem.withQuadruple(ieeeLongsToQuadruple(ieeeLongs)); // Converts 2 longs to a Quadruple value
    }

    /** Returns string representations ("Infinity" or "-Infinity" or "NaN")
     * for values that don't fall within the range allowed for IEEE-754 Quadruple.
     * Values too large for IEEE quadruple are also too large for double, so using double to format such values is OK.
     */
    @Override
    protected String findExpectedString(Quadruple operand) {
      return String.valueOf(bigDecimalValueOf(operand).doubleValue());
    }

    /**
     * Calculates a {@code BigDecimal} value of the expected result
     * of the conversion from {@link Quadruple} to {@code long[]}.<br>
     * The expected result is the input value rounded to the exact value
     * of the nearest {@code IEEE-754 Quadruple}, expressed as a {@link BigDecimal}.
     * Throws {@code NumberFormatException} for values that can't be represented as {@code BigDecimal}:
     * {@code NaN}, {@code Infinity}, and {@code -Infinity}.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quadruple -> long[] -> Quadruple -> BigDecimal
      return bigDecimalValueOf(ieeeLongsToQuadruple(quadrupleToIEEELongs(operand)));
    }

  } // private static class QuadToIEEELongsTester extends Conversion_Q2T_Tester<Quadruple, Double> {

  /** A tester class to test {@link Quadruple#toIeee754Bytes()}.<br>
   * Obtains the test data from {@link #q2ieeeConversionDataList()}
   * and performs {@link Quadruple#toIeee754Bytes()} as the tested operation.
   */
  static class QuadToIEEEBytesTester extends QuadToIEEELongsTester {

    /** Returns the name of the tested operation, namely "{@code Quadruple.toIeee754Bytes()}". */
    @Override protected String getName()                                  { return "Quadruple.toIeee754Bytes()"; }

    /** Performs the tested operation, namely {@link Quadruple#toIeee754Bytes()},
     * with the given operand, and returns its result. */
    @Override
    protected long[] performOp(Quadruple operand)               {
      return mergeBytesToLongs(operand.toIeee754Bytes());
    }

    /**
     * Creates a {@link DataItem} instance with the resulting array of {@code long} and its string representation
     */
    @Override
    protected DataItem makeResultItem(DataItem srcData) {
      final Quadruple quadValue = srcData.getQuadValue();
      try {
        final long[] result = (quadValue == null) ? null : performOp(quadValue);
        return result == null?
            new DataItem("res").withRawValue(null):
            new DataItem("res").withRawValue(result).withString(" " + hexStr_(splitLongsToBytes(result)));
      } catch (final Exception x) {
        return new DataItem("res").withError(
                                    String.format("%s\n%19s performing %s on %s",
                                                  x.toString(), "", getName(), quadToString43(quadValue)) );
      }
    }

  } // static class QuadToIEEEBytesTester extends QuadToIEEELongsTester {

  /** A tester class to test {@link Quadruple#assignIeee754(long[])}.<br>
   * Obtains the test data from {@link #q2ieeeConversionDataList()}
   * and performs {@link Quadruple#assignIeee754(long[])} as the tested operation.
   */
  static class AssignIEEELongsTester extends Conversion_T2Q_Tester<long[]> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.assignIeee754(long[])}". */
    @Override
    protected String getName() { return "Quadruple.assignIeee754(long[])"; }

    /**
     * Creates a new {@code DataItem} that contains a 'raw' value of {@code long[]}
     * to be converted by the tested method, or an error message in case of error during parsing the input string.
     * In addition to the overridden {@link Conversion_T2Q_Tester#makeSrcItem(String)},
     * adds to the created item a string hexadecimal representation of the
     * {@code long[]} raw data to be displayed.
     */
    @Override
    protected DataItem makeSrcItem(String s) {
      final DataItem di = super.makeSrcItem(s);
      final long[] rawData = (long[])di.getRawData();
      if (rawData != null)
        // di.withString(stringOfLongs(rawData));
        di.withString(hexStr_(rawData));
      return di;
    }

    /**
     * Parses a string expressing an input value for the tested conversion
     * and returns an array of {@code long} representing
     * IEEE-754 value expressed by the input string.<br>
     */
    @Override
    protected long[] parseSrcType(String s) {
      return parseIeeeLongs(s);
    }

    /**
     * Creates a new {@code DataItem} that contains Quadruple and BigDecimal representations
     * of the IEEE-754 quadruple value whose 128 bits are contained by the array of {@code long},
     * stored in the {@code rawData} field of the DataItem passed in as a parameter.<br>
     * @param srcData a {@code DataItem} instance with the raw source data of type {@code long[]}
     * @return the newly-created instance of {@code DataItem} containing numeric values of the source data
     */
    @Override
    protected DataItem makeSrcValueItem(final DataItem srcData) {
      final long[] rawData = (long[])srcData.getRawData();
      return new DataItem("val").withQuadruple(ieeeLongsToQuadruple(rawData));
    }

    /**
     * Performs the tested operation, namely {@link Quadruple#assignIeee754(long[])},
     * and returns the resulting {@code Quadruple}
     */
    @Override
    protected Quadruple performOp(long[] operand) {
      if (operand == null) return null;
      return new Quadruple().assignIeee754(operand);
    }

    /** Obtains and returns a data set intended to test {@link Quadruple#assignIeee754(long[])}.<br>
     * Uses {@link #q2ieeeConversionDataList()} to obtain the data. */
    @Override
    protected List<String[]> getTestDataList() {
      return q2ieeeConversionDataList();
    }

  } // AssignIEEELongsTester extends Conversion_T2Q_Tester<long[]> {

  /**
   * A tester class to test {@link Quadruple#assignIeee754(byte[])}.<br>
   * Obtains the test data from {@link #q2ieeeConversionDataList()}
   * and performs {@link Quadruple#assignIeee754(long[])} as the tested operation.
   */
  static class AssignIEEEBytesTester extends Conversion_T2Q_Tester<byte[]> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.assignIeee754(byte[])}". */
    @Override
    protected String getName() { return "Quadruple.assignIeee754(byte[])"; }

    /**
     * Creates a new {@code DataItem} that contains a 'raw' value of {@code byte[]}
     * to be converted by the tested method, or an error message in case of error during parsing the input string.
     * In addition to the overridden {@link Conversion_T2Q_Tester#makeSrcItem(String)},
     * adds to the created item a string hexadecimal representation of the
     * {@code byte[]} raw data to be displayed.
     */
    @Override
    protected DataItem makeSrcItem(String s) {
      final DataItem di = super.makeSrcItem(s);
      final byte[] rawData = (byte[])di.getRawData();
      if (rawData != null)
        di.withString(hexStr_(rawData));
      return di;
    }

    /**
     * Parses a string expressing an input value for the tested conversion
     * and returns an array of {@code byte} representing
     * IEEE-754 value expressed by the input string.<br>
     */
    @Override
    protected byte[] parseSrcType(String s) {
      return splitLongsToBytes(parseIeeeLongs(s));
    }

    /**
     * Creates a new {@code DataItem} that contains Quadruple and BigDecimal representations
     * of the IEEE-754 quadruple value whose 128 bits are contained by the array of {@code byte},
     * stored in the {@code rawData} field of the DataItem passed in as a parameter.<br>
     * @param srcData a {@code DataItem} instance with the raw source data of type {@code byte[]}
     * @return the newly-created instance of {@code DataItem} containing numeric values of the source data
     */
    @Override
    protected DataItem makeSrcValueItem(final DataItem srcData) {
      final byte[] rawData = (byte[])srcData.getRawData();
      return new DataItem("val").withQuadruple(ieeeBytesToQuadruple(rawData));
    }

    /**
     * Performs the tested operation, namely {@link Quadruple#assignIeee754(byte[])},
     * and returns the resulting {@code Quadruple}
     */
    @Override
    protected Quadruple performOp(byte[] operand) {
      if (operand == null) return null;
      return new Quadruple().assignIeee754(operand);
    }

    /** Obtains and returns a data set intended to test {@link Quadruple#assignIeee754(byte[])}.<br>
     * Uses {@link #q2ieeeConversionDataList()} to obtain the data. */
    @Override
    protected List<String[]> getTestDataList() {
      return q2ieeeConversionDataList();
    }
  }

  /**
   * Parses a numeric value expressed by the input string and converts it into
   * an array of {@code long} containing the 128 bits of the nearest IEEE-754 quadruple value
   * @param s a string to be parsed
   * @return an array of {@code long} containing the 128 bits of the IEEE-754 quadruple value nearest to
   * the number expressed by the string
   */
  private static long[] parseIeeeLongs(String s) {
    long[] result = new long[2];
    switch (s.toLowerCase()) {
      case "infinity":
        result[0] = IEEE754_INFINITY_LEAD;
        break;
      case "-infinity":
        result[0] = IEEE754_MINUS_INFINITY_LEAD;
        break;
      case "nan":
        result[0] = IEEE754_NAN_LEAD;
        break;
      default:
        result = quadrupleToIEEELongs(buildQuadruple(s));
    }
    return result;
  }

  /**
   * Converts a {@code Quadruple} value to an array of two {@code long}s containing the 128 bits
   * of the IEEE-754 quadruple-precision value nearest to the value of the argument.
   * @param value a {@code Quadruple} whose value is to be converted to the corresponding IEEE-754 quadruple-precision value
   * @return an array of two {@code long}s containing the resulting 128 bits
   */
  private static long[] quadrupleToIEEELongs(Quadruple value) {
    // Special values and values exceeding the valid range
    if (value.isNaN())
      return new long[] {IEEE754_NAN_LEAD, 0};

    if (value.isInfinite()
        ||  value.compareMagnitudeTo(MAX_IEEE754_QUADRUPLE) > 0)
      return value.isNegative()?  new long[] {IEEE754_MINUS_INFINITY_LEAD, 0}:
                                  new long[] {IEEE754_INFINITY_LEAD, 0};

    if (value.isZero()
        || value.compareMagnitudeTo(MIN_IEEE754_QUADRUPLE.divide(2)) < 0)
      return value.isNegative()?  new long[] {IEEE754_MINUS_ZERO_LEAD, 0}:
                                  new long[2];        // They are already zeros

    // regular number
    if (value.unbiasedExponent() > -IEEE754_EXP_BIAS)
      return makeNormalIEEELongs(value);

    return makeSubnormalIEEELongs(value);
  }

  /**
   * Converts a {@code Quadruple} value to an array of two {@code long}s containing the 128 bits
   * of the IEEE-754 quadruple-precision value nearest to the value of the argument,
   * for values falling within the range of normal IEEE-754 quadruple values.
   * @param value the value to convert
   * @return an array of two {@code long}s containing the 128 bits
   * of the resulting IEEE-754 quadruple value
   */
  private static long[] makeNormalIEEELongs(Quadruple value) {
    int exponent = value.unbiasedExponent() + IEEE754_EXP_BIAS;     // Biased IEEE-754 quadruple exponent
    final long signBit = value.isNegative()? IEEE754_SIGN_MASK : 0;
    long mantHi = value.mantHi(), mantLo = value.mantLo();

    mantLo = (mantLo + 0x8000L) & HIGHER_48_BITS >> 1; // Round half-up to 112 bits
    if ((mantLo == 0)
        && (++mantHi == 0))
        exponent++;
    mantLo &= HIGHER_48_BITS;

    final long[] result = new long[2];
    result[0] = signBit
                | (long)exponent << 48 & IEEE754_EXPONENT_MASK
                | mantHi >>> 16;
    result[1] = mantHi << 48 | mantLo >>> 16;
    return result;
  }

  /**
   * Converts a {@code Quadruple} value to an array of two {@code long}s containing the 128 bits
   * of the IEEE-754 quadruple-precision value nearest to the value of the argument,
   * for values falling within the range of subnormal IEEE-754 quadruple values.
   * @param value the value to convert
   * @return an array of two {@code long}s containing the 128 bits
   * of the resulting IEEE-754 quadruple value
   */
  private static long[] makeSubnormalIEEELongs(Quadruple value) {
    int shift     = -value.unbiasedExponent() - IEEE754_EXP_BIAS + 16;  // Biased IEEE-754 quadruple exponent
    final long signBit  = value.isNegative()? IEEE754_SIGN_MASK : 0;
    long mantHi   = value.mantHi();
    long mantLo   = value.mantLo();

    final long bitToRoundUp         = 1L << shift;
    long implicitOneToSet           = IEEE754_SIGN_MASK >>> shift;
    final long maskToClearLowerBits = ALL_64_BITS << shift;

    if (shift < 63) {             //  There are bits of the significand in the lower word
      mantLo = mantLo + bitToRoundUp & maskToClearLowerBits;
      if (mantLo == 0)            // After rounding up the lower word carry to the higher one
        if (++mantHi == 0)        // Overflow
          if (shift == 16)        // It was max subnormal, becomes min normal
            return new long[] { signBit | 0x0001_0000_0000_0000L, 0};
          else implicitOneToSet <<= 1;
      shift++;
      mantLo = mantLo >>> shift | mantHi << 64 - shift;
      mantHi = mantHi >>> shift | implicitOneToSet;
      return new long[] {signBit | mantHi, mantLo};
    } else if (shift == 63) {     // Retain 64 bits of significand. The MSB of the lower word is to be rounded
      if (mantLo < 0)             // To be rounded-up
        if (++mantHi == 0)        // Overflow
          implicitOneToSet <<= 1;
      return new long[] {signBit | implicitOneToSet, mantHi};
    } else {                      // Retain less than 64 bits of significand
      shift++;
      mantHi = mantHi + bitToRoundUp & maskToClearLowerBits;
      if (mantHi == 0)
        implicitOneToSet <<= 1;
      mantHi &= maskToClearLowerBits << 1;
      mantLo = mantHi >>> shift | implicitOneToSet;
      return new long[] {signBit, mantLo};
    }
  }

  /**
   * Creates a {@code Quadruple} instance with the value of an IEEE-754 quadruple-precision number
   * represented by the 128 bits passed in as an array of two {@code long}s
   * @param longs an array of two {@code long}s with the 128 bits of the IEEE-754
   *              quadruple-precision value to be converted
   * @return a new {@code Quadruple} with the value of the input IEEE-754 quadruple
   */
  private static Quadruple ieeeLongsToQuadruple(long[] longs) {
    if (longs == null) return null;
    final int exponent = (int)(longs[0] >>> 48) & IEEE754_EXP_OF_INFINITY;
    final boolean negative = longs[0] < 0;
    if (exponent == IEEE754_EXP_OF_INFINITY) {             // Nan or infinity
      if ((longs[0] & LOWER_48_BITS | longs[1]) != 0)      // Non-zero mantissa, NaN
        return Quadruple.nan();
      return negative?  Quadruple.negativeInfinity() :
                        Quadruple.positiveInfinity();
    }
    if (exponent == 0)  {                                  // 0 or subnormal
      if ((longs[0] & LOWER_48_BITS | longs[1]) == 0)      // Zero mantissa, 0 or -0
        return negative?  new Quadruple(-0.0) :
                          new Quadruple();
    }
    final long mantHi = (longs[0] & LOWER_48_BITS) << 16 | longs[1] >>> 48;
    final long mantLo = longs[1] << 16;
    final int quadExp = exponent - IEEE754_EXP_BIAS + Quadruple.EXPONENT_BIAS;

    if (exponent != 0)                                     // Normal value -- simply construct a Quadruple
      return new Quadruple(negative, quadExp, mantHi, mantLo);

    // Make a subnormal
    int expCorrection = Long.numberOfLeadingZeros(mantHi); // The highest bit is to be shifted out, it becomes implicit 1
    int shift = expCorrection + 1;
    if (shift == 65) {                                      // Hi-order 64 bits are empty
      expCorrection = Long.numberOfLeadingZeros(mantLo) + 64;
      shift = expCorrection - 64 + 1;
      return new Quadruple(negative, quadExp - expCorrection, mantLo << shift, 0);
    } else if (shift == 64) {
      return new Quadruple(negative, quadExp - expCorrection, mantLo, 0);
    }
    else return new Quadruple(negative, quadExp - expCorrection,
                         mantHi << shift | mantLo >>> (64 - shift),
                         mantLo << shift);
  }

  /**
   * Creates a {@code Quadruple} instance with the value of an IEEE-754 quadruple-precision number
   * represented by the 128 bits passed in as an array of 16 {@code byte}s
   * @param longs an array of 16 {@code byte}s with the 128 bits of the IEEE-754
   *              quadruple-precision value to be converted
   * @return a new {@code Quadruple} with the value of the input IEEE-754 quadruple
   */
  private static Quadruple ieeeBytesToQuadruple(byte[] bytes) {
    return ieeeLongsToQuadruple(mergeBytesToLongs(bytes));
  }

  /**
   * Splits the N {@code long} items of the input array to bytes and return N * 8 bytes containing
   * the bits of the input {@code longs}.
   * Big-endian, the most significant bit of the {@code longs[0]} becomes the MSB of the {@code result[0]}
   * @param longs the longs to split
   * @return the resulting bytes
   */
  private static byte[] splitLongsToBytes(long[] longs) {
    final byte[] result = new byte[longs.length * 8];
    for (int i = 0; i < longs.length; i++) {
      long currentLong = longs[i];
      for (int j = 7; j >= 0; j--) {
        result[i * 8 + j] = (byte) currentLong;
        currentLong >>>= 8;
      }
    }
    return result;
  }

  /**
   * Merges the given N bytes of the input array and returns N/8 longs containing the bits
   * of the of the input {@code bytes} .
   * Big-endian, the most significant bit of the {@code bytes[0]} becomes the MSB of the {@code result[0]}
   * @param bytes the bytes to merge
   * @return the resulting longs
   */
  private static long[] mergeBytesToLongs(byte[] bytes) {
    final long[] result = new long[bytes.length / 8];
    for (int i = 0; i < result.length; i++)
      for (int j = 0; j < 8; j++) {
        result[i] = (result[i] << 8) | (long)bytes[i * 8 + j] & 0xFF;
      }
    return result;
  }

//  /**
//   * Returns a string hexadecimal representation of the input 16 bytes, in form
//   * <pre>
//   *    XXXX XXXX XXXX XXXX XXXX XXXX XXXX XXXX
//   * </pre>, with the most significant 4 bits of {@code bytes[0]} in the first position
//   * @param bytes the byte array to format
//   * @return a string with the hexadecimal representation of the input bytes
//   */
//  private static String stringOfBytes(byte[] bytes) {
//    final StringBuilder sb = new StringBuilder();
//    for (int i = 0; i < bytes.length; i++) {
//      sb.append(String.format("%02x", bytes[i]));
//      if ((i % 2 != 0) && (i < bytes.length - 1))
//        sb.append(" ");
//    }
//    return sb.toString();
//  }

  /**
   * Returns the test data for Quadruple <-> IEEE-754 quadruple conversions,
   * consisting of {@link #q2ieeeTestData} and a series of
   * random values falling within the range of IEEE-754 quadruple values, whose length is
   * defined by the value of the "-r" command line argument
   * @return a list of array of strings with the test data, 2 values in each array
   * (a data to test the operation with and the expected result, which is always null in this case,
   * since for these tests the expected result can be always evaluated by the test code)
   */
  private static List<String[]> q2ieeeConversionDataList() {
    final ArrayList<String[]> list = new ArrayList<>(Arrays.asList(q2ieeeTestData));
    list.addAll(DataProviders.convertToListOfArraysNx2(
        DataGenerators.Randoms.randomIeee754Quadruples(DataProviders.getRandomCount())));
    return list;
  }

}
