package com.mvohm.quadruple.test;

import java.util.HashMap;
import java.util.Locale;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.test.SpecificTesterClasses.*;
import com.mvohm.quadruple.test.TesterClasses.QuadTester;
import com.mvohm.quadruple.test.TesterClasses.Verbosity;

import static com.mvohm.quadruple.test.AuxMethods.*;

import java.io.IOException;

/**
 * A stand-alone program for testing methods of the {@code Quadruple} class.<br>
 * Creates a set of {@code tester}s, one for each of the operations
 * of the {@link Quadruple} class that need to be tested.
 * Each {@code tester} instance is an instance of a specific descendant of the {@link QuadTest} abstract class
 * and is intended to test respective operation, like {@code a.add(b)} or {@code q.toString()}.
 * Iterating through the testers, calls their {@code test()} methods, that return instances of the
 * {@code TestResults} class containing statistics on results of the test, such as the number of errors,
 * mean error, maximum error, MSE, etc. These instances are passed to the {@code register()} method
 * of another instance of the {@code TestResults}, that collects the summary results on all performed tests.
 * After finishing the tests, the summary statistics is printed to the console as following:<pre>
Results of summary of 20 tests
on 70209 samples with err threshold 1.470e-39
  MSE        =  1.450e-79
  sqrt(MSE)  =  3.807e-40
  mean error =  3.592e-42
  Max error  =  1.469e-39
-------------------------
  Err %      =      0 (0.00%)
  Hex diff   =      0 (0.00%)
  Src errors =      0</pre>
  * <b>Usage:</b>
 * <pre>
 * java QuadTest [-v:verbosity],
 *   verbosity: 0 -- silent (no output except the summary),
 *              1 -- medium (outputs the results of specific tests),
 *              2 -- talkative (outputs data and errors for each data sample)";
 *</pre>
 * @author M.Vokhmentsev
 *
 */
public class QuadTest {

  private static final String USAGE = "\nRuns tests of operations of the Quadruple class.\n"
      + "Usage:\n"
      + "  java QuadTest [-v:verbosity] [-r:randCount]\n"
      + "  verbosity: 0 -- silent (no output except the summary),\n"
      + "             1 -- medium (default, outputs the results of specific tests),\n"
      + "             2 -- talkative (outputs data and errors for each data sample).\n"
      + "  randCount: 0 -- do not include random numbers in the test data,\n"
      + "             n -- generate n random samples for each test, default value is 3000\n"
      + "";

  /** Acceptable error for all operations is a little less than
   (0.5 * LSB == 2^-129) * 1.0005 == ~1.4693679e-39 * 1.0005 = 1.47010258395e-39 */
  static final double NORM_ERR_THRESH = 1.470e-39; // 2^-129 * 1.0005

  /**
   * A static array containing the testers for all operations that need to be tested
   */
  private final QuadTester[] testers = new QuadTester[] {

  // Conversions from Quadruple to other types
    new QuadToStringTester(),
    new QuadToDoubleTester(),
    new QuadToLongTester(),
    new QuadToIntTester(),
    new QuadToBdTester(),               // 6 source errors testing NaNs etc

  // Conversions from other types to Quadruple
    new StringToQuadTester(),             // 14 source errors testing detection of syntax errors
    new BdToQuadTester(),                 // 3 source errors testing detection of syntax errors
    new DoubleToQuadTester(),
    new LongToQuadTester(),

  // Binary operations
    new InstanceAdditionTester(),
    new InstanceSubtractionTester(),
    new InstanceMultiplicationTester(),
    new InstanceDivisionTester(),
    new StaticAdditionTester(),
    new StaticSubtractionTester(),
    new StaticMultiplicationTester(),
    new StaticDivisionTester(),

  // Reverse conversion Quadruple -> String -> Quadruple
    new QuadToStringToQuadTester(),   // 14 source errors inherited from basic_S2Q_conversionData
    new QuadToBDToQuadTester(),

  // Unary operation
    new StaticSqrtTester(),
    new InstanceSqrtTester(),       // 37 total source errors
  };

  /** The values of the valid keys of the command-line arguments */
  private enum ArgumentKeys {
    VERBOSITY,        // -v:n n = 0 - silent, n = 1 - medium,  n = 2 - talkative
    RANDOM_COUNT      // -r:n -- n -- the number of random samples to generate for each test
  }

  @SuppressWarnings("unused")
  static public void main(String...args) throws IOException {
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
   * @param args -- the command-line arguments to parse
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
   * @param arg -- a command-line argument that's expected to be a pair "key:value"
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
   * @param keyValue -- an array of two Strings containing the key and the value extracted from an argument
   * @return anything except null in case of error, null if OK
   */
  private static String parsePair(String[] keyValue) { // returns null if OK, not null otherwise
    final String key = keyValue[0], value = keyValue[1];
    switch (key) {
      case "-v":    return setVerbosity(value);
      case "-r":    return setRandomCount(value);
      default:
    } // switch (key) {
    return key;
  } // private static String parsePair(String[] keyValue) {

  /**
   * Puts the value of verbosity, extracted from the command-line arguments,
   * to a hashtable containing keys and values to control the execution mode,
   * under the corresponding key.
   * @param value -- the value to put to the hashtable for the key {@code ArgumentKeys.VERBOSITY},
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

    final TestResults totalResults = new TestResults(TesterClasses.NORM_ERR_THRESH);
    for (final QuadTester t: testers) {
      final TestResults results = t.test();
      totalResults.register(results);
      if (results.getErrCount() != 0 || results.getBitDiffCount() != 0)
        break;
    }
    say("======");
    say(totalResults.getReport(totalResults.getSummaryTestName()));
  }

}

