package com.mvohm.quadruple.testdata;

import static com.mvohm.quadruple.testdata.DataHelpers.scaled;
import static com.mvohm.quadruple.testdata.DataHelpers.copy;
import static com.mvohm.quadruple.testdata.DataHelpers.named;
import static com.mvohm.quadruple.testdata.DataHelpers.negated;
import static com.mvohm.quadruple.Quadruple.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import com.mvohm.quadruple.Quadruple;
import com.mvohm.quadruple.testdata.generators.RandomGenerator;

/**
 * Data for {@code ComparisonTest}: pairs of Quadruple values, and pairs of a Quadruple and a long or a double.
 * <p>
 * The core of the data is the "ladder": a list of values in strictly ascending order,
 * from -Infinity to +Infinity and NaN, whose adjacent values differ minimally
 * (by the lowest bit of {@code mantLo} or {@code mantHi}, by the transition from subnormal to normal values,
 * by the sign of zero), and which includes values where a signed comparison of the fields
 * would differ from an unsigned one (the exponent {@code 0x8000_0000}, mantissas with the highest bit set).
 * All pairs of the ladder values are used, along with NaNs with different bit patterns
 * and random pairs of close values.
 * <p>
 * Each pair consists of new instances, so that the values of a pair are never the same instance,
 * and the tests may modify them.
 */
public class ComparisonData {

  private static final int RANDOM_PAIR_COUNT   = scaled(3000);
  private static final int RANDOM_VALUE_COUNT  = scaled(200);     // to be compared with special values
  private static final int RANDOM_NUMBER_COUNT = scaled(1000);    // longs and doubles

  private static final long HIGH_BIT = 0x8000_0000_0000_0000L;

  /* ************************************************************************
   * The ladder and NaNs
   */

  /** Positive finite non-zero values in strictly ascending order */
  private static List<Named<Quadruple>> positiveSteps() {
    final int one = EXPONENT_OF_ONE, max = (int)EXPONENT_OF_MAX_VALUE;
    return List.of(
        q("MIN_VALUE",                         0, 0, 1),
        q("2 * MIN_VALUE",                     0, 0, 2),
        q("subnormal, mantLo = 0x8000...",     0, 0, HIGH_BIT),
        q("subnormal, mantLo = 0xFFFF...",     0, 0, -1),
        q("subnormal, mantHi = 1",             0, 1, 0),
        q("subnormal, mantHi = 0x8000...",     0, HIGH_BIT, 0),
        q("max subnormal",                     0, -1, -1),
        q("MIN_NORMAL",                        1, 0, 0),
        q("MIN_NORMAL + ulp",                  1, 0, 1),
        q("1 - ulp",                           one - 1, -1, -1),
        q("1",                                 one, 0, 0),
        q("1 + ulp",                           one, 0, 1),
        q("1, mantLo = 0x7FFF...",             one, 0, Long.MAX_VALUE),
        q("1, mantLo = 0x8000...",             one, 0, HIGH_BIT),
        q("1, mantLo = 0xFFFF...",             one, 0, -1),
        q("1, mantHi = 1",                     one, 1, 0),
        q("1, mantHi = 0x7FFF..., mantLo = 0xFFFF...", one, Long.MAX_VALUE, -1),
        q("1.5",                               one, HIGH_BIT, 0),
        q("2 - ulp",                           one, -1, -1),
        q("2 (exponent 0x8000_0000)",          one + 1, 0, 0),
        q("MAX_VALUE / 2",                     max - 1, -1, -1),
        q("MAX_VALUE - ulp",                   max, -1, -2),
        q("MAX_VALUE",                         max, -1, -1));
  }

  /**
   * The values in strictly ascending order (in the sense of {@code Double.compare()}):
   * -Infinity, negative values, -0, +0, positive values, +Infinity, NaN.
   */
  public static List<Named<Quadruple>> ladder() {
    final List<Named<Quadruple>> positive = positiveSteps();
    final List<Named<Quadruple>> negative = new ArrayList<>();
    for (final Named<Quadruple> v : positive)
      negative.add(negated(v));
    Collections.reverse(negative);

    final List<Named<Quadruple>> ladder = new ArrayList<>();
    ladder.add(Named.of("-Infinity", negativeInfinity()));
    ladder.addAll(negative);
    ladder.add(q("-0", true, 0, 0, 0));
    ladder.add(q("+0", false, 0, 0, 0));
    ladder.addAll(positive);
    ladder.add(Named.of("+Infinity", positiveInfinity()));
    ladder.add(Named.of("NaN", nan()));
    return ladder;
  }

  /** NaNs with different bit patterns: all of them are NaN, and all of them must be equal to each other */
  private static List<Named<Quadruple>> nans() {
    return List.of(
        Named.of("NaN", nan()),
        q("NaN, mantLo = 1",        false, EXPONENT_OF_INFINITY, 0, 1),
        q("-NaN",                   true,  EXPONENT_OF_INFINITY, HIGH_BIT, 0),
        q("-NaN, all bits set",     true,  EXPONENT_OF_INFINITY, -1, -1));
  }

  /** All pairs of the ladder values, with the expected result of the comparison: {@code (a, b, Integer.compare(i, j))} */
  public static Stream<Arguments> ladderPairs() {
    final List<Named<Quadruple>> ladder = ladder();
    final List<Arguments> pairs = new ArrayList<>();
    for (int i = 0; i < ladder.size(); i++)
      for (int j = 0; j < ladder.size(); j++)
        pairs.add(Arguments.of(copy(ladder.get(i)), copy(ladder.get(j)), Integer.compare(i, j)));
    return pairs.stream();
  }

  /* ************************************************************************
   * Pairs of Quadruples
   */

  /**
   * All pairs: the pairs of the ladder values, NaNs with different bit patterns paired with the ladder values
   * and with each other, special values paired with random values, and random pairs of close values
   * (see {@link RandomGenerator#quadruplePairs(int)}).
   */
  public static Stream<Arguments> pairs() {
    return allPairs().map(Pair::toArguments);
  }

  /** Pairs for {@code equals()}: all pairs except those of two NaNs (BUG-06) */
  public static Stream<Arguments> pairsForEquals() {
    return allPairs().filter(p -> !p.bothNaN()).map(Pair::toArguments);
  }

  /** Pairs of two NaNs (different instances, also with different bit patterns), BUG-06 */
  public static Stream<Arguments> pairsOfNaNs() {
    return allPairs().filter(Pair::bothNaN).map(Pair::toArguments);
  }

  /** Pairs for {@code min()} and {@code assignMin()}: all pairs except those with exactly one NaN (BUG-07) */
  public static Stream<Arguments> pairsForMin() {
    return allPairs().filter(p -> !p.oneNaN()).map(Pair::toArguments);
  }

  /** Pairs with exactly one NaN, BUG-07 */
  public static Stream<Arguments> pairsWithOneNaN() {
    return allPairs().filter(Pair::oneNaN).map(Pair::toArguments);
  }

  private static Stream<Pair> allPairs() {
    final List<Named<Quadruple>> ladder = ladder(), nans = nans();
    final List<Pair> pairs = new ArrayList<>();
    for (final Named<Quadruple> a : ladder)
      for (final Named<Quadruple> b : ladder)
        pairs.add(new Pair(a, b));
    for (final Named<Quadruple> nan : nans) {
      for (final Named<Quadruple> v : ladder) {
        pairs.add(new Pair(nan, v));
        pairs.add(new Pair(v, nan));
      }
      for (final Named<Quadruple> other : nans)
        pairs.add(new Pair(nan, other));
    }

    final List<Named<Quadruple>> specials = List.of(
        q("-0", true, 0, 0, 0), q("+0", false, 0, 0, 0),
        Named.of("-Infinity", negativeInfinity()), Named.of("+Infinity", positiveInfinity()),
        Named.of("NaN", nan()));
    new RandomGenerator().quadruples(RANDOM_VALUE_COUNT).forEach(q -> {
      for (final Named<Quadruple> s : specials) {
        pairs.add(new Pair(s, named(q)));
        pairs.add(new Pair(named(q), s));
      }
    });

    new RandomGenerator().quadruplePairs(RANDOM_PAIR_COUNT)
      .forEach(p -> pairs.add(new Pair(named(p[0]), named(p[1]))));
    return pairs.stream();
  }

  /** A pair of values; each pair gets its own instances */
  private record Pair(Named<Quadruple> a, Named<Quadruple> b) {
    Pair {
      a = copy(a);
      b = copy(b);
    }
    boolean bothNaN() { return a.getPayload().isNaN() && b.getPayload().isNaN(); }
    boolean oneNaN()  { return a.getPayload().isNaN() != b.getPayload().isNaN(); }
    Arguments toArguments() { return Arguments.of(a, b); }
  }

  /* ************************************************************************
   * A Quadruple and a long or a double
   */

  /**
   * Pairs {@code (Quadruple, long)}: the ladder values and NaNs paired with typical longs,
   * and random longs paired with the equal Quadruple values, the values differing from them
   * in the lowest bit, the values of the opposite sign, and random values.
   */
  public static Stream<Arguments> quadrupleAndLongPairs() {
    final List<Arguments> pairs = new ArrayList<>();
    final long[] typical = { 0, 1, -1, 2, -2, Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE + 1 };
    final List<Named<Quadruple>> values = new ArrayList<>(ladder());
    values.addAll(nans());
    for (final Named<Quadruple> v : values)
      for (final long l : typical)
        pairs.add(Arguments.of(copy(v), l));
    for (final long l : typical)
      neighborsOf(new Quadruple(l)).forEach(q -> pairs.add(Arguments.of(named(q), l)));

    final RandomGenerator generator = new RandomGenerator();
    final Quadruple[] randoms = generator.quadruples(RANDOM_NUMBER_COUNT).toArray(Quadruple[]::new);
    final long[] longs = generator.longs(RANDOM_NUMBER_COUNT).toArray();
    for (int i = 0; i < longs.length; i++) {
      final long l = longs[i];
      neighborsOf(new Quadruple(l)).forEach(q -> pairs.add(Arguments.of(named(q), l)));
      pairs.add(Arguments.of(named(randoms[i]), l));
    }
    return pairs.stream();
  }

  /**
   * Pairs {@code (Quadruple, double)}: the ladder values and NaNs paired with special and typical doubles,
   * and random doubles (normal and subnormal) paired with the equal Quadruple values, the values differing from them
   * in the lowest bit, the values of the opposite sign, and random values.
   */
  public static Stream<Arguments> quadrupleAndDoublePairs() {
    final List<Arguments> pairs = new ArrayList<>();
    final double[] typical = { Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0, -0.0,
                               1.0, -1.0, Double.MIN_VALUE, -Double.MIN_VALUE, Double.MIN_NORMAL,
                               Double.MAX_VALUE, -Double.MAX_VALUE };
    final List<Named<Quadruple>> values = new ArrayList<>(ladder());
    values.addAll(nans());
    for (final Named<Quadruple> v : values)
      for (final double d : typical)
        pairs.add(Arguments.of(copy(v), d));
    for (final double d : typical)
      if (Double.isFinite(d))
        neighborsOf(new Quadruple(d)).forEach(q -> pairs.add(Arguments.of(named(q), d)));

    final RandomGenerator generator = new RandomGenerator();
    final Quadruple[] randoms = generator.quadruples(RANDOM_NUMBER_COUNT).toArray(Quadruple[]::new);
    final double[] doubles = DoubleStream.concat(generator.doubles(RANDOM_NUMBER_COUNT),
                                                 generator.subnormalDoubles(RANDOM_NUMBER_COUNT / 4)).toArray();
    for (int i = 0; i < doubles.length; i++) {
      final double d = doubles[i];
      neighborsOf(new Quadruple(d)).forEach(q -> pairs.add(Arguments.of(named(q), d)));
      pairs.add(Arguments.of(named(randoms[i % randoms.length]), d));
    }
    return pairs.stream();
  }

  /**
   * The value itself, the adjacent values (greater and less in magnitude by the ulp; but zero has no lesser one),
   * and the value with the opposite sign
   */
  private static Stream<Quadruple> neighborsOf(Quadruple q) {
    final boolean negative = q.isNegative();
    final int exponent = q.exponent();
    final long mantHi = q.mantHi(), mantLo = q.mantLo();
    final Stream<Quadruple> lesser = q.isZero()? Stream.empty() :
        mantLo != 0? Stream.of(new Quadruple(negative, exponent, mantHi, mantLo - 1)) :
        mantHi != 0? Stream.of(new Quadruple(negative, exponent, mantHi - 1, -1)) :
                     Stream.of(new Quadruple(negative, exponent - 1, -1, -1));     // the previous binade
    final Stream<Quadruple> greater = mantLo != -1? Stream.of(new Quadruple(negative, exponent, mantHi, mantLo + 1)) :
                                      mantHi != -1? Stream.of(new Quadruple(negative, exponent, mantHi + 1, 0)) :
                                                    Stream.of(new Quadruple(negative, exponent + 1, 0, 0));
    return Stream.of(Stream.of(q), lesser, greater, Stream.of(new Quadruple(!negative, exponent, mantHi, mantLo)))
                 .flatMap(s -> s);
  }

  /* ************************************************************************
   * Single values
   */

  /** Values for {@code signum()}: the ladder values except -0 and NaN (BUG-08), and random values */
  public static Stream<Named<Quadruple>> valuesForSignum() {
    return Stream.concat(
        ladder().stream().filter(v -> !isNegativeZeroOrNaN(v.getPayload())),
        new RandomGenerator().quadruples(RANDOM_VALUE_COUNT).map(DataHelpers::named));
  }

  /** -0 and NaNs with different bit patterns, BUG-08 */
  public static Stream<Named<Quadruple>> negativeZeroAndNaNs() {
    return Stream.concat(Stream.of(q("-0", true, 0, 0, 0)), nans().stream());
  }

  private static boolean isNegativeZeroOrNaN(Quadruple q) {
    return q.isNaN() || q.isZero() && q.isNegative();
  }

  /* ************************************************************************
   * Helpers
   */

  private static Named<Quadruple> q(String name, int exponent, long mantHi, long mantLo) {
    return q(name, false, exponent, mantHi, mantLo);
  }

  private static Named<Quadruple> q(String name, boolean negative, int exponent, long mantHi, long mantLo) {
    return Named.of(name, new Quadruple(negative, exponent, mantHi, mantLo));
  }

}
