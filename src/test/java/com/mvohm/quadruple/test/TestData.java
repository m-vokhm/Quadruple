/*

 Copyright 2021 M.Vokhmentsev

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*/

package com.mvohm.quadruple.test;

import static com.mvohm.quadruple.test.AuxMethods.*;
import static com.mvohm.quadruple.test.Consts.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.mvohm.quadruple.Quadruple;
import static com.mvohm.quadruple.Quadruple.*;

/**
 * Statically defined data sets for testing basic public methods of {@link com.mvohm.quadruple.Quadruple}.
 * Provide coverage for all the code of the methods and some corner cases
 * (except most trivial ones that don't require special testing).
 * All the data are static arrays of {@code Strings} representing decimal numeric values,
 * generally with precision of 120 decimal digits. Every array contains data
 * for testing a certain operation, like addition or a conversion from one type to another.
 * The arrays consist of logical groups, each group of two (for unary operations and conversions)
 * or three (for binary operations) items represents a test case.
 * The first item of a pair or the two first items of a triplet are the input operands
 * for the corresponding operation, and the last one is a value that should be the result
 * of the operation. In most cases, the expected result may be determined by the code
 * that performs testing, in such cases there may be {@code null} or an empty string instead
 * of the expected value, e.g.
 * <br><pre>
 * "3.5", "4.5", "8",   // for addition: 3.5 + 4.5 = 8
 * "1", "2", null,      // the testing code will find
 *                      // that the result should be 3
 * ...
 * "-1.2345e2345", "",  // for the conversion from Quadruple to double,
 *                      // the result should be Double.NEGATIVE_INFINITY
 * </pre>
 * In cases where the operation being tested, when applied to the given operand(s), should throw an exception,
 * the word "Error" is used instead of the expected result, to inform the testing code about the fact
 * that throwing an exception is the correct and expected behavior of the operation.<br>
 * There are also two sorts of strings that are interpreted by the testing code in special ways:<br><ul>
 * <li>If the first string of the group starts with "//", such group is considered to contain a comment
 * rather than a test case data, and its first item can be printed by the testing code to the console,
 * depending on the code and/or the execution mode, the subsequent items of the group are ignored in this case.
 * <li>If the first string of the group equals "$_STOP_$" (case-insensitively),
 * it is interpreted as an instruction to quit the test execution.
 * </ul>
 * <br>
 * @author M.Vokhmentsev
 */
public class TestData {

// Constants used to compute required BigDecimal and String test data
  private static final MathContext MC_55_HALF_UP      = new MathContext(55, RoundingMode.HALF_UP);
  private static final MathContext MC_100_HALF_EVEN      = new MathContext(100, RoundingMode.HALF_EVEN);

  private static final BigDecimal BD_2$32     = BD_2.pow(32);
  private static final BigDecimal BD_2$98     = BD_2.pow(98);
  private static final BigDecimal BD_2$128    = BD_2$64.multiply(BD_2$64);

  /** BigDecimal value of 1 + 2^-128 */
  private static final BigDecimal BD_ONE_Plus   = BigDecimal.ONE.add(BD_2$_128);        // 1 + 2^-128, unity in the LSB

  @SuppressWarnings("unused")
  private static final String QUIT_TESTING = "$_STOP_$"; // Insert into a data array to stop execution and ignore the following data

  private static final String MIN_VALUE_STR       = String.format("%.55e", MIN_VALUE);
  /** MIN_VALUE * 2^64 */
  private static final BigDecimal MIN_VALUEx2$64  = MIN_VALUE.multiply(BD_2$64, MC_55_HALF_UP);
  /** MIN_VALUE * 2^64 as a String */
  private static final String MIN_VALUEx2$64_STR  = String.format("%.55e", MIN_VALUEx2$64);

  /** Minimum normal value, 2^(-2^31+2), as a String with 55 digits */
  // = 2.2706462104014925375265672651795875812474772990910655746309201361434841604722256893214141039212995740644760522403779555631474179611977248047528763819531062771213622792665125540340375601723011976709225e-646456993
  private static final String MIN_NORMAL_STR      = String.format("%.55e", MIN_NORMAL);

  /** 2^( (2^31) - 1 - 128) -- the value of the least significant bit of MAX_VALUE */
  private static final BigDecimal LSB_OF_MAX_VALUE
    = new BigDecimal(  "2.5884577382366366419710873505577468646410383081805160004365664229250911891015677877844038561582929519540528170828737642542445992622747593903e+646456954"
      // Count digits:   1___5___10____5___20____5___30____5___40____5___50____5___60____5___70____5___80____5___90____5__100____5___10____5___20____5___30____5___40
  );
  private static final String MAX_VALUE_STR       = String.format("%.55e", Consts.MAX_VALUE);

  /* ******************************************************************************
   ******** Data arrays containing test data **************************************
   ********************************************************************************/

  /**
   * A data set to test some corner cases for methods converting {@code Quadruple} values
   * to values of other types, except {@code BigDecimal}.<br>
   * Includes {@code NaN}, {@code Infinity}, the boundary values of widely used ranges, such as
   * {@link Double#MAX_VALUE}, {@link Long#MAX_VALUE}, etc., both positive and negative,
   * and some values between them.<br>
   * Used to provide coverage for quick-and-dirty checks that are performed at the beginning
   * of most of the conversion methods.
   */
  static String[] rough_Q2T_cornerCases = new String[] {
    "NaN",                                        null,
    "Infinity",                                   null,
    bdStr(mult(Double.MAX_VALUE, 2)),             null,
    bdStr(Double.MAX_VALUE),                      null,
    bdStr(div(Double.MAX_VALUE, 2)),              null,
    bdStr(mult(Long.MAX_VALUE, 2)),               null,
    bdStr(Long.MAX_VALUE),                        null,
    bdStr(div(Long.MAX_VALUE, 2)),                null,
    bdStr(mult(Integer.MAX_VALUE, 2)),            null,
    bdStr(Integer.MAX_VALUE),                     null,
    bdStr(div(Integer.MAX_VALUE, 2)),             null,
    "12345",                                      null,
    "0",                                          null,
    "-0",                                         null,
    "-12345",                                     null,
    bdStr(div(Integer.MAX_VALUE, -2)),            null,
    bdStr(-Integer.MAX_VALUE),                    null,
    bdStr(mult(Integer.MAX_VALUE, -2)),           null,
    bdStr(div(Long.MAX_VALUE, -2)),               null,
    bdStr(-Long.MAX_VALUE),                       null,
    bdStr(mult(Long.MAX_VALUE, -2)),              null,
    bdStr(div(Double.MAX_VALUE, -2)),             null,
    bdStr(-Double.MAX_VALUE),                     null,
    bdStr(mult(Double.MAX_VALUE, -2)),            null,
    "-Infinity",                                  null,
  };

  /**
   * A data set to test some corner cases for the method converting {@code Quadruple} values
   * to {@link BigDecimal} values.<br>
   * Includes {@code NaN}, {@code Infinity}, the boundary values of widely used ranges, such as
   * {@link Double#MAX_VALUE}, {@link Long#MAX_VALUE}, etc., both positive and negative,
   * and some values between them. Contains expected results ({@code "error" or "0"}) for the values
   * that have no valid {@code BigDecimal} counterparts ({@code NaN, Infinity, -Infinity, -0}).<br>
   * Used to provide coverage for quick-and-dirty checks that are performed
   * at the beginning of {@link Quadruple#bigDecimalValue()} method.
   */
  static String[] rough_Q2BD_cornerCases = new String[] {
    "NaN",                                        "error",
    "Infinity",                                   "error",
    bdStr(mult(Double.MAX_VALUE, 2)),             null,
    bdStr(Double.MAX_VALUE),                      null,
    bdStr(div(Double.MAX_VALUE, 2)),              null,
    bdStr(mult(Long.MAX_VALUE, 2)),               null,
    bdStr(Long.MAX_VALUE),                        null,
    bdStr(div(Long.MAX_VALUE, 2)),                null,
    bdStr(mult(Integer.MAX_VALUE, 2)),            null,
    bdStr(Integer.MAX_VALUE),                     null,
    bdStr(div(Integer.MAX_VALUE, 2)),             null,
    "12345",                                      null,
    "0",                                          null,
    "-0",                                         "0",
    "-12345",                                     null,
    bdStr(div(Integer.MAX_VALUE, -2)),            null,
    bdStr(-Integer.MAX_VALUE),                    null,
    bdStr(mult(Integer.MAX_VALUE, -2)),           null,
    bdStr(div(Long.MAX_VALUE, -2)),               null,
    bdStr(-Long.MAX_VALUE),                       null,
    bdStr(mult(Long.MAX_VALUE, -2)),              null,
    bdStr(div(Double.MAX_VALUE, -2)),             null,
    bdStr(-Double.MAX_VALUE),                     null,
    bdStr(mult(Double.MAX_VALUE, -2)),            null,
    "-Infinity",                                  "error",
  };

  /**
   * A data set to test some corner cases for {@link Quadruple#assign(long)} method.<br>
   * Includes {@link Long#MAX_VALUE}, {@link Long#MIN_VALUE}, and a number of values between them.
   * Used for draft testing of {@link Quadruple#assign(long)} method.
   */
  static String[] rough_l2Q_cornerCases = new String[] {
    String.valueOf(Long.MAX_VALUE),               null,
    String.valueOf(Long.MAX_VALUE / 2),           null,
    String.valueOf(Integer.MAX_VALUE * 2L),       null,
    String.valueOf(Integer.MAX_VALUE),            null,
    String.valueOf(Integer.MAX_VALUE / 2),        null,
    "12345",                                      null,
    "0",                                          null,
    "-12345",                                     null,
    String.valueOf(Integer.MIN_VALUE / 2 + 1),    null,
    String.valueOf(Integer.MIN_VALUE + 1),        null,
    String.valueOf(Integer.MIN_VALUE * 2L + 1),   null,
    String.valueOf(Long.MIN_VALUE / 2 + 1),       null,
    String.valueOf(Long.MIN_VALUE + 1),           null,
    String.valueOf(Long.MIN_VALUE),               null,
  };

  /**
   * A data set for testing {@link Quadruple#toString()} method.<br>
   * Each data sample consists of two strings: a string representation of the number
   * to be converted, and the expected result. Covers all execution paths of
   * {@code Quadruple.toString()} method.<br>
   * Actually, in this data set the expected result can be correctly deduced
   * by the test code in all cases, so all the odd items are {@code null}s.<br>
   * Completely covers the code of the {@code Quadruple.toString()} method.<br>
   * The number of items must be even.
   */
  static String[] basic_Q2S_conversionData = new String[] {
/**/

    "// Basic checks at the entrance to toString()", null,
    "NaN",                                  null,
    bdStr(0x1234_5678_9abc_defeL, 0x1234_5678_9abc_defeL, -1), null, // NaN
    "Infinity",                             null,
    "-Infinity",                            null,
    "0",                                    null,
    "-0",                                   null,

  // Normal, cover multMantByPowerOfTwo() and powerOfTwo()
    "12345",                                null,
    "1.2345",                               null,
    "0.12345",                              null,

  // Subnormal, ( if (exponent == 0) { ... )
    MIN_VALUE_STR,                          null,
    bdStr(mult(MIN_VALUE, 123.456)),        null,

  // cover decimalMantToString()
    // convertMantToString(),
    "25",                                   null,   // isEmpty() returns false
    "0.125",                                null,   // isEmpty() returns true

    // if ((multBuffer[0] & 0x8000_0000L) != 0) { // if the remainder > 5e-40, round up
    // (addCarry(sb) != 1)
    "444444444444444444444444444444444444444444444444444", null,
    // (addCarry(sb) == 1)
    "1e7",                                  null,

  // Some more data just to play
    bdStr(mult(MIN_VALUE, -123.456)),       null,
    bdStr(mult(MIN_VALUE, powerOfTwo(64))),       null,

    bdStr(sub(MIN_NORMAL, MIN_VALUE)),      null, // Max subnormal

  "// MIN_NORMAL * 0.0000123456",           null,
    bdStr(mult(MIN_NORMAL, 0.0000123456)),  null,
  "// MIN_NORMAL",                          null,
    MIN_NORMAL_STR,                         null,
  "// MIN_NORMAL / 2",                      null,
    bdStr(div(MIN_NORMAL, 2)),              null,

    "9.999999999999999999999999999999999999e-646456995",  null, // "9.999999999999999999999999999999999998810534e-646456995",
    "1.0000000000000000000000000000000000010e-646456994", null, // "1.000000000000000000000000000000000001015434e-646456994",

    "3",                                    null,
    "1.234567890",                          null,
    "0.999999999999999999999999999999999999999", null,
    "-0.0625",                              null,
    "15.9999999999999999999999999",         null,
    "99999999999999999999999999999999999999999", null,
    "1.001",                                null,
    "9.999",                                null,

  "// bits set at the border of words",     null,
    bdStr(add(BD_ONE, BD_2$_64)),           null,
    bdStr(add(BD_ONE, div(BD_2$_64, 2))),   null,

    /**/
  }; // static String[] basic_Q2S_conversionData = new String[] {

  /**
   * A data set for testing the conversion of a {@code Quadruple} value to
   * a {@code double} value with {@link Quadruple#doubleValue()} method.<br>
   * Each data sample consists of two strings: a string representation of the number
   * to be converted, and the expected result.
   * The latter is actually needed only in the cases when the value can't be expressed as
   * {@code BigDecimal}, which is the return type of
   * {@code TesterClasses.UnaryFunctionTester.findExpectedResult()}.<br>
   * Completely covers the code of the {@code Quadruple.doubleValue()} method.<br>
   * The number of items must be even.
   */
  static String[] basic_Q2D_conversionData = new String[] {

    "0",                                    null,
    "-0",                                   null,
    bdStr(1,0,0),                           null, // to cover initial detection of 0 and -0
    bdStr(0,1,0),                           null,
    bdStr(0,0,1),                           null,

    "NaN",                                  null,   //      "Double.NaN",
    // if (exponent == EXP_INF) return (mantHi != 0 || mantLo != 0 )? Double.NaN :
    // The case when exponent == EXP_INF && mantHi == 0 && mantLo != 0 can't be covered since the
    // Quadruple values used by the tests are built from Strings
    "-Infinity",                            null,         //      "Double.NEGATIVE_INFINITY",
    "Infinity",                             null,         //      "Double.POSITIVE_INFINITY",

                                            // Conversion_Q2T_Tester.findExpectedResult returns BigDecimal
    "4e308",                                "Infinity",   // Exponent out of range: q > Double.MAX_VALUE, => Infinity
    "-4e308",                               "-Infinity",  // Exponent out of range: q < -Double.MAX_VALUE, => -Infinity
    "1.23456e5000000",                      "Infinity",
    "-1.23456e5000000",                     "-Infinity",
    bdStr(0, 0, EXPONENT_OF_MAX_VALUE),     "Infinity",   // Quadruple.MAX_VALUE

    bdStr(mult(Double.MIN_VALUE, 0.5)),     null,         // q < Double.MIN_VALUE, 0
    bdStr(mult(Double.MIN_VALUE, -0.5)),    null,         // q < Double.MIN_VALUE, 0
    "// Quadruple.MIN_NORMAL",              null,
    bdStr(0, 0, 1),                         null,         // Quadruple.MIN_NORMAL

    // Subnormal values
    "// Double.MIN_VALUE",                  null,
    bdStr(Double.MIN_VALUE),                null,
    bdStr(-Double.MIN_VALUE),               null,
    bdStr(mult(Double.MIN_VALUE, 123456789)),  null,
    bdStr(mult(-Double.MIN_VALUE, 123456789)), null,

    // Some normal values
    "1.2345678901234567890",                null,
    "1.2345",                               null,
    "-1.2345",                              null,
    "5.6789",                               null,
    "9.9999",                               null,
    "1.2345e80",                            null,
    "5.6789e80",                            null,
    "9.9999e80",                            null,
    "1.2345e-80",                           null,
    "5.6789e-80",                           null,
    "9.9999e-80",                           null,

    // Some corner cases
    // Rounding up
    bdStr(0x7FFF_FFFF_FFFF_F800L, 0, EXPONENT_OF_ONE), null, // One bit to the right of the allowed bits of mantissa, should get rounded up to 1.5
    bdStr(0x7FFF_FFFF_FFFF_F7FFL, 0, EXPONENT_OF_ONE), null, // shouldn't get rounded up, should remain 1.49999...
    // Overflow of the mantissa as the result of rounding up
    bdStr(0xFFFF_FFFF_FFFF_F800L, 0, EXPONENT_OF_ONE), null, // One bit to the right of the allowed bits of mantissa, should get rounded up to 2
    bdStr(0xFFFF_FFFF_FFFF_F7FFL, 0, EXPONENT_OF_ONE), null, // shouldn't get rounded up, should remain 1.99999...

    // Double.MAX_VALUE
    bdStr(0xFFFF_FFFF_FFFF_F000L, 0, EXPONENT_BIAS + DOUBLE_EXP_BIAS),  null,
    // One extra bit, rounding up to Infinity
    bdStr(0xFFFF_FFFF_FFFF_F800L, 0, EXPONENT_BIAS + DOUBLE_EXP_BIAS),  "Infinity",
    // The same for the negative value
    "-" + bdStr(0xFFFF_FFFF_FFFF_F800L, 0, EXPONENT_BIAS + DOUBLE_EXP_BIAS), "-Infinity",
    // MAX_VALUE + Less than 0.5 LSB, should round down to MAX_VALUE
    bdStr(0xFFFF_FFFF_FFFF_F7FFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_BIAS + DOUBLE_EXP_BIAS), null,

    // All the code is covered.
    // Some more values just to play
    // QUIT_TESTING, null,

    "// Double.MAX_VALUE - (Double.MAX_VALUE * 1e-16): ", null,
    bdStr( Double.MAX_VALUE - (Double.MAX_VALUE * 1e-16)), null,

    "// MIN_NORMAL",                        null,
    bdStr(Double.MIN_NORMAL),               null,

    "// MIN_NORMAL / 2",                    null,
    bdStr(Double.MIN_NORMAL / 2),           null,

    "// MIN_NORMAL / 4",                    null,
    bdStr(Double.MIN_NORMAL / 4),           null,

    "// MIN_NORMAL / 8",                    null,
    bdStr(Double.MIN_NORMAL / 8),           null,

    "// MIN_NORMAL * (1 + 1e-15)",          null,
    bdStr(Double.MIN_NORMAL * (1 + 1e-15)), null,

    "// Double.MIN_NORMAL - (Double.MIN_NORMAL / 4)", null,
    bdStr(Double.MIN_NORMAL - (Double.MIN_NORMAL / 4)), null,

    "// Double.MIN_NORMAL / 256",           null,
    bdStr(Double.MIN_NORMAL / 256),         null,

    "// ~Double.MIN_VALUE / 2",             null,
    "2.4e-324",                             null,

    "// Double.MIN_VALUE",                  null,
    bdStr(Double.MIN_VALUE),                null,

    "// Double.MIN_VALUE * 2",              null,
    bdStr(Double.MIN_VALUE * 2),            null,

    "// Double.MIN_VALUE * 10",             null,
    bdStr(Double.MIN_VALUE * 10),           null,

    "// -Double.MAX_VALUE",                 null,
    bdStr(-Double.MAX_VALUE),               null,

    "// -(Double.MAX_VALUE * (1 - 1e-16))",   null,
    bdStr(-(Double.MAX_VALUE * (1 - 1e-16))), null,
  }; // static String[] basic_Q2D_conversionData = new String[] {

  /**
   * A data set for testing the conversion of a {@code Quadruple} value to
   * a {@code long} value with {@link Quadruple#longValue()} method.<br>
   * Each data sample consists of two strings: a string representation of the number
   * to be converted, and the expected result.
   * Actually, for all data samples from this set the expected results
   * can be correctly deduced by the test code, so all the odd items are {@code null}s.<br>
   * Completely covers the code of the {@code Quadruple.longValue()} method.<br>
   * The number of items must be even.
   */
  static String[] basic_Q2L_conversionData = new String[] {

    "NaN",                                  null,   //      "Double.NaN",
    "-Infinity",                            null,   //      "Double.NEGATIVE_INFINITY",
    "Infinity",                             null,   //      "Double.POSITIVE_INFINITY",

    "0.5",                                  null,
    "-0.5",                                 null,
    "1",                                    null,
    "-1",                                   null,
    "1.3",                                  null,
    "-1.3",                                 null,
    "1.8",                                  null,
    "-1.8",                                 null,
    "5",                                    null,
    "12345",                                null,
    "-12345",                               null,

    "// Range boundaries",                  null,
    "// Long.MAX_VALUE + 1",                null,
    str(add(Long.MAX_VALUE, 1)),            null,

    "// Long.MAX_VALUE",                    null,
    str(Long.MAX_VALUE),                    null,
    "// Long.MAX_VALUE - 1",                null,
    str(sub(Long.MAX_VALUE, 1)),            null,

    "// Long.MIN_VALUE + 1",                null,
    str(add(Long.MIN_VALUE, 1)),            null,
    "// Long.MIN_VALUE",                    null,
    str(Long.MIN_VALUE),                    null,
    "// Long.MIN_VALUE - 1",                null,
    str(sub(Long.MIN_VALUE, 1)),            null,

  }; // static String[] basic_Q2L_conversionData = new String[] {

  /**
   * A data set for testing the conversion of a {@code Quadruple} value to
   * an {@code int} value with {@link Quadruple#intValue()} method.<br>
   * Each data sample consists of two strings: a string representation of the number
   * to be converted, and the expected result.
   * Actually, the expected results can be correctly deduced by the test code in all cases,
   * so all the odd items are {@code null}s.<br>
   * Completely covers the code of the {@code Quadruple.intValue()} method.<br>
   * The number of items must be even.
   */
  static String[] basic_Q2I_conversionData = new String[] {

    "NaN",                                  null,   //      "Double.NaN",
    "-Infinity",                            null,   //      "Double.NEGATIVE_INFINITY",
    "Infinity",                             null,   //      "Double.POSITIVE_INFINITY",

    "0.5",                                  null,
    "-0.5",                                 null,
    "1",                                    null,
    "-1",                                   null,
    "1.3",                                  null,
    "-1.3",                                 null,
    "1.8",                                  null,
    "-1.8",                                 null,
    "5",                                    null,
    "12345",                                null,
    "-12345",                               null,

    "// Range boundaries",                  null,
    "// Integer.MAX_VALUE + 1",             null,
    str(add(Integer.MAX_VALUE, 1)),         null,
    "// Integer.MAX_VALUE",                 null,
    str(Integer.MAX_VALUE),                 null,
    "// Integer.MAX_VALUE - 1",             null,
    str(sub(Integer.MAX_VALUE, 1)),         null,

    "// Integer.MIN_VALUE + 1",             null,
    str(add(Integer.MIN_VALUE, 1)),         null,
    "// Integer.MIN_VALUE",                 null,
    str(Integer.MIN_VALUE),                 null,
    "// Integer.MIN_VALUE - 1",             null,
    str(sub(Integer.MIN_VALUE, 1)),         null,

  }; // static String[] basic_Q2I_conversionData = new String[] {

  /**
   * A data set for testing the conversion of a {@code Quadruple} value to
   * an {@link BigDecimal} value with {@link Quadruple#bigDecimalValue()} method.<br>
   * Each data sample consists of two strings: a string representation of the number
   * to be converted, and the expected result.
   * For the values that are not valid for {@code BigDecimal}, the tested method is expected
   * to throw an exception. The word "Error" is used as a substitute of the expected result in
   * such cases, to inform the test code that throwing an exception is the correct behavior.
   * For all other values, the expected results can be evaluated by the testing code,
   * thus the data contains {@code null}s instead of expected values.<br>
   * Completely covers the code of the {@code Quadruple.bigDecimalValue()} method.<br>
   * The number of items must be even.
   */
  static String[] basic_Q2BD_conversionData = new String[] {

  "// Check that exceptions get thrown for values that are not acceptable for BigInteger", null,
//    "Hren", "error",
    "NaN",                                                    "Error", // Not a valid value for BigDecimal
    "Infinity",                                               "Error",
    "-Infinity",                                              "Error",

  "// Test fast zero detection",                              null,
    "0",                                                      null,

  "// Test both branches in buildBDMantissa();",              null,
    "// Normal",                                              null,
    "1.234567890123456789012345678901234567890",              null,
    "-1.234567890123456789012345678901234567890",             null,
    "// Subnormal", null,
    bdStr(mult(MIN_VALUE, add(BD_2$98, bd("12345678901234567890")))), null,


  "// Test result normalization", null,

    str( mult(bd("1.25"), bd("1e-100") ) ),                   null,
    str( mult(bd("1.25"), bd("1e-10") ) ),                    null,
    str( mult(bd("1.25"), bd("1e-5") ) ),                     null,
    str( mult(bd("1.25"), bd("1e-1") ) ),                     null,
    str( mult(bd("1.25"), bd("1e0") ) ),                      null,
    str( mult(bd("1.25"), bd("1e1") ) ),                      null,
    str( mult(bd("1.25"), bd("1e10") ) ),                     null,
    str( mult(bd("1.25"), bd("1e100") ) ),                    null,

    str( mult( add (bd("1.25"), BD_2$_128), bd("1e-100") ) ), null,
    str( mult( add (bd("1.25"), BD_2$_128), bd("1e-10") ) ),  null,
    str( mult( add (bd("1.25"), BD_2$_128), bd("1e-1") ) ),   null,
    str( mult( add (bd("1.25"), BD_2$_128), bd("1e0") ) ),    null,
    str( mult( add (bd("1.25"), BD_2$_128), bd("1e1") ) ),    null,
    str( mult( add (bd("1.25"), BD_2$_128), bd("1e10") ) ),   null,
    str( mult( add (bd("1.25"), BD_2$_128), bd("1e100") ) ),  null,


    BD_ONE.divide(BD_2$32.multiply(bd("1000000"))).toString(), null,
    BD_2$32.multiply(bd("1000000")).toString(),               null,
    "123456789012345678901234567890123456789012345",          null,
    BD_2.pow(-20, MC_55_HALF_UP).toString(),                  null,

  }; // static String[] basic_Q2BD_conversionData = new String[] {

  /** An auxiliary data set that can be used for testing or debugging
   * {@code Quadruple.bigDecimalValue()} with some values of a special interest.<br>
   * Each data sample consists of two strings: a string representation of the value
   * to be used for testing or debugging the method, and a string representation
   * of the expected result.<br>
   * The number of items must be even.
   */
  static String[] special_S2Q_conversionSata = {
    "// MAX_VALUE + LSB * 0.5, expected infinity or MAX_VALUE", null,
    Consts.MAX_VALUE.add(LSB_OF_MAX_VALUE.multiply(bd("0.5")), MC_100_HALF_EVEN).toString(), null, // MAX_VALUE_STR,
  };

  /**
   * A data set for testing {@link Quadruple#assign(String value)} method or the constructor
   * that accepts a {@code String} parameter.<br>
   * Each data sample consists of two strings: a string representation of the value to be assigned,
   * and a string representation of the value that a Quadruple instance is expected to have
   * after the assignment. In most cases the expected value for a sample can be evaluated
   * by the testing code, so {@code null}s are used instead of the expected values.<br>
   * Since some special String values can be used as the assignment arguments to designate
   * certain Quadruple values ("min_value", "max_value", etc.), such strings are accompanied
   * with string representations of the corresponding Quadruple values.<br>
   * The samples of knowingly unacceptable input strings that are expected to cause an exception
   * during their parsing are accompanied with the word "error", to inform the test code that
   * throwing an exception is the correct behavior for such cases.<br>
   * Completely covers the code of the {@code Quadruple.assign(String value)} method.<br>
   * The number of items must be even.
   */
  static String[] basic_S2Q_conversionData = new String[] {

  /* */
    "// Test mantissa parsing",             null,
    "12345",                                null,
    "12345.",                               null,
    "12.345",                               null,
    ".12345",                               null,
    ".e35",                                 "error",
    "e35",                                  "error",
    "-e35",                                 "error",
    "33.e5",                                null,

    "// Test exponent parsing",             null,
    "1.2345e0",                             null,
    "1.2345e-0",                            null,
    "1.2345e+0",                            null,
    "1.2345e5",                             null,
    "1.2345e-5",                            null,
    "1.2345e+5",                            null,
    "1.2345e5.5",                           "error",
    "1.2345e-5.5",                          "error",
    "1.2345e+5.5",                          "error",
    "1.2345e",                              "error",
    "1.2345e-",                             "error",
    "1.2345e+",                             "error",
    "1.2345",                               null,

  "// String representations of the         constants", null,
    "quadruple.MIN_VALUE",                  MIN_VALUE_STR,
    "min_value",                            MIN_VALUE_STR,
    "quadruple.max_value",                  MAX_VALUE_STR,
    "max_value",                            MAX_VALUE_STR,
    "quadruple.min_normal",                 MIN_NORMAL_STR,
    "min_normal",                           MIN_NORMAL_STR,
    "quadruple.nan",                        "NaN",
    "nan",                                  "NaN",
    "infinity",                             "Infinity",
    "quadruple.negative_infinity",          "-Infinity",
    "QUADRUPLE.NEGATIVE_INFINITY",          "-Infinity",
    "negative_infinity",                    "-Infinity",
    "-infinity",                            "-Infinity",
    "quadruple.positive_infinity",          "Infinity",
    "positive_infinity",                    "Infinity",
    "infinity",                             "Infinity",
    "+infinity",                            "Infinity",

  "// Testing PARTS.decompose()",           null,
  "// Normal numbers in various forms:",    null,
    "-123.456e-079",                        null,
    "-123.456e+079",                        null,
    "+123.456e-079",                        null,
    "+123.456e+079",                        null,
    "123",                                  null,
    "123.456e079",                          null,
    "123.456e79",                           null,
    ".456e79",                              null,
    "123.e79",                              null,
    ".456",                                 null,
    "123.",                                 null,
    "123",                                  null,
    "000123",                               null,

  "// syntax errors:",                      null,
    "Blazing nonsense",                     "error",
    "",                                     "error",
    "123.456.789",                          "error",
    "123e.123",                             "error",
    "-123.321ee345",                        "error",
    ".e345",                                "error",

  "// Testing NumberParser.buildQuadruple", null,
    "000",                                  null,     // Leading zeros
    "20e-1",                                null,     // Buff not empty
    "2",                                    null,

  "// Testing NumberParser.parseMantString()",                  null,
    //12345____0____5___20____5___30____5___40____5___50____5___60
    "9.99999999999999999999999999999999999999999999999999",     null, // Extra digits with rounding up
    "9.99999999990000000000000000000000000000000000000001",     null, // Extra digits without rounding up


  "// 9_999_999_999_999_999_999 Testing NumberParts.parseLong()", null,
    "5.4321e0",                             null,
    "5.4321e000010",                        null,
    "5.4321e10",                            null,
    "1.23456e000000000009999_999_99999999999",    null,
    "1.23456e999999999999999999",           null,
    "1.23456e9999999999999999999",          null,
    "1.23456e99999999999999999999",         null,
    "1.23456e9999999999999999999999999999999999999999", null,

    "1.23456e-999999999999999999",          null,
    "1.23456e-9999999999999999999",         null,
    "1.23456e-99999999999999999999",        null,


  "// Test checking the range of the exponent and checkForMinAndMax", null, // From simple_BD2Q_conversiond_data
    "// exp < Integer.MIN_VALUE, expected 0",                   null,
    "1.23456e-2147483649",                                      null,
    "// exp < Integer.MIN_VALUE, negative, expected -0",        null,
    "-1.23456e-2147483649",                                     null,

    "// exp > Integer.MAX_VALUE, expected Infinity",            null,
    "1.23456e2147483648",                                       "Infinity", // null; // changed 2025-06-07 14:59:40
    "// exp > Integer.MAX_VALUE, negative, expected -Infinity", null,
    "-1.23456e2147483648",                                      "-Infinity",

    "// exp < MIN_EXP10, expected 0",                           null,
    "1.23456e-1646457033",                                      null,
    "// exp < MIN_EXP10, negative, expected -0",                null,
    "-1.23456e-646457033",                                      null,

    "// exp > MAX_EXP10, expected Infinity",                    null,
    "1.23456e646456994",                                        null,
    "// exp > MAX_EXP10, negative, expected -Infinity",         null,
    "-1.23456e646456994",                                       null,

  "// Test subnormal values",                                   null,
    "// exp == MIN_EXP10, value < MIN_VALUE/2, expected 0",     null,
    "1.23456e-646457032",                                       null,
    "// exp == MIN_EXP10, value > MIN_VALUE/2, expected " + MIN_VALUE_STR, null,
    "5.23456e-646457032",                                       null,  // exp == MIN_EXP10, value > MIN_VALUE/2
    "// MIN_VALUE, expected " + MIN_VALUE_STR,                  null,
    MIN_VALUE_STR,                                              null,

  /* */
  "// Test the shifts of mantissa",                             null,
    "// MIN_VALUE * (2^25 + 65535.999) (shift by 103 bits)",    null,
    bdStr(mult(MIN_VALUE, add(powerOfTwo(25), 65535.999))),           null,

  /* */
    "// MIN_VALUE * (2^63 + 65535.999) (shift by 65 bits)",     null,
    bdStr(mult(MIN_VALUE, add(powerOfTwo(63), 65535.999))),     null,
    "// MIN_VALUE * (2^64 - 1) (shift by 65 bits)",             null,
    bdStr(mult(MIN_VALUE, sub(powerOfTwo(64), 1))),             null,
    "// MIN_VALUE * (2^65 - 0.3) (shift by 64 bits)",           null,
    bdStr(mult(MIN_VALUE, sub(powerOfTwo(65), 0.3))),           null,
    "// MIN_VALUE * (2^98 + 65535.999) (shift by 30 bits)",     null,
    bdStr(mult(MIN_VALUE, add(powerOfTwo(98), 65535.999))),     null,
    "// MIN_NORMAL - MIN_VALUE (shift by 1 bit)",               null,
    bdStr(sub(MIN_NORMAL, MIN_VALUE)),                          null,

  /* */
  "// Test rounding a subnormal up to MIN_NORMAL",              null,
    "//MN - (MV * 0.1)",                                        null,
    bdStr(sub(MIN_NORMAL, mult(MIN_VALUE, 0.1))),               null,
    bdStr(sub(MIN_NORMAL, mult(MIN_VALUE, powerOfTwo(-60)))),   null,
    bdStr(add(bd(-1, -1, 0), mult(MIN_VALUE, 0.5))),            null, // (MIN_NORMAL - LSB + 0.5 * LSB

  /* */
  "// Just a few small values", null,
    "// MIN_VALUE * 2 ",        null,   bdStr(mult(MIN_VALUE, 2)),    null,
    "// MIN_VALUE * 4 ",        null,   bdStr(mult(MIN_VALUE, 4)),    null,
    "// MIN_VALUE * 8 ",        null,   bdStr(mult(MIN_VALUE, 8)),    null,
    "// MIN_VALUE * 15",        null,   bdStr(mult(MIN_VALUE, 15)),   null,
    "// MIN_VALUE * 16 ",       null,   bdStr(mult(MIN_VALUE, 16)),   null,

  /* */
  "// A few examples of rounding subnormal values",                       null,
    bdStr(add(bd(HIGH_BIT, 0, 0), mult(MIN_VALUE, sub(0.5, powerOfTwo(-32))))), null, // MIN_NORMAL / 2 + (0.5 - 2^-32) * LSB
    bdStr(add(bd(HIGH_BIT, 0, 0), mult(MIN_VALUE, sub(0.5, powerOfTwo(-60))))), null, // MIN_NORMAL / 2 + (0.5 - 2^-60) * LSB
    bdStr(add(bd(HIGH_BIT, 0, 0), mult(MIN_VALUE, 0.5))),                 null, // MIN_NORMAL / 2 + 0.5 * LSB

  /* */
    "// MIN_VALUE * (2^98 + 16.49)",                            null,
    bdStr(mult(MIN_VALUE, add(BD_2$98, 16.49))),                null, //MIN_VALUE.multiply(BD_2$98.add(bd("16.0"))).toString(),
    "// MIN_VALUE * (2^98 + 16.50)",                            null,
    bdStr(mult(MIN_VALUE, add(BD_2$98, 16.5))),                 null, // MIN_VALUE.multiply(BD_2$98.add(bd("17.0"))).toString(),

  /* */
  "// Touch the boundaries of the range",                       null,
    "// MAX_VALUE + LSB, expected infinity",                    null,
    bdStr(add(MAX_VALUE, LSB_OF_MAX_VALUE)),                    null, //"Infinity",
    "// MAX_VALUE + LSB * 0.500001, expected infinity",         null,
    bdStr(add(MAX_VALUE, mult(LSB_OF_MAX_VALUE, 0.500001))),    null, //"Infinity",
    "// MAX_VALUE + LSB * 0.5, expected Infinity",              null,
    bdStr(add(MAX_VALUE, mult(LSB_OF_MAX_VALUE, 0.5))),         null, //"Infinity",
    "// MAX_VALUE + LSB * 0.499999, expected MAX_VALUE",        null,
    bdStr(add(MAX_VALUE, mult(LSB_OF_MAX_VALUE, 0.499999))),    null, // MAX_VALUE
    "// MAX_VALUE - LSB * 0.499999, expected MAX_VALUE",        null,
    bdStr(sub(MAX_VALUE, mult(LSB_OF_MAX_VALUE, 0.499999))),    null, // MAX_VALUE_STR,
    "// MAX_VALUE - LSB * 0.5, expected MAX_VALUE",             null,
    bdStr(sub(MAX_VALUE, mult(LSB_OF_MAX_VALUE, 0.5))),         null,   // MAX_VALUE_STR,
    "// MAX_VALUE - LSB * 0.500001, expected MAX_VALUE - LSB",  null,
    bdStr(sub(MAX_VALUE, mult(LSB_OF_MAX_VALUE, 0.500001))),    null,   // MAX_VALUE.subtract(MAX_V_LOWEST_BIT).toString(),

  /* */
    "// MIN_VALUE * 0.499999, expected 0",                      null,
    bdStr(mult(MIN_VALUE, 0.499999)),                           null, // "0",
    "// MIN_VALUE * 0.5, expected MIN_VALUE",                   null,
    bdStr(mult(MIN_VALUE, 0.5)),                                null, // MIN_VALUE
    "// MIN_VALUE * 0.500001, expected MIN_VALUE ",             null,
    bdStr(mult(MIN_VALUE, 0.500001)),                           null, // MIN_VALUE

    // 20.12.26 17:04:44 All the code covered

  /**/
  }; // simple_S2Q_conversions_data

  /**
   * A data set for testing {@link Quadruple#assign(BigDecimal value)} or the constructor
   * that accepts a {@code BigDecimal} parameter.<br>
   * Each data sample consists of two strings: a string representation of the value to be assigned,
   * and a string representation of the value that a Quadruple instance is expected to have
   * after the assignment. For any {@code BigDecimal} value, the corresponding {@code Quadruple}
   * value can be evaluated by the testing code, so {@code null}s are used as the second items
   * for all samples.<br>
   * Completely covers the code of the {@code Quadruple.assign(BigDecimal value)} method.<br>
   * The number of items must be even.
   */
  static String[] basic_BD2Q_conversionData = new String[] {

  /* */
//      "// Just to test the test code -- it should not invoke Quadruple.assign(BigDecimal value)", null,
//      "Infinity",               "Error", // "Infinity",
//      "-Infinity",              "Error", // "-Infinity",
//      "NaN",                    "Error", // "NaN",

    "0",                                    null,     // The first test in the Quadruple.assign(BigDecimal value)
    "-0",                                   null,     // There's no -0 in BigDecimal, so it will be just 0

  /* */
//    "// Test both branches in checkForMinAndMax", null,
//      "//Too small exponent: exp < Integer.MIN_VALUE => Error", null, // Can be neither BigDecimal nor Quadruple
//      "1.23456e-2147483649",    "Error",        // Too small exponent for BigDecimal: exp < Integer.MIN_VALUE

    "// exp < MIN_EXP10",                   null,
    "1.23456e-1646457033",                  null,    // exp < MIN_EXP10
    "1.23456e-646457033",                   null,    // exp < MIN_EXP10
    "-1.23456e-646457033",                  null,    // exp < MIN_EXP10

  /* */
    "// exp == MIN_EXP10, value < MIN_VALUE/2, expected 0", null,
    "2.23456e-646457032",                   null,    // exp == MIN_EXP10, value < MIN_VALUE/2
    "// exp == MIN_EXP10, value > MIN_VALUE/2, expected " + MIN_VALUE_STR, null,
    "5.23456e-646457032",                   null,    // exp == MIN_EXP10, value > MIN_VALUE/2

    "// MIN_VALUE * 0.499999",              null,
    bdStr(mult(MIN_VALUE, 0.499999)),       null,

    "// exp = MAX_EXP10, value < MAX_VALUE, expected 1.7616130e+646456993", null,
    "1.7616130e+646456993",                 null,

    "// exp = MAX_EXP10, value = MAX_VALUE, expected " + MAX_VALUE_STR, null,
    MAX_VALUE_STR,                          null,

    "// exp = MAX_EXP10, value > MAX_VALUE, expected Infinity", null,
    bdStr(mult(MAX_VALUE, BD_ONE_Plus)),    null,

    "// exp > MAX_EXP10, expected Infinity", null,
    "1.23456e646456994",                    null,     // exp > MAX_EXP10

    "// exp > MAX_EXP10, expected -Infinity", null,
    "-1.23456e646456994",                   null,     // exp > MAX_EXP10

    "// exp > MAX_EXP10, expected Infinity", null,
    "1.23456e1234567890",                   null,     // exp > MAX_EXP10

    "// exp == Integer.MAX_VALUE, expected Infinity", null,
    "1.23456e2147483647",                   null,     // exp == Integer.MAX_VALUE

//      "// exp > Integer.MAX_VALUE, expected Error", null,
//      "1.23456e2147483648",     "Error",        // exp > Integer.MAX_VALUE // Such BigDecimal can't exist, there's no point in testing it

  "// test raise2toPower for not so great powers", null,
    "9.87654321e30000000",                  null,

  "// test all branches of raise10toPow",   null,
    "123.456e10",                           null,
    "123.456e-10",                          null,
    "123.456e30",                           null,
    "123.456e-30",                          null,

  "// test the corrections of the logarithm inaccuracy", null,
    "1",                                    null,
    "1.79769313486231570000e+308",          null,
    "5.56268464626800300000e-309",          null,
    "2.22507385850720140000e-308",          null,
    "2.22507385850720140000e308",           null,
    "8",                                    null,

  /* */
  "// Test the rounding up the mantissa",   null,
     "1",                                   null, // No carry at all
    "// 0, 0x7fff_ffff_ffff_ffffL, 0xff // Carry to mantLo, no carry to mantHi",                null,
    "1.000000000000000000027105054312137610850186320021749",                                    null,
    "// 0, 0xffff_ffff_ffff_ffffL, 0xff // Carry to mantLo, carry to mantHi, no carry to exp2", null,
    "1.000000000000000000054210108624275221700372640043497",                                    null,
    "// Carry to mantLo, carry to mantHi, carry to exp2, expNegative == false",                 null,
    "1.9999999999999999999999999999999999999999999",                                            null,
    "// Carry to mantLo, carry to mantHi, carry to exp2, expNegative == true",                  null,
    "1.2499999999999999999999999999999999999999999e-1",                                         null,
    "// Rounding up in the case of a subnormal, rounded fractPart == 1: MIN_NORMAL / 2 + 0.5 * LSB", null,
    bdStr(add(bd(HIGH_BIT, 0, 0), mult(MIN_VALUE, 0.5))),                                       null,

  /* */

  "// Test subnormal values",               null,
    "// exp == MIN_EXP10, value < MIN_VALUE/2, expected 0", null,
    "1.23456e-646457032",                   null, //

    "// exp == MIN_EXP10, value > MIN_VALUE/2, expected " + MIN_VALUE_STR, null,
    "5.23456e-646457032",                   null, // MIN_VALUE_STR,  // exp == MIN_EXP10, value > MIN_VALUE/2

    "// MIN_VALUE, expected " + MIN_VALUE_STR, null,
    MIN_VALUE_STR,                          null,

    "// MIN_VALUE * 15 (4 lower bits)",     null,
    bdStr(mult(MIN_VALUE, 15)),             null,

  /* */

    "// MIN_NORMAL = " + MIN_NORMAL_STR,    null,
    MIN_NORMAL_STR,                         null,
    "// MIN_NORMAL - MIN_VALUE ",           null,
    bdStr(sub(MIN_NORMAL, MIN_VALUE)),      null,
    "// MIN_NORMAL / 2",                    null,
    bdStr(div(MIN_NORMAL, 2)),              null,
    "// MIN_VALUE * 2^127",                 null,
    bdStr(mult(MIN_VALUE, powerOfTwo(127))),      null,

  /* */
  "// Just a few small values",             null,
    "// MIN_VALUE ",                        null,
    MIN_VALUE_STR,                          null,
    "// MIN_VALUE * 2 ",                    null,
    bdStr(mult(MIN_VALUE, 2)),              null,
    "// MIN_VALUE * 4 ",                    null,
    bdStr(mult(MIN_VALUE, 4)),              null,
    "// MIN_VALUE * 8 ",                    null,
    bdStr(mult(MIN_VALUE, 8)),              null,
    "// MIN_VALUE * 16 ",                   null,
    bdStr(mult(MIN_VALUE, 16)),             null,

  "// Test the shifts of mantissa",                           null,
    "// MIN_VALUE * (2^25 + 65535.999) (shift by 103 bits)",  null,
    bdStr(mult(MIN_VALUE, add(powerOfTwo(25), 65535.999))),   null,
    "// MIN_VALUE * (2^63 + 65535.999) (shift by 65 bits)",   null,
    bdStr(mult(MIN_VALUE, add(powerOfTwo(63), 65535.999))),   null,
    "// MIN_VALUE * (2^64 - 1) (shift by 65 bits)",           null,
    bdStr(mult(MIN_VALUE, sub(powerOfTwo(64), 1))),           null,
    "// MIN_VALUE * (2^65 - 0.3) (shift by 64 bits)",         null,
    bdStr(mult(MIN_VALUE, sub(powerOfTwo(65), 0.3))),         null,
    "// MIN_VALUE * (2^98 + 65535.999) (shift by 30 bits)",   null,
    bdStr(mult(MIN_VALUE, add(powerOfTwo(98), 65535.999))),   null,


  "// Test rounding a subnormal up to MIN_NORMAL",            null,
    "// MN - (MV * 0.1)",                                     null,
    bdStr(sub(MIN_NORMAL, mult(MIN_VALUE, 0.1))),             null,

      /**/
  }; // static String[] basic_BD2Q_conversionData = new String[] {

  /**
   * A data set for testing {@link Quadruple#assign(double value)} or the constructor that
   * accepts a {@code double} parameter.<br>
   * Each data sample consists of two strings: a string representation of the value to be assigned,
   * and a string representation of the value that a Quadruple instance is expected to have
   * after the assignment. For any {@code double} value, the corresponding {@code Quadruple} value
   * can be evaluated by the testing code, thus the data contains {@code null}s instead of
   * expected values.<br>
   * Completely covers the code of the {@code Quadruple.assign(double value)} method.<br>
   * The number of items must be even.
   */
  static String[] basic_d2Q_conversionData = new String[] {
    "0",                                              null,
    "-0",                                             null,
    str(Double.NaN),                                  null,
    str(Double.NEGATIVE_INFINITY),                    null,
    str(Double.POSITIVE_INFINITY),                    null,

    str(Double.MAX_VALUE),                            null,
    str(Double.MAX_VALUE - Double.MAX_VALUE * 1e-16), null,
    str(1.2345e25),                                   null,
    str(5.4351e-25),                                  null,
    str(Double.MIN_NORMAL),                           null,
    str((Double.MIN_NORMAL + Double.MIN_NORMAL * 1e-16)), null,
    str((Double.MIN_NORMAL - Double.MIN_NORMAL / 4)), null,
    str(Double.MIN_NORMAL / 256),                     null,
    str(Double.MIN_VALUE * 2),                        null,
    str(Double.MIN_VALUE * 10),                       null,
    str(Double.MIN_VALUE),                            null,

    str(-Double.MAX_VALUE),                           null,
    str(-(Double.MAX_VALUE - Double.MAX_VALUE * 1e-16)), null,
    str(-1.2345e25),                                  null,
    str(-5.4351e-25),                                 null,
    str(-Double.MIN_NORMAL),                          null,
    str(-(Double.MIN_NORMAL + Double.MIN_NORMAL * 1e-16)), null,
    str(-(Double.MIN_NORMAL - Double.MIN_NORMAL / 4)), null,
    str(-Double.MIN_NORMAL / 256),                    null,
    str(-Double.MIN_VALUE * 2),                       null,
    str(-Double.MIN_VALUE * 10),                      null,
    str(-Double.MIN_VALUE),                           null,
  }; // static String[] basic_d2Q_conversionData = new String[] {

  /**
   * A data set for testing {@link Quadruple#assign(long value)} or the constructor that
   * accepts a {@code long} parameter.<br>
   * Each data sample consists of two strings: a string representation of the value to be assigned,
   * and a string representation of the value that a Quadruple instance is expected to have
   * after the assignment. For any {@code long} value, the corresponding {@code Quadruple} value
   * can be evaluated by the testing code, thus the data contains {@code null}s instead of
   * expected values.<br>
   * Completely covers the code of the {@code Quadruple.assign(long value)} method.<br>
   * The number of items must be even.
   */
  static String[] basic_l2Q_conversionData = new String[] {
    "// Long.MAX_VALUE",                              null,
    String.valueOf(Long.MAX_VALUE),                   null, // 9223372036854775807
    "1234567890",                                     null,
    "0",                                              null,
    "-1234567890",                                    null,
    "// Long.MIN_VALUE",                              null,
    String.valueOf(Long.MIN_VALUE),                   null, //  -9223372036854775808
  }; // static String[] basic_L2Q_conversionData = new String[] {

  /**
   * A data set for testing {@link Quadruple#add(Quadruple)} and
   * {@link Quadruple#add(Quadruple, Quadruple)} methods.<br>
   * Each data sample consists of three strings: two strings representing the values of the operands,
   * and the expected result of their addition.<br>
   * For any pair of the operands, the corresponding result can be evaluated by the testing code,
   * thus the data contains {@code null}s instead of the values of the expected results.<br>
   * Completely covers the code of the {@code Quadruple.add(Quadruple)} method.<br>
   * The number of items must be divisible by 3.
   */
  static String[] basicAdditionData = new String[] {
/* */
// all possible combinations of special values like NaN and Infinity and ordinary numeric values
// are now generated by DataGenerators.CartesianSquares

// Test addition of ordinary numbers
  "// Both are subnormal",                  null,                                   null,
    bdStr(0, 7, 0),                         bdStr(0, 8, 0),                         null,
    bdStr(mult(MIN_NORMAL, 0.6)),           bdStr(mult(MIN_NORMAL, 0.3)),           null,
    bdStr(mult(MIN_NORMAL, 0.7)),           bdStr(mult(MIN_NORMAL, 0.6)),           null,
    bdStr(sub(MIN_NORMAL, MIN_VALUE)),      bdStr(mult(MIN_NORMAL, 0.8)),           null,

  "// one of two is subnormal",             null,                                   null,
    bdStr(mult(MIN_NORMAL, 2)),             bdStr(mult(MIN_NORMAL, 0.5)),           null,
    bdStr(mult(MIN_NORMAL, 0.5)),           bdStr(mult(MIN_NORMAL, 2)),             null,

  "// Exponent difference < 64",            null,                                   null,
    bdStr(mult(MIN_NORMAL, 256)),           bdStr(mult(MIN_NORMAL, 0.5)),           null,
    bdStr(mult(MIN_NORMAL, 0.5)),           bdStr(mult(MIN_NORMAL, 256)),           null,

  "// Exponent difference == 64",           null,                                   null,
    MIN_NORMAL_STR,                         bdStr(MIN_VALUEx2$64),                  null,
    bdStr(MIN_VALUEx2$64),                  MIN_NORMAL_STR,                         null,
    bdStr(mult(MIN_NORMAL, BD_2$64)),       bdStr(mult(MIN_NORMAL, 0.75)),          null,

  "// Exponent difference == 128",          null,                                   null,
    bdStr(mult(MIN_NORMAL, BD_2$128, 1.2)), bdStr(mult(MIN_NORMAL, 0.7)),           null,

  "// lz == 64",                            null,                                   null,
    bdStr(div(MIN_VALUEx2$64, BD_2)),       MIN_NORMAL_STR,                         null,

    // "// shift + lz > 128", null, null,
    bdStr(mult(MIN_NORMAL, BD_2$98)),       bdStr(div(MIN_VALUEx2$64, BD_2)),       null,
    // "// shift < 64", null, null,
    bdStr(mult(MIN_NORMAL, BD_2$32)),       bdStr(mult(MIN_NORMAL, 0.8765)),        null,
    // "// shift == 64", null, null,
    bdStr(mult(MIN_NORMAL, BD_2$64)),       bdStr(mult(MIN_NORMAL, 0.8765)),        null,
    // "// shift > 64", null, null,
    bdStr(mult(MIN_NORMAL, BD_2$98)),       bdStr(mult(MIN_NORMAL, 0.8765)),        null,
    // "// 1 shifted out from lower (nonexistent) bit and added as carry ", null, null,
    bdStr(mult(MIN_NORMAL, BD_2$32)),       bdStr(sub(MIN_NORMAL, MIN_VALUE)),      null,

// "// Overflow adding mantissas",          null,                                   null,
    bdStr(mult(sub(MIN_NORMAL, MIN_VALUE), 2)), bdStr(sub(MIN_NORMAL, MIN_VALUE)),  null,

// "// Overflow from higher word adding mantissas", null, null,
    bdStr(mult(MIN_NORMAL, 1.5)),           bdStr(sub(MIN_NORMAL, mult(MIN_VALUE, 256))), null,

// Both are normal
  "// Both are normal, same exponent",      null,                                   null,
    "1.0",                                  "1.0",                                  null, // carry2 == 0
    bdStr(sub(2, BD_2$_128)),               bdStr(add(1, BD_2$_64)),                null, // (carry2 != 0,  ++mantLo != 0) //
    bdStr(sub(2, BD_2$_128)),               "1",                                    null, // (carry2 != 0 && ++mantLo == 0)
    bdStr(mult(MAX_VALUE, 0.5001)),         bdStr(mult(MAX_VALUE, 0.5001)),         null, // ++exponent == EXP_INF

  "// Both are normal, different exponents", null,                                  null,
    "1e15",                                 "3.1415926536",                         null,
    "3.141592654",                          "1000",                                 null,
    "1e150",                                "3.1415926536",                         null,  // exponent difference > 129
  "// Exp difference == 129",               null,                                   null,
    bdStr(BD_2$128),                        "0.5",                                  null,  // exponent difference == 129, 1 in the LSB must appear
    bdStr(add(BD_2$128, sub(BD_2$64, 1))),  "0.6",                                  null,  // exponent difference == 129, carry from lower words
    bdStr(sub(BD_2$128, 0.5)),              "0.4",                                  null,  // exponent difference == 129, overflow adding mantissas
  "// Exp diff == 128",                     null,                                   null,
    "1.45678901234567890",                  bdStr(mult(1.23456789, BD_2$_128)),     null,  // 64 < exponent difference < 129
  "// Exp diff == 96",                      null,                                   null,
    bdStr(sub(BD_2$128, 1)),                bdStr(sub(BD_2$32, 0.5)),               null,   // 64 < exponent difference < 129
  "// Exp diff == 64",                      null,                                   null,
    bdStr(sub(BD_2$128, 1)),                bdStr(sub(BD_2$64, 3)),                 null,   // exponent difference < 129, no carry from lower words
  "// Exp diff == 1",                       null,                                   null,
    bdStr(sub(BD_2$128, 0.5)),              bdStr(div(sub(BD_2$128, 0.5), 2)),      null,             // exponent difference < 129, carry from lower words
    "1.2e+646456993",                       "0.562e+646456993",                     null,  // Exponent overflow: MAX_VALUE = 1.761613051683963353207493149791840285665e+646456993

// Test addition with different signs == subtraction
// Actually these calculations are performed by subtractUnsigned() and are tested more thoroughly as part of testing subtractions
  "// Different signs",                     null,                                   null,
    "-1.25",                                "1.25",                                 null,
    "1.25",                                 "-1.25",                                null,

// Both are subnormal
    MIN_VALUEx2$64_STR,                     "-"+MIN_VALUEx2$64_STR,                 null,
    MIN_VALUEx2$64_STR,                     bdStr(mult(MIN_VALUEx2$64, -2)),        null,
    "-" + MIN_VALUEx2$64_STR,               bdStr(mult(MIN_VALUEx2$64, 2)),         null,
    bdStr(mult(MIN_VALUEx2$64, 3)),         bdStr(mult(MIN_VALUEx2$64, -1)),        null,
    bdStr(mult(MIN_VALUEx2$64, -3)),        MIN_VALUEx2$64_STR,                     null,
    MIN_VALUEx2$64_STR,                     "-"+bdStr(0, -1, 0),                    null,

// Normal - subnormal
    MIN_NORMAL_STR,                         bdStr(add(div(MIN_NORMAL, -8), MIN_VALUE)), null,

    MIN_NORMAL_STR,                         "-" + MIN_VALUE_STR,                    null,
    MIN_NORMAL_STR,                         bdStr(div(MIN_NORMAL, -2)),             null,
    bdStr(mult(MIN_NORMAL, 2.55)),          bdStr(div(MIN_NORMAL, -2)),             null,
    bdStr(mult(MIN_NORMAL, 2.55)),          bdStr(mult(MIN_NORMAL, -0.125)),        null,

  // Exponent difference > 128
    bdStr(mult(MIN_NORMAL, BD_2$98, 1.2345)),   bdStr(mult(MIN_VALUE, BD_2$64, -1.9876543)),          null,
    bdStr(mult(MIN_NORMAL, BD_2$98, 1.2345)),   bdStr(mult(MIN_VALUE, sub(BD_2$64, 1), -0.9876543)),  null,
    bdStr(mult(MIN_NORMAL, BD_2$64, 12.2345)),  bdStr(mult(MIN_VALUE, sub(BD_2$98, 1), -1.9876543)),  null,
    bdStr(mult(MIN_NORMAL, BD_2$64)),           bdStr(mult(MIN_VALUE, sub(BD_2$98, 1), -1.9876543)),  null,

    // Shift == 128, minuend == 0
    bdStr(mult(MIN_NORMAL, BD_2$128)),      bdStr(true, -1, -1, 0),                 null, // (MIN_NORMAL * 2^128) + (-(MIN_NORMAL - MIN_VALUE))
    // Shift == 128, minuend != 0
    bdStr(mult(MIN_NORMAL, BD_2$128, 1.2345)), bdStr(true, -1, -1, 0),              null, // (MIN_NORMAL * 2^128 * 1.2345) + (-(MIN_NORMAL - MIN_VALUE))
    bdStr(mult(MIN_NORMAL, BD_2$32)),       bdStr(true, (long)-1 >>> 1, -1, 0),     null, // (M_N * 2^32) + (-(M_N / 2 - M_V)) // (-7FFF...FFFF, 0)
    bdStr(mult(MIN_NORMAL, BD_2$32, 1.5)),  bdStr(true, (long)-1 >>> 1, -1, 0),     null, // (M_N * 2^32 * 1.5) + (-(M_N / 2 - M_V)) // (-7FFF...FFFF, 0)

  // Both are normal
  // One of the operands is too small to affect the result
    "12345",                                "-12346e-45",                           null,

  "// Exponent difference == 129",          null,                                   null,
    bdStr(mult(24691, add(BD_2$128, 2))),   "-12345",                               null,
    bdStr(mult(BD_2$128, 2)),               "-1",                                   null, // 2^129 - 1 = 1,9999.. * 2^128

  // Same exponent
    "12345",                                "-12346",                               null,
    "-12346",                               "12345.00000000000000001",              null, // Borrow from lower word
  // Difference in lower word:

    bdStr(add(1, mult(0x1_ABCD, BD_2$_128))), "-1",                                 null, // Result must have ABCD in highest bits

  "// Difference becomes subnormal",        null,                                   null,
    bdStr(mult(MIN_NORMAL, 5.000000000012345)), bdStr(mult(MIN_NORMAL, -5)),        null,
  /**/
  }; // static String[] basicAdditionData = new String[] {

  /**
   * A data set for testing or debugging the subtraction at some specific values
   * that may be of a special interest.<br>
   * Each data sample consists of three strings: two strings representing the values of the operands,
   * and the expected result of their subtraction.<br>
   * Is not used in the current version.<br>
   * The number of items must be divisible by 3.
   */
  static String[] specialSubtractionData = new String[] {
//   // TO DO 20.10.30 18:59:53 Wrong LSB
//      "// TO DO 20.10.30 18:59:53 Wrong LSB", null, null,
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0082),  // exp. diff. == 130
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,
//
//   // TO DO 20.10.30 18:59:53 Wrong LSB
//      "// TO DO 20.10.30 18:59:53 Wrong LSB", null, null,
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),  // exp. diff. == 129
//      bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,
//
//      // TO DO 20.10.30 18:59:53 Wrong LSB
//      "// TO DO 20.10.30 18:59:53 Wrong LSB", null, null,
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),  // exp. diff. == 129
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,
//
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),  // exp. diff. == 129
//      bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,
// DONE 20.11.01 15:49:15 It was insufficient precision of estimating half the LSB
// in AuxMethods.findMantValues()
  }; // static String[] specialSubtractionData = new String[] {

  /**
   * A data set for testing {@link Quadruple#subtract(Quadruple subtrahend)} and
   * {@link Quadruple#add(Quadruple minuend, Quadruple subtrahend)} methods.<br>
   * Each data sample consists of three strings: string representations of the minuend
   * and the subtrahend, and the expected value of their difference.<br>
   * For any pair of the operands, the corresponding result can be evaluated by the testing code,
   * thus the data contains {@code null}s instead of the values of the expected results.<br>
   * Completely covers the code of the {@code Quadruple.subtract(Quadruple subtrahend)} method.<br>
   * The number of items must be divisible by 3.
   */
  static String[] basicSubtractionData = new String[] {

  /* */

  // Special cases in subtract() now are covered by the data
  // generated by DataGenerators.CartesianSquare.specialValuesForSubtraction

  // Both are regular numbers
  // Different signs. Actually performed by methods, used by addition.
  // Comprehensive test data included in simple_additions_data
    "2.5",                                  "-3.75",                                null,
    "-2.5",                                 "3.75",                                 null,
    "3.75",                                 "-2.5",                                 null,
    "-3.75",                                "2.5",                                  null,

  // Same sign. This is the only case when proper subtraction is performed (by subtractUnsigned()).
    "2.5",                                  "3.75",                                 null,
    "-2.5",                                 "-3.75",                                null,
    "3.75",                                 "2.5",                                  null,
    "-3.75",                                "-2.5",                                 null,

  // Testing subtractUnsigned()
  // operands are equal in magnitude:
    "2.5",                                  "2.5",                                  null,
    "-3.75",                                "-3.75",                                null,

  // Swapping minuend and subtrahend, when necessary -- already tested by "// Same sign..." a few lines above

  // Both minuend and subtrahend are normal.
  // subtractNormals(minuendLo, minuendHi, lesserExp);
  // exponent difference > 130, subtrahend ignored

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0083),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, 0x8000_0000), null, // exp. diff. == 131

    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, 0x8000_0083),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 131

  // exponent difference == 130, subtracts 1/2 LSB if minuend == 2^n and subtrahend > 1/4 LSB
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0082),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 130


    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0082),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null, // exp. diff. == 130

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0082),
    bdStr(0x1000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 130

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0082),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, 0x8000_0000), null,  // exp. diff. == 130

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0082),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,  // exp. diff. == 130

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0082),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,  // exp. diff. == 130

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0082),
    bdStr(0x1000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,  // exp. diff. == 130

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0082),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, 0x8000_0000), null,  // exp. diff. == 130

  // exponent difference == 129, subtrahend >= 1/2 LSB of the minuend.
  // if minuend = 2^n,
  //  result =   1.ff.ff * 2^(n-1), if subtrahend <= 3/4 LSB.
  //         or   1.ff.fE * 2^(n-1), if subtrahend > 3/4 LSB.
  // if minuend != 2^n,
  //  result = minuend,         if subtrahend = 1/2 LSB.
  //         or minuend - LSB,   if subtrahend > 1/2 LSB.
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0xC000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),
    bdStr(0xC000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129

    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0010L, 0x8000_0081),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, 0x8000_0000), null, // exp. diff. == 129

  // Borrow from high word
    bdStr(0x0000_0000_0000_0123L, 0x0000_0000_0000_0000L, 0x8000_0081),
    bdStr(0xC000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // exp. diff. == 129


  // Data for subtractDifferentExp() coverage, 20.04.20 19:23:49
  // both operands are normal, 0 < exponent difference < 129,

  // setUnity, shift < 64
    bdStr(0x0000_0000_0000_0123L, 0x0000_0000_0000_0000L, 0x8000_0039),
    bdStr(0xC000_0000_0000_0345L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // shift = 63

  // setUnity, shift = 64
    bdStr(0x0000_0000_0000_0123L, 0x0000_0000_0000_0000L, 0x8000_0040),
    bdStr(0xC000_0000_0000_0345L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // shift = 64

   // setUnity, shift > 64
    bdStr(0x0000_0000_0000_0123L, 0x0000_0000_0000_0000L, 0x8000_0041),
    bdStr(0xC000_0000_0000_0345L, 0x0000_0000_0000_0000L, 0x8000_0000), null, // shift = 65

  // subtractMant(), borrow == 0, --minuendLo == -1)", null, null,
    bdStr(0x1000_0000_0000_0123L, 0xFFFF_0000_0000_0000L, 0x8000_0010),
    bdStr(0xC000_0000_0000_0345L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

   // subtractMant(), (borrow != 0 && --minuendLo == -1)", null, null,
    bdStr(0x1000_0000_0000_0123L, 0x0000_0000_0000_0000L, 0x8000_0010),
    bdStr(0xC000_0000_0000_0345L, 0x0000_0000_0000_FFFFL, 0x8000_0000), null,

   // subtractMant(), borrow != 0, --minuendLo != -1), Long.compareUnsigned(mantLo, minuendLo) < 0", null, null,
    bdStr(0x1000_0000_0000_0123L, 0xFFFF_0000_0000_0000L, 0x8000_0010),
    bdStr(0xC000_0000_0000_0345L, 0x0000_0000_0000_FFFFL, 0x8000_0000), null,

   // subtractMant(), borrow != 0, --minuendLo != -1), Long.compareUnsigned(mantLo, minuendLo) > 0", null, null,
    bdStr(0x1000_0000_0000_0123L, 0x0000_0010_0000_0000L, 0x8000_0010),
    bdStr(0xC000_0000_0000_0345L, 0x1000_0000_0000_FFFFL, 0x8000_0000), null,

  // subtractMant(), (borrow2 != 0 && --minuendHi == -1)", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0010_0000_0000L, 0x8000_0010),
    bdStr(0xC000_0000_0000_0345L, 0x1000_0000_0000_FFFFL, 0x8000_0000), null,

  // subtractMant(), (borrow2 != 0 && --minuendHi != -1). subtractMant() covered completely", null, null,
    bdStr(0x0000_1000_0000_0000L, 0x0000_0010_0000_0000L, 0x8000_0010),
    bdStr(0xC000_0000_0000_0345L, 0x1000_0000_0000_FFFFL, 0x8000_0000), null,

  // Continue with subtractDifferentExp().     borrow != 0, shift == 1, normalizeShiftedByOneBit(shiftedOutBits);
    bdStr(0x8000_1000_0000_0000L, 0x0000_0010_0000_0000L, 0x8000_0001),
    bdStr(0x3000_0000_0000_0345L, 0x1000_0000_0000_FFFFL, 0x8000_0000), null,

  // (mantHi | mantLo) == 0, shiftedOutBits == HIGH_BIT, shift = 1
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,

   // (mantHi | mantLo) == 0, shiftedOutBits == HIGH_BIT, shift > 1
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0002L, 0x8000_0000), null,

  // (mantHi | mantLo) == 0, shiftedOutBits != HIGH_BIT, shift = 1
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  // (mantHi | mantLo) == 0, shiftedOutBits == 0, shift > 1,
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  // (mantHi | mantLo) == 0, shiftedOutBits == HIGH_BIT/2, shift > 1, subtractDifferentExp() covered completely
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,

  // continue with subtractNormals(). No return on different exps, calls subtractSameExp(), borrow propagates to the higher word
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0002), null,

  // subtractSameExp(), no borrow propagates to the higher word, subtractSameExp() covered completely.
    bdStr(0x4000_0000_0000_0000L, 0x0130_0000_0000_0000L, 0x8000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0002), null,

  // normalizeShiftedByOneBit(). lz == 0, shiftedOutBits == 0
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  // normalizeShiftedByOneBit(). lz == 0, shiftedOutBits != 0, no borrow from lower word to higher", null, null,
    bdStr(0x1230_0000_0000_0000L, 0x2000_0000_0000_0000L, 0x8000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,

  // normalizeShiftedByOneBit(). lz == 0, shiftedOutBits != 0, Borrow from lower word to higher, no borrow from the implicit unity", null, null,
    bdStr(0x1230_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,

  // normalizeShiftedByOneBit(). lz == 0, shiftedOutBits != 0, Borrow from the implicit unity != 0, remains normal", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,

  // normalizeShiftedByOneBit(). lz == 0, shiftedOutBits != 0, Borrow from the implicit unity != 0, becomes subnormal", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x0000_0001), null,

  // normalizeShiftedByOneBit(). lz != 0, shiftedOutBits == 0", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0xE000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  // normalizeShiftedByOneBit(). lz != 0, shiftedOutBits != 0, borrow doesn't propagate to the higher word ", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0xE000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000), null,

  // normalizeShiftedByOneBit(). lz != 0, shiftedOutBits != 0, borrow propagates to the higher word ", null, null,
  // 20.04.21 19:59:58 normalizeShiftedByOneBit() covered completely.
    bdStr(0x0000_0000_0000_1000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0xE000_0000_0000_0000L, 0x1000_0000_0000_0001L, 0x8000_0000), null,

  "// normalizeShiftedByAFewBits(), shiftedOutBits == HIGH_BIT, shiftedOutBits < 0x4000_0000_0000_0000L, no borrow propagating to high word", null, null,
    bdStr(0x0000_0000_0000_1000L, 0x0200_0000_0000_0000L, 0x8000_0003),
    bdStr(0xE000_0000_0000_0000L, 0x1000_0000_0000_0004L, 0x8000_0000), null,

  "// normalizeShiftedByAFewBits(), shiftedOutBits != HIGH_BIT, shiftedOutBits >= 0x4000_0000_0000_0000L, no borrow propagating to high word", null, null,
    bdStr(0x0000_0000_0000_1000L, 0x0200_0000_0000_0000L, 0x8000_0003),
    bdStr(0xE000_0000_0000_0000L, 0x1000_0000_0000_0003L, 0x8000_0000), null,

  "// normalizeShiftedByAFewBits(), shiftedOutBits != HIGH_BIT, shiftedOutBits >= 0x4000_0000_0000_0000L, borrow propagating to high word", null, null,
    bdStr(0x0000_0000_0000_1000L, 0x0000_0000_0000_0000L, 0x8000_0003),
    bdStr(0xE000_0000_0000_0000L, 0x1000_0000_0000_0003L, 0x8000_0000), null,

  "// normalizeShiftedByAFewBits(), shiftedOutBits != HIGH_BIT && shiftedOutBits < 0x4000_0000_0000_0000L, shiftedOutBits <= 0xC000_0000_0000_0000L", null, null,
    bdStr(0x0000_0000_0000_1000L, 0x0000_0000_0000_0000L, 0x8000_0003),
    bdStr(0xE000_0000_0000_0000L, 0x1000_0000_0000_0006L, 0x8000_0000), null,

  "// normalizeShiftedByAFewBits(), shiftedOutBits != HIGH_BIT && shiftedOutBits < 0x4000_0000_0000_0000L, shiftedOutBits > 0xC000_0000_0000_0000L", null, null,
  "// normalizeShiftedByAFewBits() covered completely", null, null,
    bdStr(0x0000_0000_0000_1000L, 0x0000_0000_0000_0000L, 0x8000_0003),
    bdStr(0xE000_0000_0000_0000L, 0x1000_0000_0000_0001L, 0x8000_0000), null,


  //normalize(int shift), Integer.compareUnsigned(exponent, shift > 0) -- Remains normal", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0001),
    bdStr(0xE000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  //normalize(int shift), Integer.compareUnsigned(exponent, shift <= 0) -- Becomes subnormal, exponent > 1", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0002),
    bdStr(0xE000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0001), null,

  //normalize(int shift), Integer.compareUnsigned(exponent, shift <= 0) -- Becomes subnormal, exponent <= 1", null, null,
  //normalize(int shift) covered completely
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x0000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0000L, 0x0000_0001), null,

  // Test data for subtractSubnormalFromNormal()
  "//  shift + lz > 129)", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x8000_0034),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0000L, 0x0000_0000), null,

  "//  shift + lz > 129)", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x0000_0034),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0000L, 0x0000_0000), null,

  "//  shift + lz == 129)", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x0000_0033),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0000L, 0x0000_0000), null,

  "// shift + lz < 129), shiftedOutBits > 1/2 of LSB", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x0000_0004),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0006L, 0x0000_0000), null, // shift = exp - 1 = 49

  "// shift + lz < 129), shiftedOutBits == 1/2 of LSB", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x0000_0004),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0004L, 0x0000_0000), null,  // shift = exp - 1 = 49

  "// shift + lz < 129), shiftedOutBits < 1/2 of LSB", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0010_0000_0000_0000L, 0x0000_0004),
    bdStr(0x0000_0000_0000_0000L, 0x0001_0000_0000_0003L, 0x0000_0000), null,   // shift = exp - 1 = 49

  "// shift + lz < 128, borrow propagated to implicit unity, shift = 1", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0002),
    bdStr(0x0000_0000_0000_0000L, 0x0001_F000_0000_0000L, 0x0000_0000), null,

  "// shift + lz < 128, borrow propagated to implicit unity, shift > 1", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0031),
    bdStr(0x0000_0000_0000_0000L, 0x0001_F000_0000_0000L, 0x0000_0000), null,

  "// shift + lz < 128, borrow propagated to implicit unity, shift = 0", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0001_F000_0000_0000L, 0x0000_0000), null,

  "// shift + lz < 128, borrow propagated to implicit unity, shift = 0, becomes subnormal", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0002L, 0x0000_0001),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0003L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) != 0", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0223_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0000L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) == 0, shiftedOutBits != HIGH_BIT, shiftedOutBits <= 0x40...0L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0001L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) == 0, shiftedOutBits != HIGH_BIT, shiftedOutBits <= 0x40...0L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0004L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) == 0, shiftedOutBits == HIGH_BIT", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0008L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) == 0, shiftedOutBits != HIGH_BIT, shiftedOutBits > 0x40...0L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0005L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) == 0, shiftedOutBits != HIGH_BIT, shiftedOutBits > 0x40...0L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0006L, 0x0000_0000), null,

  "// shift + lz < 128, borrow not propagated to implicit unity, (mantHi | mantLo) == 0, shiftedOutBits != HIGH_BIT, shiftedOutBits > 0x40...0L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0005),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, 0x0000_0000), null,
  // subtractSubnormalFromNormal() completely covered 20.04.26 17:39:51

  // 20.04.26 17:40:01 Test data for subtractSubnormals()
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, 0x0000_0000), // no borrow propagating from the lower word to the higher
    bdStr(0x0000_0000_0000_0000L, 0x0123_0000_0000_0000L, 0x0000_0000), null,

    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, 0x0000_0000), // no borrow propagating from the lower word to the higher
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, 0x0000_0000), null,

  // 20.04.26 18:26:37 Test data for shiftMantLeft(). it's invoked by normalize()
  // that's invoked by subtractSameExp(long minuendLo, long minuendHi)
  "// shift >= 128", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, 0x8000_0000),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  "// shift >= 64", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0007L, 0x8000_0000), // 126
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

  "// shift < 64", null, null,
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, 0x8000_0000), // 36
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, 0x8000_0000), null,

  //20.10.04 14:41:32
  // Test rounding for cases where the result is near N + 0.5

  // Subtract normal from normal with different exponents
    "// difference = n * LSB, mantLo = 0x...1201L, err = 0", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 164),
    bdStr(0x0000_0000_0200_0000L, 0x0000_0000_0000_0000L, 100), null,
    "// difference = (n - 0.49999...) * LSB, rounding up, mantLo = 0x...1201L, err = +0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 164),
    bdStr(0x0000_0000_0200_0000L, 0x7FFF_FFFF_FFFF_FFFFL, 100), null,
    "// difference = (n - 0.5) * LSB, rounding up, mantLo = 0x...1201L, err = +0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 164),
    bdStr(0x0000_0000_0200_0000L, 0x8000_0000_0000_0000L, 100), null,
    "// difference = (n - 0.500...001) * LSB, rounding down, mantLo = 0x...1200L, err = -0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 164),
    bdStr(0x0000_0000_0200_0000L, 0x8000_0000_0000_0001L, 100), null,
    // "$_Stop_$", null, null, // OK

  // Subtract subnormal from normal, with normal difference
    "// difference = n * LSB, mantLo = 0x...1201L, err = 0", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x0000_0000_0000_0000L, 0), null,
    "// difference = (n - 0.49999...) * LSB, rounding up, mantLo = 0x...1201L, err = +0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x7FFF_FFFF_FFFF_FFFFL, 0), null,
    "// difference = (n - 0.5) * LSB, rounding up, mantLo = 0x...1201L, err = +0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x8000_0000_0000_0000L, 0), null,
    "// difference = (n - 0.500...001) * LSB, rounding down, mantLo = 0x...1200L, err = -0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x8000_0000_0000_0001L, 0), null,
    //       "$_Stop_$", null, null, // OK

  // Subtract subnormal from normal, with subnormal difference
    "// difference = n * LSB, mantLo = 0x...1201L, err = 0", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, 1),
    bdStr(0x0000_0000_1000_0000L, 0x0000_0000_0000_0000L, 0), null,
    "// difference = (n - 0.49999...) * LSB, rounding up, mantLo = 0x...1201L, err = +0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x7FFF_FFFF_FFFF_FFFFL, 0), null,
    "// difference = (n - 0.5) * LSB, rounding up, mantLo = 0x...1201L, err = +0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x8000_0000_0000_0000L, 0), null,
    "// difference = (n - 0.500...001) * LSB, rounding down, mantLo = 0x...1200L, err = -0.5 * LSB", null, null,
    bdStr(0x1200_1200_1200_1201L, 0x1200_1200_1200_1201L, 65),
    bdStr(0x0000_0000_0200_0000L, 0x8000_0000_0000_0001L, 0), null,

  /**/
  }; // static String[] basicSubtractionData = new String[] {

  /**
   * A data set for testing {@link Quadruple#multiply(Quadruple factor)} and
   * {@link Quadruple#multiply(Quadruple factor1, Quadruple factor2)} methods.<br>
   * Each data sample consists of three strings: string representations of the two factors,
   * and the expected value of their product.<br>
   * For most of the pairs of the operands, the corresponding result can be evaluated
   * by the testing code, thus the data in such cases contains {@code null}s
   * instead of the values of the expected results. Exceptions are the cases
   * where the product is subnormal, since subnormal values contain fewer bits
   * in the mantissa than normal values, so the relative error of subnormal
   * values can be much higher than the standard error threshold.<br>
   * Completely covers the code of the {@code Quadruple.multiply(Quadruple factor)} method.<br>
   * The number of items must be divisible by 3.
   */
  static String[] basicMultiplicationData = new String[] {
/* */
    // Cover private Quadruple multUnsigned(Quadruple factor)
    // productExponent > EXPONENT_OF_MAX_VALUE
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2), null,

    // productExponent == EXPONENT_OF_MAX_VALUE
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1), null,

    // productExponent == EXPONENT_OF_MAX_VALUE
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1), null,

    // productExponent < EXPONENT_OF_MAX_VALUE
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1), null,

/* */
    // productExponent < -129 (-130)", null, null,
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, (EXPONENT_OF_ONE/2) - 64),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, (EXPONENT_OF_ONE/2) - 65), null,

    // productExponent == -129", null, null,
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, (EXPONENT_OF_ONE/2) - 64),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, (EXPONENT_OF_ONE/2) - 64), null,

    // productExponent == -129", null, null,
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, (EXPONENT_OF_ONE/2) - 64),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, (EXPONENT_OF_ONE/2) - 64), MIN_VALUE_STR,

    // productExponent > -129 (-128)", null, null,
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, (EXPONENT_OF_ONE/2) - 64),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, (EXPONENT_OF_ONE/2) - 63), MIN_VALUE_STR,

/* */
    // after normalizeAndUnpack(factor, productExponent, factor1, factor2) ", null, null,
    // productExponent < -129 (-130)", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x3FFF_FFFF_FFFF_FFFFL, 0x0000_0000),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE - 64), null,

    // productExponent == -129", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x3FFF_FFFF_FFFF_FFFFL, 0x0000_0000),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE - 63), MIN_VALUE_STR,

    // productExponent == -129", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x3FFF_FFFF_FFFF_FFFFL, 0x0000_0000),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE - 63), null,

    // after normalizeProduct(product, productExponent, isRoundedUp);
    // productExponent > EXPONENT_OF_MAX_VALUE", null, null,
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1), null,

    // productExponent == EXPONENT_OF_MAX_VALUE
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1), null,

    // after packBufferToMantissa(product);
    "// productExponent <= 0, subnormal", null, null,
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2), null,

    "// productExponent > 0, normal", null, null,
    bdStr(0x0000_0000_1230_0000L, 0x0123_0000_0000_0000L, EXPONENT_OF_ONE / 2 + 1),
    bdStr(0x0000_0000_0000_0000L, 0x1230_0000_0000_0007L, EXPONENT_OF_ONE / 2 + 1), null,
    // private Quadruple multUnsigned(Quadruple factor) covered completely =============

  // Test data for normalizeAndUnpack(factor, productExponent, factor1, factor2);
    "// this is subnormal", null, null,
    bdStr(0x0000_0000_1000_1230L, 0x0000_0000_0000_0000L, 0),
    bdStr(0x0000_0000_0000_0000L, 0x1000_1230_0000_0000L, EXPONENT_OF_MAX_VALUE), null, // product is approx. 2^-35 (2.9e-11)

    "// the other factor is subnormal", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x1000_1230_0000_0000L, EXPONENT_OF_MAX_VALUE),
    bdStr(0x0000_0000_1000_1230L, 0x0000_0000_0000_0000L, 0), null,

    "// the product is less than 1/2 MIN_VALUE", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x1000_1230_0000_0000L, EXPONENT_OF_ONE - 97),
    bdStr(0x0000_0000_1000_1230L, 0x0000_0000_0000_0000L, 0), "0",

  // Test data for roundBuffer(product);
  "// No carry no rounding up at all", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE),
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE), null,

  "// Carry to word 5 (the lowest one) of the buffer ", null, null,
    bdStr(0x0000_0002_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_7FFF_FFFFL, EXPONENT_OF_ONE), null,

  "// Carry to word 4 of the buffer", null, null,
    bdStr(0x0000_0001_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_FFFF_FFFFL, EXPONENT_OF_ONE), null,

  "// Carry to word 3 of the buffer", null, null,
    bdStr(0x0000_0001_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE),
    bdStr(0x0000_0000_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE), null,

  "// Carry to word 2 (the higher one) of the buffer", null, null,
    bdStr(0x1C71_C71C_71C7_1C71L, 0xC71C_71C7_1C71_C71CL, EXPONENT_OF_ONE),
    bdStr(0x2000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE), null,

  "// Carry to word 1 of the buffer (integer part of the mantissa)", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFeL, EXPONENT_OF_ONE), null,
  // roundUpBuffer(product); <br>Covered 20.05.04 18:14:16

/* */
  // Test data for  normalizeProduct(product, productExponent, isRoundedUp);
    "// No normalization required", null, null,
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE),
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE), null,

    "// Normalization, exponent remains normal", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE), null,

    "// Normalization, exponent overflow", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_MAX_VALUE - EXPONENT_OF_ONE / 2 - 1), null,
    // normalizeProduct(product, productExponent, isRoundedUp); Covered 20.05.05 15:33:51

/* */
  // Test data for  packBufferToMantissa(product); -- no special data needed
  // Test data for  normalizeSubnormal(productExponent, isRoundedUp);
    "// Normalization of a subnormal, with alreadyRounded flag set to false", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 1), null,

    "// Normalization of a subnormal, with alreadyRounded flag set to true", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE / 2 - 1),
    bdStr(0x4800_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),                 // Error exceeds the normal threshold for subnormals
    // normalizeSubnormal(productExponent, isRoundedUp); Covered 20.05.05 16:24:33

/* */
  // Test data for  makeSubnormal(long exp2) {
  // if (exp2 > 127) {
  //   exp == 129; product == 0
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 64),
    bdStr(0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 64), null,

  //   exp == 128; product == MIN_VALUE
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 64),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 64), MIN_VALUE_STR,

    //   exp == 127; product == MIN_VALUE
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 63),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 64), MIN_VALUE_STR,

  // Test data for shiftMantissa(exp2)
    // if (exp2 >= 64),  exp2 == 64
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 32),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 32), null,

    // if (exp2 >= 64),  exp2 > 64
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 33),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 32), null,

    // (exp2 < 64) && exp2 > 0
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 31),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 32), null,

    // (exp2 < 64) && exp2 == 0
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2), null,

    // back to makeSubnormal(long exp2). We've seen (shiftedOutBit == 0), now let it be 1
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_FFFFL, EXPONENT_OF_ONE / 2 + 1),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_8000L, 0),

    // back to makeSubnormal(long exp2). shiftedOutBit == 01, carry to the high word
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0xFFFE_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2 + 1), null,

    // back to makeSubnormal(long exp2). now let's see mantissa overflow (turning to minNormal)
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2 + 1), null,

    // A few more samples of becoming / not becoming MIN_NORMAL
    bdStr(mult(MIN_NORMAL, 123456789)), bdStr(div(BD_ONE, 123456789)), null,
    bdStr(mult(MIN_NORMAL, 123456789)), bdStr(div(sub(BD_ONE, 3e-39), 123456789)), null,

/* */
  // Test data for shiftBufferRight(product, isRoundedUp);
  "// No rounding performed before (isRoundedUp == false) ", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE), null,

  "// Already rounded up (isRoundedUp == true) ", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE), null,
    // <br>shiftBufferRight(product, isRoundedUp) Covered 20.05.05 17:29:38

  // Test data for shiftBuffRightWithRounding(long[] buffer)
  "// No carry propagation at all", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 1), null,

  "// The LSB of the next word is 1, without carry to the next half-word", null, null,
    bdStr(0x8000_0000_0000_0001L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 1), null,

  "// carry to the higher half-word", null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_AAAA_AAAA_AAAAL, EXPONENT_OF_ONE / 2),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 1), null,
    // shiftBuffRightWithRounding(long[] buffer) covered 20.05.06 20:35:22

    // unpack_To5x32(mantHi, mantLo, buffer1); already covered, it does not require special data
    // multiply() completely covered 2020-05-06 22:34:15

  /* */

  // 20.10.07 19:12:17 additionally check rounding in vicinity of LSB * (n + 0.5) -- does it always round in right direction?
  // Product is normal:
    "// remainder = 0x7FFF...FFFFL..., rounding down, mantLo = 0x7FFF...FFFFL", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x7FFF_FFFF_FFFF_FFFFL, 0x8000_0000), // remainder = 0x7FFF_...
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

    "// remainder = 0x8000...0000L, rounding up, mantLo = 0x8000...00001L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x8000_0000_0000_0000L, 0x8000_0000), // remainder = 0x7FFF_...
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, 0x8000_0000), null,

    "// remainder = 7FFF...FFFFL (192 bits)..., rounding down, mantLo = 0x7FFF...FFFFL", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_FFFF_FFFFL, 0x8000_0000), // remainder = 0x7FFF_...
    bdStr(0x0000_0000_8000_0000L, 0x8000_0000_8000_0000L, 0x8000_0000), null,

    // Product is subnormal:
    "// remainder = 0x8000...0000L, rounding up, mantLo = 0x8000...0000L", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x8000_0000_0000_0000L, 0x0000_0010), // remainder = 0x7FFF_...
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, 0x7FFF_FFE0), null,

    "// remainder = 0x7FFF...FFFFL..., rounding down, mantLo = 0x7FFF...FFFFL", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0x0000_0010), // remainder = 0x7FFF_...
    bdStr(0x0000_0000_0000_0001L, 0x0000_0000_0000_0000L, 0x7FFF_FFE0), null,

    "15.49999999999999999999999999999999999", MIN_VALUE_STR, bdStr(mult(15, MIN_VALUE)),
    "15.5",                                   MIN_VALUE_STR, bdStr(mult(16, MIN_VALUE)),

  /**/
  }; //  static String[] basicMultiplicationData = new String[] {

  /**
   * A data set for testing or debugging the division at some specific values
   * that may be of a special interest.<br>
   * Each data sample consists of three strings: two operands and the expected result
   * of their addition.<br>
   * Is not used in the current version.<br>
   * The number of items must be divisible by 3.
   */
  static String[] specialDivisionData = new String[] {
  "// Dividend is normal, divisor is subnormal", null, null,
    bdStr(0x2000_0000_0000_0000L, 0x0000_0000_0000_0000L, 3),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0), null,

  "// Both dividend and divisor are subnormal, divisor > dividend", null, null,
    bdStr(0xF000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0), null,

  "// Test data for normalizeAndUnpackDivisor(), shift == 64", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0000_0000_0000L, 0x1000_0000_0000_0000L, 0 ), null,

  "// shift <= 32", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0001_0000_0000L, 0x1000_0000_0000_0000L, 0 ), null,

  "// shift <= 64", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0000_0000_1000L, 0x1000_0000_0000_0000L, 0 ), null,
  };

  /**
   * A data set for testing {@link Quadruple#divide(Quadruple divisor)} and
   * {@link Quadruple#divide(Quadruple dividend, Quadruple divisor)} methods.<br>
   * Each data sample consists of three strings: string representations of the two operands,
   * and the expected value of their quotient.<br>
   * For most of the pairs of the operands, the corresponding result can be evaluated
   * by the testing code, thus the data in such cases contains {@code null}s
   * instead of the values of the expected results. Exceptions are the cases
   * where the quotient is subnormal, since subnormal values contain fewer bits
   * in the mantissa than normal values, so the relative error of subnormal values
   * can be much higher than the standard error threshold.<br>
   * Completely covers the code of the {@code Quadruple.divide(Quadruple divisor)} method.<br>
   * The number of items must be divisible by 3.
   */
  static String[] basicDivisionData = new String[] {

// 20.05.12 20:43:41 test data to cover divideUnsigned(Quadruple divisor)
/* */
  // divisor.compareMagnitudeTo(ONE) == 0
    "2.3456e-789012",                       "1.0",                                  null,

  // compareMagnitudeTo(divisor) == 0
    "1.2345e345678901",                     "1.2345e345678901",                     null,

  // checkBounds(), lower boundary
  "// exp is much less than < -128, exp == 0",                                      null, null,
    bdStr(0x1234_5678_0000_0000L, 0x0000_0000_0000_0000L, 0),
    bdStr(0x8765_4321_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_MAX_VALUE),                 null,

  "// exp < -128, result approx. == (MIN_VALUE / 8), rounded down to 0",            null, null, //
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 129),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     null, // str(div(MIN_VALUE, 8)),

  "// exp < -128, result == MIN_VALUE / 4, rounded down to 0",                      null, null,
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 129),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     null, // str(div(MIN_VALUE, 4)),

  "// exp < -128, result == (MIN_VALUE / 2) * (1 - 2^-129), rounded down to 0",     null, null,
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2 - 129),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     null, // str(mult(div(MIN_VALUE, 2), sub(1, 1.4693679e-39))),

  "// exp == -128, result == (MIN_VALUE / 4) * (1 + 2^-129), rounded down to 0",    null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 128),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     null, // str(mult(div(MIN_VALUE, 4), add(1, 1.4693679e-39))),

  "// exp == -128, result == (MIN_VALUE / 2), rounded up to MIN_VALUE",             null, null,
    bdStr(0x1234_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2 - 128),
    bdStr(0x1234_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     MIN_VALUE_STR, // str(div(MIN_VALUE, 2)),

  "// exp == -128, result == (MIN_VALUE * (1 - 2^-129)), rounded up to MIN_VALUE",  null, null,
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2 - 128),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     MIN_VALUE_STR, // str(mult(MIN_VALUE, sub(1, 1.4693679e-39))),

  "// exp much greater than -128",                                                  null, null,
    bdStr(0x8765_4321_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),
    bdStr(0x1234_5678_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),              null,

  // checkBounds(), upper boundary
  "// exp = EXPONENT_OF_MAX_VALUE + 1, quotient of mantissas < 1, result = MAX_VALUE * (1 - 1^-129) ", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2 + 1),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE / 2),              null,

  "// exp = EXPONENT_OF_MAX_VALUE + 1, quotient of mantissas == 1, result = MAX_VALUE * (1 + 1^-129) ", null, null,
    bdStr(0x1234_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2 + 1),
    bdStr(0x1234_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE / 2),              null,

  "// exp > EXPONENT_OF_MAX_VALUE + 1, quotient of mantissas is min possible, result > MAX_VALUE", null, null,
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2 + 2),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2),              null,

  "// exp is much greater than EXPONENT_OF_MAX_VALUE",                                            null, null,
    bdStr(0x1234_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_MAX_VALUE),
    bdStr(0x1234_0000_0000_0000L, 0x0000_0000_0000_0000L, 3),                       null,
  // exponentExceedsBounds and divideUnsigned completely covered 20.05.13 20:41:16

  // Test data for normalizeAndUnpackSubnormals()
  "// Dividend is subnormal, divisor is normal",                                    null, null,
    bdStr(0x2000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 3),                       null,

  "// Dividend is normal, divisor is subnormal",                                    null, null,
    bdStr(0x2000_0000_0000_0000L, 0x0000_0000_0000_0000L, 3),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),                       null,

  "// Both dividend and divisor are subnormal, divisor > dividend",                 null, null,
    bdStr(0xF000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),                       null,

  "// Both dividend and divisor are subnormal, divisor < dividend",                 null, null,
    bdStr(0xA000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),
    bdStr(0xFFFF_0000_0000_0000L, 0x0000_0000_0000_0000L, 0),                       null,
    // normalizeAndUnpackSubnormals() covered 20.05.14 17:06:42

/* */
  // Test data for divideByBuff()
  "// Carry from the remainder of division to the lower word",                      null, null,
    bdStr(0x0000_0000_3FFF_FFFFL, 0xFFF0_FFFF_0FFF_FFFFL, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE ),                 null,

  "// Carry from the remainder of division reaches the higher word",                null, null,
    bdStr(0x0000_0000_3FFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE ),                 null,
    // divideByBuff()  covered 20.05.15 12:10:36
    // unpackMantissaToBuff_10x32() covered
    // shiftBufferLeft() covered
    // packMantissaFromWords_1to4() covered

  "// Test data for normalizeMantissa(), shift == 64",                              null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE ),                 null,

/* */
  "// Test data for normalizeAndUnpackDivisor(), shift == 64",                      null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0000_0000_0000L, 0x1000_0000_0000_0000L, 0 ),                      null,

  "// shift <= 32", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0001_0000_0000L, 0x1000_0000_0000_0000L, 0 ),                      null,

  "// shift <= 64", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0000_0000_1000L, 0x1000_0000_0000_0000L, 0 ),                      null,

  "// shift > 96", null, null,
    bdStr(0x0000_0000_0000_0000L, 0xFFFF_FFFF_FFFF_FFFFL, 0 ),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0123_0000L, 0 ),                      null,

    // normalizeAndUnpackDivisor() covered
    // divideArrays() covered()
    // subtractPartialProduct()
    // findNextQutientWord()
    // decreaseIfTooGreat()
    // shiftRemainderLeft()
    // subtractFinalProduct()
    // addDivisor()
    // subtractProduct()
    // multDivisorBy()
    // div2words() -- all covered

/* */
    // Test data for  makeSubnormal(long exp2) {
    // exp > 128 impossible in case of division
    // exp == 128; product == MIN_VALUE (0.5 LSB, actually, rounded up )
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000 - 128),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   MIN_VALUE_STR,

    // exp == 127; product == MIN_VALUE
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000 - 127),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null,

    // exp == 127; product == 1.5 * MIN_VALUE
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000 - 127),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   bdStr(mult(2, MIN_VALUE)), // 1.5 * MIN_VALUE rounded up

    // to cover shiftMantissa(long exp2)
    // returning shiftedOutBit == 0
    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000 - 65),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null,   // exp >= 64;

    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000 - 64),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null,   // exp == 64;

    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000 - 63),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null,   // exp < 64;

    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE - 1000),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null,   // exp = 0;

    // to cover shiftMantissa(long exp2)
    // returning shiftedOutBit == 1
    bdStr(0x7fff_ffff_ffff_ffffL, 0xFfff_ffff_ffff_ffffL, EXPONENT_OF_ONE - 1000 - 65),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null, // exp >= 64;

    bdStr(0x7fff_ffff_ffff_ffffL, 0xFfff_ffff_ffff_ffffL, EXPONENT_OF_ONE - 1000 - 64),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null, // exp == 64;

    bdStr(0x7fff_ffff_ffff_ffffL, 0xFfff_ffff_ffff_ffffL, EXPONENT_OF_ONE - 1000 - 63),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null, // exp < 64;

    bdStr(0x8000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE - 1000),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, -1003),                   null, // exp = 0;

    bdStr(0x8000_0000_0000_000fL, 0xffff_ffff_ffff_ffffL, EXPONENT_OF_ONE - 1000),           // exp = 0; overflow of the lower 64 bits
    bdStr(0, 0, -1002),                                                             bdStr(0xc000_0000_0000_0008L, 0, 0), // the precision of findExpectedValue() is too high for subnormals

    bdStr(0xffff_ffff_ffff_ffffL, 0xffff_ffff_ffff_ffffL, EXPONENT_OF_ONE - 1000),
    bdStr(0, 0, -1002),                                                             null, // exp = 0; turning to MIN_NORMAL

  /* */
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, EXPONENT_OF_ONE / 2 + 10),
    bdStr(0xFFFF_FFFE_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE + EXPONENT_OF_ONE / 2),     null,

  /* */
    // 20.12.09 13:51:34 Data to cover all iterations of the loop in findNextBitOfQuotient()

    "// i: 0, rm: 0000_0000_0000_0000, dr: 0000_0000_0000_0001, return 0",          null, null,
    bdStr(0x8000_0000_0000_0000L, 8, EXPONENT_OF_ONE ),
    bdStr(0, 8, EXPONENT_OF_ONE ),                                                           null,

    "// i: 1, rm: 0000_0000_ffff_fffc, dr: 0000_0000_ffff_ffff, return 0",          null, null,
    bdStr(0x7FFF_FFFF_0000_0000L, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFEL, EXPONENT_OF_ONE ),                 null,

    "// i: 2, rm: 0000_0000_ffff_fffc, dr: 0000_0000_ffff_ffff, return 0",          null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0x0000_0000_0000_0000L, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFEL, EXPONENT_OF_ONE ),                 null,

    "// i: 3, rm: 0000_0000_ffff_fffc, dr: 0000_0000_ffff_ffff, return 0",          null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_0000_0000L, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFEL, EXPONENT_OF_ONE ),                 null,

    "// i: 4, rm: 0000_0000_ffff_fffc, dr: 0000_0000_ffff_fffe, finally return 0",  null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFEL, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFEL, EXPONENT_OF_ONE ),                 null,

    "// i: 0, rm: 0000_0000_0000_0003, dr: 0000_0000_0000_0001, return 1",          null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFCL, EXPONENT_OF_ONE ),
    bdStr(0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFCL, EXPONENT_OF_ONE ),                 null,

    "// i: 1, rm: 0000_0000_0000_0002, dr: 0000_0000_0000_0000, return 1",          null, null,
    bdStr(0x7FFF_FFFF_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE ),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE ),                 null,

    "// i: 2, rm: 0000_0000_0000_0002, dr: 0000_0000_0000_0000, return 1",          null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE ),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE ),                 null,

    "// i: 3, rm: 0000_0000_0000_0002, dr: 0000_0000_0000_0000, return 1",          null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_0000_0001L, EXPONENT_OF_ONE ),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE ),                 null,

    "// i: 4, rm: 0000_0000_0000_0008, dr: 0000_0000_0000_0001, finally return 1",  null, null,
    bdStr(0x7FFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFEL, EXPONENT_OF_ONE ),
    bdStr(0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L, EXPONENT_OF_ONE ),                 null,

  /**/
  }; // static String[] basicDivisionData = new String[] {

  /**
   * A data set for testing or debugging {@code Quadruple.sqrt()} at some specific values
   * that may be of a special interest.<br>
   * Each data sample consists of two strings: a string representation of the argument
   * of {@code sqrt()} and the expected result of extracting the square root of this argument.
   * Actually, in this data set the expected results can be correctly deduced by the test code
   * in all cases, so all the odd items are {@code null}s.<br>
   * Is not used in the current version.<br>
   * The number of items must be even.
   */
  static String[] specialSqrtData = {
    // Long ago, there were some doubts regarding values like the following
    bdStr(0, 1, EXPONENT_OF_ONE),                    null,
    bdStr(0, 2, EXPONENT_OF_ONE),                    null,
    bdStr(0, 3, EXPONENT_OF_ONE),                    null,
    bdStr(0, 4, EXPONENT_OF_ONE),                    null,
    bdStr(0, 5, EXPONENT_OF_ONE),                    null,
    bdStr(0, 6, EXPONENT_OF_ONE),                    null,

/** DONE 20.12.15 19:29:15 WTF???
 19406: src:  2.0076107111543718154590316207407593704378047e-5228835   (+1553_f434_c068_fb7f 6838_9c71_c1e7_ba7f e 7ef6_f52a)
        res:  4.4806369091395609484735018225361820177837586e-2614418   (+78d1_7161_c30e_4e55 684c_0a24_c51a_93b6 e 7f7b_7a94)
        exp:  4.4806369091395609484735018225361820177882315e-2614418   (+78d1_7161_c30e_4e55 684c_0a24_c51a_93b7 e 7f7b_7a94)
 $$$                                                                                                           ^
 ***      39                                          ^^^^^            (-9.983e-40)

   420: src:  5.0155671686993364828775571387906616450243522e+7262859   (+9c59_1285_a647_6880 1205_d97b_dfaf_cd67 e 8170_24e8)
        res:  7.0820669078308886610103015382957553768987526e+3631429   (+cb7a_f4d7_91d3_6efb 45ef_6422_8044_9b19 e 80b8_1273)
        exp:  7.0820669078308886610103015382957553768929548e+3631429   (+cb7a_f4d7_91d3_6efb 45ef_6422_8044_9b18 e 80b8_1273)
 $$$                                                                                                           ^
 ***      39                                          ^^ ^^            (8.187e-40)

   591: src:  1.0012513645476796590328611049112287586792016e+635361140 (+4a43_0eac_94d9_1354 d1ba_6600_1a76_958f e fdcd_9114)
        res:  1.0006254866570607490009165512268309833986040e+317680570 (+9b35_e676_529a_32fb 63f4_a59f_493f_7799 e bee6_c889)
        exp:  1.0006254866570607490009165512268309833995193e+317680570 (+9b35_e676_529a_32fb 63f4_a59f_493f_779a e bee6_c889)
 $$$                                                                                                           ^
 ***      39                                          ^^^^^            (-9.148e-40)
    20.12.17 13:40:34 Done.
    18 bytes of the root was insufficient in some cases when the root
    had to be additionally multiplied by sqrt(2). Now sqrtMant() calculates 20 bytes.
    /**/

    bdStr(0x1553_f434_c068_fb7fL, 0x6838_9c71_c1e7_ba7fL, EXPONENT_OF_ONE + 1), null,
    bdStr(0x9c59_1285_a647_6880L, 0x1205_d97b_dfaf_cd67L, EXPONENT_OF_ONE + 1), null,
    bdStr(0x4a43_0eac_94d9_1354L, 0xd1ba_6600_1a76_958fL, EXPONENT_OF_ONE + 1), ""
    /* */
  }; // static String[] specialSqrtData = {

  /**
   * A data set for testing {@link Quadruple#sqrt()} and {@link Quadruple#sqrt(Quadruple)} methods.<br>
   * Each data sample consists of two strings: a string representation of the argument of
   * {@code sqrt()} function to be tested, and the expected result of extracting
   * the square root of this argument.
   * In this data set the expected result can be correctly deduced by the test code
   * in most cases, so most of the odd items are {@code null}s. Exceptions are the cases of
   * negative arguments, for which the expected result is {@code NaN}, that is stated explicitely. <br>
   * Completely covers the code of the {@code Quadruple.sqrt()} method.<br>
   * The number of items must be even.
   */
  static String[] basicSqrtData = {
  /**/

// To test that the test code can see an error in the input data
//      "Some rubbish", null,
// Special cases:
    "NaN",                                  null,
    "Infinity",                             null,
    "-Infinity",                            "NaN",
    "-16",                                  "NaN",

    // DONE 20.12.14 15:43:24 Cover the code
    "16",                                   null,
    // if (exponent == 0) // subnormal; two cases in normalizeMantissa()
    bdStr(mult(MIN_VALUE, 12345)),          null, // Empty mantHi
    bdStr(div(MIN_NORMAL, 12345)),          null, // non-empty mantHi

    // else (exponent != 0)
    "2",                                    null,

    // to cover sqrtMant()
    // computeNextDigit: remainderIsEmpty == true
    "1.226383209228515625",                 null, // (280/256)^2

    // in findNextDigit(), decrement of the digit
    bdStr(0x03f1_8000_0000_0000L, 0, EXPONENT_OF_ONE), null,

    // in subtractBuff(),
    // if (minuend[i - 1] == 0), // increase subtrahend instead of decreasing minuend, to assure the borrow propagation
    bdStr(0x33c9_a789_5c84_8113L, 0x2f87_f69a_fd9b_6257L, EXPONENT_OF_ONE), null,

    // in multBufByDigit(),
    // if (Long.compareUnsigned(product, prodLo) < 0) carry++;
    bdStr(0x4853_8225_c5a9_984cL, 0xd9ef_6a45_01f8_5c77L, EXPONENT_OF_ONE), null,

    // in public Quadruple sqrt() {
    // if ((thirdWord & HIGH_BIT) != 0)
    bdStr(-1, -1, EXPONENT_OF_ONE),                  null, // if (++mantLo == 0) not satisfied
    bdStr(0x8fff_ffff_ffff_fffDL, 0x8000_0000_0000_0000L, EXPONENT_OF_ONE), null,  // if (++mantLo == 0) is  satisfied

     /* */
    // Just to play
    "24",                                   null,
    "12",                                   null,
    "6",                                    null,
    "3",                                    null,
    "1.5",                                  null,
    "0.64",                                 null,
    "0.36",                                 null,
    "0.1875",                               null,

    "1.2945382716049377",                   null,// 0x1.2345_6789_ABC, ^ 2
    "//A couple of magic numbers",          null,
    "1.29453827160493827160493827160493827160493827160493827160493827160", null,
    "1.1377777777777777777777777777777777777777777", null,
    "// Max number below 2",                null,
    "1.999999999999999999999999999999999999996",  null,// Max number below 2
    "2",                                    null,
    "// Must be 1.5",                       null,
    "2.25",                                 null,  // Must be 1.5
    "3",                                    null,
    "1.494885394702752627025000195463067701065646575", null,
    "1.05704359969104503157971909575375023294792748170997", null,
    "5.9795415788110105081000007818522708042625863", null,
    "2.1140871993820900631594381915075004658958549634199583820", null,
    "1.12345678901234567890123456789012345678901234567890", null,


    MIN_VALUE_STR,                          null,
    str(mult(MIN_VALUE, 16)),               null,
    str(mult(MIN_VALUE, 1023)),             null,
    MIN_NORMAL_STR,                         null,
    MAX_VALUE_STR,                          null,
    "0.0625",                               null,
    "0.25",                                 null,
    "1",                                    null,
    "1.99999999999999999",                  null,


  /* */
    "1.25",                                 null,
    "1.5",                                  null,
    "2",                                    null,
    "2.5",                                  null,
    "3",                                    null,
    "1024",                                 null,
    "10",                                   null,
    "11",                                   null,
    "12",                                   null,
    "2.56",                                 null,
    "1.28",                                 null,
  /**/
  };
  /**/

  @SuppressWarnings("unused")
  private void dummyMethodToEnableThePreviousCommentBeFoldedInEclipse() {}

}
