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

package com.mvohm.quadruple;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A floating-point number with a 128-bit fractional part of the mantissa and 32-bit
 * exponent. Normal values range from approximately {@code 2.271e-646456993}
 * to {@code 1.761e+646456993} with precision not worse than {@code 1,469368e-39}
 * ({@code 2^-129}, half the less significant bit).
 * <p>
 * Like standard {@code double}, it can store and process subnormal values,
 * with lower precision. The subnormal values range from {@code 6.673e-646457032}
 * to {@code 2.271e-646456993}, they use less than 129 bits of the mantissa and their
 * precision depends on the number of used bits, the less the value the worse the precision.
 * <p>
 * Implements conversions from/to other numeric types, conversions from/to strings,
 * formatting, arithmetic operations and square root.
 * <p>
 * <i><b>Instances are mutable</b></i>, {@code a.add(2)} changes the value of {@code a} so that it becomes
 * {@code a + 2}, and a number of {@code assign()} methods with different types of arguments
 * replace the old value with the new one, converted from the argument value.
 * <p>
 * For arithmetic operations, there provided both instance methods that modify the value
 * of the instance, and static methods that return new instances with resulting values
 * without changing the operands. A value of any numeric type may be used as an argument
 * (the second one for static methods) in arithmetic operations.
 * All the methods implementing arithmetic operations and assignments
 * allow for chaining, so that one can write
 * <p style="margin-left:20px;">{@code a = a.add(2).multiply(5).divide(3);} <p>
 * to compute
 * <p style="margin-left:20px;">{@code a = (a + 2) * 5 / 3}.<br>
 * <p>
 * <b><i>The class is not thread safe.</i></b> Different threads should not simultaneously
 * perform operations even with different instances of the class.
 * <p>
 * An instance internally contains boolean flag for the value's sign,
 * 32-bit (an {@code int}) of binary exponent, and 128 bits (2 {@code longs}) of fractional part of the mantissa.
 * Like with usual floating-point formats (e.g. standard Java {@code double}), the most significant
 * bit of the mantissa is not stored explicitly and the exponent is biased.<br>
 * <br>
 * The biased exponent values stored in the {@code exponent} field are as following:
 *
 <table class="memberSummary" border="0" cellpadding="3" cellspacing="0" summary="">
   <tr>
      <th class="colLast" scope="col">biased value</th>
      <th class="colLast" scope="col">const name</th>
      <th class="colLast" scope="col">means</th>
      <th class="colLast" scope="col">unbiased exponent (power of 2)</th>
    </tr>
    <tr class="altColor">
      <td>{@code 0x0000_0000}</td>
      <td>{@code EXPONENT_OF_SUBNORMAL}</td>
      <td>subnormal values</td>
      <td>{@code 0x8000_0001 = -2147483647 =  Integer.MIN_VALUE + 1}</td>
    </tr>
    <tr class="rowColor">
      <td>{@code 0x0000_0001}</td>
      <td>{@code EXPONENT_OF_MIN_NORMAL}</td>
      <td>{@code MIN_NORMAL}</td>
      <td>{@code 0x8000_0002 = -2147483646 =  Integer.MIN_VALUE + 2}</td>
    </tr>
    <tr class="altColor">
      <td>{@code 0x7FFF_FFFE}</td>
      <td>&nbsp;</td>
      <td>{@code -1}</td>
      <td>{@code 0xFFFF_FFFF}</td>
    </tr>
    <tr class="rowColor">
      <td>{@code 0x7FFF_FFFF}</td>
      <td>{@code EXPONENT_OF_ONE}</td>
      <td>{@code 0}</td>
      <td>{@code 0x0000_0000}</td>
    </tr>
    <tr class="altColor">
      <td>{@code 0x8000_0000}</td>
      <td>&nbsp;</td>
      <td>{@code 1}</td>
      <td>{@code 0x0000_0001}</td>
    </tr>
    <tr class="rowColor">
      <td>{@code 0xFFFF_FFFE}</td>
      <td>{@code EXPONENT_OF_MAX_VALUE}</td>
      <td>{@code MAX_VALUE}</td>
      <td>{@code 0x7fff_ffff =  2147483647 =  Integer.MAX_VALUE}</td>
    </tr>
    <tr class="altColor">
      <td>{@code 0xFFFF_FFFF}</td>
      <td>{@code EXPONENT_OF_INFINITY}</td>
      <td>{@code Infinity}</td>
      <td>{@code 0x8000_0000 =  2147483648 =  Integer.MIN_VALUE}</td>
    </tr>
  </table>
 * <br>The boundaries of the range are:
 * <pre>{@code
 * MAX_VALUE:  2^2147483647 * (2 - 2^-128) =
 *             = 1.76161305168396335320749314979184028566452310e+646456993
 * MIN_NORMAL: 2^-2147483646 =
 *             = 2.27064621040149253752656726517958758124747730e-646456993
 * MIN_VALUE:  2^-2147483774 =
 *             = 6.67282948260747430814835377499134611597699952e-646457032
 * }</pre>
 *
 * @author M.Vokhmentsev
 */
/*
 * Improvements needed:
 * -- Implement conversions from and to standard IEEE 754 values (represented as a pair of longs)
 * -- Planned for next releases: add mathematical functions, like log(), sin() etc.
 * -- Planned for next releases: speed up Q->S and S->Q conversions by splitting
 *      constants in POS_POWERS_OF_2 and NEG_POWERS_OF_2 tables into pieces by 32 bits each
 */
public class Quadruple extends Number implements Comparable<Quadruple> {


  private static final long serialVersionUID      = 1L;
  private static final int HASH_CODE_OF_NAN       = -441827835;  // All the NaNs have to have the same hashcode.
                                                            // This is the best one I could imagine.

  /** The value of the exponent (biased) corresponding to subnormal values; equals to 0
  * Deprecated, will be removed in release version. Use {@link EXPONENT_OF_SUBNORMAL} instead */
  @Deprecated
  public static final int EXP_SUB                 = 0;
  /** The value of the exponent (biased) corresponding to subnormal values; equals to 0 */
  public static final int EXPONENT_OF_SUBNORMAL   = 0;

  /** The value of the exponent (biased) corresponding to {@code MIN_NORMAL}; equals to 1
   * Deprecated, will be removed in release version. Use {@link EXPONENT_OF_MIN_NORMAL} instead */
  @Deprecated
  public static final int EXP_MIN                 = 1;
  /** The value of the exponent (biased) corresponding to {@code MIN_NORMAL}; equals to 1 */
  public static final int EXPONENT_OF_MIN_NORMAL  = 1;


  /** The value of the exponent (biased) corresponding to {@code 1.0 == 2^0};
   * equals to 2_147_483_647 ({@code 0x7FFF_FFFF})
   * Deprecated, will be removed in release version. Use {@link #EXPONENT_OF_ONE} instead */
  @Deprecated
  public static final int EXP_OF_ONE              = 0x7FFF_FFFF;
  /** The value of the exponent (biased) corresponding to {@code 1.0 == 2^0};
   * equals to 2_147_483_647 ({@code 0x7FFF_FFFF}).
   * The same as {@link #EXPONENT_BIAS}   */
  public static final int EXPONENT_OF_ONE         = 0x7FFF_FFFF;
  /** The value of the exponent (biased) corresponding to {@code 1.0 == 2^0};
   * equals to 2_147_483_647 ({@code 0x7FFF_FFFF})
   * The same as {@link #EXPONENT_OF_ONE} */
  public static final int EXPONENT_BIAS           = 0x7FFF_FFFF;

  /** The value of the exponent (biased), corresponding to {@code MAX_VALUE};
   * equals to 4_294_967_294L ({@code 0xFFFF_FFFEL})
  * Deprecated, will be removed in release version. Use {@link #EXPONENT_OF_MAX_VALUE} instead */
 @Deprecated
  public static final long EXP_MAX                = 0xFFFF_FFFEL;
  /** The value of the exponent (biased), corresponding to {@code MAX_VALUE};
   * equals to 4_294_967_294L ({@code 0xFFFF_FFFEL}) */
  public static final long EXPONENT_OF_MAX_VALUE  = 0xFFFF_FFFEL;

  /** The value of the exponent (biased), corresponding to {@code Infinity},
   * {@code _Infinty}, and {@code NaN};
   * equals to -1 ({@code 0xFFFF_FFFF})
   * Deprecated, will be removed in release version. Use {@link #EXPONENT_OF_INFINITY} instead  */
  @Deprecated
  public static final int EXP_INF                 = 0xFFFF_FFFF;
  /** The value of the exponent (biased), corresponding to {@code Infinity},
   * {@code _Infinty}, and {@code NaN};
   * equals to -1 ({@code 0xFFFF_FFFF}) */
  public static final int EXPONENT_OF_INFINITY    = 0xFFFF_FFFF;



/* **********************************************************************
 ******* Constructors ***************************************************
 ************************************************************************/

  protected void ____Constructors____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Creates a new instance of {@code Quadruple} with value 0.0
   */
  public Quadruple() {
  }

  /**
   * Creates a new {@code Quadruple} instance with the value of the given {@code Quadruple} instance.<br>
   * First creates an empty (zero) instance, then copies the fields of the parameter.
   * to the fields of the new instance
   * @param qValue the {@code Quadruple} value to be assigned to the new instance.
   */
  public Quadruple(Quadruple qValue) {
    assign(qValue);
  }

  /**
   *  Creates a new {@code Quadruple} instance with the given {@code double} value.<br>
   * First creates an empty (zero) instance, then assigns the given
   * value to the new instance, using {@link #assign(double)}.
   * @param dValue  the {@code double} value to be assigned
   */
  public Quadruple(double dValue) {
    assign(dValue);
  }

  /**
   * Creates a new {@code Quadruple} with the given {@code long} value.<br>
   * First creates an empty (zero) instance, then assigns the given
   * value to the new instance, using {@link #assign(long)}.
   * @param lValue  the {@code long} value to be assigned */
  public Quadruple(long lValue) {
    assign(lValue);
  }

  /**
   * Creates a new {@code Quadruple} with the value represented by the given {@code String}.<br>
   * First creates an empty (zero) instance, then assigns the given
   * value to the new instance, converting the string to the corresponding floating-point value.
   * Some non-standard string designations for special values are admissible, see {@link #assign(String)}
   * @param strValue the {@code String} value to be assigned
   * @see #assign(String)
   */
  public Quadruple(String strValue) {
    assign(strValue);
  }

  /**
   * Creates a new {@code Quadruple} with the value of the given {@code BigDecimal} instance.<br>
   * First creates an empty (zero) instance, then assigns the given
   * value to the new instance, converting the BigDecimal to respective floating-point value
   * @param bdValue the {@code BigDecimal} value to be assigned
   */
  public Quadruple(BigDecimal bdValue) {
    assign(bdValue);
  }

  /**
   * Creates a new {@code Quadruple} built from the given parts.<br>
   * @param negative  the sign of the value ({@code true} signifies negative values)
   * @param exponent  the binary exponent (unbiased)
   * @param mantHi  the most significant 64 bits of fractional part of the mantissa
   * @param mantLo  the least significant 64 bits of fractional part of the mantissa
   */
  public Quadruple(boolean negative, int exponent, long mantHi, long mantLo) {
    this.negative = negative;
    this.exponent = exponent;
    this.mantHi = mantHi;
    this.mantLo = mantLo;
  }

  /**
   * Creates a new {@code Quadruple} with a positive value built from the given parts.<br>
   * @param exponent  the binary exponent (unbiased)
   * @param mantHi  the most significant 64 bits of fractional part of the mantissa
   * @param mantLo  the least significant 64 bits of fractional part of the mantissa
   */
  public Quadruple(int exponent, long mantHi, long mantLo) {
    this(false, exponent, mantHi, mantLo);
  }

  /* **********************************************************************
   ******* Special values *************************************************
   ************************************************************************/

  protected void ____Special_values____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**  Returns a new {@code Quadruple} instance with the value of {@code -Infinity}.
   * @return a new {@code Quadruple} instance with the value of NEGATIVE_INFINITY */
  public static Quadruple negativeInfinity() {  return  new Quadruple().assignNegativeInfinity(); }

  /**  Returns a new {@code Quadruple} instance with the value of {@code +Infinity}.
   * @return a new {@code Quadruple} instance with the value of POSITIVE_INFINITY */
  public static Quadruple positiveInfinity() {  return  new Quadruple().assignPositiveInfinity(); }

  /**  Returns a new {@code Quadruple} instance with the value of {@code NaN}.
   * @return a new {@code Quadruple} instance with the value of NAN */
  public static Quadruple nan()             {  return  new Quadruple().assignNaN();         }

  /**  Returns a new {@code Quadruple} instance with the value of 1.0.
   * @return a new {@code Quadruple} instance with the value of 1.0 */
  public static Quadruple one()             {  return  new Quadruple().assign(1);           }

  /**  Returns a new {@code Quadruple} instance with the value of 2.0.
   * @return a new {@code Quadruple} instance with the value of 2.0 */
  public static Quadruple two()             {  return  new Quadruple().assign(2);           }

  /**  Returns a new {@code Quadruple} instance with the value of 10.0.
   * @return a new {@code Quadruple} instance with the value of 10.0 */
  public static Quadruple ten()             {  return  new Quadruple().assign(10);          }

  /**  Returns a new {@code Quadruple} instance with the value of {@code MIN_VALUE}<br>
   *   ({@code 2^-2147483774} = 6.67282948260747430814835377499134611597699952e-646457032)
   * @return a new {@code Quadruple} instance with the value of MIN_VALUE */
  public static Quadruple minValue()        {  return  new Quadruple().assignMinValue();    }

  /**  Returns a new {@code Quadruple} instance with the value of {@code MAX_VALUE}<br>
   *   ({@code 2^2147483647 * (2 - 2^-128)} = 1.76161305168396335320749314979184028566452310e+646456993)
   * @return a new {@code Quadruple} instance with the value of {@code MAX_VALUE} */
  public static Quadruple maxValue()        {  return  new Quadruple().assignMaxValue();    }

  /**  Returns a new {@code Quadruple} instance with the value of {@code MIN_NORMAL}<br>
   *  ({@code 2^-2147483646} = 2.27064621040149253752656726517958758124747730e-646456993)
   * @return a new {@code Quadruple} instance with the value of {@code MIN_NORMAL} */
  public static Quadruple minNormal()        {  return  new Quadruple().assignMinNormal();   }

  /**  Returns a new {@code Quadruple} instance with the value of the number {@code π} (pi)
   * (3.141592653589793238462643383279502884195)
   * @return a new {@code Quadruple} instance with the value of the number {@code π} (pi) */
  public static Quadruple pi() {
    return new Quadruple( 0x8000_0000, 0x921f_b544_42d1_8469L, 0x898c_c517_01b8_39a2L);
  }

  /* **********************************************************************
   ******* Getters for private fields ****************************************************
   ************************************************************************/

  protected void ____Getters_for_private_fields____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**  Returns the raw (biased) value of the binary exponent of the value
   * i. e. 0x7FFF_FFFF for values falling within the interval of {@code [1.0 .. 2.0)}, 0x8000_0000 for {@code [2.0 .. 4.0)} etc.
   * @return the raw (biased) value of the binary exponent of the value
   */
  public int exponent() { return exponent; }

  /**
   *  Returns the unbiased value of binary exponent,
   * i. e. 0 for values falling within the interval of {@code [1.0 .. 2.0)}, 1 for {@code [2.0 .. 4.0)} etc.
   * @return the unbiased value of binary exponent */
  public int unbiasedExponent() { return exponent - EXPONENT_BIAS; }

  /**
   * Returns the most significant 64 bits of the fractional part of the mantissa.
   * @return the most significant 64 bits of the fractional part of the mantissa
   */
  public long mantHi() { return mantHi; }

  /**
   * Returns the least significant 64 bits of the fractional part of the mantissa
   * @return the least significant 64 bits of the fractional part of the mantissa */
  public long mantLo() { return mantLo; }

  /**
   * Checks if the value is negative.
   * @return {@code true}, if the value is negative, {@code false} otherwise  */
  public boolean isNegative() { return negative; }

  /**
   * Checks if the value is infinite (i.e {@code NEGATIVE_INFINITY} or {@code POSITIVE_INFINITY}).
   * @return {@code true}, if the value is infinity (either positive or negative), {@code false} otherwise */
  public boolean isInfinite() {
    return (exponent == EXPONENT_OF_INFINITY) && ((mantHi | mantLo) == 0);
  }

  /** Checks if the value is not a number (i.e. has the value of {@code NaN}).
   * @return {@code true}, if the value is not a number (NaN), {@code false} otherwise */
  public boolean isNaN() {
    return (exponent == EXPONENT_OF_INFINITY) && ((mantHi | mantLo) != 0);
  }

  /**
   * Checks if the value is zero, either positive or negative.
   * @return {@code true}, if the value is 0 or -0, otherwise returns   */
  public boolean isZero() {
    return (mantHi | mantLo | exponent) == 0;
  }


  /* **********************************************************************
   ******* Assignments ****************************************************
   ************************************************************************/

  protected void ____Assignments____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Assigns the given value to the instance (copies the values of the private fields of the parameter
   * to the respective fields of this instance).
   * @param qValue  a {@code Quadruple} instance whose value is to assign
   * @return this instance with the newly assigned value
   */
  public Quadruple assign(Quadruple qValue) {
    negative = qValue.negative;
    exponent = qValue.exponent;
    mantHi = qValue.mantHi;
    mantLo = qValue.mantLo;
    return this;
  } // public Quadruple assign(Quadruple qValue) {

  /**
   * Converts the given value to quadruple and assigns it to the instance.<br>
   * Expands exponent to 32 bits preserving its value, and expands mantissa to 128 bits,
   * filling with zeroes the least significant 76 bits that absent in the double value.
   * Subnormal double values
   * <span style="white-space:nowrap">{@code (Double.MIN_NORMAL < v <= Double.MIN_VALUE)}</span>
   * are converted
   * to normal quadruple values, by shifting them leftwards and correcting the exponent accordingly.
   * @param value  the {@code double} value to be assigned.
   * @return this instance with the newly assigned value
   */
  public Quadruple assign(double value) {
    long dobleAsLong = Double.doubleToLongBits(value);
    this.negative = (dobleAsLong & DOUBLE_SIGN_MASK) != 0;  // The sign of the given value
    dobleAsLong &= ~DOUBLE_SIGN_MASK;                   // clear sign bit

    if (dobleAsLong == 0)                               // Special case: 0.0 / -0.0 (sign is cleared here)
      return assignZero(false);                         // preserve its sign

    mantLo = 0;                                         // it will be 0 in any case
    if ((dobleAsLong & DOUBLE_EXP_MASK) == 0)           // Special case: subnormal
      return makeQuadOfSubnormDoubleAsLong(dobleAsLong);

    if ((dobleAsLong & DOUBLE_EXP_MASK) == DOUBLE_EXP_MASK) { // Special case: NaN or Infinity
      exponent = EXPONENT_OF_INFINITY;
      if ((dobleAsLong & DOUBLE_MANT_MASK) == 0)        // Infinity
        mantHi = 0;
      else                                              // NaN
        mantHi = DOUBLE_SIGN_MASK;
      return this;
    }

  // Normal case
    exponent = (int)((dobleAsLong & DOUBLE_EXP_MASK) >>> 52) - EXP_0D + EXPONENT_BIAS; // 7FF -> 8000_03FF, 400 -> 8000_0000, 1 -> 7FFF_FC01,
    mantHi = (dobleAsLong & DOUBLE_MANT_MASK) << 12;    // mantissa in double starts from bit 51
    return this;
  } // public Quadruple assign(double value) {

  /**
   * Converts the given value to quadruple and assigns it to the instance.<br>
   * To find the mantissa, shifts the bits of the absolute value of the parameter left,
   * so that its most significant non-zero bit (that stands for the 'implicit unity'
   * of the floating point formats) gets shifted out, then corrects the exponent
   * depending on the shift distance and sets the sign
   * in accordance with the initial sign of the parameter.
   * @param value  the {@code long} value to be assigned
   * @return this instance with the newly assigned value
   */
  public Quadruple assign(long value) {
    // 19.07.29 11:00:05
    if (value == 0)
      return assignZero();

    if (value == HIGH_BIT)  // == Long.MIN_VALUE
      return assignWithUnbiasedExponent(true, 63, 0, 0); // -2^63

    if (value < 0) {
      negative = true;
      value = -value;
    }
    else negative = false;

    // Shift the value left so that its highest non-zero bit get shifted out
    // it should become the implied unity
    final int bitsToShift = Long.numberOfLeadingZeros(value) + 1;
    value = (bitsToShift == 64)? 0 : value << bitsToShift;
    return assignWithUnbiasedExponent(negative, 64 - bitsToShift, value, 0);
  } // public Quadruple assign(long value) {

  /**
   * Parses the given String that is expected to contain
   * floating-point value in any conventional string form or a string designation
   * of one of special values, and assigns the corresponding value to the instance.<br>
   * Parsing is case-insensitive.<br>
   * The admittable string designations for special values are the following:<ul>
   * <li>"Quadruple.MIN_VALUE", <li>"MIN_VALUE", <li>"Quadruple.MAX_VALUE", <li>"MAX_VALUE",
   * <li>"Quadruple.MIN_NORMAL", <li>"MIN_NORMAL", <li>"Quadruple.NaN", <li>"NaN",
   * <li>"Quadruple.NEGATIVE_INFINITY", <li>"NEGATIVE_INFINITY", <LI>"-INFINITY",
   * <li>"Quadruple.POSITIVE_INFINITy", <li>"POSITIVE_INFINITY", <li>"INFINITY", <li>"+INFINITY".</ul>
   *<br>
   * If the exact value of the  number represented by the input string is greater
   * than the nearest exact {@code Quadruple} value by less than
   * <span style="white-space:nowrap">{@code 0.5 - 1e-17}</span>
   * of the least significant bit of the mantissa of the latter, it gets rounded
   * down to the aforementioned {@code Quadruple} value.<br>
   * If it is greater by 0.5 LSB or more, it gets rounded up to the greater adjacent
   * Quadruple value.<br>
   * In cases when difference between the input value and the nearest {@code Quadruple}
   * value is between
   * <span style="white-space:nowrap">{@code (0.5 - 1e-17) * LSB}</span>
   *  and <span style="white-space:nowrap">{@code 0.5 * LSB}</span>,
   * the direction of the rounding is unpredictable.
   *
   * Expressing it via formulas,
   * <pre>
   * (1 + (n + d) * 2^-128) * 2^e ==&gt; (1 + n * 2^-128) * 2^e, if d &lt;= 0.5 - 1e-17;
   * (1 + (n + d) * 2^-128) * 2^e ==&gt; (1 + (n + 1) * 2^-128) * 2^e, if d =&gt; 0.5.</pre>
   * where <b>n</b> is an integer less than {@code 2^128}, <b>e</b>
   * is the exponent of the {@code Quadruple}.<br><br>
   * For example,
   * {@code 1.5 + 0.5 * 2^-128}, that equals<br>
   * {@code 1.500000000000000000000000000000000000001469367938527859384960921...}<br>
   * gets rounded up to<br>
   * {@code 1.5000000000000000000000000000000000000029387}, whose mantissa is {@code 0x8000_..._0001},<br>
   * while {@code 1.5 + (0.5 - 1e-17) * 2^-128}, that equals to<br>
   * {@code 1.500000000000000000000000000000000000001469367938527859355573561...}<br>
   * gets rounded down to 1.5, whose mantissa is {@code 0x8000_..._0000}.<br>
   * The values between the two may get rounded either up or down.
   *
   *
   * @param source the String to be parsed
   * @throws NullPointerException if the input string is {@code null}
   * @throws NumberFormatException if the input string does not contain valid value
   * @return this instance with the newly assigned value
   */
  public Quadruple assign(String source) throws NullPointerException, NumberFormatException {
    return NumberParser.parse(source, this);
  } // public Quadruple assign(String source) throws NullPointerException, NumberFormatException {

  /**
   * Converts the given value to {@code Quadruple}, and assigns it to the instance.<br>
   * If the source value can't be represented as {@code Quadruple} exactly, it gets rounded to a 129-bit value
   * (implicit unity + 128 bits of the fractional part of the mantissa) according to the 'half-up' rule.
   * Due to limited precision of computing of great powers of two, the input values
   * that differ from exact values of corresponding {@code Quadruples} by a value that lies between
   * {@code (0.5 - 1e-40) * LSB} and {@code 0.5 * LSB} may get rounded either up or down.
   * @param value the value to be assigned.
   * @throws NullPointerException if the parameter is {@code null}
   * @return this instance with the newly assigned value
   */
  public Quadruple assign(BigDecimal value) {

    if (value.signum() == 0) return assignZero();

    value = value.stripTrailingZeros();                 // Eliminates extra work and thus speeds up in some cases
    negative = value.signum() < 0;
    if (negative) value = value.negate();

    long exp2 = findBinaryExponent(value);              // Binary exponent, unbiased, = floor(log2(value))
    if (!inAcceptableRange(exp2))                       // May the value fit in the range?
      return assignBoundaryValue(exp2);                 // no, return 0 or Infinity

    final boolean expNegative = exp2 < 0;
    exp2 = Math.abs(exp2);

    BigDecimal mant2 = findBinaryMantissa(value, exp2, expNegative);  // Binary mantissa as BigDecimal

    if (mant2.compareTo(BigDecimal.ONE) < 0) {          // Correct possible logarithm inaccuracy
      mant2 = mant2.multiply(BD_TWO);
      exp2 += expNegative? 1 : -1;
    } else if (mant2.compareTo(BD_TWO) >= 0) {
      mant2 = mant2.divide(BD_TWO);
      exp2 += expNegative? -1 : 1;
    }

    exp2 = assigndMantValue(mant2, exp2, expNegative);  // assigns the mantissa to mantHi, mantLo. May round up and correct exp2
    exp2 = (expNegative? -exp2 : exp2) + EXPONENT_BIAS; // Make it biased

    if (exp2 <= 0)                                      // subnormal
      exp2 = makeSubnormal(exp2);
    exponent = (int)exp2;

    if (exponent == EXPONENT_OF_INFINITY)                            // if after rounding up, it has grown up to Infinity
      mantHi = mantLo = 0;

    return this;
  } // public Quadruple assign(BigDecimal value) {

  /**
   * Builds a Quadruple value from the given low-level parts and assigns it to the instance.<br>
   * Treats the {@code exponent} parameter as the biased exponent value,
   * so that its value equal to {@link #EXPONENT_OF_ONE} ({@code 0xFFFF_FFFEL})
   * corresponds to the {@code Quadruple} value of 1.0.
   * @param negative   the sign of the value ({@code true} for negative)
   * @param exponent  Binary exponent (biased, so that 0x7FFF_FFFF corresponds to 2^0)
   * @param mantHi  The most significant 64 bits of the fractional part of the mantissa
   * @param mantLo  The least significant 64 bits of the fractional part of the mantissa
   * @return A {@code Quadruple} containing the value built of the given parts
   */
  public Quadruple assign(boolean negative, int exponent, long mantHi, long mantLo) {
    this.negative = negative;
    this.exponent = exponent;
    this.mantHi = mantHi;
    this.mantLo = mantLo;
    return this;
  } // public Quadruple assign(boolean negative, int exponent, long mantHi, long mantLo) {

  /**
   * Builds a Quadruple value from the given low-level parts and assigns it to the instance.<br>
   * Treats the {@code exponent} parameter as the unbiased exponent value,
   * whose {@code 0} value corresponds to the {@code Quadruple} value of 1.0.
   * @param negative   the sign of the value ({@code true} for negative)
   * @param exponent  Binary exponent (unbiased, 0 means 2^0)
   * @param mantHi  The higher 64 bits of the fractional part of the mantissa
   * @param mantLo  The lower 64 bits of the fractional part of the mantissa
   * @return A Quadruple containing the value built of the given parts
   */
  public Quadruple assignWithUnbiasedExponent(boolean negative, int exponent, long mantHi, long mantLo) {
    this.negative = negative;
    this.exponent = exponent + EXPONENT_BIAS;
    this.mantHi = mantHi;
    this.mantLo = mantLo;
    return this;
  } // public Quadruple assignWithNormalExponent(boolean negative, int exponent, long mantHi, long mantLo) {

  /**
   * Builds a non-negative Quadruple value from the given low-level parts and assigns it to the instance.<br>
   * Treats the {@code exponent} parameter as the biased exponent value,
   * so that its value equal to {@link #EXPONENT_OF_ONE} ({@code 0xFFFF_FFFEL})
   * corresponds to the {@code Quadruple} value of 1.0.
   * @param exponent  Binary exponent (biased, 0x7FFF_FFFF means 2^0)
   * @param mantHi  The most significant 64 bits of the fractional part of the mantissa
   * @param mantLo  The least significant 64 bits of the fractional part of the mantissa
   * @return A Quadruple containing the value built of the given parts
   */
  public Quadruple assign(int exponent, long mantHi, long mantLo) {
    return assign(false, exponent, mantHi, mantLo);
  } // public Quadruple assign(int exponent, long mantHi, long mantLo) {

  /**
   * Builds a non-negative Quadruple value from the given low-level parts and assigns it to the instance.<br>
   * Treats the {@code exponent} parameter as the unbiased exponent value,
   * whose {@code 0} value corresponds to the {@code Quadruple} value of 1.0.
   * @param exponent  Binary exponent (unbiased, 0 means 2^0)
   * @param mantHi  The most significant 64 bits of the fractional part of the mantissa
   * @param mantLo  The least significant 64 bits of the fractional part of the mantissa
   * @return A Quadruple containing the value built of the given parts
   */
  public Quadruple assignWithUnbiasedExponent(int exponent, long mantHi, long mantLo) {
    return assignWithUnbiasedExponent(false, exponent, mantHi, mantLo);
  } // public Quadruple assignWithNormalExponent(int exponent, long mantHi, long mantLo) {

  /**
   * Builds a Quadruple from the low-level parts given as an array of {@code long}.<br>
   * The elements of the array are expected to contain the following values:<pre> {@code
   * value[0] -- sign flag in bit 63 (1 means negative),
   *             biased exponent in bits 31 .. 0
   * value[1] -- The most significant 64 bits of the fractional part of the mantissa
   * value[2] -- The most significant 64 bits of the fractional part of the mantissa}</pre>
   * @param value array of {@code long} containing the low-level parts of the Quadruple value
   * @return the instance after assigning it the required value
   * @see #toLongWords()
   */
  public Quadruple assign(long[] value) {
    if (value.length < 3)
      throw new IndexOutOfBoundsException("value.length must be greater than or equal 3");

    negative = (value[0] & HIGH_BIT) != 0;
    exponent = (int)(value[0] & LOWER_32_BITS);
    mantHi = value[1]; mantLo = value[2];
    return this;
  } // public Quadruple assign(long[] value) {


  /**
   * Assigns the value of a IEEE-754 quadruple value passed in as an array
   * of two {@code long}s containing the 128 bits of the IEEE-754 quadruple to the given instance.<br>
   * The passed in array of longs is expected to be big-endian, in other words the {@code value[0]}
   * should contain the sign bit, the exponent, and the most significant 48 bits of the significand.<br>
   * The argument remains unchanged.
   * @param value an array of two longs, containing the 128 bits of the IEEE-754 quadruple value to be assigned
   * @return this instance with the newly assigned value
   * @throws IllegalArgumentException if the length of the input array is not 2
   */
  public Quadruple assignIeee754(long[] value) throws IllegalArgumentException {
    if (value.length != 2)
      throw new IllegalArgumentException("the length of the input array must be 2, not " + value.length);

    negative = value[0] < 0;
    final int ieeeExponent = (int)((value[0] & IEEE754_EXPONENT_MASK) >>> 48);

    if (ieeeExponent == LOWER_15_BITS) {                                  // NaN or Infinity
      if (((value[0] & LOWER_48_BITS) | value[1]) != 0)                   // Significand is not 0: NaN
        return assignNaN();
      return assignInfinity(false);
    }

    if (ieeeExponent == 0) {                        // 0 or subnormal
      if (((value[0] & LOWER_48_BITS) | value[1]) == 0)                   // Significand is 0: 0 or -0
        return assignZero(false);
    }

    mantHi = value[0] & LOWER_48_BITS; mantLo = value[1];
    final int expDiff = EXPONENT_BIAS - IEEE754_EXPONENT_BIAS;
    if (ieeeExponent == 0) {                                              // Subnormal
      int shift = Long.numberOfLeadingZeros(mantHi) + 1;
      if (shift < 64) {                                                   // There are significant bits in the high word
        mantHi = mantHi << shift | mantLo >>> (64 - shift);
        mantLo <<= shift;
        exponent = expDiff + 17 - shift;
      } else if (shift == 64) {
        mantHi = mantLo;
        mantLo = 0;
        exponent = expDiff  + 17 - shift;
      } else {
        shift = Long.numberOfLeadingZeros(mantLo) + 1;
        mantHi = shift == 64? 0: mantLo << shift;                         // shift == 64 means it's smallest subnormal
        mantLo = 0;
        exponent = expDiff  + 17 - shift - 64;
      }
    } else {                                                              // normal
      mantHi = mantHi << 16 | mantLo >>> (48);
      mantLo <<= 16;
      exponent = ieeeExponent + expDiff;
    }
    return this;
  }

  /**
   * Assigns the value of a IEEE-754 quadruple value passed in as an array
   * of 16 {@code byte}s containing the 128 bits of the IEEE-754 quadruple to the given instance.<br>
   * The passed in array of longs is expected to be big-endian, in other words the {@code value[0]}
   * should contain the sign bit, and the high-order 7 bits of the IEEE-754 quadruple's exponent,
   * and the {@code value[15]} is expected to contain the least significant 8 bits of the significand.<br>
   * The argument remains unchanged.
   * @param value an array of 16 bytes, containing the 128 bits of the IEEE-754 quadruple value to be assigned
   * @return this instance with the newly assigned value
   * @throws IllegalArgumentException if the length of the input array is not 16
   */
  public Quadruple assignIeee754(byte[] value) {
    if (value.length != 16)
      throw new IllegalArgumentException("the length of the input array must be 16, not " + value.length);

    return assignIeee754(mergeBytesToLongs(value));
  }


  /**
   * Assigns the value of {@code +Infinity} to this instance.
   * @return this instance with the value of {@code POSITIVE_INFINITY}
   */
  public Quadruple assignPositiveInfinity() {
    negative = false; exponent = EXPONENT_OF_INFINITY;
    mantHi = 0; mantLo = 0;
    return this;
  } // public Quadruple assignPositiveInfinity() {

  /**
   * Assigns the value of {@code -Infinity} to this instance.
   * @return this instance with the value of {@code NEGATIVE_INFINITY}
   */
  public Quadruple assignNegativeInfinity() {
    negative = true; exponent = EXPONENT_OF_INFINITY;
    mantHi = 0; mantLo = 0;
    return this;
  } // public Quadruple assignNegativeInfinity() {

  /**
   * Assigns the value of "Not a Number" ({@code NaN}) to this instance.
   * @return this instance with the value of {@code NaN}
   */
  public Quadruple assignNaN() {
    negative = false; exponent = EXPONENT_OF_INFINITY;
    mantHi = 0x8000_0000_0000_0000L; mantLo = 0;
    return this;
  } // public Quadruple assignNaN() {


  /* **********************************************************************
   ******* Conversions to other types *************************************
   ************************************************************************/

  protected void ____Conversions____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Converts the value of this {@code Quadruple} to an {@code int} value in a way
   * similar to standard narrowing conversions (e.g., from {@code double} to {@code int}).
   * @return the value of this {@code Quadruple} instance converted to an {@code int}.
   * */
  @Override
  public int intValue() {
    final long exp = (exponent & LOWER_32_BITS) - EXPONENT_BIAS; // Unbiased exponent
    if (exp < 0 || isNaN()) return 0;
    if (exp >= 31)                                              // value <= Integer.MIN_VALUE || value > Integer.MAX_VALUE
      return negative? Integer.MIN_VALUE : Integer.MAX_VALUE;

    final int intValue = exp == 0? 1 : (1 << exp) | (int)(mantHi >>> 64 - exp);  // implicit unity | fractional part of the mantissa, shifted rightwards
    return negative? -intValue : intValue;
  } // public int intValue() {

  /** Converts the value of this {@code Quadruple} to a {@code long} value in a way
   * similar to standard narrowing conversions (e.g., from {@code double} to {@code long}).
   * @return the value of this {@code Quadruple} instance converted to a {@code long}.
   */
  @Override
  public long longValue() {
    final long exp = (exponent & LOWER_32_BITS) - EXPONENT_BIAS; // Unbiased exponent
    if (exp < 0 || isNaN()) return 0;                           // NaN.longValue == 0
    if (exp >= 63)                                              // value <= Long.MIN_VALUE || value > Long.MAX_VALUE
      return negative? Long.MIN_VALUE : Long.MAX_VALUE;

    final long longValue = exp == 0? 1: (1L << exp) | (mantHi >>> 64 - exp);  // implicit unity | fractional part of the mantissa, shifted rightwards
    return negative? -longValue : longValue;
  } // public long longValue() {

  /** Converts the value of this {@code Quadruple} to a {@code float} value in a way
   * similar to standard narrowing conversions (e.g., from {@code double} to {@code float}).
   * @return the value of this {@code Quadruple} instance converted to a {@code float}.
   * */
  @Override
  public float floatValue() {
    return (float)doubleValue();
  } // public float floatValue() {

  /** Converts the value of this {@code Quadruple} to a {@code double} value in a way
   * similar to standard narrowing conversions (e.g., from {@code double} to {@code float}).
   * Uses 'half-even' approach to the rounding, like {@code BigDecimal.doubleValue()}
   * @return the value of this {@code Quadruple} instance converted to a {@code double}.
   * */
  @Override
  public double doubleValue() {
    if (exponent == 0)                  // All subnormal Quadruples are also converted into 0d
      return negative? -0.0d : 0.0d;

    if (exponent == EXPONENT_OF_INFINITY)
      return (mantHi != 0 || mantLo != 0 )? Double.NaN :
                                            negative ?
                                              Double.NEGATIVE_INFINITY :
                                              Double.POSITIVE_INFINITY;

    int expD = exponent - EXPONENT_BIAS;                   // Unbiased, to range -0x8000_0000 ... 0x7FFF_FFFF
    if (expD > EXP_0D)                                  // Out of range
      return negative? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

    if (expD < -(EXP_0D + 52))                          // below Double.MIN_VALUE -- return 0
      return negative? -0.0d : 0.0d;

    if (expD < -(EXP_0D - 1)) {                         // subnormal
      long lValue = (mantHi >>> 12) | 0x0010_0000_0000_0000L; // implied higher bit;
      lValue = lValue + (1L << -EXP_0D - expD) >>> -EXP_0D - expD + 1;
      if (negative) lValue |= DOUBLE_SIGN_MASK;
      return Double.longBitsToDouble(lValue);
    }

    // normal case
    long dMant = mantHi >>> 12;
    if ((mantHi & HALF_DOUBLES_LSB) != 0)               // The highest bit, of those was shifted out, is 1 -- round it up
       if (     ( ((mantHi & (HALF_DOUBLES_LSB - 1)) | mantLo) != 0 )  // greater than just n + LSB * 0.5
             || (dMant & 1) != 0) {                     // 20.12.22 14:09:22 + Half-even approach, like in BigDecimal.doubleValue()
          dMant++;
          if ((dMant & DOUBLE_EXP_MASK) != 0) {         // Overflow of the mantissa, shift it right and increase the exponent
            dMant = (dMant & ~DOUBLE_IMPLIED_MSB) >>> 1;
            expD++;
          }
        }

    if (expD > EXP_0D)
      return negative? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    final long lValue = ((long)(expD + EXP_0D) << 52) | dMant | (negative ? DOUBLE_SIGN_MASK : 0);
    return Double.longBitsToDouble(lValue);
  } // public double doubleValue() {

  /**
   * Builds and returns a {@code BigDecimal} instance holding the same value as the given Quadruple
   * (rounded to 100 significant decimal digits).
   * @return a {@code BigDecimal} holding the same value as the given {@code Quadruple}
   * @throws NumberFormatException if the value of the instance is not convertible to {@code BigDecimal}
   * (i.e. it is {@code Infinity}, {@code -Infinity},  or {@code NaN})
   */
  public BigDecimal bigDecimalValue() throws NumberFormatException {
    checkNaNInfinity();                                 // Throws exception if the value is not convertible
    if (isZero()) return BigDecimal.ZERO;

    BigDecimal result = buildBDMantissa();              // binary mantissa (in range 1.0 - 1.999..)

    final int exponent = unbiasedExponent();            // Binary exponent in normal (unbiased) form
    final boolean expNegative = exponent < 0;
    final BigDecimal powerOf2 = twoRaisedTo(Math.abs(exponent));  // 2^|exp2|

    // Limit precision with some reasonable value, say, 45 digits after the point
    if (expNegative)   result = result.divide(powerOf2, MC_100_HALF_EVEN);     // mantissa * 2^|exp2|
    else               result = result.multiply(powerOf2, MC_100_HALF_EVEN);  // mantissa / 2^|exp2|

    result = result.stripTrailingZeros();
    return negative? result.negate() : result;
  } // public BigDecimal bigDecimalValue() throws NumberFormatException {

  /** A number of digits to use in the toString() method */
  private static final int SIGNIFICANT_DIGITS = 40;

  /**
   * Returns a decimal string representation of the value of this {@code Quadruple}
   * in a scientific (exponential) notation, rounded to 43 digits after point.<br>
   * For other String representations, see {@code format(String)}
   * @see #format(String)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    if (exponent == EXPONENT_OF_INFINITY)                            // infinity or NaN
      return ((mantHi | mantLo) != 0)? "NaN" : (negative)? "-Infinity" : "Infinity";

    if ((exponent | mantHi | mantLo) == 0)              // 0.0 / -0.0
      return negative? "-0.0" : "0.0";

    final int exp2 = exponent - EXPONENT_BIAS;             // Unbiased exponent

    // Decimal mantissa M and decimal exponent E are found from the binary mantissa m and the binary exponent e as
    // M = m * 2^e / 10^E.
    // Since E is always either (floor((e+1)*log(2))) or (floor((e+1)*log(2)) - 1),
    // we can find E' = floor((e+1)*log(2)) and M' = m * 2^e / 10^E',
    // and if M' < 1, we just multiply it by 10 and subtract 1 from E' to obtain M and E.
    // To avoid division by a power of 10, we multiply the mantissa by a power of 2,
    // encoded in a form of (what I call) quasidecimal number,
    // that is an array of 4 longs, where qd[0] contains the decimal exponent of the number,
    // and qd[1] - qd[3] contain 192 bits of the decimal mantissa, divided by 10,
    // so that 1.0 looks like 0x1999_9999..., and 9.9999999... looks like 0xFFFF_FFFF_.
    // (in other words, it ranges from 1/10 * 2^192 to 9.999... * 2^192)
    // The result is in the same form, that allows for easy conversion into a string of decimal digits.

    long[] mant10 = BUFFER_4x64_A;
    if (exponent == 0) {            // Subnormal, multiply by (2^-2147483646 / 10^-646456993)
      mant10 = multMantByMinNormal(mant10); // special multiplication by MIN_NORMAL == 2^-2147483646
    } else {                        // Find the quasidecimal value of 2^exp2 and multiply it by the mantissa
      mant10 = multMantByPowerOfTwo(powerOfTwo(exp2), mant10);
    }
    final StringBuilder mantStr = decimalMantToString(mant10, SIGNIFICANT_DIGITS);  // to a string of decimal digits
    final int exp10 = (int)mant10[0] - 1;

    mantStr.insert(1, '.');                               // point after the 1st digit
    mantStr.append("e" + String.format("%+03d", exp10));  // exponent
    if (negative) mantStr.insert(0, '-');                 // sign

    return mantStr.toString();
  } // public String toString()

  /**
   * Returns a {@code String} representing the value
   * of this instance in a form defined by the {@code format} parameter.
   * If the value is NaN or +/-Infinity, returns respectively "NaN", "Infinity", or "-Infinity",
   * otherwise formats the value in accordance with the rules
   * used for formatting {@code BigDecimal} values, like in String.format("%9.3f", value).
   * @param format A pattern to format the value
   * @return a {@code String} representation of this value, formatted in accordance with the
   * {@code format} parameter
   */
  public String format(String format) {
    if (isNaN() || isInfinite())
      return this.toString();
    final BigDecimal bdValue = bigDecimalValue();
    return String.format(format, bdValue);
  } // public String format(String format) {

  /**
   * Returns a {@code String} containing a hexadecimal representation
   * of the instance's value, consisting of sign, two 64-bit words of mantissa, and
   * exponent preceded by letter 'e', with '_' separating the tetrads of hexadecimal digits.
   * This way, the value -1.5 is represented by the string
   * {@code -8000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff}
   * @return a string containing a hexadecimal representation
   */
  public String toHexStr() {
    return hexStr(this);
  } //public String toHexStr() {

  /**
   * Returns the fields of the instance that make up it's value as
   * an array of {@code long}s.<br>
   * The elements of the array contain the following values:<pre> {@code
   * value[0] -- sign flag in bit 63 (1 means negative),
   *             biased exponent in bits 31 -- 0
   * value[1] -- The higher 64 bits of the fractional part of the mantissa
   * value[2] -- The lower 64 bits of the fractional part of the mantissa}</pre>
   * @return an array of 3 {@code long}s containing the contents of the
   * instance's fields, as described above
   * @see #assign(long[])
   */
  public long[] toLongWords() {
    final long[] result = new long[3];
    final long signBit = negative? 0x8000_0000_0000_0000L : 0;
    result[0] = signBit | (exponent & LOWER_32_BITS);
    result[1] = mantHi;
    result[2] = mantLo;
    return result;
  } // public long[] toLongWords() {

  /**
   * Returns the 128 bits of an IEEE-754 quadruple precision number nearest to the value
   * of {@code this} instance as an array of two {@code long}s, containing a physical representation
   * of the standard IEEE-754 quadruple-precision floating-point number.<br>
   * The order of words is big-endian, so that the sign bit, exponent
   * and 48 most significant bits of the mantissa are returned in result[0],
   * and 64 least significant bits of the mantissa in result[1].
   * The 128-bit significand of this instance is rounded to fit to the 112 bits of the
   * IEEE-754 quadruple. The rounding mode is half-up, i.e. if the exact value of the instance
   * differs from the nearest IEEE-754 quadruple value by 1/2 of LSB of the IEEE-754
   * quadruple's significand, it gets rounded up.
   * The values whose magnitude exceed the maximum possible value of IEEE-754 Quadruple
   * (namely, 1.18973149535723176508575932662800702 * 10^4932) plus half of its mantissa'a LSB
   * are converted to {@code Infinity} or {@code -Infinity}, depending on the sign,
   * the values with magnitudes less than minimum normal IEEE-754 quadruple value
   * ({@code 3.36210314311209350626267781732175260 * 10^-4932})
   * but greater or equal to {@code 6.4751751194380251109244389582276466 * 10^-4966}
   * are converted to subnormal IEEE-754 values, and the values whose magnitude is less
   * than {@code 6.4751751194380251109244389582276466 * 10^-4966} (minimum positive value of of IEEE-754 Quadruple)
   * are converted to 0 or -0, depending on the sign of {@code this} instance.
   *
   * @return an array of two longs containing the 128 bits of the IEEE-745 Quadruple
   * value nearest to the value of this instance.
   */
  public long[] toIeee754Longs() {
    final long[] result = new long[2];
    if (exponent == EXPONENT_OF_INFINITY) {         // NaN or Infinity;
      result[0] = (mantHi != 0 || mantLo != 0 )? IEEE754_NAN_LEAD :
                                            negative ?
                                                IEEE754_MINUS_INFINITY_LEAD :
                                                IEEE754_INFINITY_LEAD;
      return result;
    }

    final int unbiasedExponent = unbiasedExponent();
    if (unbiasedExponent > IEEE754_MAX_EXPONENT) {
      result[0] = negative ?  IEEE754_MINUS_INFINITY_LEAD :
                              IEEE754_INFINITY_LEAD;
      return result;
    } else if (unbiasedExponent < IEEE754_MIN_EXPONENT) {
      if (negative)
        result[0] = IEEE754_MINUS_ZERO_LEAD;
      return result;
    } else if (unbiasedExponent >= IEEE754_MIN_NORMAL_EXPONENT) {
      return makeNormal_IEEELongs(result);
    } else {
      return makeSubnormal_IEEELongs(result);
    }
  }

  /**
   * Returns the 128 bits of an IEEE-754 quadruple precision number nearest to the value
   * of {@code this} instance as an array of 16 {@code byte}s, containing a physical representation
   * of the standard IEEE-754 quadruple-precision floating-point number.<br>
   * The order of bytes is big-endian, so that the sign bit and the most significant bits
   * of the exponent is returned in result[0], and the least significant bits
   * of the mantissa in result[15].
   * The 128-bit significand of this instance is rounded to fit to the 112 bits of the
   * IEEE-754 quadruple. The rounding mode is half-up, i.e. if the exact value of the instance
   * differs from the nearest IEEE-754 quadruple value by 1/2 of LSB of the IEEE-754
   * quadruple's significand, it gets rounded up.
   * The values whose magnitude exceed the maximum possible value of IEEE-754 Quadruple
   * (namely, 1.18973149535723176508575932662800702 * 10^4932) plus half of its mantissa'a LSB
   * are converted to {@code Infinity} or {@code -Infinity}, depending on the sign,
   * the values with magnitudes less than {@code 3.36210314311209350626267781732175260 * 10^-4932}
   * but greater or equal to {@code 6.4751751194380251109244389582276466 * 10^-4966}
   * are converted to subnormal IEEE-754 values,
   * and the values whose magnitude is less than {@code 6.4751751194380251109244389582276466 * 10^-4966}
   * (minimum positive value of of IEEE-754 Quadruple)
   * are converted to 0 or -0, depending on the sign of {@code this} instance.
   *
   * @return an array of bytes containing the value of {@code this} instance
   * as a physical representation of the nearest IEEE-745 Quadruple value,
   * in the big-endian order.
   */
  public byte[] toIeee754Bytes() {
    return splitToBytes(toIeee754Longs());
  }


  /* ***********************************************************************************
   ****** Comparisons ******************************************************************
   *********************************************************************************** */

  protected void ____Comparisons____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Compares the value of this instance with the value of the specified instance.
   * @param other the {@code Quadruple} to compare with
   * @return a negative integer, zero, or a positive integer as the value of this instance is less than,
   * equal to, or greater than the value of the specified instance.
   */
  @Override
  public int compareTo(Quadruple other) {

    if (isNaN())
      return other.isNaN()? 0 : 1;                      // NaN is considered to be greater than any other value
    if (other.isNaN())
      return -1;

    // For Doubles, -0 < 0. Do it the same way
    if (negative != other.negative)                     // If signs differ
      return negative? -1: 1;

    // Signs are equal -- compare exponents (unsigned)
    int result = Integer.compareUnsigned(exponent, other.exponent);

    if (result == 0)                                    // If exponents are equal, compare mantissas
      result = Long.compareUnsigned(mantHi, other.mantHi);
    if (result == 0)
      result = Long.compareUnsigned(mantLo, other.mantLo);

    if (negative) result = -result;                     // both are negative, invert result
    return result;
  } // public int compareTo(Quadruple other) {

  /**
   * Compares the value of this instance with the specified {@code long} value.
   * The value of the argument is converted to Quadruple, and then two Quadruple values
   * are compared by {@link #compareTo(Quadruple)}
   * @param other the {@code long} value to compare with
   * @return a negative integer, zero, or a positive integer as the value of this instance is less than,
   * equal to, or greater than the specified {@code long} value.
   */
  public int compareTo(long other) {
    return compareTo(new Quadruple(other));
  } //public int compareTo(long other) {

  /**
   * Compares the value of this instance with the specified {@code double} value.
   * The value of the argument is converted to Quadruple,
   * and then two Quadruple values are compared by {@link #compareTo(Quadruple)}
   * @param other the {@code double} value to compare with
   * @return a negative integer, zero, or a positive integer as the value of this instance is less than,
   * equal to, or greater than the specified {@code double} value.
   */
  public int compareTo(double other) {
    return compareTo(new Quadruple(other));
  } // public int compareTo(double other) {

  /**
   * Indicates whether the other {@code Quadruple} is equal to this one.
   * @param obj the object to compare with
   * @return {@code true} if the given object is Quadruple and its value is equal to
   * the value of this {@code Quadruple} instance, {@code false} otherwise.
   *
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Quadruple)) return false;
    final Quadruple other = (Quadruple)obj;
    if (isNaN() && other.isNaN())                       // NaNs are like animals: they are all equal (but some NaN are more equal than others)
      return true;
    return
       negative == other.negative                       // For Doubles, -0 != 0. Do it the same way
       && exponent == other.exponent
       && mantHi == other.mantHi
       && mantLo == other.mantLo;
  } // public boolean equals(Object obj) {

  /** Computes a hashcode for this {@code Quadruple},
   * based on the values of its fields.
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    if (isNaN())                                        // Since NaNs are considered equal, they must return the same hashCode
      return HASH_CODE_OF_NAN;
    final int prime = 31;
    int result = 1;
    result = prime * result + exponent;
    result = prime * result + (int) (mantHi ^ (mantHi >>> 32));
    result = prime * result + (int) (mantLo ^ (mantLo >>> 32));
    result = prime * result + (negative ? 1231 : 1237);
    return result;
  } // public int hashCode() {

  /**
   * Compares the values of two instances.
   * @param q1 the instance to compare with the other one
   * @param q2 the instance to compare with
   * @return a negative integer, zero, or a positive integer as the value of the first
   * instance is less than, equal to, or greater than the value of the second instance.
   */
  public static int compare(Quadruple q1, Quadruple q2) {
    return q1.compareTo(q2);
  } // public static int compare(Quadruple q1, Quadruple q2) {

  /**
   * Compares the magnitude (absolute value) of this instance
   * with the magnitude of the other instance.
   * @param other the Quadruple to compare with
   * @return 1 if this instance is greater in magnitude than the {@code other} instance,
   * 0 if the argument is equal in magnitude to this instance, -1 if this instance is less in magnitude, than the argument
   *
   */
  public int compareMagnitudeTo(Quadruple other) {
    // 20.10.24 18:44:39 Regarding NaNs, behave like doubles
    if (isNaN())
      return other.isNaN()? 0 : 1;
    if (other.isNaN())
      return -1;

    if (isInfinite())
      return other.isInfinite()? 0 : 1;
    if (other.isInfinite())
      return -1;

    int result;
    if ((result = Integer.compareUnsigned(exponent, other.exponent)) != 0)
      return result;
    if ((result = Long.compareUnsigned(mantHi, other.mantHi)) != 0) // If exponents are equal, compare mantissas
      return result;
    return Long.compareUnsigned(mantLo, other.mantLo);
  } // public int compareMagnitudeTo(Quadruple other) {

  /**
   * Compares the magnitudes (absolute values) of the two Quadruples.
   * @param q1 the instance to compare with the other one
   * @param q2 the instance to compare with
   * @return a negative integer, zero, or a positive integer as the magnitude of the first
   * instance is less than, equal to, or greater than the magnitude of the second instance.
   */
  public static int compareMagnitudes(Quadruple q1, Quadruple q2) {
    return q1.compareMagnitudeTo(q2);
  } // public static int compareMagnitudes(Quadruple q1, Quadruple q2) {

  /**
   * Returns a new instance of {@code Quadruple} with the value of the
   * maximum of the values of the operands.
   * @param q1 first operand to compare
   * @param q2 first operand to compare
   * @return a new instance of {@code Quadruple} whose value is
   *      equal to the value of the greater of the operands.
   */
  public static Quadruple max(Quadruple q1, Quadruple q2) {
    if (q1.compareTo(q2) > 0)
      return new Quadruple(q1);
    else
      return new Quadruple(q2);
  }

  /**
   * Returns a new instance of {@code Quadruple} with the value of the
   * minimum of the values of the operands.
   * @param q1 first operand to compare
   * @param q2 first operand to compare
   * @return a new instance of {@code Quadruple} whose value is
   *      equal to the value of the lesser of the operands.
   */
  public static Quadruple min(Quadruple q1, Quadruple q2) {
    if (q1.compareTo(q2) < 0)
      return new Quadruple(q1);
    else
      return new Quadruple(q2);
  }

  /**
   * Assigns to this instance the maximum of the
   * values of {@code this} instance and the operand.
   * @param other the operand to compare with
   * @return {@code this} instance, after setting its value
   *      to the value of the greater of {@code this} and the operand.
   */
  public Quadruple assignMax(Quadruple other) {
    if (compareTo(other) < 0)
      assign(other);
    return this;
  }

  /**
   * Assigns to this instance the minimum of the
   * values of {@code this} instance and the operand.
   * @param other the operand to compare with
   * @return {@code this} instance, after setting its value
   *      to the value of the lesser of {@code this} and the operand.
   */
  public Quadruple assignMin(Quadruple other) {
    if (compareTo(other) > 0)
      assign(other);
    return this;
  }

  /* ***********************************************************************************
   ****** Arithmetics ******************************************************************
   *********************************************************************************** */

  protected void ________Arithmetic_________ () {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Adds the value of the given {@code Quadruple} summand to the value of this Quadruple.
   * The instance acquires a new value that equals the sum of the previous value and the value of the summand.
   * @param summand the value to add
   * @return the reference to this object, which holds a new value that equals
   * the sum of its previous value and the value of the summand
   */
  public Quadruple add(Quadruple summand) {
    if (isNaN() || summand.isNaN()) return assignNaN(); // NaN + whatever = NaN;

    if (isInfinite()) {
      if (summand.isInfinite() && (negative != summand.negative))
        return assignNaN();                             // -Infinity + Infinity = NaN
      else return this;                                 // Infinity + X = Infinity
    }

    if (summand.isInfinite()) return assign(summand);   // x + Infinity = Infinity regardless of their signs

    if (summand.isZero() ) {
      if (isZero())                                     // both are zeros
        if (summand.isNegative() && isNegative())
          negative = true;                              // -0 + -0 = -0
        else negative = false;
      return this;                                      // X + 0 = X
    }

    if (isZero()) return assign(summand);               // 0 + X = X

    // Both are regular numbers
    if (negative == summand.negative)                   // same signs
      return addUnsigned(summand);                      // Does not affect sign
    else {                                              // signs differ
      final boolean wasNegative = negative;
      subtractUnsigned(summand);                        // Subtracts ignoring sings, returns negative if the summand is greater in magnitude
      negative ^= wasNegative;                          // If signs differ and summand is greater in magnitude, the sign gets inverted
    }
    return this;
  } // public Quadruple add(Quadruple summand) {

  /**
   * Adds the value of the given {@code long} summand to the value of this Quadruple.
   * The value of the {@code long} operand is preliminarily converted to a {@code Quadruple} value.
   * The instance acquires the new value that equals the sum of the previous value and the value of the summand.
   * @param summand the value to add
   * @return the reference to this object, which holds a new value that equals
   * the sum of its previous value and the value of the summand
   */
  public Quadruple add(long summand) {
    return add(new Quadruple(summand));
  } // public Quadruple add(long summand) {

  /**
   * Adds the value of the given {@code double} summand to the value of this Quadruple.
   * The value of the {@code double} operand is preliminarily converted to a {@code Quadruple} value.
   * The instance acquires the new value that equals the sum of the previous value and the value of the summand.
   * @param summand the value to add
   * @return the reference to this object, which holds a new value that equals
   * the sum of its previous value and the value of the summand
   */
  public Quadruple add(double summand) {
    return add(new Quadruple(summand));
  } // public Quadruple add(double summand) {

  /** Adds the value of the given {@code Quadruple op2} to the value of {@code Quadruple op1}
   * and creates a new instance of Quadruple containing the sum.
   * The operands remain unchanged.
   * @param op1 the first operand to add
   * @param op2 the second operand to add
   * @return a new instance of Quadruple containing the sum of the operands
   */
  public static Quadruple add(Quadruple op1, Quadruple op2) {
    op1 = new Quadruple(op1);
    return op1.add(op2);
  } // public static Quadruple add(Quadruple op1, Quadruple op2) {

  /** Adds the value of the given {@code long op2} to the value of {@code Quadruple op1}
   * and creates a new instance of Quadruple containing the sum.
   * The value of the {@code long} operand is preliminarily converted to a {@code Quadruple} value.
   * The Quadruple operand remains unchanged.
   * @param op1 the first operand to add
   * @param op2 the second operand to add
   * @return a new instance of Quadruple containing the sum of the operands
   */
  public static Quadruple add(Quadruple op1, long op2) {
    op1 = new Quadruple(op1);
    return op1.add(op2);
  } // public static Quadruple add(Quadruple op1, long op2) {

  /** Adds the value of the given {@code double op2} to the value of {@code Quadruple op1}
   * and creates a new instance of Quadruple containing the sum.
   * The value of the {@code double} operand is preliminarily converted to a {@code Quadruple} value.
   * The Quadruple operand remains unchanged.
   * @param op1 the first operand to add
   * @param op2 the second operand to add
   * @return a new instance of Quadruple containing the sum of the operands
   */
  public static Quadruple add(Quadruple op1, double op2) {
    op1 = new Quadruple(op1);
    return op1.add(op2);
  } // public static Quadruple add(Quadruple op1, double op2) {

  /**
   * Subtracts the value of the given {@code Quadruple} subtrahend from the value of this Quadruple.
   * The instance acquires a new value that equals the difference between the previous value and the value of the subtrahend.
   * @param subtrahend the value to be subtracted from the current value of this Quadruple
   * @return the reference to this object, which holds a new value that equals
   * the difference between its previous value and the value of the subtrahend
   */
  public Quadruple subtract(Quadruple subtrahend) {
    if (isNaN() || subtrahend.isNaN()) return assignNaN(); // NaN - whatever = NaN;

    if (isInfinite()) {
      if (subtrahend.isInfinite() && (negative == subtrahend.negative))
        return assignNaN();                             // Infinity - Infinity = NaN
      else return this;                                 // Infinity - X = Infinity
    }

    if (subtrahend.isInfinite())
      return assign(subtrahend).negate();               // X - Infinity = -Infinity regardless of their signs

    if (subtrahend.isZero() ) {
      if (isZero())
        if (isNegative() && !subtrahend.isNegative())
          negative = true;                              // -0.0 - 0.0 = -0.0
        else negative = false;                          // 0 - (-0) = 0, -0 - (-0) = 0, 0 - 0 = 0
      return this;                                      // X - 0 = X
    }

    if (isZero()) return assign(subtrahend).negate();   // 0 - X = -X

    // Both are regular numbers
    if (negative != subtrahend.negative)                // Different signs
      return addUnsigned(subtrahend);                   // Does not affect sign, -X - Y = -(X + Y), X - (-Y) = X + Y
    else {
      final boolean wasNegative = negative;             // same sign
      subtractUnsigned(subtrahend);                     // Subtracts irrespective of sings, the result is negative if the subtrahend is greater in magnitude
      negative ^= wasNegative;                          // Minuend was negative and greater in magnitude or positive and less in magnitude than the subtrahend
    }
    return this;
  } // public Quadruple subtract(Quadruple subtrahend) {

  /**
   * Subtracts the value of the given {@code long} subtrahend from the value of this Quadruple.
   * The value of the {@code long} subtrahend is preliminarily converted to a {@code Quadruple} value.
   * The instance acquires a new value that equals the difference between the previous value and the value of the subtrahend.
   * @param subtrahend the value to be subtracted from the current value of this Quadruple
   * @return the reference to this object, which holds a new value that equals
   * the difference between its previous value and the value of the subtrahend
   */
  public Quadruple subtract(long subtrahend) {
    return subtract(new Quadruple(subtrahend));
  } // public Quadruple subtract(long subtrahend) {

  /**
   * Subtracts the value of the given {@code double} subtrahend from the value of this Quadruple.
   * The value of the {@code double} subtrahend is preliminarily converted to a {@code Quadruple} value.
   * The instance acquires a new value that equals the difference between the previous value and the value of the subtrahend.
   * @param subtrahend the value to be subtracted from the current value of this Quadruple
   * @return the reference to this object, which holds a new value that equals
   * the difference between its previous value and the value of the subtrahend
   */
  public Quadruple subtract(double subtrahend) {
    return subtract(new Quadruple(subtrahend));
  } // public Quadruple subtract(double subtrahend) {

  /**
   * Subtracts the value of the {@code Quadruple} {@code subtrahend} from the value of the {@code minuend},
   * creates and returns a new  instance of Quadruple that contains the difference.
   * The operands remain unchanged.
   * @param minuend the value from which the subtrahend is to be subtracted
   * @param subtrahend the value to be subtracted from the minuend
   * @return a new instance of Quadruple containing the difference
   */
  public static Quadruple subtract(Quadruple minuend, Quadruple subtrahend) {
    minuend = new Quadruple(minuend);
    return minuend.subtract(subtrahend);
  } // public static Quadruple subtract(Quadruple minuend, Quadruple subtrahend) {

  /**
   * Subtracts the value of the {@code long} {@code subtrahend} from the value of the {@code minuend},
   * creates and returns a new  instance of Quadruple that contains the difference.
   * The value of the {@code long} subtrahend is preliminarily converted to a {@code Quadruple} value.
   * The Quadruple minuend remains unchanged.
   * @param minuend the value from which the subtrahend is to be subtracted
   * @param subtrahend the value to be subtracted from the minuend
   * @return a new instance of Quadruple containing the difference
   */
  public static Quadruple subtract(Quadruple minuend, long subtrahend) {
    minuend = new Quadruple(minuend);
    final Quadruple qSubtr = new Quadruple(subtrahend);
    return minuend.subtract(qSubtr);
  } // public static Quadruple subtract(Quadruple minuend, long subtrahend) {

  /**
   * Subtracts the value of the {@code double} {@code subtrahend} from the value of the {@code minuend},
   * creates and returns a new  instance of Quadruple that contains the difference.
   * The value of the {@code double} subtrahend is preliminarily converted to a {@code Quadruple} value.
   * The Quadruple minuend remains unchanged.
   * @param minuend the value from which the subtrahend is to be subtracted
   * @param subtrahend the value to be subtracted from the minuend
   * @return a new instance of Quadruple containing the difference
   */
  public static Quadruple subtract(Quadruple minuend, double subtrahend) {
    minuend = new Quadruple(minuend);
    final Quadruple qSubtr = new Quadruple(subtrahend);
    return minuend.subtract(qSubtr);
  } // public static Quadruple subtract(Quadruple minuend, double subtrahend) {

  /**
   * Multiplies the value of this Quadruple by the value of the given {@code Quadruple} factor.
   * The instance acquires a new value that equals the product of the previous value and the value of the factor.
   * @param factor the value to multiply the current value of this Quadruple by.
   * @return the reference to this object, which holds a new value that equals
   * the product of its previous value and the value of the factor
   */
  public Quadruple multiply(Quadruple factor) {
    if (isNaN() || factor.isNaN()) return assignNaN();  // NaN * whatever = NaN;

    if (isInfinite()) {
      if (factor.isZero()) return assignNaN();          // Inf * 0 = NaN
      return assignInfinity(factor.negative);           // Change sign if factor is negative:
    }                                                   // Inf * x = Inf, Inf * -x = -Inf...

    if (isZero()) {
      if (factor.isInfinite()) return assignNaN();      // 0 * Inf = NaN
      return assignZero(factor.negative);               // Change sign if factor is negative:
    }                                                   // 0 * x = (x < 0)? -0 : 0; -0 * x = (x < 0)? 0 : -0

    // This is a normal number, non-zero, non-infinity, and factor != NaN
    if (factor.isInfinite())  return assignInfinity(factor.negative);
    if (factor.isZero() )     return assignZero(factor.negative);

    // Both are regular numbers
    multUnsigned(factor);
    negative ^= factor.negative;                        // Change sign if factor is negative
    return this;
  } // public Quadruple multiply(Quadruple factor) {

  /**
   * Multiplies the value of this Quadruple by the value of the given {@code long} factor.
   * The value of the {@code long} factor is preliminarily converted to a {@code Quadruple} value.
   * The instance acquires a new value that equals the product of the previous value and the value of the factor.
   * @param factor the value to multiply the current value of this Quadruple by.
   * @return the reference to this object, which holds a new value that equals
   * the product of its previous value and the value of the factor
   */
  public Quadruple multiply(long factor) {
    return multiply(new Quadruple(factor));
  } // public Quadruple multiply(long factor) {

  /**
   * Multiplies the value of this Quadruple by the value of the given {@code double} factor.
   * The value of the {@code double} factor is preliminarily converted to a {@code Quadruple} value.
   * The instance acquires a new value that equals the product of the previous value and the value of the factor.
   * @param factor the value to multiply the current value of this Quadruple by.
   * @return the reference to this object, which holds a new value that equals
   * the product of its previous value and the value of the factor
   */
  public Quadruple multiply(double factor) {
    return multiply(new Quadruple(factor));
  } // public Quadruple multiply(double factor) {

  /**
   * Multiplies the value of the given {@code Quadruple factor1} by the {@code Quadruple factor2},
   * creates and returns a new instance of Quadruple containing the product.
   * The operands remain unchanged.
   * @param factor1 the 1st factor to be multiplied by the second one
   * @param factor2 the 2nd factor to be multiplied by the first one
   * @return a new instance of Quadruple containing the value of the product
   */
  public static Quadruple multiply(Quadruple factor1, Quadruple factor2) {
    factor1 = new Quadruple(factor1);
    return factor1.multiply(factor2);
  } // public static Quadruple multiply(Quadruple factor1, Quadruple factor2) {

  /**
   * Multiplies the value of the given {@code Quadruple factor1} by the {@code long factor2},
   * creates and returns a new instance of Quadruple containing the product.
   * The value of the {@code long} factor is preliminarily converted to a {@code Quadruple} value.
   * The operands remain unchanged.
   * @param factor1 the 1st factor to be multiplied by the second one
   * @param factor2 the 2nd factor to be multiplied by the first one
   * @return a new instance of Quadruple containing the value of the product
   */
  public static Quadruple multiply(Quadruple factor1, long factor2) {
    factor1 = new Quadruple(factor1);
    return factor1.multiply(factor2);
  } // public static Quadruple multiply(Quadruple factor1, long factor2) {

  /**
   * Multiplies the value of the given {@code Quadruple factor1} by the {@code double factor2},
   * creates and returns a new instance of Quadruple containing the product.
   * The value of the {@code double} factor is preliminarily converted to a {@code Quadruple} value.
   * The operands remain unchanged.
   * @param factor1 the 1st factor to be multiplied by the second one
   * @param factor2 the 2nd factor to be multiplied by the first one
   * @return a new instance of Quadruple containing the value of the product
   */
  public static Quadruple multiply(Quadruple factor1, double factor2) {
    factor1 = new Quadruple(factor1);
    return factor1.multiply(factor2);
  } // public static Quadruple multiply(Quadruple factor1, double factor2) {

  /**
   * Divides the value of this Quadruple by the value of the given {@code Quadruple} divisor.
   * The instance acquires a new value that equals the quotient.
   * @param divisor the divisor to divide the current value of this Quadruple by
   * @return the reference to this object, which holds a new value that equals
   * the quotient of the previous value of this Quadruple divided by the given divisor
   */
  public Quadruple divide(Quadruple divisor) {
    if (isNaN() || divisor.isNaN()) return assignNaN(); // NaN / whatever = NaN;

    if (isInfinite()) {
      if (divisor.isInfinite()) return assignNaN();     // Inf / Inf = NaN
      return assignInfinity(divisor.negative);          // Inf / x = Inf, Inf / -x = -Inf...
    }

    if (isZero()) {
      if (divisor.isZero()) return assignNaN();         // 0 / 0 = NaN
      return assignZero(divisor.negative);              // 0 / x = 0, 0 / -x = -0, etc.
    }

    // This is normal number, not a zero, not an infinity, and divisor != NaN
    if (divisor.isInfinite())
      return assignZero(divisor.negative);              // x / Inf = 0, x / -Inf = -0

    if (divisor.isZero() )
      return assignInfinity(divisor.negative);          // x / 0 = Inf, x / -0 = -Inf

    // Both are regular numbers, do divide
    divideUnsigned(divisor);                            // ignores signs

    negative ^= divisor.negative;                       // x  / -y = -(x / y)
    return this;
  } // public Quadruple divide(Quadruple divisor) {

  /**
   * Divides the value of this Quadruple by the value of the given {@code long} divisor.
   * The instance acquires a new value that equals the quotient.
   * The value of the {@code long} divisor is preliminarily converted to a {@code Quadruple} value.
   * @param divisor the divisor to divide the current value of this Quadruple by
   * @return the reference to this object, which holds a new value that equals
   * the quotient of the previous value of this Quadruple divided by the given divisor
   */
  public Quadruple divide(long divisor) {
    return divide(new Quadruple(divisor));
  } // public Quadruple divide(long divisor) {

  /**
   * Divides the value of this Quadruple by the value of the given {@code double} divisor.
   * The instance acquires a new value that equals the quotient.
   * The value of the {@code double} divisor is preliminarily converted to a {@code Quadruple} value.
   * @param divisor the divisor to divide the current value of this Quadruple by
   * @return the reference to this object, which holds a new value that equals
   * the quotient of the previous value of this Quadruple divided by the given divisor
   */
  public Quadruple divide(double divisor) {
    return divide(new Quadruple(divisor));
  } // public Quadruple divide(double divisor) {

  /**
   * Divides the value of the given dividend by the value of the given {@code Quadruple} divisor,
   * creates and returns a new instance of Quadruple containing the quotient.
   * The operands remain unchanged.
   * @param dividend the value to be divided by the divisor
   * @param divisor the divisor to divide the dividend by
   * @return a new instance of Quadruple, which holds the value of the quotient
   */
  public static Quadruple divide(Quadruple dividend, Quadruple divisor) {
    dividend  = new Quadruple(dividend);
    return dividend.divide(divisor);
  } // public static Quadruple divide(Quadruple dividend, Quadruple divisor) {

  /**
   * Divides the value of the given dividend by the value of the given {@code long} divisor,
   * creates and returns a new instance of Quadruple containing the quotient.
   * The value of the {@code long} divisor is preliminarily converted to a {@code Quadruple} value.
   * The operands remain unchanged.
   * @param dividend the value to be divided by the divisor
   * @param divisor the divisor to divide the dividend by
   * @return a new instance of Quadruple, which holds the value of the quotient
   */
  public static Quadruple divide(Quadruple dividend, long divisor) {
    dividend  = new Quadruple(dividend);
    return dividend.divide(divisor);
  } // public static Quadruple divide(Quadruple dividend, long divisor) {

  /**
   * Divides the value of the given dividend by the value of the given {@code double} divisor,
   * creates and returns a new instance of Quadruple containing the quotient.
   * The value of the {@code double} divisor is preliminarily converted to a {@code Quadruple} value.
   * The operands remain unchanged.
   * @param dividend the value to be divided by the divisor
   * @param divisor the divisor to divide the dividend by
   * @return a new instance of Quadruple, which holds the value of the quotient
   */
  public static Quadruple divide(Quadruple dividend, double divisor) {
    dividend  = new Quadruple(dividend);
    return dividend.divide(divisor);
  } // public static Quadruple divide(Quadruple dividend, double divisor) {

  /* ***********************************************************************************
   ****** Square root ******************************************************************
   *********************************************************************************** */

  /**
   * Computes a square root of the value of this {@code Quadruple}
   * and replaces the old value of this instance with the newly-computed value.
   * @return the reference to this instance, which holds a new value that equals
   * to the square root of its previous value
   */
  public Quadruple sqrt() {
    if (negative) return assignNaN();
    if (isNaN() || isInfinite()) return this;

    long absExp = (exponent & LOWER_32_BITS) - EXPONENT_BIAS; // unbiased exponent
    if (exponent == 0)                                  // subnormal
      absExp  -= normalizeMantissa();                   // It returns 0 for MIN_NORMAL / 2, 1 for MIN_NORMAL / 4... no additional correction is needed
    exponent = (int)(absExp / 2 + EXPONENT_BIAS);       // the exponent of the root

    long thirdWord = sqrtMant();                        // puts 128 bit of the root into mantHi, mantLo
                                                        // and returns additional 64 bits of the root

    if (absExp % 2 != 0) {                              // Exponent is odd,
      final long[] multed = multBySqrt2(mantHi, mantLo, thirdWord); // multiply this value by sqrt(2), fill mantissa with the new value
      mantHi = multed[0]; mantLo = multed[1]; thirdWord = multed[2];
      if (absExp < 0) exponent--;                       // for negative odd powers of two, exp = floor(exp / 2), e.g sqrt(0.64) = 0.8, sqrt(0.36) = 0.6
    }

    if ((thirdWord & HIGH_BIT) != 0)                    // The rest of the root >= a half of the lowest bit, round up
      if (++mantLo == 0)
        if (++mantHi == 0)
          exponent++;        // 21.01.08 18:04:02: Actually, this branch can never be executed,
                             // since derivative of sqrt(x) at point x = 4 equals 1/4
                             // and is less than 1/4 if x < 4, so
                             // sqrt(-1, -1, EXP_0Q + 1) is a little more than (-1, -1, EXP_0Q),
                             // but less than (-1, -1, EXP_0Q) + 0.5 LSB, so gets rounded down to (-1, -1, EXP_0Q)
                             // (0xFFFF_FFFF_FFFF_FFFFL, 0xFFFF_FFFF_FFFF_FFFFL, and no carry to the position of the implicit unity).
                             // Nevertheless let it remain as a safety net


    return this;
  } // public Quadruple sqrt() {

  /**
   * Computes a square root of the value of the given {@code Quadruple},
   * creates and returns a new instance of Quadruple containing the value of the square root.
   * The parameter remains unchanged.
   * @param square the value to find the square root of
   * @return a new instance of Quadruple containing the value of the square root of the given argument
   */
  public static Quadruple sqrt(Quadruple square) {
    return new Quadruple(square).sqrt();
  } // public static Quadruple sqrt(Quadruple square) {

  /* ***********************************************************************************
   ****** Miscellaneous utility methods ************************************************
   *********************************************************************************** */

  protected void ________Miscellaneous_utility_methods_________ () {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Changes the sign of this Quadruple.
   * @return the reference to this object, which holds a new value that
   * equals the previous value in magnitude, but with opposite sign
   */
  public Quadruple negate() {
    negative = !negative;
    return this;
  } // public Quadruple negate() {

  /**
   * Returns 1 for positive values, -1 for negative values (including -0), and 0 for the positive zero value
   * @return 1 for positive values, -1 for negative values (including -0), and 0 for the positive zero value
   */
  public int signum() {
    return  negative? -1 :
            isZero()?  0 :
                       1;
  } // public int signum() {

  /**
   * Creates a new Quadruple instance with a pseudo-random value
   * using a static randomly initialized {@code java.util.Random} instance.
   * The generated value falls within the range from
   * -{@linkplain Quadruple#MAX_VALUE} to {@linkplain Quadruple#MAX_VALUE} inclusive.
   * @return a new instance containing a next random value
   */
  public static Quadruple nextRandom() {
    return nextRandom(rand);
  } // public static Quadruple nextRandom() {

  /**
   * Creates a new Quadruple instance with a pseudo-random value
   * using the given {@code java.util.Random} instance.}<br>
   * The generated value falls within the range from
   * -{@linkplain Quadruple#MAX_VALUE} to {@linkplain Quadruple#MAX_VALUE} inclusive.
   * Can be used to repeatedly generate the same pseudo-random sequence.
   * @param rand an instance of {@code java.util.Random} to be used for generating the random value
   * @return a new instance containing a next random value
   */
  public static Quadruple nextRandom(Random rand) {
    // FIXIT 21.04.24 9:55:14 Non-uniform distribution of returned values!
    final boolean sign = rand.nextBoolean();
    final int exp = rand.nextInt();
    final long mantHi = rand.nextLong();
    final long mantLo = rand.nextLong();
    return new Quadruple(sign, exp, mantHi, mantLo);
  } // public static Quadruple nextRandom(Random rand) {

  /**
   * Creates a new Quadruple instance with a pseudo-random value
   * using a static randomly initialized {@code java.util.Random} instance.
   * The generated value falls within the range 0.0 (inclusive) to 1.0 (exclusive).
   * @return a new instance containing a next random value
   */
  public static Quadruple nextNormalRandom() {
    return nextNormalRandom(rand);
  } // public static Quadruple nextNormalRandom() {

  /**
   * Creates a new Quadruple instance with a pseudo-random value
   * using the given {@code java.util.Random} instance.
   * The generated value falls within the range 0.0 (inclusive) to 1.0 (exclusive).
   * Can be used to repeatedly generate the same pseudo-random sequence.
   * @param rand an instance of {@code java.util.Random} to be used for generating the random value
   * @return a new instance containing a next random value
   */
  public static Quadruple nextNormalRandom(Random rand) {
    final long mantHi = rand.nextLong();
    final long mantLo = rand.nextLong();
    return new Quadruple(false, EXPONENT_OF_ONE, mantHi, mantLo).subtract(ONE);
  } // public static Quadruple nextNormalRandom(Random rand) {

  /* ***********************************************************************************
   ****** Private fields ***************************************************************
   *********************************************************************************** */

  protected void ____Private_fields_____() {} // Just to put a visible mark of the section in the outline view of the IDE

  // The fields containing the value of the instance
  private boolean negative;
  private int exponent;
  private long mantHi;
  private long mantLo;

  private static final char[] ZEROS = "0000000000000000000000000000000000000000".toCharArray(); // 40 zeros

  /** Just for convenience: 0x8000_0000_0000_0000L; (== Long.MIN_VALUE) */
  private static final long HIGH_BIT          = 0x8000_0000_0000_0000L;
  /** Just for convenience: 0x8000_0000_0000_0000L; */
  private static final long BIT_63            = HIGH_BIT;

  /** Just for convenience: 0x0000_0000_0000_7FFFL */
  private static final long LOWER_15_BITS     = 0x0000_0000_0000_7FFFL;
  /** Just for convenience: 0x0000_0000_FFFF_FFFFL */
  private static final long LOWER_32_BITS     = 0x0000_0000_FFFF_FFFFL;
  /** Just for convenience: 0x0000_FFFF_FFFF_FFFFL */
  private static final long LOWER_48_BITS     = 0x0000_FFFF_FFFF_FFFFL;
  /** Just for convenience: 0xFFFF_FFFF_0000_0000L; */
  private static final long HIGHER_32_BITS    = 0xFFFF_FFFF_0000_0000L;
  /** Just for convenience: 0x8000_0000L; // 2^31 */
  private static final long POW_2_31_L        = 0x8000_0000L; // 2^31

  /** Inner structure of double: where it holds its sign */
  private static final long DOUBLE_SIGN_MASK  = HIGH_BIT;
  /** Inner structure of double: where it holds its exponent */
  private static final long DOUBLE_EXP_MASK   = 0x7ff0_0000_0000_0000L;
  /** Inner structure of double: where it holds its mantissa */
  private static final long DOUBLE_MANT_MASK  = 0x000f_ffff_ffff_ffffL;

  /** double's exponent value corresponding to 2^0 = 1, shifted to lower bits */
  private static final int EXP_0D             = 0x0000_03FF;

  /** The highest bit of Quad's mantissa that doesn't fit in double's mantissa (is lower than the lowest) */
  private static final long HALF_DOUBLES_LSB              = 0x0000_0000_0000_0800L;
  /** The implied position of the implied unity in double */
  private static final long DOUBLE_IMPLIED_MSB            = 0x0010_0000_0000_0000L;

  /** Max value of the decimal exponent, corresponds to EXPONENT_OF_MAX_VALUE */
  private static final int  MAX_EXP10                     = 646456993;
  /** Min value of the decimal exponent, corresponds to EXPONENT_OF_MIN_NORMAL */
  private static final int  MIN_EXP10                     = -646457032;      // corresponds

  private static final int  IEEE754_EXPONENT_BIAS         = 16383; // 0x3FFF;
  private static final int  IEEE754_MAX_EXPONENT          = IEEE754_EXPONENT_BIAS;
  private static final int  IEEE754_MIN_NORMAL_EXPONENT   = -16382; // 0xFFFF_C002
  private static final int  IEEE754_MIN_EXPONENT          = IEEE754_MIN_NORMAL_EXPONENT - 112; //

  private static final long IEEE754_MINUS_ZERO_LEAD       = 0x8000_0000_0000_0000L;
  private static final long IEEE754_NAN_LEAD              = 0x7FFF_8000_0000_0000L;
  private static final long IEEE754_MINUS_INFINITY_LEAD   = 0xFFFF_0000_0000_0000L;
  private static final long IEEE754_INFINITY_LEAD         = 0x7FFF_0000_0000_0000L;
  private static final long IEEE754_EXPONENT_MASK         = 0x7FFF_0000_0000_0000L;

  /** Approximate value of log<sub>2</sub>(10) */
  private static final double LOG2_10                     = Math.log(10) / Math.log(2);
  /** Approximate value of log<sub>2</sub>(e) */
  private static final double LOG2_E                      = 1/Math.log(2.0);

  /* */
  private static final MathContext MC_120_HALF_EVEN       = new MathContext(120, RoundingMode.HALF_EVEN);
  /** = new MathContext(100, RoundingMode.HALF_EVEN) */
  private static final MathContext MC_100_HALF_EVEN       = new MathContext(100, RoundingMode.HALF_EVEN);

  private static final MathContext MC_40_HALF_EVEN        = new MathContext(40, RoundingMode.HALF_EVEN);

  /* */
  private static final MathContext MC_20_HALF_EVEN        = new MathContext(20, RoundingMode.HALF_EVEN);

  /** BigDecimal value of 0.5 */
  private static final BigDecimal HALF_OF_ONE             = new BigDecimal("0.5");

  /** BigDecimal value of 2 */
  private static final BigDecimal BD_TWO                  = new BigDecimal("2");

  /** Exact BigDecimal value of 2^63 */
  private static final BigDecimal TWO_RAISED_TO_63        = new BigDecimal( "9223372036854775808"); // 2^63

  /** Exact BigDecimal value of 2^64 */
  private static final BigDecimal TWO_RAISED_TO_64        = new BigDecimal("18446744073709551616"); // 2^64

  /** 2^100_000_000 */
  private static final BigDecimal TWO_RAISED_TO_1E8 = // 2^100_000_000 == BD_TWO.pow(100_000_000, new MathContext(80, RoundingMode.HALF_EVEN)) ==
  //  new BigDecimal("3.6846659369804587632090923909842219150699658122675497084939429616965837768179883E+30102999");
      new BigDecimal("3.6846659369804587632090923909842219150699658122675497084939429616965837768179882661472291330278812763136896778414689207236912875516200052340E+30102999");

  /** Minimum possible positive value, 6.672829482607474308148353774991346115977e-646457032 */
  private static final Quadruple MIN_VALUE    = new Quadruple().assignMinValue();
  /** Maximum possible value, 1.761613051683963353207493149791840285665e+646456993 */
  private static final Quadruple MAX_VALUE    = new Quadruple().assignMaxValue();
  /** Minimum possible positive normal value, 2.270646210401492537526567265179587581247e-646456993 */
  private static final Quadruple MIN_NORMAL   = new Quadruple().assignMinNormal();
  /** Not a number */
  private static final Quadruple NaN          = new Quadruple().assignNaN();
  /** Quadruple with value of 1.0 */
  private static final Quadruple ONE          = new Quadruple().assign(1);

  private static final Quadruple NEGATIVE_INFINITY         = new Quadruple().assignNegativeInfinity();
  private static final Quadruple POSITIVE_INFINITY         = new Quadruple().assignPositiveInfinity();

  private static final Random rand = new Random();

  /**
   * An array of positive powers of two, each value consists of 4 longs: decimal exponent and 3 x 64 bits of mantissa, divided by ten
   * Used to find an arbitrary power of 2 (by powerOfTwo(long exp) )
   */
  private static final long[][] POS_POWERS_OF_2 = {
  // v020
    // 0: 2^0 =   1 = 0.1e1
    {1, 0x1999_9999_9999_9999L, 0x9999_9999_9999_9999L, 0x9999_9999_9999_999aL},
    // 1: 2^(2^0) =   2^1 =   2 = 0.2e1
//    {1, 0x3333_3333_3333_3333L, 0x3333_3333_3333_3333L, 0x3333_3333_3333_3333L},
    {1, 0x3333_3333_3333_3333L, 0x3333_3333_3333_3333L, 0x3333_3333_3333_3334L}, // ***
    // 2: 2^(2^1) =   2^2 =   4 = 0.4e1
//    {1, 0x6666_6666_6666_6666L, 0x6666_6666_6666_6666L, 0x6666_6666_6666_6666L},
    {1, 0x6666_6666_6666_6666L, 0x6666_6666_6666_6666L, 0x6666_6666_6666_6667L}, // ***
    // 3: 2^(2^2) =   2^4 =   16 = 0.16e2
//    {2, 0x28f5_c28f_5c28_f5c2L, 0x8f5c_28f5_c28f_5c28L, 0xf5c2_8f5c_28f5_c28fL},
    {2, 0x28f5_c28f_5c28_f5c2L, 0x8f5c_28f5_c28f_5c28L, 0xf5c2_8f5c_28f5_c290L}, // ***
    // 4: 2^(2^3) =   2^8 =   256 = 0.256e3
//    {3, 0x4189_374b_c6a7_ef9dL, 0xb22d_0e56_0418_9374L, 0xbc6a_7ef9_db22_d0e5L},
    {3, 0x4189_374b_c6a7_ef9dL, 0xb22d_0e56_0418_9374L, 0xbc6a_7ef9_db22_d0e6L}, // ***
    // 5: 2^(2^4) =   2^16 =   65536 = 0.65536e5
    {5, 0xa7c5_ac47_1b47_8423L, 0x0fcf_80dc_3372_1d53L, 0xcddd_6e04_c059_2104L},
    // 6: 2^(2^5) =   2^32 =   4294967296 = 0.4294967296e10
    {10, 0x6df3_7f67_5ef6_eadfL, 0x5ab9_a207_2d44_268dL, 0x97df_837e_6748_956eL},
    // 7: 2^(2^6) =   2^64 =   18446744073709551616 = 0.18446744073709551616e20
    {20, 0x2f39_4219_2484_46baL, 0xa23d_2ec7_29af_3d61L, 0x0607_aa01_67dd_94cbL},
    // 8: 2^(2^7) =   2^128 =   340282366920938463463374607431768211456 = 0.340282366920938463463374607431768211456e39
    {39, 0x571c_bec5_54b6_0dbbL, 0xd5f6_4baf_0506_840dL, 0x451d_b70d_5904_029bL},
    // 9: 2^(2^8) =   2^256 =   1.1579208923731619542357098500868790785326998466564056403945758401E+77 = 0.11579208923731619542357098500868790785326998466564056403945758401e78
//    {78, 0x1da4_8ce4_68e7_c702L, 0x6520_247d_3556_476dL, 0x1469_caf6_db22_4cf9L},
    {78, 0x1da4_8ce4_68e7_c702L, 0x6520_247d_3556_476dL, 0x1469_caf6_db22_4cfaL}, // ***
    // 10: 2^(2^9) =   2^512 =   1.3407807929942597099574024998205846127479365820592393377723561444E+154 = 0.13407807929942597099574024998205846127479365820592393377723561444e155
    {155, 0x2252_f0e5_b397_69dcL, 0x9ae2_eea3_0ca3_ade0L, 0xeeaa_3c08_dfe8_4e30L},
    // 11: 2^(2^10) =   2^1024 =   1.7976931348623159077293051907890247336179769789423065727343008116E+308 = 0.17976931348623159077293051907890247336179769789423065727343008116e309
    {309, 0x2e05_5c9a_3f6b_a793L, 0x1658_3a81_6eb6_0a59L, 0x22c4_b082_6cf1_ebf7L},
    // 12: 2^(2^11) =   2^2048 =   3.2317006071311007300714876688669951960444102669715484032130345428E+616 = 0.32317006071311007300714876688669951960444102669715484032130345428e617
//    {617, 0x52bb_45e9_cf23_f17fL, 0x7688_c076_06e5_0364L, 0xb344_79aa_9d44_9a58L},
    {617, 0x52bb_45e9_cf23_f17fL, 0x7688_c076_06e5_0364L, 0xb344_79aa_9d44_9a57L}, // *** v007
    // 13: 2^(2^12) =   2^4096 =   1.0443888814131525066917527107166243825799642490473837803842334833E+1233 = 0.10443888814131525066917527107166243825799642490473837803842334833e1234
//    {1234, 0x1abc_81c8_ff5f_846cL, 0x8f5e_3c98_53e3_8c97L, 0x4506_0097_f3bf_9295L},
    {1234, 0x1abc_81c8_ff5f_846cL, 0x8f5e_3c98_53e3_8c97L, 0x4506_0097_f3bf_9296L}, // *** v008
    // 14: 2^(2^13) =   2^8192 =   1.0907481356194159294629842447337828624482641619962326924318327862E+2466 = 0.10907481356194159294629842447337828624482641619962326924318327862e2467
    {2467, 0x1bec_53b5_10da_a7b4L, 0x4836_9ed7_7dbb_0eb1L, 0x3b05_587b_2187_b41eL},
    // 15: 2^(2^14) =   2^16384 =   1.1897314953572317650857593266280071307634446870965102374726748212E+4932 = 0.11897314953572317650857593266280071307634446870965102374726748212e4933
//    {4933, 0x1e75_063a_5ba9_1326L, 0x8abf_b8e4_6001_6ae3L, 0x2800_8702_d29e_8a3bL},
    {4933, 0x1e75_063a_5ba9_1326L, 0x8abf_b8e4_6001_6ae3L, 0x2800_8702_d29e_8a3cL}, // *** v009
    // 16: 2^(2^15) =   2^32768 =   1.4154610310449547890015530277449516013481307114723881672343857483E+9864 = 0.14154610310449547890015530277449516013481307114723881672343857483e9865
//    {9865, 0x243c_5d8b_b5c5_fa55L, 0x40c6_d248_c588_1915L, 0x4c0f_d99f_d5be_fc21L},
    {9865, 0x243c_5d8b_b5c5_fa55L, 0x40c6_d248_c588_1915L, 0x4c0f_d99f_d5be_fc22L}, // *** v010
    // 17: 2^(2^16) =   2^65536 =   2.0035299304068464649790723515602557504478254755697514192650169737E+19728 = 0.20035299304068464649790723515602557504478254755697514192650169737e19729
    {19729, 0x334a_5570_c3f4_ef3cL, 0xa13c_36c4_3f97_9c90L, 0xda7a_c473_555f_b7a8L},
    // 18: 2^(2^17) =   2^131072 =   4.0141321820360630391660606060388767343771510270414189955825538065E+39456 = 0.40141321820360630391660606060388767343771510270414189955825538065e39457
    {39457, 0x66c3_0444_5dd9_8f3bL, 0xa8c2_93a2_0e47_a41bL, 0x4c5b_03dc_1260_4964L},
    // 19: 2^(2^18) =   2^262144 =   1.6113257174857604736195721184520050106440238745496695174763712505E+78913 = 0.16113257174857604736195721184520050106440238745496695174763712505e78914
    {78914, 0x293f_fbf5_fb02_8cc4L, 0x89d3_e5ff_4423_8406L, 0x369a_339e_1bfe_8c9bL},
    // 20: 2^(2^19) =   2^524288 =   2.5963705678310007761265964957268828277447343763484560463573654868E+157826 = 0.25963705678310007761265964957268828277447343763484560463573654868e157827
    {157827, 0x4277_92fb_b68e_5d20L, 0x7b29_7cd9_fc15_4b62L, 0xf091_4211_4aa9_a20cL},
    // 21: 2^(2^20) =   2^1048576 =   6.7411401254990734022690651047042454376201859485326882846944915676E+315652 = 0.67411401254990734022690651047042454376201859485326882846944915676e315653
    {315653, 0xac92_bc65_ad5c_08fcL, 0x00be_eb11_5a56_6c19L, 0x4ba8_82d8_a462_2437L},
    // 22: 2^(2^21) =   2^2097152 =   4.5442970191613663099961595907970650433180103994591456270882095573E+631305 = 0.45442970191613663099961595907970650433180103994591456270882095573e631306
//    {631306, 0x7455_8144_0f92_e80eL, 0x4da8_22cf_7f89_6f41L, 0x509d_5986_7816_4eccL},
    {631306, 0x7455_8144_0f92_e80eL, 0x4da8_22cf_7f89_6f41L, 0x509d_5986_7816_4ecdL}, // *** v012
    // 23: 2^(2^22) =   2^4194304 =   2.0650635398358879243991194945816501695274360493029670347841664177E+1262611 = 0.20650635398358879243991194945816501695274360493029670347841664177e1262612
    {1262612, 0x34dd_99b4_c695_23a5L, 0x64bc_2e8f_0d8b_1044L, 0xb03b_1c96_da5d_d349L},
    // 24: 2^(2^23) =   2^8388608 =   4.2644874235595278724327289260856157547554200794957122157246170406E+2525222 = 0.42644874235595278724327289260856157547554200794957122157246170406e2525223
//    {2525223, 0x6d2b_bea9_d6d2_5a08L, 0xa0a4_606a_88e9_6b70L, 0x1820_63bb_c2fe_851fL},
    {2525223, 0x6d2b_bea9_d6d2_5a08L, 0xa0a4_606a_88e9_6b70L, 0x1820_63bb_c2fe_8520L}, // *** v015
    // 25: 2^(2^24) =   2^16777216 =   1.8185852985697380078927713277749906189248596809789408311078112486E+5050445 = 0.18185852985697380078927713277749906189248596809789408311078112486e5050446
    {5050446, 0x2e8e_47d6_3bfd_d6e3L, 0x2b55_fa89_76ea_a3e9L, 0x1a6b_9d30_8641_2a73L},
    // 26: 2^(2^25) =   2^33554432 =   3.3072524881739831340558051919726975471129152081195558970611353362E+10100890 = 0.33072524881739831340558051919726975471129152081195558970611353362e10100891
//    {10100891, 0x54aa_68ef_a1d7_19dfL, 0xd850_5806_612c_5c8fL, 0xad06_8837_fee8_b43cL},
//    {10100891, 0x54aa_68ef_a1d7_19dfL, 0xd850_5806_612c_5c8fL, 0xad06_8837_fee8_b43bL}, // *** v013
    {10100891, 0x54aa_68ef_a1d7_19dfL, 0xd850_5806_612c_5c8fL, 0xad06_8837_fee8_b43aL}, // *** v016
    // 27: 2^(2^26) =   2^67108864 =   1.0937919020533002449982468634925923461910249420785622990340704603E+20201781 = 0.10937919020533002449982468634925923461910249420785622990340704603e20201782
    {20201782, 0x1c00_464c_cb7b_ae77L, 0x9e38_7778_4c77_982cL, 0xd94a_f3b6_1717_404fL},
    // 28: 2^(2^27) =   2^134217728 =   1.1963807249973763567102377630870670302911237824129274789063323723E+40403562 = 0.11963807249973763567102377630870670302911237824129274789063323723e40403563
//    {40403563, 0x1ea0_99c8_be2b_6cd0L, 0x8bfb_6d53_9fa5_0466L, 0x6d3b_c37e_69a8_4217L},
    {40403563, 0x1ea0_99c8_be2b_6cd0L, 0x8bfb_6d53_9fa5_0466L, 0x6d3b_c37e_69a8_4218L}, // *** v017
    // 29: 2^(2^28) =   2^268435456 =   1.4313268391452478724777126233530788980596273340675193575004129517E+80807124 = 0.14313268391452478724777126233530788980596273340675193575004129517e80807125
//    {80807125, 0x24a4_57f4_66ce_8d18L, 0xf2c8_f3b8_1bc6_bb59L, 0xa78c_7576_92e0_2d47L},
//    {80807125, 0x24a4_57f4_66ce_8d18L, 0xf2c8_f3b8_1bc6_bb59L, 0xa78c_7576_92e0_2d48L}, // *** v014
    {80807125, 0x24a4_57f4_66ce_8d18L, 0xf2c8_f3b8_1bc6_bb59L, 0xa78c_7576_92e0_2d49L}, // *** v018
    // 30: 2^(2^29) =   2^536870912 =   2.0486965204575262773910959587280218683219330308711312100181276813E+161614248 = 0.20486965204575262773910959587280218683219330308711312100181276813e161614249
    {161614249, 0x3472_5667_7aba_6b53L, 0x3fbf_90d3_0611_a67cL, 0x1e03_9d87_e0bd_b32bL},
//    {161614249, 0x3472_5667_7aba_6b53L, 0x3fbf_90d3_0611_a67cL, 0x1e03_9d87_e0bd_b32cL}, // *** v019 --- Bad
    // 31: 2^(2^30) =   2^1073741824 =   4.1971574329347753848087162337676781412761959309467052555732924370E+323228496 = 0.41971574329347753848087162337676781412761959309467052555732924370e323228497
    {323228497, 0x6b72_7daf_0fd3_432aL, 0x71f7_1121_f9e4_200fL, 0x8fcd_9942_d486_c10cL},
    // 32: 2^(2^31) =   2^2147483648 =   1.7616130516839633532074931497918402856671115581881347960233679023E+646456993 = 0.17616130516839633532074931497918402856671115581881347960233679023e646456994
//    {646456994, 0x2d18_e844_84d9_1f78L, 0x4079_bfe7_829d_ec6fL, 0x2155_1643_e365_abc5L},
    {646456994, 0x2d18_e844_84d9_1f78L, 0x4079_bfe7_829d_ec6fL, 0x2155_1643_e365_abc6L}, // *** v020
  }; // private static final long[][] POS_POWERS_OF_2 = {

  /**
   * An array of negative powers of two, each value consists of 4 longs: decimal exponent and 3 x 64 bits of mantissa, divided by ten.
   * Used to find an arbitrary power of 2 (by powerOfTwo(long exp) )
   */
  private static final long[][] NEG_POWERS_OF_2 = {
    // v18
    // 0: 2^0 =   1 = 0.1e1
    {1, 0x1999_9999_9999_9999L, 0x9999_9999_9999_9999L, 0x9999_9999_9999_999aL},
    // 1: 2^-(2^0) =   2^-1 =   0.5 = 0.5e0
    {0, 0x8000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    // 2: 2^-(2^1) =   2^-2 =   0.25 = 0.25e0
//      {0, 0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    {0, 0x4000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L}, // ***
    // 3: 2^-(2^2) =   2^-4 =   0.0625 = 0.625e-1
    {-1, 0xa000_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    // 4: 2^-(2^3) =   2^-8 =   0.00390625 = 0.390625e-2
    {-2, 0x6400_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    // 5: 2^-(2^4) =   2^-16 =   0.0000152587890625 = 0.152587890625e-4
//      {-4, 0x2710_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    {-4, 0x2710_0000_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L}, // ***
    // 6: 2^-(2^5) =   2^-32 =   2.3283064365386962890625E-10 = 0.23283064365386962890625e-9
//    {-9, 0x3b9a_ca00_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    {-9, 0x3b9a_ca00_0000_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0001L}, // ***
    // 7: 2^-(2^6) =   2^-64 =   5.42101086242752217003726400434970855712890625E-20 = 0.542101086242752217003726400434970855712890625e-19
    {-19, 0x8ac7_2304_89e8_0000L, 0x0000_0000_0000_0000L, 0x0000_0000_0000_0000L},
    // 8: 2^-(2^7) =   2^-128 =   2.9387358770557187699218413430556141945466638919302188037718792657E-39 = 0.29387358770557187699218413430556141945466638919302188037718792657e-38
//      {-38, 0x4b3b_4ca8_5a86_c47aL, 0x098a_2240_0000_0000L, 0x0000_0000_0000_0000L},
    {-38, 0x4b3b_4ca8_5a86_c47aL, 0x098a_2240_0000_0000L, 0x0000_0000_0000_0001L}, // ***
    // 9: 2^-(2^8) =   2^-256 =   8.6361685550944446253863518628003995711160003644362813850237034700E-78 = 0.86361685550944446253863518628003995711160003644362813850237034700e-77
//      {-77, 0xdd15_fe86_affa_d912L, 0x49ef_0eb7_13f3_9ebeL, 0xaa98_7b6e_6fd2_a000L}, // ***
    {-77, 0xdd15_fe86_affa_d912L, 0x49ef_0eb7_13f3_9ebeL, 0xaa98_7b6e_6fd2_a002L},
    // 10: 2^-(2^9) =   2^-512 =   7.4583407312002067432909653154629338373764715346004068942715183331E-155 = 0.74583407312002067432909653154629338373764715346004068942715183331e-154
    {-154, 0xbeee_fb58_4aff_8603L, 0xaafb_550f_facf_d8faL, 0x5ca4_7e4f_88d4_5371L},
    // 11: 2^-(2^10) =   2^-1024 =   5.5626846462680034577255817933310101605480399511558295763833185421E-309 = 0.55626846462680034577255817933310101605480399511558295763833185421e-308
//      {-308, 0x8e67_9c2f_5e44_ff8fL, 0x570f_09ea_a7ea_7648L, 0x5961_db50_c6d2_b887L},
    {-308, 0x8e67_9c2f_5e44_ff8fL, 0x570f_09ea_a7ea_7648L, 0x5961_db50_c6d2_b888L}, // ***
    // 12: 2^-(2^11) =   2^-2048 =   3.0943460473825782754801833699711978538925563038849690459540984582E-617 = 0.30943460473825782754801833699711978538925563038849690459540984582e-616
    {-616, 0x4f37_1b33_99fc_2ab0L, 0x8170_041c_9feb_05aaL, 0xc7c3_4344_7c75_bcf6L},
    // 13: 2^-(2^12) =   2^-4096 =   9.5749774609521853579467310122804202420597417413514981491308464986E-1234 = 0.95749774609521853579467310122804202420597417413514981491308464986e-1233
    {-1233, 0xf51e_9281_7901_3fd3L, 0xde4b_d12c_de4d_985cL, 0x4a57_3ca6_f94b_ff14L},
    // 14: 2^-(2^13) =   2^-8192 =   9.1680193377742358281070619602424158297818248567928361864131947526E-2467 = 0.91680193377742358281070619602424158297818248567928361864131947526e-2466
    {-2466, 0xeab3_8812_7bcc_aff7L, 0x1667_6391_42b9_fbaeL, 0x775e_c999_5e10_39fbL},
    // 15: 2^-(2^14) =   2^-16384 =   8.4052578577802337656566945433043815064951983621161781002720680748E-4933 = 0.84052578577802337656566945433043815064951983621161781002720680748e-4932
    {-4932, 0xd72c_b2a9_5c7e_f6ccL, 0xe81b_f1e8_25ba_7515L, 0xc2fe_b521_d6cb_5dcdL},
    // 16: 2^-(2^15) =   2^-32768 =   7.0648359655776364427774021878587184537374439102725065590941425796E-9865 = 0.70648359655776364427774021878587184537374439102725065590941425796e-9864
//      {-9864, 0xb4dc_1be6_6045_02dcL, 0xd491_079b_8eef_6535L, 0x578d_3965_d24d_e84cL},
    {-9864, 0xb4dc_1be6_6045_02dcL, 0xd491_079b_8eef_6535L, 0x578d_3965_d24d_e84dL}, // ***
    // 17: 2^-(2^16) =   2^-65536 =   4.9911907220519294656590574792132451973746770423207674161425040336E-19729 = 0.49911907220519294656590574792132451973746770423207674161425040336e-19728
//      {-19728, 0x7fc6_447b_ee60_ea43L, 0x2548_da5c_8b12_5b27L, 0x5f42_d114_2f41_d347L},
    {-19728, 0x7fc6_447b_ee60_ea43L, 0x2548_da5c_8b12_5b27L, 0x5f42_d114_2f41_d349L}, // ***
    // 18: 2^-(2^17) =   2^-131072 =   2.4911984823897261018394507280431349807329035271689521242878455599E-39457 = 0.24911984823897261018394507280431349807329035271689521242878455599e-39456
//      {-39456, 0x3fc6_5180_f88a_f8fbL, 0x6a69_15f3_8334_9413L, 0x063c_3708_b6ce_b290L},
    {-39456, 0x3fc6_5180_f88a_f8fbL, 0x6a69_15f3_8334_9413L, 0x063c_3708_b6ce_b291L}, // ***
    // 19: 2^-(2^18) =   2^-262144 =   6.2060698786608744707483205572846793091942192651991171731773832448E-78914 = 0.62060698786608744707483205572846793091942192651991171731773832448e-78913
    {-78913, 0x9ee0_197c_8dcd_55bfL, 0x2b2b_9b94_2c38_f4a2L, 0x0f8b_a634_e9c7_06aeL},
    // 20: 2^-(2^19) =   2^-524288 =   3.8515303338821801176537443725392116267291403078581314096728076497E-157827 = 0.38515303338821801176537443725392116267291403078581314096728076497e-157826
//      {-157826, 0x6299_63a2_5b8b_2d79L, 0xd00b_9d22_86f7_0876L, 0xe970_0470_0c36_44fbL},
    {-157826, 0x6299_63a2_5b8b_2d79L, 0xd00b_9d22_86f7_0876L, 0xe970_0470_0c36_44fcL}, // ***
    // 21: 2^-(2^20) =   2^-1048576 =   1.4834285912814577854404052243709225888043963245995136935174170977E-315653 = 0.14834285912814577854404052243709225888043963245995136935174170977e-315652
    {-315652, 0x25f9_cc30_8cee_f4f3L, 0x40f1_9543_911a_4546L, 0xa2cd_3894_52cf_c366L},
    // 22: 2^-(2^21) =   2^-2097152 =   2.2005603854312903332428997579002102976620485709683755186430397089E-631306 = 0.22005603854312903332428997579002102976620485709683755186430397089e-631305
    {-631305, 0x3855_97b0_d47e_76b8L, 0x1b9f_67e1_03bf_2329L, 0xc311_9848_5959_85f7L},
    // 23: 2^-(2^22) =   2^-4194304 =   4.8424660099295090687215589310713586524081268589231053824420510106E-1262612 = 0.48424660099295090687215589310713586524081268589231053824420510106e-1262611
//      {-1262611, 0x7bf7_95d2_76c1_2f66L, 0x66a6_1d62_a446_659aL, 0xa1a4_d73b_ebf0_93d4L},
    {-1262611, 0x7bf7_95d2_76c1_2f66L, 0x66a6_1d62_a446_659aL, 0xa1a4_d73b_ebf0_93d5L}, // ***
    // 24: 2^-(2^23) =   2^-8388608 =   2.3449477057322620222546775527242476219043877555386221929831430440E-2525223 = 0.23449477057322620222546775527242476219043877555386221929831430440e-2525222
//      {-2525222, 0x3c07_d96a_b1ed_7799L, 0xcb73_55c2_2cc0_5ac0L, 0x4ffc_0ab7_3b1f_6a48L},
    {-2525222, 0x3c07_d96a_b1ed_7799L, 0xcb73_55c2_2cc0_5ac0L, 0x4ffc_0ab7_3b1f_6a49L}, // ***
    // 25: 2^-(2^24) =   2^-16777216 =   5.4987797426189993226257377747879918011694025935111951649826798628E-5050446 = 0.54987797426189993226257377747879918011694025935111951649826798628e-5050445
//      {-5050445, 0x8cc4_cd8c_3ede_fb9aL, 0x6c8f_f86a_90a9_7e0cL, 0x166c_fddb_f98b_71bcL},
    {-5050445, 0x8cc4_cd8c_3ede_fb9aL, 0x6c8f_f86a_90a9_7e0cL, 0x166c_fddb_f98b_71bfL}, // ***
    // 26: 2^-(2^25) =   2^-33554432 =   3.0236578657837068435515418409027857523343464783010706819696074665E-10100891 = 0.30236578657837068435515418409027857523343464783010706819696074665e-10100890
//      {-10100890, 0x4d67_d81c_c88e_1228L, 0x1d7c_fb06_666b_79b3L, 0x7b91_6728_aaa4_e70bL},
    {-10100890, 0x4d67_d81c_c88e_1228L, 0x1d7c_fb06_666b_79b3L, 0x7b91_6728_aaa4_e70dL}, // ***
    // 27: 2^-(2^26) =   2^-67108864 =   9.1425068893156809483320844568740945600482370635012633596231964471E-20201782 = 0.91425068893156809483320844568740945600482370635012633596231964471e-20201781
//      {-20201781, 0xea0c_5549_4e7a_552dL, 0xb88c_b948_4bb8_6c61L, 0x8d44_893c_610b_b7d8L},
    {-20201781, 0xea0c_5549_4e7a_552dL, 0xb88c_b948_4bb8_6c61L, 0x8d44_893c_610b_b7dFL}, // ***
    // 28: 2^-(2^27) =   2^-134217728 =   8.3585432221184688810803924874542310018191301711943564624682743545E-40403563 = 0.83585432221184688810803924874542310018191301711943564624682743545e-40403562
    {-40403562, 0xd5fa_8c82_1ec0_c24aL, 0xa80e_46e7_64e0_f8b0L, 0xa727_6bfa_432f_ac7eL},
    // 29: 2^-(2^28) =   2^-268435456 =   6.9865244796022595809958912202005005328020601847785697028605460277E-80807125 = 0.69865244796022595809958912202005005328020601847785697028605460277e-80807124
    {-80807124, 0xb2da_e307_426f_6791L, 0xc970_b82f_58b1_2918L, 0x0472_592f_7f39_190eL},
    // 30: 2^-(2^29) =   2^-536870912 =   4.8811524304081624052042871019605298977947353140996212667810837790E-161614249 = 0.48811524304081624052042871019605298977947353140996212667810837790e-161614248
//      {-161614248, 0x7cf5_1edd_8a15_f1c9L, 0x656d_ab34_98f8_e697L, 0x12da_a2a8_0e53_c809L},
    {-161614248, 0x7cf5_1edd_8a15_f1c9L, 0x656d_ab34_98f8_e697L, 0x12da_a2a8_0e53_c807L},
    // 31: 2^-(2^30) =   2^-1073741824 =   2.3825649048879510732161697817326745204151961255592397879550237608E-323228497 = 0.23825649048879510732161697817326745204151961255592397879550237608e-323228496
    {-323228496, 0x3cfe_609a_b588_3c50L, 0xbec8_b5d2_2b19_8871L, 0xe184_7770_3b46_22b4L},
//      {-323228496, 0x3cfe_609a_b588_3c50L, 0xbec8_b5d2_2b19_8871L, 0xe184_7770_3b46_22bAL}, // ***
    // 32: 2^-(2^31) =   2^-2147483648 =   5.6766155260037313438164181629489689531186932477276639365773003794E-646456994 = 0.56766155260037313438164181629489689531186932477276639365773003794e-646456993
    {-646456993, 0x9152_447b_9d7c_da9aL, 0x3b4d_3f61_10d7_7aadL, 0xfa81_bad1_c394_adb4L},
  }; // private static final long[][] NEG_POWERS_OF_2 = {

  /**
   * Quasidecimal form of the MIN_NORMAL
   * 2^-2147483646 =   2.2706462104014925375265672651795875812474772990910655746309201518E-646456993 = 0.22706462104014925375265672651795875812474772990910655746309201518e-646456992 */
  private static final long[] MIN_NORMAL_QUASIDEC = {
      -646456992,
      0x3a20_e831_7231_f10aL,
      0x7e1e_e626_d389_6445L,
      0x9767_1787_1b08_457bL,
  }; // private static final long[] MIN_NORMAL_QUASIDEC = {

  // Buffers used internally
  // The order of words in the arrays is big-endian: the highest part is in buff[0] (in buff[1] for buffers of 10 words)
  private static final long[] BUFFER_4x32_A   = new long[4];

  private static final long[] BUFFER_4x64_A   = new long[4];
  private static final long[] BUFFER_4x64_B   = new long[4];

  private static final long[] BUFFER_3x64_A   = new long[3];
  private static final long[] BUFFER_3x64_B   = new long[3];
  private static final long[] BUFFER_3x64_C   = new long[3];
  private static final long[] BUFFER_3x64_D   = new long[3];

  private static final long[] BUFFER_5x32_A   = new long[5];
  private static final long[] BUFFER_5x32_B   = new long[5];

  private static final long[] BUFFER_6x32_A   = new long[6];
  private static final long[] BUFFER_6x32_B   = new long[6];
  private static final long[] BUFFER_6x32_C   = new long[6];

  private static final long[] BUFFER_10x32_A  = new long[10];
  private static final long[] BUFFER_10x32_B  = new long[10];

  private static final long[] BUFFER_12x32    = new long[12];

  /**
   * The mantissa of the Sqrt(2) in a format convenient for multiplying,
   * SQRT_2_AS_LONGS[1] .. SQRT_2_AS_LONGS[3] contains the mantissa including the implied unity
   * that is in the high bit of SQRT_2_AS_LONGS[1]. The other bits contain the fractional part of the mantissa.
   * Used by multBySqrt2()
   */
  private static final long[] SQRT_2_AS_LONGS = new long[] {
    // 0, 0xb504_f333_f9de_6484L, 0x597d_89b3_754a_be9fL, 0x1d6f_60ba_893b_a84cL, // 0xed17_ac85_8333_9915L,
       0, 0xb504_f333_f9de_6484L, 0x597d_89b3_754a_be9fL, 0x1d6f_60ba_893b_a84dL, // 0xed17_ac85_8333_9915L, + carry from the next word
  }; // private static final long[] SQRT_2_AS_LONGS = new long[] {

  private static final int[] SQUARE_BYTES = {
  //   0:
    0x0000,  // 1.000000000000 (+0000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0201,  // 1.007827758789 (+0201_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0404,  // 1.015686035156 (+0404_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0609,  // 1.023574829102 (+0609_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0810,  // 1.031494140625 (+0810_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0a19,  // 1.039443969727 (+0a19_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0c24,  // 1.047424316406 (+0c24_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0e31,  // 1.055435180664 (+0e31_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1040,  // 1.063476562500 (+1040_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1251,  // 1.071548461914 (+1251_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  10:
    0x1464,  // 1.079650878906 (+1464_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1679,  // 1.087783813477 (+1679_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1890,  // 1.095947265625 (+1890_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1aa9,  // 1.104141235352 (+1aa9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1cc4,  // 1.112365722656 (+1cc4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x1ee1,  // 1.120620727539 (+1ee1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x2100,  // 1.128906250000 (+2100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x2321,  // 1.137222290039 (+2321_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x2544,  // 1.145568847656 (+2544_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x2769,  // 1.153945922852 (+2769_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  20:
    0x2990,  // 1.162353515625 (+2990_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x2bb9,  // 1.170791625977 (+2bb9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x2de4,  // 1.179260253906 (+2de4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x3011,  // 1.187759399414 (+3011_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x3240,  // 1.196289062500 (+3240_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x3471,  // 1.204849243164 (+3471_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x36a4,  // 1.213439941406 (+36a4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x38d9,  // 1.222061157227 (+38d9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x3b10,  // 1.230712890625 (+3b10_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x3d49,  // 1.239395141602 (+3d49_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  30:
    0x3f84,  // 1.248107910156 (+3f84_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x41c1,  // 1.256851196289 (+41c1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x4400,  // 1.265625000000 (+4400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x4641,  // 1.274429321289 (+4641_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x4884,  // 1.283264160156 (+4884_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x4ac9,  // 1.292129516602 (+4ac9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x4d10,  // 1.301025390625 (+4d10_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x4f59,  // 1.309951782227 (+4f59_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x51a4,  // 1.318908691406 (+51a4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x53f1,  // 1.327896118164 (+53f1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  40:
    0x5640,  // 1.336914062500 (+5640_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x5891,  // 1.345962524414 (+5891_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x5ae4,  // 1.355041503906 (+5ae4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x5d39,  // 1.364151000977 (+5d39_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x5f90,  // 1.373291015625 (+5f90_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x61e9,  // 1.382461547852 (+61e9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x6444,  // 1.391662597656 (+6444_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x66a1,  // 1.400894165039 (+66a1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x6900,  // 1.410156250000 (+6900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x6b61,  // 1.419448852539 (+6b61_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  50:
    0x6dc4,  // 1.428771972656 (+6dc4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x7029,  // 1.438125610352 (+7029_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x7290,  // 1.447509765625 (+7290_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x74f9,  // 1.456924438477 (+74f9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x7764,  // 1.466369628906 (+7764_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x79d1,  // 1.475845336914 (+79d1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x7c40,  // 1.485351562500 (+7c40_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x7eb1,  // 1.494888305664 (+7eb1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x8124,  // 1.504455566406 (+8124_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x8399,  // 1.514053344727 (+8399_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  60:
    0x8610,  // 1.523681640625 (+8610_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x8889,  // 1.533340454102 (+8889_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x8b04,  // 1.543029785156 (+8b04_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x8d81,  // 1.552749633789 (+8d81_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x9000,  // 1.562500000000 (+9000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x9281,  // 1.572280883789 (+9281_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x9504,  // 1.582092285156 (+9504_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x9789,  // 1.591934204102 (+9789_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x9a10,  // 1.601806640625 (+9a10_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x9c99,  // 1.611709594727 (+9c99_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  70:
    0x9f24,  // 1.621643066406 (+9f24_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xa1b1,  // 1.631607055664 (+a1b1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xa440,  // 1.641601562500 (+a440_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xa6d1,  // 1.651626586914 (+a6d1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xa964,  // 1.661682128906 (+a964_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xabf9,  // 1.671768188477 (+abf9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xae90,  // 1.681884765625 (+ae90_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xb129,  // 1.692031860352 (+b129_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xb3c4,  // 1.702209472656 (+b3c4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xb661,  // 1.712417602539 (+b661_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  80:
    0xb900,  // 1.722656250000 (+b900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xbba1,  // 1.732925415039 (+bba1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xbe44,  // 1.743225097656 (+be44_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xc0e9,  // 1.753555297852 (+c0e9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xc390,  // 1.763916015625 (+c390_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xc639,  // 1.774307250977 (+c639_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xc8e4,  // 1.784729003906 (+c8e4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xcb91,  // 1.795181274414 (+cb91_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xce40,  // 1.805664062500 (+ce40_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xd0f1,  // 1.816177368164 (+d0f1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  90:
    0xd3a4,  // 1.826721191406 (+d3a4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xd659,  // 1.837295532227 (+d659_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xd910,  // 1.847900390625 (+d910_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xdbc9,  // 1.858535766602 (+dbc9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xde84,  // 1.869201660156 (+de84_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xe141,  // 1.879898071289 (+e141_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xe400,  // 1.890625000000 (+e400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xe6c1,  // 1.901382446289 (+e6c1_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xe984,  // 1.912170410156 (+e984_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xec49,  // 1.922988891602 (+ec49_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  // 100:
    0xef10,  // 1.933837890625 (+ef10_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xf1d9,  // 1.944717407227 (+f1d9_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xf4a4,  // 1.955627441406 (+f4a4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xf771,  // 1.966567993164 (+f771_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xfa40,  // 1.977539062500 (+fa40_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xfd11,  // 1.988540649414 (+fd11_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0xffe4,  // 1.999572753906 (+ffe4_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  }; // private static final int[] SQUARE_BYTES = {

  private static final int[] ROOT_BYTES = {
  //   0:
    0x0000,  // sqrt(1.000000000000) = 1.000000000000 (+0000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0001,  // sqrt(1.007827758789) = 1.003906250000 (+0100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0002,  // sqrt(1.015686035156) = 1.007812500000 (+0200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0003,  // sqrt(1.023574829102) = 1.011718750000 (+0300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0004,  // sqrt(1.031494140625) = 1.015625000000 (+0400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0005,  // sqrt(1.039443969727) = 1.019531250000 (+0500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0006,  // sqrt(1.047424316406) = 1.023437500000 (+0600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0007,  // sqrt(1.055435180664) = 1.027343750000 (+0700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0008,  // sqrt(1.063476562500) = 1.031250000000 (+0800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0009,  // sqrt(1.071548461914) = 1.035156250000 (+0900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  10:
    0x000a,  // sqrt(1.079650878906) = 1.039062500000 (+0a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x000b,  // sqrt(1.087783813477) = 1.042968750000 (+0b00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x000c,  // sqrt(1.095947265625) = 1.046875000000 (+0c00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x000d,  // sqrt(1.104141235352) = 1.050781250000 (+0d00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x000e,  // sqrt(1.112365722656) = 1.054687500000 (+0e00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x000f,  // sqrt(1.120620727539) = 1.058593750000 (+0f00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0010,  // sqrt(1.128906250000) = 1.062500000000 (+1000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0011,  // sqrt(1.137222290039) = 1.066406250000 (+1100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0012,  // sqrt(1.145568847656) = 1.070312500000 (+1200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0013,  // sqrt(1.153945922852) = 1.074218750000 (+1300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  20:
    0x0014,  // sqrt(1.162353515625) = 1.078125000000 (+1400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0015,  // sqrt(1.170791625977) = 1.082031250000 (+1500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0016,  // sqrt(1.179260253906) = 1.085937500000 (+1600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0017,  // sqrt(1.187759399414) = 1.089843750000 (+1700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0018,  // sqrt(1.196289062500) = 1.093750000000 (+1800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0019,  // sqrt(1.204849243164) = 1.097656250000 (+1900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x001a,  // sqrt(1.213439941406) = 1.101562500000 (+1a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x001b,  // sqrt(1.222061157227) = 1.105468750000 (+1b00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x001c,  // sqrt(1.230712890625) = 1.109375000000 (+1c00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x001d,  // sqrt(1.239395141602) = 1.113281250000 (+1d00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  30:
    0x001e,  // sqrt(1.248107910156) = 1.117187500000 (+1e00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x001f,  // sqrt(1.256851196289) = 1.121093750000 (+1f00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0020,  // sqrt(1.265625000000) = 1.125000000000 (+2000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0021,  // sqrt(1.274429321289) = 1.128906250000 (+2100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0022,  // sqrt(1.283264160156) = 1.132812500000 (+2200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0023,  // sqrt(1.292129516602) = 1.136718750000 (+2300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0024,  // sqrt(1.301025390625) = 1.140625000000 (+2400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0025,  // sqrt(1.309951782227) = 1.144531250000 (+2500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0026,  // sqrt(1.318908691406) = 1.148437500000 (+2600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0027,  // sqrt(1.327896118164) = 1.152343750000 (+2700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  40:
    0x0028,  // sqrt(1.336914062500) = 1.156250000000 (+2800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0029,  // sqrt(1.345962524414) = 1.160156250000 (+2900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x002a,  // sqrt(1.355041503906) = 1.164062500000 (+2a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x002b,  // sqrt(1.364151000977) = 1.167968750000 (+2b00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x002c,  // sqrt(1.373291015625) = 1.171875000000 (+2c00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x002d,  // sqrt(1.382461547852) = 1.175781250000 (+2d00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x002e,  // sqrt(1.391662597656) = 1.179687500000 (+2e00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x002f,  // sqrt(1.400894165039) = 1.183593750000 (+2f00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0030,  // sqrt(1.410156250000) = 1.187500000000 (+3000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0031,  // sqrt(1.419448852539) = 1.191406250000 (+3100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  50:
    0x0032,  // sqrt(1.428771972656) = 1.195312500000 (+3200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0033,  // sqrt(1.438125610352) = 1.199218750000 (+3300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0034,  // sqrt(1.447509765625) = 1.203125000000 (+3400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0035,  // sqrt(1.456924438477) = 1.207031250000 (+3500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0036,  // sqrt(1.466369628906) = 1.210937500000 (+3600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0037,  // sqrt(1.475845336914) = 1.214843750000 (+3700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0038,  // sqrt(1.485351562500) = 1.218750000000 (+3800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0039,  // sqrt(1.494888305664) = 1.222656250000 (+3900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x003a,  // sqrt(1.504455566406) = 1.226562500000 (+3a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x003b,  // sqrt(1.514053344727) = 1.230468750000 (+3b00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  60:
    0x003c,  // sqrt(1.523681640625) = 1.234375000000 (+3c00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x003d,  // sqrt(1.533340454102) = 1.238281250000 (+3d00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x003e,  // sqrt(1.543029785156) = 1.242187500000 (+3e00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x003f,  // sqrt(1.552749633789) = 1.246093750000 (+3f00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0040,  // sqrt(1.562500000000) = 1.250000000000 (+4000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0041,  // sqrt(1.572280883789) = 1.253906250000 (+4100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0042,  // sqrt(1.582092285156) = 1.257812500000 (+4200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0043,  // sqrt(1.591934204102) = 1.261718750000 (+4300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0044,  // sqrt(1.601806640625) = 1.265625000000 (+4400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0045,  // sqrt(1.611709594727) = 1.269531250000 (+4500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  70:
    0x0046,  // sqrt(1.621643066406) = 1.273437500000 (+4600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0047,  // sqrt(1.631607055664) = 1.277343750000 (+4700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0048,  // sqrt(1.641601562500) = 1.281250000000 (+4800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0049,  // sqrt(1.651626586914) = 1.285156250000 (+4900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x004a,  // sqrt(1.661682128906) = 1.289062500000 (+4a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x004b,  // sqrt(1.671768188477) = 1.292968750000 (+4b00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x004c,  // sqrt(1.681884765625) = 1.296875000000 (+4c00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x004d,  // sqrt(1.692031860352) = 1.300781250000 (+4d00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x004e,  // sqrt(1.702209472656) = 1.304687500000 (+4e00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x004f,  // sqrt(1.712417602539) = 1.308593750000 (+4f00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  80:
    0x0050,  // sqrt(1.722656250000) = 1.312500000000 (+5000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0051,  // sqrt(1.732925415039) = 1.316406250000 (+5100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0052,  // sqrt(1.743225097656) = 1.320312500000 (+5200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0053,  // sqrt(1.753555297852) = 1.324218750000 (+5300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0054,  // sqrt(1.763916015625) = 1.328125000000 (+5400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0055,  // sqrt(1.774307250977) = 1.332031250000 (+5500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0056,  // sqrt(1.784729003906) = 1.335937500000 (+5600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0057,  // sqrt(1.795181274414) = 1.339843750000 (+5700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0058,  // sqrt(1.805664062500) = 1.343750000000 (+5800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0059,  // sqrt(1.816177368164) = 1.347656250000 (+5900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  //  90:
    0x005a,  // sqrt(1.826721191406) = 1.351562500000 (+5a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x005b,  // sqrt(1.837295532227) = 1.355468750000 (+5b00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x005c,  // sqrt(1.847900390625) = 1.359375000000 (+5c00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x005d,  // sqrt(1.858535766602) = 1.363281250000 (+5d00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x005e,  // sqrt(1.869201660156) = 1.367187500000 (+5e00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x005f,  // sqrt(1.879898071289) = 1.371093750000 (+5f00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0060,  // sqrt(1.890625000000) = 1.375000000000 (+6000_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0061,  // sqrt(1.901382446289) = 1.378906250000 (+6100_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0062,  // sqrt(1.912170410156) = 1.382812500000 (+6200_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0063,  // sqrt(1.922988891602) = 1.386718750000 (+6300_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  // 100:
    0x0064,  // sqrt(1.933837890625) = 1.390625000000 (+6400_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0065,  // sqrt(1.944717407227) = 1.394531250000 (+6500_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0066,  // sqrt(1.955627441406) = 1.398437500000 (+6600_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0067,  // sqrt(1.966567993164) = 1.402343750000 (+6700_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0068,  // sqrt(1.977539062500) = 1.406250000000 (+6800_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x0069,  // sqrt(1.988540649414) = 1.410156250000 (+6900_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
    0x006a,  // sqrt(1.999572753906) = 1.414062500000 (+6a00_0000_0000_0000 0000_0000_0000_0000 e7fff_ffff)
  }; // private static final int[] ROOT_BYTES = {

  protected void ____Private_methods___() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**********************************************************************************************
   *** Private methods **************************************************************************
   **********************************************************************************************/

//  // May be used for debugging, to find a value that provides execution of a certain branch
//  // The branch under consideration should assign isFound a value greater than 0
//  private static int ___isFound = 0;
//  // A_debugging method, comment it out when the work is done
//  public static boolean ___isFound() { return ___isFound > 0; }

  protected void ____Used_By_ToString___() {} // Just to put a visible mark of the section in the outline view of the IDE
  // Methods used by String conversions

  /**
   * Multiplies the fractional part of the mantissa of this instance
   * of Quadruple that is expected to be subnormal,
   * by 192-bit quasidecimal value of MIN_MORMAL
   * <br>Uses static buffers
   * <b><i>BUFFER_4x32_A, BUFFER_6x32_A, BUFFER_10x32_A</i></b>
   * @param product_4x64 a buffer of 4 longs to be filled with the result
   * @return product_4x64, filled with the product in a form of a 192-bit quasidecimal value
   */
  private long[] multMantByMinNormal( long[] product_4x64) {
    final long[] factor_6x32 = BUFFER_6x32_A;
    unpack_3x64_to_6x32(MIN_NORMAL_QUASIDEC, factor_6x32);
    final long decimalExpOfPow2 = MIN_NORMAL_QUASIDEC[0];

    final long[] buffer_10x32 = BUFFER_10x32_A;
    Arrays.fill(buffer_10x32, 0);
    return multMantBy192bits(factor_6x32, decimalExpOfPow2, product_4x64, buffer_10x32);
  } // private long[] multMantByMinNormal(long[] result4x64) {

  /**
   * Multiplies the mantissa of this instance
   * of Quadruple by 192-bit quasidecimal value of a power of 2.
   * <br>Uses static buffers
   * <b><i>BUFFER_4x32_A, BUFFER_6x32_A, BUFFER_10x32_A</i></b>
   * @param product_4x64 a buffer of 4 longs to be filled with the result
   * @return product_4x64, filled with the product in a form of a 192-bit quasidecimal value
   */
  private long[] multMantByPowerOfTwo(long[] pow2,  long[] product_4x64) {
    final long[] factor_6x32 = BUFFER_6x32_A;
    unpack_3x64_to_6x32(pow2, factor_6x32);
    final long decimalExpOfPow2 = pow2[0];

    // pow2 multiplied by the implicit unity that's to the left of the point in the Quadruple factor:
    final long[] buffer_10x32 = BUFFER_10x32_A;
    Arrays.fill(buffer_10x32, 0); // !!! 20.05.21 11:03:18 Don't move it to multiply_4x32_by_6x32 !!!
    System.arraycopy(factor_6x32, 0, buffer_10x32, 0, 6);

    return multMantBy192bits(factor_6x32, decimalExpOfPow2, product_4x64, buffer_10x32);
  } // private long[] multMantByPowerOfTwo(long[] pow2,  long[] product_4x64) {

  /**
   * Multiplies the mantissa of this instance by a 192-bit number (a power of 2),
   * given as the content of an unpacked buffer of 6 longs (6 x 32 bits) and the decimal exponent,
   * corrects possible overflow or underflow to ensure that the product is within the range
   * 1/10 * 2^192 .. 9.999.../10 and thus fits the quasidecimal format, packs it into
   * 3 less significant words of {@code product_4x64}, corrects the exponent respectively
   * and puts it in the most significant word of {@code product_4x64}.
   * Used to multiple a quadruple value by a power of 2 while converting it to a String.<br>
   * Uses static buffers <b><i>BUFFER_4x32_A</i></b>
   * @param factor_6x32 factor 2, 192 bits as unpacked buffer 6 x 32, without the integer part (implicit unity)
   * @param decimalExpOfPow2 decimal exponent of the power of 2, whose mantissa is in
   * @param product_4x64 buffer of 4 longs to hold the product
   * @param buffer_10x32 temporal buffer used for multiplication
   *
   * @return product_4x64 filled with the product
   */
  private long[] multMantBy192bits(long[] factor_6x32,  final long decimalExpOfPow2,
                                   long[] product_4x64, long[] buffer_10x32) {
    unpackQuadToBuff(this, BUFFER_4x32_A);

    // multiply 6 x 32 bits by 4 x 32 bits
    for (int i = 5; i >= 0; i--) // compute partial 32-bit products
      for (int j = 3; j >= 0; j--) {
        final long product = factor_6x32[i] * BUFFER_4x32_A[j];
        buffer_10x32[j + i + 1] += product & LOWER_32_BITS;
        buffer_10x32[j + i] += product >>> 32;
      }

    for (int i = 9; i > 0; i--) { // Carry higher bits of the product to the lower bits of the next word
      buffer_10x32[i - 1] += buffer_10x32[i] >>> 32;
      buffer_10x32[i] &= LOWER_32_BITS;
    }

    final int expCorrection = (exponent == 0)? // Subnormal
                                correctPossibleUnderflow(buffer_10x32) :
                                correctPossibleOverflow(buffer_10x32);

    pack_10x32_to_3x64(buffer_10x32, product_4x64);
    product_4x64[0] = decimalExpOfPow2 + expCorrection;    // Correct exponent
    return product_4x64;
  } // private long[] multMantBy192bits(long[] factor_4x32, long[] factor_6x32, ...

  /**
   * Corrects possible underflow of the decimal mantissa, passed in in the {@code buffer_10x32},
   * by multiplying it by a power of ten. The corresponding value to adjust the decimal exponent is returned as the result
   * @param buffer_10x32 a buffer containing the mantissa to be corrected
   * @return a corrective (addition) that is needed to adjust the decimal exponent of the number
   */
  private static int correctPossibleUnderflow(long[] buffer_10x32) {
    int expCorr = 0;
    while (isLessThanOne(buffer_10x32)) { // Underflow
      multBuffBy10(buffer_10x32);
      expCorr -= 1;
    }
    return expCorr;
  } // private int correctPossibleUnderflow(long[] buffer_10x32) {

  /**
   * Corrects possible overflow of the decimal mantissa, passed in in the {@code buffer_10x32},
   * by dividing it by a power of ten. The corresponding value to adjust the decimal exponent is returned as the result
   * @param buffer_10x32 a buffer containing the mantissa to be corrected
   * @return a corrective (addition) that is needed to adjust the decimal exponent of the number
   */
  private static int correctPossibleOverflow(long[] buffer_10x32) {
    int expCorr = 0;
    if ((buffer_10x32[0] & HIGHER_32_BITS) != 0) { // Overflow
      divBuffBy10(buffer_10x32);
      expCorr = 1;
    }
    return expCorr;
  } // private int correctPossibleOverflow(long[] buffer_10x32) {

  /**
   * Unpacks the mantissa of a 192-bit quasidecimal (4 longs: exp10, mantHi, mantMid, mantLo)
   * to a buffer of 6 longs, where the least significant 32 bits of each long contains
   * respective 32 bits of the mantissa
   * @param qd192 array of 4 longs containing the number to unpack
   * @param buff_6x32 buffer of 6 long to hold the unpacked mantissa
   */
  private static void unpack_3x64_to_6x32(long[] qd192, long[] buff_6x32) {
    buff_6x32[0] = qd192[1] >>> 32;
    buff_6x32[1] = qd192[1] & LOWER_32_BITS;
    buff_6x32[2] = qd192[2] >>> 32;
    buff_6x32[3] = qd192[2] & LOWER_32_BITS;
    buff_6x32[4] = qd192[3] >>> 32;
    buff_6x32[5] = qd192[3] & LOWER_32_BITS;
  } // private static void unpack_3x64_to_6x32(long[] power, long[] buff) {

  /**
   * Unpacks the mantissa of given quadruple to 4 longs of buffer,
   * so that each word of the buffer contains the corresponding 32 bits of the mantissa
   * in its least significant 32 bits
   * @param quad a quadruple to unpack
   * @param buffer a buffer to hold the unpacked mantissa
   */
  private static void unpackQuadToBuff(Quadruple quad, long[] buffer) {
    buffer[0] = quad.mantHi >>> 32;                // big-endian, highest word
    buffer[1] = quad.mantHi & LOWER_32_BITS;
    buffer[2] = quad.mantLo >>> 32;
    buffer[3] = quad.mantLo & LOWER_32_BITS;
  } // private static void unpackQuadToBuff(Quadruple quad, long[] buffer) {

  /**
   * Divides the unpacked value stored in the given buffer by 10
   * @param buffer contains the unpacked value to divide (32 least significant bits are used)
   */
  private static void divBuffBy10(long[] buffer) {
    long r;
    final int maxIdx = buffer.length - 1;
    for (int i = 0; i <= maxIdx; i++) { // big/endian
      r = Long.remainderUnsigned(buffer[i], 10);
      buffer[i] = Long.divideUnsigned(buffer[i], 10);
      if (i < maxIdx)
        buffer[i+1] += r << 32;
    }
  } // private static void divBuffBy10(long[] buff) {

  /**
   * Checks if the unpacked quasidecimal value held in the given buffer
   * is less than one (in this format, one is represented as { 0x1999_9999L, 0x9999_9999L, 0x9999_9999L,...}
   * @param buffer a buffer containing the value to check
   * @return {@code true}, if the value is less than one
   */
  private static boolean isLessThanOne(long[] buffer) {
    if (buffer[0] < 0x1999_9999L) return true;
    if (buffer[0] > 0x1999_9999L) return false;

    // A note regarding the coverage:
    // Multiplying a 128-bit number by another 192-bit number,
    // as well as multiplying of two 192-bit numbers,
    // can never produce 320 (or 384 bits, respectively) of 0x1999_9999L, 0x9999_9999L,
    for (int i = 1; i < buffer.length; i++) { // so this loop can't be covered entirely
      if (buffer[i] < 0x9999_9999L) return true;
      if (buffer[i] > 0x9999_9999L) return false;
    }
    // and it can never rich this point in real life.
    return false; // Still Java requires the return statement here.
  } // private static boolean isLessThanOne(long[] buffer) {

  /**
   * Multiplies the unpacked value stored in the given buffer by 10
   * @param buffer contains the unpacked value to multiply (32 least significant bits are used)
   */
  private static void multBuffBy10(long[] buffer) {
    final int maxIdx = buffer.length - 1;
    buffer[0] &= LOWER_32_BITS;
    buffer[maxIdx] *= 10;
    for (int i = maxIdx - 1; i >= 0; i--) {
      buffer[i] = buffer[i] * 10 + (buffer[i + 1] >>> 32);
      buffer[i + 1] &= LOWER_32_BITS;
    }
  } // private static void multBuffBy10(long[] buff) {

  /**
   * Packs the unpacked quasidecimal value contained in unpackedQDMant
   * (which uses only the least significant 32 bits of each word)
   * to packed quasidecimal value (whose exponent should be in v[0] and 192 bits of mantissa in v[1]..v[3] )
   * @param unpackedQDMant a buffer of at least 6 longs, containing the unpacked mantissa
   * @param packedQDValue a buffer of at least 4 longs to hold the result
   */
  private static void pack_10x32_to_3x64(long[] unpackedQDMant, long[] packedQDValue) {
    packedQDValue[1] = (unpackedQDMant[0] << 32) + unpackedQDMant[1];
    packedQDValue[2] = (unpackedQDMant[2] << 32) + unpackedQDMant[3];
    packedQDValue[3] = (unpackedQDMant[4] << 32) + unpackedQDMant[5];
  } // private static void pack_10x32_to_3x64(long[] unpackedQDMant, long[] packedQDValue) {

  /** Calculates the required power and returns the result in
   * the quasidecimal format (an array of longs, where result[0] is the decimal exponent
   * of the resulting value, and result[1] -- result[3] contain 192 bits of the mantissa divided by ten
   * (so that 8 looks like  <pre>{@code {1, 0xCCCC_.._CCCCL, 0xCCCC_.._CCCCL, 0xCCCC_.._CCCDL}}}</pre>
   * uses static arrays
   * <b><i>BUFFER_4x64_B</b>, BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_12x32</i></b>,
   * @param exp the power to raise 2 to
   * @return   the value of {@code2^exp}
   */
  private static long[] powerOfTwo(long exp) {
    if (exp == 0)
      return POS_POWERS_OF_2[0];

    long[][] powers = POS_POWERS_OF_2;  // positive powers of 2 (2^0, 2^1, 2^2, 2^4, 2^8 ... 2^(2^31) )
    if (exp < 0) {
      exp = -exp;
      powers = NEG_POWERS_OF_2;         // positive powers of 2 (2^0, 2^-1, 2^-2, 2^-4, 2^-8 ... 2^30)
    }
    // say("powerOfTwo: exp = %s (%s)", exp, hexStr((int)exp));

    long currPowOf2 = POW_2_31_L;       // 2^31 = 0x8000_0000L; a single bit that will be shifted right at every iteration
    int idx = powers.length - 1;        // Index in the table of powers
    long[] power = null;

    // if exp = b31 * 2^31 + b30 * 2^30 + .. + b0 * 2^0, where b0..b31 are the values of the bits in exp,
    // then 2^exp = 2^b31 * 2^b30 ... * 2^b0. Find the product, using a table of powers of 2.
    while (exp > 0) {
      if (exp >= currPowOf2) {          // the current bit in the exponent is 1
        if (power == null)
          power = powers[idx];   // 4 longs, power[0] -- decimal (?) exponent, power[1..3] -- 192 bits of mantissa
        else
          power = multPacked3x64_AndAdjustExponent(power, powers[idx]); // Multiply by the corresponding power of 2
        exp -= currPowOf2;
      }
      idx--; currPowOf2 >>>= 1;
    }

    return power;
  } // private static long[] powerOfTwo(long exp) {

  /** Multiplies two quasidecimal numbers
   * contained in buffers of 3 x 64 bits with exponents, puts the product to <b><i>BUFFER_4x64_B</i></b><br>
   * and returns it.
   * Both each of the buffers and the product contain 4 longs - exponent and 3 x 64 bits of mantissa.
   * If the higher word of mantissa of the product is less than 0x1999_9999_9999_9999L (i.e. mantissa is less than 0.1)
   * multiplies mantissa by 10 and adjusts the exponent respectively.
   * <br>uses static arrays
   * <b><i>BUFFER_4x64_B, BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_12x32</i></b>,
   * (Big-endian)
   * @param factor1
   * @param factor2
   * @return BUFFER_4x64_B
   */
  private static long[] multPacked3x64_AndAdjustExponent(long[] factor1, long[] factor2) {
    multPacked3x64_simply(factor1, factor2 );
    final int expCorr = correctPossibleUnderflow(BUFFER_12x32);
    long[] result = BUFFER_4x64_B;
    result = pack_12x32_to_3x64(BUFFER_12x32, result);

    result[0] = factor1[0] + factor2[0] + expCorr; // product.exp = f1.exp + f2.exp
    return result;
  } // private static long[] multPacked3x64_AndAdjustExponent(long[] factor1, long[] factor2) {

  /**
   * Multiplies mantissas of two packed quasidecimal values
   * (each is an array of 4 longs, exponent + 3 x 64 bits of mantissa)
   * Returns the product as unpacked buffer of 12 x 32 (12 x 32 bits of product)<br>
   * uses static arrays <b><i>BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_12x32</b></i>
   * @param factor1 an array of longs containing factor 1 as packed quasidecimal
   * @param factor2 an array of longs containing factor 2 as packed quasidecimal
   * @return BUFF_12x32 filled with the product of mantissas
   */
  private static long[] multPacked3x64_simply(long[] factor1, long[] factor2) {
    Arrays.fill(BUFFER_12x32, 0);
    // TODO2 19.01.16 21:23:06 for the next version -- rebuild the table of powers to make the numbers unpacked, to avoid packing/unpacking
    unpack_3x64_to_6x32(factor1, BUFFER_6x32_A);
    unpack_3x64_to_6x32(factor2, BUFFER_6x32_B);

    for (int i = 5; i >= 0; i--) // compute partial 32-bit products
      for (int j = 5; j >= 0; j--) {
        final long part = BUFFER_6x32_A[i] * BUFFER_6x32_B[j];
        BUFFER_12x32[j + i + 1] += part & LOWER_32_BITS;
        BUFFER_12x32[j + i] += part >>> 32;
      }

    for (int i = 11; i > 0; i--) { // Carry higher bits of the product to the lower bits of the next word
      BUFFER_12x32[i - 1] += BUFFER_12x32[i] >>> 32;
      BUFFER_12x32[i] &= LOWER_32_BITS;
    }
    return BUFFER_12x32;
  } // private static long[] multPacked3x64_simply(long[] factor1, long[] factor2) {

  /**
   * converts 192 most significant bits of the mantissa of a number from an unpacked quasidecimal form (where 32 least significant bits only used)
   * to a packed quasidecimal form (where buff[0] contains the exponent and buff[1]..buff[3] contain 3 x 64 = 192 bits of mantissa)
   * @param unpackedMant a buffer of at least 6 longs containing an unpacked value
   * @param result a buffer of at least 4 long to hold the packed value
   * @return packedQD192 with words 1..3 filled with the packed mantissa. packedQD192[0] is not affected.
   */
  private static long[] pack_12x32_to_3x64(long[] unpackedMant, long[] result) {
    result[1] = (unpackedMant[0]   << 32) + unpackedMant[1];
    result[2] = (unpackedMant[2]   << 32) + unpackedMant[3];
    result[3] = (unpackedMant[4]   << 32) + unpackedMant[5];
    return result;
  } // private static long[] pack_12x32_to_3x64(long[] unpackedMant, long[] result) {

  /**
   * Converts the decimal mantissa of a number given in a binary form into
   * a string of decimal digits of the required length.
   * Rounds it up as needed and corrects the number's exponent in case of overflow caused by rounding up (e.g. 9.9999...e-1 -> 1.0e0)
   * <br>Uses static arrays
   * <b><i>BUFFER_6x32_A</i></b>
   * <pre>
   * @param qdNumber contains a quasi-decimal representation of the number
   *    ({@code qdNumber[0]} -- decimal exponent,
   *     {@code qdNumber[1]..qdNumber[3]} -- decimal mantissa divided by 10)}</pre>
   * @param strLen the required length of the string (number of significant digits of the mantissa to print,
   * including one that precedes the decimal point)
   * @return
   */
  private static StringBuilder decimalMantToString(long[] qdNumber, int strLen) {
    final long[] multBuffer = BUFFER_6x32_A;
    final StringBuilder sb = convertMantToString(qdNumber, multBuffer, strLen);

    if ((multBuffer[0] & 0x8000_0000L) != 0)  // if the remainder > 5e-40, round up
      if (addCarry(sb) == 1)       // 0.9999.. rounded to 1.00000...
        qdNumber[0]++;              // Increase exponent, 9.9999...e-1 -> 1.0e0

    if (sb.length() < strLen)       // Supplement with zeros up to the required length
      sb.append(ZEROS, 0, strLen - sb.length());

    return sb;
  } // private static StringBuilder decimalMantToString(long[] qdNumber, int strLen) {

  /**
   * Converts the decimal mantissa of a number given in a binary form into
   * a string of decimal digits of the length that is less or equal to {@code maxLen}.
   * Leaves a result of multiplying mantissa by {@code 10^maxLen} in the {@code multBuffer}
   * <pre>
   * @param qdNumber contains a quasi-decimal representation of the number (packed)
   *    ({@code qdNumber[0]} -- decimal exponent,
   *     {@code qdNumber[1]..qdNumber[3]} -- decimal mantissa divided by 10)}</pre>
   * @param multBuffer a buffer of 6 longs to store interim products of m * 10^n
   * @param maxLen maximal number of decimal digits to find
   * @return a new {@code StringBuilder}, containing decimal digits of the number
   */
  private static StringBuilder convertMantToString(long[] qdNumber, long[] multBuffer, int maxLen) {
    final StringBuilder sb = new StringBuilder(maxLen);
    unpack_3x64_to_6x32(qdNumber, multBuffer);      // 6 longs where only 32 lower bits of each are filled
    int charCount = 0;
    do {
      multBuffBy10(multBuffer);                              // next digit in turn gets into bits 35..32 of multBuffer
      sb.append(Character.forDigit((int)(multBuffer[0] >>> 32), 10)); // Here it is
      charCount++;
    } while (charCount < maxLen && !isEmpty(multBuffer));
    return sb;
  } // private static StringBuilder convertMantToString(long[] qdNumber, long[] multBuffer, int maxLen) {

  /**
   * Adds one to a decimal number represented as a sequence of decimal digits contained in {@code StringBuilder}.
   * propagates carry as needed, so that {@code addCarryTo("6789") = "6790", addCarryTo("9999") = "10000"} etc.
   * @param sb a {@code StringBuilder} containing the number that is added one to
   * @return 1 if an additional higher "1" was added in front of the number as a result of rounding-up,
   * 0 otherwise
   */
  private static int addCarry(StringBuilder sb) {
    for (int i = sb.length() - 1; i >= 0; i--) { // starting with the lowest digit
      final char c = sb.charAt(i);
      if (c == '9') sb.setCharAt(i, '0');  // replace with '0' and continue with the next higher digit
      else {
        sb.setCharAt(i, (char)(c + 1));    // replace n with n+1 and quit
        return 0;
      }
    }
    sb.insert(0, '1'); // all the digits were 9s and turned to 0s, "000" -> "1000"
    sb.deleteCharAt(sb.length() - 1); // Remove the last (excessive) 0, "1000" -> "100"
    return 1;
  } // private static int addCarry(StringBuilder sb) {

  /**
   * Checks if the buffer is empty (contains nothing but zeros)
   * @param buffer the buffer to check
   * @return {@code true} if the buffer is empty, {@code false} otherwise
   */
  private static boolean isEmpty(long[] buffer) {
    for (int i = 0; i < buffer.length; i++)
      if (buffer[i] != 0)
        return false;
    return true;
  } // private static boolean isEmpty(long[] buffer) {

  /** Returns String representing the given Quadruple as a sequence of
   * hexadecimal records of the fields of the Quadruple,
   * formatted as '(+/-)hex(mantHi) hex(mantLo) e hex(exponent),
   * e.g for -1.5 it will be "-8000_0000_0000_0000 0000_0000_0000_0000 e 7fff_ffff"
   * @param q1 the Quadruple instance to format
   * @return
   */
  private static String hexStr(Quadruple q1) {
    return String.format( "%s%s %s e %s", (q1.isNegative()? "-":"+"),
                  hexStr(q1.mantHi()), hexStr(q1.mantLo()),
                  hexStr(q1.exponent()));
  } // private static String hexStr(Quadruple q1) {

  /**
   * Returns a hexadecimal string representation of the given long value in form
   * 'DDDD_DDDD_DDDD_DDDD', where D stands for a hexadecimal digit
   * @param lValue a {@code long} value to convert to a hex string
   * @return Hexadecimal string with separators
   */
  private static String hexStr(long lValue) {
    return String.format(
        "%04x_%04x_%04x_%04x",
        lValue >> 48 & 0xFFFF, lValue >> 32 & 0xFFFF,
        lValue >> 16 & 0xFFFF, lValue & 0xFFFF);
  } // private static String hexStr(long lValue) {

  /**
   * Returns a hexadecimal string representation of the given int value in form
   * 'DDDD_DDDD', where D stands for a hexadecimal digit
   * @param iValue an int value to convert to a hex string
   * @return Hexadecimal string with separators
   */
  private static String hexStr(int iValue) {
    return String.format(
        "%04x_%04x",
        iValue >> 16 & 0xFFFF, iValue & 0xFFFF);
  } // private static String hexStr(int iValue) {

  /********************************************************************************************
   *** Used by assign(double v) ***********************************************************
   *** It's just a marker for a more convenient navigation in IDE *****************************
   ********************************************************************************************/

  protected void   ____Used_By_Assign_double_v___() {} // Just to put a visible mark of the section in the outline view of the IDE

  /** Gets a subnormal double value as long (as {@code Double#doubleToLongBits(double)} returns,
   * but without sign bit) and sets the {@code exponent and mantHi} fields of this instance of {@code Quadruple} so that
   * the resulting value is equal to the original double value. {@code mantLo} field is expected to be cleared already.
   * @param  doubleAsLong the original subnormal double value as long, with cleared sign bit
   * @return this instance, with exponent and mantHi set appropriately
   */
  private Quadruple makeQuadOfSubnormDoubleAsLong(long doubleAsLong) {
    final int numOfZeros = Long.numberOfLeadingZeros(doubleAsLong); // sign is cleared, exponent is 0 -- so it's the number of zeros to the left of the most significant bit
    exponent = EXPONENT_BIAS - EXP_0D - (numOfZeros - 12);           // bias - position of MSB relative to implied high bit in normal double
    if (numOfZeros < 63)    // 19.11.25 20:17:33: To prevent using unchanged doubleAsLong that otherwise happens
                            // when numOfZeros == 63, since d << 64 does nothing
      mantHi = doubleAsLong << numOfZeros + 1;                    // msb to the normal position (to the left from the highest bit)
    return this;
  } // private Quadruple makeQuadOfSubnormDoubleAsLong(long doubleAsLong) {

  /********************************************************************************************
   *** Used by assign(BigDecimal v) ***********************************************************
   *** It's just a marker for a more convenient navigation in IDE *****************************
   ********************************************************************************************/

  protected void   ____Used_By_Assign_BigDecimal_v___() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Calculates a value of the unbiased binary exponent of the given value ({@code floor(log2(value))})
   * and returns it as a {@code long}. Due to rounding errors, the result may differ from the true exponent value
   * by +/-1.
   * @param value the value whose exponent we need to find
   * @return unbiased binary exponent of the given value
   */
  private static long findBinaryExponent(BigDecimal value) {
    final int exp10 = value.precision() - value.scale() - 1;             // floor(log10(value))
    final double mant10d = value.divide( raise10toPower(exp10), MC_20_HALF_EVEN).doubleValue(); // Decimal mantissa, in range [1.0, 10.0)
    return (long) Math.floor( exp10 * LOG2_10 + log2(mant10d) );   // Binary exponent = floor(log2(value))
  } // private static long findBinaryExponent(BigDecimal value) {

  /**
   * Raises {@code BigDecimal} value of {@code 10.0} to power exp10
   * @param exp10 the power to raise to
   * @return {@code BigDecimal} value of 10.0<sup>exp10}</sup>
   */
  private static BigDecimal raise10toPower(int exp10) {
    return BigDecimal.ONE.scaleByPowerOfTen(exp10);
  } // private static BigDecimal raise10toPower(int exp10) {

  /**
   * Calculates log<sub>2</sub> of the given x
   * @param x argument that can't be 0
   * @return the value of log<sub>2</sub>(x)
   */
  private static double log2(double x) {
    // x can't be 0
    return LOG2_E * Math.log(x);
  } // private static double log2(double x) {

  /**
   * Preliminary checks whether a value with the given binary exponent may belong
   * to the range that is acceptable for {@code Quadruple}. For this purpose,
   * the exponent value exceeding the strict limits of the allowed range by +/-1 is considered
   * permissible, since it may get adjusted later.
   * @param unbiasedExp the unbiased binary exponent of the value
   * @return {@code true} if the exponent, being converted to biased form, falls within the range
   * <span class="nowrap">{@code -129 < exp < EXPONENT_OF_MAX_VALUE + 1},</span> {@code false} otherwise
   */
  private boolean inAcceptableRange(long unbiasedExp) {
    final long exponent = unbiasedExp + EXPONENT_BIAS;           // exp2 unbiased - bias it
    if ((exponent   < -129) || (exponent > EXPONENT_OF_MAX_VALUE + 1))
      return false;
    return true;
  } // private boolean inAcceptableRange(long unbiasedExp) {

  /**
   * For values of the unbiased binary exponent, corresponding to Quadruple values that would exceed
   * the acceptable range, assigns to this instance corresponding boundary values -- {@code 0} or {@code Infinity) -- without affecting the sign.
   * @param unbiasedExp
   * @return this instance of Quadruple with the value of: 0 or -0 if the exponent value is less than -129,
   * Infinity or -Infinity, if the exponent is greater than EXPONENT_OF_MAX_VALUE + 1, and unchanged value in other cases
   * @param unbiasedExp the unbiased binary exponent of the value
   * @see #inAcceptableRange(long)
   */
  private Quadruple assignBoundaryValue(long unbiasedExp) {
    final long exponent = unbiasedExp + EXPONENT_BIAS;           // exp2 unbiased - bias it
    if (exponent   < -129)
      return assignZero(false);
    if (exponent > EXPONENT_OF_MAX_VALUE + 1)
      return assignInfinity(false);
    return this;
  } // private Quadruple assignBoundaryValue(long unbiasedExp) {

  /**
   * Finds the binary mantissa of the given value: <br>
   * {@code mant = value / 2^exp2} for non-negative values of exp2,}<br>
   * {@code mant = value * 2^exp2} for negative values of exp2.}<br>
   * If {@code exp2} is found correctly, result falls in the range [1.0, 2.0)
   * @param value a value whose mantissa is to be found (non-negative)
   * @param exp2 the magnitude (absolute value) of the unbiased binary exponent of the value
   * @param negExp a flag signifying that the real exponent is negative (i.e. the value < 1.0)
   * @return the binary mantissa of the value
   */
  private BigDecimal findBinaryMantissa(BigDecimal value, long exp2, boolean negExp) {
    final BigDecimal pow2 = twoRaisedTo(exp2);                      // power of 2
    return (negExp)?  value.multiply(pow2, MC_120_HALF_EVEN) :  // binary mantissa
                      value.divide(pow2, MC_120_HALF_EVEN);
  } // private BigDecimal findBinaryMantissa(BigDecimal value, long exp2, boolean negExp) {

  /**
   * Raises {@code BigDecimal} value {@code 2.0} to the given power and returns the result
   * @param exp2 the power to raise to
   * @return {@code BigDecimal} value of {@code 2^exp2}
   */
  private BigDecimal twoRaisedTo(long exp2) {
    final int _1e8 = 100_000_000;             // We can't raise a BigDecimal to a power >= 1_000_000_000 directly
    return exp2 <= _1e8?
        BD_TWO.pow((int)exp2, MC_120_HALF_EVEN) :
        TWO_RAISED_TO_1E8.pow((int)(exp2 / _1e8), MC_120_HALF_EVEN )
                       .multiply(BD_TWO.pow((int)(exp2 % _1e8), MC_120_HALF_EVEN), MC_120_HALF_EVEN);
  } // private BigDecimal twoRaisedTo(long exp2) {

  /** Assigns the value of the binary mantissa to the fields mantHi, mantLo of this instance.
   * if the remainder that is to be lopped-off is >= 0.5 * 2^-128 and the value is not subnormal,
   * the mantissa gets incremented and the exponent may be adjusted in the case of overflow.
   * @param mant2 the binary mantissa of the value
   * @param exp2 the magnitude (absolute value) of the binary exponent of the value
   * @param negExp a flag signifying that original exponent was negative (to distinguish subnormal values)
   * @return the value of exp2 (may have been adjusted in case of rounding-up)
   */
  private long assigndMantValue(BigDecimal mant2, long exp2, boolean negExp) {
    BigDecimal fractPart = mant2.subtract(BigDecimal.ONE).multiply(TWO_RAISED_TO_64); // fract. part * 2^64
    mantHi = fractPart.longValue();                                            // Higher 64 bits of mantissa

    fractPart = fractPart.subtract(new BigDecimal(Long.toUnsignedString(mantHi))).multiply(TWO_RAISED_TO_64); // lower 64 bits
    mantLo = fractPart.longValue();

    // 20.09.22 18:14:37 1 + 2^-129 * (n + 0.499...) will round up
    fractPart = fractPart.subtract(new BigDecimal(Long.toUnsignedString(mantLo)), MC_40_HALF_EVEN);   // Fraction left after multiplying by 2^128

    // For subnormal values, negExp == true and exp2 >= Integer.MAX_VALUE
    // Don't round them up, makeSubnormal will do it --
    // unless the fractPart >= 1. fractPart may get rounded up to 1.0 for some subnormal values, since the value of mant2 may occur
    // 1.99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
    // in the cases where it should be 2.0, due to the imprecision of twoRaisedTo()
    if (    (!negExp || exp2 < Integer.MAX_VALUE)     // Not a subnormal
         || fractPart.compareTo(BigDecimal.ONE) >= 0  // Or fractPart >= 1.0 (as a result of rounding of 0.9999999999...
       ) {
      if (fractPart.compareTo(HALF_OF_ONE) >= 0)
        if (++mantLo == 0 && ++mantHi == 0)           //  Rounding up
        exp2 += negExp? -1 : 1;                       // Overflow, adjust exponent
    }
    return exp2;
  } // private long assigndMantValue(BigDecimal mant2, long exp2, boolean negExp) {

  /**
   * For a Quadruple with a normal mantissa (with implied unity)
   * and non-positive biased exponent, converts it into the conventional subnormal form, with the exponent = 0
   * and the mantissa shifted rightwards with explicit 1 in the appropriate position.<br>
   * Shifts mantissa rightwards by |exp2| + 1 bits, sets explicit 1, and rounds it up, taking into account the bits having been shifted-out
   * @param exp2 the exponent of the newly-found subnormal value (always negative)
   * @return the exponent for the new value, 0 in an ordinary case, an 1 if the rounding has led to overflow of the mantissa
   */
  private long makeSubnormal(long exp2) {
    exp2 = -exp2;                               // just for convenience
    if (exp2 > 127) {                           // Effectively 0 or MIN_VALUE
      mantLo = mantHi = 0;
      if (exp2 == 128) mantLo++;                // MIN_VALUE
      return 0;                                 // exp2 >= 129 means 0
    }

    final long shiftedOutBit = shiftMantissa(exp2);

    exp2 = 0;                                   // it's subnormal
    if (shiftedOutBit != 0)
      if (++mantLo == 0 && ++mantHi == 0)       // Round up. carry beyond the higher word?
        exp2++;                                 // it becomes MIN_NORMAL

    return exp2;
  } // private long makeSubnormal(long exp2) {

  /**
   * Shifts the mantissa by exp2 + 1 bits rightwards, to make a conventional subnormal value
   * @param exp2 unbiased exponent of the value (negated)
   * @return the highest bit that has been shifted out beyond the two longs of mantissa (1L if it was 1, 0 otherwise)
   */
  private long shiftMantissa(long exp2) {
    long shiftedOut = mantLo & 1;                // The highest of shifted out bits to evaluate carry
    mantLo = (mantLo >>> 1) | (mantHi << 63);
    mantHi = (mantHi >>> 1) | HIGH_BIT;          // move 1 bit right and set unity that was implied

    if (exp2 >= 64) {                            // the higher word move into the lower
      if (exp2 == 64)
        shiftedOut = mantLo >>> 63;              // former lowest bit of mantHi now is the highest bit of mantLo
      else
        shiftedOut = (mantHi >>> (exp2 - 65)) & 1; // one of the bits of the high word
      mantLo = mantHi >>> exp2 - 64;
      mantHi = 0;
    } else if (exp2 > 0) {                      // Shift both words
      shiftedOut = (mantLo >>> exp2 - 1) & 1;
      mantLo = (mantLo >>> exp2) | (mantHi << 64 - exp2);
      mantHi = mantHi >>> exp2;
    }
    return shiftedOut;
  } // private long shiftMantissa(long exp2) {

  /********************************************************************************************
   *** Used by assign(String s) ***************************************************************
   ********************************************************************************************/

  protected void   ____Used_By_Assign_String_s___() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * A class that parses a string containing a numeric value and sets the fields of its {@code Quadruple} owner accordingly.
   * Contains involved static methods, constants, and interim variables
   */
  private static class NumberParser {

    /** A pattern used to strip leading zeroes from integer numbers */
    private static final Pattern LEADING_ZEROES_PTRN = Pattern.compile("(^0+)(\\d*)");

    /** The maximum number of digits in the mantissa that are taken into account */
    private static final int MAX_MANTISSA_LENGTH  = 59;  // 2^192 = 6.277e57, so the 58-th digit after point may affect the result

    static Quadruple owner;

    /**
     * A mapping of string designations of special values,
     * used by {@link NumberParser#parse(String, Quadruple)}
     */
    @SuppressWarnings("serial")
    private static final Map<String, Quadruple> QUADRUPLE_CONSTS = new HashMap<String, Quadruple>() {{
      put("quadruple.min_value",           MIN_VALUE);
      put("min_value",                     MIN_VALUE);
      put("quadruple.max_value",           MAX_VALUE);
      put("max_value",                     MAX_VALUE);
      put("quadruple.min_normal",          MIN_NORMAL);
      put("min_normal",                    MIN_NORMAL);
      put("quadruple.nan",                 NaN);
      put("nan",                           NaN);
      put("quadruple.negative_infinity",   NEGATIVE_INFINITY);
      put("negative_infinity",             NEGATIVE_INFINITY);
      put("-infinity",                     NEGATIVE_INFINITY);
      put("quadruple.positive_infinity",   POSITIVE_INFINITY);
      put("positive_infinity",             POSITIVE_INFINITY);
      put("infinity",                      POSITIVE_INFINITY);
      put("+infinity",                     POSITIVE_INFINITY);
    }};

    /**
     * A decomposer and container to extract and store the parts of the string representing a number.
     * Its fields are set by the {@link #decompose(String)} method and used to build a Quadruple value
     * @author misa
     */
    private static class NumberParts {

      /** Decimal exponent of the number  */             private long exp10;
      /** Sign flag ({@code true} for negatives) */     private boolean negative;
      /** Mantissa without the dot and leading/trailing zeros */ private String mantStr;
      /** exponent correction, derived from mantissa */ private int expCorrection;

      /***
       * A regex to parse floating-point number with a minimal framing
       * of methods to extract separate parts of the number
       * @author misa
       *
       */
      private static class FPStringRegex {
        private static final Pattern FP_STRING_PTRN = Pattern.compile(
            "^(\\+|-)?((\\d*\\.)?(\\d*))(e(\\+|-)?0*(\\d+))?$", // 19.11.29 17:37:04 Enable any number of zeroes before exponent
            Pattern.CASE_INSENSITIVE);

        private static Matcher m;

        private static void match(String source) {
          m = FP_STRING_PTRN.matcher(source);         //   "^(\\+|-)?((\\d*\\.)?(\\d+))(e(\\+|-)?(\\d+))?$"
          if (!m.find())
            throw new NumberFormatException("Invalid number: '"+source+"'");
        }

        private static boolean negative()       { return ("-".equals(m.group(1))); }
        private static String expString()       { return m.group(5); }
        private static String intPartString()   { return m.group(3); }
        private static String fractPartString() { return m.group(4); }

      }

      private String sourceStr;

      /**
       * Decomposes an input string containing a floating-point number
       * into parts (sign, mantissa, exponent, and necessary exponent correction depending on the mantissa)
       * and sets appropriately the inner fields to be used by subsequent processing
       * @param source the source String
       * @return the reference of this instance
       */
      private NumberParts decompose(String source) {
        this.sourceStr = source;
        FPStringRegex.match(source); // It throws an exception if doesn't match

        negative = FPStringRegex.negative();
        exp10 = extractExp10(FPStringRegex.expString());
        expCorrection = buildMantString(FPStringRegex.intPartString(), FPStringRegex.fractPartString() ); // and exp correction

        return this;
      } // NumberParts.decompose(String source) {

      /**
       * Builds a String containing the mantissa of the floating-point number being parsed
       * as a string of digits without trailing or leading zeros.
       * Finds the exponent correction depending on the point position and the number of leading zeroes.
       * @param intPartString the integer part of the mantissa, (m.b. including the dot)
       * @param fractPartString the integer part of the mantissa
       */
      private int buildMantString(String intPartString, String fractPartString) {
        int expCorrection = uniteMantString(intPartString, fractPartString);

        final Matcher m2 = LEADING_ZEROES_PTRN.matcher(mantStr);     // Strip leading zeroes
        if (m2.find()) {
          mantStr = m2.group(2);
          expCorrection -= m2.group(1).length();                     // - number of leading zeroes stripped
        }
        mantStr = mantStr.replaceFirst("0*$", "");            // Strip trailing zeroes
        return expCorrection;
      } // NumberParts.findMantString(String intPartString, String fractPartString) {

      /**
       * Unites the integer part of the mantissa with the fractional part and computes
       * necessary exponent correction that depends on the position of the decimal point
       * @param intPartString the integer part of the mantissa, may be null for empty mantissa (e.g. "e123"), or consist of only "."
       * @param fractPartString the fractional part of the mantissa, may be empty for e.g. "33.e5"
       * @return the exponent correction to be added to the explicitely expressed number's exponent
       */
      private int uniteMantString(String intPartString, String fractPartString) {
        if (intPartString == null) {
          intPartString = fractPartString;
          fractPartString = "";
        }

        intPartString = intPartString.replaceFirst("\\.$", "");
        if (intPartString.isEmpty() && fractPartString.isEmpty())
          throw new NumberFormatException("Invalid number: "+sourceStr);

        mantStr = intPartString + fractPartString;      // mantissa as a string
        return intPartString.length() - 1;              // 10.0 = 1e1, 1.0 = 1e0, 0.1 = 1e-1 etc;
      } // private int NumberParts.uniteMantString( String intPartString, String fractPartString) {

      private static final Pattern EXP_STR_PTRN = Pattern.compile("e(\\+|-)?(\\d+)");

      /**
       * Extracts a long value of the exponent from a substring
       * containing the exponent of the floating-point number being parsed,
       * e.g. "e+646456993"
       * @param expString substring containing the exponent, may be null if the number is in decimal format (without exponent)
       * @return numeric exponent value
       */
      private static long extractExp10(String expString) {
        long exp10 = 0;
        if (expString != null) {
          final Matcher m = EXP_STR_PTRN.matcher(expString); // It will surely find, otherwise it couldn't get here
          if (m.find()) {
            exp10 = parseLong(m.group(2));
            if ("-".equals(m.group(1))) exp10 = -exp10;
          }
        }
        return exp10;
      } // private static long NumberParts.extractExp10(String expString) {

      /**
       * Parses a String containing an unsigned long number.
       * For values greater than   999_999_999_999_999_999 (1e18-1) returns Long.MAX_VALUE.
       * @param longString string representation of a number
       * @return a long value represented by the longString
       */
      private static long parseLong(String longString) {
        if (longString.length() > 18) return Long.MAX_VALUE;
        return Long.parseLong(longString);
      }

    } // private static class NumberParts {

    static final NumberParts PARTS = new NumberParts();


    /**
     * Parses a string containing a floating-point number and sets the fields
     * of the owner (a {@code Quadruple} instance)
     * <br>uses static arrays
     * <b><i>BUFFER_4x64_B, BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_6x32_C, BUFFER_12x32</i></b>
     * @param source input string to parse
     * @param owner the Quadruple instance whose fields are to set so that it has the value presented by the <b>source</b>
     * @return the <b>owner</b> with the values of the fields modified to correspond to the value presented by the <b>source</b>
     */
    private static Quadruple parse(String source, Quadruple owner) {
      source = source.trim().toLowerCase();

      final Quadruple qConst = QUADRUPLE_CONSTS.get(source);   // Is it one of the standard constants -- NaN, Infinity etc.?
      if (qConst != null)                                // Yes
        return owner.assign(qConst);                    // respective Quadruple value

      source = source.replaceAll("\\_", "");            // Separator '_' is allowed in numbers
      NumberParser.owner = owner;
      buildQuadruple(PARTS.decompose(source));          // Parse and set the owner's fields
      return owner;
    } // private static Quadruple parse(String source, Quadruple owner) {

    /**
     * Builds a quadruple value based on the parts of the decimal floating-point number.
     * Puts the value into the owner's fields
     * <br>uses static arrays
     * <b><i>BUFFER_4x64_B, BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_6x32_C, BUFFER_12x32</i></b>,
     * @param parts contains parts of the parsed number -- integer and fractional parts of the decimal mantissa, exponent, and sign.
     */
    private static void buildQuadruple(NumberParts parts) {
      owner.negative = parts.negative;
      long exp10 = parts.exp10;                        // Explicitely expressed exponent
      final int exp10Corr = parseMantissa(parts, BUFFER_6x32_C);   // Finds numeric value of the decimal mantissa
                                                          // and puts it into buff_6x32_C. Returns necessary exponent correction
      if (exp10Corr == 0 && isEmpty(BUFFER_6x32_C)) {   // Mantissa == 0?
        owner.assignZero(false);                      // value = 0
        return;
      }

      exp10 += exp10Corr; // takes account of the point position in the mant string
                          // and possible carry as a result of round-up (like 9.99e1 -> 1.0e2)

      if (exceedsAcceptableExponentRange(exp10)) return;       // exp10 < MIN_EXP10 or exp10 > MAX_EXP10. Assigns corresponding value to the owner in this case.

      final long exp2 = findBinaryExponent(exp10, BUFFER_6x32_C);
      findBinaryMantissa((int)exp10, exp2, BUFFER_6x32_C);   // Finds binary mantissa and possible exponent correction. Fills the owner's fields.
    } // private static void buildQuadruple(NumberParts parts) {

    /**
     * Finds the numeric value of the normalized decimal mantissa
     * in "quasidecimal" format ( M * 2^192 / 10, so that 1.0 becomes 0x19..99, and 9.99..99 becomes 0xFF..FF)
     * and puts it into the given buffer.
     * Finds and returns the exponent correction to be added to the number's exponent
     * (depending on the position of the decimal point in the original mantissa and possible rounding up), so that
     * 0.123 corresponds to expCorr == -1,  1.23 to expCorr == 0, and 123000.0 to expCorr = 5.
     * @param parts a {@code NumberParts} instance containing the parts of the number
     * @param buffer a buffer to put the numeric value to
     * @return exponent correction to be added to the parsed number's exponent
    /**/
    private static int parseMantissa(NumberParts parts, long[] buffer) {
      if (parts.mantStr.isEmpty()) { // There's nothing but zeroes
        Arrays.fill(buffer, 0);
        return 0;
      } else { // find numeric value of the mantissa
        // Rounding may result in additional exponent correction
        return parseMantString(parts.mantStr, buffer) + parts.expCorrection;
      }
    } // private static void parseMantissa(NumberParts parts, long[] buffer) {

    /** Parses the given String, containing a long decimal number,
     * and sets its numeric 192-bit value in the given buffer.
     * May require to increment exponent if (10^n) - 1 ( == 99..99) gets rounded up to 10^n ( == 100..00),
     * returns 1 in such case, otherwise returns 1.
     * @param mantStr a String containing the decimal number to be parsed
     * @param buffer a buffer to put the found value to
     * @return exponent correction to be added to the explicit exponent of the number
     */
    private static int parseMantString(String mantStr, long[] buffer) { //
      int expCorr = 0;
      final StringBuilder sb = new StringBuilder(mantStr);
      // Limit the string length to avoid unnecessary fuss
      if (sb.length() > MAX_MANTISSA_LENGTH) { // Strip extra digits that can't affect the result
        final boolean carry = sb.charAt(MAX_MANTISSA_LENGTH) >= '5'; // The highest digit to be truncated
        sb.delete(MAX_MANTISSA_LENGTH, sb.length());
        if (carry)                           // Round-up: add carry
          expCorr += addCarry(sb);           // May add an extra digit in front of it (99..99 -> 100)
      }
      findNumericMantValue(sb, buffer);
      return expCorr;
    } // private static int parseMantString(String mantStr, long[] buffer) {

    /**
     * Converts a string of digits to a 192-bit "quasidecimal" numeric value
     * @param sb a StringBuilder containing the digits of the mantissa
     * @param buffer a buffer to put the found value to
     */
    private static void findNumericMantValue(StringBuilder sb, long[] buffer) {
      assert buffer.length == 6: "findMantValue(): buffer length must be 6";
      Arrays.fill(buffer, 0);
      for (int i = sb.length() - 1; i >= 0; i--) { // digits, starting from the last
        buffer[0] |= (long)Character.digit(sb.charAt(i), 10) << 32;
        divBuffBy10(buffer);
      }
    } // private static void findNumericMantValue(StringBuilder sb, long[] buffer) {

    /**
     * Checks that the decimal exponent value doesn't exceed the possible range.<br>
     * if exponent < MIN_EXP10, assigns 0 to the owner and returns {@code true}.
     * if exponent > MAX_EXP10, assigns Infinity to the owner and returns {@code true}.
     * Otherwise returns {@code false}.
     */
    private static boolean exceedsAcceptableExponentRange(long exp10) {
      if (exp10 < MIN_EXP10) {              // exp < MIN_EXP10. Can be nothing but 0
        owner.assignZero(false);            // return 0.0 preserving sign
        return true;
      } else if (exp10 > MAX_EXP10) {        // exp > MIN_EXP10. Can be nothing but Infinity
        owner.assignInfinity(false);        // return Infinity preserving sign
        return true;
      }
      return false;
    }

    /** (2^63) / 10 =~ 9.223372e17*/
    private static final double TWO_POW_63_DIV_10 = 922337203685477580d; // 2^63 / 10

    /**
     * Finds binary exponent, using decimal exponent and mantissa.<br>
     * exp2 = exp10 * log<sub>2</sub>(10) + log<sub>2</sub>(mant)<br>
     * @param exp10 decimal exponent
     * @param mantissa array of longs containing decimal mantissa (divided by 10)
     * @return found value of binary exponent
     */
    private static long findBinaryExponent(long exp10, long[] mantissa) {
      final long mant10 =  mantissa[0] << 31 | mantissa[1] >>> 1;  // Higher 63 bits of the mantissa, in range
                                                            // 0x0CC..CCC -- 0x7FF..FFF (2^63/10 -- 2^63-1)
      final double mant10d = mant10 / TWO_POW_63_DIV_10;           // decimal value of the mantissa in range 1.0..9.9999...
      return (long) Math.floor( (exp10) * LOG2_10 + log2(mant10d) ); // Binary exponent
    } // private static long findBinaryExponent(long exp10, long[] mantissa) {

    /**
     * Finds binary mantissa based on the given decimal mantissa and binary exponent,
     * <pre>mant<sub>2</sub> = mant<sub>10</sub> * 10^exp<sub>10</sub> / 2^exp<sub>2</sub></pre>
     * Assigns the found mantissa value to the owner's fields mantHi, mantLo and sets its binary exponent.
     * <br>uses static arrays
     * <b><i>BUFFER_4x64_B, BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_12x32</i></b>,
     * @param exp10 decimal exponent from the source string
     * @param exp2 binary mantissa found from decimal mantissa
     * @param mantissa a buffer containing unpacked quasidecimal mantissa (6 x 32 bits)
     */
    private static void findBinaryMantissa(int exp10, long exp2, long[] mantissa) {
      final long[] powerOf2 = powerOfTwo(-exp2);  // pow(2, -exp2): division by 2^exp2 is multiplication by 2^(-exp2) actually
      long[] product = BUFFER_12x32;          // use it for the product (M * 10^E / 2^e)
      product = multUnpacked6x32bydPacked(mantissa, powerOf2, product); // product in buff_12x32
      multBuffBy10(product);                  // "Quasidecimals" are numbers divided by 10

      if (powerOf2[0] != -exp10)
        multBuffBy10(product);                // For some combinations of exp2 and exp10, additional multiplication needed (see mant2_from_M_E_e.xls)

      exp2 += normalizeMant(product);         // compensate possible inaccuracy of logarithms used to compute exp2
      exp2 += EXPONENT_BIAS;                     // add bias

      // For subnormal values, exp2 <= 0
      if (exp2 <= 0) {                        // subnormal
        // Don't round subnormals up, makeSubnormal will round them,
        // unless bits 129..191 of the product >= 0xFFFF_FFFF_FFFF_FFE0L
        // (i.e. unless the fractional part of the mantissa >= (0.5 - 1.7e-18))

        if (product[4] == 0xFFFF_FFFFL        // 20.11.24 17:34:48 Max. deviation for subnormals seems to be 1e-18
            && (product[5] & 0xFFFF_FFE0L) == 0xFFFF_FFE0L) // && (product[5] & 0xFFFF_FF00L) == 0xFFFF_FF00L)
          exp2+= roundUp(product);            // round up, may require exponent correction

        fillOwnerMantissaFrom(product);       // 128 bits mantHi, mantLo from unpacked buffer
        if (exp2 <= 0)
          exp2 = owner.makeSubnormal(exp2);   // Shift to the right by exp2 bits, may correct exp2 in case of rounding up
      } else {
        exp2+= roundUp(product);              // round up, may require exponent correction
        fillOwnerMantissaFrom(product);
      }

      owner.exponent = (int)exp2;
      if (owner.exponent == EXPONENT_OF_INFINITY) // Infinity
        owner.mantHi = owner.mantLo = 0;
    } // private static void findMant2(int exp10, long exp2, long[] mantissa) {

    /**
     * 20.10.02 13:16:54 Was used for debugging
     * @param indent
     * @param exp2
     * @param product
     */
    @SuppressWarnings("unused")
    private static void logExpAndMant(boolean indent, long exp2, long[] product) {
//      final String prefix = indent? "\t\t\t\t\t\t" : "";
//      log_(prefix + "exp2:\t" + hexStr((int)exp2));
//      log("\tmant:\t" + hexStr((int)product[5]) + "\t = \t" + (int)product[5]);
//      say_("exp2:\t" + hexStr((int)exp2));
//      say("\tmant:\t" + hexStr((int)product[5]) + "\t = \t" + (int)product[5]);
    }

    /**
     * Multiplies unpacked 192-bit value by a packed 192-bit factor
     * <br>uses static arrays
     * <b><i>BUFFER_6x32_B</i></b>
     * @param factor1 a buffer containing unpacked quasidecimal mantissa (6 x 32 bits)
     * @param factor2 an array of 4 longs containing packed quasidecimal power of two
     * @param product a buffer of at least 12 longs to hold the product
     * @return an unpacked (with 32 bits used only) value of 384 bits of the product put in the {@code product}
     */
    private static long[] multUnpacked6x32bydPacked(long[] factor1, long[] factor2, long[] product) {
      Arrays.fill(product, 0);
      unpack_3x64_to_6x32(factor2, BUFFER_6x32_B); // It's the powerOf2, with exponent in 0'th word
      factor2 = BUFFER_6x32_B;

      final int maxFactIdx = factor1.length - 1;

      for (int i = maxFactIdx; i >= 0; i--) // compute partial 32-bit products
        for (int j = maxFactIdx; j >= 0; j--) {
          final long part = factor1[i] * factor2[j];
          product[j + i + 1] += part & LOWER_32_BITS;
          product[j + i] += part >>> 32;
        }

      for (int i = 11; i > 0; i--) { // Carry higher bits of the product to the lower bits of the next word
        product[i - 1] += product[i] >>> 32;
        product[i] &= LOWER_32_BITS;
      }

      return product;
    } // private static long[] multUnpacked6x32bydPacked(long[] factor1, long[] factor2, long[] product) {


    /**
     * Fills the mantissa of the owner with the higher 128 bits of the buffer
     * @param mantissa a buffer containing unpacked mantissa (n longs, where only lower 32 bits of each word are used)
     */
    private static void fillOwnerMantissaFrom(long[] mantissa) {
      owner.mantHi = (mantissa[0]   << 32) + mantissa[1];
      owner.mantLo = (mantissa[2]   << 32) + mantissa[3];
    } // private static void fillOwnerMantissaFrom(long[] mantissa) {

    /***
     * Makes sure that the (unpacked) mantissa is normalized,
     * i.e. buff[0] contains 1 in bit 32 (the implied integer part) and higher 32 of mantissa in bits 31..0,
     * and buff[1]..buff[4] contain other 96 bits of mantissa in their lower halves:
     * <pre>0x0000_0001_XXXX_XXXXL, 0x0000_0000_XXXX_XXXXL...</pre>
     * If necessary, divides the mantissa by appropriate power of 2 to make it normal.
     * @param mantissa a buffer containing unpacked mantissa
     * @return if the mantissa was not normal initially, a correction that should be added to the result's exponent, or 0 otherwise
     */
    private static int normalizeMant(long[] mantissa) {
      final int expCorr = 31 - Long.numberOfLeadingZeros(mantissa[0]);
      if (expCorr != 0)
        divBuffByPower2(mantissa, expCorr);
      return expCorr;
    } // private static int normalizeMant(long[] mantissa) {

    /**
     * Rounds up the contents of the unpacked buffer to 128 bits
     * by adding unity one bit lower than the lowest of these 128 bits.
     * If carry propagates up to bit 33 of buff[0], shifts the buffer rightwards
     * to keep it normalized.
     * @param mantissa the buffer to get rounded
     * @return 1 if the buffer was shifted, 0 otherwise
     */
    private static int roundUp(long[] mantissa) {
      // due to the limited precision of the power of 2, a number with exactly half LSB in its mantissa
      // (i.e that would have 0x8000_0000_0000_0000L in bits 128..191 if it were computed precisely),
      // after multiplication by this power of 2, may get erroneous bits 185..191 (counting from the MSB),
      // taking a value from
      // 0xXXXX_XXXX_XXXX_XXXXL 0xXXXX_XXXX_XXXX_XXXXL 0x7FFF_FFFF_FFFF_FFD8L.
      // to
      // 0xXXXX_XXXX_XXXX_XXXXL 0xXXXX_XXXX_XXXX_XXXXL 0x8000_0000_0000_0014L, or something alike.
      // To round it up, we first add
      // 0x0000_0000_0000_0000L 0x0000_0000_0000_0000L 0x0000_0000_0000_0028L, to turn it into
      // 0xXXXX_XXXX_XXXX_XXXXL 0xXXXX_XXXX_XXXX_XXXXL 0x8000_0000_0000_00XXL,
      // and then add
      // 0x0000_0000_0000_0000L 0x0000_0000_0000_0000L 0x8000_0000_0000_0000L, to provide carry to higher bits.

    addToBuff(mantissa, 5, 100);               // to compensate possible inaccuracy
    addToBuff(mantissa, 4, 0x8000_0000L);               // round-up, if bits 128..159 >= 0x8000_0000L
    if ((mantissa[0] & (HIGHER_32_BITS << 1)) != 0) {   // carry's got propagated beyond the highest bit
      divBuffByPower2(mantissa, 1);
      return 1;
    }
    return 0;
    } // private static int roundUp(long[] mantissa) {

    /**
     * Divides the contents of the buffer by 2^exp2<br>
     * (shifts the buffer rightwards by exp2 if the exp2 is positive, and leftwards if it's negative),
     * keeping it unpacked (only lower 32 bits of each element are used, except the buff[0]
     * whose higher half is intended to contain integer part)
     * @param buffer the buffer to divide
     * @param exp2 the exponent of the power of two to divide by, expected to be
     */
    private static void divBuffByPower2(long[] buffer, int exp2) {
      final int maxIdx = buffer.length - 1;
      final int backShift = 32 - Math.abs(exp2);

      if (exp2 > 0) { // Shift to the right
        for (int i = maxIdx; i > 0; i--)
          buffer[i] =   (buffer[i] >>> exp2)
                        | ((buffer[i - 1] << backShift) & LOWER_32_BITS);
        buffer[0] = (buffer[0] >>> exp2) ;    // Preserve the high half of buff[0]
      } else if (exp2 < 0) { // Shift to the left
        exp2 = -exp2;
        buffer[0] =   (buffer[0] << exp2) | (buffer[1] >> backShift); // Preserve the high half of buff[0]
        for (int i = 1; i < maxIdx; i++)
          buffer[i] =   ((buffer[i] << exp2) & LOWER_32_BITS)
                        | (buffer[i + 1] >> backShift);
        buffer[maxIdx] = (buffer[maxIdx] << exp2) & LOWER_32_BITS;
      }
    } // private static void divBuffByPower2(long[] buffer, int exp2) {

  } // private static class NumberParser

  /**
   * Adds the summand to the idx'th word of the unpacked value stored in the buffer
   * and propagates carry as necessary
   * @param buff the buffer to add the summand to
   * @param idx  the index of the element to which the summand is to be added
   * @param summand the summand to add to the idx'th element of the buffer
   */
  private static void addToBuff(long[] buff, int idx, long summand) {
    final int maxIdx = idx;
    buff[maxIdx] += summand;                         // Big-endian, the lowest word
    for (int i = maxIdx; i > 0; i--) {              // from the lowest word upwards, except the highest
      if ((buff[i] & HIGHER_32_BITS) != 0) {
        buff[i] &= LOWER_32_BITS;
        buff[i-1]++;
      } else break;
    }
  } // private static void addToBuff(long[] buff, int idx, long summand) {

  /********************************************************************************************
   *** Assignments (private) ******************************************************************
   ********************************************************************************************/

  protected void ____Assignments_private____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Assigns the value of {@code Quadruple.MIN_NORMAL}
   * (2^-2147483646 = 2.27064621040149253752656726517958758124747730e-646456993)
   * to the instance.
   * @return this instance with the new value
   */
  private Quadruple assignMinNormal() {
    negative = false; exponent = 1; mantHi = 0; mantLo = 0;
    return this;
  } // private Quadruple assignMinNormal() {

  /**
   * Assigns the value of {@code Quadruple.MAX_VALUE}
   * ({@code 2^2147483647 * (2 - 2^-128)} = 1.76161305168396335320749314979184028566452310e+646456993)
   * to the instance.
   * @return this instance with the new value
   */
  private Quadruple assignMaxValue() {
    negative = false;
    exponent = (int)EXPONENT_OF_MAX_VALUE;
    mantHi = mantLo = -1;
    return this;
  } // private Quadruple assignMaxValue() {

  /**
   * Assigns the value of {@code Quadruple.MIN_VALUE)
   * (2^-2147483774 = 6.67282948260747430814835377499134611597699952e-646457032)
   * to the instance.
   * @return this instance with the new value
   */
  private Quadruple assignMinValue() {
    negative = false; exponent = 0; mantHi = 0; mantLo = 1;
    return this;
  } // private Quadruple assignMinValue() {

  /**
   * Assigns the value of zero to this instance with or without inverting its sign.
   * @param changeSign if {@code true}, the instance will change its sign, if {@code false}, the sign is not changed.
   * @return this instance with the new value (+0 or -0)
   */
  private Quadruple assignZero(boolean changeSign) {
    negative ^= changeSign;
    mantHi = mantLo = exponent = 0;
    return this;
  } // private Quadruple assignZero(boolean changeSign) {

  /**
   * Assigns the value of +0 to this instance.
   * @return this instance with the new value (+0)
   */
  private Quadruple assignZero() {
    negative = false;
    mantHi = mantLo = exponent = 0;
    return this;
  } // private Quadruple assignZero() {

  /**
   * Assigns the value of -0 to this instance.
   * @return this instance with the new value (-0)
   */
  @SuppressWarnings("unused") // May occur helpful?
  private Quadruple assignMinusZero() {
    negative = true;
    mantHi = mantLo = exponent = 0;
    return this;
  } // private Quadruple assignZero() {

  /**
   * Assigns the value of +1 or -1 to this instance,
   * depending on the sign of the previous value of the instance and the {@code changeSign} parameter.
   * @param changeSign if {@code true}, the instance will change its sign, if {@code false}, the sign is not changed.
   * @return this instance with the new value (+1 or -1)
   */
  private Quadruple assignOne(boolean changeSign) {
    negative ^= changeSign;
    exponent = EXPONENT_OF_ONE;
    mantHi = 0; mantLo = 0;
    return this;
  } // private Quadruple assignOne(boolean changeSign) {

  /**
   * Assigns the value of Infinity or -Infinity,
   * depending on the sign of the previous value of the instance and the {@code changeSign} parameter.
   * @param changeSign if {@code true}, the instance will change its sign, if {@code false}, the sign is not changed.
   * @return this instance with the new value (Infinity or -Infinity)
   */
  private Quadruple assignInfinity(boolean changeSign) {
    negative ^= changeSign;
    exponent = EXPONENT_OF_INFINITY;
    mantHi = 0; mantLo = 0;
    return this;
  } // private Quadruple assignInfinity(boolean changeSign) {

  /********************************************************************************************
   *** Used_by_conversions ********************************************************************
   ********************************************************************************************/

  protected void ____Used_by_conversions____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /**
   * Builds a {@code BigDecimal} containing the value of the mantissa
   * (in the range 1.0 -- 1.9999... for normal values or less for subnormal values)
   * @return a {@code BigDecimal} containing the value of the mantissa
   */
  private BigDecimal buildBDMantissa() {
    BigDecimal mant = new BigDecimal(Long.toUnsignedString(mantLo));
    mant = mant.divide(TWO_RAISED_TO_64).add(new BigDecimal(Long.toUnsignedString(mantHi)));

    if (exponent == 0) // subnormal
      mant = mant.divide(TWO_RAISED_TO_63);                        // No Implied "1.", just divide by 2^63
    else
      mant = BigDecimal.ONE.add(mant.divide(TWO_RAISED_TO_64));    // + Implied "1.", and now we have the mantissa
    return mant;
  } // private BigDecimal buildBDMantissa() {

  /**
   * Throws a {@code NumberFormatException} with an appropriate message
   * if we attempt to get a {@code BigDecimal} value of {@code NaN} or {@code Infinity}
   */
  private void checkNaNInfinity() {
    if (exponent == EXPONENT_OF_INFINITY) {
      throw new NumberFormatException("Can't convert "
                  + (((mantHi | mantLo) != 0)? "NaN" :
                       negative? "NEGATIVE_INFINITY" : "POSITIVE_INFINITY")
                  + " to BigDecimal");
    }
  } // private void checkNaNInfinity() {

  /**
   * Converts the value of {@code this} instance to the value of the nearest IEEE-754
   * quadruple-precision number, represented as an array of two {@code long}s,
   * containing the 128 bits of the IEEE-754 quadruple, in the big-endian order,
   * for the case when the value of this instance falls in the range of normal IEEE-754 quadruple values.
   * @param result -- the array of 2 longs that is to be filled with the 128 bits of the resulting IEEE-754 quadruple
   * @return the same input array filled with the bits of the resulting IEEE-754 quadruple
   */
  private long[] makeNormal_IEEELongs(long[] result) {
    int unbiasedExponent = unbiasedExponent();
    final long carry = (mantLo >>> 15) & 1L;              // The shifted-out bit
    long ieeeMantLo = (mantLo >>> 16) | (mantHi << 48);   // Low-order 64 bits of the mantissa of IEEE-754 Quadruple
    long ieeeMantHi = mantHi >>> 16;                      // High-order 48 bits

    ieeeMantLo += carry;
    if (carry != 0 && ieeeMantLo == 0) {                  // Propagate carry
      if (++ieeeMantHi == 0x0001_0000_0000_0000L) {       // Mantissa overflow, need to adjust exponent
        unbiasedExponent++;
        ieeeMantHi  &= 0xFFFF_FFFF_FFFFL;                 // Clear this bit
      }
    }

    if (unbiasedExponent > IEEE754_MAX_EXPONENT) {        // Overflow, return infinity
      result[0] = negative ?  IEEE754_MINUS_INFINITY_LEAD :
                              IEEE754_INFINITY_LEAD;
      return result;
    }

    // Mantissa:
    result[1] = ieeeMantLo;
    result[0] = ieeeMantHi;
    // Exponent:
    final long biasedExponent = unbiasedExponent + IEEE754_EXPONENT_BIAS;
    result[0] |= biasedExponent << 48;                    // To high-order 16 bits (the highest remains 0)
    // Sign:
    if (negative)
      result[0] |= IEEE754_MINUS_ZERO_LEAD;               // sign bit

    return result;
  } // private long[] makeNormal_IEEELongs(long[] result) {

  /**
   * Converts the value of {@code this} instance to the value of the nearest IEEE-754
   * quadruple-precision number, represented as an array of two {@code long}s,
   * containing the 128 bits of the IEEE-754 quadruple, in the big-endian order,
   * for the case when the value of this instance falls in the range of subnormal IEEE-754 quadruple values.
   * @param result -- the array of 2 longs that is to be filled with the 128 bits of the resulting IEEE-754 quadruple
   * @return the same input array filled with the bits of the resulting IEEE-754 quadruple
   */
  private long[] makeSubnormal_IEEELongs(long[] result) {
    final int unbiasedExponent = unbiasedExponent();
    long ieeeMantLo = (mantLo >>> 16) | (mantHi << 48);   // Low-order 64 bits of the mantissa of IEEE-754 Quadruple
    // High-order 48 bits + implicit lead bit which is getting explicit for subnormals:
    long ieeeMantHi = (mantHi >>> 16) | 0x0001_0000_0000_0000L;

    final int shift =  IEEE754_MIN_NORMAL_EXPONENT - unbiasedExponent;
    final long shiftedOutBit = shift > 64?  (ieeeMantHi >>> (shift - 65)) & 1L:
                                            (ieeeMantLo >>> (shift - 1)) & 1L;
    if (shift >= 64) {
      ieeeMantLo = ieeeMantHi >>> (shift - 64);
      ieeeMantHi = 0;
    } else {
      ieeeMantLo = (ieeeMantLo >>> shift) | (ieeeMantHi << 64 - shift);
      ieeeMantHi = ieeeMantHi >>> shift;
    }
    ieeeMantLo += shiftedOutBit;
    if (shiftedOutBit != 0 && ieeeMantLo == 0)
      ieeeMantHi++;                           // If carry propagates to bit 0x0001_000.., it becomes the exponent, and that's all right
                                              // A subnormal becomes min normal
    result[1] = ieeeMantLo;
    result[0] = ieeeMantHi;                   //
    // Sign:
    if (negative)
      result[0] |= IEEE754_MINUS_ZERO_LEAD;           // sign bit

    return result;
  } // private long[] makeSubnormal_IEEELongs(long[] result) {


  /**
   * Splits the {@code long} items of the given array into bytes and returns
   * an array of bytes containing the bits of the original longs, in the big-endian order.
   * The most significant bit of {@code longs[0]} becomes the MSB of result[0], etc.
   * @param longs an array of longs to be split
   * @return an array of resulting bytes
   */
  private static byte[] splitToBytes(long[] longs) {
    final byte[] bytes = new byte[longs.length * 8];
    for (int i = 0; i < longs.length; i++) {
      long currentLong = longs[i];
      for (int j = 7; j >= 0; j--) {
        bytes[i * 8 + j] = (byte) currentLong;
        currentLong >>>= 8;
      }
    }
    return bytes;
  } // private static byte[] splitToBytes(long[] longs) {

  /**
   * Merges the {@code byte} items of the given array into longs, 8 bytes per each long, and returns
   * an array of longs containing the bits of the original bytes, in the big-endian order.
   * The most significant bit of {@code bytes[0]} becomes the MSB of result[0], etc.
   * @param longs an array of longs to be split
   * @return an array of resulting bytes
   */
  private static long[] mergeBytesToLongs(byte[] bytes) {
    final long[] longs = new long[bytes.length / 8];
    for (int i = 0; i < longs.length; i++) {
      for (int j = 0; j < 8; j++) {
        longs[i] = (longs[i] << 8) | (bytes[i * 8 + j]) & 0xFFL;
      }
    }
    return longs;
  } // private static long[] mergeBytesToLongs(byte[] bytes) {

  /********************************************************************************************
   *** Used_by_arithmetic *********************************************************************
   ********************************************************************************************/

  protected void ____Used_by_arithmetic____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /* **********************************************************************************
   * Used by addition *****************************************************************
   ********************************************************************************** */

  /**
   * Adds a regular number (not NaN, not Infinity) to this instance, that also contains a regular number.
   * The signs are ignored and don't change (both summands are expected to have the same sign).
   * @param summand a Quadruple to add to this instance
   * @return this instance with the new value (the sum of the two summands)
   */
  private Quadruple addUnsigned(Quadruple summand) {
    if (exponent != 0 && summand.exponent != 0) {   // Both are normal numbers
      if (exponent == summand.exponent)             // Same exponent
        return addWithSameExps(summand);
      return addWitDifferentExps(summand);          // Different exponents
    }
                                                    // At least one of the summands is subnormal
    if ((exponent | summand.exponent) != 0)         // And one is normal
      return addNormalAndSubnormal(summand);

    // Both are subnormals. It's the simplest case
    exponent = (int)addMant(summand.mantHi, summand.mantLo);  // if there was carry (to the implicit unity),
                                                              // it becomes normal this way
    return this;                                              // otherwise it remains subnormal
  } // private Quadruple addUnsigned(Quadruple summand) {

  /**
   * Adds a summand to this instance in case when both summands are normal
   * and have the same exponent
   * @param summand a Quadruple to add to this instance
   * @return this instance with the new value (the sum of the two summands)
   */
  private Quadruple addWithSameExps(Quadruple summand) {
    final long carryUp = addMant(summand.mantHi, summand.mantLo);
    final long shiftedOutBit = mantLo & 1;           // the bit that will be shifted out
    shiftMantissaRight(1);

    if (shiftedOutBit != 0 && ++mantLo == 0)    // Carry to the higher word
      mantHi++;

    if (carryUp != 0)  mantHi |= BIT_63;        // Set the highest bit (where the carry should get)
    if (++exponent == EXPONENT_OF_INFINITY)                  // Infinity
      mantHi = mantLo = 0;
    return this;
  } // private Quadruple addWithSameExps(Quadruple summand) {

  /**
   * Adds a summand to this instance
   * in case when both summands and this are normal and have different exponents
   * @param summand a Quadruple to add to this instance
   * @return this instance with the new value (the sum of the two summands)
   */
  private Quadruple addWitDifferentExps(Quadruple summand) {
    long greaterHi, greaterLo, exp2;

    // Put the mantissa of the lesser summand, that is to be shifted, to the fields of this instance
    if (Integer.compareUnsigned(exponent, summand.exponent) < 0) { // Value of this is less than the value of the other
      greaterHi = summand.mantHi; greaterLo = summand.mantLo;    // mantissa of the greatest to be added
      exp2 = exponent;                                  // the exponent of the lesser
      exponent = summand.exponent;                      // Copy the exponent of the greater value to this
    } else {
      greaterHi = mantHi; greaterLo = mantLo;           // mantissa of the greatest to be added
      mantHi = summand.mantHi; mantLo = summand.mantLo; // mantissa of the lesser to be shifted
      exp2 = summand.exponent;                          // the exponent of the lesser
    }

    final int shift = exponent - (int)exp2;
    if (Integer.compareUnsigned(shift, 129) > 0) {      // Implied higher unity of the lesser will be two positions farther than bit 0
      mantHi = greaterHi; mantLo = greaterLo;           // The lesser summand is too small to affect the result
      return this;
    }

    if (shift == 129)                                   // The implied unity of the lesser is added to the greater
      return greaterPlusLowerBit(greaterHi, greaterLo);

    final long shiftedOutBit = shiftAndSetUnity(shift);        // shifts right and adds the implicit unity of the lesser
    final long carryUp = addAndRoundUp(greaterHi, greaterLo, shiftedOutBit);
    if (carryUp != 0)                                   // Overflow, shift 1 bit right
      shiftAndCorrectExponent(shiftedOutBit);           // shiftedOutBit flags that it was rounded up already

    return this;
  } // private Quadruple addWitDifferentExps(Quadruple summand) {

  /**
   * Adds a summand to this instance in case when exactly one of the summands is subnormal
   * @param summand a Quadruple to add to this instance
   * @return this instance with the new value (the sum of the two summands)
   */
  private Quadruple addNormalAndSubnormal(Quadruple summand) {
    long greaterHi; long greaterLo; long shiftedOutBit;
    // Put the subnormal mantissa, that will be shifted, into the instance fields,
    // the mantissa of the greater (normal) value into local variables,
    // And the exponent of the normal value in the exponent field of this
    if (exponent == 0) {                                // This is subnormal
      greaterHi = summand.mantHi; greaterLo = summand.mantLo;   // Normal value to be added to this
      exponent = summand.exponent;                      // Copy the exponent of the larger value to this
    } else {                                            // The other is subnormal
      greaterHi = mantHi; greaterLo = mantLo;           // Normal mantissa to be added
      mantHi = summand.mantHi; mantLo = summand.mantLo; // Subnormal mantissa to be shifted
    }

    final int shift = exponent - 1;                     // How far should we shift subnormal mantissa
    int lz = Long.numberOfLeadingZeros(mantHi);         // Leading zeros in the subnormal value
    if (lz == 64) lz = 64 + Long.numberOfLeadingZeros(mantLo);
    if (shift + lz > 128)  {                            // Subnormal is too small to affect the result
      mantHi = greaterHi; mantLo = greaterLo;
      return this;
    }

    shiftedOutBit = highestShiftedOutBit(shift);        // Shift the lesser summand rightwards
    shiftMantissaRight(shift);                          // Here we don't need to add the implicit 1
    final long carryUp = addAndRoundUp(greaterHi, greaterLo, shiftedOutBit);

    if (carryUp != 0)                                   // Overflow, shift 1 bit right
      shiftAndCorrectExponent(shiftedOutBit);

    return this;
  } // private Quadruple addNormalAndSubnormal(Quadruple summand) {

  /**
   * Adds the given 128-bit value to the mantissa of this instance
   * @param mantHi2 the higher 64 bits of the 128-bit summand to be added
   * @param mantLo2 the lower 64 bits of the 128-bit summand to be added
   * @return the carry (1 if the addition has resulted in overflow, 0 otherwise)
   */
  private long addMant(long mantHi2, long mantLo2) {
    mantLo += mantLo2;
    long carry = Long.compareUnsigned(mantLo, mantLo2) < 0? 1: 0;
    if (carry != 0 && (mantHi += carry) == 0) {
      mantHi = mantHi2;
      carry = 1;
    } else {
      mantHi += mantHi2;
      carry = Long.compareUnsigned(mantHi, mantHi2) < 0? 1: 0;
    }
    return carry;
  } // private long addMant(long mantHi2, long mantLo2) {

  /**
   * Shifts the mantissa of this instance by {@code shift} bits to the right,
   * without setting the implicit unity,
   * and returns the bits of the former mantissa that dont't fit in 128 bits after the shift (shifted-out bits).
   * (e.g. if the value of {@code shift} was 3, the lowest 3 bits of {@code mantLo} will be returned in bits 63-61 of the result, the other bits will be 0)
   * @param shift the distance to shift the mantissa
   * @return the bits of mantissa that was shifted out
   */
  private long shiftMantissaRight(int shift) {
    long shiftedOutBits;
    if (shift == 0) return 0;

    if (shift == 128) {
      shiftedOutBits = mantHi;
      mantHi = mantLo = 0;
      return shiftedOutBits;
    }

    shiftedOutBits = (shift <= 64) ?
        (mantLo << 64 - shift) :
        (mantHi << 128 - shift | mantLo >>> shift - 64);

    if (shift >= 64) {
      mantLo = mantHi >>> shift - 64;
      mantHi = 0;
    } else {
      mantLo = mantLo >>> shift | mantHi << (64 - shift);
      mantHi = mantHi >>> shift;
    }
    return shiftedOutBits;
  } // private void shiftMantissaRight(int shift) {

  /**
   * Increments the value passed in the parameters and assigns it to the mantissa.
   * If the mantissa becomes 0 (that indicates its overflow), increments the exponent.
   * @param greaterHi the upper 64 bits of the given value
   * @param greaterLo the lower 64 bits of the given value
   * @return this instance with the new value
   */
  private Quadruple greaterPlusLowerBit(long greaterHi, long greaterLo) {
    if ((mantLo = ++greaterLo) == 0) {
      if ((mantHi = ++greaterHi) == 0)
        exponent++;                    // If it becomes infinity, the mantissa is already 0
    } else
      mantHi = greaterHi;
    return this;
  } // private Quadruple greaterPlusLowerBit(long greaterHi, long greaterLo) {

  /**
   * Shifts the mantissa of this instance by {@code shift} bits right
   * and sets the implicit unity in the correspondent position
   * @param shift the distance to shift the mantissa
   * @return the value of the highest bit that was shifted out
   */
  private long shiftAndSetUnity(int shift) {
    final long shiftedOutBit = (shift == 0)? 0:               // Keep in mind the highest bit that will be shifted out
                             (shift <= 64)?  1 & (mantLo >>> shift - 1) :
                                             1 & (mantHi >>> shift - 65);
    shiftMantissaRight(shift);                          // Shift the mantissa

    if (shift > 64)   mantLo |= 1L << (128 - shift);    // Set implicit unity (bit 129) of the lesser
    else               mantHi |= 1L << (64 - shift);    // as if it was shifted from beyond the mantHi

    return shiftedOutBit;                               // Return the highest shifted-out bit
  } // private long shiftAndSetUnity(int shift) {

  /**
   * Adds the given 128-bit value to the mantissa, taking into account the carry from
   * the lower part of the summand (that may have been shifted out beforehand)
   * @param summandHi the higher part of the 128-bit summand
   * @param summandLo the lower part of the 128-bit summand
   * @param carry the carry from the lower bits (may be 0 or 1)
   * @return the carry (1 if the addition has led to overflow, 0 otherwise)
   */
  private long addAndRoundUp(long summandHi, long summandLo, long carry) {
    if (carry != 0 && (mantLo += carry) == 0) {
      mantLo = summandLo;
      carry = 1;
    } else {
      mantLo += summandLo;
      carry = Long.compareUnsigned(mantLo, summandLo) < 0? 1: 0;
    }
    if (carry != 0 && (mantHi += carry) == 0) {
      mantHi = summandHi;
      carry = 1;
    } else {
      mantHi += summandHi;
      carry = Long.compareUnsigned(mantHi, summandHi) < 0? 1: 0;
    }
    return carry;
  } // private long addAndRoundUp(long summandHi, long summandLo, long carry) {

  /**
   * Shifts the mantissa one bit right and rounds it up (unless rounding is forbidden by non-null value
   * of the {@code dontRoundUpAnyMore} parameter) and increments the exponent.
   * If the exponent becomes equal to {@code EXPONENT_OF_INFINITY}, clears the mantissa to force Infinity.
   * @param dontRoundUpAnyMore non-zero value
   */
  private void shiftAndCorrectExponent(long dontRoundUpAnyMore) {
    final long shiftedOutBit = dontRoundUpAnyMore != 0? 0: (mantLo & 1);    // the lowest bit to be shifted out (if was not rounded yet)
    shiftMantissaRight(1);                              // after that, the highest bit is always 0
    if (shiftedOutBit != 0)                             // Don't round up if already
      addMant(0, 1);                                    // so carry after this addition is impossible
    if (++exponent == EXPONENT_OF_INFINITY)                          // Infinity
      mantHi = mantLo = 0;
  } // private void shiftAndCorrectExponent(long dontRoundUpAnyMore) {

  /**
   * Returns the highest bit of the mantissa that will be shifter out
   * during shift right by {@code shift} bits
   * @param shift the distance the value will be shifted
   * @return 1, if the highest shifted-out bit is 1, 0 otherwise
   */
  private long highestShiftedOutBit(int shift) {
    if (shift == 0) return 0;
    if (shift <= 64) return 1 & (mantLo >>> shift - 1);
    return 1 & (mantHi >>> shift - 65);
  } // private long highestShiftedOutBit(int shift) {

  /* **********************************************************************************
   * Used by subtraction **************************************************************
   ********************************************************************************** */

  /**
   * Subtracts a regular number (Non-NaN, non-infinity) from this instance, ignoring sings.
   * returns a negative value of the difference if the subtrahend is greater in magnitude than the minuend (this),
   * and a positive one otherwise.
   * @param subtrahend the value to be subtracted
   * @return this instance, with the new value that equals the difference
   */
  private Quadruple subtractUnsigned(Quadruple subtrahend) {
    long minuendLo, minuendHi;
    int lesserExp;

    final int thisIsGreater = compareMagnitudeTo(subtrahend); // ignores signs
    if (thisIsGreater == 0)                             // operands are equal in magnitude
      return assignZero(false);

    // Swap minuend and subtrahend, if minuend is less in magnitude than subtrahend
    // so that this.mantHi, this.mantLo and lesserExp contain respectively mantissa and exponent
    // of the subtrahend (the lesser of the operands),
    // and minuendHi, minuendLo and this.exponent contain ones of the minuend (the greater one)
    if (thisIsGreater > 0) {
      minuendLo = mantLo; minuendHi = mantHi;           // mantissa of the greater
      mantHi = subtrahend.mantHi; mantLo = subtrahend.mantLo; // mantissa of the lesser to be shifted rightwards
      lesserExp = subtrahend.exponent;
      negative = false;                                 // minuend is greater than subtrahend
    } else {                                            // Subtrahend is greater in magnitude
      minuendLo = subtrahend.mantLo; minuendHi = subtrahend.mantHi; // mantissa of the greater
      lesserExp = exponent;
      exponent = subtrahend.exponent;                   // may remain unchanged
      negative = true;                                  // minuend is less than subtrahend
    }

    if (exponent != 0 && lesserExp != 0)                // Both are normal
      return subtractNormals(minuendLo, minuendHi, lesserExp);

    if ((exponent | lesserExp) == 0)                    // both are subnormal
      return subtractSubnormals(minuendLo, minuendHi);

    return subtractSubnormalFromNormal(minuendLo, minuendHi); // the lesser is subnormal
  } // private Quadruple subtractUnsigned(Quadruple subtrahend) {

  /**
   * Subtracts a normal value, whose mantissa is contained by this
   * instance and exponent is passed in as the {@code lesserExp} parameter, from another
   * normal value, whose mantissa is contained in {@code minuendLo} and {@code minuendLo}
   * parameters and exponent is in the {@code exponent} field of this instance.
   * @param minuendLo the lower 64 bits of the minuend
   * @param minuendHi the higher 64 bits of the minuend
   * @param lesserExp  the exponent of the subtrahend
   * @return  this instance with a new value that equals the difference
   * <br>Covered
   */
  private Quadruple subtractNormals(long minuendLo, long minuendHi, int lesserExp) {
    final int shift = exponent - lesserExp;             // The distance to shift the mantissa of the subtrahend
    if (shift > 130)  {                                 // The subtrahend is too small to affect the result
      mantHi = minuendHi; mantLo = minuendLo;
      return this;
    }

    if (shift == 130)  // the result differs from the minuend in the only case when minuend is 2^n (1.00..00 * 2^n)
      return subtract_1e_130(minuendLo, minuendHi);     // and subtrahend is greater than 2^(n-130).

    if (shift == 129)
      return subtract_1e_129(minuendLo, minuendHi);     // subtracts LSB if subtrahend > LSB

    if (shift != 0) // 0 < shift < 129, different exponents
      return subtractDifferentExp(minuendLo, minuendHi, shift);

    // same exponent
    return subtractSameExp(minuendLo, minuendHi);
  } // private Quadruple subtractNormals(long minuendLo, long minuendHi, int lesserExp) {

  /** Subtracts a value whose exponent is lower than the exponent of the minuend by 130.
   * The only case when the subtrahend affects the result is
   * when the minuend is 2^n and the subtrahend is greater than 2^(n-130).
   * The result is 1.FF..FF * 2^(n-1) in this case, otherwise it equals the minuend.
   * The result is assigned to this instance.
   * @param minuendLo the lower 64 bits of the minuend
   * @param minuendHi the higher 64 bits of the minuend
   * @return this instance, containing the result
   * <br>Covered
   */
  private Quadruple subtract_1e_130(long minuendLo, long minuendHi) {
    if ((mantHi | mantLo) != 0 && (minuendLo | minuendHi) == 0) {// 0b1_00...00.0000 - 0.01xx, where one of the x's != 0, == 0_11..11.10
      mantHi = mantLo = -1;
      exponent--;
    } else {
      mantHi = minuendHi; mantLo = minuendLo;
    }
    return this;
  } // private Quadruple subtract_1e_130(long minuendLo, long minuendHi) {

  /** Subtracts unity from the LSB of the mantissa of the minuend passed in,
   * if the subtrahend is greater than 1/2 LSB of the minuend.
   * if the minuend == 2^n (1.00..00*2^n), the result is 1.FF..FF * 2^(n-1) in case if the subtrahend <= 3/4 LSB,
   * and 1.FF..FE * 2^(n-1) in case if the subtrahend > 3/4 LSB.
   * Assigns the result to the {@code mantHi, mantLo} fields of this instance
   * @param minuendLo the lower 64 bits of the minuend
   * @param minuendHi the higher 64 bits of the minuend
   * @return this instance, containing the result
   * <br>Covered
   */
  private Quadruple subtract_1e_129(long minuendLo, long minuendHi) {
    final long subtrahendHi = mantHi, subtrahendLo = mantLo;

    if ((minuendHi | minuendLo) == 0) {                 // Borrow from the implicit unity is inevitable (1.000 - 0.001 = 0.FFF
        mantHi = mantLo = -1;                           // Subtrahend is not less then 1/2 LSB, 0b1.00..00 - 0b0.00..00,1 = 0b0.11..11,1 << 1 = 0b1.1111e-1
        if (     ((subtrahendHi & HIGH_BIT) !=0)        // The MSB is 1 and at least one of other bits is 1, e.g. 1.10001... > 1.5e-129
            &&   (((subtrahendHi & ~HIGH_BIT) | subtrahendLo) !=0 ) )  // i.e. subtrahend is greater than 3/4 LSB,
          mantLo--;                                     // in this case mantissa = 1.FF...FE
        exponent--;                                     // The difference can't become subnormal
      } else {
        mantLo = minuendLo; mantHi = minuendHi;
        if ((subtrahendHi | subtrahendLo) != 0)         // Subtrahend is greater than 1/2 LSB (at least one bit except the implicit unity is 1)
          if (--mantLo == -1) mantHi--;                 // Can't underflow since it's not 0 here
      }
    return this;
  } // private Quadruple subtract_1e_129(long minuendLo, long minuendHi) {

  /** Subtracts a normal value, whose mantissa is contained by this instance
   * and exponent is less than exponent of {@code this} by the amount
   * passed in the {@code shift} parameter, from another normal value,
   * whose mantissa is contained in {@code minuendLo} and {@code minuendLo} parameters
   * and exponent is contained in the {@code exponent} field of this instance
   * @param minuendLo the lower 64 bits of the mantissa of the minuend
   * @param minuendHi the higher 64 bits of the mantissa of the minuend
   * @param shift the difference between the exponents
   * @return  this instance with a new value that equals the difference
   * <br>Covered
   */
  private Quadruple subtractDifferentExp(long minuendLo, long minuendHi, int shift) {
    final long shiftedOutBits = shiftMantissaRight(shift);  // Shift the subtrahend's mantissa rightwards to align by bits' values
    setUnity(shift);                                    // The implicit highest unity of the subtrahend
    long borrow =
      Long.compareUnsigned(shiftedOutBits, HIGH_BIT) > 0? 1 : 0; // more than 1/2 of LSB
    borrow = subtrMant(minuendHi, minuendLo, borrow);   // has borrow propagated to the implicit unity?

    if (borrow != 0) {                                  // yes, needs normalization
      if (shift == 1)
        normalizeShiftedByOneBit(shiftedOutBits);       // shiftedOutBits may be MIN_VALUE or 0
      else
        normalizeShiftedByAFewBits(shiftedOutBits);     // shift > 1, highest bit of mantHi is always 1. shift can't be o here
    } else if ((mantHi | mantLo) == 0 && shiftedOutBits == HIGH_BIT) { // 1.0 - 2^129 = 1.FFFF_... * 2^-1
      exponent--; mantHi = mantLo = 0xFFFF_FFFF_FFFF_FFFFL;
    }
    return this;
  } // private Quadruple subtractDifferentExp(long minuendLo, long minuendHi, int shift) {

  /** Normalizes the mantissa after subtraction, in case when subtrahend was shifted right by one bit.
   * There may be zeros in higher bits of the result, so we shift the mantissa left by (leadingZeros + 1) bits
   * and take into account borrow that possibly has to be propagated from the shifted-out bit of the subtrahend.
   * @param shiftedOutBits the bits that were shifted out (in the leftmost position of the parameter)
   * <br>Covered
   */
  private void normalizeShiftedByOneBit(long shiftedOutBits) {
    int lz = numberOfLeadingZeros();
    if (lz == 0) {
      shiftMantLeft(1); exponent--;                     // The implicit unity was zeroed by borrow, exponent was >= 2, now >= 1
      if (shiftedOutBits != 0)                          // Is borrow to be propagated from the shifted-out bit?
        if (--mantLo == -1 && --mantHi == -1) {         // Borrow from the implicit unity
          if (--exponent != 0)                          // has it become subnormal?
            shiftMantLeft(1);                           // no, shift out the implicit unity
        }
    } else {                                            // lz != 0, a few high bits are zeros
      if ((shiftedOutBits != 0)) {                      // Need to take borrow into account
        shiftMantLeft(1); exponent--;
        if (--mantLo == -1) mantHi--;                   // Borrow. mantHi can't become -1, since it's shifted and thus >= 2
        lz = numberOfLeadingZeros();                    // it has changed, so find the new value
      }
      normalize(lz + 1);
    }
  } // private void normalizeShiftedByOneBit(long shiftedOutBits) {

  /**
   * Normalizes the mantissa after subtraction, in case when subtrahend was shifted right
   * by more than one bit. The highest bit of mantHi is always 1 (mantHi & HIGH_BIT != 0),
   * for rounding the result, take into account more shifted-out bits, than just the highest of them.
   * @param shiftedOutBits the bits that were shifted out (in the leftmost position of the parameter)
   * <br>Covered
   */
  private void normalizeShiftedByAFewBits(long shiftedOutBits) {
    shiftMantLeft(1); exponent--;
    if (shiftedOutBits == HIGH_BIT || shiftedOutBits > 0x4000_0000_0000_0000L) {
      if (--mantLo == -1)                               // round down (shifted-out part of subtrahend was .1000.. or > .0100.. )
        mantHi--;                                       // mantHi and mantLo can't be 0 at the same, so it won't get underflowed
    } else
      if (shiftedOutBits <= 0xC000_0000_0000_0000L)     // shifted-out part of subtrahend was > .1000, so it's rounded down,
                                                        // but it was <= .1100.., so it shouldn't have been rounded down
        mantLo |= 1;                                    // then undo rounding down

  } // private void normalizeShiftedByAFewBits(long shiftedOutBits) {

  /**
   * Normalizes the result of subtraction,
   * shifting the mantissa leftwards by {@code shift} bits i case when the result remains normal,
   * or by {@code exponent - 1}, when the result becomes subnormal, and correcting the exponent appropriately.
   * @param shift the distance to shift the mantissa and the amount to decrease the exponent by
   * <br>Covered
   */
  private void normalize(int shift) {
    if (Integer.compareUnsigned(exponent, shift) > 0) { // Remains normal
      shiftMantLeft(shift);
      exponent -= shift;
    } else {                                            // becomes subnormal
      if (exponent > 1)
        shiftMantLeft(exponent - 1);
      exponent = 0;
    }
  } // private void normalize(int shift) {

  /**
   * Calculates the number of leading zeros in the mantissa
   * @return the number of leading zeros in the mantissa
   * <br>Covered
   */
  private int numberOfLeadingZeros() {
    int lz = Long.numberOfLeadingZeros(mantHi);         // Leading zeros in high 64 bits
    if (lz == 64) lz += Long.numberOfLeadingZeros(mantLo);  // if all zeros, add zeros of lower 64 bits
    return lz;
  } // private int numberOfLeadingZeros() {

  /**
   * Subtracts subtrahend, whose mantissa in the {@code mantHi, mantLo} fields,
   * from the minuend passed in the parameters, when the exponents of the subtrahend and the minuend are equal.
   * @param minuendLo
   * @param minuendHi
   * @return
   */
  private Quadruple subtractSameExp(long minuendLo, long minuendHi) {
    mantLo = minuendLo - mantLo;
    if (Long.compareUnsigned(minuendLo, mantLo) < 0)    // borrow
      minuendHi--;
    mantHi = minuendHi - mantHi;                        // Borrow impossible since minuend > subtrahend

    // The implicit unity is always 0 after this subtraction (1.xxx - 1.yyy = 0.zzz), so normalization is always needed
    normalize(numberOfLeadingZeros() + 1);
    return this;
  } // private Quadruple subtractSameExp(long minuendLo, long minuendHi) {

  /**
   * Subtracts a subnormal value, whose mantissa is contained by this instance,
   * from a normal value, whose mantissa is contained in {@code minuendLo} and {@code minuendLo} parameters
   * and exponent is contained in the {@code exponent} field of this instance
   * @param minuendLo the lower 64 bits of the mantissa of the minuend
   * @param minuendHi the higher 64 bits of the mantissa of the minuend
   * @return  this instance with a new value that equals the difference
   */
  private Quadruple subtractSubnormalFromNormal(long minuendLo, long minuendHi) {
    final int shift = exponent - 1;                     // How far should we shift subnormal mantissa
    final int lz = numberOfLeadingZeros();

    if (((shift & 0xFFFF_FF00) != 0) || (shift + lz > 129))  {  // Normal is too great or Subnormal is too small to affect the result
      mantHi = minuendHi; mantLo = minuendLo;
      return this;
    }

    final long shiftedOutBits = shiftMantissaRight(shift);  // Shift the subtrahend's mantissa rightwards to align by bits' values
    long borrow = Long.compareUnsigned(shiftedOutBits, HIGH_BIT) > 0? 1 : 0; // greater than 1/2 of LSB

    borrow = subtrMant(minuendHi, minuendLo, borrow);   // has borrow propagated to the implicit unity?
    if (borrow != 0) {                                  // yes, needs normalization
      if (shift == 1)
        normalizeShiftedByOneBit(shiftedOutBits);       // shiftedOutBits may be MIN_VALUE or 0
      else if (shift != 0)
        normalizeShiftedByAFewBits(shiftedOutBits);     // shift > 1, highest bit of mantHi is always 1
      else exponent = 0;                                // exponent was 1, borrow from implicit unity, becomes subnormal
    } else
      if ((mantHi | mantLo) == 0
        && (shiftedOutBits == HIGH_BIT || shiftedOutBits > 0x4000_0000_0000_0000L) ) { // 1.0 - 2^129 = 1.FFFF_... * 2^-1
        exponent--; mantHi = mantLo = 0xFFFF_FFFF_FFFF_FFFFL;
      }
    return this;
  } // private Quadruple subtractSubnormalFromNormal(long minuendLo, long minuendHi) {

  /**
   * Subtracts a subnormal value, whose mantissa is contained by this instance,
   * from another subnormal value, whose mantissa is contained in {@code minuendLo} and {@code minuendLo} parameters.
   * FYI, exponents of subnormal values are always 0
   * @param minuendLo the lower 64 bits of the mantissa of the minuend
   * @param minuendHi the higher 64 bits of the mantissa of the minuend
   * @return  this instance with a new value that equals the difference
   */
  private Quadruple subtractSubnormals(long minuendLo, long minuendHi) {
    mantLo = minuendLo - mantLo;
    if (Long.compareUnsigned(mantLo, minuendLo) > 0)    // borrow
      minuendHi--;
    mantHi = minuendHi - mantHi;
    return this;
  } // private Quadruple subtractSubnormals(long minuendLo, long minuendHi) {

  /**
   * Sets a bit of the mantissa into 1. The position of the bit to be set is defined by the {@code shift} parameter.
   * The bits are implied to be numbered starting from the highest, from 1 to 128,
   * so that {@code setUnity(1)} sets the MSB of the {@code mantHi} field, and {@code setUnity(128)} sets the LSB of {@code mantLo}
   * @param shift the number of the bit to set, starting from 1, that means the most significant bit of the mantissa
   */
  private void setUnity(int shift) {
    if (shift > 64)
      mantLo |= 1L << 128 - shift;
    else
      mantHi |= 1L << 64 - shift;
  } // private void setUnity(int shift) {

  /** Shifts the mantissa leftwards by {@code shift} bits
   * @param shift the distance to shift the mantissa by
   */
  private void shiftMantLeft(int shift) {
    assert( shift >= 0 && shift < 129) : "Can't shift by more than 128 or less than 1 bits";
    if (shift == 0) return;
    if (shift >= 128) {
      mantHi = mantLo = 0;
      return;
    }
    if (shift >= 64) {
      mantHi = mantLo << (shift - 64);
      mantLo = 0;
    } else {
      mantHi = mantHi << shift | mantLo >>> (64 - shift);
      mantLo = mantLo << shift;
    }
  } // private void shiftMantLeft(int shift) {

  /**
   * Subtracts {@code mantHi} and {@code mantLo} from {@code minuendHi} and {@code minuendLo},
   * taking into account the {@code borrow}.
   * The result is returned in {@code mantHi} and {@code mantLo}
   * @param minuendHi the higher 64 bits of the minuend
   * @param minuendLo the lower 64 bits of the minuend
   * @param borrow the borrow from the lower (shifted out) bits (additionally subtracts 1 if borrow != 0)
   * @return the borrow from the higher bit (implicit unity). May be 0 or 1
   */
  private long subtrMant(long minuendHi, long minuendLo, long borrow) {
    if (borrow != 0 && --minuendLo == -1) {
      mantLo = -1 - mantLo;   // -1 - mantLo == minuendLo - mantLo here
      borrow = 1;
    } else {
      mantLo = minuendLo - mantLo;
      borrow = Long.compareUnsigned(mantLo, minuendLo) > 0? 1: 0;
    }
    if (borrow != 0 && --minuendHi == -1) {
      mantHi = -1 - mantHi;
      borrow = 1;
    } else {
      mantHi = minuendHi - mantHi;
      borrow = Long.compareUnsigned(mantHi, minuendHi) > 0? 1: 0;
    }
    return borrow;
  } // private long subtrMant(long minuendHi, long minuendLo, long borrow) {

  /* **********************************************************************************
   * Used by multiplication  **********************************************************
   ********************************************************************************** */

  /** Multiples this instance of {@code Quadruple} by the given {@code Quadruple} factor, ignoring the signs
   * <br>Uses static arrays
   * <b><i>BUFFER_5x32_A, BUFFER_5x32_B, BUFFER_10x32_A</i></b>
   * @param factor the factor to multiply this instance by
   */
  private Quadruple multUnsigned(Quadruple factor) {
    // will use these buffers to hold unpacked mantissas of the factors (5 longs each, 4 x 32 bits + higher (implicit) unity)
    final long[] factor1 = BUFFER_5x32_A, factor2 = BUFFER_5x32_B, product = BUFFER_10x32_A;

    long productExponent =  Integer.toUnsignedLong(exponent)  // Preliminarily evaluate the exponent of the product (may get adjusted)
                          + Integer.toUnsignedLong(factor.exponent) - EXPONENT_OF_ONE;

    if (exponentWouldExceedBounds(productExponent, 1, 0))         // exp < 129 || exp > EXPONENT_OF_MAX_VALUE, assigns respectively 0 or Infinity
      return this;

    // put the mantissas into the buffers that will be used by the proper multiplication
    productExponent = normalizeAndUnpack(factor, productExponent, factor1, factor2); // May decrease productExponent
    if (productExponent < -129)                               // Product will be less than 1/2 MIN_VALUE
      return assignZero(false);

    multiplyBuffers(factor1, factor2, product);               // Leaves the higher half-words empty
    final boolean isRoundedUp = roundBuffer(product);

    productExponent = normalizeProduct(product, productExponent, isRoundedUp);
    if (productExponent > EXPONENT_OF_MAX_VALUE)                            // Overflow, return infinity
      return assignInfinity(false);

    packBufferToMantissa(product);

    if (productExponent <= 0)                                 // Result is subnormal
      productExponent = normalizeSubnormal(productExponent, isRoundedUp);

    exponent = (int)productExponent;
    return this;
  } // private void multUnsigned(Quadruple factor) {

  /**
   * Prepares the mantissas for being multiplied:<br>
   * if one of the factors is subnormal, normalizes it and appropriately corrects the exponent of the product,
   * then unpack both mantissas to buffers {@code buffer1}, {@code buffer2}.
   * @param factor the factor to multiply this by
   * @param productExponent preliminary evaluated value of the exponent of the product
   * @param buffer1 a buffer to hold unpacked mantissa of one of the factors (5 longs, each holds 32 bits )
   * @param buffer2 a buffer to hold unpacked mantissa of the other factor
   * @return the exponent of the product, corrected in case if one of the factors is subnormal
   * <br>Covered
   */
  private long normalizeAndUnpack(Quadruple factor, long productExponent, long[] buffer1, long[] buffer2) {

    // If one of the numbers is subnormal, put its mantissa in mantHi, mantLo
    long factorMantHi = factor.mantHi, factorMantLo = factor.mantLo;
    boolean oneIsSubnormal = false;
    if (exponent == 0) {                 // this is subnormal
      oneIsSubnormal = true;
    } else if (factor.exponent == 0) {   // factor is subnormal, copy it's mantissa to this and this to mantHi, mantLo
      factorMantHi = this.mantHi; factorMantLo = this.mantLo;
      this.mantHi = factor.mantHi; this.mantLo = factor.mantLo;
      oneIsSubnormal = true;
    }

    if (oneIsSubnormal) {       // Subnotmal's mantissa is here now. Normalize it and adjust exponent
      final int lz = numberOfLeadingZeros();
      productExponent -= lz;
      if (productExponent < -129)  // Product would be less than (MIN_VALUE / 2)
        return productExponent;
      shiftMantLeft(lz+1); // Normalize mantissa
    }

    unpack_To5x32(mantHi, mantLo, buffer1);
    unpack_To5x32(factorMantHi, factorMantLo, buffer2);
    return productExponent;
  } // private long normalizeAndUnpack(Quadruple factor, long productExponent, long[] buffer1, long[] buffer2) {

  /**
   * Multiplies the value stored in {@code factor1} as unpacked 128-bit}<br>
   * {@code (4 x 32 bit + highest (implicit) unity)}
   * <br>
   * by the value stored in factor2 of the same format and saves the result
   * in the {@code product} as unpacked 256-bit value}<br>
   * {@code (8 x 32 bit + 1 or 2 highest bits of the product + 0)  }
   * @param factor1 contains the unpacked value of factor1
   * @param factor2 contains the unpacked value of factor2
   * @param product gets filled with the unpacked value of the product
   */
  private static void multiplyBuffers(long[] factor1, long[] factor2, long[] product) {
    assert(factor1.length == factor2.length && product.length == factor1.length * 2):
          "Factors' lengths must be equal to each other and twice less than the product's length";

    Arrays.fill(product, 0);
    final int maxIdxFact = factor1.length - 1;
    long sum = 0;

    for (int i = maxIdxFact; i >= 0; i--) { // compute partial 32-bit products
      for (int j = maxIdxFact; j >= 0; j--) {
        sum = factor1[i] * factor2[j];
        product[i + j + 1] += sum & LOWER_32_BITS;
        product[i + j] += (sum >>> 32) + (product[i + j + 1] >>> 32);
        product[i + j + 1] &= LOWER_32_BITS;
      }
    }
  } // private static void multiplyBuffers(long[] factor1, long[] factor2, long[] product) {

  /**
   * Rounds the content of the given unpacked buffer
   * so that it contains 128 bits of the fractional part of the product.
   * The integer part of the product of two mantissas is contained in the lowest bits of it buffer[1],
   * the fractional part is contained in the lower half-words of buffer[2]..buffer[6].
   * If bit 129 (counting rightwards starting from the point position),
   * i.e. bit 31 of buffer[6], is 1, the content of buffer[1] -- buffer[5] gets incremented.
   * @return a flag signifying that the number is actually rounded up,
   * used to prevent unnecessary rounding in the future
   */
  private boolean roundBuffer(long[] buffer) {
    buffer[6] += 0x8000_0000L;               // it's 1/2 of the LSB. Round half-up
    if ((buffer[6] & 0x1_0000_0000L) == 0)   // no carry at all
      return false;

    for (int i = 5;; i--) {             // it will inevitably break, at most at the last iteration
      buffer[i + 1] = 0;                // If there's carry here, the lower word = 0
      buffer[i]++;                      // propagate the carry. The higher half-word is always 0
      if ((buffer[i] & 0x1_0000_0000L) == 0) // Still 0. No carry to the next higher word
        break;
    }
    return true;
  } // private boolean roundBuffer(long[] buffer) {

  /**
   * Normalizes a product of multiplication.<br>
   * The product may be => 2 (e.g. 1.9 * 1.9 = 3.61), in this case it should be
   * divided by two, and the exponent should be incremented.
   * @param product a buffer containing the product
   * @param productExponent preliminary evaluated exponent of the product (may get adjusted)
   * @param isRoundedUp a flag signifying that rounding should not be applied
   * @return the exponent of the product, perhaps adjusted
   */
  private long normalizeProduct(long[] product, long productExponent, boolean isRoundedUp) {
    if (product[1] > 1) {                   // Carry to the highest (implied) bit --
      productExponent++;
      if (productExponent <= EXPONENT_OF_MAX_VALUE)
        shiftBufferRight(product, isRoundedUp);
    }
    return productExponent;
  } // private long normalizeProduct(long[] product, long productExponent, boolean isRoundedUp) {

  /**
   * Packs unpacked mantissa held in the given buffer
   * (0, 1 (integer part, i.e. implicit unity), + 4 longs containing 32 bits each)
   * into the {@code mantLo}, {@code mantHi} fields of this instance
   * @param buffer of 6 (at least) longs, containing the fractional part of the mantissa in the lower halves of words 2..5
   * <br> Covered (no special data required)
   */
  private void packBufferToMantissa(long[] buffer) {
    mantLo = buffer[5] & LOWER_32_BITS | (buffer[4] << 32);
    mantHi = buffer[3] & LOWER_32_BITS | (buffer[2] << 32);
  } // private void packBufferToMantissa(long[] buffer) {

  /**
   * Normalizes a subnormal value (with negative exponent),
   * shifting the bits of its mantissa rightwards according to the exponent's value and clearing
   * @param productExponent the exponent of the product (always negative for subnormal results)
   * @param isRoundedUp a flag to prevent taking into account the shifted-out LSB when rounding the value
   * (in case if the value was already rounded-up)
   * @return the exponent value of a subnormal Quadruple, that is 0
   */
  private long normalizeSubnormal(long productExponent, boolean isRoundedUp) {
    if (isRoundedUp) mantLo &= -2L;     // Clear LSB to avoid excessive rounding up
    productExponent = makeSubnormal(productExponent);
    return productExponent;
  } // private long normalizeSubnormal(long productExponent, boolean alreadyRounded) {

  /**
   * Shifts the contents of a buffer, containing the unpacked mantissa
   * of a Quadruple as the lower halves of {@code buffer[2] -- buffer[5]}, rightwards one bit.
   * Rounds it up unless the {@code isRoundedUp} parameter is {@code true}.
   * @param buffer the buffer of (at least) 6 longs, containing the mantissa of a Quadruple value
   * @param isRoundedUp a flag to prevent extra rounding in case if the value is already rounded up
   */
  private void shiftBufferRight(long[] buffer, boolean isRoundedUp) {
    if (isRoundedUp)
      shiftBuffRightWithoutRounding(buffer);
    else
      shiftBuffRightWithRounding(buffer); // There can't be carry to the highest (implied) bit, no need to check it
  } // private void shiftBufferRight(long[] buffer, boolean isRoundedUp) {

  /**
   * Shifts the contents of a buffer, containing the unpacked mantissa
   * of a Quadruple as the lower halves of {@code buffer[2] -- buffer[5]}, rightwards one bit, and rounds it up.
   * @param buffer the buffer of (at least) 6 longs, containing the mantissa of a Quadruple value
   */
  private void shiftBuffRightWithRounding(long[] buffer) {
    final long carry = buffer[5] & 1;
    shiftBuffRightWithoutRounding(buffer);
    buffer[5] += carry;                   // former LSB, currently shifted out
    for (int i = 5; i >= 2; i--) {        // Propagate carry.
      if ((buffer[i] & HIGHER_32_BITS) != 0) { // OVerflow of the lower 32 bits
        buffer[i] &= LOWER_32_BITS;
        buffer[i - 1]++;                  // Add carry to the next higher word
      }
    }
  } // private void shiftBuffRightWithRounding(long[] buffer) {

  /**
   * Shifts the contents of a buffer, containing the unpacked mantissa
   * of a Quadruple as the lower halves of {@code buffer[2] -- buffer[5]}, rightwards one bit, without rounding it up
   * (the shifted-out LSB is just truncated).
   * @param buffer the buffer of (at least) 6 longs, containing the mantissa of a Quadruple value
   */
  private void shiftBuffRightWithoutRounding(long[] buffer) {
    for (int i = 5; i >= 2; i--)
      buffer[i] = (buffer[i] >>> 1) | (buffer[i - 1] & 1) << 31;
  } // private void shiftBuffRightWithoutRounding(long[] buffer) {

  /** Unpacks the value of the two longs, containing 128 bits of a fractional part of the mantissa,
   * to an "unpacked" buffer, that consists of 5 longs,
   * the first of which contains the integer part of the mantissa, aka implicit unity (that is always 1),
   * and the others (the items {@code buffer[1] -- buffer[4]}) contain 128 bits
   * of the fractional part in their lower halves (bits 31 - 0),
   * the highest 32 bits in {@code buffer[1]).
   * @param factHi the higher 64 bits of the fractional part of the mantissa
   * @param mantLo the lower 64 bits of the fractional part of the mantissa
   * @param buffer the buffer to hold the unpacked mantissa, should be array of at least 5 longs
   * @return the buffer holding the unpacked value (the same reference as passed in as the {@code buffer} parameter
   */
  private static long[] unpack_To5x32(long mantHi, long mantLo, long[] buffer) {
    buffer[0] = 1;
    buffer[1] = mantHi >>> 32;
    buffer[2] = mantHi & LOWER_32_BITS;
    buffer[3] = mantLo >>> 32;
    buffer[4] = mantLo & LOWER_32_BITS;
    return buffer;
  } // private static long[] unpack_To5x32(long factHi, long factLo, long[] buffer) {

  protected void ____Used_By_division____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /* **********************************************************************************
   * Used by division *****************************************************************
   ********************************************************************************** */

  /**
   * Divides this instance of {@code Quadruple} by the given {@code Quadruple} divisor, ignoring their signs
   * <br>Uses static arrays
   * <b><i>BUFFER_5x32_A, BUFFER_5x32_B, BUFFER_10x32_A, BUFFER_10x32_B</i></b>
   * 20.10.17 10:26:36 A new version with probable doubling of the dividend and
   * simplified estimation of the quotient digit and simplified estimation of the necessity of rounding up the result
   * @param divisor the divisor to divide this instance by
   * <br>Covered
   */
  private Quadruple divideUnsigned(Quadruple divisor) { // 20.10.17 10:26:36
    if (divisor.compareMagnitudeTo(ONE) == 0)       // x / 1 = x;
      return this;
    if (compareMagnitudeTo(divisor) == 0)           // x / x = 1;
      return assignOne(false);

    long quotientExponent = Integer.toUnsignedLong(exponent) // Preliminarily evaluate the exponent of the quotient (may get adjusted)
                          - Integer.toUnsignedLong(divisor.exponent) + EXPONENT_OF_ONE;

    if (exponentWouldExceedBounds(quotientExponent, 0, 1)) // exp < -128 || exp > EXPONENT_OF_MAX_VALUE + 1, Assigns respective values if exp exceeds bounds
      return this;

    boolean needToDivide = true;
    final long[] divisorBuff = BUFFER_5x32_A;
    if (exponent != 0 & divisor.exponent != 0) {    // both are normal
      if (mantHi == divisor.mantHi && mantLo == divisor.mantLo) { // Mantissas are equal, result. mantissa = 1
        mantHi = mantLo = 0;                        // will return 2 ^ (exp1 - exp2)
        needToDivide = false;                       // Mark that actual division not needed
      } else {                                      // Mantissas differ, division may be needed
        if (divisor.mantHi == 0 && divisor.mantLo == 0) // Divisor == 2^n, mantissa remains unchanged
          needToDivide = false;                     // Mark that actual division not needed
        else                                        // divisor != 2^n
          unpack_To5x32(divisor.mantHi, divisor.mantLo, divisorBuff);  // divisor as an array of long containing the unpacked mantissa
      }
    } else {                                        // At least one is subnormal
      quotientExponent = normalizeAndUnpackSubnormals(quotientExponent, divisor, divisorBuff);
    }

    if (needToDivide)
      quotientExponent = doDivide(quotientExponent, divisorBuff); // *** Proper division

    if (exponentWouldExceedBounds(quotientExponent, 0, 0)) // exp < -128 || exp > EXPONENT_OF_MAX_VALUE, Assigns respective values if exp exceeds bounds
      return this;

    if (quotientExponent <= 0)
      quotientExponent = makeSubnormal(quotientExponent);

    exponent = (int)quotientExponent;
    return this;
  } // private Quadruple divideUnsigned(Quadruple divisor) { // 20.10.17 10:26:36

  /**
   * Checks if the exponent of the result exceeds acceptable bounds and sets the
   * corresponding value in this case.<br>
   * If it is below {@code  -(128 + lowerBoundTolerance) }, assigns zero to this instance and returns {@code true}.
   * If it is above {@code EXPONENT_OF_MAX_VALUE + upperBoundTolerance}, assigns Infinity to this instance and returns {@code true}.
   * returns {@code false} without changing the value, if the exponent is within the bounds.
   * @param exponent the exponent of the result to be examined
   * <br>Covered
   */
  private boolean exponentWouldExceedBounds(long exponent, long lowerBoundTolerance, long upperBoundTolerance) {
    if (exponent > EXPONENT_OF_MAX_VALUE + upperBoundTolerance) {// Overflow, return infinity
      assignInfinity(false);
      return true;
    }
    if (exponent < -(128 + lowerBoundTolerance)) {                            // The result may be subnormal, so expSum may be negative
      assignZero(false);
      return true;
    }
    return false;
  } // private boolean exponentWouldExceedBounds(long exponent, long lowerBoundTolerance, long upperBoundTolerance) {

  /**
   * Normalizes the operands and unpacks the divisor:<br>
   * normalizes the mantissa of this instance as needed, if it's subnormal;
   * unpacks the mantissa of the divisor into divisorBuff and normalizes the unpacked value if the divisor is subnormal
   * (the divisor itself remains unchanged);
   * adjusts appropriately the quotientExponent.
   * @param quotientExponent preliminary evaluated exponent of the divisor, gets corrected and returned
   * @param divisor  the divisor to unpack and normalize as needed
   * @param divisorBuff the buffer to unpack the divisor into
   * @return the exponent of the quotient, adjusted accordingly to the normalization results
   * <br>Covered
   */
  private long normalizeAndUnpackSubnormals(long quotientExponent, Quadruple divisor, long[] divisorBuff) {
    if (exponent == 0)                           // Dividend is subnormal
      quotientExponent -= normalizeMantissa();

    if (divisor.exponent == 0) {                // Divisor is subnormal
      quotientExponent += normalizeAndUnpackDivisor(divisor, divisorBuff);  // normalize and unpack
    } else                                       // Divisor is normal
      unpack_To5x32(divisor.mantHi, divisor.mantLo, divisorBuff); // just unpack

    return quotientExponent;
  } // private long normalizeAndUnpackSubnormals(long quotientExponent, Quadruple divisor, long[] divisorBuff) {

  /**
   * Shifts the mantissa (of a subnormal value) leftwards so that it has
   * the conventional format (with implied higher unity to the left of the highest bit of mantHi
   * and higher 64 bits of the fractional part in mantHi)
   * @return the number of bits the mantissa is shifted by, minus one (e.g. 0 for MIN_NORMAL / 2)
   * to use as an exponent correction
   * <br>Covered
   */
  private long normalizeMantissa() {
    int shift = Long.numberOfLeadingZeros(mantHi);
    if (shift == 64)
      shift += Long.numberOfLeadingZeros(mantLo);
    shiftMantLeft(shift + 1); // shift by at least one position (for MIN_NORMAL / 2)
    return shift;             // exponent correction = shift value - 1, it's 0 for MIN_NORMAL / 2
  } // private long normalizeMantissa() {

  /**
   * Unpacks the mantissa of the given divisor into the given buffer and normalizes it,
   * so that the buffer contains the MSB in the LSB of buffer[0]
   * and up to 127 bits in the lower halves of buffer[1] -- buffer[4]
   * @param divisor a subnormal Quadruple whose mantissa is to be unpacked and normalizes
   * @param buffer a buffer of at least 5 longs to unpack the mantissa to
   * @return the number of bits the mantissa is shifted by, minus one (e.g. 0 for MIN_NORMAL / 2)
   * to use as an exponent correction
   * <br>Covered
   */
  private static long normalizeAndUnpackDivisor(Quadruple divisor, long[] buffer) {
    long mantHi = divisor.mantHi, mantLo = divisor.mantLo;
    int shift = Long.numberOfLeadingZeros(mantHi); // the highest 1 will be the implied unity -- shift-out it
    if (shift == 64)
      shift += Long.numberOfLeadingZeros(mantLo);
    final long result = shift; shift++;

    if (shift <= 64) {
      mantHi = (mantHi << shift) + (mantLo >>> 64 - shift);
      mantLo <<= shift;
    } else {
      mantHi = mantLo << shift - 64;
      mantLo = 0;
    }

    unpack_To5x32(mantHi, mantLo, buffer);
    return result; // exp correction = shift value - 1, it's 0 for MIN_NORMAL / 2
  } // private static long normalizeAndUnpackDivisor(Quadruple divisor, long[] buffer) {

  /** Divides preliminarily normalized mantissa of this instance by the mantissa of the divisor
   * given as an unpacked value in {@code divisor}.
   * @param quotientExponent a preliminarily evaluated exponent of the quotient, may get adjusted
   * @param divisor unpacked divisor (integer part (implicit unity) in divisor[0],
   *    128 bits of the fractional part in the lower halves of divisor[1] -- divisor[4])
   * @return (possibly adjusted) exponent of the quotient
   */
  private long doDivide(long quotientExponent, final long[] divisor) {
    final long[] dividend = BUFFER_10x32_A; // Will hold dividend with integer part in buff[1] and mantissa in buff[2] -- buff[5]
    quotientExponent = unpackMantissaTo(quotientExponent, divisor, dividend);
    divideBuffers(dividend, divisor, quotientExponent); // proper division
    return quotientExponent;
  } // private long doDivide(long quotientExponent, final long[] divisor) {

  /**
   * Unpacks the mantissa of this instance into {@code dividend}.
   * If the mantissa of this instance is less than the divisor, multiplies it by 2 and decrements the {@code quotientExponent}
   * @param quotientExponent a preliminary evaluated exponent of the quotient being computed
   * @param divisor a buffer (5 longs) containing unpacked divisor,
   *   integer 1 in divisor[0], 4 x 32 bits of the mantissa in in divisor[1..4]
   * @param dividend a buffer (10 longs) to unpack the mantissa to,
   *   integer 1 (or up to 3 in case of doubling) in dividend[1], 4 x 32 bits of the mantissa in in dividend[2..5]
   * @return (possibly decremented) exponent of the quotient
   */
  private long unpackMantissaTo(long quotientExponent, final long[] divisor, final long[] dividend) {
    // The mantissa of this is normalized, the normalized mantissa of the divisor is in divisorBuff
    if (compareMantissaWith(divisor) < 0) {
      unpackDoubledMantissaToBuff_10x32(dividend);
      quotientExponent--;
    } else
      unpackMantissaToBuff_10x32(dividend);
    return quotientExponent;
  } // private long unpackMantissaTo(long quotientExponent, final long[] divisor, final long[] dividend) {

  /**
   * Compares the mantissa of this instance with the unpacked mantissa of another Quadruple value.
   * Returns
   * <li>a positive value if the mantissa of this instance is greater,
   * <li>zero if the mantissas are equal,
   * <li>a negative value if the mantissa of this instance is less than the mantissa of the other Quadruple.
   * <br><br>
   * @param divisor a buffer (5 longs) containing an unpacked value of the mantissa of the other operand,
   *   integer 1 in divisor[1], 4 x 32 bits of the mantissa in in divisor[1..4]
   * @return a positive value if the mantissa of this instance is greater, zero if the mantissas are equal,
   *   or a negative value if the mantissa of this instance is less than the mantissa of the other Quadruple.
   */
  private int compareMantissaWith(long[] divisor) {
    final int cmp = Long.compareUnsigned(mantHi, (divisor[1] << 32) | divisor[2] );
    return cmp == 0? Long.compareUnsigned(mantLo, (divisor[3] << 32) | divisor[4] ) : cmp;
  } // private int compareMantissaWith(long[] divisor) {

  /**
   * Unpacks the mantissa of this instance into the given buffer and multiplies it by 2 (shifts left),
   * integer part (may be up to 3) in buffer[1], fractional part in lower halves of buffer[2] -- buffer[5]
   * @param buffer a buffer to unpack the mantissa, at least 6 longs
   * <br>Covered
   */
  private void unpackDoubledMantissaToBuff_10x32(long[] buffer) { //
    Arrays.fill(buffer, 0);
    buffer[1] = 2 + (mantHi >>> 63);
    buffer[2] = mantHi >>> 31 & LOWER_32_BITS;
    buffer[3] = ((mantHi << 1) + (mantLo >>> 63)) & LOWER_32_BITS;
    buffer[4] = mantLo >>> 31 & LOWER_32_BITS;
    buffer[5] = mantLo << 1 & LOWER_32_BITS;
  }; // private void unpackDoubledMantissaToBuff_10x32(long[] buffer) { //

  /**
   * Unpacks the mantissa of this instance into the given buffer,
   * integer part (implicit unity) in buffer[1], fractional part in lower halves of buffer[2] -- buffer[5]
   * @param buffer a buffer to unpack the mantissa, at least 6 longs
   * <br>Covered
   */
  private void unpackMantissaToBuff_10x32(long[] buffer) {
    Arrays.fill(buffer, 0);
    buffer[1] = 1;
    buffer[2] = mantHi >>> 32;
    buffer[3] = mantHi & LOWER_32_BITS;
    buffer[4] = mantLo >>> 32;
    buffer[5] = mantLo & LOWER_32_BITS;
  } // private void unpackMantissaToBuff_10x32(long[] buffer) {

  /**
   * Divides the dividend given as an unpacked buffer {@code dividendBuff} by the divisor
   * held in the unpacked form in the {@code divisorBuff} and packs the result
   * into the fields {@code mantHi, mantLo} of this instance. Rounds the result as needed.
   * <br>Uses static arrays
   * <b><i>BUFFER_5x32_B, BUFFER_10x32_B</i></b>
   * @param dividend a buffer of 10 longs containing unpacked mantissa of the dividend (integer 1 (or up to 3 in case of doubling) in dividend[1],
   *    the fractional part of the mantissa in the lower halves of dividend[2] -- dividend[5])
   * @param divisor a buffer of 5 longs containing unpacked mantissa of the divisor (integer part (implicit unity) in divisor[0],
   *    the fractional part of the mantissa in the lower halves of divisor[1] -- divisor[4])
   * @param quotientExponent preliminary evaluated exponent of the quotient, may get adjusted
   * @return (perhaps adjusted) exponent of the quotient
   * <br>Covered
   */
  private void divideBuffers(long[] dividend, long[] divisor, long quotientExponent) {
    final long[] quotientBuff = BUFFER_5x32_B;  // Will be used to hold unpacked quotient
    final long nextBit = divideArrays(dividend, divisor, quotientBuff);    // Proper division of the arrays, returns the next bit of the quotient
    packMantissaFromWords_1to4(quotientBuff);   // Pack unpacked quotient into the mantissa fields of this instance

    if (quotientExponent > 0           // Not rounding for subnormals, since the rounding will be done by makeSubnormal()
        && nextBit  != 0               // if remainder >= (LSB_of_the_quotient * 0.5)
        && ++mantLo == 0)              // and mantLo was FFFF_FFFF_FFFF_FFFF, now became 0
      ++mantHi;                        // carry to the higher word
  } // private void divideBuffers(long[] dividend, long[] divisor, long quotientExponent) {

  /**
   * Divides an unpacked value held in the 10 longs of the {@code dividend)
   * by the value held in the 5 longs of the {@code divisor}
   * and fills 5 longs of the {@code quotient} with the quotient value.
   * All values are unpacked 129-bit values, containing integer parts
   * (implicit unities, always 1) in LSB of buff[0] (buff[1] for dividend)
   * and 128 bits of the fractional part in lower halves of buff[1] -- buff[4] (buff[2] -- buff[5] for dividend).
   * It uses the principle of the standard long division algorithm, with the difference
   * that instead of one decimal digit of the quotient at each step, the next 32 bits are calculated.
   * <br>Uses static arrays
   * <b><i>BUFFER_10x32_B</i></b>
   * @param dividend an unpacked value of the mantissa of the dividend (10 x 32 bits: 0, 1, dd1, dd2, dd3, dd4, 0, 0, 0, 0)
   * @param divisor an unpacked value of the mantissa of the divisor (5 x 32 bits: 1, dr1, dr2, dr3, dr4)
   * @param quotient a buffer that gets filled with the mantissa of the quotient
   * @return the next bit of the quotient (half the LSB), to be used for rounding the result
   * <br>Covered
   */
  private static long divideArrays(long[] dividend, long[] divisor, long[] quotient) {
    final long[] remainder = dividend;            // will contain remainder after each iteration
    Arrays.fill(quotient, 0);

    final long divisorHigh = (divisor[0] << 32) | divisor[1];   // The most significant word of the divisor
    int offset = 0;                               // the index of the quotient word being computed
    quotient[offset++] = 1;                       // the integer part aka the implicit unity of the quotient is always 1
    subtractDivisor(divisor, remainder);          // Subtract divisor multiplied by 1 from the remainder

    // Compute the quotient by portions by 32 bits per iterations
    if (!isEmpty(remainder))
      do {
        final long remainderHigh = (remainder[offset + 1] << 32) | remainder[offset + 2]; // The most significant 64 bits of the remainder
        long quotientWord = (remainder[offset] == 0)?
            Long.divideUnsigned(remainderHigh, divisorHigh):
            divide65bits(remainder[offset], remainderHigh, divisorHigh);

        if (quotientWord != 0) {    // Multiply divisor by quotientWord and subtract the product from the remainder, adjust quotientWord
          multipyAndSubtract(divisor, quotientWord, offset, remainder);
          if (remainder[0] < 0) {                         // The quotiendWord occurred to be too great
            quotientWord--;                               // decrease it
            addDivisorBack(divisor, remainder, offset);   // Add divisor * 1 back
          }
        }

        quotient[offset++] = quotientWord;          // The next word of the quotient
      } while (offset <= 4 && !isEmpty(remainder));        // while the 5 half-words of the quotient are not filled and the remainder !=0

    return findNextBitOfQuotient(remainder, divisor);
  } // private static long divideArrays(long[] dividend, long[] divisor, long[] quotient) {

  /**
   * Subtracts the divisor from the dividend to obtain the remainder for the first iteration
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param remainder unpacked mantissa of the dividend, 2 + 8 x 32 bits, implicit integer 1 in divisor[1]
   */
  private static void subtractDivisor(long[] divisor, long[] remainder) {
    for (int i = 5; i >= 1; i--) {
      remainder[i] -= divisor[i - 1];
      if ((remainder[i] & HIGHER_32_BITS) != 0) {
        remainder[i - 1]--;
        remainder[i] &= LOWER_32_BITS;
      }
    }
  } // private static void subtractDivisor(long[] divisor, long[] remainder) {

  /**
   * Divides a dividend, consisting of more than 64 bits (and less than 81 bits),
   * by the given divisor, that may contain up to 33 bits.
   * @param dividendHi the most significant 64 bits of the dividend
   * @param dividendLo the least significant 64 bits of the dividend
   * @param divisor the divisor
   * @return the quotient
   */
  private static long divide65bits(long dividendHi, long dividendLo, long divisor) {
    final long shiftedDividend = dividendHi << 48 | dividendLo >>> 16;  // 16 bits of dividendHi and 48 bits of dividendLo
    final long quotientHi = shiftedDividend / divisor;                  // The most significant 32 bits of the quotient
    final long remainder = ((shiftedDividend % divisor) << 16) | (dividendLo & 0xFFFF);
    final long quotientLo = remainder / divisor;                        // The least significant 16 bits of the quotient
    return quotientHi << 16 | quotientLo;
  } // private static long divide65bits(long dividendHi, long dividendLo, long divisor) {

  /**
   * Multiplies the divisor by a newly found word of quotient,
   * taking into account the position of the word in the quotient ({@code offset} is the index
   * of the given word in the array that contains the quotient being calculated),
   * and subtracts the product from the remainder, to prepare the remainder for the next iteration
   * <br>Uses static arrays
   * <b><i>BUFFER_10x32_B</i></b>
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param quotientWord a newly found word (32 bits) of the quotient being computed
   * @param offset the position (index) of the given {@code quotientWord} in the future quotient,
   *    defines the position of the product, that is subtracted from the remainder, relative to the latter
   * @param remainder the remainder to subtract the product from
   */
  private static void multipyAndSubtract(long[] divisor, long quotientWord, int offset, long[] remainder) {
    offset++;
    final long[] partialProduct = BUFFER_10x32_B;
    multDivisorBy(divisor, quotientWord, partialProduct, offset); // multiply divisor by qW with the given offset
    subtractProduct(partialProduct, remainder, offset);           // and subtract the product from the remainder
  } // private static void multipyAndSubtract(long[] divisor, long quotientWord, int offset, long[] remainder) {

  /**
   * Multiplies the divisor by the 32 bits of the quotientWord with the given offset,
   * so that <br>{@code product[i + offset] = divisor[i] * quotientWord},}<br>
   * as if the quotientWord were the only non-zero item in a buffer containing 5 x 32 bits of a factor,
   * and were located in buff[offset]. The result is an 'unpacked' value, i. e. contains the value
   * in the 32 least significant bits of each long.
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param quotientWord the 32 bits to multiply the divisor by
   * @param product a buffer to hold the result, 10 longs
   * @param offset the offset to add to the index when filling the resulting product
   * <br>Covered
   */
  private static void multDivisorBy(long[] divisor, long quotientWord, long[] product, int offset) {
    Arrays.fill(product, 0);            // Partial product
    if (quotientWord == 1)              // Just copy
      for (int i = 0; i < 5; i++)       // product has the most significant bit in halfword 1, product[0] is not used
        product[i + offset] = divisor[i];
    else {
      for (int i = 0; i < 5; i++)
        product[i + offset] = divisor[i] * quotientWord;   // product[offset..offset+4]
      for (int i = 4 + offset; i >= offset; i--) {         // propagate carry
        product[i - 1] += product[i] >>> 32;
        product[i] &= LOWER_32_BITS;
      }
    }
  } // private static void multDivisorBy(long[] divisor, long quotientWord, long[] product, int offset) {

  /**
   * Subtracts half-words of product[1] through product[4 + offset]
   * from the corresponding half-words of the remainder.
   * Propagates borrow from the least significant word of the result (remainder[4 + offset]) up to remainder[0].
   * Keeps the higher half-words of the remainder empty.
   * @param product the product to subtract from the remainder, as an unpacked 257-bit value
   * @param remainder the remainder to subtract product from, as an unpacked 257-bit value
   * @param offset the offset of the implied most significant word of 129-bit remainder
   * <br>Covered
   */
  private static void subtractProduct(long[] product, long[] remainder, int offset) {
    for (int i = 4 + offset; i >= 1; i--) {
      remainder[i] -= product[i];
      if ((remainder[i] & HIGHER_32_BITS) != 0) {
        remainder[i - 1]--;
        remainder[i] &= LOWER_32_BITS;
      }
    }
  } // private static void subtractProduct(long[] product, long[] remainder, int offset) {

  /**
   * Adds the divisor, shifted by offset words, back to remainder, to correct the remainder in case when
   * preliminarily estimated word of quotient occurred to be too great
   * @param divisor unpacked mantissa of the divisor, 1 + 4 x 32 bits, implicit integer 1 in divisor[0]
   * @param remainder unpacked mantissa of the dividend, 2 + 8 x 32 bits, implicit integer 1 in divisor[1]
   * @param offset
   */
  private static void addDivisorBack(long[] divisor, long[] remainder, int offset) {
    offset++;
    for (int i = 4 + offset; i >= 1; i--) {
      remainder[i] += i < offset? 0 : divisor[i - offset];
      if ((remainder[i] & HIGHER_32_BITS) != 0) {
        remainder[i - 1]++;
        remainder[i] &= LOWER_32_BITS;
      }
    }
  } // private static void addDivisorBack(long[] divisor, long[] remainder, int offset) {

  /**
   * After the basic division, finds the next bit of the quotient
   * (corresponding to 2^-129, i.e. half the least significant bit of the mantissa), to round up the quotient if this bit isn't 0.
   * Compares the remainder with the divisor and if the remainder >= 0.5 * divisor, returns 1, otherwise returns 0.
   * @param remainder the remainder (dividend - quotient * divisor ), unpacked (10 longs)
   * @param divisor  the divisor, unpacked (5 longs)
   * @return 1 if the remainder is equal to or greater than half the divisor
   */
  private static long findNextBitOfQuotient(long[] remainder, long[] divisor) {
    for (int i = 0; i < divisor.length - 1; i++) {
      final long rw = (remainder[i + 5] * 2 & LOWER_32_BITS) // a current word of the remainder multiplied by 2
                    + (remainder[i + 6] >> 31);              // MSB of the next word as the LSB
      if (rw > divisor[i]) return 1;
      if (rw < divisor[i]) return 0;
    }
    if ((remainder[9] * 2 & LOWER_32_BITS) < divisor[4]) return 0;
    return 1;
  } // private static long findNextBitOfQuotient(long[] remainder, long[] divisor) {

  /**
   * Packs the unpacked value from words 1..4 of the given buffer into the mantissa of this instance
   * @param buffer the buffer of at least 5 longs, containing an unpacked value of the mantissa
   * (the integer part (implicit unity) in buffer[0], the 128 bits of fractional part in the lower halves of buffer[1] -- buffer[4]
   * <br>Covered
   */
  private void packMantissaFromWords_1to4(long[] buffer) {
    mantLo = buffer[4] | (buffer[3] << 32);
    mantHi = buffer[2] | (buffer[1] << 32);
  } // private void packMantissaFromWords_1to4(long[] buffer) {

  protected void ____Used_By_sqrt____() {} // Just to put a visible mark of the section in the outline view of the IDE

  /********************************************************************************************
   *** Methods used by sqrt() *****************************************************************
   ********************************************************************************************/

  private static final int WORD_LENGTH = 64;
  private static final int DIGIT_LENGTH  = 8;
  private static final int MAX_BITS_FOR_SQRT = 20 * DIGIT_LENGTH; // To provide precision sufficient for additional multiplying by sqrt(2)

  /** Calculates the higher 136 bits of the square root of the mantissa
   * and stores the high 128 bits of result in mantHi, mantLo,
   * returns the 2 least significant bits, aligned to the left,
   * as the result for purposes of possible rounding
   * <br>Uses static arrays
   * <b><i>BUFFER_3x64_A, BUFFER_3x64_B, BUFFER_3x64_C, BUFFER_3x64_D</i></b>
   * @return bits 128 -- 135 of the root in the high byte of the long result
   */
  /*
  Implements a variant of an iterative digit-by-digit algorithm, based on the invariant

      S >= r^2 + 2rd + d^2,

  where S is an argument, r is the root found so far and d is the next digit.
  The scheme is as following:

     private static final int REQUIRED_DIGITS = 15;
     private static final double BASE = 10.0; // To find a decimal digit at each iteration;

     private static double doubleRoot(double arg) {  // Argument is in range [1.0, 2.0)
       double scale = 1.0 / BASE;   // Used to scale the next digit in turn
                                    // before adding it to the root being computed
                                     *
       // The first (highest) digit after the point
       // is a simple function of the two first digits after the point,
       // we can use a lookup table to quickly find it
       double digit = findFirstDigit(arg);
       double root = 1.0 + digit * scale; // 1 + d * 0.1 = 1.d

       scale /= BASE;
       double remainder = (arg - root * root) / (scale);

       int digitCount = 0;
       while (remainder > 0 && digitCount++ < REQUIRED_DIGITS) {
         // Find a maximum digit such that (root * 2 + digit * scale) * digit <= remainder
         digit = findNextDigit(remainder, root, scale); // The next lower digit of the root
         remainder = (remainder - (root * 2 + digit * scale) * digit) * BASE; // 2rd + d^2 * scale
         root += digit * scale;
         scale /= BASE;
       }
       return root;
     }

   Uses base = 256, i.e. calculates 8 bits at each iteration.
 */
  private long sqrtMant() {

    final long[] remainder = BUFFER_3x64_A;       // aliases for static buffers
    final long[] rootX2    = BUFFER_3x64_B; Arrays.fill(rootX2, 0);  // Doubled root with explicit higher unity
    final long[] root      = BUFFER_3x64_C; Arrays.fill(root, 0);

    final long digit = findFirstDigit();                      // Find the first byte of the root

    remainder[0] = mantHi - ((0x200 + digit) * digit << 48);  // Most significant
    remainder[1] = mantLo;
    remainder[2] = 0;
    shift_6_bitsLeft(remainder);

    root[0] = digit << WORD_LENGTH - DIGIT_LENGTH;     // the first digit to the high bits
    // The doubled root contains explicit unity, and the digits shifted right by 9 bits,
    // so that the first (most significant) digit's position is as follows: 0b0000_0000_1###_####_#000_...
    // Such scale is convenient for finding the next digit
    rootX2[0] = 0x0080_0000_0000_0000L + (digit << (WORD_LENGTH - (DIGIT_LENGTH * 2) - 1));

    int bitNumber = DIGIT_LENGTH;                        // The position of the nest digit

    if (!isEmpty(remainder))
      while (bitNumber < MAX_BITS_FOR_SQRT) {
        bitNumber = computeNextDigit(remainder, rootX2, root, bitNumber);
      }

    mantHi = root[0];
    mantLo = root[1];
    return root[2];
  } // private long sqrtMant() {

  /**
   * Finds the first byte of the root, using a table that maps
   * the most significant 16 bits of the mantissa of this instance
   * to corresponding 8 bits of the sought root
   * @return
   */
  private long findFirstDigit() {
    final int sqrtDigit = (int)(mantHi >>> 48);               // first 16 bits of the argument
    int idx = Arrays.binarySearch(SQUARE_BYTES, sqrtDigit);
    if (idx < 0) idx = -idx - 2;
    final long digit = ROOT_BYTES[idx];                        // first 8 bits of the root
    return digit;
  } // private long findFirstDigit() {

  /** Shifts the contents of the buffer left by 6 bits
   * @param buffer the buffer to shift
   */
  private static void shift_6_bitsLeft(long[] buffer) {
    for (int i = 0; i < buffer.length - 1; i++)
      buffer[i] = (buffer[i] << 6 | buffer[i + 1] >>> 58);
    buffer[buffer.length - 1] <<= 6;
  } // private static void shift_6_bitsLeft(long[] buffer) {

  /**
   * Calculates the next digit of the root, appends it to the root at a suitable position
   * and changes the involved values, remainder and rootX2, accordingly
   * (extracted from sqrtMant() 20.09.02 10:19:34)
   * <br>Uses static arrays
   * <b><i>BUFFER_3x64_D</i></b>
   * @param remainder the remainder
   * @param rootX2 doubled root
   * @param root square root found so far
   * @param bitNumber the position of the digit to be found
   * @return the position of the next to be found
   */
  private static int computeNextDigit(final long[] remainder, final long[] rootX2, final long[] root, int bitNumber) {
    final long[] aux       = BUFFER_3x64_D;           // Auxiliary variable to be subtracted from the remainder at each step, == (2r + d) * d
    final long digit = findNextDigit(rootX2, remainder, aux, bitNumber); // digit = findDigit(); aux = 2 * r + d^2 * scale) * digit
    addDigit(root, digit, bitNumber);                 // root += digit >>> (128 - bitNumber);

    final boolean remainderIsEmpty = subtractBuff(aux, remainder);   // remainder -= aux; // aux can't be greater than remainder!
    if (remainderIsEmpty || bitNumber >= MAX_BITS_FOR_SQRT - 8)
      return Integer.MAX_VALUE;

    shift_8_bitsLeft(remainder);                      // remainder <<= 8;

    addDigitToBuff(rootX2, digit, bitNumber + 9);     // rootX2 += digit * 2; (shifted properly, by 9 bits right)
    bitNumber += DIGIT_LENGTH;                        // next digit position
    return bitNumber;
  } // private static int computeNextDigit(final long[] remainder, final long[] rootX2, final long[] root, int bitNumber) {

  /** Finds the next digit in the root and the corresponding {@code aux} value,
   * that will be subtracted from the remainder.
   * @param rootX2 doubled root found so far
   * @param remainder the remainder
   * @param aux auxiliary value to be subtracted from the remainder
   * @param rootBitNumber the position of the digit in the root
   * @return the digit found
   */
  private static long findNextDigit(long[] rootX2, long[] remainder, long[] aux, int rootBitNumber) {
    long digit = Long.divideUnsigned(remainder[0], rootX2[0]);
    digit = Math.min(digit, 0xFF);

    computeAux(digit, rootBitNumber, rootX2, aux);  // (root * 2 + digit * scale) * digit == 2rd + d^2 * scale
    while (compareBuffs64(aux, remainder) > 0) {    // aux > remainder, the digit is too large. Decrease the digit and recompute aux
                                                    // A very rare case: probability is less than 0.85%
      digit--;
      computeAux(digit, rootBitNumber, rootX2, aux);
    }
    return digit;
  } // private static long findNextDigit(long[] rootX2, long[] remainder, long[] aux, int rootBitNumber) {

  /**
   * Appends the found digit to the calculated root at the position specified by rootBitNumber.
   * for cases where the digit cannot fall on a word boundary
   * @param root a buffer containing the bits of the root found so far
   * @param digit a value of the digit to append
   * @param rootBitNumber the position to place the most significant bit of the digit at, counting from MSB
   */
  private static void addDigit(long[] root, long digit, int rootBitNumber) {
    final int buffIdx = rootBitNumber / 64;
    final int bitIdx = rootBitNumber % 64;
    root[buffIdx] += digit << 56 - bitIdx;
  } // private static void addDigit(long[] root, long digit, int rootBitNumber) {

  /**
   * Subtracts a number, represented as a big-endian array of longs, from another number of the same form
   * @param subtrahend subtrahend
   * @param minuend minuend that is replaced with the difference
   * @return  {@code true} if the result is 0 (i.e. the operands are equal)
   */
  private static boolean subtractBuff(long[] subtrahend, long[] minuend) {
    boolean diffIsEmpty = true;
    long diff, minnd;

    for (int i = subtrahend.length - 1; i >= 0; i--) {
      minnd = minuend[i];
      diff = minnd - subtrahend[i];

      if (Long.compareUnsigned(diff, minnd) > 0 ) { // Underflow.
      // It can't be the most significant word (i == 0), since aux can't be greater than remainder -- guaranteed by findNextDigit()
          if (minuend[i - 1] == 0)  subtrahend[i - 1]++;
          else                      minuend[i - 1]--;
      }
      minuend[i] = diff;
      diffIsEmpty &= diff == 0;
    }
    return diffIsEmpty;
  } // private static boolean subtractBuff(long[] buff_1, long[] buff_2) {

  /** Shifts the contents of the buffer left by 8 bits
   * @param buffer the buffer to shift
   */
  private static void shift_8_bitsLeft(long[] buffer) {
    for (int i = 0; i < buffer.length - 1; i++)
      buffer[i] = (buffer[i] << 8 | buffer[i + 1] >>> 56);
    buffer[buffer.length - 1] <<= 8;
  } // private static void shift_8_bitsLeft(long[] buffer) {

  /**
   * Appends the found digit to the calculated root at the position specified by rootBitNumber.
   * for cases where the digit can fall on a word boundary
   * @param root a buffer containing the bits of the root found so far
   * @param digit a value of the digit to append
   * @param rootBitNumber the position to place the most significant bit of the digit at, counting from MSB
   */
  private static void addDigitToBuff(long[] buff, long digit, int bitNumber) { //
    final int buffIdx = bitNumber / 64;
    final int bitIdx = bitNumber % 64;

    if (bitIdx <= 64 - 8) {                // The whole digit into one word
      buff[buffIdx] += digit << 64 - 8 - bitIdx;
    } else  {                            // Parts of the digit in different words
      buff[buffIdx] += digit >>> bitIdx + 8 - 64;  // 8 is digit length
      buff[buffIdx + 1] += digit << 128 - 8 - bitIdx;
    }
  } // private static void addDigitToBuff(long[] buff, long digit, int bitNumber) { //

  /** Computes the auxiliary value to be subtracted from the remainder: aux = 2rd + d^2 * scale.
   * Instead of the 'scale' the bit position {@code rootBitNumber} is used
   * @param digit the found next digit of the root
   * @param rootBitNumber the position of the digit in the root
   * @param rootX2 doubled root found so far
   * @param aux the buffer to be filled with the found value of aux
   */
  private static void computeAux(long digit, int rootBitNumber, long[] rootX2, long[] aux) {
    copyBuff(rootX2, aux);
    addDigitToBuff(aux, digit, rootBitNumber + 10);    // aux = rootX2 + digit * scale;
    multBufByDigit(aux, digit);                        // aux *= digit,
  } // private static void computeAux(long digit, int rootBitNumber, long[] rootX2, long[] aux) {

  /** Copies an array of longs from src to dst
   * @param src source
   * @param dst destination
   */
  private static void copyBuff(long[] src, long[] dst) {
    for (int i = 0; i < src.length; i++)
      dst[i] = src[i];
  } // private static void copyBuff(long[] src, long[] dst) {

  /** Compares two numbers
   * represented as arrays of {@code long} (big-endian, most significant bits in buff[0])
   * @param buff1 contains the first number to compare
   * @param buff2 contains the second number to compare
   * @return The result of comparison, according to general Java comparison convention
   */
  private static int compareBuffs64(long[] buff1, long[] buff2) {
    for (int i = 0; i < buff1.length; i++)
      if (buff1[i] != buff2[i])
        return Long.compareUnsigned(buff1[i], buff2[i]);
    return 0;
  } // private static int compareBuffs64(long[] buff1, long[] buff2) {

  /** Multiplies a number represented as {@code long[]} by a digit
   * (that's expected to be less than 32 bits long)
   * @param buff
   * @param digit
   */
  private static void multBufByDigit(long[] buff, long digit) {
    long carry = 0;
    for (int i = buff.length - 1; i >= 0; i--) {
      final long prodLo = (buff[i] & LOWER_32_BITS) * digit + carry;
      final long prodHi = (buff[i] >>> 32) * digit;
      carry = prodHi >>> 32;                    // will get beyond the boundary of the word
      final long product = prodLo + (prodHi << 32);
      if (Long.compareUnsigned(product, (prodHi << 32)) < 0)
        carry++;
      buff[i] = product;
    }
  } // private static void multBufByDigit(long[] buff, long digit) {

  /**
   * Multiplies 192 bits of the mantissa given in the arguments
   * {@code mantHi, mantLo, and thirdWord}, without implicit unity (only fractional part)
   * by 192 bits of the constant value of sqrt(2)
   * <br> uses static arrays
   * <b><i>BUFFER_4x64_A, BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_12x32</i></b>
   * @param mantHi 64 most significant bits of the fractional part of the mantissa
   * @param mantLo bits 64..127 of the fractional part of the mantissa
   * @param thirdWord 64 least significant bits of the fractional part of the mantissa
   * @return 192 bits of the fractional part of the product
   */
  private long[] multBySqrt2(long mantHi, long mantLo, long thirdWord) {

    BUFFER_4x64_A[0] = 0;
    BUFFER_4x64_A[1] = mantHi >>> 1 | HIGH_BIT;                     // to take implied unities into account:
    BUFFER_4x64_A[2] = mantLo >>> 1 | mantHi << 63;
    BUFFER_4x64_A[3] = thirdWord >>> 1 | mantLo << 63;

    final long[] product = multPacked3x64(BUFFER_4x64_A, SQRT_2_AS_LONGS); // SQRT_2_AS_LONGS contains implied unity in the MSB

    product[0] = product[1] << 2 | product[2] >>> 62;   // x * y = ((x >>> 1) * (y >>> 1)) << 2
    product[1] = product[2] << 2 | product[3] >>> 62;
    product[2] = product[3] << 2;
    return product;
  } // private long[] multBySqrt2(long mantHi, long mantLo, long thirdWord) {

  /**
   * Multiplies mantissas of quasidecimal numbers given as contents of arrays factor1 and factor2
   * (with exponent in buff[0] and 192 bits of mantissa in buff[1]..buff[3]),
   * replaces the mantissa of factor1 with the product. Does not affect factor1[0].<br>
   * uses static arrays <b><i>BUFFER_6x32_A, BUFFER_6x32_B, BUFFER_12x32</b></i>
   * @param factor1 an array of longs, containing the first factor
   * @param factor2 an array of longs, containing the second factor
   * @return factor1, whose mantissa is replaced with the product
   */
  private static long[] multPacked3x64(long[] factor1, long[] factor2) {
    multPacked3x64_simply(factor1, factor2);
    return pack_12x32_to_3x64(BUFFER_12x32, factor1);
  } // private static long[] multPacked3x64(long[] factor1, long[] factor2) {

  @SuppressWarnings("unused")
  private static void dummy() { // Just to prevent removing static import of 'say' from imports
    // 20.10.02 13:18:53 TO DO The imports used for debugging, to be removed after it's finished
//    say(AuxMethods.hexStr(BUFFER_6x32_A));
//    say(hexStr_u(BUFFER_6x32_A));
//    say(hexStr_(BUFFER_6x32_A));
  } // private static void dummy() { // Just to prevent removing static import of 'say' from imports

}
