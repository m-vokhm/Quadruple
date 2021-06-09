package com.mvohm.quadruple.test;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.research.MathContext;
import com.mvohm.quadruple.research.RoundingMode;
import com.mvohm.quadruple.research.BigDecimal;

import static com.mvohm.quadruple.test.AuxMethods.*;

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

    testDivisions(qd1, qd2);
    testDivisions(qv(5), qv(3));
    testDivisions(qv(3), qv(5));
    final List<String[]> dataList = DataProviders.divisionDataList();
    for (final String[] dataSample: dataList) {
      if (!dataSample[0].trim().startsWith("//"))
        testDivisions(qv(dataSample[0]), qv(dataSample[1]));
    }

//    testDivisions()
  }

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
    say();
    say("%s / %s =", qd1, qd2);
    final Quadruple qdQuotient1 = Quadruple.divide(qd1, qd2);
    final Quadruple qdQuotient2 = Quadruple.divide_2(qd1, qd2);
    final Quadruple qdQuotient3 = qd1.divide_2(qd2);
    say("q1 = " + qdQuotient1);
    say("q2 = " + qdQuotient2);
    say("q3 = " + qdQuotient3);
  }

}
