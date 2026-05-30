package com.mvohm.quadruple.junit.data.generators;

import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

public class RandomGenerator {

  private static final long DEFAUULT_RANDOM_SEED = 1234567890;
  private static Random random;

  public static void initRandom() {
    random = new Random(DEFAUULT_RANDOM_SEED);
  }

  public static void initRandom(long randomSeed) {
    random = new Random(randomSeed);
  }

  public static DoubleStream randomDoubles(int count) {
    return random.doubles(count, Double.MIN_VALUE, Double.MAX_VALUE);
  }

  public static LongStream randomLongs(int count) {
    return random.longs(count, Long.MIN_VALUE, Long.MAX_VALUE);
  }

}
