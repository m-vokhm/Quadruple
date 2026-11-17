package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.Quadruple.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;
import com.mvohm.quadruple.testutil.QuadAssert;

/**
 * Data for {@code AssignTest}.
 */
public class AssignData {

  private static final int Q2Q_RANDOM_COUNT = 1000;

  /**
   * Returns a new list of new "dirty" instances, i.e. instances that already hold
   * some value before {@code assign(...)} is called on them.
   * <p>
   * The set covers all kinds of values (zeros, special, normal, subnormal) and
   * includes an instance with all mantissa bits set, so that any field left
   * unassigned by an {@code assign(...)} method becomes visible.
   * <p>
   * Instances are built from their low-level parts, independently of the tested
   * {@code assign(double)} and {@code assign(long)}. A new list is created on each call,
   * since the tests modify the instances.
   *
   * @return a list of named instances holding various values
   */
  public static List<Named<Quadruple>> dirtyInstances() {
    return List.of(
        Named.of("+0",                         new Quadruple(false, 0, 0, 0)),
        Named.of("-0",                         new Quadruple(true,  0, 0, 0)),
        Named.of("NaN",                        Quadruple.nan()),
        Named.of("+Infinity",                  Quadruple.positiveInfinity()),
        Named.of("-Infinity",                  Quadruple.negativeInfinity()),
        Named.of("1.0",                        new Quadruple(false, EXPONENT_OF_ONE, 0, 0)),
        Named.of("MAX_VALUE",                  Quadruple.maxValue()),
        Named.of("MIN_VALUE",                  Quadruple.minValue()),
        Named.of("largest subnormal",          new Quadruple(false, 0, -1L, -1L)),
        Named.of("negative, all mantissa bits set",
                                               new Quadruple(true, EXPONENT_OF_ONE + 5, -1L, -1L))
      );
  }

  /**
   * Returns the double values for {@code assign(double)} tests:
   * the same as for the creation tests, except {@code ±Double.MIN_VALUE}
   * (see BUG-01 in KNOWN_ISSUES.md; these values are tested separately).
   *
   * @return a stream of double values
   */
  public static Stream<Double> doubleValuesForAssign() {
    return CreationData.doubleValuesForCreation()
                       .filter(d -> Math.abs(d) != Double.MIN_VALUE);
  }

  /**
   * Returns the long values for {@code assign(long)} tests,
   * the same as for the creation tests.
   *
   * @return a stream of long values
   */
  public static Stream<Long> longValuesForAssign() {
    return CreationData.longValuesForCreation();
  }

  private static final int RAW_PARTS_RANDOM_COUNT = 1000;

  /** Exponents (as stored in the field, i.e. biased) to combine with {@link #EDGE_MANTISSAS} */
  private static final int[] EDGE_EXPONENTS = {
      0,                                // zero / subnormal
      1,                                // lowest normal
      EXPONENT_OF_ONE,                  // 0x7FFF_FFFF
      EXPONENT_OF_ONE + 1,              // 0x8000_0000, == Integer.MIN_VALUE as int
      (int)EXPONENT_OF_MAX_VALUE,       // 0xFFFF_FFFE
      EXPONENT_OF_INFINITY,             // 0xFFFF_FFFF, NaN / infinity
  };

  /** Pairs {mantHi, mantLo} to combine with {@link #EDGE_EXPONENTS} */
  private static final long[][] EDGE_MANTISSAS = {
      { 0, 0 },
      { 0, 1 },
      { 0x8000_0000_0000_0000L, 0 },
      { -1L, -1L },
  };

  /**
   * Returns low-level parts of Quadruple values for tests of {@code assign(boolean, int, long, long)}
   * and similar methods:
   * <ul>
   *   <li>all combinations of both signs, edge exponents, and edge mantissas;</li>
   *   <li>random parts.</li>
   * </ul>
   * Any combination of parts is admissible for these methods, so the data are not restricted
   * to valid finite values.
   *
   * @return a stream of Quadruple parts
   */
  public static Stream<QuadParts> rawPartsForAssign() {
    final Stream.Builder<QuadParts> edge = Stream.builder();
    for (final boolean negative : new boolean[] { false, true })
      for (final int exponent : EDGE_EXPONENTS)
        for (final long[] mant : EDGE_MANTISSAS)
          edge.add(new QuadParts(negative, exponent, mant[0], mant[1]));
    return Stream.concat(edge.build(), new RandomGenerator().quadrupleParts(RAW_PARTS_RANDOM_COUNT));
  }

  /**
   * Returns Quadruple values for {@code assign(Quadruple)} tests:
   * <ul>
   *   <li>the values of the dirty instances (zeros, special values, extreme values);</li>
   *   <li>values with non-zero {@code mantLo} (they can't be obtained from double or long);</li>
   *   <li>values with random parts: random sign, mantissa and exponent, including subnormal ones.</li>
   * </ul>
   *
   * @return a stream of named Quadruple values
   */
  public static Stream<Named<Quadruple>> quadrupleValuesForAssign() {
    final RandomGenerator random = new RandomGenerator();
    return Stream.of(
        dirtyInstances().stream(),
        Stream.of(
            Named.of("1.0 + lowest mantissa bit", new Quadruple(false, EXPONENT_OF_ONE, 0, 1)),
            Named.of("mixed mantissa bits",       new Quadruple(true,  EXPONENT_OF_ONE - 100,
                                                                0x0123_4567_89AB_CDEFL, 0xFEDC_BA98_7654_3210L)),
            Named.of("subnormal, mantLo only",    new Quadruple(false, 0, 0, 0x8000_0000_0000_0000L))),
        random.quadruples(Q2Q_RANDOM_COUNT)
              .map(q -> Named.of(QuadAssert.fieldsOf(q), q)))
      .flatMap(s -> s);
  }

}
