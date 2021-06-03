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

import static com.mvohm.quadruple.test.Consts.MC_55_HALF_EVEN;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.TesterClasses.Verbosity;

/**
 * A device for collecting statistics on test errors and displaying the results of the performed tests.<br>
 *
 * An instance of this class is created by a tester class intended to test an individual
 * {@code Quadruple} method, before the tester class starts executing proper tests.<br>
 *
 * Then, on performing the test on each individual test case, a set of {@link DataItem}
 * instances that contain the source data, the operation result, and the expected value
 * of the result, is passed to methods {@linkplain #record(DataItem, DataItem, DataItem)}
 * or {@linkplain #record(DataItem, DataItem, DataItem, DataItem)} (depending on the type
 * of the operation being tested), which register the test results.<br>
 *
 * When the tests are finished, the summary test results, such as mean error, MSE,
 * and error counts, can be obtained via respective getters.<br>
 *
 * Besides that, a brief report of the form<pre>

 Results of testing operand.sqrt();
 on 1203 samples with err threshold 1.470e-39
  MSE        =  3.935e-79
  sqrt(MSE)  =  6.273e-40
  mean error = -6.756e-42
  Max error  =  1.469e-39
-------------------------
  Err %      =      0 (0.00%)
  Hex diff   =      0 (0.00%)
  Src errors =      0
</pre>

 * can be generated and obtained using {@link #getReport(String)} method.<br>
 *
 * During the test execution, the test data and the results for every individual test case
 * may get printed to the console, in case if the instance was created with {@code verbosity}
 * parameter that allows such output.
 * The output for a test case includes involved data in decimal and hexadecimal forms and
 * an indication of an error (if it presents), and may look like the following:
 <pre>

  1200: src:  6.0075035027764865517221905485888473758331471e+318423594 (+7c22_dca1_b2e1_451e 3b43_b0b0_ed29_35f5 e bf0c_723c)
        res:  2.4510209103099236006979103883530902996808415e+159211797 (+b92b_28a1_96a5_be40 fe4a_cb66_8df2_e5f1 e 9f86_391d)
        exp:  2.4510209103099236006979103883530902996813872e+159211797 (+b92b_28a1_96a5_be40 fe4a_cb66_8df2_e5f1 e 9f86_391d)
 &#42;**      39                                          ^^^^^            (-2.226e-40)</pre>

 * This shows the ordinal number of the data sample (1200 in the above example),
 * the value of the input argument for the operation being tested (in this example it's {@link Quadruple#sqrt()}),
 * marked with the "src" label, the value of the result of the operation (marked with "res"),
 * the value of the the expected result ("exp"), and the difference between the latter two:
 * decimal values differ in the 39-th digit after point, and the relative error is -2.226e-40.<br>
 * <br>
 * In case of error the output for a test sample may look like the following:
 <pre>

  4080: src:  2.0007498742609427977136284742732350987201479e+641685196 (+9641_7ab8_731c_133a 2c21_e350_af94_2715 e ff0e_1fd0)
        res:  2.000749874260942797714628474273235098720e+641685196
        val:  2.0007498742609427977146284742732350987200000e+641685196 (+9641_7ab8_731c_133a 2fe0_c5d3_ee92_9e6e e ff0e_1fd0)
        exp:  2.0007498742609427977136284742732350987201479e+641685196 (+9641_7ab8_731c_133a 2c21_e350_af94_2715 e ff0e_1fd0)
 $$$                                                                                          ^^^ ^^^^ ^^ ^ ^^^^
 &#42;*****   21                        ^                  ^^^^            (4.998e-22)</pre>

 * where the line starting with "$$$" shows the bitwise difference between the {@code Quadruple} values,
 * and the next line shows that the values are differ in the 21st digit after the point and the relative error is 4.998e-22.
 *
 * @author M.Vokhmentsev
 */
public class TestResults {
  // Output formatting
  private static final String   BAD_ERROR_MARK =        " ***** ";                            // to mark errors exceeding the threshold
  private static final String   ACCEPTABLE_ERROR_MARK = " ***   ";

  private static final int      TOTAL_NUM_LEN =         DataItem.FULL_BD_LENGTH;
  private static final int      NUM_POSITION =          BAD_ERROR_MARK.length() + 3 + 2 + 1;  // 3 spaces + diffIdx + space
  private static final int      HEX_START_POS =         NUM_POSITION + TOTAL_NUM_LEN + 1 + 1; // + space + parenthesis

  private static final char[]   DIFF_CHARS =            new char[TOTAL_NUM_LEN];              // length will equal to the length of the number

  private static final Pattern  HEX_STR_PATTERN =       Pattern.compile(".+\\((.+)\\).*");    // to extract what is put in brackets

  // Operation mode
  private final double errThreshold;
  private final Verbosity verbosity;


  // Error statistics
	private double mse;
	private double meanError;
	private double maxError;

	private int testCount;       // + 20.11.01 10:44:05 The number of tests performed -- for summary results
	private int sampleCount;
	private int errCount;
	private int bitDiffCount;
	private int srcErrCount;

	private boolean summarized = false;

  private String expStr, resStr; // To be computed once per data sample

	/**
	 * Creates a new instance with the given error threshold and verbosity mode.<br>
	 * The value passed in as the {@code threshold} parameter is used to discriminate
	 * an acceptable inaccuracy, inevitable by design, from errors that are indicatives of software bugs.
	 * Typically the threshold value for {@code Quadruple} is 1.470e-39,
	 * which corresponds to half of the least significant bit of the mantissa.
	 * The test cases with an error below the threshold don't affect the number of registered errors,
	 * yet do affect statistics (mean error, max error and MSE).
	 * The value of the {@code verbosity} parameter controls whether the test data for each
	 * data sample will be output to the console during testing. The only value that enables
	 * the output is {@code Verbosity.TALKATIVE}.
	 * @param threshold defines the threshold to distinguish an acceptable inaccuracy from an error
	 * @param verbosity controls the ability of the created instance to print data to the console. {@code Verbosity.TALKATIVE} enables the output.
	 */
	public TestResults(double threshold, Verbosity verbosity) {
	  this.verbosity = verbosity;
		errThreshold = threshold;
	}

  /**
   * Registers the result of a test of an operation on a single data instance for unary operations.<br>
   *
   * The {@code DataItem} parameters {@code srcData}, {@code result}, and {@code expected}
   * contain data pertaining to the test -- source data value (the input parameter of the tested operation),
   * the actual result of the operation performed on this value, and the expected result of the operation,
   * respectively. <br>
   *
   * If the output is enabled, i.e. the instance is created with the {@code Verbosity.TALKATIVE} parameter,
   * prints decimal and hexadecimal forms of the three values to the console.
   * Then, in case if the input data is marked as erroneous and the {@code expected} parameter does not indicate that
   * the error was expected for this data sample, registers the source error incrementing the {@code srcErrCount}
   * field and prints the corresponding message (if the output is enabled).<br>
   *
   * If the source data is error-free, compares the actual result of the operation with the expected result.
   * Checks the equality of the {@code Quadruple} values of the {@code result} and {@code expected} parameters,
   * and, if they differ, registers the difference by incrementing the value of the {@code bitDiffCount} field.
   * Then compares the {@code BigDecimal} values of these parameters, and, if they differ, calculates
   * the relative error and registers it by modifying {@code meanError}, {@code mse}, and {@code maxError}
   * fields, and, if the error exceeds the {@code threshold} value that was set by the constructor,
   * increments the {@code errCount}. If the output is enabled, prints corresponding error messages
   * indicating the error magnitude and the positions of characters that differ in the string representations
   * of the {@code result} and {@code expected} parameters.
   *
   * @param srcData the source data that was used for the test (the argument of the operation)
   * @param result the actual result of the tested operation
   * @param expected the expected result of the tested operation
   * @see DataItem
   */
  public void record(DataItem srcData, DataItem result, DataItem expected) {
    if (summarized)
      throw new IllegalArgumentException("Can't update a summarized data item");
    printDataItems(srcData, result, expected);
    sampleCount++;
    if (srcErrorDetected(srcData, srcData, result, expected)) // If there's an error in the source data,
      return;                                                 // no use to calculate the error of the operation

    checkError(expected, result) ;
  }

  /**
   * Registers the result of a test of an operation on a single data instance for binary operations
   * and conversions from {@code Quadruple} to other types and from other types to {@code Quadruple}.<br>
   *
   * The {@code DataItem} parameters {@code srcData}, {@code result}, {@code resultValue} and {@code expected}
   * contain data pertaining to the test:<ul>
   * <li>source data value (the input parameter of the tested operation) or the first of the two operands for binary operations,
   * <li>the actual result of the conversion performed on this value or the second operand for binary operations,
   * <li>the numeric value of the result,
   * <li>and the numeric value of the expected result of the operation, respectively. </ul>
   *
   * The {@code srcData} and {@code result} parameters do not necessarily contain numeric values;
   * in the case of testing conversions, one of them contains a value of the type
   * conversion to or from which is being tested.
   * Their values are not taken into account when evaluating errors,
   * except for the detection of possible errors in the source data.<br>
   *
   * If the output is enabled, i.e. the instance is created with the {@code Verbosity.TALKATIVE} parameter,
   * prints decimal and hexadecimal forms of the four values to the console.
   * For a parameter that does not contain {@code BigDecimal} and {@code Quadruple} values,
   * the value of its {@code strValue} field or, if it's empty, its {@code rawData} field is printed.<br>
   *
   * Then, in case if the input data is marked as erroneous and the {@code expected} parameter does not indicate that
   * the error was expected for this data sample, registers the source error incrementing the {@code srcErrCount}
   * field and prints the corresponding message (if the output is enabled).<br>
   *
   * If the source data is error-free, compares the value of the actual result of the operation with the expected result.
   * Checks the equality of the {@code Quadruple} values of the {@code resultValue} and {@code expected} parameters,
   * and, if they differ, registers the difference by incrementing the value of the {@code bitDiffCount} field.
   * Then compares the {@code BigDecimal} values of these parameters, and, if they differ, calculates
   * the relative error and registers it by modifying {@code meanError}, {@code mse}, and {@code maxError}
   * fields and, if the error exceeds the {@code threshold} value that was set by the constructor,
   * increments the {@code errCount}. If the output is enabled, prints corresponding error messages
   * indicating the error magnitude and the positions of characters that differ in the string representations
   * of the {@code resultValue} and {@code expected} parameters.
   *
   * @param srcData the source data of the tested conversion or the first argument of the tested binary operation
   * @param result the actual result of the tested conversion or the second argument of the tested binary operation
   * @param resultValue the numeric value of the actual result of the tested operation
   * @param expected the expected result of the tested operation
   * @see DataItem
   */
  public void record(DataItem srcData, DataItem result, DataItem resultValue, DataItem expected) {
    // 19.10.26 18:40:00
    if (summarized)
      throw new IllegalArgumentException("Can't update a summarized data item");
    printDataItems(srcData, result, resultValue, expected);
    sampleCount++;
    if (srcErrorDetected(srcData, result, resultValue, expected))
      return;

    checkError(expected, resultValue) ;
  }

  /**
   * Performs final calculations that must be done before using the results, such as
   * calculation of the mean error etc.<br>
   * Must be invoked by any of the particular tests after finishing test execution with all its test data,
   * before the results are registered with the summary results.
   */
  public void summarize() {
    meanError = meanError / sampleCount;
    mse = mse / sampleCount;
    summarized = true;
  }

  /**
   * Adds the results of a particular test performed on a specific operation,
   * accumulated by the passed-in instance of the {@code TestResults},
   * to the summary results being accumulated by {@code this} instance,
   * in order to evaluate summarized results for all performed tests.
   * @param testResults an instance of the {@code TestResults} containing the results of testing a specific operation
   */
  public void register(TestResults testResults) {
    if (summarized)
      throw new IllegalArgumentException("Can't update summarized results");
    if (!testResults.summarized)
      throw new IllegalArgumentException("Partial results must be summarized before their registration");

    this.testCount++;

    this.errCount     += testResults.errCount;
    this.bitDiffCount += testResults.bitDiffCount;
    this.sampleCount  += testResults.sampleCount;
    this.srcErrCount  += testResults.srcErrCount;

    this.maxError     = Math.max(this.maxError, testResults.maxError);
    this.meanError    += testResults.meanError * testResults.sampleCount;
    this.mse          += testResults.mse * testResults.sampleCount;
  }

  /**
   * Returns the number of data samples in which the detected relative error exceeded the
   * threshold set for this {@code TestResults} instance.
   * @return the number of errors exceeding the threshold
   */
  public int getErrorCount()     { return errCount; }

  /**
   * Returns the number of data samples in which the {@code Quadruple} value of the actual result
   * of the tested operation was not exactly equal to the {@code Quadruple} value of the expected result,
   * i.e. binary representations of the two {@code Quadruple} values differed by at least one bit.
   * @return the number of errors in binary representations of the results
   */
  public int getBitDifferenceCount() { return bitDiffCount; }

  /**
   * Returns the number of data samples in which input parameters
   * for the operation being tested were invalid, i.e. could not be parsed
   * as numeric values of the expected source type.
   * @return the number of errors in the input data
   */
  public int getSourceErrorCount()  { return srcErrCount; }

  /**
   * For summary results, returns a string containing the number of particular tests whose results were
   * accumulated to evaluate the summary results contained in this instance.<br>
   * The returned string reads "summary of N tests", where N is the number of performed tests.
   * @return A test name for the summary test. Indicates the number of partial tests taken into account
   */
  public String getSummaryTestName() {
    return "summary of " + testCount + " tests";
  }

  /**
   * Generates and returns a brief human-readable report containing statistics on encountered errors.<br>
   * The report looks like the following:<pre>

Results of testing op1.add(Quadruple op2)
on 3144 samples with err threshold 1.470e-39
  MSE        =  3.536e-79
  sqrt(MSE)  =  5.946e-40
  mean error =  1.355e-41
  Max error  =  1.469e-39
-------------------------
  Err %      =      0 (0.00%)
  Hex diff   =      0 (0.00%)
  Src errors =      0

</pre>
   * @param testName a name of the tested operation to include to the report (in the above example, it's "op1.add(Quadruple op2)")
   * @return a string consisting of a few lines with a human-readable representation of the test results, as described above
   */
  public String getReport(String testName) {
    if (!summarized)
      summarize();

    final StringBuilder sb =
        new StringBuilder(String.format("Results of %s \non %d samples with err threshold %.3e\n",
            testName, sampleCount, errThreshold));
    return appendReport(sb);
  }

  /* **********************************************************************
   ****** Private methods *************************************************
   ***********************************************************************/

  /**
   * If the output to the console is enabled, prints the contents of the given
   * {@code DataItem} instances, using their {@code toString()} methods.
   * The first line is prepended with the ordinal number of the data sample.
   * @param srcData
   * @param result
   * @param resultValue
   * @param expected
   */
  private void printDataItems(DataItem srcData, DataItem result, DataItem resultValue, DataItem expected) {
    sayIfVerbose(String.format("%6d: %s", sampleCount, srcData.toString()));
    sayIfVerbose(String.format("%7s %s", "", result.toString()));

    resStr = resultValue.toString();    // to avoid multiple calls to toString(). Next time will get it from there
    sayIfVerbose(String.format("%7s %s", "", resStr));
    expStr = expected.toString();       // to avoid multiple calls to toString()
    sayIfVerbose(String.format("%7s %s", "", expStr));
  }

  /**
   * If the output to the console is enabled, prints the contents of the given
   * {@code DataItem} instances, using their {@code toString()} methods.
   * The first line is prepended with the ordinal number of the data sample.
   * @param srcData
   * @param resultValue
   * @param expected
   */
  private void printDataItems(DataItem srcData, DataItem resultValue, DataItem expected) {
    sayIfVerbose(String.format("%6d: %s", sampleCount, srcData.toString()));

    resStr = resultValue.toString();    // to avoid multiple calls to toString()
    sayIfVerbose(String.format("%7s %s", "", resStr));
    expStr = expected.toString();       // to avoid multiple calls to toString()
    sayIfVerbose(String.format("%7s %s", "", expStr));
  }

  /**
   * Checks whether the data sample that was used for the test has an error in the input arguments.
   * Increments the {@code srcErrCount} if an error presents in a data sample where it was not foreseen
   * @param srcData
   * @param srcValue
   * @param result
   * @param expected
   * @return
   */
  private boolean srcErrorDetected(DataItem srcData, DataItem srcValue, DataItem result, DataItem expected) {
    if (    srcData.hasError()      // For binary operations, these two may hold the two operands
        ||  srcValue.hasError()     // An error in either of them should result to source error
        ||  result.hasError()
        ||  (expected.hasError() && !result.hasError()) ) { // e.g. parser did not throw exception where it should
      sayIfVerbose("    === Source data error! ===\n");
      if (   expected.getErrMsg() == null
          || !expected.getErrMsg().contains("expected")     // Count it unless the error was foreseen
          || (expected.hasError() &&
              !(srcData.hasError() || srcValue.hasError() || result.hasError())))  // Or error was expected but not encountered
        srcErrCount++;
      return true;
    }
    return false;
  }

  /**
   * Compares the values contained in the {@code expected} and {@code resultValue} parameters,
   * evaluates the bitwise difference between them and the relative error,
   * and updates the statistics appropriately.
   * If an error is found and the output is enabled, additionally prints one or two lines,
   * indicating the type, magnitude and the position of the error.
   * The format of these lines is shown in the description of the class.
   * @param expected a {@code DataItem} instance containing the expected result of the operation under the test
   * @param resultValue a {@code DataItem} instance containing the actual result of the operation under the test
   */
  private void checkError(DataItem expected, DataItem resultValue) {
    if (expected.getQuadValue() != null
        && !expected.getQuadValue().equals(resultValue.getQuadValue())) { // The Quadruple values differ
      sayIfVerbose_(findHexDiff(expStr, resStr));           // Print "$$$" and underline the difference in hex representations
      bitDiffCount++;
    }

    final double error = findError(resultValue, expected);  // the relative error or a substitute integer value. Updates statistics
    final String errLinePrefix = (Math.abs(error) >= errThreshold)? BAD_ERROR_MARK :
                                                                    ACCEPTABLE_ERROR_MARK;

    final int diffIdx = findBdDiff(expStr, resStr);       // Fills DIFF_CHARS with markers as needed

    if (error == 0 && diffIdx == Integer.MAX_VALUE) {
      sayIfVerbose(); return;
    }

    // diffIdx is actually number of digit after point, starting from 1: 0.001 <=> 3
    final String diffIdxStr = (diffIdx == Integer.MAX_VALUE)? "  " : String.valueOf(diffIdx);
    sayIfVerbose("%s   %2s %s (%.3e)\n", errLinePrefix, diffIdxStr, new String(DIFF_CHARS), error);
  }

  /**
   * Finds and registers the relative error using {@code BigDecimal} values of {@code result} and {@code expected}.
   * Accordingly modifies the error statistics -- fields {@code errCount}, {@code meanError},
   * {@code mse}, and {@code maxError}.
   * @param result the result of the operation being tested
   * @param expected the expected (correct) result of the operation being tested
   * @return the value found of the relative error
   */
  private double findError(DataItem result, DataItem expected) {
    final BigDecimal bdResult = result.getBDValue();
    final BigDecimal bdExpected = expected.getBDValue();

    if (bdResult == null && bdExpected == null) return 0;
    if (bdResult == null)                       return -bdExpected.signum() * 2;
    else if (bdExpected == null)                return bdResult.signum() * 2;
    else if (bdResult.signum() == 0 && bdExpected.signum() == 0)
                                                return 0;
    else if (bdResult.signum() == 0)            return -bdExpected.signum();
    else if (bdExpected.signum() == 0)          return bdResult.signum();

    final double error = bdResult.subtract(bdExpected, MC_55_HALF_EVEN).divide(bdExpected, MC_55_HALF_EVEN).doubleValue();

    final double absErr = Math.abs(error);
    if (absErr < errThreshold * 10e-10)
      return 0;

    if (absErr >= errThreshold)
      errCount++;

    meanError   += error;
    mse         += error * error;
    maxError    = Math.max(maxError, absErr);
    return error;
  }

  /**
   * Returns a string indicating the difference in hexadecimal representations of the values.
   * The string starts with "$$$" and contains chars '^' in the positions where hexadecimal digits of the two values differ.
   * @param str1 the first string to compare
   * @param str2 the second string to compare
   * @return
   */
  private Object findHexDiff(String str1, String str2) {
    Arrays.fill(DIFF_CHARS , ' ');
    final String hexStr1 = extractHex(str1), hexStr2 = extractHex(str2);
    boolean errFlag = false;
    if (hexStr1 != null && hexStr2 != null) {
      final int maxIdx = Math.min(hexStr1.length(), hexStr2.length());
      for (int i = 0; i < maxIdx; i++)
        if (hexStr1.charAt(i) != hexStr2.charAt(i)) {
          DIFF_CHARS[i] = '^';
          errFlag = true;
        }
    }
    if (errFlag)
      return String.format("%-" + HEX_START_POS + "s%s\n", " $$$", new String(DIFF_CHARS));
    return "";
  }

  /**
   * Extracts the part of the string that's in the brackets
   * @param str1
   * @return
   */
  private String extractHex(String str1) {
    final Matcher m1 = HEX_STR_PATTERN.matcher(str1); // Pattern.compile(".+\\((.+)\\).*");    // to extract what is put in brackets
    final String s = m1.find()? m1.group(1) : null;
    return s;
  }

  /**
   * Finds the difference between decimal representations of the values and fills DIFF_CHARS with appropriate markers
   * @param str1 a string containing the first value to compare
   * @param str2 a string containing the second value to compare
   * @return the position of the first difference found, or Integer.MAX_VALUE, if no differences found
   */
  private int findBdDiff(String str1, String str2) {
    final int prefixLength = 5; // "src: " etc, not to be compared
    final int intPartLen = 2;   // e.g. "-1.", and counting digits from 1
    Arrays.fill(DIFF_CHARS , ' ');
    int diffIdx = Integer.MAX_VALUE;
    for (int i = prefixLength; i < Math.min(DIFF_CHARS.length - prefixLength, Math.min(str1.length(), str2.length())); i++)
      if (str1.charAt(i) != str2.charAt(i)) {
        DIFF_CHARS[i - prefixLength] = '^';
        diffIdx = Math.min(diffIdx, i);
      }
    if (diffIdx != Integer.MAX_VALUE)
      return diffIdx - prefixLength - intPartLen; // To indicate position after point
    return Integer.MAX_VALUE;
  }

  /**
   * Generates a report consisting of a number of strings including the values accumulated in the fields of the instance
   * and appends it to the given {@code StringBuilder}.
   * @param sb a {@code StringBuilder} instance to append the report to
   * @return a {@code String} containing the concatenation of the initial contents
   * of the given {@code StringBuilder} and the generated report
   */
	private String appendReport(StringBuilder sb) {
	  if (!summarized)
	    summarize();

		final double errPercent = errCount * 100.0 / sampleCount;
		final double hexDiffPercent = bitDiffCount * 100.0 / sampleCount;
		sb.append(String.format("  MSE        = %10.3e\n", mse));
		sb.append(String.format("  sqrt(MSE)  = %10.3e\n", Math.sqrt(mse)));
		sb.append(String.format("  mean error = %10.3e\n", meanError));
    sb.append(String.format("  Max error  = %10.3e\n", maxError));
    sb.append("-------------------------\n");
		sb.append(String.format("  Err %%      = %6d (%.2f%%)\n", errCount, errPercent)); // "%%" means "%", so looks shifted
		sb.append(String.format("  Hex diff   = %6d (%.2f%%)\n", bitDiffCount, hexDiffPercent));
		sb.append(String.format("  Src errors = %6d", srcErrCount));
		if (maxError >= errThreshold && errThreshold != 0) {
			try { Thread.sleep(75); }  	// To output normal data first,
			catch (final Exception x) {};   	// before the following err message
			System.err.println(String.format("  Max error  = %.3e\n", maxError));
		}
		return sb.toString();
	}


	private void sayIfVerbose() {
    if (verbosity == Verbosity.TALKATIVE)
      AuxMethods.say();
  }

  private void sayIfVerbose(String format, Object... values) {
    if (verbosity == Verbosity.TALKATIVE)
      AuxMethods.say(format, values);
  }

  private void sayIfVerbose_(Object o) {
	  if (verbosity == Verbosity.TALKATIVE)
	    AuxMethods.say_(o);
  }

}
