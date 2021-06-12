package com.mvohm.quadruple.test;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.research.MathContext;
import com.mvohm.quadruple.research.RoundingMode;
import com.mvohm.quadruple.research.BigDecimal;

import static com.mvohm.quadruple.test.AuxMethods.*;
import static com.mvohm.quadruple.test.DataGenerators.Randoms.randomsForDivision;
import static com.mvohm.quadruple.research.Dividers.*;

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

  private static final MathContext MC_38 = new MathContext(38, RoundingMode.HALF_EVEN);

  public static void main(String[] args) {
//    final BigDecimal bd1 = new BigDecimal("1.9876543210987654321098765432109876543");
//    final BigDecimal bd2 = new BigDecimal("1.2345678901234567890123456789012345678");
//    final BigDecimal bd3 = bd1.divide(bd2, MC_38);
//    say();
//    say(bd3);
//    say();
    final Quadruple qd1 = new Quadruple("1.9876543210987654321098765432109876543");
    final Quadruple qd2 = new Quadruple("1.2345678901234567890123456789012345678");

//    testDivisions(qd1, qd2);
//    testDivisions(qv(5), qv(3));
//    testDivisions(qv(3), qv(5));

//    final List<String> list = randomsForDivision(10000);
//    final Iterator<String> i = list.iterator();
//    while (i.hasNext()) {
//      final String s1 = i.next(), s2 = i.next(), s3 = i.next();
//      if (!s1.trim().startsWith("//"))
//        testDivisions(qv(s1), qv(s2));
//    }


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

    say();
    final long qdrAddBackCounter = getQdrAddBackCounter();
    final long mbiAddBackCounter = getMbiAddBackCounter(); //  / 2; if both instance and static divisions are performed
    final double qdrAddBackPercent = 100.0 * qdrAddBackCounter / divCounter;
    final double mbiAddBackPercent = 100.0 * mbiAddBackCounter / divCounter;

    say("Division counter  = " + divCounter);
    say("qdrAddBackCounter = %6d (%6.3f)", qdrAddBackCounter, qdrAddBackPercent);
    say("mbiAddBackCounter = %6d (%6.3f)", mbiAddBackCounter, mbiAddBackPercent);
    say("Error counter  =    " + errCount);

  }

  private static long divCounter = 0;
  private static long errCount = 0;

  private static Quadruple qv(String s) {
    return new Quadruple(s);
  }

  private static Quadruple qv(double d) {
    return new Quadruple(d);
  }

  /**
   * @param qd1
   * @param qd2
   */
  private static void testDivisions(final Quadruple qd1, final Quadruple qd2) {
    final long c1 = getMbiAddBackCounter();
    say("%s / %s =", qd1, qd2);
    final Quadruple qdQuotient1 = Quadruple.divide_0(qd1, qd2);
    final Quadruple qdQuotient2 = Quadruple.divide_1(qd1, qd2);
//    final Quadruple qdQuotient3 = qd1.divide_2(qd2);
    say("q0 = %s (%s)", qdQuotient1, hexStr(qdQuotient1));
    say("q1 = %s (%s)", qdQuotient2, hexStr(qdQuotient2));
    if (!qdQuotient1.equals(qdQuotient2) && (!qdQuotient1.isNaN() || !qdQuotient2.isNaN())) {
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
