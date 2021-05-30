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

import com.mvohm.quadruple.Quadruple;

/**
 * A {@code DataItem} is a container for a value that is used or generated when
 * testing an operation with a data sample.
 * The contained value may be the value of an input parameter, or the actual result of an operation,
 * or an expected result to compare with the actual result, etc.<br>
 * In most cases, it holds a {@code BigDecimal} value and the corresponding {@code Quadruple}
 * value that is the best possible approximation of the BigDecimal value.
 * It also may hold a value of a 'raw' type, that may be String, BigDecimal, Double, Integer or Long.
 * This capability is used when testing conversions from Quadruple to other types and
 * from other types to Quadruple, to store the results or input values, respectively.<br>
 * The value to store may be set using
 * {@link #withQuadValueOfString(String)},
 * {@link #withValueOf(Object)},
 * {@link #withRawValue(Object)}
 * {@link #withQuadruple(Quadruple)},
 * or {@link #withValueOfString(String)} methods.<br>
 *
 * In case of error occurring when testing, a DataItem with appropriate error message may be created with methods
 * {@link #withError(String)} or {@link #withError(String, String)}.<br>
 * A string representation of the data item or an error message can be obtained with {@link #toString()} method,
 * the values of specific fields can be obtained with
 * {@link #getBDValue()}, {@link #getQuadValue()}, {@link #getRawData()}, {@link #getStrValue()} methods.
 * The {@link #hasError()} methods indicates if the {@code DataItem} contains an error message.
 *
 * <br>
 * {@code DataItem}s with values or errors messages are used to collect statistics on test results
 * and to print them to the console while performing tests
 * (if the test execution code works in a talkative mood).
 *
 * @author M.Vokhmentsev
 *
 */
class DataItem {
  // Started 19.10.01 11:44:59
  private static final int DIGITS_AFTER_POINT     = 43; // to format BigDecimal values
  private static final int INTEGER_PART_LENGTH    = 3;  // including sign and decimal point, e.g. "-1."
  private static final int MAX_EXPONENT_LENGTH    = 11; // including "e", sign, and 9 digits

  // MAX_VALUE * (1 + 2^-129), the minimum value that should be treated as Quadruple.Infinty.
  // The values such that MAX_VALUE <= v < MAX_VALUE_THRESHOLD should be rounded down to Quadruple.MAX_VALUE,
  // if v >= MAX_VALUE_THRESHOLD, then it should be interpreted as Quadruple.Infinty.
  private static final BigDecimal MAX_VALUE_THRESHOLD    = MAX_VALUE.multiply(BD_ONE.add(BD_2$_129), MC_120_HALF_EVEN); // MAX_VALUE

  // used by TestResults.java
  static final int FULL_BD_LENGTH         = INTEGER_PART_LENGTH + DIGITS_AFTER_POINT + MAX_EXPONENT_LENGTH;

  private final String  role;       // the role this data item plays in the test: source data, result, expected result, etc.

  private String        strValue;
  private Object        rawData;
  private BigDecimal    bdValue;
  private Quadruple     quadValue;

  private String        errMsg;


  /***************************************************************************************
  ******* Constructors *******************************************************************
  ****************************************************************************************/

  /**
   * Creates a new instance with the given role
   * @param role the value to display as a role designation in the output string
   */
  public DataItem(String role) {
    this.role = role;
  }

  /***************************************************************************************
  ******* 21.01.20 New generation of the Setters *****************************************
  ****************************************************************************************/

  /**
   * Parses the given {@code String} and assigns the corresponding {@code Quadruple} value
   * to the {@code quadValue} field. If the value can be represented as a {@code BigDecimal} value,
   * assigns the corresponding {@code BigDecimal} value to the {@code bdValue} field, otherwise
   * assigns a {@code String} representation of the value to the {@code strValue} field.
   * Unlike #{@link DataItem#withValueOfString(String)},
   * assigns to the {@link #bdValue} field not the exact value expressed with the input string,
   * but the exact value of the Quadruple value derived from it, that has a limited precision and may
   * differ from the exact value expressed by the string by approximately 1.469e-39.
   *  <br>
   * If the input string can't be parsed, puts a relevant error message to the {@code errMsg} field
   * and the input string to the {@code strValue} field, leaving other fields empty (i.e. with {@code null} values).
   * <br>
   * To be usable for DataItems with expected values, distinguish word "error" as an indication of the fact
   * that the operation being tested, when applied to the given input data, was expected to cause an error
   * (like for a priori illegal source strings when testing String to Quadruple conversion)
   * @param strValue the {@code String} that's expected to signify the numeric value to be assigned
   * @return this instance with the fields modified as described above
   * @see DataItem#withValueOfString(String)
   */
  public DataItem withQuadValueOfString(String strValue) {
    if ("error".equals(strValue.trim().toLowerCase()))
      return withError("Error was expected.");
    final Quadruple quadValue = parseQuadruple(strValue);
    return withQuadruple(quadValue);
  }

  /**
   * Sets the fields of the instance to reflect the value
   * numerically equal to or closest to the value of the input parameter,
   * that can be a {@code BigDecomal}, a {@code String}, a {@code Double}, a {@code Long},
   * or an {@code Integer} instance.
   * Assigns the corresponding values to the {@code quadValue} and {@code bdValue} fields.
   * In cases of values that can't be represented by a {@code BigDecimal} (like NaN or Infinity),
   * instead of assigning to {@code bdValue}, assigns respective string designations to the
   * {@code strValue} field. If the parameter is a {@code String} and it can't be parsed as a numeric value,
   * the {@code quadValue} and {@code bdValue} remains empty and the {@code errMsg} field is assigned with
   * a relevant error message. In this case, {@code strValue} field takes the value of the input string
   * @param rawData the data whose value is to be assigned
   * @return this instance with the fields modified as described above
   */
  public DataItem withValueOf(Object rawData) {
    if (rawData == null) return this;
    if (rawData instanceof BigDecimal) return setBDValue((BigDecimal)rawData);
    if (rawData instanceof String)     return withValueOfString((String)rawData);
    if (rawData instanceof Double)     return setDoubleValue((Double)rawData);
    if (rawData instanceof Long)       return setBDValue(new BigDecimal((Long)rawData));
    if (rawData instanceof Integer)    return setBDValue(new BigDecimal((Integer)rawData));
    return withError("unsupported type: " + rawData.getClass().getSimpleName());
  }

  /** Assigns the given value, that may be of any type, to the {@code rawData} field of the instance.
   * Used to hold and display source values of various types that are to be converted to
   * {@code Quadruple} and results of converting {@code Quadruple} values to various other types.
   * @param rawData the value to assign
   * @return the same instance with the given value assigned to the {@code rawData} field
   */
  public DataItem withRawValue(Object rawData) {
    this.rawData = rawData;
    return this;
  }

  /** Fills the fields of the instance with values corresponding to the given {@code Quadruple} value.
   * Assigns the given value to the {@code quadValue} field; if the value may be represented as a {@code BigDecimal},
   * additionally assigns the corresponding {@code BigDecimal} value to the {@code bdValue} field, otherwise (in case of NaN or Infinity)
   * sets a {@code String} string representation of the given value in the {@code strValue} field.
   * @param value the {@code Quadruple} whose value is to be assigned to the fields of the instance
   * @return this instance with the fields modified as described above
   */
  public DataItem withQuadruple(Quadruple value) {
    if (value == null) return this;
    quadValue = value;
    if (value.isZero() && value.isNegative()) {// -0
      bdValue = new BigDecimal(0);
      strValue = getBDasString();
    } else if (value.exponent() != Quadruple.EXP_INF)  // An ordinary numeric value
      bdValue = bigDecimalValueOf(value);
    else                                        // NaN or Infinity
      strValue = quadToString43(value);           // assign the corresponding String representation
    return this;
  }

  /**
   * Parses the given {@code String} and assigns respective values
   * to the fields of this instance. If the value expressed with the string can be represented as a {@code BigDecimal} value,
   * assigns the exact {@code BigDecimal} value to the {@code bdValue} field, otherwise
   * assigns a {@code String} representation of the value to the {@code strValue} field.
   * Assigns a {@code Quadruple} value,
   * that can differ from the exact input value by approximately 1.469e-39 (due to limited precision of {@code Quadruple}),
   * to the {@code quadValue} field.<br>
   * Unlike {@link DataItem#withQuadValueOfString(String)}, does not align the {@code BigDecimal}
   * value to make it exactly equal to the value of the {@code quadValue},
   * but assigns to the {@link #bdValue} field the exact value expressed with the input string.
   *  <br>
   * If the input string can't be parsed, puts a relevant error message to the {@code errMsg} field
   * and the input string to the {@code strValue} field, leaving other fields empty (i.e. with {@code null} values).
   * <br>
   * To be usable for DataItems with expected values, distinguish word "error" as an indication of the fact
   * that the operation being tested, when applied to the given input data, was expected to cause an error
   * (like for a priori illegal source strings when testing String to Quadruple conversion)
   * @param strValue the {@code String} that's expected to signify the numeric value to be assigned
   * @return this instance with the fields modified as described above
   * @see DataItem#withQuadValueOfString(String)
   */
  public DataItem withValueOfString(String strValue) {
    final String lowerCased = strValue.toLowerCase().trim();
    if (lowerCased.contains("error"))
      return withError(strValue.replace("Error: ", ""));

    this.strValue = strValue;
    final String s = lowerCased.replaceAll("_", "");

    try {
      bdValue = new BigDecimal(s);
      quadValue = buildQuadruple(bdValue);
      if (bdValue.signum() == 0 && s.startsWith("-")) // respect zero's sign
        quadValue.negate();
    } catch (final Exception x) {                     // Not convertible to BigDecimal. Perhaps it's "NaN" or "Infinity" or alike
      quadValue = QUADRUPLE_CONSTS.get(lowerCased);   // Is it a Quadruple constant, like "min_value" or "NaN" or "infinity"?
      if (quadValue != null)                          // it may be a numeric constant designation, like "min_value"
        try { bdValue = bigDecimalValueOf(quadValue); // that is convertible to BigDecimal
        } catch (final Exception x1) {}               // "NaN" or "Infinity", bdValue remains empty
      else
        // Not a Quadruple constant, and can't be converted to BigDecimal.
        // Than it may be a valid floating point number with too great or too small exponent.
        // It should be converted to 0, -0, Infinity or -Infinity.
        try {
          final Double d = Double.parseDouble(s);
          quadValue = Consts.QUADRUPLE_CONSTS.get(d.toString().toLowerCase()); // NnN / Infinity
          if (quadValue == null && d == 0) {          // May be -0
              quadValue = new Quadruple();            // value = 0
              bdValue = bd(0);
              if (s.startsWith("-"))                  // respect zero's sign
                quadValue.negate();
            }
        } catch (final Exception x1) {} // Not convertible to Double. quadValue remains null
      if (quadValue == null)
        errMsg = String.format("Error %s converting %s to a numeric value", x.toString(), strValue);
    }
    return this;
  } // public DataItem withValueOfString(String strValue) {

  /** Sets the given error message, after prepending it with "Error: ", in the {@code errMsg} field.
   * @param errMsg the message to set
   * @return this instance with the {@code errMsg} field containing the error message
   */
  public DataItem withError(String errMsg) {
    this.errMsg = "Error: " + errMsg;
    return this;
  }

  /** Sets the given error message, after prepending it with "Error: ", in the {@code errMsg} field.
   * Additionally, sets the value of the {@code sourceStr} parameter in the {@code strValue} field
   * @param errMsg the message to set in the {@code errMsg} field
   * @param sourceStr the string to set in the {@code strValue} field
   * @return this instance with the {@code errMsg} field containing the error message
   * and the {@code strValue} field containing the value of the {@code sourceStr} parameter
   */
  public DataItem withError(String errMsg, String sourceStr) {
    this.strValue = sourceStr;
    return withError(errMsg);
  }


  /***************************************************************************************
  ******* Getters ************************************************************************
  ****************************************************************************************/

  /**
   * Returns the value of the {@code quadValue} field
   * @return the value of the {@code quadValue} field
   */
  public Quadruple getQuadValue() {
    return quadValue;
  }

  /**
   * Returns the value of the {@code strValue} field
   * @return the value of the {@code strValue} field
   */
  public String getStrValue() {
    return strValue;
  }

  /**
   * Returns the value of the {@code bdValue} field
   * @return the value of the {@code bdValue} field
   */
  public BigDecimal getBDValue() {
    return bdValue;
  }

  /**
   * Returns the value of the {@code rawData} field
   * @return the value of the {@code rawData} field
   */
  public Object getRawData() {
    return rawData;
  }

  /**
   * returns true, if the instance contains an error message
   * @return true, if the instance contains an error message, false otherwise
   */
  public boolean hasError() {
    return errMsg == null? false: true;
  }

  /**
   * Returns the value of the {@code errMsg} field
   * @return the value of the {@code errMsg} field
   */
  public String getErrMsg() {
    return errMsg;
  }

  /**
   * Returns a string that designate the role of the item and represents the contents of the fields
   * in a form suitable for printing to console when testing operations.<br>
   * If the item contains an error message, appends it to the role,
   * preceding it with the contents of the source string value (if it's not empty), e.g.<pre>
   * val: '.e35': Error java.lang.NumberFormatException converting .e35 to a numeric value</pre>
   * or <pre>
   * res: Error: java.lang.NumberFormatException: Can't convert POSITIVE_INFINITY to BigDecimal
   *             performing Quadruple.bigDecimalValue() on Infinity </pre>
   * Otherwise, if the {@code rawData} field is not empty, returns the role along with a string
   * form of the contents of the field, e.g.<pre>
   * src:  12345.0
   * </pre>
   * Otherwise, builds a String from the role designation,
   * a decimal representation of the BigDeciamal value from the {@code bdValue} field
   * or a string representation of a value in case if the value is not valid for BigDecimal,
   * and a hexadecimal representation of the {@code Quadruple} value from the {@code quadValue} field, e.g. <pre>
   * res:  1.2345000000000000000000000000000000000000000e+04        (+81c8_0000_0000_0000 0000_0000_0000_0000 e 8000_000c)
   * </pre> or <pre>
   * res: Infinity                                                  (+0000_0000_0000_0000 0000_0000_0000_0000 e ffff_ffff)
   * </pre>
   * The hexadecimal representation of the {@code Quadruple} value consists of the sign and
   * the hexadecimal forms of the {@code mantHi} and {@code mantLo} fields of the Quadruple instance,
   * followed by the hexadecimal form of its exponent after the letter 'e'.
   *    */
  @Override
  public String toString() {
    String s;
    if (errMsg != null) // an error
      s = (strValue != null)? "'" + strValue + "': " + errMsg : errMsg; // Возможно, есть исходная строка
    else if (rawData != null) {
      s = rawData.toString();
      if (Character.isDigit(s.charAt(0)))
        s = " " + s;          // Число без знака -- добавить пробел для выравнивания
      else if (s.startsWith("["))
        s = strValue;        // If rawData is an array, strValue contains its hex representation
    } else {
      // if bdVaue == null, then getBDasString() substitutes NaN или Infinity, depending on the quadValue value
      s = String.format("%-" + FULL_BD_LENGTH + "s (%s)", getBDasString(), quadValue == null? "null" : hexStr(quadValue));
    }
    return role + ": " + s;
  }

  /***************************************************************************************
  ******* Private methods  ***************************************************************
  ****************************************************************************************/

  /**
   * Parses an input string expected to represent a {@code Quadruple} value. In case of error
   * sets the appropriate error message in the the {@code errMsg} field and the puts the input string
   * in the {@code strValue} field.
   * @param strValue a string expected to represent a Quadruple value
   * @return a new {@code Quadruple} instance with the value obtained from the string, or null in case of error
   */
  private Quadruple parseQuadruple(String strValue) {
    String s = strValue.trim().toLowerCase();
    Quadruple quadValue = QUADRUPLE_CONSTS.get(s);
    if (quadValue == null) {
      s = s.replaceAll("_", "");
      try {
        quadValue = buildQuadruple(s);
      } catch (final Exception x2) {                        // buildQuadruple(strValue) couldn't parse it
        try {
          final Double d = Double.parseDouble(s);           // May be Double.parseDouble(strValue) can parse it
          quadValue = QUADRUPLE_CONSTS.get(d.toString().toLowerCase()); // NaN or Infinity
          if (quadValue == null && d == 0) {                // May be 0
            quadValue = new Quadruple();
            if (s.startsWith("-"))                          // respect zero's sign
              quadValue.negate();
          }
        } catch (final Exception x3) {                      // Double.parseDouble(strValue) couldn't parse it either
          errMsg = String.format("Error %s converting %s to a numeric value", x2.toString(), strValue);
          this.strValue = strValue;
        }
      }
    }
    return quadValue;
  }

  /**
   * Sets the input value in the {@code bdValue} field, and finds and sets in the {@code quadValue} field
   * the nearest possible approximation of that value.
   * @param bdValue the value to set
   * @return this instance with the fields modified as described above
   */
  private DataItem setBDValue(BigDecimal bdValue) { // 19.11.15 17:24:47
    if (bdValue.compareTo(MAX_VALUE_THRESHOLD) >= 0)
      withQuadruple(Quadruple.positiveInfinity());
    else if (bdValue.compareTo(MAX_VALUE_THRESHOLD.negate()) <= 0)
      withQuadruple(Quadruple.negativeInfinity());
    else {
      this.bdValue = bdValue;
      quadValue = buildQuadruple(bdValue);
    }
    return this;
  } // private DataItem setBDValue(BigDecimal bdValue) {

  /**
   * Sets the fields of the instance so that they represent the exact value of the input value
   * @param d the value to set
   * @return this instance with the fields modified as described above
   */
  private DataItem setDoubleValue(Double d) {
    if (Double.isNaN(d) || Double.isInfinite(d)) {
      final Quadruple q = Consts.QUADRUPLE_CONSTS.get(d.toString().toLowerCase());
      withQuadruple(q);
    } else if (Double.doubleToLongBits(d) == 0x8000_0000_0000_0000L) { // 20.12.18 18:15:59 "-0"
      withValueOfString("-0");
    } else {
      setBDValue(new BigDecimal(d));
    }
    return this;
  } // private DataItem setDoubleValue(Double d) {

  /**
   * Returns an uniformly formatted BigDecimal from the {@code bdValue} field, or, if it's empty,
   * a string representation of the Quadruple from the {@code quadValue} field (it may be NaN or Infinity that are not valid for BigDecimal)
   * @return
   */
  private String getBDasString() {
    if (bdValue == null) {                                // Deduce a String from quadValue
      final String s = stringOfSpecQuadValue(quadValue);  // May be it's NaN or Infinity
      if (s != null) return s;
      bdValue = bigDecimalValueOf(quadValue);
    }

    if (bdValue.signum() == 0) bdValue = bdValue.setScale(0); // Otherwise may show incorrect exponent for zero values
    final String sign = bdValue.signum() < 0 ? "" :           // To right-align: It's negative, already includes the minus
                        ( quadValue != null &&  quadValue.isNegative() &&  bdValue.signum() == 0) ? // -0?
                            "-" : // It's -0, "-" should be added
                            " ";  // it's non-negative, " " should be added
    return String.format("%s%." + DIGITS_AFTER_POINT + "e", sign, bdValue);
  }

  /**
   * Returns a string representation for a value that isn't valid for BigDecimal -- NaN or Infinity
   */
  private String stringOfSpecQuadValue(Quadruple quadValue) {
    if (quadValue == null)
      return "---";
    if (quadValue.exponent() == Quadruple.EXP_INF) {
      if ((quadValue.mantHi() | quadValue.mantLo()) != 0)
        return "NaN";
      if (quadValue.isNegative())
        return "-Infinity";
      return "Infinity";
    }
    // quadValue.exponent() != Quadruple.EXP_INF)
    assert false: "Regular quad value and bdValue == null";
    return null;    // Actually should never happen, since it may be invoked only in case when bdValue could not be deduced from quadValue
  }


  public DataItem withString(String s) {
    this.strValue = s;
    return this;
  }


}

