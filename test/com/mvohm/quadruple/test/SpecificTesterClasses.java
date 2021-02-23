package com.mvohm.quadruple.test;

import static com.mvohm.quadruple.test.AuxMethods.*;
import static com.mvohm.quadruple.test.Consts.*;
import static com.mvohm.quadruple.test.DataProviders.*;

import java.math.BigDecimal;
import java.util.List;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.TesterClasses.*;

/**
 * Contains static classes that implement concrete descendants of abstract tester classes
 * intended to test specific {@code Quadruple} operations
 * @author M.Vokhmentev
 */

public class SpecificTesterClasses {

  /*===============================================================================
  // Testers for conversions from Quadruple to other types
  //=============================================================================*/

  /** A tester to test {@code Quadruple.toString()} */
  static class QuadToStringTester extends Conversion_Q2T_Tester<String> {

    /** Returns the name of the tested operation -- "Quadruple.toString()" */
    @Override protected String getName()                                  { return "Quadruple.toString()"; }

    /** Obtains and returns a data set intended to test {@code Quadruple#toString()} */
    @Override protected List<String[]> getTestDataList()                  { return q2sConversionDataList(); }

    /** Performs the tested operation ({@code Quadruple#toString()}) with the given operand ad returns the result */
    @Override protected String performOp(Quadruple operand)               { return operand.toString(); }

  } // private static class QuadToStringTester extends Conversion_Q2T_Tester<Quadruple, String> {

  /**
   * A tester to test {@code Quadruple#doubleValue()}
   * {@inheritDoc}
   */
  static class QuadToDoubleTester extends Conversion_Q2T_Tester<Double> {

    /** Returns the name of the tested operation -- "Quadruple.doubleValue()" */
    @Override protected String getName()                                  { return "Quadruple.doubleValue()"; }

    /** Obtains and returns a data set intended to test {@code Quadruple#doubleValue()} */
    @Override protected List<String[]> getTestDataList()                  { return q2dConversionDataList(); }

    /** Performs the tested operation ({@code Quadruple#doubleValue()}) with the given operand ad returns the result */
    @Override protected Double performOp(Quadruple operand)               { return operand.doubleValue(); }

    /** Returns a string representations ("Infinity" or "-Infinity") for values that don't
     fall within the range of values allowed for Double */
    @Override
    protected String findExpectedString(Quadruple operand) {
      return String.valueOf(bigDecimalValueOf(operand).doubleValue());
    }

    /**
     * Calculates a {@code BigDecimal} value of the expected result of conversion from {@code Quadruple} to {@code double}
     * (the input value rounded to the nearest double as a BigDecimal)
     * Throws {@code NumberFormatException} for values that can't be represented as {@code BigDecimal}
     * (NaN, Infinity, -Infinity)
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quadruple -> BigDecimal -> double -> BigDecimal
      return new BigDecimal(bigDecimalValueOf(operand).doubleValue()); // the value rounded to the nearest double as a BigDecimal
    }

  } // private static class QuadToDoubleTester extends Conversion_Q2T_Tester<Quadruple, Double> {

  /**
   * A tester to test {@code Quadruple#longValue()}
   */
  static class QuadToLongTester extends Conversion_Q2T_Tester<Long> {

    /** Returns the name of the tested operation -- "Quadruple.longValue()" */
    @Override protected String getName()                                  { return "Quadruple.longValue()"; }

    /** Obtains and returns a data set intended to test {@code Quadruple#longValue()} */
    @Override protected List<String[]> getTestDataList()                  { return q2lConversionDataList(); }

    /** Performs the tested operation ({@code Quadruple#longValue()}) with the given operand ad returns the result */
    @Override protected Long performOp(Quadruple operand)                 { return operand.longValue(); }

    private static final BigDecimal MAX_LONG_VALUE = new BigDecimal(Long.MAX_VALUE);
    private static final BigDecimal MIN_LONG_VALUE = new BigDecimal(Long.MIN_VALUE);

    /**
     * Calculates a {@code BigDecimal} value of the expected result of conversion from {@code Quadruple} to {@code long}.
     * The expected result is the the result of the narrowing conversion analogous to {@code Double.longValue()}
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quadruple -> BigDecimal -> long -> BigDecimal

      if (isNaN(operand))
        return BigDecimal.ZERO;
      if (isInfinite(operand))
        return isNegative(operand)? MIN_LONG_VALUE : MAX_LONG_VALUE;

      final BigDecimal bd = bigDecimalValueOf(operand);
      if (bd.compareTo(MAX_LONG_VALUE) > 0) return MAX_LONG_VALUE;
      if (bd.compareTo(MIN_LONG_VALUE) < 0) return MIN_LONG_VALUE;
      return new BigDecimal(bd.longValue());
    }

  } // private static class QuadToLongTester extends Conversion_Q2T_Tester<Quadruple, Long> {

  /**
   * A tester to test {@code Quadruple#intValue()}
   */
  static class QuadToIntTester extends Conversion_Q2T_Tester<Integer> {

    /** Returns the name of the tested operation -- "Quadruple.intValue()" */
    @Override protected String getName()                                  { return "Quadruple.intValue()"; }

    /** Obtains and returns a data set intended to test {@code Quadruple#intValue()} */
    @Override protected List<String[]> getTestDataList()                  { return q2iConversionDataList(); }

    /** Performs the tested operation ({@code Quadruple#intValue()}) with the given operand ad returns the result */
    @Override protected Integer performOp(Quadruple operand)              { return  operand.intValue(); }

    private static final BigDecimal MAX_INT_VALUE = new BigDecimal(Integer.MAX_VALUE);
    private static final BigDecimal MIN_INT_VALUE = new BigDecimal(Integer.MIN_VALUE);

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of conversion from {@code Quadruple} to {@code int}.
     * The expected result is the the result of the narrowing conversion analogous to {@code Double.intValue()}
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

  /**
   * A tester to test {@code Quadruple#bigDecimalValue()}
   */
  static class QuadToBdTester extends Conversion_Q2T_Tester<BigDecimal> {

    /** Returns the name of the tested operation -- "Quadruple.bigDecimalValue()" */
    @Override protected String getName()                                  { return "Quadruple.bigDecimalValue()"; }

    /** Obtains and returns a data set intended to test {@code Quadruple#bigDecimalValue()} */
    @Override protected List<String[]> getTestDataList()                  { return q2bdConversionDataList(); }

    /** Performs the tested operation ({@code Quadruple#bigDecimalValue()}) with the given operand ad returns the result */
    @Override protected BigDecimal  performOp(Quadruple operand)          { return operand.bigDecimalValue(); }
  } // static class QuadToBdTester extends Conversion_Q2T_Tester<BigDecimal> {

  /*===============================================================================
  // Testers for conversions from other types to Quadruple
  //=============================================================================*/

  /**
   * A tester to test conversion from {@code String} to {@code Quadruple} (namely, {@code new Quadruple(String s)})
   */
  static class StringToQuadTester extends Conversion_T2Q_Tester<String> {

    /** Returns the name of the tested operation -- "new Quadruple(String s)" */
    @Override protected String getName()                                  { return "new Quadruple(String s)";  }

    /** Obtains and returns a data set intended to test {@code new Quadruple(String s)} */
    @Override protected List<String[]> getTestDataList()                  { return s2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code String}
     * (just returns the argument, actually)*/
    @Override protected String      parseSrcType(String s)                { return s; }
  } // static class StringToQuadTester extends Conversion_T2Q_Tester<String> {

  /**
   * A tester to test conversion from {@code BigDecimal} to {@code Quadruple} (namely, {@code new Quadruple(BigDecimal bd)})
   */
  static class BdToQuadTester extends Conversion_T2Q_Tester<BigDecimal> {

    /** Returns the name of the tested operation -- "new Quadruple(BigDecimal bd)" */
    @Override protected String getName()                                  { return "new Quadruple(BigDecimal bd)";  }

    /** Obtains and returns a data set intended to test {@code new Quadruple(BigDecimal bd)} */
    @Override protected List<String[]> getTestDataList()                  { return bd2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code BigDecimal}
     * (<nobr>{@code new BigDecimal(s)},</nobr> actually)*/
    @Override protected BigDecimal  parseSrcType(String s)                { return new BigDecimal(s); }

  } // static class BdToQuadTester extends Conversion_T2Q_Tester<BigDecimal> {

  /**
   * A tester to test conversion from {@code double} to {@code Quadruple} (namely, {@code new Quadruple(double d)})
   */
  static class DoubleToQuadTester extends Conversion_T2Q_Tester<Double> {

    /** Returns the name of the tested operation -- "new Quadruple(double d)" */
    @Override protected String getName()                                  { return "new Quadruple(double d)";  }

    /** Obtains and returns a data set intended to test {@code new Quadruple(double d)} */
    @Override protected List<String[]> getTestDataList()                  { return d2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code double}
     * (<nobr>{@code Double.parseDouble(s)},</nobr> actually)*/
    @Override protected Double parseSrcType(String s)                     { return Double.parseDouble(s); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of conversion from {@code double} to {@code Quadruple}.
     * The expected result is the exact value of the {@code double} whose value is passed in as a {@code Quadruple} parameter.
     * In the cases of {@code NaN} and {@code Infinity} throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quad -> BD -> Double -> BigDecimal
      return new BigDecimal(bigDecimalValueOf(operand).doubleValue());
    }

  } // static class DoubleToQuadTester extends Conversion_T2Q_Tester<Double> {

  /**
   * A tester to test conversion from {@code long} to {@code Quadruple} (namely, {@code new Quadruple(long v)})
   */
  static class LongToQuadTester extends Conversion_T2Q_Tester<Long> {

    /** Returns the name of the tested operation -- "new Quadruple(long v)" */
    @Override protected String getName()                                  { return "new Quadruple(long v)";  }

    /** Obtains and returns a data set intended to test {@code new Quadruple(long v)} */
    @Override protected List<String[]> getTestDataList()                  { return l2qConversionDataList(); }

    /** Parses the input string and returns the corresponding value of type {@code long}
     * (<nobr>{@code Long.parseLong(s)},</nobr> actually)*/
    @Override protected Long parseSrcType(String s)                       { return Long.parseLong(s); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of conversion from {@code long} to {@code Quadruple}.
     * The expected result is the exact value of the {@code long} whose value is passed in as a {@code Quadruple} parameter.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand) {
      // Quad -> BD -> Long -> BigDecimal
      return new BigDecimal(bigDecimalValueOf(operand).longValue());
    }

  } // static class LongToQuadTester extends Conversion_T2Q_Tester<Long>

  /*===============================================================================
  // Testers for unary functions
  //=============================================================================*/

  /**
   * A tester to test conversion from {@code Quadruple} to {@code String} and back to {@code Quadruple}.
   * The {@code Quadruple} to {@code String} conversion must be reversible, i.e.
   * the result of the conversion must be equal to the source value in all cases.
   */
  static class QuadToStringToQuadTester extends UnaryQuadrupleFunctionTester {

    /** Returns the name of the tested operation -- "new Quadruple(q.toString())" */
    @Override protected String getName()                                  { return "new Quadruple(q.toString())"; }

    /** Obtains and returns a data set intended to test {@code new Quadruple(q.toString())} */
    @Override protected List<String[]>  getTestDataList()                 { return q2s2qConversionDataList(); }

    /** Performs the tested operation ({@code new Quadruple(operand.toString()}) with the given operand ad returns the result */
    @Override protected Quadruple       performOp(Quadruple operand)      { return new Quadruple(operand.toString()); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of conversion
     * from {@code Quadruple} to {@code String} and back to {@code Quadruple}, that is the value of the input parameter.
     * In the cases of {@code NaN} and {@code Infinity} throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override protected BigDecimal      findExpectedResult(Quadruple op)  { return bigDecimalValueOf(op); }

  } // static class QuadToStringToQuadTester extends UnaryQuadrupleFunctionTester {

  /**
   * A tester to test conversion from {@code Quadruple} to {@code BigDecimal} and back to {@code Quadruple}.
   * The {@code Quadruple} to {@code BigDecimal} conversion must be reversible, i.e.
   * the result of the conversion must be equal to the source value in all cases.
   */
  static class QuadToBDToQuadTester extends UnaryQuadrupleFunctionTester {

    /** Returns the name of the tested operation -- "new Quadruple(q.bigDecimalValue())" */
    @Override protected String getName()                                  { return "new Quadruple(q.bigDecimalValue())"; }

    /** Obtains and returns a data set intended to test {@code new Quadruple(q.toString())} */
    @Override protected List<String[]>  getTestDataList()                 { return q2bd2qConversionDataList(); }


    /** Performs the tested operation ({@code new Quadruple(operand.toString()}) with the given operand ad returns the result */
    @Override protected Quadruple       performOp(Quadruple operand)      { return new Quadruple(operand.bigDecimalValue()); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of conversion
     * from {@code Quadruple} to {@code BigDecimal} and back to {@code Quadruple}, that is the value of the input parameter.
     * In the cases of {@code NaN} and {@code Infinity} throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override protected BigDecimal      findExpectedResult(Quadruple op)  {
      if (op.isNegative() && isZero(op)) return BigDecimal.ZERO;
      if (isInfinite(op))
        throw new ArithmeticException("Error was expected");
      return bigDecimalValueOf(op);
    }

    /**
     * for {@code Quadruple} to {@code String} and back to {@code Quadruple} conversion,
     * Returns "#Error was expected#" in case if the operand is {@code Infinity},
     * to avoid counting such cases as source errors
     */
    @Override
    protected String findExpectedString(Quadruple operand) {
      if (isInfinite(operand))
        return("#Error was expected#");
      return null;
    };



    @Override protected String otherTypeName() { return "NoMinusZero"; }

  } // static class QuadToStringToQuadTester extends UnaryQuadrupleFunctionTester {

  /**
   *  A tester to test static method {@link Quadruple.sqrt(Quadruple q)}
   */
  static class StaticSqrtTester extends UnaryQuadrupleFunctionTester {

    /** Returns the name of the tested operation -- "Quadruple.sqrt(Quadruple q)" */
    @Override protected String          getName()                         { return "Quadruple.sqrt(Quadruple q)"; }

    /** Obtains and returns a data set intended to test the sqrt() function  */
    @Override protected List<String[]>  getTestDataList()                 { return sqrtDataList(); }

    /** Performs the tested operation ({@code Quadruple.sqrt(Quadruple operand)}) with the given operand ad returns the result */
    @Override protected Quadruple       performOp(Quadruple operand)      { return Quadruple.sqrt(operand); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of the {@code sqrt()} function.
     * A special method that calculates the square root of a {@code BigDecimal} by 'Babylonian' method is used.
     */
    @Override protected BigDecimal      findExpectedResult(Quadruple op)  { return sqrt(bigDecimalValueOf(op)); }
  } // static class StaticSqrtTester extends UnaryQuadrupleFunctionTester {

  /**
   * A tester to test instance method {@link Quadruple.sqrt()}
   */
  static class InstanceSqrtTester extends StaticSqrtTester  {

    /** Returns the name of the tested operation -- "operand.sqrt()" */
    @Override protected String getName()                                  { return "operand.sqrt()"; }

    /** Performs the tested operation ({@code Quadruple.sqrt(operand)}) with the given operand ad returns the result */
    @Override protected Quadruple performOp(Quadruple operand)            { return new Quadruple(operand).sqrt(); }
  } // static class InstanceSqrtTester extends StaticSqrtTester  {

  /*===============================================================================
  // Testers for binary functions
  //=============================================================================*/

  /**
   *  A tester to test static method {@link Quadruple.add(Quadruple op1, Quadruple op2)}
   */
  static class StaticAdditionTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation -- "add(Quadruple op1, Quadruple op2)" */
    @Override protected String getName()                                  { return "add(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the addition */
    @Override protected List<String[]> getTestDataList()                  { return additionDataList(); }

    /** Performs the tested operation ({@code Quadruple.add(Quadruple op1, Quadruple op2)})
     * with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.add(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of the addition of the two operands.
     * Converts arguments to {@code BigDecimal}s and finds and returns their sum.
     * If one or both operands are not convertible to {@code BigDecimal}, throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      return bigDecimalValueOf(operand1).add(bigDecimalValueOf(operand2), MC_80_HALF_EVEN);
    }

    /**
     * Returns a string representation of the expected result of the addition in cases
     * when one or both operands are not convertible to {@code BigDecimal}.
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

  /**
   *  A tester to test instance method {@link Quadruple.add(Quadruple op2)}
   */
  static class InstanceAdditionTester extends StaticAdditionTester {

    /** Returns the name of the tested operation -- "op1.add(Quadruple op2)" */
    @Override protected String getName()                                  { return "op1.add(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.add(Quadruple op2)}) with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).add(op2); }
  } // static class InstanceAdditionTester extends StaticAdditionTester {

  /**
   *  A tester to test static method {@link Quadruple.subtract(Quadruple op1, Quadruple op2)}
   */
  static class StaticSubtractionTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation -- "subtract(Quadruple op1, Quadruple op2)" */
    @Override protected String getName()                                  { return "subtract(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the subtraction */
    @Override protected List<String[]> getTestDataList()                  { return subtractionDataList(); }

    /** Performs the tested operation ({@code Quadruple.subtract(Quadruple op1, Quadruple op2)})
     * with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.subtract(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of the subtraction of the two operands.
     * Converts arguments to {@code BigDecimal}s and finds and returns their difference.
     * If one or both operands are not convertible to {@code BigDecimal}, throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      return bigDecimalValueOf(operand1).subtract(bigDecimalValueOf(operand2), MC_80_HALF_EVEN);
    }

    /**
     * Returns a string representation of the expected result of the subtraction in cases
     * when one or both operands are not convertible to {@code BigDecimal}.
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

  /**
   *  A tester to test instance method {@link Quadruple.subtract(Quadruple op2)}
   */
  static class InstanceSubtractionTester extends StaticSubtractionTester  {

    /** Returns the name of the tested operation -- "op1.subtract(Quadruple op2)" */
    @Override protected String getName()                                  { return "op1.subtract(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.subtract(Quadruple op2)}) with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).subtract(op2); }

  } // static class InstanceSubtractionTester extends StaticSubtractionTester  {

  /**
   *  A tester to test static method {@link Quadruple.multiply(Quadruple op1, Quadruple op2)}
   */
  static class StaticMultiplicationTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation -- "multiply(Quadruple op1, Quadruple op2)" */
    @Override protected String getName()                                  { return "multiply(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the multiplication */
    @Override protected List<String[]> getTestDataList()                  { return multiplicationDataList(); }

    /** Performs the tested operation ({@code Quadruple.multiply(Quadruple op1, Quadruple op2)})
     * with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.multiply(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of the multiplication of the two operands.
     * Converts arguments to {@code BigDecimal}s and finds and returns their product.
     * If one or both operands are not convertible to {@code BigDecimal}, throws a {@code NumberFormatException}
     * and further processing is performed by the parent class.
     */
    @Override
    protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2) {
      return bigDecimalValueOf(operand1).multiply(bigDecimalValueOf(operand2));
    }

    /**
     * Returns a string representation of the expected result of the multiplication in cases
     * when one or both operands are not convertible to {@code BigDecimal}.
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

  /**
   *  A tester to test instance method {@link Quadruple.multiply(Quadruple op2)}
   */
  static class InstanceMultiplicationTester extends StaticMultiplicationTester {

    /** Returns the name of the tested operation -- "op1.multiply(Quadruple op2)" */
    @Override protected String getName()                                  { return "op1.multiply(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.multiply(Quadruple op2)}) with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).multiply(op2); }

  } // static class InstanceMultiplicationTester extends StaticMultiplicationTester {

  /**
   *  A tester to test static method {@link Quadruple.divide(Quadruple op1, Quadruple op2)}
   */
  static class StaticDivisionTester extends BinaryFunctionTester {

    /** Returns the name of the tested operation -- "divide(Quadruple op1, Quadruple op2)" */
    @Override protected String getName()                                  { return "divide(Quadruple op1, Quadruple op2)"; }

    /** Obtains and returns a data set intended to test the division */
    @Override protected List<String[]> getTestDataList()                  { return divisionDataList(); }

    /** Performs the tested operation ({@code Quadruple.divide(Quadruple op1, Quadruple op2)})
     * with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return Quadruple.divide(op1, op2); }

    /**
     * Calculates a {@code BigDecimal} with the value of the expected result of the division of the two operands.
     * Converts arguments to {@code BigDecimal}s and finds and returns their quotient.
     * If one or both operands are not convertible to {@code BigDecimal}, throws a {@code NumberFormatException}
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
     * Returns a string representation of the expected result of the division in cases
     * when one or both operands are not convertible to {@code BigDecimal} or the divisor is zero
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

  } // static class StaticDivisionTester extends BinaryFunctionTester {

  /**
   *  A tester to test instance method {@link Quadruple.divide(Quadruple op2)}
   */
  static class InstanceDivisionTester extends StaticDivisionTester {

    /** Returns the name of the tested operation -- "op1.divide(Quadruple op2)" */
    @Override protected String getName() { return "op1.divide(Quadruple op2)"; }

    /** Performs the tested operation ({@code op1.divide(Quadruple op2)}) with the given operands ad returns the result */
    @Override protected Quadruple performOp(Quadruple op1, Quadruple op2) { return new Quadruple(op1).divide(op2); }

  } // static class InstanceDivisionTester extends StaticDivisionTester {

 /*
  * // TODO 20.10.29 12:36:03 testRandoms(): Придумать как-то использовать Results, как прочие тесты,
  * /
 private void testRandoms() {

   for (int sampleNum = 1000; sampleNum < 1_000_000_000; sampleNum *= 10) {
     testNRandoms(sampleNum);
   }
   say("Фсё!");
 }
 /**/

 /*
  * testNRandoms(int sampleNum) {
  * @param sampleNum
  * /
 private void testNRandoms(int sampleNum) {
   final int[] counts = new int[100];
   for (int i = 0; i < sampleNum; i++) {
     final Quadruple q100 = new Quadruple(100);
     final Quadruple q = Quadruple.nextNormalRandom();
     final int idx = Quadruple.multiply(q,  q100).intValue();
     counts[idx]++;
//     if (i % 10 == 0) say(q.format("%.9f"));
   }
//   say();

   double d = 0;
   for (int i = 0; i < counts.length; i++) {
     double percent = 100.0 * counts[i] / sampleNum;
     say("%02d: %.6f", i, percent);
     percent -= 1;
     d += percent * percent;
   }
   say();
   say("N = %,d, d = %.6f\n", sampleNum, Math.sqrt(d/100));
 }
 /**/

  private static boolean isNaN(Quadruple operand) {
    return
      operand.exponent() == EXP_INF
      && (operand.mantHi() | operand.mantLo()) != 0;
  }

  private static boolean isInfinite(Quadruple operand) {
    return
      operand.exponent() == EXP_INF
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
