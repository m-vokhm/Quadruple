package com.mvohm.quadruple.testdata;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.mvohm.quadruple.testdata.constants.ConstantArrays.*;

import com.mvohm.quadruple.testdata.generators.RandomGenerator;

public class CreationData {

  private static final int D2Q_RANDOM_COUNT = 3000;
  private static final int D2Q_SUBNORMAL_RANDOM_COUNT = 1000;
  private static final int L2Q_RANDOM_COUNT = 3000;

  /**
   * Returns a stream of double values for testing creation of Quadruple
   * instances from double operands.
   * <p>
   * The returned stream includes:
   * <ul>
   *   <li>basic special and boundary values;</li>
   *   <li>a deterministic sequence of pseudo-random finite values of both signs over the whole range;</li>
   *   <li>a deterministic sequence of pseudo-random subnormal values.</li>
   * </ul>
   *
   * @return a stream of double values for creation tests
   */
  public static Stream<Double> doubleValuesForCreation() {
    final RandomGenerator random = new RandomGenerator();
    return Stream.of(
        Arrays.stream(BASIC_d2Q_CONVERSION_DATA).boxed(),
        random.doubles(D2Q_RANDOM_COUNT).boxed(),
        random.subnormalDoubles(D2Q_SUBNORMAL_RANDOM_COUNT).boxed())
      .flatMap(s -> s);
  }

  /**
   * Returns a stream of long values for testing creation of Quadruple
   * instances from long operands.
   * <p>
   * The returned stream includes:
   * <ul>
   *   <li>basic special and boundary values;</li>
   *   <li>a deterministic sequence of pseudo-random values of both signs
   *       with magnitudes of all bit lengths.</li>
   * </ul>
   *
   * @return a stream of long values for creation tests
   */
  public static Stream<Long> longValuesForCreation() {
    final RandomGenerator random = new RandomGenerator();
    return Stream.concat(Arrays.stream(BASIC_l2Q_CONVERSION_DATA).boxed(),
                         random.longs(L2Q_RANDOM_COUNT).boxed());
  }

}
