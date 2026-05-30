package com.mvohm.quadruple.junit.data;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.DoubleStream;

import static com.mvohm.quadruple.junit.data.constants.ConstantArrays.*;
import static com.mvohm.quadruple.junit.data.generators.RandomGenerator.*;

public class CreationData {

  private static int D2Q_RANDOM_COUNT = 3000;
  private static int L2Q_RANDOM_COUNT = 3000;

  /**
   * Returns a stream of double values for testing creation of Quadruple
   * instances from double operands.
   * <p>
   * The returned stream includes:
   * <ul>
   *   <li>basic special and boundary values;</li>
   *   <li>a deterministic sequence of pseudo-random values.</li>
   * </ul>
   *
   * @return a stream of double values for creation tests
   */
  public static Stream<Double> doubleValuesForCreation() {
    initRandom();
    return Stream.concat(Arrays.stream(BASIC_d2Q_CONVERSION_DATA).boxed(),
                         randomDoubles(D2Q_RANDOM_COUNT).boxed());
  }

  /**
   * Returns a stream of long values for testing creation of Quadruple
   * instances from long operands.
   * <p>
   * The returned stream includes:
   * <ul>
   *   <li>basic special and boundary values;</li>
   *   <li>a deterministic sequence of pseudo-random values.</li>
   * </ul>
   *
   * @return a stream of double values for creation tests
   */
  public static Stream<Long> longValuesForCreation() {
    initRandom();
    return Stream.concat(Arrays.stream(BASIC_l2Q_CONVERSION_DATA).boxed(),
                         randomLongs(L2Q_RANDOM_COUNT).boxed());
}}
