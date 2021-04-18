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
import java.util.List;

import com.mvohm.quadruple.Quadruple;

/**
 * The "Tester classes" are the classes that perform testing of individual
 * types of operations on {@code Quadruple}, such as addition, division, or conversion
 * from Quadruple to another numeric type.<br>
 * For each type of operation that should be tested, there exists a corresponding
 * concrete tester class, e.g. method {@link Quadruple#add(Quadruple, Quadruple)}
 * is tested by class {@code StaticAdditionTester}.<br>
 * Each tester class gets a list of data samples, for each sample performs
 * the operation being tested and passes the result along with the expected value,
 * that can be provided with the test data or calculated based on it,
 * to an instance of {@code TestResults} class, which registers the results
 * and calculates the necessary statistics (mean error, MSE, etc.) when the testing is done.
 *<p>
 * This file contains the abstract classes that are ancestors of concrete
 * tester classes for specific operations, that are nested in {@link SpecificTesterClasses} class.
 *
 * @author M.Vokhmentsev
 */
public class TesterClasses {

  /**
   * Some records in the data sets are actually just comments to be displayed
   * and there may be a special record that is a command to quit testing.
   * Such records are processed in a special way.
   */
  private static enum DataKinds {
    COMMENT, DATA, SIGNAL_TO_QUIT
  };

  /**
   * An enumeration of possible levels of the verbosity of the output.<br>
   */
  public static enum Verbosity {
    /** Only the summary results for all performed tests are printed */
    SILENT,
    /** Intermediate results for each tested operation are printed */
    MEDIUM,
    /** The results of the test for each data sample are printed */
    TALKATIVE
  };

  /**
   * Controls the verbosity of the output, set by the corresponding setter below
   */
  private static Verbosity verbosity = Verbosity.TALKATIVE;

  /**
   * A setter to set the verbosity mode that controls how verbose
   * is the output to the console during the execution of the tests.
   * @param verbosityValue the value to set
   * @see Verbosity
   */
  public static void setVerbosity(Verbosity verbosityValue) {
    if (verbosityValue == null)
      verbosity = Verbosity.MEDIUM; // default
    else
      verbosity = verbosityValue;
  }

  /**
   * Common interface for all testers classes.
   */
  interface QuadOpTester {

    /**
     * Tests the corresponding operation using data from the default provider
     * that is specified in each concrete descendant class.
     * @return an instance of {@link TestResults} containing the statistics on the test results
     */
    TestResults test();

    /**
     * Tests the corresponding operation using the given test data.
     * Each element of the list is expected to be a data sample in a form of an array of 2
     * (for unary operations including conversions) or 3
     * (for binary operations) strings, which represent numeric values of respectively one or two
     * input arguments for the operation being tested and the expected result of the operation.
     * The expected value in a data sample can be null or an empty string, in this case
     * the expected value of the tested operation is evaluated by the concrete descendant class
     * based on the arguments.
     * @param testData the data to run the test on
     * @return an instance of {@link TestResults} containing the statistics on the test results
     */
    TestResults test(List<String[]> testData);
  }

  /**
   * Common ancestor for all tester classes.<br><br>
   * Implements public methods:<ul style="list-style-position: outside">
   * <li>{@link #test()}     -- tests the operation with the default data set obtained via
   *                            {@link #getTestDataList()}
   * <li>{@link #test(List)} -- tests the operation with the given data set
   * </ul><br>
   * Defines abstract methods to be implemented by descendants:<ul style="list-style-position: outside">
   * <li>{@link #getName()} -- to return the tested operation name to be used in the report;
   * <li>{@link #getTestDataList()} -- to return the data set to be used in the test;
   * <li>{@link #testOp(String[])} -- to test the operation on the given individual data sample.
   * </ul><br>
   * Implements protected methods to be used by descendants:<ul style="list-style-position: outside">
   * <li>{@link #getThreshold()} -- returns the default value of the tolerable error threshold for the test,
   *                                <span class="nowrap">1.470e-39</span>.
   *                                May be overridden by descendants
   * </ul><br>
   *
   */
  static abstract class QuadTester implements QuadOpTester {

    /**
     * An instance of the {@link TestResults} class that accumulates the results of testing
     * the operation on individual data samples.
     */
    protected TestResults results;

    /** An implementation provided by a specific descendant should return the name of the specific test
     * to be used in the report and in error messages.
     * @return the name of the tested operation */
    abstract protected String getName();

    /** An implementation provided by a specific descendant should obtain and return
     * a specific default dataset to test the specific operation being tested.
     * @return a list of data samples to test the operation
     * @see DataProviders */
    abstract protected List<String[]> getTestDataList();

    /**
     * Tests the operation using the data from the default dataset provided by
     * {@link #getTestDataList()} method of a specific descendant.<br>
     * First creates an instance of {@link TestResults} to keep the results,
     * then for each data sample from the default test data list calls
     * the {@link #testOp(String[])} method, which performs the test on the data sample
     * and registers its result in the {@code TestResults},
     * then forms and prints the report (if in talkative mode)
     * and returns the {@code TestResults} instance.
     * <p>
     * @return an instance of {@link TestResults} with the results of the test
     */
    @Override
    public TestResults test() {
      return doTest(getTestDataList());
    };

    /**
     * Tests the operation using the data from a specific dataset passed in as a parameter.<br>
     * First creates an instance of {@link TestResults} to keep the results,
     * then for each data sample from the {@code testData} list calls
     * the {@link #testOp(String[])} method, which performs the test on the data sample
     * and registers its result in the {@code TestResults},
     * then forms and prints the report (if in talkative mode)
     * and returns the {@code TestResults} instance.
     * <p>
     * Can be called instead of the {@link #test()} method from an external code to test the operation
     * on a specific custom data set.<br>
     * @return an instance of {@link TestResults} with the results of the test
     */
    @Override
    public TestResults test(List<String[]> testData) {
      return doTest(testData);
    };

    /**
     * Performs the test.<br>
     * Creates an instance of {@link TestResults} to keep the results,
     * then for each data sample from the {@code #testData} list calls
     * the {@link #testOp(String[])} method, that performs the test on the data sample
     * and registers its result in {@code TestResults},
     * then forms and prints the report (if in talkative mode)
     * and returns the {@code TestResults} instance.
     * @return the newly-created instance of {@link TestResults} with the results of the performed test
     */
    private TestResults doTest(List<String[]> testData) {
      // if (TesterClasses.verbosity != Verbosity.SILENT)
        say(getHeader());

      results = new TestResults(getThreshold(), verbosity);

      for (final String[] sample: testData) {
        final DataKinds dataKind = checkDataKind(sample);
        if (dataKind == DataKinds.SIGNAL_TO_QUIT) break;
        if (dataKind != DataKinds.COMMENT)
          testOp(sample);
      }

      results.summarize();
      if (TesterClasses.verbosity != Verbosity.SILENT) {
        say(results.getReport("testing " + getName()));
        say("====\n");
      }
      return results;
    }

    /** Builds and returns a header for the report including the specific test name,
     * provided by {@link #getName()}, and the error threshold value. */
    private String getHeader() {
      return String.format("Testing %s\nwith err threshold = %.3e\n", getName(), getThreshold());
    }

    /**
     * Identifies the kind of the data item.
     * A data item may be an actual data sample that contains a proper data for testing an operation,
     * or a comment to be printed to the console, or an instruction to quit the execution of the test.
     * If the item is a comment (i.e. the first string starts with "//") and the mode is talkative,
     * prints the comment on the console. Returns a value that indicates the kind of the sample.
     * Used by the {@code #doTest()} method to find out what to do with the given data sample.
     * @param dataSample a data item (an array of strings) to be processed
     * @return one of the {@link DataKinds} values indicating the kind of the data item
     */
    private DataKinds checkDataKind(String[] dataSample) {
      if (isEmpty(dataSample[0]) || dataSample[0].trim().startsWith("//")) {
        if (TesterClasses.verbosity == Verbosity.TALKATIVE) {
          if (dataSample[0] != null)
            say(dataSample[0]);
          else
            say();
        }
        return DataKinds.COMMENT;
      }
      if (dataSample[0].toLowerCase().equals("$_stop_$")) {
        if (TesterClasses.verbosity != Verbosity.SILENT) {
          say("Instructed to exit the test!!!");
          say();
        }
        return DataKinds.SIGNAL_TO_QUIT;
      }
      return DataKinds.DATA;
    }

    /**
     * An implementation provided by a specific descendant tests a specific operation
     * on the given data sample and registers the results in the instance of {@link TestResults} class
     * held in the {@link #results} variable of {@code this} instance of the tester.<br>
     * The data sample is expected to be an array of {@code String}s where the first string
     * or the first two strings contain string representations of the operands
     * for the tested operation and the second (or the third) string contains
     * the expected result of the tested operation. In most cases the expected result
     * can be calculated by the instance of the tester class based on the value(s) of the argument(s).
     * In such cases the element intended to hold the expected value can be {@code null} or an empty string.
     * @param dataSample an array containing {@code String} representations of the input argument(s)
     * of the tested operation and a string representation of the expected result
     */
    abstract protected void testOp(String[] dataSample);

    /** Returns the value of the threshold of acceptable relative errors set for this instance.<br>
     * The default implementation returns the default value of the threshold,
     * {@link Consts#NORM_ERR_THRESH}.
     * Relative errors whose values don't exceed the threshold are inevitable for most operations
     * and considered acceptable.
     * Testers which deal with data types or operations that may produce greater relative errors
     * may have to override this method to return another value.
     * @return the value of the error threshold set for this instance
     */
    protected static double getThreshold() {
      return NORM_ERR_THRESH;
    }

  } // static abstract class QuadTester implements QuadOpTester {

  /**
   * A common ancestor of the tester classes that test conversions and unary operations, like {@link Quadruple#toString()}
   *  or {@link Quadruple#sqrt(Quadruple)}.<br><br>
   *
   * Defines abstract methods to be implemented by descendants:<ul style="list-style-position: outside">
   *  <li>{@link #performOp(Object)} whose concrete implementations are to perform the tested operation,
   * </ul><br>
   * Implements protected methods (that can be overridden) to be used by descendants:<br><ul style="list-style-position: outside">
   *  <li>{@link #otherTypeName()}      --  to return the name of the type the conversion from which
   *                                        or to which is tested. The default implementation returns "Quadruple"
   *                                        required for unary operations on {@code Quadruple}s.
   *  <li>{@link #makeExpectedItem(DataItem, String)}   --  to create a {@link DataItem} with the (provided or evaluated)
   *                                        expected value of the conversion,
   *  <li>{@link #findExpectedResult(Quadruple)} --  to find the value that is expected to be the correct result
   *                                        of the conversion,
   *  <li>{@link #findExpectedString(Quadruple)} --  to finds the string representing a value that can't be
   *                                        expressed as a {@code BigDecimal}
   * </ul>
   * @param <S> The type of the operand of the tested operation
   * @param <R> The type of the result of the tested operation
   */
  static abstract class UnaryFunctionTester<S, R> extends QuadTester {

    protected String otherTypeClassName = null;

    /**
     * A concrete descendant should provide an implementation of this method that performs
     * the tested operation with the given operand.
     * @param operand the argument for the operation being tested
     * @return the result of the operation
     */
    abstract protected R performOp(S operand);

    /**
     * Returns the simple name of the source type for conversions from other types to {@code Quadruple},
     * or the simple name of the result type for conversions from {@code Quadruple} to other types,
     * or "Quadruple" for unary operations. The default implementation returns "Quadruple".
     * @return the simple name of the type participating in the tested conversion, other than {@code Quadruple},
     * or "Quadruple" for unary operations
     */
    protected String otherTypeName() {
      return "Quadruple";
    }

    /**
     * Creates a {@link DataItem} with the the value of the expected result of the tested operation.
     * If the expected value is provided within the data sample, uses it, otherwise
     * calculates the expected value using the {@link #findExpectedResult} and {@link #findExpectedString(Quadruple)}
     * methods that can be overridden by descendants.
     * @param srcValue a {@code DataItem} containing the source value for the conversion being tested
     * @param expextedString the element of the data sample that's intended to hold the expected value
     * @return a {@code DataItem} containing the expected value of the conversion for the given source value
     */
    protected DataItem makeExpectedItem(DataItem srcValue, String expextedString) {
      if (isEmpty(expextedString)) { // Expected value not provided, deduce from source
        return deduceExpected(srcValue);
      } else {
        return new DataItem("exp").withQuadValueOfString(expextedString);
      }
    } // protected DataItem makeExpectedItem(DataItem srcValue, String expextedString) {

    /**
     * Calculates the value of the expected result of the tested operation.<br>
     * The default implementation returns the {@link BigDecimal} value of the given {@link Quadruple} operand.
     * If the value can't be represented as {@code BigDecimal} ({@code NaN} or {@code Infinity}), throws {@code NumberFormatException}.
     * In cases of functions or narrowing conversions, such as {@link Quadruple#doubleValue()}, descendants should override this method to provide
     * the value that equals the corresponding value of the target type.
     * @param operand the value to be converted
     * @return the expected result of the tested operation as a {@code BigDecimal}
     */
    protected BigDecimal findExpectedResult(Quadruple operand) { return bigDecimalValueOf(operand); };

    /**
     * Returns a string representation of the calculated expected value
     * in cases when it can't be represented as a {@code BigInteger}.
     * Specific testers, that has to return "NaN" or "Infinity" for some
     * specific operands or operand combinations, ought to override
     * this method. The default implementation returns null and does not use the parameter.
     * @param operand not used in the default implementation
     * @return the default implementation returns null. Specific descendants that require this method should provide a proper value.
     */
    protected String findExpectedString(Quadruple operand) { return null; };

    /**
     * Creates a {@code DataItem} with the expected result of the tested operation
     * in cases when the expected value is not provided within the data sample.
     * @param srcData a {@code DataItem} containing the source value for the operation
     * @return a {@code DataItem} containing the value of the expected result or an error message in case of failure.
     */
    private DataItem deduceExpected(DataItem srcData) {
      final Quadruple quadValue = srcData.getQuadValue();
      final DataItem di = new DataItem("exp");
      if (quadValue == null)
        return di.withError(String.format("error converting %s to a number", srcData.getStrValue()));
      BigDecimal bdValue = null;
      try {
        bdValue = findExpectedResult(quadValue);
        if (quadValue.isNegative() && bdValue.signum() == 0
            && !otherTypeName().equals("Long")         // ++ 20.12.24 18:30:02 for longs and ints, there does not exist -0
            && !otherTypeName().equals("Integer")
            && !otherTypeName().equals("QuadToBigDecimalToQuad")  // ++ 21.02.19 16:11:33 For Q -> BD -> Q, BigDecimal can't be -0
            )
          return di.withQuadruple(new Quadruple(true, 0,0,0));  // Respect -0.0
        else
          return di.withValueOf(bdValue);
      } catch (final Exception x) {                         // Can't be converted to BigDecimal
        final String strOfQuadValue = quadToString43(quadValue);
        if (quadValue.exponent() == Quadruple.EXP_INF
            && !x.getMessage().equals("Error was expected"))      // it's NaN or Infinity
          return di.withValueOfString(strOfQuadValue);      // A String "NaN" or "Infinity"

        final String expextedStr = findExpectedString(quadValue);
        if (expextedStr != null)
          return di.withValueOfString(expextedStr);         // For double, if it's NaN or Infinity, returns respective String designation

        return di.withError(String.format("%s\n%19s converting %s to %s", // e.g. "NaN" to BigDecimal
                                          x.toString(), "", strOfQuadValue, otherTypeName()));
      }
    } // private DataItem deduceExpected(DataItem srcData) {

  } // static abstract class UnaryFunctionTester<S, R> extends QuadTester {

  /**
   * An abstract tester class whose descendants perform testing of conversions from
   * {@code Quadruple} to other types, like {@link Quadruple#toString()} or {@link Quadruple#doubleValue}.<br><br>
   *
   * Implements protected methods:<ul style="list-style-position: outside">
   * <li>{@link UnaryFunctionTester#testOp(String[])} --  performs the tested operation, creates {@link DataItem}
   *      instances with the data involved with the test, and passes them to the {@link TestResults} instance,
   *      stored in the corresponding instance variable, to register the result.<br>
   * <li>{@link UnaryFunctionTester#otherTypeName()} -- returns the simple name of the actual result type using
   *      the implementation of the {@link UnaryFunctionTester#performOp(Object)} method provided by a concrete descendant.
   *      </ul>
   *
   * @param <R> The target type, i.e. the type of the result of the conversion
   */
  static abstract class Conversion_Q2T_Tester<R> extends UnaryFunctionTester<Quadruple, R> {

    /**
     * Tests a conversion from {@code Quadruple} to type R.<br>
     * Creates {@link DataItem} instances for the source input value taken from the data sample,
     * for the actual result (of type {@code R}) of the tested operation applied to the source value,
     * for the {@code Quadruple} and {@code BigDecimal} values of the result,
     * and for the expected value of the result, all with the corresponding data, and registers
     * the result in the {@link TestResults} instance stored in the corresponding instance
     * variable using its {@link TestResults#record(DataItem, DataItem, DataItem, DataItem)} method.
     */
    @Override
    protected void testOp(String[] dataSample) {
      final DataItem srcData      = new DataItem("src").withQuadValueOfString(dataSample[0]); // Makes an item with source value as BD and Quad
      final DataItem result       = makeResultItem(srcData);                          // Takes Quadruple from srcData and performs tested op, puts result into rawData
      final DataItem resultValue  = new DataItem("val").withValueOf(result.getRawData());  // Takes rawData and converts it into BD and Quad
      final DataItem expected     = makeExpectedItem(srcData, dataSample[1]);         // expected if provided, else converts source type
      results.record(srcData, result, resultValue, expected);                         // it computes the error as well
    }

    /**
     * Performs the tested operation and creates a {@code DataItem} with its result,
     * or with an error message in case of failure.
     * @param srcData a data item containing the source value for the tested operation
     * @return a data item containing the result of the tested operation or an error message in case of failure
     */
    private DataItem makeResultItem(DataItem srcData) {
      final Quadruple quadValue = srcData.getQuadValue();
      try {
        final Object result = (quadValue == null) ? null : performOp(quadValue); // may be of any type
        return new DataItem("res").withRawValue(result);
      } catch (final Exception x) {
        return new DataItem("res").withError(
                                    String.format("%s\n%19s performing %s on %s",
                                                  x.toString(), "", getName(), quadToString43(quadValue)) );
      }
    }

    /**
     * Returns the simple name of the actual type of the result of the tested operation.<br>
     * Uses an implementation of the
     * {@link UnaryFunctionTester#performOp(Object)} method provided by a concrete descendant,
     * to obtain an instance of the result type.
     */
    @Override
    protected String otherTypeName() {
      if (otherTypeClassName == null)
        otherTypeClassName = performOp(new Quadruple(0)).getClass().getSimpleName(); // performOp returns an instance of the class conversion to which is tested
      return otherTypeClassName;
    };

  } // static abstract class Conversion_Q2T_Tester<S, R> extends UnaryFunctionTester<Quadruple, R> {

  /**
   * An abstract tester class whose descendants perform testing of conversions from
   * other types to {@code Quadruple}, like {@link Quadruple#Quadruple(String)}
   * or {@link Quadruple#Quadruple(double)}.<br>
   * <br>
   * Implements protected methods:<ul style="list-style-position: outside">
   * <li>{@link UnaryFunctionTester#testOp(String[])} -- performs the tested operation
   *      and registers the result in the {@link TestResults} instance stored in the corresponding field.
   * <li>{@link UnaryFunctionTester#otherTypeName()} -- returns the simple name of the
   *      input type of the tested conversion.<br>
   * <li>{@link UnaryFunctionTester#performOp(Object)} -- performs the tested conversion
   *      by creating a new instance of {@code Quadruple} via call to a constructor
   *      with appropriate type of the argument, depending on the source type.
   * </ul><br>
   *
   * Defines abstract {@link #parseSrcType(String)} method whose implementations provided by the descendants
   * should return a value of the source type expressed by the input string from the data sample.<br><br>
   *
   * @param <S> the type of the input value of the tested conversion
   */
  static abstract class Conversion_T2Q_Tester<S> extends UnaryFunctionTester<S, Quadruple> {

    /**
     * Tests a conversion from type S to {@code Quadruple}.<br>
     * Creates {@link DataItem} instances for the source value of type S from the data sample
     * and for the same value expressed as {@code Quadruple} and {@code BigDecimal} values,
     * performs the tested operation and creates a {@link DataItem} with the result,
     * creates a {@link DataItem} with the expected value of the result, then registers
     * the result in the {@link TestResults} instance stored in the corresponding field
     * using its {@link TestResults#record(DataItem, DataItem, DataItem, DataItem)} method.
     */
    @Override
    protected void testOp(String[] dataSample) {
      final DataItem srcData  = makeSrcItem(dataSample[0]);   // Makes an item with specific source type
      final DataItem srcValue = new DataItem("val").withValueOf(srcData.getRawData());
      final DataItem result   = makeResultItem(srcData);
      final DataItem expected = makeExpectedItem(srcValue, dataSample[1]); // expected if provided, else convert source type
      results.record(srcData, srcValue, result, expected);    // it computes the error as well
    } // protected void testOp(String[] dataSample) {

    /**
     * Returns the simple name of the input type of the tested conversion.<br>
     * Uses the {@link #parseSrcType(String)} method of a concrete
     * descendant to obtain an instance of the concrete input type.
     */
    @Override
    protected String otherTypeName() {
      if (otherTypeClassName == null)
        otherTypeClassName = parseSrcType("0").getClass().getSimpleName();
      return otherTypeClassName;
    };

    /**
     * Performs the tested conversion by creating a new instance of {@code Quadruple}
     * via call to a constructor with appropriate type of the argument, depending on the source type.
     */
    @Override
    protected Quadruple performOp(S operand) {
      if (operand == null) return null;
      if (operand instanceof String)      return new Quadruple((String)operand);
      if (operand instanceof Double)      return new Quadruple((Double)operand);
      if (operand instanceof Integer)     return new Quadruple((Integer)operand);
      if (operand instanceof Long)        return new Quadruple((Long)operand);
      if (operand instanceof BigDecimal)  return new Quadruple((BigDecimal)operand);
      throw new IllegalArgumentException("Can't perform " + getName() + " on " + operand.getClass().getSimpleName());
    }

    /**
     * Parses a string expressing an input value for the tested conversion
     * and returns the corresponding value of type <b>S</b>.<br>
     * A concrete implementation should be provided by a concrete descendant.
     * Indirectly used by {@link #testOp(String[])}
     * @param s a string representing the input value of type S
     * @return the value of type <b>S</b>, expressed by the input string
     */
    protected abstract S parseSrcType(String s);

    /**
     * Creates a new {@code DataItem} that contains a 'raw' value of type <b>S</b>
     * to be converted by the tested method, or an error message in case of error during parsing the input string.
     * @param s a {@code String} containing a representation of the the value to be converted to {@code Quadruple}
     * @return a {@code DataItem} containing the corresponding value of type <b>S</b> or an error message
     */
    private DataItem makeSrcItem(String s) {
      try {
        return new DataItem("src").withRawValue(parseSrcType(s));
      } catch (final Exception x) {
        return new DataItem("src").withError(String.format("%s\n%19s parsing '%s' as %s",
                                                            x.toString(), "", s, otherTypeName()),
                                             s);
      }
    } //private DataItem makeSrcItem(String s) {

    /**
     * Performs the tested operation with the value of type <b>S</b> extracted
     * from the given {@code DataItem} and creates another {@code DataItem} with the result of the operation
     * or with an error message in case of failure
     * @param srcData a {@code DataItem} with the source value
     * @return a {@code DataItem} with the result or with an error message
     */
    @SuppressWarnings("unchecked")
    private DataItem makeResultItem(DataItem srcData) {
      try {
        return new DataItem("res").withQuadruple(performOp((S)srcData.getRawData()));
      } catch (final Exception x) {
        return new DataItem("res").withError(
            String.format("%s\n%19s performing %s on %s", x.toString(), "", getName(), srcData.getRawData()));
      }
    } // private DataItem makeResultItem(DataItem srcData) {

  } // abstract class Conversion_T2Q_Tester<S> extends UnaryFunctionTester {

  /**
   * An abstract tester class whose descendants perform testing of unary operations with {@code Quadruple}.<br>
   * <br>
   * Implements protected methods:<ul style="list-style-position: outside">
   * <li>{@link UnaryFunctionTester#testOp(String[])} -- performs the tested operation
   *      and register the result in the {@link TestResults} instance stored in the corresponding field.
   *      </ul>
   */
  static abstract class UnaryQuadrupleFunctionTester extends UnaryFunctionTester<Quadruple, Quadruple> {

    /**
     * Tests an unary function on Quadruple.<br>
     * Creates a {@link DataItem} instance with the source value expressed as {@code Quadruple} and {@code BigDecimal} values,
     * performs the tested operation and creates a {@link DataItem} with the result,
     * creates a {@link DataItem} with the expected value of the result, then registers
     * the result in the {@link TestResults} instance stored in the corresponding field
     * using its {@link TestResults#record(DataItem, DataItem, DataItem)} method.
     */
    @Override
    protected void testOp(String[] dataSample) {
      final DataItem srcData  = new DataItem("src").withQuadValueOfString(dataSample[0]);
      final DataItem result   = makeResultItem(srcData);
      final DataItem expected = makeExpectedItem(srcData, dataSample[1]); // expected if provided, else convert source type
      results.record(srcData, result, expected);   // it computes an error as well
    };

    /**
     * Performs the tested operation with the {@code Quadruple} value extracted
     * from the given {@code DataItem} and creates another {@code DataItem} with the result of the operation
     * or with an error message in case of failure.
     * @param srcData a {@code DataItem} with the source value
     * @return a {@code DataItem} with the result or with an error message
     */
    private DataItem makeResultItem(DataItem srcData) {
      try {
        return new DataItem("res").withQuadruple(performOp(srcData.getQuadValue()));
      } catch (final Exception x) {
        if (srcData.getQuadValue() == null)                 // could not parse source as Quadruple
          return new DataItem("res").withQuadruple(null);
        final String srcString = srcData.getRawData() != null?
            String.format("%s", srcData.getRawData()) :
            String.format("%s", srcData.getQuadValue());
        return new DataItem("res").withError(String.format("%s\n%19s performing %s on %s", // could not perform tested operation
                                                             x.toString(), "", getName(), srcString));
      }
    }

  } // static abstract class UnaryQuadrupleFunctionTester extends UnaryFunctionTester<Quadruple, Quadruple> {

  /**
   * An abstract tester class whose descendants perform testing of binary operations with {@code Quadruple}s,
   * such as {@link Quadruple#add(Quadruple, Quadruple)} or {@link Quadruple#divide(Quadruple, Quadruple)}.<br>
   * <br>
   * Implements protected methods:<ul style="list-style-position: outside">
   * <li>{@link UnaryFunctionTester#testOp(String[])} -- tests the operation with the given arguments
   *      and registers its result in the {@link TestResults} instance stored in the corresponding field.
   *      </ul><br>
   * Defines abstract methods to be implemented by descendants:<ul style="list-style-position: outside">
   *  <li>{@link #performOp(Quadruple, Quadruple)} -- to perform the tested operation,
   *  <li>{@link #findExpectedResult(Quadruple, Quadruple)} --  to find the value that is expected to be
   *      the correct result of the tested operation,
   *  <li>{@link #findExpectedString(Quadruple, Quadruple)} --  to find the string representing a value
   *      that can't be expressed as a {@code BigDecimal}
   * </ul>
   */
  static abstract class BinaryFunctionTester extends QuadTester {

    /**
     * Tests a binary function on Quadruple.<br>
     * Creates two {@link DataItem} instances with the values of the operands,
     * each containing {@code Quadruple} and {@code BigDecimal} equivalents of the corresponding
     * operand, performs the tested operation with the operands and creates a {@link DataItem}
     * with the result, creates a {@link DataItem} with the expected value of the result,
     * then registers the result in the {@link TestResults} instance stored in the corresponding field
     * using its {@link TestResults#record(DataItem, DataItem, DataItem, DataItem)} method.
     */
    @Override
    protected void testOp(String[] dataSample) {
      final DataItem op1Item  = new DataItem("op1").withQuadValueOfString(dataSample[0]);
      final DataItem op2Item  = new DataItem("op2").withQuadValueOfString(dataSample[1]);
      final Quadruple op1Value = op1Item.getQuadValue(), op2Value = op2Item.getQuadValue();
      final DataItem result     = makeResultItem(op1Value, op2Value);
      final DataItem expected   = makeExpectedItem(op1Value, op2Value, dataSample[2]); // expected if provided, else convert source type
      results.record(op1Item, op2Item, result, expected);   // it computes the error as well
    }

    /**
     * An implementation provided by a descendant should perform the tested operation with the given
     * operands and return its result.
     * @param operand1 the first operand of the tested function,
     * @param operand2 the second operand of the tested function,
     * @return the result of the operation
     */
    abstract protected Quadruple performOp(Quadruple  operand1, Quadruple  operand2);

    /**
     * An implementation provided by a descendant should calculate the expected result of the tested operation
     * applied to the given operands and return its value as a {@code BigDecimal}.
     * @param operand1 the first operand of the tested function,
     * @param operand2 the second operand of the tested function,
     * @return an expected value of the result of the operation
     */
    abstract protected BigDecimal findExpectedResult(Quadruple operand1, Quadruple operand2);

    /**
     * An implementation provided by a descendant should return a string representation
     * of the expected result in case when the result can't be expressed as a {@code BigDecimal},
     * for instance if the result should be {@code NaN} or {@code Infinity}.
     * @param operand1 the first operand of the tested function,
     * @param operand2 the second operand of the tested function,
     * @return a string expressing a value of the expected result
     */
    abstract protected String findExpectedString(Quadruple operand1, Quadruple operand2);

    /**
     * Performs the tested operation with the given operands
     * and creates and returns a {@code DataItem} instance with the value of the result
     * @param operand1 the first operand of the tested function,
     * @param operand2 the second operand of the tested function,
     * @return a new {@code DataItem} with the value of the result of the operation
     */
    private DataItem makeResultItem(Quadruple operand1, Quadruple operans2) { // 19.11.26 15:50:53
      final Quadruple result = (operand1 == null || operans2 == null)?
                                  null : performOp(operand1, operans2);
      return new DataItem("res").withQuadruple(result);
    };

    /**
     * Creates a {@code DataItem} instance with the expected result of the operation.
     * If the expString value is provided, uses it, otherwise calculates the expected result
     * based on the values of the given operands, using {@link #findExpectedResult(Quadruple, Quadruple)}
     * or {@link #findExpectedString(Quadruple, Quadruple)}.
     * @param operand1 the first operand of the tested function,
     * @param operand2 the second operand of the tested function,
     * @param expString
     * @return
     */
    private DataItem makeExpectedItem(Quadruple operand1, Quadruple operans2, String expString) { // 19.11.26 15:51:02
      if (!isEmpty(expString))
        return new DataItem("exp").withValueOfString(expString);
      try {
        BigDecimal expectedResult  = findExpectedResult(operand1, operans2);
        if (expectedResult.abs().compareTo(MIN_VALUE.divide(BD_TWO)) < 0)
          expectedResult = BigDecimal.ZERO;
        return new DataItem("exp").withValueOf(expectedResult);
      } catch (final Exception x) {                            // Can't be represented as a BigDecimal
        return new DataItem("exp").withValueOfString(findExpectedString(operand1, operans2));
      }
    }

  } // static abstract class BinaryFunctionTester extends QuadTester {

  private static boolean isEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

}
