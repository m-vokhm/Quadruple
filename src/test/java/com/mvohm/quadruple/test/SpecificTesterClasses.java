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
import static com.mvohm.quadruple.test.DataProviders.*;
import static com.mvohm.quadruple.Quadruple.*;

import java.math.BigDecimal;
import java.util.List;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.TesterClasses.*;

/**
 * Contains concrete descendants of the abstract tester classes defined in {@link TesterClasses}.
 * These concrete classes are intended to test specific {@code Quadruple} operations,
 * e.g. {@link Quadruple#toString()} is tested by an instance of {@link QuadToStringTester}.
 * The general logic of these classes is described in more detail in the descriptions
 * of their respective ancestors such as {@link Conversion_Q2T_Tester},
 * {@link Conversion_T2Q_Tester} and others, nested in {@link TesterClasses}.
 * @author M.Vokhmentev
 */

public class SpecificTesterClasses {

  /* *******************************************************************************
   **** Testers for conversions from Quadruple to other types **********************
   *********************************************************************************/

  /** A tester class to test {@link Quadruple#toString()}.<br>
   * Obtains the test data from {@link DataProviders#q2sConversionDataList()}
   * and performs {@link Quadruple#toString()} as the tested operation.
   */
  static class QuadToStringTester extends Conversion_Q2T_Tester<String> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.toString()}". */
    @Override protected String getName()                                  { return "Quadruple.toString()"; }

    /** Obtains and returns a data set intended to test {@link Quadruple#toString()}.<br>
     * Uses {@link DataProviders#q2sConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return q2sConversionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#toString()},
     * with the given operand, and returns its result. */
    @Override protected String performOp(Quadruple operand)               { return operand.toString(); }

  } // private static class QuadToStringTester extends Conversion_Q2T_Tester<Quadruple, String> {

  /** A tester class to test {@link Quadruple#doubleValue()}.<br>
   * Obtains the test data from {@link DataProviders#q2dConversionDataList()}
   * and performs {@link Quadruple#doubleValue()} as the tested operation.
   */
  static class QuadToDoubleTester extends Conversion_Q2T_Tester<Double> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.doubleValue()}". */
    @Override protected String getName()                                  { return "Quadruple.doubleValue()"; }

    /** Obtains and returns a data set intended to test {@link Quadruple#doubleValue()}.<br>
     * Uses {@link DataProviders#q2dConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return q2dConversionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#doubleValue()},
     * with the given operand, and returns its result. */
    @Override protected Double performOp(Quadruple operand)               { return operand.doubleValue(); }

    /** Returns string representations ("Infinity" or "-Infinity") for values that don't
     fall within the range allowed for Double. */
    @Override
    protected String findExpectedString(Quadruple operand) {
      return String.valueOf(bigDecimalValueOf(operand).doubleValue());
    }

    /**
     * Calculates a {@code BigDecimal} value of the expected result
     * of the conversion from {@link Quadruple} to {@code double}.<br>
     * The expected result is the input value rounded to the exact value
     * of the nearest {@code double}, expressed as a {@link BigDecimal}.
     * Throws {@code NumberFormatException} for values that can't be represented as {@code BigDecimal}:
     * {@code NaN}, {@code Infinity}, and {@code -Infinity}.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quadruple -> BigDecimal -> double -> BigDecimal
      return new BigDecimal(bigDecimalValueOf(operand).doubleValue()); // the value rounded to the nearest double as a BigDecimal
    }

  } // private static class QuadToDoubleTester extends Conversion_Q2T_Tester<Quadruple, Double> {

  /** A tester class to test {@link Quadruple#longValue()}.<br>
   * Obtains the test data from {@link DataProviders#q2lConversionDataList()}
   * and performs {@link Quadruple#longValue()} as the tested operation.
   */
  static class QuadToLongTester extends Conversion_Q2T_Tester<Long> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.longValue()}". */
    @Override protected String getName()                                  { return "Quadruple.longValue()"; }

    /** Obtains and returns a data set intended to test {@link Quadruple#longValue()}.<br>
     * Uses {@link DataProviders#q2lConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return q2lConversionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#longValue()},
     * with the given operand, and returns its result. */
    @Override protected Long performOp(Quadruple operand)                 { return operand.longValue(); }

    private static final BigDecimal MAX_LONG_VALUE = new BigDecimal(Long.MAX_VALUE);
    private static final BigDecimal MIN_LONG_VALUE = new BigDecimal(Long.MIN_VALUE);

    /**
     * Calculates a {@code BigDecimal} value of the expected result
     * of the conversion from {@link Quadruple} to {@code long}.<br>
     * The expected result is the exact result of the narrowing conversion
     * from {@code Quadruple} to {@code long}, expressed as a {@link BigDecimal}.<br>
     * {@code NaN}, {@code Infinity}, and {@code -Infinity} are translated
     * to {@code BigDecimal}s with values of 0, {@code Long.MAX_VALUE},
     * and {@code Long.MIN_VALUE}, respectively.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quadruple -> BigDecimal -> long -> BigDecimal
      if (isNaN(operand)) return BigDecimal.ZERO;
      if (isInfinite(operand))
        return isNegative(operand)? MIN_LONG_VALUE : MAX_LONG_VALUE;
      final BigDecimal bd = bigDecimalValueOf(operand);
      if (bd.compareTo(MAX_LONG_VALUE) > 0) return MAX_LONG_VALUE;
      if (bd.compareTo(MIN_LONG_VALUE) < 0) return MIN_LONG_VALUE;
      return new BigDecimal(bd.longValue());
    }

  } // private static class QuadToLongTester extends Conversion_Q2T_Tester<Quadruple, Long> {

  /** A tester class to test {@link Quadruple#intValue()}.<br>
   * Obtains the test data from {@link DataProviders#q2iConversionDataList()}
   * and performs {@link Quadruple#intValue()} as the tested operation.
   */
  static class QuadToIntTester extends Conversion_Q2T_Tester<Integer> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.intValue()}". */
    @Override protected String getName()                                  { return "Quadruple.intValue()"; }

    /** Obtains and returns a data set intended to test {@link Quadruple#intValue()}.<br>
     * Uses {@link DataProviders#q2iConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return q2iConversionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#intValue()},
     * with the given operand, and returns its result. */
    @Override protected Integer performOp(Quadruple operand)              { return  operand.intValue(); }

    private static final BigDecimal MAX_INT_VALUE = new BigDecimal(Integer.MAX_VALUE);
    private static final BigDecimal MIN_INT_VALUE = new BigDecimal(Integer.MIN_VALUE);

    /**
     * Calculates a {@code BigDecimal} value of the expected result
     * of the conversion from {@link Quadruple} to {@code int}.<br>
     * The expected result is the exact result of the narrowing conversion
     * from {@code Quadruple} to {@code int}, expressed as a {@link BigDecimal}.<br>
     * {@code NaN}, {@code Infinity}, and {@code -Infinity} are translated
     * to {@code BigDecimal}s with values of 0, {@code Integer.MAX_VALUE},
     * and {@code Integer.MIN_VALUE}, respectively.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quadruple -> BigDecimal -> Integer -> BigDecimal
      if (isNaN(operand)) return BigDecimal.ZERO;
      if (isInfinite(operand))
        return (operand.signum() < 0)? MIN_INT_VALUE: MAX_INT_VALUE;
      final BigDecimal bd = bigDecimalValueOf(operand);
      if (bd.compareTo(MAX_INT_VALUE) > 0) return MAX_INT_VALUE;
      if (bd.compareTo(MIN_INT_VALUE) < 0) return MIN_INT_VALUE;
      return new BigDecimal(bd.intValue());
    }

  } // static class QuadToIntTester extends Conversion_Q2T_Tester<Integer> {

  /** A tester class to test {@link Quadruple#bigDecimalValue()}.<br>
   * Obtains the test data from {@link DataProviders#q2bdConversionDataList()}
   * and performs {@link Quadruple#bigDecimalValue()} as the tested operation.
   */
  static class QuadToBdTester extends Conversion_Q2T_Tester<BigDecimal> {

    /** Returns the name of the tested operation, namely "{@code Quadruple.bigDecimalValue()}". */
    @Override protected String getName()                                  { return "Quadruple.bigDecimalValue()"; }

    /** Obtains and returns a data set intended to test {@link Quadruple#bigDecimalValue()}.<br>
     * Uses {@link DataProviders#q2bdConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return q2bdConversionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#bigDecimalValue()},
     * with the given operand, and returns its result. */
    @Override protected BigDecimal  performOp(Quadruple operand)          { return operand.bigDecimalValue(); }
  } // static class QuadToBdTester extends Conversion_Q2T_Tester<BigDecimal> {

  /* *******************************************************************************
   **** Testers for conversions from other types to Quadruple **********************
   *********************************************************************************/

  /** A tester class to test the conversion from {@code String} to {@link Quadruple},
   * namely the {@link Quadruple#Quadruple(String)} constructor.<br>
   * Obtains the test data from {@link DataProviders#s2qConversionDataList()}
   * and performs {@link Quadruple#Quadruple(String)} as the tested operation.
   */
  static class StringToQuadTester extends Conversion_T2Q_Tester<String> {

    /** Returns the name of the tested operation, namely "{@code new Quadruple(String s)}". */
    @Override protected String getName()                                  { return "new Quadruple(String s)";  }

    /** Obtains and returns a data set intended to test the
     * {@code String} to {@code Quadruple} conversion.<br>
     * Uses {@link DataProviders#s2qConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return s2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code String}<br>
     * (just returns the argument, actually)*/
    @Override protected String      parseSrcType(String s)                { return s; }
  } // static class StringToQuadTester extends Conversion_T2Q_Tester<String> {

  /** A tester class to test the conversion from {@link BigDecimal} to {@link Quadruple},
   * namely the {@link Quadruple#Quadruple(BigDecimal)} constructor.<br>
   * Obtains the test data from {@link DataProviders#bd2qConversionDataList()}
   * and performs {@link Quadruple#Quadruple(BigDecimal)} as the tested operation.
   */
  static class BdToQuadTester extends Conversion_T2Q_Tester<BigDecimal> {

    /** Returns the name of the tested operation, namely "{@code new Quadruple(BigDecimal bd)}". */
    @Override protected String getName()                                  { return "new Quadruple(BigDecimal bd)";  }

    /** Obtains and returns a data set intended to test the
     * {@link BigDecimal} to {@code Quadruple} conversion.<br>
     * Uses {@link DataProviders#bd2qConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return bd2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@link BigDecimal}.<br>
     * Uses {@link BigDecimal#BigDecimal(String)}. */
    @Override protected BigDecimal  parseSrcType(String s)                { return new BigDecimal(s); }

  } // static class BdToQuadTester extends Conversion_T2Q_Tester<BigDecimal> {

  /** A tester class to test the conversion from {@code double} to {@link Quadruple},
   * namely the {@link Quadruple#Quadruple(double)} constructor.<br>
   * Obtains the test data from {@link DataProviders#d2qConversionDataList()}
   * and performs {@link Quadruple#Quadruple(double)} as the tested operation.
   */
  static class DoubleToQuadTester extends Conversion_T2Q_Tester<Double> {

    /** Returns the name of the tested operation, namely "{@code new Quadruple(double d)}". */
    @Override protected String getName()                                  { return "new Quadruple(double d)";  }

    /** Obtains and returns a data set intended to test the
     * {@code double} to {@code Quadruple} conversion.<br>
     * Uses {@link DataProviders#d2qConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return d2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code double}.<br>
     * Uses {@link Double#valueOf(String)}. */
    @Override protected Double parseSrcType(String s)                     { return Double.valueOf(s); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the conversion from {@code double} to {@code Quadruple}.<br>
     * The expected result is the exact value of the {@code double},
     * whose value is passed in as a {@code Quadruple} parameter, expressed as a {@code BigDecimal}.
     * In cases of {@code NaN}, {@code Infinity}, and {@code -Infinity}, throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quad -> BD -> Double -> BigDecimal
      return new BigDecimal(bigDecimalValueOf(operand).doubleValue());
    }

  } // static class DoubleToQuadTester extends Conversion_T2Q_Tester<Double> {

  /** A tester class to test the conversion from {@code long} to {@link Quadruple},
   * namely the {@link Quadruple#Quadruple(long)} constructor.<br>
   * Obtains the test data from {@link DataProviders#l2qConversionDataList()}
   * and performs {@link Quadruple#Quadruple(long)} as the tested operation.
   */
  static class LongToQuadTester extends Conversion_T2Q_Tester<Long> {

    /** Returns the name of the tested operation, namely "{@code new Quadruple(long v)}". */
    @Override protected String getName()                                  { return "new Quadruple(long v)";  }

    /** Obtains and returns a data set intended to test the
     * {@code long} to {@code Quadruple} conversion.<br>
     * Uses {@link DataProviders#l2qConversionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return l2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code long}.<br>
     * Uses {@link Long#valueOf(String)}. */
    @Override protected Long parseSrcType(String s)                       { return Long.valueOf(s); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the conversion from {@code long} to {@code Quadruple}.<br>
     * The expected result is the exact value of the {@code long},
     * whose value is passed in as a {@code Quadruple} parameter, expressed as a {@code BigDecimal}.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quad -> BD -> Long -> BigDecimal
      return new BigDecimal(bigDecimalValueOf(operand).longValue());
    }

  } // static class LongToQuadTester extends Conversion_T2Q_Tester<Long>

  /* *******************************************************************************
   **** Testers for unary functions ************************************************
   *********************************************************************************/

  /** A tester class to test the conversion from {@code Quadruple} to {@code String}
   * and back to {@code Quadruple}.<br>
   * Obtains the test data from {@link DataProviders#q2s2qConversionDataList()}
   * and performs {@code new Quadruple(operand.toString())} as the tested operation.
   */
  static class QuadToStringToQuadTester extends UnaryQuadrupleFunctionTester {

    /** Returns the name of the tested operation, namely "{@code new Quadruple(q.toString())}". */
    @Override protected String getName()                                  { return "new Quadruple(q.toString())"; }

    /** Obtains and returns a data set intended to test the conversion
     * from {@code Quadruple} to {@code String} and back to {@code Quadruple}.<br>
     * Uses {@link DataProviders#q2s2qConversionDataList()} to obtain the data. */
    @Override protected List<String[]>  getTestDataList()                 { return q2s2qConversionDataList(); }

    /** Performs the tested operation, namely {@code new Quadruple(operand.toString()},
     * with the given operand, and returns its result. */
    @Override protected Quadruple       performOp(Quadruple operand)      { return new Quadruple(operand.toString()); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the conversion from {@code Quadruple} to {@code String} and back to {@code Quadruple}.<br>
     * The expected result is the exact value of the {@code Quadruple} parameter, expressed as a {@code BigDecimal}.
     */
    @Override protected BigDecimal      findExpectedResult(Quadruple op)  { return bigDecimalValueOf(op); }

  } // static class QuadToStringToQuadTester extends UnaryQuadrupleFunctionTester {

  /** A tester class to test the conversion from {@code Quadruple} to {@code BigDecimal}
   * and back to {@code Quadruple}.<br>
   * Obtains the test data from {@link DataProviders#q2bd2qConversionDataList()}
   * and performs {@code new Quadruple(operand.bigDecimalValue())} as the tested operation.
   */
  static class QuadToBDToQuadTester extends UnaryQuadrupleFunctionTester {

    /** Returns the name of the tested operation, namely "{@code new Quadruple(q.bigDecimalValue())}". */
    @Override protected String getName()                                  { return "new Quadruple(q.bigDecimalValue())"; }

    /** Obtains and returns a data set intended to test the conversion
     * from {@code Quadruple} to {@code BigDecimal} and back to {@code Quadruple}.<br>
     * Uses {@link DataProviders#q2bd2qConversionDataList()} to obtain the data. */
    @Override protected List<String[]>  getTestDataList()                 { return q2bd2qConversionDataList(); }

    /** Performs the tested operation, namely {@code new Quadruple(operand.bigDecimalValue())},
     * with the given operand, and returns its result. */
    @Override protected Quadruple       performOp(Quadruple operand)      { return new Quadruple(operand.bigDecimalValue()); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result of the conversion
     * from {@code Quadruple} to {@code BigDecimal} and back to {@code Quadruple}.<br>
     * The expected result is the exact value of the {@code Quadruple} parameter, expressed as a {@code BigDecimal}.
     * In cases of {@code NaN}, {@code Infinity}, and {@code -Infinity} throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override protected BigDecimal      findExpectedResult(Quadruple op)  {
      if (op.isNegative() && isZero(op)) return BigDecimal.ZERO;
      if (isInfinite(op))                 // q2bd2qConversionDataList() includes bd2qConversionDataList() that includes infinities
        throw new ArithmeticException("Error was expected"); // Don't count these cases for errors
      return bigDecimalValueOf(op);
    }

    /**
     * for {@code Quadruple} to {@code BigDecimal} and back to {@code Quadruple} conversion,
     * returns "#Error was expected#" in case if the operand is {@code Infinity},
     * to avoid counting such cases as source errors
     */
    @Override
    protected String findExpectedString(Quadruple operand) {
      if (isInfinite(operand))
        return("Error: can't express Infinity as BigDecimal (as expected)");
      return null;
    };

    @Override protected String otherTypeName() { return "QuadToBigDecimalToQuad"; }

  } // static class QuadToStringToQuadTester extends UnaryQuadrupleFunctionTester {

  /** A tester class to test {@link Quadruple#sqrt(Quadruple)}.<br>
   * Obtains the test data from {@link DataProviders#sqrtDataList()}
   * and performs {@link Quadruple#sqrt(Quadruple)} as the tested operation.
   */
  static class StaticSqrtTester extends UnaryQuadrupleFunctionTester {

    /** Returns the name of the tested operation, namely "{@code Quadruple.sqrt(Quadruple q)}". */
    @Override protected String          getName()                         { return "Quadruple.sqrt(Quadruple q)"; }

    /** Obtains and returns a data set intended to test the {@code sqrt()} function.<br>
     * Uses {@link DataProviders#sqrtDataList()} to obtain the data. */
    @Override protected List<String[]>  getTestDataList()                 { return sqrtDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#sqrt(Quadruple)},
     * with the given operand, and returns its result. */
    @Override protected Quadruple       performOp(Quadruple operand)      { return Quadruple.sqrt(operand); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result of the {@code sqrt()} function.<br>
     * A special method {@link AuxMethods#sqrt(BigDecimal)} that uses the 'Babylonian'
     * algorithm calculates the square root of a {@code BigDecimal} value
     * representing the value of the operand.
     * Its result is used as the expected value of the {@code sqrt()} function.
     */
    @Override protected BigDecimal      findExpectedResult(Quadruple op)  { return sqrt(bigDecimalValueOf(op)); }
  } // static class StaticSqrtTester extends UnaryQuadrupleFunctionTester {

  /** A tester class to test {@link Quadruple#sqrt()}.<br>
   * Obtains the test data from {@link DataProviders#sqrtDataList()}
   * and performs {@link Quadruple#sqrt()} as the tested operation.
   */
  static class InstanceSqrtTester extends StaticSqrtTester  {

    /** Returns the name of the tested operation, namely "{@code operand.sqrt()}". */
    @Override protected String getName()                                  { return "operand.sqrt()"; }

    /** Performs the tested operation, namely {@link Quadruple#sqrt()},
     * with the given operand, and returns its result. */
    @Override protected Quadruple performOp(Quadruple operand)            { return new Quadruple(operand).sqrt(); }
  } // static class InstanceSqrtTester extends StaticSqrtTester  {

  /* *******************************************************************************
   **** Testers for binary functions ***********************************************
   *********************************************************************************/

  /** A tester class to test static method {@link Quadruple#add(Quadruple op1, Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#additionDataList()}
   * and performs {@link Quadruple#add(Quadruple op1, Quadruple op2)} as the tested operation.
   */
  static class StaticAdditionTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation, namely "{@code add(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "add(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the addition.<br>
     * Uses {@link DataProviders#additionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return additionDataList(); }

    /** Performs the tested operation, namely {@link Quadruple#add(Quadruple op1, Quadruple op2)},
     * with the given operands, and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.add(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the addition of the two operands.<br>
     * Converts arguments to {@code BigDecimal}s and finds and returns their sum.
     * If one or both operands are not convertible to {@code BigDecimal},
     * throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      return bigDecimalValueOf(operand1).add(bigDecimalValueOf(operand2), MC_80_HALF_EVEN);
    }

    /**
     * Returns a string representation of the expected result of the addition in the cases
     * when one or both operands are not convertible to {@code BigDecimal}.<br>
     * Depending on the values of the operands, returns "NaN", "Infinity", or "-Infinity",
     * following the same rules that are used for addition of double operands.
     */
    @Override
    protected String findExpectedString(Quadruple q1, Quadruple q2) {
      if (isNaN(q1) || isNaN(q2)) return "NaN"; // NaN + whatever = NaN;

      if (isInfinite(q1)) {
        if (isInfinite(q2) && (isNegative(q1) != isNegative(q2)))
          return "NaN";                           // -Infinity + Infinity = NaN
        else return q1.toString();                // Infinity + X = Infinity
      }

      if (isInfinite(q2)) return q2.toString();  // x + Infinity = Infinity regardless of their signs

      return null;
    }

  } // static class StaticAdditionTester extends BinaryFunctionTester {

  /** A tester class to test instance method {@link Quadruple#add(Quadruple op)}.<br>
   * Obtains the test data from {@link DataProviders#additionDataList()}
   * and performs {@code op1.add(op2)} as the tested operation.
   */
  static class InstanceAdditionTester extends StaticAdditionTester {

    /** Returns the name of the tested operation, namely "{@code op1.add(Quadruple op2)}". */
    @Override protected String getName()                                  { return "op1.add(Quadruple op2)"; }

    /** Performs the tested operation, namely {@code op1.add(Quadruple op2)},
     * with the given operands, and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).add(op2); }
  } // static class InstanceAdditionTester extends StaticAdditionTester {

  /** A tester class to test static method {@link Quadruple#subtract(Quadruple op1, Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#subtractionDataList()}
   * and performs {@link Quadruple#subtract(Quadruple op1, Quadruple op2)} as the tested operation.
   */
  static class StaticSubtractionTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation, namely "{@code subtract(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "subtract(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the subtraction.<br>
     * Uses {@link DataProviders#subtractionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return subtractionDataList(); }

    /** Performs the tested operation ({@code Quadruple.subtract(Quadruple op1, Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.subtract(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the subtraction of the two operands.<br>
     * Converts arguments to {@code BigDecimal}s and finds and returns their difference.
     * If one or both operands are not convertible to {@code BigDecimal},
     * throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      return bigDecimalValueOf(operand1).subtract(bigDecimalValueOf(operand2), MC_80_HALF_EVEN);
    }

    /**
     * Returns a string representation of the expected result of the subtraction in the cases
     * when one or both operands are not convertible to {@code BigDecimal}.<br>
     * Depending on the values of the operands, returns "NaN", "Infinity", or "-Infinity",
     * following the same rules that are used for subtraction of double operands.
     */
    @Override
    protected String findExpectedString(Quadruple q1, Quadruple q2) {
      if (isNaN(q1) || isNaN(q2)) return "NaN"; // NaN - whatever = NaN;

      if (isInfinite(q1)) {
        if (isInfinite(q2) && (isNegative(q1) == isNegative(q2)))
          return "NaN";                           // Infinity - Infinity = NaN
        else return q1.toString();                // Infinity - X = Infinity
      }

      if (isInfinite(q2))
        return isNegative(q2)?
            "Infinity" : // X - Infinity = -Infinity regardless of their signs
            "-Infinity";

      return null;
    }

  } // static class StaticSubtractionTester extends BinaryFunctionTester {

  /** A tester class to test instance method {@link Quadruple#subtract(Quadruple op)}.<br>
   * Obtains the test data from {@link DataProviders#subtractionDataList()}
   * and performs {@code Quadruple.add(op1, op2)} as the tested operation.
   */
  static class InstanceSubtractionTester extends StaticSubtractionTester  {

    /** Returns the name of the tested operation, namely "{@code op1.subtract(Quadruple op2)}". */
    @Override protected String getName()                                  { return "op1.subtract(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.subtract(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).subtract(op2); }

  } // static class InstanceSubtractionTester extends StaticSubtractionTester  {

  /** A tester class to test static method {@link Quadruple#multiply(Quadruple op1, Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#multiplicationDataList()}
   * and performs {@link Quadruple#multiply(Quadruple op1, Quadruple op2)} as the tested operation.
   */
  static class StaticMultiplicationTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation, namely "{@code multiply(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "multiply(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the multiplication.<br>
     * Uses {@link DataProviders#multiplicationDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return multiplicationDataList(); }

    /** Performs the tested operation ({@code Quadruple.multiply(Quadruple op1, Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.multiply(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the multiplication of the two operands.<br>
     * Converts arguments to {@code BigDecimal}s and finds and returns their product.
     * If one or both operands are not convertible to {@code BigDecimal},
     * throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      return bigDecimalValueOf(operand1).multiply(bigDecimalValueOf(operand2));
    }

    /**
     * Returns a string representation of the expected result of the multiplication in the cases
     * when one or both operands are not convertible to {@code BigDecimal}.<br>
     * Depending on the values of the operands, returns "NaN", "Infinity", or "-Infinity",
     * following the same rules that are used for multiplication of double operands.
     */
    @Override
    protected String findExpectedString(Quadruple q1, Quadruple q2) {
      if (isNaN(q1) || isNaN(q2))
        return "NaN";     // NaN * whatever = NaN;

      if (isInfinite(q1) && isZero(q2) || isZero(q1) && isInfinite(q2))
        return "NaN";     // Infinity * 0.0 = NaN

      if (isInfinite(q1) || isInfinite(q2))
        return (isNegative(q1) == isNegative(q2))?
            "Infinity" : // At least one of the operands is Infinity, same sign
            "-Infinity"; // At least one of the operands is Infinity, different signs

      return null;
    }

  } // static class StaticMultiplicationTester extends BinaryFunctionTester {

  /** A tester class to test instance method {@link Quadruple#multiply(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#multiplicationDataList()}
   * and performs {@link Quadruple#multiply(Quadruple op2)} as the tested operation.
   */
  static class InstanceMultiplicationTester extends StaticMultiplicationTester {

    /** Returns the name of the tested operation, namely "{@code op1.multiply(Quadruple op2)}". */
    @Override protected String getName()                                  { return "op1.multiply(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.multiply(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).multiply(op2); }

  } // static class InstanceMultiplicationTester extends StaticMultiplicationTester {

  /** A tester class to test static method {@link Quadruple#divide_1(Quadruple op1, Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#divisionDataList()}
   * and performs {@link Quadruple#divide_1(Quadruple op1, Quadruple op2)} as the tested operation.
   */
  static class StaticDivision_0_Tester extends BinaryFunctionTester {

    /** Returns the name of the tested operation, namely "{@code divide(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "divide(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the division.<br>
     * Uses {@link DataProviders#divisionDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return divisionDataList(); }

    /** Performs the tested operation ({@code Quadruple.divide(Quadruple op1, Quadruple op2)})
     * with the given operands, and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return Quadruple.divide_1(op1, op2);
    }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of the division of the two operands.<br>
     * Converts arguments to {@code BigDecimal}s and finds and returns their quotient.
     * If one or both operands are not convertible to {@code BigDecimal},
     * throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     * If the divisor is zero, throws an {@code ArithmeticException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple op1, Quadruple op2) {
      if (isZero(op2))
        throw new ArithmeticException("Division by zero");
      return bigDecimalValueOf(op1).divide(bigDecimalValueOf(op2), MC_80_HALF_EVEN);
    }

    /**
     * Returns a string representation of the expected result of the division in the cases
     * when one or both operands are not convertible to {@code BigDecimal} or the divisor is zero.<br>
     * Depending on the values of the operands, returns "NaN", "Infinity", or "-Infinity",
     * following the same rules that are used for division of double operands.
     */
    @Override
    protected String findExpectedString(Quadruple op1, Quadruple op2) {
      if (isNaN(op1) || isNaN(op2))
        return "NaN"; // NaN / X == X / NaN = NaN;

      if (isInfinite(op1))
        if  (isInfinite(op2))
          return "NaN";
        else
          return (isNegative(op1) == isNegative(op2))?
              "Infinity" : // The dividend is Infinity, same sign
              "-Infinity"; // The dividend is Infinity, different signs

      if (isInfinite(op2))
        return (isNegative(op1) == isNegative(op2))?
            "0.0" : // The divisor is Infinity, same sign
            "-0.0"; // The divisor is Infinity, different signs

      if (isZero(op2))
        if (isZero(op1))
          return "NaN";
        else
          return (isNegative(op1) == isNegative(op2))?
              "Infinity" : // The divisor is 0, same sign
              "-Infinity"; // The divisor is 0, different signs

      return null;
    }

  } // static class StaticDivision_0_Tester extends BinaryFunctionTester {

  static class StaticDivision_1_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code divide(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "Experimental divide_1(Quadruple op1, Quadruple op2)"; }

    /** Performs the tested operation ({@code Quadruple.divide(Quadruple op1, Quadruple op2)})
     * with the given operands, and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return Quadruple.divide_1(op1, op2);
    }

  } // static class StaticDivision_0_Tester extends BinaryFunctionTester {

  static class StaticDivision_2_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code divide(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "Experimental divide_2(Quadruple op1, Quadruple op2)"; }

    /** Performs the tested operation ({@code Quadruple.divide(Quadruple op1, Quadruple op2)})
     * with the given operands, and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return Quadruple.divide_2(op1, op2);
    }

  } // static class StaticDivision_0_Tester extends BinaryFunctionTester {

  static class StaticDivision_3_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code divide(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "Experimental divide_3(Quadruple op1, Quadruple op2)"; }

    /** Performs the tested operation ({@code Quadruple.divide(Quadruple op1, Quadruple op2)})
     * with the given operands, and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return Quadruple.divide_3(op1, op2);
    }

  } // static class StaticDivision_0_Tester extends BinaryFunctionTester {



  /** A tester class to test instance method {@link Quadruple#divide_1(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#divisionDataList()}
   * and performs {@link Quadruple#divide_1(Quadruple op2)} as the tested operation.
   */
  static class InstanceDivision_0_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code op1.divide(Quadruple op2)}". */
    @Override protected String getName() { return "op1.divide(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.divide(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return new Quadruple(op1).divide_0(op2);
    }

  } // static class InstanceDivision_0_Tester extends StaticDivision_0_Tester {

  /** A tester class to test instance method {@link Quadruple#divide_1(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#divisionDataList()}
   * and performs {@link Quadruple#divide_1(Quadruple op2)} as the tested operation.
   */
  //21.06.08 18:40:13 Trying to find a faster way to divide
  static class InstanceDivision_1_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code op1.divide(Quadruple op2)}". */
    @Override protected String getName() { return "Experimental op1.divide_1(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.divide(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return new Quadruple(op1).divide_1(op2);
    }

  } // static class InstanceDivision_0_Tester extends StaticDivision_0_Tester {


  /** A tester class to test instance method {@link Quadruple#divide_1(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#divisionDataList()}
   * and performs {@link Quadruple#divide_1(Quadruple op2)} as the tested operation.
   */
  //21.06.08 18:40:13 Trying to find a faster way to divide
  static class InstanceDivision_2_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code op1.divide(Quadruple op2)}". */
    @Override protected String getName() { return "Experimental op1.divide_2(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.divide(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return new Quadruple(op1).divide_2(op2);
    }

  } // static class InstanceDivision_0_Tester extends StaticDivision_0_Tester {


  /** A tester class to test instance method {@link Quadruple#divide_1(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#divisionDataList()}
   * and performs {@link Quadruple#divide_1(Quadruple op2)} as the tested operation.
   */
  //21.06.08 18:40:13 Trying to find a faster way to divide
  static class InstanceDivision_3_Tester extends StaticDivision_0_Tester {

    /** Returns the name of the tested operation, namely "{@code op1.divide(Quadruple op2)}". */
    @Override protected String getName() { return "Experimental op1.divide_3(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.divide(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) {
      return new Quadruple(op1).divide_3(op2);
    }

  } // static class InstanceDivision_0_Tester extends StaticDivision_0_Tester {


  /*
   * TODO 20.10.29 12:36:03 static class RandomTester():
   * to find a way to test randoms in such a way that the results can be represented
   * by the TestResults
   */

  /** A tester class to test static method {@link Quadruple#max(Quadruple op1, Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#maxDataList()}
   * and performs {@link Quadruple#max(Quadruple op1, Quadruple op2)} as the tested operation.
   */
  static class StaticMaxTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation, namely "{@code max(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "max(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test {@code max()} function.<br>
     * Uses {@link DataProviders#maxDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return maxDataList(); }

    /** Performs the tested operation ({@code Quadruple.max(Quadruple op1, Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.max(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of {@code max()} function of the two operands.<br>
     * Converts arguments to {@code BigDecimal}s and finds and returns their maximum.
     * If one or both operands are not convertible to {@code BigDecimal},
     * throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      final BigDecimal bdOp1 = bigDecimalValueOf(operand1);
      final BigDecimal bdOp2 = bigDecimalValueOf(operand2);
      return bdOp1.max(bdOp2);
    }

    /**
     * Returns a string representation of the expected result of {@code max()} function
     * in the cases when one or both operands are not convertible to {@code BigDecimal}.<br>
     * Depending on the values of the operands, returns "NaN", "Infinity", "-Infinity", or
     * a string representation of one of the operands,
     * following the same rules that are used for comparison of double operands.
     */
    @Override
    protected String findExpectedString(Quadruple q1, Quadruple q2) {
      if (isNaN(q1) || isNaN(q2))
        return "NaN";     // NaN > anything, so it's the max in any case

      if (    isInfinite(q1) && !isNegative(q1)
          ||  isInfinite(q2) && !isNegative(q2))
        return "Infinity";     // Infinity

      if (isInfinite(q1) && isNegative(q1))
        return (q2.toString());
      if (isInfinite(q2) && isNegative(q2))
        return (q1.toString());

      return null;
    }

  } // static class StaticMaxTester extends BinaryFunctionTester {

  /** A tester class to test instance method {@link Quadruple#assignMax(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#maxDataList()}
   * and performs {@link Quadruple#assignMax(Quadruple op2)} as the tested operation.
   */
  static class  InstanceMaxTester extends StaticMaxTester {
    /** Returns the name of the tested operation, namely "{@code op1.assignMax(Quadruple op2)}". */
    @Override protected String getName() { return "op1.assignMax(op2)"; }

    /** Performs the tested operation ({@code op1.assignMax(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).assignMax(op2); }

  } // static class  InstanceMaxTester extends StaticMaxTester {

  /** A tester class to test static method {@link Quadruple#min(Quadruple op1, Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#minDataList()}
   * and performs {@link Quadruple#min(Quadruple op1, Quadruple op2)} as the tested operation.
   */
  static class StaticMinTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation, namely "{@code min(Quadruple op1, Quadruple op2)}". */
    @Override protected String getName()                                  { return "min(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test {@code min()} function.<br>
     * Uses {@link DataProviders#minDataList()} to obtain the data. */
    @Override protected List<String[]> getTestDataList()                  { return minDataList(); }

    /** Performs the tested operation ({@code Quadruple.min(Quadruple op1, Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.min(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} value equal to the expected result
     * of {@code min()} function of the two operands.<br>
     * Converts arguments to {@code BigDecimal}s and finds and returns their minimum.
     * If one or both operands are not convertible to {@code BigDecimal},
     * throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      final BigDecimal bdOp1 = bigDecimalValueOf(operand1);
      final BigDecimal bdOp2 = bigDecimalValueOf(operand2);
      return bdOp1.min(bdOp2);
    }

    /**
     * Returns a string representation of the expected result of {@code min()} function
     * in the cases when one or both operands are not convertible to {@code BigDecimal}.<br>
     * Depending on the values of the operands, returns "NaN", "Infinity", "-Infinity", or
     * a string representation of one of the operands,
     * following the same rules that are used for comparison of double operands.
     */
    @Override
    protected String findExpectedString(Quadruple q1, Quadruple q2) {
      if (isNaN(q1))
        return q2.toString();
      if (isNaN(q2))
        return q1.toString();     // NaN > anything, so the other operand is the min in any case

      if (isInfinite(q1) && !isNegative(q1)) // Positive infinity
        return q2.toString();
      if (isInfinite(q2) && !isNegative(q2))
        return q1.toString();

      if (   isInfinite(q1) && isNegative(q1)  // Negative infinity
          || isInfinite(q2) && isNegative(q2))
        return ("-Infinity");

      return null;
    }

  } // static class StaticMinTester extends BinaryFunctionTester {

  /** A tester class to test instance method {@link Quadruple#assignMin(Quadruple op2)}.<br>
   * Obtains the test data from {@link DataProviders#minDataList()}
   * and performs {@link Quadruple#assignMin(Quadruple op2)} as the tested operation.
   */
  static class  InstanceMinTester extends StaticMinTester {
    /** Returns the name of the tested operation, namely "{@code op1.max(Quadruple op2)}". */
    @Override protected String getName() { return "op1.assignMin(op2)"; }

    /** Performs the tested operation ({@code op1.assignMin(Quadruple op2)})
     * with the given operands and returns the result. */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).assignMin(op2); }

  } // static class  InstanceMaxTester extends StaticMaxTester {


/************************************************************
 ***** Private utility methods ******************************
 ************************************************************/

  private static boolean isNaN(Quadruple operand) {
    return
      operand.exponent() == EXPONENT_OF_INFINITY
      && (operand.mantHi() | operand.mantLo()) != 0;
  }

  private static boolean isInfinite(Quadruple operand) {
    return
      operand.exponent() == EXPONENT_OF_INFINITY
      && (operand.mantHi() | operand.mantLo()) == 0;
  }

  private static boolean isNegative(Quadruple operand) {
    return operand.isNegative();
  }

  private static boolean isZero(Quadruple operand) {
    return (operand.mantHi() | operand.mantLo() | operand.exponent()) == 0;
  }

  @SuppressWarnings("unused")
  private void dummyMethodToEnableThePreviousCommentBeFoldedInEclipse() {}


}
