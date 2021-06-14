package com.mvohm.quadruple.test;

import com.mvohm.quadruple.Quadruple;
//import com.mvohm.quadruple.research.MathContext;
//import com.mvohm.quadruple.research.RoundingMode;
//import com.mvohm.quadruple.research.BigDecimal;

import static com.mvohm.quadruple.test.AuxMethods.*;
import static com.mvohm.quadruple.research.Dividers.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;


/**
 * A hands-on simple division executor, for testing and debugging
 * alternative division algorithm
 * started 21.06.08 19:03:16
 *
 * @author M.Vokhmentev
 *
 */
public class SimpleDivisionTester {

  private static final MathContext MC_80 = new MathContext(80, RoundingMode.HALF_EVEN);

  public static void main(String[] args) {

//    testDivisions(
//        qv("1.230919777131638002153391684099464616718e-646457012"),
//        qv("5.286760186856926392516918077820158268184e-646457003")
//    );
//    exit();
//


//    testDivisions(qv(5), qv(30));
//    exit();
//
//    testDivisions(qv(6), qv(1));
//    testDivisions(qv(5), qv(6));
//
//    testDivisions(qv(30), qv(1));
//    testDivisions(qv(5), qv(1));
//    testDivisions(qv(30), qv(5));

    testWithDataList();

    say();

    final long qdrAddBackCounter = getQdrAddBackCounter();
    final long mbiAddBackCounter = getMbiAddBackCounter(); //  / 2; if both instance and static divisions are performed

    say("Division counter  = %6s", divCounter);
    say("qdrAddBackCounter = %6d (%6.3f)", qdrAddBackCounter, 100.0 * qdrAddBackCounter / divCounter);
    say("mbiAddBackCounter = %6d (%6.3f)", mbiAddBackCounter, 100.0 * mbiAddBackCounter / divCounter);
    say("Error counter     = %6s", errCount);
  }

  /**
   *
   */
  private static void testWithDataList() {
    int count = 0;
    final List<String[]> dataList = DataProviders.divisionDataList();
    for (final String[] dataSample: dataList) {
      if (!dataSample[0].trim().startsWith("//")) {
        say("Count = " + count++);
        testDivisions(qv(dataSample[0]), qv(dataSample[1]));
      } else {
        say(dataSample[0]);
      }
    }
  }

  private static long divCounter = 0;
  private static long errCount = 0;

  private static Quadruple qv(String s) {
    return new Quadruple(s);
  }

  private static Quadruple qv(double d) {
    return new Quadruple(d);
  }

  private static Quadruple qv(long mantHi, long mantLo, int exponent) {
    return new Quadruple(exponent, mantHi, mantLo);
  }

  /**
   * @param qd1
   * @param qd2
   */
  private static void testDivisions(final Quadruple qd1, final Quadruple qd2) {
    final long c1 = getMbiAddBackCounter();
    say("%s / %s =", qd1, qd2);
    Quadruple qdQuotient0 = null;
    try {
      qdQuotient0 = buildQuadruple(bigDecimalValueOf(qd1).divide(bigDecimalValueOf(qd2), MC_80));
    } catch (final Exception x) {};
    final Quadruple qdQuotient1 = Quadruple.divide(qd1, qd2);
    if (qdQuotient0 == null)
      qdQuotient0 = new Quadruple(qdQuotient1);
    if (qdQuotient0.isZero() && !qdQuotient0.isNegative() && (qd1.isNegative() != qd2.isNegative()))
      qdQuotient0.negate();
    say("q0 = %s (%s)", qdQuotient0, hexStr(qdQuotient0));
    say("q1 = %s (%s)", qdQuotient1, hexStr(qdQuotient1));
    if (!qdQuotient0.equals(qdQuotient1) && (!qdQuotient0.isNaN() || !qdQuotient1.isNaN())) {
      say("###########################################");
      errCount++;
    }
//    say("q3 = " + qdQuotient3);
    final long c2 = getMbiAddBackCounter();
    if (c2 != c1)
      say("Counter = %s -> %s", c1, c2);
    divCounter++;
    say();
  }

}
