package com.mvohm.quadruple;

import static com.mvohm.quadruple.testutil.QuadAssert.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.mvohm.quadruple.testdata.generators.RandomGenerator;

/**
 * Thread safety: the class is conditionally thread-safe, i.e. different instances may be used concurrently
 * (an instance may not be modified concurrently). The risk is in the shared static state:
 * thread-local buffers and parser, tables of powers, etc.
 * <p>
 * The results of all operations are first computed in a single thread; then several threads, started at the same
 * moment, compute them concurrently, each in its own random order of operations, and many times; all the results
 * must be the same. The operands are <b>shared</b> by all threads: the static operations only read them,
 * the instance operations are performed on the thread's own copies; the operands must remain unchanged.
 * <p>
 * Slow ({@code @Tag("slow")}), excluded from the default run; run it with
 * {@code mvn test -Dtest.excludedGroups=none -Dgroups=slow} (or along with the others: {@code -Dtest.excludedGroups=none}).
 */
@Tag("slow")
public class ThreadSafetyTest {

  private static final int DATA_SIZE = 20_000;
  private static final int ROUNDS = 5;
  private static final int THREADS = Math.max(4, Runtime.getRuntime().availableProcessors());
  private static final int TIMEOUT_MINUTES = 10;

  /** An operation on a pair of shared operands, giving a result comparable by {@code equals()} */
  private record Operation(String name, Function<Quadruple[], Object> function) {}

  private static final List<Operation> OPERATIONS = List.of(
      new Operation("Quadruple.add(a, b)",      p -> fields(Quadruple.add(p[0], p[1]))),
      new Operation("copy(a).add(b)",           p -> fields(new Quadruple(p[0]).add(p[1]))),
      new Operation("Quadruple.subtract(a, b)", p -> fields(Quadruple.subtract(p[0], p[1]))),
      new Operation("copy(a).subtract(b)",      p -> fields(new Quadruple(p[0]).subtract(p[1]))),
      new Operation("Quadruple.multiply(a, b)", p -> fields(Quadruple.multiply(p[0], p[1]))),
      new Operation("copy(a).multiply(b)",      p -> fields(new Quadruple(p[0]).multiply(p[1]))),
      new Operation("Quadruple.divide(a, b)",   p -> fields(Quadruple.divide(p[0], p[1]))),
      new Operation("copy(a).divide(b)",        p -> fields(new Quadruple(p[0]).divide(p[1]))),
      new Operation("Quadruple.sqrt(a)",        p -> fields(Quadruple.sqrt(p[0]))),
      new Operation("a.toString()",             p -> p[0].toString()),
      new Operation("new Quadruple(b.toString())", p -> fields(new Quadruple(p[1].toString()))),
      new Operation("a.doubleValue()",          p -> Double.doubleToRawLongBits(p[0].doubleValue())),
      new Operation("a.bigDecimalValue()",      p -> p[0].isNaN() || p[0].isInfinite()? "" : p[0].bigDecimalValue()),
      new Operation("a.compareTo(b)",           p -> Integer.signum(p[0].compareTo(p[1]))));

  @Test
  void concurrentOperationsShouldGiveTheSameResults() throws Exception {
    final List<Quadruple[]> operands = operands();
    final List<Quadruple[]> originals = operands.stream().map(p -> new Quadruple[] {new Quadruple(p[0]), new Quadruple(p[1])})
                                                .toList();
    final Object[][] expected = compute(operands, OPERATIONS);            // single-threaded

    final CyclicBarrier start = new CyclicBarrier(THREADS);
    final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
    try {
      final List<Future<List<String>>> futures = new ArrayList<>();
      for (int t = 0; t < THREADS; t++) {
        final long seed = t;
        futures.add(executor.submit(() -> {
          final List<Operation> order = new ArrayList<>(OPERATIONS);
          final Random random = new Random(seed);
          final List<String> mismatches = new ArrayList<>();
          start.await();
          for (int round = 0; round < ROUNDS; round++) {
            Collections.shuffle(order, random);
            final Object[][] actual = compute(operands, order);
            for (int op = 0; op < order.size() && mismatches.size() < 10; op++) {
              final int index = OPERATIONS.indexOf(order.get(op));
              for (int i = 0; i < operands.size() && mismatches.size() < 10; i++)
                if (!expected[index][i].equals(actual[op][i]))
                  mismatches.add(String.format("%s, round %s: %s, a = %s, b = %s: expected %s, actual %s",
                      Thread.currentThread().getName(), round, order.get(op).name(),
                      fieldsOf(originals.get(i)[0]), fieldsOf(originals.get(i)[1]), expected[index][i], actual[op][i]));
            }
          }
          return mismatches;
        }));
      }
      final List<String> mismatches = new ArrayList<>();
      for (final Future<List<String>> f : futures)
        mismatches.addAll(f.get(TIMEOUT_MINUTES, TimeUnit.MINUTES));
      assertThat(mismatches).as("results of concurrent operations differing from the single-threaded ones").isEmpty();
    } finally {
      executor.shutdownNow();
    }

    for (int i = 0; i < operands.size(); i++) {
      assertSameFields(operands.get(i)[0], originals.get(i)[0], "shared operand a #" + i);
      assertSameFields(operands.get(i)[1], originals.get(i)[1], "shared operand b #" + i);
    }
  }

  /* ************************************************************************
   * Helpers
   */

  /** Random pairs over the whole range, with special values, and with moderate exponents */
  private static List<Quadruple[]> operands() {
    final List<Quadruple[]> list = new ArrayList<>();
    new RandomGenerator().quadruplePairs(DATA_SIZE / 2).forEach(list::add);
    final List<Quadruple> moderate = new RandomGenerator(RandomGenerator.DEFAULT_RANDOM_SEED + 1)
                                       .quadruples(DATA_SIZE, -500, 500).toList();
    for (int i = 0; i + 1 < moderate.size(); i += 2)
      list.add(new Quadruple[] {moderate.get(i), moderate.get(i + 1)});
    final Quadruple[] specials = {new Quadruple(), new Quadruple(-0.0), Quadruple.nan(),
                                  Quadruple.positiveInfinity(), Quadruple.negativeInfinity(), Quadruple.minValue()};
    for (final Quadruple s : specials)
      for (final Quadruple t : specials)
        list.add(new Quadruple[] {s, t});
    return list;
  }

  /** @return {@code results[op][i]}: the results of the operations, in the given order, on all the operands */
  private static Object[][] compute(List<Quadruple[]> operands, List<Operation> operations) {
    final Object[][] results = new Object[operations.size()][operands.size()];
    for (int op = 0; op < operations.size(); op++)
      for (int i = 0; i < operands.size(); i++)
        results[op][i] = operations.get(op).function().apply(operands.get(i));
    return results;
  }

  /** The fields of a value, comparable by {@code equals()} */
  private static List<Object> fields(Quadruple q) {
    return Arrays.asList(q.isNegative(), q.exponent(), q.mantHi(), q.mantLo());
  }

}
