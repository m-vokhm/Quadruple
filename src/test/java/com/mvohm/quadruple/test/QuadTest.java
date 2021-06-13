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

import java.util.HashMap;
import java.util.Locale;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.SpecificTesterClasses.*;
import com.mvohm.quadruple.test.IeeeConversionTesterClasses.*;
import com.mvohm.quadruple.test.TesterClasses.QuadTester;
import com.mvohm.quadruple.test.TesterClasses.Verbosity;

import static com.mvohm.quadruple.test.AuxMethods.*;

/**
 * A stand-alone program for testing methods of the {@link com.mvohm.quadruple.Quadruple} class.
 * <p>
 * Creates a set of {@code tester}s, one for each of the operations of the
 * {@link Quadruple} class that need to be tested. Each {@code tester} is an instance
 * of a specific concrete descendant of abstract class
 * {@link com.mvohm.quadruple.test.TesterClasses.QuadTester}
 * and is intended to test respective operation of {@code Quadruple}, such as
 * {@link Quadruple#add(Quadruple)} or {@link Quadruple#sqrt()}.
 * Concrete tester classes are defined in the {@link SpecificTesterClasses} class.
 * <p>
 * The {@link #main(String...)} method parses the command-line arguments and sets
 * the execution mode accordingly, then iterates through the testers and calls their
 * {@link com.mvohm.quadruple.test.TesterClasses.QuadTester#test()} methods,
 * which return instances of the {@link TestResults} class containing information about
 * the results of the test, such as the number of errors, mean error, maximum error, MSE, etc.
 * <p>
 * These instances are then passed to the {@link TestResults#register(TestResults)} method
 * of another instance of the {@code TestResults}, that accumulates the summary results
 * for all performed tests. After finishing the tests, the summary statistics is printed
 * to the console as following:<br>
 * <hr><pre>
Results of summary of 20 tests
on 70209 samples with err threshold 1.470e-39
  MSE        =  1.450e-79
  sqrt(MSE)  =  3.807e-40
  mean error =  3.592e-42
  Max error  =  1.469e-39
-------------------------
  Err %      =      0 (0.00%)
  Hex diff   =      0 (0.00%)
  Src errors =      0</pre><hr>
  <b>Usage:</b>
 * <pre>
  java QuadTest [-v:verbosity] [-r:randCount]
  verbosity: 0 -- silent (no output except the summary),
             1 -- medium (default, outputs the results of specific tests),
             2 -- talkative (outputs data and errors for each data sample).
  randCount: 0 -- do not include random numbers in the test data,
             n -- generate n random samples for each test, default value is 3000
 *</pre>
 * @author M.Vokhmentsev
 *
 */
public class QuadTest {

  private static final String USAGE = "\nTests the operations of the Quadruple class.\n"
      + "Usage:\n"
      + "  java QuadTest [-v:verbosity] [-r:randCount] [-x:exitMode]\n"
      + "  verbosity: 0 -- silent (no output except the summary),\n"
      + "             1 -- medium (default, outputs the results of specific tests),\n"
      + "             2 -- talkative (outputs data and errors for each data sample).\n"
      + "  randCount: 0 -- do not include random numbers in the test data,\n"
      + "             n -- generate n random samples for each test, default value is 3000\n"
      + "  exitMode:  \"y\" -- stop running tests and exit immediately if one of the tests ends with errors\n"
      + "             \"n\" -- to continue testing regardless of errors encountered\n"
      + "";

  /**
   * A static array containing the testers for all operations that need to be tested
   */
  private final QuadTester[] testers = new QuadTester[] {

  // Conversions from Quadruple to other types
//    new QuadToStringTester(),
//    new QuadToDoubleTester(),
//    new QuadToLongTester(),
//    new QuadToIntTester(),
//    new QuadToBdTester(),               // 6 source errors testing NaNs etc
//    new QuadToIEEELongsTester(),
//    new QuadToIEEEBytesTester(),
//
//
//  // Conversions from other types to Quadruple
//    new StringToQuadTester(),             // 14 source errors testing detection of syntax errors
//    new BdToQuadTester(),                 // 3 source errors testing detection of syntax errors
//    new DoubleToQuadTester(),
//    new LongToQuadTester(),
//    new AssignIEEELongsTester(),
//    new AssignIEEEBytesTester(),
//
//
//  // Binary operations
//    new InstanceAdditionTester(),
//    new InstanceSubtractionTester(),
//    new InstanceMultiplicationTester(),

//    // 21.06.08 18:40:13 Trying to find a faster way to divide
    new InstanceDivision_0_Tester(),
    new InstanceDivision_1_Tester(),
    new InstanceDivision_2_Tester(),
    new InstanceDivision_3_Tester(),

//    new StaticAdditionTester(),
//    new StaticSubtractionTester(),
//    new StaticMultiplicationTester(),
    new StaticDivision_0_Tester(),
    new StaticDivision_1_Tester(),
    new StaticDivision_2_Tester(),
    new StaticDivision_3_Tester(),
//
//    new InstanceMaxTester(),
//    new InstanceMinTester(),
//    new StaticMaxTester(),
//    new StaticMinTester(),
//
//  // Reverse conversion Quadruple -> String -> Quadruple
//    new QuadToStringToQuadTester(),   // 14 source errors inherited from basic_S2Q_conversionData
//    new QuadToBDToQuadTester(),
//
//  // Unary operation
//    new StaticSqrtTester(),
//    new InstanceSqrtTester(),       // 37 total source errors
//
  };

  /** The values of the valid keys of the command-line arguments */
  private enum ArgumentKeys {
    VERBOSITY,        // -v:n n = 0 - silent, n = 1 - medium,  n = 2 - talkative
    RANDOM_COUNT,     // -r:n -- n -- the number of random samples to generate for each test
    EXIT_ON_ERROR     // -x:y -- letter 'y' or 'Y' means exit on error
  }

  /**
   * Performs the tests.<br>
   * Parses the command line arguments and accordingly sets the execution mode,
   * then iterates through the testers contained in a statically defined {@code testers} array
   * and calls their {@link com.mvohm.quadruple.test.TesterClasses.QuadTester#test()} methods.
   * The results and statistics for individual tests are accumulated in a
   * {@link com.mvohm.quadruple.test.TestResults} instance and displayed to the console
   * after all tests have completed.
   * @param args the command line arguments
   */
  @SuppressWarnings("unused")
  static public void main(String...args) { // throws IOException { // if uses the logger
    Locale.setDefault(Locale.US);
    if (parseArgs(args)) { // returns true of args are OK
      // A simplest "logger" (actually just a PrintStream), if needed
      // change the path to point where appropriate
      // AuxMethods.openLog("F:/git/QuadFloat/QuadFloat/Data/wrk_20_09_28/log.txt");
      new QuadTest().run();
      // AuxMethods.closeLog();
    }
  }

  /**
   * Parses the command-line arguments and sets accordingly variables that control the mode of the execution.
   * The only argument currently usable is '-v' that controls verbosity
   * @see #USAGE
   * @param args the command-line arguments to parse
   * @return true if all the arguments are valid, false otherwise
   */
  private static boolean parseArgs(String[] args) {
    String errorFound = null;
    for (final String arg: args) {
      say("arg: " + arg);
      errorFound = parseArg(arg);
      if (errorFound != null) {
        say("Invalid parameter: " + errorFound);
        say(USAGE);
        System.exit(1);
      }
    }
    return true;
  }

  /**
   * A single argument is expected to be a pair "key:value"
   * @param arg a command-line argument that's expected to be a pair "key:value"
   * @return anything except null in case of error, null if OK
   */
  private static String parseArg(String arg) {
    final String[] parts = arg.toLowerCase().split(":");
    if (parts.length != 2)
      return arg;
    final String result =  parsePair(parts);
    return (result == null)? null : arg;
  }

  /**
   * Processes a key-value pair extracted from a command-line argument
   * @param keyValue an array of two Strings containing the key and the value extracted from an argument
   * @return anything except null in case of error, null if OK
   */
  private static String parsePair(String[] keyValue) { // returns null if OK, not null otherwise
    final String key = keyValue[0], value = keyValue[1];
    switch (key) {
      case "-v":    return setVerbosity(value);
      case "-r":    return setRandomCount(value);
      case "-x":    return setExitOnErrorMode(value);
      default:
    } // switch (key) {
    return key;
  } // private static String parsePair(String[] keyValue) {

  /**
   * Puts the value of verbosity, extracted from the command-line arguments,
   * to a hashtable containing keys and values to control the execution mode,
   * under the corresponding key.
   * @param value the value to put to the hashtable for the key {@code ArgumentKeys.VERBOSITY},
   * expected to be a String representing an integer.
   * @return not null as a sign of an error, if the value is not a valid String for an int value
   */
  private static String setVerbosity(String value) {
    final String result = setNumericArgValue(ArgumentKeys.VERBOSITY, value);
    if (result != null) return result;
    // Translate the value from int to Verbosity
    try {
      final Long longValue = (Long)COMMAND_LINE_ARGS.get(ArgumentKeys.VERBOSITY);
      COMMAND_LINE_ARGS.put(ArgumentKeys.VERBOSITY, Verbosity.values()[longValue.intValue()]);
      return null;
    } catch (final Exception x) {
      return "bad argument value";
    }
  }

  /**
   * Puts the value of RandomCount, extracted from the command-line arguments,
   * to a hashtable containing keys and values to control the execution mode,
   * under the corresponding key.
   * @param value the value to put to the hashtable for the key {@code ArgumentKeys.RANDOM_COUNT},
   * expected to be a String representing an integer.
   * @return not null as a sign of an error, if the value is not a valid String for an int value
   */
  private static String setRandomCount(String value) {
    return setNumericArgValue(ArgumentKeys.RANDOM_COUNT, value);
  }

  /**
   * Puts the value of {@code EXIT_ON_ERROR} key, extracted from the command-line arguments,
   * to a hashtable containing keys and values to control the execution mode,
   * under the corresponding key.
   * @param value the value to put to the hashtable for the key {@code ArgumentKeys.EXIT_ON_ERROR},
   * expected to be a letter "y" or "n".
   * @return not null as a sign of an error, if the value is not "y" neither "n".
   */
  private static String setExitOnErrorMode(String value) {
    return setYesNoArgValue(ArgumentKeys.EXIT_ON_ERROR, value);
  }

  /**
   * Puts the value of the given key, extracted from the command-line arguments,
   * to a hashtable containing keys and values to control the execution mode,
   * under the corresponding key.
   * @param value the value to put to the hashtable for the key {@code ArgumentKeys.EXIT_ON_ERROR},
   * expected to be a letter "y" or "n".
   * @param argumentKey the key in the hashtable to put the value under
   * @param value the value to put into the hashtable under the given key, must be either "y" or "n"
   * @return not null as a sign of an error, if the value is not "y" neither "n".
   */
  private static String setYesNoArgValue(ArgumentKeys argumentKey, String value) {
    if (!"y".equals(value) && !"n".equals(value)) return value;
    COMMAND_LINE_ARGS.put(argumentKey, value);
    return null;
  }


  /**
   * Puts a value, that's expected to be a long value expressed by the given {@code String},
   * to a hashtable containing keys and values to control the execution mode,
   * under the given key.
   * @param argumentKey the key to use for the hashtable access
   * @param argumentValue the string representation of the value to put to the hashtable
   * @return
   */
  private static String setNumericArgValue(ArgumentKeys argumentKey, String argumentValue) {
    try {
      COMMAND_LINE_ARGS.put(argumentKey, Long.parseLong(argumentValue)); // remains null for the default mode
      return null;
    } catch (final Exception x) {
      return "bad argument value";
    }
  }

  /**
   * A dictionary containing {@code ArgumentKeys} keys and corresponding {@code Object} values
   * derived from the command-line arguments. Controls the execution mode.
   * Initialized with the default values of the command-line arguments.
   */
  @SuppressWarnings("serial")
  static private final HashMap<ArgumentKeys, Object> COMMAND_LINE_ARGS = new HashMap<ArgumentKeys, Object>() {{
    put(ArgumentKeys.VERBOSITY, Verbosity.MEDIUM);
    put(ArgumentKeys.RANDOM_COUNT, 3000L);
    put(ArgumentKeys.EXIT_ON_ERROR, "y");
  }};

  /**
   * Iterates through the testers contained in the {@code testers} array and calls their {@code test()} methods.
   * The results of the individual tests are accumulated in a separate {@code results} instance
   * and printed to the console when all the tests are finished.
   */
  private void run() {
// TODO 20.10.28 11:48:25 do it like anything else, with TestResults
//  testRandoms();
//    say(Quadruple.pi());
//    exit();

    TesterClasses.setVerbosity((Verbosity)COMMAND_LINE_ARGS.get(ArgumentKeys.VERBOSITY));
    DataProviders.setRandomCount(((Long)COMMAND_LINE_ARGS.get(ArgumentKeys.RANDOM_COUNT)).intValue());

    final TestResults totalResults = new TestResults(Consts.NORM_ERR_THRESH, Verbosity.SILENT);
    final boolean toStopOnError = "y".equals(COMMAND_LINE_ARGS.get(ArgumentKeys.EXIT_ON_ERROR));
    for (final QuadTester t: testers) {
      final TestResults results = t.test();
      totalResults.register(results);
      if (   (results.getErrorCount() != 0 || results.getBitDifferenceCount() != 0)
           && toStopOnError )
        break;
    }
    say("======");
    say(totalResults.getReport(totalResults.getSummaryTestName()));
  }

}

