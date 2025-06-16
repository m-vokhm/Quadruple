/**
 *
 */
package com.mvohm.quadruple.test;

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

import static com.mvohm.quadruple.test.AuxMethods.*;
/**
 *
 */
public class ThreadSafetyTest_01 {

  // With these values and doubles, it needs 8 GB: use -Xmx8G
  private final static int DATA_SIZE = 4_000_000;
  private final static int NUMBER_OF_THREADS = 16;

  static double[] args1;
  static double[] args2;
  static String[] strings;

  static class ResultSet {
    int size;
    double[] multiplicationResults = new double[DATA_SIZE];
    double[] divisionResults = new double[DATA_SIZE];
    double[] sqrtResults = new double[DATA_SIZE];
    double[] parseResults = new double[DATA_SIZE];
    String[] toStringResults = new String[DATA_SIZE];
    enum OPCODES {
      MULTIPLICATIOIN, DIVISION, SQRT, PARSE, TOSTRING,
    };
    List<OPCODES> operations;
    String signature = "";

    ResultSet(int size) {
      this.size = size;
      multiplicationResults = new double[size];
      divisionResults = new double[size];
      sqrtResults = new double[size];
      parseResults = new double[size];
      toStringResults = new String[size];

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

    private void performDivisions() {
      for (int i = 0; i < size; i++) {
        divisionResults[i] = args1[i] / args2[i];
      }
      say(signature + " Performed division");
    }

    private void performMultiplications() {
      for (int i = 0; i < size; i++) {
        multiplicationResults[i] = args1[i] * args2[i];
      }
      say(signature + " Performed multiplication");
    }

    private void performSqrt() {
      for (int i = 0; i < size; i++) {
        sqrtResults[i] = Math.sqrt(args1[i]);
      }
      say(signature + " Performed sqrt");
    }

    private void performParse() {
      for (int i = 0; i < size; i++) {
        parseResults[i] = Double.valueOf(strings[i]);
      }
      say(signature + " Performed parse");
    }

    private void performToString() {
      for (int i = 0; i < size; i++) {
        toStringResults[i] = Double.toString(args1[i]);
      }
      say(signature + " Performed toString()");
    }

    public int verifyMultiplicationResults(double[] multiplicationResults2) {
      final int errorCount = compareArtrays(multiplicationResults, multiplicationResults2);
      if (errorCount != 0) {
        say("Multiplivation errors: " + errorCount);
      }
      return errorCount;
    }

    public int verifyDivisionResults(double[] divisionResults2) {
      final int errorCount = compareArtrays(divisionResults, divisionResults2);
      if (errorCount != 0) {
        say("Division errors:       " + errorCount);
      }
      return errorCount;
    }

    public int verifySqrtResults(double[] sqrtResults2) {
      final int errorCount = compareArtrays(sqrtResults, sqrtResults2);
      if (errorCount != 0) {
        say("sqrt() errors:         " + errorCount);
      }
      return errorCount;
    }

    public int verifyParseResults(double[] parseResults2) {
      final int errorCount = compareArtrays(parseResults, parseResults2);
      if (errorCount != 0) {
        say("parse errors:          " + errorCount);
      }
      return errorCount;
    }

    public int verifytoStringResults(String[] toStringResults2) {
      final int errorCount = compareArtrays(toStringResults2, toStringResults2);
      if (errorCount != 0) {
        say("parse errors:          " + errorCount);
      }
      return errorCount;
    }

    private int compareArtrays(double[] array1, double[] array2) {
      int errorCount = 0;
      for (int i = 0; i < array1.length; i++) {
        if (array1[i] != array2[i]
            && !(Double.isNaN(array1[i]) && Double.isNaN(array2[i]) )
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
    final ResultSet model = initData();
    final ExecutorService executors = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    final List<Future<ResultSet>> futures = runConcurentComputations(executors);
    executors.shutdown();
    waitForCompletion(executors);

    final int errors = verifyResults(futures, model);
    say("-----------------------------");
    say("Total %s errors", errors);
    say("======================");
    say("Done.");
  }

  private static void waitForCompletion(final ExecutorService executors) {
    say("Waiting for completion...");
    try {
      final boolean tasksFinished = executors.awaitTermination(30, TimeUnit.SECONDS);
      if (tasksFinished) {
        System.out.println("All tasks completed within the timeout.");
      } else {
        System.out.println("Timeout occurred before all tasks finished.");
      }
    } catch (final InterruptedException e) {
      System.out.println("Await termination interrupted.");
      Thread.currentThread().interrupt();
    }
  }

  private static List<Future<ResultSet>> runConcurentComputations(ExecutorService executors) {
    final List<Future<ResultSet>> futures = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_THREADS; i++) {
      futures.add(executors.submit(callableTask));
    }
    return futures;
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
    args1 = new double[DATA_SIZE];
    args2 = new double[DATA_SIZE];
    strings = new String[DATA_SIZE];
    final Random random = new Random();
    for (int i = 0; i < DATA_SIZE; i++) {
      args1[i] = random.nextDouble() * random.nextInt();
      args1[i] = random.nextDouble() * random.nextInt();
      strings[i] = Double.toString(random.nextDouble() * random.nextInt());
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
