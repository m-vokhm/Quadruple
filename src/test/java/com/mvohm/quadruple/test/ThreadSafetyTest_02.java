/**
 *
 */
package com.mvohm.quadruple.test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.mvohm.quadruple.Quadruple;

import static com.mvohm.quadruple.test.AuxMethods.*;
/**
 *
 */
public class ThreadSafetyTest_02 {

  // With these values and quadruples, it needs 8 GB: use -Xmx8G TODO ???
  private final static int DATA_SIZE = 100_000; // 200_000;
  private final static int NUMBER_OF_THREADS = 16;
  private final static int RAND_SEED = 123;
  private final static int TIMEOUT = 15;  // will awaitTermination in 30 seconds

  static Quadruple[] args1;
  static Quadruple[] args2;
  static String[] strings;

  static class ResultSet {
    int size;
    Quadruple[] multiplicationResults;
    Quadruple[] divisionResults;
    Quadruple[] sqrtResults;
    Quadruple[] parseResults;
    String[] toStringResults;
    enum OPCODES {
      MULTIPLICATIOIN, DIVISION, SQRT, PARSE, TOSTRING,
    };
    List<OPCODES> operations;
    String signature = "";

    ResultSet(int size) {
      this.size = size;
      multiplicationResults = new Quadruple[size];
      divisionResults = new Quadruple[size];
      sqrtResults = new Quadruple[size];
      parseResults = new Quadruple[size];
      toStringResults = new String[size];

      // To provide different order of performing operations
      operations = Arrays.asList(OPCODES.values());
      Collections.shuffle(operations);
    }

    void computeResults() {
      signature = Thread.currentThread().getName();
      if (signature.length() > 4) {
        signature = String.format("%-9s", signature.substring(signature.indexOf("thread")));
      }
      for(final OPCODES opcode: operations) {
        switch (opcode) {
          case DIVISION:
            performDivisions(); break;
          case MULTIPLICATIOIN:
            performMultiplications(); break;
          case SQRT:
            performSqrt(); break;
          case PARSE:
            performParse(); break;
          case TOSTRING:
            performToString(); break;
        }
      }
    }

    private void performMultiplications() {
      for (int i = 0; i < size; i++) {
        multiplicationResults[i] = Quadruple.multiply(args1[i], args2[i]);
      }
      say(signature + " done multiplication");
    }

    private void performDivisions() {
      for (int i = 0; i < size; i++) {
        divisionResults[i] =  Quadruple.divide(args1[i], args2[i]);
      }
      say(signature + " done division");
    }

    private void performSqrt() {
      for (int i = 0; i < size; i++) {
        sqrtResults[i] = Quadruple.sqrt(args1[i]);
      }
      say(signature + " done sqrt");
    }

    private void performParse() {
      for (int i = 0; i < size; i++) {
        parseResults[i] = new Quadruple(strings[i]);
      }
      say(signature + " done parse");
    }

    private void performToString() {
      for (int i = 0; i < size; i++) {
        toStringResults[i] = args1[i].toString();
      }
      say(signature + " done toString()");
    }

    public int verifyMultiplicationResults(Quadruple[] multiplicationResults2) {
      final int errorCount = compareArtrays(multiplicationResults, multiplicationResults2);
      if (errorCount != 0) {
        say("Multiplication errors: %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifyDivisionResults(Quadruple[] divisionResults2) {
      final int errorCount = compareArtrays(divisionResults, divisionResults2);
      if (errorCount != 0) {
        say("Division errors:       %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifySqrtResults(Quadruple[] sqrtResults2) {
      final int errorCount = compareArtrays(sqrtResults, sqrtResults2);
      if (errorCount != 0) {
        say("sqrt() errors:         %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifyParseResults(Quadruple[] parseResults2) {
      final int errorCount = compareArtrays(parseResults, parseResults2);
      if (errorCount != 0) {
        say("parse errors:          %-6s", errorCount);
      }
      return errorCount;
    }

    public int verifytoStringResults(String[] toStringResults2) {
      final int errorCount = compareArtrays(toStringResults, toStringResults2);
      if (errorCount != 0) {
        say("Tostring errors:       %-6s", errorCount);
      }
      return errorCount;
    }

    private int compareArtrays(Quadruple[] array1, Quadruple[] array2) {
      int errorCount = 0;
      for (int i = 0; i < array1.length; i++) {
        if (!array1[i].equals(array2[i])
            && !(array1[i].isNaN() && array2[i].isNaN())
            ) {
          errorCount++;
        }
      }
      return errorCount;
    }

    private int compareArtrays(String[] array1, String[] array2) {
      int errorCount = 0;
      for (int i = 0; i < array1.length; i++) {
        if (!array1[i].equals(array2[i])) {
          errorCount++;
        }
      }
      return errorCount;
    }

  }

  static Callable<ResultSet> callableTask = () -> {
    final ResultSet result= new ResultSet(DATA_SIZE);
    result.computeResults();
    return result;
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    final LocalDateTime startTime = LocalDateTime.now();
    final ResultSet model = initData();

    final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    final List<Future<ResultSet>> futures = runConcurentComputations(executor);

    executor.shutdown();
    waitForCompletion(executor);

    final int errors = verifyResults(futures, model);

    final LocalDateTime endTime = LocalDateTime.now();
    final Duration elapsed = Duration.between(startTime, endTime);
    say("-----------------------------");
    say("Total %s errors", errors);
    say("======================");
    say("Done in %02d.%03d", elapsed.toSeconds(), elapsed.toMillisPart());
  }

  private static final Object[] threadRefs = new Object[NUMBER_OF_THREADS];

  private static List<Future<ResultSet>> runConcurentComputations(ExecutorService executor) {
    final List<Future<ResultSet>> futures = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_THREADS; i++) {
      final AtomicReference<Thread> threadRef = new AtomicReference<>();
      futures.add(executor.submit(() -> {
        threadRef.set(Thread.currentThread());
        final ResultSet result= new ResultSet(DATA_SIZE);
        result.computeResults();
        return result;
      }));
      threadRefs[i] = threadRef;
    }
    return futures;
  }

  private static void waitForCompletion(final ExecutorService executor) {
    say("Waiting for completion...");
    try {
      final boolean tasksFinished = executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
      if (tasksFinished) {
        System.out.println("All tasks completed within the timeout.");
      } else {
        System.out.println("Timeout occurred before all tasks finished.");
        executor.shutdownNow();
        for (final Object obj: threadRefs) {
          @SuppressWarnings("unchecked")
          final AtomicReference<Thread> threadRef = (AtomicReference<Thread>) obj;
          final Thread thread = threadRef.get();
          if (thread.getState() == Thread.State.RUNNABLE) {
            try {
              thread.interrupt();
            } catch (final Exception x) { x.printStackTrace(); }
          }
        }
      }
    } catch (final InterruptedException e) {
      System.out.println("Await termination interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  private static ResultSet initData() {
    say("Generating test data......");
    // Prepare common source data for all datasets
    prepareSourceData();

    // Compute a model with a priori correct results;
    final ResultSet model = new ResultSet(DATA_SIZE);
    model.computeResults();
    say("---------------------------");
    return model;
  }

  private static void prepareSourceData() {
    args1 = new Quadruple[DATA_SIZE];
    args2 = new Quadruple[DATA_SIZE];
    strings = new String[DATA_SIZE];
    final Random random = new Random(RAND_SEED);
    for (int i = 0; i < DATA_SIZE; i++) {
      args1[i] = Quadruple.nextRandom(random).multiply(Math.pow(2.0, random.nextInt(-1024, 1024)));
      args2[i] = Quadruple.nextRandom(random).multiply(Math.pow(2.0, random.nextInt(-1024, 1024)));
      Quadruple qq;
      if (random.nextInt() % 111 == 0) {
        qq = Quadruple.nan();
      } else if (random.nextInt() % 107 == 0) {
        qq = (random.nextBoolean())? Quadruple.positiveInfinity() : Quadruple.negativeInfinity();
      } else {
        qq = Quadruple.nextRandom(random).multiply(Math.pow(2.0, random.nextInt(-1024, 1024)));
      }
      final String s = qq.toString();
      strings[i] = s;
    }
  }

  private static int verifyResults(List<Future<ResultSet>> futures, ResultSet model) {
    say("---------------------------");
    int errors = 0, unavailable = 0;
    for (final Future<ResultSet> future: futures) {
      ResultSet result = null;

      try {
        result = future.get();
      } catch (InterruptedException | ExecutionException x) { x.printStackTrace(); }

      if (result != null) {
        say("Verifing " + result.signature + " results:");
        errors += verify(result, model);
      } else {
        say("Could not get the result...");
        unavailable += 1;
      }
      say();
    }
    if (unavailable != 0) {
      say("%s results are unavailable", unavailable);
      return -errors;
    }
    return errors;
  }

  private static int verify(ResultSet result, ResultSet model) {
    int errors = 0;
    errors += model.verifyMultiplicationResults(result.multiplicationResults);
    errors += model.verifyDivisionResults(result.divisionResults);
    errors += model.verifySqrtResults(result.sqrtResults);
    errors += model.verifyParseResults(result.parseResults);
    errors += model.verifytoStringResults(result.toStringResults);
    if (errors == 0) {
      say("OK.");
    }
    return errors;
  }

}
