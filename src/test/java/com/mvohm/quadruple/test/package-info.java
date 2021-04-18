/**
 *
 * A test appliance for methods of the Quadruple class.
 * The tests are performed with the specific tester classes that are descendants of the class QuadTester,
 * each operation is tested with a special descendant. The generic abstract tester classes are defined
 * in the TesterClasses.java, their specific descendants in the SpecificTesterClasses.java.
 * The test is performed by the method test() of a tester class. It obtains a list of test data samples
 * using one of the static methods of the DataProviders class, then, iterating over the data samples,
 * executes the tested operation with each data item, calculates or obtains from the data sample
 * the expected result of the operation, creates DataItems containing source data and the expected result,
 * and registers the result, passing the DataItems to an instance of the TestResults class,
 * that computes and accumulates statistics on test results, such as mean relative error, MSE etc.
 * After the test is finished, these values can be obtained via respective getters or printed to the console.
 *
 * The QuadTest class contains a main() method that executes all the tests autonomously.
 * The QuadJUnitTest contains JUnit test methods.
 *
 * Includes classes:<ul style="list-style-position: outside">
 * <li>{@link com.mvohm.quadruple.test.AuxMethods}            -- auxiliary static methods used by other classes of the package
 * <li>{@link com.mvohm.quadruple.test.Consts}                -- constants used by the other classes
 * <li>{@link com.mvohm.quadruple.test.DataGenerators}        -- static methods to generate sequences of test data with certain properties
 * <li>{@link com.mvohm.quadruple.test.DataItem}              -- a container for data pertaining to a test
 * <li>{@link com.mvohm.quadruple.test.DataProviders}         -- a set of static methods that generate and collect data sets
 *                                                              for specific operations
 * <li>{@link com.mvohm.quadruple.test.QuadJUnitTest}        -- a set of test methods to be used with JUnit
 * <li>{@link com.mvohm.quadruple.test.QuadTest}              -- an autonomous executor of all the tests
 * <li>{@link com.mvohm.quadruple.test.SpecificTesterClasses} -- concrete descendants of the QuadTester class,
 *                                                              each for a specific quadruple operation
 * <li>{@link com.mvohm.quadruple.test.TestData}              -- Arrays of statically-defined (hardcoded) test data
 * <li>{@link com.mvohm.quadruple.test.TesterClasses}         -- a hierarchy of abstract ancestors of the tester classes
 * <li>{@link com.mvohm.quadruple.test.TestResults}           -- a container capable to accumulate the statistics
 *                                                              on the results of the tests
 * </ul>
 *
 * @author M.Vokhmentsev
 *
 */

package com.mvohm.quadruple.test;