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

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.mvohm.quadruple.test.AuxMethods.*;
import static com.mvohm.quadruple.test.Consts.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.*;

/**
 * Auxiliary static methods that generate various collections of test data
 * used by DataProviders
 *
 * @author M.Vokhmentsev
 */
public class DataGenerators {

  /* ********************************************************************************
  /* package level methods (marked as public for clarity) ***************************
  /* ********************************************************************************/

  /**
   * Takes a list of {@code Strings} and copies all the items of it into
   * a new list, inserting {@code null} after each of them.
   * The resulting list looks like {@code (v1, null, v2, null, ...)},
   * where {@code v1, v2...} are the values from the first list.
   * @param inpList a dense list with values
   * @return a new list where the values from the input list are supplemented with null items
   */
  public static List<String> insertNulls(List<String> inpList) {
    final List<String> result = new ArrayList<>();
    for (final String s : inpList) {
      result.add(s);
      result.add(null);
    }
    return result;
  } // public static List<String> insertNulls(List<String> inpList) {

  /**
   * Takes a list of {@code Strings} and copies all the items of it into
   * a new list, inserting {@code null} after each of them.
   * Additionally inserts the given {@code header} and {@code null} in the beginning of the resulting list.
   * The resulting list looks like {@code (h, null, v1, null, v2, null, ...)},
   * where {@code h} is the {@code header}, and {@code v1, v2...} are the values from the
   * first list. The input list remains unchanged.
   * @param header the header to insert in the the beginning of the list
   * @param inpList the list to copy the values from
   * @return the list that contains the header and all the items from the input list,
   * with nulls inserted after each item
   */
  public static List<String> insertNulls(String header, List<String> inpList) {
    final List<String> result = insertNulls(inpList);
    result.add(0, null);
    result.add(0, header);
    return result;
  } // public static List<String> insertNulls(String header, List<String> inpList) {

  /**
   * Appends the given strings to the end of the list.
   * @param list the list to add the comments to
   * @param comments the comment lines to add to the list
   * @return the input list with the given strings added to the end of it
   */
  public static List<String> appendComment(List<String> list, String...comments) {
    for (final String s: comments)
      list.add(s);
    return list;
  } // public static List<String> addComment(List<String> list, String...comments) {

  /**
   * Returns a list of {@code Strings} representing a sequence of values growing
   * from <span style="white-space:nowrap">{@code (baseValue + (startDelta * 2^-128))}</span>
   * to <span style="white-space:nowrap">{@code (baseValue + (endDelta * 2^-128))}</span>
   * inclusive, with a step of {@code 2^-128}, which corresponds to the least significant bit
   * of the of the mantissa of the corresponding {@code Quadruple} value.
   * @param baseValue the value to add the delta to, expressed as a {@code String}
   * @param startDelta the factor to multiply the LSB by, to get the delta
   *          to add to the base at the first iteration
   * @param endDelta the factor to multiply the LSB by, to get the delta to add
   *          to the base at the last iteration
   * @return a list containing the sequence of the values described above, expressed as strings
   */
  public static List<String> sequence(String baseValue, int startDelta, int endDelta) { //
    final List<String> result = new ArrayList<>(endDelta - startDelta + 1);
    final BigDecimal  sourceValue = new BigDecimal(baseValue);
    BigDecimal bdDelta;

    for (int delta = startDelta; delta <= endDelta; delta++) {
      bdDelta = (delta >= 0 || sourceValue.compareTo(BD_ONE) > 0) ?
          BD_2$_128.multiply(new BigDecimal(delta)):
          BD_2$_129.multiply(new BigDecimal(delta));
      result.add(sourceValue.add(bdDelta, MC_120_HALF_EVEN).toString());
    }
    return result;
  } // public static List<String> sequence(String baseValue, int startDelta, int endDelta) { //

  /**
   * Returns a list of {@code Strings} representing a sequence of values growing from
   * <span style="white-space:nowrap">{@code (baseValue* (1 + startDelta * 2^-128))}</span>
   * to <span style="white-space:nowrap">{@code (baseValue* (1 + endDelta * 2^-128))}</span>
   * inclusive, with a step of
   * <span style="white-space:nowrap">{@code (baseValue* step * 1^-128)}.</span><br>
   * In other words, the resulting list contains the following values:<pre>
   * baseValue * (1 + (startDelta * 2^-128)),
   * baseValue * (1 + (startDelta + (1 * step * 2^-128))),
   * baseValue * (1 + (startDelta + (2 * step * 2^-128))),
   * ...
   * baseValue * (1 + (ensDelta - (1 * step * 2^-128))),
   * baseValue * (1 + endDelta * 2^-128)</pre>
   * @param baseValue   a factor to multiply by the growing coefficient to get the next value
   *                    of the sequence
   * @param startDelta  addition to one, in 2^-128, to find the starting value
   *                    of the the growing coefficient
   * @param endDelta    addition to one, in 2^-128, to find the ending value of the the growing
   *                    coefficient
   * @param step        defines the difference between adjacent values, so
   *                    that v1 = v0 + baseValue* step * 2^-128
   * @param addComment  a flag signifying that comment lines with human-readable expressions should be
   *                    added before each value in the sequence
   * @return a list of string representations of the values described above
   */
  public static List<String> sequence(BigDecimal baseValue, double startDelta, double endDelta,
                                      double step, boolean addComment) {
    final List<String> result = new ArrayList<>();
    BigDecimal bdDelta;

    for (double delta = startDelta; delta <= endDelta; delta += step) {
      delta = Math.round(delta * 1e14)/1e14;
      bdDelta = BigDecimal.valueOf(delta).multiply(BD_2$_128);
      if (addComment)
        result.add(String.format("// %.9e * (1 %s %s * 2^-128)", baseValue, delta >= 0? "+" : "-", Math.abs(delta)));
      result.add(BD_ONE.add(bdDelta, MC_120_HALF_EVEN).multiply(baseValue, MC_120_HALF_EVEN).toString());
    }
    return result;
  } // public static List<String> sequence(BigDecimal baseValue, double startDelta, double endDelta, double step, boolean addComment) {

  /**
   * Returns a list of {@code Strings} representing a sequence of values growing from
   * <span style="white-space:nowrap">{@code ((baseValue+ startDelta * 2^-128) * 2^exp)}</span>
   * to <span style="white-space:nowrap">{@code ((baseValue+ endDelta * 2^-128) * 2^exp)}</span>
   * inclusive, with a step of
   * <span style="white-space:nowrap">{@code (step * 2^-128 * 2^exp)}.</span><br>
   * In other words, the resulting list contains the following values:<pre>
   * {@code (baseValue + startDelta * 2^-128) * 2^exp},
   * {@code (baseValue + (startDelta + 1 * step) * 2^-128) * 2^exp},
   * {@code (baseValue + (startDelta + 2 * step) * 2^-128) * 2^exp},
   * ...
   * {@code (baseValue + (ensDelta - 1 * step) * 2^-128) * 2^exp},
   * {@code (baseValue + endDelta * 2^-128) * 2^exp}</pre>
   * @param baseValue   a base value to add the growing addition to before raising to the power of two
   * @param startDelta  a delta that is to be multiplied by 2^-128 before adding to sourceValue
   *                    and raising to the power of two to form the starting value of the sequence
   * @param endDelta    a delta that is to be multiplied by 2^-128 before adding to sourceValue
   *                    and raising to the power of two to form the ending value of the sequence
   * @param step        defines the difference between adjacent values, so that v1 = v0 + step * 2^-128 * 2^exp
   * @param exponent    defines the power of two of the factor 2^exp
   * @param addComment  a flag signifying that comment lines with human-readable expressions should be added before each value in the sequence
   * @return a list of string representations of the values described above
   */
  public static List<String> sequence(BigDecimal baseValue, double startDelta, double endDelta,
                                       double step, int exponent, boolean addComment) {
    final List<String> result = new ArrayList<>();
    BigDecimal bdDelta;
    final BigDecimal powerOf2 = powerOfTwo(exponent, MC_120_HALF_EVEN);

    for (double delta = startDelta; delta <= endDelta; delta += step) {
      delta = Math.round(delta * 1e14)/1e14;
      bdDelta = BigDecimal.valueOf(delta).multiply(BD_2$_128);
      if (addComment)
        result.add(String.format("// (%.9e %s %s * 2^-128) * %.9e)",
                                  baseValue, delta >= 0? "+" : "-", Math.abs(delta), powerOf2));
      result.add(baseValue.add(bdDelta, MC_120_HALF_EVEN).multiply(powerOf2, MC_120_HALF_EVEN).toString());
    }

    return result;
  } // public static List<String> sequence (BigDecimal baseValue, double startDelta, double endDelta, double step,

  /**
   * Generates and returns a series of sequences of form

   <div style="margin-left:20px; margin-top:10px; margin-bottom:10px;">
   <code>
       s[0] + sd * 2^-128,&nbsp;&nbsp;s[0] + (sd + 1) * 2^-128,  ... s[0] + ed * 2^-128;<br>
       s[1] + sd * 2^-128,&nbsp;&nbsp;s[1] + (sd + 1) * 2^-128,  ... s[1] + ed * 2^-128;<br>
       ... ; <br>
       s[n-1] + sd * 2^-128,&nbsp;&nbsp;s[n-1] + (sd + 1) * 2^-128,  ... s[n-1]+ ed * 2^-128,<br>
   </code>
   </div>

   * <div style="margin-left:0px; margin-top:0px; margin-bottom:0px;">
   * where <b>{@code s[0] .. s[n-1]}</b> are the elements of baseValues,
   * <b>{@code sd}</b> is the value of the {@code startDelta} parameter,
   * and <b>{@code ed}</b> is the value of the {@code endDelta} parameter.</div>
   <!-- Seems like JavaDoc can't handle div's in brief method descriptions -->
   * @param startDelta  the starting delta, in the units of 2^-128
   * @param endDelta    the ending delta, in the units of 2^-128
   * @param baseValues   an array of string representations of the base values to generate the sequences
   * @return a list of string representations of the values described above
   */
  public static List<String> sequences(int startDelta, int endDelta, String... baseValues) {
    final List<String> result = new ArrayList<>();
    for (final String s: baseValues)
      result.addAll(sequence(s, startDelta, endDelta));
    return result;
  } // public static List<String> sequences(int startDelta, int endDelta, String... baseValues) {

  /**
   * Multiplies each of the the values, represented by the items of {@code inputList},
   * by the given {@code factor}, and returns a list containing string representations
   * of the corresponding products.<br>
   * The input list remains unchanged.
   * @param inputList a list of strings representing the values to be multiplied by the given {@code factor}
   * @param factor a factor to multiply the values from the {@code inputList}
   * @return a list of strings representing the products
   */
  public static List<String> multiply(List<String> inputList, BigDecimal factor) {
    final List<String> result = new ArrayList<>();
    for (final String s: inputList) {
      try {
        result.add(new BigDecimal(s).multiply(factor, MC_120_HALF_EVEN).toString());
      } catch (final NumberFormatException x) {
        result.add(s);
      }
    }
    return result;
  } // public static List<String> multiply(List<String> inputList, BigDecimal factor) {

  /**
   * Multiplies each of the the values, represented by the items of {@code inputList},
   * by the given {@code factor}, and returns a list containing string representations
   * of the corresponding products.<br>
   * The input list remains unchanged.
   * @param inputList a list of strings representing the values to be multiplied by the given {@code factor}
   * @param factor a factor to multiply the values from the {@code inputList}
   * @return a list of strings representing the products
   */
  public static List<String> multiply(List<String> inputList, double factor) {
    return multiply(inputList, BigDecimal.valueOf(factor));
  } // public static List<String> multiply(List<String> inputList, double factor) {

  /**
   * Multiplies each of the given {@code factors} by each of the the values,
   * represented by the items of {@code inputList}, and returns a list containing
   * string representations of the corresponding products.<br>
   * The resulting list contains the products in the following order:
   * <pre> v[0] * f[0], v[1] * f[0], ... v[n-1] * f[0],
   * v[0] * f[1], v[1] * f[1], ... v[n-1] * f[1],
   * ...
   * v[0] * f[m-1], v[1] * f[m-1], ... v[n-1] * f[m-1]</pre>
   * Both input arguments remain unchanged.
   * @param inputList a list of strings representing the values to be multiplied by the given {@code factor}
   * @param factors an array of factors to multiply the values from the {@code inputList}
   * @return a list of strings expressing the products, v[0] * f[0], v[1] * f[0], ... v[n-1] * f[0],
   * v[0] * f[1], v[1] * f[1], ... v[n-1] * f[1], ... v[0] * f[m-1], v[1] * f[m-1], ... v[n-1] * f[m-1]
   */
  public static List<String> multiplyByFactors(List<String> inputList, double... factors) {
    final List<String> list = new ArrayList<>();
    for (final double factor: factors) {
      list.add("// multiplied by " + factor);
      list.addAll(multiply(inputList, factor));
    };
    return list;
  } // public static List<String> multiplyByFactors(List<String> inputList, double... factors) {

  /**
   * Generates and returns a series of sequences, each of which is a number of values,
   * successively approaching and then moving away from the base value, that is
   * a product of a value from the {@code mantissas} list multiplied by {@code Quadruple.MIN_VALUE};
   * such sequences are repeated for a number of values of binary exponents within
   * the range of subnormal {@code Quadruple} values.<br><br>
   * The result for maxDelta = 3e-39 and minDelta = 3e-40, for example,
   * would look as the following:<pre>
   *
   *
   * ((m * 2^e + a) - maxDelta) * MIN_VALUE,
   * ((m * 2^e + a) - maxDelta / 10) * MIN_VALUE,
   * ...
   * ((m * 2^e + a) + maxDelta / 10) * MIN_VALUE,
   * ((m * 2^e + a) + maxDelta) * MIN_VALUE,

   (0.5 - 3E-39) * MIN_VALUE,
   (0.5 - 3E-40) * MIN_VALUE,
    0.5 * MIN_VALUE,
   (0.5 + 3E-40) * MIN_VALUE,
   (0.5 + 3E-39) * MIN_VALUE,

   (1.5 - 3E-39) * MIN_VALUE
   (1.5 - 3E-40) * MIN_VALUE,
    1.5 * MIN_VALUE,
   (1.5 + 3E-40) * MIN_VALUE,
   (1.5 + 3E-39) * MIN_VALUE,

   (2.5 - 3E-39) * MIN_VALUE,
   (2.5 - 3E-40) * MIN_VALUE,
    2.5 * MIN_VALUE,
   (2.5 + 3E-40) * MIN_VALUE,
   (2.5 + 3E-39) * MIN_VALUE,
   ...
   (32767.5 - 3E-39) * MIN_VALUE,
   (32767.5 - 3E-40) * MIN_VALUE,
    32767.5 * MIN_VALUE,
   (32767.5 + 3E-40) * MIN_VALUE,
   (32767.5 + 3E-39) * MIN_VALUE,

   (40959.5 - 3E-39) * MIN_VALUE,
   (40959.5 - 3E-40) * MIN_VALUE,
    40959.5 * MIN_VALUE,
   (40959.5 + 3E-40) * MIN_VALUE,
   (40959.5 + 3E-39) * MIN_VALUE,
   ...
   (340282366920938463463374607431768211455.5 - 3E-39) * MIN_VALUE
   (340282366920938463463374607431768211455.5 - 3E-40) * MIN_VALUE
    340282366920938463463374607431768211455.5 * MIN_VALUE
   (340282366920938463463374607431768211455.5 + 3E-40) * MIN_VALUE
   (340282366920938463463374607431768211455.5 + 3E-39) * MIN_VALUE
</pre>
   * Used to test the precision of rounding subnormal values that are close to {@code (n + 0.5) * 2^-2147483774},
   * which is performed by {@code String} to {@code Quadruple} and {@code String} to {@code Quadruple} conversions.
   * @param mantissas a number of mantissas to form the base values for the subsequences
   * @param addition a constant to be added to the mantissa to form the base value of the subsequence
   * @param maxDelta maximum magnitude of the delta to be added or subtracted to the base value before multiplying it by MIN_VALUE;
   * @param minDelta minimum magnitude of the delta to be added or subtracted to the base value before multiplying it by MIN_VALUE;
   * @return a list of strings expressing the values described
   */
  public static List<String> subnormalVicinities(String[] mantissas, double addition, double maxDelta, double minDelta) {
    final List<String> result = new ArrayList<>();
    prevMantissa = null;                                  // A static variable used to suppress duplicating of values
    for (int exponent = 0; exponent < 128; exponent++) {
      result.addAll(subnormalVicinities(mantissas, addition, maxDelta, minDelta, exponent));
    }
    return result;
  } // public static List<String> subnormalVicinities(String[] mantissas, double addition, double maxDelta, double minDelta) {

  /**
   * Generates and returns a series of sequences of values, such that in each sequences the values are successively
   * approaching and then moving away from the base value of the sequence,
   * calculated as {@code (m[i] + (a + delta) * 2^-128)}, where {@code m[i]} is
   * an element of the {@code mantissas} array, {@code a} is the value of the
   * {@code addition} parameter, and {@code delta} grows from {@code -maxDelta}
   * up to {@code -minDelta} decreasing in module ten times each step, passes
   * through 0, and then grows from {@code minDelta} up to {@code maxDelta}
   * increasing ten times each step.<br>
   * For m[i] = 1.25, addition = 0.5, maxDelta = 3e-39, and minDelta = 3-40, the
   * corresponding fragment of the resulting list would be the following:
   *
   * <pre>
     1.25 + (0.5 - 3E-39) * 2^-128,
     1.25 + (0.5 - 3E-40) * 2^-128,
     1.25 +  0.5 * 2^-128,
     1.25 + (0.5 + 3E-40) * 2^-128,
     1.25 + (0.5 + 3E-39) * 2^-128,
   * </pre>
   *
   * Used to test the precision of rounding normal values whose mantissas are
   * close to {@code 1 + (n + 0.5 * 2^-128)}, i.e. the values that differ from
   * the exact values of the nearest {@code Quadruple} values by deltas that are
   * close to the half of the least significant bit of the mantissa.
   *
   * @param mantissas
   *          a number of mantissas to form the base values for the sequence
   * @param addition
   *          a constant to be added to the mantissa to form the base value of
   *          the sequence, in units of the LSB of the mantissa
   * @param maxDelta
   *          the magnitude of the maximum difference between the base value and
   *          an element of the generated sequence, in units of the LSB of the
   *          mantissa
   * @param minDelta
   *          the magnitude of the minimal difference between the base value and
   *          an element of the generated sequence, in units of the LSB of the
   *          mantissa
   * @return a list of strings representing the values described above
   */
  public static List<String> vicinities(String[] mantissas, double addition, double maxDelta, double minDelta) {
    final ArrayList<String> result = new ArrayList<>();
    for (final String mant: mantissas) result.addAll(vicinityOfNormal(mant, addition, maxDelta, minDelta));
    return result;
  } // public static List<String> vicinities(String[] mantissas, double addition, double maxDelta, double minDelta) {

  /**
   * Generates and returns a list consisting of the values from the input list multiplied by powers
   * of two whose exponents include all combinations of bits like 0b0_1000, 0b0_1111, 0b1_0000, 0b1_1111,
   * so that the calculation of the corresponding powers using both only a single
   * factor from the table of powers of 2, and a product of all possible factors.<br>
   * The resulting list looks like the following:<pre>
v[0] * 2^-2147483646, v[1] * 2^-2147483646, ..., v[n-1] * 2^-2147483646,
v[0] * 2^-1073741824, v[1] * 2^-1073741824, ..., v[n-1] * 2^-1073741824,
...,
v[0] * 2^-1, v[1] * 2^-1, ..., v[n-1] * 2^-1,
v[0] * 2^0, v[1] * 2^0, ..., v[n-1] * 2^0,
v[0] * 2^1, v[1] * 2^1, ..., v[n-1] * 2^1,
...,
v[0] * 2^1073741824, v[1] * 2^1073741824, ..., v[n-1] * 2^1073741824,
v[0] * 2^2147483647, v[1] * 2^2147483647, ..., v[n-1] * 2^2147483647,
</pre>
   *
   * @param values a list of values to be multiplied by the powers of two
   * @return a list of strings representing the values described above
   */
  public static List<String> multByAllPowersOfTwo(List<String> values) {
    final ArrayList<String> result = new ArrayList<>();
    final List<Integer> exponents = allExponents();
    for (final int exp: exponents) {
      result.add(exp < 0?
                    String.format("// n * 2 ^ %s (-%s, %s -> %s)",  exp, hexStr(-exp), hexStr(exp), hexStr(exp + EXP_0Q)):
                    String.format("// n * 2 ^ %s (%s -> %s)",       exp, hexStr(exp), hexStr(exp + EXP_0Q)));
      result.add(null);

      for (final String value: values)
        result.add(multByPowerOfTwo(value, exp));
    }
    return result;
  } // public static List<String> multByAllPowersOfTwo(List<String> values) {

  /** *********************************************************************************
   * Generates Cartesian squares of the set of operands including special values like
   * {@code NaN}, {@code Infinity}, and {@code -0}, to form a set of operands
   * to test binary operators on various combinations of the operands. Each item of the resulting lists,
   * specific for different operations, includes a pair of operands from the set and,
   * in cases when the expected result of the tested operation applied
   * to a certain pair of of the operands is -0, the explicit "-0" as the expected result.
   ***********************************************************************************/
  static class CartesianSquare {

    /**
     * The values of the operands to form the pairs of operands to test binary operations
     */
    private static String[] specialValies = new String[] {
      "-Infinity",
      "-30.0",
      "-5.0",
      "-0.0",
      "0.0",
      "6.0",
      "40.0",
      "Infinity",
      "NaN",
    }; // private static String[] specialValies = new String[] {

    /**
     * Generates and returns a list of pairs of operands for testing {@code min()} function.
     * Since findExpectedResult() in the tester class returns BigDecimal, its result can't be -0.<br>
     * Thus we have to provide -0 as the expected value for the pairs of operands whose min() should be -0.
     * So, replace nulls with "-0.0" wherever the function result equals -0.
     * @return a Cartesian square of the {@link #specialValies} with values of {@code -0}
     * wherever the tested operation is expected to result in -0.
     * */
    static List<String> specialValuesForMin()  {
      return cartesianSquareWithMinusZeros(specialValies, (a, b) -> Math.min(a, b));
    } // static List<String> specialValuesForMin() {

    /**
     * Generates and returns a list of pairs of operands for testing {@code max()} function.
     * Since findExpectedResult() in the tester class returns BigDecimal, its result can't be -0.<br>
     * Thus we have to provide -0 as the expected value for the pairs of operands whose max() should be -0.
     * So, replace nulls with "-0.0" wherever the function result equals -0.
     * @return a Cartesian square of the {@link #specialValies} with values of {@code -0}
     * wherever the tested operation is expected to result in -0.
     * */
    static List<String> specialValuesForMax()  {
      return cartesianSquareWithMinusZeros(specialValies, (a, b) -> Math.max(a, b));
    } // static List<String> specialValuesForMin() {


    /**
     * Generates and returns a list of pairs of operands for testing the addition.
     * Since findExpectedResult() in the tester class returns BigDecimal, its result can't be -0.<br>
     * Thus we have to provide -0 as the expected value for the pairs of summands whose sum should be -0.
     * So, replace nulls with "-0.0" wherever the sum equals -0.
     * @return a Cartesian square of the {@link #specialValies} with values of {@code -0}
     * wherever the tested operation is expected to result in -0.
     * */
    static List<String> specialValuesForAddition() {
      return cartesianSquareWithMinusZeros(specialValies, (a, b) -> a + b);
    } // static List<String> specialValuesForAddition() {

    /**
     * Generates and returns a list of pairs of operands for testing the subtraction.
     * Since findExpectedResult() in the tester class returns BigDecimal, its result can't be -0.<br>
     * Thus we have to provide -0 as the expected value for the pairs of operands whose difference should be -0.
     * So, replace nulls with "-0.0" wherever the difference equals -0.
     * @return a Cartesian square of the {@link #specialValies} with values of {@code -0}
     * wherever the tested operation is expected to result in -0.
     * */
    static List<String> specialValuesForSubtraction() {
      return cartesianSquareWithMinusZeros(specialValies, (a, b) -> a - b);
    } // static List<String> specialValuesForSubtraction() {

    /**
     * Generates and returns a list of pairs of operands for testing the multiplication.
     * Since findExpectedResult() in the tester class returns BigDecimal, its result can't be -0.<br>
     * Thus we have to provide -0 as the expected value for the pairs of factors whose product should be -0.
     * So, replace nulls with "-0.0" wherever the product equals -0.
     * @return a Cartesian square of the {@link #specialValies} with values of {@code -0}
     * wherever the tested operation is expected to result in -0.
     * */
    static List<String> specialValuesForMultiplication() {
      return cartesianSquareWithMinusZeros(specialValies, (a, b) -> a * b);
    } //static List<String> specialValuesForMultiplication() {

    /**
     * Generates and returns a list of pairs of operands for testing the division.
     * Since findExpectedResult() in the tester class returns BigDecimal, its result can't be -0.<br>
     * Thus we have to provide -0 as the expected value for the pairs of factors whose quotient should be -0.
     * So, replace nulls with "-0.0" wherever the quotient equals -0.
     * @return a Cartesian square of the {@link #specialValies} with values of {@code -0}
     * wherever the tested operation is expected to result in -0.
     * */
    static List<String> specialValuesForDivision() {
      return cartesianSquareWithMinusZeros(specialValies, (a, b) -> a / b);
    } // static List<String> specialValuesForDivision() {


    /**
     * Builds a list of all possible pairs of operands from the given array of values,
     * supplemented with "-0.0" (if the expected result of the tested operation applied to the operands is -0)
     * or with null (in all other cases).
     * @param operands an array containing string representations of double operands to combine
     * @param operation the operation whose result affects the third item of the triplet (the expected result of the operation)
     * @return a newly-created list with the values described above
     */
    private static List<String> cartesianSquareWithMinusZeros(String[] operands, BinaryOperator<Double> operation) {
      final List<String> result = new ArrayList<>();
      for (final String s1: operands) {
        for (final String s2: operands) {
          result.add(s1);
          result.add(s2);
          result.add(isMinusZero(s1, s2, operation)? "-0.0" : null);
        };
      };
      return result;
    } // private static List<String> cartesianSquareWithMinusZeros(String[] operands, BinaryOperator<Double> operation) {

    /**
     * Checks whether the given operation applied to the given operands results in -0
     * @param op1str 1st operand as a String
     * @param op2str 2nd operand as a String
     * @param operation the operation to perform on the operands
     * @return true, if the result of the operation is -0, false otherwise
     */
    private static boolean isMinusZero(String op1str, String op2str, BinaryOperator<Double> operation) {
      final double op1 = Double.parseDouble(op1str);
      final double op2 = Double.parseDouble(op2str);
      // ??? is there a neater way to check if a value is -0?
      return Double.doubleToLongBits(operation.apply(op1, op2)) == 0x8000_0000_0000_0000L;
    } // private static boolean isMinusZero(String op1str, String op2str, BinaryOperator<Double> operation) {

  } // static class CartesianSquare {

  /** *********************************************************************************
   * The methods of this class generate random values and various random sequences,
   * including lists of pairs of random values suitable for testing binary operations
   ************************************************************************************/
  static class Randoms {

    private static Random random;

    /**
     * Initializes the random generator
     */
    static {
        random = new Random();
        final long seed =
//            random.nextLong(); // for the real (unpredictable) random value
            -8467433414517167298L;  // for repeatability
//            2345678901234567890L;   // for repeatability
//            345345345345L;          // for repeatability
//            1234567890123456789L;   // for repeatability
//            987654321098765432L;    // for repeatability

//        say("Random seed = " + seed);
        random = new Random(seed);
    } // static {

    /**
     * Generates and returns a list of string representations of
     * random {@code BigDecimal} values that fall within the range valid for {@code Quadruple}.
     * @param count the amount of random numbers to be generated
     * @return the generated list, with nulls inserted after each item
     */
    public static List<String> simpleRandomsWithNulls(int count) {
      return randList("Simple random generator", count, ()->randomBigDecimalString());
    } // static List<String> simpleRandomsWithNulls(int count) {

    public static List<String> randomIeee754Quadruples(int count ) {
      return randList("Simple random IEEE-754 generator", count, ()->randomIeee754String());
    }

    /**
     * Generates and returns a list with string representations of
     * random {@code BigDecimal} values that fall within the range valid for @code double},
     * such that the mantissa of each value differs from the mantissa of the nearest {@code double} value by
     * {@code 1^-53} (half of least significant bit of the double's mantissa) or by {@code 1^-53 - 1^-128}.
     * Used to test the correctness of the rounding performed during the conversion from {@code Quadruple} to {@code double}.
     * @param count the amount of random numbers to be generated
     * @return the generated list, with nulls inserted after each item
     */
    public static List<String> randDoublesWithHalfLSB(int count) {
      return randList("Randoms to test rounding of doubles at N + LSB * 0.5", count, ()->randDoubleWithHalfLsb());
    } // static List<String> randDoublesWithHalfLSB(int count) {

    /**
     * Generates and returns a list of strings with pairs of {@code BigDecimal}
     * values that fall within the range valid for {@code Quadruple}.
     * The first item of each pair is a random {@code BigDecimal} value such
     * that its mantissa differs from the mantissa of the nearest
     * {@code Quadruple} value by <br><span class="nowrap">{@code 2^-129 - 2^(-129 + expOfDelta)},</span><br>
     * and the mantissa of the second value differs from the mantissa of the same {@code Quadruple}
     * value by <br><span class="nowrap">{@code 2^-129}.</span><br>
     * In other words, the first value of the pair can be written as<br>
     * <span class="nowrap">{@code (1 + (n + 0.5 - 2^(expOfDelta-1)) * 2^-128) * 2^N},</span><br> and the second one can
     * be written as <br><span class="nowrap">{@code (1 + (n + 0.5) * 2^-128) * 2^N}.</span> <br>
     * During the conversion from {@code BigDecimal} to {@code Quadruple} the first value
     * should get rounded down to {@code (1 + n * 2^-128) * 2^N}, and the second
     * should get rounded up to {@code (1 + (n + 1) * 2^-128) * 2^N}.<br>
     * Used to test the correctness of the rounding performed during the conversion from
     * {@code BigDecimal} to {@code Quadruple}.
     *
     * @param count
     *          the amount of random numbers to be generated
     * @param expOfDelta
     *          exponent of the power of two that should be subtracted from the
     *          LSB of the mantissa of the first value of the pair
     * @return the generated list, with nulls inserted after each value
     */
    public static List<String> randQuadruplesWithHalfLSB(int count, int expOfDelta) {
      return randList(String.format("Randoms to test rounding of Quadruples: N + LSB * 0.5 vs N + LSB * (0.5 - 2^%s)",
                      expOfDelta),
                      count, ()->randQuadrupleWithHalfLsb(expOfDelta));
    } // static List<String> randQuadruplesWithHalfLSB(int count, int expOfDelta) {

    /**
     * Generates and returns a list of strings representing random {@code Quadruple} values
     * falling within the range valid for {@code double}s.
     * @param count the amount of random numbers to be generated
     * @return the generated list, with nulls inserted after each item
     */
    public static List<String> randDoubles(int count) {
      return randList("Randoms to test operations with doubles", count, ()->randDoubleValue());
    } // static List<String> randDoubles(int count) {

    /**
     * Generates and returns a list of strings representing random {@code Long} values.
     * @param count the amount of random numbers to be generated
     * @return the generated list, with nulls inserted after each item
     */
    public static List<String> randLongs(int count) {
      return randList("Randoms to test operations with Longs", count, ()->randLongValue());
    } // static List<String> randLongs(int count) {

    /**
     * Returns a list of pairs of random values, with null after each pair.
     * The values of the items in each pair are concerted so that their addition makes sense in most cases
     * (i.e. the expected result does not equal to one of the operands and is not Infinity)
     * @param count the number of pairs to generate
     * @return a list of strings, containing string representations of the generated pairs of values,
     * with nulls inserted after each pair
     */
    public static List<String> randomsForAddition(int count) {
      return randPairList("addition", count, exp -> exp + random.nextInt(261) - 130); // From exp - 130 to exp + 130 inclusively
    } // static List<String> randomsForAddition(int count) {

    /**
     * Returns a list of pairs of random values, with null after each pair.
     * The values of the items in each pair are concerted so that their multiplication makes sense in most cases
     * (i.e. the expected result is rarely equal to 0 or Infinity).
     * @param count the number of pairs to generate
     * @return a list of strings, containing string representations of the generated pairs of values,
     * with nulls inserted after each pair
     */
    public static List<String> randomsForMultiplication(int count) {
      return randPairList("multiplication", count, exp -> otherExpForMult(exp));
    } // public static List<String> randomsForMultiplication(int count) {

    /**
     * Returns a list of pairs of random values, with null after each pair.
     * The values of the items in each pair are concerted so that division makes sense in most cases
     * (i.e. the expected result is rarely equal to 0 or Infinity)
     * @param count the number of pairs to generate
     * @return a list of strings, containing string representations of the generated pairs of values,
     * with nulls inserted after each pair
     */
    public static List<String> randomsForDivision(int count) {
      return randPairList("division", count, exp -> otherExpForDiv(exp));
    } // public static List<String> randomsForDivision(int count) {

    /**
     * Returns a list of pairs of random values, with null after each pair.
     * The values of the items in each pair are concerted so that division makes sense in most cases
     * (i.e. the expected result is rarely equal to 0 or Infinity)
     * @param count the number of pairs to generate
     * @return a list of strings, containing string representations of the generated pairs of values,
     * with nulls inserted after each pair
     */
    public static List<String> randomsPairs(int count) {
      return randPairList("min()/max()", count, exp -> random.nextInt());
    } // public static List<String> randomsForDivision(int count) {

    /* *****************************************************************************************
     * Private methods *************************************************************************
     * *****************************************************************************************/

    /**
     * Generates a list of values, preceded by the given header,
     * containing values provided by the given {@code Supplier}.
     * Inserts null after each value returned by the supplier.
     * @param headerComment the comment to precede the generated list
     * @param count the amount of the values to be generated
     * @param generator a Supplier to generate the required values
     * @return
     */
    private static List<String> randList(String headerComment, int count, Supplier<String> generator) {
      final List<String> list = new ArrayList<>((count + 1) * 2);
      if (count > 0)
        appendComment(list, "// " + headerComment + ": " + count + " random values", null);
      for (int i = 0; i < count; i++) {
        list.add(generator.get());
        list.add(null);
      }
      return list;
    } // private static List<String> randList(String headerComment, int count, Supplier<String> generator) {

    /**
     * Generates a list of pairs of values, preceded by the given header,
     * containing pairs whose elements are restricted in range based on the value provided by the given {@code Function}.
     * The exponent of the second element of each pair is calculated,
     * using the given function, based on the exponent of the first element;
     * null is inserted after each pair.
     * @param headerComment
     * @param count a number of pairs to generate
     * @param findNextExponent a function to find an exponent of the second value based on the found exponent of the first value of a pair
     * @return the input list to which the generated values have been added
     */
    private static List<String> randPairList(String headerComment, int count, Function<Integer, Integer> findNextExponent) {
      final List<String> list = new ArrayList<>((count + 1) * 3);
      if (count > 0)
        appendComment(list, String.format("// %d pairs of random values for %s", count, headerComment), null, null);
      for (int i = 0; i < count; i++) {
        final int exp1 = random.nextInt();
        final int exp2 = findNextExponent.apply(exp1); // = exp1 + random.nextInt(258) - 129;
        list.add(bdStr(random.nextLong(), random.nextLong(), exp1));
        list.add(bdStr(random.nextLong(), random.nextLong(), exp2));
        list.add(null);
      }
      return list;
    } // private static List<String> randomsForBinaryOperations(List<String> list, int count, Function<Integer, Integer> findNextExponent) {

    /**
     * Generates a string representing a random {@code BigDecimal} value falling within the range valid for {@code Quadruple}s.
     * @return a string with decimal representation of the generated value
     */
    private static String randomBigDecimalString() {
      final BigInteger bi = new BigInteger(128, random);
      final BigDecimal bd = new BigDecimal(String.valueOf(random.nextInt(9)+1) + "." + String.format("%040d", bi));
      final String expStr = "1e" + (-MAX_EXP10  + random.nextInt(MAX_EXP10 * 2));
      final String s = bd.toString() + expStr;
      return s;
    } // static String randomBigDecimalString() {

    /**
     * Generates a string representing a random {@code BigDecimal} value falling within the range valid for {@code Quadruple}s.
     * @return a string with decimal representation of the generated value
     */
    private static String randomIeee754String() {
      final BigInteger bi = new BigInteger(128, random);
      final BigDecimal bd = new BigDecimal(String.valueOf(random.nextInt(9)+1) + "." + String.format("%040d", bi));
      final String expStr = "1e" + (MIN_IEEE754_EXPONENT  + random.nextInt(MAX_IEEE754_EXPONENT - MIN_IEEE754_EXPONENT));
      final String s = bd.toString() + expStr;
      return s;
    } // static String randomBigDecimalString() {


    /**
     * Generates a string expressing a random value, within the range valid for {@code double}s,
     * whose mantissa differs from the mantissa of the nearest {@code double} value by 2^-53
     * (half of the LSB of the mantissa of {@code double})
     * or by 2^-53 - 2^-128 (half of the LSB of the mantissa of {@code double} minus LSB of the mantissa of Quadruple)
     * @return a string with decimal representation of the generated value
     */
    private static String randDoubleWithHalfLsb() {
      long mantHi = randomLongRanged(0, DOUBLE_MANT_MASK * 2 + 1); // 53 bits: for double mantissa + half its LSB
      long mantLo;
      if ((mantHi & 1) == 0) {
        mantHi = mantHi << 11 | DOUBLE_EXP_MASK >>> 52; // 0xMMM..MMM7FFL, 0xFFF...FFFL -- mantissa + half LSB - 2^-128
        mantLo = -1;
      } else {
        mantHi = mantHi << 11; // mantissa + half LSB
        mantLo = 0;
      }
      return bdStr(mantHi, mantLo, randomDoubleExponent());
    } // private static String randDoubleWithHalfLsb() {

    /** Static variables to keep the generated random value to be used for the second item of a pair */
    private static Integer randExponent = null;
    private static Long randMantHi      = null;
    private static Long randMantLo      = null;

    /**
     * Generates pairs of adjacent random values each of which equals to a random Quadruple value,
     * plus an addition that's equal or close to 0.5 of the LSB of the corresponding Quadruple's mantissa.<br>
     * the first (0th) and all subsequent even calls return Q + * 0.5 * LSB * (1 - 2^expOfDelta),
     * where Q is a generated random Quadruple value (the result is expected to get rounded down to Q),<br>
     * the next (1st) and all subsequent odd calls return Q + 0.5 * LSB,
     * where Q is a value generated during the previous call ((the result is expected to get rounded up to Q + LSB)).
     * @param expOfDelta the exponent of delta to be subtracted from the generated value (for even calls)
     * @return a string with a decimal representation of the generated value
     */
    private static String randQuadrupleWithHalfLsb(int expOfDelta) {
      int exp; long mantHi, mantLo;
      if (randExponent == null) {                 // even call, initialize parts
        randExponent  = exp = random.nextInt();
        randMantHi    = mantHi = random.nextLong();
        randMantLo    = mantLo = random.nextLong();
      } else {                                    // odd call, restore the previous value and reset randExponent
        exp           = randExponent;
        mantHi        = randMantHi;
        mantLo        = randMantLo;
        randExponent  = null;
      }

      final BigDecimal bd = bd(mantHi, mantLo, exp); // Approximately equals to some random Quadruple (140 decimal digits)
      BigDecimal addition = bd(0, 0, (int)Math.max(0L, ((long)exp - 129) & LOWER_32_BITS)); // LSB * 0.5, to be rounded up
      if (randExponent  != null)
        addition = mult(addition, sub(BD_ONE, powerOfTwo(expOfDelta))); //LSB * 0.5 * (1 - 2^expOfDelta)
      return add(bd, addition).stripTrailingZeros().toString();
    } // static String randQuadrupleWithHalfLsb(int expOfDelta) {

    /**
     * Generates a string representing the exact value of a random {@code Quadruple} value
     * falling within the range valid for {@code double}s.
     * @return a string representing the generated value
     */
    private static String randDoubleValue() {
      final long mantHi = random.nextLong();
      final long mantLo = random.nextLong();
      return bdStr(mantHi, mantLo, randomDoubleExponent());
    } // private static String randDoubleValue() {


    /**
     * Generates a string representing a random {@code long} value.
     * @return a string representing the generated value
     */
    private static String randLongValue() {
      return str(random.nextLong());
    } // static String randLongValue() {

    /**
     * Generates a random value of an exponent of {@code Quadruple} such
     * that the value of a {@code Quadruple} with this exponent falls within the range valid for {@code double}.
     */
    private static int randomDoubleExponent() {
      return randomIntRanged((EXP_0Q - EXP_0D) & LOWER_32_BITS, (EXP_0Q + EXP_0D) & LOWER_32_BITS);
    } // public static int randomDoubleExponent() {


    /**
     * A function to get a random exponent of the the second value of a pair
     * based on the exponent of the first value such that multiplication of the two values
     * make sense in most cases
     * @param exp the exponent of the first value of the pair
     * @return the generated value of the exponent for the second value of the pair
     */
    private static int otherExpForMult(int exp) {
      final long unbiased = Integer.toUnsignedLong(exp) - EXP_0Q;
      return randomIntRanged(max(0, 1 - unbiased),
                          min(0x1_0000_0000L, 0x1_0000_0000L - unbiased));
    } // private static int otherExpForMult(int exp) {

    /**
     * A function to get a random exponent of the the second value of a pair
     * based on the exponent of the first value such that division of the first value
     * by the second would make sense in most cases
     * @param exp the exponent of the first value of the pair
     * @return the generated value of the exponent for the second value of the pair
     */
    private static int otherExpForDiv(int exp) {
      final long unbiased = Integer.toUnsignedLong(exp) - EXP_0Q;
      return randomIntRanged(max(0, unbiased),
                          min(0xFFFF_FFFEL, 0xFFFF_FFFEL - 1 + unbiased));
    } // private static int otherExpForDiv(int exp) {

    /**
     * Generates and returns a random {@code int} value within the specified range
     *
     * @param bottom the lower boundary of the range (inclusive)
     * @param ceiling the upper boundary of the range (exclusive)
     * @return the generated value
     */
    private static int randomIntRanged(long bottom, long ceiling) {
      return (int)randomLongRanged(bottom, ceiling);
    } // private static int intRandomRanged(long bottom, long ceiling) {

    /**
     * Generates and returns a random {@code long} value within the specified range
     *
     * @param bottom the lower boundary of the range (inclusive)
     * @param ceiling the upper boundary of the range (exclusive)
     * @return the generated value
     */
    private static long randomLongRanged(long bottom, long ceiling) {
      return bottom + (long)(random.nextDouble() * (ceiling - bottom));
    } // private static long longRandomRanged(long bottom, long ceiling) {

  } // static class Randoms {

  /* ********************************************************************************
  /* private methods () *************************************************************
  /* ********************************************************************************/

  private static BigDecimal prevMantissa = null;

  /**
   * Generates and returns a series of sequences of values, where each sequence
   * is a set of values in a vicinity of the base value that is calculated based
   * on a corresponding value from the {@code mantissas} array as
   * {@code (m[i] * 2^exp + a) * MIN_VALUE}. The values of the sequence based on
   * an i'th member of the {@code mantissas} grow from
   * {@code (v[0] = m[i] * 2^exp + a - maxDelta) * MIN_VALUE} to
   * {@code (v[n] = m[i] * 2^exp + a + maxDelta) * MIN_VALUE},
   * such that the difference {@code delta * MIN_VALUE)
   * between an element of the sequence and its base value first
   * decreases in magnitude by a factor of 10 with each step, running thru
   * values from {@code -maxDelta * MIN_VALUE} to {@code -minDelta * MIN_VALUE},
   * then passes 0, and then increases by a factor of 10, running thru values
   * from {@code minDelta * MIN_VALUE} to {@code maxDelta * MIN_VALUE}, where
   * {@code m[i]} is a value from the {@code mantissas} array, {@code exp} is
   * the value of the {@code exponent} parameter, {@code a} is the value of the
   * {@code addition} parameter, {@code maxDelta} and {@code minDelta} are the
   * values of the corresponding parameters, and {@code MIN_VALUE} is {@code
   * Quadruple.MIN_VALUE}, i.e. the value of the least significant bit of a
   * subnormal {@code Quadruple} value.<br>
   * In other words, the values in the resulting list may described as follows:
   *
   * <pre>
   *    ((m[0] * 2^e + a) - maxDelta) * MIN_VALUE,
   *    ((m[0] * 2^e + a) - maxDelta / 10) * MIN_VALUE,
   *    ...
   *    ((m[0] * 2^e + a) - minDelta) * MIN_VALUE,
   *    (m[0] * 2^e + a) * MIN_VALUE,
   *    ((m[0] * 2^e + a) + minDelta) * MIN_VALUE,
   *    ...
   *    ((m[0] * 2^e + a) + maxDelta / 10) * MIN_VALUE,
   *    ((m[0] * 2^e + a) + maxDelta) * MIN_VALUE,
   *
   *    ((m[1] * 2^e + a) - maxDelta) * MIN_VALUE,
   *    ((m[1] * 2^e + a) - maxDelta / 10) * MIN_VALUE,
   *    ...
   *    ((m[1] * 2^e + a) - minDelta) * MIN_VALUE,
   *    (m[1] * 2^e + a) * MIN_VALUE,
   *    ((m[1] * 2^e + a) + minDelta) * MIN_VALUE,
   *    ...
   *    ((m[1] * 2^e + a) + maxDelta / 10) * MIN_VALUE,
   *    ((m[1] * 2^e + a) + maxDelta) * MIN_VALUE,
   * </pre>
   *
   * @param mantissas
   *          an array with values of mantissas to form the base values for the subsequences,
   * @param addition
   *          an addition to be added to the mantissa multiplied by 2^exponent,
   * @param maxDelta
   *          the maximum magnitude of the delta to be added or subtracted to
   *          the base value before multiplying it by MIN_VALUE;
   * @param minDelta
   *          the minimum magnitude of the delta to be added or subtracted to
   *          the base value before multiplying it by MIN_VALUE;
   * @param exponent
   *          the exponent of the power of two that the mantissa should be
   *          multiplied by,
   * @return a list of strings containing the values described above
   */
  private static Collection<? extends String> subnormalVicinities(String[] mantissas, double addition, double maxDelta,
                                                                  double minDelta, int exponent) {
    final List<String> result = new ArrayList<>();
    if (prevMantissa == null)
      prevMantissa = new BigDecimal(0);
    for (final String mantissa : mantissas) {
      // m * 2^e
      BigDecimal bdMantissa = mult(new BigDecimal(mantissa), powerOfTwo(exponent, MC_120_HALF_EVEN));
      // m * 2^e + a
      bdMantissa = add(new BigDecimal(bdMantissa.toBigInteger()), addition);
      if (bdMantissa.compareTo(prevMantissa) != 0) {                          // To suppress doubling of the values
        result.addAll(vicinityOfSubnormal(bdMantissa, maxDelta, minDelta));
        prevMantissa = bdMantissa;
      }
    }
    return result;
  }

  /**
   * Generates and returns a sequence of values in a vicinity of the base value,
   * {@code m * MIN_VALUE}, from {@code v[0] = (m - maxDelta) * MIN_VALUE } to
   * {@code v[n] = (m + maxDelta) * MIN_VALUE}, such that the difference (delta)
   * between the elements of the sequence and {@code m * MIN_VALUE} first
   * decreases in magnitude by a factor of 10 with each step, running thru
   * values from {@code -maxDelta * MIN_VALUE} to {@code -minDelta * MIN_VALUE},
   * then passes 0, and then increases by a factor of 10, running thru values
   * from {@code minDelta * MIN_VALUE} to {@code maxDelta * MIN_VALUE},
   * where m is the given {@code mantissa} and
   * {@code MIN_VALUE} is the value of {@code Quadruple.MIN_VALUE}, i.e. the
   * value of the least significant bit of subnormal {@code Quadruple} values:
   *
   * <pre>
   * (m - maxDelta) * MIN_VALUE,
   * (m - maxDelta / 10) * MIN_VALUE,
   * (m - maxDelta / 100) * MIN_VALUE,
   * (..
   * (m - minDelta * 10) * MIN_VALUE,
   * (m - minDelta) * MIN_VALUE,
   *  m * MIN_VALUE,
   * (m + minDelta) * MIN_VALUE,
   * (m + minDelta * 10) * MIN_VALUE,
   * (..
   * (m + maxDelta / 100) * MIN_VALUE,
   * (m + maxDelta / 10) * MIN_VALUE,
   * (m + maxDelta) * MIN_VALUE,
   *
   * </pre>
   * TODO 21.05.31 10:51:32 Continue from here
   * @param mantissa
   *          the factor to be applied to the {@code Quadruple.MIN_VALUE}
   * @param maxDelta
   *          the magnitude of the maximum difference between the base value and
   *          an element of the generated sequence
   * @param minDelta
   *          the magnitude of the minimal difference between the base value and
   *          an element of the generated sequence
   * @return a list containing string representations of the values of the generated sequence
   */
  private static List<String> vicinityOfSubnormal(BigDecimal mantissa, double maxDelta, double minDelta) {
    final List<String> result = new ArrayList<>();

    addDecreasingDeltas(result, mantissa, maxDelta, minDelta);
    result.add(String.format("// %s * MIN_VALUE", mantissa));
    result.add(mult(mantissa, MIN_VALUE).toString());
    addIncreasingDeltas(result, mantissa, maxDelta, minDelta);

    return result;
  } // private static List<String> vicinityOfSubnormal(BigDecimal mantissa, double maxDelta, double minDelta) {

  /**
   * Computes and adds to the given list a subsequence of string representations
   * of values from {@code (mantissa - maxDelta) * MIN_VALUE}
   * to {@code (mantissa - minxDelta) * MIN_VALUE},
   * with the delta decreasing 10 times each step.
   * @param result the list to add the generated values
   * @param mantissa the base value to calculate the values
   * @param maxDelta
   *          the magnitude of the difference between the base value and
   *          the least value of the generated sequence
   * @param minDelta
   *          the magnitude of the difference between the base value and
   *          the greatest value of the generated sequence
   */
  private static void addDecreasingDeltas(final List<String> result, BigDecimal mantissa,
                                          double maxDelta, double minDelta) {
    if (maxDelta < 1.0e-100)
      return;
    BigDecimal bdDelta;
    for (double delta = maxDelta; delta >= minDelta * 0.9; delta /= 10.0) {
      bdDelta = new BigDecimal(String.format("%.0e", delta));
      result.add(String.format("// (%s - %s) * MIN_VALUE", mantissa, bdDelta));
      result.add(mult(sub(mantissa, bdDelta), MIN_VALUE).toString());
    }
  } // private static void addDecreasingDeltas(final List<String> result, BigDecimal mantissa, double maxDelta, double minDelta) {

  /**
   * Computes and adds to the given list a subsequence of string representations
   * of values from {@code (mantissa + minDelta) * MIN_VALUE}
   * to {@code (mantissa + minDelta) * MIN_VALUE},
   * with the delta increasing 10 times each step.
   * @param result the list to add the generated values
   * @param mantissa the base value to calculate the values
   * @param maxDelta
   *          the magnitude of the difference between the base value and
   *          the greatest value of the generated sequence
   * @param minDelta
   *          the magnitude of the difference between the base value and
   *          the least value of the generated sequence
   */
  private static void addIncreasingDeltas(final List<String> result, BigDecimal mantissa,
                                          double maxDelta, double minDelta) {
    if (maxDelta < 1.0e-100)
      return;
    BigDecimal bdDelta;
    for (double delta = minDelta; delta <= maxDelta * 1.1; delta *= 10.0) {
      bdDelta = new BigDecimal(String.format("%.0e", delta));
      result.add(String.format("// (%s + %s) * MIN_VALUE", mantissa, bdDelta));
      result.add(mult(add(mantissa, bdDelta), MIN_VALUE).toString());
    }
  } // private static void addIncreasingDeltas(final List<String> result, BigDecimal mantissa, double maxDelta, double minDelta) {

  /**
   * Returns a list containing a sequence of values of form {@code mantissa + (addition + delta) * 2^-128}, <br>
   * where {@code delta} grows from {@code -maxDelta} up to {@code -minDelta} decreasing in module ten times each step,
   * passes through 0, and then grows from {@code minDelta} up to {@code maxDelta} increasing ten times each step:<pre>
   * mantissa + (addition - maxDelta)    * 2^-128,
   * mantissa + (addition - maxDelta/10) * 2^-128,
   * ...
   * mantissa + (addition - minDelta)    * 2^-128,
   * mantissa + (addition)               * 2^-128
   * mantissa + (addition + minDelta)    * 2^-128,
   * ...
   * mantissa + (addition + maxDelta/10) * 2^-128
   * mantissa + (addition + maxDelta)    * 2^-128
   * </pre>
   * @param mantissa the base mantissa value represented as a String;
   * @param addition the addition to the mantissa in units of 2^-128 so that 1.0 corresponds to the value of the LSB of the mantissa of Quadruple
   * @param maxDelta the maximum magnitude of the delta to be added or subtracted to the addition before multiplying it by 2^-128;
   * @param minDelta the minimum magnitude of the delta to be added or subtracted to the addition before multiplying it by 2^-128;
   * @return a new list containing String representations of the generated values
   */
  private static List<String> vicinityOfNormal(String mantissa, double addition, double maxDelta, double minDelta) {
    final List<String> result = new ArrayList<>();
    final BigDecimal bdMantissa = bd(mantissa);

    addDecreasingDeltas(result, bdMantissa, addition, maxDelta, minDelta);

    result.add(String.format("// (%s + %s * 2^-128)", bdMantissa, addition));
    result.add(add(bdMantissa, mult(addition, BD_2$_128)).toString());

    addIncreasingDeltas(result, bdMantissa, addition, maxDelta, minDelta);

    return result;
  }

  /**
   * Computes and adds to the given list a sequence of string representations of
   * values from {@code mantissa + (addition - maxDelta) * 2^-128} to
   * {@code mantissa + (addition - minDelta) * 2^-128} with the delta decreasing 10 times each step.
   *
   * @param list      the list to add the generated values
   * @param mantissa  the base value to calculate the values
   * @param addition  the addition to add to the mantissa, in units of the mantissa's LSB
   * @param maxDelta  the magnitude of the difference between the base value and the least
   *                  value of the generated sequence, in the units of the mantissa's LSB
   * @param minDelta  the magnitude of the difference between the base value and the greatest value
   *                  of the generated sequence, in units of the mantissa's LSB
   */
  private static void addDecreasingDeltas(List<String> list, BigDecimal mantissa, double addition, double maxDelta, double minDelta) {
    if (maxDelta < 1.0e-100) return;
    BigDecimal bdDelta;
    for (double delta = maxDelta; delta >= minDelta * 0.9; delta /= 10.0) {
      bdDelta = new BigDecimal(String.format("%.0e", delta));
      list.add(String.format("// (%s + (%s - %s) * 2^-128)", mantissa, addition, bdDelta));
      list.add(add(mantissa, mult( sub(addition, bdDelta), BD_2$_128) ).toString());
    }
  } // private static void addDecreasingDeltas(List<String> list, BigDecimal mantissa, double addition, double maxDelta, double minDelta) {

  /**
   * Computes and adds to the given list a sequence of string representations of
   * values from {@code mantissa + (addition + minDelta) * 2^-128} to
   * {@code mantissa + (addition + maxDelta) * 2^-128} with the delta decreasing 10 times each step.
   *
   * @param list      the list to add the generated values
   * @param mantissa  the base value to calculate the values
   * @param addition  the addition to add to the mantissa, in units of the mantissa's LSB
   * @param maxDelta  the magnitude of the difference between the base value and the least
   *                  value of the generated sequence, in the units of the mantissa's LSB
   * @param minDelta  the magnitude of the difference between the base value and the greatest value
   *                  of the generated sequence, in units of the mantissa's LSB
   */
  private static void addIncreasingDeltas(List<String> result, BigDecimal mantissa, double addition, double maxDelta, double minDelta) {
    if (maxDelta < 1.0e-100) return;
    BigDecimal bdDelta;
    for (double delta = minDelta; delta <= maxDelta * 1.1; delta *= 10.0) {
      bdDelta = new BigDecimal(String.format("%.0e", delta));
      result.add(String.format("// (%s + (%s + %s) * 2^-128)", mantissa, addition, bdDelta));
      result.add(add(mantissa, mult(add(addition, bdDelta), BD_2$_128)).toString());
    }
  } // private static void addIncreasingDeltas(List<String> result, BigDecimal mantissa, double addition, double maxDelta, double minDelta) {

  /**
   * Generates and returns a growing sequence of values of binary exponents for generating a test sequence
   * for testing the precision of the rounding of values in vicinity of {@code (1.0 + (n + 0.5) * 2^128) * 2^e}.<br>
   * The values {@code (1.0 + (n + 0.49999..) * 2^128) * 2^e} must get rounded down to {@code (1.0 + n * 2^128) * 2^e},<br>
   * while {@code (1.0 + (n + 0.5) * 2^128) * 2^e} and {@code (1.0 + (n + 0.500..01) * 2^128) * 2^e} must get rounded up to
   * {@code (1.0 + (n + 1) * 2^128) * 2^e}.<br>
   * The returned list<pre>
    0x8000_0002, 0xc000_0000, 0xc000_0001, 0xe000_0000, 0xe000_0001, 0xf000_0000, 0xf000_0001,
    ...
    0xffff_fff9, 0xffff_fffc, 0xffff_fffd, 0xffff_fffe, 0xffff_ffff, 0x0000_0000,
    0x0000_0001, 0x0000_0002, 0x0000_0003, 0x0000_0004, 0x0000_0007, 0x0000_0008,
    ...
    0x2000_0000, 0x3fff_ffff, 0x4000_0000, 0x7fff_ffff,
    </pre>
   * covers the entire range of valid values for exponents and includes all combinations of bits like 0x0_1000, 0x0_1111, 0x1_0000, 0x1_1111,
   * so that the calculation of the corresponding powers uses both only a single factor from the table of powers of 2,
   * and a product of all possible factors.
   */
  private static List<Integer> allExponents() {
    final ArrayList<Integer> result = new ArrayList<>();
    result.add(-Integer.MAX_VALUE + 1);               // -2147483646, 0x8000_0002, for MIN_NORMAL,
    for (int exponent = 0x4000_0000; exponent > 0;) { // Negative values from -1073741824 to -1
      result.add(-exponent);
      exponent = Integer.bitCount(exponent) == 1? --exponent : (exponent + 1) >>> 1;
    }
    result.add(0);
    for (int exponent = 1; exponent > 0;) {
      result.add(exponent);
      exponent = exponent == 1? 2 : Integer.bitCount(exponent) == 1? exponent * 2 - 1 : ++exponent;
    }
    return result;
  } // private static List<Integer> allExponents() {

  /** Returns a string representation of the value represented by the {@code value} parameter
   * multiplied by two raised to power {@code exp}: {@code value * 2^exp}
   * @param value the value to multiply by the power of two
   * @param exp the power to raise two to to get the factor
   * @return a string representation of the resulting value
   */
  private static String multByPowerOfTwo(String value, int exp) {
    if (value == null) return null;
    if (value.startsWith("//")) return String.format("%s * 2^%s", value, exp);
    try {
      final BigDecimal bdValue = bd(value);
      return mult(bdValue, powerOfTwo(exp, MC_120_HALF_EVEN)).toString();
    } catch (final Exception x) {
      return value;
    }
  } // private static String multByPowerOfTwo(String value, int exp) {

}
