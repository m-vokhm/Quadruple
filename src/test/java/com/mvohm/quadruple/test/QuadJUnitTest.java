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


import java.util.Locale;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.mvohm.quadruple.test.SpecificTesterClasses.*;
import com.mvohm.quadruple.test.IeeeConversionTesterClasses.*;
import com.mvohm.quadruple.test.TesterClasses.QuadTester;
import com.mvohm.quadruple.test.TesterClasses.Verbosity;
import com.mvohm.quadruple.Quadruple;
import static com.mvohm.quadruple.test.AuxMethods.say;


/**
 * A set of JUnit test methods. Each method tests a certain {@link Quadruple} operation.
 * @author M.Vokhmentev
 */
@TestInstance(Lifecycle.PER_CLASS)
public class QuadJUnitTest {

  /** The size of data arrays (that is the number of operations of each kind)
   * performed in every thread by the ThreadSafetyTester */
  private final static int DATA_SIZE = 200_000;
  /** The number of threads to be concurrently run by the
   * performed in every thread by the ThreadSafetyTester */
  private final static int NUMBER_OF_THREADS = 16;

  private static final TestResults totalResults = new TestResults(Consts.NORM_ERR_THRESH, Verbosity.SILENT);

  /**
   * Prepares the testing environment.<br>
   * Sets the medium level of verbosity and sets {@link java.util.Locale#US} as the default locale, to provide formatting
   * of numbers in such a way that they can be parsed by methods like {@link Double#valueOf(String)}
   */
  @BeforeAll
  public static void setup() {
    TesterClasses.setVerbosity(Verbosity.SILENT);
    DataProviders.setRandomCount(100);
    Locale.setDefault(Locale.US);
  }

  @AfterAll
  public void showResults() {
    say("======");
    say(totalResults.getReport(totalResults.getSummaryTestName()));
  }

  /**
   * Tests {@link Quadruple#toString()} method.<br>
   * Creates an instance of {@link QuadToStringTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToStringConversion() {
    final TestResults results = new QuadToStringTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToStringConversion() {

  /**
   * Tests {@link Quadruple#doubleValue()} method.<br>
   * Creates an instance of {@link QuadToDoubleTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToDoubleConversion() {
    final TestResults results = new QuadToDoubleTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToDoubleConversion() {

  /**
   * Tests {@link Quadruple#longValue()} method.<br>
   * Creates an instance of {@link QuadToLongTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToLongConversion() {
    final TestResults results = new QuadToLongTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToLongConversion() {

  /**
   * Tests {@link Quadruple#intValue()} method.<br>
   * Creates an instance of {@link QuadToIntTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToIntConversion() {
    final TestResults results = new QuadToIntTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToIntConversion() {

  /**
   * Tests {@link Quadruple#bigDecimalValue()} method.<br>
   * Creates an instance of {@link QuadToBdTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToBdConversion() {
    final TestResults results = new QuadToBdTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToBdConversion() {

  /**
   * Tests {@link Quadruple#Quadruple(String)} method.<br>
   * Creates an instance of {@link StringToQuadTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testStringToQuadConversion() {
    final TestResults results = new StringToQuadTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStringToQuadConversion() {

  /**
   * Tests {@link Quadruple#Quadruple(BigDecimal)} method.<br>
   * Creates an instance of {@link BdToQuadTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testBdToQuadConversion() {
    final TestResults results = new BdToQuadTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testBdToQuadConversion() {

  /**
   * Tests {@link Quadruple#Quadruple(double)} method.<br>
   * Creates an instance of {@link DoubleToQuadTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testDoubleToQuadConversion() {
    final TestResults results = new DoubleToQuadTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testDoubleToQuadConversion() {

  /**
   * Tests {@link Quadruple#Quadruple(long)} method.<br>
   * Creates an instance of {@link LongToQuadTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testLongToQuadConversion() {
    final TestResults results = new LongToQuadTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } //public void testLongToQuadConversion() {

  /**
   * Tests {@link Quadruple#add(Quadruple)} method.<br>
   * Creates an instance of {@link InstanceAdditionTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testInstanceAddition() {
    final TestResults results = new InstanceAdditionTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceAddition() {

  /**
   * Tests {@link Quadruple#subtract(Quadruple)} method.<br>
   * Creates an instance of {@link InstanceSubtractionTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testInstanceSubtraction() {
    final TestResults results = new InstanceSubtractionTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceSubtraction() {

  /**
   * Tests {@link Quadruple#multiply(Quadruple)} method.<br>
   * Creates an instance of {@link InstanceMultiplicationTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testInstanceMultiplication() {
    final TestResults results = new InstanceMultiplicationTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceMultiplication() {

  /**
   * Tests {@link Quadruple#divide(Quadruple)} method.<br>
   * Creates an instance of {@link InstanceDivisionTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testInstanceDivision() {
    final TestResults results = new InstanceDivisionTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceDivision() {

  /**
   * Tests {@link Quadruple#add(Quadruple, Quadruple)} method.<br>
   * Creates an instance of {@link StaticAdditionTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testStaticAddition() {
    final TestResults results = new StaticAdditionTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticAddition() {

  /**
   * Tests {@link Quadruple#subtract(Quadruple, Quadruple)} method.<br>
   * Creates an instance of {@link StaticSubtractionTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testStaticSubtraction() {
    final TestResults results = new StaticSubtractionTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticSubtraction() {

  /**
   * Tests {@link Quadruple#multiply(Quadruple, Quadruple)} method.<br>
   * Creates an instance of {@link StaticMultiplicationTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testStaticMultiplication() {
    final TestResults results = new StaticMultiplicationTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticMultiplication() {

  /**
   * Tests {@link Quadruple#divide(Quadruple, Quadruple)}method.<br>
   * Creates an instance of {@link StaticDivisionTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testStaticDivision() {
    final TestResults results = new StaticDivisionTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticDivision() {

  /**
   * Tests {@code Quadruple -> String -> Quadruple} conversion that is expected
   * to return the exact original value.<br>
   * Creates an instance of {@link QuadToStringToQuadTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToStringToQuad() {
    final TestResults results = new QuadToStringToQuadTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToStringToQuad() {

  /**
   * Tests {@code Quadruple -> BigDecimal -> Quadruple} conversion
   * that is expected to return the exact original value.<br>
   * Creates an instance of {@link QuadToBDToQuadTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToBDToQuad() {
    final TestResults results = new QuadToBDToQuadTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToBDToQuad() {

  /**
   * Tests {@link Quadruple#sqrt(Quadruple)} method.<br>
   * Creates an instance of {@link StaticSqrtTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testStaticSqrt() {
    final TestResults results = new StaticSqrtTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticSqrt() {

  /**
   * Tests instance {@link Quadruple#sqrt()} method.<br>
   * Creates an instance of {@link InstanceSqrtTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testInstanceSqrt() {
    final TestResults results = new InstanceSqrtTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceSqrt() {

  /* ********************************************************************************************
   ***** Methods to test IEEE-754 conversions:                                              *****
   *****    QuadToIEEELongsTester                                                           *****
   *****    QuadToIEEEBytesTester                                                           *****
   *****    AssignIEEELongsTester                                                           *****
   *****    AssignIEEEBytesTester                                                           *****
   **********************************************************************************************/

  /**
   * Tests {@link Quadruple#toIeee754Longs()} method.<br>
   * Creates an instance of {@link QuadToIEEELongsTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToIEEELongs() {
    final TestResults results = new QuadToIEEELongsTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToIEEELongs() {

  /**
   * Tests {@link Quadruple#toIeee754Bytes()} method.<br>
   * Creates an instance of {@link QuadToIEEEBytesTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testQuadToIEEEBytes() {
    final TestResults results = new QuadToIEEEBytesTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToIEEEBytes() {

  /**
   * Tests {@link Quadruple#assignIeee754(long[])} method.<br>
   * Creates an instance of {@link AssignIEEELongsTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testAssignIEEELongs() {
    final TestResults results = new AssignIEEELongsTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testAssignIEEELongs() {

  /**
   * Tests {@link Quadruple#assignIeee754(byte[])} method.<br>
   * Creates an instance of {@link AssignIEEEBytesTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors exceeding {@link Consts#NORM_ERR_THRESH}.
   */
  @Test
  public void testAssignIEEEBytes() {
    final TestResults results = new AssignIEEEBytesTester().test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testAssignIEEEBytes() {

  /**
   * Tests thread-safety of Quadruples.<br>
   * Creates an instance of {@link ThreadSafetyTester}, calls its {@link QuadTester#test()} method,
   * and verifies that the {@link TestResults} instance returned by it
   * does not indicate errors/
   */
  @Test
  public void testThreadSafety() {
    final TestResults results = new ThreadSafetyTester(DATA_SIZE, NUMBER_OF_THREADS).test();
    totalResults.register(results);
    assertArrayEquals( new int[] {
          results.getErrorCount(),
          results.getBitDifferenceCount(),
          results.getSourceErrorCount() },
        new int[] {0, 0, 0}
    );
  } // public void testAssignIEEEBytes() {

}
