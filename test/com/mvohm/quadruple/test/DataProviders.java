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
import static com.mvohm.quadruple.test.DataGenerators.*;
import static com.mvohm.quadruple.test.DataGenerators.CartesianSquare.*;
import static com.mvohm.quadruple.test.DataGenerators.Randoms.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of static methods that build and return data sets for testing different
 * {@link Quadruple} operations.<br>
 * Each data set is a list of arrays of {@code String}s. Each array consists of two
 * (for unary operations, including conversions) or three (for binary operations) strings
 * an contain string representations of input operand(s) for the tested operation
 * and the expected result of the operation being applied to the given operand(s).
 * In most cases the expected result can be computed by the corresponding tester class,
 * that uses the data returned by a provider.
 * In such cases, a null or an empty string may be in place of the expected result value.
 *
 * @author M.Vokhmentsev
 */
public class DataProviders {

  /**
   * String representations of a few different values whose Quadruple equivalents have many bits set in mantissas
   */
  private static final String[] DIFFERENT_MANTISSAS = new String[] {
      bdStr(sub(1,    BD_2$_128)), // ffff_ffff_ffff_ffff ffff_ffff_ffff_fffe e 7fff_fffe = "0.999999999999999999999999999999999999997061264122944281230..."
      bdStr(sub(1.25, BD_2$_128)),
      bdStr(sub(1.5,  BD_2$_128)),
      bdStr(sub(1.75, BD_2$_128)),
      bdStr(sub(2,    BD_2$_128)),
   };

  private static int randomCount = 3000; // default value

  /**
   * Sets the numbers of random data samples to add to the test data lists
   * @param count the number of random data samples to add to the test data list for each test
   */
  public static void setRandomCount(int count) {
    randomCount = count;// TODO Auto-generated method stub
  }



  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code String}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code NaN}, {@code Infinity}, {@code Double.MAX_VALUE}, {@code Long.MAX_VALUE}, etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>a sequence of a few adjacent values to see how the result changes
   *      when the input data changes by the least significant bit of the mantissa;<br>
   * <li>A series of sequences of adjacent values with various exponents to ascertain that multiplying by powers
   *      of two don't spoil the overall precision;<br>
   * <li>A number of random numbers
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2sConversionDataList() {
    final List<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2T_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_Q2S_conversionData));
    list.addAll(insertNulls(multiply(sequence("1.0", -6, 6), bd("128.000"))));
    list.addAll(insertNulls(multByAllPowersOfTwo(
                    sequences(-4, 4, "1.0", "1.5", "1.999999999999999"))));
    list.addAll(simpleRandomsWithNulls(randomCount));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> q2sConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code double}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code NaN}, {@code Infinity}, {@code Double.MAX_VALUE}, {@code Long.MAX_VALUE}, etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>A number of random double values with added or subtracted half of the LSB of the {@code Quadruple}'s mantissa,
   *      to ascertain that the rounding works right
   * <li>A number of random numbers
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2dConversionDataList() {
    final List<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2T_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_Q2D_conversionData));
    list.addAll(randDoublesWithHalfLSB(randomCount));
    list.addAll(randDoubles(randomCount));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> q2dConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code long}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code NaN}, {@code Infinity}, {@code Double.MAX_VALUE}, {@code Long.MAX_VALUE}, etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2lConversionDataList() {
    final List<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2T_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_Q2L_conversionData));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> q2lConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code int}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code NaN}, {@code Infinity}, {@code Double.MAX_VALUE}, {@code Long.MAX_VALUE}, etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2iConversionDataList() {
    final List<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2T_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_Q2I_conversionData));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> q2iConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code BigDecimal}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, excluding the values that are not representable as {@code BifDecimal}<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>A number of random numbers
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2bdConversionDataList() {
    final List<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2BD_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_Q2BD_conversionData));
    list.addAll(simpleRandomsWithNulls(randomCount));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> q2bdConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code String} to {@code Quadruple}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code NaN}, {@code Infinity}, {@code Double.MAX_VALUE}, {@code Long.MAX_VALUE}, etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>A number of random numbers;
   * <li>A number of random values that differ from the corresponding Quadruple values by 0.5 LSB or 0.5 LSB * (1 - 2^-55),
   *      to ascertain that the rounding works right
   * <li>A series of values near {@code Quadruple.MIN_NORMAL} : {@code Quadruple.MIN_NORMAL + (Quadruple.MIN_VALUE * delta)}, where delta = -1.0 ... 1.0, with step 0.1;
   * <li>A series of subnormal values, {@code Quadruple.MIN_VALUE * (2^98 + delta)}, where delta = 9.0 ... 25.0, with step 0.5;,
   * <li>A series of values of {@code Quadruple.MIN_VALUE * (N + 0.5 +/- 3e-17)},
   *      i.e close vicinities of subnormal values + 0.5 LSB, to test rounding down/up after computing subnormal values with diverse powers of 2;,
   * <li>A series of values of {@code (1 + Quadruple.MIN_VALUE * (N + 0.5 +/- 3e-17)) * 2^M},
   *      i.e close vicinities of normal values + 0.5 LSB, with various exponents, to test rounding down/up after multiplying the mantissa by diverse powers of 2;,
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> s2qConversionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2T_cornerCases));
//    list.addAll(Arrays.asList(TestData.special_S2Q_conversionSata)); // If one wants to test something special

    list.addAll(Arrays.asList(TestData.basic_S2Q_conversionData));
    list.addAll(simpleRandomsWithNulls(randomCount));
    list.addAll(randQuadruplesWithHalfLSB(randomCount, -55));  // mantissa close to n + 0.5 LSB:  n + 0.5 or n + 0.5 * (1 - 2^-55)
                                                         // i.e. may differ from n + 0.5 LSB in 183-rd bit after point

  /* */
  // Test the threshold to treat a value as subnormal
  // A series of values MN + (MV * delta), delta = -1.0 ... 1.0, step 0.1
    list.addAll(insertNulls( // +++ 20.11.13 18:27:05
                  "// Near MIN_NORMAL: A series of MIN_NORMAL + (MIN_VALUE * delta), delta = -1.0 ... 1.0, step 0.1",
                  sequence(Consts.MIN_NORMAL, -1.0, 1.0, 0.1, true))); // caution! Low step leads to long calculations!


    list.addAll(insertNulls( //
                  "// A series of subnormal MIN_VALUE * (2^98 + delta), delta = 9.0 ... 25.0, step 0.5",
                  sequence(Consts.BD_2$_32.multiply(new BigDecimal(4)), 9.0, 25.0, 0.5, Integer.MIN_VALUE + 2, true)));

  // Tests vicinities of n + 0.5 for the full acceptable range of the powers
    list.addAll(insertNulls(
                  "// Close vicinities of subnormal values + 0.5 LSB, \nto test rounding down/up after multiplying the mantissa by diverse powers of 2",
                  subnormalVicinities(DIFFERENT_MANTISSAS, 0.5, 3e-17, 3e-17)));

  // Tests vicinities of n + 0.5 for the full acceptable range of the powers
    list.addAll(insertNulls(
                  "// Close vicinities of normal values + 0.5 LSB, \nto test rounding down/up after multiplying the mantissa by diverse powers of 2",
                  multByAllPowersOfTwo(vicinities(DIFFERENT_MANTISSAS, 0.5, 3e-17, 3e-17)) ));
    /**/
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> s2qConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code BigDecimal} to {@code Quadruple}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, excluding the values that are not representable as {@code BifDecimal}<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>A number of random numbers;
   * <li>A number of random values that differ from the corresponding Quadruple values by 0.5 LSB or 0.5 LSB * (1 - 2^-128),
   *      to ascertain that the rounding works right
   * <li>A series of values of {@code Quadruple.MIN_VALUE * (N + 0.5) +/- 3e-40)},
   *      i.e close vicinities of subnormal values + 0.5 LSB, to test rounding down/up after computing subnormal values with diverse powers of 2;,
   * <li>A series of values of {@code (1 + Quadruple.MIN_VALUE * (N + 0.5 +/- 3e-40)) * 2^M},
   *      i.e close vicinities of normal values + 0.5 LSB, with various exponents, to test rounding down/up after multiplying the mantissa by diverse powers of 2;,
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> bd2qConversionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2BD_cornerCases));

    list.addAll(Arrays.asList(TestData.basic_BD2Q_conversionData));

    list.addAll(simpleRandomsWithNulls(randomCount));
    list.addAll(randQuadruplesWithHalfLSB(randomCount, -128));  // mantissa close to n + 0.5 LSB:  n + 0.5 or n + 0.5 * (1 - 2^-128)
                                                         // i.e. may differ from n + 0.5 LSB in 256-th bit after point

    // Tests vicinities of n + 0.5 for the full acceptable range of the powers
    list.addAll(insertNulls(
                "// Close vicinities of subnormal values + 0.5 LSB, \nto test rounding down/up after multiplying the mantissa by diverse powers of 2",
                subnormalVicinities(DIFFERENT_MANTISSAS, 0.5, 3e-40, 3e-40)));

    // Tests vicinities of n + 0.5 for the full acceptable range of the powers
    list.addAll(insertNulls(
                "// Close vicinities of normal values + 0.5 LSB, \nto test rounding down/up after multiplying the mantissa by diverse powers of 2",
                multByAllPowersOfTwo(vicinities(DIFFERENT_MANTISSAS, 0.5, 3e-40, 3e-40)) ));

    return convertToListOfArraysNx2(list);
  } // public static List<String[]> bd2qConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code double} to {@code Quadruple}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code NaN}, {@code Infinity}, {@code Double.MAX_VALUE}, {@code Long.MAX_VALUE}, etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>A number of random doubles;
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * * @return a set of data described above
   */
  public static List<String[]> d2qConversionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2T_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_d2Q_conversionData));
    list.addAll(randDoubles(randomCount));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> d2qConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code long} to {@code Quadruple}.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of values for rough testing of the corner cases, containing such values as
   *      {@code Long.MAX_VALUE}, {@code Long.MIN_VALUE}etc,
   *      and a few values between them;<br>
   * <li>basic data to test the conversion that covers all execution paths;<br>
   * <li>A number of random longs;
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an input value
   * for the tested operation and the expected result, that can be null or an empty string.
   * * @return a set of data described above
   */
  public static List<String[]> l2qConversionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(TestData.rough_Q2L_cornerCases));
    list.addAll(Arrays.asList(TestData.basic_L2Q_conversionData));
    list.addAll(randLongs(randomCount));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> l2qConversionDataList() {

/* ***************************************************************************
 * Data collectors for binary operations *************************************
 *****************************************************************************/

  /**
   * Assembles and returns a data set for testing addition.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of pairs including all possible combinations of special values like -0, "NaN" and "Infinity"
   *      with each other and with some normal numeric values, to ascertain that initial checks in {@code Quadruple.add()} work right;
   * <li>basic data to test the addition that covers all execution paths;<br>
   * <li>A number of pairs of random numbers.
   * </ul>
   * <br>The elements of the list are arrays, each containing three strings -- two operands
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> additionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(specialValuesForAddition());
    list.addAll(Arrays.asList(TestData.basicAdditionData));
    list.addAll(randomsForAddition(randomCount));
    return convertToListOfArraysNx3(list);
  } // public static List<String[]> additionDataList() {

  /**
   * Assembles and returns a data set for testing subtraction.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of pairs including all possible combinations of special values like -0, "NaN" and "Infinity"
   *      with each other and with some normal numeric values, to ascertain that initial checks in {@code Quadruple.subtract()} work right;
   * <li>basic data to test the subtraction that covers all execution paths;<br>
   * <li>A number of pairs of random numbers.
   * </ul>
   * <br>The elements of the list are arrays, each containing three strings -- two operands
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> subtractionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(specialValuesForSubtraction());
    list.addAll(Arrays.asList(TestData.basicSubtractionData));
    list.addAll(randomsForAddition(randomCount));          // Not an error. The same data fit here
    return convertToListOfArraysNx3(list);
  } // public static List<String[]> subtractionDataList() {

  /**
   * Assembles and returns a data set for testing multiplication.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of pairs including all possible combinations of special values like -0, "NaN" and "Infinity"
   *      with each other and with some normal numeric values, to ascertain that initial checks in {@code Quadruple.multiply()} work right;
   * <li>basic data to test the multiplication that covers all execution paths;<br>
   * <li>A number of pairs of random numbers.
   * </ul>
   * <br>The elements of the list are arrays, each containing three strings -- two operands
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> multiplicationDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(specialValuesForMultiplication());
    list.addAll(Arrays.asList(TestData.basicMultiplicationData));
    list.addAll(randomsForMultiplication(randomCount));
    return convertToListOfArraysNx3(list);
  } // public static List<String[]> multiplicationDataList() {

  /**
   * Assembles and returns a data set for testing division.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>a number of pairs including all possible combinations of special values like -0, "NaN" and "Infinity"
   *      with each other and with some normal numeric values, to ascertain that initial checks in {@code Quadruple.divide()} work right;
   * <li>basic data to test the division that covers all execution paths;<br>
   * <li>A number of pairs of random numbers.
   * </ul>
   * <br>The elements of the list are arrays, each containing three strings -- two operands
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> divisionDataList() {
    final ArrayList<String> list = new ArrayList<>();
    list.addAll(specialValuesForDivision());
    list.addAll(Arrays.asList(TestData.basicDivisionData));
    list.addAll(randomsForDivision(randomCount));
    return convertToListOfArraysNx3(list);
  } // public static List<String[]> divisionDataList() {

/* ***************************************************************************
 * Data collectors for unary operations **************************************
 *****************************************************************************/

  /**
   * Assembles and returns a data set for testing square root.
   * Includes subsets:<ul style="list-style-position: outside">
   * <li>basic data to test the square root that covers all execution paths;<br>
   * <li>A number of sequences of growing values where each item differs from the previous one
   * by the least significant bit in the mantissa of the argument;
   * <li>A number of random values;
   * </ul>
   * <br>The elements of the list are arrays, each containing two strings -- an operand to apply the operation to
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> sqrtDataList() {
    final ArrayList<String> list = new ArrayList<>(1500);
    list.addAll(Arrays.asList(TestData.basicSqrtData));

    addComment(list, "// Generated sequence:", null);
    list.addAll(insertNulls(multiplyByFactors(sequence("1", 1, 20),
                                              1.0, 1.2, 1.5, 1.7, 2.0, 3.0, 3.4)));

    list.addAll(simpleRandomsWithNulls(randomCount));
    return convertToListOfArraysNx2(list);
  } // public static List<String[]> sqrtDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code String} and back to {@code Quadruple},
   * to ascertain that the conversion from {@code Quadruple} to {@code String} is reversible.
   * Consists of the data returned by {@link #q2sConversionDataList()} and the data returned by {@link #s2qConversionDataList()}.
   * <br>The elements of the list are arrays, each containing two strings -- an operand to apply the operation to
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2s2qConversionDataList() {
    final List<String[]> data = q2sConversionDataList();
    data.addAll(s2qConversionDataList());
    return data;
  } // public static List<String[]> q2s2qConversionDataList() {

  /**
   * Assembles and returns a data set for testing the conversion from {@code Quadruple} to {@code BigDecimal} and back to {@code Quadruple},
   * to ascertain that the conversion from {@code Quadruple} to {@code BigDecimal} is reversible.
   * Consists of the data returned by {@link #q2bdConversionDataList()} and the data returned by {@link #bd2qConversionDataList()}.
   * <br>The elements of the list are arrays, each containing two strings -- an operand to apply the operation to
   * and the expected result, that can be null or an empty string.
   * @return a set of data described above
   */
  public static List<String[]> q2bd2qConversionDataList() {
    final List<String[]> data = new ArrayList<>();
    data.addAll(q2bdConversionDataList());
    data.addAll(bd2qConversionDataList());
    return data;
  } // public static List<String[]> q2bd2qConversionDataList() {

  /* ***************************************************************************
   * convert simple one-dimentional lists to lists of arrays *******************
   *****************************************************************************/


  /**
   * A helper method to convert a list of {@code String}s to a list of {@code String[]},
   * where each array contains two adjacent elements of the input list.
   * @param inpList -- a list of {@code String}s to convert;
   * @return a list of arrays of {@code String}s where each array contains two adjacent elements of the input list
   */
  private static ArrayList<String[]> convertToListOfArraysNx2(List<String> inpList) {
    final ArrayList<String[]> list = new ArrayList<>();
    for (int i = 0; i < inpList.size(); i += 2)
      list.add(new String[] { inpList.get(i), inpList.get(i + 1) } );
    return list;
  } // private static ArrayList<String[]> convertToListOfArraysNx2(List<String> inpList) {

  /**
   * A helper method to convert a list of {@code String}s to a list of {@code String[]},
   * where each array contains three adjacent elements of the input list.
   * @param inpList -- a list of {@code String}s to convert;
   * @return a list of arrays of {@code String}s where each array contains three adjacent elements of the input list
   */
  private static ArrayList<String[]> convertToListOfArraysNx3(List<String> inpList) {
    final ArrayList<String[]> list = new ArrayList<>();
    for (int i = 0; i < inpList.size(); i += 3)
      list.add(new String[] { inpList.get(i), inpList.get(i + 1), inpList.get(i + 2) } );
    return list;
  } // private static ArrayList<String[]> convertToListOfArraysNx3(List<String> inpList) {

}
