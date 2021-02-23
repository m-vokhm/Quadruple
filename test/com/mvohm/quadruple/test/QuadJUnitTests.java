package com.mvohm.quadruple.test;

import static org.junit.Assert.assertArrayEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.mvohm.quadruple.test.SpecificTesterClasses.*;
import com.mvohm.quadruple.test.TesterClasses.Verbosity;

public class QuadJUnitTests {

  @Before
  public void setup() {
    TesterClasses.setVerbosity(Verbosity.MEDIUM);
    Locale.setDefault(Locale.US);
  }

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#toString()} method
   */
  @Test
  public void testQuadToStringConversion() {
    final TestResults results = new QuadToStringTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToStringConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#doubleValue()} method
   */
  @Test
  public void testQuadToDoubleConversion() {
    final TestResults results = new QuadToDoubleTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToDoubleConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#longValue()} method
   */
  @Test
  public void testQuadToLongConversion() {
    final TestResults results = new QuadToLongTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToLongConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#intValue()} method
   */
  @Test
  public void testQuadToIntConversion() {
    final TestResults results = new QuadToIntTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToIntConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#bigDecimalValue()} method
   */
  @Test
  public void testQuadToBdConversion() {
    final TestResults results = new QuadToBdTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToBdConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#Quadruple(String)} method
   */
  @Test
  public void testStringToQuadConversion() {
    final TestResults results = new StringToQuadTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStringToQuadConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#Quadruple(BigDecimal)} method
   */
  @Test
  public void testBdToQuadConversion() {
    final TestResults results = new BdToQuadTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testBdToQuadConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#Quadruple(Double)} method
   */
  @Test
  public void testDoubleToQuadConversion() {
    final TestResults results = new DoubleToQuadTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testDoubleToQuadConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#Quadruple(Long)} method
   */
  @Test
  public void testLongToQuadConversion() {
    final TestResults results = new LongToQuadTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } //public void testLongToQuadConversion() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#add(com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testInstanceAddition() {
    final TestResults results = new InstanceAdditionTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceAddition() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#subtract(com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testInstanceSubtraction() {
    final TestResults results = new InstanceSubtractionTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceSubtraction() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#multiply(com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testInstanceMultiplication() {
    final TestResults results = new InstanceMultiplicationTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceMultiplication() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#divide(com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testInstanceDivision() {
    final TestResults results = new InstanceDivisionTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceDivision() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#add(com.mvohm.quadruple.Quadruple, com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testStaticAddition() {
    final TestResults results = new StaticAdditionTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticAddition() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#subtract(com.mvohm.quadruple.Quadruple, com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testStaticSubtraction() {
    final TestResults results = new StaticSubtractionTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticSubtraction() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#multiply(com.mvohm.quadruple.Quadruple, com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testStaticMultiplication() {
    final TestResults results = new StaticMultiplicationTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticMultiplication() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#divide(com.mvohm.quadruple.Quadruple, com.mvohm.quadruple.Quadruple)} method
   */
  @Test
  public void testStaticDivision() {
    final TestResults results = new StaticDivisionTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticDivision() {

  /**
   * Tests {@code Quadruple -> String -> Quadruple} conversion that is expected to return the exact original value
   */
  @Test
  public void testQuadToStringToQuad() {
    final TestResults results = new QuadToStringToQuadTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToStringToQuad() {

  /**
   * Tests {@code Quadruple -> BigDecimal -> Quadruple} conversion that is expected to return the exact original value
   */
  @Test
  public void testQuadToBDToQuad() {
    final TestResults results = new QuadToBDToQuadTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testQuadToBDToQuad() {

  /**
   * Tests {@link com.mvohm.quadruple.Quadruple#sqrt(com.mvohm.quadruple.Quadruple) method }
   */
  @Test
  public void testStaticSqrt() {
    final TestResults results = new StaticSqrtTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testStaticSqrt() {

  /**
   * Tests instance {@link com.mvohm.quadruple.Quadruple#sqrt() method }
   */
  @Test
  public void testInstanceSqrt() {
    final TestResults results = new InstanceSqrtTester().test();
    assertArrayEquals( new int[] {
          results.getErrCount(),
          results.getBitDiffCount(),
          results.getSrcErrCount() },
        new int[] {0, 0, 0}
    );
  } // public void testInstanceSqrt() {

}
